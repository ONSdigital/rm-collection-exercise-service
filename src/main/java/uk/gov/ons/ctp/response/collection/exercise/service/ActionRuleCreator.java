package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.response.survey.representation.SurveyDTO;

public interface ActionRuleCreator {
  void execute(
      Event collectionExerciseEvent,
      CaseTypeOverride businessCaseTypeOverride,
      CaseTypeOverride businessIndividualCaseTypeOverride,
      SurveyDTO survey);
}
