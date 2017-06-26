package uk.gov.ons.ctp.response.collection.exercise.distribution;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitChild;
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

/**
 * Class responsible for business logic to distribute SampleUnits.
 *
 */
@Component
@Slf4j
public class SampleUnitDistributor {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Autowired
  private SampleUnitRepository sampleUnitRepo;

  @Autowired
  private CollectionExerciseRepository collectionExerciseRepo;

  @Autowired
  private SampleUnitPublisher publisher;

  @Autowired
  @Qualifier("collectionExercise")
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseTransitionState;

  @Autowired
  @Qualifier("sampleUnitGroup")
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  /**
   * Distribute SampleUnits for a CollectionExercise.
   *
   * @param exercise for which to distribute sample units.
   * @throws CTPException if sampleUnitGroup state transition error
   */
  public void distributeSampleUnits(CollectionExercise exercise) throws CTPException {

    List<ExerciseSampleUnitGroup> sampleUnitGroups = sampleUnitGroupRepo
        .findByStateFKAndCollectionExerciseOrderByModifiedDateTimeDesc(
            SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED,
            exercise, new PageRequest(0, appConfig.getSchedules().getDistributionScheduleRetrievalMax()));
    for (ExerciseSampleUnitGroup anExerciseSampleUnitGroup : sampleUnitGroups) {
      distributeSampleUnits(exercise, anExerciseSampleUnitGroup);
    }

    if (sampleUnitGroupRepo.countByStateFKAndCollectionExercise(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED,
        exercise) == 0) {
      exercise.setState(collectionExerciseTransitionState.transition(exercise.getState(),
          CollectionExerciseDTO.CollectionExerciseEvent.PUBLISH));
      exercise.setActualPublishDateTime(new Timestamp(new Date().getTime()));
      collectionExerciseRepo.saveAndFlush(exercise);
    }
  }

  /**
   * Distribute SampleUnits for a SampleUnitGroup.
   *
   * @param exercise CollectionExercise of which sampleUnitGroup is a member.
   * @param sampleUnitGroup for which to distribute sample units.
   * @throws CTPException if sampleUnitGroup state transition error
   */
  private void distributeSampleUnits(CollectionExercise exercise, ExerciseSampleUnitGroup sampleUnitGroup)
          throws CTPException {
    List<ExerciseSampleUnit> sampleUnits = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
    SampleUnitChild child = null;
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
        actionPlanId = collectionExerciseRepo
            .getActiveActionPlanId(exercise.getExercisePK(), sampleUnit.getSampleUnitType().name(),
                exercise.getSurvey().getSurveyPK());
      } else {
        child = new SampleUnitChild();
        child.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        child.setSampleUnitType(sampleUnit.getSampleUnitType().name());
        child.setPartyId(sampleUnit.getPartyId().toString());
        child.setCollectionInstrumentId(sampleUnit.getCollectionInstrumentId().toString());
        child.setActionPlanId(
            collectionExerciseRepo
                .getActiveActionPlanId(exercise.getExercisePK(), sampleUnit.getSampleUnitType().name(),
                    exercise.getSurvey().getSurveyPK()));
      }
    }

    if (!(parent == null)) {
      if (!(child == null)) {
        parent.setSampleUnitChild(child);
        publishSampleUnit(sampleUnitGroup, parent);
      } else if (!(actionPlanId == null)) {
        parent.setActionPlanId(actionPlanId);
        publishSampleUnit(sampleUnitGroup, parent);
      } else {
        log.error("No Child or ActionPlan for SampleUnitRef {}, SampleUnitType {}", parent.getSampleUnitRef(),
            parent.getSampleUnitType());
      }
    } else {
      log.error("No Parent for SampleUnit in SampleUnitGroupPK {} ", sampleUnitGroup.getSampleUnitGroupPK());
    }
  }

  /**
   * Publish a message to the Case Service for a SampleUnitGroup and transition state
   *
   * @param sampleUnitGroup from which publish message created and for which to transition state.
   * @param sampleUnitMessage to publish.
   * @throws CTPException if sampleUnitGroup state transition error
   */
  private void publishSampleUnit(ExerciseSampleUnitGroup sampleUnitGroup, SampleUnitParent sampleUnitMessage)
          throws CTPException {
    publisher.sendSampleUnit(sampleUnitMessage);
    sampleUnitGroup
        .setStateFK(sampleUnitGroupState.transition(sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.PUBLISH));
    sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);
  }
}
