package com.example.a2.activity;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a2.R;
import com.example.a2.controller.FirebaseHelper;
import com.example.a2.helper.CustomInfoWindowAdaptor;
import com.example.a2.helper.SiteRenderer;
import com.example.a2.model.Site;
import com.example.a2.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.a2.databinding.ActivityMapsBinding;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ClusterManager.OnClusterClickListener, ClusterManager.OnClusterItemClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final long UPDATE_INTERVAL = 10 * 1000; //10s
    private static final long FASTEST_INTERVAL = 2 * 1000; //2s
    public static final String RESTAURANT_API = "https://my-json-server.typicode.com/jimmy-nhk/json-server";
    public static final String TAG = "MapsActivity";

    protected FusedLocationProviderClient client;
    protected LocationRequest mLocationRequest;

    private FirebaseHelper firebaseHelper;
    private List<Site> siteList;

    private ClusterManager<Site> clusterManager;
    private SiteRenderer siteRender;

    private User currentUser;
    private boolean isLeader;
    private boolean isSuperUser;
    private Dialog dialog;

    private Drawable drawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get the intent from login
        Intent intent = getIntent();
        currentUser = (User) intent.getParcelableExtra("user");
        Log.d(TAG, currentUser.toString());
        isSuperUser = currentUser.getIsSuperUser();


        drawable = getResources().getDrawable(R.drawable.site_cluster_large);

        // Init the object
        firebaseHelper = new FirebaseHelper(MapsActivity.this);
        siteList = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        dialog.dismiss();
    }

    // sign out
    public void signOut(View view) {
        Intent intent = new Intent(MapsActivity.this, LogInActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    // This solves the asynchronous problem with fetch data
    public interface FirebaseHelperCallback {
        void onDataChanged(List<Site> siteList);
    }

    public void loadSitesFromDb(FirebaseHelperCallback myCallback) {


        firebaseHelper.getAllSites(new FirebaseHelperCallback() {
            @Override
            public void onDataChanged(List<Site> sites) {

                for (Site site : sites
                ) {

                    mMap.addMarker(new MarkerOptions().snippet(site.getDescription()).title(site.getName()).icon(getMarkerIconFromDrawable(drawable)).position(new LatLng(site.getLatitude(), site.getLongitude())));
                }
                // take the site
                siteList = sites;

                // Find out the current user a owner
                isLeader = ifUserIsAbleToSeeDetails();
            }
        });
    }

    public BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        loadSitesFromDb(new FirebaseHelperCallback() {
            @Override
            public void onDataChanged(List<Site> siteList) {
                Log.d(TAG, siteList.toString());

            }

        });

        // set up cluster
//        setUpClusters();

        // Init the camera
        // Initialize the camera
        LatLng rmit = new LatLng(10.72978835877818, 106.69307559874231);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rmit));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rmit, 15));
        mMap.getUiSettings().setZoomControlsEnabled(true);



        // set on map click
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {


                if (isLeader){
                    //TODO: Announce the alertdialog instead
                    // display another alert dialog
                    final AlertDialog dialog1 = new AlertDialog.Builder(MapsActivity.this)
                            // validate the result of adding the item to the database
                            .setTitle("Fail")
                            .setIcon(R.drawable.thumb_up)
                            .setMessage("The user already has joined or created his site.")
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    dialog1.show();
                    return;
                }

                // show the dialog
                showDialogForCreateSite(latLng);



//                Toast.makeText(MapsActivity.this, latLng.latitude + " , " + latLng.longitude, Toast.LENGTH_LONG).show();
            }
        });

        CustomInfoWindowAdaptor customInfoWindowAdaptor =  new CustomInfoWindowAdaptor(MapsActivity.this, isLeader, currentUser, siteList);

        // set custom marker window info
        mMap.setInfoWindowAdapter(customInfoWindowAdaptor);

        Button registerBtn = customInfoWindowAdaptor.getmWindow().findViewById(R.id.registerBtn);


        Button seeDetailsBtn = customInfoWindowAdaptor.getmWindow().findViewById(R.id.seeDetailsBtn);


    }

    public boolean ifUserIsAbleToSeeDetails(){


        for (Site site: siteList
        ) {

            Log.d(TAG, "Here hello");

            System.out.println(site.getUsername() + " site's owner name");
            System.out.println(currentUser.getName() + " username");
            // check if the username is as same as owner's site
            if (currentUser.getName().equals(site.getUsername())){
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

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_custom_dialog);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

        dialog.show();


        EditText siteNameTxt = dialog.findViewById(R.id.txttite);

        EditText descriptionTxt = dialog.findViewById(R.id.txtDesc);

        Button btn_yes = dialog.findViewById(R.id.btn_yes);

        // Check the type of user

        //TODO: Check if the user is the owner of the site
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

                dialog.dismiss();
                dialog1.show();
                return;
            }

            // Create the Site object
            Site site = new Site();
            site.setUsername(currentUser.getName());
            site.setLatitude(latLng.latitude);
            site.setLongitude(latLng.longitude);
            site.setName(siteNameTxt.getText().toString());
            site.setDescription(descriptionTxt.getText().toString());

            // If not null, then add it to the db
            firebaseHelper.addSite(site);


            mMap.addMarker(new MarkerOptions().icon(getMarkerIconFromDrawable(drawable)).snippet(site.getDescription()).title(site.getName()).position(new LatLng(site.getLatitude(), site.getLongitude())));

            // set the leader to has his own site
            isLeader = true;

            // load again the site
            loadSitesFromDb(new FirebaseHelperCallback() {
                @Override
                public void onDataChanged(List<Site> siteList) {
                    Log.d(TAG, "Loaded after added successfully");
                }
            });

            // display another alert dialog
            final AlertDialog dialog1 = new AlertDialog.Builder(v.getContext())
                    // validate the result of adding the item to the database
                    .setTitle("Success")
                    .setIcon(R.drawable.thumb_up)
                    .setMessage("The site is successfully created")
                    .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                    .create();
            dialog.dismiss();

            dialog1.show();
        });

    }
}