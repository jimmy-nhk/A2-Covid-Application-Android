package com.example.a2;

import com.google.android.gms.maps.model.LatLng;

public class Site {

    public final static String NAME = "name";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";

    private int id;
    private String name;
    private LatLng latLng;

    public Site(){};

    public Site(int id, String name, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.latLng = latLng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
