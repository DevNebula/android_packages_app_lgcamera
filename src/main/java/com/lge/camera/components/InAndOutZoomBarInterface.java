package com.lge.camera.components;

public interface InAndOutZoomBarInterface {
    void drawExceedsLevel();

    boolean isDoubleCameraSwitchingAvailable();

    boolean isRunningFilmEmulator();

    void onBarSwitching(boolean z);

    void resetBarDisappearTimer();

    void setValue(int i);

    void stopDrawingExceedsLevel();
}
