package com.lge.camera.components;

import com.lge.camera.C0088R;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiViewLayoutInfoTriple1 extends MultiViewLayoutInfo {
    protected final float X_REARWIDE_WIDTH01 = 0.75f;
    protected final float X_REARWIDE_WIDTH02 = 0.62399995f;
    protected final float X_REAR_PADDING01 = 0.33125f;
    protected final float X_REAR_PADDING02 = 0.3596f;
    protected final float Y_REARWIDE_HEIGHT01 = 1.0f;
    protected final float Y_REARWIDE_HEIGHT02 = 1.0f;
    protected final float Y_REAR_PADDING01 = 0.275f;
    protected final float Y_REAR_PADDING02 = 0.275f;

    public MultiViewLayoutInfoTriple1() {
        initVertice();
        initTexture();
        initCameraId();
        initMultiviewGuideIcon();
        initCurrentValues();
        initExtCamTextures();
    }

    protected void initVertice() {
        this.mVertices = new float[]{0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.25f, 0.0f, 0.25f, -1.0f, 1.0f, 0.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 0.25f, 1.0f, 0.25f};
    }

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.812f, 1.0f, 0.812f, 0.0f, 0.188f, 1.0f, 0.188f, 0.0f, 0.6404f, 0.725f, 0.6404f, 0.275f, 0.3596f, 0.725f, 0.3596f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.66875f, 0.725f, 0.66875f, 0.275f, 0.33125f, 0.725f, 0.33125f, 0.275f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.875f, 1.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.125f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f}, null, new float[]{0.6404f, 0.725f, 0.6404f, 0.275f, 0.3596f, 0.725f, 0.3596f, 0.275f, 0.0835f, 1.0f, 0.0835f, 0.0f, 0.9165f, 1.0f, 0.9165f, 0.0f, 0.812f, 1.0f, 0.812f, 0.0f, 0.188f, 1.0f, 0.188f, 0.0f, 0.875f, 1.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.125f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f}, null, new float[]{0.0f, 0.9165f, 1.0f, 0.9165f, 0.0f, 0.0835f, 1.0f, 0.0835f}};
        this.mTextureVariation[0] = transformTexture(this.mTextureVariation[0]);
        this.mTextureVariation[2] = transformTexture(this.mTextureVariation[2]);
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{1, 0, 2};
    }

    protected void initMultiviewGuideIcon() {
        this.mDrawableArray = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_02_01), Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_02_02), Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_01_02)}));
    }

    protected void initExtCamTextures() {
        this.mTextureVariationExtCam16By9Rotation = new float[][]{new float[]{0.29f, 1.0f, 0.71f, 1.0f, 0.29f, 0.0f, 0.71f, 0.0f, 0.125f, 0.0f, 0.125f, 1.0f, 0.875f, 0.0f, 0.875f, 1.0f, 0.71f, 0.0f, 0.29f, 0.0f, 0.71f, 1.0f, 0.29f, 1.0f, 0.875f, 1.0f, 0.875f, 0.0f, 0.125f, 1.0f, 0.125f, 0.0f}, null, new float[]{0.2468f, 1.0f, 0.7532f, 1.0f, 0.2468f, 0.0f, 0.7532f, 0.0f, 0.1875f, 0.0f, 0.1875f, 1.0f, 0.8125f, 0.0f, 0.8125f, 1.0f, 0.7532f, 0.0f, 0.2468f, 0.0f, 0.7532f, 1.0f, 0.2468f, 1.0f, 0.8125f, 1.0f, 0.8125f, 0.0f, 0.1875f, 1.0f, 0.1875f, 0.0f}};
        this.mTextureVariationExtCam4By3Rotation = new float[][]{new float[]{0.2187f, 1.0f, 0.7813f, 1.0f, 0.2187f, 0.0f, 0.7813f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.7813f, 0.0f, 0.2187f, 0.0f, 0.7813f, 1.0f, 0.2187f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f}, null, new float[]{0.1625f, 1.0f, 0.8375f, 1.0f, 0.1625f, 0.0f, 0.8375f, 0.0f, 0.0835f, 0.0f, 0.0835f, 1.0f, 0.9165f, 0.0f, 0.9165f, 1.0f, 0.8375f, 0.0f, 0.1625f, 0.0f, 0.8375f, 1.0f, 0.1625f, 1.0f, 0.9165f, 1.0f, 0.9165f, 0.0f, 0.0835f, 1.0f, 0.0835f, 0.0f}};
        this.mTextureVariationExtCam1By1Rotation = new float[][]{new float[]{0.125f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f, 0.0f, 0.125f, 0.0f, 0.875f, 1.0f, 0.125f, 1.0f, 0.875f, 0.875f, 0.0f, 0.125f, 0.0f, 0.875f, 1.0f, 0.125f, 1.0f, 1.0f, 0.875f, 1.0f, 0.125f, 0.0f, 0.875f, 0.0f, 0.125f}, null, new float[]{0.05f, 1.0f, 0.95f, 1.0f, 0.05f, 0.0f, 0.95f, 0.0f, 0.0f, 0.05f, 0.0f, 0.95f, 1.0f, 0.05f, 1.0f, 0.95f, 0.95f, 0.0f, 0.05f, 0.0f, 0.95f, 1.0f, 0.05f, 1.0f, 1.0f, 0.95f, 1.0f, 0.05f, 0.0f, 0.95f, 0.0f, 0.05f}};
    }
}
