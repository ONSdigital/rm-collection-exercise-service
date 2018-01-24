package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CollectionExerciseEventPublisher eventPublisher;

    @Override
    public Event createEvent(EventDTO eventDto) throws CTPException {
        CollectionExercise collex =
                this.collectionExerciseService.findCollectionExercise(eventDto.getCollectionExerciseId());

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

                eventPublisher.publishCollectionExerciseEvent(
                        CollectionExerciseEventPublisher.MessageType.EventCreated,
                        EventService.createEventDTOFromEvent(event));

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

                this.eventRepository.save(event);

                eventPublisher.publishCollectionExerciseEvent(
                        CollectionExerciseEventPublisher.MessageType.EventUpdated,
                        EventService.createEventDTOFromEvent(event));
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
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, String.format("Event %s does not exist", event));
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

                eventPublisher.publishCollectionExerciseEvent(
                        CollectionExerciseEventPublisher.MessageType.EventDeleted,
                        EventService.createEventDTOFromEvent(event));
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

    @Override
    public List<Event> getOutstandingEvents() {
        return this.eventRepository.findByMessageSentNotNull();
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

}
