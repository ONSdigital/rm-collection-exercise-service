package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.ActionRuleCreator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.response.survey.representation.SurveyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@Component
public final class ReminderActionRuleCreator implements ActionRuleCreator {
  private final ActionSvcClient actionSvcClient;

  public ReminderActionRuleCreator(final ActionSvcClient actionSvcClient) {
    this.actionSvcClient = actionSvcClient;
  }

  @Override
  public void execute(
      final Event collectionExerciseEvent,
      final CaseTypeOverride businessCaseTypeOverride,
      final CaseTypeOverride businessIndividualCaseTypeOverride,
      final SurveyDTO survey) {
    if (survey.getSurveyType() != SurveyType.Business) {
      return;
    }

    if (!isReminder(collectionExerciseEvent)) {
      return;
    }

    final Instant instant = Instant.ofEpochMilli(collectionExerciseEvent.getTimestamp().getTime());
    final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    final CollectionExercise collectionExercise = collectionExerciseEvent.getCollectionExercise();

    actionSvcClient.createActionRule(
        survey.getShortName() + "REME",
        survey.getShortName() + " Reminder Email " + collectionExercise.getExerciseRef(),
        "BSRE",
        offsetDateTime,
        3,
        businessIndividualCaseTypeOverride.getActionPlanId());

    actionSvcClient.createActionRule(
        survey.getShortName() + "REMF",
        survey.getShortName() + " Reminder File " + collectionExercise.getExerciseRef(),
        "BSRL",
        offsetDateTime,
        3,
        businessCaseTypeOverride.getActionPlanId());
  }

  private boolean isReminder(final Event collectionExerciseEvent) {
    return Arrays.asList(Tag.reminder, Tag.reminder2, Tag.reminder3)
        .contains(Tag.valueOf(collectionExerciseEvent.getTag()));
  }
}
