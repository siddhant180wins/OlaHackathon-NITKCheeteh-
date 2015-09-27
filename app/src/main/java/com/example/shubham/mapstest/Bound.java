package com.example.shubham.mapstest;

/**
 * Created by shubham on 27-09-2015.
 */
import com.google.android.gms.maps.model.LatLng;

public class Bound {
    private LatLng northEast;
    private LatLng southWest;
    public LatLng getNorthEast() {
        return northEast;
    }
    public void setNorthEast(LatLng northEast) {
        this.northEast = northEast;
    }
    public LatLng getSouthWest() {
        return southWest;
    }
    public void setSouthWest(LatLng southWest) {
        this.southWest = southWest;
    }
}
