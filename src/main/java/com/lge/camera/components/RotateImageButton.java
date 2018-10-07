package com.lge.camera.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class RotateImageButton extends RotateView {
    private static final float DIAGONAL_FACTOR = 1.41421f;
    private int mInitialTextPaddingBottom;
    private Drawable mRotateBgDrawable;
    private int mRotateBgResource;
    private int mTextBottomPaddingForMultiLines;

    public RotateImageButton(Context context) {
        this(context, null);
    }

    public RotateImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRotateBgResource = 0;
        this.mInitialTextPaddingBottom = 0;
        this.mTextBottomPaddingForMultiLines = 0;
        if (this.mIncludeFontPadding) {
            this.mBaseTextPaddingRate = 0.1f;
        }
        this.mTEXT_WIDTH_BUFFER = 0;
        this.mInitialTextPaddingBottom = this.mTextPaddingBottom;
        this.mTextBottomPaddingForMultiLines = (this.mTextPaddingBottom - this.mTextSize) - this.mLineSpacingExtra;
    }

    public void setTextPaddingBottom(int paddingBottom) {
        this.mInitialTextPaddingBottom = paddingBottom;
        this.mTextBottomPaddingForMultiLines = (paddingBottom - this.mTextSize) - this.mLineSpacingExtra;
        super.setTextPaddingBottom(paddingBottom);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth;
        int measuredHeight;
        int contentWidth = 0;
        int contentHeight = 0;
        if (this.mRotateBgDrawable != null) {
            int bgWidth = this.mRotateBgDrawable.getMinimumWidth();
            int bgHeight = this.mRotateBgDrawable.getMinimumHeight();
            if (0 < bgWidth) {
                contentWidth = bgWidth;
            }
            if (0 < bgHeight) {
                contentHeight = bgHeight;
            }
        }
        Drawable imageDrawable = getDrawable();
        if (imageDrawable != null) {
            int imageWidth = imageDrawable.getMinimumWidth();
            int imageHeight = imageDrawable.getMinimumHeight();
            if (contentWidth < imageWidth) {
                contentWidth = imageWidth;
            }
            if (contentHeight < imageHeight) {
                contentHeight = imageHeight;
            }
        }
        if (MeasureSpec.getMode(widthMeasureSpec) == 1073741824) {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            measuredWidth = contentWidth;
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == 1073741824) {
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            measuredHeight = contentHeight;
        }
        if (!(this.mText == null || this.mTextLines == null)) {
            if (this.mTextPaint.measureText(this.mText) <= ((float) measuredWidth) || this.mSingleLine) {
                this.mTextPaddingBottom = this.mInitialTextPaddingBottom;
            } else {
                this.mLandscapeTextLines = wordWrap(measuredWidth, this.mText);
                setTextLines(this.mLandscapeTextLines);
                if (this.mTextGravity == 4) {
                    this.mTextPaddingBottom = this.mTextBottomPaddingForMultiLines;
                }
            }
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    protected void canvasRotate(Canvas canvas, int viewWidth, int viewHeight) {
        float bgCenterX = ((float) viewWidth) / 2.0f;
        float bgCenterY = ((float) viewHeight) / 2.0f;
        canvas.rotate((float) (-this.mRotationInfo.getCurrentDegree()), bgCenterX, bgCenterY);
        if (this.mRotateInsideView) {
            applyRotateImageScale(canvas, viewWidth, viewHeight, bgCenterX, bgCenterY);
        }
        if (!this.mRotateIconOnly) {
            getBackground().draw(canvas);
        }
    }

    protected boolean checkBackground(Canvas canvas) {
        Drawable drawable = getBackground();
        if (drawable == null) {
            drawable = getDrawable();
            if (drawable == null) {
                return false;
            }
        }
        if ((drawable instanceof BitmapDrawable) && ((BitmapDrawable) drawable).getBitmap().isRecycled()) {
            return false;
        }
        Rect bounds = drawable.getBounds();
        int drawableHeight = bounds.bottom - bounds.top;
        if (bounds.right - bounds.left == 0 || drawableHeight == 0) {
            CamLog.m3d(CameraConstants.TAG, "drawable width,height is zero, return");
            return false;
        }
        if (this.mRotateIconOnly) {
            drawable.draw(canvas);
        }
        return true;
    }

    public int getTextPaintWidth() {
        Paint p = new Paint();
        p.setTextSize((float) this.mTextSize);
        int textWidth = (int) p.measureText(this.mText);
        return ((int) (((float) textWidth) * this.mBaseTextPaddingRate)) + textWidth;
    }

    public int getTextWidth() {
        Paint p = new Paint();
        p.setTextSize((float) this.mTextSize);
        return (int) p.measureText(this.mText);
    }

    public void setRotated(int degree) {
        if (degree > 0) {
            this.mRotationInfo.setCurrentDegree(degree - 1);
        } else {
            this.mRotationInfo.setCurrentDegree(1);
        }
        this.mRotationInfo.setTargetDegree(degree);
        invalidate();
    }

    public void setBackgroundResource(int resId) {
        if (this.mRotateBgResource != resId) {
            updateDrawable(null);
            this.mRotateBgResource = resId;
            Drawable d = null;
            if (this.mRotateBgResource != 0) {
                try {
                    Resources rsrc = getResources();
                    if (rsrc != null) {
                        d = rsrc.getDrawable(this.mRotateBgResource);
                    }
                } catch (Exception e) {
                    CamLog.m12w(CameraConstants.TAG, "Unable to find resource: " + this.mRotateBgResource, e);
                }
            }
            updateDrawable(d);
            requestLayout();
            invalidate();
        }
    }

    public int getBackgroundResource() {
        return this.mRotateBgResource;
    }

    public void setBackgroundDrawable(Drawable drawable) {
        if (this.mRotateBgDrawable != drawable) {
            updateDrawable(drawable);
            requestLayout();
            invalidate();
        }
    }

    public Drawable getBackground() {
        return this.mRotateBgDrawable;
    }

    private void updateDrawable(Drawable d) {
        if (this.mRotateBgDrawable != null) {
            this.mRotateBgDrawable.setCallback(null);
            unscheduleDrawable(this.mRotateBgDrawable);
        }
        this.mRotateBgDrawable = d;
        if (d != null) {
            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            configureBounds();
        }
    }

    private void configureBounds() {
        if (this.mRotateBgDrawable != null) {
            this.mRotateBgDrawable.setBounds(0, 0, getWidth(), getHeight());
        }
        int longerSide = getWidth();
        if (getWidth() < getHeight()) {
            longerSide = getHeight();
        }
        this.mExpand4Rotate = (int) ((((float) longerSide) * DIAGONAL_FACTOR) - ((float) longerSide));
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable d = this.mRotateBgDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    protected boolean verifyDrawable(Drawable dr) {
        return this.mRotateBgDrawable == dr || super.verifyDrawable(dr);
    }

    public void invalidateDrawable(Drawable dr) {
        if (dr == this.mRotateBgDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        configureBounds();
        return changed;
    }

    public void setRotateIconOnly(boolean rotateIconOnly) {
        if (this.mRotateIconOnly != rotateIconOnly) {
            this.mRotateIconOnly = rotateIconOnly;
            invalidate();
        }
    }

    private void applyRotateImageScale(Canvas canvas, int viewWidth, int viewHeight, float bgCenterX, float bgCenterY) {
        Drawable imageSrc = getDrawable();
        if (imageSrc != null) {
            float rotatedImageScale;
            Rect imageBounds = imageSrc.getBounds();
            int imageWidth = imageBounds.right - imageBounds.left;
            int imageHeight = imageBounds.bottom - imageBounds.top;
            float viewRatio = (float) (viewWidth / viewHeight);
            if (viewRatio < ((float) imageWidth) / ((float) imageHeight)) {
                imageHeight = (int) (((float) imageHeight) * (((float) viewWidth) / ((float) imageWidth)));
                imageWidth = viewWidth;
            } else {
                imageWidth = (int) (((float) imageWidth) * ((float) (viewHeight / imageHeight)));
                imageHeight = viewHeight;
            }
            double cosA = Math.cos(Math.toRadians((double) this.mRotationInfo.getCurrentDegree()));
            double cosRevA = Math.cos(Math.toRadians((double) (90 - this.mRotationInfo.getCurrentDegree())));
            int rw = (int) (Math.abs(((double) imageWidth) * cosA) + Math.abs(((double) imageHeight) * cosRevA));
            int rh = (int) (Math.abs(((double) imageWidth) * cosRevA) + Math.abs(((double) imageHeight) * cosA));
            float rotatedImageScaleW = ((float) viewWidth) / ((float) rw);
            float rotatedImageScaleH = (float) (viewHeight / rh);
            if (viewRatio < ((float) rw) / ((float) rh)) {
                rotatedImageScale = rotatedImageScaleW;
            } else {
                rotatedImageScale = rotatedImageScaleH;
            }
            canvas.scale(rotatedImageScale, rotatedImageScale, bgCenterX, bgCenterY);
        }
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setTextColor(this.mSelectedTextColor == -1 ? this.mPressedTextColor : this.mSelectedTextColor);
        } else {
            setTextColor(this.mNormalTextColor);
        }
        super.setSelected(selected);
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (this.mPressedTextColor != -1) {
            if (pressed) {
                setTextColor(this.mPressedTextColor);
            } else if (isSelected()) {
                setTextColor(this.mSelectedTextColor == -1 ? this.mPressedTextColor : this.mSelectedTextColor);
            } else {
                setTextColor(this.mNormalTextColor);
            }
        }
    }

    public void initButtonText(String message) {
        if (this.mTextAllCaps && message != null) {
            message = message.toUpperCase();
        }
        setText(message);
        setTextScaleX(1.0f);
        Paint tp = new Paint();
        tp.setTextSize((float) this.mTextSize);
        float quickButtonTargetWidth = (float) this.mTextWidth;
        float mearsureText = tp.measureText(message);
        float scaleFactor = 0.0f;
        if (Float.compare(mearsureText, 0.0f) != 0 && Float.compare(quickButtonTargetWidth, 0.0f) != 0 && Float.compare(mearsureText, quickButtonTargetWidth) != 0) {
            if (Float.compare(mearsureText, quickButtonTargetWidth) >= 0) {
                scaleFactor = quickButtonTargetWidth / mearsureText;
            }
            if (Float.compare(scaleFactor, 0.0f) != 0) {
                setTextScaleX(scaleFactor);
            }
        }
    }

    public void setTextColorFilter(ColorFilter cf) {
        getTextPaint().setColorFilter(cf);
    }

    public void setSingleLine(boolean set) {
        this.mSingleLine = set;
    }

    public void setTextWidth(int width) {
        this.mTextWidth = width;
    }
}
