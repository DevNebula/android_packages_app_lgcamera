package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BarProgress extends ImageView {
    public static final int FORWARD = 0;
    public static final int REVERSE = 1;
    private int mCurs = 0;
    private int mHeight = 0;
    private boolean mHorizontalMode = false;
    private int mMax = 90;
    private int mWidth = 0;

    public BarProgress(Context context) {
        super(context);
    }

    public BarProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMaxValue(int maxValue) {
        this.mMax = maxValue;
    }

    public void setProgress(int value) {
        if (value > this.mMax) {
            value = this.mMax;
        }
        this.mCurs = value;
        invalidate();
    }

    public void setHorizontalMode(boolean set) {
        this.mHorizontalMode = set;
    }

    protected void onDraw(Canvas canvas) {
        this.mWidth = getWidth();
        this.mHeight = getHeight();
        if (this.mHorizontalMode) {
            canvas.clipRect(((float) this.mWidth) - (((float) this.mWidth) * (((float) this.mCurs) / ((float) this.mMax))), 0.0f, (float) this.mWidth, (float) this.mHeight);
        } else {
            canvas.clipRect(0.0f, ((float) this.mHeight) - (((float) this.mHeight) * (((float) this.mCurs) / ((float) this.mMax))), (float) this.mWidth, (float) this.mHeight);
        }
        super.onDraw(canvas);
    }
}
