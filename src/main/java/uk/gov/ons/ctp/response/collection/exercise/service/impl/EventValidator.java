package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

public class EventValidator {

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
      final CollectionExerciseDTO.CollectionExerciseState collectionExerciseState) {

    // Can only update reminders of the non mandatory events when READY_FOR_LIVE
    if ((collectionExerciseState.equals(
                CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE)
            || collectionExerciseState.equals(CollectionExerciseDTO.CollectionExerciseState.LIVE))
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
