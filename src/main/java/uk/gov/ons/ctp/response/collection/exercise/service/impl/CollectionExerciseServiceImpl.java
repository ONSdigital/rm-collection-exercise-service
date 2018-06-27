package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO.SampleLinkState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** The implementation of the SampleService */
@Service
@Slf4j
public class CollectionExerciseServiceImpl implements CollectionExerciseService {

  private CaseTypeDefaultRepository caseTypeDefaultRepo;

  private CaseTypeOverrideRepository caseTypeOverrideRepo;

  private CollectionExerciseRepository collectRepo;

  private ActionSvcClient actionSvcClient;

  private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  private SampleLinkRepository sampleLinkRepository;

  private SurveyService surveyService;

  private StateTransitionManager<
          CollectionExerciseDTO.CollectionExerciseState,
          CollectionExerciseDTO.CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @Autowired
  public CollectionExerciseServiceImpl(
      CaseTypeDefaultRepository caseTypeDefaultRepo,
      CaseTypeOverrideRepository caseTypeOverrideRepo,
      CollectionExerciseRepository collectRepo,
      SampleLinkRepository sampleLinkRepository,
      ActionSvcClient actionSvcClient,
      CollectionInstrumentSvcClient collectionInstrumentSvcClient,
      SurveyService surveyService,
      @Qualifier("collectionExercise")
          StateTransitionManager<
                  CollectionExerciseDTO.CollectionExerciseState,
                  CollectionExerciseDTO.CollectionExerciseEvent>
              collectionExerciseTransitionState) {
    this.caseTypeOverrideRepo = caseTypeOverrideRepo;
    this.caseTypeDefaultRepo = caseTypeDefaultRepo;
    this.collectRepo = collectRepo;
    this.sampleLinkRepository = sampleLinkRepository;
    this.actionSvcClient = actionSvcClient;
    this.collectionInstrumentSvcClient = collectionInstrumentSvcClient;
    this.surveyService = surveyService;
    this.collectionExerciseTransitionState = collectionExerciseTransitionState;
  }

  @Override
  public List<CollectionExercise> findCollectionExercisesForSurvey(SurveyDTO survey) {
    return this.collectRepo.findBySurveyId(UUID.fromString(survey.getId()));
  }

  @Override
  public List<CollectionExercise> findCollectionExercisesForParty(final UUID id) {
    return this.collectRepo.findByPartyId(id);
  }

  @Override
  public List<SampleLink> findLinkedSampleSummaries(UUID id) {
    return sampleLinkRepository.findByCollectionExerciseId(id);
  }

  @Override
  public List<CollectionExercise> findAllCollectionExercise() {
    return collectRepo.findAll();
  }

  @Override
  public CollectionExercise findCollectionExercise(UUID id) {

    return collectRepo.findOneById(id);
  }

  @Override
  public CollectionExercise findCollectionExercise(String surveyRef, String exerciseRef) {
    CollectionExercise collex = null;
    SurveyDTO survey = this.surveyService.findSurveyByRef(surveyRef);

    if (survey != null) {
      collex = findCollectionExercise(exerciseRef, survey);
    }

    return collex;
  }

  @Override
  public CollectionExercise findCollectionExercise(String exerciseRef, SurveyDTO survey) {
    List<CollectionExercise> existing =
        this.collectRepo.findByExerciseRefAndSurveyId(exerciseRef, UUID.fromString(survey.getId()));

    switch (existing.size()) {
      case 0:
        return null;
      default:
        return existing.get(0);
    }
  }

  @Override
  public CollectionExercise findCollectionExercise(String exerciseRef, UUID surveyId) {
    List<CollectionExercise> existing =
        this.collectRepo.findByExerciseRefAndSurveyId(exerciseRef, surveyId);

    switch (existing.size()) {
      case 0:
        return null;
      default:
        return existing.get(0);
    }
  }

  @Override
  public Collection<CaseType> getCaseTypesList(CollectionExercise collectionExercise) {

    List<CaseTypeDefault> caseTypeDefaultList =
        caseTypeDefaultRepo.findBySurveyId(collectionExercise.getSurveyId());

    List<CaseTypeOverride> caseTypeOverrideList =
        caseTypeOverrideRepo.findByExerciseFK(collectionExercise.getExercisePK());

    return createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);
  }

  /**
   * Creates a Collection of CaseTypes
   *
   * @param caseTypeDefaultList List of caseTypeDefaults
   * @param caseTypeOverrideList List of caseTypeOverrides
   * @return Collection<CaseType> Collection of CaseTypes
   */
  public Collection<CaseType> createCaseTypeList(
      List<? extends CaseType> caseTypeDefaultList, List<? extends CaseType> caseTypeOverrideList) {

    Map<String, CaseType> defaultMap = new HashMap<>();

    for (CaseType caseTypeDefault : caseTypeDefaultList) {
      defaultMap.put(caseTypeDefault.getSampleUnitTypeFK(), caseTypeDefault);
    }

    for (CaseType caseTypeOverride : caseTypeOverrideList) {
      defaultMap.put(caseTypeOverride.getSampleUnitTypeFK(), caseTypeOverride);
    }

    return defaultMap.values();
  }

  /**
   * Delete existing SampleSummary links for input CollectionExercise then link all SampleSummaries
   * in list to CollectionExercise
   *
   * @param collectionExerciseId the Id of the CollectionExercise to link to
   * @param sampleSummaryIds the list of Ids of the SampleSummaries to be linked
   * @return linkedSummaries the list of CollectionExercises and the linked SampleSummaries
   */
  @Transactional
  @Override
  public List<SampleLink> linkSampleSummaryToCollectionExercise(
      UUID collectionExerciseId, List<UUID> sampleSummaryIds) {
    sampleLinkRepository.deleteByCollectionExerciseId(collectionExerciseId);
    List<SampleLink> linkedSummaries = new ArrayList<>();
    for (UUID summaryId : sampleSummaryIds) {
      linkedSummaries.add(createLink(summaryId, collectionExerciseId));
    }

    // This used to transition the collection exercise to ready for review, but now that only
    // happens if
    // the sample link is ACTIVE

    return linkedSummaries;
  }

  /**
   * Delete SampleSummary link
   *
   * @param sampleSummaryId a sample summary uuid
   * @param collectionExerciseId a collection exercise uuid
   * @throws CTPException thrown if transition fails
   */
  @Override
  @Transactional
  public void removeSampleSummaryLink(final UUID sampleSummaryId, final UUID collectionExerciseId)
      throws CTPException {
    sampleLinkRepository.deleteBySampleSummaryIdAndCollectionExerciseId(
        sampleSummaryId, collectionExerciseId);

    List<SampleLink> sampleLinks =
        this.sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId);

    if (sampleLinks.size() == 0) {
      transitionCollectionExercise(
          collectionExerciseId, CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_DELETED);
    }
  }

  /**
   * Sets the values in a supplied collection exercise from a supplied DTO. WARNING: Mutates
   * collection exercise
   *
   * @param collex the dto containing the data
   * @param collectionExercise the collection exercise to apply the value from the dto to
   */
  private void setCollectionExerciseFromDto(
      CollectionExerciseDTO collex, CollectionExercise collectionExercise) {
    collectionExercise.setName(collex.getName());
    collectionExercise.setUserDescription(collex.getUserDescription());
    collectionExercise.setExerciseRef(collex.getExerciseRef());
    collectionExercise.setSurveyId(UUID.fromString(collex.getSurveyId()));

    // In the strictest sense, some of these dates are mandatory fields for collection exercises.
    // However as they
    // are not supplied at creation time, but later as "events" we will allow them to be null
    if (collex.getScheduledStartDateTime() != null) {
      collectionExercise.setScheduledStartDateTime(
          new Timestamp(collex.getScheduledStartDateTime().getTime()));
    }
    if (collex.getScheduledEndDateTime() != null) {
      collectionExercise.setScheduledEndDateTime(
          new Timestamp(collex.getScheduledEndDateTime().getTime()));
    }
    if (collex.getScheduledExecutionDateTime() != null) {
      collectionExercise.setScheduledExecutionDateTime(
          new Timestamp(collex.getScheduledExecutionDateTime().getTime()));
    }
    if (collex.getActualExecutionDateTime() != null) {
      collectionExercise.setActualExecutionDateTime(
          new Timestamp(collex.getActualExecutionDateTime().getTime()));
    }
    if (collex.getActualPublishDateTime() != null) {
      collectionExercise.setActualPublishDateTime(
          new Timestamp(collex.getActualPublishDateTime().getTime()));
    }
  }

  /**
   * Create collection exercise This will also create the required action plans and casetypeoverride
   *
   * @param collex the data to create the collection exercise from
   * @param survey representation of the survey for the given collection exercise
   * @return created collection exercise
   */
  @Transactional
  @Override
  public CollectionExercise createCollectionExercise(
      CollectionExerciseDTO collex, SurveyDTO survey) {
    log.debug(
        "Creating collection exercise, ExerciseRef: {}, SurveyRef: {}",
        collex.getExerciseRef(),
        survey.getSurveyRef());
    CollectionExercise collectionExercise = newCollectionExerciseFromDTO(collex);
    // Save collection exercise before creating action plans because we need the exercisepk
    collectionExercise = this.collectRepo.saveAndFlush(collectionExercise);
    createActionPlans(collectionExercise, survey);
    log.debug(
        "Successfully created collection exercise, CollectionExerciseId: {}",
        collectionExercise.getId());
    return collectionExercise;
  }

  /**
   * Create and populate details of collection exercise.
   *
   * @param collex collection exercise
   * @return collection exercise with details
   */
  private CollectionExercise newCollectionExerciseFromDTO(CollectionExerciseDTO collex) {
    log.debug("Create new collection exercise from DTO");
    CollectionExercise collectionExercise = new CollectionExercise();
    setCollectionExerciseFromDto(collex, collectionExercise);
    collectionExercise.setState(CollectionExerciseDTO.CollectionExerciseState.CREATED);
    collectionExercise.setCreated(new Timestamp(new Date().getTime()));
    collectionExercise.setId(UUID.randomUUID());
    log.debug(
        "Successfully created collection exercise from DTO, CollectionExerciseId: {}",
        collectionExercise.getId());
    return collectionExercise;
  }

  /**
   * Create required action plans
   *
   * @param collectionExercise Collection Exercise
   * @param survey SurveyDTO representing survey of collection exercise
   */
  private void createActionPlans(CollectionExercise collectionExercise, SurveyDTO survey) {
    log.debug(
        "Creating action plans for exercise, CollectionExerciseId: {}, SurveyId: {}",
        collectionExercise.getId(),
        survey.getId());

    switch (survey.getSurveyType()) {
      case Business:
        createDefaultActionPlan(survey, "B");
        createDefaultActionPlan(survey, "BI");
        createOverrideActionPlan(collectionExercise, survey, "B");
        createOverrideActionPlan(collectionExercise, survey, "BI");
        break;

      case Social:
        createDefaultActionPlan(survey, "H");
        createOverrideActionPlan(collectionExercise, survey, "H");
        break;

      case Census:
      default:
        throw new RuntimeException("Census surveys not supported... yet!");
    }

    log.debug(
        "Successfully created action plans for exercise, CollectionExerciseId: {}, SurveyID: {}",
        collectionExercise.getId(),
        survey.getId());
  }

  /**
   * Create default action plan for collection exercise and case type if not already exists
   *
   * @param survey DTO Representation of survey
   * @param sampleUnitType Sample Unit Type i.e. (B, H, HI)
   */
  private void createDefaultActionPlan(SurveyDTO survey, String sampleUnitType) {
    log.debug(
        "Creating default action plan, SurveyId: {} , SampleUnitType: {}",
        survey.getId(),
        sampleUnitType);

    // If a casetypedefault already exists for this survey/sampleUnitType do nothing
    CaseTypeDefault existingCaseTypeDefault =
        caseTypeDefaultRepo.findTopBySurveyIdAndSampleUnitTypeFK(
            UUID.fromString(survey.getId()), sampleUnitType);
    if (existingCaseTypeDefault != null) {
      log.debug(
          "Default action plan already exists, SurveyId: {} SampleUnitType: {}",
          survey.getId(),
          sampleUnitType);
      return;
    }

    // Create new action plan and associated casetypedefault
    String shortName = survey.getShortName();
    String name = String.format("%s %s", shortName, sampleUnitType);
    String description = String.format("%s %s Case", shortName, sampleUnitType);
    ActionPlanDTO actionPlan = actionSvcClient.createActionPlan(name, description);
    createCaseTypeDefault(survey, sampleUnitType, actionPlan);
    log.debug(
        "Successfully created default action plan,"
            + "ActionPlanId: {}, SurveyId: {}, SampleUnitType: {}",
        actionPlan.getId(),
        survey.getId(),
        sampleUnitType);
  }

  /**
   * Create case type default for action plan, survey and sample unit type if not already exists
   *
   * @param survey DTO Representation of survey
   * @param sampleUnitType Sample Unit Type
   * @param actionPlan DTO Representation of action plan
   */
  private void createCaseTypeDefault(
      SurveyDTO survey, String sampleUnitType, ActionPlanDTO actionPlan) {
    log.debug(
        "Creating case type default, ActionPlanId: {}, SurveyId: {}, SampleUnitType: {}",
        actionPlan.getId(),
        survey.getId(),
        sampleUnitType);
    CaseTypeDefault caseTypeDefault = new CaseTypeDefault();
    caseTypeDefault.setSurveyId(UUID.fromString(survey.getId()));
    caseTypeDefault.setSampleUnitTypeFK(sampleUnitType);
    caseTypeDefault.setActionPlanId(actionPlan.getId());

    this.caseTypeDefaultRepo.saveAndFlush(caseTypeDefault);
    log.debug(
        "Successfully created case type default,"
            + "ActionPlanId: {}, SurveyId: {}, SampleUnitType: {}",
        actionPlan.getId(),
        survey.getId(),
        sampleUnitType);
  }

  /**
   * Create action plan for given collection exercise and case type if not already exists
   *
   * @param survey DTO Representation of survey for collection exercise
   * @param collectionExercise Collection Exercise
   * @param sampleUnitType Sample Unit Type i.e. (B, H, HI)
   */
  private void createOverrideActionPlan(
      CollectionExercise collectionExercise, SurveyDTO survey, String sampleUnitType) {
    log.debug(
        "Creating override action plan, CollectionExerciseId: {}, SampleUnitType: {}",
        collectionExercise.getId(),
        sampleUnitType);

    // If a casetypeoverride already exists for this exercise/sampleUnitType do nothing
    CaseTypeOverride existingCaseTypeOverride =
        caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(
            collectionExercise.getExercisePK(), sampleUnitType);
    if (existingCaseTypeOverride != null) {
      log.debug(
          "Override action plan already exists, CollectionExerciseId: {}, SampleUnitType: {}",
          collectionExercise.getId(),
          sampleUnitType);
      return;
    }

    // Create action plan with appropriate name and description
    String exerciseRef = collectionExercise.getExerciseRef();
    String shortName = survey.getShortName();
    String name = String.format("%s %s %s", shortName, sampleUnitType, exerciseRef);
    String description = String.format("%s %s Case %s", shortName, sampleUnitType, exerciseRef);
    ActionPlanDTO actionPlan = actionSvcClient.createActionPlan(name, description);

    // Create casetypeoverride linking collection exercise and sample unit type to the action plan
    createCaseTypeOverride(collectionExercise, sampleUnitType, actionPlan);
    log.debug(
        "Successfully created override action plan, "
            + "ActionPlanId: {}, CollectionExerciseId: {}, SampleUnitType: {}",
        actionPlan.getId(),
        collectionExercise.getId(),
        sampleUnitType);
  }

  /**
   * Create case type override for given action plan and collection exercise
   *
   * @param collectionExercise representation of collection exercise
   * @param sampleUnitType Sample unit type i.e. (B, H, HI)
   * @param actionPlan the newly created action plan
   * @throws DataAccessException if caseTypeOverride fails to save to database
   */
  private void createCaseTypeOverride(
      CollectionExercise collectionExercise, String sampleUnitType, ActionPlanDTO actionPlan)
      throws DataAccessException {
    log.debug(
        "Creating case type override,"
            + "ActionPlanId: {}, CollectionExerciseId: {}, SampleUnitType: {}",
        actionPlan.getId(),
        collectionExercise.getId(),
        sampleUnitType);
    CaseTypeOverride caseTypeOverride = new CaseTypeOverride();
    caseTypeOverride.setExerciseFK(collectionExercise.getExercisePK());
    caseTypeOverride.setSampleUnitTypeFK(sampleUnitType);
    caseTypeOverride.setActionPlanId(actionPlan.getId());
    this.caseTypeOverrideRepo.saveAndFlush(caseTypeOverride);
    log.debug(
        "Successfully created case type override, "
            + "CollectionExerciseId: {}, SampleUnitType: {}, ActionPlanId: {}",
        collectionExercise.getId(),
        sampleUnitType,
        actionPlan.getId());
  }

  @Override
  public CollectionExercise patchCollectionExercise(UUID id, CollectionExerciseDTO patchData)
      throws CTPException {
    CollectionExercise collex = findCollectionExercise(id);

    if (collex == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Collection exercise %s not found", id));
    } else {
      String proposedPeriod =
          patchData.getExerciseRef() == null ? collex.getExerciseRef() : patchData.getExerciseRef();
      UUID proposedSurvey =
          patchData.getSurveyId() == null
              ? collex.getSurveyId()
              : UUID.fromString(patchData.getSurveyId());

      // If period/survey not supplied in patchData then this call will trivially return
      validateUniqueness(collex, proposedPeriod, proposedSurvey);

      if (!StringUtils.isBlank(patchData.getSurveyId())) {
        UUID surveyId = UUID.fromString(patchData.getSurveyId());

        SurveyDTO survey = this.surveyService.findSurvey(surveyId);

        if (survey == null) {
          throw new CTPException(
              CTPException.Fault.BAD_REQUEST, String.format("Survey %s does not exist", surveyId));
        } else {
          collex.setSurveyId(surveyId);
        }
      }

      if (!StringUtils.isBlank(patchData.getExerciseRef())) {
        collex.setExerciseRef(patchData.getExerciseRef());
      }
      if (!StringUtils.isBlank(patchData.getName())) {
        collex.setName(patchData.getName());
      }
      if (!StringUtils.isBlank(patchData.getUserDescription())) {
        collex.setUserDescription(patchData.getUserDescription());
      }
      if (patchData.getScheduledStartDateTime() != null) {
        collex.setScheduledStartDateTime(
            new Timestamp(patchData.getScheduledStartDateTime().getTime()));
      }

      collex.setUpdated(new Timestamp(new Date().getTime()));

      return updateCollectionExercise(collex);
    }
  }

  /**
   * This method checks whether the supplied CollectionExercise (existing) can change it's period to
   * candidatePeriod and it's survey to candidateSurvey without breaching the uniqueness constraint
   * on those fields
   *
   * @param existing the collection exercise that is to be updated
   * @param candidatePeriod the proposed new value for the period (exerciseRef)
   * @param candidateSurvey the proposed new value for the survey
   * @throws CTPException thrown if there is an existing different collection exercise that already
   *     uses the proposed combination of period and survey
   */
  private void validateUniqueness(
      CollectionExercise existing, String candidatePeriod, UUID candidateSurvey)
      throws CTPException {
    if (!existing.getSurveyId().equals(candidateSurvey)
        || !existing.getExerciseRef().equals(candidatePeriod)) {
      CollectionExercise otherExisting = findCollectionExercise(candidatePeriod, candidateSurvey);

      if (otherExisting != null && !otherExisting.getId().equals(existing.getId())) {
        throw new CTPException(
            CTPException.Fault.RESOURCE_VERSION_CONFLICT,
            String.format(
                "A collection exercise with period %s and id %s already exists.",
                candidatePeriod, candidateSurvey));
      }
    }
  }

  @Override
  public CollectionExercise updateCollectionExercise(UUID id, CollectionExerciseDTO collexDto)
      throws CTPException {
    CollectionExercise existing = findCollectionExercise(id);

    if (existing == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Collection exercise with id %s does not exist", id));
    } else {
      UUID surveyUuid = UUID.fromString(collexDto.getSurveyId());
      String period = collexDto.getExerciseRef();

      // This will throw exception if period & surveyId are not unique
      validateUniqueness(existing, period, surveyUuid);

      SurveyDTO survey = this.surveyService.findSurvey(surveyUuid);

      if (survey == null) {
        throw new CTPException(
            CTPException.Fault.BAD_REQUEST, String.format("Survey %s does not exist", surveyUuid));
      } else {
        setCollectionExerciseFromDto(collexDto, existing);
        existing.setUpdated(new Timestamp(new Date().getTime()));

        return updateCollectionExercise(existing);
      }
    }
  }

  @Override
  public CollectionExercise updateCollectionExercise(final CollectionExercise collex) {
    collex.setUpdated(new Timestamp(new Date().getTime()));
    return this.collectRepo.saveAndFlush(collex);
  }

  /**
   * Utility method to set the deleted flag for a collection exercise
   *
   * @param id the uuid of the collection exercise to update
   * @param deleted true if the collection exercise is to be marked as deleted, false otherwise
   * @return 200 if success, 404 if not found
   * @throws CTPException thrown if specified collection exercise does not exist
   */
  private CollectionExercise updateCollectionExerciseDeleted(UUID id, boolean deleted)
      throws CTPException {
    CollectionExercise collex = findCollectionExercise(id);

    if (collex == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Collection exercise %s does not exists", id));
    } else {
      collex.setDeleted(deleted);

      return updateCollectionExercise(collex);
    }
  }

  @Override
  public CollectionExercise deleteCollectionExercise(UUID id) throws CTPException {
    return updateCollectionExerciseDeleted(id, true);
  }

  @Override
  public CollectionExercise undeleteCollectionExercise(UUID id) throws CTPException {
    return updateCollectionExerciseDeleted(id, false);
  }

  @Override
  public List<CollectionExercise> findByState(CollectionExerciseDTO.CollectionExerciseState state) {
    return collectRepo.findByState(state);
  }

  @Override
  public void transitionCollectionExercise(
      CollectionExercise collex, CollectionExerciseDTO.CollectionExerciseEvent event)
      throws CTPException {
    CollectionExerciseDTO.CollectionExerciseState oldState = collex.getState();
    CollectionExerciseDTO.CollectionExerciseState newState =
        collectionExerciseTransitionState.transition(collex.getState(), event);
    if (oldState != newState) {
      collex.setState(newState);
      updateCollectionExercise(collex);
    }
  }

  @Override
  public void transitionCollectionExercise(
      final UUID collectionExerciseId, final CollectionExerciseDTO.CollectionExerciseEvent event)
      throws CTPException {
    CollectionExercise collex = findCollectionExercise(collectionExerciseId);

    if (collex == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Cannot find collection exercise %s", collectionExerciseId));
    }

    transitionCollectionExercise(collex, event);
  }

  @Override
  public void transitionScheduleCollectionExerciseToReadyToReview(
      final CollectionExercise collectionExercise) throws CTPException {
    UUID collexId = collectionExercise.getId();

    Map<String, String> searchStringMap =
        Collections.singletonMap("COLLECTION_EXERCISE", collectionExercise.getId().toString());
    String searchStringJson = new JSONObject(searchStringMap).toString();
    Integer numberOfCollectionInstruments =
        collectionInstrumentSvcClient.countCollectionInstruments(searchStringJson);
    boolean sampleLinksValid = validateSampleLinks(collexId);
    boolean shouldTransition =
        sampleLinksValid
            && numberOfCollectionInstruments != null
            && numberOfCollectionInstruments > 0;
    log.info(
        "ready_for_review transition check:"
            + "sampleLinksValid: {}, numberOfCollectionInstruments: {},"
            + " shouldTransition: {}",
        sampleLinksValid,
        numberOfCollectionInstruments,
        shouldTransition);
    if (shouldTransition) {
      transitionCollectionExercise(
          collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_ADDED);
    } else {
      transitionCollectionExercise(
          collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_DELETED);
    }
  }

  @Override
  public void transitionScheduleCollectionExerciseToReadyToReview(final UUID collectionExerciseId)
      throws CTPException {
    CollectionExercise collex = findCollectionExercise(collectionExerciseId);

    if (collex != null) {
      transitionScheduleCollectionExerciseToReadyToReview(collex);
    }
  }

  /**
   * Method to validate the sample links for a collection exercise by ensuring that all associated
   * SampleLinks are in the ACTIVE state
   *
   * @param collexId the collection exercise to validate
   * @return true if the associated sample links are valid, false otherwise
   */
  private boolean validateSampleLinks(final UUID collexId) {
    List<SampleLink> sampleLinks = this.sampleLinkRepository.findByCollectionExerciseId(collexId);
    List<SampleLink> nonActiveSampleLinks =
        sampleLinks
            .stream()
            .filter(sl -> SampleLinkState.ACTIVE != sl.getState())
            .collect(Collectors.toList());

    return sampleLinks.size() > 0 && nonActiveSampleLinks.size() == 0;
  }

  /**
   * Links a sample summary to a collection exercise and stores in db
   *
   * @param sampleSummaryId the Id of the Sample summary to be linked
   * @param collectionExerciseId the Id of the Sample summary to be linked
   * @return sampleLink stored in database
   */
  SampleLink createLink(final UUID sampleSummaryId, final UUID collectionExerciseId) {
    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    sampleLink.setCollectionExerciseId(collectionExerciseId);
    sampleLink.setState(SampleLinkState.INIT);
    return sampleLinkRepository.saveAndFlush(sampleLink);
  }
}
