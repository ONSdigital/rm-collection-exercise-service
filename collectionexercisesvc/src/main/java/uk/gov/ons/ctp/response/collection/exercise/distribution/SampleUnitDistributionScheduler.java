package uk.gov.ons.ctp.response.collection.exercise.distribution;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/**
 * Schedule Publish of sample units in VALIDATED state
 *
 */
@Component
public class SampleUnitDistributionScheduler {

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Autowired
  private SampleService sampleService;

  /**
   * Carry out publish according to configured fixed delay.
   */
  @Scheduled(fixedDelayString = "#{appConfig.schedules.distributionScheduleDelayMilliSeconds}")
  public void scheduleDistribution() {

    List<CollectionExercise> exercises = collectRepo
        .findByState(CollectionExerciseDTO.CollectionExerciseState.VALIDATED);

    exercises.forEach((exercise) -> {
      sampleService.distributeSampleUnits(exercise);
    });
  }

}
