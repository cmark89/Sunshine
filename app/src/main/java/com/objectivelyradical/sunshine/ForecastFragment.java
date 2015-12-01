package com.objectivelyradical.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
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
import com.objectivelyradical.sunshine.sync.SunshineSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
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

    private int mSelectedIndex = 0;
    private static final String SELECTED_INDEX_KEY = "SCROLL_VIEW_INDEX";
    private boolean mUseTodayLayout = false;

    ForecastAdapter adapter;
    public ForecastFragment() {
    }

    public void setUseTodayLayout(boolean b) {
        mUseTodayLayout = b;
        if(adapter != null) {
            adapter.setUseTodayLayout(mUseTodayLayout);
        }
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
        ListView list = (ListView)getActivity().findViewById(R.id.listview_forecast);
        if(list != null) {
            list.smoothScrollToPosition(mSelectedIndex);
            list.setItemChecked(mSelectedIndex, true);
        }
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicates the fragment has items to contribute to the global options menu
        setHasOptionsMenu(true);
        if(savedInstanceState != null) {
            mSelectedIndex = savedInstanceState.getInt(SELECTED_INDEX_KEY);
        }
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
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {};

        String locationSetting = Utility.getPreferredLocation(getContext());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate
                (locationSetting, System.currentTimeMillis());
        Cursor cursor = getContext().getContentResolver().query(weatherForLocationUri, null, null,
                null, sortOrder);
        adapter = new ForecastAdapter(getContext(), cursor, 0);
        adapter.setUseTodayLayout(mUseTodayLayout);

        ListView list = (ListView)view.findViewById(R.id.listview_forecast);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                mSelectedIndex = position;
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                            cursor.getLong(COL_WEATHER_DATE));
                    ((Callback)(getActivity())).onItemSelected(uri);
                }
            }
        });
        return view;
    }

    @Override
    // Update whenever the fragment starts
    public void onStart() {
        super.onStart();
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
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

        float lat = 0;
        float lon = 0;

        String[] projection = new String[]{
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG,
        };
        final int LOCATION_COL = 0;
        final int LAT_COL = 1;
        final int LON_COL = 2;
        String location = PreferenceManager.getDefaultSharedPreferences(getContext()).getString
                (getString(R.string.settings_location_key), getString(R.string.settings_location_default));
        Cursor cr = getContext().getContentResolver().query(WeatherContract.LocationEntry
                .CONTENT_URI, projection, WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING +
                        "== ?", new String[] { location }, null);
        if(cr.moveToFirst()) {
            lat = cr.getFloat(LAT_COL);
            lon = cr.getFloat(LON_COL);
        }
        intent.setData(Uri.parse("geo:"+lat+","+lon));
        if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast toast = new Toast(getActivity());
            toast.setText("Unable to display location.  No map application found.");
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_INDEX_KEY, mSelectedIndex);
        super.onSaveInstanceState(outState);

    }



    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
