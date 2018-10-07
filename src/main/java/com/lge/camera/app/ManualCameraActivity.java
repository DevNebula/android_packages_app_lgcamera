package com.lge.camera.app;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class ManualCameraActivity extends ShortcutActivityBase {
    protected int checkCameraId(int cameraId) {
        if (!CameraDeviceUtils.isRearCamera(cameraId)) {
            cameraId = SharedPreferenceUtil.getRearCameraId(getAppContext());
            SharedPreferenceUtilBase.setCameraId(getAppContext(), cameraId);
        }
        return super.checkCameraId(cameraId);
    }

    protected String getInitCameraMode() {
        return CameraConstants.MODE_MANUAL_CAMERA;
    }

    protected void selectModuleOnCreate() {
        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_MANUAL_CAMERA);
    }

    protected String getShortcutShotMode() {
        return CameraConstants.MODE_MANUAL_CAMERA;
    }

    protected String getShortcutDummyCmdButtonLayout() {
        return CameraConstants.START_CAMERA_MANUAL_CAMERA_VIEW_DUMMY;
    }
}
