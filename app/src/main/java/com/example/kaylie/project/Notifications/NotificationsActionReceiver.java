package com.example.kaylie.project.Notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

/**
 * Created by temilola on 7/28/16.
 */
public class NotificationsActionReceiver extends BroadcastReceiver {

    List<ParseObject> mTaskList;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Get instance of notification manager
        NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        int notificationId = intent.getIntExtra("notification_id", 0);

        if(intent.getStringExtra("geofence_id") != null) {
            String geofenceId = intent.getStringExtra("geofence_id");

            //Queres parse objects where equal to geofenceId
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
            query.fromLocalDatastore();
            query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
            query.whereEqualTo("geofence_id", geofenceId);
            try {
                mTaskList = query.find();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Cancel the notification
            manager.cancel(geofenceId, notificationId);
        }
        else {

            Date createdAt = intent.getParcelableExtra("created_at");
            //Queres parse objects where equal to geofenceId
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
            query.fromLocalDatastore();
            query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
            query.whereEqualTo("createdAt", createdAt);
            try {
                mTaskList = query.find();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Cancel the notification
            manager.cancel(notificationId);
        }

        //Put is_completed as true and save the task
        for (int i = 0; i < mTaskList.size(); i++) {
            ParseObject task = mTaskList.get(i);
            task.put("is_completed", true);

            task.pinInBackground();
            task.saveEventually();
        }

    }

}
