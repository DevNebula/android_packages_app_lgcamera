package com.lge.camera.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;
import com.lge.camera.C0088R;

public class StrokeTextView extends TextView {
    private int mStrokeColor;
    private float mStrokeWidth;
    private boolean mUseStroke;

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, C0088R.styleable.stroke);
        this.mUseStroke = typedArray.getBoolean(0, false);
        this.mStrokeColor = typedArray.getColor(1, 0);
        this.mStrokeWidth = typedArray.getDimension(2, 0.0f);
        typedArray.recycle();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mUseStroke) {
            ColorStateList states = getTextColors();
            getPaint().setStyle(Style.STROKE);
            getPaint().setStrokeWidth(this.mStrokeWidth);
            setTextColor(this.mStrokeColor);
            super.onDraw(canvas);
            getPaint().setStyle(Style.FILL);
            setTextColor(states);
        }
        super.onDraw(canvas);
    }
}
