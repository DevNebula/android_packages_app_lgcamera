package com.lge.camera.util;

import com.lge.camera.constants.CameraConstants;

public abstract class HandlerRunnable implements Runnable {
    private OnRemoveHandler mRemoveHandler = null;

    public interface OnRemoveHandler {
        void onRemoveRunnable(HandlerRunnable handlerRunnable);
    }

    public abstract void handleRun();

    public HandlerRunnable(OnRemoveHandler removeFunc) {
        this.mRemoveHandler = removeFunc;
    }

    public void run() {
        removeRunnable();
        handleRun();
    }

    public void removeRunnable() {
        if (this.mRemoveHandler != null) {
            this.mRemoveHandler.onRemoveRunnable(this);
        } else {
            CamLog.m5e(CameraConstants.TAG, "Can not remove runable. Please Check memory status");
        }
    }
}
