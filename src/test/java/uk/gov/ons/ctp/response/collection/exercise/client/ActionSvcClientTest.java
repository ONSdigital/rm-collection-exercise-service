package uk.gov.ons.ctp.response.collection.exercise.client;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtilityConfig;

@RunWith(MockitoJUnitRunner.class)
public class ActionSvcClientTest {

  private static final String EVENT_TAG = "mps";
  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final UUID COLLECTION_EXERCISE_UUID = UUID.fromString(COLLECTION_EXERCISE_ID);

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @InjectMocks private ActionSvcClient actionSvcRestClient;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    ActionSvc actionSvc = new ActionSvc();
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
  }

  @Test
  public void processEvent_204Response() {
    // Given
    ResponseEntity responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
    when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), any()))
        .thenReturn(responseEntity);

    // When
    boolean result = actionSvcRestClient.processEvent(EVENT_TAG, COLLECTION_EXERCISE_UUID);

    // Then
    verify(restTemplate, times(1)).postForEntity(any(URI.class), any(HttpEntity.class), any());
    assertTrue(result);
  }

  @Test(expected = HttpClientErrorException.class)
  public void processEvent_400Response() {
    // Given
    when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    // When
    boolean result = actionSvcRestClient.processEvent(EVENT_TAG, COLLECTION_EXERCISE_UUID);

    // Then
    verify(restTemplate, times(1)).postForEntity(any(URI.class), any(HttpEntity.class), any(null));
  }

  @Test(expected = HttpClientErrorException.class)
  public void processEvent_500Response() {
    // Given
    when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    actionSvcRestClient.processEvent(EVENT_TAG, COLLECTION_EXERCISE_UUID);

    // Then
    verify(restTemplate, times(1)).postForEntity(any(URI.class), any(HttpEntity.class), any(null));
  }
}
