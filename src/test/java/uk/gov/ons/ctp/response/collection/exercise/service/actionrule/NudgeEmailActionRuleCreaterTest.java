package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

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
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@RunWith(MockitoJUnitRunner.class)
public class NudgeEmailActionRuleCreaterTest {
  private static final String SURVEY_SHORT_NAME = "TEST_SURVEY";
  private static final String EXERCISE_REF = "201809";
  private static final UUID BUSINESS_ACTION_PLAN_ID = UUID.randomUUID();
  private static final UUID BUSINESS_INDIVIDUAL_ACTION_PLAN_ID = UUID.randomUUID();
  private static final int EXERCISE_PK = 6433;
  private static final UUID EXERCISE_ID = UUID.randomUUID();
  @Mock private ActionSvcClient actionSvcClient;
  @Spy private NudgeEmailSuffixGenerator nudgeEmailSuffixGenerator;
  @InjectMocks private NudgeEmailActionRuleCreator nudgeEmailActionRuleCreator;
  @Mock private SurveySvcClient surveyService;

  @Test
  public void isActionRuleCreator() {
    assertThat(nudgeEmailActionRuleCreator, instanceOf(ActionRuleCreator.class));
  }

  @Test
  public void doNothingIfNotNudgeEvent() throws CTPException {
    final String tag = EventService.Tag.go_live.name();
    final Event collectionExerciseEvent = new Event();
    collectionExerciseEvent.setTag(tag);

    nudgeEmailActionRuleCreator.execute(collectionExerciseEvent);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() throws CTPException {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyDTO.SurveyType.Social);

    final CollectionExercise collex = new CollectionExercise();
    final Event event =
        createCollectionExerciseEvent(EventService.Tag.nudge_email_0.name(), null, collex);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    nudgeEmailActionRuleCreator.execute(event);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void testCreateCorrectActionRulesForNudge1Event() throws CTPException {
    // Given
    testNudgeTagCreatesActionRules(EventService.Tag.nudge_email_0.name(), "+1");
  }

  @Test
  public void testCreateCorrectActionRulesForNudge2Event() throws CTPException {
    // Given
    testNudgeTagCreatesActionRules(EventService.Tag.nudge_email_1.name(), "+2");
  }

  @Test
  public void testCreateCorrectActionRulesForNudge3Event() throws CTPException {
    // Given
    testNudgeTagCreatesActionRules(EventService.Tag.nudge_email_2.name(), "+3");
  }

  @Test
  public void testCreateCorrectActionRulesForNudge4Event() throws CTPException {
    // Given
    testNudgeTagCreatesActionRules(EventService.Tag.nudge_email_3.name(), "+4");
  }

  @Test
  public void testCreateCorrectActionRulesForNudge5Event() throws CTPException {
    // Given
    testNudgeTagCreatesActionRules(EventService.Tag.nudge_email_4.name(), "+5");
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

  private void testNudgeTagCreatesActionRules(final String tag, final String suffixNumber)
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
    survey.setSurveyType(SurveyDTO.SurveyType.Business);

    when(surveyService.getSurveyForCollectionExercise(collectionExercise)).thenReturn(survey);
    when(actionSvcClient.createActionRule(
            anyString(),
            anyString(),
            eq(ActionType.BSNUE),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID)))
        .thenReturn(new ActionRuleDTO());

    // When
    nudgeEmailActionRuleCreator.execute(collectionExerciseEvent);

    // Then
    verify(actionSvcClient)
        .createActionRule(
            eq(SURVEY_SHORT_NAME + "NUDGE" + suffixNumber),
            eq(SURVEY_SHORT_NAME + " Nudge Email " + EXERCISE_REF),
            eq(ActionType.BSNUE),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID));
  }
}
