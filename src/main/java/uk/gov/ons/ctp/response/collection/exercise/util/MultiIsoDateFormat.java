package uk.gov.ons.ctp.response.collection.exercise.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MultiIsoDateFormat extends AggregatedDateFormat {

    private static final DateFormat ISO_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final DateFormat ISO_FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
    private static final DateFormat ISO_FORMAT_3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static final DateFormat[] ALL_FORMATS = { ISO_FORMAT_1, ISO_FORMAT_2, ISO_FORMAT_3 };

    public MultiIsoDateFormat(){
        super(ISO_FORMAT_3, ALL_FORMATS);
    }
}
