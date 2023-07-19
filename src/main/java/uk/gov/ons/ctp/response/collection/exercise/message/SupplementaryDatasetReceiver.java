package uk.gov.ons.ctp.response.collection.exercise.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;

@Component
public class SupplementaryDatasetReceiver {
  private static final Logger log = LoggerFactory.getLogger(SupplementaryDatasetReceiver.class);

  @Autowired private ObjectMapper objectMapper;

  @ServiceActivator(inputChannel = "supplementaryDataServiceMessageChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg) {
    log.info(
        "Receiving message ID from PubSub {}",
        kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()));
    String payload = new String((byte[]) message.getPayload());
    log.with("payload", payload).info("New message from Supplementary Data Service");
    try {
      log.info("Mapping payload to Supplementary Dataset object");
      SupplementaryDatasetDTO supplementaryDatasetDTO =
          objectMapper.readValue(payload, SupplementaryDatasetDTO.class);
      log.info("Mapping to Supplementary Dataset object successful {}", supplementaryDatasetDTO);
      pubSubMsg.ack();
    } catch (JsonProcessingException e) {
      log.with(e)
          .error(
              "Error processing message from Supplementary Data Service",
              e);
      pubSubMsg.nack();
    }
  }
}
