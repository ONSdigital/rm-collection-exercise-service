package uk.gov.ons.ctp.response.collection.exercise.message;

import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * Interface for the receipt of sample unit messages from the Spring Integration
 * inbound message queue
 */
public interface SampleUnitReceiver {

  /**
   * Method called with the deserialised message
   *
   * @param sampleUnit The java representation of the message body
   */
  void acceptSampleUnit(SampleUnit sampleUnit);

}
