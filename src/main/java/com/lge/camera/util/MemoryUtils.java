package com.lge.camera.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.lge.camera.constants.CameraConstants;

public class MemoryUtils {
    public static final boolean MEMORY_REDUCE = false;

    private MemoryUtils() {
    }

    public static void releaseViews(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
                view.setBackgroundDrawable(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    releaseViews(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (UnsupportedOperationException e) {
                    CamLog.m11w(CameraConstants.TAG, "UnsupportedOperationException");
                }
            }
            if (view instanceof ImageView) {
                ((ImageView) view).setImageDrawable(null);
            }
        }
    }

    public static void removeChildViews(ViewGroup view) {
        if (view != null && view.getChildCount() != 0) {
            view.removeAllViews();
        }
    }
}
