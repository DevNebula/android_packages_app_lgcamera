package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.lge.camera.C0088R;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.Utils;

public class FlashControlBar extends AeControlBar {
    public FlashControlBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FlashControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlashControlBar(Context context) {
        super(context);
    }

    public void init(ModuleInterface mGet, int min, int max, int defaultValue) {
        super.init(mGet, min, max, defaultValue);
        this.mBarHeight = Utils.getPx(getContext(), C0088R.dimen.flash_control_bar_height);
        this.mBarWidth = Utils.getPx(getContext(), C0088R.dimen.flash_control_bar_width);
        this.mBarHalfWidth = this.mBarWidth / 2;
        this.mBarHalfHeight = this.mBarHeight / 2;
        this.mDiff = (-this.mBarHalfWidth) / 3;
        this.mBarBitmap = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_selfie_flash_bar);
    }

    protected void drawBar(Canvas canvas) {
        if (this.mIsEnabled && this.mRotationInfo != null) {
            switch (this.mRotationInfo.getCurrentDegree()) {
                case 0:
                case 270:
                    this.mBarBitmap.setBounds(this.mLayoutHalfWidth - this.mBarHalfWidth, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mBarHalfWidth, this.mLayoutHalfHeight + this.mBarHalfHeight);
                    break;
                case 90:
                case 180:
                    this.mBarBitmap.setBounds(this.mLayoutHalfWidth - this.mBarHalfWidth, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mBarHalfWidth, this.mLayoutHalfHeight + this.mBarHalfHeight);
                    break;
            }
            this.mBarBitmap.draw(canvas);
        }
    }

    protected boolean doTouchActionDown(MotionEvent event) {
        this.mIsTouchMoving = true;
        this.mBarTouchState = true;
        this.mStartPosX = (int) event.getX();
        if (this.mStartPosX < this.mLayoutHalfWidth / 2) {
            this.mCurValue = 0;
        } else if (this.mStartPosX < this.mLayoutHalfWidth) {
            this.mCurValue = 1;
        } else if (this.mStartPosX < (this.mLayoutHalfWidth * 3) / 2) {
            this.mCurValue = 2;
        } else {
            this.mCurValue = 3;
        }
        if (this.mListener != null) {
            this.mListener.onBarDown();
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
        return true;
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        return true;
    }

    protected boolean doTouchActionUp() {
        if (this.mListener != null) {
            this.mListener.onBarUp();
        }
        return true;
    }

    protected void calculateCurValue() {
        this.mPreValue = this.mCurValue;
        int PrePos = ((this.mPreValue * this.mBarWidth) / 3) - this.mBarHalfWidth;
        if (this.mDiff - PrePos >= this.mBarWidth / 3) {
            this.mCurValue++;
        } else if (PrePos - this.mDiff >= this.mBarWidth / 3) {
            this.mCurValue--;
        }
        if (this.mCurValue < 0) {
            this.mCurValue = 0;
        } else if (this.mCurValue > 3) {
            this.mCurValue = 3;
        }
        if (this.mListener != null && this.mPreValue != this.mCurValue) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
    }

    protected void drawCursor(Canvas canvas) {
        this.mCursorRect.set((((this.mCurValue * this.mLayoutHalfWidth) / 2) + (this.mLayoutHalfWidth / 4)) - this.mCursorHalfWidth, this.mLayoutHalfHeight - this.mCursorHalfHeight, (((this.mCurValue * this.mLayoutHalfWidth) / 2) + (this.mLayoutHalfWidth / 4)) + this.mCursorHalfWidth, this.mLayoutHalfHeight + this.mCursorHalfHeight);
        this.mCursorNormal.setVisible(true, false);
        this.mCursorNormal.setBounds(this.mCursorRect);
        this.mCursorNormal.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mCursorNormal.draw(canvas);
    }

    public boolean doTouchEvent(MotionEvent event) {
        if (Float.compare(getAlpha(), 0.35f) == 0) {
            return false;
        }
        return super.doTouchEvent(event);
    }
}
