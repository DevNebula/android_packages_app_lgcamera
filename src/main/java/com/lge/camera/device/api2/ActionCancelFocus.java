package com.lge.camera.device.api2;

import android.hardware.camera2.TotalCaptureResult;

public class ActionCancelFocus extends Action {
    public ActionCancelFocus(ICameraOps captureFuction, int checkStep) {
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
        return "ActionCancelFocus";
    }

    public int run() {
        this.mCurrStepRequestId = this.mCameraOps.doCancelAutoFocus();
        return this.mCurrStepRequestId;
    }
}
