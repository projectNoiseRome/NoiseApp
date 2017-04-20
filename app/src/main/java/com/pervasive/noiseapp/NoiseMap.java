package com.pervasive.noiseapp;

import android.*;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.lang.Thread.sleep;

public class NoiseMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public String ris;
    public ArrayList<MarkerOptions> markersOptList;
    public String db;

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
        //Decimal Degrees = degrees + (minutes/60) + (seconds/3600)
        LatLng Mni = new LatLng(41.9144113, 12.548262000000022);
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

        /*Log.d("OKHTTP3", "Here");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //String db;
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://noiseapp.azurewebsites.net/service/sound/getSensorList")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    Log.d("OKHTTP3", "Got request");
                    ris = response.body().string();
                    Log.d("OKHTTP3", ris);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        try {
            JSONArray jsonarray = new JSONArray(ris);
            markersOptList = new ArrayList<>();
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);

                double latitude = Double.parseDouble( jsonobject.getString("latitude") );
                double longitude = Double.parseDouble( jsonobject.getString("longitude") );
                String sensorName = (jsonobject.getString("sensorName")).toString();
                MarkerOptions myMarker = new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(sensorName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                markersOptList.add(myMarker);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Iterator x = markersOptList.iterator();
        while (x.hasNext()){
            MarkerOptions aux = (MarkerOptions) x.next();
            googleMap.addMarker(aux);
        }*/

        Marker myMarker = googleMap.addMarker(new MarkerOptions()
                .position(Boa)
                .title("Casa del Boa Arduino")
                //.snippet("Casa")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("OKHTTP3", "Here");
        HttpCall call = new HttpCall(this.getContext());
        call.makeCall();
        return false;
    }

}

