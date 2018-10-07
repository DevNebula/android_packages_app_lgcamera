package com.lge.camera.managers;

public interface CaptureButtonInterface {
    void onExtraButtonClicked(int i);

    void onShutterBottomButtonClickListener();

    void onShutterBottomButtonFocus(boolean z);

    boolean onShutterBottomButtonLongClickListener();

    void onShutterLargeButtonClicked();

    boolean onShutterLargeButtonLongClicked();

    void onShutterTopButtonClickListener();

    void onShutterTopButtonFocus(boolean z);

    void onShutterTopButtonLongClickListener();

    void onSnapShotButtonClicked();

    void onVideoPauseClicked();

    void onVideoResumeClicked();

    void onVideoShutterClicked();

    void onVideoStopClicked(boolean z, boolean z2);
}
