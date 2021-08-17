package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleSummaryActivationDTO;

@Component
public class SampleSummaryActivationPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleSummaryActivationDTO.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CollectionExerciseApplication.PubsubOutboundGateway messagingGateway;

  public void sendSampleSummaryActivation(SampleSummaryActivationDTO sampleSummaryActivationDTO) {
    try {
      String payload = objectMapper.writeValueAsString(sampleSummaryActivationDTO);
      messagingGateway.sendToPubsub(payload);
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialise sample summary activation", e);
    }
  }
}
