package com.objectivelyradical.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.objectivelyradical.sunshine.Data.WeatherContract;

import java.net.URI;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    ShareActionProvider shareProvider;
    String detailString;
    View rootView;

    TextView mDay;
    TextView mDate;
    TextView mHigh;
    TextView mLow;
    TextView mDescription;
    TextView mPressure;
    TextView mWind;
    TextView mHumidity;
    ImageView mIcon;
    TestView mCompass;
    int mWeatherId;
    Uri mUri;

    private static final int DETAIL_LOADER_ID = 667;
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    static final int COL_HUMIDITY = 9;
    static final int COL_WIND_SPEED = 10;
    static final int COL_DEGREES = 11;
    static final int COL_PRESSURE = 12;
    static final int COL_WEATHER_ID = 13;

    public DetailActivityFragment() {

    }

    public static DetailActivityFragment initializeInstance(Uri uri) {
        DetailActivityFragment f = new DetailActivityFragment();

        Bundle args = new Bundle();
        args.putString("uri", uri.toString());
        f.setArguments(args);
        return f;
    }

    public Uri getBundledUri() {
        if(getArguments() != null) {
            return Uri.parse(getArguments().getString("uri", ""));
        } else {
            return null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mUri == null) {
            return null;
        }
        return new CursorLoader(getContext(), mUri, FORECAST_COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            detailString = convertCursorRowToUXFormat(data);
            if(shareProvider != null) {
                shareProvider.setShareIntent(createForecastShareIntent());
            }
            mWeatherId = data.getInt(COL_WEATHER_ID);

            long date = data.getLong(ForecastFragment.COL_WEATHER_DATE);
            // POPULATE THE text views
            mDay.setText(Utility.getDayName(getActivity(), date));
            mDate.setText(Utility.getFormattedMonthDay(getActivity(), date));

            mHigh.setText(getString(R.string.format_degrees, data.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP)));
            mLow.setText(getString(R.string.format_degrees, data.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP)));
            mHumidity.setText(getString(R.string.format_humidity, data.getFloat(COL_HUMIDITY)));
            mPressure.setText(getString(R.string.format_pressure, data.getFloat(COL_PRESSURE)));
            mIcon.setImageResource(Utility.weatherCodeToArtPath(mWeatherId));

            float windSpeed = data.getFloat(COL_WIND_SPEED);
            String windDirection = getWindDirection(data.getFloat(COL_DEGREES));
            mWind.setText(getString(R.string.format_wind_kmh, windSpeed, windDirection));
            if(mCompass != null) {
                mCompass.setAngle(degreesToRadians(data.getFloat(COL_DEGREES)));
            }

            mDescription.setText(data.getString(ForecastFragment.COL_WEATHER_DESC));
        }
    }

    private float degreesToRadians(float windDirection) {
        windDirection -= 90;
        return (float)(windDirection * (Math.PI / 180f));
    }

    private String getWindDirection(float degrees) {
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 337.5) {
            direction = "NW";
        }
        return direction;
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        detailString = "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mUri = getBundledUri();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mDay = (TextView)rootView.findViewById(R.id.detail_day_textview);
        mDate = (TextView)rootView.findViewById(R.id.detail_date_textview);
        mHigh = (TextView)rootView.findViewById(R.id.detail_high_textview);
        mLow = (TextView)rootView.findViewById(R.id.detail_low_textview);
        mDescription = (TextView)rootView.findViewById(R.id.detail_forecast_textview);
        mPressure = (TextView)rootView.findViewById(R.id.detail_pressure_textview);
        mWind = (TextView)rootView.findViewById(R.id.detail_wind_textview);
        mHumidity = (TextView)rootView.findViewById(R.id.detail_humidity_textview);
        mIcon = (ImageView)rootView.findViewById(R.id.detail_icon);
        mCompass = (TestView)rootView.findViewById(R.id.compass);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // The provider needs to store an intent when it is loaded!  It is not handled via
        // onOptionsItemSelected
        if(detailString != null) {
            shareProvider.setShareIntent(createForecastShareIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            Log.d("DetailActivityFragment", "SETTINGS!");
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createForecastShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, detailString);
        return intent;
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }
    private String formatHighLows(double high, double low) {
        String highLowStr = Utility.convertTempToSystemUnit(high, getContext()) + "/" +
                Utility.convertTempToSystemUnit(low, getContext());
        return highLowStr;
    }

    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if(uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }
    }
}