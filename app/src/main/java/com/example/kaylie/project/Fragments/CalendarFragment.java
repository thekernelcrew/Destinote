package com.example.kaylie.project.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.Models.Task;
import com.example.kaylie.project.R;
import com.parse.ParseObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 * Created by temilola on 7/7/16.
 */
public class CalendarFragment extends Fragment{

    ArrayList<Task> tasks;
    Date mDate;
    OnDayClickListener mCallback;
    List<ParseObject> mTaskList;
    List<ParseObject> mRepeatedTaskList;

    com.example.kaylie.project.Models.CalendarView cv;

    // Defines the listener interface with a method
    //    passing back date as result.
    public interface OnDayClickListener {
         void onDateClick(Date date);
    }


    public static CalendarFragment newInstance(Date date) {
        CalendarFragment frag = new CalendarFragment();
        Bundle args = new Bundle();
        args.putSerializable("date", date);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnDayClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDayClickListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Construct the data source
        tasks = new ArrayList<>();
        mDate= new Date();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        cv = ((com.example.kaylie.project.Models.CalendarView) view.findViewById(R.id.calendar_view));

        HashSet<Date> events = DisplayHomeActivity.updateEvents();
        cv.updateCalendar(events);

        mDate = (Date) getArguments().getSerializable("date");

        // assign event handler
        cv.setEventHandler(new com.example.kaylie.project.Models.CalendarView.EventHandler()
        {
            @Override
            public void onDayLongPress(Date date)
            {
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
            }

            @Override
            public void onDateClick(Date date){
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                mDate = date;
                mCallback.onDateClick(mDate);
            }
        });
    }
}
