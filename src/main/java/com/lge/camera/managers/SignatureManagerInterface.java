package com.lge.camera.managers;

import android.app.Activity;
import android.content.Context;

public interface SignatureManagerInterface {
    Activity getActivity();

    Context getAppContext();

    int getCameraId();

    String getCurSettingValue(String str);

    void setSetting(String str, String str2, boolean z);

    void updateGuideTextSettingMenu(String str, String str2);
}
