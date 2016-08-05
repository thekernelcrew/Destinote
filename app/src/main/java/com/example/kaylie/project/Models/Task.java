package com.example.kaylie.project.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by kaylie on 7/6/16.
 */

@Parcel(analyze = {ParseUser.class})
@ParseClassName("Task")
public class Task extends ParseObject {

    /// fields are public to implement Parceler
    ParseUser owner; // owner of the task (creator of the task)
    String fbUser;
    String name; // task content
    String description;

    String latitude;
    String longitude;

    String geofenceId;

    String startDate;
    String endDate;

    int repeatNum;
    List<Integer> repeatDays;

    String notifSentDate;
    String notifSentTime;

    boolean repeating; // whether task is repeating

    int color;

    String startTime;
    String endTime;

    String listName;

    String placeName;

    boolean isCompleted;

    public Task() {
    }

    public Task(String name, String description,
                String latitude, String longitude, String placeName,
                String startDate, String endDate, String startTime, String endTime,
                boolean repeating, List<Integer> repeatDays, int repeatNum){
        super();
        setName(name);
        setDescription(description);

        setLatitude(latitude);
        setLongitude(longitude);
        setPlaceName(placeName);

        setStartDate(startDate);
        setEndDate(endDate);
        setStartTime(startTime);
        setEndTime(endTime);

        setRepeating(repeating);
        setRepeatDays(repeatDays);
        setRepeatNum(repeatNum);
    }

    /*
     * Setters and getters for owner of task (Parse user)
     */

    public ParseUser getOwner() {
        return getParseUser("owner");
    }

    public void setOwner(ParseUser user) {
        this.owner = user;
        put("owner",user);
    }


    public String getFbUser() {
        return fbUser;
    }

    public void setFbUser(String fbUser) {
        this.fbUser = fbUser;
        put("fb_user", fbUser);
    }


    /*
     * Setters and getters for task description/name
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        put("name", name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        put("description", description);
    }


    /*
     * Setters and getters for geofence/location related variables
     */

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
        put("latitude", latitude);
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
        put("longitude", longitude);
    }

    public String getGeofenceId(){
        return geofenceId;
    }
    public void setGeofenceId(String geofenceId){
        this.geofenceId= geofenceId;
        put("geofence_id", geofenceId);
    }

    /*
     * Setters and getters for date related variables (start date, repeating dates)
     */

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
        put("start_date", startDate);
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
        put("repeating", repeating);
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
        put("end_date", endDate);
    }

    public int getRepeatNum() {
        return repeatNum;
    }

    public void setRepeatNum(int repeatNum) {
        this.repeatNum = repeatNum;
        put("repeat_num", repeatNum);
    }

    public List<Integer> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(List<Integer> repeatDays) {
        this.repeatDays = repeatDays;
        put("repeat_days", repeatDays);
    }

    public String getNotifSentDate(){
        return notifSentDate;
    }

    public void setNotifSentDate(String notifSentDate) {
        this.notifSentDate= notifSentDate;
        put("notif_sent_date", notifSentDate);
    }

    public void setNotifSentTime(String notifSentTime) {
        this.notifSentTime= notifSentTime;
        put("notif_sent_time", notifSentTime);
    }

    public String getNotifSentTime(){
        return notifSentTime;
    }


    /*
     * Setters and getters for time related variables (time range, if applicable)
     */

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
        put("start_time", startTime);
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
        put("end_time", endTime);
    }

    /*
     * Setters and getters for name of parent list that task is in
     */

    public String getListName() {
        return getString("listName");
    }

    public void setListName(String listName) {
        this.listName = listName;
        put("listName", listName);
    }

    /*
     * Setters and getters for the associated number of the color of the event
     */

    public int getColor() {
        return getInt("color");
    }

    public void setColor(int color) {
        this.color = color;
        put("color", color);
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
        put("place_name", placeName);
    }

    /*
     * Setters and getters for task completion
     */
    public boolean getCompleted(){
        return getBoolean("is_completed");
    }

    public void setCompleted(boolean isCompleted){
        this.isCompleted= isCompleted;
        put("is_completed", isCompleted);
    }

    public static ParseQuery<Task>getQuery(){
        return ParseQuery.getQuery(Task.class);
    }

}
