package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

/** Class containing tests for EventServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {
  private static final UUID SURVEY_ID = UUID.fromString("4ca97b1b-de9c-4fed-9898-fac594d1565f");
  private static final UUID COLLECTION_EXERCISE_EVENT_ID =
      UUID.fromString("ba6a92c1-9869-41ca-b0d8-12c27fc30e23");
  private static final UUID COLLEX_UUID = UUID.fromString("f03206ee-137d-41e3-af5c-2dea393bb360");
  private static final int EXERCISE_PK = 6433;

  @Mock private SurveySvcClient surveySvcClient;

  @Mock private CollectionExerciseService collectionExerciseService;

  @Mock private ActionRuleCreator actionRuleCreator;

  @Mock private ActionRuleCreator actionRuleCreator2;

  @Mock private ActionRuleUpdater actionRuleUpdater;

  @Mock private ActionRuleRemover actionRuleRemover;

  @Mock private ActionRuleUpdater actionRuleUpdater2;

  @Mock private EventValidator eventValidator;

  @Mock private EventRepository eventRepository;

  @Spy private List<ActionRuleCreator> actionRuleCreators = new ArrayList<>();

  @Spy private List<ActionRuleUpdater> actionRuleUpdaters = new ArrayList<>();

  @Spy private List<ActionRuleRemover> actionRuleRemovers = new ArrayList<>();

  @Spy private List<EventValidator> eventValidators = new ArrayList<>();

  @InjectMocks private EventService eventService;

  private static Event createEvent(Tag tag) {
    Timestamp eventTime = new Timestamp(new Date().getTime());
    Event event = new Event();
    event.setTimestamp(eventTime);
    event.setTag(tag.name());

    return event;
  }

  @Test
  public void givenCollectionExcerciseDoesNotExistWhenEventIsCreatedThenExceptionIsThrown() {
    EventDTO eventDto = new EventDTO();
    UUID collexUuid = UUID.randomUUID();
    eventDto.setCollectionExerciseId(collexUuid);

    try {
      eventService.createEvent(eventDto);

      fail("Created event with non-existent collection exercise");
    } catch (CTPException e) {
      // Expected 404
      assertThat(e.getFault(), is(Fault.RESOURCE_NOT_FOUND));
    }
  }

  @Test
  public void givenEventAlreadyExistsWhenEventIsCreatedThenExceptionIsThrown() throws CTPException {
    String tag = Tag.mps.name();
    EventDTO eventDto = new EventDTO();
    CollectionExercise collex = new CollectionExercise();
    UUID collexUuid = UUID.randomUUID();
    eventDto.setCollectionExerciseId(collexUuid);
    eventDto.setTag(tag);
    collex.setId(collexUuid);

    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, tag)).thenReturn(new Event());

    try {
      eventService.createEvent(eventDto);

      fail("Created event with non-existent collection exercise");
    } catch (CTPException e) {
      // Expected 409
      assertThat(e.getFault(), is(Fault.RESOURCE_VERSION_CONFLICT));
    }
  }

  @Test
  public void testCreateCorrectActionRulesForAnyEvent() throws CTPException {
    // Given
    Event collectionExerciseEvent = new Event();

    CollectionExercise collex = new CollectionExercise();
    collex.setExercisePK(EXERCISE_PK);
    collex.setSurveyId(SURVEY_ID);

    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(Tag.mps.name());
    collectionExerciseEvent.setTimestamp(eventTriggerDate);
    collectionExerciseEvent.setId(COLLECTION_EXERCISE_EVENT_ID);

    actionRuleCreators.add(actionRuleCreator);
    actionRuleCreators.add(actionRuleCreator2);

    // When
    eventService.createActionRulesForEvent(collectionExerciseEvent);

    // Then
    verify(actionRuleCreator).execute(eq(collectionExerciseEvent));
    verify(actionRuleCreator2).execute(eq(collectionExerciseEvent));
  }

  @Test
  public void testNoActionRulesCreatedForNonActionableEvents() throws CTPException {
    // Given
    final Event collectionExerciseEvent = new Event();
    final CollectionExercise collex = new CollectionExercise();

    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(Tag.employment.name());

    // When
    eventService.createActionRulesForEvent(collectionExerciseEvent);

    // Then
    verify(actionRuleCreator, never()).execute(any());
  }

  @Test
  public void givenNoEventsWhenScheduledIsCheckedThenFalse() throws CTPException {
    UUID collexUuid = UUID.randomUUID();
    when(eventRepository.findByCollectionExerciseId(collexUuid)).thenReturn(new ArrayList<>());

    boolean scheduled = this.eventService.isScheduled(collexUuid);

    assertFalse(scheduled);
  }

  @Test
  public void givenCollectionExcerciseDoesNotExistWhenEventIsUpdatedThenExceptionIsThrown() {
    final UUID collexUuid = UUID.randomUUID();

    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(null);

    try {
      eventService.updateEvent(collexUuid, Tag.mps.name(), new Date());

      Assert.fail("Updated event with non-existent collection exercise");
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(Fault.BAD_REQUEST));
    }
  }

  @Test
  public void givenCollectionExcerciseDoesNotExistWhenEventIsDeletedThenExceptionIsThrown() {
    final UUID collexUuid = UUID.randomUUID();

    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(null);

    try {
      eventService.deleteEvent(collexUuid, Tag.mps.name());

      Assert.fail("Deleted event with non-existent collection exercise");
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(Fault.BAD_REQUEST));
    }
  }

  @Test
  public void givenEventDoesNotExistWhenEventIsUpdatedThenExceptionIsThrown() {
    final UUID collexUuid = UUID.randomUUID();

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(collexUuid);

    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.mps.name()))
        .thenReturn(null);

    try {
      eventService.updateEvent(collexUuid, Tag.mps.name(), new Date());

      Assert.fail("Updated non-existent event");
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(Fault.RESOURCE_NOT_FOUND));
    }
  }

  @Test
  public void givenEventDoesNotExistWhenEventIsDeletedThenExceptionIsThrown() {
    final UUID collexUuid = UUID.randomUUID();

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(collexUuid);

    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.mps.name()))
        .thenReturn(null);

    try {
      eventService.deleteEvent(collexUuid, Tag.mps.name());

      Assert.fail("Deleted non-existent event");
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(Fault.RESOURCE_NOT_FOUND));
    }
  }

  @Test
  public void
      givenEventsForCollectionExerciseDoNotValidateWhenEventIsUpdatedThenExceptionIsThrown() {
    final UUID collexUuid = UUID.randomUUID();

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(collexUuid);
    final CollectionExerciseState collectionExerciseState = CollectionExerciseState.SCHEDULED;
    collex.setState(collectionExerciseState);

    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
    final Event existingEvent = new Event();
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.mps.name()))
        .thenReturn(existingEvent);

    final List<Event> existingEvents = new ArrayList<>();
    when(eventRepository.findByCollectionExercise(collex)).thenReturn(existingEvents);
    eventValidators.add(eventValidator);

    try {
      eventService.updateEvent(collexUuid, Tag.mps.name(), new Date());
    } catch (final CTPException e) {
      Assert.assertEquals(Fault.BAD_REQUEST, e.getFault());
    }
  }

  @Test
  public void givenEventsForCollectionExerciseValidateWhenEventIsUpdatedItIsSaved()
      throws CTPException {

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_UUID);
    collex.setExercisePK(EXERCISE_PK);
    final CollectionExerciseState collectionExerciseState = CollectionExerciseState.SCHEDULED;
    collex.setState(collectionExerciseState);

    final SurveyDTO survey = new SurveyDTO();
    when(surveySvcClient.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    when(collectionExerciseService.findCollectionExercise(COLLEX_UUID)).thenReturn(collex);
    final Event existingEvent = new Event();
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.mps.name()))
        .thenReturn(existingEvent);

    final List<Event> existingEvents = new ArrayList<>();

    when(eventRepository.findByCollectionExercise(collex)).thenReturn(existingEvents);
    eventValidators.add(eventValidator);

    actionRuleUpdaters.add(actionRuleUpdater);
    actionRuleUpdaters.add(actionRuleUpdater2);

    eventService.updateEvent(COLLEX_UUID, Tag.mps.name(), new Date());

    verify(eventRepository, atLeastOnce()).save(eq(existingEvent));
    verify(actionRuleUpdater, atLeastOnce()).execute(existingEvent);
    verify(actionRuleUpdater2, atLeastOnce()).execute(existingEvent);
  }

  @Test
  public void givenEventsForCollectionExerciseValidateWhenEventIsDeletedItIsSaved()
      throws CTPException {

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_UUID);
    collex.setExercisePK(EXERCISE_PK);
    final CollectionExerciseState collectionExerciseState = CollectionExerciseState.SCHEDULED;
    collex.setState(collectionExerciseState);

    final SurveyDTO survey = new SurveyDTO();
    when(surveySvcClient.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    when(collectionExerciseService.findCollectionExercise(COLLEX_UUID)).thenReturn(collex);
    final Event existingEvent = new Event();
    existingEvent.setTag(Tag.nudge_email_4.toString());
    existingEvent.setId(UUID.randomUUID());
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.nudge_email_4.name()))
        .thenReturn(existingEvent);

    final List<Event> existingEvents = new ArrayList<>();

    when(eventRepository.findByCollectionExercise(collex)).thenReturn(existingEvents);
    eventValidators.add(eventValidator);

    actionRuleRemovers.add(actionRuleRemover);

    eventService.deleteEvent(COLLEX_UUID, Tag.nudge_email_4.name());

    verify(eventRepository, atLeastOnce()).delete(eq(existingEvent));
    verify(actionRuleRemover, atLeastOnce()).execute(existingEvent);
  }

  @Test
  public void givenSomeEventsWhenScheduledIsCheckedThenFalse() throws CTPException {
    UUID collexUuid = UUID.randomUUID();
    List<Event> events = createEventList(Tag.mps, Tag.exercise_end);
    when(eventRepository.findByCollectionExerciseId(collexUuid)).thenReturn(events);

    boolean scheduled = this.eventService.isScheduled(collexUuid);

    assertFalse(scheduled);
  }

  @Test
  public void givenAllEventsWhenScheduledIsCheckedThenTrue() throws CTPException {
    UUID collexUuid = UUID.randomUUID();
    List<Event> events = createEventList(Tag.values());
    when(eventRepository.findByCollectionExerciseId(collexUuid)).thenReturn(events);

    boolean scheduled = this.eventService.isScheduled(collexUuid);

    assertTrue(scheduled);
  }

  private List<Event> createEventList(Tag... tags) {
    return Arrays.stream(tags).map(EventServiceTest::createEvent).collect(Collectors.toList());
  }

  /** Given collection exercise doesn't exist Then exception is thrown */
  @Test
  public void givenCollectionExerciseDoesNotExistWhenCreatingAnExceptionThrowError() {
    final String tag = Tag.mps.name();
    final EventDTO eventDto = new EventDTO();
    final UUID collexUuid = UUID.randomUUID();
    eventDto.setCollectionExerciseId(collexUuid);
    eventDto.setTag(tag);
    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(null);
    try {
      eventService.createEvent(eventDto);
      fail("Created event with non-existent collection exercise");
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(Fault.RESOURCE_NOT_FOUND));
    }
  }

  /** Given collection exercise doesn't exist Then exception is thrown */
  @Test
  public void givenCollectionExerciseEventsAreInInvalidStateThrowException() {
    final String tag = Tag.mps.name();
    final EventDTO eventDto = new EventDTO();
    final CollectionExercise collex = new CollectionExercise();
    final UUID collexUuid = UUID.randomUUID();
    eventDto.setCollectionExerciseId(collexUuid);
    eventDto.setTag(tag);
    eventDto.setTimestamp(new Timestamp(Instant.now().toEpochMilli()));
    collex.setId(collexUuid);
    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.mps.name()))
        .thenReturn(null);
    final List<Event> existingEvents = new ArrayList<>();
    final Event event = new Event();
    existingEvents.add(event);
    when(eventRepository.findByCollectionExercise(collex)).thenReturn(existingEvents);
    eventValidators.add(eventValidator);
    try {
      eventService.createEvent(eventDto);
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(Fault.BAD_REQUEST));
    }
  }

  @Test
  public void testTagShouldHaveMpsAsIsActionable() {
    assertThat(Tag.mps.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveGoLiveAsIsAnActionableTag() {
    assertThat(Tag.go_live.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveExerciseEndAsNotIsAnActionableTag() {
    assertThat(Tag.exercise_end.isActionable(), Matchers.is(false));
  }

  @Test
  public void testTagShouldHaveReminderAsAnActionableTag() {
    assertThat(Tag.reminder.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveReminder2AsAnActionableTag() {
    assertThat(Tag.reminder2.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveReminder3AsAnActionableTag() {
    assertThat(Tag.reminder3.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveNudge0AsAnActionableTag() {
    assertThat(Tag.nudge_email_0.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveNudge1AsAnActionableTag() {
    assertThat(Tag.nudge_email_1.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveNudge2AsAnActionableTag() {
    assertThat(Tag.nudge_email_2.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveNudge3AsAnActionableTag() {
    assertThat(Tag.nudge_email_3.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveNudge4AsAnActionableTag() {
    assertThat(Tag.nudge_email_4.isActionable(), Matchers.is(true));
  }

  @Test
  public void testCompareTagToItsName() {
    assertThat(Tag.mps.hasName("mps"), Matchers.is(true));
  }

  @Test
  public void testCompareTagToRandomString() {
    assertThat(Tag.mps.hasName("asdadfgsfgth"), Matchers.is(false));
  }

  @Test
  public void testTagShouldHaveReminderAsIsReminder() {
    assertThat(Tag.reminder.isReminder(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveReminder2AsIsReminder() {
    assertThat(Tag.reminder2.isReminder(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveReminder3AsIsReminder() {
    assertThat(Tag.reminder3.isReminder(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveMPSAsIsNotReminder() {
    assertThat(Tag.mps.isReminder(), Matchers.is(false));
  }
}
