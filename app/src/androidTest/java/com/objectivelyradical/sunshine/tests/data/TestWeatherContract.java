package com.objectivelyradical.sunshine.tests.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by c.mark on 2015/10/19.
 */

import com.objectivelyradical.sunshine.Data.WeatherContract;

/*
    Students: This is NOT a complete test for the WeatherContract --- just for the functions
    that we expect you to write.
 */
public class TestWeatherContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_WEATHER_LOCATION = "/North Pole";
    private static final long TEST_WEATHER_DATE = 1419033600L;  // December 20th, 2014

    /*
        Students: Uncomment this out to test your weather location function.
     */
    public void testBuildWeatherLocation() {
        Uri locationUri = WeatherContract.WeatherEntry.buildWeatherLocation(TEST_WEATHER_LOCATION);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildWeatherLocation in " +
                        "WeatherContract.",
                locationUri);
        assertEquals("Error: Weather location not properly appended to the end of the Uri",
                TEST_WEATHER_LOCATION, locationUri.getLastPathSegment());
        assertEquals("Error: Weather location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.objectivelyradical.sunshine/weather/%2FNorth%20Pole");
    }
}