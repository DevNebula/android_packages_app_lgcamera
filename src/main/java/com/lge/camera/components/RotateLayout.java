package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class RotateLayout extends RelativeLayout {
    private View mChild;
    private int mOrientation;

    public RotateLayout(Context context) {
        super(context);
    }

    public RotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (getResources() != null) {
            setBackgroundResource(17170445);
        }
    }

    protected void onFinishInflate() {
        this.mChild = getChildAt(0);
        if (this.mChild != null) {
            this.mChild.setPivotX(0.0f);
            this.mChild.setPivotY(0.0f);
        }
    }

    protected void onLayout(boolean change, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        if (this.mChild != null) {
            switch (this.mOrientation) {
                case 0:
                case 180:
                    this.mChild.layout(0, 0, width, height);
                    return;
                case 90:
                case 270:
                    this.mChild.layout(0, 0, height, width);
                    return;
                default:
                    return;
            }
        }
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        int w = 0;
        int h = 0;
        if (this.mChild == null) {
            onFinishInflate();
        }
        if (this.mChild == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        switch (this.mOrientation) {
            case 0:
            case 180:
                measureChild(this.mChild, widthSpec, heightSpec);
                w = this.mChild.getMeasuredWidth();
                h = this.mChild.getMeasuredHeight();
                break;
            case 90:
            case 270:
                measureChild(this.mChild, heightSpec, widthSpec);
                w = this.mChild.getMeasuredHeight();
                h = this.mChild.getMeasuredWidth();
                break;
        }
        setMeasuredDimension(w, h);
        switch (this.mOrientation) {
            case 0:
                this.mChild.setTranslationX(0.0f);
                this.mChild.setTranslationY(0.0f);
                break;
            case 90:
                this.mChild.setTranslationX(0.0f);
                this.mChild.setTranslationY((float) h);
                break;
            case 180:
                this.mChild.setTranslationX((float) w);
                this.mChild.setTranslationY((float) h);
                break;
            case 270:
                this.mChild.setTranslationX((float) w);
                this.mChild.setTranslationY(0.0f);
                break;
        }
        this.mChild.setRotation((float) (-this.mOrientation));
    }

    public void setAngle(int orientation) {
        orientation %= 360;
        if (this.mOrientation == orientation) {
            requestLayout();
            return;
        }
        this.mOrientation = orientation;
        requestLayout();
    }

    public int getAngle() {
        return this.mOrientation;
    }

    public void rotateLayout(int degree) {
        setAngle(degree);
        invalidate();
    }

    public void unbind() {
        this.mChild = null;
    }
}
