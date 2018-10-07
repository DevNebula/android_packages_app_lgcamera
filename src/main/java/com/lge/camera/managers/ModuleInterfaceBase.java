package com.lge.camera.managers;

import android.net.Uri;
import com.lge.camera.app.IModuleBase;
import java.util.ArrayList;

public interface ModuleInterfaceBase extends IModuleBase {
    boolean attatchMediaOnPostview(Uri uri, int i);

    boolean checkModuleValidate(int i);

    boolean easyCheckStorage(int i, int i2, boolean z);

    ArrayList<String> getAllDir(boolean z);

    String getCurrentSelectedPictureSize();

    String getCurrentSelectedPreviewSize();

    String getCurrentSelectedVideoSize();

    int getDialogID();

    int getOutfocusErrorTextHeight();

    String getParamValue(String str);

    int getSnapFixedDegree();

    void hideHelpList(boolean z, boolean z2);

    void hideModeMenu(boolean z, boolean z2);

    boolean isCenterKeyPressed();

    boolean isFHD60();

    boolean isFocusEnableCondition();

    boolean isGestureZooming();

    boolean isModuleChanging();

    boolean isMultishotState(int i);

    boolean isOutfocusAvailable();

    boolean isProgressDialogVisible();

    boolean isRotateDialogVisible();

    boolean isShutterZoomSupported();

    boolean isStickerIconDisableCondition();

    boolean isStickerSelected();

    boolean isStillBurstShotSaving();

    boolean isStorageRemoved(int i);

    boolean isSuperZoomEnableCondition();

    boolean isUHDmode();

    boolean isZoomAvailable();

    boolean isZoomBarVisible();

    boolean isZoomControllerTouched();

    void notifyNewMediaFromPostview(Uri uri);

    void notifyNewMediaFromVideoTrim(Uri uri);

    void removeRotateDialog();

    void setBarVisible(int i, boolean z, boolean z2);

    void setEditDim(boolean z);

    void setExtraPreviewButtonSelected(int i, boolean z);

    void setOrientationLock(boolean z);

    void setQuickButtonByPreset(boolean z, boolean z2);

    void setupOptionMenu(int i);

    void showDialog(int i);

    void showDialog(int i, String str, boolean z);

    void showDialog(int i, boolean z);

    boolean showLocationPermissionRequestDialog(boolean z);

    void showProcessingDialog(boolean z, int i);

    void showProgressBarDialog(boolean z, int i);

    void showSavingDialog(boolean z, int i);

    void stopPreviewThread();
}
