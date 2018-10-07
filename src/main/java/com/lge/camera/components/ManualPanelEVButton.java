package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class ManualPanelEVButton extends RotateImageButton {
    private Bitmap mArrowIcon;
    protected final Paint mArrowIconPaint;
    private int mCursorIndex;
    private String mEvValue;
    private boolean mIsPortraitMode;
    private int mMAX_ARROW_CURSOR_INDEX;
    protected final Paint mTxtPaintTitle;
    protected final Paint mTxtPaintValue;
    private float mTxtSize;

    public ManualPanelEVButton(Context context) {
        this(context, null);
    }

    public ManualPanelEVButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ManualPanelEVButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mArrowIcon = null;
        this.mArrowIconPaint = new Paint(1);
        this.mTxtPaintTitle = new Paint(1);
        this.mTxtPaintValue = new Paint(1);
        this.mMAX_ARROW_CURSOR_INDEX = 8;
        this.mCursorIndex = this.mMAX_ARROW_CURSOR_INDEX / 2;
        this.mTxtSize = 0.0f;
        this.mIsPortraitMode = false;
        this.mEvValue = null;
    }

    public void init() {
        setArrowBitmap();
        initResources();
        if (this.mTxtPaintTitle != null) {
            this.mTxtPaintTitle.setColor(-1);
            this.mTxtPaintTitle.setTextSize(this.mTxtSize);
            this.mTxtPaintTitle.setTextAlign(Align.LEFT);
            this.mTxtPaintTitle.setFakeBoldText(true);
            this.mTxtPaintTitle.setShadowLayer(this.mTextShadowRadius, this.mTextShadowDx, this.mTextShadowDy, this.mTextShadowColor);
        }
        if (this.mTxtPaintValue != null) {
            this.mTxtPaintValue.setColor(-1);
            this.mTxtPaintValue.setTextSize(this.mTxtSize);
            this.mTxtPaintValue.setTextAlign(Align.LEFT);
            this.mTxtPaintValue.setFakeBoldText(false);
            this.mTxtPaintValue.setShadowLayer(this.mTextShadowRadius, this.mTextShadowDx, this.mTextShadowDy, this.mTextShadowColor);
        }
    }

    private void initResources() {
        this.mTxtSize = Utils.dpToPx(getContext(), 12.0f);
    }

    private void setArrowBitmap() {
        if (this.mArrowIcon == null) {
            this.mArrowIcon = BitmapFactory.decodeResource(getResources(), C0088R.drawable.camera_manual_panel_icon_ev_arrow);
        }
    }

    public void setPortraitMode(boolean portraitMode) {
        this.mIsPortraitMode = portraitMode;
        if (portraitMode) {
            setBackground(getResources().getDrawable(C0088R.drawable.camera_manual_panel_icon_ev_top_dummy));
        } else {
            setBackground(getResources().getDrawable(C0088R.drawable.camera_manual_panel_icon_ev_top));
        }
        invalidate();
    }

    public void setEVValue(int evValue, String evValueStr) {
        this.mEvValue = evValueStr;
        int cursorIndex = evValue + (this.mMAX_ARROW_CURSOR_INDEX / 2);
        if (cursorIndex != this.mCursorIndex) {
            this.mCursorIndex = cursorIndex;
            requestLayout();
            invalidate();
        }
    }

    public void setMaxCursorIndex(int maxIndex) {
        this.mMAX_ARROW_CURSOR_INDEX = maxIndex;
    }

    public void drawContents(Canvas canvas) {
        if (this.mIsPortraitMode) {
            String title = "EV";
            String sign = "";
            if (Float.compare(0.0f, Float.valueOf(Float.parseFloat(this.mEvValue)).floatValue()) <= 0) {
                sign = sign + "+";
            }
            Rect mTxtRect = new Rect();
            this.mTxtPaintTitle.getTextBounds(title, 0, title.length(), mTxtRect);
            float txtX = (float) mTxtRect.width();
            float txtY = this.mTxtSize + 18.0f;
            canvas.drawText(title, 0.0f, txtY, this.mTxtPaintTitle);
            canvas.drawText(sign + this.mEvValue, txtX, txtY, this.mTxtPaintValue);
            return;
        }
        int paddingLeft = getTextPaddingLeft();
        int paddingRight = getTextPaddingRight();
        int paddingBottom = getTextPaddingBottom();
        int arrowWidth = this.mArrowIcon.getWidth();
        int cursorCoordLeft = ((int) (((float) this.mCursorIndex) * (((float) (((getWidth() - paddingLeft) - paddingRight) - arrowWidth)) / ((float) this.mMAX_ARROW_CURSOR_INDEX)))) + paddingLeft;
        int cursorCoordTop = (getHeight() - paddingBottom) - this.mArrowIcon.getHeight();
        canvas.drawBitmap(this.mArrowIcon, (float) cursorCoordLeft, (float) cursorCoordTop, this.mArrowIconPaint);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContents(canvas);
    }

    public void setColorFilter(ColorFilter cf) {
        super.setColorFilter(cf);
        getBackground().setColorFilter(cf);
        this.mArrowIconPaint.setColorFilter(cf);
        this.mTxtPaintTitle.setColorFilter(cf);
        this.mTxtPaintValue.setColorFilter(cf);
    }

    public void unbind() {
        if (this.mArrowIcon != null) {
            this.mArrowIcon.recycle();
            this.mArrowIcon = null;
        }
    }
}
