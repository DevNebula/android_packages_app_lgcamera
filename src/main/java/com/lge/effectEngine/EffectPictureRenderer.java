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
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import android.view.Surface;

public class EffectPictureRenderer extends Thread {
    private static final String TAG = "RenderThreadPicture";
    private float[] mBoundaryMatrix = new float[16];
    private int mCameraPictureHeightNormal;
    private int mCameraPictureHeightWide;
    private int mCameraPictureWidthNormal;
    private int mCameraPictureWidthWide;
    private SurfaceTexture mCameraTexture = null;
    private float[] mDisplayProjectionMatrix = new float[16];
    private int mDrawMode;
    public EffectFrameDrawer mFrameDrawer;
    private int mFrameType;
    private boolean mIsHal3;
    private LGPopoutEffectEngineListener mListener;
    private Bitmap mNormBmp = null;
    private boolean mReady = false;
    private int mReverseSign;
    private Object mStartLock = new Object();
    private Bitmap mWideBmp = null;
    private EffectEGLSurfaceBase mWindowSurface;
    private int mWindowSurfaceHeight;
    private int mWindowSurfaceWidth;

    public EffectFrameDrawer getShader() {
        return this.mFrameDrawer;
    }

    public EffectPictureRenderer(LGPopoutEffectEngineListener listener, Bitmap norm, Bitmap wide, boolean reverse, int mode, int frameType, boolean isHal3) {
        this.mListener = listener;
        this.mDrawMode = mode;
        this.mFrameType = frameType;
        this.mNormBmp = norm;
        this.mWideBmp = wide;
        this.mReverseSign = reverse ? -1 : 1;
        this.mWindowSurfaceWidth = wide.getWidth();
        this.mWindowSurfaceHeight = wide.getHeight();
        this.mIsHal3 = isHal3;
    }

    public void run() {
        try {
            Log.d(TAG, "before init");
            initGL(this.mWindowSurfaceWidth, this.mWindowSurfaceHeight);
            this.mFrameDrawer.setNormalTexture(this.mNormBmp);
            this.mNormBmp.recycle();
            this.mNormBmp = null;
            this.mFrameDrawer.setWideTexture(this.mWideBmp);
            this.mWideBmp.recycle();
            this.mWideBmp = null;
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
        this.mListener.onSendStillImage(this.mFrameDrawer.draw(this.mDisplayProjectionMatrix, this.mBoundaryMatrix, null, null, mode, this.mFrameType, this.mWindowSurfaceWidth, this.mWindowSurfaceHeight, 0.0f, true));
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
    }

    private void initGL(int desiredWidth, int desiredHeight) throws OutOfMemoryError {
        int width = this.mWindowSurfaceWidth;
        int height = this.mWindowSurfaceHeight;
        this.mWindowSurface = new EffectEGLSurfaceBase(null, new Surface(), 0, width, height);
        this.mWindowSurface.makeContextCurrent();
        this.mFrameDrawer = new EffectFrameDrawer(this.mIsHal3);
        this.mFrameDrawer.createTextureObject(width, height);
        this.mFrameDrawer.createPrograms();
        this.mFrameDrawer.createLocations();
        finishSurfaceSetup();
    }

    private void finishSurfaceSetup() throws OutOfMemoryError {
        GLES20.glViewport(0, 0, this.mWindowSurfaceWidth, this.mWindowSurfaceHeight);
        updateGeometry();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    private void updateGeometry() throws OutOfMemoryError {
        int tFrameType;
        int width = this.mWindowSurfaceWidth;
        int height = this.mWindowSurfaceHeight;
        float reverseAspect = 1.7777778f * (((float) height) / ((float) width));
        float[] modelMatrix = new float[16];
        Matrix.orthoM(modelMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
        Matrix.translateM(modelMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
        Matrix.scaleM(modelMatrix, 0, (float) (this.mReverseSign * width), (float) (-height), 1.0f);
        this.mDisplayProjectionMatrix = modelMatrix;
        float[] bmodelMatrix = new float[16];
        Matrix.orthoM(bmodelMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
        Matrix.translateM(bmodelMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
        Matrix.scaleM(bmodelMatrix, 0, ((float) (this.mReverseSign * width)) * reverseAspect, (float) (-height), 1.0f);
        this.mBoundaryMatrix = bmodelMatrix;
        if (this.mFrameType == 4) {
            tFrameType = 3;
        } else {
            tFrameType = this.mFrameType;
        }
        float thickness = ShaderParameterData.getInstance().getFrameThickness();
        float shadowRadius = ShaderParameterData.getInstance().getFrameShadowsSize();
        float dx = ((float) width) * ShaderParameterData.getInstance().getFrameShadowDX();
        float dy = ((float) height) * ShaderParameterData.getInstance().getFrameShadowDY();
        int normFrameWidth = (int) (((float) width) * ShaderParameterData.getInstance().getNormToWideRatio()[tFrameType]);
        int normFrameHeight = (int) (((float) height) * ShaderParameterData.getInstance().getNormToWideRatio()[tFrameType]);
        int shadowHeight = (int) (((float) normFrameHeight) * ShaderParameterData.getInstance().getFrameShadowsScale());
        int shadowWidth = (normFrameWidth + shadowHeight) - normFrameHeight;
        int shadowColor = ShaderParameterData.getInstance().getFrameShadowColor();
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
        if (this.mFrameType == 0) {
            bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            sleft = (((float) (width - shadowWidth)) * 0.5f) + dx;
            stop = (((float) (height - shadowHeight)) * 0.5f) + dy;
            canvas.drawRect(sleft - thickness, stop - thickness, (((float) shadowWidth) + sleft) + thickness, (((float) shadowHeight) + stop) + thickness, paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            left = ((float) (width - normFrameWidth)) * 0.5f;
            top = ((float) (height - normFrameHeight)) * 0.5f;
            canvas.drawRect(left - thickness, top - thickness, (((float) normFrameWidth) + left) + thickness, (((float) normFrameHeight) + top) + thickness, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            paint.setShadowLayer(0.0f, 0.0f, 0.0f, Color.argb(255, 0, 0, 0));
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
            sleft = (((float) (width - shadowWidth)) * 0.5f) + dx;
            stop = (((float) (height - shadowHeight)) * 0.5f) + dy;
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
        } else if (this.mFrameType == 4) {
            bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            sleft = (((float) (width - shadowWidth)) * 0.5f) + dx;
            stop = (((float) (height - shadowHeight)) * 0.5f) + dy;
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
        } else if (this.mFrameType == 1) {
            bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            canvas.drawCircle(((float) (width / 2)) + dx, ((float) (height / 2)) + dy, (((float) shadowHeight) + thickness) / 2.0f, paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            canvas.drawCircle((float) (width / 2), (float) (height / 2), (((float) normFrameHeight) + thickness) / 2.0f, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            canvas.drawCircle((float) (width / 2), (float) (height / 2), (float) (normFrameHeight / 2), paint);
            this.mFrameDrawer.setBoundaryTexture(bm);
            bm.recycle();
            mask = Bitmap.createBitmap(normFrameWidth, normFrameHeight, Config.ARGB_8888);
            cMask = new Canvas(mask);
            paint2 = new Paint(1);
            paint2.setColor(ViewCompat.MEASURED_STATE_MASK);
            cMask.drawPaint(paint2);
            paint2.setColor(-1);
            cMask.drawCircle((float) (normFrameWidth / 2), (float) (normFrameHeight / 2), (float) (normFrameHeight / 2), paint2);
            this.mFrameDrawer.setMaskTexture(mask);
            mask.recycle();
            System.gc();
        } else if (this.mFrameType == 2) {
            bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            canvas = new Canvas(bm);
            paint = new Paint(1);
            paint.setColor(shadowColor);
            paint.setStyle(Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(shadowRadius, Blur.NORMAL));
            canvas.drawPath(getPathHex(((int) dx) + width, ((int) dy) + height, ((int) (((float) shadowHeight) + thickness)) / 2), paint);
            paint.setColor(Color.argb(102, 0, 0, 0));
            paint.setMaskFilter(null);
            canvas.drawPath(getPathHex(width, height, ((int) (((float) normFrameHeight) + thickness)) / 2), paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            paint.setColor(0);
            canvas.drawPath(getPathHex(width, height, normFrameHeight / 2), paint);
            this.mFrameDrawer.setBoundaryTexture(bm);
            bm.recycle();
            mask = Bitmap.createBitmap(normFrameWidth, normFrameHeight, Config.ARGB_8888);
            cMask = new Canvas(mask);
            paint2 = new Paint(1);
            paint2.setColor(ViewCompat.MEASURED_STATE_MASK);
            cMask.drawPaint(paint2);
            cMask.clipPath(getPathHex(normFrameWidth, normFrameHeight, normFrameHeight / 2));
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
}
