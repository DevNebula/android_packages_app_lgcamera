package com.lge.camera.app;

import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.animation.Animation.AnimationListener;
import com.lge.camera.components.CameraHybridView;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.PreferenceGroup;
import com.lge.camera.settings.Setting;

public interface ActivityBridgeBase extends IActivityBase {
    void backupSetting(String str, String str2);

    void clearAllAssistantFlag();

    void delayCursorUpdate();

    boolean deleteAllImageSavers();

    void doInitSettingOrder();

    boolean getAssistantBoolFlag(String str, boolean z);

    int getAssistantIntFlag(String str, int i);

    String getAssistantStringFlag(String str, String str2);

    String getCurrentViewType();

    CameraHybridView getHybridView();

    ListPreference getListPreference(String str);

    ListPreference getListPreference(String str, boolean z);

    PreferenceGroup getPreferenceGroup();

    int getPreviewCoverVisibility();

    Object getPreviewSurface();

    int getSettingIndex(String str);

    boolean getSettingMenuEnable(String str);

    int getSharedPreferenceCameraId();

    Setting getSpecificSetting(boolean z);

    SurfaceHolder getSurfaceHolder();

    SurfaceTexture getSurfaceTexture();

    SurfaceView getSurfaceView();

    int getTextureState();

    TextureView getTextureView();

    void hideModeMenu(boolean z, boolean z2);

    boolean isAssistantImageIntent();

    boolean isAssistantVideoIntent();

    boolean isModeEditable();

    boolean isModeMenuVisible();

    boolean isOpeningSettingMenu();

    boolean isSettingMenuVisible();

    boolean isVoiceAssistantSpecified();

    boolean onHideMenu(int i);

    void refreshSettingByCameraId();

    void refreshTilePreviewCursor();

    void removeChildSettingView(boolean z);

    void removeSettingMenu(boolean z, boolean z2);

    void restoreBackupSetting(String str, boolean z);

    boolean saveImageSavers(byte[] bArr, int i, boolean z, int i2);

    void setAllSettingChildMenuEnable(String str, boolean z);

    void setAllSettingMenuEnable(boolean z);

    void setAssistantFlag(String str, Object obj);

    boolean setForcedSetting(String str, String str2);

    void setModeEditMode(boolean z);

    void setOrientationLock(boolean z);

    void setPreviewCoverBackground(Drawable drawable);

    void setPreviewCoverVisibility(int i, boolean z);

    void setPreviewCoverVisibility(int i, boolean z, AnimationListener animationListener, boolean z2, boolean z3);

    void setPreviewVisibility(int i);

    void setSetting(String str, String str2, boolean z);

    void setSettingChildMenuEnable(String str, String str2, boolean z);

    void setSettingMenuEnable(String str, boolean z);

    void setSystemUiVisibilityListener(boolean z);

    void setTextureLayoutParams(int i, int i2, int i3);

    void setTextureLayoutParams(int i, int i2, int i3, boolean z);

    void setupOptionMenu(int i);

    void setupSetting();

    boolean showLocationPermissionRequestDialog(boolean z);

    void showModeMenu(boolean z);

    void showSettingMenu(boolean z);

    void startCameraSwitchingAnimation(int i);

    void switchModeList();
}
