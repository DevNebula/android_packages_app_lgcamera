package com.lge.camera.device;

import android.hardware.Camera.Area;
import android.hardware.camera2.params.RggbChannelVector;
import android.util.Size;
import java.util.List;

public interface CameraParameters {
    String flatten();

    String get(String str);

    String getAntibanding();

    boolean getAutoExposureLock();

    boolean getAutoWhiteBalanceLock();

    String getColorEffect();

    int getContrast();

    int getExposureCompensation();

    String getFlashMode();

    float getFocalLength();

    List<Area> getFocusAreas();

    String getFocusMode();

    float getHorizontalViewAngle();

    int getInt(String str);

    int getMaxExposureCompensation();

    int getMaxNumDetectedFaces();

    int getMaxNumFocusAreas();

    int getMaxNumMeteringAreas();

    int getMaxZoom();

    List<Area> getMeteringAreas();

    int getMinExposureCompensation();

    Object getParameters();

    Size getPictureSize();

    int getPreviewFormat();

    Size getPreviewSize();

    String getSceneMode();

    String getStrValue(Object obj);

    List<String> getSupportedAntibanding();

    List<String> getSupportedColorEffects();

    List<String> getSupportedFlashModes();

    List<String> getSupportedFocusModes();

    List<Size> getSupportedPictureSizes();

    List<String> getSupportedSceneModes();

    List<String> getSupportedWhiteBalance();

    float getVerticalViewAngle();

    String getWhiteBalance();

    int getZoom();

    List<Integer> getZoomRatios();

    boolean isAutoExposureLockSupported();

    boolean isAutoWhiteBalanceLockSupported();

    boolean isZoomSupported();

    void remove(String str);

    void removeGpsData();

    void set(String str, float f);

    void set(String str, int i);

    void set(String str, String str2);

    void setAntibanding(String str);

    void setAutoExposureLock(boolean z);

    void setAutoWhiteBalanceLock(boolean z);

    void setColorCorrectionGains(RggbChannelVector rggbChannelVector);

    void setContrast(int i);

    void setExposureCompensation(int i);

    void setFlashMode(String str);

    void setFocusAreas(List<Area> list);

    void setFocusMode(String str);

    void setGpsAltitude(double d);

    void setGpsLatitude(double d);

    void setGpsLongitude(double d);

    void setGpsProcessingMethod(String str);

    void setGpsTimestamp(long j);

    void setManualFocusStep(String str);

    void setMeteringAreas(List<Area> list);

    void setParameters(Object obj);

    void setPreviewSize(int i, int i2);

    void setRecordingHint(boolean z);

    void setRotation(int i);

    void setSceneMode(String str);

    void setWhiteBalance(String str);

    void setZoom(int i);

    void unflatten(String str);
}
