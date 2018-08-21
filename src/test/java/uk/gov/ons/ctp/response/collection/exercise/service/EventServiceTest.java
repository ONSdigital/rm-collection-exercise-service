package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;

public class EventServiceTest {
  @Test
  public void testTagShouldHaveMpsAsIsActionable() {
    assertThat(Tag.mps.isActionable(), is(true));
  }

  @Test
  public void testTagShouldHaveGoLiveAsIsAnActionableTag() {
    assertThat(Tag.go_live.isActionable(), is(true));
  }

  @Test
  public void testTagShouldHaveExerciseEndAsNotIsAnActionableTag() {
    assertThat(Tag.exercise_end.isActionable(), is(false));
  }

  @Test
  public void testTagShouldHaveReminderAsAnActionableTag() {
    assertThat(Tag.reminder.isActionable(), is(true));
  }

  @Test
  public void testTagShouldHaveReminder2AsAnActionableTag() {
    assertThat(Tag.reminder2.isActionable(), is(true));
  }

  @Test
  public void testTagShouldHaveReminder3AsAnActionableTag() {
    assertThat(Tag.reminder3.isActionable(), is(true));
  }

  @Test
  public void testCompareTagToItsName() {
    assertThat(Tag.mps.hasName("mps"), is(true));
  }

  @Test
  public void testCompareTagToRandomString() {
    assertThat(Tag.mps.hasName("asdadfgsfgth"), is(false));
  }

  @Test
  public void testTagShouldHaveReminderAsIsReminder() {
    assertThat(Tag.reminder.isReminder(), is(true));
  }

  @Test
  public void testTagShouldHaveReminder2AsIsReminder() {
    assertThat(Tag.reminder2.isReminder(), is(true));
  }

  @Test
  public void testTagShouldHaveReminder3AsIsReminder() {
    assertThat(Tag.reminder3.isReminder(), is(true));
  }

  @Test
  public void testTagShouldHaveMPSAsIsNotReminder() {
    assertThat(Tag.mps.isReminder(), is(false));
  }
}
