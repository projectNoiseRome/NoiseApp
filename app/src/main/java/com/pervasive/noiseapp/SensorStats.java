package com.pervasive.noiseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SensorStats extends AppCompatActivity {

    private String sensorName;
    private JSONObject values;
    private final String  SENSOR_VALUES = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorValues";
    private ProgressDialog mProgressDialog;
    private PieChart pieChart;
    private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        sensorName = intent.getStringExtra("sensorName");
        //RETRIEVE THE DATA
        GetSensorValues task = new GetSensorValues();
        try {
            showProgressDialog();
            String result = task.execute("").get();
            hideProgressDialog();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //CREATE THE CHART
        pieChart = (PieChart)findViewById(R.id.idPieChart);
        Description description = new Description();
        description.setText("Sensor values per day");
        pieChart.setDescription(description);
        pieChart.setRotationEnabled(true);

        pieChart.setHoleRadius(25f);
        pieChart.setDrawEntryLabels(true);
        addDataSet(pieChart);

    }

    private void addDataSet(PieChart pieChart) {
        ArrayList<PieEntry> yEntrys = new ArrayList<PieEntry>();
        ArrayList<String> xEntrys = new ArrayList<String>();
        JSONArray list = new JSONArray();
        String day = "";

        try {
            list = values.getJSONArray(sensorName);
            JSONObject temp = new JSONObject();
            //Cycle for exploring the array
            for (int i = 0; i < list.length(); i++) {
                temp = list.getJSONObject(i);
                day = temp.getString("dayweek");
                //TODO: ADD ELEMENT TO THE CHART
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    //HTTP CALL
    //Get the sensor's values
    private class GetSensorValues extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            JSONArray list = new JSONArray();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(SENSOR_VALUES).newBuilder();
            urlBuilder.addQueryParameter("sensorName", sensorName);
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        json = new JSONObject(result);
                        values = json;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return "Successfully obtained the sensor's values";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Download failed";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(SensorStats.this, result, Toast.LENGTH_LONG).show();
        }
    }

    //Loading widget
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(SensorStats.this);
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




