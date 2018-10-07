package com.lge.camera.managers;

import android.hardware.Camera.Area;
import com.lge.camera.components.CameraFocusView;
import com.lge.camera.components.CameraManualModeFocusMultiWindowView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.util.CamLog;
import java.util.List;

public abstract class FocusBaseMulti extends FocusBase {
    private boolean mSetInVisibleFocus = false;

    public FocusBaseMulti(ModuleInterface iModule) {
        super(iModule);
    }

    public void setFocusInVisible(boolean isInVisible) {
        this.mSetInVisibleFocus = isInVisible;
    }

    public boolean initAFView() {
        if (this.mCameraFocusView == null || this.mCameraFocusView.isInitialized()) {
            return true;
        }
        if (!(this.mCameraFocusView instanceof CameraManualModeFocusMultiWindowView)) {
            return super.initAFView();
        }
        int unUsedAreaCount = FunctionProperties.getSupportedHal() == 2 ? 0 : 1;
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "Device is null, return.");
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "initAFView GET PARAM AFTER");
        List<Area> area = cameraDevice.getMultiWindowFocusArea();
        if (area == null) {
            return true;
        }
        this.mCameraMultiFocusView.init(area, getPreviewSizeOnScreen(), unUsedAreaCount);
        this.mCameraManualMultiFocusView.init(area, getPreviewSizeOnScreen(), unUsedAreaCount);
        return true;
    }

    public void updateFocusStateIndicator() {
        if (this.mGet.checkModuleValidate(193) && !this.mGet.isRecordingAnimShowing()) {
            if (this.mGet.getRecordingPreviewState(1)) {
                CamLog.m7i(CameraConstants.TAG, "skip drawing focus UI when recording preview is started");
                return;
            }
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                updateFocusStateIndicator(this.mGet.getFocusState(), this.mGet.isMWAFSupported() ? cameraDevice.getMultiWindowFocusArea() : null);
            }
        }
    }

    protected void updateFocusStateIndicator(int focusState, List<Area> areaList) {
        boolean disalbeMultiWindowFocus;
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "Multiwindow - updateFocusStateIndicator : " + focusState);
        if (!this.mGet.checkModuleValidate(193)) {
            CamLog.m3d(CameraConstants.TAG, "in recording state");
        }
        if (CameraConstants.MODE_POPOUT_CAMERA.equals(this.mGet.getShotMode()) || CameraConstants.MODE_SMART_CAM.equals(this.mGet.getShotMode()) || CameraConstants.MODE_REAR_OUTFOCUS.equals(this.mGet.getShotMode())) {
            disalbeMultiWindowFocus = true;
        } else {
            disalbeMultiWindowFocus = false;
        }
        if (this.mCameraFocusView == null) {
            CamLog.m3d(CameraConstants.TAG, "camera focus view is null");
        } else if (this.mSetInVisibleFocus) {
            this.mCameraFocusView.setVisibility(4);
            CamLog.m3d(CameraConstants.TAG, "camera return mSetInVisibleFocus = " + this.mSetInVisibleFocus);
        } else {
            switch (focusState) {
                case 5:
                    setFocusLength();
                    setMoveNormalFocusRectCenter();
                    setFocusViewState(3);
                    return;
                case 6:
                    if (getFocusViewState() == 3) {
                        if (!disalbeMultiWindowFocus) {
                            CamLog.m3d(CameraConstants.TAG, "show MW focus - FOCUS_CONTINUOUS_SUCCESS");
                            this.mCameraFocusView.setVisibility(0);
                        }
                        this.mCameraFocusView.setState(4);
                        return;
                    }
                    return;
                case 8:
                    if (this.mGet.isMWAFSupported()) {
                        CameraProxy cameraDevice = this.mGet.getCameraDevice();
                        if (cameraDevice != null) {
                            CameraParameters parameters = cameraDevice.getParameters();
                            if (parameters != null) {
                                List<Integer> zoomRatioList = parameters.getZoomRatios();
                                if (zoomRatioList != null) {
                                    int zoomRatio = ((Integer) zoomRatioList.get(parameters.getZoom())).intValue();
                                    setMoveNormalFocusRectCenter();
                                    CamLog.m3d(CameraConstants.TAG, "FOCUS_MULTIWINDOWAF_SEARCHING : " + zoomRatio);
                                    CameraFocusView cameraFocusView = this.mCameraFocusView;
                                    if (zoomRatio < 200) {
                                        z = false;
                                    }
                                    cameraFocusView.setCenterWindowVisibility(z);
                                    this.mCameraFocusView.setState(focusState);
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                case 9:
                    if (this.mGet.isMWAFSupported() && areaList != null) {
                        if (!disalbeMultiWindowFocus) {
                            CamLog.m3d(CameraConstants.TAG, "show MW focus - FOCUS_MULTIWINDOWAF_SUCCESS");
                            this.mCameraFocusView.setVisibility(0);
                        }
                        this.mCameraFocusView.setList(areaList);
                        this.mCameraFocusView.setState(focusState);
                        if (((Area) areaList.get(areaList.size() - 1)).weight == 2) {
                            this.mGet.removePostRunnable(this.mReleaseTouchFocus);
                            this.mGet.postOnUiThread(this.mReleaseTouchFocus, 500);
                            this.mCameraFocusView.setCenterWindowVisibility(true);
                            if (!disalbeMultiWindowFocus) {
                                this.mCameraFocusView.setVisibility(0);
                            }
                            this.mCameraFocusView.setState(focusState);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void setFocusLength() {
        if (!this.mGet.isMWAFSupported()) {
            int[] previewSize = getPreviewSizeOnScreen();
            int width = (int) (((double) previewSize[1]) * 0.18d);
            int height = (int) (((double) previewSize[0]) * 0.18d);
            if (width <= height) {
                height = width;
            }
            this.mRectHeight = height;
            this.mRectWidth = height;
        }
    }

    public void setFocusAreaWindow(int width, int height, int startMargin, int topMargin) {
        super.setFocusAreaWindow(width, height, startMargin, topMargin);
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.refresh(getPreviewSizeOnScreen());
        }
    }

    public void setFocusViewLayoutParam(int left, int top, int right, int bottom) {
        if (!this.mGet.isMWAFSupported()) {
            super.setFocusViewLayoutParam(left, top, right, bottom);
        }
    }

    public void startGuideViewAnimation(float pivotX, float pivotY) {
        CamLog.m3d(CameraConstants.TAG, "startGuideViewAnimation - multiFocus visible");
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setVisibility(0);
        }
    }
}
