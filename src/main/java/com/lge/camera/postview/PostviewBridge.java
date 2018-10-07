package com.lge.camera.postview;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.view.View;

public interface PostviewBridge {
    boolean attatchMediaOnPostview(Uri uri, int i);

    void doCancelClickInDeleteConfirmDialog();

    void doOkClickInDeleteConfirmDialog();

    View findViewById(int i);

    Activity getActivity();

    Context getAppContext();

    String getCurDir();

    Location getCurLocation();

    int getCurPostviewType();

    String getCurSettingValue(String str);

    int getOrientationDegree();

    String getTempDir();

    View inflateStub(int i);

    View inflateView(int i);

    boolean isConfigChanging();

    boolean isPaused();

    boolean isPostviewShowing();

    boolean isProgressDialogVisible();

    boolean isRotateDialogVisible();

    void notifyNewMediaFromPostview(Uri uri);

    void postOnUiThread(Object obj, long j);

    void releasePostview();

    void releasePostviewAfter(int i);

    void removePostRunnable(Object obj);

    void removeRotateDialog();

    void runOnUiThread(Object obj);

    void setOrientationLock(boolean z);

    void setSystemUiVisibilityListener(boolean z);

    void setupOptionMenu(int i);

    void showDialog(int i);

    void showProgressDialog(boolean z, int i);

    void showSavingDialog(boolean z, int i);

    void showToast(String str, long j);
}
