package com.lge.camera.managers;

import com.lge.camera.app.IActivityBase;
import com.lge.camera.device.CameraManager.CameraProxy;

public interface HelpInterface extends IActivityBase {
    void enableConeMenuIcon(int i, boolean z);

    CameraProxy getCameraDevice();

    String getSettingValue(String str);

    String getShotMode();

    void hideHelpList(boolean z, boolean z2);

    boolean isAssistantImageIntent();

    boolean isAssistantVideoIntent();

    boolean isManualMode();

    boolean isMenuShowing(int i);

    boolean isStillImageCameraIntent();

    boolean isVideoCameraIntent();

    void onCallLocalHelp();

    void setQuickButtonSelected(int i, boolean z);

    void setSetting(String str, String str2, boolean z);

    void setupSetting();

    void showHelpList(boolean z);
}
