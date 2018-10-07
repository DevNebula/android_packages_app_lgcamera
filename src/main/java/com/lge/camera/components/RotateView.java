package com.lge.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public abstract class RotateView extends ImageButton {
    public static final float BASE_TEXT_SCALE_X_RATE = 0.1f;
    protected static final boolean DEBUG_ON = false;
    public static final float DEFAULT_TEXT_SCALE_X = 1.0f;
    public static final float DEFAULT_TEXT_SHADOWRADIUS = 2.0f;
    public static final int DEFAULT_TEXT_SIZE = 20;
    public static final String ELLIPSIS_STRING = "...";
    public static final int GRAVITY_BOTTOM = 4;
    public static final int GRAVITY_CENTER = 17;
    public static final int GRAVITY_CENTER_HORIZONTAL = 16;
    public static final int GRAVITY_CENTER_VERTICAL = 1;
    public static final int GRAVITY_LEFT = 32;
    public static final int GRAVITY_RIGHT = 64;
    public static final int GRAVITY_TOP = 2;
    public static final int MAX_TEXT_LINES = 10;
    public static final int PIVOT_CENTER = Integer.MAX_VALUE;
    public static final float PORTRAIT_TEXT_WIDTH_CORRECTION_RATE = 0.09f;
    public static final String TEXT_STYLE_BOLD = "bold";
    public static final String TEXT_STYLE_ITALIC = "italic";
    public static final String TEXT_STYLE_NORMAL = "normal";
    protected final int DISABLED_ALPHA;
    protected final int ENABLED_ALPHA;
    protected float mBaseTextPaddingRate;
    private float mBottomBoundaryPos;
    private float mCurScrollPostion;
    protected Paint mDebugPaint;
    protected int mDisabledTextColor;
    protected boolean mEllipsisEnabled;
    protected int mExpand4Rotate;
    protected boolean mIncludeFontPadding;
    protected String[] mLandscapeTextLines;
    protected int mLineSpacingExtra;
    protected int mMaxTextLines;
    protected int mNormalTextColor;
    protected Path[] mPath;
    protected String[] mPortraitTextLines;
    protected int mPressedTextColor;
    protected boolean mRotateIconOnly;
    protected boolean mRotateInsideView;
    protected int mRotatePivotLeft;
    protected int mRotatePivotTop;
    protected RotationInfo mRotationInfo;
    private float mScrollGap;
    protected int mSelectedTextColor;
    protected boolean mSingleLine;
    private boolean mStartAnimation;
    protected int mTEXT_WIDTH_BUFFER;
    protected String mText;
    protected boolean mTextAllCaps;
    protected float mTextBasePadding;
    protected StringBuffer mTextBuffer;
    protected int mTextColor;
    protected int mTextDisabledShadowColor;
    protected int mTextGravity;
    protected String[] mTextLines;
    protected int mTextPaddingBottom;
    protected int mTextPaddingLeft;
    protected int mTextPaddingRight;
    protected int mTextPaddingTop;
    protected Paint mTextPaint;
    protected float mTextScaleX;
    protected int mTextShadowColor;
    protected float mTextShadowDx;
    protected float mTextShadowDy;
    protected float mTextShadowRadius;
    protected int mTextSize;
    protected int mTextStrokeColor;
    protected int mTextStrokeWidth;
    protected String mTextStyle;
    protected int mTextTypeFace;
    protected int mTextWidth;
    private float mTopBoundaryPos;

    protected abstract void canvasRotate(Canvas canvas, int i, int i2);

    protected abstract boolean checkBackground(Canvas canvas);

    public abstract int getTextPaintWidth();

    public abstract void setRotated(int i);

    public RotateView(Context context) {
        this(context, null);
    }

    public RotateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mExpand4Rotate = 0;
        this.ENABLED_ALPHA = 255;
        this.DISABLED_ALPHA = 102;
        this.mBaseTextPaddingRate = 0.1f;
        this.mTEXT_WIDTH_BUFFER = 0;
        this.mTextSize = 20;
        this.mTextWidth = 0;
        this.mTextColor = -3355444;
        this.mTextStrokeWidth = 0;
        this.mTextStrokeColor = -3355444;
        this.mPressedTextColor = -1;
        this.mSelectedTextColor = -1;
        this.mDisabledTextColor = -1;
        this.mNormalTextColor = -3355444;
        this.mTextPaddingTop = 0;
        this.mTextPaddingBottom = 0;
        this.mTextPaddingLeft = 0;
        this.mTextPaddingRight = 0;
        this.mTextGravity = 17;
        this.mTextShadowColor = ViewCompat.MEASURED_STATE_MASK;
        this.mTextDisabledShadowColor = -12303292;
        this.mTextShadowRadius = 0.0f;
        this.mTextShadowDx = 0.0f;
        this.mTextShadowDy = 0.0f;
        this.mTextStyle = "normal";
        this.mText = null;
        this.mTextLines = null;
        this.mLandscapeTextLines = null;
        this.mPortraitTextLines = null;
        this.mTextBuffer = new StringBuffer();
        this.mPath = new Path[10];
        this.mRotatePivotLeft = Integer.MAX_VALUE;
        this.mRotatePivotTop = Integer.MAX_VALUE;
        this.mTextPaint = new Paint();
        this.mEllipsisEnabled = false;
        this.mTextScaleX = 1.0f;
        this.mRotateInsideView = false;
        this.mRotateIconOnly = false;
        this.mIncludeFontPadding = true;
        this.mSingleLine = false;
        this.mMaxTextLines = 10;
        this.mLineSpacingExtra = 0;
        this.mTextAllCaps = false;
        this.mRotationInfo = new RotationInfo();
        this.mDebugPaint = null;
        this.mStartAnimation = false;
        this.mCurScrollPostion = 0.0f;
        this.mTopBoundaryPos = 0.0f;
        this.mBottomBoundaryPos = 0.0f;
        this.mScrollGap = 10.0f;
        this.mTextTypeFace = 0;
        setFocusable(false);
        setStyle(getContext().obtainStyledAttributes(attrs, C0088R.styleable.Rotatable));
    }

    public void setStyle(int defStyle) {
        setStyle(getContext().obtainStyledAttributes(defStyle, C0088R.styleable.Rotatable));
    }

    private void setStyle(TypedArray ta) {
        int n = ta.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case 0:
                    setBackground(ta.getDrawable(attr));
                    break;
                case 1:
                    setText(ta.getString(attr));
                    break;
                case 2:
                    this.mTextAllCaps = ta.getBoolean(attr, false);
                    break;
                case 3:
                    this.mTextSize = ta.getDimensionPixelSize(attr, 20);
                    break;
                case 4:
                    this.mTextColor = ta.getInt(attr, -3355444);
                    this.mNormalTextColor = this.mTextColor;
                    break;
                case 5:
                    this.mPressedTextColor = ta.getInt(attr, -1);
                    break;
                case 6:
                    this.mSelectedTextColor = ta.getInt(attr, -1);
                    break;
                case 7:
                    this.mDisabledTextColor = ta.getInt(attr, -1);
                    break;
                case 8:
                    this.mTextPaddingTop = ta.getDimensionPixelOffset(attr, 0);
                    break;
                case 9:
                    this.mTextPaddingBottom = ta.getDimensionPixelOffset(attr, 0);
                    break;
                case 10:
                    this.mTextPaddingLeft = ta.getDimensionPixelOffset(attr, 0);
                    break;
                case 11:
                    this.mTextPaddingRight = ta.getDimensionPixelOffset(attr, 0);
                    break;
                case 12:
                    this.mTextGravity = ta.getInt(attr, 17);
                    break;
                case 13:
                    this.mTextShadowColor = ta.getInt(attr, ViewCompat.MEASURED_STATE_MASK);
                    break;
                case 14:
                    this.mTextShadowRadius = ta.getFloat(attr, 2.0f);
                    break;
                case 15:
                    this.mTextShadowDx = ta.getFloat(attr, 0.0f);
                    break;
                case 16:
                    this.mTextShadowDy = ta.getFloat(attr, 0.0f);
                    break;
                case 17:
                    this.mTextStyle = ta.getString(attr);
                    break;
                case 18:
                    this.mRotateInsideView = ta.getBoolean(attr, false);
                    break;
                case 19:
                    this.mRotateIconOnly = ta.getBoolean(attr, false);
                    break;
                case 20:
                    this.mRotatePivotLeft = ta.getDimensionPixelOffset(attr, Integer.MAX_VALUE);
                    break;
                case 21:
                    this.mRotatePivotTop = ta.getDimensionPixelOffset(attr, Integer.MAX_VALUE);
                    break;
                case 22:
                    this.mEllipsisEnabled = ta.getBoolean(attr, false);
                    break;
                case 23:
                    this.mTextWidth = ta.getDimensionPixelSize(attr, 10);
                    break;
                case 24:
                    this.mIncludeFontPadding = ta.getBoolean(attr, true);
                    if (!this.mIncludeFontPadding) {
                        this.mBaseTextPaddingRate = 0.0f;
                        break;
                    }
                    break;
                case 25:
                    setFocusable(ta.getBoolean(attr, false));
                    break;
                case 26:
                    this.mTextStrokeWidth = ta.getDimensionPixelSize(attr, 0);
                    break;
                case 27:
                    this.mTextStrokeColor = ta.getInt(attr, -3355444);
                    break;
                case 28:
                    this.mSingleLine = ta.getBoolean(attr, false);
                    break;
                case 29:
                    this.mMaxTextLines = ta.getInt(attr, 10);
                    if (this.mMaxTextLines != 1) {
                        break;
                    }
                    this.mSingleLine = true;
                    break;
                case 30:
                    this.mLineSpacingExtra = ta.getDimensionPixelSize(attr, 0);
                    break;
                default:
                    break;
            }
        }
        this.mTextBasePadding = ((float) this.mTextSize) * this.mBaseTextPaddingRate;
    }

    public void setText(String string, boolean requestLayout) {
        textChanging(string);
        if (requestLayout) {
            requestLayout();
        }
        invalidate();
    }

    public void setText(String string) {
        setText(string, true);
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (isFocusable()) {
            setContentDescription(this.mText);
        }
    }

    public void textChanging(String string) {
        if (string == null) {
            this.mText = "";
        } else {
            if (this.mTextAllCaps) {
                string = string.toUpperCase();
            }
            this.mText = string;
        }
        this.mTextLines = this.mText.split("\n");
        for (int i = 0; i < this.mTextLines.length; i++) {
            if (this.mPath[i] == null) {
                this.mPath[i] = new Path();
            }
        }
        setTextPaint();
        this.mLandscapeTextLines = this.mTextLines;
    }

    public String getText() {
        return this.mText;
    }

    public void AdjustFontSize() {
        int textSize = AdjustFontSize(this.mTextSize);
        if (textSize != this.mTextSize) {
            this.mTextSize = textSize;
            this.mTextBasePadding = ((float) this.mTextSize) * this.mBaseTextPaddingRate;
            invalidate();
        }
    }

    public int AdjustFontSize(int size) {
        if (this.mTextLines == null) {
            return 0;
        }
        if (this.mTextPaint != null) {
            Rect targetBound = new Rect();
            Rect sourceBound = new Rect();
            float paddingTop = ((float) this.mTextPaddingTop) + this.mTextBasePadding;
            float paddingLeft = ((float) this.mTextPaddingLeft) + this.mTextBasePadding;
            float paddingBottom = ((float) this.mTextPaddingBottom) + this.mTextBasePadding;
            float paddingRight = ((float) this.mTextPaddingRight) + this.mTextBasePadding;
            LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                int minWidth = layoutParams.width < layoutParams.height ? layoutParams.width : layoutParams.height;
                targetBound.left = 0;
                targetBound.top = 0;
                targetBound.right = (int) (((float) minWidth) - (paddingLeft + paddingRight));
                targetBound.bottom = (int) (((float) minWidth) - (paddingTop + paddingBottom));
            }
            int maxLength = 0;
            String maxString = null;
            for (String strLine : this.mTextLines) {
                if (strLine.length() > maxLength) {
                    maxString = strLine;
                    maxLength = strLine.length();
                }
            }
            size = getFontSize(size, maxString, maxLength, targetBound, sourceBound);
        }
        return size;
    }

    private int getFontSize(int size, String maxString, int maxLength, Rect targetBound, Rect sourceBound) {
        if (maxString == null || targetBound.width() <= 0 || targetBound.height() <= 0) {
            return size;
        }
        int tmpSize = size;
        int tmpScaleX = 10;
        this.mTextScaleX = this.mTextPaint.getTextScaleX();
        this.mTextPaint.setTextSize((float) tmpSize);
        this.mTextPaint.setTextScaleX(this.mTextScaleX);
        this.mTextPaint.getTextBounds(maxString, 0, maxLength, sourceBound);
        sourceBound.offsetTo(0, 0);
        sourceBound.right = (int) (((float) sourceBound.right) + (((float) sourceBound.width()) * this.mBaseTextPaddingRate));
        while (!targetBound.contains(sourceBound) && tmpScaleX >= 7) {
            tmpScaleX--;
            this.mTextPaint.setTextScaleX(((float) tmpScaleX) * 0.1f);
            this.mTextPaint.getTextBounds(maxString, 0, maxLength, sourceBound);
            sourceBound.offsetTo(0, 0);
            this.mTextScaleX = ((float) tmpScaleX) * 0.1f;
        }
        while (!targetBound.contains(sourceBound)) {
            tmpSize--;
            this.mTextPaint.setTextSize((float) tmpSize);
            this.mTextPaint.getTextBounds(maxString, 0, maxLength, sourceBound);
            sourceBound.offsetTo(0, 0);
        }
        if (tmpSize > 0) {
            return tmpSize;
        }
        return size;
    }

    public void setTextSize(int size) {
        this.mTextSize = AdjustFontSize(size);
        this.mTextBasePadding = ((float) this.mTextSize) * this.mBaseTextPaddingRate;
        invalidate();
    }

    public void setTextColor(int color) {
        this.mTextColor = color;
        invalidate();
    }

    public void setTextGravity(int gravity) {
        this.mTextGravity = gravity;
        invalidate();
    }

    public void setTextShadowColor(int color) {
        this.mTextShadowColor = color;
        invalidate();
    }

    public void setTextShadowRadius(float radius) {
        this.mTextShadowRadius = radius;
        invalidate();
    }

    public void setTextShadowDxDy(float dx, float dy) {
        this.mTextShadowDx = dx;
        this.mTextShadowDy = dy;
        invalidate();
    }

    public void setRotateInsideView(boolean enable) {
        this.mRotateInsideView = enable;
        invalidate();
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mRotationInfo != null) {
            this.mRotationInfo.setDegree(degree, animation);
        }
        invalidate();
    }

    public int getDegree() {
        if (this.mRotationInfo != null) {
            return this.mRotationInfo.getTargetDegree();
        }
        return 0;
    }

    protected void onDraw(Canvas canvas) {
        try {
            if (checkBackground(canvas)) {
                if (!(this.mRotationInfo == null || this.mRotationInfo.getCurrentDegree() == this.mRotationInfo.getTargetDegree() || !this.mRotationInfo.calcCurrentDegree())) {
                    invalidate();
                }
                int viewWidth = getWidth();
                int viewHeight = getHeight();
                canvasRotate(canvas, viewWidth, viewHeight);
                super.onDraw(canvas);
                if (this.mText != null) {
                    drawText(canvas, viewWidth, viewHeight);
                }
            }
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, String.format("RotateImageButton onDraw exception: %s", new Object[]{e}));
            e.printStackTrace();
        }
    }

    protected void drawText(Canvas canvas, int viewWidth, int viewHeight) {
        if (this.mTextLines != null) {
            setTextPaint();
            float paddingTop = ((float) this.mTextPaddingTop) + this.mTextBasePadding;
            float paddingLeft = ((float) this.mTextPaddingLeft) + this.mTextBasePadding;
            float paddingBottom = ((float) this.mTextPaddingBottom) + this.mTextBasePadding;
            float paddingRight = ((float) this.mTextPaddingRight) + this.mTextBasePadding;
            float textHeight = this.mTextPaint.getFontSpacing() + ((float) this.mLineSpacingExtra);
            float textAreaHeight = (((float) viewHeight) - paddingTop) - paddingBottom;
            float textAreaWidth = (((float) viewWidth) - paddingLeft) - paddingRight;
            float totalTextLineHeight = textHeight * ((float) this.mTextLines.length);
            float aboveBaseLine = -this.mTextPaint.ascent();
            float belowBaseLine = this.mTextPaint.descent();
            for (int i = 0; i < this.mTextLines.length; i++) {
                float textOffsetY;
                float textWidth = this.mTextPaint.measureText(this.mTextLines[i]);
                this.mTextBuffer.setLength(0);
                if (!this.mEllipsisEnabled || ((float) viewWidth) >= (textWidth + paddingLeft) + paddingRight) {
                    this.mTextBuffer.append(this.mTextLines[i]);
                } else {
                    int length = this.mTextPaint.breakText(this.mTextLines[i], true, (((float) viewWidth) - paddingLeft) - paddingRight, null);
                    if (length > 2) {
                        this.mTextBuffer.append(this.mTextLines[i].substring(0, length - 2));
                        this.mTextBuffer.append(ELLIPSIS_STRING);
                        textWidth = this.mTextPaint.measureText(this.mTextBuffer.toString());
                    }
                }
                if ((this.mTextGravity & 2) != 0) {
                    textOffsetY = (paddingTop + aboveBaseLine) + (((float) i) * textHeight);
                } else if ((this.mTextGravity & 4) != 0) {
                    textOffsetY = ((((float) viewHeight) - paddingBottom) - belowBaseLine) - (((float) ((this.mTextLines.length - 1) - i)) * textHeight);
                } else {
                    textOffsetY = ((((textAreaHeight - totalTextLineHeight) / 2.0f) + paddingTop) + aboveBaseLine) + (((float) i) * textHeight);
                }
                drawLineForDebug(canvas, viewWidth, textOffsetY, aboveBaseLine, belowBaseLine);
                float textOffsetX = getTextOffsetX(viewWidth, paddingLeft, paddingRight, textWidth, textAreaWidth);
                float preventClippingMargin = textWidth * this.mBaseTextPaddingRate;
                this.mPath[i].reset();
                this.mPath[i].moveTo(textOffsetX, textOffsetY);
                this.mPath[i].lineTo((textOffsetX + textWidth) + preventClippingMargin, textOffsetY);
                drawTextWithSpinEffect(canvas, textOffsetX, textOffsetY);
            }
            drawPaddingForDebug(canvas, viewWidth, viewHeight, paddingTop, paddingBottom, paddingLeft, paddingRight);
        }
    }

    private void drawTextWithSpinEffect(Canvas canvas, float textOffsetX, float textOffsetY) {
        if (this.mStartAnimation) {
            textOffsetY += this.mCurScrollPostion;
            this.mCurScrollPostion += this.mScrollGap;
            if (this.mCurScrollPostion >= this.mBottomBoundaryPos) {
                this.mCurScrollPostion = this.mTopBoundaryPos;
            }
        }
        if (this.mTextStrokeWidth != 0) {
            float alpha = (float) this.mTextPaint.getAlpha();
            this.mTextPaint.setStyle(Style.STROKE);
            this.mTextPaint.setStrokeWidth((float) this.mTextStrokeWidth);
            this.mTextPaint.setColor(this.mTextStrokeColor);
            canvas.drawText(this.mTextBuffer.toString(), textOffsetX, textOffsetY, this.mTextPaint);
            this.mTextPaint.setStyle(Style.FILL);
            this.mTextPaint.setColor(this.mTextColor);
            this.mTextPaint.setAlpha((int) alpha);
            canvas.drawText(this.mTextBuffer.toString(), textOffsetX, textOffsetY, this.mTextPaint);
        } else {
            canvas.drawText(this.mTextBuffer.toString(), textOffsetX, textOffsetY, this.mTextPaint);
        }
        if (this.mStartAnimation) {
            invalidate();
        }
    }

    private float getTextOffsetX(int viewWidth, float paddingLeft, float paddingRight, float textWidth, float textAreaWidth) {
        if ((this.mTextGravity & 32) != 0) {
            return paddingLeft;
        }
        if ((this.mTextGravity & 64) != 0) {
            return (((float) viewWidth) - textWidth) - paddingRight;
        }
        return ((textAreaWidth - textWidth) / 2.0f) + paddingLeft;
    }

    private void drawLineForDebug(Canvas canvas, int viewWidth, float textOffsetY, float aboveBaseLine, float belowBaseLine) {
    }

    private void drawPaddingForDebug(Canvas canvas, int viewWidth, int viewHeight, float paddingTop, float paddingBottom, float paddingLeft, float paddingRight) {
    }

    protected void setTextPaint() {
        this.mTextPaint.setTextSize((float) this.mTextSize);
        this.mTextPaint.setTextScaleX(this.mTextScaleX);
        int textColor = this.mTextColor;
        if (isPressed() && this.mPressedTextColor != -1) {
            textColor = this.mPressedTextColor;
        } else if (isSelected() && this.mSelectedTextColor != -1) {
            textColor = this.mSelectedTextColor;
        }
        if (!(isEnabled() || this.mDisabledTextColor == -1)) {
            textColor = this.mDisabledTextColor;
        }
        this.mTextColor = textColor;
        this.mTextPaint.setColor(textColor);
        if (isEnabled()) {
            this.mTextPaint.setAlpha(255);
        } else if (this.mDisabledTextColor == -1) {
            this.mTextPaint.setAlpha(102);
        }
        this.mTextPaint.setAntiAlias(true);
        if ("normal".equals(this.mTextStyle)) {
            setTextTypeface(0);
        } else if (TEXT_STYLE_BOLD.equals(this.mTextStyle)) {
            setTextTypeface(1);
        } else if (TEXT_STYLE_ITALIC.equals(this.mTextStyle)) {
            setTextTypeface(2);
        }
        this.mTextPaint.setShadowLayer(this.mTextShadowRadius, this.mTextShadowDx, this.mTextShadowDy, this.mTextShadowColor);
        if (!isEnabled()) {
            this.mTextPaint.setShadowLayer(this.mTextShadowRadius, this.mTextShadowDx, this.mTextShadowDy, this.mTextDisabledShadowColor);
        }
    }

    public void invalidate() {
        invalidate(0, 0, getWidth(), getHeight());
    }

    public void invalidate(Rect dirty) {
        dirty.left -= this.mExpand4Rotate;
        dirty.top -= this.mExpand4Rotate;
        dirty.right += this.mExpand4Rotate;
        dirty.bottom += this.mExpand4Rotate;
        super.invalidate(dirty);
    }

    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l - this.mExpand4Rotate, t - this.mExpand4Rotate, this.mExpand4Rotate + r, this.mExpand4Rotate + b);
    }

    public int getTextPaintHeight() {
        Paint p = new Paint();
        Rect textBounds = new Rect();
        p.getTextBounds(this.mText, 0, this.mText.length(), textBounds);
        return textBounds.height();
    }

    public int getTextSize() {
        return this.mTextSize;
    }

    public Paint getTextPaint() {
        return this.mTextPaint;
    }

    protected int getTextPaddingLeft() {
        return this.mTextPaddingLeft;
    }

    protected int getTextPaddingRight() {
        return this.mTextPaddingRight;
    }

    protected int getTextPaddingTop() {
        return this.mTextPaddingTop;
    }

    protected int getTextPaddingBottom() {
        return this.mTextPaddingBottom;
    }

    public void setTextPaddingTop(int paddingTop) {
        this.mTextPaddingTop = paddingTop;
        invalidate();
    }

    public void setTextPaddingBottom(int paddingBottom) {
        this.mTextPaddingBottom = paddingBottom;
        invalidate();
    }

    public void setTextScaleX(float scaleX) {
        this.mTextScaleX = scaleX;
        invalidate();
    }

    public void setTypeface(Typeface tf) {
        boolean z = false;
        if (this.mTextPaint != null) {
            this.mTextPaint.setTypeface(tf);
            int need = this.mTextTypeFace & ((tf != null ? tf.getStyle() : 0) ^ -1);
            Paint paint = this.mTextPaint;
            if ((need & 1) != 0) {
                z = true;
            }
            paint.setFakeBoldText(z);
            this.mTextPaint.setTextSkewX((need & 2) != 0 ? -0.25f : 0.0f);
        }
    }

    public void setTextTypeface(int style) {
        boolean z = false;
        if (this.mTextTypeFace != style) {
            this.mTextTypeFace = style;
            if (style > 0) {
                int typefaceStyle;
                float f;
                Typeface tf = Typeface.defaultFromStyle(style);
                this.mTextPaint.setTypeface(tf);
                if (tf != null) {
                    typefaceStyle = tf.getStyle();
                } else {
                    typefaceStyle = 0;
                }
                int need = style & (typefaceStyle ^ -1);
                Paint paint = this.mTextPaint;
                if ((need & 1) != 0) {
                    z = true;
                }
                paint.setFakeBoldText(z);
                paint = this.mTextPaint;
                if ((need & 2) != 0) {
                    f = -0.25f;
                } else {
                    f = 0.0f;
                }
                paint.setTextSkewX(f);
            } else {
                this.mTextPaint.setFakeBoldText(false);
                this.mTextPaint.setTextSkewX(0.0f);
                this.mTextPaint.setTypeface(null);
            }
            invalidate();
        }
    }

    protected String[] wordWrap(int maxWidth, String message) {
        if (message == null) {
            return null;
        }
        StringBuffer mergedText = new StringBuffer(message);
        maxWidth = (int) (((float) maxWidth) - (((((float) this.mTextPaddingLeft) + this.mTextBasePadding) + ((float) this.mTextPaddingRight)) + this.mTextBasePadding));
        maxWidth = (int) (((float) maxWidth) - (((float) maxWidth) * 0.09f));
        ArrayList<String> textLines = new ArrayList();
        int i = 0;
        while (mergedText.length() > 0 && i < this.mMaxTextLines) {
            while (mergedText.length() > 0 && mergedText.charAt(0) == ' ') {
                mergedText.deleteCharAt(0);
            }
            String remainText = mergedText.toString();
            maxWidth += this.mTEXT_WIDTH_BUFFER;
            int lineLength = this.mTextPaint.breakText(remainText, true, (float) maxWidth, new float[this.mMaxTextLines]);
            if (lineLength > mergedText.length()) {
                lineLength = mergedText.length();
            }
            String currentLine = mergedText.substring(0, lineLength);
            if (lineLength < remainText.length()) {
                lineLength = currentLine.lastIndexOf(32);
                if (lineLength < 0) {
                    lineLength = currentLine.length();
                }
            }
            textLines.add(mergedText.substring(0, lineLength));
            mergedText.delete(0, lineLength);
            i++;
        }
        String[] result = new String[textLines.size()];
        textLines.toArray(result);
        return result;
    }

    protected void setTextLines(String[] textLines) {
        if (textLines != null) {
            if (this.mSingleLine) {
                textLines = new String[]{textLines.toString()};
            }
            this.mTextLines = textLines;
            if (this.mTextLines != null) {
                for (int i = 0; i < this.mTextLines.length; i++) {
                    if (this.mPath[i] == null) {
                        this.mPath[i] = new Path();
                    }
                }
            }
        }
    }
}
