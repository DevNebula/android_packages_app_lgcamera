package com.lge.camera.components;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;

public class ThumbnailTransitionDrawable extends LayerDrawable implements Callback {
    private static final int TRANSITION_NONE = 2;
    private static final int TRANSITION_RUNNING = 1;
    private static final int TRANSITION_STARTING = 0;
    private Rect mCalcBount = new Rect();
    private Drawable[] mChildDrawables;
    private int mDuration;
    private Rect mOriginBound = null;
    private int mStartOffset;
    private long mStartTimeMillis;
    private int mTransitionState = 2;
    private int mWidth;

    public ThumbnailTransitionDrawable(Drawable[] layers) {
        super(layers);
        this.mChildDrawables = layers;
    }

    public void startTransition(int durationMillis) {
        this.mDuration = durationMillis;
        this.mTransitionState = 0;
        setDefaultValue();
        invalidateSelf();
    }

    public void draw(Canvas canvas) {
        boolean done = true;
        if (isDrawable()) {
            if (this.mOriginBound == null) {
                setDefaultValue();
                if (this.mWidth == 0) {
                    return;
                }
            }
            if (this.mCalcBount != null) {
                this.mCalcBount.set(this.mOriginBound);
            }
            switch (this.mTransitionState) {
                case 0:
                    this.mStartTimeMillis = SystemClock.uptimeMillis();
                    done = false;
                    this.mTransitionState = 1;
                    this.mCalcBount = calcBounds(0.0f, this.mCalcBount);
                    break;
                case 1:
                    if (this.mStartTimeMillis >= 0) {
                        float normalized = ((float) (SystemClock.uptimeMillis() - this.mStartTimeMillis)) / ((float) this.mDuration);
                        if (normalized >= 1.0f) {
                            done = true;
                        } else {
                            done = false;
                        }
                        this.mCalcBount = calcBounds(Math.min(normalized, 1.0f), this.mCalcBount);
                        break;
                    }
                    break;
            }
            if (done) {
                this.mChildDrawables[1].setBounds(this.mOriginBound);
                this.mChildDrawables[1].draw(canvas);
                this.mTransitionState = 2;
                return;
            }
            this.mChildDrawables[0].draw(canvas);
            Drawable d = this.mChildDrawables[1];
            d.setBounds(this.mCalcBount);
            d.draw(canvas);
            if (!done) {
                invalidateSelf();
            }
        }
    }

    private void setDefaultValue() {
        this.mOriginBound = this.mChildDrawables[1].copyBounds();
        this.mWidth = this.mOriginBound.right - this.mOriginBound.left;
        this.mStartOffset = this.mWidth / 2;
        this.mOriginBound = this.mChildDrawables[1].copyBounds();
    }

    private Rect calcBounds(float normalized, Rect rect) {
        if (rect == null) {
            return null;
        }
        float crop = (((float) this.mWidth) - (((float) this.mStartOffset) + (((float) this.mStartOffset) * normalized))) / 2.0f;
        if (crop <= 0.0f) {
            return this.mOriginBound;
        }
        rect.left = (int) (((float) rect.left) + crop);
        rect.top = (int) (((float) rect.top) + crop);
        rect.right = (int) (((float) rect.right) - crop);
        rect.bottom = (int) (((float) rect.bottom) - crop);
        return rect;
    }

    private boolean isDrawable() {
        return (this.mChildDrawables.length < 2 || this.mChildDrawables[0] == null || this.mChildDrawables[1] == null) ? false : true;
    }

    public void unbind() {
        if (this.mChildDrawables != null) {
            int dSize = this.mChildDrawables.length;
            for (int i = 0; i < dSize; i++) {
                if (this.mChildDrawables[i] != null) {
                    this.mChildDrawables[i] = null;
                }
            }
        }
        this.mOriginBound = null;
        this.mCalcBount = null;
    }
}
