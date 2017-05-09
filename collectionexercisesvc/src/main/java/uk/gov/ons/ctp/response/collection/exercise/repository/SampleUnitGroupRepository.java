package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;

/**
 * Spring JPA Repository for ExerciseSampleUnitGroup
 *
 */
@Repository
public interface SampleUnitGroupRepository extends JpaRepository<ExerciseSampleUnitGroup, BigInteger> {


}
