package com.lge.camera.util;

import android.os.Handler;
import android.os.Message;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.SnapShotChecker.SanpShotCheckerListener;

public class SnapShotCheckerBase {
    protected static final int FLASH_CHECK_TIMEOUT_CNT = 2;
    protected static final int RELEASER_START = 101;
    protected static final int RELEASE_DELAY = 500;
    protected static final int RELEASE_DELAY_LONG = 2500;
    protected int mAttachShotState = 0;
    protected int mBurstState = 0;
    protected String mCurFlashSetting = "none";
    protected String mCurHDRSetting = "none";
    protected int mFlashCheckTimeout = 0;
    protected int mFlashJumpCutCnt = 0;
    protected Handler mHandler = new C14331();
    protected int mIntervalShotCnt = 0;
    protected int mIntervalShotTimerChecker = 0;
    protected int mIntevalShotState = 1;
    protected int mLGSFParamState = 0;
    protected SanpShotCheckerListener mListener = null;
    protected int mLongshotAvailable = 0;
    protected int mMultiShotState = 0;
    protected int mPictureCallbackState = 0;
    protected int mRawPicState = 0;
    protected int mSnapShotState = -1;
    protected Object mSyncer = new Object();
    protected int mTakePicFlashState = 0;

    /* renamed from: com.lge.camera.util.SnapShotCheckerBase$1 */
    class C14331 extends Handler {
        C14331() {
        }

        public void handleMessage(Message msg) {
            CamLog.m7i(CameraConstants.TAG, "handleMessage what =" + msg.what);
            switch (msg.what) {
                case 101:
                    if (SnapShotCheckerBase.this.mListener != null) {
                        SnapShotCheckerBase.this.mListener.doTakePictureAfterOnReleaser();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void setSnapShotState(int state) {
        CamLog.m3d(CameraConstants.TAG, "### [setSnapShotState] : " + state);
        this.mSnapShotState = state;
    }

    public void setPictureCallbackState(int state) {
        CamLog.m3d(CameraConstants.TAG, "### [setPictureCallbackState] : " + state);
        this.mPictureCallbackState = state;
    }

    public int getSnapShotState() {
        return this.mSnapShotState;
    }

    public int getPictureCallbackState() {
        return this.mPictureCallbackState;
    }

    public void initSnapShotChecker() {
        this.mSnapShotState = -1;
        this.mPictureCallbackState = 0;
        this.mMultiShotState = 0;
        this.mRawPicState = 0;
        this.mBurstState = 0;
        this.mLGSFParamState = 0;
        this.mIntevalShotState = 1;
        this.mAttachShotState = 0;
        this.mFlashCheckTimeout = 0;
        this.mCurFlashSetting = "none";
        this.mCurHDRSetting = "none";
        this.mLongshotAvailable = 0;
        this.mTakePicFlashState = 0;
    }

    public void releaseSnapShotChecker() {
        this.mSnapShotState = 0;
        this.mPictureCallbackState = 0;
    }

    public void resetSnapShotChecker() {
        this.mSnapShotState = -1;
        this.mPictureCallbackState = 0;
        removeReleaser();
    }

    public boolean isNotTaking() {
        return this.mSnapShotState == -1;
    }

    public boolean isAvailableTakePictureBefore() {
        return this.mPictureCallbackState == 0 || this.mPictureCallbackState == 3;
    }

    public boolean isSnapShotProcessing() {
        return this.mSnapShotState > 2 || this.mPictureCallbackState > 0;
    }

    public boolean isIdle() {
        return this.mSnapShotState <= 0 && this.mPictureCallbackState == 0;
    }

    public void setLGSFParamState(int state) {
        this.mLGSFParamState = state;
    }

    public int getLGSFParamState() {
        return this.mLGSFParamState;
    }

    public void resetLGSFParamState() {
        this.mLGSFParamState = 0;
    }

    public void setMultiShotState(int state) {
        this.mMultiShotState |= state;
    }

    public void removeMultiShotState(int state) {
        this.mMultiShotState &= state ^ -1;
    }

    public boolean checkMultiShotState(int checkType) {
        int i;
        int i2 = 1;
        boolean invalid = false;
        if ((checkType & 1) != 0) {
            invalid = false | ((this.mMultiShotState & 1) != 0 ? 1 : 0);
        }
        if ((checkType & 2) != 0) {
            if ((this.mMultiShotState & 2) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 4) != 0) {
            if ((this.mMultiShotState & 4) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 8) != 0) {
            if ((this.mMultiShotState & 8) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 16) == 0) {
            return invalid;
        }
        if ((this.mMultiShotState & 16) == 0) {
            i2 = 0;
        }
        return invalid | i2;
    }

    public void setBurstState(int state) {
        this.mBurstState |= state;
    }

    public int getBurstState() {
        return this.mBurstState;
    }

    public void removeBurstState(int state) {
        this.mBurstState &= state ^ -1;
    }

    public boolean checkBurstState(int checkType) {
        int i;
        int i2 = 1;
        boolean invalid = false;
        if ((checkType & 1) != 0) {
            invalid = false | ((this.mBurstState & 1) != 0 ? 1 : 0);
        }
        if ((checkType & 2) != 0) {
            if ((this.mBurstState & 2) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 8) != 0) {
            if ((this.mBurstState & 8) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 4) != 0) {
            if ((this.mBurstState & 4) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 16) != 0) {
            if ((this.mBurstState & 16) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 32) != 0) {
            if ((this.mBurstState & 32) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 64) == 0) {
            return invalid;
        }
        if ((this.mBurstState & 64) == 0) {
            i2 = 0;
        }
        return invalid | i2;
    }

    public void setIntervalShotState(int state) {
        this.mIntevalShotState |= state;
    }

    public void removeIntervalShotState(int state) {
        this.mIntevalShotState &= state ^ -1;
    }

    public boolean checkIntervalShotState(int checkType) {
        int i;
        int i2 = 1;
        boolean invalid = false;
        if ((checkType & 1) != 0) {
            invalid = false | ((this.mIntevalShotState & 1) != 0 ? 1 : 0);
        }
        if ((checkType & 2) != 0) {
            if ((this.mIntevalShotState & 2) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 4) != 0) {
            if ((this.mIntevalShotState & 4) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 8) == 0) {
            return invalid;
        }
        if ((this.mIntevalShotState & 8) == 0) {
            i2 = 0;
        }
        return invalid | i2;
    }

    public int increaseIntervalShotCount() {
        int i = this.mIntervalShotCnt + 1;
        this.mIntervalShotCnt = i;
        return i;
    }

    public int getIntervalShotCount() {
        return this.mIntervalShotCnt;
    }

    public void initIntervalShotCount() {
        this.mIntervalShotCnt = 0;
    }

    public boolean isIntervalShotMax() {
        return this.mIntervalShotCnt >= 4;
    }

    public int increaseFlashJumpCutCount() {
        CamLog.m3d(CameraConstants.TAG, "[jumpcut] before mFlashJumpCutCnt : " + this.mFlashJumpCutCnt);
        int i = this.mFlashJumpCutCnt + 1;
        this.mFlashJumpCutCnt = i;
        return i;
    }

    public int getFlashJumpCutCount() {
        return this.mFlashJumpCutCnt;
    }

    public void initFlashJumpCutCount() {
        this.mFlashJumpCutCnt = 0;
    }

    public int increaseIntervalShotTimerChecker() {
        int i = this.mIntervalShotTimerChecker + 1;
        this.mIntervalShotTimerChecker = i;
        return i;
    }

    public void initIntervalShotTimerChecker() {
        this.mIntervalShotTimerChecker = 0;
    }

    public boolean isWaitingIntervalShot() {
        return this.mIntervalShotTimerChecker < 1;
    }

    public void setRawPicState(int state) {
        this.mRawPicState = state;
    }

    public int getRawPicState() {
        return this.mRawPicState;
    }

    public void setAttachShotState(int state) {
        this.mAttachShotState = state;
    }

    public boolean isAttachShotPictureTaken() {
        return this.mAttachShotState == 1;
    }

    public void releaseAttachShotState() {
        this.mAttachShotState = 0;
    }

    public void sendReleaser(boolean useLongDelay) {
        synchronized (this.mSyncer) {
            if (this.mHandler == null) {
                return;
            }
            int delayType = useLongDelay ? 2500 : 500;
            this.mHandler.removeMessages(101);
            this.mHandler.sendEmptyMessageDelayed(101, (long) delayType);
            CamLog.m3d(CameraConstants.TAG, "[Releaser] remove and sendReleaser.");
        }
    }

    /* JADX WARNING: Missing block: B:14:?, code:
            return;
     */
    public void removeReleaser() {
        /*
        r3 = this;
        r1 = r3.mSyncer;
        monitor-enter(r1);
        r0 = r3.mHandler;	 Catch:{ all -> 0x0023 }
        if (r0 != 0) goto L_0x0009;
    L_0x0007:
        monitor-exit(r1);	 Catch:{ all -> 0x0023 }
    L_0x0008:
        return;
    L_0x0009:
        r0 = r3.mHandler;	 Catch:{ all -> 0x0023 }
        r2 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r0 = r0.hasMessages(r2);	 Catch:{ all -> 0x0023 }
        if (r0 == 0) goto L_0x0021;
    L_0x0013:
        r0 = r3.mHandler;	 Catch:{ all -> 0x0023 }
        r2 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        r0.removeMessages(r2);	 Catch:{ all -> 0x0023 }
        r0 = "CameraApp";
        r2 = "[Releaser] removeReleaser.";
        com.lge.camera.util.CamLog.m3d(r0, r2);	 Catch:{ all -> 0x0023 }
    L_0x0021:
        monitor-exit(r1);	 Catch:{ all -> 0x0023 }
        goto L_0x0008;
    L_0x0023:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0023 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.SnapShotCheckerBase.removeReleaser():void");
    }

    public boolean hasReleaser() {
        boolean z;
        synchronized (this.mSyncer) {
            if (this.mHandler == null) {
                z = false;
            } else {
                z = this.mHandler.hasMessages(101);
            }
        }
        return z;
    }
}
