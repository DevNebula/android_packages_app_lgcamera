package com.lge.camera.components;

import android.view.View;
import com.lge.camera.components.LayoutChangeNotifier.Listener;

public class LayoutChangeHelper implements LayoutChangeNotifier {
    private boolean mFirstTimeLayout = true;
    private Listener mListener;
    private View mView;

    public LayoutChangeHelper(View v) {
        this.mView = v;
    }

    public void setOnLayoutChangeListener(Listener listener) {
        this.mListener = listener;
    }

    public void layout(boolean changed, int l, int t, int r, int b) {
        if (this.mListener != null) {
            if (this.mFirstTimeLayout || changed) {
                this.mFirstTimeLayout = false;
                this.mListener.onLayoutChange(this.mView, l, t, r, b);
            }
        }
    }
}
