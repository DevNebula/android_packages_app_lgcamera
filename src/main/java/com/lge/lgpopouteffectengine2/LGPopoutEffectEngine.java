package com.lge.lgpopouteffectengine2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.effectEngine2.EffectPictureRenderer;
import com.lge.effectEngine2.EffectPreviewRenderer;
import com.lge.effectEngine2.EffectPreviewRenderer.RenderHandlerPreview;
import com.lge.effectEngine2.LGPopoutEffectEngineListener;

public class LGPopoutEffectEngine {
    private static final String TAG = "LGPopoutEffectEngine";
    private boolean isReversed;
    private int mDrawMode;
    private EffectPictureRenderer mEffectPictureRenderer = null;
    private RenderHandlerPreview mEffectPreviewHandler = null;
    private EffectPreviewRenderer mEffectPreviewRenderer = null;
    private EffectFrameData mFrame = null;
    private int mFrameType;
    private SurfaceHolder mHolder;
    private LGPopoutEffectEngineListener mListener = null;
    private Camera mNormCamera;
    private Camera mWideCamera;

    public class EffectFrameData {
        Bitmap boundary;
        RectF innerPos;
        Bitmap mask;
        RectF outterPos;

        public EffectFrameData(Bitmap boundary, Bitmap mask, RectF outterPos, RectF innerPos) {
            this.boundary = boundary;
            this.mask = mask;
            this.outterPos = outterPos;
            this.innerPos = innerPos;
        }

        public Bitmap getBoundary() {
            return this.boundary;
        }

        public void setBoundary(Bitmap boundary) {
            this.boundary = boundary;
        }

        public Bitmap getMask() {
            return this.mask;
        }

        public void setMask(Bitmap mask) {
            this.mask = mask;
        }

        public RectF getOutterPos() {
            return this.outterPos;
        }

        public void setOutterPos(RectF outterPos) {
            this.outterPos = outterPos;
        }

        public RectF getInnerPos() {
            return this.innerPos;
        }

        public void setInnerPos(RectF innerPos) {
            this.innerPos = innerPos;
        }

        public void release() {
            if (this.boundary != null) {
                this.boundary.recycle();
                this.boundary = null;
            }
            if (this.mask != null) {
                this.mask.recycle();
                this.mask = null;
            }
            this.outterPos = null;
            this.innerPos = null;
        }
    }

    public LGPopoutEffectEngine(Camera camera, LGPopoutEffectEngineListener listener, SurfaceHolder holder, boolean reverse) {
        this.mWideCamera = camera;
        this.mNormCamera = null;
        this.mListener = listener;
        this.mHolder = holder;
        this.isReversed = reverse;
        initialize();
        this.mDrawMode = 0;
        this.mFrameType = 0;
    }

    public LGPopoutEffectEngine(Camera normalCamera, Camera wideCamera, LGPopoutEffectEngineListener listener, SurfaceHolder holder, boolean reverse) {
        this.mNormCamera = normalCamera;
        this.mWideCamera = wideCamera;
        this.mHolder = holder;
        this.mListener = listener;
        this.isReversed = reverse;
        initialize();
        this.mDrawMode = 0;
        this.mFrameType = 0;
    }

    private void initialize() {
        this.mEffectPreviewRenderer = new EffectPreviewRenderer(this.mListener, this.mNormCamera, this.mWideCamera, this.mHolder, this.isReversed);
        this.mEffectPreviewRenderer.setName("TexFromCam Render Dual");
        this.mEffectPreviewRenderer.start();
        this.mEffectPreviewRenderer.waitUntilReady();
        this.mEffectPreviewHandler = this.mEffectPreviewRenderer.getHandler();
        Log.d(TAG, "Engine init complete");
    }

    public void surfaceChanged(SurfaceHolder holder, int width, int height) {
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.sendSurfaceChanged(width, height);
        }
    }

    public void release() {
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.sendShutdown();
        }
        if (this.mEffectPreviewRenderer != null) {
            try {
                this.mEffectPreviewRenderer.join();
                this.mEffectPreviewRenderer = null;
            } catch (InterruptedException ie) {
                throw new RuntimeException("join was interrupted preview thread", ie);
            }
        }
    }

    public void setDrawMode(boolean blur, boolean perspective, boolean gray, boolean vignet) {
        this.mDrawMode = blur ? this.mDrawMode | 1 : this.mDrawMode & -2;
        this.mDrawMode = perspective ? this.mDrawMode | 2 : this.mDrawMode & -3;
        this.mDrawMode = gray ? this.mDrawMode | 4 : this.mDrawMode & -5;
        this.mDrawMode = vignet ? this.mDrawMode | 8 : this.mDrawMode & -9;
        this.mEffectPreviewHandler.sendDrawMode(this.mDrawMode);
    }

    public boolean[] getDrawMode() {
        boolean z;
        boolean z2 = true;
        boolean[] modes = new boolean[4];
        if ((this.mDrawMode & 1) > 0) {
            z = true;
        } else {
            z = false;
        }
        modes[0] = z;
        if ((this.mDrawMode & 2) > 0) {
            z = true;
        } else {
            z = false;
        }
        modes[1] = z;
        if ((this.mDrawMode & 4) > 0) {
            z = true;
        } else {
            z = false;
        }
        modes[2] = z;
        if ((this.mDrawMode & 8) <= 0) {
            z2 = false;
        }
        modes[3] = z2;
        return modes;
    }

    public void setFrameType(Bitmap boundary, Bitmap mask, RectF rect, RectF relativeRect) {
        if (this.mFrame != null) {
            this.mFrame.release();
        }
        this.mFrame = new EffectFrameData(boundary, mask, rect, relativeRect);
        this.mEffectPreviewHandler.sendFrameType(this.mFrame);
    }

    public int getFrameType() {
        return this.mFrameType;
    }

    public void setStillImage(byte[] stillData) {
        setStillImage(null, stillData);
    }

    public void setStillImage(byte[] normStillData, byte[] wideStillData) {
        Bitmap norm;
        Bitmap wide = BitmapFactory.decodeByteArray(wideStillData, 0, wideStillData.length);
        if (normStillData == null) {
            norm = null;
        } else {
            norm = BitmapFactory.decodeByteArray(normStillData, 0, normStillData.length);
        }
        if (this.mFrame != null) {
            this.mEffectPictureRenderer = new EffectPictureRenderer(this.mListener, norm, wide, this.mFrame.getBoundary(), this.mFrame.getMask(), this.mEffectPreviewRenderer.getBoundRect(), this.mFrame.getInnerPos(), this.mDrawMode);
            this.mEffectPictureRenderer.setName("Still Thread");
            this.mEffectPictureRenderer.start();
        }
    }

    public void refreshCameraParameter() {
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.updateCamParam();
            this.mEffectPreviewRenderer.waitUpdatedCameraParam();
        }
        Log.d(TAG, "refreshed");
    }

    public void prepareRecording(Surface recSurface, int width, int height) {
        Log.d(TAG, "surface : " + recSurface);
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.sendPrepareRecording(recSurface, width, height);
        }
    }

    public void startRecording(boolean stopWideCam) {
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.sendStartRecording(stopWideCam);
        }
    }

    public void stopRecording() {
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.sendStopRecording();
        }
        if (this.mEffectPreviewRenderer != null) {
            this.mEffectPreviewRenderer.waitStopRecording();
        }
    }

    public void pauseRecording() {
        if (this.mEffectPreviewRenderer != null) {
            this.mEffectPreviewRenderer.pauseRecording();
        }
    }

    public void resumeRecording() {
        if (this.mEffectPreviewRenderer != null) {
            this.mEffectPreviewRenderer.resumeRecording();
        }
    }

    public void pauseWideView() {
        if (this.mEffectPreviewRenderer != null) {
            this.mEffectPreviewRenderer.pauseWideView();
        }
    }

    public void resumeWideView() {
        if (this.mEffectPreviewRenderer != null) {
            this.mEffectPreviewRenderer.resumeWideView();
        }
    }
}
