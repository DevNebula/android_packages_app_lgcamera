package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class SwitchableBar extends View implements OnTouchListener {
    private static final int BLOCK_JITTER = 6;
    private static final float FAST_MOVE = 20.0f;
    private static final int FAST_SPAN = 40;
    private static final int SLOW_STEP = 3;
    private static final int SWITCHING_AFTER_TRY_MAX_CNT_LOWER_BAR = 3;
    private static final int SWITCHING_TRY_MAX_CNT_LOWER_BAR = 5;
    private static final int SWITCHING_TRY_MAX_CNT_UPPER_BAR = 5;
    private static final int SWITCH_AREA_THRESHOLD = 10;
    public static final int ZOOM_LEVEL_UNAVAILABLE = -1;
    private static final boolean mRotateNeeded = true;
    protected static final String sInAndOutTag = "[INO_Debugging] ";
    private float mAverageDistance = 0.0f;
    private Paint mBackgroundPaint = new Paint(1);
    protected int mBarBottom = 0;
    protected int mBarEnd = 0;
    private int mBarHeight = 0;
    protected int mBarPaddingEnd = 0;
    protected int mBarStart = 0;
    protected int mBarTop = 0;
    private int mBarWidth = 0;
    private Bitmap mBorderBitmap = null;
    protected int mBorderCenter = 0;
    private Paint mBorderPaint = new Paint(1);
    private int mBorderStart = 0;
    protected int mBorderTop = 0;
    private Bitmap mBottomIconBitmap = null;
    private Paint mBottomIconPaint = new Paint(1);
    protected int mCurrentBarHeight = 0;
    protected int mCursorCenterX = 0;
    protected float mCursorCenterY = 0.0f;
    private Paint mCursorPaint = new Paint(1);
    protected int mCursorRadius = 0;
    private Paint mForegroundPaint = new Paint(1);
    private int mIconHeight = 0;
    private int mIconTopBottomPadding = 0;
    private int mIconWidth = 0;
    protected boolean mIsBarTouched = false;
    private boolean mIsDrawingExceedsLevel = false;
    protected boolean mIsMovable = true;
    protected boolean mIsSwitching = false;
    protected boolean mIsUpperBarMode = true;
    private int mLowerBarHeight = 0;
    protected int mMaxValue = 0;
    protected int mMoveStep = 20;
    private int mMovingCnt = 0;
    private boolean mNextTime = false;
    private float mPrevYPos = 0.0f;
    protected boolean mPreventChangeCamera = false;
    protected RotationInfo mRotationInfo = new RotationInfo();
    protected float mSTEP_UNIT = 0.856f;
    protected InAndOutZoomBarInterface mSwitchableBarInterface = null;
    private int mSwitchingAfterTryCnt = 0;
    protected int mSwitchingAreaMargin = 0;
    protected int mSwitchingAreaTop = 0;
    private int mSwitchingTryCnt = 0;
    protected float mToBeCursor = 0.0f;
    protected int mToBeValue = 0;
    private Bitmap mTopIconBitmap = null;
    private Paint mTopIconPaint = new Paint(1);
    private float mTotalDistance = 0.0f;
    private int mTotalPaddingEnd = 0;
    private int mTotalPaddingTop = 0;
    private int mTotalWidth = 0;
    protected int mTouchableAreaBottom = 0;
    protected int mTouchableAreaEnd = 0;
    protected int mTouchableAreaMargin = 0;
    protected int mTouchableAreaStart = 0;
    protected int mTouchableAreaTop = 0;
    protected int mValue = 0;

    public SwitchableBar(Context arg0) {
        super(arg0);
        init();
    }

    public SwitchableBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchableBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setInterface(InAndOutZoomBarInterface intface) {
        this.mSwitchableBarInterface = intface;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!(this.mRotationInfo == null || this.mRotationInfo.getCurrentDegree() == this.mRotationInfo.getTargetDegree() || !this.mRotationInfo.calcCurrentDegree())) {
            invalidate();
        }
        drawBackground(canvas);
        drawIcons(canvas);
        drawBorder(canvas);
        drawCursor(canvas);
    }

    public void setDegree(int degree) {
        boolean useAnimation = getVisibility() == 0;
        if (this.mRotationInfo != null) {
            this.mRotationInfo.setDegree(degree, useAnimation);
        }
        invalidate();
    }

    public void notifySwitchingFinished() {
        this.mSwitchingTryCnt = 0;
        this.mSwitchingAfterTryCnt = 0;
        this.mIsSwitching = false;
        CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Switching camera finished : " + this.mIsDrawingExceedsLevel);
        if (this.mIsDrawingExceedsLevel && !FunctionProperties.isSupportedOpticZoom()) {
            calcultateZoomValueByCursorPosition(this.mToBeCursor);
            this.mSwitchableBarInterface.drawExceedsLevel();
        }
        this.mMovingCnt = 0;
        this.mAverageDistance = 0.0f;
        this.mTotalDistance = 0.0f;
    }

    public boolean isSwitching() {
        return this.mIsSwitching;
    }

    protected boolean isInTouchableArea(float x, float y) {
        if (Float.compare(x, (float) this.mTouchableAreaStart) < 0 || Float.compare(x, (float) this.mTouchableAreaEnd) > 0 || Float.compare(y, (float) this.mTouchableAreaTop) < 0 || Float.compare(y, (float) this.mTouchableAreaBottom) > 0) {
            return false;
        }
        return true;
    }

    protected boolean isCursorInSlowMovingArea() {
        float slowMoveAreaTop = this.mIsUpperBarMode ? (float) (this.mBorderCenter - (this.mCursorRadius / 4)) : (float) this.mBorderCenter;
        float slowMoveAreaBottom = this.mIsUpperBarMode ? (float) this.mBorderCenter : (float) (this.mBorderCenter + (this.mCursorRadius / 4));
        if (Float.compare(this.mCursorCenterY, slowMoveAreaTop) < 0 || Float.compare(this.mCursorCenterY, slowMoveAreaBottom) > 0) {
            return false;
        }
        return true;
    }

    private boolean isCursorInSwitchingArea() {
        float switchingAreaTop = this.mIsUpperBarMode ? (float) this.mSwitchingAreaTop : (float) this.mBarTop;
        float switchingAreaBottom = this.mIsUpperBarMode ? (float) this.mBarBottom : (float) this.mBorderCenter;
        if (Float.compare(this.mCursorCenterY, switchingAreaTop) < 0 || Float.compare(this.mCursorCenterY, switchingAreaBottom) > 0) {
            return false;
        }
        return true;
    }

    private void init() {
        setOnTouchListener(this);
        initConstants();
    }

    private void initConstants() {
        this.mTotalWidth = Utils.getPx(getContext(), C0088R.dimen.in_and_out_zoom_width);
        this.mBarWidth = (int) Utils.dpToPx(getContext(), 3.0f);
        this.mBarHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), false, 0.53f);
        this.mLowerBarHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), false, 0.166f);
        Drawable topIcon = getContext().getDrawable(C0088R.drawable.camera_preview_bar_plus);
        if (topIcon != null) {
            this.mIconWidth = topIcon.getIntrinsicWidth();
            this.mIconHeight = topIcon.getIntrinsicHeight();
        }
        this.mBackgroundPaint.setColor(Color.argb(255, 255, 255, 255));
        this.mForegroundPaint.setColor(Color.argb(255, 255, 255, 255));
        this.mCursorPaint.setColor(Color.argb(255, 255, 255, 255));
        this.mIconTopBottomPadding = (int) Utils.dpToPx(getContext(), 4.0f);
        this.mBarPaddingEnd = (this.mIconWidth - this.mBarWidth) / 2;
        this.mTotalPaddingTop = (((Utils.getLCDsize(getContext(), false)[1] - this.mBarHeight) - (this.mIconHeight * 2)) - (this.mIconTopBottomPadding * 2)) / 2;
        if (ModelProperties.isLongLCDModel()) {
            this.mTotalPaddingEnd = this.mTotalWidth - RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), true, 0.274f);
        } else {
            this.mTotalPaddingEnd = this.mTotalWidth - ((int) Utils.dpToPx(getContext(), 93.0f));
        }
        this.mBarStart = (this.mTotalPaddingEnd - this.mBarWidth) - this.mBarPaddingEnd;
        this.mBarEnd = this.mTotalPaddingEnd - this.mBarPaddingEnd;
        this.mBarTop = (this.mTotalPaddingTop + this.mIconHeight) + this.mIconTopBottomPadding;
        this.mBarBottom = this.mBarTop + this.mBarHeight;
        this.mTouchableAreaMargin = (int) Utils.dpToPx(getContext(), 10.0f);
        this.mTouchableAreaStart = this.mBarStart - (this.mTouchableAreaMargin * 3);
        this.mTouchableAreaEnd = this.mBarEnd + (this.mTouchableAreaMargin * 3);
        this.mCursorRadius = (int) Utils.dpToPx(getContext(), 10.0f);
        Drawable borderIcon = getContext().getDrawable(C0088R.drawable.bg_camera_inout_progress);
        int borderWidth = borderIcon.getIntrinsicWidth();
        int borderHeight = borderIcon.getIntrinsicHeight();
        this.mBorderStart = this.mBarStart - ((borderWidth / 2) - (this.mBarWidth / 2));
        this.mBorderTop = this.mBarBottom - this.mLowerBarHeight;
        this.mBorderCenter = this.mBorderTop + (borderHeight / 2);
        this.mCursorCenterX = this.mBarStart + (this.mBarWidth / 2);
        this.mCursorCenterY = (float) this.mBorderCenter;
        this.mSwitchingAreaMargin = (int) Utils.dpToPx(getContext(), 1.0f);
        this.mSwitchingAreaTop = this.mBorderTop - this.mSwitchingAreaMargin;
        calculateNecessaryValues();
        setBarIcons(C0088R.drawable.camera_preview_bar_plus, C0088R.drawable.camera_preview_bar_minus);
        setBorderIcon(C0088R.drawable.bg_camera_inout_progress);
    }

    public void setBarIcons(int topIconResId, int bottomIconResId) {
        if (this.mTopIconBitmap == null) {
            this.mTopIconBitmap = BitmapManagingUtil.getBitmap(this.mContext, topIconResId);
        }
        if (this.mBottomIconBitmap == null) {
            this.mBottomIconBitmap = BitmapManagingUtil.getBitmap(this.mContext, bottomIconResId);
        }
    }

    public void setBorderIcon(int cursorIconResId) {
        if (this.mBorderBitmap == null) {
            this.mBorderBitmap = BitmapManagingUtil.getBitmap(this.mContext, cursorIconResId);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRect(new Rect(this.mBarStart, this.mBarTop, this.mBarEnd, this.mBarBottom), this.mBackgroundPaint);
        canvas.drawRect(new Rect(this.mBarStart, (int) this.mCursorCenterY, this.mBarEnd, this.mIsUpperBarMode ? this.mBarBottom : this.mBarBottom), this.mForegroundPaint);
    }

    private void drawBorder(Canvas canvas) {
        canvas.drawBitmap(this.mBorderBitmap, (float) this.mBorderStart, (float) this.mBorderTop, this.mBorderPaint);
    }

    private void drawCursor(Canvas canvas) {
        canvas.drawCircle((float) this.mCursorCenterX, this.mCursorCenterY, (float) this.mCursorRadius, this.mCursorPaint);
    }

    private void drawIcons(Canvas canvas) {
        int topIconTopMargin = this.mTotalPaddingTop;
        int topIconEndMargin = this.mTotalPaddingEnd - this.mIconWidth;
        int bottomIconTopMargin = ((this.mTotalPaddingTop + this.mIconHeight) + (this.mIconTopBottomPadding * 2)) + this.mBarHeight;
        int rotDegree = 0;
        if (this.mRotationInfo != null) {
            rotDegree = -this.mRotationInfo.getCurrentDegree();
        }
        int bitmapIconHalfWidth = this.mIconWidth / 2;
        canvas.rotate((float) rotDegree, (float) (topIconEndMargin + bitmapIconHalfWidth), (float) (topIconTopMargin + bitmapIconHalfWidth));
        canvas.drawBitmap(this.mTopIconBitmap, (float) topIconEndMargin, (float) topIconTopMargin, this.mTopIconPaint);
        canvas.rotate((float) (-rotDegree), (float) (topIconEndMargin + bitmapIconHalfWidth), (float) (topIconTopMargin + bitmapIconHalfWidth));
        canvas.rotate((float) (rotDegree + 90), (float) (topIconEndMargin + bitmapIconHalfWidth), (float) (bottomIconTopMargin + bitmapIconHalfWidth));
        canvas.drawBitmap(this.mBottomIconBitmap, (float) topIconEndMargin, (float) bottomIconTopMargin, this.mBottomIconPaint);
        canvas.rotate((float) ((-rotDegree) - 90), (float) (topIconEndMargin + bitmapIconHalfWidth), (float) (bottomIconTopMargin + bitmapIconHalfWidth));
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
        if (this.mMaxValue <= 0) {
            this.mMaxValue = 0;
        }
        calculateNecessaryValues();
    }

    public void setBarValue(int value) {
        this.mValue = value;
        CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] mValue = " + this.mValue);
        calculateCursorPosition(this.mValue, true);
        invalidate();
    }

    protected boolean checkGestureZoomAvailable(int step, int gapSpan, boolean isGestureGoing) {
        if (this.mIsSwitching) {
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Camera's switching, return");
            return false;
        } else if (this.mSwitchingAfterTryCnt >= 3) {
            return true;
        } else {
            this.mSwitchingAfterTryCnt++;
            return false;
        }
    }

    public int setBarCursorByGestureStep(int step, int gapSpan, boolean isScaleEnd) {
        boolean z = true;
        this.mIsMovable = true;
        if (!checkGestureZoomAvailable(step, gapSpan, isScaleEnd)) {
            return -1;
        }
        if (Math.abs(gapSpan) < 40 && isCursorInSlowMovingArea()) {
            if (Math.abs(gapSpan) <= 6) {
                CamLog.m3d(CameraConstants.TAG, "gapSpan = " + gapSpan);
                return -1;
            } else if (step < 0) {
                step = -3;
            } else {
                step = 3;
            }
        }
        this.mValue += step;
        if (this.mValue > this.mMaxValue) {
            this.mValue = this.mMaxValue;
        }
        if (this.mValue < 0) {
            this.mValue = 0;
        }
        if (isScaleEnd) {
            this.mSwitchingTryCnt = 0;
        }
        calculateCursorPosition(this.mValue, false);
        boolean isCursorGoingDown = step < 0;
        if (isCursorGoingDown) {
            z = false;
        }
        if (isSwitchingNeeded(isCursorGoingDown, z)) {
            return -1;
        }
        correctCenterCoordination();
        invalidate();
        return this.mValue;
    }

    public boolean isJogZoomAvailable(int value, int factor) {
        boolean isCursorGoingDown;
        boolean z;
        setBarValue(value);
        if (factor == -1) {
            isCursorGoingDown = true;
        } else {
            isCursorGoingDown = false;
        }
        if (isCursorGoingDown) {
            z = false;
        } else {
            z = true;
        }
        if (isSwitchingNeeded(isCursorGoingDown, z) && !FunctionProperties.isSupportedOpticZoom()) {
            return false;
        }
        correctCenterCoordination();
        invalidate();
        return true;
    }

    protected void calculateCursorPosition(int value, boolean correctPosition) {
        if (this.mIsUpperBarMode) {
            this.mCursorCenterY = ((float) this.mBorderCenter) - (this.mSTEP_UNIT * ((float) value));
        } else {
            this.mCursorCenterY = ((float) this.mBarBottom) - (this.mSTEP_UNIT * ((float) value));
        }
        if (correctPosition) {
            correctCenterCoordination();
        }
    }

    public void unbind() {
        if (this.mBorderBitmap != null) {
            this.mBorderBitmap.recycle();
            this.mBorderBitmap = null;
        }
        if (this.mTopIconBitmap != null) {
            this.mTopIconBitmap.recycle();
            this.mTopIconBitmap = null;
        }
        if (this.mBottomIconBitmap != null) {
            this.mBottomIconBitmap.recycle();
            this.mBottomIconBitmap = null;
        }
        setOnTouchListener(null);
        this.mSwitchingTryCnt = 0;
        this.mIsSwitching = false;
        this.mIsMovable = true;
        this.mSwitchableBarInterface = null;
    }

    public void setUpperBarMode(boolean upperBarMode) {
        this.mIsUpperBarMode = upperBarMode;
        calculateNecessaryValues();
    }

    protected void calculateNecessaryValues() {
        if (this.mIsUpperBarMode) {
            this.mTouchableAreaTop = this.mBarTop - this.mTouchableAreaMargin;
            this.mTouchableAreaBottom = this.mBorderCenter + this.mTouchableAreaMargin;
            this.mCurrentBarHeight = this.mBorderCenter - this.mBarTop;
        } else {
            this.mTouchableAreaTop = this.mBorderCenter - this.mTouchableAreaMargin;
            this.mTouchableAreaBottom = this.mBarBottom + this.mTouchableAreaMargin;
            this.mCurrentBarHeight = this.mBarBottom - this.mBorderCenter;
        }
        if (this.mMaxValue != 0) {
            this.mSTEP_UNIT = ((float) this.mCurrentBarHeight) / ((float) this.mMaxValue);
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != 0) {
            this.mIsBarTouched = false;
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (getVisibility() != 0) {
            this.mIsBarTouched = false;
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case 0:
            case 2:
                if (this.mPrevYPos == y) {
                    return true;
                }
                if (event.getActionMasked() == 0) {
                    this.mIsDrawingExceedsLevel = false;
                    this.mPrevYPos = y;
                    if (Math.abs(this.mCursorCenterY - ((float) this.mBorderCenter)) < 10.0f) {
                        this.mPreventChangeCamera = true;
                    }
                }
                if (checkTouchEnableCondition(x, y)) {
                    return doTouchMove(x, y);
                }
                this.mToBeCursor = y;
                this.mPrevYPos = y;
                return false;
            case 1:
            case 3:
                this.mSwitchingTryCnt = 0;
                this.mIsBarTouched = false;
                this.mIsMovable = true;
                this.mPreventChangeCamera = false;
                this.mToBeCursor = y;
                correctCenterCoordination();
                averageMoving(false, this.mPrevYPos - y);
                if (Float.compare(Math.abs(this.mAverageDistance), FAST_MOVE) > 0) {
                    this.mIsDrawingExceedsLevel = true;
                }
                this.mMovingCnt = 0;
                this.mAverageDistance = 0.0f;
                this.mTotalDistance = 0.0f;
                return true;
            default:
                return false;
        }
    }

    private void averageMoving(boolean touchDown, float distance) {
        if (Float.compare(distance, 0.0f) != 0) {
            distance = Math.abs(distance);
            this.mMovingCnt++;
            this.mTotalDistance += distance;
            this.mAverageDistance = this.mTotalDistance / ((float) this.mMovingCnt);
            if (Float.compare(Math.abs(this.mAverageDistance - distance), 10.0f) >= 0) {
                this.mMovingCnt = 1;
                this.mTotalDistance = distance;
                this.mAverageDistance = distance;
            }
        }
    }

    private boolean doTouchMove(float x, float y) {
        boolean isCursorGoingDown;
        boolean isCursorGoingUp;
        CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] doTouchMove, y = " + y);
        if (this.mSwitchableBarInterface != null) {
            this.mSwitchableBarInterface.resetBarDisappearTimer();
        }
        if (!this.mIsBarTouched || this.mIsDrawingExceedsLevel) {
            this.mCursorCenterY = y;
        } else {
            this.mCursorCenterY -= this.mPrevYPos - y;
        }
        if (Float.compare(this.mPrevYPos, y) < 0) {
            isCursorGoingDown = true;
        } else {
            isCursorGoingDown = false;
        }
        if (Float.compare(this.mPrevYPos, y) > 0) {
            isCursorGoingUp = true;
        } else {
            isCursorGoingUp = false;
        }
        float diff = this.mPrevYPos - y;
        this.mPrevYPos = y;
        this.mToBeCursor = y;
        this.mIsBarTouched = true;
        averageMoving(true, diff);
        if (!isSwitchingNeeded(isCursorGoingDown, isCursorGoingUp)) {
            correctCenterCoordination();
            calcultateZoomValueByCursorPosition(this.mCursorCenterY);
            this.mIsDrawingExceedsLevel = false;
            if (this.mValue != this.mToBeValue) {
                this.mValue = this.mToBeValue;
                setParameter();
                invalidate();
            }
        } else if (!this.mIsDrawingExceedsLevel && checkFastMove()) {
            this.mIsDrawingExceedsLevel = true;
        }
        return true;
    }

    private boolean checkFastMove() {
        return Float.compare(Math.abs(this.mAverageDistance), FAST_MOVE) > 0;
    }

    protected void correctCenterCoordination() {
        if (this.mIsUpperBarMode) {
            if (Float.compare(this.mCursorCenterY, (float) this.mBarTop) < 0) {
                this.mCursorCenterY = (float) this.mBarTop;
            }
            if (Float.compare(this.mCursorCenterY, (float) this.mBorderCenter) > 0) {
                this.mCursorCenterY = (float) this.mBorderCenter;
                return;
            }
            return;
        }
        if (Float.compare(this.mCursorCenterY, (float) this.mBorderCenter) < 0) {
            this.mCursorCenterY = (float) this.mBorderCenter;
        }
        if (Float.compare(this.mCursorCenterY, (float) this.mBarBottom) > 0) {
            this.mCursorCenterY = (float) this.mBarBottom;
        }
    }

    protected boolean checkTouchEnableCondition(float x, float y) {
        if (this.mIsDrawingExceedsLevel) {
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Drawing fake cursor, return");
            return false;
        } else if (!isInTouchableArea(x, y) && !this.mIsBarTouched) {
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Out of touchable area, return");
            return false;
        } else if (this.mIsSwitching) {
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Camera's switching, return");
            return false;
        } else if (!checkBarMovable(y)) {
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Bar's not movable, return");
            return false;
        } else if (!this.mIsBarTouched || this.mSwitchingAfterTryCnt >= 3) {
            return true;
        } else {
            this.mSwitchingAfterTryCnt++;
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Counting, return : " + this.mSwitchingAfterTryCnt);
            return false;
        }
    }

    protected boolean checkBarMovable(float y) {
        if (this.mIsMovable) {
            return true;
        }
        if (this.mIsUpperBarMode) {
        }
        if (Float.compare(Math.abs(this.mPrevYPos - y), 0.0f) >= 0) {
            this.mIsMovable = true;
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Bar is not movable yet.");
        return false;
    }

    protected boolean isSwitchingNeeded(boolean isCursorGoingDown, boolean isCursorGoingUp) {
        if (!isCursorInSwitchingArea()) {
            return false;
        }
        if (this.mSwitchableBarInterface == null || this.mSwitchableBarInterface.isDoubleCameraSwitchingAvailable()) {
            return checkSwitchingNeeded(isCursorGoingDown, isCursorGoingUp);
        }
        return false;
    }

    private boolean checkSwitchingNeeded(boolean isCursorGoingDown, boolean isCursorGoingUp) {
        if (this.mIsUpperBarMode && isCursorGoingDown) {
            if (Float.compare(this.mCursorCenterY, (float) this.mBorderCenter) < 0) {
                CamLog.m3d(CameraConstants.TAG, "Still not in switchable area, return");
                invalidate();
                return false;
            } else if (!this.mNextTime && Float.compare(this.mCursorCenterY, (float) this.mBorderCenter) > 0) {
                CamLog.m3d(CameraConstants.TAG, "Still not in switchable area, return");
                this.mNextTime = true;
                invalidate();
                return false;
            } else if (checkFastMove() || this.mSwitchingTryCnt == 5) {
                this.mSwitchingTryCnt = 0;
                switchBar();
                return true;
            } else {
                this.mSwitchingTryCnt++;
                CamLog.m3d(CameraConstants.TAG, "Upper bar, mSwitchingTryCnt = " + this.mSwitchingTryCnt);
                return false;
            }
        } else if (this.mIsUpperBarMode || !isCursorGoingUp) {
            return false;
        } else {
            if (Float.compare(this.mCursorCenterY, (float) this.mBorderCenter) > 0) {
                CamLog.m3d(CameraConstants.TAG, "Still not in switchable area, return");
                invalidate();
                return false;
            } else if (!this.mNextTime && Float.compare(this.mCursorCenterY, (float) this.mBorderCenter) < 0) {
                CamLog.m3d(CameraConstants.TAG, "Still not in switchable area, return");
                this.mNextTime = true;
                invalidate();
                return false;
            } else if (checkFastMove() || this.mSwitchingTryCnt == 5) {
                this.mSwitchingTryCnt = 0;
                switchBar();
                return true;
            } else {
                this.mSwitchingTryCnt++;
                CamLog.m3d(CameraConstants.TAG, "Lower bar, mSwitchingTryCnt = " + this.mSwitchingTryCnt);
                return false;
            }
        }
    }

    protected void calcultateZoomValueByCursorPosition(float y) {
        if (this.mIsSwitching) {
            CamLog.m7i(CameraConstants.TAG, " switching !!!");
        }
        if (this.mIsUpperBarMode) {
            if (Float.compare(y, (float) this.mBarTop) < 0) {
                y = (float) this.mBarTop;
            }
            if (Float.compare(y, (float) this.mBorderCenter) > 0) {
                y = (float) this.mBorderCenter;
            }
            this.mToBeValue = (int) ((Math.abs(((float) this.mBorderCenter) - y) * ((float) this.mMaxValue)) / ((float) this.mCurrentBarHeight));
        } else {
            if (Float.compare(y, (float) this.mBorderCenter) < 0) {
                y = (float) this.mBorderCenter;
            }
            if (Float.compare(y, (float) this.mBarBottom) > 0) {
                y = (float) this.mBarBottom;
            }
            this.mToBeValue = this.mMaxValue - ((int) ((Math.abs(((float) this.mBorderCenter) - y) * ((float) this.mMaxValue)) / ((float) this.mCurrentBarHeight)));
        }
        CamLog.m3d(CameraConstants.TAG, "mToBeValue = " + this.mToBeValue);
        CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] y = " + y);
    }

    public void switchBar() {
        boolean z = true;
        if (this.mSwitchableBarInterface != null) {
            if (!FunctionProperties.isSupportedOpticZoom()) {
                this.mSwitchableBarInterface.stopDrawingExceedsLevel();
            }
            CamLog.m3d(CameraConstants.TAG, "[INO_Debugging] Switching camera starts");
            this.mIsSwitching = true;
            this.mIsMovable = false;
            this.mNextTime = false;
            if (this.mIsUpperBarMode) {
                z = false;
            }
            this.mIsUpperBarMode = z;
            calculateNecessaryValues();
            this.mSwitchableBarInterface.onBarSwitching(this.mIsBarTouched);
        }
    }

    private void setCursorMovingStep() {
        float distance = getDistance();
        if (Float.compare(distance, 50.0f) <= 0) {
            this.mMoveStep = 5;
        } else if (Float.compare(distance, 50.0f) > 0 && Float.compare(distance, 100.0f) <= 0) {
            this.mMoveStep = 10;
        } else if (Float.compare(distance, 101.0f) > 0 && Float.compare(distance, 200.0f) <= 0) {
            this.mMoveStep = 20;
        } else if (Float.compare(distance, 201.0f) > 0 && Float.compare(distance, 300.0f) <= 0) {
            this.mMoveStep = 30;
        } else if (Float.compare(distance, 301.0f) <= 0 || Float.compare(distance, 400.0f) > 0) {
            this.mMoveStep = 50;
        } else {
            this.mMoveStep = 40;
        }
    }

    protected float getDistance() {
        return Math.abs(this.mCursorCenterY - ((float) this.mBorderCenter));
    }

    public int drawExceedsLevels() {
        setCursorMovingStep();
        int stepInternal = this.mMoveStep;
        if (this.mValue > this.mToBeValue) {
            stepInternal *= -1;
        }
        int result = stepInternal;
        this.mValue += stepInternal;
        if ((stepInternal > 0 && this.mValue >= this.mToBeValue) || (stepInternal < 0 && this.mValue <= this.mToBeValue)) {
            this.mValue = this.mToBeValue;
            this.mIsDrawingExceedsLevel = false;
        }
        calculateCursorPosition(this.mValue, true);
        invalidate();
        return result;
    }

    public boolean isDrawingExceedsLevels() {
        return this.mIsDrawingExceedsLevel;
    }

    public void stopDrawingExceedsLevels() {
        this.mIsDrawingExceedsLevel = false;
        this.mToBeValue = 0;
    }

    private void setParameter() {
        if ((this.mValue > 0 || this.mIsSwitching || !this.mIsUpperBarMode) && this.mValue >= this.mMaxValue && !this.mIsSwitching && !this.mIsUpperBarMode) {
        }
        if (this.mSwitchableBarInterface != null) {
            this.mSwitchableBarInterface.setValue(this.mValue);
        }
    }

    public boolean isBarTouched() {
        return this.mIsBarTouched;
    }

    public int getInAndOutZoomBarEndMargin(boolean isReverse) {
        if (isReverse) {
            return this.mBarStart;
        }
        return this.mTotalWidth - this.mBarEnd;
    }

    public void setBorderPosition(int step) {
    }
}
