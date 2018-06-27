package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;

/**
 * An interface to implement if you want to perform some action when a collection exercise event is
 * updated, created or deleted. WARNING: All implementation of this interface are autowired into the
 * EventServiceImpl and get used for real
 */
@FunctionalInterface
public interface EventChangeHandler {
  /**
   * The method that is called when a collection exercise event is created, updated or deleted
   *
   * @param change the type of change that occurred
   * @param event the event to which the change occurred
   * @throws CTPException thrown if an error occurs
   */
  void handleEventLifecycle(CollectionExerciseEventPublisher.MessageType change, Event event)
      throws CTPException;
}
