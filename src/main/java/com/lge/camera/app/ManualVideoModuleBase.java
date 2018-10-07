package com.lge.camera.app;

import android.view.KeyEvent;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;

public abstract class ManualVideoModuleBase extends ManualBaseModule {
    private boolean mIsCheckHeadsetUnplugged = false;

    public ManualVideoModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected int setQuickButtonPresetWithMode(int preset) {
        return 8;
    }

    protected void setPatialUpdateForManualVideo() {
        int fps = 30;
        try {
            fps = Integer.parseInt(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
        } catch (NumberFormatException e) {
            CamLog.m11w(CameraConstants.TAG, "frameRate parsing fail");
        }
        if (fps >= 60) {
            ViewUtil.setPatialUpdate(getAppContext(), false);
        } else {
            ViewUtil.setPatialUpdate(getAppContext(), true);
        }
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        super.doRunnableStartRecorder(info);
        if (this.mManualViewManager != null) {
            this.mManualViewManager.setAudioLoopbackInRecording(true);
        }
    }

    protected void setAudioLoopbackOnPreview(boolean enable) {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.setAudioLoopbackOnPreview(enable);
        }
    }

    protected void setAudioLoopbackInRecording(boolean enable) {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.setAudioLoopbackInRecording(enable);
        }
    }

    protected void setSSRSetting(boolean enable) {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.setSSRSetting(enable);
        }
    }

    public String getCurrentViewModeToString() {
        return "manual_video";
    }

    protected String getVideoSizeSettingKey() {
        return Setting.KEY_MANUAL_VIDEO_SIZE;
    }

    public boolean isUHDmode() {
        return ParamConstants.VIDEO_3840_BY_2160.equalsIgnoreCase(getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE).split("@")[0]);
    }

    public boolean isFHD60() {
        String videoSizeStr = getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE);
        if (!"1920x1080".equals(videoSizeStr) && !"2160x1080".equals(videoSizeStr)) {
            return false;
        }
        return CameraConstants.FPS_60.equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
    }

    public boolean isFHDCinema60() {
        if (!CameraConstants.VIDEO_CINEMA_FHD.equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE))) {
            return false;
        }
        return CameraConstants.FPS_60.equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
    }

    public boolean isSlowMotionMode() {
        try {
            if (Integer.parseInt(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE)) >= 100) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isZoomAvailable() {
        try {
            if (Integer.parseInt(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE)) >= 100) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public boolean isZoomAvailable(boolean checkRecordingState) {
        return isZoomAvailable();
    }

    public boolean isShutterZoomSupported() {
        return false;
    }

    protected void setFocusStateBeforeStartRecording() {
        if (this.mFocusManager != null) {
            this.mFocusManager.hideAllFocus();
        }
    }

    public boolean isLoopbackAvailable() {
        if (this.mManualViewManager != null) {
            return this.mManualViewManager.isLoopbackAvailable();
        }
        return super.isLoopbackAvailable();
    }

    public boolean onShutterDown(int keyCode, KeyEvent event) {
        return true;
    }

    public boolean onVolumeKeyLongPressed(int keyCode, KeyEvent event) {
        return true;
    }

    protected void sendLDBIntentOnAfterStopRecording() {
        sendLDBIntentAfterContentsCreated(LdbConstants.LDB_FEATURE_NAME_MANUAL_VIDEO, 8, true);
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (this.mManualViewManager != null) {
            return extraStr + this.mManualViewManager.getLDBString();
        }
        return extraStr;
    }

    public boolean isRecordingPriorityMode() {
        return true;
    }

    public String getContentSize() {
        return getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE);
    }

    public int getShutterButtonType() {
        return 4;
    }

    protected void showHeadsetRecordingToastPopup() {
        int iFps = Utils.parseStringToInteger(this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
        if (iFps != 1 && iFps != 2 && this.mManualViewManager != null && this.mManualViewManager.isAudioEarphoneGainBtnSelected()) {
            super.showHeadsetRecordingToastPopup();
        }
    }

    public void onHeadsetPlugged(int headsetState) {
        this.mIsCheckHeadsetUnplugged = false;
        super.onHeadsetPlugged(headsetState);
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onHeadsetStateChanged(true);
        }
    }

    public void onHeadsetUnPlugged() {
        if (!this.mIsCheckHeadsetUnplugged) {
            super.onHeadsetUnPlugged();
            if (this.mManualViewManager != null) {
                this.mManualViewManager.onHeadsetStateChanged(false);
            }
        }
    }

    public void onBTConnectionStateChanged(boolean connect) {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onBTConnectionStateChanged(connect);
        }
    }

    public void onBTStateChanged(boolean isOn) {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onBTStateChanged(isOn);
        }
    }

    public void onBTAudioConnectionStateChanged(boolean connect) {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onBTAudioStateChanged(connect);
        }
    }

    public void onReceiveAudioNoiseIntent() {
        this.mIsCheckHeadsetUnplugged = true;
        doOnHeadsetUnPlugged();
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onHeadsetStateChanged(false);
        }
    }

    protected void setVoiceShutter(boolean enable, int watingTime) {
        super.setVoiceShutter(false, 0);
    }

    public boolean checkPreviewCoverVisibilityForRecording() {
        return false;
    }

    public boolean isManualVideoAudioPopupShowing() {
        if (this.mManualViewManager == null) {
            return false;
        }
        return this.mManualViewManager.isAudioControlPanelShowing();
    }

    protected void oneShotPreviewCallbackDone() {
        super.oneShotPreviewCallbackDone();
        if (this.mCameraDevice != null && checkModuleValidate(192)) {
            this.mCameraDevice.setRecordSurfaceToTarget(false);
        }
    }

    public boolean isHALSignatureCaptureMode() {
        return false;
    }
}
