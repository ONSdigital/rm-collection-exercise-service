package uk.gov.ons.ctp.response.collection.exercise.service.impl;

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
import org.junit.Assert;
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
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.CaseTypeOverrideService;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

/** Class containing tests for EventServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class EventServiceImplTest {
  private static final UUID SURVEY_ID = UUID.fromString("4ca97b1b-de9c-4fed-9898-fac594d1565f");
  private static final UUID BUSINESS_INDIVIDUAL_ACTION_PLAN_ID =
      UUID.fromString("84a3afcd-bb2c-4a89-b8c6-3f117420f047");
  private static final UUID BUSINESS_ACTION_PLAN_ID =
      UUID.fromString("459304df-e4f9-450f-947b-ec7c2c85a1aa");
  private static final UUID COLLECTION_EXERCISE_EVENT_ID =
      UUID.fromString("ba6a92c1-9869-41ca-b0d8-12c27fc30e23");
  private static final UUID COLLEX_UUID = UUID.fromString("f03206ee-137d-41e3-af5c-2dea393bb360");
  private static final String BUSINESS_SAMPLE_TYPE = "B";
  private static final String BUSINESS_INDIVIDUAL_SAMPLE_TYPE = "BI";
  private static final int EXERCISE_PK = 6433;

  @Mock private SurveySvcClient surveySvcClient;

  @Mock private CaseTypeOverrideService caseTypeOverrideService;

  @Mock private CollectionExerciseService collectionExerciseService;

  @Mock private EventValidator eventValidator;

  @Mock private ActionRuleCreator actionRuleCreator;

  @Mock private ActionRuleCreator actionRuleCreator2;

  @Mock private ActionRuleUpdater actionRuleUpdater;

  @Mock private ActionRuleUpdater actionRuleUpdater2;

  @Mock private EventRepository eventRepository;

  @Spy private List<ActionRuleCreator> actionRuleCreators = new ArrayList<>();

  @Spy private List<ActionRuleUpdater> actionRuleUpdaters = new ArrayList<>();

  @InjectMocks private EventServiceImpl eventService;

  /* Given collection excercise does not exist When event is created Then exception is thrown */

  private static Event createEvent(Tag tag) {
    Timestamp eventTime = new Timestamp(new Date().getTime());
    Event event = new Event();
    event.setTimestamp(eventTime);
    event.setTag(tag.name());

    return event;
  }
  /* Given event already exists When event is created Then exception is thrown */

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

    final SurveyDTO surveyDto = new SurveyDTO();
    surveyDto.setSurveyType(SurveyType.Business);
    when(surveySvcClient.getSurveyForCollectionExercise(collex)).thenReturn(surveyDto);

    CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    businessCaseTypeOverride.setActionPlanId(BUSINESS_ACTION_PLAN_ID);

    CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);

    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(Tag.mps.name());
    collectionExerciseEvent.setTimestamp(eventTriggerDate);
    collectionExerciseEvent.setId(COLLECTION_EXERCISE_EVENT_ID);

    when(caseTypeOverrideService.getCaseTypeOverride(collex, BUSINESS_SAMPLE_TYPE))
        .thenReturn(businessCaseTypeOverride);
    when(caseTypeOverrideService.getCaseTypeOverride(collex, BUSINESS_INDIVIDUAL_SAMPLE_TYPE))
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

  @Test
  public void testNoActionRulesCreatedForNonActionableEvents() throws CTPException {
    // Given
    final Event collectionExerciseEvent = new Event();
    final CollectionExercise collex = new CollectionExercise();

    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(Tag.employment.name());

    // When
    eventService.createActionRulesForEvent(collectionExerciseEvent, collex);

    // Then
    verify(actionRuleCreator, never()).execute(any(), any(), any(), any());
  }

  @Test
  public void testNoActionRulesCreatedForNonBusinessesSurveyEvents() throws CTPException {
    // Given
    final Event collectionExerciseEvent = new Event();
    final CollectionExercise collex = new CollectionExercise();
    collex.setSurveyId(SURVEY_ID);

    SurveyDTO surveyDto = new SurveyDTO();
    surveyDto.setSurveyType(SurveyType.Social);
    when(surveySvcClient.getSurveyForCollectionExercise(collex)).thenReturn(surveyDto);

    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(Tag.mps.name());

    // When
    eventService.createActionRulesForEvent(collectionExerciseEvent, collex);

    // Then
    verify(actionRuleCreator, never()).execute(any(), any(), any(), any());
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
    when(eventValidator.validate(existingEvents, existingEvent, collectionExerciseState))
        .thenReturn(false);

    try {
      eventService.updateEvent(collexUuid, Tag.mps.name(), new Date());

      Assert.fail("Validation failed and request was not rejected");
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

    final CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    businessCaseTypeOverride.setActionPlanId(BUSINESS_ACTION_PLAN_ID);

    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);

    when(caseTypeOverrideService.getCaseTypeOverride(collex, BUSINESS_SAMPLE_TYPE))
        .thenReturn(businessCaseTypeOverride);
    when(caseTypeOverrideService.getCaseTypeOverride(collex, BUSINESS_INDIVIDUAL_SAMPLE_TYPE))
        .thenReturn(businessIndividualCaseTypeOverride);

    when(eventRepository.findByCollectionExercise(collex)).thenReturn(existingEvents);
    when(eventValidator.validate(existingEvents, existingEvent, collectionExerciseState))
        .thenReturn(true);

    actionRuleUpdaters.add(actionRuleUpdater);
    actionRuleUpdaters.add(actionRuleUpdater2);

    eventService.updateEvent(COLLEX_UUID, Tag.mps.name(), new Date());

    verify(eventRepository, atLeastOnce()).save(eq(existingEvent));
    verify(actionRuleUpdater, atLeastOnce())
        .execute(
            existingEvent, businessCaseTypeOverride, businessIndividualCaseTypeOverride, survey);
    verify(actionRuleUpdater2, atLeastOnce())
        .execute(
            existingEvent, businessCaseTypeOverride, businessIndividualCaseTypeOverride, survey);
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
    return Arrays.stream(tags).map(EventServiceImplTest::createEvent).collect(Collectors.toList());
  }
}
