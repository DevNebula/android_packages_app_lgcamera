package com.lge.camera.components;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.lge.camera.components.ShutterButtonBase.OnShutterZoomListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class ShutterButton extends ShutterButtonBase {
    public static final int LONG_SHUTTER_DELAY = 500;
    protected OnShutterButtonListener mListener;
    private int mLongShutterDelay = 500;
    private boolean mOldPressed;
    private CheckForLongPress mPendingCheckForLongPress;

    public interface OnShutterButtonListener {
        Handler getHandler();

        void onShutterButtonClick();

        void onShutterButtonFocus(boolean z);

        void onShutterButtonLongClick();

        void onShutterTouchUp();
    }

    class CheckForLongPress implements Runnable {
        CheckForLongPress() {
        }

        public void run() {
            if (ShutterButton.this.mShutterState != 2 && ShutterButton.this.performLongClick()) {
                ShutterButton.this.mShutterState = 1;
            }
        }
    }

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        calShutterZoomDistance(context);
    }

    public void setOnShutterButtonListener(OnShutterButtonListener listener) {
        this.mListener = listener;
        setOnTouchListener(this);
    }

    public void setOnShutterZoomListener(OnShutterZoomListener listener) {
        this.mShutterZoomListener = listener;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        if (pressed != this.mOldPressed) {
            if (pressed) {
                callShutterButtonFocus(pressed);
            } else {
                post(new Runnable() {
                    public void run() {
                        ShutterButton.this.callShutterButtonFocus(pressed);
                    }
                });
            }
            this.mOldPressed = pressed;
        }
    }

    private void callShutterButtonFocus(boolean pressed) {
        if (this.mListener != null) {
            this.mListener.onShutterButtonFocus(pressed);
        }
    }

    public boolean performClick() {
        boolean result = super.performClick();
        if (this.mListener != null) {
            removeLongPressCallback();
            if (this.mShutterState == 2) {
                this.mShutterState = 0;
            } else if (isEnabled()) {
                this.mListener.onShutterButtonClick();
            }
        }
        return result;
    }

    protected boolean onTouchDown(View view, MotionEvent event) {
        postCheckForLongClick(this.mLongShutterDelay);
        return super.onTouchDown(view, event);
    }

    protected boolean onTouchMove(View view, MotionEvent event) {
        if (this.mShutterState != 2 || this.mShutterZoomListener == null) {
            if (this.mIsShutterZoomAvailable) {
                int deltaX = Math.abs((int) (this.mLastX - event.getX()));
                if (this.mShutterState != 1 && deltaX >= this.mShutterZoomInvokeDistance) {
                    removeLongPressCallback();
                    if (this.mShutterZoomListener != null && this.mShutterZoomListener.moveShutter(getShutterZoomDistance((int) event.getRawX()))) {
                        this.mShutterState = 2;
                        return true;
                    }
                }
            }
            return false;
        }
        this.mShutterZoomListener.moveShutter(getShutterZoomDistance((int) event.getRawX()));
        return true;
    }

    protected boolean onTouchUp(View view, MotionEvent event) {
        if (this.mListener != null) {
            this.mListener.onShutterTouchUp();
        }
        if (!(super.onTouchUp(view, event) || this.mShutterState == 1)) {
            removeLongPressCallback();
        }
        return false;
    }

    protected boolean onTouchCancel(View view, MotionEvent event) {
        if (!(super.onTouchCancel(view, event) || this.mShutterState == 1)) {
            removeLongPressCallback();
        }
        return false;
    }

    private void postCheckForLongClick(int delayOffset) {
        this.mShutterState = 0;
        if (this.mPendingCheckForLongPress == null) {
            this.mPendingCheckForLongPress = new CheckForLongPress();
        }
        if (this.mListener != null) {
            Handler handler = this.mListener.getHandler();
            if (handler != null) {
                handler.postDelayed(this.mPendingCheckForLongPress, (long) delayOffset);
            }
        }
    }

    private void removeLongPressCallback() {
        if (this.mPendingCheckForLongPress != null && this.mListener != null) {
            Handler handler = this.mListener.getHandler();
            if (handler != null) {
                handler.removeCallbacks(this.mPendingCheckForLongPress);
            }
        }
    }

    public boolean performLongClick() {
        if (this.mListener == null) {
            return false;
        }
        if (isEnabled()) {
            this.mListener.onShutterButtonLongClick();
        }
        return true;
    }

    public void unbind() {
        this.mListener = null;
        setOnTouchListener(null);
        this.mPendingCheckForLongPress = null;
    }

    public boolean isShutterButtonLongPress() {
        return this.mShutterState == 1;
    }

    public void setLongShutterDelay(int delay) {
        this.mLongShutterDelay = delay;
        CamLog.m3d(CameraConstants.TAG, "-ls- setShutterLongDelay = " + this.mLongShutterDelay);
    }
}
