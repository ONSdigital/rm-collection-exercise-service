package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;

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
   * Query repository for list of collection exercises associated to survey fk.
   *
   * @param surveyfk survey fk to which collection exercises are associated.
   * @return List of collection exercises.
   */
  List<CollectionExercise> findBySurveySurveyPK(Integer surveyfk);

}
