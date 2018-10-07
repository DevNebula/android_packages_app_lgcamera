package com.lge.camera.components;

public class MultiViewLayoutInfoTriple2FrontRearWide extends MultiViewLayoutInfoTriple2 {
    protected final float X_FRONTWIDE_WIDTH01 = 1.0f;
    protected final float X_FRONTWIDE_WIDTH02 = 1.0f;
    protected final float X_FRONT_PADDING01 = 0.16999999f;
    protected final float X_FRONT_PADDING02 = 0.16999999f;
    protected final float Y_FRONTWIDE_HEIGHT01 = 0.75f;
    protected final float Y_FRONTWIDE_HEIGHT02 = 0.375f;
    protected final float Y_FRONT_PADDING01 = 0.2525f;
    protected final float Y_FRONT_PADDING02 = 0.37625f;

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 1.0f, 0.75f, 1.0f, 0.25f, 0.0f, 0.75f, 0.0f, 0.25f, 0.725f, 0.725f, 0.725f, 0.275f, 0.275f, 0.725f, 0.275f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.725f, 0.725f, 0.725f, 0.275f, 0.275f, 0.725f, 0.275f, 0.275f, 0.16999999f, 0.7475f, 0.16999999f, 0.2525f, 0.83000004f, 0.7475f, 0.83000004f, 0.2525f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 1.0f, 0.75f, 1.0f, 0.25f, 0.0f, 0.75f, 0.0f, 0.25f}, new float[]{0.725f, 0.6125f, 0.725f, 0.3875f, 0.275f, 0.6125f, 0.275f, 0.3875f, 0.16999999f, 0.62375f, 0.16999999f, 0.37625f, 0.83000004f, 0.62375f, 0.83000004f, 0.37625f, 1.0f, 0.75f, 1.0f, 0.25f, 0.0f, 0.75f, 0.0f, 0.25f, 0.0f, 0.6875f, 0.0f, 0.3125f, 1.0f, 0.6875f, 1.0f, 0.3125f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.2525f, 0.83000004f, 0.7475f, 0.83000004f, 0.2525f, 0.16999999f, 0.7475f, 0.16999999f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f}, new float[]{0.37625f, 0.83000004f, 0.62375f, 0.83000004f, 0.37625f, 0.16999999f, 0.62375f, 0.16999999f, 0.3125f, 1.0f, 0.6875f, 1.0f, 0.3125f, 0.0f, 0.6875f, 0.0f}};
        this.mTextureVariation[0] = transformTextureFrontRearWide(this.mTextureVariation[0]);
        this.mTextureVariation[1] = transformTextureFrontRearWide(this.mTextureVariation[1]);
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{3, 2, 0};
    }
}
