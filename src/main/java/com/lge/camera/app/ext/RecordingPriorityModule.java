package com.lge.camera.app.ext;

import android.graphics.SurfaceTexture;
import android.view.KeyEvent;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUpdater;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.file.MediaSaveService;
import com.lge.camera.file.MediaSaveService.OnMediaSavedListener;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.managers.RecordingUIManager;
import com.lge.camera.managers.ToastManager;
import com.lge.camera.managers.ext.RecordingPriorityInterface;
import com.lge.camera.managers.ext.RecordingPriorityManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;

public class RecordingPriorityModule extends DefaultCameraModule implements RecordingPriorityInterface {
    protected boolean mRecordingFlashOn = false;
    protected RecordingPriorityManager mRecordingPriorityManager = null;

    public RecordingPriorityModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void startPreview(CameraParameters params, SurfaceTexture surfaceTexture) {
        CamLog.m7i(CameraConstants.TAG, "startPreview");
        if (FunctionProperties.getSupportedHal() == 2 && getCameraState() != 8) {
            VideoRecorder.createPersistentSurface(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), getCameraId())).split("@")[0], 0, getMimeType());
        }
        super.startPreview(params, surfaceTexture, true);
        if (params == null) {
            params = this.mCameraDevice.getParameters();
        }
    }

    protected String getMimeType() {
        return null;
    }

    public void onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "shutter large clicked.");
        if (!this.mSnapShotChecker.checkMultiShotState(2) && checkModuleValidate(15)) {
            if ((this.mPostviewManager == null || !this.mPostviewManager.isPostviewShowing()) && !stopBurstShotTaking(false) && !SystemBarUtil.isSystemUIVisible(getActivity()) && !this.mCaptureButtonManager.isWorkingShutterAnimation()) {
                if (this.mReviewThumbnailManager == null || !this.mReviewThumbnailManager.isQuickViewAniStarted()) {
                    switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                        case 14:
                            onRecordStartButtonClicked();
                            return;
                        default:
                            super.onShutterLargeButtonClicked();
                            return;
                    }
                }
            }
        }
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (keyCode == 66) {
                LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_ENTER);
            } else if (keyCode == 25) {
                LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_VOLUME);
            }
            if (isRecordingState()) {
                onShutterBottomButtonClickListener();
            } else {
                onShutterLargeButtonClicked();
            }
        }
        return true;
    }

    protected void setVoiceShutter(boolean enable, int watingTime) {
        super.setVoiceShutter(false, 0);
    }

    protected void changeRequester() {
        super.changeRequester();
        this.mParamUpdater.setParamValue("hdr-mode", "0");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, "off");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL_BUFF_COUNT, "1");
        this.mParamUpdater.setParamValue("recording-hint", "true");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_RAW_FORMAT, "0");
        if (!isAvailableSteadyCam()) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, "off");
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_VIDEO_SNAPSHOT, "off");
        if (checkModuleValidate(128)) {
            String focusMode = ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO;
            if (!(this.mCameraCapabilities == null || this.mCameraCapabilities.isAFSupported())) {
                focusMode = ParamConstants.FOCUS_MODE_FIXED;
            }
            this.mParamUpdater.setParamValue("focus-mode", focusMode);
            ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            String mLiveSnapShotSize = getLiveSnapShotSize(getCameraDevice().getParameters(), listPref.getValue());
            this.mParamUpdater.setParamValue("picture-size", mLiveSnapShotSize);
            if (isOpticZoomSupported(null) && !FunctionProperties.isSameResolutionOpticZoom()) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PICTURE_SIZE_WIDE, mLiveSnapShotSize);
                CamLog.m3d(CameraConstants.TAG, "[opticzoom] set KEY_PICTURE_SIZE_WIDE livesnapshot : " + mLiveSnapShotSize);
            }
            String mPreviewSize = getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue());
            if (this.mRecordingPriorityManager == null) {
                this.mRecordingPriorityManager = new RecordingPriorityManager(this);
            }
            this.mRecordingPriorityManager.setFpsRange();
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, mPreviewSize);
            String flashMode = this.mRecordingFlashOn ? "on" : "off";
            if (checkFeatureDisableBatteryLevel(1, false) != 0) {
                flashMode = "off";
            }
            setFlashTorch(this.mCameraDevice.getParameters(), this.mRecordingFlashOn, flashMode, false);
            this.mGet.setSetting("flash-mode", flashMode, false);
            this.mParamUpdater.setParamValue(ParamConstants.KEY_BINNING_PARAM, "normal", false);
        }
    }

    protected void startRecorder() {
        CamLog.m3d(CameraConstants.TAG, "startRecorder");
        AudioUtil.setUseBuiltInMicForRecording(getAppContext(), false);
        this.mIsFileSizeLimitReached = false;
        this.mIsUHDRecTimeLimitWarningDisplayed = false;
        StartRecorderInfo recorderInfo = makeRecorderInfo();
        setStartRecorderInfo(recorderInfo);
        if (FunctionProperties.getSupportedHal() == 2) {
            initRecorder(recorderInfo);
            if (CameraConstants.MODE_CINEMA.equals(getShotMode()) && this.mCameraDevice != null) {
                this.mCameraDevice.setRecordStreamForSpecialMode(true);
            }
        }
        if (this.mRecordingPriorityManager != null) {
            this.mRecordingPriorityManager.doStartRecorder();
        }
        this.mCameraIdBeforeInAndOutZoom = this.mCameraId;
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            showFrameGridView(getGridSettingValue(), false);
        }
        if (this.mFocusManager != null && this.mFocusManager.isAEControlBarEnableCondition()) {
            this.mFocusManager.registerEVCallback(false, false);
        }
    }

    protected void setCAFButtonVisibility() {
        super.setCAFButtonVisibility();
        CamLog.m3d(CameraConstants.TAG, "mParamUpdater.getParamValue(KEY_FOCUS_MODE) " + getCameraFocusMode());
        if ("auto".equals(getCameraFocusMode()) && !"on".equals(getSettingValue("tracking-af"))) {
            doTouchAFInRecording();
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(14);
        }
    }

    public double getVideoFPS() {
        if (this.mRecordingPriorityManager == null) {
            return 30.0d;
        }
        return this.mRecordingPriorityManager.getVideoFPS();
    }

    public int getVideoBitrate() {
        return 0;
    }

    public boolean showPreviewCoverForRecording(boolean isStart) {
        return false;
    }

    protected void stopPreview() {
        if (this.mGet.isPaused() || checkModuleValidate(64)) {
            super.stopPreview();
        } else {
            CamLog.m3d(CameraConstants.TAG, "skip stop preview");
        }
    }

    protected void initializeSettingMenus() {
        this.mGet.setAllSettingMenuEnable(true);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSettingMenuEnable("picture-size", false);
        setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        if ("1".equals(getSettingValue("hdr-mode"))) {
            String key = !isRearCamera() ? Setting.KEY_LIGHTFRAME : "flash-mode";
            if ("flash-mode".equals(key)) {
                setSpecificSettingValueAndDisable(key, "off", false);
            }
        }
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        restoreSettingValue("hdr-mode");
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        setSettingMenuEnable("picture-size", true);
        setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, true);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        if (!"1".equals(getSettingValue("hdr-mode"))) {
            restoreFlashSetting();
        } else if (getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_FULLVISION);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
        this.mRecordingFlashOn = false;
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mRecordingFlashOn && this.mCameraDevice != null) {
            setFlashOnRecord(null, false, true);
        }
    }

    protected void setParameterByLGSF(CameraParameters parameters, boolean isRecording) {
        setParameterByLGSF(parameters, "mode_normal", isRecording);
    }

    public boolean isSuperZoomEnableCondition() {
        return false;
    }

    public boolean isShutterZoomSupported() {
        return false;
    }

    protected void cameraStartUpEnd() {
    }

    protected void setContinuousFocus(CameraParameters parameters, boolean recordStart) {
        String focusMode;
        if (this.mCameraCapabilities == null || !this.mCameraCapabilities.isContinousFocusSupported()) {
            focusMode = ParamConstants.FOCUS_MODE_FIXED;
        } else {
            focusMode = ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO;
        }
        CamLog.m3d(CameraConstants.TAG, "### setFocusMode-" + focusMode);
        this.mParamUpdater.setParameters(parameters, "focus-mode", focusMode);
    }

    protected void setVideoSizeAndRestartPreview(CameraParameters parameters, String previewSize, String screenSize, String videoSize) {
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
        if (this.mGet.isLGUOEMCameraIntent()) {
            setCaptureButtonEnable(false, 1);
            setCaptureButtonEnable(true, 2);
        }
    }

    public boolean isRecordingPriorityMode() {
        return true;
    }

    protected void updateShutterButtonsOnVideoStopClicked() {
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(14);
        }
    }

    protected void stopRecorder() {
        CamLog.m3d(CameraConstants.TAG, "stopRecorder");
        String fileName = VideoRecorder.getFileName();
        String filePath = VideoRecorder.getFilePath();
        String uuid = "";
        if (this.mLoopRecorderInfo != null && VideoRecorder.getLoopState() == 3) {
            fileName = this.mLoopRecorderInfo.mFileName;
            filePath = this.mLoopRecorderInfo.mOutFilePath;
        }
        if (VideoRecorder.getLoopState() != 0) {
            uuid = VideoRecorder.getUUID();
        }
        resetMicPath();
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setForStopRecording();
        }
        if (CameraConstants.MODE_CINEMA.equals(getShotMode()) && this.mCameraDevice != null) {
            this.mCameraDevice.setRecordStreamForSpecialMode(false);
        }
        if (this.mRecordingPriorityManager != null && this.mRecordingPriorityManager.doStopRecorder(fileName, filePath, uuid)) {
            this.mIsFileSizeLimitReached = false;
        }
    }

    public void clearEmptyVideoFile() {
        if (this.mTempLoopRecorderInfo != null) {
            VideoRecorder.clearEmptyVideoFile(this.mTempLoopRecorderInfo.mOutFilePath, true);
        }
    }

    public int getModeColumn() {
        return 0;
    }

    protected void setPreviewLayoutParam() {
        ListPreference listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        if (listPref != null) {
            int[] size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
            this.mGet.setTextureLayoutParams(size[0], size[1], -1);
            if (this.mPreviewFrameLayout != null) {
                this.mPreviewFrameLayout.setAspectRatio(((double) size[0]) / ((double) size[1]));
            }
            setAnimationLayout(1);
        }
    }

    protected void updateFlashParam(CameraParameters parameters, int flashMsg, boolean save) {
        CamLog.m3d(CameraConstants.TAG, "updateFlashParam : flashMsg = " + flashMsg);
        if (this.mCameraDevice != null && parameters != null) {
            this.mCameraDevice.refreshParameters();
            this.mLocalParamForZoom = null;
            if (parameters != null) {
                boolean z;
                String flashMode = ParamUtils.getFlashMode(flashMsg);
                if (flashMsg == 51) {
                    z = true;
                } else {
                    z = false;
                }
                this.mRecordingFlashOn = z;
                setFlashTorch(parameters, this.mRecordingFlashOn, flashMode, true);
                this.mGet.setSetting("flash-mode", flashMode, false);
            }
        }
    }

    public void setFlashTorch(CameraParameters parameters, boolean on, String flashMode, boolean setParam) {
        if (parameters != null && CameraDeviceUtils.isFlashSupported(parameters, flashMode)) {
            if (on && "on".equals(flashMode)) {
                setParamUpdater(parameters, "flash-mode", ParamConstants.FLASH_MODE_TORCH);
            } else {
                setParamUpdater(parameters, "flash-mode", "off");
            }
            if (setParam) {
                setParameters(parameters);
            }
        }
    }

    protected void oneShotPreviewCallbackDone() {
        super.oneShotPreviewCallbackDone();
        setFlashOnRecord(null, true, true);
    }

    public void childSettingMenuClicked(String key, String value, int clickedType) {
        if (checkModuleValidate(207)) {
            super.childSettingMenuClicked(key, value, clickedType);
            if (SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId).equals(key) || (FunctionProperties.getSupportedHal() == 2 && Setting.KEY_VIDEO_STEADY.equals(key) && !CameraConstants.MODE_CINEMA.equals(getShotMode()))) {
                setupPreview(null);
                return;
            }
            return;
        }
        CamLog.m11w(CameraConstants.TAG, "childSettingMenuClicked, app finishing or recording. return.");
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    public RecordingUIManager getRecordingUIManager() {
        return this.mRecordingUIManager;
    }

    public ToastManager getToastManager() {
        return this.mToastManager;
    }

    public void setStartRecorderInfo(StartRecorderInfo info) {
        this.mStartRecorderInfo = info;
    }

    protected StartRecorderInfo makeRecorderInfo() {
        return this.mRecordingPriorityManager.makeRecorderInfo();
    }

    public int[] getVideoSize() {
        return sVideoSize;
    }

    public void setTextureLayoutParams(int width, int height, int leftMargin) {
        this.mGet.setTextureLayoutParams(width, height, leftMargin);
    }

    public ParamUpdater getParamUpdater() {
        return this.mParamUpdater;
    }

    public QuickClipManager getQuickClipManager() {
        return this.mQuickClipManager;
    }

    public MediaSaveService getMediaSaveService() {
        return this.mGet.getMediaSaveService();
    }

    public OnMediaSavedListener getOnMediaSavedListener() {
        return this.mOnMediaSavedListener;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mRecordingPriorityManager != null) {
            this.mRecordingPriorityManager.mGet = null;
            this.mRecordingPriorityManager = null;
        }
    }

    protected boolean checkToUseFilmEffect() {
        return false;
    }

    public int getShutterButtonType() {
        return 4;
    }

    public void setCaptureButtonEnable(boolean enable, int type) {
        super.setCaptureButtonEnable(enable, 4);
    }

    protected void doPhoneStateListenerAction(int state) {
        super.doPhoneStateListenerAction(state);
        if (state == 0) {
            if (!checkModuleValidate(192)) {
                return;
            }
            if (MDMUtil.allowMicrophone()) {
                setCaptureButtonEnable(true, 4);
                if (this.mCaptureButtonManager != null) {
                    this.mCaptureButtonManager.setShutterButtonEnable(true, 1);
                    return;
                }
                return;
            }
            setCaptureButtonEnable(false, 4);
        } else if (state == 1 && getCameraState() != 6 && getCameraState() != 7) {
            setCaptureButtonEnable(false, 4);
        }
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        if (this.mFocusManager != null) {
            if (this.mFocusManager.isTrackingState()) {
                this.mFocusManager.resetEVValue(0);
            } else {
                this.mFocusManager.registerEVCallback(true, true);
            }
        }
        showDoubleCamera(true);
    }

    protected void setupVideosize(CameraParameters parameters, ListPreference listPref) {
        restoreTrackingAFSetting();
    }

    protected void showCommandArearUI(boolean show) {
        if (!show || !isRecordingState()) {
            super.showCommandArearUI(show);
        }
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    public boolean checkPreviewCoverVisibilityForRecording() {
        return false;
    }

    public boolean isFocusEnableCondition() {
        return super.isFocusEnableCondition() && isAFSupported();
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    public boolean isHALSignatureCaptureMode() {
        return false;
    }

    public boolean isInitialHelpSupportedModule() {
        return false;
    }

    public boolean isEVShutterSupportedMode() {
        return false;
    }

    public boolean isSupportedFilterMenu() {
        return false;
    }

    public boolean isFaceDetectionSupported() {
        return false;
    }

    public boolean isIndicatorSupported(int indicatorId) {
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
                return true;
            case C0088R.id.indicator_item_steady:
                if (isRecordingState() && isAvailableSteadyCam()) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public void playRecordingSound(boolean start) {
        super.playRecordingSound(start);
        if (FunctionProperties.getSupportedHal() == 2 && start) {
            AudioUtil.setSleepForRecordSound();
        }
    }

    protected boolean doVoiceAssistantTakeCommand() {
        return onRecordStartButtonClicked();
    }
}
