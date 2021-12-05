package com.example.a2.controller;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.a2.activity.LogInActivity;
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
    private List<User> userList;

    public FirebaseHelper(Activity activity) {

        this.activity = activity;
        this.db = FirebaseFirestore.getInstance();
        siteCoordinatesCollection = db.collection(COLLECTION_PATH);
        userCollection = db.collection(USER_COLLECTION);
        siteList = new ArrayList<>();
        userList = new ArrayList<>();

    }


    public void getAllUsersMapsActivity(MapsActivity.FirebaseCallback firebaseCallback){

        ArrayList<User> userArrayList = new ArrayList<>();
        userCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            System.out.println(User.USER_SUPERUSER + " superUser");
                            // create new site
                            User user = new User(documentSnapshot.getString(User.USER_NAME),
                                    documentSnapshot.getString(User.USER_EMAIL)
                                    ,documentSnapshot.getBoolean(User.USER_SUPERUSER));


                            // add site to list
                            userArrayList.add(user);
                        }

                        userList = userArrayList;

                        firebaseCallback.onDataChanged(userArrayList);
                    }
                });


    }


    public void getAllUsersForLogin(LogInActivity.FirebaseHelperCallback callback){

        ArrayList<User> userArrayList = new ArrayList<>();
        userCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            System.out.println(User.USER_SUPERUSER + " superUser");
                            // create new site
                            User user = new User(documentSnapshot.getString(User.USER_NAME),
                                    documentSnapshot.getString(User.USER_EMAIL)
                                    ,documentSnapshot.getBoolean(User.USER_SUPERUSER));


                            // add site to list
                            userArrayList.add(user);
                        }

                        callback.onDataChanged(userArrayList);
                    }
                });

    }


    public void getAllUsersForRegister(RegisterActivity.FirebaseHelperCallback callback){

        ArrayList<User> userArrayList = new ArrayList<>();
        userCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            System.out.println(User.USER_SUPERUSER + " superUser");
                            // create new site
                            User user = new User(documentSnapshot.getString(User.USER_NAME),
                                    documentSnapshot.getString(User.USER_EMAIL)
                                    ,documentSnapshot.getBoolean(User.USER_SUPERUSER));


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
        userObject.put(User.USER_SUPERUSER, user.getIsSuperUser());

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
                            site.setDescription(documentSnapshot.getString(Site.DESCRIPTION));
                            site.setUsername(documentSnapshot.getString(Site.USERNAME));


                            try {
                                site.setUserList((ArrayList<String>) documentSnapshot.get(Site.USERLIST));
                            }catch (Exception e){

                            }


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