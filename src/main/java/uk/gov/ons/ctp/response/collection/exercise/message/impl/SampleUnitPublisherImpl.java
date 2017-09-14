package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.cobertura.CoverageIgnore;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitParent;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitPublisher;

/**
 * Service implementation responsible for publishing a sampleUnit message to the
 * case service.
 *
 */
@CoverageIgnore
@MessageEndpoint
@Slf4j
public class SampleUnitPublisherImpl implements SampleUnitPublisher {

  @Qualifier("caseRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Override
  public void sendSampleUnit(SampleUnitParent sampleUnit) {
    log.debug("Entering sendSampleUnit for SampleUnitRef {}, SampleUnitType {} ", sampleUnit.getSampleUnitRef(),
        sampleUnit.getSampleUnitType());
    rabbitTemplate.convertAndSend(sampleUnit);
  }

}
