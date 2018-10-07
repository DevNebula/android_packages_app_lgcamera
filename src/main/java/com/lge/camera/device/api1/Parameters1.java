package com.lge.camera.device.api1;

import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.params.RggbChannelVector;
import android.util.Size;
import com.lge.camera.device.CameraParameters;
import com.lge.hardware.LGCamera;
import java.util.ArrayList;
import java.util.List;

public class Parameters1 implements CameraParameters {
    Parameters mParameters;

    public Parameters1(LGCamera camera) {
        if (camera != null && camera.getCamera() != null) {
            this.mParameters = camera.getCamera().getParameters();
        }
    }

    public Parameters1(Parameters parameters) {
        this.mParameters = parameters;
    }

    public Object getParameters() {
        return this.mParameters;
    }

    public void setParameters(Object parameters) {
        this.mParameters = (Parameters) parameters;
    }

    public String flatten() {
        return this.mParameters.flatten();
    }

    public void unflatten(String flattened) {
        this.mParameters.unflatten(flattened);
    }

    public void remove(String key) {
        this.mParameters.remove(key);
    }

    public void set(String key, String value) {
        this.mParameters.set(key, value);
    }

    public void set(String key, int value) {
        this.mParameters.set(key, value);
    }

    public String get(String key) {
        return this.mParameters.get(key);
    }

    public int getInt(String key) {
        return this.mParameters.getInt(key);
    }

    public void setPreviewSize(int width, int height) {
        this.mParameters.setPreviewSize(width, height);
    }

    public Size getPreviewSize() {
        return new Size(this.mParameters.getPreviewSize().width, this.mParameters.getPreviewSize().height);
    }

    public Size getPictureSize() {
        return new Size(this.mParameters.getPictureSize().width, this.mParameters.getPictureSize().height);
    }

    public List<Size> getSupportedPictureSizes() {
        List<Size> supported = new ArrayList();
        for (Camera.Size size : this.mParameters.getSupportedPictureSizes()) {
            supported.add(new Size(size.width, size.height));
        }
        return supported;
    }

    public void setRotation(int rotation) {
        this.mParameters.setRotation(rotation);
    }

    public void setGpsLatitude(double latitude) {
        this.mParameters.setGpsLatitude(latitude);
    }

    public void setGpsLongitude(double longitude) {
        this.mParameters.setGpsLongitude(longitude);
    }

    public void setGpsAltitude(double altitude) {
        this.mParameters.setGpsAltitude(altitude);
    }

    public void setGpsTimestamp(long timestamp) {
        this.mParameters.setGpsTimestamp(timestamp);
    }

    public void setGpsProcessingMethod(String processing_method) {
        this.mParameters.setGpsProcessingMethod(processing_method);
    }

    public void removeGpsData() {
        this.mParameters.removeGpsData();
    }

    public String getWhiteBalance() {
        return this.mParameters.getWhiteBalance();
    }

    public void setWhiteBalance(String value) {
        this.mParameters.setWhiteBalance(value);
    }

    public List<String> getSupportedWhiteBalance() {
        return this.mParameters.getSupportedWhiteBalance();
    }

    public String getAntibanding() {
        return this.mParameters.getAntibanding();
    }

    public void setAntibanding(String antibanding) {
        this.mParameters.setAntibanding(antibanding);
    }

    public List<String> getSupportedAntibanding() {
        return this.mParameters.getSupportedAntibanding();
    }

    public String getSceneMode() {
        return this.mParameters.getSceneMode();
    }

    public void setSceneMode(String value) {
        this.mParameters.setSceneMode(value);
    }

    public List<String> getSupportedSceneModes() {
        return this.mParameters.getSupportedSceneModes();
    }

    public String getFlashMode() {
        return this.mParameters.getFlashMode();
    }

    public void setFlashMode(String value) {
        this.mParameters.setFlashMode(value);
    }

    public List<String> getSupportedFlashModes() {
        return this.mParameters.getSupportedFlashModes();
    }

    public String getFocusMode() {
        return this.mParameters.getFocusMode();
    }

    public void setFocusMode(String value) {
        this.mParameters.setFocusMode(value);
    }

    public List<String> getSupportedFocusModes() {
        return this.mParameters.getSupportedFocusModes();
    }

    public float getFocalLength() {
        return this.mParameters.getFocalLength();
    }

    public float getHorizontalViewAngle() {
        return this.mParameters.getHorizontalViewAngle();
    }

    public float getVerticalViewAngle() {
        return this.mParameters.getVerticalViewAngle();
    }

    public int getExposureCompensation() {
        return this.mParameters.getExposureCompensation();
    }

    public void setExposureCompensation(int value) {
        this.mParameters.setExposureCompensation(value);
    }

    public int getMaxExposureCompensation() {
        return this.mParameters.getMaxExposureCompensation();
    }

    public int getMinExposureCompensation() {
        return this.mParameters.getMinExposureCompensation();
    }

    public void setAutoExposureLock(boolean toggle) {
        this.mParameters.setAutoExposureLock(toggle);
    }

    public boolean getAutoExposureLock() {
        return this.mParameters.getAutoExposureLock();
    }

    public boolean isAutoExposureLockSupported() {
        return this.mParameters.isAutoExposureLockSupported();
    }

    public void setAutoWhiteBalanceLock(boolean toggle) {
        this.mParameters.setAutoWhiteBalanceLock(toggle);
    }

    public boolean getAutoWhiteBalanceLock() {
        return this.mParameters.getAutoWhiteBalanceLock();
    }

    public boolean isAutoWhiteBalanceLockSupported() {
        return this.mParameters.isAutoWhiteBalanceLockSupported();
    }

    public int getZoom() {
        return this.mParameters.getZoom();
    }

    public void setZoom(int value) {
        this.mParameters.setZoom(value);
    }

    public boolean isZoomSupported() {
        return this.mParameters.isZoomSupported();
    }

    public int getMaxZoom() {
        return this.mParameters.getMaxZoom();
    }

    public List<Integer> getZoomRatios() {
        return this.mParameters.getZoomRatios();
    }

    public int getMaxNumFocusAreas() {
        return this.mParameters.getMaxNumFocusAreas();
    }

    public List<Area> getFocusAreas() {
        return this.mParameters.getFocusAreas();
    }

    public void setFocusAreas(List<Area> focusAreas) {
        this.mParameters.setFocusAreas(focusAreas);
    }

    public int getMaxNumMeteringAreas() {
        return this.mParameters.getMaxNumMeteringAreas();
    }

    public List<Area> getMeteringAreas() {
        return this.mParameters.getMeteringAreas();
    }

    public void setMeteringAreas(List<Area> meteringAreas) {
        this.mParameters.setMeteringAreas(meteringAreas);
    }

    public int getMaxNumDetectedFaces() {
        return this.mParameters.getMaxNumDetectedFaces();
    }

    public void setRecordingHint(boolean hint) {
        this.mParameters.setRecordingHint(hint);
    }

    public int getPreviewFormat() {
        return this.mParameters.getPreviewFormat();
    }

    public void setColorCorrectionGains(RggbChannelVector rggbChannelVector) {
    }

    public String getStrValue(Object paramValue) {
        return null;
    }

    public void set(String key, float value) {
    }

    public void setManualFocusStep(String value) {
    }

    public int getContrast() {
        return 0;
    }

    public void setContrast(int value) {
    }

    public List<String> getSupportedColorEffects() {
        return this.mParameters.getSupportedColorEffects();
    }

    public String getColorEffect() {
        return this.mParameters.getColorEffect();
    }
}
