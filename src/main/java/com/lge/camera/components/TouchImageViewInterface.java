package com.lge.camera.components;

public interface TouchImageViewInterface {
    boolean isSystemUIVisible();

    void onClicked();

    void onTouchStateChanged(boolean z);

    void onZoomScaleStart();
}
