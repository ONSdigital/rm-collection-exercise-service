package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NudgeEmailValidatorTest {

  @Spy private EventDateOrderChecker eventDateOrderChecker;

  @InjectMocks private NudgeEmailValidator nudgeEmailValidator;

  @Before
  public void setUp() {}

  @Test
  public void isEventValidator() {
    assertThat(nudgeEmailValidator, instanceOf(EventValidator.class));
  }

  @Test
  public void returnTrueAndDoNothingIfNotNudge() throws CTPException {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((EventService.Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now()));
    final List<Event> events = new ArrayList<>();
    nudgeEmailValidator.validate(
        events, mpsEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED);

    verify(eventDateOrderChecker, never()).isEventDatesInOrder(anyList());
  }

  @Test
  public void testCanUpdateNudgeWhenReadyForLive() throws CTPException {
    final Event nudgeEvent = new Event();
    nudgeEvent.setTag(EventService.Tag.nudge_email_0.toString());
    nudgeEvent.setTimestamp(Timestamp.from(Instant.now()));

    final List<Event> events = new ArrayList<>();
    nudgeEmailValidator.validate(
        events, nudgeEvent, CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE);
  }

  @Test
  public void testCanUpdateNudgeWhenLive() throws CTPException {
    final Event nudgeEvent = new Event();
    nudgeEvent.setTag(EventService.Tag.nudge_email_0.toString());
    nudgeEvent.setTimestamp(Timestamp.from(Instant.now()));

    final List<Event> events = new ArrayList<>();

    nudgeEmailValidator.validate(
        events, nudgeEvent, CollectionExerciseDTO.CollectionExerciseState.LIVE);
  }

  @Test
  public void testCantUpdateNudgeThatHasPastAndCollectionExerciseInLockedState() {
    final Event nudge = new Event();
    nudge.setTag((EventService.Tag.nudge_email_0.toString()));
    nudge.setTimestamp(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));

    final Event newNudge = new Event();
    newNudge.setTag((EventService.Tag.nudge_email_0.toString()));
    newNudge.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final List<Event> events = Collections.singletonList(nudge);
    CTPException actualException = null;
    try {
      nudgeEmailValidator.validate(
          events, newNudge, CollectionExerciseDTO.CollectionExerciseState.LIVE);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Nudge email cannot be set in the past", actualException.getMessage());
  }

  @Test
  public void testCanUpdateNudgeThatHasPastAndCollectionExerciseNotInLockedState()
      throws CTPException {
    final Event nudge = new Event();
    nudge.setTag((EventService.Tag.nudge_email_0.toString()));
    nudge.setTimestamp(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));

    final Event newNudge = new Event();
    newNudge.setTag((EventService.Tag.nudge_email_0.toString()));
    newNudge.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final List<Event> events = Collections.singletonList(nudge);

    nudgeEmailValidator.validate(
        events, newNudge, CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
  }

  @Test
  public void testValidNudgeEventCreation() throws CTPException {
    final Event goLive = new Event();
    goLive.setTag((EventService.Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now()));

    final Event nudge = new Event();
    nudge.setTag((EventService.Tag.nudge_email_0.toString()));
    nudge.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final Event returnBy = new Event();
    returnBy.setTag((EventService.Tag.return_by.toString()));
    returnBy.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(goLive, returnBy);

    nudgeEmailValidator.validate(
        events, nudge, CollectionExerciseDTO.CollectionExerciseState.CREATED);
  }

  @Test
  public void testNudgeAfterReturnByEndInvalid() {
    final Event goLive = new Event();
    goLive.setTag((EventService.Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now()));

    final Event nudgeEvent = new Event();
    nudgeEvent.setTag(EventService.Tag.nudge_email_0.toString());
    nudgeEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final Event returnBy = new Event();
    returnBy.setTag((EventService.Tag.return_by.toString()));
    returnBy.setTimestamp(Timestamp.from(Instant.now().plus(3, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(goLive, returnBy);
    CTPException actualException = null;
    try {
      nudgeEmailValidator.validate(
          events, nudgeEvent, CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNotNull(actualException);
    String expectedMessage =
        "Nudge email must be set after the Go Live date ("
            + goLive.getTimestamp()
            + ") "
            + "and before Return by date ("
            + returnBy.getTimestamp()
            + ")";
    assertEquals(expectedMessage, actualException.getMessage());
  }

  @Test
  public void testNudgeBeforeGoliveInvalid() {
    final Event goLive = new Event();
    goLive.setTag((EventService.Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final Event returnBy = new Event();
    returnBy.setTag((EventService.Tag.return_by.toString()));
    returnBy.setTimestamp(Timestamp.from(Instant.now().plus(3, ChronoUnit.DAYS)));
    final List<Event> events = Arrays.asList(goLive, returnBy);

    final Event nudgeEvent = new Event();
    nudgeEvent.setTag(EventService.Tag.nudge_email_0.toString());
    nudgeEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));

    CTPException actualException = null;
    try {
      nudgeEmailValidator.validate(
          events, nudgeEvent, CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNotNull(actualException);
    String expectedMessage =
        "Nudge email must be set after the Go Live date ("
            + goLive.getTimestamp()
            + ") "
            + "and before Return by date ("
            + returnBy.getTimestamp()
            + ")";
    assertEquals(expectedMessage, actualException.getMessage());
  }

  @Test
  public void testNudge2WrongOrderEventCreation() {
    final Event nudge = new Event();
    nudge.setTag((EventService.Tag.nudge_email_0.toString()));
    nudge.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final Event nudge2 = new Event();
    nudge2.setTag((EventService.Tag.nudge_email_1.toString()));
    nudge2.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    final List<Event> events = new ArrayList<>();
    events.add(nudge);
    CTPException actualException = null;
    try {
      nudgeEmailValidator.validate(
          events, nudge2, CollectionExerciseDTO.CollectionExerciseState.CREATED);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Nudge Email must be set sequentially", actualException.getMessage());
  }

  @Test
  public void testNudge3WrongOrderEventCreation() {
    final Event nudge2 = new Event();
    nudge2.setTag((EventService.Tag.nudge_email_1.toString()));
    nudge2.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final Event nudge3 = new Event();
    nudge3.setTag((EventService.Tag.nudge_email_2.toString()));
    nudge3.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    final List<Event> events = new ArrayList<>();
    events.add(nudge2);
    CTPException actualException = null;
    try {
      nudgeEmailValidator.validate(
          events, nudge3, CollectionExerciseDTO.CollectionExerciseState.CREATED);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Nudge Email must be set sequentially", actualException.getMessage());
  }
}
