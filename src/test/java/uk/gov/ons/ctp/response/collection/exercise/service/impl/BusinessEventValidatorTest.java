package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@RunWith(MockitoJUnitRunner.class)
public class BusinessEventValidatorTest {

  private BusinessEventValidator validator;

  private List<Event> mandatoryEvents;

  private List<Event> allEvents;

  @Before
  public void setUp() {
    this.validator = new BusinessEventValidator();
    this.mandatoryEvents = createMandatoryEvents();
    this.allEvents = createAllEvents();
  }

  @Test
  public void testValidMpsEventCreation() {
    long now = System.currentTimeMillis();
    Event mpsEvent = new Event();
    mpsEvent.setTag((EventService.Tag.mps.toString()));
    mpsEvent.setTimestamp(new Timestamp(now + 15000000));
    List<Event> events = new ArrayList<>();
    assertTrue(
        this.validator.validateOnCreate(
            events, mpsEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidGoLiveEventCreation() {
    long now = System.currentTimeMillis();
    Event goLiveEvent = new Event();
    goLiveEvent.setTag((EventService.Tag.mps.toString()));
    goLiveEvent.setTimestamp(new Timestamp(now + 15000000));
    List<Event> events = new ArrayList<>();
    assertTrue(
        this.validator.validateOnCreate(
            events, goLiveEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidGoLiveEventCreation() {
    long now = System.currentTimeMillis();
    Event mpsEvent = new Event();
    mpsEvent.setTag((EventService.Tag.mps.toString()));
    mpsEvent.setTimestamp(new Timestamp(now + 15000000));
    Event goLive = new Event();
    goLive.setTag((EventService.Tag.go_live.toString()));
    goLive.setTimestamp((new Timestamp(now + 11000000)));
    List<Event> events = new ArrayList<>();
    events.add(mpsEvent);
    assertFalse(
        this.validator.validateOnCreate(
            events, goLive, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidReturnByEventCreation() {
    long now = System.currentTimeMillis();
    Event mpsEvent = new Event();
    mpsEvent.setTag((EventService.Tag.mps.toString()));
    mpsEvent.setTimestamp(new Timestamp(now + 15000000));
    Event goLiveEvent = new Event();
    goLiveEvent.setTag((EventService.Tag.go_live.toString()));
    goLiveEvent.setTimestamp((new Timestamp(now + 17000000)));
    List<Event> events = new ArrayList<>();
    events.add(mpsEvent);
    events.add(goLiveEvent);
    Event returnByEvent = new Event();
    returnByEvent.setTag((EventService.Tag.return_by.toString()));
    returnByEvent.setTimestamp((new Timestamp(now + 11000000)));
    assertFalse(
        this.validator.validateOnCreate(
            events, returnByEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidReturnByEventCreation() {
    long now = System.currentTimeMillis();
    Event mpsEvent = new Event();
    mpsEvent.setTag((EventService.Tag.mps.toString()));
    mpsEvent.setTimestamp(new Timestamp(now + 15000000));
    Event goLiveEvent = new Event();
    goLiveEvent.setTag((EventService.Tag.go_live.toString()));
    goLiveEvent.setTimestamp((new Timestamp(now + 17000000)));
    List<Event> events = new ArrayList<>();
    events.add(mpsEvent);
    events.add(goLiveEvent);
    Event returnByEvent = new Event();
    returnByEvent.setTag((EventService.Tag.return_by.toString()));
    returnByEvent.setTimestamp((new Timestamp(now + 19000000)));
    assertTrue(
        this.validator.validateOnCreate(
            events, returnByEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidMpsEventUpdateReadyForLiveState() {
    long now = System.currentTimeMillis();
    Event mpsEvent = new Event();
    mpsEvent.setTag(EventService.Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 1500000));
    assertFalse(
      this.validator.validate(
        this.mandatoryEvents,
        mpsEvent,
        CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE));
  }
  @Test
  public void testInvalidMpsEventUpdateLiveState() {
    long now = System.currentTimeMillis();
    Event mpsEvent = new Event();
    mpsEvent.setTag(EventService.Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 1500000));
    assertFalse(
      this.validator.validate(
        this.mandatoryEvents, mpsEvent, CollectionExerciseDTO.CollectionExerciseState.LIVE));
  }

  @Test
  public void testInvalidGoLiveEventUpdateReadyForLiveState() {
    long now = System.currentTimeMillis();
    Event goLiveEvent = new Event();
    goLiveEvent.setTag(EventService.Tag.go_live.toString());
    goLiveEvent.setTimestamp(new Timestamp(now + 2500000));
    assertFalse(
      this.validator.validate(
        this.mandatoryEvents,
        goLiveEvent,
        CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE));
  }
  @Test
  public void testInvalidGoLiveEventUpdateLiveState() {
    long now = System.currentTimeMillis();
    Event goLiveEvent = new Event();
    goLiveEvent.setTag(EventService.Tag.go_live.toString());
    goLiveEvent.setTimestamp(new Timestamp(now + 2500000));
    assertFalse(
      this.validator.validate(
        this.mandatoryEvents, goLiveEvent, CollectionExerciseDTO.CollectionExerciseState.LIVE));
  }

  @Test
  public void testValidMpsEventUpdate() {
    long now = System.currentTimeMillis();

    Event mpsEvent = new Event();
    mpsEvent.setTag(EventService.Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 1500000));

    assertTrue(
        this.validator.validate(
            this.mandatoryEvents, mpsEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidGoLiveEventUpdate() {
    long now = System.currentTimeMillis();

    Event goLiveEvent = new Event();
    goLiveEvent.setTag(EventService.Tag.go_live.toString());
    goLiveEvent.setTimestamp(new Timestamp(now + 2500000));

    assertTrue(
        this.validator.validate(
            this.mandatoryEvents,
            goLiveEvent,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidReturnByEventUpdate() {
    long now = System.currentTimeMillis();

    Event returnByEvent = new Event();
    returnByEvent.setTag(EventService.Tag.return_by.toString());
    returnByEvent.setTimestamp(new Timestamp(now + 3500000));

    assertTrue(
        this.validator.validate(
            this.mandatoryEvents,
            returnByEvent,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidExerciseEndEventUpdate() {
    long now = System.currentTimeMillis();

    Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(new Timestamp(now + 4500000));

    assertTrue(
        this.validator.validate(
            this.mandatoryEvents,
            exerciseEndEvent,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidMpsEventUpdate() {
    long now = System.currentTimeMillis();

    Event mpsEvent = new Event();
    mpsEvent.setTag(EventService.Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 3500000));

    assertFalse(
        this.validator.validate(
            this.mandatoryEvents, mpsEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidGoLiveEventUpdate() {
    long now = System.currentTimeMillis();

    Event goLiveEvent = new Event();
    goLiveEvent.setTag(EventService.Tag.go_live.toString());
    goLiveEvent.setTimestamp(new Timestamp(now + 3500000));

    assertFalse(
        this.validator.validate(
            this.mandatoryEvents,
            goLiveEvent,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidReturnByEventUpdate() {
    long now = System.currentTimeMillis();

    Event returnByEvent = new Event();
    returnByEvent.setTag(EventService.Tag.return_by.toString());
    returnByEvent.setTimestamp(new Timestamp(now + 4500000));

    assertFalse(
        this.validator.validate(
            this.mandatoryEvents,
            returnByEvent,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidExerciseEndEventUpdate() {
    long now = System.currentTimeMillis();

    Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(new Timestamp(now + 1500000));

    assertFalse(
        this.validator.validate(
            this.mandatoryEvents,
            exerciseEndEvent,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidEventInPastUpdate() {

    List<Event> eventListWithPastMPS = createMandatoryEvents(-1000000);

    long now = System.currentTimeMillis();

    Event mpsEvent = new Event();
    mpsEvent.setTag(EventService.Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 1500000));

    assertFalse(
        this.validator.validate(
            eventListWithPastMPS, mpsEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testUpdateNonExistentEvent() {
    List<Event> eventListWithoutExerciseEnd = createMandatoryEvents();
    eventListWithoutExerciseEnd.remove(eventListWithoutExerciseEnd.size() - 1);

    long now = System.currentTimeMillis();

    Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(EventService.Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(new Timestamp(now));

    assertFalse(
        this.validator.validate(
            eventListWithoutExerciseEnd,
            exerciseEndEvent,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testReferencePeriodIncorrectOrder() {
    long now = System.currentTimeMillis();

    Event referencePeriodStart = new Event();
    referencePeriodStart.setTag(EventService.Tag.ref_period_start.toString());
    referencePeriodStart.setTimestamp(new Timestamp(now - 1000));

    assertFalse(
        this.validator.validate(
            this.allEvents,
            referencePeriodStart,
            CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testReminderBeforeGoLive() {
    long now = System.currentTimeMillis();

    Event reminderEvent = new Event();
    reminderEvent.setTag(EventService.Tag.reminder.toString());
    reminderEvent.setTimestamp(new Timestamp(now));

    assertFalse(
        this.validator.validate(
            this.allEvents, reminderEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testReminderBeforeAfterExerciseEnd() {
    long now = System.currentTimeMillis();

    Event reminderEvent = new Event();
    reminderEvent.setTag(EventService.Tag.reminder.toString());
    reminderEvent.setTimestamp(new Timestamp(now + 5000000));

    assertFalse(
        this.validator.validate(
            this.allEvents, reminderEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
  }

  @Test
  public void testCanUpdateReminderWhenReadyForLive() {
    long now = System.currentTimeMillis();

    Event reminderEvent = new Event();
    reminderEvent.setTag(EventService.Tag.reminder.toString());
    reminderEvent.setTimestamp(new Timestamp(now + 3000000));

    assertTrue(
        this.validator.validate(
            this.allEvents,
            reminderEvent,
            CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void testUpdateNonMandatoryNonReminderWhenReadyForLive() {
    long now = System.currentTimeMillis();

    Event referencePeriodStart = new Event();
    referencePeriodStart.setTag(EventService.Tag.ref_period_start.toString());
    referencePeriodStart.setTimestamp(new Timestamp(now - 20000));

    assertFalse(
        this.validator.validate(
            this.allEvents,
            referencePeriodStart,
            CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void testUpdateMandatoryEventWhenReadyForLive() {
    long now = System.currentTimeMillis();

    Event mpsEvent = new Event();
    mpsEvent.setTag(EventService.Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 1000000));

    assertFalse(
        this.validator.validate(
            this.allEvents,
            mpsEvent,
            CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void testCantUpdatePastReminder() {
    List<Event> events = createMandatoryEvents(-230000);
    List<Event> nonMandatoryEventsWithPastReminder = createNonMandatoryEvents(-230000);

    events.addAll(nonMandatoryEventsWithPastReminder);

    long now = System.currentTimeMillis();

    Event reminderEvent = new Event();
    reminderEvent.setTag(EventService.Tag.reminder.toString());
    reminderEvent.setTimestamp(new Timestamp(now));

    assertFalse(
        this.validator.validate(
            events, reminderEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED));
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

    Event reminder = new Event();
    reminder.setTag(EventService.Tag.reminder.toString());
    reminder.setTimestamp(new Timestamp(now + 3000000 + offset));
    nonMandatoryEvents.add(reminder);

    Event reminder2 = new Event();
    reminder2.setTag(EventService.Tag.reminder2.toString());
    reminder2.setTimestamp(new Timestamp(now + 3000000 + offset));
    nonMandatoryEvents.add(reminder2);

    Event reminder3 = new Event();
    reminder3.setTag(EventService.Tag.reminder3.toString());
    reminder3.setTimestamp(new Timestamp(now + 3000000 + offset));
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
