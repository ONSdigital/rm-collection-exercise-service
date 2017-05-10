package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

/**
 * The REST endpoint controller for Collection Exercises.
 */
@RestController
@RequestMapping(value = "/collectionexercises", produces = "application/json")
@Slf4j
public class CollectionExerciseEndpoint {

  private static final String RETURN_SAMPLENOTFOUND = "Sample not found for collection exercise Id";

  @Autowired
  private SampleService sampleService;

  /**
   * PUT to manually trigger the request of the sample units from the sample
   * service for the given collection exercise Id.
   *
   * @param exerciseId Collection exercise Id for which to trigger delivery of
   *          sample units
   * @return total sample units to be delivered.
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/{exerciseId}", method = RequestMethod.PUT)
  public ResponseEntity<?> requestSampleUnits(@PathVariable("exerciseId") final Integer exerciseId)
      throws CTPException {
    log.debug("Entering collection exercise fetch with Id {}", exerciseId);
    SampleUnitsRequestDTO requestDTO = sampleService.requestSampleUnits(exerciseId);
    if (requestDTO == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_SAMPLENOTFOUND, exerciseId));
    }
    return ResponseEntity.ok(requestDTO);
  }
}
