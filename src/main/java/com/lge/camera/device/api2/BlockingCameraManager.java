package com.lge.camera.device.api2;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.p000v4.view.PointerIconCompat;
import android.util.Log;
import android.view.Surface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CamLog.TraceTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockingCameraManager {
    private static final int OPEN_TIME_OUT = 2000;
    private static final int SESSION_TIME_OUT = 3000;
    private static final String TAG = "CameraAppBlockingOpener";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private final CameraManager mManager;

    public static class BlockingOpenException extends Exception {
        public static final int ERROR_DISCONNECTED = 0;
        private static final long serialVersionUID = 12397123891238912L;
        private final int mError;

        public BlockingOpenException(int errorCode, String message) {
            super(message);
            this.mError = errorCode;
        }

        public boolean wasDisconnected() {
            return this.mError == 0;
        }

        public boolean wasError() {
            return this.mError != 0;
        }

        public int getCode() {
            return this.mError;
        }
    }

    private class OpenListener extends StateCallback {
        private static final int ERROR_UNINITIALIZED = -1;
        private final String mCameraId;
        private CameraDevice mDevice = null;
        private final ConditionVariable mDeviceReady = new ConditionVariable();
        private boolean mDisconnected = false;
        private int mError = -1;
        private final Object mLock = new Object();
        private boolean mNoReply = true;
        private final StateCallback mProxy;
        private boolean mSuccess = false;
        private boolean mTimedOut = false;

        OpenListener(CameraManager manager, String cameraId, StateCallback callback, Handler handler) throws CameraAccessException {
            this.mCameraId = cameraId;
            this.mProxy = callback;
            Log.v(BlockingCameraManager.TAG, "### OpenListener creation");
            CamLog.traceBegin(TraceTag.OPTIONAL, "HAL3_openCamera", 1001);
            manager.openCamera(cameraId, this, handler);
        }

        private void assertInitialState() {
            BlockingCameraManager.assertEquals(null, this.mDevice);
            BlockingCameraManager.assertEquals(Boolean.valueOf(false), Boolean.valueOf(this.mDisconnected));
            BlockingCameraManager.assertEquals(Integer.valueOf(-1), Integer.valueOf(this.mError));
            BlockingCameraManager.assertEquals(Boolean.valueOf(false), Boolean.valueOf(this.mSuccess));
        }

        /* JADX WARNING: Missing block: B:18:0x0051, code:
            if (r3.mProxy == null) goto L_?;
     */
        /* JADX WARNING: Missing block: B:19:0x0053, code:
            r3.mProxy.onOpened(r4);
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:29:?, code:
            return;
     */
        public void onOpened(android.hardware.camera2.CameraDevice r4) {
            /*
            r3 = this;
            r0 = com.lge.camera.util.CamLog.TraceTag.OPTIONAL;
            r1 = "HAL3_openCamera";
            r2 = 1001; // 0x3e9 float:1.403E-42 double:4.946E-321;
            com.lge.camera.util.CamLog.traceEnd(r0, r1, r2);
            r0 = com.lge.camera.device.api2.BlockingCameraManager.VERBOSE;
            if (r0 == 0) goto L_0x002d;
        L_0x000f:
            r1 = "CameraAppBlockingOpener";
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r2 = "onOpened: camera ";
            r2 = r0.append(r2);
            if (r4 == 0) goto L_0x004b;
        L_0x001e:
            r0 = r4.getId();
        L_0x0022:
            r0 = r2.append(r0);
            r0 = r0.toString();
            android.util.Log.v(r1, r0);
        L_0x002d:
            r1 = r3.mLock;
            monitor-enter(r1);
            r3.assertInitialState();	 Catch:{ all -> 0x0059 }
            r0 = 0;
            r3.mNoReply = r0;	 Catch:{ all -> 0x0059 }
            r0 = 1;
            r3.mSuccess = r0;	 Catch:{ all -> 0x0059 }
            r3.mDevice = r4;	 Catch:{ all -> 0x0059 }
            r0 = r3.mDeviceReady;	 Catch:{ all -> 0x0059 }
            r0.open();	 Catch:{ all -> 0x0059 }
            r0 = r3.mTimedOut;	 Catch:{ all -> 0x0059 }
            if (r0 == 0) goto L_0x004e;
        L_0x0044:
            if (r4 == 0) goto L_0x004e;
        L_0x0046:
            r4.close();	 Catch:{ all -> 0x0059 }
            monitor-exit(r1);	 Catch:{ all -> 0x0059 }
        L_0x004a:
            return;
        L_0x004b:
            r0 = "null";
            goto L_0x0022;
        L_0x004e:
            monitor-exit(r1);	 Catch:{ all -> 0x0059 }
            r0 = r3.mProxy;
            if (r0 == 0) goto L_0x004a;
        L_0x0053:
            r0 = r3.mProxy;
            r0.onOpened(r4);
            goto L_0x004a;
        L_0x0059:
            r0 = move-exception;
            monitor-exit(r1);	 Catch:{ all -> 0x0059 }
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.BlockingCameraManager.OpenListener.onOpened(android.hardware.camera2.CameraDevice):void");
        }

        /* JADX WARNING: Missing block: B:16:0x003f, code:
            if (r3.mProxy == null) goto L_?;
     */
        /* JADX WARNING: Missing block: B:17:0x0041, code:
            r3.mProxy.onDisconnected(r4);
     */
        /* JADX WARNING: Missing block: B:26:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:27:?, code:
            return;
     */
        public void onDisconnected(android.hardware.camera2.CameraDevice r4) {
            /*
            r3 = this;
            r1 = "CameraAppBlockingOpener";
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r2 = "onDisconnected: camera ";
            r2 = r0.append(r2);
            if (r4 == 0) goto L_0x0039;
        L_0x000f:
            r0 = r4.getId();
        L_0x0013:
            r0 = r2.append(r0);
            r0 = r0.toString();
            android.util.Log.e(r1, r0);
            r1 = r3.mLock;
            monitor-enter(r1);
            r0 = 0;
            r3.mNoReply = r0;	 Catch:{ all -> 0x0047 }
            r0 = 1;
            r3.mDisconnected = r0;	 Catch:{ all -> 0x0047 }
            r3.mDevice = r4;	 Catch:{ all -> 0x0047 }
            r0 = r3.mDeviceReady;	 Catch:{ all -> 0x0047 }
            r0.open();	 Catch:{ all -> 0x0047 }
            r0 = r3.mTimedOut;	 Catch:{ all -> 0x0047 }
            if (r0 == 0) goto L_0x003c;
        L_0x0032:
            if (r4 == 0) goto L_0x003c;
        L_0x0034:
            r4.close();	 Catch:{ all -> 0x0047 }
            monitor-exit(r1);	 Catch:{ all -> 0x0047 }
        L_0x0038:
            return;
        L_0x0039:
            r0 = "null";
            goto L_0x0013;
        L_0x003c:
            monitor-exit(r1);	 Catch:{ all -> 0x0047 }
            r0 = r3.mProxy;
            if (r0 == 0) goto L_0x0038;
        L_0x0041:
            r0 = r3.mProxy;
            r0.onDisconnected(r4);
            goto L_0x0038;
        L_0x0047:
            r0 = move-exception;
            monitor-exit(r1);	 Catch:{ all -> 0x0047 }
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.BlockingCameraManager.OpenListener.onDisconnected(android.hardware.camera2.CameraDevice):void");
        }

        /* JADX WARNING: Missing block: B:19:0x0052, code:
            if (r3.mProxy == null) goto L_?;
     */
        /* JADX WARNING: Missing block: B:20:0x0054, code:
            r3.mProxy.onError(r4, r5);
     */
        /* JADX WARNING: Missing block: B:29:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:30:?, code:
            return;
     */
        public void onError(android.hardware.camera2.CameraDevice r4, int r5) {
            /*
            r3 = this;
            r1 = "CameraAppBlockingOpener";
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r2 = "onError: camera ";
            r2 = r0.append(r2);
            if (r4 == 0) goto L_0x0032;
        L_0x000f:
            r0 = r4.getId();
        L_0x0013:
            r0 = r2.append(r0);
            r2 = " error : ";
            r0 = r0.append(r2);
            r0 = r0.append(r5);
            r0 = r0.toString();
            android.util.Log.e(r1, r0);
            if (r5 > 0) goto L_0x0035;
        L_0x002a:
            r0 = new java.lang.AssertionError;
            r1 = "Expected error to be a positive number";
            r0.<init>(r1);
            throw r0;
        L_0x0032:
            r0 = "null";
            goto L_0x0013;
        L_0x0035:
            r1 = r3.mLock;
            monitor-enter(r1);
            r0 = 0;
            r3.mNoReply = r0;	 Catch:{ all -> 0x005a }
            r3.mError = r5;	 Catch:{ all -> 0x005a }
            r3.mDevice = r4;	 Catch:{ all -> 0x005a }
            r0 = r3.mDeviceReady;	 Catch:{ all -> 0x005a }
            r0.open();	 Catch:{ all -> 0x005a }
            r0 = r3.mTimedOut;	 Catch:{ all -> 0x005a }
            if (r0 == 0) goto L_0x004f;
        L_0x0048:
            if (r4 == 0) goto L_0x004f;
        L_0x004a:
            r4.close();	 Catch:{ all -> 0x005a }
            monitor-exit(r1);	 Catch:{ all -> 0x005a }
        L_0x004e:
            return;
        L_0x004f:
            monitor-exit(r1);	 Catch:{ all -> 0x005a }
            r0 = r3.mProxy;
            if (r0 == 0) goto L_0x004e;
        L_0x0054:
            r0 = r3.mProxy;
            r0.onError(r4, r5);
            goto L_0x004e;
        L_0x005a:
            r0 = move-exception;
            monitor-exit(r1);	 Catch:{ all -> 0x005a }
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.BlockingCameraManager.OpenListener.onError(android.hardware.camera2.CameraDevice, int):void");
        }

        public void onClosed(CameraDevice camera) {
            if (this.mProxy != null) {
                this.mProxy.onClosed(camera);
            }
        }

        CameraDevice blockUntilOpen() throws BlockingOpenException {
            CameraDevice cameraDevice;
            if (!this.mDeviceReady.block(CameraConstants.TOAST_LENGTH_SHORT)) {
                synchronized (this.mLock) {
                    if (this.mNoReply) {
                        this.mTimedOut = true;
                        throw new TimeoutRuntimeException(String.format("Timed out after %d ms while trying to open camera device %s", new Object[]{Integer.valueOf(2000), this.mCameraId}));
                    }
                }
            }
            synchronized (this.mLock) {
                if (!(this.mSuccess || this.mDevice == null)) {
                    this.mDevice.close();
                }
                if (this.mSuccess) {
                    cameraDevice = this.mDevice;
                } else if (this.mDisconnected) {
                    throw new BlockingOpenException(0, "Failed to open camera device: it is disconnected");
                } else if (this.mError != -1) {
                    throw new BlockingOpenException(this.mError, "Failed to open camera device: error code " + this.mError);
                } else {
                    throw new AssertionError("Failed to open camera device (impl bug)");
                }
            }
            return cameraDevice;
        }
    }

    private class SessionConfigListener {
        private final ConditionVariable mDeviceReady = new ConditionVariable();
        private final Object mLock = new Object();
        private int mOperationType = 0;
        private CameraCaptureSession mSession = null;
        private boolean mSuccess = false;

        public SessionConfigListener(Boolean isReprocess, CameraDevice camera, InputConfiguration inputConfig, ArrayList<?> outputList, Handler handler, int operationType) throws CameraAccessException {
            BlockingCameraManager.assertNotEquals(null, camera);
            BlockingCameraManager.assertNotEquals(null, outputList);
            BlockingCameraManager.assertNotEquals(null, outputList.get(0));
            BlockingCameraManager.assertNotEquals(null, handler);
            this.mSuccess = false;
            this.mOperationType = operationType;
            List<Surface> surfaceList = null;
            List<OutputConfiguration> outputConfigList = null;
            if (outputList.get(0) instanceof Surface) {
                surfaceList = outputList;
            } else if (outputList.get(0) instanceof OutputConfiguration) {
                Object outputConfigList2 = outputList;
            } else {
                throw new AssertionError("output Surface Error");
            }
            CameraCaptureSession.StateCallback cb = getSessionCallback("main");
            if (this.mOperationType != 0) {
                camera.createCustomCaptureSession(inputConfig, getOutputConfigurationList(surfaceList), this.mOperationType, cb, handler);
                return;
            }
            CamLog.m7i(BlockingCameraManager.TAG, " Create Session reproc : " + isReprocess + " IsConfigList " + (outputConfigList2 != null));
            if (isReprocess.booleanValue()) {
                BlockingCameraManager.assertNotEquals(null, inputConfig);
                CamLog.traceBegin(TraceTag.OPTIONAL, "createReprocessableCaptureSession", PointerIconCompat.TYPE_HAND);
                if (outputConfigList2 != null) {
                    camera.createReprocessableCaptureSessionByConfigurations(inputConfig, outputConfigList2, cb, handler);
                    return;
                } else {
                    camera.createReprocessableCaptureSession(inputConfig, surfaceList, cb, handler);
                    return;
                }
            }
            CamLog.traceBegin(TraceTag.OPTIONAL, "createCaptureSession", PointerIconCompat.TYPE_HAND);
            if (outputConfigList2 != null) {
                CamLog.m7i(BlockingCameraManager.TAG, "createCaptureSessionByOutputConfigurations");
                camera.createCaptureSessionByOutputConfigurations(outputConfigList2, cb, handler);
                return;
            }
            camera.createCaptureSession(surfaceList, cb, handler);
        }

        public SessionConfigListener(Boolean isHighSpeedRecording, CameraDevice camera, ArrayList<?> outputList, Handler handler, int operationType) throws CameraAccessException {
            BlockingCameraManager.assertNotEquals(null, camera);
            BlockingCameraManager.assertNotEquals(null, outputList);
            BlockingCameraManager.assertNotEquals(null, outputList.get(0));
            BlockingCameraManager.assertNotEquals(null, handler);
            this.mSuccess = false;
            this.mOperationType = operationType;
            List<Surface> surfaceList = null;
            List<OutputConfiguration> outputConfigList = null;
            if (outputList.get(0) instanceof Surface) {
                surfaceList = outputList;
            } else if (outputList.get(0) instanceof OutputConfiguration) {
                Object outputConfigList2 = outputList;
            } else {
                throw new AssertionError("output Surface Error");
            }
            CameraCaptureSession.StateCallback cb = getSessionCallback("Recording");
            if (this.mOperationType != 0) {
                camera.createCustomCaptureSession(null, getOutputConfigurationList(surfaceList), this.mOperationType, cb, handler);
            } else if (isHighSpeedRecording.booleanValue()) {
                CamLog.traceBegin(TraceTag.OPTIONAL, "createConstrainedHighSpeedCaptureSession", PointerIconCompat.TYPE_HAND);
                if (surfaceList != null) {
                    camera.createConstrainedHighSpeedCaptureSession(surfaceList, cb, handler);
                    return;
                }
                throw new AssertionError("Not supported OutputConfiguration in HighSpeedCaptureSession");
            } else {
                CamLog.traceBegin(TraceTag.OPTIONAL, "createCaptureSession", PointerIconCompat.TYPE_HAND);
                if (outputConfigList2 != null) {
                    camera.createCaptureSessionByOutputConfigurations(outputConfigList2, cb, handler);
                } else {
                    camera.createCaptureSession(surfaceList, cb, handler);
                }
            }
        }

        CameraCaptureSession blockUntilCreateSession() {
            CameraCaptureSession cameraCaptureSession = null;
            if (this.mDeviceReady.block(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT)) {
                synchronized (this.mLock) {
                    if (!this.mSuccess && this.mSession != null) {
                    } else if (this.mSuccess) {
                        cameraCaptureSession = this.mSession;
                    }
                }
            }
            return cameraCaptureSession;
        }

        CameraCaptureSession.StateCallback getSessionCallback(final String name) {
            return new CameraCaptureSession.StateCallback() {
                public void onConfigured(CameraCaptureSession session) {
                    CamLog.m3d(BlockingCameraManager.TAG, "onConfigured: " + name + " session is ready to operate");
                    CamLog.traceEnd(TraceTag.OPTIONAL, "createReprocessableCaptureSession", PointerIconCompat.TYPE_HAND);
                    CamLog.traceEnd(TraceTag.OPTIONAL, "createCaptureSession", PointerIconCompat.TYPE_HAND);
                    CamLog.traceEnd(TraceTag.OPTIONAL, "createConstrainedHighSpeedCaptureSession", PointerIconCompat.TYPE_HAND);
                    SessionConfigListener.this.mSuccess = true;
                    SessionConfigListener.this.mSession = session;
                    SessionConfigListener.this.mDeviceReady.open();
                }

                public void onConfigureFailed(CameraCaptureSession session) {
                    CamLog.m5e(BlockingCameraManager.TAG, "onConfigureFailed:" + name + " session is not created");
                    SessionConfigListener.this.mSuccess = false;
                    SessionConfigListener.this.mSession = null;
                    SessionConfigListener.this.mDeviceReady.open();
                }

                public void onReady(CameraCaptureSession session) {
                    CamLog.m3d(BlockingCameraManager.TAG, "onReady: " + name + "  session is ready to operate " + session);
                }

                public void onClosed(CameraCaptureSession session) {
                    CamLog.m3d(BlockingCameraManager.TAG, "onClosed: " + name + "  session is closed " + session);
                }
            };
        }

        private List<OutputConfiguration> getOutputConfigurationList(List<Surface> surfaceList) {
            if (surfaceList == null || surfaceList.size() <= 0) {
                CamLog.m5e(BlockingCameraManager.TAG, "wrong surfaceList");
                return null;
            }
            List<OutputConfiguration> outConfigurations = new ArrayList(surfaceList.size());
            for (Surface surface : surfaceList) {
                if (surface != null) {
                    outConfigurations.add(new OutputConfiguration(surface));
                }
            }
            return outConfigurations;
        }
    }

    public BlockingCameraManager(CameraManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager must not be null");
        }
        this.mManager = manager;
    }

    private static void assertEquals(Object a, Object b) {
        if (!Objects.equals(a, b)) {
            throw new AssertionError("Expected " + a + ", but got " + b);
        }
    }

    private static void assertNotEquals(Object a, Object b) {
        if (Objects.equals(a, b)) {
            throw new AssertionError("Expected " + a + ", is not " + b);
        }
    }

    public CameraDevice openCamera(String cameraId, StateCallback callback, Handler handler) throws CameraAccessException, BlockingOpenException {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        } else if (handler.getLooper() == Looper.myLooper()) {
            throw new IllegalArgumentException("handler's looper must not be the current looper");
        } else {
            return new OpenListener(this.mManager, cameraId, callback, handler).blockUntilOpen();
        }
    }

    public CameraCaptureSession createSession(Boolean isReprocess, CameraDevice camera, InputConfiguration inputConfig, ArrayList<?> outputConfigList, Handler handler, int operationType) throws CameraAccessException {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        } else if (handler.getLooper() == Looper.myLooper()) {
            throw new IllegalArgumentException("handler's looper must not be the current looper");
        } else {
            CamLog.m7i(TAG, " Shot Session. isReprocess? " + isReprocess + " / operationType : " + operationType);
            return new SessionConfigListener(isReprocess, camera, inputConfig, outputConfigList, handler, operationType).blockUntilCreateSession();
        }
    }

    public CameraCaptureSession createRecordingSession(Boolean isHighSpeedRecording, CameraDevice camera, ArrayList<?> outputList, Handler handler, int operationType) throws CameraAccessException {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        } else if (handler.getLooper() == Looper.myLooper()) {
            throw new IllegalArgumentException("handler's looper must not be the current looper");
        } else {
            CamLog.m7i(TAG, " Recording Session . is HRF ? " + isHighSpeedRecording + " / operationType : " + operationType);
            return new SessionConfigListener(isHighSpeedRecording, camera, outputList, handler, operationType).blockUntilCreateSession();
        }
    }
}
