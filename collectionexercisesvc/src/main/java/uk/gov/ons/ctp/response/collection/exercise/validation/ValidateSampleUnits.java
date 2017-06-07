package uk.gov.ons.ctp.response.collection.exercise.validation;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;

/**
 * Class responsible for business logic to validate SampleUnits.
 *
 */
@Component
public class ValidateSampleUnits {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private SampleUnitRepository sampleUnitRepo;

  @Autowired
  private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Autowired
  @Qualifier("collectionExercise")
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseTransitionState;

  @Autowired
  @Qualifier("sampleUnitGroup")
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  /**
   * Validate SampleUnits
   *
   */
  public void validateSampleUnits() {

    List<CollectionExercise> exercises = collectRepo
        .findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED);

    List<ExerciseSampleUnitGroup> sampleUnitGroups = sampleUnitGroupRepo
        .findByStateFKAndCollectionExerciseInOrderByCreatedDateTimeDesc(SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            exercises, new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax()));

    sampleUnitGroups.forEach((sampleUnitGroup) -> {

      // TODO Call Survey, Party and CollectionInstrument service for
      // validation, dummy values set for PartyId and CollectionInstrumentId
      // below
      List<ExerciseSampleUnit> sampleUnits = sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
      sampleUnits.forEach((sampleUnit) -> {
        sampleUnit.setCollectionInstrumentId(UUID.randomUUID());
        sampleUnit.setPartyId(UUID.randomUUID());
        sampleUnitRepo.saveAndFlush(sampleUnit);
      });

      sampleUnitGroup
          .setStateFK(sampleUnitGroupState.transition(sampleUnitGroup.getStateFK(), SampleUnitGroupEvent.VALIDATE));
      if (sampleUnitGroup.getStateFK() == SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED) {
        sampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));
        sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);
      }
    });

    exercises.forEach((exercise) -> {
      if (sampleUnitGroupRepo.countByStateFKAndCollectionExercise(SampleUnitGroupDTO.SampleUnitGroupState.INIT,
          exercise) == 0) {
        exercise.setState(collectionExerciseTransitionState.transition(exercise.getState(),
            CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE));
        collectRepo.saveAndFlush(exercise);
      }
    });
  }

}
