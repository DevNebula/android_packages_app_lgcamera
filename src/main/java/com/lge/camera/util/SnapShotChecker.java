package com.lge.camera.util;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;

public class SnapShotChecker extends SnapShotCheckerBase {
    private int mCaptureCount = 0;
    private boolean mInitBurstCountLater = false;
    public boolean mIsStillSavingBurstShot = false;

    public interface SanpShotCheckerListener {
        boolean doTakePictureAfterOnReleaser();

        String getSettingValue(String str);
    }

    public SnapShotChecker(SanpShotCheckerListener settingGetter) {
        this.mListener = settingGetter;
    }

    public void setLongshotAvailable(int curFlash, int curHDR, int curNight) {
        if (FunctionProperties.isSupportedFastShot()) {
            if (curFlash == 3) {
                this.mLongshotAvailable |= 1;
            } else {
                this.mLongshotAvailable &= -2;
            }
            if (curHDR == 3) {
                this.mLongshotAvailable |= 2;
            } else {
                this.mLongshotAvailable &= -3;
            }
            if (curNight == 4) {
                this.mLongshotAvailable |= 4;
            } else {
                this.mLongshotAvailable &= -5;
            }
            CamLog.m3d(CameraConstants.TAG, "mLongshotAvailable = " + this.mLongshotAvailable);
        }
    }

    public void setCurFlashSetting(String flashValue) {
        this.mCurFlashSetting = flashValue;
    }

    public void setCurHDRSetting(String hdrValue) {
        this.mCurHDRSetting = hdrValue;
    }

    public boolean isFastShotAvailable(int checkItem) {
        int i = 1;
        validateFlashAndHdrSetting();
        if (this.mLongshotAvailable != 0) {
            return false;
        }
        boolean result = true;
        if ((checkItem & 1) != 0) {
            result = true & (!"on".equals(this.mCurFlashSetting) ? 1 : 0);
        }
        if ((checkItem & 2) != 0) {
            if ("1".equals(this.mCurHDRSetting)) {
                i = 0;
            }
            result &= i;
        }
        return result;
    }

    public void setTakePicFlashState(int state) {
        this.mTakePicFlashState = state;
    }

    public void resetTakePicFlashState() {
        this.mTakePicFlashState = 0;
        this.mFlashCheckTimeout = 0;
    }

    public int getTakePicFlashState() {
        return this.mTakePicFlashState;
    }

    public void increaseFlashCheckTimeOut() {
        this.mFlashCheckTimeout++;
    }

    public boolean isFlashCheckTimeOut() {
        return this.mFlashCheckTimeout > 2;
    }

    public boolean isAvailableStateWithFlash() {
        validateFlashAndHdrSetting();
        return !"auto".equals(this.mCurFlashSetting) || (this.mLongshotAvailable & 1) == 0;
    }

    public void validateFlashAndHdrSetting() {
        if (this.mListener != null) {
            if ("none".equals(this.mCurFlashSetting)) {
                this.mCurFlashSetting = this.mListener.getSettingValue("flash-mode");
            }
            if ("none".equals(this.mCurHDRSetting)) {
                this.mCurHDRSetting = this.mListener.getSettingValue("hdr-mode");
            }
        }
    }

    public int increaseBurstCount() {
        int i = this.mCaptureCount + 1;
        this.mCaptureCount = i;
        return i;
    }

    public boolean isBurstCountMax(boolean isInternalStorage, boolean invalidate, boolean isRearCamera) {
        if (isRearCamera) {
            return this.mCaptureCount >= FunctionProperties.getSupportedBurstShotMaxCount(isInternalStorage, invalidate, isRearCamera);
        } else {
            if (this.mCaptureCount < 20) {
                return false;
            }
            return true;
        }
    }

    public boolean isBurstCaptureStarted() {
        return this.mCaptureCount > 0;
    }

    public int getBurstCount() {
        return this.mCaptureCount;
    }

    public void initBurstCount() {
        this.mCaptureCount = 0;
        this.mInitBurstCountLater = false;
    }

    public void initBurstCountLater(boolean intiLater) {
        if (intiLater) {
            if (checkBurstState(64)) {
                this.mInitBurstCountLater = intiLater;
            } else {
                initBurstCount();
            }
        } else if (this.mInitBurstCountLater) {
            initBurstCount();
        }
    }

    public void setStillSavingBurstShot(boolean set) {
        this.mIsStillSavingBurstShot = set;
    }

    public boolean isStillSavingBurstShot() {
        return this.mIsStillSavingBurstShot;
    }

    public boolean isAvailableNightAndFlash() {
        return (this.mLGSFParamState == 2 || this.mTakePicFlashState == 1) ? false : true;
    }

    public boolean isAvailableGestureZoom() {
        return isAvailableNightAndFlash() && this.mSnapShotState <= 0;
    }
}
