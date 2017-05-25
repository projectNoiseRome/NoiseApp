package com.pervasive.noiseapp;


import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;


public class CalculateNoise extends AppCompatActivity {
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_RECORD_AUDIO = 1;
    public MediaRecorder mrec = null;
    private Button startRecording = null;
    private Button stopRecording = null;
    private AudioRecord recorder = null;
    private String email = "";
    private String decibels;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final String TAG = "SoundRecordingDemo";
    private ProgressDialog mProgressDialog;
    //private static final String POST_NOISE = "http://10.0.2.2:8080/service/sound/userNoiseLevel";
    //private static final String POST_NOISE = "http://192.168.1.180:8080/NoiseAppServer/service/sound/userNoiseLevel";
    private static final String POST_NOISE = "http://10.0.2.2:8080/NoiseAppServer/service/sound/userNoiseLevel";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calculate_noise);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        email = getIntent().getExtras().getString("email");
        //check permission
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_ACCESS_RECORD_AUDIO);
        }


        //mrec = new MediaRecorder();

        startRecording = (Button) findViewById(R.id.startrecording);
        stopRecording = (Button) findViewById(R.id.stoprecording);

        startRecording.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    //startRecording.setEnabled(false);
                    stopRecording.setEnabled(true);
                    stopRecording.requestFocus();
                    decibels = "" + getNoiseLevel();
                    TextView tv = (TextView) findViewById(R.id.decibelView);
                    tv.setText(decibels.substring(0, 5));
                    //startRecording();
                } catch (Exception ee) {
                    Log.e(TAG, "Caught io exception " + ee.getMessage());
                }

            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startRecording.setEnabled(true);
                stopRecording.setEnabled(false);
                startRecording.requestFocus();
                postNoise();
            }

        });

        stopRecording.setEnabled(false);
        startRecording.setEnabled(true);

    }



    /*public void startRecording() throws IOException{
        if (mrec == null) {
            mrec = new MediaRecorder();
            mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
            mrec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mrec.setOutputFile("/dev/null");
            mrec.prepare();
            mrec.start();
        }
    }*/

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

    protected void postNoise() {
        int locationPermission = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        //check permission
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        } else {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                //this is a little problem but, actually i don't need it

                if (location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    Log.d("long", "" + longitude);
                    Log.d("lat", "" + latitude);
                }

            }
        }



        final JSONObject actualData = new JSONObject();
        Spinner sp = (Spinner) findViewById(R.id.spinner);
        try {
            actualData.put("userName", email);
            actualData.put("latitude", Double.toString(latitude));
            actualData.put("longitude", Double.toString(longitude));
            Object nt = sp.getSelectedItem();
            actualData.put("noiseType", nt.toString().toLowerCase());
            actualData.put("noiseValue", decibels);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SendNoiseLevel snl = new SendNoiseLevel(actualData);
        snl.execute("");

    }


    //HTTP CALL
    //Get the sensor's values
    private class SendNoiseLevel extends AsyncTask<String, Void, String> {

        private JSONObject body;

        public SendNoiseLevel(JSONObject body){
            this.body = body;
        }

        @Override
        protected void onPreExecute(){
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody formBody = RequestBody.create(JSON, body.toString());
            HttpUrl.Builder urlBuilder = HttpUrl.parse(POST_NOISE).newBuilder();
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).post(formBody).build();
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    return "Success";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Upload failed";
        }

        @Override
        protected void onPostExecute(String result) {
            hideProgressDialog();
            Toast.makeText(CalculateNoise.this, result, Toast.LENGTH_LONG).show();
        }
    }



    public static double REFERENCE = 0.00002;

    public double getNoiseLevel()
    {
        Log.e(TAG, "start new recording process");
        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
        //making the buffer bigger....
        //bufferSize=bufferSize*4;
        bufferSize=bufferSize*40;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        short data [] = new short[bufferSize];
        double average = 0.0;
        recorder.startRecording();
        //recording data;
        recorder.read(data, 0, bufferSize);

        recorder.stop();
        Log.e(TAG, "stop");
        for (short s : data)
        {
            if(s>0)
            {
                average += Math.abs(s);
            }
            else
            {
                bufferSize--;
            }
        }
        //x=max;
        double x = average/bufferSize;
        Log.e(TAG, ""+x);
        recorder.release();
        Log.d(TAG, "getNoiseLevel() ");
        double db=0;

        // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
        // relative to the pressure
        double pressure = x/51805.5336; //the value 51805.5336 can be derived from asuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)
        Log.d(TAG, "x="+pressure +" Pa");
        db = (20 * Math.log10(pressure/REFERENCE));
        Log.d(TAG, "db="+db);
        //Toast.makeText(getApplicationContext(), "db = " + db, Toast.LENGTH_SHORT).show();
        if (mrec != null) {
            //Toast.makeText(getApplicationContext(), "db = " + getNoiseLevel(), Toast.LENGTH_SHORT).show();
            mrec.stop();
            //Toast.makeText(getApplicationContext(), "db = " + getAmplitude(), Toast.LENGTH_SHORT).show();
            mrec.release();
            //Toast.makeText(getApplicationContext(), "db = " + getAmplitude(), Toast.LENGTH_SHORT).show();
            mrec = null;
        }
        return db;

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
}
