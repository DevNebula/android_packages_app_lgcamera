package com.lge.camera.device.api2;

import android.hardware.camera2.TotalCaptureResult;

public class ActionAutoFocus extends Action {
    public ActionAutoFocus(ICameraOps captureFuction, int checkStep) {
        super(captureFuction, checkStep);
    }

    public boolean canGoNextStep(TotalCaptureResult result, int requestId) {
        if (this.mCurrStepRequestId != requestId) {
            return false;
        }
        this.mIsDone = true;
        return true;
    }

    String getActionName() {
        return "ActionAutoFocus";
    }

    public int run() {
        this.mCurrStepRequestId = this.mCameraOps.doAutoFocus(false);
        return this.mCurrStepRequestId;
    }
}
