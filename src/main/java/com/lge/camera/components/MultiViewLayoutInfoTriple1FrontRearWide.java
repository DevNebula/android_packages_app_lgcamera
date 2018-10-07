package com.lge.camera.components;

public class MultiViewLayoutInfoTriple1FrontRearWide extends MultiViewLayoutInfoTriple1 {
    protected final float X_FRONTWIDE_WIDTH01 = 1.0f;
    protected final float X_FRONTWIDE_WIDTH02 = 0.83299994f;
    protected final float X_FRONT_PADDING01 = 0.16999999f;
    protected final float X_FRONT_PADDING02 = 0.22511f;
    protected final float Y_FRONTWIDE_HEIGHT01 = 1.0f;
    protected final float Y_FRONTWIDE_HEIGHT02 = 1.0f;
    protected final float Y_FRONT_PADDING01 = 0.16999999f;
    protected final float Y_FRONT_PADDING02 = 0.16999999f;

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.812f, 1.0f, 0.812f, 0.0f, 0.188f, 1.0f, 0.188f, 0.0f, 0.6404f, 0.725f, 0.6404f, 0.275f, 0.3596f, 0.725f, 0.3596f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.66875f, 0.725f, 0.66875f, 0.275f, 0.33125f, 0.725f, 0.33125f, 0.275f, 0.16999999f, 0.83000004f, 0.16999999f, 0.16999999f, 0.83000004f, 0.83000004f, 0.83000004f, 0.16999999f, 0.875f, 1.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.125f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f}, null, new float[]{0.6404f, 0.725f, 0.6404f, 0.275f, 0.3596f, 0.725f, 0.3596f, 0.275f, 0.22511f, 0.83000004f, 0.22511f, 0.16999999f, 0.77489f, 0.83000004f, 0.77489f, 0.16999999f, 0.812f, 1.0f, 0.812f, 0.0f, 0.188f, 1.0f, 0.188f, 0.0f, 0.0835f, 1.0f, 0.0835f, 0.0f, 0.9165f, 1.0f, 0.9165f, 0.0f, 0.875f, 1.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.125f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.16999999f, 0.83000004f, 0.83000004f, 0.83000004f, 0.16999999f, 0.16999999f, 0.83000004f, 0.16999999f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f}, null, new float[]{0.16999999f, 0.77489f, 0.83000004f, 0.77489f, 0.16999999f, 0.22511f, 0.83000004f, 0.22511f, 0.0f, 0.9165f, 1.0f, 0.9165f, 0.0f, 0.0835f, 1.0f, 0.0835f}};
        this.mTextureVariation[0] = transformTextureFrontRearWide(this.mTextureVariation[0]);
        this.mTextureVariation[2] = transformTextureFrontRearWide(this.mTextureVariation[2]);
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{3, 0, 2};
    }
}
