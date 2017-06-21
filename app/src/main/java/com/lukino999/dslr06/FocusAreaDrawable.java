package com.lukino999.dslr06;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;

/**
 * Created by Luca on 21/06/2017.
 */

public class FocusAreaDrawable extends View {
    private ShapeDrawable mDrawable;
    Canvas canvas;

    public FocusAreaDrawable(Context context) {
        super(context);

        int x = 0;
        int y = 0;
        int width = 1;
        int height = 1;

        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setStyle(Paint.Style.STROKE);
        mDrawable.getPaint().setStrokeWidth(5f);
        mDrawable.getPaint().setColor(0xffffff);
        mDrawable.setBounds(x, y, x + width, y + height);
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
        this.canvas = canvas;
    }

    public void set(Rect bounds, int color){
        mDrawable.getPaint().setColor(color);
        mDrawable.setBounds(bounds);

    }
}
