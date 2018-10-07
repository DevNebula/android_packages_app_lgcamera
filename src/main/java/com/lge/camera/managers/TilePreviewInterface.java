package com.lge.camera.managers;

import android.net.Uri;
import com.lge.camera.app.IActivityBase;
import com.lge.camera.file.MediaSaveService;
import com.lge.camera.settings.ListPreference;
import java.util.ArrayList;

public interface TilePreviewInterface extends IActivityBase {
    boolean checkFocusStateForChangingSetting();

    boolean checkInterval(int i);

    boolean checkUndoCurrentState(int i);

    void closeDetailViewAfterStartPreview();

    void deleteImmediatelyNotUndo();

    void deleteOrUndo(Uri uri, String str, UndoInterface undoInterface);

    void doBackKey();

    boolean getBurstProgress();

    String getCurDir();

    ArrayList<String> getDirPath(boolean z);

    ListPreference getListPreference(String str);

    MediaSaveService getMediaSaveService();

    int getPreviewCoverVisibility();

    String getShotMode();

    boolean isActivatedQuickview();

    boolean isJogZoomMoving();

    boolean isModuleChanging();

    boolean isSettingMenuVisible();

    boolean isSnapShotProcessing();

    boolean isTimerShotCountdown();

    boolean isZoomControllerTouched();

    void launchGallery(Uri uri, int i);

    void quickClipDrawerClose(boolean z);

    void removeFileFromTilePreview();

    void removeRotateDialog();

    void removeStopPreviewMessage();

    void setLaunchGalleryLocation(float[] fArr);

    void showDialogPopup(int i);

    void showThumbnailListDetailView(boolean z, boolean z2, float f);

    void updateQuickClipForTilePreview(boolean z, boolean z2, Uri uri);

    void updateThumbnail(boolean z);
}
