package com.lge.effectEngine2;

import android.graphics.Rect;
import android.opengl.Matrix;

public class EffectRecordFrame extends EffectPreviewFrame {
    private static final String TAG = "EffectRecordFrame";

    public EffectRecordFrame(boolean isDualMode, EffectPreviewFrame pFrame, int width, int height) {
        super(isDualMode);
        this.mSurfaceWidth = width;
        this.mPreviewWidth = width;
        this.mSurfaceHeight = height;
        this.mPreviewHeight = height;
        this.mTextures = pFrame.mTextures;
        this.mPrograms = pFrame.mPrograms;
        createFrameBuffers(width, height);
        this.mBoundRectf.set(pFrame.mBoundRectf);
        this.mBoundNormalRectF.set(pFrame.mBoundNormalRectF);
        this.mBoundaryWidth = pFrame.mBoundaryWidth;
        this.mBoundaryHeight = pFrame.mBoundaryHeight;
        createLocations();
        setInnerRectPos(this.mBoundRectf);
        Matrix.orthoM(this.mProjectOutsideViewMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
        Matrix.translateM(this.mProjectOutsideViewMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
        Matrix.scaleM(this.mProjectOutsideViewMatrix, 0, (float) width, (float) (-height), 1.0f);
        Matrix.orthoM(this.mProjectInsideViewMatrix, 0, 0.0f, (float) this.mBoundaryWidth, 0.0f, (float) this.mBoundaryHeight, -1.0f, 1.0f);
        Matrix.translateM(this.mProjectInsideViewMatrix, 0, ((float) this.mBoundaryWidth) / 2.0f, ((float) this.mBoundaryHeight) / 2.0f, 0.0f);
        Matrix.scaleM(this.mProjectInsideViewMatrix, 0, (float) this.mBoundaryWidth, (float) (-this.mBoundaryHeight), 1.0f);
    }

    protected Rect convertCoord(float left, float top, float width, float height) {
        return new Rect((int) (((float) this.mSurfaceWidth) * left), (int) (((float) this.mSurfaceHeight) - ((top + height) * ((float) this.mSurfaceHeight))), (int) ((left + width) * ((float) this.mSurfaceWidth)), (int) (((float) this.mSurfaceHeight) - (((float) this.mSurfaceHeight) * top)));
    }

    public void release() {
        releaseFrameBuffers();
    }
}
