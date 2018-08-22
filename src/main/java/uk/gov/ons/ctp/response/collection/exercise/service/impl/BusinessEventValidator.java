package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static java.time.temporal.ChronoUnit.DAYS;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

public class BusinessEventValidator implements EventValidator {

  /**
   * q Validates the events timestamps are in the correct order and the updated event can be
   * updated, i.e is not a mandatory or reminder event.
   *
   * @param existingEvents
   * @param updatedEvent
   * @return
   */
  public boolean validate(
      final List<Event> existingEvents,
      final Event updatedEvent,
      final CollectionExerciseState collectionExerciseState) {

    // Can only update reminders of the non mandatory events when READY_FOR_LIVE
    if ((collectionExerciseState.equals(CollectionExerciseState.READY_FOR_LIVE)
            || collectionExerciseState.equals(CollectionExerciseState.LIVE))
        && (isMandatory(updatedEvent) || !isReminder(updatedEvent))) {
      return false;
    }

    Optional<Event> existingEvent =
        existingEvents
            .stream()
            .findFirst()
            .filter(event -> event.getTag().equals(updatedEvent.getTag()));

    // Events can't be updated if already past or updated date is in the past
    if ((existingEvent.isPresent() && isEventInPast(existingEvent.get()))
        || isEventInPast(updatedEvent)) {
      return false;
    }

    Map<String, Event> eventMap = generateEventsMap(existingEvents, updatedEvent);

    return validateMandatoryEvents(eventMap) && validateNonMandatoryEvents(eventMap);
  }

  /** Validates the dates on event creation */
  public boolean validateOnCreate(
      final List<Event> existingEvents,
      final Event newEvent,
      final CollectionExerciseState collectionExerciseState) {
    Map<String, Event> events =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));
    if (collectionExerciseState.equals(CollectionExerciseState.CREATED)) {
      return validateMandatoryEventsOnCreate(events, newEvent);
    }
    return false;
  }

  /** Validates the mandatory dates on event creation. Event dates can be added in any order. */
  private boolean validateMandatoryEventsOnCreate(
      final Map<String, Event> eventMap, Event newEvent) {
    List<Event> events =
        Arrays.asList(Tag.mps, Tag.go_live, Tag.return_by, Tag.exercise_end)
            .stream()
            .map(tag -> getEventByTag(tag, newEvent, eventMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return datesInValidOrder(events);
  }

  private Event getEventByTag(Tag tag, Event newEvent, Map<String, Event> eventMap) {
    return newEvent.getTag().equals(tag.toString()) ? newEvent : eventMap.get(tag.toString());
  }

  /** Validates list of events in chronological order. */
  private boolean datesInValidOrder(List<Event> events) {
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

  /** Validates dates are 24 hours apart. */
  @SuppressWarnings("unused")
  private boolean dateLessThan24HoursLater(Timestamp t1, Timestamp t2) {
    long days = DAYS.between(t1.toInstant(), t2.toInstant());
    return days < 1;
  }

  /**
   * Validates the dates which aren't mandatory for a collection exercise to be executed. If the
   * events exist it checks that reference period start before referencePeriodEnd and all reminders
   * are during the collection exercise.
   *
   * @param eventMap
   * @return
   */
  private boolean validateNonMandatoryEvents(final Map<String, Event> eventMap) {
    Event referencePeriodStart = eventMap.get(Tag.ref_period_start.toString());
    Event referencePeriodEnd = eventMap.get(Tag.ref_period_end.toString());

    return referencePeriodInCorrectOrder(referencePeriodStart, referencePeriodEnd)
        && remindersDuringExercise(eventMap);
  }

  private boolean referencePeriodInCorrectOrder(
      final Event referencePeriodStart, final Event referencePeriodEnd) {
    boolean isValid = true;
    if (referencePeriodStart != null && referencePeriodEnd != null) {
      isValid = referencePeriodStart.getTimestamp().before(referencePeriodEnd.getTimestamp());
    }
    return isValid;
  }

  private boolean remindersDuringExercise(final Map<String, Event> eventMap) {
    Event goLive = eventMap.get(Tag.go_live.toString());
    Event exerciseEnd = eventMap.get(Tag.exercise_end.toString());

    Event reminder = eventMap.get(Tag.reminder.toString());
    Event reminder2 = eventMap.get(Tag.reminder2.toString());
    Event reminder3 = eventMap.get(Tag.reminder3.toString());
    return eventDuringExercise(goLive, reminder, exerciseEnd)
        && eventDuringExercise(goLive, reminder2, exerciseEnd)
        && eventDuringExercise(goLive, reminder3, exerciseEnd);
  }

  private boolean eventDuringExercise(Event goLive, Event event, Event exerciseEnd) {
    boolean isValid = true;
    if (event != null) {
      isValid =
          goLive.getTimestamp().before(event.getTimestamp())
              && event.getTimestamp().before(exerciseEnd.getTimestamp());
    }
    return isValid;
  }

  /**
   * Validates the mandatory events are in the following order: MPS Go Live Return By Exercise End
   */
  private boolean validateMandatoryEvents(final Map<String, Event> eventMap) {
    Event mpsEvent = eventMap.get(Tag.mps.toString());
    Event goLiveEvent = eventMap.get(Tag.go_live.toString());
    Event returnByEvent = eventMap.get(Tag.return_by.toString());
    Event exerciseEndEvent = eventMap.get(Tag.exercise_end.toString());

    return mpsEvent.getTimestamp().before(goLiveEvent.getTimestamp())
        && goLiveEvent.getTimestamp().before(returnByEvent.getTimestamp())
        && returnByEvent.getTimestamp().before(exerciseEndEvent.getTimestamp());
  }

  private Map<String, Event> generateEventsMap(
      final List<Event> existingEvents, final Event updatedEvent) {
    Map<String, Event> eventMap = new HashMap<>();
    for (Event event : existingEvents) {
      eventMap.put(event.getTag(), event);
    }
    eventMap.put(updatedEvent.getTag(), updatedEvent);

    return eventMap;
  }

  private boolean isEventInPast(final Event existingEvent) {
    Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
    return existingEvent.getTimestamp().before(currentTimestamp);
  }

  private boolean isMandatory(final Event updatedEvent) {
    return Tag.valueOf(updatedEvent.getTag()).isMandatory();
  }

  private boolean isReminder(final Event updatedEvent) {
    return Tag.valueOf(updatedEvent.getTag()).isReminder();
  }
}
