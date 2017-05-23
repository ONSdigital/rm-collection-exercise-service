package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;

import java.util.List;
import java.util.UUID;

/**
 * Created by stevee on 23/05/2017.
 */
public interface CaseTypeOverrideRepository extends JpaRepository<CaseType, UUID> {

  List<CaseType> findByExerciseFK(Integer collectionExercisePK);


}
