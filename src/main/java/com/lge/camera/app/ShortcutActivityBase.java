package com.lge.camera.app;

import android.app.ActivityManager.TaskDescription;
import android.content.Intent;
import android.os.Bundle;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;

public class ShortcutActivityBase extends CameraActivity {
    protected boolean mIsCleanViewState = false;

    public boolean isEnteringDirectFromShortcut() {
        return true;
    }

    protected void onNewIntent(Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "ShortcutActivity : onNewIntent, mode = " + getInitCameraMode());
        super.onNewIntent(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        CamLog.m3d(CameraConstants.TAG, "ShortcutActivity : onCreate, mode = " + getInitCameraMode());
        super.onCreate(savedInstanceState);
        setActivityTaskDescription(C0088R.string.app_name, C0088R.mipmap.lg_iconframe_camera);
    }

    public void onStart() {
        CamLog.m3d(CameraConstants.TAG, "ShortcutActivity : onStart, mode = " + getInitCameraMode());
        super.onStart();
    }

    public void onResume() {
        CamLog.m3d(CameraConstants.TAG, "ShortcutActivity : onResume, mode = " + getInitCameraMode());
        super.onResume();
        if (AppControlUtil.isStartFromOnCreate()) {
            doShortcutWorkAfterResume();
        } else {
            setActivityTaskDescription(C0088R.string.app_name, C0088R.mipmap.lg_iconframe_camera);
        }
    }

    public void onPause() {
        CamLog.m3d(CameraConstants.TAG, "ShortcutActivity : onPause, mode = " + getInitCameraMode());
        super.onPause();
    }

    public void onStop() {
        CamLog.m3d(CameraConstants.TAG, "ShortcutActivity : onStop, mode = " + getInitCameraMode());
        super.onStop();
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "ShortcutActivity : onDestroy, mode = " + getInitCameraMode());
        super.onDestroy();
    }

    protected void doShortcutWorkAfterResume() {
        String mode = getShortcutShotMode();
        if (!(mode == null || mode.equals(getCurSettingValue(Setting.KEY_MODE)))) {
            setSetting(Setting.KEY_MODE, mode, false);
        }
        String swapString = getShortcutSwapString();
        if (swapString != null && !swapString.equals(getCurSettingValue(Setting.KEY_SWAP_CAMERA))) {
            setSetting(Setting.KEY_SWAP_CAMERA, swapString, true);
        }
    }

    protected String getShortcutShotMode() {
        return "mode_normal";
    }

    protected String getShortcutDummyCmdButtonLayout() {
        return CameraConstants.START_CAMERA_NORMAL_SHUTTER_ZOOM_VIEW_DUMMY;
    }

    protected String getShortcutSwapString() {
        return "rear";
    }

    protected void setActivityTaskDescription(int labelId, int iconId) {
        setTaskDescription(new TaskDescription(getString(labelId), BitmapManagingUtil.getBitmap(this, iconId)));
    }
}
