package uk.gov.ons.ctp.response.collection.exercise.validation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitService;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;

/** Class responsible for business logic to validate SampleUnits. */
@Component
@Slf4j
public class ValidateSampleUnits {

  private static final String CASE_TYPE_SELECTOR = "COLLECTION_INSTRUMENT";
  private static final String VALIDATION_LIST_ID = "group";
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  private AppConfig appConfig;

  private CollectionExerciseService collexService;
  private ExerciseSampleUnitService sampleUnitSvc;
  private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

  private CollectionInstrumentSvcClient collectionInstrumentSvcClient;
  private PartySvcClient partySvcClient;
  private SurveySvcClient surveySvcClient;

  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  private DistributedListManager<Integer> sampleValidationListManager;

  @Autowired
  public ValidateSampleUnits(
      AppConfig appConfig,
      CollectionExerciseService collexService,
      ExerciseSampleUnitService sampleUnitSvc,
      ExerciseSampleUnitGroupService sampleUnitGroupSvc,
      CollectionInstrumentSvcClient collectionInstrumentSvcClient,
      PartySvcClient partySvcClient,
      SurveySvcClient surveySvcClient,
      @Qualifier("sampleUnitGroup")
          StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState,
      @Qualifier("validation") DistributedListManager<Integer> sampleValidationListManager) {
    this.appConfig = appConfig;
    this.collexService = collexService;
    this.sampleUnitSvc = sampleUnitSvc;
    this.sampleUnitGroupSvc = sampleUnitGroupSvc;
    this.collectionInstrumentSvcClient = collectionInstrumentSvcClient;
    this.partySvcClient = partySvcClient;
    this.surveySvcClient = surveySvcClient;
    this.sampleUnitGroupState = sampleUnitGroupState;
    this.sampleValidationListManager = sampleValidationListManager;
  }

  /** Validate SampleUnits */
  public void validateSampleUnits() {

    List<CollectionExercise> exercises =
        collexService.findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED);

    if (!exercises.isEmpty()) {

      try {

        List<ExerciseSampleUnitGroup> sampleUnitGroups = retrieveSampleUnitGroups(exercises);

        // Not searching DB for individual Collection Exercise above when
        // getting batch of SampleUnitGroups to process but processing in
        // Collection Exercise order will save external service calls so sorting
        // them now.
        Map<CollectionExercise, List<ExerciseSampleUnitGroup>> collections =
            sampleUnitGroups
                .stream()
                .collect(Collectors.groupingBy(ExerciseSampleUnitGroup::getCollectionExercise));

        collections.forEach(
            (exercise, groups) -> {
              addCollectionInstrumentIds(exercise, groups);

              try {
                CollectionExerciseEvent event = getCollectionExerciseTransitionState(exercise);

                if (event != null) {
                  this.collexService.transitionCollectionExercise(exercise, event);
                }
              } catch (CTPException e) {
                log.error("Error validating collection exercise {}: {}", exercise.getId(), e);
              }
            }); // End looping collections

      } catch (LockingException ex) {
        log.error("Validation failed due to {}", ex.getMessage());
        log.error("Stack trace: " + ex);
      } finally {
        try {
          sampleValidationListManager.deleteList(VALIDATION_LIST_ID, true);
        } catch (LockingException ex) {
          log.error(
              "Failed to release sampleValidationListManager data - error msg is {}",
              ex.getMessage());
          log.error("Stack trace: " + ex);
        }
      }
    }
  }

  /**
   * Add collection instrument id and party id to each SampleUnit
   *
   * @param exercise for which to validate the SampleUnitGroups
   * @param sampleUnitGroups in exercise
   */
  private void addCollectionInstrumentIds(
      CollectionExercise exercise, List<ExerciseSampleUnitGroup> sampleUnitGroups) {

    List<String> classifierTypes = requestSurveyClassifiers(exercise);

    for (ExerciseSampleUnitGroup sampleUnitGroup : sampleUnitGroups) {
      List<ExerciseSampleUnit> sampleUnits = sampleUnitSvc.findBySampleUnitGroup(sampleUnitGroup);
      for (ExerciseSampleUnit sampleUnit : sampleUnits) {
        try {
          UUID collectionInstrumentId =
              requestCollectionInstrumentId(
                  classifierTypes, sampleUnit, exercise.getSurveyId().toString());
          sampleUnit.setCollectionInstrumentId(collectionInstrumentId);

          PartyDTO party =
              partySvcClient.requestParty(
                  sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
          sampleUnit.setPartyId(UUID.fromString(party.getId()));
        } catch (RestClientException ex) {
          log.error(
              "Error in validation for SampleUnitGroup PK: {} due to: {}",
              sampleUnitGroup.getSampleUnitGroupPK(),
              ex.getMessage());
          log.error("Stack trace: " + ex);
        }
      }
      saveUpdatedSampleUnits(sampleUnitGroup, sampleUnits);
    }
  }

  private void saveUpdatedSampleUnits(
      final ExerciseSampleUnitGroup sampleUnitGroup,
      final List<ExerciseSampleUnit> sampleUnitsWithRespondents) {
    ExerciseSampleUnitGroup updatedSampleUnitGroup =
        transitionSampleUnitGroupState(sampleUnitGroup, sampleUnitsWithRespondents);
    updatedSampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
    sampleUnitGroupSvc.storeExerciseSampleUnitGroup(
        updatedSampleUnitGroup, sampleUnitsWithRespondents);
  }

  /**
   * Retrieve SampleUnitGroups to be validated - state INIT - but do not retrieve the same
   * SampleUnitGroups as other service instances.
   *
   * @param exercises for which to return sampleUnitGroups.
   * @return list of SampleUnitGroups.
   * @throws LockingException problem obtaining lock for data shared across instances.
   */
  private List<ExerciseSampleUnitGroup> retrieveSampleUnitGroups(List<CollectionExercise> exercises)
      throws LockingException {

    List<ExerciseSampleUnitGroup> sampleUnitGroups;

    List<Integer> excludedGroups = sampleValidationListManager.findList(VALIDATION_LIST_ID, false);
    log.debug("VALIDATION - Retrieve sampleUnitGroups excluding {}", excludedGroups);

    excludedGroups.add(IMPOSSIBLE_ID);
    sampleUnitGroups =
        sampleUnitGroupSvc
            .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
                SampleUnitGroupDTO.SampleUnitGroupState.INIT,
                exercises,
                excludedGroups,
                new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax()));

    if (!CollectionUtils.isEmpty(sampleUnitGroups)) {
      log.debug(
          "VALIDATION retrieved sampleUnitGroup PKs {}",
          sampleUnitGroups
              .stream()
              .map(group -> group.getSampleUnitGroupPK().toString())
              .collect(Collectors.joining(",")));
      sampleValidationListManager.saveList(
          VALIDATION_LIST_ID,
          sampleUnitGroups
              .stream()
              .map(ExerciseSampleUnitGroup::getSampleUnitGroupPK)
              .collect(Collectors.toList()),
          true);
    } else {
      log.debug("VALIDATION retrieved 0 sampleUnitGroup PKs");
      sampleValidationListManager.unlockContainer();
    }
    return sampleUnitGroups;
  }

  /**
   * Request the Collection Instrument details from the Collection Instrument Service using the
   * given classifiers and return the instrument Id.
   *
   * @param classifierTypes used in search by Collection Instrument service to return instrument
   *     details matching classifiers.
   * @param sampleUnit to which the collection instrument relates.
   * @return UUID of collection instrument or null if not found.
   * @throws RestClientException something went wrong making http call
   */
  private UUID requestCollectionInstrumentId(
      List<String> classifierTypes, ExerciseSampleUnit sampleUnit, String surveyId)
      throws RestClientException {
    Map<String, String> classifiers = new HashMap<>();
    classifiers.put("SURVEY_ID", surveyId);
    for (String classifier : classifierTypes) {
      try {
        CollectionInstrumentClassifierTypes classifierType =
            CollectionInstrumentClassifierTypes.valueOf(classifier);
        classifiers.put(classifierType.name(), classifierType.apply(sampleUnit));
      } catch (IllegalArgumentException e) {
        log.warn("Classifier not supported {}", classifier);
      }
    }
    String searchString = convertToJSON(classifiers);
    List<CollectionInstrumentDTO> collectionInstruments =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    UUID collectionInstrumentId;
    if (collectionInstruments.isEmpty()) {
      log.error("No collection instruments found for: {}", searchString);
      collectionInstrumentId = null;
    } else if (collectionInstruments.size() > 1) {
      log.warn(
          "{} collection instruments found for: {}, taking most recent first",
          collectionInstruments.size(),
          searchString);
      collectionInstrumentId = collectionInstruments.get(0).getId();
    } else {
      collectionInstrumentId = collectionInstruments.get(0).getId();
    }

    return collectionInstrumentId;
  }

  /**
   * Convert map of classifier types and values to JSON search string.
   *
   * @param classifiers classifier types and values from which to construct search String.
   * @return JSON string used in search.
   */
  private String convertToJSON(Map<String, String> classifiers) {
    JSONObject searchString = new JSONObject(classifiers);
    return searchString.toString();
  }

  /**
   * Request the classifier type selectors from the Survey service.
   *
   * @param exercise for which to get collection instrument classifier selectors.
   * @return List<String> Survey classifier type selectors for exercise
   */
  private List<String> requestSurveyClassifiers(CollectionExercise exercise) {

    SurveyClassifierTypeDTO classifierTypeSelector;
    List<String> classifierTypes = new ArrayList<>();

    // Call Survey Service
    // Get Classifier types for Collection Instruments
    try {
      List<SurveyClassifierDTO> classifierTypeSelectors =
          surveySvcClient.requestClassifierTypeSelectors(exercise.getSurveyId());
      SurveyClassifierDTO chosenSelector =
          classifierTypeSelectors
              .stream()
              .filter(classifierType -> CASE_TYPE_SELECTOR.equals(classifierType.getName()))
              .findAny()
              .orElse(null);
      if (chosenSelector != null) {
        classifierTypeSelector =
            surveySvcClient.requestClassifierTypeSelector(
                exercise.getSurveyId(), UUID.fromString(chosenSelector.getId()));
        if (classifierTypeSelector != null) {
          classifierTypes = classifierTypeSelector.getClassifierTypes();
        } else {
          log.error(
              "Error requesting Survey Classifier Types for SurveyId: {},  caseTypeSelectorId: {}",
              exercise.getSurveyId(),
              chosenSelector.getId());
        }
      } else {
        log.error(
            "Error requesting Survey Classifier Types for SurveyId: {}", exercise.getSurveyId());
      }
    } catch (RestClientException ex) {
      log.error("Error requesting Survey service for classifierTypes: {}", ex.getMessage());
      log.error("Stack trace: " + ex);
    }

    return classifierTypes;
  }

  /**
   * Transition Collection Exercise state for validation.
   *
   * @param exercise to transition.
   * @return exercise Collection Exercise with new state.
   */
  private CollectionExerciseEvent getCollectionExerciseTransitionState(
      CollectionExercise exercise) {
    log.info("getCollectionExerciseTransitionState is called!");
    CollectionExerciseEvent event = null;
    long init =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupDTO.SampleUnitGroupState.INIT, exercise);
    long validated =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED, exercise);
    long failed =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION, exercise);

    if (validated == exercise.getSampleSize().longValue()) {
      // All sample units validated, set exercise state to VALIDATED
      event = CollectionExerciseEvent.VALIDATE;
      log.info("State of collection exercise id: {} is now VALIDATE", exercise.getId());
    } else if (init < 1 && failed > 0) {
      // None left to validate but some failed, set exercise to
      // FAILEDVALIDATION
      log.info(
          "State of collection exercise id: {} is now INVALIDATED (FAILEDVALIDATION)",
          exercise.getId());
      event = CollectionExerciseEvent.INVALIDATE;
    }

    return event;
  }

  /**
   * Transition Sample Unit Group state for validation.
   *
   * @param sampleUnitGroup to be transitioned.
   * @param sampleUnits in SampleUnitGroup.
   * @return sampleUnitGroup with new state.
   */
  private ExerciseSampleUnitGroup transitionSampleUnitGroupState(
      ExerciseSampleUnitGroup sampleUnitGroup, List<ExerciseSampleUnit> sampleUnits) {

    boolean stateValidated =
        sampleUnits.size() > 0
            && sampleUnits.stream().allMatch(ValidateSampleUnits::isSampleUnitValid);
    try {
      if (stateValidated) {
        sampleUnitGroup.setStateFK(
            sampleUnitGroupState.transition(
                sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.VALIDATE));
      } else {
        sampleUnitGroup.setStateFK(
            sampleUnitGroupState.transition(
                sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.INVALIDATE));
      }
    } catch (CTPException ex) {
      log.error("Sample Unit group state transition failed: {}", ex.getMessage());
      log.error("Stack trace: " + ex);
    }
    return sampleUnitGroup;
  }

  private static boolean isSampleUnitValid(ExerciseSampleUnit sampleUnit) {
    if (sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.H) {
      return sampleUnit.getCollectionInstrumentId() != null && sampleUnit.getPartyId() == null;
    } else if (sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.B
        || sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.BI) {
      return sampleUnit.getCollectionInstrumentId() != null && sampleUnit.getPartyId() != null;
    } else {
      return false;
    }
  }
}
