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
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@RunWith(MockitoJUnitRunner.class)
public class GoLiveActionRuleCreatorTest {

  public static final String EXERCISE_REF = "201802";
  public static final String SURVEY_SHORT_NAME = "TEST_SURVEY";
  @Mock private ActionSvcClient actionSvcClient;

  @Mock private CaseTypeOverrideRepository caseTypeOverrideRepo;

  @InjectMocks private GoLiveActionRuleCreator goLiveActionRuleCreator;
  public static final UUID BUSINESS_INDIVIDUAL_ACTION_PLAN_ID = UUID.randomUUID();
  public static final int EXERCISE_PK = 6433;

  @Test
  public void isActionRuleCreator() {
    assertThat(goLiveActionRuleCreator, instanceOf(ActionRuleCreator.class));
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Social);

    goLiveActionRuleCreator.execute(
        new Event(), new CaseTypeOverride(), new CaseTypeOverride(), survey);
    verify(actionSvcClient, times(0))
        .createActionRule(anyString(), anyString(), anyString(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfNotGoLiveEvent() {
    String tag = EventService.Tag.mps.name();
    Event collectionExerciseEvent = new Event();
    CollectionExercise collex = new CollectionExercise();
    SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    collectionExerciseEvent.setTag(tag);
    collectionExerciseEvent.setCollectionExercise(collex);

    CaseTypeOverride bCaseTypeOverride = new CaseTypeOverride();
    goLiveActionRuleCreator.execute(collectionExerciseEvent, bCaseTypeOverride, null, survey);
    verify(actionSvcClient, times(0))
        .createActionRule(anyString(), anyString(), anyString(), any(), anyInt(), any());
  }

  @Test
  public void testCreateCorrectActionRulesForGoLiveEvent() {
    // Given
    Instant eventTriggerInstant = Instant.now();
    Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    String tag = EventService.Tag.go_live.name();
    CollectionExercise collex = createCollectionExercise();
    Event collectionExerciseEvent = createCollectionExerciseEvent(tag, eventTriggerDate, collex);

    SurveyDTO survey = new SurveyDTO();
    survey.setShortName(SURVEY_SHORT_NAME);
    survey.setSurveyType(SurveyType.Business);

    CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);

    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(EXERCISE_PK, "BI"))
        .thenReturn(businessIndividualCaseTypeOverride);

    OffsetDateTime eventTriggerOffsetDateTime =
        OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault());
    when(actionSvcClient.createActionRule(
            anyString(),
            anyString(),
            eq("BSNE"),
            eq(eventTriggerOffsetDateTime),
            eq(3),
            eq(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID)))
        .thenReturn(new ActionRuleDTO());

    // When
    goLiveActionRuleCreator.execute(
        collectionExerciseEvent, null, businessIndividualCaseTypeOverride, survey);

    // Then
    verify(actionSvcClient)
        .createActionRule(
            eq(SURVEY_SHORT_NAME + "NOTE"),
            eq(SURVEY_SHORT_NAME + " Notification Email " + EXERCISE_REF),
            eq("BSNE"),
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
    collex.setExercisePK(GoLiveActionRuleCreatorTest.EXERCISE_PK);
    collex.setExerciseRef(EXERCISE_REF);
    return collex;
  }
}
