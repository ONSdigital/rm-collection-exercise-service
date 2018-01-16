package uk.gov.ons.ctp.response.collection.exercise.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class AggregatedDateFormat extends DateFormat {

    public AggregatedDateFormat(DateFormat outputFormat, DateFormat[] inputFormats){
        this.inputFormats = inputFormats;
        this.outputFormat = outputFormat;
    }
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        log.debug("Formatting: {}", date);
        return this.outputFormat.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Object clone() {
        return new AggregatedDateFormat(this.outputFormat, this.inputFormats);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        log.debug("Parsing: {}", source);
        return Arrays.stream(this.inputFormats).map(d -> d.parse(source, pos)).findFirst().orElse(null);
    }

    private DateFormat[] inputFormats;
    private DateFormat outputFormat;
}
