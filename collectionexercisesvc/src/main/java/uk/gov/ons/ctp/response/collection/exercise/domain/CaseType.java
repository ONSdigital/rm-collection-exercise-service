package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.UUID;

/**
 * CaseType Interface for CaseTypeDefault and CaseTypeOverride
 */


public interface CaseType {

  String getSampleUnitTypeFK();

  UUID getActionPlanId();

}
