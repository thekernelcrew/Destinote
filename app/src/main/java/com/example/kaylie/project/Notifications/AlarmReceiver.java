package com.example.kaylie.project.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.kaylie.project.Geofence.GeofenceTransitionsReceiver;

/**
 * Created by temilola on 7/21/16.
 */
public class AlarmReceiver extends BroadcastReceiver{
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.kaylie.example.GeofenceTransitionsReceiver.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, GeofenceTransitionsReceiver.class);
        Toast.makeText(context, "Alarm Receiver triggered", Toast.LENGTH_LONG).show();
        context.startService(i);
    }
}
