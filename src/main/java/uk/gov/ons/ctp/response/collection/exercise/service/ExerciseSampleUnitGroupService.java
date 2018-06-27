package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;

/** Service responsible for dealing with stored ExerciseSampleUnitGroups. */
public interface ExerciseSampleUnitGroupService {

  /**
   * Count SampleUnitGroups by state in a CollectionExercise.
   *
   * @param state filter criteria.
   * @param exercise CollectionExercise for which to provide count.
   * @return count of SampleUnitGroups in provided state.
   */
  Long countByStateFKAndCollectionExercise(
      SampleUnitGroupDTO.SampleUnitGroupState state, CollectionExercise exercise);

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
  List<ExerciseSampleUnitGroup>
      findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
          SampleUnitGroupDTO.SampleUnitGroupState state,
          List<CollectionExercise> exercises,
          List<Integer> excludedGroups,
          Pageable pageable);

  /**
   * To store ExerciseSampleUnitGroup and associated ExerciseSampleUnits
   *
   * @param sampleUnitGroup to be stored
   * @param sampleUnits associated sampleUnitGroup to be stored.
   * @return the stored ExerciseSampleUnitGroup
   */
  ExerciseSampleUnitGroup storeExerciseSampleUnitGroup(
      ExerciseSampleUnitGroup sampleUnitGroup, List<ExerciseSampleUnit> sampleUnits);
}
