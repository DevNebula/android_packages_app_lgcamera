package com.lge.camera.postview;

import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.MotionEvent;

public interface PostviewInterface {
    int getPostviewType();

    void getPreviewSize(int i, int i2);

    void init();

    void onConfigurationChanged(Configuration configuration);

    void onDestroy();

    boolean onKeyDown(int i, KeyEvent keyEvent);

    void onPauseAfter();

    void onPauseBefore();

    void onResumeAfter();

    void onResumeBefore();

    void onStop();

    boolean onTouchEvent(MotionEvent motionEvent);

    void postviewShow();

    void releaseLayout();

    void reloadedPostview();

    void setDegree(int i, boolean z);

    void setupLayout();

    void setupPostviewActionBar();
}
