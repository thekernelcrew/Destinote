package com.example.kaylie.project.Models;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kaylie.project.DisplayHomeActivity;
import com.example.kaylie.project.R;
import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by claireshu on 7/8/16.
 */
public class CalendarView extends LinearLayout
{
    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // date format
    private String dateFormat;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    List<ParseObject> mTaskList;
    List<ParseObject> mRepeatedTaskList;
    ParseQuery<ParseObject> repeatedDatesQuery;

    Typeface customLato;
    Typeface customSourceSans;
    Typeface customLatoBold;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;
    TextView tvDate;

    // seasons' rainbow
    int[] rainbow = new int[] {
            R.color.summer,
            R.color.fall,
            R.color.winter,
            R.color.spring
    };

    // month-season association
    int[] monthSeason = new int[] {2, 2, 3, 3, 3, 0, 0, 0, 1, 1, 1, 2};

    public CalendarView(Context context)
    {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();

    }

    private void loadDateFormat(AttributeSet attrs)
    {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        try
        {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        }
        finally
        {
            ta.recycle();
        }
    }
    private void assignUiElements()
    {
        // layout is inflated, assign local variables to components
        header = (LinearLayout)findViewById(R.id.calendar_header);
        btnPrev = (ImageView)findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView)findViewById(R.id.calendar_next_button);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);
        grid.setDrawSelectorOnTop(true);
    }

    private void assignClickHandlers()
    {
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.add(Calendar.MONTH, 1);
                HashSet<Date> events = DisplayHomeActivity.updateEvents();
                updateCalendar(events);
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.add(Calendar.MONTH, -1);
                HashSet<Date> events = DisplayHomeActivity.updateEvents();
                updateCalendar(events);
            }
        });

        // long-pressing a day
      grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id)
            {
                // handle long-press
                if (eventHandler == null)
                    return false;

                eventHandler.onDayLongPress((Date)view.getItemAtPosition(position));
                return true;
            }
        });

        // clicking a day
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                eventHandler.onDateClick((Date)adapterView.getItemAtPosition(i));

            }
        });
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar()
    {
        updateCalendar(null);
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(HashSet<Date> events)
    {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT)
        {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()).toUpperCase());

        // set header color according to current season
        int month = currentDate.get(Calendar.MONTH);
        int season = monthSeason[month];
        int color = rainbow[season];

        header.setBackgroundColor(getResources().getColor(color));
    }


    private class CalendarAdapter extends ArrayAdapter<Date>
    {
        // days with events
        private HashSet<Date> eventDays;

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays)
        {
            super(context, R.layout.control_calendar_day, days);
            this.eventDays = eventDays;

            repeatedDatesQuery = ParseQuery.getQuery("Task");
            repeatedDatesQuery.fromLocalDatastore();
            repeatedDatesQuery.whereNotEqualTo("repeating", null);
            repeatedDatesQuery.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());

            try {
                mRepeatedTaskList = repeatedDatesQuery.find();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent)
        {
            // day in question
            Date date = getItem(position);
            int day = date.getDate();
            int month = date.getMonth();
            int year = date.getYear();

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.control_calendar_day, parent, false);

            //Create custom Typefaces
            customLato = Typeface.createFromAsset(getContext().getAssets(),  "fonts/Lato-Regular.ttf");
            customSourceSans = Typeface.createFromAsset(getContext().getAssets(),  "fonts/SourceSansPro-Regular.otf");
            customLatoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Black.ttf");

            TextView sunday = (TextView) findViewById(R.id.sunday);
            sunday.setTypeface(customLato);

            TextView monday = (TextView) findViewById(R.id.monday);
            monday.setTypeface(customLato);

            TextView tuesday = (TextView) findViewById(R.id.tuesday);
            tuesday.setTypeface(customLato);

            TextView wednesday = (TextView) findViewById(R.id.wednesday);
            wednesday.setTypeface(customLato);

            TextView thursday = (TextView) findViewById(R.id.thursday);
            thursday.setTypeface(customLato);

            TextView friday = (TextView) findViewById(R.id.friday);
            friday.setTypeface(customLato);

            TextView saturday = (TextView) findViewById(R.id.saturday);
            saturday.setTypeface(customLato);

            TextView monthTitle = (TextView) findViewById(R.id.calendar_date_display);
            monthTitle.setTypeface(customLato);


            tvDate = (TextView) view.findViewById(R.id.tvDateTime);
            int numEvents = 0;

            // if this day has an event, specify event image
            if (eventDays != null)
            {
                for (Date eventDate : eventDays)
                {
                    if (eventDate.getDate() == day &&
                            eventDate.getMonth() == month &&
                            eventDate.getYear() == year)
                    {
                        numEvents++;
                    }
                }
            }

            ImageView ivOne = (ImageView) view.findViewById(R.id.ivOne);
            ImageView ivTwo = (ImageView) view.findViewById(R.id.ivTwo);
            ImageView ivThree = (ImageView) view.findViewById(R.id.ivThree);

            if (numEvents == 0) {
                ivOne.setVisibility(View.INVISIBLE);
                ivTwo.setVisibility(View.INVISIBLE);
                ivThree.setVisibility(View.INVISIBLE);

            } else if (numEvents == 1) {
                ivOne.setVisibility(View.INVISIBLE);
                ivThree.setVisibility(View.INVISIBLE);

                ivTwo.setImageResource(R.drawable.mid_event);
                ivTwo.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

            } else if (numEvents == 2) {
                ivThree.setVisibility(View.INVISIBLE);

                ivOne.setImageResource(R.drawable.two_left);
                ivOne.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

                ivTwo.setImageResource(R.drawable.two_right);
                ivTwo.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

            } else if (numEvents == 3) {
                ivOne.setImageResource(R.drawable.mid_event);
                ivOne.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

                ivTwo.setImageResource(R.drawable.left_event);
                ivTwo.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

                ivThree.setImageResource(R.drawable.right_event);
                ivThree.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

            } else { // numEvents > 3
                ivOne.setImageResource(R.drawable.mid_event);
                ivOne.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

                ivTwo.setImageResource(R.drawable.left_event);
                ivTwo.getDrawable().setColorFilter(getResources().getColor(R.color.accent_blue), PorterDuff.Mode.MULTIPLY);

                ivThree.setImageResource(R.drawable.plus_event);
                ivThree.getDrawable().setColorFilter(getResources().getColor(R.color.event_gray), PorterDuff.Mode.MULTIPLY);
            }

            Typeface customLatoLight =  Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Light.ttf");

            // clear styling
            tvDate.setTypeface(customLatoLight);
            tvDate.setTextColor(Color.BLACK);

            if (month != today.getMonth() || year != today.getYear())
            {
                // if this day is outside current month, grey it out
                tvDate.setTextColor(getResources().getColor(R.color.greyed_out));
            }
            else if (day == today.getDate())
            {
                // if it is today, set it to blue/bold
                tvDate.setTypeface(customLatoBold);
                tvDate.setTextColor(getResources().getColor(R.color.today));
            }

            // set text
            tvDate.setText(String.valueOf(date.getDate()));

            return view;
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler
    {
        void onDayLongPress(Date date);
        void onDateClick(Date date);
    }
}