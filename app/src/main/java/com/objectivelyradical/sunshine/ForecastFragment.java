package com.objectivelyradical.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

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

        String[] forecastArray = {
                "Today - Rain, 19/22",
                "Tomorrow - Rain, 18/21",
                "Tuesday - Rain, 11/16",
                "Wednesday - Snow, -5/-1",
                "Thursday - Rain, 4/19",
                "Friday - Rain, 16/17",
                "Saturday - Rain, 12/18",
                "Sunday - Rain, 9/18"
        };
        ArrayList<String> forecastList = new ArrayList<String>(Arrays.asList(forecastArray));
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, forecastList);

        ListView list = (ListView)view.findViewById(R.id.listview_forecast);
        list.setAdapter(adapter);
        return view;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();
        if(id == R.id.action_refresh) {
            new FetchWeatherTask().execute("Tokorozawa");
        }
        return true;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] strings) {
            //super.onPostExecute(strings);
            adapter.clear();
            adapter.addAll(Arrays.asList(strings));
        }

        protected String[] doInBackground(String... city) {

            Uri.Builder uriBuilder = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily").buildUpon();
            uriBuilder.appendQueryParameter("q", city[0]);
            uriBuilder.appendQueryParameter("mode", "json");
            uriBuilder.appendQueryParameter("units", "metric");
            uriBuilder.appendQueryParameter("cnt", "7");

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonResponse = null;

            try {
                URL url = new URL(uriBuilder.toString());
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0) {
                    return null;
                }

                jsonResponse = buffer.toString();

            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch(final Exception e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return parseJson(jsonResponse);
            } catch(JSONException err) {
                Log.e(LOG_TAG, err.toString());
                err.printStackTrace();
            }
            return null;
        }

        private String[] parseJson (String jsonString) throws JSONException{

            JSONObject root = new JSONObject(jsonString);
            JSONArray days = root.getJSONArray("list");
            String[] forecast = new String[days.length()];

            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            for(int i = 0; i < days.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject weatherData = days.getJSONObject(i);
                JSONObject temperature = weatherData.getJSONObject("temp");
                JSONObject weather = (JSONObject)weatherData.getJSONArray("weather").getJSONObject(0);

                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                description = weather.getString("main");
                highAndLow = formatHighLows(temperature.getDouble("max"), temperature.getDouble("min"));


                forecast[i] = day + " - " + description + " - " + highAndLow ;
            }
            return forecast;
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }
    }
}
