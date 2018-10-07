package com.lge.filmemulatorengine;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.arcsoft.stickerlibrary.utils.Constant;
import com.lge.camera.components.HybridViewConfig;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.AnimationManager;
import com.lge.camera.util.CamLog;
import com.lge.ellievision.parceldata.ISceneCategory;
import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class FilmEmulatorRendererPreview extends Thread {
    private static final int CAMERA1 = 0;
    private static final int CAMERA2 = 1;
    private static final int MAX_FILTER_LUT_COUNT = 81;
    private static final int NUM_OF_FRAME = 16;
    private static final int PROGRAM_NUM = 6;
    private static final float[] RECTANGLE_FRAG_COORDS = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
    private static final float[] RECTANGLE_VERTEX_COORDS = new float[]{-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f};
    private static final String TAG = "FilmEmulationRendererPrivew";
    private static int sFrameCount = 0;
    private static long sStartTick = -1;
    private static long sTotalTime = 0;
    private final String BACKGROUND_FRAGMENT_SHADER = "precision mediump float;\nuniform sampler2D sTexture;\nuniform float alpha;\nvarying vec2 texCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture,texCoord);\n  gl_FragColor.a = alpha;\n}";
    private final String DELETE_BTN_FRAGMENT_SHADER = "precision mediump float;\nuniform sampler2D sTexture;\nuniform float alpha;\nvarying vec2 texCoord;\nvoid main() {\n    vec4 colorOut = texture2D(sTexture,texCoord);\n    if (colorOut.a < 0.2) {\n        discard;\n    }\n    gl_FragColor = colorOut;\n    gl_FragColor.a = alpha;\n}";
    private final String LUT_FRAGMENT_SHADER = "#extension GL_OES_texture_3D : enable\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nprecision mediump sampler3D;\nuniform samplerExternalOES sTexture;\nuniform sampler3D lut;\nuniform sampler2D text;\nuniform sampler2D deleteBtn;\nvarying vec2 texCoord;\nuniform float alpha;\nuniform float strength;\nuniform float showtext;\nuniform float selected;\nuniform float borderSize;\nuniform float pressMenu;\nuniform float drawDeleteBtn;\nuniform float squareResolution;\nvoid main() {\n    vec4 colorIn = texture2D(sTexture, texCoord);\n    vec4 lut = texture3D(lut, colorIn.rgb);\n    vec4 textIn = texture2D(text, texCoord);\n    vec4 deleteBtnIn = texture2D(deleteBtn, texCoord);\n    vec4 colorOut;\n    if (showtext == 1.0) {\n        if (textIn.a == 0.0) {\n            if (selected == 1.0 && squareResolution == 0.0 && (texCoord.x < (0.01 + borderSize) || texCoord.x > (0.99 - borderSize) || texCoord.y < 0.01 || texCoord.y > 0.99)) {\n                colorOut.rgba = vec4(1,0.95,0,0);\n            } else if (selected == 1.0 && squareResolution == 1.0 && (texCoord.y < (0.01 + borderSize) || texCoord.y > (0.99 - borderSize) || texCoord.x < 0.01 || texCoord.x > 0.99)) {\n                colorOut.rgba = vec4(1,0.95,0,0);\n            } else {\n                colorOut.rgba = lut*strength + colorIn*(1.0-strength);\n            }\n        } else {\n            if (selected == 1.0 && textIn.a > 0.8) {\n                colorOut.rgba = vec4(1,0.95,0,0);\n            } else {\n                colorOut.rgba = textIn;\n            } \n        } \n    } else {\n        colorOut.rgba = lut*strength + colorIn*(1.0-strength);\n    } \n    colorOut.a = alpha;\n    if (pressMenu == 1.0) {\n        gl_FragColor = colorOut * vec4(0.68,0.68,0.68,1);\n    } else {        gl_FragColor = colorOut;\n    } \n}";
    private final String ORIGINAL_FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nuniform float alpha;\nvarying vec2 texCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture,texCoord);\n  gl_FragColor.a = alpha;\n}";
    private final String STENCIL_FRAGMENT_SHADER = "precision mediump float;\nvarying vec4 vColor;\nuniform float alpha;\nvoid main() {\n  gl_FragColor = vColor;  gl_FragColor.a = alpha;\n}";
    private final String STENCIL_VERTEX_SHADER = "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nattribute vec4 aColor;\nvarying vec4 vColor;\nvoid main() {\n  gl_Position = uMVPMatrix * aPosition;\n  vColor = aColor;\n}";
    private final String VERTEX_SHADER = "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aTexCoord;\nvarying vec2 texCoord;\nvoid main() {\n  texCoord = (uTexMatrix * aTexCoord).xy;\n  gl_Position = uMVPMatrix * aPosition;\n}";
    private int[] bgTexture = new int[1];
    private int[] camTexture = new int[2];
    private int[] deleteBtnTexture = new int[2];
    private float[] faColPlane = new float[]{0.0f, 0.0f, 0.0f, 0.7f, 0.0f, 0.0f, 0.0f, 0.7f, 0.0f, 0.0f, 0.0f, 0.7f, 0.0f, 0.0f, 0.0f, 0.7f};
    private FloatBuffer fbColPlane;
    private FloatBuffer fbVtxPlane;
    private int[] filmTexture = new int[81];
    private int[] filmTitleTexture = new int[81];
    private int[] hProgram = new int[6];
    public boolean isFPSshow;
    private boolean isRecording;
    private int[] mAlphaLocs = new int[6];
    private Drawable mBackground;
    private float mBorderSize;
    private int mBorderSizeLocs;
    private Object mCamChangeLock = new Object();
    private SurfaceTexture mCamTexture1;
    private SurfaceTexture mCamTexture2;
    private int mCurrentCamera;
    private int mDegree;
    private Drawable[] mDeleteBtnDrawable;
    private FilmEmulatorEGLSurfaceBase mEGLSurfaceBase;
    private String[] mFilmStringList;
    private int[] mFragShader = new int[6];
    private volatile FilmEmulationHandler mHandler;
    private boolean[] mIsFrameAvailable;
    private boolean mIsReverse = false;
    private boolean mIsShowFilmMenu = false;
    private boolean mIsUpdAteFilterNameTexture = false;
    private int mLevel;
    private OnFilmEmulationViewListener mListener = null;
    private int mLutLocs;
    private String[] mLuts;
    private int mPressMenu;
    private int mPressedLUTNumber = -1;
    private float mPreviewRatio;
    private boolean mReady = false;
    private float mRecAspectFrom;
    private float mRecAspectTo;
    private FilmEmulatorEGLSurfaceBase mRecEGLSurfaceBase;
    public int mRecHeight;
    public int mRecIndex;
    private float[] mRecMatrix = new float[16];
    public int mRecWidth;
    private Object mRecordingLock = new Object();
    private Object mRotateTitleLock = new Object();
    private int mSelectedLocs;
    private int mSelectedLut;
    private float mShowText;
    private int mShowTextLocs;
    private float mSquareResolution;
    private int mSquareResolutionLocs;
    private Object mStartLock = new Object();
    private float mStrength;
    private int mStrengthLoc;
    private Surface mSurface;
    public int mSurfaceViewHeight;
    public int mSurfaceViewWidth;
    private int mTextLocs;
    private float mThumbMenuRatio = 1.0f;
    private int[] mVertexShader = new int[6];
    private int[] maPositionLocs = new int[6];
    private int[] maTextureCoordLocs = new int[6];
    private int[] msTexture = new int[6];
    private int[] muMVPMatrixLocs = new int[6];
    private int[] muTexMatrixLocs = new int[6];
    private float[] mvpMatrix = new float[16];
    private FloatBuffer pTexCoord;
    private FloatBuffer pVertex;
    private Rect[] viewRect = new Rect[81];

    /* renamed from: com.lge.filmemulatorengine.FilmEmulatorRendererPreview$1 */
    class C14401 implements OnFrameAvailableListener {
        C14401() {
        }

        public void onFrameAvailable(SurfaceTexture arg0) {
            FilmEmulatorRendererPreview.this.mIsFrameAvailable[0] = true;
            FilmEmulatorRendererPreview.this.mHandler.sendFrameAvailable();
        }
    }

    /* renamed from: com.lge.filmemulatorengine.FilmEmulatorRendererPreview$2 */
    class C14412 implements OnFrameAvailableListener {
        C14412() {
        }

        public void onFrameAvailable(SurfaceTexture arg0) {
            FilmEmulatorRendererPreview.this.mIsFrameAvailable[1] = true;
            FilmEmulatorRendererPreview.this.mHandler.sendFrameAvailable();
        }
    }

    /* renamed from: com.lge.filmemulatorengine.FilmEmulatorRendererPreview$3 */
    class C14423 implements OnFrameAvailableListener {
        C14423() {
        }

        public void onFrameAvailable(SurfaceTexture arg0) {
            FilmEmulatorRendererPreview.this.mIsFrameAvailable[0] = true;
            FilmEmulatorRendererPreview.this.mHandler.sendFrameAvailable();
        }
    }

    public class FilmEmulationHandler extends Handler {
        private static final int MSG_CHANGE_BACKGROUND = 14;
        private static final int MSG_CHANGE_PICTURESIZE = 12;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_REC_PREPARE = 9;
        private static final int MSG_REC_START = 10;
        private static final int MSG_REC_STOP = 11;
        private static final int MSG_REMOVE_DOWNLOADED_LUT = 13;
        private static final int MSG_RESET_LUT = 15;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_CREATED = 0;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final String TAG = "FilmEmulationHandler";
        private WeakReference<FilmEmulatorRendererPreview> mWeakRenderThread;

        public FilmEmulationHandler(FilmEmulatorRendererPreview rt) {
            this.mWeakRenderThread = new WeakReference(rt);
        }

        public void sendSurfaceCreated(Surface surface) {
            sendMessage(obtainMessage(0, surface));
        }

        public void sendSurfaceChanged(SurfaceHolder holder, int width, int height) {
            sendMessage(obtainMessage(1, width, height, holder));
        }

        public void sendSurfaceDestroyed(SurfaceHolder holder) {
            sendMessage(obtainMessage(2, holder));
        }

        public void sendShutdown() {
            removeMessages(4);
            sendMessage(obtainMessage(3));
        }

        public void sendFrameAvailable() {
            sendMessage(obtainMessage(4));
        }

        public void sendPrepareRecording(Surface sur, int width, int height, int idx, boolean isCinema, int flagReverse) {
            removeMessages(4);
            HashMap<String, Object> map = new HashMap();
            map.put(HybridViewConfig.SURFACE, sur);
            map.put("width", Integer.valueOf(width));
            map.put("height", Integer.valueOf(height));
            map.put("index", Integer.valueOf(idx));
            map.put("isCinema", Boolean.valueOf(isCinema));
            map.put("flagReverse", Integer.valueOf(flagReverse));
            sendMessage(obtainMessage(9, map));
        }

        public void sendStartRecording() {
            removeMessages(4);
            sendMessage(obtainMessage(10));
        }

        public void sendStopRecording() {
            removeMessages(4);
            sendMessage(obtainMessage(11));
        }

        public void removeDownloadedLUT(int lutNum) {
            removeMessages(4);
            sendMessage(obtainMessage(13, lutNum, 0));
        }

        public void resetLutView() {
            removeMessages(4);
            sendMessage(obtainMessage(15));
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            FilmEmulatorRendererPreview renderThread = (FilmEmulatorRendererPreview) this.mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }
            switch (what) {
                case 0:
                    renderThread.filmInit((Surface) msg.obj);
                    return;
                case 1:
                    renderThread.handledSurfaceChanged((SurfaceHolder) msg.obj, msg.arg1, msg.arg2);
                    return;
                case 2:
                    return;
                case 3:
                    Log.i(TAG, "MSG_SHUTDOWN");
                    removeMessages(4);
                    renderThread.shutdown();
                    return;
                case 4:
                    if (FilmEmulatorRendererPreview.this.mIsShowFilmMenu && !FilmEmulatorRendererPreview.this.mIsUpdAteFilterNameTexture) {
                        renderThread.onDrawFrame();
                        sendEmptyMessage(4);
                        return;
                    } else if (FilmEmulatorRendererPreview.this.mIsFrameAvailable[0]) {
                        renderThread.onDrawFrame();
                        sendEmptyMessage(4);
                        FilmEmulatorRendererPreview.this.mIsFrameAvailable[0] = false;
                        return;
                    } else {
                        return;
                    }
                case 9:
                    Log.i(TAG, "MSG_REC_PREPARE");
                    removeMessages(4);
                    HashMap<String, Object> map = msg.obj;
                    renderThread.prepareRecording((Surface) map.get(HybridViewConfig.SURFACE), ((Integer) map.get("width")).intValue(), ((Integer) map.get("height")).intValue(), ((Integer) map.get("index")).intValue(), ((Boolean) map.get("isCinema")).booleanValue(), ((Integer) map.get("flagReverse")).intValue());
                    sendEmptyMessage(4);
                    return;
                case 10:
                    Log.i(TAG, "MSG_REC_START");
                    renderThread.startRecording();
                    renderThread.setFrameAvailableCallback();
                    return;
                case 11:
                    Log.i(TAG, "MSG_REC_STOP");
                    renderThread.stopRecording();
                    return;
                case 12:
                    Log.i(TAG, "MSG_CHANGE_PICTURESIZE");
                    renderThread.updateFilterNameTexture();
                    renderThread.onDrawFrame();
                    FilmEmulatorRendererPreview.this.mIsUpdAteFilterNameTexture = false;
                    return;
                case 13:
                    Log.i(TAG, "MSG_REMOVE_DOWNLOADED_LUT");
                    FilmEmulatorRendererPreview.this.resetLutViewRect(msg.arg1);
                    return;
                case 14:
                    FilmEmulatorRendererPreview.this.initBackground(FilmEmulatorRendererPreview.this.mBackground, FilmEmulatorRendererPreview.this.mSurfaceViewWidth, FilmEmulatorRendererPreview.this.mSurfaceViewHeight);
                    return;
                case 15:
                    FilmEmulatorRendererPreview.this.resetLutViewRect();
                    return;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
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
            Log.d(TAG, name + " fps=" + ((((float) sFrameCount) * 1.0E9f) / ((float) sTotalTime)));
            sFrameCount = 0;
            sStartTick = currentTick;
            sTotalTime = 0;
        }
    }

    private static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != 0) {
            Log.e(TAG, op + ": glError 0x" + Integer.toHexString(error));
        }
    }

    private static float[] calcFragCoordForCrop(float from, float to) {
        if (from > to) {
            float dy = (from - to) / (2.0f * from);
            return new float[]{0.0f, dy, 1.0f, dy, 0.0f, 1.0f - dy, 1.0f, 1.0f - dy};
        }
        float dx = (to - from) / (2.0f * to);
        return new float[]{dx, 0.0f, 1.0f - dx, 0.0f, dx, 1.0f, 1.0f - dx, 1.0f};
    }

    public FilmEmulatorRendererPreview(Surface surface, String[] luts, int level, boolean reverse, Drawable[] deleteBtnDrawable, Drawable background, OnFilmEmulationViewListener listener, float previewRatio, String[] filmNames, int downloadIndex) {
        this.mSurface = surface;
        this.mCurrentCamera = 0;
        this.mLuts = luts;
        this.mLevel = level;
        this.mIsReverse = reverse;
        this.isRecording = false;
        this.isFPSshow = false;
        this.mListener = listener;
        this.mDeleteBtnDrawable = deleteBtnDrawable;
        this.mBackground = background;
        this.mStrength = 1.0f;
        this.mShowText = 0.0f;
        this.mSelectedLut = 0;
        this.mPreviewRatio = previewRatio;
        float borderSize = ((this.mPreviewRatio - this.mThumbMenuRatio) / 2.0f) / this.mPreviewRatio;
        if (this.mPreviewRatio > this.mThumbMenuRatio) {
            this.mSquareResolution = 0.0f;
        } else {
            borderSize = ((this.mThumbMenuRatio - this.mPreviewRatio) / 2.0f) / this.mThumbMenuRatio;
            this.mSquareResolution = 1.0f;
        }
        this.mBorderSize = ((float) Math.round(1000.0f * borderSize)) / 1000.0f;
        this.pVertex = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pVertex.put(RECTANGLE_VERTEX_COORDS);
        this.pVertex.position(0);
        this.pTexCoord = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(RECTANGLE_FRAG_COORDS);
        this.pTexCoord.position(0);
        this.fbVtxPlane = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.fbVtxPlane.put(RECTANGLE_VERTEX_COORDS);
        this.fbVtxPlane.position(0);
        this.fbColPlane = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.fbColPlane.put(this.faColPlane);
        this.fbColPlane.position(0);
        for (int i = 0; i < this.viewRect.length; i++) {
            this.viewRect[i] = new Rect();
        }
        this.mFilmStringList = filmNames;
    }

    public void run() {
        Log.d(TAG, "START Film Thread");
        Looper.prepare();
        this.mHandler = new FilmEmulationHandler(this);
        filmInit(this.mSurface);
        synchronized (this.mStartLock) {
            this.mReady = true;
            this.mStartLock.notify();
        }
        Log.d(TAG, "looper start");
        Looper.loop();
        Log.d(TAG, "looper quit");
        release();
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
                    Log.w(TAG, "waitUntilReady - InterruptedException");
                }
            }
        }
    }

    public FilmEmulationHandler getHanlder() {
        return this.mHandler;
    }

    public int getNearestView(int x, int y, int ignoreIndex) {
        int r = -1;
        y = this.mSurfaceViewHeight - y;
        int i = 0;
        while (i < this.viewRect.length) {
            if (i != ignoreIndex && x >= this.viewRect[i].left && x <= this.viewRect[i].right && y >= this.viewRect[i].top && y <= this.viewRect[i].bottom) {
                r = i;
                break;
            }
            i++;
        }
        if (r != -1 || ignoreIndex == -1) {
            return r;
        }
        return ignoreIndex;
    }

    public boolean getDeleteView(int x, int y, int pressedIndex, int degree) {
        if (pressedIndex >= this.viewRect.length || pressedIndex < 0) {
            return false;
        }
        y = this.mSurfaceViewHeight - y;
        if (degree == 0 && x >= this.viewRect[pressedIndex].left + ((this.viewRect[pressedIndex].width() * 3) / 4) && x <= this.viewRect[pressedIndex].right && y >= this.viewRect[pressedIndex].top + ((this.viewRect[pressedIndex].height() * 3) / 4) && y <= this.viewRect[pressedIndex].bottom) {
            return true;
        }
        if (degree == 270 && x >= this.viewRect[pressedIndex].left + ((this.viewRect[pressedIndex].width() * 3) / 4) && x <= this.viewRect[pressedIndex].right && y >= this.viewRect[pressedIndex].top && y <= this.viewRect[pressedIndex].bottom - ((this.viewRect[pressedIndex].height() * 3) / 4)) {
            return true;
        }
        if (degree == 90 && x >= this.viewRect[pressedIndex].left && x <= this.viewRect[pressedIndex].right - ((this.viewRect[pressedIndex].width() * 3) / 4) && y >= this.viewRect[pressedIndex].top + ((this.viewRect[pressedIndex].height() * 3) / 4) && y <= this.viewRect[pressedIndex].bottom) {
            return true;
        }
        if (degree != 180 || x < this.viewRect[pressedIndex].left || x > this.viewRect[pressedIndex].right - ((this.viewRect[pressedIndex].width() * 3) / 4) || y < this.viewRect[pressedIndex].top || y > this.viewRect[pressedIndex].bottom - ((this.viewRect[pressedIndex].height() * 3) / 4)) {
            return false;
        }
        return true;
    }

    public void drawFrame(int tex, boolean touchable, float alpha, float strength, float aspectFrom, float aspectTo, boolean isFocusPeaking, float showText) {
        if (touchable) {
            int[] r = new int[4];
            GLES20.glGetIntegerv(2978, r, 0);
            this.viewRect[tex].left = r[0] + 10;
            this.viewRect[tex].top = r[1] + 10;
            this.viewRect[tex].right = (r[0] + r[2]) - 20;
            this.viewRect[tex].bottom = (r[1] + r[3]) - 20;
        }
        synchronized (this.mRotateTitleLock) {
            if (this.mIsUpdAteFilterNameTexture) {
                updateFilterNameTexture();
                this.mIsUpdAteFilterNameTexture = false;
            }
        }
        drawFrame(tex, this.mvpMatrix, alpha, strength, aspectFrom, aspectTo, isFocusPeaking, showText);
    }

    public void enableStencilBuffer() {
        GLES20.glEnable(2960);
        GLES20.glColorMask(false, false, false, false);
        GLES20.glStencilFunc(519, 1, 1);
        GLES20.glStencilOp(7681, 7681, 7681);
    }

    public void drawPlaneAndDisableStencilBuffer() {
        GLES20.glUseProgram(this.hProgram[5]);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[5], 1, false, this.mvpMatrix, 0);
        GLES20.glEnableVertexAttribArray(this.maPositionLocs[5]);
        GLES20.glVertexAttribPointer(this.maPositionLocs[5], 2, 5126, false, 0, this.fbVtxPlane);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[5]);
        GLES20.glVertexAttribPointer(this.maTextureCoordLocs[5], 4, 5126, false, 0, this.fbColPlane);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glDisableVertexAttribArray(this.maPositionLocs[5]);
        GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[5]);
        GLES20.glColorMask(true, true, true, true);
        GLES20.glStencilFunc(514, 1, 1);
        GLES20.glStencilOp(Constant.PHOTO_EFFECT_ID_SKETCH, Constant.PHOTO_EFFECT_ID_SKETCH, Constant.PHOTO_EFFECT_ID_SKETCH);
    }

    public void disableStencilBuffer() {
        GLES20.glDisable(2960);
    }

    public void drawBackground(float alpha) {
        GLES20.glUseProgram(this.hProgram[5]);
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[5], 1, false, this.mvpMatrix, 0);
        GLES20.glUniform1f(this.mAlphaLocs[5], alpha);
        GLES20.glEnableVertexAttribArray(this.maPositionLocs[5]);
        GLES20.glVertexAttribPointer(this.maPositionLocs[5], 2, 5126, false, 0, this.fbVtxPlane);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[5]);
        GLES20.glVertexAttribPointer(this.maTextureCoordLocs[5], 4, 5126, false, 0, this.fbColPlane);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glDisable(3042);
        GLES20.glDisableVertexAttribArray(this.maPositionLocs[5]);
        GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[5]);
    }

    public void drawFrame(int tex, float[] pMatrix, float alpha, float strength, float aspectFrom, float aspectTo, boolean isFocus, float showText) {
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        this.mStrength = strength;
        this.mShowText = showText;
        Buffer texCoord = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoord.put(calcFragCoordForCrop(aspectFrom, aspectTo));
        texCoord.position(0);
        float[] curTransMat = new float[16];
        getCurrentCameraTexture().getTransformMatrix(curTransMat);
        if (tex != 0 || this.mShowText == 1.0f) {
            GLES20.glUseProgram(this.hProgram[1]);
            GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[1], 1, false, pMatrix, 0);
            GLES20.glUniformMatrix4fv(this.muTexMatrixLocs[1], 1, false, curTransMat, 0);
            GLES20.glEnableVertexAttribArray(this.maPositionLocs[1]);
            GLES20.glVertexAttribPointer(this.maPositionLocs[1], 2, 5126, false, 8, this.pVertex);
            GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[1]);
            GLES20.glVertexAttribPointer(this.maTextureCoordLocs[1], 2, 5126, false, 8, texCoord);
            synchronized (this.mCamChangeLock) {
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(36197, 0);
                GLES20.glBindTexture(36197, this.camTexture[this.mCurrentCamera]);
                GLES20.glUniform1i(this.msTexture[1], 0);
            }
            GLES20.glActiveTexture(33985);
            GLES30.glBindTexture(32879, this.filmTexture[tex]);
            GLES20.glUniform1i(this.mLutLocs, 1);
            GLES20.glUniform1f(this.mStrengthLoc, strength);
            GLES20.glUniform1f(this.mAlphaLocs[1], alpha);
            GLES20.glUniform1f(this.mShowTextLocs, showText);
            if (tex == this.mSelectedLut) {
                GLES20.glUniform1f(this.mSelectedLocs, 1.0f);
            } else {
                GLES20.glUniform1f(this.mSelectedLocs, 0.0f);
            }
            GLES20.glUniform1f(this.mBorderSizeLocs, this.mBorderSize);
            GLES20.glUniform1f(this.mSquareResolutionLocs, this.mSquareResolution);
            if (showText <= 0.0f || tex != this.mPressedLUTNumber) {
                GLES20.glUniform1f(this.mPressMenu, 0.0f);
            } else {
                GLES20.glUniform1f(this.mPressMenu, 1.0f);
            }
            GLES20.glActiveTexture(33986);
            GLES30.glBindTexture(3553, this.filmTitleTexture[tex]);
            GLES20.glUniform1i(this.mTextLocs, 2);
            GLES20.glDrawArrays(5, 0, 4);
            GLES20.glDisableVertexAttribArray(this.maPositionLocs[1]);
            GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[1]);
            GLES20.glBindTexture(3553, 0);
            GLES30.glBindTexture(32879, 0);
        } else {
            GLES20.glUseProgram(this.hProgram[0]);
            GLES20.glUniform1f(this.mAlphaLocs[0], alpha);
            GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[0], 1, false, pMatrix, 0);
            GLES20.glUniformMatrix4fv(this.muTexMatrixLocs[0], 1, false, curTransMat, 0);
            GLES20.glEnableVertexAttribArray(this.maPositionLocs[0]);
            GLES20.glVertexAttribPointer(this.maPositionLocs[0], 2, 5126, false, 8, this.pVertex);
            GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[0]);
            GLES20.glVertexAttribPointer(this.maTextureCoordLocs[0], 2, 5126, false, 8, texCoord);
            synchronized (this.mCamChangeLock) {
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(36197, 0);
                GLES20.glBindTexture(36197, this.camTexture[this.mCurrentCamera]);
                GLES20.glUniform1i(this.msTexture[0], 0);
            }
            GLES20.glDrawArrays(5, 0, 4);
            GLES20.glDisableVertexAttribArray(this.maPositionLocs[0]);
            GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[0]);
            GLES20.glBindTexture(3553, 0);
        }
        GLES20.glUseProgram(0);
        GLES20.glDisable(3042);
        checkGlError("FilmEmulator Draw error");
    }

    public void drawDeleteButton(float alpha, boolean isPressed) {
        float[] curTransMat = new float[16];
        getCurrentCameraTexture().getTransformMatrix(curTransMat);
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        int programIndex = 2;
        int textureIndex = 0;
        if (isPressed) {
            programIndex = 3;
            textureIndex = 1;
        }
        GLES20.glUseProgram(this.hProgram[programIndex]);
        GLES20.glUniform1f(this.mAlphaLocs[programIndex], alpha);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[programIndex], 1, false, this.mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLocs[programIndex], 1, false, curTransMat, 0);
        GLES20.glEnableVertexAttribArray(this.maPositionLocs[programIndex]);
        GLES20.glVertexAttribPointer(this.maPositionLocs[programIndex], 2, 5126, false, 8, this.pVertex);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[programIndex]);
        GLES20.glVertexAttribPointer(this.maTextureCoordLocs[programIndex], 2, 5126, false, 8, this.pTexCoord);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, 0);
        GLES20.glBindTexture(3553, this.deleteBtnTexture[textureIndex]);
        GLES20.glUniform1i(this.msTexture[programIndex], 0);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glDisableVertexAttribArray(this.maPositionLocs[programIndex]);
        GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[programIndex]);
        GLES20.glBindTexture(3553, 0);
        GLES20.glUseProgram(0);
        GLES20.glDisable(3042);
    }

    public void drawPreviewBackground(float alpha) {
        if (this.mBackground != null) {
            float[] curTransMat = new float[16];
            getCurrentCameraTexture().getTransformMatrix(curTransMat);
            GLES20.glEnable(3042);
            GLES20.glBlendFunc(770, 771);
            GLES20.glUseProgram(this.hProgram[4]);
            GLES20.glUniform1f(this.mAlphaLocs[4], alpha);
            GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[4], 1, false, this.mvpMatrix, 0);
            GLES20.glUniformMatrix4fv(this.muTexMatrixLocs[4], 1, false, curTransMat, 0);
            GLES20.glEnableVertexAttribArray(this.maPositionLocs[4]);
            GLES20.glVertexAttribPointer(this.maPositionLocs[4], 2, 5126, false, 8, this.pVertex);
            GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[4]);
            GLES20.glVertexAttribPointer(this.maTextureCoordLocs[4], 2, 5126, false, 8, this.pTexCoord);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, 0);
            GLES20.glBindTexture(3553, this.bgTexture[0]);
            GLES20.glUniform1i(this.msTexture[4], 0);
            GLES20.glDrawArrays(5, 0, 4);
            GLES20.glDisableVertexAttribArray(this.maPositionLocs[4]);
            GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[4]);
            GLES20.glBindTexture(3553, 0);
            GLES20.glUseProgram(0);
            GLES20.glDisable(3042);
        }
    }

    @SuppressLint({"WrongCall"})
    protected void onDrawFrame() {
        try {
            this.mEGLSurfaceBase.makeContextCurrent();
            this.mCamTexture1.updateTexImage();
            this.mCamTexture2.updateTexImage();
            if (this.mListener != null) {
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(17408);
                this.mListener.onDraw(this);
                this.mEGLSurfaceBase.swapBuffers();
                if (this.isFPSshow) {
                    checkFPS("onDrawFrame");
                    throw new RuntimeException();
                }
            }
            synchronized (this.mRecordingLock) {
                if (this.isRecording) {
                    this.mRecEGLSurfaceBase.makeContextCurrent();
                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                    GLES20.glClear(17408);
                    GLES20.glViewport(0, 0, this.mRecWidth, this.mRecHeight);
                    drawFrame(this.mRecIndex, this.mRecMatrix, 1.0f, this.mStrength, this.mRecAspectFrom, this.mRecAspectTo, false, this.mShowText);
                    this.mRecEGLSurfaceBase.swapBuffers();
                }
            }
        } catch (RuntimeException e) {
            Log.d(TAG, "cannot draw without surface : " + e.getMessage());
            if (this.mCamTexture1 != null) {
                this.mCamTexture1.setOnFrameAvailableListener(null);
            }
            if (this.mCamTexture2 != null) {
                this.mCamTexture2.setOnFrameAvailableListener(null);
            }
            this.mListener.onErrorOccured(0);
            shutdown();
        }
    }

    protected void release() {
        Log.d(TAG, "Textures release...");
        if (this.mCamTexture1 != null) {
            this.mCamTexture1.release();
            this.mCamTexture1 = null;
        }
        if (this.mCamTexture2 != null) {
            this.mCamTexture2.release();
            this.mCamTexture2 = null;
        }
        deleteTex();
        Log.d(TAG, "EGL release...");
        if (this.mRecEGLSurfaceBase != null) {
            this.mRecEGLSurfaceBase.makeContextCurrent();
            this.mRecEGLSurfaceBase.releaseWithoutSurface();
            this.mRecEGLSurfaceBase = null;
        }
        if (this.mEGLSurfaceBase != null) {
            this.mEGLSurfaceBase.release();
            this.mEGLSurfaceBase = null;
        }
    }

    private void deleteTex() {
        for (int i = 0; i < 6; i++) {
            GLES20.glDeleteProgram(this.hProgram[i]);
        }
        GLES20.glDeleteTextures(this.camTexture.length, this.camTexture, 0);
        GLES20.glDeleteTextures(this.filmTexture.length, this.filmTexture, 0);
        GLES20.glDeleteTextures(this.deleteBtnTexture.length, this.deleteBtnTexture, 0);
        GLES20.glDeleteTextures(this.filmTitleTexture.length, this.filmTitleTexture, 0);
        GLES20.glDeleteTextures(this.mLuts.length, this.filmTitleTexture, 0);
        GLES20.glDeleteTextures(this.bgTexture.length, this.bgTexture, 0);
    }

    private int loadShader(int programID, String vss, String fss) {
        this.mVertexShader[programID] = GLES20.glCreateShader(35633);
        GLES20.glShaderSource(this.mVertexShader[programID], vss);
        GLES20.glCompileShader(this.mVertexShader[programID]);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(this.mVertexShader[programID], 35713, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile vshader");
            Log.v("Shader", "Could not compile vshader:" + GLES20.glGetShaderInfoLog(this.mVertexShader[programID]));
            GLES20.glDeleteShader(this.mVertexShader[programID]);
            this.mVertexShader[programID] = 0;
        }
        this.mFragShader[programID] = GLES20.glCreateShader(35632);
        GLES20.glShaderSource(this.mFragShader[programID], fss);
        GLES20.glCompileShader(this.mFragShader[programID]);
        GLES20.glGetShaderiv(this.mFragShader[programID], 35713, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile fshader");
            Log.v("Shader", "Could not compile fshader:" + GLES20.glGetShaderInfoLog(this.mFragShader[programID]));
            GLES20.glDeleteShader(this.mFragShader[programID]);
            this.mFragShader[programID] = 0;
        }
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, this.mVertexShader[programID]);
        GLES20.glAttachShader(program, this.mFragShader[programID]);
        GLES20.glLinkProgram(program);
        return program;
    }

    protected void filmInit(Surface surface) {
        this.mEGLSurfaceBase = new FilmEmulatorEGLSurfaceBase(null, surface, 0);
        this.mEGLSurfaceBase.makeContextCurrent();
        GLES20.glGenTextures(this.camTexture.length, this.camTexture, 0);
        initCameraTexture(0);
        initCameraTexture(1);
        this.mIsFrameAvailable = new boolean[2];
        Arrays.fill(this.mIsFrameAvailable, false);
        this.mCamTexture1 = new SurfaceTexture(this.camTexture[0]);
        this.mCamTexture1.setOnFrameAvailableListener(new C14401());
        this.mCamTexture2 = new SurfaceTexture(this.camTexture[1]);
        this.mCamTexture2.setOnFrameAvailableListener(new C14412());
        this.mListener.onEngineInitializeDone(this.mCamTexture1, this.mCamTexture2);
        GLES20.glGenTextures(this.mLuts.length, this.filmTexture, 0);
        String[] strArr = this.mLuts;
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            int i3 = i2 + 1;
            init3DTex(strArr[i], i2, this.mLevel);
            i++;
            i2 = i3;
        }
        GLES20.glGenTextures(this.mLuts.length, this.filmTitleTexture, 0);
        updateFilterNameTexture();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        this.hProgram[0] = loadShader(0, "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aTexCoord;\nvarying vec2 texCoord;\nvoid main() {\n  texCoord = (uTexMatrix * aTexCoord).xy;\n  gl_Position = uMVPMatrix * aPosition;\n}", "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nuniform float alpha;\nvarying vec2 texCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture,texCoord);\n  gl_FragColor.a = alpha;\n}");
        this.hProgram[1] = loadShader(1, "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aTexCoord;\nvarying vec2 texCoord;\nvoid main() {\n  texCoord = (uTexMatrix * aTexCoord).xy;\n  gl_Position = uMVPMatrix * aPosition;\n}", "#extension GL_OES_texture_3D : enable\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nprecision mediump sampler3D;\nuniform samplerExternalOES sTexture;\nuniform sampler3D lut;\nuniform sampler2D text;\nuniform sampler2D deleteBtn;\nvarying vec2 texCoord;\nuniform float alpha;\nuniform float strength;\nuniform float showtext;\nuniform float selected;\nuniform float borderSize;\nuniform float pressMenu;\nuniform float drawDeleteBtn;\nuniform float squareResolution;\nvoid main() {\n    vec4 colorIn = texture2D(sTexture, texCoord);\n    vec4 lut = texture3D(lut, colorIn.rgb);\n    vec4 textIn = texture2D(text, texCoord);\n    vec4 deleteBtnIn = texture2D(deleteBtn, texCoord);\n    vec4 colorOut;\n    if (showtext == 1.0) {\n        if (textIn.a == 0.0) {\n            if (selected == 1.0 && squareResolution == 0.0 && (texCoord.x < (0.01 + borderSize) || texCoord.x > (0.99 - borderSize) || texCoord.y < 0.01 || texCoord.y > 0.99)) {\n                colorOut.rgba = vec4(1,0.95,0,0);\n            } else if (selected == 1.0 && squareResolution == 1.0 && (texCoord.y < (0.01 + borderSize) || texCoord.y > (0.99 - borderSize) || texCoord.x < 0.01 || texCoord.x > 0.99)) {\n                colorOut.rgba = vec4(1,0.95,0,0);\n            } else {\n                colorOut.rgba = lut*strength + colorIn*(1.0-strength);\n            }\n        } else {\n            if (selected == 1.0 && textIn.a > 0.8) {\n                colorOut.rgba = vec4(1,0.95,0,0);\n            } else {\n                colorOut.rgba = textIn;\n            } \n        } \n    } else {\n        colorOut.rgba = lut*strength + colorIn*(1.0-strength);\n    } \n    colorOut.a = alpha;\n    if (pressMenu == 1.0) {\n        gl_FragColor = colorOut * vec4(0.68,0.68,0.68,1);\n    } else {        gl_FragColor = colorOut;\n    } \n}");
        this.hProgram[2] = loadShader(2, "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aTexCoord;\nvarying vec2 texCoord;\nvoid main() {\n  texCoord = (uTexMatrix * aTexCoord).xy;\n  gl_Position = uMVPMatrix * aPosition;\n}", "precision mediump float;\nuniform sampler2D sTexture;\nuniform float alpha;\nvarying vec2 texCoord;\nvoid main() {\n    vec4 colorOut = texture2D(sTexture,texCoord);\n    if (colorOut.a < 0.2) {\n        discard;\n    }\n    gl_FragColor = colorOut;\n    gl_FragColor.a = alpha;\n}");
        this.hProgram[3] = loadShader(2, "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aTexCoord;\nvarying vec2 texCoord;\nvoid main() {\n  texCoord = (uTexMatrix * aTexCoord).xy;\n  gl_Position = uMVPMatrix * aPosition;\n}", "precision mediump float;\nuniform sampler2D sTexture;\nuniform float alpha;\nvarying vec2 texCoord;\nvoid main() {\n    vec4 colorOut = texture2D(sTexture,texCoord);\n    if (colorOut.a < 0.2) {\n        discard;\n    }\n    gl_FragColor = colorOut;\n    gl_FragColor.a = alpha;\n}");
        this.hProgram[4] = loadShader(3, "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aTexCoord;\nvarying vec2 texCoord;\nvoid main() {\n  texCoord = (uTexMatrix * aTexCoord).xy;\n  gl_Position = uMVPMatrix * aPosition;\n}", "precision mediump float;\nuniform sampler2D sTexture;\nuniform float alpha;\nvarying vec2 texCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture,texCoord);\n  gl_FragColor.a = alpha;\n}");
        this.hProgram[5] = loadShader(4, "attribute vec4 aPosition;\nuniform mat4 uMVPMatrix;\nattribute vec4 aColor;\nvarying vec4 vColor;\nvoid main() {\n  gl_Position = uMVPMatrix * aPosition;\n  vColor = aColor;\n}", "precision mediump float;\nvarying vec4 vColor;\nuniform float alpha;\nvoid main() {\n  gl_FragColor = vColor;  gl_FragColor.a = alpha;\n}");
        initLocs();
    }

    private void initLocs() {
        for (int i = 0; i < 5; i++) {
            this.maPositionLocs[i] = GLES20.glGetAttribLocation(this.hProgram[i], "aPosition");
            this.muMVPMatrixLocs[i] = GLES20.glGetUniformLocation(this.hProgram[i], "uMVPMatrix");
            this.maTextureCoordLocs[i] = GLES20.glGetAttribLocation(this.hProgram[i], "aTexCoord");
            this.muTexMatrixLocs[i] = GLES20.glGetUniformLocation(this.hProgram[i], "uTexMatrix");
            this.msTexture[i] = GLES20.glGetUniformLocation(this.hProgram[i], "sTexture");
            this.mAlphaLocs[i] = GLES20.glGetUniformLocation(this.hProgram[i], AnimationManager.ANI_ALPHA);
        }
        this.maPositionLocs[5] = GLES20.glGetAttribLocation(this.hProgram[5], "aPosition");
        this.muMVPMatrixLocs[5] = GLES20.glGetUniformLocation(this.hProgram[5], "uMVPMatrix");
        this.maTextureCoordLocs[5] = GLES20.glGetAttribLocation(this.hProgram[5], "aColor");
        this.mAlphaLocs[5] = GLES20.glGetUniformLocation(this.hProgram[5], AnimationManager.ANI_ALPHA);
        this.mStrengthLoc = GLES20.glGetUniformLocation(this.hProgram[1], "strength");
        this.mLutLocs = GLES20.glGetUniformLocation(this.hProgram[1], "lut");
        this.mTextLocs = GLES20.glGetUniformLocation(this.hProgram[1], ISceneCategory.CATEGORY_ID_TEXT);
        this.mShowTextLocs = GLES20.glGetUniformLocation(this.hProgram[1], "showtext");
        this.mSelectedLocs = GLES20.glGetUniformLocation(this.hProgram[1], "selected");
        this.mBorderSizeLocs = GLES20.glGetUniformLocation(this.hProgram[1], "borderSize");
        this.mSquareResolutionLocs = GLES20.glGetUniformLocation(this.hProgram[1], "squareResolution");
        this.mPressMenu = GLES20.glGetUniformLocation(this.hProgram[1], "pressMenu");
        checkGlError("initLocs");
    }

    private void initCameraTexture(int camID) {
        GLES20.glBindTexture(36197, this.camTexture[camID]);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        GLES20.glTexParameteri(36197, 10241, 9729);
        GLES20.glTexParameteri(36197, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00aa A:{SYNTHETIC, Splitter: B:25:0x00aa} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00af A:{SYNTHETIC, Splitter: B:28:0x00af} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c2 A:{SYNTHETIC, Splitter: B:36:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00c7 A:{SYNTHETIC, Splitter: B:39:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c2 A:{SYNTHETIC, Splitter: B:36:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00c7 A:{SYNTHETIC, Splitter: B:39:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00aa A:{SYNTHETIC, Splitter: B:25:0x00aa} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00af A:{SYNTHETIC, Splitter: B:28:0x00af} */
    private void init3DTex(java.lang.String r19, int r20, int r21) {
        /*
        r18 = this;
        r2 = r21 * r21;
        r2 = r2 * r21;
        r2 = r2 * 3;
        r14 = new byte[r2];
        r16 = 0;
        r12 = 0;
        r17 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x00a4 }
        r0 = r17;
        r1 = r19;
        r0.<init>(r1);	 Catch:{ Exception -> 0x00a4 }
        r13 = new java.io.BufferedInputStream;	 Catch:{ Exception -> 0x00de, all -> 0x00d5 }
        r0 = r17;
        r13.<init>(r0);	 Catch:{ Exception -> 0x00de, all -> 0x00d5 }
        r2 = 0;
        r3 = r14.length;	 Catch:{ Exception -> 0x00e2, all -> 0x00d9 }
        r13.read(r14, r2, r3);	 Catch:{ Exception -> 0x00e2, all -> 0x00d9 }
        if (r13 == 0) goto L_0x0025;
    L_0x0022:
        r13.close();	 Catch:{ IOException -> 0x0097 }
    L_0x0025:
        if (r17 == 0) goto L_0x00e7;
    L_0x0027:
        r17.close();	 Catch:{ IOException -> 0x009c }
        r12 = r13;
        r16 = r17;
    L_0x002d:
        r2 = r21 * r21;
        r2 = r2 * r21;
        r2 = r2 * 3;
        r11 = java.nio.ByteBuffer.allocateDirect(r2);
        r2 = r11.put(r14);
        r3 = 0;
        r2.position(r3);
        r2 = 32879; // 0x806f float:4.6073E-41 double:1.62444E-319;
        r0 = r18;
        r3 = r0.filmTexture;
        r3 = r3[r20];
        android.opengl.GLES30.glBindTexture(r2, r3);
        r2 = 32879; // 0x806f float:4.6073E-41 double:1.62444E-319;
        r3 = 0;
        r4 = 32849; // 0x8051 float:4.6031E-41 double:1.62296E-319;
        r8 = 0;
        r9 = 6407; // 0x1907 float:8.978E-42 double:3.1655E-320;
        r10 = 5121; // 0x1401 float:7.176E-42 double:2.53E-320;
        r5 = r21;
        r6 = r21;
        r7 = r21;
        android.opengl.GLES30.glTexImage3D(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11);
        r2 = 32879; // 0x806f float:4.6073E-41 double:1.62444E-319;
        r3 = 10241; // 0x2801 float:1.435E-41 double:5.0597E-320;
        r4 = 9729; // 0x2601 float:1.3633E-41 double:4.807E-320;
        android.opengl.GLES30.glTexParameteri(r2, r3, r4);
        r2 = 32879; // 0x806f float:4.6073E-41 double:1.62444E-319;
        r3 = 10240; // 0x2800 float:1.4349E-41 double:5.059E-320;
        r4 = 9729; // 0x2601 float:1.3633E-41 double:4.807E-320;
        android.opengl.GLES30.glTexParameteri(r2, r3, r4);
        r2 = 32879; // 0x806f float:4.6073E-41 double:1.62444E-319;
        r3 = 10242; // 0x2802 float:1.4352E-41 double:5.06E-320;
        r4 = 33071; // 0x812f float:4.6342E-41 double:1.6339E-319;
        android.opengl.GLES30.glTexParameteri(r2, r3, r4);
        r2 = 32879; // 0x806f float:4.6073E-41 double:1.62444E-319;
        r3 = 10243; // 0x2803 float:1.4354E-41 double:5.0607E-320;
        r4 = 33071; // 0x812f float:4.6342E-41 double:1.6339E-319;
        android.opengl.GLES30.glTexParameteri(r2, r3, r4);
        r2 = 32879; // 0x806f float:4.6073E-41 double:1.62444E-319;
        r3 = 32882; // 0x8072 float:4.6077E-41 double:1.6246E-319;
        r4 = 33071; // 0x812f float:4.6342E-41 double:1.6339E-319;
        android.opengl.GLES30.glTexParameteri(r2, r3, r4);
        return;
    L_0x0097:
        r15 = move-exception;
        r15.printStackTrace();
        goto L_0x0025;
    L_0x009c:
        r15 = move-exception;
        r15.printStackTrace();
        r12 = r13;
        r16 = r17;
        goto L_0x002d;
    L_0x00a4:
        r15 = move-exception;
    L_0x00a5:
        r15.printStackTrace();	 Catch:{ all -> 0x00bf }
        if (r12 == 0) goto L_0x00ad;
    L_0x00aa:
        r12.close();	 Catch:{ IOException -> 0x00ba }
    L_0x00ad:
        if (r16 == 0) goto L_0x002d;
    L_0x00af:
        r16.close();	 Catch:{ IOException -> 0x00b4 }
        goto L_0x002d;
    L_0x00b4:
        r15 = move-exception;
        r15.printStackTrace();
        goto L_0x002d;
    L_0x00ba:
        r15 = move-exception;
        r15.printStackTrace();
        goto L_0x00ad;
    L_0x00bf:
        r2 = move-exception;
    L_0x00c0:
        if (r12 == 0) goto L_0x00c5;
    L_0x00c2:
        r12.close();	 Catch:{ IOException -> 0x00cb }
    L_0x00c5:
        if (r16 == 0) goto L_0x00ca;
    L_0x00c7:
        r16.close();	 Catch:{ IOException -> 0x00d0 }
    L_0x00ca:
        throw r2;
    L_0x00cb:
        r15 = move-exception;
        r15.printStackTrace();
        goto L_0x00c5;
    L_0x00d0:
        r15 = move-exception;
        r15.printStackTrace();
        goto L_0x00ca;
    L_0x00d5:
        r2 = move-exception;
        r16 = r17;
        goto L_0x00c0;
    L_0x00d9:
        r2 = move-exception;
        r12 = r13;
        r16 = r17;
        goto L_0x00c0;
    L_0x00de:
        r15 = move-exception;
        r16 = r17;
        goto L_0x00a5;
    L_0x00e2:
        r15 = move-exception;
        r12 = r13;
        r16 = r17;
        goto L_0x00a5;
    L_0x00e7:
        r12 = r13;
        r16 = r17;
        goto L_0x002d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.filmemulatorengine.FilmEmulatorRendererPreview.init3DTex(java.lang.String, int, int):void");
    }

    public void initBackground(Drawable back, int width, int height) {
        if (back != null) {
            Bitmap bmp = Bitmap.createBitmap(height, width, Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            back.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            back.draw(canvas);
            GLES20.glBindTexture(3553, this.bgTexture[0]);
            GLES20.glTexParameteri(3553, 10241, 9729);
            GLES20.glTexParameteri(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLUtils.texImage2D(3553, 0, bmp, 0);
            canvas.release();
            bmp.recycle();
        }
    }

    private void initDeleteButton(Drawable back, int index, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(height, width, Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        back.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        back.draw(canvas);
        GLES20.glBindTexture(3553, this.deleteBtnTexture[index]);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLUtils.texImage2D(3553, 0, bmp, 0);
        canvas.release();
        bmp.recycle();
    }

    protected void handledSurfaceChanged(SurfaceHolder holder, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.mSurfaceViewWidth = width;
        this.mSurfaceViewHeight = height;
        GLES20.glGenTextures(this.deleteBtnTexture.length, this.deleteBtnTexture, 0);
        initDeleteButton(this.mDeleteBtnDrawable[0], 0, width, height);
        initDeleteButton(this.mDeleteBtnDrawable[1], 1, width, height);
        GLES20.glGenTextures(this.bgTexture.length, this.bgTexture, 0);
        initBackground(this.mBackground, width, height);
        updateGeometry(width, height);
        for (Rect rect : this.viewRect) {
            rect.left = -1;
        }
    }

    protected void updateGeometry(int width, int height) {
        Log.d(TAG, "update geometry : " + width + "x" + height);
        Matrix.orthoM(this.mvpMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
        Matrix.translateM(this.mvpMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
        Matrix.scaleM(this.mvpMatrix, 0, (float) width, (float) height, 1.0f);
    }

    protected void shutdown() {
        Looper.myLooper().quit();
    }

    private SurfaceTexture getCurrentCameraTexture() {
        return this.mCurrentCamera == 0 ? this.mCamTexture1 : this.mCamTexture2;
    }

    public void changeCamera() {
        synchronized (this.mCamChangeLock) {
            this.mCurrentCamera = this.mCurrentCamera == 0 ? 1 : 0;
        }
    }

    protected void prepareRecording(Surface surface, int width, int height, int idx, boolean isCinema, int flagReverse) {
        if (this.mRecEGLSurfaceBase != null) {
            this.mRecEGLSurfaceBase.releaseWithoutSurface();
            this.mRecEGLSurfaceBase = null;
        }
        this.mRecEGLSurfaceBase = new FilmEmulatorEGLSurfaceBase(this.mEGLSurfaceBase.getEGLContext(), surface, 1);
        int reverseVer = (flagReverse & 1) != 0 ? -1 : 1;
        int reverseHor = (flagReverse & 2) != 0 ? -1 : 1;
        Matrix.orthoM(this.mRecMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
        Matrix.translateM(this.mRecMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
        Matrix.scaleM(this.mRecMatrix, 0, (float) (reverseVer * width), (float) (reverseHor * height), 1.0f);
        Matrix.rotateM(this.mRecMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f);
        this.mRecWidth = width;
        this.mRecHeight = height;
        this.mRecIndex = idx;
        if (isCinema) {
            this.mRecAspectFrom = 1.7777778f;
            this.mRecAspectTo = 2.3333333f;
            return;
        }
        this.mRecAspectFrom = 1.0f;
        this.mRecAspectTo = 1.0f;
    }

    protected void startRecording() {
        synchronized (this.mRecordingLock) {
            this.isRecording = true;
        }
    }

    public void stopRecordingDirect() {
        synchronized (this.mRecordingLock) {
            this.isRecording = false;
            this.mRecordingLock.notify();
        }
    }

    public void stopRecording() {
        synchronized (this.mRecordingLock) {
            this.isRecording = false;
            if (this.mRecEGLSurfaceBase != null) {
                this.mRecEGLSurfaceBase.makeContextCurrent();
                this.mRecEGLSurfaceBase.releaseWithoutSurface();
                this.mRecEGLSurfaceBase = null;
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

    public void showFilmMenu(boolean show) {
        this.mIsShowFilmMenu = show;
    }

    public void changePictureSize(float ratio) {
        this.mPreviewRatio = ratio;
        this.mHandler.removeMessages(4);
        this.mHandler.sendEmptyMessage(12);
        float borderSize = ((this.mPreviewRatio - this.mThumbMenuRatio) / 2.0f) / this.mPreviewRatio;
        if (this.mPreviewRatio > this.mThumbMenuRatio) {
            this.mSquareResolution = 0.0f;
        } else {
            borderSize = ((this.mThumbMenuRatio - this.mPreviewRatio) / 2.0f) / this.mThumbMenuRatio;
            this.mSquareResolution = 1.0f;
        }
        this.mBorderSize = ((float) Math.round(borderSize * 1000.0f)) / 1000.0f;
        Log.d(TAG, "changePictureSize, border size : " + this.mBorderSize);
    }

    public void updateFilterNameTexture() {
        Log.i(TAG, "updateFilterNameTexture - START, textWidth : " + CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL + ", textHeight : " + ((int) (((float) 480) / this.mPreviewRatio)) + ", ratio : " + this.mPreviewRatio);
        int[] textureBitmapSize = new int[]{CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL, textHeight};
        for (int index = 0; index < this.mLuts.length; index++) {
            GLES20.glBindTexture(3553, this.filmTitleTexture[index]);
            GLES20.glTexParameterf(3553, 10241, 9729.0f);
            GLES20.glTexParameterf(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729.0f);
            Bitmap bmp = getFilterNameBitmap(this.mDegree, textureBitmapSize[0], textureBitmapSize[1], 0, 0, this.mFilmStringList[index], -1);
            GLUtils.texImage2D(3553, 0, bmp, 0);
            GLES20.glGenerateMipmap(3553);
            bmp.recycle();
        }
        Log.i(TAG, "updateFilterNameTexture - END");
    }

    public Bitmap getFilterNameBitmap(int orientation, int bmpWidth, int bmpHeight, int x, int y, String title, int color) {
        Bitmap bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize((((float) bmpHeight) / ((float) bmpWidth)) * 75.0f);
        int cropMargin = (bmpWidth - bmpHeight) / 2;
        int topMargin = bmpWidth - cropMargin;
        float shadowRadius = ModelProperties.getAppTier() >= 5 ? 1.0f : 2.5f;
        if (this.mIsReverse) {
            android.graphics.Matrix matrix = c.getMatrix();
            matrix.setScale(-1.0f, 1.0f, (float) (c.getWidth() / 2), (float) (c.getHeight() / 2));
            c.setMatrix(matrix);
        }
        c.save();
        Rect textBound = new Rect();
        if (orientation == 0) {
            c.rotate(270.0f);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
            textPaint.setColor(-1);
            textPaint.setAlpha(255);
            textPaint.setStyle(Style.FILL);
            textPaint.setShadowLayer(shadowRadius, 0.0f, 0.0f, ViewCompat.MEASURED_STATE_MASK);
            textPaint.getTextBounds(title, 0, title.length(), textBound);
            c.drawText(title, (float) (((-bmpHeight) + x) + ((bmpHeight - textBound.width()) / 2)), (float) ((topMargin + y) - 20), textPaint);
        } else if (orientation == 270) {
            c.rotate(0.0f);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
            textPaint.setColor(-1);
            textPaint.setAlpha(255);
            textPaint.setStyle(Style.FILL);
            textPaint.setShadowLayer(shadowRadius, 0.0f, 0.0f, ViewCompat.MEASURED_STATE_MASK);
            textPaint.getTextBounds(title, 0, title.length(), textBound);
            c.drawText(title, (float) ((cropMargin + x) + ((bmpHeight - textBound.width()) / 2)), (float) ((bmpHeight + y) - 20), textPaint);
        } else if (orientation == 180) {
            c.rotate(90.0f);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
            textPaint.setColor(-1);
            textPaint.setAlpha(255);
            textPaint.setStyle(Style.FILL);
            textPaint.setShadowLayer(shadowRadius, 0.0f, 0.0f, ViewCompat.MEASURED_STATE_MASK);
            textPaint.getTextBounds(title, 0, title.length(), textBound);
            c.drawText(title, (float) (x + ((bmpHeight - textBound.width()) / 2)), (float) (((-cropMargin) + y) - 20), textPaint);
        } else if (orientation == 90) {
            c.rotate(180.0f);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
            textPaint.setColor(-1);
            textPaint.setAlpha(255);
            textPaint.setStyle(Style.FILL);
            textPaint.setShadowLayer(shadowRadius, 0.0f, 0.0f, ViewCompat.MEASURED_STATE_MASK);
            textPaint.getTextBounds(title, 0, title.length(), textBound);
            c.drawText(title, (float) (((-topMargin) + x) + ((bmpHeight - textBound.width()) / 2)), (float) (y - 20), textPaint);
        }
        c.restore();
        return bmp;
    }

    public void setSelectedLUT(int lutNum) {
        this.mSelectedLut = lutNum;
    }

    public void setDegree(int degree, boolean updateFilterName) {
        synchronized (this.mRotateTitleLock) {
            this.mDegree = degree;
            if (updateFilterName) {
                this.mIsUpdAteFilterNameTexture = true;
            }
        }
    }

    public void pressFilmMenu(int lut) {
        this.mPressedLUTNumber = lut;
    }

    public void resetLutViewRect(int tex) {
        this.viewRect[tex].left = -10000;
        this.viewRect[tex].right = -10000;
        this.viewRect[tex].top = -10000;
        this.viewRect[tex].bottom = -10000;
    }

    public void changeBackground(Drawable drawable) {
        this.mBackground = drawable;
        getHanlder().sendEmptyMessage(14);
    }

    public void resetLutViewRect() {
        for (int i = 0; i < this.viewRect.length; i++) {
            this.viewRect[i] = new Rect();
            this.viewRect[i].left = -10000;
            this.viewRect[i].right = -10000;
            this.viewRect[i].top = -10000;
            this.viewRect[i].bottom = -10000;
        }
    }

    public void setFrameAvailableCallback() {
        CamLog.m7i(TAG, "setFrameAvailableCallback");
        this.mCamTexture1.setOnFrameAvailableListener(null);
        this.mCamTexture1.setOnFrameAvailableListener(new C14423());
    }
}
