package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitReceiver;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * Service implementation responsible for receipt of sample units. See Spring
 * Integration flow for details of inbound queue.
 *
 */
@MessageEndpoint
public class SampleUnitReceiverImpl implements SampleUnitReceiver {

  @Autowired
  private SampleService sampleService;

  @Override
  @ServiceActivator(inputChannel = "sampleUnitTransformed", adviceChain = "sampleUnitRetryAdvice")
  public void acceptSampleUnit(SampleUnit sampleUnit) {
    sampleService.acceptSampleUnit(sampleUnit);
  }
}
