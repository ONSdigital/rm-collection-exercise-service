package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRulePostRequestDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRulePutRequestDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

@RunWith(MockitoJUnitRunner.class)
public class ActionSvcClientTest {

  private static final String ACTION_PATH = "/actions";
  private static final String ACTION_RULES_PATH = "/actionrules";
  private static final String ACTION_RULE_PATH = "/actionrules/{actionRuleId}";
  private static final String HTTP = "http";
  private static final String LOCALHOST = "localhost";
  private static final String ACTION_PLAN_NAME = "example";
  private static final String ACTION_PLAN_DESCRIPTION = "example description";
  private static final String ACTION_RULES_FOR_PLAN_PATH = "/actionrules/actionplan/{actionPlanId}";
  private static final String ACTION_RULE_NAME = "BSREM+45";
  private static final String ACTION_RULE_DESCRIPTION = "Enrolment Reminder Letter(+45 days)";
  private static final UUID ACTION_RULE_ID =
      UUID.fromString("714356ba-7236-4179-8007-f09190eed323");
  private static final int ACTION_RULE_PRIORITY = 3;
  private static final OffsetDateTime ACTION_RULE_TRIGGER_DATE_TIME = OffsetDateTime.now();
  private static final UUID ACTION_PLAN_ID =
      UUID.fromString("003e587a-843f-11e8-adc0-fa7ae01bbebc");

  @Mock private AppConfig appConfig;

  @InjectMocks private ActionSvcClient actionSvcClient;

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
    actionSvcConfig.setActionRulesPath(ACTION_RULES_PATH);
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
    actionSvcConfig.setActionRulesPath(ACTION_RULES_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    when(restUtility.createUriComponents(any(String.class), any(MultiValueMap.class)))
        .thenReturn(UriComponentsBuilder.newInstance().build());

    when(restTemplate.postForObject(any(), any(), any())).thenThrow(RestClientException.class);

    // When
    actionSvcClient.createActionRule(
        ACTION_RULE_NAME,
        ACTION_RULE_DESCRIPTION,
        ActionType.BSREM,
        OffsetDateTime.now().plusDays(45),
        ACTION_RULE_PRIORITY,
        ACTION_RULE_ID);

    // Then RestClientException is thrown
  }

  /** Test that the action service is called with the correct details when creating action rules. */
  @Test
  public void testUpdateActionRule() {
    // Given
    final ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionRulePath(ACTION_RULE_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    final UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_RULE_PATH)
            .build();
    when(restUtility.createUriComponents(eq(ACTION_RULE_PATH), eq(null), eq(ACTION_RULE_ID)))
        .thenReturn(uriComponents);

    final ActionRulePutRequestDTO actionRulePutRequestDTO = getActionRulePutRequestDTO();
    final ActionRuleDTO actionRuleDTO = getActionRuleDTO();

    final HttpEntity httpEntity = new HttpEntity<>(actionRulePutRequestDTO, null);
    when(restUtility.createHttpEntity(any(ActionRulePutRequestDTO.class))).thenReturn(httpEntity);
    when(restTemplate.exchange(
            eq(uriComponents.toUri()), eq(HttpMethod.PUT), eq(httpEntity), eq(ActionRuleDTO.class)))
        .thenReturn(new ResponseEntity<>(actionRuleDTO, HttpStatus.OK));

    // When
    final ActionRuleDTO updateActionRule =
        actionSvcClient.updateActionRule(
            ACTION_RULE_ID,
            ACTION_RULE_NAME,
            ACTION_RULE_DESCRIPTION,
            ACTION_RULE_TRIGGER_DATE_TIME,
            ACTION_RULE_PRIORITY);

    // Then
    assertThat(actionRuleDTO.getName(), is(updateActionRule.getName()));
    assertThat(actionRuleDTO.getDescription(), is(updateActionRule.getDescription()));
    assertThat(actionRuleDTO.getActionTypeName(), is(updateActionRule.getActionTypeName()));
    assertThat(actionRuleDTO.getId(), is(updateActionRule.getId()));
  }

  /**
   * Test that a rest client exception is thrown when issues contacting the action service to create
   * an action rule.
   */
  @Test(expected = RestClientException.class)
  public void testUpdateActionRuleRestClientException() {
    // Given
    final ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionRulePath(ACTION_RULE_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    final UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_RULE_PATH)
            .build();
    when(restUtility.createUriComponents(eq(ACTION_RULE_PATH), eq(null), eq(ACTION_RULE_ID)))
        .thenReturn(uriComponents);

    final ActionRulePutRequestDTO actionRulePutRequestDTO = getActionRulePutRequestDTO();

    final HttpEntity httpEntity = new HttpEntity<>(actionRulePutRequestDTO, null);
    when(restUtility.createHttpEntity(any(ActionRulePutRequestDTO.class))).thenReturn(httpEntity);
    when(restTemplate.exchange(
            eq(uriComponents.toUri()), eq(HttpMethod.PUT), eq(httpEntity), eq(ActionRuleDTO.class)))
        .thenThrow(new RestClientException("Err"));

    // When
    actionSvcClient.updateActionRule(
        ACTION_RULE_ID,
        ACTION_RULE_NAME,
        ACTION_RULE_DESCRIPTION,
        ACTION_RULE_TRIGGER_DATE_TIME,
        ACTION_RULE_PRIORITY);
    // Then RestClientException is thrown
  }

  @Test
  public void testGetActionRulesForActionPlan() throws CTPException {
    // Given
    final ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionRulesForActionPlanPath(ACTION_RULES_FOR_PLAN_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    final UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_RULES_FOR_PLAN_PATH)
            .build();
    final UUID actionPlanId = UUID.randomUUID();
    when(restUtility.createUriComponents(
            endsWith(ACTION_RULES_FOR_PLAN_PATH), eq(null), eq(actionPlanId)))
        .thenReturn(uriComponents);

    final HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);

    final List<ActionRuleDTO> actionRuleDTOs = new ArrayList<>();
    when(restTemplate.exchange(
            eq(uriComponents.toUri()),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionRuleDTO>>() {})))
        .thenReturn(new ResponseEntity<>(actionRuleDTOs, HttpStatus.OK));

    // When
    final List<ActionRuleDTO> returnedActionRuleDTOs =
        actionSvcClient.getActionRulesForActionPlan(actionPlanId);

    // Then
    assertThat(returnedActionRuleDTOs, is(actionRuleDTOs));
  }

  @Test(expected = RestClientException.class)
  public void testGetActionRulesForActionPlanRestClientException() throws CTPException {
    // Given
    final ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionRulesForActionPlanPath(ACTION_RULES_FOR_PLAN_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    final UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_RULES_FOR_PLAN_PATH)
            .build();
    final UUID actionPlanId = UUID.randomUUID();
    when(restUtility.createUriComponents(any(String.class), eq(null), eq(actionPlanId)))
        .thenReturn(uriComponents);

    final HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);

    when(restTemplate.exchange(
            eq(uriComponents.toUri()),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionRuleDTO>>() {})))
        .thenThrow(new RestClientException("Error"));

    // When
    actionSvcClient.getActionRulesForActionPlan(actionPlanId);
  }

  @Test(expected = CTPException.class)
  public void testGetActionRulesForActionPlanRaisesCTPExceptionOn404() throws CTPException {
    // Given
    final ActionSvc actionSvcConfig = new ActionSvc();
    actionSvcConfig.setActionRulesForActionPlanPath(ACTION_RULES_FOR_PLAN_PATH);
    when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

    final UriComponents uriComponents =
        UriComponentsBuilder.newInstance()
            .scheme(HTTP)
            .host(LOCALHOST)
            .port(80)
            .path(ACTION_RULES_FOR_PLAN_PATH)
            .build();
    final UUID actionPlanId = UUID.randomUUID();
    when(restUtility.createUriComponents(any(String.class), eq(null), eq(actionPlanId)))
        .thenReturn(uriComponents);

    final HttpEntity httpEntity = new HttpEntity(null, null);
    when(restUtility.createHttpEntity(any())).thenReturn(httpEntity);

    when(restTemplate.exchange(
            eq(uriComponents.toUri()),
            eq(HttpMethod.GET),
            eq(httpEntity),
            eq(new ParameterizedTypeReference<List<ActionRuleDTO>>() {})))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    actionSvcClient.getActionRulesForActionPlan(actionPlanId);
  }

  private ActionRulePutRequestDTO getActionRulePutRequestDTO() {
    final ActionRulePutRequestDTO actionRulePutRequestDTO = new ActionRulePutRequestDTO();
    actionRulePutRequestDTO.setName(ACTION_RULE_NAME);
    actionRulePutRequestDTO.setDescription(ACTION_RULE_DESCRIPTION);
    actionRulePutRequestDTO.setTriggerDateTime(ACTION_RULE_TRIGGER_DATE_TIME);
    actionRulePutRequestDTO.setPriority(ACTION_RULE_PRIORITY);
    return actionRulePutRequestDTO;
  }

  private ActionRulePostRequestDTO getActionRulePostRequestDTO() {
    final ActionRulePostRequestDTO actionRulePostRequestDTO = new ActionRulePostRequestDTO();
    actionRulePostRequestDTO.setName(ACTION_RULE_NAME);
    actionRulePostRequestDTO.setActionTypeName(ActionType.BSREM);
    actionRulePostRequestDTO.setDescription(ACTION_RULE_DESCRIPTION);
    actionRulePostRequestDTO.setActionPlanId(ACTION_PLAN_ID);
    actionRulePostRequestDTO.setTriggerDateTime(ACTION_RULE_TRIGGER_DATE_TIME);
    actionRulePostRequestDTO.setPriority(ACTION_RULE_PRIORITY);
    return actionRulePostRequestDTO;
  }

  private ActionRuleDTO getActionRuleDTO() {
    final ActionRuleDTO actionRuleDTO = new ActionRuleDTO();
    actionRuleDTO.setName(ACTION_RULE_NAME);
    actionRuleDTO.setActionTypeName(ActionType.BSREM);
    actionRuleDTO.setDescription(ACTION_RULE_DESCRIPTION);
    actionRuleDTO.setId(ACTION_PLAN_ID);
    actionRuleDTO.setTriggerDateTime(ACTION_RULE_TRIGGER_DATE_TIME);
    actionRuleDTO.setPriority(ACTION_RULE_PRIORITY);
    return actionRuleDTO;
  }
}
