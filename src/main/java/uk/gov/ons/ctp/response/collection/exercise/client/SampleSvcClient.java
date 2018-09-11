package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

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

  /**
   * Request the delivery of sample units from the Sample Service via a message queue.
   *
   * @param exercise the Collection Exercise for which to request sample units.
   * @return the total number of sample units in the collection exercise.
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
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

      log.with("collection_exercise_id", exercise.getId().toString())
          .debug("Requesting sample unit for collection exercise");
      ResponseEntity<SampleUnitsRequestDTO> responseEntity =
          restTemplate.exchange(
              uriComponents.toUri(), HttpMethod.POST, httpEntity, SampleUnitsRequestDTO.class);
      return responseEntity.getBody();
    }
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public SampleSummaryDTO getSampleSummary(UUID sampleSummaryId) {
    log.with("sample_summary_id").debug("Getting sample summary");
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
}
