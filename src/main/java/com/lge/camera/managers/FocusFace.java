package com.lge.camera.managers;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.FaceCommon;
import com.lge.camera.device.ICameraCallback.CameraAFCallback;
import com.lge.camera.device.ICameraCallback.CameraFaceDetectionCallback;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;

public class FocusFace extends FocusBaseMulti {
    private static final int MAX_FACE_NUM = 5;
    private static final int RESET_FACE_DELAY = 500;
    private static boolean sFaceDetectionStarted = false;
    private float mCenter_x = 0.0f;
    private float mCenter_y = 0.0f;
    private int mCheckNoneFaceCount = 0;
    private int mCurrentFaceCount = 0;
    private Rect[] mDetectedFaces = new Rect[]{new Rect(), new Rect(), new Rect(), new Rect(), new Rect()};
    private boolean mEnableDrawRect = false;
    private int mLargestFaceIndex = 0;
    private int mPreviewHeight = 0;
    private int mPreviewWidth = 0;
    private int mPreviousFaceCount = 0;
    private Rect mPreviousFaceRect = new Rect(-1, -1, 0, 0);
    private Rect mPreviousFaceRectByFocusing = new Rect(-1, -1, 0, 0);
    private int mPreviousLargestFaceIndex = 0;
    private boolean mQflOpen = true;
    private Runnable mResetFaceRunnable = new C09102();
    private Point mSumOfFacePoint = new Point(0, 0);

    /* renamed from: com.lge.camera.managers.FocusFace$1 */
    class C09091 implements CameraAFCallback {
        C09091() {
        }

        public void onAutoFocus(boolean focused, CameraProxy camera) {
            CamLog.m3d(CameraConstants.TAG, "onAutoFocus success = " + focused);
            FocusFace.this.setFocusState(focused ? 3 : 4);
            FocusFace.this.mGet.postOnUiThread(new HandlerRunnable(FocusFace.this) {
                public void handleRun() {
                    CamLog.m3d(CameraConstants.TAG, "unregister handler called focus state = " + FocusFace.this.mGet.getFocusState());
                    if (FocusFace.this.mGet.getCameraDevice() != null && FocusFace.this.mGet.getFocusState() != 0) {
                        FocusFace.this.setFocusState(0);
                    }
                }
            }, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
        }
    }

    /* renamed from: com.lge.camera.managers.FocusFace$2 */
    class C09102 implements Runnable {
        C09102() {
        }

        public void run() {
            if (FocusFace.this.mPreviousFaceRectByFocusing != null) {
                FocusFace.this.mPreviousFaceRectByFocusing.set(-1, -1, 0, 0);
            }
            if (FocusFace.this.mPreviousFaceRect != null) {
                FocusFace.this.mPreviousFaceRect.set(-1, -1, 0, 0);
            }
            if (FocusFace.this.mSumOfFacePoint != null) {
                FocusFace.this.mSumOfFacePoint.set(0, 0);
            }
            if (FocusFace.this.mCameraFocusView != null) {
                FocusFace.this.mCameraFocusView.setRectangles(FocusFace.this.mDetectedFaces, 0);
                FocusFace.this.mCameraFocusView.setVisibility(8);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.FocusFace$3 */
    class C09113 implements CameraFaceDetectionCallback {
        C09113() {
        }

        public void onFaceDetection(FaceCommon[] faces, CameraProxy camera) {
            FocusFace.this.onFaceDetectionFromHal(faces);
        }
    }

    /* renamed from: com.lge.camera.managers.FocusFace$4 */
    class C09124 implements CameraFaceDetectionCallback {
        C09124() {
        }

        public void onFaceDetection(FaceCommon[] faces, CameraProxy camera) {
            FocusFace.this.mGet.onFaceDetection(faces);
        }
    }

    /* renamed from: com.lge.camera.managers.FocusFace$5 */
    class C09135 implements AnimationListener {
        C09135() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (FocusFace.this.mCameraFocusView != null) {
                FocusFace.this.mCameraFocusView.clearAnimation();
                FocusFace.this.mCameraFocusView.setVisibility(4);
                FocusFace.this.setEnableDrawRect(false);
            }
        }
    }

    public void setEnableDrawRect(boolean enabled) {
        this.mEnableDrawRect = enabled;
    }

    public FocusFace(ModuleInterface moduleInterface) {
        super(moduleInterface);
        initFaceFocus();
    }

    private void initFaceFocus() {
        if (checkSupportFaceDetection(true)) {
            this.mDetectedFaces = new Rect[5];
            for (int i = 0; i < 5; i++) {
                this.mDetectedFaces[i] = new Rect();
                this.mDetectedFaces[i].setEmpty();
            }
        }
    }

    public void setCameraFocusView(View view) {
        super.setCameraFocusView(view);
        int[] previewSizeOnScreen = getPreviewSizeOnScreen();
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setSrcImageSize(previewSizeOnScreen[0], previewSizeOnScreen[1]);
        }
    }

    public void setFocusViewLayoutParam(int left, int top, int right, int bottom) {
    }

    public void registerCallback() {
        setFocusState(1);
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            cameraDevice.autoFocus(this.mGet.getHandler(), new C09091());
        }
    }

    public void unregisterCallback() {
        if (this.mGet.getCameraDevice() != null || this.mGet.getFocusState() != 0) {
            cancelAutoFocus();
        }
    }

    public boolean cancelAutoFocus() {
        this.mPreviousFaceCount = 0;
        this.mCurrentFaceCount = 0;
        return true;
    }

    public void release() {
        stopFaceDetection();
        super.release();
    }

    public void startFaceDetection() {
        if (checkSupportFaceDetection(true)) {
            if (!sFaceDetectionStarted || FunctionProperties.getSupportedHal() == 1) {
                initFaceDetectInfo();
            }
            startFaceDetectionFromHal();
            this.mGet.resumeShutterless();
        }
    }

    public void stopFaceDetection() {
        if (checkSupportFaceDetection(false)) {
            CamLog.m3d(CameraConstants.TAG, "Face detection stop!");
            stopFaceDetectionFromHal();
            initFaceDetectInfo();
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessage(3);
            }
            this.mGet.pauseShutterless();
        }
    }

    private synchronized void startFaceDetectionFromHal() {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (!(sFaceDetectionStarted || cameraDevice == null)) {
            if (checkSupportFaceDetection(true)) {
                CamLog.m3d(CameraConstants.TAG, "Face detection Start!");
                if (this.mGet.isRearCamera()) {
                    cameraDevice.setFaceDetectionCallback(this.mGet.getHandler(), new C09113());
                    cameraDevice.startFaceDetection();
                    sFaceDetectionStarted = true;
                } else {
                    cameraDevice.setFaceDetectionCallback(this.mGet.getHandler(), new C09124());
                    cameraDevice.startFaceDetection();
                    sFaceDetectionStarted = true;
                }
            }
        }
    }

    private int getIndexLargestFace(Rect[] detectedFaces, int length) {
        int largestFaceIndex = length == 0 ? 0 : this.mPreviousLargestFaceIndex;
        if (this.mGet.getFocusState() == 0) {
            for (int i = 0; i < length; i++) {
                int newWidth = detectedFaces[i].right - detectedFaces[i].left;
                if (detectedFaces[largestFaceIndex].right - detectedFaces[largestFaceIndex].left < newWidth) {
                    largestFaceIndex = i;
                    int largestFaceWidth = newWidth;
                }
            }
            this.mPreviousLargestFaceIndex = largestFaceIndex;
        }
        return largestFaceIndex;
    }

    private void processFrontFaceLcoationFront(Rect[] rects, int count) {
        if (this.mCameraFocusView != null) {
            int previewWidth = this.mCameraFocusView.getWidth();
            int degree = this.mGet.getOrientationDegree();
            if (degree == 180 || degree == 0) {
                previewWidth = this.mCameraFocusView.getHeight();
            }
            for (int i = 0; i < count; i++) {
                int width = rects[i].width();
                rects[i].left = previewWidth - rects[i].right;
                rects[i].right = rects[i].left + width;
            }
        }
    }

    public void onFaceDetectionFromHal(FaceCommon[] faces) {
        if (faces != null) {
            if (this.mCameraFocusView == null) {
                CamLog.m11w(CameraConstants.TAG, "mFaceDetectView is null.");
                return;
            }
            if (!(this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || this.mGet.isTimerShotCountdown() || this.mGet.isAnimationShowing())) {
                this.mQflOpen = false;
            }
            if (!this.mQflOpen) {
                checkFaceDetection(faces);
                if (!this.mGet.checkModuleValidate(95)) {
                    return;
                }
                if (this.mQflOpen || !(this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || this.mGet.isTimerShotCountdown() || this.mGet.isAnimationShowing())) {
                    Handler mainHandler = this.mGet.getHandler();
                    if (mainHandler == null) {
                        return;
                    }
                    if (this.mCurrentFaceCount > 0 && sFaceDetectionStarted && this.mEnableDrawRect && !this.mQflOpen) {
                        mainHandler.removeCallbacks(this.mResetFaceRunnable);
                        this.mCheckNoneFaceCount = 0;
                        drawFaceDetectedRect(faces);
                        return;
                    } else if (this.mCheckNoneFaceCount >= 1) {
                        mainHandler.removeCallbacks(this.mResetFaceRunnable);
                        this.mResetFaceRunnable.run();
                        return;
                    } else {
                        mainHandler.removeCallbacks(this.mResetFaceRunnable);
                        mainHandler.postDelayed(this.mResetFaceRunnable, 500);
                        this.mCheckNoneFaceCount++;
                        return;
                    }
                }
                this.mQflOpen = true;
                this.mCameraFocusView.clearAnimation();
                this.mCameraFocusView.setVisibility(4);
                this.mHandler.removeMessages(9);
                this.mHandler.removeMessages(8);
                Message msg = new Message();
                msg.what = 9;
                msg.arg1 = 2;
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private void checkFaceDetection(FaceCommon[] faces) {
        this.mCurrentFaceCount = faces.length;
        if (!(this.mHandler == null || this.mPreviousFaceCount == this.mCurrentFaceCount)) {
            if (this.mPreviousFaceCount > 0 && this.mCurrentFaceCount == 0) {
                this.mHandler.sendEmptyMessage(3);
            } else if (this.mPreviousFaceCount == 0 && this.mCurrentFaceCount > 0) {
                this.mHandler.sendEmptyMessage(2);
            }
            if (this.mPreviousFaceCount > 0 && this.mCurrentFaceCount == 0) {
                this.mCameraFocusView.clearAnimation();
                this.mCameraFocusView.setVisibility(4);
                this.mHandler.removeMessages(9);
                this.mHandler.removeMessages(8);
                Message msg = new Message();
                msg.what = 9;
                msg.arg1 = 2;
                this.mHandler.sendMessage(msg);
            } else if (this.mCurrentFaceCount > 0) {
                this.mCameraFocusView.clearAnimation();
                this.mCameraFocusView.setVisibility(4);
                this.mHandler.removeMessages(9);
                this.mHandler.removeMessages(8);
                this.mHandler.sendEmptyMessage(8);
            }
        }
        this.mPreviousFaceCount = this.mCurrentFaceCount;
    }

    private void drawFaceDetectedRect(FaceCommon[] faces) {
        if (faces != null) {
            int faceDetectedCount = faces.length > this.mDetectedFaces.length ? this.mDetectedFaces.length : faces.length;
            this.mGet.getHandler().removeCallbacks(this.mResetFaceRunnable);
            for (int i = 0; i < faceDetectedCount; i++) {
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
                this.mDetectedFaces[i].left = Math.round(((float) ((faces[i].getRect().left * this.mPreviewWidth) / sensorPreviewWidth)) + this.mCenter_x);
                this.mDetectedFaces[i].right = Math.round(((float) ((faces[i].getRect().right * this.mPreviewWidth) / sensorPreviewWidth)) + this.mCenter_x);
                this.mDetectedFaces[i].top = Math.round(((float) ((faces[i].getRect().top * this.mPreviewHeight) / sensorPreviewHeight)) + this.mCenter_y);
                this.mDetectedFaces[i].bottom = Math.round(((float) ((faces[i].getRect().bottom * this.mPreviewHeight) / sensorPreviewHeight)) + this.mCenter_y);
            }
            if (!this.mGet.isRearCamera()) {
                processFrontFaceLcoationFront(this.mDetectedFaces, faceDetectedCount);
            }
            this.mCameraFocusView.setRectangles(this.mDetectedFaces, faceDetectedCount);
            this.mLargestFaceIndex = getIndexLargestFace(this.mDetectedFaces, faceDetectedCount);
            if (this.mGet.getFocusState() == 0) {
                this.mPreviousFaceRectByFocusing.set(-1, -1, 0, 0);
            }
            this.mPreviousFaceRect = this.mDetectedFaces[this.mLargestFaceIndex];
            if (this.mCameraFocusView.getVisibility() != 0) {
                this.mCameraFocusView.setVisibility(0);
            }
            this.mCameraFocusView.postInvalidate();
        }
    }

    public void hideFaceDetectedRectAnimation() {
        Animation animFadeout = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fd_focus_fade_out);
        animFadeout.setAnimationListener(new C09135());
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.startAnimation(animFadeout);
        }
    }

    private synchronized void stopFaceDetectionFromHal() {
        if (sFaceDetectionStarted) {
            if (checkSupportFaceDetection(false)) {
                CameraProxy cameraDevice = this.mGet.getCameraDevice();
                if (cameraDevice != null) {
                    sFaceDetectionStarted = false;
                    cameraDevice.setFaceDetectionCallback(this.mGet.getHandler(), null);
                    cameraDevice.stopFaceDetection();
                }
            }
        }
    }

    private boolean checkSupportFaceDetection(boolean isStart) {
        if ((isStart && this.mGet.isPostviewShowing()) || this.mGet.isManualFocusModeEx()) {
            return false;
        }
        if (!isStart || this.mGet.checkModuleValidate(192)) {
            return this.mGet.isFaceDetectionSupported();
        }
        CamLog.m3d(CameraConstants.TAG, "prevent startFaceDetection in recording state");
        return false;
    }

    private void initFaceDetectInfo() {
        int i;
        if (!checkSupportFaceDetection(true)) {
            this.mDetectedFaces = null;
        } else if (this.mDetectedFaces == null) {
            this.mDetectedFaces = new Rect[5];
            for (i = 0; i < 5; i++) {
                this.mDetectedFaces[i] = new Rect();
                this.mDetectedFaces[i].setEmpty();
            }
        } else {
            for (i = 0; i < 5; i++) {
                this.mDetectedFaces[i].setEmpty();
            }
        }
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.resetRectangles();
        }
        int[] previewSizeOnScreen = getPreviewSizeOnScreen();
        this.mPreviewWidth = previewSizeOnScreen[0];
        this.mPreviewHeight = previewSizeOnScreen[1];
        this.mCenter_x = ((float) previewSizeOnScreen[0]) / 2.0f;
        this.mCenter_y = ((float) previewSizeOnScreen[1]) / 2.0f;
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setSrcImageSize(previewSizeOnScreen[0], previewSizeOnScreen[1]);
        }
    }

    public boolean isFaceDetectionStarted() {
        return sFaceDetectionStarted;
    }
}
