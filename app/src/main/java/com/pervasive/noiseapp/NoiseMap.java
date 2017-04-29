package com.pervasive.noiseapp;

import android.*;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NoiseMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private JSONObject sensorList = new JSONObject();
    private final String SENSOR_LIST = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorList";
    private final String  SENSOR_VALUES = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorValues";
    private final String  SENSOR_STATS = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorStats";
    private final String AZURE = "http://noiseapp.azurewebsites.net/service/sound/getSensorValues";
    private HttpCall call = new HttpCall();
    private ProgressDialog mProgressDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_noise_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
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
        //Decimal Degrees = degrees + (minutes/60) + (seconds/3600)
        LatLng Boa = new LatLng(41.89186583, 12.5436269);
        LatLng roma = new LatLng(41.90278349999999, 12.496365500000024);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Boa , 15));

        int locationPermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);

        //check permission
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(getActivity() ,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION );

        } else {
            mMap.setMyLocationEnabled(true);
            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //this is a little problem but, actually i don't need it

            if( location!=null ){
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                LatLng myPos = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15));
            }
        }

        try {
            JSONArray list = sensorList.getJSONArray("sensors");
            for(int i = 0; i < list.length(); i++){
                JSONObject marker = list.getJSONObject(i);
                LatLng pos = new LatLng(Double.parseDouble(marker.getString("latitude")), Double.parseDouble(marker.getString("longitude")));
                double noiseLevel = Double.parseDouble(marker.getString("noiseLevel"));
                if(noiseLevel < 40){
                    MarkerOptions m = new MarkerOptions().position(pos)
                            .title(marker.getString("sensorName"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mMap.addMarker(m);

                }
                else if(noiseLevel >= 40 && noiseLevel < 60){
                    MarkerOptions m = new MarkerOptions().position(pos)
                            .title(marker.getString("sensorName"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    mMap.addMarker(m);

                }
                else{
                    MarkerOptions m = new MarkerOptions().position(pos)
                            .title(marker.getString("sensorName"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    mMap.addMarker(m);

                }
            }
        } catch (Exception e) {
            Toast.makeText(this.getContext(), e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("OKHTTP3", "Here");
        call.setContext(this.getContext());
        call.setQuery(SENSOR_STATS);
        LatLng pos = marker.getPosition();
        String sensor = "";
        try {
            JSONArray list = sensorList.getJSONArray("sensors");
            for (int i = 0; i < list.length(); i++) {
                JSONObject mark = list.getJSONObject(i);
                LatLng posMarker = new LatLng(Double.parseDouble(mark.getString("latitude")), Double.parseDouble(mark.getString("longitude")));
                if(pos.equals(posMarker)){
                    sensor = mark.getString("sensorName");
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        call.setSensorName(sensor);
        call.makeCall();
        return false;
    }

    //Generic HTTPCall
    private void makeHttpCall(String query, String sensorName){
        call.setContext(this.getContext());
        call.setQuery(query);
        call.setSensorName(sensorName);
        call.makeCall();
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
            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this.getContext());
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

