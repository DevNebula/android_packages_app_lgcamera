package com.lge.lgpopouteffectengine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.effectEngine.EffectPictureRenderer;
import com.lge.effectEngine.EffectPreviewRenderer;
import com.lge.effectEngine.EffectPreviewRenderer.RenderHandlerPreview;
import com.lge.effectEngine.LGPopoutEffectEngineListener;

public class LGPopoutEffectEngine {
    private static final String TAG = "LGPopoutEffectEngine";
    private boolean isReversed;
    private int mDrawMode;
    private EffectPictureRenderer mEffectPictureRenderer = null;
    private RenderHandlerPreview mEffectPreviewHandler = null;
    private EffectPreviewRenderer mEffectPreviewRenderer = null;
    private int mFrameType;
    private SurfaceHolder mHolder;
    private boolean mIsHal3;
    private LGPopoutEffectEngineListener mListener = null;
    private int mNormalSTHeight;
    private int mNormalSTWidth;
    private int mWideSTHeight;
    private int mWideSTWidth;

    public LGPopoutEffectEngine(Camera camera, SurfaceHolder holder, LGPopoutEffectEngineListener listener, boolean reverse) {
        this.mListener = listener;
        this.mHolder = holder;
        this.isReversed = reverse;
        this.mDrawMode = 0;
        this.mFrameType = 0;
        initialize();
    }

    public LGPopoutEffectEngine(Camera normalCamera, Camera wideCamera, SurfaceHolder holder, LGPopoutEffectEngineListener listener, boolean reverse) {
        this.mListener = listener;
        this.mHolder = holder;
        this.isReversed = reverse;
        this.mDrawMode = 0;
        this.mFrameType = 0;
        initialize();
    }

    public LGPopoutEffectEngine(SurfaceHolder holder, LGPopoutEffectEngineListener listener, boolean reverse, int normW, int normH, int wideW, int wideH, boolean isHal3) {
        this.mListener = listener;
        this.mHolder = holder;
        this.isReversed = reverse;
        this.mDrawMode = 0;
        this.mFrameType = 0;
        this.mNormalSTWidth = normW;
        this.mNormalSTHeight = normH;
        this.mWideSTWidth = wideW;
        this.mWideSTHeight = wideH;
        this.mIsHal3 = isHal3;
        initialize();
    }

    private void initialize() {
        this.mEffectPreviewRenderer = new EffectPreviewRenderer(this.mListener, this.mHolder, this.isReversed, this.mNormalSTWidth, this.mNormalSTHeight, this.mWideSTWidth, this.mWideSTHeight, this.mIsHal3);
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

    public void setDrawMode(boolean blur, boolean blur2, boolean perspective, boolean gray, boolean vignet) {
        this.mDrawMode = blur ? this.mDrawMode | 1 : this.mDrawMode & -2;
        this.mDrawMode = blur2 ? this.mDrawMode | 16 : this.mDrawMode & -17;
        this.mDrawMode = perspective ? this.mDrawMode | 2 : this.mDrawMode & -3;
        this.mDrawMode = gray ? this.mDrawMode | 4 : this.mDrawMode & -5;
        this.mDrawMode = vignet ? this.mDrawMode | 8 : this.mDrawMode & -9;
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.sendDrawMode(this.mDrawMode);
        }
    }

    public boolean[] getDrawMode() {
        boolean z;
        boolean z2 = true;
        boolean[] modes = new boolean[5];
        modes[0] = (this.mDrawMode & 1) > 0;
        if ((this.mDrawMode & 16) > 0) {
            z = true;
        } else {
            z = false;
        }
        modes[1] = z;
        if ((this.mDrawMode & 8) > 0) {
            z = true;
        } else {
            z = false;
        }
        modes[2] = z;
        if ((this.mDrawMode & 4) > 0) {
            z = true;
        } else {
            z = false;
        }
        modes[3] = z;
        if ((this.mDrawMode & 2) <= 0) {
            z2 = false;
        }
        modes[4] = z2;
        return modes;
    }

    public void setFrameType(int type) {
        if (type < 0 || 4 < type) {
            throw new RuntimeException("frame type must be 0~2");
        }
        this.mFrameType = type;
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.sendFrameType(this.mFrameType);
        }
    }

    public int getFrameType() {
        return this.mFrameType;
    }

    public void setStillImage(byte[] stillData) {
        setStillImage(null, stillData);
    }

    public void setStillImage(byte[] normStillData, byte[] wideStillData) {
        try {
            Bitmap norm;
            Bitmap wide = BitmapFactory.decodeByteArray(wideStillData, 0, wideStillData.length);
            if (normStillData == null) {
                norm = null;
            } else {
                norm = BitmapFactory.decodeByteArray(normStillData, 0, normStillData.length);
            }
            this.mEffectPictureRenderer = new EffectPictureRenderer(this.mListener, norm, wide, this.isReversed, this.mDrawMode, this.mFrameType, this.mIsHal3);
            this.mEffectPictureRenderer.setName("Still Thread");
            this.mEffectPictureRenderer.start();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            this.mEffectPictureRenderer = null;
            this.mListener.onErrorOccured(0);
        }
        this.mEffectPictureRenderer = null;
    }

    public void refreshCameraParameter() {
        if (this.mEffectPreviewHandler != null) {
            this.mEffectPreviewHandler.updateCamParam();
        }
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
}
