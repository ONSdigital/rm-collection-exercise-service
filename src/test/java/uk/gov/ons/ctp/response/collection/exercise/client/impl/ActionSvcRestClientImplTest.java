package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.common.rest.RestUtilityConfig;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

@RunWith(MockitoJUnitRunner.class)
public class ActionSvcRestClientImplTest {

  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @InjectMocks private ActionSvcRestClientImpl actionSvcRestClient;

  private ResponseEntity<List<ActionPlanDTO>> responseEntity;

  private List<ActionPlanDTO> actionPlans;

  @Before
  public void setUp() {
    ActionPlanDTO actionPlan = new ActionPlanDTO();
    actionPlan.setName("Test");
    actionPlan.setId(UUID.fromString("14fb3e68-4dca-46db-bf49-04b84e07e77c"));
    HashMap<String, String> selectors = new HashMap<>();
    selectors.put("collectionExerciseId", COLLECTION_EXERCISE_ID);
    actionPlan.setSelectors(selectors);

    actionPlans = new ArrayList<>();
    actionPlans.add(actionPlan);

    responseEntity = new ResponseEntity(actionPlans, HttpStatus.OK);

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getActionPlanBySelectorsBusiness() throws CTPException {

    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any(ActionPlanDTO.class))).thenReturn(httpEntity);
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(responseEntity);

    // When
    ActionPlanDTO actionplan =
        actionSvcRestClient.getActionPlanBySelectorsBusiness(COLLECTION_EXERCISE_ID, false);

    // Then call is made to correct url
    verify(restTemplate, times(1))
        .exchange(
            String.format(
                "test:path?collectionExerciseId=%s&activeEnrolment=false", COLLECTION_EXERCISE_ID),
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<ActionPlanDTO>>() {});
    assertEquals(actionplan.getId(), actionPlans.get(0).getId());
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsBusinessNoContentThrowsCTPException() throws CTPException {

    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.NO_CONTENT));

    // When
    actionSvcRestClient.getActionPlanBySelectorsBusiness(COLLECTION_EXERCISE_ID, false);

    // Then CTPException is thrown
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsBusinessMultiplePlansThrowsCTPException()
      throws CTPException {
    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            eq(httpEntity),
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
  public void getActionPlanBySelectorsSocial() throws CTPException {

    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(responseEntity);

    // When
    ActionPlanDTO actionplan =
        actionSvcRestClient.getActionPlanBySelectorsSocial(COLLECTION_EXERCISE_ID);

    // Then call is made to correct url
    verify(restTemplate, times(1))
        .exchange(
            String.format("test:path?collectionExerciseId=%s", COLLECTION_EXERCISE_ID),
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<ActionPlanDTO>>() {});
    assertEquals(actionplan.getId(), actionPlans.get(0).getId());
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsSocialNoContentThrowsCTPException() throws CTPException {

    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.NO_CONTENT));

    // When
    actionSvcRestClient.getActionPlanBySelectorsSocial(COLLECTION_EXERCISE_ID);

    // Then CTPException is thrown
  }

  @Test(expected = CTPException.class)
  public void getActionPlanBySelectorsSocialMultiplePlansThrowsCTPException() throws CTPException {
    // Given
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setActionPlansPath("test:path");
    HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);
    when(appConfig.getActionSvc()).thenReturn(actionSvc);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionPlanDTO>>() {})))
        .thenReturn(
            new ResponseEntity<>(
                Arrays.asList(new ActionPlanDTO(), new ActionPlanDTO()), HttpStatus.OK));

    // When
    actionSvcRestClient.getActionPlanBySelectorsSocial(COLLECTION_EXERCISE_ID);

    // Then call is made to correct url
    verify(restTemplate, times(1))
        .exchange(
            String.format(
                "test:path?collectionExerciseId=%s&activeEnrolment=false", COLLECTION_EXERCISE_ID),
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<ActionPlanDTO>>() {});
  }
}
