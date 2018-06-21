package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.common.util.MultiIsoDateFormat;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.representation.CaseTypeDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkedSampleSummariesDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitValidationErrorDTO;
import uk.gov.ons.ctp.response.collection.exercise.schedule.SchedulerConfiguration;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.response.survey.representation.SurveyDTO;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.net.URI;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private CollectionExerciseService collectionExerciseService;

    private SurveyService surveyService;

    private SampleService sampleService;

    private EventService eventService;

    private MapperFacade mapperFacade;

    private Scheduler scheduler;

    @Autowired
    public CollectionExerciseEndpoint(CollectionExerciseService collectionExerciseService,
                                      SurveyService surveyService,
                                      SampleService sampleService,
                                      EventService eventService,
                                      @Qualifier("collectionExerciseBeanMapper") MapperFacade mapperFacade,
                                      Scheduler scheduler) {
        this.collectionExerciseService = collectionExerciseService;
        this.surveyService = surveyService;
        this.sampleService = sampleService;
        this.eventService = eventService;
        this.mapperFacade = mapperFacade;
        this.scheduler = scheduler;
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
     * GET to find collection exercises from the collection exercise service for
     * the given survey Id.
     *
     * @param id survey Id for which to trigger delivery of collection exercises
     * @return list of collection exercises associated to survey
     * @throws CTPException on resource not found
     */
    @RequestMapping(value = "/survey/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<CollectionExerciseDTO>> getCollectionExercisesForSurvey(
            @PathVariable("id") final UUID id) throws CTPException {

        SurveyDTO survey = surveyService.findSurvey(id);

        List<CollectionExerciseDTO> collectionExerciseSummaryDTOList;

        if (survey == null) {
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                    String.format("%s %s", RETURN_SURVEYNOTFOUND, id));
        } else {
            log.debug("Entering collection exercise fetch with survey Id {}", id);
            List<CollectionExercise> collectionExerciseList = collectionExerciseService
                    .findCollectionExercisesForSurvey(survey);
            collectionExerciseSummaryDTOList = collectionExerciseList
                    .stream()
                    .map(collex -> getCollectionExerciseDTO(collex))
                    .collect(Collectors.toList());
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
     *           collection exercise
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

        CollectionExerciseDTO collectionExerciseDTO = getCollectionExerciseDTO(collectionExercise);

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
            CollectionExerciseDTO collectionExerciseDTO = getCollectionExerciseDTO(collectionExercise);
            result.add(collectionExerciseDTO);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * PUT request to update a collection exercise
     *
     * @param id        Collection exercise Id to update
     * @param collexDto a DTO containing survey id, name, user description and exercise ref
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
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
     * A method to manually validate the constraints on a CollectionExerciseDTO.  The reason this is needed is for
     * validating PUTs to subresources.  As these are the literal field values and not a JSON document it's difficult
     * to get Spring to validate these automatically. Hence the need for some kind of manual validation
     *
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
     *
     * @param id        the uuid of the collection exercise to update
     * @param collexDto a dto containing some or all of the data to update
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException thrown if constraint violation etc
     */
    private ResponseEntity<?> patchCollectionExercise(final UUID id, final CollectionExerciseDTO collexDto)
            throws CTPException {
        validateConstraints(collexDto);

        this.collectionExerciseService.patchCollectionExercise(id, collexDto);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT request to update a collection exercise scheduledStartDateTime
     *
     * @param id             Collection exercise Id to update
     * @param scheduledStart new value for exercise ref
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
     */
    @RequestMapping(value = "/{id}/scheduledStart", method = RequestMethod.PUT, consumes = "text/plain")
    public ResponseEntity<?> patchCollectionExerciseScheduledStart(
            @PathVariable("id") final UUID id,
            final @RequestBody String scheduledStart)
            throws CTPException {
        log.info("Updating collection exercise {}, setting scheduledStartDateTime to {}", id, scheduledStart);
        CollectionExerciseDTO collexDto = new CollectionExerciseDTO();

        try {
            LocalDateTime date = LocalDateTime.parse(scheduledStart,
                    DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX", Locale.ROOT));
            collexDto.setScheduledStartDateTime(java.sql.Timestamp.valueOf(date));

            return patchCollectionExercise(id, collexDto);
        } catch (DateTimeParseException e) {
            throw new CTPException(CTPException.Fault.BAD_REQUEST, String.format("Unparseable date %s (%s)",
                    scheduledStart, e.getLocalizedMessage()));
        }
    }

    /**
     * PUT request to update a collection exercise exerciseRef
     *
     * @param id          Collection exercise Id to update
     * @param exerciseRef new value for exercise ref
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
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
     *
     * @param id   Collection exercise Id to update
     * @param name new value for name
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
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
     *
     * @param id              Collection exercise Id to update
     * @param userDescription new value for user description
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
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
     *
     * @param id       Collection exercise Id to update
     * @param surveyId The new survey to associate with this collection exercise
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
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
     * Also creates required action plans and casetypeoverrides
     *
     * @param collex A dto containing the data about the collection exercise
     * @return 201 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> createCollectionExercise(
            final @Validated(CollectionExerciseDTO.PostValidation.class) @RequestBody CollectionExerciseDTO collex)
            throws CTPException {
        log.info("Creating collection exercise, ExerciseRef: {}, SurveyRef: {}",
                collex.getExerciseRef(), collex.getSurveyRef());
        String surveyId = collex.getSurveyId();
        String surveyRef = collex.getSurveyRef();
        SurveyDTO survey;

        // Retrieve survey from survey service if it exists
        if (!StringUtils.isBlank(surveyId)) {
            survey = this.surveyService.findSurvey(UUID.fromString(collex.getSurveyId()));
        } else if (!StringUtils.isBlank(surveyRef)) {
            survey = this.surveyService.findSurveyByRef(surveyRef);
            // Downstream expects the surveyId to be present so add it now
            collex.setSurveyId(survey.getId());
        } else {
            throw new CTPException(CTPException.Fault.BAD_REQUEST, "No survey specified");
        }
        if (survey == null) {
            throw new CTPException(CTPException.Fault.BAD_REQUEST, "Invalid survey: " + surveyId);
        }
        // Check if collection exercise already exists
        CollectionExercise existing = this.collectionExerciseService.findCollectionExercise(
                collex.getExerciseRef(), survey);
        if (existing != null) {
            throw new CTPException(CTPException.Fault.RESOURCE_VERSION_CONFLICT,
                    String.format("Collection exercise already exists, ExerciseRef: %s, SurveyRef: %s",
                            collex.getExerciseRef(), surveyRef));
        }

        CollectionExercise newCollex = this.collectionExerciseService.createCollectionExercise(collex, survey);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCollex.getId()).toUri();
        log.info("Successfully created collection exercise, CollectionExerciseId: {}", newCollex.getId());
        return ResponseEntity.created(location).build();
    }

    /**
     * DELETE request which deletes a collection exercise
     *
     * @param id Collection exercise Id to delete
     * @return the collection exercise that was to be deleted
     * @throws CTPException on resource not found
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
     *                             summaries to
     * @param linkSampleSummaryDTO the array of all sample summaries to link to
     *                             the collection exercise, including summaries previously linked as
     *                             all currently linked summaries are removed from the table
     * @param bindingResult        the bindingResult used to validate requests
     * @return list of the newly linked collection exercises and sample summaries
     * @throws InvalidRequestException if binding errors
     * @throws CTPException            on resource not found
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
                .linkSampleSummaryToCollectionExercise(collectionExerciseId,
                        linkSampleSummaryDTO.getSampleSummaryIds());
        LinkedSampleSummariesDTO result = new LinkedSampleSummariesDTO();

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
     * Endpoint to return the list of samples linked to a collection exercise
     *
     * @param collectionExerciseId the collection exercise for which the links are required
     * @return a list of sample summaries linked to the collection exercise
     */
    @RequestMapping(value = "/link/{collectionExerciseId}", method = RequestMethod.GET,
            produces = "application/vnd.ons.sdc.samplelink.v1+json")
    public ResponseEntity<List<SampleLinkDTO>> getSampleLinks(
            @PathVariable("collectionExerciseId") final UUID collectionExerciseId) {
        log.debug("Getting linked sample summaries for {}", collectionExerciseId);
        List<SampleLink> sampleLinks = this.collectionExerciseService.findLinkedSampleSummaries(collectionExerciseId);
        List<SampleLinkDTO> sampleLinkList = mapperFacade.mapAsList(sampleLinks, SampleLinkDTO.class);

        return ResponseEntity.ok(sampleLinkList);
    }

    /**
     * for unlinking sample summary from a collection exercise
     *
     * @param collectionExerciseId the collection exercise to unlink from sample
     * @param sampleSummaryId      the collection exercise to unlink from collection exercise
     * @return noContent response
     * @throws CTPException on resource not found
     */
    @RequestMapping(value = "/unlink/{collectionExerciseId}/sample/{sampleSummaryId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> unlinkSampleSummary(
            @PathVariable("collectionExerciseId") final UUID collectionExerciseId,
            @PathVariable("sampleSummaryId") final UUID sampleSummaryId) throws CTPException {
        log.debug("Entering unlinkSampleSummary with collectionExerciseID {} and sampleSummaryId {}",
                collectionExerciseId, sampleSummaryId);

        CollectionExercise collectionExercise = collectionExerciseService.findCollectionExercise(collectionExerciseId);
        if (collectionExercise == null) {
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                    String.format("%s %s", RETURN_COLLECTIONEXERCISENOTFOUND, collectionExerciseId));
        }

        collectionExerciseService.removeSampleSummaryLink(sampleSummaryId, collectionExerciseId);

        return ResponseEntity.noContent().build();

    }

    /**
     * return a list of UUIDs for the sample summaries linked to a specific
     * collection exercise
     *
     * @param collectionExerciseId the id of the collection exercise to get linked
     *                             sample summaries for
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
     *                           case types and surveyIds for
     * @return a CollectionExerciseDTO of the collection exercise with case types
     * and surveyId added which is output by the endpoints
     */
    private CollectionExerciseDTO getCollectionExerciseDTO(final CollectionExercise collectionExercise) {
        Collection<CaseType> caseTypeList = collectionExerciseService.getCaseTypesList(collectionExercise);
        List<CaseTypeDTO> caseTypeDTOList = mapperFacade.mapAsList(caseTypeList, CaseTypeDTO.class);

        CollectionExerciseDTO collectionExerciseDTO = mapperFacade.map(collectionExercise, CollectionExerciseDTO.class);
        collectionExerciseDTO.setCaseTypes(caseTypeDTOList);
        // NOTE: this method used to fail (NPE) if the survey did not exist in the local database.  Now the survey id
        // is not validated and passed on verbatim
        collectionExerciseDTO.setSurveyId(collectionExercise.getSurveyId().toString());

        // If we are in the failed validation state, then there should be validation error so go look them up.
        // We don't do this for all the states as this is a non-trivial database operation.
        // Note: this code here will suppress any validation errors that are present in the other states
        // (shouldn't happen but ...)
        if (collectionExercise.getState() == CollectionExerciseDTO.CollectionExerciseState.FAILEDVALIDATION) {
            SampleUnitValidationErrorDTO[] errors = this.sampleService.getValidationErrors(collectionExercise.getId());

            collectionExerciseDTO.setValidationErrors(errors);
        }

        return collectionExerciseDTO;
    }

    @RequestMapping(value = "/{id}/events", method = RequestMethod.POST)
    public ResponseEntity<?> createCollectionExerciseEvent(
            @PathVariable("id") final UUID id,
            final @RequestBody EventDTO eventDto)
            throws CTPException {
        log.info("Creating event {} for collection exercise {}", eventDto.getTag(), id);

        eventDto.setCollectionExerciseId(id);

        Event newEvent = eventService.createEvent(eventDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}/events/{tag}")
                .buildAndExpand(newEvent.getId(), newEvent.getTag()).toUri();

        try {
            SchedulerConfiguration.scheduleEvent(this.scheduler, newEvent);
        } catch (SchedulerException e) {
            log.error("Failed to schedule event: " + newEvent);
        }

        return ResponseEntity.created(location).build();
    }


    @RequestMapping(value = "/{id}/events", method = RequestMethod.GET)
    public ResponseEntity<List<EventDTO>> getCollectionExerciseEvents(
            @PathVariable("id") final UUID id)
            throws CTPException {
        List<EventDTO> result =
                this.eventService.getEvents(id).stream().map(
                        EventService::createEventDTOFromEvent).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * PUT request to update a collection event date
     *
     * @param id  Collection exercise Id
     * @param tag collection exercise event tag
     * @return 200 if all is ok, 400 for bad request, 409 for conflict
     * @throws CTPException on resource not found
     */
    @RequestMapping(value = "/{id}/events/{tag}", method = RequestMethod.PUT, consumes = "text/plain")
    public ResponseEntity<?> updateEventDate(
            @PathVariable("id") final UUID id,
            @PathVariable("tag") final String tag,
            final @RequestBody String date)
            throws CTPException {

        log.info("Adding collection exercise {}, setting date to {}", id, date);

        try {
            MultiIsoDateFormat dateParser = new MultiIsoDateFormat();
            eventService.updateEvent(id, tag, dateParser.parse(date));
            return ResponseEntity.noContent().build();
        } catch (ParseException e) {
            throw new CTPException(CTPException.Fault.BAD_REQUEST, String.format("Unparseable date %s (%s)", date,
                    e.getLocalizedMessage()));
        }
    }


    /**
     * GET to find event from the collection exercise service for
     * the given event tag collection Id.
     *
     * @param id  collection exercise id
     * @param tag collection exercise event tag
     * @return event associated to collection exercise
     * @throws CTPException on resource not found
     */
    @RequestMapping(value = "/{id}/events/{tag}", method = RequestMethod.GET)
    public ResponseEntity<Event> getEvent(@PathVariable("id") final UUID id, @PathVariable("tag") final String tag)
            throws CTPException {
        log.debug("Entering Event fetch with event id {}, event tag {} ", id, tag);

        Event event = eventService.getEvent(id, tag);

        return ResponseEntity.ok(event);
    }


    /**
     * DELETE request to delete a collection exercise event
     *
     * @param id  Collection exercise Id
     * @param tag collection exercise event tag
     * @return the collection exercise event that was to be deleted
     * @throws CTPException on resource not found
     */
    @RequestMapping(value = "/{id}/events/{tag}", method = RequestMethod.DELETE)
    public ResponseEntity<Event> deleteCollectionExerciseEvent(@PathVariable("id") final UUID id,
                                                               @PathVariable("tag") final String tag)
            throws CTPException {
        log.info("Deleting collection exercise event id {}, event tag ", id, tag);

        eventService.deleteEvent(id, tag);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get's the list of all scheduled events from quartz (i.e. it's not the list of events as stored in the database,
     * it's the actual jobs scheduled in quartz)
     * @return the list of events derived from quartz jobs
     * @throws SchedulerException thrown if issues getting data from quartz
     */
    @RequestMapping(value = "/events/scheduled", method = RequestMethod.GET)
    public ResponseEntity<List<EventDTO>> getAllScheduledEvents() throws SchedulerException {
        List<EventDTO> scheduledEvents = SchedulerConfiguration.getAllScheduledEvents(this.scheduler);

        return ResponseEntity.ok(scheduledEvents);
    }

    /**
     * Endpoint to get a collection exercise by exercise ref and survey ref
     *
     * @param exerciseRef the exercise ref
     * @param surveyRef   the survey ref
     * @return 200 with collection exercise body if found, otherwise 404
     * @throws CTPException
     */
    @RequestMapping(value = "/{exerciseRef}/survey/{surveyRef}", method = RequestMethod.GET)
    public ResponseEntity<CollectionExerciseDTO> getCollectionExercisesForSurvey(
            @PathVariable("exerciseRef") final String exerciseRef, @PathVariable("surveyRef") final String surveyRef)
            throws CTPException {
        CollectionExercise collex = this.collectionExerciseService.findCollectionExercise(surveyRef, exerciseRef);

        if (collex == null) {
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                    String.format("Cannot find collection exercise for survey %s and period %s", surveyRef,
                            exerciseRef));
        } else {
            return ResponseEntity.ok(getCollectionExerciseDTO(collex));
        }
    }
}
