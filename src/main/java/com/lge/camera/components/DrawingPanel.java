package com.lge.camera.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.p000v4.internal.view.SupportMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.util.Utils;

public class DrawingPanel extends View {
    public static final int DIRECTION_BOTTOM_TO_TOP = 24;
    public static final int DIRECTION_LEFT_TO_RIGHT = 21;
    public static final int DIRECTION_RIGHT_TO_LEFT = 22;
    public static final int DIRECTION_TOP_TO_BOTTOM = 23;
    public static final int DIRECTION_UNKNOWN = 0;
    public static final int STATUS_OK = 0;
    public static final int STATUS_WARNING_WRONG_DIRECTION_DOWN = 102;
    public static final int STATUS_WARNING_WRONG_DIRECTION_LEFT = 103;
    public static final int STATUS_WARNING_WRONG_DIRECTION_RIGHT = 104;
    public static final int STATUS_WARNING_WRONG_DIRECTION_UP = 101;
    public static final int STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_DOWN = 1102;
    public static final int STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_LEFT = 1103;
    public static final int STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_RIGHT = 1104;
    public static final int STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_UP = 1101;
    private final float ADJ_RATIO = 1.24f;
    private Rect mAdjRect;
    protected Drawable mArrowDown = null;
    protected Drawable mArrowLeft = null;
    protected Drawable mArrowRight = null;
    protected Drawable mArrowUp = null;
    protected Bitmap mBitmap = null;
    protected Paint mBorderPaint = new Paint();
    protected int mCaptureDirection = 0;
    protected Paint mCenterPaint = new Paint();
    protected Paint mCursorPaint = new Paint();
    protected Paint mDebugPaint = new Paint();
    protected int mDistanceFromGuideFrame = 0;
    protected Point mGuideFrameCenter = new Point(0, 0);
    protected int mGuideHeight = 0;
    protected int mGuideWidth = 0;
    protected int mHeight = 2560;
    protected DrawingPanelListener mListener = null;
    protected boolean mNeedAdj = false;
    protected int mPanoPrevSizeH = 0;
    protected int mPanoPrevSizeW = 0;
    protected Point mPreviewTopLeft = new Point(0, 0);
    protected int mSideLength = 160;
    public float mStopMarginRatio = 0.0f;
    protected Matrix mTransformation = new Matrix();
    protected int mWarningStatus = 0;
    public float mWideRatio = 1.0f;
    protected int mWidth = CameraConstantsEx.QHD_SCREEN_RESOLUTION;

    public interface DrawingPanelListener {
        void onCompleted();
    }

    public DrawingPanel(Context context) {
        super(context);
        init(context);
    }

    public DrawingPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public void setListener(DrawingPanelListener listener) {
        this.mListener = listener;
    }

    public void unbind() {
        this.mArrowRight = null;
        this.mArrowLeft = null;
        this.mArrowUp = null;
        this.mArrowDown = null;
        if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        this.mListener = null;
    }

    protected void init(Context context) {
        this.mCursorPaint.setColor(-1);
        this.mCursorPaint.setStyle(Style.STROKE);
        this.mCursorPaint.setStrokeWidth((float) Utils.getPx(context, C0088R.dimen.panorama_guide_box_line));
        this.mBorderPaint.setColor(getContext().getColor(C0088R.color.panorama_panel_border));
        this.mBorderPaint.setStyle(Style.STROKE);
        this.mBorderPaint.setStrokeWidth((float) Utils.getPx(context, C0088R.dimen.panorama_border_line_width));
        this.mCenterPaint.setColor(Color.argb(255, 255, 242, 0));
        this.mCenterPaint.setStyle(Style.STROKE);
        this.mCenterPaint.setStrokeWidth((float) Utils.getPx(context, C0088R.dimen.panorama_center_line_width));
        this.mDebugPaint.setColor(SupportMenu.CATEGORY_MASK);
        this.mDebugPaint.setStyle(Style.STROKE);
        this.mDebugPaint.setStrokeWidth(20.0f);
        this.mTransformation.reset();
        this.mWarningStatus = 0;
        this.mSideLength = Utils.getPx(context, C0088R.dimen.panorama_arrow_length);
    }

    public void setArrowRes(int idRight, int idLeft, int idUp, int idDown) {
        this.mArrowRight = getResources().getDrawable(idRight);
        this.mArrowLeft = getResources().getDrawable(idLeft);
        this.mArrowUp = getResources().getDrawable(idUp);
        this.mArrowDown = getResources().getDrawable(idDown);
    }

    public void setWarningState(int statusWarning) {
        if (this.mWarningStatus != statusWarning) {
            this.mWarningStatus = statusWarning;
            if (statusWarning == 101 || statusWarning == 102 || statusWarning == 103 || statusWarning == 104) {
                this.mCursorPaint.setColor(Color.argb(255, 224, 38, 83));
            } else if (statusWarning == STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_UP || statusWarning == STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_DOWN || statusWarning == STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_LEFT || statusWarning == STATUS_WARNING_WRONG_TOO_MUCH_DIRECTION_RIGHT) {
                this.mCursorPaint.setColor(Color.argb(255, 29, 219, 217));
            } else {
                this.mCursorPaint.setColor(-1);
            }
        }
    }

    protected void drawArrowMovement(Canvas canvas, int leftGuideFrame, int topGuideFrame, int bottomGuideFrame, int rightGuideFrame) {
        int left_ = (this.mGuideFrameCenter.x - (this.mSideLength / 2)) + this.mPreviewTopLeft.x;
        int top_ = (this.mGuideFrameCenter.y - (this.mSideLength / 2)) + this.mPreviewTopLeft.y;
        int right_ = (this.mGuideFrameCenter.x + (this.mSideLength / 2)) + this.mPreviewTopLeft.x;
        int bottom_ = (this.mGuideFrameCenter.y + (this.mSideLength / 2)) + this.mPreviewTopLeft.y;
        int panoPrevWidth = this.mPanoPrevSizeW;
        boolean isCompleted = false;
        int l;
        int r;
        int end;
        int t;
        int b;
        if (this.mCaptureDirection == 21) {
            l = rightGuideFrame + this.mDistanceFromGuideFrame;
            r = (this.mDistanceFromGuideFrame + rightGuideFrame) + this.mSideLength;
            end = this.mPreviewTopLeft.x + panoPrevWidth;
            if (r >= end) {
                l = Math.min(end, panoPrevWidth - this.mSideLength);
                r = l + this.mSideLength;
            }
            this.mArrowRight.setBounds(l, top_, r, bottom_);
            this.mArrowRight.draw(canvas);
            isCompleted = ((float) rightGuideFrame) >= ((float) end) + (((float) panoPrevWidth) * this.mStopMarginRatio);
        } else if (this.mCaptureDirection == 22) {
            l = (leftGuideFrame - this.mDistanceFromGuideFrame) - this.mSideLength;
            r = leftGuideFrame - this.mDistanceFromGuideFrame;
            end = this.mPreviewTopLeft.x;
            if (l <= end) {
                l = Math.max(0, end - this.mSideLength);
                r = l + this.mSideLength;
            }
            this.mArrowLeft.setBounds(l, top_, r, bottom_);
            this.mArrowLeft.draw(canvas);
            isCompleted = ((float) leftGuideFrame) <= ((float) end) - (((float) panoPrevWidth) * this.mStopMarginRatio);
        } else if (this.mCaptureDirection == 23) {
            t = bottomGuideFrame + this.mDistanceFromGuideFrame;
            b = (this.mDistanceFromGuideFrame + bottomGuideFrame) + this.mSideLength;
            end = this.mPreviewTopLeft.y + this.mPanoPrevSizeH;
            if (b >= end) {
                t = end - this.mSideLength;
                b = end;
            }
            this.mArrowDown.setBounds(left_, t, right_, b);
            this.mArrowDown.draw(canvas);
            isCompleted = ((float) bottomGuideFrame) >= ((float) end) + (((float) panoPrevWidth) * this.mStopMarginRatio);
        } else if (this.mCaptureDirection == 24) {
            t = (topGuideFrame - this.mDistanceFromGuideFrame) - this.mSideLength;
            b = topGuideFrame - this.mDistanceFromGuideFrame;
            end = this.mPreviewTopLeft.y;
            if (t <= end) {
                t = end;
                b = end + this.mSideLength;
            }
            this.mArrowUp.setBounds(left_, t, right_, b);
            this.mArrowUp.draw(canvas);
            isCompleted = ((float) bottomGuideFrame) <= ((float) end) - (((float) panoPrevWidth) * this.mStopMarginRatio);
        }
        if (this.mListener != null && isCompleted) {
            this.mListener.onCompleted();
        }
    }

    public int getPanelHeight() {
        return this.mHeight;
    }

    public void setPreviewParameters(Size horizontalPreviewSize, Size verticalPreviewSize, int captureDirection, Size cameraFrameSize, int frameRotationCorrection, int panoramaOutputMaxLength, Size previewSize, boolean needAdj) {
        this.mCaptureDirection = captureDirection;
        this.mGuideFrameCenter.x = 0;
        this.mGuideFrameCenter.y = 0;
        float resizeRatioX = 1.0f;
        float resizeRatioY = 1.0f;
        int previewFrameWidth;
        int previewFrameHeight;
        if (captureDirection == 21 || captureDirection == 22) {
            if (panoramaOutputMaxLength != 0) {
                resizeRatioY = ((float) horizontalPreviewSize.getHeight()) / ((float) panoramaOutputMaxLength);
                resizeRatioX = ((float) horizontalPreviewSize.getWidth()) / ((float) panoramaOutputMaxLength);
            }
            if (frameRotationCorrection == 0 || frameRotationCorrection == 180) {
                previewFrameWidth = (int) (((float) cameraFrameSize.getWidth()) * resizeRatioX);
                previewFrameHeight = horizontalPreviewSize.getHeight();
            } else {
                previewFrameHeight = horizontalPreviewSize.getHeight();
                previewFrameWidth = (int) (((float) cameraFrameSize.getHeight()) * resizeRatioX);
            }
            this.mPanoPrevSizeW = horizontalPreviewSize.getWidth();
            this.mPanoPrevSizeH = horizontalPreviewSize.getHeight();
            this.mGuideHeight = this.mPanoPrevSizeH;
            this.mGuideWidth = (int) (((float) previewFrameWidth) * this.mWideRatio);
            this.mPreviewTopLeft.x = 0;
            this.mPreviewTopLeft.y = (this.mHeight / 2) - (this.mGuideHeight / 2);
        } else {
            if (panoramaOutputMaxLength != 0) {
                resizeRatioX = ((float) verticalPreviewSize.getWidth()) / ((float) panoramaOutputMaxLength);
                resizeRatioY = ((float) verticalPreviewSize.getHeight()) / ((float) panoramaOutputMaxLength);
            }
            if (frameRotationCorrection == 0 || frameRotationCorrection == 180) {
                previewFrameWidth = verticalPreviewSize.getWidth();
                previewFrameHeight = (int) (((float) cameraFrameSize.getHeight()) * resizeRatioY);
            } else {
                previewFrameHeight = (int) (((float) cameraFrameSize.getWidth()) * resizeRatioY);
                previewFrameWidth = verticalPreviewSize.getWidth();
            }
            this.mPanoPrevSizeW = verticalPreviewSize.getWidth();
            this.mPanoPrevSizeH = verticalPreviewSize.getHeight();
            this.mGuideWidth = this.mPanoPrevSizeW;
            this.mGuideHeight = (int) (((float) previewFrameHeight) * this.mWideRatio);
            this.mPreviewTopLeft.x = (this.mWidth / 2) - (this.mGuideWidth / 2);
            this.mPreviewTopLeft.y = 0;
        }
        setPanoPreviewBitmap(previewSize, needAdj);
    }

    public void setPanoPreviewBitmap(Size previewSize, boolean needAdj) {
        this.mNeedAdj = needAdj;
        this.mBitmap = Bitmap.createBitmap(previewSize.getWidth(), previewSize.getHeight(), Config.ARGB_8888);
        if (this.mNeedAdj) {
            if (this.mCaptureDirection == 21) {
                this.mAdjRect = new Rect(this.mPreviewTopLeft.x, this.mPreviewTopLeft.y, (int) (((float) this.mPreviewTopLeft.x) + (((float) this.mPanoPrevSizeW) * 1.24f)), this.mPreviewTopLeft.y + this.mPanoPrevSizeH);
            } else {
                this.mAdjRect = new Rect((int) (((float) this.mPreviewTopLeft.x) - (((float) this.mPanoPrevSizeW) * 0.24000001f)), this.mPreviewTopLeft.y, this.mPreviewTopLeft.x + this.mPanoPrevSizeW, this.mPreviewTopLeft.y + this.mPanoPrevSizeH);
            }
            this.mGuideWidth = (int) (((float) this.mGuideWidth) * 1.24f);
        }
        this.mTransformation.setTranslate((float) this.mPreviewTopLeft.x, (float) this.mPreviewTopLeft.y);
    }

    public int getPreviewWidth() {
        return this.mPanoPrevSizeW;
    }

    public int getPreviewHeight() {
        return this.mPanoPrevSizeH;
    }

    public Bitmap getPanoPrevBitmap() {
        return this.mBitmap;
    }

    public void setArrowPos(int x_pos, int y_pox) {
        if (this.mNeedAdj) {
            if (this.mCaptureDirection == 21) {
                x_pos = (int) (((float) x_pos) * 1.24f);
            } else {
                x_pos = (int) (((float) this.mPanoPrevSizeW) - (((float) (this.mPanoPrevSizeW - x_pos)) * 1.24f));
            }
        }
        this.mGuideFrameCenter.set(x_pos, y_pox);
    }

    public void onDraw(Canvas canvas) {
        if (this.mBitmap != null) {
            int center = this.mPreviewTopLeft.y + (this.mPanoPrevSizeH / 2);
            canvas.drawLine((float) this.mPreviewTopLeft.x, (float) center, (float) (this.mPreviewTopLeft.x + this.mPanoPrevSizeW), (float) center, this.mCenterPaint);
            canvas.drawLine((float) this.mPreviewTopLeft.x, (float) this.mPreviewTopLeft.y, (float) this.mPreviewTopLeft.x, (float) (this.mPreviewTopLeft.y + this.mPanoPrevSizeH), this.mBorderPaint);
            canvas.drawLine((float) (this.mPreviewTopLeft.x + this.mPanoPrevSizeW), (float) this.mPreviewTopLeft.y, (float) (this.mPreviewTopLeft.x + this.mPanoPrevSizeW), (float) (this.mPreviewTopLeft.y + this.mPanoPrevSizeH), this.mBorderPaint);
            if (this.mNeedAdj) {
                canvas.drawBitmap(this.mBitmap, null, this.mAdjRect, null);
            } else {
                canvas.drawBitmap(this.mBitmap, this.mTransformation, null);
            }
            canvas.drawLine((float) this.mPreviewTopLeft.x, (float) this.mPreviewTopLeft.y, (float) (this.mPreviewTopLeft.x + this.mPanoPrevSizeW), (float) this.mPreviewTopLeft.y, this.mBorderPaint);
            canvas.drawLine((float) this.mPreviewTopLeft.x, (float) (this.mPreviewTopLeft.y + this.mPanoPrevSizeH), (float) (this.mPreviewTopLeft.x + this.mPanoPrevSizeW), (float) (this.mPreviewTopLeft.y + this.mPanoPrevSizeH), this.mBorderPaint);
            if (this.mGuideFrameCenter.x != 0 && this.mGuideFrameCenter.y != 0) {
                int bottom = (this.mGuideFrameCenter.y + (this.mGuideHeight / 2)) + this.mPreviewTopLeft.y;
                drawArrowMovement(canvas, (this.mGuideFrameCenter.x - (this.mGuideWidth / 2)) + this.mPreviewTopLeft.x, (this.mGuideFrameCenter.y - (this.mGuideHeight / 2)) + this.mPreviewTopLeft.y, bottom, (this.mGuideFrameCenter.x + (this.mGuideWidth / 2)) + this.mPreviewTopLeft.x);
            }
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        Log.d(CameraConstants.TAG, "onSizeChanged: w x h = " + this.mWidth + ", " + this.mHeight);
    }
}
