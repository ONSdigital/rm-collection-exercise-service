package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

@Component
public class ReminderSuffixGenerator {

  public String getReminderSuffix(final String tagName) {
    Tag tag = Tag.valueOf(tagName);

    if (!tag.isReminder()) {
      throw new IllegalArgumentException(String.format("Tag %s is not a reminder", tagName));
    }

    final int reminderIndex = Tag.ORDERED_REMINDERS.indexOf(tag);
    return String.format("+%d", reminderIndex + 1);
  }
}
