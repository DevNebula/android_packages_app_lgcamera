package com.lge.camera.managers.ext;

import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.device.ParamUpdater;
import com.lge.camera.file.MediaSaveService;
import com.lge.camera.file.MediaSaveService.OnMediaSavedListener;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.managers.RecordingUIManager;
import com.lge.camera.managers.ToastManager;
import com.lge.camera.settings.ListPreference;

public interface RecordingPriorityInterface extends ModuleInterface {
    void clearEmptyVideoFile();

    MediaSaveService getMediaSaveService();

    int getModeColumn();

    OnMediaSavedListener getOnMediaSavedListener();

    ParamUpdater getParamUpdater();

    QuickClipManager getQuickClipManager();

    int getRecCompensationTime();

    long getRecDurationTime(String str);

    RecordingUIManager getRecordingUIManager();

    ToastManager getToastManager();

    int getVideoBitrate();

    double getVideoFPS();

    int getVideoOrientation();

    int[] getVideoSize();

    boolean isCnasRecordingLimitation(int i);

    void keepScreenOnAwhile();

    String makeFileName(int i, int i2, String str, boolean z, String str2);

    void runStartRecorder(boolean z);

    void setCameraState(int i);

    boolean setParamForVideoRecord(boolean z, ListPreference listPreference);

    void setStartRecorderInfo(StartRecorderInfo startRecorderInfo);

    void setTextureLayoutParams(int i, int i2, int i3);

    boolean showPreviewCoverForRecording(boolean z);

    void startHeatingWarning(boolean z);

    void videoRecorderRelease();
}
