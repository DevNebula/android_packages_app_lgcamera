package com.lge.camera.managers;

import android.view.animation.Animation.AnimationListener;
import com.lge.camera.app.IActivityBase;

public interface ConeUIManagerInterface extends IActivityBase {

    public interface OnConeViewModeButtonListener {
        boolean checkOnConeMenuClicked(int i);

        void onConeAutoViewButtonClick();

        void onConeManualCameraButtonClick();

        void onConeManualCameraVideoButtonClick();
    }

    boolean checkFocusingStateWithFlash();

    boolean checkModuleValidate(int i);

    int getCurrentConeMode();

    int getPreviewCoverVisibility();

    String getShotMode();

    boolean isAnimationShowing();

    boolean isCameraChanging();

    boolean isCameraReOpeningAfterInAndOutRecording();

    boolean isModuleChanging();

    boolean isRearCamera();

    boolean isShutterZoomSupported();

    boolean isVideoCameraMode();

    void removeUIBeforeModeChange();

    void saveStartingWindowLayout(int i);

    void setOnConeViewModeButtonListener(OnConeViewModeButtonListener onConeViewModeButtonListener);

    void setPreviewCoverVisibility(int i, boolean z);

    void setPreviewCoverVisibility(int i, boolean z, AnimationListener animationListener, boolean z2, boolean z3);

    void showConeViewMode(boolean z);
}
