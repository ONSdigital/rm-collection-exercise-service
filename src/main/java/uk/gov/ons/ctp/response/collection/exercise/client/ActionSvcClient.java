package uk.gov.ons.ctp.response.collection.exercise.client;

import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;

/**
 * Service responsible for making client calls to the Action service
 *
 */
public interface ActionSvcClient {

    /**
        Request action plan is created.
        @param name name of action plan
        @param description description of action plan
     */
    ActionPlanDTO  createActionPlan(String name, String description) throws RestClientException;
}