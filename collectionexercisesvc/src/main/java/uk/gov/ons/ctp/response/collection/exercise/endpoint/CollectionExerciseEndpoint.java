package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExerciseSummary;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * The REST endpoint controller for Collection Exercises.
 */
@RestController
@RequestMapping(value = "/collectionexercises", produces = "application/json")
@Slf4j
public class CollectionExerciseEndpoint {

  private static final String RETURN_SAMPLENOTFOUND = "Sample not found for collection exercise Id";
  private static final String RETURN_COLLECTIONEXERCISESNOTFOUND = "Collection Exercises not found for collection survey Id";
  private static final String RETURN_COLLECTIONEXERCISENOTFOUND = "Collection Exercise not found for collection exercise Id";
  private static final String RETURN_SURVEYNOTFOUND = "Survey not found for survey Id";

  @Autowired
  private SampleService sampleService;

  @Autowired
  private CollectionExerciseService collectionExerciseService;

  @Autowired
  private SurveyService surveyService;

  /**
   * GET to request collection exercises from the collection exercise service
   * for the given collection survey Id.
   *
   * @param id survey Id for which to trigger delivery of
   *          collection exercises
   * @return list of collection exercises associated to survey
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/survey/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> requestCollectionExercisesForSurvey(@PathVariable("id") final String id)
          throws CTPException {

    Survey survey = surveyService.requestSurvey(UUID.fromString(id));

    List<CollectionExerciseSummary> collectionExerciseSummaryList;

    if (survey == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
              String.format("%s %s", RETURN_SURVEYNOTFOUND, id));
    } else {
      log.debug("Entering collection exercise fetch with survey Id {}", id);
      collectionExerciseSummaryList = collectionExerciseService.requestCollectionExerciseSummariesForSurvey(survey);
      if (collectionExerciseSummaryList.isEmpty()) {
        throw new CTPException(CTPException.Fault.NO_CONTENT,
                String.format("%s %s", RETURN_COLLECTIONEXERCISESNOTFOUND, id));
      }
    }
    return ResponseEntity.ok(collectionExerciseSummaryList);
  }

  /**
   * GET to request collection exercise from the collection exercise service
   * for the given collection exercise Id.
   *
   * @param id collection exercise Id for which to trigger delivery of
   *          collection exercise
   * @return collection exercise associated to collection exercise id
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getCollectionExercise(@PathVariable("id") final String id)
          throws CTPException {
    log.debug("Entering collection exercise fetch with collection exercise Id {}", id);
    CollectionExercise collectionExercise = collectionExerciseService.requestCollectionExercise(id);
    if (collectionExercise == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
              String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, id));
    }
    return ResponseEntity.ok(collectionExercise);
  }

  /**
   * PUT to manually trigger the request of the sample units from the sample
   * service for the given collection exercise Id.
   *
   * @param id Collection exercise Id for which to trigger delivery of
   *          sample units
   * @return total sample units to be delivered.
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> requestSampleUnits(@PathVariable("id") final String id)
      throws CTPException {
    log.debug("Entering collection exercise fetch with Id {}", id);
    SampleUnitsRequestDTO requestDTO = sampleService.requestSampleUnits(id);
    if (requestDTO == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_SAMPLENOTFOUND, id));
    }
    return ResponseEntity.ok(requestDTO);
  }
}
