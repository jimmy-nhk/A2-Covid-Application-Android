package com.example.a2;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {


    private FirebaseFirestore db;
    private CollectionReference dbCollection;
    public static final String COLLECTION_PATH= "siteCoordinates";
    private Activity activity;
    private List<Site> siteList;

    public FirebaseHelper(Activity activity) {

        this.activity = activity;
        this.db = FirebaseFirestore.getInstance();
        dbCollection = db.collection(COLLECTION_PATH);
        siteList = new ArrayList<>();
    }



    public boolean addSite(Site site) {

        // Initialize the siteObject to add
        Map<String, Object> siteObject = new HashMap<>();

        siteObject.put(Site.NAME,  site.getName());
        siteObject.put(Site.LATITUDE, site.getLatLng().latitude);
        siteObject.put(Site.LONGITUDE, site.getLatLng().longitude);


        dbCollection
                .document(site.getName())
                .set(siteObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Successfully added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String error = "Error adding document" + e.toString();
                        System.out.println(error);
                    }
                });

        return true;
    }


    public void getAllSites(MapsActivity.FirebaseHelperCallback callback){

        // fetch the data
        dbCollection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete (@NonNull Task<QuerySnapshot> task) {

                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){

                            // create new site
                            Site site = new Site();

                            site.setName(documentSnapshot.getString(Site.NAME));
                            // create new latlng
                            LatLng latLng = new LatLng(documentSnapshot.getDouble(Site.LATITUDE),
                                    documentSnapshot.getDouble(Site.LONGITUDE));
                            site.setLatLng(latLng);


                            // add site to list
                            siteList.add(site);
                        }

                        // callBack on changed
                        callback.onDataChanged(siteList);
                    }
                });
    }



}