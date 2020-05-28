package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@RunWith(MockitoJUnitRunner.class)
public class NudgeEmailActionTypeRuleGeneratorTest {
  private static final UUID EXERCISE_ID = UUID.randomUUID();

  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private ActionSvcClient actionSvcClient;

  @Spy private NudgeEmailSuffixGenerator nudgeEmailSuffixGenerator;
  @Spy private ActionRulesFilter actionRulesFilter;

  @InjectMocks private NudgeEmailActionTypeRuleGenerator nudgeEmailActionTypeRuleGenerator;

  @Test
  public void givenNotNugeEventThenThrowsException() {
    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(EventService.Tag.reminder.name());
    try {
      nudgeEmailActionTypeRuleGenerator.getActionRuleDTOS(event, collex);

      fail("Incorrect Tag");
    } catch (IllegalArgumentException | CTPException e) {
      // Expected 404
      assertEquals(e.getMessage(), "Tag reminder is not a nudge email");
    }
  }

  @Test
  public void givenNoExistingBSNUEActionRuleThrowsException() throws CTPException {
    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(EventService.Tag.nudge_email_0.name());

    final UUID inactiveActionPlanId = UUID.fromString("29f312e4-fe2e-4042-97c6-98e7d48cacfa");
    final UUID activeActionPlanId = UUID.fromString("1795efdf-9961-40eb-b22a-db4b3612c1f3");

    final HashMap<String, String> inactiveEnrolmentSelector = new HashMap<>();
    inactiveEnrolmentSelector.put("activeEnrolment", "false");

    final ActionPlanDTO inactiveActionPlanDTO =
        getActionPlanDTO(inactiveActionPlanId, inactiveEnrolmentSelector);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), false))
        .thenReturn(inactiveActionPlanDTO);

    final HashMap<String, String> activeEnrolmentSelector = new HashMap<>();
    activeEnrolmentSelector.put("activeEnrolment", "true");

    final ActionPlanDTO activeActionPlanDTO =
        getActionPlanDTO(activeActionPlanId, activeEnrolmentSelector);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(activeActionPlanDTO);

    try {
      nudgeEmailActionTypeRuleGenerator.getActionRuleDTOS(event, collex);

      Assert.fail("No BSNUE Action Rules Found");
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(CTPException.Fault.RESOURCE_NOT_FOUND));
    }
  }

  @Test
  public void givenNoIssuesGeneratorShouldReturnActionRulesDtoList() throws CTPException {
    final Instant eventTriggerInstant = Instant.now();
    final Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(EventService.Tag.nudge_email_0.name());
    event.setTimestamp(eventTriggerDate);

    final String actionRuleSuffix =
        nudgeEmailSuffixGenerator.getNudgeEmailNumber(EventService.Tag.nudge_email_0.name());
    final UUID actionRuleId1 = UUID.fromString("7186077b-809f-46c7-a0ba-43139d3efa23");
    final ActionRuleDTO actionRuleDTO1 =
        createActionRuleDTO(actionRuleId1, ActionType.BSNUE, actionRuleSuffix);
    final UUID actionRuleId2 = UUID.fromString("012cda1e-916a-4182-8d9a-cc66e32f8860");
    final ActionRuleDTO actionRuleDTO2 =
        createActionRuleDTO(actionRuleId2, ActionType.BSNUL, actionRuleSuffix);
    final List<ActionRuleDTO> bActionRuleDTOs = Arrays.asList(actionRuleDTO2);
    final List<ActionRuleDTO> biActionRules = Arrays.asList(actionRuleDTO1);
    final UUID inactiveActionPlanId = UUID.fromString("29f312e4-fe2e-4042-97c6-98e7d48cacfa");
    final UUID activeActionPlanId = UUID.fromString("1795efdf-9961-40eb-b22a-db4b3612c1f3");
    final HashMap<String, String> inactiveEnrolmentSelector = new HashMap<>();
    inactiveEnrolmentSelector.put("activeEnrolment", "false");
    final ActionPlanDTO inactiveActionPlanDTO =
        getActionPlanDTO(inactiveActionPlanId, inactiveEnrolmentSelector);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), false))
        .thenReturn(inactiveActionPlanDTO);
    final HashMap<String, String> activeEnrolmentSelector = new HashMap<>();
    activeEnrolmentSelector.put("activeEnrolment", "true");

    final ActionPlanDTO activeActionPlanDTO =
        getActionPlanDTO(activeActionPlanId, activeEnrolmentSelector);
    when(actionSvcClient.getActionPlanBySelectorsBusiness(EXERCISE_ID.toString(), true))
        .thenReturn(activeActionPlanDTO);
    when(actionSvcClient.getActionRulesForActionPlan(activeActionPlanId)).thenReturn(biActionRules);
    when(actionSvcClient.getActionRulesForActionPlan(inactiveActionPlanId))
        .thenReturn(bActionRuleDTOs);
    List<ActionRuleDTO> actualActionRuleDTO =
        nudgeEmailActionTypeRuleGenerator.getActionRuleDTOS(event, collex);
    assertThat(actualActionRuleDTO, hasItem(actionRuleDTO1));
    assertThat(actualActionRuleDTO, hasItem(actionRuleDTO2));
  }

  private ActionPlanDTO getActionPlanDTO(
      UUID inactiveActionPlanId, HashMap<String, String> inactiveEnrolmentSelector) {
    final ActionPlanDTO inactiveActionPlanDTO = new ActionPlanDTO();
    inactiveActionPlanDTO.setSelectors(inactiveEnrolmentSelector);
    inactiveActionPlanDTO.setId(inactiveActionPlanId);
    return inactiveActionPlanDTO;
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
