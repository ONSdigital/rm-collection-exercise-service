package uk.gov.ons.ctp.response.collection.exercise.message;

import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitParent;

/**
 * Service responsible for publishing  Sample Units to the case service.
 *
 */
public interface SampleUnitPublisher {

  /**
   * To publish a SampleUnitGroup
   *
   * @param sampleUnit the SampleUnitGroup message to publish.
   */
  void sendSampleUnit(SampleUnitParent sampleUnit);
}
