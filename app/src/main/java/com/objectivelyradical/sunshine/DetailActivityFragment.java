package com.objectivelyradical.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    ShareActionProvider shareProvider;
    String detailString;
    public DetailActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if(intent != null && intent.hasExtra("FORECAST_TEXT")) {
            detailString = intent.getStringExtra("FORECAST_TEXT");
            TextView text = (TextView)rootView.findViewById(R.id.detail_view_text);
            text.setText(detailString);
        }

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
        if(shareProvider != null) {
            shareProvider.setShareIntent(createForecastShareIntent());
            Log.d("DetailActivityFragment", "Share Intent created successfully.");

        } else {
            Log.d("DetailActivityFragment", "Share Action Provider is null for some reason.");
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
}