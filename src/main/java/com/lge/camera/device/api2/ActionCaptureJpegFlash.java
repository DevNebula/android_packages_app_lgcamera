package com.lge.camera.device.api2;

import android.hardware.camera2.TotalCaptureResult;

public class ActionCaptureJpegFlash extends Action {
    public ActionCaptureJpegFlash(ICameraOps captureFuction, int checkStep) {
        super(captureFuction, checkStep);
    }

    public boolean canGoNextStep(TotalCaptureResult result, int requestId) {
        return this.mIsDone;
    }

    String getActionName() {
        return "ActionCaptureJpegFlash";
    }

    public int run() {
        try {
            this.mCameraOps.doJpegCapture(true);
        } catch (ApiFailureException e) {
            e.printStackTrace();
        }
        this.mIsDone = true;
        return 0;
    }
}
