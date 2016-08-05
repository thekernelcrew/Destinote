package com.example.kaylie.project.Fragments;

import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kaylie.project.Adapters.TasksAdapter;
import com.example.kaylie.project.ListUpdater;
import com.example.kaylie.project.R;
import com.parse.ParseObject;


public class TaskListFragment extends Fragment implements ListUpdater.TaskUpdateListener {

    public TasksAdapter adapter;
    AddTaskDialogFragment editTaskFragment;
    ListUpdater mListUpdater;

    public TaskListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListUpdater = ListUpdater.getInstance();
        mListUpdater.addListener(this);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        // Inflate the layout for this fragment

        adapter = new TasksAdapter(getContext(),mListUpdater.getTasks());
        final ListView listView = (ListView) view.findViewById(R.id.lvTasks);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ParseObject task = mListUpdater.getTasks().get(i);
               displayPopupWindow(view, inflater, task);
            }
        });
        return view;
    }

    private void displayPopupWindow(View anchorView, LayoutInflater inflater, final ParseObject task) {

        //get custom typeface
        Typeface customSourceSans = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Light.otf");

        final String taskName= task.getString("name");
        String description;
        String placeName;
        String dateTimeStart;
        String dateTimeEnd;
        if (task.getString("description")!= null) {
            description= task.getString("description");
        }
        else {
            description= "";
        }

        //Get Place name if it exists
        if (task.getString("place_name") != null) {
            placeName= task.getString("place_name");
        }
        else {
            placeName = "No Location";
        }

        //Get time or date or both if any exists
        if (!task.getString("start_date").equals("no_date") && !(task.getString("start_time").equals("no_time"))) {
            dateTimeStart = task.getString("start_date") + " " + task.getString("start_time");
            dateTimeEnd = task.getString("end_date") + " " +  task.getString("end_time");
        }
        else if (task.getString("start_date").equals("no_date") && !task.getString("start_time").equals("no_time")) {
            dateTimeStart = task.getString("start_time");
            dateTimeEnd = task.getString("end_time");
        }
        else if(task.getString("start_time").equals("no_time") && !task.getString("start_date").equals("no_date")) {
            dateTimeStart = task.getString("start_date");
            dateTimeEnd = task.getString("end_date");
        }
        else{
            dateTimeStart = "No start date or time set";
            dateTimeEnd = "No end date or time set";

        }

        final PopupWindow popup = new PopupWindow(getActivity());
        View layout = inflater.inflate(R.layout.tooltip_layout, null);

        //Find views
        TextView tvName= (TextView)layout.findViewById(R.id.tvName);
        TextView tvDescription= (TextView)layout.findViewById(R.id.tvDescription);
        TextView tvLocation= (TextView)layout.findViewById(R.id.tvLocation);
        TextView tvDateTimeStart= (TextView)layout.findViewById(R.id.tvDateTime);
        TextView tvDateTimeEnd = (TextView) layout.findViewById(R.id.tvDateTimeEnd);
        ImageView ivDelete= (ImageView)layout.findViewById(R.id.ivDelete);
        ImageView ivEdit= (ImageView)layout.findViewById(R.id.ivEdit);

        //set typeface to TextViews
        tvName.setTypeface(customSourceSans);
        tvDescription.setTypeface(customSourceSans);
        tvLocation.setTypeface(customSourceSans);
        tvDateTimeStart.setTypeface(customSourceSans);
        tvDateTimeEnd.setTypeface(customSourceSans);

        //Set views
        tvName.setText("Task: " + taskName);
        tvDescription.setText("Description: " + description);
        tvLocation.setText("Location: " + placeName);
        tvDateTimeStart.setText("From: " + dateTimeStart);
        tvDateTimeEnd.setText("To: " + dateTimeEnd);

        //Set Image Icons
        ivEdit.setImageResource(R.drawable.ic_edit_icon);
        ivDelete.setImageResource(R.drawable.ic_delete);


        final String objectId = task.getObjectId();

        //set image click listeners
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                editTaskFragment = AddTaskDialogFragment.newInstance("Add Task");

                Bundle bundle = new Bundle();
                bundle.putString("objectId", objectId);
                editTaskFragment.setArguments(bundle);

                editTaskFragment.show(fm, "fragment_add_task");

                Toast.makeText(getContext(), "Edit clicked", Toast.LENGTH_SHORT).show();

//                Button btnCancelEdit = (Button) fm.findFragmentByTag("fragment_edit_task").getView().findViewById(R.id.btnCancelTask);
//
//                btnCancelEdit.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        DisplayHomeActivity.cancelTask = true;
//
//                        if (editTaskFragment != null) {
//                            editTaskFragment.dismiss();
//                        }
//                    }
//                });
            }
        });

        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.unpinInBackground();
                task.deleteEventually();
                mListUpdater.removeTask(task);
                Toast.makeText(getContext(), "Deleted "+ taskName, Toast.LENGTH_SHORT ).show();
                popup.dismiss();
            }
        });

        int screen_pos[] = new int[2];
        // Get location of anchor view on screen
        anchorView.getLocationOnScreen(screen_pos);

        // Get rect for anchor view
        Rect anchor_rect = new Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                + anchorView.getWidth(), screen_pos[1] + anchorView.getHeight());

        // Call view measure to calculate how big your view should be.
        layout.measure(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT);

        int contentViewHeight = layout.getMeasuredHeight();
        int contentViewWidth = layout.getMeasuredWidth();
        int position_x = anchor_rect.centerX() - (contentViewWidth / 2);
        int position_y = anchor_rect.bottom - (anchor_rect.height() / 2);

        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        //popup.showAsDropDown(anchorView, -20, -20);
        popup.showAtLocation(anchorView, Gravity.NO_GRAVITY, position_x,
                position_y);
    }

    @Override
    public void onTaskAdded(ParseObject task) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskRemoved(ParseObject task) {
        adapter.notifyDataSetChanged();
    }
}
