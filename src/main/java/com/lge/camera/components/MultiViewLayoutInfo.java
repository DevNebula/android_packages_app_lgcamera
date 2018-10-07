package com.lge.camera.components;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public abstract class MultiViewLayoutInfo {
    public static final int RATIO_16BY9 = 0;
    public static final int RATIO_1BY1 = 2;
    public static final int RATIO_4BY3 = 1;
    protected static int sCurPreviewRatio = 0;
    protected final float FRONT_WIDE_NORMAL_RATIO = 0.66f;
    protected final float REAR_WIDE_NORMAL_RATIO = 0.45f;
    protected int[] mCameraIdArray;
    protected int[] mCurCameraIdArray;
    protected float[] mCurTextureCoord;
    protected float[] mCurVertices;
    protected ArrayList<Integer> mDrawableArray = null;
    protected float[][] mFrontCollageTexture = ((float[][]) null);
    protected float[] mTextureCoord = null;
    protected float[][] mTextureVariation = ((float[][]) null);
    protected float[][] mTextureVariationExtCam16By9Rotation = ((float[][]) null);
    protected float[][] mTextureVariationExtCam1By1Rotation = ((float[][]) null);
    protected float[][] mTextureVariationExtCam4By3Rotation = ((float[][]) null);
    protected float[] mVertices = null;

    public float[] getTextureVariationExtCam(int viewIndex) {
        CamLog.m3d(CameraConstants.TAG, "getTextureVariationExtCam");
        int ratio = getCurPreviewRatio();
        if (ratio == 2) {
            return this.mTextureVariationExtCam1By1Rotation[viewIndex];
        }
        if (ratio == 1) {
            return this.mTextureVariationExtCam4By3Rotation[viewIndex];
        }
        return this.mTextureVariationExtCam16By9Rotation[viewIndex];
    }

    protected void initCurrentValues() {
        this.mCurVertices = (float[]) this.mVertices.clone();
        this.mCurTextureCoord = (float[]) this.mTextureCoord.clone();
        this.mCurCameraIdArray = (int[]) this.mCameraIdArray.clone();
    }

    protected float[] transformTexture(float[] texture) {
        if (texture == null || texture.length != 32) {
            return null;
        }
        int i;
        float[] transformedTexture = new float[texture.length];
        for (i = 0; i < 8; i++) {
            transformedTexture[i] = texture[7 - i];
        }
        transformedTexture[8] = texture[14];
        transformedTexture[9] = texture[15];
        transformedTexture[10] = texture[12];
        transformedTexture[11] = texture[13];
        transformedTexture[12] = texture[10];
        transformedTexture[13] = texture[11];
        transformedTexture[14] = texture[8];
        transformedTexture[15] = texture[9];
        for (i = 0; i < 8; i++) {
            transformedTexture[i + 16] = texture[23 - i];
        }
        for (i = 0; i < 8; i++) {
            transformedTexture[i + 24] = texture[31 - i];
        }
        return transformedTexture;
    }

    protected float[] transformTextureFrontRearWide(float[] texture) {
        if (texture == null || texture.length != 40) {
            return null;
        }
        int i;
        float[] transformedTexture = new float[texture.length];
        for (i = 0; i < 8; i++) {
            transformedTexture[i] = texture[7 - i];
        }
        if (FunctionProperties.getSupportedHal() != 2) {
            transformedTexture[8] = texture[14];
            transformedTexture[9] = texture[15];
            transformedTexture[10] = texture[12];
            transformedTexture[11] = texture[13];
            transformedTexture[12] = texture[10];
            transformedTexture[13] = texture[11];
            transformedTexture[14] = texture[8];
            transformedTexture[15] = texture[9];
        } else {
            for (i = 0; i < 8; i++) {
                transformedTexture[i + 8] = texture[15 - i];
            }
        }
        for (i = 0; i < 8; i++) {
            transformedTexture[i + 16] = texture[23 - i];
        }
        if (FunctionProperties.getSupportedHal() != 2) {
            transformedTexture[24] = texture[30];
            transformedTexture[25] = texture[31];
            transformedTexture[26] = texture[28];
            transformedTexture[27] = texture[29];
            transformedTexture[28] = texture[26];
            transformedTexture[29] = texture[27];
            transformedTexture[30] = texture[24];
            transformedTexture[31] = texture[25];
        } else {
            for (i = 0; i < 8; i++) {
                transformedTexture[i + 24] = texture[31 - i];
            }
        }
        for (i = 0; i < 8; i++) {
            transformedTexture[i + 32] = texture[39 - i];
        }
        return transformedTexture;
    }

    public float[] getCurVertices() {
        return this.mCurVertices;
    }

    public void setCurVertices(float[] vertices) {
        this.mCurVertices = vertices;
    }

    public float[] getCurTextureCoord() {
        return this.mCurTextureCoord;
    }

    public void setCurTextureCoord(float[] coord) {
        this.mCurTextureCoord = coord;
    }

    public int[] getCurCameraIdArray() {
        return this.mCurCameraIdArray;
    }

    public ArrayList<Integer> getDrawableArray() {
        return this.mDrawableArray;
    }

    public void setCurCameraIdArray(int[] array) {
        this.mCurCameraIdArray = (int[]) array.clone();
    }

    public float[] getOriginalVertices() {
        return this.mVertices;
    }

    public int[] getCameraIdArray() {
        return this.mCameraIdArray;
    }

    public float[] getTextureVariation(int index) {
        return this.mTextureVariation[index];
    }

    public void setNetworkCamIdWithInitialCamId() {
        CamLog.m3d(CameraConstants.TAG, "-c1- setNetworkCamIdWithInitialCamId");
        for (int i = 0; i < this.mCameraIdArray.length; i++) {
            if (this.mCameraIdArray[i] == 0) {
                this.mCurCameraIdArray[i] = 4;
            }
        }
    }

    public void removeNetworkCamId() {
        CamLog.m3d(CameraConstants.TAG, "-c1- removeNetworkCamId");
        for (int i = 0; i < this.mCurCameraIdArray.length; i++) {
            if (this.mCurCameraIdArray[i] == 4) {
                this.mCurCameraIdArray[i] = 0;
            }
        }
    }

    public void setCurPreviewRatio(int ratio) {
        CamLog.m3d(CameraConstants.TAG, "-c1- set ratio = " + ratio);
        sCurPreviewRatio = ratio;
    }

    public int getCurPreviewRatio() {
        return sCurPreviewRatio;
    }

    public float[] getFrontCollageTex(int viewIndex) {
        return this.mFrontCollageTexture[viewIndex];
    }
}
