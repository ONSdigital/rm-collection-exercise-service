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
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDataServiceDTO;

@Component
public class SupplementaryDataServiceReceiver {
  private static final Logger log = LoggerFactory.getLogger(SupplementaryDataServiceReceiver.class);

  @Autowired private ObjectMapper objectMapper;

  @ServiceActivator(inputChannel = "supplementaryDataServiceMessageChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg) {
    log.info(
        "Receiving message ID from PubSub {}",
        kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()));
    String payload = new String((byte[]) message.getPayload());
    log.with("payload", payload).info("New request for Supplementary Data Service");
    try {
      log.info("Mapping payload to Supplementary Data Service object");
      SupplementaryDataServiceDTO supplementaryDataServiceDTO =
          objectMapper.readValue(payload, SupplementaryDataServiceDTO.class);
      log.info("Mapping successful {}", supplementaryDataServiceDTO);
      pubSubMsg.ack();
    } catch (JsonProcessingException e) {
      log.with(e)
          .error(
              "Something went wrong while processing message received from PubSub for updating action event status",
              e);
      pubSubMsg.nack();
    }
  }
}
