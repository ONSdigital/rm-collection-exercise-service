package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

import java.util.List;
import java.util.UUID;
import java.util.Date;

public interface EventService {

    /**
     * An enum to represent the collection exercise events that are mandatory for all surveys
     */
    enum Tag { mps, go_live, return_by, exercise_end }

    Event createEvent(EventDTO eventDto) throws CTPException;

    List<Event> getEvents(UUID collexId) throws CTPException;

    static EventDTO createEventDTOFromEvent(Event event){
        EventDTO dto = new EventDTO();

        dto.setCollectionExerciseId(event.getCollectionExercise().getId());
        dto.setId(event.getId());
        dto.setTag(event.getTag());
        dto.setTimestamp(event.getTimestamp());

        return dto;
    }

    Event updateEvent(UUID collexUuid, String tag, Date date) throws CTPException;
    Event getEvent(UUID collexUuid, String tag) throws CTPException;
    Event getEvent(UUID eventId) throws CTPException;
    Event deleteEvent(UUID collexUuid, String tag) throws CTPException;
    List<Event> getOutstandingEvents();
    void setEventMessageSent(UUID eventId) throws CTPException;
    void clearEventMessageSent(UUID eventId) throws CTPException;

}
