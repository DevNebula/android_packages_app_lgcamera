package com.lge.camera.managers;

import com.lge.camera.components.ZoomBar;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtil;
import java.util.List;

public class ZoomManagerBase extends ManagerInterfaceImpl {
    protected static final int BASE_SPAN = 900;
    protected static final int FAKE_MAX_ZOOM_SCALE_RATIO = 4;
    protected static final int SCALE_FACTOR = 1;
    protected static final int STEP_LIMIT = 8;
    protected int mBaseSpan = (this.mMaxZoomScaleRatio * BASE_SPAN);
    protected boolean mIsGestureZooming = false;
    protected int mMaxZoomScaleRatio = 1;
    protected float mPinchZoomFactor = 0.5f;
    protected int mPreZoomValue = -1;
    protected List<Integer> mRatioList = null;
    protected int mScaleFactor = (this.mMaxZoomScaleRatio * 1);
    protected int mStepLimit = (this.mMaxZoomScaleRatio * 8);
    protected ZoomBar mZoomBar = null;
    protected ZoomInterface mZoomInterface = null;
    protected int mZoomMaxValue = 90;
    protected int mZoomValue = -1;

    public interface ZoomInterface {
        void doInAndOutZoom(boolean z, boolean z2);

        boolean isZoomAvailable();

        void onZoomHide();

        void onZoomShow();

        void setZoomStep(int i, boolean z, boolean z2, boolean z3);

        void stopShutterZoom();
    }

    public ZoomManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setZoomInterface(ZoomInterface zoomInterface) {
        this.mZoomInterface = zoomInterface;
    }

    public int getZoomMaxValue() {
        return this.mZoomMaxValue;
    }

    public void setZoomRatioList(List<Integer> ratioList) {
        this.mRatioList = ratioList;
    }

    public List<Integer> getZoomRatioList() {
        return this.mRatioList;
    }

    public int getZoomRatio() {
        if (this.mRatioList == null || this.mRatioList.size() <= 0 || this.mRatioList.size() <= this.mZoomValue) {
            return 0;
        }
        if (this.mZoomValue == -1) {
            return ((Integer) this.mRatioList.get(0)).intValue();
        }
        return ((Integer) this.mRatioList.get(this.mZoomValue)).intValue();
    }

    public void setZoomValue(int zoomValue) {
        this.mZoomValue = zoomValue;
        if (this.mZoomValue < 0) {
            this.mZoomValue = 0;
        }
        if (this.mZoomValue > this.mZoomMaxValue) {
            CamLog.m3d(CameraConstants.TAG, "setZoomValue over!");
            this.mZoomValue = this.mZoomMaxValue;
        }
        CamLog.m3d(CameraConstants.TAG, "setZoomValue : " + this.mZoomValue);
    }

    public void setZoomMaxValue(int value) {
        this.mZoomMaxValue = value;
        if (this.mZoomMaxValue > 300) {
            this.mMaxZoomScaleRatio = 8;
        } else if (this.mZoomMaxValue > 78) {
            this.mMaxZoomScaleRatio = 4;
        } else {
            this.mMaxZoomScaleRatio = 1;
        }
        setZoomScaleFactors();
        CamLog.m3d(CameraConstants.TAG, "Max zoom step : " + this.mZoomMaxValue + ", Zoom scale ratio : " + this.mMaxZoomScaleRatio);
    }

    public void setZoomScaleFactors() {
        this.mScaleFactor = this.mMaxZoomScaleRatio * 1;
        this.mBaseSpan = this.mMaxZoomScaleRatio * BASE_SPAN;
        this.mStepLimit = this.mMaxZoomScaleRatio * 8;
        this.mPinchZoomFactor = 0.001f * ((float) this.mZoomMaxValue);
        CamLog.m3d(CameraConstants.TAG, "[zoom] mPinchZoomFactor = " + this.mPinchZoomFactor);
    }

    public int getZoomValue() {
        if (isReadyZoom()) {
            if (this.mZoomValue < 0) {
                this.mZoomValue = getInitZoomStep(this.mGet.getCameraId());
            }
            CamLog.m3d(CameraConstants.TAG, "getZoomValue : " + this.mZoomValue);
            return this.mZoomValue;
        }
        CamLog.m3d(CameraConstants.TAG, "Not ready zoom");
        return 0;
    }

    public int getCropAngleZoomValue() {
        if (SharedPreferenceUtil.getCropAngleButtonId(this.mGet.getAppContext()) == 0) {
            return FunctionProperties.getCropAngleZoomLevel();
        }
        return 0;
    }

    public void onGestureZoomBegin() {
        this.mPreZoomValue = this.mZoomValue;
        this.mIsGestureZooming = true;
    }

    public boolean isGestureZooming() {
        return this.mIsGestureZooming;
    }

    public void resetZoomLevel() {
        this.mZoomValue = -1;
        CamLog.m3d(CameraConstants.TAG, "mZoomValue set to -1");
    }

    public void onDestroy() {
        super.onDestroy();
        resetZoomLevel();
    }

    public int getInitZoomStep(int cameraId) {
        return 0;
    }

    protected void unbindZoomBarView() {
        if (this.mZoomBar != null) {
            this.mZoomBar.unbind();
            this.mZoomBar = null;
        }
    }

    public boolean isReadyZoom() {
        return true;
    }

    public boolean isInitZoomStep(int zoomStep) {
        return zoomStep == 0;
    }
}
