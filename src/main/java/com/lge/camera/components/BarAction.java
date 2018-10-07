package com.lge.camera.components;

public interface BarAction extends ComponentInterface {
    String getBarSettingValue(String str);

    int getOrientationDegree();

    String getSettingValue(String str);

    String getShotMode();

    boolean isPaused();

    boolean isPreview4by3();

    boolean isRearCamera();

    void pauseShutterless();

    void resetBarDisappearTimer(int i, int i2);

    void resumeShutterless();

    void rotateSettingBar(int i, int i2, boolean z);

    boolean setBarSetting(String str, String str2, boolean z);

    void switchCamera();

    void updateAllBars(int i, int i2);
}
