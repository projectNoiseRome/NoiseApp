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
    private final String  SENSOR_VALUES = "http://192.168.1.180:8080/NoiseAppServer/service/sound/getSensorValues";
    private final String  SENSOR_STATS = "http://192.168.1.180:8080/NoiseAppServer/service/sound/getSensorStats";
    //private final String  SENSOR_VALUES = "http://10.0.2.2:8080/service/sound/getSensorValues";
    //private final String  SENSOR_STATS = "http://10.0.2.2:8080/service/sound/getSensorStats";
    //private final String  SENSOR_VALUES = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorValues";
    //private final String  SENSOR_STATS = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorStats";

    //private final String AZURE = "http://noiseapp.azurewebsites.net/service/sound/getSensorValues";



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
            if(query.equals(SENSOR_VALUES) || query.equals(SENSOR_STATS)){
                urlBuilder.addQueryParameter("sensorName", sensorName);
            }
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                String sensorName = "";
                String max = "";
                String min = "";
                String avg = "";
                String last = "";
                String noiseLevel = "";
                String date = "";
                if (response.isSuccessful() && query.equals(SENSOR_VALUES)) {
                    try {
                        String result = response.body().string();
                        json = new JSONObject(result);
                        list = json.getJSONArray(sensorName);
                        //Cycle for exploring the array
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject explrObject = list.getJSONObject(i);
                            noiseLevel = explrObject.getString("noiseLevel").substring(0, 5);
                            date = explrObject.getString("date");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return "Noise level : "+noiseLevel+"dB, "+date;

                }
                else if(response.isSuccessful() && query.equals(SENSOR_STATS)){

                    try {
                        String result = response.body().string();
                        json = new JSONObject(result);
                        avg = json.getString("noiseAverage");
                        max = json.getString("maxNoise");
                        min = json.getString("minNoise");
                        last = json.getString("lastNoise");
                        sensorName = json.getString("sensorName");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return sensorName + " data: \n Last rilevation : " + last + " \n Average : "+avg+ "\n Max : " + max + "\n Min : " + min;
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
