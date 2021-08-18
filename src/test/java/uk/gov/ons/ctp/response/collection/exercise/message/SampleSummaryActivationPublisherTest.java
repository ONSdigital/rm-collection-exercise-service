package uk.gov.ons.ctp.response.collection.exercise.message;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleSummaryActivationDTO;

@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryActivationPublisherTest {

  @Mock private CollectionExerciseApplication.PubsubOutboundGateway messagingGateway;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks SampleSummaryActivationPublisher sampleSummaryActivationPublisher;

  @Test
  public void testSendSampleSummaryActivation() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID surveyId = UUID.randomUUID();

    ObjectMapper mapper = new ObjectMapper();
    String payload =
        mapper.writeValueAsString(
            new SampleSummaryActivationDTO(collectionExerciseId, sampleSummaryId, surveyId));
    when(objectMapper.writeValueAsString(any(SampleSummaryActivationDTO.class)))
        .thenReturn(payload);

    sampleSummaryActivationPublisher.sendSampleSummaryActivation(
        collectionExerciseId, sampleSummaryId, surveyId);

    verify(objectMapper, times(1)).writeValueAsString(any(SampleSummaryActivationDTO.class));
    verify(messagingGateway, times(1)).sendToPubsub(payload);
  }
}
