package com.lge.camera.managers;

import com.lge.camera.app.IModuleBase;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;

public interface ManualModeInterface extends IModuleBase {
    boolean checkModuleValidate(int i);

    CameraProxy getCameraDevice();

    boolean isModeMenuVisible();

    boolean isModuleChanging();

    boolean isSettingMenuVisible();

    boolean isShowViewMode();

    boolean isSlowMotionMode();

    void setManualFocus(boolean z);

    void setParamUpdater(CameraParameters cameraParameters, String str, String str2);

    void showHistogram(boolean z);
}
