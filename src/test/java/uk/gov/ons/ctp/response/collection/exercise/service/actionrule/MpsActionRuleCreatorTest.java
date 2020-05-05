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
public class MpsActionRuleCreatorTest {

  private static final String SURVEY_SHORT_NAME = "TEST_SURVEY";
  private static final String EXERCISE_REF = "201808";
  @Mock private ActionSvcClient actionSvcClient;
  @Mock private SurveySvcClient surveyService;

  @InjectMocks private MpsActionRuleCreator mpsActionRuleCreator;
  private static final UUID BUSINESS_ACTION_PLAN_ID = UUID.randomUUID();
  private static final UUID EXERCISE_ID = UUID.randomUUID();
  private static final int EXERCISE_PK = 6433;

  @Test
  public void isActionRuleCreator() {
    assertThat(mpsActionRuleCreator, instanceOf(ActionRuleCreator.class));
  }

  @Test
  public void doNothingIfNotMpsEvent() throws CTPException {
    final Event collectionExerciseEvent =
        createCollectionExerciseEvent(Tag.go_live.name(), null, null);

    mpsActionRuleCreator.execute(collectionExerciseEvent);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() throws CTPException {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Social);

    final CollectionExercise collectionExercise = createCollectionExercise();
    final Event event = createCollectionExerciseEvent(Tag.mps.name(), null, collectionExercise);
    when(surveyService.getSurveyForCollectionExercise(collectionExercise)).thenReturn(survey);

    mpsActionRuleCreator.execute(event);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void testCreateCorrectActionRulesForMPSEvent() throws CTPException {
    // Given
    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    String tag = Tag.mps.name();
    CollectionExercise collex = createCollectionExercise();
    Event collectionExerciseEvent = createCollectionExerciseEvent(tag, eventTriggerDate, collex);

    String businessSampleType = "B";

    SurveyDTO survey = new SurveyDTO();
    survey.setShortName(SURVEY_SHORT_NAME);
    survey.setSurveyType(SurveyType.Business);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    final ActionPlanDTO actionPlan = new ActionPlanDTO();
    actionPlan.setId(BUSINESS_ACTION_PLAN_ID);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), false))
        .thenReturn(actionPlan);

    OffsetDateTime eventTriggerOffsetDateTime =
        OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault());
    when(actionSvcClient.createActionRule(
            anyString(),
            anyString(),
            eq(ActionType.BSNL),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_ACTION_PLAN_ID)))
        .thenReturn(new ActionRuleDTO());

    // When
    mpsActionRuleCreator.execute(collectionExerciseEvent);

    // Then
    verify(actionSvcClient)
        .createActionRule(
            eq(SURVEY_SHORT_NAME + "NOTF"),
            eq(SURVEY_SHORT_NAME + " Notification File " + EXERCISE_REF),
            eq(ActionType.BSNL),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_ACTION_PLAN_ID));
  }

  private Event createCollectionExerciseEvent(
      final String tag, final Timestamp eventTriggerDate, final CollectionExercise collex) {
    final Event collectionExerciseEvent = new Event();
    final UUID collectionExerciseEventId = UUID.fromString("ba6a92c1-9869-41ca-b0d8-12c27fc30e25");
    collectionExerciseEvent.setCollectionExercise(collex);
    collectionExerciseEvent.setTag(tag);
    collectionExerciseEvent.setTimestamp(eventTriggerDate);
    collectionExerciseEvent.setId(collectionExerciseEventId);

    return collectionExerciseEvent;
  }

  private CollectionExercise createCollectionExercise() {
    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    collex.setExercisePK(MpsActionRuleCreatorTest.EXERCISE_PK);
    collex.setExerciseRef(EXERCISE_REF);
    return collex;
  }
}
