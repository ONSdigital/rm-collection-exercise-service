package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    @Autowired
    private EventRepository eventRepository;

    @Override
    public Event createEvent(EventDTO eventDto) throws CTPException {
        CollectionExercise collex = this.collectionExerciseService.findCollectionExercise(eventDto.getCollectionExerciseId());

        Event event = new Event();

        event.setCollectionExercise(collex);
        event.setTag(eventDto.getTag());
        event.setId(UUID.randomUUID());
        event.setTimestamp(new Timestamp(eventDto.getTimestamp().getTime()));
        event.setCreated(new Timestamp(new Date().getTime()));

        eventRepository.save(event);

        return event;
    }
}
