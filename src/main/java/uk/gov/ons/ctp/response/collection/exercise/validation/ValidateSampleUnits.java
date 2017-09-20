package uk.gov.ons.ctp.response.collection.exercise.validation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitService;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
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
  private static final String VALIDATION_LIST_ID = "group";
  // this is a bit of a kludge - jpa does not like having an IN clause with an
  // empty list
  // it does not return results when you expect it to - so ... always have this
  // in the list of excluded case ids
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private ExerciseSampleUnitService sampleUnitSvc;

  @Autowired
  private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

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
  private PartySvcClient partySvcClient;

  @Autowired
  @Qualifier("validation")
  private DistributedListManager<Integer> sampleValidationListManager;

  /**
   * Validate SampleUnits
   *
   */
  public void validateSampleUnits() {

    List<CollectionExercise> exercises = collectRepo.findByState(
        CollectionExerciseDTO.CollectionExerciseState.EXECUTED);

    if (!exercises.isEmpty()) {

      try {

        List<ExerciseSampleUnitGroup> sampleUnitGroups = retrieveSampleUnitGroups(exercises);

        // Not searching DB for individual Collection Exercise above when
        // getting batch of SampleUnitGroups to process but processing in
        // Collection Exercise order will save external service calls so sorting
        // them now.
        Map<CollectionExercise, List<ExerciseSampleUnitGroup>> collections = sampleUnitGroups.stream()
            .collect(Collectors.groupingBy(ExerciseSampleUnitGroup::getCollectionExercise));

        collections.forEach((exercise, groups) -> {
          if (!validateSampleUnits(exercise, groups)) {
            log.error("Exited without validating Collection Exercise: {}, Survey: {}", exercise.getId(),
                exercise.getSurvey().getId());
            return; // Exit collection forEach for exercise as no
                    // classifierTypes, fatal error.
          }

          collectRepo.saveAndFlush(collectionExerciseTransitionState(exercise));
        }); // End looping collections

      } catch (LockingException ex) {
        log.error("Validation failed due to {}", ex.getMessage());
      } finally {
        try {
          sampleValidationListManager.deleteList(VALIDATION_LIST_ID, true);
        } catch (LockingException ex) {
          log.error("Failed to release sampleValidationListManager data - error msg is {}", ex.getMessage());
        }
      }
    } // End exercises not empty. Just check this to save processing if going
      // to get empty sampleUnitGroups List to process
  }

  /**
   * Validate the SampleUnitGroups for a CollectionExercise.
   *
   * @param exercise for which to validate the SampleUnitGroups
   * @param sampleUnitGroups in exercise.
   * @return boolean false if fatal error validating, for example no
   *         classifierTypes
   */
  private boolean validateSampleUnits(CollectionExercise exercise,
      List<ExerciseSampleUnitGroup> sampleUnitGroups) {

    List<String> classifierTypes = requestSurveyClassifiers(exercise);
    if (classifierTypes.isEmpty()) {
      return false;
    }

    String surveyId = exercise.getSurvey().getId().toString();
    List<ExerciseSampleUnit> updatedSampleUnitsForGroup = new ArrayList<>();

    for (ExerciseSampleUnitGroup sampleUnitGroup : sampleUnitGroups) {

      List<ExerciseSampleUnit> sampleUnits = sampleUnitSvc.findBySampleUnitGroup(sampleUnitGroup);
      for (ExerciseSampleUnit sampleUnitParent : sampleUnits) {
        if (!sampleUnitParent.getSampleUnitType().isParent()) {
          log.warn("Validation for SampleUnit PK: {} Is type {}", sampleUnitParent.getSampleUnitPK(),
              sampleUnitParent.getSampleUnitType());
          // Skip current sampleUnit as not parent type. Respondent Unit.
          // Something must have gone wrong before?
          continue;
        }
        try {
          UUID collectionInstrumentId = requestCollectionInstrumentId(classifierTypes, sampleUnitParent);
          updatedSampleUnitsForGroup = requestPartyDetails(sampleUnitParent, sampleUnits, sampleUnitGroup,
              surveyId);
          updatedSampleUnitsForGroup.forEach(updatedSampleUnit -> {
            updatedSampleUnit.setCollectionInstrumentId(collectionInstrumentId);
          });
        } catch (RestClientException ex) {
          log.error("Error in validation for SampleUnitGroup PK: {} due to: {}", sampleUnitGroup.getSampleUnitGroupPK(),
              ex.getMessage());
        }
      }
      sampleUnitGroup = sampleUnitGroupTransitionState(sampleUnitGroup, updatedSampleUnitsForGroup);
      sampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
      // Update sampleUnits and group in transaction to ensure no inconsistent
      // state arises between them.
      sampleUnitGroupSvc.storeExerciseSampleUnitGroup(sampleUnitGroup, updatedSampleUnitsForGroup);
    } // End looping group

    return true;
  }

  /**
   * Retrieve SampleUnitGroups to be validated - state INIT - but do not
   * retrieve the same SampleUnitGroups as other service instances.
   *
   * @param exercises for which to return sampleUnitGroups.
   * @return list of SampleUnitGroups.
   * @throws LockingException problem obtaining lock for data shared across
   *           instances.
   */
  private List<ExerciseSampleUnitGroup> retrieveSampleUnitGroups(List<CollectionExercise> exercises)
      throws LockingException {

    List<ExerciseSampleUnitGroup> sampleUnitGroups;

    List<Integer> excludedGroups = sampleValidationListManager.findList(VALIDATION_LIST_ID, false);
    log.debug("VALIDATION - Retrieve sampleUnitGroups excluding {}", excludedGroups);

    excludedGroups.add(Integer.valueOf(IMPOSSIBLE_ID));
    sampleUnitGroups = sampleUnitGroupSvc
        .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
            SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            exercises,
            excludedGroups,
            new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax()));

    if (!CollectionUtils.isEmpty(sampleUnitGroups)) {
      log.debug("VALIDATION retrieved sampleUnitGroup PKs {}",
          sampleUnitGroups.stream().map(group -> group.getSampleUnitGroupPK().toString()).collect(
              Collectors.joining(",")));
      sampleValidationListManager.saveList(VALIDATION_LIST_ID,
          sampleUnitGroups.stream().map(group -> group.getSampleUnitGroupPK()).collect(Collectors.toList()), true);
    } else {
      log.debug("VALIDATION retrieved 0 sampleUnitGroup PKs");
      sampleValidationListManager.unlockContainer();
    }
    return sampleUnitGroups;
  }

  /**
   * Request party information from the Party Service. Update reporting unit
   * with partyId. Create a SampleUnit for each enrolled respondent for the
   * reporting unit for that survey. Operation is designed to be repeatable by
   * checking if sampleUnit already exists for partyId.
   *
   * @param sampleUnit sampled reporting unit for which to request party
   *          information.
   * @param sampleUnits all sampleUnits belonging to this group.
   * @param sampleUnitGroup group to which sampleUnit belongs.
   * @param surveyId Survey of which sampleUnit is a member.
   * @return List<ExerciseSampleUnit> of updated, created sampleUnits
   * @throws RestClientException something went wrong making http call.
   */
  private List<ExerciseSampleUnit> requestPartyDetails(ExerciseSampleUnit sampleUnit,
      List<ExerciseSampleUnit> sampleUnits, ExerciseSampleUnitGroup sampleUnitGroup, String surveyId)
      throws RestClientException {
    List<ExerciseSampleUnit> updatedSampleUnits = new ArrayList<ExerciseSampleUnit>();
    PartyDTO party = partySvcClient.requestParty(sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
    sampleUnit.setPartyId(UUID.fromString(party.getId()));
    updatedSampleUnits.add(sampleUnit);
    party.getAssociations().forEach(association -> {
      association.getEnrolments().forEach(enrolment -> {
        if (enrolment.getSurveyId().equals(surveyId)) {
          // Make sure respondent unit doesn't already exist.
          Optional<ExerciseSampleUnit> match = (sampleUnits.stream().filter(
              existingSampleUnit -> association.getPartyId().equals(existingSampleUnit.getPartyId().toString()))
              .findFirst());
          if (match.isPresent()) {
            log.warn("Validation for SampleUnit PK: {} Respondent already exists {}", sampleUnit.getSampleUnitPK(),
                association.getPartyId());
            updatedSampleUnits.add(match.get());
          } else {
            // Doesn't already exist, create new sampleUnit for respondent.
            ExerciseSampleUnit respondent = new ExerciseSampleUnit();
            respondent.setSampleUnitGroup(sampleUnitGroup);
            respondent.setPartyId(UUID.fromString(association.getPartyId()));
            respondent.setSampleUnitRef(sampleUnit.getSampleUnitRef());
            respondent.setSampleUnitType(sampleUnit.getSampleUnitType().getChild());
            updatedSampleUnits.add(respondent);
          }
        }
      });
    });
    return updatedSampleUnits;

  }

  /**
   * Request the Collection Instrument details from the Collection Instrument
   * Service using the given classifiers and return the instrument Id.
   *
   * @param classifierTypes used in search by Collection Instrument service to
   *          return instrument details matching classifiers.
   * @param sampleUnit to which the collection instrument relates.
   * @return UUID of collection instrument.
   * @throws RestClientException something went wrong making http call
   */
  private UUID requestCollectionInstrumentId(List<String> classifierTypes, ExerciseSampleUnit sampleUnit)
      throws RestClientException {

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
    if (requestCollectionInstruments.isEmpty()) {
      log.error("No collection instruments found for: {}", searchString);
    } else if (requestCollectionInstruments.size() > 1) {
      log.warn("{} collection instruments found for: {}, taking first", requestCollectionInstruments.size(),
          searchString);
      collectionInstrumentId = requestCollectionInstruments.get(0).getId();
    } else {
      collectionInstrumentId = requestCollectionInstruments.get(0).getId();
    }

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
    try {
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
    } catch (RestClientException ex) {
      log.error("Error requesting Survey service for classifierTypes: {}", ex.getMessage());
    }

    return classifierTypes;

  }

  /**
   * Transition Collection Exercise state for validation.
   *
   * @param exercise to transition.
   * @return exercise Collection Exercise with new state.
   */
  private CollectionExercise collectionExerciseTransitionState(CollectionExercise exercise) {

    long init = sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
        SampleUnitGroupDTO.SampleUnitGroupState.INIT, exercise);
    long validated = sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
        SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED, exercise);
    long failed = sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
        SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION, exercise);

    try {
      if (validated == exercise.getSampleSize().longValue()) {
        // All sample units validated, set exercise state to VALIDATED
        exercise.setState(collectionExerciseTransitionState.transition(exercise.getState(),
            CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE));
      } else if (init < 1 && failed > 0) {
        // None left to validate but some failed, set exercise to
        // FAILEDVALIDATION
        exercise.setState(collectionExerciseTransitionState.transition(exercise.getState(),
            CollectionExerciseDTO.CollectionExerciseEvent.INVALIDATE));
      }
    } catch (CTPException ex) {
      log.error("Collection Exercise state transition failed: {}", ex.getMessage());
    }
    return exercise;
  }

  /**
   * Transition Sample Unit Group state for validation.
   *
   * @param sampleUnitGroup to be transitioned.
   * @param sampleUnits in SampleUnitGroup.
   * @return sampleUnitGroup with new state.
   */
  private ExerciseSampleUnitGroup sampleUnitGroupTransitionState(ExerciseSampleUnitGroup sampleUnitGroup,
      List<ExerciseSampleUnit> sampleUnits) {

    Predicate<ExerciseSampleUnit> stateTest = su -> su.getPartyId() instanceof UUID
        && su.getCollectionInstrumentId() instanceof UUID;
    boolean stateValidated = false;
    if (sampleUnits.isEmpty()) {
      stateValidated = false;
    } else {
      stateValidated = sampleUnits.stream().allMatch(stateTest);
    }
    try {
      if (stateValidated) {
        sampleUnitGroup.setStateFK(
            sampleUnitGroupState.transition(sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.VALIDATE));
      } else {
        sampleUnitGroup.setStateFK(
            sampleUnitGroupState.transition(sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.INVALIDATE));
      }
    } catch (CTPException ex) {
      log.error("Sample Unit group state transition failed: {}", ex.getMessage());
    }
    return sampleUnitGroup;
  }

}
