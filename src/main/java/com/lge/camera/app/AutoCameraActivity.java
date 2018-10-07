package com.lge.camera.app;

import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class AutoCameraActivity extends ShortcutActivityBase {
    protected int checkCameraId(int cameraId) {
        if (!CameraDeviceUtils.isRearCamera(cameraId)) {
            cameraId = SharedPreferenceUtil.getRearCameraId(getAppContext());
            SharedPreferenceUtilBase.setCameraId(getAppContext(), cameraId);
        }
        return super.checkCameraId(cameraId);
    }

    protected void selectModuleOnCreate() {
        this.mCurrentModule = getCreatedModule("mode_normal");
    }

    protected String getInitCameraMode() {
        return "mode_normal";
    }
}
