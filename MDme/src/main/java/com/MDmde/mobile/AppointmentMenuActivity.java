package com.MDmde.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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


public class AppointmentMenuActivity extends ActionBarActivity {

    private final String APPOINTMENT_MENU_URL = WebserverUrl.ROOT_URL + "/api/v1/patients/appointments/tasks.json";
    private SharedPreferences mPreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Appointments Menu");
        setContentView(R.layout.activity_appointment_menu);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.appointment_menu, menu);
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
            loadTasksFromAPI(APPOINTMENT_MENU_URL);
        }
        else
        {
            Intent intent = new Intent(AppointmentMenuActivity.this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    private void loadTasksFromAPI(String url)
    {
        GetAppointmentMenuTask getAppointmentMenuTask = new GetAppointmentMenuTask(AppointmentMenuActivity.this);
        getAppointmentMenuTask.setMessageLoading("Loading...");
        getAppointmentMenuTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
    }

    private class GetAppointmentMenuTask extends UrlJsonAsyncTask
    {
        public GetAppointmentMenuTask(Context context) { super(context); }

        @Override
        public void onPostExecute(JSONObject json)
        {
            try
            {
                //get task names add to a list
                JSONArray jsonMenuOptions = json.getJSONObject("data").getJSONArray("tasks");
                int length = jsonMenuOptions.length();
                final List<String> menuOptions = new ArrayList<String>(length);
                for (int i = 0; i < length; i++)
                {
                    menuOptions.add(jsonMenuOptions.getJSONObject(i).getString("title"));
                }

                //add to listview
                ListView menuListView = (ListView)findViewById(R.id.appointment_menu_list_view);
                if (menuListView != null)
                {
                    menuListView.setAdapter(new ArrayAdapter<String>(AppointmentMenuActivity.this,
                            android.R.layout.simple_list_item_1, menuOptions));
                    menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            switch(position)
                            {
                                case 0:
                                    Intent intent = new Intent(getApplicationContext(), ConfirmedAppointmentsActivity.class);
                                    startActivityForResult(intent, 0);
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                            }
                        }
                    });
                }
            }
            catch (Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally
            {
                super.onPostExecute(json);
            }
        }
    }

}
