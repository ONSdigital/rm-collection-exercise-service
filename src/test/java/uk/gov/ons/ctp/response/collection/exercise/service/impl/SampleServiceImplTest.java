package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitType;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {
  private static final UUID COLLEX_ID = UUID.randomUUID();
  private static final UUID SAMPLE_ID = UUID.randomUUID();

  @Mock private SampleUnitRepository sampleUnitRepo;

  @Mock private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Mock private CollectionExerciseRepository collectRepo;

  @Mock
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @InjectMocks private SampleServiceImpl underTest;

  @Test
  public void testAcceptSampleUnit_CountNotEqual() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setSampleSize(50);
    collex.setState(CollectionExerciseState.EXECUTION_STARTED);

    acceptSampleUnitWithCollex(collex);

    verify(collectRepo, never()).saveAndFlush(any());
    verify(collectionExerciseTransitionState, never()).transition(any(), any());
  }

  @Test
  public void testAcceptSampleUnit_CountEqual() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setSampleSize(99);
    collex.setState(CollectionExerciseState.EXECUTION_STARTED);

    when(collectionExerciseTransitionState.transition(any(), any()))
        .thenReturn(CollectionExerciseState.EXECUTED);

    acceptSampleUnitWithCollex(collex);

    ArgumentCaptor<CollectionExercise> collexArgumentCaptor =
        ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectRepo).saveAndFlush(collexArgumentCaptor.capture());
    assertEquals(CollectionExerciseState.EXECUTED, collexArgumentCaptor.getValue().getState());
    assertNotNull(collexArgumentCaptor.getValue().getActualExecutionDateTime());
  }

  private void acceptSampleUnitWithCollex(CollectionExercise collex) throws CTPException {
    SampleUnit sampleUnit =
        SampleUnit.builder()
            .withId(SAMPLE_ID.toString())
            .withFormType("X")
            .withSampleUnitRef("REF123")
            .withSampleUnitType("B")
            .withCollectionExerciseId(COLLEX_ID.toString())
            .build();

    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();

    when(collectRepo.findOneById(any())).thenReturn(collex);
    when(sampleUnitGroupRepo.saveAndFlush(any())).thenReturn(sampleUnitGroup);
    when(sampleUnitRepo.tupleExists(any(), any(), any())).thenReturn(false);
    when(sampleUnitRepo.countBySampleUnitGroupCollectionExercise(any())).thenReturn(99);

    underTest.acceptSampleUnit(sampleUnit);

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupArgumentCaptor =
        ArgumentCaptor.forClass(ExerciseSampleUnitGroup.class);
    verify(sampleUnitGroupRepo).saveAndFlush(sampleUnitGroupArgumentCaptor.capture());
    assertEquals(collex, sampleUnitGroupArgumentCaptor.getValue().getCollectionExercise());
    assertEquals(SampleUnitGroupState.INIT, sampleUnitGroupArgumentCaptor.getValue().getStateFK());
    assertEquals("X", sampleUnitGroupArgumentCaptor.getValue().getFormType());
    assertNotNull(sampleUnitGroupArgumentCaptor.getValue().getCreatedDateTime());

    ArgumentCaptor<ExerciseSampleUnit> sampleUnitArgumentCaptor =
        ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(sampleUnitRepo).saveAndFlush(sampleUnitArgumentCaptor.capture());
    assertEquals(sampleUnitGroup, sampleUnitArgumentCaptor.getValue().getSampleUnitGroup());
    assertEquals("REF123", sampleUnitArgumentCaptor.getValue().getSampleUnitRef());
    assertEquals(SampleUnitType.B, sampleUnitArgumentCaptor.getValue().getSampleUnitType());
    assertEquals(SAMPLE_ID, sampleUnitArgumentCaptor.getValue().getSampleUnitId());
  }
}
