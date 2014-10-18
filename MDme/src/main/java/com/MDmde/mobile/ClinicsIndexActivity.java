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
                final List<Clinic> clinics = new ArrayList<Clinic>(length);
                for (int i = 0; i < length; i++) {
                    int id = jsonClinics.getJSONObject(i).getInt("id");
                    String name = jsonClinics.getJSONObject(i).getString("name");
                    String address1 = jsonClinics.getJSONObject(i).getString("address1");
                    String address2 = jsonClinics.getJSONObject(i).getString("address2");
                    String address3 = jsonClinics.getJSONObject(i).getString("address3");
                    String city = jsonClinics.getJSONObject(i).getString("city");
                    String state = jsonClinics.getJSONObject(i).getString("state");
                    String country = jsonClinics.getJSONObject(i).getString("country");
                    String zipcode = jsonClinics.getJSONObject(i).getString("zipcode");
                    String phone_number = jsonClinics.getJSONObject(i).getString("phone_number");
                    String fax_number = jsonClinics.getJSONObject(i).getString("fax_number");
                    String ne_latitude = jsonClinics.getJSONObject(i).getString("ne_latitude");
                    String ne_longitude = jsonClinics.getJSONObject(i).getString("ne_longitude");
                    String sw_latitude = jsonClinics.getJSONObject(i).getString("sw_latitude");
                    String sw_longitude = jsonClinics.getJSONObject(i).getString("sw_longitude");

                    clinics.add(new Clinic(id, name, address1, address2, address3, city, state,
                                           country, zipcode, phone_number, fax_number, ne_latitude,
                                           ne_longitude, sw_latitude, sw_longitude));
                }

                //add list to listview
                final ListView clinicsListView = (ListView)findViewById(R.id.clinicsindex_list_view);
                if (clinicsListView != null)
                {
                    clinicsListView.setAdapter(new ArrayAdapter<Clinic>(ClinicsIndexActivity.this,
                            android.R.layout.simple_list_item_1, clinics));
                    //add listeners to each item
                    clinicsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String clinicName = clinicsListView.getAdapter().getItem(position).toString();
                            Intent intent = new Intent(getApplicationContext(), ClinicActivity.class);
                            intent.putExtra("clinic", ((Clinic)clinicsListView.getAdapter().
                                                                                getItem(position)));
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
