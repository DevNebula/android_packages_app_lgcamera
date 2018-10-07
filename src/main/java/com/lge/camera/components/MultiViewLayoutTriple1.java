package com.lge.camera.components;

import android.graphics.Point;
import android.graphics.Rect;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import java.util.ArrayList;

public class MultiViewLayoutTriple1 extends MultiViewLayout {
    public MultiViewLayoutTriple1(int degree, MultiViewLayoutInfo info) {
        this.mInfo = info;
        initTextureCoord(degree, -1);
    }

    public String getLayoutName() {
        return LdbConstants.LDB_MULTIVIEW_LAYOUT_TRIPLE1_TXT;
    }

    public int getFrameShotMaxCount() {
        return 3;
    }

    public int getTouchedViewIndex(float x, float y) {
        int x_boundary = (int) (((float) MultiViewLayout.sLcdWidth) * 0.5f);
        if (y > ((float) ((int) (((float) MultiViewLayout.sLcdHeight) * 0.375f)))) {
            return 2;
        }
        if (x > ((float) x_boundary)) {
            return 0;
        }
        return 1;
    }

    protected float[] calculateTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        boolean isFirstSecondView = true;
        if (!(viewIndex == 0 || viewIndex == 1)) {
            isFirstSecondView = false;
        }
        System.arraycopy(isFirstSecondView ? this.mInfo.getTextureVariation(0) : this.mInfo.getTextureVariation(2), cameraId * 8, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    protected float[] getFrontCollageTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        boolean isFirstSecondView;
        if (viewIndex == 0 || viewIndex == 1) {
            isFirstSecondView = true;
        } else {
            isFirstSecondView = false;
        }
        int start = 0;
        if (FunctionProperties.multiviewFrontRearWideSupported()) {
            if (cameraId == 1) {
                start = 0;
            } else {
                start = 8;
            }
        }
        System.arraycopy(isFirstSecondView ? this.mInfo.getFrontCollageTex(0) : this.mInfo.getFrontCollageTex(2), start, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    public Rect getViewRect(int viewIndex) {
        int x_boundary = (int) (((float) MultiViewLayout.sLcdWidth) * 0.5f);
        int y_boundary = (int) (((float) MultiViewLayout.sLcdHeight) * 0.375f);
        Rect rect = new Rect();
        if (viewIndex == 0) {
            rect.left = x_boundary;
            rect.right = MultiViewLayout.sLcdWidth;
            rect.top = 0;
            rect.bottom = y_boundary;
        } else if (viewIndex == 1) {
            rect.left = 0;
            rect.right = x_boundary;
            rect.top = 0;
            rect.bottom = y_boundary;
        } else {
            rect.left = 0;
            rect.right = MultiViewLayout.sLcdWidth;
            rect.top = y_boundary;
            rect.bottom = MultiViewLayout.sLcdHeight;
        }
        return rect;
    }

    public ArrayList<int[]> getGuideTextLocation(int textWidth, int textHeight) {
        this.mGuideTextLocationArray.clear();
        this.mGuideTextLocationArray.add(new int[]{getViewRect(0).left, getViewRect(0).bottom - textHeight});
        this.mGuideTextLocationArray.add(new int[]{getViewRect(1).right - textWidth, getViewRect(1).bottom - textHeight});
        this.mGuideTextLocationArray.add(new int[]{getViewRect(1).right - (textWidth / 2), getViewRect(2).top});
        return this.mGuideTextLocationArray;
    }

    public Point getCenterPoint() {
        return new Point((int) (((float) MultiViewLayout.sLcdWidth) * 0.5f), (int) (((float) MultiViewLayout.sLcdHeight) * 0.375f));
    }
}
