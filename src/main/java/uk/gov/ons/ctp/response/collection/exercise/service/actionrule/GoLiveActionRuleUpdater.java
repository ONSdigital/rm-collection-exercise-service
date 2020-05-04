package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
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
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO.SurveyType;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

@Component
public final class GoLiveActionRuleUpdater implements ActionRuleUpdater {
  private final ActionRulesFilter actionRulesFilter;
  private final ActionSvcClient actionSvcClient;
  private final SurveySvcClient surveyService;

  private GoLiveActionRuleUpdater(
      ActionRulesFilter actionRulesFilter,
      ActionSvcClient actionSvcClient,
      SurveySvcClient surveyService) {
    this.actionRulesFilter = actionRulesFilter;
    this.actionSvcClient = actionSvcClient;
    this.surveyService = surveyService;
  }

  @Override
  public void execute(final Event event) throws CTPException {

    if (!Tag.go_live.hasName(event.getTag())) {
      return;
    }

    final CollectionExercise collectionExercise = event.getCollectionExercise();

    final SurveyDTO survey = surveyService.getSurveyForCollectionExercise(collectionExercise);

    if (survey.getSurveyType() != SurveyType.Business) {
      return;
    }

    final ActionPlanDTO actionPlan =
        actionSvcClient.getActionPlanBySelectorsBusiness(
            collectionExercise.getId().toString(), true);

    final List<ActionRuleDTO> actionRules =
        actionSvcClient.getActionRulesForActionPlan(actionPlan.getId());
    final ActionRuleDTO bsneRule =
        actionRulesFilter.getActionRuleByType(actionRules, ActionType.BSNE, actionPlan.getId());

    actionSvcClient.updateActionRule(
        bsneRule.getId(),
        bsneRule.getName(),
        bsneRule.getDescription(),
        OffsetDateTime.ofInstant(event.getTimestamp().toInstant(), ZoneId.systemDefault()),
        bsneRule.getPriority());
  }
}
