package uk.gov.ons.ctp.response.collection.exercise.validation;

import java.sql.Timestamp;
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
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
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
import uk.gov.ons.ctp.response.collection.exercise.service.PartyService;
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
  private PartyService  partyService;

  /**
   * Validate SampleUnits
   *
   */
  public void validateSampleUnits() {

    List<CollectionExercise> exercises = collectRepo
        .findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED);

    List<ExerciseSampleUnitGroup> sampleUnitGroups = sampleUnitGroupRepo
        .findByStateFKAndCollectionExerciseInOrderByCreatedDateTimeDesc(SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            exercises, new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax()));

    // Not searching DB for individual Collection Exercise above when getting
    // batch of SampleUnitGroups to process but processing in Collection
    // Exercise order will save external service calls so sorting them now.
    Map<CollectionExercise, List<ExerciseSampleUnitGroup>> collections = sampleUnitGroups.stream()
        .collect(Collectors.groupingBy(ExerciseSampleUnitGroup::getCollectionExercise));

    collections.forEach((exercise, groups) -> {

      if (!validateSampleUnits(exercise, groups)) {
        return; // Exit collection forEach as failed validation
      }

      if (sampleUnitGroupRepo.countByStateFKAndCollectionExercise(SampleUnitGroupDTO.SampleUnitGroupState.INIT,
          exercise) == 0) {
        exercise.setState(collectionExerciseTransitionState.transition(exercise.getState(),
            CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE));
        collectRepo.saveAndFlush(exercise);
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

    SurveyClassifierTypeDTO classifierTypes = requestSurveyClassifiers(exercise);
    if (classifierTypes == null) {
      return false;
    }
    Map<String, String> classifiers = new HashMap<>();
    if(classifierTypes.getClassifierTypes() != null && classifierTypes.getClassifierTypes().contains("COLLECTION_EXERCISE")){
      classifiers.put("COLLECTION_EXERCISE", exercise.getId().toString());
    }

    sampleUnitGroups.forEach((sampleUnitGroup) -> {

      // TODO Call Survey and CollectionInstrument service for
      // validation, dummy value set for CollectionInstrumentId below

      List<ExerciseSampleUnit> sampleUnits = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
      sampleUnits.forEach((sampleUnit) -> {
        PartyDTO party = partyService.requestParty(sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
        sampleUnit.setPartyId(party.getId());
        sampleUnit.setCollectionInstrumentId(getCollectionInstrument(classifiers, sampleUnit));
        sampleUnitRepo.saveAndFlush(sampleUnit);
      });

      sampleUnitGroup
          .setStateFK(sampleUnitGroupState.transition(sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.VALIDATE));
      if (sampleUnitGroup.getStateFK() == SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED) {
        sampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
        sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);
      }
    }); // End looping group
    return true;
  }
  
  private UUID getCollectionInstrument(Map<String, String> classifiers, ExerciseSampleUnit sampleUnit){
    classifiers.put("RU_REF", sampleUnit.getSampleUnitRef());
    String searchString = convertToJSON(classifiers);
    List<CollectionInstrumentDTO> requestCollectionInstruments = collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    return requestCollectionInstruments.get(0).getId();
  }
  
  private String convertToJSON(Map<String, String> map){
    JSONObject JSON = new JSONObject(map);
    return JSON.toString();
  }

  /**
   * Request the classifier type selectors from the Survey service.
   *
   * @param exercise for which to get collection instrument classifier selectors.
   * @return SurveyClassifierTypeDTO Survey classifier type selectors
   */
  private SurveyClassifierTypeDTO requestSurveyClassifiers(CollectionExercise exercise) {

    SurveyClassifierTypeDTO classifierTypes = null;

    // Call Survey Service
    // Get Classifier types for Collection Instruments
    List<SurveyClassifierDTO> classifiers = surveySvcClient
        .requestClassifierTypeSelectors(exercise.getSurvey().getId());
    SurveyClassifierDTO classifier = classifiers.stream()
        .filter(claz -> CASE_TYPE_SELECTOR.equals(claz.getName())).findAny().orElse(null);
    if (classifier != null) {
      classifierTypes = surveySvcClient
          .requestClassifierTypeSelector(exercise.getSurvey().getId(), UUID.fromString(classifier.getId()));
      if (classifierTypes == null) {
        log.error("Error requesting Survey Classifier Types for SurveyId: {},  ", exercise.getSurvey().getId(),
            classifier.getId());
      }

    } else {
      log.error("Error requesting Survey Classifier Types for SurveyId: {} caseTypeSelectorId {}",
          exercise.getSurvey().getId());
    }

    return classifierTypes;
  }

}
