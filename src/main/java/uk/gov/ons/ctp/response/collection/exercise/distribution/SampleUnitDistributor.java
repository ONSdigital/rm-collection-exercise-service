package uk.gov.ons.ctp.response.collection.exercise.distribution;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitParent;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.party.representation.Association;
import uk.gov.ons.ctp.response.party.representation.Enrolment;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/** Class responsible for business logic to distribute SampleUnits. */
@Component
@Slf4j
public class SampleUnitDistributor {

  private static final String DISTRIBUTION_LIST_ID = "group";
  private static final String ENABLED = "ENABLED";
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;
  private static final int TRANSACTION_TIMEOUT = 60;

  private AppConfig appConfig;

  private CollectionExerciseRepository collectionExerciseRepo;
  private EventRepository eventRepository;
  private SampleUnitGroupRepository sampleUnitGroupRepo;
  private SampleUnitRepository sampleUnitRepo;

  private ActionSvcClient actionSvcClient;
  private PartySvcClient partySvcClient;
  private SurveySvcClient surveySvcClient;

  private SampleUnitPublisher publisher;

  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  private DistributedListManager<Integer> sampleDistributionListManager;
  private final TransactionTemplate transactionTemplate;

  /**
   * Constructor into which the Spring PlatformTransactionManager is injected
   *
   * @param transactionManager provided by Spring
   */
  @Autowired
  public SampleUnitDistributor(
      AppConfig appConfig,
      CollectionExerciseRepository collectionExerciseRepo,
      EventRepository eventRepository,
      SampleUnitGroupRepository sampleUnitGroupRepo,
      SampleUnitRepository sampleUnitRepo,
      ActionSvcClient actionSvcClient,
      PartySvcClient partySvcClient,
      SurveySvcClient surveySvcClient,
      SampleUnitPublisher publisher,
      @Qualifier("collectionExercise")
          StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
              collectionExerciseTransitionState,
      @Qualifier("sampleUnitGroup")
          StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState,
      @Qualifier("distribution") DistributedListManager<Integer> sampleDistributionListManager,
      final PlatformTransactionManager transactionManager) {

    this.appConfig = appConfig;
    this.collectionExerciseRepo = collectionExerciseRepo;
    this.eventRepository = eventRepository;
    this.sampleUnitGroupRepo = sampleUnitGroupRepo;
    this.sampleUnitRepo = sampleUnitRepo;
    this.actionSvcClient = actionSvcClient;
    this.partySvcClient = partySvcClient;
    this.surveySvcClient = surveySvcClient;
    this.publisher = publisher;
    this.collectionExerciseTransitionState = collectionExerciseTransitionState;
    this.sampleUnitGroupState = sampleUnitGroupState;
    this.sampleDistributionListManager = sampleDistributionListManager;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setTimeout(TRANSACTION_TIMEOUT);
  }

  /**
   * Distribute SampleUnits for a CollectionExercise
   *
   * @param exercise for which to distribute sample units
   */
  public void distributeSampleUnits(CollectionExercise exercise) {

    try {
      List<ExerciseSampleUnitGroup> sampleUnitGroups = retrieveSampleUnitGroups(exercise);
      if (sampleUnitGroups.isEmpty()) {
        log.debug(
            "No sample unit groups to distribute for exercise, collectionExerciseId: {}",
            exercise.getId());
        return;
      }

      for (ExerciseSampleUnitGroup sampleUnitGroup : sampleUnitGroups) {
        try {
          distributeSampleUnitGroup(exercise, sampleUnitGroup);
        } catch (RestClientException ex) {
          log.error(
              "Failed to distribute sample unit group, sampleUnitGroupPK: {}",
              sampleUnitGroup.getSampleUnitGroupPK());
        }
      }

      collectionExerciseTransitionState(exercise);

    } catch (LockingException ex) {
      log.error("Sample Unit Distribution failed, error: {}", ex.getMessage());
      log.error(ex.toString());
    } finally {
      try {
        sampleDistributionListManager.deleteList(DISTRIBUTION_LIST_ID, true);
      } catch (LockingException ex) {
        log.error(
            "Failed to release sampleDistributionListManager data, error: {}", ex.getMessage());
        log.error(ex.toString());
      }
    }
  }

  /**
   * Retrieve SampleUnitGroups to be distributed - state VALIDATED - but do not retrieve the same
   * SampleUnitGroups as other service instances.
   *
   * @param exercise in VALIDATED state for which to return sampleUnitGroups.
   * @return list of SampleUnitGroups.
   * @throws LockingException problem obtaining lock for data shared across instances.
   */
  private List<ExerciseSampleUnitGroup> retrieveSampleUnitGroups(CollectionExercise exercise)
      throws LockingException {

    List<Integer> excludedGroups =
        sampleDistributionListManager.findList(DISTRIBUTION_LIST_ID, false);
    log.debug("DISTRIBUTION - Retrieve sampleUnitGroups excluding {}", excludedGroups);

    excludedGroups.add(IMPOSSIBLE_ID);
    List<ExerciseSampleUnitGroup> sampleUnitGroups =
        sampleUnitGroupRepo
            .findByStateFKAndCollectionExerciseAndSampleUnitGroupPKNotInOrderByModifiedDateTimeAsc(
                SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED,
                exercise,
                excludedGroups,
                new PageRequest(0, appConfig.getSchedules().getDistributionScheduleRetrievalMax()));

    if (!CollectionUtils.isEmpty(sampleUnitGroups)) {
      List<Integer> sampleUnitGroupPKs =
          sampleUnitGroups
              .stream()
              .map(ExerciseSampleUnitGroup::getSampleUnitGroupPK)
              .collect(Collectors.toList());
      log.debug(
          "DISTRIBUTION retrieved sampleUnitGroups, sampleUnitGroupPKs: {}",
          sampleUnitGroupPKs.stream().map(Object::toString));
      sampleDistributionListManager.saveList(DISTRIBUTION_LIST_ID, sampleUnitGroupPKs, true);
    } else {
      log.debug("DISTRIBUTION retrieved 0 sampleUnitGroups PKs");
      sampleDistributionListManager.unlockContainer();
    }
    return sampleUnitGroups;
  }

  /**
   * Distribute SampleUnits for a SampleUnitGroup
   *
   * @param exercise CollectionExercise of which sampleUnitGroup is a member
   * @param sampleUnitGroup for which to distribute sample units
   */
  private void distributeSampleUnitGroup(
      CollectionExercise exercise, ExerciseSampleUnitGroup sampleUnitGroup)
      throws RestClientException {
    ExerciseSampleUnit sampleUnit = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup).get(0);

    String actionPlanId;
    if (sampleUnit.getSampleUnitType().equals(SampleUnitDTO.SampleUnitType.B)) {
      actionPlanId = getActionPlanIdBusiness(sampleUnit, exercise).toString();
    } else {
      actionPlanId = getActionPlanIdSocial(exercise).toString();
    }

    // SampleUnitParents/Children are being removed
    // We only expect one sample unit per sample unit group now
    // but still use SampleUnitParent class until it's removed from rabbit message
    SampleUnitParent sampleUnitParent =
        sampleUnit.toSampleUnitParent(actionPlanId, exercise.getId());
    publishSampleUnit(sampleUnitGroup, sampleUnitParent);
  }

  private UUID getActionPlanIdBusiness(ExerciseSampleUnit sampleUnit, CollectionExercise exercise)
      throws RestClientException {
    PartyDTO businessParty =
        partySvcClient.requestParty(sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
    Boolean activeEnrolment =
        surveyHasEnrolledRespondent(businessParty, exercise.getSurveyId().toString());
    return actionSvcClient
        .getActionPlansBySelectorsBusiness(exercise.getId().toString(), activeEnrolment)
        .get(0)
        .getId();
  }

  private UUID getActionPlanIdSocial(CollectionExercise exercise) throws RestClientException {
    return actionSvcClient
        .getActionPlansBySelectorsSocial(exercise.getId().toString())
        .get(0)
        .getId();
  }

  private boolean surveyHasEnrolledRespondent(PartyDTO party, String surveyId) {
    List<Enrolment> enrolments =
        party
            .getAssociations()
            .stream()
            .map(Association::getEnrolments)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    return enrolments
        .stream()
        .anyMatch(enrolment -> enrolmentIsEnabledForSurvey(enrolment, surveyId));
  }

  private boolean enrolmentIsEnabledForSurvey(final Enrolment enrolment, String surveyId) {
    return enrolment.getSurveyId().equals(surveyId)
        && enrolment.getEnrolmentStatus().equalsIgnoreCase(ENABLED);
  }

  /**
   * Publish a message to the Case Service for a SampleUnitGroup and transition state. Note this is
   * a transaction boundary but as a private method we cannot use Spring declarative transaction
   * management but must use a programmatic transaction.
   *
   * @param sampleUnitGroup from which publish message created and for which to transition state.
   * @param sampleUnitMessage to publish.
   */
  private void publishSampleUnit(
      ExerciseSampleUnitGroup sampleUnitGroup, SampleUnitParent sampleUnitMessage) {

    transactionTemplate.execute(
        new TransactionCallbackWithoutResult() {
          // the code in this method executes in a transaction context
          protected void doInTransactionWithoutResult(TransactionStatus status) {
            try {
              sampleUnitGroupTransitionState(sampleUnitGroup);
              publisher.sendSampleUnit(sampleUnitMessage);
            } catch (CTPException ex) {
              log.error("Sample Unit group state transition failed: {}", ex.getMessage());
              log.error(ex.toString());
            }
          }
        });
  }

  /**
   * Transition Sample Unit Group state for publish.
   *
   * @param sampleUnitGroup to be transitioned.
   * @throws CTPException if state transition fails.
   */
  private void sampleUnitGroupTransitionState(ExerciseSampleUnitGroup sampleUnitGroup)
      throws CTPException {

    sampleUnitGroup.setStateFK(
        sampleUnitGroupState.transition(
            sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.PUBLISH));
    sampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
    sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);
  }

  /**
   * Transition Collection Exercise state for distribution.
   *
   * @param exercise to transition.
   */
  private void collectionExerciseTransitionState(CollectionExercise exercise) {

    long published =
        sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED, exercise);

    try {
      if (published == exercise.getSampleSize().longValue()) {

        // Check if go_live date is in the past
        if ((eventRepository
                .findOneByCollectionExerciseAndTag(exercise, EventService.Tag.go_live.name())
                .getTimestamp()
                .getTime())
            < System.currentTimeMillis()) {
          log.debug(
              "Attempting to transition collection exercise to Live, collectionExerciseId={}",
              exercise.getId());
          // All sample units published and go live date in past, set exercise state to LIVE
          exercise.setState(
              collectionExerciseTransitionState.transition(
                  exercise.getState(), CollectionExerciseEvent.GO_LIVE));
        } else {
          // All sample units published, set exercise state to READY_FOR_LIVE
          log.debug(
              "Attempting to transition collection exercise to Ready for Live, "
                  + "collectionExerciseId={}",
              exercise.getId());
          exercise.setState(
              collectionExerciseTransitionState.transition(
                  exercise.getState(), CollectionExerciseDTO.CollectionExerciseEvent.PUBLISH));
          exercise.setActualPublishDateTime(new Timestamp(new Date().getTime()));
        }
        log.debug(
            "Successfully set collection exercise state collectionExerciseId={}, state={}",
            exercise.getId(),
            exercise.getState());
        collectionExerciseRepo.saveAndFlush(exercise);
      }
    } catch (CTPException ex) {
      log.error("Collection Exercise state transition failed: {}", ex.getMessage());
      log.error("Stack trace: " + ex);
    }
  }
}
