package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

@RunWith(MockitoJUnitRunner.class)
public class ReminderEventValidatorTest {

  @Spy private EventDateOrderChecker eventDateOrderChecker;

  @InjectMocks private ReminderEventValidator reminderValidator;

  @Before
  public void setUp() {}

  @Test
  public void isEventValidator() {
    assertThat(reminderValidator, instanceOf(EventValidator.class));
  }

  @Test
  public void returnTrueAndDoNothingIfNotReminder() {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now()));
    final List<Event> events = new ArrayList<>();
    assertTrue(reminderValidator.validate(events, mpsEvent, CollectionExerciseState.CREATED));

    verify(eventDateOrderChecker, never()).isEventDatesInOrder(anyList());
  }

  @Test
  public void testCanUpdateReminderWhenReadyForLive() {
    final Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder.toString());
    reminderEvent.setTimestamp(Timestamp.from(Instant.now()));

    final List<Event> events = new ArrayList<>();
    assertTrue(
        reminderValidator.validate(events, reminderEvent, CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void testCanUpdateReminderWhenLive() {
    final Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder.toString());
    reminderEvent.setTimestamp(Timestamp.from(Instant.now()));

    final List<Event> events = new ArrayList<>();
    assertTrue(reminderValidator.validate(events, reminderEvent, CollectionExerciseState.LIVE));
  }

  @Test
  public void testCantUpdateReminderThatHasPastAndCollectionExerciseInLockedState() {
    final Event reminder = new Event();
    reminder.setTag((Tag.reminder.toString()));
    reminder.setTimestamp(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));

    final Event newReminder = new Event();
    newReminder.setTag((Tag.reminder.toString()));
    newReminder.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final List<Event> events = Collections.singletonList(reminder);

    assertFalse(reminderValidator.validate(events, newReminder, CollectionExerciseState.LIVE));
  }

  @Test
  public void testCanUpdateReminderThatHasPastAndCollectionExerciseNotInLockedState() {
    final Event reminder = new Event();
    reminder.setTag((Tag.reminder.toString()));
    reminder.setTimestamp(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));

    final Event newReminder = new Event();
    newReminder.setTag((Tag.reminder.toString()));
    newReminder.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final List<Event> events = Collections.singletonList(reminder);

    assertTrue(reminderValidator.validate(events, newReminder, CollectionExerciseState.SCHEDULED));
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

    assertTrue(reminderValidator.validate(events, reminder, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReminderAfterExerciseEndInvalid() {
    final Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder3.toString());
    reminderEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final Event exerciseEnd = new Event();
    exerciseEnd.setTag((Tag.exercise_end.toString()));
    exerciseEnd.setTimestamp(Timestamp.from(Instant.now().plus(3, ChronoUnit.DAYS)));

    final List<Event> events = Collections.singletonList(exerciseEnd);

    assertFalse(
        reminderValidator.validate(events, reminderEvent, CollectionExerciseState.SCHEDULED));
  }

  @Test
  public void testReminderBeforeGoliveInvalid() {
    final Event goLive = new Event();
    goLive.setTag((Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final List<Event> events = Collections.singletonList(goLive);

    final Event reminderEvent = new Event();
    reminderEvent.setTag(Tag.reminder.toString());
    reminderEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));

    assertFalse(
        reminderValidator.validate(events, reminderEvent, CollectionExerciseState.SCHEDULED));
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
    assertFalse(reminderValidator.validate(events, reminder2, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReminder3WrongOrderEventCreation() {
    final Event reminder2 = new Event();
    reminder2.setTag((Tag.reminder2.toString()));
    reminder2.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final Event reminder3 = new Event();
    reminder3.setTag((Tag.reminder3.toString()));
    reminder3.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    final List<Event> events = new ArrayList<>();
    events.add(reminder2);
    assertFalse(reminderValidator.validate(events, reminder3, CollectionExerciseState.CREATED));
  }
}
