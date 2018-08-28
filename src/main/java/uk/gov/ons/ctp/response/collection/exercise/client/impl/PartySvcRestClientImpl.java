package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
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
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.party.definition.SampleLinkCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.party.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/** HTTP RestClient implementation for calls to the Party service */
@Component
public class PartySvcRestClientImpl implements PartySvcClient {
  private static final Logger log = LoggerFactory.getLogger(PartySvcRestClientImpl.class);

  @Autowired private AppConfig appConfig;

  @Autowired private RestTemplate restTemplate;

  @Qualifier("partyRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Autowired private ObjectMapper objectMapper;

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public PartyDTO requestParty(SampleUnitDTO.SampleUnitType sampleUnitType, String sampleUnitRef)
      throws RestClientException {

    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getPartySvc().getRequestPartyPath(), null, sampleUnitType, sampleUnitRef);

    HttpEntity<PartyDTO> httpEntity = restUtility.createHttpEntity(null);

    log.debug(
        "about to get the Party with Sample Unit Type: {} and Sample Unit Ref: {}",
        sampleUnitType,
        sampleUnitRef);

    ResponseEntity<String> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, String.class);

    if (responseEntity != null) {
      System.out.println(responseEntity.getStatusCodeValue());
    }

    PartyDTO result = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      String responseBody = responseEntity.getBody();
      try {
        result = objectMapper.readValue(responseBody, PartyDTO.class);
      } catch (IOException e) {
        String msg = String.format("cause = %s - message = %s", e.getCause(), e.getMessage());
        log.error(msg);
      }
    }

    return result;
  }

  @Override
  public SampleLinkDTO linkSampleSummaryId(String sampleSummaryId, String collectionExerciseId)
      throws RestClientException {
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getPartySvc().getSampleLinkPath(), null, sampleSummaryId);
    SampleLinkCreationRequestDTO sampleLinkCreationRequestDTO = new SampleLinkCreationRequestDTO();
    sampleLinkCreationRequestDTO.setCollectionExerciseId(collectionExerciseId);
    HttpEntity<SampleLinkCreationRequestDTO> httpEntity =
        restUtility.createHttpEntity(sampleLinkCreationRequestDTO);
    ResponseEntity<SampleLinkDTO> responseEntity =
        restTemplate.exchange(
            uriComponents.toUri(), HttpMethod.PUT, httpEntity, SampleLinkDTO.class);

    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      log.info(
          "Created link Sample Summary Id: "
              + sampleSummaryId
              + " Collection exercise: "
              + collectionExerciseId);
    } else {
      log.error(
          "Couldn't link Sample Summary Id: "
              + sampleSummaryId
              + " Collection exercise: "
              + collectionExerciseId
              + " Status code: "
              + responseEntity.getStatusCode());
    }
    return responseEntity.getBody();
  }
}
