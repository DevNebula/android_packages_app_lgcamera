package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.util.Utils;

public class RecProgressBar extends View {
    public static final int FORWARD = 0;
    public static final int REVERSE = 1;
    private int[] lcdSize;
    private Paint mBackgroundPaint = new Paint(1);
    private int mCurs = 0;
    private int mDirection = 0;
    private int mDrawWidth;
    private Paint mForegroundPaint = new Paint(1);
    private int mHeight;
    private int mMax = 5000;
    private int mPadding = 0;
    protected RotationInfo mRotationInfo = new RotationInfo();
    private float mStep;
    private int mTopPadding;
    private int mTopPaddingBottom;
    private int mTopPaddingEnd;
    private int mWidth;

    public RecProgressBar(Context context) {
        super(context);
        init();
    }

    public RecProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.lcdSize = Utils.getLCDsize(getContext(), true);
        this.mBackgroundPaint.setColor(Color.argb(128, 255, 255, 255));
        this.mForegroundPaint.setColor(Color.argb(255, 75, 219, 190));
        this.mHeight = (int) Utils.dpToPx(getContext(), 4.0f);
        this.mWidth = (int) Utils.dpToPx(getContext(), 224.0f);
        this.mDrawWidth = this.mWidth - (this.mPadding * 2);
        this.mStep = ((float) this.mMax) / ((float) this.mDrawWidth);
    }

    public int getMax() {
        return this.mMax;
    }

    public void setProgress(int value) {
        if (value > this.mMax) {
            value = this.mMax;
        }
        this.mCurs = value;
        invalidate();
    }

    public void setTopPadding(int topPaddingEnd, int topPaddingButtom) {
        this.mTopPaddingEnd = topPaddingEnd;
        this.mTopPaddingBottom = topPaddingButtom;
    }

    public void setDegree(int degree) {
        if (this.mRotationInfo != null) {
            this.mRotationInfo.setDegree(degree, false);
        }
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        Rect rect;
        if (this.mRotationInfo != null) {
            if (this.mRotationInfo.getCurrentDegree() == 0) {
                this.mTopPadding = this.lcdSize[0] - this.mTopPaddingEnd;
            } else if (this.mRotationInfo.getCurrentDegree() == 90) {
                this.mTopPadding = this.mTopPaddingBottom;
            } else if (this.mRotationInfo.getCurrentDegree() == 180) {
                this.mTopPadding = this.mTopPaddingEnd;
            } else if (this.mRotationInfo.getCurrentDegree() == 270) {
                this.mTopPadding = this.lcdSize[1] - this.mTopPaddingBottom;
            }
        }
        canvas.drawRect(new Rect(0, this.mTopPadding, this.mWidth, this.mHeight + this.mTopPadding), this.mBackgroundPaint);
        int value = (int) (((float) this.mCurs) / this.mStep);
        if (this.mDirection == 0) {
            rect = new Rect(0, this.mTopPadding, value, this.mHeight + this.mTopPadding);
        } else {
            rect = new Rect(this.mDrawWidth - value, 0, this.mDrawWidth, this.mHeight);
        }
        canvas.drawRect(rect, this.mForegroundPaint);
        super.onDraw(canvas);
    }
}
