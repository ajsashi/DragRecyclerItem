package com.ait.dragrecycleritem;

import android.location.Location;

import com.google.android.gms.maps.model.PolylineOptions;

interface MapListener {

    void getLocation(Location location);

    void onRouteObtained(PolylineOptions routes);
}
