package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;

public class DetailRotateLayout extends RotateLayout {
    private Path path = new Path();
    private RectF rect = new RectF();

    public DetailRotateLayout(Context context) {
        super(context);
    }

    public DetailRotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.path.reset();
        this.rect.set(0.0f, 0.0f, (float) w, (float) h);
        this.path.addRoundRect(this.rect, 40.0f, 40.0f, Direction.CW);
        this.path.close();
    }

    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(this.path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}
