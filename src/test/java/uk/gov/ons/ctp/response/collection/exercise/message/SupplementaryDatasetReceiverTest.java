// package uk.gov.ons.ctp.response.collection.exercise.message;
//
// import static org.mockito.Mockito.*;
//
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.google.pubsub.v1.PubsubMessage;
// import java.util.Arrays;
// import java.util.List;
// import java.util.UUID;
// import java.util.stream.Collectors;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.mockito.junit.MockitoJUnitRunner;
// import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
// import org.springframework.messaging.Message;
// import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;
// import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
// import uk.gov.ons.ctp.response.collection.exercise.service.SupplementaryDatasetService;
//
// @RunWith(MockitoJUnitRunner.class)
// public class SupplementaryDatasetReceiverTest {
//  @Mock private ObjectMapper objectMapper;
//  @InjectMocks private SupplementaryDatasetReceiver supplementaryDatasetReceiver;
//  @Mock private SupplementaryDatasetService supplementaryDatasetService;
//
//  @Test
//  public void testMessageAcked() throws Exception {
//    UUID datasetId = UUID.randomUUID();
//    String surveyId = "221";
//    String periodId = "2023";
//    List<String> formTypes = Arrays.asList("0017", "0023", "0099");
//
//    SupplementaryDatasetDTO supplementaryDatasetDTO = new SupplementaryDatasetDTO();
//
//    supplementaryDatasetDTO.setDatasetId(datasetId);
//    supplementaryDatasetDTO.setSurveyId(surveyId);
//    supplementaryDatasetDTO.setPeriodId(periodId);
//    supplementaryDatasetDTO.setFormTypes(formTypes);
//
//    SupplementaryDatasetEntity supplementaryDatasetEntity = new SupplementaryDatasetEntity();
//
//    supplementaryDatasetEntity.setId(1);
//    supplementaryDatasetEntity.setSupplementaryDatasetId(datasetId);
//    supplementaryDatasetEntity.setFormTypes(
//        supplementaryDatasetDTO
//            .getFormTypes()
//            .stream()
//            .distinct()
//            .collect(Collectors.toMap(s -> s, s -> s)));
//
//    ObjectMapper mapper = new ObjectMapper();
//    String payload = mapper.writeValueAsString(supplementaryDatasetDTO);
//
//    Message message = Mockito.mock(Message.class);
//
//    when(message.getPayload()).thenReturn(payload.getBytes());
//    when(objectMapper.readValue(payload, SupplementaryDatasetDTO.class))
//        .thenReturn(supplementaryDatasetDTO);
//
//    BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage =
//        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);
//
//    PubsubMessage pubsubMessage = Mockito.mock(PubsubMessage.class);
//
//    String messageId = "123";
//    when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(pubsubMessage);
//
//
// when(basicAcknowledgeablePubsubMessage.getPubsubMessage().getMessageId()).thenReturn(messageId);
//
//    when(supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO))
//        .thenReturn(supplementaryDatasetEntity);
//
//    supplementaryDatasetReceiver.messageReceiver(message, basicAcknowledgeablePubsubMessage);
//
//    verify(basicAcknowledgeablePubsubMessage, times(1)).ack();
//  }
//
//  @Test
//  public void testMessageNacked() throws Exception {
//    UUID datasetId = UUID.randomUUID();
//    String surveyId = "221";
//    String periodId = "2023";
//    List<String> formTypes = Arrays.asList("0017", "0023", "0099");
//
//    SupplementaryDatasetDTO supplementaryDatasetDTO = new SupplementaryDatasetDTO();
//
//    supplementaryDatasetDTO.setDatasetId(datasetId);
//    supplementaryDatasetDTO.setSurveyId(surveyId);
//    supplementaryDatasetDTO.setPeriodId(periodId);
//    supplementaryDatasetDTO.setFormTypes(formTypes);
//
//    ObjectMapper mapper = new ObjectMapper();
//    String payload = mapper.writeValueAsString(supplementaryDatasetDTO);
//
//    Message message = Mockito.mock(Message.class);
//
//    when(message.getPayload()).thenReturn(payload.getBytes());
//    when(objectMapper.readValue(payload, SupplementaryDatasetDTO.class))
//        .thenThrow(new JsonProcessingException("Error occurred") {});
//
//    BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage =
//        Mockito.mock(BasicAcknowledgeablePubsubMessage.class);
//
//    PubsubMessage pubsubMessage = Mockito.mock(PubsubMessage.class);
//
//    String messageId = "123";
//    when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(pubsubMessage);
//
//
// when(basicAcknowledgeablePubsubMessage.getPubsubMessage().getMessageId()).thenReturn(messageId);
//
//    supplementaryDatasetReceiver.messageReceiver(message, basicAcknowledgeablePubsubMessage);
//
//    verify(basicAcknowledgeablePubsubMessage, times(1)).nack();
//  }
// }
