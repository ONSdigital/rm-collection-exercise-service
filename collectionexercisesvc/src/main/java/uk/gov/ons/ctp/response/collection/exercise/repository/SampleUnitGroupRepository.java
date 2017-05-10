package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;

/**
 * Spring JPA Repository for ExerciseSampleUnitGroup
 *
 */
@Repository
public interface SampleUnitGroupRepository extends JpaRepository<ExerciseSampleUnitGroup, Integer> {

}
