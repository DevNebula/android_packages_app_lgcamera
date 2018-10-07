package com.lge.camera.components;

import android.graphics.Point;
import android.graphics.Rect;
import com.lge.camera.constants.LdbConstants;
import java.util.ArrayList;

public class MultiViewLayoutSingle extends MultiViewLayout {
    public MultiViewLayoutSingle(int degree, MultiViewLayoutInfo info) {
        this.mInfo = info;
        initTextureCoord(degree, -1);
    }

    public String getLayoutName() {
        return LdbConstants.LDB_MULTIVIEW_LAYOUT_SINGLE_TXT;
    }

    public int getFrameShotMaxCount() {
        return 1;
    }

    public int getTouchedViewIndex(float x, float y) {
        return 0;
    }

    protected float[] calculateTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        System.arraycopy(this.mInfo.getTextureVariation(0), cameraId * 8, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    protected float[] getFrontCollageTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        System.arraycopy(this.mInfo.getFrontCollageTex(0), 0, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    public Rect getViewRect(int viewIndex) {
        Rect rect = new Rect();
        if (viewIndex == 0) {
            rect.left = 0;
            rect.right = MultiViewLayout.sLcdWidth;
            rect.top = 0;
            rect.bottom = MultiViewLayout.sLcdHeight;
        }
        return rect;
    }

    public ArrayList<int[]> getGuideTextLocation(int textWidth, int textHeight) {
        this.mGuideTextLocationArray.clear();
        this.mGuideTextLocationArray.add(new int[]{(getViewRect(0).right / 2) - (textWidth / 2), ((getViewRect(0).bottom / 2) - textWidth) - textHeight});
        return this.mGuideTextLocationArray;
    }

    public Point getCenterPoint() {
        return new Point(MultiViewLayout.sLcdWidth, MultiViewLayout.sLcdHeight);
    }
}
