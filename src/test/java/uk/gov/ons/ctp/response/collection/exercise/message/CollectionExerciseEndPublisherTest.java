package uk.gov.ons.ctp.response.collection.exercise.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication.PubsubOutboundGateway;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionExerciseEndEventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleSummaryActivationDTO;

@RunWith(MockitoJUnitRunner.class)
public class CollectionExerciseEndPublisherTest {

  @Mock private PubsubOutboundGateway messagingGateway;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks CollectionExerciseEndPublisher collectionExerciseEndPublisher;

  @Test
  public void testSendCollectionExerciseEnd() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();

    ObjectMapper mapper = new ObjectMapper();
    String payload =
        mapper.writeValueAsString(new CollectionExerciseEndEventDTO(collectionExerciseId));
    when(objectMapper.writeValueAsString(any(CollectionExerciseDTO.class))).thenReturn(payload);

    collectionExerciseEndPublisher.sendCollectionExerciseEnd(collectionExerciseId);

    verify(objectMapper, times(1)).writeValueAsString(any(SampleSummaryActivationDTO.class));
    verify(messagingGateway, times(1)).sendToPubsub(payload);
  }
}
