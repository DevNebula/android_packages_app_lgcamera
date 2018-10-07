package com.lge.camera.components;

import com.lge.camera.C0088R;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiViewLayoutInfoSingle extends MultiViewLayoutInfo {
    private final float X_NORMAL_PADDING;
    private final float X_WIDE_WIDTH;
    private final float Y_NORMAL_PADDING;
    private final float Y_WIDE_HEIGHT;

    public MultiViewLayoutInfoSingle() {
        this.X_WIDE_WIDTH = 0.5f;
        this.Y_WIDE_HEIGHT = 1.0f;
        this.X_NORMAL_PADDING = 0.3875f;
        this.Y_NORMAL_PADDING = 0.275f;
        this.mVertices = new float[]{-1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 2.0f, 1.0f, 2.0f};
        this.mTextureCoord = new float[]{0.167f, 1.0f, 0.167f, 0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 0.6125f, 0.725f, 0.6125f, 0.275f, 0.3875f, 0.725f, 0.3875f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.6125f, 0.725f, 0.6125f, 0.275f, 0.3875f, 0.725f, 0.3875f, 0.275f, 0.167f, 1.0f, 0.167f, 0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        this.mTextureVariationExtCam16By9Rotation = new float[][]{new float[]{0.21875f, 0.944f, 0.78125f, 0.944f, 0.21875f, 0.055f, 0.78125f, 0.055f, 0.25f, 0.0f, 0.25f, 1.0f, 0.75f, 0.0f, 0.75f, 1.0f, 0.78125f, 0.055f, 0.21875f, 0.055f, 0.78125f, 0.944f, 0.21875f, 0.944f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        this.mTextureVariationExtCam4By3Rotation = new float[][]{new float[]{0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.169f, 1.0f, 0.169f, 0.0f, 0.078f, 0.0f, 0.922f, 1.0f, 0.078f, 1.0f, 0.922f, 1.0f, 0.169f, 0.0f, 0.169f, 1.0f, 0.833f, 0.0f, 0.833f, 1.0f, 0.922f, 1.0f, 0.078f, 0.0f, 0.922f, 0.0f, 0.078f}};
        this.mTextureVariationExtCam1By1Rotation = new float[][]{new float[]{0.21875f, 0.944f, 0.78125f, 0.944f, 0.21875f, 0.055f, 0.78125f, 0.055f, 0.25f, 0.0f, 0.25f, 1.0f, 0.75f, 0.0f, 0.75f, 1.0f, 0.78125f, 0.055f, 0.21875f, 0.055f, 0.78125f, 0.944f, 0.21875f, 0.944f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        this.mCameraIdArray = new int[]{2};
        this.mDrawableArray = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_01_01)}));
        this.mTextureVariation[0] = transformTexture(this.mTextureVariation[0]);
        initCurrentValues();
    }
}
