package com.example.a2.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.a2.R;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdaptor implements GoogleMap.InfoWindowAdapter {


    private final View mWindow;

    private Context mContext;

    public CustomInfoWindowAdaptor(Context mContext) {
        this.mContext = mContext;
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


        //TODO: Action of button
        Button registerBtn = view.findViewById(R.id.registerBtn);

        registerBtn.setActivated(false);
        Button seeListVolunteer = view.findViewById(R.id.seeListBtn);

    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}
