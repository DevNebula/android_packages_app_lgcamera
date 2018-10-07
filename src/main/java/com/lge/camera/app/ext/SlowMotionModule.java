package com.lge.camera.app.ext;

import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ViewUtil;
import com.lge.media.CamcorderProfileEx;

public class SlowMotionModule extends RecordingPriorityModule {
    protected double mSlowMotionFps = 120.0d;

    public SlowMotionModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        ViewUtil.setPatialUpdate(getAppContext(), false);
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FRAME_RATE, "30");
                this.mParamUpdater.updateAllParameters(parameters);
                setParameters(parameters);
            }
        }
        ViewUtil.setPatialUpdate(getAppContext(), true);
    }

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    protected void changeRequester() {
        super.changeRequester();
        this.mParamUpdater.addRequester(ParamConstants.KEY_PREVIEW_FRAME_RATE, "240", false, true);
        this.mParamUpdater.addRequester("zoom", "0", false, true);
        setSlowMotionParametersBySettings();
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        if (!FunctionProperties.enableSlowMotionVideoSizeMenu()) {
            setDefaultSettingValueAndDisable(getVideoSizeSettingKey(), false);
            this.mGet.setSetting(getVideoSizeSettingKey(), "1280x720", false);
        }
        setSpecificSettingValueAndDisable("tracking-af", "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        requestFocusOnShutterButton(true);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        restoreSettingValue(getVideoSizeSettingKey());
        restoreSettingValue(Setting.KEY_VOICESHUTTER);
        restoreSettingValue("tracking-af");
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_QR);
    }

    protected void createChildSettingMap() {
        super.createChildSettingMap();
        if (FunctionProperties.enableSlowMotionVideoSizeMenu()) {
            this.mChildSettingMap.put(Setting.KEY_SLOW_MOTION_VIDEO_SIZE, this.mChildSettingUpdater_videoSize);
        }
    }

    protected void setupVideosize(CameraParameters parameters, ListPreference listPref) {
        super.setupVideosize(parameters, listPref);
        if (this.mCameraDevice != null && parameters != null && listPref != null) {
            setSlowMotionParametersBySettings();
            setParameters(parameters);
            this.mGet.setSwitchingAniViewParam(false);
        }
    }

    protected void setPreviewFpsRange(CameraParameters parameters, boolean isRecordingStarted) {
        setSlowMotionParametersBySettings();
    }

    private void setSlowMotionParametersBySettings() {
        if (this.mParamUpdater == null) {
            CamLog.m11w(CameraConstants.TAG, "mParamUpdater is null. return");
            return;
        }
        String[] videoSize = getSettingValue(getVideoSizeSettingKey()).split("@");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
        if (videoSize.length > 1) {
            this.mSlowMotionFps = Double.parseDouble(videoSize[1]);
        } else {
            videoSize[0] = "1280x720";
            if (CamcorderProfileEx.hasProfile(this.mCameraId, 10017)) {
                this.mSlowMotionFps = 240.0d;
            } else {
                this.mSlowMotionFps = 120.0d;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "set videoSize : " + videoSize[0] + ",  Fps : " + this.mSlowMotionFps);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_SIZE, videoSize[0]);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, videoSize[0]);
        if (this.mSlowMotionFps == 240.0d) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FRAME_RATE, "240", false, true);
            this.mParamUpdater.setParamValue(ParamConstants.KEY_HFR, "240");
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FPS_RANGE, ParamConstants.PREVIEW_FPS_RANGE_SLOW_MOTION_240);
            return;
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FRAME_RATE, "120", false, true);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_HFR, "120");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FPS_RANGE, ParamConstants.PREVIEW_FPS_RANGE_SLOW_MOTION);
    }

    public boolean isZoomAvailable() {
        return false;
    }

    public boolean isZoomAvailable(boolean checkRecordingState) {
        return false;
    }

    protected boolean isAudioRecordingAvailable() {
        return false;
    }

    public String getShotMode() {
        return CameraConstants.MODE_SLOW_MOTION;
    }

    public String getRecordingType() {
        return CameraConstants.VIDEO_SLOMO_TYPE;
    }

    protected int getLoopRecordingType() {
        return 0;
    }

    protected boolean useAFTrackingModule() {
        return false;
    }

    public double getVideoFPS() {
        return this.mSlowMotionFps;
    }
}
