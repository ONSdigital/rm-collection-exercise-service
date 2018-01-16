package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Spring JPA Repository for Collection Exercise
 *
 */
public interface CollectionExerciseRepository extends JpaRepository<CollectionExercise, Integer> {

  /**
   * Query repository for collection exercise by id.
   *
   * @param id collection exercise id to find.
   * @return collection exercise object.
   */
  CollectionExercise findOneById(UUID id);

  /**
   * Query repository for collection exercise with given period and survey
   *
   * @param exerciseRef collection exercise period
   * @param surveyUuid surveyfk to which collection exercises are associated.
   * @return List of collection exercises.
   */
  List<CollectionExercise> findByExerciseRefAndSurveyId(String exerciseRef, UUID surveyUuid);

  List<CollectionExercise> findBySurveyId(UUID surveyUuid);

  /**
   * Query repository for list of collection exercises associated with a certain
   * state.
   *
   * @param state for which to return Collection Exercises.
   * @return collection exercises in the requested state.
   */
  List<CollectionExercise> findByState(CollectionExerciseDTO.CollectionExerciseState state);

  /**
   * Query repository for active actionPlanId (default or override) for SampleUnitType for
   * Survey of if overridden for SampleUnitType for CollectionExercise.
   *
   * @param exercisefk of CollectionExercise.
   * @param sampleunittypefk of SampleUnitType.
   * @param surveyuuid uuid of Survey.
   * @return ActiveActionPlanId
   */
  @Query(value = "SELECT CASE WHEN r.actionplanid IS NULL THEN CAST(df.actionplanid AS VARCHAR) ELSE "
          + "CAST(r.actionplanid AS VARCHAR) END as to_use FROM (SELECT o.* FROM collectionexercise.casetypeoverride o "
          + "WHERE o.exercisefk = :p_exercisefk AND o.sampleunittypefk = :p_sampleunittypefk) r "
          + "RIGHT OUTER JOIN (SELECT d.* FROM collectionexercise.casetypedefault d WHERE d.survey_uuid = :p_surveyuuid "
          + "AND d.sampleunittypefk = :p_sampleunittypefk) df ON r.sampleunittypeFK = df.sampleunittypeFK;",
          nativeQuery = true)
  String getActiveActionPlanId(@Param("p_exercisefk") Integer exercisefk,
      @Param("p_sampleunittypefk") String sampleunittypefk, @Param("p_surveyuuid") UUID surveyuuid);
}
