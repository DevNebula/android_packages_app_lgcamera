package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class AudioOffStepBar extends AudioControlStepBar {
    private String mDefaultBarValue = "off";

    public AudioOffStepBar(Context context) {
        super(context);
    }

    public AudioOffStepBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AudioOffStepBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(int factor, int step, String defaultValue, String unit) {
        setOnTouchListener(this);
        initResources();
        this.mMaxStep = step - 1;
        this.mFactor = factor;
        this.mItemGap = (this.mBarHeight - (this.mStepTop * 2)) / this.mMaxStep;
        if (this.mItemGap < 1) {
            this.mItemGap = 1;
        }
        this.mDefaultBarValue = defaultValue;
        if ("off".equals(this.mDefaultBarValue)) {
            this.mScrollPosition = (this.mBarY + this.mBarHeight) - this.mStepTop;
        } else {
            this.mScrollPosition = (this.mBarY + this.mStepTop) + (((this.mMaxStep - (Integer.valueOf(this.mDefaultBarValue).intValue() / this.mFactor)) - 1) * this.mItemGap);
        }
        this.mUnit = unit;
        this.mTextPaint = new Paint();
        this.mTextPaint.setTextSize((float) Utils.getPx(getContext(), C0088R.dimen.manual_audio_step_textsize));
        this.mTextPaint.setColor(getContext().getColor(C0088R.color.camera_white_txt));
        this.mTextPaint.setAlpha(178);
        this.mTextPaint.setTypeface(Typeface.SANS_SERIF);
        this.mTextPaint.setTextAlign(Align.RIGHT);
    }

    public void init(int factor, int step, String defaultValue, String title, String unit) {
        init(factor, step, defaultValue, unit);
        this.mTitle = title;
        this.mTitlePaint = new Paint();
        this.mTitlePaint.setTextSize((float) Utils.getPx(getContext(), C0088R.dimen.manual_audio_bar_name_textsize));
        this.mTitlePaint.setColor(getContext().getColor(C0088R.color.camera_white_txt));
        this.mTitlePaint.setTypeface(Typeface.SANS_SERIF);
        this.mTitlePaint.setTextAlign(Align.CENTER);
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
            this.mStepBarListener.onBarTouchUp(this.mCurValue == 0 ? "off" : String.valueOf(this.mCurValue - this.mFactor));
        }
        invalidate();
        return true;
    }

    public void updateCursorPositon(String value) {
        if ("off".equals(value)) {
            this.mScrollPosition = (this.mBarY + this.mBarHeight) - this.mStepTop;
            this.mCurValue = 0;
        } else {
            this.mCurValue = Integer.valueOf(value).intValue();
            this.mScrollPosition = (this.mBarY + this.mStepTop) + (((this.mMaxStep - (this.mCurValue / this.mFactor)) - 1) * this.mItemGap);
        }
        if (this.mListener != null) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
    }

    protected void drawStep(Canvas canvas) {
        for (int i = 0; i < this.mMaxStep + 1; i++) {
            String str;
            if (i == this.mMaxStep) {
                str = "OFF";
            } else {
                str = (this.mFactor * ((this.mMaxStep - i) - 1)) + this.mUnit;
            }
            canvas.drawText(str, (float) this.mTextStepX, (float) ((this.mStepY + (this.mItemGap * i)) + this.mTextMarginY), this.mTextPaint);
            this.mStepDrawable.setBounds(this.mStep1X, this.mStepY + (this.mItemGap * i), this.mStep1X + this.mStepDrawable.getIntrinsicWidth(), (this.mStepY + (this.mItemGap * i)) + this.mStepDrawable.getIntrinsicHeight());
            this.mStepDrawable.draw(canvas);
            this.mStepDrawable.setBounds(this.mStep2X, this.mStepY + (this.mItemGap * i), this.mStep2X + this.mStepDrawable.getIntrinsicWidth(), (this.mStepY + (this.mItemGap * i)) + this.mStepDrawable.getIntrinsicHeight());
            this.mStepDrawable.draw(canvas);
        }
    }
}
