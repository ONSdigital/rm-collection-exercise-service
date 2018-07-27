package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.common.rest.RestUtilityConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.SurveySvc;

@RunWith(MockitoJUnitRunner.class)
public class SurveySvcRestClientImplTest {

  private static final String SURVEY_PATH = "/surveys";
  private static final String SURVEY_REF_PATH = SURVEY_PATH + "/ref";

  @Mock private RestTemplate restTemplate;

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @InjectMocks private SurveySvcRestClientImpl surveySvcClient;

  @Mock private AppConfig appConfig;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = RestClientException.class)
  public void ensure4xxThrownSurveyFindByRef() {
    String surveyRef = "ABC123";

    SurveySvc surveySvc = new SurveySvc();
    surveySvc.setSurveyRefPath(SURVEY_REF_PATH);
    given(appConfig.getSurveySvc()).willReturn(surveySvc);

    given(
            restTemplate.exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .willThrow(new RestClientException("Bad request"));

    surveySvcClient.findSurveyByRef(surveyRef);

    verify(restTemplate, times(1))
        .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
  }
}
