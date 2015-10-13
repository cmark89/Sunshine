/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.objectivelyradical.sunshine.tests.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.objectivelyradical.sunshine.Data.WeatherContract;
import com.objectivelyradical.sunshine.Data.WeatherDbHelper;
import com.objectivelyradical.sunshine.Data.WeatherContract.LocationEntry;
import com.objectivelyradical.sunshine.Data.WeatherContract.WeatherEntry;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.
        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();

        // (you can use the createNorthPoleLocationValues if you wish)

        // Insert ContentValues into database and get a row ID back

        long rowId = insertLocation(db);
        assertTrue("Failed to insert table row.", rowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(LocationEntry.TABLE_NAME, null, "_ID = ?", new String[]{Long.toString(rowId)}, null,
                null, null, null);
        assertNotNull("The returned cursor is null.", cursor);

        // Move the cursor to a valid database row
        cursor.moveToFirst();

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        assertEquals("City name TOKOROZAWA not found.", "Tokorozawa", cursor.getString(cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME)));
        assertEquals("Location setting 359-1143 not found.", "359-1143", cursor.getString(cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING)));
        assertEquals("Latitude 35.8000 not found.", 35.8000f, cursor.getFloat(cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT)));
        assertEquals("Latitude 139.4667 not found.", 139.4667f, cursor.getFloat(cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG)));

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
        long locationId = insertLocation(db);

        assertTrue("Failed to insert location table row.", locationId != -1);

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues values = new ContentValues();
        values.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
        values.put(WeatherEntry.COLUMN_DATETEXT, 19990101);
        values.put(WeatherEntry.COLUMN_DEGREES, 100f);
        values.put(WeatherEntry.COLUMN_HUMIDITY, 0f);
        values.put(WeatherEntry.COLUMN_MAX_TEMP, 5000f);
        values.put(WeatherEntry.COLUMN_MIN_TEMP, -1000f);
        values.put(WeatherEntry.COLUMN_PRESSURE, 23f);
        values.put(WeatherEntry.COLUMN_SHORT_DESC, "rainy");
        values.put(WeatherEntry.COLUMN_WEATHER_ID, 77);
        values.put(WeatherEntry.COLUMN_WIND_SPEED, 23.23f);

        long weatherId = db.insert(WeatherEntry.TABLE_NAME, null, values);

        assertTrue("Failed to insert weather table row.", weatherId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(WeatherEntry.TABLE_NAME, null, null, null, null, null, null, null);

        assertTrue("Failed to move cursor to first row.", cursor.moveToFirst());

        assertEquals("Location ID incorrect.", locationId, cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY)));
        assertEquals("Datetext incorrect.", 19990101, cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
        assertEquals("Degrees incorrect.", 100f, cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES)));
        assertEquals("Humidity incorrect.", 0f, cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)));
        assertEquals("Max temp incorrect.", 5000f, cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)));
        assertEquals("Min temp incorrect.", -1000f, cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)));
        assertEquals("Pressure incorrect.", 23f, cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)));
        assertEquals("Short description incorrect.", "rainy", cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)));
        assertEquals("Weather ID incorrect.", 77, cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID)));
        assertEquals("Wind speed incorrect.", 23.23f, cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED)));

        assertFalse("Table contains multiple rows of data.", cursor.moveToNext());

        cursor.close();
        db.close();

        // Finally, close the cursor and database
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
    public long insertLocation(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, "Tokorozawa");
        values.put(LocationEntry.COLUMN_COORD_LAT, "35.8000");
        values.put(LocationEntry.COLUMN_COORD_LONG, "139.4667");
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, "359-1143");

        return db.insert(LocationEntry.TABLE_NAME, null, values);
    }
}