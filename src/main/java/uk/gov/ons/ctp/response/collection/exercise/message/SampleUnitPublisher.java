package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitParentDTO;

/** Service implementation responsible for publishing a sampleUnit message to the case service. */
@Service
public class SampleUnitPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitPublisher.class);

  @Autowired AppConfig appConfig;

  @Autowired private PubSub pubSub;

  @Autowired private ObjectMapper objectMapper;

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
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      Publisher publisher = pubSub.sampleUnitPublisher();
      try {
        log.with("publisher", publisher).info("Publishing message to PubSub");
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        ApiFutures.addCallback(
            messageIdFuture,
            new ApiFutureCallback<>() {
              @Override
              public void onFailure(Throwable throwable) {
                if (throwable instanceof ApiException) {
                  ApiException apiException = ((ApiException) throwable);
                  log.with("error", apiException.getStatusCode().getCode())
                      .error("SampleUnit publish sent failure to PubSub.");
                }
                log.with("message", message).error("Error Publishing PubSub message");
              }

              @Override
              public void onSuccess(String messageId) {
                // Once published, returns server-assigned message ids (unique within the topic)
                log.with("messageId", messageId).info("SampleUnit publish sent successfully");
              }
            },
            MoreExecutors.directExecutor());
      } finally {
        publisher.shutdown();
        pubSub.shutdown();
      }
    } catch (JsonProcessingException e) {
      log.with("sampleUnit", sampleUnit).error("Error while sampleUnit can not be parsed.");
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("PubSub Error while processing sample unit distribution", e);
      throw new RuntimeException(e);
    }
  }
}
