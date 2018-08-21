package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;

public interface ActionRuleUpdater {
  void execute(Event event) throws CTPException;
}
