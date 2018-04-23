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
    enum Tag {
        mps(true), go_live(true), return_by(true), exercise_end(true), reminder(false), reminder2(false),
        reminder3(false), ref_period_start(false), ref_period_end(false), employment_date(false) ;

        Tag(final boolean mandatory){
            this.mandatory = mandatory;
        }

        public boolean isMandatory(){
            return mandatory;
        }

        private boolean mandatory;
    }

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
    boolean isScheduled(UUID collexUuid) throws CTPException;

    /**
     * Unschedule a collection exercise event
     * @param event the event to unshchedule
     * @throws CTPException thrown if error occurred unscheduling event
     */
    void unscheduleEvent(Event event) throws CTPException;

    /**
     * Schedule a collection exercise event
     * @param event the event to shchedule
     * @throws CTPException thrown if error occurred scheduling event
     */
    void scheduleEvent(Event event) throws CTPException;

}
