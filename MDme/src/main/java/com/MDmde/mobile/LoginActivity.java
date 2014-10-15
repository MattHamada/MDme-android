package com.MDmde.mobile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class LoginActivity extends ActionBarActivity {

//    private final static String LOGIN_API_ENDPOINT_URL = "http://www.mdme.us/api/v1/sessions.json";
    private final static String LOGIN_API_ENDPOINT_URL = WebserverUrl.ROOT_URL + "/api/v1/sessions.json";
    private SharedPreferences mPreferences;
    private String mUserEmail;
    private String mUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        int playservAvail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (playservAvail != ConnectionResult.SUCCESS)
        {
            Dialog playDialog = GooglePlayServicesUtil.getErrorDialog(playservAvail, this, 0);
            playDialog.show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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

    public void login(View button)
    {
        EditText userEmailField = (EditText)findViewById(R.id.userEmail);
        mUserEmail = userEmailField.getText().toString();
        EditText userPasswordField = (EditText)findViewById(R.id.userPassword);
        mUserPassword = userPasswordField.getText().toString();

        if (mUserEmail.length() == 0 || mUserPassword.length() == 0)
        {
            Toast.makeText(this, "Fields cannot be blank", Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            LoginTask loginTask = new LoginTask(LoginActivity.this);
            loginTask.setMessageLoading("Logging in...");
            loginTask.execute(LOGIN_API_ENDPOINT_URL);
        }
    }

    private class LoginTask extends UrlJsonAsyncTask
    {
        public LoginTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try
            {
                try
                {
                    //setup return values incase something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    //Add login info to params
                    userObj.put("email", mUserEmail);
                    userObj.put("password", mUserPassword);
                    holder.put("patient", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    //setup request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);
                }
                catch (HttpResponseException e)
                {
                    e.printStackTrace();
                    Log.e("ClientProtocol", e.toString());
                    json.put("info", "Email and/or password are invalid. Retry!");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Log.e("JSON", ""+ e);
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            try
            {
                if (json.getBoolean("success"))
                {
                    //everything ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    //save auth token to shared prefs
                    editor.putString("ApiToken", json.getJSONObject("data").getString("api_token"));
                    editor.commit();

                    //launch home activity, close this one
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            }
            catch (Exception e)
            {
                //show toast with exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally
            {
                super.onPostExecute(json);
            }
        }
    }

}
