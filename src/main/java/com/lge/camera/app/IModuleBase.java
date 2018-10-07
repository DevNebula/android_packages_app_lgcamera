package com.lge.camera.app;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import com.lge.app.MiniActivity;
import com.lge.camera.device.FaceCommon;

public interface IModuleBase {
    boolean checkInterval(int i);

    boolean checkStorage();

    boolean checkStorage(int i, int i2);

    boolean checkStorage(int i, int i2, int i3, int i4, boolean z);

    void doInitSettingOrder();

    View findViewById(int i);

    Activity getActivity();

    Context getAppContext();

    int getCameraId();

    int getCameraState();

    String getCurDir();

    int getCurStorage();

    Location getCurrentLocation();

    int getDisplayOrientation();

    int getDualviewType();

    Handler getHandler();

    Object getListPreference(String str);

    Object getListPreference(String str, boolean z);

    MiniActivity getMiniActivity();

    int getOrientationDegree();

    String getSettingValue(String str);

    String getShotMode();

    String getStorageDir(int i);

    String getStorageSaveDir(int i);

    String getTempDir();

    int getUspBottomMargin();

    View inflateStub(int i);

    View inflateView(int i);

    View inflateView(int i, ViewGroup viewGroup);

    boolean isAFSupported();

    boolean isAttachIntent();

    boolean isAttachResol();

    boolean isCameraChanging();

    boolean isConfigChanging();

    boolean isFaceDetectionSupported();

    boolean isFocusTrackingSupported();

    boolean isMMSIntent();

    boolean isMWAFSupported();

    boolean isPaused();

    boolean isPostviewShowing();

    boolean isRearCamera();

    boolean isRequestedSingleImage();

    void movePreviewOutOfWindow(boolean z);

    void onFaceDetection(FaceCommon[] faceCommonArr);

    void playSound(int i, boolean z, int i2);

    void postOnUiThread(Object obj, long j);

    void removePostRunnable(Object obj);

    void runOnUiThread(Object obj);

    void setLocationOnByCamera(boolean z);

    void setQuickButtonSelected(int i, boolean z);

    void setSetting(String str, String str2, boolean z);

    void showToast(String str, long j);

    void showToastConstant(String str);

    void updateIndicator(int i, int i2, boolean z);
}
