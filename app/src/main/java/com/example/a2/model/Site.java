package com.example.a2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Site implements ClusterItem, Parcelable, Comparable<Site> {

    public final static String NAME = "name";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static String DESCRIPTION = "description";
    public final static String USERNAME = "username";
    public final static String USERLIST = "userList";
    public final static String PEOPLETESTED = "numberPeopleTested";

    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private String username;
    private ArrayList<String> userList;
    private int numberPeopleTested;



    public Site(){
        userList = new ArrayList<>();
    };

    public Site(int numberPeopleTested){
        this.numberPeopleTested = numberPeopleTested;
    }

    public Site(String username, String name, double latitude, double longitude, ArrayList<String> userList, String description, int numberPeopleTested) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.username = username;
        this.userList = userList;
        this.numberPeopleTested = numberPeopleTested;
    }



    protected Site(Parcel in) {
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        description = in.readString();
        username = in.readString();
        userList = in.createStringArrayList();

        try {
            System.out.println(userList.size() + "  userlist size in create to parcel");

        } catch (Exception e){

        }

        // error occurs here ???
//        numberPeopleTested = in.readInt() == 0 ? 0: in.readInt();
        numberPeopleTested = in.readInt();
        System.out.println(numberPeopleTested + " in create to parcel");

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

//        dest.writeInt(id);
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(description);

        dest.writeString(username);
        dest.writeStringList(userList);


        try {
            System.out.println(userList.size() + "  userlist size in write to parcel");

        } catch (Exception e){

        }
        dest.writeInt(numberPeopleTested);
        System.out.println(numberPeopleTested + " in write to parcel");


        System.out.println("write site");

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

    public int getNumberPeopleTested() {
        return numberPeopleTested;
    }

    public void setNumberPeopleTested(int numberPeopleTested) {
        this.numberPeopleTested = numberPeopleTested;
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

    public ArrayList<String> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(NAME, name);
        result.put(LATITUDE, latitude);
        result.put(LONGITUDE, longitude);
        result.put(DESCRIPTION, description);
        result.put(USERNAME, username);
        result.put(USERLIST, userList);
        result.put(PEOPLETESTED, numberPeopleTested);

        return result;
    }


    @Override
    public int compareTo(Site o) {

        if (this.name.equals(o.name)
            && this.description.equals(o.description)
            && this.latitude == o.latitude
            && this.longitude == o.longitude
            && this.userList.equals(o.userList)
            && this.numberPeopleTested == o.numberPeopleTested
            && this.username.equals(o.username)) {
            return 0;
        }

        return 1;
    }
}
