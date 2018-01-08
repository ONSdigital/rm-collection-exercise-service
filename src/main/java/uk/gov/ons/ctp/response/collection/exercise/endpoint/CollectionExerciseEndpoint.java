package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.representation.CaseTypeDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkedSampleSummariesDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.response.survey.representation.SurveyDTO;

import static java.util.stream.Collectors.joining;

/**
 * The REST endpoint controller for Collection Exercises.
 */
@RestController
@RequestMapping(value = "/collectionexercises", produces = "application/json")
@Slf4j
public class CollectionExerciseEndpoint {

  private static final String RETURN_COLLECTIONEXERCISENOTFOUND =
          "Collection Exercise not found for collection exercise Id";
  private static final String RETURN_SURVEYNOTFOUND = "Survey not found for survey Id";
  private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

  @Autowired
  private PartySvcClient partySvcClient;

  @Autowired
  private CollectionExerciseService collectionExerciseService;

  @Autowired
  private SurveyService surveyService;

  @Autowired
  private EventService eventService;

  @Qualifier("collectionExerciseBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * GET to find collection exercises from the collection exercise service for
   * the given survey Id.
   *
   * @param id survey Id for which to trigger delivery of collection exercises
   * @return list of collection exercises associated to survey
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/survey/{id}", method = RequestMethod.GET)
  public ResponseEntity<List<CollectionExerciseSummaryDTO>> getCollectionExercisesForSurvey(
      @PathVariable("id") final UUID id) throws CTPException {

    SurveyDTO survey = surveyService.findSurvey(id);

    List<CollectionExerciseSummaryDTO> collectionExerciseSummaryDTOList;

    if (survey == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_SURVEYNOTFOUND, id));
    } else {
      log.debug("Entering collection exercise fetch with survey Id {}", id);
      List<CollectionExercise> collectionExerciseList = collectionExerciseService
          .findCollectionExercisesForSurvey(survey);
      collectionExerciseSummaryDTOList = mapperFacade.mapAsList(collectionExerciseList,
          CollectionExerciseSummaryDTO.class);
      if (collectionExerciseList.isEmpty()) {
        return ResponseEntity.noContent().build();
      }
    }

    return ResponseEntity.ok(collectionExerciseSummaryDTOList);
  }

  /**
   * GET to find collection exercise from the collection exercise service for
   * the given collection exercise Id.
   *
   * @param id collection exercise Id for which to trigger delivery of
   *          collection exercise
   * @return collection exercise associated to collection exercise id
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<CollectionExerciseDTO> getCollectionExercise(@PathVariable("id") final UUID id)
      throws CTPException {
    log.debug("Entering collection exercise fetch with collection exercise Id {}", id);
    CollectionExercise collectionExercise = collectionExerciseService.findCollectionExercise(id);
    if (collectionExercise == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, id));
    }

    CollectionExerciseDTO collectionExerciseDTO = addCaseTypesandSurveyId(collectionExercise);

    return ResponseEntity.ok(collectionExerciseDTO);
  }

  /**
   * GET endpoint to return a list of all collection exercises
   *
   * @return a list of all Collection Exercises
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<CollectionExerciseDTO>> getAllCollectionExercises() {
    log.debug("Entering fetch all collection exercise");
    List<CollectionExercise> collectionExercises = collectionExerciseService.findAllCollectionExercise();
    List<CollectionExerciseDTO> result = new ArrayList<>();

    for (CollectionExercise collectionExercise : collectionExercises) {
      CollectionExerciseDTO collectionExerciseDTO = addCaseTypesandSurveyId(collectionExercise);
      result.add(collectionExerciseDTO);
    }
    return ResponseEntity.ok(result);
  }

  /**
   * PUT request to update a collection exercise
   * @param id Collection exercise Id to update
   * @param collexDto a DTO containing survey id, name, user description and exercise ref
   * @throws CTPException on resource not found
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateCollectionExercise(
          @PathVariable("id") final UUID id,
          final @Validated(CollectionExerciseDTO.PutValidation.class) @RequestBody CollectionExerciseDTO collexDto)
          throws CTPException {
    log.info("Updating collection exercise {}", id);

    this.collectionExerciseService.updateCollectionExercise(id, collexDto);

    return ResponseEntity.ok().build();
  }

  /**
   * Method to return a useful message from a ConstraintViolation
   * @param cv a constraint violation
   * @return a human readable message
   */
  private static String getMessageForConstraintViolation(final ConstraintViolation<?> cv) {
    return cv.getPropertyPath() + " " + cv.getMessage();
  }

  /**
   * A method to manually validate the constraints on a CollectionExerciseDTO.  The reason this is needed is for
   * validating PUTs to subresources.  As these are the literal field values and not a JSON document it's difficult
   * to get Spring to validate these automatically. Hence the need for some kind of manual validation
   * @param collexDto The collection exercise data to validate
   * @throws CTPException thrown if constraint violation
   */
  private void validateConstraints(final CollectionExerciseDTO collexDto) throws CTPException {
    javax.validation.Validator validator = VALIDATOR_FACTORY.getValidator();
    Set<ConstraintViolation<CollectionExerciseDTO>> result = validator.validate(collexDto,
            CollectionExerciseDTO.PatchValidation.class);

    if (result.size() > 0) {
      String errorMessage =
              result
                .stream()
                .map(CollectionExerciseEndpoint::getMessageForConstraintViolation)
                .collect(joining(","));

      throw new CTPException(CTPException.Fault.BAD_REQUEST, errorMessage);
    }
  }

  /**
   * Utility method to wrap partial update of a collection exercise
   * @param id the uuid of the collection exercise to update
   * @param collexDto a dto containing some or all of the data to update
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException thrown if constraint violation etc
   */
  private ResponseEntity<?> patchCollectionExercise(final UUID id, final CollectionExerciseDTO collexDto) throws CTPException {
    validateConstraints(collexDto);

    this.collectionExerciseService.patchCollectionExercise(id, collexDto);
    return ResponseEntity.ok().build();
  }

  /**
   * PUT request to update a collection exercise scheduledStartDateTime
   * @param id Collection exercise Id to update
   * @param scheduledStart new value for exercise ref
   * @throws CTPException on resource not found
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   */
  @RequestMapping(value = "/{id}/scheduledStart", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseScheduledStart(
          @PathVariable("id") final UUID id,
          final @RequestBody String scheduledStart)
          throws CTPException {
    log.info("Updating collection exercise {}, setting scheduledStartDateTime to {}", id, scheduledStart);
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();

    try {
      LocalDateTime date = LocalDateTime.parse(scheduledStart, DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX", Locale.ROOT));
      collexDto.setScheduledStartDateTime(java.sql.Timestamp.valueOf(date));

      return patchCollectionExercise(id, collexDto);
    } catch (DateTimeParseException e){
      throw new CTPException(CTPException.Fault.BAD_REQUEST, String.format("Unparseable date %s (%s)", scheduledStart, e.getLocalizedMessage()));
    }
  }

  /**
   * PUT request to update a collection exercise exerciseRef
   * @param id Collection exercise Id to update
   * @param exerciseRef new value for exercise ref
   * @throws CTPException on resource not found
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   */
  @RequestMapping(value = "/{id}/exerciseRef", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseExerciseRef(
          @PathVariable("id") final UUID id,
          final @RequestBody String exerciseRef)
          throws CTPException {
    log.info("Updating collection exercise {}, setting exerciseRef to {}", id, exerciseRef);
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();
    collexDto.setExerciseRef(exerciseRef);

    return patchCollectionExercise(id, collexDto);
  }

  /**
   * PUT request to update a collection exercise name
   * @param id Collection exercise Id to update
   * @param name new value for name
   * @throws CTPException on resource not found
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   */
  @RequestMapping(value = "/{id}/name", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseName(
          @PathVariable("id") final UUID id,
          final @RequestBody String name)
          throws CTPException {
    log.info("Updating collection exercise {}, setting name to {}", id, name);
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();
    collexDto.setName(name);

    return patchCollectionExercise(id, collexDto);
  }

  /**
   * PUT request to update a collection exercise userDescription
   * @param id Collection exercise Id to update
   * @param userDescription new value for user description
   * @throws CTPException on resource not found
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   */
  @RequestMapping(value = "/{id}/userDescription", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseUserDescription(
          @PathVariable("id") final UUID id,
          final @RequestBody String userDescription)
          throws CTPException {
    log.info("Updating collection exercise {}, setting userDescription to {}", id, userDescription);
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();
    collexDto.setUserDescription(userDescription);

    return patchCollectionExercise(id, collexDto);
  }

  /**
   * PUT request to update a collection exercise userDescription
   * @param id Collection exercise Id to update
   * @param surveyId The new survey to associate with this collection exercise
   * @throws CTPException on resource not found
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   */
  @RequestMapping(value = "/{id}/surveyId", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseSurveyId(
          @PathVariable("id") final UUID id,
          final @RequestBody String surveyId)
          throws CTPException {
    log.info("Updating collection exercise {}, setting surveyId to {}", id, surveyId);
    try {
      UUID.fromString(surveyId);
    } catch (IllegalArgumentException e) {
      throw new CTPException(CTPException.Fault.BAD_REQUEST, e);
    }

    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();
    collexDto.setSurveyId(surveyId);

    return patchCollectionExercise(id, collexDto);
  }

  /**
   * POST request to create a collection exercise
   * @param collex A dto containing the data about the collection exercise
   * @return 201 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<?> createCollectionExercise(
          final @Validated(CollectionExerciseDTO.PostValidation.class) @RequestBody CollectionExerciseDTO collex)
          throws CTPException {
    log.info("Creating collection exercise");
    String surveyId = collex.getSurveyId();
    String surveyRef = collex.getSurveyRef();
    SurveyDTO survey = null;

    if (StringUtils.isBlank(surveyId) == false){
      survey = this.surveyService.findSurvey(UUID.fromString(collex.getSurveyId()));
    } else if (StringUtils.isBlank(surveyRef) == false){
      survey = this.surveyService.findSurveyByRef(surveyRef);
      // Downstream expects the surveyId to be present so add it now
      collex.setSurveyId(survey.getId());
    } else {
      throw new CTPException(CTPException.Fault.BAD_REQUEST, "No survey specified");
    }

    if (survey == null) {
        throw new CTPException(CTPException.Fault.BAD_REQUEST, "Invalid survey: " + surveyId);
    } else {
      CollectionExercise existing = this.collectionExerciseService.findCollectionExercise(
              collex.getExerciseRef(), survey);

      if (existing != null) {
        throw new CTPException(CTPException.Fault.RESOURCE_VERSION_CONFLICT,
                String.format("Collection exercise with survey %s and exerciseRef %s already exists",
                        survey.getId().toString(),
                        collex.getExerciseRef()));
      } else {
        CollectionExercise newCollex = this.collectionExerciseService.createCollectionExercise(collex);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCollex.getId()).toUri();

        return ResponseEntity.created(location).build();
      }
    }
  }

  /**
   * DELETE request to delete a collection exercise
   * @param id Collection exercise Id to delete
   * @throws CTPException on resource not found
   * @return the collection exercise that was to be deleted
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<CollectionExercise> deleteCollectionExercise(@PathVariable("id") final UUID id)
      throws CTPException {
    log.info("Deleting collection exercise {}", id);
    this.collectionExerciseService.deleteCollectionExercise(id);

    return ResponseEntity.accepted().build();
  }

  /**
   * PUT request for linking an array of sample summaries to a collection
   * exercise
   *
   * @param collectionExerciseId the collection exercise to link sample
   *          summaries to
   * @param linkSampleSummaryDTO the array of all sample summaries to link to
   *          the collection exercise, including summaries previously linked as
   *          all currently linked summaries are removed from the table
   * @param bindingResult the bindingResult used to validate requests
   * @return list of the newly linked collection exercises and sample summaries
   * @throws InvalidRequestException if binding errors
   * @throws CTPException on resource not found
   */
  @RequestMapping(value = "/link/{collectionExerciseId}", method = RequestMethod.PUT, consumes = "application/json")
  public ResponseEntity<LinkedSampleSummariesDTO> linkSampleSummary(
      @PathVariable("collectionExerciseId") final UUID collectionExerciseId,
      @RequestBody(required = false) @Valid final LinkSampleSummaryDTO linkSampleSummaryDTO,
      BindingResult bindingResult) throws InvalidRequestException, CTPException {
    log.debug("Entering linkSampleSummary with collectionExerciseID {}", collectionExerciseId);

    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for execute action plan: ", bindingResult);
    }

    CollectionExercise collectionExercise = collectionExerciseService.findCollectionExercise(collectionExerciseId);
    if (collectionExercise == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, collectionExerciseId));
    }

    List<SampleLink> linkSampleSummaryToCollectionExercise = collectionExerciseService
        .linkSampleSummaryToCollectionExercise(collectionExerciseId, linkSampleSummaryDTO.getSampleSummaryIds());
    LinkedSampleSummariesDTO result = new LinkedSampleSummariesDTO();

    for (UUID summaryId : linkSampleSummaryDTO.getSampleSummaryIds()) {
      partySvcClient.linkSampleSummaryId(summaryId.toString(), collectionExerciseId.toString());
    }

    if (linkSampleSummaryToCollectionExercise != null) {
      List<UUID> summaryIds = new ArrayList<UUID>();
      for (SampleLink sampleLink : linkSampleSummaryToCollectionExercise) {
        summaryIds.add(sampleLink.getSampleSummaryId());
      }
      result.setSampleSummaryIds(summaryIds);

      result.setCollectionExerciseId(linkSampleSummaryToCollectionExercise.get(0).getCollectionExerciseId());
    }
    return ResponseEntity.ok(result);

  }

  /**
   * return a list of UUIDs for the sample summaries linked to a specific
   * collection exercise
   *
   * @param collectionExerciseId the id of the collection exercise to get linked
   *          sample summaries for
   * @return list of UUIDs of linked sample summaries
   * @throws CTPException if no collection exercise found for UUID
   */
  @RequestMapping(value = "link/{collectionExerciseId}", method = RequestMethod.GET)
  public ResponseEntity<List<UUID>> requestLinkedSampleSummaries(
      @PathVariable("collectionExerciseId") final UUID collectionExerciseId) throws CTPException {
    log.debug("Getting sample summaries linked to collectionExerciseId {}", collectionExerciseId);

    CollectionExercise collectionExercise = collectionExerciseService.findCollectionExercise(collectionExerciseId);
    if (collectionExercise == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, collectionExerciseId));
    }

    List<SampleLink> result = collectionExerciseService.findLinkedSampleSummaries(collectionExerciseId);
    if (CollectionUtils.isEmpty(result)) {
      return ResponseEntity.noContent().build();
    }

    List<UUID> output = new ArrayList<UUID>();
    for (SampleLink link : result) {
      output.add(link.getSampleSummaryId());
    }
    return ResponseEntity.ok(output);
  }

  /**
   * adds the case types and surveyId to the CollectionExerciseDTO
   *
   * @param collectionExercise the collection exercise to find the associated
   *          case types and surveyIds for
   * @return a CollectionExerciseDTO of the collection exercise with case types
   *         and surveyId added which is output by the endpoints
   */
  private CollectionExerciseDTO addCaseTypesandSurveyId(CollectionExercise collectionExercise) {
    Collection<CaseType> caseTypeList = collectionExerciseService.getCaseTypesList(collectionExercise);
    List<CaseTypeDTO> caseTypeDTOList = mapperFacade.mapAsList(caseTypeList, CaseTypeDTO.class);

    CollectionExerciseDTO collectionExerciseDTO = mapperFacade.map(collectionExercise, CollectionExerciseDTO.class);
    collectionExerciseDTO.setCaseTypes(caseTypeDTOList);
    // NOTE: this method used to fail (NPE) if the survey did not exist in the local database.  Now the survey id
    // is not validated and passed on verbatim
    collectionExerciseDTO.setSurveyId(collectionExercise.getSurveyUuid().toString());

    return collectionExerciseDTO;
  }

  @RequestMapping(value = "/{id}/events", method = RequestMethod.POST)
  public ResponseEntity<?> createCollectionExerciseEvent(
          @PathVariable("id") final UUID id,
          final @RequestBody EventDTO eventDto)
          throws CTPException {
    log.info("Creating event {} for collection exercise {}", eventDto.getTag(), eventDto.getCollectionExerciseId());

    Event newEvent = eventService.createEvent(eventDto);

    // MATTTODO - fix this
    URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}/events")
            .buildAndExpand(newEvent.getId()).toUri();

    return ResponseEntity.created(location).build();
  }

  @RequestMapping(value = "/{id}/events", method = RequestMethod.GET)
  public ResponseEntity<List<EventDTO>> getCollectionExerciseEvents(
          @PathVariable("id") final UUID id)
          throws CTPException {
    List<EventDTO> result = this.eventService.getEvents(id).stream().map(EventService::createEventDTOFromEvent).collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }


}
