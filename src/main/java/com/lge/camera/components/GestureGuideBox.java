package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class GestureGuideBox extends CameraFocusView {
    private NinePatchDrawable mBoxHorizon = null;
    private NinePatchDrawable mBoxVertical = null;
    private int mHandHeight = 0;
    private int mHandWidth = 0;
    private int mLeftTopX = 0;
    private int mLeftTopY = 0;
    private int mPreviewHeight = 1;
    private int mPreviewWidth = 1;

    public GestureGuideBox(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public GestureGuideBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureGuideBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init() {
        if (!this.mInit) {
            initResources();
            this.mInit = true;
        }
    }

    public void setInitialDegree(int degree) {
        this.mDegree = degree;
        setState(0);
    }

    public void unbind() {
        this.mInit = false;
        this.mBoxHorizon = null;
        this.mBoxVertical = null;
    }

    public void initResources() {
        this.mBoxHorizon = getNinePatchDrawable(C0088R.drawable.camera_focus_gesture_on);
        this.mBoxVertical = getNinePatchDrawable(C0088R.drawable.camera_focus_gesture_on_land);
    }

    public void setState(int state) {
        if (this.mInit) {
            setImageDrawable(isHorizontal() ? this.mBoxHorizon : this.mBoxVertical);
            invalidate();
        }
    }

    public void setRectangleArea(int x, int y, int w, int h) {
        if (this.mInit) {
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            this.mLeftTopX = x;
            this.mLeftTopY = y;
            this.mHandWidth = w;
            this.mHandHeight = h;
            invalidate();
        }
    }

    public void setPreviewSize(Context context, int previewW, int previewH) {
        if (context != null) {
            if (Utils.isConfigureLandscape(context.getResources())) {
                this.mPreviewWidth = previewW;
                this.mPreviewHeight = previewH;
                return;
            }
            this.mPreviewWidth = previewH;
            this.mPreviewHeight = previewW;
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mInit && getVisibility() == 0) {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            if ((this.mLeftTopX != 0 || this.mLeftTopY != 0 || this.mHandWidth != 0 || this.mHandHeight != 0) && this.mPreviewWidth != 0 && this.mPreviewHeight != 0) {
                float rateW = ((float) getMeasuredWidth()) / ((float) this.mPreviewWidth);
                float rateH = ((float) getMeasuredHeight()) / ((float) this.mPreviewHeight);
                drawable.setBounds((int) (((float) this.mLeftTopX) * rateW), (int) (((float) this.mLeftTopY) * rateH), (int) (((float) (this.mLeftTopX + this.mHandWidth)) * rateW), (int) (((float) (this.mLeftTopY + this.mHandHeight)) * rateH));
                drawable.draw(canvas);
            }
        }
    }
}
