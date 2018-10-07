package com.lge.camera.dialog;

import android.net.Uri;
import com.lge.camera.app.IModuleBase;
import com.lge.camera.settings.ModeItem;

public interface CamDialogInterface extends IModuleBase {
    void childSettingMenuClicked(String str, String str2, int i);

    void closeNetworkCamera();

    void deleteMode(ModeItem modeItem);

    void doCancelClickInDeleteConfirmDialog();

    void doCancleClickDuringSlomoSaving();

    void doDeleteOnUndoDialog();

    void doOkClickInDeleteConfirmDialog(int i);

    void doSelectInOverlapSampleDialog(int i);

    Uri getUri();

    boolean isCollageProgressing();

    boolean isOrientationLocked();

    boolean isVideoAttachMode();

    void onDialogDismiss();

    void onDialogShowing();

    void requeryGraphyItems();

    void selectMyFilterItem();

    void setSettingMenuEnable(String str, boolean z);

    void showDialog(int i);

    boolean showLocationPermissionRequestDialog(boolean z);
}
