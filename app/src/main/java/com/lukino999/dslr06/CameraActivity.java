package com.lukino999.dslr06;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

import static android.R.attr.id;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1002;
    private boolean cameraPermissionGranted;
    private boolean writeExternalStoragePermissionGranted;

    // sets fullscreen declarations
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private void setFullscreen(){
        // makes fullscreen
        mContentView = findViewById(R.id.fullscreen_content);
        mHideHandler.post(mHidePart2Runnable);
    }





    private Camera mCamera;
    private CameraPreview mPreview;



    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.i(" - - - - - - - - - - - ", "something wrong opening the camera");

        }
        return c; // returns null if camera is unavailable
    }



    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){  //why not used??
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }



    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");  //e.getMessage();
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            //startPreview();
            Log.i(" - - - - - - - - - - - ", "Picture taken");
            Toast.makeText(CameraActivity.this, "Picture taken", Toast.LENGTH_SHORT).show();
            mCamera.startPreview();
            Toast.makeText(CameraActivity.this, "mCamera.startPreview", Toast.LENGTH_SHORT).show();
        }
    };



    private void startPreview(){

        // Create an instance of Camera
        mCamera = getCameraInstance();

        System.out.println("mcamera: " + mCamera);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

    }

    private void startCamera(){

        if ((ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(CameraActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            checkPermissions();
            return;
        }

        setFullscreen();

        // Start preview
        startPreview();

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }


    private void checkPermissions(){
        /** Beginning in Android 6.0 (API level 23), users grant permissions to apps
         * while the app is running, not when they install the app.
         * https://developer.android.com/training/permissions/requesting.html#perm-check */

        if (ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

            // The callback method gets the result of the request.
        }

        if (ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

            // The callback method gets the result of the request.
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    startCamera();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted.
                    Toast.makeText(this, "Writing on storage permission granted", Toast.LENGTH_SHORT).show();
                    startCamera();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(" - - - - - - - - - - - ", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        startCamera();

        Log.i(" - - - - - - - - - - - ", "end of onCreate");
    }



    // relese the camera once done
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(" - - - - - - - - - - - ", "onPause");
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i(" - - - - - - - - - - - ", "onRestart");
        startCamera();
    }


}
