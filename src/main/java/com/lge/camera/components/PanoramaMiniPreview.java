package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PanoramaMiniPreview extends ImageView {
    public boolean sDrawing = false;

    public PanoramaMiniPreview(Context context) {
        super(context);
    }

    public PanoramaMiniPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PanoramaMiniPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isDrawing() {
        return this.sDrawing;
    }

    protected void onDraw(Canvas canvas) {
        this.sDrawing = true;
        super.onDraw(canvas);
        this.sDrawing = false;
    }
}
