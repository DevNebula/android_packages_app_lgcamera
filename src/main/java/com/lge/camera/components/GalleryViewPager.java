package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class GalleryViewPager extends GalleryViewPagerBase {
    private boolean mIsEnabled = true;
    private OnGalleryViewPagerListener mListener;

    public interface OnGalleryViewPagerListener {
        void onChangeViewPagerTouchState(boolean z);
    }

    public GalleryViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryViewPager(Context context) {
        super(context);
    }

    public void setGalleryViewPagerListener(OnGalleryViewPagerListener listener) {
        this.mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mIsEnabled) {
            return true;
        }
        switch (event.getAction()) {
            case 0:
                if (this.mListener != null) {
                    this.mListener.onChangeViewPagerTouchState(true);
                    break;
                }
                break;
            case 1:
                if (this.mListener != null) {
                    this.mListener.onChangeViewPagerTouchState(false);
                    break;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setEnableScroll(boolean set) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] enable view page scroll : " + set);
        this.mIsEnabled = set;
    }
}
