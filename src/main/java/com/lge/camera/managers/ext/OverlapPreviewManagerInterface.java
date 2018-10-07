package com.lge.camera.managers.ext;

public interface OverlapPreviewManagerInterface {
    int getOverlapCaptureMode();

    boolean isSelfie();

    void onSeekBarAnimationEnd();
}
