package com.lge.effectEngine2;

import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.lgpopouteffectengine2.LGPopoutEffectEngine.EffectFrameData;
import java.lang.ref.WeakReference;

public class EffectPreviewRenderer extends Thread {
    private static final String TAG = "RenderThreadPreview";
    private final int NUM_OF_FRAME = 16;
    private boolean isDualCameraMode;
    private boolean isRecording;
    private boolean isWideCamStopping;
    private RectF mBoundaryRect;
    private Camera mCameraNormal = null;
    private int mCameraPreviewHeightNormal;
    private int mCameraPreviewHeightWide;
    private int mCameraPreviewWidthNormal;
    private int mCameraPreviewWidthWide;
    private SurfaceTexture mCameraTextureNormal = null;
    private SurfaceTexture mCameraTextureWide = null;
    private Camera mCameraWide = null;
    private int mDrawMode = 0;
    private volatile RenderHandlerPreview mHandler;
    private SurfaceHolder mHolder;
    private LGPopoutEffectEngineListener mListener;
    public EffectPreviewFrame mPreviewDrawer;
    private boolean mReady = false;
    private boolean mReadyRec = false;
    private EffectEGLSurfaceBase mRecSurface;
    public EffectRecordFrame mRecordDrawer;
    private Object mRecordingLock = new Object();
    private int mReverseSign;
    private Object mStartLock = new Object();
    private Object mStopWideLock = new Object();
    private Object mUpdateCamParamLock = new Object();
    private EffectEGLSurfaceBase mWindowSurface;
    private int mWindowSurfaceHeight;
    private int mWindowSurfaceWidth;
    private int sFrameCount = 0;
    private long sStartTick = -1;
    private long sTotalTime = 0;

    /* renamed from: com.lge.effectEngine2.EffectPreviewRenderer$1 */
    class C16101 implements OnFrameAvailableListener {
        C16101() {
        }

        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            EffectPreviewRenderer.this.mHandler.sendFrameAvailableNormal();
        }
    }

    /* renamed from: com.lge.effectEngine2.EffectPreviewRenderer$2 */
    class C16112 implements OnFrameAvailableListener {
        C16112() {
        }

        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            EffectPreviewRenderer.this.mHandler.sendFrameAvailableWide();
        }
    }

    public class RenderHandlerPreview extends Handler {
        private static final int MSG_DRAWMODE = 7;
        private static final int MSG_FRAMETYPE = 8;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_FRAME_AVAILABLE_NORMAL = 5;
        private static final int MSG_FRAME_AVAILABLE_WIDE = 6;
        private static final int MSG_REC_PREPARE = 10;
        private static final int MSG_REC_START = 11;
        private static final int MSG_REC_STOP = 12;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_UPDATE_CAM_PARAM = 9;
        private static final String TAG = "RenderHandlerPreview";
        private WeakReference<EffectPreviewRenderer> mWeakRenderThread;

        public RenderHandlerPreview(EffectPreviewRenderer rt) {
            this.mWeakRenderThread = new WeakReference(rt);
        }

        public void sendSurfaceCreated(SurfaceHolder holder, boolean newSurface) {
            int i;
            if (newSurface) {
                i = 1;
            } else {
                i = 0;
            }
            sendMessage(obtainMessage(0, i, 0, holder));
        }

        public void sendSurfaceChanged(int width, int height) {
            sendMessage(obtainMessage(1, width, height));
        }

        public void sendSurfaceDestroyed() {
            sendMessage(obtainMessage(2));
        }

        public void sendShutdown() {
            sendMessage(obtainMessage(3));
        }

        public void sendFrameAvailable() {
            sendMessage(obtainMessage(4));
        }

        public void sendFrameAvailableNormal() {
            sendMessage(obtainMessage(5));
        }

        public void sendFrameAvailableWide() {
            sendMessage(obtainMessage(6));
        }

        public void sendDrawMode(int mode) {
            sendMessage(obtainMessage(7, mode, 0));
        }

        public void sendFrameType(EffectFrameData elem) {
            sendMessage(obtainMessage(8, elem));
        }

        public void updateCamParam() {
            sendMessage(obtainMessage(9));
        }

        public void sendPrepareRecording(Surface sur, int width, int height) {
            sendMessage(obtainMessage(10, width, height, sur));
        }

        public void sendStartRecording(boolean stopWideCam) {
            int i;
            if (stopWideCam) {
                i = 1;
            } else {
                i = 0;
            }
            sendMessage(obtainMessage(11, i, 0));
        }

        public void sendStopRecording() {
            sendMessage(obtainMessage(12));
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            EffectPreviewRenderer renderThread = (EffectPreviewRenderer) this.mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }
            switch (what) {
                case 0:
                case 2:
                    return;
                case 1:
                    renderThread.surfaceChanged(msg.arg1, msg.arg2);
                    return;
                case 3:
                    renderThread.shutdown();
                    return;
                case 5:
                    renderThread.frameAvailableNormal();
                    return;
                case 6:
                    renderThread.frameAvailableWide();
                    return;
                case 7:
                    renderThread.setMode(msg.arg1);
                    return;
                case 8:
                    renderThread.setFrameType((EffectFrameData) msg.obj);
                    return;
                case 9:
                    renderThread.updatedCameraParameters();
                    return;
                case 10:
                    renderThread.prepareRecording((Surface) msg.obj, msg.arg1, msg.arg2);
                    break;
                case 11:
                    break;
                case 12:
                    renderThread.stopRecording();
                    return;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
            renderThread.startRecording(msg.arg1 == 1);
        }
    }

    public void checkFPS(String name) {
        if (this.sStartTick < 0) {
            this.sStartTick = System.nanoTime();
            this.sFrameCount = 0;
            return;
        }
        this.sFrameCount++;
        if (this.sFrameCount >= 16) {
            long currentTick = System.nanoTime();
            this.sTotalTime = currentTick - this.sStartTick;
            this.mListener.displayFPS((((float) this.sFrameCount) * 1.0E9f) / ((float) this.sTotalTime), this.isRecording);
            this.sFrameCount = 0;
            this.sStartTick = currentTick;
            this.sTotalTime = 0;
        }
    }

    public static long getUsedMemorySize() {
        try {
            Runtime info = Runtime.getRuntime();
            return info.totalMemory() - info.freeMemory();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public EffectPreviewRenderer(LGPopoutEffectEngineListener listener, Camera norm, Camera wide, SurfaceHolder holder, boolean reverse) {
        int i = 1;
        this.mListener = listener;
        if (norm == null) {
            this.isDualCameraMode = false;
            this.mCameraNormal = null;
        } else {
            this.isDualCameraMode = true;
            this.mCameraNormal = norm;
        }
        this.mCameraWide = wide;
        this.mHolder = holder;
        this.mDrawMode = 0;
        if (reverse) {
            i = -1;
        }
        this.mReverseSign = i;
        readCameraSize();
    }

    public void readCameraSize() {
        Size sz = this.mCameraWide.getParameters().getPreviewSize();
        this.mCameraPreviewWidthWide = sz.width;
        this.mCameraPreviewHeightWide = sz.height;
        if (this.isDualCameraMode) {
            sz = this.mCameraNormal.getParameters().getPreviewSize();
            this.mCameraPreviewWidthNormal = sz.width;
            this.mCameraPreviewHeightNormal = sz.height;
        }
    }

    public void run() {
        Looper.prepare();
        this.mHandler = new RenderHandlerPreview(this);
        surfaceCreated(this.mHolder);
        synchronized (this.mStartLock) {
            this.mReady = true;
            this.mStartLock.notify();
        }
        Log.d(TAG, "looper start");
        Looper.loop();
        Log.d(TAG, "looper quit");
        releaseGl();
        synchronized (this.mStartLock) {
            this.mReady = false;
        }
    }

    public void waitUntilReady() {
        synchronized (this.mStartLock) {
            while (!this.mReady) {
                try {
                    this.mStartLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public RenderHandlerPreview getHandler() {
        return this.mHandler;
    }

    public RectF getBoundRect() {
        return this.mBoundaryRect;
    }

    protected void surfaceCreated(SurfaceHolder holder) {
        int width = holder.getSurfaceFrame().width();
        int height = holder.getSurfaceFrame().height();
        this.mWindowSurface = new EffectEGLSurfaceBase(null, holder.getSurface(), 0);
        this.mWindowSurface.makeContextCurrent();
        this.mPreviewDrawer = new EffectPreviewFrame(this.isDualCameraMode);
        int[] textureIDs = this.mPreviewDrawer.createTextureObject(width, height, this.mCameraPreviewWidthWide, this.mCameraPreviewHeightWide);
        Log.d(TAG, "texture ids : " + textureIDs[0] + ", " + textureIDs[1]);
        if (this.isDualCameraMode) {
            this.mCameraTextureNormal = new SurfaceTexture(textureIDs[0]);
            this.mCameraTextureNormal.setOnFrameAvailableListener(new C16101());
        }
        this.mCameraTextureWide = new SurfaceTexture(textureIDs[1]);
        this.mCameraTextureWide.setOnFrameAvailableListener(new C16112());
        if (this.isDualCameraMode) {
            this.mListener.onEngineInitializeDone(this.mCameraTextureNormal, this.mCameraTextureWide);
        } else {
            this.mListener.onEngineInitializeDone(null, this.mCameraTextureWide);
        }
        this.mPreviewDrawer.createPrograms();
        this.mPreviewDrawer.createLocations();
    }

    private void releaseGl() {
        if (this.mCameraTextureNormal != null) {
            this.mCameraTextureNormal.release();
            this.mCameraTextureNormal = null;
        }
        if (this.mCameraTextureWide != null) {
            this.mCameraTextureWide.release();
            this.mCameraTextureWide = null;
        }
        if (this.mPreviewDrawer != null) {
            this.mPreviewDrawer.release();
            this.mPreviewDrawer = null;
        }
        if (this.mWindowSurface != null) {
            this.mWindowSurface.release();
            this.mWindowSurface = null;
        }
    }

    protected void shutdown() {
        Log.d(TAG, "shutdown");
        Looper.myLooper().quit();
    }

    protected void updatedCameraParameters() {
        readCameraSize();
        changedSizes();
        Log.d(TAG, "end update param");
        synchronized (this.mUpdateCamParamLock) {
            this.mUpdateCamParamLock.notify();
        }
    }

    public void waitUpdatedCameraParam() {
        synchronized (this.mUpdateCamParamLock) {
            try {
                this.mUpdateCamParamLock.wait(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void surfaceChanged(int width, int height) {
        Log.d(TAG, "RenderThread surfaceChanged " + width + "x" + height);
        this.mWindowSurfaceWidth = width;
        this.mWindowSurfaceHeight = height;
        changedSizes();
    }

    protected void changedSizes() {
        this.mPreviewDrawer.setImageSize(this.mWindowSurfaceWidth, this.mWindowSurfaceHeight, this.mCameraPreviewWidthWide, this.mCameraPreviewHeightWide);
        Log.d(TAG, "size changed");
    }

    protected void frameAvailableNormal() {
        this.mWindowSurface.makeContextCurrent();
        this.mCameraTextureNormal.updateTexImage();
        synchronized (this.mStopWideLock) {
            if (!this.isWideCamStopping) {
                this.mCameraTextureWide.updateTexImage();
            }
        }
        draw(this.mDrawMode);
    }

    protected void frameAvailableWide() {
        if (!this.isDualCameraMode) {
            this.mWindowSurface.makeContextCurrent();
            this.mCameraTextureWide.updateTexImage();
            draw(this.mDrawMode);
        }
    }

    protected void draw(int mode) {
        try {
            RectF newRect = this.mListener.onRequestBoundaryPosition();
            if (newRect == null) {
                newRect = this.mBoundaryRect;
            }
            this.mBoundaryRect = newRect;
            synchronized (this.mRecordingLock) {
                if (this.isRecording) {
                    this.mRecSurface.makeContextCurrent();
                    GLES20.glClear(16384);
                    this.mRecordDrawer.setInnerRectPos(this.mBoundaryRect);
                    this.mRecordDrawer.draw(mode);
                    this.mRecSurface.swapBuffers();
                }
            }
            this.mWindowSurface.makeContextCurrent();
            GLES20.glClear(16384);
            this.mPreviewDrawer.setInnerRectPos(this.mBoundaryRect);
            this.mPreviewDrawer.draw(mode);
            this.mWindowSurface.swapBuffers();
            if (ShaderParameterData.getInstance().isFPSMode()) {
                checkFPS("Preview");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (this.isDualCameraMode) {
                this.mCameraTextureNormal.setOnFrameAvailableListener(null);
            }
            this.mCameraTextureWide.setOnFrameAvailableListener(null);
            this.mListener.onErrorOccured(1);
            shutdown();
        }
    }

    protected void setMode(int mode) {
        this.mDrawMode = mode;
    }

    protected void setFrameType(EffectFrameData elem) {
        this.mPreviewDrawer.setInnerRectPos(elem.getOutterPos());
        this.mPreviewDrawer.setBoundaryTexture(elem.getBoundary());
        this.mPreviewDrawer.setMaskTexture(elem.getMask(), elem.getInnerPos());
        this.mBoundaryRect = elem.getOutterPos();
    }

    protected void prepareRecording(Surface surface, int width, int height) {
        Log.d(TAG, "surface : " + surface);
        synchronized (this.mRecordingLock) {
            this.mRecSurface = new EffectEGLSurfaceBase(this.mWindowSurface.getEGLContext(), surface, 1);
            this.mRecSurface.makeContextCurrent();
            this.mRecordDrawer = new EffectRecordFrame(this.isDualCameraMode, this.mPreviewDrawer, width, height);
        }
    }

    protected void startRecording(boolean stopWideCam) {
        synchronized (this.mRecordingLock) {
            this.isRecording = true;
            this.mReadyRec = false;
        }
        synchronized (this.mStopWideLock) {
            this.isWideCamStopping = stopWideCam;
        }
    }

    protected void stopRecording() {
        synchronized (this.mRecordingLock) {
            this.isRecording = false;
            if (this.mRecSurface != null) {
                this.mRecSurface.makeContextCurrent();
                this.mRecordDrawer.release();
                this.mRecSurface.releaseRecordingEGL();
                this.mRecSurface = null;
            }
            this.mReadyRec = true;
            this.mRecordingLock.notify();
        }
    }

    public void waitStopRecording() {
        if (this.isRecording) {
            synchronized (this.mRecordingLock) {
                this.mReadyRec = false;
                while (!this.mReadyRec) {
                    try {
                        this.mRecordingLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void pauseRecording() {
        synchronized (this.mRecordingLock) {
            this.isRecording = false;
        }
    }

    public void resumeRecording() {
        synchronized (this.mRecordingLock) {
            this.isRecording = true;
        }
    }

    public void pauseWideView() {
        synchronized (this.mStopWideLock) {
            this.isWideCamStopping = true;
        }
    }

    public void resumeWideView() {
        synchronized (this.mStopWideLock) {
            this.isWideCamStopping = false;
        }
    }
}
