package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.lge.camera.util.Utils;

public class FrameGridView extends View {
    private static final int ANI_DRAW = 2;
    private static final int ANI_DURATION = 300;
    private static final int ANI_NONE = 0;
    private static final int ANI_READY = 1;
    private static final float GRID_ALPHA = 0.4f;
    private static final int NOT_DRAW = -1;
    private int mAnimationState = 0;
    private float mCurAlpha = GRID_ALPHA;
    private float mCur_x1 = 0.0f;
    private float mCur_x2 = 0.0f;
    private float mCur_y1 = 0.0f;
    private float mCur_y2 = 0.0f;
    private float mDst_x1 = 0.0f;
    private float mDst_x2 = 0.0f;
    private float mDst_y1 = 0.0f;
    private float mDst_y2 = 0.0f;
    private long mEndTime = 0;
    private AccelerateDecelerateInterpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private Paint mPaint = new Paint();
    private boolean mShowAnimation = true;
    private long mStartTime = 0;
    private float mStart_x1 = 0.0f;
    private float mStart_x2 = 0.0f;
    private float mStart_y1 = 0.0f;
    private float mStart_y2 = 0.0f;

    /* renamed from: com.lge.camera.components.FrameGridView$1 */
    class C05411 implements Runnable {
        C05411() {
        }

        public void run() {
            FrameGridView.this.animationTrigger();
        }
    }

    public FrameGridView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public FrameGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public FrameGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }

    public void startGridViewAnimation(boolean show) {
        if (show) {
            setVisibility(0);
        }
        this.mShowAnimation = show;
        this.mStartTime = 0;
        this.mEndTime = 0;
        this.mAnimationState = 1;
        this.mCurAlpha = GRID_ALPHA;
        invalidate();
        postDelayed(new C05411(), 100);
    }

    public void animationTrigger() {
        this.mStartTime = System.currentTimeMillis();
        this.mEndTime = this.mStartTime + 300;
        this.mAnimationState = 2;
        invalidate();
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        this.mAnimationState = -1;
    }

    public void clearAnimation() {
        super.clearAnimation();
        this.mAnimationState = 0;
        postInvalidate();
        this.mCurAlpha = GRID_ALPHA;
    }

    public void calcPosition() {
        long curTime = System.currentTimeMillis();
        if (curTime <= this.mEndTime) {
            float interpolate = this.mInterpolator.getInterpolation(Math.min(((float) (curTime - this.mStartTime)) / 300.0f, 1.0f));
            if (this.mShowAnimation) {
                this.mCur_x1 = Math.abs(this.mDst_x1 - this.mStart_x1) * interpolate;
                this.mCur_x2 = this.mStart_x2 - (Math.abs(this.mDst_x2 - this.mStart_x2) * interpolate);
                this.mCur_y1 = Math.abs(this.mDst_y1 - this.mStart_y1) * interpolate;
                this.mCur_y2 = this.mStart_y2 - (Math.abs(this.mDst_y2 - this.mStart_y2) * interpolate);
                this.mCurAlpha = GRID_ALPHA * interpolate;
                return;
            }
            this.mCur_x1 = this.mStart_x1 - (Math.abs(this.mDst_x1 - this.mStart_x1) * interpolate);
            this.mCur_x2 = this.mStart_x2 + (Math.abs(this.mDst_x2 - this.mStart_x2) * interpolate);
            this.mCur_y1 = this.mStart_y1 - (Math.abs(this.mDst_y1 - this.mStart_y1) * interpolate);
            this.mCur_y2 = this.mStart_y2 + (Math.abs(this.mDst_y2 - this.mStart_y2) * interpolate);
            this.mCurAlpha = (1.0f - interpolate) * GRID_ALPHA;
            return;
        }
        this.mCur_x1 = this.mDst_x1;
        this.mCur_x2 = this.mDst_x2;
        this.mCur_y1 = this.mDst_y1;
        this.mCur_y2 = this.mDst_y2;
        this.mCurAlpha = GRID_ALPHA;
        if (this.mShowAnimation) {
            this.mAnimationState = 0;
        } else {
            setVisibility(8);
        }
    }

    protected void onDraw(Canvas canvas) {
        if (getVisibility() == 0) {
            boolean show;
            this.mPaint.setColor(-3355444);
            this.mPaint.setStrokeWidth(Math.max(Utils.dpToPx(getContext(), 0.75f), 1.0f));
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            switch (this.mAnimationState) {
                case 0:
                    this.mCur_x1 = (float) (width / 3);
                    this.mCur_x2 = this.mCur_x1 * 2.0f;
                    this.mCur_y1 = (float) (height / 3);
                    this.mCur_y2 = this.mCur_y1 * 2.0f;
                    this.mPaint.setAlpha(Math.round(102.0f));
                    show = true;
                    break;
                case 1:
                    if (this.mShowAnimation) {
                        this.mStart_x1 = 0.0f;
                        this.mStart_x2 = (float) width;
                        this.mStart_y1 = 0.0f;
                        this.mStart_y2 = (float) height;
                        this.mDst_x1 = (float) (width / 3);
                        this.mDst_x2 = this.mDst_x1 * 2.0f;
                        this.mDst_y1 = (float) (height / 3);
                        this.mDst_y2 = this.mDst_y1 * 2.0f;
                        show = false;
                    } else {
                        this.mStart_x1 = (float) (width / 3);
                        this.mStart_x2 = this.mStart_x1 * 2.0f;
                        this.mStart_y1 = (float) (height / 3);
                        this.mStart_y2 = this.mStart_y1 * 2.0f;
                        this.mDst_x1 = 0.0f;
                        this.mDst_x2 = (float) width;
                        this.mDst_y1 = 0.0f;
                        this.mDst_y2 = (float) height;
                        this.mPaint.setAlpha(Math.round(102.0f));
                        show = true;
                    }
                    this.mCur_x1 = this.mStart_x1;
                    this.mCur_x2 = this.mStart_x2;
                    this.mCur_y1 = this.mStart_y1;
                    this.mCur_y2 = this.mStart_y2;
                    break;
                case 2:
                    calcPosition();
                    postInvalidate();
                    this.mPaint.setAlpha(Math.round(this.mCurAlpha * 255.0f));
                    show = true;
                    break;
                default:
                    show = false;
                    break;
            }
            if (show) {
                canvas.drawLine(this.mCur_x1, 0.0f, this.mCur_x1, (float) height, this.mPaint);
                canvas.drawLine(this.mCur_x2, 0.0f, this.mCur_x2, (float) height, this.mPaint);
                canvas.drawLine(0.0f, this.mCur_y1, (float) width, this.mCur_y1, this.mPaint);
                canvas.drawLine(0.0f, this.mCur_y2, (float) width, this.mCur_y2, this.mPaint);
            }
            super.onDraw(canvas);
        }
    }
}
