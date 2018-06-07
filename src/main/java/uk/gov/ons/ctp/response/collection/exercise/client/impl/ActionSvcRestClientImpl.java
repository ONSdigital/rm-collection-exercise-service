package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

@Component
@Slf4j
public class ActionSvcRestClientImpl implements ActionSvcClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppConfig appConfig;

    @Qualifier("actionRestUtility")
    @Autowired
    private RestUtility restUtility;

    @Autowired
    public ActionSvcRestClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void createActionPlan(String name, String description) {
        log.debug("Attempting to post action plan to action service");
        UriComponents uriComponents = restUtility.createUriComponents(appConfig.getActionSvc().getActionsPath(),
                null);

        ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
        actionPlanDTO.setName(name);
        actionPlanDTO.setDescription(description);
        actionPlanDTO.setCreatedBy("SYSTEM");
        HttpEntity<ActionPlanDTO> httpEntity = restUtility.createHttpEntity(actionPlanDTO);


        restTemplate.exchange(
                uriComponents.toUri(),
                HttpMethod.POST,
                httpEntity,
                ActionPlanDTO.class
        );
        log.debug("Posted to action service to create action plan");
    }
}
