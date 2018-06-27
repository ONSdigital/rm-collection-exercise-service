package uk.gov.ons.ctp.response.collection.exercise.distribution;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitChild;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitChildren;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitParent;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;

/** Class responsible for business logic to distribute SampleUnits. */
@Component
@Slf4j
public class SampleUnitDistributor {

  private static final String DISTRIBUTION_LIST_ID = "group";
  // this is a bit of a kludge - jpa does not like having an IN clause with an
  // empty list
  // it does not return results when you expect it to - so ... always have this
  // in the list of excluded case ids
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;
  private static final int TRANSACTION_TIMEOUT = 60;

  @Autowired private AppConfig appConfig;

  @Autowired private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Autowired private SampleUnitRepository sampleUnitRepo;

  @Autowired private CollectionExerciseRepository collectionExerciseRepo;

  @Autowired private SampleUnitPublisher publisher;

  @Autowired
  @Qualifier("collectionExercise")
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @Autowired
  @Qualifier("sampleUnitGroup")
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  @Autowired
  @Qualifier("distribution")
  private DistributedListManager<Integer> sampleDistributionListManager;

  // single TransactionTemplate shared amongst all methods in this instance
  private final TransactionTemplate transactionTemplate;

  /**
   * Constructor into which the Spring PlatformTransactionManager is injected
   *
   * @param transactionManager provided by Spring
   */
  @Autowired
  public SampleUnitDistributor(final PlatformTransactionManager transactionManager) {
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setTimeout(TRANSACTION_TIMEOUT);
  }

  /**
   * Distribute SampleUnits for a CollectionExercise.
   *
   * @param exercise for which to distribute sample units.
   */
  public void distributeSampleUnits(CollectionExercise exercise) {

    try {
      List<ExerciseSampleUnitGroup> sampleUnitGroups = retrieveSampleUnitGroups(exercise);

      for (ExerciseSampleUnitGroup anExerciseSampleUnitGroup : sampleUnitGroups) {
        distributeSampleUnits(exercise, anExerciseSampleUnitGroup);
      }

      if (!sampleUnitGroups.isEmpty()) {
        collectionExerciseTransitionState(exercise);
      }

    } catch (LockingException ex) {
      log.error("Distribution failed due to {}", ex.getMessage());
      log.error("Stack trace: " + ex);
    } finally {
      try {
        sampleDistributionListManager.deleteList(DISTRIBUTION_LIST_ID, true);
      } catch (LockingException ex) {
        log.error(
            "Failed to release sampleDistributionListManager data - error msg is {}",
            ex.getMessage());
        log.error("Stack trace: " + ex);
      }
    }
  }

  /**
   * Distribute SampleUnits for a SampleUnitGroup.
   *
   * @param exercise CollectionExercise of which sampleUnitGroup is a member.
   * @param sampleUnitGroup for which to distribute sample units.
   */
  private void distributeSampleUnits(
      CollectionExercise exercise, ExerciseSampleUnitGroup sampleUnitGroup) {
    List<ExerciseSampleUnit> sampleUnits = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
    List<SampleUnitChild> children = new ArrayList<SampleUnitChild>();
    String actionPlanId = null;
    SampleUnitParent parent = null;
    for (ExerciseSampleUnit sampleUnit : sampleUnits) {
      if (sampleUnit.getSampleUnitType().isParent()) {
        parent = new SampleUnitParent();
        parent.setCollectionExerciseId(exercise.getId().toString());
        parent.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        parent.setSampleUnitType(sampleUnit.getSampleUnitType().name());
        parent.setPartyId(sampleUnit.getPartyId().toString());
        parent.setCollectionInstrumentId(sampleUnit.getCollectionInstrumentId().toString());
        actionPlanId =
            collectionExerciseRepo.getActiveActionPlanId(
                exercise.getExercisePK(),
                sampleUnit.getSampleUnitType().name(),
                exercise.getSurveyId());
      } else {
        SampleUnitChild child = new SampleUnitChild();
        child.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        child.setSampleUnitType(sampleUnit.getSampleUnitType().name());
        child.setPartyId(sampleUnit.getPartyId().toString());
        child.setCollectionInstrumentId(sampleUnit.getCollectionInstrumentId().toString());
        child.setActionPlanId(
            collectionExerciseRepo.getActiveActionPlanId(
                exercise.getExercisePK(),
                sampleUnit.getSampleUnitType().name(),
                exercise.getSurveyId()));
        children.add(child);
      }
    }

    if ((parent != null)) {
      if (!children.isEmpty()) {
        parent.setSampleUnitChildren(new SampleUnitChildren(children));
        publishSampleUnit(sampleUnitGroup, parent);
      } else if ((actionPlanId != null)) {
        parent.setActionPlanId(actionPlanId);
        publishSampleUnit(sampleUnitGroup, parent);
      } else {
        log.error(
            "No Child or ActionPlan for SampleUnitRef {}, SampleUnitType {}",
            parent.getSampleUnitRef(),
            parent.getSampleUnitType());
      }
    } else {
      log.error(
          "No Parent for SampleUnit in SampleUnitGroupPK {} ",
          sampleUnitGroup.getSampleUnitGroupPK());
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

    List<ExerciseSampleUnitGroup> sampleUnitGroups;

    List<Integer> excludedGroups =
        sampleDistributionListManager.findList(DISTRIBUTION_LIST_ID, false);
    log.debug("DISTRIBUTION - Retrieve sampleUnitGroups excluding {}", excludedGroups);

    excludedGroups.add(Integer.valueOf(IMPOSSIBLE_ID));
    sampleUnitGroups =
        sampleUnitGroupRepo
            .findByStateFKAndCollectionExerciseAndSampleUnitGroupPKNotInOrderByModifiedDateTimeAsc(
                SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED,
                exercise,
                excludedGroups,
                new PageRequest(0, appConfig.getSchedules().getDistributionScheduleRetrievalMax()));

    if (!CollectionUtils.isEmpty(sampleUnitGroups)) {
      log.debug(
          "DISTRIBUTION retrieved sampleUnitGroup PKs {}",
          sampleUnitGroups
              .stream()
              .map(group -> group.getSampleUnitGroupPK().toString())
              .collect(Collectors.joining(",")));
      sampleDistributionListManager.saveList(
          DISTRIBUTION_LIST_ID,
          sampleUnitGroups
              .stream()
              .map(group -> group.getSampleUnitGroupPK())
              .collect(Collectors.toList()),
          true);
    } else {
      log.debug("DISTRIBUTION retrieved 0 sampleUnitGroups PKs");
      sampleDistributionListManager.unlockContainer();
    }
    return sampleUnitGroups;
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
              log.error("Stack trace: " + ex);
            }
          }
        });
  }

  /**
   * Transition Sample Unit Group state for publish.
   *
   * @param sampleUnitGroup to be transitioned.
   * @return sampleUnitGroup with new state.
   * @throws CTPException if state transition fails.
   */
  private ExerciseSampleUnitGroup sampleUnitGroupTransitionState(
      ExerciseSampleUnitGroup sampleUnitGroup) throws CTPException {

    sampleUnitGroup.setStateFK(
        sampleUnitGroupState.transition(
            sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.PUBLISH));
    sampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
    sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);

    return sampleUnitGroup;
  }

  /**
   * Transition Collection Exercise state for distribution.
   *
   * @param exercise to transition.
   * @return exercise Collection Exercise with new state.
   */
  private CollectionExercise collectionExerciseTransitionState(CollectionExercise exercise) {

    long published =
        sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED, exercise);

    try {
      if (published == exercise.getSampleSize().longValue()) {
        // All sample units published, set exercise state to PUBLISHED
        exercise.setState(
            collectionExerciseTransitionState.transition(
                exercise.getState(), CollectionExerciseDTO.CollectionExerciseEvent.PUBLISH));
        exercise.setActualPublishDateTime(new Timestamp(new Date().getTime()));
        collectionExerciseRepo.saveAndFlush(exercise);
      }
    } catch (CTPException ex) {
      log.error("Collection Exercise state transition failed: {}", ex.getMessage());
      log.error("Stack trace: " + ex);
    }
    return exercise;
  }
}
