package uk.gov.ons.ctp.response.collection.exercise.message;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.messaging.Message;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDataServiceDTO;

@RunWith(MockitoJUnitRunner.class)
public class SupplementaryDataServiceReceiverTest {
  @Mock private ObjectMapper objectMapper;
  @InjectMocks private SupplementaryDataServiceReceiver supplementaryDataServiceReceiver;

  @Test
  public void testMessageAcked() throws Exception {
    UUID datasetId = UUID.randomUUID();
    String surveyId = "221";
    String periodId = "2023";
    List<String> formTypes = Arrays.asList("0017", "0023", "0099");

    SupplementaryDataServiceDTO supplementaryDataServiceDTO = new SupplementaryDataServiceDTO();

    supplementaryDataServiceDTO.setDatasetId(datasetId);
    supplementaryDataServiceDTO.setSurveyId(surveyId);
    supplementaryDataServiceDTO.setPeriodId(periodId);
    supplementaryDataServiceDTO.setFormTypes(formTypes);

    ObjectMapper mapper = new ObjectMapper();
    String payload = mapper.writeValueAsString(supplementaryDataServiceDTO);

    Message message = Mockito.mock(Message.class);

    when(message.getPayload()).thenReturn(payload.getBytes());
    when(objectMapper.readValue(payload, SupplementaryDataServiceDTO.class))
        .thenReturn(supplementaryDataServiceDTO);

    BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage =
        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);

    PubsubMessage pubsubMessage = Mockito.mock(PubsubMessage.class);

    String messageId = "123";
    when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(pubsubMessage);
    when(basicAcknowledgeablePubsubMessage.getPubsubMessage().getMessageId()).thenReturn(messageId);

    supplementaryDataServiceReceiver.messageReceiver(message, basicAcknowledgeablePubsubMessage);

    verify(basicAcknowledgeablePubsubMessage, times(1)).ack();
  }

  @Test
  public void testMessageNacked() throws Exception {
    UUID datasetId = UUID.randomUUID();
    String surveyId = "221";
    String periodId = "2023";
    List<String> formTypes = Arrays.asList("0017", "0023", "0099");

    SupplementaryDataServiceDTO supplementaryDataServiceDTO = new SupplementaryDataServiceDTO();

    supplementaryDataServiceDTO.setDatasetId(datasetId);
    supplementaryDataServiceDTO.setSurveyId(surveyId);
    supplementaryDataServiceDTO.setPeriodId(periodId);
    supplementaryDataServiceDTO.setFormTypes(formTypes);

    ObjectMapper mapper = new ObjectMapper();
    String payload = mapper.writeValueAsString(supplementaryDataServiceDTO);

    Message message = Mockito.mock(Message.class);

    when(message.getPayload()).thenReturn(payload.getBytes());
    when(objectMapper.readValue(payload, SupplementaryDataServiceDTO.class))
        .thenThrow(new JsonProcessingException("Error occurred") {});

    BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage =
        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);

    PubsubMessage pubsubMessage = Mockito.mock(PubsubMessage.class);

    String messageId = "123";
    when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(pubsubMessage);
    when(basicAcknowledgeablePubsubMessage.getPubsubMessage().getMessageId()).thenReturn(messageId);

    supplementaryDataServiceReceiver.messageReceiver(message, basicAcknowledgeablePubsubMessage);

    verify(basicAcknowledgeablePubsubMessage, times(1)).nack();
  }
}
