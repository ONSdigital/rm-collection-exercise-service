package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;

@RunWith(MockitoJUnitRunner.class)
public class CollexSampleCountUpdaterTest {
  @Mock
  private CollectionExerciseRepository collexRepo;

  @InjectMocks
  private CollexSampleCountUpdater underTest;

  @Test
  public void updateSampleSizeHappyPath() {
    UUID collexId = UUID.randomUUID();
    CollectionExercise collex = new CollectionExercise();

    // Given
    when(collexRepo.findOneById(eq(collexId))).thenReturn(collex);

    // When
    underTest.updateSampleSize(collexId, 666);

    // Then
    ArgumentCaptor<CollectionExercise> actual = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collexRepo).saveAndFlush(actual.capture());
    assertEquals(collex, actual.getValue());
    assertEquals(666, actual.getValue().getSampleSize().intValue());
  }
}
