package uk.gov.ons.ctp.response.collection.exercise.service.change;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseMessageType.MessageType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

/**
 * EventChangeHandler to set the scheduledStartDate for a collection exercise when an mps event is
 * created or updated
 */
@Component
public final class ScheduledStartDateHandler implements EventChangeHandler {
  private static final Logger log = LoggerFactory.getLogger(ScheduledStartDateHandler.class);

  @Autowired private CollectionExerciseService collectionExerciseService;

  @Override
  public void handleEventLifecycle(MessageType change, Event event) {
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
            log.debug(
                "Setting scheduledStartDate",
                kv("collection_exercise_id", collex.getId()),
                kv("event_time", event.getTimestamp()));
            collex.setScheduledStartDateTime(event.getTimestamp());
            collex.setScheduledExecutionDateTime(event.getTimestamp());
            collex.setPeriodStartDateTime(event.getTimestamp());
            break;
          case exercise_end:
            log.debug(
                "Setting scheduledEndDate",
                kv("collection_exercise_id", collex.getId()),
                kv("event_time", event.getTimestamp()));
            collex.setScheduledEndDateTime(event.getTimestamp());
            collex.setPeriodEndDateTime(event.getTimestamp());
            break;
          case return_by:
            log.debug(
                "Setting scheduledReturnDate",
                kv("collection_exercise_id", collex.getId()),
                kv("event_time", event.getTimestamp()));
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
