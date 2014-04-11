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
import android.widget.GridView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DoctorIndexActivity extends ActionBarActivity {

    //private static final String DOCTORS_URL = "http://www.mdme.us/api/v1/patients/doctors.json";
    private static final String DOCTORS_URL = WebserverUrl.ROOT_URL + "/api/v1/patients/doctors.json";

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_index);
        setTitle(getIntent().getStringExtra("departmentName"));

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.doctor_index, menu);
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
            loadDoctorsFromAPI(DOCTORS_URL);
        }
        else
        {
            Intent intent = new Intent(DoctorIndexActivity.this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    private void loadDoctorsFromAPI(String url)
    {
        GetDoctorsTask getDoctorsTask = new GetDoctorsTask(DoctorIndexActivity.this);
        getDoctorsTask.setMessageLoading("Loading Doctors...");
        getDoctorsTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", "") +
                               "&name=" + getIntent().getStringExtra("departmentName"));
    }

    private class GetDoctorsTask extends UrlJsonAsyncTask
    {
        public GetDoctorsTask(Context context) { super(context); }

        @Override
        protected  void onPostExecute(JSONObject json)
        {
            try
            {
                //add doctors to list
                JSONArray jsonDoctors = json.getJSONObject("data").getJSONArray("doctors");
                int length = jsonDoctors.length();
                final List<Doctor> doctors = new ArrayList<Doctor>(length);

                for (int i = 0; i < length; i++)
                {
                    String name = jsonDoctors.getJSONObject(i).getString("full_name");
                    String imageUrl = WebserverUrl.ROOT_URL + jsonDoctors.getJSONObject(i).
                                                                getString("avatar_thumb_url");
                    int id = jsonDoctors.getJSONObject(i).getInt("id");
                    doctors.add(new Doctor(imageUrl, name, id));
                }

                //add to gridview
                final GridView gridview = (GridView)findViewById(R.id.doctor_index_grid_view);
                gridview.setAdapter(new DoctorAdapater(getApplicationContext(), doctors));

                //setup listener
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        int doctorId = ((Doctor)gridview.getAdapter().getItem(position)).getId();
                        Intent intent = new Intent(getApplicationContext(), DoctorShowActivity.class);
                        intent.putExtra("doctorId", doctorId);
                        startActivityForResult(intent, 0);
                    }
                });
            }
            catch(Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("DoctorIndexLoad", "Error loading department list", e);
            }
            finally
            {
                super.onPostExecute(json);
            }
        }

    }

}
