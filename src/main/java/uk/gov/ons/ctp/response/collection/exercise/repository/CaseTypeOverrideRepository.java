package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;

import java.util.List;
import java.util.UUID;

/**
 * Spring JPA Repository for CaseTypeOverride
 *
 */
public interface CaseTypeOverrideRepository extends JpaRepository<CaseTypeOverride, UUID> {

  /**
   * Query repository for case types associated to collection exercise pk.
   *
   * @param collectionExercisePK collection exercise pk to which the Case Type is associated.
   * @return list of associated casetypes.
   */
  List<CaseTypeOverride> findByExerciseFK(Integer collectionExercisePK);


}
