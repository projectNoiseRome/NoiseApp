package com.pervasive.noiseapp;


import java.io.File;
import java.io.IOException;

import android.*;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;


public class CalculateNoise extends AppCompatActivity {
    static final int MY_PERMISSIONS_REQUEST_ACCESS_RECORD_AUDIO = 1;
    public MediaRecorder mrec = null;
    private Button startRecording = null;
    private Button stopRecording = null;
    AudioRecord recorder = null;
    private static final String TAG = "SoundRecordingDemo";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calculate_noise);


        //check permission
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_ACCESS_RECORD_AUDIO);

        }

        //mrec = new MediaRecorder();

        startRecording = (Button)findViewById(R.id.startrecording);
        stopRecording = (Button)findViewById(R.id.stoprecording);


        startRecording.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                try
                {
                    startRecording.setEnabled(false);
                    stopRecording.setEnabled(true);
                    stopRecording.requestFocus();
                    getNoiseLevel();
                    //startRecording();
                }catch (Exception ee)
                {
                    Log.e(TAG,"Caught io exception " + ee.getMessage());
                }

            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startRecording.setEnabled(true);
                stopRecording.setEnabled(false);
                startRecording.requestFocus();
                stopRecording();
            }

        });

        stopRecording.setEnabled(false);
        startRecording.setEnabled(true);

    }


    public void startRecording() throws IOException{
        if (mrec == null) {
            mrec = new MediaRecorder();
            mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
            mrec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mrec.setOutputFile("/dev/null");
            mrec.prepare();
            mrec.start();
        }
    }

    protected void stopRecording() {
        if (mrec != null) {
            Toast.makeText(getApplicationContext(), "db = " + getNoiseLevel(), Toast.LENGTH_SHORT).show();
            mrec.stop();
            //Toast.makeText(getApplicationContext(), "db = " + getAmplitude(), Toast.LENGTH_SHORT).show();
            mrec.release();
            //Toast.makeText(getApplicationContext(), "db = " + getAmplitude(), Toast.LENGTH_SHORT).show();
            mrec = null;
        }
    }

    /*public double getAmplitude() {
        if (mrec != null)
            return  mrec.getMaxAmplitude();
        else
            return 0;

    }*/

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
        Toast.makeText(getApplicationContext(), "db = " + db, Toast.LENGTH_SHORT).show();
        return db;
    }
}
