package com.MDmde.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;


public class UpdateProfileActivity extends ActionBarActivity {

//    private final String UPDATE_PROFILE_URL = "http://www.mdme.us/api/v1/patients/update";
    private final String UPDATE_PROFILE_URL = WebserverUrl.ROOT_URL + "/api/v1/patients/update";
    private static final int REQUEST_PHOTO = 0;

    private SharedPreferences mPreferences;

    private EditText mFirstNameField;
    private EditText mLastNameField;
    private EditText mPhoneNumberField;
    private EditText mEmailField;
    private ImageView mProfileImage;
    private ImageButton mPhotoButton;
    private int orientation;
    private Photo mPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        mPhotoButton = (ImageButton)findViewById(R.id.update_profile_camera_button);
        mPhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(getApplicationContext(), ProfileCameraActivity.class);
                startActivityForResult(i, REQUEST_PHOTO);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.update_profile, menu);
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
        mFirstNameField =   (EditText)findViewById(R.id.first_name_edit);
        mLastNameField =    (EditText)findViewById(R.id.last_name_edit);
        mEmailField =       (EditText)findViewById(R.id.email_edit);
        mPhoneNumberField = (EditText)findViewById(R.id.phone_number_edit);
        mProfileImage =     (ImageView)findViewById(R.id.edit_profile_image);

        //populate fields with current data
        Intent intent = getIntent();
        mFirstNameField.setText(intent.getStringExtra("firstName"));
        mLastNameField.setText(intent.getStringExtra("lastName"));
        mPhoneNumberField.setText(intent.getStringExtra("phoneNumber"));
        mEmailField.setText(intent.getStringExtra("email"));

        //load image
        Bundle extras = getIntent().getBundleExtra("profileBundle");
        try
        {
            //check if user took new photo - avoids overwriting new photo preview
            if (mPhoto == null)
            {
                Bitmap bmp = extras.getParcelable("profileImage");
                mProfileImage.setImageBitmap(bmp);
            }
        }
        catch(Exception e)
        {
            Log.e("ProfileImage", "No image to load", e);
        }


        Button mUpdateButton = (Button)findViewById(R.id.update_profile_button);
        mUpdateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateProfileTask updateProfileTask = new UpdateProfileTask(UpdateProfileActivity.this);
                updateProfileTask.setMessageLoading("Updating profile....");
                updateProfileTask.execute(UPDATE_PROFILE_URL + "?api_token=" + mPreferences.getString("ApiToken", ""));
            }
        });

        mProfileImage.invalidate();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != Activity.RESULT_OK) return;

        switch(requestCode)
        {
            case REQUEST_PHOTO:
                String filename = data.getStringExtra(ProfileCameraActivity.EXTRA_PHOTO_FILENAME);
                orientation = data.getIntExtra(ProfileCameraActivity.EXTRA_ORIENTATION, Surface.ROTATION_270);
                if (filename != null)
                {
                    Photo p = new Photo(filename, orientation);
                    p.setOrientation(orientation);
                    showPhoto(p);

                }
        }
    }

    private void showPhoto(Photo p)
    {
        BitmapDrawable b = null;
        if (p != null)
        {
            int mOrientation = p.getOrientation();
            String path = getFileStreamPath(p.getFIlename()).getAbsolutePath();
            b = PictureUtils.getScaledDrawable(this, path);

            if (p.getOrientation() == Configuration.ORIENTATION_PORTRAIT)
            {
                b = PictureUtils.getPortraitDrawable(mProfileImage, b);
            }
        }
        mPhoto = p;
        mProfileImage.setImageDrawable(b);
    }

    private class UpdateProfileTask extends UrlJsonAsyncTask
    {
        public UpdateProfileTask(Context context) { super(context); }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPut put = new HttpPut(urls[0]);

            MultipartEntityBuilder build = MultipartEntityBuilder.create();
            if (mPhoto != null)
            {
                File imgFile = new File(getFileStreamPath(mPhoto.getFIlename()).getAbsolutePath());
                build.addPart("patient[avatar]", new FileBody(imgFile));
                HttpEntity ent = build.build();
                put.setEntity(ent);
                //submit photo first
                //TODO see if possible to combine both put requests
                try
                {
                    HttpResponse response = client.execute(put);
                }
                catch(Exception e)
                {
                    Log.e("ImagePut", "Errors submiting image", e);
                }
            }

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
                    //add new params
                    userObj.put("first_name", mFirstNameField.getText());
                    userObj.put("last_name", mLastNameField.getText());
                    userObj.put("email", mEmailField.getText());
                    userObj.put("phone_number", mPhoneNumberField.getText());
                    holder.put("patient", userObj);
                    //setup request headers
                    put.setHeader("Accept", "application/json");
                    put.setHeader("Content-Type", "application/json");
                    StringEntity se = new StringEntity(holder.toString());
                    put.setEntity(se);


                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(put, responseHandler);
                    json = new JSONObject(response);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Log.e("IO", e.toString());
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                Log.e("JSON", e.toString());
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
//                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
//                    startActivity(intent);
                    mPhoto.deletePhoto();
                    finish();
                    Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
                }
                else
                {
                    Iterator<?> errorKeys = json.getJSONObject("data").keys();
                    while (errorKeys.hasNext())
                    {
                        //print out error messages returned from api
                        String key = (String)errorKeys.next();
                        JSONArray tempArray = json.getJSONObject("data").getJSONArray(key);
                        for(int i = 0; i < tempArray.length(); i++)
                        {
                            String message = tempArray.getString(i);
                            Toast.makeText(context, key + " : " + message, Toast.LENGTH_LONG).show();
                        }
                    }

                }
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
