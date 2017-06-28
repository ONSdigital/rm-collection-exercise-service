package uk.gov.ons.ctp.response.collection.exercise.validation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collectionInstrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;

/**
 * Class responsible for business logic to validate SampleUnits.
 *
 */
@Component
@Slf4j
public class ValidateSampleUnits {

  private static final String CASE_TYPE_SELECTOR = "COLLECTION_INSTRUMENT";

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private SampleUnitRepository sampleUnitRepo;

  @Autowired
  private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Autowired
  @Qualifier("collectionExercise")
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseTransitionState;

  @Autowired
  @Qualifier("sampleUnitGroup")
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  @Autowired
  private SurveySvcClient surveySvcClient;

  @Autowired
  private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Autowired
  private PartySvcClient partySvcClinet;

  /**
   * Validate SampleUnits
   *
   */
  public void validateSampleUnits() {
    List<CollectionExercise> exercises = collectRepo.findByState(
        CollectionExerciseDTO.CollectionExerciseState.EXECUTED);

    List<ExerciseSampleUnitGroup> sampleUnitGroups = sampleUnitGroupRepo
        .findByStateFKAndCollectionExerciseInOrderByCreatedDateTimeDesc(SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            exercises, new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax()));

    // Not searching DB for individual Collection Exercise above when getting
    // batch of SampleUnitGroups to process but
    // processing in Collection Exercise order will save external service calls
    // so sorting them now.
    Map<CollectionExercise, List<ExerciseSampleUnitGroup>> collections = sampleUnitGroups.stream()
        .collect(Collectors.groupingBy(ExerciseSampleUnitGroup::getCollectionExercise));

    collections.forEach((exercise, groups) -> {
      if (!validateSampleUnits(exercise, groups)) {
        return; // Exit collection forEach as failed validation
      }

      if (sampleUnitGroupRepo.countByStateFKAndCollectionExercise(SampleUnitGroupDTO.SampleUnitGroupState.INIT,
          exercise) == 0) {
        try {
          exercise.setState(collectionExerciseTransitionState.transition(exercise.getState(),
              CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE));
          collectRepo.saveAndFlush(exercise);
        } catch (CTPException e) {
          log.error(String.format("cause = %s - message = %s", e.getCause(), e.getMessage()));
        }
      }
    }); // End looping collections
  }

  /**
   * Validate the SampleUnitGroups for a CollectionExercise.
   *
   * @param exercise for which to validate the SampleUnitGroups
   * @param sampleUnitGroups in exercise.
   * @return boolean if validation successful.
   */
  private boolean validateSampleUnits(CollectionExercise exercise,
      List<ExerciseSampleUnitGroup> sampleUnitGroups) {

    List<String> classifierTypes = requestSurveyClassifiers(exercise);
    if (classifierTypes.isEmpty()) {
      return false;
    }

    sampleUnitGroups.forEach((sampleUnitGroup) -> {

      // TODO Look at enrolled parties returned

      List<ExerciseSampleUnit> sampleUnits = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
      sampleUnits.forEach((sampleUnit) -> {
        PartyDTO party = partySvcClinet.requestParty(sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
        sampleUnit.setPartyId(party.getId());
        sampleUnit.setCollectionInstrumentId(requestCollectionInstrumentId(classifierTypes, sampleUnit));
        sampleUnitRepo.saveAndFlush(sampleUnit);
      });

      try {
        sampleUnitGroup
            .setStateFK(sampleUnitGroupState.transition(sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.VALIDATE));
        if (sampleUnitGroup.getStateFK() == SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED) {
          sampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
          sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);
        }
      } catch (CTPException e) {
        log.error("Cause = %s - Message = %s", e.getCause(), e.getMessage());
      }
    }); // End looping group
    return true;
  }

  /**
   * Request the Collection Instrument details from the Collection Instrument
   * Service using the given classifiers and return the instrument Id.
   *
   * @param classifierTypes used in search by Collection Instrument service to
   *          return instrument details matching classifiers.
   * @param sampleUnit to which the collection instrument relates.
   * @return UUID of collection instrument.
   */
  private UUID requestCollectionInstrumentId(List<String> classifierTypes, ExerciseSampleUnit sampleUnit) {

    Map<String, String> classifiers = new HashMap<>();
    UUID collectionInstrumentId = null;
    for (String classifier : classifierTypes) {
      try {
        CollectionInstrumentClassifierTypes classifierType = CollectionInstrumentClassifierTypes.valueOf(classifier);
        classifiers.put(classifierType.name(), classifierType.apply(sampleUnit));
      } catch (IllegalArgumentException e) {
        log.error("Classifier cannot be dealt with {}", e.getMessage());
        return collectionInstrumentId;
      }
    }
    String searchString = convertToJSON(classifiers);
    List<CollectionInstrumentDTO> requestCollectionInstruments = collectionInstrumentSvcClient
        .requestCollectionInstruments(searchString);
    collectionInstrumentId = requestCollectionInstruments.get(0).getId();
    return collectionInstrumentId;
  }

  /**
   * Convert map of classifier types and values to JSON search string.
   *
   * @param classifiers classifier types and values from which to construct
   *          search String.
   * @return JSON string used in search.
   */
  private String convertToJSON(Map<String, String> classifiers) {
    JSONObject searchString = new JSONObject(classifiers);
    return searchString.toString();
  }

  /**
   * Request the classifier type selectors from the Survey service.
   *
   * @param exercise for which to get collection instrument classifier
   *          selectors.
   * @return List<String> Survey classifier type selectors for exercise
   */
  private List<String> requestSurveyClassifiers(CollectionExercise exercise) {

    SurveyClassifierTypeDTO classifierTypeSelector = null;
    List<String> classifierTypes = new ArrayList<String>();

    // Call Survey Service
    // Get Classifier types for Collection Instruments
    List<SurveyClassifierDTO> classifierTypeSelectors = surveySvcClient
        .requestClassifierTypeSelectors(exercise.getSurvey().getId());
    SurveyClassifierDTO chosenSelector = classifierTypeSelectors.stream()
        .filter(claz -> CASE_TYPE_SELECTOR.equals(claz.getName())).findAny().orElse(null);
    if (chosenSelector != null) {
      classifierTypeSelector = surveySvcClient
          .requestClassifierTypeSelector(exercise.getSurvey().getId(), UUID.fromString(chosenSelector.getId()));
      if (classifierTypeSelector != null) {
        classifierTypes = classifierTypeSelector.getClassifierTypes();
      } else {
        log.error("Error requesting Survey Classifier Types for SurveyId: {},  caseTypeSelectorId: {}",
            exercise.getSurvey().getId(), chosenSelector.getId());
      }
    } else {
      log.error("Error requesting Survey Classifier Types for SurveyId: {}",
          exercise.getSurvey().getId());
    }

    return classifierTypes;
  }

}
