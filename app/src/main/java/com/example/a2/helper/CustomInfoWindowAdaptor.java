package com.example.a2.helper;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.a2.R;

import com.example.a2.activity.MapsActivity;
import com.example.a2.model.Site;
import com.example.a2.model.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class CustomInfoWindowAdaptor implements GoogleMap.InfoWindowAdapter {


    private final View mWindow;

    private Context mContext;
    private boolean isCurrentUserAOwner;
    private User currentUser;
    private List<Site> siteList;

    public CustomInfoWindowAdaptor(Context mContext, boolean isCurrentUserAOwner , User currentUser , List<Site> siteList) {
        this.mContext = mContext;
        this.isCurrentUserAOwner = isCurrentUserAOwner;
        this.currentUser = currentUser;
        this.siteList = siteList;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null);
    }

    private void renderWindowText(Marker marker, View view){

        String title = marker.getTitle();
        TextView titleText = view.findViewById(R.id.titleText);
        titleText.setText(title);

        TextView description = view.findViewById(R.id.description);
        description.setText(marker.getSnippet());

        TextView numberOfPeople = view.findViewById(R.id.numberPeopleText);
        numberOfPeople.setText("10");

        view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_window));


        //TODO: Action of button
//        Button registerBtn = view.findViewById(R.id.registerBtn);
//
//        registerBtn.setEnabled(!isCurrentUserAOwner);
//        registerBtn.setActivated(!isCurrentUserAOwner);
//        registerBtn.setTextColor(isCurrentUserAOwner ? Color.parseColor("#808080") : Color.parseColor("#FFFFFFFF"));
//
//
//        Button seeDetailsBtn = view.findViewById(R.id.seeDetailsBtn);
//
//        seeDetailsBtn.setEnabled(ifUserIsAbleToSeeDetails());
//        seeDetailsBtn.setActivated(ifUserIsAbleToSeeDetails());
//        seeDetailsBtn.setTextColor(!ifUserIsAbleToSeeDetails() ? Color.parseColor("#808080") : Color.parseColor("#FFFFFFFF"));


    }

    public boolean ifUserIsAbleToSeeDetails(){

        //
        if (currentUser.getIsSuperUser()){
            return true;
        }

        for (Site site: siteList
             ) {

            // check if the username is as same as owner's site
            if (currentUser.getName().equals(site.getUsername())){
                return true;
            }

        }

        return false;
    }

    public View getmWindow() {
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        renderWindowText(marker, mWindow);
        mWindow.setBackground(mContext.getResources().getDrawable(R.drawable.bg_window));
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderWindowText(marker, mWindow);
        mWindow.setBackground(mContext.getResources().getDrawable(R.drawable.bg_window));
        return mWindow;
    }
}
