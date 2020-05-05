package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
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
import uk.gov.ons.ctp.response.collection.exercise.lib.party.definition.SampleLinkCreationRequestDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;

/** HTTP RestClient implementation for calls to the Party service */
@Component
public class PartySvcClient {
  private static final Logger log = LoggerFactory.getLogger(PartySvcClient.class);

  private AppConfig appConfig;
  private RestTemplate restTemplate;
  private RestUtility restUtility;

  public PartySvcClient(
      AppConfig appConfig,
      RestTemplate restTemplate,
      @Qualifier("partyRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Request party from the Party Service
   *
   * @param sampleUnitType the sample unit type for which to request party
   * @param sampleUnitRef the sample unit ref for which to request party
   * @return the party object
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public PartyDTO requestParty(SampleUnitDTO.SampleUnitType sampleUnitType, String sampleUnitRef) {
    log.with("sample_unit_type", sampleUnitType)
        .with("sample_unit_ref", sampleUnitRef)
        .debug("Retrieving party");
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getPartySvc().getRequestPartyPath(), null, sampleUnitType, sampleUnitRef);
    HttpEntity<PartyDTO> httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<PartyDTO> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, PartyDTO.class);
    return responseEntity.getBody();
  }

  public void linkSampleSummaryId(String sampleSummaryId, String collectionExerciseId) {
    log.with("sample_summary_id", sampleSummaryId)
        .with("collection_exercise_id", collectionExerciseId)
        .debug("Linking sample summary to collection exercise");
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getPartySvc().getSampleLinkPath(), null, sampleSummaryId);
    SampleLinkCreationRequestDTO sampleLinkCreationRequestDTO = new SampleLinkCreationRequestDTO();
    sampleLinkCreationRequestDTO.setCollectionExerciseId(collectionExerciseId);
    HttpEntity<SampleLinkCreationRequestDTO> httpEntity =
        restUtility.createHttpEntity(sampleLinkCreationRequestDTO);
    restTemplate.exchange(uriComponents.toUri(), HttpMethod.PUT, httpEntity, SampleLinkDTO.class);
  }
}
