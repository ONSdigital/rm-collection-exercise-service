package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;

import java.io.IOException;
import java.util.List;

/**
 * HTTP RestClient implementation for calls to the Collection Instrument
 * service.
 *
 */
@Component
@Slf4j
public class CollectionInstrumentSvcRestClientImpl implements CollectionInstrumentSvcClient {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private RestTemplate restTemplate;

  @Qualifier("collectionInstrumentRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Autowired
  private ObjectMapper objectMapper;

  @Retryable(value = {RestClientException.class}, maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public List<CollectionInstrumentDTO> requestCollectionInstruments(String searchString) throws RestClientException {

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("searchString", searchString);

    UriComponents uriComponents = restUtility.createUriComponents(
            appConfig.getCollectionInstrumentSvc().getRequestCollectionInstruments(), queryParams);

    HttpEntity<List<CollectionInstrumentDTO>> httpEntity = restUtility.createHttpEntity(null);

    ResponseEntity<String> responseEntity = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity,
        String.class);

    List<CollectionInstrumentDTO> result = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      String responseBody = responseEntity.getBody();
      try {
        result = objectMapper.readValue(responseBody, new TypeReference<List<CollectionInstrumentDTO>>() { });
      } catch (IOException e) {
        String msg = String.format("cause = %s - message = %s", e.getCause(), e.getMessage());
        log.error(msg);
      }

    }

    return result;
  }
}
