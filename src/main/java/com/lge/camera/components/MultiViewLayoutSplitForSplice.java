package com.lge.camera.components;

import com.lge.camera.constants.LdbConstants;

public class MultiViewLayoutSplitForSplice extends MultiViewLayoutSplit {
    public MultiViewLayoutSplitForSplice(int degree, MultiViewLayoutInfo info) {
        super(degree, info);
    }

    public String getLayoutName() {
        return LdbConstants.LDB_MULTIVIEW_LAYOUT_SPLIT_SPLICE_TXT;
    }

    protected float[] calculateTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        if (cameraId == 5) {
            System.arraycopy(getExternalCameraTexCoord(0), getOrientationIndex(degree) * 8, texture, viewIndex * 8, 8);
        } else {
            System.arraycopy(this.mInfo.getTextureVariation(0), cameraId * 8, texture, viewIndex * 8, 8);
        }
        return (float[]) texture.clone();
    }
}
