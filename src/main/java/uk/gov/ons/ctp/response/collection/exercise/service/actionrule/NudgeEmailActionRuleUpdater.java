package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleUpdater;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@Component
public class NudgeEmailActionRuleUpdater implements ActionRuleUpdater {
  private final NudgeEmailActionTypeRuleGenerator nudgeEmailActionTypeRuleGenerator;
  private final SurveySvcClient surveySvcClient;
  private final ActionSvcClient actionSvcClient;

  public NudgeEmailActionRuleUpdater(
      NudgeEmailActionTypeRuleGenerator nudgeEmailActionTypeRuleGenerator,
      SurveySvcClient surveySvcClient,
      ActionSvcClient actionSvcClient) {
    this.nudgeEmailActionTypeRuleGenerator = nudgeEmailActionTypeRuleGenerator;
    this.surveySvcClient = surveySvcClient;
    this.actionSvcClient = actionSvcClient;
  }

  @Override
  public void execute(Event event) throws CTPException {
    if (!EventService.Tag.valueOf(event.getTag()).isNudgeEmail()) {
      return;
    }

    final CollectionExercise collectionExercise = event.getCollectionExercise();
    final SurveyDTO survey = surveySvcClient.getSurveyForCollectionExercise(collectionExercise);

    if (survey.getSurveyType() != SurveyDTO.SurveyType.Business) {
      return;
    }
    final List<ActionRuleDTO> actionRuleList =
        nudgeEmailActionTypeRuleGenerator.getActionRuleDTOS(event, collectionExercise);

    for (final ActionRuleDTO actionRule : actionRuleList) {
      actionSvcClient.updateActionRule(
          actionRule.getId(),
          actionRule.getName(),
          actionRule.getDescription(),
          OffsetDateTime.ofInstant(event.getTimestamp().toInstant(), ZoneId.systemDefault()),
          actionRule.getPriority());
    }
  }
}
