package uk.gov.ons.ctp.response.collection.exercise.validation;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitService;
import uk.gov.ons.ctp.response.collection.exercise.service.impl.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.party.representation.Association;
import uk.gov.ons.ctp.response.party.representation.Enrolment;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** Class responsible for business logic to validate SampleUnits. */
@Component
public class ValidateSampleUnits {
  private static final Logger log = LoggerFactory.getLogger(ValidateSampleUnits.class);

  private static final String CASE_TYPE_SELECTOR = "COLLECTION_INSTRUMENT";
  private static final String VALIDATION_LIST_ID = "group";
  private static final String ENABLED = "ENABLED";
  // this is a bit of a kludge - jpa does not like having an IN clause with an
  // empty list
  // it does not return results when you expect it to - so ... always have this
  // in the list of excluded case ids
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  @Autowired private AppConfig appConfig;

  @Autowired private ExerciseSampleUnitService sampleUnitSvc;

  @Autowired private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

  @Autowired private CollectionExerciseService collexService;

  @Autowired
  @Qualifier("sampleUnitGroup")
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  @Autowired private SurveySvcClient surveySvcClient;

  @Autowired private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Autowired private PartySvcClient partySvcClient;

  @Autowired
  @Qualifier("validation")
  private DistributedListManager<Integer> sampleValidationListManager;

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
              generateSampleUnits(exercise, groups);

              try {
                CollectionExerciseEvent event = getCollectionExerciseTransitionState(exercise);

                if (event != null) {
                  this.collexService.transitionCollectionExercise(exercise, event);
                }
              } catch (CTPException e) {
                log.with("collection_exercise_id", exercise.getId())
                    .error("Error validating collection exercise", e);
              }
            }); // End looping collections

      } catch (LockingException ex) {
        log.error("Validation failed", ex);
      } finally {
        try {
          sampleValidationListManager.deleteList(VALIDATION_LIST_ID, true);
        } catch (LockingException ex) {
          log.error("Failed to release sampleValidationListManager data", ex);
        }
      }
    } // End exercises not empty. Just check this to save processing if going
    // to get empty sampleUnitGroups List to process
  }

  /**
   * Populate Sample units for the SampleUnitGroups for a CollectionExercise. Creates the sample
   * units for parent sample units, populating the party id by calling the party service. Also adds
   * sample units for any enrolled respondents associated with the sample unit.
   *
   * @param exercise for which to validate the SampleUnitGroups
   * @param sampleUnitGroups in exercise.
   * @return boolean false if fatal error validating, for example no classifierTypes
   */
  private void generateSampleUnits(
      CollectionExercise exercise, List<ExerciseSampleUnitGroup> sampleUnitGroups) {

    List<String> classifierTypes = requestSurveyClassifiers(exercise);

    List<ExerciseSampleUnit> sampleUnitsWithRespondents = new ArrayList<>();

    for (ExerciseSampleUnitGroup sampleUnitGroup : sampleUnitGroups) {

      List<ExerciseSampleUnit> sampleUnits = sampleUnitSvc.findBySampleUnitGroup(sampleUnitGroup);
      for (ExerciseSampleUnit sampleUnitParent : sampleUnits) {
        if (!sampleUnitParent.getSampleUnitType().isParent()) {
          log.with("sample_unit_pk", sampleUnitParent.getSampleUnitPK())
              .with("sample_unit_type", sampleUnitParent.getSampleUnitType())
              .warn("Validation for SampleUnit");
          // Skip current sampleUnit as not parent type. Respondent Unit.
          // Something must have gone wrong before?
          continue;
        }
        try {
          String surveyId = exercise.getSurveyId().toString();
          SurveyDTO survey = surveySvcClient.findSurvey(UUID.fromString(surveyId));
          UUID collectionInstrumentId =
              requestCollectionInstrumentId(classifierTypes, sampleUnitParent, surveyId);

          if (survey.getSurveyType() == SurveyDTO.SurveyType.Business) {
            sampleUnitsWithRespondents =
                createEnrolledRespondentSampleUnits(
                    sampleUnitParent, sampleUnits, sampleUnitGroup, surveyId);

          } else if (survey.getSurveyType() == SurveyDTO.SurveyType.Social) {
            sampleUnitsWithRespondents.add(sampleUnitParent);
          }

          sampleUnitsWithRespondents.forEach(
              updatedSampleUnit -> {
                updatedSampleUnit.setCollectionInstrumentId(collectionInstrumentId);
              });

        } catch (RestClientException ex) {
          log.with("sample_unit_group_pk", sampleUnitGroup.getSampleUnitGroupPK())
              .error("Error in validation for SampleUnitGroup", ex);
        }
      }

      saveUpdatedSampleUnits(sampleUnitGroup, sampleUnitsWithRespondents);
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
    log.with("excluded_groups", excludedGroups)
        .debug("VALIDATION - Retrieve sampleUnitGroups excluding");

    excludedGroups.add(IMPOSSIBLE_ID);
    sampleUnitGroups =
        sampleUnitGroupSvc
            .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
                SampleUnitGroupDTO.SampleUnitGroupState.INIT,
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
   * Request party information from the Party Service. Update reporting unit with partyId. Create a
   * SampleUnit for each enrolled respondent for the reporting unit for that survey. Operation is
   * designed to be repeatable by checking if sampleUnit already exists for partyId.
   *
   * @param sampleUnit sampled reporting unit for which to request party information.
   * @param sampleUnits all sampleUnits belonging to this group.
   * @param sampleUnitGroup group to which sampleUnit belongs.
   * @param surveyId Survey of which sampleUnit is a member.
   * @return List<ExerciseSampleUnit> of updated, created sampleUnits
   * @throws RestClientException something went wrong making http call.
   */
  private List<ExerciseSampleUnit> createEnrolledRespondentSampleUnits(
      ExerciseSampleUnit sampleUnit,
      List<ExerciseSampleUnit> sampleUnits,
      ExerciseSampleUnitGroup sampleUnitGroup,
      String surveyId)
      throws RestClientException {
    List<ExerciseSampleUnit> updatedSampleUnits = new ArrayList<>();
    PartyDTO party =
        partySvcClient.requestParty(sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
    sampleUnit.setPartyId(UUID.fromString(party.getId()));
    updatedSampleUnits.add(sampleUnit);
    party
        .getAssociations()
        .forEach(
            association -> {
              association
                  .getEnrolments()
                  .forEach(
                      enrolment -> {
                        if (surveyHasEnrolledRespondent(enrolment, surveyId)) {
                          Optional<ExerciseSampleUnit> existingRespondent =
                              findExistingRespondent(sampleUnits, association);
                          if (existingRespondent.isPresent()) {
                            log.with("sample_unit_pk", sampleUnit.getSampleUnitPK())
                                .with("party_id", association.getPartyId())
                                .warn("Respondent already exists");
                            updatedSampleUnits.add(existingRespondent.get());
                          } else {
                            ExerciseSampleUnit respondent =
                                createNewRespondent(sampleUnitGroup, sampleUnit, association);
                            updatedSampleUnits.add(respondent);
                          }
                        }
                      });
            });
    return updatedSampleUnits;
  }

  private boolean surveyHasEnrolledRespondent(final Enrolment enrolment, String surveyId) {
    return enrolment.getSurveyId().equals(surveyId)
        && enrolment.getEnrolmentStatus().equalsIgnoreCase(ENABLED);
  }

  private Optional<ExerciseSampleUnit> findExistingRespondent(
      List<ExerciseSampleUnit> sampleUnits, Association association) {
    return sampleUnits
        .stream()
        .filter(
            existingSampleUnit ->
                association.getPartyId().equals(existingSampleUnit.getPartyId().toString()))
        .findFirst();
  }

  private ExerciseSampleUnit createNewRespondent(
      final ExerciseSampleUnitGroup sampleUnitGroup,
      ExerciseSampleUnit sampleUnit,
      Association association) {
    ExerciseSampleUnit respondent = new ExerciseSampleUnit();
    respondent.setSampleUnitId(UUID.randomUUID());
    respondent.setSampleUnitGroup(sampleUnitGroup);
    respondent.setPartyId(UUID.fromString(association.getPartyId()));
    respondent.setSampleUnitRef(sampleUnit.getSampleUnitRef());
    respondent.setSampleUnitType(sampleUnit.getSampleUnitType().getChild());
    return respondent;
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
          log.with("survey_id", exercise.getSurveyId())
              .with("case_type_selector_id", chosenSelector.getId())
              .error("Error requesting Survey Classifier Types");
        }
      } else {
        log.with("survey_id", exercise.getSurveyId())
            .error("Error requesting Survey Classifier Types for Survey");
      }
    } catch (RestClientException ex) {
      log.error("Error requesting Survey service for classifierTypes", ex);
    }

    return classifierTypes;
  }

  /**
   * Transition Collection Exercise state for validation.
   *
   * @param exercise to transition.
   * @return exercise Collection Exercise with new state.
   * @throws CTPException
   */
  private CollectionExerciseEvent getCollectionExerciseTransitionState(CollectionExercise exercise)
      throws CTPException {
    log.debug("getCollectionExerciseTransitionState is called!");
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
      log.with("collection_exercise_id", exercise.getId())
          .debug("State of collection exercise is now VALIDATE");
    } else if (init < 1 && failed > 0) {
      // None left to validate but some failed, set exercise to
      // FAILEDVALIDATION
      log.with("collection_exercise_id", exercise.getId())
          .info("State of collection exercise is now INVALIDATED (FAILEDVALIDATION)");
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
