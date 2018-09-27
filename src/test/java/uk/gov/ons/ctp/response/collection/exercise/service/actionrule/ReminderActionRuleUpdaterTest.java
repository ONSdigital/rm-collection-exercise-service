package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
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
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@RunWith(MockitoJUnitRunner.class)
public class ReminderActionRuleUpdaterTest {
  private static final UUID EXERCISE_ID = UUID.randomUUID();

  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private ActionSvcClient actionSvcClient;
  @Mock private SurveySvcClient surveyService;

  @Spy private ReminderSuffixGenerator reminderSuffixGenerator;
  @Spy private ActionRulesFilter actionRulesFilter;

  @InjectMocks private ReminderActionRuleUpdater updater;

  @Test
  public void isAnActionRuleUpdater() {
    assertThat(updater, instanceOf(ActionRuleUpdater.class));
  }

  @Test
  public void doNothingIfNotReminder() throws CTPException {
    final Event event = new Event();
    event.setTag(Tag.employment.name());

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
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(Tag.reminder.name());
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    updater.execute(event);
    verify(actionSvcClient, times(0))
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
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

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(tag.name());
    event.setTimestamp(eventTriggerDate);

    final UUID inactiveActionPlanId = UUID.fromString("29f312e4-fe2e-4042-97c6-98e7d48cacfa");
    final UUID activeActionPlanId = UUID.fromString("1795efdf-9961-40eb-b22a-db4b3612c1f3");

    final HashMap<String, String> inactiveEnrolmentSelector = new HashMap<>();
    inactiveEnrolmentSelector.put("activeEnrolment", "false");

    final ActionPlanDTO inactiveActionPlanDTO = new ActionPlanDTO();
    inactiveActionPlanDTO.setSelectors(inactiveEnrolmentSelector);
    inactiveActionPlanDTO.setId(inactiveActionPlanId);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), false))
        .thenReturn(inactiveActionPlanDTO);

    final HashMap<String, String> activeEnrolmentSelector = new HashMap<>();
    activeEnrolmentSelector.put("activeEnrolment", "true");

    final ActionPlanDTO activeActionPlanDTO = new ActionPlanDTO();
    activeActionPlanDTO.setSelectors(activeEnrolmentSelector);
    activeActionPlanDTO.setId(activeActionPlanId);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(activeActionPlanDTO);

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    final String actionRuleSuffix = reminderSuffixGenerator.getReminderSuffix(tag.name());
    final UUID actionRuleId1 = UUID.fromString("7186077b-809f-46c7-a0ba-43139d3efa23");
    final ActionRuleDTO actionRuleDTO1 =
        createActionRuleDTO(actionRuleId1, ActionType.BSRE, actionRuleSuffix);
    final List<ActionRuleDTO> biActionRules = Arrays.asList(actionRuleDTO1);
    when(actionSvcClient.getActionRulesForActionPlan(activeActionPlanId)).thenReturn(biActionRules);

    final UUID actionRuleId2 = UUID.fromString("012cda1e-916a-4182-8d9a-cc66e32f8860");
    final ActionRuleDTO actionRuleDTO2 =
        createActionRuleDTO(actionRuleId2, ActionType.BSRL, actionRuleSuffix);
    final List<ActionRuleDTO> bActionRuleDTOs = Arrays.asList(actionRuleDTO2);
    when(actionSvcClient.getActionRulesForActionPlan(inactiveActionPlanId))
        .thenReturn(bActionRuleDTOs);

    updater.execute(event);

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

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(Tag.reminder.name());
    event.setTimestamp(eventTriggerDate);

    final UUID inactiveActionPlanId = UUID.fromString("29f312e4-fe2e-4042-97c6-98e7d48cacfa");
    final UUID activeActionPlanId = UUID.fromString("1795efdf-9961-40eb-b22a-db4b3612c1f3");

    final HashMap<String, String> inactiveEnrolmentSelector = new HashMap<>();
    inactiveEnrolmentSelector.put("activeEnrolment", "false");

    final ActionPlanDTO inactiveActionPlanDTO = new ActionPlanDTO();
    inactiveActionPlanDTO.setSelectors(inactiveEnrolmentSelector);
    inactiveActionPlanDTO.setId(inactiveActionPlanId);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), false))
        .thenReturn(inactiveActionPlanDTO);

    final HashMap<String, String> activeEnrolmentSelector = new HashMap<>();
    activeEnrolmentSelector.put("activeEnrolment", "true");

    final ActionPlanDTO activeActionPlanDTO = new ActionPlanDTO();
    activeActionPlanDTO.setSelectors(activeEnrolmentSelector);
    activeActionPlanDTO.setId(activeActionPlanId);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(activeActionPlanDTO);

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyType.Business);
    survey.setShortName("QBS");
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    final UUID actionRuleId1 = UUID.fromString("7186077b-809f-46c7-a0ba-43139d3efa23");
    final ActionRuleDTO actionRuleDTO1 =
        createActionRuleDTO(actionRuleId1, ActionType.BSRE, "QBSREME+1");
    final UUID actionRuleId2 = UUID.fromString("14639bd2-b569-4611-b42e-f20448d40272");
    final ActionRuleDTO actionRuleDTO2 =
        createActionRuleDTO(actionRuleId2, ActionType.BSRE, "QBSREME+3");
    final List<ActionRuleDTO> biActionRules = Arrays.asList(actionRuleDTO1, actionRuleDTO2);
    when(actionSvcClient.getActionRulesForActionPlan(activeActionPlanId)).thenReturn(biActionRules);

    final UUID actionRuleId3 = UUID.fromString("50acbf7c-ffbd-4428-b200-5cf62b4de99a");
    final ActionRuleDTO actionRuleDTO3 =
        createActionRuleDTO(actionRuleId3, ActionType.BSRL, "QBSREMF+1");
    final UUID actionRuleId4 = UUID.fromString("ef3bf58b-18f7-4697-afe5-46a236256dc7");
    final ActionRuleDTO actionRuleDTO4 =
        createActionRuleDTO(actionRuleId4, ActionType.BSRL, "QBSREMF+2");
    final List<ActionRuleDTO> bActionRuleDTOs = Arrays.asList(actionRuleDTO3, actionRuleDTO4);
    when(actionSvcClient.getActionRulesForActionPlan(inactiveActionPlanId))
        .thenReturn(bActionRuleDTOs);

    updater.execute(event);

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
