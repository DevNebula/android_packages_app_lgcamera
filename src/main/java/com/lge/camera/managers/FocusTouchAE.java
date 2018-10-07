package com.lge.camera.managers;

import android.graphics.drawable.Drawable;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.LdbUtil;

public class FocusTouchAE extends FocusBase implements FocusTouchInterface {
    public FocusTouchAE(ModuleInterface iModule) {
        super(iModule);
        int focusImage = (this.mGet.isAFSupported() || !this.mGet.isRearCamera()) ? C0088R.drawable.camera_focus_front_ae : C0088R.drawable.camera_focus_rear_ae;
        Drawable drawable = this.mGet.getAppContext().getResources().getDrawable(focusImage);
        this.mRectWidth = drawable.getIntrinsicWidth();
        this.mRectHeight = drawable.getIntrinsicHeight();
    }

    protected void setTAFSize(int degree) {
        int focusImage = (this.mGet.isAFSupported() || !this.mGet.isRearCamera()) ? C0088R.drawable.camera_focus_front_ae : C0088R.drawable.camera_focus_rear_ae;
        Drawable drawable = this.mGet.getAppContext().getResources().getDrawable(focusImage);
        int rectWidth = (int) (((float) drawable.getIntrinsicWidth()) * 1.4f);
        int rectHeight = (int) (((float) drawable.getIntrinsicHeight()) * 1.4f);
        this.mRectWidth = rectWidth;
        this.mRectHeight = rectHeight;
        if (degree == 90 || degree == 270) {
            this.mRectWidth = rectHeight;
            this.mRectHeight = rectWidth;
        }
    }

    protected void updateFocusStateIndicator(int focusState) {
        switch (focusState) {
            case 11:
                int i = (this.mGet.isAFSupported() || !this.mGet.isRearCamera()) ? 9 : 10;
                setFocusViewState(i);
                setEVShutterVisiblity(true);
                return;
            default:
                CamLog.m3d(CameraConstants.TAG, "Wrong focus state!: " + this.mGet.getFocusState());
                return;
        }
    }

    protected void setFocusWindowParameters(CameraParameters parameters) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null && parameters != null) {
            CameraDeviceUtils.setEnable3ALocks(cameraDevice, false, false);
            parameters.setFocusAreas(null);
            parameters.setMeteringAreas(this.mFocusArea);
            cameraDevice.setParameters(parameters);
        }
    }

    public void startFocusByTouchPress(int x, int y, boolean bTakePicture) {
        if (FunctionProperties.isSupportedAEFocus() || bTakePicture) {
            if (this.mGet.isLightFrameOn()) {
                if (this.mHandler != null && bTakePicture) {
                    this.mHandler.sendEmptyMessage(4);
                }
            } else if (this.mCameraFocusView != null) {
                this.mCameraFocusView.setVisibility(0);
                int i = (this.mGet.isAFSupported() || !this.mGet.isRearCamera()) ? 9 : 10;
                setFocusViewState(i);
                setFocusState(0);
                setMoveNormalFocusRect(x, y, true);
                setFocusWindow(this.mFocusRect);
                setFocusState(11);
                showFocus();
                CamLog.m7i(CameraConstants.TAG, "TAE mCameraFocusView is null");
                setEVShutterButtonPosition(x, y);
                updateFocusStateIndicator();
                this.mGet.removePostRunnable(this.mReleaseTouchFocus);
                if (bTakePicture) {
                    if (this.mHandler != null) {
                        this.mHandler.sendEmptyMessageDelayed(4, 800);
                    }
                    this.mGet.postOnUiThread(this.mReleaseTouchFocus, 800);
                } else if (this.mGet.isAELock()) {
                    this.mGet.postOnUiThread(this.mStartLockAE, 1000);
                    LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_AE_LOCK);
                } else {
                    this.mGet.postOnUiThread(this.mReleaseTouchFocus, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_TOUCH_AE);
                }
            } else {
                CamLog.m7i(CameraConstants.TAG, "TAE mCameraFocusView is null");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "------startAEByTouchPress : x = " + x + ", y = " + y);
        }
    }

    public int getTouchGuideRes() {
        return (this.mGet.isAFSupported() || !this.mGet.isRearCamera()) ? C0088R.drawable.camera_focus_front_ae : C0088R.drawable.camera_focus_rear_ae;
    }

    public void releaseTouchFocus() {
        CamLog.m3d(CameraConstants.TAG, "releaseTouchFocus");
        cancelAutoFocus();
        hideFocus();
        initFocusAreas();
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(6);
        }
    }

    public void releaseFocusHandler() {
        super.releaseFocusHandler();
    }

    public void release() {
        super.release();
    }

    public boolean cancelAutoFocus() {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice == null || !this.mGet.checkModuleValidate(15)) {
            return false;
        }
        cameraDevice.cancelAutoFocus();
        return true;
    }

    public void unregisterCallback() {
    }

    public void startFocusByTouchPressForTracking(int x, int y) {
    }

    public void onAEControlBarDown() {
        releaseFocusHandler();
    }

    public void onAEControlBarUp() {
        this.mGet.postOnUiThread(this.mReleaseTouchFocus, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
    }

    public boolean hideFocusForce() {
        return super.hideFocus();
    }
}
