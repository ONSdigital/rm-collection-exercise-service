package uk.gov.ons.ctp.response.collection.exercise.client;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;

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
   * Get action plans for the given selectors
   *
   * @param surveyRef surveyRef for action plan to be retrieved
   * @param exerciseRef surveyRef for action plan to be retrieved
   * @param activeEnrolment boolean for if the sampleunit already has an active enrolment
   * @return List of ActionPlanDTO representations of matching action plans
   * @throws RestClientException for failed connection to action service
   */
  List<ActionPlanDTO> getActionPlansBySelectors(
      String surveyRef, String exerciseRef, Boolean activeEnrolment) throws RestClientException;

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
      String actionTypeName,
      OffsetDateTime triggerDateTime,
      int priority,
      UUID actionPlanId)
      throws RestClientException;
}
