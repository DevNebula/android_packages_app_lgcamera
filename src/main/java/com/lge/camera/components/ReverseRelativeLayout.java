package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ReverseRelativeLayout extends RelativeLayout {
    private boolean isReverse = false;

    public void setReverse(boolean isReverse) {
        this.isReverse = isReverse;
    }

    public ReverseRelativeLayout(Context context) {
        super(context);
    }

    public ReverseRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReverseRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void dispatchDraw(Canvas arg0) {
        Matrix matrix = arg0.getMatrix();
        if (this.isReverse) {
            matrix.setScale(-1.0f, 1.0f, (float) (arg0.getWidth() / 2), (float) (arg0.getHeight() / 2));
        }
        arg0.setMatrix(matrix);
        super.dispatchDraw(arg0);
    }

    public boolean isReverse() {
        return this.isReverse;
    }
}
