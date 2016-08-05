package com.example.kaylie.project;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.kaylie.project.Adapters.TasksAdapter;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    List<ParseObject> mTaskList;
    TasksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        int code= getIntent().getIntExtra("code", 0);
        if(code == 100){
           String placeName= getIntent().getStringExtra("place");
            mTaskList= searchLocations(placeName);
        }
        else {
            String query = getIntent().getStringExtra("query");
            mTaskList = searchTasks(query);
        }

        ListView lvSearchResults= (ListView) findViewById(R.id.lvSearchResults);
        adapter = new TasksAdapter(this, mTaskList);
        adapter.notifyDataSetChanged();
        lvSearchResults.setAdapter(adapter);
    }

    public List<ParseObject> searchLocations(String placeName){
            List<ParseObject> taskList = null;
            ParseQuery<ParseObject> query= ParseQuery.getQuery("Task");
            query.fromLocalDatastore();
            query.whereExists("place_name");
            query.whereContains("place_name", placeName);
            try {
                taskList = query.find();
            }
            catch(ParseException e) {
                e.printStackTrace();
            }
            return taskList;
    }

    public List<ParseObject> searchTasks(String searchQuery){
        List<ParseObject> taskList = null;
        ParseQuery<ParseObject> query= ParseQuery.getQuery("Task");
        query.fromLocalDatastore();
        query.whereContains("name", searchQuery);
//        query.whereContains("description", searchQuery);
        try {
            taskList = query.find();
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
        return taskList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                mTaskList= searchTasks(query);
                adapter.notifyDataSetChanged();
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


}
