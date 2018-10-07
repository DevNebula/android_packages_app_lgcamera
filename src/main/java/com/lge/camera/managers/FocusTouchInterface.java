package com.lge.camera.managers;

import android.os.Handler;
import android.view.View;

public interface FocusTouchInterface {
    boolean cancelAutoFocus();

    Runnable getReleaseTouchFocusRunnable();

    int getTouchGuideRes();

    boolean hideFocus();

    boolean hideFocusForce();

    boolean initAFView();

    void initFocusAreas();

    void onAEControlBarDown();

    void onAEControlBarUp();

    void release();

    void releaseFocusHandler();

    void releaseTouchFocus();

    void setCameraFocusView(View view);

    void setFocusAreaWindow(int i, int i2, int i3, int i4);

    void setFocusManagerHandler(Handler handler);

    void setFocusState(int i);

    void setFocusViewDegree(int i);

    void setMoveNormalFocusRect(int i, int i2, boolean z);

    boolean showFocus();

    void startFocusByTouchPress(int i, int i2, boolean z);

    void startFocusByTouchPressForTracking(int i, int i2);

    void unregisterCallback();
}
