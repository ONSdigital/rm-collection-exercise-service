package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import static org.hamcrest.CoreMatchers.instanceOf;
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
import java.util.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@RunWith(MockitoJUnitRunner.class)
public class ReminderActionRuleUpdaterTest {
  private static final String MULTIPLE_BSRE_ACTION_RULES_FOUND_EXCEPTION =
      "Multiple BSRE Action Rules Found for Action Plan %s, remove all but 1 before continuing";
  private static final String MULTIPLE_BSRL_ACTION_RULES_FOUND_EXCEPTION =
      "Multiple BSRL Action Rules Found for Action Plan %s, remove all but 1 before continuing";
  private static final String ACTION_PLAN_NOT_FOUND_EXCEPTION = "Action Plan %s not found";
  private static final UUID ACTION_PLAN_ID =
      UUID.fromString("31cbf9ee-4c56-4a81-958b-4b952fc5cc2d");
  private static final String NO_BSRE_ACTION_RULES_FOUND_EXCEPTION =
      "No BSRE Action Rules Found for Action Plan %s, please create one instead";
  private static final String NO_BSRL_ACTION_RULES_FOUND_EXCEPTION =
      "No BSRL Action Rules Found for Action Plan %s, please create one instead";

  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private ActionSvcClient actionSvcClient;

  @Spy private ReminderSuffixGenerator reminderSuffixGenerator;

  @InjectMocks private ReminderActionRuleUpdater updater;

  @Test
  public void isAnActionRuleUpdater() {
    assertThat(updater, instanceOf(ActionRuleUpdater.class));
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() throws CTPException {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Social);

    updater.execute(new Event(), new CaseTypeOverride(), new CaseTypeOverride(), survey);
    verify(actionSvcClient, times(0))
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void doNothingIfNotReminder() throws CTPException {
    final Event event = new Event();
    event.setTag(Tag.employment.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    updater.execute(event, null, new CaseTypeOverride(), survey);

    verify(actionSvcClient, never())
        .updateActionRule(
            any(UUID.class), anyString(), anyString(), any(OffsetDateTime.class), anyInt());
  }

  @Test
  public void raiseCTPExceptionIfNoActionPlanFound() throws CTPException {
    final Event event = new Event();
    event.setTag(Tag.reminder.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(ACTION_PLAN_ID);

    when(actionSvcClient.getActionRulesForActionPlan(ACTION_PLAN_ID)).thenReturn(null);

    thrown.expect(CTPException.class);
    thrown.expectMessage(String.format(ACTION_PLAN_NOT_FOUND_EXCEPTION, ACTION_PLAN_ID.toString()));

    updater.execute(event, null, businessIndividualCaseTypeOverride, survey);
  }

  @Test
  public void raiseCTPExceptionNoBSREActionRulesFound() throws CTPException {
    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(NO_BSRE_ACTION_RULES_FOUND_EXCEPTION, ACTION_PLAN_ID.toString()));

    final Event event = new Event();
    event.setTag(Tag.reminder.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(ACTION_PLAN_ID);

    final ActionRuleDTO actionRuleDTO = createActionRuleDTO(null, ActionType.BSRL, "+1");
    final List<ActionRuleDTO> actionRuleDTOs = Collections.singletonList(actionRuleDTO);

    when(actionSvcClient.getActionRulesForActionPlan(ACTION_PLAN_ID)).thenReturn(actionRuleDTOs);

    updater.execute(event, null, businessIndividualCaseTypeOverride, survey);
  }

  @Test
  public void raiseCTPExceptionNoBSRLActionRulesFound() throws CTPException {

    final Event event = new Event();
    event.setTag(Tag.reminder.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    final CaseTypeOverride bCaseTypeOverride = new CaseTypeOverride();
    final UUID bActionPlanId = UUID.fromString("2346ddba-db99-4265-8d11-edbaa462e6a6");
    bCaseTypeOverride.setActionPlanId(bActionPlanId);
    final List<ActionRuleDTO> bActionRuleDTOs = new ArrayList<>();
    when(actionSvcClient.getActionRulesForActionPlan(bActionPlanId)).thenReturn(bActionRuleDTOs);

    final CaseTypeOverride biCaseTypeOverride = new CaseTypeOverride();
    final UUID biActionPlanId = ACTION_PLAN_ID;
    biCaseTypeOverride.setActionPlanId(biActionPlanId);

    final ActionRuleDTO actionRuleDTO = createActionRuleDTO(null, ActionType.BSRE, "+1");
    final List<ActionRuleDTO> biActionRuleDTOs = Collections.singletonList(actionRuleDTO);
    when(actionSvcClient.getActionRulesForActionPlan(biActionPlanId)).thenReturn(biActionRuleDTOs);

    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(NO_BSRL_ACTION_RULES_FOUND_EXCEPTION, bActionPlanId.toString()));

    updater.execute(event, bCaseTypeOverride, biCaseTypeOverride, survey);
  }

  @Test
  public void raiseCTPExceptionNoBSRLOrBSREActionRulesFound() throws CTPException {
    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(NO_BSRE_ACTION_RULES_FOUND_EXCEPTION, ACTION_PLAN_ID.toString()));

    final Event event = new Event();
    event.setTag(Tag.reminder.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(ACTION_PLAN_ID);

    final ActionRuleDTO actionRuleDTO = createActionRuleDTO(null, ActionType.SOCIALNOT, "+44");
    final List<ActionRuleDTO> actionRuleDTOs = Collections.singletonList(actionRuleDTO);

    when(actionSvcClient.getActionRulesForActionPlan(ACTION_PLAN_ID)).thenReturn(actionRuleDTOs);

    updater.execute(event, null, businessIndividualCaseTypeOverride, survey);
  }

  @Test
  public void raiseCTPExceptionWhenMoreThanOneBSREActionRuleFound() throws CTPException {
    final UUID bsrlActionPlanId = UUID.fromString("a1dbb1c9-1ae0-45ec-9e09-3ef70be7d3d9");
    final UUID bsreActionPlanId = UUID.fromString("3e1bc645-d37f-4055-8d27-4a4f02a7602a");

    final Event event = new Event();
    event.setTag(Tag.reminder.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(bsreActionPlanId);

    final ActionRuleDTO bsreActionRule1 = createActionRuleDTO(null, ActionType.BSRE, "+1");
    final ActionRuleDTO bsreActionRule2 = createActionRuleDTO(null, ActionType.BSRE, "+1");

    final List<ActionRuleDTO> bsreDtos = Arrays.asList(bsreActionRule1, bsreActionRule2);
    when(actionSvcClient.getActionRulesForActionPlan(bsreActionPlanId)).thenReturn(bsreDtos);

    final CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    businessCaseTypeOverride.setActionPlanId(bsrlActionPlanId);

    final ActionRuleDTO bsrlActionRule1 = new ActionRuleDTO();
    bsrlActionRule1.setActionTypeName(ActionType.BSRL);
    bsrlActionRule1.setName("+1");
    final List<ActionRuleDTO> bsrlDtos = Arrays.asList(bsrlActionRule1);
    when(actionSvcClient.getActionRulesForActionPlan(bsrlActionPlanId)).thenReturn(bsrlDtos);

    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(MULTIPLE_BSRE_ACTION_RULES_FOUND_EXCEPTION, bsreActionPlanId));

    updater.execute(event, businessCaseTypeOverride, businessIndividualCaseTypeOverride, survey);
  }

  @Test
  public void raiseCTPExceptionWhenMoreThanOneBSRLActionRuleFound() throws CTPException {
    final UUID bsrlActionPlanId = UUID.fromString("5df76355-f167-4fa8-8e11-e07d67bde5ad");
    final UUID bsreActionPlanId = UUID.fromString("a7c10382-d069-42cb-b3d4-63a3392dab04");

    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(String.format(MULTIPLE_BSRL_ACTION_RULES_FOUND_EXCEPTION, bsrlActionPlanId)));

    final Event event = new Event();
    event.setTag(Tag.reminder.name());

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(bsreActionPlanId);

    final CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    businessCaseTypeOverride.setActionPlanId(bsrlActionPlanId);

    final ActionRuleDTO bsreActionRule = createActionRuleDTO(null, ActionType.BSRE, "+1");
    final List<ActionRuleDTO> bsreDtos = Collections.singletonList(bsreActionRule);
    when(actionSvcClient.getActionRulesForActionPlan(bsreActionPlanId)).thenReturn(bsreDtos);

    final ActionRuleDTO bsrlActionRule1 = createActionRuleDTO(null, ActionType.BSRL, "+1");
    final ActionRuleDTO bsrlActionRule2 = createActionRuleDTO(null, ActionType.BSRL, "+1");
    final List<ActionRuleDTO> bsrlDtos = Arrays.asList(bsrlActionRule1, bsrlActionRule2);
    when(actionSvcClient.getActionRulesForActionPlan(bsrlActionPlanId)).thenReturn(bsrlDtos);

    updater.execute(event, businessCaseTypeOverride, businessIndividualCaseTypeOverride, survey);
  }

  @Test
  public void testActionRulesUpdatedForReminder() throws CTPException {
    testReminderTagUpdatesActionRules(Tag.reminder);
  }

  @Test
  public void testActionRulesUpdatedForReminder2() throws CTPException {
    testReminderTagUpdatesActionRules(Tag.reminder2);
  }

  @Test
  public void testActionRulesUpdatedForReminder3() throws CTPException {
    testReminderTagUpdatesActionRules(Tag.reminder3);
  }

  private void testReminderTagUpdatesActionRules(final Tag tag) throws CTPException {
    final Instant eventTriggerInstant = Instant.now();
    final Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    final Event event = new Event();
    event.setTag(tag.name());
    event.setTimestamp(eventTriggerDate);

    final UUID bActionPlanId = UUID.fromString("29f312e4-fe2e-4042-97c6-98e7d48cacfa");
    final CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    businessCaseTypeOverride.setActionPlanId(bActionPlanId);

    final UUID biActionPlanId = UUID.fromString("1795efdf-9961-40eb-b22a-db4b3612c1f3");
    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(biActionPlanId);

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);

    final String actionRuleSuffix = reminderSuffixGenerator.getReminderSuffix(tag.name());
    final UUID actionRuleId1 = UUID.fromString("7186077b-809f-46c7-a0ba-43139d3efa23");
    final ActionRuleDTO actionRuleDTO1 =
        createActionRuleDTO(actionRuleId1, ActionType.BSRE, actionRuleSuffix);
    final List<ActionRuleDTO> biActionRules = Arrays.asList(actionRuleDTO1);
    when(actionSvcClient.getActionRulesForActionPlan(biActionPlanId)).thenReturn(biActionRules);

    final UUID actionRuleId2 = UUID.fromString("012cda1e-916a-4182-8d9a-cc66e32f8860");
    final ActionRuleDTO actionRuleDTO2 =
        createActionRuleDTO(actionRuleId2, ActionType.BSRL, actionRuleSuffix);
    final List<ActionRuleDTO> bActionRuleDTOs = Arrays.asList(actionRuleDTO2);
    when(actionSvcClient.getActionRulesForActionPlan(bActionPlanId)).thenReturn(bActionRuleDTOs);

    updater.execute(event, businessCaseTypeOverride, businessIndividualCaseTypeOverride, survey);

    verify(actionSvcClient, atLeastOnce())
        .updateActionRule(
            actionRuleId1,
            actionRuleDTO1.getName(),
            actionRuleDTO1.getDescription(),
            OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault()),
            actionRuleDTO1.getPriority());
    verify(actionSvcClient, atLeastOnce())
        .updateActionRule(
            actionRuleId2,
            actionRuleDTO2.getName(),
            actionRuleDTO2.getDescription(),
            OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault()),
            actionRuleDTO2.getPriority());
  }

  @Test
  public void rulesForOtherRemindersOtherThanTheCurrentTagShouldNotBeUpdated() throws CTPException {
    final Instant eventTriggerInstant = Instant.now();
    final Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    final Event event = new Event();
    event.setTag(Tag.reminder.name());
    event.setTimestamp(eventTriggerDate);

    final UUID bActionPlanId = UUID.fromString("29f312e4-fe2e-4042-97c6-98e7d48cacfa");
    final CaseTypeOverride businessCaseTypeOverride = new CaseTypeOverride();
    businessCaseTypeOverride.setActionPlanId(bActionPlanId);

    final UUID biActionPlanId = UUID.fromString("1795efdf-9961-40eb-b22a-db4b3612c1f3");
    final CaseTypeOverride businessIndividualCaseTypeOverride = new CaseTypeOverride();
    businessIndividualCaseTypeOverride.setActionPlanId(biActionPlanId);

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);
    survey.setShortName("QBS");

    final UUID actionRuleId1 = UUID.fromString("7186077b-809f-46c7-a0ba-43139d3efa23");
    final ActionRuleDTO actionRuleDTO1 =
        createActionRuleDTO(actionRuleId1, ActionType.BSRE, "QBSREME+1");
    final UUID actionRuleId2 = UUID.fromString("14639bd2-b569-4611-b42e-f20448d40272");
    final ActionRuleDTO actionRuleDTO2 =
        createActionRuleDTO(actionRuleId2, ActionType.BSRE, "QBSREME+3");
    final List<ActionRuleDTO> biActionRules = Arrays.asList(actionRuleDTO1, actionRuleDTO2);
    when(actionSvcClient.getActionRulesForActionPlan(biActionPlanId)).thenReturn(biActionRules);

    final UUID actionRuleId3 = UUID.fromString("50acbf7c-ffbd-4428-b200-5cf62b4de99a");
    final ActionRuleDTO actionRuleDTO3 =
        createActionRuleDTO(actionRuleId3, ActionType.BSRL, "QBSREMF+1");
    final UUID actionRuleId4 = UUID.fromString("ef3bf58b-18f7-4697-afe5-46a236256dc7");
    final ActionRuleDTO actionRuleDTO4 =
        createActionRuleDTO(actionRuleId4, ActionType.BSRL, "QBSREMF+2");
    final List<ActionRuleDTO> bActionRuleDTOs = Arrays.asList(actionRuleDTO3, actionRuleDTO4);
    when(actionSvcClient.getActionRulesForActionPlan(bActionPlanId)).thenReturn(bActionRuleDTOs);

    updater.execute(event, businessCaseTypeOverride, businessIndividualCaseTypeOverride, survey);

    verify(actionSvcClient, atLeastOnce())
        .updateActionRule(
            actionRuleId1,
            actionRuleDTO1.getName(),
            actionRuleDTO1.getDescription(),
            OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault()),
            actionRuleDTO1.getPriority());

    verify(actionSvcClient, times(0))
        .updateActionRule(eq(actionRuleId2), anyString(), anyString(), any(), anyInt());

    verify(actionSvcClient, atLeastOnce())
        .updateActionRule(
            actionRuleId3,
            actionRuleDTO3.getName(),
            actionRuleDTO3.getDescription(),
            OffsetDateTime.ofInstant(eventTriggerInstant, ZoneId.systemDefault()),
            actionRuleDTO3.getPriority());

    verify(actionSvcClient, never())
        .updateActionRule(eq(actionRuleId4), anyString(), anyString(), any(), anyInt());
  }

  private ActionRuleDTO createActionRuleDTO(
      final UUID actionRuleId, final ActionType actionRuleType, final String actionRuleName) {
    final ActionRuleDTO actionRuleDTO = new ActionRuleDTO();
    actionRuleDTO.setId(actionRuleId);
    actionRuleDTO.setActionTypeName(actionRuleType);
    actionRuleDTO.setName(actionRuleName);
    actionRuleDTO.setPriority(3);
    return actionRuleDTO;
  }
}
