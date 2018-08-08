package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;

public abstract class AbstractActionRuleUpdater implements ActionRuleUpdater {
  private static final String MULTIPLE_ACTION_RULES_FOUND_EXCEPTION =
      "Multiple %s Action Rules Found for Action Plan %s, remove all but %d before continuing";
  private static final String NO_ACTION_RULES_FOUND_EXCEPTION =
      "No %s Action Rules Found for Action Plan %s, please create one instead";
  private static final String ACTION_PLAN_NOT_FOUND_EXCEPTION = "Action Plan %s not found";
  private static final int MAX_ACTION_RULES = 1;
  protected ActionSvcClient actionSvcClient;

  public AbstractActionRuleUpdater(final ActionSvcClient actionSvcClient) {
    this.actionSvcClient = actionSvcClient;
  }

  protected List<ActionRuleDTO> getActionRulesForActionPlan(final UUID actionPlanId)
      throws CTPException {
    final List<ActionRuleDTO> actionRules =
        actionSvcClient.getActionRulesForActionPlan(actionPlanId);

    if (actionRules == null) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR,
          String.format(ACTION_PLAN_NOT_FOUND_EXCEPTION, actionPlanId));
    }
    return actionRules;
  }

  protected List<ActionRuleDTO> filterActionRulesByType(
      final List<ActionRuleDTO> actionRules, final ActionType actionType, final UUID actionPlanId)
      throws CTPException {
    final List<ActionRuleDTO> filteredRules =
        actionRules
            .stream()
            .filter(actionRuleDTO -> actionRuleDTO.getActionTypeName().equals(actionType))
            .collect(Collectors.toList());

    if (filteredRules.isEmpty()) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format(NO_ACTION_RULES_FOUND_EXCEPTION, actionType, actionPlanId));
    }

    if (filteredRules.size() > MAX_ACTION_RULES) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR,
          String.format(
              MULTIPLE_ACTION_RULES_FOUND_EXCEPTION, actionType, actionPlanId, MAX_ACTION_RULES));
    }

    return filteredRules;
  }
}
