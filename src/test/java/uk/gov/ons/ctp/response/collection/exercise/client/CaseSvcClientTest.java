package uk.gov.ons.ctp.response.collection.exercise.client;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.CaseSvc;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtilityConfig;

@RunWith(MockitoJUnitRunner.class)
public class CaseSvcClientTest {

  private static final String EVENT_TAG = "mps";
  private static final String COLLECTION_EXERCISE_ID = "f52da9e6-73de-4aa6-a49b-e5de9d1c18e1";
  private static final UUID COLLECTION_EXERCISE_UUID = UUID.fromString(COLLECTION_EXERCISE_ID);

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @InjectMocks private CaseSvcClient caseSvcClient;

  private ResponseEntity responseEntity;

  @Before
  public void setUp() {

    responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);

    MockitoAnnotations.initMocks(this);

    CaseSvc caseSvc = new CaseSvc();
    caseSvc.setProcessEventPath("test:path");
    when(appConfig.getCaseSvc()).thenReturn(caseSvc);
  }

  @Test
  public void processEvent_204Response() {
    // Given
    when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), any()))
        .thenReturn(responseEntity);

    // When
    boolean result = caseSvcClient.processEvent(EVENT_TAG, COLLECTION_EXERCISE_UUID);

    // Then
    verify(restTemplate, times(1)).postForEntity(any(URI.class), any(HttpEntity.class), any(null));
    assertTrue(result);
  }

  @Test(expected = HttpClientErrorException.class)
  public void processEvent_400Response() {
    // Given
    when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    // When
    boolean result = caseSvcClient.processEvent(EVENT_TAG, COLLECTION_EXERCISE_UUID);

    // Then
    verify(restTemplate, times(1)).postForEntity(any(URI.class), any(HttpEntity.class), any(null));
  }

  @Test(expected = HttpClientErrorException.class)
  public void processEvent_500Response() {
    // Given
    when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    caseSvcClient.processEvent(EVENT_TAG, COLLECTION_EXERCISE_UUID);

    // Then
    verify(restTemplate, times(1)).postForEntity(any(URI.class), any(HttpEntity.class), any(null));
  }
}
