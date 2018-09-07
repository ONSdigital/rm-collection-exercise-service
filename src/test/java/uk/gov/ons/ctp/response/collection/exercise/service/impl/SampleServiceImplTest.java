package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollexSampleCountUpdater;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitType;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** Unit tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {
  private static final UUID COLLEX_ID = UUID.randomUUID();
  private static final UUID SAMPLE_ID = UUID.randomUUID();

  @Mock private SampleUnitRepository sampleUnitRepo;

  @Mock private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Mock private SampleLinkRepository sampleLinkRepo;

  @Mock private CollectionExerciseRepository collectRepo;

  @Mock private SampleSvcClient sampleSvcClient;

  @Mock private CollexSampleCountUpdater collexSampleCountUpdater;

  @Mock private PartySvcClient partySvcClient;

  @Mock
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @InjectMocks private SampleServiceImpl underTest;

  /** Unit test */
  @Test
  public void testAcceptSampleUnitCountNotEqual() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_ID);
    collex.setSampleSize(50);
    collex.setState(CollectionExerciseState.EXECUTION_STARTED);

    acceptSampleUnitWithCollex(collex);

    verify(collectRepo, never()).saveAndFlush(any());
    verify(collectionExerciseTransitionState, never()).transition(any(), any());
  }

  /** Unit test */
  @Test
  public void testAcceptSampleUnitCountEqual() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_ID);
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

  /** Unit test */
  @Test
  public void testAcceptSampleUnitAlreadyExists() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_ID);
    collex.setSampleSize(99);
    collex.setState(CollectionExerciseState.EXECUTION_STARTED);

    SampleUnit sampleUnit =
        SampleUnit.builder()
            .withId(SAMPLE_ID.toString())
            .withFormType("X")
            .withSampleUnitRef("REF123")
            .withSampleUnitType("B")
            .withCollectionExerciseId(COLLEX_ID.toString())
            .build();

    when(collectRepo.findOneById(any())).thenReturn(collex);
    when(sampleUnitRepo.existsBySampleUnitRefAndSampleUnitTypeAndSampleUnitGroupCollectionExercise(
            any(), any(), any()))
        .thenReturn(true);

    underTest.acceptSampleUnit(sampleUnit);

    verify(collectionExerciseTransitionState, never()).transition(any(), any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(sampleUnitRepo, never()).saveAndFlush(any());
    verify(collectRepo, never()).saveAndFlush(any());
  }

  @Test
  public void requestSampleUnitsHappyPath() throws CTPException {
    UUID collexId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collexId);
    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    List<SampleLink> sampleLinks = Collections.singletonList(sampleLink);
    SampleUnitsRequestDTO sampleUnitsRequestDTO = new SampleUnitsRequestDTO();
    sampleUnitsRequestDTO.setSampleUnitsTotal(666);

    // Given
    when(collectRepo.findOneById(eq(collexId))).thenReturn(collectionExercise);
    when(sampleLinkRepo.findByCollectionExerciseId(any())).thenReturn(sampleLinks);
    when(sampleSvcClient.getSampleUnitSize(any())).thenReturn(sampleUnitsRequestDTO);
    when(sampleSvcClient.requestSampleUnits(any())).thenReturn(sampleUnitsRequestDTO);

    // When
    underTest.requestSampleUnits(collexId);

    // Then
    verify(collexSampleCountUpdater).updateSampleSize(eq(collexId), eq(666));
    verify(partySvcClient).linkSampleSummaryId(any(), any());
    verify(collectionExerciseTransitionState).transition(any(), any());
    verify(collectRepo).saveAndFlush(any());
  }

  private void acceptSampleUnitWithCollex(CollectionExercise collex) throws CTPException {
    SampleUnit sampleUnit =
        SampleUnit.builder()
            .withId(SAMPLE_ID.toString())
            .withFormType("X")
            .withSampleUnitRef("REF123")
            .withSampleUnitType("B")
            .withCollectionExerciseId(collex.getId().toString())
            .build();

    when(collectRepo.findOneById(any())).thenReturn(collex);
    when(sampleUnitGroupRepo.saveAndFlush(any())).then(returnsFirstArg());
    when(sampleUnitRepo.existsBySampleUnitRefAndSampleUnitTypeAndSampleUnitGroupCollectionExercise(
            any(), any(), any()))
        .thenReturn(false);
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
    assertEquals(
        sampleUnitGroupArgumentCaptor.getValue(),
        sampleUnitArgumentCaptor.getValue().getSampleUnitGroup());
    assertEquals("REF123", sampleUnitArgumentCaptor.getValue().getSampleUnitRef());
    assertEquals(SampleUnitType.B, sampleUnitArgumentCaptor.getValue().getSampleUnitType());
    assertEquals(SAMPLE_ID, sampleUnitArgumentCaptor.getValue().getSampleUnitId());
  }
}
