package uk.gov.ons.ctp.response.collection.exercise.service.impl.change;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.SchedulerException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * Test class for ScheduleEvent
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduleEventTests {

    /**
     * Test timestamp to use
     */
    private static final Date EVENT_TIMESTAMP = new Date();
    /**
     * Random collection exercise id
     */
    private static final UUID COLLECTION_EXERCISE_ID = UUID.randomUUID();
    /**
     * Random collection exercise event id
     */
    private static final UUID EVENT_ID = UUID.randomUUID();
    /**
     * Scheduler mock
     */
    @Mock
    private EventService eventService;
    /**
     * Schedule event object under test
     */
    @InjectMocks
    private ScheduleEvent scheduleEvent;
    /**
     * Test event
     */
    private Event event;

    /**
     * Setup method - creates an Event for testing
     */
    @Before
    public void setUp() {
        Event newEvent = new Event();

        newEvent.setTimestamp(new Timestamp(EVENT_TIMESTAMP.getTime()));
        newEvent.setTag(EventService.Tag.go_live.name());
        newEvent.setId(EVENT_ID);

        CollectionExercise collex = new CollectionExercise();
        collex.setId(COLLECTION_EXERCISE_ID);

        newEvent.setCollectionExercise(collex);

        this.event = newEvent;
    }

    /**
     * Given all fields
     * When created
     * Then schedule event
     *
     * @throws CTPException thrown if error occurs
     * @throws SchedulerException thrown if error occurs
     */
    @Test
    public void givenAllFieldsWhenCreatedThenScheduleEvent() throws CTPException, SchedulerException {
        this.scheduleEvent.handleEventLifecycle(CollectionExerciseEventPublisher.MessageType.EventCreated, this.event);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(this.eventService).scheduleEvent(eventCaptor.capture());

        assertEquals(this.event, eventCaptor.getValue());
    }

    /**
     * Given all fields
     * When updated
     * Then schedule event
     *
     * @throws CTPException thrown if error occurs
     * @throws SchedulerException thrown if error occurs
     */
    @Test
    public void givenAllFieldsWhenUpdatedThenScheduleEvent() throws CTPException, SchedulerException {
        this.scheduleEvent.handleEventLifecycle(CollectionExerciseEventPublisher.MessageType.EventUpdated, this.event);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(this.eventService).scheduleEvent(eventCaptor.capture());

        assertEquals(this.event, eventCaptor.getValue());
    }

    /**
     * Given all fields
     * When deleted
     * Then unschedule event
     *
     * @throws CTPException thrown if error occurs
     * @throws SchedulerException thrown if error occurs
     */
    @Test
    public void givenAllFieldsWhenDeletedThenUnscheduleEvent() throws CTPException, SchedulerException {
        this.scheduleEvent.handleEventLifecycle(CollectionExerciseEventPublisher.MessageType.EventDeleted, this.event);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(this.eventService).unscheduleEvent(eventCaptor.capture());

        assertEquals(this.event, eventCaptor.getValue());
    }
}
