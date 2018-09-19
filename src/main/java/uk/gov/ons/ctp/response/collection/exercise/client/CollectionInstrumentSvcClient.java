package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
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
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;

/** HTTP RestClient implementation for calls to the Collection Instrument service. */
@Component
public class CollectionInstrumentSvcClient {
  private static final Logger log = LoggerFactory.getLogger(CollectionInstrumentSvcClient.class);

  private AppConfig appConfig;
  private RestTemplate restTemplate;
  private RestUtility restUtility;

  public CollectionInstrumentSvcClient(
    AppConfig appConfig,
    RestTemplate restTemplate,
    @Qualifier("collectionInstrumentRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Request the existing collection instruments
   *
   * @param searchString search string for looking up collection instruments based on classifiers
   * @return list of collection instruments matching the search string
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public List<CollectionInstrumentDTO> requestCollectionInstruments(String searchString) {
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("searchString", searchString);
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getCollectionInstrumentSvc().getRequestCollectionInstruments(), queryParams);

    HttpEntity httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<List<CollectionInstrumentDTO>> response =
        restTemplate.exchange(
          uriComponents.toUri(),
          HttpMethod.GET,
          httpEntity,
          new ParameterizedTypeReference<List<CollectionInstrumentDTO>>() {});

    return response.getBody();
  }

  /**
   * Request the count of existing collection instruments
   *
   * @param searchString search string for looking up collection instruments based on classifiers
   * @return count of collection instruments matching the search string
   */
  public Integer countCollectionInstruments(String searchString) {
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("searchString", searchString);
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getCollectionInstrumentSvc().getRequestCollectionInstrumentsCount(),
            queryParams);

    HttpEntity httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<String> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, String.class);

    return Integer.parseInt(responseEntity.getBody());
  }
}
