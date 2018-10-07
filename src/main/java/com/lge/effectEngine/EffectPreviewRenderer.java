package com.lge.effectEngine;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.lang.ref.WeakReference;

public class EffectPreviewRenderer extends Thread {
    private static final int NUM_OF_FRAME = 16;
    private static final String TAG = "RenderThreadPreview";
    private static int sFrameCount = 0;
    private static long sStartTick = -1;
    private static long sTotalTime = 0;
    private boolean isRecording;
    private boolean isWideCamStopping;
    private float[] mBoundaryMatrix = new float[16];
    private float mCameraAspect;
    private Camera mCameraNormal = null;
    private int mCameraPreviewHeightNormal;
    private int mCameraPreviewHeightWide;
    private int mCameraPreviewWidthNormal;
    private int mCameraPreviewWidthWide;
    private SurfaceTexture mCameraTextureNormal = null;
    private SurfaceTexture mCameraTextureWide = null;
    private Camera mCameraWide = null;
    private float[] mDisplayProjectionMatrix = new float[16];
    private int mDrawMode = 0;
    public EffectFrameDrawer mFrameDrawer;
    private int mFrameType = 0;
    private volatile RenderHandlerPreview mHandler;
    private SurfaceHolder mHolder;
    private boolean mIsHal3;
    private LGPopoutEffectEngineListener mListener;
    private double mPrevFrameTime;
    private boolean mReady = false;
    private int mRecHeight;
    private float[] mRecProjectionMatrix = new float[16];
    private EffectEGLSurfaceBase mRecSurface;
    private int mRecWidth;
    private Object mRecordingLock = new Object();
    private int mReverseSign;
    private Object mStartLock = new Object();
    private Object mStopWideLock = new Object();
    private EffectEGLSurfaceBase mWindowSurface;
    private int mWindowSurfaceHeight;
    private int mWindowSurfaceWidth;

    /* renamed from: com.lge.effectEngine.EffectPreviewRenderer$1 */
    class C00771 implements OnFrameAvailableListener {
        C00771() {
        }

        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            EffectPreviewRenderer.this.mHandler.sendFrameAvailableNormal();
        }
    }

    /* renamed from: com.lge.effectEngine.EffectPreviewRenderer$2 */
    class C00782 implements OnFrameAvailableListener {
        C00782() {
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

        public void sendFrameType(int type) {
            sendMessage(obtainMessage(8, type, 0));
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
                    renderThread.setFrameType(msg.arg1);
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

    public static void checkFPS(String name) {
        if (sStartTick < 0) {
            sStartTick = System.nanoTime();
            sFrameCount = 0;
            return;
        }
        sFrameCount++;
        if (sFrameCount >= 16) {
            long currentTick = System.nanoTime();
            sTotalTime = currentTick - sStartTick;
            Log.d(TAG, new StringBuilder(String.valueOf(name)).append(" fps=").append((((float) sFrameCount) * 1.0E9f) / ((float) sTotalTime)).toString());
            sFrameCount = 0;
            sStartTick = currentTick;
            sTotalTime = 0;
        }
    }

    public EffectPreviewRenderer(LGPopoutEffectEngineListener listener, SurfaceHolder holder, boolean reverse, int normW, int normH, int wideW, int wideH, boolean isHal3) {
        this.mListener = listener;
        this.mHolder = holder;
        this.mDrawMode = 0;
        this.mReverseSign = reverse ? -1 : 1;
        this.mCameraPreviewWidthWide = wideW;
        this.mCameraPreviewHeightWide = wideH;
        this.mCameraPreviewWidthNormal = normW;
        this.mCameraPreviewHeightNormal = normH;
        this.mIsHal3 = isHal3;
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

    protected void surfaceCreated(SurfaceHolder holder) {
        int width = holder.getSurfaceFrame().width();
        int height = holder.getSurfaceFrame().height();
        this.mWindowSurfaceWidth = width;
        this.mWindowSurfaceHeight = height;
        long prev = System.nanoTime();
        this.mWindowSurface = new EffectEGLSurfaceBase(null, holder.getSurface(), 0);
        this.mWindowSurface.makeContextCurrent();
        this.mFrameDrawer = new EffectFrameDrawer(this.mIsHal3);
        int[] textureIDs = this.mFrameDrawer.createTextureObject(width, height);
        Log.d(TAG, "texture ids : " + textureIDs[0] + ", " + textureIDs[1]);
        this.mCameraTextureNormal = new SurfaceTexture(textureIDs[0]);
        this.mCameraTextureNormal.setDefaultBufferSize(this.mCameraPreviewWidthNormal, this.mCameraPreviewHeightNormal);
        this.mCameraTextureNormal.setOnFrameAvailableListener(new C00771());
        this.mCameraTextureWide = new SurfaceTexture(textureIDs[1]);
        this.mCameraTextureWide.setDefaultBufferSize(this.mCameraPreviewWidthWide, this.mCameraPreviewHeightWide);
        this.mCameraTextureWide.setOnFrameAvailableListener(new C00782());
        this.mFrameDrawer.createPrograms();
        this.mFrameDrawer.createLocations();
        this.mListener.onEngineInitializeDone(this.mCameraTextureNormal, this.mCameraTextureWide);
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
        if (this.mFrameDrawer != null) {
            this.mFrameDrawer.release();
            this.mFrameDrawer = null;
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
        updateGeometry();
    }

    protected void surfaceChanged(int width, int height) {
        Log.d(TAG, "RenderThread surfaceChanged " + width + "x" + height);
        this.mWindowSurfaceWidth = width;
        this.mWindowSurfaceHeight = height;
        finishSurfaceSetup();
    }

    protected void finishSurfaceSetup() {
        GLES20.glViewport(0, 0, this.mWindowSurfaceWidth, this.mWindowSurfaceHeight);
        updateGeometry();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Log.d(TAG, "starting camera preview");
    }

    protected void updateGeometry() {
        int tFrameType;
        int width = this.mWindowSurfaceWidth;
        int height = this.mWindowSurfaceHeight;
        int smallDim = Math.min(width, height);
        this.mCameraAspect = ((float) this.mCameraPreviewWidthWide) / ((float) this.mCameraPreviewHeightWide);
        float[] modelMatrix = new float[16];
        Matrix.orthoM(modelMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
        Matrix.translateM(modelMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
        Matrix.scaleM(modelMatrix, 0, (float) smallDim, ((float) (-smallDim)) * this.mCameraAspect, 1.0f);
        this.mDisplayProjectionMatrix = modelMatrix;
        float[] bmodelMatrix = new float[16];
        Matrix.orthoM(bmodelMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
        Matrix.translateM(bmodelMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
        Matrix.rotateM(bmodelMatrix, 0, -90.0f, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(bmodelMatrix, 0, (float) (this.mReverseSign * height), (float) width, 1.0f);
        this.mBoundaryMatrix = bmodelMatrix;
        float thickness = ShaderParameterData.getInstance().getFrameThickness();
        float shadowRadius = ShaderParameterData.getInstance().getFrameShadowsSize();
        float dx = ((float) height) * ShaderParameterData.getInstance().getFrameShadowDX();
        float dy = ((float) width) * ShaderParameterData.getInstance().getFrameShadowDY();
        int shadowColor = ShaderParameterData.getInstance().getFrameShadowColor();
        if (this.mFrameType == 4) {
            tFrameType = 3;
        } else {
            tFrameType = this.mFrameType;
        }
        int normFrameWidth = (int) (((float) width) * ShaderParameterData.getInstance().getNormToWideRatio()[tFrameType]);
        int normFrameHeight = (int) (((float) height) * ShaderParameterData.getInstance().getNormToWideRatio()[tFrameType]);
        int shadowWidth = (int) (((float) normFrameWidth) * ShaderParameterData.getInstance().getFrameShadowsScale());
        int shadowHeight = (normFrameHeight + shadowWidth) - normFrameWidth;
        Bitmap bm;
        Canvas canvas;
        Paint paint;
        float sleft;
        float stop;
        float left;
        float top;
        Bitmap mask;
        Canvas cMask;
        Paint paint2;
        int normFrameHeightMod;
        int heightMod;
        if (this.mFrameType == 0) {
            bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            sleft = (((float) (width - shadowWidth)) * 0.5f) + dy;
            stop = (((float) (height - shadowHeight)) * 0.5f) + (-dx);
            canvas.drawRect(sleft - thickness, stop - thickness, (((float) shadowWidth) + sleft) + thickness, (((float) shadowHeight) + stop) + thickness, paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            left = ((float) (width - normFrameWidth)) * 0.5f;
            top = ((float) (height - normFrameHeight)) * 0.5f;
            canvas.drawRect(left - thickness, top - thickness, (((float) normFrameWidth) + left) + thickness, (((float) normFrameHeight) + top) + thickness, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            canvas.drawRect(left, top, left + ((float) normFrameWidth), top + ((float) normFrameHeight), paint);
            this.mFrameDrawer.setBoundaryTexture(bm);
            bm.recycle();
            mask = Bitmap.createBitmap(normFrameWidth, normFrameHeight, Config.ARGB_8888);
            cMask = new Canvas(mask);
            paint2 = new Paint(1);
            paint2.setColor(-1);
            cMask.drawPaint(paint2);
            this.mFrameDrawer.setMaskTexture(mask);
            mask.recycle();
            System.gc();
            this.mBoundaryMatrix = this.mDisplayProjectionMatrix;
        } else if (this.mFrameType == 3) {
            bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            sleft = (((float) (width - shadowWidth)) * 0.5f) + dy;
            stop = (((float) (height - shadowHeight)) * 0.5f) + (-dx);
            canvas.drawRect(0.0f - thickness, stop - thickness, ((float) width) + thickness, (((float) shadowHeight) + stop) + thickness, paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            left = ((float) (width - normFrameWidth)) * 0.5f;
            top = ((float) (height - normFrameHeight)) * 0.5f;
            canvas.drawRect(0.0f - thickness, top - thickness, ((float) width) + thickness, (((float) normFrameHeight) + top) + thickness, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            canvas.drawRect(0.0f, top, (float) width, top + ((float) normFrameHeight), paint);
            this.mFrameDrawer.setBoundaryTexture(bm);
            bm.recycle();
            mask = Bitmap.createBitmap(normFrameWidth, normFrameHeight, Config.ARGB_8888);
            cMask = new Canvas(mask);
            paint2 = new Paint(1);
            paint2.setColor(-1);
            cMask.drawPaint(paint2);
            this.mFrameDrawer.setMaskTexture(mask);
            mask.recycle();
            System.gc();
            this.mBoundaryMatrix = this.mDisplayProjectionMatrix;
        } else if (this.mFrameType == 4) {
            bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            sleft = (((float) (width - shadowWidth)) * 0.5f) + dy;
            stop = (((float) (height - shadowHeight)) * 0.5f) + (-dx);
            canvas.drawRect(sleft - thickness, 0.0f - thickness, (((float) shadowWidth) + sleft) + thickness, ((float) height) + thickness, paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            left = ((float) (width - normFrameWidth)) * 0.5f;
            top = ((float) (height - normFrameHeight)) * 0.5f;
            canvas.drawRect(left - thickness, 0.0f - thickness, (((float) normFrameWidth) + left) + thickness, ((float) height) + thickness, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            canvas.drawRect(left, 0.0f, left + ((float) normFrameWidth), (float) height, paint);
            this.mFrameDrawer.setBoundaryTexture(bm);
            bm.recycle();
            mask = Bitmap.createBitmap(normFrameWidth, normFrameHeight, Config.ARGB_8888);
            cMask = new Canvas(mask);
            paint2 = new Paint(1);
            paint2.setColor(-1);
            cMask.drawPaint(paint2);
            this.mFrameDrawer.setMaskTexture(mask);
            mask.recycle();
            System.gc();
            this.mBoundaryMatrix = this.mDisplayProjectionMatrix;
        } else if (this.mFrameType == 1) {
            normFrameHeightMod = (int) (((float) normFrameWidth) * this.mCameraAspect);
            heightMod = (int) (((float) width) * this.mCameraAspect);
            bm = Bitmap.createBitmap(width, heightMod, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            canvas.save();
            canvas.rotate(-90.0f, (float) (width / 2), (float) (heightMod / 2));
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            canvas.drawCircle(((float) (width / 2)) + dx, ((float) (heightMod / 2)) + dy, (((float) shadowWidth) + thickness) / 2.0f, paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            canvas.drawCircle((float) (width / 2), (float) (heightMod / 2), (((float) normFrameWidth) + thickness) / 2.0f, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            canvas.drawCircle((float) (width / 2), (float) (heightMod / 2), (float) (normFrameWidth / 2), paint);
            canvas.restore();
            this.mFrameDrawer.setBoundaryTexture(bm);
            bm.recycle();
            mask = Bitmap.createBitmap(normFrameHeightMod, normFrameWidth, Config.ARGB_8888);
            cMask = new Canvas(mask);
            paint2 = new Paint(1);
            paint2.setColor(ViewCompat.MEASURED_STATE_MASK);
            cMask.drawPaint(paint2);
            paint2.setColor(-1);
            cMask.drawCircle((float) (normFrameHeightMod / 2), (float) (normFrameWidth / 2), (float) (normFrameWidth / 2), paint2);
            this.mFrameDrawer.setMaskTexture(mask);
            mask.recycle();
            System.gc();
        } else if (this.mFrameType == 2) {
            normFrameHeightMod = (int) (((float) normFrameWidth) * this.mCameraAspect);
            heightMod = (int) (((float) width) * this.mCameraAspect);
            bm = Bitmap.createBitmap(width, heightMod, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            canvas.save();
            canvas.rotate(-90.0f, (float) (width / 2), (float) (heightMod / 2));
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            canvas.drawPath(getPathHex(((int) dx) + width, ((int) dy) + heightMod, ((int) (((float) shadowWidth) + thickness)) / 2), paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            canvas.drawPath(getPathHex(width, heightMod, ((int) (((float) normFrameWidth) + thickness)) / 2), paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            canvas.drawPath(getPathHex(width, heightMod, normFrameWidth / 2), paint);
            canvas.restore();
            this.mFrameDrawer.setBoundaryTexture(bm);
            bm.recycle();
            mask = Bitmap.createBitmap(normFrameHeightMod, normFrameWidth, Config.ARGB_8888);
            cMask = new Canvas(mask);
            paint2 = new Paint(1);
            paint2.setColor(ViewCompat.MEASURED_STATE_MASK);
            cMask.drawPaint(paint2);
            cMask.clipPath(getPathHex(normFrameHeightMod, normFrameWidth, normFrameWidth / 2));
            cMask.drawColor(-1);
            this.mFrameDrawer.setMaskTexture(mask);
            mask.recycle();
            System.gc();
        }
    }

    protected Path getPathHex(int width, int height, int radius) {
        Path hexagonPath = new Path();
        float triangleHeight = (float) ((Math.sqrt(3.0d) * ((double) radius)) / 2.0d);
        float centerX = (float) (width / 2);
        float centerY = (float) (height / 2);
        hexagonPath.moveTo(centerX, ((float) radius) + centerY);
        hexagonPath.lineTo(centerX - triangleHeight, ((float) (radius / 2)) + centerY);
        hexagonPath.lineTo(centerX - triangleHeight, centerY - ((float) (radius / 2)));
        hexagonPath.lineTo(centerX, centerY - ((float) radius));
        hexagonPath.lineTo(centerX + triangleHeight, centerY - ((float) (radius / 2)));
        hexagonPath.lineTo(centerX + triangleHeight, ((float) (radius / 2)) + centerY);
        hexagonPath.lineTo(centerX, ((float) radius) + centerY);
        return hexagonPath;
    }

    protected void frameAvailableNormal() {
        this.mWindowSurface.makeContextCurrent();
        this.mCameraTextureNormal.updateTexImage();
        synchronized (this.mStopWideLock) {
            synchronized (this.mRecordingLock) {
                if (!(this.isRecording && this.isWideCamStopping)) {
                    this.mCameraTextureWide.updateTexImage();
                }
            }
        }
        draw(this.mDrawMode);
    }

    protected void frameAvailableWide() {
    }

    protected void draw(int mode) {
        try {
            float[] normalTrasnformMatrix = new float[16];
            float[] wideTransformMatrix = new float[16];
            this.mCameraTextureNormal.getTransformMatrix(normalTrasnformMatrix);
            this.mCameraTextureWide.getTransformMatrix(wideTransformMatrix);
            GLES20.glClear(16384);
            this.mFrameDrawer.draw(this.mDisplayProjectionMatrix, this.mBoundaryMatrix, normalTrasnformMatrix, wideTransformMatrix, this.mDrawMode, this.mFrameType, this.mWindowSurfaceWidth, this.mWindowSurfaceHeight, this.mCameraAspect, false);
            this.mWindowSurface.swapBuffers();
            synchronized (this.mRecordingLock) {
                if (this.isRecording) {
                    this.mRecSurface.makeContextCurrent();
                    GLES20.glClear(16384);
                    this.mFrameDrawer.draw(this.mRecProjectionMatrix, this.mBoundaryMatrix, normalTrasnformMatrix, wideTransformMatrix, this.mDrawMode, this.mFrameType, this.mRecWidth, this.mRecHeight, this.mCameraAspect, false, true);
                    this.mRecSurface.swapBuffers();
                }
            }
            if (ShaderParameterData.getInstance().isFPSMode()) {
                checkFPS("Preview");
            }
        } catch (RuntimeException e) {
            Log.d(TAG, "cannot draw without surface : " + e.getMessage());
            this.mCameraTextureNormal.setOnFrameAvailableListener(null);
            this.mCameraTextureWide.setOnFrameAvailableListener(null);
            this.mListener.onErrorOccured(1);
            shutdown();
        }
    }

    protected void setMode(int mode) {
        this.mDrawMode = mode;
    }

    protected void setFrameType(int type) {
        this.mFrameType = type;
    }

    protected void prepareRecording(Surface surface, int width, int height) {
        Log.d(TAG, "surface : " + surface);
        synchronized (this.mRecordingLock) {
            this.mRecSurface = new EffectEGLSurfaceBase(this.mWindowSurface.getEGLContext(), surface, 1);
            float[] modelMatrix = new float[16];
            Matrix.orthoM(modelMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
            Matrix.translateM(modelMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
            Matrix.scaleM(modelMatrix, 0, (float) ((-this.mReverseSign) * width), (float) height, 1.0f);
            Matrix.rotateM(modelMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f);
            this.mRecProjectionMatrix = modelMatrix;
            this.mRecWidth = width;
            this.mRecHeight = height;
        }
    }

    protected void startRecording(boolean stopWideCam) {
        synchronized (this.mRecordingLock) {
            this.isRecording = true;
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
                this.mRecSurface.releaseRecordingEGL();
                this.mRecSurface = null;
            }
            this.mRecordingLock.notify();
        }
    }

    public void waitStopRecording() {
        synchronized (this.mRecordingLock) {
            if (this.isRecording) {
                try {
                    this.mRecordingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
}
