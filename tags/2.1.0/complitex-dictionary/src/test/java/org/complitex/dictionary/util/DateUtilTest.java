/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.testng.annotations.Test;
import static org.complitex.dictionary.util.DateUtil.*;
import static org.testng.Assert.*;

/**
 *
 * @author Artem
 */
public class DateUtilTest {

    @Test
    public void testDateInterval() throws ParseException {
        final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        format.setLenient(false);

        assertTrue(isValidDateInterval(format.parse("02.01.2016"), format.parse("01.01.2000"), 16));
        assertTrue(isValidDateInterval(format.parse("29.02.2016"), format.parse("28.02.2000"), 16));
        assertFalse(isValidDateInterval(format.parse("01.01.2016"), format.parse("01.01.2000"), 16));
        assertFalse(isValidDateInterval(format.parse("01.01.2000"), format.parse("01.01.2000"), 16));

        {
            Date p = previousDay(format.parse("02.05.1999"));
            assertEquals(getYear(p), 1999);
            assertEquals(getMonth(p), 4);
            assertEquals(getDay(p), 1);
        }

        {
            Date p = previousDay(format.parse("01.01.2000"));
            assertEquals(getYear(p), 1999);
            assertEquals(getMonth(p), 11);
            assertEquals(getDay(p), 31);
        }

        {
            Date p = previousDay(format.parse("02.01.2000"));
            assertEquals(getYear(p), 2000);
            assertEquals(getMonth(p), 0);
            assertNotEquals(getDay(p), 2);
        }
    }
}
