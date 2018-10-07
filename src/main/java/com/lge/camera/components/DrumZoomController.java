package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.GradientDrawable;
import android.support.p000v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.util.Utils;

public class DrumZoomController extends DrumBarController {
    protected int mAcceleratedDistance = 0;
    private int mDrawingStep = 0;
    public OnDrumZoomControllerListener mDrumZoomListener = null;
    private int mMaxZoomLevel = 0;
    private float mOneStepDistance = 0.0f;
    private boolean mUseAcceleratedPosition = false;
    private VelocityTracker mVelocityTracker;

    public interface OnDrumZoomControllerListener {
        void onDrumTouchDown();

        void onDrumZoomValueChanged(int i);
    }

    public DrumZoomController(Context context) {
        super(context);
    }

    public DrumZoomController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrumZoomController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDrumZoomControllerListener(OnDrumZoomControllerListener listener) {
        this.mDrumZoomListener = listener;
    }

    public void initDimensionValue() {
        super.initDimensionValue();
        this.mTickWidthL = Utils.dpToPx(getContext(), 16.0f);
        this.mMinimumDistanceForDrumMovement = Utils.dpToPx(getContext(), 2.0f);
    }

    protected void initConstantValues() {
        super.initConstantValues();
        this.mDRUM_HEIGHT = (int) Utils.dpToPx(getContext(), 160.0f);
        this.mDRUM_WIDTH = (int) Utils.dpToPx(getContext(), 24.25f);
        this.mSHOWING_STEP = 20;
        this.mITEM_GAP = Math.round((float) (this.mDRUM_HEIGHT / this.mSHOWING_STEP));
        this.mDrawingStep = this.mSHOWING_STEP / 20;
        this.mOneStepDistance = ((float) this.mDRUM_HEIGHT) / ((float) this.mMaxZoomLevel);
    }

    public void initDrumZoom(int drumType, int maxZoomLevel) {
        initDrumZoom(drumType, maxZoomLevel, false);
    }

    public void initDrumZoom(int drumType, int maxZoomLevel, boolean byForce) {
        this.mDrumType = drumType;
        this.mMaxZoomLevel = maxZoomLevel;
        initDimensionValue();
        if (byForce) {
            this.mInit = false;
            if (this.mClickRegionMap != null) {
                this.mClickRegionMap.clear();
            }
        }
        if (!this.mInit) {
            initConfiguration();
            initConstantValues();
            this.mLcdHeight = (int) Utils.dpToPx(getContext(), 260.0f);
            this.mCenterY = Math.round(((float) this.mLcdHeight) / 2.0f) + ((int) Utils.dpToPx(getContext(), 7.5f));
            this.mHalfHeight = this.mDRUM_HEIGHT / 2;
            this.mDrumEndPosX = (int) Utils.dpToPx(getContext(), 40.0f);
            this.mDrumEndPosY = this.mCenterY + this.mHalfHeight;
            this.mDrumStartPosX = this.mDrumEndPosX - this.mDRUM_WIDTH;
            this.mDrumStartPosY = this.mCenterY - this.mHalfHeight;
            this.mDrumStartMargin = (int) Utils.dpToPx(getContext(), 7.875f);
            this.mDrumEndMargin = (int) Utils.dpToPx(getContext(), 7.875f);
            this.mStartPosition = this.mDrumEndPosY;
            this.mCurValuePosition = this.mStartPosition;
            this.mGradiantBg = (GradientDrawable) getContext().getDrawable(C0088R.drawable.drum_background);
            if (this.mGestureDetector == null) {
                this.mGestureDetector = new GestureDetectorCompat(getContext(), this);
            }
            setOnTouchListener(this);
            this.mScrollPosition = this.mCurValuePosition;
            this.mInit = true;
            postInvalidate();
        }
        makeClickableAreaList();
    }

    protected void onDraw(Canvas canvas) {
        boolean isTouchMoving;
        drawBackground(canvas);
        if (this.mScrollState == 0 && this.mTouchState == 2) {
            isTouchMoving = true;
        } else {
            isTouchMoving = false;
        }
        int currentPosition = this.mScrollPosition;
        int index = (int) (((float) (this.mDrumEndPosY - currentPosition)) / this.mOneStepDistance);
        if (index < 0) {
            index = 0;
        } else if (index > this.mMaxZoomLevel) {
            index = this.mMaxZoomLevel;
        }
        if (this.mCurrentSelectedIndex != index) {
            this.mCurrentSelectedIndex = index;
            if (isTouchMoving) {
                sendPerformClick(index, 0);
            }
        }
        this.mTickPaint.setStyle(Style.STROKE);
        this.mTickPaint.setStrokeWidth(this.mTickThick);
        drawCursor(canvas, currentPosition);
        drawDrumBarTick(canvas, currentPosition);
    }

    protected void drawCursor(Canvas canvas, int currentPosition) {
    }

    protected void initDrumBgDimension() {
        this.mTopDownBarWidth = Math.round(Utils.dpToPx(getContext(), 10.75f));
        this.mOuterMargin = Math.round(Utils.dpToPx(getContext(), 0.5f));
        this.mCenterTickWidth = Math.round(Utils.dpToPx(getContext(), 24.5f));
        this.mCenterTickHeight = Math.round(Utils.dpToPx(getContext(), 2.0f));
    }

    protected void drawBackground(Canvas canvas) {
        this.mBarBgRect.setColor(Color.argb(153, 174, 174, 174));
        this.mBarBgRect.setStyle(Style.FILL);
        if (this.mGradiantBg != null) {
            this.mGradiantBg.setBounds(this.mDrumStartMargin, this.mDrumStartPosY, this.mDrumStartMargin + this.mDRUM_WIDTH, this.mDrumEndPosY);
            this.mGradiantBg.draw(canvas);
        }
    }

    protected void drawDrumBarTick(Canvas canvas, int currentPosition) {
        int i;
        int tichHalfHeight = Math.max(1, (int) (this.mTickThick / 2.0f));
        for (i = 0; i <= this.mSHOWING_STEP; i++) {
            drawTick(canvas, tichHalfHeight, (int) this.mTickWidthL, currentPosition - (this.mITEM_GAP * i));
        }
        for (i = 1; i <= this.mSHOWING_STEP; i++) {
            drawTick(canvas, tichHalfHeight, (int) this.mTickWidthL, currentPosition + (this.mITEM_GAP * i));
        }
    }

    protected void drawTick(Canvas canvas, int tichHalfHeight, int tickWidth, int itemPosition) {
        if (itemPosition >= this.mDrumStartPosY + this.mITEM_GAP && itemPosition <= this.mDrumEndPosY - this.mITEM_GAP) {
            int tickAlpha;
            if (isExistItemInScope(itemPosition, this.mCenterY, this.mITEM_GAP * this.mDrawingStep)) {
                tickAlpha = 255;
            } else if (isExistItemInScope(itemPosition, this.mCenterY, (this.mITEM_GAP * this.mDrawingStep) * 2)) {
                tickAlpha = 200;
            } else if (isExistItemInScope(itemPosition, this.mCenterY, (this.mITEM_GAP * this.mDrawingStep) * 3)) {
                tickAlpha = 150;
            } else if (isExistItemInScope(itemPosition, this.mCenterY, (this.mITEM_GAP * this.mDrawingStep) * 4)) {
                tickAlpha = 100;
            } else {
                tickAlpha = 50;
            }
            this.mTickPaint.setColor(Color.argb(tickAlpha, 255, 255, 255));
            int tickPadding = Math.round(Utils.dpToPx(getContext(), 4.25f));
            canvas.drawRect((float) (this.mDrumStartMargin + tickPadding), (float) (itemPosition - tichHalfHeight), (float) ((this.mDrumStartMargin + this.mDRUM_WIDTH) - tickPadding), (float) ((itemPosition + tichHalfHeight) - 1), this.mTickPaint);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (this.mUseAcceleratedPosition) {
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            this.mVelocityTracker.addMovement(event);
        }
        if (!this.mInit || getVisibility() != 0 || !isEnabled()) {
            return false;
        }
        if (this.mGestureDetector != null) {
            this.mGestureDetector.onTouchEvent(event);
        }
        if (!doTouchEvent(event)) {
            return false;
        }
        if (this.mIsTouchArea) {
            return true;
        }
        this.mTouchState = 0;
        return false;
    }

    protected boolean doTouchActionDown(MotionEvent event) {
        if (this.mDrumZoomListener != null) {
            this.mDrumZoomListener.onDrumTouchDown();
        }
        this.mAcceleratedDistance = 0;
        return super.doTouchActionDown(event);
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        if (this.mTouchState == 0 || !this.mIsTouchArea) {
            this.mTouchState = 0;
            return false;
        }
        float distance;
        int pointerIndex = event.findPointerIndex(this.mActivePointerId);
        float distY = event.getY(pointerIndex) - this.mStartY;
        if (this.mUseAcceleratedPosition) {
            float velocity = 0.0f;
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.computeCurrentVelocity(1);
                velocity = this.mVelocityTracker.getYVelocity();
            }
            if (velocity < -0.8f && distY < 0.0f) {
                this.mAcceleratedDistance -= 50;
            } else if (velocity > 0.9f && distY > 0.0f) {
                this.mAcceleratedDistance += 50;
            }
            distance = distY + ((float) this.mAcceleratedDistance);
        } else {
            distance = distY * 2.0f;
        }
        if (this.mScrollPosition <= this.mDrumStartPosY && distY < 0.0f) {
            this.mStartY = event.getY(pointerIndex);
            this.mCurValuePosition = this.mScrollPosition;
            this.mAcceleratedDistance = 0;
            return true;
        } else if (this.mScrollPosition < this.mDrumEndPosY || distY <= 0.0f) {
            int absDistance = (int) Math.abs(distY);
            if (!this.mUseAcceleratedPosition && this.mTouchState == 2 && ((float) absDistance) < this.mMinimumDistanceForDrumMovement) {
                return true;
            }
            if (((float) absDistance) > this.mMinimumDistanceForDrumMovement) {
                this.mTouchState = 2;
            }
            onScrollPosition(this.mStartX, this.mStartY, 0.0f, distance);
            return true;
        } else {
            this.mStartY = event.getY(pointerIndex);
            this.mCurValuePosition = this.mScrollPosition;
            this.mAcceleratedDistance = 0;
            return true;
        }
    }

    protected boolean doTouchActionUp() {
        if (this.mUseAcceleratedPosition && this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        return super.doTouchActionUp();
    }

    protected boolean checkTouchArea(int x, int y) {
        return true;
    }

    protected void sendPerformClick(final int selectedIndex, int duration) {
        postDelayed(new Runnable() {
            public void run() {
                if (DrumZoomController.this.mScrollState == 0) {
                    DrumZoomController.this.setContentDescription(selectedIndex + "");
                    DrumZoomController.this.sendAccessibilityEvent(16384);
                }
                DrumZoomController.this.mDrumZoomListener.onDrumZoomValueChanged(selectedIndex);
            }
        }, 0);
    }

    protected boolean checkScrollBoundary(double diff) {
        return true;
    }

    protected void releaseScrollPosition(boolean draw, boolean confirm) {
    }

    public void setSelectedItem(String value, boolean isDefault) {
        if (value != null) {
            this.mCurValuePosition = (int) (((float) this.mDrumEndPosY) - (((float) Integer.parseInt(value)) * this.mOneStepDistance));
            this.mScrollPosition = this.mCurValuePosition;
        }
    }
}
