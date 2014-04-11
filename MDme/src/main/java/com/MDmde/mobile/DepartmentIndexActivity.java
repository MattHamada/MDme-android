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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DepartmentIndexActivity extends ActionBarActivity {

    //private final static String DEPARTMENTS_URL = "http://www.mdme.us:3000/api/v1/patients/doctors/departments.json";
    private final static String DEPARTMENTS_URL = WebserverUrl.ROOT_URL + "/api/v1/patients/doctors/departments.json";

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_index);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.department_index, menu);
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

    @Override
    public void onResume()
    {
        super.onResume();

        //only load if still signed in
        if(mPreferences.contains("ApiToken"))
        {
            loadTasksFromAPI(DEPARTMENTS_URL);
        }
        else
        {
            Intent intent = new Intent(DepartmentIndexActivity.this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    private void loadTasksFromAPI(String url)
    {
        GetDepartmentsTask getDepartmentsTask = new GetDepartmentsTask(DepartmentIndexActivity.this);
        getDepartmentsTask.setMessageLoading("Loading Departments...");
        getDepartmentsTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
    }

    private class GetDepartmentsTask extends UrlJsonAsyncTask
    {
        public GetDepartmentsTask(Context context) { super(context); }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            try
            {
                //make sure api key still correct
                Boolean validApi = json.getBoolean("success");
                if (!validApi)
                {
                    SharedPreferences.Editor editor = mPreferences.edit();
                    //reset auth token
                    editor.remove("ApiToken");
                    editor.commit();
                    //launch Login Activity
                    Intent intent3 = new Intent(getApplicationContext(), WelcomeActivity.class);
                    startActivity(intent3);
                    finish();
                }

                //add department names to list
                JSONArray jsonDepartments = json.getJSONObject("data").getJSONArray("departments");
                int length = jsonDepartments.length();
                final List<String> departmentNames = new ArrayList<String>(length);

                for (int i = 0; i < length; i++)
                {
                    departmentNames.add(jsonDepartments.getJSONObject(i).getString("name"));
                }

                //add list to listview
                final ListView departmentsListView = (ListView)findViewById(R.id.department_index_list_view);
                if (departmentsListView != null)
                {
                    departmentsListView.setAdapter(new ArrayAdapter<String>(DepartmentIndexActivity.this,
                            android.R.layout.simple_list_item_1, departmentNames));
                    //add listeners to items
                    departmentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            String departmentName = departmentsListView.getAdapter().getItem(position).toString();
                            Intent intent = new Intent(getApplicationContext(), DoctorIndexActivity.class);
                            intent.putExtra("departmentName", departmentName);
                            startActivity(intent);
                        }
                    });
                }
            }
            catch(Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("DepartmentIndexLoad", "Error loading department list", e);
            }
            finally
            {
                super.onPostExecute(json);
            }
        }
    }

}
