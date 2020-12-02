package uk.gov.ons.ctp.response.collection.exercise.distribution;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.lib.casesvc.message.sampleunitnotification.SampleUnitParent;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed.LockingException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.Association;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.Enrolment;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
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

/** Class responsible for business logic to distribute SampleUnits. */
@Component
public class SampleUnitDistributor {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitDistributor.class);

  private static final String DISTRIBUTION_LIST_ID = "group";
  private static final String ENABLED = "ENABLED";
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;
  private static final int TRANSACTION_TIMEOUT = 60;

  private AppConfig appConfig;

  private SampleUnitPublisher publisher;

  private CollectionExerciseRepository collectionExerciseRepo;
  private EventRepository eventRepository;
  private SampleUnitGroupRepository sampleUnitGroupRepo;
  private SampleUnitRepository sampleUnitRepo;

  private ActionSvcClient actionSvcClient;
  private PartySvcClient partySvcClient;

  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  private DistributedListManager<Integer> sampleDistributionListManager;
  private final TransactionTemplate transactionTemplate;

  public SampleUnitDistributor(
      final AppConfig appConfig,
      final SampleUnitPublisher publisher,
      final CollectionExerciseRepository collectionExerciseRepo,
      final EventRepository eventRepository,
      final SampleUnitGroupRepository sampleUnitGroupRepo,
      final SampleUnitRepository sampleUnitRepo,
      final ActionSvcClient actionSvcClient,
      final PartySvcClient partySvcClient,
      final @Qualifier("collectionExercise") StateTransitionManager<
                  CollectionExerciseState, CollectionExerciseEvent>
              collectionExerciseTransitionState,
      final @Qualifier("sampleUnitGroup") StateTransitionManager<
                  SampleUnitGroupState, SampleUnitGroupEvent>
              sampleUnitGroupState,
      final @Qualifier("distribution") DistributedListManager<Integer>
              sampleDistributionListManager,
      final PlatformTransactionManager transactionManager) {
    this.appConfig = appConfig;
    this.publisher = publisher;
    this.collectionExerciseRepo = collectionExerciseRepo;
    this.eventRepository = eventRepository;
    this.sampleUnitGroupRepo = sampleUnitGroupRepo;
    this.sampleUnitRepo = sampleUnitRepo;
    this.actionSvcClient = actionSvcClient;
    this.partySvcClient = partySvcClient;
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
    log.with("collection_exercise_id", exercise.getId()).info("Distributing sample unit");
    List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
    try {
      sampleUnitGroups = retrieveSampleUnitGroups(exercise);
    } catch (LockingException ex) {
      log.error("Sample Unit Distribution failed", ex);
    }
    if (sampleUnitGroups.isEmpty()) {
      log.with("collection_exercise_id", exercise.getId())
          .debug("No sample unit groups to distribute for exercise");
      return;
    }

    // Catch errors distributing sample units so that only failing units are stopped
    sampleUnitGroups.forEach(
        sampleUnitGroup -> {
          try {
            distributeSampleUnitGroup(exercise, sampleUnitGroup);
          } catch (CTPException ex) {
            log.with("sampleUnitGroupPK", sampleUnitGroup.getSampleUnitGroupPK())
                .error("Failed to distribute sample unit group", ex);
          }
        });

    // Collection exercise will transition to LIVE/READY_FOR_LIVE
    // if all sample units were distributed successfully
    collectionExerciseTransitionState(exercise);

    try {
      sampleDistributionListManager.deleteList(DISTRIBUTION_LIST_ID, true);
    } catch (LockingException ex) {
      log.error("Failed to release sampleDistributionListManager data", ex);
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
    log.with("excluded_groups", excludedGroups).debug("DISTRIBUTION - Retrieve sampleUnitGroups");

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
   * Distribute SampleUnits for a SampleUnitGroup. Will send the sampleUnitParent data to Case via
   * Rabbit and will transition the sampleUnitGroup state in collection exercise to PUBLISHED on
   * success.
   *
   * @param exercise CollectionExercise of which sampleUnitGroup is a member
   * @param sampleUnitGroup for which to distribute sample units
   */
  private void distributeSampleUnitGroup(
      CollectionExercise exercise, ExerciseSampleUnitGroup sampleUnitGroup) throws CTPException {
    ExerciseSampleUnit sampleUnit = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup).get(0);
    SampleUnitParent sampleUnitParent;

    if (actionSvcClient.isDeprecated()) {
      sampleUnitParent = sampleUnit.toSampleUnitParent(exercise.getId());
    } else {
      String actionPlanId;
      if (sampleUnit.getSampleUnitType().equals(SampleUnitDTO.SampleUnitType.B)) {
        actionPlanId = getActionPlanIdBusiness(sampleUnit, exercise).toString();
      } else {
        actionPlanId = getActionPlanIdSocial(exercise).toString();
      }

      // SampleUnitParents/Children are being removed
      // We only expect one sample unit per sample unit group now
      // but still use SampleUnitParent class until it's removed from rabbit message
      sampleUnitParent = sampleUnit.toSampleUnitParent(actionPlanId, exercise.getId());
    }
    publishSampleUnitToCase(sampleUnitGroup, sampleUnitParent);
  }

  private UUID getActionPlanIdBusiness(ExerciseSampleUnit sampleUnit, CollectionExercise exercise)
      throws CTPException {
    boolean activeEnrolment;
    PartyDTO businessParty =
        partySvcClient.requestParty(sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
    activeEnrolment = surveyHasEnrolledRespondent(businessParty, exercise.getSurveyId().toString());
    return actionSvcClient
        .getActionPlanBySelectorsBusiness(exercise.getId().toString(), activeEnrolment)
        .getId();
  }

  private UUID getActionPlanIdSocial(CollectionExercise exercise) throws CTPException {
    return actionSvcClient.getActionPlanBySelectorsSocial(exercise.getId().toString()).getId();
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
  private void publishSampleUnitToCase(
      ExerciseSampleUnitGroup sampleUnitGroup, SampleUnitParent sampleUnitMessage) {

    transactionTemplate.execute(
        new TransactionCallbackWithoutResult() {
          // the code in this method executes in a transaction context
          protected void doInTransactionWithoutResult(TransactionStatus status) {
            try {
              sampleUnitGroupTransitionState(sampleUnitGroup);
              publisher.sendSampleUnit(sampleUnitMessage);
            } catch (CTPException ex) {
              log.error("Sample Unit group state transition failed", ex);
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
          log.with("collection_exercise_id", exercise.getId())
              .debug("Attempting to transition collection exercise to Live");
          // All sample units published and go live date in past, set exercise state to LIVE
          exercise.setState(
              collectionExerciseTransitionState.transition(
                  exercise.getState(), CollectionExerciseEvent.GO_LIVE));
        } else {
          // All sample units published, set exercise state to READY_FOR_LIVE
          log.with("collection_exercise_id", exercise.getId())
              .debug("Attempting to transition collection exercise to Ready for Live");
          exercise.setState(
              collectionExerciseTransitionState.transition(
                  exercise.getState(), CollectionExerciseDTO.CollectionExerciseEvent.PUBLISH));
          exercise.setActualPublishDateTime(new Timestamp(new Date().getTime()));
        }
        log.with("collection_exercise_id", exercise.getId())
            .with("state", exercise.getState())
            .debug("Successfully set collection exercise state");
        collectionExerciseRepo.saveAndFlush(exercise);
      }
    } catch (CTPException ex) {
      log.error("Collection Exercise state transition failed", ex);
    }
  }
}
