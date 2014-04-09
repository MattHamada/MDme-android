package com.MDmde.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONObject;

import java.io.InputStream;


public class ProfileActivity extends ActionBarActivity {

//    private final static String PROFILE_URL = "http://www.mdme.us/api/v1/patients/show.json";
    private final static String PROFILE_URL = "http://10.0.2.2:3000/api/v1/patients/show.json";

    private SharedPreferences mPreferences;
    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mPhoneNumber;
    private String mPictureUrl;
    private ImageView mProfileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        mProfileImage = (ImageView)findViewById(R.id.profile_image);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
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
            loadProfileFromAPI(PROFILE_URL);
            Button mEditButton = (Button)findViewById(R.id.edit_profile_button);
            mEditButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                    intent.putExtra("firstName", mFirstName);
                    intent.putExtra("lastName", mLastName);
                    intent.putExtra("phoneNumber", mPhoneNumber);
                    intent.putExtra("email", mEmail);
                    //send profile image
                    mProfileImage.buildDrawingCache();
                    Bitmap image = mProfileImage.getDrawingCache();
                    Bundle extras = new Bundle();
                    extras.putParcelable("profileImage", image);
                    intent.putExtra("profileBundle", extras);
                    startActivity(intent);
                }
            });
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    private void loadProfileFromAPI(String url)
    {
        GetProfileTask getProfileTask = new GetProfileTask(ProfileActivity.this);
        getProfileTask.setMessageLoading("Loading...");
        getProfileTask.execute(url + "?api_token=" + mPreferences.getString("ApiToken", ""));
    }

    private class GetProfileTask extends UrlJsonAsyncTask
    {
        public GetProfileTask(Context context) { super(context); }

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
                mFirstName = json.getJSONObject("data").getJSONObject("patient").getString("first_name");
                mLastName = json.getJSONObject("data").getJSONObject("patient").getString("last_name");
                mEmail = json.getJSONObject("data").getJSONObject("patient").getString("email");
                mPhoneNumber = json.getJSONObject("data").getJSONObject("patient").getString("phone_number");
                mPictureUrl = json.getJSONObject("data").getJSONObject("patient").getString("avatar_thumb_url");


                TextView mFullNameText = (TextView) findViewById(R.id.profile_full_name);
                mFullNameText.setText(mFirstName + " " + mLastName);
                TextView mPhoneNumText = (TextView) findViewById(R.id.profile_phone_number);
                mPhoneNumText.setText(mPhoneNumber);
                TextView mEmailAddrText = (TextView) findViewById(R.id.profile_email_address);
                mEmailAddrText.setText(mEmail);

                //load image
                //new DownloadImageTask(mProfileImage).execute("http://www.mdme.us" + mPictureUrl);
                new DownloadImageTask(mProfileImage).execute("http://10.0.2.2:3000" + mPictureUrl);

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
    //https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try
            {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                in.close();
            }
            catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}
