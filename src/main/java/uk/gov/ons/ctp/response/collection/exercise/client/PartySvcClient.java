package uk.gov.ons.ctp.response.collection.exercise.client;

import org.springframework.web.client.RestClientException;

import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.party.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/**
 * Service responsible for making client calls to the Party service
 *
 */
public interface PartySvcClient {

  /**
   * Request the delivery of party from the Party Service.
   *
   * @param sampleUnitType the sample unit type for which to request party.
   * @param sampleUnitRef the sample unit ref for which to request party.
   * @return the party object
   * @throws RestClientException something went wrong making http call
   */
  PartyDTO requestParty(SampleUnitDTO.SampleUnitType sampleUnitType, String sampleUnitRef) throws RestClientException;

  SampleLinkDTO linkSampleSummaryId(String sampleSummaryId, String collectionExercise) throws RestClientException;
}
