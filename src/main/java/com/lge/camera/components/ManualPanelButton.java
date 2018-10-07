package com.lge.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.ColorUtil;

public class ManualPanelButton extends RotateImageButton {
    protected String mContentDescription;
    private Bitmap mLockIcon;
    protected int mLockIconPaddingStart;
    protected int mLockIconPaddingTop;
    protected final Paint mLockIconPaint;
    protected boolean mShowLockIcon;
    protected String mTitle;
    protected int mTitleTextColor;
    protected int mTitleTextColorDisabled;
    protected int mTitleTextColorNormal;
    protected int mTitleTextColorPressed;
    protected int mTitleTextColorSelected;
    protected int mTitleTextGravity;
    protected int mTitleTextPaddingBottom;
    protected int mTitleTextPaddingLeft;
    protected int mTitleTextPaddingRight;
    protected int mTitleTextPaddingTop;
    protected Paint mTitleTextPaint;
    protected int mTitleTextShadowColor;
    protected float mTitleTextShadowRadius;
    protected int mTitleTextSize;
    protected int mTitleTextStrokeColor;
    protected int mTitleTextStrokeWidth;
    protected String mTitleTextStyle;
    protected int mTitleTextTypeFace;

    public ManualPanelButton(Context context) {
        this(context, null);
        setRotateIconOnly(true);
    }

    public ManualPanelButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setRotateIconOnly(true);
    }

    public ManualPanelButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTitleTextPaint = new Paint();
        this.mTitle = "";
        this.mContentDescription = "";
        this.mLockIcon = null;
        this.mLockIconPaint = new Paint(1);
        this.mLockIconPaddingTop = 0;
        this.mLockIconPaddingStart = 0;
        this.mShowLockIcon = false;
        this.mTitleTextSize = 20;
        this.mTitleTextColor = -7829368;
        this.mTitleTextColorNormal = -7829368;
        this.mTitleTextColorSelected = -1;
        this.mTitleTextColorPressed = -1;
        this.mTitleTextColorDisabled = -1;
        this.mTitleTextPaddingRight = 0;
        this.mTitleTextPaddingLeft = 0;
        this.mTitleTextPaddingTop = 0;
        this.mTitleTextPaddingBottom = 0;
        this.mTitleTextStrokeColor = ViewCompat.MEASURED_STATE_MASK;
        this.mTitleTextStrokeWidth = 0;
        this.mTitleTextShadowColor = ViewCompat.MEASURED_STATE_MASK;
        this.mTitleTextShadowRadius = 0.0f;
        this.mTitleTextTypeFace = 0;
        this.mTitleTextStyle = "normal";
        this.mTitleTextGravity = 2;
        setTitleStyle(getContext().obtainStyledAttributes(attrs, C0088R.styleable.Rotatable));
        setRotateIconOnly(true);
    }

    protected void setTitleStyle(TypedArray ta) {
        int n = ta.getIndexCount();
        for (int i = 0; i < n; i++) {
            setTitleStyle(ta, ta.getIndex(i));
        }
    }

    public void setTitle(String title) {
        this.mTitle = title;
        if (this.mLockIcon == null) {
            this.mLockIcon = BitmapManagingUtil.getBitmap(this.mContext, C0088R.drawable.camera_icon_manual_video_lock_normal);
            this.mLockIconPaddingTop = Math.round(getResources().getDimension(C0088R.dimen.manual_lock_icon_marginTop));
            this.mLockIconPaddingStart = Math.round(getResources().getDimension(C0088R.dimen.manual_lock_icon_marginStart));
        }
    }

    public void setContentDescriptionString(String contentDescription) {
        this.mContentDescription = contentDescription;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getContentDescriptionString() {
        return this.mContentDescription;
    }

    protected void setTextPaint() {
        this.mTitleTextPaint.setTextSize((float) this.mTitleTextSize);
        this.mTitleTextPaint.setTextScaleX(this.mTextScaleX);
        int textColor = this.mTitleTextColor;
        if (isPressed() && this.mTitleTextColorPressed != -1) {
            textColor = this.mTitleTextColorPressed;
        } else if (isSelected() && this.mTitleTextColorSelected != -1) {
            textColor = this.mTitleTextColorSelected;
        }
        if (!(isEnabled() || this.mTitleTextColorDisabled == -1)) {
            textColor = this.mTitleTextColorDisabled;
        }
        this.mTitleTextColor = textColor;
        this.mTitleTextPaint.setColor(textColor);
        if (isEnabled()) {
            this.mTitleTextPaint.setAlpha(255);
        } else if (this.mTitleTextColorDisabled == -1) {
            this.mTitleTextPaint.setAlpha(102);
        }
        this.mTitleTextPaint.setAntiAlias(true);
        if ("normal".equals(this.mTitleTextStyle)) {
            setTitleTextTypeface(0);
        } else if (RotateView.TEXT_STYLE_BOLD.equals(this.mTitleTextStyle)) {
            setTitleTextTypeface(1);
        } else if (RotateView.TEXT_STYLE_ITALIC.equals(this.mTitleTextStyle)) {
            setTitleTextTypeface(2);
        }
        this.mTitleTextPaint.setShadowLayer(this.mTitleTextShadowRadius, this.mTextShadowDx, this.mTextShadowDy, this.mTitleTextShadowColor);
        if (!isEnabled()) {
            this.mTitleTextPaint.setShadowLayer(this.mTitleTextShadowRadius, this.mTextShadowDx, this.mTextShadowDy, this.mTextDisabledShadowColor);
        }
        super.setTextPaint();
    }

    private void setTitleTextTypeface(int style) {
        boolean z = false;
        if (this.mTitleTextTypeFace != style) {
            this.mTitleTextTypeFace = style;
            if (style > 0) {
                int typefaceStyle;
                float f;
                Typeface tf = Typeface.defaultFromStyle(style);
                this.mTitleTextPaint.setTypeface(tf);
                if (tf != null) {
                    typefaceStyle = tf.getStyle();
                } else {
                    typefaceStyle = 0;
                }
                int need = style & (typefaceStyle ^ -1);
                Paint paint = this.mTitleTextPaint;
                if ((need & 1) != 0) {
                    z = true;
                }
                paint.setFakeBoldText(z);
                paint = this.mTitleTextPaint;
                if ((need & 2) != 0) {
                    f = -0.25f;
                } else {
                    f = 0.0f;
                }
                paint.setTextSkewX(f);
                return;
            }
            this.mTitleTextPaint.setFakeBoldText(false);
            this.mTitleTextPaint.setTextSkewX(0.0f);
            this.mTitleTextPaint.setTypeface(null);
        }
    }

    protected void drawText(Canvas canvas, int viewWidth, int viewHeight) {
        super.drawText(canvas, viewWidth, viewHeight);
        int textOffsetX = this.mTitleTextPaddingLeft;
        float textAreaWidth = (float) ((viewWidth - this.mTitleTextPaddingLeft) - this.mTitleTextPaddingRight);
        float textWidth = (float) ((int) this.mTitleTextPaint.measureText(this.mTitle));
        if ((this.mTextGravity & 32) != 0) {
            textOffsetX = this.mTitleTextPaddingLeft;
        } else if ((this.mTextGravity & 64) != 0) {
            textOffsetX = (int) ((((float) viewWidth) - textWidth) - ((float) this.mTitleTextPaddingRight));
        } else {
            textOffsetX = (int) (((textAreaWidth - textWidth) / 2.0f) + ((float) this.mTitleTextPaddingLeft));
        }
        int textOffsetY = this.mTitleTextPaddingTop;
        if ((this.mTitleTextGravity & 2) != 0) {
            textOffsetY = this.mTitleTextPaddingTop;
        } else if ((this.mTitleTextGravity & 4) != 0) {
            textOffsetY = viewHeight - this.mTitleTextPaddingBottom;
        }
        float alpha = (float) this.mTitleTextPaint.getAlpha();
        this.mTitleTextPaint.getTextBounds(this.mTitle, 0, this.mTitle.length(), new Rect());
        this.mTitleTextPaint.setStyle(Style.STROKE);
        this.mTitleTextPaint.setStrokeWidth((float) this.mTitleTextStrokeWidth);
        this.mTitleTextPaint.setColor(this.mTitleTextStrokeColor);
        canvas.drawText(this.mTitle, (float) textOffsetX, (float) (this.mTitleTextSize + textOffsetY), this.mTitleTextPaint);
        this.mTitleTextPaint.setStyle(Style.FILL);
        this.mTitleTextPaint.setColor(this.mTitleTextColor);
        this.mTitleTextPaint.setAlpha((int) alpha);
        canvas.drawText(this.mTitle, (float) textOffsetX, (float) (this.mTitleTextSize + textOffsetY), this.mTitleTextPaint);
        if (this.mShowLockIcon) {
            canvas.drawBitmap(this.mLockIcon, (((float) textOffsetX) + textWidth) + ((float) this.mLockIconPaddingStart), (float) this.mLockIconPaddingTop, this.mLockIconPaint);
        }
    }

    protected void setTitleStyle(TypedArray ta, int attr) {
        switch (attr) {
            case 31:
                this.mTitleTextColor = ta.getInt(attr, -3355444);
                this.mTitleTextColorNormal = this.mTitleTextColor;
                return;
            case 32:
                this.mTitleTextColorSelected = ta.getInt(attr, -1);
                return;
            case 33:
                this.mTitleTextColorPressed = ta.getInt(attr, -1);
                return;
            case 34:
                this.mTitleTextColorDisabled = ta.getInt(attr, -1);
                return;
            case 35:
                this.mTitleTextSize = ta.getDimensionPixelSize(attr, 20);
                return;
            case 36:
                this.mTitleTextShadowColor = ta.getInt(attr, ViewCompat.MEASURED_STATE_MASK);
                return;
            case 37:
                this.mTitleTextShadowRadius = ta.getFloat(attr, 2.0f);
                return;
            case 38:
                this.mTitleTextStyle = ta.getString(attr);
                return;
            case 39:
                this.mTitleTextStrokeWidth = ta.getDimensionPixelSize(attr, 0);
                return;
            case 40:
                this.mTitleTextStrokeColor = ta.getInt(attr, -3355444);
                return;
            case 41:
                this.mTitleTextPaddingTop = ta.getDimensionPixelOffset(attr, 0);
                return;
            case 42:
                this.mTitleTextPaddingBottom = ta.getDimensionPixelOffset(attr, 0);
                return;
            case 43:
                this.mTitleTextPaddingLeft = ta.getDimensionPixelOffset(attr, 0);
                return;
            case 44:
                this.mTitleTextPaddingRight = ta.getDimensionPixelOffset(attr, 0);
                return;
            case 45:
                this.mTitleTextGravity = ta.getInt(attr, 17);
                return;
            default:
                return;
        }
    }

    public void setTextColor(int color) {
        this.mTitleTextColor = color;
        setDrawablesColorFilter(isEnabled());
        super.setTextColor(color);
    }

    public void setDrawablesColorFilter(boolean enabled) {
        float alpha = 255.0f;
        if (!enabled) {
            if (this.mDisabledTextColor == -1) {
                alpha = 102.0f;
            } else {
                alpha = (float) Color.alpha(this.mDisabledTextColor);
            }
        }
        ColorFilter cf = ColorUtil.getColorMatrix(alpha, (float) Color.red(this.mTitleTextColor), (float) Color.green(this.mTitleTextColor), (float) Color.blue(this.mTitleTextColor));
        Drawable drawable = getDrawable();
        if (drawable != null) {
            drawable.setColorFilter(cf);
        }
        if (this.mLockIcon != null) {
            this.mLockIconPaint.setColorFilter(cf);
        }
    }

    public void setSelected(boolean selected) {
        setImageLevel(selected ? 1 : 0);
        super.setSelected(selected);
    }

    public void setPressed(boolean pressed) {
        if (pressed) {
            setImageLevel(1);
        } else if (isSelected()) {
            setImageLevel(1);
        } else {
            setImageLevel(0);
        }
        super.setPressed(pressed);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setDrawablesColorFilter(enabled);
    }

    public void setShowLockIcon(boolean isShowing) {
        if (this.mShowLockIcon != isShowing) {
            this.mShowLockIcon = isShowing;
            invalidate();
        }
    }
}
