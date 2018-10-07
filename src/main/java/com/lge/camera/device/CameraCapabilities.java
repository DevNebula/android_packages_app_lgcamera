package com.lge.camera.device;

import com.lge.camera.constants.FunctionProperties;
import java.util.List;

public class CameraCapabilities {
    private boolean mAFSupported = true;
    private boolean mAeLockSupported = true;
    private boolean mAwbLockSupported = true;
    private List<String> mColorEffectList = null;
    private boolean mContinousFocusSupported = true;
    private boolean mFaceDetectionSupproted = false;
    private boolean mFingerDetectionSupported = false;
    private boolean mFlashSupported = true;
    private boolean mFocusAreaSupported = true;
    private boolean mFocusPeakingSupproted = false;
    private boolean mFocusTrackingSupproted = false;
    private boolean mFrontCameraSuppotred = true;
    private CameraParameters mInitialParams = null;
    private boolean mLiveSnapshotSupported = true;
    private boolean mMWContinousFocusSupported = true;
    private boolean mManualAntibandingSupported = true;
    private int mMaxEVStep = 12;
    private int mMaxZoomLevel = 0;
    private boolean mMeteringAreaSupported = true;
    private boolean mPictureFlipSupported = true;
    private boolean mVideoFlipSupported = true;
    private boolean[] mVideoHDRSupported = null;
    private boolean mVideoStabiliztionSupported = true;
    private boolean mWBSupported = true;
    private boolean mZoomSupported = true;

    public CameraCapabilities(CameraParameters parameters) {
        if (parameters != null) {
            this.mInitialParams = parameters;
            this.mAFSupported = CameraDeviceUtils.isAFSupported(this.mInitialParams);
            this.mFocusAreaSupported = CameraDeviceUtils.isFocusAreaSupported(this.mInitialParams);
            this.mMeteringAreaSupported = CameraDeviceUtils.isMeteringAreaSupported(this.mInitialParams);
            this.mAeLockSupported = CameraDeviceUtils.isAutoExposureLockSupported(this.mInitialParams);
            this.mAwbLockSupported = CameraDeviceUtils.isAutoWhiteBalanceLockSupported(this.mInitialParams);
            this.mVideoStabiliztionSupported = CameraDeviceUtils.isVideoStabilizationSupported(this.mInitialParams);
            this.mVideoHDRSupported = CameraDeviceUtils.isVideoHDRSupported(this.mInitialParams);
            this.mLiveSnapshotSupported = CameraDeviceUtils.isVideoSnapshotSupported(this.mInitialParams);
            this.mFlashSupported = CameraDeviceUtils.isFlashSupported(this.mInitialParams, "auto");
            this.mWBSupported = CameraDeviceUtils.isWhiteBalanceSupported(this.mInitialParams);
            this.mZoomSupported = CameraDeviceUtils.isZoomSupported(this.mInitialParams);
            this.mMaxZoomLevel = CameraDeviceUtils.getMaxZoom(this.mInitialParams);
            List<String> supportedFocusModes = this.mInitialParams.getSupportedFocusModes();
            if (supportedFocusModes != null) {
                this.mContinousFocusSupported = supportedFocusModes.contains(ParamConstants.FOCUS_MODE_CONTINUOUS_PICTURE);
                this.mMWContinousFocusSupported = supportedFocusModes.contains(ParamConstants.FOCUS_MODE_MULTIWINDOWAF);
            }
            this.mFrontCameraSuppotred = CameraDeviceUtils.isFrontCameraSupported();
            this.mManualAntibandingSupported = CameraDeviceUtils.isManualAntiBandingSupported(this.mInitialParams);
            this.mPictureFlipSupported = CameraDeviceUtils.isSnapshotPictureFlipSupported(this.mInitialParams);
            this.mVideoFlipSupported = CameraDeviceUtils.isVideoFlipSupported(this.mInitialParams);
            this.mFocusPeakingSupproted = CameraDeviceUtils.isFocusPeakingSupported(this.mInitialParams);
            this.mFocusTrackingSupproted = CameraDeviceUtils.isFocusTrackingSupported(this.mInitialParams);
            this.mFaceDetectionSupproted = CameraDeviceUtils.isFaceDetectionSupported(this.mInitialParams);
            this.mFingerDetectionSupported = CameraDeviceUtils.isFingerDetectionSupported(this.mInitialParams);
            this.mMaxEVStep = this.mInitialParams.getMaxExposureCompensation();
            this.mColorEffectList = this.mInitialParams.getSupportedColorEffects();
        }
    }

    public CameraParameters getInitialParams() {
        return this.mInitialParams;
    }

    public boolean isAFSupported() {
        return this.mAFSupported;
    }

    public boolean isFocusAreaSupported() {
        return this.mFocusAreaSupported;
    }

    public boolean isMeteringAreaSupported() {
        return this.mMeteringAreaSupported;
    }

    public boolean isAeLockSupported() {
        return this.mAeLockSupported;
    }

    public boolean isAwbLockSupported() {
        return this.mAwbLockSupported;
    }

    public boolean isContinousFocusSupported() {
        return this.mContinousFocusSupported;
    }

    public boolean isMWContinousFocusSupported() {
        return this.mMWContinousFocusSupported;
    }

    public boolean isLiveSnapshotSupported() {
        return this.mLiveSnapshotSupported;
    }

    public boolean isVideoStabiliztionSupported() {
        return this.mVideoStabiliztionSupported;
    }

    public boolean isVideoHDRSupported(int cameraId) {
        if (this.mVideoHDRSupported == null || this.mVideoHDRSupported.length <= cameraId) {
            return false;
        }
        return this.mVideoHDRSupported[cameraId];
    }

    public boolean isFlashSupported() {
        return this.mFlashSupported;
    }

    public boolean isWBSupported() {
        return this.mWBSupported;
    }

    public boolean isZoomSupported() {
        return this.mZoomSupported;
    }

    public int getMaxZoom() {
        return this.mMaxZoomLevel;
    }

    public boolean isFrontCameraSupported() {
        return this.mFrontCameraSuppotred;
    }

    public boolean isManualAntibandingSupported() {
        return this.mManualAntibandingSupported;
    }

    public boolean isPictureFlipSupported() {
        return this.mPictureFlipSupported;
    }

    public boolean isVideoFlipSupported() {
        return this.mVideoFlipSupported;
    }

    public boolean isFocusPeakingSupported() {
        return this.mFocusPeakingSupproted;
    }

    public boolean isFocusTrackingSupported() {
        return this.mFocusTrackingSupproted;
    }

    public boolean isFaceDetectionSupported() {
        return this.mFaceDetectionSupproted;
    }

    public void disalbeFocusFeature() {
        this.mAFSupported = false;
        this.mMWContinousFocusSupported = false;
        this.mContinousFocusSupported = false;
        this.mFocusAreaSupported = false;
        this.mMeteringAreaSupported = false;
        this.mFocusPeakingSupproted = false;
        this.mFocusTrackingSupproted = false;
    }

    public boolean isFingerDetectionSupported() {
        if (this.mFingerDetectionSupported && FunctionProperties.isSupportedFingerDetection()) {
            return true;
        }
        return false;
    }

    public int getMaxEVStep() {
        return this.mMaxEVStep;
    }

    public List<String> getSupportedColorEffects() {
        return this.mColorEffectList;
    }
}
