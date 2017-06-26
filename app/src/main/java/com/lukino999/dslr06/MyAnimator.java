package com.lukino999.dslr06;

import android.animation.Animator;
import android.media.MediaActionSound;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Luca on 15/06/2017.
 */

public class MyAnimator extends AppCompatActivity {

    // fadeIn and fadeOut duration
    long fadeDuration = 300;

    long shutterDuration = 100;

    private Handler shutterHandler = new Handler();

    private View shutterView;

    private TextView tempTextView;

    private Handler tempTextViewHandler = new Handler();

    Runnable run = new Runnable() {
        @Override
        public void run() {
            tempTextView.setVisibility(View.INVISIBLE);
        }
    };


    // fadesIn the View v
    public void fadeIn(final View v){
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1f).setDuration(fadeDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    // fadesOut the View v
    public void fadeOut(final View v){


        v.animate().alpha(0f).setDuration(fadeDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }






    Runnable open = new Runnable() {
        @Override
        public void run() {
            shutterView.animate().alpha(1f).setDuration(250);
        }
    };

    // blacks out the camera prewiew to simulate the shutter
    public void shutter(final View v){

        this.shutterView = v;

        v.animate().alpha(0f).setDuration(250);

        shutterHandler.postDelayed(open, shutterDuration);

    }


    // temporaryTextView
    // fades out after textFieldFadeOutDelay ms
    boolean fadeOutHasBeenCalled = false;
    public void tempTextView(final TextView v, final String text, long textFieldFadeOutDelay){

        // remove any scheduled post
        tempTextViewHandler.removeCallbacks(run);

        // Set view text
        v.setText(text);

        // fadeIn
        v.setVisibility(View.VISIBLE);

        // assign tempTextView as it has to be in the main class
        tempTextView = v;

        // Schedule fade out
        tempTextViewHandler.postDelayed(run, textFieldFadeOutDelay);

    }




}
