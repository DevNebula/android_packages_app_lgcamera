package com.lge.camera.device;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback;
import com.lge.camera.util.CamLog;

public class CameraHolder {
    private static final int KEEP_CAMERA_TIMEOUT = 3000;
    private static final int RELEASE_CAMERA = 1;
    protected static CameraHolder sHolder;
    private static CameraProxy[] sMockCamera;
    private static CameraInfo[] sMockCameraInfo;
    protected CameraProxy mCameraDevice;
    private int mCameraId = -1;
    protected CameraManager mCameraManager;
    private boolean mCameraOpened;
    private Handler mHandler;
    private final CameraInfo[] mInfo;
    private long mKeepBeforeTime;
    private final int mNumberOfCameras;
    private boolean mSkipNextReconnection = false;

    private class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (CameraHolder.this) {
                        if (!CameraHolder.this.mCameraOpened) {
                            CameraHolder.this.release();
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static synchronized CameraHolder instance() {
        CameraHolder cameraHolder;
        synchronized (CameraHolder.class) {
            if (sHolder == null) {
                sHolder = new CameraHolder();
            }
            cameraHolder = sHolder;
        }
        return cameraHolder;
    }

    public static void injectMockCamera(CameraInfo[] info, CameraProxy[] camera) {
        sMockCameraInfo = info;
        sMockCamera = camera;
        sHolder = new CameraHolder();
    }

    public CameraHolder() {
        HandlerThread ht = new HandlerThread("CameraHolder");
        ht.start();
        this.mHandler = new MyHandler(ht.getLooper());
        if (sMockCameraInfo != null) {
            this.mNumberOfCameras = sMockCameraInfo.length;
            this.mInfo = sMockCameraInfo;
        } else {
            this.mNumberOfCameras = Camera.getNumberOfCameras();
            this.mInfo = new CameraInfo[this.mNumberOfCameras];
            for (int i = 0; i < this.mNumberOfCameras; i++) {
                this.mInfo[i] = new CameraInfo();
                Camera.getCameraInfo(i, this.mInfo[i]);
            }
        }
        CameraDeviceUtils.sNumbersOfCamera = this.mNumberOfCameras;
        CamLog.m3d(CameraConstants.TAG, "mNumberOfCameras : " + this.mNumberOfCameras);
    }

    public int getNumberOfCameras() {
        return this.mNumberOfCameras;
    }

    public CameraInfo[] getCameraInfo() {
        return this.mInfo;
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0122  */
    public synchronized com.lge.camera.device.CameraManager.CameraProxy open(android.os.Handler r7, int r8, com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback r9, boolean r10, android.content.Context r11) {
        /*
        r6 = this;
        r0 = 0;
        monitor-enter(r6);
        r1 = r6.mCameraDevice;	 Catch:{ all -> 0x0109 }
        if (r1 == 0) goto L_0x0016;
    L_0x0006:
        r1 = r6.mCameraId;	 Catch:{ all -> 0x0109 }
        if (r1 == r8) goto L_0x0016;
    L_0x000a:
        r1 = r6.mCameraDevice;	 Catch:{ all -> 0x0109 }
        r2 = 0;
        r1.release(r2);	 Catch:{ all -> 0x0109 }
        r1 = 0;
        r6.mCameraDevice = r1;	 Catch:{ all -> 0x0109 }
        r1 = -1;
        r6.mCameraId = r1;	 Catch:{ all -> 0x0109 }
    L_0x0016:
        r1 = r6.mCameraManager;	 Catch:{ all -> 0x0109 }
        if (r1 != 0) goto L_0x0020;
    L_0x001a:
        r1 = com.lge.camera.device.CameraManagerFactory.getAndroidCameraManager(r11);	 Catch:{ all -> 0x0109 }
        r6.mCameraManager = r1;	 Catch:{ all -> 0x0109 }
    L_0x0020:
        r1 = r6.mCameraDevice;	 Catch:{ all -> 0x0109 }
        if (r1 != 0) goto L_0x0136;
    L_0x0024:
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0109 }
        r2.<init>();	 Catch:{ all -> 0x0109 }
        r3 = "open camera ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.append(r8);	 Catch:{ all -> 0x0109 }
        r2 = r2.toString();	 Catch:{ all -> 0x0109 }
        com.lge.camera.util.CamLog.m9v(r1, r2);	 Catch:{ all -> 0x0109 }
        r1 = sMockCameraInfo;	 Catch:{ all -> 0x0109 }
        if (r1 != 0) goto L_0x00fe;
    L_0x0040:
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0109 }
        r2.<init>();	 Catch:{ all -> 0x0109 }
        r3 = "[Time Info][2] Activity End : UI Initialization ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = 1;
        r4 = com.lge.camera.util.DebugUtil.interimCheckTime(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0109 }
        r3 = " ms";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.toString();	 Catch:{ all -> 0x0109 }
        android.util.Log.i(r1, r2);	 Catch:{ all -> 0x0109 }
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0109 }
        r2.<init>();	 Catch:{ all -> 0x0109 }
        r3 = "[Time Info][3] Device Open Start : Driver Initialization ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = 0;
        r4 = com.lge.camera.util.DebugUtil.interimCheckTime(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0109 }
        r2 = r2.toString();	 Catch:{ all -> 0x0109 }
        android.util.Log.i(r1, r2);	 Catch:{ all -> 0x0109 }
        r1 = com.lge.camera.util.CamLog.TraceTag.OPTIONAL;	 Catch:{ all -> 0x0109 }
        r2 = "DeviceOpen";
        r3 = 2;
        com.lge.camera.util.CamLog.traceBegin(r1, r2, r3);	 Catch:{ all -> 0x0109 }
        r1 = r6.mCameraManager;	 Catch:{ all -> 0x0109 }
        r1 = r1.cameraOpen(r7, r8, r9);	 Catch:{ all -> 0x0109 }
        r6.mCameraDevice = r1;	 Catch:{ all -> 0x0109 }
        r1 = com.lge.camera.util.CamLog.TraceTag.OPTIONAL;	 Catch:{ all -> 0x0109 }
        r2 = "DeviceOpen";
        r3 = 2;
        com.lge.camera.util.CamLog.traceEnd(r1, r2, r3);	 Catch:{ all -> 0x0109 }
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0109 }
        r2.<init>();	 Catch:{ all -> 0x0109 }
        r3 = "[Time Info][3] Device Open End : Driver Initialization ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = 1;
        r4 = com.lge.camera.util.DebugUtil.interimCheckTime(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0109 }
        r3 = " ms";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.toString();	 Catch:{ all -> 0x0109 }
        android.util.Log.i(r1, r2);	 Catch:{ all -> 0x0109 }
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0109 }
        r2.<init>();	 Catch:{ all -> 0x0109 }
        r3 = "[Time Info][4] App Param setting Start : Parameter setting ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = 0;
        r4 = com.lge.camera.util.DebugUtil.interimCheckTime(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.append(r4);	 Catch:{ all -> 0x0109 }
        r2 = r2.toString();	 Catch:{ all -> 0x0109 }
        android.util.Log.i(r1, r2);	 Catch:{ all -> 0x0109 }
    L_0x00d8:
        r1 = r6.mCameraDevice;	 Catch:{ all -> 0x0109 }
        if (r1 != 0) goto L_0x0117;
    L_0x00dc:
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0109 }
        r2.<init>();	 Catch:{ all -> 0x0109 }
        r3 = "fail to connect Camera:";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = r6.mCameraId;	 Catch:{ all -> 0x0109 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = ", aborting.";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.toString();	 Catch:{ all -> 0x0109 }
        com.lge.camera.util.CamLog.m5e(r1, r2);	 Catch:{ all -> 0x0109 }
    L_0x00fc:
        monitor-exit(r6);
        return r0;
    L_0x00fe:
        r1 = sMockCamera;	 Catch:{ all -> 0x0109 }
        if (r1 == 0) goto L_0x010c;
    L_0x0102:
        r1 = sMockCamera;	 Catch:{ all -> 0x0109 }
        r1 = r1[r8];	 Catch:{ all -> 0x0109 }
        r6.mCameraDevice = r1;	 Catch:{ all -> 0x0109 }
        goto L_0x00d8;
    L_0x0109:
        r0 = move-exception;
        monitor-exit(r6);
        throw r0;
    L_0x010c:
        r1 = "CameraApp";
        r2 = "MockCameraInfo found, but no MockCamera provided.";
        com.lge.camera.util.CamLog.m5e(r1, r2);	 Catch:{ all -> 0x0109 }
        r1 = 0;
        r6.mCameraDevice = r1;	 Catch:{ all -> 0x0109 }
        goto L_0x00d8;
    L_0x0117:
        r6.mCameraId = r8;	 Catch:{ all -> 0x0109 }
        r6.mSkipNextReconnection = r10;	 Catch:{ all -> 0x0109 }
    L_0x011b:
        r0 = 1;
        r6.mCameraOpened = r0;	 Catch:{ all -> 0x0109 }
        r0 = r6.mHandler;	 Catch:{ all -> 0x0109 }
        if (r0 == 0) goto L_0x0128;
    L_0x0122:
        r0 = r6.mHandler;	 Catch:{ all -> 0x0109 }
        r1 = 1;
        r0.removeMessages(r1);	 Catch:{ all -> 0x0109 }
    L_0x0128:
        r0 = 0;
        r6.mKeepBeforeTime = r0;	 Catch:{ all -> 0x0109 }
        r0 = "CameraApp";
        r1 = "open camera done";
        com.lge.camera.util.CamLog.m9v(r0, r1);	 Catch:{ all -> 0x0109 }
        r0 = r6.mCameraDevice;	 Catch:{ all -> 0x0109 }
        goto L_0x00fc;
    L_0x0136:
        r1 = r6.mSkipNextReconnection;	 Catch:{ all -> 0x0109 }
        if (r1 != 0) goto L_0x0163;
    L_0x013a:
        r1 = r6.mCameraDevice;	 Catch:{ all -> 0x0109 }
        r1 = r1.reconnect(r7, r9);	 Catch:{ all -> 0x0109 }
        if (r1 != 0) goto L_0x011b;
    L_0x0142:
        r1 = "CameraApp";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0109 }
        r2.<init>();	 Catch:{ all -> 0x0109 }
        r3 = "fail to reconnect Camera:";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = r6.mCameraId;	 Catch:{ all -> 0x0109 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r3 = ", aborting.";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0109 }
        r2 = r2.toString();	 Catch:{ all -> 0x0109 }
        com.lge.camera.util.CamLog.m5e(r1, r2);	 Catch:{ all -> 0x0109 }
        goto L_0x00fc;
    L_0x0163:
        r0 = "CameraApp";
        r1 = "skip reconnect Camera";
        com.lge.camera.util.CamLog.m5e(r0, r1);	 Catch:{ all -> 0x0109 }
        r0 = 0;
        r6.mSkipNextReconnection = r0;	 Catch:{ all -> 0x0109 }
        goto L_0x011b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.CameraHolder.open(android.os.Handler, int, com.lge.camera.device.ICameraCallback$CameraOpenErrorCallback, boolean, android.content.Context):com.lge.camera.device.CameraManager$CameraProxy");
    }

    public synchronized CameraProxy tryOpen(Handler handler, int cameraId, CameraOpenErrorCallback cb, Context context) {
        return !this.mCameraOpened ? open(handler, cameraId, cb, false, context) : null;
    }

    public synchronized void release() {
        if (releaseCommon()) {
            CameraManagerFactory.releaseAndroidCameraManager();
            sHolder = null;
            sMockCamera = null;
            sMockCameraInfo = null;
        }
    }

    public synchronized void strongRelease() {
        if (this.mCameraDevice != null) {
            this.mCameraOpened = false;
            this.mCameraDevice.release(true);
            this.mCameraDevice = null;
            this.mCameraId = -1;
        }
    }

    protected boolean releaseCommon() {
        if (this.mCameraDevice == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < this.mKeepBeforeTime) {
            if (this.mCameraOpened) {
                this.mCameraOpened = false;
            }
            this.mHandler.sendEmptyMessageDelayed(1, this.mKeepBeforeTime - now);
            return false;
        }
        strongRelease();
        this.mHandler.removeCallbacksAndMessages(null);
        this.mHandler.getLooper().quit();
        this.mHandler = null;
        return true;
    }

    public void keep() {
        keep(3000);
    }

    public synchronized void keep(int time) {
        this.mKeepBeforeTime = System.currentTimeMillis() + ((long) time);
    }

    public boolean isCameraOpened() {
        return this.mCameraOpened;
    }
}
