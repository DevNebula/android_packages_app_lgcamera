package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.support.p000v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.file.ExifInterface.GpsSpeedRef;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public class DrumControllerBase extends View implements OnGestureListener, OnTouchListener {
    protected static final int CLICK_DELAY = 250;
    protected static final int CONFIRM_SEL_DELAY = 300;
    protected static final int DISABLED_COLOR = Color.argb(102, 255, 255, 255);
    protected static final int ICON_DISABLED_ALPHA = 102;
    protected static final int ICON_ENABLED_ALPHA = 255;
    protected static final int INVALID_CLICK_ITEM = -100;
    protected static final int INVALID_POINTER = -1;
    protected static final int NORMAL_COLOR = -1;
    protected static final int SCROLL_IDLE = 0;
    protected static final int SCROLL_SCROLLING = 1;
    protected static final int SELECTED_COLOR = Color.argb(255, 255, 242, 0);
    protected static final int TICK_ALPHA_L = 230;
    protected static final int TICK_ALPHA_S = 72;
    protected static final int TICK_COLOR = -1;
    protected static final int TOUCH_DONW = 1;
    protected static final int TOUCH_IDLE = 0;
    protected static final int TOUCH_MOVING = 2;
    protected static final int UNSELECTED = -1;
    protected int mActivePointerId = -1;
    protected final Paint mArrowIconPaint = new Paint(1);
    protected final Paint mBarBgRect = new Paint(1);
    protected int mBaseTextSize = 0;
    protected int mCENTER_SELECTED_WIDTH = 0;
    protected int mCenterTextSize = 0;
    protected int mCenterTickHeight = 0;
    protected final Paint mCenterTickPaint = new Paint(1);
    protected int mCenterTickWidth = 0;
    protected int mCenterY = 0;
    protected Runnable mClickFeedback = new C05362();
    protected SparseArray<Rect> mClickRegionMap = new SparseArray();
    protected int mClickedItem = -100;
    protected Runnable mConfirmSelection = new C05351();
    protected int mCurValuePosition = 0;
    protected int mDEFAULT_FLING_DURATION = 150;
    protected int mDRUM_HEIGHT = 0;
    protected int mDRUM_WIDTH = 0;
    protected final ArrayList<DrumItem> mDataList = new ArrayList();
    protected float mDeceleration = 0.0f;
    protected int mDrumEndMargin = 0;
    protected int mDrumEndPosX = 0;
    protected int mDrumEndPosY = 0;
    protected int mDrumStartMargin = 0;
    protected int mDrumStartPosX = 0;
    protected int mDrumStartPosY = 0;
    protected int mDrumTouchInnerPadding = 0;
    protected int mDrumTouchPadding = 0;
    protected int mDrumType = 0;
    protected long mEndTime = 0;
    protected GestureDetectorCompat mGestureDetector = null;
    protected GradientDrawable mGradiantBg;
    protected int mHalfHeight = 0;
    protected int mITEM_GAP = 0;
    protected final Paint mIconPaint = new Paint(1);
    protected int mIconSize = 0;
    protected boolean mInit = false;
    protected DecelerateInterpolator mInterpolator = new DecelerateInterpolator(2.0f);
    protected boolean mIsInItemArea = false;
    protected boolean mIsNotNeedToCorrectPosition = false;
    protected boolean mIsTouchArea;
    protected boolean mIsVisibilityChanging = false;
    protected int mLcdHeight = 0;
    public DrumControllerListener mListener = null;
    protected float mMinimumDistanceForDrumMovement = 0.0f;
    protected int mMovePosition;
    protected int mOuterMargin = 0;
    protected int mParamWaitingCount;
    protected PerformClick mPerformClick = new PerformClick();
    protected int mPressedStateDuration = 0;
    protected RotationInfo mRotationInfo = new RotationInfo();
    protected int mSECOND_LEVEL_GAP = 0;
    protected int mSELECT_BOUND_POSITION = 0;
    protected int mSHOWING_STEP = 6;
    protected long mScrollDuration = 0;
    protected int mScrollPosition = 0;
    protected int mScrollState = 0;
    protected float mShadowRadius = 0.0f;
    protected int mStartPosition = 0;
    protected int mStartPostion;
    protected long mStartTime = 0;
    protected float mStartX = 0.0f;
    protected float mStartY = 0.0f;
    protected int mTHIRD_LEVEL_GAP = 0;
    protected int mTPaintColor = -1;
    protected int mTextAreaMinClickWidth = 0;
    protected int mTextAreaMinWidth = 0;
    protected int mTextPadding = 0;
    protected int mTextPaddingSelected = 0;
    protected final Rect mTextRect = new Rect();
    protected final TextPaint mTextpaint = new TextPaint(1);
    protected final Paint mTickPaint = new Paint(1);
    protected int mTickPaintColor = -1;
    protected float mTickThick = 0.0f;
    protected float mTickWidthL = 0.0f;
    protected float mTickWidthS = 0.0f;
    protected int mTopDownBarWidth = 0;
    protected int mTouchSlop = 0;
    protected int mTouchState = 0;
    protected int mViewDegree = 0;
    protected GradientDrawable mWBColorBg;
    protected int mWB_BAR_WIDTH = 0;

    /* renamed from: com.lge.camera.components.DrumControllerBase$1 */
    class C05351 implements Runnable {
        C05351() {
        }

        public void run() {
            if (DrumControllerBase.this.mListener != null) {
                DrumItem item = DrumControllerBase.this.getCurSelectedItem();
                if (item != null && item.getTitle() != null) {
                    DrumControllerBase.this.sendPerformClick(DrumControllerBase.this.mPressedStateDuration);
                    DrumControllerBase.this.mListener.onDrumScrollReleased(item);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.components.DrumControllerBase$2 */
    class C05362 implements Runnable {
        C05362() {
        }

        public void run() {
            AudioUtil.performHapticFeedback(DrumControllerBase.this, 65542);
            if (DrumControllerBase.this.mListener != null) {
                DrumControllerBase.this.mListener.playDrumEffectSound();
            }
        }
    }

    /* renamed from: com.lge.camera.components.DrumControllerBase$3 */
    class C05373 implements Runnable {
        C05373() {
        }

        public void run() {
            DrumControllerBase drumControllerBase = DrumControllerBase.this;
            drumControllerBase.mParamWaitingCount--;
        }
    }

    protected class PerformClick implements Runnable {
        protected int mSelectedIndex = -1;
        protected String mTalkbackString;

        protected PerformClick() {
        }

        protected void setSeletedIndex(int index) {
            this.mSelectedIndex = index;
        }

        protected void release() {
            this.mSelectedIndex = -1;
        }

        public void run() {
            DrumItem item;
            boolean updateDrum = false;
            if (this.mSelectedIndex == -1) {
                item = DrumControllerBase.this.getCurSelectedItem();
            } else {
                item = DrumControllerBase.this.getDrumItem(this.mSelectedIndex);
            }
            if (item != null && DrumControllerBase.this.mListener != null) {
                if (DrumControllerBase.this.mScrollState == 0) {
                    String desc = item.mTitle;
                    if (DrumControllerBase.this.mDrumType == 4) {
                        desc = item.mTitle.split(GpsSpeedRef.KILOMETERS)[0] + " K";
                    }
                    if (!desc.equals(this.mTalkbackString)) {
                        DrumControllerBase.this.setContentDescription(desc);
                        DrumControllerBase.this.sendAccessibilityEvent(16384);
                    }
                    this.mTalkbackString = desc;
                }
                if (DrumControllerBase.this.getVisibility() == 0) {
                    updateDrum = true;
                }
                DrumControllerBase.this.mListener.onItemSelected(item, updateDrum);
                this.mSelectedIndex = -1;
            }
        }
    }

    public DrumControllerBase(Context context) {
        super(context);
    }

    public DrumControllerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrumControllerBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onDraw(Canvas canvas) {
        if (!this.mInit || canvas == null) {
            postInvalidate();
            return;
        }
        this.mTickPaint.setStyle(Style.FILL);
        this.mTickPaint.setColor(this.mTickPaintColor);
        calculateScrollPosition();
        if (!(this.mRotationInfo == null || this.mRotationInfo.getCurrentDegree() == this.mRotationInfo.getTargetDegree() || !this.mRotationInfo.calcCurrentDegree())) {
            invalidate();
        }
        drawBackground(canvas);
        drawDrum(canvas);
        drawMainTick(canvas, this.mCenterY);
        if (this.mIsNotNeedToCorrectPosition) {
            this.mIsNotNeedToCorrectPosition = false;
        }
    }

    protected void initDrumBgDimension() {
        this.mTopDownBarWidth = Math.round(Utils.dpToPx(getContext(), 9.25f));
        this.mOuterMargin = Math.round(Utils.dpToPx(getContext(), 0.75f));
        this.mCenterTickWidth = Math.round(Utils.dpToPx(getContext(), 25.5f));
        this.mCenterTickHeight = Math.round(Utils.dpToPx(getContext(), 1.5f));
    }

    protected void drawBackground(Canvas canvas) {
        this.mBarBgRect.setColor(Color.argb(38, 0, 0, 0));
        this.mBarBgRect.setStyle(Style.FILL);
        this.mGradiantBg.setBounds(this.mDrumStartPosX + this.mDrumStartMargin, this.mDrumStartPosY, this.mDrumEndPosX - this.mDrumEndMargin, this.mDrumEndPosY);
        this.mGradiantBg.draw(canvas);
    }

    protected void drawDrum(Canvas canvas) {
        boolean isTouchMoving;
        int dataSize = this.mDataList.size();
        if (this.mScrollState == 0 && this.mTouchState == 2) {
            isTouchMoving = true;
        } else {
            isTouchMoving = false;
        }
        for (int i = 0; i < dataSize; i++) {
            canvas.save();
            DrumItem item = (DrumItem) this.mDataList.get(i);
            int itemPosition = item.mPosition + this.mScrollPosition;
            drawWBBar(canvas, itemPosition, item);
            if (itemPosition < this.mDrumStartPosY || itemPosition > this.mDrumEndPosY) {
                item.mSelected = false;
            } else {
                int itemAlpha;
                if (isExistItemInScope(itemPosition, this.mCenterY, this.mSELECT_BOUND_POSITION)) {
                    itemAlpha = 255;
                    if (!item.mSelected) {
                        if (!this.mIsNotNeedToCorrectPosition) {
                            sendClickFeedback();
                        }
                        if (isTouchMoving) {
                            sendPerformClick(i, 250);
                        }
                    }
                    item.mSelected = true;
                } else if (isExistItemInScope(itemPosition, this.mCenterY, this.mSECOND_LEVEL_GAP)) {
                    itemAlpha = 230;
                    item.mSelected = false;
                } else if (isExistItemInScope(itemPosition, this.mCenterY, this.mTHIRD_LEVEL_GAP)) {
                    itemAlpha = 178;
                    item.mSelected = false;
                } else {
                    itemAlpha = 128;
                    item.mSelected = false;
                }
                drawDrumContents(canvas, item, itemPosition, itemAlpha);
            }
            canvas.restore();
        }
    }

    protected boolean isExistItemInScope(int itemPostion, int basePostion, int boundaryPostion) {
        if (itemPostion < basePostion - boundaryPostion || itemPostion > basePostion + boundaryPostion) {
            return false;
        }
        return true;
    }

    protected void drawDrumContents(Canvas canvas, DrumItem item, int itemPosition, int itemAlpha) {
        int tickAlpha = itemAlpha;
        float tickThickness = this.mTickThick;
        float tickWidth = item.mIsShowTitle ? this.mTickWidthL : this.mTickWidthS;
        if (!item.mIsShowTitle) {
            tickAlpha /= 2;
        }
        tickThickness = item.mIsShowTitle ? this.mTickThick : this.mTickThick / 2.0f;
        this.mTickPaint.setAlpha(tickAlpha);
        canvas.drawRect((float) (this.mDrumStartPosX + this.mDrumStartMargin), ((float) itemPosition) - tickThickness, ((float) (this.mDrumStartPosX + this.mDrumStartMargin)) + tickWidth, ((float) itemPosition) + tickThickness, this.mTickPaint);
        drawText(canvas, item, itemPosition, itemAlpha);
        drawExtras(canvas, item, itemPosition, itemAlpha);
    }

    private void drawExtras(Canvas canvas, DrumItem item, int itemPosition, int itemAlpha) {
        Bitmap icon = null;
        boolean hasIcon = false;
        if (item.mResId > 0) {
            icon = BitmapManagingUtil.getBitmap(this.mContext, item.mResId);
            if (icon != null) {
                hasIcon = true;
            }
        }
        if (hasIcon && icon != null) {
            if (item.mSelected) {
                this.mIconPaint.setColorFilter(ColorUtil.getColorMatrix(255.0f, 255.0f, 255.0f, 0.0f));
            } else {
                this.mIconPaint.setColorFilter(ColorUtil.getNormalColorByAlpha());
            }
            if (isEnabled()) {
                this.mIconPaint.setAlpha(itemAlpha);
            } else {
                this.mIconPaint.setAlpha(102);
            }
            int bitmapStartPosX = ((this.mDrumStartPosX - this.mTextRect.width()) - (item.mSelected ? this.mTextPaddingSelected : this.mTextPadding)) - this.mIconSize;
            int rotDegree = 0;
            if (this.mRotationInfo != null) {
                rotDegree = this.mRotationInfo.getCurrentDegree();
            }
            canvas.rotate((float) (-rotDegree), (float) ((this.mIconSize / 2) + bitmapStartPosX), (float) itemPosition);
            canvas.drawBitmap(icon, (float) bitmapStartPosX, (float) (itemPosition - (this.mIconSize / 2)), this.mIconPaint);
            canvas.rotate((float) rotDegree, (float) ((this.mIconSize / 2) + bitmapStartPosX), (float) itemPosition);
        }
    }

    private void drawText(Canvas canvas, DrumItem item, int itemPosition, int itemAlpha) {
        int textColor = Color.argb(itemAlpha, 255, 255, 255);
        int textSize = this.mBaseTextSize;
        int textPadding = item.mSelected ? this.mTextPaddingSelected : this.mTextPadding;
        if (item.mSelected) {
            textColor = SELECTED_COLOR;
            textSize = (int) Utils.dpToPx(getContext(), 13.0f);
        }
        this.mTextpaint.setColor(textColor);
        this.mTextpaint.setStyle(Style.FILL);
        this.mTextpaint.setFakeBoldText(true);
        this.mTextpaint.setTextAlign(Align.CENTER);
        this.mTextpaint.setTextSize((float) textSize);
        this.mTextpaint.getTextBounds(item.mTitle, 0, item.mTitle.length(), this.mTextRect);
        float textDrawPosX = (float) ((this.mDrumStartPosX - (this.mTextRect.width() / 2)) - textPadding);
        float textDrawPosY = ((float) this.mTextRect.height()) / 2.0f;
        if (item.mIsShowTitle) {
            int rotDegree = 0;
            if (this.mRotationInfo != null) {
                rotDegree = this.mRotationInfo.getCurrentDegree();
            }
            canvas.rotate((float) (-rotDegree), textDrawPosX, (float) itemPosition);
            canvas.drawText(item.mTitle, textDrawPosX, ((float) itemPosition) + textDrawPosY, this.mTextpaint);
            this.mTextpaint.setColor(Color.argb(128, 0, 0, 0));
            this.mTextpaint.setStyle(Style.STROKE);
            this.mTextpaint.setStrokeWidth(Utils.dpToPx(getContext(), 0.5f));
            canvas.drawText(item.mTitle, textDrawPosX, ((float) itemPosition) + textDrawPosY, this.mTextpaint);
            canvas.rotate((float) rotDegree, textDrawPosX, (float) itemPosition);
        }
    }

    private void drawWBBar(Canvas canvas, int itemPosition, DrumItem item) {
        if (this.mDrumType == 4) {
            int colorBarStartY = itemPosition - this.mITEM_GAP;
            int colorBarEndY = itemPosition;
            int colorBarEndX = this.mDrumEndPosX - this.mDrumEndMargin;
            int wbTopBottomBuffer = this.mOuterMargin;
            if (colorBarStartY >= this.mDrumStartPosY - this.mITEM_GAP && colorBarStartY <= this.mDrumStartPosY) {
                colorBarStartY = this.mDrumStartPosY + this.mOuterMargin;
                colorBarEndY = itemPosition;
            }
            if (colorBarEndY >= this.mDrumEndPosY && colorBarEndY <= this.mDrumEndPosY + this.mITEM_GAP) {
                colorBarStartY = itemPosition - this.mITEM_GAP;
                colorBarEndY = this.mDrumEndPosY - this.mOuterMargin;
            }
            if (colorBarStartY >= this.mDrumStartPosY + wbTopBottomBuffer && colorBarEndY <= this.mDrumEndPosY - wbTopBottomBuffer) {
                this.mWBColorBg.setGradientType(0);
                this.mWBColorBg.setOrientation(Orientation.BOTTOM_TOP);
                this.mWBColorBg.setColors(new int[]{item.mStartBarColor, item.mEndBarColor});
                this.mWBColorBg.setBounds(colorBarEndX - this.mWB_BAR_WIDTH, colorBarStartY, colorBarEndX, colorBarEndY);
                this.mWBColorBg.draw(canvas);
            }
        }
    }

    protected void drawMainTick(Canvas canvas, int position) {
        int bgMainBarStartX = this.mDrumStartPosX + this.mDrumStartMargin;
        int bgMainBarEndX = this.mDrumEndPosX - this.mDrumEndMargin;
        int tickBarWidthDiff = ((this.mCenterTickWidth + (this.mOuterMargin * 2)) - (bgMainBarEndX - bgMainBarStartX)) / 2;
        this.mCenterTickPaint.setColor(SELECTED_COLOR);
        this.mCenterTickPaint.setStyle(Style.FILL);
        canvas.drawRect((float) (bgMainBarStartX - tickBarWidthDiff), (float) ((position - this.mCenterTickHeight) - this.mOuterMargin), (float) (bgMainBarEndX + tickBarWidthDiff), (float) ((this.mCenterTickHeight + position) + this.mOuterMargin), this.mBarBgRect);
        canvas.drawRect((float) ((bgMainBarStartX - tickBarWidthDiff) + this.mOuterMargin), (float) (position - this.mCenterTickHeight), (float) ((bgMainBarEndX + tickBarWidthDiff) - this.mOuterMargin), (float) (this.mCenterTickHeight + position), this.mCenterTickPaint);
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
        if (this.mIsTouchArea) {
            int moveSymbol;
            float durAdj = this.mDrumType == 4 ? 0.3f : 0.15f;
            if (velocityY < 0.0f) {
                moveSymbol = -1;
            } else {
                moveSymbol = 1;
            }
            int absVelocity = Math.round(Math.abs(velocityY));
            int duration = Math.round(((1000.0f * durAdj) * ((float) absVelocity)) / this.mDeceleration);
            int scrollStep = moveSymbol * Math.round(((float) absVelocity) / this.mDeceleration);
            if (this.mDrumType == 4 && ((float) absVelocity) > 5000.0f) {
                scrollStep *= 3;
            }
            if (Math.abs(scrollStep) >= 1) {
                moveDrumScrollStep(scrollStep, (long) duration);
            }
        } else {
            this.mTouchState = 0;
        }
        return false;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (!this.mInit || getVisibility() != 0 || this.mDataList == null || this.mDataList.size() == 0 || !isEnabled()) {
            return false;
        }
        if (this.mListener != null && !this.mListener.isAvailableToMoveDrum()) {
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
        this.mClickedItem = checkItemClickArea((int) this.mStartX, (int) this.mStartY);
        if (this.mClickedItem != -100) {
            z = true;
        }
        this.mIsInItemArea = z;
        return true;
    }

    protected boolean doTouchActionMove(MotionEvent event) {
        boolean z = false;
        if (this.mTouchState == 0 || !this.mIsTouchArea) {
            this.mTouchState = 0;
            return false;
        }
        int pointerIndex = event.findPointerIndex(this.mActivePointerId);
        float distX = event.getX(pointerIndex) - this.mStartX;
        float distY = event.getY(pointerIndex) - this.mStartY;
        if (Math.abs(distX) < this.mMinimumDistanceForDrumMovement && Math.abs(distY) < this.mMinimumDistanceForDrumMovement) {
            return true;
        }
        this.mTouchState = 2;
        if (this.mIsInItemArea) {
            boolean z2;
            if (this.mClickedItem == checkItemClickArea((int) event.getX(pointerIndex), (int) event.getY(pointerIndex))) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mIsInItemArea = z2;
            if (this.mIsInItemArea) {
                if (Math.abs(distY) < ((float) this.mTouchSlop)) {
                    z = true;
                }
                this.mIsInItemArea = z;
            }
        } else {
            this.mClickedItem = -100;
        }
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
        if (!this.mIsInItemArea) {
            return true;
        }
        moveDrumScrollStep(-this.mClickedItem, (long) (this.mDEFAULT_FLING_DURATION * Math.abs(this.mClickedItem)));
        return true;
    }

    private boolean doTouchActionPointerUp(MotionEvent event) {
        int newPointerIndex = 0;
        if (this.mIsTouchArea) {
            int pointerIndexs = (event.getAction() & 65280) >> 8;
            if (event.getPointerId(pointerIndexs) == this.mActivePointerId) {
                if (pointerIndexs == 0) {
                    newPointerIndex = 1;
                }
                this.mActivePointerId = event.getPointerId(newPointerIndex);
            }
            return true;
        }
        this.mTouchState = 0;
        return false;
    }

    protected boolean checkTouchArea(int x, int y) {
        int extraTouchAreaWidth = this.mDRUM_WIDTH * 2;
        int extraTouchAreaHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getContext(), false, 0.03125f);
        if (x < this.mDrumStartPosX - extraTouchAreaWidth || x > this.mDrumEndPosX + this.mDRUM_WIDTH || y < this.mDrumStartPosY || y > (this.mDrumStartPosY + this.mDRUM_HEIGHT) + extraTouchAreaHeight) {
            return false;
        }
        return true;
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
            sendPerformClick(this.mPressedStateDuration);
        }
    }

    protected void onScrollPosition(float x, float y, float distanceX, float distanceY) {
        if (checkScrollBoundary((double) distanceY)) {
            this.mScrollPosition = (int) (((float) this.mCurValuePosition) + distanceY);
        }
        invalidate();
    }

    public boolean moveDrumScrollStep(int step, long duration) {
        int adjResult = 0;
        if (!this.mInit || getVisibility() != 0 || this.mDataList == null || this.mDataList.size() == 0) {
            return false;
        }
        if (step >= 0) {
            if (((DrumItem) this.mDataList.get(0)).mPosition + this.mScrollPosition < this.mCenterY) {
                return true;
            }
        } else if (((DrumItem) this.mDataList.get(this.mDataList.size() - 1)).mPosition + this.mScrollPosition > this.mCenterY) {
            return true;
        }
        this.mCurValuePosition = this.mScrollPosition;
        int adjPosition = this.mScrollPosition % this.mITEM_GAP;
        if (adjPosition != 0) {
            adjResult = adjPosition;
        }
        this.mMovePosition = (this.mITEM_GAP * step) - adjResult;
        this.mStartTime = System.currentTimeMillis();
        this.mScrollDuration = Math.max(duration, (long) this.mDEFAULT_FLING_DURATION);
        this.mEndTime = this.mStartTime + this.mScrollDuration;
        this.mScrollState = 1;
        invalidate();
        return true;
    }

    protected boolean checkScrollBoundary(double diff) {
        if (this.mDataList == null || this.mDataList.size() == 0) {
            return true;
        }
        DrumItem item = (DrumItem) this.mDataList.get(this.mDataList.size() - 1);
        if (item.mPosition + this.mScrollPosition < this.mCenterY || diff < 0.0d) {
            item = (DrumItem) this.mDataList.get(0);
            if (item.mPosition + this.mScrollPosition > this.mCenterY || diff > 0.0d) {
                return true;
            }
            this.mScrollPosition += (this.mCenterY - item.mPosition) - this.mScrollPosition;
            return false;
        }
        this.mScrollPosition += (this.mCenterY - item.mPosition) - this.mScrollPosition;
        return false;
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
        if (Math.abs(adjPosition) > Math.abs(this.mITEM_GAP / 2)) {
            adjResult = symbol * (this.mITEM_GAP - Math.abs(adjPosition));
            if (checkScrollBoundary((double) adjResult)) {
                this.mScrollPosition = scrollPosition + adjResult;
            }
        } else if (checkScrollBoundary((double) adjResult)) {
            this.mScrollPosition = scrollPosition - adjResult;
        }
        if (draw) {
            invalidate();
        } else {
            setSelectedItemByPosition();
        }
        if (confirm) {
            sendConfirmSelection(300);
        }
    }

    protected void setSelectedItemByPosition() {
        if (this.mInit && this.mDataList != null && this.mDataList.size() != 0) {
            int dataSize = this.mDataList.size();
            for (int i = 0; i < dataSize; i++) {
                DrumItem item = (DrumItem) this.mDataList.get(i);
                int diffPosition = item.mPosition - this.mScrollPosition;
                if (diffPosition <= this.mCenterY - diffPosition || diffPosition >= this.mCenterY + diffPosition) {
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
            waitingParamHandling((int) delay);
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
            waitingParamHandling(duration);
        }
    }

    protected void sendPerformClick(int selectedIndex, int duration) {
        if (this.mPerformClick != null) {
            removePerformClick();
            this.mPerformClick.setSeletedIndex(selectedIndex);
            postDelayed(this.mPerformClick, (long) duration);
            waitingParamHandling(duration);
        }
    }

    protected void removePerformClick() {
        if (this.mPerformClick != null) {
            removeCallbacks(this.mPerformClick);
            this.mPerformClick.release();
        }
    }

    protected void waitingParamHandling(int delay) {
        this.mParamWaitingCount++;
        new Handler().postDelayed(new C05373(), (long) (delay + 20));
    }

    public DrumItem getCurSelectedItem() {
        return null;
    }

    public DrumItem getDrumItem(int index) {
        return null;
    }
}
