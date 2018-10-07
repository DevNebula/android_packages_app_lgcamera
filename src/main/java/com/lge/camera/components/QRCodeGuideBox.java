package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class QRCodeGuideBox extends CameraFocusView {
    private int mBottom = 0;
    private NinePatchDrawable mBox = null;
    private int mHeight = 0;
    private int mLeft = 0;
    private int mPreviewHeight = 1;
    private int mPreviewWidth = 1;
    private int mRight = 0;
    private int mTop = 0;
    private int mWidth = 0;

    public QRCodeGuideBox(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public QRCodeGuideBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QRCodeGuideBox(Context context, AttributeSet attrs, int defStyle) {
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
        this.mBox = null;
    }

    public void initResources() {
        this.mBox = getNinePatchDrawable(C0088R.drawable.camera_qrcode_focus);
    }

    public void setState(int state) {
        if (this.mInit) {
            setImageDrawable(this.mBox);
            invalidate();
        }
    }

    public void setRectangleArea(int x, int y, int r, int b) {
        if (this.mInit) {
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            if (r > this.mPreviewWidth) {
                r = this.mPreviewWidth;
            }
            if (b > this.mPreviewHeight) {
                b = this.mPreviewHeight;
            }
            this.mLeft = x;
            this.mTop = y;
            this.mRight = r;
            this.mBottom = b;
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

    public void setBitmapSize(Context context, int width, int height) {
        if (context != null) {
            if (Utils.isConfigureLandscape(context.getResources())) {
                this.mWidth = width;
                this.mHeight = width;
                return;
            }
            this.mWidth = height;
            this.mHeight = width;
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mInit && getVisibility() == 0) {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            if ((this.mLeft != 0 || this.mTop != 0 || this.mRight != 0 || this.mBottom != 0) && this.mPreviewWidth != 0 && this.mPreviewHeight != 0 && this.mWidth != 0 && this.mHeight != 0) {
                float rateW = ((float) this.mPreviewWidth) / ((float) this.mWidth);
                float rateH = ((float) this.mPreviewHeight) / ((float) this.mHeight);
                float realW = ((float) getMeasuredWidth()) / ((float) this.mPreviewWidth);
                float realH = ((float) getMeasuredHeight()) / ((float) this.mPreviewHeight);
                drawable.setBounds((int) ((((float) this.mLeft) * rateW) * realW), (int) ((((float) this.mTop) * rateH) * realH), (int) ((((float) this.mRight) * rateW) * realW), (int) ((((float) ((this.mRight - this.mLeft) + this.mTop)) * rateH) * realH));
                drawable.draw(canvas);
            }
        }
    }
}
