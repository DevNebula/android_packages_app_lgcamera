package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.lge.camera.C0088R;
import com.lge.camera.util.RatioCalcUtil;

public class HorizontalSeekBar extends View implements OnTouchListener {
    private final float BAR_HEIGHT_RATIO = 0.014f;
    private final float BAR_WIDTH_RATIO = 0.45f;
    private final int SEEK_BAR_AVAILABLE_STEP = 6;
    private final float TOP_MARGIN = 0.022f;
    protected NinePatchDrawable mBarBitmap;
    protected int mBarHeight = 0;
    protected int mBarWidth = 0;
    protected int mCenterPositionY = 0;
    protected int mCurValue = 0;
    protected int mCursorHalfHeight = 0;
    protected int mCursorHalfWidth = 0;
    protected Drawable mCursorNormal;
    protected Rect mCursorRect = new Rect();
    protected int mDefaultValue = 0;
    protected boolean mIsAvailableMoving = false;
    protected boolean mIsEnabled = true;
    protected boolean mIsTouchMoving = false;
    protected int mItemGap = 0;
    public OnVerticalSeekBarListener mListener = null;
    protected int mMaxStep = 100;
    private int mMaxValue = 0;
    private int mMinValue = 0;
    protected int mPreValue = 0;
    protected Rect mProgressBar = new Rect();
    protected Paint mProgressBarPaint = new Paint();
    protected int mScrollPositon = 0;
    protected int mStartPosX = 0;
    protected int mTopMargin = 0;

    public interface OnVerticalSeekBarListener {
        void onBarTouchDown();

        void onBarTouchUp();

        void onBarValueChanged(int i);
    }

    public HorizontalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDrawables();
    }

    public HorizontalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawables();
    }

    public HorizontalSeekBar(Context context) {
        super(context);
    }

    public void init(int min, int max, int defaultValue) {
        setOnTouchListener(this);
        initDrawables();
        this.mMinValue = min;
        this.mMaxValue = max;
        this.mItemGap = this.mBarWidth / this.mMaxValue;
        this.mDefaultValue = defaultValue;
        this.mScrollPositon = (this.mMaxValue - this.mDefaultValue) * this.mItemGap;
    }

    protected void initDrawables() {
        this.mCursorNormal = getContext().getDrawable(C0088R.drawable.ic_camera_jog_zoom_normal);
        this.mCursorHalfWidth = this.mCursorNormal.getIntrinsicWidth() / 2;
        this.mCursorHalfHeight = this.mCursorNormal.getIntrinsicHeight() / 2;
        this.mBarBitmap = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_camera_jog_zoom);
        this.mBarWidth = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.45f);
        this.mBarHeight = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.014f);
        this.mCenterPositionY = this.mBarHeight / 2;
        this.mTopMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.022f);
    }

    public void setOnVerticalSeekBarListener(OnVerticalSeekBarListener listener) {
        this.mListener = listener;
    }

    protected void onDraw(Canvas canvas) {
        drawBar(canvas);
        setSelected(this.mIsTouchMoving);
        drawCursor(canvas);
        calculateCurValue();
    }

    protected void drawBar(Canvas canvas) {
        this.mBarBitmap.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mBarBitmap.setBounds(0, this.mTopMargin, this.mBarWidth, this.mBarHeight + this.mTopMargin);
        this.mBarBitmap.draw(canvas);
    }

    protected void drawCursor(Canvas canvas) {
        this.mCursorRect.set(this.mScrollPositon - this.mCursorHalfWidth, (this.mCenterPositionY - this.mCursorHalfHeight) + this.mTopMargin, this.mScrollPositon + this.mCursorHalfWidth, (this.mCenterPositionY + this.mCursorHalfHeight) + this.mTopMargin);
        this.mCursorNormal.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mCursorNormal.setBounds(this.mCursorRect);
        this.mCursorNormal.draw(canvas);
    }

    protected void calculateCurValue() {
        int scroll = this.mScrollPositon;
        if (this.mScrollPositon <= this.mCursorHalfWidth) {
            scroll = this.mScrollPositon - this.mCursorHalfWidth;
            if (scroll < 0) {
                scroll = 0;
            }
        } else if (this.mScrollPositon >= this.mBarWidth - this.mCursorHalfWidth) {
            scroll = this.mScrollPositon + this.mCursorHalfWidth;
            if (scroll > this.mBarWidth) {
                scroll = this.mBarWidth;
            }
        }
        this.mCurValue = (this.mBarWidth - scroll) / this.mItemGap;
        if (this.mIsTouchMoving && this.mCurValue != this.mDefaultValue) {
            this.mPreValue = this.mCurValue;
            this.mListener.onBarValueChanged(this.mCurValue);
        }
    }

    protected boolean doTouchEvent(MotionEvent event) {
        switch (event.getActionMasked() & 255) {
            case 0:
                return doTouchActionDown(event);
            case 1:
            case 3:
                return doTouchActionUp();
            case 2:
                return doTouchActionMove(event);
            case 6:
                return doTouchActionPointerUp(event);
            default:
                return true;
        }
    }

    protected boolean doTouchActionPointerUp(MotionEvent event) {
        return false;
    }

    protected boolean doTouchActionUp() {
        this.mScrollPositon = (this.mMaxValue - this.mDefaultValue) * this.mItemGap;
        this.mIsTouchMoving = false;
        this.mIsAvailableMoving = false;
        invalidate();
        if (this.mListener != null) {
            this.mListener.onBarTouchUp();
        }
        return true;
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        int diff = ((int) event.getX()) - this.mStartPosX;
        this.mIsTouchMoving = true;
        if (this.mIsAvailableMoving || Math.abs(diff) >= 6) {
            this.mIsAvailableMoving = true;
            this.mScrollPositon = ((this.mMaxValue - this.mDefaultValue) * this.mItemGap) + diff;
            if (this.mScrollPositon <= this.mCursorHalfWidth) {
                this.mScrollPositon = this.mCursorHalfWidth;
            } else if (this.mScrollPositon >= this.mBarWidth - this.mCursorHalfWidth) {
                this.mScrollPositon = this.mBarWidth - this.mCursorHalfWidth;
            }
            invalidate();
            return true;
        }
        invalidate();
        return false;
    }

    protected boolean doTouchActionDown(MotionEvent event) {
        this.mStartPosX = (int) event.getX();
        this.mScrollPositon = (this.mMaxValue - this.mDefaultValue) * this.mItemGap;
        if (this.mListener != null) {
            this.mListener.onBarTouchDown();
        }
        return true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (doTouchEvent(event)) {
            return true;
        }
        return false;
    }

    public void unbind() {
        this.mBarBitmap = null;
        this.mCursorNormal = null;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    public void updateCursorPositon(int value) {
        if (value > this.mMaxValue) {
            value = this.mMaxValue;
        } else if (value < this.mMinValue) {
            value = this.mMinValue;
        }
        this.mScrollPositon = (this.mMaxValue - value) * this.mItemGap;
        this.mCurValue = value;
        if (this.mIsTouchMoving && this.mListener != null) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
    }

    public void refreshControlBar() {
        if (this.mIsTouchMoving && this.mListener != null) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
    }

    public int getBarHeight() {
        return (this.mTopMargin + this.mBarHeight) + this.mTopMargin;
    }
}
