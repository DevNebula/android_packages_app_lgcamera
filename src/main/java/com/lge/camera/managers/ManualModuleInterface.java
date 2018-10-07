package com.lge.camera.managers;

import com.lge.camera.device.CameraParameters;

public interface ManualModuleInterface extends ModuleInterface {
    boolean IsDataChangedByGraphy();

    String convertISOParamValue(String str);

    String convertShutterSpeedParamValue(String str);

    boolean getAELock();

    String getContentSize();

    int getControllableFeature();

    int getEnabledFeature();

    String getISOParamKey();

    int getLockedFeature();

    ManualData getManualData(int i);

    String getSettingDesc(String str);

    String getShutterSpeedParamKey();

    int getSupportedFeature();

    boolean hasHDR10Limitation();

    boolean hasLogProfileLimitation();

    boolean isDrumMovingAvailable();

    boolean isEnableAutoFuntion(int i);

    boolean isRatioGuideNeeded();

    boolean isSupportedAEUnlock();

    void loadManualData(CameraParameters cameraParameters);

    void onDrumVisibilityChanged(int i, boolean z);

    void onManualButtonClicked(int i);

    boolean setAELock(Boolean bool);

    void setLockedFeature();

    boolean setManualData(int i, String str, String str2, boolean z);
}
