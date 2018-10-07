package com.lge.camera.app.ext;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.view.MotionEvent;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
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

public class SmartCamModule extends DefaultCameraModule {
    private String mCurrentFilterKindName = null;
    private String mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
    private String mCurrentSelectedFilter = CameraConstants.FILM_SMARTCAM_NONE;
    private boolean mIsTimerShot = false;
    private int mLastBinningWhereFrom = 0;
    private int mLastEvParamValue = 0;
    private int mLastSilContrastParamValue = 0;
    private int mLastTextAutoContrastParamValue = 0;
    protected SmartCamInterface mSmartCamInterface = new C04861();
    private SmartCamManager mSmartCamManager = new SmartCamManager(this);
    protected Object mSync = new Object();

    /* renamed from: com.lge.camera.app.ext.SmartCamModule$1 */
    class C04861 implements SmartCamInterface {
        C04861() {
        }

        public void applyFilterToSceneTextSelected(String selectedFilter) {
            synchronized (SmartCamModule.this.mSync) {
                CamLog.m3d(CameraConstants.TAG, "AI-selectedFilter " + selectedFilter);
                if (SmartCamModule.this.mAdvancedFilmManager != null) {
                    SmartCamModule.this.mAdvancedFilmManager.applySceneTextFilter(selectedFilter);
                }
            }
        }

        public void isStartWideAngleAnimation(boolean isStartWideAngleAni) {
            if (SmartCamModule.this.mDoubleCameraManager != null) {
                SmartCamModule.this.mDoubleCameraManager.isStartWideAngleAnimation(isStartWideAngleAni);
            }
        }

        public void updateContrastParam(int value) {
            if (SmartCamModule.this.mCameraDevice == null || FunctionProperties.getSupportedHal() != 2) {
                CamLog.m11w(CameraConstants.TAG, "AI-This function should be supported on HAL3 only.");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AI-updateContrastParam = " + value);
            synchronized (SmartCamModule.this.mSync) {
                CameraParameters param = SmartCamModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    param.setContrast(value);
                    SmartCamModule.this.mCameraDevice.setParameters(param);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                }
            }
        }

        public void updateAutoContrastSolution(int value) {
            if (SmartCamModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-mCameraDevice is null. return!");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AI-updateAutoContrastSolution value : " + value);
            synchronized (SmartCamModule.this.mSync) {
                CameraParameters param = SmartCamModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    param.set(ParamConstants.KEY_APP_AUTO_CONTRAST_LEVEL, value);
                    SmartCamModule.this.mCameraDevice.setParameters(param);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                }
            }
        }

        public void setAutoContrastSolution(boolean on) {
            if (SmartCamModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-mCameraDevice is null. return!");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AI-setAutoContrastSolution on/off : " + on);
            synchronized (SmartCamModule.this.mSync) {
                CameraParameters param = SmartCamModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    param.set(ParamConstants.KEY_APP_AUTO_CONTRAST, on ? 1 : 0);
                    SmartCamModule.this.mCameraDevice.setParameters(param);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                }
            }
        }

        public void setEVParam(int value) {
            if (SmartCamModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-mCameraDevice is null. return!");
                return;
            }
            synchronized (SmartCamModule.this.mSync) {
                CameraParameters param = SmartCamModule.this.mCameraDevice.getParameters();
                int curValue = param.getExposureCompensation();
                CamLog.m3d(CameraConstants.TAG, "AI-setEVParam : " + value + ", getEVParam : " + curValue);
                if (param == null) {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                } else if (curValue != value) {
                    param.setExposureCompensation(value);
                    SmartCamModule.this.mCameraDevice.setParameters(param);
                }
            }
        }

        public void resetAllparamFunction(int contVal, int evValue, boolean autoCont) {
            if (SmartCamModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-mCameraDevice is null. return!");
                return;
            }
            synchronized (SmartCamModule.this.mSync) {
                CamLog.m5e(CameraConstants.TAG, "resetAllparamFunction");
                CameraParameters param = SmartCamModule.this.mCameraDevice.getParameters();
                if (param != null) {
                    if (FunctionProperties.getSupportedHal() == 2) {
                        param.setContrast(contVal);
                    }
                    if (param.getExposureCompensation() != evValue) {
                        param.setExposureCompensation(evValue);
                    }
                    resetAutoContrastSolution(autoCont, false);
                    SmartCamModule.this.mCameraDevice.setParameters(param);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
                }
            }
        }

        public void resetAutoContrastSolution(boolean autoCont, boolean setParam) {
            if (SmartCamModule.this.mCameraDevice == null) {
                CamLog.m11w(CameraConstants.TAG, "AI-mCameraDevice is null. return!");
                return;
            }
            CameraParameters param = SmartCamModule.this.mCameraDevice.getParameters();
            if (param != null) {
                param.set(ParamConstants.KEY_APP_AUTO_CONTRAST, autoCont ? 1 : 0);
                param.set(ParamConstants.KEY_APP_AUTO_CONTRAST_LEVEL, 200);
                if (setParam) {
                    SmartCamModule.this.mCameraDevice.setParameters(param);
                    return;
                }
                return;
            }
            CamLog.m7i(CameraConstants.TAG, "AI-Parameter is null.");
        }

        public void showFilmMenu(boolean show) {
            if (SmartCamModule.this.mAdvancedFilmManager != null && SmartCamModule.this.mSmartCamManager != null && SmartCamModule.this.mCaptureButtonManager != null) {
                if (SmartCamModule.this.mAdvancedFilmManager.isShowingFilmMenu() || !show) {
                    if (FunctionProperties.isLivePhotoSupported() && SmartCamModule.this.mLivePhotoManager != null && "on".equals(SmartCamModule.this.getSettingValue(Setting.KEY_LIVE_PHOTO)) && !SmartCamModule.this.isRecordingState()) {
                        SmartCamModule.this.mLivePhotoManager.enableLivePhoto();
                    }
                    SmartCamModule.this.mAdvancedFilmManager.showFilmMenu(false, 3, true, true, 0, false);
                    SmartCamModule.this.mSmartCamManager.hideSmartCamBar();
                    SmartCamModule.this.mCaptureButtonManager.setShutterZoomArrowVisibility(0);
                    return;
                }
                if (FunctionProperties.isLivePhotoSupported() && SmartCamModule.this.mLivePhotoManager != null) {
                    SmartCamModule.this.mLivePhotoManager.disableLivePhoto();
                }
                SmartCamModule.this.mAdvancedFilmManager.showFilmMenu(true, 3, true, false, 0, false);
                SmartCamModule.this.mSmartCamManager.hideSmartCamTagCloud();
                SmartCamModule.this.mCaptureButtonManager.setShutterZoomArrowVisibility(4);
                hideZoomBar();
            }
        }

        public void setFilterListForSmartCam(String filterName, int selectedIndex) {
            if (SmartCamModule.this.mAdvancedFilmManager != null) {
                SmartCamModule.this.mAdvancedFilmManager.setFilterListForSmartCam(filterName, selectedIndex);
            }
        }

        public void onSmartCamBarShow(boolean show) {
        }

        public boolean isShowingFilmMenu() {
            return SmartCamModule.this.mAdvancedFilmManager != null && SmartCamModule.this.mAdvancedFilmManager.isShowingFilmMenu();
        }

        public void hideFocus() {
            if (SmartCamModule.this.mFocusManager != null) {
                SmartCamModule.this.mFocusManager.releaseTouchFocus();
            }
        }

        public void hideZoomBar() {
            SmartCamModule.this.hideZoomBar();
        }

        public void notifySceneChanged(String curCategory, String curDisplayName) {
            SmartCamModule.this.mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
        }

        public void onMotionHandShakingBinningReset() {
            if ("on".equals(SmartCamModule.this.getSettingValue(Setting.KEY_BINNING))) {
                SmartCamModule.this.access$5000(false, 0);
                if (SmartCamModule.this.mCameraDevice != null) {
                    CameraParameters param;
                    if (FunctionProperties.getSupportedHal() == 2) {
                        param = SmartCamModule.this.mCameraDevice.getParameters();
                        if (!(param == null || SmartCamModule.this.mParamUpdater == null)) {
                            SmartCamModule.this.mParamUpdater.setParameters(param, ParamConstants.KEY_BINNING_PARAM, "normal");
                            SmartCamModule.this.mParamUpdater.setParameters(param, "picture-size", SmartCamModule.this.getCurrentSelectedPictureSize());
                            SmartCamModule.this.access$4700(param);
                        }
                    } else {
                        synchronized (SmartCamModule.this.mSync) {
                            param = SmartCamModule.this.mCameraDevice.getParameters();
                            if (param != null) {
                                param.set(ParamConstants.KEY_BINNING_PARAM, "normal");
                                param.set("picture-size", SmartCamModule.this.getCurrentSelectedPictureSize());
                                SmartCamModule.this.mCameraDevice.setParameters(param);
                            }
                        }
                    }
                }
                SmartCamModule.this.access$5000(true, 0);
            }
            SmartCamModule.this.mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
        }
    }

    public SmartCamModule(ActivityBridge activityBridge) {
        super(activityBridge);
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

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mSmartCamManager);
    }

    protected void setManagersListener() {
        super.setManagersListener();
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setSmartCamInterface(this.mSmartCamInterface);
        }
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
        this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, ParamConstants.VIDEO_3840_BY_2160, false);
        this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, CameraConstants.VIDEO_FHD_60FPS, false);
        this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, ParamConstants.VIDEO_3840_BY_2160, false);
        this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, CameraConstants.VIDEO_FHD_60FPS, false);
        if (isUHDmode() || isFHD60()) {
            this.mGet.setSetting(Setting.KEY_VIDEO_RECORDSIZE, this.mGet.getListPreference(Setting.KEY_VIDEO_RECORDSIZE).getDefaultValue(), false);
            this.mGet.setSetting(Setting.KEY_VIDEO_RECORDSIZE_SUB, this.mGet.getListPreference(Setting.KEY_VIDEO_RECORDSIZE_SUB).getDefaultValue(), false);
        }
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        if (FunctionProperties.isAppyingFilmLimitation()) {
            restoreSettingValue("hdr-mode");
        }
        restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE);
        restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE_SUB);
        for (String size : ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS) {
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, size, true);
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, size, true);
        }
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected void setRecordingEVParameters(CameraParameters parameters) {
        if (this.mSmartCamManager != null && parameters != null) {
            CameraParameters cameraParameters = parameters;
            this.mSmartCamManager.updateSpecificDeviceParams(cameraParameters, this.mSmartCamManager.convertCurCategoryFromDisplayName(this.mCurrentSceneDisplayName), this.mLastEvParamValue, 0, this.mLastSilContrastParamValue);
        }
    }

    public void prepareRecordingVideo() {
        if (isShutterKeyOptionTimerActivated() && this.mSmartCamManager != null) {
            this.mSmartCamManager.hideSmartCamBar();
        }
        super.prepareRecordingVideo();
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
        CamLog.m3d(CameraConstants.TAG, "AI-onVideoShutterClickedBefore, mCurrentSceneDisplayName : " + this.mCurrentSceneDisplayName + ", mCurrentFilterKindName : " + this.mCurrentFilterKindName + ", mCurrentSelectedFilter : " + this.mCurrentSelectedFilter);
        CamLog.m3d(CameraConstants.TAG, "AI-onVideoShutterClickedBefore, mLastEvParamValue : " + this.mLastEvParamValue + ", mLastTextAutoContrastParamValue : " + this.mLastTextAutoContrastParamValue + ", mLastSilContrastParamValue : " + this.mLastSilContrastParamValue);
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        this.mSmartCamManager.onStopSmartcam(true);
        if (!ISceneCategory.CATEGORY_DISPLAY_TEXT.equals(this.mCurrentSceneDisplayName)) {
            return true;
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SmartCamModule.this.showToast(SmartCamModule.this.mGet.getAppContext().getString(C0088R.string.ai_cam_effect_cannot_apply), CameraConstants.TOAST_LENGTH_SHORT);
            }
        }, 500);
        return true;
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

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    public boolean isEVShutterSupportedMode() {
        return true;
    }

    public void onImageData(Image image) {
        if (!(image == null || this.mSmartCamManager == null)) {
            this.mSmartCamManager.onImageData(image, null, null);
        }
        super.onImageData(image);
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
        if (this.mCaptureButtonManager != null && checkModuleValidate(192)) {
            this.mCaptureButtonManager.setShutterZoomArrowVisibility(0);
        }
        super.onNormalAngleButtonClicked();
    }

    public void onWideAngleButtonClicked() {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.hideSmartCamTagCloud();
            this.mSmartCamManager.hideSmartCamBar();
            this.mSmartCamManager.stopPeopleEffect(true);
            this.mSmartCamManager.mResetUnknownCount.run();
            this.mSmartCamManager.startTimerForSmartCamScene(1);
        }
        if (this.mCaptureButtonManager != null && checkModuleValidate(192)) {
            this.mCaptureButtonManager.setShutterZoomArrowVisibility(0);
        }
        super.onWideAngleButtonClicked();
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

    public void notifyNewMedia(Uri uri, boolean updateThumbnail) {
        super.notifyNewMedia(uri, updateThumbnail);
        if (checkModuleValidate(192)) {
            Intent intent = new Intent(SmartcamUtil.TAG_SERVICE_ACTION);
            intent.setClassName(SmartcamUtil.TAG_SERVICE_PACKAGE_NAME, SmartcamUtil.TAG_SERVICE_CLASS_NAME);
            intent.setData(uri);
            getActivity().startService(intent);
        }
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
            if (menuType == 1 || menuType == 2) {
                this.mSmartCamManager.mResetUnknownCount.run();
            }
            this.mSmartCamManager.startTimerForSmartCamScene(1);
        }
    }

    public String getShotMode() {
        return CameraConstants.MODE_SMART_CAM;
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

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        if (this.mSmartCamManager != null) {
            this.mSmartCamManager.setSmartCamLayoutVisibility(doByAction ? 8 : 0);
            if (doByAction) {
                this.mSmartCamManager.hideSmartCamBar();
                this.mSmartCamManager.hideSmartCamTagCloud();
            }
            super.doCleanView(doByAction, useAnimation, saveState);
        }
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
        super.access$100();
    }

    protected void setParameterByLGSF(CameraParameters parameters, String shotMode, boolean isRecording) {
        synchronized (this.mSync) {
            if (!(this.mSmartCamManager == null || parameters == null || isRecording || !checkModuleValidate(192))) {
                parameters = this.mSmartCamManager.updateDeviceParams(parameters, false, this.mCurrentSceneDisplayName);
            }
            super.setParameterByLGSF(parameters, "mode_normal", isRecording);
        }
    }

    protected void startPreviewWithFilmEffect(CameraParameters parameters) {
        synchronized (this.mSync) {
            if (!(this.mSmartCamManager == null || parameters == null || !checkModuleValidate(192))) {
                parameters = this.mSmartCamManager.updateDeviceParams(parameters, false, this.mCurrentSceneDisplayName);
            }
            super.startPreviewWithFilmEffect(parameters);
        }
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
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

    public boolean doBackKey() {
        if (this.mAdvancedFilmManager == null || this.mSmartCamManager == null || this.mCaptureButtonManager == null) {
            return false;
        }
        if (this.mAdvancedFilmManager.isShowingFilmMenu() && !this.mAdvancedFilmManager.isFilterMenuAnimationWorking()) {
            this.mAdvancedFilmManager.showFilmMenu(false, 3, true, true, 0, false);
            this.mSmartCamManager.hideSmartCamBar();
            this.mSmartCamManager.startTimerForSmartCamScene(0);
            this.mCaptureButtonManager.setShutterZoomArrowVisibility(0);
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

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (this.mSmartCamManager != null) {
            extraStr = extraStr + "aicam_scene=" + this.mCurrentSceneDisplayName + ";aicam_filter=" + this.mCurrentSelectedFilter + ";";
        }
        this.mCurrentSceneDisplayName = ISceneCategory.CATEGORY_UNKNOWN;
        this.mCurrentSelectedFilter = CameraConstants.FILM_SMARTCAM_NONE;
        return extraStr;
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
        synchronized (this.mSync) {
            super.restoreBinningSetting();
        }
    }
}
