package com.lge.camera.managers.ext;

public interface SpliceViewListener {
    int[] getCameraArray();

    int getCurrentDegreeForImportImg();

    void onRotate();

    void onRotateDegree(int i);

    void onSwap();
}
