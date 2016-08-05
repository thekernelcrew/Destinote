package com.example.kaylie.project.Fragments;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.kaylie.project.R;
import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by temilola on 7/8/16.
 */
public class TimeFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener{

    Date mDate;
    ArrayList<String> mTimeList;

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private WeekView mWeekView;

    /*
     * Empty Time fragment constructor
     */
    public TimeFragment(){

    }

    /*
     * Creates a new instance of the fragment displaying events for a certain date
     */
    public static TimeFragment newInstance(Date date) {
        TimeFragment fragmentTime = new TimeFragment();
        Bundle args = new Bundle();
        args.putSerializable("date", date);
        fragmentTime.setArguments(args);
        return fragmentTime;
    }

    /*
     * Empty method for on creation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     * Inflates the view for the fragment based on layout XML
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.time_lib_fragment, container, false);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) view.findViewById(R.id.weekView);

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));

        // Construct the data source
        mDate= new Date();

        // Fetch today's date from bundle
        mDate = (Date)getArguments().getSerializable("date");
        setDate(mDate);

        return view;
    }

    /*
     * Called from display home activity and sets the date to display based on the calendar
     */
    public void setDate(Date date) {

        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);
        mWeekView.goToDate(calDate);
    }

    /*
     * Parses the hour from the stored time string
     */
    public static int getHour(String time){

        if(Integer.parseInt(time.substring(0,2)) == 12 ){
            if(time.substring(6).equals("AM")){
                return 24;
            }else{
                return 12;
            }
        }else if(time.substring(6).equals("AM")){
            return Integer.parseInt(time.substring(0, 2));

        }else {
            return Integer.parseInt(time.substring(0, 2)) + 12;
        }
    }

    /*
     * Parses the minutes from the stored time string
     */
    public static int getMinute(String time){

        return Integer.parseInt(time.substring(3,5));
    }

    /*
     * Parses the month from the stored date string
     */
    public static int getMonth(String date){
        String monthString = date.substring(0, 3);
        Date convertDate = null;
        try{
            convertDate = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(monthString);
        } catch (java.text.ParseException e1) {
            e1.printStackTrace();
        }
        return convertDate.getMonth() + 1;

    }

    /*
     * Parses the date from the stored date string
     */
    public static int getYear(String date){
        return Integer.parseInt(date.substring(date.length() - 4, date.length()));
    }

    /*
     * Getter for the current week view
     */
    public WeekView getWeekView() {
        return mWeekView;
    }

    /*
     * Parses the day of the month from the stored date string
     */
    public int getDayOfMonth(String date){

        return Integer.parseInt(date.substring(date.length() - 8, date.length() - 6));
    }

    /*
     * Default onDestroy method
     */
    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    /*
     * Might do tooltips for each event
     */
    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {

    }


    /*
     * Chooses a random color for the event
     */
    private int randomEventColor(){

        Random rand = new Random();
        int randInt = rand.nextInt(9);
        switch(randInt){
            case 0:
                return getResources().getColor(R.color.event_red);
            case 1:
                return getResources().getColor(R.color.event_pink);
            case 2:
                return getResources().getColor(R.color.event_blue);
            case 3:
                return getResources().getColor(R.color.event_purple);
            case 4:
                return getResources().getColor(R.color.event_cyan);
            case 5:
                return getResources().getColor(R.color.event_gray);
            case 6:
                return getResources().getColor(R.color.event_green);
            case 7:
                return getResources().getColor(R.color.event_yellow);
            case 8:
                return getResources().getColor(R.color.event_orange);
            case 9:
                return getResources().getColor(R.color.rand1);
            case 10:
                return getResources().getColor(R.color.rand2);
            case 11:
                return getResources().getColor(R.color.rand3);
            case 12:
                return getResources().getColor(R.color.rand4);
            case 13:
                return getResources().getColor(R.color.rand5);
            case 14:
                return getResources().getColor(R.color.rand6);
            case 15:
                return getResources().getColor(R.color.rand7);

        }

        return randInt;
    }

    /*
     * Populates the calendar with events from tasks list
     */
    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with some events.
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        List<ParseObject> tasks = null;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereNotEqualTo("start_date", "no_date");
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());

        try {
            tasks = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < tasks.size(); i++) {

            ParseObject currTask = tasks.get(i);


            // If the times are repeating
            if (currTask.getBoolean("repeating") && currTask.getInt("repeat_num") != -1) {

                // And the time range is within the month
                if (getMonth(currTask.getString("start_date")) == newMonth && getYear(currTask.getString("start_date")) == newYear &&
                        getMonth(currTask.getString("end_date")) == newMonth && getYear(currTask.getString("end_date")) == newYear) {

                    String strStartTime = currTask.getString("start_time");
                    String strEndTime = currTask.getString("end_time");
                    int firstDay = getDayOfMonth(currTask.getString("start_date"));
                    int currMonth = getMonth(currTask.getString("start_date"));
                    int currYear = getYear(currTask.getString("start_date"));
                    int numRepeats = currTask.getInt("repeat_num");

                    JSONArray repeatDaysJSON = currTask.getJSONArray("repeat_days");

                    int repeatFirstDay;
                    try {
                        repeatFirstDay = repeatDaysJSON.getInt(0);

                        // For each day that is repeated and for the number of repeats
                        for (int j = 0; j < repeatDaysJSON.length(); j++) {
                            for (int k = 0; k < numRepeats; k++) {

                                int repeatDay;

                                try {

                                    Calendar startTime = Calendar.getInstance();
                                    Calendar endTime = Calendar.getInstance();


                                    repeatDay = repeatDaysJSON.getInt(j);
                                    repeatDay = repeatDay - repeatFirstDay;

                                    if (strStartTime.equals("all_day")) {

                                        startTime.set(Calendar.HOUR_OF_DAY, 0);
                                        startTime.set(Calendar.MINUTE, 1);
                                        endTime.set(Calendar.HOUR_OF_DAY, 23);
                                        endTime.set(Calendar.MINUTE, 59);
                                    } else {
                                        startTime.set(Calendar.HOUR_OF_DAY, getHour(strStartTime));
                                        startTime.set(Calendar.MINUTE, getMinute(strStartTime));
                                        endTime.set(Calendar.HOUR_OF_DAY, getHour(strEndTime));
                                        endTime.set(Calendar.MINUTE, getMinute(strEndTime));
                                    }

                                    // Calculate the day that the event will show up on
                                    startTime.set(Calendar.DAY_OF_MONTH, firstDay + repeatDay + 7 * k);
                                    startTime.set(Calendar.MONTH, currMonth - 1);
                                    startTime.set(Calendar.YEAR, currYear);
                                    endTime.set(Calendar.DAY_OF_MONTH, firstDay + repeatDay + 7 * k);
                                    endTime.set(Calendar.MONTH, currMonth - 1);
                                    endTime.set(Calendar.YEAR, currYear);


                                    WeekViewEvent event = new WeekViewEvent(1, currTask.getString("name"), startTime, endTime);
                                    event.setColor(randomEventColor());
                                    events.add(event);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                // If the date is not a repeating date
            } else if (getMonth(currTask.getString("start_date")) == newMonth && getYear(currTask.getString("start_date")) == newYear) {

                String time = currTask.getString("start_time");
                String date = currTask.getString("start_date");

                Calendar startTime = Calendar.getInstance();

                if (time.equals("all_day")) {

                    startTime.set(Calendar.HOUR_OF_DAY, 0);
                    startTime.set(Calendar.MINUTE, 1);

                } else {

                    startTime.set(Calendar.HOUR_OF_DAY, getHour(time));
                    startTime.set(Calendar.MINUTE, getMinute(time));
                }

                startTime.set(Calendar.DAY_OF_MONTH, getDayOfMonth(date));
                startTime.set(Calendar.MONTH, getMonth(date) - 1);
                startTime.set(Calendar.YEAR, getYear(date));


                time = currTask.getString("end_time");
                date = currTask.getString("end_date");
                Calendar endTime = Calendar.getInstance();

                if (time.equals("all_day")) {

                    endTime.set(Calendar.HOUR_OF_DAY, 23);
                    endTime.set(Calendar.MINUTE, 59);

                } else {

                    endTime.set(Calendar.HOUR_OF_DAY, getHour(time));
                    endTime.set(Calendar.MINUTE, getMinute(time));

                }

                endTime.set(Calendar.DAY_OF_MONTH, getDayOfMonth(date));
                endTime.set(Calendar.MONTH, getMonth(date) - 1);
                endTime.set(Calendar.YEAR, getYear(date));
                WeekViewEvent event = new WeekViewEvent(1, currTask.getString("name"), startTime, endTime);


                event.setColor(randomEventColor());
                events.add(event);
            }
        }


        return events;
      }
}
