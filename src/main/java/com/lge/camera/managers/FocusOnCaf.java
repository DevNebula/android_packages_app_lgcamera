package com.lge.camera.managers;

import android.hardware.Camera.AutoFocusCallback;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CameraAFCallback;
import com.lge.camera.util.CamLog;

public class FocusOnCaf extends FocusBase {
    private CameraContinuousFocusCallback mFocusCallback;
    private int mPreviousFocusedState;

    class CameraContinuousFocusCallback implements CameraAFCallback {
        CameraContinuousFocusCallback() {
        }

        public void onAutoFocus(boolean focused, CameraProxy camera) {
            FocusOnCaf.this.callbackOnCaf(focused, camera);
        }
    }

    public FocusOnCaf(ModuleInterface iModule) {
        super(iModule);
        this.mFocusCallback = null;
        this.mPreviousFocusedState = 0;
        this.mFocusCallback = new CameraContinuousFocusCallback();
    }

    public void registerCallback(AutoFocusCallback focusAuto) {
        initFocusAreas();
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            cameraDevice.autoFocus(this.mGet.getHandler(), this.mFocusCallback);
        }
    }

    public void unregisterCallback() {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            cameraDevice.autoFocus(this.mGet.getHandler(), null);
        }
    }

    private void callbackOnCaf(boolean success, CameraProxy camera) {
        if (ModelProperties.isMTKChipset() && ((success && this.mPreviousFocusedState == 6) || (!success && this.mPreviousFocusedState == 5))) {
            this.mPreviousFocusedState = 0;
            CamLog.m9v(CameraConstants.TAG, "### onContinuousFocus(): skipped");
        } else if (this.mGet.getFocusState() == 1 || this.mGet.getFocusState() == 2 || this.mGet.getFocusState() == 13 || this.mGet.getFocusState() == 12) {
            CamLog.m3d(CameraConstants.TAG, "focusing state. return");
        } else {
            if (success) {
                setFocusState(6);
                if (ModelProperties.isMTKChipset()) {
                    this.mPreviousFocusedState = 6;
                }
            } else {
                setFocusState(5);
                if (ModelProperties.isMTKChipset()) {
                    this.mPreviousFocusedState = 5;
                }
            }
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                cameraDevice.autoFocus(this.mGet.getHandler(), this.mFocusCallback);
                updateFocusStateIndicator();
            }
        }
    }

    public void release() {
        this.mFocusCallback = null;
        super.release();
    }
}
