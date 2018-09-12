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

  List<CollectionExercise> findBySurveyIdAndState(UUID surveyUuid, CollectionExerciseState state);

  /**
   * Query repository for list of collection exercises associated with a certain state.
   *
   * @param state for which to return Collection Exercises.
   * @return collection exercises in the requested state.
   */
  List<CollectionExercise> findByState(CollectionExerciseDTO.CollectionExerciseState state);
}
