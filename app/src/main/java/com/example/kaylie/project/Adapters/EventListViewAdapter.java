package com.example.kaylie.project.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kaylie.project.Fragments.TimeFragment;
import com.example.kaylie.project.Models.Task;
import com.example.kaylie.project.R;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by temilola on 7/11/16.
 * Adapter for ListView in TimeOfDay Fragment
 */
public class EventListViewAdapter extends ArrayAdapter<String> {
    CheckBox checkBox;
    ListView lvTasks;
    //TextView tvTime;
    Date date;
    ArrayList<String> timeList;
    List<ParseObject> tasks;

    public EventListViewAdapter(Context context, ArrayList<String> list, Date date, List<ParseObject> tasks){
        super(context, R.layout.item_time, list);
        this.date= date;
        this.timeList= list;
        this.tasks = tasks;
    }

    public void addItem(final String item) {
        timeList.add(item);
        notifyDataSetChanged();
    }

    @Override

    public int getViewTypeCount() {

        return getCount();
    }
    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getCount() {
        return timeList.size();
    }

    @Override
    public String getItem(int position) {
        return timeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void add(String object) {
        super.add(object);
    }

    class RecipeCompare implements Comparator<ParseObject> {

        @Override
        public int compare(ParseObject o1, ParseObject o2) {

            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //Get the task time
        //Task task= getItem(position);
        //setUpDateArray();
        ViewHolder holder = null;

        //check if existing view is being re-used, otherwise, inflate the view
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.item_time, parent, false);
            //checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            holder = new ViewHolder();
            holder.tvTime = (TextView)convertView.findViewById(R.id.tvTime);
            holder.tvTimeTask = (TextView)convertView.findViewById(R.id.tvTimeTask);
//            lvTasks=(ListView)convertView.findViewById(R.id.lvEvents);
//            final ListArrayAdapter adapter = new ListArrayAdapter(getContext(),
//                    android.R.layout.simple_list_item_1, tasks);
//            lvTasks.setAdapter(adapter);

            convertView.setTag(holder);
            //lvTasks= (ListView)convertView.findViewById(R.id.lvEvents);
            //tvTime= (TextView)convertView.findViewById(R.id.tvTime);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.tvTime.setText(timeList.get(position));
        for(int i = 0; i < tasks.size(); i++){

            String startTime = tasks.get(i).getString("start_time");
            int startTimeNum;
            if(startTime != null){
                startTimeNum = TimeFragment.getHour(startTime);
                if(startTimeNum == position){
                    holder.tvTimeTask.setText((tasks.get(i)).getString("name") + (tasks.get(i).getString("description")));
                }
            }

        }

        //TimeListViewAdapter adapter =new TimeListViewAdapter(getContext(), tasks, date);
        //lvTasks.setAdapter(adapter);
//        //Check if task date equals calendar day
//        if(task.getStartDate().getDay()== date.getDay()) {

//            checkBox.setText(task.getDescripti0on());
//        }else{
//            checkBox.setText(" ");
//        }


        return convertView;
    }

    public static class ViewHolder {
        public TextView tvTime;
        public TextView tvTimeTask;
    }

    private class ListArrayAdapter extends ArrayAdapter<Task> {

        HashMap<Task, Integer> mIdMap = new HashMap<>();

        public ListArrayAdapter(Context context, int textViewResourceId,
                                List<Task> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            Task item = getItem(position);
            return mIdMap.get(item);
        }
    }

}


