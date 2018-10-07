package com.lge.camera.components;

public class MultiViewLayoutInfoQuadFrontRearWide extends MultiViewLayoutInfoQuad {
    protected final float X_FRONTWIDE_WIDTH = 1.0f;
    protected final float X_FRONT_PADDING = 0.16999999f;
    protected final float Y_FRONTWIDE_HEIGHT = 0.75f;
    protected final float Y_FRONT_PADDING = 0.2525f;

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.725f, 0.725f, 0.725f, 0.275f, 0.275f, 0.725f, 0.275f, 0.275f};
        this.mTextureCoord = new float[]{0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 0.16999999f, 0.7475f, 0.16999999f, 0.2525f, 0.83000004f, 0.7475f, 0.83000004f, 0.2525f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.725f, 0.725f, 0.725f, 0.275f, 0.275f, 0.725f, 0.275f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.725f, 0.725f, 0.725f, 0.275f, 0.275f, 0.725f, 0.275f, 0.275f, 0.16999999f, 0.7475f, 0.16999999f, 0.2525f, 0.83000004f, 0.7475f, 0.83000004f, 0.2525f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.2525f, 0.83000004f, 0.7475f, 0.83000004f, 0.2525f, 0.16999999f, 0.7475f, 0.16999999f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f}};
        this.mTextureVariation[0] = transformTextureFrontRearWide(this.mTextureVariation[0]);
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{1, 3, 2, 0};
    }
}
