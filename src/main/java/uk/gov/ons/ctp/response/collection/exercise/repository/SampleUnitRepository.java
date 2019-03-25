package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/** JPA repository for SampleUnit entities */
@Repository
public interface SampleUnitRepository extends JpaRepository<ExerciseSampleUnit, Integer> {

  /**
   * Check repository for SampleUnit uniqueness.
   *
   * @param sampleUnitRef to check for existence of sample unit.
   * @param sampleUnitType to check for existence of sample unit.
   * @param collectionExercise CollectionExercise of which the sample unit to check is a part.
   * @return boolean whether exists
   */
  boolean existsBySampleUnitRefAndSampleUnitTypeAndSampleUnitGroupCollectionExercise(
      String sampleUnitRef,
      SampleUnitDTO.SampleUnitType sampleUnitType,
      CollectionExercise collectionExercise);

  /**
   * Count the number of SampleUnits for the CollectionExercise.
   *
   * @param collectionExercise is CollectionExercise for which to count SampleUnits.
   * @return int of SampleUnit total for given collectionExercise.
   */
  int countBySampleUnitGroupCollectionExercise(CollectionExercise collectionExercise);

  /**
   * Query repository for SampleUnits belonging to a SampleUnitGroup.
   *
   * @param sampleUnitGroup to which the SampleUnits belong.
   * @return List<ExerciseSampleUnit> SampleUnits belonging to SampleUnitGroup
   */
  List<ExerciseSampleUnit> findBySampleUnitGroup(ExerciseSampleUnitGroup sampleUnitGroup);

  /**
   * Find sample units with group in a particular state for a given collection exercise
   *
   * @param collectionExercise a collection exercise
   * @param sampleUnitGroupState a group state
   * @return a list of sample units with group in a particular state for a collection exercise
   */
  List<ExerciseSampleUnit> findBySampleUnitGroupCollectionExerciseAndSampleUnitGroupStateFK(
      CollectionExercise collectionExercise, SampleUnitGroupState sampleUnitGroupState);

  /**
   * Find sample units with collection exercise and group in a particular state
   *
   * @param collexState a collection exercise state
   * @param sampleUnitGroupState a group state
   * @return a stream of sample units with collection exercise and group in a particular state
   */
  Stream<ExerciseSampleUnit> findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
      CollectionExerciseDTO.CollectionExerciseState collexState,
      SampleUnitGroupState sampleUnitGroupState);
}
