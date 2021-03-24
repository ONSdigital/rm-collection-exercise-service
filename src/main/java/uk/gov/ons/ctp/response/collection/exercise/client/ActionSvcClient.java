package uk.gov.ons.ctp.response.collection.exercise.client;

import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.*;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;

/** HTTP RestClient implementation for calls to the Action service. */
@Component
public class ActionSvcClient {

  private AppConfig appConfig;
  private RestTemplate restTemplate;
  private RestUtility restUtility;

  public ActionSvcClient(
      AppConfig appConfig,
      final RestTemplate restTemplate,
      final @Qualifier("actionRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Request for an event to be processed in action. Processing an event means doing any physical
   * actions that might need to be done, such as sending a letter or an email relating to the event.
   *
   * @param tag The tag of the event (i.e., mps, go_live, return_by)
   * @param collectionExerciseId The id of the collection exercise the event relates too.
   */
  public boolean processEvent(final String tag, final UUID collectionExerciseId)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getActionSvc().getProcessEventPath(), null);

    final Event event = new Event();
    event.setCollectionExerciseID(collectionExerciseId);
    event.setTag(Event.EventTag.valueOf(tag));
    final HttpEntity<Event> httpEntity = restUtility.createHttpEntity(event);
    final ResponseEntity<String> response =
        restTemplate.postForEntity(uriComponents.toUri(), httpEntity, String.class);
    return response.getStatusCode().is2xxSuccessful();
  }
}
