package com.MDmde.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONObject;


public class ClinicActivity extends ActionBarActivity {

    private final String CLINIC_URL = WebserverUrl.ROOT_URL + "/api/v1/patients/clinic/";
    private MapView mClinicMapView;
    private SharedPreferences mPreferences;
    private Clinic mClinic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        mClinic = (Clinic)getIntent().getSerializableExtra("clinic");

        mClinicMapView = (MapView) findViewById(R.id.clinic_map);
        mClinicMapView.onCreate(savedInstanceState);
        final GoogleMap map = mClinicMapView.getMap();
        configureMap(map);

    }

    private void configureMap(final GoogleMap map) {
        if (map == null) {
            return; //google maps not available
        }
        MapsInitializer.initialize(ClinicActivity.this);
        map.setMyLocationEnabled(true);
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (mClinic != null) {
                    LatLngBounds clinicLatLng = new LatLngBounds(
                            new LatLng(Double.parseDouble(mClinic.getSw_latitude()),
                                    Double.parseDouble(mClinic.getSw_longitude())),
                            new LatLng(Double.parseDouble(mClinic.getNe_latitude()),
                                    Double.parseDouble(mClinic.getNe_longitude()))
                    );
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(clinicLatLng, 0));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mClinicMapView.onResume();

        if (mPreferences.contains("ApiToken")) {
            if (mClinic != null) {
                TextView mAdr1TextView = (TextView)findViewById(R.id.clinic_profile_address1);
                mAdr1TextView.setText(mClinic.getAddress1());
                TextView mAdr2TextView = (TextView)findViewById(R.id.clinic_profile_address2);
                mAdr2TextView.setText(mClinic.getAddress2());
                TextView mAdr3TextView = (TextView)findViewById(R.id.clinic_profile_address3);
                mAdr3TextView.setText(mClinic.getAddress3());

                //TODO: handle international addresses (no states etc)
                TextView mCityStateTextView =
                        (TextView)findViewById(R.id.clinic_profile_city_state);
                String cityState = mClinic.getCity() + ", " + mClinic.getState();
                mAdr3TextView.setText(cityState);

                TextView mCountry = (TextView)findViewById(R.id.clinic_profile_country);
                mAdr3TextView.setText(mClinic.getCountry());
                TextView mZipcode = (TextView)findViewById(R.id.clinic_profile_zipcode);
                mAdr3TextView.setText(mClinic.getZipcode());
            }
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mClinicMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClinicMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mClinicMapView.onLowMemory();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.clinic, menu);
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
}
