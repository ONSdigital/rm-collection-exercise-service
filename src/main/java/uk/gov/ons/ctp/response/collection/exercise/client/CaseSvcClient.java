package uk.gov.ons.ctp.response.collection.exercise.client;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
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
import uk.gov.ons.ctp.response.collection.exercise.representation.ProcessEventDTO;

/** HTTP RestClient implementation for calls to the Case service. */
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
   * Request for an event to be process in case
   *
   * @param tag The tag of the event (i.e., mps, go_live, return_by)
   * @param collectionExerciseId The id of the collection exercise the event relates too.
   */
  public boolean processEvent(final String tag, final UUID collectionExerciseId)
      throws RestClientException {
    final UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getCaseSvc().getProcessEventPath(), null);

    final ProcessEventDTO processEventDTO = new ProcessEventDTO();
    processEventDTO.setTag(tag);
    processEventDTO.setCollectionExerciseId(collectionExerciseId);
    final HttpEntity<ProcessEventDTO> httpEntity = restUtility.createHttpEntity(processEventDTO);
    final ResponseEntity<String> response =
        restTemplate.postForEntity(uriComponents.toUri(), httpEntity, String.class);
    log.info(String.valueOf(response));
    return response.getStatusCode().is2xxSuccessful();
  }
}
