package com.MDmde.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONObject;


public class DoctorShowActivity extends ActionBarActivity {

    private int doctorId;
    private SharedPreferences mPreferences;
    private ImageView mProfileImage;

    //full url with doctor id generated in onResume
    private final String PROFILE_URL = WebserverUrl.ROOT_URL +
            "/api/v1/patients/doctors/";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_show);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        mProfileImage = (ImageView)findViewById(R.id.doctor_profile_image);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.doctor_show, menu);
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
        //load api token, redrect if missing
        if(mPreferences.contains("ApiToken"))
        {
            doctorId = getIntent().getIntExtra("doctorId", -1);
            loadDoctorProfileFromAPI(PROFILE_URL + doctorId + ".json");
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    private void loadDoctorProfileFromAPI(String url)
    {
        GetDoctorProfileTask getProfileTask = new GetDoctorProfileTask(DoctorShowActivity.this);
        getProfileTask.setMessageLoading("Loading...");
        getProfileTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
    }

    private class GetDoctorProfileTask extends UrlJsonAsyncTask
    {
        public GetDoctorProfileTask(Context context) { super(context); }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            try
            {
                //get json values
                String fullName = json.getJSONObject("data").getJSONObject("doctor").getString("full_name");
                String education = json.getJSONObject("data").getJSONObject("doctor").getString("education");
                String description = json.getJSONObject("data").getJSONObject("doctor").getString("description");
                String pictureUrl = json.getJSONObject("data").getJSONObject("doctor").getString("avatar_medium_url");
                String departmentName = json.getJSONObject("data").getJSONObject("doctor").getString("department_name");

                setTitle(fullName);

                //assign to view
                TextView fullNameText = (TextView)findViewById(R.id.doctor_profile_full_name);
                fullNameText.setText(fullName);
                TextView educationText = (TextView)findViewById(R.id.doctor_profile_education);
                educationText.setText(education);
                TextView departmentText = (TextView)findViewById(R.id.doctor_profile_department_name);
                departmentText.setText(departmentName);
                TextView descriptionText = (TextView)findViewById(R.id.doctor_profile_description);
                descriptionText.setText(description);

                new DownloadImageTask(mProfileImage).execute(WebserverUrl.ROOT_URL + pictureUrl);
            }
            catch(Exception e)
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
