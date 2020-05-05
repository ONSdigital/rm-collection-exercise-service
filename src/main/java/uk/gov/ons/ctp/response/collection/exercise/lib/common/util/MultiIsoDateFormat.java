package uk.gov.ons.ctp.response.collection.exercise.lib.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class is intended to be used as the default means for parsing and formatting dates in RM
 * service requests/responses. It allows a number of different ISO8601 formats and aims to be
 * compatible with the Python ISO8601 format
 */
public class MultiIsoDateFormat extends AggregatedDateFormat {

  private static final String ISO_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
  private static final String ISO_FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
  private static final String ISO_FORMAT_3 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  /** Default constructor */
  public MultiIsoDateFormat() {
    DateFormat outputFormat = new SimpleDateFormat(ISO_FORMAT_3);

    DateFormat inputFormat1 = new SimpleDateFormat(ISO_FORMAT_1);
    DateFormat inputFormat2 = new SimpleDateFormat(ISO_FORMAT_2);
    DateFormat inputFormat3 = new SimpleDateFormat(ISO_FORMAT_3);

    DateFormat[] inputFormats = {inputFormat1, inputFormat2, inputFormat3};

    init(outputFormat, inputFormats);
  }
}
