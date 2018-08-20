package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@RunWith(MockitoJUnitRunner.class)
public class GoLiveActionRuleUpdaterTest {
  private static final String MULTIPLE_ACTION_RULES_FOUND_EXCEPTION =
      "Multiple BSNE Action Rules Found for Action Plan %s, remove all but 1 before continuing";
  private static final String NO_BSNE_ACTION_RULES_FOUND_EXCEPTION =
      "No BSNE Action Rules Found for Action Plan %s, please create one instead";
  private static final UUID BUSINESS_INDIVIDUAL_ACTION_PLAN_ID = UUID.randomUUID();
  private static final UUID ACTION_RULE_ID = UUID.randomUUID();
  private static final UUID EXERCISE_ID = UUID.randomUUID();

  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private ActionSvcClient actionSvcClient;
  @Mock private SurveyService surveyService;

  @Spy private ActionRulesFilter actionRulesFilter;

  @InjectMocks private GoLiveActionRuleUpdater updater;

  public void isAnActionRuleUpdater() {
    assertThat(updater, instanceOf(ActionRuleUpdater.class));
  }

  @Test
  public void doNothingIfNotGoLiveEvent() throws CTPException {
    final Event event = new Event();
    event.setTag(Tag.mps.name());

    updater.execute(event);

    verify(actionSvcClient, never())
        .updateActionRule(
            any(UUID.class), anyString(), anyString(), any(OffsetDateTime.class), anyInt());
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() throws CTPException {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Social);

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(Tag.go_live.name());
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    updater.execute(event);
    verify(actionSvcClient, never())
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  public void raiseCTPExceptionIfNoBSNEActionRulesFound() throws CTPException {
    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(
            NO_BSNE_ACTION_RULES_FOUND_EXCEPTION, BUSINESS_INDIVIDUAL_ACTION_PLAN_ID.toString()));

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(Tag.go_live.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    final ActionRuleDTO actionRuleDTO = new ActionRuleDTO();
    actionRuleDTO.setActionTypeName(ActionType.BSNL);
    final List<ActionRuleDTO> actionRuleDTOs = Collections.singletonList(actionRuleDTO);

    when(actionSvcClient.getActionRulesForActionPlan(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID))
        .thenReturn(actionRuleDTOs);

    updater.execute(event);
  }

  @Test
  public void raiseCTPExceptionIfMultipleBSNEActionRulesFound() throws CTPException {
    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(
            MULTIPLE_ACTION_RULES_FOUND_EXCEPTION, BUSINESS_INDIVIDUAL_ACTION_PLAN_ID.toString()));

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(Tag.go_live.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    final ActionPlanDTO actionPlan = new ActionPlanDTO();
    actionPlan.setId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(actionPlan);

    final ActionRuleDTO actionRuleDTO1 = new ActionRuleDTO();
    actionRuleDTO1.setActionTypeName(ActionType.BSNE);
    final ActionRuleDTO actionRuleDTO2 = new ActionRuleDTO();
    actionRuleDTO2.setActionTypeName(ActionType.BSNE);
    final List<ActionRuleDTO> actionRuleDTOs = Arrays.asList(actionRuleDTO1, actionRuleDTO2);

    when(actionSvcClient.getActionRulesForActionPlan(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID))
        .thenReturn(actionRuleDTOs);

    updater.execute(event);
  }

  @Test
  public void testSuccessfullyChangeActionRule() throws CTPException {
    final Instant eventTriggerInstant = Instant.now();
    final Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(Tag.go_live.name());
    event.setTimestamp(eventTriggerDate);

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    final ActionRuleDTO actionRuleDTO = new ActionRuleDTO();
    actionRuleDTO.setId(ACTION_RULE_ID);
    actionRuleDTO.setActionTypeName(ActionType.BSNE);
    actionRuleDTO.setPriority(3);
    final List<ActionRuleDTO> actionRuleDTOs = Collections.singletonList(actionRuleDTO);

    when(actionSvcClient.getActionRulesForActionPlan(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID))
        .thenReturn(actionRuleDTOs);

    final ActionPlanDTO actionPlan = new ActionPlanDTO();
    actionPlan.setId(BUSINESS_INDIVIDUAL_ACTION_PLAN_ID);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(actionPlan);

    updater.execute(event);

    verify(actionSvcClient, atLeastOnce())
        .updateActionRule(
            ACTION_RULE_ID,
            actionRuleDTO.getName(),
            actionRuleDTO.getDescription(),
            OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault()),
            actionRuleDTO.getPriority());
  }
}
