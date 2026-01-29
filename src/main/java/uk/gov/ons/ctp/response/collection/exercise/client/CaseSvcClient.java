package uk.gov.ons.ctp.response.collection.exercise.client;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.Event;
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
        log.error(
            "Unable to read no. of cases response",
            kv("Collection Exercise", collectionExerciseId),
            e);
      }
    }
    return result;
  }

  public boolean processEvent(final String tag, final UUID collectionExerciseId)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getCaseSvc().getProcessEventPath(), null);

    final Event event = new Event();
    event.setCollectionExerciseID(collectionExerciseId);
    event.setTag(Event.EventTag.valueOf(tag));
    final HttpEntity<Event> httpEntity = restUtility.createHttpEntity(event);
    final ResponseEntity<String> response =
        restTemplate.postForEntity(uriComponents.toUri(), httpEntity, String.class);
    return response.getStatusCode().is2xxSuccessful();
  }
}
