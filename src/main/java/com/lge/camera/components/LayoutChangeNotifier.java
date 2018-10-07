package com.lge.camera.components;

import android.view.View;

public interface LayoutChangeNotifier {

    public interface Listener {
        void onLayoutChange(View view, int i, int i2, int i3, int i4);
    }

    void setOnLayoutChangeListener(Listener listener);
}
