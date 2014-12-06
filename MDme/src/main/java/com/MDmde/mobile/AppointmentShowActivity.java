package com.MDmde.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONObject;

public class AppointmentShowActivity extends ActionBarActivity {

    private final String APPOINTMENT_SHOW_URL =
            WebserverUrl.ROOT_URL + "/api/v1/patients/appointments/";

    private SharedPreferences mPreferences;
    private ImageView mDoctorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_show);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        mDoctorImage = (ImageView)findViewById(R.id.appointment_show_doctor_image);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_appointment_show, menu);
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

        if (mPreferences.contains("ApiToken")) {
            int appointmentId = getIntent().getIntExtra("appointment_id", -1);
            loadAppointmentFromApi(APPOINTMENT_SHOW_URL + appointmentId);
        }
        else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        }
    }

    private void loadAppointmentFromApi(String url) {
        GetAppointmentTask getAppointmentTask =
                new GetAppointmentTask(AppointmentShowActivity.this);
        getAppointmentTask.setMessageLoading("Loading...");
        getAppointmentTask.execute(url + ".json?api_token=" + mPreferences.getString("ApiToken", ""));
    }

    private class GetAppointmentTask extends UrlJsonAsyncTask {
        public GetAppointmentTask(Context context) { super(context); }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                //parse json
                String clinicName = json.getJSONObject("data").getJSONObject("appointment")
                        .getString("clinic_name");
                String thumbUrl = json.getJSONObject("data").getJSONObject("appointment")
                        .getString("doctor_avatar_thumb_url");
                String date = json.getJSONObject("data").getJSONObject("appointment")
                        .getString("date");
                String time = json.getJSONObject("data").getJSONObject("appointment")
                        .getString("delayed_time_ampm");
                String doctorName = json.getJSONObject("data").getJSONObject("appointment")
                        .getString("doctor_full_name");
                String description = json.getJSONObject("data").getJSONObject("appointment")
                        .getString("description");
                boolean request = json.getJSONObject("data").getJSONObject("appointment")
                        .getBoolean("request");
                String status = request ? "Requested" : "Confirmed";

                setTitle(clinicName);

                //set view
                TextView clinicNameText = (TextView)findViewById(R.id.appointment_show_clinic_name);
                clinicNameText.setText(clinicName);
                TextView dateText = (TextView)findViewById(R.id.appointment_show_date);
                dateText.setText(date);
                TextView timeText = (TextView)findViewById(R.id.appointment_show_time);
                timeText.setText(time);
                TextView doctorNameText = (TextView)findViewById(R.id.appointment_show_doctor);
                doctorNameText.setText(doctorName);
                TextView statusText = (TextView)findViewById(R.id.appointment_show_confirmed);
                statusText.setText(status);
                TextView descriptionText = (TextView)findViewById(R.id.appointment_show_description);
                descriptionText.setText(description);

                new DownloadImageTask(mDoctorImage).execute(WebserverUrl.ROOT_URL + thumbUrl);
            }
            catch (Exception e) {
                Toast.makeText(context, "Error loading appointment", Toast.LENGTH_LONG).show();
            }
            finally {
                super.onPostExecute(json);
            }
        }
    }
}
