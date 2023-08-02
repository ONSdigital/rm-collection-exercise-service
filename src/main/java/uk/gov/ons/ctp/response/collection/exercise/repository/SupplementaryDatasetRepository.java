package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;

/** JPA Data Repository. */
public interface SupplementaryDatasetRepository
    extends JpaRepository<SupplementaryDatasetEntity, Integer> {

  boolean existsByExerciseFK(int exerciseFk);

  void deleteByExerciseFK(int exerciseFk);
}
