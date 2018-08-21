package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

public interface EventService {

  /** An enum to represent the collection exercise events that are mandatory for all surveys */
  enum Tag {
    mps(true),
    go_live(true),
    return_by(true),
    exercise_end(true),
    reminder(false),
    reminder2(false),
    reminder3(false),
    ref_period_start(false),
    ref_period_end(false),
    employment(false);

    public static final List<Tag> ORDERED_REMINDERS = Arrays.asList(reminder, reminder2, reminder3);
    public static final List<Tag> ORDERED_MANDATORY_EVENTS =
        Arrays.asList(Tag.mps, Tag.go_live, Tag.return_by, Tag.exercise_end);

    Tag(final boolean mandatory) {
      this.mandatory = mandatory;
    }

    public boolean isMandatory() {
      return mandatory;
    }

    private boolean mandatory;

    public boolean isActionable() {
      List<Tag> actionableEvents = Arrays.asList(mps, go_live, reminder, reminder2, reminder3);

      return actionableEvents.contains(this);
    }

    public boolean hasName(final String eventName) {
      return name().equals(eventName);
    }

    public boolean isReminder() {
      return ORDERED_REMINDERS.contains(this);
    }
  }

  Event createEvent(EventDTO eventDto) throws CTPException;

  List<Event> getEvents(UUID collexId) throws CTPException;

  static EventDTO createEventDTOFromEvent(Event event) {
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
   *
   * @param event the event to unshchedule
   * @throws CTPException thrown if error occurred unscheduling event
   */
  void unscheduleEvent(Event event) throws CTPException;

  /**
   * Schedule a collection exercise event
   *
   * @param event the event to shchedule
   * @throws CTPException thrown if error occurred scheduling event
   */
  void scheduleEvent(Event event) throws CTPException;

  /**
   * Create action rules for collection exercise event
   *
   * @param event the event to create action rules for
   * @throws CTPException on error
   */
  void createActionRulesForEvent(Event event) throws CTPException;
}
