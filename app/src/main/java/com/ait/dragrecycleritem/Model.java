package com.ait.dragrecycleritem;

import com.google.android.gms.maps.model.LatLng;

public class Model {
    String address;
    LatLng latLng;
    float distance;

    public Model(String address, LatLng latLng) {
        this.address = address;
        this.latLng = latLng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
