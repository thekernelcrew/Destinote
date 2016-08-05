package com.example.kaylie.project.Fragments;

import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.ListUpdater;
import com.example.kaylie.project.Models.Task;
import com.example.kaylie.project.R;
import com.facebook.Profile;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.parceler.Parcels;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * Created by claireshu on 7/12/16.
 */
public class AddTaskDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, DisplayHomeActivity.DisplayHomeActivityListener{


    public static final String STARTDATEPICKER_TAG = "start_datepicker";
    public static final String ENDDATEPICKER_TAG = "end_datepicker";
    public static final String STARTTIMEPICKER_TAG = "start_timepicker";
    public static final String ENDTIMEPICKER_TAG = "end_timepicker";

    private EditText mEditTextTaskName;
    private EditText mEditTextTaskDescrip;

    private TextView mTextViewRecurrence;
    private Task mTask;

    private long startTime;
    private long endTime;

    boolean setTimeStart;
    Date date;

    ParseObject editTask;
    String objectId;

    Typeface customSourceSans;

    public AddTaskDialogFragment() {

    }

    public static AddTaskDialogFragment newInstance(String title) {
        AddTaskDialogFragment frag = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public interface AddTaskDialogFragmentListener{

        public void taskAdded(Task t);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // retrieves information from bundle, either mTask (if creating new task)
        // or an objectId (for editing a task)
        Bundle bundle = this.getArguments();
        if (bundle != null) {

            if (bundle.getParcelable("Task") != null) {
                mTask = (Task) Parcels.unwrap(bundle.getParcelable("Task"));
            }

            objectId = bundle.getString("objectId");
            if (objectId != null) {

                List<ParseObject> taskList = new List<ParseObject>() {
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

                String objectId = bundle.getString("objectId");

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
                query.fromLocalDatastore();
                query.whereEqualTo("objectId", objectId);

                try {
                    taskList = query.find();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                editTask = taskList.get(0);

            }
        }
        return inflater.inflate(R.layout.dialog_fragment_add_task, container);
    }

    /*
     * Sets up listeners and displays for buttons, date pickers, etc.
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Create SourceSans typeface
        customSourceSans = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Light.otf");

        LinearLayout llTaskTimeDetail = (LinearLayout) getView().findViewById(R.id.llTaskTimeDetail);
        llTaskTimeDetail.setVisibility(View.INVISIBLE);

        Button btnCreateTask = (Button) view.findViewById(R.id.btnCreateTask);
        btnCreateTask.setTypeface(customSourceSans);
        if (editTask != null) {
            btnCreateTask.setText(R.string.edit_task);
        }

        setUpButtons();

        mEditTextTaskDescrip = (EditText) view.findViewById(R.id.etTaskDescription);
        mEditTextTaskDescrip.setTypeface(customSourceSans);

        mEditTextTaskDescrip.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        mEditTextTaskName = (EditText) view.findViewById(R.id.etTaskName);
        mEditTextTaskName.setTypeface(customSourceSans);
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        mEditTextTaskName.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //Set typeface for checkBoxes and TextViews
        CheckBox cbRepeating = (CheckBox) view.findViewById(R.id.cbRepeating);
        cbRepeating.setTypeface(customSourceSans);
        CheckBox cbAllDay = (CheckBox) view.findViewById(R.id.cbAllDay);
        cbAllDay.setTypeface(customSourceSans);

        TextView tvLocation = (TextView) view.findViewById(R.id.tvLocation);
        tvLocation.setTypeface(customSourceSans);

        if (editTask != null) {
            mEditTextTaskName.setText(editTask.getString("name"));
            String description = editTask.getString("description");
            if (description != null) {
                mEditTextTaskDescrip.setText(description);
            }

            String location = editTask.getString("place_name");
            if (location != null) {
                tvLocation.setText(location);
            }

            boolean repeating = editTask.getBoolean("repeating");
            if (repeating) {
                cbRepeating.setChecked(true);
            }

            String startTime = editTask.getString("start_time");
            if (startTime.equals("all_day")) {
                cbAllDay.setChecked(true);

                LinearLayout llTaskSelectTime = (LinearLayout) getView().findViewById(R.id.llTaskSelectTime);
                llTaskSelectTime.setVisibility(View.INVISIBLE);
            }
        }

        mTextViewRecurrence = (TextView) view.findViewById(R.id.tvRecurrence);

        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog startDatePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
        final DatePickerDialog endDatePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
        final TimePickerDialog startTimePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);
        final TimePickerDialog endTimePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);


        view.findViewById(R.id.btnStartDate).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startDatePickerDialog.setVibrate(true);
                startDatePickerDialog.setYearRange(1985, 2028);
                startDatePickerDialog.setCloseOnSingleTapDay(false);
                startDatePickerDialog.show(getFragmentManager(), STARTDATEPICKER_TAG);
            }
        });

        view.findViewById(R.id.btnEndDate).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                endDatePickerDialog.setVibrate(true);
                endDatePickerDialog.setYearRange(1985, 2028);
                endDatePickerDialog.setCloseOnSingleTapDay(false);
                endDatePickerDialog.show(getFragmentManager(), ENDDATEPICKER_TAG);
            }
        });


        view.findViewById(R.id.btnStartTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimeStart = true;
                startTimePickerDialog.setVibrate(true);
                startTimePickerDialog.setCloseOnSingleTapMinute(false);
                startTimePickerDialog.show(getFragmentManager(), STARTTIMEPICKER_TAG);
            }
        });

        view.findViewById(R.id.btnEndTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimeStart = false;
                endTimePickerDialog.setVibrate(true);
                endTimePickerDialog.setCloseOnSingleTapMinute(false);
                endTimePickerDialog.show(getFragmentManager(), ENDTIMEPICKER_TAG);
            }
        });

        view.findViewById(R.id.cbRepeating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            CheckBox cbRepeating = (CheckBox) v.findViewById(R.id.cbRepeating);
                cbRepeating.setTypeface(customSourceSans);
            if (cbRepeating.isChecked()) {
                FragmentManager fm = getFragmentManager();
                SetRecurringDateDialogFragment setRecurringDateFragment = SetRecurringDateDialogFragment.newInstance("fragment_recurrence");
                Bundle bundle = new Bundle();
                bundle.putParcelable("Task", Parcels.wrap(mTask));
                bundle.putString("objectId", objectId);
                setRecurringDateFragment.setArguments(bundle);

                setRecurringDateFragment.show(fm, "fragment_recurrence");
            }
            }
        });

        view.findViewById(R.id.cbAllDay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cbAllDay = (CheckBox) v.findViewById(R.id.cbAllDay);
                cbAllDay.setTypeface(customSourceSans);
                LinearLayout llTaskSelectTime = (LinearLayout) getView().findViewById(R.id.llTaskSelectTime);

                if (cbAllDay.isChecked()) {
                    llTaskSelectTime.setVisibility(View.INVISIBLE);
                } else {
                    llTaskSelectTime.setVisibility(View.VISIBLE);
                }
            }
        });

        view.findViewById(R.id.swToggleTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch swToggleTime = (Switch) v.findViewById(R.id.swToggleTime);
                LinearLayout llTaskTimeDetail = (LinearLayout) getView().findViewById(R.id.llTaskTimeDetail);
                if (swToggleTime.isChecked()) {
                    llTaskTimeDetail.setVisibility(View.VISIBLE);
                } else {
                    llTaskTimeDetail.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (savedInstanceState != null) {
            DatePickerDialog sDpd = (DatePickerDialog) getFragmentManager().findFragmentByTag(STARTDATEPICKER_TAG);
            if (sDpd != null) {
                sDpd.setOnDateSetListener(this);
            }

            DatePickerDialog eDpd = (DatePickerDialog) getFragmentManager().findFragmentByTag(ENDDATEPICKER_TAG);
            if (eDpd != null) {
                eDpd.setOnDateSetListener(this);
            }

            TimePickerDialog sTpd = (TimePickerDialog) getFragmentManager().findFragmentByTag(STARTTIMEPICKER_TAG);
            if (sTpd != null) {
                sTpd.setOnTimeSetListener(this);
            }

            TimePickerDialog eTpd = (TimePickerDialog) getFragmentManager().findFragmentByTag(ENDTIMEPICKER_TAG);
            if (eTpd != null) {
                eTpd.setOnTimeSetListener(this);
            }
        }

    }

    /*
     * Sets up buttons to display the current date/time (if adding a new task) or
     * the previously set date/time (if editing a task)
     */
    private void setUpButtons() {
        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        dateFormat.setCalendar(date);

        Button btnStartDate = (Button) getView().findViewById(R.id.btnStartDate);
        Button btnEndDate = (Button) getView().findViewById(R.id.btnEndDate);

        Button btnStartTime = (Button) getView().findViewById(R.id.btnStartTime);
        Button btnEndTime = (Button) getView().findViewById(R.id.btnEndTime);

        Button btnLocation = (Button) getView().findViewById(R.id.btnLocation);
        Button btnCancel = (Button) getView().findViewById(R.id.btnCancelTask);

        TextView tvSetTime = (TextView) getView().findViewById(R.id.tvSetTime);

        //Set typeface for Buttons and TextViews
        btnStartDate.setTypeface(customSourceSans);
        btnEndDate.setTypeface(customSourceSans);
        btnStartTime.setTypeface(customSourceSans);
        btnEndTime.setTypeface(customSourceSans);

        btnLocation.setTypeface(customSourceSans);

        btnCancel.setTypeface(customSourceSans);

        tvSetTime.setTypeface(customSourceSans);

        String startDate;
        String endDate;
        String startTime;
        String endTime;

        if (editTask == null) {
            startDate = dateFormat.format(date.getTime());
            endDate = dateFormat.format(date.getTime());
            startTime = convertTimeToString(date.getTime());
            date.add(Calendar.HOUR_OF_DAY, 1);
            endTime = convertTimeToString(date.getTime());

        } else {
            if (editTask.getString("start_date").equals("no_date") || editTask.getString("start_date") == null ) {
                startDate = dateFormat.format(date.getTime());
                endDate = dateFormat.format(date.getTime());
            } else {
                startDate = editTask.getString("start_date");
                endDate = editTask.getString("end_date");

                Switch swToggleTime = (Switch) getView().findViewById(R.id.swToggleTime);
                swToggleTime.setChecked(true);
                LinearLayout llTaskTimeDetail = (LinearLayout) getView().findViewById(R.id.llTaskTimeDetail);
                llTaskTimeDetail.setVisibility(View.VISIBLE);
            }

            Log.d("START_DATE_", startDate);

            if (editTask.getString("start_time").equals("no_time") || editTask.getString("start_time").equals("all_day") || editTask.get("start_time") == null) {
                startTime = convertTimeToString(date.getTime());
                date.add(Calendar.HOUR_OF_DAY, 1);
                endTime = convertTimeToString(date.getTime());
            } else {
                startTime = editTask.getString("start_time");
                endTime = editTask.getString("end_time");
            }
        }

        btnStartDate.setText(startDate);
        btnEndDate.setText(endDate);
        btnStartTime.setText(startTime);
        btnEndTime.setText(endTime);

        if (mTask != null) {
            mTask.setStartDate(startDate);
            mTask.setEndDate(endDate);
        }
    }

    /*
     * Converts a Time object to a String
     */
    private String convertTimeToString(Date date) {
        int hourOfDay = date.getHours();

        String timeSuffix;
        String hourStr;

        if (hourOfDay > 12 ) {
            hourOfDay = hourOfDay % 12;
            timeSuffix = "PM";
        } else if (hourOfDay == 12) {
            timeSuffix = "PM";
        } else {
            timeSuffix = "AM";
        }


        if(hourOfDay < 10){
            hourStr = "0" + hourOfDay;
        }else{
            hourStr = "" + hourOfDay;
        }

        String time;
        time = hourStr + ":00 " + timeSuffix;

        return time;
    }

    /*
     * Set the button text dynamically when the user selects a time using the time picker
     */
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        Button btnTime;

        if (setTimeStart) {
            btnTime = (Button) getView().findViewById(R.id.btnStartTime);
            this.startTime = hourOfDay * 60 + minute;
        } else {
            btnTime = (Button) getView().findViewById(R.id.btnEndTime);
            this.endTime = hourOfDay * 60 + minute;
        }

        String timeSuffix;
        String hourStr;
        if (hourOfDay == 24) {
            timeSuffix = "AM";
        } else if (hourOfDay > 12 ) {
            hourOfDay = hourOfDay % 12;
            timeSuffix = "PM";
        } else if (hourOfDay == 12) {
            timeSuffix = "PM";
        } else {
            timeSuffix = "AM";
        }


        if(hourOfDay < 10){
            hourStr = "0" + hourOfDay;
        }else{
            hourStr = "" + hourOfDay;
        }

        String time;
        if (minute < 10) {
            time = hourStr + ":0" + minute + " " + timeSuffix;
        } else {
            time = hourStr + ":" + minute + " " + timeSuffix;

        }

        btnTime.setText(time);

        if (setTimeStart) {
            if(this.endTime != 0 && this.startTime > this.endTime) {
                Toast.makeText(getContext(), "Start time must be earlier than end time", Toast.LENGTH_LONG).show();
            } else {
                if (editTask == null) {
                    mTask.setStartTime(time);
                } else {
                    editTask.put("start_time", time);
                }
            }
        } else {
            if (this.startTime != 0 && this.startTime > this.endTime) {
                Toast.makeText(getContext(), "End time must be later than start time", Toast.LENGTH_LONG).show();
            } else {
                if (editTask == null) {
                    mTask.setEndTime(time);
                } else {
                    editTask.put("end_time", time);
                }
            }
        }
    }

    /*
     * Set the button text dynamically when the user selects a date using the date picker
     */
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        Button btnDate;

        if (datePickerDialog.getTag().equals(STARTDATEPICKER_TAG)) {
            btnDate = (Button) getView().findViewById(R.id.btnStartDate);
        } else {
            btnDate = (Button) getView().findViewById(R.id.btnEndDate);
        }

        Calendar calDate = Calendar.getInstance();
        calDate.set(year, month, day);

        date = new Date();
        date.setYear(year);
        date.setMonth(month);
        date.setDate(day);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        dateFormat.setCalendar(calDate);
        btnDate.setText(dateFormat.format(calDate.getTime()));

        String parseDate = dateFormat.format(calDate.getTime());

        if (editTask == null) {
            if (datePickerDialog.getTag().equals(STARTDATEPICKER_TAG)) {
                mTask.setStartDate(parseDate);
            } else {
                mTask.setEndDate(parseDate);
            }
        } else {
            if (datePickerDialog.getTag().equals(STARTDATEPICKER_TAG)) {
                editTask.put("start_date", parseDate);
            } else {
                editTask.put("end_date", parseDate);
            }
        }
    }

    /*
     * Sets the dimensions of the dialog fragment for add task
     */
    public void onResume() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        getDialog().getWindow().setLayout(width - 60, height - 500);
        super.onResume();

    }

    /*
     * Sets values for the task object and stores it on parse for new objects
     * and updates changed fields for editing objects
     */
    @Override
    public void onDestroyView() {
        if (DisplayHomeActivity.addTask) {

            mEditTextTaskName = (EditText) getView().findViewById(R.id.etTaskName);
            mEditTextTaskDescrip = (EditText) getView().findViewById(R.id.etTaskDescription);
            CheckBox cbRepeating = (CheckBox) getView().findViewById(R.id.cbRepeating);

            Button btnStartDate = (Button) getView().findViewById(R.id.btnStartDate);
            Button btnEndDate = (Button) getView().findViewById(R.id.btnEndDate);
            Button btnStartTime = (Button) getView().findViewById(R.id.btnStartTime);
            Button btnEndTime = (Button) getView().findViewById(R.id.btnEndTime);

            CheckBox cbAllDay = (CheckBox) getView().findViewById(R.id.cbAllDay);
            Switch swToggleTime = (Switch) getView().findViewById(R.id.swToggleTime);

            if (editTask == null) {
                mTask.setName(mEditTextTaskName.getText().toString());
                mTask.setDescription(mEditTextTaskDescrip.getText().toString());
                mTask.setRepeating(cbRepeating.isChecked());

                if (swToggleTime.isChecked()) {

                    mTask.setStartDate(btnStartDate.getText().toString());
                    mTask.setEndDate(btnEndDate.getText().toString());

                    if (!cbAllDay.isChecked()) {
                        mTask.setStartTime(btnStartTime.getText().toString());
                        mTask.setEndTime(btnEndTime.getText().toString());
                    } else {
                        mTask.setStartTime("all_day");
                        mTask.setEndTime("all_day");
                    }
                } else {
                    mTask.setStartDate("no_date");
                    mTask.setEndDate("no_date");
                    mTask.setStartTime("no_time");
                    mTask.setEndTime("no_time");
                }

                //Automatically set isCompleted to false
                mTask.setCompleted(false);

                ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    mTask.setOwner(user);
                }

                String fbUser = Profile.getCurrentProfile().toString();
                if (fbUser != null) {
                    mTask.setFbUser(fbUser);
                }

                if (mTask.getStartDate() == null) {
                    Date startDate = java.util.Calendar.getInstance().getTime();

                    Calendar currDate = Calendar.getInstance();
                    currDate.set(startDate.getYear() + 1900, startDate.getMonth(), startDate.getDate());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                    dateFormat.setCalendar(currDate);

                    String parseStartDate = dateFormat.format(currDate.getTime());

                    mTask.setStartDate(parseStartDate);
                }

                mTask.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("CREATETASK", "callback finished");
                    }
                });

                ListUpdater.getInstance().addTask(mTask);

                mTask.saveEventually();

            } else {

                editTask.put("name", mEditTextTaskName.getText().toString());
                editTask.put("description", mEditTextTaskDescrip.getText().toString());
                editTask.put("repeating", cbRepeating.isChecked());

                if (swToggleTime.isChecked()) {
                    editTask.put("start_date", btnStartDate.getText().toString());
                    editTask.put("end_date", btnEndDate.getText().toString());

                    if (!cbAllDay.isChecked()) {
                        editTask.put("start_time", btnStartTime.getText().toString());
                        editTask.put("end_time", btnEndTime.getText().toString());
                    } else {
                        editTask.put("start_time", "all_day");
                        editTask.put("end_time", "all_day");
                    }
                } else {
                    editTask.put("start_date", "no_date");
                    editTask.put("end_date", "no_date");
                    editTask.put("start_time", "no_time");
                    editTask.put("end_time", "no_time");
                }

                //Automatically set isCompleted to false
                editTask.put("is_completed", false);

                ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    editTask.put("owner", user);
                }

                String fbUser = Profile.getCurrentProfile().toString();
                if (fbUser != null) {
                    editTask.put("fb_user", fbUser);
                }

                if (editTask.getString("start_date") == null) {
                    Date startDate = java.util.Calendar.getInstance().getTime();

                    Calendar currDate = Calendar.getInstance();
                    currDate.set(startDate.getYear() + 1900, startDate.getMonth(), startDate.getDate());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    dateFormat.setCalendar(currDate);

                    String parseStartDate = dateFormat.format(currDate.getTime());

                    editTask.put("start_date", parseStartDate);
                }

                editTask.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("CREATETASK", "callback finished");
                    }
                });

                editTask.saveEventually();
            }
        }

        editTask = null;

        super.onDestroyView();
    }

    /*
     * Stores the location-based information upon location is selected by user
     */
    @Override
    public void onFinishChooseLocationActivity(double latitude, double longitude, String placeName, String geofenceId) {
        mTask.setGeofenceId(geofenceId);
        mTask.setLatitude(Double.toString(latitude));
        mTask.setLongitude(Double.toString(longitude));
        if(placeName == null){
            placeName = "";
        }
        mTask.setPlaceName(placeName);
        TextView tvLocation = (TextView) getView().findViewById(R.id.tvLocation);
        tvLocation.setTypeface(customSourceSans);
        tvLocation.setText(placeName);
    }

    /*
     * When cancel button is clicked in the recurrence picker, the repeating checkbox is unchecked
     */
    @Override
    public void cancelRecurrence() {
        CheckBox cbRepeating = (CheckBox) getView().findViewById(R.id.cbRepeating);
        if(cbRepeating != null)
        cbRepeating.setChecked(false);
    }

}