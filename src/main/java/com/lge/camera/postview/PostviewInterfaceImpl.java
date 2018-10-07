package com.lge.camera.postview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;

public abstract class PostviewInterfaceImpl implements PostviewInterface, OnRemoveHandler {
    public PostviewBridge mGet = null;
    public int mPostviewDegree = 0;

    public PostviewInterfaceImpl(PostviewBridge postviewBridge) {
        this.mGet = postviewBridge;
    }

    public Activity getActivity() {
        return this.mGet.getActivity();
    }

    public Context getAppContext() {
        return this.mGet.getAppContext();
    }

    public int getOrientationDegree() {
        return this.mGet.getOrientationDegree();
    }

    public void init() {
    }

    public void onResumeBefore() {
        setDegree(getOrientationDegree(), false);
    }

    public void onResumeAfter() {
    }

    public void onPauseBefore() {
    }

    public void onPauseAfter() {
        this.mPostviewDegree = 0;
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public void onConfigurationChanged(Configuration config) {
        setDegree(getOrientationDegree(), false);
    }

    public void setupPostviewActionBar() {
    }

    public void setupLayout() {
    }

    public void postviewShow() {
    }

    public void reloadedPostview() {
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    public void releaseLayout() {
    }

    public void setDegree(int degree, boolean animation) {
        this.mPostviewDegree = degree;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }
}
