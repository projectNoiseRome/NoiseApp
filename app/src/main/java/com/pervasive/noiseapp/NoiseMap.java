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
import java.util.concurrent.ExecutionException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NoiseMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private JSONObject sensorList = new JSONObject();
    private JSONObject userList = new JSONObject();
    private final String SENSOR_LIST = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorList";
    private final String USER_LIST = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getUserDataList";
    //private final String  SENSOR_VALUES = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorValues";
    private final String  SENSOR_STATS = "http://10.0.2.2:8080/NoiseAppServer/service/sound/getSensorStats";
    //private final String AZURE = "http://noiseapp.azurewebsites.net/service/sound/getSensorValues";
    private final String TRAFFIC = "traffic";
    private final String CROWD = "crowd";
    private final String ENTERTEINMENT = "enterteinment";
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
        getSensorList taskSensor = new getSensorList(SENSOR_LIST);
        getSensorList taskUser = new getSensorList(USER_LIST);
        try {
            showProgressDialog();
            String result = taskSensor.execute("").get();
            String user = taskUser.execute("").get();
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
            //FOR THE STATIC SENSOR
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
            //FOR THE USER RILEVATION
            JSONArray userlist = userList.getJSONArray("userData");
            for(int i = 0; i < list.length(); i++){
                JSONObject marker = userlist.getJSONObject(i);
                LatLng pos = new LatLng(Double.parseDouble(marker.getString("latitude")), Double.parseDouble(marker.getString("longitude")));
                double noiseLevel = Double.parseDouble(marker.getString("noiseLevel"));
                String noiseType = marker.getString("noiseType");
                if(noiseType.equals(TRAFFIC)){
                    MarkerOptions m = new MarkerOptions().position(pos)
                            .title(marker.getString("userName"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mMap.addMarker(m);

                }
                else if(noiseType.equals(CROWD)){
                    MarkerOptions m = new MarkerOptions().position(pos)
                            .title(marker.getString("userName"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    mMap.addMarker(m);

                }
                else{
                    MarkerOptions m = new MarkerOptions().position(pos)
                            .title(marker.getString("userName"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
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
                    break;
                }
            }

            if(!sensor.equals("")) {
                call.setSensorName(sensor);
                call.makeCall();
            }
            else {
                JSONArray listuser = userList.getJSONArray("userData");
                JSONObject obj = new JSONObject();
                for (int i = 0; i < list.length(); i++) {
                    JSONObject mark = listuser.getJSONObject(i);
                    LatLng posMarker = new LatLng(Double.parseDouble(mark.getString("latitude")), Double.parseDouble(mark.getString("longitude")));
                    if (pos.equals(posMarker)) {
                        obj = mark;
                        break;
                    }
                }
                Toast.makeText(this.getContext(), obj.getString("userName")+": \n Noise Value : " + obj.getString("noiseLevel"), Toast.LENGTH_LONG).show();
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return false;
    }

    //Get the sensors
    private class getSensorList extends AsyncTask<String, Void, String> {

        private String httpString = "";

        public getSensorList(String httpString){
            this.httpString = httpString;
        }

        @Override
        protected String doInBackground(String... urls) {
            // we use the OkHttp library from https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            JSONArray list = new JSONArray();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(httpString).newBuilder();
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        json = new JSONObject(result);
                        if(httpString.equals(SENSOR_LIST)){
                            sensorList = json;
                        }
                        else {
                            userList = json;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(httpString.equals(SENSOR_LIST)) {
                        return "Loaded all the sensors on the map";
                    }
                    else {
                        return "Loaded all the user data";
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(httpString.equals(SENSOR_LIST)) {
                return "Failed to load the sensors on the map";
            }
            else {
                return "Failed to load the user data";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
        }
    }

    //Loading widget
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this.getContext());
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

