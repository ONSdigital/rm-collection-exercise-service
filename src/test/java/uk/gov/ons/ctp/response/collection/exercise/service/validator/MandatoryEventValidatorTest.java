 package uk.gov.ons.ctp.response.collection.exercise.service.validator;

 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.anyList;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;

 import java.sql.Timestamp;
 import java.time.Instant;
 import java.time.temporal.ChronoUnit;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Spy;
 import org.mockito.runners.MockitoJUnitRunner;
 import uk.gov.ons.ctp.common.error.CTPException;
 import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
 import
 uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
 import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
 import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

 @RunWith(MockitoJUnitRunner.class)
 public class MandatoryEventValidatorTest {

  @Spy private EventDateOrderChecker eventDateOrderChecker;

  @InjectMocks private MandatoryEventValidator mandatoryValidator;

  @Test
  public void isEventValidator() {
    assertThat(mandatoryValidator, instanceOf(EventValidator.class));
  }

  @Test
  public void returnTrueAndDoNothingIfNotReminder() throws CTPException {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.reminder.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now()));
    final List<Event> events = new ArrayList<>();
    mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.CREATED);

    verify(eventDateOrderChecker, never()).isEventDatesInOrder(anyList());
  }

  @Test
  public void testValidMpsEventCreation() throws CTPException {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    final List<Event> events = new ArrayList<>();
    mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.CREATED);
  }

  @Test
  public void testValidGoLiveEventCreation() throws CTPException {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final Event goLiveEvent = new Event();
    goLiveEvent.setTag((Tag.go_live.toString()));
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(mpsEvent);

    mandatoryValidator.validate(events, goLiveEvent, CollectionExerciseState.CREATED);
  }

  @Test
  public void testValidReturnByEventCreation() throws CTPException {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final Event goLiveEvent = new Event();
    goLiveEvent.setTag((Tag.go_live.toString()));
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(mpsEvent, goLiveEvent);

    final Event returnByEvent = new Event();
    returnByEvent.setTag((Tag.return_by.toString()));
    returnByEvent.setTimestamp(Timestamp.from(Instant.now().plus(6, ChronoUnit.DAYS)));

    mandatoryValidator.validate(events, returnByEvent, CollectionExerciseState.CREATED);
  }

  @Test
  public void testInvalidGoLiveEventCreation() {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(10, ChronoUnit.DAYS)));

    final Event goLive = new Event();
    goLive.setTag((Tag.go_live.toString()));
    goLive.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(mpsEvent);

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, goLive, CollectionExerciseState.CREATED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Collection exercise events must be set sequentially", actualException.getMessage());
  }

  @Test
  public void testInvalidReturnByEventCreation() {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    final Event goLiveEvent = new Event();
    goLiveEvent.setTag((Tag.go_live.toString()));
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(4, ChronoUnit.DAYS)));

    final List<Event> events = Arrays.asList(mpsEvent, goLiveEvent);

    final Event returnByEvent = new Event();
    returnByEvent.setTag((Tag.return_by.toString()));
    returnByEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, returnByEvent, CollectionExerciseState.CREATED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Collection exercise events must be set sequentially", actualException.getMessage());
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsLive() {
    final List<Event> events = new ArrayList<>();

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.LIVE);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Mandatory events cannot be changed if collection exercise is set to live (or locked)", actualException.getMessage());
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsValidated() {
    final List<Event> events = new ArrayList<>();

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.VALIDATED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Mandatory events cannot be changed if collection exercise is set to live, validated or locked)", actualException.getMessage());
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsExecutionStarted() {
    final List<Event> events = new ArrayList<>();

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.EXECUTION_STARTED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Mandatory events cannot be changed if collection exercise is set to live, executed, validated or locked", actualException.getMessage());
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsReadyForLive() {
    final List<Event> events = new ArrayList<>();

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.READY_FOR_LIVE);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Mandatory events cannot be changed if collection exercise is set to live, executed, validated or locked", actualException.getMessage());
  }

  @Test
  public void testMandatoryEventsCannotBeChangedIfCollectionExerciseIsExecuted() {
    final List<Event> events = new ArrayList<>();

    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.EXECUTED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Mandatory events cannot be changed if collection exercise is set to live, executed, validated or locked", actualException.getMessage());

  }

  @Test
  public void testValidReturnByEventUpdate() throws CTPException {
    final List<Event> events = createMandatoryEvents();

    final Event returnByEvent = new Event();
    returnByEvent.setTag(Tag.return_by.toString());
    returnByEvent.setTimestamp(Timestamp.from(Instant.now().plus(5, ChronoUnit.DAYS)));

        mandatoryValidator.validate(events, returnByEvent, CollectionExerciseState.SCHEDULED);
  }

  @Test
  public void testValidExerciseEndEventUpdate() throws CTPException {
    final List<Event> events = createMandatoryEvents();

    final Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(Timestamp.from(Instant.now().plus(10, ChronoUnit.DAYS)));

        mandatoryValidator.validate(events, exerciseEndEvent, CollectionExerciseState.SCHEDULED);
  }

  @Test
  public void testInvalidMpsEventUpdate() {
    final List<Event> events = createMandatoryEvents();

    final Event mpsEvent = new Event();
    mpsEvent.setTag(Tag.mps.toString());
    mpsEvent.setTimestamp(Timestamp.from(Instant.now().plus(6, ChronoUnit.DAYS)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, mpsEvent, CollectionExerciseState.SCHEDULED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Collection exercise events must be set sequentially", actualException.getMessage());
  }

  @Test
  public void testInvalidGoLiveEventUpdate() {
    final List<Event> events = createMandatoryEvents();

    final Event goLiveEvent = new Event();
    goLiveEvent.setTag(Tag.go_live.toString());
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(8, ChronoUnit.DAYS)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, goLiveEvent, CollectionExerciseState.SCHEDULED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Collection exercise events must be set sequentially", actualException.getMessage());
  }

  @Test
  public void testInvalidReturnByEventUpdate() {
    final List<Event> events = createMandatoryEvents();

    final Event returnByEvent = new Event();
    returnByEvent.setTag(Tag.return_by.toString());
    returnByEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, returnByEvent, CollectionExerciseState.SCHEDULED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Collection exercise events must be set sequentially", actualException.getMessage());
  }

  @Test
  public void testInvalidExerciseEndEventUpdate() {
    final List<Event> events = createMandatoryEvents();

    final Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));

    CTPException actualException = null;
    try {
      mandatoryValidator.validate(events, exerciseEndEvent, CollectionExerciseState.SCHEDULED);
    } catch (CTPException expectedException){
      actualException = expectedException;
    }
    assertNotNull(actualException);
    assertEquals("Collection exercise events must be set sequentially", actualException.getMessage());
  }

  private List<Event> createMandatoryEvents() {
    List<Event> eventList = new ArrayList<>();

    Event mpsEvent = new Event();
    mpsEvent.setTag(Tag.mps.toString());
    mpsEvent.setTimestamp(Timestamp.from(Instant.now()));
    eventList.add(mpsEvent);

    Event goLiveEvent = new Event();
    goLiveEvent.setTag(Tag.go_live.toString());
    goLiveEvent.setTimestamp(Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)));
    eventList.add(goLiveEvent);

    Event returnByEvent = new Event();
    returnByEvent.setTag(Tag.return_by.toString());
    returnByEvent.setTimestamp(Timestamp.from(Instant.now().plus(5, ChronoUnit.DAYS)));
    eventList.add(returnByEvent);

    Event exerciseEndEvent = new Event();
    exerciseEndEvent.setTag(Tag.exercise_end.toString());
    exerciseEndEvent.setTimestamp(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
    eventList.add(exerciseEndEvent);

    return eventList;
  }
 }
