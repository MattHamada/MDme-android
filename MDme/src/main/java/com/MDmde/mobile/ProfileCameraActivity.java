package com.MDmde.mobile;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;


public class ProfileCameraActivity extends ActionBarActivity {

    public static final String EXTRA_PHOTO_FILENAME =
            "com.MDme.mobile.photo_filename";
    public static final String EXTRA_ORIENTATION =
            "com.MDme.mobile.orientation";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;
    public int orientation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //hide window title
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);

        //hide status bar and other os-level items
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_camera);

        mProgressContainer = findViewById(R.id.profile_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        ImageButton takePictureButton = (ImageButton)findViewById(R.id.profile_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mCamera != null)
                {
                    //passed null for raw callback since not implemented - not needed here
                    mCamera.takePicture(mShutterCallback, null, mJpegCallback);
                    //check if taken in portait or landscape mode
                    orientation = getResources().getConfiguration().orientation;
                }
            }
        });

        mSurfaceView = (SurfaceView)findViewById(R.id.profile_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        //setType() and SURFACE_TYPE_PUSH_BUFFERS deprecated but needed for pre3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //connect camera to surface holder
        holder.addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                //connect camera to surface
                try
                {
                    if (mCamera != null)
                    {
                        mCamera.setPreviewDisplay(holder);
                        mCamera.setDisplayOrientation(90);
                    }
                }
                catch (IOException e)
                {
                    Log.e("ProfileCamera", "Error setting up preview display", e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                if (mCamera == null) return;
                //surface cahnged size, update preview size
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
                parameters.setPreviewSize(s.width, s.height);

                //set image size
                s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
                parameters.setPictureSize(s.width, s.height);
                mCamera.setParameters(parameters);

                try
                {
                    mCamera.startPreview();
                }
                catch(Exception e)
                {
                    Log.e("ProfileCamera", "Could not start Preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                //disconenct camera
                if(mCamera != null)
                {
                    mCamera.stopPreview();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_camera, menu);
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

    @TargetApi(9)
    @Override
    public void onResume()
    {
        super.onResume();
        //start camera
        //different camera call for older apis
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        {
            mCamera = Camera.open(0);
        }
        else
        {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //free camera
        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height)
    {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes)
        {
            int area = s.width * s.height;
            if (area > largestArea)
            {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }

    //camera callbacks for converting to jpg
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback()
    {
        public void onShutter()
        {
            //show progess bar while processing
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback()
    {

        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            //create filename
            String filename = UUID.randomUUID().toString() + ".jpg";
            //save to disk
            FileOutputStream os = null;
            boolean success = true;
            try
            {
                os = openFileOutput(filename, Context.MODE_PRIVATE);
                os.write(data);
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(), "Error writing file. " + "" + e,
                        Toast.LENGTH_LONG).show();
                success = false;
            }
            finally
            {
                try
                {
                    if (os != null)
                    {
                        os.close();
                    }
                }
                catch (Exception e)
                {
                    Log.e("CameraSave", "Error closing file " + filename, e);
                    success = false;
                }
            }
            if (success)
            {
                //send the photo filename back to the profile update activity
                Intent intent = new Intent();
                intent.putExtra(EXTRA_PHOTO_FILENAME, filename);
                intent.putExtra(EXTRA_ORIENTATION,
                        getResources().getConfiguration().orientation);
                setResult(Activity.RESULT_OK, intent);
            }
            else
            {
                setResult(Activity.RESULT_CANCELED);
            }
            //destroy view
            finish();
        }
    };

}
