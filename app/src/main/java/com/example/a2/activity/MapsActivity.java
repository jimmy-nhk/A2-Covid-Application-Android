
package com.example.a2.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2.R;
import com.example.a2.helper.CustomListAdapter;
import com.example.a2.helper.CustomInfoWindowAdaptor;
import com.example.a2.helper.SiteRenderer;
import com.example.a2.model.PolylineData;
import com.example.a2.model.Site;
import com.example.a2.model.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.a2.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowLongClickListener,
        GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnPolylineClickListener
        , ClusterManager.OnClusterClickListener, ClusterManager.OnClusterItemClickListener {

    private static final int LOGIN_CODE = 100;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    public static final String TAG = "MapsActivity";

    protected FusedLocationProviderClient mFusedLocationProviderClient;
    protected LocationRequest mLocationRequest;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private ArrayList<Site> siteList;
    private ArrayList<PolylineData> mPolylineData = new ArrayList<>();
    private List<User> userList;

    private boolean isLoggedIn;
    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();

    private ClusterManager<Site> clusterManager;
    private SiteRenderer siteRender;

    private User currentUser;
    private Site currentSite;

    // init current selected site
    private Site mSelectedSite = null;

    //    private Site oldSite;
    private boolean isLeader;
    private boolean isSuperUser;
    private boolean isZoomedIn;
    private boolean isPossibleToAdd;
    private Dialog createSiteDialog;
    private Dialog registerSiteDialog;


    private Drawable drawable;

    private CustomInfoWindowAdaptor customInfoWindowAdaptor;

    // Route API (direction
    private GeoApiContext mGeoApiContext = null;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private Location currentLocation;

    /**
     * Component for details dialog
     */
    private Button backBtn, listBtn, editBtn;

    private TextView siteTitle, ownerSite,
            siteLatitude, siteLongitude, numberPeopleSite;

    private EditText numberPeopleTested, siteDescription;

    private ImageButton signInOutBtn, currentPositionBtn, addSiteBtn;
//    private EditText mSearchText;


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initServices();

        //get location permission for the map
        getLocationPermission();

        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                0000);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // init direction
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();

        }

        // get intent from the notification
        try {
            Intent intent = getIntent();
            currentUser = intent.getParcelableExtra("user");
            Log.d(TAG, "onCreate: currentUser name: " + currentUser.getName());
            isLoggedIn = true;
            signInOutBtn.setImageResource(R.drawable.logout_image);

        } catch (Exception e) {
            currentUser = new User();
        }

    }

    // reset the map
    private void resetMap() {
        if (mMap != null) {
            mMap.clear();

            if (clusterManager != null) {
                clusterManager.clearItems();
            }

//            if (mClusterMarkers.size() > 0) {
//                mClusterMarkers.clear();
//                mClusterMarkers = new ArrayList<>();
//            }

            // remove polyline
            if (mPolylineData.size() > 0) {
                mPolylineData.clear();
                mPolylineData = new ArrayList<>();
            }

            // read data again
            readDataFromDb(false);
        }
    }

    // remove trip markers
    private void removeTripMarkers() {

        for (Marker marker : mTripMarkers) {
            marker.remove();
        }
    }

    // reset selected site
    private void resetSelectedMarker() {

        if (mSelectedSite != null) {

//            clusterManager.addItem(mSelectedSite);
            clusterManager.clearItems();
            clusterManager.addItems(siteList);
            mSelectedSite = null;
            clusterManager.cluster();

            removeTripMarkers();
        }

    }

    // add polylines
    private void addPolyLines(DirectionsResult result) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                // clear the polyline before adding in
                if (mPolylineData.size() > 0) {

                    for (PolylineData polylineData : mPolylineData) {
                        polylineData.getPolyline().remove();
                    }

                    mPolylineData.clear();
                    mPolylineData = new ArrayList<>();
                }

                double duration = 9999999;
                // finding the route
                for (DirectionsRoute route : result.routes) {

                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    for (com.google.maps.model.LatLng latLng : decodedPath) {

                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(R.color.colorCardDT);
                    polyline.setClickable(true);

                    // add to polyline list
                    mPolylineData.add(new PolylineData(polyline, route.legs[0]));


                    double tempDuration = route.legs[0].duration.inSeconds;

                    // find the closest route
                    if (tempDuration < duration) {
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }


//                    clusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<Site>(new GridBasedAlgorithm<Site>()));

                    clusterManager.removeItem(mSelectedSite);
//                    mSelectedMarker.setVisible(false);
                }
            }
        });
    }

    // calculate the directions
    private void calculateDirections(Marker marker) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        // get the destination
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
//                Log.d(TAG, "onResult: duration: " + result.routes[0].legs[0].duration);
//                Log.d(TAG, "onResult: distance: " + result.routes[0].legs[0].distance);
//                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolyLines(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: Fail to get the routes" + e.getMessage());

            }
        });
    }

    // init the service
    private void initServices() {

        // init realtime db
        firebaseDatabase = FirebaseDatabase.getInstance("https://a2-android-56cbb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();


        // init isLogin
        isLoggedIn = false;

        currentUser = new User();
        firebaseAuth = FirebaseAuth.getInstance();
        drawable = getResources().getDrawable(R.drawable.site_cluster_large);

        // Init the object
        siteList = new ArrayList<>();
        userList = new ArrayList<>();


        // read the db
        readDataFromDb(false);

        // reset button
        ImageButton imageButton = findViewById(R.id.refreshBtn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMap();
            }
        });

        // set sign in out btn
        signInOutBtn = findViewById(R.id.signInOutBtn);

        if (isLoggedIn) {
            signInOutBtn.setImageResource(R.drawable.logout_image);

        } else {
            signInOutBtn.setImageResource(R.drawable.login_image);

        }
        signInOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // validate login
                if (isLoggedIn) {

                    firebaseAuth.signOut();
                    isLoggedIn = false;
                    currentSite = null;
                    signInOutBtn.setImageResource(R.drawable.login_image);

                    // announce the log out message
                    final AlertDialog dialog1 = new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Announcement")
                            .setIcon(R.drawable.thumb_up)
                            .setMessage("Log out successfully")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    dialog1.show();
                    return;

                }

                // log in intent
                Intent intent = new Intent(MapsActivity.this, LogInActivity.class);
                startActivityForResult(intent, LOGIN_CODE);
            }
        });

        // current position btn
        currentPositionBtn = findViewById(R.id.currentPositionBtn);

        currentPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isZoomedIn = true;
                getDeviceLocation();
            }
        });

        // add site btn is on
        addSiteBtn = findViewById(R.id.addSiteBtn);

        addSiteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSuperUser) {
                    return;
                }

                if (!isLoggedIn) {
                    return;
                }

                isPossibleToAdd = !isPossibleToAdd;

                showPositiveDialog("Announce", isPossibleToAdd ? "You are allowed to create new" +
                        "site" : "You are not allowed to create new site", v);
            }
        });

    }


    // read data from db
    private void readDataFromDb(boolean isGetCurrentLocation) {


        // load users
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.


                GenericTypeIndicator<HashMap<String, User>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, User>>() {
                };

                HashMap<String, User> users = snapshot.getValue(genericTypeIndicator);


                try {
                    for (User u : users.values()) {
//                        Log.d(TAG, "Value is: " + u.getEmail());
                        userList.add(u);
                    }


                } catch (Exception e) {
                    Log.d(TAG, "Cannot load the users");
                }

                try {
                    isLeader = ifUserIsAbleToSeeDetails();
                } catch (Exception e) {
                    Log.d(TAG, "Not yet setup the isLeader ");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // load sites
        databaseReference.child("sites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                GenericTypeIndicator<HashMap<String, Site>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Site>>() {
                };

                HashMap<String, Site> sites = snapshot.getValue(genericTypeIndicator);


                try {
                    mMap.clear();

                    // get current location
                    if (isGetCurrentLocation) {
                        getDeviceLocation();
                    }

                    // init the siteList again
                    siteList = new ArrayList<>();
                    clusterManager.clearItems();

                    for (Site s : sites.values()) {

//                        mMap.addMarker(new MarkerOptions().icon(getMarkerIconFromDrawable(drawable)).snippet(s.getDescription()).title(s.getName()).position(new LatLng(s.getLatitude(), s.getLongitude())));
                        clusterManager.addItem(s);
                        siteList.add(s);

                    }
                    clusterManager.cluster();
                    searchFunction();

                } catch (Exception e) {
                    Log.d(TAG, "Cannot load the sites");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildChanged: " + snapshot.getKey());

                Site site = snapshot.getValue(Site.class);

                if (site.getUsername().equals(currentUser.getName())) {
                    createNotification(site.getTitle(), getApplicationContext());
                }


                Log.d(TAG, "onChildChanged: " + site.getTitle());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        databaseReference.child("sites").addChildEventListener(childEventListener);

    }

    private NotificationManager notifManager;

    // create notification
    public void createNotification(String aMessage, Context context) {
        final int NOTIFY_ID = 0; // ID of notification
        String id = context.getString(R.string.app_name); // default_channel_id
        String title = context.getString(R.string.app_name); // Default Channel
        Intent intent;
        PendingIntent pendingIntent;


        NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = notifManager.getNotificationChannel(id);
        if (mChannel == null) {
            mChannel = new NotificationChannel(id, title, importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notifManager.createNotificationChannel(mChannel);
        }
        builder = new NotificationCompat.Builder(context, id);
        intent = new Intent(context, MapsActivity.class);
        intent.putExtra("user", currentUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        stackBuilder.editIntentAt(0).putExtra("user", currentUser);

// Get the PendingIntent containing the entire back stack
        pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle(aMessage)                            // required
                .setSmallIcon(R.drawable.site_cluster_large)   // required
                .setContentText("Something has changed in your site") // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(aMessage)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // create notification
        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);


    }

    // search Function
    private void searchFunction() {

        List<String> siteString = new ArrayList<>();
        for (Site s : siteList) {
            siteString.add("Title: " + s.getTitle() + "\nLeader: " + s.getUsername() + "\nDescription: " + s.getDescription() + "\n");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, siteString);

        AutoCompleteTextView siteSearch = findViewById(R.id.input_search);
        siteSearch.setAdapter(adapter);
        siteSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String searchInput = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "searchInput: " + i + " th");
                LatLng location = findLatLng(searchInput, i);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
                siteSearch.setText("");
            }
        });
    }

    // find the latlng based on the given input
    private LatLng findLatLng(String searchInput, int iTh) {

        int i = 1;

        // loop through the list
        for (Site s : siteList) {

            if (searchInput.contains(s.getTitle()) && searchInput.contains(s.getUsername()) ) {

                // get the latLng
                Log.d(TAG, "Here" );
                LatLng latLng = s.getPosition();
                Log.d(TAG, "findLocation: " + s.toString());
//                Toast.makeText(MapsActivity.this, s.getTitle() + "", Toast.LENGTH_SHORT).show();
                return latLng;
            }
        }


        return null;
    }


    // get the current location
    public void getDeviceLocation() {
        Log.d(TAG, "GetDeviceLocation: getting devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: found location!");
                        currentLocation = (Location) task.getResult();


                        try {

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 17, "My Location", true);

                        } catch (Exception e) {
                            Log.d(TAG, "onComplete: cannot move the map");
                        }

                    } else {
                        Log.d(TAG, "onComplete: current location is null!");
//                        Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "getdevicelocation: Security: " + e.getMessage());
        }
    }

    // move the camera
    private void moveCamera(LatLng latLng, float zoom, String title, boolean isMyLocation) {


        Drawable drawable = getResources().getDrawable(isMyLocation ? R.drawable.my_location : R.drawable.ic_android);


        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .visible(true)
                .icon(getMarkerIconFromDrawable(drawable))
                .title(title));

        marker.showInfoWindow();


        // validate if zoom is needed
        if (isZoomedIn) {

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lgn: " +
                    latLng.longitude + " , " + marker.isVisible());
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // validate search places
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {


//                readDataFromDb(false);

                Place place = Autocomplete.getPlaceFromIntent(data);

                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng().latitude + ", "
                        + place.getLatLng().longitude);


//
//
//                isZoomedIn = true;
//
//                MarkerOptions options = new MarkerOptions()
//                        .position(place.getLatLng())
//                        .title(place.getName());
//
//
//
//
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17));
                Log.d(TAG, "moveCamera: moving the camera to lat: " + place.getLatLng().latitude + ", lgn: " +
                        place.getLatLng().longitude);

                return;
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }

        // login code
        if (requestCode == LOGIN_CODE) {

            if (resultCode == RESULT_OK) {

                // load the data again
                readDataFromDb(false);

                isLoggedIn = true;
                signInOutBtn.setImageResource(R.drawable.logout_image);

                // current site  to null
                currentSite = null;
                // is possible to add to false
                isPossibleToAdd = false;


                // get the intent from login
                currentUser = (User) data.getParcelableExtra("user");
                Log.d(TAG, currentUser.toString());
                Log.d(TAG, "onActivityResult: isSuperUser: " + currentUser.getIsSuperUser());
                isSuperUser = currentUser.getIsSuperUser();


                // display the result alert dialog
                final AlertDialog dialog1 = new AlertDialog.Builder(MapsActivity.this)
                        // validate the result of adding the item to the database
                        .setTitle("Success")
                        .setIcon(R.drawable.thumb_up)
                        .setMessage("You have logged in successfully.")
                        .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                        .create();


                dialog1.show();
            }
        }

    }


    private String PLACES_API_KEY = "AIzaSyC6CB6ZxpS-goZcAbiQUocsNw11PR12HUs";

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // setup place api key
        Places.initialize(getApplicationContext(), PLACES_API_KEY);

        mMap = googleMap;
        // setup polyline on click
        mMap.setOnPolylineClickListener(this);


        // set up cluster
        setUpClusters();

        isZoomedIn = true;

        LatLng latLng = new LatLng(10.729567, 106.6930756);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        mMap.getUiSettings().setZoomControlsEnabled(true);

        // set on map click
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (!isLoggedIn) {
                    final AlertDialog dialog1 = new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Not Login Yet")
                            .setIcon(R.drawable.ic_warning)
                            .setMessage("You are required to log in to perform the task")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    dialog1.show();
                    return;
                }

                // validate the isSuperUser
                if (isSuperUser) {
                    return;
                }

                // validate if the current user is possible to add
                if (!isPossibleToAdd) {
                    return;
                }

                // show the dialog
                showDialogForCreateSite(latLng);
            }
        });

        customInfoWindowAdaptor = new CustomInfoWindowAdaptor(MapsActivity.this, isLeader, currentUser, siteList);

    }

    // setup cluster
    public void setUpClusters() {

        clusterManager = new ClusterManager<Site>(this, mMap);

        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // set info window
        clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdaptor(MapsActivity.this, isLeader, currentUser, siteList));

        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());

        // site render
        siteRender = new SiteRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(siteRender);


        // set on click for window info
        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<Site>() {
            @Override
            public void onClusterItemInfoWindowClick(Site site) {
                if (site.getTitle().equals("My Location")) {
                    return;
                }
                //TODO: Remember to turn this code below on again
                Marker marker = mMap.addMarker(new MarkerOptions().icon(getMarkerIconFromDrawable(drawable)).snippet(site.getDescription()).title(site.getName()).position(new LatLng(site.getLatitude(), site.getLongitude()))
                );
                showDialogDetailsRegister(marker);
                marker.remove();


                return;
            }
        });

    }

    // on cluster click
    @Override
    public boolean onClusterClick(Cluster cluster) {
        float zoomLevel;
        if (mMap.getCameraPosition().zoom < 14) {
            zoomLevel = 14;
        } else {
            zoomLevel = mMap.getCameraPosition().zoom;
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cluster.getPosition().latitude, cluster.getPosition().longitude), zoomLevel));

        return true;
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        float zoomLevel;
        if (mMap.getCameraPosition().zoom < 14) {
            zoomLevel = 14;
        } else {
            zoomLevel = mMap.getCameraPosition().zoom;
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(clusterItem.getPosition().latitude, clusterItem.getPosition().longitude), zoomLevel));

        return true;
    }


    // some permissions
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean mLocationPermissionsGranted = false;

    // requests permissions
    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION,
                COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions
                        , LOCATION_PERMISSION_REQUEST_CODE);

            }
        } else {
            ActivityCompat.requestPermissions(this, permissions
                    , LOCATION_PERMISSION_REQUEST_CODE);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }

                    mLocationPermissionsGranted = true;
                    // initialize our map
                }
            }
        }
    }



    //show dialog of register and show details
    public void showDialogDetailsRegister(Marker marker) {

        // init current site
        currentSite = findCurrentSite(marker);

        // init dialog
        Dialog registerDetailsDialog = new Dialog(MapsActivity.this);
        registerDetailsDialog.setContentView(R.layout.register_see_details_layout);


        registerDetailsDialog.show();

        Button directionBtn = registerDetailsDialog.findViewById(R.id.btn_direction);

        directionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // routing
                resetSelectedMarker();
                mSelectedMarker = marker;
                mSelectedSite = currentSite;
                registerDetailsDialog.dismiss();
                marker.hideInfoWindow();
                calculateDirections(marker);
                marker.hideInfoWindow();
                marker.setVisible(false);
            }
        });

        Button detailsBtn = registerDetailsDialog.findViewById(R.id.btn_details);
        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isLoggedIn) {

                    showNegativeDialog("Error", "You cannot see the details of any site!", v);
                    return;
                }

                // check if the user is super user
                if (isSuperUser) {
                    // open the show details dialog
                    showDetailsDialog(marker);

                    return;
                }

                Log.d(TAG, currentSite.getDescription() + " , " + currentSite.getName() + " name");
                // check if the current user is the owner of this site
                if (currentSite.getUsername().equals(currentUser.getName())) {

                    registerDetailsDialog.dismiss();

                    // open the show details dialog
                    showDetailsDialog(marker);

                    return;
                } else {

                    registerDetailsDialog.dismiss();
                    showNegativeDialog("Error", "You are not the founder of this site!", v);
                    return;

                }
            }
        });

        // init close btn
        ImageButton closeBtn = registerDetailsDialog.findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerDetailsDialog.dismiss();
            }
        });


        // init register btn to show another dialog
        Button registerBtn = registerDetailsDialog.findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isLoggedIn) {


                    registerDetailsDialog.dismiss();
                    showNegativeDialog("Error", "You cannot register any site unless you login!", v);
                    return;
                }

                // validate the super user cannot register a site
                if (isSuperUser) {

                    // show dialog
                    registerDetailsDialog.dismiss();
                    showNegativeDialog("Error", "You cannot register any site because you are a super user.", v);
                    return;
                }

                showDialogForRegisterSite(marker);
                registerDetailsDialog.dismiss();
            }
        });

    }

    private Dialog detailsDialog;

    // show details dialog
    public void showDetailsDialog(Marker marker) {

        detailsDialog = new Dialog(MapsActivity.this);
        detailsDialog.setContentView(R.layout.dialog_details);

//        detailsDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        detailsDialog.show();

        // init the dialog and its function
        attachComponentInDetailsDialog();
        setTextToComponentInDetailsDialog();
        setButtonClickerInDetailsDialog(marker);

    }

    // set button clicker in details dialog
    private void setButtonClickerInDetailsDialog(Marker marker) {
        // backBtn
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                detailsDialog.dismiss();
            }
        });

        // edit btn
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.d(TAG, "editBtn: siteDescription: " + siteDescription.getText().toString());
//                Log.d(TAG, "editBtn: numberPeopleTested: " + numberPeopleTested.getText().toString());

                isZoomedIn = false;
                currentSite.setDescription(siteDescription.getText().toString());
                currentSite.setNumberPeopleTested(Integer.parseInt(numberPeopleTested.getText().toString()));

                databaseReference.child("sites").child(currentSite.getUsername() + "-" + currentSite.getName())
                        .updateChildren(currentSite.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // show the dialog
                        detailsDialog.dismiss();
                        showPositiveDialog("Success", "The site is successfully updated", v);
                        Log.d(TAG, "Successfully loaded the db after modify");
                    }
                });

                Log.d(TAG, "Edit button is clicked");
            }
        });

        // list btn
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click list volunteer");
                showListVolunteer();
            }
        });
    }

    // show list volunteers
    private void showListVolunteer() {

        // create dialog component
        Dialog listDialog = new Dialog(MapsActivity.this);
        listDialog.setContentView(R.layout.volunteer_lists);
//        listDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        Button backBtn = listDialog.findViewById(R.id.backToDetailsBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listDialog.dismiss();
            }
        });

        TextView siteName = listDialog.findViewById(R.id.titleDisplay);
        siteName.setText(currentSite.getTitle());


        // create list view components
        ListView lv = (ListView) listDialog.findViewById(R.id.list_volunteer);

        // create custom adapter
        CustomListAdapter customListAdapter = new CustomListAdapter(MapsActivity.this, currentSite.getUsers());
        lv.setAdapter(customListAdapter);


        // download btn
        Button btnDownload = listDialog.findViewById(R.id.downloadBtn);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    // validate the size to 0
                    if (currentSite.getUsers().size() == 0) {

                    }
                } catch (Exception e) {

                    // display the alert
                    listDialog.dismiss();
                    showNegativeDialog("Announcement", "There is no volunteer yet to download", v);
                    return;
                }


                try {

                    // write to external storage
                    writeFileExternalStorage();

                    listDialog.dismiss();
                    showPositiveDialog("Success", "The user list is successfully downloaded", v);

                } catch (Exception e) {
                    Log.d("Download", "Cannot read to the file");
                }


            }
        });

        listDialog.show();

    }

    // download files
    public void writeFileExternalStorage() {


        //Checking the availability state of the External Storage.
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {

            //If it isn't mounted - we can't write into it.
            return;
        }


        String filenameExternal = currentSite.getUsername() + "-" + currentSite.getTitle() + ".txt";
        //Create a new file that points to the root directory, with the given name:
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), filenameExternal);

        //This point and below is responsible for the write operation
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();

            String titleString = "Site: " + currentSite.getTitle() + "\n";

            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists
            outputStream = new FileOutputStream(file, false);

            outputStream.write(titleString.getBytes());

            //volunteer lists:
            for (User u : currentSite.getUsers()
            ) {

                String userString = "Username: " + u.getName() + " , mail: " + u.getEmail() + "\n";

                outputStream.write(userString.getBytes());
            }

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // attach component in dialog
    public void attachComponentInDetailsDialog() {

        backBtn = detailsDialog.findViewById(R.id.backToMapsBtn);
        listBtn = detailsDialog.findViewById(R.id.showListBtn);
        editBtn = detailsDialog.findViewById(R.id.editSiteBtn);
        siteTitle = detailsDialog.findViewById(R.id.siteTitle);
        ownerSite = detailsDialog.findViewById(R.id.ownerSite);
        siteLatitude = detailsDialog.findViewById(R.id.siteLatitude);
        siteLongitude = detailsDialog.findViewById(R.id.siteLongitude);
        numberPeopleSite = detailsDialog.findViewById(R.id.numberPeopleSiteTxt);
        numberPeopleTested = detailsDialog.findViewById(R.id.numberPeopleTested);
        siteDescription = detailsDialog.findViewById(R.id.siteDescription);
    }


    // set text
    public void setTextToComponentInDetailsDialog() {

        siteTitle.setText(currentSite.getTitle());
        siteLatitude.setText(currentSite.getLatitude() + "");
        siteLongitude.setText(currentSite.getLongitude() + "");
        siteDescription.setText(currentSite.getDescription());
        ownerSite.setText(currentSite.getUsername());

        Log.d(TAG, "setTextToComponentInDetailsDialog: currentSite.getUserStringLists().size() = " + currentSite.getUsers().size());
        numberPeopleSite.setText(currentSite.getUsers().size() + " ");
        numberPeopleTested.setText(currentSite.getNumberPeopleTested() + "");

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
            mMap.clear();

            readDataFromDb(false);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);


    }

    // check if user in the db
    public User getIfUserInDb(String username) {
        // validate if the user in the db
        for (User u : userList
        ) {
            Log.d(TAG, u.getName() + " name checked");


            try {
                if (u.getName().equals(username)) {
                    return u;
                }
            } catch (Exception e) {
                return null;
            }

        }
        return null;
    }

    // validate the user in db
    public boolean checkIfUserInDb(String username) {
        // validate if the user in the db
        for (User u : userList
        ) {
            Log.d(TAG, u.getName() + " name checked");


            try {
                if (u.getName().equals(username)) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }

        }
        return false;
    }

    // find current site when clicking into the marker
    public Site findCurrentSite(Marker marker) {
        // get the latlng
        LatLng latLng = marker.getPosition();


        Log.d(TAG, latLng.latitude + " current latitude");
        // loop through the site to get the current site
        for (Site s :
                siteList) {


//            Log.d(TAG, s.getLatitude() + " site latitude");
            if (s.getLatitude() == latLng.latitude && s.getLongitude() == latLng.longitude) {
//                Log.d(TAG, latLng.longitude + "  longitude");

//                Log.d(TAG, s.getLatitude() + " site latitude in if condition");

                return s;
            }

        }
        return new Site();
    }

    // show the dialog for register
    public void showDialogForRegisterSite(Marker marker) {

        registerSiteDialog = new Dialog(MapsActivity.this);
        registerSiteDialog.setContentView(R.layout.register_site_layout);


        registerSiteDialog.show();

        EditText usernameTxt = registerSiteDialog.findViewById(R.id.userNameRegisterSiteText);

        // yes btn
        Button btn_yes_register = registerSiteDialog.findViewById(R.id.btn_yes_register);

        btn_yes_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean ifUsernameExists = checkIfUserInDb(usernameTxt.getText().toString());

                // get user in db
                User user = getIfUserInDb(usernameTxt.getText().toString());

                Log.d(TAG, ifUsernameExists + " checked");


                // validate username exists
                if (!ifUsernameExists) {
                    registerSiteDialog.dismiss();
                    showNegativeDialog("Error", "The username is not found in the database!", v);

                    return;
                }

                // validate if the user already registered for this site
                Site site = findCurrentSite(marker);

                Log.d(TAG, site.getUsers() + " site longitude after loop");


                // validate the leader cannot join his site
                if (site.getUsername().equals(usernameTxt.getText().toString())) {

                    registerSiteDialog.dismiss();
                    showNegativeDialog("Error", "The username is already the leader of this site!", v);
                    return;
                }

                // variable to define if the user exists in site
                boolean isUserExistsInSite = false;

                //check if the site has that user
                try {
                    for (User u : site.getUsers()) {
                        Log.d(TAG, u.getName() + " username in loop");

                        if (u.getName().equals(usernameTxt.getText().toString())) {
                            isUserExistsInSite = true;
                            break;
                        }
                    }

                    Log.d(TAG, isUserExistsInSite + " is user exists in loop");

                    // validate if user is in that site
                    if (isUserExistsInSite) {
                        registerSiteDialog.dismiss();
                        showNegativeDialog("Error", "The username already registered!", v);
                        return;
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }


                List<User> userList1 = site.getUsers();

                // update the site
                //validate the username list in site
                try {
                    userList1.add(user);
                } catch (Exception e) {

                    userList1 = new ArrayList<>();
                    userList1.add(user);
                }


                site.setUsers(userList1);
                isZoomedIn = false;
                databaseReference.child("sites").child(site.getUsername() + "-" + site.getName()).setValue(site.toMap());

                registerSiteDialog.dismiss();
                showPositiveDialog("Success", "The user is successfully registered this site", v);
            }
        });
    }

    // check the user type
    public boolean ifUserIsAbleToSeeDetails() {

        for (Site site : siteList
        ) {

            // check if the username is as same as owner's site
            if (currentUser.getName().equals(site.getUsername())) {
                Log.d(TAG, "Equal");
                return true;
            }

        }

        return false;
    }


    // dialog used to create the new site
    private void showDialogForCreateSite(LatLng latLng) {

        createSiteDialog = new Dialog(this);
        createSiteDialog.setContentView(R.layout.layout_custom_dialog);

//        createSiteDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        createSiteDialog.show();

        // edit text
        EditText siteNameTxt = createSiteDialog.findViewById(R.id.txttite);

        EditText descriptionTxt = createSiteDialog.findViewById(R.id.txtDesc);

        // btn yes
        Button btn_yes = createSiteDialog.findViewById(R.id.btn_yes);

        btn_yes.setOnClickListener(v -> {
            // validate if the site name is null
            if (siteNameTxt.getText().toString().equals("")) {

                createSiteDialog.dismiss();

                showNegativeDialog("Error", "The site title is null! Cannot create the site", v);

                return;
            }

            // validate if the title name of the site is already existed in that user's sites
            if (ifSiteNameExistInThatUser(siteNameTxt.getText().toString())) {
                showNegativeDialog("Error", "This user already has this title for the site! Cannot create the site", v);
                return;
            }

            // Create the Site object
            Site site = new Site();
            site.setUsername(currentUser.getName());
            site.setLatitude(latLng.latitude);
            site.setLongitude(latLng.longitude);
            site.setName(siteNameTxt.getText().toString());
            site.setDescription(descriptionTxt.getText().toString());


            isZoomedIn = false;
            // If not null, then add it to the db
            databaseReference.child("sites").child(site.getUsername() + "-" + site.getName()).setValue(site.toMap());

            mMap.addMarker(new MarkerOptions().icon(getMarkerIconFromDrawable(drawable)).snippet(site.getDescription()).title(site.getName()).position(new LatLng(site.getLatitude(), site.getLongitude())));

            // set the leader to has his own site
            isLeader = true;

            createSiteDialog.dismiss();

            showPositiveDialog("Success", "The site is successfully created", v);
        });

    }

    // check if site name exists in the user
    public boolean ifSiteNameExistInThatUser(String stringName) {

        for (Site s : siteList
        ) {

            // look for the user who owns the sites first
            if (s.getUsername().equals(currentUser.getName())) {
                // check for if the name is duplicated
                if (stringName.equals(s.getName())) {

                    return true;
                }
            }
        }

        return false;
    }

    // dialog to show positive message
    public void showPositiveDialog(String title, String message, View v) {
        // display another alert dialog
        final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                // validate the result of adding the item to the database
                .setTitle(title)
                .setIcon(R.drawable.thumb_up)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .create();

        dialog1.show();
    }

    // dialog to show warning
    public void showNegativeDialog(String title, String message, View v) {
        final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                .setTitle(title)
                .setIcon(R.drawable.ic_warning)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .create();

        dialog1.show();
    }


    // close dialog
    public void closeDialog(View view) {
        createSiteDialog.dismiss();
    }


    // close register dialog
    public void closeRegisterSiteDialog(View view) {
        registerSiteDialog.dismiss();
        return;
    }

// draw bitmap
    public BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        marker.hideInfoWindow();
        onInfoWindowClose(marker);
//        Toast.makeText(MapsActivity.this, "Long click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClose(@NonNull Marker marker) {
        marker.hideInfoWindow();
//        Toast.makeText(MapsActivity.this, "Close click", Toast.LENGTH_SHORT).show();

    }

    // zoom route
    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    // on polyline click
    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

        int index = 0;

        for (PolylineData polylineData : mPolylineData) {
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(this.getColor(R.color.blue_phantom));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Drawable drawable = getResources().getDrawable(R.drawable.my_end_location);

                // add marker
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip: #" + index)
                        .icon(getMarkerIconFromDrawable(drawable))
                        .snippet("Duration: " + polylineData.getLeg().duration)

                );

                marker.showInfoWindow();

                mTripMarkers.add(marker);

            } else {
                polylineData.getPolyline().setColor(this.getColor(R.color.colorCardDT));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        readDataFromDb(false);
    }

    private static int AUTOCOMPLETE_REQUEST_CODE = 1;

    // search place using api
    public void searchPlace(View view) {


        Log.i("OnBackButtonClicked", "Success");

        // Set the fields to specify which types of currentPlace data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }
}
