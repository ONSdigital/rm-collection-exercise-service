package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitValidationErrorDTO;

/** Unit tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleServiceTest {

  @Mock private SampleLinkRepository sampleLinkRepo;

  @Mock private SampleSvcClient sampleSvcClient;

  @InjectMocks private SampleService sampleService;

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
