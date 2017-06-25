package com.lukino999.dslr06;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import java.util.Map;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1002;

    private static String appName;

    final CameraFunctionsList cameraFunctionsList = new CameraFunctionsList();

    // sets fullscreen declarations
    private final Handler mHideHandler = new Handler();

    MediaActionSound shutterClick =  new MediaActionSound();

    // this will be used to inflate an xml to its own View obj
    LayoutInflater controlInflater = null;

    boolean menuViewVisible = false;

    MyAnimator animator = new MyAnimator();

    int focusAreaSize = 300;

    FocusAreaDrawable focusAreaDrawable;
    private Rect focusAreaDrawableBounds;
    private Handler removeFocusHandler = new Handler();
    private Runnable removeFocusRect = new Runnable() {
        @Override
        public void run() {
            focusAreaDrawable.set(focusAreaDrawableBounds, 0x00000000);
            focusAreaDrawable.invalidate();
        }
    };

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            log("Autofocus has been successful: " + success);
            if (success) {
                focusAreaDrawable.set(focusAreaDrawableBounds, 0xAA00FF00);
                focusAreaDrawable.invalidate();
            } else {
                focusAreaDrawable.set(focusAreaDrawableBounds, 0xAAFF0000);
                focusAreaDrawable.invalidate();
            }
            removeFocusHandler.postDelayed(removeFocusRect, 500);
        }

    };


    private int countDown = 3;

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

    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean keepTakingPictures = false;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mOnPictureTaken(data);
        }
    };

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

            shutterClick.play(MediaActionSound.SHUTTER_CLICK);

            FrameLayout frameShutter = (FrameLayout) findViewById(R.id.camera_preview);
            animator.shutter(frameShutter);
        }
    };


    private Camera.Parameters mCameraParameters;
    private View whoIsUsingTheValueMenu;

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){

            // Camera is not available (in use or does not exist)
            log(" - - - - - - - - - - - something wrong opening the camera");

        }
        log("------Camera parameters------------------------------------------------------");

        log(c.getParameters().flatten().replace(";", "\n"));
        log("-----------------------------------------------------------------------------");

        return c; // returns null if camera is unavailable
    }


    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        boolean saveOnSD = true;

        File mediaStorageDir;

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.


        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);

        log(mediaStorageDir.toString());

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                log("Failed to create directory" + mediaStorageDir.toString());
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
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

    // log
    private static void log(String s) {
        System.out.println(s);
    }

    private void setFullscreen(){
        // makes fullscreen
        mContentView = findViewById(R.id.fullscreen_content);
        mHideHandler.post(mHidePart2Runnable);
        log("SetFullscreen");
    }



    private void mOnPictureTaken(byte[] data) {

        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            log("Error creating media file, check storage permissions: ");  //e.getMessage();
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            log("File not found: " + e.getMessage());
        } catch (IOException e) {
            log("Error accessing file: " + e.getMessage());
        }

        log(" - - - - - - - - - - - Picture taken " + pictureFile.toString());
        Toast.makeText(CameraActivity.this, "Saved as: " + pictureFile.toString(), Toast.LENGTH_SHORT).show();


        mCamera.startPreview();

        /*
        keep taking pictures?
         */
        if (keepTakingPictures) {

            // get how many pictures left to take
            TextView howManyPicturesLeft = (TextView) findViewById(R.id.text_view_how_many_pictures);
            int picturesLeftToTake = Integer.parseInt(howManyPicturesLeft.getText().toString());
            log("Pictures left: " + picturesLeftToTake);

            if (picturesLeftToTake > 1){
                //buttonCapture.performClick();
                takePicture();
                picturesLeftToTake--;
                //set pictureLeftToTake
                howManyPicturesLeft.setText(String.valueOf(picturesLeftToTake));
            } else {
                howManyPicturesLeft.setText("0");
                animator.fadeOut(howManyPicturesLeft);
                animator.fadeIn(findViewById(R.id.menu_zoom));
            }
        }

    }
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------

    private void startPreview(){

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        preview.addView(mPreview);



        //  8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8


        // inflate the controls_camera.xml layout as View viewControl
        controlInflater = LayoutInflater.from(getBaseContext());

        View viewControl = controlInflater.inflate(R.layout.controls_camera, null);

        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);

        this.addContentView(viewControl, layoutParamsControl);

        // add focusAreaDrawable to layout
        focusAreaDrawable = new FocusAreaDrawable(this);
        this.addContentView(focusAreaDrawable, layoutParamsControl);

    }

    private void startCamera(){

        // are permissions granted
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
//                setListeners();

                setControls();

            }
        });

    }

    private void setPreviewAspectRatio() {

        /*
        in order to set preview aspect ratio to be the
        same as camera output format proportions
        get the format proportions from camera params
        */

        mCameraParameters = mCamera.getParameters();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        float pictureAspectRatio;

        Camera.Size pictureSize = mCameraParameters.getPictureSize();
        log(pictureSize.width + " X " + pictureSize.height);

        Camera.Size previewSize = mCameraParameters.getPreviewSize();

        pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
        log("pictureAspectRatio: " + pictureAspectRatio);

        float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;
        log("previewAspectRatio: " + previewAspectRatio);

        // now set the preview width
        FrameLayout cameraPreviewLayout = (FrameLayout) findViewById(R.id.camera_preview);
        int cameraPreviewHeight = height;
        int cameraPreviewWidth = (int) (height * pictureAspectRatio);

        log("camera_preview size: " + cameraPreviewWidth + " X " + cameraPreviewHeight);

        ViewGroup.LayoutParams params = cameraPreviewLayout.getLayoutParams();
        params.width = cameraPreviewWidth;
        params.height = cameraPreviewHeight;
        cameraPreviewLayout.setLayoutParams(params);

        mCameraParameters.setPreviewSize(cameraPreviewWidth, cameraPreviewHeight);
        mCamera.setParameters(mCameraParameters);

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
        log(" - - - - - - - - - - - onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
        log("AppName " + appName);

        startCamera();



        log(" - - - - - - - - - - - end of onCreate");
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

        log(" - - - - - - - - - - - onPause");
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        log(" - - - - - - - - - - - onRestart");
        startCamera();
    }



    // set the UI ----------------------------------------------------------------------------------
    private void setControls() {

        mCameraParameters = mCamera.getParameters();

        mCameraParameters.set(" jpeg-quality", "100");
        mCamera.setParameters(mCameraParameters);
        log(" jpeg-quality: " + mCameraParameters.get(" jpeg-quality"));


        initializeButtonCapture();

        initializeCameraPreviewAutofocus();

        initializeMainMenu();

        initializeZoom();

        initializeButtonShowMenu();

        initializePicturesLeftMenu();

    }

    private void initializeMainMenu() {


        final ListView mainMenu = (ListView) findViewById(R.id.list_view_main_menu);
        ArrayList<String> functionsArrayList = new ArrayList<>();

        /*
        Populate the functionsArrayList with LABEL: currentValue
         */
        for (Map m : cameraFunctionsList.availableFuntions){
            log(m.toString());

            functionsArrayList.add(m.get(cameraFunctionsList.LABEL).toString() + ": " +
            mCameraParameters.get(m.get(cameraFunctionsList.VALUE).toString()));
        }

        // populate the mainMenu ListView
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,
                        functionsArrayList);
        mainMenu.setAdapter(arrayAdapter);


        mainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                log("mainMenu.setOnItemClickListener");
                updateValuesMenu(position, (TextView) view);
            }
        });

    }


    private String[] availableValues = new String[0];
    private void updateValuesMenu(final int i, final TextView whoIsCalling){

        /*
        Update menu with values returned by CameraParameters.get(keyAvailableValues)
        If menu is already up, whoIsCalling tells whether to toggle off or update

        flagAvailableValues -> "-values": gets available values from cameraParameters
        flagAvailableValues -> "-supported": available values are "true" or "false"
        */

        final String flagAvailableValues = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.AVAILABLE_VALUES).toString();
        final String keyCurrentValue = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.VALUE).toString();
        final String label = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.LABEL).toString();
        final ListView listView = (ListView) findViewById(R.id.list_view_values_menu);
        final String keyAvailableValues = keyCurrentValue + flagAvailableValues;
        final String[] boolValues = {"true", "false"};


        if (mCameraParameters.get(keyAvailableValues) != null ||
                mCameraParameters.get(keyAvailableValues) == "true" ||
                flagAvailableValues == "max-") {
            // ---------------------------------------------------------------------
            if (menuViewVisible && (whoIsUsingTheValueMenu == whoIsCalling)) {
                // toggle OFF
                log("Update menu:: toggle OFF");
                animator.fadeOut(listView);
                menuViewVisible = false;
                whoIsUsingTheValueMenu = null;
                setFullscreen();
            } else {
                // toggle ON
                log("Update menu:: toggle ON");
                whoIsUsingTheValueMenu = whoIsCalling;
                menuViewVisible = true;


                // get the availableValues as string[]
                if (flagAvailableValues == "-values") {
                    // -values
                    availableValues = mCameraParameters.get(keyAvailableValues).split(",");
                } else if (flagAvailableValues == "-supported") {
                    // -supported
                    availableValues = boolValues;
                } else if (flagAvailableValues == "max-") {
                    availableValues = getPossibleValues(keyCurrentValue);
                }


                //convert it to ArrayList
                final ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(availableValues));
                // fill the menuView
                fillValuesMenu(arrayList);


                String currentValueString = mCameraParameters.get(keyCurrentValue);
                //find current value index
                final int currentValueIndex = arrayList.indexOf(currentValueString);
                log("currentValueString " + currentValueString + "   -   currentValueInt: " + currentValueIndex);


                // wait for the listView to be ready before fadingIn and setting the listener
                ViewTreeObserver vto = listView.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // remove this listener
                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        // show the menuView
                        animator.fadeIn(listView);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                log(" \nvaluesMenu.OnItemClick");
                                view.getFocusables(position);
                                view.setSelected(true);
                                mCameraParameters.set(keyCurrentValue, availableValues[position]);
                                mCamera.setParameters(mCameraParameters);
                                setMenuItemLabel(whoIsCalling, i);
                                checkForFurtherAction(keyCurrentValue);
                            }
                        });

                    }
                });

            }
            // ---------------------------------------------------------------------
        } else {
            whoIsCalling.setText(label + ": not available");
            Toast.makeText(this, "Not availabe", Toast.LENGTH_SHORT).show();
        }


    }

    private String[] getPossibleValues(String keyCurrentValue) {
        float max;
        String maxString = mCamera.getParameters().get("max-" + keyCurrentValue);
        float min;
        String minString = mCamera.getParameters().get("min-" + keyCurrentValue);
        float step;
        String stepString = mCamera.getParameters().get(keyCurrentValue + "-step");


        if (maxString != null) {
            max = Float.valueOf(maxString);
        } else {
            max = 0f;
        }


        if (minString != null) {
            min = Float.valueOf(minString);
        } else {
            min = 0f;
        }


        if (stepString != null) {
            step = Float.valueOf(stepString);
        } else {
            step = 1f;
        }


        log("min: " + min + "   max: " + max + "   step: " + step);


        int arraySize = (int) (Math.abs((max - min)/step) + 1);
        String[] possibleValues = new String[arraySize];


        float actualValue = min;
        possibleValues[0] = format(actualValue);
        for (int i = 1; i < arraySize; i++) {
            actualValue = actualValue + step;
            possibleValues[i] = format(actualValue);

        }

        return possibleValues;
    }

    public static String format(float d) {
        String s = String.valueOf(d);
        return s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    private void checkForFurtherAction(String keyCurrentValue) {
        switch (keyCurrentValue){
            case "picture-size":    setPreviewAspectRatio();
        }
    }

    private void initializePicturesLeftMenu() {
        final ListView howManyPicturesMenu = (ListView) findViewById(R.id.list_view_how_many_pictures);
        final TextView textViewHowMany = (TextView) findViewById(R.id.text_view_how_many_pictures);
        String[] howManyPicsMenuItems = {"RESET", "+100", "+10", "+1", "START"};
        ArrayList<String> howManyPicsItemsArrayList = new ArrayList<>(Arrays.asList(howManyPicsMenuItems));
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, howManyPicsItemsArrayList);
        howManyPicturesMenu.setAdapter(arrayAdapter);
        ViewTreeObserver vto = howManyPicturesMenu.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setHowManyPicsOnItemSelectListener();
            }

            private void setHowManyPicsOnItemSelectListener() {
                howManyPicturesMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        log("Click on: " + view.toString());
                        TextView textView = (TextView) view;
                        String command = textView.getText().toString();
                        if (command == "RESET") {
                            textViewHowMany.setText("0");
                        } else if (command == "START") {
                            animator.fadeOut(howManyPicturesMenu);
                            initializeCameraPreviewAutofocus();
                            countDown();
                        } else {
                            int howManyPicturesLeft = Integer.valueOf(textViewHowMany.getText().toString());
                            howManyPicturesLeft = howManyPicturesLeft + Integer.valueOf(textView.getText().toString());
                            if (howManyPicturesLeft < 0) {
                                howManyPicturesLeft = 0;
                            }
                            textViewHowMany.setText(String.valueOf(howManyPicturesLeft));
                        }
                    }
                });
            }
        });


    }

    private void initializeButtonShowMenu() {
        final FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        final ListView mainMenu = (ListView) findViewById(R.id.list_view_main_menu);
        final RelativeLayout zoomMenu = (RelativeLayout) findViewById(R.id.menu_zoom);
        final ImageButton buttonShowMenu = (ImageButton) findViewById(R.id.button_show_menu);
        buttonShowMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide rightArrow and Zoom
                animator.fadeOut(buttonShowMenu);
                animator.fadeOut(zoomMenu);
                // bring in main menu
                animator.fadeIn(mainMenu);
                // change preview listener
                changePreviewListener();
            }

            private void changePreviewListener() {
                // set listener so that it removes the menus and goes back to taking pictures
                cameraPreview.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        log("new cameraPreview.setOnTouchListener");
                        removeMenus();
                        return false;
                    }
                });
            }
        });
    }

    private void removeMenus() {

        animator.fadeOut(findViewById(R.id.list_view_values_menu));
        animator.fadeOut(findViewById(R.id.list_view_main_menu));
        animator.fadeIn(findViewById(R.id.menu_zoom));
        animator.fadeIn(findViewById(R.id.button_show_menu));
        initializeCameraPreviewAutofocus();

    }

    private void initializeZoom() {

        final RelativeLayout menuZoom = (RelativeLayout) findViewById(R.id.menu_zoom);

        final SeekBar seekBarZoom = (SeekBar) findViewById(R.id.seekbar_zoom);
        final TextView textViewZoom = (TextView) findViewById(R.id.text_view_zoom);
        // get how many steps
        final List zoomRatiosList = mCameraParameters.getZoomRatios();

        if (zoomRatiosList != null) {

            /*
            RelativeLayout.LayoutParams menuZoomParams = (RelativeLayout.LayoutParams) menuZoom.getLayoutParams();
            menuZoomParams.setMargins(0,0,0,0);
            menuZoom.requestLayout();
            */


            int steps = zoomRatiosList.size();
            log("Zoom steps: " + steps);
            log("getMaxZoom: " + mCameraParameters.getMaxZoom());

            // set max
            seekBarZoom.setMax(steps - 1);

            // set step
            seekBarZoom.incrementProgressBy(1);

            // set bar to zero
            seekBarZoom.setProgress(0);


            // setListener
            seekBarZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mCameraParameters.setZoom(progress);
                    mCamera.setParameters(mCameraParameters);
                    if (fromUser) {
                        log("seekBar from user: " + progress);
                    } else {
                        log("seekBar from code: " + progress);
                    }
                    float zoomRatio = Float.valueOf(zoomRatiosList.get(progress).toString()) / 100;
                    animator.tempTextView(textViewZoom, "x " + String.valueOf(zoomRatio));

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

    }

    private void setMenuItemLabel(TextView view, final int i) {

        final String keyCurrentValue = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.VALUE).toString();
        final String label = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.LABEL).toString();
        view.setText(label +": " + mCameraParameters.get(keyCurrentValue));

    }

    private void initializeButtonCapture() {

        shutterClick.load(MediaActionSound.SHUTTER_CLICK);

        final ImageButton buttonCapture = (ImageButton) findViewById(R.id.button_capture);
        buttonCapture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        keepTakingPictures = false;
                        takePicture();
                    }
                }
        );

        buttonCapture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                log("OnLongClick");
                //countDown();
                animator.fadeOut(findViewById(R.id.menu_zoom));
                animator.fadeOut(findViewById(R.id.list_view_values_menu));
                animator.fadeOut(findViewById(R.id.list_view_main_menu));
                animator.fadeIn(findViewById(R.id.button_show_menu));
                animator.fadeIn(findViewById(R.id.list_view_how_many_pictures));
                animator.fadeIn(findViewById(R.id.text_view_how_many_pictures));
                findViewById(R.id.camera_preview).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        animator.fadeOut(findViewById(R.id.list_view_how_many_pictures));
                        animator.fadeOut(findViewById(R.id.text_view_how_many_pictures));
                        removeMenus();
                        initializeCameraPreviewAutofocus();
                        return false;
                    }
                });

                return true;
            }
        });



    }


    private void initializeCameraPreviewAutofocus() {

        whoIsUsingTheValueMenu = null;

        // focus touching the preview
        FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                removeFocusHandler.removeCallbacks(removeFocusRect);

                log("cameraPreview.OnTouch");

                /*
                The Rect field in a Camera.Area object describes a
                rectangular shape mapped on a 2000 x 2000 unit grid.
                The coordinates -1000, -1000 represent the top, left corner of the camera image,
                and coordinates 1000, 1000 represent the bottom,
                right corner of the camera image, as shown in the illustration below.
                https://developer.android.com/guide/topics/media/images/camera-area-coordinates.png
                */

                int xRect = (int) ((event.getX() / v.getWidth() * 2000)-1000);
                int yRect = (int) ((event.getY() / v.getHeight() * 2000)-1000);

                log("getX, getY: " + event.getX() + ", " + event.getY());
                log("xRect, yRect: " + xRect + ", " + yRect);

                // make sure the rect it's inside the allowed range
                if (xRect < (-1000 + (focusAreaSize / 2 ))) xRect = (-1000 + (focusAreaSize / 2));
                if (xRect > (1000 - (focusAreaSize / 2 ))) xRect = (1000 - (focusAreaSize / 2 ));
                if (yRect < (-1000 + (focusAreaSize / 2 ))) yRect = (-1000 + (focusAreaSize / 2));
                if (yRect > (1000 - (focusAreaSize / 2 ))) yRect = (1000 - (focusAreaSize / 2 ));


                Rect cameraFocusRect = new Rect(xRect - focusAreaSize / 2, yRect - focusAreaSize / 2,
                        xRect + focusAreaSize / 2, yRect + focusAreaSize / 2);
                List<Camera.Area> focusAreasList = new ArrayList<>();
                focusAreasList.add(new Camera.Area(cameraFocusRect, 1000));
                mCameraParameters.setFocusAreas(focusAreasList);
                mCamera.setParameters(mCameraParameters);


                if (mCameraParameters.get("focus-mode").equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
                        mCameraParameters.get("focus-mode").equals(Camera.Parameters.FOCUS_MODE_MACRO)){
                    log("autofocus");

                    // draw the autofocus
                    focusAreaDrawableBounds = getDrawableRectBounds(cameraFocusRect);
                    focusAreaDrawable.set(focusAreaDrawableBounds, 0xAAFFFFFF);
                    focusAreaDrawable.invalidate();

                    // call the autofocus
                    mCamera.autoFocus(autoFocusCallback);
                }

                return false;
            }
        });


    }

    private Rect getDrawableRectBounds(Rect cameraFocusRect) {


        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        RelativeLayout fullscreen = (RelativeLayout) findViewById(R.id.fullscreen_content);

        // Rect(int left, int top, int right, int bottom)

        log(" \ncameraFocusRect.left: " + cameraFocusRect.left);
        log("cameraFocusRect.top: " + cameraFocusRect.top);
        log("cameraFocusRect.right: " + cameraFocusRect.right);
        log("cameraFocusRect.bottom: " + cameraFocusRect.bottom);

        log(" \npreview top: " + preview.getTop());
        log("preview left: " + preview.getLeft());

        int left = (int) (((cameraFocusRect.left + 1000d) / 2000d * preview.getWidth()) + preview.getLeft());
        int top = (int) (((cameraFocusRect.top + 1000d) / 2000d * preview.getHeight()) + preview.getTop());
        int right = (int) (((cameraFocusRect.right+ 1000d) / 2000d * preview.getWidth()) + preview.getLeft());
        int bottom = (int) (((cameraFocusRect.bottom + 1000d) / 2000d * preview.getHeight()) + preview.getTop());


        log(" \nleft: " + left);
        log("top: " + top);
        log("right: " + right);
        log("bottom: " + bottom);


        return new Rect(left, top, right, bottom);

    }

    private void takePicture() {
        // get an image from the camera
        mCamera.takePicture(mShutterCallback, null, mPicture);
    }

    private void fillValuesMenu(ArrayList<String> arrayList){

        ListView listView = (ListView) findViewById(R.id.list_view_values_menu);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

    }

    private void countDown() {

        final TextView textViewCentral = (TextView) findViewById(R.id.text_view_countdown);

        final Handler h = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                textViewCentral.setText(String.valueOf(countDown));
                if (countDown > 1) {
                    log("Countdown: " + countDown);
                    countDown--;
                    h.postDelayed(this, 1000);
                } else {
                    log("TimeOver");
                    animator.fadeOut(textViewCentral);
                    countDown = 3;
                    /*
                    after countdown, take as many picture as it says
                    on button_howManyPictures.getText
                     */

                    // get sequence of pictures
                    keepTakingPictures = true;
                    takePicture();

                }
            }


        };

        animator.fadeIn(textViewCentral);
        h.post(r);

    }

}
