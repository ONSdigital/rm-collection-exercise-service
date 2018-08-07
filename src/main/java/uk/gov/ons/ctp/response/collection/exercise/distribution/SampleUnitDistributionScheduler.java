package uk.gov.ons.ctp.response.collection.exercise.distribution;

import java.util.List;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/** Schedule Publish of sample units in VALIDATED state */
@CoverageIgnore
@Component
public class SampleUnitDistributionScheduler {

  private CollectionExerciseRepository collectRepo;

  private SampleService sampleService;

  @Autowired
  public SampleUnitDistributionScheduler(
      CollectionExerciseRepository collectRepo, SampleService sampleService) {
    this.collectRepo = collectRepo;
    this.sampleService = sampleService;
  }

  /** Carry out publish according to configured fixed delay. */
  @Scheduled(fixedDelayString = "#{appConfig.schedules.distributionScheduleDelayMilliSeconds}")
  public void scheduleDistribution() {
    List<CollectionExercise> exercises =
        collectRepo.findByState(CollectionExerciseDTO.CollectionExerciseState.VALIDATED);

    for (CollectionExercise collectionExercise : exercises) {
      sampleService.distributeSampleUnits(collectionExercise);
    }
  }
}
