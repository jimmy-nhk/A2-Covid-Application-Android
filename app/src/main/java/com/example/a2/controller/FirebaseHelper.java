package com.example.a2.controller;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.a2.activity.MapsActivity;
import com.example.a2.activity.RegisterActivity;
import com.example.a2.model.Site;
import com.example.a2.model.User;
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
    private CollectionReference siteCoordinatesCollection;
    private CollectionReference userCollection;
    public static final String COLLECTION_PATH= "siteCoordinates";
    public static final String USER_COLLECTION = "users";

    private Activity activity;
    private List<Site> siteList;

    public FirebaseHelper(Activity activity) {

        this.activity = activity;
        this.db = FirebaseFirestore.getInstance();
        siteCoordinatesCollection = db.collection(COLLECTION_PATH);
        userCollection = db.collection(USER_COLLECTION);
        siteList = new ArrayList<>();
    }

    public void getAllUsers(RegisterActivity.FirebaseHelperCallback callback){

        ArrayList<User> userArrayList = new ArrayList<>();
        userCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            // create new site
                            User user = new User(documentSnapshot.getString(User.USER_NAME), documentSnapshot.getString(User.USER_EMAIL));


                            // add site to list
                            userArrayList.add(user);
                        }

                        // callBack on changed
                        callback.onDataChanged(userArrayList);
                    }
                });


    }

    public void addUser(User user){

        Log.d("FirebaseHelper" , "Here");
        Map<String, Object> userObject = new HashMap<>();

        userObject.put(User.USER_NAME , user.getName());
        userObject.put(User.USER_EMAIL, user.getEmail());

        userCollection
                .document(user.getName())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("FirebaseHelper", "Success added");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String error = "Error adding document" + e.toString();
                        System.out.println(error);
                    }
                });

    }



    public boolean addSite(Site site) {

        // Initialize the siteObject to add
        Map<String, Object> siteObject = new HashMap<>();

        siteObject.put(Site.NAME,  site.getName());
        siteObject.put(Site.LATITUDE, site.getLatitude());
        siteObject.put(Site.LONGITUDE, site.getLongitude());


        siteCoordinatesCollection
                .document(site.getName())
                .set(site)
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
        siteCoordinatesCollection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete (@NonNull Task<QuerySnapshot> task) {

                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){

                            // create new site
                            Site site = new Site();

                            site.setName(documentSnapshot.getString(Site.NAME));


                            // create new latlng
                            site.setLatitude(documentSnapshot.getDouble(Site.LATITUDE));
                            site.setLongitude(documentSnapshot.getDouble(Site.LONGITUDE));



                            // add site to list
                            siteList.add(site);
                        }

                        // callBack on changed
                        callback.onDataChanged(siteList);
                    }
                });
    }



}