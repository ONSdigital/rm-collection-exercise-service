package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@RunWith(MockitoJUnitRunner.class)
public class ReminderActionRuleCreatorTest {

  private static final String SURVEY_SHORT_NAME = "TEST_SURVEY";
  private static final String EXERCISE_REF = "201809";
  private static final UUID BUSINESS_ACTION_PLAN_ID = UUID.randomUUID();
  private static final UUID BUSINESS_INDIVIDUAL_ACTION_PLAN_ID = UUID.randomUUID();
  private static final int EXERCISE_PK = 6433;
  @Mock private ActionSvcClient actionSvcClient;
  @Mock private CaseTypeOverrideRepository caseTypeOverrideRepo;
  @Spy private ReminderSuffixGenerator reminderSuffix;
  @InjectMocks private ReminderActionRuleCreator reminderActionRuleCreator;

  @Test
  public void isActionRuleCreator() {
    assertThat(reminderActionRuleCreator, instanceOf(ActionRuleCreator.class));
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Social);

    reminderActionRuleCreator.execute(
        new Event(), new CaseTypeOverride(), new CaseTypeOverride(), survey);
    verify(actionSvcClient, times(0))
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfNotReminderEvent() {
    String tag = Tag.go_live.name();
    Event collectionExerciseEvent = new Event();
    CollectionExercise collex = new CollectionExercise();

    collectionExerciseEvent.setTag(tag);
    collectionExerciseEvent.setCollectionExercise(collex);

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    reminderActionRuleCreator.execute(
        collectionExerciseEvent, new CaseTypeOverride(), new CaseTypeOverride(), survey);
    verify(actionSvcClient, times(0))
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void testCreateCorrectActionRulesForReminderEvent() {
    // Given
    testReminderTagCreatesActionRules(Tag.reminder.name(), "+1");
  }

  /** Test correct action rules are created for new mps event */
  @Test
  public void testCreateCorrectActionRulesForReminder2Event() {
    // Given
    testReminderTagCreatesActionRules(Tag.reminder2.name(), "+2");
  }

  /** Test correct action rules are created for new mps event */
  @Test
  public void testCreateCorrectActionRulesForReminder3Event() {
    // Given
    testReminderTagCreatesActionRules(Tag.reminder3.name(), "+3");
  }

  private Event createCollectionExerciseEvent(
      final String tag, final Timestamp eventTriggerDate, final CollectionExercise collex) {
    Event collectionExerciseEvent = new Event();
    UUID collectionExerciseEventId = UUID.fromString("ba6a92c1-9869-41ca-b0d8-12c27fc30e23");
    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(tag);
    collectionExerciseEvent.setTimestamp(eventTriggerDate);
    collectionExerciseEvent.setId(collectionExerciseEventId);

    return collectionExerciseEvent;
  }

  private CollectionExercise createCollectionExercise() {
    CollectionExercise collex = new CollectionExercise();
    collex.setExercisePK(EXERCISE_PK);
    collex.setExerciseRef(EXERCISE_REF);
    return collex;
  }

  private void testReminderTagCreatesActionRules(final String tag, final String suffixNumber) {
    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    CollectionExercise collectionExercise = createCollectionExercise();
    Event collectionExerciseEvent =
        createCollectionExerciseEvent(tag, eventTriggerDate, collectionExercise);

    CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    businessCaseTypeOverride.setActionPlanId(BUSINESS_ACTION_PLAN_ID);

    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(EXERCISE_PK, "B"))
        .thenReturn(businessCaseTypeOverride);

    CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);

    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(EXERCISE_PK, "BI"))
        .thenReturn(businessIndividualCaseTypeOverride);

    OffsetDateTime eventTriggerOffsetDateTime =
        OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault());

    final SurveyDTO survey = new SurveyDTO();
    survey.setShortName(SURVEY_SHORT_NAME);
    survey.setSurveyType(SurveyType.Business);

    when(actionSvcClient.createActionRule(
            anyString(),
            anyString(),
            eq(ActionType.BSRE),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID)))
        .thenReturn(new ActionRuleDTO());
    when(actionSvcClient.createActionRule(
            anyString(),
            anyString(),
            eq(ActionType.BSRL),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_ACTION_PLAN_ID)))
        .thenReturn(new ActionRuleDTO());

    // When
    reminderActionRuleCreator.execute(
        collectionExerciseEvent,
        businessCaseTypeOverride,
        businessIndividualCaseTypeOverride,
        survey);

    // Then
    verify(actionSvcClient)
        .createActionRule(
            eq(SURVEY_SHORT_NAME + "REME" + suffixNumber),
            eq(SURVEY_SHORT_NAME + " Reminder Email " + EXERCISE_REF),
            eq(ActionType.BSRE),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID));
    verify(actionSvcClient)
        .createActionRule(
            eq(SURVEY_SHORT_NAME + "REMF" + suffixNumber),
            eq(SURVEY_SHORT_NAME + " Reminder File " + EXERCISE_REF),
            eq(ActionType.BSRL),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_ACTION_PLAN_ID));
  }
}
