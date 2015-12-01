package com.objectivelyradical.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.objectivelyradical.sunshine.Data.WeatherContract;
import com.objectivelyradical.sunshine.sync.SunshineSyncAdapter;

import java.io.Console;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{
    String mLocation;
    boolean mTwoPane = false;
    final String DETAIL_FRAGMENT_TAG = "DFTAG";

    @Override
    protected void onStop() {
        Log.d("MainActivity", "Lifecycle - onStop()");
        super.onStop();
    }

    protected void onDestroy() {
        Log.d("MainActivity", "Lifecycle - onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity", "Lifecycle - onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("MainActivity", "Lifecycle - onResume()");
        super.onResume();

        String newLocation = PreferenceManager.getDefaultSharedPreferences(this).getString(
            getString(R.string.settings_location_key), getString(R.string.settings_location_default));

        if(newLocation != null && !newLocation.equals(mLocation)) {
            Log.d("MainActivity", "UPDATE LOCATIONS");
            Log.d("MainActivity", mLocation + " --> " + newLocation);
            mLocation = newLocation;
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if(ff != null) {
                Log.d("MainActivity", "Update ForecastFragment");
                ff.onLocationChanged();
            }
            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if(df != null) {
                Log.d("MainActivity", "Update DetailFragment");
                df.onLocationChanged(mLocation);
            }
        }
    }

    @Override
    protected void onStart() {
        Log.d("MainActivity", "Lifecycle - onStart()");
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "Lifecycle - onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = PreferenceManager.getDefaultSharedPreferences(this).getString
                (getString(R.string.settings_location_key), getString(R.string.settings_location_default));
        if(findViewById(R.id.weather_detail_container) != null) {
            // This is a two pane layout on a tablet
            mTwoPane = true;
            if(savedInstanceState == null) {
                // We are just starting up, so add the second fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(),
                                DETAIL_FRAGMENT_TAG).commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        ForecastFragment ff = (ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        ff.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(id == R.id.action_settings) {
                Intent intent  = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if(mTwoPane) {
            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container,
                    DetailActivityFragment.initializeInstance(dateUri)).commit();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class).setData(dateUri);
            startActivity(detailIntent);
        }
    }
}
