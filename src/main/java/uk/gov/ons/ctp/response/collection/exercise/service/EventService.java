package uk.gov.ons.ctp.response.collection.exercise.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseBeanMapper.MessageType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CaseSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEndPublisher;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CaseActionEventStatusDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.ResponseEventDTO;

@Service
public class EventService {
  private static final Logger log = LoggerFactory.getLogger(EventService.class);
  /** An enum to represent the collection exercise events that are mandatory for all surveys */
  public enum Tag {
    mps(true),
    go_live(true),
    return_by(true),
    exercise_end(true),
    reminder(false),
    reminder2(false),
    reminder3(false),
    ref_period_start(false),
    ref_period_end(false),
    employment(false),
    nudge_email_0(false),
    nudge_email_1(false),
    nudge_email_2(false),
    nudge_email_3(false),
    nudge_email_4(false);

    public static final List<Tag> ORDERED_REMINDERS = Arrays.asList(reminder, reminder2, reminder3);
    public static final List<Tag> ORDERED_NUDGE_EMAIL =
        Arrays.asList(nudge_email_0, nudge_email_1, nudge_email_2, nudge_email_3, nudge_email_4);
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
      List<EventService.Tag> actionableEvents =
          Arrays.asList(
              mps,
              go_live,
              reminder,
              reminder2,
              reminder3,
              nudge_email_0,
              nudge_email_1,
              nudge_email_2,
              nudge_email_3,
              nudge_email_4);

      return actionableEvents.contains(this);
    }

    public boolean hasName(final String eventName) {
      return name().equals(eventName);
    }

    public boolean isReminder() {
      return ORDERED_REMINDERS.contains(this);
    }

    public boolean isNudgeEmail() {
      return ORDERED_NUDGE_EMAIL.contains(this);
    }
  }

  public static EventDTO createEventDTOFromEvent(Event event) {
    EventDTO dto = new EventDTO();

    dto.setCollectionExerciseId(event.getCollectionExercise().getId());
    dto.setId(event.getId());
    dto.setTag(event.getTag());
    dto.setTimestamp(event.getTimestamp());
    dto.setEventStatus(event.getStatus());

    return dto;
  }

  @Autowired private AppConfig appConfig;

  @Autowired private CollectionExerciseService collectionExerciseService;

  @Autowired private EventRepository eventRepository;

  @Autowired private EventChangeHandler[] changeHandlers = {};

  @Autowired private List<EventValidator> eventValidators;

  @Autowired private ActionSvcClient actionSvcClient;

  @Autowired private CaseSvcClient caseSvcClient;

  @Autowired private CollectionExerciseEndPublisher collectionExerciseEndPublisher;

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
    event.setStatus(EventDTO.Status.SCHEDULED);
    validateSubmittedEvent(collex, event);

    event = eventRepository.save(event);
    fireEventChangeHandlers(MessageType.EventCreated, event);

    return event;
  }

  public List<Event> getEvents(UUID collexId) throws CTPException {
    return this.eventRepository.findByCollectionExerciseId(collexId);
  }

  public List<Event> getEvents(List<UUID> collexIds) throws CTPException {
    return this.eventRepository.findByCollectionExerciseIdIn(collexIds);
  }

  public ResponseEventDTO updateEvent(final UUID collexUuid, final String tag, final Date date)
      throws CTPException {
    final CollectionExercise collex = getCollectionExercise(collexUuid, Fault.BAD_REQUEST);
    final Event event = getEventByTagAndCollectionExerciseId(tag, collex);

    ResponseEventDTO updatedEvent = new ResponseEventDTO();
    event.setTimestamp(new Timestamp(date.getTime()));
    validateSubmittedEvent(collex, event);
    if (tag.equals("return_by")) {
      deleteNudgeEmail(collex, event, updatedEvent);
    }
    eventRepository.save(event);

    fireEventChangeHandlers(MessageType.EventUpdated, event);
    updatedEvent.setEvent(event);
    return updatedEvent;
  }

  private void deleteNudgeEmail(
      final CollectionExercise collex, final Event event, final ResponseEventDTO updateDTO)
      throws CTPException {
    final List<Event> existingEvents = eventRepository.findByCollectionExercise(collex);
    final List<Event> existingNudgeEmails =
        filterExistingNudgeEmails(existingEvents, event, collex.getState());
    if (existingNudgeEmails.size() > 0) {
      updateDTO.setInfo(
          String.format(
              "%s nudge email scheduled after the return by date was removed.",
              existingNudgeEmails.size()));
    }
    for (Event nudgeEmail : existingNudgeEmails) {
      nudgeEmail.setDeleted(true);
      this.eventRepository.delete(nudgeEmail);
    }
  }

  private void validateSubmittedEvent(final CollectionExercise collex, final Event event)
      throws CTPException {
    final List<Event> existingEvents = eventRepository.findByCollectionExercise(collex);

    for (EventValidator validator : eventValidators) {
      validator.validate(existingEvents, event, collex.getState());
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

    CollectionExercise collex = getCollectionExercise(collexUuid, Fault.BAD_REQUEST);
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

  public List<Event> getOutstandingEvents() {
    return this.eventRepository.findByMessageSentNull();
  }

  public void setEventMessageSent(UUID eventId) throws CTPException {
    Event event = getEvent(eventId);

    event.setMessageSent(new Timestamp(new Date().getTime()));

    this.eventRepository.save(event);
  }

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

  @Transactional
  public void updateEventStatus(CaseActionEventStatusDTO eventStatus) {
    Event eventToBeUpdated =
        eventRepository.findOneByCollectionExerciseIdAndTag(
            eventStatus.getCollectionExerciseID(), eventStatus.getTag().toString());
    if (null == eventToBeUpdated) {
      log.with("collectionExerciseId", eventStatus.getCollectionExerciseID().toString())
          .with("eventTag", eventStatus.getTag().toString())
          .error("Unable to find an event for the matching combination.");
      return;
    }
    eventToBeUpdated.setStatus(eventStatus.getStatus());
    eventRepository.saveAndFlush(eventToBeUpdated);
  }

  /** Get all the scheduled events and send them to action to be acted on. */
  @Transactional
  public void processEvents() {
    Stream<Event> eventList = eventRepository.findByStatus(EventDTO.Status.SCHEDULED);
    AtomicInteger counter = new AtomicInteger();
    eventList.forEach(
        event -> {
          counter.getAndIncrement();
          CollectionExercise exercise = event.getCollectionExercise();
          boolean isExerciseActive = isCollectionExerciseActive(exercise);
          boolean isEventInThePast = event.getTimestamp().before(Timestamp.from(Instant.now()));
          if (isExerciseActive && isEventInThePast) {
            log.with("id", event.getId()).with("tag", event.getTag()).info("Processing event");

            /* There is a situation where case could still be processing messages from sample while an event happens
             * if the ready for live button is pressed shortly before an event is meant to trigger.
             *
             * If we send the event to action before case has all the data it needs, it can result in potentially
             * missing entries in the print files and actions not being taken.
             *
             * By not handling the event until the case service has a number of cases equal to the expected sample size
             * we can guarantee case (and action by extension) will have everything set up before anything else happens.
             */
            Long numberOfCases = caseSvcClient.getNumberOfCases(exercise.getId());
            log.with("collection_exercise_id", exercise.getId())
                .with("number_of_cases", numberOfCases)
                .with("sample_size", exercise.getSampleSize())
                .info(
                    "About to check that case has every sample in this exercise before processing this event");
            boolean casesMatchSampleSize;
            if (exercise.getSampleSize() == null) {
              log.with("collection_exercise_id", exercise.getId())
                  .info(
                      "Collection exercise has null value for sample. Setting the match to false");
              casesMatchSampleSize = false;
            } else {
              casesMatchSampleSize = numberOfCases.longValue() == exercise.getSampleSize();
            }
            if (casesMatchSampleSize) {
              // If the event is go_live we need to transition the state of the collection exercise
              Tag tag = EventService.Tag.valueOf(event.getTag());
              if (tag == EventService.Tag.go_live) {
                try {
                  collectionExerciseService.transitionCollectionExercise(
                      event.getCollectionExercise(),
                      CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
                  log.with("collection_exercise_id", event.getCollectionExercise().getId())
                      .info("Set collection exercise to LIVE state");
                } catch (CTPException e) {
                  log.with("collection_exercise_id", event.getCollectionExercise().getId())
                      .error("Failed to set collection exercise to LIVE state", e);
                }
              }
              if (tag == EventService.Tag.exercise_end) {
                try {
                  collectionExerciseService.transitionCollectionExercise(
                      event.getCollectionExercise(),
                      CollectionExerciseDTO.CollectionExerciseEvent.END_EXERCISE);
                  log.with("collection_exercise_id", event.getCollectionExercise().getId())
                      .info("Set collection exercise to ENDED state");
                  collectionExerciseEndPublisher.sendCollectionExerciseEnd(
                      event.getCollectionExercise().getId());
                } catch (CTPException e) {
                  log.with("collection_exercise_id", event.getCollectionExercise().getId())
                      .error("Failed to set collection exercise to ENDED state", e);
                }
              }

              if (tag.isActionable()) {
                log.with("tag", event.getTag()).info("Event is actionable, beginning processing");
                boolean success;
                success = caseSvcClient.processEvent(event.getTag(), exercise.getId());

                if (success) {
                  log.info("Event processing succeeded, setting to PROCESSING state");
                  EventDTO.Status status = EventDTO.Status.PROCESSING;
                  event.setStatus(status);
                  event.setMessageSent(Timestamp.from(Instant.now()));
                } else {
                  log.error(
                      "Event processing failed, due to service call hence keeping SCHEDULED state");
                  event.setStatus(EventDTO.Status.SCHEDULED);
                }
              } else {
                log.with("tag", event.getTag())
                    .debug("Event is not actionable, setting to COMPLETED state");
                event.setStatus(EventDTO.Status.PROCESSED);
              }
              eventRepository.saveAndFlush(event);
            } else {
              log.with("collection_exercise_id", exercise.getId())
                  .with("number_of_cases", numberOfCases)
                  .with("sample_size", exercise.getSampleSize())
                  .info(
                      "Number of cases does not match the sample size.  Case may still be processing messages from sample");
            }
          }
        });
    log.info("Found [" + counter + "] events in the SCHEDULED state");
  }

  private List<Event> filterExistingNudgeEmails(
      List<Event> existingEvents,
      Event submittedEvent,
      CollectionExerciseDTO.CollectionExerciseState collectionExerciseState)
      throws CTPException {
    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));
    return EventService.Tag.ORDERED_NUDGE_EMAIL
        .stream()
        .map(tag -> getEventByTag(tag, existingEventsMap))
        .filter(Objects::nonNull)
        .filter(event -> isEventAfterReturnBy(event, submittedEvent))
        .collect(Collectors.toList());
  }

  /**
   * Check if a collection exercise is in an active state. 'Active' in this context means either
   * live or ready_for_live. An exercise in the READY_FOR_LIVE state has preparation events that can
   * happen even if it's not 'live' yet (i.e., mps).
   *
   * @param exercise A collection exercise object
   * @return True/False value on whether the exercise is active
   */
  private boolean isCollectionExerciseActive(CollectionExercise exercise) {
    List<CollectionExerciseDTO.CollectionExerciseState> states =
        Arrays.asList(
            CollectionExerciseDTO.CollectionExerciseState.LIVE,
            CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE);
    return states.contains(exercise.getState());
  }

  private boolean isEventAfterReturnBy(Event nudgeEvent, Event submittedEvent) {
    return nudgeEvent.getTimestamp().after(submittedEvent.getTimestamp());
  }

  private Event getEventByTag(EventService.Tag tag, Map<String, Event> existingEvents) {
    return existingEvents.get(tag.toString());
  }
}
