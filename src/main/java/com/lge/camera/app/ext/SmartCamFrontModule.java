package com.lge.camera.app.ext;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.p000v4.view.PointerIconCompat;
import android.view.MotionEvent;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.BeautyShotCameraModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ext.SmartCamInterface;
import com.lge.camera.managers.ext.SmartCamManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SmartcamUtil;
import com.lge.camera.util.Utils;
import com.lge.ellievision.parceldata.ISceneCategory;

public class SmartCamFrontModule extends BeautyShotCameraModule {
    private String mCurrentFilterKindName = null;
    private String mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
    private String mCurrentSelectedFilter = CameraConstants.FILM_SMARTCAM_NONE;
    private boolean mIsTimerShot = false;
    private int mLastBinningWhereFrom = 0;
    private int mLastEvParamValue = 0;
    private int mLastSilContrastParamValue = 0;
    private int mLastTextAutoContrastParamValue = 0;
    protected SmartCamInterface mSmartCamInterface = new C04841();
    private SmartCamManager mSmartCamManager = new SmartCamManager(this);
    protected Object mSyncFront = new Object();

    /* renamed from: com.lge.camera.app.ext.SmartCamFrontModule$1 */
    class C04841 implements SmartCamInterface {
        C04841() {
        }

        public void updateContrastParam(int value) {
            if (SmartCamFrontModule.this.mCameraDevice == null || FunctionProperties.getSupportedHal() != 2) {
                CamLog.m11w(CameraConstants.TAG, "AI-This function should be supported on HAL3 only.");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AI-updateContrastParam = " + value);
            synchronized (SmartCamFrontModule.this.mSyncFront) {
                CameraParameters param = SmartCamFrontModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    param.setContrast(value);
                    SmartCamFrontModule.this.mCameraDevice.setParameters(param);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                }
            }
        }

        public void updateAutoContrastSolution(int value) {
            if (SmartCamFrontModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-SmartCamFrontModule mCameraDevice is null. return!");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AI-SmartCamFrontModule - updateAutoContrastSolution value : " + value);
            synchronized (SmartCamFrontModule.this.mSyncFront) {
                CameraParameters param = SmartCamFrontModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    param.set(ParamConstants.KEY_APP_AUTO_CONTRAST_LEVEL, value);
                    SmartCamFrontModule.this.mCameraDevice.setParameters(param);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                }
            }
        }

        public void setAutoContrastSolution(boolean on) {
            if (SmartCamFrontModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-SmartCamFrontModule mCameraDevice is null. return!");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AI-SmartCamFrontModule - setAutoContrastSolution on/off : " + on);
            synchronized (SmartCamFrontModule.this.mSyncFront) {
                CameraParameters param = SmartCamFrontModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    param.set(ParamConstants.KEY_APP_AUTO_CONTRAST, on ? 1 : 0);
                    SmartCamFrontModule.this.mCameraDevice.setParameters(param);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                }
            }
        }

        public void resetAutoContrastSolution(boolean autoCont, boolean setParam) {
            if (SmartCamFrontModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-mCameraDevice is null. return!");
                return;
            }
            CameraParameters param = SmartCamFrontModule.this.mCameraDevice.getParameters();
            if (param != null) {
                param.set(ParamConstants.KEY_APP_AUTO_CONTRAST, autoCont ? 1 : 0);
                param.set(ParamConstants.KEY_APP_AUTO_CONTRAST_LEVEL, 200);
                if (setParam) {
                    SmartCamFrontModule.this.mCameraDevice.setParameters(param);
                    return;
                }
                return;
            }
            CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
        }

        public void resetAllparamFunction(int contVal, int evValue, boolean autoCont) {
            if (SmartCamFrontModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-mCameraDevice is null. return!");
                return;
            }
            synchronized (SmartCamFrontModule.this.mSyncFront) {
                CameraParameters param = SmartCamFrontModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    if (FunctionProperties.getSupportedHal() == 2) {
                        param.setContrast(contVal);
                    }
                    if (param.getExposureCompensation() != evValue) {
                        param.setExposureCompensation(evValue);
                    }
                    resetAutoContrastSolution(autoCont, false);
                    SmartCamFrontModule.this.mCameraDevice.setParameters(param);
                }
            }
        }

        public void applyFilterToSceneTextSelected(String selectedFilter) {
            synchronized (SmartCamFrontModule.this.mSyncFront) {
                CamLog.m3d(CameraConstants.TAG, "AI-selectedFilter " + selectedFilter);
                if (SmartCamFrontModule.this.mAdvancedFilmManager != null) {
                    SmartCamFrontModule.this.mAdvancedFilmManager.applySceneTextFilter(selectedFilter);
                }
            }
        }

        public void isStartWideAngleAnimation(boolean isStartWideAngleAni) {
            if (SmartCamFrontModule.this.mDoubleCameraManager != null) {
                SmartCamFrontModule.this.mDoubleCameraManager.isStartWideAngleAnimation(isStartWideAngleAni);
            }
        }

        public void setFilterListForSmartCam(String filterName, int selectedIndex) {
            if (SmartCamFrontModule.this.mAdvancedFilmManager != null) {
                SmartCamFrontModule.this.mAdvancedFilmManager.setFilterListForSmartCam(filterName, selectedIndex);
            }
        }

        public void onSmartCamBarShow(boolean show) {
            if (SmartCamFrontModule.this.mExtraPrevewUIManager == null) {
                return;
            }
            if (show) {
                SmartCamFrontModule.this.mExtraPrevewUIManager.hide(false, false, true);
            } else {
                SmartCamFrontModule.this.mExtraPrevewUIManager.show(false, false, true);
            }
        }

        public void setEVParam(int value) {
            if (SmartCamFrontModule.this.mCameraDevice != null) {
                synchronized (SmartCamFrontModule.this.mSyncFront) {
                    CameraParameters param = SmartCamFrontModule.this.mCameraDevice.getParameters();
                    int curValue = param.getExposureCompensation();
                    CamLog.m3d(CameraConstants.TAG, "AI-setEVParam : " + value + ", getEVParam : " + curValue);
                    if (param == null) {
                        CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                    } else if (curValue != value) {
                        param.setExposureCompensation(value);
                        SmartCamFrontModule.this.mCameraDevice.setParameters(param);
                    }
                }
            }
        }

        public void showFilmMenu(boolean show) {
            if (SmartCamFrontModule.this.mAdvancedFilmManager != null && SmartCamFrontModule.this.mSmartCamManager != null) {
                if (SmartCamFrontModule.this.mAdvancedFilmManager.isShowingFilmMenu() || !show) {
                    if (FunctionProperties.isLivePhotoSupported() && SmartCamFrontModule.this.mLivePhotoManager != null && "on".equals(SmartCamFrontModule.this.getSettingValue(Setting.KEY_LIVE_PHOTO)) && !SmartCamFrontModule.this.isRecordingState()) {
                        SmartCamFrontModule.this.mLivePhotoManager.enableLivePhoto();
                    }
                    if (SmartCamFrontModule.this.mGestureShutterManager != null && SmartCamFrontModule.this.mGestureShutterManager.isAvailableGestureShutterStarted()) {
                        SmartCamFrontModule.this.mGestureShutterManager.startGestureEngine();
                    }
                    SmartCamFrontModule.this.resumeShutterless();
                    SmartCamFrontModule.this.mAdvancedFilmManager.showFilmMenu(false, 3, true, true, 0, false);
                    SmartCamFrontModule.this.mSmartCamManager.hideSmartCamBar();
                    boolean isShotActivated;
                    if ((SmartCamFrontModule.this.mTimerManager == null || !(SmartCamFrontModule.this.mTimerManager.isTimerShotCountdown() || SmartCamFrontModule.this.mTimerManager.getIsGesureTimerShotProgress())) && !SmartCamFrontModule.this.mSnapShotChecker.checkMultiShotState(2)) {
                        isShotActivated = false;
                    } else {
                        isShotActivated = true;
                    }
                    if (!SmartCamFrontModule.this.isMenuShowing(PointerIconCompat.TYPE_ZOOM_OUT) && !isShotActivated) {
                        SmartCamFrontModule.this.showExtraPreviewUI(true, false, false, true);
                        return;
                    }
                    return;
                }
                if (FunctionProperties.isLivePhotoSupported() && SmartCamFrontModule.this.mLivePhotoManager != null) {
                    SmartCamFrontModule.this.mLivePhotoManager.disableLivePhoto();
                }
                SmartCamFrontModule.this.pauseShutterless();
                if (SmartCamFrontModule.this.mGestureShutterManager != null) {
                    SmartCamFrontModule.this.mGestureShutterManager.stopGestureEngine();
                }
                SmartCamFrontModule.this.mAdvancedFilmManager.showFilmMenu(true, 3, true, false, 0, false);
                SmartCamFrontModule.this.mAdvancedFilmManager.setSelfieOptionVisibility(false, false, true);
                SmartCamFrontModule.this.showExtraPreviewUI(false, false, true, true);
                SmartCamFrontModule.this.mSmartCamManager.hideSmartCamTagCloud();
            }
        }

        public void hideFocus() {
            if (SmartCamFrontModule.this.mFocusManager != null) {
                SmartCamFrontModule.this.mFocusManager.releaseTouchFocus();
            }
        }

        public boolean isShowingFilmMenu() {
            return SmartCamFrontModule.this.mAdvancedFilmManager != null && SmartCamFrontModule.this.mAdvancedFilmManager.isShowingFilmMenu();
        }

        public void onMotionHandShakingBinningReset() {
            SmartCamFrontModule.this.mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
        }

        public void hideZoomBar() {
            SmartCamFrontModule.this.hideZoomBar();
        }

        public void notifySceneChanged(String curCategory, String curDisplayName) {
            SmartCamFrontModule.this.mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
        }
    }

    public SmartCamFrontModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mSmartCamManager);
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setSmartCamLayoutVisibility(doByAction ? 8 : 0);
            if (doByAction) {
                this.mSmartCamManager.hideSmartCamBar();
                this.mSmartCamManager.hideSmartCamTagCloud();
            }
        }
        super.doCleanView(doByAction, useAnimation, saveState);
    }

    protected boolean checkToUseFilmEffect() {
        return true;
    }

    public void init() {
        super.init();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.showInitGuide();
        }
    }

    public void onResumeAfter() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.onStartSmartcam(false);
        }
        super.onResumeAfter();
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        super.setContentFrameLayoutParam(params);
        if (this.mSmartCamManager != null) {
            int previewTopMargin;
            int previewStartMargin = Utils.isConfigureLandscape(getAppContext().getResources()) ? params.getMarginStart() : params.topMargin;
            if (Utils.isConfigureLandscape(getAppContext().getResources())) {
                previewTopMargin = params.topMargin;
            } else {
                previewTopMargin = params.getMarginStart();
            }
            this.mSmartCamManager.setTagCloudLayout(params.width, params.height, previewStartMargin, previewTopMargin);
        }
    }

    public void onPauseBefore() {
        access$700(false);
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.onStopSmartcam(false);
            this.mGet.removePostRunnable(this.mSmartCamManager.mStopPeopleEffectRunnable);
        }
        super.onPauseBefore();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        this.mLastBinningWhereFrom = 0;
        this.mLastEvParamValue = 0;
        this.mLastTextAutoContrastParamValue = 0;
        this.mLastSilContrastParamValue = 0;
    }

    protected void onTakePictureAfter() {
        if (!(this.mAdvancedFilmManager == null || this.mSmartCamManager == null)) {
            if (this.mAdvancedFilmManager.isShowingFilmMenu()) {
                showDoubleCamera(true);
                setDoubleCameraEnable(true);
            }
            if (this.mIsTimerShot) {
                this.mSmartCamManager.startTimerForSmartCamScene(0);
                this.mIsTimerShot = false;
            }
            this.mCurrentSceneDisplayName = this.mSmartCamManager.getCurrentSceneDisplayName();
            this.mCurrentSelectedFilter = this.mGet.getCurSettingValue(Setting.KEY_SMART_CAM_FILTER);
        }
        super.onTakePictureAfter();
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (this.mSmartCamManager != null) {
            extraStr = extraStr + "aicam_scene=" + this.mCurrentSceneDisplayName + ";aicam_filter=" + this.mCurrentSelectedFilter + ";";
        }
        this.mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
        this.mCurrentSelectedFilter = CameraConstants.FILM_SMARTCAM_NONE;
        return extraStr;
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        if (FunctionProperties.isAppyingFilmLimitation()) {
            setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        }
        if (!isBinningSupportedMode()) {
            setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        }
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void setParameterByLGSF(CameraParameters parameters, String shotMode, boolean isRecording) {
        synchronized (this.mSyncFront) {
            if (!(this.mSmartCamManager == null || parameters == null || isRecording || !checkModuleValidate(192))) {
                parameters = this.mSmartCamManager.updateDeviceParams(parameters, false, this.mCurrentSceneDisplayName);
            }
            super.setParameterByLGSF(parameters, "mode_normal", isRecording);
        }
    }

    protected void startPreviewWithFilmEffect(CameraParameters params) {
        synchronized (this.mSyncFront) {
            if (!(this.mSmartCamManager == null || params == null || !checkModuleValidate(192))) {
                params = this.mSmartCamManager.updateDeviceParams(params, false, this.mCurrentSceneDisplayName);
            }
            super.startPreviewWithFilmEffect(params);
        }
    }

    protected void setManagersListener() {
        super.setManagersListener();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setSmartCamInterface(this.mSmartCamInterface);
        }
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected void onTakePictureBefore() {
        if (this.mTimerManager != null) {
            this.mIsTimerShot = this.mTimerManager.isTimerShotCountdown();
        }
        if (this.mIsTimerShot && this.mSmartCamManager != null) {
            this.mSmartCamManager.hideSmartCamBar();
        }
        super.onTakePictureBefore();
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue("hdr-mode");
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    public boolean onVideoShutterClickedBefore() {
        if (this.mSmartCamManager == null || this.mAdvancedFilmManager == null) {
            return super.onVideoShutterClickedBefore();
        }
        this.mCurrentSceneDisplayName = this.mSmartCamManager.getCurrentSceneDisplayName();
        this.mCurrentFilterKindName = this.mSmartCamManager.getCurFilterName();
        this.mCurrentSelectedFilter = this.mGet.getCurSettingValue(Setting.KEY_SMART_CAM_FILTER);
        this.mLastEvParamValue = this.mSmartCamManager.getEvParamValue();
        this.mLastTextAutoContrastParamValue = this.mSmartCamManager.getTextAutoContrastParamValue();
        this.mLastSilContrastParamValue = this.mSmartCamManager.getSilContrastParamValue();
        CamLog.m3d(CameraConstants.TAG, "AI-onVideoShutterClickedBefore, mLastEvParamValue : " + this.mLastEvParamValue + ", mLastTextAutoContrastParamValue : " + this.mLastTextAutoContrastParamValue + ", mLastSilContrastParamValue : " + this.mLastSilContrastParamValue);
        CamLog.m3d(CameraConstants.TAG, "AI-onVideoShutterClickedBefore, mCurrentSceneDisplayName : " + this.mCurrentSceneDisplayName + ", mCurrentFilterKindName : " + this.mCurrentFilterKindName + ", mCurrentSelectedFilter : " + this.mCurrentSelectedFilter);
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        this.mSmartCamManager.onStopSmartcam(true);
        if (!ISceneCategory.CATEGORY_DISPLAY_TEXT.equals(this.mCurrentSceneDisplayName)) {
            return true;
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SmartCamFrontModule.this.showToast(SmartCamFrontModule.this.mGet.getAppContext().getString(C0088R.string.ai_cam_effect_cannot_apply), CameraConstants.TOAST_LENGTH_SHORT);
            }
        }, 500);
        return true;
    }

    protected void setRecordingEVParameters(CameraParameters parameters) {
        if (this.mSmartCamManager != null && parameters != null) {
            CameraParameters cameraParameters = parameters;
            this.mSmartCamManager.updateSpecificDeviceParams(cameraParameters, this.mSmartCamManager.convertCurCategoryFromDisplayName(this.mCurrentSceneDisplayName), this.mLastEvParamValue, 0, this.mLastSilContrastParamValue);
        }
    }

    public void onImageData(Image image) {
        if (!(image == null || this.mSmartCamManager == null)) {
            this.mSmartCamManager.onImageData(image, null, null);
        }
        super.onImageData(image);
    }

    protected void afterStopRecording() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setLatestSceneAndFilter(this.mCurrentSceneDisplayName, this.mCurrentFilterKindName, this.mCurrentSelectedFilter);
            this.mSmartCamManager.setLatestSceneAndBar(this.mCurrentSceneDisplayName, this.mLastEvParamValue, this.mLastTextAutoContrastParamValue, this.mLastSilContrastParamValue);
        }
        super.afterStopRecording();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.onStartSmartcam(true);
        }
        this.mLastEvParamValue = 0;
        this.mLastTextAutoContrastParamValue = 0;
        this.mLastSilContrastParamValue = 0;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    public boolean isEVShutterSupportedMode() {
        return true;
    }

    public void onWideAngleButtonClicked() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.hideSmartCamTagCloud();
            this.mSmartCamManager.hideSmartCamBar();
            this.mSmartCamManager.stopPeopleEffect(true);
            this.mSmartCamManager.mResetUnknownCount.run();
            this.mSmartCamManager.startTimerForSmartCamScene(1);
        }
        super.onWideAngleButtonClicked();
        if (checkModuleValidate(192)) {
            showExtraPreviewUI(true, false, false, true);
        }
    }

    public void onPreviewFrame(byte[] data, CameraProxy camera) {
        if (!(data == null || camera == null || this.mSmartCamManager == null)) {
            this.mSmartCamManager.onImageData(null, data, camera);
        }
        super.onPreviewFrame(data, camera);
    }

    public void onNormalAngleButtonClicked() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.hideSmartCamTagCloud();
            this.mSmartCamManager.hideSmartCamBar();
            this.mSmartCamManager.mResetUnknownCount.run();
            this.mSmartCamManager.startTimerForSmartCamScene(1);
        }
        super.onNormalAngleButtonClicked();
        if (checkModuleValidate(192)) {
            showExtraPreviewUI(true, false, false, true);
        }
    }

    public void notifyNewMedia(Uri uri, boolean updateThumbnail) {
        super.notifyNewMedia(uri, updateThumbnail);
        if (checkModuleValidate(192)) {
            Intent intent = new Intent(SmartcamUtil.TAG_SERVICE_ACTION);
            intent.setClassName(SmartcamUtil.TAG_SERVICE_PACKAGE_NAME, SmartcamUtil.TAG_SERVICE_CLASS_NAME);
            intent.setData(uri);
            getActivity().startService(intent);
        }
    }

    protected void oneShotPreviewCallbackDone() {
        super.oneShotPreviewCallbackDone();
        access$700(true);
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.getResizedPreviewSize();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mSmartCamManager != null && this.mSmartCamManager.isSceneBtnSelected() && this.mSmartCamManager.isSmartCamBarVisibility()) {
            this.mSmartCamManager.hideSmartCamBar();
        }
        return super.onTouchEvent(event);
    }

    public String getShotMode() {
        return CameraConstants.MODE_SMART_CAM_FRONT;
    }

    protected void onShowSpecificMenu(int menuType) {
        super.onShowSpecificMenu(menuType);
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.hideSmartCamTagCloud();
            this.mSmartCamManager.setSmartCamLayoutVisibility(8);
        }
    }

    protected void onHideSpecificMenu(int menuType) {
        super.onHideSpecificMenu(menuType);
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setSmartCamLayoutVisibility(0);
            this.mSmartCamManager.hideSmartCamBar();
            this.mSmartCamManager.mResetUnknownCount.run();
            this.mSmartCamManager.startTimerForSmartCamScene(1);
        }
    }

    public void stopTimerForSmartCamScene() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.stopTimerForSmartCamScene();
        }
    }

    public void startTimerForSmartCamScene() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.startTimerForSmartCamScene(0);
        }
    }

    public boolean doBackKey() {
        if (this.mAdvancedFilmManager != null && this.mSmartCamManager != null && this.mAdvancedFilmManager.isShowingFilmMenu() && !this.mAdvancedFilmManager.isFilterMenuAnimationWorking()) {
            this.mAdvancedFilmManager.showFilmMenu(false, 3, true, true, 0, false);
            this.mSmartCamManager.hideSmartCamBar();
            this.mSmartCamManager.startTimerForSmartCamScene(0);
            if (this.mHandler == null) {
                return true;
            }
            this.mHandler.sendEmptyMessage(84);
            return true;
        } else if (this.mSmartCamManager.hideInitGuide(true)) {
            this.mSmartCamManager.startTimerForSmartCamScene(1);
            return true;
        } else if (this.mSmartCamManager.isSmartSamBarShown()) {
            this.mSmartCamManager.hideSmartCamBar();
            return true;
        } else {
            boolean isTimerActivated;
            if (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress()) {
                isTimerActivated = true;
            } else {
                isTimerActivated = false;
            }
            boolean ret_val = super.doBackKey();
            if (isTimerActivated) {
                this.mSmartCamManager.startTimerForSmartCamScene(1);
            }
            return ret_val;
        }
    }

    protected void onStartRecordingPreview() {
        super.onStartRecordingPreview();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setFilterButtonEnabled(false);
        }
    }

    protected void doShutterTopTouchUp() {
        super.doShutterTopTouchUp();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setFilterButtonEnabled(true);
        }
    }

    protected void doActionAfterIntervshot() {
        super.doActionAfterIntervshot();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.startTimerForSmartCamScene(0);
        }
    }

    protected void doIntervalShot() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.stopTimerForSmartCamScene();
        }
        super.doIntervalShot();
    }

    public void prepareRecordingVideo() {
        if (isShutterKeyOptionTimerActivated() && this.mSmartCamManager != null) {
            this.mSmartCamManager.hideSmartCamBar();
        }
        super.prepareRecordingVideo();
    }

    public void onBinningIconVisible(boolean visible, int whereFrom) {
        super.onBinningIconVisible(visible, whereFrom);
        if (this.mSmartCamManager == null) {
            return;
        }
        if (whereFrom == 1) {
            this.mLastBinningWhereFrom = whereFrom;
            this.mSmartCamManager.turnOnBinning(visible, whereFrom);
        } else if (whereFrom == 2) {
            if (this.mLastBinningWhereFrom != 2) {
                this.mCurrentSceneDisplayName = this.mSmartCamManager.getCurrentSceneDisplayName();
                this.mCurrentFilterKindName = this.mSmartCamManager.getCurFilterName();
                this.mCurrentSelectedFilter = this.mGet.getCurSettingValue(Setting.KEY_SMART_CAM_FILTER);
            }
            CamLog.m7i(CameraConstants.TAG, "AI-onBinningIconVisible, mCurrentSceneDisplayName : " + this.mCurrentSceneDisplayName + ", mCurrentFilterKindName : " + this.mCurrentFilterKindName + ", mCurrentSelectedFilter : " + this.mCurrentSelectedFilter + ", mLastBinningWhereFrom : " + this.mLastBinningWhereFrom);
            this.mLastBinningWhereFrom = whereFrom;
            this.mSmartCamManager.turnOnBinning(visible, 0);
        } else {
            if (this.mLastBinningWhereFrom == 2) {
                if (visible) {
                    this.mSmartCamManager.setLatestSceneAndFilter(this.mCurrentSceneDisplayName, this.mCurrentFilterKindName, this.mCurrentSelectedFilter);
                } else {
                    this.mSmartCamManager.setLatestSceneAndFilter(null, null, null);
                }
            }
            this.mLastBinningWhereFrom = whereFrom;
            this.mSmartCamManager.turnOnBinning(visible, whereFrom);
        }
    }

    public boolean isAICamInitGuideShowing() {
        return this.mSmartCamManager != null && this.mSmartCamManager.isAICamInitGuideShowing();
    }

    protected void completeHideQuickview() {
        super.completeHideQuickview();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.mResetUnknownCount.run();
            this.mSmartCamManager.startTimerForSmartCamScene(1);
        }
    }

    protected void restoreBinningSetting() {
        synchronized (this.mSyncFront) {
            super.restoreBinningSetting();
        }
    }
}
