package uk.gov.ons.ctp.response.collection.exercise.client;

import static uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication.ACTION_PLAN_CACHE;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.*;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.representation.ProcessEventDTO;

/** HTTP RestClient implementation for calls to the Action service. */
@Component
public class ActionSvcClient {
  private static final Logger log = LoggerFactory.getLogger(ActionSvcClient.class);

  private static final String FOUND_NO_ACTION_PLANS =
      "Expected one action plan for selectors,"
          + " collectionExerciseId: %s, activeEnrolment: %b But None Found";
  private static final String FOUND_NO_ACTION_PLANS_2 =
      "Expected one action plan for selectors," + " collectionExerciseId: %s, But None Found";
  private static final String MULTIPLE_ACTION_PLANS_FOUND =
      "Expected one action plan for selectors,"
          + " collectionExerciseId: %s, activeEnrolment: %b But %d Found";
  private static final String MULTIPLE_ACTION_PLANS_FOUND_2 =
      "Expected one action plan for selectors," + " collectionExerciseId: %s, But %d Found";
  private static final String SELECTOR_COLLECTION_EXERCISE_ID = "collectionExerciseId";
  private static final String SELECTOR_ACTIVE_ENROLMENT = "activeEnrolment";

  private AppConfig appConfig;
  private RestTemplate restTemplate;
  private RestUtility restUtility;

  public ActionSvcClient(
      AppConfig appConfig,
      final RestTemplate restTemplate,
      final @Qualifier("actionRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Request action plan is created.
   *
   * @param name name of action plan
   * @param description description of action plan
   * @param selectors Map of selectors for actionplans as key value pairs
   * @return ActionPlanDTO representation of the created action plan
   */
  public ActionPlanDTO createActionPlan(
      final String name, final String description, final HashMap<String, String> selectors) {
    log.debug("Posting to action service to create action plan");
    UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getActionSvc().getActionPlansPath(), null);

    ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
    actionPlanDTO.setName(name);
    actionPlanDTO.setDescription(description);
    actionPlanDTO.setCreatedBy("SYSTEM");
    actionPlanDTO.setSelectors(selectors);
    HttpEntity<ActionPlanDTO> httpEntity = restUtility.createHttpEntity(actionPlanDTO);

    ActionPlanDTO createdActionPlan =
        restTemplate.postForObject(uriComponents.toUri(), httpEntity, ActionPlanDTO.class);
    log.with("action_plan_id", createdActionPlan.getId())
        .debug("Successfully posted to action service to create action plan");
    return createdActionPlan;
  }

  @Cacheable(ACTION_PLAN_CACHE)
  public ActionPlanDTO getActionPlanBySelectorsBusiness(
      String collectionExerciseId, boolean activeEnrolment) throws CTPException {

    final List<ActionPlanDTO> actionPlans =
        getActionPlansBySelectorsBusiness(collectionExerciseId, activeEnrolment);

    if (actionPlans == null) {
      log.with("collection_exercise_id", collectionExerciseId)
          .with("active_enrolment", activeEnrolment)
          .error("Retrieved no action plans");
      throw new CTPException(
          Fault.RESOURCE_NOT_FOUND,
          String.format(FOUND_NO_ACTION_PLANS, collectionExerciseId, activeEnrolment));
    }

    if (actionPlans.size() != 1) {
      log.with("collection_exercise_id", collectionExerciseId)
          .with("active_enrolment", activeEnrolment)
          .error("Retrieved more than one action plan");
      throw new CTPException(
          Fault.RESOURCE_NOT_FOUND,
          String.format(
              MULTIPLE_ACTION_PLANS_FOUND,
              collectionExerciseId,
              activeEnrolment,
              actionPlans.size()));
    }

    return actionPlans.get(0);
  }

  private List<ActionPlanDTO> getActionPlansBySelectorsBusiness(
      final String collectionExerciseId, final Boolean activeEnrolment) {
    log.with("collection_exercise_id", collectionExerciseId)
        .with("active_enrolment", activeEnrolment)
        .debug("Retrieving action plan for selectors");

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add(SELECTOR_COLLECTION_EXERCISE_ID, collectionExerciseId);
    queryParams.add(SELECTOR_ACTIVE_ENROLMENT, Boolean.toString(activeEnrolment));
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getActionSvc().getActionPlansPath(), queryParams);

    HttpEntity httpEntity = restUtility.createHttpEntity(null);

    final ResponseEntity<List<ActionPlanDTO>> responseEntity =
        restTemplate.exchange(
            uriComponents.toString(),
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<ActionPlanDTO>>() {});

    log.with("collection_exercise_id", collectionExerciseId)
        .with("active_enrolment", activeEnrolment)
        .debug("Successfully retrieved action plans for selectors");
    return responseEntity.getBody();
  }

  /**
   * Request action rule is created.
   *
   * @param name name of action rule
   * @param description description of action rule
   * @param actionTypeName name of action type
   * @param triggerDateTime date time to trigger action rule
   * @param priority priority number
   * @param actionPlanId uuid of the action plan this action type is for
   * @return ActionRuleDTO representation of the created action plan
   * @throws RestClientException for failed connection to action service
   */
  public ActionRuleDTO createActionRule(
      final String name,
      final String description,
      final ActionType actionTypeName,
      final OffsetDateTime triggerDateTime,
      final int priority,
      final UUID actionPlanId)
      throws RestClientException {
    log.debug("Posting to action service to create action rule");
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getActionSvc().getActionRulesPath(), null);

    final ActionRulePostRequestDTO actionRulePostRequestDTO = new ActionRulePostRequestDTO();
    actionRulePostRequestDTO.setName(name);
    actionRulePostRequestDTO.setActionTypeName(actionTypeName);
    actionRulePostRequestDTO.setDescription(description);
    actionRulePostRequestDTO.setActionPlanId(actionPlanId);
    actionRulePostRequestDTO.setTriggerDateTime(triggerDateTime);
    actionRulePostRequestDTO.setPriority(priority);

    final HttpEntity<ActionRulePostRequestDTO> httpEntity =
        restUtility.createHttpEntity(actionRulePostRequestDTO);
    final ActionRuleDTO createdActionRule =
        restTemplate.postForObject(uriComponents.toUri(), httpEntity, ActionRuleDTO.class);
    log.with("action_plan_id", actionPlanId)
        .with("action_rule_id", createdActionRule.getId().toString())
        .debug("Successfully posted to action service to create action rule");
    return createdActionRule;
  }

  public ActionRuleDTO updateActionRule(
      final UUID actionRuleId,
      final String name,
      final String description,
      final OffsetDateTime triggerDateTime,
      final int priority)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getActionSvc().getActionRulePath(), null, actionRuleId);

    final ActionRulePutRequestDTO actionRulePutRequestDTO = new ActionRulePutRequestDTO();
    actionRulePutRequestDTO.setName(name);
    actionRulePutRequestDTO.setDescription(description);
    actionRulePutRequestDTO.setTriggerDateTime(triggerDateTime);
    actionRulePutRequestDTO.setPriority(priority);

    final HttpEntity<ActionRulePutRequestDTO> httpEntity =
        restUtility.createHttpEntity(actionRulePutRequestDTO);
    return restTemplate
        .exchange(uriComponents.toUri(), HttpMethod.PUT, httpEntity, ActionRuleDTO.class)
        .getBody();
  }

  public ActionPlanDTO updateActionPlanNameAndDescription(
      final UUID actionPlanUUID, final String name, final String description)
      throws RestClientException {
    final ActionPlanPutRequestDTO actionPlanPutRequestDTO = new ActionPlanPutRequestDTO();
    actionPlanPutRequestDTO.setDescription(description);
    actionPlanPutRequestDTO.setName(name);
    return updateActionPlan(actionPlanUUID, actionPlanPutRequestDTO);
  }

  private ActionPlanDTO updateActionPlan(
      final UUID actionPlanUUID, final ActionPlanPutRequestDTO actionPlanPutRequestDTO)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getActionSvc().getActionPlanPath(), null, actionPlanUUID);

    final HttpEntity<ActionPlanPutRequestDTO> httpEntity =
        restUtility.createHttpEntity(actionPlanPutRequestDTO);
    try {
      log.with("actionPlanId", actionPlanUUID).debug("Updating action plan");
      return restTemplate
          .exchange(uriComponents.toUri(), HttpMethod.PUT, httpEntity, ActionPlanDTO.class)
          .getBody();
    } catch (HttpClientErrorException e) {
      log.error("Failed to update action plan", e);
      throw e;
    }
  }

  public HttpStatus deleteActionRule(
      final UUID actionRuleId,
      final String name,
      final String description,
      final OffsetDateTime triggerDateTime,
      final int priority)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getActionSvc().getActionRulePath(), null, actionRuleId);

    final ActionRulePostRequestDTO actionRuleDeleteRequestDTO = new ActionRulePostRequestDTO();
    actionRuleDeleteRequestDTO.setActionPlanId(actionRuleId);
    actionRuleDeleteRequestDTO.setName(name);
    actionRuleDeleteRequestDTO.setDescription(description);
    actionRuleDeleteRequestDTO.setTriggerDateTime(triggerDateTime);
    actionRuleDeleteRequestDTO.setPriority(priority);
    log.with("actionRuleId", actionRuleId).info("About to delete action rule");
    final HttpEntity<ActionRulePostRequestDTO> httpEntity =
        restUtility.createHttpEntity(actionRuleDeleteRequestDTO);
    return restTemplate
        .exchange(uriComponents.toUri(), HttpMethod.DELETE, httpEntity, ActionRuleDTO.class)
        .getStatusCode();
  }

  public List<ActionRuleDTO> getActionRulesForActionPlan(final UUID actionPlanId)
      throws CTPException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getActionSvc().getActionRulesForActionPlanPath(), null, actionPlanId);

    ResponseEntity<List<ActionRuleDTO>> response;
    HttpEntity httpEntity = restUtility.createHttpEntity(null);

    try {
      response =
          restTemplate.exchange(
              uriComponents.toUri(),
              HttpMethod.GET,
              httpEntity,
              new ParameterizedTypeReference<List<ActionRuleDTO>>() {});
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new CTPException(
            Fault.SYSTEM_ERROR, String.format("Action Plan %s not found", actionPlanId));
      }
      throw e;
    }

    return response.getBody();
  }

  /**
   * Request for an event to be process in case
   *
   * @param tag The tag of the event (i.e., mps, go_live, return_by)
   * @param collectionExerciseId The id of the collection exercise the event relates too.
   */
  public boolean processEvent(final String tag, final UUID collectionExerciseId)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getActionSvc().getProcessEventPath(), null);

    final ProcessEventDTO processEventDTO = new ProcessEventDTO();
    processEventDTO.setTag(tag);
    processEventDTO.setCollectionExerciseId(collectionExerciseId);
    final HttpEntity<ProcessEventDTO> httpEntity = restUtility.createHttpEntity(processEventDTO);
    final ResponseEntity<String> response =
        restTemplate.postForEntity(uriComponents.toUri(), httpEntity, String.class);
    return response.getStatusCode().is2xxSuccessful();
  }

  public boolean isDeprecated() {
    return appConfig.getActionSvc().isDeprecated();
  }
}
