package com.lge.camera.managers;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.view.View;
import com.lge.camera.postview.PostviewBase;
import com.lge.camera.postview.PostviewBridge;
import com.lge.camera.util.Utils;

public abstract class PostviewManagerBase extends ManagerInterfaceImpl implements PostviewBridge {
    @SuppressLint({"UseSparseArrays"})
    protected PostviewBase mCurPostview = null;

    public abstract void createPostview();

    public PostviewManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public boolean isPostviewShowing() {
        return this.mCurPostview != null;
    }

    public int getCurPostviewType() {
        return this.mCurPostview != null ? this.mCurPostview.getPostviewType() : -1;
    }

    public void releaseLayout() {
        if (this.mCurPostview != null) {
            this.mCurPostview.releasePostview();
        }
    }

    public void init() {
        createPostview();
        if (this.mCurPostview != null) {
            this.mCurPostview.init();
            int[] size = Utils.sizeStringToArray(this.mGet.getCurrentSelectedPreviewSize());
            this.mCurPostview.getPreviewSize(size[1], size[0]);
        }
    }

    public void onResumeBefore() {
        if (this.mCurPostview != null) {
            this.mCurPostview.onResumeBefore();
        }
        super.onResumeBefore();
    }

    public void onResumeAfter() {
        if (this.mCurPostview != null) {
            this.mCurPostview.onResumeAfter();
        }
    }

    public void onPauseBefore() {
        if (this.mCurPostview != null) {
            this.mCurPostview.onPauseBefore();
        }
    }

    public void onPauseAfter() {
        if (this.mCurPostview != null) {
            this.mCurPostview.onPauseAfter();
        }
        super.onPauseAfter();
    }

    public void onStop() {
        if (this.mCurPostview != null) {
            this.mCurPostview.onStop();
        }
    }

    public void onDestroy() {
        if (this.mCurPostview != null) {
            this.mCurPostview.onDestroy();
            this.mCurPostview.releaseLayout();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        if (this.mCurPostview != null) {
            this.mCurPostview.onConfigurationChanged(config);
        }
        super.onConfigurationChanged(config);
    }

    public View inflateView(int resource) {
        return this.mGet.inflateView(resource);
    }

    public View findViewById(int resource) {
        return this.mGet.findViewById(resource);
    }

    public void setupOptionMenu(int postView) {
        this.mGet.setupOptionMenu(postView);
    }

    public void setSystemUiVisibilityListener(boolean register) {
        this.mGet.setSystemUiVisibilityListener(register);
    }

    public boolean isPaused() {
        return this.mGet.isPaused();
    }

    public boolean isConfigChanging() {
        return this.mGet.isConfigChanging();
    }

    public void runOnUiThread(Object action) {
        this.mGet.runOnUiThread(action);
    }

    public void postOnUiThread(Object action, long delay) {
        this.mGet.postOnUiThread(action, delay);
    }

    public void removePostRunnable(Object object) {
        this.mGet.removePostRunnable(object);
    }

    public void showToast(String message, long hideDelayMillis) {
        this.mGet.showToast(message, hideDelayMillis);
    }

    public String getCurDir() {
        return this.mGet.getCurDir();
    }

    public String getTempDir() {
        return this.mGet.getTempDir();
    }

    public String getCurSettingValue(String key) {
        return this.mGet.getSettingValue(key);
    }

    public Location getCurLocation() {
        return this.mGet.getCurrentLocation();
    }

    public boolean attatchMediaOnPostview(Uri uri, int mediaType) {
        return this.mGet.attatchMediaOnPostview(uri, mediaType);
    }

    public void showProgressDialog(boolean show, int delay) {
        this.mGet.showProcessingDialog(show, delay);
    }

    public void showSavingDialog(boolean show, int delay) {
        this.mGet.showSavingDialog(show, delay);
    }

    public void notifyNewMediaFromPostview(Uri uri) {
        this.mGet.notifyNewMediaFromPostview(uri);
    }

    public void showDialog(int id) {
        this.mGet.showDialog(id);
    }

    public boolean isRotateDialogVisible() {
        return this.mGet.isRotateDialogVisible();
    }

    public void removeRotateDialog() {
        this.mGet.removeRotateDialog();
    }

    public boolean isProgressDialogVisible() {
        return this.mGet.isProgressDialogVisible();
    }

    public void doOkClickInDeleteConfirmDialog() {
        if (this.mCurPostview != null) {
            this.mCurPostview.doOkClickInDeleteConfirmDialog();
        }
    }

    public void doCancelClickInDeleteConfirmDialog() {
        if (this.mCurPostview != null) {
            this.mCurPostview.doCancelClickInDeleteConfirmDialog();
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mCurPostview != null) {
            this.mCurPostview.setDegree(degree, animation);
        }
    }

    public void setOrientationLock(boolean lock) {
        this.mGet.setOrientationLock(lock);
    }
}
