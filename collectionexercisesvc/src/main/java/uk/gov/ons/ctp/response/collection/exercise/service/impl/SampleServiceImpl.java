package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * The implementation of the SampleService
 *
 */
@Service
@Slf4j
public class SampleServiceImpl implements SampleService {

  private static final int TRANSACTION_TIMEOUT = 60;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private RestClient sampleRestClient;

  @Autowired
  private SampleUnitRepository sampleUnitRepo;

  @Autowired
  private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Override
  public SampleUnitsRequestDTO requestSampleUnits(final Integer exerciseId) {

    SampleUnitsRequestDTO replyDTO = null;

    CollectionExercise collectionExercise = collectRepo
        .findOne(exerciseId);
    // Check collection exercise exists
    if (collectionExercise != null) {
      CollectionExerciseJobCreationRequestDTO requestDTO = new CollectionExerciseJobCreationRequestDTO();
      requestDTO.setCollectionExerciseId(collectionExercise.getExerciseId());
      requestDTO.setSurveyRef(collectionExercise.getSurvey().getSurveyRef());
      requestDTO.setExerciseDateTime(collectionExercise.getScheduledStartDateTime());
      replyDTO = sampleRestClient.postResource(
          appConfig.getSampleSvc().getRequestSampleUnitsPath(),
          requestDTO, SampleUnitsRequestDTO.class);
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
  public ExerciseSampleUnit acceptSampleUnit(SampleUnit sampleUnit) {

    ExerciseSampleUnit exerciseSampleUnit = null;

    CollectionExercise collectionExercise = collectRepo
        .findOne(sampleUnit.getCollectionExerciseId());

    // Check collection exercise exists
    if (collectionExercise != null) {

      // Check Sample Unit doesn't already exist for collection exercise
      if (!sampleUnitRepo.tupleExists(collectionExercise.getExerciseId(), sampleUnit.getSampleId())) {

        ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
        sampleUnitGroup.setCollectionExercise(collectionExercise);
        sampleUnitGroup.setFormType(sampleUnit.getFormType());
        sampleUnitGroup.setState(SampleUnitGroupState.INIT);
        sampleUnitGroup.setCreatedDateTime(new Timestamp(new Date().getTime()));
        sampleUnitGroup = sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);

        exerciseSampleUnit = new ExerciseSampleUnit();
        exerciseSampleUnit.setSampleUnitId(sampleUnit.getSampleId());
        exerciseSampleUnit.setSampleUnitGroup(sampleUnitGroup);
        exerciseSampleUnit.setSampleUnitRef(sampleUnit.getSampleUnitRef());
        exerciseSampleUnit.setSampleUnitType(sampleUnit.getSampleUnitType());

        sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

      } else {
        log.warn("SampleUnit {} already exists for CollectionExercise {}", sampleUnit.getSampleUnitId(),
            sampleUnit.getCollectionExerciseId());
      }
    } else {
      log.error("No CollectionExercise {} for SampleUnit {}", sampleUnit.getCollectionExerciseId(),
          sampleUnit.getSampleUnitId());
    }

    return exerciseSampleUnit;
  }

}
