package com.example.a2.activity;

import androidx.appcompat.app.AppCompatActivity;
import com.example.a2.R;
import com.example.a2.controller.FirebaseHelper;
import com.example.a2.model.Site;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    private final static String TAG = "DetailsActivity";
    private Site currentSite;
    private Button backBtn, listBtn, editBtn;

    private TextView siteTitle, ownerSite,
            siteLatitude, siteLongitude, numberPeopleSite;

    private EditText siteDescription, numberPeopleTested;
    private FirebaseHelper firebaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Init the component
        extractSite();
        attachComponents();
        setTextToComponent();

        // button
        setButtonClicker();

    }



    private void setButtonClicker() {
        // backBtn
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, MapsActivity.class);
                ArrayList<Site> sites = new ArrayList<>();
                sites.add(new Site(1));
                sites.add(new Site(2));


                intent.putExtra("siteList", sites );
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // edit btn
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSite.setNumberPeopleTested(Integer.parseInt(numberPeopleTested.getText().toString()));
                currentSite.setDescription(siteDescription.getText().toString());
                firebaseHelper.addSite(currentSite);
                Log.d(TAG, "Edit button is clicked");
            }
        });

        // list btn
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click list volunteer");
            }
        });
    }

    // set text
    public void setTextToComponent(){

        siteTitle.setText(currentSite.getTitle());
        siteLatitude.setText(currentSite.getLatitude()+ "");
        siteLongitude.setText(currentSite.getLongitude()+ "");
        siteDescription.setText(currentSite.getDescription());
        ownerSite.setText(currentSite.getUsername());

        numberPeopleSite.setText(currentSite.getUserList().size() + " ");
        numberPeopleTested.setText(currentSite.getNumberPeopleTested() + "");

    }

    // attach components
    public void attachComponents(){
        firebaseHelper = new FirebaseHelper(DetailsActivity.this);
        backBtn = findViewById(R.id.backToMapsBtn);
        listBtn = findViewById(R.id.showListBtn);
        editBtn = findViewById(R.id.editSiteBtn);

        siteTitle = findViewById(R.id.siteTitle);
        ownerSite = findViewById(R.id.ownerSite);
        siteLatitude = findViewById(R.id.siteLatitude);
        siteLongitude = findViewById(R.id.siteLongitude);
        numberPeopleSite = findViewById(R.id.numberPeopleSiteTxt);
        numberPeopleTested = findViewById(R.id.numberPeopleTested);
        siteDescription = findViewById(R.id.siteDescription);
    }

    // Extract the intent;
    public void extractSite(){
        Intent intent  = getIntent();
        currentSite = (Site) intent.getExtras().getParcelable("site");
//        ArrayList<String> userList = intent.getStringArrayListExtra("userList");
//        System.out.println(currentSite.getUserList().size() + " size in currentsite");

//        currentSite.setUserList(userList);
//        System.out.println(currentSite.getUserList().get(0) + " size of currentsite");
//        System.out.println(currentSite.getNumberPeopleTested() + " number ppl test of currentsite");

    }
}