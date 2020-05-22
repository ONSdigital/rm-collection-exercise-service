package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NudgeEmailActionRuleUpdaterTest {
  private static final UUID EXERCISE_ID = UUID.randomUUID();

  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private ActionSvcClient actionSvcClient;
  @Mock private SurveySvcClient surveyService;
  @Mock private NudgeEmailActionTypeRuleGenerator nudgeEmailActionTypeRuleGenerator;

  @Spy private NudgeEmailSuffixGenerator nudgeSuffixGenerator;

  @InjectMocks private NudgeEmailActionRuleUpdater updater;

  @Test
  public void isAnActionRuleUpdater() {
    assertThat(updater, instanceOf(ActionRuleUpdater.class));
  }

  @Test
  public void doNothingIfNotNudge() throws CTPException {
    final Event event = new Event();
    event.setTag(EventService.Tag.employment.name());

    updater.execute(event);

    verify(actionSvcClient, never())
        .updateActionRule(
            any(UUID.class), anyString(), anyString(), any(OffsetDateTime.class), anyInt());
  }

  @Test
  public void doNothingIfNotBusinessSurveyEvent() throws CTPException {
    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyDTO.SurveyType.Social);

    final CollectionExercise collex = new CollectionExercise();
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(EventService.Tag.nudge_email_0.name());
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    updater.execute(event);
    verify(actionSvcClient, times(0))
        .createActionRule(anyString(), anyString(), any(), any(), anyInt(), any());
  }

  @Test
  public void testActionRulesUpdatedForNudgeEmail0() throws CTPException {
    testNudgeTagUpdatesActionRules(EventService.Tag.nudge_email_0);
  }

  @Test
  public void testActionRulesUpdatedForNudgeEmail1() throws CTPException {
    testNudgeTagUpdatesActionRules(EventService.Tag.nudge_email_1);
  }

  @Test
  public void testActionRulesUpdatedForNudgeEmail2() throws CTPException {
    testNudgeTagUpdatesActionRules(EventService.Tag.nudge_email_2);
  }

  @Test
  public void testActionRulesUpdatedForNudgeEmail3() throws CTPException {
    testNudgeTagUpdatesActionRules(EventService.Tag.nudge_email_3);
  }

  @Test
  public void testActionRulesUpdatedForNudgeEmail4() throws CTPException {
    testNudgeTagUpdatesActionRules(EventService.Tag.nudge_email_4);
  }

  private void testNudgeTagUpdatesActionRules(final EventService.Tag tag) throws CTPException {
    final Instant eventTriggerInstant = Instant.now();
    final Timestamp eventTriggerDate = new Timestamp(eventTriggerInstant.toEpochMilli());

    final CollectionExercise collex = new CollectionExercise();
    collex.setId(EXERCISE_ID);
    final Event event = new Event();
    event.setCollectionExercise(collex);
    event.setTag(tag.name());
    event.setTimestamp(eventTriggerDate);

    final SurveyDTO survey = new SurveyDTO();
    survey.setSurveyType(SurveyDTO.SurveyType.Business);
    when(surveyService.getSurveyForCollectionExercise(collex)).thenReturn(survey);

    final String actionRuleSuffix = nudgeSuffixGenerator.getNudgeEmailNumber(tag.name());
    final UUID actionRuleId1 = UUID.fromString("7186077b-809f-46c7-a0ba-43139d3efa23");
    final ActionRuleDTO actionRuleDTO1 =
        createActionRuleDTO(actionRuleId1, ActionType.BSNUE, actionRuleSuffix);

    final UUID actionRuleId2 = UUID.fromString("012cda1e-916a-4182-8d9a-cc66e32f8860");
    final ActionRuleDTO actionRuleDTO2 =
        createActionRuleDTO(actionRuleId2, ActionType.BSNUL, actionRuleSuffix);

    final List<ActionRuleDTO> actionRuleList = Arrays.asList(actionRuleDTO1, actionRuleDTO2);
    when(nudgeEmailActionTypeRuleGenerator.getActionRuleDTOS(event, collex))
        .thenReturn(actionRuleList);

    updater.execute(event);

    verify(actionSvcClient, atLeastOnce())
        .updateActionRule(
            actionRuleId1,
            actionRuleDTO1.getName(),
            actionRuleDTO1.getDescription(),
            OffsetDateTime.ofInstant(eventTriggerDate.toInstant(), ZoneId.systemDefault()),
            actionRuleDTO1.getPriority());
    verify(actionSvcClient, atLeastOnce())
        .updateActionRule(
            actionRuleId2,
            actionRuleDTO2.getName(),
            actionRuleDTO2.getDescription(),
            OffsetDateTime.ofInstant(eventTriggerDate.toInstant(), ZoneId.systemDefault()),
            actionRuleDTO2.getPriority());
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
