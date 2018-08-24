package uk.gov.ons.ctp.response.collection.exercise.service;

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

/** Implementation to deal with sampleUnitGroups. */
@Service
public class ExerciseSampleUnitGroupService {
  private static final Logger log = LoggerFactory.getLogger(ExerciseSampleUnitGroupService.class);

  private static final int TRANSACTION_TIMEOUT = 60;

  @Autowired private SampleUnitRepository sampleUnitRepo;

  @Autowired private SampleUnitGroupRepository sampleUnitGroupRepo;

  /**
   * Count SampleUnitGroups by state in a CollectionExercise.
   *
   * @param state filter criteria.
   * @param exercise CollectionExercise for which to provide count.
   * @return count of SampleUnitGroups in provided state.
   */
  public Long countByStateFKAndCollectionExercise(
      SampleUnitGroupDTO.SampleUnitGroupState state, CollectionExercise exercise) {
    return sampleUnitGroupRepo.countByStateFKAndCollectionExercise(state, exercise);
  }

  /**
   * Query repository for SampleUnitGroups by state and belonging to a list of collection exercises.
   * Order by CreatedDateTime ascending.
   *
   * @param state filter criteria.
   * @param exercises for which to return SampleUnitGroups.
   * @param excludedGroups SampleUnitGroupPKs to exclude from results.
   * @param pageable Spring JPA pageable object used to return required number of results.
   * @return returns requested number of SampleUnitGroups for state within requested exercises.
   */
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

  /**
   * To store ExerciseSampleUnitGroup and associated ExerciseSampleUnits
   *
   * @param sampleUnitGroup to be stored
   * @param sampleUnits associated sampleUnitGroup to be stored.
   * @return the stored ExerciseSampleUnitGroup
   */
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
