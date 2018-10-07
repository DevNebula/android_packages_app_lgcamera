package com.lge.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class ArcProgress extends View {
    public static final int CYCLE_PERIOD_REC_3SEC = 3;
    public static final int CYCLE_PERIOD_REC_4SEC = 4;
    private static final int SNAP_FREQUENCY = 30;
    private int default_cycle_period;
    private int default_finished_color;
    private int default_inner_background_color;
    private float default_stroke_width;
    private int default_unfinished_color;
    private int default_update_frequency;
    private final RectF finishedOuterRect;
    private Paint finishedOutlinePaint;
    private float finishedOutlineStrokeWidth;
    private Paint finishedPaint;
    private int finishedStrokeColor;
    private float finishedStrokeWidth;
    private int innerBackgroundColor;
    private Paint innerCirclePaint;
    private boolean isAnimationPasued;
    private boolean isAnimationStop;
    public int mCyclePeriod;
    private Handler mHandler;
    private int mTotalArcLevel;
    public int mUpdateFrequency;
    private int max;
    private float outline_stroke_width;
    private int progress;
    private int sweepAngle;
    private final RectF unfinishedOuterRect;
    private Paint unfinishedPaint;
    private int unfinishedStrokeColor;
    private float unfinishedStrokeWidth;

    public ArcProgress(Context context) {
        this(context, null);
    }

    public ArcProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHandler = null;
        this.sweepAngle = 0;
        this.finishedOuterRect = new RectF();
        this.unfinishedOuterRect = new RectF();
        this.progress = 270;
        this.default_finished_color = Color.rgb(75, 219, 190);
        this.default_unfinished_color = 0;
        this.default_inner_background_color = Color.rgb(75, 219, 190);
        this.default_cycle_period = 1;
        this.default_update_frequency = 6;
        this.isAnimationStop = true;
        this.isAnimationPasued = false;
        this.mTotalArcLevel = 0;
        this.default_stroke_width = (float) Utils.getPx(context, C0088R.dimen.arc_progress_stroke_width);
        this.outline_stroke_width = (float) Utils.getPx(context, C0088R.dimen.arc_progress_outline_stroke_width);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, C0088R.styleable.ArcProgress, defStyleAttr, 0);
        initByAttributes(attributes);
        attributes.recycle();
        initPainters();
    }

    protected void initPainters() {
        this.finishedPaint = new Paint();
        this.finishedPaint.setColor(this.finishedStrokeColor);
        this.finishedPaint.setStyle(Style.STROKE);
        this.finishedPaint.setAntiAlias(true);
        this.finishedPaint.setStrokeCap(Cap.ROUND);
        this.finishedPaint.setStrokeWidth(this.finishedStrokeWidth);
        this.finishedOutlinePaint = new Paint();
        this.finishedOutlinePaint.setColor(Color.argb(128, 56, 56, 56));
        this.finishedOutlinePaint.setStyle(Style.STROKE);
        this.finishedOutlinePaint.setAntiAlias(true);
        this.finishedOutlinePaint.setStrokeCap(Cap.ROUND);
        this.finishedOutlinePaint.setStrokeWidth(this.finishedOutlineStrokeWidth);
        this.unfinishedPaint = new Paint();
        this.unfinishedPaint.setColor(this.unfinishedStrokeColor);
        this.unfinishedPaint.setStyle(Style.STROKE);
        this.unfinishedPaint.setAntiAlias(true);
        this.unfinishedPaint.setStrokeWidth(this.unfinishedStrokeWidth);
        this.innerCirclePaint = new Paint();
        this.innerCirclePaint.setColor(this.innerBackgroundColor);
        this.innerCirclePaint.setAntiAlias(true);
    }

    protected void initByAttributes(TypedArray attributes) {
        this.finishedStrokeColor = attributes.getColor(3, this.default_finished_color);
        this.unfinishedStrokeColor = attributes.getColor(2, this.default_unfinished_color);
        setProgress(attributes.getInt(0, 0));
        this.finishedStrokeWidth = attributes.getDimension(4, this.default_stroke_width);
        this.finishedOutlineStrokeWidth = attributes.getDimension(4, this.outline_stroke_width);
        this.unfinishedStrokeWidth = attributes.getDimension(5, this.default_stroke_width);
        this.innerBackgroundColor = attributes.getColor(6, this.default_inner_background_color);
        this.mCyclePeriod = attributes.getColor(7, this.default_cycle_period);
        this.mUpdateFrequency = attributes.getColor(8, this.default_update_frequency);
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.sweepAngle += progress;
        invalidate();
    }

    public void setProgressOnly(int progress) {
        this.sweepAngle += progress;
    }

    public void setInnerCircleColor(int color) {
        this.innerBackgroundColor = color;
    }

    public int getSweepAngle() {
        return this.sweepAngle;
    }

    public void setUpdateFrequency(int set) {
        this.mUpdateFrequency = set;
    }

    public int getUpdateFrequency() {
        return this.mUpdateFrequency;
    }

    public void startRotateAnimation() {
        if (this.isAnimationStop) {
            this.isAnimationStop = false;
            setProgress(0);
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessageDelayed(71, 0);
            }
        }
    }

    public void stopRotateAnimation() {
        this.isAnimationStop = true;
    }

    public void pauseRotateAnimation() {
        this.isAnimationPasued = true;
    }

    public void resumeRotateAnimation() {
        this.isAnimationStop = false;
        this.isAnimationPasued = false;
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(71, 0);
        }
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public int getPeriod() {
        return this.mCyclePeriod;
    }

    public void setPeriod(int set) {
        this.mCyclePeriod = set;
    }

    public boolean isRotateAnimationStop() {
        return this.isAnimationStop;
    }

    public boolean isRotateAnimationPasued() {
        return this.isAnimationPasued;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        if (max > 0) {
            this.max = max;
            invalidate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == 1073741824) {
            return size;
        }
        int result = (int) (this.default_stroke_width * 2.0f);
        if (mode == Integer.MIN_VALUE) {
            return Math.min(result, size);
        }
        return result;
    }

    protected void onDraw(Canvas canvas) {
        float delta = Math.max(this.finishedStrokeWidth, this.unfinishedStrokeWidth);
        this.finishedOuterRect.set(delta, delta, ((float) getWidth()) - delta, ((float) getHeight()) - delta);
        this.unfinishedOuterRect.set(delta, delta, ((float) getWidth()) - delta, ((float) getHeight()) - delta);
        float innerCircleRadius = ((((float) getWidth()) - Math.min(this.finishedStrokeWidth, this.unfinishedStrokeWidth)) + Math.abs(this.finishedStrokeWidth - this.unfinishedStrokeWidth)) / 2.0f;
        this.finishedPaint.setColor(this.default_finished_color);
        this.unfinishedPaint.setColor(this.default_unfinished_color);
        if (getSweepAngle() % (360 / this.mCyclePeriod) == 0) {
            if (this.innerCirclePaint.getColor() == this.innerBackgroundColor) {
                this.innerCirclePaint.setColor(0);
            } else {
                this.innerCirclePaint.setColor(this.innerBackgroundColor);
            }
        }
        canvas.drawCircle(((float) getWidth()) / 2.0f, ((float) getHeight()) / 2.0f, innerCircleRadius / 2.0f, this.innerCirclePaint);
        canvas.drawArc(this.finishedOuterRect, 270.0f, (float) getSweepAngle(), false, this.finishedOutlinePaint);
        canvas.drawArc(this.finishedOuterRect, 270.0f, (float) getSweepAngle(), false, this.finishedPaint);
        canvas.drawArc(this.unfinishedOuterRect, (float) (getSweepAngle() + 270), (float) (360 - getSweepAngle()), false, this.unfinishedPaint);
        super.onDraw(canvas);
    }

    public void updateArcProgress(int period) {
        final int endTime = ((int) SystemClock.currentThreadTimeMillis()) + (period * 1000);
        setPeriod(period);
        setUpdateFrequency(30);
        setInnerCircleColor(0);
        final int level = (360 / getPeriod()) / getUpdateFrequency();
        new Thread(new Runnable() {
            public void run() {
                int curTime = 0;
                ArcProgress.this.mTotalArcLevel = 0;
                while (endTime >= curTime) {
                    curTime = (int) SystemClock.currentThreadTimeMillis();
                    ArcProgress.this.setProgressOnly(level);
                    ArcProgress.this.postInvalidate();
                    if (!(ArcProgress.this.mTotalArcLevel = ArcProgress.this.mTotalArcLevel + level >= 360)) {
                        try {
                            Thread.sleep((long) (1000 / ArcProgress.this.getUpdateFrequency()));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return;
                    }
                }
            }
        }).start();
    }
}
