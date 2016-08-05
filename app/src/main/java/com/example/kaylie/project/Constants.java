package com.example.kaylie.project;

import com.google.android.gms.location.Geofence;

/**
 * Created by temilola on 7/12/16.
 */
public final class Constants {

    private Constants(){

    }

    //Geoference Radius
    public static final float GEOFENCE_RADIUS= 300.0f;
    public static final long EXPIRATION_TIME= Geofence.NEVER_EXPIRE;
    //Tag for debugging
    public static final String TAG= "Geofence";

    // Keys for flattened geofences stored in SharedPreferences.
    public static final String KEY_LATITUDE = "com.example.kaylie.project.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "com.example.kaylie.project.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "com.example.kaylie.project.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION = "com.example.kaylie.projectKEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE = "com.example.kaylie.project.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys.
    public static final String KEY_PREFIX = "com.example.kaylie.project.geofencing.KEY";

    // Invalid values, used to test geofence storage when retrieving geofences.
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
}
