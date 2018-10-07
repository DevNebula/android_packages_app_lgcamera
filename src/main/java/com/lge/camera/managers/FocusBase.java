package com.lge.camera.managers;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Area;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.CameraFocusView;
import com.lge.camera.components.CameraManualModeFocusMultiWindowView;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamQueue;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.List;

public abstract class FocusBase implements OnRemoveHandler {
    protected CameraFocusView mCameraFocusView = null;
    protected CameraFocusView mCameraManualMultiFocusView = null;
    protected CameraFocusView mCameraMultiFocusView = null;
    protected RotateImageButton mEVShutterButton = null;
    protected int mFOCUS_RECT_HEIGHT = 0;
    protected int mFOCUS_RECT_WIDTH = 0;
    protected ArrayList<Area> mFocusArea = null;
    protected int mFocusAreaHeight = 0;
    protected int mFocusAreaStartMargin = 0;
    protected int mFocusAreaTopMargin = 0;
    protected int mFocusAreaWidth = 0;
    protected Rect mFocusRect = new Rect();
    protected ModuleInterface mGet = null;
    protected Handler mHandler = null;
    protected HandlerRunnable mHideTouchFocus = new HandlerRunnable(this) {
        public void handleRun() {
            FocusBase.this.initFocusRectangle();
            FocusBase.this.hideFocus();
            FocusBase.this.mGet.setManualFocusButtonVisibility(false);
            if (FocusBase.this.mGet.checkModuleValidate(192)) {
                FocusBase.this.mGet.registerEVCallback(true, false);
            }
            FocusBase.this.setFocusState(0);
        }
    };
    protected int mRectHeight = 0;
    protected int mRectWidth = 0;
    protected HandlerRunnable mReleaseTouchFocus = new HandlerRunnable(this) {
        public void handleRun() {
            if (FocusBase.this.mGet.isFocusTrackingSupported() && FocusBase.this.mGet.getFocusState() == 14) {
                CamLog.m3d(CameraConstants.TAG, "-tf- Do not release focus during Tracking AF");
            } else if (!FocusBase.this.mGet.isTimerShotCountdown() && FocusBase.this.mGet.getFocusState() != 14 && !FocusBase.this.mGet.isFocusLock() && !FocusBase.this.mGet.isAELock()) {
                FocusBase.this.releaseTouchFocus();
                if (FocusBase.this.mGet.checkModuleValidate(192)) {
                    FocusBase.this.mGet.registerEVCallback(true, false);
                }
                FocusBase.this.setFocusState(0);
            }
        }
    };
    protected HandlerRunnable mStartLockAE = new HandlerRunnable(this) {
        public void handleRun() {
            if (!FocusBase.this.mGet.isTimerShotCountdown() && FocusBase.this.mGet.isAELock()) {
                CameraProxy cameraDevice = FocusBase.this.mGet.getCameraDevice();
                if (cameraDevice != null) {
                    CameraParameters parameters = cameraDevice.getParameters();
                    if (parameters != null) {
                        CameraDeviceUtils.setEnable3ALocks(cameraDevice, true, false);
                        parameters.setFocusAreas(null);
                        parameters.setMeteringAreas(FocusBase.this.mFocusArea);
                        cameraDevice.setParameters(parameters);
                    }
                }
            }
        }
    };
    protected int mStartMargin = 0;

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    public FocusBase(ModuleInterface iModule) {
        this.mGet = iModule;
        setFocusRectSize(false);
        this.mRectWidth = this.mFOCUS_RECT_WIDTH;
        this.mRectHeight = this.mFOCUS_RECT_HEIGHT;
    }

    public void setFocusRectSize(boolean isTracking) {
        Drawable drawable;
        if (!isTracking || this.mGet.isFocusLock()) {
            drawable = this.mGet.getAppContext().getResources().getDrawable(C0088R.drawable.focus_touch_taf);
        } else {
            drawable = this.mGet.getAppContext().getResources().getDrawable(C0088R.drawable.camera_focus_tracking_af);
        }
        this.mFOCUS_RECT_WIDTH = (int) (((float) drawable.getIntrinsicWidth()) * 1.4f);
        this.mFOCUS_RECT_HEIGHT = (int) (((float) drawable.getIntrinsicHeight()) * 1.4f);
        CamLog.m3d(CameraConstants.TAG, "isTracking = " + isTracking + " mFOCUS_RECT_WIDTH = " + this.mFOCUS_RECT_WIDTH);
    }

    public void setStartMargin(int startMargin) {
        this.mStartMargin = startMargin;
    }

    public void setFocusManagerHandler(Handler handle) {
        this.mHandler = handle;
    }

    public void setCameraFocusView(View view) {
        if (view instanceof CameraManualModeFocusMultiWindowView) {
            this.mCameraFocusView = this.mCameraManualMultiFocusView;
        } else if (view instanceof RotateImageButton) {
            this.mEVShutterButton = (RotateImageButton) view;
        } else {
            this.mCameraFocusView = (CameraFocusView) view;
        }
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setVisibility(4);
        }
        if (this.mEVShutterButton != null) {
            this.mEVShutterButton.setVisibility(4);
        }
        this.mGet.releaseAeControlBar();
    }

    public void initCameraFocusView(View cameraMultiFocus, View cameraManualMultiFocus) {
        this.mCameraMultiFocusView = (CameraFocusView) cameraMultiFocus;
        this.mCameraManualMultiFocusView = (CameraFocusView) cameraManualMultiFocus;
    }

    public boolean initAFView() {
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.init();
        }
        return true;
    }

    public boolean showFocus() {
        CamLog.m3d(CameraConstants.TAG, "showFocus");
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setVisibility(0);
        }
        setEVShutterVisiblity(true);
        return true;
    }

    public boolean hideFocus() {
        if (!this.mGet.isManualFocusModeEx()) {
            if (this.mCameraFocusView != null) {
                this.mCameraFocusView.clearAnimation();
                this.mCameraFocusView.setVisibility(4);
            }
            setEVShutterVisiblity(false);
            this.mGet.releaseAeControlBar();
            this.mGet.setManualFocusButtonVisibility(false);
        }
        return true;
    }

    public boolean hideFocusForce() {
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.clearAnimation();
            this.mCameraFocusView.setVisibility(4);
        }
        setEVShutterVisiblity(false);
        this.mGet.releaseAeControlBar();
        this.mGet.setManualFocusButtonVisibility(false);
        return true;
    }

    public void setFocusViewState(int state) {
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setState(state);
        }
    }

    public int getFocusViewState() {
        if (this.mCameraFocusView != null) {
            return this.mCameraFocusView.getState();
        }
        return 0;
    }

    public void setFocusViewDegree(int degree) {
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setDegree(degree);
        }
        setTAFSize(degree);
    }

    public void updateFocusStateIndicator() {
        if (this.mGet.checkModuleValidate(1)) {
            updateFocusStateIndicator(this.mGet.getFocusState());
        }
    }

    protected void updateFocusStateIndicator(int focusState, List<Area> list) {
        CamLog.m3d(CameraConstants.TAG, "updateFocusStateIndicator : " + focusState);
    }

    protected void updateFocusStateIndicator(int focusState) {
        CamLog.m3d(CameraConstants.TAG, "updateFocusStateIndicator : " + focusState);
        switch (focusState) {
            case 0:
                setFocusViewState(0);
                return;
            case 1:
                setFocusViewState(0);
                return;
            case 2:
            case 3:
                setFocusViewState(1);
                setEVShutterVisiblity(true);
                return;
            case 4:
                setFocusViewState(2);
                setEVShutterVisiblity(true);
                return;
            case 5:
                setMoveNormalFocusRectCenter();
                setFocusViewState(3);
                return;
            case 6:
                setFocusViewState(4);
                return;
            case 7:
                setClearFocusAnimation();
                setFocusViewState(5);
                return;
            default:
                CamLog.m3d(CameraConstants.TAG, "Wrong focus state!: " + this.mGet.getFocusState());
                return;
        }
    }

    protected void setEVShutterVisiblity(boolean visible) {
        if (this.mEVShutterButton != null && this.mGet.isEVShutterSupportedMode()) {
            if (visible && this.mGet.isRecordingState()) {
                CamLog.m3d(CameraConstants.TAG, "Cannot show EV shutter during recording, return");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "setEVShutterVisiblity, visible = " + visible);
            this.mEVShutterButton.setVisibility(visible ? 0 : 4);
        }
    }

    protected void setClearFocusAnimation() {
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.clearAnimation();
        }
    }

    public void initFocusRectangle() {
        CamLog.m3d(CameraConstants.TAG, "initFocusRectangle.");
    }

    public void initFocusAreas() {
        CamLog.m3d(CameraConstants.TAG, "InitFocusAreas");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters parameters = cameraDevice.getParameters();
            if (parameters != null) {
                int meteringAreas;
                ParamQueue paramQueue = new ParamQueue();
                if (this.mGet.isRearCamera()) {
                    int focusAreas;
                    String focusAreasStr = parameters.get(ParamConstants.KEY_MAX_NUM_FOCUS_AREAS);
                    if (CameraConstants.NULL.equals(focusAreasStr) || focusAreasStr == null) {
                        focusAreas = 0;
                    } else {
                        focusAreas = Integer.parseInt(focusAreasStr);
                    }
                    CamLog.m3d(CameraConstants.TAG, "initFocusAreas focusAreas = " + focusAreas);
                    if (focusAreas > 0) {
                        paramQueue.add(ParamConstants.KEY_FOCUS_AREAS, null);
                    }
                }
                String meteringAreasStr = parameters.get(ParamConstants.KEY_MAX_NUM_METERING_AREAS);
                if (CameraConstants.NULL.equals(meteringAreasStr) || meteringAreasStr == null || "".equals(meteringAreasStr)) {
                    meteringAreas = 0;
                } else {
                    meteringAreas = Integer.parseInt(meteringAreasStr);
                }
                CamLog.m3d(CameraConstants.TAG, "initFocusAreas meteringAreas = " + meteringAreas);
                if (meteringAreas > 0) {
                    paramQueue.add(ParamConstants.KEY_METERING_AREAS, null);
                }
                paramQueue.add(ParamConstants.KEY_VIEW_MODE, this.mGet.getCurrentViewModeToString());
                paramQueue.add(ParamConstants.KEY_JOG_ZOOM, this.mGet.getParamValue(ParamConstants.KEY_JOG_ZOOM));
                if ("mode_normal".equals(this.mGet.getShotMode())) {
                    paramQueue.add("picture-size", this.mGet.getBinningEnabledState() ? this.mGet.getBinningPictureSize() : this.mGet.getCurrentSelectedPictureSize());
                    paramQueue.add(ParamConstants.KEY_BINNING_PARAM, this.mGet.getBinningEnabledState() ? ParamConstants.VALUE_BINNING_MODE : "normal");
                }
                CameraDeviceUtils.setEnable3ALocks(cameraDevice, false, false);
                try {
                    CamLog.m3d(CameraConstants.TAG, "initFocusAreas setparam forcely");
                    cameraDevice.setParameters(parameters, paramQueue);
                } catch (RuntimeException e) {
                    CamLog.m8i(CameraConstants.TAG, "RuntimeException:", e);
                }
            }
        }
    }

    public void setMoveNormalFocusRectCenter() {
        int[] previewSizeOnScreen = getPreviewSizeOnScreen();
        int previewWidth = previewSizeOnScreen[0];
        int previewHeight = previewSizeOnScreen[1];
        if (!Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
            previewWidth = previewSizeOnScreen[1];
            previewHeight = previewSizeOnScreen[0];
        }
        setMoveNormalFocusRect(previewWidth / 2, previewHeight / 2, false);
    }

    protected void setTAFSize(int degree) {
        this.mRectWidth = this.mFOCUS_RECT_WIDTH;
        this.mRectHeight = this.mFOCUS_RECT_HEIGHT;
        if (degree == 90 || degree == 270) {
            this.mRectWidth = this.mFOCUS_RECT_HEIGHT;
            this.mRectHeight = this.mFOCUS_RECT_WIDTH;
        }
    }

    public void setMoveNormalFocusRect(int x, int y, boolean bAnimation) {
        int left = x - (this.mRectWidth / 2);
        int top = y - (this.mRectHeight / 2);
        int right = x + (this.mRectWidth / 2);
        int bottom = y + (this.mRectHeight / 2);
        CamLog.m7i(CameraConstants.TAG, "[Focus] setMoveNormalFocusRect x = " + x + ", y = " + y);
        CamLog.m7i(CameraConstants.TAG, "[Focus] mRectWidth = " + this.mRectWidth + ", mRectHeight = " + this.mRectHeight);
        CamLog.m7i(CameraConstants.TAG, "[Focus] move to left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
        if (left < 0) {
            left = 0;
            right = this.mRectWidth;
        }
        if (top < 0) {
            top = 0;
            bottom = this.mRectHeight;
        }
        if (right >= this.mFocusAreaWidth) {
            right = this.mFocusAreaWidth;
            left = this.mFocusAreaWidth - this.mRectWidth;
        }
        if (bottom >= this.mFocusAreaHeight) {
            top = this.mFocusAreaHeight - this.mRectHeight;
            bottom = this.mFocusAreaHeight;
        }
        setFocusRectangle(left, top, right, bottom);
        if (!bAnimation || ("on".equals(this.mGet.getSettingValue("tracking-af")) && this.mGet.isFocusTrackingSupported() && !this.mGet.isFocusLock())) {
            this.mCameraFocusView.setFocusLocation(x - left, y - top);
        } else {
            startGuideViewAnimation((float) (x - left), (float) (y - top));
        }
    }

    public void setFocusAreaWindow(int width, int height, int startMargin, int topMargin) {
        this.mFocusAreaWidth = width;
        this.mFocusAreaHeight = height;
        this.mFocusAreaStartMargin = startMargin;
        this.mFocusAreaTopMargin = topMargin;
    }

    public void setFocusRectangle(int left, int top, int right, int bottom) {
        if (this.mFocusRect != null && this.mCameraFocusView != null) {
            setFocusViewLayoutParam(left, top, right, bottom);
            if (left < 0) {
                left = 0;
                right = this.mRectWidth;
            }
            Rect rect = new Rect();
            rect.left = left;
            rect.top = top;
            rect.right = right;
            rect.bottom = bottom;
            if (!Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                rect.left = top;
                rect.top = this.mFocusAreaWidth - right;
                rect.right = bottom;
                rect.bottom = this.mFocusAreaWidth - left;
            }
            this.mFocusRect.set(rect.left, rect.top, rect.right, rect.bottom);
        }
    }

    public void setFocusViewLayoutParam(int left, int top, int right, int bottom) {
        if (this.mCameraFocusView != null) {
            LayoutParams rl = (LayoutParams) this.mCameraFocusView.getLayoutParams();
            rl.setMarginStart(left);
            rl.topMargin = top;
            this.mCameraFocusView.setLayoutParams(rl);
        }
    }

    public void startGuideViewAnimation(float pivotX, float pivotY) {
        float scaleEnd;
        int focusState = this.mGet.getFocusState();
        float scaleStart = (focusState == 5 || focusState == 8) ? 1.5f : 1.4f;
        if (focusState == 5 || focusState == 8) {
            scaleEnd = 1.0f;
        } else {
            scaleEnd = 1.0f;
        }
        if (this.mCameraFocusView != null) {
            CamLog.m3d(CameraConstants.TAG, "startGuideViewAnimation");
            this.mCameraFocusView.startFocusAnimation(200, scaleStart, scaleEnd, pivotX, pivotY, 0.25f, 1.0f);
        }
    }

    protected int[] getPreviewSizeOnScreen() {
        int[] previewSize = new int[]{this.mFocusAreaWidth, this.mFocusAreaHeight};
        if (this.mFocusAreaWidth == 0 || this.mFocusAreaHeight == 0) {
            ListPreference pref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
            if (pref != null) {
                return Utils.sizeStringToArray(pref.getExtraInfo(2));
            }
            return Utils.getLCDsize(this.mGet.getAppContext(), true);
        } else if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
            return previewSize;
        } else {
            previewSize[0] = this.mFocusAreaHeight;
            previewSize[1] = this.mFocusAreaWidth;
            return previewSize;
        }
    }

    public void setFocusState(int focusState) {
        CamLog.m7i(CameraConstants.TAG, "setFocusState state " + focusState);
        this.mGet.setFocusState(focusState);
    }

    protected void setFocusWindow(Rect rect) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "cameraDevice is null");
            return;
        }
        CameraParameters parameters = cameraDevice.getParameters();
        if (parameters == null) {
            CamLog.m11w(CameraConstants.TAG, "parameter is null");
            return;
        }
        if (this.mFocusArea == null) {
            this.mFocusArea = new ArrayList();
            this.mFocusArea.add(new Area(this.mFocusRect, 1));
        }
        try {
            int[] previewSizeOnScreen = getPreviewSizeOnScreen();
            int widthOnScreen = previewSizeOnScreen[0];
            int heightOnScreen = previewSizeOnScreen[1];
            float center_x = ((float) widthOnScreen) / 2.0f;
            float center_y = ((float) heightOnScreen) / 2.0f;
            rect.left = Math.round(((((float) rect.left) - center_x) * 2000.0f) / ((float) widthOnScreen));
            rect.top = Math.round(((((float) rect.top) - center_y) * 2000.0f) / ((float) heightOnScreen));
            rect.right = Math.round(((((float) rect.right) - center_x) * 2000.0f) / ((float) widthOnScreen));
            rect.bottom = Math.round(((((float) rect.bottom) - center_y) * 2000.0f) / ((float) heightOnScreen));
            if (!this.mGet.isRearCamera()) {
                Rect tmprect = new Rect(rect);
                rect.left = tmprect.right * -1;
                rect.right = tmprect.left * -1;
            }
            ((Area) this.mFocusArea.get(0)).rect.left = rect.left;
            ((Area) this.mFocusArea.get(0)).rect.right = rect.right;
            ((Area) this.mFocusArea.get(0)).rect.top = rect.top;
            ((Area) this.mFocusArea.get(0)).rect.bottom = rect.bottom;
            ((Area) this.mFocusArea.get(0)).weight = 1;
            if (!isTrackingState() || this.mGet.isFocusLock()) {
                setFocusWindowParameters(parameters);
            } else {
                setTrackingsWindowParameters(parameters);
            }
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "setTouchIndexAf setParameter exception : ", e);
        }
    }

    public boolean isTrackingState() {
        boolean isTracking = false;
        if (this.mGet.isFocusTrackingSupported()) {
            isTracking = "on".equals(this.mGet.getSettingValue("tracking-af"));
        }
        if ((this.mGet.isUHDmode() || this.mGet.isFHD60()) && !this.mGet.checkModuleValidate(128)) {
            return false;
        }
        return isTracking;
    }

    protected void setFocusWindowParameters(CameraParameters parameters) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null && parameters != null) {
            CameraDeviceUtils.setEnable3ALocks(cameraDevice, false, false);
            parameters.setFocusAreas(this.mFocusArea);
            parameters.setMeteringAreas(this.mFocusArea);
            parameters.setFocusMode("auto");
            cameraDevice.setParameters(parameters);
            CamLog.m3d(CameraConstants.TAG, "## Touch focus setParameter :  left " + ((Area) this.mFocusArea.get(0)).rect.left + " right " + ((Area) this.mFocusArea.get(0)).rect.right + " top " + ((Area) this.mFocusArea.get(0)).rect.top + " bottom " + ((Area) this.mFocusArea.get(0)).rect.bottom);
        }
    }

    protected void setTrackingsWindowParameters(CameraParameters parameters) {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null && parameters != null) {
            String value = Integer.toString(((Area) this.mFocusArea.get(0)).rect.left).concat(",").concat(Integer.toString(((Area) this.mFocusArea.get(0)).rect.top)).concat(",").concat(Integer.toString(((Area) this.mFocusArea.get(0)).rect.right)).concat(",").concat(Integer.toString(((Area) this.mFocusArea.get(0)).rect.bottom));
            if (((Area) this.mFocusArea.get(0)).rect.left == -1 && ((Area) this.mFocusArea.get(0)).rect.top == -1 && ((Area) this.mFocusArea.get(0)).rect.right == -1 && ((Area) this.mFocusArea.get(0)).rect.bottom == -1) {
                value = CameraConstants.NULL;
            }
            CamLog.m3d(CameraConstants.TAG, "-tf-  value : " + value);
            parameters.set(ParamConstants.KEY_FOCUS_MODE_OBJECT_TRACKING_AREA, value);
            cameraDevice.setParameters(parameters);
            CamLog.m3d(CameraConstants.TAG, "## Touch focus setParameter :  left " + ((Area) this.mFocusArea.get(0)).rect.left + " right " + ((Area) this.mFocusArea.get(0)).rect.right + " top " + ((Area) this.mFocusArea.get(0)).rect.top + " bottom " + ((Area) this.mFocusArea.get(0)).rect.bottom);
        }
    }

    public boolean cancelAutoFocus() {
        CamLog.m3d(CameraConstants.TAG, "cancelAutoFocus");
        return true;
    }

    protected void releaseTouchFocus() {
    }

    public void releaseFocusHandler() {
        if (this.mReleaseTouchFocus != null) {
            this.mGet.removePostRunnable(this.mReleaseTouchFocus);
        }
        if (this.mHideTouchFocus != null) {
            this.mGet.removePostRunnable(this.mHideTouchFocus);
        }
    }

    public HandlerRunnable getReleaseTouchFocusRunnable() {
        return this.mReleaseTouchFocus;
    }

    public void release() {
        if (this.mCameraFocusView != null) {
            this.mCameraFocusView.setVisibility(4);
            this.mCameraFocusView.unbind();
            this.mCameraFocusView = null;
        }
        if (this.mCameraMultiFocusView != null) {
            this.mCameraMultiFocusView.setVisibility(4);
            this.mCameraMultiFocusView.unbind();
            this.mCameraMultiFocusView = null;
        }
        if (this.mCameraManualMultiFocusView != null) {
            this.mCameraManualMultiFocusView.setVisibility(4);
            this.mCameraManualMultiFocusView.unbind();
            this.mCameraManualMultiFocusView = null;
        }
        if (this.mFocusArea != null) {
            this.mFocusArea.clear();
            this.mFocusArea = null;
        }
        this.mHandler = null;
    }

    protected void setEVShutterButtonPosition(int x, int y) {
        if (this.mEVShutterButton != null) {
            int marginTop = y - (this.mEVShutterButton.getHeight() / 2);
            LayoutParams lp = (LayoutParams) this.mEVShutterButton.getLayoutParams();
            lp.leftMargin = x - (this.mEVShutterButton.getWidth() / 2);
            lp.topMargin = marginTop;
            this.mEVShutterButton.setLayoutParams(lp);
        }
    }
}
