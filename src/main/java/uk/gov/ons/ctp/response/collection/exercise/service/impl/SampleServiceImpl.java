package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.distribution.SampleUnitDistributor;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitValidationErrorDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollexSampleCountUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.validation.ValidateSampleUnits;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** The implementation of the SampleService */
@Service
public class SampleServiceImpl implements SampleService {
  private static final Logger log = LoggerFactory.getLogger(SampleServiceImpl.class);

  private static final int TRANSACTION_TIMEOUT = 60;

  @Autowired private PartySvcClient partySvcClient;

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Autowired private SampleUnitRepository sampleUnitRepo;

  @Autowired private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Autowired private CollectionExerciseRepository collectRepo;

  @Autowired private SampleSvcClient sampleSvcClient;

  @Autowired private CollexSampleCountUpdater collexSampleCountUpdater;

  @Autowired
  @Qualifier("collectionExercise")
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @Autowired private ValidateSampleUnits validate;

  @Autowired private SampleUnitDistributor distributor;

  /**
   * Method to get all the validation errors for a sample unit
   *
   * @param su a sample unit
   * @return a dto containing all the validation errors
   */
  private static SampleUnitValidationErrorDTO validateSampleUnit(final ExerciseSampleUnit su) {
    SampleUnitValidationErrorDTO dto = new SampleUnitValidationErrorDTO();

    dto.setSampleUnitRef(su.getSampleUnitRef());
    List<SampleUnitValidationErrorDTO.ValidationError> errors = new ArrayList<>();

    if (su.getCollectionInstrumentId() == null) {
      errors.add(SampleUnitValidationErrorDTO.ValidationError.MISSING_COLLECTION_INSTRUMENT);
    }
    if (su.getPartyId() == null) {
      errors.add(SampleUnitValidationErrorDTO.ValidationError.MISSING_PARTY);
    }

    SampleUnitValidationErrorDTO.ValidationError[] errorArray =
        errors.toArray(new SampleUnitValidationErrorDTO.ValidationError[errors.size()]);
    dto.setErrors(errorArray);

    return dto;
  }

  @Override
  public SampleUnitsRequestDTO requestSampleUnits(final UUID id) throws CTPException {
    CollectionExercise collectionExercise = collectRepo.findOneById(id);

    if (collectionExercise == null) {
      throw new IllegalArgumentException(String.format("Collection exercise %s not found", id));
    }

    List<SampleLink> sampleLinks = sampleLinkRepository.findByCollectionExerciseId(id);
    List<UUID> sampleSummaryIdList =
        sampleLinks.stream().map(SampleLink::getSampleSummaryId).collect(Collectors.toList());

    // Pre-grab and save the total number of sample units we expect to receive from the sample
    // service BEFORE it starts to send them, to ensure no race condition
    SampleUnitsRequestDTO responseDTO = sampleSvcClient.getSampleUnitSize(sampleSummaryIdList);
    collexSampleCountUpdater.updateSampleSize(id, responseDTO.getSampleUnitsTotal());

    // Request the sample units. They'll start arriving as soon as this line executes. Be ready!
    SampleUnitsRequestDTO replyDTO = sampleSvcClient.requestSampleUnits(collectionExercise);

    if (replyDTO != null && replyDTO.getSampleUnitsTotal() > 0) {
      for (SampleLink samplelink : sampleLinks) {
        partySvcClient.linkSampleSummaryId(
            samplelink.getSampleSummaryId().toString(), collectionExercise.getId().toString());
      }

      collectionExercise.setSampleSize(replyDTO.getSampleUnitsTotal());
      collectionExercise.setState(
          collectionExerciseTransitionState.transition(
              collectionExercise.getState(), CollectionExerciseEvent.EXECUTE));
      collectRepo.saveAndFlush(collectionExercise);
    }

    return replyDTO;
  }

  /**
   * Accepts the sample unit from the sample service. This checks that this is dealing with the
   * initial creation of the sample, no additions of sample units to a sample unit group, no updates
   * to a sample unit.
   *
   * @param sampleUnit the sample unit from the message.
   * @return the saved sample unit.
   */
  @Transactional(
      propagation = Propagation.REQUIRED,
      readOnly = false,
      timeout = TRANSACTION_TIMEOUT)
  @Override
  public ExerciseSampleUnit acceptSampleUnit(SampleUnit sampleUnit) throws CTPException {
    log.with("sample_unit", sampleUnit).debug("Processing sample unit");
    ExerciseSampleUnit exerciseSampleUnit = null;

    CollectionExercise collectionExercise =
        collectRepo.findOneById(UUID.fromString(sampleUnit.getCollectionExerciseId()));

    // Check collection exercise exists
    if (collectionExercise != null) {
      // Check Sample Unit doesn't already exist for collection exercise
      if (!sampleUnitRepo
          .existsBySampleUnitRefAndSampleUnitTypeAndSampleUnitGroupCollectionExercise(
              sampleUnit.getSampleUnitRef(),
              SampleUnitDTO.SampleUnitType.valueOf(sampleUnit.getSampleUnitType()),
              collectionExercise)) {

        ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
        sampleUnitGroup.setCollectionExercise(collectionExercise);
        sampleUnitGroup.setFormType(sampleUnit.getFormType());
        sampleUnitGroup.setStateFK(SampleUnitGroupState.INIT);
        sampleUnitGroup.setCreatedDateTime(new Timestamp(new Date().getTime()));
        sampleUnitGroup = sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);

        exerciseSampleUnit = new ExerciseSampleUnit();
        exerciseSampleUnit.setSampleUnitGroup(sampleUnitGroup);
        exerciseSampleUnit.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        exerciseSampleUnit.setSampleUnitId(UUID.fromString(sampleUnit.getId()));
        exerciseSampleUnit.setSampleUnitType(
            SampleUnitDTO.SampleUnitType.valueOf(sampleUnit.getSampleUnitType()));

        sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

        if (sampleUnitRepo.countBySampleUnitGroupCollectionExercise(collectionExercise)
            == collectionExercise.getSampleSize()) {
          collectionExercise.setState(
              collectionExerciseTransitionState.transition(
                  collectionExercise.getState(), CollectionExerciseEvent.EXECUTION_COMPLETE));
          collectionExercise.setActualExecutionDateTime(new Timestamp(new Date().getTime()));
          collectRepo.saveAndFlush(collectionExercise);
        }

      } else {
        log.with("sample_unit_type_fk", sampleUnit.getSampleUnitType())
            .with("sample_unit_ref", sampleUnit.getSampleUnitRef())
            .with("collection_exercise_id", sampleUnit.getCollectionExerciseId())
            .warn("SampleUnitRef with SampleUnitType already exists for CollectionExercise");
      }
    } else {
      log.with("sample_unit_type_fk", sampleUnit.getSampleUnitType())
          .with("sample_unit_ref", sampleUnit.getSampleUnitRef())
          .with("collection_exercise_id", sampleUnit.getCollectionExerciseId())
          .with("form_type", sampleUnit.getFormType())
          .error("No CollectionExercise");
    }

    return exerciseSampleUnit;
  }

  @Override
  public void validateSampleUnits() {
    validate.validateSampleUnits();
  }

  @Override
  public void distributeSampleUnits(CollectionExercise exercise) {
    distributor.distributeSampleUnits(exercise);
  }

  @Override
  public SampleUnitValidationErrorDTO[] getValidationErrors(
      final CollectionExercise collectionExercise) {
    List<ExerciseSampleUnit> sampleUnits =
        sampleUnitRepo.findBySampleUnitGroupCollectionExerciseAndSampleUnitGroupStateFK(
            collectionExercise, SampleUnitGroupState.FAILEDVALIDATION);
    Predicate<ExerciseSampleUnit> validTest =
        su -> su.getPartyId() == null || su.getCollectionInstrumentId() == null;
    return sampleUnits
        .stream()
        .filter(validTest)
        .map(SampleServiceImpl::validateSampleUnit)
        .toArray(SampleUnitValidationErrorDTO[]::new);
  }

  @Override
  public List<SampleLink> getSampleLinksForSummary(final UUID sampleSummaryId) {
    return sampleLinkRepository.findBySampleSummaryId(sampleSummaryId);
  }

  @Override
  public SampleLink saveSampleLink(final SampleLink sampleLink) {
    return sampleLinkRepository.saveAndFlush(sampleLink);
  }

  @Override
  public SampleSummaryDTO getSampleSummary(UUID sampleSummaryId) {
    return sampleSvcClient.getSampleSummary(sampleSummaryId);
  }
}
