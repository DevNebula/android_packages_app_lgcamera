package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class CameraFocusTouchMoveView extends CameraFocusView {
    private Rect mRect = new Rect();

    public CameraFocusTouchMoveView(Context context) {
        super(context);
    }

    public CameraFocusTouchMoveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraFocusTouchMoveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFocusMovePoint(int x, int y, int width, int height) {
        this.mRect.set(x, y, x + width, y + height);
        invalidate();
    }

    public void unbind() {
        this.mRect = null;
    }

    protected void onDraw(Canvas canvas) {
        if (getVisibility() == 0) {
            Drawable drawable = getDrawable();
            if (drawable != null) {
                drawable.setBounds(this.mRect);
                drawable.draw(canvas);
            }
        }
    }
}
