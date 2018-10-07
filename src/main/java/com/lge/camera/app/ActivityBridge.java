package com.lge.camera.app;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import com.lge.camera.file.MediaSaveService;
import com.lge.camera.managers.ConeUIManagerInterface.OnConeViewModeButtonListener;
import com.lge.camera.managers.UndoInterface;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.ModeItem;

public interface ActivityBridge extends ActivityBridgeBase {
    void bindSlomoSaveService(Intent intent);

    int calStartMargin(int i, int i2, int i3, boolean z);

    void changeModule();

    void changeModule(boolean z);

    void changeModuleByCoverstate();

    void changeToAttachModule();

    boolean checkCameraChanging(int i);

    void childSettingMenuClicked(String str, String str2);

    void closeDetailView();

    void deleteMode(ModeItem modeItem);

    void deleteOrUndo(Uri uri, String str, UndoInterface undoInterface);

    void enableConeMenuIcon(int i, boolean z);

    int getAnimationType();

    String getBeforeMode();

    Bitmap getCurPreviewBitmap(int i, int i2);

    Bitmap getCurPreviewBlurredBitmap(int i, int i2, int i3, boolean z);

    Bitmap getCurPreviewBlurredBitmap(int i, int i2, int i3, boolean z, boolean z2);

    int getCurrentConeMode();

    String getCurrentUSPZone();

    Uri getDeletedUri();

    MediaSaveService getMediaSaveService();

    boolean getPhotoSizeHelpShown();

    int getQueueCount();

    Bitmap getReviewThumbBmp();

    Uri getSavedUri();

    String getSettingDesc(String str);

    void hideHelpList(boolean z, boolean z2);

    boolean isActivatedQuickdetailView();

    boolean isActivatedTilePreview();

    boolean isAnimationShowing();

    boolean isCallAnswering();

    boolean isCallingPackage(String str);

    boolean isCameraChanging();

    boolean isCameraChangingOnFlashJumpCut();

    boolean isCameraChangingOnSnap();

    boolean isCameraChangingOnSquareSnap();

    boolean isEnteringDirectFromShortcut();

    boolean isFromFloatingBar();

    boolean isHelpListVisible();

    boolean isLGUOEMCameraIntent();

    boolean isMMSIntent();

    boolean isModuleChanging();

    boolean isNightVisionGuideShown();

    boolean isOpticZoomSupported(String str);

    boolean isOrientationLocked();

    boolean isResumeAfterProcessingDone();

    boolean isReturnFromHelp();

    boolean isScreenPinningState();

    boolean isSnapShotProcessing();

    boolean isStartedFromQuickCover();

    boolean isStickerGuideShown();

    boolean isStillImageCameraIntent();

    boolean isVideoCameraIntent();

    boolean isVideoCaptureMode();

    void launchGallery(Uri uri, int i);

    void loadSound();

    void modeMenuClicked(String str);

    void movePreviewOutOfWindow(boolean z);

    void onCameraSwitchingEnd();

    void onCameraSwitchingStart();

    void onNewItemAdded(Uri uri, int i, String str);

    void playSound(int i, boolean z, int i2);

    void playSound(int i, boolean z, int i2, boolean z2);

    void refreshSetting();

    void refreshUspZone(boolean z);

    void registerBroadcastReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter);

    void removeCameraChanging(int i);

    void removePostAllRunnables();

    void removeRotateDialog();

    void requestNotifyNewMediaonActivity(Uri uri, boolean z);

    void sendCancelNotiToSlomoSaveService();

    void setAnimationShowing(boolean z);

    void setBeforeMode(String str);

    void setCameraChanging(int i);

    void setCameraChangingOnFlashJumpCut(boolean z);

    void setCameraChangingOnSnap(boolean z);

    void setCameraChangingOnSquareSnap(boolean z);

    boolean setCameraPreviewSize(int i, int i2);

    void setConeClickable(boolean z);

    void setConeModeChanged();

    void setCurrentConeMode(int i, boolean z);

    void setDeleteButtonVisibility(boolean z);

    void setDeletedUriFromGallery(Uri uri);

    void setFrontFlashOff();

    void setGestureType(int i);

    void setIsStickerGuideShown();

    void setLocationOnByCamera(boolean z);

    void setModuleChanging(boolean z);

    void setNaviBarVisibility(boolean z, long j);

    void setNightVisionGuideShown();

    void setOnConeViewModeButtonListener(OnConeViewModeButtonListener onConeViewModeButtonListener);

    void setPhotoSizeHelpShown(boolean z);

    void setPreviewCoverParam(boolean z);

    void setReviewThumbBmp(Bitmap bitmap);

    void setThumbnailListEnable(boolean z);

    void showConeViewMode(boolean z);

    void showHelpList(boolean z);

    void showTilePreview(boolean z);

    void showTilePreviewCoverView(boolean z);

    void startPreviewDone(boolean z);

    void startSelfieEngine();

    void stopQuickShotModeByBackkey();

    void stopSelfieEngin();

    void stopSound(int i);

    void thumbnailListInit();

    void turnOnTilePreview(boolean z);

    void unbindSlomoSaveService();

    void unregisterBroadcastReceiver(BroadcastReceiver broadcastReceiver);

    void updateButtonBySetting(String str);

    void updateLocationSwitchButton();

    boolean updatePictureSizeListPreference(ListPreference listPreference, String[] strArr, String str, int i, String str2);

    void updateUspLayout(String str, boolean z);

    void waitConfigPreviewBuffer();
}
