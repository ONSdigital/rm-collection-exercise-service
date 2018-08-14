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
import uk.gov.ons.response.survey.representation.SurveyDTO.SurveyType;

@Component
public final class ReminderActionRuleCreator implements ActionRuleCreator {
  private final ActionSvcClient actionSvcClient;
  private final ReminderSuffixGenerator reminderSuffixGenerator;

  public ReminderActionRuleCreator(
      final ActionSvcClient actionSvcClient,
      final ReminderSuffixGenerator reminderSuffixGenerator) {
    this.actionSvcClient = actionSvcClient;
    this.reminderSuffixGenerator = reminderSuffixGenerator;
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

    if (!Tag.valueOf(collectionExerciseEvent.getTag()).isReminder()) {
      return;
    }

    final Instant instant = Instant.ofEpochMilli(collectionExerciseEvent.getTimestamp().getTime());
    final OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    final CollectionExercise collectionExercise = collectionExerciseEvent.getCollectionExercise();

    final String reminderSuffix =
        reminderSuffixGenerator.getReminderSuffix(collectionExerciseEvent.getTag());

    actionSvcClient.createActionRule(
        survey.getShortName() + "REME" + reminderSuffix,
        survey.getShortName() + " Reminder Email " + collectionExercise.getExerciseRef(),
        ActionType.BSRE,
        offsetDateTime,
        3,
        businessIndividualCaseTypeOverride.getActionPlanId());

    actionSvcClient.createActionRule(
        survey.getShortName() + "REMF" + reminderSuffix,
        survey.getShortName() + " Reminder File " + collectionExercise.getExerciseRef(),
        ActionType.BSRL,
        offsetDateTime,
        3,
        businessCaseTypeOverride.getActionPlanId());
  }
}
