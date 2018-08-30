package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
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

  public boolean validate(
      List<Event> existingEvents,
      Event submittedEvent,
      CollectionExerciseState collectionExerciseState) {

    if (!isReminder(submittedEvent)) {
      return true;
    }

    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));

    final Event goLive = existingEventsMap.get(Tag.go_live.toString());
    final Event exerciseEnd = existingEventsMap.get(Tag.exercise_end.toString());

    if (!eventDuringExercise(goLive, submittedEvent, exerciseEnd)) {
      return false;
    }

    if (isExsistingReminderInPast(submittedEvent, existingEventsMap)) {
      return false;
    }

    final List<Event> reminders =
        Tag.ORDERED_REMINDERS
            .stream()
            .map(tag -> getEventByTag(tag, submittedEvent, existingEventsMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return eventDateOrderChecker.isEventDatesInOrder(reminders);
  }

  private boolean isExsistingReminderInPast(
      Event submittedEvent, Map<String, Event> existingEventsMap) {
    final Event existingReminder = existingEventsMap.get(submittedEvent.getTag());
    if (existingReminder == null) {
      return false;
    }
    final Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
    return existingReminder.getTimestamp().before(currentTimestamp);
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
