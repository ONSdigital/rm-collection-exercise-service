package uk.gov.ons.ctp.response.collection.exercise.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SupplementaryDatasetService;

@Component
public class SupplementaryDatasetReceiver {
  private static final Logger log = LoggerFactory.getLogger(SupplementaryDatasetReceiver.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SupplementaryDatasetService supplementaryDatasetService;

  @ServiceActivator(inputChannel = "supplementaryDataServiceMessageChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg) {
    log.info(
        "Receiving message ID from PubSub {}",
        kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()));
    String payload = new String((byte[]) message.getPayload());
    log.info("New message from Supplementary Data Service", kv("payload", payload));
    try {
      log.info("Mapping payload to Supplementary Dataset object");
      SupplementaryDatasetDTO supplementaryDatasetDTO = createSupplementaryDatasetDTO(payload);
      supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO);
      pubSubMsg.ack();
    } catch (CTPException e) {
      log.error("Error processing message from Supplementary Dataset Service", e);
      pubSubMsg.nack();
    }
  }

  private SupplementaryDatasetDTO createSupplementaryDatasetDTO(String payload)
      throws CTPException {
    SupplementaryDatasetDTO supplementaryDatasetDTO;
    try {
      supplementaryDatasetDTO = objectMapper.readValue(payload, SupplementaryDatasetDTO.class);
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Could not map message to Supplementary Dataset DTO");
    }
    log.info("Mapping to Supplementary Dataset object successful");
    return supplementaryDatasetDTO;
  }
}
