package com.lge.camera.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import com.lge.app.MiniActivity;
import com.lge.camera.device.CameraCapabilities;
import com.lge.camera.managers.UndoInterface;

public interface IActivityBase {
    boolean checkModuleValidate(int i);

    Bitmap composeSignatureImage(Bitmap bitmap, int i);

    void composeSignatureImage(Image image, int i);

    byte[] composeSignatureImage(byte[] bArr, int i);

    void deleteOrUndo(Uri uri, String str, UndoInterface undoInterface);

    View findViewById(int i);

    Activity getActivity();

    Intent getActivityIntent();

    Context getAppContext();

    CameraCapabilities getCameraCapabilities();

    String getCurSettingValue(String str);

    int getFilmState();

    boolean getGifVisibleStatus();

    int getGraphyIndex();

    MiniActivity getMiniActivity();

    int getOrientationDegree();

    Bitmap getSignatureBitmap(int i, int i2, int i3);

    String getSignatureText();

    int getUspBottomMargin();

    boolean hasRunnable(Object obj);

    View inflateStub(int i);

    void initSignatureContent();

    boolean isAttachIntent();

    boolean isFocusTrackingSupported();

    boolean isFromGraphyApp();

    boolean isNeedToStartSignatureActivity(String str);

    boolean isPaused();

    boolean isSettingChildMenuVisible();

    boolean isUspVisible();

    boolean isUspZoneSupportedMode(String str);

    View layoutInflate(int i, ViewGroup viewGroup);

    void postOnUiThread(Object obj);

    void postOnUiThread(Object obj, long j);

    void refreshUspZone(boolean z);

    void removePostRunnable(Object obj);

    void removeUIBeforeModeChange();

    void runOnUiThread(Object obj);

    void saveStartingWindowLayout(int i);

    void setFromGraphyFlag(boolean z);

    void setGIFVisibility(boolean z);

    void setGifVisibleStatus(boolean z);

    void setGraphyIndex(int i);

    void setReturnFromHelp(boolean z);

    void setStartingWindowTheme(String str);

    void setSwitchingAniViewParam(boolean z);

    void setUspVisibility(int i);

    void showToast(String str, long j);

    void startSignatureActivity();

    boolean unselectUspOnBackKey();

    void updateGuideTextSettingMenu(String str, String str2);

    void updateSpecificSettingMenu(String str, String[] strArr, String[] strArr2, int i);
}
