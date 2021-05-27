package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException.Fault.SYSTEM_ERROR;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;

@RunWith(MockitoJUnitRunner.class)
public class CollexSampleUnitReceiptPreparerTest {
  @Mock private CollectionExerciseRepository collexRepo;

  @Mock
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @InjectMocks private CollexSampleUnitReceiptPreparer underTest;

  @Test
  public void prepareCollexHappyPath() throws CTPException {
    UUID collexId = UUID.randomUUID();
    CollectionExercise collex = new CollectionExercise();

    // Given
    when(collexRepo.findOneById(eq(collexId))).thenReturn(collex);
    when(collectionExerciseTransitionState.transition(any(), any()))
        .thenReturn(CollectionExerciseState.EXECUTION_STARTED);

    // When
    underTest.prepareCollexToAcceptSampleUnits(collexId, 666);

    // Then
    ArgumentCaptor<CollectionExercise> actual = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collexRepo).saveAndFlush(actual.capture());
    assertEquals(collex, actual.getValue());
    assertEquals(666, actual.getValue().getSampleSize().intValue());
    assertEquals(CollectionExerciseState.EXECUTION_STARTED, actual.getValue().getState());
  }

  @Test(expected = IllegalStateException.class)
  public void prepareCollexCantStateTransition() throws CTPException {
    UUID collexId = UUID.randomUUID();
    CollectionExercise collex = new CollectionExercise();

    // Given
    when(collexRepo.findOneById(eq(collexId))).thenReturn(collex);
    when(collectionExerciseTransitionState.transition(any(), any()))
        .thenThrow(new CTPException(SYSTEM_ERROR));

    // When
    underTest.prepareCollexToAcceptSampleUnits(collexId, 666);

    // Then
    // ...exceptions happen....
  }
}
