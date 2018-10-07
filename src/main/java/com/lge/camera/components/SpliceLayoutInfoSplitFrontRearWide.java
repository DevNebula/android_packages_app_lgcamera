package com.lge.camera.components;

import com.lge.camera.constants.FunctionProperties;

public class SpliceLayoutInfoSplitFrontRearWide extends MultiViewLayoutInfoSplitFrontRearWide {
    protected final float X_FRONTWIDE_WIDTH = 0.666f;
    protected final float X_FRONT_PADDING = 0.28021997f;
    protected final float Y_FRONTWIDE_HEIGHT = 1.0f;
    protected final float Y_FRONT_PADDING = 0.16999999f;

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.133f, 1.0f, 0.133f, 0.0f, 0.867f, 1.0f, 0.867f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 0.6125f, 0.725f, 0.6125f, 0.275f, 0.3875f, 0.725f, 0.3875f, 0.275f};
        if (FunctionProperties.getSupportedHal() != 2) {
            this.mTextureVariation = new float[][]{new float[]{0.67f, 0.8f, 0.67f, 0.2f, 0.33f, 0.8f, 0.33f, 0.2f, 0.22f, 0.867f, 0.22f, 0.133f, 0.78f, 0.867f, 0.78f, 0.133f, 0.8f, 1.0f, 0.8f, 0.0f, 0.2f, 1.0f, 0.2f, 0.0f, 0.133f, 1.0f, 0.133f, 0.0f, 0.867f, 1.0f, 0.867f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        } else {
            this.mTextureVariation = new float[][]{new float[]{0.67f, 0.8f, 0.67f, 0.2f, 0.33f, 0.8f, 0.33f, 0.2f, 0.78f, 0.867f, 0.78f, 0.133f, 0.22f, 0.867f, 0.22f, 0.133f, 0.8f, 1.0f, 0.8f, 0.0f, 0.2f, 1.0f, 0.2f, 0.0f, 0.867f, 1.0f, 0.867f, 0.0f, 0.133f, 1.0f, 0.133f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        }
        if (FunctionProperties.getCameraTypeFront() == 0) {
            System.arraycopy(this.mTextureVariation[0], 24, this.mTextureVariation[0], 8, 8);
        }
        this.mFrontCollageTexture = new float[][]{new float[]{0.133f, 0.78f, 0.867f, 0.78f, 0.133f, 0.22f, 0.867f, 0.22f, 0.0f, 0.867f, 1.0f, 0.867f, 0.0f, 0.133f, 1.0f, 0.133f}};
        this.mTextureVariation[0] = transformTextureFrontRearWide(this.mTextureVariation[0]);
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{1, 0};
    }
}
