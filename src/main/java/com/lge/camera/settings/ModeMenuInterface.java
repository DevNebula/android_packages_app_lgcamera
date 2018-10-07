package com.lge.camera.settings;

import android.os.Handler;
import android.view.animation.Animation.AnimationListener;
import com.lge.camera.app.IActivityBase;

public interface ModeMenuInterface extends IActivityBase {
    void changeMultiviewLayout(String str);

    boolean checkModuleValidate(int i);

    int getCameraId();

    Handler getHandler();

    ListPreference getListPreference(String str);

    int getSettingIndex(String str);

    SettingManager getSettingManager();

    String getShotMode();

    void hideModeMenu(boolean z, boolean z2);

    boolean isEnteringDirectFromShortcut();

    boolean isLGUOEMCameraIntent();

    boolean isModuleChanging();

    boolean isNeedHelpItem();

    boolean isOpticZoomSupported(String str);

    boolean isRearCamera();

    boolean isVideoCameraIntent();

    void modeMenuClicked(String str);

    void onHelpButtonClicked(int i);

    void onHideModeMenuEnd(boolean z);

    void onHideMultiviewMenuEnd();

    void onShowModeMenuEnd();

    void setCameraIdBeforeChange(boolean z, int i, boolean z2);

    void setCurrentConeMode(int i, boolean z);

    void setPreviewCallbackAll(boolean z);

    void setPreviewCoverVisibility(int i, boolean z);

    void setPreviewCoverVisibility(int i, boolean z, AnimationListener animationListener, boolean z2, boolean z3);

    void setQuickButtonSelected(int i, boolean z);

    void setSetting(String str, String str2, boolean z);

    void showCameraDialog(int i);

    void showModeDeleteDialog(ModeItem modeItem);

    void updateModeMenuIndicator();
}
