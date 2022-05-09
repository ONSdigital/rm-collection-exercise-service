package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleSummaryService;

/** The REST endpoint controller for Collection Exercises. */
@RestController
@RequestMapping(value = "/collectionexerciseexecution", produces = "application/json")
public class CollectionExerciseExecutionEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(CollectionExerciseExecutionEndpoint.class);

  private static final String RETURN_SAMPLENOTFOUND = "Sample not found for collection exercise Id";

  @Autowired AppConfig appConfig;

  @Autowired private SampleSummaryService sampleSummaryService;

  /**
   * PUT to manually trigger the request of the sample units from the sample service for the given
   * collection exercise Id.
   *
   * @param id Collection exercise Id for which to trigger delivery of sample units
   * @return total sample units to be delivered.
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.POST)
  public ResponseEntity<Void> setReadyForLive(@PathVariable("id") final UUID id)
      throws CTPException {
    log.with("collection_exercise_id", id).debug("About to set collection exercise to live");

    sampleSummaryService.activateSamples(id);
    return ResponseEntity.ok().build();
  }
}
