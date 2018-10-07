package com.lge.camera.app;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class SelfieCameraActivity extends ShortcutActivityBase {
    protected int checkCameraId(int cameraId) {
        if (!CameraDeviceUtils.isRearCamera(cameraId)) {
            return cameraId;
        }
        cameraId = SharedPreferenceUtil.getFrontCameraId(getAppContext());
        SharedPreferenceUtilBase.setCameraId(getAppContext(), cameraId);
        return cameraId;
    }

    protected void selectModuleOnCreate() {
        if (FunctionProperties.isSupportedBeautyShot()) {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_BEAUTY);
        } else if (FunctionProperties.isSupportedGestureShot()) {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_GESTURESHOT);
        } else {
            this.mCurrentModule = getCreatedModule("mode_normal");
        }
    }

    protected String getInitCameraMode() {
        return CameraConstants.MODE_BEAUTY;
    }

    protected String getShortcutDummyCmdButtonLayout() {
        return CameraConstants.START_CAMERA_NORMAL_VIEW_DUMMY;
    }

    protected String getShortcutSwapString() {
        return "front";
    }
}
