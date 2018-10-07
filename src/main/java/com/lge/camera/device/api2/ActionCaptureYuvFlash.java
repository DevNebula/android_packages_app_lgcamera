package com.lge.camera.device.api2;

import android.hardware.camera2.TotalCaptureResult;

public class ActionCaptureYuvFlash extends Action {
    public ActionCaptureYuvFlash(ICameraOps captureFuction, int checkStep) {
        super(captureFuction, checkStep);
    }

    public boolean canGoNextStep(TotalCaptureResult result, int requestId) {
        return this.mIsDone;
    }

    String getActionName() {
        return "ActionCaptureYuvFlash";
    }

    public int run() {
        this.mCameraOps.captureYuvFlash();
        this.mIsDone = true;
        return 0;
    }
}
