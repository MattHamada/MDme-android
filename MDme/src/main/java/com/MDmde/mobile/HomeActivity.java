package com.MDmde.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends ActionBarActivity {

    private final static String TASKS_URL = "http://www.mdme.us/api/v1/patients.json";
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //loadTasksFromAPI(TASKS_URL);
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
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
        switch(id)
        {
            case R.id.menu_refresh:
                loadTasksFromAPI(TASKS_URL);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onResume()
    {
        super.onResume();

        //only load if still signed in
        if(mPreferences.contains("ApiToken"))
        {
            loadTasksFromAPI(TASKS_URL);
        }
        else
        {
            Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    private void loadTasksFromAPI(String url)
    {
        GetTasksTask getTasksTask = new GetTasksTask(HomeActivity.this);
        getTasksTask.setMessageLoading("Loading tasks");
        getTasksTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
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
                //add task names t list
                JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("tasks");
                int length = jsonTasks.length();
                List<String> taskTitles = new ArrayList<String>(length);

                for (int i = 0; i < length; i++)
                {
                    taskTitles.add(jsonTasks.getJSONObject(i).getString("title"));
                }

                //add list of string to listview
                ListView tasksListView = (ListView)findViewById((R.id.tasks_list_view));
                if (tasksListView != null)
                {
                    tasksListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                                        android.R.layout.simple_list_item_1, taskTitles));
                }
                //add listener for choosing an item in list
                tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        switch(position)
                        {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                SharedPreferences.Editor editor = mPreferences.edit();
                                //reset auth token
                                editor.remove("ApiToken");
                                editor.commit();
                                //launch Login Activity
                                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                                startActivity(intent);
                                finish();

                        }
                    }
                });
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
