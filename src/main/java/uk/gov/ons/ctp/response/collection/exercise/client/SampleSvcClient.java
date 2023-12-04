package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;

/** HTTP RestClient implementation for calls to the Sample service */
@Component
public class SampleSvcClient {
  private static final Logger log = LoggerFactory.getLogger(SampleSvcClient.class);

  @Autowired private AppConfig appConfig;

  @Autowired private RestTemplate restTemplate;

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Qualifier("sampleRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Autowired private SurveySvcClient surveyService;

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public SampleSummaryDTO getSampleSummary(UUID sampleSummaryId) {
    log.with("sampleSummaryId", sampleSummaryId).debug("Getting sample summary");
    UriComponents uri =
        restUtility.createUriComponents(
            "/samples/samplesummary/{sampleSummaryId}", null, sampleSummaryId);
    HttpEntity<UriComponents> httpEntity = restUtility.createHttpEntity(uri);
    ResponseEntity<SampleSummaryDTO> response =
        restTemplate.exchange(uri.toUri(), HttpMethod.GET, httpEntity, SampleSummaryDTO.class);

    SampleSummaryDTO sampleSummary = response.getBody();
    log.debug("Got sampleSummary={}", sampleSummary);
    return sampleSummary;
  }

  public SampleUnitsRequestDTO getSampleUnitCount(List<UUID> sampleSummaryIdList) {

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    sampleSummaryIdList.forEach(id -> queryParams.add("sampleSummaryId", id.toString()));

    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSampleSvc().getRequestSampleUnitCountPath(), queryParams);

    HttpEntity<UriComponents> httpEntity = restUtility.createHttpEntity(uriComponents);

    ResponseEntity<SampleUnitsRequestDTO> responseEntity =
        restTemplate.exchange(
            uriComponents.toUri(), HttpMethod.GET, httpEntity, SampleUnitsRequestDTO.class);

    return responseEntity.getBody();
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public SampleUnitDTO[] requestSampleUnitsForSampleSummary(UUID sampleSummaryId, boolean failed) {
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    if (failed) {
      queryParams.add("state", SampleUnitDTO.SampleUnitState.FAILED.name());
    }

    log.with("sampleSummaryId", sampleSummaryId).info("request sample units for sample summary");
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSampleSvc().getRequestSampleUnitsForSampleSummaryPath(),
            queryParams,
            sampleSummaryId);

    HttpEntity<UriComponents> httpEntity = restUtility.createHttpEntity(uriComponents);

    ResponseEntity<SampleUnitDTO[]> responseEntity =
        restTemplate.exchange(
            uriComponents.toUri(), HttpMethod.GET, httpEntity, SampleUnitDTO[].class);

    return responseEntity.getBody();
  }
}
