package com.MDmde.mobile;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends ActionBarActivity {

    private final static String TASKS_URL = "http://www.mdme.us/api/v1/tasks.json";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loadTasksFromAPI(TASKS_URL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTasksFromAPI(String url)
    {
        GetTasksTask getTasksTask = new GetTasksTask(HomeActivity.this);
        getTasksTask.setMessageLoading("Loading tasks");
        getTasksTask.execute(url);
    }

    private class GetTasksTask extends UrlJsonAsyncTask
    {
        public GetTasksTask(Context context)
        {
            super(context);
        }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            try
            {
                JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("Tasks");
                int length = jsonTasks.length();
                List<String> taskTitles = new ArrayList<String>(length);

                for (int i = 0; i < length; i++)
                {
                    taskTitles.add(jsonTasks.getJSONObject(i).getString("title"));
                }

                ListView tasksListView = (ListView)findViewById((R.id.tasks_list_view));
                if (tasksListView != null)
                {
                    tasksListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                                        android.R.layout.simple_list_item_1, taskTitles));
                }
            }
            catch (Exception e)
            {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            finally
            {
                super.onPostExecute(json);
            }
        }
    }

}
