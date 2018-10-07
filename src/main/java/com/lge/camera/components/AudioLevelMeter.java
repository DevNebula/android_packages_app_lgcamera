package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class AudioLevelMeter extends View {
    private static final int BACKGROUND_COLOR = Color.argb(0, 0, 0, 0);
    private static final int HIGH_COLOR = Color.rgb(233, 31, 31);
    private static final int LEVEL_METER_TYPE_LANDSCAPE = 1;
    private static final int LEVEL_METER_TYPE_PORTRAIT = 0;
    private static final int LOW_COLOR = Color.rgb(91, 208, 91);
    private static final int MAX_LEVEL_METER_CNT = 10;
    private static final int MID_COLOR = Color.rgb(245, 203, 58);
    private int HIGH_START_POSITION = 151;
    private int LEVEL_METER_WIDTH = 168;
    private int MID_START_POSITION = 100;
    private final float mConvertConstant = 0.78740156f;
    private int mCurrentLevel;
    private GradientDrawable[] mDrawableList = null;
    private boolean mIsGageType = false;
    private int mLevelBoxGap = 0;
    private int mLevelBoxHeight = 0;
    private int mLevelBoxWidth = 0;
    private int mLevelMeterType = 1;
    private Paint mLevelPaint = new Paint();
    private Rect mLevelRect = null;

    public AudioLevelMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AudioLevelMeter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioLevelMeter(Context context) {
        super(context);
    }

    public void init() {
        this.LEVEL_METER_WIDTH = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), true, 0.037f);
        this.MID_START_POSITION = (int) (((double) this.LEVEL_METER_WIDTH) * 0.6d);
        this.HIGH_START_POSITION = (int) (((double) this.LEVEL_METER_WIDTH) * 0.9d);
        if (this.mLevelMeterType == 0) {
            this.mLevelBoxGap = (int) Utils.dpToPx(getContext(), 0.5f);
            this.mLevelBoxWidth = (int) Utils.dpToPx(getContext(), 4.0f);
            this.mLevelBoxHeight = (int) Utils.dpToPx(getContext(), 2.5f);
        } else {
            this.mLevelBoxGap = (int) Utils.dpToPx(getContext(), 0.25f);
            this.mLevelBoxHeight = (int) Utils.dpToPx(getContext(), 11.0f);
            this.mLevelBoxWidth = (this.LEVEL_METER_WIDTH - (this.mLevelBoxGap * 9)) / 10;
        }
        this.mLevelRect = new Rect();
        makeGradientList();
    }

    private void makeGradientList() {
        int i;
        GradientDrawable drawable;
        int startColor;
        int endColor;
        this.mDrawableList = new GradientDrawable[10];
        int stepR = 251 / 5;
        int stepG = 33 / 5;
        int stepB = -189 / 5;
        int currentR = 4;
        int currentG = 219;
        int currentB = 189;
        for (i = 0; i < 5; i++) {
            drawable = new GradientDrawable();
            startColor = Color.rgb(currentR, currentG, currentB);
            currentR += 50;
            currentG += 6;
            currentB -= 37;
            endColor = Color.rgb(currentR, currentG, currentB);
            drawable.setColors(new int[]{startColor, endColor});
            this.mDrawableList[i] = drawable;
        }
        stepR = 0 / 5;
        stepG = -156 / 5;
        stepB = 0 / 5;
        for (i = 5; i < 10; i++) {
            drawable = new GradientDrawable();
            startColor = Color.rgb(currentR, currentG, currentB);
            currentR += 0;
            currentG -= 31;
            currentB += 0;
            endColor = Color.rgb(currentR, currentG, currentB);
            drawable.setColors(new int[]{startColor, endColor});
            this.mDrawableList[i] = drawable;
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mLevelMeterType == 0) {
            drawLevelMeterPortraitType(canvas);
            return;
        }
        int drawStartPosition = 0;
        if (this.mIsGageType) {
            drawLevelMeterContinuousType(canvas);
            return;
        }
        for (int i = 0; i < this.mCurrentLevel && this.mDrawableList != null && this.mDrawableList.length >= this.mCurrentLevel - 1; i++) {
            this.mDrawableList[i].setBounds(drawStartPosition, 0, this.mLevelBoxWidth + drawStartPosition, this.mLevelBoxHeight);
            this.mDrawableList[i].draw(canvas);
            drawStartPosition += this.mLevelBoxWidth + this.mLevelBoxGap;
        }
    }

    protected void drawLevelMeterPortraitType(Canvas canvas) {
        int drawStartPosition = 0;
        int reverseLevel = 10 - this.mCurrentLevel;
        for (int i = 0; i < 10; i++) {
            if (i < reverseLevel) {
                this.mLevelPaint.setColor(BACKGROUND_COLOR);
            } else if (i < 1) {
                this.mLevelPaint.setColor(HIGH_COLOR);
            } else if (i < 4) {
                this.mLevelPaint.setColor(MID_COLOR);
            } else if (this.mCurrentLevel != 0) {
                this.mLevelPaint.setColor(LOW_COLOR);
            }
            this.mLevelRect.set(0, drawStartPosition, this.mLevelBoxWidth, this.mLevelBoxHeight + drawStartPosition);
            canvas.drawRect(this.mLevelRect, this.mLevelPaint);
            drawStartPosition += this.mLevelBoxHeight + this.mLevelBoxGap;
        }
    }

    protected void drawLevelMeterContinuousType(Canvas canvas) {
        this.mLevelPaint.setColor(LOW_COLOR);
        int width = (int) (((float) this.LEVEL_METER_WIDTH) * (((float) this.mCurrentLevel) / 127.0f));
        int remainWidth = width;
        if (width > this.HIGH_START_POSITION) {
            this.mLevelPaint.setColor(HIGH_COLOR);
            this.mLevelRect.set(this.HIGH_START_POSITION, 0, width, this.mLevelBoxHeight);
            canvas.drawRect(this.mLevelRect, this.mLevelPaint);
            remainWidth = this.HIGH_START_POSITION;
        }
        if (width > this.MID_START_POSITION) {
            this.mLevelPaint.setColor(MID_COLOR);
            this.mLevelRect.set(this.MID_START_POSITION, 0, remainWidth, this.mLevelBoxHeight);
            canvas.drawRect(this.mLevelRect, this.mLevelPaint);
            remainWidth = this.MID_START_POSITION;
        }
        this.mLevelPaint.setColor(LOW_COLOR);
        this.mLevelRect.set(0, 0, remainWidth, this.mLevelBoxHeight);
        canvas.drawRect(this.mLevelRect, this.mLevelPaint);
    }

    public void updateAudioLevel(int level) {
        if (this.mIsGageType) {
            this.mCurrentLevel = level;
        } else {
            this.mCurrentLevel = ((int) (0.78740156f * ((float) level))) / 10;
        }
        invalidate();
    }

    public void unbind() {
        if (this.mDrawableList != null) {
            this.mDrawableList = null;
        }
    }
}
