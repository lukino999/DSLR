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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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

import static android.content.ContentValues.TAG;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1002;
    private static String appName = "DSLR";
    final CameraFunctionsList cameraFunctionsList = new CameraFunctionsList();
    // sets fullscreen declarations
    private final Handler mHideHandler = new Handler();
    MediaActionSound mediaActionSound =  new MediaActionSound();
    // this will be used to inflate an xml to its own View obj
    LayoutInflater controlInflater = null;
    boolean menuViewVisible = false;
    int focusAreaSize = 200;
    MyAnimator animator = new MyAnimator();
    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            log("Autofocus has been successful: " + success);
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
    private boolean isPictureSequenceEnabled = false;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            onPictureTakenCode(data);
        }
    };
    private Camera.Parameters mCameraParameters;
    private ListView menuView;
    private View whoIsUsingTheMenuView;

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
        log("------Camera parameters------------------------------------------------------");

        log(c.getParameters().flatten().replace(";", "\n"));
        log("-----------------------------------------------------------------------------");

        return c; // returns null if camera is unavailable
    }

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

    // log
    private static void log(String s) {
        System.out.println(s);
    }

    private void setFullscreen(){
        // makes fullscreen
        mContentView = findViewById(R.id.fullscreen_content);
        mHideHandler.post(mHidePart2Runnable);
        System.out.println("SetFullscreen");
    }

    private void onPictureTakenCode(byte[] data) {
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
            TextView howManyPicturesLeft = (TextView) findViewById(R.id.text_view_how_many_pictures);
            int picturesLeftToTake = Integer.parseInt(howManyPicturesLeft.getText().toString());
            System.out.println("Pictures left: " + picturesLeftToTake);

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


        // this inflates the overlay_button_take_picture_button_take_picture.xml file to the View viewControl
        controlInflater = LayoutInflater.from(getBaseContext());

        View viewControl = controlInflater.inflate(R.layout.controls_camera, null);

        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);

        this.addContentView(viewControl, layoutParamsControl);

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
        log(" - - - - - - - - - - - onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
        log("AppName " + appName);

        startCamera();



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

    /*
    Update menu with values returned by CameraParameters.get(keyAvailableValues)
    If menu is already up, whoIsCalling tells whether to toggle off or update
     */
    private void updateValuesMenu(final int i, final TextView whoIsCalling){

        final String keyAvailableValues = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.AVAILABLE_VALUES).toString();
        final String keyCurrentValue = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.VALUE).toString();
        final String label = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.LABEL).toString();
        final ListView listView = (ListView) findViewById(R.id.list_view_values_menu);



        if (mCameraParameters.get(keyAvailableValues) != null) {
            // ---------------------------------------------------------------------
            if (menuViewVisible && (whoIsUsingTheMenuView == whoIsCalling)) {
                // toggle OFF
                log("Update menu:: toggle OFF");
                animator.fadeOut(listView);
                menuViewVisible = false;
                whoIsUsingTheMenuView = null;
                setFullscreen();
            } else {
                // toggle ON
                log("Update menu:: toggle ON");
                whoIsUsingTheMenuView = whoIsCalling;
                menuViewVisible = true;
                // get the availableValues as string[]
                final String[] availableValues = mCameraParameters.get(keyAvailableValues).split(",");


                //convert it to ArrayList
                final ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(availableValues));
                // fill the menuView
                fillValuesMenu(arrayList);


                String currentValueString = mCameraParameters.get(keyCurrentValue);
                //find current value index
                final int currentValueIndex = arrayList.indexOf(currentValueString);
                log("currentValueString " + currentValueString + "   -   currentValueInt: " + currentValueIndex);



                ViewTreeObserver vto = listView.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        log("vto.onGlobalLayout");

                        // remove this listener
                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);


                        // show the menuView
                        animator.fadeIn(listView);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                log(" mainMenu.setOnItemSelectedListener");
                                view.getFocusables(position);
                                view.setSelected(true);
                                mCameraParameters.set(keyCurrentValue, availableValues[position]);
                                mCamera.setParameters(mCameraParameters);
                                setMenuItemLabel(whoIsCalling, i);
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

    private void setControls() {

        mCameraParameters = mCamera.getParameters();



        initializeButtonCapture();

        initializeCameraPreviewAutofocus();

        //initializeButtonHowManyPictures();

        initializeMainMenu();

        initializeZoom();

        initializeButtonShowMenu();

        initializePictureLeftMenu();

    }

    private void initializePictureLeftMenu() {
        final ListView howManyPicturesMenu = (ListView) findViewById(R.id.list_view_how_many_pictures);
        final TextView textViewHowMany = (TextView) findViewById(R.id.text_view_how_many_pictures);
        String[] howManyPicsMenuItems = {"+10", "+1", "RESET", "-1", "-10", "START"};
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
        final SeekBar seekBarZoom = (SeekBar) findViewById(R.id.seekbar_zoom);
        final TextView textViewZoom = (TextView) findViewById(R.id.text_view_zoom);
        // get how many steps
        List zoomRatiosList = mCameraParameters.getZoomRatios();

        if (zoomRatiosList != null) {
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
        }




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
    }

    private void initializeMainMenu() {

        final ListView mainMenu = (ListView) findViewById(R.id.list_view_main_menu);
        ArrayList<String> functionsArrayList = new ArrayList<>();

        // populate the functionsArrayList with the values in CameraFunctionList
        for (Map m : cameraFunctionsList.availableFuntions){
            log(m.toString());
            functionsArrayList.add(m.get(cameraFunctionsList.LABEL).toString());
        }

        // populate the mainMenu ListView
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,
                        functionsArrayList);

        mainMenu.setAdapter(arrayAdapter);


        // once mainMenu is drawn, initialize each item
        final ViewTreeObserver vto = mainMenu.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vto.removeOnGlobalLayoutListener(this);

                TextView mainMenuItem;

                for (int i = 0; i < mainMenu.getChildCount(); i++){
                    mainMenuItem = (TextView) mainMenu.getChildAt(i);
                    initializeButton(mainMenuItem, i);
                }

                mainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        log("mainMenu.setOnItemClickListener");
                        updateValuesMenu(position, (TextView) view);
                    }
                });

            }
        });

        mainMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });





    }

    // initialize button
    private void initializeButton(TextView view, final int i ){

        final String keyAvailableValues = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.AVAILABLE_VALUES).toString();
        final String keyCurrentValue = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.VALUE).toString();

        setMenuItemLabel(view, i);

        /*
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("onClick:: " + keyCurrentValue);
                updateValuesMenu(i, (TextView) v);
            }
        });
        */

    }

    private void setMenuItemLabel(TextView view, final int i) {

        final String keyCurrentValue = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.VALUE).toString();
        final String label = cameraFunctionsList.availableFuntions.get(i).get(cameraFunctionsList.LABEL).toString();
        view.setText(label +": " + mCameraParameters.get(keyCurrentValue));

    }

    private void initializeButtonCapture() {

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
        // focus touching the preview
        FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                log("cameraPreview.OnTouch");

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

                log("getX, getY: " + event.getX() + ", " + event.getY());
                log("xRect, yRect: " + xRect + ", " + yRect);

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

                log("Camera.Parameters.FOCUS_MODE_AUTO: " + Camera.Parameters.FOCUS_MODE_AUTO);
                log("Camera.Parameters.FOCUS_MODE_MACRO: " + Camera.Parameters.FOCUS_MODE_MACRO);
                log("mCameraParameters.getFocusMode(): " + mCameraParameters.getFocusMode());
                log("mCameraParameters.get(\"focus-mode\") " + mCameraParameters.get("focus-mode"));
                log("-----------------------------------");

                if (mCameraParameters.get("focus-mode").equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
                        mCameraParameters.get("focus-mode").equals(Camera.Parameters.FOCUS_MODE_MACRO)){
                    log("autofocus");
                    mCamera.autoFocus(autoFocusCallback);
                }

                return false;
            }
        });
    }

    private void takePicture() {
        // get an image from the camera
        mCamera.takePicture(null, null, mPicture);
    }

    private void fillValuesMenu(ArrayList<String> arrayList){

        ListView listView = (ListView) findViewById(R.id.list_view_values_menu);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

    }

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
