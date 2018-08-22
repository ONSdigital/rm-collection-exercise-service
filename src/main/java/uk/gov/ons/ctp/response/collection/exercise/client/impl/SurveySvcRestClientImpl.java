package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
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
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** HTTP RestClient implementation for calls to the Survey service */
@Component
@Qualifier("restClient")
@Primary
public class SurveySvcRestClientImpl implements SurveySvcClient {
  private static final Logger log = LoggerFactory.getLogger(SurveySvcRestClientImpl.class);

  @Autowired private AppConfig appConfig;

  @Autowired private RestTemplate restTemplate;

  @Qualifier("surveyRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Autowired private ObjectMapper objectMapper;

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public List<SurveyClassifierDTO> requestClassifierTypeSelectors(final UUID surveyId)
      throws RestClientException {

    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getRequestClassifierTypesListPath(), null, surveyId);

    HttpEntity<List<SurveyClassifierDTO>> httpEntity = restUtility.createHttpEntity(null);

    log.with("survey_id", surveyId).debug("about to get to the Survey SVC");
    ResponseEntity<String> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, String.class);

    List<SurveyClassifierDTO> result = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      String responseBody = responseEntity.getBody();
      try {
        result =
            objectMapper.readValue(responseBody, new TypeReference<List<SurveyClassifierDTO>>() {});
      } catch (IOException e) {
        log.error("Unable to read Survey Classifier response", e);
      }
    }

    return result;
  }

  @Override
  public SurveyClassifierTypeDTO requestClassifierTypeSelector(
      final UUID surveyId, final UUID classifierType) throws RestClientException {

    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getRequestClassifierTypesPath(),
            null,
            surveyId,
            classifierType);

    HttpEntity<?> httpEntity = restUtility.createHttpEntity(null);

    log.with("survey_id", surveyId)
        .with("classifier_type", classifierType)
        .debug("about to get to the Survey SVC");
    ResponseEntity<String> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, String.class);

    SurveyClassifierTypeDTO result = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      String responseBody = responseEntity.getBody();
      try {
        result = objectMapper.readValue(responseBody, SurveyClassifierTypeDTO.class);
      } catch (IOException e) {
        log.error("Unable to read Survey Classifier response", e);
      }
    }
    return result;
  }

  private SurveyDTO getSurveyDtoFromResponseEntity(ResponseEntity<String> responseEntity) {
    SurveyDTO result = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      String responseBody = responseEntity.getBody();
      try {
        result = objectMapper.readValue(responseBody, SurveyDTO.class);
      } catch (IOException e) {
        log.error("Unable to read Survey response", e);
      }
    }
    return result;
  }

  @Override
  public SurveyDTO findSurvey(final UUID surveyId) {
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getSurveyDetailPath(), null, surveyId);

    HttpEntity<?> httpEntity = restUtility.createHttpEntity(null);
    ResponseEntity<String> responseEntity = null;
    SurveyDTO survey = null;

    try {
      log.with("survey_id", surveyId)
          .with("uri", uriComponents.toUri())
          .debug("about to get to the Survey SVC");
      responseEntity =
          restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, String.class);
      survey = getSurveyDtoFromResponseEntity(responseEntity);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      }
      throw e;
    }
    return survey;
  }

  @Override
  public SurveyDTO findSurveyByRef(final String surveyRef) {
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSurveySvc().getSurveyRefPath(), null, surveyRef);

    HttpEntity<?> httpEntity = restUtility.createHttpEntity(null);
    ResponseEntity<String> responseEntity = null;
    SurveyDTO survey = null;

    try {
      responseEntity =
          restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, String.class);

      survey = getSurveyDtoFromResponseEntity(responseEntity);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      }
      throw e;
    }
    return survey;
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
