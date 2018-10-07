package com.lge.camera.managers;

import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUpdater;
import com.lge.camera.util.CamLog;

public class BeautyShotManager {
    public static final BeautyShotManager NULL = new BeautyShotManager(null, false);
    protected int mBeautyLevel = 0;
    protected boolean mBeautyOn;
    protected int mBeautyOneLevelValue = 0;
    protected boolean mBeautyPreviewOn;
    private boolean mBeautyRecordingSupported = true;
    protected CameraProxy mCameraDevice = null;
    protected int mCameraId = 0;
    protected ActivityBridge mGet;
    protected boolean mIsInit = false;
    protected int mMaxLavel = 0;
    protected int mMinLevel = 0;
    protected int mRelightingLevel = 0;
    protected int mRelightingOneLevelValue = 0;

    public enum PostProcessType {
        STILL_IMAGE,
        PREVIEW_IMAGE
    }

    public BeautyShotManager(ActivityBridge activityBridge, boolean beautyOn) {
        this.mGet = activityBridge;
        this.mBeautyOn = beautyOn;
        this.mBeautyPreviewOn = beautyOn;
    }

    public void init(CameraProxy cameraDevice, int cameraId, int beautyLevel, int relightingLevel, int minLevel, int maxLavel) {
        int i = 10;
        this.mCameraDevice = cameraDevice;
        this.mCameraId = cameraId;
        this.mBeautyLevel = beautyLevel;
        this.mRelightingLevel = relightingLevel;
        this.mBeautyOneLevelValue = FunctionProperties.getBeautyStep();
        this.mBeautyOneLevelValue = this.mBeautyOneLevelValue == 0 ? 10 : this.mBeautyOneLevelValue;
        this.mRelightingOneLevelValue = FunctionProperties.getRelightingStep();
        if (this.mRelightingOneLevelValue != 0) {
            i = this.mRelightingOneLevelValue;
        }
        this.mRelightingOneLevelValue = i;
        this.mIsInit = true;
        this.mBeautyRecordingSupported = true;
        if (cameraDevice != null) {
            this.mBeautyRecordingSupported = CameraDeviceUtils.isBeautyRecordingSupported(cameraDevice.getParameters());
            CamLog.m7i(CameraConstants.TAG, "mBeautyRecordingSupported ?  " + this.mBeautyRecordingSupported);
        }
    }

    public void setCamera(CameraProxy cameraDevice) {
        this.mCameraDevice = cameraDevice;
    }

    public boolean isInit() {
        return this.mIsInit;
    }

    public void release() {
        this.mCameraDevice = null;
        this.mIsInit = false;
        this.mBeautyRecordingSupported = false;
    }

    public void setBeautyPreviewOn(boolean isOn) {
        if (this.mBeautyOn) {
            this.mBeautyPreviewOn = isOn;
        }
    }

    public boolean getBeautyPreviewOn() {
        if (this.mBeautyOn) {
            return this.mBeautyPreviewOn;
        }
        return false;
    }

    public void setBeautyLevel(int level) {
        this.mBeautyLevel = level;
        if (this.mCameraDevice != null && !CameraDeviceUtils.isRearCamera(this.mCameraId) && isInit()) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                int strength = 0;
                if (this.mBeautyPreviewOn) {
                    strength = getBeautyStrength(this.mBeautyLevel);
                }
                if (strength == CameraDeviceUtils.getBeautyLevel(parameters)) {
                    CamLog.m7i(CameraConstants.TAG, "Already set beauty Level");
                    return;
                }
                CamLog.m7i(CameraConstants.TAG, "updateBeautyLevel " + strength);
                CameraDeviceUtils.setBeautyLevel(parameters, strength);
                this.mCameraDevice.setParameters(parameters);
            }
        }
    }

    public int getBeautyLevel() {
        return this.mBeautyLevel;
    }

    public void setBeautyRelightingLevel(int level) {
        this.mRelightingLevel = level;
        if (this.mCameraDevice != null && !CameraDeviceUtils.isRearCamera(this.mCameraId) && isInit()) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                int strength = 0;
                if (this.mBeautyPreviewOn) {
                    strength = getRelightingStrength(this.mRelightingLevel);
                }
                if (strength == CameraDeviceUtils.getRelightingLevel(parameters)) {
                    CamLog.m7i(CameraConstants.TAG, "Already set relighting Level");
                    return;
                }
                CamLog.m7i(CameraConstants.TAG, "updateRelightingLevel " + strength);
                CameraDeviceUtils.setRelightingLevel(parameters, strength);
                this.mCameraDevice.setParameters(parameters);
            }
        }
    }

    public boolean isBeautyOn() {
        return this.mBeautyOn;
    }

    public boolean isSupportRecordingBeauty(String videoSize) {
        return this.mBeautyRecordingSupported;
    }

    public ParamUpdater setBeautyParam(ParamUpdater param) {
        if (isInit()) {
            int strength = getBeautyStrength(this.mBeautyLevel);
            if (!this.mBeautyPreviewOn) {
                strength = 0;
            }
            CamLog.m7i(CameraConstants.TAG, "Beauty Level " + strength);
            param.setParamValue(ParamConstants.KEY_BEAUTY_LEVEL, "" + strength);
            param.setParamValue("beautyshot", "on");
        } else {
            param.setParamValue(ParamConstants.KEY_BEAUTY_LEVEL, "0");
            param.setParamValue("beautyshot", "off");
        }
        return param;
    }

    public ParamUpdater setBeautyRelightingParam(ParamUpdater param) {
        CamLog.m5e(CameraConstants.TAG, "[relighting] setBeautyRelightingParam");
        if (isInit()) {
            int strength = getRelightingStrength(this.mRelightingLevel);
            if (!this.mBeautyPreviewOn) {
                strength = 0;
            }
            CamLog.m7i(CameraConstants.TAG, "Relighting Level " + strength);
            param.setParamValue(ParamConstants.KEY_RELIGHTING_LEVEL, "" + strength);
        } else {
            param.setParamValue(ParamConstants.KEY_RELIGHTING_LEVEL, "0");
        }
        return param;
    }

    public void onTakePictureBefore() {
        if (!this.mBeautyPreviewOn) {
            this.mBeautyPreviewOn = true;
            setBeautyLevel(getBeautyLevel());
            this.mBeautyPreviewOn = false;
        }
    }

    public void onTakePictureAfter() {
        if (!this.mBeautyPreviewOn) {
            setBeautyLevel(getBeautyLevel());
        }
    }

    public int getBeautyStrength(int beautyLevel) {
        return this.mBeautyOneLevelValue * beautyLevel;
    }

    public int getBeautyStrength() {
        return this.mBeautyLevel * this.mBeautyOneLevelValue;
    }

    public int getRelightingStrength(int level) {
        return this.mRelightingOneLevelValue * level;
    }
}
