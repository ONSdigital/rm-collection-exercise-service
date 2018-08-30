package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

public class ReferencePeriodEventValidatorTest {

  ReferencePeriodEventValidator referencePeriodValidator;

  @Before
  public void setUp() {
    referencePeriodValidator = new ReferencePeriodEventValidator();
  }

  @Test
  public void isEventValidator() {
    assertThat(referencePeriodValidator, instanceOf(EventValidator.class));
  }

  @Test
  public void returnTrueAndDoNothingIfNotReferencePeriodEvent() {
    final Event mpsEvent = new Event();
    mpsEvent.setTag((Tag.mps.toString()));
    mpsEvent.setTimestamp(Timestamp.from(Instant.now()));
    final List<Event> events = new ArrayList<>();
    assertTrue(
        referencePeriodValidator.validate(events, mpsEvent, CollectionExerciseState.CREATED));
  }

  @Test
  public void canUpdateReferencePeriodWhenCollectionExerciseReadyForLive() {
    final Event referencePeriodStart = new Event();
    referencePeriodStart.setTag(Tag.ref_period_end.toString());
    referencePeriodStart.setTimestamp(Timestamp.from(Instant.now()));

    final List<Event> events = new ArrayList<>();

    assertTrue(
        referencePeriodValidator.validate(
            events, referencePeriodStart, CollectionExerciseState.READY_FOR_LIVE));
  }

  @Test
  public void canUpdateReferencePeriodWhenCollectionExerciseLive() {
    final Event referencePeriodStart = new Event();
    referencePeriodStart.setTag(Tag.ref_period_start.toString());
    referencePeriodStart.setTimestamp(Timestamp.from(Instant.now()));

    final List<Event> events = new ArrayList<>();

    assertTrue(
        referencePeriodValidator.validate(
            events, referencePeriodStart, CollectionExerciseState.LIVE));
  }

  @Test
  public void testReferenceStartCanBeSetInThePast() {
    final Event refStart = new Event();
    refStart.setTag((Tag.ref_period_start.toString()));
    refStart.setTimestamp(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));

    final List<Event> events = new ArrayList<>();
    assertTrue(
        referencePeriodValidator.validate(events, refStart, CollectionExerciseState.CREATED));
  }

  @Test
  public void testReferenceEndCanBeSetInThePast() {
    final Event refEnd = new Event();
    refEnd.setTag((Tag.ref_period_end.toString()));
    refEnd.setTimestamp(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));

    final List<Event> events = new ArrayList<>();
    assertTrue(referencePeriodValidator.validate(events, refEnd, CollectionExerciseState.CREATED));
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
    assertFalse(
        referencePeriodValidator.validate(events, refStart, CollectionExerciseState.CREATED));
  }
}
