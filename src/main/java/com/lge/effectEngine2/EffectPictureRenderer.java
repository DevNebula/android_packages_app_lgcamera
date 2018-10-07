package com.lge.effectEngine2;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

public class EffectPictureRenderer extends Thread {
    private static final String TAG = "RenderThreadPicture";
    private boolean isDualCameraMode;
    private float[] mBoundaryMatrix = new float[16];
    private int mCameraPictureHeightNormal;
    private int mCameraPictureHeightWide;
    private int mCameraPictureWidthNormal;
    private int mCameraPictureWidthWide;
    private SurfaceTexture mCameraTexture = null;
    private float[] mDisplayProjectionMatrix = new float[16];
    private int mDrawMode;
    private Bitmap mFrame = null;
    private RectF mFrameCoods = null;
    public EffectPictureFrame mFrameDrawer;
    private int mFrameType;
    private LGPopoutEffectEngineListener mListener;
    private Bitmap mMask = null;
    private Bitmap mNormBmp = null;
    private boolean mReady = false;
    private RectF mRelativeCoods = null;
    private int mReverseSign;
    private Object mStartLock = new Object();
    private Bitmap mWideBmp = null;
    private EffectEGLSurfaceBase mWindowSurface;
    private int mWindowSurfaceHeight;
    private int mWindowSurfaceWidth;

    public EffectPictureFrame getShader() {
        return this.mFrameDrawer;
    }

    public EffectPictureRenderer(LGPopoutEffectEngineListener listener, Bitmap norm, Bitmap wide, Bitmap frame, Bitmap mask, RectF coord, RectF relativeCoord, int drawMode) {
        this.mListener = listener;
        this.mFrame = frame;
        this.mMask = mask;
        this.mFrameCoods = coord;
        this.mRelativeCoods = relativeCoord;
        this.mDrawMode = drawMode;
        if (norm == null) {
            this.isDualCameraMode = false;
            this.mNormBmp = null;
        } else {
            this.isDualCameraMode = true;
            this.mNormBmp = norm;
        }
        this.mWideBmp = wide;
        this.mWindowSurfaceWidth = wide.getWidth();
        this.mWindowSurfaceHeight = wide.getHeight();
    }

    public void run() {
        try {
            Log.d(TAG, "before init");
            initGL(this.mWindowSurfaceWidth, this.mWindowSurfaceHeight);
            if (this.isDualCameraMode) {
                this.mFrameDrawer.setNormalTexture(this.mNormBmp);
            }
            this.mFrameDrawer.setWideTexture(this.mWideBmp);
            draw(this.mDrawMode);
            releaseGl();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            releaseGl();
            this.mListener.onErrorOccured(0);
        }
        synchronized (this.mStartLock) {
            this.mReady = false;
        }
    }

    protected void draw(int mode) throws OutOfMemoryError {
        GLES20.glClear(16384);
        this.mListener.onSendStillImage(this.mFrameDrawer.draw(mode, 3));
    }

    protected void releaseGl() {
        if (this.mFrameDrawer != null) {
            this.mFrameDrawer.release();
            this.mFrameDrawer = null;
        }
        if (this.mWindowSurface != null) {
            this.mWindowSurface.release();
            this.mWindowSurface = null;
        }
        this.mNormBmp.recycle();
        this.mWideBmp.recycle();
    }

    private void initGL(int desiredWidth, int desiredHeight) {
        int width = this.mWindowSurfaceWidth;
        int height = this.mWindowSurfaceHeight;
        this.mWindowSurface = new EffectEGLSurfaceBase(null, new Surface(), 0, width, height);
        this.mWindowSurface.makeContextCurrent();
        this.mFrameDrawer = new EffectPictureFrame(this.isDualCameraMode, width, height);
        this.mFrameDrawer.setBoundaryTexture(this.mFrame);
        this.mFrameDrawer.setMaskTexture(this.mMask, this.mRelativeCoods);
        this.mFrameDrawer.setBoundaryPos(this.mFrameCoods);
    }
}
