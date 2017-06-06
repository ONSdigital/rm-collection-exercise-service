package uk.gov.ons.ctp.response.collection.exercise.distribution;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
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
   */
  public void distributeSampleUnits(CollectionExercise exercise) {

    List<ExerciseSampleUnitGroup> sampleUnitGroups = sampleUnitGroupRepo
        .findByStateFKAndCollectionExerciseOrderByModifiedDateTimeDesc(
            SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED,
            exercise, new PageRequest(0, appConfig.getSchedules().getDistributionScheduleRetrievalMax()));
    sampleUnitGroups.forEach((sampleUnitGroup) -> {
      distributeSampleUnits(sampleUnitGroup, exercise.getExercisePK(), exercise.getSurvey().getSurveyPK());
    });

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
   * @param sampleUnitGroup for which to distribute sample units.
   * @param exercisePK of CollectionExercise of which sampleUnitGroup is a member.
   * @param surveyPK of Survey of which CollectionExercise is a member.
   */
  private void distributeSampleUnits(ExerciseSampleUnitGroup sampleUnitGroup, Integer exercisePK, Integer surveyPK) {
    List<ExerciseSampleUnit> sampleUnits = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
    SampleUnitChild child = null;
    String actionPlanId = null;
    SampleUnitParent parent = null;
    for (ExerciseSampleUnit sampleUnit : sampleUnits) {
      if (sampleUnit.getSampleUnitType().isParent()) {
        parent = new SampleUnitParent();
        parent.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        parent.setSampleUnitType(sampleUnit.getSampleUnitType().name());
        parent.setPartyId(sampleUnit.getPartyId().toString());
        parent.setCollectionInstrumentId(sampleUnit.getCollectionInstrumentId().toString());
        actionPlanId = collectionExerciseRepo
            .getActiveActionPlanId(exercisePK, sampleUnit.getSampleUnitType().name(),
               surveyPK);
      } else {
        child = new SampleUnitChild();
        child.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        child.setSampleUnitType(sampleUnit.getSampleUnitType().name());
        child.setPartyId(sampleUnit.getPartyId().toString());
        child.setCollectionInstrumentId(sampleUnit.getCollectionInstrumentId().toString());
        child.setActionPlanId(
            collectionExerciseRepo
                .getActiveActionPlanId(exercisePK, sampleUnit.getSampleUnitType().name(),
                    surveyPK));
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
   */
  private void publishSampleUnit(ExerciseSampleUnitGroup sampleUnitGroup, SampleUnitParent sampleUnitMessage) {
    publisher.sendSampleUnit(sampleUnitMessage);
    sampleUnitGroup
        .setStateFK(sampleUnitGroupState.transition(sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.PUBLISH));
    sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);
  }
}
