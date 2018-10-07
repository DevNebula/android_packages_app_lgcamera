package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.hardware.Camera.Area;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.util.BitmapManagingUtil;
import java.util.List;

public abstract class CameraFocusView extends View {
    protected int mDegree = -1;
    protected Drawable mDrawable = null;
    protected boolean mInit = false;
    protected int mState = 0;

    public CameraFocusView(Context context) {
        super(context);
    }

    public CameraFocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraFocusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initResources() {
    }

    public boolean isInitialized() {
        return this.mInit;
    }

    public void unbind() {
        this.mDrawable = null;
    }

    public void init() {
    }

    public void init(List<Area> list, int[] previewSizeOnScreen, int unUsedAreaCount) {
    }

    public void setList(List<Area> list) {
    }

    public void setState(int state) {
        this.mState = state;
    }

    public int getState() {
        return this.mState;
    }

    public void setCenterWindowVisibility(boolean centerShowing) {
    }

    public void refresh(int[] previewSizeOnScreen) {
    }

    public void setSrcImageSize(int width, int height) {
    }

    public void setRectangles(Rect[] rect, int faceCount) {
    }

    public void setRectangleColor(int index, int color) {
    }

    public void resetRectangles() {
    }

    public void startFocusAnimation(int duration, float startScale, float endScale, float px, float py, float startAlpha, float endAlpha) {
    }

    public void setImageDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }

    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public void setImageResource(int resId) {
        setImageDrawable(getResources().getDrawable(resId));
    }

    public void setRotateDrawable(int resId, int degree) {
        setImageDrawable(getBitmapDrawable(resId, degree));
    }

    public BitmapDrawable getBitmapDrawable(int resid, int degree) {
        return new BitmapDrawable(getResources(), BitmapManagingUtil.getRotatedImage(BitmapFactory.decodeResource(getResources(), resid), degree, false));
    }

    public Bitmap getBitmapTypeDrawable(int resid, int degree) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resid);
        Bitmap rotateBmp = BitmapManagingUtil.getRotatedImage(bmp, degree, false);
        if (rotateBmp != null) {
            return rotateBmp.copy(Config.ARGB_8888, true);
        }
        return bmp;
    }

    public void setNinePatchDrawable(int resId) {
        setImageDrawable(getNinePatchDrawable(resId));
    }

    public void setNinePatchRotateDrawable(int resId, int degree) {
        setImageDrawable(getNinePatchRotateDrawable(resId, degree));
    }

    public NinePatchDrawable getNinePatchDrawable(int resid) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resid);
        return new NinePatchDrawable(getResources(), bmp, bmp.getNinePatchChunk(), new Rect(), null);
    }

    public NinePatchDrawable getNinePatchRotateDrawable(int resid, int degree) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resid);
        byte[] chunk = bmp.getNinePatchChunk();
        return new NinePatchDrawable(getResources(), BitmapManagingUtil.getRotatedImage(bmp, degree, false), chunk, new Rect(), null);
    }

    public boolean isHorizontal() {
        return this.mDegree == 0 || this.mDegree == 180;
    }

    public void setDegree(int degree) {
        if (this.mDegree != degree) {
            this.mDegree = degree;
            setState(this.mState);
        }
    }

    public void setFocusLocation(int px, int py) {
    }
}
