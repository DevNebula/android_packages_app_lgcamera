package com.lge.camera.components;

import android.graphics.Rect;
import java.util.ArrayList;

public abstract class MultiViewLayoutBase {
    protected static final float[] TEXTURE_SINGLE_VIEW_RECORDING = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    protected static final float[] VERTEX_SINGLE_VIEW_RECORDING = new float[]{-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    public static int sLcdHeight = 0;
    public static int sLcdWidth = 0;
    protected static int sNumOfCameras = 2;
    protected final int FIRST_VIEW = 0;
    protected final int FOURTH_VIEW = 3;
    protected final int SECOND_VIEW = 1;
    protected final int THIRD_VIEW = 2;
    protected MultiViewLayoutInfo mInfo;
    protected int mLayoutIndex = 0;
    protected int mRotateForGuidePhoto = 0;

    protected abstract float[] calculateTextureCoord(float[] fArr, int i, int i2, int i3);

    public abstract int getFrameShotMaxCount();

    protected abstract float[] getFrontCollageTextureCoord(float[] fArr, int i, int i2, int i3);

    public abstract ArrayList<int[]> getGuideTextLocation(int i, int i2);

    public abstract String getLayoutName();

    public abstract int getTouchedViewIndex(float f, float f2);

    public abstract Rect getViewRect(int i);

    public static float[] getVertexSingleViewRecording() {
        return (float[]) VERTEX_SINGLE_VIEW_RECORDING.clone();
    }

    public static float[] getTextureSingleViewRecording() {
        return (float[]) TEXTURE_SINGLE_VIEW_RECORDING.clone();
    }
}
