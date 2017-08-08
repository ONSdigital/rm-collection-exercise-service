package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitReceiver;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * Service implementation responsible for receipt of sample units. See Spring
 * Integration flow for details of inbound queue.
 *
 */
@CoverageIgnore
@MessageEndpoint
public class SampleUnitReceiverImpl implements SampleUnitReceiver {

  @Autowired
  private SampleService sampleService;

  // TODO CTPA-1340
  @Override
  @ServiceActivator(inputChannel = "sampleUnitTransformed", adviceChain = "sampleUnitRetryAdvice")
  public void acceptSampleUnit(SampleUnit sampleUnit) throws CTPException {
    sampleService.acceptSampleUnit(sampleUnit);
  }
}
