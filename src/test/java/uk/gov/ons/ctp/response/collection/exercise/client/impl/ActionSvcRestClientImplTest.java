package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.common.rest.RestUtilityConfig;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

@RunWith(MockitoJUnitRunner.class)
public class ActionSvcRestClientImplTest {

  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";

  @Spy RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @InjectMocks private ActionSvcRestClientImpl actionSvcRestClient;

  private ResponseEntity<List<ActionPlanDTO>> responseEntity;
  private ResponseEntity<List<ActionPlanDTO>> responseEntity404;
  private ResponseEntity<List<ActionPlanDTO>> responseEntityFail;

  @Before
  public void setup() {
    ActionPlanDTO actionPlan = new ActionPlanDTO();
    actionPlan.setName("Test");
    actionPlan.setId(UUID.fromString("14fb3e68-4dca-46db-bf49-04b84e07e77c"));
    HashMap<String, String> selectors = new HashMap<>();
    selectors.put("collectionExerciseId", COLLECTION_EXERCISE_ID);
    actionPlan.setSelectors(selectors);

    List<ActionPlanDTO> actionPlans = new ArrayList<>();
    actionPlans.add(actionPlan);

    responseEntity = new ResponseEntity(actionPlans, HttpStatus.OK);
    responseEntity404 = new ResponseEntity(HttpStatus.NOT_FOUND);
    responseEntityFail = new ResponseEntity(HttpStatus.BAD_REQUEST);

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getActionPlansBySelectors() {

    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            eq(null),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(responseEntityFail);

    // When
    actionSvcRestClient.getActionPlansBySelectors(COLLECTION_EXERCISE_ID, false);

    // Then call is made to correct url
    verify(restTemplate, times(1))
        .exchange(
            String.format(
                "test:path?collectionExerciseId=%s&activeEnrolment=false", COLLECTION_EXERCISE_ID),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActionPlanDTO>>() {});
  }

  @Test
  public void getActionPlansBySelectors404() {

    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
      any(String.class),
      eq(HttpMethod.GET),
      eq(null),
      eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
      .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // When
    List<ActionPlanDTO> actionPlanDTOs =
      actionSvcRestClient.getActionPlansBySelectors(COLLECTION_EXERCISE_ID, false);

    // Then call is made to correct url
    assertNull(actionPlanDTOs);
  }

  @Test(expected = HttpClientErrorException.class)
  public void getActionPlansBySelectorsFail() {

    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
      any(String.class),
      eq(HttpMethod.GET),
      eq(null),
      eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
      .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    // When
    actionSvcRestClient.getActionPlansBySelectors(COLLECTION_EXERCISE_ID, false);

    // Then HTTPClientErrorException is thrown
  }
}
