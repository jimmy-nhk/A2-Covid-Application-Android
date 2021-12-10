package com.example.a2.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
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
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
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
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
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
    private boolean isLeader;
    private boolean isSuperUser;
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
            siteLatitude, siteLongitude,  numberPeopleSite,siteDescription;

    private EditText numberPeopleTested ;

    private ImageButton signInOutBtn, currentPositionBtn;
    private EditText mSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initServices();

        //get location permission for the map
        getLocationPermission();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // init direction
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                            .apiKey(getString(R.string.google_maps_key))
                            .build();

        }

        try {
            Intent intent = getIntent();
            currentUser = intent.getParcelableExtra("user");
            Log.d(TAG, "onCreate: currentUser name: " + currentUser.getName());
            isLoggedIn = true;
            signInOutBtn.setImageResource(R.drawable.logout_image);

        } catch (Exception e){
            currentUser = new User();
        }

    }

    private void resetMap(){
        if(mMap != null) {
            mMap.clear();

//            if(mClusterManager != null){
//                mClusterManager.clearItems();
//            }

//            if (mClusterMarkers.size() > 0) {
//                mClusterMarkers.clear();
//                mClusterMarkers = new ArrayList<>();
//            }

            if(mPolylineData.size() > 0){
                mPolylineData.clear();
                mPolylineData = new ArrayList<>();
            }

            readDataFromDb(true);
        }
    }

    // remove trip markers
    private void removeTripMarkers(){

        for (Marker marker: mTripMarkers){
            marker.remove();
        }
    }

    private void resetSelectedMarker(){
        if (mSelectedMarker != null){

            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    // add polylines
    private void addPolyLines(DirectionsResult result){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                // clear the polyline before adding in
                if (mPolylineData.size() > 0){

                    for (PolylineData polylineData: mPolylineData){
                        polylineData.getPolyline().remove();
                    }

                    mPolylineData.clear();
                    mPolylineData = new ArrayList<>();
                }

                double duration = 9999999;
                for (DirectionsRoute route: result.routes){

                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    for (com.google.maps.model.LatLng latLng: decodedPath){

                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(R.color.colorCardDT);
                    polyline.setClickable(true);

                    // add to polyline list
                    mPolylineData.add(new PolylineData(polyline, route.legs[0]));



                    double tempDuration = route.legs[0].duration.inSeconds;

                    // find the closest route
                    if (tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                    mSelectedMarker.setVisible(false);
                }
            }
        });
    }

    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

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
                Log.d(TAG, "onResult: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "onResult: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolyLines(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: Fail to get the routes" + e.getMessage() );

            }
        });
    }

    private void initServices(){

        // init realtime db
        firebaseDatabase = FirebaseDatabase.getInstance("https://a2-android-56cbb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();



        // init isLogin
        //TODO: isloggedin is true for testing now! by default, it is false
        isLoggedIn = false;
//        isLoggedIn = true;
        currentUser = new User();
        firebaseAuth = FirebaseAuth.getInstance();
        mSearchText = findViewById(R.id.input_search);
        drawable = getResources().getDrawable(R.drawable.site_cluster_large);

        // Init the object
//        firebaseHelper = new FirebaseHelper(MapsActivity.this);
        siteList = new ArrayList<>();
        userList = new ArrayList<>();

        // read the db
        readDataFromDb(true);

        ImageButton imageButton = findViewById(R.id.refreshBtn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMap();
            }
        });

        signInOutBtn = findViewById(R.id.signInOutBtn);

        if (isLoggedIn){
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
                    signInOutBtn.setImageResource(R.drawable.login_image);


                    return;
                }

                Intent intent = new Intent(MapsActivity.this, LogInActivity.class);
                startActivityForResult(intent, LOGIN_CODE);
            }
        });

        currentPositionBtn = findViewById(R.id.currentPositionBtn);

        currentPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

    }


    private Site oldSite;

    // read data from db
    private void readDataFromDb(boolean isGetCurrentLocation){

        // get current location
        if (isGetCurrentLocation){
            getDeviceLocation();
        }

        // load users
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                long size = snapshot.getChildrenCount();
//                Log.d(TAG, "Size is: " + size);

                GenericTypeIndicator<HashMap<String, User>> genericTypeIndicator =new GenericTypeIndicator<HashMap<String, User>>(){};

                HashMap<String,User> users= snapshot.getValue(genericTypeIndicator);


                try {
                    for (User u : users.values() ){
                        Log.d(TAG, "Value is: " + u.getEmail());
                        userList.add(u);
                    }



                } catch (Exception e){
                    Log.d(TAG, "Cannot load the users");
                }

                try {
                    isLeader = ifUserIsAbleToSeeDetails();
                }catch (Exception e){
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

                GenericTypeIndicator<HashMap<String, Site>> genericTypeIndicator =new GenericTypeIndicator<HashMap<String, Site>>(){};

                HashMap<String,Site> sites= snapshot.getValue(genericTypeIndicator);


                try {
//                    mMap.clear();

                    // init the siteList again
                    siteList = new ArrayList<>();


                    for (Site s : sites.values()){

                        mMap.addMarker(new MarkerOptions().icon(getMarkerIconFromDrawable(drawable)).snippet(s.getDescription()).title(s.getName()).position(new LatLng(s.getLatitude(), s.getLongitude())));
                        siteList.add(s);

                    }
                }catch (Exception e){
                    Log.d(TAG, "Cannot load the sites");
                }

                Site site = new Site();

                // validate the leader site
                for (Site s: siteList
                     ) {

                    try {
                        if (s.getUsername().equals(currentUser.getName())){
                            site = s;
//                            Log.d(TAG, "sitesDb: size of userList: " + site.getUserList().size());
//
//
//                            Log.d(TAG, "sitesDb: notification on change here");


                            //Fixme: A bug in the first load. It does not update the notification

                            // validate the old site
                            try {
                                if (site.compareTo(oldSite) == 0){
                                    Log.d(TAG, "readDataFromDb: no change in the leader site");
                                    return;
                                } else {
                                    oldSite = site;
                                    Log.d(TAG, "readDataFromDb: successfully send notification");
                                    // validate not to render notification
                                    createNotification(site.getTitle(), getApplicationContext());
                                    return;
                                }
                            } catch (Exception e){
                                oldSite = site;
                                Log.d(TAG, "readDataFromDb: First reload, no old site");
                                return;
                            }


                        }
                    } catch (Exception e){
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private NotificationManager notifManager;
    public void createNotification(String aMessage, Context context) {
        final int NOTIFY_ID = 0; // ID of notification
        String id = context.getString(R.string.app_name); // default_channel_id
        String title = context.getString(R.string.app_name); // Default Channel
        Intent intent;
        PendingIntent pendingIntent;


        NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
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
        intent.putExtra("user" , currentUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        stackBuilder.editIntentAt(0).putExtra("user" , currentUser);

// Get the PendingIntent containing the entire back stack
        pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

//        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentTitle(aMessage)                            // required
                .setSmallIcon(R.drawable.google)   // required
                .setContentText("Something has changed in your site") // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(aMessage)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        //TODO: when click to notification, it does not load the database
        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);


    }


    private void init() {
        Log.d(TAG, "init: initializing");

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                geoLocate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        List<Site> sites = new ArrayList<>();
        try {

            for (Site site: siteList
                 ) {

                //TODO: filter on more criteria if have time
                if (Objects.requireNonNull(site.getTitle()).contains(searchString)){
                    sites.add(site);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "geoLocated: IOException: " + e.getMessage());
        }

        if (sites.size() > 0){
            Log.d(TAG, "geoLocate: found a location: " + sites.get(0).toString());
            Toast.makeText(MapsActivity.this, sites.get(0).toString(), Toast.LENGTH_SHORT).show();

            LatLng latLng = new LatLng(sites.get(0).getPosition().latitude,sites.get(0).getPosition().longitude );
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }



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

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 17, "My Location");

                        } catch (Exception e){
                            Log.d(TAG, "onComplete: cannot move the map");
                        }

                    } else {
                        Log.d(TAG, "onComplete: current location is null!");
                        Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "getdevicelocation: Security: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lgn: " +
                latLng.longitude);

        Drawable drawable = getResources().getDrawable(R.drawable.my_location);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(getMarkerIconFromDrawable(drawable))
                .title(title);

        Marker marker = mMap.addMarker(options);
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        readDataFromDb(true);


    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        readDataFromDb(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // checking the notification sending intent
        if (requestCode == 0){

            isLoggedIn = true;
            currentUser = data.getParcelableExtra("user");
            isSuperUser = currentUser.getIsSuperUser();
        }

        if (requestCode == LOGIN_CODE) {

            if (resultCode == RESULT_OK) {

                readDataFromDb(true);


                isLoggedIn = true;
                signInOutBtn.setImageResource(R.drawable.logout_image);

                // get the intent from login
                currentUser = (User) data.getParcelableExtra("user");
                Log.d(TAG, currentUser.toString());
                isSuperUser = currentUser.getIsSuperUser();
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnPolylineClickListener(this);

        // set up cluster
//        setUpClusters();


        //TODO: get current location does not work
        getDeviceLocation();
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // set on map click
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (!isLoggedIn) {
                    final AlertDialog dialog1 = new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Not Login Yet")
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

                // validate if the current user is a leader
                if (isLeader) {
                    return;
                }

                // show the dialog
                showDialogForCreateSite(latLng);
            }
        });

        // search field
        init();

//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(@NonNull Marker marker) {
//                //FIXME: Testing purpose
//                calculateDirections(marker);
//                return false;
//            }
//        });
        customInfoWindowAdaptor = new CustomInfoWindowAdaptor(MapsActivity.this, isLeader, currentUser, siteList);

        // set custom marker window info
        mMap.setInfoWindowAdapter(customInfoWindowAdaptor);


        mMap.setOnInfoWindowClickListener(marker -> {

            if (marker.getTitle().equals("My Location")){
                return;
            }
            //TODO: Remember to turn this code below on again
            showDialogDetailsRegister(marker);

        });



    }

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean mLocationPermissionsGranted = false;

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

        Log.d(TAG, "showDialogDetailsRegister: currentSite- get userlist: " + currentSite.getUserList().size());

        Dialog registerDetailsDialog = new Dialog(MapsActivity.this);
        registerDetailsDialog.setContentView(R.layout.register_see_details_layout);

        registerDetailsDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        registerDetailsDialog.show();

        Button directionBtn = registerDetailsDialog.findViewById(R.id.btn_direction);

        directionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetSelectedMarker();
                //FIXME: testing the code below
                mSelectedMarker = marker;
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
                    final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                            .setTitle("Error")
                            .setMessage("You cannot see the details of any site!")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    dialog1.show();
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

                    final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                            .setTitle("Error")
                            .setMessage("You are not the founder of this site!")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    registerDetailsDialog.dismiss();
                    dialog1.show();
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
                    final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                            .setTitle("Error")
                            .setMessage("You cannot register any site unless you login!")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    dialog1.show();
                    return;
                }

                // validate the super user cannot register a site
                if (isSuperUser) {
                    final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                            .setTitle("Error")
                            .setMessage("You cannot register any site because you are a super user.")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    registerDetailsDialog.dismiss();
                    dialog1.show();
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

        detailsDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

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

                currentSite.setDescription(siteDescription.getText().toString());
                currentSite.setNumberPeopleTested(Integer.parseInt(numberPeopleTested.getText().toString()));
//                firebaseHelper.addSite(currentSite);

                databaseReference.child("sites").child(currentSite.getUsername() + "-"+currentSite.getName())
                        .updateChildren(currentSite.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // display the result alert dialog
                        final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                                // validate the result of adding the item to the database
                                .setTitle("Success")
                                .setIcon(R.drawable.thumb_up)
                                .setMessage("The site is successfully updated")
                                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                                .create();
                        detailsDialog.dismiss();

                        dialog1.show();
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

    private void showListVolunteer() {

        // create dialog component
        Dialog listDialog = new Dialog(MapsActivity.this);
        listDialog.setContentView(R.layout.volunteer_lists);

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
        CustomListAdapter customListAdapter = new CustomListAdapter(MapsActivity.this, currentSite.getUserList());
        lv.setAdapter(customListAdapter);

        listDialog.show();

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

        Log.d(TAG, "setTextToComponentInDetailsDialog: currentSite.getUserStringLists().size() = " + currentSite.getUserList().size());
        numberPeopleSite.setText(currentSite.getUserList().size() + " ");
        numberPeopleTested.setText(currentSite.getNumberPeopleTested() + "");

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
            mMap.clear();

        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);


    }

    public boolean checkIfUserInDb(String username) {
        // validate if the user in the db
        for (User u : userList
        ) {
            Log.d(TAG, u.getName() + " name checked");


            try {
                if (u.getName().equals(username)) {
                    return true;
                }
            } catch (Exception e){
                return false;
            }

        }
        return false;
    }

    public Site findCurrentSite(Marker marker) {
        // get the latlng
        LatLng latLng = marker.getPosition();


        Log.d(TAG, latLng.latitude + " current latitude");
        // loop through the site to get the current site
        for (Site s :
                siteList) {


            Log.d(TAG, s.getLatitude() + " site latitude");
            if (s.getLatitude() == latLng.latitude && s.getLongitude() == latLng.longitude) {
                Log.d(TAG, latLng.longitude + "  longitude");

                Log.d(TAG, s.getLatitude() + " site latitude in if condition");

                return s;
            }

        }
        return new Site();
    }

    public void showDialogForRegisterSite(Marker marker) {

        registerSiteDialog = new Dialog(MapsActivity.this);
        registerSiteDialog.setContentView(R.layout.register_site_layout);

        registerSiteDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        registerSiteDialog.show();

        EditText usernameTxt = registerSiteDialog.findViewById(R.id.userNameRegisterSiteText);

        Button btn_yes_register = registerSiteDialog.findViewById(R.id.btn_yes_register);


        btn_yes_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean ifUsernameExists = checkIfUserInDb(usernameTxt.getText().toString());

                Log.d(TAG, ifUsernameExists + " checked");


                // validate username
                if (!ifUsernameExists) {
                    final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                            .setTitle("Error")
                            .setMessage("The username is not found in the database!")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    registerSiteDialog.dismiss();
                    dialog1.show();
                    return;
                }

                // validate if the user already registered for this site


                Site site = findCurrentSite(marker);

                Log.d(TAG, site.getUserList() + " site longitude after loop");


                // validate the leader cannot join his site
                if (site.getUsername().equals(usernameTxt.getText().toString())) {
                    final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                            .setTitle("Error")
                            .setMessage("The username is already the leader of this site!")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    registerSiteDialog.dismiss();
                    dialog1.show();
                    return;
                }

                // variable to define if the user exists in site
                boolean isUserExistsInSite = false;

                //check if the site has that user
                try {
                    for (String username : site.getUserList()) {
                        Log.d(TAG, username + " username in loop");

                        if (username.equals(usernameTxt.getText().toString())) {
                            isUserExistsInSite = true;
                            break;
                        }
                    }

                    Log.d(TAG, isUserExistsInSite + " is user exists in loop");

                    // validate if user is in that site
                    if (isUserExistsInSite) {

                        final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                                .setTitle("Error")
                                .setMessage("The username already registered!")
                                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                                .create();

                        registerSiteDialog.dismiss();
                        dialog1.show();
                        return;
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }


                ArrayList<String> usernameList = site.getUserList();

                // update the site
                //validate the username list in site
                try {
                    usernameList.add(usernameTxt.getText().toString());
                } catch (Exception e) {
                    usernameList = new ArrayList<>();
                    usernameList.add(usernameTxt.getText().toString());
                }

                site.setUserList(usernameList);

                //TODO: Add the site to the db
                databaseReference.child("sites").child(site.getUsername() + "-"+ site.getName()).setValue(site.toMap());
//                firebaseHelper.addSite(site);

                // load again the site
//                loadSitesFromDb(new FirebaseHelperCallback() {

//
//                    @Override
//                    public void onDataChanged(List<Site> sites) {
//                        Log.d(TAG, "Loaded after added successfully");
//                        siteList = (ArrayList<Site>) sites;
//                    }
//                });

                // display the result alert dialog
                final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                        // validate the result of adding the item to the database
                        .setTitle("Success")
                        .setIcon(R.drawable.thumb_up)
                        .setMessage("The user is successfully registered this site")
                        .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                        .create();
                registerSiteDialog.dismiss();

                dialog1.show();
            }
        });
    }

    // check the user type
    public boolean ifUserIsAbleToSeeDetails() {


        for (Site site : siteList
        ) {

            Log.d(TAG, "Here hello");

            System.out.println(site.getUsername() + " site's owner name");
            System.out.println(currentUser.getName() + " username");
            // check if the username is as same as owner's site
            if (currentUser.getName().equals(site.getUsername())) {
                Log.d(TAG, "Equal");
                return true;
            }

        }

        return false;
    }

    public void setUpClusters() {

        clusterManager = new ClusterManager<>(this, mMap);
        siteRender = new SiteRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer((ClusterRenderer<Site>) clusterManager);
        clusterManager.setAnimation(true);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);

        mMap.setOnMarkerClickListener(clusterManager);


    }

    // dialog used to create the new site
    private void showDialogForCreateSite(LatLng latLng) {

        createSiteDialog = new Dialog(this);
        createSiteDialog.setContentView(R.layout.layout_custom_dialog);

        createSiteDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        createSiteDialog.show();


        EditText siteNameTxt = createSiteDialog.findViewById(R.id.txttite);

        EditText descriptionTxt = createSiteDialog.findViewById(R.id.txtDesc);

        Button btn_yes = createSiteDialog.findViewById(R.id.btn_yes);

        // Check the type of user

//        if (isSuperUser) {
//            btn_yes.setEnabled(false);
//            btn_yes.setTextColor(Color.parseColor("#808080"));
//        }

        btn_yes.setOnClickListener(v -> {
            // validate if the site name is null
            if (siteNameTxt.getText().toString().equals("")) {
                final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                        .setTitle("Error")
                        .setMessage("The site title is null! Cannot create the site")
                        .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                        .create();

                createSiteDialog.dismiss();
                dialog1.show();
                return;
            }

            // Create the Site object
            Site site = new Site();
            //TODO: setusername = currentUser.getName by default, set to constant for testing purpose
            site.setUsername(currentUser.getName());
            site.setLatitude(latLng.latitude);
            site.setLongitude(latLng.longitude);
            site.setName(siteNameTxt.getText().toString());
            site.setDescription(descriptionTxt.getText().toString());

            // If not null, then add it to the db
            databaseReference.child("sites").child(site.getUsername() + "-"+ site.getName()).setValue(site.toMap());
//            firebaseHelper.addSite(site);


            mMap.addMarker(new MarkerOptions().icon(getMarkerIconFromDrawable(drawable)).snippet(site.getDescription()).title(site.getName()).position(new LatLng(site.getLatitude(), site.getLongitude())));

            // set the leader to has his own site
            //TODO: isLeader is true by default, set to false for testing purpose
            isLeader = true;
//            isLeader = false;

            // load again the site
//            loadSitesFromDb(new FirebaseHelperCallback() {
//
//                @Override
//                public void onDataChanged(List<Site> siteList) {
//                    Log.d(TAG, "Loaded after added successfully");
//                }
//            });

            // display another alert dialog
            final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                    // validate the result of adding the item to the database
                    .setTitle("Success")
                    .setIcon(R.drawable.thumb_up)
                    .setMessage("The site is successfully created")
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .create();
            createSiteDialog.dismiss();

            dialog1.show();
        });

    }

    @Override
    public boolean onClusterClick(Cluster cluster) {
        return false;
    }

    @Override
    public boolean onClusterItemClick(ClusterItem item) {
        return false;
    }


    public void closeDialog(View view) {
        createSiteDialog.dismiss();
    }


    public void closeRegisterSiteDialog(View view) {
        registerSiteDialog.dismiss();
        return;
    }


    public BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    // This solves the asynchronous problem with fetch data
//    public interface FirebaseCallback {
//        void onDataChanged(List<User> users);
//    }
//
//    public void loadUsersFromDb(FirebaseCallback firebaseCallback) {
//
//        firebaseHelper.getAllUsersMapsActivity(new FirebaseCallback() {
//            @Override
//            public void onDataChanged(List<User> users) {
//                userList = users;
//            }
//        });
//    }

    // This solves the asynchronous problem with fetch data
//    public interface FirebaseHelperCallback {
//        void onDataChanged(List<Site> siteList);
//    }
//
//    // load the sites
//    public void loadSitesFromDb(FirebaseHelperCallback myCallback) {
//        firebaseHelper.getAllSites(new FirebaseHelperCallback() {
//            @Override
//            public void onDataChanged(List<Site> sites) {
//                for (Site site : sites
//                ) {
//                    mMap.addMarker(new MarkerOptions().snippet(site.getDescription()).title(site.getName()).icon(getMarkerIconFromDrawable(drawable)).position(new LatLng(site.getLatitude(), site.getLongitude())));
//                }
//                // take the site
//                siteList = (ArrayList<Site>) sites;
//
//                // Find out the current user a owner
//                isLeader = ifUserIsAbleToSeeDetails();
//            }
//        });
//    }

    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        marker.hideInfoWindow();
        onInfoWindowClose(marker);
        Toast.makeText(MapsActivity.this, "Long click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClose(@NonNull Marker marker) {
        marker.hideInfoWindow();
        Toast.makeText(MapsActivity.this, "Close click", Toast.LENGTH_SHORT).show();

    }

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

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

        int index = 0;

        for(PolylineData polylineData: mPolylineData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(this.getColor(R.color.blue_phantom));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(endLocation)
                        .title("Trip: #" + index)
//                        .icon(getMarkerIconFromDrawable(drawable))
                        .snippet("Duration: " + polylineData.getLeg().duration)

                );

                marker.showInfoWindow();

                mTripMarkers.add(marker);

            }
            else{
                polylineData.getPolyline().setColor(this.getColor(R.color.colorCardDT));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }
}