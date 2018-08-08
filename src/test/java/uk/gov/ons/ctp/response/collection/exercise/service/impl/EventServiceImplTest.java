package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** Class containing tests for EventServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class EventServiceImplTest {
  public static final UUID SURVEY_ID = UUID.fromString("4ca97b1b-de9c-4fed-9898-fac594d1565f");
  @Mock private SurveySvcClient surveySvcClient;

  @Mock private EventRepository eventRepository;

  @Mock private CaseTypeOverrideRepository caseTypeOverrideRepo;

  @Mock private CollectionExerciseService collectionExerciseService;

  @Mock private ActionRuleCreator actionRuleCreator;

  @Mock private ActionRuleCreator actionRuleCreator2;

  @Spy private List<ActionRuleCreator> actionRuleCreators = new ArrayList<ActionRuleCreator>();

  @InjectMocks private EventServiceImpl eventService;

  @Mock private EventValidator eventValidator;

  private static Event createEvent(Tag tag) {
    Timestamp eventTime = new Timestamp(new Date().getTime());
    Event event = new Event();
    event.setTimestamp(eventTime);
    event.setTag(tag.name());

    return event;
  }

  /** Given collection excercise does not exist When event is created Then exception is thrown */
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
      assertEquals(Fault.RESOURCE_NOT_FOUND, e.getFault());
    }
  }

  /* Given event already exists When event is created Then exception is thrown */
  @Test
  public void givenEventAlreadyExistsWhenEventIsCreatedThenExceptionIsThrown() {
    final String tag = Tag.mps.name();
    final EventDTO eventDto = new EventDTO();
    final CollectionExercise collex = new CollectionExercise();
    final UUID collexUuid = UUID.randomUUID();
    eventDto.setCollectionExerciseId(collexUuid);
    eventDto.setTag(tag);
    collex.setId(collexUuid);

    when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
    when(eventRepository.findOneByCollectionExerciseAndTag(collex, tag)).thenReturn(new Event());

    try {
      eventService.createEvent(eventDto);

      fail("Created event with non-existent collection exercise");
    } catch (final CTPException e) {
      assertEquals(Fault.RESOURCE_VERSION_CONFLICT, e.getFault());
    }
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
      assertEquals(Fault.RESOURCE_NOT_FOUND, e.getFault());
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
    when(eventValidator.validateOnCreate(existingEvents, event, collex.getState()))
        .thenReturn(false);

    try {
      eventService.createEvent(eventDto);

      fail("No exception thrown on bad event");
    } catch (final CTPException e) {
      assertEquals(Fault.BAD_REQUEST, e.getFault());
    }
  }

  /**
   * Test CTP exception thrown if no business case type override found, so no action plans have been
   * associated to CE
   */
  @Test
  public void testCreateActionRulesRaisesCTPExceptionIfNoBCaseOverRide() {
    Event event = new Event();
    CollectionExercise collex = new CollectionExercise();
    CaseTypeOverride biCaseTypeOverride = new CaseTypeOverride();
    collex.setExercisePK(1);
    event.setTag(Tag.mps.name());

    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(1, "B")).thenReturn(null);
    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(1, "BI"))
        .thenReturn(biCaseTypeOverride);
    try {
      eventService.createActionRulesForEvent(event, collex);
      fail("Trying to create action rules when no action plans associated");
    } catch (CTPException e) {
      assertEquals(CTPException.Fault.RESOURCE_NOT_FOUND, e.getFault());
    }
  }

  /**
   * Test CTP exception thrown if no business individual case type override found, so no action
   * plans have been associated to CE
   */
  @Test
  public void testCreateActionRulesRaisesCTPExceptionIfNoBICaseOverRide() {
    String tag = Tag.mps.name();
    Event event = new Event();
    CollectionExercise collex = new CollectionExercise();
    CaseTypeOverride bCaseTypeOverride = new CaseTypeOverride();
    collex.setExercisePK(1);
    event.setTag(tag);

    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(1, "B"))
        .thenReturn(bCaseTypeOverride);
    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(1, "BI")).thenReturn(null);
    try {
      eventService.createActionRulesForEvent(event, collex);
      fail("Trying to create action rules when no action plans associated");
    } catch (CTPException e) {
      assertEquals(CTPException.Fault.RESOURCE_NOT_FOUND, e.getFault());
    }
  }

  @Test
  public void testCreateCorrectActionRulesForAnyEvent() throws CTPException {
    // Given
    String businessSampleType = "B";
    String businessIndividualSampleType = "BI";
    Event collectionExerciseEvent = new Event();

    CollectionExercise collex = new CollectionExercise();
    int exercisePk = 6433;
    collex.setExercisePK(exercisePk);
    collex.setSurveyId(SURVEY_ID);

    SurveyDTO surveyDto = new SurveyDTO();
    when(surveySvcClient.findSurvey(SURVEY_ID)).thenReturn(surveyDto);

    CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    UUID businessActionPlanId = UUID.randomUUID();
    businessCaseTypeOverride.setActionPlanId(businessActionPlanId);

    CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    UUID businessIndividualActionPlanId = UUID.randomUUID();
    businessIndividualCaseTypeOverride.setActionPlanId(businessIndividualActionPlanId);

    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(Tag.mps.name());
    collectionExerciseEvent.setTimestamp(eventTriggerDate);
    UUID collectionExerciseEventId = UUID.fromString("ba6a92c1-9869-41ca-b0d8-12c27fc30e23");
    collectionExerciseEvent.setId(collectionExerciseEventId);

    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(
            exercisePk, businessSampleType))
        .thenReturn(businessCaseTypeOverride);
    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(
            exercisePk, businessIndividualSampleType))
        .thenReturn(businessIndividualCaseTypeOverride);

    actionRuleCreators.add(actionRuleCreator);
    actionRuleCreators.add(actionRuleCreator2);

    // When
    eventService.createActionRulesForEvent(collectionExerciseEvent, collex);

    // Then
    verify(actionRuleCreator)
        .execute(
            eq(collectionExerciseEvent),
            eq(businessCaseTypeOverride),
            eq(businessIndividualCaseTypeOverride),
            eq(surveyDto));
    verify(actionRuleCreator2)
        .execute(
            eq(collectionExerciseEvent),
            eq(businessCaseTypeOverride),
            eq(businessIndividualCaseTypeOverride),
            eq(surveyDto));
  }

  private List<Event> createEventList(Tag... tags) {
    return Arrays.stream(tags).map(EventServiceImplTest::createEvent).collect(Collectors.toList());
  }

  @Test
  public void givenNoEventsWhenScheduledIsCheckedThenFalse() throws CTPException {
    UUID collexUuid = UUID.randomUUID();
    when(eventRepository.findByCollectionExerciseId(collexUuid)).thenReturn(new ArrayList<>());

    boolean scheduled = this.eventService.isScheduled(collexUuid);

    assertFalse(scheduled);
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
}
