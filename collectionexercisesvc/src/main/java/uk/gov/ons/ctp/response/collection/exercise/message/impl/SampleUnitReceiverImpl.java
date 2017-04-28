package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import org.springframework.integration.annotation.ServiceActivator;

import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitReceiver;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * Service implementation responsible for receipt of sample units.
 * See Spring Integration flow for details of inbound queue.
 *
 */
public class SampleUnitReceiverImpl implements SampleUnitReceiver {

  @Override
  @ServiceActivator(inputChannel = "sampleUnitTransformed", adviceChain = "sampleUnitRetryAdvice")
  public void acceptSampleUnit(SampleUnit sampleUnit) {
    
  }
}
