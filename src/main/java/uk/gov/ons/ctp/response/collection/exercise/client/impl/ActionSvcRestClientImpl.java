package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

/**
 * HTTP RestClient implementation for calls to the Action service.
 *
 */
@Component
@Slf4j
public class ActionSvcRestClientImpl implements ActionSvcClient {

    private AppConfig appConfig;

    private RestTemplate restTemplate;

    private RestUtility restUtility;

    /**
     * Implementation for request to action service to create action plan
     * @param restTemplate Spring frameworks rest template
     * @param appConfig description of action plan
     * @param restUtility fro creating URI's and HTTPEntities
     */
    @Autowired
    public ActionSvcRestClientImpl(RestTemplate restTemplate,
                                   @Qualifier("actionRestUtility") RestUtility restUtility, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
        this.restUtility = restUtility;
    }

    /**
     * Implementation for request to action service to create action plan
     * @param name name of action plan
     * @param description description of action plan
     * @return action plan
     */
    @Override
    public ActionPlanDTO createActionPlan(String name, String description) throws RestClientException {
        log.debug("Posting to action service to create action plan");
        UriComponents uriComponents = restUtility.createUriComponents(appConfig.getActionSvc().getActionPlansPath(),
                null);

        ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
        actionPlanDTO.setName(name);
        actionPlanDTO.setDescription(description);
        actionPlanDTO.setCreatedBy("SYSTEM");
        HttpEntity<ActionPlanDTO> httpEntity = restUtility.createHttpEntity(actionPlanDTO);

        ActionPlanDTO createdActionPlan = restTemplate.postForObject(uriComponents.toUri(),
                httpEntity, ActionPlanDTO.class);
        log.debug("Successfully posted to action service to create action plan, ActionPlanIn: %s",
                createdActionPlan.getId());
        return createdActionPlan;
    }
}
