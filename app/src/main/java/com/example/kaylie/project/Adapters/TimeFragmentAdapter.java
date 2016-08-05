package com.example.kaylie.project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kaylie.project.Models.Task;

import java.util.ArrayList;

/**
 * Created by temilola on 7/11/16.
 */
public class TimeFragmentAdapter extends ArrayAdapter<Task> {


    public TimeFragmentAdapter(Context context, ArrayList<Task> tasks){
        super(context, 0, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //Get the task time
        Task task= getItem(position);
        //check if existing view is being re-used, otherwise, inflate the view
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tvItem= (TextView)convertView.findViewById(android.R.id.text1);
        //check if time for task is null. If null, set listview text, else fill the listview position corresponding to the time with the task
        if(task.getStartDate()!=null){
            tvItem.setText(task.getDescription());
        }
        else{
            tvItem.setText("No Events at this Time");
        }

        return convertView;
    }

}
