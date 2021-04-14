package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static java.util.stream.Collectors.joining;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.net.URI;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.InvalidRequestException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.util.MultiIsoDateFormat;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.*;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/** The REST endpoint controller for Collection Exercises. */
@RestController
@RequestMapping(value = "/collectionexercises", produces = "application/json")
public class CollectionExerciseEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseEndpoint.class);

  private static final String RETURN_COLLECTIONEXERCISENOTFOUND =
      "Collection Exercise not found for collection exercise Id";
  private static final String RETURN_SURVEYNOTFOUND = "Survey not found for survey Id";
  private static final ValidatorFactory VALIDATOR_FACTORY =
      Validation.buildDefaultValidatorFactory();

  private CollectionExerciseService collectionExerciseService;
  private CollectionExerciseRepository collectRepo;
  private EventService eventService;
  private SampleService sampleService;
  private SurveySvcClient surveyService;

  private MapperFacade mapperFacade;

  @Autowired private AppConfig appConfig;

  @Autowired
  public CollectionExerciseEndpoint(
      CollectionExerciseService collectionExerciseService,
      CollectionExerciseRepository collectRepo,
      SurveySvcClient surveyService,
      SampleService sampleService,
      EventService eventService,
      @Qualifier("collectionExerciseBeanMapper") MapperFacade mapperFacade) {
    this.collectionExerciseService = collectionExerciseService;
    this.collectRepo = collectRepo;
    this.surveyService = surveyService;
    this.sampleService = sampleService;
    this.eventService = eventService;
    this.mapperFacade = mapperFacade;
  }

  /**
   * Method to return a useful message from a ConstraintViolation
   *
   * @param cv a constraint violation
   * @return a human readable message
   */
  private static String getMessageForConstraintViolation(final ConstraintViolation<?> cv) {
    return cv.getPropertyPath() + " " + cv.getMessage();
  }

  /**
   * GET to find collection exercises from the collection exercise service for the given survey Id.
   *
   * @param id survey Id for which to trigger delivery of collection exercises
   * @return list of collection exercises associated to survey
   * @throws CTPException on resource not found
   */
  @Operation(summary = "GET request to find collection exercises for the given survey Id.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of collection exercises for surveyId"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/survey/{id}", method = RequestMethod.GET)
  public ResponseEntity<List<CollectionExerciseDTO>> getCollectionExercisesForSurvey(
      @PathVariable("id") final UUID id, @RequestParam("liveOnly") Optional<Boolean> liveOnly)
      throws CTPException {

    log.with("survey_id", id).debug("Retrieving collection exercises by surveyId");

    SurveyDTO survey = surveyService.findSurvey(id);

    List<CollectionExerciseDTO> collectionExerciseSummaryDTOList;

    if (survey == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND, String.format("%s %s", RETURN_SURVEYNOTFOUND, id));
    } else {
      log.with("survey_id", id).debug("Entering collection exercise fetch with surveyId");
      List<CollectionExercise> collectionExerciseList = null;

      if (liveOnly.isPresent() && liveOnly.get().booleanValue() == true) {
        collectionExerciseList =
            collectionExerciseService.findCollectionExercisesBySurveyIdAndState(
                id, CollectionExerciseState.LIVE);
      } else {
        collectionExerciseList = collectionExerciseService.findCollectionExercisesForSurvey(survey);
      }

      collectionExerciseSummaryDTOList =
          collectionExerciseList
              .stream()
              .map(this::getCollectionExerciseDTO)
              .collect(Collectors.toList());
      if (collectionExerciseList.isEmpty()) {
        return ResponseEntity.noContent().build();
      }
    }

    log.with("survey_id", id).debug("Sucessfully retrieved collection exercises for surveyId");
    return ResponseEntity.ok(collectionExerciseSummaryDTOList);
  }

  /**
   * Endpoint to get a collection exercise by exercise ref and survey ref
   *
   * @param exerciseRef the exercise ref
   * @param surveyRef the survey ref
   * @return 200 with collection exercise body if found, otherwise 404
   * @throws CTPException
   */
  @Operation(
      summary =
          "GET request to retrieve a collection exercise by exercise reference and survey reference")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Successful retrieval of collection exercise by exercise "
                    + "reference and survey reference"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{exerciseRef}/survey/{surveyRef}", method = RequestMethod.GET)
  public ResponseEntity<CollectionExerciseDTO> getCollectionExercisesForSurvey(
      @PathVariable("exerciseRef") final String exerciseRef,
      @PathVariable("surveyRef") final String surveyRef)
      throws CTPException {

    log.with("survey_ref", surveyRef)
        .with("period", exerciseRef)
        .debug("Retrieving collection exercise with surveyRef and period");

    CollectionExercise collex =
        this.collectionExerciseService.findCollectionExercise(surveyRef, exerciseRef);

    if (collex == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format(
              "Cannot find collection exercise for surveyRef={} and period={}",
              surveyRef,
              exerciseRef));
    } else {
      log.with("survey_ref", surveyRef)
          .with("period", exerciseRef)
          .debug("Successfully retrieved collection exercise using surveyRef and period");
      return ResponseEntity.ok(getCollectionExerciseDTO(collex));
    }
  }

  /**
   * Return collection exercises for each of the the surveys in the given list of survey ids. Return
   * data as a Json dictionary.
   *
   * @param surveyIds survey Ids for which to get collection exercises
   * @param liveOnly Boolean , if set only returns live collection exercises
   * @return json dictionary or collection exercises per survey
   * @throws CTPException on resource not found
   */
  @Operation(summary = "GET request to retrieve collection exercises for given list of survey ids")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of collection exercise"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/surveys", method = RequestMethod.GET, produces = "application/json")
  public ResponseEntity<HashMap> getCollectionExercisesForSurveys(
      final @RequestParam List<UUID> surveyIds,
      @RequestParam("liveOnly") Optional<Boolean> liveOnly) {

    HashMap<UUID, List<CollectionExercise>> surveyCollexMap;

    if (liveOnly.isPresent() && liveOnly.get().booleanValue()) {
      surveyCollexMap =
          collectionExerciseService.findCollectionExercisesForSurveysByState(
              surveyIds, CollectionExerciseState.LIVE);
    } else {
      surveyCollexMap = collectionExerciseService.findCollectionExercisesForSurveys(surveyIds);
    }

    return ResponseEntity.ok(surveyCollexMap);
  }

  /**
   * GET to find collection exercise from the collection exercise service for the given collection
   * exercise Id.
   *
   * @param id collection exercise Id for which to trigger delivery of collection exercise
   * @return collection exercise associated to collection exercise id
   * @throws CTPException on resource not found
   */
  @Operation(
      summary = "GET request to retrieve collection exercise for given collection exercise id")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of collection exercise"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<CollectionExerciseDTO> getCollectionExercise(
      @PathVariable("id") final UUID id) throws CTPException {
    log.with("collection_exercise_id", id)
        .debug("Entering collection exercise fetch with collectionExerciseId");
    CollectionExercise collectionExercise = collectionExerciseService.findCollectionExercise(id);
    if (collectionExercise == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, id));
    }

    CollectionExerciseDTO collectionExerciseDTO = getCollectionExerciseDTO(collectionExercise);

    log.with("collection_exercise_id", id)
        .debug("Successfully retrieved collection exercise with collectionExerciseId");
    return ResponseEntity.ok(collectionExerciseDTO);
  }

  /**
   * GET endpoint to return a list of all collection exercises
   *
   * @return a list of all Collection Exercises
   */
  @Operation(summary = "GET request to retrieve all collection exercises")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of collection exercises"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<CollectionExerciseDTO>> getAllCollectionExercises() {
    log.debug("Entering fetch all collection exercises");
    List<CollectionExercise> collectionExercises =
        collectionExerciseService.findAllCollectionExercise();
    List<CollectionExerciseDTO> result = new ArrayList<>();

    for (CollectionExercise collectionExercise : collectionExercises) {
      CollectionExerciseDTO collectionExerciseDTO = getCollectionExerciseDTO(collectionExercise);
      result.add(collectionExerciseDTO);
    }
    log.debug("Successfully retrieved all collection exercises");
    return ResponseEntity.ok(result);
  }

  /**
   * PUT request to update a collection exercise
   *
   * @param id Collection exercise Id to update
   * @param collexDto a DTO containing survey id, name, user description and exercise ref
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "PUT request to update a collection exercise")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateCollectionExercise(
      @PathVariable("id") final UUID id,
      final @Validated(CollectionExerciseDTO.PutValidation.class) @RequestBody CollectionExerciseDTO
              collexDto)
      throws CTPException {
    log.with("collection_exercise_id", id)
        .debug("Updating collection exercise with collectionExerciseId");

    collectionExerciseService.updateCollectionExercise(id, collexDto);

    log.with("collection_exercise_id", id)
        .debug("Sucessfully updated collection exercise with collectionExerciseId");
    return ResponseEntity.ok().build();
  }

  /**
   * A method to manually validate the constraints on a CollectionExerciseDTO. The reason this is
   * needed is for validating PUTs to subresources. As these are the literal field values and not a
   * JSON document it's difficult to get Spring to validate these automatically. Hence the need for
   * some kind of manual validation
   *
   * @param collexDto The collection exercise data to validate
   * @throws CTPException thrown if constraint violation
   */
  private void validateConstraints(final CollectionExerciseDTO collexDto) throws CTPException {
    javax.validation.Validator validator = VALIDATOR_FACTORY.getValidator();
    Set<ConstraintViolation<CollectionExerciseDTO>> result =
        validator.validate(collexDto, CollectionExerciseDTO.PatchValidation.class);

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
   *
   * @param id the uuid of the collection exercise to update
   * @param collexDto a dto containing some or all of the data to update
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException thrown if constraint violation etc
   */
  private ResponseEntity<?> patchCollectionExercise(
      final UUID id, final CollectionExerciseDTO collexDto) throws CTPException {
    validateConstraints(collexDto);

    this.collectionExerciseService.patchCollectionExercise(id, collexDto);
    return ResponseEntity.ok().build();
  }

  /**
   * PUT request to update a collection exercise scheduledStartDateTime
   *
   * @param id Collection exercise Id to update
   * @param scheduledStart new value for exercise ref
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "PUT request to update a collection exercise scheduledStartDateTime")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(
      value = "/{id}/scheduledStart",
      method = RequestMethod.PUT,
      consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseScheduledStart(
      @PathVariable("id") final UUID id, final @RequestBody String scheduledStart)
      throws CTPException {
    log.with("collection_exercise_id", id)
        .with("scheduledStartDateTime", scheduledStart)
        .debug("Updating collection exercise, setting scheduledStartDateTime");
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();

    try {
      LocalDateTime date =
          LocalDateTime.parse(
              scheduledStart,
              DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX", Locale.ROOT));
      collexDto.setScheduledStartDateTime(java.sql.Timestamp.valueOf(date));

      return patchCollectionExercise(id, collexDto);
    } catch (DateTimeParseException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          String.format("Unparseable date %s (%s)", scheduledStart, e.getLocalizedMessage()));
    }
  }

  /**
   * PUT request to update a collection exercise exerciseRef
   *
   * @param id Collection exercise Id to update
   * @param exerciseRef new value for exercise ref
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "PUT request to update a collection exercise exerciseRef")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/exerciseRef", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseExerciseRef(
      @PathVariable("id") final UUID id, final @RequestBody String exerciseRef)
      throws CTPException {
    log.with("collection_exercise_id", id)
        .with("exercise_ref", exerciseRef)
        .debug("Updating collection exercise, setting exerciseRef");
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();
    collexDto.setExerciseRef(exerciseRef);

    return patchCollectionExercise(id, collexDto);
  }

  /**
   * PUT request to update a collection exercise name
   *
   * @param id Collection exercise Id to update
   * @param name new value for name
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "PUT request to update a collection exercise name")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/name", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseName(
      @PathVariable("id") final UUID id, final @RequestBody String name) throws CTPException {
    log.with("collection_exercise_id", id)
        .with("name", name)
        .debug("Updating collection exercise, setting name");
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();
    collexDto.setName(name);

    return patchCollectionExercise(id, collexDto);
  }

  /**
   * PUT request to update a collection exercise userDescription
   *
   * @param id Collection exercise Id to update
   * @param userDescription new value for user description
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "PUT request to update a collection exercise userDescription")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(
      value = "/{id}/userDescription",
      method = RequestMethod.PUT,
      consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseUserDescription(
      @PathVariable("id") final UUID id, final @RequestBody String userDescription)
      throws CTPException {
    log.with("collection_exercise_id", id)
        .with("user_description", userDescription)
        .debug("Updating collection exercise, setting userDescription");
    CollectionExerciseDTO collexDto = new CollectionExerciseDTO();
    collexDto.setUserDescription(userDescription);

    return patchCollectionExercise(id, collexDto);
  }

  /**
   * PUT request to update a collection exercise surveyId
   *
   * @param id Collection exercise Id to update
   * @param surveyId The new survey to associate with this collection exercise
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "PUT request to update a collection exercise surveyId")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/surveyId", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> patchCollectionExerciseSurveyId(
      @PathVariable("id") final UUID id, final @RequestBody String surveyId) throws CTPException {
    log.with("collection_exercise_id", id)
        .with("survey_id", surveyId)
        .debug("Updating collection exercise, setting surveyId");
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
   *
   * @param collex A dto containing the data about the collection exercise
   * @return 201 on success, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "POST request to create a collection exercise")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successful creation"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<?> createCollectionExercise(
      final @Validated(CollectionExerciseDTO.PostValidation.class) @RequestBody
          CollectionExerciseDTO collex)
      throws CTPException {
    log.with("exercise_ref", collex.getExerciseRef())
        .with("survey_ref", collex.getSurveyRef())
        .debug("Creating collection exercise");
    SurveyDTO survey = getSurveyFromCollex(collex);

    if (survey == null) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Invalid survey: " + collex.getSurveyId());
    }

    collex.setSurveyId(survey.getId());
    // Check if collection exercise already exists
    CollectionExercise existing =
        this.collectionExerciseService.findCollectionExercise(collex.getExerciseRef(), survey);

    if (existing != null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_VERSION_CONFLICT,
          String.format(
              "Collection exercise already exists, ExerciseRef: %s, SurveyRef: %s",
              collex.getExerciseRef(), collex.getSurveyRef()));
    }

    CollectionExercise newCollex =
        collectionExerciseService.createCollectionExercise(collex, survey);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(newCollex.getId())
            .toUri();
    log.with("collection_exercise_id", newCollex.getId())
        .debug("Successfully created collection exercise");
    return ResponseEntity.created(location).build();
  }

  private SurveyDTO getSurveyFromCollex(
      @Validated(CollectionExerciseDTO.PostValidation.class) @RequestBody
          final CollectionExerciseDTO collex)
      throws CTPException {
    final String surveyId = collex.getSurveyId();
    final String surveyRef = collex.getSurveyRef();

    if (!StringUtils.isBlank(surveyId)) {
      return surveyService.findSurvey(UUID.fromString(collex.getSurveyId()));
    }

    if (!StringUtils.isBlank(surveyRef)) {
      return surveyService.findSurveyByRef(surveyRef);
    }

    throw new CTPException(CTPException.Fault.BAD_REQUEST, "No survey specified");
  }

  /**
   * DELETE request which deletes a collection exercise
   *
   * @param id Collection exercise Id to delete
   * @return the collection exercise that was to be deleted
   * @throws CTPException on resource not found
   */
  @Operation(summary = "DELETE request which deletes a collection exercise.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successful Deletion"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<CollectionExercise> deleteCollectionExercise(
      @PathVariable("id") final UUID id) throws CTPException {
    log.with("collection_exercise_id", id).debug("Deleting collection exercise");
    collectionExerciseService.deleteCollectionExercise(id);

    return ResponseEntity.accepted().build();
  }

  /**
   * PUT request for linking an array of sample summaries to a collection exercise
   *
   * @param collectionExerciseId the collection exercise to link sample summaries to
   * @param linkSampleSummaryDTO the array of all sample summaries to link to the collection
   *     exercise, including summaries previously linked as all currently linked summaries are
   *     removed from the table
   * @param bindingResult the bindingResult used to validate requests
   * @return list of the newly linked collection exercises and sample summaries
   * @throws InvalidRequestException if binding errors
   * @throws CTPException on resource not found
   */
  @Operation(
      summary = "PUT request for linking an array of sample summaries to a collection exercise.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful Update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(
      value = "/link/{collectionExerciseId}",
      method = RequestMethod.PUT,
      consumes = "application/json")
  public ResponseEntity<LinkedSampleSummariesDTO> linkSampleSummary(
      @PathVariable("collectionExerciseId") final UUID collectionExerciseId,
      @RequestBody(required = false) @Valid final LinkSampleSummaryDTO linkSampleSummaryDTO,
      BindingResult bindingResult)
      throws InvalidRequestException, CTPException {
    log.with("collection_exercise_id", collectionExerciseId)
        .debug("Entering linkSampleSummary with collectionExerciseID");

    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for execute action plan: ", bindingResult);
    }

    CollectionExercise collectionExercise =
        collectionExerciseService.findCollectionExercise(collectionExerciseId);
    if (collectionExercise == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, collectionExerciseId));
    }

    List<SampleLink> linkSampleSummaryToCollectionExercise =
        collectionExerciseService.linkSampleSummaryToCollectionExercise(
            collectionExerciseId, linkSampleSummaryDTO.getSampleSummaryIds());
    LinkedSampleSummariesDTO result = new LinkedSampleSummariesDTO();

    if (linkSampleSummaryToCollectionExercise != null) {
      List<UUID> summaryIds = new ArrayList<UUID>();
      for (SampleLink sampleLink : linkSampleSummaryToCollectionExercise) {
        summaryIds.add(sampleLink.getSampleSummaryId());
      }
      result.setSampleSummaryIds(summaryIds);

      result.setCollectionExerciseId(
          linkSampleSummaryToCollectionExercise.get(0).getCollectionExerciseId());
    }
    return ResponseEntity.ok(result);
  }

  /**
   * Endpoint to return the list of samples linked to a collection exercise
   *
   * @param collectionExerciseId the collection exercise for which the links are required
   * @return a list of sample summaries linked to the collection exercise
   */
  @Operation(summary = "GET request to retrieve list of samples linked to a collection exercise")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of samples linked to a collection exercise"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(
      value = "/link/{collectionExerciseId}",
      method = RequestMethod.GET,
      produces = "application/vnd.ons.sdc.samplelink.v1+json")
  public ResponseEntity<List<SampleLinkDTO>> getSampleLinks(
      @PathVariable("collectionExerciseId") final UUID collectionExerciseId) {
    log.with("collection_exercise_id", collectionExerciseId)
        .debug("Getting linked sample summaries");
    List<SampleLink> sampleLinks =
        collectionExerciseService.findLinkedSampleSummaries(collectionExerciseId);
    List<SampleLinkDTO> sampleLinkList = mapperFacade.mapAsList(sampleLinks, SampleLinkDTO.class);

    return ResponseEntity.ok(sampleLinkList);
  }

  /**
   * for unlinking sample summary from a collection exercise
   *
   * @param collectionExerciseId the collection exercise to unlink from sample
   * @param sampleSummaryId the collection exercise to unlink from collection exercise
   * @return noContent response
   * @throws CTPException on resource not found
   */
  @Operation(summary = "DELETE request for unlinking sample summary from a collection exercise.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successful Deletion"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(
      value = "/unlink/{collectionExerciseId}/sample/{sampleSummaryId}",
      method = RequestMethod.DELETE)
  public ResponseEntity<?> unlinkSampleSummary(
      @PathVariable("collectionExerciseId") final UUID collectionExerciseId,
      @PathVariable("sampleSummaryId") final UUID sampleSummaryId)
      throws CTPException {
    log.with("collection_exercise_id", collectionExerciseId)
        .with("sample_summary_id", sampleSummaryId)
        .debug("Entering unlinkSampleSummary with collectionExerciseID and sampleSummaryId");

    CollectionExercise collectionExercise =
        collectionExerciseService.findCollectionExercise(collectionExerciseId);
    if (collectionExercise == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, collectionExerciseId));
    }

    collectionExerciseService.removeSampleSummaryLink(sampleSummaryId, collectionExerciseId);

    return ResponseEntity.noContent().build();
  }

  /**
   * return a list of UUIDs for the sample summaries linked to a specific collection exercise
   *
   * @param collectionExerciseId the id of the collection exercise to get linked sample summaries
   *     for
   * @return list of UUIDs of linked sample summaries
   * @throws CTPException if no collection exercise found for UUID
   */
  @Operation(
      summary =
          "GET request to retrieve list of UUIDs for the sample summaries linked to a specific collection exercise")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful Operation"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "link/{collectionExerciseId}", method = RequestMethod.GET)
  public ResponseEntity<List<UUID>> requestLinkedSampleSummaries(
      @PathVariable("collectionExerciseId") final UUID collectionExerciseId) throws CTPException {
    log.with("collection_exercise_id", collectionExerciseId)
        .debug("Getting sample summaries linked to collectionExerciseId");

    CollectionExercise collectionExercise =
        collectionExerciseService.findCollectionExercise(collectionExerciseId);
    if (collectionExercise == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, collectionExerciseId));
    }

    List<SampleLink> result =
        collectionExerciseService.findLinkedSampleSummaries(collectionExerciseId);
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
   * @param collectionExercise the collection exercise to find the associated case types and
   *     surveyIds for
   * @return a CollectionExerciseDTO of the collection exercise with case types and surveyId added
   *     which is output by the endpoints
   */
  private CollectionExerciseDTO getCollectionExerciseDTO(
      final CollectionExercise collectionExercise) {
    log.with("collection_exercise_id", collectionExercise.getId())
        .debug("Populating data for requested collection exercise");

    CollectionExerciseDTO collectionExerciseDTO =
        mapperFacade.map(collectionExercise, CollectionExerciseDTO.class);
    // NOTE: this method used to fail (NPE) if the survey did not exist in the local database.  Now
    // the survey id
    // is not validated and passed on verbatim
    collectionExerciseDTO.setSurveyId(collectionExercise.getSurveyId().toString());

    // If we are in the failed validation state, then there should be validation error so go look
    // them up.
    // We don't do this for all the states as this is a non-trivial database operation.
    // Note: this code here will suppress any validation errors that are present in the other states
    // (shouldn't happen but ...)
    if (collectionExercise.getState()
        == CollectionExerciseDTO.CollectionExerciseState.FAILEDVALIDATION) {
      SampleUnitValidationErrorDTO[] errors = sampleService.getValidationErrors(collectionExercise);

      collectionExerciseDTO.setValidationErrors(errors);
    }

    try {
      List<EventDTO> eventList =
          this.eventService
              .getEvents(collectionExercise.getId())
              .stream()
              .map(EventService::createEventDTOFromEvent)
              .collect(Collectors.toList());

      collectionExerciseDTO.setEvents(eventList);
    } catch (CTPException e) {
      log.with("collection_exercise_id", collectionExercise.getId())
          .error("Error retrieving events for collection exercise Id", e);
    }

    return collectionExerciseDTO;
  }

  @Operation(summary = "POST request to create a collection exercise event.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successful creation"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/events", method = RequestMethod.POST)
  public ResponseEntity<?> createCollectionExerciseEvent(
      @PathVariable("id") final UUID id, final @RequestBody EventDTO eventDto) throws CTPException {
    log.with("collection_exercise_id", id)
        .with("event_tag", eventDto.getTag())
        .debug("Creating event for collection exercise");

    eventDto.setCollectionExerciseId(id);
    Event newEvent;
    try {
      newEvent = eventService.createEvent(eventDto);
    } catch (CTPException e) {
      log.with("fault", e.getFault())
          .with("message", e.getMessage())
          .info("An error occurred creating event");
      return ResponseEntity.badRequest().body(e);
    }

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}/events/{tag}")
            .buildAndExpand(newEvent.getId(), newEvent.getTag())
            .toUri();

    return ResponseEntity.created(location).build();
  }

  @Operation(summary = "GET request to retrieve events for collection exercise")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of events for collection exercise"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/events", method = RequestMethod.GET)
  public ResponseEntity<List<EventDTO>> getCollectionExerciseEvents(
      @PathVariable("id") final UUID id) throws CTPException {
    List<EventDTO> result =
        this.eventService
            .getEvents(id)
            .stream()
            .map(EventService::createEventDTOFromEvent)
            .collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }

  @Operation(summary = "GET request to retrieve collection exercise events")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval of collection exercise events"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/events", method = RequestMethod.GET)
  public ResponseEntity<Map<UUID, List<EventDTO>>> getMultipleCollectionExerciseEvents(
      @RequestParam("ids") final List<UUID> ids) throws CTPException {
    Map<UUID, List<EventDTO>> result =
        this.eventService
            .getEvents(ids)
            .stream()
            .map(EventService::createEventDTOFromEvent)
            .collect(Collectors.groupingBy(EventDTO::getCollectionExerciseId, Collectors.toList()));

    return ResponseEntity.ok(result);
  }

  /**
   * PUT request to update a collection event date
   *
   * @param id Collection exercise Id
   * @param tag collection exercise event tag
   * @return 200 if all is ok, 400 for bad request, 409 for conflict
   * @throws CTPException on resource not found
   */
  @Operation(summary = "PUT request for updating collection event date for a given event tag.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful Update"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/events/{tag}", method = RequestMethod.PUT, consumes = "text/plain")
  public ResponseEntity<?> updateEventDate(
      @PathVariable("id") final UUID id,
      @PathVariable("tag") final String tag,
      final @RequestBody String date)
      throws CTPException {

    log.with("collection_exercise_id", id)
        .with("date", date)
        .debug("Adding collection exercise, setting date to");

    try {
      MultiIsoDateFormat dateParser = new MultiIsoDateFormat();
      ResponseEventDTO responseEventDTO = eventService.updateEvent(id, tag, dateParser.parse(date));
      return ResponseEntity.ok().body(responseEventDTO);
    } catch (ParseException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          String.format("Unparseable date %s (%s)", date, e.getLocalizedMessage()));
    } catch (CTPException e) {
      log.with("fault", e.getFault())
          .with("message", e.getMessage())
          .info("An error occurred updating event");
      return ResponseEntity.badRequest().body(e);
    }
  }

  /**
   * GET to find event from the collection exercise service for the given event tag collection Id.
   *
   * @param id collection exercise id
   * @param tag collection exercise event tag
   * @return event associated to collection exercise
   * @throws CTPException on resource not found
   */
  @Operation(summary = "GET request to retrieve specified tag event against collection exercise id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "404",
            description = "Resource Not Found",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/events/{tag}", method = RequestMethod.GET)
  public ResponseEntity<Event> getEvent(
      @PathVariable("id") final UUID id, @PathVariable("tag") final String tag)
      throws CTPException {
    log.with("event_id", id)
        .with("tag", tag)
        .debug("Entering Event fetch with event id, event tag");

    Event event = eventService.getEvent(id, tag);

    return ResponseEntity.ok(event);
  }

  /**
   * DELETE request to delete a collection exercise event
   *
   * @param id Collection exercise Id
   * @param tag collection exercise event tag
   * @return the collection exercise event that was to be deleted
   * @throws CTPException on resource not found
   */
  @Operation(summary = "POST request to remove tag event against collection exercise id.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successful deletion"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/events/{tag}", method = RequestMethod.POST)
  public ResponseEntity<Event> deleteCollectionExerciseEventTag(
      @PathVariable("id") final UUID id, @PathVariable("tag") final String tag)
      throws CTPException {
    log.with("event_id", id).with("tag", tag).info("Deleting collection exercise event");

    eventService.deleteEvent(id, tag);

    return ResponseEntity.noContent().build();
  }

  /**
   * DELETE request to delete a collection exercise event
   *
   * @param id Collection exercise Id
   * @param tag collection exercise event tag
   * @return the collection exercise event that was to be deleted
   * @throws CTPException on resource not found
   */
  @Operation(summary = "DELETE request to remove tag event against collection exercise id.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Successful deletion"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(examples = {})),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(examples = {}))
      })
  @RequestMapping(value = "/{id}/events/{tag}", method = RequestMethod.DELETE)
  public ResponseEntity<Event> deleteCollectionExerciseEvent(
      @PathVariable("id") final UUID id, @PathVariable("tag") final String tag)
      throws CTPException {
    log.with("event_id", id)
        .with("tag", tag)
        .debug("Deleting collection exercise event id, event tag ");

    eventService.deleteEvent(id, tag);

    return ResponseEntity.noContent().build();
  }

  /**
   * Finds all the validated collection exercises and distributes them.
   *
   * <p>Distributing a sample unit means sending a message to the case service with details about
   * the sample unit and transitioning the state of the sample in collection-exercise to mark the
   * event happening.
   *
   * @throws CTPException on any exception thrown
   */
  @RequestMapping(value = "/sample-unit-distribution", method = RequestMethod.GET)
  public final ResponseEntity<String> distributeSampleUnits() throws CTPException {
    try {
      log.info("About to begin sample unit distribution");
      List<CollectionExercise> exercises =
          collectRepo.findByState(CollectionExerciseDTO.CollectionExerciseState.VALIDATED);

      log.info("Found [" + exercises.size() + "] collection exercises to distribute");
      for (CollectionExercise collectionExercise : exercises) {
        sampleService.distributeSampleUnits(collectionExercise);
      }
      log.info("Completed sample unit distribution");
      return ResponseEntity.ok().body("Completed sample unit distribution");
    } catch (RuntimeException e) {
      log.error(
          "Uncaught exception - transaction rolled back.", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when validating sample units");
    }
  }
}
