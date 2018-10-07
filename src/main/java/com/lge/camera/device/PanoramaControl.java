package com.lge.camera.device;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Range;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.api2.CameraOps;
import com.lge.camera.util.CamLog;

public class PanoramaControl {
    protected int mDebugMetadata = 0;
    protected long mDefaultExposureTimeNano = 0;
    protected long mExposureTime = 0;
    protected float mFocusDistance = 0.0f;
    private IPanoramaControl mGet = null;
    protected long mLastFrameExposureTime = 0;
    protected int mLastFrameISO = 400;
    protected long mMinExposureTimeNano = 0;
    protected int mSceneFlicker = 0;
    protected int mSensorSensitivity = 400;

    public interface IPanoramaControl {
        CameraProxy getCameraDevice();
    }

    public PanoramaControl(IPanoramaControl panoramaControl) {
        this.mGet = panoramaControl;
        initializationControl();
    }

    public void initializationControl() {
        this.mDefaultExposureTimeNano = exposureTimeDenominatorToNanoseconds(60);
        this.mMinExposureTimeNano = exposureTimeDenominatorToNanoseconds(120);
        this.mExposureTime = this.mDefaultExposureTimeNano;
    }

    public long exposureTimeDenominatorToNanoseconds(int exposureTimeDenominator) {
        return exposureTimeDenominator > 0 ? (long) (1.0E9d / ((double) exposureTimeDenominator)) : 0;
    }

    public long getExposureTime() {
        return this.mExposureTime;
    }

    public int getSensorSensitivity() {
        return this.mSensorSensitivity;
    }

    public int getLastFrameISO() {
        return this.mLastFrameISO;
    }

    public long getmLastFrameExposureTime() {
        return this.mLastFrameExposureTime;
    }

    public float getmFocusDistance() {
        return this.mFocusDistance;
    }

    public void quickenExposureTimeConstantBrightness() {
        if (FunctionProperties.getSupportedHal() != 2) {
            CamLog.m5e(CameraConstants.TAG, "quickenExposureTimeConstantBrightness should be invoked for HAL3. return.");
            return;
        }
        if (this.mSceneFlicker == 1) {
            this.mDefaultExposureTimeNano = exposureTimeDenominatorToNanoseconds(50);
            this.mMinExposureTimeNano = exposureTimeDenominatorToNanoseconds(100);
            this.mExposureTime = this.mDefaultExposureTimeNano;
            CamLog.m7i(CameraConstants.TAG, "Current sceneFilcker : " + this.mSceneFlicker + ", default exposure time : " + this.mDefaultExposureTimeNano + ", min exposure time : " + this.mMinExposureTimeNano);
        } else {
            initializationControl();
            CamLog.m7i(CameraConstants.TAG, "Current sceneFilcker : " + this.mSceneFlicker);
        }
        if (this.mLastFrameExposureTime > this.mMinExposureTimeNano) {
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice == null) {
                CamLog.m5e(CameraConstants.TAG, "cameraDevice is null. return.");
                return;
            }
            CameraOps camera2 = (CameraOps) cameraDevice.getCamera();
            if (camera2 == null) {
                CamLog.m5e(CameraConstants.TAG, "CameraOps is null. return.");
                return;
            }
            CameraCharacteristics characteristics = camera2.getCameraCharacteristics();
            if (characteristics == null) {
                CamLog.m5e(CameraConstants.TAG, "Camera Characteristics is null. return.");
                return;
            }
            Range<Integer> sensitivityRange = (Range) characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
            CamLog.m3d(CameraConstants.TAG, "sensitivity range: " + sensitivityRange);
            CamLog.m3d(CameraConstants.TAG, "exposure time range: " + ((Range) characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)));
            long targetExposureTime = this.mMinExposureTimeNano;
            int newSensorSensitivity = (int) (((float) this.mLastFrameISO) * (((float) this.mLastFrameExposureTime) / ((float) targetExposureTime)));
            if (sensitivityRange != null) {
                float sens_upper = (float) ((Integer) sensitivityRange.getUpper()).intValue();
                if (sens_upper < ((float) newSensorSensitivity) && sens_upper > 0.0f) {
                    targetExposureTime = this.mDefaultExposureTimeNano;
                    newSensorSensitivity = (int) (((float) this.mLastFrameISO) * (((float) this.mLastFrameExposureTime) / ((float) targetExposureTime)));
                    if (sens_upper < ((float) newSensorSensitivity) && sens_upper > 0.0f) {
                        newSensorSensitivity = (int) sens_upper;
                    }
                }
                this.mSensorSensitivity = newSensorSensitivity;
                this.mExposureTime = targetExposureTime;
                CamLog.m7i(CameraConstants.TAG, "mLastFrameExposureTime : " + this.mLastFrameExposureTime + ", mLastFrameISO : " + this.mLastFrameISO);
            }
        } else {
            this.mExposureTime = this.mLastFrameExposureTime;
            this.mSensorSensitivity = this.mLastFrameISO;
        }
        CamLog.m7i(CameraConstants.TAG, "mExposureTime : " + this.mExposureTime + ", mSensorSensitivity : " + this.mSensorSensitivity);
    }

    public void onMetadataCallback(TotalCaptureResult result, boolean isFullFrame) {
        if (result == null) {
            CamLog.m11w(CameraConstants.TAG, "CameraImageMetaCallback, TotalCaptureResult is null. return.");
            return;
        }
        this.mLastFrameISO = ((Integer) result.get(CaptureResult.SENSOR_SENSITIVITY)).intValue();
        this.mLastFrameExposureTime = ((Long) result.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue();
        this.mFocusDistance = ((Float) result.get(CaptureResult.LENS_FOCUS_DISTANCE)).floatValue();
        this.mSceneFlicker = ((Integer) result.get(CaptureResult.STATISTICS_SCENE_FLICKER)).intValue();
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (isFullFrame && cameraDevice != null) {
            cameraDevice.updateRequestCapture(this.mExposureTime, this.mSensorSensitivity);
        }
        this.mDebugMetadata++;
        if (this.mDebugMetadata > 30) {
            this.mDebugMetadata = 0;
            CamLog.m3d(CameraConstants.TAG, "onMetadataCallback. mLastFrameISO : " + this.mLastFrameISO + ", mLastFrameExposureTime : " + this.mLastFrameExposureTime + ", mFocusDistance : " + this.mFocusDistance + ", mSceneFlicker : " + this.mSceneFlicker);
        }
    }
}
