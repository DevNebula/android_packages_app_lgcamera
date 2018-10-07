package com.lge.camera.systeminput;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public interface ReceiverInterfaceBase {
    void changeCoverState(boolean z);

    void changeModule();

    void disableCheeseShutterByCallPopup();

    void doInitSettingOrder();

    View findViewById(int i);

    Activity getActivity();

    Context getAppContext();

    String getCurSettingValue(String str);

    String getStorageDir(int i);

    void hideAllToast();

    void hideHelpList(boolean z, boolean z2);

    void hideModeMenu(boolean z, boolean z2);

    boolean isPaused();

    boolean isRecordingState();

    boolean isRotateDialogVisible();

    void onBatteryLevelChanged(int i, int i2, float f);

    void onPowerConnected();

    void onPowerDisconnected();

    void onSDcardInserted();

    void postOnUiThread(Object obj, long j);

    void removePostRunnable(Object obj);

    void removeRotateDialog();

    void removeSettingMenu(boolean z, boolean z2);

    void runOnUiThread(Object obj);

    void setBatteryIndicatorVisibility(boolean z);

    void setBlockTouchByCallPopup(boolean z);

    void setCharging(boolean z);

    void setHeadsetState(int i);

    void setMessageIndicatorReceived(int i, boolean z);

    void setSetting(String str, String str2, boolean z);

    void setSettingMenuEnable(String str, boolean z);

    void setVoiceMailIndicatorReceived(boolean z);

    void showDialogPopup(int i);

    void showDialogPopup(int i, boolean z);

    void showModuleToast(String str, long j);

    void showSettingMenu(boolean z);

    void stopRecordByCallPopup();

    void updateIndicator(int i, int i2, boolean z);
}
