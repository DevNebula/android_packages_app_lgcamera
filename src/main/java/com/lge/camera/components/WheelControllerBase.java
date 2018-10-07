package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.p000v4.view.GestureDetectorCompat;
import android.support.p000v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import java.util.ArrayList;

public class WheelControllerBase extends View implements OnGestureListener, OnTouchListener {
    protected static final int BOUND_MAX_ADJ = 2;
    protected static final int CENTER_BOUND_DEG = 4;
    protected static final int CLICK_DELAY = 250;
    protected static final int CONFIRM_SEL_DELAY = 300;
    protected static final int DEFAULT_FLING_DURATION = 150;
    protected static final double DEG_CVT = 57.29577951308232d;
    protected static final int DISABLED_COLOR = Color.argb(102, 255, 255, 255);
    protected static final int ICON_DISABLED_ALPHA = 102;
    protected static final int ICON_ENABLED_ALPHA = 255;
    protected static final int INVALID_CLICK_ITEM = -100;
    protected static final int INVALID_POINTER = -1;
    protected static final int INVALID_SS_COLOR = Color.argb(230, 255, 207, 108);
    protected static final int NORMAL_COLOR = -1;
    /* renamed from: PI */
    protected static final double f18PI = 3.141592653589793d;
    protected static final int POINT_COLOR = Color.argb(255, 29, 219, 217);
    protected static final float RADIUS_LARGE_FACTOR = 1.503f;
    protected static final float RADIUS_SMALL_FACTOR = 2.483f;
    protected static final double RAD_CVT = 0.017453292519943295d;
    protected static final int RIM_BOUNDARY_COLOR = Color.argb(180, 0, 0, 0);
    protected static final int RIM_INNER_COLOR = Color.argb(255, 255, 255, 255);
    protected static final int SCROLL_IDLE = 0;
    protected static final int SCROLL_SCROLLING = 1;
    protected static final int TICK_ALPHA_L = 230;
    protected static final int TICK_ALPHA_S = 72;
    protected static final int TICK_COLOR = -1;
    protected static final int TOUCH_DONW = 1;
    protected static final int TOUCH_IDLE = 0;
    protected static final int TOUCH_MOVING = 2;
    protected static final int UNSELECTED = -1;
    protected static final int VALID_SS_COLOR = Color.argb(230, 55, 223, 221);
    protected int mActivePointerId = -1;
    protected Bitmap mArrowIcon = null;
    protected final Paint mArrowIconPaint = new Paint(1);
    protected int mBOUND_MAX_DEG = 0;
    protected int mBaseTextSize = 0;
    protected int mCenterTextSize = 0;
    protected int mCenterX = 0;
    protected int mCenterY = 0;
    protected Runnable mClickFeedback = new C05582();
    protected SparseArray<Rect> mClickRegionMap = new SparseArray();
    protected int mClickedItem = -100;
    protected Runnable mConfirmSelection = new C05571();
    protected double mCurValueAngle = 0.0d;
    protected int mCurrentTouchWheelType = 0;
    protected final ArrayList<WheelItem> mDataList = new ArrayList();
    protected float mDeceleration = 0.0f;
    protected long mEndTime = 0;
    protected float mGAUGE_DRAW_DEG = 0.0f;
    protected float mGaugeThick = 0.0f;
    protected GestureDetectorCompat mGestureDetector = null;
    protected float mHideRadius = 0.0f;
    protected double mITEM_ANGLE = 0.0d;
    protected int mITEM_DEG = 0;
    protected int mIconPadding = 0;
    protected boolean mInit = false;
    protected DecelerateInterpolator mInterpolator = new DecelerateInterpolator(2.0f);
    protected boolean mIsInItemArea = false;
    protected boolean mIsUseLargeRadius = false;
    protected int mLcdHeight = 0;
    public OnWheelControllerListener mListener = null;
    protected int mMAX_SHOW_DEGREE = 0;
    protected int mMIN_SHOW_DEGREE = 0;
    protected float mMinimumDistanceForWheelMovement = 0.0f;
    protected double mMoveAngle = 0.0d;
    protected final Paint mPaint = new Paint(1);
    protected PerformClick mPerformClick = new PerformClick();
    protected int mPressedStateDuration = 0;
    protected float mRIM_DRAW_DEG = 0.0f;
    protected int mRadius = 0;
    protected final Paint mRimPaint = new Paint(1);
    protected float mRimThick = 0.0f;
    protected RotationInfo mRotationInfo = new RotationInfo();
    protected int mSHOWING_STEP = 0;
    protected double mScrollAngle = 0.0d;
    protected long mScrollDuration = 0;
    protected int mScrollState = 0;
    protected float mShadowRadius = 0.0f;
    protected double mStartAngleRadius = 0.0d;
    protected long mStartTime = 0;
    protected float mStartX = 0.0f;
    protected float mStartY = 0.0f;
    protected int mTPaintColor = -1;
    protected int mTextAreaMinClickHeight = 0;
    protected int mTextAreaMinClickWidth = 0;
    protected int mTextAreaMinWidth = 0;
    protected int mTextPadding = 0;
    protected final Rect mTextRect = new Rect();
    protected final Paint mTickPaint = new Paint(1);
    protected int mTickPaintColor = -1;
    protected float mTickThick = 0.0f;
    protected float mTickWidthL = 0.0f;
    protected float mTickWidthS = 0.0f;
    protected int mTouchSlop = 0;
    protected int mTouchState = 0;
    protected final TextPaint mTpaint = new TextPaint(1);
    protected int mViewDegree = 0;
    protected final RectF mWheelRectF = new RectF();
    protected int mWheelTouchInnerPadding = 0;
    protected int mWheelTouchPadding = 0;
    protected int mWheelType = 0;

    /* renamed from: com.lge.camera.components.WheelControllerBase$1 */
    class C05571 implements Runnable {
        C05571() {
        }

        public void run() {
            if (WheelControllerBase.this.mListener != null) {
                String panelItemTitle = WheelControllerBase.this.mListener.onGettingSeletedItem(WheelControllerBase.this.mWheelType);
                WheelItem item = WheelControllerBase.this.getCurSelectedItem();
                if (item != null && !"none".equals(panelItemTitle)) {
                    String title = item.getTitle();
                    if (title != null && !title.equals(panelItemTitle)) {
                        WheelControllerBase.this.sendPerformClick(WheelControllerBase.this.mPressedStateDuration);
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.components.WheelControllerBase$2 */
    class C05582 implements Runnable {
        C05582() {
        }

        public void run() {
            if (WheelControllerBase.this.mListener != null) {
                WheelControllerBase.this.mListener.playWheelEffectSound();
            }
        }
    }

    public interface OnWheelControllerListener {
        String onGettingSeletedItem(int i);

        void onItemSelected(WheelItem wheelItem, boolean z);

        void playWheelEffectSound();
    }

    protected class PerformClick implements Runnable {
        protected int mSelectedIndex = -1;

        protected PerformClick() {
        }

        protected void setSeletedIndex(int index) {
            this.mSelectedIndex = index;
        }

        protected void release() {
            this.mSelectedIndex = -1;
        }

        public void run() {
            WheelItem item;
            if (this.mSelectedIndex == -1) {
                item = WheelControllerBase.this.getCurSelectedItem();
            } else {
                item = WheelControllerBase.this.getWheelItem(this.mSelectedIndex);
            }
            if (item != null && WheelControllerBase.this.mListener != null) {
                WheelControllerBase.this.mListener.onItemSelected(item, WheelControllerBase.this.getVisibility() == 0);
                this.mSelectedIndex = -1;
            }
        }
    }

    public WheelControllerBase(Context context) {
        super(context);
    }

    public WheelControllerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WheelControllerBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onDraw(Canvas canvas) {
        if (!this.mInit || canvas == null) {
            postInvalidate();
        } else if (getVisibility() == 0 && this.mDataList != null && this.mDataList.size() != 0) {
            drawGauge(canvas);
            calculateScrollAngle();
            int dataSize = this.mDataList.size();
            double scrollAngle = this.mScrollAngle;
            double startDegree = this.mStartAngleRadius * DEG_CVT;
            float rimSweepDegree = 0.0f;
            int rotDegree = 0;
            this.mRimPaint.setStyle(Style.STROKE);
            this.mRimPaint.setStrokeWidth(this.mGaugeThick);
            this.mTpaint.setColor(this.mTPaintColor);
            this.mWheelRectF.set((float) (this.mCenterX - this.mRadius), (float) (this.mCenterY - this.mRadius), (float) (this.mCenterX + this.mRadius), (float) (this.mCenterY + this.mRadius));
            boolean isTouchMoving = this.mScrollState == 0 && this.mTouchState == 2;
            if (!(this.mRotationInfo == null || this.mRotationInfo.getCurrentDegree() == this.mRotationInfo.getTargetDegree() || !this.mRotationInfo.calcCurrentDegree())) {
                invalidate();
            }
            canvas.drawBitmap(this.mArrowIcon, (float) (this.mCenterX - this.mRadius), (float) (this.mCenterY - (this.mArrowIcon.getHeight() / 2)), this.mArrowIconPaint);
            for (int i = 0; i < dataSize; i++) {
                canvas.save();
                WheelItem item = (WheelItem) this.mDataList.get(i);
                boolean hasIcon = false;
                Bitmap icon = null;
                if (item.mResId > 0) {
                    icon = BitmapManagingUtil.getBitmap(this.mContext, item.mResId);
                    if (icon != null) {
                        hasIcon = true;
                    }
                }
                double itemAngle = item.mAngle + scrollAngle;
                double sweepDegree = itemAngle * DEG_CVT;
                double originSweepDegree = sweepDegree;
                float rimStartDegree = (float) startDegree;
                if (rimStartDegree < ((float) (this.mMIN_SHOW_DEGREE - 1))) {
                    rimStartDegree = (float) this.mMIN_SHOW_DEGREE;
                }
                rimSweepDegree = (float) sweepDegree;
                if (rimSweepDegree > ((float) this.mMAX_SHOW_DEGREE) && rimSweepDegree < ((float) (this.mMAX_SHOW_DEGREE + this.mITEM_DEG))) {
                    rimSweepDegree = (float) this.mMAX_SHOW_DEGREE;
                }
                float rimSweepAngle = rimSweepDegree - rimStartDegree;
                if (rimSweepDegree < ((float) this.mMIN_SHOW_DEGREE) || rimSweepDegree > ((float) this.mMAX_SHOW_DEGREE)) {
                    rimSweepAngle = 0.0f;
                }
                if (!item.mRecommend || item.mRecommendType == 1) {
                    if (i == 0) {
                        this.mRimPaint.setColor(RIM_BOUNDARY_COLOR);
                    } else if (this.mWheelType != 1) {
                        this.mRimPaint.setColor(RIM_INNER_COLOR);
                    } else {
                        this.mRimPaint.setColor(RIM_INNER_COLOR);
                    }
                    canvas.drawArc(this.mWheelRectF, rimStartDegree, rimSweepAngle, false, this.mRimPaint);
                } else {
                    this.mRimPaint.setColor(VALID_SS_COLOR);
                    canvas.drawArc(this.mWheelRectF, rimStartDegree, rimSweepAngle, false, this.mRimPaint);
                }
                if (Math.abs(sweepDegree - 180.0d) <= ((double) this.mBOUND_MAX_DEG)) {
                    if (sweepDegree <= 176.0d || sweepDegree >= 184.0d) {
                        if (isTouchMoving) {
                            double itemDegree = (double) ((float) (DEG_CVT * itemAngle));
                            double adjDegree = itemDegree % ((double) this.mITEM_DEG);
                            if (Math.abs(adjDegree) < 4.0d) {
                                itemAngle = this.mITEM_ANGLE * ((itemDegree - adjDegree) / ((double) this.mITEM_DEG));
                            } else if (((double) this.mITEM_DEG) - Math.abs(adjDegree) < 4.0d) {
                                itemAngle = this.mITEM_ANGLE * (((((double) this.mITEM_DEG) - adjDegree) + itemDegree) / ((double) this.mITEM_DEG));
                            }
                            double distDegree = Math.abs(((double) ((float) (DEG_CVT * itemAngle))) - 180.0d);
                        }
                        if (hasIcon && icon != null) {
                            this.mPaint.setColorFilter(null);
                        }
                        this.mTpaint.setTextSize((float) this.mBaseTextSize);
                        this.mTpaint.setColor(-1);
                        this.mTpaint.setShadowLayer(this.mShadowRadius, 0.0f, 0.0f, ViewCompat.MEASURED_STATE_MASK);
                        item.mSelected = false;
                    } else {
                        this.mTpaint.setTextSize((float) this.mCenterTextSize);
                        this.mTpaint.setColor(POINT_COLOR);
                        this.mTpaint.setShadowLayer(this.mShadowRadius, 0.0f, 0.0f, ViewCompat.MEASURED_STATE_MASK);
                        if (!item.mSelected) {
                            sendClickFeedback();
                            if (isTouchMoving) {
                                sendPerformClick(i, 250);
                            }
                        }
                        if (isTouchMoving) {
                            itemAngle = f18PI;
                        }
                        if (hasIcon && icon != null) {
                            this.mPaint.setColorFilter(ColorUtil.getColorMatrix(255.0f, 29.0f, 219.0f, 217.0f));
                        }
                        item.mSelected = true;
                    }
                    this.mTpaint.getTextBounds(item.mTitle, 0, item.mTitle.length(), this.mTextRect);
                    float textCentX = ((float) this.mTextRect.width()) / 2.0f;
                    float textCentY = ((float) this.mTextRect.height()) / 2.0f;
                    float dx = (float) (((double) this.mCenterX) + (Math.cos(itemAngle) * ((double) (((float) (this.mRadius + this.mTextPadding)) + textCentX))));
                    float dy = (float) (((double) this.mCenterY) + (Math.sin(itemAngle) * ((double) (((float) (this.mRadius + this.mTextPadding)) + textCentX))));
                    canvas.translate(dx, dy);
                    if (this.mRotationInfo != null) {
                        rotDegree = (-this.mRotationInfo.getCurrentDegree()) + (((int) (DEG_CVT * itemAngle)) - 180);
                    }
                    canvas.rotate((float) rotDegree, 0.0f, 0.0f);
                    canvas.drawText(item.mTitle, -textCentX, textCentY, this.mTpaint);
                    if (hasIcon && icon != null) {
                        if (isEnabled()) {
                            this.mPaint.setAlpha(255);
                        } else {
                            this.mPaint.setAlpha(102);
                        }
                        canvas.drawBitmap(icon, ((-textCentX) - ((float) this.mIconPadding)) - ((float) icon.getWidth()), textCentY - ((((float) icon.getHeight()) * 4.0f) / 5.0f), this.mPaint);
                    }
                    canvas.rotate((float) (-rotDegree), 0.0f, 0.0f);
                    canvas.translate(-dx, -dy);
                } else {
                    item.mSelected = false;
                }
                canvas.restore();
                startDegree = originSweepDegree;
            }
            float lastStartDegree = rimSweepDegree;
            float lastExtraDegree = lastStartDegree + ((float) (this.mITEM_DEG * 3));
            if (lastStartDegree < ((float) this.mMAX_SHOW_DEGREE)) {
                if (lastExtraDegree > ((float) this.mMAX_SHOW_DEGREE) && lastExtraDegree < ((float) (this.mMAX_SHOW_DEGREE + (this.mITEM_DEG * 3)))) {
                    lastExtraDegree = (float) this.mMAX_SHOW_DEGREE;
                }
                this.mRimPaint.setColor(RIM_BOUNDARY_COLOR);
                canvas.drawArc(this.mWheelRectF, lastStartDegree, lastExtraDegree - lastStartDegree, false, this.mRimPaint);
            }
        }
    }

    protected void drawGauge(Canvas canvas) {
        this.mTickPaint.setStyle(Style.STROKE);
        this.mTickPaint.setStrokeWidth(this.mTickThick);
        this.mTickPaint.setColor(this.mTickPaintColor);
        float radius = ((float) this.mRadius) + this.mGaugeThick;
        int tickCount = Math.round((this.mGAUGE_DRAW_DEG * 5.0f) / ((float) this.mITEM_DEG));
        float tickDegree = ((float) this.mITEM_DEG) / 5.0f;
        canvas.save();
        canvas.rotate((-this.mGAUGE_DRAW_DEG) / 2.0f, (float) this.mCenterX, (float) this.mCenterY);
        int i = 0;
        while (i <= tickCount) {
            float tickWidth;
            int tickAlpha;
            if (i == tickCount || i % 5 == 0) {
                tickWidth = this.mTickWidthL;
                tickAlpha = 230;
            } else {
                tickWidth = this.mTickWidthS;
                tickAlpha = 72;
            }
            this.mTickPaint.setAlpha(tickAlpha);
            if (!this.mIsUseLargeRadius || (i >= 2 && i <= tickCount - 2)) {
                Canvas canvas2 = canvas;
                canvas2.drawRect((((float) this.mCenterX) - radius) - tickWidth, ((float) this.mCenterY) - (this.mTickThick / 2.0f), ((float) this.mCenterX) - radius, (this.mTickThick / 2.0f) + ((float) this.mCenterY), this.mTickPaint);
                canvas.rotate(tickDegree, (float) this.mCenterX, (float) this.mCenterY);
            } else {
                canvas.rotate(tickDegree, (float) this.mCenterX, (float) this.mCenterY);
            }
            i++;
        }
        canvas.restore();
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
        if (this.mIsInItemArea) {
            this.mIsInItemArea = false;
        }
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if ((this.mWheelType & this.mCurrentTouchWheelType) == 0) {
            this.mTouchState = 0;
        } else {
            int moveSymbol = velocityY < 0.0f ? 1 : -1;
            int absVelocity = Math.round(Math.abs(velocityY));
            int duration = Math.round((150.0f * ((float) absVelocity)) / this.mDeceleration);
            int scrollStep = moveSymbol * Math.round(((float) absVelocity) / this.mDeceleration);
            if (Math.abs(scrollStep) >= 1) {
                moveWheelScrollStep(scrollStep, (long) duration);
            }
        }
        return false;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (!this.mInit || getVisibility() != 0 || this.mDataList == null || this.mDataList.size() == 0 || !isEnabled()) {
            return false;
        }
        if (this.mGestureDetector != null) {
            this.mGestureDetector.onTouchEvent(event);
        }
        if (!doTouchEvent(event)) {
            return false;
        }
        if ((this.mWheelType & this.mCurrentTouchWheelType) != 0) {
            return true;
        }
        this.mTouchState = 0;
        return false;
    }

    protected boolean doTouchEvent(MotionEvent event) {
        switch (event.getActionMasked() & 255) {
            case 0:
                this.mActivePointerId = event.getPointerId(0);
                if (this.mScrollState == 1) {
                    this.mScrollState = 0;
                    this.mCurValueAngle = this.mScrollAngle;
                }
                this.mTouchState = 1;
                this.mStartX = event.getX();
                this.mStartY = event.getY();
                checkTouchArea((int) this.mStartX, (int) this.mStartY);
                this.mClickedItem = checkItemClickArea((int) this.mStartX, (int) this.mStartY);
                this.mIsInItemArea = this.mClickedItem != -100;
                break;
            case 1:
            case 3:
                if (this.mTouchState == 0 || (this.mWheelType & this.mCurrentTouchWheelType) == 0) {
                    this.mTouchState = 0;
                    this.mStartX = 0.0f;
                    this.mStartY = 0.0f;
                    this.mActivePointerId = -1;
                    return false;
                }
                if (this.mScrollState == 0) {
                    releaseScrollAngle(true, true);
                    this.mCurValueAngle = this.mScrollAngle;
                }
                this.mStartX = 0.0f;
                this.mStartY = 0.0f;
                this.mActivePointerId = -1;
                this.mTouchState = 0;
                if (this.mIsInItemArea) {
                    moveWheelScrollStep(-this.mClickedItem, (long) (Math.abs(this.mClickedItem) * 150));
                    return true;
                }
                break;
            case 2:
                if (this.mTouchState == 0 || (this.mWheelType & this.mCurrentTouchWheelType) == 0) {
                    this.mTouchState = 0;
                    return false;
                }
                int pointerIndex = event.findPointerIndex(this.mActivePointerId);
                float distX = event.getX(pointerIndex) - this.mStartX;
                float distY = event.getY(pointerIndex) - this.mStartY;
                if (Math.abs(distX) >= this.mMinimumDistanceForWheelMovement || Math.abs(distY) >= this.mMinimumDistanceForWheelMovement) {
                    this.mTouchState = 2;
                    if (this.mIsInItemArea) {
                        this.mIsInItemArea = this.mClickedItem == checkItemClickArea((int) event.getX(pointerIndex), (int) event.getY(pointerIndex));
                        if (this.mIsInItemArea) {
                            this.mIsInItemArea = Math.abs(distY) < ((float) this.mTouchSlop);
                        }
                    } else {
                        this.mClickedItem = -100;
                    }
                    onScrollAngle(this.mStartX, this.mStartY, distX, distY);
                    break;
                }
                return true;
                break;
            case 6:
                if ((this.mWheelType & this.mCurrentTouchWheelType) != 0) {
                    int pointerIndexs = (event.getAction() & 65280) >> 8;
                    if (event.getPointerId(pointerIndexs) == this.mActivePointerId) {
                        this.mActivePointerId = event.getPointerId(pointerIndexs == 0 ? 1 : 0);
                        break;
                    }
                }
                this.mTouchState = 0;
                return false;
                break;
        }
        return true;
    }

    protected void checkTouchArea(int x, int y) {
        int largeRadius = Math.round(((float) this.mLcdHeight) / RADIUS_LARGE_FACTOR);
        int smallRadius = Math.round(((float) this.mLcdHeight) / RADIUS_SMALL_FACTOR);
        int maximumTouchRadius = largeRadius + (largeRadius - smallRadius);
        int dx = x - this.mCenterX;
        int dy = y - this.mCenterY;
        int radius = (int) Math.sqrt((double) ((dx * dx) + (dy * dy)));
        if (radius > smallRadius - this.mWheelTouchInnerPadding && radius < largeRadius - this.mWheelTouchPadding && !this.mIsUseLargeRadius) {
            this.mCurrentTouchWheelType = 31;
        } else if (radius < largeRadius - this.mWheelTouchPadding || radius >= maximumTouchRadius || !this.mIsUseLargeRadius) {
            this.mCurrentTouchWheelType = 0;
        } else {
            this.mCurrentTouchWheelType = 2;
        }
    }

    protected int checkItemClickArea(int x, int y) {
        if (this.mClickRegionMap == null) {
            return -100;
        }
        int size = this.mClickRegionMap.size();
        for (int i = 0; i < size; i++) {
            int key = this.mClickRegionMap.keyAt(i);
            if (((Rect) this.mClickRegionMap.get(key)).contains(x, y)) {
                return key;
            }
        }
        return -100;
    }

    protected void calculateScrollAngle() {
        if (this.mScrollState == 1) {
            long curTime = System.currentTimeMillis();
            if (curTime <= this.mEndTime) {
                double interpolatedAngle = this.mMoveAngle * ((double) this.mInterpolator.getInterpolation(Math.min(((float) (curTime - this.mStartTime)) / ((float) this.mScrollDuration), 1.0f)));
                if (checkScrollBoundary(interpolatedAngle)) {
                    this.mScrollAngle = this.mCurValueAngle + interpolatedAngle;
                } else {
                    this.mEndTime = 0;
                }
                invalidate();
                return;
            }
            this.mScrollState = 0;
            releaseScrollAngle(true, true);
            this.mCurValueAngle = this.mScrollAngle;
            sendPerformClick(this.mPressedStateDuration);
        }
    }

    protected void onScrollAngle(float x, float y, float distanceX, float distanceY) {
        double angle = Math.atan2((double) (x - ((float) this.mCenterX)), (double) (y - ((float) this.mCenterY))) - Math.atan2((double) ((x - ((float) this.mCenterX)) + distanceX), (double) ((y - ((float) this.mCenterY)) + distanceY));
        if (checkScrollBoundary(angle)) {
            this.mScrollAngle = this.mCurValueAngle + angle;
        }
        invalidate();
    }

    public boolean moveWheelScrollStep(int step, long duration) {
        if (!this.mInit || getVisibility() != 0 || this.mDataList == null || this.mDataList.size() == 0) {
            return false;
        }
        if (step >= 0) {
            if (((WheelItem) this.mDataList.get(0)).mAngle + this.mScrollAngle >= f18PI) {
                return true;
            }
        } else if (((WheelItem) this.mDataList.get(this.mDataList.size() - 1)).mAngle + this.mScrollAngle <= f18PI) {
            return true;
        }
        this.mCurValueAngle = this.mScrollAngle;
        this.mMoveAngle = (((double) step) * this.mITEM_ANGLE) - (((int) Math.round(this.mScrollAngle * DEG_CVT)) % this.mITEM_DEG == 0 ? 0.0d : this.mScrollAngle % this.mITEM_ANGLE);
        this.mStartTime = System.currentTimeMillis();
        this.mScrollDuration = Math.max(duration, 150);
        this.mEndTime = this.mStartTime + this.mScrollDuration;
        this.mScrollState = 1;
        invalidate();
        return true;
    }

    protected boolean checkScrollBoundary(double angle) {
        if (this.mDataList == null || this.mDataList.size() == 0) {
            return true;
        }
        WheelItem item = (WheelItem) this.mDataList.get(0);
        if (item.mAngle + this.mScrollAngle < f18PI || angle < 0.0d) {
            item = (WheelItem) this.mDataList.get(this.mDataList.size() - 1);
            if (item.mAngle + this.mScrollAngle > f18PI || angle > 0.0d) {
                return true;
            }
            this.mScrollAngle += (f18PI - item.mAngle) - this.mScrollAngle;
            return false;
        }
        this.mScrollAngle += (f18PI - item.mAngle) - this.mScrollAngle;
        return false;
    }

    protected void releaseScrollAngle(boolean draw, boolean confirm) {
        CamLog.m3d(CameraConstants.TAG, "releaseScrollAngle - start : draw = " + draw + ", confirm = " + confirm);
        double scrollAngle = this.mScrollAngle;
        int adjDegree = ((int) Math.round(DEG_CVT * scrollAngle)) % this.mITEM_DEG;
        double adjAngle = adjDegree == 0 ? 0.0d : scrollAngle % this.mITEM_ANGLE;
        int symbol = adjAngle >= 0.0d ? 1 : -1;
        if (Math.abs(adjDegree) > Math.abs(this.mITEM_DEG / 2)) {
            adjAngle = ((double) symbol) * (this.mITEM_ANGLE - Math.abs(adjAngle));
            if (checkScrollBoundary(adjAngle)) {
                this.mScrollAngle = scrollAngle + adjAngle;
            }
        } else if (checkScrollBoundary(adjAngle)) {
            this.mScrollAngle = scrollAngle - adjAngle;
        }
        if (draw) {
            invalidate();
        } else {
            setSelectedItemByAngle();
        }
        if (confirm) {
            sendConfirmSelection(300);
        }
    }

    protected void setSelectedItemByAngle() {
        if (this.mInit && this.mDataList != null && this.mDataList.size() != 0) {
            int dataSize = this.mDataList.size();
            for (int i = 0; i < dataSize; i++) {
                WheelItem item = (WheelItem) this.mDataList.get(i);
                double sweepDegree = (item.mAngle + this.mScrollAngle) * DEG_CVT;
                if (sweepDegree <= 176.0d || sweepDegree >= 184.0d) {
                    item.mSelected = false;
                } else {
                    item.mSelected = true;
                }
            }
        }
    }

    protected void sendConfirmSelection(long delay) {
        if (this.mConfirmSelection != null) {
            postDelayed(this.mConfirmSelection, delay);
        }
    }

    protected void removeConfirmSelection() {
        if (this.mConfirmSelection != null) {
            removeCallbacks(this.mConfirmSelection);
        }
    }

    protected void sendClickFeedback() {
        if (this.mClickFeedback != null) {
            postDelayed(this.mClickFeedback, 0);
        }
    }

    protected void sendPerformClick(int duration) {
        if (this.mPerformClick != null) {
            removePerformClick();
            postDelayed(this.mPerformClick, (long) duration);
        }
    }

    protected void sendPerformClick(int selectedIndex, int duration) {
        if (this.mPerformClick != null) {
            removePerformClick();
            this.mPerformClick.setSeletedIndex(selectedIndex);
            postDelayed(this.mPerformClick, (long) duration);
        }
    }

    protected void removePerformClick() {
        if (this.mPerformClick != null) {
            removeCallbacks(this.mPerformClick);
            this.mPerformClick.release();
        }
    }

    public WheelItem getCurSelectedItem() {
        return null;
    }

    public WheelItem getWheelItem(int index) {
        return null;
    }
}
