package com.lge.camera.device.api2;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.DngCreator;
import android.os.ConditionVariable;
import android.util.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraCallbackForwards.CameraErrorCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.ShutterCallbackForward;
import com.lge.camera.device.ICameraCallback.OutFocusCallback;
import com.lge.camera.device.OutfocusCaptureResult;
import com.lge.camera.solution.SolutionPickResult;
import com.lge.camera.util.CamLog;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SnapShotManager {
    public static final int BURST_SHOT = 4;
    private static final boolean DROP_SHOT_BEFORE_JPEG_CB = true;
    public static final int FLASH_SHOT = 2;
    public static final int IDLE = 0;
    public static final int JPEG_CAPTURE_CNT = 1;
    public static final int NORMAL_SHOT = 1;
    public static final int PANORAMA_SHOT = 5;
    public static final int RAW_CAPTURE_CNT = 2;
    public static final int SOLUTION_SHOT = 3;
    private static final int TIME_TO_DROP = 80;
    private int mBurstId = 1;
    private HashMap<Long, Integer> mBurstShotIdMap = new HashMap();
    CaptureListener mCaptureListener;
    private final byte[] mDummyExif = new byte[1];
    LinkedBlockingQueue<byte[]> mHiddenExifQueue = new LinkedBlockingQueue();
    private boolean mIsProcessingRawShot = false;
    private boolean mIsProcessingShot = false;
    private long mLastShotTime = 0;
    private final Object mLock = new Object();
    OutFocusCallback mOutFocusCallback = null;
    private ConditionVariable mRawShotLock;
    private int mRequestShotCnt = 0;
    private ConditionVariable mShotLock;
    ShutterCallbackForward mShutterCallback;
    private int mState = 0;

    public void setLastShotTime(long shotTime) {
        synchronized (this.mLock) {
            this.mLastShotTime = shotTime;
        }
    }

    public void setState(int state) {
        synchronized (this.mLock) {
            if (this.mState == 0 || state == 0) {
                this.mState = state;
            } else {
                CamLog.m3d(CameraConstants.TAG, " Abnormal State change.. current " + this.mState + "  new State : " + state);
            }
        }
    }

    public boolean stopBurstState() {
        boolean z = true;
        synchronized (this.mLock) {
            if (this.mState != 4) {
                z = false;
            } else {
                this.mBurstId++;
                reset();
                if (this.mBurstId <= 0) {
                    this.mBurstId = 1;
                }
            }
        }
        return z;
    }

    public int getState() {
        int i;
        synchronized (this.mLock) {
            i = this.mState;
        }
        return i;
    }

    public void reset() {
        synchronized (this.mLock) {
            CamLog.m3d(CameraConstants.TAG, " reset ");
            setState(0);
            this.mIsProcessingShot = false;
            this.mIsProcessingRawShot = false;
            this.mRequestShotCnt = 0;
            this.mShutterCallback = null;
            this.mCaptureListener = null;
        }
    }

    public void stopPreview() {
        this.mBurstId = 1;
        this.mBurstShotIdMap.clear();
    }

    public boolean isAvailableTimeToShot() {
        if (this.mState != 4 && System.currentTimeMillis() - this.mLastShotTime <= 80) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:35:?, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            return r0;
     */
    public boolean isAvailableShot(com.lge.camera.solution.SolutionPickResult r7) {
        /*
        r6 = this;
        r1 = 1;
        r0 = 0;
        r3 = r6.mLock;
        monitor-enter(r3);
        r2 = r6.mState;	 Catch:{ all -> 0x0031 }
        if (r2 == 0) goto L_0x000e;
    L_0x0009:
        r2 = r6.mState;	 Catch:{ all -> 0x0031 }
        r4 = 4;
        if (r2 != r4) goto L_0x0011;
    L_0x000e:
        monitor-exit(r3);	 Catch:{ all -> 0x0031 }
        r0 = r1;
    L_0x0010:
        return r0;
    L_0x0011:
        r2 = r6.mRequestShotCnt;	 Catch:{ all -> 0x0031 }
        if (r2 <= 0) goto L_0x0034;
    L_0x0015:
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0031 }
        r2.<init>();	 Catch:{ all -> 0x0031 }
        r4 = " drop because wait jpeg cb ";
        r2 = r2.append(r4);	 Catch:{ all -> 0x0031 }
        r4 = r6.mRequestShotCnt;	 Catch:{ all -> 0x0031 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0031 }
        r2 = r2.toString();	 Catch:{ all -> 0x0031 }
        com.lge.camera.util.CamLog.m3d(r1, r2);	 Catch:{ all -> 0x0031 }
        monitor-exit(r3);	 Catch:{ all -> 0x0031 }
        goto L_0x0010;
    L_0x0031:
        r0 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0031 }
        throw r0;
    L_0x0034:
        r2 = r6.mState;	 Catch:{ all -> 0x0031 }
        r4 = 3;
        if (r2 != r4) goto L_0x0069;
    L_0x0039:
        r4 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0031 }
        r2.<init>();	 Catch:{ all -> 0x0031 }
        r5 = " Fast shot to shot ";
        r5 = r2.append(r5);	 Catch:{ all -> 0x0031 }
        if (r7 != 0) goto L_0x0060;
    L_0x0048:
        r2 = "null";
    L_0x004a:
        r2 = r5.append(r2);	 Catch:{ all -> 0x0031 }
        r2 = r2.toString();	 Catch:{ all -> 0x0031 }
        com.lge.camera.util.CamLog.m7i(r4, r2);	 Catch:{ all -> 0x0031 }
        if (r7 == 0) goto L_0x005d;
    L_0x0057:
        r2 = r7.isSupportedFastShot();	 Catch:{ all -> 0x0031 }
        if (r2 == 0) goto L_0x005e;
    L_0x005d:
        r0 = r1;
    L_0x005e:
        monitor-exit(r3);	 Catch:{ all -> 0x0031 }
        goto L_0x0010;
    L_0x0060:
        r2 = r7.isSupportedFastShot();	 Catch:{ all -> 0x0031 }
        r2 = java.lang.Boolean.valueOf(r2);	 Catch:{ all -> 0x0031 }
        goto L_0x004a;
    L_0x0069:
        r2 = r6.mState;	 Catch:{ all -> 0x0031 }
        r4 = 2;
        if (r2 == r4) goto L_0x0074;
    L_0x006e:
        r2 = r6.mState;	 Catch:{ all -> 0x0031 }
        r4 = 5;
        if (r2 == r4) goto L_0x0074;
    L_0x0073:
        r0 = r1;
    L_0x0074:
        monitor-exit(r3);	 Catch:{ all -> 0x0031 }
        goto L_0x0010;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.SnapShotManager.isAvailableShot(com.lge.camera.solution.SolutionPickResult):boolean");
    }

    public boolean sendDropCallback(CameraErrorCallbackForward cb, SolutionPickResult solutionPicked) {
        boolean z = false;
        if (cb != null) {
            synchronized (this.mLock) {
                int dropReason = 99;
                if (this.mState == 4) {
                } else {
                    if (this.mState == 3 && solutionPicked != null && solutionPicked.isEnabledNightSolution()) {
                        dropReason = 97;
                    }
                    cb.onError(dropReason, null);
                    z = true;
                }
            }
        }
        return z;
    }

    public boolean sendShotStateCallback(CameraErrorCallbackForward cb, SolutionPickResult solutionPicked) {
        if (cb == null || solutionPicked == null) {
            return false;
        }
        if (solutionPicked.isEnabledNightSolution()) {
            cb.onError(96, null);
        }
        return true;
    }

    public boolean sendPictureCallback(byte[] capture) {
        boolean z;
        synchronized (this.mLock) {
            if (this.mCaptureListener == null) {
                z = false;
            } else {
                this.mCaptureListener.onCaptureAvailable(capture, getHiddenExif());
                z = true;
            }
        }
        return z;
    }

    public boolean sendOutFocusCapturCallback(OutfocusCaptureResult outfocusResult) {
        boolean z;
        synchronized (this.mLock) {
            if (this.mCaptureListener == null || this.mOutFocusCallback == null) {
                z = false;
            } else {
                this.mOutFocusCallback.onOutFocusCaptureResult(outfocusResult);
                z = true;
            }
        }
        return z;
    }

    public boolean sendPictureCallbackForDng(DngCreator dngCreator, ByteBuffer byteBuffer, Size size) {
        boolean z;
        synchronized (this.mLock) {
            if (this.mCaptureListener == null) {
                z = false;
            } else {
                this.mCaptureListener.onCaptureAvailable(dngCreator, byteBuffer, size);
                z = true;
            }
        }
        return z;
    }

    public boolean sendShutterCallback() {
        boolean z;
        synchronized (this.mLock) {
            if (this.mShutterCallback == null) {
                z = false;
            } else {
                this.mShutterCallback.onShutter();
                z = true;
            }
        }
        return z;
    }

    public void setCallback(ShutterCallbackForward shutterCallback, CaptureListener listener) {
        synchronized (this.mLock) {
            this.mShutterCallback = shutterCallback;
            this.mCaptureListener = listener;
        }
    }

    public void setOutFocusCallback(OutFocusCallback cb) {
        synchronized (this.mLock) {
            this.mOutFocusCallback = cb;
        }
    }

    public CaptureListener getCaptureCallback() {
        CaptureListener captureListener;
        synchronized (this.mLock) {
            captureListener = this.mCaptureListener;
        }
        return captureListener;
    }

    public boolean isRawCapture() {
        boolean z;
        synchronized (this.mLock) {
            if (this.mCaptureListener == null) {
                z = false;
            } else {
                z = this.mCaptureListener.isValidRawPictureCallback();
            }
        }
        return z;
    }

    public void addHiddenExif(byte[] data) {
        if (data == null) {
            data = this.mDummyExif;
        }
        try {
            this.mHiddenExifQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public byte[] getHiddenExif() {
        byte[] data = (byte[]) this.mHiddenExifQueue.poll();
        if (data == this.mDummyExif) {
            return null;
        }
        return data;
    }

    public void setBurstTag(Builder captureBuilder) {
        synchronized (this.mLock) {
            if (this.mState != 4) {
                return;
            }
            captureBuilder.setTag(Integer.valueOf(this.mBurstId));
        }
    }

    public boolean checkVaildRequest(CaptureRequest request, long timestamp) {
        Object tag = request.getTag();
        if (tag == null) {
            return true;
        }
        int burstId = ((Integer) tag).intValue();
        this.mBurstShotIdMap.put(Long.valueOf(timestamp), Integer.valueOf(burstId));
        if (burstId != this.mBurstId) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:8:0x0035, code:
            if (r0 == null) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:10:0x003d, code:
            if (r0.intValue() == r6.mBurstId) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:11:0x003f, code:
            com.lge.camera.util.CamLog.m7i(com.lge.camera.constants.CameraConstants.TAG, "Drop invalid burst shot old id " + r0 + " new id " + r6.mBurstId);
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            return true;
     */
    public boolean checkVaildImage(android.media.Image r7) {
        /*
        r6 = this;
        r1 = 0;
        r2 = r6.mBurstShotIdMap;
        r4 = r7.getTimestamp();
        r3 = java.lang.Long.valueOf(r4);
        r0 = r2.remove(r3);
        r0 = (java.lang.Integer) r0;
        r2 = r6.mLock;
        monitor-enter(r2);
        r3 = r6.mState;	 Catch:{ all -> 0x0064 }
        if (r3 != 0) goto L_0x0034;
    L_0x0018:
        r3 = "CameraApp";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0064 }
        r4.<init>();	 Catch:{ all -> 0x0064 }
        r5 = "Drop Idle State ";
        r4 = r4.append(r5);	 Catch:{ all -> 0x0064 }
        r5 = r6.mState;	 Catch:{ all -> 0x0064 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0064 }
        r4 = r4.toString();	 Catch:{ all -> 0x0064 }
        com.lge.camera.util.CamLog.m7i(r3, r4);	 Catch:{ all -> 0x0064 }
        monitor-exit(r2);	 Catch:{ all -> 0x0064 }
    L_0x0033:
        return r1;
    L_0x0034:
        monitor-exit(r2);	 Catch:{ all -> 0x0064 }
        if (r0 == 0) goto L_0x0067;
    L_0x0037:
        r2 = r0.intValue();
        r3 = r6.mBurstId;
        if (r2 == r3) goto L_0x0067;
    L_0x003f:
        r2 = "CameraApp";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Drop invalid burst shot old id ";
        r3 = r3.append(r4);
        r3 = r3.append(r0);
        r4 = " new id ";
        r3 = r3.append(r4);
        r4 = r6.mBurstId;
        r3 = r3.append(r4);
        r3 = r3.toString();
        com.lge.camera.util.CamLog.m7i(r2, r3);
        goto L_0x0033;
    L_0x0064:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0064 }
        throw r1;
    L_0x0067:
        r1 = 1;
        goto L_0x0033;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.SnapShotManager.checkVaildImage(android.media.Image):boolean");
    }

    public boolean isBurstShot(long timestamp) {
        Integer burstId = (Integer) this.mBurstShotIdMap.get(Long.valueOf(timestamp));
        CamLog.m7i(CameraConstants.TAG, "burstId  " + burstId);
        return burstId != null;
    }

    /* JADX WARNING: Missing block: B:17:?, code:
            return;
     */
    public void startRequestShot() {
        /*
        r3 = this;
        r1 = r3.mLock;
        monitor-enter(r1);
        r0 = r3.mState;	 Catch:{ all -> 0x001c }
        if (r0 == 0) goto L_0x000c;
    L_0x0007:
        r0 = r3.mState;	 Catch:{ all -> 0x001c }
        r2 = 4;
        if (r0 != r2) goto L_0x000e;
    L_0x000c:
        monitor-exit(r1);	 Catch:{ all -> 0x001c }
    L_0x000d:
        return;
    L_0x000e:
        r2 = r3.mRequestShotCnt;	 Catch:{ all -> 0x001c }
        r0 = r3.isRawCapture();	 Catch:{ all -> 0x001c }
        if (r0 == 0) goto L_0x001f;
    L_0x0016:
        r0 = 2;
    L_0x0017:
        r0 = r0 + r2;
        r3.mRequestShotCnt = r0;	 Catch:{ all -> 0x001c }
        monitor-exit(r1);	 Catch:{ all -> 0x001c }
        goto L_0x000d;
    L_0x001c:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x001c }
        throw r0;
    L_0x001f:
        r0 = 1;
        goto L_0x0017;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.SnapShotManager.startRequestShot():void");
    }

    /* JADX WARNING: Missing block: B:14:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            return;
     */
    public void endRequestShot(boolean r3) {
        /*
        r2 = this;
        r1 = r2.mLock;
        monitor-enter(r1);
        r0 = r2.mRequestShotCnt;	 Catch:{ all -> 0x001a }
        if (r0 <= 0) goto L_0x0009;
    L_0x0007:
        if (r3 == 0) goto L_0x000b;
    L_0x0009:
        monitor-exit(r1);	 Catch:{ all -> 0x001a }
    L_0x000a:
        return;
    L_0x000b:
        r0 = r2.mRequestShotCnt;	 Catch:{ all -> 0x001a }
        r0 = r0 + -1;
        r2.mRequestShotCnt = r0;	 Catch:{ all -> 0x001a }
        r0 = r2.mRequestShotCnt;	 Catch:{ all -> 0x001a }
        if (r0 != 0) goto L_0x0018;
    L_0x0015:
        r2.reset();	 Catch:{ all -> 0x001a }
    L_0x0018:
        monitor-exit(r1);	 Catch:{ all -> 0x001a }
        goto L_0x000a;
    L_0x001a:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x001a }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.SnapShotManager.endRequestShot(boolean):void");
    }

    public void setProcessingShot() {
        synchronized (this.mLock) {
            this.mIsProcessingShot = true;
        }
    }

    /* JADX WARNING: Missing block: B:31:?, code:
            return;
     */
    public void waitShot() {
        /*
        r4 = this;
        r1 = r4.mLock;
        monitor-enter(r1);
        r0 = r4.mIsProcessingShot;	 Catch:{ all -> 0x0032 }
        if (r0 == 0) goto L_0x000b;
    L_0x0007:
        r0 = r4.mShotLock;	 Catch:{ all -> 0x0032 }
        if (r0 == 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
    L_0x000c:
        return;
    L_0x000d:
        r0 = new android.os.ConditionVariable;	 Catch:{ all -> 0x0032 }
        r0.<init>();	 Catch:{ all -> 0x0032 }
        r4.mShotLock = r0;	 Catch:{ all -> 0x0032 }
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
        r0 = "CameraApp";
        r1 = "Wait Image processiong - block";
        com.lge.camera.util.CamLog.m7i(r0, r1);
        r0 = r4.mShotLock;
        r2 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        r0.block(r2);
        r1 = r4.mLock;
        monitor-enter(r1);
        r0 = 0;
        r4.mShotLock = r0;	 Catch:{ all -> 0x0035 }
        monitor-exit(r1);	 Catch:{ all -> 0x0035 }
        r0 = "CameraApp";
        r1 = "Wait Image processiong - done ";
        com.lge.camera.util.CamLog.m7i(r0, r1);
        goto L_0x000c;
    L_0x0032:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
        throw r0;
    L_0x0035:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0035 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.SnapShotManager.waitShot():void");
    }

    /* JADX WARNING: Missing block: B:22:?, code:
            return;
     */
    public void doneShot(boolean r5) {
        /*
        r4 = this;
        r1 = r4.mLock;
        monitor-enter(r1);
        r0 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x003a }
        r2.<init>();	 Catch:{ all -> 0x003a }
        r3 = "doneShot ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x003a }
        r3 = r4.mIsProcessingShot;	 Catch:{ all -> 0x003a }
        r2 = r2.append(r3);	 Catch:{ all -> 0x003a }
        r2 = r2.toString();	 Catch:{ all -> 0x003a }
        com.lge.camera.util.CamLog.m7i(r0, r2);	 Catch:{ all -> 0x003a }
        r0 = r4.mIsProcessingShot;	 Catch:{ all -> 0x003d }
        if (r0 == 0) goto L_0x0025;
    L_0x0021:
        r0 = r4.mShotLock;	 Catch:{ all -> 0x003d }
        if (r0 != 0) goto L_0x002d;
    L_0x0025:
        r0 = 0;
        r4.mIsProcessingShot = r0;	 Catch:{ all -> 0x003d }
        r4.endRequestShot(r5);	 Catch:{ all -> 0x003a }
        monitor-exit(r1);	 Catch:{ all -> 0x003a }
    L_0x002c:
        return;
    L_0x002d:
        r0 = 0;
        r4.mIsProcessingShot = r0;	 Catch:{ all -> 0x003d }
        r0 = r4.mShotLock;	 Catch:{ all -> 0x003d }
        r0.open();	 Catch:{ all -> 0x003d }
        r4.endRequestShot(r5);	 Catch:{ all -> 0x003a }
        monitor-exit(r1);	 Catch:{ all -> 0x003a }
        goto L_0x002c;
    L_0x003a:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x003a }
        throw r0;
    L_0x003d:
        r0 = move-exception;
        r4.endRequestShot(r5);	 Catch:{ all -> 0x003a }
        throw r0;	 Catch:{ all -> 0x003a }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.SnapShotManager.doneShot(boolean):void");
    }

    public void setProcessingRawShot() {
        synchronized (this.mLock) {
            this.mIsProcessingRawShot = true;
        }
    }

    /* JADX WARNING: Missing block: B:31:?, code:
            return;
     */
    public void waitRawShot() {
        /*
        r4 = this;
        r1 = r4.mLock;
        monitor-enter(r1);
        r0 = r4.mIsProcessingRawShot;	 Catch:{ all -> 0x0032 }
        if (r0 == 0) goto L_0x000b;
    L_0x0007:
        r0 = r4.mRawShotLock;	 Catch:{ all -> 0x0032 }
        if (r0 == 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
    L_0x000c:
        return;
    L_0x000d:
        r0 = new android.os.ConditionVariable;	 Catch:{ all -> 0x0032 }
        r0.<init>();	 Catch:{ all -> 0x0032 }
        r4.mRawShotLock = r0;	 Catch:{ all -> 0x0032 }
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
        r0 = "CameraApp";
        r1 = "Wait Raw Image processiong - block";
        com.lge.camera.util.CamLog.m7i(r0, r1);
        r0 = r4.mRawShotLock;
        r2 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        r0.block(r2);
        r1 = r4.mLock;
        monitor-enter(r1);
        r0 = 0;
        r4.mRawShotLock = r0;	 Catch:{ all -> 0x0035 }
        monitor-exit(r1);	 Catch:{ all -> 0x0035 }
        r0 = "CameraApp";
        r1 = "Wait Raw Image processiong - done ";
        com.lge.camera.util.CamLog.m7i(r0, r1);
        goto L_0x000c;
    L_0x0032:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0032 }
        throw r0;
    L_0x0035:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0035 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.SnapShotManager.waitRawShot():void");
    }

    public void doneRawShot() {
        synchronized (this.mLock) {
            CamLog.m7i(CameraConstants.TAG, "doneRawShot " + this.mIsProcessingRawShot);
            try {
                if (!this.mIsProcessingRawShot || this.mRawShotLock == null) {
                    this.mIsProcessingRawShot = false;
                    endRequestShot(false);
                    return;
                }
                this.mIsProcessingRawShot = false;
                this.mRawShotLock.open();
                endRequestShot(false);
            } catch (Throwable th) {
                endRequestShot(false);
            }
        }
    }
}
