package com.lge.camera.app.ext;

import android.os.Bundle;
import com.lge.camera.app.ShortcutActivityBase;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class PopoutActivity extends ShortcutActivityBase {
    protected void onCreate(Bundle savedInstanceState) {
        CamLog.m3d(CameraConstants.TAG, "PopoutActivity : onCreate");
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        CamLog.m3d(CameraConstants.TAG, "PopoutActivity : onResume");
        super.onResume();
    }

    public void onPause() {
        CamLog.m3d(CameraConstants.TAG, "PopoutActivity : onPause");
        super.onPause();
    }

    public void onStop() {
        CamLog.m3d(CameraConstants.TAG, "PopoutActivity : onStop");
        super.onStop();
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "PopoutActivity : onDestory");
        super.onDestroy();
    }

    protected String getInitCameraMode() {
        return CameraConstants.MODE_POPOUT_CAMERA;
    }

    protected int checkCameraId(int cameraId) {
        if (CameraDeviceUtils.isRearCamera(cameraId)) {
            return cameraId;
        }
        SharedPreferenceUtilBase.setCameraId(getApplicationContext(), 0);
        return 0;
    }

    protected void selectModuleOnCreate() {
        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_POPOUT_CAMERA);
    }
}
