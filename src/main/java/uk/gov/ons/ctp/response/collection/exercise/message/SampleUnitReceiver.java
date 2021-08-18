package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.sampleunit.definition.SampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/** PubSub subscription responsible for receipt of sample units via PubSub. */
@Component
public class SampleUnitReceiver {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitReceiver.class);
  @Autowired private ObjectMapper objectMapper;
  @Autowired private SampleService sampleService;
  @Autowired AppConfig appConfig;

  @ServiceActivator(inputChannel = "sampleUnitReceiverChannel")
  public void messageReceiver(Message message) {
    String payload = new String((byte[]) message.getPayload());
    log.with("payload", payload).info("New request for sample");
    try {
      log.info("Mapping payload to SampleUnit object");
      SampleUnit sampleUnit = objectMapper.readValue(payload, SampleUnit.class);
      log.info("Mapping successful, accepting sampleUnit");
      sampleService.acceptSampleUnit(sampleUnit);
    } catch (final IOException e) {
      log.with(e)
          .error(
              "Something went wrong while processing message received from PubSub "
                  + "for sample unit receivable");
    }
  }
}
