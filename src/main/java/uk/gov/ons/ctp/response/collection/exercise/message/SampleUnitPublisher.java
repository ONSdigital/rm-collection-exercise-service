package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitParentDTO;

/** Service implementation responsible for publishing a sampleUnit message to the case service. */
@Service
public class SampleUnitPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitPublisher.class);

  @Autowired AppConfig appConfig;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CollectionExerciseApplication.PubSubOutboundSampleUnitGroupGateway publisher;

  /**
   * To publish a SampleUnitGroup
   *
   * @param sampleUnit the SampleUnitGroup message to publish.
   */
  public void sendSampleUnit(SampleUnitParentDTO sampleUnit) {
    log.with("sample_unit_ref", sampleUnit.getSampleUnitRef())
        .with("sample_unit_type", sampleUnit.getSampleUnitType())
        .debug("Entering sendSampleUnit");
    try {
      String message = objectMapper.writeValueAsString(sampleUnit);
      log.with("collectionExerciseId", sampleUnit.getCollectionExerciseId())
          .info("Publishing message to PubSub");
      publisher.sendToPubSub(message);
      log.with("collectionExerciseId", sampleUnit.getCollectionExerciseId())
          .info("SampleUnit publish sent successfully");
    } catch (JsonProcessingException e) {
      log.with("sampleUnit", sampleUnit).error("Error while sampleUnit can not be parsed.");
      throw new RuntimeException(e);
    }
  }
}
