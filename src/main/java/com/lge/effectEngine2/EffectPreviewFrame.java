package com.lge.effectEngine2;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class EffectPreviewFrame extends EffectFrameBase {
    protected static final int BOUNDARY_INPUT_TEXTURE_ID = 2;
    protected static final int MASK_INPUT_TEXTURE_ID = 3;
    protected static final int NORM_CAM_OES_TEXTURE_ID = 0;
    protected static final int NUM_TEXTURES = 4;
    private static final String TAG = "EffectPreviewFrame";
    protected static final int WIDE_CAM_OES_TEXTURE_ID = 1;
    protected RectF mBoundNormalRectF = new RectF();
    protected RectF mBoundRectf = new RectF();
    protected int mBoundaryHeight;
    protected int mBoundaryWidth;
    protected Rect mConvertedBoundRect = new Rect();
    protected int mConvertedImageHeight;
    protected int mConvertedImageWidth;
    protected Rect mConvertedNormalRect = new Rect();
    protected int mPreviewHeight;
    protected int mPreviewWidth;
    protected float[] mProjectInsideViewMatrix = new float[16];
    protected float[] mProjectOutsideViewMatrix = new float[16];
    protected int mSurfaceHeight;
    protected int mSurfaceWidth;
    protected int[] mTextures = new int[4];

    public EffectPreviewFrame(boolean isDualMode) {
        super(isDualMode);
    }

    public int[] createTextureObject(int surfaceWidth, int surfaceHeight, int previewWidth, int previewHeight) {
        Log.d(TAG, "createTextureObject");
        this.mSurfaceWidth = surfaceWidth;
        this.mSurfaceHeight = surfaceHeight;
        this.mPreviewWidth = previewWidth;
        this.mPreviewHeight = previewHeight;
        this.mBoundNormalRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        setInnerRectPos(new RectF(0.0f, 0.0f, 1.0f, 1.0f));
        GLES20.glGenTextures(4, this.mTextures, 0);
        EffectFrameBase.checkGlError("glGenTextures");
        if (this.isDualCameraMode) {
            bindTextureOES(this.mTextures[0]);
        }
        bindTextureOES(this.mTextures[1]);
        bindTexture2D(this.mTextures[2]);
        bindTexture2D(this.mTextures[3]);
        return new int[]{this.mTextures[0], this.mTextures[1]};
    }

    public void release() {
        super.release();
        Log.d(TAG, "deleting textures..");
        GLES20.glDeleteTextures(4, this.mTextures, 0);
    }

    public void setBoundaryTexture(Bitmap data) {
        GLES20.glBindTexture(3553, this.mTextures[2]);
        GLES20.glTexParameteri(3553, 10241, 9985);
        GLUtils.texImage2D(3553, 0, data, 0);
        GLES20.glGenerateMipmap(3553);
        EffectFrameBase.checkGlError("setBoundaryTexture");
        this.mBoundaryWidth = data.getWidth();
        this.mBoundaryHeight = data.getHeight();
        Matrix.orthoM(this.mProjectInsideViewMatrix, 0, 0.0f, (float) this.mBoundaryWidth, 0.0f, (float) this.mBoundaryHeight, -1.0f, 1.0f);
        Matrix.translateM(this.mProjectInsideViewMatrix, 0, ((float) this.mBoundaryWidth) / 2.0f, ((float) this.mBoundaryHeight) / 2.0f, 0.0f);
        Matrix.scaleM(this.mProjectInsideViewMatrix, 0, (float) this.mBoundaryWidth, (float) this.mBoundaryHeight, 1.0f);
        Matrix.rotateM(this.mProjectInsideViewMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(this.mProjectInsideViewMatrix, 0, 180.0f, 0.0f, 1.0f, 0.0f);
    }

    public void setMaskTexture(Bitmap mask, RectF relativeCoord) {
        GLES20.glBindTexture(3553, this.mTextures[3]);
        GLES20.glTexParameteri(3553, 10241, 9985);
        GLUtils.texImage2D(3553, 0, mask, 0);
        GLES20.glGenerateMipmap(3553);
        EffectFrameBase.checkGlError("setMaskTexutre");
        this.mBoundNormalRectF = relativeCoord;
    }

    public void setInnerRectPos(RectF pos) {
        this.mBoundRectf = pos;
        this.mConvertedBoundRect = convertCoord(pos.left, pos.top, pos.width(), pos.height());
        this.mConvertedNormalRect = convertCoord(pos.left + (this.mBoundNormalRectF.left * pos.width()), pos.top + (this.mBoundNormalRectF.top * pos.height()), pos.width() * this.mBoundNormalRectF.width(), pos.height() * this.mBoundNormalRectF.height());
    }

    public void setImageSize(int surfaceWidth, int surfaceHeight, int previewWidth, int previewHeight) {
        this.mSurfaceWidth = surfaceWidth;
        this.mSurfaceHeight = surfaceHeight;
        this.mPreviewWidth = previewWidth;
        this.mPreviewHeight = previewHeight;
        releaseFrameBuffers();
        createFrameBuffers(previewWidth, previewHeight);
        setInnerRectPos(this.mBoundRectf);
        Matrix.orthoM(this.mProjectOutsideViewMatrix, 0, 0.0f, (float) surfaceWidth, 0.0f, (float) surfaceHeight, -1.0f, 1.0f);
        Matrix.translateM(this.mProjectOutsideViewMatrix, 0, ((float) surfaceWidth) / 2.0f, ((float) surfaceHeight) / 2.0f, 0.0f);
        Matrix.rotateM(this.mProjectOutsideViewMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(this.mProjectOutsideViewMatrix, 0, 180.0f, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(this.mProjectOutsideViewMatrix, 0, (float) this.mConvertedImageWidth, (float) this.mConvertedImageHeight, 1.0f);
    }

    public Rect getBoundaryRect() {
        return this.mConvertedBoundRect;
    }

    public void draw(int mode) {
        int filterWidth = this.mPreviewWidth / this.scale_of_fbo;
        int filterHeight = this.mPreviewHeight / this.scale_of_fbo;
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        EffectFrameBase.checkGlError("blend");
        this.mCurrentFrameBuf = 0;
        bindFBOAndTexture(this.mTextures[1], true);
        drawTexture(1, null, 0, 0, filterWidth, filterHeight);
        this.mCurrentFrameBuf = 1;
        filterDraw(mode, filterWidth, filterHeight);
        bindRenderBufferAndTexture();
        drawTexture(0, this.mProjectOutsideViewMatrix, 0, 0, this.mSurfaceWidth, this.mSurfaceHeight);
        EffectFrameBase.checkGlError("draw to render");
        GLES20.glActiveTexture(33984);
        if (this.isDualCameraMode) {
            GLES20.glBindTexture(36197, this.mTextures[0]);
        } else {
            GLES20.glBindTexture(36197, this.mTextures[1]);
        }
        drawTexture(9, this.mProjectInsideViewMatrix, this.mConvertedNormalRect.left, this.mConvertedNormalRect.top, this.mConvertedNormalRect.width(), this.mConvertedNormalRect.height());
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.mTextures[2]);
        drawTexture(0, this.mProjectInsideViewMatrix, this.mConvertedBoundRect.left, this.mConvertedBoundRect.top, this.mConvertedBoundRect.width(), this.mConvertedBoundRect.height());
        GLES20.glDisable(3042);
    }

    protected void drawTexture(int programID, float[] mvpMatrix, int x, int y, int width, int height) {
        if (mvpMatrix == null) {
            mvpMatrix = new float[16];
            Matrix.orthoM(mvpMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
            Matrix.translateM(mvpMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
            Matrix.scaleM(mvpMatrix, 0, (float) width, (float) height, 1.0f);
        }
        GLES20.glUseProgram(this.mPrograms[programID]);
        GLES20.glViewport(x, y, width, height);
        EffectFrameBase.checkGlError("glUseProgram : " + programID);
        setVertexParam(programID, width, height, mvpMatrix);
        setTextureParam(programID, width, height);
        EffectFrameBase.checkGlError("glParams : " + programID);
        GLES20.glDrawArrays(5, 0, this.mVertexCount);
        GLES20.glDisableVertexAttribArray(this.maPositionLocs[programID]);
        GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[programID]);
        GLES20.glBindFramebuffer(36160, 0);
        GLES20.glBindRenderbuffer(36161, 0);
        GLES20.glBindTexture(3553, 0);
        GLES20.glUseProgram(0);
    }

    protected void setTextureParam(int programID, int width, int height) {
        super.setTextureParam(programID, width, height);
        switch (programID) {
            case 8:
            case 9:
                int loc = GLES20.glGetUniformLocation(this.mPrograms[programID], "mask");
                GLES20.glActiveTexture(33985);
                GLES20.glBindTexture(3553, this.mTextures[3]);
                GLES20.glUniform1i(loc, 1);
                break;
        }
        EffectFrameBase.checkGlError("glTextureParamError");
    }

    protected Rect convertCoord(float left, float top, float width, float height) {
        int smallDim = Math.min(this.mSurfaceWidth, this.mSurfaceHeight);
        this.mConvertedImageWidth = (int) (((float) smallDim) * (((float) this.mPreviewWidth) / ((float) this.mPreviewHeight)));
        this.mConvertedImageHeight = smallDim;
        Rect src = new Rect((int) (((float) this.mConvertedImageWidth) * left), (int) (((float) this.mConvertedImageHeight) * top), (int) ((left + width) * ((float) this.mConvertedImageWidth)), (int) ((top + height) * ((float) this.mConvertedImageHeight)));
        int rectLeft = this.mSurfaceWidth - src.top;
        int rectTop = ((int) ((((float) this.mSurfaceHeight) / 2.0f) + (((float) this.mConvertedImageWidth) / 2.0f))) - src.left;
        return new Rect(rectLeft - src.height(), rectTop - src.width(), rectLeft, rectTop);
    }
}
