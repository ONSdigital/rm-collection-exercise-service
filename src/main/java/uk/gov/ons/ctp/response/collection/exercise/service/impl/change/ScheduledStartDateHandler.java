package uk.gov.ons.ctp.response.collection.exercise.service.impl.change;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.impl.CollectionExerciseService;

/**
 * EventChangeHandler to set the scheduledStartDate for a collection exercise when an mps event is
 * created or updated
 */
@Component
public final class ScheduledStartDateHandler implements EventChangeHandler {
  private static final Logger log = LoggerFactory.getLogger(ScheduledStartDateHandler.class);

  @Autowired private CollectionExerciseService collectionExerciseService;

  @Override
  public void handleEventLifecycle(
      CollectionExerciseEventPublisher.MessageType change, Event event) {
    switch (change) {
      case EventCreated:
      case EventUpdated:
        updateCollectionExerciseFromEvent(event);
        break;
      default:
        break;
    }
  }

  /**
   * Updates the scheduledStartDate for the collection exercise from the event timestamp if the
   * event is an mps event
   *
   * @param event the incoming event
   */
  private void updateCollectionExerciseFromEvent(final Event event) {
    try {
      EventService.Tag tag = EventService.Tag.valueOf(event.getTag());
      CollectionExercise collex = event.getCollectionExercise();

      if (collex != null) {
        switch (tag) {
          case mps:
            log.with("collection_exercise_id", collex.getId())
                .with("event_time", event.getTimestamp())
                .debug("Setting scheduledStartDate");
            collex.setScheduledStartDateTime(event.getTimestamp());
            collex.setScheduledExecutionDateTime(event.getTimestamp());
            collex.setPeriodStartDateTime(event.getTimestamp());
            break;
          case exercise_end:
            log.with("collection_exercise_id", collex.getId())
                .with("event_time", event.getTimestamp())
                .debug("Setting scheduledEndDate");
            collex.setScheduledEndDateTime(event.getTimestamp());
            collex.setPeriodEndDateTime(event.getTimestamp());
            break;
          case return_by:
            log.with("collection_exercise_id", collex.getId())
                .with("event_time", event.getTimestamp())
                .debug("Setting scheduledReturnDate");
            collex.setScheduledReturnDateTime(event.getTimestamp());
            break;
          default:
            break;
        }
        this.collectionExerciseService.updateCollectionExercise(collex);
      }
    } catch (IllegalArgumentException e) {
      // Thrown if tag isn't one of the mandatory types - if it happens we don't care about the
      // event
    }
  }
}
