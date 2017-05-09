package com.pervasive.noiseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

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
    private ProgressDialog mProgressDialog;
    private JSONObject values;
    private final String  SENSOR_AVG = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getAvgValues";
    private PieChart pieChart;
    private String[] xValue = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private float[]  yValue = new float[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        sensorName = intent.getStringExtra("sensorName");


        //CREATE THE CHART
        pieChart = (PieChart)findViewById(R.id.idPieChart);
        Description description = new Description();
        description.setText("Sensor values per day");
        description.setTextSize(15);
        pieChart.setDescription(description);
        pieChart.setRotationEnabled(true);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Avg per day");
        pieChart.setHoleRadius(40f);
        pieChart.setDrawEntryLabels(true);
        try {
            addDataSet();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void addDataSet() throws JSONException, ExecutionException, InterruptedException {
        final ArrayList<PieEntry> yEntrys = new ArrayList<PieEntry>();
        ArrayList<String> xEntrys = new ArrayList<String>();
        showProgressDialog();
        for (int i = 0; i < yValue.length; i++) {
            GetSensorValues task = new GetSensorValues(0);
            task.setDay(i);
            String result = task.execute("").get();
            yValue[i] = Float.parseFloat(values.getString("avgNoise"));
            if(yValue[i] == 0){
                //TODO: 10 is default values, means that there are no data on that specific day
                yValue[i] = 10;
                yEntrys.add(new PieEntry(yValue[i], i));
            }
            else{
                yEntrys.add(new PieEntry(yValue[i], i));
            }
        }
        for(int i = 0; i < xValue.length; i++){
            xEntrys.add(xValue[i]);
        }
        hideProgressDialog();
        mProgressDialog.dismiss();
        //Create dataSet
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "S, M, T, W, T, F, S");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(15);

        //Add color to dataSet
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for(int i = 0; i < yValue.length; i++){
            System.out.println("Noise avg : " + yValue[i]);
            if(yValue[i] < 40){
                colors.add(Color.GREEN);
            }
            else if(yValue[i] >= 40 && yValue[i] < 60){
                colors.add(Color.YELLOW);
            }
            else{
                colors.add(Color.RED);
            }
        }

        pieDataSet.setColors(colors);
        //Add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        legend.setEnabled(true);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = 0;
                for(int i = 0; i < yValue.length; i++){
                    if(e.getY() == yValue[i]){
                        index = i;
                    }
                }
                Toast.makeText(SensorStats.this, xValue[index], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {
                }
            });

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


    //HTTP CALL
    //Get the sensor's values
    private class GetSensorValues extends AsyncTask<String, Void, String> {

        private int day;

        public GetSensorValues(int day){
            this.day = day;
        }

        public void setDay(int day){
            this.day = day;
        }

        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(SENSOR_AVG).newBuilder();
            urlBuilder.addQueryParameter("sensorName", sensorName);
            urlBuilder.addQueryParameter("day", Integer.toString(day));
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
            //Toast.makeText(SensorStats.this, result, Toast.LENGTH_LONG).show();
        }
    }

}



