package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;

import java.util.List;
import java.util.UUID;

/**
 * Spring JPA Repository for CaseTypeDefault
 *
 */
public interface CaseTypeDefaultRepository extends JpaRepository<CaseTypeDefault, UUID> {

  /**
   * Query repository for case type defaults associated to survey uuid.
   *
   * @param surveyUuid survey uuid to which the Case Type Default is associated.
   * @return list of associated casetypedefaults.
   */
  List<CaseTypeDefault> findBySurveyId(UUID surveyUuid);

  /**
   * Query repository for case type defaults associated to survey uuid and sample unit type
   *
   * @param surveyUuid survey uuid to which the Case Type Default is associated.
   * @param sampleUnitType string representing sample unit type e.g. B, H, HI
   * @return list of associated casetypedefaults.
   */
  CaseTypeDefault findTopBySurveyIdAndSampleUnitTypeFK(UUID surveyUuid, String sampleUnitType);

}
