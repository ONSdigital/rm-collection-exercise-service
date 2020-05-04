package uk.gov.ons.ctp.response.collection.exercise.client;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.PartySvc;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtilityConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;

@RunWith(MockitoJUnitRunner.class)
public class PartySvcClientTest {

  private static final String SAMPLE_UNIT_REF = "testRef";
  private static final String SAMPLE_SUMMARY_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @InjectMocks private PartySvcClient partySvcClient;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    PartySvc partySvc = new PartySvc();
    when(appConfig.getPartySvc()).thenReturn(partySvc);
  }

  @Test
  public void createActionPlan_201Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

    // When
    partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, SAMPLE_UNIT_REF);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class));
  }

  @Test(expected = HttpClientErrorException.class)
  public void createActionPlan_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, SAMPLE_UNIT_REF);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class));
  }

  @Test
  public void linkSampleSummaryId_200Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.PUT), any(HttpEntity.class), eq(SampleLinkDTO.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

    // When
    partySvcClient.linkSampleSummaryId(SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(URI.class), eq(HttpMethod.PUT), any(HttpEntity.class), eq(SampleLinkDTO.class));
  }

  @Test(expected = HttpClientErrorException.class)
  public void linkSampleSummaryId() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.PUT), any(HttpEntity.class), eq(SampleLinkDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    partySvcClient.linkSampleSummaryId(SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(URI.class), eq(HttpMethod.PUT), any(HttpEntity.class), eq(SampleLinkDTO.class));
  }
}
