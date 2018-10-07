package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import com.lge.camera.C0088R;
import com.lge.camera.util.RatioCalcUtil;

public class AudioBalanceController extends AudioControlBar {
    private static final int OVAL_BOTTOM = 3;
    private static final int OVAL_LEFT = 0;
    private static final int OVAL_RIGHT = 2;
    private static final int OVAL_TOP = 1;
    public int OVAL_COUNT = 1;
    private Drawable mBarBitmap = null;
    private int mBarCenterPositionY = 0;
    private Drawable mBarDisableBitmap = null;
    private int mBasePositionLeft = 0;
    private int mBasePositionRight = 0;
    private int mBaseRadius = 150;
    private GradientDrawable mBottomGradiantOval;
    private OvalInfo mBottomOval;
    public int mOvalGapLeftAndRight = 0;
    public int mOvalGapUpAndDown = 0;
    private int mOvalHeight = 0;
    private int mOvalWidth = 0;
    private GradientDrawable mTopGradiantOval;
    private OvalInfo mTopOval;

    class OvalInfo {
        private OvalInfo mBackupOvalInfo;
        public int mBottomPosition;
        public int mLeftPosition;
        private Rect mOvalRectF;
        public int mRightPosition;
        public int mTopPosition;

        public OvalInfo(int left, int top, int right, int bottom) {
            this.mLeftPosition = left;
            this.mTopPosition = top;
            this.mRightPosition = right;
            this.mBottomPosition = bottom;
        }

        private void init() {
            this.mBackupOvalInfo = new OvalInfo(this.mLeftPosition, this.mTopPosition, this.mRightPosition, this.mBottomPosition);
            this.mOvalRectF = new Rect();
        }

        public void setOvalSize(int left, int top, int right, int bottom) {
            this.mLeftPosition = left;
            this.mTopPosition = top;
            this.mRightPosition = right;
            this.mBottomPosition = bottom;
        }

        public void updateOvalInfo() {
            this.mBackupOvalInfo.mLeftPosition = this.mLeftPosition;
            this.mBackupOvalInfo.mTopPosition = this.mTopPosition;
            this.mBackupOvalInfo.mRightPosition = this.mRightPosition;
            this.mBackupOvalInfo.mBottomPosition = this.mBottomPosition;
        }

        public OvalInfo getBackupOvalInfo() {
            return this.mBackupOvalInfo;
        }

        public Rect getOvalRectF() {
            this.mOvalRectF.set(this.mLeftPosition, this.mTopPosition, this.mRightPosition, this.mBottomPosition);
            return this.mOvalRectF;
        }
    }

    public AudioBalanceController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AudioBalanceController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioBalanceController(Context context) {
        super(context);
    }

    public void init(int min, int max, int defaultValue) {
        super.init(min, max, defaultValue);
        this.mBasePositionLeft = 0;
        this.mBasePositionRight = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.094f);
        this.mBarCenterPositionY = this.mBarHalfHeight;
        this.mTopOval = new OvalInfo(this.mBasePositionLeft, this.mBarCenterPositionY - this.mBaseRadius, this.mBasePositionRight, this.mBarCenterPositionY);
        this.mBottomOval = new OvalInfo(this.mBasePositionLeft, this.mBarCenterPositionY, this.mBasePositionRight, this.mBaseRadius + this.mBarCenterPositionY);
        this.mTopOval.init();
        this.mBottomOval.init();
    }

    public void initResources() {
        this.mCursor = getContext().getDrawable(C0088R.drawable.camera_manual_video_audio_panel_control_marking);
        this.mCursorHalfWidth = (int) ((((float) this.mCursor.getIntrinsicWidth()) / 2.0f) + 0.5f);
        this.mBarBitmap = getContext().getDrawable(C0088R.drawable.camera_manual_video_audio_panel_bar_dot);
        this.mBarDisableBitmap = getContext().getDrawable(C0088R.drawable.camera_video_bar_dot_disabled);
        this.mBarWidth = this.mBarBitmap.getIntrinsicWidth();
        this.mBarHeight = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.377f);
        this.mBarHalfWidth = this.mBarWidth / 2;
        this.mBarHalfHeight = this.mBarHeight / 2;
        this.mTopGradiantOval = (GradientDrawable) getContext().getDrawable(C0088R.drawable.manual_audio_balance_gradiant);
        this.mBottomGradiantOval = (GradientDrawable) getContext().getDrawable(C0088R.drawable.manual_audio_balance_gradiant);
        this.mScrollPosition = 0;
    }

    protected void onDraw(Canvas canvas) {
        float topMovingRatio;
        this.mCenterPositionX = canvas.getWidth() / 2;
        this.mCenterPositionY = canvas.getHeight() / 2;
        this.mOvalHeight = this.mBarHalfHeight;
        this.mOvalWidth = this.mCenterPositionX;
        drawBar(canvas);
        float movingRatio = ((((float) this.mScrollPosition) / ((float) this.mBarHalfHeight)) - 1.0f) / 2.0f;
        if (movingRatio > 0.0f) {
            topMovingRatio = movingRatio / 2.0f;
        } else {
            topMovingRatio = movingRatio;
        }
        float bottomMovingRatio = movingRatio > 0.0f ? movingRatio : movingRatio / 2.0f;
        if (this.mTopOval != null && this.mBottomOval != null) {
            this.mTopOval.setOvalSize(checkTopOvalSize(0, topMovingRatio), checkTopOvalSize(1, topMovingRatio), checkTopOvalSize(2, topMovingRatio), this.mBarCenterPositionY);
            this.mBottomOval.setOvalSize(checkBottomOvalSize(0, bottomMovingRatio), this.mBarCenterPositionY, checkBottomOvalSize(2, bottomMovingRatio), checkBottomOvalSize(3, bottomMovingRatio));
            if (isEnabled()) {
                this.mTopGradiantOval.setBounds(this.mTopOval.getOvalRectF());
                this.mTopGradiantOval.setOrientation(Orientation.BOTTOM_TOP);
                this.mTopGradiantOval.draw(canvas);
                this.mBottomGradiantOval.setBounds(this.mBottomOval.getOvalRectF());
                this.mBottomGradiantOval.setOrientation(Orientation.TOP_BOTTOM);
                this.mBottomGradiantOval.draw(canvas);
            }
            drawCursor(canvas);
            calculateCurValue();
        }
    }

    protected void drawCursor(Canvas canvas) {
        this.mCursorRect.set(this.mCenterPositionX - this.mCursorHalfWidth, this.mCenterPositionY, this.mCenterPositionX + this.mCursorHalfWidth, this.mCenterPositionY);
        this.mCursor.setBounds(this.mCursorRect);
        this.mCursor.draw(canvas);
    }

    protected void drawBar(Canvas canvas) {
        if (this.mBarBitmap != null) {
            if (isEnabled()) {
                this.mBarBitmap.setBounds(this.mCenterPositionX - this.mBarHalfWidth, 0, this.mCenterPositionX + this.mBarHalfWidth, this.mBarHeight);
                this.mBarBitmap.draw(canvas);
                return;
            }
            this.mBarDisableBitmap.setBounds(this.mCenterPositionX - this.mBarHalfWidth, 0, this.mCenterPositionX + this.mBarHalfWidth, this.mBarHeight);
            this.mBarDisableBitmap.draw(canvas);
        }
    }

    private int checkTopOvalSize(int direction, float movingRatio) {
        int value;
        switch (direction) {
            case 0:
                value = (this.mOvalWidth + ((int) (((float) this.mOvalWidth) * movingRatio))) - (this.mOvalWidth / 2);
                if (value <= this.mBasePositionLeft) {
                    return this.mBasePositionLeft;
                }
                return value;
            case 1:
                return (this.mOvalHeight + ((int) (((float) this.mOvalHeight) * movingRatio))) - (this.mOvalHeight / 2);
            case 2:
                value = (this.mOvalWidth - ((int) (((float) this.mOvalWidth) * movingRatio))) + (this.mOvalWidth / 2);
                if (value >= this.mBasePositionRight) {
                    return this.mBasePositionRight;
                }
                return value;
            default:
                return 0;
        }
    }

    private int checkBottomOvalSize(int direction, float movingRatio) {
        int value;
        switch (direction) {
            case 0:
                value = (this.mOvalWidth / 2) - ((int) (((float) this.mOvalWidth) * movingRatio));
                if (value <= this.mBasePositionLeft) {
                    return this.mBasePositionLeft;
                }
                return value;
            case 2:
                value = (this.mOvalWidth + ((int) (((float) this.mOvalWidth) * movingRatio))) + (this.mOvalWidth / 2);
                if (value >= this.mBasePositionRight) {
                    return this.mBasePositionRight;
                }
                return value;
            case 3:
                return (this.mOvalHeight + ((int) (((float) this.mOvalHeight) * movingRatio))) + (this.mOvalHeight / 2);
            default:
                return 0;
        }
    }

    protected boolean doTouchActionUp() {
        super.doTouchActionUp();
        releaseTouchUp();
        this.mTopOval.updateOvalInfo();
        this.mBottomOval.updateOvalInfo();
        return true;
    }

    public void releaseTouchUp() {
        if (this.mScrollPosition < this.mBarCenterPositionY + this.mItemGap && this.mScrollPosition > this.mBarCenterPositionY - this.mItemGap) {
            this.mScrollPosition = this.mBarCenterPositionY;
            invalidate();
        }
    }

    public void unbind() {
        this.mBarBitmap = null;
        this.mBarDisableBitmap = null;
        this.mCursor = null;
        this.mTopGradiantOval = null;
        this.mBottomGradiantOval = null;
    }
}
