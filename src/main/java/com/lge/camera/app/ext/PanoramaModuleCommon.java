package com.lge.camera.app.ext;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;

public class PanoramaModuleCommon extends PanoramaModuleCommonBase {
    protected final float FACTOR_NORMAL_TO_WIDE = 1.72f;
    protected View mBaseView = null;
    protected LinearLayout mButtonLayout = null;
    protected boolean mIsOnRecording = false;
    protected int mOldDegreeMiniOutline = -1;
    protected int mPictureH = 0;
    protected int mPictureW = 0;
    protected boolean mPrevDrawStarted = true;
    protected int mPreviewH = 0;
    protected RotateLayout mPreviewMiniLayout = null;
    protected RelativeLayout mPreviewMiniLayoutArrow = null;
    protected RelativeLayout mPreviewMiniLayoutOutline = null;
    protected int mPreviewW = 0;

    public PanoramaModuleCommon(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public boolean isFocusEnableCondition() {
        if ((isAFSupported() || !isRearCamera()) && !checkModuleValidate(128)) {
            return true;
        }
        return false;
    }

    public boolean onRecordStartButtonClicked() {
        if (this.mState == 2) {
            return super.onRecordStartButtonClicked();
        }
        CamLog.m3d(CameraConstants.TAG, "exit onRecordStartButtonClicked panoState = " + this.mState);
        return false;
    }

    protected void startRecorder() {
        stopEngine();
        this.mIsOnRecording = true;
        super.startRecorder();
    }

    protected void restoreRecorderToIdle() {
        super.restoreRecorderToIdle();
        this.mIsOnRecording = false;
        this.mState = 0;
    }

    protected void stopRecorder() {
        super.stopRecorder();
        this.mIsOnRecording = false;
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                PanoramaModuleCommon.this.mZoomManager.setZoomValue(0);
            }
        });
    }

    protected boolean takePicture() {
        if (this.mState == 2 && this.mPrevDrawStarted) {
            boolean result = super.takePicture();
            showDoubleCamera(false);
            return result;
        }
        CamLog.m7i(CameraConstants.TAG, "takePicture panorama mState : " + this.mState + ", mPrevDrawStarted : " + this.mPrevDrawStarted);
        return false;
    }

    protected void doTakePicture() {
        if (this.mState == 2) {
            startPanorama();
        } else {
            doBackKey();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        if (this.mState >= 3 && this.mState <= 5) {
            stopPanorama(false, false);
            setCameraState(1);
        }
        super.onConfigurationChanged(config);
    }

    protected boolean backKeyProcessing() {
        return true;
    }

    public boolean doBackKey() {
        if (this.mState < 3 || this.mState > 5) {
            return super.doBackKey();
        }
        CamLog.m3d(CameraConstants.TAG, "Panorama doBackKey - mState : " + this.mState);
        backKeyProcessing();
        boolean isSaving = this.mState == 5;
        stopPanorama(isSaving, false);
        if (isSaving) {
            return true;
        }
        resetToPreviewState();
        return true;
    }

    public void takePictureByTimer(int type) {
        if (type == 0) {
            doTakePicture();
        } else {
            super.takePictureByTimer(type);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    protected boolean isFocusOnTouchEvent() {
        return this.mFocusManager != null && checkModuleValidate(31) && isAFSupported() && isRearCamera() && !checkModuleValidate(128);
    }

    public void onShutterLargeButtonClicked() {
        if (checkModuleValidate(15) || this.mState == 5) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 8:
                    onShutterStopButtonClicked();
                    return;
                case 12:
                    onCameraShutterButtonClicked();
                    return;
                default:
                    super.onShutterLargeButtonClicked();
                    return;
            }
        }
    }

    protected void oneShotPreviewCallbackDone() {
        super.oneShotPreviewCallbackDone();
        if (!this.mIsOnRecording) {
            setDoubleCameraEnable(true);
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    PanoramaModuleCommon.this.startEngine();
                }
            }, 0);
        }
    }

    public void onVideoShutterClicked() {
        if (this.mState != 2) {
            CamLog.m3d(CameraConstants.TAG, "exit onVideoShutterClicked panoState = " + this.mState);
        } else {
            super.onVideoShutterClicked();
        }
    }

    public void onShutterStopButtonClicked() {
        if (this.mIsOnRecording) {
            super.onShutterStopButtonClicked();
        } else {
            doBackKey();
        }
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mState == 2) {
            return super.onCameraShutterButtonClicked();
        }
        if (this.mState <= 2 || this.mState == 4) {
            return false;
        }
        doBackKey();
        return false;
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        if (this.mBaseView != null) {
            this.mBaseView.setVisibility(4);
        }
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (!(isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL) || this.mBaseView == null)) {
            this.mBaseView.setVisibility(0);
        }
        return true;
    }

    protected void doSwitchCamera() {
        this.mLocalParamForZoom = null;
        if (checkModuleValidate(15)) {
            int cameraIdForSwitch;
            if (this.mCameraId == 0) {
                cameraIdForSwitch = 2;
            } else {
                cameraIdForSwitch = 0;
            }
            if (checkModuleValidate(192)) {
                int i;
                stopPreview();
                closeCamera();
                SharedPreferenceUtilBase.setCameraId(getAppContext(), cameraIdForSwitch);
                SharedPreferenceUtil.saveRearCameraId(getAppContext(), cameraIdForSwitch);
                this.mGet.setupSetting();
                if (isStartedByInAndOutZoom()) {
                    i = 1;
                } else {
                    i = 0;
                }
                startCameraDevice(i);
                setSettingMenuEnable(SettingKeyWrapper.getPictureSizeKey(getShotMode(), cameraIdForSwitch), false);
                setSettingMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), cameraIdForSwitch), false);
                if (this.mDoubleCameraManager != null) {
                    this.mDoubleCameraManager.setButtonsSelected();
                }
                if (this.mQuickButtonManager != null) {
                    this.mQuickButtonManager.setEnable(100, false);
                }
            }
        }
    }

    protected boolean isStartedByInAndOutZoom() {
        return false;
    }

    public void onNormalAngleButtonClicked() {
        if (!checkModuleValidate(192)) {
            super.onNormalAngleButtonClicked();
        } else if (getCameraId() != 0 && !this.mGet.isCameraChanging() && this.mState == 2) {
            doSwitchCamera();
            resetToPreviewState();
        }
    }

    public void onWideAngleButtonClicked() {
        if (!checkModuleValidate(192)) {
            super.onWideAngleButtonClicked();
        } else if (getCameraId() != 2 && !this.mGet.isCameraChanging() && this.mState == 2) {
            doSwitchCamera();
            resetToPreviewState();
        }
    }

    protected void resetToPreviewState() {
        this.mState = 0;
        if (this.mGet == null || isPaused() || this.mCameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "exit");
            return;
        }
        resetViews();
        startPreview(null, null);
        if (this.mFocusManager != null) {
            this.mFocusManager.registerCallback(0);
        }
    }

    private void resetViews() {
        if (!(this.mCaptureButtonManager == null || this.mReviewThumbnailManager == null || this.mQuickButtonManager == null)) {
            this.mReviewThumbnailManager.setEnabled(true);
            this.mCaptureButtonManager.changeButtonByMode(12);
            this.mCaptureButtonManager.setShutterButtonEnable(true, getShutterButtonType());
            this.mQuickButtonManager.updateButton(100);
            this.mCaptureButtonManager.setExtraButtonEnable(true, 3);
            View miniPreviewBorder = findViewById(C0088R.id.panorama_preview_mini_border);
            if (miniPreviewBorder != null) {
                miniPreviewBorder.setBackgroundResource(C0088R.drawable.panorama_white_box);
            }
            this.mPreviewMiniLayout.clearAnimation();
            if (FunctionProperties.isSupportedConeUI()) {
                this.mGet.enableConeMenuIcon(31, true);
            }
        }
        doCleanView(false, true, false);
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (PanoramaModuleCommon.this.mQuickButtonManager != null && PanoramaModuleCommon.this.mReviewThumbnailManager != null) {
                    if (!PanoramaModuleCommon.this.mGet.isLGUOEMCameraIntent()) {
                        PanoramaModuleCommon.this.mReviewThumbnailManager.setThumbnailVisibility(0);
                    }
                    CamLog.m3d(CameraConstants.TAG, "mQuickButtonManager.show");
                    PanoramaModuleCommon.this.mQuickButtonManager.show(true, false, true);
                    PanoramaModuleCommon.this.mQuickClipManager.enableQuickClip(true);
                    PanoramaModuleCommon.this.setQuickClipIcon(false, true);
                }
            }
        });
    }

    public void showPanoramaStopButton() {
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(8);
            this.mCaptureButtonManager.setShutterButtonEnable(true, getShutterButtonType());
        }
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        int i = 180;
        super.onOrientationChanged(degree, isFirst);
        if (this.mState != 5) {
            View view = this.mStartAndStopGuideTextLayout;
            if (degree != 180) {
                i = 0;
            }
            startRotateGuideText(view, i);
        }
        if (this.mState == 2) {
            setMiniOutline(0);
        }
    }

    protected boolean setMiniOutline(int degree) {
        if (this.mOldDegreeMiniOutline == degree || this.mPreviewMiniLayoutOutline == null) {
            return false;
        }
        this.mOldDegreeMiniOutline = degree;
        LayoutParams lpOutline = (LayoutParams) this.mPreviewMiniLayoutOutline.getLayoutParams();
        Utils.resetLayoutParameter(lpOutline);
        RelativeLayout dummyLayout = (RelativeLayout) this.mBaseView.findViewById(C0088R.id.panorama_preview_mini_dummy_layout);
        LayoutParams lpDummy = (LayoutParams) dummyLayout.getLayoutParams();
        calcMiniOutline(lpOutline, lpDummy);
        View arrowUp = this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_up);
        View arrowDown = this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_down);
        View arrowLeft = this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_left);
        View arrowRight = this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_right);
        lpOutline.addRule(13, 1);
        DisplayMetrics dm = Utils.getWindowRealMatics(getActivity());
        if (degree == 0 || degree == 180) {
            lpOutline.width = dm.widthPixels - (Utils.getPx(getAppContext(), C0088R.dimen.panorama_bar_side_margin) * 2);
            arrowUp.setVisibility(4);
            arrowDown.setVisibility(4);
            arrowLeft.setVisibility(0);
            arrowRight.setVisibility(0);
        } else if (degree == 90 || degree == 270) {
            int barMargineEnd = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 320.0f);
            lpOutline.width = (int) (((double) lpOutline.height) * 0.62d);
            lpOutline.height = dm.heightPixels - (barMargineEnd * 2);
            lpDummy.width = (int) (((double) lpDummy.width) * 1.15d);
            lpDummy.height = (int) (((double) lpDummy.height) * 1.15d);
            arrowUp.setVisibility(0);
            arrowDown.setVisibility(0);
            arrowLeft.setVisibility(4);
            arrowRight.setVisibility(4);
        }
        LayoutParams lp = (LayoutParams) this.mPreviewMiniLayout.getLayoutParams();
        lp.width = lpDummy.width - 1;
        lp.height = lpDummy.height - 1;
        this.mPreviewMiniLayout.setLayoutParams(lp);
        this.mPreviewMiniLayoutOutline.setLayoutParams(lpOutline);
        dummyLayout.setLayoutParams(lpDummy);
        return true;
    }

    private void calcMiniOutline(LayoutParams lpOutline, LayoutParams lpDummy) {
        float previewRatio = ((float) this.mPreviewW) / ((float) this.mPreviewH);
        int w = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 264.0f);
        int h = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 165.6f);
        int wideH = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 196.0f);
        int wideH2 = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 196.0f);
        if (useWideAngleExpand() && this.mCameraId == 2) {
            w = (int) (((float) w) * 1.72f);
            h = (int) (((float) h) * 1.72f);
            wideH = (int) (((float) wideH) * 1.72f);
            wideH2 = (int) (((float) wideH2) * 1.72f);
        }
        if (2 == getActivity().getResources().getConfiguration().orientation) {
            lpOutline.width = w;
            lpDummy.width = w;
            lpOutline.height = h;
            lpDummy.height = h;
            if (previewRatio > 1.777f) {
                lpOutline.height = wideH;
                lpDummy.height = wideH;
                return;
            } else if (previewRatio >= 1.6f) {
                lpOutline.height = wideH2;
                lpDummy.height = wideH2;
                return;
            } else {
                return;
            }
        }
        lpOutline.width = h;
        lpDummy.width = h;
        lpOutline.height = w;
        lpDummy.height = w;
        if (previewRatio > 1.777f) {
            lpOutline.width = wideH;
            lpDummy.width = wideH;
        } else if (previewRatio >= 1.6f) {
            lpOutline.width = wideH2;
            lpDummy.width = wideH2;
        }
        float factor = ((float) this.mPreviewH) / ((float) this.mPreviewW);
        lpOutline.width = (int) (((float) lpOutline.height) * factor);
        lpDummy.width = (int) (((float) lpDummy.height) * factor);
    }

    public boolean isZoomAvailable() {
        if (this.mCameraState == 1 || this.mCameraState == 2 || this.mCameraState == 3) {
            return false;
        }
        if (this.mCameraState != 0) {
            return super.isZoomAvailable();
        }
        if (this.mIsOnRecording) {
            return true;
        }
        return false;
    }

    protected void startPreview(CameraParameters params, SurfaceTexture surfaceTexture) {
        if (this.mCameraDevice != null) {
            if (params == null) {
                params = this.mCameraDevice.getParameters();
            }
            setParameterByLGSF(params, CameraConstants.PANO_PARAM_START, false);
            this.mNeedProgressDuringCapture = 0;
            super.startPreview(params, surfaceTexture);
        }
    }

    protected void initPreviewPictureSize() {
        CameraParameters param = this.mCameraDevice.getParameters();
        Size previewSize = param.getPreviewSize();
        Size pictureSize = param.getPictureSize();
        this.mPreviewW = previewSize.getWidth();
        this.mPreviewH = previewSize.getHeight();
        this.mPictureW = pictureSize.getWidth();
        this.mPictureH = pictureSize.getHeight();
        CamLog.m3d(CameraConstants.TAG, "panorama preview size=" + this.mPreviewW + "," + this.mPreviewH + " picture size=" + this.mPictureW + "," + this.mPictureH);
    }

    protected void changeRequester() {
        if (this.mParamUpdater != null) {
            this.mParamUpdater.addRequester("zoom", "0", false, true);
            String pictureSize = getPanoramaPictureSize(getPanoramaCameraId());
            if (pictureSize == null) {
                postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        CamLog.m3d(CameraConstants.TAG, "Error : UNSUPPORTED and finish app");
                        DebugUtil.printMemory();
                        Toast.makeText(PanoramaModuleCommon.this.getAppContext(), C0088R.string.camera_error_occurred_try_again, 0).show();
                        PanoramaModuleCommon.this.mGet.getActivity().finish();
                    }
                }, 0);
            }
            this.mParamUpdater.setParamValue("picture-size", pictureSize);
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, ModelProperties.getPanoramaPreviewSize(true));
            this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, getSettingValue(Setting.KEY_VIDEO_STEADY));
            this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, FunctionProperties.isSupportedZSL(this.mCameraId) ? "on" : "off");
            if (FunctionProperties.isSupportedHDR(isRearCamera()) > 0) {
                this.mParamUpdater.setParamValue("hdr-mode", "0", false);
            }
            if (FunctionProperties.getSupportedHal() == 2) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_FULLFRAME_PCALLBACK_STR);
                this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_FULL_FRAME_BUF_SIZE, "2");
            }
        }
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        hide();
    }

    public void startCameraSwitchingAnimation(int animationType) {
        super.startCameraSwitchingAnimation(animationType);
        hide();
    }

    public void onGestureCleanViewDetected() {
        if (!this.mIsTouchStartedFromNaviArea && checkModuleValidate(16) && !ModelProperties.isKeyPadSupported(getAppContext())) {
            if ((this.mCameraCapabilities != null && !this.mCameraCapabilities.isFrontCameraSupported()) || !this.mIsScreenCaptured) {
                return;
            }
            if (SystemBarUtil.isSystemUIVisible(getActivity())) {
                this.mGet.setNaviBarVisibility(true, 200);
            } else if (this.mHandler != null && checkModuleValidate(192)) {
                if ((this.mTimerManager == null || !(this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress())) && this.mState < 3 && !this.mGet.isAnimationShowing()) {
                    CamLog.m7i(CameraConstants.TAG, "-swap- onGestureCleanViewDetected");
                    hide();
                    stopPreviewThread();
                    this.mGet.setGestureType(this.mGestureManager.getGestureFlickingType());
                    this.mGet.startCameraSwitchingAnimation(1);
                    this.mHandler.sendEmptyMessage(6);
                    LdbUtil.sendLDBIntentForSwapCamera(this.mGet.getAppContext(), isRearCamera(), "Flicking");
                }
            }
        }
    }

    public void setVisiblePreviewMini(boolean isVisible, boolean isForced) {
        if (isVisible) {
            setMiniOutline(0);
        }
        startAnimationAlphaShowing(this.mPreviewMiniLayout, isVisible, isForced);
        startAnimationAlphaShowing(this.mPreviewMiniLayoutOutline, isVisible, isForced);
    }

    public void setVisibleArrowGuide(boolean isVisible, boolean isForced, boolean isBlicking, boolean useAnimation) {
        if (this.mPreviewMiniLayoutArrow != null) {
            this.mPreviewMiniLayoutArrow.clearAnimation();
            if (!useAnimation) {
                int visibility = isVisible ? 0 : 4;
                this.mPreviewMiniLayoutArrow.setVisibility(visibility);
                this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_up).setVisibility(visibility);
                this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_down).setVisibility(visibility);
                this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_left).setVisibility(visibility);
                this.mPreviewMiniLayoutArrow.findViewById(C0088R.id.panorama_arrow_right).setVisibility(visibility);
            } else if (isBlicking) {
                startAnimationGuideArrowShowing(this.mPreviewMiniLayoutArrow, isVisible, isForced);
            } else {
                startAnimationAlphaShowing(this.mPreviewMiniLayoutArrow, isVisible, isForced);
            }
        }
    }

    public void onPauseAfter() {
        if (isPaused()) {
            restorePictureSizeListPref();
        }
        super.onPauseAfter();
    }

    protected void switchCamera() {
        restorePictureSizeListPref();
        super.switchCamera();
    }

    protected void restoreSettingMenus() {
        restorePictureSizeListPref();
        super.restoreSettingMenus();
    }

    private void restorePictureSizeListPref() {
        int arrayIndex = 0;
        while (arrayIndex < 2) {
            int i;
            String shotMode = getShotMode();
            if (arrayIndex == 0) {
                i = 0;
            } else {
                i = 2;
            }
            ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(shotMode, i));
            if (!(listPref == null || this.mBackupOriginalEntries == null || this.mBackupOriginalEntries[arrayIndex] == null)) {
                listPref.setEntries(this.mBackupOriginalEntries[arrayIndex]);
                listPref.setEntryValues(this.mBackupOriginalEntryValues[arrayIndex]);
                listPref.setExtraInfos(this.mBackupOriginalExtraInfoTwo[arrayIndex], 2);
                this.mBackupOriginalEntries[arrayIndex] = null;
                this.mBackupOriginalEntryValues[arrayIndex] = null;
                this.mBackupOriginalExtraInfoTwo[arrayIndex] = null;
            }
            arrayIndex++;
        }
    }

    public void onDestroy() {
        this.mBaseView = null;
        this.mPreviewMiniLayout = null;
        this.mPreviewMiniLayoutOutline = null;
        this.mPreviewMiniLayoutArrow = null;
        this.mStartAndStopGuideTextLayout = null;
        super.onDestroy();
    }

    protected void panoramaSaveAfter() {
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setAfterShot();
        }
        sendLDBIntentOnTakePictureAfter();
    }
}
