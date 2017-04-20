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

    private TextView textView;
    private String result = "";
    private Context context;


    public HttpCall(Context context){
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_read_webpage_async_task);
        //textView = (TextView) findViewById(R.id.TextView01);
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            JSONArray list = new JSONArray();
            //http://noiseapp.azurewebsites.net/service/sound/getSensorList
            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://noiseapp.azurewebsites.net/service/sound/getSensorValues").newBuilder();
            urlBuilder.addQueryParameter("sensorName", "ArduinoUno");
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();
            //.url("http://noiseapp.azurewebsites.net/service/sound/getSensorList")
            //post()
            try {
                Response response = client.newCall(request).execute();
                String noiseLevel = "";
                String date = "";

                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        json = new JSONObject(result);
                        list = json.getJSONArray("ArduinoUno");
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject explrObject = list.getJSONObject(i);
                            noiseLevel = explrObject.getString("noiseLevel");
                            date = explrObject.getString("date");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return noiseLevel+"dB, "+date;
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
        task.execute(new String[] { "http://10.0.2.2:8080/TestIOT/service/operation/sendTemperature" });

    }



}
