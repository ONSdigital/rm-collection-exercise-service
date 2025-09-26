package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SampleSummaryStatusDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleSummaryService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleSummaryValidationException;
import uk.gov.ons.ctp.response.collection.exercise.service.change.SampleSummaryDistributionException;

@Component
public class SampleSummaryStateReceiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleSummaryStateReceiver.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SampleSummaryService sampleSummaryService;

  @ServiceActivator(inputChannel = "sampleSummaryStatusChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg) {

    UUID collectionExerciseId = null;
    try {
      String payload = new String((byte[]) message.getPayload());
      SampleSummaryStatusDTO sampleSummaryStatusDTO =
          objectMapper.readValue(payload, SampleSummaryStatusDTO.class);
      collectionExerciseId = sampleSummaryStatusDTO.getCollectionExerciseId();
      if (SampleSummaryStatusDTO.Event.ENRICHED.equals(sampleSummaryStatusDTO.getEvent())) {
        sampleSummaryService.sampleSummaryValidated(
            sampleSummaryStatusDTO.isSuccessful(), collectionExerciseId);
        pubSubMsg.ack();
      } else if (SampleSummaryStatusDTO.Event.DISTRIBUTED.equals(
          sampleSummaryStatusDTO.getEvent())) {
        sampleSummaryService.sampleSummaryDistributed(
            sampleSummaryStatusDTO.isSuccessful(), collectionExerciseId);
        pubSubMsg.ack();
      } else {
        LOGGER.error("unsupported message type");
        pubSubMsg.nack();
      }
    } catch (SampleSummaryValidationException | SampleSummaryDistributionException e) {
      pubSubMsg.nack();
    } catch (JsonProcessingException e) {
      LOGGER.error("failed to deserialize message type", e);
      pubSubMsg.nack();
      return;
    }
  }
}
