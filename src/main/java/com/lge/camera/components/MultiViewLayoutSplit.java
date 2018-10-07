package com.lge.camera.components;

import android.graphics.Point;
import android.graphics.Rect;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class MultiViewLayoutSplit extends MultiViewLayout {
    public MultiViewLayoutSplit(int degree, MultiViewLayoutInfo info) {
        this.mInfo = info;
        initTextureCoord(degree, -1);
    }

    public String getLayoutName() {
        return LdbConstants.LDB_MULTIVIEW_LAYOUT_SPLIT_TXT;
    }

    public int getFrameShotMaxCount() {
        return 2;
    }

    public int getTouchedViewIndex(float x, float y) {
        CamLog.m3d(CameraConstants.TAG, "MultiViewLayout.sLcdHeight : " + MultiViewLayout.sLcdHeight);
        int y_boundary = (int) (((float) MultiViewLayout.sLcdHeight) * 0.5f);
        CamLog.m3d(CameraConstants.TAG, "y_boundary : " + y_boundary);
        if (y > ((float) y_boundary)) {
            return 1;
        }
        return 0;
    }

    protected float[] calculateTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        System.arraycopy(this.mInfo.getTextureVariation(0), cameraId * 8, texture, viewIndex * 8, 8);
        if (cameraId == 4) {
            System.arraycopy(getExternalCameraTexCoord(0), getOrientationIndex(degree) * 8, texture, viewIndex * 8, 8);
        }
        return (float[]) texture.clone();
    }

    protected float[] getFrontCollageTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        int start = 0;
        if (FunctionProperties.multiviewFrontRearWideSupported()) {
            if (cameraId == 1) {
                start = 0;
            } else {
                start = 8;
            }
        }
        System.arraycopy(this.mInfo.getFrontCollageTex(0), start, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    public Rect getViewRect(int viewIndex) {
        int y_boundary = (int) (((float) MultiViewLayout.sLcdHeight) * 0.5f);
        Rect rect = new Rect();
        if (viewIndex == 0) {
            rect.left = 0;
            rect.right = MultiViewLayout.sLcdWidth;
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
        this.mGuideTextLocationArray.add(new int[]{(getViewRect(0).right / 2) - (textWidth / 2), getViewRect(0).bottom - textHeight});
        this.mGuideTextLocationArray.add(new int[]{(getViewRect(1).right / 2) - (textWidth / 2), getViewRect(1).top});
        return this.mGuideTextLocationArray;
    }

    public Point getCenterPoint() {
        return new Point((int) (((float) MultiViewLayout.sLcdWidth) * 0.5f), (int) (((float) MultiViewLayout.sLcdHeight) * 0.5f));
    }
}
