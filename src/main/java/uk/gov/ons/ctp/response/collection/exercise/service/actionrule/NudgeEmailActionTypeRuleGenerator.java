package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;

@Component
public class NudgeEmailActionTypeRuleGenerator {
  private final NudgeEmailSuffixGenerator nudgeEmailSuffixGenerator;
  private final ActionRulesFilter actionRulesFilter;
  private final ActionSvcClient actionSvcClient;

  public NudgeEmailActionTypeRuleGenerator(
      NudgeEmailSuffixGenerator nudgeEmailSuffixGenerator,
      ActionRulesFilter actionRulesFilter,
      ActionSvcClient actionSvcClient) {
    this.nudgeEmailSuffixGenerator = nudgeEmailSuffixGenerator;
    this.actionRulesFilter = actionRulesFilter;
    this.actionSvcClient = actionSvcClient;
  }

  public List<ActionRuleDTO> getActionRuleDTOS(Event event, CollectionExercise collectionExercise)
      throws CTPException, IllegalArgumentException {
    final String CollectionExerciseId = collectionExercise.getId().toString();
    final ActionPlanDTO activeActionPlan =
        actionSvcClient.getActionPlanBySelectorsBusiness(CollectionExerciseId, true);
    final ActionPlanDTO inactiveActionPlan =
        actionSvcClient.getActionPlanBySelectorsBusiness(CollectionExerciseId, false);
    final String nudgeEmailIndex = nudgeEmailSuffixGenerator.getNudgeEmailNumber(event.getTag());

    final ActionRuleDTO actionTypeBSNUERule =
        getActionRuleDTO(activeActionPlan, nudgeEmailIndex, ActionType.BSNUE);

    final ActionRuleDTO actionTypeBSNULRule =
        getActionRuleDTO(inactiveActionPlan, nudgeEmailIndex, ActionType.BSNUL);

    return Arrays.asList(actionTypeBSNUERule, actionTypeBSNULRule);
  }

  private List<ActionRuleDTO> filterRulesMatchingSuffix(
      final String actionRuleName, final List<ActionRuleDTO> actionRules) {
    return actionRules
        .stream()
        .filter(actionRuleDTO -> actionRuleDTO.getName().endsWith(actionRuleName))
        .collect(Collectors.toList());
  }

  private ActionRuleDTO getActionRuleDTO(
      ActionPlanDTO activeActionPlan, String nudgeEmailIndex, ActionType actionType)
      throws CTPException {
    final UUID biActionPlanId = activeActionPlan.getId();
    final List<ActionRuleDTO> biActionRules =
        actionSvcClient.getActionRulesForActionPlan(biActionPlanId);
    final List<ActionRuleDTO> biActionRulesMatchingSuffix =
        filterRulesMatchingSuffix(nudgeEmailIndex, biActionRules);
    return actionRulesFilter.getActionRuleByType(
        biActionRulesMatchingSuffix, actionType, biActionPlanId);
  }
}
