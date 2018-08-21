package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

@RunWith(MockitoJUnitRunner.class)
public class BusinessEventValidatorTest {

  private static final int DAYS_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
  private BusinessEventValidator validator;

  private List<Event> mandatoryEvents;

  private List<Event> allEvents;

  @Before
  public void setUp() {
    validator = new BusinessEventValidator();
    mandatoryEvents = createMandatoryEvents();
    allEvents = createAllEvents();
  }

  @Test
  public void testMandatoryEventShouldNotBeInThePast() {
    Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    assertFalse(validator.validate(events, mpsEvent, CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidMpsEventCreation() {
    Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    assertTrue(validator.validate(events, mpsEvent, CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidGoLiveEventCreation() {
    Event goLiveEvent = new Event();
    goLiveEvent.setTag((Tag.mps.toString()));
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    assertTrue(this.validator.validate(events, goLiveEvent, CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidReturnByEventCreation() {
    Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    Event goLiveEvent = new Event();
    goLiveEvent.setTag((Tag.go_live.toString()));
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    events.add(mpsEvent);
    events.add(goLiveEvent);
    Event returnByEvent = new Event();
    returnByEvent.setTag((Tag.return_by.toString()));
    returnByEvent.setTimestamp(Timestamp.from(Instant.now().plus(6, ChronoUnit.DAYS)));
    assertTrue(this.validator.validate(events, returnByEvent, CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidGoLiveEventCreation() {
    Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(10, ChronoUnit.DAYS)));
    Event goLive = new Event();
    goLive.setTag((Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    events.add(mpsEvent);
    assertFalse(this.validator.validate(events, goLive, CollectionExerciseState.CREATED));
  }

  @Test
  public void testInvalidReturnByEventCreation() {
    Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    Event goLiveEvent = new Event();
    goLiveEvent.setTag((Tag.go_live.toString()));
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    events.add(mpsEvent);
    events.add(goLiveEvent);
    Event returnByEvent = new Event();
    returnByEvent.setTag((Tag.return_by.toString()));
    returnByEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    assertFalse(validator.validate(events, returnByEvent, CollectionExerciseState.CREATED));
  }

  @Test
  public void testMandatoryEventNot24HoursApart() {
    Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    Event goLive = new Event();
    goLive.setTag((Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    events.add(mpsEvent);
    assertFalse(validator.validate(events, goLive, CollectionExerciseState.CREATED));
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsLive() {
    final List<Event> events = createMandatoryEvents(DAYS_IN_MILLISECONDS);

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    assertFalse(validator.validate(events, mpsEvent, CollectionExerciseState.LIVE));
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsValidated() {
    final List<Event> events = createMandatoryEvents(DAYS_IN_MILLISECONDS);

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    assertFalse(validator.validate(events, mpsEvent, CollectionExerciseState.VALIDATED));
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsExecutionStarted() {
    final List<Event> events = createMandatoryEvents(DAYS_IN_MILLISECONDS);

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    assertFalse(validator.validate(events, mpsEvent, CollectionExerciseState.EXECUTION_STARTED));
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsReadyForLive() {
    final List<Event> events = createMandatoryEvents(DAYS_IN_MILLISECONDS);

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    assertFalse(validator.validate(events, mpsEvent, CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsExecuted() {
    final List<Event> events = createMandatoryEvents(DAYS_IN_MILLISECONDS);

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    assertFalse(validator.validate(events, mpsEvent, CollectionExerciseState.EXECUTED));
  }

  @Test
  public void testCanUpdateReminderWhenReadyForLive() {
    long now = System.currentTimeMillis();

    Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder.toString());
    reminderEvent.setTimestamp(new Timestamp(now + 4 * DAYS_IN_MILLISECONDS));

    assertTrue(
        validator.validate(allEvents, reminderEvent, CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void testCanUpdateReminderWhenLive() {
    long now = System.currentTimeMillis();

    Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder.toString());
    reminderEvent.setTimestamp(new Timestamp(now + 4 * DAYS_IN_MILLISECONDS));

    assertTrue(validator.validate(allEvents, reminderEvent, CollectionExerciseState.LIVE));
  }

  @Test
  public void testUpdateNonMandatoryNonReminderWhenReadyForLive() {
    long now = System.currentTimeMillis();

    final Event referencePeriodStart = new Event();
    referencePeriodStart.setTag(Tag.ref_period_end.toString());
    referencePeriodStart.setTimestamp(new Timestamp(now + 8 * DAYS_IN_MILLISECONDS));

    assertTrue(
        validator.validate(
            allEvents, referencePeriodStart, CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void testUpdateNonMandatoryNonReminderWhenLiveValid() {
    long now = System.currentTimeMillis();

    final Event referencePeriodStart = new Event();
    referencePeriodStart.setTag(Tag.ref_period_start.toString());
    referencePeriodStart.setTimestamp(new Timestamp(now - 20000));

    assertTrue(validator.validate(allEvents, referencePeriodStart, CollectionExerciseState.LIVE));
  }

  @Test
  public void testNewReminderEventShouldNotBeInThePast() {
    Event reminder = new Event();
    reminder.setTag((Tag.reminder.toString()));
    reminder.setTimestamp(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));
    List<Event> events = new ArrayList<>();
    assertFalse(validator.validate(events, reminder, CollectionExerciseState.CREATED));
  }

  @Test
  public void testCantUpdatePastReminder() {
    Event reminder = new Event();
    reminder.setTag((Tag.reminder.toString()));
    reminder.setTimestamp(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));

    Event newReminder = new Event();
    newReminder.setTag((Tag.reminder.toString()));
    newReminder.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final List<Event> events = Collections.singletonList(reminder);

    assertFalse(validator.validate(events, newReminder, CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidReminderEventCreation() {
    final Event goLive = new Event();
    goLive.setTag((Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now()));

    final Event reminder = new Event();
    reminder.setTag((Tag.reminder.toString()));
    reminder.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final Event exerciseEnd = new Event();
    exerciseEnd.setTag((Tag.exercise_end.toString()));
    exerciseEnd.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(goLive, exerciseEnd);

    assertTrue(validator.validate(events, reminder, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReminderLessThan24hrBeforeExerciseEndInvalid() {
    long now = System.currentTimeMillis();

    final Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder3.toString());
    reminderEvent.setTimestamp(
        new Timestamp(now + 3 * DAYS_IN_MILLISECONDS + DAYS_IN_MILLISECONDS / 2));

    final Event exerciseEnd = new Event();
    exerciseEnd.setTag((Tag.exercise_end.toString()));
    exerciseEnd.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Collections.singletonList(exerciseEnd);

    assertFalse(validator.validate(events, reminderEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testReminderLessThan24hrsAfterGoliveInvalid() {
    long now = System.currentTimeMillis();

    final Event goLive = new Event();
    goLive.setTag((Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now()));
    final List<Event> events = Collections.singletonList(goLive);

    final Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder.toString());
    reminderEvent.setTimestamp(new Timestamp(now + DAYS_IN_MILLISECONDS / 2));

    assertFalse(validator.validate(events, reminderEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testReminder2WrongOrderEventCreation() {
    final Event reminder = new Event();
    reminder.setTag((Tag.reminder.toString()));
    reminder.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final Event reminder2 = new Event();
    reminder2.setTag((Tag.reminder2.toString()));
    reminder2.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    final List<Event> events = new ArrayList<>();
    events.add(reminder);
    assertFalse(validator.validate(events, reminder2, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReminderEventNot24HoursApartIsInvalid() {
    final Event reminder = new Event();
    reminder.setTag((Tag.reminder.toString()));
    reminder.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    final Event reminder2 = new Event();
    reminder2.setTag((Tag.reminder2.toString()));
    reminder2.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    final List<Event> events = new ArrayList<>();
    events.add(reminder);
    assertFalse(validator.validate(events, reminder2, CollectionExerciseState.CREATED));
  }

  @Test
  public void testEmploymentDateCanBeSetInThePast() {
    final Event employment = new Event();
    employment.setTag((Tag.employment.toString()));
    employment.setTimestamp(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));

    final List<Event> events = new ArrayList<>();
    assertTrue(validator.validate(events, employment, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReferenceStartCanBeSetInThePast() {
    final Event refStart = new Event();
    refStart.setTag((Tag.ref_period_start.toString()));
    refStart.setTimestamp(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));

    final List<Event> events = new ArrayList<>();
    assertTrue(validator.validate(events, refStart, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReferenceEndCanBeSetInThePast() {
    final Event refEnd = new Event();
    refEnd.setTag((Tag.ref_period_end.toString()));
    refEnd.setTimestamp(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));

    final List<Event> events = new ArrayList<>();
    assertTrue(validator.validate(events, refEnd, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReferenceEndBeforeReferenceStartIsInvalid() {
    final Event refEnd = new Event();
    refEnd.setTag((Tag.ref_period_end.toString()));
    refEnd.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    final Event refStart = new Event();
    refStart.setTag((Tag.ref_period_start.toString()));
    refStart.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final List<Event> events = new ArrayList<>();
    events.add(refEnd);
    assertFalse(validator.validate(events, refStart, CollectionExerciseState.CREATED));
  }

  @Test
  public void testValidMpsEventUpdate() {
    long now = System.currentTimeMillis();

    final Event mpsEvent = new Event();
    mpsEvent.setTag(Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 1500000));

    assertTrue(validator.validate(mandatoryEvents, mpsEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testValidGoLiveEventUpdate() {
    long now = System.currentTimeMillis();

    final Event goLiveEvent = new Event();
    goLiveEvent.setTag(Tag.go_live.toString());
    goLiveEvent.setTimestamp(new Timestamp(now + 3 * DAYS_IN_MILLISECONDS));

    assertTrue(validator.validate(mandatoryEvents, goLiveEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testValidReturnByEventUpdate() {
    long now = System.currentTimeMillis();

    final Event returnByEvent = new Event();
    returnByEvent.setTag(Tag.return_by.toString());
    returnByEvent.setTimestamp(new Timestamp(now + 6 * DAYS_IN_MILLISECONDS));

    assertTrue(
        validator.validate(mandatoryEvents, returnByEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testValidExerciseEndEventUpdate() {
    long now = System.currentTimeMillis();

    final Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(new Timestamp(now + 10 * DAYS_IN_MILLISECONDS));

    assertTrue(
        validator.validate(mandatoryEvents, exerciseEndEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testInvalidMpsEventUpdate() {
    long now = System.currentTimeMillis();

    final Event mpsEvent = new Event();
    mpsEvent.setTag(Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + 6 * DAYS_IN_MILLISECONDS));

    assertFalse(validator.validate(mandatoryEvents, mpsEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testInvalidGoLiveEventUpdate() {
    long now = System.currentTimeMillis();

    final Event goLiveEvent = new Event();
    goLiveEvent.setTag(Tag.go_live.toString());
    goLiveEvent.setTimestamp(new Timestamp(now + 3500000));

    assertFalse(
        validator.validate(mandatoryEvents, goLiveEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testInvalidReturnByEventUpdate() {
    long now = System.currentTimeMillis();

    final Event returnByEvent = new Event();
    returnByEvent.setTag(Tag.return_by.toString());
    returnByEvent.setTimestamp(new Timestamp(now + 4500000));

    assertFalse(
        validator.validate(mandatoryEvents, returnByEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testInvalidExerciseEndEventUpdate() {
    long now = System.currentTimeMillis();

    final Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(new Timestamp(now + 1500000));

    assertFalse(
        validator.validate(mandatoryEvents, exerciseEndEvent, CollectionExerciseState.SCHEDULED));
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
    reminder.setTag(Tag.reminder.toString());
    reminder.setTimestamp(new Timestamp(now + 4 * DAYS_IN_MILLISECONDS + offset));
    nonMandatoryEvents.add(reminder);

    Event reminder2 = new Event();
    reminder2.setTag(Tag.reminder2.toString());
    reminder2.setTimestamp(new Timestamp(now + 5 * DAYS_IN_MILLISECONDS + offset));
    nonMandatoryEvents.add(reminder2);

    Event reminder3 = new Event();
    reminder3.setTag(Tag.reminder3.toString());
    reminder3.setTimestamp(new Timestamp(now + 6 * DAYS_IN_MILLISECONDS + offset));
    nonMandatoryEvents.add(reminder3);

    Event refPeriodStart = new Event();
    refPeriodStart.setTag(Tag.ref_period_start.toString());
    refPeriodStart.setTimestamp(new Timestamp(now + DAYS_IN_MILLISECONDS + offset));
    nonMandatoryEvents.add(refPeriodStart);

    Event refPeriodEnd = new Event();
    refPeriodEnd.setTag(Tag.ref_period_end.toString());
    refPeriodEnd.setTimestamp(new Timestamp(now + 7 * DAYS_IN_MILLISECONDS + offset));
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
    mpsEvent.setTag(Tag.mps.toString());
    mpsEvent.setTimestamp(new Timestamp(now + offset));
    eventList.add(mpsEvent);

    Event goLiveEvent = new Event();
    goLiveEvent.setTag(Tag.go_live.toString());
    goLiveEvent.setTimestamp(new Timestamp(now + 2 * DAYS_IN_MILLISECONDS + offset));
    eventList.add(goLiveEvent);

    Event returnByEvent = new Event();
    returnByEvent.setTag(Tag.return_by.toString());
    returnByEvent.setTimestamp(new Timestamp(now + 5 * DAYS_IN_MILLISECONDS + offset));
    eventList.add(returnByEvent);

    Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(new Timestamp(now + 7 * DAYS_IN_MILLISECONDS + offset));
    eventList.add(exerciseEndEvent);

    return eventList;
  }
}
