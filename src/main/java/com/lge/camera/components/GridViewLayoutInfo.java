package com.lge.camera.components;

import com.lge.camera.util.Utils;

public class GridViewLayoutInfo {
    private static final int MAX_GRID_COUNT = 4;
    protected int[] mCameraIdArray;
    protected int[] mCurCameraIdArray;
    protected float[] mCurTextureCoord;
    protected float[] mCurVertices;
    protected float[][] mFrontCollageTexture = ((float[][]) null);
    protected float[][] mFrontFilmCollageTexture = ((float[][]) null);
    protected float[] mTextureCoord = null;
    protected float[][] mTextureVariation = ((float[][]) null);
    protected float[] mVertices = null;

    public GridViewLayoutInfo() {
        initVertice();
        initTexture();
        initCurrentValues();
    }

    protected void initCurrentValues() {
        this.mCurVertices = (float[]) this.mVertices.clone();
        this.mCurTextureCoord = (float[]) this.mTextureCoord.clone();
        this.mCameraIdArray = new int[4];
        this.mCurCameraIdArray = (int[]) this.mCameraIdArray.clone();
    }

    protected float[] transformTextureFrontRearWide(float[] texture) {
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
        transformedTexture[24] = texture[30];
        transformedTexture[25] = texture[31];
        transformedTexture[26] = texture[28];
        transformedTexture[27] = texture[29];
        transformedTexture[28] = texture[26];
        transformedTexture[29] = texture[27];
        transformedTexture[30] = texture[24];
        transformedTexture[31] = texture[25];
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

    public float[] getOriginalVertices() {
        return this.mVertices;
    }

    public float[] getTextureVariation(int index) {
        return this.mTextureVariation[index];
    }

    public float[] getFrontCollageTex(int viewIndex) {
        return this.mFrontCollageTexture[viewIndex];
    }

    public float[] getFrontFilmCollageTexture(int viewIndex) {
        return this.mFrontFilmCollageTexture[viewIndex];
    }

    protected void initVertice() {
        if (Utils.isRTLLanguage()) {
            this.mVertices = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f};
        } else {
            this.mVertices = new float[]{-1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        }
    }

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.98f, 0.98f, 0.98f, 0.02f, 0.02f, 0.98f, 0.02f, 0.02f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.98f, 0.98f, 0.98f, 0.02f, 0.02f, 0.98f, 0.02f, 0.02f};
        this.mTextureVariation = new float[][]{new float[]{0.98f, 0.98f, 0.98f, 0.02f, 0.02f, 0.98f, 0.02f, 0.02f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.98f, 0.98f, 0.98f, 0.02f, 0.02f, 0.98f, 0.02f, 0.02f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f}};
        this.mFrontFilmCollageTexture = new float[][]{new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f}};
        this.mTextureVariation[0] = transformTextureFrontRearWide(this.mTextureVariation[0]);
    }

    public int[] getCurCameraIdArray() {
        return this.mCurCameraIdArray;
    }

    public int[] getCameraIdArray() {
        return this.mCameraIdArray;
    }

    public void setCameraIdArray(int[] isRearCam) {
        this.mCurCameraIdArray = isRearCam;
    }
}
