package com.lge.camera.components;

import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.ColorUtil;

public class ShutterButtonBase extends ImageButton implements OnTouchListener {
    protected static final int SHUTTER_LONG_PRESS = 1;
    protected static final int SHUTTER_NONE = 0;
    protected static final int SHUTTER_ZOOM = 2;
    protected final float SHUTTER_ZOOM_MAX_DISTANCE_RATIO;
    protected final int TOUCH_CONSTANT;
    private ColorMatrixColorFilter mAlphaFilter = ColorUtil.getAlphaMatrix(0.3f);
    protected ImageView mFadeInView = null;
    private boolean mIsBigShutterButton;
    protected boolean mIsShutterZoomAvailable = false;
    protected float mLastRawX = 0.0f;
    protected float mLastX = 0.0f;
    private boolean mPrevPressedState;
    private int mPrevResid;
    protected int mShutterState = 0;
    protected int mShutterZoomInvokeDistance = 35;
    protected OnShutterZoomListener mShutterZoomListener;
    protected int mShutterZoomMaxDistance = 220;

    public interface OnShutterZoomListener {
        boolean moveShutter(int i);

        void onTouchUp();

        void stopShutter();
    }

    public ShutterButtonBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TOUCH_CONSTANT = ModelProperties.getRawAppTier() >= 5 ? 7 : 8;
        this.SHUTTER_ZOOM_MAX_DISTANCE_RATIO = 0.92f;
        calShutterZoomDistance(context);
        setOnTouchListener(this);
    }

    protected void calShutterZoomDistance(Context context) {
        this.mShutterZoomMaxDistance = (((int) (((float) context.getDrawable(C0088R.drawable.camera_shutter_zoom).getIntrinsicWidth()) * 0.92f)) - context.getDrawable(C0088R.drawable.shutter_stroke_normal).getIntrinsicWidth()) / 2;
        this.mShutterZoomInvokeDistance = context.getDrawable(C0088R.drawable.shutter_stroke_normal).getIntrinsicWidth() / this.TOUCH_CONSTANT;
    }

    public void setOnShutterButtonTouchListener(boolean set) {
        setOnTouchListener(set ? this : null);
    }

    public void setOnShutterZoomListener(OnShutterZoomListener listener) {
        this.mShutterZoomListener = listener;
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (this.mPrevPressedState != pressed && isBigShutterButton() && setFadeInView()) {
            Drawable background = this.mFadeInView.getBackground();
            if (background != null) {
                if (pressed) {
                    background.setColorFilter(this.mAlphaFilter);
                } else {
                    background.clearColorFilter();
                }
                this.mPrevPressedState = pressed;
            }
        }
    }

    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        this.mIsBigShutterButton = resid == C0088R.drawable.btn_shutter_stroke;
        if (setFadeInView() && this.mPrevResid != resid) {
            this.mFadeInView.getBackground().clearColorFilter();
        }
        this.mPrevResid = resid;
    }

    public boolean isBigShutterButton() {
        return this.mIsBigShutterButton;
    }

    private boolean setFadeInView() {
        if (this.mFadeInView != null) {
            return true;
        }
        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) {
            return false;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) instanceof RotateImageButton) {
                this.mFadeInView = (ImageView) parent.getChildAt(i);
                break;
            }
        }
        return this.mFadeInView != null;
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                return onTouchDown(view, event);
            case 1:
                return onTouchUp(view, event);
            case 2:
                return onTouchMove(view, event);
            case 3:
                return onTouchCancel(view, event);
            default:
                return false;
        }
    }

    protected boolean onTouchDown(View view, MotionEvent event) {
        this.mLastRawX = event.getRawX();
        this.mLastX = event.getX();
        this.mShutterState = 0;
        return false;
    }

    protected boolean onTouchMove(View view, MotionEvent event) {
        if (this.mShutterState != 2 || this.mShutterZoomListener == null) {
            if (this.mIsShutterZoomAvailable) {
                if (Math.abs((int) (this.mLastX - event.getX())) >= this.mShutterZoomInvokeDistance && this.mShutterZoomListener != null && this.mShutterZoomListener.moveShutter(getShutterZoomDistance((int) event.getRawX()))) {
                    this.mShutterState = 2;
                    return true;
                }
            }
            return false;
        }
        this.mShutterZoomListener.moveShutter(getShutterZoomDistance((int) event.getRawX()));
        return true;
    }

    protected boolean onTouchUp(View view, MotionEvent event) {
        if (this.mShutterState == 2) {
            if (this.mShutterZoomListener != null) {
                this.mShutterZoomListener.stopShutter();
            }
            if (this.mFadeInView != null) {
                Drawable background = this.mFadeInView.getBackground();
                if (background != null) {
                    background.clearColorFilter();
                }
            }
            this.mShutterState = 0;
            setPressed(false);
            return true;
        } else if (this.mShutterZoomListener == null) {
            return false;
        } else {
            this.mShutterZoomListener.onTouchUp();
            return false;
        }
    }

    protected boolean onTouchCancel(View view, MotionEvent event) {
        if (this.mShutterState == 2) {
            if (this.mShutterZoomListener != null) {
                this.mShutterZoomListener.stopShutter();
            }
            if (this.mFadeInView != null) {
                Drawable background = this.mFadeInView.getBackground();
                if (background != null) {
                    background.clearColorFilter();
                }
            }
            this.mShutterState = 0;
            setPressed(false);
            return true;
        } else if (this.mShutterZoomListener == null) {
            return false;
        } else {
            this.mShutterZoomListener.onTouchUp();
            return false;
        }
    }

    public void setShutterZoomAvailable(boolean set) {
        this.mIsShutterZoomAvailable = set;
    }

    protected int getShutterZoomDistance(int rawX) {
        int distance = Math.min(this.mShutterZoomMaxDistance, Math.max(-this.mShutterZoomMaxDistance, (int) (((float) rawX) - this.mLastRawX)));
        if (distance < 0) {
            if (distance - this.mShutterZoomInvokeDistance < (-this.mShutterZoomMaxDistance)) {
                return -this.mShutterZoomMaxDistance;
            }
            return distance;
        } else if (this.mShutterZoomInvokeDistance + distance > this.mShutterZoomMaxDistance) {
            return this.mShutterZoomMaxDistance;
        } else {
            return distance;
        }
    }

    public int getShutterZoomMaxDistance() {
        return this.mShutterZoomMaxDistance;
    }
}
