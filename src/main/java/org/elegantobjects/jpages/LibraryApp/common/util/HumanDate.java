package org.elegantobjects.jpages.LibraryApp.common.util;

import org.jetbrains.annotations.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * HumanDate - Utility class for converting epoch millis to human-readable date/time strings.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class HumanDate {

    private final long epochMillis;

    public
    HumanDate(long epochMillis) {
        this.epochMillis = epochMillis;
    }

    public String toDateTimeStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(epochMillis));
    }

    public String toDateStr() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(epochMillis));
    }

    public String toTimeStr() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(epochMillis));
    }

    public String toTimeAgoStr(@Nullable Long nowMillis) {
        if (nowMillis == null) {
            nowMillis = System.currentTimeMillis();
        }
        long diff = nowMillis - epochMillis;

        if (diff < 0) {
            return "in the future";
        }
        if (diff < 1000) {
            return "just now";
        }
        if (diff < 60 * 1000) {
            return diff / 1000 + " seconds ago";
        }
        if (diff < 60 * 60 * 1000) {
            return diff / (60 * 1000) + " minutes ago";
        }
        if (diff < 24 * 60 * 60 * 1000) {
            return diff / (60 * 60 * 1000) + " hours ago";
        }
        if (diff < 30 * 24 * 60 * 60 * 1000L) {
            return diff / (24 * 60 * 60 * 1000) + " days ago";
        }
        if (diff < 365 * 24 * 60 * 60 * 1000L) {
            return diff / (30 * 24 * 60 * 60 * 1000L) + " months ago";
        }
        return diff / (365 * 24 * 60 * 60 * 1000L) + " years ago";
    }
    public String toTimeAgoStr() {
        return toTimeAgoStr(null);
    }
}
