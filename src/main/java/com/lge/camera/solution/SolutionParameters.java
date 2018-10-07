package com.lge.camera.solution;

import android.graphics.Rect;
import android.hardware.camera2.params.Face;

public class SolutionParameters extends SolutionParametersforJNI {
    public static final int MAX_FACE_INFO_COUNT = 10;
    private boolean mAutoContrast;
    private boolean mAutoContrastForText;
    private boolean mBackLightDetected;
    private int mBeautyEnabled;
    private int mCameraId;
    private Rect mCropRegion;
    private long mExposureTime;
    private int mFaceDetectionCount;
    private Rect[] mFaceInfo = new Rect[10];
    private boolean mFilmEmulatorMode;
    private String mFlashMode;
    private int mFpsValue;
    private boolean mHDRMode;
    private int mISO;
    private boolean mLowLightDetected;
    private float mLuxIndex;
    private boolean mNightShot;
    private boolean mOutFocus;
    private float mRealGain;
    private int mShootMode;
    private boolean mSignature;
    private boolean mSumBinningMode;
    private boolean mSuperZoomEnabled;
    private int mVideoHeight;
    private int mVideoWidth;
    private float mZoomRatio;

    public SolutionParameters() {
        initParamters();
    }

    public void initParamters() {
        this.mCameraId = 0;
        this.mShootMode = 0;
        this.mISO = -1;
        this.mLuxIndex = -1.0f;
        this.mRealGain = -1.0f;
        this.mExposureTime = -1;
        this.mFlashMode = null;
        this.mHDRMode = false;
        this.mFaceDetectionCount = 0;
        this.mZoomRatio = 0.0f;
        this.mSuperZoomEnabled = false;
        this.mBeautyEnabled = 0;
        this.mFilmEmulatorMode = false;
        this.mSignature = false;
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        this.mSumBinningMode = false;
        this.mLowLightDetected = false;
        this.mAutoContrastForText = false;
        this.mBackLightDetected = false;
        this.mFpsValue = 0;
    }

    void setZoomEnabled(boolean enabled) {
        this.mSuperZoomEnabled = enabled;
    }

    void setSignatureMode(boolean mode) {
        this.mSignature = mode;
    }

    boolean isLowLightDetected() {
        return this.mLowLightDetected;
    }

    void setLowLightDetected(boolean detected) {
        this.mLowLightDetected = detected;
    }

    String isRearCamera() {
        if (this.mCameraId == 0 || this.mCameraId == 2) {
            return "rear";
        }
        return "front";
    }

    int getCameraId() {
        return this.mCameraId;
    }

    void setCameraId(int id) {
        this.mCameraId = id;
    }

    int getShootMode() {
        return this.mShootMode;
    }

    void setShootMode(int mode) {
        this.mShootMode = mode;
    }

    int getISO() {
        return this.mISO;
    }

    void setISO(int iso) {
        this.mISO = iso;
    }

    float getLuxIndex() {
        return this.mLuxIndex;
    }

    void setLuxIndex(float luxIndex) {
        this.mLuxIndex = luxIndex;
    }

    float getRealGain() {
        return this.mRealGain;
    }

    void setRealGain(float gain) {
        this.mRealGain = gain;
    }

    long getExposureTime() {
        return this.mExposureTime;
    }

    void setExposureTime(long expTime) {
        this.mExposureTime = expTime;
    }

    String getFlash() {
        return this.mFlashMode;
    }

    void setFlash(String mode) {
        this.mFlashMode = mode;
    }

    boolean getHDRMode() {
        return this.mHDRMode;
    }

    void setHDRMode(boolean mode) {
        this.mHDRMode = mode;
    }

    Rect[] getFaceInfo() {
        return this.mFaceInfo;
    }

    void setFaceInfo(Face[] faces) {
        for (int i = 0; i < faces.length; i++) {
            if (i < 10) {
                this.mFaceInfo[i] = faces[i].getBounds();
            }
        }
    }

    int getFaceCount() {
        return this.mFaceDetectionCount;
    }

    void setFaceCount(int count) {
        this.mFaceDetectionCount = count;
    }

    float getZoomRatio() {
        return this.mZoomRatio;
    }

    void setZoomRatio(float ratio) {
        this.mZoomRatio = ratio;
    }

    Rect getCropRegion() {
        return this.mCropRegion;
    }

    void setCropRegion(Rect region) {
        this.mCropRegion = region;
    }

    boolean getSuperZoomEnabled() {
        return this.mSuperZoomEnabled;
    }

    boolean getBeautyEnabled() {
        return this.mBeautyEnabled != 0;
    }

    void setBeautyEnabled(int enabled) {
        this.mBeautyEnabled = enabled;
    }

    boolean getFilmEmulatorMode() {
        return this.mFilmEmulatorMode;
    }

    void setFilmEmulatorMode(boolean mode) {
        this.mFilmEmulatorMode = mode;
    }

    boolean getSignatureEanbled() {
        return this.mSignature;
    }

    boolean getOutFocusEanbled() {
        return this.mOutFocus;
    }

    void setOutFocusEnabled(int enabled) {
        this.mOutFocus = enabled != 0;
    }

    boolean getAutoContrastEnabled() {
        return this.mAutoContrast;
    }

    void setAutoContrastEnabled(boolean enabled) {
        this.mAutoContrast = enabled;
    }

    boolean getAutoContrastForTextEnabled() {
        return this.mAutoContrastForText;
    }

    void setAutoContrastForTextEnabled(boolean enabled) {
        this.mAutoContrastForText = enabled;
    }

    boolean getNightShotEnabled() {
        return this.mNightShot;
    }

    void setNightShotEnabled(boolean enabled) {
        this.mNightShot = enabled;
    }

    int getVideoWidth() {
        return this.mVideoWidth;
    }

    int getVideoHeight() {
        return this.mVideoHeight;
    }

    void setVideoSize(int width, int height) {
        this.mVideoWidth = width;
        this.mVideoHeight = height;
    }

    void setSumBinninMode(boolean enabled) {
        this.mSumBinningMode = enabled;
    }

    boolean isSumBinningMode() {
        return this.mSumBinningMode;
    }

    boolean getBackLightDetected() {
        return this.mBackLightDetected;
    }

    void setBackLightDetected(boolean enabled) {
        this.mBackLightDetected = enabled;
    }

    int getFpsValue() {
        return this.mFpsValue;
    }

    void setFpsValue(int fps) {
        this.mFpsValue = fps;
    }
}
