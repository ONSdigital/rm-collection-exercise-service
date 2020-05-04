package uk.gov.ons.ctp.lib.common.matcher;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.util.MultiIsoDateFormat;

public class DateMatcher extends BaseMatcher<Date> {

  private static DateFormat DEFAULT_DATE_FORMAT = new MultiIsoDateFormat();
  private Date date;

  public DateMatcher(String date) throws ParseException {
    this(DEFAULT_DATE_FORMAT.parse(date));
  }

  public DateMatcher(Date date) {
    this.date = date;
  }

  @Override
  public boolean matches(Object o) {
    if (o instanceof String) {
      try {
        return this.date.equals(DEFAULT_DATE_FORMAT.parse((String) o));
      } catch (ParseException e) {
        return false;
      }
    } else if (o instanceof Date) {
      return this.date.equals(o);
    } else {
      return false;
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("The dates do not match");
  }
}
