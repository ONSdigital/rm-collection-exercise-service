package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

import java.sql.Timestamp;
import java.util.Date;

/**
 * The implementation of the SampleService
 *
 */
@Service
@Slf4j
public class SampleServiceImpl implements SampleService {

  private static final int TRANSACTION_TIMEOUT = 60;

  @Autowired
  private SampleUnitRepository sampleUnitRepo;

  @Autowired
  private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Autowired
  private SampleSvcClient sampleSvcClient;

  @Autowired
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionTransitionState;

  @Override
  public SampleUnitsRequestDTO requestSampleUnits(final String id) {

    SampleUnitsRequestDTO replyDTO = null;

    CollectionExercise collectionExercise = collectRepo
        .findOne(id);
    // Check collection exercise exists
    if (collectionExercise != null) {
      replyDTO = sampleSvcClient.requestSampleUnits(collectionExercise);

      if (replyDTO != null && replyDTO.getSampleUnitsTotal() > 0) {

        collectionExercise.setSampleSize(replyDTO.getSampleUnitsTotal());

        collectionExercise.setState(collectionTransitionState.transition(collectionExercise.getState(),
            CollectionExerciseEvent.REQUEST));
        collectRepo.saveAndFlush(collectionExercise);
      }
    }
    return replyDTO;
  }

  /**
   * Accepts the sample unit from the sample service. This checks that this is
   * dealing with the initial creation of the sample, no additions of sample
   * units to a sample unit group, no updates to a sample unit.
   *
   * @param sampleUnit the sample unit from the message.
   * @return the saved sample unit.
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false, timeout = TRANSACTION_TIMEOUT)
  @Override
  public ExerciseSampleUnit acceptSampleUnit(SampleUnit sampleUnit) {

    ExerciseSampleUnit exerciseSampleUnit = null;

    //TODO: Remove .toString()
    CollectionExercise collectionExercise = collectRepo
        .findOne(sampleUnit.getCollectionExerciseId().toString());

    // Check collection exercise exists
    if (collectionExercise != null) {

      // Check Sample Unit doesn't already exist for collection exercise
      if (!sampleUnitRepo.tupleExists(collectionExercise.getId(), sampleUnit.getSampleId())) {

        ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
        sampleUnitGroup.setCollectionExercise(collectionExercise);
        sampleUnitGroup.setFormType(sampleUnit.getFormType());
        sampleUnitGroup.setState(SampleUnitGroupState.INIT);
        sampleUnitGroup.setCreatedDateTime(new Timestamp(new Date().getTime()));
        sampleUnitGroup = sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);

        exerciseSampleUnit = new ExerciseSampleUnit();
        exerciseSampleUnit.setSampleUnitId(sampleUnit.getSampleUnitId());
        exerciseSampleUnit.setSampleUnitGroup(sampleUnitGroup);
        exerciseSampleUnit.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        exerciseSampleUnit.setSampleUnitType(sampleUnit.getSampleUnitType());

        sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

        if (sampleUnitRepo.totalByExerciseId(collectionExercise.getId()) == collectionExercise
            .getSampleSize()) {
          collectionExercise.setState(collectionTransitionState.transition(collectionExercise.getState(),
              CollectionExerciseEvent.EXECUTE));
          collectionExercise.setActualExecutionDateTime(new Timestamp(new Date().getTime()));
          collectRepo.saveAndFlush(collectionExercise);
        }

      } else {
        log.warn("SampleUnit {} already exists for CollectionExercise {}", sampleUnit.getSampleUnitId(),
            sampleUnit.getCollectionExerciseId());
      }
    } else {
      log.error("No CollectionExercise {} for SampleUnit {}", sampleUnit.getCollectionExerciseId(),
          sampleUnit.getSampleUnitId());
    }

    return exerciseSampleUnit;

  }
}
