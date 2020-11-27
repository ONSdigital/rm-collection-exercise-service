package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.*;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;

/** HTTP RestClient implementation for calls to the Action service. */
@Component
public class CaseSvcClient {
  private static final Logger log = LoggerFactory.getLogger(CaseSvcClient.class);

  private final AppConfig appConfig;
  private final RestTemplate restTemplate;
  private final RestUtility restUtility;

  public CaseSvcClient(
      AppConfig appConfig,
      final RestTemplate restTemplate,
      final @Qualifier("actionRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Request for an event to be executed in case
   *
   * @param name name of action plan
   * @param description description of action plan
   * @return ActionPlanDTO representation of the created action plan
   */
  public ActionRuleDTO executeEvent(final String tag, final String description)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getCaseSvc().getExecuteEventsPath(), null);

    final ActionRulePutRequestDTO actionRulePutRequestDTO = new ActionRulePutRequestDTO();
    actionRulePutRequestDTO.setDescription(description);

    final HttpEntity<ActionRulePutRequestDTO> httpEntity =
        restUtility.createHttpEntity(actionRulePutRequestDTO);
    return restTemplate
        .exchange(uriComponents.toUri(), HttpMethod.PUT, httpEntity, ActionRuleDTO.class)
        .getBody();
  }

  public boolean isDeprecated() {
    return appConfig.getActionSvc().isDeprecated();
  }
}
