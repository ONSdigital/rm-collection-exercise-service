package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher.MessageType;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.schedule.SchedulerConfiguration;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

@Service
public class EventServiceImpl implements EventService {
  private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

  @Autowired private CollectionExerciseService collectionExerciseService;

  @Autowired private EventRepository eventRepository;

  @Autowired private EventChangeHandler[] changeHandlers = {};

  @Autowired private EventValidator eventValidator;

  @Autowired private SurveySvcClient surveySvcClient;

  @Autowired private Scheduler scheduler;

  @Autowired private List<ActionRuleCreator> actionRuleCreators;

  @Autowired private List<ActionRuleUpdater> actionRuleUpdaters;

  @Override
  public Event createEvent(EventDTO eventDto) throws CTPException {
    UUID collexId = eventDto.getCollectionExerciseId();
    CollectionExercise collex = getCollectionExercise(collexId, Fault.RESOURCE_NOT_FOUND);
    Event existing =
        this.eventRepository.findOneByCollectionExerciseAndTag(collex, eventDto.getTag());

    if (existing != null) {
      throw new CTPException(
          Fault.RESOURCE_VERSION_CONFLICT,
          String.format(
              "Event %s already exists for collection exercise %s",
              eventDto.getTag(), collex.getId()));
    }

    Event event = new Event();

    event.setCollectionExercise(collex);
    event.setTag(eventDto.getTag());
    event.setId(UUID.randomUUID());
    event.setTimestamp(new Timestamp(eventDto.getTimestamp().getTime()));
    event.setCreated(new Timestamp(new Date().getTime()));

    final List<Event> existingEvents = eventRepository.findByCollectionExercise(collex);
    if (!eventValidator.validate(existingEvents, event, collex.getState())) {
      throw new CTPException(CTPException.Fault.BAD_REQUEST, String.format("Invalid event update"));
    }

    createActionRulesForEvent(event);
    event = eventRepository.save(event);

    fireEventChangeHandlers(MessageType.EventCreated, event);

    return event;
  }

  @Override
  public void createActionRulesForEvent(final Event collectionExerciseEvent) throws CTPException {
    if (!Tag.valueOf(collectionExerciseEvent.getTag()).isActionable()) {
      return;
    }

    for (ActionRuleCreator arc : actionRuleCreators) {
      arc.execute(collectionExerciseEvent);
    }
  }

  @Override
  public List<Event> getEvents(UUID collexId) throws CTPException {
    return this.eventRepository.findByCollectionExerciseId(collexId);
  }

  @Override
  public Event updateEvent(final UUID collexUuid, final String tag, final Date date)
      throws CTPException {
    final CollectionExercise collex = getCollectionExercise(collexUuid, Fault.BAD_REQUEST);
    final Event event = getEventByTagAndCollectionExerciseId(tag, collex);

    event.setTimestamp(new Timestamp(date.getTime()));
    validateUpdatedEvents(collex, event);
    updateActionRules(event);

    eventRepository.save(event);

    fireEventChangeHandlers(MessageType.EventUpdated, event);

    return event;
  }

  private void updateActionRules(final Event event) throws CTPException {

    for (final ActionRuleUpdater aru : actionRuleUpdaters) {
      aru.execute(event);
    }
  }

  private void validateUpdatedEvents(final CollectionExercise collex, final Event event)
      throws CTPException {
    final List<Event> existingEvents = eventRepository.findByCollectionExercise(collex);

    if (!eventValidator.validate(existingEvents, event, collex.getState())) {
      throw new CTPException(Fault.BAD_REQUEST, "Invalid event update");
    }
  }

  private Event getEventByTagAndCollectionExerciseId(
      final String tag, final CollectionExercise collex) throws CTPException {
    final Event event = eventRepository.findOneByCollectionExerciseAndTag(collex, tag);
    if (event == null) {
      throw new CTPException(
          Fault.RESOURCE_NOT_FOUND,
          String.format(
              "Event with tag %s for Collection Exercise %s does not exist", tag, collex.getId()));
    }
    return event;
  }

  private CollectionExercise getCollectionExercise(final UUID collexUuid, final Fault fault)
      throws CTPException {
    final CollectionExercise collex = collectionExerciseService.findCollectionExercise(collexUuid);

    if (collex == null) {
      throw new CTPException(
          fault, String.format("Collection exercise %s does not exist", collexUuid));
    }
    return collex;
  }

  @Override
  public Event getEvent(UUID collexUuid, String tag) throws CTPException {
    CollectionExercise collex = this.collectionExerciseService.findCollectionExercise(collexUuid);
    if (collex != null) {
      Event event = this.eventRepository.findOneByCollectionExerciseAndTag(collex, tag);
      if (event != null) {
        return event;
      } else {
        throw new CTPException(
            Fault.RESOURCE_NOT_FOUND, String.format("Event %s does not exist", event.getId()));
      }

    } else {
      throw new CTPException(
          Fault.BAD_REQUEST,
          String.format("Collection exercise %s does not exist", collex.getId()));
    }
  }

  @Override
  public Event getEvent(UUID eventId) throws CTPException {
    Event event = this.eventRepository.findOneById(eventId);

    if (event == null) {
      throw new CTPException(
          Fault.RESOURCE_NOT_FOUND, String.format("Event %s does not exist", event));
    } else {
      return event;
    }
  }

  public Event deleteEvent(UUID collexUuid, String tag) throws CTPException {

    CollectionExercise collex = this.collectionExerciseService.findCollectionExercise(collexUuid);
    if (collex != null) {
      Event event = this.eventRepository.findOneByCollectionExerciseAndTag(collex, tag);
      if (event != null) {
        event.setDeleted(true);
        this.eventRepository.delete(event);

        fireEventChangeHandlers(MessageType.EventDeleted, event);
        return event;
      } else {
        throw new CTPException(
            Fault.RESOURCE_NOT_FOUND, String.format("Event %s does not exist", tag));
      }

    } else {
      throw new CTPException(
          Fault.BAD_REQUEST,
          String.format("Collection exercise %s does not exist", collex.getId()));
    }
  }

  /**
   * Method that is called whenever a change occurs to a collection exercise event
   *
   * @param messageType the type of change
   * @param event the event to which the change occurred
   */
  private void fireEventChangeHandlers(final MessageType messageType, final Event event) {

    Arrays.stream(changeHandlers)
        .forEach(
            handler -> {
              try {
                handler.handleEventLifecycle(messageType, event);
              } catch (CTPException e) {
                log.with("message_type", messageType)
                    .with("event_id", event.getId())
                    .error("Failed to handle event change", e);
              }
            });
  }

  @Override
  public List<Event> getOutstandingEvents() {
    return this.eventRepository.findByMessageSentNull();
  }

  @Override
  public void setEventMessageSent(UUID eventId) throws CTPException {
    Event event = getEvent(eventId);

    event.setMessageSent(new Timestamp(new Date().getTime()));

    this.eventRepository.save(event);
  }

  @Override
  public void clearEventMessageSent(UUID eventId) throws CTPException {
    Event event = getEvent(eventId);

    event.setMessageSent(null);

    this.eventRepository.save(event);
  }

  /**
   * 'scheduled' is defined as having events for mps, go_live, return_by and exercise_end
   *
   * @param collexUuid the collection exercise to check for scheduled
   * @return true if the mandatory events are all present, false otherwise
   * @throws CTPException if collection exercise not found etc
   */
  @Override
  public boolean isScheduled(UUID collexUuid) throws CTPException {
    Map<String, Event> events =
        getEvents(collexUuid)
            .stream()
            .collect(Collectors.toMap(Event::getTag, Function.identity()));

    int numberOfMandatoryEvents =
        Arrays.stream(Tag.values()).filter(Tag::isMandatory).collect(Collectors.toList()).size();

    return Arrays.stream(Tag.values())
            .filter(Tag::isMandatory)
            .map(t -> events.get(t.name()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
            .size()
        >= numberOfMandatoryEvents;
  }

  /**
   * Unschedule a collection exercise event
   *
   * @param event the event to unschedule
   * @throws CTPException thrown if error occurred scheduling event
   */
  @Override
  public void unscheduleEvent(final Event event) throws CTPException {
    try {
      SchedulerConfiguration.unscheduleEvent(this.scheduler, event);
    } catch (SchedulerException e) {
      throw new CTPException(
          Fault.SYSTEM_ERROR, String.format("Error unscheduling event %s", event.getId()), e);
    }
  }

  /**
   * Schedule a collection exercise event
   *
   * @param event the event to shchedule
   * @throws CTPException thrown if error occurred scheduling event
   */
  @Override
  public void scheduleEvent(final Event event) throws CTPException {
    try {
      SchedulerConfiguration.scheduleEvent(this.scheduler, event);
    } catch (SchedulerException e) {
      throw new CTPException(
          Fault.SYSTEM_ERROR, String.format("Error scheduling event %s", event.getId()), e);
    }
  }
}
