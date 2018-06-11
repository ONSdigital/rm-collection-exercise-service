package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
     * @param restUtility for creating URI's and HTTPEntities
     * @param appConfig application config object
     */
    @Autowired
    public ActionSvcRestClientImpl(final RestTemplate restTemplate,
                                   final @Qualifier("actionRestUtility") RestUtility restUtility, AppConfig appConfig) {
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
        this.restUtility = restUtility;
    }

    @Retryable(value = {RestClientException.class}, maxAttemptsExpression = "#{${retries.maxAttempts}}",
            backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
    @Override
    public ActionPlanDTO createActionPlan(final String name, String description) throws RestClientException {
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
