package com.objectivelyradical.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import com.objectivelyradical.sunshine.Data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final static int FORECAST_LOADER_ID = 666;
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
    };
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTINGS = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    ForecastAdapter adapter;
    public ForecastFragment() {
    }

    // Loader Callbacks:
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(getContext());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate
                (locationSetting, System.currentTimeMillis());
        // CursorLoader extends AsyncTaskLoader<Cursor>, so our return type makes sense
        return new CursorLoader(getContext(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicates the fragment has items to contribute to the global options menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items stored in the forecastfragment xml file
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {};

        String locationSetting = Utility.getPreferredLocation(getContext());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate
                (locationSetting, System.currentTimeMillis());
        Cursor cursor = getContext().getContentResolver().query(weatherForLocationUri, null, null,
                null, sortOrder);
        adapter = new ForecastAdapter(getContext(), cursor, 0);

        ListView list = (ListView)view.findViewById(R.id.listview_forecast);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class).setData
                            (WeatherContract.WeatherEntry.buildWeatherLocationWithDate
                                    (locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                    startActivity(detailIntent);
                }
            }
        });
        return view;
    }

    @Override
    // Update whenever the fragment starts
    public void onStart() {
        super.onStart();
        //updateWeather();
    }

    private void updateWeather() {
        Log.d("ForecastFragment", "updateWeather()");
        // Load initial data
        String location =  PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.settings_location_key),
                getString(R.string.settings_location_default));
        Log.d("ForecastFragment", location);
        new FetchWeatherTask(getActivity()).execute(location);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();
        if(id == R.id.action_refresh) {
           updateWeather();
        } else if(id == R.id.action_view_location) {
            viewPreferredLocation();
        }
        return true;
    }

    public void onLocationChanged() {
        Log.d("ForecastFragment", "onLocationChanged()");
        updateWeather();

        // If you use initLoader here, since the ID is already associated with a loader,
        // it will NOT rerun it, but instead just replace the callbacks... restartLoader
        // will suspend and destroy it, and then REPLACE it with a new one that then runs
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    private void viewPreferredLocation() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String location = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
        getString(R.string.settings_location_key), "");
        intent.setData(Uri.parse("geo:0,0?q="+location));
        if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast toast = new Toast(getActivity());
            toast.setText("Unable to display location.  No map application found.");
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
