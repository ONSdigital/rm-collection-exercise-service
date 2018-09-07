package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;

@Component
public class CollexSampleCountUpdater {
  private final CollectionExerciseRepository collexRepo;

  public CollexSampleCountUpdater(CollectionExerciseRepository collexRepo) {
    this.collexRepo = collexRepo;
  }

  // REQUIRES_NEW forces this to happen in a new transaction, so we know that it's been committed
  // when this method exits - it's important to avoid race condition
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateSampleSize(UUID collexId, int sampleSize) {
    CollectionExercise collex = collexRepo.findOneById(collexId);
    collex.setSampleSize(sampleSize);
    collexRepo.saveAndFlush(collex);
  }
}
