package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;

/**
 * Spring JPA Repository for Collection Exercise
 *
 */
public interface CollectionExerciseRepository extends JpaRepository<CollectionExercise, BigInteger> {

}
