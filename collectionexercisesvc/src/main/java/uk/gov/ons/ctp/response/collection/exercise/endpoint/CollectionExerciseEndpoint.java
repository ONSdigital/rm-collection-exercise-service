package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExerciseSummary;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.representation.CaseTypeDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

import java.util.ArrayList;
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
  private static final String RETURN_COLLECTIONEXERCISENOTFOUND = "Collection Exercise not found for collection exercise Id";
  private static final String RETURN_SURVEYNOTFOUND = "Survey not found for survey Id";

  @Autowired
  private SampleService sampleService;

  @Autowired
  private CollectionExerciseService collectionExerciseService;

  @Autowired
  private SurveyService surveyService;

  @Qualifier("collectionExerciseBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * GET to request collection exercises from the collection exercise service
   * for the given survey Id.
   *
   * @param id survey Id for which to trigger delivery of
   *          collection exercises
   * @return list of collection exercises associated to survey
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/survey/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> requestCollectionExercisesForSurvey(@PathVariable("id") final UUID id)
          throws CTPException {

    Survey survey = surveyService.requestSurvey(id);

    List<CollectionExerciseSummaryDTO> collectionExerciseSummaryDTOList;

    if (survey == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
              String.format("%s %s", RETURN_SURVEYNOTFOUND, id));
    } else {
      log.debug("Entering collection exercise fetch with survey Id {}", id);
      List<CollectionExerciseSummary> collectionExerciseSummaryList = collectionExerciseService.requestCollectionExerciseSummariesForSurvey(survey);
      collectionExerciseSummaryDTOList = mapperFacade.mapAsList(collectionExerciseSummaryList, CollectionExerciseSummaryDTO.class);
      if (collectionExerciseSummaryList.isEmpty()) {
        return ResponseEntity.noContent().build();
      }
    }

    return ResponseEntity.ok(collectionExerciseSummaryDTOList);
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
  public ResponseEntity<?> getCollectionExercise(@PathVariable("id") final UUID id)
          throws CTPException {
    log.debug("Entering collection exercise fetch with collection exercise Id {}", id);
    CollectionExercise collectionExercise = collectionExerciseService.requestCollectionExercise(id);
    if (collectionExercise == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
              String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, id));
    }

    CollectionExerciseDTO collectionExerciseDTO = mapperFacade.map(collectionExercise, CollectionExerciseDTO.class);

    List<CaseType> caseTypesList = collectionExerciseService.getCaseTypesForCollectionExercise(collectionExercise.getExercisePK());
    List<CaseTypeDTO> caseTypeDTOList = new ArrayList<>();

    for (CaseType caseType : caseTypesList) {
      CaseTypeDTO caseTypeDTO = new CaseTypeDTO();
      caseTypeDTO.setSampleUnitType(caseType.getSampleUnitTypeFK());
      caseTypeDTO.setActionPlanId(caseType.getActionPlanId());
      caseTypeDTOList.add(caseTypeDTO);
    }

    collectionExerciseDTO.setCaseTypes(caseTypeDTOList);

    return ResponseEntity.ok(collectionExerciseDTO);
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
  public ResponseEntity<?> requestSampleUnits(@PathVariable("id") final UUID id)
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
