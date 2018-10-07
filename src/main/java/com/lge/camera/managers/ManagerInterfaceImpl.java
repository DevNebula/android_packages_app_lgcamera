package com.lge.camera.managers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import com.lge.app.MiniActivity;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.Utils;

public abstract class ManagerInterfaceImpl implements ManagerInterface, OnRemoveHandler {
    public ModuleInterface mGet = null;
    protected int mManagerDegree = -1;

    public ManagerInterfaceImpl(ModuleInterface moduleInterface) {
        this.mGet = moduleInterface;
    }

    public Activity getActivity() {
        return this.mGet.getActivity();
    }

    public MiniActivity getMiniActivity() {
        return this.mGet.getMiniActivity();
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
        this.mManagerDegree = -1;
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public void initializeAfterStartPreviewDone() {
    }

    public void onConfigurationChanged(Configuration config) {
        setDegree(getOrientationDegree(), false);
    }

    public void onChangeModuleAfter() {
    }

    public void onCameraSwitchingStart() {
    }

    public void onCameraSwitchingEnd() {
    }

    public void setRotateDegree(int degree, boolean animation) {
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mManagerDegree != degree) {
            setRotateDegree(degree, animation);
            this.mManagerDegree = degree;
        }
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    public boolean isQuickClip4by3Location() {
        if (this.mGet == null || this.mGet.isRecordingPriorityMode() || CameraConstants.MODE_MULTIVIEW.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(this.mGet.getShotMode())) {
            return false;
        }
        return Utils.calculate4by3Preview(this.mGet.getCurrentSelectedPictureSize());
    }

    public void setListenerAfterOneShotCallback() {
    }

    public boolean isAudioZoomAvailable() {
        if (this.mGet != null) {
            return this.mGet.isAudioZoomAvailable();
        }
        return false;
    }

    public boolean isScreenPinningState() {
        if (this.mGet != null) {
            return this.mGet.isScreenPinningState();
        }
        return false;
    }
}
