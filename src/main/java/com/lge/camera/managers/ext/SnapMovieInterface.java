package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import com.lge.camera.managers.CaptureButtonManager;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.managers.RecordingUIManager;
import com.lge.camera.managers.ReviewThumbnailManager;

public interface SnapMovieInterface extends ModuleInterface {
    void enableConeMenuIcon(int i, boolean z);

    CaptureButtonManager getCaptureButtonManager();

    String getCurTempDir();

    Bitmap getPreviewBitmap();

    QuickClipManager getQuickClipManager();

    long getRecDurationTime();

    RecordingUIManager getRecordingUIManager();

    ReviewThumbnailManager getReviewThumbnailManager();

    int getSaveResult();

    void hideZoomBar();

    boolean isLiveSnapshotSupported();

    boolean isNeedFlip();

    boolean isSupportedQuickClip();

    boolean onCameraShutterButtonClicked();

    boolean onRecordStartButtonClicked();

    void onShutterStopButtonClicked();

    void onVideoStopClicked(boolean z, boolean z2);

    void sendLDBIntentOnAfterStopRecording();

    void setCaptureButtonEnable(boolean z, int i);

    void setQuickButtonEnable(int i, boolean z, boolean z2);

    void setSavePath(String str);

    void setSettingMenuEnable(String str, boolean z);

    boolean setShutterButtonListener(boolean z);

    void setSpecificSettingValueAndDisable(String str, String str2, boolean z);

    void updateSaveResult(int i);
}
