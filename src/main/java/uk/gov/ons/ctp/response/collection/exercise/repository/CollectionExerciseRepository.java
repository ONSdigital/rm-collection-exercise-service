package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;

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

  /**
   * Query repository for list of collection exercises associated with a certain party ID.
   *
   * @param partyid for which to return Collection Exercises.
   * @return collection exercises for party ID.
   */
  @Query(value = "select ce.id, ce.exercisepk, ce.name, ce.scheduledstartdatetime, ce.scheduledexecutiondatetime, "
          + "ce.scheduledreturndatetime, ce.scheduledenddatetime, ce.periodstartdatetime, ce.periodenddatetime, "
          + "ce.actualexecutiondatetime, ce.actualpublishdatetime, ce.executedby, ce.statefk, ce.samplesize, "
          + "ce.exerciseref, ce.user_description, ce.created, ce.updated, ce.deleted, ce.survey_uuid "
          + "from collectionexercise.collectionexercise ce "
          + "inner join collectionexercise.sampleunitgroup sg "
          + "on ce.exercisepk = sg.exercisefk "
          + "inner join collectionexercise.sampleunit su "
          + "on sg.sampleunitgrouppk = su.sampleunitgroupfk "
          + "where su.partyid = :p_partyid ;", nativeQuery = true)
  List<CollectionExercise> findByPartyId(@Param("p_partyid") UUID partyid);
}
