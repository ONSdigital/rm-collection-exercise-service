package uk.gov.ons.ctp.response.collection.exercise.message;

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
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SampleSummaryStatusDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleSummaryService;

@Component
public class SampleSummaryStateReceiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleSummaryStateReceiver.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SampleSummaryService sampleSummaryService;

  @ServiceActivator(inputChannel = "sampleSummaryStatusChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage gcpMessage) {

    String payload = new String((byte[]) message.getPayload());
    SampleSummaryStatusDTO sampleSummaryStatusDTO = null;
    try {
      sampleSummaryStatusDTO = objectMapper.readValue(payload, SampleSummaryStatusDTO.class);
    } catch (JsonProcessingException e) {
      LOGGER.error("failed to deserialize message type", e);
      gcpMessage.nack();
    }

    if (SampleSummaryStatusDTO.Event.ENRICHED.equals(sampleSummaryStatusDTO.getEvent())) {
      boolean enrich =
          sampleSummaryService.validSample(
              sampleSummaryStatusDTO.isSuccessful(),
              sampleSummaryStatusDTO.getCollectionExerciseId());
      if (enrich) {
        LOGGER
            .with("collectionExerciseId", sampleSummaryStatusDTO.getCollectionExerciseId())
            .info("collection exercise transition to valid successful");
        gcpMessage.ack();
      } else {
        LOGGER
            .with("collectionExerciseId", sampleSummaryStatusDTO.getCollectionExerciseId())
            .info("collection exercise transition to valid failed");
        gcpMessage.nack();
      }
    } else if (SampleSummaryStatusDTO.Event.DISTRIBUTED.equals(sampleSummaryStatusDTO.getEvent())) {
      boolean distributed =
          sampleSummaryService.sampleSummaryDistributed(
              sampleSummaryStatusDTO.isSuccessful(),
              sampleSummaryStatusDTO.getCollectionExerciseId());
      if (distributed) {
        LOGGER
            .with("collectionExerciseId", sampleSummaryStatusDTO.getCollectionExerciseId())
            .info("collection exercise transition successful");
        gcpMessage.ack();
      } else {
        LOGGER
            .with("collectionExerciseId", sampleSummaryStatusDTO.getCollectionExerciseId())
            .info("collection exercise transition failed");
        gcpMessage.nack();
      }
    } else {
      LOGGER.error("unsupported message type");
      gcpMessage.nack();
    }
  }
}
