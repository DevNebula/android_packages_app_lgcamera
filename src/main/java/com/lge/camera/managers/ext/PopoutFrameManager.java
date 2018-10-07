package com.lge.camera.managers.ext;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.lge.camera.C0088R;
import com.lge.camera.components.PIPResizeHandlerView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class PopoutFrameManager extends ManagerInterfaceImpl {
    private static final int MIN_MOVEMENT_THRESHOLD = 15;
    protected int mCurrentFrameType = 1;
    protected int mExtraInvisibleMargin = 0;
    private GestureDetector mGestureDetector = null;
    protected boolean mIsNormalPreviewMoving = false;
    protected boolean mIsResizeHandlerShown = false;
    private boolean mIsResizeHandlerShownBefore;
    protected int[] mLcdSize;
    private onPopoutFrameListener mListener;
    protected int mMaxNormalPreviewHeight = 0;
    protected float mMaxNormalPreviewRatio = 1.0f;
    protected int mMaxNormalPreviewWidth = 0;
    protected int mMinNormalPreviewHeight = 0;
    protected float mMinNormalPreviewRatio = 0.25f;
    protected int mMinNormalPreviewWidth = 0;
    private int mMoveDirection;
    protected Rect mMovingRect = new Rect();
    protected RectF mNormalPreviewPosition = new RectF();
    protected PIPResizeHandlerView mPIPResizeHandlerView;
    private int mPrevX = 0;
    private int mPrevY = 0;
    protected float mPreviewAspect = 1.7777778f;
    protected int[] mPreviewSizeOnScreen;
    private int mStartX = 0;
    private int mStartY = 0;
    protected Rect mStartingRect = new Rect();
    protected Rect mTempRect = new Rect();
    /* renamed from: mX */
    private int f35mX;
    /* renamed from: mY */
    private int f36mY;

    public interface onPopoutFrameListener {
        void onFrameLongPressed();

        void onFrameMovingDone(RectF rectF, boolean z);
    }

    private class PIPGestureDetector extends SimpleOnGestureListener {
        private PIPGestureDetector() {
        }

        public void onLongPress(MotionEvent e) {
            CamLog.m3d(CameraConstants.TAG, "onLongPress");
            if (PopoutFrameManager.this.mGet.checkModuleValidate(15) && !PopoutFrameManager.this.isDivisionFrameType() && PopoutFrameManager.this.isInNormalPreviewTouch(PopoutFrameManager.this.mNormalPreviewPosition, (int) e.getX(), (int) e.getY())) {
                if (!PopoutFrameManager.this.mIsResizeHandlerShownBefore) {
                    PopoutFrameManager.this.showSubWindowResizeHandler(PopoutFrameManager.this.mNormalPreviewPosition);
                }
                PopoutFrameManager.this.mPIPResizeHandlerView.updatePosition(15);
                PopoutFrameManager.this.doTouchActionDown();
                PopoutFrameManager.this.mMovingRect.set(PopoutFrameManager.this.mStartingRect);
                if (PopoutFrameManager.this.mListener != null) {
                    PopoutFrameManager.this.mListener.onFrameLongPressed();
                }
            }
        }
    }

    public void setOnPopoutFrameListener(onPopoutFrameListener listener) {
        this.mListener = listener;
    }

    public PopoutFrameManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        this.mPIPResizeHandlerView = new PIPResizeHandlerView(this.mGet.getAppContext());
        this.mPreviewSizeOnScreen = Utils.getLCDsize(getAppContext(), true);
        this.mLcdSize = Utils.getLCDsize(getAppContext(), true);
    }

    public void initDefaultResizeHandlerPosition(RectF boundRect) {
        if (this.mPIPResizeHandlerView != null) {
            setResizeHandlerPosition(boundRect);
            this.mPIPResizeHandlerView.updatePosition(0);
            this.mMovingRect.set(this.mPIPResizeHandlerView.getCurrentPosition());
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mGestureDetector != null && this.mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        this.f35mX = (int) event.getX();
        this.f36mY = (int) event.getY();
        CamLog.m3d(CameraConstants.TAG, "onTouchEvent, x : " + this.f35mX + ", y : " + this.f36mY);
        switch (event.getAction()) {
            case 0:
                CamLog.m7i(CameraConstants.TAG, "onTouchEvent - ACTION DOWN");
                return doTouchActionDown();
            case 1:
                CamLog.m7i(CameraConstants.TAG, "onTouchEvent - ACTION UP");
                return doTouchActionUp();
            case 2:
                CamLog.m7i(CameraConstants.TAG, "onTouchEvent - ACTION MOVE");
                return doTouchActionMove();
            default:
                return false;
        }
    }

    public int checkResizeDirection(int x, int y) {
        return checkResizeDirectionWithMargin(x, y, 80, 80);
    }

    private int checkResizeDirectionWithMargin(int x, int y, int innerMargin, int outerMargin) {
        int[] position = new int[]{this.mStartingRect.left, this.mStartingRect.top, this.mStartingRect.right, this.mStartingRect.bottom};
        int x0 = position[0];
        int y0 = position[1];
        int x1 = position[2];
        int y1 = position[3];
        int retVal = 15;
        if (x < x0 - outerMargin || x > x1 + outerMargin || y < y0 - outerMargin || y > y1 + outerMargin) {
            retVal = 0;
        }
        return checkResizeDirectionTopOrBottom(y, y0, y1, innerMargin, outerMargin, checkResizeDirectionLeftOrRight(x, x0, x1, innerMargin, outerMargin, retVal));
    }

    private int checkResizeDirectionLeftOrRight(int x, int x0, int x1, int innerMargin, int outerMargin, int retVal) {
        if (retVal == 0) {
            return retVal;
        }
        if (x < x0 + innerMargin && x > x0 - outerMargin) {
            return 1;
        }
        if (x <= x1 - innerMargin || x >= x1 + outerMargin) {
            return retVal;
        }
        return 4;
    }

    private int checkResizeDirectionTopOrBottom(int y, int y0, int y1, int innerMargin, int outerMargin, int retVal) {
        if (retVal == 0) {
            return retVal;
        }
        if (y < y0 + innerMargin && y > y0 - outerMargin) {
            return retVal | 2;
        }
        if (y <= y1 - innerMargin || y >= y1 + outerMargin) {
            return retVal;
        }
        return retVal | 8;
    }

    private void fitToRatio(Rect rect, int diffX, int diffY, int direction) {
        float prevewAspect = isSquareFrameType() ? 1.0f : this.mPreviewAspect;
        if (diffY * diffY > diffX * diffX) {
            if ((direction & 1) != 0) {
                rect.left = (int) (((float) rect.right) - (((float) rect.height()) / prevewAspect));
            } else {
                rect.right = (int) (((float) rect.left) + (((float) rect.height()) / prevewAspect));
            }
        } else if ((direction & 2) != 0) {
            rect.top = (int) (((float) rect.bottom) - (((float) rect.width()) * prevewAspect));
        } else {
            rect.bottom = (int) (((float) rect.top) + (((float) rect.width()) * prevewAspect));
        }
    }

    private boolean doTouchActionDown() {
        this.mPrevX = this.f35mX;
        this.mPrevY = this.f36mY;
        this.mStartX = this.f35mX;
        this.mStartY = this.f36mY;
        this.mIsResizeHandlerShownBefore = this.mIsResizeHandlerShown;
        if (!this.mIsResizeHandlerShown) {
            return false;
        }
        this.mIsNormalPreviewMoving = true;
        this.mStartingRect.set(this.mPIPResizeHandlerView.getCurrentPosition());
        this.mMoveDirection = checkResizeDirection(this.f35mX, this.f36mY);
        return true;
    }

    private boolean doTouchActionMove() {
        int i = 0;
        boolean retVal = false;
        int diffX = this.f35mX - this.mPrevX;
        int diffY = this.f36mY - this.mPrevY;
        if (this.mIsNormalPreviewMoving) {
            if (this.mMoveDirection == 15) {
                hideResizeHandlerOnMove();
                this.mMovingRect.left = this.mStartingRect.left + diffX;
                this.mMovingRect.right = this.mStartingRect.right + diffX;
                this.mMovingRect.top = this.mStartingRect.top + diffY;
                this.mMovingRect.bottom = this.mStartingRect.bottom + diffY;
                checkScreenBoundary();
                updateNormalPreview(true);
            } else if (this.mMoveDirection == 0) {
                this.mMovingRect.set(this.mStartingRect);
            } else {
                int i2;
                this.mTempRect.set(this.mMovingRect);
                this.mTempRect.left = ((this.mMoveDirection & 1) != 0 ? diffX : 0) + this.mStartingRect.left;
                Rect rect = this.mTempRect;
                int i3 = this.mStartingRect.right;
                if ((this.mMoveDirection & 4) != 0) {
                    i2 = diffX;
                } else {
                    i2 = 0;
                }
                rect.right = i2 + i3;
                rect = this.mTempRect;
                i3 = this.mStartingRect.top;
                if ((this.mMoveDirection & 2) != 0) {
                    i2 = diffY;
                } else {
                    i2 = 0;
                }
                rect.top = i2 + i3;
                Rect rect2 = this.mTempRect;
                int i4 = this.mStartingRect.bottom;
                if ((this.mMoveDirection & 8) != 0) {
                    i = diffY;
                }
                rect2.bottom = i + i4;
                fitToRatio(this.mTempRect, diffX, diffY, this.mMoveDirection);
                if (checkValidResizeHandlerSize(this.mTempRect)) {
                    this.mMovingRect.set(this.mTempRect);
                }
                checkScreenBoundary();
            }
            this.mStartingRect.set(this.mMovingRect);
            if (this.mPIPResizeHandlerView != null) {
                this.mPIPResizeHandlerView.setPosition(this.mMovingRect.left, this.mMovingRect.top, this.mMovingRect.right, this.mMovingRect.bottom);
                this.mPIPResizeHandlerView.updatePosition(this.mMoveDirection);
            }
            retVal = true;
        }
        this.mPrevX = this.f35mX;
        this.mPrevY = this.f36mY;
        return retVal;
    }

    private void hideResizeHandlerOnMove() {
        if ((Math.abs(this.f35mX - this.mStartX) >= 15 || Math.abs(this.f36mY - this.mStartY) >= 15) && this.mIsResizeHandlerShown) {
            hideSubWindowResizeHandler();
        }
    }

    private boolean checkValidResizeHandlerSize(Rect resizeHandlerRect) {
        int minNormalPreviewWidth = this.mMinNormalPreviewWidth;
        int minNormalPreviewHeight = this.mMinNormalPreviewHeight;
        int maxNormalPreviewWidth = this.mMaxNormalPreviewWidth;
        int maxNormalPreviewHeight = this.mMaxNormalPreviewHeight;
        if (isSquareFrameType()) {
            minNormalPreviewWidth = minNormalPreviewHeight;
            maxNormalPreviewWidth = maxNormalPreviewHeight;
        }
        int resizeHandlreWidth = resizeHandlerRect.width();
        int resizeHandlerHeight = resizeHandlerRect.height();
        return resizeHandlreWidth >= minNormalPreviewHeight && resizeHandlreWidth <= maxNormalPreviewHeight && resizeHandlerHeight >= minNormalPreviewWidth && resizeHandlerHeight <= maxNormalPreviewWidth;
    }

    public void checkScreenBoundary() {
        int rectWidth = this.mMovingRect.width();
        int rectHeight = this.mMovingRect.height();
        int topMargin = (this.mLcdSize[0] - this.mPreviewSizeOnScreen[0]) / 2;
        if (this.mMovingRect.left <= 0) {
            this.mMovingRect.left = 0;
            this.mMovingRect.right = rectWidth;
        }
        if (this.mMovingRect.right >= this.mPreviewSizeOnScreen[1]) {
            this.mMovingRect.right = this.mPreviewSizeOnScreen[1];
            this.mMovingRect.left = this.mPreviewSizeOnScreen[1] - rectWidth;
        }
        if (this.mMovingRect.top <= topMargin) {
            this.mMovingRect.top = topMargin;
            this.mMovingRect.bottom = topMargin + rectHeight;
        }
        if (this.mMovingRect.bottom >= this.mPreviewSizeOnScreen[0] + topMargin) {
            this.mMovingRect.bottom = this.mPreviewSizeOnScreen[0] + topMargin;
            this.mMovingRect.top = (this.mPreviewSizeOnScreen[0] + topMargin) - rectHeight;
        }
    }

    private boolean doTouchActionUp() {
        if (!this.mIsNormalPreviewMoving) {
            return false;
        }
        if (this.mMoveDirection == 0) {
            hideSubWindowResizeHandler();
        } else {
            updateNormalPreview(true);
            if (!this.mIsResizeHandlerShown) {
                showSubWindowResizeHandler(this.mNormalPreviewPosition);
            }
        }
        this.mIsNormalPreviewMoving = false;
        return true;
    }

    public void updateNormalPreview(boolean isMovoed) {
        int extraMargin = 0;
        if (isSquareFrameType()) {
            extraMargin = (((int) (((float) this.mMovingRect.height()) * this.mPreviewAspect)) - this.mMovingRect.width()) / 2;
        }
        int topMargin = (this.mLcdSize[0] - this.mPreviewSizeOnScreen[0]) / 2;
        setCurNormalPreviewPosition(new RectF(((float) ((this.mMovingRect.top - topMargin) - extraMargin)) / ((float) this.mPreviewSizeOnScreen[0]), ((float) (this.mPreviewSizeOnScreen[1] - this.mMovingRect.right)) / ((float) this.mPreviewSizeOnScreen[1]), ((float) ((this.mMovingRect.bottom - topMargin) + extraMargin)) / ((float) this.mPreviewSizeOnScreen[0]), ((float) (this.mPreviewSizeOnScreen[1] - this.mMovingRect.left)) / ((float) this.mPreviewSizeOnScreen[1])));
        this.mPIPResizeHandlerView.updatePosition(15);
        this.mListener.onFrameMovingDone(this.mNormalPreviewPosition, isMovoed);
    }

    private void setGestureDetectorListener() {
        this.mGestureDetector = new GestureDetector(this.mGet.getAppContext(), new PIPGestureDetector());
    }

    private void releaseGestureDetectorListener() {
        this.mGestureDetector = null;
    }

    public void showSubWindowResizeHandler(RectF boundRect) {
        if (!this.mIsResizeHandlerShown && this.mPIPResizeHandlerView != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.module_base_root);
            if (vg != null && vg.indexOfChild(this.mPIPResizeHandlerView) == -1) {
                CamLog.m3d(CameraConstants.TAG, "showSubWindowResizeHandler");
                if (this.mPIPResizeHandlerView != null) {
                    setResizeHandlerPosition(boundRect);
                }
                vg.invalidate();
                vg.addView(this.mPIPResizeHandlerView);
                this.mIsResizeHandlerShown = true;
            }
        }
    }

    private void setResizeHandlerPosition(RectF boundRect) {
        int rectHeight = (int) (boundRect.height() * ((float) this.mPreviewSizeOnScreen[1]));
        int rectWidth = (int) (boundRect.width() * ((float) this.mPreviewSizeOnScreen[0]));
        if (isSquareFrameType()) {
            this.mExtraInvisibleMargin = (rectWidth - rectHeight) / 2;
        } else {
            this.mExtraInvisibleMargin = 0;
        }
        int right = (int) ((1.0f - boundRect.top) * ((float) this.mPreviewSizeOnScreen[1]));
        int topMargin = (this.mLcdSize[0] - this.mPreviewSizeOnScreen[0]) / 2;
        this.mPIPResizeHandlerView.setPosition((int) ((1.0f - boundRect.bottom) * ((float) this.mPreviewSizeOnScreen[1])), (((int) (boundRect.left * ((float) this.mPreviewSizeOnScreen[0]))) + topMargin) + this.mExtraInvisibleMargin, right, (((int) (boundRect.right * ((float) this.mPreviewSizeOnScreen[0]))) + topMargin) - this.mExtraInvisibleMargin);
    }

    public void hideSubWindowResizeHandler() {
        if (this.mIsResizeHandlerShown) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.module_base_root);
            if (vg != null && vg.indexOfChild(this.mPIPResizeHandlerView) != -1) {
                CamLog.m3d(CameraConstants.TAG, "hideSubWindowResizeHandler");
                vg.removeView(this.mPIPResizeHandlerView);
                this.mIsResizeHandlerShown = false;
            }
        }
    }

    public boolean isInNormalPreviewTouch(RectF boundRect, int x, int y) {
        int invisibleMargin;
        int result;
        int rectHeight = (int) (boundRect.height() * ((float) this.mPreviewSizeOnScreen[1]));
        int rectWidth = (int) (boundRect.width() * ((float) this.mPreviewSizeOnScreen[0]));
        if (isSquareFrameType()) {
            invisibleMargin = (rectWidth - rectHeight) / 2;
        } else {
            invisibleMargin = 0;
        }
        int topMargin = (this.mLcdSize[0] - this.mPreviewSizeOnScreen[0]) / 2;
        int top = (((int) (boundRect.left * ((float) this.mPreviewSizeOnScreen[0]))) + topMargin) + invisibleMargin;
        int right = (int) ((1.0f - boundRect.top) * ((float) this.mPreviewSizeOnScreen[1]));
        int bottom = (((int) (boundRect.right * ((float) this.mPreviewSizeOnScreen[0]))) + topMargin) - invisibleMargin;
        if (x <= ((int) ((1.0f - boundRect.bottom) * ((float) this.mPreviewSizeOnScreen[1]))) || x >= right) {
            result = 0;
        } else {
            result = 0 + 1;
        }
        if (y <= top || y >= bottom) {
            result = 0;
        } else {
            result++;
        }
        if (result == 2) {
            return true;
        }
        return false;
    }

    public void setCurNormalPreviewPosition(RectF rect) {
        this.mNormalPreviewPosition.set(rect);
    }

    public void setPreviewSizeOnScreen(String size) {
        int[] screenSize = Utils.sizeStringToArray(size);
        this.mPreviewSizeOnScreen = screenSize;
        this.mPreviewAspect = ((float) screenSize[0]) / ((float) screenSize[1]);
        if (this.mIsResizeHandlerShown) {
            hideSubWindowResizeHandler();
        }
        if (this.mCurrentFrameType == 0) {
            this.mMaxNormalPreviewRatio = 0.75f;
        } else {
            this.mMaxNormalPreviewRatio = 1.0f;
        }
        this.mMinNormalPreviewWidth = (int) (((float) screenSize[0]) * this.mMinNormalPreviewRatio);
        this.mMinNormalPreviewHeight = (int) (((float) screenSize[1]) * this.mMinNormalPreviewRatio);
        this.mMaxNormalPreviewWidth = (int) (((float) screenSize[0]) * this.mMaxNormalPreviewRatio);
        this.mMaxNormalPreviewHeight = (int) (((float) screenSize[1]) * this.mMaxNormalPreviewRatio);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        setGestureDetectorListener();
    }

    public void onPauseBefore() {
        hideSubWindowResizeHandler();
        super.onPauseAfter();
        releaseGestureDetectorListener();
        this.mIsResizeHandlerShown = false;
        this.mIsNormalPreviewMoving = false;
    }

    public void setFrameType(int type) {
        this.mCurrentFrameType = type;
    }

    public boolean isSquareFrameType() {
        return this.mCurrentFrameType == 1 || this.mCurrentFrameType == 5 || this.mCurrentFrameType == 6;
    }

    public boolean isDivisionFrameType() {
        return this.mCurrentFrameType == 4 || this.mCurrentFrameType == 7;
    }

    public void onFrameTypeChanged(RectF boundRect) {
        if (this.mPIPResizeHandlerView != null && boundRect != null) {
            setResizeHandlerPosition(boundRect);
            hideSubWindowResizeHandler();
            this.mPIPResizeHandlerView.updatePosition(15);
            this.mMovingRect.set(this.mPIPResizeHandlerView.getCurrentPosition());
        }
    }

    public boolean isNormalPreviewMoving() {
        return this.mIsNormalPreviewMoving;
    }
}
