package com.example.kaylie.project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kaylie.project.Models.Task;
import com.example.kaylie.project.R;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by temilola on 7/11/16.
 * Adapter for listview in timeFragment
 */
public class TimeListViewAdapter extends ArrayAdapter<Task> {

    TextView tvItem;
    Date date;

    public TimeListViewAdapter(Context context, ArrayList<Task> tasks, Date date){
        super(context, android.R.layout.simple_list_item_1, tasks);
        this.date= date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //Get the task item from ArrayList
        Task task= getItem(position);
        //check if existing view is being re-used, otherwise, inflate the view
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
          tvItem = (TextView)convertView.findViewById(R.id.tvListItem);
        }

        //Get hours from date
        int hours= date.getHours();

        //check if task has a date filled and if task date corresponds with calendar date
//        for(int i=0; i<24; i++) {
//            if (task.getStartDate() != null && task.getStartDate().getDay() == date.getDay()) {
//                if (String.valueOf(i).equals(String.valueOf(hours))) {
//                    tvItem.setText(task.getDescription());
//                }
//            } else {
//                tvItem.setText("No Events at This Time");
//            }
//        }

        return convertView;
    }
}
