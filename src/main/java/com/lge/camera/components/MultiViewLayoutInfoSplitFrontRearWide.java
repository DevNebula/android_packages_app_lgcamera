package com.lge.camera.components;

public class MultiViewLayoutInfoSplitFrontRearWide extends MultiViewLayoutInfoSplit {
    protected final float X_FRONTWIDE_WIDTH = 0.666f;
    protected final float X_FRONT_PADDING = 0.28021997f;
    protected final float Y_FRONTWIDE_HEIGHT = 1.0f;
    protected final float Y_FRONT_PADDING = 0.16999999f;

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.167f, 1.0f, 0.167f, 0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 0.6125f, 0.725f, 0.6125f, 0.275f, 0.3875f, 0.725f, 0.3875f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.6125f, 0.725f, 0.6125f, 0.275f, 0.3875f, 0.725f, 0.3875f, 0.275f, 0.28021997f, 0.83000004f, 0.28021997f, 0.16999999f, 0.71978f, 0.83000004f, 0.71978f, 0.16999999f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 0.167f, 1.0f, 0.167f, 0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.16999999f, 0.71978f, 0.83000004f, 0.71978f, 0.16999999f, 0.28021997f, 0.83000004f, 0.28021997f, 0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.167f, 1.0f, 0.167f}};
        this.mTextureVariation[0] = transformTextureFrontRearWide(this.mTextureVariation[0]);
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{3, 2};
    }
}
