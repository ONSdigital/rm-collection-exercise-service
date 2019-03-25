package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

@Component
public class ReminderEventValidator implements EventValidator {

  private final EventDateOrderChecker eventDateOrderChecker;

  public ReminderEventValidator(EventDateOrderChecker eventDateOrderChecker) {
    this.eventDateOrderChecker = eventDateOrderChecker;
  }

  public void validate(
      List<Event> existingEvents,
      Event submittedEvent,
      CollectionExerciseState collectionExerciseState)
      throws CTPException {

    if (!isReminder(submittedEvent)) {
      return;
    }

    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));

    final Event goLive = existingEventsMap.get(Tag.go_live.toString());
    final Event exerciseEnd = existingEventsMap.get(Tag.exercise_end.toString());

    if (!eventDuringExercise(goLive, submittedEvent, exerciseEnd)) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          "Reminder must take place during collection exercise period");
    }

    if (isCollectionExerciseLockedState(collectionExerciseState)
        && isExistingReminderInPast(submittedEvent, existingEventsMap)) {
      throw new CTPException(CTPException.Fault.BAD_REQUEST, "Reminder cannot be set in the past");
    }

    final List<Event> reminders =
        Tag.ORDERED_REMINDERS
            .stream()
            .map(tag -> getEventByTag(tag, submittedEvent, existingEventsMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (!eventDateOrderChecker.isEventDatesInOrder(reminders)) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Collection exercise events must be set sequentially");
    }
  }

  private boolean isExistingReminderInPast(
      Event submittedEvent, Map<String, Event> existingEventsMap) {
    final Event existingReminder = existingEventsMap.get(submittedEvent.getTag());
    if (existingReminder == null) {
      return false;
    }
    final Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
    return existingReminder.getTimestamp().before(currentTimestamp);
  }

  private boolean isCollectionExerciseLockedState(CollectionExerciseState collectionExerciseState) {
    final List<CollectionExerciseState> lockedStates =
        Arrays.asList(
            CollectionExerciseState.EXECUTION_STARTED,
            CollectionExerciseState.VALIDATED,
            CollectionExerciseState.EXECUTED,
            CollectionExerciseState.READY_FOR_LIVE,
            CollectionExerciseState.LIVE);

    return lockedStates.contains(collectionExerciseState);
  }

  private boolean eventDuringExercise(Event goLive, Event event, Event exerciseEnd) {
    boolean isAfterGoLive = true;
    boolean isBeforeExerciseEnd = true;

    if (event == null) {
      return true;
    }

    if (goLive != null) {
      isAfterGoLive = goLive.getTimestamp().before(event.getTimestamp());
    }

    if (exerciseEnd != null) {
      isBeforeExerciseEnd = event.getTimestamp().before(exerciseEnd.getTimestamp());
    }

    return isAfterGoLive && isBeforeExerciseEnd;
  }

  private Event getEventByTag(Tag tag, Event submittedEvent, Map<String, Event> existingEvents) {
    return submittedEvent.getTag().equals(tag.toString())
        ? submittedEvent
        : existingEvents.get(tag.toString());
  }

  private boolean isReminder(final Event event) {
    return Tag.valueOf(event.getTag()).isReminder();
  }
}
