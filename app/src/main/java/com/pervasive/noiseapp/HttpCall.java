package com.pervasive.noiseapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Federico Boarelli on 20/04/2017.
 */

public class HttpCall extends Activity{



    private Context context;
    private String query;
    private String sensorName;
    private JSONObject sensorList;

    //ALL HTTP CALL POSSIBLE
    private final String  SENSOR_VALUES = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorValues";
    private final String AZURE = "http://noiseapp.azurewebsites.net/service/sound/getSensorValues";



    public void setSensorList(JSONObject sensorList){
        this.sensorList = sensorList;
    }

    public void setQuery(String query){
        this.query = query;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setSensorName(String sensorName){
        this.sensorName = sensorName;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            JSONArray list = new JSONArray();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(query).newBuilder();
            //WE PASS THE SENSOR NAME AS QUERY PARAM
            if(query.equals(SENSOR_VALUES)){
                urlBuilder.addQueryParameter("sensorName", sensorName);
            }
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                String sensor = "";
                String latitude = "";
                String longitude = "";
                String noiseLevel = "";
                String date = "";

                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        json = new JSONObject(result);
                        list = json.getJSONArray(sensorName);
                        //Cycle for exploring the array
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject explrObject = list.getJSONObject(i);
                            noiseLevel = explrObject.getString("noiseLevel");
                            date = explrObject.getString("date");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return "Noise level : "+noiseLevel+"dB, "+date;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Download failed";
        }

        @Override
        protected void onPostExecute(String result) {

            Toast.makeText(context , result, Toast.LENGTH_LONG).show();// display toast

        }
    }



    public void makeCall(){
        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute(new String[] { "This is not needed" });
    }



}
