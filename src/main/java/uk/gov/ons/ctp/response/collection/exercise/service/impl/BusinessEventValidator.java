package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

public class BusinessEventValidator implements EventValidator {

  /** Validates the dates on event creation */
  public boolean validate(
      final List<Event> existingEvents,
      final Event submittedEvent,
      final CollectionExerciseState collectionExerciseState) {

    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));

    if (isMandatory(submittedEvent)) {
      return validateMandatoryEvents(existingEventsMap, submittedEvent, collectionExerciseState);
    }

    if (isReminder(submittedEvent)) {
      return validateReminderEvents(existingEventsMap, submittedEvent);
    }

    if (isReferencePeriod(submittedEvent)) {
      return validateReferencePeriodDates(existingEventsMap, submittedEvent);
    }

    return true;
  }

  private boolean validateReferencePeriodDates(
      Map<String, Event> existingEvents, Event submittedEvent) {
    final String tagName = submittedEvent.getTag();
    final String refPeriodStart = Tag.ref_period_start.toString();
    final String refPeriodEnd = Tag.ref_period_end.toString();

    final Event referencePeriodStart =
        (Tag.ref_period_start.hasName(tagName))
            ? submittedEvent
            : existingEvents.get(refPeriodStart);
    final Event referencePeriodEnd =
        (Tag.ref_period_end.hasName(tagName)) ? submittedEvent : existingEvents.get(refPeriodEnd);

    return referencePeriodInCorrectOrder(referencePeriodStart, referencePeriodEnd);
  }

  private boolean validateReminderEvents(Map<String, Event> existingEvents, Event submittedEvent) {
    final Event goLive = existingEvents.get(Tag.go_live.toString());
    final Event exerciseEnd = existingEvents.get(Tag.exercise_end.toString());

    if (!eventDuringExercise(goLive, submittedEvent, exerciseEnd)) {
      return false;
    }

    final Event currentReminder = existingEvents.get(submittedEvent.getTag());

    if (currentReminder != null && isEventInPast(currentReminder)) {
      return false;
    }

    final List<Event> reminders =
        Tag.ORDERED_REMINDERS
            .stream()
            .map(tag -> getEventByTag(tag, submittedEvent, existingEvents))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return datesInCorrectOrder(reminders);
  }

  /** Validates the mandatory dates. Event dates can be added in any order. */
  private boolean validateMandatoryEvents(
      final Map<String, Event> existingEvents,
      final Event submittedEvent,
      final CollectionExerciseState collectionExerciseState) {

    final List<CollectionExerciseState> lockedStates =
        Arrays.asList(
            CollectionExerciseState.EXECUTION_STARTED,
            CollectionExerciseState.VALIDATED,
            CollectionExerciseState.EXECUTED,
            CollectionExerciseState.READY_FOR_LIVE,
            CollectionExerciseState.LIVE);

    if (lockedStates.contains(collectionExerciseState)) {
      return false;
    }

    final List<Event> events =
        Tag.ORDERED_MANDATORY_EVENTS
            .stream()
            .map(tag -> getEventByTag(tag, submittedEvent, existingEvents))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return datesInCorrectOrder(events);
  }

  private Event getEventByTag(Tag tag, Event submittedEvent, Map<String, Event> existingEvents) {
    return submittedEvent.getTag().equals(tag.toString())
        ? submittedEvent
        : existingEvents.get(tag.toString());
  }

  /** Validates list of events in chronological order. */
  private boolean datesInCorrectOrder(List<Event> events) {
    Event[] eventsArray = events.stream().toArray(Event[]::new);
    boolean result = true;
    for (int i = 0; i < eventsArray.length - 1; i++) {
      Timestamp t1 = eventsArray[i].getTimestamp();
      Timestamp t2 = eventsArray[i + 1].getTimestamp();
      if (t1.after(t2)) {
        result = false;
      }
    }
    return result;
  }

  private boolean referencePeriodInCorrectOrder(
      final Event referencePeriodStart, final Event referencePeriodEnd) {
    boolean isValid = true;
    if (referencePeriodStart != null && referencePeriodEnd != null) {
      isValid = referencePeriodStart.getTimestamp().before(referencePeriodEnd.getTimestamp());
    }
    return isValid;
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

  private boolean isEventInPast(final Event event) {
    Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
    return event.getTimestamp().before(currentTimestamp);
  }

  private boolean isMandatory(final Event updatedEvent) {
    return Tag.valueOf(updatedEvent.getTag()).isMandatory();
  }

  private boolean isReminder(final Event updatedEvent) {
    return Tag.valueOf(updatedEvent.getTag()).isReminder();
  }

  private boolean isReferencePeriod(final Event event) {
    final List<String> referencePeriods =
        Arrays.asList(Tag.ref_period_start.toString(), Tag.ref_period_end.toString());
    return referencePeriods.contains(event.getTag());
  }
}
