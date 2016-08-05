package com.example.kaylie.project.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.kaylie.project.DisplayHomeActivity;
import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

/**
 * Resets Alarm For task without notifications if device shuts down
 */
public class CustomBootReceiver extends BroadcastReceiver {

    List<ParseObject> mTaskList;

    @Override
    public void onReceive(Context context, Intent intent) {

        //alarm interval - 30 minutes
        int interval = 1000 * 60 * 30;

        String startDate = null;
        String endDate = null;
        String startTime = null;
        String endTime = null;
        Date beginDate = null;
        Date finishDate = null;
        int[] beginTime = new int[]{0,0};
        int[] finishTime= new int[]{0,0};

        Date currentDate = new Date();


        ParseQuery<ParseObject> query= ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        query.whereEqualTo("is_completed", false);
        query.whereDoesNotExist("geofence_id");
        try {
            mTaskList = query.find();
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Intent alarmIntent = new Intent(context, CustomAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (int i = 0; i < mTaskList.size(); i++){
            ParseObject task = mTaskList.get(i);

            //get start date
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

            if (startDate != null && startTime != null) {
                beginDate = new Date(beginDate.getYear(), beginDate.getMonth(), beginDate.getDate(), beginTime[0], beginTime[1]);
                finishDate = new Date(finishDate.getYear(), finishDate.getMonth(), finishDate.getDate(), finishTime[0], finishTime[1]);
            }
            else if (startTime != null) {
                beginDate = new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate(), beginTime[0], beginTime[1]);
                finishDate = new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate(), finishTime[0], finishTime[1]);
            }

            //Trigger alarm if currentDate is not after to finished date
            if((startDate != null || startTime != null)){
                if (finishDate.after(currentDate)) {
                    manager.setRepeating(AlarmManager.RTC_WAKEUP, beginDate.getTime(), interval, pendingIntent);
                }
            }

            Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show();
            }

        }

    }

    /**
     * converts string date into date object
     * @param inputDate String input of date
     * @return date object
     */
    public Date convertDate(String inputDate){

        android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("MMMM dd, yyyy");
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
    public int[] convertTime(String inputTime) {

        int minutes = 0;
        int hours = 0;

        String amPmString = null;
        int length = inputTime.length();
        if (length == 8) {
            minutes = Integer.valueOf(inputTime.substring(3, 5));
            amPmString = inputTime.substring(6);
        } else {
            minutes = Integer.valueOf(inputTime.substring(2, 4));
            amPmString = inputTime.substring(5);
        }

        switch (amPmString) {
            case "AM":
                hours = Integer.valueOf(inputTime.substring(0, inputTime.indexOf(":")));
                if (hours == 12) {
                    hours = 0;
                }
                break;
            case "PM":
                hours = 12 + Integer.valueOf(inputTime.substring(0, inputTime.indexOf(":")));
                if (hours == 24) {
                    hours = 12;
                }
                break;
        }
        return new int[]{hours, minutes};
    }
}
