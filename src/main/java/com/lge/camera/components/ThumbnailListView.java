package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;
import com.lge.camera.util.Utils;

public class ThumbnailListView extends ListView {
    private boolean mIsEnabled = true;

    public ThumbnailListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailListView(Context context) {
        super(context);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        switch (event.getAction()) {
            case 0:
                if ((!Utils.isRTLLanguage() && ((double) x) > ((double) getWidth()) * 0.7d) || (Utils.isRTLLanguage() && ((double) x) < ((double) getWidth()) * 0.3d)) {
                    this.mIsEnabled = false;
                    break;
                }
                this.mIsEnabled = true;
                break;
                break;
            case 1:
                if (!this.mIsEnabled) {
                    return false;
                }
                break;
            case 2:
                if (!this.mIsEnabled) {
                    return false;
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
