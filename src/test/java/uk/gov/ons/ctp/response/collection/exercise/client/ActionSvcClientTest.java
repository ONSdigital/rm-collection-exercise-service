package uk.gov.ons.ctp.response.collection.exercise.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.sun.jndi.toolkit.url.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtilityConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;

@RunWith(MockitoJUnitRunner.class)
public class ActionSvcClientTest {

  private static final String ACTION_PLAN_NAME = "APName";
  private static final String ACTION_PLAN_DESCRIPTION = "APDescription";
  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String ACTION_PLAN_ID = "14fb3e68-5dca-46db-bf49-04b84e07e77c";

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @InjectMocks private ActionSvcClient actionSvcRestClient;

  private ResponseEntity<List<ActionPlanDTO>> responseEntity;

  private ResponseEntity<ActionPlanDTO> actionPlanResponseEntity;

  private List<ActionPlanDTO> actionPlans;

  private HashMap<String, String> selectors;

  @Before
  public void setUp() {
    ActionPlanDTO actionPlan = new ActionPlanDTO();
    actionPlan.setName(ACTION_PLAN_NAME);
    actionPlan.setId(UUID.fromString("14fb3e68-4dca-46db-bf49-04b84e07e77c"));
    selectors = new HashMap<>();
    selectors.put("collectionExerciseId", COLLECTION_EXERCISE_ID);
    actionPlan.setSelectors(selectors);

    actionPlans = new ArrayList<>();
    actionPlans.add(actionPlan);

    responseEntity = new ResponseEntity<>(actionPlans, HttpStatus.OK);
    actionPlanResponseEntity = new ResponseEntity<>(actionPlan, HttpStatus.OK);

    MockitoAnnotations.initMocks(this);

    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
  }

  @Test
  public void createActionPlan_201Response() {
    // Given
    when(restTemplate.postForObject(any(URI.class), any(HttpEntity.class), eq(ActionPlanDTO.class)))
        .thenReturn(actionPlans.get(0));

    // When
    actionSvcRestClient.createActionPlan(ACTION_PLAN_NAME, ACTION_PLAN_DESCRIPTION, selectors);

    // Then
    verify(restTemplate, times(1))
        .postForObject(any(URI.class), any(HttpEntity.class), eq(ActionPlanDTO.class));
  }

  @Test(expected = HttpClientErrorException.class)
  public void createActionPlan_500Response() {
    // Given
    when(restTemplate.postForObject(any(URI.class), any(HttpEntity.class), eq(ActionPlanDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    actionSvcRestClient.createActionPlan(ACTION_PLAN_NAME, ACTION_PLAN_DESCRIPTION, selectors);

    // Then
    verify(restTemplate, times(1))
        .postForObject(any(URI.class), any(HttpEntity.class), eq(ActionPlanDTO.class));
  }

  @Test
  public void updateActionPlanNameAndDescription_200Response() throws CTPException {

    // Given
    when(restTemplate.exchange(
      any(URI.class),
      eq(HttpMethod.PUT),
      any(HttpEntity.class),
      eq(ActionPlanDTO.class)))
      .thenReturn(actionPlanResponseEntity);

    // When
    ActionPlanDTO actionPlan =
      actionSvcRestClient.updateActionPlanNameAndDescription(UUID.fromString(ACTION_PLAN_ID),
        "test_name", "test_description");

    assertEquals(actionPlan.getId(), actionPlans.get(0).getId());
  }

  @Test(expected = HttpClientErrorException.class)
  public void updateActionPlanNameAndDescription_500Response() {
    // Given
    when(restTemplate.exchange(
      any(URI.class),
      eq(HttpMethod.PUT),
      any(HttpEntity.class),
      eq(ActionPlanDTO.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    actionSvcRestClient.updateActionPlanNameAndDescription(UUID.fromString(ACTION_PLAN_ID),
      "test_name", "test_description");

    // Then
    verify(restTemplate, times(1))
      .exchange(any(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(ActionPlanDTO.class));
  }

  @Test
  public void getActionPlanById_200Response() throws CTPException {

    // Given
    when(restTemplate.exchange(
      any(URI.class),
      eq(HttpMethod.GET),
      any(HttpEntity.class),
      eq(ActionPlanDTO.class)))
      .thenReturn(actionPlanResponseEntity);

    // When
    ActionPlanDTO actionPlan =
      actionSvcRestClient.getActionPlanById(UUID.fromString(ACTION_PLAN_ID));

    assertEquals(actionPlan.getId(), actionPlans.get(0).getId());
  }

  @Test
  public void getActionPlanByID_404Response() throws CTPException {
    // Given
    when(restTemplate.exchange(
      any(URI.class),
      eq(HttpMethod.GET),
      any(HttpEntity.class),
      eq(ActionPlanDTO.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // When
    ActionPlanDTO actionPlan =
    actionSvcRestClient.getActionPlanById(UUID.fromString(ACTION_PLAN_ID));

    // Then
    verify(restTemplate, times(1))
      .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ActionPlanDTO.class));
    assertNull(actionPlan);
  }

  @Test(expected = HttpClientErrorException.class)
  public void getActionPlanByID_500Response() {
    // Given
    when(restTemplate.exchange(
      any(URI.class),
      eq(HttpMethod.GET),
      any(HttpEntity.class),
      eq(ActionPlanDTO.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    actionSvcRestClient.getActionPlanById(UUID.fromString(ACTION_PLAN_ID));

    // Then
    verify(restTemplate, times(1))
      .exchange(any(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ActionPlanDTO.class));
  }

  @Test
  public void getActionPlanBySelectorsBusiness_200Response() throws CTPException {

    // Given
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(responseEntity);

    // When
    ActionPlanDTO actionPlan =
        actionSvcRestClient.getActionPlanBySelectorsBusiness(COLLECTION_EXERCISE_ID, false);

    assertEquals(actionPlan.getId(), actionPlans.get(0).getId());
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsBusiness_204Response() throws CTPException {
    // Given
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

    // When
    actionSvcRestClient.getActionPlanBySelectorsBusiness(COLLECTION_EXERCISE_ID, false);

    // Then CTPException is thrown
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsBusiness_MultipleActionPlans() throws CTPException {
    // Given
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(
            new ResponseEntity<>(
                Arrays.asList(new ActionPlanDTO(), new ActionPlanDTO()), HttpStatus.OK));

    // When
    actionSvcRestClient.getActionPlanBySelectorsBusiness(COLLECTION_EXERCISE_ID, false);

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
  public void getActionPlanBySelectorsSocial_200Response() throws CTPException {

    // Given
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(responseEntity);

    // When
    ActionPlanDTO actionplan =
        actionSvcRestClient.getActionPlanBySelectorsSocial(COLLECTION_EXERCISE_ID);

    // Then call is made to correct url
    assertEquals(actionplan.getId(), actionPlans.get(0).getId());
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsSocial_204Response() throws CTPException {

    // Given
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

    // When
    actionSvcRestClient.getActionPlanBySelectorsSocial(COLLECTION_EXERCISE_ID);

    // Then CTPException is thrown
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsSocial_MultipleActionPlans() throws CTPException {
    // Given
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(
            new ResponseEntity<>(
                Arrays.asList(new ActionPlanDTO(), new ActionPlanDTO()), HttpStatus.OK));

    // When
    actionSvcRestClient.getActionPlanBySelectorsSocial(COLLECTION_EXERCISE_ID);

    // Then CTPException is thrown
  }
}
