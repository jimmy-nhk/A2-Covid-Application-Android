package com.example.a2.helper;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.a2.R;

import com.example.a2.activity.MapsActivity;
import com.example.a2.model.Site;
import com.example.a2.model.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.util.List;

public class CustomInfoWindowAdaptor implements GoogleMap.InfoWindowAdapter {


    private final View mWindow;

    private Context mContext;
    private boolean isCurrentUserAOwner;
    private User currentUser;
    private List<Site> siteList;
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public CustomInfoWindowAdaptor(Context mContext, boolean isCurrentUserAOwner , User currentUser , List<Site> siteList) {
        this.mContext = mContext;
        this.isCurrentUserAOwner = isCurrentUserAOwner;
        this.currentUser = currentUser;
        this.siteList = siteList;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null);
    }

    // render the window
    private void renderWindowText(Marker marker, View view){

        String title = marker.getTitle();
        TextView titleText = view.findViewById(R.id.titleText);
        titleText.setText(title);

        TextView registerMessage =  view.findViewById(R.id.registerMessage);
        TextView description = view.findViewById(R.id.description);

        if (title.equals("My Location")){
            description.setText("My Location");
            titleText.setText("");
            registerMessage.setText("");
            description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            return;
        }

        description.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        registerMessage.setText("Click here to register");

        description.setText(marker.getSnippet());



        view.setBackground(mContext.getResources().getDrawable(R.drawable.bg_window));
    }



    public View getmWindow() {
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        renderWindowText(marker, mWindow);
        this.marker = marker;
        mWindow.setBackground(mContext.getResources().getDrawable(R.drawable.bg_window));
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderWindowText(marker, mWindow);
        this.marker = marker;
        mWindow.setBackground(mContext.getResources().getDrawable(R.drawable.bg_window));
        return mWindow;
    }
}
