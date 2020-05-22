package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

@Component
public class NudgeEmailActionRuleCreator implements ActionRuleCreator {
  private final ActionSvcClient actionSvcClient;
  private final NudgeEmailSuffixGenerator nudgeEmailSuffixGenerator;
  private final SurveySvcClient surveySvcClient;

  public NudgeEmailActionRuleCreator(
      final ActionSvcClient actionSvcClient,
      final NudgeEmailSuffixGenerator nudgeEmailSuffixGenerator,
      final SurveySvcClient surveySvcClient) {
    this.actionSvcClient = actionSvcClient;
    this.nudgeEmailSuffixGenerator = nudgeEmailSuffixGenerator;
    this.surveySvcClient = surveySvcClient;
  }

  @Override
  public void execute(Event collectionExerciseEvent) throws CTPException {
    if (!Tag.valueOf(collectionExerciseEvent.getTag()).isNudgeEmail()) {
      return;
    }
    final CollectionExercise collectionExercise = collectionExerciseEvent.getCollectionExercise();
    final SurveyDTO survey = surveySvcClient.getSurveyForCollectionExercise(collectionExercise);
    if (survey.getSurveyType() != SurveyDTO.SurveyType.Business) {
      return;
    }
    final Instant instant = Instant.ofEpochMilli(collectionExerciseEvent.getTimestamp().getTime());
    final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    final String CollectionExerciseId = collectionExercise.getId().toString();
    final ActionPlanDTO activeActionPlan =
        actionSvcClient.getActionPlanBySelectorsBusiness(CollectionExerciseId, true);
    final ActionPlanDTO inactiveActionPlan =
        actionSvcClient.getActionPlanBySelectorsBusiness(CollectionExerciseId, false);
    final String nudgeEmailIndex =
        nudgeEmailSuffixGenerator.getNudgeEmailNumber(collectionExerciseEvent.getTag());

    actionSvcClient.createActionRule(
        survey.getShortName() + "NUDGE" + nudgeEmailIndex,
        survey.getShortName() + " Nudge Email " + collectionExercise.getExerciseRef(),
        ActionType.BSNUE,
        offsetDateTime,
        3,
        activeActionPlan.getId());

    actionSvcClient.createActionRule(
        survey.getShortName() + "NUDGE" + nudgeEmailIndex,
        survey.getShortName() + " Nudge File " + collectionExercise.getExerciseRef(),
        ActionType.BSNUL,
        offsetDateTime,
        3,
        inactiveActionPlan.getId());
  }
}
