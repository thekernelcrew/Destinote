package com.example.kaylie.project.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kaylie.project.Adapters.TimeFragmentAdapter;
import com.example.kaylie.project.Models.Task;
import com.example.kaylie.project.R;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by temilola on 7/8/16.
 */
public class TimeOfDayFragment extends Fragment {

    private int year;
    private int month;
    private int day;
    TimeFragmentAdapter adapter;
    ArrayList<Task> tasks;
    Date date;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Construct the data source
        tasks = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new TimeFragmentAdapter(getContext(), tasks);
        View view = inflater.inflate(R.layout.time_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //set tvDate on top based on the date selected by user from calendar fragment
        TextView tvDate= (TextView)view.findViewById(R.id.tvDateTime);
         //setUpTime(view);
        ListView lvChecklist= (ListView) view.findViewById(R.id.lvChecklist);
    }

    //Had to statically add times
    //In future, could figure out how to make this work dynamically

    public void setUpTime(View view) {

//        LinearLayout linearLayout= (LinearLayout) view.findViewById(R.id.timeLayout);
//        TextView tvTime= (TextView) view.findViewById(R.id.tvEndDate);
//        tvTime.setText("12 AM");
//        TextView tvTime1= (TextView) linearLayout.findViewById(R.id.linearList2).findViewById(R.id.tvEndDate);
//        tvTime1.setText("1 AM");
//        TextView tvTime2= (TextView) linearLayout.findViewById(R.id.linearList3).findViewById(R.id.tvEndDate);
//        tvTime2.setText("2 AM");
//        TextView tvTime3= (TextView) linearLayout.findViewById(R.id.linearList4).findViewById(R.id.tvEndDate);
//        tvTime3.setText("3 AM");
//        TextView tvTime4= (TextView) linearLayout.findViewById(R.id.linearList5).findViewById(R.id.tvEndDate);
//        tvTime4.setText("4 AM");
//        TextView tvTime5= (TextView) linearLayout.findViewById(R.id.linearList6).findViewById(R.id.tvEndDate);
//        tvTime5.setText("5 AM");
//        TextView tvTime6= (TextView) linearLayout.findViewById(R.id.linearList7).findViewById(R.id.tvEndDate);
//        tvTime6.setText("6 AM");
//        TextView tvTime7= (TextView) linearLayout.findViewById(R.id.linearList8).findViewById(R.id.tvEndDate);
//        tvTime7.setText("7 AM");
//        TextView tvTime8= (TextView) linearLayout.findViewById(R.id.linearList9).findViewById(R.id.tvEndDate);
//        tvTime8.setText("8 AM");
//        TextView tvTime9= (TextView) linearLayout.findViewById(R.id.linearList10).findViewById(R.id.tvEndDate);
//        tvTime9.setText("9 AM");
//        TextView tvTime10= (TextView) linearLayout.findViewById(R.id.linearList11).findViewById(R.id.tvEndDate);
//        tvTime10.setText("10 AM");
//        TextView tvTime11= (TextView) linearLayout.findViewById(R.id.linearList12).findViewById(R.id.tvEndDate);
//        tvTime11.setText("11 AM");
//        TextView tvTime12= (TextView) linearLayout.findViewById(R.id.linearList13).findViewById(R.id.tvEndDate);
//        tvTime12.setText("12 PM");
//        TextView tvTime13= (TextView) linearLayout.findViewById(R.id.linearList14).findViewById(R.id.tvEndDate);
//        tvTime13.setText("1 PM");
//        TextView tvTime14= (TextView) linearLayout.findViewById(R.id.linearList15).findViewById(R.id.tvEndDate);
//        tvTime14.setText("2 PM");
//        TextView tvTime15= (TextView) linearLayout.findViewById(R.id.linearList16).findViewById(R.id.tvEndDate);
//        tvTime15.setText("3 PM");
//        TextView tvTime16= (TextView) linearLayout.findViewById(R.id.linearList17).findViewById(R.id.tvEndDate);
//        tvTime16.setText("4 PM");
//        TextView tvTime17= (TextView) linearLayout.findViewById(R.id.linearList18).findViewById(R.id.tvEndDate);
//        tvTime17.setText("5 PM");
//        TextView tvTime18= (TextView) linearLayout.findViewById(R.id.linearList19).findViewById(R.id.tvEndDate);
//        tvTime18.setText("6 PM");
//        TextView tvTime19= (TextView) linearLayout.findViewById(R.id.linearList20).findViewById(R.id.tvEndDate);
//        tvTime19.setText("7 PM");
//        TextView tvTime20= (TextView) linearLayout.findViewById(R.id.linearList21).findViewById(R.id.tvEndDate);
//        tvTime20.setText("8 PM");
//        TextView tvTime21= (TextView) linearLayout.findViewById(R.id.linearList22).findViewById(R.id.tvEndDate);
//        tvTime21.setText("9 PM");
//        TextView tvTime22= (TextView) linearLayout.findViewById(R.id.linearList23).findViewById(R.id.tvEndDate);
//        tvTime22.setText("10 PM");
//        TextView tvTime23= (TextView) linearLayout.findViewById(R.id.linearList24).findViewById(R.id.tvEndDate);
//        tvTime23.setText("11 PM");
    }
}
