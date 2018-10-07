package com.lge.camera.app.ext;

import android.net.Uri;
import android.os.Message;
import android.view.KeyEvent;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.dialog.CamDialogInterfaceSnap;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.managers.TimerManager.TimerType;
import com.lge.camera.managers.ext.SnapMovieInterfaceImpl;
import com.lge.camera.managers.ext.SnapMovieManagerTrigger;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.SettingKeyWrapper;

public class SnapMovieSingleCameraModule extends SnapMovieSingleCameraModuleBase implements CamDialogInterfaceSnap {
    public SnapMovieSingleCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void doYesOnInitDialog() {
        if (this.mSnapManager != null) {
            this.mSnapManager.deleteAllShot(true);
        }
    }

    public void doYesOnEditDialog() {
        if (this.mSnapManager != null) {
            this.mSnapManager.launchVideoStudio(SnapMovieInterfaceImpl.EXTRA_NAME_VIDEO_EDITOR_TYPE_STYLE);
        }
    }

    public void onShutterTopButtonClickListener() {
        if (this.mSnapManager != null && !this.mSnapManager.onShutterTopButtonClickListener(this.mPostviewManager, this.mReviewThumbnailManager)) {
            super.onShutterTopButtonClickListener();
        }
    }

    public void onShutterTopButtonLongClickListener() {
        if (this.mSnapManager != null && !this.mSnapManager.onShutterTopButtonLongClickListener(this.mPostviewManager, this.mReviewThumbnailManager)) {
            super.onShutterTopButtonLongClickListener();
        }
    }

    public void onShutterBottomButtonClickListener() {
        if (this.mSnapManager != null && !this.mSnapManager.onShutterBottomButtonClickListener()) {
            super.onShutterBottomButtonClickListener();
        }
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (this.mSnapManager != null) {
            this.mSnapManager.onZoomBarVisibilityChanged(true);
        }
    }

    public void onZoomHide() {
        super.onZoomHide();
        if (this.mSnapManager != null) {
            this.mSnapManager.onZoomBarVisibilityChanged(false);
        }
    }

    public int getVideoOrientation() {
        if (this.mSnapManager != null) {
            return this.mSnapManager.getVideoOrientation();
        }
        return super.getVideoOrientation();
    }

    protected void setVideoLimitSize() {
        int i;
        this.mLimitRecordingSize = 0;
        if (this.mSnapManager == null) {
            i = SnapMovieManagerTrigger.SHOT_TIME_VIDEO_MIN;
        } else {
            i = this.mSnapManager.getRemainShotTime();
        }
        this.mLimitRecordingDuration = i;
    }

    protected void startRecorder() {
        if (this.mSnapManager != null) {
            this.mSnapManager.startRecorder();
            setVideoLimitSize();
            super.startRecorder();
        }
    }

    public void onVideoShutterClicked() {
        if (this.mSnapManager != null && this.mSnapManager.isHaveEnoughTime(1)) {
            this.mSnapManager.setStatus(2);
            super.onVideoShutterClicked();
            if (getCameraState() == 5) {
                this.mSnapManager.onVideoShutterClicked();
            } else {
                this.mSnapManager.setStatus(1);
            }
        }
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mSnapManager == null || !this.mSnapManager.isHaveEnoughTime(0)) {
            return false;
        }
        boolean result = super.onCameraShutterButtonClicked();
        this.mSnapManager.onCameraShutterButtonClicked(this.mTimerManager);
        return result;
    }

    public String getCurDir() {
        if (this.mSnapManager == null) {
            return "";
        }
        return this.mSnapManager.getCurDir(this.mStorageManager, this.mNeedProgressDuringCapture);
    }

    public int getCurStorage() {
        if (this.mSnapManager == null || this.mSnapManager.getStatus() == 2) {
            return 0;
        }
        return super.getCurStorage();
    }

    public String makeFileName(int useType, int storage, String dir, boolean useThread, String shotMode) {
        if (this.mSnapManager == null) {
            return null;
        }
        return this.mSnapManager.getFileName(useType, storage, dir);
    }

    protected void restoreSettingMenus() {
        restoreSettingValue(Setting.KEY_VOICESHUTTER);
        restoreSettingValue(Setting.KEY_RAW_PICTURE);
        restoreSettingValue("hdr-mode");
        restoreSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        restoreSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreTrackingAFSetting();
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_TAG_LOCATION);
        if (!"1".equals(getSettingValue("hdr-mode"))) {
            restoreFlashSetting();
        } else if (getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        this.mRecordingFlashOn = false;
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_RAW_PICTURE, "off", false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TAG_LOCATION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        if (this.mSnapManager != null) {
            this.mSnapManager.setInputSize();
            restoreTrackingAFSetting();
        }
    }

    public void onNormalAngleButtonClicked() {
        if (this.mSnapManager != null && this.mSnapManager.getStatus() != 3) {
            super.onNormalAngleButtonClicked();
        }
    }

    public void onWideAngleButtonClicked() {
        if (this.mSnapManager != null && this.mSnapManager.getStatus() != 3) {
            super.onWideAngleButtonClicked();
        }
    }

    protected void setVoiceShutter(boolean enable, int watingTime) {
        super.setVoiceShutter(false, 0);
    }

    public void onResumeBefore() {
        AppControlUtil.setSnapMovieMVFrameType(0);
        super.onResumeBefore();
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        if (this.mSnapManager == null || !this.mSnapManager.doMainHandlerHandleMessage(msg)) {
            return super.mainHandlerHandleMessage(msg);
        }
        return true;
    }

    protected void updateThumbnail(ExifInterface exif, int exifDegree, boolean isBurst) {
        if (this.mSnapManager != null && this.mSnapManager.isUpdateThumbCondition()) {
            super.updateThumbnail(exif, exifDegree, isBurst);
        }
    }

    protected void checkSavedURI(Uri uri) {
        if (this.mSnapManager != null && this.mSnapManager.isUpdateThumbCondition()) {
            super.checkSavedURI(uri);
        }
    }

    public boolean doBackKey() {
        int i = 1;
        if (this.mSnapManager == null || this.mTimerManager == null) {
            return false;
        }
        if (this.mSnapManager.isProcessingSave()) {
            CamLog.m3d(CameraConstants.TAG, "block back because do saving");
            return true;
        }
        if (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress()) {
            this.mSnapManager.setCountShutterClicked(0);
            this.mSnapManager.showGuideTextByList();
        }
        boolean result = super.doBackKey();
        SnapMovieManagerTrigger snapMovieManagerTrigger = this.mSnapManager;
        if (!result) {
            i = 0;
        }
        snapMovieManagerTrigger.setStatus(i);
        return result;
    }

    protected void changeToAutoView() {
        if (this.mGet != null) {
            this.mGet.setCurrentConeMode(1, true);
            setSetting(Setting.KEY_MODE, "mode_normal", true);
            this.mGet.modeMenuClicked("mode_normal");
        }
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_VOLUME);
            onShutterLargeButtonClicked();
        }
        return true;
    }

    public void onQueueStatus(int count) {
        if (this.mSnapManager != null && !this.mSnapManager.isProcessingSave()) {
            super.onQueueStatus(count);
        }
    }

    protected void initializeControls(boolean enable) {
        super.initializeControls(enable);
        setQuickClipListListener();
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (this.mSnapManager != null && !this.mGet.isModuleChanging()) {
            this.mSnapManager.doCleanView(doByAction, useAnimation, saveState);
        }
    }

    protected boolean doStartTimerShot(TimerType timerType) {
        if (this.mSnapManager == null) {
            return false;
        }
        return super.doStartTimerShot(timerType);
    }

    protected void onCameraSwitchingStart() {
        this.mGet.setCameraChangingOnSnap(true);
        super.onCameraSwitchingStart();
    }

    public void onGestureCleanViewDetected() {
        if (this.mSnapManager == null || this.mSnapManager.getStatus() == 1) {
            super.onGestureCleanViewDetected();
        }
    }

    protected void afterSendSwitchCameraMsgByGesture() {
        if (this.mSnapManager != null) {
            this.mSnapManager.removeSaveButtonTouchListener();
        }
        super.afterSendSwitchCameraMsgByGesture();
    }
}
