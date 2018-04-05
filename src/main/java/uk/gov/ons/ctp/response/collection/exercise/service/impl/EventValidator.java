package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EventValidator {

    /**
     * Validates the events timestamps are in the correct order and the updated event can be updated,
     * i.e is not a mandatory or reminder event.
     * @param existingEvents
     * @param updatedEvent
     * @return
     */
    public boolean validate(final List<Event> existingEvents, final Event updatedEvent,
                            final CollectionExerciseDTO.CollectionExerciseState collectionExerciseState) {

        // Can only update reminders of the non mandatory events when READY_FOR_LIVE
        if (collectionExerciseState.equals(CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE)
                && (!isReminder(updatedEvent) && !isMandatory(updatedEvent))) {
            return false;
        }

        Optional<Event> existingEvent = existingEvents.stream().findFirst().filter(
                event -> event.getTag().equals(updatedEvent.getTag()));

        // Mandatory & Reminder events can't be updated if already past
        if ((isMandatory(updatedEvent) || isReminder(updatedEvent)) && isEventInPast(existingEvent)) {
            return false;
        }

        Map<String, Event> eventMap = generateEventsMap(existingEvents, updatedEvent);

        return validateMandatoryEvents(eventMap) && validateNonMandatoryEvents(eventMap);
    }

    /**
     * Validates the dates which aren't mandatory for a collection exercise to be executed.
     *  If the events exist it checks that reference period start before referencePeriodEnd and
     *  all reminders are during the collection exercise.
     * @param eventMap
     * @return
     */
    private boolean validateNonMandatoryEvents(final Map<String, Event>eventMap) {
        Event referencePeriodStart = eventMap.get(EventService.Tag.ref_period_start.toString());
        Event referencePeriodEnd = eventMap.get(EventService.Tag.ref_period_end.toString());

        return referencePeriodInCorrectOrder(referencePeriodStart, referencePeriodEnd)
                && remindersDuringExercise(eventMap);
    }

    private boolean referencePeriodInCorrectOrder(final Event referencePeriodStart, final Event referencePeriodEnd) {
        boolean isValid = true;
        if (referencePeriodStart != null && referencePeriodEnd != null) {
            isValid = referencePeriodStart.getTimestamp().before(referencePeriodEnd.getTimestamp());
        }
        return isValid;
    }


    private boolean remindersDuringExercise(final Map<String, Event>eventMap) {
        Event goLive = eventMap.get(EventService.Tag.go_live.toString());
        Event exerciseEnd = eventMap.get(EventService.Tag.exercise_end.toString());

        Event reminder1 = eventMap.get(EventService.Tag.reminder_1.toString());
        Event reminder2 = eventMap.get(EventService.Tag.reminder_2.toString());
        Event reminder3 = eventMap.get(EventService.Tag.reminder_3.toString());

        return eventDuringExercise(goLive, reminder1, exerciseEnd)
                && eventDuringExercise(goLive, reminder2, exerciseEnd)
                && eventDuringExercise(goLive, reminder3, exerciseEnd);

    }

    private boolean eventDuringExercise(Event goLive, Event event, Event exerciseEnd) {
        boolean isValid = true;
        if(event != null) {
            isValid = goLive.getTimestamp().before(event.getTimestamp())
                    && event.getTimestamp().before(exerciseEnd.getTimestamp());
        }
        return isValid;
    }

    /**
     * Validates the mandatory events are in the following order:
     * MPS
     * Go Live
     * Return By
     * Exercise End
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

    private Map<String, Event> generateEventsMap(final List<Event> existingEvents, final Event updatedEvent) {
        Map<String, Event> eventMap = new HashMap<>();
        for (Event event : existingEvents) {
            eventMap.put(event.getTag(), event);
        }
        eventMap.put(updatedEvent.getTag(), updatedEvent);

        return eventMap;
    }

    private boolean isEventInPast(final Optional<Event> existingEvent) {
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        return existingEvent.isPresent() && existingEvent.get().getTimestamp().before(currentTimestamp);
    }

    private boolean isMandatory(final Event updatedEvent) {
        return EventService.Tag.valueOf(updatedEvent.getTag()).isMandatory();
    }

    private boolean isReminder(final Event updatedEvent) {
        return EventService.Tag.valueOf(updatedEvent.getTag()).equals(EventService.Tag.reminder_1)
                || EventService.Tag.valueOf(updatedEvent.getTag()).equals(EventService.Tag.reminder_2)
                || EventService.Tag.valueOf(updatedEvent.getTag()).equals(EventService.Tag.reminder_3);
    }
}
