package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sampleunit.definition.SampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;

/** Unit tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleServiceTest {
  private static final UUID COLLEX_ID = UUID.randomUUID();
  private static final UUID SAMPLE_ID = UUID.randomUUID();

  @Mock private SampleUnitRepository sampleUnitRepo;

  @Mock private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Mock private SampleLinkRepository sampleLinkRepo;

  @Mock private CollectionExerciseRepository collectRepo;

  @Mock private SampleSvcClient sampleSvcClient;

  @Mock private CollexSampleUnitReceiptPreparer collexSampleUnitReceiptPreparer;

  @Mock private PartySvcClient partySvcClient;

  @Mock
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @InjectMocks private SampleService underTest;

  /** Unit test */
  @Test
  public void testAcceptSampleUnitAlreadyExists() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_ID);
    collex.setSampleSize(99);
    collex.setState(CollectionExerciseState.EXECUTION_STARTED);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setCollectionExerciseId(COLLEX_ID.toString());
    sampleUnit.setFormType("X");
    sampleUnit.setId(SAMPLE_ID.toString());
    sampleUnit.setSampleUnitType("B");
    sampleUnit.setSampleUnitRef("REF123");
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
    when(sampleSvcClient.getSampleUnitCount(any())).thenReturn(sampleUnitsRequestDTO);
    when(sampleSvcClient.requestSampleUnits(any())).thenReturn(sampleUnitsRequestDTO);

    // When
    underTest.requestSampleUnits(collexId);

    // Then
    verify(collexSampleUnitReceiptPreparer).prepareCollexToAcceptSampleUnits(eq(collexId), eq(666));
    verify(partySvcClient).linkSampleSummaryId(any(), any());
  }
}
