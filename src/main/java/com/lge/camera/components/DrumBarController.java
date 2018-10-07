package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualModeItem;
import com.lge.camera.util.Utils;

public class DrumBarController extends DrumController {
    protected int mCurrentSelectedIndex = 0;
    protected Bitmap mCursor = null;
    protected int mCursorHeight = 0;
    protected int mCursorWidth = 0;
    private final Paint mIcon = new Paint(1);
    private int mIconHeight = 0;
    private Bitmap mIconInfinity = null;
    private Bitmap mIconMacro = null;
    private int mIconPaddingEnd = 0;
    private int mIconWidth = 0;

    public DrumBarController(Context context) {
        super(context);
    }

    public DrumBarController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrumBarController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initDimensionValue() {
        super.initDimensionValue();
        this.mTickThick = Math.max(1.0f, Utils.dpToPx(getContext(), 0.5f));
    }

    protected void initConstantValues() {
        super.initConstantValues();
        this.mSHOWING_STEP = 60;
        this.mITEM_GAP = Math.round((float) (this.mDRUM_HEIGHT / this.mSHOWING_STEP));
        this.mIconPaddingEnd = (int) Utils.dpToPx(getContext(), 0.25f);
    }

    protected void initDrumBgDimension() {
        super.initDrumBgDimension();
        this.mCenterTickWidth = Math.round(Utils.dpToPx(getContext(), 14.5f));
    }

    public void initResources(int drumType, ManualModeItem modeItem) {
        super.initResources(drumType, modeItem);
        this.mIconInfinity = BitmapManagingUtil.getBitmap(getContext(), C0088R.drawable.camera_drum_wheel_focus_icon_infinity);
        this.mIconMacro = BitmapManagingUtil.getBitmap(getContext(), C0088R.drawable.camera_drum_wheel_focus_icon_macro);
        this.mCursor = BitmapManagingUtil.getBitmap(getContext(), C0088R.drawable.camera_main_arrow);
        if (this.mIconInfinity == null || this.mIconMacro == null || this.mCursor == null) {
            CamLog.m11w(CameraConstants.TAG, "mIconInfinity, mIconMacro or mCursor is null");
            return;
        }
        this.mIconWidth = this.mIconInfinity.getWidth();
        this.mIconHeight = this.mIconInfinity.getHeight();
        this.mCursorHeight = this.mCursor.getHeight();
        this.mCursorWidth = this.mCursor.getWidth();
    }

    protected void makeDataList(String key, String[] entries, String[] values, Integer[] icons, int[] barColors, boolean[] showTitle, String defaultEntryValue, String currentValue) {
        this.mStartPosition = this.mDrumEndPosY;
        for (int i = 0; i < entries.length; i++) {
            DrumItem item = new DrumItem();
            item.mTitle = entries[i];
            item.mValue = values[i];
            item.mKey = key;
            item.mIsShowTitle = showTitle[i];
            item.mPosition = this.mITEM_GAP * i;
            if (entries[i].equals(defaultEntryValue)) {
                item.mSelected = true;
                this.mCurValuePosition = this.mStartPosition - (this.mITEM_GAP * i);
            }
            this.mDataList.add(item);
        }
    }

    public void refreshPositionValues() {
        if (this.mDataList != null && this.mDataList.size() != 0) {
            int dataSize = this.mDataList.size();
            this.mStartPosition = this.mDrumEndPosY;
            for (int i = 0; i < dataSize; i++) {
                DrumItem item = (DrumItem) this.mDataList.get(i);
                if (item != null) {
                    int newPosition = i * this.mITEM_GAP;
                    if (item.mPosition != newPosition) {
                        item.mPosition = newPosition;
                    }
                    if (item.mSelected) {
                        this.mCurValuePosition = this.mStartPosition - (this.mITEM_GAP * i);
                        this.mScrollPosition = this.mCurValuePosition;
                    }
                }
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        if (!this.mInit || canvas == null) {
            postInvalidate();
            return;
        }
        boolean isTouchMoving;
        if (!(this.mRotationInfo == null || this.mRotationInfo.getCurrentDegree() == this.mRotationInfo.getTargetDegree() || !this.mRotationInfo.calcCurrentDegree())) {
            invalidate();
        }
        drawBackground(canvas);
        calculateScrollPosition();
        if (this.mScrollState == 0 && this.mTouchState == 2) {
            isTouchMoving = true;
        } else {
            isTouchMoving = false;
        }
        int index = (this.mDrumEndPosY - this.mScrollPosition) / this.mITEM_GAP;
        if (index < 0) {
            index = 0;
        } else if (index >= this.mDataList.size()) {
            index = this.mDataList.size() - 1;
        }
        if (this.mCurrentSelectedIndex != index) {
            this.mCurrentSelectedIndex = index;
            if (isTouchMoving) {
                if (((DrumItem) this.mDataList.get(index)).mIsShowTitle) {
                    sendClickFeedback();
                }
                sendPerformClick(index, 0);
            }
        }
        this.mTickPaint.setStyle(Style.FILL);
        drawDrumBarTick(canvas, this.mScrollPosition);
        drawMainTick(canvas, this.mScrollPosition);
    }

    protected void drawCursor(Canvas canvas, int currentPosition) {
        canvas.drawBitmap(this.mCursor, (float) ((this.mDrumEndPosX - this.mDrumEndMargin) - this.mCursorWidth), ((float) currentPosition) - (((float) this.mCursorHeight) / 2.0f), this.mArrowIconPaint);
    }

    protected void drawMainTick(Canvas canvas, int position) {
        int bgMainBarStartX = this.mDrumStartPosX + this.mDrumStartMargin;
        int bgMainBarEndX = this.mDrumEndPosX - this.mDrumEndMargin;
        int tickBarWidthDiff = ((this.mCenterTickWidth + (this.mOuterMargin * 2)) - (bgMainBarEndX - bgMainBarStartX)) / 2;
        this.mCenterTickPaint.setColor(this.mTickPaintColor);
        this.mCenterTickPaint.setStyle(Style.FILL);
        canvas.drawRect((float) (bgMainBarStartX - tickBarWidthDiff), (float) ((position - this.mCenterTickHeight) - this.mOuterMargin), (float) (bgMainBarEndX + tickBarWidthDiff), (float) ((this.mCenterTickHeight + position) + this.mOuterMargin), this.mBarBgRect);
        canvas.drawRect((float) bgMainBarStartX, (float) (position - this.mCenterTickHeight), (float) bgMainBarEndX, (float) (this.mCenterTickHeight + position), this.mCenterTickPaint);
    }

    protected int getTickAlpha(int itemPosition) {
        int dataSize = this.mDataList.size();
        int itemAlpha = 0;
        for (int i = 0; i < dataSize; i++) {
            if (itemPosition >= this.mDrumStartPosY && itemPosition <= this.mDrumEndPosY) {
                if (isExistItemInScope(itemPosition, this.mCenterY, this.mSELECT_BOUND_POSITION)) {
                    itemAlpha = 255;
                } else if (isExistItemInScope(itemPosition, this.mCenterY, this.mSECOND_LEVEL_GAP)) {
                    itemAlpha = 180;
                } else {
                    itemAlpha = 100;
                }
            }
        }
        return itemAlpha;
    }

    protected void drawDrumBarTick(Canvas canvas, int currentPosition) {
        int tichHalfHeight = Math.max(1, (int) (this.mTickThick / 2.0f));
        int tickWidth = (int) this.mTickWidthL;
        this.mTickPaint.setColor(this.mTickPaintColor);
        for (int i = this.mDataList.size() - 1; i >= 0; i--) {
            DrumItem item = (DrumItem) this.mDataList.get(i);
            if (item.mIsShowTitle) {
                int itemPosition = currentPosition - item.mPosition;
                this.mTickPaint.setAlpha(getTickAlpha(itemPosition));
                if (itemPosition >= this.mDrumStartPosY && itemPosition <= this.mDrumEndPosY) {
                    canvas.drawRect((float) (this.mDrumStartPosX + this.mDrumStartMargin), (float) (itemPosition - tichHalfHeight), (float) ((this.mDrumStartPosX + this.mDrumStartMargin) + tickWidth), (float) (itemPosition + tichHalfHeight), this.mTickPaint);
                }
                itemPosition = currentPosition + item.mPosition;
                this.mTickPaint.setAlpha(getTickAlpha(itemPosition));
                if (itemPosition >= this.mDrumStartPosY && itemPosition <= this.mDrumEndPosY) {
                    canvas.drawRect((float) (this.mDrumStartPosX + this.mDrumStartMargin), (float) (itemPosition - tichHalfHeight), (float) ((this.mDrumStartPosX + this.mDrumStartMargin) + tickWidth), (float) (itemPosition + tichHalfHeight), this.mTickPaint);
                }
            }
        }
    }

    protected void drawBackground(Canvas canvas) {
        super.drawBackground(canvas);
        int iconLocX = (this.mDrumStartPosX - this.mIconHeight) - this.mIconPaddingEnd;
        int iconLocPaddingY = (int) Utils.dpToPx(getContext(), -8.0f);
        if (this.mDrumType == 16) {
            int rotDegree = 0;
            if (this.mRotationInfo != null) {
                rotDegree = this.mRotationInfo.getCurrentDegree();
            }
            this.mIcon.setAlpha(isEnabled() ? 255 : 102);
            canvas.rotate((float) (-rotDegree), (float) ((this.mIconWidth / 2) + iconLocX), (float) ((this.mDrumStartPosY + iconLocPaddingY) + (this.mIconHeight / 2)));
            canvas.drawBitmap(this.mIconInfinity, (float) iconLocX, (float) (this.mDrumStartPosY + iconLocPaddingY), this.mIcon);
            canvas.rotate((float) rotDegree, (float) ((this.mIconWidth / 2) + iconLocX), (float) ((this.mDrumStartPosY + iconLocPaddingY) + (this.mIconHeight / 2)));
            canvas.rotate((float) (-rotDegree), (float) ((this.mIconWidth / 2) + iconLocX), (float) (((this.mDrumEndPosY - this.mIconHeight) - iconLocPaddingY) + (this.mIconHeight / 2)));
            canvas.drawBitmap(this.mIconMacro, (float) iconLocX, (float) ((this.mDrumEndPosY - this.mIconHeight) - iconLocPaddingY), this.mIcon);
            canvas.rotate((float) rotDegree, (float) ((this.mIconWidth / 2) + iconLocX), (float) (((this.mDrumEndPosY - this.mIconHeight) - iconLocPaddingY) + (this.mIconHeight / 2)));
        }
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    protected boolean doTouchActionDown(MotionEvent event) {
        boolean z = false;
        this.mActivePointerId = event.getPointerId(0);
        if (this.mScrollState == 1) {
            this.mScrollState = 0;
            this.mCurValuePosition = this.mScrollPosition;
        }
        this.mTouchState = 1;
        this.mStartX = event.getX();
        this.mStartY = event.getY();
        this.mIsTouchArea = checkTouchArea((int) this.mStartX, (int) this.mStartY);
        if (this.mClickedItem != -100) {
            z = true;
        }
        this.mIsInItemArea = z;
        return true;
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        if (this.mTouchState == 0 || !this.mIsTouchArea) {
            this.mTouchState = 0;
            return false;
        }
        int pointerIndex = event.findPointerIndex(this.mActivePointerId);
        float distX = event.getX(pointerIndex) - this.mStartX;
        float distY = event.getY(pointerIndex) - this.mStartY;
        if (this.mScrollPosition <= this.mDrumStartPosY && distY < 0.0f) {
            this.mStartY = event.getY(pointerIndex);
            this.mCurValuePosition = this.mScrollPosition;
        } else if (this.mScrollPosition >= this.mDrumEndPosY && distY > 0.0f) {
            this.mStartY = event.getY(pointerIndex);
            this.mCurValuePosition = this.mScrollPosition;
        }
        if (Math.abs(distX) < this.mMinimumDistanceForDrumMovement && Math.abs(distY) < this.mMinimumDistanceForDrumMovement) {
            return true;
        }
        this.mTouchState = 2;
        onScrollPosition(this.mStartX, this.mStartY, distX, distY);
        return true;
    }

    protected boolean doTouchActionUp() {
        if (this.mTouchState == 0 || !this.mIsTouchArea) {
            this.mTouchState = 0;
            this.mStartX = 0.0f;
            this.mStartY = 0.0f;
            this.mActivePointerId = -1;
            return false;
        }
        if (this.mScrollState == 0) {
            releaseScrollPosition(true, true);
            this.mCurValuePosition = this.mScrollPosition;
        }
        this.mStartX = 0.0f;
        this.mStartY = 0.0f;
        this.mActivePointerId = -1;
        this.mTouchState = 0;
        return true;
    }

    protected void onScrollPosition(float x, float y, float distanceX, float distanceY) {
        if (checkScrollBoundary((double) distanceY)) {
            this.mScrollPosition = (int) (((float) this.mCurValuePosition) + distanceY);
            if (this.mScrollPosition >= this.mDrumEndPosY) {
                this.mScrollPosition = this.mDrumEndPosY;
            } else if (this.mScrollPosition <= this.mDrumStartPosY) {
                this.mScrollPosition = this.mDrumStartPosY;
            }
        }
        invalidate();
    }

    protected boolean checkScrollBoundary(double diff) {
        if (this.mScrollPosition >= this.mDrumEndPosY && diff >= 0.0d) {
            this.mScrollPosition += this.mDrumEndPosY - this.mScrollPosition;
            return false;
        } else if (this.mScrollPosition > this.mDrumStartPosY || diff > 0.0d) {
            return true;
        } else {
            this.mScrollPosition += this.mDrumStartPosY - this.mScrollPosition;
            return false;
        }
    }

    protected void releaseScrollPosition(boolean draw, boolean confirm) {
        int adjResult;
        CamLog.m3d(CameraConstants.TAG, "releaseScrollPosition - start : draw = " + draw + ", confirm = " + confirm);
        int scrollPosition = this.mScrollPosition;
        int adjPosition = scrollPosition % this.mITEM_GAP;
        if (adjPosition == 0) {
            adjResult = 0;
        } else {
            adjResult = adjPosition;
        }
        int symbol = adjPosition >= 0 ? 1 : -1;
        if (!(this.mScrollPosition == this.mDrumStartPosY || this.mScrollPosition == this.mDrumEndPosY)) {
            if (Math.abs(adjPosition) > Math.abs(this.mITEM_GAP / 2)) {
                adjResult = symbol * (this.mITEM_GAP - Math.abs(adjPosition));
                if (checkScrollBoundary((double) adjResult)) {
                    this.mScrollPosition = scrollPosition + adjResult;
                }
            } else if (checkScrollBoundary((double) adjResult)) {
                this.mScrollPosition = scrollPosition - adjResult;
            }
        }
        if (draw) {
            invalidate();
        }
        setSelectedItemByPosition();
        if (confirm) {
            sendConfirmSelection(0);
        }
    }

    public boolean moveDrumScrollStep(int step, long duration) {
        if (!this.mInit || getVisibility() != 0 || this.mDataList == null || this.mDataList.size() == 0) {
            return false;
        }
        this.mCurValuePosition = this.mScrollPosition;
        int diffPositon = (this.mDrumEndPosY + (this.mITEM_GAP * step)) - this.mCurValuePosition;
        if ((diffPositon > 0 ? 1 : -1) > 0) {
            if (this.mScrollPosition > this.mDrumEndPosY) {
                return true;
            }
        } else if (this.mScrollPosition < this.mDrumStartPosY) {
            return true;
        }
        this.mMovePosition = diffPositon;
        this.mStartTime = System.currentTimeMillis();
        this.mScrollDuration = (long) this.mDEFAULT_FLING_DURATION;
        this.mEndTime = this.mStartTime + this.mScrollDuration;
        this.mScrollState = 1;
        invalidate();
        return true;
    }

    protected void makeClickableAreaList() {
        if (this.mClickRegionMap != null) {
            for (int i = 0; i < this.mSHOWING_STEP; i++) {
                int clickableX = this.mDrumEndPosX - (this.mDRUM_WIDTH / 2);
                int clickableY = this.mDrumEndPosY - (i * this.mITEM_GAP);
                this.mClickRegionMap.put(i, new Rect(clickableX - this.mTextAreaMinClickWidth, clickableY - this.mITEM_GAP, this.mTextAreaMinClickWidth + clickableX, clickableY));
            }
        }
    }

    protected void calculateScrollPosition() {
        if (this.mScrollState == 1) {
            long curTime = System.currentTimeMillis();
            if (curTime <= this.mEndTime) {
                double interpolatedPosition = (double) (((float) this.mMovePosition) * this.mInterpolator.getInterpolation(Math.min(((float) (curTime - this.mStartTime)) / ((float) this.mScrollDuration), 1.0f)));
                if (checkScrollBoundary(interpolatedPosition)) {
                    this.mScrollPosition = (int) (((double) this.mCurValuePosition) + interpolatedPosition);
                } else {
                    this.mEndTime = 0;
                }
                invalidate();
                return;
            }
            this.mScrollState = 0;
            releaseScrollPosition(true, true);
            this.mCurValuePosition = this.mScrollPosition;
            int index = (this.mDrumEndPosY - this.mCurValuePosition) / this.mITEM_GAP;
            if (index < 0) {
                index = 0;
            } else if (index >= this.mDataList.size()) {
                index = this.mDataList.size() - 1;
            }
            this.mCurrentSelectedIndex = index;
            sendPerformClick(index, this.mPressedStateDuration);
        }
    }

    public void setSelectedItem(String value, boolean isDefault) {
        if (this.mDataList != null && this.mDataList.size() != 0 && value != null) {
            int dataSize = this.mDataList.size();
            for (int i = 0; i < dataSize; i++) {
                DrumItem item = (DrumItem) this.mDataList.get(i);
                if (item != null) {
                    item.mSelected = false;
                    if (value.equals(item.mValue)) {
                        item.mSelected = true;
                        this.mCurValuePosition = this.mDrumEndPosY - (this.mITEM_GAP * i);
                        this.mScrollPosition = this.mCurValuePosition;
                    }
                }
            }
            if (isDefault) {
                this.mCurValuePosition = 0;
                this.mScrollPosition = 0;
            }
        }
    }

    protected void setSelectedItemByPosition() {
        if (this.mInit && this.mDataList != null && this.mDataList.size() != 0) {
            int dataSize = this.mDataList.size();
            int idx = (this.mDrumEndPosY - this.mScrollPosition) / this.mITEM_GAP;
            if (idx < 0) {
                idx = 0;
            }
            if (idx > dataSize - 1) {
                idx = dataSize - 1;
            }
            for (int i = 0; i < dataSize; i++) {
                DrumItem item = (DrumItem) this.mDataList.get(i);
                if (i == idx) {
                    item.mSelected = true;
                } else {
                    item.mSelected = false;
                }
            }
        }
    }

    public DrumItem getCurSelectedItem() {
        if (!this.mInit || this.mDataList == null || this.mDataList.size() == 0) {
            return null;
        }
        int index = (this.mDrumEndPosY - this.mScrollPosition) / this.mITEM_GAP;
        if (index < 0) {
            index = 0;
        } else if (index >= this.mDataList.size()) {
            index = this.mDataList.size() - 1;
        }
        return (DrumItem) this.mDataList.get(index);
    }

    public void unbind() {
        super.unbind();
        this.mIconInfinity = null;
        this.mIconMacro = null;
        this.mCursor = null;
    }
}
