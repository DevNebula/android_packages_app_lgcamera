package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public abstract class BarView extends BarViewBase {
    private BarManagerListener mListener = null;

    public interface BarManagerListener {
        void resetBarDisappearTimer(int i, int i2);

        boolean setBarSetting(String str, String str2, boolean z);
    }

    public abstract void getBarSettingValue();

    public abstract void releaseBar();

    public abstract void setBarEnable(boolean z);

    public abstract void updateExtraInfo(String str);

    public void setBarListener(BarManagerListener listener) {
        this.mListener = listener;
    }

    public BarManagerListener getBarListener() {
        return this.mListener;
    }

    public BarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarView(Context context) {
        super(context);
    }

    public void setBarValueInitialization(int maxStep, int minStep, int onStepMinus, int oneStepPlus, String barSettingKey) {
        this.mCursorMaxStep = maxStep;
        sCURSOR_MIN_STEP = minStep;
        sCURSOR_ONE_STEP_MINUS = onStepMinus;
        sCURSOR_ONE_STEP_PLUS = oneStepPlus;
        this.mBarSettingKey = barSettingKey;
    }

    public void setBarResources(int cursor, int cursorBG, int minus, int minusView, int plus, int plusView, int cursorText) {
        this.cursorResId = cursor;
        this.mCursorBGResId = cursorBG;
        this.minusButtonResId = minus;
        this.minusButtonViewResId = minusView;
        this.plusButtonResId = plus;
        this.plusButtonViewResId = plusView;
        this.cursorTextId = cursorText;
    }

    public void showControl(boolean visible) {
        int i = 0;
        if (this.mInitial) {
            CamLog.m3d(CameraConstants.TAG, "BarView-showControl:mValue = " + this.mValue + " visible=" + visible);
            if (visible) {
                setDisplayTimeout();
            } else {
                findViewById(this.cursorResId).setPressed(false);
                findViewById(this.plusButtonViewResId).setPressed(false);
                findViewById(this.minusButtonViewResId).setPressed(false);
                stopTimerTask();
                if (getVisibility() == 0) {
                    releaseBar();
                }
            }
            if (!visible) {
                i = 8;
            }
            setVisibility(i);
        }
    }

    public void showExtraButton(boolean plusButtonVisible, boolean minusButonVisible) {
        int minus = 0;
        if (this.mInitial) {
            int pluse;
            if (plusButtonVisible) {
                pluse = 0;
            } else {
                pluse = 4;
            }
            if (!minusButonVisible) {
                minus = 4;
            }
            RotateLayout rl = getBarLayout();
            if (rl == null) {
                CamLog.m3d(CameraConstants.TAG, "Exit because layout is null");
            } else if (rl.getAngle() == 90) {
                findViewById(this.minusButtonViewResId).setVisibility(pluse);
                findViewById(this.plusButtonViewResId).setVisibility(minus);
            } else {
                findViewById(this.plusButtonViewResId).setVisibility(pluse);
                findViewById(this.minusButtonViewResId).setVisibility(minus);
            }
        }
    }

    public void initCursor() {
        findViewById(this.cursorResId).setPressed(false);
    }

    public void setBarValue(int value) {
        setCursorValue(value);
        refreshBar();
    }

    public void refreshBar() {
        setCursor(this.mValue);
    }

    public void resetCursor(int value) {
        setCursor(value);
        updateAllBars();
    }

    public void resetValue(int value) {
        setCursorValue(value);
    }

    public int getValue() {
        return this.mValue;
    }

    public void setCursorMaxStep(int maxStep) {
        this.mCursorMaxStep = maxStep;
    }

    public int getCursorMaxStep() {
        return this.mCursorMaxStep;
    }

    public int changeDegreeFixedPortrait(int degree) {
        int changeDegree = degree;
        if (degree == 0) {
            return 270;
        }
        if (degree == 180) {
            return 90;
        }
        return changeDegree;
    }

    protected void disallowTouchInParentView(View view) {
    }

    public void unbind() {
        releaseBar();
        this.mListener = null;
    }
}
