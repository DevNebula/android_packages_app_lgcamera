package com.lge.camera.components;

import com.lge.camera.C0088R;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiViewLayoutInfoQuad extends MultiViewLayoutInfo {
    protected final float X_REARWIDE_WIDTH = 1.0f;
    protected final float X_REAR_PADDING = 0.275f;
    protected final float Y_REARWIDE_HEIGHT = 1.0f;
    protected final float Y_REAR_PADDING = 0.275f;

    public MultiViewLayoutInfoQuad() {
        initVertice();
        initTexture();
        initCameraId();
        initMultiviewGuideIcon();
        initCurrentValues();
        initExtCamTextures();
    }

    protected void initMultiviewGuideIcon() {
        this.mDrawableArray = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_02_01), Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_02_02), Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_04_03), Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_03_03)}));
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{1, 1, 2, 0};
    }

    protected void initVertice() {
        this.mVertices = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    }

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.725f, 0.725f, 0.725f, 0.275f, 0.275f, 0.725f, 0.275f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.725f, 0.725f, 0.725f, 0.275f, 0.275f, 0.725f, 0.275f, 0.275f, 0.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f}};
        this.mTextureVariation[0] = transformTexture(this.mTextureVariation[0]);
    }

    protected void initExtCamTextures() {
        this.mTextureVariationExtCam16By9Rotation = new float[][]{new float[]{0.333f, 1.0f, 0.666f, 1.0f, 0.333f, 0.0f, 0.666f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.666f, 0.0f, 0.333f, 0.0f, 0.666f, 1.0f, 0.333f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f}};
        this.mTextureVariationExtCam4By3Rotation = new float[][]{new float[]{0.289f, 1.0f, 0.711f, 1.0f, 0.289f, 0.0f, 0.711f, 0.0f, 0.0f, 0.125f, 0.0f, 0.875f, 1.0f, 0.125f, 1.0f, 0.875f, 0.711f, 0.0f, 0.289f, 0.0f, 0.711f, 1.0f, 0.289f, 1.0f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f, 0.125f}};
        this.mTextureVariationExtCam1By1Rotation = new float[][]{new float[]{0.218f, 1.0f, 0.782f, 1.0f, 0.218f, 0.0f, 0.782f, 0.0f, 0.0f, 0.218f, 0.0f, 0.782f, 1.0f, 0.218f, 1.0f, 0.782f, 0.782f, 0.0f, 0.218f, 0.0f, 0.782f, 1.0f, 0.218f, 1.0f, 1.0f, 0.782f, 1.0f, 0.218f, 0.0f, 0.782f, 0.0f, 0.218f}};
    }
}
