package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.response.collection.exercise.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/**
 * Service responsible for dealing with parties
 *
 */
public interface PartyService {

  /**
   * Request the delivery of party from the Party Service.
   *
   * @param sampleUnitType the sample unit type for which to request party.
   * @param sampleUnitRef the sample unit ref for which to request party.
   * @return the party object
   */
  PartyDTO requestParty(final SampleUnitDTO.SampleUnitType sampleUnitType, String sampleUnitRef);

}
