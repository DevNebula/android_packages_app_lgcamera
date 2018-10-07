package com.lge.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class MultiDirectionSlidingDrawer extends ViewGroup {
    protected static final int ANIMATION_FRAME_DURATION = 16;
    private static final int COLLAPSED_FULL_CLOSED = -10002;
    private static final int EXPANDED_FULL_OPEN = -10001;
    private static final float MAXIMUM_ACCELERATION = 2000.0f;
    private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
    private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
    private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
    protected static final int MSG_ANIMATE = 1000;
    public static final int ORIENTATION_BTT = 1;
    public static final int ORIENTATION_LTR = 2;
    public static final int ORIENTATION_RTL = 0;
    public static final int ORIENTATION_TTB = 3;
    private static final int TAP_THRESHOLD = 6;
    private static final int VELOCITY_UNITS = 1000;
    private float MINIMUM_DISTANCE_FOR_MOVEMENT;
    private boolean mAllowSingleTap;
    private boolean mAnimateOnClick;
    private float mAnimatedAcceleration;
    private float mAnimatedVelocity;
    protected boolean mAnimating;
    private long mAnimationLastTime;
    private float mAnimationPosition;
    protected int mBottomOffset;
    protected View mContent;
    protected final Rect mContentFrame;
    private final int mContentId;
    protected int mContentOffset;
    private Context mContext;
    private long mCurrentAnimationTime;
    protected boolean mExpanded;
    protected final Rect mFrame;
    protected View mHandle;
    protected int mHandleHeight;
    private final int mHandleId;
    protected int mHandleWidth;
    private final Handler mHandler;
    private final Rect mInvalidate;
    private boolean mInvert;
    protected boolean mLocked;
    private int mMaximumAcceleration;
    private int mMaximumMajorVelocity;
    private int mMaximumMinorVelocity;
    private final int mMaximumTapVelocity;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;
    private boolean mSentScrollStartEvent;
    private float mStartX;
    private float mStartY;
    private final int mTapThreshold;
    protected int mTopOffset;
    private int mTouchDelta;
    protected boolean mTracking;
    private VelocityTracker mVelocityTracker;
    private final int mVelocityUnits;
    private boolean mVertical;

    private class DrawerToggler implements OnClickListener {
        private DrawerToggler() {
        }

        public void onClick(View v) {
            if (!MultiDirectionSlidingDrawer.this.mLocked) {
                if (MultiDirectionSlidingDrawer.this.mAnimateOnClick) {
                    MultiDirectionSlidingDrawer.this.animateToggle();
                } else {
                    MultiDirectionSlidingDrawer.this.toggle();
                }
            }
        }
    }

    public interface OnDrawerCloseListener {
        void onDrawerClosed();
    }

    public interface OnDrawerOpenListener {
        void onDrawerOpened();
    }

    public interface OnDrawerScrollListener {
        void onScrollEnded();

        void onScrollStarted();
    }

    private class SlidingHandler extends Handler {
        private SlidingHandler() {
        }

        public void handleMessage(Message m) {
            switch (m.what) {
                case 1000:
                    MultiDirectionSlidingDrawer.this.doAnimation();
                    return;
                default:
                    return;
            }
        }
    }

    public MultiDirectionSlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiDirectionSlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
        boolean z;
        super(context, attrs, defStyle);
        this.mFrame = new Rect();
        this.mContentFrame = new Rect();
        this.mInvalidate = new Rect();
        this.mHandler = new SlidingHandler();
        this.MINIMUM_DISTANCE_FOR_MOVEMENT = 6.0f;
        TypedArray a = context.obtainStyledAttributes(attrs, C0088R.styleable.MultiDirectionSlidingDrawer, defStyle, 0);
        this.mContext = context;
        int orientation = a.getInt(0, 1);
        if (orientation == 1 || orientation == 3) {
            z = true;
        } else {
            z = false;
        }
        this.mVertical = z;
        this.mBottomOffset = (int) a.getDimension(3, 0.0f);
        this.mTopOffset = (int) a.getDimension(4, 0.0f);
        this.mContentOffset = (int) a.getDimension(5, 0.0f);
        this.mAllowSingleTap = a.getBoolean(6, true);
        this.mAnimateOnClick = a.getBoolean(7, true);
        if (orientation == 3 || orientation == 2) {
            z = true;
        } else {
            z = false;
        }
        this.mInvert = z;
        int handleId = a.getResourceId(1, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException("The handle attribute is required and must refer to a valid child.");
        }
        int contentId = a.getResourceId(2, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
        } else if (handleId == contentId) {
            throw new IllegalArgumentException("The content and handle attributes must refer to different children.");
        } else {
            this.mHandleId = handleId;
            this.mContentId = contentId;
            float density = getResources().getDisplayMetrics().density;
            this.mTapThreshold = (int) ((6.0f * density) + 0.5f);
            this.mMaximumTapVelocity = (int) ((MAXIMUM_TAP_VELOCITY * density) + 0.5f);
            this.mMaximumMinorVelocity = (int) ((MAXIMUM_MINOR_VELOCITY * density) + 0.5f);
            this.mMaximumMajorVelocity = (int) ((MAXIMUM_MAJOR_VELOCITY * density) + 0.5f);
            this.mMaximumAcceleration = (int) ((MAXIMUM_ACCELERATION * density) + 0.5f);
            this.mVelocityUnits = (int) ((1000.0f * density) + 0.5f);
            if (this.mInvert) {
                this.mMaximumAcceleration = -this.mMaximumAcceleration;
                this.mMaximumMajorVelocity = -this.mMaximumMajorVelocity;
                this.mMaximumMinorVelocity = -this.mMaximumMinorVelocity;
            }
            this.MINIMUM_DISTANCE_FOR_MOVEMENT = Utils.dpToPx(this.mContext, 1.5f);
            a.recycle();
            setAlwaysDrawnWithCacheEnabled(false);
        }
    }

    protected void onFinishInflate() {
        this.mHandle = findViewById(this.mHandleId);
        if (this.mHandle == null) {
            throw new IllegalArgumentException("The handle attribute is must refer to an existing child.");
        }
        this.mHandle.setOnClickListener(new DrawerToggler());
        this.mContent = findViewById(this.mContentId);
        if (this.mContent == null) {
            throw new IllegalArgumentException("The content attribute is must refer to an existing child.");
        }
        this.mContent.setVisibility(4);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == 0 || heightSpecMode == 0) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }
        View handle = this.mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);
        if (this.mVertical) {
            this.mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, 1073741824), MeasureSpec.makeMeasureSpec((heightSpecSize - handle.getMeasuredHeight()) - this.mTopOffset, 1073741824));
        } else {
            this.mContent.measure(MeasureSpec.makeMeasureSpec((widthSpecSize - handle.getMeasuredWidth()) - this.mTopOffset, 1073741824), MeasureSpec.makeMeasureSpec(heightSpecSize, 1073741824));
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    protected void dispatchDraw(Canvas canvas) {
        float f = 0.0f;
        long drawingTime = getDrawingTime();
        View handle = this.mHandle;
        boolean isVertical = this.mVertical;
        drawChild(canvas, handle, drawingTime);
        if (this.mTracking || this.mAnimating) {
            Bitmap cache = this.mContent.getDrawingCache();
            if (cache == null) {
                canvas.save();
                float top;
                if (this.mInvert) {
                    float left = isVertical ? 0.0f : (float) (((handle.getLeft() - getWidth()) + this.mTopOffset) + this.mHandleWidth);
                    if (isVertical) {
                        top = (float) (((handle.getTop() - getHeight()) + this.mTopOffset) + this.mHandleHeight);
                    } else {
                        top = 0.0f;
                    }
                    canvas.translate(left, top);
                } else {
                    top = isVertical ? 0.0f : (float) (handle.getLeft() - this.mTopOffset);
                    if (isVertical) {
                        f = (float) (handle.getTop() - this.mTopOffset);
                    }
                    canvas.translate(top, f);
                }
                drawChild(canvas, this.mContent, drawingTime);
                canvas.restore();
            } else if (!isVertical) {
                canvas.drawBitmap(cache, this.mInvert ? (float) (handle.getLeft() - cache.getWidth()) : (float) handle.getRight(), 0.0f, null);
            } else if (this.mInvert) {
                canvas.drawBitmap(cache, 0.0f, (float) (handle.getTop() - cache.getHeight()), null);
            } else {
                canvas.drawBitmap(cache, 0.0f, (float) handle.getBottom(), null);
            }
            invalidate();
        } else if (this.mExpanded) {
            drawChild(canvas, this.mContent, drawingTime);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!this.mTracking) {
            int handleLeft;
            int handleTop;
            int width = r - l;
            int height = b - t;
            View handle = this.mHandle;
            int handleWidth = handle.getMeasuredWidth();
            int handleHeight = handle.getMeasuredHeight();
            View content = this.mContent;
            if (this.mVertical) {
                handleLeft = (width - handleWidth) / 2;
                if (this.mInvert) {
                    handleTop = this.mExpanded ? (height - this.mTopOffset) - handleHeight : -this.mBottomOffset;
                    content.layout(0, ((height - this.mTopOffset) - handleHeight) - content.getMeasuredHeight(), content.getMeasuredWidth(), (height - this.mTopOffset) - handleHeight);
                } else {
                    handleTop = this.mExpanded ? this.mTopOffset : (height - handleHeight) + this.mBottomOffset;
                    content.layout(0, this.mTopOffset + handleHeight, content.getMeasuredWidth(), (this.mTopOffset + handleHeight) + content.getMeasuredHeight());
                }
            } else {
                handleTop = (height - handleHeight) / 2;
                if (this.mInvert) {
                    handleLeft = this.mExpanded ? (width - this.mTopOffset) - handleWidth : -this.mBottomOffset;
                    content.layout(((width - this.mTopOffset) - handleWidth) - content.getMeasuredWidth(), 0, (width - this.mTopOffset) - handleWidth, content.getMeasuredHeight());
                } else {
                    handleLeft = this.mExpanded ? this.mTopOffset : (width - handleWidth) + this.mBottomOffset;
                    content.layout(this.mTopOffset + handleWidth, 0, (this.mTopOffset + handleWidth) + content.getMeasuredWidth(), content.getMeasuredHeight());
                }
            }
            handle.layout(handleLeft, handleTop, handleLeft + handleWidth, handleTop + handleHeight);
            this.mHandleHeight = handle.getHeight();
            this.mHandleWidth = handle.getWidth();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mLocked) {
            return false;
        }
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        Rect frame = this.mFrame;
        Rect contentFrame = this.mContentFrame;
        View handle = this.mHandle;
        View content = this.mContent;
        handle.getHitRect(frame);
        content.getHitRect(this.mContentFrame);
        if (!isTouchEventOnDrawer(event, frame, contentFrame)) {
            return false;
        }
        if (action == 0) {
            this.mTracking = true;
            prepareContent();
            setPressedItem(event, handle, content);
            this.mStartX = event.getX();
            this.mStartY = event.getY();
            this.mSentScrollStartEvent = false;
            if (this.mVertical) {
                int top = this.mHandle.getTop();
                this.mTouchDelta = ((int) y) - top;
                prepareTracking(top);
            } else {
                int left = this.mHandle.getLeft();
                this.mTouchDelta = ((int) x) - left;
                prepareTracking(left);
            }
            this.mVelocityTracker.addMovement(event);
        }
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mLocked) {
            return false;
        }
        if (this.mTracking) {
            this.mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case 1:
                case 3:
                    boolean negative;
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(this.mVelocityUnits);
                    float yVelocity = velocityTracker.getYVelocity();
                    float xVelocity = velocityTracker.getXVelocity();
                    boolean vertical = this.mVertical;
                    if (vertical) {
                        negative = yVelocity < 0.0f;
                        if (xVelocity < 0.0f) {
                            xVelocity = -xVelocity;
                        }
                        if ((!this.mInvert && xVelocity > ((float) this.mMaximumMinorVelocity)) || (this.mInvert && xVelocity < ((float) this.mMaximumMinorVelocity))) {
                            xVelocity = (float) this.mMaximumMinorVelocity;
                        }
                    } else {
                        negative = xVelocity < 0.0f;
                        if (yVelocity < 0.0f) {
                            yVelocity = -yVelocity;
                        }
                        if ((!this.mInvert && yVelocity > ((float) this.mMaximumMinorVelocity)) || (this.mInvert && yVelocity < ((float) this.mMaximumMinorVelocity))) {
                            yVelocity = (float) this.mMaximumMinorVelocity;
                        }
                    }
                    float velocity = (float) Math.hypot((double) xVelocity, (double) yVelocity);
                    if (negative) {
                        velocity = -velocity;
                    }
                    int handleTop = this.mHandle.getTop();
                    int handleLeft = this.mHandle.getLeft();
                    int handleBottom = this.mHandle.getBottom();
                    int handleRight = this.mHandle.getRight();
                    if (!isTapVelocity(velocity)) {
                        if (!vertical) {
                            handleTop = handleLeft;
                        }
                        performFling(handleTop, velocity, false);
                        break;
                    }
                    boolean c1;
                    boolean c2;
                    boolean c3;
                    boolean c4;
                    if (this.mInvert) {
                        c1 = this.mExpanded && handleBottom > ((getBottom() - getTop()) - this.mTopOffset) - this.mTapThreshold;
                        c2 = !this.mExpanded && handleBottom < ((-this.mBottomOffset) + this.mHandleHeight) + this.mTapThreshold;
                        c3 = this.mExpanded && handleRight > ((getRight() - getLeft()) - this.mTopOffset) - this.mTapThreshold;
                        c4 = !this.mExpanded && handleRight < ((-this.mBottomOffset) + this.mHandleWidth) + this.mTapThreshold;
                    } else {
                        c1 = this.mExpanded && handleTop < this.mTapThreshold + this.mTopOffset;
                        c2 = !this.mExpanded && handleTop > (((this.mBottomOffset + getBottom()) - getTop()) - this.mHandleHeight) - this.mTapThreshold;
                        c3 = this.mExpanded && handleLeft < this.mTapThreshold + this.mTopOffset;
                        c4 = !this.mExpanded && handleLeft > (((this.mBottomOffset + getRight()) - getLeft()) - this.mHandleWidth) - this.mTapThreshold;
                    }
                    if (vertical ? c1 || c2 : c3 || c4) {
                        if (!this.mAllowSingleTap) {
                            if (!vertical) {
                                handleTop = handleLeft;
                            }
                            performFling(handleTop, velocity, false);
                            break;
                        }
                        changeTouchEvent(vertical, handleTop, handleLeft, event, velocity);
                        break;
                    }
                    if (!vertical) {
                        handleTop = handleLeft;
                    }
                    performFling(handleTop, velocity, false);
                    break;
                    break;
                case 2:
                    if (!this.mSentScrollStartEvent) {
                        float distY = event.getY() - this.mStartY;
                        if (Math.abs(event.getX() - this.mStartX) > this.MINIMUM_DISTANCE_FOR_MOVEMENT && Math.abs(distY) > this.MINIMUM_DISTANCE_FOR_MOVEMENT) {
                            if (this.mOnDrawerScrollListener != null) {
                                this.mOnDrawerScrollListener.onScrollStarted();
                            }
                            this.mSentScrollStartEvent = true;
                        }
                    }
                    moveHandle(((int) (this.mVertical ? event.getY() : event.getX())) - this.mTouchDelta);
                    break;
            }
        }
        if (this.mTracking || this.mAnimating || super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    protected boolean isTapVelocity(float velocity) {
        return Math.abs(velocity) < ((float) this.mMaximumTapVelocity);
    }

    protected void animateClose(int position) {
        prepareTracking(position);
        performFling(position, (float) this.mMaximumAcceleration, true);
    }

    protected void animateOpen(int position) {
        prepareTracking(position);
        performFling(position, (float) (-this.mMaximumAcceleration), true);
    }

    protected void performFling(int position, float velocity, boolean always) {
        this.mAnimationPosition = (float) position;
        this.mAnimatedVelocity = velocity;
        boolean c1;
        boolean c2;
        boolean c3;
        if (this.mExpanded) {
            int bottom = this.mVertical ? getBottom() : getRight();
            int handleHeight = this.mVertical ? this.mHandleHeight : this.mHandleWidth;
            c1 = this.mInvert ? velocity < ((float) (-this.mMaximumMajorVelocity)) : velocity > ((float) this.mMaximumMajorVelocity);
            if (this.mInvert) {
                c2 = (bottom - (position + handleHeight)) + this.mBottomOffset > handleHeight;
            } else {
                c2 = position > (this.mVertical ? this.mHandleHeight : this.mHandleWidth) + this.mTopOffset;
            }
            c3 = this.mInvert ? velocity < ((float) this.mMaximumMajorVelocity) : velocity > ((float) (-this.mMaximumMajorVelocity));
            if (always || c1 || (c2 && c3)) {
                this.mAnimatedAcceleration = (float) this.mMaximumAcceleration;
                if (this.mInvert) {
                    if (velocity > 0.0f) {
                        this.mAnimatedVelocity = 0.0f;
                    }
                } else if (velocity < 0.0f) {
                    this.mAnimatedVelocity = 0.0f;
                }
            } else {
                this.mAnimatedAcceleration = (float) (-this.mMaximumAcceleration);
                if (this.mInvert) {
                    if (velocity < 0.0f) {
                        this.mAnimatedVelocity = 0.0f;
                    }
                } else if (velocity > 0.0f) {
                    this.mAnimatedVelocity = 0.0f;
                }
            }
        } else {
            c1 = this.mInvert ? velocity < ((float) (-this.mMaximumMajorVelocity)) : velocity > ((float) this.mMaximumMajorVelocity);
            if (this.mInvert) {
                c2 = position < (this.mVertical ? getHeight() : getWidth()) / 2;
            } else {
                c2 = position > (this.mVertical ? getHeight() : getWidth()) / 2;
            }
            c3 = this.mInvert ? velocity < ((float) this.mMaximumMajorVelocity) : velocity > ((float) (-this.mMaximumMajorVelocity));
            boolean c4 = this.mInvert ? position >= (getWidth() - this.mTopOffset) - this.mHandleWidth : position <= (getHeight() - this.mTopOffset) - this.mHandleHeight;
            if (always || c4 || !(c1 || (c2 && c3))) {
                this.mAnimatedAcceleration = (float) (-this.mMaximumAcceleration);
                if (this.mInvert) {
                    if (velocity < 0.0f) {
                        this.mAnimatedVelocity = 0.0f;
                    }
                } else if (velocity > 0.0f) {
                    this.mAnimatedVelocity = 0.0f;
                }
            } else {
                this.mAnimatedAcceleration = (float) this.mMaximumAcceleration;
                if (this.mInvert) {
                    if (velocity > 0.0f) {
                        this.mAnimatedVelocity = 0.0f;
                    }
                } else if (velocity < 0.0f) {
                    this.mAnimatedVelocity = 0.0f;
                }
            }
        }
        long now = SystemClock.uptimeMillis();
        this.mAnimationLastTime = now;
        this.mCurrentAnimationTime = 16 + now;
        this.mAnimating = true;
        this.mHandler.removeMessages(1000);
        this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(1000), this.mCurrentAnimationTime);
        stopTracking();
    }

    protected void prepareTracking(int position) {
        boolean opening;
        this.mTracking = true;
        this.mVelocityTracker = VelocityTracker.obtain();
        if (this.mExpanded) {
            opening = false;
        } else {
            opening = true;
        }
        if (opening) {
            this.mAnimatedAcceleration = (float) this.mMaximumAcceleration;
            this.mAnimatedVelocity = (float) this.mMaximumMajorVelocity;
            if (this.mInvert) {
                this.mAnimationPosition = (float) (-this.mBottomOffset);
            } else {
                this.mAnimationPosition = (float) ((this.mVertical ? getHeight() - this.mHandleHeight : getWidth() - this.mHandleWidth) + this.mBottomOffset);
            }
            moveHandle((int) this.mAnimationPosition);
            this.mAnimating = true;
            this.mHandler.removeMessages(1000);
            long now = SystemClock.uptimeMillis();
            this.mAnimationLastTime = now;
            this.mCurrentAnimationTime = 16 + now;
            this.mAnimating = true;
            return;
        }
        if (this.mAnimating) {
            this.mAnimating = false;
            this.mHandler.removeMessages(1000);
        }
        moveHandle(position);
    }

    protected void moveHandle(int position) {
        View handle = this.mHandle;
        Rect frame;
        Rect region;
        if (this.mVertical) {
            if (position == EXPANDED_FULL_OPEN) {
                if (this.mInvert) {
                    handle.offsetTopAndBottom(((((-this.mTopOffset) + getBottom()) - getTop()) - this.mHandleHeight) - handle.getTop());
                } else {
                    handle.offsetTopAndBottom(this.mTopOffset - handle.getTop());
                }
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
                if (this.mInvert) {
                    handle.offsetTopAndBottom((-this.mBottomOffset) - handle.getTop());
                } else {
                    handle.offsetTopAndBottom((((this.mBottomOffset + getBottom()) - getTop()) - this.mHandleHeight) - handle.getTop());
                }
                invalidate();
            } else {
                int top = handle.getTop();
                int deltaY = position - top;
                if (this.mInvert) {
                    if (position > (getBottom() - this.mTopOffset) - this.mHandleHeight) {
                        deltaY = ((getBottom() - this.mTopOffset) - this.mHandleHeight) - top;
                    } else if (deltaY < (-this.mBottomOffset) - top) {
                        deltaY = (-this.mBottomOffset) - top;
                    }
                } else if (position < this.mTopOffset) {
                    deltaY = this.mTopOffset - top;
                } else if (deltaY > (((this.mBottomOffset + getBottom()) - getTop()) - this.mHandleHeight) - top) {
                    deltaY = (((this.mBottomOffset + getBottom()) - getTop()) - this.mHandleHeight) - top;
                }
                handle.offsetTopAndBottom(deltaY);
                frame = this.mFrame;
                region = this.mInvalidate;
                handle.getHitRect(frame);
                region.set(frame);
                region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
                region.union(0, frame.bottom - deltaY, getWidth(), (frame.bottom - deltaY) + this.mContent.getHeight());
                invalidate(region);
            }
        } else if (position == EXPANDED_FULL_OPEN) {
            if (this.mInvert) {
                handle.offsetLeftAndRight(((((-this.mTopOffset) + getRight()) - getLeft()) - this.mHandleWidth) - handle.getLeft());
            } else {
                handle.offsetLeftAndRight(this.mTopOffset - handle.getLeft());
            }
            invalidate();
        } else if (position == COLLAPSED_FULL_CLOSED) {
            if (this.mInvert) {
                handle.offsetLeftAndRight((-this.mBottomOffset) - handle.getLeft());
            } else {
                handle.offsetLeftAndRight((((this.mBottomOffset + getRight()) - getLeft()) - this.mHandleWidth) - handle.getLeft());
            }
            invalidate();
        } else {
            int left = handle.getLeft();
            int deltaX = position - left;
            if (this.mInvert) {
                if (position > (getRight() - this.mTopOffset) - this.mHandleWidth) {
                    deltaX = ((getRight() - this.mTopOffset) - this.mHandleWidth) - left;
                } else if (deltaX < (-this.mBottomOffset) - left) {
                    deltaX = (-this.mBottomOffset) - left;
                }
            } else if (position < this.mTopOffset) {
                deltaX = this.mTopOffset - left;
            } else if (deltaX > (((this.mBottomOffset + getRight()) - getLeft()) - this.mHandleWidth) - left) {
                deltaX = (((this.mBottomOffset + getRight()) - getLeft()) - this.mHandleWidth) - left;
            }
            handle.offsetLeftAndRight(deltaX);
            frame = this.mFrame;
            region = this.mInvalidate;
            handle.getHitRect(frame);
            region.set(frame);
            region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom);
            region.union(frame.right - deltaX, 0, (frame.right - deltaX) + this.mContent.getWidth(), getHeight());
            invalidate(region);
        }
    }

    protected void prepareContent() {
        if (!this.mAnimating) {
            View content = this.mContent;
            if (content.isLayoutRequested()) {
                if (this.mVertical) {
                    int handleHeight = this.mHandleHeight;
                    content.measure(MeasureSpec.makeMeasureSpec(getRight() - getLeft(), 1073741824), MeasureSpec.makeMeasureSpec(((getBottom() - getTop()) - handleHeight) - this.mTopOffset, 1073741824));
                    if (this.mInvert) {
                        content.layout(0, this.mTopOffset, content.getMeasuredWidth(), this.mTopOffset + content.getMeasuredHeight());
                    } else {
                        content.layout(0, this.mTopOffset + handleHeight, content.getMeasuredWidth(), (this.mTopOffset + handleHeight) + content.getMeasuredHeight());
                    }
                } else {
                    int handleWidth = this.mHandle.getWidth();
                    content.measure(MeasureSpec.makeMeasureSpec(((getRight() - getLeft()) - handleWidth) - this.mTopOffset, 1073741824), MeasureSpec.makeMeasureSpec(getBottom() - getTop(), 1073741824));
                    if (this.mInvert) {
                        content.layout(this.mTopOffset, 0, this.mTopOffset + content.getMeasuredWidth(), content.getMeasuredHeight());
                    } else {
                        content.layout(this.mTopOffset + handleWidth, 0, (this.mTopOffset + handleWidth) + content.getMeasuredWidth(), content.getMeasuredHeight());
                    }
                }
            }
            content.getViewTreeObserver().dispatchOnPreDraw();
            if (content.getVisibility() != 4) {
                content.buildDrawingCache();
            }
            content.setVisibility(4);
        }
    }

    protected void stopTracking() {
        this.mHandle.setPressed(false);
        this.mTracking = false;
        if (this.mOnDrawerScrollListener != null) {
            this.mOnDrawerScrollListener.onScrollEnded();
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void doAnimation() {
        if (this.mAnimating) {
            incrementAnimation();
            if (!this.mInvert) {
                if (this.mAnimationPosition >= ((float) (((this.mVertical ? getHeight() : getWidth()) + this.mBottomOffset) - 1))) {
                    this.mAnimating = false;
                    closeDrawer();
                } else if (this.mAnimationPosition < ((float) this.mTopOffset)) {
                    this.mAnimating = false;
                    openDrawer();
                } else {
                    moveHandle((int) this.mAnimationPosition);
                    this.mCurrentAnimationTime += 16;
                    this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(1000), this.mCurrentAnimationTime);
                }
            } else if (this.mAnimationPosition <= ((float) ((-this.mBottomOffset) + 1))) {
                this.mAnimating = false;
                closeDrawer();
            } else {
                if (this.mAnimationPosition > ((float) ((this.mVertical ? getHeight() - this.mHandleHeight : getWidth() - this.mHandleWidth) + (-this.mTopOffset)))) {
                    this.mAnimating = false;
                    openDrawer();
                    return;
                }
                moveHandle((int) this.mAnimationPosition);
                this.mCurrentAnimationTime += 16;
                this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(1000), this.mCurrentAnimationTime);
            }
        }
    }

    private void incrementAnimation() {
        long now = SystemClock.uptimeMillis();
        float t = ((float) (now - this.mAnimationLastTime)) / 1000.0f;
        float position = this.mAnimationPosition;
        float v = this.mAnimatedVelocity;
        float a = this.mInvert ? this.mAnimatedAcceleration : this.mAnimatedAcceleration;
        this.mAnimationPosition = ((v * t) + position) + (((0.5f * a) * t) * t);
        this.mAnimatedVelocity = (a * t) + v;
        this.mAnimationLastTime = now;
    }

    public void toggle() {
        if (this.mExpanded) {
            closeDrawer();
        } else {
            openDrawer();
        }
        invalidate();
        requestLayout();
    }

    public void animateToggle() {
        if (this.mExpanded) {
            animateClose();
        } else {
            animateOpen();
        }
    }

    public void open() {
        openDrawer();
        invalidate();
        requestLayout();
        sendAccessibilityEvent(32);
    }

    public void close() {
        closeDrawer();
        invalidate();
        requestLayout();
    }

    public void animateClose() {
        CamLog.m3d(CameraConstants.TAG, "animateClose");
        prepareContent();
        OnDrawerScrollListener scrollListener = this.mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateClose(this.mVertical ? this.mHandle.getTop() : this.mHandle.getLeft());
        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    public void animateOpen() {
        CamLog.m3d(CameraConstants.TAG, "animateOpen");
        prepareContent();
        OnDrawerScrollListener scrollListener = this.mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateOpen(this.mVertical ? this.mHandle.getTop() : this.mHandle.getLeft());
        sendAccessibilityEvent(32);
        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    private void closeDrawer() {
        moveHandle(COLLAPSED_FULL_CLOSED);
        this.mContent.setVisibility(4);
        this.mContent.destroyDrawingCache();
        this.mExpanded = false;
        if (this.mOnDrawerCloseListener != null) {
            this.mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    private void openDrawer() {
        moveHandle(EXPANDED_FULL_OPEN);
        this.mContent.setVisibility(0);
        this.mExpanded = true;
        if (this.mOnDrawerOpenListener != null) {
            this.mOnDrawerOpenListener.onDrawerOpened();
        }
    }

    public void setOnDrawerOpenListener(OnDrawerOpenListener onDrawerOpenListener) {
        this.mOnDrawerOpenListener = onDrawerOpenListener;
    }

    public void setOnDrawerCloseListener(OnDrawerCloseListener onDrawerCloseListener) {
        this.mOnDrawerCloseListener = onDrawerCloseListener;
    }

    public void setOnDrawerScrollListener(OnDrawerScrollListener onDrawerScrollListener) {
        this.mOnDrawerScrollListener = onDrawerScrollListener;
    }

    public View getHandle() {
        return this.mHandle;
    }

    public View getContent() {
        return this.mContent;
    }

    public void unlock() {
        this.mLocked = false;
    }

    public void lock() {
        this.mLocked = true;
    }

    public boolean isLock() {
        return this.mLocked;
    }

    public boolean isOpened() {
        return this.mExpanded;
    }

    public boolean isMoving() {
        return this.mTracking || this.mAnimating;
    }

    protected boolean isTouchEventOnDrawer(MotionEvent event, Rect frame, Rect mContentFrame) {
        float x = event.getX();
        float y = event.getY();
        if (this.mTracking || frame.contains((int) x, (int) y)) {
            return true;
        }
        return false;
    }

    protected void changeTouchEvent(boolean vertical, int handleTop, int handleLeft, MotionEvent event, float velocity) {
        playSoundEffect(0);
        if (this.mExpanded) {
            if (!vertical) {
                handleTop = handleLeft;
            }
            animateClose(handleTop);
            return;
        }
        if (!vertical) {
            handleTop = handleLeft;
        }
        animateOpen(handleTop);
    }

    protected void setPressedItem(MotionEvent event, View handle, View content) {
        handle.setPressed(true);
    }

    public void setBottomOffset(int offset) {
        this.mBottomOffset = offset;
    }
}
