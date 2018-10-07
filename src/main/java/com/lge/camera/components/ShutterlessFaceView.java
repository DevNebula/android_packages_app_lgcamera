package com.lge.camera.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.FaceCommon;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class ShutterlessFaceView extends View {
    private final int BLING_ANI_INTERVAL = 150;
    private final int BLING_ANI_START_DELAY = 300;
    private final int BLING_DRAWABLE_INDEX_1 = 1;
    private final int BLING_DRAWABLE_INDEX_2 = 2;
    private final int BLING_DRAWABLE_INDEX_3 = 3;
    private final int MAX_FACE_NUM = 10;
    private final int STATE_FACE_DETECTED = 1;
    private final int STATE_FACE_SHUTTERLESS_DETECTED = 2;
    private final int STATE_NO_FACE = 0;
    private int mAniMargin = 0;
    private Drawable mBlingDrawable = null;
    private float mBlingTop;
    private int mDegree;
    private final float[] mFaceBottom = new float[10];
    private Drawable mFaceBoxDrawable;
    private Drawable mFaceBoxDrawableLand;
    private Drawable mFaceBoxDrawablePort;
    private Drawable mFaceBoxGrayDrawable;
    private Drawable mFaceBoxGrayDrawableLand;
    private Drawable mFaceBoxGrayDrawablePort;
    private Drawable mFaceDrawable = null;
    private final float[] mFaceLeft = new float[10];
    private final Rect[] mFaceRect = new Rect[]{new Rect(), new Rect(), new Rect(), new Rect(), new Rect()};
    private int mFaceRectCount;
    private final float[] mFaceRight = new float[10];
    private int mFaceState = 0;
    private final float[] mFaceTop = new float[10];
    private ModuleInterface mGet = null;
    private Handler mHandler = new Handler();
    private boolean mIsBlingAniStarted = false;
    private boolean mIsFaceDetected = false;
    public boolean mIsFirstDraw = true;
    private int mLargestFaceIndex = 0;
    private float mPreviewHeight;
    private float mPreviewWidth;
    private int mStatusBottom;
    private int mStatusLeft;
    private int mStatusRight;
    private float mStatusSize;
    private int mStatusTop;

    public ShutterlessFaceView(Context context, ModuleInterface iModule) {
        super(context);
        this.mGet = iModule;
        init();
    }

    public void setIsFaceDetected(boolean isDetected) {
        this.mIsFaceDetected = isDetected;
        if (isDetected) {
            AudioUtil.performHapticFeedback(this, 65576);
        }
    }

    public void setBlingAnimation(boolean show) {
        if (show) {
            for (int i = 0; i < 3; i++) {
                final int index = i + 1;
                Runnable runnable = new Runnable() {
                    public void run() {
                        ShutterlessFaceView.this.changeBlingDrawable(index);
                    }
                };
                if (this.mHandler != null) {
                    this.mHandler.postDelayed(runnable, (long) ((i * 150) + 300));
                }
            }
            return;
        }
        this.mIsBlingAniStarted = false;
        if (this.mBlingDrawable != null) {
            this.mBlingDrawable.setVisible(false, false);
            this.mBlingDrawable = null;
        }
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void changeBlingDrawable(int index) {
        if (this.mGet != null) {
            this.mBlingDrawable = null;
            Resources resources;
            int i;
            switch (index) {
                case 1:
                    this.mIsBlingAniStarted = true;
                    resources = this.mGet.getAppContext().getResources();
                    i = (this.mDegree == 0 || this.mDegree == 180) ? C0088R.drawable.camera_shutterless_ani_bling_01 : C0088R.drawable.camera_shutterless_ani_bling_01_land;
                    this.mBlingDrawable = resources.getDrawable(i);
                    if (this.mBlingDrawable != null) {
                        this.mBlingDrawable.setVisible(true, true);
                        return;
                    }
                    return;
                case 2:
                    resources = this.mGet.getAppContext().getResources();
                    i = (this.mDegree == 0 || this.mDegree == 180) ? C0088R.drawable.camera_shutterless_ani_bling_02 : C0088R.drawable.camera_shutterless_ani_bling_02_land;
                    this.mBlingDrawable = resources.getDrawable(i);
                    return;
                case 3:
                    resources = this.mGet.getAppContext().getResources();
                    i = (this.mDegree == 0 || this.mDegree == 180) ? C0088R.drawable.camera_shutterless_ani_bling_03 : C0088R.drawable.camera_shutterless_ani_bling_03_land;
                    this.mBlingDrawable = resources.getDrawable(i);
                    return;
                default:
                    return;
            }
        }
    }

    private void init() {
        setWillNotDraw(false);
        this.mIsFirstDraw = true;
        this.mFaceState = 0;
        Resources resources = this.mGet.getAppContext().getResources();
        this.mFaceBoxDrawablePort = resources.getDrawable(C0088R.drawable.camera_focus_face_on);
        this.mFaceBoxDrawableLand = resources.getDrawable(C0088R.drawable.camera_focus_face_on_land);
        this.mFaceBoxDrawable = this.mFaceBoxDrawablePort;
        this.mFaceBoxGrayDrawablePort = resources.getDrawable(C0088R.drawable.camera_focus_face_off);
        this.mFaceBoxGrayDrawableLand = resources.getDrawable(C0088R.drawable.camera_focus_face_off_land);
        this.mFaceBoxGrayDrawable = this.mFaceBoxGrayDrawablePort;
        this.mFaceDrawable = this.mFaceBoxGrayDrawablePort;
        this.mStatusSize = (float) resources.getDrawable(C0088R.drawable.camera_shutterless_ani_bling_01).getIntrinsicWidth();
        this.mBlingTop = (float) (((double) this.mStatusSize) * 0.5d);
        this.mIsFaceDetected = false;
        this.mDegree = this.mGet.getOrientationDegree();
        this.mAniMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.shutterless_face_focus_ani_margin);
    }

    public void drawFaceBox(FaceCommon[] faces, View preview) {
        if (preview == null) {
            this.mFaceRectCount = 0;
            invalidate();
            return;
        }
        this.mFaceRectCount = Math.min(faces.length, this.mFaceRect.length);
        int largestFaceIndex = this.mLargestFaceIndex;
        for (int i = 0; i < this.mFaceRectCount; i++) {
            Rect srcRect = faces[i].getRect();
            this.mFaceRect[i].left = srcRect.left;
            this.mFaceRect[i].top = srcRect.top;
            this.mFaceRect[i].right = srcRect.right;
            this.mFaceRect[i].bottom = srcRect.bottom;
            int newWidth = this.mFaceRect[i].right - this.mFaceRect[i].left;
            if (this.mFaceRect[largestFaceIndex].right - this.mFaceRect[largestFaceIndex].left < newWidth) {
                largestFaceIndex = i;
                int largestFaceWidth = newWidth;
            }
        }
        this.mLargestFaceIndex = largestFaceIndex;
        drawFaceDetectedRect(faces, preview);
        invalidate();
    }

    private void drawFaceDetectedRect(FaceCommon[] faces, View preview) {
        this.mPreviewWidth = (float) preview.getHeight();
        this.mPreviewHeight = (float) preview.getWidth();
        int centerX = ((int) this.mPreviewWidth) / 2;
        int centerY = ((int) this.mPreviewHeight) / 2;
        for (int i = 0; i < this.mFaceRectCount; i++) {
            int sensorPreviewWidth;
            int sensorPreviewHeight;
            if (faces[i].getPreviewWidthOfactiveSize() > 0) {
                sensorPreviewWidth = faces[i].getPreviewWidthOfactiveSize();
            } else {
                sensorPreviewWidth = 2000;
            }
            if (faces[i].getPreviewHeightOfactiveSize() > 0) {
                sensorPreviewHeight = faces[i].getPreviewHeightOfactiveSize();
            } else {
                sensorPreviewHeight = 2000;
            }
            this.mFaceLeft[i] = (float) Math.round(((((float) faces[i].getRect().left) * this.mPreviewWidth) / ((float) sensorPreviewWidth)) + ((float) centerX));
            this.mFaceRight[i] = (float) Math.round(((((float) faces[i].getRect().right) * this.mPreviewWidth) / ((float) sensorPreviewWidth)) + ((float) centerX));
            this.mFaceTop[i] = (float) Math.round(((((float) faces[i].getRect().top) * this.mPreviewHeight) / ((float) sensorPreviewHeight)) + ((float) centerY));
            this.mFaceBottom[i] = (float) Math.round(((((float) faces[i].getRect().bottom) * this.mPreviewHeight) / ((float) sensorPreviewHeight)) + ((float) centerY));
            Rect rect = new Rect((int) this.mFaceLeft[i], (int) this.mFaceTop[i], (int) this.mFaceRight[i], (int) this.mFaceBottom[i]);
            setReverseAndVertical(rect);
            this.mFaceLeft[i] = (float) rect.left;
            this.mFaceTop[i] = (float) rect.top;
            this.mFaceRight[i] = (float) rect.right;
            this.mFaceBottom[i] = (float) rect.bottom;
        }
    }

    private void setReverseAndVertical(Rect rect) {
        int faceWidth = rect.right - rect.left;
        int faceHeight = rect.bottom - rect.top;
        rect.right = ((int) this.mPreviewWidth) - rect.left;
        rect.left = rect.right - faceWidth;
        rect.bottom = ((int) this.mPreviewHeight) - rect.top;
        rect.top = rect.bottom - faceHeight;
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        rect.left = top;
        rect.top = left;
        rect.right = bottom;
        rect.bottom = right;
    }

    public void setDegree(int degree) {
        this.mDegree = degree;
        if (degree == 0 || degree == 180) {
            CamLog.m7i(CameraConstants.TAG, "portrait mode");
            this.mFaceBoxDrawable = this.mFaceBoxDrawablePort;
            this.mFaceBoxGrayDrawable = this.mFaceBoxGrayDrawablePort;
            return;
        }
        CamLog.m7i(CameraConstants.TAG, "landscape mode");
        this.mFaceBoxDrawable = this.mFaceBoxDrawableLand;
        this.mFaceBoxGrayDrawable = this.mFaceBoxGrayDrawableLand;
    }

    private void changeFaceState() {
        if (this.mFaceRectCount == 0) {
            this.mFaceState = 0;
        } else if (this.mIsFaceDetected) {
            this.mFaceState = 2;
        } else {
            this.mFaceState = 1;
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mIsFirstDraw) {
            this.mFaceRectCount = 0;
            this.mIsFirstDraw = false;
            return;
        }
        changeFaceState();
        int i = 0;
        while (i < this.mFaceRectCount) {
            int left = (int) this.mFaceLeft[i];
            int top = (int) this.mFaceTop[i];
            int right = (int) this.mFaceRight[i];
            int bottom = (int) this.mFaceBottom[i];
            if (this.mFaceState == 2 && i == this.mLargestFaceIndex) {
                this.mFaceDrawable = this.mFaceBoxDrawable;
                if (this.mIsBlingAniStarted) {
                    setBlingDrawableLocation();
                    switch (this.mDegree) {
                        case 0:
                            this.mStatusLeft = (int) (((float) left) - (this.mStatusSize + ((float) this.mAniMargin)));
                            this.mStatusTop = (int) (((float) top) + this.mBlingTop);
                            break;
                        case 90:
                            this.mStatusLeft = (int) (((float) left) + this.mBlingTop);
                            this.mStatusTop = (int) (((double) bottom) + (((double) this.mStatusSize) * 0.1d));
                            break;
                        case 180:
                            this.mStatusLeft = this.mAniMargin + right;
                            this.mStatusTop = (int) (((float) bottom) - (this.mStatusSize + this.mBlingTop));
                            break;
                        case 270:
                            this.mStatusLeft = (int) (((float) right) - (this.mStatusSize + this.mBlingTop));
                            this.mStatusTop = (int) (((double) top) - (((double) this.mStatusSize) * 1.1d));
                            break;
                    }
                    this.mStatusRight = (int) (((float) this.mStatusLeft) + this.mStatusSize);
                    this.mStatusBottom = (int) (((float) this.mStatusTop) + this.mStatusSize);
                    this.mBlingDrawable.setBounds(this.mStatusLeft, this.mStatusTop, this.mStatusRight, this.mStatusBottom);
                    this.mBlingDrawable.draw(canvas);
                }
            } else {
                this.mFaceDrawable = this.mFaceBoxGrayDrawable;
            }
            this.mFaceDrawable.setBounds(left, top, right, bottom);
            this.mFaceDrawable.draw(canvas);
            i++;
        }
    }

    public void clearFaceCoordinate() {
        if (this.mFaceRect != null) {
            CamLog.m3d(CameraConstants.TAG, "-sh- clearFaceCoordinate");
            for (Rect empty : this.mFaceRect) {
                empty.setEmpty();
            }
        }
    }

    private void setBlingDrawableLocation() {
        this.mBlingTop = (float) (this.mStatusSize >= ((float) (this.mFaceRect[this.mLargestFaceIndex].height() / 3)) ? -((int) (this.mStatusSize / 4.0f)) : this.mFaceRect[this.mLargestFaceIndex].height() / 8);
    }

    public void releaseLayout() {
        this.mIsBlingAniStarted = false;
        this.mFaceDrawable = null;
        this.mFaceBoxDrawable = null;
        this.mFaceBoxDrawablePort = null;
        this.mFaceBoxDrawableLand = null;
        this.mFaceBoxGrayDrawable = null;
        this.mFaceBoxGrayDrawablePort = null;
        this.mFaceBoxGrayDrawableLand = null;
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler = null;
        }
    }
}
