package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ArcEffectView extends View {
    private Paint mPaint = new Paint();
    private RectF mRect = new RectF();
    private int mStartAngle = 0;
    private int mSweepAngle = 0;

    public ArcEffectView(Context context) {
        super(context);
    }

    public ArcEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArcEffectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initArcEffect(int color, float radius, int ratio, int startAngle, int sweepAngle) {
        this.mPaint.setColor(color);
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setAntiAlias(true);
        this.mStartAngle = startAngle;
        this.mSweepAngle = sweepAngle;
        this.mRect.set(0.0f, 0.0f, ((float) ratio) * radius, ((float) ratio) * radius);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(this.mRect, (float) this.mStartAngle, (float) this.mSweepAngle, false, this.mPaint);
    }
}
