package com.lge.camera.managers;

import android.view.View;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CameraAFMoveCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;

public class FocusMove extends FocusBaseMulti {
    private boolean mIsFaceDetected = false;
    private boolean mIsManual = false;

    class CameraAutoFocusMoveCallback implements CameraAFMoveCallback {
        CameraAutoFocusMoveCallback() {
        }

        public void onAutoFocusMoving(boolean moving, CameraProxy camera) {
            CamLog.m11w(CameraConstants.TAG, "onAutoFocusMoving: start = " + moving + ", mIsFaceDetected = " + FocusMove.this.mIsFaceDetected);
            if (!FocusMove.this.mGet.isFocusEnableCondition() || !FocusMove.this.mGet.checkModuleValidate(159) || FocusMove.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
                CamLog.m3d(CameraConstants.TAG, "Focusing checkModuleValidate. return");
            } else if (FocusMove.this.mGet.getFocusState() == 1 || FocusMove.this.mGet.getFocusState() == 2 || FocusMove.this.mGet.getFocusState() == 13 || FocusMove.this.mGet.getFocusState() == 12) {
                CamLog.m3d(CameraConstants.TAG, "Focusing state. return");
            } else if ("auto".equals(FocusMove.this.mGet.getCameraFocusMode())) {
                CamLog.m3d(CameraConstants.TAG, "Focus mode auto. return;");
            } else {
                if (moving) {
                    FocusMove.this.mGet.removePostRunnable(FocusMove.this.mReleaseTouchFocus);
                    if (!FocusMove.this.mIsManual) {
                        FocusMove.this.hideFocus();
                    }
                    FocusMove.this.setFocusState(!FocusMove.this.mGet.isMWAFSupported() ? 5 : 8);
                } else {
                    FocusMove.this.setFocusState(!FocusMove.this.mGet.isMWAFSupported() ? 6 : 9);
                }
                if (!FocusMove.this.mIsFaceDetected) {
                    FocusMove.this.updateFocusStateIndicator();
                }
            }
        }
    }

    public void setFaceDetected(boolean bFaceDetected) {
        this.mIsFaceDetected = bFaceDetected;
    }

    public void setCameraMultiFocusView(View view, boolean manual) {
        this.mIsManual = manual;
        setCameraFocusView(view);
    }

    public FocusMove(ModuleInterface iModule) {
        super(iModule);
    }

    public void registerCallback() {
        CamLog.m3d(CameraConstants.TAG, "### registerCallback move callback");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            if (this.mGet.checkModuleValidate(192)) {
                initFocusAreas();
                cameraDevice.setAutoFocusMoveCallback(this.mGet.getHandler(), new CameraAutoFocusMoveCallback());
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "prevent registering move callback when recording state");
        }
    }

    public void unregisterCallback() {
        CamLog.m3d(CameraConstants.TAG, "### unregisterCallback move callback");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            cameraDevice.setAutoFocusMoveCallback(this.mGet.getHandler(), null);
        }
    }

    public boolean cancelAutoFocus() {
        String focusMode = ParamConstants.FOCUS_MODE_MULTIWINDOWAF;
        if (this.mGet.checkModuleValidate(128)) {
            if (!this.mGet.isAFSupported()) {
                CamLog.m3d(CameraConstants.TAG, "-Move focus- moveFocus is alive in AF not supported camera type");
                focusMode = ParamConstants.FOCUS_MODE_FIXED;
            } else if (!this.mGet.isMWAFSupported()) {
                focusMode = ParamConstants.FOCUS_MODE_CONTINUOUS_PICTURE;
            }
            if (this.mGet.isManualFocusMode()) {
                focusMode = "normal";
            }
        } else {
            focusMode = ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO;
        }
        this.mFocusArea = null;
        this.mGet.setCameraFocusMode(focusMode);
        CamLog.m3d(CameraConstants.TAG, "## cancelAutoFocus Focus Move : " + focusMode);
        return true;
    }

    protected void releaseTouchFocus() {
        hideFocus();
    }

    public boolean showFocus() {
        String shotMode = this.mGet.getShotMode();
        if (CameraConstants.MODE_POPOUT_CAMERA.equals(shotMode) || CameraConstants.MODE_SMART_CAM.equals(shotMode) || CameraConstants.MODE_REAR_OUTFOCUS.equals(shotMode)) {
            return false;
        }
        return super.showFocus();
    }
}
