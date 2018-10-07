package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;

public class JogZoomMinimap extends View {
    private final float CURSOR_START_RATIO = 0.28f;
    private int mCameraId = -1;
    private int mCenterPositionX = 0;
    private Drawable mCursor = null;
    private int mCursorLocation = 0;
    private int mCursorStart = 0;
    private boolean mIsInAndOutZoom = false;
    private int mLayoutHeight = 0;
    private int mMaxZoomValue = 0;
    private float mRatio = 0.0f;
    private NinePatchDrawable mTelephotoBar;
    private NinePatchDrawable mWideAngleBar;
    private int mWideAngleBarBottom = 0;
    private int mWideCameraMaxZoomValue = 0;
    private float mZoomRatio = 0.0f;

    public JogZoomMinimap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public JogZoomMinimap(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JogZoomMinimap(Context context) {
        super(context);
        init();
    }

    protected void init() {
        this.mCursor = getContext().getDrawable(C0088R.drawable.img_shutter_zoom_cursor);
        this.mTelephotoBar = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bar_shutter_zoom_line);
        this.mWideAngleBar = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bar_shutter_zoom_dot);
    }

    protected void onDraw(Canvas canvas) {
        this.mLayoutHeight = canvas.getHeight();
        this.mCenterPositionX = canvas.getWidth() / 2;
        this.mCursorStart = (int) (((float) getWidth()) * 0.28f);
        if (this.mIsInAndOutZoom) {
            drawInAndOutBar(canvas);
        }
        drawZoomBar(canvas);
        drawCursor(canvas);
        super.onDraw(canvas);
    }

    private void drawCursor(Canvas canvas) {
        this.mCursorLocation = ((int) (((float) this.mLayoutHeight) * this.mRatio)) - (this.mCursor.getIntrinsicHeight() / 2);
        if (this.mCursorLocation > this.mLayoutHeight - this.mCursor.getIntrinsicHeight()) {
            this.mCursorLocation = this.mLayoutHeight - this.mCursor.getIntrinsicHeight();
        } else if (this.mCursorLocation < 0) {
            this.mCursorLocation = 0;
        }
        this.mCursor.setBounds(this.mCursorStart, this.mCursorLocation, getWidth() - this.mCursorStart, this.mCursorLocation + this.mCursor.getIntrinsicHeight());
        this.mCursor.draw(canvas);
    }

    private void drawInAndOutBar(Canvas canvas) {
        int maxZoom = FunctionProperties.isSupportedOpticZoom() ? this.mMaxZoomValue : this.mWideCameraMaxZoomValue + CameraConstantsEx.HD_SCREEN_RESOLUTION;
        if (this.mWideAngleBarBottom == 0) {
            this.mWideAngleBarBottom = (int) (((float) (this.mWideCameraMaxZoomValue * this.mLayoutHeight)) / ((float) maxZoom));
            moveInAndOutZoomLine(this.mZoomRatio, this.mCameraId);
        }
        this.mWideAngleBar.setBounds(this.mCenterPositionX - this.mWideAngleBar.getMinimumWidth(), 0, this.mCenterPositionX + this.mWideAngleBar.getMinimumWidth(), this.mWideAngleBarBottom);
        this.mWideAngleBar.draw(canvas);
    }

    private void drawZoomBar(Canvas canvas) {
        this.mTelephotoBar.setBounds(this.mCenterPositionX - this.mTelephotoBar.getMinimumWidth(), this.mIsInAndOutZoom ? this.mWideAngleBarBottom : 0, this.mCenterPositionX + this.mTelephotoBar.getMinimumWidth(), this.mLayoutHeight);
        this.mTelephotoBar.draw(canvas);
    }

    public void setMaxZoomValue(int max) {
        this.mMaxZoomValue = max;
    }

    public void setWideCameraMaxZoomValue(int secondMax) {
        this.mIsInAndOutZoom = true;
        this.mWideCameraMaxZoomValue = secondMax;
    }

    public void moveZoomLine(int zoomValue) {
        this.mRatio = ((float) zoomValue) / ((float) this.mMaxZoomValue);
        invalidate();
    }

    public void moveInAndOutZoomLine(float zoomRatio, int cameraId) {
        this.mCameraId = cameraId;
        this.mZoomRatio = zoomRatio;
        this.mRatio = ((((float) (cameraId == 2 ? this.mWideAngleBarBottom : this.mLayoutHeight - this.mWideAngleBarBottom)) * zoomRatio) + (cameraId == 2 ? 0.0f : (float) this.mWideAngleBarBottom)) / ((float) this.mLayoutHeight);
        invalidate();
    }
}
