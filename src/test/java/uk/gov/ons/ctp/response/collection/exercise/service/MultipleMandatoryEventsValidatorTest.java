package uk.gov.ons.ctp.response.collection.exercise.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.validator.EventDateOrderChecker;
import uk.gov.ons.ctp.response.collection.exercise.service.validator.MandatoryEventValidator;

@RunWith(Parameterized.class)
public class MultipleMandatoryEventsValidatorTest {

  private EventValidator validator;
  private final Event mpsEvent;
  private final Event goLiveEvent;
  private final Event returnByEvent;
  private final Event exerciseEndEvent;

  public MultipleMandatoryEventsValidatorTest(
      Event mpsEvent, Event goLiveEvent, Event returnByEvent, Event exerciseEndEvent) {
    this.mpsEvent = mpsEvent;
    this.goLiveEvent = goLiveEvent;
    this.returnByEvent = returnByEvent;
    this.exerciseEndEvent = exerciseEndEvent;
  }

  @Before
  public void setUp() {
    final EventDateOrderChecker eventDateOrderChecker = new EventDateOrderChecker();
    validator = new MandatoryEventValidator(eventDateOrderChecker);
  }

  @Parameters
  public static Collection<Object[]> existingEvents() {
    Event mpsEvent = getMpsEvent();
    Event goLiveEvent = getGoLiveEvent();
    Event returnByEvent = getReturnByEvent();
    Event exerciseEnd = getExerciseEndEvent();
    return Arrays.asList(
        new Object[][] {
          {null, null, null, null},
          {null, null, null, exerciseEnd},
          {null, null, returnByEvent, null},
          {null, null, returnByEvent, exerciseEnd},
          {null, goLiveEvent, null, null},
          {null, goLiveEvent, null, exerciseEnd},
          {null, goLiveEvent, returnByEvent, null},
          {null, goLiveEvent, returnByEvent, exerciseEnd},
          {mpsEvent, null, null, null},
          {mpsEvent, null, null, exerciseEnd},
          {mpsEvent, null, returnByEvent, null},
          {mpsEvent, null, returnByEvent, exerciseEnd},
          {mpsEvent, goLiveEvent, null, null},
          {mpsEvent, goLiveEvent, null, exerciseEnd},
          {mpsEvent, goLiveEvent, returnByEvent, null},
          {mpsEvent, goLiveEvent, returnByEvent, exerciseEnd}
        });
  }

  @Test
  public void testValidMpsEventCreation() throws CTPException {
    List<Event> events = getExistingEvents();
    Instant timestamp = Instant.now().plus(2, ChronoUnit.DAYS);
    Event newMpsEvent = new Event();
    newMpsEvent.setTag((EventService.Tag.mps.toString()));
    newMpsEvent.setTimestamp(Timestamp.from(timestamp));
    validator.validate(events, newMpsEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED);
  }

  @Test
  public void testValidGoLiveEventCreation() throws CTPException {
    List<Event> events = getExistingEvents();
    Instant timestamp = Instant.now().plus(4, ChronoUnit.DAYS);
    Event newGoLiveEvent = new Event();
    newGoLiveEvent.setTag((EventService.Tag.go_live.toString()));
    newGoLiveEvent.setTimestamp(Timestamp.from(timestamp));
    validator.validate(
        events, newGoLiveEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED);
  }

  @Test
  public void testValidReturnByEventCreation() throws CTPException {
    List<Event> events = getExistingEvents();
    Instant timestamp = Instant.now().plus(6, ChronoUnit.DAYS);
    Event newReturnByEvent = new Event();
    newReturnByEvent.setTag((EventService.Tag.return_by.toString()));
    newReturnByEvent.setTimestamp(Timestamp.from(timestamp));
    validator.validate(
        events, newReturnByEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED);
  }

  @Test
  public void testValidExerciseEndEventCreation() throws CTPException {
    List<Event> events = getExistingEvents();
    Instant timestamp = Instant.now().plus(8, ChronoUnit.DAYS);
    Event newExerciseEndEvent = new Event();
    newExerciseEndEvent.setTag((EventService.Tag.exercise_end.toString()));
    newExerciseEndEvent.setTimestamp(Timestamp.from(timestamp));
    validator.validate(
        events, newExerciseEndEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED);
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
    Instant timestamp = Instant.now().plus(2, ChronoUnit.DAYS);
    Event mpsEvent = new Event();
    mpsEvent.setTag((EventService.Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(timestamp));
    return mpsEvent;
  }

  private static Event getGoLiveEvent() {
    Instant timestamp = Instant.now().plus(4, ChronoUnit.DAYS);
    Event event = new Event();
    event.setTag((EventService.Tag.go_live.toString()));
    event.setTimestamp(Timestamp.from(timestamp));
    return event;
  }

  private static Event getReturnByEvent() {
    Instant timestamp = Instant.now().plus(6, ChronoUnit.DAYS);
    Event event = new Event();
    event.setTag((EventService.Tag.return_by.toString()));
    event.setTimestamp(Timestamp.from(timestamp));
    return event;
  }

  private static Event getExerciseEndEvent() {
    Instant timestamp = Instant.now().plus(8, ChronoUnit.DAYS);
    Event event = new Event();
    event.setTag((EventService.Tag.exercise_end.toString()));
    event.setTimestamp(Timestamp.from(timestamp));
    return event;
  }
}
