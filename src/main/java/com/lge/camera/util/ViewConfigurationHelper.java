package com.lge.camera.util;

import android.content.Context;
import android.view.ViewConfiguration;
import com.lge.camera.constants.CameraConstants;

public class ViewConfigurationHelper {
    private static ViewConfigurationHelper sHelper = null;
    private final int TOUCH_MOVE_FACTOR = 20;
    private boolean sIsInit = false;
    private int sMaximumVelocity = 0;
    private int sPressedStateDuration = 0;
    private int sTapTimeout = 0;
    private int sTouchSlop = 0;

    public static ViewConfigurationHelper getHelper(Context context) {
        if (sHelper == null) {
            sHelper = new ViewConfigurationHelper();
            sHelper.initialViewConfiguration(context);
        }
        return sHelper;
    }

    public static void release() {
        sHelper = null;
    }

    private ViewConfigurationHelper() {
    }

    public void initialViewConfiguration(Context context) {
        if (context != null) {
            ViewConfiguration onfiguration = ViewConfiguration.get(context);
            this.sTouchSlop = onfiguration.getScaledTouchSlop();
            this.sMaximumVelocity = onfiguration.getScaledMaximumFlingVelocity();
            this.sTapTimeout = ViewConfiguration.getTapTimeout();
            this.sPressedStateDuration = ViewConfiguration.getPressedStateDuration();
            this.sIsInit = true;
            CamLog.m3d(CameraConstants.TAG, "sTouchSlop = " + this.sTouchSlop);
            CamLog.m3d(CameraConstants.TAG, "sMaximumVelocity = " + this.sMaximumVelocity);
            CamLog.m3d(CameraConstants.TAG, "sTapTimeout = " + this.sTapTimeout);
            CamLog.m3d(CameraConstants.TAG, "sPressedStateDuration = " + this.sPressedStateDuration);
        }
    }

    public boolean isInitialize() {
        return this.sIsInit;
    }

    public int getScaledTouchSlop() {
        return this.sTouchSlop;
    }

    public int getTouchMoveSlop() {
        return this.sTouchSlop * 20;
    }

    public int getMaxVelocity() {
        return this.sMaximumVelocity;
    }

    public int getTapTimeOut() {
        return this.sTapTimeout;
    }

    public int getPressedStateDuration() {
        return this.sPressedStateDuration;
    }
}
