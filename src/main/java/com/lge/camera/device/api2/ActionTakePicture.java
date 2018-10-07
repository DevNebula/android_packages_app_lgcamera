package com.lge.camera.device.api2;

import android.hardware.camera2.TotalCaptureResult;

public class ActionTakePicture extends Action {
    public ActionTakePicture(ICameraOps captureFuction, int checkStep) {
        super(captureFuction, checkStep);
    }

    public boolean canGoNextStep(TotalCaptureResult result, int requestId) {
        return this.mIsDone;
    }

    String getActionName() {
        return "ActionTakePicture";
    }

    public int run() {
        try {
            this.mCameraOps.takePicture();
        } catch (ApiFailureException e) {
            e.printStackTrace();
        }
        this.mIsDone = true;
        return 0;
    }
}
