package com.example.kaylie.project.Geofence;

import com.google.android.gms.location.Geofence;

/**
 * Created by temilola on 7/12/16.
 */
public class GeofenceClass {

    //Instance Variables
    private String mGeofenceId;
    private int mTransitionType;
    private double mLongitude;
    private double mLatitude;
    private float mRadius;
    private long mExpirationTime;

    /**
     * Default constructor
     * @param geofenceId unique identifier for geofence
     * @param transitionType geofence transitions to watch
     * @param longitude geofence longitude
     * @param latitude geofence latitude
     * @param radius geofence radius in meters
     * @param expirationTime geofence expiration time
     */
    public GeofenceClass(String geofenceId, int transitionType, double longitude, double latitude, float radius, long expirationTime) {
        this.mGeofenceId = geofenceId;
        this.mTransitionType = transitionType;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mRadius = radius;
        this.mExpirationTime = expirationTime;
    }

    // Instance field getters.
    public String getId() {
            return mGeofenceId;
        }
    public double getLatitude() {
            return mLatitude;
        }
    public double getLongitude() {
            return mLongitude;
        }
    public float getRadius() {
            return mRadius;
        }
    public long getExpirationDuration() {
            return mExpirationTime;
        }
    public int getTransitionType() {
            return mTransitionType;
        }


    /**
     * Builds a new Geofence object.
     * @return Geofence object
     */
    public Geofence createGeofence() {

        return new Geofence.Builder()
                .setRequestId(mGeofenceId)
                .setCircularRegion(mLatitude, mLongitude, mRadius)
                .setExpirationDuration(mExpirationTime).setTransitionTypes(mTransitionType)
                .setLoiteringDelay(10)
                .build();
        }

}
