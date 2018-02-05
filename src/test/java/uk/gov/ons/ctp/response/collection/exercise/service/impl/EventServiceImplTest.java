package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Class containing tests for EventServiceImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private CollectionExerciseService collectionExerciseService;

    @InjectMocks
    private EventServiceImpl eventService;

    /**
     * Given collection excercise does not exist
     * When event is created
     * Then exception is thrown
     */
    @Test
    public void givenCollectionExcerciseDoesNotExistWhenEventIsCreatedThenExceptionIsThrown() {
        EventDTO eventDto = new EventDTO();
        UUID collexUuid = UUID.randomUUID();
        eventDto.setCollectionExerciseId(collexUuid);

        try {
            eventService.createEvent(eventDto);

            fail("Created event with non-existent collection exercise");
        } catch (CTPException e) {
            // Expected 404
            assertEquals(CTPException.Fault.RESOURCE_NOT_FOUND, e.getFault());
        }
    }

    /**
     * Given event already exists
     * When event is created
     * Then exception is thrown
     */
    @Test
    public void givenEventAlreadyExistsWhenEventIsCreatedThenExceptionIsThrown() {
        String tag = EventService.Tag.mps.name();
        EventDTO eventDto = new EventDTO();
        CollectionExercise collex = new CollectionExercise();
        UUID collexUuid = UUID.randomUUID();
        eventDto.setCollectionExerciseId(collexUuid);
        eventDto.setTag(tag);
        collex.setId(collexUuid);

        when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
        when(eventRepository.findOneByCollectionExerciseAndTag(collex, tag)).thenReturn(new Event());

        try {
            eventService.createEvent(eventDto);

            fail("Created event with non-existent collection exercise");
        } catch (CTPException e) {
            // Expected 409
            assertEquals(CTPException.Fault.RESOURCE_VERSION_CONFLICT, e.getFault());
        }
    }

    private static Event createEvent(EventService.Tag tag){
        Timestamp eventTime = new Timestamp(new Date().getTime());
        Event event = new Event();
        event.setTimestamp(eventTime);
        event.setTag(tag.name());

        return event;
    }

    private List<Event> createEventList(EventService.Tag... tags){
        return Arrays.stream(tags)
                .map(EventServiceImplTest::createEvent)
                .collect(Collectors.toList());
    }

    @Test
    public void givenNoEventsWhenScheduledIsCheckedThenFalse() throws CTPException {
        UUID collexUuid = UUID.randomUUID();
        when(eventRepository.findByCollectionExerciseId(collexUuid)).thenReturn(new ArrayList<>());

        boolean scheduled = this.eventService.isScheduled(collexUuid);

        assertFalse(scheduled);
    }

    @Test
    public void givenSomeEventsWhenScheduledIsCheckedThenFalse() throws CTPException {
        UUID collexUuid = UUID.randomUUID();
        List<Event> events = createEventList(EventService.Tag.mps, EventService.Tag.exercise_end);
        when(eventRepository.findByCollectionExerciseId(collexUuid)).thenReturn(events);

        boolean scheduled = this.eventService.isScheduled(collexUuid);

        assertFalse(scheduled);
    }

    @Test
    public void givenAllEventsWhenScheduledIsCheckedThenTrue() throws CTPException {
        UUID collexUuid = UUID.randomUUID();
        List<Event> events = createEventList(EventService.Tag.values());
        when(eventRepository.findByCollectionExerciseId(collexUuid)).thenReturn(events);

        boolean scheduled = this.eventService.isScheduled(collexUuid);

        assertTrue(scheduled);
    }
}
