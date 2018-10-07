package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class OpticSwitchableBar extends SwitchableBar {
    private static final int SWITCH_PREVENT_THRESHOLD = 20;

    public OpticSwitchableBar(Context arg0) {
        super(arg0);
    }

    public OpticSwitchableBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpticSwitchableBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isSwitching() {
        return false;
    }

    protected boolean checkGestureZoomAvailable(int step, int gapSpan, boolean isGestureGoing) {
        return true;
    }

    protected boolean isCursorInSlowMovingArea() {
        return false;
    }

    public boolean isJogZoomAvailable(int value, int factor) {
        setBarValue(value);
        correctCenterCoordination();
        invalidate();
        return true;
    }

    protected void calculateCursorPosition(int value, boolean correctPosition) {
        this.mCursorCenterY = calcultateCursorPositionByZoomValue(value, correctPosition);
        CamLog.m7i(CameraConstants.TAG, " mCursorCenterY =" + this.mCursorCenterY);
    }

    private float calcultateCursorPositionByZoomValue(int value, boolean correctPosition) {
        float position = ((float) this.mBarBottom) - (this.mSTEP_UNIT * ((float) value));
        if (!correctPosition) {
            return position;
        }
        if (Float.compare(position, (float) this.mBarTop) < 0) {
            position = (float) this.mBarTop;
        }
        if (Float.compare(position, (float) this.mBarBottom) > 0) {
            return (float) this.mBarBottom;
        }
        return position;
    }

    public void setUpperBarMode(boolean upperBarMode) {
    }

    protected void calculateNecessaryValues() {
        this.mTouchableAreaTop = this.mBarTop - this.mTouchableAreaMargin;
        this.mTouchableAreaBottom = this.mBarBottom + this.mTouchableAreaMargin;
        this.mCurrentBarHeight = this.mBarBottom - this.mBarTop;
        if (this.mMaxValue != 0) {
            this.mSTEP_UNIT = ((float) this.mCurrentBarHeight) / ((float) this.mMaxValue);
        }
    }

    protected void correctCenterCoordination() {
        if (Float.compare(this.mCursorCenterY, (float) this.mBarTop) < 0) {
            this.mCursorCenterY = (float) this.mBarTop;
        }
        if (Float.compare(this.mCursorCenterY, (float) this.mBarBottom) > 0) {
            this.mCursorCenterY = (float) this.mBarBottom;
        }
    }

    protected boolean checkTouchEnableCondition(float x, float y) {
        if (!isInTouchableArea(x, y) && !this.mIsBarTouched) {
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Out of touchable area, return");
            return false;
        } else if (checkBarMovable(y)) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Bar's not movable, return");
            return false;
        }
    }

    protected boolean isSwitchingNeeded(boolean isCursorGoingDown, boolean isCursorGoingUp) {
        if (this.mIsUpperBarMode && this.mPreventChangeCamera) {
            float moveDistance = this.mCursorCenterY - ((float) this.mBorderCenter);
            if (Math.abs(moveDistance) < 20.0f) {
                CamLog.m7i(CameraConstants.TAG, " ignore touch event , Distance = " + moveDistance);
                return true;
            }
        }
        this.mPreventChangeCamera = false;
        return false;
    }

    protected void calcultateZoomValueByCursorPosition(float y) {
        if (Float.compare(y, (float) this.mBarTop) < 0) {
            y = (float) this.mBarTop;
        }
        if (Float.compare(y, (float) this.mBarBottom) > 0) {
            y = (float) this.mBarBottom;
        }
        this.mToBeValue = (int) ((Math.abs(((float) this.mBarBottom) - y) * ((float) this.mMaxValue)) / ((float) this.mCurrentBarHeight));
        CamLog.m3d(CameraConstants.TAG, "mToBeValue = " + this.mToBeValue);
        CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] y = " + y);
    }

    public void switchBar() {
    }

    protected float getDistance() {
        return Math.abs(this.mCursorCenterY - ((float) this.mBarBottom));
    }

    public int drawExceedsLevels() {
        return -1;
    }

    public boolean isDrawingExceedsLevels() {
        return false;
    }

    public void stopDrawingExceedsLevels() {
    }

    public void setBorderPosition(int step) {
        this.mBorderTop = (int) calcultateCursorPositionByZoomValue(step, true);
        this.mBorderCenter = this.mBorderTop + (getContext().getDrawable(C0088R.drawable.bg_camera_inout_progress).getIntrinsicHeight() / 2);
        this.mCursorCenterY = (float) this.mBorderCenter;
    }
}
