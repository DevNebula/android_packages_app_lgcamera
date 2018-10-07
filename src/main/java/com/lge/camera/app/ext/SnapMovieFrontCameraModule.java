package com.lge.camera.app.ext;

import android.net.Uri;
import android.os.Message;
import android.view.KeyEvent;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.dialog.CamDialogInterfaceSnap;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.managers.ext.SnapMovieInterfaceImpl;
import com.lge.camera.managers.ext.SnapMovieManagerTrigger;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.SettingKeyWrapper;

public class SnapMovieFrontCameraModule extends SnapMovieFrontCameraModuleBase implements CamDialogInterfaceSnap {
    public SnapMovieFrontCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void initBeautyManager() {
        if (!isRearCamera() && !this.mBeautyManager.isInit()) {
            this.mBeautyManager.init(this.mCameraDevice, this.mCameraId, ModelProperties.getBeautyDefaultLevel(), ModelProperties.getRelightingDefaultLevel(), 0, 100);
        }
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

    public boolean onShutterLargeButtonLongClicked() {
        if (this.mSnapManager != null && this.mSnapManager.isHaveEnoughTime(1)) {
            return !this.mSnapManager.onShutterLargeButtonLongClicked() ? super.onShutterLargeButtonLongClicked() : true;
        } else {
            return false;
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

    public String makeFileName(int useType, int storage, String dir, boolean useThread, String shotMode) {
        if (this.mSnapManager == null) {
            return null;
        }
        return this.mSnapManager.getFileName(useType, storage, dir);
    }

    protected void restoreSettingMenus() {
        restoreSettingValue(Setting.KEY_VOICESHUTTER);
        restoreSettingValue(Setting.KEY_SAVE_DIRECTION);
        restoreSettingValue(Setting.KEY_MOTION_QUICKVIEWER);
        restoreSettingValue(Setting.KEY_RAW_PICTURE);
        restoreSettingValue("hdr-mode");
        restoreSettingValue(Setting.KEY_FRAME_GRID);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_LIGHTFRAME);
        restoreSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        restoreSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_TAG_LOCATION);
        if ("1".equals(getSettingValue("hdr-mode")) && getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_FULLVISION);
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SAVE_DIRECTION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_MOTION_QUICKVIEWER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_RAW_PICTURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FRAME_GRID, "off", false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSpecificSettingValueAndDisable(Setting.KEY_TAG_LOCATION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIGHTFRAME, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
        if (this.mSnapManager != null) {
            this.mSnapManager.setInputSize();
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

    protected boolean doSetParamForStartRecording(CameraParameters parameters, boolean recordStart, ListPreference listPref, String videoSize) {
        if (this.mFlipManager != null) {
            parameters = this.mFlipManager.setForceVideoFlipParam(this.mCameraId, parameters, getVideoOrientation(), true, true);
        }
        return super.doSetParamForStartRecording(parameters, recordStart, listPref, videoSize);
    }

    public int getVideoOrientation() {
        if (this.mSnapManager != null) {
            return this.mSnapManager.getVideoOrientation();
        }
        return super.getVideoOrientation();
    }

    protected void changeRequester() {
        super.changeRequester();
        if (this.mSnapManager == null) {
            this.mSnapManager = new SnapMovieManagerTrigger(this);
        }
        restoreFlashSetting();
        this.mParamUpdater.setParamValue("flash-mode", getSettingValue("flash-mode"));
        this.mSnapManager.setInputSizeParam(this.mParamUpdater);
    }

    public int getCurStorage() {
        if (this.mSnapManager == null || this.mSnapManager.getStatus() == 2) {
            return 0;
        }
        return super.getCurStorage();
    }

    protected void setVoiceShutter(boolean enable, int watingTime) {
        super.setVoiceShutter(false, 0);
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        if (this.mSnapManager == null || !this.mSnapManager.doMainHandlerHandleMessage(msg)) {
            return super.mainHandlerHandleMessage(msg);
        }
        return true;
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType) || this.mSnapManager == null) {
            return false;
        }
        this.mSnapManager.onShowSpecificMenu();
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (!(isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL) || this.mSnapManager == null)) {
            this.mSnapManager.onHideSpecificMenu();
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

    protected void doShowGestureGuide() {
        super.doShowGestureGuide();
        if (this.mSnapManager != null) {
            this.mSnapManager.setVisibleBar(false);
        }
    }

    protected void doHideGestureGuide() {
        super.doHideGestureGuide();
        if (this.mSnapManager != null) {
            this.mSnapManager.setVisibleBar(true);
        }
    }

    public void onQueueStatus(int count) {
        if (this.mSnapManager == null || !this.mSnapManager.isProcessingSave()) {
            super.onQueueStatus(count);
        }
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
            AudioUtil.setUseBuiltInMicForRecording(getAppContext(), false);
            super.startRecorder();
        }
    }

    public boolean isShutterKeyOptionTimerActivated() {
        if (this.mSnapManager != null && this.mSnapManager.isUseTimer()) {
            return super.isShutterKeyOptionTimerActivated();
        }
        return false;
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mSnapManager != null) {
            this.mSnapManager.onResumeAfter();
            CamLog.m3d(CameraConstants.TAG, "-c1- onResumeAfter");
        }
    }

    public void onPauseAfter() {
        setQuickButtonEnable(100, false, true);
        if (this.mCameraDevice != null) {
            CameraParameters params = this.mCameraDevice.getParameters();
            this.mFlipManager.setForceVideoFlipParam(this.mCameraId, params, 0, false, false);
            this.mCameraDevice.setParameters(params);
        }
        if (this.mSnapManager != null) {
            this.mSnapManager.onPauseAfter();
        }
        super.onPauseAfter();
    }

    protected void onChangeModuleBefore() {
        super.onChangeModuleBefore();
        if (!this.mGet.isCameraChangingOnSnap() && !CameraConstants.MODE_SNAP.equals(getSettingValue(Setting.KEY_MODE))) {
            ListPreference listPref = this.mGet.getListPreference(Setting.KEY_MODE);
            String defaultValue = listPref == null ? "mode_normal" : listPref.getDefaultValue();
            String modeRear = this.mGet.getSpecificSetting(true).getSettingValue(Setting.KEY_MODE);
            String modeFront = this.mGet.getSpecificSetting(false).getSettingValue(Setting.KEY_MODE);
            if (CameraConstants.MODE_SNAP.equals(modeRear)) {
                this.mGet.getSpecificSetting(true).setSetting(Setting.KEY_MODE, defaultValue, true);
            }
            if (CameraConstants.MODE_SNAP.equals(modeFront)) {
                this.mGet.getSpecificSetting(false).setSetting(Setting.KEY_MODE, defaultValue, true);
            }
        }
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (this.mSnapManager != null && !this.mGet.isModuleChanging()) {
            this.mSnapManager.doCleanView(doByAction, useAnimation, saveState);
        }
    }

    protected boolean onReviewThumbnailClicked(int waitType) {
        if (this.mSnapManager != null) {
            this.mSnapManager.setStatus(0);
        }
        return super.onReviewThumbnailClicked(waitType);
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        if (this.mSnapManager != null) {
            this.mSnapManager.rotateView(degree, isFirst);
        }
        super.onOrientationChanged(degree, isFirst);
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

    public void onShutterLargeButtonClicked() {
        if (this.mSnapManager != null && !this.mSnapManager.onShutterLargeButtonClicked()) {
            super.onShutterLargeButtonClicked();
        }
    }

    protected void onCameraSwitchingStart() {
        this.mGet.setCameraChangingOnSnap(true);
        super.onCameraSwitchingStart();
    }

    public boolean isFocusEnableCondition() {
        return isRearCamera();
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

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        if (this.mSnapManager != null) {
            this.mSnapManager.onShowSpecificMenu();
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot() || this.mSnapManager == null) {
            return false;
        }
        this.mSnapManager.doCleanView(false, true, true);
        return true;
    }
}
