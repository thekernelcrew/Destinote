package com.example.kaylie.project.Fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.Geofence.GeofenceClass;
import com.example.kaylie.project.ListUpdater;
import com.example.kaylie.project.R;
import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.example.kaylie.project.Constants.EXPIRATION_TIME;
import static com.example.kaylie.project.Constants.GEOFENCE_RADIUS;
import static com.example.kaylie.project.Constants.TAG;

/**
 * Created by claireshu on 7/7/16.
 */

@RuntimePermissions
public class MapsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private static View mView;

    List<ParseObject> mTaskLocationList;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 60000; /* 60 secs */
    private static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int ACCESS_FINE_LOCATION_REQUEST = 1;
    private static final int ACCESS_COARSE_LOCATION_REQUEST = 2;

    List<Geofence> mGeofenceList;
    PendingIntent mGeofencePendingIntent; //pending intent used to add and remove geofences
    List<GeofenceClass> mGeofenceClassList;
    List<ParseObject> mTaskList;
    List<ParseObject> mTaskRemoveList;
    List<String> removeIds; //Arraylist to contain remove ids of geofences
    private boolean firstLocationUpdate = true; // Only zooms camera to current location on creation


    private AlarmManager alarmMgr;

    /*
     * Sets up maps and location requests and gets geofences
     */
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        // Create the location client to start receiving updates
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener(this).build();

        mGoogleApiClient.connect();
        getPermissionFineLocation();
        getPermissionCoarseLocation();

        mGeofenceList = new ArrayList<>();
        mGeofenceClassList= new ArrayList<>();
        mGeofencePendingIntent = null;
        removeIds = new ArrayList<>();

        ArrayList<String> idArrayList= new ArrayList<>();

        updateGeofences(idArrayList);

        //Check to remove any geofences and remove geofences
        removeSavedGeoFence();
        ListUpdater listUpdater = ListUpdater.getInstance();

    }

    /*
     * Sets up the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if (container == null) {
            return null;
        }
        mView = inflater.inflate(R.layout.maps_layout, container, false);

        setUpMapIfNeeded(); // Sets up the map method

        return mView;
    }

    /*
     * Delegates to setUpMap method
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        if (mMap != null)
            setUpMap();



    }

    /*
     * Remove the mapfragment's id from the FragmentManager or else if it is passed on the next time then
     * app will crash
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /*
     * Remove the mapfragment id in onStop because otherwise the activity is destroyed before the fragment is destroyed
     */
    @Override
    public void onStop() {

        if (mMap != null) {
            mMap = null;
            DisplayHomeActivity.fragmentManager.beginTransaction()
                    .remove(getChildFragmentManager().findFragmentById(R.id.location_map)).commitAllowingStateLoss();
        }
        super.onStop();
    }


    /*
     * To overcome FragmentManager being broken when it is detached from an activity.
     */
    @Override
    public void onDetach() {


        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        super.onDetach();
    }

    /*
     * Disconnects the GoogleApiClient if it is not null
     */
    @Override
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }



    /*
     * Get fine location permissions
     */
    public void getPermissionFineLocation(){

        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
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
     * Get coarse location permissions
     */
    public void getPermissionCoarseLocation(){

        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
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

    /*
     * Callback with the request from calling requestPermissions(...)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == ACCESS_COARSE_LOCATION_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Access coarse location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Access coarse location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == ACCESS_FINE_LOCATION_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Access coarse permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Access coarse permission denied", Toast.LENGTH_SHORT).show();
            }

        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    /*
     * Sets up the map if there is no map
     */
    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.location_map));
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {

                    mMap = googleMap;

                    setUpMap();

                }
            });
            // Check if we were successful in obtaining the map.
            if (mMap != null)
                setUpMap();
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
        // For showing a move to my loction button
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);



        ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereNotEqualTo("latitude", null);
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        try {
            mTaskLocationList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mTaskLocationList.size(); i++) {
            ParseObject task = mTaskLocationList.get(i);
            String latitude = task.getString("latitude");
            String longitude = task.getString("longitude");
            String title= task.getString("name");
            String description;
            if(task.getString("description") != null) {
                description = task.getString("description");
            } else {
                description = "Remember: " + title;
            }

            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))).title(title).snippet(description).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_marker)));
        }



        for (int i= 0; i < mGeofenceClassList.size(); i++){
            mMap.addCircle(getCircleOptions(mGeofenceClassList.get(i)));
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onConnected(@Nullable Bundle bundle) {

        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        /* Trigger location updates */
        if (mCurrentLocation != null) {
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            // For zooming automatically to the current location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));

        }else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        try{
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        // save the geofence using shared preferences

                    } else {
                        // 5. If not successful, log and send an error
                        Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                                " : " + status.getStatusCode());
                    }
                }
            });
        }
        catch (SecurityException securityException) {
            Log.d("Add Geofence Exception", securityException.toString(), securityException);
        }
        catch(IllegalArgumentException exception){
            Log.d(TAG, exception.toString(), exception);
        }

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    removeIds
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        // save the geofence using shared preferences

                    } else {
                        // 5. If not successful, log and send an error
                        Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage() +
                                " : " + status.getStatusCode());
                    }
                }
            });
        }
        catch (SecurityException securityException){
            Log.d("DeleteGeofenceException", securityException.toString(), securityException);
        }
        catch(IllegalArgumentException exception){
            Log.d(TAG, exception.toString(), exception);
        }

        // Begin polling for new location updates.
        startLocationUpdates();

    }

    /*
     * Starts new location updates at interval
     */
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        // Request location updates
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    /* Notifies user if the location service has been stopped*/
    @Override
    public void onConnectionSuspended(int i) {

        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(getContext(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(getContext(), "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * Updates camera to zoom on current location at first
     */
    @Override
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onLocationChanged(Location location) {

        // Zooms on current location at first
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(mMap != null && firstLocationUpdate){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            firstLocationUpdate = false;
        }

    }


    //specifies geofencing requests to be monitored and how the geofence notifications should be reported
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();

    }

    private PendingIntent getGeofencePendingIntent(){
      //Geofence pending intent

        //Reuse pending intent if we already have it
        if (mGeofencePendingIntent != null){
            return  mGeofencePendingIntent;
        }
        //Intent intent= new Intent(getContext(), GeofenceTransitionsReceiver.class);
        Intent intent= new Intent("com.example.kaylie.project.geofence.ACTION_RECEIVE_GEOFENCE");
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void removeSavedGeoFence() {
        //Method to remove geofence when User has completed task

        //Check if isCompleted and remove geofences on isCompleted
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        query.whereEqualTo("is_completed", true);
        try {
            mTaskRemoveList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < mTaskRemoveList.size(); j++) {
            ParseObject task = mTaskRemoveList.get(j);
            if (task.getString("geofence_id") != null) {
                removeIds.add(task.getString("geofence_id"));
            }
        }
    }

    public void removeGeofence(){
        //Method removes the geofence from api and stops tracking
        PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient, removeIds);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "Success");
                } else {
                    // 5. If not successful, log and send an error
                    Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                            " : " + status.getStatusCode());
                }
            }
        }); // Result processed in onResult().
    }

    /*
     * Add circle to geofence
     */
    public CircleOptions getCircleOptions(GeofenceClass geofenceClass){
        // Create CircleOptions based on geofence
        return new CircleOptions()
                .center(new LatLng(geofenceClass.getLatitude(), geofenceClass.getLongitude()))
                .radius(geofenceClass.getRadius())
                .strokeColor(Color.TRANSPARENT)
                .fillColor(0x1A00ff00)
                .visible(true);
    }

    public void updateGeofences(ArrayList<String> idArrayList){
        //Check for tasks that have not been completed and update the sharedPreferences if empty
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        query.whereEqualTo("is_completed", false);
        try {
            mTaskList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

     for(int i=0; i< mTaskList.size(); i++) {
         ParseObject task = mTaskList.get(i);
         if (task.getString("geofence_id") != null) {
             String geofenceId = task.getString("geofence_id");
             double latitude = Double.valueOf(task.getString("latitude"));
             double longitude = Double.valueOf(task.getString("longitude"));
             GeofenceClass geofenceClass = new GeofenceClass(geofenceId, Geofence.GEOFENCE_TRANSITION_DWELL,
                     longitude, //longitude
                     latitude, //latitude
                     GEOFENCE_RADIUS, EXPIRATION_TIME);
             //mGeofenceStore.setGeofence(geofenceId, geofenceClass);
             mGeofenceClassList.add(geofenceClass);
             mGeofenceList.add(geofenceClass.createGeofence());
             idArrayList.add(geofenceId);
         }
     }
    }

}
