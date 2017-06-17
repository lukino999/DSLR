package com.lukino999.dslr06;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1002;
    private static String appName = "DSLR";

    MediaActionSound mediaActionSound =  new MediaActionSound();

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
        System.out.println("SetFullscreen");
    }





    private Camera mCamera;
    private CameraPreview mPreview;

    // this will be used to inflate an xml to its own View obj
    LayoutInflater controlInflater = null;



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
                Environment.DIRECTORY_PICTURES), appName);
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

    private boolean isPictureSequenceEnabled = false;



    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // shutter animation
            FrameLayout frameShutter = (FrameLayout) findViewById(R.id.camera_preview);
            animator.shutterAnimation(frameShutter);

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

            Log.i(" - - - - - - - - - - - ", "Picture taken " + getOutputMediaFileUri(MEDIA_TYPE_IMAGE));
            Toast.makeText(CameraActivity.this, "Saved as: " + getOutputMediaFileUri(MEDIA_TYPE_IMAGE), Toast.LENGTH_SHORT).show();
            mCamera.startPreview();

            //Button buttonHowManyPictures = (Button) findViewById(R.id.button_howManyPictures);
            // check whether is picture sequence
            if (isPictureSequenceEnabled) {

                // get how many pictures left to take
                Button buttonHowManyPictures = (Button) findViewById(R.id.button_howManyPictures);
                int picturesLeftToTake = Integer.parseInt(buttonHowManyPictures.getText().toString());
                System.out.println("Pictures left: " + picturesLeftToTake);

               if (picturesLeftToTake > 1){
                    //buttonCapture.performClick();
                   takePicture();
                    picturesLeftToTake--;
                    //set pictureLeftToTake
                    buttonHowManyPictures.setText(String.valueOf(picturesLeftToTake));
                } else {
                   buttonHowManyPictures.setText("0");
               }
            }

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



        //  8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8


        // this inflates the overlay_button_take_picture_button_take_picture.xml file to the View viewControl
        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.overlay_button_take_picture, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);

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

    }


    private void setPreviewAspectRatio() {

        /* in order to set preview aspect ratio to be the
         same as camera output format proportions
        get the format proportions from camera params*/

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        float aspectRatio;

        Camera.Size pictureSize = mCamera.getParameters().getPictureSize();
        System.out.println(pictureSize.width + " X " + pictureSize.height);

        aspectRatio = (float) pictureSize.width / (float) pictureSize.height;

        System.out.println("Aspect Ratio: " + aspectRatio);

        // now set the preview width

        FrameLayout cameraPreviewLayout = (FrameLayout) findViewById(R.id.camera_preview);
        int cameraPreviewHeight = height;
        int cameraPreviewWidth = (int) (height * aspectRatio);

        System.out.println("camera_preview size: " + cameraPreviewWidth + " X " + cameraPreviewHeight);

        ViewGroup.LayoutParams params = cameraPreviewLayout.getLayoutParams();
        params.width = cameraPreviewWidth;
        params.height = cameraPreviewHeight;
        cameraPreviewLayout.setLayoutParams(params);



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

        appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
        Log.i("AppName", appName);

        startCamera();


        // listens to layout to be ready before calling the setPreviewAspectRatio
        final FrameLayout layout = (FrameLayout) findViewById(R.id.camera_preview);



        // add listener for camera_preview to be drawn, then call the setPreviewAspectRatio()
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // remove the listener as you only what this executed once
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // set camera_preview aspect ratio
                setPreviewAspectRatio();

                // sets the user interface functionality
                setListeners();

            }
        });
        Log.i(" - - - - - - - - - - - ", "end of onCreate");
    }





    // release the camera once done ----------------------------------------------------------------
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
    // ---------------------------------------------------------------------------------------------





    // set Listeners -------------------------------------------------------------------------------


    // define buttons clicked state
    boolean isButtonFocusModeClicked = false;
    boolean isSpinnerVisible = false;
    public boolean hasSpinnerJustBeenFired = true;
    private View whoIsUsingTheSpinner;


    int focusAreaSize = 200;

    MyAnimator animator = new MyAnimator();




    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            System.out.println("Autofocus success: " + success);
            float focusDistance[] = new float[3];
            if (success) {
                mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING);
                mCamera.getParameters().getFocusDistances(focusDistance);
                for (float fd : focusDistance){
                    System.out.println(fd);
                }
            } else {
                Toast.makeText(CameraActivity.this, "Unable to focus", Toast.LENGTH_SHORT).show();

            }
        }
    };


    private void setListeners(){

        final Camera.Parameters mCameraParameters = mCamera.getParameters();
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);



        /*
        When it starts in AUTO the autoFocus doesnt seem to work
        Camera is very hot though. Check again with cool phone
         */
        mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        mCamera.setParameters(mCameraParameters);



        // Add a listener to the button_capture


        final ImageButton buttonCapture = (ImageButton) findViewById(R.id.button_capture);
        buttonCapture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPictureSequenceEnabled = false;
                        takePicture();

                    }
                }
        );

        buttonCapture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                System.out.println("OnLongClick");
                countDown();

                return true;
            }
        });





        // Add listener to button_focus_mode
        final RelativeLayout menuFocusMode = (RelativeLayout) findViewById(R.id.menu_focus_mode);
        final Button buttonFocusMode = (Button) findViewById(R.id.button_focus_mode);
        if (mCameraParameters.getFocusMode() == Camera.Parameters.FOCUS_MODE_INFINITY){
            buttonFocusMode.setText("INF");
        } else {
            buttonFocusMode.setText(mCameraParameters.getFocusMode().toUpperCase());
        }
        buttonFocusMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuFocusMode.getVisibility() == View.VISIBLE){
                    // menu is visible. Toggle it off
                    animator.fadeOut(menuFocusMode);
                    isButtonFocusModeClicked = false;
                } else {
                    // menu is invisible. Toggle it on
                    animator.fadeIn(menuFocusMode);
                    isButtonFocusModeClicked = true;
                }
            }
        });



        // set button_focus_mode_macro if supported
        if (mCameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_MACRO)){
            System.out.println("Macro supported");
            Button buttonFocusModeMacro = (Button) findViewById(R.id.button_focus_mode_macro);
            buttonFocusModeMacro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Macro");
                    mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                    mCamera.setParameters(mCameraParameters);
                    buttonFocusMode.setText(mCameraParameters.getFocusMode().toUpperCase());
                    mediaActionSound.load(MediaActionSound.START_VIDEO_RECORDING); // preload the sample
                    mCamera.autoFocus(autoFocusCallback);
                    // menu is visible. Toggle it off
                    menuFocusMode.setVisibility(View.INVISIBLE);
                    isButtonFocusModeClicked = false;
                }
            });
        }



        // set button_focus_mode_inf if supported. Do not call autofocus in this mode
        if (mCameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_INFINITY)){
            System.out.println("Infinity supported");
            Button buttonFocusModeInf = (Button) findViewById(R.id.button_focus_mode_inf);
            buttonFocusModeInf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Inf");
                    mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                    mCamera.setParameters(mCameraParameters);
                    buttonFocusMode.setText("INF");
                    // menu is visible. Toggle it off
                    menuFocusMode.setVisibility(View.INVISIBLE);
                    isButtonFocusModeClicked = false;
                }
            });
        }



        // set button_focus_mode_auto
        if (mCameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            Button buttonFocusModeAuto = (Button) findViewById(R.id.button_focus_mode_auto);
            buttonFocusModeAuto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Auto");
                    mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.setParameters(mCameraParameters);
                    mediaActionSound.load(MediaActionSound.START_VIDEO_RECORDING); // preload the sample
                    mCamera.autoFocus(autoFocusCallback);
                    buttonFocusMode.setText(mCameraParameters.getFocusMode().toUpperCase());
                    // menu is visible. Toggle it off
                    menuFocusMode.setVisibility(View.INVISIBLE);
                    isButtonFocusModeClicked = false;
                }
            });
        }



        // focus touching the preview
        FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // only available in
                /*The Rect field in a Camera.Area object describes a
                rectangular shape mapped on a 2000 x 2000 unit grid.
                The coordinates -1000, -1000 represent the top, left corner of the camera image,
                and coordinates 1000, 1000 represent the bottom,
                right corner of the camera image, as shown in the illustration below.
                https://developer.android.com/guide/topics/media/images/camera-area-coordinates.png
                 */
                int xRect = (int) ((event.getX() / v.getWidth() * 2000)-1000);
                int yRect = (int) ((event.getY() / v.getHeight() * 2000)-1000);

                System.out.println("getX, getY: " + event.getX() + ", " + event.getY());
                System.out.println("xRect, yRect: " + xRect + ", " + yRect);

                // make sure the rect it's inside the allowed range
                if (xRect < (-1000 + (focusAreaSize / 2 ))) xRect = (-1000 + (focusAreaSize / 2));
                if (xRect > (1000 - (focusAreaSize / 2 ))) xRect = (1000 - (focusAreaSize / 2 ));
                if (yRect < (-1000 + (focusAreaSize / 2 ))) yRect = (-1000 + (focusAreaSize / 2));
                if (yRect > (1000 - (focusAreaSize / 2 ))) yRect = (1000 - (focusAreaSize / 2 ));

                Rect focusAreaRect = new Rect(xRect - focusAreaSize / 2, yRect - focusAreaSize / 2,
                        xRect + focusAreaSize / 2, yRect + focusAreaSize / 2);
                List<Camera.Area> focusAreasList = new ArrayList<>();
                focusAreasList.add(new Camera.Area(focusAreaRect, 1000));
                mCameraParameters.setFocusAreas(focusAreasList);
                mCamera.setParameters(mCameraParameters);

                if ((mCameraParameters.getFocusMode() == Camera.Parameters.FOCUS_MODE_AUTO) ||
                        (mCameraParameters.getFocusMode() == Camera.Parameters.FOCUS_MODE_MACRO)){
                    System.out.println("autofocus");
                    mCamera.autoFocus(autoFocusCallback);
                }

                return false;
            }
        });







        // button_iso
        Button buttonGetIso = (Button) findViewById(R.id.button_iso);
        buttonGetIso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSpinnerVisible && (whoIsUsingTheSpinner == v)) {
                    // toggle OFF
                    animator.fadeOut(spinner);
                    isSpinnerVisible = false;
                    whoIsUsingTheSpinner = null;
                    setFullscreen();
                } else {
                    // toggle ON
                    whoIsUsingTheSpinner = v;
                    isSpinnerVisible = true;


                    // get the iso_values as string[]
                    final String[] iso_values = mCameraParameters.get("iso-values").split(",");
                    //convert it to ArrayList
                    final ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(iso_values));
                    // fill the spinner
                    fillTheSpinner(arrayList);

                    String currentValueString = mCameraParameters.get("iso");
                    //find current value index
                    int currentValueInt = arrayList.indexOf(currentValueString);
                    System.out.println("currentValueString " + currentValueString + "   -   currentValueInt: " + currentValueInt);
                    // select from spinner to match current value
                    spinner.setSelection(currentValueInt, true);

                    // show the spinner
                    animator.fadeIn(spinner);

                    // set a onItemSelectListener for the spinner
                    spinner.setOnItemSelectedListener(null); // null the listener in case it's already assigned
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            mCameraParameters.set("iso", iso_values[position]);
                            mCamera.setParameters(mCameraParameters);
                            setFullscreen();

                        }



                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
            }
        });






        // button_scene_mode
        Button buttonSceneMode = (Button) findViewById(R.id.button_scene_mode);
        buttonSceneMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSpinnerVisible && (whoIsUsingTheSpinner == v)) {
                    // toggle OFF
                    animator.fadeOut(spinner);
                    isSpinnerVisible = false;
                    whoIsUsingTheSpinner = null;
                    setFullscreen();
                } else {
                    // toggle ON
                    whoIsUsingTheSpinner = v;
                    isSpinnerVisible = true;


                    // get the iso_values as string[]
                    final String[] supportedSceneModeValues = mCameraParameters.get("scene-mode-values").split(",");
                    //convert it to ArrayList
                    final ArrayList<String> sceneModesValues= new ArrayList<>(Arrays.asList(supportedSceneModeValues));
                    // fill the spinner
                    fillTheSpinner(sceneModesValues);

                    //get current value index
                    int currentValueIndex = sceneModesValues.indexOf(mCameraParameters.get("scene-mode"));

                    // set spinner to current value
                    spinner.setSelection(currentValueIndex, false);

                    animator.fadeIn(spinner);
                    spinner.setOnItemSelectedListener(null);
                    //hasSpinnerJustBeenFired = true;
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            mCameraParameters.set("scene-mode", supportedSceneModeValues[position]);
                            mCamera.setParameters(mCameraParameters);
                            setFullscreen();
                            System.out.println("Scene set to: " + mCameraParameters.get("scene-mode"));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            System.out.println("No scene selected");
                        }
                    });
                }

            }
        });


        final Button buttonZSL = (Button) findViewById(R.id.button_zsl);
        buttonZSL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonZSL.getText() == "ZSL\nON") {
                    //toggle off
                    mCameraParameters.set("zsl", "off");
                    mCamera.setParameters(mCameraParameters);
                    buttonZSL.setText("ZSL\nOFF");
                } else {
                    //toggle on
                    mCameraParameters.set("zsl", "on");
                    mCamera.setParameters(mCameraParameters);
                    buttonZSL.setText("ZSL\nON");
                }
            }
        });


        // toggle howManyPictures +1 +10 menu
        final RelativeLayout menuPictureCount = (RelativeLayout) findViewById(R.id.menu_picture_count);
        menuPictureCount.setVisibility(View.INVISIBLE);
        final Button buttonHowManyPictures = (Button) findViewById(R.id.button_howManyPictures);
        buttonHowManyPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("buttonHowManyPictures.setOnClickListener");
                if (menuPictureCount.getVisibility() == View.VISIBLE) {
                    //toggle off
                    animator.fadeOut(menuPictureCount);
                } else {
                    // toggle on
                    animator.fadeIn(menuPictureCount);
                }
            }
        });

        // reset howManyPictures
        buttonHowManyPictures.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                buttonHowManyPictures.setText("0");
                return true;
            }
        });

        // button_plus_one
        Button buttonPlusOne = (Button) findViewById(R.id.button_plus_one);
        buttonPlusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int picturesLeftToTake = Integer.parseInt(buttonHowManyPictures.getText().toString());
                picturesLeftToTake++;
                buttonHowManyPictures.setText(String.valueOf(picturesLeftToTake));
            }
        });

        // button_plus_ten
        Button buttonPlusTen = (Button) findViewById(R.id.button_plus_ten);
        buttonPlusTen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int picturesLeftToTake = Integer.parseInt(buttonHowManyPictures.getText().toString());
                picturesLeftToTake+=10;
                buttonHowManyPictures.setText(String.valueOf(picturesLeftToTake));
            }
        });

        // button_flash_mode
        final Button buttonFlashMode = (Button) findViewById(R.id.button_flash_mode);
        final RelativeLayout menuFlashMode = (RelativeLayout) findViewById(R.id.menu_flash_mode);
        menuFlashMode.setVisibility(View.INVISIBLE);
        // set button_flash_mode text
        buttonFlashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonFlashMode.setText("Flash\n" + mCameraParameters.get("flash-mode").toUpperCase());
                if (menuFlashMode.getVisibility() == View.VISIBLE){
                    //toggle off
                    animator.fadeOut(menuFlashMode);
                } else {
                    // toggle on
                    animator.fadeIn(menuFlashMode);
                }
            }
        });

        // button_flash_off
        Button buttonFlashOff = (Button) findViewById(R.id.button_flash_off);
        buttonFlashOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraParameters.set("flash-mode", "off");
                mCamera.setParameters(mCameraParameters);
                buttonFlashMode.performClick();
            }
        });

        // button_flash_on
        Button buttonFlashOn = (Button) findViewById(R.id.button_flash_on);
        buttonFlashOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraParameters.set("flash-mode", "on");
                mCamera.setParameters(mCameraParameters);
                buttonFlashMode.performClick();
            }
        });

        // button_flash_auto
        Button buttonFlashAuto = (Button) findViewById(R.id.button_flash_auto);
        buttonFlashAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraParameters.set("flash-mode", "auto");
                mCamera.setParameters(mCameraParameters);
                buttonFlashMode.performClick();
            }
        });

        // button_flash_torch
        Button buttonFlashTorch = (Button) findViewById(R.id.button_flash_torch);
        buttonFlashTorch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraParameters.set("flash-mode", "torch");
                mCamera.setParameters(mCameraParameters);
                buttonFlashMode.performClick();
            }
        });

        final SeekBar seekBarZoom = (SeekBar) findViewById(R.id.seekbar_zoom);
        final TextView textViewZoom = (TextView) findViewById(R.id.text_view_zoom);
        // get how many steps
        List zoomRatiosList = mCameraParameters.getZoomRatios();
        int steps = zoomRatiosList.size();
        System.out.println("Zoom steps: " + steps);
        System.out.println("getMaxZoom: " + mCameraParameters.getMaxZoom());
        // set max
        seekBarZoom.setMax(steps - 1);
        // set step
        seekBarZoom.incrementProgressBy(1);
        // setListener
        seekBarZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCameraParameters.setZoom(progress);
                mCamera.setParameters(mCameraParameters);
                if (fromUser) {
                    System.out.println("seekBar from user: " + progress);
                } else {
                    System.out.println("seekBar from code: " + progress);
                }

                animator.tempTextView(textViewZoom, String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        // butttonZoomPlus
        Button buttonZoomPlus = (Button) findViewById(R.id.button_zoom_plus);
        buttonZoomPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBarZoom.setProgress((seekBarZoom.getProgress() + 1));
            }
        });

        // buttonZoomMinus
        Button buttonZoomMinus = (Button) findViewById(R.id.button_zoom_minus);
        buttonZoomMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBarZoom.setProgress((seekBarZoom.getProgress() - 1));
            }
        });



        // end of set listeners --------------------------------------------------------------------
    }







    private void takePicture() {
        // get an image from the camera
        mCamera.takePicture(null, null, mPicture);
    }

    private void fillTheSpinner(ArrayList<String> arrayList){

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        spinner.setAdapter(arrayAdapter);

    }

    private int countDown = 3;
    private void countDown() {

        final TextView textViewCentral = (TextView) findViewById(R.id.text_view_central);

        final Handler h = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                textViewCentral.setText(String.valueOf(countDown));
                if (countDown > 1) {
                    System.out.println("Countdown: " + countDown);
                    countDown--;
                    h.postDelayed(this, 1000);
                } else {
                    System.out.println("TimeOver");
                    animator.fadeOut(textViewCentral);
                    countDown = 3;
                    /*
                    after countdown, take as many picture as it says
                    on button_howManyPictures.getText
                     */

                    // get sequence of pictures
                    isPictureSequenceEnabled = true;
                    takePicture();

                }
            }


        };

        animator.fadeIn(textViewCentral);
        h.post(r);

    }

}
