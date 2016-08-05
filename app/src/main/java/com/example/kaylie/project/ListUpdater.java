package com.example.kaylie.project;

import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kaylie on 7/29/16.
 */
public class ListUpdater {

    private static ListUpdater sInstance;
    private List<TaskUpdateListener> mListeners;
    private List<ParseObject> mTasks;

    public interface TaskUpdateListener {
        void onTaskAdded(ParseObject task);
        void onTaskRemoved(ParseObject task);
    }

    public void addListener(TaskUpdateListener listener) {
        mListeners.add(listener);
    }

    public static synchronized ListUpdater getInstance(){

        if(sInstance == null)
            sInstance = new ListUpdater();

        return sInstance;
    }

    private ListUpdater(){

        mListeners = new LinkedList<>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereEqualTo("fb_user", Profile.getCurrentProfile().toString());
        query.orderByDescending("createdAt");
        try {
            mTasks = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void addTask(ParseObject task) {
        mTasks.add(task);
        // Add task to server and notify the listeners

        for(TaskUpdateListener listener : mListeners) {
            listener.onTaskAdded(task);
        }
    }

    public void removeTask(ParseObject task) {
        mTasks.remove(task);

        // save on the server

        for(TaskUpdateListener listener : mListeners) {
            listener.onTaskAdded(task);
        }
    }

    public List<ParseObject> getTasks() {
        return mTasks;
    }
}
