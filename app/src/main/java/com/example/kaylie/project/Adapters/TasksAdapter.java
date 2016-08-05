package com.example.kaylie.project.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kaylie.project.R;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by kaylie on 7/19/16.
 */
public class TasksAdapter extends ArrayAdapter<ParseObject> {


    private static class ViewHolder{
        TextView taskName;
        TextView description;
        TextView date;
        TextView location;
    }

    public TasksAdapter(Context context, List<ParseObject> tasks){
       super(context, 0, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        final ParseObject task = getItem(position);

        final ViewHolder viewHolder;

        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_task_layout, parent, false);
            viewHolder.taskName = (TextView) convertView.findViewById(R.id.tvTaskName);
            viewHolder.description = (TextView) convertView.findViewById(R.id.tvDescription);
            viewHolder.date = (TextView)convertView.findViewById(R.id.tvStartDate);
            viewHolder.location = (TextView)convertView.findViewById(R.id.tvLocation);
            convertView.setTag(viewHolder);

        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        //Create CXustome Typeface
        Typeface customSourceSans = Typeface.createFromAsset(getContext().getAssets(),  "fonts/SourceSansPro-Light.otf");

        //Set TypeFace to TextViews
        viewHolder.taskName.setTypeface(customSourceSans);
        viewHolder.date.setTypeface(customSourceSans);
        viewHolder.description.setTypeface(customSourceSans);
        viewHolder.location.setTypeface(customSourceSans);

        if(task.getString("name")!= null) {
            viewHolder.taskName.setText(task.getString("name"));
        }else{
            viewHolder.taskName.setText("");
        }

        if(task.getString("description")!= null) {
            viewHolder.description.setText(task.getString("description"));
        }else{
            viewHolder.description.setText("");
        }
        if(task.getString("place_name")!= null) {
            viewHolder.location.setText(task.getString("place_name"));
        }else{
            viewHolder.location.setText("");
        }

        String taskDate = task.getString("start_date");

        if(taskDate != null && !taskDate.equals("no_date") && !taskDate.equals("all_day")) {

            viewHolder.date.setText(taskDate);

        }else{

            viewHolder.date.setText("");

        }

        return convertView;
    }
}
