package com.objectivelyradical.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, forecastArray);

        ListView list = (ListView)view.findViewById(R.id.listview_forecast);
        list.setAdapter(adapter);
        return view;
    }
}
