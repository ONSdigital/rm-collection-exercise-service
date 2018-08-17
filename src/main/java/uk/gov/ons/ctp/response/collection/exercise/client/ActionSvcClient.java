package uk.gov.ons.ctp.response.collection.exercise.client;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;

/** HTTP RestClient implementation for calls to the Action service. */
public interface ActionSvcClient {

  /**
   * Request action plan is created.
   *
   * @param name name of action plan
   * @param description description of action plan
   * @param selectors Map of selectors for actionplans as key value pairs
   * @return ActionPlanDTO representation of the created action plan
   * @throws RestClientException for failed connection to action service
   */
  ActionPlanDTO createActionPlan(String name, String description, HashMap<String, String> selectors)
      throws RestClientException;

  /**
   * Request list of business action plans with given selectors
   *
   * @param collectionExerciseId collectionExerciseId to find action plans for
   * @param activeEnrolment boolean for if sample unit has an active enrolment associated with it
   * @return List<ActionPlanDTO> representation of the created action plan
   */
  List<ActionPlanDTO> getActionPlansBySelectorsBusiness(
      String collectionExerciseId, Boolean activeEnrolment);

  /**
   * Request list of social action plans with given selectors
   *
   * @param collectionExerciseId collectionExerciseId to find action plans for
   * @return List<ActionPlanDTO> representation of the created action plan
   */
  List<ActionPlanDTO> getActionPlansBySelectorsSocial(String collectionExerciseId);

  /**
   * Request single action plan with given selectors
   *
   * @param collectionExerciseId collectionExerciseId to find action plans for
   * @param activeEnrolment boolean for if sample unit has an active enrolment associated with it
   * @return ActionPlanDTO representation of the created action plan
   * @throws CTPException Resource Not Found if not exactly one action plan found
   */
  ActionPlanDTO getActionPlanBySelectorsBusiness(
      String collectionExerciseId, boolean activeEnrolment) throws CTPException;

  /**
   * Request single action plan with given selectors
   *
   * @param collectionExerciseId collectionExerciseId to find action plans for
   * @return ActionPlanDTO representation of the created action plan
   * @throws CTPException Resource Not Found if not exactly one action plan found
   */
  ActionPlanDTO getActionPlanBySelectorsSocial(String collectionExerciseId) throws CTPException;

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
  ActionRuleDTO createActionRule(
      String name,
      String description,
      ActionType actionTypeName,
      OffsetDateTime triggerDateTime,
      int priority,
      UUID actionPlanId)
      throws RestClientException;

  ActionRuleDTO updateActionRule(
      UUID id, String name, String description, OffsetDateTime triggerDateTime, int priority)
      throws RestClientException;

  List<ActionRuleDTO> getActionRulesForActionPlan(UUID actionPlanId) throws CTPException;
}
