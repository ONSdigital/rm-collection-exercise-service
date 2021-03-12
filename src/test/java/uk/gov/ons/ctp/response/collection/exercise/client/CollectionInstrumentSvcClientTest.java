package uk.gov.ons.ctp.response.collection.exercise.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.CollectionInstrumentSvc;
import uk.gov.ons.ctp.response.collection.exercise.lib.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtilityConfig;

@RunWith(MockitoJUnitRunner.class)
public class CollectionInstrumentSvcClientTest {

  private static final String SEARCH_STRING = "testSearchString";
  private static final String TEN = "10";
  private static final Integer TEN_INT = 10;

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());
  @Mock private RestTemplate restTemplate;
  @Mock private AppConfig appConfig;

  @InjectMocks private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  private List<CollectionInstrumentDTO> collectionInstruments;

  @Before
  public void setUp() {
    collectionInstruments =
        Arrays.asList(new CollectionInstrumentDTO(), new CollectionInstrumentDTO());

    MockitoAnnotations.initMocks(this);
    CollectionInstrumentSvc collectionInstrumentSvc = new CollectionInstrumentSvc();
    when(appConfig.getCollectionInstrumentSvc()).thenReturn(collectionInstrumentSvc);
  }

  @Test
  public void getCollectionInstruments_200Response() {
    // Given
    ResponseEntity<List<CollectionInstrumentDTO>> responseEntity =
        new ResponseEntity<>(collectionInstruments, HttpStatus.CREATED);
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<CollectionInstrumentDTO>>() {})))
        .thenReturn(responseEntity);

    // When
    collectionInstrumentSvcClient.requestCollectionInstruments(SEARCH_STRING);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<CollectionInstrumentDTO>>() {}));
  }

  @Test(expected = HttpClientErrorException.class)
  public void getCollectionInstruments_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<CollectionInstrumentDTO>>() {})))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    collectionInstrumentSvcClient.requestCollectionInstruments(SEARCH_STRING);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<CollectionInstrumentDTO>>() {}));
  }

  @Test
  public void countCollectionInstruments_200Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(TEN, HttpStatus.OK));

    // When
    Integer count = collectionInstrumentSvcClient.countCollectionInstruments(SEARCH_STRING);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    assertEquals(TEN_INT, count);
  }

  @Test(expected = HttpClientErrorException.class)
  public void countCollectionInstruments_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    collectionInstrumentSvcClient.countCollectionInstruments(SEARCH_STRING);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
  }
}
