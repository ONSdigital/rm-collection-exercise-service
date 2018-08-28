package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;

/** Implementation to deal with sampleUnitGroups. */
@Service
public class ExerciseSampleUnitGroupServiceImpl implements ExerciseSampleUnitGroupService {
  private static final Logger log =
      LoggerFactory.getLogger(ExerciseSampleUnitGroupServiceImpl.class);

  private static final int TRANSACTION_TIMEOUT = 60;

  @Autowired private SampleUnitRepository sampleUnitRepo;

  @Autowired private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Override
  public Long countByStateFKAndCollectionExercise(
      SampleUnitGroupDTO.SampleUnitGroupState state, CollectionExercise exercise) {
    return sampleUnitGroupRepo.countByStateFKAndCollectionExercise(state, exercise);
  }

  @Override
  public List<ExerciseSampleUnitGroup>
      findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
          SampleUnitGroupDTO.SampleUnitGroupState state,
          List<CollectionExercise> exercises,
          List<Integer> excludedGroups,
          Pageable pageable) {
    return sampleUnitGroupRepo
        .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
            state, exercises, excludedGroups, pageable);
  }

  @Override
  @Transactional(
      propagation = Propagation.REQUIRED,
      readOnly = false,
      timeout = TRANSACTION_TIMEOUT)
  public ExerciseSampleUnitGroup storeExerciseSampleUnitGroup(
      ExerciseSampleUnitGroup sampleUnitGroup, List<ExerciseSampleUnit> sampleUnits) {
    ExerciseSampleUnitGroup savedExerciseSampleUnitGroup =
        sampleUnitGroupRepo.save(sampleUnitGroup);
    if (sampleUnits.isEmpty()) {
      log.warn(
          "No sampleUnits updated for SampleUnitGroup {}", sampleUnitGroup.getSampleUnitGroupPK());
    } else {
      sampleUnitRepo.save(sampleUnits);
    }
    return savedExerciseSampleUnitGroup;
  }
}
