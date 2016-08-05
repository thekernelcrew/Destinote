package com.example.kaylie.project.Geofence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.Notifications.NotificationsActionReceiver;
import com.example.kaylie.project.R;
import com.facebook.Profile;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by temilola on 7/11/16.
 */
public class GeofenceTransitionsReceiver extends BroadcastReceiver {

    List<ParseObject> mTaskList;

    Context context;

    Intent broadcastIntent = new Intent();

    private final String TAG= "Geofence Error";
    private static String EXTRA_GEOFENCE_ID= "GeofenceId";
    private static String EXTRA_GEOFENCE_TRANSITION_TYPE= "TransitionType";

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        //If geofence event has an error, handle error, otherwise handle intent
        if (GeofencingEvent.fromIntent(intent).hasError()) {
            handleError(intent);
        } else {
            onHandleIntent(intent);
        }
    }

    private void handleError(Intent intent){

        // Get the error code
        int errorCode = GeofencingEvent.fromIntent(intent).getErrorCode();

        // Get the error message
        String errorMessage = GeofenceStatusCodes.getStatusCodeString(errorCode);
        // Log the error
        Log.e(TAG, "GeofenceReceiver: " + errorMessage);

        // Set the action and error message for the broadcast intent
        broadcastIntent
                .setAction(GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE))
                .putExtra("error", errorCode);

        // Broadcast the error *locally* to other components in this app
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                broadcastIntent);
    }

    /**
     *default constructor
     */
    public GeofenceTransitionsReceiver() {
        super();
    }



    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "LocationServicesError: "+ errorCode);
            return;
        }

        // Get the transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT||geofenceTransition==Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String[] geofenceIds = new String[triggeringGeofences.size()];
            for (int i = 0; i < triggeringGeofences.size() ; i++) {
                geofenceIds[i] = triggeringGeofences.get(i).getRequestId();
            }
            onEnteredGeofence(geofenceIds);

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition);

            Log.i(EXTRA_GEOFENCE_TRANSITION_TYPE, geofenceTransitionDetails);

            // Create an Intent to broadcast to the app
            broadcastIntent
                    .setAction("Geofence")
                    .addCategory("Geofence")
                    .putExtra(EXTRA_GEOFENCE_ID, geofenceIds)
                    .putExtra(EXTRA_GEOFENCE_TRANSITION_TYPE,
                            geofenceTransitionDetails);

            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(broadcastIntent);
        } else {
            // Log the error.
            Log.e("GeofenceTrans Error", String.valueOf(geofenceTransition));
        }

    }

    /**
     * pushes notifications with task details
     * @param tag geofenceId to distinguish notifications
     * @param taskDetails contains taskDetails (name, description, date, time)
     */
    public void sendNotification(String tag, List<String> taskDetails){

        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        //Get last four strings of time
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        int notificationId = Integer.valueOf(last4Str);

        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent =
                new Intent(context, DisplayHomeActivity.class);

        notificationIntent.putExtra("resultCode", 200);
        notificationIntent.putExtra("geofence_id", tag);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(DisplayHomeActivity.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent for action: MarkCompleted
        Intent markCompletedIntent = new Intent(context, NotificationsActionReceiver.class);
        markCompletedIntent.putExtra("geofence_id", tag);
        markCompletedIntent.putExtra("notification_id", notificationId);


        //Pending intent for action: markCompleted
        PendingIntent pendingIntentMarkCompleted = PendingIntent.getBroadcast(context, 0, markCompletedIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get notification builder
        Notification.Builder builder = new Notification.Builder(context);

        // Set the notification contents
        Notification noti= new Notification.InboxStyle(builder.setSmallIcon(R.drawable.notif_big_icon)
                .setContentTitle(context.getString(R.string.location_notif_title))
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.location_notif_description))
                .addAction(R.drawable.ic_mark_complete, "Mark As Completed", pendingIntentMarkCompleted )
                .setContentIntent(notificationPendingIntent))
                .addLine(taskDetails.get(0))
                .addLine(taskDetails.get(1))
                .addLine(taskDetails.get(2))
                .addLine(taskDetails.get(3))
                .setBigContentTitle("Task Details: ")
                .build();

        // Get an instance of the Notification manager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Add auto cancel flag
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        // Issue the notification
        notificationManager.notify(tag, notificationId, noti);
    }

    /**
     * Gets geofence transition details to string for logging
     * @param transitionType type of geofence transition
     * @return string value of transition type
     */
    public String getGeofenceTransitionDetails(int transitionType) {

        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return context.getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return context.getString(R.string.geofence_transition_exited);

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return context.getString(R.string.geofence_transition_dwell);

            default:
                return context.getString(R.string.geofence_transition_unknown);
        }

    }

    /**
     * Queries parse for  task object with the geofenceId
     * @param geofenceId geofenceId of triggered geofence
     */
    public void findTaskTriggered(String geofenceId){

        ParseQuery<ParseObject> query= ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        query.whereEqualTo("geofence_id", geofenceId);
        try {
            mTaskList = query.find();
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calls sendNotification if task object returned meets certain checks
     * @param geofenceIds List of Geofence Ids triggered
     */
    public void onEnteredGeofence(String[] geofenceIds) {

        Date rightNow = new Date();
        Date currentDate = new Date(rightNow.getYear(), rightNow.getMonth(), rightNow.getDate());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

        // Outer loop over all geofenceIds
        for (String geofenceId : geofenceIds) {

            //Find the trigerred tasks
            findTaskTriggered(geofenceId);

            // Send notification and log the transition details.
            for(int i=0; i< mTaskList.size(); i++){
                ParseObject task = mTaskList.get(i);

                String taskName;
                String description;
                String placeName;
                String startDate;
                String endDate;
                String startTime;
                String endTime;

                //List of taskDetails
                List<String> taskDetails= new ArrayList<>();

                String notifDateSent;
                Date sentDate;

                String notifSentTime;
                int[] sentTime= {0,0};

                Date beginDate= null;
                Date finishDate=null;

                int[] beginTime= {0,0};
                int[] finishTime= {0,0};

                //repeat variables
                List<Integer> repeatDays;
                boolean repeatDay = false;

                //Get taskName
                taskName= task.getString("name");
                taskDetails.add(0, "Task Name: " + taskName);

                // Get description
                if(task.getString("description") != null) {
                    description = task.getString("description");
                } else {
                    description = "Remember: " + taskName;
                }
                taskDetails.add(1, "Description: " + description);

                //Get the place name
                placeName = task.getString("place_name");
                taskDetails.add(2, placeName);

                // get start date
                if (task.getString("start_date") != null && !task.get("start_date").equals("no_date")) {
                    startDate = task.getString("start_date");
                    beginDate = convertDate(startDate);
                }
                else {
                    startDate= null;
                }

                // Get end Date
                if (task.getString("end_date") != null && !task.get("start_date").equals("no_date")) {
                    endDate = task.getString("end_date");
                    finishDate = convertDate(endDate);
                }
                else {
                    endDate = null;
                }

                // Get start time
                if (task.getString("start_time") != null && !task.getString("start_time").equals("no_time")
                        && !task.getString("start_time").equals("all_day")) {
                    startTime = task.getString("start_time");
                    beginTime = convertTime(startTime);
                }
                else {
                    startTime = null;
                }

                // Get end time
                if (task.getString("end_time") != null && !task.getString("end_time").equals("no_time")
                        && !task.getString("end_time").equals("all_day")) {
                    endTime = task.getString("end_time");
                    finishTime = convertTime(endTime);
                }
                else {
                    endTime = null;
                }

                //check if it's the repeat day
                if (task.getBoolean("is_repeating")) {
                    repeatDays = task.getList("repeat_days");
                    for (int j = 0; j < repeatDays.size(); j++) {
                        if (rightNow.getDay() == repeatDays.get(i)) {
                            repeatDay = true;
                            break;
                        }
                    }
                }

                //get the notification sent day if it's been sent before
                if (task.getString("notif_sent_date") != null) {
                    notifDateSent = task.getString("notif_sent_date");
                    sentDate = convertDate(notifDateSent);
                }
                else{
                    notifDateSent= null;
                    sentDate= null;
                }

                //get the notification sent time if it's been sent before
                if (task.getString("notif_sent_time") != null) {
                    notifSentTime= task.getString("notif_sent_time");
                    sentTime= convertTime(notifSentTime);
                }
                else{
                    notifSentTime= null;
                }

                //if task has startDate and startTime, add to taskDetails
                if (startDate != null && startTime != null) {
                    taskDetails.add(3, "On: " + startDate + " " + startTime + " - " + endDate + " " + endTime);
                }
                else if (startDate != null) {
                    taskDetails.add(3, "On: " + startDate + "-" + endDate);
                }
                else if (startTime != null) {
                    taskDetails.add(3, "From: " + startTime + "-" + endTime);
                }
                else {
                    taskDetails.add(3, "No Date or Time set for this task!");
                }

                // Send Notifications for each one it gets
                if (startDate == null && startTime == null) {
                    sendNotification(geofenceId, taskDetails);
                    task.put("is_completed", true);
                }
                else if (startDate!=null && !startDate.isEmpty() && startTime == null && endDate != null) {

                    //Check if the task repeats on certain days
                    if(!task.getBoolean("is_repeating")) {
                        //check that the notification has not been sent
                        //check if current date is after or equal to startDate but not after end date
                        if ((notifDateSent == null || checkSent(sentDate, rightNow, sentTime))
                                && ((beginDate.equals(currentDate)
                                || finishDate.equals(currentDate)
                                || (currentDate.after(beginDate) && currentDate.before(finishDate))))) {

                            sendNotification(geofenceId, taskDetails);
                            task.put("notif_sent_date", dateFormat.format(rightNow));
                            task.put("notif_sent_time", setTime(rightNow));
                        }
                        if (finishDate.equals(currentDate) || finishDate.before(currentDate)) {
                            task.put("is_completed", true);
                        }


                    }
                    else {

                        //check if current date is after or equal to startDate but not after end date and on a repeat day
                        if ((notifDateSent == null || checkSent(sentDate, rightNow, sentTime))
                                && (repeatDay
                                && (beginDate.equals(currentDate)
                                || finishDate.equals(currentDate)
                                || (currentDate.after(beginDate) && currentDate.before(finishDate))))) {

                            sendNotification(geofenceId, taskDetails);
                            task.put("notif_sent_date", dateFormat.format(rightNow));
                            task.put("notif_sent_time", setTime(rightNow));
                        }

                        if (finishDate.equals(currentDate) || finishDate.before(currentDate)) {
                            task.put("is_completed", true);
                        }
                    }
                }
                else if (startTime == null) {

                    if (task.getString("start_time").equals("all_day") && (notifDateSent == null || checkSent(sentDate, rightNow, sentTime))) {

                        sendNotification(geofenceId, taskDetails);
                        task.put("notif_sent_date", dateFormat.format(rightNow));
                        task.put("notif_sent_time", setTime(rightNow));
                    }
                    if(rightNow.getHours() == 0 && rightNow.getMinutes() == 0){
                        task.put("is_completed", true);
                    }
                }
                else if (startDate == null) {

                    Date startTimeDate = new Date(rightNow.getYear(), rightNow.getMonth(), rightNow.getDate(), beginTime[0], beginTime[1]);
                    Date endTimeDate = new Date(rightNow.getYear(), rightNow.getMonth(), rightNow.getDate(), finishTime[0], finishTime[1]);
                    Date compare = new Date(rightNow.getYear(), rightNow.getMonth(), rightNow.getDate(), rightNow.getHours(), rightNow.getMinutes());

                    if ((notifDateSent == null || checkSent(sentDate, rightNow, sentTime)
                            && (startTimeDate.equals(compare)
                            || endTimeDate.equals(compare)
                            || (compare.after(startTimeDate) && compare.before(endTimeDate))))) {

                        sendNotification(geofenceId, taskDetails);
                        task.put("notif_sent_date", dateFormat.format(rightNow));
                        task.put("notif_sent_time", setTime(rightNow));
                    }
                    if(endTimeDate.equals(compare) || endTimeDate.before(compare)){
                        task.put("is_completed", true);
                    }
                }
                else if (endDate != null) {

                    Date startTimeDate = new Date(beginDate.getYear(), beginDate.getMonth(), beginDate.getDate(), beginTime[0], beginTime[1]);
                    Date endTimeDate = new Date(finishDate.getYear(), finishDate.getMonth(), finishDate.getDate(), finishTime[0], finishTime[1]);
                    Date compare = new Date(rightNow.getYear(), rightNow.getMonth(), rightNow.getDate(), rightNow.getHours(), rightNow.getMinutes());

                    if(!task.getBoolean("is_repeating")) {
                        if ((notifDateSent == null || checkSent(sentDate, rightNow, sentTime))
                                && ((startTimeDate.equals(compare)
                                || endTimeDate.equals(compare)
                                || (compare.after(startTimeDate) && compare.before(endTimeDate))))) {

                            sendNotification(geofenceId, taskDetails);
                            task.put("notif_sent_date", dateFormat.format(rightNow));
                            task.put("notif_sent_time", setTime(rightNow));
                        }
                        if(endTimeDate.equals(compare) || endTimeDate.before(compare)){
                            task.put("is_completed", true);
                        }
                    }
                    else {
                        if ((notifDateSent == null || checkSent(sentDate, rightNow, sentTime))
                                && (repeatDay
                                && (startTimeDate.equals(compare)
                                || endTimeDate.equals(compare)
                                || (compare.after(startTimeDate) && compare.before(endTimeDate))))) {

                            sendNotification(geofenceId, taskDetails);
                            task.put("notif_sent_date", dateFormat.format(rightNow));
                            task.put("notif_sent_time", setTime(rightNow));
                        }
                        if(endTimeDate.equals(compare) || endTimeDate.before(compare)){
                            task.put("is_completed", true);
                        }
                    }
                }

                task.pinInBackground();
                task.saveEventually();
            }

        }
    }

    /**
     * checks if notification has been sent more than an hour ago
     * @param sentDate date notification was previously sent
     * @param rightNow current date
     * @param sentTime array of time notification was previously sent
     * @return true if sent  more than an hour ago
     */
    public boolean checkSent(Date sentDate, Date rightNow, int[] sentTime){

        boolean isNotSent;
        int sentDateHours = sentTime[0];
        int sentDateMinutes = sentTime[1];
        int rightNowHours= rightNow.getHours() ;
        int rightNowMinutes = rightNow.getMinutes();
        Date compareDate= new Date(rightNow.getYear(), rightNow.getMonth(), rightNow.getDate(), 0, 0);

        //Check if notif was sent on the same day
        if(sentDate.equals(compareDate)){
            //check if notification was sent at least an hour ago
            isNotSent = (((rightNowHours * 60) + rightNowMinutes) - ((sentDateHours * 60) + sentDateMinutes) >= 60);
        }
        else {
            isNotSent = ((((rightNowHours + 24) * 60) + rightNowMinutes) - ((sentDateHours * 60) + sentDateMinutes) >= 60);
        }

        return isNotSent;

    }

    /**
     * converts string date into date object
     * @param inputDate String input of date
     * @return date object
     */
    public Date convertDate(String inputDate){

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        Date date = null;
        //Converts date format to date object
        try {
            date = dateFormat.parse(inputDate);
            date= new Date(date.getYear(), date.getMonth(), date.getDate()+1);
        }
        catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    /**
     * converts string time into an integer array
     * @param inputTime time string
     * @return integer array of hours and minutes
     */
    public int[] convertTime(String inputTime){

        int minutes = 0;
        int hours = 0;

        String amPmString= null;
        int length= inputTime.length();
        if (length==8) {
            minutes = Integer.valueOf(inputTime.substring(3, 5));
            amPmString = inputTime.substring(6);
        }
        else {
            minutes = Integer.valueOf(inputTime.substring(2, 4));
            amPmString = inputTime.substring(5);
        }

        switch(amPmString) {
            case "AM":
                hours= Integer.valueOf(inputTime.substring(0, inputTime.indexOf(":")));
                if(hours==12){
                    hours= 0;
                }
                break;
            case "PM":
                hours= 12 + Integer.valueOf(inputTime.substring(0, inputTime.indexOf(":")));
                if(hours==24){
                    hours= 12;
                }
                break;
        }
        return new int[]{hours, minutes};
    }

    /**
     * Sets notification sent time
     * @param rightNow current date
     * @return time string
     */
    public String setTime(Date rightNow){
        int hourOfDay= rightNow.getHours();
        int minute= rightNow.getMinutes();
        String timeSuffix;
        String hourStr;
        String time;

        if (hourOfDay > 12 ) {
            hourOfDay = hourOfDay % 12;
            timeSuffix = "PM";
        } else {
            timeSuffix = "AM";
        }

        if(hourOfDay < 10){
            hourStr = "0" + hourOfDay;
        }else{
            hourStr = "" + hourOfDay;
        }

        if (minute < 10) {
            time = hourStr + ":0" + minute + " " + timeSuffix;
        } else {
            time = hourStr + ":" + minute + " " + timeSuffix;

        }

        return time;
    }

}
