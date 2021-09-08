package uk.gov.ons.ctp.response.collection.exercise.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;

/** HTTP RestClient implementation for calls to the Case service. */
@Component
public class CaseSvcClient {
  private static final Logger log = LoggerFactory.getLogger(CaseSvcClient.class);

  private AppConfig appConfig;
  private RestTemplate restTemplate;
  private RestUtility restUtility;
  @Autowired private ObjectMapper objectMapper;

  public CaseSvcClient(
      AppConfig appConfig,
      final RestTemplate restTemplate,
      final @Qualifier("caseRestUtility") RestUtility restUtility,
      final ObjectMapper objectMapper) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
    this.objectMapper = objectMapper;
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public Long getNumberOfCases(final UUID collectionExerciseId) {
    final UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getCaseSvc().getNumberOfCasesPath(), null, collectionExerciseId);

    final HttpEntity<?> httpEntity = restUtility.createHttpEntity(null);

    final ResponseEntity<String> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, String.class);

    Long result = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      final String responseBody = responseEntity.getBody();
      try {
        result = objectMapper.readValue(responseBody, Long.class);
      } catch (final IOException e) {
        log.with("Collection Exercise", collectionExerciseId)
            .error("Unable to read no. of cases response", e);
      }
    }
    return result;
  }
}
