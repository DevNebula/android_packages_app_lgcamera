package com.lge.camera.device.api2;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

public class ActionLockExposure extends Action {
    public ActionLockExposure(ICameraOps captureFuction, int checkStep) {
        super(captureFuction, checkStep);
    }

    public boolean canGoNextStep(TotalCaptureResult result, int requestId) {
        if (((Integer) result.get(CaptureResult.CONTROL_AE_STATE)).intValue() == 3) {
            if (!this.mCameraOps.isAFSupported() || ((Integer) result.get(CaptureResult.CONTROL_AF_STATE)).intValue() == 0) {
                this.mIsDone = true;
                return true;
            } else if (((Integer) result.get(CaptureResult.CONTROL_AF_STATE)).intValue() == 4 || ((Integer) result.get(CaptureResult.CONTROL_AF_STATE)).intValue() == 5) {
                this.mIsDone = true;
                return true;
            }
        }
        return false;
    }

    String getActionName() {
        return "ActionLockExposure";
    }

    public int run() {
        this.mCurrStepRequestId = this.mCameraOps.lockExposure(true);
        return this.mCurrStepRequestId;
    }
}
