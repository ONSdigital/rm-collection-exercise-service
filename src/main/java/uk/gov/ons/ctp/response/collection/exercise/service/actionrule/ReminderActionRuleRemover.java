package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleRemover;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@Component
public class ReminderActionRuleRemover implements ActionRuleRemover {
  private final ReminderSuffixGenerator reminderSuffixGenerator;
  private final SurveySvcClient surveyService;
  private final ActionSvcClient actionSvcClient;
  private final ActionRulesFilter actionRulesFilter;

  public ReminderActionRuleRemover(
      ReminderSuffixGenerator reminderSuffixGenerator,
      SurveySvcClient surveySvcClient,
      ActionSvcClient actionSvcClient,
      ActionRulesFilter actionRulesFilter) {
    this.reminderSuffixGenerator = reminderSuffixGenerator;
    this.surveyService = surveySvcClient;
    this.actionSvcClient = actionSvcClient;
    this.actionRulesFilter = actionRulesFilter;
  }

  @Override
  public void execute(Event event) throws CTPException {
    if (!EventService.Tag.valueOf(event.getTag()).isReminder()) {
      return;
    }

    final CollectionExercise collectionExercise = event.getCollectionExercise();

    final SurveyDTO survey = surveyService.getSurveyForCollectionExercise(collectionExercise);

    if (survey.getSurveyType() != SurveyDTO.SurveyType.Business) {
      return;
    }

    final String CollectionExerciseId = collectionExercise.getId().toString();
    final ActionPlanDTO activeActionPlan =
        actionSvcClient.getActionPlanBySelectorsBusiness(CollectionExerciseId, true);
    final ActionPlanDTO inactiveActionPlan =
        actionSvcClient.getActionPlanBySelectorsBusiness(CollectionExerciseId, false);

    final String reminderSuffix = reminderSuffixGenerator.getReminderSuffix(event.getTag());

    final UUID biActionPlanId = activeActionPlan.getId();
    final List<ActionRuleDTO> biActionRules =
        actionSvcClient.getActionRulesForActionPlan(biActionPlanId);
    final List<ActionRuleDTO> biActionRulesMatchingSuffix =
        filterRulesMatchingSuffix(reminderSuffix, biActionRules);
    final ActionRuleDTO bsreRule =
        actionRulesFilter.getActionRuleByType(
            biActionRulesMatchingSuffix, ActionType.BSRE, biActionPlanId);

    final UUID bActionPlanId = inactiveActionPlan.getId();
    final List<ActionRuleDTO> bActionRules =
        actionSvcClient.getActionRulesForActionPlan(bActionPlanId);
    final List<ActionRuleDTO> bActionRulesMatchingSuffix =
        filterRulesMatchingSuffix(reminderSuffix, bActionRules);
    final ActionRuleDTO bsrlRule =
        actionRulesFilter.getActionRuleByType(
            bActionRulesMatchingSuffix, ActionType.BSRL, bActionPlanId);

    final List<ActionRuleDTO> bsrlAndBsre = Arrays.asList(bsreRule, bsrlRule);

    for (final ActionRuleDTO actionRule : bsrlAndBsre) {
      actionSvcClient.deleteActionRule(
          actionRule.getId(),
          actionRule.getName(),
          actionRule.getDescription(),
          OffsetDateTime.ofInstant(event.getTimestamp().toInstant(), ZoneId.systemDefault()),
          actionRule.getPriority());
    }
  }

  private List<ActionRuleDTO> filterRulesMatchingSuffix(
      final String reminderSuffix, final List<ActionRuleDTO> actionRules) {
    return actionRules
        .stream()
        .filter(actionRuleDTO -> actionRuleDTO.getName().endsWith(reminderSuffix))
        .collect(Collectors.toList());
  }
}
