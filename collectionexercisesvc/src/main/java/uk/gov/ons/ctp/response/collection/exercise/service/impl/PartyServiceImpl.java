package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.PartyService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/**
 * The implementation of the SampleService
 *
 */
@Service
@Slf4j
public class PartyServiceImpl implements PartyService {

  @Autowired
  private PartySvcClient partySvcClient;

  @Override
  public PartyDTO requestParty(SampleUnitDTO.SampleUnitType sampleUnitType, String sampleUnitRef) {

    return partySvcClient.requestParty(sampleUnitType, sampleUnitRef);
  }

}
