package com.jerek.drone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;


public class MainActivity extends AppCompatActivity {

    private Button mStreamButton;
    private TextView mStreamText;
    private int mIP1 = 192;
    private int mIP2= 168;
    private int mIP3 = 1;
    private int mIP4 = 4;
    private int mIPPort = 8080;
    private String mIPCommand = "?action=stream";
    private String mURL;

    private static final String TAG = "MainActivity";

    static {
        if(!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded2");
        }
        else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main);


        mStreamButton = (Button) findViewById(R.id.stream_button);
        mStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, StreamActivity.class);
                startActivity(i);
            }
        });


        mStreamText = (TextView) findViewById(R.id.stream_string);

        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings){
            Intent settings_intent = new Intent(this, SettingsActivity.class);
            startActivity(settings_intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadData()
    {
        SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        mIP1 = preferences.getInt("ip_ad1", mIP1);
        mIP2 = preferences.getInt("ip_ad2", mIP2);
        mIP3 = preferences.getInt("ip_ad3", mIP3);
        mIP4 = preferences.getInt("ip_ad4", mIP4);
        mIPPort = preferences.getInt("ip_port", mIPPort);
        mIPCommand = preferences.getString("ip_command", mIPCommand);

        StringBuilder sb = new StringBuilder();
        String sb_http = "http://";
        String sb_dot = ".";
        String sb_colon = ":";
        String sb_slash = "/";

        sb.append(sb_http);
        sb.append(mIP1);
        sb.append(sb_dot);
        sb.append(mIP2);
        sb.append(sb_dot);
        sb.append(mIP3);
        sb.append(sb_dot);
        sb.append(mIP4);
        sb.append(sb_colon);
        sb.append(mIPPort);
        sb.append(sb_slash);
        sb.append(mIPCommand);
        mURL = new String(sb);
        mStreamText.setText(mURL);
    }
}

