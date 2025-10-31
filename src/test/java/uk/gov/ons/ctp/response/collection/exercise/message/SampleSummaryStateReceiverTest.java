package uk.gov.ons.ctp.response.collection.exercise.message;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SampleSummaryStatusDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleSummaryService;

@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryStateReceiverTest {

  @Mock private ObjectMapper objectMapper;

  @Mock private SampleSummaryService sampleSummaryService;

  @InjectMocks private SampleSummaryStateReceiver sampleSummaryStateReceiver;

  @Test
  public void messageReceiverEnrich() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();

    SampleSummaryStatusDTO sampleSummaryStatusDTO = new SampleSummaryStatusDTO();
    sampleSummaryStatusDTO.setCollectionExerciseId(collectionExerciseId);
    sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.ENRICHED);
    sampleSummaryStatusDTO.setSuccessful(true);

    ObjectMapper mapper = new ObjectMapper();
    String payload = mapper.writeValueAsString(sampleSummaryStatusDTO);

    Message message = Mockito.mock(Message.class);
    when(message.getPayload()).thenReturn(payload.getBytes());
    when(objectMapper.readValue(payload, SampleSummaryStatusDTO.class))
        .thenReturn(sampleSummaryStatusDTO);

    BasicAcknowledgeablePubsubMessage pubSubMsg =
        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);

    sampleSummaryStateReceiver.messageReceiver(message, pubSubMsg);

    verify(sampleSummaryService, times(1)).sampleSummaryValidated(true, collectionExerciseId);
    verify(pubSubMsg, times(1)).ack();
  }

  @Test
  public void messageReceiverEnrichFails() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();

    SampleSummaryStatusDTO sampleSummaryStatusDTO = new SampleSummaryStatusDTO();
    sampleSummaryStatusDTO.setCollectionExerciseId(collectionExerciseId);
    sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.ENRICHED);
    sampleSummaryStatusDTO.setSuccessful(false);

    ObjectMapper mapper = new ObjectMapper();
    String payload = mapper.writeValueAsString(sampleSummaryStatusDTO);

    Message message = Mockito.mock(Message.class);
    when(message.getPayload()).thenReturn(payload.getBytes());
    when(objectMapper.readValue(payload, SampleSummaryStatusDTO.class))
        .thenReturn(sampleSummaryStatusDTO);

    BasicAcknowledgeablePubsubMessage pubSubMsg =
        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);

    sampleSummaryStateReceiver.messageReceiver(message, pubSubMsg);

    verify(sampleSummaryService, times(1)).sampleSummaryValidated(false, collectionExerciseId);
    verify(pubSubMsg, times(1)).ack();
  }

  @Test
  public void messageReceiverDistributed() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();

    SampleSummaryStatusDTO sampleSummaryStatusDTO = new SampleSummaryStatusDTO();
    sampleSummaryStatusDTO.setCollectionExerciseId(collectionExerciseId);
    sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.DISTRIBUTED);
    sampleSummaryStatusDTO.setSuccessful(true);

    ObjectMapper mapper = new ObjectMapper();
    String payload = mapper.writeValueAsString(sampleSummaryStatusDTO);

    Message message = Mockito.mock(Message.class);
    when(message.getPayload()).thenReturn(payload.getBytes());
    when(objectMapper.readValue(payload, SampleSummaryStatusDTO.class))
        .thenReturn(sampleSummaryStatusDTO);

    BasicAcknowledgeablePubsubMessage pubSubMsg =
        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);

    sampleSummaryStateReceiver.messageReceiver(message, pubSubMsg);

    verify(sampleSummaryService, times(1)).sampleSummaryDistributed(true, collectionExerciseId);
    verify(pubSubMsg, times(1)).ack();
  }

  @Test
  public void messageReceiverDistributedFails() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();

    SampleSummaryStatusDTO sampleSummaryStatusDTO = new SampleSummaryStatusDTO();
    sampleSummaryStatusDTO.setCollectionExerciseId(collectionExerciseId);
    sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.DISTRIBUTED);
    sampleSummaryStatusDTO.setSuccessful(false);

    ObjectMapper mapper = new ObjectMapper();
    String payload = mapper.writeValueAsString(sampleSummaryStatusDTO);

    Message message = Mockito.mock(Message.class);
    when(message.getPayload()).thenReturn(payload.getBytes());
    when(objectMapper.readValue(payload, SampleSummaryStatusDTO.class))
        .thenReturn(sampleSummaryStatusDTO);

    BasicAcknowledgeablePubsubMessage pubSubMsg =
        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);

    sampleSummaryStateReceiver.messageReceiver(message, pubSubMsg);

    verify(sampleSummaryService, times(1)).sampleSummaryDistributed(false, collectionExerciseId);
    verify(pubSubMsg, times(1)).ack();
  }
}
