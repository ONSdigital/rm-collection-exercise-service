package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EventValidator {

    /**
     * Validates the events timestamps are in the correct order and the updated event isn't already past.
     * @param existingEvents
     * @param updatedEvent
     * @return
     */
    public boolean validate(final List<Event> existingEvents, final Event updatedEvent) {

        Optional<Event> existingEvent = existingEvents.stream().findFirst().filter(
                event -> event.getTag().equals(updatedEvent.getTag()));

        if (isEventInPast(existingEvent)) {
            return false;
        }

        Map<String, Event> eventMap = generateEventsMap(existingEvents, updatedEvent);

        Event mpsEvent = eventMap.get(EventService.Tag.mps.toString());
        Event goLiveEvent = eventMap.get(EventService.Tag.go_live.toString());
        Event returnByEvent = eventMap.get(EventService.Tag.return_by.toString());
        Event exerciseEndEvent = eventMap.get(EventService.Tag.exercise_end.toString());


        if (!mpsEvent.getTimestamp().before(goLiveEvent.getTimestamp())) {
            return false;
        }

        if (!goLiveEvent.getTimestamp().before(returnByEvent.getTimestamp())) {
            return false;
        }

        if (!returnByEvent.getTimestamp().before(exerciseEndEvent.getTimestamp())) {
            return false;
        }

        return true;
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
}
