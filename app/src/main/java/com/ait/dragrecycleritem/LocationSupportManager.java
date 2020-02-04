package com.ait.dragrecycleritem;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
/*Dependecies Required*//*
 *//*GoogleMaps*//*
    implementation 'com.google.android.gms:play-services-maps:17.0.0'
            implementation 'com.google.android.gms:play-services-location:17.0.0'*/

public class LocationSupportManager {

    Context mContext;
    MapListener listener;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    private Location mLastLocation;
    LocationCallback locationCallback;

    public LocationSupportManager(Context mContext, MapListener listener) {
        this.mContext = mContext;
        this.listener = listener;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(mContext));

    }


    public void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    public void buildLocationCallBacks() {
        Log.d("Location update", "buildLocationCallBacks called");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                    Log.d("Location update", String.valueOf(mLastLocation));
                    if (mLastLocation != null) {
                        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                            Toast.makeText(mContext, "callback" + addresses.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                displayLocation();
            }
        };
    }

    public void displayLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        mLastLocation = location;
                        if (mLastLocation != null) {
                            listener.getLocation(mLastLocation);
                        } else {
                            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
