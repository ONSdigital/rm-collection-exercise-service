package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

@RunWith(MockitoJUnitRunner.class)
public class SampleUploadedInboundReceiverTest {

  @InjectMocks private SampleUploadedInboundReceiver receiver;

  @Mock private SampleService sampleService;

  @Mock private CollectionExerciseService collectionExerciseService;

  @Test
  public void testTransitionToReadyToReview() throws CTPException {
    // Given
    SampleSummaryDTO sampleSummary = new SampleSummaryDTO();
    sampleSummary.setId(UUID.randomUUID());
    SampleLink sampleLink = new SampleLink();
    sampleLink.setCollectionExerciseId(UUID.randomUUID());
    given(sampleService.getSampleLinksForSummary(sampleSummary.getId()))
        .willReturn(Collections.singletonList(sampleLink));

    // When
    receiver.sampleUploaded(sampleSummary);

    // Then
    verify(collectionExerciseService)
        .transitionScheduleCollectionExerciseToReadyToReview(sampleLink.getCollectionExerciseId());
  }
}
