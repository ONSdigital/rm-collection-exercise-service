package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@Component
public class ReminderActionRuleUpdater extends AbstractActionRuleUpdater {

  private final ReminderSuffixGenerator reminderSuffixGenerator;

  public ReminderActionRuleUpdater(
      final ActionSvcClient actionSvcClient,
      final ReminderSuffixGenerator reminderSuffixGenerator) {
    super(actionSvcClient);
    this.reminderSuffixGenerator = reminderSuffixGenerator;
  }

  @Override
  public void execute(
      final Event event,
      final CaseTypeOverride businessCaseTypeOverride,
      final CaseTypeOverride businessIndividualCaseTypeOverride,
      final SurveyDTO survey)
      throws CTPException {
    if (survey.getSurveyType() != SurveyType.Business) {
      return;
    }

    if (!Tag.valueOf(event.getTag()).isReminder()) {
      return;
    }
    final String reminderSuffix = reminderSuffixGenerator.getReminderSuffix(event.getTag());

    final UUID biActionPlanId = businessIndividualCaseTypeOverride.getActionPlanId();
    final List<ActionRuleDTO> biActionRules = getActionRulesForActionPlan(biActionPlanId);
    final List<ActionRuleDTO> biActionRulesMatchingSuffix =
        filterRulesMatchingSuffix(reminderSuffix, biActionRules);
    final List<ActionRuleDTO> bsreRules =
        filterActionRulesByType(biActionRulesMatchingSuffix, ActionType.BSRE, biActionPlanId);

    final UUID bActionPlanId = businessCaseTypeOverride.getActionPlanId();
    final List<ActionRuleDTO> bActionRules = getActionRulesForActionPlan(bActionPlanId);
    final List<ActionRuleDTO> bActionRulesMatchingSuffix =
        filterRulesMatchingSuffix(reminderSuffix, bActionRules);
    final List<ActionRuleDTO> bsrlRules =
        filterActionRulesByType(bActionRulesMatchingSuffix, ActionType.BSRL, bActionPlanId);

    final ArrayList<ActionRuleDTO> bsrlAndBsre = new ArrayList<>();
    bsrlAndBsre.addAll(bsreRules);
    bsrlAndBsre.addAll(bsrlRules);

    for (final ActionRuleDTO actionRule : bsrlAndBsre) {
      actionSvcClient.updateActionRule(
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
