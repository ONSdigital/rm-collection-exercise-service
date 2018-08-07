package uk.gov.ons.ctp.response.collection.exercise.client;

import java.util.HashMap;
import java.util.List;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;

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
}
