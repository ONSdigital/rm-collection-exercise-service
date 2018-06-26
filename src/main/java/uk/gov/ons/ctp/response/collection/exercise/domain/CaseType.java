package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.UUID;

/** CaseType Interface for CaseTypeDefault and CaseTypeOverride */
public interface CaseType {

  /**
   * Gets SampleUnitType Foreign Key
   *
   * @return sampleUnitTypeFK
   */
  String getSampleUnitTypeFK();

  /**
   * Gets ActionPlan Id
   *
   * @return actionPlanId
   */
  UUID getActionPlanId();
}
