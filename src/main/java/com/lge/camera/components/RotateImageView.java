package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.lge.camera.util.ColorUtil;

public class RotateImageView extends ImageView {
    private RotationInfo mRotationInfo = new RotationInfo();

    public RotateImageView(Context context) {
        super(context);
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDegree(int degree) {
        setDegree(degree, true);
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mRotationInfo != null) {
            this.mRotationInfo.setDegree(degree, animation);
        }
        invalidate(-100, -100, 300, 300);
    }

    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            if (drawable instanceof TransitionDrawable) {
                TransitionDrawable transDrawable = (TransitionDrawable) drawable;
                Bitmap bmpDraw0 = ((BitmapDrawable) transDrawable.getDrawable(0)).getBitmap();
                Bitmap bmpDraw1 = ((BitmapDrawable) transDrawable.getDrawable(1)).getBitmap();
                if (bmpDraw0.isRecycled() || bmpDraw1.isRecycled()) {
                    return;
                }
            } else if (drawable instanceof BitmapDrawable) {
                Bitmap bmpDraw = ((BitmapDrawable) drawable).getBitmap();
                if (bmpDraw != null && bmpDraw.isRecycled()) {
                    return;
                }
            }
            Rect bounds = drawable.getBounds();
            if (bounds.right - bounds.left != 0 && bounds.bottom - bounds.top != 0 && this.mRotationInfo != null) {
                float boundWidth = (float) (bounds.right - bounds.left);
                float boundHeight = (float) (bounds.bottom - bounds.top);
                if (this.mRotationInfo.getCurrentDegree() != this.mRotationInfo.getTargetDegree() && this.mRotationInfo.calcCurrentDegree()) {
                    invalidate(-100, -100, 300, 300);
                }
                int left = getPaddingStart();
                int top = getPaddingTop();
                int right = getPaddingEnd();
                float width = (float) ((getWidth() - left) - right);
                float height = (float) ((getHeight() - top) - getPaddingBottom());
                int saveCount = canvas.getSaveCount();
                canvas.translate(((float) left) + (width / 2.0f), ((float) top) + (height / 2.0f));
                canvas.rotate((float) (-this.mRotationInfo.getCurrentDegree()));
                canvas.translate((-boundWidth) / 2.0f, (-boundHeight) / 2.0f);
                drawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }

    public void setContentsColor(int alpha, int r, int g, int b) {
        setColorFilter(ColorUtil.getColorMatrix((float) alpha, (float) r, (float) g, (float) b));
    }
}
