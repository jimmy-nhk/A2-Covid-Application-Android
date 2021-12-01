package com.example.a2;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.a2.databinding.ActivityMapsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final long UPDATE_INTERVAL = 10 * 1000; //10s
    private static final long FASTEST_INTERVAL = 2 * 1000; //2s
    public static final String RESTAURANT_API = "https://my-json-server.typicode.com/jimmy-nhk/json-server";
    public static final String TAG = "MapsActivity";

    protected FusedLocationProviderClient  client;
    protected LocationRequest mLocationRequest;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Init the object
        firebaseHelper = new FirebaseHelper(MapsActivity.this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    // This solves the asynchronous problem with fetch data
    public interface FirebaseHelperCallback {
        void onDataChanged(List<Site> siteList);
    }

    public void loadSitesFromDb(FirebaseHelperCallback myCallback) {


        firebaseHelper.getAllSites(new FirebaseHelperCallback() {
            @Override
            public void onDataChanged(List<Site> siteList) {

                for (Site site: siteList
                     ) {

                    mMap.addMarker(new MarkerOptions().title(site.getName()).position(site.getLatLng()));
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        loadSitesFromDb(new FirebaseHelperCallback() {
            @Override
            public void onDataChanged(List<Site> siteList) {
                Log.d(TAG, siteList.toString());
            }

        });

        // Initialize the camera
        LatLng rmit = new LatLng(10.72978835877818, 106.69307559874231);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(rmit));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rmit, 15));

        mMap.getUiSettings().setZoomControlsEnabled(true);


        // set on map click
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {


                Toast.makeText(MapsActivity.this, "Hello 1", Toast.LENGTH_SHORT).show();


                showDialogForCreateSite(latLng);


                System.out.println(" latitude: " + latLng.latitude);


                Toast.makeText(MapsActivity.this, latLng.latitude + " , " + latLng.longitude, Toast.LENGTH_LONG).show();
            }
        });

        // set on marker listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                System.out.println(marker.getP);
                return false;
            }
        });


    }

    // dialog used to create the new site
    private void showDialogForCreateSite(LatLng latLng) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // Set up the input
        EditText siteText = new EditText(this);

        siteText.setInputType(InputType.TYPE_CLASS_TEXT);
        dialogBuilder.setView(siteText);
        dialogBuilder.setTitle("Add Location");
        dialogBuilder.setMessage("Please fill in the site's name to create the site.");

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", latLng.latitude);
        location.put("longitude", latLng.longitude);



        dialogBuilder.setMessage("")

                .setPositiveButton("Yes", (dialog, which) -> {


                    // validate if the site name is null
                    if (siteText.getText().toString().equals("")){
                        final AlertDialog dialog1 = new AlertDialog.Builder(this)
                                .setTitle("Error")
                                .setMessage("The site title is null! Cannot create the site")
                                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                                .create();

                        dialog1.show();
                        return;
                    }

                    // Create the Site object
                    Site site = new Site();
                    site.setLatLng(latLng);
                    site.setName(siteText.getText().toString());
                    // If not null, then add it to the db
                    firebaseHelper.addSite(site); ;

                    mMap.addMarker(new MarkerOptions().title(site.getName()).position(site.getLatLng()));

                    // display another alert dialog
                    final AlertDialog dialog1 = new AlertDialog.Builder(this)
                            // validate the result of adding the item to the database
                            .setTitle(  "Success")
//                            .setIcon(android.R.drawable.th.xml) //TODO: setIcon
                            .setMessage( "The site is successfully created" )
                            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                            .create();

                    dialog1.show();


                })

                // A null listener allows the button to dismiss the dialogBuilder and take no further action.
                .setNegativeButton("No", (dialog, which) -> {

                    return;
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
}