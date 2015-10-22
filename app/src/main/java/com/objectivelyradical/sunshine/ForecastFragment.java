package com.objectivelyradical.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
public class ForecastFragment extends Fragment {
    ArrayAdapter<String> adapter;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicates the fragment has items to contribute to the global options menu
        setHasOptionsMenu(true);
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
        ArrayList<String> forecastList = new ArrayList<String>(Arrays.asList(forecastArray));
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, forecastList);

        ListView list = (ListView)view.findViewById(R.id.listview_forecast);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecastText = (String) parent.getItemAtPosition(position);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra("FORECAST_TEXT", forecastText);
                startActivity(detailIntent);
            }
        });
        return view;
    }

    @Override
    // Update whenever the fragment starts
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        // Load initial data
        String location =  PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.settings_location_key),
                getString(R.string.settings_location_default));
        Log.d("ForecastFragment", location);
        new FetchWeatherTask(getActivity(), adapter).execute(location);
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
