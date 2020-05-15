package uk.gov.ons.ctp.response.collection.exercise.service.actionrule;

import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

@Component
public class NudgeEmailSuffixGenerator {

  public String getNudgeEmailNumber(final String tagName) {
    Tag tag = Tag.valueOf(tagName);

    if (!tag.isNudgeEmail()) {
      throw new IllegalArgumentException(String.format("Tag %s is not a nudge email", tagName));
    }
    final int nudgeEmailIndex = Tag.ORDERED_NUDGE_EMAIL.indexOf(tag);
    return String.format("+%d", nudgeEmailIndex + 1);
  }
}
