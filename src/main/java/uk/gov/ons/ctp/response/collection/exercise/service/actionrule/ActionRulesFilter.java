package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;

@Component
public class ActionRulesFilter {
  private static final String MULTIPLE_ACTION_RULES_FOUND_EXCEPTION =
      "Multiple %s Action Rules Found for Action Plan %s, remove all but %d before continuing";
  private static final String NO_ACTION_RULES_FOUND_EXCEPTION =
      "No %s Action Rules Found for Action Plan %s, please create one instead";
  private static final int MAX_ACTION_RULES = 1;

  public ActionRuleDTO getActionRuleByType(
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

    return filteredRules.get(0);
  }
}
