package com.lge.camera.managers.ext;

public interface SmartCamInterface {
    void applyFilterToSceneTextSelected(String str);

    void hideFocus();

    void hideZoomBar();

    boolean isShowingFilmMenu();

    void isStartWideAngleAnimation(boolean z);

    void notifySceneChanged(String str, String str2);

    void onMotionHandShakingBinningReset();

    void onSmartCamBarShow(boolean z);

    void resetAllparamFunction(int i, int i2, boolean z);

    void resetAutoContrastSolution(boolean z, boolean z2);

    void setAutoContrastSolution(boolean z);

    void setEVParam(int i);

    void setFilterListForSmartCam(String str, int i);

    void showFilmMenu(boolean z);

    void updateAutoContrastSolution(int i);

    void updateContrastParam(int i);
}
