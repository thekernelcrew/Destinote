package com.example.kaylie.project.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import permissions.dispatcher.NeedsPermission;

import static com.example.kaylie.project.Constants.TAG;

public class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, PlaceSelectionListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editor;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Data that is passed back into AddTaskDialog
    private static double sLatitude, sLongitude;
    private static String sPlaceName;

    private static String uniqueId;
    private static final int ACCESS_FINE_LOCATION_REQUEST = 1;
    private static final int ACCESS_COARSE_LOCATION_REQUEST = 2;
    public static final int REQUEST_OK = 10;
    public ArrayList<String> mFavLocations;

    private boolean firstTimeLocation = true; // Only zooms camera to current location when created


    private SupportMapFragment mSupportMapFragment;

    /*
     * Gets permissions for and sets up map and location polling
     */
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        // Get access to the custom title view
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        Typeface customLato = Typeface.createFromAsset(getAssets(),  "fonts/SourceSansPro-Light.otf");
        mTitle.setTypeface(customLato);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        // Create the location client to start receiving updates
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener(this).build();

        mGoogleApiClient.connect();
        getPermissionFineLocation();
        getPermissionCoarseLocation();

        Button resBtn = (Button) findViewById(R.id.btnResMap);
        resBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendBackResult();
            }
        });


        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapwhere);
        if (mSupportMapFragment == null) {
            mSupportMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.flMapsLocPlaceHolder, mSupportMapFragment).commit();
        }

        //Getting the map
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (googleMap != null) {

                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                        mMap = googleMap;
                        setUpMap();

                    }

                }
            });
        }

        //Access shared preferences to see stored favorite locations
        sharedPref = getSharedPreferences("FavoriteLocations", 0);
        editor = sharedPref.edit();
        mFavLocations = new ArrayList<>();
        Set<String> hash = sharedPref.getStringSet("favoriteLocations", null);
        if (hash != null) {
            for (String s : hash) {
                mFavLocations.add(s);
            }
        }

        if (mMap != null)
            setUpMap();

        // Set up the autocomplete fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);
        uniqueId = UUID.randomUUID().toString();



    }


    /*
     * Destroys the map when the fragment is destroyed
     */
    @Override
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onDestroy() {
        // Disconnecting the client invalidates it.

        if(LocationServices.FusedLocationApi != null && mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        if (mMap != null && getSupportFragmentManager().findFragmentById(R.id.dialog_map) != null) {
            DisplayHomeActivity.fragmentManager.beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.dialog_map)).commit();
            mMap = null;
        }

        super.onDestroy();

    }

    /*
     * Get permissions for fine location
     */
    public void getPermissionFineLocation(){

        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST);
        }

    }

    /*
     * Get permissions for coarse location
     */
    public void getPermissionCoarseLocation(){

        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_REQUEST);
        }

    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {


        // Make sure it's the first READ_CONTACTS request
        if (requestCode == ACCESS_COARSE_LOCATION_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access coarse location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Access coarse location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == ACCESS_FINE_LOCATION_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access coarse permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Access coarse permission denied", Toast.LENGTH_SHORT).show();
            }

        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap}
     * is not null.
     */
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void setUpMap() {
        // For showing a move to my location button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

        LatLng location = null;
        for( String loc: mFavLocations){
            String placeName = loc.substring(0, loc.indexOf('~'));
            location = new LatLng(Double.parseDouble(loc.substring(loc.indexOf('~') + 1, loc.indexOf(','))),
                    Double.parseDouble(loc.substring(loc.indexOf(',') + 1)));
            mMap.addMarker(new MarkerOptions().position(location).title(placeName).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_marker)));
        }

        if(location != null) {
            // Zooms to location of last dropped pin
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*
     * Get current location
     */
    @Override
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onConnected(@Nullable Bundle bundle) {

        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }


        //Pol for new location updates.
        startLocationUpdates();

    }

    /*
     * Trigger new location updates at interval
     */
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    /*
     * Notifies user if the location services have been suspended
     */
    @Override
    public void onConnectionSuspended(int i) {

        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * Changes the map to reflect the current location the first time it is loaded
     */
    @Override
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(mMap != null && firstTimeLocation){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            firstTimeLocation = false;
        }

    }


    /*
     * Makes markers when map is clicked
     */
    public void onMapClick(LatLng latLng) {

        final LatLng finLatLng = latLng;

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        MarkerOptions m = new MarkerOptions().position(latLng).title(input.getText().toString()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_marker));
        final Marker currMarker = mMap.addMarker(m);


        AlertDialog promptBox = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Location name for " + String.format("%.2f", latLng.latitude) + ", " + String.format("%.2f", latLng.longitude))
                .setNegativeButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sLatitude = finLatLng.latitude;
                        sLongitude = finLatLng.longitude;
                        sPlaceName = input.getText().toString();
                        dialog.dismiss();

                    }
                })

                .setPositiveButton("Add to Favorites and Save", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        sLatitude = finLatLng.latitude;
                        sLongitude = finLatLng.longitude;
                        sPlaceName = input.getText().toString();
                        String savedLocation = sPlaceName + "~" + sLatitude + "," + sLongitude;
                        mFavLocations.add(savedLocation);
                        dialog.dismiss();


                    }

                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        sLatitude = 0;
                        sLongitude = 0;
                        sPlaceName = null;
                        currMarker.remove();
                        dialog.dismiss();
                    }
                })
                .create();
        promptBox.show();

    }

    /*
     * Calls method to show the searched place
     */
    @Override
    public void onPlaceSelected(Place place) {

        showPlaceSelected(place);

    }


    /*
     * Shows the selected place on the map as a marker and provides options for sending data back
     */
    public void showPlaceSelected(Place place){

        final Place finPlace = place;

        final String finPlaceName = place.getName().toString();

        final LatLng location = place.getLatLng();

        if(mMap != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }


        MarkerOptions m = new MarkerOptions().position(location).title(finPlaceName).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_marker));
        final Marker currMarker = mMap.addMarker(m);


        AlertDialog promptBox = new AlertDialog.Builder(this)
                .setTitle("Location: " + finPlaceName)
                .setNegativeButton("Save ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sLatitude = finPlace.getLatLng().latitude;
                        sLongitude = finPlace.getLatLng().longitude;
                        if(finPlaceName != null) {

                            sPlaceName = finPlace.getName().toString();
                        }
                        dialog.dismiss();

                    }
                })
                .setPositiveButton("Add to Favorites and Save", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        sLatitude = location.latitude;
                        sLongitude = location.longitude;
                        if(finPlaceName != null) {

                            sPlaceName = finPlace.getName().toString();
                        }
                        String savedLocation = sPlaceName + "~" + sLatitude + "," + sLongitude;

                        mFavLocations.add(savedLocation);
                        dialog.dismiss();


                    }

                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currMarker.remove();
                        dialog.dismiss();
                    }
                })
                .create();
        promptBox.show();


    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }


    /*
     * Call this method to send the data back to the parent fragment
     */
    public void sendBackResult() {

        HashSet<String> hash = new HashSet<String>();
        for (String l: mFavLocations){
            hash.add(l);
        }

        editor.putStringSet("favoriteLocations", hash);
        editor.apply();

        Intent i = new Intent(this, DisplayHomeActivity.class);
        i.putExtra("geofence_id", uniqueId);
        i.putExtra("latitude", sLatitude);
        i.putExtra("longitude",sLongitude);
        i.putExtra("place_name", sPlaceName);
        setResult(REQUEST_OK, i);
        finish();
    }

    /*
     * Stop back press
     */
    @Override
    public void onBackPressed() {

    }


    /*
     * When the marker is pressed provide options for saving the location, deleting the marker, and canceling
     * the transaction
     */
    @Override
    public boolean onMarkerClick(Marker marker) {

        final Marker markerReference = marker;

        final String placeName = marker.getTitle();
        final LatLng placeLoc = marker.getPosition();
        AlertDialog promptBox = new AlertDialog.Builder(this)
                .setTitle("Location: " + placeName)
                .setPositiveButton("Save ", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        sLatitude = placeLoc.latitude;
                        sLongitude = placeLoc.longitude;
                        sPlaceName = placeName;
                        dialog.dismiss();

                    }
                }).setNeutralButton("Delete Marker", new DialogInterface.OnClickListener(){


                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        for( int j = 0; j < mFavLocations.size(); j++){
                            if (mFavLocations.get(j).equals(placeName + "~" + placeLoc.latitude + "," + placeLoc.longitude)){
                                mFavLocations.remove(j);
                            }
                        }
                        markerReference.remove();
                        dialog.dismiss();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener(){


                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                }).create();
        promptBox.show();
        return true;
    }
}
