package com.example.kaylie.project.Notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.support.v4.app.TaskStackBuilder;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.R;
import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Alarm Service For Tasks Without Locations
 */
public class CustomAlarmService extends IntentService {

    List<ParseObject> mTaskList;

    public CustomAlarmService(){
        super("CustomAlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Date createdAt= (Date) intent.getSerializableExtra("created_at");
        onTaskTriggered(createdAt);
    }

    /**
     * pushes notifications with task details
     * @param taskDetails contains taskDetails (name, description, date, time)
     * @param createdAt task createdAt date
     */
    public void sendNotification(List<String> taskDetails, Date createdAt){

        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        //Get last four strings of time
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        int notificationId = Integer.valueOf(last4Str);

        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent =
                new Intent(this, DisplayHomeActivity.class);

        notificationIntent.putExtra("resultCode", 700);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(DisplayHomeActivity.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent for action: MarkCompleted
        Intent markCompletedIntent = new Intent(this, NotificationsActionReceiver.class);
        markCompletedIntent.putExtra("notification_id", notificationId);
        markCompletedIntent.putExtra("created_at", createdAt);

        //Pending intent for action: markCompleted
        PendingIntent pendingIntentMarkCompleted = PendingIntent.getBroadcast(this, 0, markCompletedIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get notification builder
        Notification.Builder builder = new Notification.Builder(this);

        // Set the notification contents
        Notification noti= new Notification.InboxStyle(builder.setSmallIcon(R.drawable.notif_big_icon)
                .setContentTitle(this.getString(R.string.location_notif_title))
                .setAutoCancel(true)
                .setContentText(this.getString(R.string.time_notif_description))
                .addAction(R.drawable.ic_mark_complete, "Mark As Completed", pendingIntentMarkCompleted )
                .setContentIntent(notificationPendingIntent))
                .addLine(taskDetails.get(0))
                .addLine(taskDetails.get(1))
                .addLine(taskDetails.get(2))
                .setBigContentTitle("Task Details: ")
                .build();

        // Get an instance of the Notification manager
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //Add auto cancel flag
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        // Issue the notification
        notificationManager.notify(notificationId, noti);
    }

    /**
     * Queries parse for  task object with the createdAt field
     * @param createdAt task Object createdAt time
     */
    public void onTaskTriggered (Date createdAt){

        List<String> taskDetails = new ArrayList<>();
        String taskName;
        String description;
        String startDate;
        String endDate;
        String startTime;
        String endTime;

        String notifDateSent;
        Date sentDate;

        String notifSentTime;
        int[] sentTime= {0,0};

        Date rightNow = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

        ParseQuery<ParseObject> query= ParseQuery.getQuery("Task");
        query.fromLocalDatastore();

        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        query.whereEqualTo("createdAt", createdAt);
        try {
            mTaskList = query.find();
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

        for (int i =0; i < mTaskList.size(); i++) {

            ParseObject task = mTaskList.get(i);
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

            if (task.getString("start_date") != null && !task.get("start_date").equals("no_date")) {
                startDate = task.getString("start_date");
            }
            else {
                startDate= null;
            }

            // Get end Date
            if (task.getString("end_date") != null && !task.get("start_date").equals("no_date")) {
                endDate = task.getString("end_date");
            }
            else {
                endDate = null;
            }

            // Get start time
            if (task.getString("start_time") != null && !task.getString("start_time").equals("no_time")
                    && !task.getString("start_time").equals("all_day")) {
                startTime = task.getString("start_time");
            }
            else {
                startTime = null;
            }

            // Get end time
            if (task.getString("end_time") != null && !task.getString("end_time").equals("no_time")
                    && !task.getString("end_time").equals("all_day")) {
                endTime = task.getString("end_time");
            }
            else {
                endTime = null;
            }

            //if task has startDate and startTime, add to taskDetails
            if (startDate != null && startTime != null) {
                taskDetails.add(2, "On: " + startDate + " " + startTime + " - " + endDate + " " + endTime);
            }
            else if (startDate != null) {
                taskDetails.add(2, "On: " + startDate + "-" + endDate);
            }
            else if (startTime != null) {
                taskDetails.add(2, "From: " + startTime + "-" + endTime);
            }
            else {
                taskDetails.add(2, "No Date or Time set for this task!");
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

            //check if notification has been sent before
            if (notifDateSent == null || checkSent(sentDate, rightNow, sentTime)) {
                sendNotification(taskDetails, createdAt);
                task.put("notif_sent_date", dateFormat.format(rightNow));
                task.put("notif_sent_time", setTime(rightNow));
            }
        }
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
}
