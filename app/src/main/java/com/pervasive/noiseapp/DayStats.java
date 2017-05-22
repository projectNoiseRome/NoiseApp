package com.pervasive.noiseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DayStats extends AppCompatActivity {

    private String day;
    private String sensorName;
    private int dayInt;
    private ProgressDialog mProgressDialog;
    private ArrayList<BarEntry> entries = new ArrayList<>();
    private ArrayList<String> labels = new ArrayList<String>();
    private JSONObject values;
    private final String  SENSOR_VALUES = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorValues";
    private BarData data;
    private BarChart chart;
    private int[] noiseValues;
    private String[] xValue = {"1-8", "9-14", "15-20", "21-23"};
    private float[]  yValue = {1, 2, 3, 4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_stats);
        chart = (BarChart) findViewById(R.id.chart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        day = intent.getStringExtra("day");
        sensorName = intent.getStringExtra("sensorName");
        noiseValues = new int[4];
        //Toast.makeText(getApplicationContext(), day, Toast.LENGTH_LONG).show();
        switch(day){
            case "Sunday":
                dayInt = 0;
                break;
            case "Monday":
                dayInt = 1;
                break;
            case "Tuesday":
                dayInt = 2;
                break;
            case "Wednesday":
                dayInt = 3;
                break;
            case "Thursday":
                dayInt = 4;
                break;
            case "Friday":
                dayInt = 5;
                break;
            case "Saturday":
                dayInt = 6;
                break;
        }
        //Toast.makeText(getApplicationContext(), Integer.toString(dayInt), Toast.LENGTH_LONG).show();
        GetDaySensorValues g = new GetDaySensorValues(day);
        g.execute("");



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


    //HTTP CALL
    //Get the sensor's values
    private class GetDaySensorValues extends AsyncTask<String, Void, String> {

        private String day;
        private JSONArray dayStats = new JSONArray();
        private JSONObject json;


        public GetDaySensorValues(String day){
            this.day = day;
        }


        @Override
        protected void onPreExecute(){
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            JSONObject temp = new JSONObject();
            JSONArray array;
            String dayTemp = Integer.toString(dayInt);
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
                        array = json.getJSONArray(sensorName);
                        for(int i = 0; i < array.length(); i++){
                            temp = array.getJSONObject(i);
                            Log.d("In the day Stast", "Before retriving data");
                            if(temp.getString("dayweek").equals(dayTemp)){

                                Log.d("In the day Stast", "Added value for day ");
                                dayStats.put(temp);
                                Log.d("day", temp.getString("dayweek"));

                            }

                        }
                        makeMedia(dayStats, noiseValues);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return "Successfully obtained the sensor's values";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Error while retriving data";
        }

        private void makeMedia(JSONArray dayStats, int[] noiseValues) {
            List<Integer> first = new ArrayList<Integer>(); //1-8
            List<Integer> two = new ArrayList<Integer>();   //9-14
            List<Integer> third = new ArrayList<Integer>(); //15-20
            List<Integer> four = new ArrayList<Integer>();  //21-24
            int hour;
            try {
                //Add the values to the right hour group
                for (int i = 0; i < dayStats.length(); i++) {
                    JSONObject temp = dayStats.getJSONObject(i);
                    hour = temp.getInt("hour");
                    if(hour < 9)
                        first.add(temp.getInt("noiseLevel"));
                    else if(hour >= 9 && hour < 15)
                        two.add(temp.getInt("noiseLevel"));
                    else if(hour >= 15 && hour < 21)
                        third.add(temp.getInt("noiseLevel"));
                    else
                        four.add(temp.getInt("noiseLevel"));
                }
                int tempsum = 0;
                if(first.size() > 0) {
                    for (int i = 0; i < first.size(); i++) {
                        tempsum += first.get(i);
                        Log.d("Inside first", Integer.toString(tempsum));
                    }
                    noiseValues[0] = tempsum / first.size();
                }
                else{
                    Log.d("Inside first", Integer.toString(tempsum));
                    noiseValues[0] = 0;
                }
                if(two.size() > 0) {
                    tempsum = 0;
                    for (int i = 0; i < two.size(); i++) {
                        tempsum += two.get(i);
                        Log.d("Inside second", Integer.toString(tempsum));
                    }
                    noiseValues[1] = tempsum / two.size();
                }
                else{
                    tempsum = 0;
                    Log.d("Inside second", Integer.toString(tempsum));
                    noiseValues[1] = 0;
                }
                if(third.size() > 0) {
                    tempsum = 0;
                    for (int i = 0; i < third.size(); i++) {
                        tempsum += third.get(i);
                        Log.d("Inside third", Integer.toString(tempsum));
                    }
                    noiseValues[2] = tempsum / third.size();
                }
                else{
                    tempsum = 0;
                    Log.d("Inside third", Integer.toString(tempsum));
                    noiseValues[2] = 0;
                }

                if(four.size() > 0) {
                    tempsum = 0;
                    for (int i = 0; i < four.size(); i++) {
                        tempsum += four.get(i);
                        Log.d("Inside fourth", Integer.toString(tempsum));
                    }
                    noiseValues[3] = tempsum / four.size();
                }
                else{
                    tempsum = 0;
                    Log.d("Inside fourth", Integer.toString(tempsum));
                    noiseValues[3] = 0;
                }

            }catch(JSONException e){
                e.printStackTrace();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(DayStats.this, result, Toast.LENGTH_LONG).show();
            data = new BarData(getDataSet());
            chart.setData(data);
            Description description = new Description();
            description.setText(day);
            chart.setDescription(description);
            chart.animateXY(2000, 2000);
            chart.invalidate();

            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    int index = 0;
                    Log.d("e.getY() value : ", Float.toString(e.getY()));
                    for(int i = 0; i < noiseValues.length; i++){
                        if(e.getY() == noiseValues[i]){
                            index = i;
                            break;
                        }
                    }
                    Toast.makeText(DayStats.this, "Time slot : \n"+xValue[index], Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNothingSelected() {
                }
            });
            hideProgressDialog();
        }
    }



    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private ArrayList<IBarDataSet> getDataSet() {
        ArrayList<IBarDataSet> dataSets = null;
        //yVals.add(new BarEntry(LocalTeam.getRedCard(),"Rojas"),1 *2f);


        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        BarEntry v1e1 = new BarEntry(0, noiseValues[0]); // Jan
        Log.d("noiseValue[0]", Integer.toString(noiseValues[0]));
        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry(1, noiseValues[1]); // Feb
        Log.d("noiseValue[1]", Integer.toString(noiseValues[1]));
        valueSet1.add(v1e2);
        BarEntry v1e3 = new BarEntry(2, noiseValues[2]); // Mar
        Log.d("noiseValue[2]", Integer.toString(noiseValues[2]));
        valueSet1.add(v1e3);
        BarEntry v1e4 = new BarEntry(3, noiseValues[3]); // Apr
        Log.d("noiseValue[3]", Integer.toString(noiseValues[3]));
        valueSet1.add(v1e4);

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, sensorName);
        barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);

        dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(barDataSet1);
        return dataSets;
    }


}
