package com.example.kaylie.project.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.kaylie.project.Fragments.CalendarFragment;
import com.example.kaylie.project.Fragments.MapsFragment;
import com.example.kaylie.project.Fragments.TaskListFragment;

import java.util.Date;

/**
 * Created by claireshu on 7/7/16.
 */
public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Maps", "Calendar", "Lists" };
    private Context context;
    private Date date;

    public HomeFragmentPagerAdapter(FragmentManager fm, Context context, Date date) {
        super(fm);
        this.context = context;
        this.date= date;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MapsFragment();
            case 1:
                return new CalendarFragment().newInstance(date);
            case 2:
                return new TaskListFragment();
            default:
                return new MapsFragment();
        }

    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
