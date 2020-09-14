package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    Date goLiveDate = new Date(goLive.getTimestamp().getTime());
    Date returnByDate = new Date(returnBy.getTimestamp().getTime());

    try {
      nudgeEmailValidator.validate(
          events, nudgeEvent, CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNotNull(actualException);
    String expectedMessage =
        "Nudge email must be set after the Go Live date ("
            + sdf.format(goLiveDate)
            + ") "
            + "and before Return by date ("
            + sdf.format(returnByDate)
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

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    Date goLiveDate = new Date(goLive.getTimestamp().getTime());
    Date returnByDate = new Date(returnBy.getTimestamp().getTime());

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
            + sdf.format(goLiveDate)
            + ") "
            + "and before Return by date ("
            + sdf.format(returnByDate)
            + ")";
    assertEquals(expectedMessage, actualException.getMessage());
  }

  @Test
  public void testNudgeWithSameDateAndTimeEventCreation() {
    final Instant now = Instant.now();
    final Long nudgeTime = now.plus(2, ChronoUnit.DAYS).toEpochMilli();
    final Event goLive = new Event();
    goLive.setTag((EventService.Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(now));

    final Event nudge = new Event();
    nudge.setTag((EventService.Tag.nudge_email_0.toString()));
    nudge.setTimestamp(new Timestamp(nudgeTime));

    final Event returnBy = new Event();
    returnBy.setTag((EventService.Tag.return_by.toString()));
    returnBy.setTimestamp(Timestamp.from(now.plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(goLive, returnBy, nudge);

    final Event submittedEvent = new Event();
    submittedEvent.setTag((EventService.Tag.nudge_email_1.toString()));
    submittedEvent.setTimestamp(new Timestamp(nudgeTime));

    CTPException actualException = null;
    try {
      nudgeEmailValidator.validate(
          events, submittedEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals(
        "A nudge email has already been scheduled for this date and time. Choose a different date or time.",
        actualException.getMessage());
  }

  @Test
  public void testNudgeWithSameDateAndTimeEventCreationNotValidForTheSameEvent() {
    final Instant now = Instant.now();
    final Long nudgeTime = now.plus(2, ChronoUnit.DAYS).toEpochMilli();
    final Event goLive = new Event();
    goLive.setTag((EventService.Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(now));

    final Event nudge = new Event();
    nudge.setTag((EventService.Tag.nudge_email_0.toString()));
    nudge.setTimestamp(new Timestamp(nudgeTime));

    final Event returnBy = new Event();
    returnBy.setTag((EventService.Tag.return_by.toString()));
    returnBy.setTimestamp(Timestamp.from(now.plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(goLive, returnBy, nudge);

    final Event submittedEvent = new Event();
    submittedEvent.setTag((EventService.Tag.nudge_email_0.toString()));
    submittedEvent.setTimestamp(new Timestamp(nudgeTime));

    CTPException actualException = null;
    try {
      nudgeEmailValidator.validate(
          events, submittedEvent, CollectionExerciseDTO.CollectionExerciseState.CREATED);
    } catch (CTPException expectedException) {
      actualException = expectedException;
    }
    assertNull(actualException);
  }
}
