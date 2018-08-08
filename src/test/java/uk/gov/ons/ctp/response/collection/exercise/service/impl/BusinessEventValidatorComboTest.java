package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class BusinessEventValidatorComboTest {

        private EventValidator validator;
        private final Event mpsEvent;
        private final Event goLiveEvent;
        private final Event returnByEvent;
        private final Event exerciseEndEvent;

    public BusinessEventValidatorComboTest(Event mpsEvent, Event goLiveEvent, Event returnByEvent, Event exerciseEndEvent) {
        this.mpsEvent = mpsEvent;
        this.goLiveEvent = goLiveEvent;
        this.returnByEvent = returnByEvent;
        this.exerciseEndEvent = exerciseEndEvent;
    }

    @Before
    public void setUp() {
        this.validator = new BusinessEventValidator();
    }

    @Parameters
    public static Collection<Object[]> existingEvents() {
        Event mpsEvent = getMpsEvent();
        Event goLiveEvent = getGoLiveEvent();
        Event returnByEvent = getReturnByEvent();
        Event exerciseEnd = getExerciseEndEvent();
        return Arrays.asList(new Object[][] {
                { null, null, null, null },
                { null, null, null, exerciseEnd },
                { null, null, returnByEvent, null },
                { null , null, returnByEvent, exerciseEnd},
                { null, goLiveEvent, null, null },
                { null, goLiveEvent, null, exerciseEnd},
                { null, goLiveEvent, returnByEvent, null},
                { null, goLiveEvent, returnByEvent, exerciseEnd },
                { mpsEvent, null, null, null },
                { mpsEvent, null, null, exerciseEnd},
                { mpsEvent, null, returnByEvent, null },
                { mpsEvent, null, returnByEvent, exerciseEnd},
                { mpsEvent, goLiveEvent, null, null },
                { mpsEvent, goLiveEvent, null, exerciseEnd},
                { mpsEvent, goLiveEvent, returnByEvent, null},
                { mpsEvent, goLiveEvent, returnByEvent, exerciseEnd}

        });
    }

    @Test
    public void testValidMpsEventCreation() {
        List<Event> events = getExistingEvents();
        long now = System.currentTimeMillis();
        Event newMpsEvent = new Event();
        newMpsEvent.setTag((EventService.Tag.mps.toString()));
        newMpsEvent.setTimestamp(new Timestamp(now + 12000000));
        assertTrue(
                this.validator.validateOnCreate(
                        events, newMpsEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
    }

    @Test
    public void testValidGoLiveEventCreation() {
        List<Event> events = getExistingEvents();
        long now = System.currentTimeMillis();
        Event newGoLiveEvent = new Event();
        newGoLiveEvent.setTag((EventService.Tag.go_live.toString()));
        newGoLiveEvent.setTimestamp(new Timestamp(now + 16500000));
        assertTrue(
                this.validator.validateOnCreate(
                        events, newGoLiveEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
    }

    @Test
    public void testValidReturnByEventCreation() {
        List<Event> events = getExistingEvents();
        long now = System.currentTimeMillis();
        Event newReturnByEvent = new Event();
        newReturnByEvent.setTag((EventService.Tag.return_by.toString()));
        newReturnByEvent.setTimestamp(new Timestamp(now + 17500000));
        assertTrue(
                this.validator.validateOnCreate(
                        events, newReturnByEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
    }

    @Test
    public void testValidExerciseEndEventCreation() {
        List<Event> events = getExistingEvents();
        long now = System.currentTimeMillis();
        Event newExerciseEndEvent = new Event();
        newExerciseEndEvent.setTag((EventService.Tag.exercise_end.toString()));
        newExerciseEndEvent.setTimestamp(new Timestamp(now + 19000000));
        assertTrue(
                this.validator.validateOnCreate(
                        events, newExerciseEndEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
    }

    private List<Event> getExistingEvents() {
        List<Event> existingEvents = new ArrayList<>();
        if (mpsEvent != null) {
            existingEvents.add(mpsEvent);
        }
        if (goLiveEvent != null) {
            existingEvents.add(goLiveEvent);
        }
        if (returnByEvent != null) {
            existingEvents.add(returnByEvent);
        }
        if (exerciseEndEvent != null) {
            existingEvents.add(exerciseEndEvent);
        }
        return existingEvents;
    }

    private static Event getMpsEvent() {
        long now = System.currentTimeMillis();
        Event mpsEvent = new Event();
        mpsEvent.setTag((EventService.Tag.mps.toString()));
        mpsEvent.setTimestamp(new Timestamp(now + 15000000));
        return mpsEvent;
    }

    private static Event getGoLiveEvent() {
        long now = System.currentTimeMillis();
        Event event = new Event();
        event.setTag((EventService.Tag.go_live.toString()));
        event.setTimestamp(new Timestamp(now + 16000000));
        return event;
    }

    private static Event getReturnByEvent() {
        long now = System.currentTimeMillis();
        Event event = new Event();
        event.setTag((EventService.Tag.return_by.toString()));
        event.setTimestamp(new Timestamp(now + 17000000));
        return event;
    }

    private static Event getExerciseEndEvent() {
        long now = System.currentTimeMillis();
        Event event = new Event();
        event.setTag((EventService.Tag.exercise_end.toString()));
        event.setTimestamp(new Timestamp(now + 18000000));
        return event;
    }
}
