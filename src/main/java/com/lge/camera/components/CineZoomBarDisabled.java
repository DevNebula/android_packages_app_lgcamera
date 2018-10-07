package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.lge.camera.C0088R;

public class CineZoomBarDisabled extends HorizontalSeekBar {
    protected Drawable mCineZoomBarDisabledBitmap;

    public CineZoomBarDisabled(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDrawables();
    }

    public CineZoomBarDisabled(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawables();
    }

    public CineZoomBarDisabled(Context context) {
        super(context);
    }

    protected void initDrawables() {
        super.initDrawables();
        this.mCursorNormal = getContext().getDrawable(C0088R.drawable.ic_camera_cine_jog_handler_disable);
        this.mCursorHalfWidth = this.mCursorNormal.getIntrinsicWidth() / 2;
        this.mCursorHalfHeight = this.mCursorNormal.getIntrinsicHeight() / 2;
        this.mCineZoomBarDisabledBitmap = getContext().getDrawable(C0088R.drawable.bg_camera_cine_jog_disable);
    }

    protected void drawBar(Canvas canvas) {
        this.mCineZoomBarDisabledBitmap.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mCineZoomBarDisabledBitmap.setBounds(0, this.mTopMargin, this.mBarWidth, this.mBarHeight + this.mTopMargin);
        this.mCineZoomBarDisabledBitmap.draw(canvas);
    }

    public void unbind() {
        this.mBarBitmap = null;
        this.mCursorNormal = null;
        this.mCineZoomBarDisabledBitmap = null;
    }

    protected boolean doTouchEvent(MotionEvent event) {
        return true;
    }
}
