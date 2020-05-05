package uk.gov.ons.ctp.response.collection.exercise.lib.common.util;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Date;

/**
 * This class is designed to provide more flexible parsing of dates than the standard DateFormats.
 * It accepts an array of input formats that are attempted in order when parsing. The value returned
 * by the first input format that successfully parses the date will be returned. This class also
 * contains an output format that is used for formatting dates.
 */
public class AggregatedDateFormat extends DateFormat {

  private static final Logger log = LoggerFactory.getLogger(AggregatedDateFormat.class);

  private DateFormat[] inputFormats;
  private DateFormat outputFormat;

  public void init(final DateFormat outputFormat, final DateFormat[] inputFormats) {
    this.inputFormats = inputFormats;
    this.outputFormat = outputFormat;
  }

  @Override
  public StringBuffer format(
      final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition) {
    log.with("date", date).trace("Formatting date to string");
    return outputFormat.format(date, toAppendTo, fieldPosition);
  }

  @Override
  public Object clone() {
    AggregatedDateFormat result = new AggregatedDateFormat();

    // Make a DEEP copy of the array
    DateFormat[] inputFormatsClone = new DateFormat[inputFormats.length];
    for (int i = 0; i < inputFormats.length; i++) {
      inputFormatsClone[i] = (DateFormat) inputFormats[i].clone();
    }

    result.init((DateFormat) outputFormat.clone(), inputFormatsClone);
    return result;
  }

  @Override
  public Date parse(final String source, final ParsePosition pos) {
    log.with("string", source).trace("Parsing string to date");
    return Arrays.stream(inputFormats).map(d -> d.parse(source, pos)).findFirst().orElse(null);
  }
}
