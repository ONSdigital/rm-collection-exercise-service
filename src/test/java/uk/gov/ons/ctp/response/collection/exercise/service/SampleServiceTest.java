package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.junit.Assert.assertEquals;
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
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sampleunit.definition.SampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitValidationErrorDTO;

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

  @InjectMocks private SampleService sampleService;

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

    sampleService.acceptSampleUnit(sampleUnit);

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
    sampleService.requestSampleUnits(collexId);

    // Then
    verify(collexSampleUnitReceiptPreparer).prepareCollexToAcceptSampleUnits(eq(collexId), eq(666));
    verify(partySvcClient).linkSampleSummaryId(any(), any());
  }

  @Test
  public void getValidationErrorsMissingCollectionInstrument() {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);
    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    List<SampleLink> sampleLinks = Collections.singletonList(sampleLink);

    UUID sampleUnitId = UUID.randomUUID();
    String sampleUnitRef = "111111";
    SampleUnitDTO sampleUnitDTO = new SampleUnitDTO();
    sampleUnitDTO.setState(SampleUnitDTO.SampleUnitState.FAILED);
    sampleUnitDTO.setCollectionInstrumentId(null);
    sampleUnitDTO.setPartyId(UUID.randomUUID());
    sampleUnitDTO.setId(sampleUnitId.toString());
    sampleUnitDTO.setSampleUnitRef(sampleUnitRef);

    SampleUnitDTO[] sampleUnitDTOs = new SampleUnitDTO[] {sampleUnitDTO};

    // Given
    when(sampleLinkRepo.findByCollectionExerciseId(any())).thenReturn(sampleLinks);
    when(sampleSvcClient.requestSampleUnitsForSampleSummary(sampleSummaryId, true))
        .thenReturn(sampleUnitDTOs);

    // when
    SampleUnitValidationErrorDTO[] validationErrors =
        sampleService.getValidationErrors(collectionExercise);

    // then
    assertEquals(validationErrors.length, 1);
    SampleUnitValidationErrorDTO sampleUnitValidationErrorDTO = validationErrors[0];
    assertEquals(
        SampleUnitValidationErrorDTO.ValidationError.MISSING_COLLECTION_INSTRUMENT,
        sampleUnitValidationErrorDTO.getErrors()[0]);
    assertEquals(sampleUnitRef, sampleUnitValidationErrorDTO.getSampleUnitRef());
  }

  @Test
  public void getValidationErrorsMissingPartyId() {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);
    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    List<SampleLink> sampleLinks = Collections.singletonList(sampleLink);

    UUID sampleUnitId = UUID.randomUUID();
    String sampleUnitRef = "111111";
    SampleUnitDTO sampleUnitDTO = new SampleUnitDTO();
    sampleUnitDTO.setState(SampleUnitDTO.SampleUnitState.FAILED);
    sampleUnitDTO.setCollectionInstrumentId(UUID.randomUUID());
    sampleUnitDTO.setPartyId(null);
    sampleUnitDTO.setId(sampleUnitId.toString());
    sampleUnitDTO.setSampleUnitRef(sampleUnitRef);

    SampleUnitDTO[] sampleUnitDTOs = new SampleUnitDTO[] {sampleUnitDTO};

    // Given
    when(sampleLinkRepo.findByCollectionExerciseId(any())).thenReturn(sampleLinks);
    when(sampleSvcClient.requestSampleUnitsForSampleSummary(sampleSummaryId, true))
        .thenReturn(sampleUnitDTOs);

    // when
    SampleUnitValidationErrorDTO[] validationErrors =
        sampleService.getValidationErrors(collectionExercise);

    // then
    assertEquals(validationErrors.length, 1);
    SampleUnitValidationErrorDTO sampleUnitValidationErrorDTO = validationErrors[0];
    assertEquals(
        SampleUnitValidationErrorDTO.ValidationError.MISSING_PARTY,
        sampleUnitValidationErrorDTO.getErrors()[0]);
    assertEquals(sampleUnitRef, sampleUnitValidationErrorDTO.getSampleUnitRef());
  }

  @Test
  public void getValidationErrorsNone() {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);
    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    List<SampleLink> sampleLinks = Collections.singletonList(sampleLink);

    UUID sampleUnitId = UUID.randomUUID();
    String sampleUnitRef = "111111";
    SampleUnitDTO sampleUnitDTO = new SampleUnitDTO();
    sampleUnitDTO.setState(SampleUnitDTO.SampleUnitState.DELIVERED);
    sampleUnitDTO.setCollectionInstrumentId(UUID.randomUUID());
    sampleUnitDTO.setPartyId(UUID.randomUUID());
    sampleUnitDTO.setId(sampleUnitId.toString());
    sampleUnitDTO.setSampleUnitRef(sampleUnitRef);

    SampleUnitDTO[] sampleUnitDTOs = new SampleUnitDTO[] {sampleUnitDTO};

    // Given
    when(sampleLinkRepo.findByCollectionExerciseId(any())).thenReturn(sampleLinks);
    when(sampleSvcClient.requestSampleUnitsForSampleSummary(sampleSummaryId, true))
        .thenReturn(sampleUnitDTOs);

    // when
    SampleUnitValidationErrorDTO[] validationErrors =
        sampleService.getValidationErrors(collectionExercise);

    // then
    assertEquals(validationErrors.length, 0);
  }

  @Test
  public void getValidationErrorsNoSampleSummaryLink() {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);

    UUID sampleUnitId = UUID.randomUUID();
    String sampleUnitRef = "111111";
    SampleUnitDTO sampleUnitDTO = new SampleUnitDTO();
    sampleUnitDTO.setState(SampleUnitDTO.SampleUnitState.DELIVERED);
    sampleUnitDTO.setCollectionInstrumentId(UUID.randomUUID());
    sampleUnitDTO.setPartyId(UUID.randomUUID());
    sampleUnitDTO.setId(sampleUnitId.toString());
    sampleUnitDTO.setSampleUnitRef(sampleUnitRef);

    SampleUnitDTO[] sampleUnitDTOs = new SampleUnitDTO[] {sampleUnitDTO};

    // Given
    when(sampleLinkRepo.findByCollectionExerciseId(any())).thenReturn(Collections.EMPTY_LIST);

    // when
    SampleUnitValidationErrorDTO[] validationErrors =
        sampleService.getValidationErrors(collectionExercise);

    // then
    assertEquals(validationErrors.length, 0);
  }
}
