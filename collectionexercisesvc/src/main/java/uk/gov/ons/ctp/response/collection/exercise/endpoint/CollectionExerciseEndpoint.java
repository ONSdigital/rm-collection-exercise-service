package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import java.math.BigInteger;
import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * The REST endpoint controller for Collection Exercises.
 */
@RestController
@RequestMapping(value = "/collectionexercises", produces = "application/json")
@Slf4j
public class CollectionExerciseEndpoint {

  /**
   * PUT to manually trigger the request of the sample units from the sample
   * service for the given collection exercise Id.
   *
   * @param exerciseId Collection exercise Id for which to trigger delivery of
   *          sample units
   * @return total sample units to be delivered.
   * 
   */
  @RequestMapping(value = "/{exerciseId}", method = RequestMethod.PUT)
  public ResponseEntity<?> requestSampleUnits(@PathVariable("exerciseId") final BigInteger exerciseId) {
    log.debug("Entering collection exercise fetch with Id {}", exerciseId);

    return ResponseEntity.created(URI.create("TODO")).body("TODO");
  }
}
