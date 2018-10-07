package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.lge.camera.C0088R;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class AudioControlBar extends View implements OnTouchListener {
    protected final float AUDIO_CONTROL_BAR_BIG_STEP_X = 0.001f;
    protected final float AUDIO_CONTROL_BAR_HEIGHT_RATIO = 0.78f;
    protected final float AUDIO_CONTROL_BAR_MARGIN_TOP = 0.1134f;
    protected final float AUDIO_CONTROL_BAR_SMALL_STEP_X = 1.09f;
    protected final float AUDIO_CONTROL_BAR_STEP_TOP = 0.126f;
    protected final float AUDIO_CONTROL_BAR_STEP_Y = 0.0085f;
    protected final float AUDIO_CONTROL_BAR_TEXT_MARGIN = 0.01f;
    protected final float AUDIO_CONTROL_BAR_TEXT_X = 0.007f;
    private final float SMALL_STEP_RATIO = 0.5f;
    protected final int STEP_COUNT = 15;
    protected final float STEP_PAINT_OPACITY = 0.7f;
    protected NinePatchDrawable mBar;
    protected int mBarHalfHeight = 0;
    protected int mBarHalfWidth = 0;
    protected int mBarHeight = 0;
    protected int mBarWidth = 0;
    protected int mBarX = 0;
    protected int mBarY = 0;
    protected int mCenterPositionX = 0;
    protected int mCenterPositionY = 0;
    protected Context mContext = null;
    protected int mCurValue = 0;
    protected Drawable mCursor;
    protected int mCursorHalfHeight = 0;
    protected int mCursorHalfWidth = 0;
    protected Rect mCursorRect = new Rect();
    protected int mDefaultBarValue = 0;
    private int mDefaultValue = 0;
    protected boolean mIsTouchMoving = false;
    protected int mItemGap = 0;
    protected int mLayoutHeight = 0;
    protected int mLayoutWidth = 0;
    public OnAudioControlBarListener mListener = null;
    protected int mMaxStep = 10;
    private int mMaxValue = 0;
    private int mMinValue = 0;
    protected NinePatchDrawable mProgress;
    protected int mScrollPosition = 0;
    protected int mStep1X = 0;
    protected int mStep2X = 0;
    protected Drawable mStepDrawable = null;
    private int mStepInterval = 0;
    protected int mStepTop = 0;
    protected int mStepY = 0;
    protected int mTextMarginY = 0;
    protected Paint mTextPaint = null;
    protected int mTextStepX = 0;
    protected String mTitle = null;
    protected Paint mTitlePaint = null;
    protected String mUnit = null;

    public interface OnAudioControlBarListener {
        void onBarTouchUp(int i);

        void onBarValueChanged(int i);
    }

    public AudioControlBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public AudioControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public AudioControlBar(Context context) {
        super(context);
        this.mContext = context;
    }

    public void init(int min, int max, int defaultValue) {
        setOnTouchListener(this);
        initResources();
        this.mMinValue = min;
        this.mMaxValue = max;
        this.mMaxStep = this.mMaxValue - this.mMinValue;
        this.mItemGap = this.mBarHeight / this.mMaxStep;
        this.mDefaultBarValue = defaultValue;
        this.mScrollPosition += (this.mMaxValue - this.mDefaultBarValue) * this.mItemGap;
    }

    protected void initResources() {
        this.mCursor = getContext().getDrawable(C0088R.drawable.ic_manual_cursor);
        this.mCursorHalfWidth = this.mCursor.getIntrinsicWidth() / 2;
        this.mCursorHalfHeight = this.mCursor.getIntrinsicHeight() / 2;
        this.mBar = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_manual_audio_panel_progress);
        this.mProgress = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.bg_manual_audio_panel_progress_accent);
        this.mStepDrawable = getContext().getDrawable(C0088R.drawable.camera_manual_video_audio_panel_marking);
        this.mBarWidth = Utils.getPx(getContext(), C0088R.dimen.manual_audio_control_bar_width);
        this.mBarHalfWidth = this.mBarWidth / 2;
        float layoutHeight = (float) RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.419f);
        this.mBarHeight = (int) (0.78f * layoutHeight);
        this.mBarHalfHeight = this.mBarHeight / 2;
        this.mBarY = (int) (0.1134f * layoutHeight);
        this.mStepTop = (int) (((float) this.mBarHeight) * 0.04f);
        this.mStepInterval = this.mBarHalfHeight / 15;
        this.mScrollPosition = this.mBarY + this.mStepTop;
    }

    public void init(int min, int max, int defaultValue, String unit) {
        init(min, max, defaultValue);
        this.mUnit = unit;
        this.mTextPaint = new Paint();
        this.mTextPaint.setTextSize((float) Utils.getPx(getContext(), C0088R.dimen.manual_audio_step_textsize));
        this.mTextPaint.setColor(getContext().getColor(C0088R.color.camera_white_txt));
        this.mTextPaint.setAlpha(178);
        this.mTextPaint.setTypeface(Typeface.SANS_SERIF);
        this.mTextPaint.setTextAlign(Align.RIGHT);
    }

    public void init(int min, int max, int defaultValue, String title, String unit) {
        init(min, max, defaultValue, unit);
        this.mTitle = title;
        this.mTitlePaint = new Paint();
        this.mTitlePaint.setTextSize((float) Utils.getPx(getContext(), C0088R.dimen.manual_audio_bar_name_textsize));
        this.mTitlePaint.setColor(getContext().getColor(C0088R.color.camera_white_txt));
        this.mTitlePaint.setTypeface(Typeface.SANS_SERIF);
        this.mTitlePaint.setTextAlign(Align.CENTER);
    }

    public void setOnAudioControlBarListener(OnAudioControlBarListener listener) {
        this.mListener = listener;
    }

    protected void onDraw(Canvas canvas) {
        this.mLayoutWidth = canvas.getWidth();
        this.mLayoutHeight = canvas.getHeight();
        this.mCenterPositionX = this.mLayoutWidth / 2;
        this.mBarX = (this.mLayoutWidth / 5) * 3;
        drawBar(canvas);
        setSelected(this.mIsTouchMoving);
        setBarStepStartPos();
        drawCursor(canvas);
        drawStep(canvas);
        drawTitle(canvas);
    }

    protected void drawTitle(Canvas canvas) {
        if (this.mTitle != null && this.mTitlePaint != null) {
            canvas.drawText(this.mTitle, (float) (this.mBarX + this.mBarHalfWidth), (float) (((double) this.mLayoutHeight) * 0.99d), this.mTitlePaint);
        }
    }

    protected void drawBar(Canvas canvas) {
        if (this.mBar != null && this.mProgress != null) {
            this.mBar.setBounds(this.mBarX, this.mBarY, this.mBarX + this.mBarWidth, this.mBarY + this.mBarHeight);
            this.mBar.draw(canvas);
            if (isEnabled()) {
                this.mProgress.setBounds(this.mBarX, this.mScrollPosition, this.mBarX + this.mBarWidth, this.mBarY + this.mBarHeight);
                this.mProgress.draw(canvas);
            }
        }
    }

    protected void drawCursor(Canvas canvas) {
        if (this.mCursorRect != null && this.mCursor != null) {
            this.mCursorRect.set((this.mBarX + this.mBarHalfWidth) - this.mCursorHalfWidth, this.mScrollPosition - this.mCursorHalfHeight, (this.mBarX + this.mBarHalfWidth) + this.mCursorHalfWidth, this.mScrollPosition + this.mCursorHalfHeight);
            this.mCursor.setVisible(true, false);
            this.mCursor.setBounds(this.mCursorRect);
            this.mCursor.draw(canvas);
        }
    }

    protected void setBarStepStartPos() {
        if (this.mStepDrawable != null) {
            this.mStep1X = (this.mBarX - this.mStepDrawable.getIntrinsicWidth()) - RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.001f);
            this.mStep2X = (this.mBarX + this.mBarWidth) + RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.001f);
            this.mStepY = this.mBarY + this.mStepTop;
            this.mTextStepX = this.mStep1X - RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.007f);
            this.mTextMarginY = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.01f);
        }
    }

    protected void drawStep(Canvas canvas) {
        if (this.mStepDrawable != null) {
            int i;
            int bigStepInterval = this.mBarHalfHeight - this.mStepTop;
            for (i = 0; i < 3; i++) {
                this.mStepDrawable.setBounds(this.mStep1X, this.mStepY + (bigStepInterval * i), this.mStep1X + this.mStepDrawable.getIntrinsicWidth(), (this.mStepY + (bigStepInterval * i)) + this.mStepDrawable.getIntrinsicHeight());
                this.mStepDrawable.draw(canvas);
                this.mStepDrawable.setBounds(this.mStep2X, this.mStepY + (bigStepInterval * i), this.mStep2X + this.mStepDrawable.getIntrinsicWidth(), (this.mStepY + (bigStepInterval * i)) + this.mStepDrawable.getIntrinsicHeight());
                this.mStepDrawable.draw(canvas);
            }
            int smallStep1X = this.mStep1X + ((int) (((float) this.mStepDrawable.getIntrinsicWidth()) - (((float) this.mStepDrawable.getIntrinsicWidth()) * 0.5f)));
            for (i = 1; i < 15; i++) {
                this.mStepDrawable.setBounds(smallStep1X, (this.mStepY + (this.mStepInterval * i)) - this.mStepDrawable.getIntrinsicHeight(), ((int) (((float) this.mStepDrawable.getIntrinsicWidth()) * 0.5f)) + smallStep1X, this.mStepY + (this.mStepInterval * i));
                this.mStepDrawable.draw(canvas);
                this.mStepDrawable.setBounds(this.mStep2X, (this.mStepY + (this.mStepInterval * i)) - this.mStepDrawable.getIntrinsicHeight(), this.mStep2X + ((int) (((float) this.mStepDrawable.getIntrinsicWidth()) * 0.5f)), this.mStepY + (this.mStepInterval * i));
                this.mStepDrawable.draw(canvas);
                this.mStepDrawable.setBounds(smallStep1X, ((this.mBarY + this.mBarHalfHeight) + (this.mStepInterval * i)) - this.mStepDrawable.getIntrinsicHeight(), ((int) (((float) this.mStepDrawable.getIntrinsicWidth()) * 0.5f)) + smallStep1X, (this.mBarY + this.mBarHalfHeight) + (this.mStepInterval * i));
                this.mStepDrawable.draw(canvas);
                this.mStepDrawable.setBounds(this.mStep2X, ((this.mBarY + this.mBarHalfHeight) + (this.mStepInterval * i)) - this.mStepDrawable.getIntrinsicHeight(), this.mStep2X + ((int) (((float) this.mStepDrawable.getIntrinsicWidth()) * 0.5f)), (this.mBarY + this.mBarHalfHeight) + (this.mStepInterval * i));
                this.mStepDrawable.draw(canvas);
            }
            int bigStepGap = (this.mBarHeight - (this.mStepTop * 2)) / 2;
            for (i = 0; i < this.mMaxStep + 1; i++) {
                String stepString = "";
                if (i == 0) {
                    stepString = this.mMinValue + this.mUnit;
                } else if (i == 1) {
                    stepString = this.mDefaultValue + this.mUnit;
                } else if (i == 2) {
                    stepString = this.mMaxValue + this.mUnit;
                }
                canvas.drawText(stepString, (float) this.mTextStepX, (float) ((this.mStepY + (bigStepGap * i)) + this.mTextMarginY), this.mTextPaint);
            }
        }
    }

    protected void calculateCurValue() {
        this.mCurValue = (int) (((float) this.mMaxValue) - (((float) this.mMaxStep) * Math.max(Math.min(1.0f, ((float) ((this.mScrollPosition - this.mStepTop) - this.mBarY)) / ((float) (this.mBarHeight - (this.mStepTop * 2)))), 0.0f)));
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
        if (this.mScrollPosition < this.mBarY + this.mStepTop) {
            this.mScrollPosition = this.mBarY + this.mStepTop;
        }
        if (this.mScrollPosition > (this.mBarY + this.mBarHeight) - this.mStepTop) {
            this.mScrollPosition = (this.mBarY + this.mBarHeight) - this.mStepTop;
        }
        if (this.mListener != null) {
            this.mListener.onBarTouchUp(this.mCurValue);
        }
        invalidate();
        return true;
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        this.mScrollPosition = (int) event.getY();
        adjustScrollPosition();
        calculateCurValue();
        if (this.mListener != null) {
            this.mListener.onBarValueChanged(this.mCurValue);
        }
        invalidate();
        return true;
    }

    protected boolean doTouchActionDown(MotionEvent event) {
        this.mIsTouchMoving = true;
        this.mScrollPosition = (int) event.getY();
        adjustScrollPosition();
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
        if (this.mScrollPosition < this.mBarY) {
            this.mScrollPosition = this.mBarY;
        }
        if (this.mScrollPosition > (this.mBarY + this.mBarHeight) - this.mStepTop) {
            this.mScrollPosition = (this.mBarY + this.mBarHeight) - this.mStepTop;
        }
    }

    public void unbind() {
        this.mBar = null;
        this.mProgress = null;
        this.mCursor = null;
        this.mCursorRect = null;
        this.mTitlePaint = null;
    }

    public void updateCursorPositon(int value) {
        if (value >= this.mMinValue && value <= this.mMaxValue) {
            this.mScrollPosition = (this.mBarY + this.mStepTop) + ((this.mMaxValue - value) * this.mItemGap);
            this.mCurValue = value;
            invalidate();
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : 0.35f);
    }
}
