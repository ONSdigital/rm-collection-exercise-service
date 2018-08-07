package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

/** HTTP RestClient implementation for calls to the Action service. */
@Component
@Slf4j
public class ActionSvcRestClientImpl implements ActionSvcClient {

  private AppConfig appConfig;

  private RestTemplate restTemplate;

  private RestUtility restUtility;

  /**
   * Implementation for request to action service to create action plan
   *
   * @param restTemplate Spring frameworks rest template
   * @param restUtility for creating URI's and HTTPEntities
   * @param appConfig application config object
   */
  @Autowired
  public ActionSvcRestClientImpl(
      final RestTemplate restTemplate,
      final @Qualifier("actionRestUtility") RestUtility restUtility,
      AppConfig appConfig) {
    this.restTemplate = restTemplate;
    this.appConfig = appConfig;
    this.restUtility = restUtility;
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public ActionPlanDTO createActionPlan(
      final String name, final String description, final HashMap<String, String> selectors)
      throws RestClientException {
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
    log.debug(
        "Successfully posted to action service to create action plan, ActionPlanId: {}",
        createdActionPlan.getId());
    return createdActionPlan;
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  @Override
  public List<ActionPlanDTO> getActionPlansBySelectors(
      String surveyRef, String exerciseRef, Boolean activeEnrolment) throws RestClientException {
    log.debug("Retrieving action plan for selectors");

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("surveyRef", surveyRef);
    queryParams.add("exerciseRef", exerciseRef);
    queryParams.add("activeEnrolment", activeEnrolment.toString());
    UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getActionSvc().getActionPlansPath(), queryParams);

    ResponseEntity<List<ActionPlanDTO>> responseEntity =
        restTemplate.exchange(
            uriComponents.toString(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ActionPlanDTO>>() {});

    log.debug("Successfully retrieved action plan for selectors");
    return responseEntity.getBody();
  }
}
