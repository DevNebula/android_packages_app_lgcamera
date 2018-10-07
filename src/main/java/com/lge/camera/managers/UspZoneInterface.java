package com.lge.camera.managers;

import com.lge.camera.app.IActivityBase;

public interface UspZoneInterface extends IActivityBase {
    boolean checkUSPZoneAvailable();

    String getAssistantStringFlag(String str, String str2);

    String getShotMode();

    boolean isModuleChanging();

    boolean isRearCamera();

    boolean isScreenPinningState();

    void onModeItemClick(String str);

    boolean onOutfocusModeClicked(boolean z);

    boolean onSmartCamModeClicked(boolean z);

    void setSetting(String str, String str2, boolean z);

    void setSettingChildMenuEnable(String str, String str2, boolean z);

    void stopPreview();
}
