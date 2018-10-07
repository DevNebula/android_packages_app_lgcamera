package com.lge.camera.managers;

import android.content.res.Configuration;

public interface ManagerInterface {
    void init();

    void initializeAfterStartPreviewDone();

    boolean isQuickClip4by3Location();

    void onCameraSwitchingEnd();

    void onCameraSwitchingStart();

    void onChangeModuleAfter();

    void onConfigurationChanged(Configuration configuration);

    void onDestroy();

    void onPauseAfter();

    void onPauseBefore();

    void onResumeAfter();

    void onResumeBefore();

    void onStop();

    void setDegree(int i, boolean z);

    void setListenerAfterOneShotCallback();

    void setRotateDegree(int i, boolean z);
}
