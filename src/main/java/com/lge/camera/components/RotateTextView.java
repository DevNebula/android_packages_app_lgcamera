package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;

public class RotateTextView extends RotateView {
    private boolean mPortrait;

    public RotateTextView(Context context) {
        this(context, null);
    }

    public RotateTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPortrait = false;
        if (this.mIncludeFontPadding) {
            this.mBaseTextPaddingRate = 0.2f;
        }
        this.mTEXT_WIDTH_BUFFER = 20;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setTextPaint();
        if (widthMode == 1073741824) {
            width = widthSize;
            if (!(this.mPortrait || this.mSingleLine)) {
                this.mLandscapeTextLines = wordWrap(widthSize, mergeStrings(this.mTextLines));
                setTextLines(this.mLandscapeTextLines);
            }
        } else {
            width = this.mPortrait ? getDesiredHeight() : getDesiredWidth();
            if (widthMode == Integer.MIN_VALUE) {
                if (!(this.mPortrait || width <= widthSize || this.mSingleLine)) {
                    this.mLandscapeTextLines = wordWrap(widthSize, mergeStrings(this.mTextLines));
                    setTextLines(this.mLandscapeTextLines);
                    width = getDesiredWidth();
                }
                width = Math.min(widthSize, width);
            }
        }
        if (heightMode == 1073741824) {
            height = heightSize;
        } else {
            height = this.mPortrait ? getDesiredWidth() : getDesiredHeight();
            if (heightMode == Integer.MIN_VALUE) {
                if (this.mPortrait && height > heightSize && !this.mSingleLine) {
                    this.mPortraitTextLines = wordWrap(heightSize, mergeStrings(this.mTextLines));
                    setTextLines(this.mPortraitTextLines);
                    height = getDesiredWidth();
                }
                height = Math.min(height, heightSize);
            }
        }
        setMeasuredDimension(width, height);
    }

    protected void canvasRotate(Canvas canvas, int viewWidth, int viewHeight) {
        float pivotX = (float) this.mRotatePivotLeft;
        float pivotY = (float) this.mRotatePivotTop;
        if (this.mRotatePivotLeft == Integer.MAX_VALUE) {
            pivotX = ((float) viewWidth) / 2.0f;
        }
        if (this.mRotatePivotTop == Integer.MAX_VALUE) {
            pivotY = ((float) viewHeight) / 2.0f;
        }
        canvas.rotate((float) (-this.mRotationInfo.getCurrentDegree()), pivotX, pivotY);
    }

    protected boolean checkBackground(Canvas canvas) {
        return true;
    }

    public int getTextPaintWidth() {
        Paint p = new Paint();
        p.setTextSize((float) this.mTextSize);
        return (int) p.measureText(this.mText);
    }

    public void setRotated(int degree) {
        boolean portrait;
        if (degree == 90 || degree == 270) {
            setTextLines(this.mPortraitTextLines);
            portrait = true;
        } else {
            setTextLines(this.mLandscapeTextLines);
            portrait = false;
        }
        if (this.mPortrait != portrait) {
            this.mPortrait = portrait;
            invalidate();
        }
    }

    private int getDesiredWidth() {
        float width = 0.0f;
        if (this.mTextLines != null) {
            for (String measureText : this.mTextLines) {
                float textWidth = this.mTextPaint.measureText(measureText);
                width = Math.max((textWidth + (0.09f * textWidth)) + (((((float) this.mTextPaddingLeft) + this.mTextBasePadding) + ((float) this.mTextPaddingRight)) + this.mTextBasePadding), width);
            }
        }
        return (int) width;
    }

    private int getDesiredHeight() {
        float textHeight = this.mTextPaint.getFontSpacing() + ((float) this.mLineSpacingExtra);
        float totalTextLineHeight = 0.0f;
        if (this.mTextLines != null) {
            totalTextLineHeight = (textHeight * ((float) this.mTextLines.length)) + (((((float) this.mTextPaddingTop) + this.mTextBasePadding) + ((float) this.mTextPaddingBottom)) + this.mTextBasePadding);
        }
        return (int) totalTextLineHeight;
    }

    private String mergeStrings(String[] strings) {
        if (this.mTextLines == null) {
            return null;
        }
        StringBuffer mergedText = new StringBuffer();
        for (int i = 0; i < this.mTextLines.length; i++) {
            if (i != 0) {
                mergedText.append(' ');
            }
            mergedText.append(this.mTextLines[i]);
        }
        return mergedText.toString();
    }

    public void setTextOnOneLine() {
        float mearsureText = (float) getDesiredWidth();
        float textViewSize = (float) this.mTextWidth;
        float scaleFactor = 0.0f;
        if (Float.compare(mearsureText, 0.0f) != 0 && Float.compare(textViewSize, 0.0f) != 0 && Float.compare(mearsureText, textViewSize) != 0) {
            if (Float.compare(mearsureText, textViewSize) >= 0) {
                scaleFactor = textViewSize / mearsureText;
            }
            if (Float.compare(scaleFactor, 0.0f) != 0) {
                setTextScaleX(scaleFactor);
            }
        }
    }

    public void setColorFilter(ColorFilter cf) {
        super.setColorFilter(cf);
        this.mTextPaint.setColorFilter(cf);
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

    public void setSelected(boolean selected) {
        if (selected) {
            setTextColor(this.mSelectedTextColor == -1 ? this.mPressedTextColor : this.mSelectedTextColor);
        } else {
            setTextColor(this.mNormalTextColor);
        }
        super.setSelected(selected);
    }
}
