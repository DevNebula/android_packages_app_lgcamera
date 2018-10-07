package com.lge.camera.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.OverScroller;
import android.widget.Scroller;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class TouchImageViewBase extends ImageView {
    protected static final String DEBUG = "DEBUG";
    protected static final float SUPER_MAX_MULTIPLIER = 1.0f;
    protected static final float SUPER_MIN_MULTIPLIER = 1.0f;
    protected Context context;
    protected ZoomVariables delayedZoomVariables;
    protected OnDoubleTapListener doubleTapListener = null;
    protected Fling fling;
    protected boolean imageRenderedAtLeastOnce;
    /* renamed from: m */
    protected float[] f17m;
    protected GestureDetector mGestureDetector;
    protected TouchImageViewInterface mGet = null;
    protected ScaleGestureDetector mScaleDetector;
    protected ScaleType mScaleType;
    protected float matchViewHeight;
    protected float matchViewWidth;
    protected Matrix matrix;
    protected float maxScale;
    protected float minScale;
    protected float normalizedScale;
    protected boolean onDrawReady;
    protected float prevMatchViewHeight;
    protected float prevMatchViewWidth;
    protected Matrix prevMatrix;
    protected int prevViewHeight;
    protected int prevViewWidth;
    protected State state;
    protected float superMaxScale;
    protected float superMinScale;
    protected OnTouchImageViewListener touchImageViewListener = null;
    protected OnTouchListener userTouchListener = null;
    protected int viewHeight;
    protected int viewWidth;

    /* renamed from: com.lge.camera.components.TouchImageViewBase$1 */
    static /* synthetic */ class C05541 {
        static final /* synthetic */ int[] $SwitchMap$android$widget$ImageView$ScaleType = new int[ScaleType.values().length];

        static {
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ScaleType.CENTER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ScaleType.CENTER_CROP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ScaleType.CENTER_INSIDE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ScaleType.FIT_CENTER.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ScaleType.FIT_XY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    @TargetApi(9)
    protected class CompatScroller {
        boolean isPreGingerbread;
        OverScroller overScroller;
        Scroller scroller;

        public CompatScroller(Context context) {
            if (VERSION.SDK_INT < 9) {
                this.isPreGingerbread = true;
                this.scroller = new Scroller(context);
                return;
            }
            this.isPreGingerbread = false;
            this.overScroller = new OverScroller(context);
        }

        public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
            if (this.isPreGingerbread) {
                this.scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            } else {
                this.overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            }
        }

        public void forceFinished(boolean finished) {
            if (this.isPreGingerbread) {
                this.scroller.forceFinished(finished);
            } else {
                this.overScroller.forceFinished(finished);
            }
        }

        public boolean isFinished() {
            if (this.isPreGingerbread) {
                return this.scroller.isFinished();
            }
            return this.overScroller.isFinished();
        }

        public boolean computeScrollOffset() {
            if (this.isPreGingerbread) {
                return this.scroller.computeScrollOffset();
            }
            this.overScroller.computeScrollOffset();
            return this.overScroller.computeScrollOffset();
        }

        public int getCurrX() {
            if (this.isPreGingerbread) {
                return this.scroller.getCurrX();
            }
            return this.overScroller.getCurrX();
        }

        public int getCurrY() {
            if (this.isPreGingerbread) {
                return this.scroller.getCurrY();
            }
            return this.overScroller.getCurrY();
        }
    }

    protected class DoubleTapZoom implements Runnable {
        private static final float ZOOM_TIME = 200.0f;
        private float bitmapX;
        private float bitmapY;
        private PointF endTouch;
        private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        private long startTime;
        private PointF startTouch;
        private float startZoom;
        private boolean stretchImageToSuper;
        private float targetZoom;

        DoubleTapZoom(float targetZoom, float focusX, float focusY, boolean stretchImageToSuper) {
            TouchImageViewBase.this.setState(State.ANIMATE_ZOOM);
            this.startTime = System.currentTimeMillis();
            this.startZoom = TouchImageViewBase.this.normalizedScale;
            this.targetZoom = targetZoom;
            this.stretchImageToSuper = stretchImageToSuper;
            PointF bitmapPoint = TouchImageViewBase.this.transformCoordTouchToBitmap(focusX, focusY, false);
            this.bitmapX = bitmapPoint.x;
            this.bitmapY = bitmapPoint.y;
            this.startTouch = TouchImageViewBase.this.transformCoordBitmapToTouch(this.bitmapX, this.bitmapY);
            this.endTouch = new PointF((float) (TouchImageViewBase.this.viewWidth / 2), (float) (TouchImageViewBase.this.viewHeight / 2));
        }

        public void run() {
            float t = interpolate();
            TouchImageViewBase.this.scaleImage(calculateDeltaScale(t), this.bitmapX, this.bitmapY, this.stretchImageToSuper);
            translateImageToCenterTouchPosition(t);
            TouchImageViewBase.this.fixScaleTrans();
            TouchImageViewBase.this.setImageMatrix(TouchImageViewBase.this.matrix);
            if (TouchImageViewBase.this.touchImageViewListener != null) {
                TouchImageViewBase.this.touchImageViewListener.onMove();
            }
            if (t < 1.0f) {
                TouchImageViewBase.this.compatPostOnAnimation(this);
            } else {
                TouchImageViewBase.this.setState(State.NONE);
            }
        }

        private void translateImageToCenterTouchPosition(float t) {
            float targetX = this.startTouch.x + ((this.endTouch.x - this.startTouch.x) * t);
            float targetY = this.startTouch.y + ((this.endTouch.y - this.startTouch.y) * t);
            PointF curr = TouchImageViewBase.this.transformCoordBitmapToTouch(this.bitmapX, this.bitmapY);
            TouchImageViewBase.this.matrix.postTranslate(targetX - curr.x, targetY - curr.y);
        }

        private float interpolate() {
            return this.interpolator.getInterpolation(Math.min(1.0f, ((float) (System.currentTimeMillis() - this.startTime)) / ZOOM_TIME));
        }

        private double calculateDeltaScale(float t) {
            return ((double) (this.startZoom + ((this.targetZoom - this.startZoom) * t))) / ((double) TouchImageViewBase.this.normalizedScale);
        }
    }

    protected class Fling implements Runnable {
        int currX;
        int currY;
        CompatScroller scroller;

        Fling(int velocityX, int velocityY) {
            int minX;
            int maxX;
            int minY;
            int maxY;
            TouchImageViewBase.this.setState(State.FLING);
            this.scroller = new CompatScroller(TouchImageViewBase.this.context);
            TouchImageViewBase.this.matrix.getValues(TouchImageViewBase.this.f17m);
            int startX = (int) TouchImageViewBase.this.f17m[2];
            int startY = (int) TouchImageViewBase.this.f17m[5];
            if (TouchImageViewBase.this.getImageWidth() > ((float) TouchImageViewBase.this.viewWidth)) {
                minX = TouchImageViewBase.this.viewWidth - ((int) TouchImageViewBase.this.getImageWidth());
                maxX = 0;
            } else {
                maxX = startX;
                minX = startX;
            }
            if (TouchImageViewBase.this.getImageHeight() > ((float) TouchImageViewBase.this.viewHeight)) {
                minY = TouchImageViewBase.this.viewHeight - ((int) TouchImageViewBase.this.getImageHeight());
                maxY = 0;
            } else {
                maxY = startY;
                minY = startY;
            }
            this.scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            this.currX = startX;
            this.currY = startY;
        }

        public void cancelFling() {
            if (this.scroller != null) {
                TouchImageViewBase.this.setState(State.NONE);
                this.scroller.forceFinished(true);
            }
        }

        public void run() {
            if (TouchImageViewBase.this.touchImageViewListener != null) {
                TouchImageViewBase.this.touchImageViewListener.onMove();
            }
            if (this.scroller.isFinished()) {
                this.scroller = null;
            } else if (this.scroller.computeScrollOffset()) {
                int newX = this.scroller.getCurrX();
                int newY = this.scroller.getCurrY();
                int transX = newX - this.currX;
                int transY = newY - this.currY;
                this.currX = newX;
                this.currY = newY;
                TouchImageViewBase.this.matrix.postTranslate((float) transX, (float) transY);
                TouchImageViewBase.this.fixTrans();
                TouchImageViewBase.this.setImageMatrix(TouchImageViewBase.this.matrix);
                TouchImageViewBase.this.compatPostOnAnimation(this);
            }
        }
    }

    protected class GestureListener extends SimpleOnGestureListener {
        protected GestureListener() {
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (TouchImageViewBase.this.doubleTapListener != null) {
                return TouchImageViewBase.this.doubleTapListener.onSingleTapConfirmed(e);
            }
            return TouchImageViewBase.this.performClick();
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (TouchImageViewBase.this.fling != null) {
                TouchImageViewBase.this.fling.cancelFling();
            }
            TouchImageViewBase.this.fling = new Fling((int) velocityX, (int) velocityY);
            TouchImageViewBase.this.compatPostOnAnimation(TouchImageViewBase.this.fling);
            super.onFling(e1, e2, velocityX, velocityY);
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
            boolean consumed = false;
            if (TouchImageViewBase.this.doubleTapListener != null) {
                consumed = TouchImageViewBase.this.doubleTapListener.onDoubleTap(e);
            }
            if (TouchImageViewBase.this.state == State.NONE) {
                DoubleTapZoom doubleTap;
                float targetZoom = TouchImageViewBase.this.normalizedScale == TouchImageViewBase.this.minScale ? (float) 2 : TouchImageViewBase.this.minScale;
                if (TouchImageViewBase.this.normalizedScale == TouchImageViewBase.this.minScale) {
                    doubleTap = new DoubleTapZoom(targetZoom, e.getX(), e.getY(), false);
                } else {
                    doubleTap = new DoubleTapZoom(targetZoom, (float) (TouchImageViewBase.this.viewWidth / 2), (float) (TouchImageViewBase.this.viewHeight / 2), false);
                }
                TouchImageViewBase.this.compatPostOnAnimation(doubleTap);
                consumed = true;
            }
            CamLog.m3d(CameraConstants.TAG, "consumed : " + consumed);
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            if (TouchImageViewBase.this.doubleTapListener != null) {
                return TouchImageViewBase.this.doubleTapListener.onDoubleTapEvent(e);
            }
            return false;
        }
    }

    public interface OnTouchImageViewListener {
        void onMove();
    }

    protected class PrivateOnTouchListener implements OnTouchListener {
        private int clickDistance = 5;
        private PointF last = new PointF();
        private PointF start = new PointF();

        protected PrivateOnTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (TouchImageViewBase.this.mGet != null && TouchImageViewBase.this.mGet.isSystemUIVisible()) {
                return true;
            }
            if (event.getPointerCount() > 1) {
                TouchImageViewBase.this.mScaleDetector.onTouchEvent(event);
            }
            TouchImageViewBase.this.mGestureDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());
            if (TouchImageViewBase.this.state == State.NONE || TouchImageViewBase.this.state == State.DRAG || TouchImageViewBase.this.state == State.FLING) {
                doTouchEvent(event, curr);
            }
            TouchImageViewBase.this.setImageMatrix(TouchImageViewBase.this.matrix);
            if (TouchImageViewBase.this.userTouchListener != null) {
                TouchImageViewBase.this.userTouchListener.onTouch(v, event);
            }
            if (TouchImageViewBase.this.touchImageViewListener != null) {
                TouchImageViewBase.this.touchImageViewListener.onMove();
            }
            return false;
        }

        private void doTouchEvent(MotionEvent event, PointF curr) {
            switch (event.getAction()) {
                case 0:
                    this.start.set(curr);
                    this.last.set(curr);
                    if (TouchImageViewBase.this.fling != null) {
                        TouchImageViewBase.this.fling.cancelFling();
                    }
                    if (TouchImageViewBase.this.mGet != null) {
                        TouchImageViewBase.this.mGet.onTouchStateChanged(true);
                    }
                    TouchImageViewBase.this.setState(State.DRAG);
                    return;
                case 1:
                case 6:
                    if (TouchImageViewBase.this.mGet != null) {
                        if (Math.abs(curr.x - this.start.x) < ((float) this.clickDistance) || Math.abs(curr.y - this.start.y) < ((float) this.clickDistance)) {
                            TouchImageViewBase.this.mGet.onClicked();
                        }
                        TouchImageViewBase.this.mGet.onTouchStateChanged(false);
                    }
                    TouchImageViewBase.this.setState(State.NONE);
                    return;
                case 2:
                    if (TouchImageViewBase.this.state == State.DRAG) {
                        float deltaY = curr.y - this.last.y;
                        TouchImageViewBase.this.matrix.postTranslate(TouchImageViewBase.this.getFixDragTrans(curr.x - this.last.x, (float) TouchImageViewBase.this.viewWidth, TouchImageViewBase.this.getImageWidth()), TouchImageViewBase.this.getFixDragTrans(deltaY, (float) TouchImageViewBase.this.viewHeight, TouchImageViewBase.this.getImageHeight()));
                        TouchImageViewBase.this.fixTrans();
                        this.last.set(curr.x, curr.y);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected class ScaleListener extends SimpleOnScaleGestureListener {
        protected ScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            TouchImageViewBase.this.setState(State.ZOOM);
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            TouchImageViewBase.this.scaleImage((double) (detector.getCurrentSpan() / detector.getPreviousSpan()), detector.getFocusX(), detector.getFocusY(), true);
            if (TouchImageViewBase.this.touchImageViewListener != null) {
                TouchImageViewBase.this.touchImageViewListener.onMove();
            }
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            TouchImageViewBase.this.setState(State.NONE);
            boolean animateToZoomBoundary = false;
            float targetZoom = TouchImageViewBase.this.normalizedScale;
            if (TouchImageViewBase.this.normalizedScale > TouchImageViewBase.this.maxScale) {
                targetZoom = TouchImageViewBase.this.maxScale;
                animateToZoomBoundary = true;
            } else if (TouchImageViewBase.this.normalizedScale < TouchImageViewBase.this.minScale) {
                targetZoom = TouchImageViewBase.this.minScale;
                animateToZoomBoundary = true;
            }
            if (animateToZoomBoundary) {
                TouchImageViewBase.this.compatPostOnAnimation(new DoubleTapZoom(targetZoom, (float) (TouchImageViewBase.this.viewWidth / 2), (float) (TouchImageViewBase.this.viewHeight / 2), true));
            }
        }
    }

    protected enum State {
        NONE,
        DRAG,
        ZOOM,
        FLING,
        ANIMATE_ZOOM
    }

    protected class ZoomVariables {
        public float focusX;
        public float focusY;
        public boolean isScaleUp = true;
        public float scale;
        public ScaleType scaleType;

        public ZoomVariables(float scale, float focusX, float focusY, ScaleType scaleType, boolean isScaleUp) {
            this.scale = scale;
            this.focusX = focusX;
            this.focusY = focusY;
            this.scaleType = scaleType;
            this.isScaleUp = isScaleUp;
        }
    }

    public TouchImageViewBase(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public TouchImageViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    public TouchImageViewBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.mGestureDetector = new GestureDetector(context, new GestureListener());
        this.matrix = new Matrix();
        this.prevMatrix = new Matrix();
        this.f17m = new float[9];
        this.normalizedScale = 1.0f;
        if (this.mScaleType == null) {
            this.mScaleType = ScaleType.FIT_CENTER;
        }
        this.minScale = 1.0f;
        int[] lcdSize = Utils.getLCDsize(context, true);
        this.maxScale = (float) (Math.max(lcdSize[0], lcdSize[1]) / 425);
        this.superMinScale = this.minScale * 1.0f;
        this.superMaxScale = this.maxScale * 1.0f;
        setImageMatrix(this.matrix);
        setScaleType(ScaleType.MATRIX);
        setState(State.NONE);
        this.onDrawReady = false;
        super.setOnTouchListener(new PrivateOnTouchListener());
    }

    protected void savePreviousImageValues() {
        if (this.matrix != null && this.viewHeight != 0 && this.viewWidth != 0) {
            this.matrix.getValues(this.f17m);
            this.prevMatrix.setValues(this.f17m);
            this.prevMatchViewHeight = this.matchViewHeight;
            this.prevMatchViewWidth = this.matchViewWidth;
            this.prevViewHeight = this.viewHeight;
            this.prevViewWidth = this.viewWidth;
        }
    }

    protected void onDraw(Canvas canvas) {
        this.onDrawReady = true;
        this.imageRenderedAtLeastOnce = true;
        if (this.delayedZoomVariables != null) {
            setZoom(this.delayedZoomVariables.scale, this.delayedZoomVariables.focusX, this.delayedZoomVariables.focusY, this.delayedZoomVariables.scaleType, this.delayedZoomVariables.isScaleUp);
            this.delayedZoomVariables = null;
        }
        super.onDraw(canvas);
    }

    public void setZoom(float scale, float focusX, float focusY, ScaleType scaleType) {
        setZoom(scale, focusX, focusY, scaleType, true);
    }

    public void setZoom(float scale, float focusX, float focusY, ScaleType scaleType, boolean isScaleUp) {
        if (this.onDrawReady) {
            if (scaleType != this.mScaleType) {
                setScaleType(scaleType);
            }
            this.normalizedScale = 1.0f;
            fitImageToView();
            if (isScaleUp) {
                scaleImage((double) scale, (float) (this.viewWidth / 2), (float) (this.viewHeight / 2), true);
            } else {
                scaleImage((double) scale, (float) (this.viewWidth / 2), (float) (this.viewHeight / 2), true, false);
            }
            this.matrix.getValues(this.f17m);
            this.f17m[2] = -((getImageWidth() * focusX) - (((float) this.viewWidth) * 0.5f));
            this.f17m[5] = -((getImageHeight() * focusY) - (((float) this.viewHeight) * 0.5f));
            this.matrix.setValues(this.f17m);
            fixTrans();
            setImageMatrix(this.matrix);
            return;
        }
        this.delayedZoomVariables = new ZoomVariables(scale, focusX, focusY, scaleType, isScaleUp);
    }

    public PointF getScrollPosition() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return null;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        PointF point = transformCoordTouchToBitmap((float) (this.viewWidth / 2), (float) (this.viewHeight / 2), true);
        point.x /= (float) drawableWidth;
        point.y /= (float) drawableHeight;
        return point;
    }

    protected void fixTrans() {
        this.matrix.getValues(this.f17m);
        float transX = this.f17m[2];
        float transY = this.f17m[5];
        float fixTransX = getFixTrans(transX, (float) this.viewWidth, getImageWidth());
        float fixTransY = getFixTrans(transY, (float) this.viewHeight, getImageHeight());
        if (fixTransX != 0.0f || fixTransY != 0.0f) {
            this.matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    protected void fixScaleTrans() {
        fixTrans();
        this.matrix.getValues(this.f17m);
        if (getImageWidth() < ((float) this.viewWidth)) {
            this.f17m[2] = (((float) this.viewWidth) - getImageWidth()) / 2.0f;
        }
        if (getImageHeight() < ((float) this.viewHeight)) {
            this.f17m[5] = (((float) this.viewHeight) - getImageHeight()) / 2.0f;
        }
        this.matrix.setValues(this.f17m);
    }

    protected float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans;
        float maxTrans;
        if (contentSize <= viewSize) {
            minTrans = 0.0f;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0.0f;
        }
        if (trans < minTrans) {
            return (-trans) + minTrans;
        }
        if (trans > maxTrans) {
            return (-trans) + maxTrans;
        }
        return 0.0f;
    }

    protected float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0.0f;
        }
        return delta;
    }

    protected float getImageWidth() {
        return this.matchViewWidth * this.normalizedScale;
    }

    protected float getImageHeight() {
        return this.matchViewHeight * this.normalizedScale;
    }

    protected void fitImageToView() {
        Drawable drawable = getDrawable();
        if (drawable != null && drawable.getIntrinsicWidth() != 0 && drawable.getIntrinsicHeight() != 0 && this.matrix != null && this.prevMatrix != null) {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            float scaleX = ((float) this.viewWidth) / ((float) drawableWidth);
            float scaleY = ((float) this.viewHeight) / ((float) drawableHeight);
            switch (C05541.$SwitchMap$android$widget$ImageView$ScaleType[this.mScaleType.ordinal()]) {
                case 1:
                    scaleY = 1.0f;
                    scaleX = 1.0f;
                    break;
                case 2:
                    scaleY = Math.max(scaleX, scaleY);
                    scaleX = scaleY;
                    break;
                case 3:
                    scaleY = Math.min(1.0f, Math.min(scaleX, scaleY));
                    scaleX = scaleY;
                    break;
                case 4:
                    scaleY = Math.min(scaleX, scaleY);
                    scaleX = scaleY;
                    break;
                case 5:
                    break;
                default:
                    throw new UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END");
            }
            float redundantXSpace = ((float) this.viewWidth) - (((float) drawableWidth) * scaleX);
            float redundantYSpace = ((float) this.viewHeight) - (((float) drawableHeight) * scaleY);
            this.matchViewWidth = ((float) this.viewWidth) - redundantXSpace;
            this.matchViewHeight = ((float) this.viewHeight) - redundantYSpace;
            if (this.normalizedScale != 1.0f || this.imageRenderedAtLeastOnce) {
                if (this.prevMatchViewWidth == 0.0f || this.prevMatchViewHeight == 0.0f) {
                    savePreviousImageValues();
                }
                this.prevMatrix.getValues(this.f17m);
                this.f17m[0] = (this.matchViewWidth / ((float) drawableWidth)) * this.normalizedScale;
                this.f17m[4] = (this.matchViewHeight / ((float) drawableHeight)) * this.normalizedScale;
                float transX = this.f17m[2];
                float transY = this.f17m[5];
                translateMatrixAfterRotate(2, transX, this.prevMatchViewWidth * this.normalizedScale, getImageWidth(), this.prevViewWidth, this.viewWidth, drawableWidth);
                translateMatrixAfterRotate(5, transY, this.prevMatchViewHeight * this.normalizedScale, getImageHeight(), this.prevViewHeight, this.viewHeight, drawableHeight);
                this.matrix.setValues(this.f17m);
            } else {
                this.matrix.setScale(scaleX, scaleY);
                this.matrix.postTranslate(redundantXSpace / 2.0f, redundantYSpace / 2.0f);
                this.normalizedScale = 1.0f;
            }
            fixTrans();
            setImageMatrix(this.matrix);
        }
    }

    protected void translateMatrixAfterRotate(int axis, float trans, float prevImageSize, float imageSize, int prevViewSize, int viewSize, int drawableSize) {
        if (imageSize < ((float) viewSize)) {
            this.f17m[axis] = (((float) viewSize) - (((float) drawableSize) * this.f17m[0])) * 0.5f;
        } else if (trans > 0.0f) {
            this.f17m[axis] = -((imageSize - ((float) viewSize)) * 0.5f);
        } else {
            this.f17m[axis] = -((((Math.abs(trans) + (((float) prevViewSize) * 0.5f)) / prevImageSize) * imageSize) - (((float) viewSize) * 0.5f));
        }
    }

    protected void setState(State state) {
        this.state = state;
    }

    protected void scaleImage(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {
        scaleImage(deltaScale, focusX, focusY, stretchImageToSuper, true);
    }

    protected void scaleImage(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper, boolean isScaleUp) {
        float lowerScale;
        float upperScale;
        if (stretchImageToSuper) {
            lowerScale = this.superMinScale;
            upperScale = this.superMaxScale;
        } else {
            lowerScale = this.minScale;
            upperScale = this.maxScale;
        }
        if (this.mGet != null && Float.compare(this.normalizedScale, 1.0f) == 0) {
            this.mGet.onZoomScaleStart();
        }
        float origScale = this.normalizedScale;
        if (isScaleUp) {
            deltaScale = ((deltaScale - 1.0d) * 1.5d) + 1.0d;
            this.normalizedScale = (float) (((double) this.normalizedScale) * deltaScale);
        } else {
            this.normalizedScale = (float) deltaScale;
        }
        if (this.normalizedScale > upperScale) {
            this.normalizedScale = upperScale;
            deltaScale = (double) (upperScale / origScale);
        } else if (this.normalizedScale < lowerScale) {
            this.normalizedScale = lowerScale;
            deltaScale = (double) (lowerScale / origScale);
        }
        this.matrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
        fixScaleTrans();
    }

    protected PointF transformCoordTouchToBitmap(float x, float y, boolean clipToBitmap) {
        this.matrix.getValues(this.f17m);
        float origW = (float) getDrawable().getIntrinsicWidth();
        float origH = (float) getDrawable().getIntrinsicHeight();
        float transX = this.f17m[2];
        float finalX = ((x - transX) * origW) / getImageWidth();
        float finalY = ((y - this.f17m[5]) * origH) / getImageHeight();
        if (clipToBitmap) {
            finalX = Math.min(Math.max(finalX, 0.0f), origW);
            finalY = Math.min(Math.max(finalY, 0.0f), origH);
        }
        return new PointF(finalX, finalY);
    }

    protected PointF transformCoordBitmapToTouch(float bx, float by) {
        this.matrix.getValues(this.f17m);
        return new PointF(this.f17m[2] + (getImageWidth() * (bx / ((float) getDrawable().getIntrinsicWidth()))), this.f17m[5] + (getImageHeight() * (by / ((float) getDrawable().getIntrinsicHeight()))));
    }

    @TargetApi(16)
    protected void compatPostOnAnimation(Runnable runnable) {
        if (VERSION.SDK_INT >= 16) {
            postOnAnimation(runnable);
        } else {
            postDelayed(runnable, 16);
        }
    }
}
