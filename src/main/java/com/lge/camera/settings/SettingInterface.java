package com.lge.camera.settings;

import android.os.Handler;
import android.view.MotionEvent;
import com.lge.camera.app.IActivityBase;
import com.lge.camera.util.TimeIntervalChecker;

public interface SettingInterface extends IActivityBase {
    boolean canUseTrackingAF();

    boolean checkModuleValidate(int i);

    void childSettingMenuClicked(String str, String str2);

    void childSettingMenuClicked(String str, String str2, int i);

    void childSettingMenuClicked(String str, String str2, int i, int i2);

    void doCleanView(boolean z, boolean z2, boolean z3);

    void enableConeMenuIcon(int i, boolean z);

    int getCurrentCameraId();

    int getCurrentConeMode();

    int getFilmState();

    Handler getHandler();

    Handler getModuleHandler();

    int getSharedPreferenceCameraId();

    String getShotMode();

    TimeIntervalChecker getTimeIntervalChecker();

    boolean isActivatedTilePreview();

    boolean isMMSIntent();

    boolean isMenuShowing(int i);

    boolean isNeedHelpItem();

    boolean isRecordingPriorityMode();

    boolean isStorageRemoved(int i);

    boolean isVideoCaptureMode();

    void modeMenuClicked(String str);

    void onFrameRateListRefreshed(String str, String str2);

    void onHelpButtonClicked(int i);

    void onRemoveChildSettingEnd();

    void onRemoveSettingEnd();

    void onShowChildSettingEnd();

    void onShowSettingEnd();

    boolean onTouchEvent(MotionEvent motionEvent);

    void refreshSetting();

    void removeSettingMenu(boolean z, boolean z2);

    void setQuickButtonSelected(int i, boolean z);

    void updateModeMenuIndicator();
}
