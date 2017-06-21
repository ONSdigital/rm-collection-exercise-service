package uk.gov.ons.ctp.response.collection.exercise.client;

import uk.gov.ons.ctp.response.collection.exercise.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/**
 * Service responsible for making client calls to the Survey service
 *
 */
public interface PartySvcClient {

  /**
   * Request the delivery of party from the Party Service.
   *
   * @param sampleUnitType the sample unit type for which to request party.
   * @param sampleUnitRef the sample unit ref for which to request party.
   * @return the party object
   */
  PartyDTO requestParty(final SampleUnitDTO.SampleUnitType sampleUnitType, String sampleUnitRef);

}
