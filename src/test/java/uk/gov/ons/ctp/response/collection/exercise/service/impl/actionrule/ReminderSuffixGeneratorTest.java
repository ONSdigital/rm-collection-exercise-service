package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

@RunWith(MockitoJUnitRunner.class)
public class ReminderSuffixGeneratorTest {

  @InjectMocks private ReminderSuffixGenerator reminderSuffix;

  @Test
  public void testReminders1ReturnsPlusOne() {
    assertThat(reminderSuffix.getReminderSuffix(Tag.reminder.name()), is("+1"));
  }

  @Test
  public void testReminders2ReturnsPlusTwo() {
    assertThat(reminderSuffix.getReminderSuffix(Tag.reminder2.name()), is("+2"));
  }

  @Test
  public void testReminders3ReturnsPlusThree() {
    assertThat(reminderSuffix.getReminderSuffix(Tag.reminder3.name()), is("+3"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void onlyRemindersAreSupported() {
    reminderSuffix.getReminderSuffix(Tag.go_live.name());
  }
}
