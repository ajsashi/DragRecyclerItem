package com.ait.dragrecycleritem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback, MapListener {
    Utils utils;
    private GoogleMap mMap;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    TextView currentLocation;
    ImageView addIcon;
    private LatLng latLng;
    private String locality;
    private String country;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
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
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        currentLocation=findViewById(R.id.currentLocation);
        addIcon=findViewById(R.id.addIcon);
        configureCameraIdle();

        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLocation();
            }
        });
    }

    private void configureCameraIdle() {
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                latLng = mMap.getCameraPosition().target;
                Geocoder geocoder = new Geocoder(AddLocationActivity.this);

                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        locality = addressList.get(0).getAddressLine(0);
                        country = addressList.get(0).getCountryName();
                        if (!locality.isEmpty() && !country.isEmpty())
                           Log.d("Locality",locality + "  " + country);
                        currentLocation.setText(locality+" "+country);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
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

    public void addLocation(){
        Constants.datalist.add(new Model(locality+" "+country,latLng));
        finish();
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,16));
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRouteObtained(PolylineOptions routes) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(onCameraIdleListener);
    }


}
