package uk.gov.ons.ctp.response.collection.exercise.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.collection.exercise.lib.casesvc.message.sampleunitnotification.SampleUnitParent;

/** Service implementation responsible for publishing a sampleUnit message to the case service. */
@CoverageIgnore
@MessageEndpoint
public class SampleUnitPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitPublisher.class);

  private RabbitTemplate rabbitTemplate;

  public SampleUnitPublisher(@Qualifier("caseRabbitTemplate") RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * To publish a SampleUnitGroup
   *
   * @param sampleUnit the SampleUnitGroup message to publish.
   */
  public void sendSampleUnit(SampleUnitParent sampleUnit) {
    log.with("sample_unit_ref", sampleUnit.getSampleUnitRef())
        .with("sample_unit_type", sampleUnit.getSampleUnitType())
        .debug("Entering sendSampleUnit");
    rabbitTemplate.convertAndSend(sampleUnit);
  }
}
