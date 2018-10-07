package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.lge.camera.C0088R;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class OutfocusBar extends View implements OnTouchListener {
    protected NinePatchDrawable mBar;
    protected int mBarHalfWidth = 0;
    protected int mBarHeight = 0;
    protected int mBarWidth = 0;
    protected int mBarY = 0;
    protected int mCenterPositionY = 0;
    protected Context mContext = null;
    protected int mCurValue = 0;
    protected Drawable mCursor;
    protected int mCursorHalfHeight = 0;
    protected int mCursorHalfWidth = 0;
    protected Rect mCursorRect = new Rect();
    private int mDefaultValue = 0;
    protected boolean mIsTouched = false;
    protected float mItemGap = 0.0f;
    protected int mLayoutHeight = 0;
    protected int mLayoutWidth = 0;
    public OnOutfocusBarListener mListener = null;
    protected int mMaxStep = 10;
    private int mMaxValue = 0;
    private int mMinValue = 0;
    protected int mScrollPosition = 0;

    public interface OnOutfocusBarListener {
        void onBarTouchDownAndMove(int i);

        void onBarTouchUp();
    }

    public OutfocusBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public OutfocusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public OutfocusBar(Context context) {
        super(context);
        this.mContext = context;
    }

    public void init(int min, int max, int defaultValue) {
        setOnTouchListener(this);
        initResources();
        this.mMinValue = min;
        this.mMaxValue = max;
        this.mMaxStep = this.mMaxValue - this.mMinValue;
        this.mItemGap = (((float) this.mBarWidth) * 1.0f) / ((float) this.mMaxStep);
        this.mDefaultValue = defaultValue;
        this.mScrollPosition = (int) (((float) this.mDefaultValue) * this.mItemGap);
    }

    protected void initResources() {
        this.mCursor = getContext().getDrawable(C0088R.drawable.ic_camera_cursor);
        this.mCursorHalfWidth = this.mCursor.getIntrinsicWidth() / 2;
        this.mCursorHalfHeight = this.mCursor.getIntrinsicHeight() / 2;
        this.mBar = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_camera_progress);
        this.mBarWidth = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.5f);
        this.mBarHeight = Utils.getPx(getContext(), C0088R.dimen.outfocus_bar_height);
    }

    public void setListener(OnOutfocusBarListener listener) {
        this.mListener = listener;
    }

    protected void onDraw(Canvas canvas) {
        this.mLayoutWidth = canvas.getWidth();
        this.mLayoutHeight = canvas.getHeight();
        this.mCenterPositionY = this.mLayoutHeight / 2;
        drawBar(canvas);
        setSelected(this.mIsTouched);
        drawCursor(canvas);
    }

    protected void drawBar(Canvas canvas) {
        if (this.mBar != null) {
            this.mBar.setBounds(this.mCursorHalfWidth, this.mCenterPositionY - (this.mBarHeight / 2), this.mBarWidth - this.mCursorHalfWidth, this.mCenterPositionY + (this.mBarHeight / 2));
            this.mBar.draw(canvas);
        }
    }

    protected void drawCursor(Canvas canvas) {
        if (this.mCursorRect != null && this.mCursor != null) {
            this.mCursorRect.set(this.mScrollPosition - this.mCursorHalfWidth, this.mCenterPositionY - this.mCursorHalfHeight, this.mScrollPosition + this.mCursorHalfWidth, this.mCenterPositionY + this.mCursorHalfWidth);
            this.mCursor.setVisible(true, false);
            this.mCursor.setBounds(this.mCursorRect);
            this.mCursor.draw(canvas);
        }
    }

    public int getScrollPosition() {
        return this.mScrollPosition;
    }

    protected void calculateCurValue() {
        this.mCurValue = (int) (((float) this.mMaxValue) * Math.max(Math.min(1.0f, ((float) (this.mScrollPosition - this.mCursorHalfWidth)) / ((float) (this.mBarWidth - (this.mCursorHalfWidth * 2)))), 0.0f));
        if (this.mCurValue < this.mMinValue) {
            this.mCurValue = this.mMinValue;
        }
        if (this.mCurValue > this.mMaxValue) {
            this.mCurValue = this.mMaxValue;
        }
    }

    public int getCurrentValue() {
        return this.mCurValue;
    }

    protected boolean doTouchEvent(MotionEvent event) {
        switch (event.getActionMasked() & 255) {
            case 0:
            case 2:
                return doTouchDownAndMove(event);
            case 1:
            case 3:
                return doTouchUp();
            case 6:
                return doTouchPointerUp(event);
            default:
                return true;
        }
    }

    protected boolean doTouchPointerUp(MotionEvent event) {
        return false;
    }

    protected boolean doTouchUp() {
        this.mIsTouched = false;
        adjustScrollPosition();
        if (this.mListener != null) {
            this.mListener.onBarTouchUp();
        }
        invalidate();
        return true;
    }

    protected boolean doTouchDownAndMove(MotionEvent event) {
        this.mScrollPosition = (int) event.getX();
        adjustScrollPosition();
        calculateCurValue();
        if (this.mListener != null) {
            this.mListener.onBarTouchDownAndMove(this.mCurValue);
        }
        invalidate();
        return true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (doTouchEvent(event)) {
            return true;
        }
        return false;
    }

    protected void adjustScrollPosition() {
        if (this.mScrollPosition < this.mCursorHalfWidth) {
            this.mScrollPosition = this.mCursorHalfWidth;
        }
        if (this.mScrollPosition > this.mBarWidth - this.mCursorHalfWidth) {
            this.mScrollPosition = this.mBarWidth - this.mCursorHalfWidth;
        }
    }

    public boolean isTouched() {
        return this.mIsTouched;
    }

    public void unbind() {
        setOnTouchListener(null);
        this.mBar = null;
        this.mCursor = null;
        this.mCursorRect = null;
    }
}
