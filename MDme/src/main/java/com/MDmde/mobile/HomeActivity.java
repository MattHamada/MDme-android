package com.MDmde.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpRetryException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class HomeActivity extends ActionBarActivity {

    //private final static String TASKS_URL = "http://www.mdme.us/api/v1/patients.json";
    private final static String TASKS_URL = WebserverUrl.ROOT_URL + "/api/v1/patients.json";
    private final static String API_NOTIFICATIONS_URL =
            WebserverUrl.ROOT_URL + "/api/v1/patients/devices.json";


    private SharedPreferences mPreferences;

    //for gcm reg
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String TAG = "GCMSettings";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //google API project number
    String SENDER_ID = "570465868788";
    String regid;

    GoogleCloudMessaging gcm;
    AtomicInteger msgID = new AtomicInteger();
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        context = getApplicationContext();

        //check for play services APK for GCM registration
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            else {
                Log.i(TAG, "No valid google play services apk found");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        setTitle("Home");
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
            case R.id.menu_home_settings:
                //menu temp just turns on notifications, will open settingsActivity eventaully
               // turnOnNotifications(API_NOTIFICATIONS_URL);

//                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
//                startActivityForResult(intent, 0);, String title
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkPlayServices();

        //only load if still signed in
        if(mPreferences.contains("ApiToken"))
        {
            loadTasksFromAPI(TASKS_URL);
        }
        else
        {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Log.i(TAG, "Device does not support google play services.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration id not found");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "Application changed versions.");
            return "";
        }
        return registrationId;
    }

    //get applications shared preferences for gcm key
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(HomeActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     * Must be done with async as register() / unregister() are blocking
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device regsistered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error : " + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }



        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to MDME rails server over http.
     * Must have auth'd user for rails to assign device to user.
     */


    private void sendRegistrationIdToBackend() {
//        SendRegIdTask sendRegIdTask = new SendRegIdTask(HomeActivity.this);
//        sendRegIdTask.setMessageLoading("Synching settings...");
//        sendRegIdTask.execute(
//                API_NOTIFICATIONS_URL + "?api_token=" + mPreferences.getString("ApiToken", ""));

        // TODO this would not work before inside UrlJsonAsyncTask, figure out why.

        //onPreExecture
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(API_NOTIFICATIONS_URL + "?api_token=" + mPreferences.getString("ApiToken", ""));
        JSONObject holder = new JSONObject();
        JSONObject deviceObj = new JSONObject();
        String response = null;
        JSONObject json = new JSONObject();

        try {
            try {
                //fake return values incase no connection
                json.put("success", false);
                json.put("info", "Could not connect to server.");

                deviceObj.put("patient_id", mPreferences.getString("patientId", ""));
                deviceObj.put("platform", "android");
                deviceObj.put("token", regid);
                holder.put("device", deviceObj);
                holder.put("api_token", mPreferences.getString("ApiToken", ""));

                StringEntity se = new StringEntity(holder.toString());
                post.setEntity(se);

                //setup headers
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-Type", "application/json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(post, responseHandler);
                json = new JSONObject(response);
            }
            catch (HttpRetryException e) {
                e.printStackTrace();
                Log.e("ClientProtocol", e.toString());
                json.put("info", "Email and/or password are invalid");
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.e("IO", "" + e);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", ""+e);
        }

        //onPostExecute
        try {
            if (!json.getBoolean("success")) {
                Toast.makeText(context, "Error synching settings", Toast.LENGTH_LONG);
            }
        }
        catch (Exception e) {
            Log.e("Error occured: ", e.getMessage());
        }
    }

    /**
     * Will store gcm registration id in shared preferences
     * @param context application's context
     * @param regId registration id for gcm
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regID on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void loadTasksFromAPI(String url)
    {
        GetTasksTask getTasksTask = new GetTasksTask(HomeActivity.this);
        getTasksTask.setMessageLoading("Loading...");
        getTasksTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
    }

//    private class SendRegIdTask extends UrlJsonAsyncTask
//    {
//        public SendRegIdTask(Context context) { super(context); }
//
//        @Override
//        protected  JSONObject doInBackground(String... urls) {
//            DefaultHttpClient client = new DefaultHttpClient();
//            HttpPost post = new HttpPost(urls[0]);
//            JSONObject holder = new JSONObject();
//            JSONObject deviceObj = new JSONObject();
//            String response = null;
//            JSONObject json = new JSONObject();
//
//            try {
//                try {
//                    //fake return values incase no connection
//                    json.put("success", false);
//                    json.put("info", "Could not connect to server.");
//
//                    deviceObj.put("patient_id", mPreferences.getString("patientId", ""));
//                    deviceObj.put("platform", "android");
//                    deviceObj.put("token", regid);
//                    holder.put("device", deviceObj);
//                    holder.put("api_token", mPreferences.getString("ApiToken", ""));
//
//                    StringEntity se = new StringEntity(holder.toString());
//                    post.setEntity(se);
//
//                    //setup headers
//                    post.setHeader("Accept", "application/json");
//                    post.setHeader("Content-Type", "application/json");
//
//                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
//                    response = client.execute(post, responseHandler);
//                    json = new JSONObject(response);
//                }
//                catch (HttpRetryException e) {
//                    e.printStackTrace();
//                    Log.e("ClientProtocol", e.toString());
//                    json.put("info", "Email and/or password are invalid");
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e("IO", "" + e);
//                }
//            }
//            catch (JSONException e) {
//                e.printStackTrace();
//                Log.e("JSON", ""+e);
//            }
//            return json;
//        }
//
//        @Override
//        protected void onPostExecute(JSONObject json)
//        {
//            try {
//                if (!json.getBoolean("success")) {
//                    Toast.makeText(context, "Error synching settings", Toast.LENGTH_LONG);
//                }
//            }
//            catch (Exception e) {
//                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//            finally {
//                super.onPostExecute(json);
//            }
//        }
//    }

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
                //add task names to list
                JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("tasks");
                int length = jsonTasks.length();
                List<String> taskTitles = new ArrayList<String>(length);

                for (int i = 0; i < length; i++)
                {
                    taskTitles.add(jsonTasks.getJSONObject(i).getString("title"));
                }

                //add list of string to listview
                ListView tasksListView = (ListView) findViewById((R.id.tasks_list_view));
                if (tasksListView != null)
                {
                    tasksListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                            android.R.layout.simple_list_item_1, taskTitles));
                    //add listener for choosing an item in list
                    tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            switch (position)
                            {
                                case 0:
                                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    Intent intent1 = new Intent(getApplicationContext(), AppointmentMenuActivity.class);
                                    startActivity(intent1);
                                    break;
                                case 2:
                                    Intent intent2 = new Intent(getApplicationContext(), ClinicsIndexActivity.class);
                                    startActivity(intent2);
                                    break;
                                case 3:
                                    SharedPreferences.Editor editor = mPreferences.edit();
                                    //reset auth token
                                    editor.remove("ApiToken");
                                    editor.commit();
                                    //launch Login Activity
                                    Intent intent3 = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent3);
                                    finish();
                                    break;
                            }
                        }
                    });
                }
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
