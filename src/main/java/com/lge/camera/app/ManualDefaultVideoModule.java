package com.lge.camera.app;

import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ManualAudioControlManager;
import com.lge.camera.managers.ManualDefaultControlManager;
import com.lge.camera.settings.Setting;

public class ManualDefaultVideoModule extends ManualVideoModule {
    public ManualDefaultVideoModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void initUIDone() {
        super.initUIDone();
        if (FunctionProperties.isSupportedLogProfile() || FunctionProperties.isSupportedHDR10()) {
            boolean z = "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)) || "on".equals(getSettingValue(Setting.KEY_HDR10));
            disableWideAngle(z);
        }
    }

    protected void initManualManager() {
        this.mManualViewManager = new ManualAudioControlManager(this);
        this.mManualControlManager = new ManualDefaultControlManager(this);
    }

    public String getShotMode() {
        return CameraConstants.MODE_MANUAL_VIDEO;
    }

    protected int convertCameraId(int cameraId) {
        if (!FunctionProperties.isSupportedOpticZoom()) {
            return cameraId;
        }
        if (!isZoomAvailable()) {
            this.mIsDualCameraMode = false;
            return cameraId;
        } else if (FunctionProperties.isSupportedLogProfile() && "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)) && !hasLogProfileLimitation()) {
            setCameraIdForNormalAngleOnly();
            return 0;
        } else if (FunctionProperties.isSupportedHDR10() && "on".equals(getSettingValue(Setting.KEY_HDR10)) && !hasHDR10Limitation()) {
            setCameraIdForNormalAngleOnly();
            return 0;
        } else {
            this.mIsDualCameraMode = true;
            return super.convertCameraId(cameraId);
        }
    }

    public boolean IsDataChangedByGraphy() {
        return false;
    }

    public boolean isInitialHelpSupportedModule() {
        return false;
    }

    protected boolean doVoiceAssistantTakeCommand() {
        return onRecordStartButtonClicked();
    }

    public boolean isEVShutterSupportedMode() {
        return false;
    }

    public boolean isIndicatorSupported(int indicatorId) {
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
                return true;
            case C0088R.id.indicator_item_steady:
                return isRecordingState();
            default:
                return false;
        }
    }
}
