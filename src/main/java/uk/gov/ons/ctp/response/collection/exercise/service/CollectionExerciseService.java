package uk.gov.ons.ctp.response.collection.exercise.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;

/** The implementation of the SampleService */
@Service
public class CollectionExerciseService {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseService.class);

  private final CollectionExerciseRepository collectRepo;

  private final ActionSvcClient actionSvcClient;

  private final CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  private final SampleLinkRepository sampleLinkRepository;

  private final SampleSvcClient sampleSvcClient;

  private final SurveySvcClient surveyService;

  private final RabbitTemplate rabbitTemplate;

  private final StateTransitionManager<
          CollectionExerciseDTO.CollectionExerciseState,
          CollectionExerciseDTO.CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @Autowired
  public CollectionExerciseService(
      CollectionExerciseRepository collectRepo,
      SampleLinkRepository sampleLinkRepository,
      ActionSvcClient actionSvcClient,
      CollectionInstrumentSvcClient collectionInstrumentSvcClient,
      SampleSvcClient sampleSvcClient,
      SurveySvcClient surveyService,
      @Qualifier("collexTransitionTemplate") RabbitTemplate rabbitTemplate,
      @Qualifier("collectionExercise")
          StateTransitionManager<
                  CollectionExerciseDTO.CollectionExerciseState,
                  CollectionExerciseDTO.CollectionExerciseEvent>
              collectionExerciseTransitionState) {
    this.collectRepo = collectRepo;
    this.sampleLinkRepository = sampleLinkRepository;
    this.actionSvcClient = actionSvcClient;
    this.collectionInstrumentSvcClient = collectionInstrumentSvcClient;
    this.surveyService = surveyService;
    this.sampleSvcClient = sampleSvcClient;
    this.rabbitTemplate = rabbitTemplate;
    this.collectionExerciseTransitionState = collectionExerciseTransitionState;
  }

  /**
   * Find a list of collection exercises associated to a survey from the Collection Exercise Service
   *
   * @param survey the survey for which to find collection exercises
   * @return the associated collection exercises.
   */
  public List<CollectionExercise> findCollectionExercisesForSurvey(SurveyDTO survey) {
    return this.collectRepo.findBySurveyId(UUID.fromString(survey.getId()));
  }

  /**
   * Find a list of collection exercises associated with a list of surveys from the Collection
   * Exercise Service
   *
   * @param surveyIds the survey UUIDS for which to find collection exercises
   * @return the associated collection exercises.
   */
  public HashMap<UUID, List<CollectionExercise>> findCollectionExercisesForSurveys(
      List<UUID> surveyIds) {

    List<CollectionExercise> collexList =
        this.collectRepo.findBySurveyIdInOrderBySurveyId(surveyIds);

    return this.collexListToMap(collexList);
  }

  /**
   * Walks a list of collection exercises and splits them into a HashMap with key of survey id and
   * value of list of collection exercises
   *
   * @param collexList list of collection exercises to split
   * @return the associated collection exercises.
   */
  private HashMap<UUID, List<CollectionExercise>> collexListToMap(
      List<CollectionExercise> collexList) {
    HashMap<UUID, List<CollectionExercise>> collexMap = new HashMap<>();

    for (CollectionExercise current : collexList) {
      UUID surveyId = current.getSurveyId();

      if (!collexMap.containsKey(surveyId)) {
        collexMap.put(surveyId, new ArrayList<CollectionExercise>());
      }
      collexMap.get(surveyId).add(current);
    }
    return collexMap;
  }

  /**
   * find a list of all sample summary linked to a collection exercise
   *
   * @param id the collection exercise Id to find the linked sample summaries for
   * @return list of linked sample summary
   */
  public List<SampleLink> findLinkedSampleSummaries(UUID id) {
    return sampleLinkRepository.findByCollectionExerciseId(id);
  }

  /**
   * Find all Collection Exercises
   *
   * @return a list of all Collection Exercises
   */
  public List<CollectionExercise> findAllCollectionExercise() {
    return collectRepo.findAll();
  }

  public List<CollectionExercise> findCollectionExercisesBySurveyIdAndState(
      UUID surveyId, CollectionExerciseState state) {
    return collectRepo.findBySurveyIdAndState(surveyId, state);
  }

  /**
   * Find a list of collection exercises associated with a list of surveys from the Collection
   * Exercise Service
   *
   * @param surveyIds the survey UUIDS for which to find collection exercises
   * @param state Only return collection exercises in this state
   * @return the associated collection exercises as a HashMap, key is survey id , value is List of
   *     collex
   */
  public HashMap<UUID, List<CollectionExercise>> findCollectionExercisesForSurveysByState(
      List<UUID> surveyIds, CollectionExerciseState state) {
    List<CollectionExercise> collexList =
        this.collectRepo.findBySurveyIdInAndStateOrderBySurveyId(surveyIds, state);

    return this.collexListToMap(collexList);
  }

  /**
   * Find a collection exercise associated to a collection exercise Id from the Collection Exercise
   * Service
   *
   * @param id the collection exercise Id for which to find collection exercise
   * @throws CTPException if collection exercise not found
   * @return the associated collection exercise.
   */
  public CollectionExercise findCollectionExercise(UUID id) {

    return collectRepo.findOneById(id);
  }

  /**
   * Find a collection exercise from a survey ref (e.g. 221) and a collection exercise ref (e.g.
   * 201808)
   *
   * @param surveyRef the survey ref
   * @param exerciseRef the collection exercise ref
   * @return the specified collection exercise or null if not found
   */
  public CollectionExercise findCollectionExercise(String surveyRef, String exerciseRef) {
    CollectionExercise collex = null;
    SurveyDTO survey = this.surveyService.findSurveyByRef(surveyRef);

    if (survey != null) {
      collex = findCollectionExercise(exerciseRef, survey);
    }

    return collex;
  }

  /**
   * Gets collection exercise with given exerciseRef and survey (should be no more than 1)
   *
   * @param exerciseRef the exerciseRef (period) of the collection exercise
   * @param survey the survey the collection exercise is associated with
   * @return the collection exercise if it exists, null otherwise
   */
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

  /**
   * Gets collection exercise with given exerciseRef and survey uuid (should be no more than 1)
   *
   * @param exerciseRef the exerciseRef (period) of the collection exercise
   * @param surveyId the uuid of the survey the collection exercise is associated with
   * @return the collection exercise if it exists, null otherwise
   */
  public CollectionExercise findCollectionExercise(final String exerciseRef, final UUID surveyId) {
    List<CollectionExercise> existing =
        this.collectRepo.findByExerciseRefAndSurveyId(exerciseRef, surveyId);

    switch (existing.size()) {
      case 0:
        return null;
      default:
        return existing.get(0);
    }
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
  public List<SampleLink> linkSampleSummaryToCollectionExercise(
      UUID collectionExerciseId, List<UUID> sampleSummaryIds) throws CTPException {
    sampleLinkRepository.deleteByCollectionExerciseId(collectionExerciseId);
    List<SampleLink> linkedSummaries = new ArrayList<>();
    for (UUID summaryId : sampleSummaryIds) {
      linkedSummaries.add(createLink(summaryId, collectionExerciseId));
    }

    transitionScheduleCollectionExerciseToReadyToReview(collectionExerciseId);

    return linkedSummaries;
  }

  /**
   * Delete SampleSummary link
   *
   * @param sampleSummaryId a sample summary uuid
   * @param collectionExerciseId a collection exercise uuid
   * @throws CTPException thrown if transition fails
   */
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
   * Create collection exercise.
   *
   * @param collex the data to create the collection exercise from
   * @param survey representation of the survey for the given collection exercise
   * @return created collection exercise
   */
  @Transactional
  public CollectionExercise createCollectionExercise(
      CollectionExerciseDTO collex, SurveyDTO survey) {
    log.with("survey_ref", survey.getSurveyRef())
        .with("exercise_ref", collex.getExerciseRef())
        .debug("Creating collection exercise");
    CollectionExercise collectionExercise = newCollectionExerciseFromDTO(collex);
    collectionExercise = this.collectRepo.saveAndFlush(collectionExercise);
    log.with("collection_exercise_id", collectionExercise.getId())
        .debug("Successfully created collection exercise");
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
    log.with("collection_exercise_id", collectionExercise.getId())
        .debug("Successfully created collection exercise from DTO");
    return collectionExercise;
  }

  /**
   * Patch a collection exercise
   *
   * @param id the id of the collection exercise to patch
   * @param patchData the patch data
   * @return the patched CollectionExercise object
   * @throws CTPException thrown if error occurs
   */
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
      SurveyDTO survey = null;
      if (!StringUtils.isBlank(patchData.getSurveyId())) {
        UUID surveyId = UUID.fromString(patchData.getSurveyId());

        survey = this.surveyService.findSurvey(surveyId);

        if (survey == null) {
          throw new CTPException(
              CTPException.Fault.BAD_REQUEST, String.format("Survey %s does not exist", surveyId));
        } else {
          collex.setSurveyId(surveyId);
        }
      } else {
        survey = this.surveyService.findSurvey(collex.getSurveyId());
      }
      if (!StringUtils.isBlank(patchData.getExerciseRef())) {
        collex.setExerciseRef(patchData.getExerciseRef());
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

  /**
   * Update a collection exercise
   *
   * @param id the id of the collection exercise to update
   * @param collexDto the updated collection exercise
   * @return the updated CollectionExercise object
   */
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

  /**
   * Update a collection exercise
   *
   * @param collex the updated collection exercise
   * @return the updated CollectionExercise object
   */
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

  /**
   * Delete a collection exercise
   *
   * @param id the id of the collection exercise to delete
   * @return the updated CollectionExercise object
   * @throws CTPException thrown if error occurs
   */
  public CollectionExercise deleteCollectionExercise(UUID id) throws CTPException {
    return updateCollectionExerciseDeleted(id, true);
  }

  /**
   * Undelete a collection exercise
   *
   * @param id the id of the collection exercise to delete
   * @return the updated CollectionExercise object
   * @throws CTPException thrown if error occurs
   */
  public CollectionExercise undeleteCollectionExercise(UUID id) throws CTPException {
    return updateCollectionExerciseDeleted(id, false);
  }

  /**
   * Find all collection exercises with a given state
   *
   * @param state the state to find
   * @return a list of collection exercises with the given state
   */
  public List<CollectionExercise> findByState(CollectionExerciseDTO.CollectionExerciseState state) {
    return collectRepo.findByState(state);
  }

  /**
   * Utility method to transition a collection exercise to a new state
   *
   * @param collex a collection exercise
   * @param event a collection exercise event
   * @throws CTPException thrown if the specified event is not valid for the current state
   */
  public void transitionCollectionExercise(
      CollectionExercise collex, CollectionExerciseDTO.CollectionExerciseEvent event)
      throws CTPException {
    CollectionExerciseDTO.CollectionExerciseState oldState = collex.getState();
    CollectionExerciseDTO.CollectionExerciseState newState =
        collectionExerciseTransitionState.transition(collex.getState(), event);

    if (oldState == newState) {
      return;
    }

    collex.setState(newState);
    updateCollectionExercise(collex);
    rabbitTemplate.convertAndSend(new CollectionTransitionEvent(collex.getId(), collex.getState()));
  }

  /**
   * Utility method to transition a collection exercise to a new state
   *
   * @param collectionExerciseId a collection exercise UUID
   * @param event a collection exercise event
   * @throws CTPException thrown if the specified event is not valid for the current state or a
   *     collection exercise with the given id cannot be found
   */
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

  public void transitionScheduleCollectionExerciseToReadyToReview(
      final CollectionExercise collectionExercise) throws CTPException {
    UUID collexId = collectionExercise.getId();

    Map<String, String> searchStringMap =
        Collections.singletonMap("COLLECTION_EXERCISE", collectionExercise.getId().toString());
    String searchStringJson = new JSONObject(searchStringMap).toString();
    Integer numberOfCollectionInstruments =
        collectionInstrumentSvcClient.countCollectionInstruments(searchStringJson);
    boolean allSamplesActive = allSamplesActive(collexId);
    boolean shouldTransition =
        allSamplesActive
            && numberOfCollectionInstruments != null
            && numberOfCollectionInstruments > 0;
    log.with("all_samples_active", allSamplesActive)
        .with("number_of_collection_instruments", numberOfCollectionInstruments)
        .with("should_transition", shouldTransition)
        .info("ready for review transition check");
    if (shouldTransition) {
      transitionCollectionExercise(
          collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_ADDED);
    } else {
      transitionCollectionExercise(
          collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_DELETED);
    }
  }

  /**
   * Transition scheduled collection exercises with collection instruments and samples to {@link
   * CollectionExerciseDTO.CollectionExerciseState#READY_FOR_REVIEW}
   */
  public void transitionScheduleCollectionExerciseToReadyToReview(final UUID collectionExerciseId)
      throws CTPException {
    CollectionExercise collex = findCollectionExercise(collectionExerciseId);

    if (collex != null) {
      transitionScheduleCollectionExerciseToReadyToReview(collex);
    }
  }

  private boolean allSamplesActive(final UUID collexId) throws CTPException {
    List<SampleLink> sampleLinks = this.sampleLinkRepository.findByCollectionExerciseId(collexId);
    if (sampleLinks.isEmpty()) {
      return false;
    }

    return sampleLinks
        .stream()
        .map(sampleLink -> sampleSvcClient.getSampleSummary(sampleLink.getSampleSummaryId()))
        .allMatch(ss -> ss.getState().equals(SampleSummaryDTO.SampleState.ACTIVE));
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
    return sampleLinkRepository.saveAndFlush(sampleLink);
  }
}
