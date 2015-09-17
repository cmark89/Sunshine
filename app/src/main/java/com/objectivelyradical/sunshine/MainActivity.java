package com.objectivelyradical.sunshine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import java.io.Console;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onStop() {
        Log.d("MainActivity", "Lifecycle - onStop()");
        super.onStop();
    }

    @Override
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
}
