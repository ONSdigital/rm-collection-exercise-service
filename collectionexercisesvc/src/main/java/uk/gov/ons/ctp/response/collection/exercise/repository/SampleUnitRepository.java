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
      + "where su.sampleunitgroupfk = sg.sampleunitgrouppk and sg.exercisefk = ce.exercisepk and "
      + "ce.exercisepk = :p_exercisepk and su.sampleunitpk = :p_sampleunitpk);", nativeQuery = true)
  boolean tupleExists(@Param("p_exercisepk") Integer id, @Param("p_sampleunitpk") Integer sampleUnitId);

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
