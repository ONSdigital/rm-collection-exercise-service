package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** HTTP RestClient implementation for calls to the Survey service */
@Component
public class SurveySvcClient {
  private static final Logger log = LoggerFactory.getLogger(SurveySvcClient.class);

  private final AppConfig appConfig;

  private final RestTemplate restTemplate;
  private final RestUtility restUtility;

  @Autowired
  public SurveySvcClient(
      AppConfig appConfig,
      RestTemplate restTemplate,
      @Qualifier("surveyRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Get classifier type selectors for Survey by UUID.
   *
   * @param surveyId UUID for which to request classifiers.
   * @return List of SurveyClassifierDTO classifier selectors.
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public List<SurveyClassifierDTO> requestClassifierTypeSelectors(final UUID surveyId) {
    log.with("survey_id", surveyId).debug("Retrieving survey");
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getRequestClassifierTypesListPath(), null, surveyId);
    HttpEntity<List<SurveyClassifierDTO>> httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<List<SurveyClassifierDTO>> responseEntity =
        restTemplate.exchange(
            uriComponents.toUri(),
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<SurveyClassifierDTO>>() {});
    return responseEntity.getBody();
  }

  /**
   * Get classifier type selector for Survey UUID and ClassifierType UUID.
   *
   * @param surveyId UUID for which to request classifiers.
   * @param classifierType UUID for classifier type.
   * @return SurveyClassifierTypeDTO details of selector type requested.
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public SurveyClassifierTypeDTO requestClassifierTypeSelector(
      final UUID surveyId, final UUID classifierType) {
    log.with("survey_id", surveyId.toString())
        .with("classifier_type", classifierType.toString())
        .debug("Requesting survey classifier type");
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getRequestClassifierTypesPath(),
            null,
            surveyId,
            classifierType);
    HttpEntity<?> httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<SurveyClassifierTypeDTO> responseEntity =
        restTemplate.exchange(
            uriComponents.toUri(), HttpMethod.GET, httpEntity, SurveyClassifierTypeDTO.class);
    return responseEntity.getBody();
  }

  public SurveyDTO findSurvey(final UUID surveyId) {
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getSurveyDetailPath(), null, surveyId);

    HttpEntity<?> httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<SurveyDTO> responseEntity;
    try {
      log.with("survey_id", surveyId.toString()).debug("Retrieving survey");
      responseEntity =
          restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, SurveyDTO.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      }
      log.error("Failed to retrieve survey", e);
      throw e;
    }
    return responseEntity.getBody();
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public SurveyDTO findSurveyByRef(final String surveyRef) {
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getSurveyRefPath(), null, surveyRef);

    HttpEntity<?> httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<SurveyDTO> responseEntity;
    try {
      responseEntity =
          restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, SurveyDTO.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      }
      log.error("Failed to retrieve survey", e);
      throw e;
    }
    return responseEntity.getBody();
  }

  public SurveyDTO getSurveyForCollectionExercise(final CollectionExercise collex)
      throws CTPException {
    final SurveyDTO survey = findSurvey(collex.getSurveyId());

    if (survey == null) {
      throw new CTPException(
          Fault.SYSTEM_ERROR, String.format("Could not find survey id %s", collex.getId()));
    }

    return survey;
  }
}
