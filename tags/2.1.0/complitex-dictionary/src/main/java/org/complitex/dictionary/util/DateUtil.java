package org.complitex.dictionary.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.util.Calendar.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.01.2010 0:30:49
 */
public class DateUtil {
    private static ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<>();

    public static final Date MIN_BEGIN_DATE =  newDate(1, 1, 1970);
    public static final Date MAX_END_DATE =  newDate(31, 1, 2054);

    private DateUtil() {
    }

    public static SimpleDateFormat getDateFormat() {
        if (DATE_FORMAT.get() == null) {
            DATE_FORMAT.set(new SimpleDateFormat("dd.MM.yyyy"));
        }

        return DATE_FORMAT.get();
    }

    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    public static Date getEndOfDay(Date date) {
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(SECOND, 59);
        c.set(MILLISECOND, 999);

        return c.getTime();
    }

    public static Date getBeginOfDay(Date date) {
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(SECOND, 0);
        c.set(MILLISECOND, 0);

        return c.getTime();
    }

    public static String getTimeDiff(long start, long end) {
        long time = end - start;

        long msec = time % 1000;
        time = time / 1000;
        long sec = time % 60;
        time = time / 60;
        long min = time % 60;
        time = time / 60;
        long hour = time;

        return String.format("%d:%02d:%02d", hour, min, sec);
    }

    /**
     * Отображает локализованный месяц
     * @param month месяц, формат 1-январь, 2-февраль
     * @param locale локализация
     * @return месяц
     */
    public static String displayMonth(int month, Locale locale) {
        Calendar c = Calendar.getInstance(locale);
        c.set(Calendar.MONTH, month - 1);
        c.set(DAY_OF_MONTH, 1);
        return c.getDisplayName(MONTH, LONG, locale);
    }

    public static String displayMonth(Date date, Locale locale) {
        Calendar c = Calendar.getInstance(locale);
        c.setTime(date);

        return c.getDisplayName(MONTH, LONG, locale);
    }

    public static boolean isTheSameDay(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        } else if (date1 == null || date2 == null) {
            return false;
        }

        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);

        return c1.get(YEAR) == c2.get(YEAR)
                && c1.get(DAY_OF_YEAR) == c2.get(DAY_OF_YEAR);
    }

    public static Date getMax(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return date1 == null ? date2 : date1;
        }

        return date1.compareTo(date2) > 0 ? date1 : date2;
    }

    public static Date getMin(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return date1 == null ? date2 : date1;
        }

        return date1.compareTo(date2) > 0 ? date2 : date1;
    }

    public static boolean isCurrentDay(Date date) {
        return isTheSameDay(date, getCurrentDate());
    }

    public static Date newDate(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        calendar.set(year, month - 1, 1);

        return calendar.getTime();
    }

    public static Date getLastDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        calendar.add(DAY_OF_YEAR, -1);
        return calendar.getTime();
    }

    public static Date getFirstDayOfMonth(Date date) {
        Calendar calendar = newCalendar(date);
        calendar.set(DAY_OF_MONTH, 1);

        return calendar.getTime();
    }

    public static Date getFirstDayOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(DAY_OF_MONTH, 1);
        calendar.set(HOUR_OF_DAY, 0);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);

        return calendar.getTime();
    }

    public static boolean isSameMonth(Date d1, Date d2) {
        Calendar c1 = newCalendar(d1);
        Calendar c2 = newCalendar(d2);

        return c1.get(YEAR) == c2.get(YEAR) && c1.get(MONTH) == c2.get(MONTH);
    }

    public static Date justBefore(Date current) {
        Calendar c = Calendar.getInstance();
        c.setTime(current);
        c.add(SECOND, -1);
        return c.getTime();
    }

    public static Date justAfter(Date current) {
        Calendar c = Calendar.getInstance();
        c.setTime(current);
        c.add(SECOND, 1);
        return c.getTime();
    }

    public static int getDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(DAY_OF_MONTH);
    }

    public static int getMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(MONTH);
    }

    public static int getYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return c.get(YEAR);
    }

    public static Date previousDay(Date date) {
        Calendar c = newCalendar(date);
        c.add(Calendar.DAY_OF_YEAR, -1);
        return c.getTime();
    }

    public static Date nextDay(Date date) {
        Calendar c = newCalendar(date);
        c.add(Calendar.DAY_OF_YEAR, 1);
        return c.getTime();
    }

    /**
     * Creates Date object based on day, month and year.
     * Lenient mode turned off so that exception may be thrown in case of incorrect date information.
     *
     * @param day
     * @param month
     * @param year
     * @return date
     */
    public static Date newDate(int day, int month, int year) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setLenient(false);
        c.set(year, month - 1, day);
        return c.getTime();
    }

    public static boolean isValidDateInterval(Date d1, Date d2, int years) {
        Calendar c = Calendar.getInstance();
        c.setTime(d2);
        c.add(YEAR, years);
        return d1.after(c.getTime());
    }

    public static Date add(Date date, int years) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(YEAR, years);
        return c.getTime();
    }

    public static Date asDate(String value, String pattern) {
        if (value == null) {
            return null;
        }
        DateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
        }
        return null;
    }

    public static Calendar newCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    public static Date addMonth(Date date, int amount) {
        Calendar calendar = newCalendar(date);
        calendar.add(MONTH, amount);

        return calendar.getTime();
    }

    public static Date previousMonth(Date date) {
        return addMonth(date, -1);
    }

    public static String format(Date date) {
        return date != null ? getDateFormat().format(date) : "";
    }

    public static String format(Date date1, Date date2) {
        return (date1 != null ? getDateFormat().format(date1) : "..")
                + " - " + (date2 != null ? getDateFormat().format(date2) : "..");
    }

    public static int getDaysDiff(Date date1, Date date2) {


        return (int) Math.abs((date1.getTime() - date2.getTime()) / 86400000);
    }
}
