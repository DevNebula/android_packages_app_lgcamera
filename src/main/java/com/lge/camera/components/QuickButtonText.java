package com.lge.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import com.lge.camera.C0088R;
import java.util.Locale;

public class QuickButtonText extends QuickButton {
    protected int mStyleId;
    protected int mTextWidth;

    public QuickButtonText(Context context) {
        this(context, null);
    }

    public QuickButtonText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickButtonText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mStyleId = -1;
        this.mTextWidth = 0;
        this.mStyleId = defStyle;
    }

    public void init(Context context, QuickButtonType type) {
        String message = "";
        if (type != null && type.mStringIds != null) {
            initButtonAttributes(context);
            super.init(context, type);
            if (type.mStringIds[this.mIndex] != -1) {
                message = context.getString(type.mStringIds[this.mIndex]).toUpperCase(Locale.US);
            }
            initButtonText(message);
        }
    }

    private void initButtonAttributes(Context context) {
        TypedArray ta = context.obtainStyledAttributes(this.mStyleId, C0088R.styleable.Rotatable);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int index = ta.getIndex(i);
            switch (index) {
                case 2:
                    this.mTextAllCaps = ta.getBoolean(index, true);
                    break;
                case 3:
                    this.mTextSize = ta.getDimensionPixelSize(index, 10);
                    break;
                case 4:
                    this.mTextColor = ta.getInt(index, -7829368);
                    this.mNormalTextColor = this.mTextColor;
                    break;
                case 5:
                    this.mPressedTextColor = ta.getInt(index, -7829368);
                    break;
                case 6:
                    this.mSelectedTextColor = ta.getInt(index, -7829368);
                    break;
                case 7:
                    this.mDisabledTextColor = ta.getInt(index, -7829368);
                    break;
                case 8:
                    this.mTextPaddingTop = ta.getDimensionPixelOffset(index, 0);
                    break;
                case 9:
                    this.mTextPaddingBottom = ta.getDimensionPixelOffset(index, 0);
                    break;
                case 12:
                    this.mTextGravity = ta.getInt(index, 17);
                    break;
                case 13:
                    this.mTextShadowColor = ta.getInt(index, -7829368);
                    break;
                case 14:
                    this.mTextShadowRadius = ta.getFloat(index, 2.0f);
                    break;
                case 17:
                    this.mTextStyle = ta.getString(index);
                    break;
                case 23:
                    this.mTextWidth = ta.getDimensionPixelSize(index, 10);
                    break;
                case 26:
                    this.mTextStrokeWidth = ta.getDimensionPixelSize(index, 0);
                    break;
                case 27:
                    this.mTextStrokeColor = ta.getInt(index, -3355444);
                    break;
                default:
                    break;
            }
        }
        ta.recycle();
    }

    public void initButtonText(String message) {
        setText(message);
        setTextScaleX(1.0f);
        Paint tp = getTextPaint();
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

    public void setText(String string) {
        if (string == null || !this.mTextAllCaps) {
            super.setText(string);
        } else {
            super.setText(string.toUpperCase(Locale.US));
        }
    }

    protected void setTextPaint() {
        super.setTextPaint();
        this.mTextPaint.setAlpha(255);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth;
        int measuredHeight;
        int contentWidth = 0;
        int contentHeight = 0;
        if (getBackground() != null) {
            int bgWidth = getBackground().getMinimumWidth();
            int bgHeight = getBackground().getMinimumHeight();
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
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    public void setColorFilter(ColorFilter cf) {
        super.setColorFilter(cf);
        setTextColorFilter(cf);
    }
}
