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


public class ClinicsIndexActivity extends ActionBarActivity {

    private final String CLINICS_URL = WebserverUrl.ROOT_URL +"/api/v1/patients/clinics.json";
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinics_index);
        setTitle("My Clinics");
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.clinics_index, menu);
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
        if (mPreferences.contains("ApiToken"))
        {
            loadTasksFromAPI(CLINICS_URL);
        }
        else
        {
            Intent intent = new Intent(ClinicsIndexActivity.this, LoginActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    private void loadTasksFromAPI(String url)
    {
       GetClinicsTask getClinicsTask = new GetClinicsTask(ClinicsIndexActivity.this);
       getClinicsTask.setMessageLoading("Loading Clinics...");
        getClinicsTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
    }

    private class GetClinicsTask extends UrlJsonAsyncTask
    {
        public GetClinicsTask(Context context) { super(context); }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            try {
                //add clinic names to list
                JSONArray jsonClinics = json.getJSONObject("data").getJSONArray("clinics");
                int length = jsonClinics.length();
                final List<String> clinicNames = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    clinicNames.add(jsonClinics.getJSONObject(i).getString("name"));
                }

                //add list to listview
                final ListView clinicsListView = (ListView)findViewById(R.id.clinicsindex_list_view);
                if (clinicsListView != null)
                {
                    clinicsListView.setAdapter(new ArrayAdapter<String>(ClinicsIndexActivity.this,
                            android.R.layout.simple_list_item_1, clinicNames));
                    //add listeners to each item
                    clinicsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String clinicName = clinicsListView.getAdapter().getItem(position).toString();
                            Intent intent = new Intent(getApplicationContext(), ClinicActivity.class);
                            intent.putExtra("clinicName", clinicName);
                            startActivity(intent);
                        }
                    });
                }

            }
            catch (Exception e)
            {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ClinicsIndexLoad", "Error loading clinics list", e);
            }
            finally
            {
                super.onPostExecute(json);
            }
        }
    }
}
