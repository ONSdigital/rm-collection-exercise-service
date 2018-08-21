package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.common.rest.RestUtilityConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.SurveySvc;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.response.survey.representation.SurveyDTO;

@RunWith(MockitoJUnitRunner.class)
public class SurveySvcRestClientImplTest {

  private static final String SURVEY_PATH = "/surveys";
  private static final String SURVEY_REF_PATH = SURVEY_PATH + "/ref";

  @Mock private RestTemplate restTemplate;

  @Spy private final RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @InjectMocks private SurveySvcRestClientImpl surveySvcClient;

  @Mock private ObjectMapper objectMapper;

  @Mock private AppConfig appConfig;

  @Before
  public void prepareSurveyService() {
    SurveySvc surveySvc = new SurveySvc();
    surveySvc.setSurveyRefPath(SURVEY_REF_PATH);
    given(appConfig.getSurveySvc()).willReturn(surveySvc);
  }

  @Test(expected = RestClientException.class)
  public void ensure4xxThrownSurveyFindByRef() {
    String surveyRef = "ABC123";

    given(
            restTemplate.exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .willThrow(new RestClientException("Bad request"));

    surveySvcClient.findSurveyByRef(surveyRef);

    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
  }

  @Test
  public void ensureNullReturnedOnNullSurvey() {
    UUID surveyId = UUID.randomUUID();

    given(
            restTemplate.exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .willThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Bad request"));

    SurveyDTO survey = surveySvcClient.findSurvey(surveyId);

    // When a 404 error is thrown, the function will return a null survey
    assertNull("Survey was not null as expected", survey);
  }

  @Test
  public void ensureSurveyReturnedOnGetSurvey() throws IOException {
    final UUID surveyId = UUID.randomUUID();

    final ResponseEntity<String> responseEntity = new ResponseEntity<>("json", HttpStatus.OK);
    given(
            restTemplate.exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .willReturn(responseEntity);

    final SurveyDTO surveyDto = new SurveyDTO();

    when(objectMapper.readValue("json", SurveyDTO.class)).thenReturn(surveyDto);

    final SurveyDTO returnedSurvey = surveySvcClient.findSurvey(surveyId);

    assertThat(returnedSurvey, is(surveyDto));
  }

  @Test
  public void ensureGetSurveyForCollectionExerciseReturnsSurvey() throws IOException, CTPException {
    final UUID surveyId = UUID.randomUUID();
    final SurveyDTO surveyDto = new SurveyDTO();
    surveyDto.setId(surveyId.toString());

    final CollectionExercise collex = new CollectionExercise();
    collex.setSurveyId(surveyId);

    final ResponseEntity<String> responseEntity = new ResponseEntity<>("json", HttpStatus.OK);
    given(
            restTemplate.exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .willReturn(responseEntity);

    when(objectMapper.readValue("json", SurveyDTO.class)).thenReturn(surveyDto);

    final SurveyDTO returnedSurvey = surveySvcClient.getSurveyForCollectionExercise(collex);

    assertThat(returnedSurvey, is(surveyDto));
  }

  @Test(expected = CTPException.class)
  public void throwCTPExceptionIfSurveyNotFoundForCollectionExercise() throws CTPException {
    final UUID surveyId = UUID.randomUUID();
    final CollectionExercise collectionExercse = new CollectionExercise();
    collectionExercse.setSurveyId(surveyId);

    given(
            restTemplate.exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .willThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Bad request"));

    surveySvcClient.getSurveyForCollectionExercise(collectionExercse);
  }
}
