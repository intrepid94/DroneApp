package com.jerek.drone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;

public class StreamActivity extends AppCompatActivity {
    private MjpegView mv = null;
    private static final String TAG = "StreamActivity";
    private static final boolean DEBUG = false;
    private boolean suspending = false;
    final Handler handler = new Handler();
    private int mIP1 = 192;
    private int mIP2= 168;
    private int mIP3 = 1;
    private int mIP4 = 4;
    private int mIPPort = 8080;
    private String mIPCommand = "?action=stream";
    private String mURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

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

        mv = (MjpegView) findViewById(R.id.mv);
        if (mv != null) {
            mv.setResolution(640, 480);
        }
        new DoRead().execute("mURL");
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

    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume()");
        super.onResume();
        if (mv != null) {
            if (suspending) {
                new DoRead().execute(mURL);
                suspending = false;
            }
        }

    }

    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart()");
        super.onStart();
    }

    public void onPause() {
        if (DEBUG) Log.d(TAG, "onPause()");
        super.onPause();
        if (mv != null) {
            if (mv.isStreaming()) {
                mv.stopPlayback();
                suspending = true;
            }
        }
    }

    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop()");
        super.onStop();
    }

    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");

        if (mv != null) {
            mv.freeCameraMemory();
        }

        super.onDestroy();
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);
            if (DEBUG) Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if (DEBUG)
                    Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if (res.getStatusLine().getStatusCode() == 401) {
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-ClientProtocolException", e);
                }
                //Error connecting to camera
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-IOException", e);
                }
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);
            if (result != null) {
                result.setSkip(1);
                setTitle(R.string.app_name);
            } else {
                setTitle("disconnected");
            }
            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mv.showFps(false);
        }
    }

    public void setImageError() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTitle("error");
                return;
            }
        });
    }


}
