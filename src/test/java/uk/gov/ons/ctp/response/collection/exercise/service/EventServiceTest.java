package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CaseSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

/** Class containing tests for EventServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {
  private static final UUID COLLEX_UUID = UUID.fromString("f03206ee-137d-41e3-af5c-2dea393bb360");
  private static final int EXERCISE_PK = 6433;

  @Mock private ActionSvcClient actionSvcClient;

  @Mock private CaseSvcClient caseSvcClient;

  @Mock private SurveySvcClient surveySvcClient;

  @Mock private AppConfig mockAppConfig;

  @Mock private CollectionExerciseService collectionExerciseService;

  @Mock private EventValidator eventValidator;

  @Mock private EventRepository eventRepository;

  @Spy private List<EventValidator> eventValidators = new ArrayList<>();

  @InjectMocks private EventService eventService;

  @Before
  public void setActionDeprecatedFalse() {
    ActionSvc actionSvc = new ActionSvc();
    actionSvc.setDeprecated(false);
    when(mockAppConfig.getActionSvc()).thenReturn(actionSvc);
  }

  private static Event createEvent(Tag tag) {
    Timestamp eventTime = new Timestamp(new Date().getTime());
    Event event = new Event();
    event.setTimestamp(eventTime);
    event.setTag(tag.name());

    return event;
  }

  /**
   * @param tag Tag object
   * @param timestamp Timestamp in the form of dd/MM/yyyy
   * @return An event with the tag and timestamp setup
   */
  private static Event createEvent(Tag tag, String timestamp) {
    Date parsedDate = new Date();
    try {
      parsedDate = new SimpleDateFormat("dd/MM/yyyy").parse(timestamp);
    } catch (ParseException e) {
      fail("Failed to parse date");
    }
    Event event = new Event();
    event.setTimestamp(new java.sql.Timestamp(parsedDate.getTime()));
    event.setTag(tag.name());
    return event;
  }

  @Test
  public void givenCollectionExerciseDoesNotExistWhenEventIsCreatedThenExceptionIsThrown() {
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

    when(collectionExerciseService.findCollectionExercise(COLLEX_UUID)).thenReturn(collex);
    final Event existingEvent = new Event();
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.mps.name()))
        .thenReturn(existingEvent);

    final List<Event> existingEvents = new ArrayList<>();

    when(eventRepository.findByCollectionExercise(collex)).thenReturn(existingEvents);
    eventValidators.add(eventValidator);

    eventService.updateEvent(COLLEX_UUID, Tag.mps.name(), new Date());

    verify(eventRepository, atLeastOnce()).save(eq(existingEvent));
  }

  @Test
  public void givenEventsForCollectionExerciseValidateWhenEventIsDeletedItIsSaved()
      throws CTPException {

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_UUID);
    collex.setExercisePK(EXERCISE_PK);
    final CollectionExerciseState collectionExerciseState = CollectionExerciseState.SCHEDULED;
    collex.setState(collectionExerciseState);

    when(collectionExerciseService.findCollectionExercise(COLLEX_UUID)).thenReturn(collex);
    final Event existingEvent = new Event();
    existingEvent.setTag(Tag.nudge_email_4.toString());
    existingEvent.setId(UUID.randomUUID());
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.nudge_email_4.name()))
        .thenReturn(existingEvent);

    final List<Event> existingEvents = new ArrayList<>();

    eventValidators.add(eventValidator);

    eventService.deleteEvent(COLLEX_UUID, Tag.nudge_email_4.name());

    verify(eventRepository, atLeastOnce()).delete(eq(existingEvent));
  }

  @Test
  public void givenReminderEmailIsDeletedItGetsPropagatedToActionSVC() throws CTPException {

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_UUID);
    collex.setExercisePK(EXERCISE_PK);
    final CollectionExerciseState collectionExerciseState = CollectionExerciseState.SCHEDULED;
    collex.setState(collectionExerciseState);

    when(collectionExerciseService.findCollectionExercise(COLLEX_UUID)).thenReturn(collex);
    final Event existingEvent = new Event();
    existingEvent.setTag(Tag.reminder.toString());
    existingEvent.setId(UUID.randomUUID());
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.reminder.name()))
        .thenReturn(existingEvent);

    final List<Event> existingEvents = new ArrayList<>();

    eventValidators.add(eventValidator);

    eventService.deleteEvent(COLLEX_UUID, Tag.reminder.name());

    verify(eventRepository, atLeastOnce()).delete(eq(existingEvent));
  }

  @Test
  public void
      givenScheduledExistingNudgeEmailsBeforeReturnByDateNudgeEmailsAreDeletedAndReturnByIsUpdated()
          throws CTPException {

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(COLLEX_UUID);
    collex.setExercisePK(EXERCISE_PK);
    final CollectionExerciseState collectionExerciseState = CollectionExerciseState.SCHEDULED;
    collex.setState(collectionExerciseState);

    when(collectionExerciseService.findCollectionExercise(COLLEX_UUID)).thenReturn(collex);
    final Event nudgeEvent = new Event();
    final Instant now = Instant.now();
    nudgeEvent.setTag(Tag.nudge_email_4.toString());
    nudgeEvent.setId(UUID.randomUUID());
    nudgeEvent.setTimestamp(new Timestamp(now.toEpochMilli()));

    final Event returnByEvent = new Event();
    returnByEvent.setTag(Tag.return_by.toString());
    returnByEvent.setId(UUID.randomUUID());
    returnByEvent.setTimestamp(new Timestamp(now.toEpochMilli()));

    Date newDate = new Date();
    newDate.setTime(now.minus(1, ChronoUnit.DAYS).toEpochMilli());

    when(eventRepository.findOneByCollectionExerciseAndTag(collex, Tag.return_by.name()))
        .thenReturn(returnByEvent);

    final List<Event> existingEvents = new ArrayList<>();
    existingEvents.add(nudgeEvent);
    existingEvents.add(returnByEvent);

    when(eventRepository.findByCollectionExercise(collex)).thenReturn(existingEvents);
    eventValidators.add(eventValidator);

    eventService.updateEvent(COLLEX_UUID, Tag.return_by.name(), newDate);

    verify(eventRepository, atLeastOnce()).save(returnByEvent);
    verify(eventRepository, atLeastOnce()).delete(eq(nudgeEvent));
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
  public void testStatusIsSetToScheduledNewEventCreated() {
    final CollectionExercise collex = new CollectionExercise();
    String tag = Tag.mps.name();

    when(collectionExerciseService.findCollectionExercise(COLLEX_UUID)).thenReturn(collex);
    when(eventRepository.save(any(Event.class))).then(returnsFirstArg());

    EventDTO eventDto = new EventDTO();
    eventDto.setCollectionExerciseId(COLLEX_UUID);
    eventDto.setTag(tag);
    eventDto.setTimestamp(new Timestamp(new Date().getTime()));

    try {
      Event event = eventService.createEvent(eventDto);
      assertThat(event.getStatus(), is(EventDTO.Status.SCHEDULED));
    } catch (CTPException e) {
      fail();
    }
  }

  @Test
  public void testProcessEventsNoScheduledEvents() {
    // Given
    List<Event> emptyList = Collections.emptyList();
    Stream<Event> eventStream = emptyList.stream();
    when(eventRepository.findByStatus(EventDTO.Status.SCHEDULED)).thenReturn(eventStream);

    // When
    eventService.processEvents();

    // Then
    verify(eventRepository, times(1)).findByStatus(EventDTO.Status.SCHEDULED);
    verify(actionSvcClient, never()).processEvent(any(), any());
  }

  @Test
  public void testProcessEventsOnlyEventInFuture() {
    // Given
    List<Event> list = new ArrayList<>();
    Event event = createEvent(Tag.mps, "31/12/2999");
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setState(CollectionExerciseState.LIVE);
    event.setCollectionExercise(collectionExercise);
    list.add(event);
    Stream<Event> eventStream = list.stream();

    when(eventRepository.findByStatus(EventDTO.Status.SCHEDULED)).thenReturn(eventStream);

    // When
    eventService.processEvents();

    // Then
    verify(eventRepository, times(1)).findByStatus(EventDTO.Status.SCHEDULED);
    verify(actionSvcClient, never()).processEvent(any(), any());
  }

  @Test
  public void testProcessEventsTransitionGoLive() {
    // Given
    List<Event> list = new ArrayList<>();
    Event event = createEvent(Tag.go_live);
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSampleSize(1);
    collectionExercise.setState(CollectionExerciseState.LIVE);
    event.setCollectionExercise(collectionExercise);
    list.add(event);
    Stream<Event> eventStream = list.stream();

    when(caseSvcClient.getNumberOfCases(any())).thenReturn(1L);
    when(eventRepository.findByStatus(EventDTO.Status.SCHEDULED)).thenReturn(eventStream);

    // When
    eventService.processEvents();

    // Then
    verify(eventRepository, times(1)).findByStatus(EventDTO.Status.SCHEDULED);
    verify(actionSvcClient, times(1)).processEvent(any(), any());
    try {
      verify(collectionExerciseService, times(1))
          .transitionCollectionExercise(
              any(CollectionExercise.class),
              eq(CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE));
    } catch (CTPException e) {
      fail();
    }
  }

  @Test
  public void testProcessEventsTransitionEnded() {
    // Given
    List<Event> list = new ArrayList<>();
    Event event = createEvent(Tag.exercise_end);
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSampleSize(1);
    collectionExercise.setState(CollectionExerciseState.LIVE);
    event.setCollectionExercise(collectionExercise);
    list.add(event);
    Stream<Event> eventStream = list.stream();

    when(caseSvcClient.getNumberOfCases(any())).thenReturn(1L);
    when(eventRepository.findByStatus(EventDTO.Status.SCHEDULED)).thenReturn(eventStream);

    // When
    eventService.processEvents();

    // Then
    verify(eventRepository, times(1)).findByStatus(EventDTO.Status.SCHEDULED);
    try {
      verify(collectionExerciseService, times(1))
          .transitionCollectionExercise(
              any(CollectionExercise.class),
              eq(CollectionExerciseDTO.CollectionExerciseEvent.END_EXERCISE));
    } catch (CTPException e) {
      fail();
    }
  }

  @Test
  public void testProcessEventsTransitionGoLiveNullSampleSize() {
    // Given
    List<Event> list = new ArrayList<>();
    Event event = createEvent(Tag.go_live);
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSampleSize(null);
    collectionExercise.setState(CollectionExerciseState.LIVE);
    event.setCollectionExercise(collectionExercise);
    list.add(event);
    Stream<Event> eventStream = list.stream();

    when(caseSvcClient.getNumberOfCases(any())).thenReturn(1L);
    when(eventRepository.findByStatus(EventDTO.Status.SCHEDULED)).thenReturn(eventStream);

    // When
    eventService.processEvents();

    // Then
    verify(eventRepository, times(1)).findByStatus(EventDTO.Status.SCHEDULED);
    verify(actionSvcClient, never()).processEvent(any(), any());
    try {
      verify(collectionExerciseService, never())
          .transitionCollectionExercise(
              any(CollectionExercise.class),
              any(CollectionExerciseDTO.CollectionExerciseEvent.class));
    } catch (CTPException e) {
      fail();
    }
  }

  @Test
  public void testProcessEventsTransitionGoLiveMismatchedCases() {
    // Given

    List<Event> list = new ArrayList<>();
    Event event = createEvent(Tag.go_live);
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSampleSize(10);
    collectionExercise.setState(CollectionExerciseState.LIVE);
    event.setCollectionExercise(collectionExercise);
    list.add(event);
    Stream<Event> eventStream = list.stream();

    when(caseSvcClient.getNumberOfCases(any())).thenReturn(9L);
    when(eventRepository.findByStatus(EventDTO.Status.SCHEDULED)).thenReturn(eventStream);

    // When
    eventService.processEvents();

    // Then
    verify(eventRepository, times(1)).findByStatus(EventDTO.Status.SCHEDULED);
    verify(actionSvcClient, never()).processEvent(any(), any());
    try {
      verify(collectionExerciseService, never())
          .transitionCollectionExercise(
              any(CollectionExercise.class),
              any(CollectionExerciseDTO.CollectionExerciseEvent.class));
    } catch (CTPException e) {
      fail();
    }
  }

  @Test
  public void testTagShouldHaveMpsAsIsActionable() {
    assertThat(Tag.mps.isActionable(), Matchers.is(true));
  }

  @Test
  public void testTagShouldHaveGoLiveAsIsAnActionableTag() {
    assertTrue(Tag.go_live.isActionable());
  }

  @Test
  public void testTagShouldHaveExerciseEndAsNotIsAnActionableTag() {
    assertFalse(Tag.exercise_end.isActionable());
  }

  @Test
  public void testTagShouldHaveReminderAsAnActionableTag() {
    assertTrue(Tag.reminder.isActionable());
  }

  @Test
  public void testTagShouldHaveReminder2AsAnActionableTag() {
    assertTrue(Tag.reminder2.isActionable());
  }

  @Test
  public void testTagShouldHaveReminder3AsAnActionableTag() {
    assertTrue(Tag.reminder3.isActionable());
  }

  @Test
  public void testTagShouldHaveNudge0AsAnActionableTag() {
    assertTrue(Tag.nudge_email_0.isActionable());
  }

  @Test
  public void testTagShouldHaveNudge1AsAnActionableTag() {
    assertTrue(Tag.nudge_email_1.isActionable());
  }

  @Test
  public void testTagShouldHaveNudge2AsAnActionableTag() {
    assertTrue(Tag.nudge_email_2.isActionable());
  }

  @Test
  public void testTagShouldHaveNudge3AsAnActionableTag() {
    assertTrue(Tag.nudge_email_3.isActionable());
  }

  @Test
  public void testTagShouldHaveNudge4AsAnActionableTag() {
    assertTrue(Tag.nudge_email_4.isActionable());
  }

  @Test
  public void testCompareTagToItsName() {
    assertTrue(Tag.mps.hasName("mps"));
  }

  @Test
  public void testCompareTagToRandomString() {
    assertFalse(Tag.mps.hasName("asdadfgsfgth"));
  }

  @Test
  public void testTagShouldHaveReminderAsIsReminder() {
    assertTrue(Tag.reminder.isReminder());
  }

  @Test
  public void testTagShouldHaveReminder2AsIsReminder() {
    assertTrue(Tag.reminder2.isReminder());
  }

  @Test
  public void testTagShouldHaveReminder3AsIsReminder() {
    assertTrue(Tag.reminder3.isReminder());
  }

  @Test
  public void testTagShouldHaveMPSAsIsNotReminder() {
    assertFalse(Tag.mps.isReminder());
  }
}
