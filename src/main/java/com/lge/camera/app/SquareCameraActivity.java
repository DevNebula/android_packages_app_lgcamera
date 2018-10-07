package com.lge.camera.app;

import android.os.Bundle;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class SquareCameraActivity extends ShortcutActivityBase {
    protected void onCreate(Bundle savedInstanceState) {
        CamLog.m3d(CameraConstants.TAG, "SquareCameraActivity : onCreate");
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        CamLog.m3d(CameraConstants.TAG, "SquareCameraActivity : onResume");
        super.onResume();
    }

    public void onPause() {
        CamLog.m3d(CameraConstants.TAG, "SquareCameraActivity : onPause");
        super.onPause();
    }

    public void onStop() {
        CamLog.m3d(CameraConstants.TAG, "SquareCameraActivity : onStop");
        super.onStop();
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "SquareCameraActivity : onDestory");
        super.onDestroy();
    }

    protected String getInitCameraMode() {
        return CameraConstants.MODE_SQUARE_SNAPSHOT;
    }

    public boolean isEnteringDirectFromShortcut() {
        return true;
    }

    protected void selectModuleOnCreate() {
        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SQUARE_SNAPSHOT);
    }

    protected String getShortcutShotMode() {
        return CameraConstants.MODE_SQUARE_SNAPSHOT;
    }

    protected String getShortcutSwapString() {
        return isRearCamera() ? "rear" : "front";
    }
}
