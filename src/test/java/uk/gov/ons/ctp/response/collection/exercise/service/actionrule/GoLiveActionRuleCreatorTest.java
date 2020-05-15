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
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
public class GoLiveActionRuleCreatorTest {

  private static final String EXERCISE_REF = "201802";
  private static final String SURVEY_SHORT_NAME = "TEST_SURVEY";
  @Mock private ActionSvcClient actionSvcClient;

  @Mock private SurveySvcClient surveyService;

  @InjectMocks private GoLiveActionRuleCreator goLiveActionRuleCreator;
  private static final UUID BUSINESS_INDIVIDUAL_ACTION_PLAN_ID = UUID.randomUUID();
  private static final int EXERCISE_PK = 6433;
  private static final UUID EXERCISE_ID = UUID.randomUUID();

  @Test
  public void isActionRuleCreator() {
    assertThat(goLiveActionRuleCreator, instanceOf(ActionRuleCreator.class));
  }

  @Test
  public void doNothingIfNotGoLiveEvent() throws CTPException {
    final Event collectionExerciseEvent = createCollectionExerciseEvent(Tag.mps.name(), null, null);

    goLiveActionRuleCreator.execute(collectionExerciseEvent);

    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() throws CTPException {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Social);

    final CollectionExercise collex = new CollectionExercise();
    final Event event = createCollectionExerciseEvent(Tag.go_live.name(), null, collex);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    goLiveActionRuleCreator.execute(event);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void testCreateCorrectActionRulesForGoLiveEvent() throws CTPException {
    // Given
    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    String tag = Tag.go_live.name();
    CollectionExercise collex = createCollectionExercise();
    Event collectionExerciseEvent = createCollectionExerciseEvent(tag, eventTriggerDate, collex);

    SurveyDTO survey = new SurveyDTO();
    survey.setShortName(SURVEY_SHORT_NAME);
    survey.setSurveyType(SurveyType.Business);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    ActionPlanDTO actionPlan = new ActionPlanDTO();
    actionPlan.setId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(actionPlan);

    OffsetDateTime eventTriggerOffsetDateTime =
        OffsetDateTime.ofInstant(eventTriggerDate.toInstant(), ZoneId.systemDefault());
    when(actionSvcClient.createActionRule(
            anyString(),
            anyString(),
            eq(ActionType.BSNE),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID)))
        .thenReturn(new ActionRuleDTO());

    // When
    goLiveActionRuleCreator.execute(collectionExerciseEvent);

    // Then
    verify(actionSvcClient)
        .createActionRule(
            eq(SURVEY_SHORT_NAME + "NOTE"),
            eq(SURVEY_SHORT_NAME + " Notification Email " + EXERCISE_REF),
            eq(ActionType.BSNE),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID));
  }

  private Event createCollectionExerciseEvent(
      final String tag, final Timestamp eventTriggerDate, final CollectionExercise collex) {
    final Event collectionExerciseEvent = new Event();
    final UUID collectionExerciseEventId = UUID.fromString("ba6a92c1-9869-41ca-b0d8-12c27fc30e24");
    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(tag);
    collectionExerciseEvent.setTimestamp(eventTriggerDate);
    collectionExerciseEvent.setId(collectionExerciseEventId);

    return collectionExerciseEvent;
  }

  private CollectionExercise createCollectionExercise() {
    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    collex.setExercisePK(GoLiveActionRuleCreatorTest.EXERCISE_PK);
    collex.setExerciseRef(EXERCISE_REF);
    return collex;
  }
}
