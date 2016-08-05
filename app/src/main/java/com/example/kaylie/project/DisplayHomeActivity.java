package com.example.kaylie.project;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kaylie.project.Adapters.HomeFragmentPagerAdapter;
import com.example.kaylie.project.Fragments.AddTaskDialogFragment;
import com.example.kaylie.project.Fragments.CalendarFragment;
import com.example.kaylie.project.Fragments.LocationActivity;
import com.example.kaylie.project.Fragments.SetRecurringDateDialogFragment;
import com.example.kaylie.project.Fragments.TimeFragment;
import com.example.kaylie.project.Geofence.GeofenceClass;
import com.example.kaylie.project.Models.CalendarView;
import com.example.kaylie.project.Models.Task;
import com.example.kaylie.project.Notifications.CustomAlarmReceiver;
import com.example.kaylie.project.Notifications.CustomAlarmService;
import com.example.kaylie.project.Notifications.CustomBootReceiver;
import com.facebook.Profile;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static com.example.kaylie.project.Constants.EXPIRATION_TIME;
import static com.example.kaylie.project.Constants.GEOFENCE_RADIUS;


//@Runtime Permissions
public class DisplayHomeActivity extends AppCompatActivity implements
        CalendarFragment.OnDayClickListener{

    private static final String TAG = "Debug";
    public static final String TASKS = "TASKS";
    public static FragmentManager fragmentManager;

    public static final int REQUEST_CODE = 20;
    public static final int REQUEST_OK = 10;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public static boolean addTask;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private Date date;

    private Task mNewTask;
    private TimeFragment mTimeFragment;
    AddTaskDialogFragment addTaskFragment;

    GeofenceClass geofenceClass;

    public static List<ParseObject> sTaskList;
    public static List<ParseObject> sRepeatedTaskList;

    ViewPager viewPager;
    HomeFragmentPagerAdapter tabsAdapter;

    private ArrayList<Task> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_task);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        // Get access to the custom title view
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        Typeface customLato = Typeface.createFromAsset(getAssets(),  "fonts/SourceSansPro-Light.otf");
        mTitle.setTypeface(customLato);
        loadFromParseServer();

        date = new Date();

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabsAdapter = new HomeFragmentPagerAdapter(getSupportFragmentManager(), DisplayHomeActivity.this, date);
        viewPager.setAdapter(tabsAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);


        Typeface tfLatoLight = Typeface.createFromAsset(getAssets(), "fonts/Lato-Light.ttf");
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(tfLatoLight);
                }
            }
        }

        fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null) {
            Log.d("DEBUG", "fragment is null");
        }

        setTaskAlarm();
        //scheduleAlarm();

        this.getSupportActionBar().setElevation(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocations();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                Intent i= new Intent(DisplayHomeActivity.this, SearchActivity.class);
                i.putExtra("code", 500);
                i.putExtra("query", query);
                startActivity(i);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void searchLocations(){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void onAddTask(View view) {
        // creates a new task each time a task is added
        mNewTask = new Task();

        FragmentManager fm = getSupportFragmentManager();
        addTaskFragment = AddTaskDialogFragment.newInstance("Add Task");

        Bundle bundle = new Bundle();
        bundle.putParcelable("Task", Parcels.wrap(mNewTask));
        addTaskFragment.setArguments(bundle);
        addTaskFragment.show(fm, "fragment_add_task");
    }

    public void onSelectLocation(View view) {

        Intent i = new Intent(this, LocationActivity.class);
        startActivityForResult(i, REQUEST_CODE);

    }

    public void onSetRecurrence(View view) {
        SetRecurringDateDialogFragment recurrenceDialogFragment = (SetRecurringDateDialogFragment)
                getSupportFragmentManager().findFragmentByTag("fragment_recurrence");

        if(recurrenceDialogFragment.recurrenceSelected) {
            if (recurrenceDialogFragment != null)
                recurrenceDialogFragment.recurrenceSelected = false;
                recurrenceDialogFragment.dismiss();
        }else{

            Toast.makeText(recurrenceDialogFragment.getContext(), "Must select a recurrence option", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *If cancel button was selected
     */
    public void cancelSettingRecurrence(View view) {


        SetRecurringDateDialogFragment recurrenceDialogFragment = (SetRecurringDateDialogFragment)
                getSupportFragmentManager().findFragmentByTag("fragment_recurrence");
        DisplayHomeActivityListener addTaskDialogFragment = (DisplayHomeActivityListener)
                getSupportFragmentManager().findFragmentByTag("fragment_add_task");
        if(addTaskDialogFragment != null)
        addTaskDialogFragment.cancelRecurrence();
        if (recurrenceDialogFragment != null){
            recurrenceDialogFragment.recurrenceSelected = false;
            recurrenceDialogFragment.dismiss();
        }
    }

    /**
     * Send information to the fragment that implements it
     */
    public interface DisplayHomeActivityListener {
        void onFinishChooseLocationActivity(double latitude, double longitude, String placeName, String geofenceId);
        void cancelRecurrence();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        viewPager.setCurrentItem(2);
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String geofenceId = data.getStringExtra("geofence_id");
            String placeName = data.getStringExtra("place_name");
            if (requestCode == REQUEST_CODE && resultCode == REQUEST_OK) {
                DisplayHomeActivityListener addTaskFrag = (DisplayHomeActivityListener) addTaskFragment;
                addTaskFrag.onFinishChooseLocationActivity(latitude, longitude, placeName, geofenceId);

                geofenceClass = (new GeofenceClass(geofenceId, //geofence id
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL,
                        longitude, //longitude
                        latitude, //latitude
                        GEOFENCE_RADIUS, EXPIRATION_TIME));

            }
        }

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                Intent intent= new Intent(DisplayHomeActivity.this, SearchActivity.class);
                intent.putExtra("code", 100);
                intent.putExtra("place", place.getName());
                startActivity(intent);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void onCancelTask(View view) {
        addTask = false;

        if (addTaskFragment != null) {
            addTaskFragment.dismiss();
        }
    }

    public void onTaskAdded(View view) {
        addTask = true;

        boolean formFilled = true;

        AddTaskDialogFragment addTaskFragment = (AddTaskDialogFragment) getSupportFragmentManager().findFragmentByTag("fragment_add_task");

        CheckBox cbAllDay = (CheckBox) addTaskFragment.getView().findViewById(R.id.cbAllDay);
        Switch swToggleTime = (Switch) addTaskFragment.getView().findViewById(R.id.swToggleTime);
        Button btnCreateTask = (Button) addTaskFragment.getView().findViewById(R.id.btnCreateTask);

        EditText etTaskName = (EditText) addTaskFragment.getView().findViewById(R.id.etTaskName);

        if (etTaskName.getText().toString().equals("")) {
            btnCreateTask.setBackground(getResources().getDrawable(R.drawable.shape_button_greyed_out));
            Toast.makeText(addTaskFragment.getContext(), "Name of task is required!", Toast.LENGTH_LONG).show();
            formFilled = false;
        }

        // if time is set
        if (swToggleTime.isChecked()) {
            Button btnStartDate = (Button) addTaskFragment.getView().findViewById(R.id.btnStartDate);
            Button btnEndDate = (Button) addTaskFragment.getView().findViewById(R.id.btnEndDate);
            String endDate = btnEndDate.getText().toString();
            String startDate = btnStartDate.getText().toString();

            if (convertStringToDate(startDate).after(convertStringToDate(endDate))) {
                btnCreateTask.setBackground(getResources().getDrawable(R.drawable.shape_button_greyed_out));
                Toast.makeText(addTaskFragment.getContext(), "End date is before start date", Toast.LENGTH_LONG).show();
                formFilled = false;
            }

            if (!cbAllDay.isChecked()) {
                if (startDate.equals(endDate)) {
                    Button btnStartTime = (Button) addTaskFragment.getView().findViewById(R.id.btnStartTime);
                    Button btnEndTime = (Button) addTaskFragment.getView().findViewById(R.id.btnEndTime);
                    String startTime = btnStartTime.getText().toString();
                    String endTime = btnEndTime.getText().toString();

                    if (!compareTimes(startTime, endTime)) {
                        btnCreateTask.setBackground(getResources().getDrawable(R.drawable.shape_button_greyed_out));
                        Toast.makeText(addTaskFragment.getContext(), "End time is before start time", Toast.LENGTH_LONG).show();
                        formFilled = false;
                    }
                }
            }
        }

        if (formFilled) {
            if (addTaskFragment != null)
                addTaskFragment.dismiss();

            CalendarView cv = (CalendarView) findViewById(R.id.calendar_view);
            HashSet<Date> events = updateEvents();
            cv.updateCalendar(events);
        }
    }

    private boolean compareTimes(String startTime, String endTime) {
        int startHour;
        int endHour;
        int startMins;
        int endMins;
        String startTimeOfDay;
        String endTimeOfDay;

        int startIndex = startTime.indexOf(":", 0);
        int endIndex = endTime.indexOf(":", 0);
        startHour = Integer.parseInt(startTime.substring(0, startIndex));
        endHour = Integer.parseInt(endTime.substring(0, endIndex));
        int startIndex1 = startTime.indexOf(" ", startIndex + 1);
        int endIndex1 = endTime.indexOf(" ", endIndex + 1);
        startMins = Integer.parseInt(startTime.substring(startIndex + 1, startIndex1));
        endMins = Integer.parseInt(endTime.substring(endIndex + 1, endIndex1));
        startTimeOfDay = startTime.substring(startIndex1 + 1);
        endTimeOfDay = endTime.substring(endIndex1 + 1);

        if (startTimeOfDay.equals("PM") && endTimeOfDay.equals("AM")) {
            return false;
        } else if (startTimeOfDay.equals("AM") && endTimeOfDay.equals("PM")) {
            return true;
        } else {
            if (startHour > endHour) {
                return false;
            } else {
                if (startMins > endMins) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onDateClick(Date date) {
        // Access the date here and pass to DayFragment
        // Start new day fragment
        //create the transaction
        if (mTimeFragment == null) {
            FragmentManager fm = getSupportFragmentManager();
            mTimeFragment = TimeFragment.newInstance(date);

            fm.beginTransaction().replace(R.id.flLayout, mTimeFragment)
                    .show(mTimeFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        } else {
            mTimeFragment.setDate(date);
        }
    }

    /*
     * Updates the event calendar with new tasks when created
     */
    public static HashSet<Date> updateEvents() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereNotEqualTo("start_date", "no_date");
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        try {
            sTaskList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HashSet<Date> events = new HashSet<>();

        for (int i = 0; i < sTaskList.size(); i++) {

            ParseObject task = sTaskList.get(i);
            String dateString = task.getString("start_date");
            int monthIndex = dateString.indexOf(" ", 0);
            String monthString = dateString.substring(0, monthIndex);
            int dateIndex = dateString.indexOf(" ", monthIndex + 1);
            int date = Integer.parseInt(dateString.substring(monthIndex + 1, dateIndex - 1));
            int year = Integer.parseInt(dateString.substring(dateIndex + 1));

            Date convertDate = null;

            try {
                convertDate = new java.text.SimpleDateFormat("MMM", Locale.ENGLISH).parse(monthString);
            } catch (java.text.ParseException e1) {
                e1.printStackTrace();
            }

            android.icu.util.Calendar cal = android.icu.util.Calendar.getInstance();
            cal.setTime(convertDate);
            int month = cal.get(android.icu.util.Calendar.MONTH);

            android.icu.util.Calendar newEvent = android.icu.util.Calendar.getInstance();
            newEvent.set(year, month, date);

            Date formattedEvent = new Date();
            formattedEvent = (Date) newEvent.getTime();
            events.add(formattedEvent);
        }

        ParseQuery<ParseObject> repeatedDatesQuery = ParseQuery.getQuery("Task");
        repeatedDatesQuery.whereEqualTo("repeating", true);
        repeatedDatesQuery.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());

        try {
            sRepeatedTaskList = repeatedDatesQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < sRepeatedTaskList.size(); i++) {

            ParseObject task = sRepeatedTaskList.get(i);
            List<Integer> daysOfWeeksRepeated = task.getList("repeat_days");

            String parseStartDate = task.getString("start_date");
            Date startDate;
            if (parseStartDate.equals("no_date") || parseStartDate == null) {
                startDate = java.util.Calendar.getInstance().getTime();
            } else {
                startDate = convertStringToDate(task.getString("start_date"));
            }

            String parseEndDate = task.getString("end_date");
            Date endDate;
            if (parseEndDate.equals("no_date") || parseEndDate == null) {
                endDate = java.util.Calendar.getInstance().getTime();
            } else {
                endDate = convertStringToDate(task.getString("end_date"));
            }

            java.util.Calendar currDate = java.util.Calendar.getInstance();
            currDate.set(startDate.getYear() + 1900, startDate.getMonth(), startDate.getDate());
            Log.d("ADD_EVENT", "start date: " + startDate.toString());
            Log.d("ADD_EVENT", "end date: " + endDate.toString());

            java.util.Calendar endingDate = java.util.Calendar.getInstance();
            endingDate.set(endDate.getYear() + 1900, endDate.getMonth(), endDate.getDate());

            while (currDate.before(endingDate)) {
                if (daysOfWeeksRepeated.contains(currDate.getTime().getDay())) {
                    events.add(currDate.getTime());
                }
                currDate.add(java.util.Calendar.DATE, 1);
            }
        }

        return events;
    }

    /*
     * Converts string format of dates to Date objects
     */
    public static Date convertStringToDate(String dateString) {
        int monthIndex = dateString.indexOf(" ", 0);
        String monthString = dateString.substring(0, monthIndex);
        int dateIndex = dateString.indexOf(" ", monthIndex + 1);
        int date = Integer.parseInt(dateString.substring(monthIndex + 1, dateIndex - 1));
        int year = Integer.parseInt(dateString.substring(dateIndex + 1));

        Date convertDate = null;

        try {
            convertDate = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(monthString);
        } catch (java.text.ParseException e1) {
            e1.printStackTrace();
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(convertDate);
        int month = cal.get(java.util.Calendar.MONTH);

        java.util.Calendar newEvent = java.util.Calendar.getInstance();
        newEvent.set(year, month, date);
        Log.d("ADD_EVENT", "calendar date: " + newEvent.toString());

        return newEvent.getTime();
    }

    /*
     * Calculates the end date of an event when given the number of repeats and days of week repeated
     */
    public static Date findEndDate (Date date, int repeatNum, List<Integer> daysOfWeeksRepeated) {
        int repeatCounter = 0;

        java.util.Calendar currDate = java.util.Calendar.getInstance();
        currDate.set(date.getYear() + 1900, date.getMonth(), date.getDate());

        while (repeatCounter < repeatNum) {
            if (daysOfWeeksRepeated.contains(currDate.getTime().getDay())) {
                repeatCounter++;
            }
            currDate.add(java.util.Calendar.DATE, 1);
        }

        return currDate.getTime();

    }

    /*
     * Loads information from parse server
     */
    private void loadFromParseServer () {
        ParseObject.unpinAllInBackground();

        List<ParseObject> loadedTaskList = new List<ParseObject>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<ParseObject> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }

            @Override
            public boolean add(ParseObject parseObject) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends ParseObject> collection) {
                return false;
            }

            @Override
            public boolean addAll(int i, Collection<? extends ParseObject> collection) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public ParseObject get(int i) {
                return null;
            }

            @Override
            public ParseObject set(int i, ParseObject parseObject) {
                return null;
            }

            @Override
            public void add(int i, ParseObject parseObject) {

            }

            @Override
            public ParseObject remove(int i) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<ParseObject> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<ParseObject> listIterator(int i) {
                return null;
            }

            @NonNull
            @Override
            public List<ParseObject> subList(int i, int i1) {
                return null;
            }
        };
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());

        try {
            loadedTaskList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < loadedTaskList.size(); i++) {
            ParseObject task = loadedTaskList.get(i);
            task.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("CREATETASK", "callback finished");
                }
            });
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //setTaskAlarm();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                Intent i = new Intent(this, LoginActivity.class);
                i.putExtra("from_logout", true);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() { }

    /**
     * sets alarm for tasks without location
     */
    public void setTaskAlarm(){

        //Enables Custom Boot Receiver
        ComponentName receiver = new ComponentName(this, CustomBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        //alarm interval - 1 hour
        int interval = 1000 * 60 * 60;

        String startDate = null;
        String endDate = null;
        String startTime = null;
        String endTime = null;
        Date beginDate = null;
        Date finishDate = null;
        int[] beginTime = new int[]{0,0};
        int[] finishTime= new int[]{0,0};

        Date currentDate = new Date();

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        List<ParseObject> taskList = new LinkedList<>();

        ParseQuery<ParseObject> query= ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        query.whereEqualTo("is_completed", false);
        query.whereDoesNotExist("geofence_id");
        try {
            taskList = query.find();
        }
        catch(ParseException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < taskList.size(); i++){
            ParseObject task = taskList.get(i);

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
            if(startDate != null || startTime != null ){
                if (finishDate.after(currentDate)) {
                    Intent intent= new Intent(getApplicationContext(), CustomAlarmReceiver.class);
                    intent.putExtra("created_at", task.getCreatedAt());
                    PendingIntent pendingIntent= PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, beginDate.getTime(), interval, pendingIntent);
                }
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
    public int[] convertTime(String inputTime){

        int minutes = 0;
        int hours = 0;

        String amPmString= null;
        int length= inputTime.length();
        if (length==8) {
            minutes = Integer.valueOf(inputTime.substring(3, 5));
            amPmString = inputTime.substring(6);
        }
        else {
            minutes = Integer.valueOf(inputTime.substring(2, 4));
            amPmString = inputTime.substring(5);
        }

        switch(amPmString) {
            case "AM":
                hours= Integer.valueOf(inputTime.substring(0, inputTime.indexOf(":")));
                if(hours==12){
                    hours= 0;
                }
                break;
            case "PM":
                hours= 12 + Integer.valueOf(inputTime.substring(0, inputTime.indexOf(":")));
                if(hours==24){
                    hours= 12;
                }
                break;
        }
        return new int[]{hours, minutes};
    }
}
