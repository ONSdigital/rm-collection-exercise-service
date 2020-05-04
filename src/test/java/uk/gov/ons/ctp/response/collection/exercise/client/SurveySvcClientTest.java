package uk.gov.ons.ctp.response.collection.exercise.client;

import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.SurveySvc;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtilityConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyClassifierTypeDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;

@RunWith(MockitoJUnitRunner.class)
public class SurveySvcClientTest {

  private static final UUID SURVEY_ID = UUID.fromString("ede0c029-3109-4ae1-b8c8-e4968000e2f4");
  private static final UUID CLASSIFIER_TYPE =
      UUID.fromString("dee0c029-3109-4ae1-b8c8-e4968000e2e5");
  private static final String SURVEY_REF = "ABC123";
  private static final String SURVEY_REF_PATH = "/surveys/ref";

  @Mock private RestTemplate restTemplate;

  @Spy private final RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private AppConfig appConfig;

  @InjectMocks private SurveySvcClient surveySvcClient;

  @Before
  public void setUp() {
    SurveySvc surveySvc = new SurveySvc();
    surveySvc.setSurveyRefPath(SURVEY_REF_PATH);
    given(appConfig.getSurveySvc()).willReturn(surveySvc);
  }

  @Test
  public void findSurveyByRef_200Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    // When
    surveySvcClient.findSurveyByRef(SURVEY_REF);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class));
  }

  @Test
  public void findSurveyByRef_404Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // When
    SurveyDTO survey = surveySvcClient.findSurveyByRef(SURVEY_REF);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class));
    assertNull(survey);
  }

  @Test(expected = HttpClientErrorException.class)
  public void findSurveyByRef_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    surveySvcClient.findSurveyByRef(SURVEY_REF);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class));
  }

  @Test
  public void findSurvey_200Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    // When
    surveySvcClient.findSurvey(SURVEY_ID);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class));
  }

  @Test
  public void findSurvey_404Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // When
    SurveyDTO survey = surveySvcClient.findSurvey(SURVEY_ID);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class));
    assertNull(survey);
  }

  @Test(expected = HttpClientErrorException.class)
  public void findSurvey_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    surveySvcClient.findSurvey(SURVEY_ID);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyDTO.class));
  }

  @Test
  public void requestClassifierTypeSelector_200Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(SurveyClassifierTypeDTO.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    // When
    surveySvcClient.requestClassifierTypeSelector(SURVEY_ID, CLASSIFIER_TYPE);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyClassifierTypeDTO.class));
  }

  @Test(expected = HttpClientErrorException.class)
  public void requestClassifierTypeSelector_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(SurveyClassifierTypeDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    surveySvcClient.requestClassifierTypeSelector(SURVEY_ID, CLASSIFIER_TYPE);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SurveyClassifierTypeDTO.class));
  }

  @Test
  public void requestClassifierTypeSelectors_200Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<SurveyClassifierDTO>>() {})))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    // When
    surveySvcClient.requestClassifierTypeSelectors(SURVEY_ID);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<SurveyClassifierDTO>>() {}));
  }

  @Test(expected = HttpClientErrorException.class)
  public void requestClassifierTypeSelectors_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<SurveyClassifierDTO>>() {})))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    surveySvcClient.requestClassifierTypeSelectors(SURVEY_ID);

    // Then
    verify(restTemplate, times(1))
        .exchange(
            any(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<SurveyClassifierDTO>>() {}));
  }
}
