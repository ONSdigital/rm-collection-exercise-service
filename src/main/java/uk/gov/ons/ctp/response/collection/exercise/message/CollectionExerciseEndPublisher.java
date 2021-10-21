package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionExerciseEndEventDTO;

@Component
public class CollectionExerciseEndPublisher {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CollectionExerciseEndPublisher.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CollectionExerciseApplication.CollectionExerciseEndOutboundGateway messagingGateway;

  public void sendCollectionExerciseEnd(UUID collectionExerciseId) {
    CollectionExerciseEndEventDTO collectionExerciseEndEventDTO =
        new CollectionExerciseEndEventDTO();
    collectionExerciseEndEventDTO.setCollectionExerciseId(collectionExerciseId);

    try {
      String payload = objectMapper.writeValueAsString(collectionExerciseEndEventDTO);
      messagingGateway.sendToPubsub(payload);
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialise collection exercise end event", e);
    }
  }
}
