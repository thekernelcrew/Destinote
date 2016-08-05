package com.example.kaylie.project.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

/**
 * Created by temilola on 7/25/16.
 */
public class BootReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "In Boot Receiver", Toast.LENGTH_LONG).show();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent broadcastIntent = new Intent("com.example.kaylie.project.Geofence.GeofenceTransitionsReceiver");
            context.startService(broadcastIntent);
            Toast.makeText(context, "Sent intent to Geofence receiver", Toast.LENGTH_LONG).show();
            PendingIntent pi = PendingIntent.getService(context, 0, broadcastIntent, 0);
            am.cancel(pi);
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime()
                            + 30*1000,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
            Toast.makeText(context, "Set up alarm from Boot reciever", Toast.LENGTH_LONG).show();
        }
    }

}
