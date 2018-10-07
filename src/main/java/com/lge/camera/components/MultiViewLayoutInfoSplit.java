package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiViewLayoutInfoSplit extends MultiViewLayoutInfo {
    protected final float X_REARWIDE_WIDTH = 0.5f;
    protected final float X_REAR_PADDING = 0.3875f;
    protected final float Y_REARWIDE_HEIGHT = 1.0f;
    protected final float Y_REAR_PADDING = 0.275f;

    public MultiViewLayoutInfoSplit() {
        initVertice();
        initTexture();
        initCameraId();
        initMultiviewGuideIcon();
        initCurrentValues();
        initExtCamTextures();
    }

    protected void initVertice() {
        this.mVertices = new float[]{-1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 2.0f, 1.0f, 2.0f};
    }

    protected void initTexture() {
        this.mTextureCoord = new float[]{0.167f, 1.0f, 0.167f, 0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 0.6125f, 0.725f, 0.6125f, 0.275f, 0.3875f, 0.725f, 0.3875f, 0.275f};
        this.mTextureVariation = new float[][]{new float[]{0.6125f, 0.725f, 0.6125f, 0.275f, 0.3875f, 0.725f, 0.3875f, 0.275f, 0.167f, 1.0f, 0.167f, 0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        this.mFrontCollageTexture = new float[][]{new float[]{0.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.167f, 1.0f, 0.167f}};
        this.mTextureVariation[0] = transformTexture(this.mTextureVariation[0]);
    }

    protected void initCameraId() {
        this.mCameraIdArray = new int[]{1, 2};
    }

    protected void initMultiviewGuideIcon() {
        this.mDrawableArray = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_01_01), Integer.valueOf(C0088R.drawable.camera_multiview_icon_type_01_02)}));
    }

    protected void initExtCamTextures() {
        this.mTextureVariationExtCam16By9Rotation = new float[][]{new float[]{0.1835f, 1.0f, 0.8165f, 1.0f, 0.1835f, 0.0f, 0.8165f, 0.0f, 0.25f, 0.0f, 0.25f, 1.0f, 0.75f, 0.0f, 0.75f, 1.0f, 0.8165f, 0.0f, 0.1835f, 0.0f, 0.8165f, 1.0f, 0.1835f, 1.0f, 0.75f, 1.0f, 0.75f, 0.0f, 0.25f, 1.0f, 0.25f, 0.0f}};
        this.mTextureVariationExtCam4By3Rotation = new float[][]{new float[]{0.078f, 1.0f, 0.922f, 1.0f, 0.078f, 0.0f, 0.922f, 0.0f, 0.167f, 0.0f, 0.167f, 1.0f, 0.833f, 0.0f, 0.833f, 1.0f, 0.922f, 0.0f, 0.078f, 0.0f, 0.922f, 1.0f, 0.078f, 1.0f, 0.833f, 1.0f, 0.833f, 0.0f, 0.167f, 1.0f, 0.167f, 0.0f}};
        this.mTextureVariationExtCam1By1Rotation = new float[][]{new float[]{0.0f, 0.944f, 1.0f, 0.944f, 0.0f, 0.056f, 1.0f, 0.056f, 0.056f, 0.0f, 0.056f, 1.0f, 0.944f, 0.0f, 0.944f, 1.0f, 1.0f, 0.056f, 0.0f, 0.056f, 1.0f, 0.944f, 0.0f, 0.944f, 0.944f, 1.0f, 0.944f, 0.0f, 0.056f, 1.0f, 0.056f, 0.0f}};
    }

    public void setNetworkCamIdWithInitialCamId() {
        CamLog.m3d(CameraConstants.TAG, "-c1- setNetworkCamIdWithInitialCamId split");
        for (int i = 0; i < this.mCameraIdArray.length; i++) {
            if (this.mCameraIdArray[i] == 2) {
                this.mCurCameraIdArray[i] = 4;
            }
        }
    }
}
