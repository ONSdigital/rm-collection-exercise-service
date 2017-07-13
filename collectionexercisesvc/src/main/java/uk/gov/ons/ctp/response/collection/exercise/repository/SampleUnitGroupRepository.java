package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;

/**
 * Spring JPA Repository for ExerciseSampleUnitGroup
 *
 */
@Repository
public interface SampleUnitGroupRepository extends JpaRepository<ExerciseSampleUnitGroup, Integer> {

  /**
   * Count SampleUnitGroups by state in a CollectionExercise.
   *
   * @param state filter criteria.
   * @param exercise CollectionExercise for which to provide count.
   * @return count of SampleUnitGroups in provided state.
   */
  Long countByStateFKAndCollectionExercise(SampleUnitGroupDTO.SampleUnitGroupState state, CollectionExercise exercise);

  /**
   * Query repository for SampleUnitGroups by state and belonging to a list of
   * collection exercises. Order by ModifiedDateTime ascending.
   *
   * @param state filter criteria.
   * @param exercises for which to return SampleUnitGroups.
   * @param pageable Spring JPA pageable object used to return required number
   *          of results.
   * @return returns all SampleUnitGroups for state within requested exercises.
   */
  List<ExerciseSampleUnitGroup> findByStateFKAndCollectionExerciseInOrderByCreatedDateTimeAsc(
      SampleUnitGroupDTO.SampleUnitGroupState state, List<CollectionExercise> exercises, Pageable pageable);

  /**
   * Query repository for SampleUnitGroups by state and belonging to a
   * collection exercise. Order by ModifiedDateTime ascending.
   *
   * @param state filter criteria.
   * @param exercise for which to return SampleUnitGroups.
   * @param pageable Spring JPA pageable object used to return required number
   *          of results.
   * @return returns all SampleUnitGroups for state within requested exercise.
   */
  List<ExerciseSampleUnitGroup> findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(
      SampleUnitGroupDTO.SampleUnitGroupState state, CollectionExercise exercise, Pageable pageable);
}
