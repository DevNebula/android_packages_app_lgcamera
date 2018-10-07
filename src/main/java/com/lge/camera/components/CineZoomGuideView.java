package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class CineZoomGuideView extends View implements OnScaleGestureListener {
    static Context sContext = null;
    public final int CAM_DEVICE_HEIGHT = 4656;
    public int CAM_DEVICE_WIDTH = 2620;
    protected final int SCALE_TO_LB = 3;
    protected final int SCALE_TO_LT = 1;
    protected final int SCALE_TO_RB = 4;
    protected final int SCALE_TO_RT = 2;
    protected final int STATUS_IDLE = 0;
    protected final int STATUS_INVALID = 4;
    protected final int STATUS_MOVE = 1;
    protected final int STATUS_SCALE = 2;
    public int mCAM_SCREEN_HEIGHT = 2560;
    public int mCAM_SCREEN_HEIGHT_MIN = 640;
    public int mCAM_SCREEN_WIDTH = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    public int mCAM_SCREEN_WIDTH_MIN = 360;
    protected int mDirection;
    private float mGapTop = 0.0f;
    public Rect mGuideRect = new Rect();
    public RectF mGuideRectF = new RectF();
    protected NinePatchDrawable mGuideView;
    private int mHeight = (this.mCAM_SCREEN_HEIGHT / 2);
    private boolean mIs16BY9 = false;
    private boolean mIsScaleByX = true;
    private float mLastX;
    private float mLastY;
    protected Object mLock = new Object();
    protected int mMARGIN = (this.mCAM_SCREEN_WIDTH / 15);
    private float mNeoGapTop = 0.0f;
    private float mPosX = ((float) (this.mCAM_SCREEN_WIDTH / 4));
    private float mPosY = ((float) ((this.mCAM_SCREEN_HEIGHT / 4) + 150));
    public float mRATIO_H = (4656.0f / ((float) this.mCAM_SCREEN_HEIGHT));
    public float mRATIO_SCREEN = ((((float) this.mCAM_SCREEN_HEIGHT) * 1.0f) / ((float) this.mCAM_SCREEN_WIDTH));
    public float mRATIO_W = ((((float) this.CAM_DEVICE_WIDTH) * 1.0f) / ((float) this.mCAM_SCREEN_WIDTH));
    private ScaleGestureDetector mScaleGestureDetector;
    public Rect mScreen;
    protected RectF mScreenF;
    protected int mStatus;
    private int mWidth = (this.mCAM_SCREEN_WIDTH / 2);

    public CineZoomGuideView(Context context) {
        super(context);
        CamLog.m3d(CameraConstants.TAG, "CineZoomGuideView - create");
        sContext = context;
        this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.mGuideView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_cursor);
        initConstants(context);
    }

    public CineZoomGuideView(Context context, boolean is16BY9) {
        super(context);
        CamLog.m3d(CameraConstants.TAG, "CineZoomGuideView - create");
        sContext = context;
        this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.mGuideView = (NinePatchDrawable) getContext().getDrawable(C0088R.drawable.camera_cine_zoom_cursor);
        this.mIs16BY9 = is16BY9;
        initConstants(context);
    }

    private void initConstants(Context context) {
        if (FunctionProperties.getSupportedHal() == 2) {
            this.CAM_DEVICE_WIDTH = 3492;
        }
        int[] lcdSize = Utils.getLCDsize(context, true);
        if (this.mIs16BY9) {
            this.mCAM_SCREEN_WIDTH = lcdSize[1];
            this.mCAM_SCREEN_HEIGHT = (lcdSize[1] * 16) / 9;
            this.mGapTop = ((float) ((lcdSize[0] - this.mCAM_SCREEN_HEIGHT) / 2)) * 1.0f;
            this.mNeoGapTop = 0.0f;
        } else {
            this.mCAM_SCREEN_WIDTH = lcdSize[1];
            this.mCAM_SCREEN_HEIGHT = lcdSize[0];
            this.mGapTop = 0.0f;
            if (ModelProperties.getLCDType() == 2) {
                this.mNeoGapTop = (float) RatioCalcUtil.getNotchDisplayHeight(sContext);
            }
        }
        this.mCAM_SCREEN_WIDTH_MIN = this.mCAM_SCREEN_WIDTH / 4;
        this.mCAM_SCREEN_HEIGHT_MIN = this.mCAM_SCREEN_HEIGHT / 4;
        this.mRATIO_H = 4656.0f / ((float) this.mCAM_SCREEN_HEIGHT);
        this.mRATIO_W = (((float) this.CAM_DEVICE_WIDTH) * 1.0f) / ((float) this.mCAM_SCREEN_WIDTH);
        this.mRATIO_SCREEN = (((float) this.mCAM_SCREEN_HEIGHT) * 1.0f) / ((float) this.mCAM_SCREEN_WIDTH);
        this.mMARGIN = this.mCAM_SCREEN_WIDTH / 15;
        this.mWidth = this.mCAM_SCREEN_WIDTH / 2;
        this.mHeight = this.mCAM_SCREEN_HEIGHT / 2;
        this.mWidth = this.mCAM_SCREEN_WIDTH_MIN;
        this.mHeight = this.mCAM_SCREEN_HEIGHT_MIN;
        this.mPosX = (float) ((this.mCAM_SCREEN_WIDTH - this.mCAM_SCREEN_WIDTH_MIN) / 2);
        this.mPosY = (((float) ((this.mCAM_SCREEN_HEIGHT - this.mCAM_SCREEN_HEIGHT_MIN) / 2)) + this.mGapTop) + this.mNeoGapTop;
        this.mScreenF = new RectF(0.0f, 0.0f, (float) this.mCAM_SCREEN_WIDTH, (float) this.mCAM_SCREEN_HEIGHT);
        this.mScreen = new Rect(0, 0, this.mCAM_SCREEN_WIDTH, this.mCAM_SCREEN_HEIGHT);
        setBounds();
    }

    public void setupVideosize(boolean is16BY9) {
        this.mIs16BY9 = is16BY9;
        initConstants(sContext);
    }

    public boolean onScale(ScaleGestureDetector detector) {
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector arg0) {
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector arg0) {
    }

    protected void onDraw(Canvas canvas) {
        this.mGuideView.draw(canvas);
    }

    protected void setBounds() {
        if (this.mGuideView != null) {
            this.mGuideView.setBounds((int) this.mPosX, (int) this.mPosY, ((int) this.mPosX) + this.mWidth, ((int) this.mPosY) + this.mHeight);
        }
    }

    public RectF getGuideRectF() {
        if (this.mGuideRectF != null) {
            float y = (this.mPosY - this.mGapTop) - this.mNeoGapTop;
            float left = y * this.mRATIO_H;
            float top = ((((float) this.mCAM_SCREEN_WIDTH) - this.mPosX) - ((float) this.mWidth)) * this.mRATIO_W;
            float right = (((float) this.mHeight) + y) * this.mRATIO_H;
            float bottom = (((float) this.mCAM_SCREEN_WIDTH) - this.mPosX) * this.mRATIO_W;
            if (right >= 4656.0f) {
                right = 4655.0f;
            }
            if (bottom >= ((float) this.CAM_DEVICE_WIDTH) * 1.0f) {
                bottom = (((float) this.CAM_DEVICE_WIDTH) * 1.0f) - 1.0f;
            }
            this.mGuideRectF.set(left, top, right, bottom);
        }
        return this.mGuideRectF;
    }

    public Rect getGuideRect() {
        if (this.mGuideRect != null) {
            this.mGuideRect.set((int) this.mPosX, (int) this.mPosY, ((int) this.mPosX) + this.mWidth, ((int) this.mPosY) + this.mHeight);
        }
        return this.mGuideRect;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        int action = event.getActionMasked();
        synchronized (this.mLock) {
            float x = event.getX();
            float y = event.getY();
            if (this.mScaleGestureDetector != null) {
                this.mScaleGestureDetector.onTouchEvent(event);
            }
            switch (action) {
                case 0:
                    result = onAcationDown(x, y);
                    break;
                case 1:
                    this.mStatus = 0;
                    break;
                case 2:
                    result = onAcationMove(x, y);
                    break;
            }
        }
        return result;
    }

    protected boolean onAcationDown(float x, float y) {
        if (y > (((float) this.mCAM_SCREEN_HEIGHT) + this.mGapTop) + this.mNeoGapTop) {
            return false;
        }
        this.mPosX = (float) ((int) (x - ((float) (this.mCAM_SCREEN_WIDTH_MIN / 2))));
        this.mPosY = (float) ((int) (y - ((float) (this.mCAM_SCREEN_HEIGHT_MIN / 2))));
        if (this.mPosX < 0.0f) {
            this.mPosX = 0.0f;
        }
        if (this.mPosY < this.mGapTop) {
            this.mPosY = this.mGapTop;
        }
        if (this.mPosX + ((float) this.mWidth) > ((float) this.mCAM_SCREEN_WIDTH)) {
            this.mPosX = (float) (this.mCAM_SCREEN_WIDTH - this.mWidth);
        }
        if (this.mPosY + ((float) this.mHeight) > ((float) this.mCAM_SCREEN_HEIGHT) + this.mGapTop) {
            this.mPosY = ((float) (this.mCAM_SCREEN_HEIGHT - this.mHeight)) + this.mGapTop;
        }
        if (this.mNeoGapTop != 0.0f && this.mPosY < this.mNeoGapTop) {
            this.mPosY = this.mNeoGapTop;
        }
        setBounds();
        invalidate();
        return true;
    }

    protected boolean onAcationMove(float x, float y) {
        if (this.mStatus != 4) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "CineZoomGuideView - onTouchEvent invalid");
        return false;
    }

    protected void onScale(float dx, float dy) {
        switch (this.mDirection) {
            case 1:
                onScaleToLT(dx, dy);
                return;
            case 2:
                onScaleToRT(dx, dy);
                return;
            case 3:
                onScaleToLB(dx, dy);
                return;
            case 4:
                onScaleToRB(dx, dy);
                return;
            default:
                return;
        }
    }

    protected void onScaleToLT(float scaleX, float scaleY) {
        if (this.mIsScaleByX) {
            if (this.mPosX + scaleX >= 0.0f && this.mPosY + (this.mRATIO_SCREEN * scaleX) >= 0.0f && ((float) this.mWidth) - scaleX >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) - (this.mRATIO_SCREEN * scaleX) >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
                this.mPosX += scaleX;
                this.mWidth = (int) (((float) this.mWidth) - scaleX);
                this.mPosY += this.mRATIO_SCREEN * scaleX;
                this.mHeight = (int) (((float) this.mWidth) * this.mRATIO_SCREEN);
            }
        } else if (this.mPosX + (scaleY / this.mRATIO_SCREEN) >= 0.0f && this.mPosY + scaleY >= 0.0f && ((float) this.mWidth) - (scaleY / this.mRATIO_SCREEN) >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) - scaleY >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
            this.mPosY += scaleY;
            this.mHeight = (int) (((float) this.mHeight) - scaleY);
            this.mPosX += scaleY / this.mRATIO_SCREEN;
            this.mWidth = (int) (((float) this.mHeight) / this.mRATIO_SCREEN);
        }
    }

    protected void onScaleToRT(float scaleX, float scaleY) {
        if (this.mIsScaleByX) {
            if ((this.mPosX + ((float) this.mWidth)) + scaleX <= ((float) this.mCAM_SCREEN_WIDTH) && this.mPosY - (this.mRATIO_SCREEN * scaleX) >= 0.0f && ((float) this.mWidth) + scaleX >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) + (this.mRATIO_SCREEN * scaleX) >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
                this.mPosY -= this.mRATIO_SCREEN * scaleX;
                this.mWidth = (int) (((float) this.mWidth) + scaleX);
                this.mHeight = (int) (((float) this.mWidth) * this.mRATIO_SCREEN);
            }
        } else if ((this.mPosX + ((float) this.mWidth)) - (scaleY / this.mRATIO_SCREEN) <= ((float) this.mCAM_SCREEN_WIDTH) && this.mPosY + scaleY >= 0.0f && ((float) this.mWidth) - (scaleY / this.mRATIO_SCREEN) >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) - scaleY >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
            this.mPosY += scaleY;
            this.mHeight = (int) (((float) this.mHeight) - scaleY);
            this.mWidth = (int) (((float) this.mHeight) / this.mRATIO_SCREEN);
        }
    }

    protected void onScaleToLB(float scaleX, float scaleY) {
        if (this.mIsScaleByX) {
            if (this.mPosX + scaleX >= 0.0f && (this.mPosY - (this.mRATIO_SCREEN * scaleX)) + ((float) this.mHeight) <= ((float) this.mCAM_SCREEN_HEIGHT) && ((float) this.mWidth) - scaleX >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) - (this.mRATIO_SCREEN * scaleX) >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
                this.mPosX += scaleX;
                this.mWidth = (int) (((float) this.mWidth) - scaleX);
                this.mHeight = (int) (((float) this.mWidth) * this.mRATIO_SCREEN);
            }
        } else if (this.mPosX - (scaleY / this.mRATIO_SCREEN) >= 0.0f && (this.mPosY + scaleY) + ((float) this.mHeight) <= ((float) this.mCAM_SCREEN_HEIGHT) && ((float) this.mWidth) + (scaleY / this.mRATIO_SCREEN) >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) + scaleY >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
            this.mPosX -= scaleY / this.mRATIO_SCREEN;
            this.mHeight = (int) (((float) this.mHeight) + scaleY);
            this.mWidth = (int) (((float) this.mHeight) / this.mRATIO_SCREEN);
        }
    }

    protected void onScaleToRB(float scaleX, float scaleY) {
        if (this.mIsScaleByX) {
            if ((this.mPosX + ((float) this.mWidth)) + scaleX < ((float) this.mCAM_SCREEN_WIDTH) && (this.mPosY + ((float) this.mHeight)) + (this.mRATIO_SCREEN * scaleX) < ((float) this.mCAM_SCREEN_HEIGHT) && ((float) this.mWidth) + scaleX >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) + (this.mRATIO_SCREEN * scaleX) >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
                this.mWidth = (int) (((float) this.mWidth) + scaleX);
                this.mHeight = (int) (((float) this.mWidth) * this.mRATIO_SCREEN);
            }
        } else if ((this.mPosX + ((float) this.mWidth)) + (scaleY / this.mRATIO_SCREEN) < ((float) this.mCAM_SCREEN_WIDTH) && (this.mPosY + ((float) this.mHeight)) + scaleY < ((float) this.mCAM_SCREEN_HEIGHT) && ((float) this.mWidth) + (scaleY / this.mRATIO_SCREEN) >= ((float) this.mCAM_SCREEN_WIDTH_MIN) && ((float) this.mHeight) + scaleY >= ((float) this.mCAM_SCREEN_HEIGHT_MIN)) {
            this.mHeight = (int) (((float) this.mHeight) + scaleY);
            this.mWidth = (int) (((float) this.mHeight) / this.mRATIO_SCREEN);
        }
    }
}
