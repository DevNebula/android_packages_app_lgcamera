package com.lge.camera.managers;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.animation.Animation.AnimationListener;
import com.lge.camera.util.SnapShotChecker;

public interface ModuleInterface extends ModuleInterfaceBaseEx {
    void addQuickButtonListDone();

    void bindSlomoSaveService(Intent intent);

    void callMediaSave(Uri uri);

    boolean canUseAEAFLock();

    boolean canUseAeControlBar();

    boolean checkCameraChanging(int i);

    boolean checkCollageContentsShareAvailable();

    boolean checkCurrentConeMode(int i);

    int checkFeatureDisableBatteryLevel(int i, boolean z);

    boolean checkQuickButtonAvailable();

    boolean checkUndoCurrentState(int i);

    void childSettingMenuClicked(String str, String str2);

    void deleteImmediatelyNotUndo();

    void deleteOrUndo(Uri uri, String str, UndoInterface undoInterface);

    boolean getBinningEnabledState();

    String getBinningPictureSize();

    boolean getBlurredBitmapForSwitchingCamera();

    boolean getBurstProgress();

    int getCompleteBurstCount();

    Bitmap getCurPreviewBlurredBitmap(int i, int i2, int i3, boolean z);

    Bitmap getCurPreviewBlurredBitmap(int i, int i2, int i3, boolean z, boolean z2);

    String getCurrentViewModeToString();

    int getFilmStrengthValue();

    boolean getGestureVisibility();

    boolean getGifVisibleStatus();

    int getInitZoomStep(int i);

    int getIntervalshotVisibiity();

    int getMaxEVStep();

    boolean getPhotoSizeHelpShown();

    int getPreviewCoverVisibility();

    View getPreviewFrameLayout();

    int getQuickClipTopPosition();

    Rect getRealFocusWindow(Rect rect, int i, int i2, int i3, int i4);

    long getRecDurationTime(String str);

    boolean getRecordingPreviewState(int i);

    Bitmap getReviewThumbBmp();

    long getSavingQueueCount();

    SnapShotChecker getSnapshotChecker();

    boolean getSpliceviewReverseState();

    Uri getUri();

    int getWaitSavingDialogType();

    void handleSwitchCamera();

    void handleTouchModeChanged(boolean z);

    void hideAllToast();

    void hideAndCancelAllFocusForBinningState(boolean z);

    boolean isActivatedQuickdetailView();

    boolean isActivatedQuickview();

    boolean isActivatedTilePreview();

    boolean isAnimationShowing();

    boolean isAudioZoomAvailable();

    boolean isAvailableTilePreview();

    boolean isBarVisible(int i);

    boolean isCaptureCompleted();

    boolean isCinemaLUTVisible();

    boolean isColorEffectSupported();

    boolean isCropZoomStarting();

    boolean isEVShutterSupportedMode();

    boolean isFlashBarPressed();

    boolean isFlashSupported();

    boolean isGIFEncoding();

    boolean isGestureGuideShowing();

    boolean isGifButtonAvailable();

    boolean isGridPostViesShowing();

    boolean isHelpListVisible();

    boolean isInitialHelpSupportedModule();

    boolean isIntervalShotProgress();

    boolean isJogZoomAvailable();

    boolean isLGUOEMCameraIntent();

    boolean isLightFrameOn();

    boolean isLoopbackAvailable();

    boolean isMMSRecording();

    boolean isManualFocusMode();

    boolean isManualFocusModeEx();

    boolean isManualMode();

    boolean isNightVisionGuideShown();

    boolean isOpticZoomSupported(String str);

    boolean isQuickViewAniStarted();

    boolean isRecordingAnimShowing();

    boolean isRecordingPriorityMode();

    boolean isRecordingSingleZoom();

    boolean isRecordingState();

    boolean isScreenPinningState();

    boolean isSelfieOptionVisible();

    boolean isShutterKeyOptionTimerActivated();

    boolean isShutterlessSelfieProgress();

    boolean isSlowMotionMode();

    boolean isSpliceViewImporteImage();

    boolean isStartedFromQuickCover();

    boolean isSwithingCameraDuringTheRecording();

    boolean isTimerShotCountdown();

    boolean isUspVisible();

    boolean isUspZoneSupportedMode(String str);

    void keepScreenOnAwhile();

    String makeFileName(int i, int i2, String str, boolean z, String str2);

    void onBinningIconVisible(boolean z, int i);

    boolean onCameraShutterButtonClicked();

    void onHideAEBar();

    void onHideBeautyMenu();

    boolean onHideMenu(int i);

    void onPrepareCineZoom();

    void onShowAEBar();

    void onShowBeautyMenu(int i);

    boolean onShowMenu(int i);

    void playRecordingSound(boolean z);

    void playSound(int i, boolean z, int i2, boolean z2);

    void refreshSetting();

    void registerBroadcastReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter);

    void registerEVCallback(boolean z, boolean z2);

    void removeSpliceDimColor();

    void renameTempLoopFile();

    void resetBarDisappearTimer(int i, int i2);

    void setBinningManualOff();

    void setBinningSettings(boolean z);

    void setCollageContentsSharedFlag();

    void setDeleteButtonVisibility(boolean z);

    void setFilmStrength(float f);

    void setFilmStrengthButtonVisibility(boolean z, boolean z2);

    void setFlashBarEnabled(boolean z);

    void setFocusPointVisibility(boolean z);

    void setGIFVisibility(boolean z);

    void setGestureType(int i);

    void setGifEncoding(boolean z);

    void setGifVisibleStatus(boolean z);

    void setManualFocus(boolean z);

    void setManualFocusButtonVisibility(boolean z);

    void setManualFocusModeEx(boolean z);

    void setManualFocusVisibility(boolean z);

    void setNightVisionGuideShown();

    void setPhotoSizeHelpShown(boolean z);

    void setPreviewCoverBackground(Drawable drawable);

    void setPreviewCoverVisibility(int i, boolean z);

    void setPreviewCoverVisibility(int i, boolean z, AnimationListener animationListener, boolean z2, boolean z3);

    void setPreviewForBinning(boolean z);

    void setQuickClipIcon(boolean z, boolean z2);

    void setQuickShareAfterGifMaking();

    void setReviewThumbBmp(Bitmap bitmap);

    void setSelfieOptionVisibility(boolean z, boolean z2);

    void setSystemUiVisibilityListener(boolean z);

    void setTilePreviewLayout(boolean z);

    void setTrackingFocusState(boolean z);

    void setWaitSavingDialogType(int i);

    void showConeViewMode(boolean z);

    void showDoubleCamera(boolean z);

    void showInitDialog();

    void showSetBinningToastManually(boolean z);

    void showTilePreview(boolean z);

    void startCameraSwitchingAnimation(int i);

    void stopMotionEngine();

    void switchCameraOnFront(int i);

    void takePictureByTimer(int i);

    void unregisterBroadcastReceiver(BroadcastReceiver broadcastReceiver);
}
