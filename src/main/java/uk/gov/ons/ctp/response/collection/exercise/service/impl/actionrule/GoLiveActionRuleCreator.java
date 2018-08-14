package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.action.representation.ActionType;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.response.survey.representation.SurveyDTO;

@Component
public final class GoLiveActionRuleCreator implements ActionRuleCreator {
  private final ActionSvcClient actionSvcClient;

  public GoLiveActionRuleCreator(final ActionSvcClient actionSvcClient) {
    this.actionSvcClient = actionSvcClient;
  }

  @Override
  public void execute(
      final Event collectionExerciseEvent,
      final CaseTypeOverride businessCaseTypeOverride,
      final CaseTypeOverride businessIndividualCaseTypeOverride,
      final SurveyDTO survey) {
    if (survey.getSurveyType() != SurveyDTO.SurveyType.Business) {
      return;
    }

    if (!isGoLive(collectionExerciseEvent)) {
      return;
    }

    final Instant instant = Instant.ofEpochMilli(collectionExerciseEvent.getTimestamp().getTime());
    final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    final CollectionExercise collectionExercise = collectionExerciseEvent.getCollectionExercise();

    actionSvcClient.createActionRule(
        survey.getShortName() + "NOTE",
        survey.getShortName() + " Notification Email " + collectionExercise.getExerciseRef(),
        ActionType.BSNE,
        offsetDateTime,
        3,
        businessIndividualCaseTypeOverride.getActionPlanId());
  }

  private boolean isGoLive(final Event collectionExerciseEvent) {
    return Tag.go_live.hasName(collectionExerciseEvent.getTag());
  }
}
