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
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

/** The REST endpoint controller for Collection Exercises. */
@RestController
@RequestMapping(value = "/collectionexerciseexecution", produces = "application/json")
public class CollectionExerciseExecutionEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(CollectionExerciseExecutionEndpoint.class);

  private static final String RETURN_SAMPLENOTFOUND = "Sample not found for collection exercise Id";

  @Autowired private SampleService sampleService;

  /**
   * PUT to manually trigger the request of the sample units from the sample service for the given
   * collection exercise Id.
   *
   * @param id Collection exercise Id for which to trigger delivery of sample units
   * @return total sample units to be delivered.
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.POST)
  public ResponseEntity<SampleUnitsRequestDTO> requestSampleUnits(@PathVariable("id") final UUID id)
      throws CTPException {
    log.debug("Entering collection exercise fetch with Id {}", id);
    SampleUnitsRequestDTO requestDTO = sampleService.requestSampleUnits(id);
    if (requestDTO == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND, String.format("%s %s", RETURN_SAMPLENOTFOUND, id));
    }
    return ResponseEntity.ok(requestDTO);
  }
}
