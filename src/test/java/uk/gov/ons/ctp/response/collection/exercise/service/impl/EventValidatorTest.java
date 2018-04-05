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
        this.events = createMandatoryEvents();

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

        List<Event> eventListWithPastMPS = createMandatoryEvents(-1000000);

        long now = System.currentTimeMillis();

        Event mpsEvent = new Event();
        mpsEvent.setTag(EventService.Tag.mps.toString());
        mpsEvent.setTimestamp(new Timestamp(now+1500000));

        assertFalse(this.validator.validate(eventListWithPastMPS, mpsEvent));
    }

    @Test
    public void testUpdateNonExistentEvent() {
        List<Event> eventListWithoutExerciseEnd = createMandatoryEvents();
        eventListWithoutExerciseEnd.remove(eventListWithoutExerciseEnd.size()-1);

        long now = System.currentTimeMillis();

        Event exerciseEndEvent = new Event();
        exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
        exerciseEndEvent.setTimestamp(new Timestamp(now));

        assertFalse(this.validator.validate(eventListWithoutExerciseEnd, exerciseEndEvent));
    }

    @Test
    public void testReferencePeriodIncorrectOrder() {
        List<Event> allEvents = createAllEvents();

        long now = System.currentTimeMillis();

        Event referencePeriodStart = new Event();
        referencePeriodStart.setTag(EventService.Tag.ref_period_start.toString());
        referencePeriodStart.setTimestamp(new Timestamp(now-1000));

        assertFalse(this.validator.validate(allEvents, referencePeriodStart));
    }

    @Test
    public void testReminderBeforeGoLive() {
        List<Event> allEvents = createAllEvents();

        long now = System.currentTimeMillis();

        Event reminder1Event = new Event();
        reminder1Event.setTag(EventService.Tag.reminder_1.toString());
        reminder1Event.setTimestamp(new Timestamp(now));

        assertFalse(this.validator.validate(allEvents, reminder1Event));
    }

    @Test
    public void testReminderBeforeAfterExerciseEnd() {
        List<Event> allEvents = createAllEvents();

        long now = System.currentTimeMillis();

        Event reminder1Event = new Event();
        reminder1Event.setTag(EventService.Tag.reminder_1.toString());
        reminder1Event.setTimestamp(new Timestamp(now + 5000000));

        assertFalse(this.validator.validate(allEvents, reminder1Event));
    }

    @Test
    public void testCantUpdatePastReminder() {
        List<Event> events = createMandatoryEvents(-230000);
        List<Event> nonMandatoryEventsWithPastReminder = createNonMandatoryEvents(-230000);

        events.addAll(nonMandatoryEventsWithPastReminder);

        long now = System.currentTimeMillis();

        Event reminder1Event = new Event();
        reminder1Event.setTag(EventService.Tag.reminder_1.toString());
        reminder1Event.setTimestamp(new Timestamp(now));

        assertFalse(this.validator.validate(events, reminder1Event));
    }

    private List<Event> createAllEvents() {
        List<Event> events = createMandatoryEvents();
        events.addAll(createNonMandatoryEvents());
        return events;
    }

    private List<Event> createNonMandatoryEvents() {
        return createNonMandatoryEvents(0);
    }

    private List<Event> createNonMandatoryEvents(final long offset) {
        List<Event> nonMandatoryEvents = new ArrayList<>();

        long now = System.currentTimeMillis();

        Event reminder1 = new Event();
        reminder1.setTag(EventService.Tag.reminder_1.toString());
        reminder1.setTimestamp(new Timestamp(now + 220000 + offset));
        nonMandatoryEvents.add(reminder1);

        Event reminder2 = new Event();
        reminder2.setTag(EventService.Tag.reminder_2.toString());
        reminder2.setTimestamp(new Timestamp(now + 240000 + offset));
        nonMandatoryEvents.add(reminder2);

        Event reminder3 = new Event();
        reminder3.setTag(EventService.Tag.reminder_3.toString());
        reminder3.setTimestamp(new Timestamp(now + 320000 + offset));
        nonMandatoryEvents.add(reminder2);

        Event refPeriodStart = new Event();
        refPeriodStart.setTag(EventService.Tag.ref_period_start.toString());
        refPeriodStart.setTimestamp(new Timestamp(now - 20000 + offset));
        nonMandatoryEvents.add(refPeriodStart);

        Event refPeriodEnd = new Event();
        refPeriodEnd.setTag(EventService.Tag.ref_period_end.toString());
        refPeriodEnd.setTimestamp(new Timestamp(now - 10000 + offset));
        nonMandatoryEvents.add(refPeriodEnd);

        return nonMandatoryEvents;
    }

    private List<Event> createMandatoryEvents() {
        return createMandatoryEvents(0);
    }

    private List<Event> createMandatoryEvents(final long offset) {
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
