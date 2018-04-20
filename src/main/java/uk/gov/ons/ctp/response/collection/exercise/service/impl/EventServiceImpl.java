package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.schedule.SchedulerConfiguration;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventChangeHandler[] changeHandlers;

    @Autowired
    private EventValidator eventValidator;

    @Autowired
    private Scheduler scheduler;

    @Override
    public Event createEvent(EventDTO eventDto) throws CTPException {
        UUID collexId = eventDto.getCollectionExerciseId();
        CollectionExercise collex =
                this.collectionExerciseService.findCollectionExercise(collexId);

        if (collex == null) {
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                    String.format("Collection exercise %s does not exist", eventDto.getCollectionExerciseId()));
        } else {
            Event existing = this.eventRepository.findOneByCollectionExerciseAndTag(collex, eventDto.getTag());

            if (existing != null) {
                throw new CTPException(CTPException.Fault.RESOURCE_VERSION_CONFLICT,
                        String.format("Event %s already exists for collection exercise %s",
                                eventDto.getTag(), collex.getId()));
            } else {
                Event event = new Event();

                event.setCollectionExercise(collex);
                event.setTag(eventDto.getTag());
                event.setId(UUID.randomUUID());
                event.setTimestamp(new Timestamp(eventDto.getTimestamp().getTime()));
                event.setCreated(new Timestamp(new Date().getTime()));

                event = eventRepository.save(event);

                fireEventChangeHandlers(CollectionExerciseEventPublisher.MessageType.EventCreated, event);

                return event;
            }
        }
    }

    @Override
    public List<Event> getEvents(UUID collexId) throws CTPException {
        return this.eventRepository.findByCollectionExerciseId(collexId);
    }

    @Override
    public Event updateEvent(UUID collexUuid, String tag, Date date) throws CTPException
    {
        CollectionExercise collex = this.collectionExerciseService.findCollectionExercise(collexUuid);
        if (collex != null)
        {
            Event event = this.eventRepository.findOneByCollectionExerciseAndTag(collex, tag);
            if (event != null)
            {
                event.setTimestamp(new Timestamp(date.getTime()));

                List<Event> existingEvents = this.eventRepository.findByCollectionExercise(collex);

                if (this.eventValidator.validate(existingEvents, event, collex.getState())) {

                    this.eventRepository.save(event);

                    fireEventChangeHandlers(CollectionExerciseEventPublisher.MessageType.EventUpdated, event);
                } else {
                    throw new CTPException(CTPException.Fault.BAD_REQUEST,
                            String.format("Invalid event update"));
                }

            }
            else
                {
                    throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                            String.format("Event %s does not exist", event.getId()));
                }

                return event;
        }
        else
            {
                throw new CTPException(CTPException.Fault.BAD_REQUEST,
                        String.format("Collection exercise %s does not exist", collexUuid));
            }


    }

    @Override
    public Event getEvent(UUID collexUuid, String tag) throws CTPException
    {
        CollectionExercise collex = this.collectionExerciseService.findCollectionExercise(collexUuid);
        if (collex != null)
        {
            Event event = this.eventRepository.findOneByCollectionExerciseAndTag(collex, tag);
            if (event != null)
            {
                return event;
            }
            else
            {
                throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                        String.format("Event %s does not exist", event.getId()));
            }

        }
        else
        {
            throw new CTPException(CTPException.Fault.BAD_REQUEST,
                    String.format("Collection exercise %s does not exist", collex.getId()));
        }

    }

    @Override
    public Event getEvent(UUID eventId) throws CTPException {
        Event event = this.eventRepository.findOneById(eventId);

        if (event == null){
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, String.format("Event %s does not exist",
                    event));
        } else {
            return event;
        }
    }

    public Event deleteEvent(UUID collexUuid, String tag) throws CTPException
    {

        CollectionExercise collex = this.collectionExerciseService.findCollectionExercise(collexUuid);
        if (collex != null)
        {
            Event event = this.eventRepository.findOneByCollectionExerciseAndTag(collex, tag);
            if (event != null)
            {
                event.setDeleted(true);
                this.eventRepository.delete(event);

                fireEventChangeHandlers(CollectionExerciseEventPublisher.MessageType.EventDeleted, event);
                return event;
            }
            else
            {
                throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                        String.format("Event %s does not exist", tag));
            }

        }
        else
        {
            throw new CTPException(CTPException.Fault.BAD_REQUEST,
                    String.format("Collection exercise %s does not exist", collex.getId()));

        }

    }

    /**
     * Method that is called whenever a change occurs to a collection exercise event
     * @param messageType the type of change
     * @param event the event to which the change occurred
     */
    private void fireEventChangeHandlers(final CollectionExerciseEventPublisher.MessageType messageType,
                                         final Event event) {
        Arrays.stream(this.changeHandlers).forEach(handler -> {
            try {
                handler.handleEventLifecycle(messageType, event);
            } catch (CTPException e) {
                log.error("Failed to handle event change for {} of {} - {} ({})",
                        messageType, event.getId(), e.getMessage(), e.getFault());
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
     * @param collexUuid  the collection exercise to check for scheduled
     * @return true if the mandatory events are all present, false otherwise
     * @throws CTPException if collection exercise not found etc
     */
    @Override
    public boolean isScheduled(UUID collexUuid) throws CTPException {
        Map<String, Event> events = getEvents(collexUuid).stream().collect(
                Collectors.toMap(Event::getTag, Function.identity())
        );

        int numberOfMandatoryEvents = Arrays.stream(Tag.values())
                .filter(Tag::isMandatory).collect(Collectors.toList()).size();

        return Arrays.stream(Tag.values())
                .filter(Tag::isMandatory)
                .map(t -> events.get(t.name()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                .size() >= numberOfMandatoryEvents;

    }

    /**
     * Unschedule a collection exercise event
     * @param event the event to unschedule
     * @throws CTPException thrown if error occurred scheduling event
     */
    @Override
    public void unscheduleEvent(final Event event) throws CTPException {
        try {
            SchedulerConfiguration.unscheduleEvent(this.scheduler, event);
        } catch (SchedulerException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, String.format("Error unscheduling event %s",
                    event.getId()), e);
        }
    }

    /**
     * Schedule a collection exercise event
     * @param event the event to shchedule
     * @throws CTPException thrown if error occurred scheduling event
     */
    @Override
    public void scheduleEvent(final Event event) throws CTPException {
        try {
            SchedulerConfiguration.scheduleEvent(this.scheduler, event);
        } catch (SchedulerException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, String.format("Error scheduling event %s",
                    event.getId()), e);
        }
    }
}
