package com.lge.camera.device.api2;

import android.hardware.camera2.TotalCaptureResult;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public abstract class Action {
    public static final int DO_BY_CAMERA_OPS = 1;
    public static final int DO_BY_CAPTURE_RESULT = 0;
    protected ICameraOps mCameraOps = null;
    protected int mCheckStepBy = -1;
    protected int mCurrStepRequestId = -1;
    protected boolean mIsDone = false;

    abstract String getActionName();

    abstract int run();

    public Action(ICameraOps cameraOps, int checkstep) {
        this.mCameraOps = cameraOps;
        this.mCheckStepBy = checkstep;
    }

    public int getCurrStepRequestId() {
        return this.mCurrStepRequestId;
    }

    public boolean isDone() {
        return this.mIsDone;
    }

    public boolean canGoNextStep(TotalCaptureResult result, int requestId) {
        return true;
    }

    public boolean skipStep(int requestId) {
        boolean z = this.mCurrStepRequestId == -1 || this.mCurrStepRequestId == requestId;
        this.mIsDone = z;
        if (this.mIsDone) {
            CamLog.m7i(CameraConstants.TAG, getActionName() + " is skiped");
        }
        return this.mIsDone;
    }

    public boolean isRunnableByCaptureResult() {
        return this.mCheckStepBy == 0;
    }

    public void setChcekStep(int value) {
        this.mCheckStepBy = value;
    }
}
