package com.lge.effectEngine2;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EffectPictureFrame extends EffectFrameBase {
    protected static final int BOUNDARY_INPUT_TEXTURE_ID = 2;
    protected static final int MASK_INPUT_TEXTURE_ID = 3;
    protected static final int NORM_INPUT_TEXTURE_ID = 0;
    protected static final int NUM_TEXTURES = 4;
    private static final String TAG = "EffectPictureFrame";
    protected static final int WIDE_INPUT_TEXTURE_ID = 1;
    protected RectF mBoundNormalRectF = null;
    protected RectF mBoundRectf = null;
    protected int mBoundaryHeight;
    protected int mBoundaryWidth;
    protected Rect mConvertedBoundRect = null;
    protected Rect mConvertedNormalRect = null;
    protected float[] mProjectInsideViewMatrix = new float[16];
    protected float[] mProjectOutsideViewMatrix = new float[16];
    protected int mSurfaceHeight;
    protected int mSurfaceWidth;
    protected int[] mTextures = new int[4];

    public EffectPictureFrame(boolean isDualMode, int width, int height) {
        super(isDualMode);
        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        createTextureObject();
        createPrograms();
        createLocations();
        setImageSize(width, height);
    }

    public void createTextureObject() {
        Log.d(TAG, "createTextureObject");
        GLES20.glGenTextures(4, this.mTextures, 0);
        EffectFrameBase.checkGlError("glGenTextures");
        if (this.isDualCameraMode) {
            bindTexture2D(this.mTextures[0]);
        }
        bindTexture2D(this.mTextures[1]);
        bindTexture2D(this.mTextures[2]);
        bindTexture2D(this.mTextures[3]);
    }

    public void release() {
        super.release();
        Log.d(TAG, "deleting textures..");
        GLES20.glDeleteTextures(4, this.mTextures, 0);
    }

    public void setWideTexture(Bitmap data) {
        GLES20.glBindTexture(3553, this.mTextures[1]);
        GLUtils.texImage2D(3553, 0, data, 0);
        EffectFrameBase.checkGlError("setWideTexture");
    }

    public void setNormalTexture(Bitmap data) {
        GLES20.glBindTexture(3553, this.mTextures[0]);
        GLUtils.texImage2D(3553, 0, data, 0);
        EffectFrameBase.checkGlError("setNormalTexture");
    }

    public void setBoundaryTexture(Bitmap data) {
        GLES20.glBindTexture(3553, this.mTextures[2]);
        GLUtils.texImage2D(3553, 0, data, 0);
        EffectFrameBase.checkGlError("setBoundaryTexture");
        this.mBoundaryWidth = data.getWidth();
        this.mBoundaryHeight = data.getHeight();
        Matrix.orthoM(this.mProjectInsideViewMatrix, 0, 0.0f, (float) this.mBoundaryWidth, 0.0f, (float) this.mBoundaryHeight, -1.0f, 1.0f);
        Matrix.translateM(this.mProjectInsideViewMatrix, 0, ((float) this.mBoundaryWidth) / 2.0f, ((float) this.mBoundaryHeight) / 2.0f, 0.0f);
        Matrix.scaleM(this.mProjectInsideViewMatrix, 0, (float) this.mBoundaryWidth, (float) this.mBoundaryHeight, 1.0f);
    }

    public void setMaskTexture(Bitmap mask, RectF relativeCoord) {
        GLES20.glBindTexture(3553, this.mTextures[3]);
        GLUtils.texImage2D(3553, 0, mask, 0);
        EffectFrameBase.checkGlError("setMaskTexutre");
        this.mBoundNormalRectF = relativeCoord;
    }

    public void setBoundaryPos(RectF pos) {
        this.mBoundRectf = pos;
        this.mConvertedBoundRect = convertCoord(pos.left, pos.top, pos.width(), pos.height());
        this.mConvertedNormalRect = convertCoord(pos.left + (this.mBoundNormalRectF.left * pos.width()), pos.top + (this.mBoundNormalRectF.top * pos.height()), pos.width() * this.mBoundNormalRectF.width(), pos.height() * this.mBoundNormalRectF.height());
        Log.d(TAG, "boundrect : " + this.mConvertedBoundRect.left + ", " + this.mConvertedBoundRect.top + ", " + this.mConvertedBoundRect.width() + ", " + this.mConvertedBoundRect.height());
    }

    public void setImageSize(int surfaceWidth, int surfaceHeight) {
        this.mSurfaceWidth = surfaceWidth;
        this.mSurfaceHeight = surfaceHeight;
        createFrameBuffers(this.mSurfaceWidth, this.mSurfaceHeight);
        Log.d(TAG, "iwh : " + surfaceWidth + "x" + surfaceHeight);
        Matrix.orthoM(this.mProjectOutsideViewMatrix, 0, 0.0f, (float) surfaceWidth, 0.0f, (float) surfaceHeight, -1.0f, 1.0f);
        Matrix.translateM(this.mProjectOutsideViewMatrix, 0, ((float) surfaceWidth) / 2.0f, ((float) surfaceHeight) / 2.0f, 0.0f);
        Matrix.scaleM(this.mProjectOutsideViewMatrix, 0, (float) surfaceWidth, (float) surfaceHeight, 1.0f);
    }

    public Rect getBoundaryRect() {
        return this.mConvertedBoundRect;
    }

    public Bitmap draw(int mode, int i) throws OutOfMemoryError {
        int filterWidth = this.mSurfaceWidth / this.scale_of_fbo;
        int filterHeight = this.mSurfaceHeight / this.scale_of_fbo;
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        EffectFrameBase.checkGlError("blend");
        this.mCurrentFrameBuf = 0;
        bindFBOAndTexture(this.mTextures[1], false);
        drawTexture(0, null, 0, 0, filterWidth, filterHeight);
        this.mCurrentFrameBuf = 1;
        filterDraw(mode, filterWidth, filterHeight);
        bindRenderBufferAndTexture();
        drawTexture(0, this.mProjectOutsideViewMatrix, 0, 0, this.mSurfaceWidth, this.mSurfaceHeight);
        EffectFrameBase.checkGlError("draw to render");
        GLES20.glActiveTexture(33984);
        if (this.isDualCameraMode) {
            GLES20.glBindTexture(3553, this.mTextures[0]);
        } else {
            GLES20.glBindTexture(3553, this.mTextures[1]);
        }
        drawTexture(8, this.mProjectInsideViewMatrix, this.mConvertedNormalRect.left, this.mConvertedNormalRect.top, this.mConvertedNormalRect.width(), this.mConvertedNormalRect.height());
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.mTextures[2]);
        drawTexture(0, this.mProjectInsideViewMatrix, this.mConvertedBoundRect.left, this.mConvertedBoundRect.top, this.mConvertedBoundRect.width(), this.mConvertedBoundRect.height());
        GLES20.glDisable(3042);
        ByteBuffer buf = ByteBuffer.allocateDirect((this.mSurfaceWidth * this.mSurfaceHeight) * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, this.mSurfaceWidth, this.mSurfaceHeight, 6408, 5121, buf);
        EffectFrameBase.checkGlError("glReadPixels");
        Bitmap bmp = Bitmap.createBitmap(this.mSurfaceWidth, this.mSurfaceHeight, Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buf);
        return bmp;
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
        int loc;
        switch (programID) {
            case 8:
                loc = GLES20.glGetUniformLocation(this.mPrograms[programID], "mask");
                GLES20.glActiveTexture(33985);
                GLES20.glBindTexture(3553, this.mTextures[3]);
                GLES20.glUniform1i(loc, 1);
                break;
            case 9:
                loc = GLES20.glGetUniformLocation(this.mPrograms[programID], "mask");
                GLES20.glActiveTexture(33985);
                GLES20.glBindTexture(3553, this.mTextures[3]);
                GLES20.glUniform1i(loc, 1);
                break;
        }
        EffectFrameBase.checkGlError("glTextureParamError");
    }

    protected Rect convertCoord(float left, float top, float width, float height) {
        Log.d(TAG, "wh : " + width + "x" + height);
        return new Rect((int) (((float) this.mSurfaceWidth) * left), (int) (((float) this.mSurfaceHeight) * top), (int) ((left + width) * ((float) this.mSurfaceWidth)), (int) ((top + height) * ((float) this.mSurfaceHeight)));
    }
}
