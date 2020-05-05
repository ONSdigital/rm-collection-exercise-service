package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;

public interface ActionRuleCreator {
  void execute(Event collectionExerciseEvent) throws CTPException;
}
