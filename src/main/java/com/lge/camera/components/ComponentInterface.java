package com.lge.camera.components;

import android.app.Activity;
import android.view.View;

public interface ComponentInterface {
    View findViewById(int i);

    Activity getActivity();

    void postOnUiThread(Object obj, long j);

    void removePostRunnable(Object obj);

    void runOnUiThread(Object obj);
}
