package com.example.a2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

public class Site implements ClusterItem, Parcelable {

    public final static String NAME = "name";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static String DESCRIPTION = "description";
    public final static String USERNAME = "username";
    public final static String USERLIST = "userList";

    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private String username;
    private List<String> userList;



    public Site(){};

    public Site( String username, String name, double latitude, double longitude, List<String> userList, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.username = username;
        this.userList = userList;
        this.description = description;
    }



    protected Site(Parcel in) {
//        id = in.readInt();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        username = in.readString();
        userList = in.createStringArrayList();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

//        dest.writeInt(id);
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(username);
        dest.writeStringList(userList);
        dest.writeString(description);
    }

    public static final Creator<Site> CREATOR = new Creator<Site>() {
        @Override
        public Site createFromParcel(Parcel in) {
            return new Site(in);
        }

        @Override
        public Site[] newArray(int size) {
            return new Site[size];
        }
    };


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Nullable
    @Override
    public String getTitle() {
        return name;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return description;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }



}
