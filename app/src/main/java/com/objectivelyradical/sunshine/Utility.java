package com.objectivelyradical.sunshine;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.objectivelyradical.sunshine.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.settings_location_key),
                context.getString(R.string.settings_location_default));
    }


    public static String convertTempToSystemUnit(double temp, Context context) {
        int units = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).
                getString(context.getString(R.string.settings_units_key), "1"));

        double newTemp = 0;
        switch(units) {
            case(2):
                // Imperial
                newTemp = (temp * 1.8f) + 32;
                return context.getString(R.string.format_degrees, newTemp);
            case(3):
                // Kelvin
                newTemp = temp + 273.15f;
                return context.getString(R.string.format_degrees, newTemp);
            case(4):
                // Rankine
                newTemp = (temp + 273.15) * 1.8f;
                return context.getString(R.string.format_degrees, newTemp);
            case(5):
                // Wacky
                Random rand = new Random();
                return context.getString(R.string.format_degrees, (rand.nextInt(200) - 100));
            default:
                // Celsius
                return context.getString(R.string.format_degrees, temp);
        }
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static int weatherCodeToIconPath(int code) {
        if(code >= 200 && code <= 232) {
            return R.drawable.ic_storm;
        } else if (code >= 300 && code <=321) {
            return R.drawable.ic_light_rain;
        } else if (code >= 400 && code <= 531) {
            return R.drawable.ic_rain;
        } else if (code >= 600 && code <= 622) {
            return R.drawable.ic_snow;
        } else if (code >= 700 && code <= 781) {
            return R.drawable.ic_fog;
        } else if(code == 800) {
            return R.drawable.ic_clear;
        } else if (code >= 801 && code <= 802) {
            return R.drawable.ic_light_clouds;
        } else if (code >= 803 && code <= 804) {
            return R.drawable.ic_cloudy;
        } else {
            return R.drawable.ic_storm;
        }
    }

    public static int weatherCodeToArtPath(int code) {
        if(code >= 200 && code <= 232) {
            return R.drawable.art_storm;
        } else if (code >= 300 && code <=321) {
            return R.drawable.art_light_rain;
        } else if (code >= 400 && code <= 531) {
            return R.drawable.art_rain;
        } else if (code >= 600 && code <= 622) {
            return R.drawable.art_snow;
        } else if (code >= 700 && code <= 781) {
            return R.drawable.art_fog;
        } else if(code == 800) {
            return R.drawable.art_clear;
        } else if (code >= 801 && code <= 802) {
            return R.drawable.art_light_clouds;
        } else if (code >= 803 && code <= 804) {
            return R.drawable.art_clouds;
        } else {
            return R.drawable.art_storm;
        }
    }
}