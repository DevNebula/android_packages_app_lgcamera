package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class AudioControlStepBar extends AudioControlBar {
    protected int mFactor = 0;
    protected OnAudioControlStepBarListener mStepBarListener = null;

    public interface OnAudioControlStepBarListener {
        void onBarTouchUp(String str);
    }

    public AudioControlStepBar(Context context) {
        super(context);
    }

    public AudioControlStepBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AudioControlStepBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(int factor, int step, int defaultValue, String unit) {
        setOnTouchListener(this);
        initResources();
        this.mMaxStep = step - 1;
        this.mFactor = factor;
        this.mItemGap = (this.mBarHeight - (this.mStepTop * 2)) / this.mMaxStep;
        this.mDefaultBarValue = defaultValue;
        this.mScrollPosition += (this.mMaxStep - (this.mDefaultBarValue / this.mFactor)) * this.mItemGap;
        this.mUnit = unit;
        this.mTextPaint = new Paint();
        this.mTextPaint.setTextSize((float) Utils.getPx(getContext(), C0088R.dimen.manual_audio_step_textsize));
        this.mTextPaint.setColor(getContext().getColor(C0088R.color.camera_white_txt));
        this.mTextPaint.setAlpha(178);
        this.mTextPaint.setTypeface(Typeface.SANS_SERIF);
        this.mTextPaint.setTextAlign(Align.RIGHT);
    }

    public void updateCursorPositon(int value) {
        this.mScrollPosition = (this.mBarY + this.mStepTop) + ((this.mMaxStep - (value / this.mFactor)) * this.mItemGap);
        this.mCurValue = value;
        if (this.mListener != null) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
    }

    public void setListener(OnAudioControlStepBarListener listener) {
        this.mStepBarListener = listener;
    }

    protected boolean doTouchActionUp() {
        this.mIsTouchMoving = false;
        int position = this.mScrollPosition - (this.mBarY + this.mStepTop);
        int quotient = position / this.mItemGap;
        if (position % this.mItemGap >= this.mItemGap / 2) {
            this.mScrollPosition = (quotient + 1) * this.mItemGap;
        } else {
            this.mScrollPosition = this.mItemGap * quotient;
        }
        this.mScrollPosition += this.mBarY + this.mStepTop;
        if (this.mScrollPosition < this.mBarY + this.mStepTop) {
            this.mScrollPosition = this.mBarY + this.mStepTop;
        }
        if (this.mScrollPosition > (this.mBarY + this.mBarHeight) - this.mStepTop) {
            this.mScrollPosition = (this.mBarY + this.mBarHeight) - this.mStepTop;
        }
        calculateCurValue();
        if (this.mStepBarListener != null) {
            this.mStepBarListener.onBarTouchUp(String.valueOf(this.mCurValue));
        }
        invalidate();
        return true;
    }

    protected void drawStep(Canvas canvas) {
        for (int i = 0; i < this.mMaxStep + 1; i++) {
            String str;
            if (i == this.mMaxStep) {
                str = "OFF";
            } else {
                str = "  " + (this.mFactor * (this.mMaxStep - i)) + this.mUnit;
            }
            canvas.drawText(str, (float) this.mTextStepX, (float) ((this.mStepY + (this.mItemGap * i)) + this.mTextMarginY), this.mTextPaint);
            this.mStepDrawable.setBounds(this.mStep1X, this.mStepY + (this.mItemGap * i), this.mStep1X + this.mStepDrawable.getIntrinsicWidth(), (this.mStepY + (this.mItemGap * i)) + this.mStepDrawable.getIntrinsicHeight());
            this.mStepDrawable.draw(canvas);
            this.mStepDrawable.setBounds(this.mStep2X, this.mStepY + (this.mItemGap * i), this.mStep2X + this.mStepDrawable.getIntrinsicWidth(), (this.mStepY + (this.mItemGap * i)) + this.mStepDrawable.getIntrinsicHeight());
            this.mStepDrawable.draw(canvas);
        }
    }

    protected void calculateCurValue() {
        int position = (this.mScrollPosition - this.mStepTop) - this.mBarY;
        int quotient = position / this.mItemGap;
        if (position % this.mItemGap >= this.mItemGap / 2) {
            this.mCurValue = ((this.mMaxStep - quotient) + 1) * this.mFactor;
        } else {
            this.mCurValue = (this.mMaxStep - quotient) * this.mFactor;
        }
        if (this.mFactor < 0) {
            this.mCurValue = Math.min(0, this.mCurValue);
            this.mCurValue = Math.max(this.mMaxStep * this.mFactor, this.mCurValue);
            return;
        }
        this.mCurValue = Math.min(this.mMaxStep * this.mFactor, this.mCurValue);
        this.mCurValue = Math.max(0, this.mCurValue);
    }

    protected void adjustScrollPosition() {
        if (this.mScrollPosition < this.mBarY) {
            this.mScrollPosition = this.mBarY;
        }
        if (this.mScrollPosition > this.mBarY + this.mBarHeight) {
            this.mScrollPosition = this.mBarY + this.mBarHeight;
        }
    }
}
