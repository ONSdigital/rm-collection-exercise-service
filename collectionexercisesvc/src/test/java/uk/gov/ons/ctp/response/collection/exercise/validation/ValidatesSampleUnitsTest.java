package uk.gov.ons.ctp.response.collection.exercise.validation;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.response.collection.exercise.client.impl.PartySvcRestClientImpl;
import uk.gov.ons.ctp.response.collection.exercise.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Tests for the ValidatesSampleTest
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ValidatesSampleUnitsTest {

  @Mock
  private PartySvcRestClientImpl partySvcClient;

  @Test
  public void validateSampleUnits() throws Exception {

    List<PartyDTO> partyJson = FixtureHelper.loadClassFixtures(PartyDTO[].class);

    when(partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, "45297c23-763d-46a9-b4e5-c37ff5b4fbe9")).thenReturn(partyJson.get(0));

  }

}
