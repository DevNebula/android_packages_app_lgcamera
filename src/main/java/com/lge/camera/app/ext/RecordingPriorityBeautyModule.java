package com.lge.camera.app.ext;

import android.graphics.SurfaceTexture;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.BeautyShotCameraModule;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUpdater;
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
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;

public class RecordingPriorityBeautyModule extends BeautyShotCameraModule implements RecordingPriorityInterface {
    protected RecordingPriorityManager mManager = new RecordingPriorityManager(this);

    public RecordingPriorityBeautyModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void changeRequester() {
        super.changeRequester();
        ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        CameraParameters mParameter = getCameraDevice().getParameters();
        String focusMode = ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO;
        if (!(this.mCameraCapabilities == null || this.mCameraCapabilities.isAFSupported())) {
            focusMode = ParamConstants.FOCUS_MODE_FIXED;
        }
        this.mParamUpdater.setParamValue("focus-mode", focusMode);
        this.mParamUpdater.setParamValue("hdr-mode", "0");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, "off");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL_BUFF_COUNT, "1");
        this.mParamUpdater.setParamValue("recording-hint", "true");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_RAW_FORMAT, "0");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, "off");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
        this.mParamUpdater.setParamValue("picture-size", getLiveSnapShotSize(mParameter, listPref.getValue()));
        String mPreviewSize = getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue());
        this.mManager.setFpsRange();
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, mPreviewSize);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_VIDEO_SNAPSHOT, "off");
    }

    protected void startRecorder() {
        CamLog.m3d(CameraConstants.TAG, "startRecorder");
        this.mIsFileSizeLimitReached = false;
        this.mIsUHDRecTimeLimitWarningDisplayed = false;
        StartRecorderInfo recorderInfo = makeRecorderInfo();
        setStartRecorderInfo(recorderInfo);
        if (FunctionProperties.getSupportedHal() == 2) {
            initRecorder(recorderInfo);
        }
        this.mManager.doStartRecorder();
        this.mCameraIdBeforeInAndOutZoom = this.mCameraId;
    }

    protected void startPreview(CameraParameters params, SurfaceTexture surfaceTexture) {
        CamLog.m7i(CameraConstants.TAG, "startPreview");
        if (FunctionProperties.getSupportedHal() == 2 && getCameraState() != 8) {
            VideoRecorder.createPersistentSurface(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), getCameraId())).split("@")[0], 0, null);
        }
        super.startPreview(params, surfaceTexture, true);
    }

    public double getVideoFPS() {
        return this.mManager.getVideoFPS();
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
        }
    }

    protected void initializeSettingMenus() {
        this.mGet.setAllSettingMenuEnable(true);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIGHTFRAME, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        restoreSettingValue("hdr-mode");
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue(Setting.KEY_LIGHTFRAME);
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    public boolean isSuperZoomEnableCondition() {
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

    public boolean isRecordingPriorityMode() {
        return true;
    }

    protected void stopRecorder() {
        CamLog.m3d(CameraConstants.TAG, "stopRecorder");
        if (this.mManager.doStopRecorder(VideoRecorder.getFileName(), VideoRecorder.getFilePath(), "")) {
            this.mIsFileSizeLimitReached = false;
        }
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

    public void clearEmptyVideoFile() {
    }

    public int getModeColumn() {
        return 0;
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
        return this.mManager.makeRecorderInfo();
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

    protected int getLoopRecordingType() {
        return 0;
    }

    public MediaSaveService getMediaSaveService() {
        return this.mGet.getMediaSaveService();
    }

    public OnMediaSavedListener getOnMediaSavedListener() {
        return this.mOnMediaSavedListener;
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    public boolean checkPreviewCoverVisibilityForRecording() {
        return false;
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    public boolean isSupportedFilterMenu() {
        return false;
    }

    public void playRecordingSound(boolean start) {
        super.playRecordingSound(start);
        if (FunctionProperties.getSupportedHal() == 2 && start) {
            AudioUtil.setSleepForRecordSound();
        }
    }

    protected void showCommandArearUI(boolean show) {
        if (!show || !isRecordingState()) {
            super.access$1300(show);
        }
    }
}
