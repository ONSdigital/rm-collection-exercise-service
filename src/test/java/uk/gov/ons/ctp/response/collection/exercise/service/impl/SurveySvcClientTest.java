package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.impl.SurveySvcRestClientImpl;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.SurveySvc;
import uk.gov.ons.response.survey.representation.SurveyDTO;

import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SurveySvcClientTest {

  private static final String SURVEY_PATH = "/surveys";
  private static final String SURVEY_REF_PATH = SURVEY_PATH + "/ref";
  private static final String HTTP = "http";
  private static final String LOCALHOST = "localhost";

  @InjectMocks private SurveySvcRestClientImpl surveySvcClient;

  @Mock private AppConfig appConfig;

  @Mock private RestUtility restUtility;

  @Mock private RestTemplate restTemplate;

  @Test
  public void ensure4xxThrownSurveyFindByRef() throws Exception {
    String surveyRef = "ABC123";

    UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(SURVEY_REF_PATH)
            .build();
    SurveySvc surveySvcConfig = new SurveySvc();
    surveySvcConfig.setSurveyRefPath(SURVEY_REF_PATH);

    URI uri = uriComponents.toUri();

    when(appConfig.getSurveySvc()).thenReturn(surveySvcConfig);

    when(restUtility.createUriComponents(Mockito.eq(appConfig.getSurveySvc().getSurveyRefPath()), eq(null), eq(surveyRef)))
        .thenReturn(uriComponents);

    //    when(restTemplate.exchange(Matchers.anyObject(), Matchers.any(HttpMethod.class),
    // Matchers.<HttpEntity>any(), Matchers.<Class<String>> any()))
    //        .thenThrow(RestClientException.class);

    when(restTemplate.getForObject(Matchers.anyObject(), Matchers.<Class<SurveyDTO>>any())).thenThrow(RestClientException.class);

    surveySvcClient.findSurveyByRef(surveyRef);

    verify(restUtility, times(1)).createUriComponents(SURVEY_REF_PATH, null);
    verify(restTemplate, times(1))
        .exchange(
            eq(uriComponents.toUri()),
            eq(HttpMethod.GET),
            Matchers.any(HttpEntity.class),
            eq(String.class));
  }
}
