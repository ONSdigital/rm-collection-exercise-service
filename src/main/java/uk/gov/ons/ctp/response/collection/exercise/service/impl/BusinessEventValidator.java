package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
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
    final Optional<Event> mpsEvent =
        Optional.ofNullable(eventMap.get(EventService.Tag.mps.toString()));
    final Optional<Event> goLiveEvent =
        Optional.ofNullable(eventMap.get(EventService.Tag.go_live.toString()));
    final Optional<Event> returnByEvent =
        Optional.ofNullable(eventMap.get(EventService.Tag.return_by.toString()));
    final Optional<Event> exerciseEndEvent =
        Optional.ofNullable(eventMap.get(EventService.Tag.exercise_end.toString()));

    List<Event> events = new ArrayList<>();
    if (newEvent.getTag().equals(EventService.Tag.mps.toString())) {
      events.add(newEvent);
    } else {
      mpsEvent.ifPresent(events::add);
    }

    if (newEvent.getTag().equals(EventService.Tag.go_live.toString())) {
      events.add(newEvent);
    } else {
      goLiveEvent.ifPresent(events::add);
    }

    if (newEvent.getTag().equals(EventService.Tag.return_by.toString())) {
      events.add(newEvent);
    } else {
      returnByEvent.ifPresent(events::add);
    }

    if (newEvent.getTag().equals(EventService.Tag.exercise_end.toString())) {
      events.add(newEvent);
    } else {
      exerciseEndEvent.ifPresent(events::add);
    }

    return datesInValidOrder(events);
  }

  private boolean datesInValidOrder(List<Event> events) {
    Timestamp previous = null;
    boolean result = true;
    for (Event e : events) {
      if (previous == null) {
        previous = e.getTimestamp();
      } else {
        if (previous.after(e.getTimestamp())) {
          result = false;
        }
      }
    }
    return result;
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
    Event referencePeriodStart = eventMap.get(EventService.Tag.ref_period_start.toString());
    Event referencePeriodEnd = eventMap.get(EventService.Tag.ref_period_end.toString());

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
    Event goLive = eventMap.get(EventService.Tag.go_live.toString());
    Event exerciseEnd = eventMap.get(EventService.Tag.exercise_end.toString());

    Event reminder = eventMap.get(EventService.Tag.reminder.toString());
    Event reminder2 = eventMap.get(EventService.Tag.reminder2.toString());
    Event reminder3 = eventMap.get(EventService.Tag.reminder3.toString());
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
    Event mpsEvent = eventMap.get(EventService.Tag.mps.toString());
    Event goLiveEvent = eventMap.get(EventService.Tag.go_live.toString());
    Event returnByEvent = eventMap.get(EventService.Tag.return_by.toString());
    Event exerciseEndEvent = eventMap.get(EventService.Tag.exercise_end.toString());

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
    return EventService.Tag.valueOf(updatedEvent.getTag()).isMandatory();
  }

  private boolean isReminder(final Event updatedEvent) {
    return EventService.Tag.valueOf(updatedEvent.getTag()).equals(EventService.Tag.reminder)
        || EventService.Tag.valueOf(updatedEvent.getTag()).equals(EventService.Tag.reminder2)
        || EventService.Tag.valueOf(updatedEvent.getTag()).equals(EventService.Tag.reminder3);
  }
}
