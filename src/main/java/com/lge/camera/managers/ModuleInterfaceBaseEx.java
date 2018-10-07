package com.lge.camera.managers;

import android.net.Uri;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;

public interface ModuleInterfaceBaseEx extends ModuleInterfaceBase {
    boolean checkDoubleCameraSwitchingAvailable();

    boolean checkFocusStateForChangingSetting();

    int getAEControlLayoutButtomMargin();

    CameraProxy getCameraDevice();

    String getCameraFocusMode();

    SquareSnapGalleryItem getCurSquareSnapItem();

    int getFilmState();

    int getFocusState();

    int getSettingIndex(String str);

    boolean getSettingMenuEnable(String str);

    int getShutterButtonType();

    void initBeautyBar(int i);

    boolean isAEAFJustLocked();

    boolean isAELock();

    boolean isAeControlBarShowing();

    boolean isAeControlBarTouched();

    boolean isAvailableManualFocus(boolean z);

    boolean isFocusLock();

    boolean isIndicatorSupported(int i);

    boolean isJogZoomMoving();

    boolean isManualDrumShowing(int i);

    boolean isManualVideoAudioPopupShowing();

    boolean isMenuShowing(int i);

    boolean isModeMenuVisible();

    boolean isOpeningSettingMenu();

    boolean isQuickClipShowingCondition();

    boolean isSettingMenuVisible();

    boolean isSnapShotProcessing();

    boolean isSquareGalleryBtn();

    boolean isSquareSnapAccessView();

    boolean isVideoCameraIntent();

    boolean isVideoCaptureMode();

    boolean isVolumeKeyPressed();

    void onChangeZoomMinimapVisibility(boolean z);

    void pauseShutterless();

    void refreshTilePreviewCursor();

    void releaseAeControlBar();

    void removeSettingMenu(boolean z, boolean z2);

    void restoreSelfieMenuVisibility();

    void resumeShutterless();

    boolean setBarSetting(String str, String str2, boolean z);

    void setCameraFocusMode(String str);

    void setFocusState(int i);

    void setParamUpdater(CameraParameters cameraParameters, String str, String str2);

    void setParameters(CameraParameters cameraParameters);

    void setQuickButtonIndex(int i, int i2);

    void setQuickClipSharedUri(Uri uri);

    void setTextGuideVisibilityForEachMode(boolean z);

    void setZoomUiVisibility(boolean z);

    void setZoomUiVisibility(boolean z, int i);

    void showExtraPreviewUI(boolean z, boolean z2, boolean z3, boolean z4);

    boolean stopBurstSaving();

    void updateIndicatorPosition(int i);
}
