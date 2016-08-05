package com.example.kaylie.project.Fragments;

import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.Models.Task;
import com.example.kaylie.project.R;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by claireshu on 7/13/16.
 */
public class SetRecurringDateDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Task mTask;
    public static boolean recurrenceSelected = false;
    ParseObject editTask;

    Typeface customSourceSans;
    Typeface lato;

    public static final String ENDDATEPICKER_TAG = "end_datepicker";


    public SetRecurringDateDialogFragment() {

    }

    public static SetRecurringDateDialogFragment newInstance(String title) {
        SetRecurringDateDialogFragment frag = new SetRecurringDateDialogFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // retrieves information from bundle, either mTask (if creating new task)
        // or an objectId (for editing a task)
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mTask = (Task) Parcels.unwrap(bundle.getParcelable("Task"));
        }

        if (bundle.getString("objectId") != null) {

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
        return inflater.inflate(R.layout.dialog_fragment_recurrence, container);
    }

    /*
     * Sets up error checking for recurrence options, displays start/end date if applicable,
     * and sets up date picker listener
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Create SourceSans typeface
        customSourceSans = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Light.otf");
        //Create lato typeface
        lato = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Regular.ttf");

        TextView tvStartDate = (TextView) view.findViewById(R.id.tvStartDate);
        TextView tvEndDate = (TextView) view.findViewById(R.id.tvEndDate);

        TextView tvStartsOn = (TextView) view.findViewById(R.id.tvStartsOn);
        TextView tvRepeatsOn = (TextView) view.findViewById(R.id.tvRepeatsOn);

        TextView tvEndsOn = (TextView) view.findViewById(R.id.tvEndsOn);

        TextView tvRecur = (TextView) view.findViewById(R.id.tvRecur);
        EditText etNumRepeat = (EditText) view.findViewById(R.id.etNumRepeat);

        Button btnCancelRecur = (Button) view.findViewById(R.id.btnCancelRecur);
        Button btnSetRecurrence = (Button) view.findViewById(R.id.btnSetRecurrence);

        ToggleButton tbSunday = (ToggleButton) view.findViewById(R.id.tbSunday);
        ToggleButton tbMonday = (ToggleButton) view.findViewById(R.id.tbMonday);
        ToggleButton tbTuesday = (ToggleButton) view.findViewById(R.id.tbTuesday);
        ToggleButton tbWednesday = (ToggleButton) view.findViewById(R.id.tbWednesday);
        ToggleButton tbThursday = (ToggleButton) view.findViewById(R.id.tbThursday);
        ToggleButton tbFriday = (ToggleButton) view.findViewById(R.id.tbFriday);
        ToggleButton tbSaturday = (ToggleButton) view.findViewById(R.id.tbSaturday);

        //set typefaces
        tvStartDate.setTypeface(customSourceSans);
        tvEndDate.setTypeface(customSourceSans);

        tvStartsOn.setTypeface(customSourceSans);
        tvRepeatsOn.setTypeface(customSourceSans);

        tvEndsOn.setTypeface(customSourceSans);

        tvRecur.setTypeface(customSourceSans);
        etNumRepeat.setTypeface(customSourceSans);

        btnCancelRecur.setTypeface(customSourceSans);
        btnSetRecurrence.setTypeface(customSourceSans);

        tbSunday.setTypeface(lato);
        tbMonday.setTypeface(lato);
        tbTuesday.setTypeface(lato);
        tbWednesday.setTypeface(lato);
        tbThursday.setTypeface(lato);
        tbFriday.setTypeface(lato);
        tbSaturday.setTypeface(lato);

        if (editTask == null) {
            tvStartDate.setText(mTask.getStartDate());
            tvEndDate.setText(mTask.getEndDate());

        } else {
            tvStartDate.setText(editTask.getString("start_date"));
            tvEndDate.setText(editTask.getString("end_date"));

        }

        final RadioButton rbEndDate = (RadioButton) view.findViewById(R.id.rbEndDate);
        final RadioButton rbNumRepeat = (RadioButton) view.findViewById(R.id.rbNumRepeat);

        rbEndDate.setTypeface(customSourceSans);
        rbNumRepeat.setTypeface(customSourceSans);


        view.findViewById(R.id.rbNumRepeat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rbEndDate.setChecked(false);
                rbNumRepeat.setChecked(true);
                recurrenceSelected = true;
            }
        });

        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog endDatePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);

        view.findViewById(R.id.rbEndDate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDatePickerDialog.setVibrate(true);
                endDatePickerDialog.setYearRange(1985, 2028);
                endDatePickerDialog.setCloseOnSingleTapDay(false);
                endDatePickerDialog.show(getFragmentManager(), ENDDATEPICKER_TAG);
                rbEndDate.setChecked(true);
                rbNumRepeat.setChecked(false);
                recurrenceSelected = true;

            }
        });

        if (savedInstanceState != null) {
            DatePickerDialog eDpd = (DatePickerDialog) getFragmentManager().findFragmentByTag(ENDDATEPICKER_TAG);
            if (eDpd != null) {
                eDpd.setOnDateSetListener(this);
            }
        }
    }

    /*
     * Set the button text dynamically when the user selects a date using the date picker
     */
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        Toast.makeText(getActivity(), "new date:" + year + "-" + month + "-" + day, Toast.LENGTH_LONG).show();
        TextView tvEndDate = (TextView) getView().findViewById(R.id.tvEndDate);

        Calendar calDate = Calendar.getInstance();
        calDate.set(year, month, day);

        Date date = new Date();
        date.setYear(year);
        date.setMonth(month);
        date.setDate(day);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        dateFormat.setCalendar(calDate);
        tvEndDate.setText(dateFormat.format(calDate.getTime()));

        String parseEndDate = dateFormat.format(calDate.getTime());

        if (editTask == null) {
            mTask.setEndDate(parseEndDate);
        } else {
            editTask.put("end_date", parseEndDate);
        }
    }

    /*
     * Sets the dimensions of the dialog fragment for add task
     */
    public void onResume() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        getDialog().getWindow().setLayout(width - 150, height - 550);
        super.onResume();

    }

    /*
     * Sets all recurrence information selected by the user to the task being edited/added
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ToggleButton tbSunday = (ToggleButton) getView().findViewById(R.id.tbSunday);
        ToggleButton tbMonday = (ToggleButton) getView().findViewById(R.id.tbMonday);
        ToggleButton tbTuesday = (ToggleButton) getView().findViewById(R.id.tbTuesday);
        ToggleButton tbWednesday = (ToggleButton) getView().findViewById(R.id.tbWednesday);
        ToggleButton tbThursday = (ToggleButton) getView().findViewById(R.id.tbThursday);
        ToggleButton tbFriday = (ToggleButton) getView().findViewById(R.id.tbFriday);
        ToggleButton tbSaturday = (ToggleButton) getView().findViewById(R.id.tbSaturday);

        List<Integer> daysOfWeeksRepeated = new ArrayList<Integer>();
        if (tbSunday.isChecked()) {
            daysOfWeeksRepeated.add(0);
        }

        if (tbMonday.isChecked()) {
            daysOfWeeksRepeated.add(1);
        }

        if (tbTuesday.isChecked()) {
            daysOfWeeksRepeated.add(2);
        }

        if (tbWednesday.isChecked()) {
            daysOfWeeksRepeated.add(3);
        }

        if (tbThursday.isChecked()) {
            daysOfWeeksRepeated.add(4);
        }

        if (tbFriday.isChecked()) {
            daysOfWeeksRepeated.add(5);
        }

        if (tbSaturday.isChecked()) {
            daysOfWeeksRepeated.add(6);
        }

        if (editTask == null) {
            mTask.setRepeatDays(daysOfWeeksRepeated);
        } else {
            editTask.put("repeat_days", daysOfWeeksRepeated);
        }

        RadioButton rbEndDate = (RadioButton) getView().findViewById(R.id.rbEndDate);
        if (rbEndDate.isChecked()) {
            TextView tvEndDate = (TextView) getView().findViewById(R.id.tvEndDate);
            String endDate = tvEndDate.getText().toString();
            if (!endDate.equals("")) {
                Button btnEndDate = (Button) getActivity().getSupportFragmentManager().findFragmentByTag("fragment_add_task").getView().findViewById(R.id.btnEndDate);
                btnEndDate.setText(endDate);
            }
        }

        int numRepeat;

        RadioButton rbNumRepeat = (RadioButton) getView().findViewById(R.id.rbNumRepeat);
        if (rbNumRepeat.isChecked()) {
            EditText etNumRepeat = (EditText) getView().findViewById(R.id.etNumRepeat);

            if (etNumRepeat.getText().toString().equals("")) {
                numRepeat = -1;
            } else {
                numRepeat = Integer.parseInt(etNumRepeat.getText().toString());
                Log.d("ADD_EVENT", "num repeat: " + numRepeat);

                Button btnStartDate = (Button) getActivity().getSupportFragmentManager().findFragmentByTag("fragment_add_task").getView().findViewById(R.id.btnStartDate);
                Date date = DisplayHomeActivity.findEndDate(DisplayHomeActivity.convertStringToDate(btnStartDate.getText().toString()), numRepeat, daysOfWeeksRepeated);

                Calendar endDate = Calendar.getInstance();
                endDate.set(date.getYear() + 1900, date.getMonth(), date.getDate());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                dateFormat.setCalendar(endDate);
                String parseEndDate = dateFormat.format(endDate.getTime());

                if (editTask == null) {
                    mTask.setEndDate(parseEndDate);
                } else {
                    editTask.put("end_date", parseEndDate);
                }

                Button btnEndDate = (Button) getActivity().getSupportFragmentManager().findFragmentByTag("fragment_add_task").getView().findViewById(R.id.btnEndDate);
                btnEndDate.setText(parseEndDate);
            }
        } else {
            numRepeat = -1;
        }

        if (editTask == null) {
            mTask.setRepeatNum(numRepeat);
            mTask.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("CREATETASK", "callback finished for recurrence task");
                }
            });

            mTask.saveEventually();

        } else {
            editTask.put("repeat_num", numRepeat);
            editTask.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("CREATETASK", "callback finished for recurrence task");
                }
            });

            editTask.saveEventually();
        }
    }
}