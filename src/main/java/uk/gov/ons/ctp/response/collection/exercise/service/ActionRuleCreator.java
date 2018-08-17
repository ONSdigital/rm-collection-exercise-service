package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;

public interface ActionRuleCreator {
  void execute(Event collectionExerciseEvent) throws CTPException;
}
