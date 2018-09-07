package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitSizeRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** HTTP RestClient implementation for calls to the Sample service */
@Component
public class SampleSvcRestClientImpl implements SampleSvcClient {
  private static final Logger log = LoggerFactory.getLogger(SampleSvcRestClientImpl.class);

  @Autowired private AppConfig appConfig;

  @Autowired private RestTemplate restTemplate;

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Qualifier("sampleRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SurveyService surveyService;

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public SampleUnitsRequestDTO requestSampleUnits(CollectionExercise exercise) throws CTPException {

    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSampleSvc().getRequestSampleUnitsPath(), null, exercise);

    List<SampleLink> sampleLinks =
        sampleLinkRepository.findByCollectionExerciseId(exercise.getId());
    List<UUID> sampleSummaryUUIDList = new ArrayList<>();
    for (SampleLink samplelink : sampleLinks) {
      sampleSummaryUUIDList.add(samplelink.getSampleSummaryId());
    }

    SurveyDTO surveyDto = this.surveyService.findSurvey(exercise.getSurveyId());

    if (surveyDto == null) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          String.format(
              "Invalid survey %s for collection exercise %s",
              exercise.getSurveyId(), exercise.getId()));
    } else {
      CollectionExerciseJobCreationRequestDTO requestDTO =
          new CollectionExerciseJobCreationRequestDTO();
      requestDTO.setCollectionExerciseId(exercise.getId());
      requestDTO.setSurveyRef(surveyDto.getSurveyRef());
      requestDTO.setExerciseDateTime(exercise.getScheduledStartDateTime());
      requestDTO.setSampleSummaryUUIDList(sampleSummaryUUIDList);

      HttpEntity<CollectionExerciseJobCreationRequestDTO> httpEntity =
          restUtility.createHttpEntity(requestDTO);

      log.with("collection_exercise_id", exercise.getId()).debug("about to get to the Sample SVC");
      ResponseEntity<String> responseEntity =
          restTemplate.exchange(uriComponents.toUri(), HttpMethod.POST, httpEntity, String.class);

      SampleUnitsRequestDTO result = null;
      if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
        String responseBody = responseEntity.getBody();
        try {
          result = objectMapper.readValue(responseBody, SampleUnitsRequestDTO.class);
        } catch (IOException e) {
          log.error("Unable to read party response", e);
        }
      }
      return result;
    }
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public SampleSummaryDTO getSampleSummary(UUID sampleSummaryId) {
    log.with("sample_summary_id", sampleSummaryId).debug("Getting sample summary");
    UriComponents uri =
        restUtility.createUriComponents(
            "/samples/samplesummary/{sampleSummaryId}", null, sampleSummaryId);
    HttpEntity<UriComponents> httpEntity = restUtility.createHttpEntity(uri);
    ResponseEntity<SampleSummaryDTO> response =
        restTemplate.exchange(uri.toUri(), HttpMethod.GET, httpEntity, SampleSummaryDTO.class);

    SampleSummaryDTO sampleSummary = response.getBody();
    log.with("sample_summary", sampleSummary).debug("Got sample Summary");
    return sampleSummary;
  }

  @Override
  public SampleUnitsRequestDTO getSampleUnitSize(
      SampleUnitSizeRequestDTO sampleUnitSizeRequestDTO) {

    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSampleSvc().getRequestSampleUnitSizePath(), null);

    HttpEntity<SampleUnitSizeRequestDTO> httpEntity =
        restUtility.createHttpEntity(sampleUnitSizeRequestDTO);

    ResponseEntity<SampleUnitsRequestDTO> responseEntity =
        restTemplate.exchange(
            uriComponents.toUri(), HttpMethod.POST, httpEntity, SampleUnitsRequestDTO.class);

    return responseEntity.getBody();
  }
}
