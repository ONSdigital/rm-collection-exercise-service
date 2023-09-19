package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;

/** JPA Data Repository. */
public interface SupplementaryDatasetRepository
    extends JpaRepository<SupplementaryDatasetEntity, Integer> {

  SupplementaryDatasetEntity findByExerciseFK(int exerciseFk);

  boolean existsByExerciseFK(int exerciseFk);

  @Modifying
  @Query("DELETE from SupplementaryDatasetEntity where exerciseFK = :sampleSummaryPK")
  void deleteByExerciseFK(@Param("sampleSummaryPK") Integer exerciseFk);
}
