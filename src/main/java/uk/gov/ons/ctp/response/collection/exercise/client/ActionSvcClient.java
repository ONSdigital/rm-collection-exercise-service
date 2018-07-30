package uk.gov.ons.ctp.response.collection.exercise.client;

import java.util.HashMap;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;

/** HTTP RestClient implementation for calls to the Action service. */
public interface ActionSvcClient {

  /**
   * Request action plan is created.
   *
   * @param name name of action plan
   * @param description description of action plan
   * @return ActionPlanDTO representation of the created action plan
   * @throws RestClientException for failed connection to action service
   */
  ActionPlanDTO createActionPlan(String name, String description, HashMap<String, String> selectors)
      throws RestClientException;
}
