package com.lge.camera.app;

import android.content.res.Configuration;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.managers.GraphyControlManager;
import com.lge.camera.managers.GraphyDataManager;
import com.lge.camera.managers.GraphyViewManager;
import com.lge.camera.managers.ManualControlManager;
import com.lge.camera.managers.ManualControlManager.ManualControlInterface;
import com.lge.camera.managers.ManualData;
import com.lge.camera.managers.ManualDataSS;
import com.lge.camera.managers.ManualModuleInterface;
import com.lge.camera.managers.ManualViewManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;

public abstract class ManualBaseModule extends DefaultCameraModule implements ManualControlInterface, ManualModuleInterface {
    protected GraphyControlManager mGraphyControlManager = null;
    protected GraphyDataManager mGraphyDataManager = null;
    protected GraphyViewManager mGraphyViewManager = null;
    protected ManualControlManager mManualControlManager = null;
    protected ManualViewManager mManualViewManager = null;

    protected abstract void initManualManager();

    public ManualBaseModule(ActivityBridge activityBridge) {
        super(activityBridge);
        initManualManager();
        if (this.mManualControlManager != null) {
            this.mManualControlManager.setCallback(this.mManualViewManager);
        }
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (this.mManualViewManager != null) {
            this.mManagerList.add(this.mManualViewManager);
        }
        if (this.mManualControlManager != null) {
            this.mManagerList.add(this.mManualControlManager);
        }
        if (this.mGraphyViewManager != null) {
            this.mManagerList.add(this.mGraphyViewManager);
        }
        if (this.mGraphyControlManager != null) {
            this.mManagerList.add(this.mGraphyControlManager);
        }
        if (this.mGraphyDataManager != null) {
            this.mManagerList.add(this.mGraphyDataManager);
        }
    }

    public boolean isRatioGuideNeeded() {
        if (ModelProperties.isLongLCDModel()) {
            return false;
        }
        return true;
    }

    public boolean isSuperZoomEnableCondition() {
        return false;
    }

    public boolean isManualMode() {
        return true;
    }

    protected void initializeAfterCameraOpen() {
        super.initializeAfterCameraOpen();
        setManualRecommendMetaDataCallback(true);
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
        if (this.mManualViewManager != null) {
            this.mManualViewManager.enableManualControls(enable);
        }
    }

    public void onDrumVisibilityChanged(int type, boolean isVisible) {
        if (isVisible) {
            hideZoomBar();
            setFilmStrengthButtonVisibility(false, false);
        }
    }

    public void onManualButtonClicked(int type) {
        this.mLocalParamForZoom = null;
    }

    public boolean isDrumMovingAvailable() {
        if (this.mZoomManager == null || !this.mZoomManager.isZoomControllersGetTouched()) {
            return true;
        }
        return false;
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (this.mManualViewManager != null) {
            this.mManualViewManager.setButtonAndDrumUnselected(31);
        }
    }

    public void onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "shutter large clicked.");
        if (checkModuleValidate(15) && !this.mGet.isAnimationShowing() && !this.mCaptureButtonManager.isWorkingShutterAnimation() && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 12:
                    onShutterBottomButtonClickListener();
                    return;
                case 14:
                    onRecordStartButtonClicked();
                    return;
                default:
                    super.onShutterLargeButtonClicked();
                    return;
            }
        }
    }

    protected void afterCommonRequester() {
        super.afterCommonRequester();
        this.mParamUpdater.addRequester("lg-wb", ParamConstants.PARAM_VALUE_NOSET, false, true);
        this.mParamUpdater.addRequester(getISOParamKey(), ParamConstants.PARAM_VALUE_NOSET, false, false);
        this.mParamUpdater.addRequester(getShutterSpeedParamKey(), ParamConstants.PARAM_VALUE_NOSET, false, false);
        this.mParamUpdater.addRequester(ParamConstants.MANUAL_FOCUS_STEP, "-1", false, true);
        this.mParamUpdater.addRequester(ParamConstants.KEY_AUTO_EXPOSURE_LOCK, "false", false, true);
        this.mParamUpdater.addRequester(ParamConstants.KEY_EXPOSURE_COMPENSATION, "0", false, true);
        this.mParamUpdater.addRequester(ParamConstants.KEY_FOCUS_PEAKING, "off", false, true);
    }

    public boolean onHideMenu(int menuType) {
        boolean ret = super.onHideMenu(menuType);
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onMenuVisibilityChanged(false);
        }
        return ret;
    }

    public boolean onShowMenu(int menuType) {
        if (isMenuShowing(menuType)) {
            return false;
        }
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onMenuVisibilityChanged(true);
        }
        return super.onShowMenu(menuType);
    }

    protected boolean isEnableManualView() {
        return (isAttachIntent() || isRecordingState() || isTimerShotCountdown()) ? false : true;
    }

    protected void doCleanViewAfterStopRecording() {
        super.doCleanViewAfterStopRecording();
        if (isEnableManualView() && this.mAdvancedFilmManager != null) {
            setFilmStrengthButtonVisibility(true, false);
        }
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (this.mManualViewManager != null && this.mManualControlManager != null && this.mCaptureButtonManager != null) {
            boolean enableManualView = isEnableManualView();
            CamLog.m3d(CameraConstants.TAG, "doCleanView : doByAction = " + doByAction + ", useAnimation = " + useAnimation + ", saveState = " + saveState + ", enableManualView = " + enableManualView);
            CamLog.m3d(CameraConstants.TAG, "doCleanView, isRecordingState : " + isRecordingState());
            if (doByAction && !enableManualView && saveState) {
                this.mManualControlManager.resetParameters(31);
            }
            if (!this.mGet.isModuleChanging()) {
                if (enableManualView) {
                    this.mManualViewManager.setPanelVisibility(0);
                    return;
                }
                this.mManualViewManager.setPanelVisibility(8);
                this.mManualViewManager.audioBtnClicked(false);
            }
        }
    }

    public void initUIDone() {
        super.initUIDone();
        if (this.mManualViewManager != null) {
            this.mManualViewManager.updateAllButtonValueByForce();
        }
    }

    protected void changeRequester() {
        super.changeRequester();
        this.mParamUpdater.setParamValue("hdr-mode", "0");
        if (this.mManualControlManager != null && this.mCameraDevice != null && this.mCameraDevice.getParameters() != null) {
            int unsuportedFeature = 0;
            if (!this.mCameraCapabilities.isAFSupported()) {
                unsuportedFeature = 0 | 16;
            }
            this.mManualControlManager.setManualControlInterface(this);
            if (!this.mManualControlManager.isInitValues()) {
                CameraParameters parameters = this.mCameraDevice.getParameters();
                if (parameters != null) {
                    this.mManualControlManager.loadManualData(parameters);
                } else {
                    return;
                }
            }
            this.mManualControlManager.setDisabledFeature(unsuportedFeature);
            this.mManualControlManager.setUncontrollableFeatureByViewMode();
            this.mManualControlManager.setLockedFeature();
            this.mManualControlManager.setInitialParameters();
        }
    }

    protected void oneShotPreviewCallbackDoneAfter() {
        super.oneShotPreviewCallbackDoneAfter();
        if (ManualUtil.isManualVideoMode(getShotMode()) || FunctionProperties.getSupportedHal() != 2) {
            this.mParamUpdater.removeRequester(ParamConstants.KEY_EXPOSURE_COMPENSATION);
        }
    }

    public void onPauseAfter() {
        setManualRecommendMetaDataCallback(false);
        super.onPauseAfter();
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        if (this.mManualViewManager != null) {
            this.mManualViewManager.destroyViews();
            this.mManualViewManager.initAllViews();
            this.mManualViewManager.updateViews();
        }
    }

    protected void setPreviewLayoutParam() {
        CamLog.m3d(CameraConstants.TAG, "-manual- setPreviewLayoutParam");
        if (isRatioGuideNeeded()) {
            int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
            if (lcdSize != null) {
                if (!(isMMSIntent() && this.mGet.isVideoCaptureMode())) {
                    this.mGet.setTextureLayoutParams(lcdSize[0], lcdSize[1], -1);
                }
                if (this.mPreviewFrameLayout != null) {
                    this.mPreviewFrameLayout.setAspectRatio(((double) lcdSize[0]) / ((double) lcdSize[1]));
                }
                setAnimationLayout(3);
                return;
            }
            return;
        }
        super.setPreviewLayoutParam();
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        super.setContentFrameLayoutParam(params);
        if (isRatioGuideNeeded()) {
            String contentSize = getContentSize();
            if (contentSize == null) {
                super.setContentFrameLayoutParam(params);
                return;
            }
            int[] contentSizeArr = Utils.sizeStringToArray(contentSize);
            int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
            if (contentSizeArr == null || lcdSize == null) {
                super.setContentFrameLayoutParam(params);
                return;
            }
            float scaleRatio;
            float[] targetSize = new float[]{(float) lcdSize[0], (float) lcdSize[1]};
            float contentSizeRatioY = ((float) contentSizeArr[1]) / ((float) contentSizeArr[0]);
            targetSize[0] = ((float) lcdSize[0]) * (((float) contentSizeArr[0]) / ((float) contentSizeArr[1]));
            targetSize[1] = targetSize[0] * contentSizeRatioY;
            if (targetSize[0] > ((float) lcdSize[0])) {
                scaleRatio = ((float) lcdSize[0]) / targetSize[0];
                targetSize[0] = targetSize[0] * scaleRatio;
                targetSize[1] = targetSize[1] * scaleRatio;
            }
            if (targetSize[1] > ((float) lcdSize[1])) {
                scaleRatio = ((float) lcdSize[1]) / targetSize[1];
                targetSize[0] = targetSize[0] * scaleRatio;
                targetSize[1] = targetSize[1] * scaleRatio;
            }
            LayoutParams relativeParams = Utils.getRelativeLayoutParams(this.mGet.getAppContext(), (int) targetSize[0], (int) targetSize[1]);
            int startPointX = (int) ((((float) lcdSize[0]) - targetSize[0]) / 2.0f);
            int startPointY = (int) ((((float) lcdSize[1]) - targetSize[1]) / 2.0f);
            if (relativeParams != null) {
                relativeParams.setMarginStart(startPointY);
                relativeParams.topMargin = startPointX;
                View gridView = findViewById(C0088R.id.preview_frame_grid);
                if (gridView != null) {
                    gridView.setLayoutParams(relativeParams);
                }
                if (this.mFocusManager != null) {
                    this.mFocusManager.setTouchAreaWindow(relativeParams.width, relativeParams.height);
                }
            }
        }
    }

    protected void setManualRecommendMetaDataCallback(boolean bSet) {
        CamLog.m3d(CameraConstants.TAG, "setManualRecommendMetaDataCallback = " + this.mCameraDevice + ", bSet : " + bSet);
        if (this.mCameraDevice != null && this.mManualRecommendMetaDataCallback != null) {
            if (bSet) {
                this.mCameraDevice.setManualCameraMetadataCb(this.mManualRecommendMetaDataCallback);
            } else {
                this.mCameraDevice.setManualCameraMetadataCb(null);
            }
        }
    }

    public void updateParameters(String key, String value) {
        if (FunctionProperties.isSupportedManualZSL()) {
            if (getISOParamKey().equals(key)) {
                value = convertISOParamValue(value);
            } else if (getShutterSpeedParamKey().equals(key)) {
                value = convertShutterSpeedParamValue(value);
            }
        }
        this.mParamUpdater.setParamValue(key, value);
    }

    public void setParameters(String key, String value) {
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                this.mParamUpdater.setParameters(parameters, key, value);
                this.mCameraDevice.setParameters(parameters);
            }
        }
    }

    protected void updateManualSettingValueFromMetadataCallback(float currentWB, float currentEV, float currentISO, float currentShutterSpeed, float currentMFStep) {
        if (this.mManualControlManager != null) {
            this.mManualControlManager.setManualCallbackData(currentWB, currentEV, currentISO, currentShutterSpeed, currentMFStep);
        }
    }

    protected void changeToAutoView() {
        this.mGet.setCurrentConeMode(1, true);
        setSetting(Setting.KEY_MODE, "mode_normal", true);
        this.mGet.modeMenuClicked("mode_normal");
    }

    public boolean isFocusEnableCondition() {
        if (isManualFocusMode() || (!isAFSupported() && getAELock())) {
            return false;
        }
        return super.isFocusEnableCondition();
    }

    public int getSupportedFeature() {
        if (this.mManualControlManager == null) {
            return 0;
        }
        return this.mManualControlManager.getSupportedFeature();
    }

    public ManualData getManualData(int type) {
        if (this.mManualControlManager == null) {
            return null;
        }
        return this.mManualControlManager.getManualData(type);
    }

    public boolean isSupportedAEUnlock() {
        if (this.mManualControlManager == null) {
            return false;
        }
        return this.mManualControlManager.isSupportedAEUnlock();
    }

    public boolean getAELock() {
        if (this.mManualControlManager == null) {
            return false;
        }
        return this.mManualControlManager.getAELock();
    }

    public boolean setAELock(Boolean isLock) {
        this.mLocalParamForZoom = null;
        if (this.mManualControlManager == null) {
            return false;
        }
        updateFlashSetting(isLock.booleanValue());
        return this.mManualControlManager.setAELock(isLock);
    }

    public void updateFlashSetting(boolean isLock) {
        if (ManualUtil.isManualCameraMode(getShotMode())) {
            CameraParameters parameters = null;
            if (this.mCameraDevice != null) {
                parameters = this.mCameraDevice.getParameters();
            }
            if (isLock) {
                setSpecificSettingValueAndDisable("flash-mode", "off", false);
                updateFlashParam(parameters, 50, false);
            } else {
                restoreFlashSetting();
                setSettingMenuEnable("flash-mode", true);
                updateFlashParam(parameters, ParamUtils.getFlashMode(this.mGet.getCurSettingValue("flash-mode")), true);
            }
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    ManualBaseModule.this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
                }
            });
        }
    }

    public boolean setManualData(int type, String key, String value, boolean save) {
        if (this.mManualControlManager == null) {
            return false;
        }
        if (type == 8) {
            if (ManualControlManager.LOCK.equals(value)) {
                updateFlashSetting(true);
            }
            return this.mManualControlManager.setISO(key, value, save);
        } else if (type == 1) {
            if (ManualControlManager.LOCK.equals(value)) {
                updateFlashSetting(true);
            }
            return this.mManualControlManager.setSS(key, value, save);
        } else if (type == 2) {
            return this.mManualControlManager.setEV(key, value, save);
        } else {
            if (type == 4) {
                return this.mManualControlManager.setWB(key, value, save);
            }
            if (type == 16) {
                return this.mManualControlManager.setMF(key, value, save);
            }
            return false;
        }
    }

    public void loadManualData(CameraParameters parameter) {
        if (this.mManualControlManager != null) {
            this.mManualControlManager.loadManualData(parameter);
        }
    }

    public int getEnabledFeature() {
        if (this.mManualControlManager == null) {
            return 0;
        }
        return this.mManualControlManager.getEnabledFeature();
    }

    public int getControllableFeature() {
        if (this.mManualControlManager == null) {
            return 0;
        }
        return this.mManualControlManager.getControllableFeature();
    }

    public void setLockedFeature() {
        if (this.mManualControlManager != null) {
            this.mManualControlManager.setLockedFeature();
        }
    }

    public int getLockedFeature() {
        if (this.mManualControlManager == null) {
            return 0;
        }
        return this.mManualControlManager.getLockedFeature();
    }

    public boolean isEnableAutoFuntion(int type) {
        if (this.mManualControlManager == null) {
            return false;
        }
        return this.mManualControlManager.isEnableAutoFuntion(type);
    }

    public String getSettingDesc(String key) {
        return this.mGet.getSettingDesc(key);
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    public boolean onShutterBottomButtonLongClickListener() {
        CamLog.m7i(CameraConstants.TAG, "Skip long click event of shutter botton.");
        return true;
    }

    public void startedCameraByInAndOutZoom() {
        super.startedCameraByInAndOutZoom();
        if (checkModuleValidate(192)) {
            doCleanViewAfterStopRecording();
        }
        if (this.mManualViewManager != null) {
            this.mManualViewManager.updateManualPannel();
        }
    }

    public void setGuideTextSettingMenu(boolean isManual) {
        String string;
        ActivityBridge activityBridge = this.mGet;
        String str = "tracking-af";
        if (isManual) {
            string = getAppContext().getString(C0088R.string.setting_guide_tracking_AF);
        } else {
            string = getAppContext().getString(C0088R.string.setting_guide_tracking_AF_auto);
        }
        activityBridge.updateGuideTextSettingMenu(str, string);
    }

    public boolean isSupportedQuickClip() {
        return false;
    }

    public boolean isManualDrumShowing(int type) {
        if (this.mManualViewManager == null) {
            return false;
        }
        return this.mManualViewManager.isDrumShowing(type);
    }

    protected void doOpticZoom(boolean switchingByButton, boolean switchingByBar, int cameraIdForSwitch) {
        if (isOpticZoomSupported(null)) {
            if (this.mManualViewManager != null) {
                this.mManualViewManager.setButtonAndDrumUnselected(31);
            }
            super.doOpticZoom(switchingByButton, switchingByBar, cameraIdForSwitch);
        }
    }

    public void setInitFocus(boolean isMF) {
        if (this.mFocusManager != null) {
            this.mFocusManager.setManualFocus(isMF);
        }
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    public String getISOParamKey() {
        if (FunctionProperties.isSupportedManualZSL()) {
            return "iso";
        }
        return ParamConstants.KEY_MANUAL_ISO;
    }

    public String getShutterSpeedParamKey() {
        if (FunctionProperties.isSupportedManualZSL()) {
            return ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL;
        }
        return "shutter-speed";
    }

    public String convertISOParamValue(String iso) {
        if (FunctionProperties.getSupportedHal() == 2) {
            return iso;
        }
        if (FunctionProperties.isSupportedManualZSL() && !"auto".equals(iso)) {
            iso = "ISO" + iso;
        }
        return iso;
    }

    public String convertShutterSpeedParamValue(String shutterSpeed) {
        if (!FunctionProperties.isSupportedManualZSL()) {
            return shutterSpeed;
        }
        ManualDataSS md = (ManualDataSS) getManualData(1);
        if (md != null) {
            return md.convertShutterSpeedToExposureTime(shutterSpeed);
        }
        return shutterSpeed;
    }

    public boolean hasLogProfileLimitation() {
        return FunctionProperties.isSupportedLogProfile();
    }

    public boolean hasHDR10Limitation() {
        return FunctionProperties.isSupportedHDR10();
    }

    public void hideDrum() {
        if (this.mManualViewManager != null && this.mManualViewManager.isDrumShowing(31)) {
            this.mManualViewManager.setButtonAndDrumUnselected(31);
        }
    }

    public boolean isFaceDetectionSupported() {
        return false;
    }
}
