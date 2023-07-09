package org.elegantobjects.jpages.LibraryAppTest;

import org.elegantobjects.jpages.LibraryApp.common.util.HumanDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.junit.runners.Parameterized.*;

/**
 * HumanDateTest - Unit tests for HumanDate class.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */
@RunWith(Parameterized.class)
public class HumanDateTest {

    private final Instant testDateTimeInstant;

    private final String expectedDateTimeString;
    private final String expectedDateStr;
    private final String expectedTimeStr;
    private final String expectedTimeAgoStr;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { Instant.parse("2020-02-01T00:00:00.00Z"), "2020-02-01 00:00:00", "2020-02-01", "00:00:00", "2 years ago" },
            { Instant.parse("2020-02-01T00:00:01.00Z"), "2020-02-01 00:00:01", "2020-02-01", "00:00:01", "2 years ago" },
            { Instant.parse("2020-02-01T00:01:00.00Z"), "2020-02-01 00:01:00", "2020-02-01", "00:01:00", "2 years ago" },
            { Instant.parse("2020-02-01T01:00:00.00Z"), "2020-02-01 01:00:00", "2020-02-01", "01:00:00", "2 years ago" },
            { Instant.parse("2020-02-02T00:00:00.00Z"), "2020-02-02 00:00:00", "2020-02-02", "00:00:00", "2 years ago" },
            { Instant.parse("2020-03-01T00:00:00.00Z"), "2020-03-01 00:00:00", "2020-03-01", "00:00:00", "2 years ago" },
            { Instant.parse("2021-02-01T00:00:35.00Z"), "2021-02-01 00:00:35", "2021-02-01", "00:00:35", "2 years ago" },
            { Instant.parse("2021-02-01T00:59:00.00Z"), "2021-02-01 00:59:00", "2021-02-01", "00:59:00", "2 years ago" },
            { Instant.parse("2022-02-01T00:00:00.00Z"), "2022-02-01 00:00:00", "2022-02-01", "00:00:00", "2 years ago" },
        });
    }

    static final long ONE_SECOND = 1000L;
    static final long ONE_MINUTE = ONE_SECOND * 60;
    static final long ONE_HOUR   = ONE_MINUTE * 60;
    static final long ONE_DAY    = ONE_HOUR * 24;
    static final long ONE_MONTH  = ONE_DAY * 30;
    static final long ONE_YEAR   = ONE_DAY * 365;

    public
    HumanDateTest(Instant testDateTimeInstant, String expectedDateTimeString, String expectedDateStr, String expectedTimeStr, String expectedTimeAgoStr) {
        this.testDateTimeInstant = testDateTimeInstant;
        this.expectedDateTimeString = expectedDateTimeString;
        this.expectedDateStr = expectedDateStr;
        this.expectedTimeStr = expectedTimeStr;
        this.expectedTimeAgoStr = expectedTimeAgoStr;
    }


    @Test
    public void Calling_toDateTimeStr_returns_DateTime_string_is_Success() {
        Assert.assertEquals(expectedDateTimeString,
            new HumanDate(testDateTimeInstant, ZoneId.of("UTC")).toDateTimeStr()
        );
    }

    @Test
    public void Calling_toDateStr_returns_Date_string_is_Success() {
        assertEquals(expectedDateStr,
            new HumanDate(testDateTimeInstant, ZoneId.of("UTC")).toDateStr()
        );
    }

    @Test
    public void Calling_toTimeStr_returns_Time_string_is_Success() {
        assertEquals(expectedTimeStr,
            new HumanDate(testDateTimeInstant, ZoneId.of("UTC")).toTimeStr()
        );
    }

    @Test
    public void Calling_toTimeAgoStr_for_Two_Years_Ago_is_Success() {
        // • ARRANGE
        long now = testDateTimeInstant.toEpochMilli();
        long oneYearAgo = testDateTimeInstant.toEpochMilli() - ONE_YEAR * 2;

        // • ACT & ASSERT
        assertEquals(expectedTimeAgoStr,
            new HumanDate(oneYearAgo, ZoneId.of("UTC")).toTimeAgoStr(now)
        );
    }

    @Test
    public void Calling_ToTimeAgo_on_All_Time_period_Strings() {
        // • ARRANGE
        long now = testDateTimeInstant.toEpochMilli();
        long timeInTheFuture = now + ONE_SECOND;
        long timeAgoJustNow  = now - ONE_SECOND + 500; // 500ms
        long timeAgoSeconds  = now - ONE_SECOND * 10;
        long timeAgoMinutes  = now - ONE_MINUTE * 10;
        long timeAgoHours    = now - ONE_HOUR   * 10;
        long timeAgoDays     = now - ONE_DAY    * 10;
        long timeAgoMonths   = now - ONE_MONTH  * 10;
        long timeAgoYears    = now - ONE_YEAR   * 10;

        // • ACT & ASSERT
        assertEquals("in the future",  new HumanDate(timeInTheFuture).toTimeAgoStr(now));
        assertEquals("just now",       new HumanDate(timeAgoJustNow).toTimeAgoStr(now));
        assertEquals("10 seconds ago", new HumanDate(timeAgoSeconds).toTimeAgoStr(now));
        assertEquals("10 minutes ago", new HumanDate(timeAgoMinutes).toTimeAgoStr(now));
        assertEquals("10 hours ago",   new HumanDate(timeAgoHours).toTimeAgoStr(now));
        assertEquals("10 days ago",    new HumanDate(timeAgoDays).toTimeAgoStr(now));
        assertEquals("10 months ago",  new HumanDate(timeAgoMonths).toTimeAgoStr(now));
        assertEquals("10 years ago",   new HumanDate(timeAgoYears).toTimeAgoStr(now));
    }
}