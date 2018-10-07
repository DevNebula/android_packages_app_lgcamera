package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class CameraFocusFaceView extends CameraFocusView {
    private Rect mDrawRect;
    private int mFaceCount;
    private NinePatchDrawable mFaceHorizon;
    private Rect[] mFaceRect;
    private NinePatchDrawable mFaceVertical;
    private float mPadding;
    private int mSrcPreviewHeight;
    private int mSrcPreviewWidth;

    public CameraFocusFaceView(Context context) {
        super(context);
        this.mFaceRect = new Rect[5];
        this.mDrawRect = new Rect();
        this.mPadding = 0.0f;
        this.mSrcPreviewWidth = 0;
        this.mSrcPreviewHeight = 0;
        this.mFaceHorizon = null;
        this.mFaceVertical = null;
        this.mPadding = Utils.dpToPx(context, 6.0f);
        setWillNotDraw(false);
    }

    public CameraFocusFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFaceRect = new Rect[5];
        this.mDrawRect = new Rect();
        this.mPadding = 0.0f;
        this.mSrcPreviewWidth = 0;
        this.mSrcPreviewHeight = 0;
        this.mFaceHorizon = null;
        this.mFaceVertical = null;
        this.mPadding = Utils.dpToPx(context, 6.0f);
    }

    public CameraFocusFaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mFaceRect = new Rect[5];
        this.mDrawRect = new Rect();
        this.mPadding = 0.0f;
        this.mSrcPreviewWidth = 0;
        this.mSrcPreviewHeight = 0;
        this.mFaceHorizon = null;
        this.mFaceVertical = null;
        this.mPadding = Utils.dpToPx(context, 6.0f);
    }

    public void init() {
        if (!this.mInit) {
            initResources();
            this.mInit = true;
        }
    }

    public void unbind() {
        this.mInit = false;
        this.mFaceHorizon = null;
        this.mFaceVertical = null;
    }

    public void initResources() {
        this.mFaceHorizon = getNinePatchDrawable(C0088R.drawable.camera_focus_face_on);
        this.mFaceVertical = getNinePatchDrawable(C0088R.drawable.camera_focus_face_on_land);
    }

    public void setRectangles(Rect[] rect, int faceCount) {
        if (!this.mInit) {
            init();
        }
        this.mFaceRect = rect;
        this.mFaceCount = faceCount;
        postInvalidate();
    }

    public void resetRectangles() {
        if (!this.mInit) {
            init();
        }
        this.mFaceRect = null;
        this.mFaceCount = 0;
        postInvalidate();
    }

    public void setSrcImageSize(int width, int height) {
        if (Utils.isConfigureLandscape(getResources())) {
            this.mSrcPreviewWidth = width;
            this.mSrcPreviewHeight = height;
            return;
        }
        this.mSrcPreviewWidth = height;
        this.mSrcPreviewHeight = width;
    }

    public void setState(int state) {
        if (!this.mInit) {
            init();
        }
        super.setState(state);
        setImageDrawable(isHorizontal() ? this.mFaceHorizon : this.mFaceVertical);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mInit && getVisibility() == 0) {
            Drawable drawable = getDrawable();
            if (drawable != null && this.mFaceCount > 0) {
                if (!(getMeasuredWidth() == this.mSrcPreviewWidth || getMeasuredHeight() == this.mSrcPreviewHeight)) {
                    canvas.scale(((float) getMeasuredWidth()) / ((float) this.mSrcPreviewWidth), ((float) getMeasuredHeight()) / ((float) this.mSrcPreviewHeight));
                }
                for (int i = 0; i < this.mFaceCount; i++) {
                    if (Utils.isConfigureLandscape(getResources())) {
                        this.mDrawRect.left = this.mFaceRect[i].left - ((int) this.mPadding);
                        this.mDrawRect.right = this.mFaceRect[i].right + ((int) this.mPadding);
                        this.mDrawRect.top = this.mFaceRect[i].top - ((int) this.mPadding);
                        this.mDrawRect.bottom = this.mFaceRect[i].bottom + ((int) this.mPadding);
                    } else {
                        this.mDrawRect.left = (this.mSrcPreviewWidth - this.mFaceRect[i].bottom) - ((int) this.mPadding);
                        this.mDrawRect.right = (this.mSrcPreviewWidth - this.mFaceRect[i].top) + ((int) this.mPadding);
                        this.mDrawRect.top = this.mFaceRect[i].left - ((int) this.mPadding);
                        this.mDrawRect.bottom = this.mFaceRect[i].right + ((int) this.mPadding);
                    }
                    drawable.setBounds(this.mDrawRect);
                    drawable.draw(canvas);
                }
            }
        }
    }
}
