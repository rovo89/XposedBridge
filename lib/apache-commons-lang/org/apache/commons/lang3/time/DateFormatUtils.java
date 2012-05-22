/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.time;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>Date and time formatting utilities and constants.</p>
 *
 * <p>Formatting is performed using the thread-safe
 * {@link org.apache.commons.lang3.time.FastDateFormat} class.</p>
 *
 * @since 2.0
 * @version $Id: DateFormatUtils.java 1088899 2011-04-05 05:31:27Z bayard $
 */
public class DateFormatUtils {

    /**
     * The UTC time zone (often referred to as GMT).
     * This is private as it is mutable.
     */
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("GMT");
    /**
     * ISO8601 formatter for date-time without time zone.
     * The format used is <tt>yyyy-MM-dd'T'HH:mm:ss</tt>.
     */
    public static final FastDateFormat ISO_DATETIME_FORMAT
            = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * ISO8601 formatter for date-time with time zone.
     * The format used is <tt>yyyy-MM-dd'T'HH:mm:ssZZ</tt>.
     */
    public static final FastDateFormat ISO_DATETIME_TIME_ZONE_FORMAT
            = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZZ");

    /**
     * ISO8601 formatter for date without time zone.
     * The format used is <tt>yyyy-MM-dd</tt>.
     */
    public static final FastDateFormat ISO_DATE_FORMAT
            = FastDateFormat.getInstance("yyyy-MM-dd");

    /**
     * ISO8601-like formatter for date with time zone.
     * The format used is <tt>yyyy-MM-ddZZ</tt>.
     * This pattern does not comply with the formal ISO8601 specification
     * as the standard does not allow a time zone  without a time.
     */
    public static final FastDateFormat ISO_DATE_TIME_ZONE_FORMAT
            = FastDateFormat.getInstance("yyyy-MM-ddZZ");

    /**
     * ISO8601 formatter for time without time zone.
     * The format used is <tt>'T'HH:mm:ss</tt>.
     */
    public static final FastDateFormat ISO_TIME_FORMAT
            = FastDateFormat.getInstance("'T'HH:mm:ss");

    /**
     * ISO8601 formatter for time with time zone.
     * The format used is <tt>'T'HH:mm:ssZZ</tt>.
     */
    public static final FastDateFormat ISO_TIME_TIME_ZONE_FORMAT
            = FastDateFormat.getInstance("'T'HH:mm:ssZZ");

    /**
     * ISO8601-like formatter for time without time zone.
     * The format used is <tt>HH:mm:ss</tt>.
     * This pattern does not comply with the formal ISO8601 specification
     * as the standard requires the 'T' prefix for times.
     */
    public static final FastDateFormat ISO_TIME_NO_T_FORMAT
            = FastDateFormat.getInstance("HH:mm:ss");

    /**
     * ISO8601-like formatter for time with time zone.
     * The format used is <tt>HH:mm:ssZZ</tt>.
     * This pattern does not comply with the formal ISO8601 specification
     * as the standard requires the 'T' prefix for times.
     */
    public static final FastDateFormat ISO_TIME_NO_T_TIME_ZONE_FORMAT
            = FastDateFormat.getInstance("HH:mm:ssZZ");

    /**
     * SMTP (and probably other) date headers.
     * The format used is <tt>EEE, dd MMM yyyy HH:mm:ss Z</tt> in US locale.
     */
    public static final FastDateFormat SMTP_DATETIME_FORMAT
            = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    //-----------------------------------------------------------------------
    /**
     * <p>DateFormatUtils instances should NOT be constructed in standard programming.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public DateFormatUtils() {
        super();
    }

    /**
     * <p>Formats a date/time into a specific pattern using the UTC time zone.</p>
     * 
     * @param millis  the date to format expressed in milliseconds
     * @param pattern  the pattern to use to format the date, not null
     * @return the formatted date
     */
    public static String formatUTC(long millis, String pattern) {
        return format(new Date(millis), pattern, UTC_TIME_ZONE, null);
    }

    /**
     * <p>Formats a date/time into a specific pattern using the UTC time zone.</p>
     * 
     * @param date  the date to format, not null
     * @param pattern  the pattern to use to format the date, not null
     * @return the formatted date
     */
    public static String formatUTC(Date date, String pattern) {
        return format(date, pattern, UTC_TIME_ZONE, null);
    }
    
    /**
     * <p>Formats a date/time into a specific pattern using the UTC time zone.</p>
     * 
     * @param millis  the date to format expressed in milliseconds
     * @param pattern  the pattern to use to format the date, not null
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String formatUTC(long millis, String pattern, Locale locale) {
        return format(new Date(millis), pattern, UTC_TIME_ZONE, locale);
    }

    /**
     * <p>Formats a date/time into a specific pattern using the UTC time zone.</p>
     * 
     * @param date  the date to format, not null
     * @param pattern  the pattern to use to format the date, not null
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String formatUTC(Date date, String pattern, Locale locale) {
        return format(date, pattern, UTC_TIME_ZONE, locale);
    }
    
    /**
     * <p>Formats a date/time into a specific pattern.</p>
     * 
     * @param millis  the date to format expressed in milliseconds
     * @param pattern  the pattern to use to format the date, not null
     * @return the formatted date
     */
    public static String format(long millis, String pattern) {
        return format(new Date(millis), pattern, null, null);
    }

    /**
     * <p>Formats a date/time into a specific pattern.</p>
     * 
     * @param date  the date to format, not null
     * @param pattern  the pattern to use to format the date, not null
     * @return the formatted date
     */
    public static String format(Date date, String pattern) {
        return format(date, pattern, null, null);
    }

    /**
     * <p>Formats a calendar into a specific pattern.</p>
     * 
     * @param calendar  the calendar to format, not null
     * @param pattern  the pattern to use to format the calendar, not null
     * @return the formatted calendar
     * @see FastDateFormat#format(Calendar)
     * @since 2.4
     */
    public static String format(Calendar calendar, String pattern) {
        return format(calendar, pattern, null, null);
    }
    
    /**
     * <p>Formats a date/time into a specific pattern in a time zone.</p>
     * 
     * @param millis  the time expressed in milliseconds
     * @param pattern  the pattern to use to format the date, not null
     * @param timeZone  the time zone  to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String format(long millis, String pattern, TimeZone timeZone) {
        return format(new Date(millis), pattern, timeZone, null);
    }

    /**
     * <p>Formats a date/time into a specific pattern in a time zone.</p>
     * 
     * @param date  the date to format, not null
     * @param pattern  the pattern to use to format the date, not null
     * @param timeZone  the time zone  to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String format(Date date, String pattern, TimeZone timeZone) {
        return format(date, pattern, timeZone, null);
    }

    /**
     * <p>Formats a calendar into a specific pattern in a time zone.</p>
     * 
     * @param calendar  the calendar to format, not null
     * @param pattern  the pattern to use to format the calendar, not null
     * @param timeZone  the time zone  to use, may be <code>null</code>
     * @return the formatted calendar
     * @see FastDateFormat#format(Calendar)
     * @since 2.4
     */
    public static String format(Calendar calendar, String pattern, TimeZone timeZone) {
        return format(calendar, pattern, timeZone, null);
    }

    /**
     * <p>Formats a date/time into a specific pattern in a locale.</p>
     * 
     * @param millis  the date to format expressed in milliseconds
     * @param pattern  the pattern to use to format the date, not null
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String format(long millis, String pattern, Locale locale) {
        return format(new Date(millis), pattern, null, locale);
    }

    /**
     * <p>Formats a date/time into a specific pattern in a locale.</p>
     * 
     * @param date  the date to format, not null
     * @param pattern  the pattern to use to format the date, not null
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String format(Date date, String pattern, Locale locale) {
        return format(date, pattern, null, locale);
    }

    /**
     * <p>Formats a calendar into a specific pattern in a locale.</p>
     * 
     * @param calendar  the calendar to format, not null
     * @param pattern  the pattern to use to format the calendar, not null
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted calendar
     * @see FastDateFormat#format(Calendar)
     * @since 2.4
     */
    public static String format(Calendar calendar, String pattern, Locale locale) {
        return format(calendar, pattern, null, locale);
    }

    /**
     * <p>Formats a date/time into a specific pattern in a time zone  and locale.</p>
     * 
     * @param millis  the date to format expressed in milliseconds
     * @param pattern  the pattern to use to format the date, not null
     * @param timeZone  the time zone  to use, may be <code>null</code>
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String format(long millis, String pattern, TimeZone timeZone, Locale locale) {
        return format(new Date(millis), pattern, timeZone, locale);
    }

    /**
     * <p>Formats a date/time into a specific pattern in a time zone  and locale.</p>
     * 
     * @param date  the date to format, not null
     * @param pattern  the pattern to use to format the date, not null, not null
     * @param timeZone  the time zone  to use, may be <code>null</code>
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted date
     */
    public static String format(Date date, String pattern, TimeZone timeZone, Locale locale) {
        FastDateFormat df = FastDateFormat.getInstance(pattern, timeZone, locale);
        return df.format(date);
    }

    /**
     * <p>Formats a calendar into a specific pattern in a time zone  and locale.</p>
     * 
     * @param calendar  the calendar to format, not null
     * @param pattern  the pattern to use to format the calendar, not null
     * @param timeZone  the time zone  to use, may be <code>null</code>
     * @param locale  the locale to use, may be <code>null</code>
     * @return the formatted calendar
     * @see FastDateFormat#format(Calendar)
     * @since 2.4
     */
    public static String format(Calendar calendar, String pattern, TimeZone timeZone, Locale locale) {
        FastDateFormat df = FastDateFormat.getInstance(pattern, timeZone, locale);
        return df.format(calendar);
    }

}
