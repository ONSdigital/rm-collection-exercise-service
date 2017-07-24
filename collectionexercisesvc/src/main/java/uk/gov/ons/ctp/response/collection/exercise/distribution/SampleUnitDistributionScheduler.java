package uk.gov.ons.ctp.response.collection.exercise.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import net.sourceforge.cobertura.CoverageIgnore;


import java.util.List;

/**
 * Schedule Publish of sample units in VALIDATED state
 *
 */
@CoverageIgnore
@Component
public class SampleUnitDistributionScheduler {

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Autowired
  private SampleService sampleService;

  /**
   * Carry out publish according to configured fixed delay.
   * @throws CTPException if state transition errors
   */
  @Scheduled(fixedDelayString = "#{appConfig.schedules.distributionScheduleDelayMilliSeconds}")
  public void scheduleDistribution() throws CTPException {
    List<CollectionExercise> exercises = collectRepo
        .findByState(CollectionExerciseDTO.CollectionExerciseState.VALIDATED);

    for (CollectionExercise aCollectionExercise : exercises) {
      sampleService.distributeSampleUnits(aCollectionExercise);
    }
  }

}
