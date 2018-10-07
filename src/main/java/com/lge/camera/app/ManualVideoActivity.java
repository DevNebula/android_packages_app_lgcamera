package com.lge.camera.app;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class ManualVideoActivity extends ShortcutActivityBase {
    protected int checkCameraId(int cameraId) {
        if (!CameraDeviceUtils.isRearCamera(cameraId)) {
            SharedPreferenceUtilBase.setCameraId(getApplicationContext(), SharedPreferenceUtil.getRearCameraId(getApplicationContext()));
        }
        return -1;
    }

    protected String getInitCameraMode() {
        return CameraConstants.MODE_MANUAL_VIDEO;
    }

    protected void selectModuleOnCreate() {
        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_MANUAL_VIDEO);
    }

    protected String getShortcutShotMode() {
        return CameraConstants.MODE_MANUAL_VIDEO;
    }

    protected String getShortcutDummyCmdButtonLayout() {
        if (ManualUtil.isCinemaSize(this, getCurSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE))) {
            return CameraConstants.START_CAMERA_MANUAL_VIDEO_CINEMA_VIEW_DUMMY;
        }
        return CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
    }
}
