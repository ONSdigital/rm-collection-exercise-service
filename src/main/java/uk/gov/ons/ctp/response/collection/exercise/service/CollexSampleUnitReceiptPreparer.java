package uk.gov.ons.ctp.response.collection.exercise.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;

@Component
public class CollexSampleUnitReceiptPreparer {
  private static final Logger log = LoggerFactory.getLogger(CollexSampleUnitReceiptPreparer.class);

  private final CollectionExerciseRepository collexRepo;
  private final StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  public CollexSampleUnitReceiptPreparer(
      CollectionExerciseRepository collexRepo,
      @Qualifier("collectionExercise")
          StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
              collectionExerciseTransitionState) {
    this.collexRepo = collexRepo;
    this.collectionExerciseTransitionState = collectionExerciseTransitionState;
  }

  // REQUIRES_NEW forces this to happen in a new transaction, so we know that it's been committed
  // when this method exits - it's important to avoid race condition
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void prepareCollexToAcceptSampleUnits(UUID collexId, int sampleSize) {
    CollectionExercise collex = collexRepo.findOneById(collexId);
    collex.setSampleSize(sampleSize);
    try {
      collex.setState(
          collectionExerciseTransitionState.transition(
              collex.getState(), CollectionExerciseEvent.EXECUTE));
    } catch (CTPException ex) {
      log.with("collex", collex).error("Could not update collex state to EXECUTION_STARTED", ex);
      throw new IllegalStateException();
    }

    collexRepo.saveAndFlush(collex);
  }
}
