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

public class RequestedAppointmentsActivity extends ActionBarActivity {

    private final String REQUESTED_APPOINTMENTS_URL = WebserverUrl.ROOT_URL + "/api/v1/patients/appointments/requested.json";

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requested_appointments);
        setTitle("Pending Appointments");

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_requested_appointments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPreferences.contains("ApiToken"))
        {
            loadAppointmentsFromAPI(REQUESTED_APPOINTMENTS_URL);
        }
        else {
            Intent intent = new Intent(RequestedAppointmentsActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void loadAppointmentsFromAPI(String url) {
        GetAppointmentsTask getAppointmentsTask = new GetAppointmentsTask(RequestedAppointmentsActivity.this);
        getAppointmentsTask.setMessageLoading("Loading Appointments...");
        getAppointmentsTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
    }

    private class GetAppointmentsTask extends UrlJsonAsyncTask {
        public GetAppointmentsTask(Context context) { super(context); }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            try
            {
                //add appointment times to list
                JSONArray jsonAppointments = json.getJSONObject("data").getJSONArray("appointments");
                int length = jsonAppointments.length();
                final List<Appointment> appointments = new ArrayList<Appointment>(length);
                if (length == 0)
                {
                    Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
                }
                for (int i = 0; i < length; i++)
                {
                    int id = jsonAppointments.getJSONObject(i).getInt("id");
                    String time = jsonAppointments.getJSONObject(i).getString("delayed_date_time_ampm");
                    String description = jsonAppointments.getJSONObject(i).getString("description");
                    String doctorName  = jsonAppointments.getJSONObject(i).getString("doctor_full_name");
                    int docId = jsonAppointments.getJSONObject(i).getInt("doctor_id");
                    Appointment apt = new Appointment(id, time, description, doctorName, docId);
                    appointments.add(apt);
                }

                //add to listview
                final ListView appointmentsListView = (ListView)findViewById(R.id.requested_appointments_list_view);
                if (appointmentsListView != null)
                {
                    appointmentsListView.setAdapter(new ArrayAdapter<Appointment>(RequestedAppointmentsActivity.this,
                            android.R.layout.simple_list_item_1, appointments));

                    //add listener
                    appointmentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            int appointmentId = ((Appointment)appointmentsListView
                                    .getAdapter().getItem(position)).getId();
                            Intent intent = new Intent(getApplicationContext(),
                                    AppointmentShowActivity.class);
                            intent.putExtra("appointment_id", appointmentId);
                            startActivity(intent);
                        }
                    });
                }
            }
            catch (Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("RequestedAppointmentsIndex", "Error loading appointments", e);
            }
            finally
            {
                super.onPostExecute(json);
            }

        }
    }

}
