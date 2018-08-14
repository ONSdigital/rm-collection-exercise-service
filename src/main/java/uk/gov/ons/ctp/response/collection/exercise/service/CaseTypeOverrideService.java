package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;

public interface CaseTypeOverrideService {
  CaseTypeOverride getCaseTypeOverride(CollectionExercise collectionExercise, String sampleUnitType)
      throws CTPException;
}
