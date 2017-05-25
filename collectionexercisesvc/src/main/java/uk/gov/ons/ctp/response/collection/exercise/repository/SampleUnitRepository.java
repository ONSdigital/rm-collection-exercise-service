package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;

import java.util.UUID;

/**
 * JPA repository for SampleUnit entities
 */
@Repository
public interface SampleUnitRepository extends JpaRepository<ExerciseSampleUnit, Integer> {

  /**
   * Check repository for sampleUnitId existence. May exist in different
   * collection exercise.
   *
   * @param id collection exercise id of which the sample unit is a part.
   * @param sampleUnitId to check for existence.
   * @return boolean whether exists
   */
  @Query(value = "select exists (select 1 from "
      + "collectionexercise.sampleunit su, "
      + "collectionexercise.sampleunitgroup sg, "
      + "collectionexercise.collectionexercise ce "
      + "where su.sampleunitgroupid = sg.sampleunitgroupid and sg.exerciseid = ce.exerciseid and "
      + "ce.exerciseid = :p_exerciseid and su.sampleunitgroupid = :p_sampleunitid);", nativeQuery = true)
  boolean tupleExists(@Param("p_exerciseid") UUID id, @Param("p_sampleunitid") Integer sampleUnitId);

  /**
   * Count the number of SampleUnits for the collection exercise.
   *
   * @param id collection exercise for which to count sample units.
   * @return int sample unit total for given exercisePK.
   */
  @Query(value = "select count(*) from "
      + "collectionexercise.sampleunit su, "
      + "collectionexercise.sampleunitgroup sg "
      + "where sg.exerciseid = :p_exerciseid and "
      + "su.sampleunitgroupid = sg.sampleunitgroupid;", nativeQuery = true)
  int totalByExerciseId(@Param("p_exerciseid") UUID id);

}
