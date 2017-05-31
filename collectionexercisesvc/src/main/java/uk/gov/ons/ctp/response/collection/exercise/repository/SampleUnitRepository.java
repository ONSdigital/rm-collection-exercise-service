package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;

/**
 * JPA repository for SampleUnit entities
 */
@Repository
public interface SampleUnitRepository extends JpaRepository<ExerciseSampleUnit, Integer> {

  /**
   * Check repository for Sample Unit existence. May exist in different
   * collection exercise.
   *
   * @param id collection exercise id of which the sample unit is a part.
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
   * Count the number of SampleUnits for the collection exercise.
   *
   * @param id collection exercise for which to count sample units.
   * @return int sample unit total for given exercisePK.
   */
  @Query(value = "select count(*) from "
      + "collectionexercise.sampleunit su, "
      + "collectionexercise.sampleunitgroup sg "
      + "where sg.exercisefk = :p_exercisefk and "
      + "su.sampleunitgroupfk = sg.sampleunitgrouppk;", nativeQuery = true)
  int totalByExercisePK(@Param("p_exercisefk") Integer id);

}
