package uk.gov.ons.ctp.response.collection.exercise.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CaseActionEventStatusDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@Component
public class CaseActionEventStatusReceiver {
  private static final Logger log = LoggerFactory.getLogger(CaseActionEventStatusReceiver.class);
  @Autowired private ObjectMapper objectMapper;
  @Autowired private EventService eventService;

  @ServiceActivator(inputChannel = "collectionExerciseEventStatusUpdateChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg) {
    log.info(
        "Receiving message ID from PubSub",
        kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()));
    String payload = new String((byte[]) message.getPayload());
    log.with("payload", payload).info("New request to update case action event status");
    try {
      log.info("Mapping payload to CaseActionEventStatus object");
      CaseActionEventStatusDTO eventStatus =
          objectMapper.readValue(payload, CaseActionEventStatusDTO.class);
      log.info("Mapping successful, accepting action case notification");
      eventService.updateEventStatus(eventStatus);
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
