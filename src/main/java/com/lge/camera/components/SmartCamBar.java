package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class SmartCamBar extends View implements OnTouchListener {
    protected static final int COLOR_PROGRESS = Color.parseColor("#4bdbbe");
    protected NinePatchDrawable mBarBackgroundBitmap;
    protected NinePatchDrawable mBarBitmap;
    protected int mBarHalfHeight = 0;
    protected int mBarHalfWidth = 0;
    protected int mBarHeight = 0;
    protected int mBarWidth = 0;
    protected int mCurValue = 0;
    protected int mCursorBorderWidth = 0;
    protected int mCursorHalfHeight = 0;
    protected int mCursorHalfWidth = 0;
    protected int mCursorHeight = 0;
    protected Drawable mCursorNormal;
    protected Rect mCursorRect = new Rect();
    protected int mCursorWidth = 0;
    protected int mDefaultBarValue = 0;
    protected int mDiff = 0;
    protected boolean mIsEnabled = true;
    protected boolean mIsTouchMoving = false;
    protected int mLayoutHalfHeight = 0;
    protected int mLayoutHalfWidth = 0;
    protected int mLayoutHeight = 0;
    protected int mLayoutWidth = 0;
    public SmartCamBarInterface mListener = null;
    protected int mMaxStep = 10;
    protected int mPreValue = 0;
    protected int mPrevDiff = 0;
    protected RotationInfo mRotationInfo = new RotationInfo();
    protected int mStartPosX = 0;

    public SmartCamBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SmartCamBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartCamBar(Context context) {
        super(context);
    }

    public void init(int min, int max, int defaultValue) {
        setOnTouchListener(this);
        initDrawables();
        this.mBarHeight = Utils.getPx(getContext(), C0088R.dimen.smart_cam_filter_slider_bar_height);
        this.mBarWidth = Utils.getPx(getContext(), C0088R.dimen.smart_cam_filter_slider_bar_width);
        this.mBarHalfWidth = this.mBarWidth / 2;
        this.mBarHalfHeight = this.mBarHeight / 2;
        configVar(min, max, defaultValue);
    }

    public void configVar(int min, int max, int defaultValue) {
        this.mMaxStep = max - min;
        this.mDefaultBarValue = defaultValue;
        this.mPreValue = this.mDefaultBarValue;
        this.mCurValue = this.mDefaultBarValue;
    }

    protected void initDrawables() {
        this.mCursorNormal = getContext().getDrawable(C0088R.drawable.ic_camera_cursor);
        this.mCursorWidth = this.mCursorNormal.getIntrinsicWidth();
        this.mCursorHeight = this.mCursorNormal.getIntrinsicHeight();
        this.mCursorHalfWidth = this.mCursorWidth / 2;
        this.mCursorHalfHeight = this.mCursorHeight / 2;
        this.mCursorBorderWidth = Utils.getPx(getContext(), C0088R.dimen.ae_control_bar_cursor_border_width);
        this.mBarBitmap = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_camera_progress);
        this.mBarBackgroundBitmap = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_camera_progress);
    }

    public void setOnSmartCamBarListener(SmartCamBarInterface listener) {
        this.mListener = listener;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mLayoutWidth == 0 || this.mLayoutHeight == 0) {
            this.mLayoutWidth = canvas.getWidth();
            this.mLayoutHeight = canvas.getHeight();
            this.mLayoutHalfWidth = this.mLayoutWidth / 2;
            this.mLayoutHalfHeight = this.mLayoutHeight / 2;
        }
        drawBar(canvas);
        setSelected(this.mIsTouchMoving);
        drawCursor(canvas);
    }

    protected void drawCursor(Canvas canvas) {
        int diff = scaleDownDiff();
        if (this.mRotationInfo != null && (this.mRotationInfo.getCurrentDegree() == 90 || this.mRotationInfo.getCurrentDegree() == 180)) {
            diff = -diff;
        }
        this.mCursorRect.set((this.mLayoutHalfWidth + diff) - this.mCursorHalfWidth, this.mLayoutHalfHeight - this.mCursorHalfHeight, (this.mLayoutHalfWidth + diff) + this.mCursorHalfWidth, this.mLayoutHalfHeight + this.mCursorHalfHeight);
        this.mCursorNormal.setVisible(true, false);
        this.mCursorNormal.setBounds(this.mCursorRect);
        this.mCursorNormal.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mCursorNormal.draw(canvas);
    }

    protected void drawBar(Canvas canvas) {
        int i = 255;
        this.mBarBackgroundBitmap.setBounds(this.mLayoutHalfWidth - this.mBarHalfWidth, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mBarHalfWidth, this.mLayoutHalfHeight + this.mBarHalfHeight);
        this.mBarBackgroundBitmap.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mBarBackgroundBitmap.draw(canvas);
        if (this.mIsEnabled && this.mRotationInfo != null) {
            switch (this.mRotationInfo.getCurrentDegree()) {
                case 0:
                case 270:
                    this.mBarBitmap.setBounds(this.mLayoutHalfWidth - this.mBarHalfWidth, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mDiff, this.mLayoutHalfHeight + this.mBarHalfHeight);
                    break;
                case 90:
                case 180:
                    this.mBarBitmap.setBounds(this.mLayoutHalfWidth - this.mDiff, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mBarHalfWidth, this.mLayoutHalfHeight + this.mBarHalfHeight);
                    break;
            }
            NinePatchDrawable ninePatchDrawable = this.mBarBitmap;
            if (!this.mIsEnabled) {
                i = 89;
            }
            ninePatchDrawable.setAlpha(i);
            this.mBarBitmap.draw(canvas);
        }
    }

    private int scaleDownDiff() {
        return (int) (((float) this.mDiff) * (((float) ((this.mBarWidth - this.mCursorHalfWidth) + (this.mCursorBorderWidth * 2))) / ((float) this.mBarWidth)));
    }

    public boolean doTouchEvent(MotionEvent event) {
        if (!this.mIsEnabled || getVisibility() != 0) {
            return true;
        }
        if (this.mListener != null && !this.mListener.isTouchAvailable()) {
            return true;
        }
        switch (event.getActionMasked() & 255) {
            case 0:
                return doTouchActionDown(event);
            case 1:
            case 3:
                return doTouchActionUp();
            case 2:
                return doTouchActionMove(event);
            case 6:
                return doTouchActionPointerUp(event);
            default:
                return true;
        }
    }

    protected boolean doTouchActionPointerUp(MotionEvent event) {
        return false;
    }

    protected void calculateCurValue() {
        if (this.mBarWidth == 0) {
            this.mBarWidth = Utils.getPx(getContext(), C0088R.dimen.smart_cam_filter_slider_bar_width);
        }
        this.mPreValue = this.mCurValue;
        this.mCurValue = this.mDefaultBarValue + Math.round(((float) (this.mDiff * this.mMaxStep)) / ((float) this.mBarWidth));
        if (this.mCurValue < 0) {
            this.mCurValue = 0;
        } else if (this.mCurValue > this.mMaxStep) {
            this.mCurValue = this.mMaxStep;
        }
        if (this.mListener != null && this.mPreValue != this.mCurValue) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
    }

    public void setCurVarValue(int curValue) {
        if (this.mMaxStep != 0) {
            this.mCurValue = curValue;
            this.mPreValue = curValue;
            this.mDiff = ((curValue - this.mDefaultBarValue) * this.mBarWidth) / this.mMaxStep;
            this.mPrevDiff = this.mDiff;
            invalidate();
        }
    }

    protected boolean doTouchActionUp() {
        this.mIsTouchMoving = false;
        this.mPrevDiff = this.mDiff;
        if (this.mListener != null) {
            this.mListener.onBarUp();
        }
        invalidate();
        return true;
    }

    protected boolean doTouchActionDown(MotionEvent event) {
        this.mIsTouchMoving = true;
        this.mStartPosX = (int) event.getX();
        invalidate();
        return true;
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        int x = (int) event.getX();
        if (this.mRotationInfo != null) {
            if (this.mRotationInfo.getCurrentDegree() == 0 || this.mRotationInfo.getCurrentDegree() == 270) {
                this.mDiff = this.mPrevDiff + (x - this.mStartPosX);
            } else {
                this.mDiff = this.mPrevDiff + (this.mStartPosX - x);
            }
        }
        if (this.mDiff <= (-this.mBarHalfWidth)) {
            this.mDiff = -this.mBarHalfWidth;
        } else if (this.mDiff >= this.mBarHalfWidth) {
            this.mDiff = this.mBarHalfWidth;
        }
        calculateCurValue();
        invalidate();
        return true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (doTouchEvent(event)) {
            return true;
        }
        return false;
    }

    public void unbind() {
        this.mBarBackgroundBitmap = null;
        this.mBarBitmap = null;
        this.mCursorNormal = null;
        this.mCursorRect = null;
        this.mRotationInfo = null;
        this.mListener = null;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    public void initValue() {
        this.mDiff = 0;
        this.mPrevDiff = 0;
        this.mCurValue = this.mDefaultBarValue;
        this.mPreValue = this.mDefaultBarValue;
    }

    public int getCurValue() {
        return this.mCurValue;
    }

    public Size getCursorSize() {
        return new Size(this.mCursorWidth, this.mCursorHeight);
    }
}
