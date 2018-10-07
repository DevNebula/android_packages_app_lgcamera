package com.lge.camera.managers;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.HashMap;

public class GestureManager extends ManagerInterfaceImpl {
    private static final int MIN_HOLD_TIME_TO_CANCEL_SWIPE_GESTURE = 400;
    private static final int MIN_MOVEMENT_THRESHOLD = 30;
    private static final int MOTION_GESTURE = 2;
    private static final int MOTION_NONE = 0;
    private static final int MOTION_SCALE = 1;
    private static final int MOTION_SCALE_OUT_OF_RANGE = 3;
    private double mDistance;
    private MotionEvent mEndMotionEvent;
    private Rect mGestureArea = new Rect();
    private int mGestureFlickingType = 0;
    private GestureInterface mGestureInterface = null;
    private HashMap<Integer, Integer[]> mGestureTypeMap = new HashMap();
    private boolean mIsLongPressed = false;
    private boolean mIsTouchDown = false;
    private int mMotionType = 0;
    private MotionEvent mPrevMotionEvent;
    private ScaleGestureDetector mScaleDetector = null;
    private MotionEvent mStartMotionEvent;
    private int mSwipeMinDistance = 0;
    private long mTouchDownTime;

    public interface GestureInterface {
        void onGestureCleanViewDetected();

        void onGestureFlicking(MotionEvent motionEvent, MotionEvent motionEvent2, int i);

        void onMultiTouchDetected();

        void onScaleGesture(int i, int i2);

        void onScaleGestureBegin();

        void onScaleGestureEnd();
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        private static final float BLOCK_JITTER = 4.0f;
        private float mOneStepBeforeSpan;
        private float mSpanBegin;

        private ScaleListener() {
            this.mOneStepBeforeSpan = 0.0f;
            this.mSpanBegin = 0.0f;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float currentSpan = detector.getCurrentSpan();
            int gapSpan = (int) (currentSpan - this.mOneStepBeforeSpan);
            if (((float) Math.abs(gapSpan)) <= BLOCK_JITTER) {
                return false;
            }
            if (GestureManager.this.mGestureInterface != null) {
                GestureManager.this.mGestureInterface.onScaleGesture(gapSpan, (int) (currentSpan - this.mSpanBegin));
            }
            this.mOneStepBeforeSpan = currentSpan;
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            this.mOneStepBeforeSpan = detector.getCurrentSpan();
            this.mSpanBegin = this.mOneStepBeforeSpan;
            if (GestureManager.this.mGestureInterface != null) {
                GestureManager.this.mGestureInterface.onScaleGestureBegin();
            }
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            this.mOneStepBeforeSpan = 0.0f;
            this.mSpanBegin = 0.0f;
            if (GestureManager.this.mGestureInterface != null) {
                GestureManager.this.mGestureInterface.onScaleGestureEnd();
            }
        }
    }

    public GestureManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setGestureInterface(GestureInterface gestureInterface) {
        this.mGestureInterface = gestureInterface;
    }

    public void onResumeBefore() {
        this.mSwipeMinDistance = Utils.getPx(getAppContext(), C0088R.dimen.gesture_min_distance);
        super.onResumeBefore();
        this.mGestureTypeMap.put(Integer.valueOf(0), new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)});
        this.mGestureTypeMap.put(Integer.valueOf(180), new Integer[]{Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(3), Integer.valueOf(2)});
        this.mGestureTypeMap.put(Integer.valueOf(90), new Integer[]{Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(0)});
        this.mGestureTypeMap.put(Integer.valueOf(270), new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1)});
    }

    public int getConvertedGestureType(int degree, int originalGestyreType) {
        if (this.mGestureTypeMap == null || this.mGestureTypeMap.isEmpty()) {
            return originalGestyreType;
        }
        return ((Integer[]) this.mGestureTypeMap.get(Integer.valueOf(degree)))[originalGestyreType].intValue();
    }

    public void setListenerAfterOneShotCallback() {
        setScaleDetectorListener();
    }

    public void onPauseBefore() {
        releaseScaleDetectorListener();
        this.mGestureTypeMap.clear();
    }

    public void setLongPressed(boolean isLongPress) {
        this.mIsLongPressed = isLongPress;
    }

    public boolean isLongPressed() {
        return this.mIsLongPressed;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mGestureInterface = null;
    }

    public boolean gestureDetected(MotionEvent event) {
        boolean isGestureDetected = false;
        if (this.mGet.isCameraChanging() || this.mGet.isAnimationShowing() || this.mGet.isAEAFJustLocked()) {
            return false;
        }
        try {
            if (event.getPointerCount() > 1) {
                if (this.mScaleDetector != null) {
                    if (!this.mGet.isZoomBarVisible() && (!isGestureAvailableArea(event.getX(0), event.getY(0)) || !isGestureAvailableArea(event.getX(1), event.getY(1)))) {
                        this.mMotionType = 3;
                    } else if (this.mGet.isZoomAvailable()) {
                        if (this.mScaleDetector.onTouchEvent(event)) {
                            this.mMotionType = 1;
                        }
                        this.mGestureInterface.onMultiTouchDetected();
                    } else {
                        CamLog.m3d(CameraConstants.TAG, "- gesture - getCameraState() : " + this.mGet.getCameraState());
                        showZoomIsNotSupportedToast();
                        this.mMotionType = 1;
                    }
                }
            } else if (!(this.mMotionType == 1 || this.mMotionType == 3 || !detectSwipeGesture(event))) {
                this.mMotionType = 2;
            }
            if (this.mMotionType == 1 || this.mMotionType == 2) {
                isGestureDetected = true;
            } else {
                isGestureDetected = false;
            }
            if (event.getActionMasked() == 1) {
                if (((this.mMotionType == 1 || this.mMotionType == 3) && this.mGet.isFocusEnableCondition()) || (this.mMotionType == 2 && !this.mGet.getHandler().hasMessages(6))) {
                    this.mGet.getHandler().sendEmptyMessage(17);
                }
                this.mMotionType = 0;
            }
        } catch (IllegalArgumentException e) {
            CamLog.m6e(CameraConstants.TAG, "GestureManager IllegalArgumentException = ", e);
        }
        return isGestureDetected;
    }

    public void setTouchableArea(int width, int height, int startMargin, int topMargin) {
        boolean isFullScreen;
        int qflWidth;
        int commnadWidth;
        int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
        if (lcdSize == null) {
            lcdSize = new int[]{1920, CameraConstantsEx.FHD_SCREEN_RESOLUTION};
        }
        if (lcdSize[0] == height) {
            isFullScreen = true;
        } else {
            isFullScreen = false;
        }
        if (isFullScreen) {
            qflWidth = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
            commnadWidth = getShutterAreaWidth();
        } else {
            qflWidth = startMargin;
            commnadWidth = startMargin;
        }
        if (this.mGestureArea == null) {
            this.mGestureArea = new Rect();
        }
        this.mGestureArea.set(topMargin, qflWidth, lcdSize[1] - topMargin, lcdSize[0] - commnadWidth);
    }

    protected int getShutterAreaWidth() {
        View view = this.mGet.findViewById(C0088R.id.back_button);
        if (view == null) {
            return 0;
        }
        return view.getWidth() + Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.extra_button_marginTop);
    }

    private boolean isGestureAvailableArea(float x, float y) {
        if (this.mGestureArea == null || !this.mGestureArea.contains((int) x, (int) y)) {
            return false;
        }
        return true;
    }

    private boolean detectSwipeGesture(MotionEvent event) {
        switch (event.getActionMasked() & 255) {
            case 0:
                this.mDistance = 0.0d;
                this.mStartMotionEvent = event.copy();
                this.mPrevMotionEvent = this.mStartMotionEvent;
                this.mTouchDownTime = System.currentTimeMillis();
                this.mIsTouchDown = true;
                return false;
            case 1:
                this.mIsTouchDown = false;
                return false;
            case 2:
                if (this.mPrevMotionEvent == null) {
                    return true;
                }
                if (!this.mIsTouchDown) {
                    return false;
                }
                float diffX = Math.abs(event.getX() - this.mPrevMotionEvent.getX());
                float diffY = Math.abs(event.getY() - this.mPrevMotionEvent.getY());
                this.mDistance += Math.sqrt(Math.pow((double) diffX, 2.0d) + Math.pow((double) diffY, 2.0d));
                if (diffX <= 30.0f && diffY <= 30.0f) {
                    return false;
                }
                this.mPrevMotionEvent = event.copy();
                this.mEndMotionEvent = event;
                if (isSwipeGestureUp()) {
                    return false;
                }
                this.mIsTouchDown = false;
                this.mEndMotionEvent = null;
                return true;
            default:
                return false;
        }
    }

    private boolean isSwipeGestureUp() {
        if (this.mStartMotionEvent == null || this.mEndMotionEvent == null) {
            return false;
        }
        long duration = System.currentTimeMillis() - this.mTouchDownTime;
        int result = evaluateFlickingType(this.mStartMotionEvent, this.mEndMotionEvent);
        if (result == -1) {
            return true;
        }
        int convertedResult = getConvertedGestureType(this.mGet.getOrientationDegree(), result);
        if (convertedResult > 1 || duration <= 400) {
            CamLog.m3d(CameraConstants.TAG, "-swap- gesture result = " + result + ", converted result : " + convertedResult);
            if (result > -1) {
                this.mGestureFlickingType = result;
                if (this.mGestureInterface != null) {
                    this.mGestureInterface.onGestureFlicking(this.mStartMotionEvent, this.mEndMotionEvent, result);
                }
                return false;
            } else if (this.mDistance > ((double) this.mSwipeMinDistance)) {
                return false;
            } else {
                return true;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "-swap- duration is over 400msec.");
        return false;
    }

    private void setScaleDetectorListener() {
        this.mScaleDetector = new ScaleGestureDetector(this.mGet.getAppContext(), new ScaleListener());
        this.mScaleDetector.setQuickScaleEnabled(false);
    }

    private void releaseScaleDetectorListener() {
        this.mScaleDetector = null;
    }

    private boolean checkGestureDisableArea(int startX, int startY) {
        if (this.mGet.isSettingMenuVisible()) {
            return false;
        }
        boolean isLand = Utils.isConfigureLandscape(getActivity().getResources());
        int[] lcdSize = Utils.getLCDsize(getAppContext(), false);
        int gestureDisableArea = Utils.getPx(getAppContext(), C0088R.dimen.indicators.height);
        if (isLand) {
            if (startX < gestureDisableArea) {
                return true;
            }
            return false;
        } else if (startY < gestureDisableArea) {
            return true;
        } else {
            return false;
        }
    }

    private int evaluateFlickingType(MotionEvent e1, MotionEvent e2) {
        float startX = e1.getX();
        float endX = e2.getX();
        float startY = e1.getY();
        float endY = e2.getY();
        if (Utils.isConfigureLandscape(getAppContext().getResources())) {
            float tmpStartX = startX;
            startX = startY;
            startY = tmpStartX;
            float tmpEndX = endX;
            endX = endY;
            endY = tmpEndX;
        }
        int distance = (int) Math.sqrt(Math.pow((double) Math.abs(endX - startX), 2.0d) + Math.pow((double) Math.abs(endY - startY), 2.0d));
        if (checkGestureDisableArea((int) startX, (int) startY)) {
            return -1;
        }
        if (distance <= this.mSwipeMinDistance) {
            return -1;
        }
        int flickingType = Math.abs(startX - endX) > Math.abs(startY - endY) ? startX - endX > ((float) this.mSwipeMinDistance) ? 3 : 2 : startY - endY > ((float) this.mSwipeMinDistance) ? 1 : 0;
        return flickingType;
    }

    public int getGestureFlickingType() {
        return this.mGestureFlickingType;
    }

    public void setDegree(int degree, boolean animation) {
    }

    private void showZoomIsNotSupportedToast() {
        if (this.mGet.getCameraState() == 5 || this.mGet.getCameraState() == 8 || this.mGet.getCameraState() == 9 || this.mGet.getCameraState() == 0) {
            CamLog.m3d(CameraConstants.TAG, "- gesture - getCameraState() in condition : " + this.mGet.getCameraState());
            return;
        }
        int toastId = C0088R.string.volume_key_zoom_disable;
        if (this.mGet.isSlowMotionMode()) {
            if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode())) {
                toastId = C0088R.string.volume_key_zoom_disable_resolution;
            } else if (this.mGet.getCameraState() == 6 && !CameraConstants.MODE_SLOW_MOTION.equals(this.mGet.getShotMode())) {
                toastId = C0088R.string.volume_key_zoom_disable_resolution;
            }
        }
        if (CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode())) {
            toastId = C0088R.string.camera_cz_pinch_zoom_not_support;
        }
        String toastMsg = this.mGet.getAppContext().getString(toastId);
        CamLog.m3d(CameraConstants.TAG, "- gesture - getCameraState() before show toast : " + this.mGet.getCameraState());
        if (toastMsg != null) {
            this.mGet.showToastConstant(toastMsg);
        }
    }
}
