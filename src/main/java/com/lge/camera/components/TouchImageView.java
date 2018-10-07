package com.lge.camera.components;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.ImageView.ScaleType;
import com.lge.camera.components.TouchImageViewBase.OnTouchImageViewListener;

public class TouchImageView extends TouchImageViewBase {
    public TouchImageView(Context context) {
        super(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnTouchImageViewListener(OnTouchImageViewListener l) {
        this.touchImageViewListener = l;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener l) {
        this.doubleTapListener = l;
    }

    public void setTouchImageViewInterface(TouchImageViewInterface listener) {
        this.mGet = listener;
    }

    public void setOnTouchListener(OnTouchListener l) {
        this.userTouchListener = l;
    }

    public void setImageResource(int resId) {
        super.setImageResource(resId);
        savePreviousImageValues();
        fitImageToView();
    }

    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        savePreviousImageValues();
        fitImageToView();
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        savePreviousImageValues();
        fitImageToView();
    }

    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        savePreviousImageValues();
        fitImageToView();
    }

    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putFloat("saveScale", this.normalizedScale);
        bundle.putFloat("matchViewHeight", this.matchViewHeight);
        bundle.putFloat("matchViewWidth", this.matchViewWidth);
        bundle.putInt("viewWidth", this.viewWidth);
        bundle.putInt("viewHeight", this.viewHeight);
        this.matrix.getValues(this.f17m);
        bundle.putFloatArray("matrix", this.f17m);
        bundle.putBoolean("imageRendered", this.imageRenderedAtLeastOnce);
        return bundle;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.normalizedScale = bundle.getFloat("saveScale");
            this.f17m = bundle.getFloatArray("matrix");
            this.prevMatrix.setValues(this.f17m);
            this.prevMatchViewHeight = bundle.getFloat("matchViewHeight");
            this.prevMatchViewWidth = bundle.getFloat("matchViewWidth");
            this.prevViewHeight = bundle.getInt("viewHeight");
            this.prevViewWidth = bundle.getInt("viewWidth");
            this.imageRenderedAtLeastOnce = bundle.getBoolean("imageRendered");
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        savePreviousImageValues();
    }

    public void setScaleType(ScaleType type) {
        if (type == ScaleType.FIT_START || type == ScaleType.FIT_END) {
            throw new UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END");
        } else if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX);
        } else {
            this.mScaleType = type;
            if (this.onDrawReady) {
                setZoom(this);
            }
        }
    }

    public ScaleType getScaleType() {
        return this.mScaleType;
    }

    public RectF getZoomedRect() {
        if (this.mScaleType == ScaleType.FIT_XY) {
            throw new UnsupportedOperationException("getZoomedRect() not supported with FIT_XY");
        }
        PointF topLeft = transformCoordTouchToBitmap(0.0f, 0.0f, true);
        PointF bottomRight = transformCoordTouchToBitmap((float) this.viewWidth, (float) this.viewHeight, true);
        float w = (float) getDrawable().getIntrinsicWidth();
        float h = (float) getDrawable().getIntrinsicHeight();
        return new RectF(topLeft.x / w, topLeft.y / h, bottomRight.x / w, bottomRight.y / h);
    }

    public boolean isZoomed() {
        return this.normalizedScale != 1.0f;
    }

    public void setZoom(float scale) {
        setZoom(scale, 0.5f, 0.5f);
    }

    public void setZoom(float scale, float focusX, float focusY) {
        setZoom(scale, focusX, focusY, this.mScaleType);
    }

    public void setZoom(TouchImageView img) {
        PointF center = img.getScrollPosition();
        if (center != null) {
            setZoom(img.getCurrentZoom(), center.x, center.y, img.getScaleType());
        }
    }

    public void setZoom(TouchImageView img, float scale) {
        PointF center = img.getScrollPosition();
        if (center != null) {
            setZoom(scale, center.x, center.y, img.getScaleType(), false);
        }
    }

    public void resetZoom() {
        this.normalizedScale = 1.0f;
        fitImageToView();
    }

    public float getCurrentZoom() {
        return this.normalizedScale;
    }

    public float getMaxZoom() {
        return this.maxScale;
    }

    public void setMaxZoom(float max) {
        this.maxScale = max;
        this.superMaxScale = 1.0f * this.maxScale;
    }

    public float getMinZoom() {
        return this.minScale;
    }

    public void setMinZoom(float min) {
        this.minScale = min;
        this.superMinScale = 1.0f * this.minScale;
    }

    public void setScrollPosition(float focusX, float focusY) {
        setZoom(this.normalizedScale, focusX, focusY);
    }

    public boolean canScrollHorizontally(int direction) {
        if (!isZoomed()) {
            return false;
        }
        this.matrix.getValues(this.f17m);
        float x = this.f17m[2];
        if (getImageWidth() < ((float) this.viewWidth)) {
            return false;
        }
        if (x >= -1.0f && direction < 0) {
            return false;
        }
        if ((Math.abs(x) + ((float) this.viewWidth)) + 1.0f < getImageWidth() || direction <= 0) {
            return true;
        }
        return false;
    }

    protected void printMatrixInfo() {
        float[] n = new float[9];
        this.matrix.getValues(n);
        Log.d("DEBUG", "Scale: " + n[0] + " TransX: " + n[2] + " TransY: " + n[5]);
    }

    protected int setViewSize(int mode, int size, int drawableWidth) {
        switch (mode) {
            case Integer.MIN_VALUE:
                return Math.min(drawableWidth, size);
            case 0:
                return drawableWidth;
            case 1073741824:
                return size;
            default:
                return size;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        this.viewWidth = setViewSize(widthMode, widthSize, drawableWidth);
        this.viewHeight = setViewSize(heightMode, heightSize, drawableHeight);
        setMeasuredDimension(this.viewWidth, this.viewHeight);
        fitImageToView();
    }
}
