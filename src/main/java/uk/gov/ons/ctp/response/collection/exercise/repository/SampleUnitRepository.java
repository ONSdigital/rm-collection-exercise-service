package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;

/**
 * JPA repository for SampleUnit entities
 */
@Repository
public interface SampleUnitRepository extends JpaRepository<ExerciseSampleUnit, Integer> {

  /**
   * Check repository for SampleUnit existence. May exist in different
   * CollectionExercise.
   *
   * @param id CollectionExercise id of which the sample unit to check is a part.
   * @param sampleUnitRef to check for existence of sample unit.
   * @param sampleUnitTypeFK to check for existence of sample unit.
   * @return boolean whether exists
   */
  @Query(value = "select exists (select 1 from "
      + "collectionexercise.sampleunit su, "
      + "collectionexercise.sampleunitgroup sg "
      + "where su.sampleunitgroupfk = sg.sampleunitgrouppk and "
      + "sg.exercisefk = :p_exercisefk and "
      + "su.sampleunitref = :p_sampleunitref and "
      + "su.sampleunittypefk = :p_sampleunittypefk);", nativeQuery = true)
  boolean tupleExists(@Param("p_exercisefk") Integer id, @Param("p_sampleunitref") String sampleUnitRef,
      @Param("p_sampleunittypefk") String sampleUnitTypeFK);

  /**
   * Count the number of SampleUnits for the CollectionExercise.
   *
   * @param id of CollectionExercise for which to count SampleUnits.
   * @return int of SampleUnit total for given exercisePK.
   */
  @Query(value = "select count(*) from "
      + "collectionexercise.sampleunit su, "
      + "collectionexercise.sampleunitgroup sg "
      + "where sg.exercisefk = :p_exercisefk and "
      + "su.sampleunitgroupfk = sg.sampleunitgrouppk;", nativeQuery = true)
  int totalByExercisePK(@Param("p_exercisefk") Integer id);

  /**
   * Query repository for SampleUnits belonging to a SampleUnitGroup.
   *
   *  @param sampleUnitGroup to which the SampleUnits belong.
   *  @return List<ExerciseSampleUnit> SampleUnits belonging to SampleUnitGroup
   */
  List<ExerciseSampleUnit> findBySampleUnitGroup(ExerciseSampleUnitGroup sampleUnitGroup);

}
