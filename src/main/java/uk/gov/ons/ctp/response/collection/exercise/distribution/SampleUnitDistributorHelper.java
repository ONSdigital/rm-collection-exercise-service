package uk.gov.ons.ctp.response.collection.exercise.distribution;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;

@Component
public class SampleUnitDistributorHelper {
  @Autowired private CaseTypeDefaultRepository caseTypeDefaultRepo;

  @Autowired private CaseTypeOverrideRepository caseTypeOverrideRepo;

  /**
   * Get the action plan for the collection exercise - either an override or a default. This method
   * is going to be made obselete by action plan selectors.
   *
   * @param exercisefk the collection exercise
   * @param sampleunittypefk the type of sample unit
   * @param surveyuuid the survey ID
   */
  public String getActiveActionPlanId(
      final Integer exercisefk, final String sampleunittypefk, final UUID surveyuuid) {
    String actionPlanId = null;

    CaseTypeOverride caseTypeOverride =
        caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(exercisefk, sampleunittypefk);

    if (caseTypeOverride != null && caseTypeOverride.getActionPlanId() != null) {
      actionPlanId = caseTypeOverride.getActionPlanId().toString();
    } else {
      CaseTypeDefault caseTypeDefault =
          caseTypeDefaultRepo.findTopBySurveyIdAndSampleUnitTypeFK(surveyuuid, sampleunittypefk);

      if (caseTypeDefault != null && caseTypeDefault.getActionPlanId() != null) {
        actionPlanId = caseTypeDefault.getActionPlanId().toString();
      }
    }

    return actionPlanId;
  }
}
