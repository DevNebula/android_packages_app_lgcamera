package com.lge.camera.app;

import android.content.Intent;
import android.os.Bundle;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class GraphyCameraActivity extends CameraActivity {
    protected void onCreate(Bundle savedInstanceState) {
        CamLog.m3d(CameraConstants.TAG, "GraphyCameraActivity : onCreate");
        super.onCreate(savedInstanceState);
    }

    protected void checkIntent(Intent intent) {
        super.checkIntent(intent);
        CamLog.m3d(CameraConstants.TAG, "Camera is entring as manual camera mode");
        this.mIsEnteringDirectGraphyMode = true;
    }

    public void onResume() {
        CamLog.m3d(CameraConstants.TAG, "GraphyCameraActivity : onResume");
        this.mIsEnteringDirectGraphyMode = true;
        super.onResume();
    }

    public void onPause() {
        CamLog.m3d(CameraConstants.TAG, "GraphyCameraActivity : onPause");
        super.onPause();
        this.mIsEnteringDirectGraphyMode = false;
    }

    public void onStop() {
        CamLog.m3d(CameraConstants.TAG, "GraphyCameraActivity : onStop");
        super.onStop();
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "GraphyCameraActivity : onDestory");
        super.onDestroy();
    }

    protected void selectModuleOnCreate() {
        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_MANUAL_CAMERA);
    }

    protected String getInitCameraMode() {
        return CameraConstants.MODE_MANUAL_CAMERA;
    }
}
