package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.distribution.SampleUnitDistributor;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitValidationErrorDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.validation.ValidateSampleUnits;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The implementation of the SampleService
 */
@Service
@Slf4j
public class SampleServiceImpl implements SampleService {

    private static final int TRANSACTION_TIMEOUT = 60;

    @Autowired
    private SampleUnitRepository sampleUnitRepo;

    @Autowired
    private SampleUnitGroupRepository sampleUnitGroupRepo;

    @Autowired
    private CollectionExerciseRepository collectRepo;

    @Autowired
    private SampleSvcClient sampleSvcClient;

    @Autowired
    @Qualifier("collectionExercise")
    private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseTransitionState;

    @Autowired
    private ValidateSampleUnits validate;

    @Autowired
    private SampleUnitDistributor distributor;

    /**
     * Method to get all the validation errors for a sample unit
     * @param su a sample unit
     * @return a dto containing all the validation errors
     */
    private static SampleUnitValidationErrorDTO validateSampleUnit(final ExerciseSampleUnit su) {
        SampleUnitValidationErrorDTO dto = new SampleUnitValidationErrorDTO();

        dto.setSampleUnitRef(su.getSampleUnitRef());
        List<SampleUnitValidationErrorDTO.ValidationError> errors = new ArrayList<>();

        if (su.getCollectionInstrumentId() == null || !(su.getCollectionInstrumentId() instanceof UUID)) {
            errors.add(SampleUnitValidationErrorDTO.ValidationError.MISSING_COLLECTION_INSTRUMENT);
        }
        if (su.getPartyId() == null || !(su.getPartyId() instanceof UUID)) {
            errors.add(SampleUnitValidationErrorDTO.ValidationError.MISSING_PARTY);
        }

        SampleUnitValidationErrorDTO.ValidationError[] errorArray =
                errors.toArray(new SampleUnitValidationErrorDTO.ValidationError[errors.size()]);
        dto.setErrors(errorArray);

        return dto;
    }

    @Override
    public SampleUnitsRequestDTO requestSampleUnits(final UUID id) throws CTPException {

        SampleUnitsRequestDTO replyDTO = null;

        CollectionExercise collectionExercise = collectRepo.findOneById(id);

        // Check collection exercise exists
        if (collectionExercise != null) {
            replyDTO = sampleSvcClient.requestSampleUnits(collectionExercise);

            if (replyDTO != null && replyDTO.getSampleUnitsTotal() > 0) {
                log.info("HERE");
                collectionExercise.setSampleSize(replyDTO.getSampleUnitsTotal());

                collectionExercise.setState(collectionExerciseTransitionState.transition(collectionExercise.getState(),
                        CollectionExerciseEvent.EXECUTE));
                collectRepo.saveAndFlush(collectionExercise);
            }
        }
        return replyDTO;
    }

    /**
     * Accepts the sample unit from the sample service. This checks that this is
     * dealing with the initial creation of the sample, no additions of sample
     * units to a sample unit group, no updates to a sample unit.
     *
     * @param sampleUnit the sample unit from the message.
     * @return the saved sample unit.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false, timeout = TRANSACTION_TIMEOUT)
    @Override
    public ExerciseSampleUnit acceptSampleUnit(SampleUnit sampleUnit) throws CTPException {
        ExerciseSampleUnit exerciseSampleUnit = null;

        CollectionExercise collectionExercise = collectRepo.findOneById(
                UUID.fromString(sampleUnit.getCollectionExerciseId()));

        // Check collection exercise exists
        if (collectionExercise != null) {
            // Check Sample Unit doesn't already exist for collection exercise
            if (!sampleUnitRepo.tupleExists(collectionExercise.getExercisePK(), sampleUnit.getSampleUnitRef(),
                    sampleUnit.getSampleUnitType())) {

                ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
                sampleUnitGroup.setCollectionExercise(collectionExercise);
                sampleUnitGroup.setFormType(sampleUnit.getFormType());
                sampleUnitGroup.setStateFK(SampleUnitGroupState.INIT);
                sampleUnitGroup.setCreatedDateTime(new Timestamp(new Date().getTime()));
                sampleUnitGroup = sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);

                exerciseSampleUnit = new ExerciseSampleUnit();
                exerciseSampleUnit.setSampleUnitGroup(sampleUnitGroup);
                exerciseSampleUnit.setSampleUnitRef(sampleUnit.getSampleUnitRef());
                exerciseSampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.valueOf(sampleUnit.getSampleUnitType()));

                sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

                if (sampleUnitRepo.totalByExercisePK(collectionExercise.getExercisePK()) == collectionExercise
                        .getSampleSize()) {
                    collectionExercise.setState(collectionExerciseTransitionState.transition(collectionExercise.getState(),
                            CollectionExerciseEvent.EXECUTION_COMPLETE));
                    collectionExercise.setActualExecutionDateTime(new Timestamp(new Date().getTime()));
                    collectRepo.saveAndFlush(collectionExercise);
                }

            } else {
                log.warn("SampleUnitRef {} with  setSampleUnitTypeFK {} already exists for CollectionExercise {}",
                        sampleUnit.getSampleUnitRef(),
                        sampleUnit.getSampleUnitType(), sampleUnit.getCollectionExerciseId());
            }
        } else {
            log.error("No CollectionExercise {} for SampleUnit Ref: {} Type: {}, FormType: {}",
                    sampleUnit.getCollectionExerciseId(),
                    sampleUnit.getSampleUnitRef(), sampleUnit.getSampleUnitType(), sampleUnit.getFormType());
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
    public boolean partyExists(final UUID id) {
        return this.sampleUnitRepo.partyExists(id);
    }

    @Override
    public SampleUnitValidationErrorDTO[] getValidationErrors(final UUID collectionExerciseId) {
        List<ExerciseSampleUnit> sampleUnits = this.sampleUnitRepo.findInvalidByCollectionExercise(collectionExerciseId);
        Predicate<ExerciseSampleUnit> validTest = su -> !(su.getPartyId() instanceof UUID)
                || !(su.getCollectionInstrumentId() instanceof UUID);
        return sampleUnits
                .stream()
                .filter(validTest)
                .map(SampleServiceImpl::validateSampleUnit)
                .toArray(SampleUnitValidationErrorDTO[]::new);
    }
}
