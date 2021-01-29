package uk.gov.ons.ctp.response.collection.exercise.service.change;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

/**
 * EventChangeHandler to schedule and unschedule collection exercise events when CRUD operations
 * occur
 */
@Component
public class ScheduleEvent implements EventChangeHandler {
  @Autowired private EventService eventService;
  @Autowired private AppConfig appConfig;

  /**
   * Schedules and unschedules events based on collection exercise event CRUD events
   *
   * @param change the type of change that occurred
   * @param event the event to which the change occurred
   * @throws CTPException
   */
  @Override
  public void handleEventLifecycle(
      final CollectionExerciseEventPublisher.MessageType change, final Event event)
      throws CTPException {
    if (!appConfig.getActionSvc().isDeprecated()) {
      switch (change) {
        case EventCreated:
        case EventUpdated:
          this.eventService.scheduleEvent(event);
          break;
        case EventDeleted:
          this.eventService.unscheduleEvent(event);
          break;
        default:
          // We don't care about Elapsed
      }
    }
  }
}
