package uk.gov.ons.ctp.response.collection.exercise.validation;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.lib.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyClassifierTypeDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;

/** Class responsible for business logic to validate SampleUnits. */
@Component
public class ValidateSampleUnits {

  private static final Logger log = LoggerFactory.getLogger(ValidateSampleUnits.class);

  private static final String CASE_TYPE_SELECTOR = "COLLECTION_INSTRUMENT";

  private CollectionExerciseService collexService;
  private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

  private CollectionInstrumentSvcClient collectionInstrumentSvcClient;
  private PartySvcClient partySvcClient;
  private SurveySvcClient surveySvcClient;

  private SampleUnitRepository sampleUnitRepo;

  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  public ValidateSampleUnits(
      final CollectionExerciseService collexService,
      final ExerciseSampleUnitGroupService sampleUnitGroupSvc,
      final CollectionInstrumentSvcClient collectionInstrumentSvcClient,
      final PartySvcClient partySvcClient,
      final SurveySvcClient surveySvcClient,
      final SampleUnitRepository sampleUnitRepo,
      final @Qualifier("sampleUnitGroup") StateTransitionManager<
                  SampleUnitGroupState, SampleUnitGroupEvent>
              sampleUnitGroupState) {
    this.collexService = collexService;
    this.sampleUnitGroupSvc = sampleUnitGroupSvc;
    this.collectionInstrumentSvcClient = collectionInstrumentSvcClient;
    this.partySvcClient = partySvcClient;
    this.surveySvcClient = surveySvcClient;
    this.sampleUnitRepo = sampleUnitRepo;
    this.sampleUnitGroupState = sampleUnitGroupState;
  }

  /** Validate SampleUnits and transition collection exercises */
  @Transactional
  public void validateSampleUnits() {
    // Make sure that any collection exercises which were having their sample units sent from
    // the sample service are state transitioned once they've got all their sample units.
    updateExecutingCollectionExercises();

    // This is a hand-rolled cache which stores all the collection instruments which are needed
    // to be added onto the samples which are being 'validated'. The map allows a collection
    // exercise and a sample form type to be mapped to the correct collection instrument. It
    // provides a very significant performance boost when 'validating' large samples (e.g. 100k+)
    Map<CollectionExercise, Map<String, Optional<UUID>>> collectionInstrumentMap = new HashMap<>();

    log.debug("beginning validation of sample units");
    try (Stream<ExerciseSampleUnit> sampleUnits =
        sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            CollectionExerciseState.EXECUTED, SampleUnitGroupState.INIT)) {

      sampleUnits.forEach(
          (sampleUnit) -> {
            log.with("sampleUnitPK", sampleUnit.getSampleUnitPK())
                .with("collectionInstrumentId", sampleUnit.getCollectionInstrumentId())
                .info("Beginning validation of sample unit");
            final CollectionExercise collex =
                sampleUnit.getSampleUnitGroup().getCollectionExercise();

            // If we haven't seen this collex before, create a map to store the CI
            Map<String, Optional<UUID>> formTypeMap =
                collectionInstrumentMap.computeIfAbsent(collex, key -> new HashMap<>());

            // If we haven't seen this form type before, add the CI to the cache if we can find it
            Optional<UUID> collectionInstrumentId =
                formTypeMap.computeIfAbsent(
                    sampleUnit.getSampleUnitGroup().getFormType(),
                    key -> {
                      UUID returnValue = null;
                      List<String> classifierTypes = requestSurveyClassifiers(collex);
                      try {
                        returnValue =
                            requestCollectionInstrumentId(
                                classifierTypes, sampleUnit, collex.getSurveyId().toString());
                      } catch (HttpClientErrorException e) {
                        if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                          log.with("sample_unit", sampleUnit)
                              .with("status_code", e.getStatusCode())
                              .error("Unexpected HTTP response code from collection instrument");
                          throw e; // Re-throw anything that's not a 404 so that we retry
                        }
                      }

                      return Optional.ofNullable(returnValue);
                    });

            // If we could find the CI, then set it on the sample (or it will fail validation)
            if (collectionInstrumentId.isPresent()) {
              sampleUnit.setCollectionInstrumentId(collectionInstrumentId.get());
            }

            if (sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.B) {
              try {
                PartyDTO party = partySvcClient.requestParty(sampleUnit.getSampleUnitRef());
                sampleUnit.setPartyId(UUID.fromString(party.getId()));
              } catch (HttpClientErrorException e) {
                if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                  log.with("sample_unit", sampleUnit)
                      .with("status_code", e.getStatusCode())
                      .error("Unexpected HTTP response code from party service");
                  throw e; // Re-throw anything that's not a 404 so that we retry
                }
              }
            }

            ExerciseSampleUnitGroup sampleUnitGroup = sampleUnit.getSampleUnitGroup();
            sampleUnitGroup.setModifiedDateTime(new Timestamp(new Date().getTime()));

            SampleUnitGroupEvent event = SampleUnitGroupEvent.VALIDATE;
            if (!isSampleUnitValid(sampleUnit)) {
              log.with("partyId", sampleUnit.getPartyId())
                  .with("collectionInstrumentId", sampleUnit.getCollectionInstrumentId())
                  .with("sampleUnitPK", sampleUnit.getSampleUnitPK())
                  .info("Sample unit is invalid");
              event = SampleUnitGroupEvent.INVALIDATE;
            }

            try {
              sampleUnitGroup.setStateFK(
                  sampleUnitGroupState.transition(
                      sampleUnit.getSampleUnitGroup().getStateFK(), event));
            } catch (CTPException e) {
              throw new IllegalStateException(); // Not thrown because we already checked the state
            }

            sampleUnitRepo.save(sampleUnit);
          });
    }

    log.debug("beginning transitioning of collection exercises");
    collectionInstrumentMap
        .keySet()
        .forEach(
            (exercise) -> {
              try {
                transitionCollectionExercise(exercise);
              } catch (CTPException e) {
                throw new IllegalStateException(); // Not thrown because we already checked the
                // state
              }
            });
  }

  private void updateExecutingCollectionExercises() {
    List<CollectionExercise> exercises =
        collexService.findByState(CollectionExerciseState.EXECUTION_STARTED);

    log.debug("Found [" + exercises.size() + "] collection exercises in EXECUTION_STARTED state");
    for (CollectionExercise collex : exercises) {
      log.with("collection_exercise_id", collex.getId())
          .info("Checking number of records in sample unit group match the sampleSize");
      if (collex.getSampleSize() != null
          && sampleUnitRepo.countBySampleUnitGroupCollectionExercise(collex)
              == collex.getSampleSize()) {

        log.with("id", collex.getId())
            .info(
                "Number of records in sample unit group match the sampleSize, about to transition");
        collex.setActualExecutionDateTime(new Timestamp(new Date().getTime()));

        try {
          collexService.transitionCollectionExercise(
              collex, CollectionExerciseEvent.EXECUTION_COMPLETE);
        } catch (CTPException e) {
          throw new IllegalStateException(); // Never thrown because we already checked the state
        }
      } else {
        log.with("collection_exercise_id", collex.getId())
            .with("sampleSize", collex.getSampleSize())
            .with(
                "sample_group_count",
                sampleUnitRepo.countBySampleUnitGroupCollectionExercise(collex))
            .info("Number of records in sample unit group did not match the sampleSize");
      }
    }
  }

  /**
   * Request the classifier type selectors from the Survey service.
   *
   * @param exercise for which to get collection instrument classifier selectors.
   * @return List<String> Survey classifier type selectors for exercise
   */
  private List<String> requestSurveyClassifiers(CollectionExercise exercise) {

    SurveyClassifierTypeDTO classifierTypeSelector;

    // Call Survey Service
    // Get Classifier types for Collection Instruments
    List<SurveyClassifierDTO> classifierTypeSelectors =
        surveySvcClient.requestClassifierTypeSelectors(exercise.getSurveyId());
    SurveyClassifierDTO chosenSelector =
        classifierTypeSelectors
            .stream()
            .filter(classifierType -> CASE_TYPE_SELECTOR.equals(classifierType.getName()))
            .findAny()
            .orElse(null);
    if (chosenSelector != null) {
      classifierTypeSelector =
          surveySvcClient.requestClassifierTypeSelector(
              exercise.getSurveyId(), UUID.fromString(chosenSelector.getId()));
      if (classifierTypeSelector != null) {
        return classifierTypeSelector.getClassifierTypes();
      } else {
        log.with("survey_id", exercise.getSurveyId())
            .with("case_type_selector_id", chosenSelector.getId())
            .error("Error requesting Survey Classifier Types");
        throw new IllegalStateException("Error requesting Survey Classifier Types");
      }
    } else {
      log.with("survey_id", exercise.getSurveyId())
          .error("Error requesting Survey Classifier Types");
      throw new IllegalStateException("Error requesting Survey Classifier Types");
    }
  }

  /**
   * Request the Collection Instrument details from the Collection Instrument Service using the
   * given classifiers and return the instrument Id.
   *
   * @param classifierTypes used in search by Collection Instrument service to return instrument
   *     details matching classifiers.
   * @param sampleUnit to which the collection instrument relates.
   * @return UUID of collection instrument or null if not found.
   * @throws RestClientException something went wrong making http call
   */
  private UUID requestCollectionInstrumentId(
      List<String> classifierTypes, ExerciseSampleUnit sampleUnit, String surveyId) {
    Map<String, String> classifiers = new HashMap<>();
    classifiers.put("SURVEY_ID", surveyId);
    for (String classifier : classifierTypes) {
      try {
        CollectionInstrumentClassifierTypes classifierType =
            CollectionInstrumentClassifierTypes.valueOf(classifier);
        classifiers.put(classifierType.name(), classifierType.apply(sampleUnit));
      } catch (IllegalArgumentException e) {
        log.with("classifier", classifier).warn("Classifier not supported", e);
      }
    }
    String searchString = convertToJSON(classifiers);
    List<CollectionInstrumentDTO> collectionInstruments =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    UUID collectionInstrumentId;
    if (collectionInstruments.isEmpty()) {
      log.with("search_string", searchString).error("No collection instruments found");
      collectionInstrumentId = null;
    } else if (collectionInstruments.size() > 1) {
      log.with("collection_instruments_found", collectionInstruments.size())
          .with("search_string", searchString)
          .warn("Multiple collection instruments found, taking most recent first");
      collectionInstrumentId = collectionInstruments.get(0).getId();
    } else {
      collectionInstrumentId = collectionInstruments.get(0).getId();
    }

    return collectionInstrumentId;
  }

  /**
   * Convert map of classifier types and values to JSON search string.
   *
   * @param classifiers classifier types and values from which to construct search String.
   * @return JSON string used in search.
   */
  private String convertToJSON(Map<String, String> classifiers) {
    JSONObject searchString = new JSONObject(classifiers);
    return searchString.toString();
  }

  /**
   * Validates and transitions the collection exercise.
   *
   * @param exercise A collection exercise database object
   * @throws CTPException thrown when transitioning state fails
   */
  private void transitionCollectionExercise(CollectionExercise exercise) throws CTPException {
    CollectionExerciseEvent event = null;
    long init =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(SampleUnitGroupState.INIT, exercise);
    long validated =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.VALIDATED, exercise);
    long failed =
        sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.FAILEDVALIDATION, exercise);

    if (validated == exercise.getSampleSize().longValue()) {
      // All sample units validated, set exercise state to VALIDATE
      event = CollectionExerciseEvent.VALIDATE;
      log.with("collection_exercise_id", exercise.getId())
          .info("State of collection exercise is now VALIDATE");
    } else if (init < 1 && failed > 0) {
      // None left to validate but some failed, set exercise to INVALIDATE
      log.with("collection_exercise_id", exercise.getId())
          .with("failed_count", failed)
          .with("init_count", init)
          .info("State of collection exercise is now INVALIDATE as it failed validation");
      event = CollectionExerciseEvent.INVALIDATE;
    }

    if (event != null) {
      collexService.transitionCollectionExercise(exercise, event);
    } else {
      log.info("Event is null, not transitioning collection exercise state");
    }
  }

  private boolean isSampleUnitValid(ExerciseSampleUnit sampleUnit) {
    if (sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.H) {
      return sampleUnit.getCollectionInstrumentId() != null && sampleUnit.getPartyId() == null;
    } else if (sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.B
        || sampleUnit.getSampleUnitType() == SampleUnitDTO.SampleUnitType.BI) {
      return sampleUnit.getCollectionInstrumentId() != null && sampleUnit.getPartyId() != null;
    } else {
      return false;
    }
  }
}
