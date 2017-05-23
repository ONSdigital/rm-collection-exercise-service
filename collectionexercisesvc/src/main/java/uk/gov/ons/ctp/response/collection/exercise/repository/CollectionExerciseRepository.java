package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;

import java.util.List;
import java.util.UUID;

/**
 * Spring JPA Repository for Collection Exercise
 *
 */
public interface CollectionExerciseRepository extends JpaRepository<CollectionExercise, UUID> {

    CollectionExercise findOneById(UUID id);

    List<CollectionExercise> findBySurveySurveyPK(Integer surveyfk);

}
