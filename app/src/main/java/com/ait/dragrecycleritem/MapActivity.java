package com.ait.dragrecycleritem;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapListener {
    Utils utils;
    private GoogleMap mMap;
    private PolylineOptions polylineOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        utils = new Utils();
        if (utils.isLocationEnabled(this)) {
            if (ActivityCompat.checkSelfPermission(this,
                    ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                updateScreen();
            } else {
                askLocationPermission();
            }
        } else {
            utils.showlocationAlert(this);
        }
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_activity);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
        }
    }

    private void askLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this, ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions((Activity) this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 100);
            } else {
                /*if (!notFirst) {
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.getLocationRequestCode());


                    preferenceManager.storeBoolsData(Constants.getLocationPermission(), false);
                    notFirst = true;
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.enable_location), Toast.LENGTH_SHORT).show();

                }*/
            }
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateScreen() {
        boolean fineLocation = utils.hasPermission(this, ACCESS_FINE_LOCATION);
        boolean coarseLocation = utils.hasPermission(this, ACCESS_COARSE_LOCATION);
        if (fineLocation && coarseLocation) {

            LocationSupportManager manager = new LocationSupportManager(this, this);
            manager.buildLocationRequest();
            manager.buildLocationCallBacks();
            manager.displayLocation();
        }
    }

    @Override
    public void getLocation(Location location) {
        LatLng currentlocation = new LatLng(location.getLatitude(), location.getLongitude());
//        mMap.addMarker(new MarkerOptions().position(currentlocation).icon(BitmapDescriptorFactory.fromBitmap(utils.getBluePinWithNumber(this, -1))));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,16));
        mMap.setMyLocationEnabled(true);
        int i = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentlocation);
        for (Model model : Constants.datalist) {
            i++;
            LatLng latLng = model.getLatLng();
            mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(utils.getBluePinWithNumber(this, i))));
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        mMap.moveCamera(cu);
        mMap.animateCamera(cu);

        GetRoute route = new GetRoute();
        route.getDirection(this,this);
    }

    @Override
    public void onRouteObtained(PolylineOptions polylineOptions) {

        if (polylineOptions != null){
         /*   runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.addPolyline(polylineOptions);
                }
            });*/
            mMap.addPolyline(polylineOptions);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


}
