package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
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
      final @Qualifier("caseRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Request for an event to be executed in case
   *
   * @param tag The tag of the event (i.e., mps, go_live, return_by)
   * @param collectionExerciseId The id of the collection exercise the event relates too.
   * @return ActionPlanDTO representation of the created action plan
   */
  public ActionRuleDTO executeEvent(final String tag, final UUID collectionExerciseId)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getCaseSvc().getExecuteEventsPath(), null);

    final ActionRulePutRequestDTO actionRulePutRequestDTO = new ActionRulePutRequestDTO();

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
