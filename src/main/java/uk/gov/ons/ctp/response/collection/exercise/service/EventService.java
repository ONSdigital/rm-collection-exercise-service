package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

import java.util.List;
import java.util.UUID;

public interface EventService {
    Event createEvent(EventDTO eventDto) throws CTPException;

    List<Event> getEvents(UUID collexId) throws CTPException;

    static EventDTO createEventDTOFromEvent(Event event){
        EventDTO dto = new EventDTO();

        dto.setCollectionExerciseId(event.getId());
        dto.setTag(event.getTag());
        dto.setTimestamp(event.getTimestamp());

        return dto;
    }
}
