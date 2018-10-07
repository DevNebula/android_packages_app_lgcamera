package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.lge.camera.C0088R;

public class CineZoomBar extends HorizontalSeekBar {
    protected Drawable mCineZoomBarBitmap;

    public CineZoomBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDrawables();
    }

    public CineZoomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawables();
    }

    public CineZoomBar(Context context) {
        super(context);
    }

    protected void initDrawables() {
        super.initDrawables();
        this.mCursorNormal = getContext().getDrawable(C0088R.drawable.ic_camera_cine_jog_handler);
        this.mCursorHalfWidth = this.mCursorNormal.getIntrinsicWidth() / 2;
        this.mCursorHalfHeight = this.mCursorNormal.getIntrinsicHeight() / 2;
        this.mCineZoomBarBitmap = getContext().getDrawable(C0088R.drawable.bg_camera_cine_jog);
    }

    protected void drawBar(Canvas canvas) {
        this.mCineZoomBarBitmap.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mCineZoomBarBitmap.setBounds(0, this.mTopMargin, this.mBarWidth, this.mBarHeight + this.mTopMargin);
        this.mCineZoomBarBitmap.draw(canvas);
    }

    public void unbind() {
        this.mBarBitmap = null;
        this.mCursorNormal = null;
        this.mCineZoomBarBitmap = null;
    }
}
