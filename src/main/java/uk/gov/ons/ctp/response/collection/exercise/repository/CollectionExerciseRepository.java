package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;

/** Spring JPA Repository for Collection Exercise */
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
   * Query repository for collection exercises in a list of survey ids
   *
   * @param surveyIds the surveys to select by
   * @return List of collection exercises, ordered by survey id
   */
  List<CollectionExercise> findBySurveyIdInOrderBySurveyId(List<UUID> surveyIds);

  List<CollectionExercise> findBySurveyIdAndState(UUID surveyUuid, CollectionExerciseState state);

  /**
   * Query repository for collection exercises in a list of survey ids
   *
   * @param surveyIds the surveys to select by
   * @param state the state of the survey to limit by
   * @return List of collection exercises, ordered by survey id
   */
  List<CollectionExercise> findBySurveyIdInAndStateOrderBySurveyId(
      List<UUID> surveyIds, CollectionExerciseState state);

  /**
   * Query repository for list of collection exercises associated with a certain state.
   *
   * @param state for which to return Collection Exercises.
   * @return collection exercises in the requested state.
   */
  List<CollectionExercise> findByState(CollectionExerciseDTO.CollectionExerciseState state);
}
