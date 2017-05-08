package com.pervasive.noiseapp;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Stats extends AppCompatActivity {

    ListView sensorView;
    private JSONObject sensorList = new JSONObject();
    private final String SENSOR_LIST = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorList";
    private ProgressDialog mProgressDialog;
    public static int[] prgmImages = {R.drawable.arduino, R.drawable.raspberry};
    public static String[] prgmNameList = {"ArduinoUno", "Raspberry"};
    private String[] sensor_name;
    private String[] sensor_position;
    private int[] images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //GET THE SENSOR LIST
        //Now we got our sensor - WAIT FOR THE RESULT
        getSensorList task = new getSensorList();
        try {
            showProgressDialog();
            String result = task.execute("").get();
            hideProgressDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        sensorView = (ListView)findViewById(R.id.listView);

        /*
        We will use a custom listview adapter(CustomAdapter):
        we have three array, one with the sensor name, one with the sensor position, one with the images
        */

        try {
            JSONArray list = sensorList.getJSONArray("sensors");
            sensor_name = new String[list.length()];
            sensor_position = new String[list.length()];
            images = new int[list.length()];
            for(int i = 0; i < list.length(); i++){
                JSONObject sensor = list.getJSONObject(i);
                //LatLng pos = new LatLng(Double.parseDouble(sensor.getString("latitude")), Double.parseDouble(sensor.getString("longitude")));
                String name = sensor.getString("sensorName");
                String latitude = sensor.getString("latitude");
                String longitude = sensor.getString("longitude");
                sensor_name[i] = name;
                sensor_position[i] = "Latitude: "+latitude.substring(0, 5)+", Longitude: " + longitude.substring(0, 5);
                if(name.contains("Arduino")||name.contains("Genuino")){
                    images[i] = R.drawable.arduino;
                }
                else{
                    images[i] = R.drawable.raspberry;
                }
            }
        } catch (Exception e) {
            Toast.makeText(Stats.this, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        sensorView.setAdapter(new CustomAdapter(this, sensor_name, sensor_position, images));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // Launch the correct Activity here
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Get the sensors
    private class getSensorList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            JSONArray list = new JSONArray();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(SENSOR_LIST).newBuilder();
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        json = new JSONObject(result);
                        sensorList = json;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return "Loaded all the sensors on the map";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Download failed";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(Stats.this, result, Toast.LENGTH_LONG).show();
        }
    }

    //Loading widget
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(Stats.this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    //Loading widget
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}
