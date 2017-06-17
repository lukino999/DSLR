package com.lukino999.dslr06;

import android.animation.Animator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Luca on 15/06/2017.
 */

public class MyAnimator extends AppCompatActivity {

    long duration = 300;

    public void fadeIn(final View v){
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1f).setDuration(duration).setListener(new Animator.AnimatorListener() {
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

    public void fadeOut(final View v){


        v.animate().alpha(0f).setDuration(duration).setListener(new Animator.AnimatorListener() {
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

    public void shutterAnimation(final View v){

        v.setAlpha(1f);
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(0f).setDuration(200).setListener(new Animator.AnimatorListener() {
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
}
