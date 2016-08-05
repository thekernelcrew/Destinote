package com.example.kaylie.project.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

/**
 * Alarm Receiver for Tasks Without Locations
 */
public class CustomAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARM_RECEIVER", "received");

        Date createdAt = (Date) intent.getSerializableExtra("created_at");

//        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//        if (alarmUri == null) {
//            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        }
//        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
//        ringtone.play();

       Intent i = new Intent(context, CustomAlarmService.class);
        i.putExtra("created_at", createdAt);
        context.startService(i);
    }


}
