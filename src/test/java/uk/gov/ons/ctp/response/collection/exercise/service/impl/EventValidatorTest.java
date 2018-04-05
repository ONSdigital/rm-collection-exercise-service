package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class EventValidatorTest {

    private EventValidator validator;

    private List<Event> events;

    @Before
    public void setUp() {
        this.validator = new EventValidator();
        this.events = createEvents();

    }

    @Test
    public void testValidMpsEventUpdate() {
        long now = System.currentTimeMillis();

        Event mpsEvent = new Event();
        mpsEvent.setTag(EventService.Tag.mps.toString());
        mpsEvent.setTimestamp(new Timestamp(now + 1500000));

        assertTrue(this.validator.validate(this.events, mpsEvent));
    }

    @Test
    public void testValidGoLiveEventUpdate() {
        long now = System.currentTimeMillis();

        Event goLiveEvent = new Event();
        goLiveEvent.setTag(EventService.Tag.go_live.toString());
        goLiveEvent.setTimestamp(new Timestamp(now + 2500000));

        assertTrue(this.validator.validate(this.events, goLiveEvent));
    }

    @Test
    public void testValidReturnByEventUpdate() {
        long now = System.currentTimeMillis();

        Event returnByEvent = new Event();
        returnByEvent.setTag(EventService.Tag.return_by.toString());
        returnByEvent.setTimestamp(new Timestamp(now + 3500000));

        assertTrue(this.validator.validate(this.events, returnByEvent));
    }

    @Test
    public void testValidExerciseEndEventUpdate() {
        long now = System.currentTimeMillis();

        Event exerciseEndEvent = new Event();
        exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
        exerciseEndEvent.setTimestamp(new Timestamp(now + 4500000));

        assertTrue(this.validator.validate(this.events, exerciseEndEvent));
    }

    @Test
    public void testInvalidMpsEventUpdate() {
        long now = System.currentTimeMillis();

        Event mpsEvent = new Event();
        mpsEvent.setTag(EventService.Tag.mps.toString());
        mpsEvent.setTimestamp(new Timestamp(now + 3500000));

        assertFalse(this.validator.validate(this.events, mpsEvent));
    }

    @Test
    public void testInvalidGoLiveEventUpdate() {
        long now = System.currentTimeMillis();

        Event goLiveEvent = new Event();
        goLiveEvent.setTag(EventService.Tag.go_live.toString());
        goLiveEvent.setTimestamp(new Timestamp(now + 3500000));

        assertFalse(this.validator.validate(this.events, goLiveEvent));
    }

    @Test
    public void testInvalidReturnByEventUpdate() {
        long now = System.currentTimeMillis();

        Event returnByEvent = new Event();
        returnByEvent.setTag(EventService.Tag.return_by.toString());
        returnByEvent.setTimestamp(new Timestamp(now + 4500000));

        assertFalse(this.validator.validate(this.events, returnByEvent));
    }

    @Test
    public void testInvalidExerciseEndEventUpdate() {
        long now = System.currentTimeMillis();

        Event exerciseEndEvent = new Event();
        exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
        exerciseEndEvent.setTimestamp(new Timestamp(now + 1500000));

        assertFalse(this.validator.validate(this.events, exerciseEndEvent));
    }


    @Test
    public void testInvalidEventInPastUpdate() {

        List<Event> eventListWithPastMPS = createEvents(-1000000);

        long now = System.currentTimeMillis();

        Event mpsEvent = new Event();
        mpsEvent.setTag(EventService.Tag.mps.toString());
        mpsEvent.setTimestamp(new Timestamp(now+1500000));

        assertFalse(this.validator.validate(eventListWithPastMPS, mpsEvent));
    }

    @Test
    public void testUpdateNonExistentEvent() {
        List<Event> eventListWithoutExerciseEnd = createEvents();
        eventListWithoutExerciseEnd.remove(eventListWithoutExerciseEnd.size()-1);

        long now = System.currentTimeMillis();

        Event exerciseEndEvent = new Event();
        exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
        exerciseEndEvent.setTimestamp(new Timestamp(now));

        assertFalse(this.validator.validate(eventListWithoutExerciseEnd, exerciseEndEvent));
    }


    private List<Event> createEvents() {
        return createEvents(0);
    }

    private List<Event> createEvents(final long offset) {
        List<Event> eventList = new ArrayList<>();

        long now = System.currentTimeMillis();

        Event mpsEvent = new Event();
        mpsEvent.setTag(EventService.Tag.mps.toString());
        mpsEvent.setTimestamp(new Timestamp(now + 1000000 + offset));
        eventList.add(mpsEvent);


        Event goLiveEvent = new Event();
        goLiveEvent.setTag(EventService.Tag.go_live.toString());
        goLiveEvent.setTimestamp(new Timestamp(now + 2000000 + offset));
        eventList.add(goLiveEvent);

        Event returnByEvent = new Event();
        returnByEvent.setTag(EventService.Tag.return_by.toString());
        returnByEvent.setTimestamp(new Timestamp(now + 3000000 + offset));
        eventList.add(returnByEvent);

        Event exerciseEndEvent = new Event();
        exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
        exerciseEndEvent.setTimestamp(new Timestamp(now + 4000000 + offset));
        eventList.add(exerciseEndEvent);

        return eventList;
    }
}
