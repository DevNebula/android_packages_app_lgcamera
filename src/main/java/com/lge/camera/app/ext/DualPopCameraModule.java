package com.lge.camera.app.ext;

import com.lge.camera.app.ActivityBridge;

public class DualPopCameraModule extends DualPopCameraModuleBase {
    public DualPopCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public int getCameraIdFromPref() {
        return 0;
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    public boolean isFaceDetectionSupported() {
        return false;
    }

    protected boolean useAFTrackingModule() {
        return false;
    }

    protected boolean isFingerDetectionSupportedMode() {
        return false;
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
    }

    public void setCaptureButtonEnable(boolean enable, int type) {
        super.setCaptureButtonEnable(enable, 4);
    }

    protected boolean isPauseWaitDuringShot() {
        return false;
    }
}
