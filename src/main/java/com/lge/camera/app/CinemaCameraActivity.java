package com.lge.camera.app;

import android.os.Bundle;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class CinemaCameraActivity extends ShortcutActivityBase {
    boolean mCannotEnterCineVideoMode = false;

    protected void onCreate(Bundle savedInstanceState) {
        if (AppControlUtil.isPowerSaveMaximumModeOn(getAppContext())) {
            ConfigurationUtil.setConfiguration(getApplicationContext());
            this.mCannotEnterCineVideoMode = FunctionProperties.isSupportedHDR10();
        }
        super.onCreate(savedInstanceState);
        if (this.mCannotEnterCineVideoMode) {
            CamLog.m3d(CameraConstants.TAG, "maximum battery saver on! change to Auto mode");
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CinemaCameraActivity.this.showToast(CinemaCameraActivity.this.getAppContext().getString(C0088R.string.cannot_use_cine_video_on_max_power_saving_toast), CameraConstants.TOAST_LENGTH_SHORT);
                }
            });
        }
    }

    protected int checkCameraId(int cameraId) {
        if (this.mCannotEnterCineVideoMode) {
            return super.checkCameraId(cameraId);
        }
        SharedPreferenceUtilBase.setCameraId(getAppContext(), 0);
        SharedPreferenceUtil.saveRearCameraId(getAppContext(), 0);
        return 0;
    }

    protected String getInitCameraMode() {
        if (this.mCannotEnterCineVideoMode) {
            return super.getInitCameraMode();
        }
        return CameraConstants.MODE_CINEMA;
    }

    protected void selectModuleOnCreate() {
        if (this.mCannotEnterCineVideoMode) {
            super.selectModuleOnCreate();
        } else {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_CINEMA);
        }
    }

    protected String getShortcutShotMode() {
        if (this.mCannotEnterCineVideoMode) {
            return super.getShortcutShotMode();
        }
        return CameraConstants.MODE_CINEMA;
    }

    protected String getShortcutDummyCmdButtonLayout() {
        if (this.mCannotEnterCineVideoMode) {
            return super.getShortcutDummyCmdButtonLayout();
        }
        return CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
    }
}
