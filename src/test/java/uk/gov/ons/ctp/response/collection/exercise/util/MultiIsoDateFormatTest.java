package uk.gov.ons.ctp.response.collection.exercise.util;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MultiIsoDateFormatTest {

   private static final Date TEST_DATE = new Date(1516120265100L);

   private MultiIsoDateFormat dateFormat;

   @Before
   public void setUp(){
      this.dateFormat = new MultiIsoDateFormat();
   }

   @Test
   public void testFormat(){
       String formatted = this.dateFormat.format(TEST_DATE);

       assertEquals("2018-01-16T16:31:05.100Z", formatted);
   }

    @Test
    public void testParse1() throws ParseException {
        Date date = this.dateFormat.parse("2018-01-16T16:31:05.100+0000");

        assertEquals(TEST_DATE, date);
    }

    @Test
    public void testParse2() throws ParseException {
        Date date = this.dateFormat.parse("2018-01-16T17:31:05.100+01:00");

        assertEquals(TEST_DATE, date);
    }

   @Test
   public void testParse3() throws ParseException {
       Date date = this.dateFormat.parse("2018-01-16T16:31:05.100Z");

       assertEquals(TEST_DATE, date);
   }
}
