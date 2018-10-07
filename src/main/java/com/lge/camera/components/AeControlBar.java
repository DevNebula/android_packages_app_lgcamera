package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.lge.camera.C0088R;
import com.lge.camera.managers.AEControlBarInterface;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.Utils;

public class AeControlBar extends View implements OnTouchListener {
    protected static final int COLOR_PROGRESS = Color.parseColor("#4bdbbe");
    protected final float AUDIO_CONTROL_BAR_HEIGHT = 0.168f;
    protected final float AUDIO_CONTROL_BAR_MARGIN_TOP = 0.02f;
    protected NinePatchDrawable mBarBackgroundBitmap;
    protected NinePatchDrawable mBarBitmap;
    protected int mBarHalfHeight = 0;
    protected int mBarHalfWidth = 0;
    protected int mBarHeight = 0;
    protected boolean mBarTouchState = false;
    protected int mBarWidth = 0;
    protected Context mContext = null;
    protected int mCurValue = 0;
    protected int mCursorBorderWidth = 0;
    protected int mCursorHalfHeight = 0;
    protected int mCursorHalfWidth = 0;
    protected int mCursorHeight = 0;
    protected Drawable mCursorNormal;
    protected Rect mCursorRect = new Rect();
    protected int mCursorWidth = 0;
    protected int mDefaultBarValue = 0;
    protected int mDiff = 0;
    protected ModuleInterface mGet;
    protected boolean mIsEnabled = true;
    protected boolean mIsTouchMoving = false;
    protected int mItemGap = 0;
    protected int mLayoutHalfHeight = 0;
    protected int mLayoutHalfWidth = 0;
    protected int mLayoutHeight = 0;
    protected int mLayoutWidth = 0;
    public AEControlBarInterface mListener = null;
    protected int mMaxStep = 10;
    protected int mPreValue = 0;
    protected int mPrevDiff = 0;
    protected Rect mProgressBar = new Rect();
    protected Paint mProgressBarPaint = new Paint();
    protected RotationInfo mRotationInfo = new RotationInfo();
    protected int mStartPosX = 0;

    public AeControlBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public AeControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public AeControlBar(Context context) {
        super(context);
        this.mContext = context;
    }

    public void init(ModuleInterface mGet, int min, int max, int defaultValue) {
        this.mGet = mGet;
        setOnTouchListener(this);
        initDrawables();
        this.mProgressBarPaint.setColor(COLOR_PROGRESS);
        this.mBarWidth = Utils.getPx(getContext(), C0088R.dimen.ae_control_bar_width);
        if (this.mBarBackgroundBitmap != null) {
            this.mBarHeight = this.mBarBackgroundBitmap.getIntrinsicHeight();
        }
        this.mBarHalfWidth = this.mBarWidth / 2;
        this.mBarHalfHeight = this.mBarHeight / 2;
        this.mMaxStep = max - min;
        this.mItemGap = this.mBarWidth / this.mMaxStep;
        this.mDefaultBarValue = defaultValue;
        this.mPreValue = this.mDefaultBarValue;
        this.mCurValue = this.mDefaultBarValue;
    }

    protected void initDrawables() {
        this.mCursorNormal = getContext().getDrawable(C0088R.drawable.ic_camera_cursor);
        this.mCursorWidth = this.mCursorNormal.getIntrinsicWidth();
        this.mCursorHeight = this.mCursorNormal.getIntrinsicHeight();
        this.mCursorHalfWidth = this.mCursorWidth / 2;
        this.mCursorHalfHeight = this.mCursorHeight / 2;
        this.mCursorBorderWidth = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.ae_control_bar_cursor_border_width);
        this.mBarBitmap = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_camera_progress);
        this.mBarBackgroundBitmap = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_camera_progress);
    }

    public void setOnAEControlBarListener(AEControlBarInterface listener) {
        this.mListener = listener;
    }

    public void setDegree(int degree) {
        if (this.mRotationInfo != null) {
            this.mRotationInfo.setDegree(degree, false);
        }
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mLayoutWidth == 0 || this.mLayoutHeight == 0) {
            this.mLayoutWidth = canvas.getWidth();
            this.mLayoutHeight = canvas.getHeight();
            this.mLayoutHalfWidth = this.mLayoutWidth / 2;
            this.mLayoutHalfHeight = this.mLayoutHeight / 2;
        }
        drawBar(canvas);
        setSelected(this.mIsTouchMoving);
        drawCursor(canvas);
    }

    protected void drawBar(Canvas canvas) {
        int i = 255;
        this.mBarBackgroundBitmap.setBounds(this.mLayoutHalfWidth - this.mBarHalfWidth, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mBarHalfWidth, this.mLayoutHalfHeight + this.mBarHalfHeight);
        this.mBarBackgroundBitmap.setAlpha(this.mIsEnabled ? 255 : 89);
        this.mBarBackgroundBitmap.draw(canvas);
        if (this.mIsEnabled && this.mRotationInfo != null) {
            switch (this.mRotationInfo.getCurrentDegree()) {
                case 0:
                case 270:
                    this.mBarBitmap.setBounds(this.mLayoutHalfWidth - this.mBarHalfWidth, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mDiff, this.mLayoutHalfHeight + this.mBarHalfHeight);
                    break;
                case 90:
                case 180:
                    this.mBarBitmap.setBounds(this.mLayoutHalfWidth - this.mDiff, this.mLayoutHalfHeight - this.mBarHalfHeight, this.mLayoutHalfWidth + this.mBarHalfWidth, this.mLayoutHalfHeight + this.mBarHalfHeight);
                    break;
            }
            NinePatchDrawable ninePatchDrawable = this.mBarBitmap;
            if (!this.mIsEnabled) {
                i = 89;
            }
            ninePatchDrawable.setAlpha(i);
            this.mBarBitmap.draw(canvas);
        }
    }

    protected void drawCursor(Canvas canvas) {
        int diff = scaleDownDiff();
        if (this.mRotationInfo != null && (this.mRotationInfo.getCurrentDegree() == 90 || this.mRotationInfo.getCurrentDegree() == 180)) {
            diff = -diff;
        }
        this.mCursorRect.set((this.mLayoutHalfWidth + diff) - this.mCursorHalfWidth, this.mLayoutHalfHeight - this.mCursorHalfHeight, (this.mLayoutHalfWidth + diff) + this.mCursorHalfWidth, this.mLayoutHalfHeight + this.mCursorHalfHeight);
        this.mCursorNormal.setVisible(true, false);
        this.mCursorNormal.setBounds(this.mCursorRect);
        this.mCursorNormal.setColorFilter(this.mIsEnabled ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha());
        this.mCursorNormal.draw(canvas);
    }

    private int scaleDownDiff() {
        return (int) (((float) this.mDiff) * (((float) ((this.mBarWidth - this.mCursorHalfWidth) + (this.mCursorBorderWidth * 2))) / ((float) this.mBarWidth)));
    }

    protected void calculateCurValue() {
        this.mPreValue = this.mCurValue;
        this.mCurValue = this.mDefaultBarValue + (this.mDiff / this.mItemGap);
        if (this.mCurValue < 0) {
            this.mCurValue = 0;
        } else if (this.mCurValue > this.mMaxStep) {
            this.mCurValue = this.mMaxStep;
        }
        if (this.mListener != null && this.mPreValue != this.mCurValue) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
    }

    public int getCurrentValue() {
        return this.mCurValue;
    }

    public boolean doTouchEvent(MotionEvent event) {
        if (!this.mGet.checkModuleValidate(16) && !this.mGet.isMultishotState(7)) {
            return true;
        }
        switch (event.getActionMasked() & 255) {
            case 0:
                return doTouchActionDown(event);
            case 1:
            case 3:
                return doTouchActionUp();
            case 2:
                return doTouchActionMove(event);
            case 6:
                return doTouchActionPointerUp(event);
            default:
                return true;
        }
    }

    protected boolean doTouchActionPointerUp(MotionEvent event) {
        return false;
    }

    protected boolean doTouchActionUp() {
        this.mIsTouchMoving = false;
        this.mBarTouchState = false;
        this.mPrevDiff = this.mDiff;
        if (this.mListener != null) {
            this.mListener.onBarUp();
        }
        invalidate();
        return true;
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        this.mBarTouchState = true;
        int x = (int) event.getX();
        if (this.mRotationInfo != null) {
            if (this.mRotationInfo.getCurrentDegree() == 0 || this.mRotationInfo.getCurrentDegree() == 270) {
                this.mDiff = this.mPrevDiff + (x - this.mStartPosX);
            } else {
                this.mDiff = this.mPrevDiff + (this.mStartPosX - x);
            }
        }
        if (this.mDiff <= (-this.mBarHalfWidth)) {
            this.mDiff = -this.mBarHalfWidth;
        } else if (this.mDiff >= this.mBarHalfWidth) {
            this.mDiff = this.mBarHalfWidth;
        }
        calculateCurValue();
        invalidate();
        return true;
    }

    protected boolean doTouchActionDown(MotionEvent event) {
        this.mIsTouchMoving = true;
        this.mBarTouchState = true;
        this.mStartPosX = (int) event.getX();
        if (this.mListener != null) {
            this.mListener.onBarDown();
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

    public void unbind() {
        this.mBarBitmap = null;
        this.mCursorNormal = null;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    public void updateCursorPositon(int value) {
        this.mCurValue = value;
        if (this.mListener != null) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
    }

    public void refreshControlBar() {
        if (this.mListener != null) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
    }

    public boolean isAeControlBarTouched() {
        return this.mBarTouchState;
    }

    public int getMaxStep() {
        return this.mMaxStep;
    }

    public void initValue() {
        this.mDiff = 0;
        this.mPrevDiff = 0;
        this.mCurValue = this.mDefaultBarValue;
        this.mPreValue = this.mDefaultBarValue;
    }

    public Size getCursorSize() {
        return new Size(this.mCursorWidth, this.mCursorHeight);
    }

    public int getDefaultBarValue() {
        return this.mDefaultBarValue;
    }
}
