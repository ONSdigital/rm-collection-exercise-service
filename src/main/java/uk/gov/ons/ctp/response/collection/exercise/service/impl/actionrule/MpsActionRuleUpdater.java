package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
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
public class MpsActionRuleUpdater extends AbstractActionRuleUpdater {

  public MpsActionRuleUpdater(final ActionSvcClient actionSvcClient) {
    super(actionSvcClient);
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

    if (!Tag.mps.hasName(event.getTag())) {
      return;
    }

    final UUID actionPlanId = businessCaseTypeOverride.getActionPlanId();
    final List<ActionRuleDTO> actionRules = getActionRulesForActionPlan(actionPlanId);
    final List<ActionRuleDTO> bsnlRules =
        filterActionRulesByType(actionRules, ActionType.BSNL, actionPlanId);
    final ActionRuleDTO bsnlRule = bsnlRules.get(0);

    actionSvcClient.updateActionRule(
        bsnlRule.getId(),
        bsnlRule.getName(),
        bsnlRule.getDescription(),
        OffsetDateTime.ofInstant(event.getTimestamp().toInstant(), ZoneId.systemDefault()),
        bsnlRule.getPriority());
  }
}
