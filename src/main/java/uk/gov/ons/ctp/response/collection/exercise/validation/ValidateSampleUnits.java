package uk.gov.ons.ctp.response.collection.exercise.validation;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
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
public class ValidateSampleUnits {
  private static final Logger log = LoggerFactory.getLogger(ValidateSampleUnits.class);

  private static final String CASE_TYPE_SELECTOR = "COLLECTION_INSTRUMENT";
  private static final String VALIDATION_LIST_ID = "group";
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  private static final String SURVEY_CLASSIFIER_TYPES_NOT_FOUND =
      "Survey classifier types not found";

  private AppConfig appConfig;

  private CollectionExerciseService collexService;
  private ExerciseSampleUnitService sampleUnitSvc;
  private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

  private CollectionInstrumentSvcClient collectionInstrumentSvcClient;
  private PartySvcClient partySvcClient;
  private SurveySvcClient surveySvcClient;

  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  private DistributedListManager<Integer> sampleValidationListManager;

  public ValidateSampleUnits(
      final AppConfig appConfig,
      final CollectionExerciseService collexService,
      final ExerciseSampleUnitService sampleUnitSvc,
      final ExerciseSampleUnitGroupService sampleUnitGroupSvc,
      final CollectionInstrumentSvcClient collectionInstrumentSvcClient,
      final PartySvcClient partySvcClient,
      final SurveySvcClient surveySvcClient,
      final @Qualifier("sampleUnitGroup") StateTransitionManager<
                  SampleUnitGroupState, SampleUnitGroupEvent>
              sampleUnitGroupState,
      final @Qualifier("validation") DistributedListManager<Integer> sampleValidationListManager) {
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
    if (exercises.isEmpty()) {
      return;
    }

    try {
      List<ExerciseSampleUnitGroup> sampleUnitGroups = retrieveSampleUnitGroups(exercises);
      Map<CollectionExercise, List<ExerciseSampleUnitGroup>> collections =
          sampleUnitGroups
              .stream()
              .collect(Collectors.groupingBy(ExerciseSampleUnitGroup::getCollectionExercise));

      collections.forEach(
          (exercise, groups) -> {
            try {
              addCollectionInstrumentIds(exercise, groups);
              transitionCollectionExercise(exercise);
            } catch (CTPException e) {
              log.error(
                  "Error validating collection exercise, collectionExerciseId: {}",
                  exercise.getId());
              log.error("Stack trace: {}", e);
            }
          });

    } catch (LockingException ex) {
      log.error("Failed to get lock", ex);
    } finally {
      try {
        sampleValidationListManager.deleteList(VALIDATION_LIST_ID, true);
      } catch (LockingException ex) {
        log.error("Failed to delete lock list", ex);
      }
    }
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

    List<Integer> excludedGroups = sampleValidationListManager.findList(VALIDATION_LIST_ID, false);
    log.with("excluded_groups", excludedGroups)
        .debug("VALIDATION - Retrieve sampleUnitGroups excluding");

    excludedGroups.add(IMPOSSIBLE_ID);
    List<ExerciseSampleUnitGroup> sampleUnitGroups =
        sampleUnitGroupSvc
            .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
                SampleUnitGroupState.INIT,
                exercises,
                excludedGroups,
                new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax()));

    if (!CollectionUtils.isEmpty(sampleUnitGroups)) {
      String sampleGroupPks =
          sampleUnitGroups
              .stream()
              .map(group -> group.getSampleUnitGroupPK().toString())
              .collect(Collectors.joining(","));
      log.with("sample_unit_group_pks", sampleGroupPks)
          .debug("VALIDATION retrieved sampleUnitGroup PKs");
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
   * Add collection instrument id and party id to each SampleUnit
   *
   * @param exercise for which to validate the SampleUnitGroups
   * @param sampleUnitGroups in exercise
   */
  private void addCollectionInstrumentIds(
      CollectionExercise exercise, List<ExerciseSampleUnitGroup> sampleUnitGroups)
      throws CTPException {

    List<String> classifierTypes = requestSurveyClassifiers(exercise);
    if (classifierTypes.isEmpty()) {
      log.with("survey_id", exercise.getSurveyId().toString())
          .error("Failed to retrieve survey classifiers");
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format(
              "%s, surveyId: %s", SURVEY_CLASSIFIER_TYPES_NOT_FOUND, exercise.getId().toString()));
    }

    for (ExerciseSampleUnitGroup sampleUnitGroup : sampleUnitGroups) {
      List<ExerciseSampleUnit> sampleUnits = sampleUnitSvc.findBySampleUnitGroup(sampleUnitGroup);
      for (ExerciseSampleUnit sampleUnit : sampleUnits) {
        try {
          UUID collectionInstrumentId =
              requestCollectionInstrumentId(
                  classifierTypes, sampleUnit, exercise.getSurveyId().toString());
          sampleUnit.setCollectionInstrumentId(collectionInstrumentId);

          if (sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.B) {
            PartyDTO party =
                partySvcClient.requestParty(
                    sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
            sampleUnit.setPartyId(UUID.fromString(party.getId()));
          }
        } catch (RestClientException ex) {
          log.with("sample_unit_group_PK", sampleUnitGroup.getSampleUnitGroupPK())
              .error("Error in validation of SampleUnitGroup", ex);
        }
      }
      saveUpdatedSampleUnits(sampleUnitGroup, sampleUnits);
    }
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
      log.error("Error retrieving survey classifiers, error: {}", ex.getMessage());
      log.error(ex.toString());
    }

    return classifierTypes;
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
      List<String> classifierTypes, ExerciseSampleUnit sampleUnit, String surveyId) {
    Map<String, String> classifiers = new HashMap<>();
    classifiers.put("SURVEY_ID", surveyId);
    for (String classifier : classifierTypes) {
      try {
        CollectionInstrumentClassifierTypes classifierType =
            CollectionInstrumentClassifierTypes.valueOf(classifier);
        classifiers.put(classifierType.name(), classifierType.apply(sampleUnit));
      } catch (IllegalArgumentException e) {
        log.with("classifier", classifier).warn("Classifier not supported", e);
      }
    }
    String searchString = convertToJSON(classifiers);
    List<CollectionInstrumentDTO> collectionInstruments =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    UUID collectionInstrumentId;
    if (collectionInstruments.isEmpty()) {
      log.with("search_string", searchString).error("No collection instruments found");
      collectionInstrumentId = null;
    } else if (collectionInstruments.size() > 1) {
      log.with("collection_instruments_found", collectionInstruments.size())
          .with("search_string", searchString)
          .warn("Multiple collection instruments found, taking most recent first");
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

  private void saveUpdatedSampleUnits(
      final ExerciseSampleUnitGroup sampleUnitGroup, final List<ExerciseSampleUnit> sampleUnits) {
    ExerciseSampleUnitGroup updatedSampleUnitGroup =
        transitionSampleUnitGroupState(sampleUnitGroup, sampleUnits);
    updatedSampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
    sampleUnitGroupSvc.storeExerciseSampleUnitGroup(updatedSampleUnitGroup, sampleUnits);
  }

  private void transitionCollectionExercise(CollectionExercise exercise) throws CTPException {
    CollectionExerciseEvent event = null;
    long init =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(SampleUnitGroupState.INIT, exercise);
    long validated =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.VALIDATED, exercise);
    long failed =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.FAILEDVALIDATION, exercise);

    if (validated == exercise.getSampleSize().longValue()) {
      // All sample units validated, set exercise state to VALIDATED
      event = CollectionExerciseEvent.VALIDATE;
      log.with("collection_exercise_id", exercise.getId())
          .debug("State of collection exercise is now VALIDATE");
    } else if (init < 1 && failed > 0) {
      // None left to validate but some failed, set exercise to FAILEDVALIDATION
      log.with("collection_exercise_id", exercise.getId())
          .info("State of collection exercise is now INVALIDATED (FAILEDVALIDATION)");
      event = CollectionExerciseEvent.INVALIDATE;
    }

    if (event != null) {
      this.collexService.transitionCollectionExercise(exercise, event);
    }
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
        log.info(
            "Setting sample unit group to FAILEDVALIDATION, sampleUnitGroupPK: {}",
            sampleUnitGroup.getSampleUnitGroupPK());
        sampleUnitGroup.setStateFK(
            sampleUnitGroupState.transition(
                sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.INVALIDATE));
      }
    } catch (CTPException ex) {
      log.error("Sample Unit group state transition failed", ex);
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
