package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRulePostRequestDTO;
import uk.gov.ons.ctp.response.collection.exercise.client.impl.ActionSvcRestClientImpl;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

@RunWith(MockitoJUnitRunner.class)
public class ActionSvcClientImplTest {

  private static final String ACTION_PATH = "/actions";
  private static final String ACTION_RULE_PATH = "/actionrules";
  private static final String HTTP = "http";
  private static final String LOCALHOST = "localhost";
  private static final String ACTION_PLAN_NAME = "example";
  private static final String ACTION_PLAN_DESCRIPTION = "example description";

  @Mock private AppConfig appConfig;

  @InjectMocks private ActionSvcRestClientImpl actionSvcClient;

  @Mock private RestTemplate restTemplate;

  @Mock private RestUtility restUtility;

  /** Test that the action service is called with the correct details when creating action plan. */
  @Test
  public void testCreateActionPlan() {
    // Given
    ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionPlansPath(ACTION_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_PATH)
            .build();
    when(restUtility.createUriComponents(any(String.class), any(MultiValueMap.class)))
        .thenReturn(uriComponents);

    ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
    actionPlanDTO.setName(ACTION_PLAN_NAME);
    actionPlanDTO.setDescription(ACTION_PLAN_DESCRIPTION);
    actionPlanDTO.setCreatedBy("SYSTEM");
    HashMap<String, String> selectors = new HashMap<>();
    selectors.put("testSelector", "testValue");
    actionPlanDTO.setSelectors(selectors);

    HttpEntity httpEntity = new HttpEntity<>(actionPlanDTO, null);
    when(restUtility.createHttpEntity(any(ActionPlanDTO.class))).thenReturn(httpEntity);
    when(restTemplate.postForObject(
            eq(uriComponents.toUri()), eq(httpEntity), eq(ActionPlanDTO.class)))
        .thenReturn(actionPlanDTO);

    // When
    ActionPlanDTO createdActionPlanDTO =
        actionSvcClient.createActionPlan(ACTION_PLAN_NAME, ACTION_PLAN_DESCRIPTION, selectors);

    // Then
    verify(restTemplate)
        .postForObject(eq(uriComponents.toUri()), eq(httpEntity), eq(ActionPlanDTO.class));
    assertEquals(createdActionPlanDTO.getName(), ACTION_PLAN_NAME);
    assertEquals(createdActionPlanDTO.getDescription(), ACTION_PLAN_DESCRIPTION);
    assertEquals(createdActionPlanDTO.getCreatedBy(), "SYSTEM");
  }

  /**
   * Test that a rest client exception is thrown when issues contacting the action service to create
   * an action plan.
   */
  @Test(expected = RestClientException.class)
  public void testCreateActionPlanRestClientException() {
    // Given
    ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionPlansPath(ACTION_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_PATH)
            .build();
    when(restUtility.createUriComponents(any(String.class), any(MultiValueMap.class)))
        .thenReturn(uriComponents);

    ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
    actionPlanDTO.setName(ACTION_PLAN_NAME);
    actionPlanDTO.setDescription(ACTION_PLAN_DESCRIPTION);
    actionPlanDTO.setCreatedBy("SYSTEM");
    HashMap<String, String> selectors = new HashMap<>();
    selectors.put("testSelector", "testValue");
    actionPlanDTO.setSelectors(selectors);
    HttpEntity httpEntity = new HttpEntity<>(actionPlanDTO, null);
    when(restUtility.createHttpEntity(any(ActionPlanDTO.class))).thenReturn(httpEntity);
    when(restTemplate.postForObject(
            eq(uriComponents.toUri()), eq(httpEntity), eq(ActionPlanDTO.class)))
        .thenThrow(RestClientException.class);

    // When
    actionSvcClient.createActionPlan(ACTION_PLAN_NAME, ACTION_PLAN_DESCRIPTION, selectors);

    // Then RestClientException is thrown
  }

  /** Test that the action service is called with the correct details when creating action rules. */
  @Test
  public void testCreateActionRule() {
    // Given
    ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionRulesPath(ACTION_RULE_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_PATH)
            .build();
    when(restUtility.createUriComponents(any(String.class), any(MultiValueMap.class)))
        .thenReturn(uriComponents);

    ActionRulePostRequestDTO actionRulePostRequestDTO = getActionRulePostRequestDTO();

    ActionRuleDTO actionRuleDTO = getActionRuleDTO();

    HttpEntity httpEntity = new HttpEntity<>(actionRulePostRequestDTO, null);
    when(restUtility.createHttpEntity(any(ActionRulePostRequestDTO.class))).thenReturn(httpEntity);
    when(restTemplate.postForObject(
            eq(uriComponents.toUri()), eq(httpEntity), eq(ActionRuleDTO.class)))
        .thenReturn(actionRuleDTO);

    // When
    ActionRuleDTO createdActionRuleDTO =
        actionSvcClient.createActionRule(
            actionRulePostRequestDTO.getName(),
            actionRulePostRequestDTO.getDescription(),
            actionRulePostRequestDTO.getActionTypeName(),
            actionRulePostRequestDTO.getTriggerDateTime(),
            actionRulePostRequestDTO.getPriority(),
            actionRulePostRequestDTO.getActionPlanId());

    // Then
    verify(restTemplate)
        .postForObject(eq(uriComponents.toUri()), eq(httpEntity), eq(ActionRuleDTO.class));
    verify(restUtility).createHttpEntity(eq(actionRulePostRequestDTO));
    assertEquals(createdActionRuleDTO.getName(), actionRuleDTO.getName());
    assertEquals(createdActionRuleDTO.getDescription(), actionRuleDTO.getDescription());
    assertEquals(createdActionRuleDTO.getActionTypeName(), actionRuleDTO.getActionTypeName());
    assertEquals(createdActionRuleDTO.getId(), actionRuleDTO.getId());
  }

  /**
   * Test that a rest client exception is thrown when issues contacting the action service to create
   * an action rule.
   */
  @Test(expected = RestClientException.class)
  public void testCreateActionRuleRestClientException() {
    // Given
    ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionRulesPath(ACTION_RULE_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    when(restUtility.createUriComponents(any(String.class), any(MultiValueMap.class)))
        .thenReturn(UriComponentsBuilder.newInstance().build());

    when(restTemplate.postForObject(any(), any(), any())).thenThrow(RestClientException.class);

    // When
    actionSvcClient.createActionRule(
        "BSREM+45",
        "Enrolment Reminder Letter(+45 days)",
        "BSREM",
        OffsetDateTime.now().plusDays(45),
        3,
        UUID.fromString("003e587a-843f-11e8-adc0-fa7ae01bbebc"));

    // Then RestClientException is thrown
  }

  private ActionRulePostRequestDTO getActionRulePostRequestDTO() {
    ActionRulePostRequestDTO actionRulePostRequestDTO = new ActionRulePostRequestDTO();
    actionRulePostRequestDTO.setName("BSREM+45");
    actionRulePostRequestDTO.setActionTypeName("BSREM");
    actionRulePostRequestDTO.setDescription("Enrolment Reminder Letter(+45 days)");
    actionRulePostRequestDTO.setActionPlanId(
        UUID.fromString("003e587a-843f-11e8-adc0-fa7ae01bbebc"));
    actionRulePostRequestDTO.setTriggerDateTime(OffsetDateTime.now());
    actionRulePostRequestDTO.setPriority(3);
    return actionRulePostRequestDTO;
  }

  private ActionRuleDTO getActionRuleDTO() {
    ActionRuleDTO actionRuleDTO = new ActionRuleDTO();
    actionRuleDTO.setName("BSREM+45");
    actionRuleDTO.setActionTypeName("BSREM");
    actionRuleDTO.setDescription("Enrolment Reminder Letter(+45 days)");
    actionRuleDTO.setId(UUID.fromString("714356ba-7236-4179-8007-f09190eed323"));
    actionRuleDTO.setTriggerDateTime(OffsetDateTime.now());
    actionRuleDTO.setPriority(3);
    return actionRuleDTO;
  }
}
