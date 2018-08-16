package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.response.survey.representation.SurveyDTO;

@Component
public final class MpsActionRuleCreator implements ActionRuleCreator {

  private final ActionSvcClient actionSvcClient;
  private final SurveyService surveyService;

  public MpsActionRuleCreator(
      final ActionSvcClient actionSvcClient, final SurveyService surveyService) {
    this.actionSvcClient = actionSvcClient;
    this.surveyService = surveyService;
  }

  @Override
  public void execute(final Event collectionExerciseEvent) throws CTPException {

    final SurveyDTO survey =
        surveyService.getSurveyForCollectionExercise(
            collectionExerciseEvent.getCollectionExercise());

    if (survey.getSurveyType() != SurveyDTO.SurveyType.Business) {
      return;
    }

    if (!isMps(collectionExerciseEvent)) {
      return;
    }

    final Instant instant = Instant.ofEpochMilli(collectionExerciseEvent.getTimestamp().getTime());
    final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    final CollectionExercise collectionExercise = collectionExerciseEvent.getCollectionExercise();

    final ActionPlanDTO actionPlan =
        actionSvcClient.getActionPlanBySelectors(collectionExercise.getId().toString(), false);

    actionSvcClient.createActionRule(
        survey.getShortName() + "NOTF",
        survey.getShortName() + " Notification File " + collectionExercise.getExerciseRef(),
        ActionType.BSNL,
        offsetDateTime,
        3,
        actionPlan.getId());
  }

  private boolean isMps(final Event collectionExerciseEvent) {
    return Tag.mps.hasName(collectionExerciseEvent.getTag());
  }
}
