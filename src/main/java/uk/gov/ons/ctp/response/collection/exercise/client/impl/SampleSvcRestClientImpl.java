package uk.gov.ons.ctp.response.collection.exercise.client.impl;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HTTP RestClient implementation for calls to the Sample service
 *
 */
@Component
@Slf4j
public class SampleSvcRestClientImpl implements SampleSvcClient {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private SampleLinkRepository sampleLinkRepository;

  @Qualifier("sampleRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Autowired
  private ObjectMapper objectMapper;

  @Retryable(value = {RestClientException.class}, maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public SampleUnitsRequestDTO requestSampleUnits(CollectionExercise exercise) {

    UriComponents uriComponents = restUtility.createUriComponents(appConfig.getSampleSvc().getRequestSampleUnitsPath(),
        null, exercise);

    List<SampleLink> sampleLinks = sampleLinkRepository.findByCollectionExerciseId(exercise.getId());
    List<UUID> sampleSummaryUUIDList =  new ArrayList<>();
    for (SampleLink samplelink : sampleLinks) {
      sampleSummaryUUIDList.add(samplelink.getSampleSummaryId());
    }

    CollectionExerciseJobCreationRequestDTO requestDTO = new CollectionExerciseJobCreationRequestDTO();
    requestDTO.setCollectionExerciseId(exercise.getId());
    requestDTO.setSurveyRef(exercise.getSurvey().getSurveyRef());
    requestDTO.setExerciseDateTime(exercise.getScheduledStartDateTime());
    requestDTO.setSampleSummaryUUIDList(sampleSummaryUUIDList);

    HttpEntity<CollectionExerciseJobCreationRequestDTO> httpEntity = restUtility.createHttpEntity(requestDTO);

    log.debug("about to get to the Sample SVC with CollectionExerciseId: {}", exercise.getId());
    ResponseEntity<String> responseEntity = restTemplate.exchange(
            uriComponents.toUri(), HttpMethod.POST, httpEntity, String.class);

    SampleUnitsRequestDTO result = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      String responseBody = responseEntity.getBody();
      try {
        result = objectMapper.readValue(responseBody, SampleUnitsRequestDTO.class);
      } catch (IOException e) {
        String msg = String.format("cause = %s - message = %s", e.getCause(), e.getMessage());
        log.error(msg);
      }
    }
    return result;
  }

}
