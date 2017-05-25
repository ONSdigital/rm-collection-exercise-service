package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;

import java.util.List;
import java.util.UUID;

/**
 * Spring JPA Repository for CaseTypeOverride
 *
 */
public interface CaseTypeDefaultRepository extends JpaRepository<CaseTypeDefault, UUID> {

  /**
   * Query repository for case type defaults associated to survey pk.
   *
   * @param surveyPK survey pk to which the Case Type Default is associated.
   * @return list of associated casetypes.
   */
  List<CaseTypeDefault> findBySurveyFK(Integer surveyPK);


}
