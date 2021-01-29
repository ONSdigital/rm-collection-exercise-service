package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO.SurveyType;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

@RunWith(MockitoJUnitRunner.class)
public class ReminderActionRuleCreatorTest {

  private static final String SURVEY_SHORT_NAME = "TEST_SURVEY";
  private static final String EXERCISE_REF = "201809";
  private static final UUID BUSINESS_ACTION_PLAN_ID = UUID.randomUUID();
  private static final UUID BUSINESS_INDIVIDUAL_ACTION_PLAN_ID = UUID.randomUUID();
  private static final int EXERCISE_PK = 6433;
  private static final UUID EXERCISE_ID = UUID.randomUUID();
  @Mock private ActionSvcClient actionSvcClient;
  @Spy private ReminderSuffixGenerator reminderSuffix;
  @InjectMocks private ReminderActionRuleCreator reminderActionRuleCreator;
  @Mock private SurveySvcClient surveyService;

  @Test
  public void isActionRuleCreator() {
    assertThat(reminderActionRuleCreator, instanceOf(ActionRuleCreator.class));
  }

  @Test
  public void doNothingIfNotReminderEvent() throws CTPException {
    final String tag = Tag.go_live.name();
    final Event collectionExerciseEvent = new Event();
    collectionExerciseEvent.setTag(tag);

    reminderActionRuleCreator.execute(collectionExerciseEvent);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfActionDeprecated() throws CTPException {
    when(actionSvcClient.isDeprecated()).thenReturn(true);

    final CollectionExercise collex = new CollectionExercise();
    final Event event = createCollectionExerciseEvent(Tag.reminder.name(), null, collex);

    reminderActionRuleCreator.execute(event);

    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() throws CTPException {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Social);

    final CollectionExercise collex = new CollectionExercise();
    final Event event = createCollectionExerciseEvent(Tag.reminder.name(), null, collex);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    reminderActionRuleCreator.execute(event);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void testCreateCorrectActionRulesForReminderEvent() throws CTPException {
    // Given
    testReminderTagCreatesActionRules(Tag.reminder.name(), "+1");
  }

  /** Test correct action rules are created for new mps event */
  @Test
  public void testCreateCorrectActionRulesForReminder2Event() throws CTPException {
    // Given
    testReminderTagCreatesActionRules(Tag.reminder2.name(), "+2");
  }

  /** Test correct action rules are created for new mps event */
  @Test
  public void testCreateCorrectActionRulesForReminder3Event() throws CTPException {
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

  private void testReminderTagCreatesActionRules(final String tag, final String suffixNumber)
      throws CTPException {
    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    CollectionExercise collectionExercise = createCollectionExercise();
    collectionExercise.setId(EXERCISE_ID);
    Event collectionExerciseEvent =
        createCollectionExerciseEvent(tag, eventTriggerDate, collectionExercise);

    OffsetDateTime eventTriggerOffsetDateTime =
        OffsetDateTime.ofInstant(eventTriggerDate.toInstant(), ZoneId.systemDefault());

    final HashMap<String, String> inactiveEnrolmentSelector = new HashMap<>();
    inactiveEnrolmentSelector.put("activeEnrolment", "false");

    ActionPlanDTO inactiveActionPlanDTO = new ActionPlanDTO();
    inactiveActionPlanDTO.setSelectors(inactiveEnrolmentSelector);
    inactiveActionPlanDTO.setId(BUSINESS_ACTION_PLAN_ID);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), false))
        .thenReturn(inactiveActionPlanDTO);

    final HashMap<String, String> activeEnrolmentSelector = new HashMap<>();
    activeEnrolmentSelector.put("activeEnrolment", "true");

    ActionPlanDTO activeActionPlanDTO = new ActionPlanDTO();
    activeActionPlanDTO.setSelectors(activeEnrolmentSelector);
    activeActionPlanDTO.setId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(activeActionPlanDTO);

    final SurveyDTO survey = new SurveyDTO();
    survey.setShortName(SURVEY_SHORT_NAME);
    survey.setSurveyType(SurveyType.Business);

    when(surveyService.getSurveyForCollectionExercise(collectionExercise)).thenReturn(survey);
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
    reminderActionRuleCreator.execute(collectionExerciseEvent);

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
