package com.lge.camera.managers.ext;

public interface OverlapProjectManagerInterface {
    int getOverlapCaptureMode();

    boolean isProjectSelectable();

    void onProjectSelected(int i, boolean z, boolean z2);
}
