package com.lge.camera.app;

import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.view.KeyEvent;
import com.lge.camera.C0088R;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.managers.AdvancedSelfieManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ChildSettingRunnable;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;
import java.io.File;
import java.util.HashMap;

public abstract class ManualVideoModule extends ManualVideoModuleBase {
    protected static final long TIME_TO_UPDATE_TIMER = 30;
    protected ChildSettingRunnable mChildSettingUpdater_manual_video = new C03281();
    private HandlerRunnable mDrawWave = new HandlerRunnable(this) {
        public void handleRun() {
            if (ManualVideoModule.this.mManualViewManager != null) {
                ManualVideoModule.this.mManualViewManager.drawWave();
            }
        }
    };
    protected boolean mHDR10MenuChanged = false;
    protected boolean mIsDualCameraMode = false;
    protected boolean mIsRestartInManualVideo = false;
    protected boolean mLogProfileMenuChanged = false;
    private boolean mManualFlashOn = false;
    private long mManualRecordingTime = 0;
    private int mUpdateTimer = 0;
    protected boolean mVideoSteadyChanged = false;
    private HandlerRunnable setHDR10bySettingClickedRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            boolean on = "on".equals(ManualVideoModule.this.getSettingValue(Setting.KEY_HDR10));
            ManualVideoModule.this.turnOnHDR10(on);
            ManualVideoModule.this.mManualViewManager.setVideoSettingInfo();
            if (on && ManualVideoModule.this.mToastManager != null) {
                ManualVideoModule.this.mToastManager.showLongToast(ManualVideoModule.this.mGet.getAppContext().getString(C0088R.string.manual_video_hdr10_on_toast));
            }
            ManualVideoModule.this.childSettingMenuClickedWithRestartPreview();
        }
    };

    /* renamed from: com.lge.camera.app.ManualVideoModule$1 */
    class C03281 extends ChildSettingRunnable {
        C03281() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            ManualVideoModule.this.setSetting(key, value, true);
            ManualVideoModule.this.onContentSizeChanged();
        }

        public boolean checkChildAvailable() {
            if (ManualVideoModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    public ManualVideoModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        this.mCaptureButtonManager.changeButtonByMode(14);
        disableZoomFunction();
        setPatialUpdateForManualVideo();
        if (FunctionProperties.isSupportedLogProfile() && "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
            setLogProfileMode(true);
            this.mManualViewManager.setLogDisplayLUTValue(this.mManualViewManager.getLogDisplayLUTValue());
        } else if (FunctionProperties.isSupportedHDR10() && "on".equals(getSettingValue(Setting.KEY_HDR10))) {
            disableWideAngle(true);
        }
    }

    public void init() {
        super.init();
        this.mGet.refreshSettingByCameraId();
    }

    protected void initializeSettingMenus() {
        setSetting("hdr-mode", "0", false);
        super.initializeSettingMenus();
        setSettingMenuEnable("hdr-mode", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_INCLINOMETER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.hasFilmLimitationForManualVideo()) {
            setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        }
        if (FunctionProperties.isSupportedLogProfile()) {
            if ("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
                setLogProfileModeForSettingMenu(true);
            }
            if (hasLogProfileLimitation()) {
                setSpecificSettingValueAndDisable(Setting.KEY_MANUAL_VIDEO_LOG, "off", false);
            }
        } else if (FunctionProperties.isSupportedHDR10() && hasHDR10Limitation()) {
            setSpecificSettingValueAndDisable(Setting.KEY_HDR10, "off", false);
        }
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setGuideTextSettingMenu(true);
        setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
        setSettingMenuEnable("picture-size", false);
        setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void updateSettingDuringInAndOutRecording() {
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                updateFlashSetting(parameters);
            }
        }
    }

    protected void updateFlashSetting(CameraParameters parameters) {
        CamLog.m3d(CameraConstants.TAG, "updateFlashSetting, mManualFlashOn : " + this.mManualFlashOn);
        String flashMode = this.mManualFlashOn ? "on" : "off";
        if (checkFeatureDisableBatteryLevel(1, false) != 0) {
            flashMode = "off";
        }
        if (CameraDeviceUtils.isFlashSupported(parameters, flashMode)) {
            if ("on".equals(flashMode)) {
                this.mParamUpdater.setParamValue("flash-mode", ParamConstants.FLASH_MODE_TORCH);
            } else {
                this.mParamUpdater.setParamValue("flash-mode", "off");
            }
        }
        this.mGet.setSetting("flash-mode", flashMode, false);
    }

    public boolean setFlashOffByHighTemperature(boolean isSetParam) {
        if (!super.setFlashOffByHighTemperature(isSetParam)) {
            return false;
        }
        this.mManualFlashOn = false;
        return true;
    }

    protected void addModuleChildSettingMap(HashMap<String, ChildSettingRunnable> map) {
        super.addModuleChildSettingMap(map);
        if (map != null) {
            map.put(Setting.KEY_MANUAL_VIDEO_BITRATE, this.mChildSettingUpdater_manual_video);
            map.put(Setting.KEY_MANUAL_VIDEO_FRAME_RATE, this.mChildSettingUpdater_manual_video);
            map.put(Setting.KEY_MANUAL_VIDEO_SIZE, this.mChildSettingUpdater_manual_video);
            map.put(Setting.KEY_MANUAL_VIDEO_AUDIO, this.mChildSettingUpdater_manual_video);
            if (FunctionProperties.isSupportedLogProfile()) {
                map.put(Setting.KEY_MANUAL_VIDEO_LOG, this.mChildSettingUpdater_manual_video);
            } else if (FunctionProperties.isSupportedHDR10()) {
                map.put(Setting.KEY_HDR10, this.mChildSettingUpdater_manual_video);
            }
        }
    }

    protected void restoreSettingMenus() {
        restoreSettingValue("hdr-mode");
        super.restoreSettingMenus();
        restoreSettingValue(Setting.KEY_VOICESHUTTER);
        restoreSettingValue(Setting.KEY_INCLINOMETER);
        if (!"1".equals(getSettingValue("hdr-mode"))) {
            restoreFlashSetting();
        } else if (getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_SIGNATURE);
        if (FunctionProperties.isSupportedLogProfile()) {
            if ("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
                setLogProfileModeForSettingMenu(false);
            }
            restoreSettingValue(Setting.KEY_MANUAL_VIDEO_LOG);
        }
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        this.mManualFlashOn = false;
        setGuideTextSettingMenu(false);
        restoreSettingValue(Setting.KEY_FULLVISION);
        setSettingMenuEnable("picture-size", true);
        setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, true);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_HDR10);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    public void childSettingMenuClicked(String key, String value, int clickedType) {
        if (checkModuleValidate(207)) {
            super.childSettingMenuClicked(key, value, clickedType);
            if (this.mCameraDevice == null) {
                CamLog.m3d(CameraConstants.TAG, "mCameraDevice is null");
                return;
            }
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                CamLog.m3d(CameraConstants.TAG, "childSettingMenuClicked - key : " + key + " / value = " + value);
                boolean isRestartPreview = false;
                if (Setting.KEY_MANUAL_VIDEO_SIZE.equals(key) && this.mManualViewManager != null) {
                    this.mPictureOrVideoSizeChanged = true;
                    String videoSize = getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE);
                    String mLiveSnapShotSize = getLiveSnapShotSize(parameters, videoSize);
                    this.mParamUpdater.setParamValue("picture-size", mLiveSnapShotSize);
                    if (isOpticZoomSupported(null) && !FunctionProperties.isSameResolutionOpticZoom()) {
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_PICTURE_SIZE_WIDE, mLiveSnapShotSize);
                        CamLog.m3d(CameraConstants.TAG, "[opticzoom] set KEY_PICTURE_SIZE_WIDE livesnapshot : " + mLiveSnapShotSize);
                    }
                    this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_SIZE, videoSize);
                    this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, checkPreviewSize(videoSize));
                    changeFPSValueSP(false);
                    isRestartPreview = true;
                    this.mGet.saveStartingWindowLayout(3);
                    if (FunctionProperties.isSupportedLogProfile() && isUHDmode()) {
                        restoreSettingValue(Setting.KEY_MANUAL_VIDEO_LOG);
                        setSettingMenuEnable(Setting.KEY_MANUAL_VIDEO_LOG, true);
                        if ("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
                            this.mLogProfileMenuChanged = true;
                            setLogProfileMode("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)));
                        }
                    } else if (FunctionProperties.isSupportedHDR10() && isUHDmode()) {
                        restoreSettingValue(Setting.KEY_HDR10);
                        if ("on".equals(getSettingValue(Setting.KEY_HDR10))) {
                            setSettingMenuEnable(Setting.KEY_HDR10, false);
                            this.mHDR10MenuChanged = true;
                            turnOnHDR10(true);
                        } else {
                            setSettingMenuEnable(Setting.KEY_HDR10, true);
                        }
                    }
                    this.mManualViewManager.setVideoSettingInfo();
                } else if (Setting.KEY_MANUAL_VIDEO_FRAME_RATE.equals(key) && this.mManualViewManager != null) {
                    this.mPictureOrVideoSizeChanged = true;
                    changeFPSValueSP(false);
                    isRestartPreview = true;
                    if (FunctionProperties.isSupportedLogProfile()) {
                        if (hasLogProfileLimitation()) {
                            this.mLogProfileMenuChanged = true;
                            setLogProfileMode(false);
                            this.mLogProfileMenuChanged = true;
                            setSpecificSettingValueAndDisable(Setting.KEY_MANUAL_VIDEO_LOG, "off", false);
                        } else {
                            restoreSettingValue(Setting.KEY_MANUAL_VIDEO_LOG);
                            setSettingMenuEnable(Setting.KEY_MANUAL_VIDEO_LOG, true);
                            if ("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
                                this.mLogProfileMenuChanged = true;
                                setLogProfileMode("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)));
                            }
                        }
                    } else if (FunctionProperties.isSupportedHDR10()) {
                        if (hasHDR10Limitation()) {
                            setSpecificSettingValueAndDisable(Setting.KEY_HDR10, "off", false);
                            this.mHDR10MenuChanged = true;
                            turnOnHDR10(false);
                        } else {
                            restoreSettingValue(Setting.KEY_HDR10);
                            if ("on".equals(getSettingValue(Setting.KEY_HDR10))) {
                                setSettingMenuEnable(Setting.KEY_HDR10, false);
                                this.mHDR10MenuChanged = true;
                                turnOnHDR10(true);
                            } else {
                                setSettingMenuEnable(Setting.KEY_HDR10, true);
                            }
                        }
                    }
                    this.mManualViewManager.setVideoSettingInfo();
                } else if (Setting.KEY_MANUAL_VIDEO_BITRATE.equals(key) && this.mManualViewManager != null) {
                    this.mManualViewManager.setVideoSettingInfo();
                } else if (Setting.KEY_MANUAL_VIDEO_AUDIO.equals(key) && this.mManualViewManager != null) {
                    this.mManualViewManager.setAudioBtnDrawable("on".equals(value));
                    if ("on".equals(value)) {
                        showDialog(139);
                    }
                } else if (Setting.KEY_MANUAL_VIDEO_LOG.equals(key) && this.mManualViewManager != null) {
                    boolean on = "on".equals(value);
                    isRestartPreview = true;
                    this.mLogProfileMenuChanged = true;
                    setLogProfileMode(on);
                    this.mManualViewManager.setVideoSettingInfo();
                    if (on) {
                        this.mManualViewManager.setLogDisplayLUTValue(true);
                        this.mManualViewManager.needDisplayLUTToast(true);
                        if (this.mToastManager != null) {
                            this.mToastManager.showLongToast(this.mGet.getAppContext().getString(C0088R.string.manual_video_log_profile_on_toast_2));
                        }
                    }
                    setSettingMenuEnable(Setting.KEY_MANUAL_VIDEO_LOG, false);
                } else if (FunctionProperties.getSupportedHal() == 2 && Setting.KEY_VIDEO_STEADY.equals(key)) {
                    isRestartPreview = true;
                    this.mVideoSteadyChanged = true;
                } else if (Setting.KEY_HDR10.equals(key)) {
                    if (!this.mHDR10MenuChanged) {
                        setSettingMenuEnable(Setting.KEY_HDR10, false);
                        postOnUiThread(this.setHDR10bySettingClickedRunnable, 0);
                    }
                    this.mHDR10MenuChanged = true;
                }
                if (isRestartPreview) {
                    childSettingMenuClickedWithRestartPreview();
                    return;
                }
                return;
            }
            return;
        }
        CamLog.m11w(CameraConstants.TAG, "childSettingMenuClicked, app finishing or recording. return.");
    }

    private void setLogProfileMode(boolean isSet) {
        disableWideAngle(isSet);
        setLogProfileModeForParam(isSet);
        setLogProfileModeForSettingMenu(isSet);
    }

    private void childSettingMenuClickedWithRestartPreview() {
        boolean z = false;
        if (!this.mAdvancedFilmManager.hasFilmLimitationForManualVideo()) {
            restoreSettingValue(Setting.KEY_FILM_EMULATOR);
            setSettingMenuEnable(Setting.KEY_FILM_EMULATOR, true);
            if (!needRestartCamera()) {
                setupPreview(null);
            }
        } else if (this.mAdvancedFilmManager == null || (CameraConstants.FILM_NONE.equals(this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR)) && !this.mAdvancedFilmManager.isRunningFilmEmulator())) {
            setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
            if (!needRestartCamera()) {
                setupPreview(null);
            }
        } else {
            setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
            AdvancedSelfieManager advancedSelfieManager = this.mAdvancedFilmManager;
            if (!needRestartCamera()) {
                z = true;
            }
            advancedSelfieManager.stopFilmEmulator(z, true);
        }
        this.mQuickButtonManager.updateButtonIcon(C0088R.id.quick_button_film_emulator, this.mQuickButtonManager.getFilmQuickButtonSelector(), this.mQuickButtonManager.getFilmQuickButtonPressedImage());
        this.mQuickButtonManager.updateButton(C0088R.id.quick_button_film_emulator);
        if (needRestartCamera()) {
            restartCamera();
        }
    }

    protected void disableWideAngle(boolean isSet) {
        replaceZoomManager(isSet);
        if (!isSet) {
            this.mDoubleCameraManager.setEnabledButton(2);
        } else if (this.mDoubleCameraManager != null && checkDoubleCameraAvailableMode(false)) {
            if (this.mCameraId == 2) {
                this.mDoubleCameraManager.setForceButtonsSelected(0);
                setCameraIdForNormalAngleOnly();
            }
            this.mDoubleCameraManager.setDisabledButton(2);
        }
    }

    protected void setLogProfileModeForSettingMenu(boolean isSet) {
        if (isSet) {
            setSpecificSettingValueAndDisable("hdr-mode", "1", false);
        } else {
            restoreSettingValue("hdr-mode");
        }
    }

    protected void setLogProfileModeForParam(boolean isSet) {
        CamLog.m3d(CameraConstants.TAG, "-log profile- isSet:" + isSet);
        if (isSet) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, "on");
            this.mParamUpdater.setParamValue(ParamConstants.KEY_CINEMA_MODE, ParamConstants.CINEMA_PREVIEW_ONLY);
            this.mParamUpdater.setParamValue(ParamConstants.KEY_CINEMA_VIGNETTE, ParamConstants.CINEMA_MANUAL_VIGNETTE);
            if (this.mManualViewManager.getLogDisplayLUTValue()) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_CINEMA_LUT, ParamConstants.CINEMA_MANUAL_DISPLAY_LUT);
                return;
            } else {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_CINEMA_LUT, ParamConstants.CINEMA_MANUAL_VIDEO_VALUE);
                return;
            }
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, "off");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_CINEMA_MODE, ParamConstants.CINEMA_OFF);
    }

    protected void setCameraIdForNormalAngleOnly() {
        this.mCameraId = 0;
        SharedPreferenceUtilBase.setCameraId(getAppContext(), this.mCameraId);
        SharedPreferenceUtil.saveRearCameraId(getAppContext(), this.mCameraId);
        this.mGet.setupSetting();
    }

    protected void turnOnHDR10(boolean isSet) {
        CamLog.m3d(CameraConstants.TAG, "-HDR10- isSet:" + isSet);
        disableWideAngle(isSet);
        setHDR10Param(isSet);
    }

    private void setHDR10Param(boolean isSet) {
        if (isSet) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, "on");
            this.mParamUpdater.setParamValue("hdr10", "on");
            return;
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, "off");
        this.mParamUpdater.setParamValue("hdr10", "off");
    }

    public boolean hasLogProfileLimitation() {
        try {
            return hasLogProfileLimitationInternal(Integer.parseInt(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE)));
        } catch (NumberFormatException e) {
            CamLog.m3d(CameraConstants.TAG, "KEY_MANUAL_VIDEO_FRAME_RATE is invalid" + e);
            return true;
        }
    }

    public boolean hasHDR10Limitation() {
        try {
            return Integer.parseInt(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE)) >= 60;
        } catch (NumberFormatException e) {
            CamLog.m3d(CameraConstants.TAG, "KEY_MANUAL_VIDEO_FRAME_RATE is invalid" + e);
            return true;
        }
    }

    private boolean hasLogProfileLimitationInternal(int fps) {
        return fps >= 60;
    }

    private boolean needRestartCamera() {
        boolean z = false;
        if (FunctionProperties.isSupportedLogProfile() && this.mLogProfileMenuChanged) {
            return true;
        }
        if ((FunctionProperties.isSupportedHDR10() && this.mHDR10MenuChanged) || this.mVideoSteadyChanged) {
            return true;
        }
        if (!this.mGet.isOpticZoomSupported(null)) {
            return false;
        }
        if (!(isZoomAvailable() && this.mIsDualCameraMode)) {
            z = true;
        }
        return z;
    }

    private void restartCamera() {
        if (this.mCameraDevice == null || this.mParamUpdater == null) {
            CamLog.m11w(CameraConstants.TAG, "restartCamera, device is null, return.");
            return;
        }
        if (this.mCameraStartUpThread != null && this.mCameraStartUpThread.isAlive()) {
            try {
                this.mCameraStartUpThread.join(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!checkModuleValidate(207)) {
                CamLog.m11w(CameraConstants.TAG, "restartCamera, app is finishing, return.");
                return;
            }
        }
        this.mGet.setCameraChanging(1);
        this.mParamUpdater.updateAllParameters(this.mCameraDevice.getParameters());
        stopPreview();
        closeCamera();
        startCameraDevice(2);
        this.mIsRestartInManualVideo = true;
    }

    protected void oneShotPreviewCallbackDoneAfter() {
        boolean z = true;
        super.oneShotPreviewCallbackDoneAfter();
        boolean isFocusInit = false;
        if (FunctionProperties.isSupportedLogProfile() && this.mLogProfileMenuChanged) {
            setSettingMenuEnable(Setting.KEY_MANUAL_VIDEO_LOG, !hasLogProfileLimitation());
            if (this.mFocusManager != null) {
                this.mFocusManager.initializeFocusManagerByForce();
                this.mFocusManager.initAFView();
                if (!isRecordingState()) {
                    this.mFocusManager.registerCallback();
                }
                isFocusInit = true;
            }
        }
        if (FunctionProperties.isSupportedHDR10() && this.mHDR10MenuChanged) {
            String str = Setting.KEY_HDR10;
            if (hasHDR10Limitation()) {
                z = false;
            }
            setSettingMenuEnable(str, z);
            if (!(this.mFocusManager == null || isFocusInit)) {
                this.mFocusManager.initializeFocusManagerByForce();
                this.mFocusManager.initAFView();
                if (!isRecordingState()) {
                    this.mFocusManager.registerCallback();
                }
                isFocusInit = true;
            }
        }
        if (!(!this.mVideoSteadyChanged || this.mFocusManager == null || isFocusInit)) {
            this.mFocusManager.initializeFocusManagerByForce();
            this.mFocusManager.initAFView();
            if (!isRecordingState()) {
                this.mFocusManager.registerCallback();
            }
        }
        this.mLogProfileMenuChanged = false;
        this.mHDR10MenuChanged = false;
        this.mVideoSteadyChanged = false;
    }

    protected void initializeAfterCameraOpen() {
        super.initializeAfterCameraOpen();
        if (this.mIsRestartInManualVideo) {
            this.mIsRestartInManualVideo = false;
            this.mGet.removeCameraChanging(1);
        }
    }

    protected void addCommonRequester() {
        super.addCommonRequester();
        if (FunctionProperties.isSupportedLogProfile()) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)) ? "on" : "off", false, true);
            this.mParamUpdater.addRequester(ParamConstants.KEY_CINEMA_MODE, "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)) ? ParamConstants.CINEMA_PREVIEW_ONLY : ParamConstants.CINEMA_OFF, false, true);
            this.mParamUpdater.addRequester(ParamConstants.KEY_CINEMA_VIGNETTE, ParamConstants.CINEMA_MANUAL_VIGNETTE, false, true);
            this.mParamUpdater.addRequester(ParamConstants.KEY_CINEMA_LUT, ParamConstants.CINEMA_MANUAL_DISPLAY_LUT, false, true);
        } else if (FunctionProperties.isSupportedHDR10()) {
            boolean isHdr10;
            if (!"on".equals(getSettingValue(Setting.KEY_HDR10)) || hasHDR10Limitation()) {
                isHdr10 = false;
            } else {
                isHdr10 = true;
            }
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, isHdr10 ? "on" : "off", false, true);
            this.mParamUpdater.addRequester("hdr10", isHdr10 ? "on" : "off", false, true);
        }
    }

    protected void changeRequester() {
        boolean z = false;
        super.changeRequester();
        this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, "off");
        String initZoomValue = "0";
        if (this.mZoomManager != null) {
            initZoomValue = Integer.toString(this.mZoomManager.getZoomValue());
        }
        if (isZoomAvailable()) {
            this.mParamUpdater.setParamValue("zoom", initZoomValue);
        } else {
            this.mParamUpdater.addRequester("zoom", initZoomValue, false, true);
        }
        this.mParamUpdater.setParamValue("recording-hint", "true");
        String focusMode = ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO;
        if (!(this.mCameraCapabilities == null || this.mCameraCapabilities.isAFSupported())) {
            focusMode = ParamConstants.FOCUS_MODE_FIXED;
        }
        this.mParamUpdater.setParamValue("focus-mode", focusMode);
        if (this.mFocusManager != null && this.mFocusManager.isManualFocusMode()) {
            this.mParamUpdater.setParamValue("focus-mode", "normal");
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL_BUFF_COUNT, "1");
        String videoSize = getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_SIZE, videoSize);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, checkPreviewSize(videoSize));
        restoreSteadyCamSetting();
        this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, getSettingValue(Setting.KEY_VIDEO_STEADY));
        if (this.mCameraDevice == null) {
            return;
        }
        if (checkModuleValidate(128) || !isOpticZoomSupported(null)) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                updateFlashSetting(parameters);
                String mLiveSnapShotSize = getLiveSnapShotSize(parameters, videoSize);
                this.mParamUpdater.setParamValue("picture-size", mLiveSnapShotSize);
                if (isOpticZoomSupported(null) && !FunctionProperties.isSameResolutionOpticZoom()) {
                    this.mParamUpdater.setParamValue(ParamConstants.KEY_PICTURE_SIZE_WIDE, mLiveSnapShotSize);
                    CamLog.m3d(CameraConstants.TAG, "[opticzoom] set KEY_PICTURE_SIZE_WIDE livesnapshot : " + mLiveSnapShotSize);
                }
                if (FunctionProperties.isSupportedLogProfile()) {
                    setLogProfileModeForParam("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)));
                } else if (FunctionProperties.isSupportedHDR10()) {
                    if ("on".equals(getSettingValue(Setting.KEY_HDR10)) && !hasHDR10Limitation()) {
                        z = true;
                    }
                    setHDR10Param(z);
                }
                changeFPSValueSP(true);
            }
        }
    }

    protected void setPreviewLayoutParam() {
        if (ModelProperties.isLongLCDModel()) {
            ListPreference listPref = getListPreference(getVideoSizeSettingKey());
            if (listPref != null) {
                int[] previewSize = Utils.sizeStringToArray(listPref.getExtraInfo(1));
                int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
                int[] screenSize = new int[2];
                double previewRatio = ((double) previewSize[0]) / ((double) previewSize[1]);
                boolean isNotchModel = ModelProperties.getLCDType() == 2;
                int topMargin = -1;
                if (previewRatio <= 2.3d) {
                    screenSize[1] = lcdSize[1];
                    screenSize[0] = (lcdSize[1] * previewSize[0]) / previewSize[1];
                } else if (isNotchModel) {
                    topMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), previewSize[0], previewSize[1], 0);
                    screenSize[0] = (lcdSize[0] - topMargin) - RatioCalcUtil.getNavigationBarHeight(getAppContext());
                    screenSize[1] = (screenSize[0] * previewSize[1]) / previewSize[0];
                } else {
                    screenSize[0] = lcdSize[0];
                    screenSize[1] = (lcdSize[0] * previewSize[1]) / previewSize[0];
                }
                this.mGet.setTextureLayoutParams(screenSize[0], screenSize[1], topMargin);
                if (this.mPreviewFrameLayout != null) {
                    this.mPreviewFrameLayout.setAspectRatio(previewRatio);
                }
                this.mAnimationManager.setSnapshotAniLayout(screenSize[0], screenSize[1], RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), screenSize[0], screenSize[1], 0));
                return;
            }
            return;
        }
        super.setPreviewLayoutParam();
    }

    protected void inAndOutZoomOnRecording(int cameraIdForSwitch, boolean switchingByButton) {
        setManualRecommendMetaDataCallback(false);
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onInAndOutRecording();
        }
        super.inAndOutZoomOnRecording(cameraIdForSwitch, switchingByButton);
    }

    protected void onCameraSwitchedDuringTheRecording(boolean switchingByButton) {
        if (this.mManualControlManager != null) {
            this.mManualControlManager.onInAndOutRecording();
        }
        super.onCameraSwitchedDuringTheRecording(switchingByButton);
        setManualRecommendMetaDataCallback(true);
    }

    protected String getInAndOutZoomRecordingParameter(boolean switchingByButton, int cameraIdForSwitch) {
        String returnValue = super.getInAndOutZoomRecordingParameter(switchingByButton, cameraIdForSwitch) + this.mManualControlManager.getInitialParameters();
        String strFps = this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        int iFps = Utils.parseStringToInteger(strFps);
        if (iFps == 0) {
            return returnValue;
        }
        if (iFps == 1 || iFps == 2) {
            iFps = 30;
        }
        returnValue = returnValue + "video-hfr=" + (iFps >= 50 ? strFps : "off") + ";";
        iFps *= 1000;
        returnValue = returnValue + "preview-fps-range=" + (Integer.toString(iFps) + "," + Integer.toString(iFps)) + ";";
        CamLog.m3d(CameraConstants.TAG, "returnValue = " + returnValue);
        return returnValue;
    }

    public void onDrumVisibilityChanged(int type, boolean isVisible) {
        super.onDrumVisibilityChanged(type, isVisible);
        if (this.mZoomManager != null && this.mManualViewManager != null) {
            if (FunctionProperties.isSupportedLogProfile()) {
                this.mManualViewManager.relocateLogDisplayLUTButton(getOrientationDegree());
            }
            int visibility = isVisible ? 8 : 0;
            if (isVisible || !(checkModuleValidate(128) || !isZoomAvailable() || this.mZoomManager.isZoomBarVisible() || this.mManualViewManager.isAudioControlPanelShowing())) {
                this.mZoomManager.setZoomButtonVisibility(visibility);
            }
        }
    }

    protected void onContentSizeChanged() {
        if (this.mManualViewManager != null) {
            this.mManualViewManager.onContentSizeChanged();
            if (isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
                this.mManualViewManager.updateRatioGuideVisibility(8);
            }
            String contentSize = getContentSize();
            if (contentSize != null) {
                int[] contentSizeArr = Utils.sizeStringToArray(contentSize);
                if (contentSizeArr != null && contentSizeArr.length > 1) {
                    boolean showRatioGuide = ManualUtil.isCinemaSize(this.mGet.getAppContext(), contentSizeArr[0], contentSizeArr[1]);
                    int[] lcdSizeArr = Utils.getLCDsize(this.mGet.getAppContext(), true);
                    int previewWidth = lcdSizeArr[0];
                    int previewHeight = lcdSizeArr[1];
                    if (showRatioGuide) {
                        float contentRatio = ((float) contentSizeArr[0]) / ((float) contentSizeArr[1]);
                        float lcdSizeRatio = ((float) lcdSizeArr[0]) / ((float) lcdSizeArr[1]);
                        if (Float.compare(contentRatio, lcdSizeRatio) > 0) {
                            previewHeight = (int) (((float) lcdSizeArr[0]) * (((float) contentSizeArr[1]) / ((float) contentSizeArr[0])));
                        } else if (Float.compare(contentRatio, lcdSizeRatio) < 0) {
                            previewWidth = (int) (((float) lcdSizeArr[1]) * (((float) contentSizeArr[0]) / ((float) contentSizeArr[1])));
                        }
                    }
                    if (this.mFocusManager != null) {
                        this.mFocusManager.setTouchAreaWindow(previewHeight, previewWidth);
                    }
                } else {
                    return;
                }
            }
            updateManagersAfterContentSizeChanged();
        }
    }

    protected void updateManagersAfterContentSizeChanged() {
        if (this.mBackButtonManager != null) {
            this.mBackButtonManager.setBackButtonLayout();
        }
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setExtraButtonLayout();
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setUpManualModeThumbnail();
        }
    }

    public boolean isFocusEnableCondition() {
        if ((this.mZoomManager == null || !this.mZoomManager.isZoomBarVisible()) && isAFSupported()) {
            return true;
        }
        return false;
    }

    protected void changeFPSValueSP(boolean isChangeRequester) {
        if (this.mParamUpdater != null) {
            String strFps = this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
            int iFps = Utils.parseStringToInteger(strFps);
            if (iFps != 0) {
                if (iFps == 1 || iFps == 2) {
                    iFps = 30;
                }
                setPatialUpdateForManualVideo();
                this.mParamUpdater.setParamValue(ParamConstants.KEY_HFR, iFps >= 50 ? strFps : "off");
                iFps *= 1000;
                String fpsRange = Integer.toString(iFps) + "," + Integer.toString(iFps);
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FPS_RANGE, fpsRange);
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FRAME_RATE, strFps);
                CamLog.m3d(CameraConstants.TAG, "set fpsRange = " + fpsRange + " strFps = " + strFps);
                restoreTrackingAFSetting();
                disableZoomFunction();
                if (this.mManualControlManager != null && this.mCameraDevice != null) {
                    CameraParameters parameters = this.mCameraDevice.getParameters();
                    this.mParamUpdater.updateAllParameters(parameters);
                    this.mManualControlManager.refreshShutterSpeedData(parameters, strFps);
                    if (isChangeRequester) {
                        this.mCameraDevice.setParameters(parameters);
                    } else {
                        this.mManualControlManager.setAELock(parameters, Boolean.valueOf(false));
                    }
                }
            }
        }
    }

    private void disableZoomFunction() {
        if (!isZoomAvailable()) {
            resetZoomValue();
        }
    }

    private void resetZoomValue() {
        if (this.mCameraDevice != null && this.mZoomManager != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                if (!this.mZoomManager.isInitZoomStep(parameters.getZoom())) {
                    showToastConstant(this.mGet.getAppContext().getString(C0088R.string.volume_key_zoom_disable_resolution));
                }
                if (this.mZoomManager.isReadyZoom()) {
                    int zoomValue = this.mZoomManager.getInitZoomStep(this.mCameraId);
                    this.mParamUpdater.setParamValue("zoom", Integer.toString(zoomValue));
                    this.mParamUpdater.updateAllParameters(parameters);
                    updateZoomParam(parameters, zoomValue);
                    setZoomCompensation(parameters);
                    this.mZoomManager.resetZoomLevel();
                }
            }
        }
    }

    protected boolean isInAndOutZoomSupprotedModule() {
        if (FunctionProperties.isSupportedLogProfile() && "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
            return false;
        }
        if (FunctionProperties.isSupportedHDR10() && "on".equals(getSettingValue(Setting.KEY_HDR10))) {
            return false;
        }
        return true;
    }

    protected void restoreTrackingAFSetting() {
        int fps;
        CamLog.m3d(CameraConstants.TAG, "restoreTrackingAFSetting");
        try {
            fps = Integer.parseInt(this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
        } catch (Exception e) {
            fps = 30;
            e.printStackTrace();
        }
        if (fps > 30 || getCameraId() == 2 || isUHDmode()) {
            setSpecificSettingValueAndDisable("tracking-af", "off", false);
            setTrackingFocusState(false);
        } else if (getCameraId() == 0) {
            restoreSettingValue("tracking-af");
            setSettingMenuEnable("tracking-af", true);
        }
    }

    protected String checkPreviewSize(String videoSize) {
        String previewSize = this.mGet.getListPreference(Setting.KEY_MANUAL_VIDEO_SIZE).getExtraInfo(1);
        return previewSize != null ? previewSize : videoSize;
    }

    public void setManualFocus(boolean set) {
        if (this.mFocusManager != null) {
            CamLog.m3d(CameraConstants.TAG, "set manual focus - isSet : " + set);
            if (set) {
                this.mFocusManager.setManualFocus(true);
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        ManualVideoModule.this.mFocusManager.cancelTouchAutoFocus();
                        ManualVideoModule.this.mFocusManager.hideAndCancelAllFocus(false);
                    }
                });
                setSetting("focus-mode", ParamConstants.FOCUS_MODE_MANUAL, false);
                return;
            }
            this.mFocusManager.setManualFocus(false);
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isAFSupported()) {
                setSetting("focus-mode", "auto", false);
                this.mParamUpdater.setParamValue("focus-mode", ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO);
                setCameraFocusMode(ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
        }
    }

    protected void setContinuousFocus(CameraParameters parameters, boolean recordStart) {
        String focusMode = getSettingValue("focus-mode");
        if (ParamConstants.FOCUS_MODE_MANUAL.equals(focusMode)) {
            this.mFocusManager.setManualFocus(true);
            this.mParamUpdater.setParamValue("focus-mode", "normal");
            CamLog.m3d(CameraConstants.TAG, "### setFocusMode-" + focusMode);
            return;
        }
        super.setContinuousFocus(parameters, recordStart);
    }

    public long getRecDurationTime(String recordingFilePath) {
        int iFps = Utils.parseStringToInteger(this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
        long mRecDuration = 0;
        if (iFps != 1 && iFps != 2) {
            return super.getRecDurationTime(recordingFilePath);
        }
        File mRecordingFile = new File(recordingFilePath);
        if (mRecordingFile != null && mRecordingFile.exists()) {
            mRecDuration = (long) FileUtil.getDurationFromFilePath(this.mGet.getAppContext(), recordingFilePath);
            CamLog.m3d(CameraConstants.TAG, "timelapse play duration = " + mRecDuration);
        }
        if (mRecDuration <= 1000) {
            return 1000;
        }
        return mRecDuration;
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
            setCaptureButtonEnable(false, getShutterButtonType());
        }
    }

    protected void afterCommonRequester() {
        super.afterCommonRequester();
        ListPreference listPref = this.mGet.getListPreference(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        if (listPref != null) {
            this.mParamUpdater.addRequester(ParamConstants.KEY_PREVIEW_FRAME_RATE, listPref.getDefaultValue(), false, true);
        }
    }

    public void onPauseBefore() {
        if (!(this.mCameraDevice == null || isPaused())) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                this.mParamUpdater.setParamValue("flash-mode", "off");
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FRAME_RATE, "30");
                if (FunctionProperties.isSupportedLogProfile() && "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
                    setLogProfileMode(false);
                } else if (FunctionProperties.isSupportedHDR10() && "on".equals(getSettingValue(Setting.KEY_HDR10))) {
                    disableWideAngle(false);
                }
                this.mParamUpdater.updateAllParameters(parameters);
                setParameters(parameters);
            }
        }
        if (!(checkModuleValidate(192) || this.mAdvancedFilmManager == null)) {
            this.mAdvancedFilmManager.stopRecorder(false);
        }
        super.onPauseBefore();
        ViewUtil.setPatialUpdate(getAppContext(), true);
    }

    protected void doPhoneStateListenerAction(int state) {
        super.doPhoneStateListenerAction(state);
        if (state == 0) {
            if (!checkModuleValidate(192)) {
                return;
            }
            if (MDMUtil.allowMicrophone()) {
                setCaptureButtonEnable(true, 4);
            } else {
                setCaptureButtonEnable(false, 4);
            }
        } else if (state == 1 && getCameraState() != 6 && getCameraState() != 7) {
            setCaptureButtonEnable(false, 4);
        }
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (keyCode == 66) {
                LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_ENTER);
            } else if (keyCode == 25) {
                LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_VOLUME);
            }
            if (isRecordingState()) {
                onShutterBottomButtonClickListener();
            } else {
                onShutterLargeButtonClicked();
            }
        }
        return true;
    }

    protected void updateFlashParam(CameraParameters parameters, int flashMsg, boolean save) {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "updateFlashParam : flashMsg = " + flashMsg);
        if (this.mCameraDevice != null && parameters != null) {
            this.mCameraDevice.refreshParameters();
            if (parameters != null) {
                boolean z2;
                String flashMode = ParamUtils.getFlashMode(flashMsg);
                if (flashMsg == 51) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                setFlashTorch(parameters, z2, flashMode, true);
                if (flashMsg != 51) {
                    z = false;
                }
                this.mManualFlashOn = z;
                this.mGet.setSetting("flash-mode", flashMode, false);
            }
            if (!checkModuleValidate(128)) {
                this.mLocalParamForZoom = null;
            }
        }
    }

    public void setFlashTorch(CameraParameters parameters, boolean on, String flashMode, boolean setParam) {
        if (parameters != null && CameraDeviceUtils.isFlashSupported(parameters, flashMode)) {
            if (on && "on".equals(flashMode)) {
                setParamUpdater(parameters, "flash-mode", ParamConstants.FLASH_MODE_TORCH);
            } else {
                setParamUpdater(parameters, "flash-mode", "off");
            }
            if (setParam) {
                setParameters(parameters);
            }
        }
    }

    public boolean onVideoShutterClickedBefore() {
        if (this.mHDR10MenuChanged || this.mLogProfileMenuChanged || this.mVideoSteadyChanged) {
            CamLog.m7i(CameraConstants.TAG, "prevent recording while reopen camera HDR " + this.mHDR10MenuChanged + ", LOG " + this.mLogProfileMenuChanged + ", STEADY " + this.mVideoSteadyChanged);
            return false;
        } else if (!super.onVideoShutterClickedBefore()) {
            return false;
        } else {
            if (this.mManualViewManager != null) {
                this.mManualViewManager.setFocusPeaking(16, false);
            }
            return true;
        }
    }

    protected void startRecorder() {
        if (!checkModuleValidate(15) || this.mGet.isModuleChanging() || this.mGet.isCameraChanging()) {
            restoreRecorderToIdle();
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "startRecorder");
        AudioUtil.setUseBuiltInMicForRecording(getAppContext(), false);
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.initVideoTime();
        }
        if (this.mToastManager.isShowing()) {
            this.mToastManager.hideAndResetDisturb(0);
        }
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            showFrameGridView(getGridSettingValue(), false);
        }
        setVideoLimitSize();
        ListPreference listPref = getListPreference(getVideoSizeSettingKey());
        if (listPref == null) {
            CamLog.m5e(CameraConstants.TAG, "KEY_VIDEO_RECORDSIZE listPref is null in startRecorder");
            return;
        }
        setCameraState(5);
        this.mStartRecorderInfo = makeRecorderInfo();
        if (this.mStartRecorderInfo != null) {
            if (FunctionProperties.getSupportedHal() == 2) {
                if (this.mManualViewManager != null) {
                    this.mManualViewManager.setAudioParamOnRecordingStart();
                }
                initRecorder(this.mStartRecorderInfo);
            }
            setParamForVideoRecord(true, listPref);
            this.mIsFileSizeLimitReached = false;
            this.mIsUHDRecTimeLimitWarningDisplayed = false;
            runStartRecorder(true);
            if (this.mManualViewManager != null) {
                this.mManualViewManager.setPanelVisibility(0);
                this.mManualViewManager.updateRatioGuideVisibility(0);
            }
            setRecordingIndicator();
            this.mCameraIdBeforeInAndOutZoom = this.mCameraId;
        }
    }

    protected StartRecorderInfo makeRecorderInfo() {
        String videoSize = getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE);
        String prefFps = getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        String bitrate = getSettingValue(Setting.KEY_MANUAL_VIDEO_BITRATE);
        String audio = getSettingValue(Setting.KEY_MANUAL_VIDEO_AUDIO);
        String hdr10 = getSettingValue(Setting.KEY_HDR10);
        CamLog.m3d(CameraConstants.TAG, "videoSize : " + videoSize);
        double videoFps = prefFps != null ? Double.valueOf(prefFps).doubleValue() : 30.0d;
        int videoBitrate = Integer.valueOf(bitrate).intValue();
        int purpose = 1;
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            purpose = 2;
        }
        if ("on".equals(audio)) {
            purpose |= 8;
        }
        if ("on".equals(hdr10) && !hasHDR10Limitation()) {
            purpose |= 16;
        }
        boolean isCNasLimitation = getCurStorage() == 2 && (isUHDmode() || isFHD60() || isFHDCinema60());
        int storage = isCNasLimitation ? 0 : getCurStorage();
        String dir = (!isCNasLimitation || this.mStorageManager == null) ? getCurDir() : this.mStorageManager.getDir(storage);
        String fileName = makeFileName(1, storage, dir, false, getSettingValue(Setting.KEY_MODE));
        String outFilePath = dir + fileName + ".mp4";
        CamLog.m3d(CameraConstants.TAG, "output file is : " + outFilePath);
        return new StartRecorderInfo(storage, fileName, outFilePath, videoSize, purpose, videoFps, videoBitrate, CameraDeviceUtils.getVideoFlipType(getVideoOrientation(), this.mCameraId, "off".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION)), false));
    }

    protected void updateRecordingUi() {
        super.updateRecordingUi();
        if ("on".equals(getSettingValue(Setting.KEY_HDR10))) {
            this.mRecordingUIManager.showHdr10Text();
        }
        if (this.mZoomManager != null && this.mManualViewManager != null) {
            if (!isZoomAvailable(false) || this.mManualViewManager.isDrumShowing(31) || !checkDoubleCameraAvailableMode(false) || this.mManualViewManager.isAudioControlPanelShowing()) {
                this.mZoomManager.setZoomButtonVisibility(8);
            } else {
                this.mZoomManager.setZoomButtonVisibility(0);
            }
        }
    }

    public void onManualButtonClicked(int type) {
        super.onManualButtonClicked(type);
        if (type == 16 && !checkModuleValidate(128) && this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 4, false);
        }
    }

    protected void setCAFButtonVisibility() {
        super.setCAFButtonVisibility();
        CamLog.m3d(CameraConstants.TAG, "mParamUpdater.getParamValue(KEY_FOCUS_MODE) " + getCameraFocusMode());
        if ("auto".equals(getCameraFocusMode()) && !"on".equals(getSettingValue("tracking-af"))) {
            doTouchAFInRecording();
        }
    }

    protected void stopRecorder() {
        if (!checkModuleValidate(192)) {
            CamLog.m3d(CameraConstants.TAG, "stopRecorder.");
            this.mManualViewManager.setAudioLoopbackInRecording(false);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (ManualVideoModule.this.mManualViewManager != null) {
                        ManualVideoModule.this.mManualViewManager.stopDrawingWave();
                    }
                }
            });
            this.mGet.removePostRunnable(this.mDrawWave);
            if (this.mAdvancedFilmManager != null) {
                this.mAdvancedFilmManager.stopRecorder(false);
            }
            IntentBroadcastUtil.unblockAlarmInRecording(this.mGet.getActivity());
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CamLog.m3d(CameraConstants.TAG, "send CLEAR_SCREEN_DELAY message");
                    ManualVideoModule.this.keepScreenOnAwhile();
                    if (ManualVideoModule.this.mLightFrameManager.isLightFrameMode()) {
                        ManualVideoModule.this.mLightFrameManager.turnOnLightFrame();
                        ManualVideoModule.this.mAdvancedFilmManager.turnOnLightFrame(true);
                    }
                    if (ManualVideoModule.this.mRecordingUIManager != null) {
                        ManualVideoModule.this.mRecordingUIManager.hide();
                        ManualVideoModule.this.mRecordingUIManager.destroyLayout();
                    }
                }
            });
            boolean deleteFile = false;
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateVideoTime(3, SystemClock.uptimeMillis());
                this.mRecordingUIManager.setRecDurationTime((long) getRecCompensationTime());
                if (!this.mRecordingUIManager.checkMinRecTime()) {
                    deleteFile = true;
                }
            }
            this.mIsFileSizeLimitReached = false;
            if (this.mCameraDevice != null) {
                this.mCameraDevice.setForStopRecording();
                this.mCameraDevice.setRecordSurfaceToTarget(false);
            }
            videoRecorderRelease();
            AudioUtil.setAllSoundCaseMute(getAppContext(), false);
            playRecordingSound(false);
            AudioUtil.enableRaM(getAppContext(), true);
            saveVideoFile(deleteFile);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    int callState = TelephonyUtil.getPhoneCallState(ManualVideoModule.this.mGet.getActivity().getApplicationContext());
                    if (ManualVideoModule.this.checkModuleValidate(1) && !TelephonyUtil.phoneIsOffhook(callState) && !ManualVideoModule.this.mGet.isCallAnswering()) {
                        ManualVideoModule.this.setAudioLoopbackOnPreview(true);
                    }
                }
            }, 750);
        }
    }

    public String getRecordingType() {
        if (FunctionProperties.isSupportedLogProfile() && "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
            return CameraConstants.VIDEO_LOG_PROFILE_TYPE;
        }
        if (FunctionProperties.isSupportedHDR10() && "on".equals(getSettingValue(Setting.KEY_HDR10))) {
            return CameraConstants.VIDEO_HDR10_TYPE;
        }
        String frameRate = getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        if (frameRate == null || "not found".equals(frameRate)) {
            return super.getRecordingType();
        }
        int frameRateValue = 30;
        try {
            frameRateValue = Integer.parseInt(frameRate);
        } catch (NumberFormatException e) {
            CamLog.m3d(CameraConstants.TAG, "Exception error occured during converting frame rate value to integer");
        }
        if (frameRateValue == 1 || frameRateValue == 2) {
            return CameraConstants.VIDEO_VIDEO_TIMELAPSE_TYPE;
        }
        if (frameRateValue >= 100) {
            return CameraConstants.VIDEO_SLOMO_TYPE;
        }
        return super.getRecordingType();
    }

    protected void saveVideoFile(boolean deleteFile) {
        renameTempLoopFile();
        String videoSize = getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE).split("@")[0];
        String fileName = VideoRecorder.getFileName();
        String outFilePath = VideoRecorder.getFilePath();
        String uuid = "";
        if (this.mLoopRecorderInfo != null && VideoRecorder.getLoopState() == 3) {
            fileName = this.mLoopRecorderInfo.mFileName;
            outFilePath = this.mLoopRecorderInfo.mOutFilePath;
        }
        if (VideoRecorder.getLoopState() != 0) {
            uuid = VideoRecorder.getUUID();
        }
        String dir = FileUtil.getDirFromFullName(outFilePath);
        setParamForVideoRecord(false, getListPreference(Setting.KEY_MANUAL_VIDEO_SIZE), videoSize);
        startHeatingWarning(false);
        if (outFilePath != null) {
            File outFile = new File(outFilePath);
            if (outFile != null) {
                if (deleteFile) {
                    CamLog.m3d(CameraConstants.TAG, "The recording time is too short, delete the file.");
                    outFile.delete();
                } else {
                    CamLog.m3d(CameraConstants.TAG, "save file path : " + outFilePath);
                    this.mGet.getMediaSaveService().addVideo(this.mGet.getAppContext(), this.mGet.getAppContext().getContentResolver(), dir, fileName, videoSize, getRecDurationTime(outFilePath), outFile.length(), this.mLocationServiceManager.getCurrentLocation(), 1, this.mOnMediaSavedListener, getModeColumn(), uuid);
                }
            }
        }
        if (this.mTempLoopRecorderInfo != null) {
            VideoRecorder.clearEmptyVideoFile(this.mTempLoopRecorderInfo.mOutFilePath, true);
        }
        checkStorage();
    }

    private int getModeColumn() {
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
            return 24;
        }
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_HDR10))) {
            return 25;
        }
        return 0;
    }

    protected void afterStopRecording() {
        if (this.mCameraDevice != null && !changeCameraAfterInAndOutZoom()) {
            releaseParamUpdater();
            addModuleRequester();
            this.mParamUpdater.removeRequester(ParamConstants.KEY_EXPOSURE_COMPENSATION);
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                this.mParamUpdater.setParameters(parameters, "zoom", Integer.toString(parameters.getZoom()));
            }
            setCameraState(1);
            this.mHandler.sendEmptyMessage(5);
            this.mSnapShotChecker.releaseSnapShotChecker();
            doCleanViewAfterStopRecording();
            if (this.mManualViewManager != null && this.mManualViewManager.isDrumShowing(16)) {
                this.mManualViewManager.setFocusPeaking(16, SharedPreferenceUtil.getFocusPeakingEnable(getAppContext()));
            }
            if (this.mFocusManager != null) {
                this.mFocusManager.setTrackingFocusState(false);
            }
            sendLDBIntentOnAfterStopRecording();
            checkThemperatureOnRecording(false);
            enableControls(true);
        }
    }

    protected void updateShutterButtonsOnVideoStopClicked() {
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(14);
        }
    }

    protected boolean doSetParamForStartRecording(CameraParameters parameters, boolean recordStart, ListPreference listPref, String videoSize) {
        checkHeatingConditionForFlashOff();
        setLiveSnapshotSize(parameters, videoSize);
        setMicPath();
        setFlashOnRecord(parameters, true, false);
        this.mParamUpdater.setParameters(parameters, "focus-mode", parameters.getFocusMode());
        this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_EXPOSURE_COMPENSATION, "" + parameters.getExposureCompensation());
        this.mParamUpdater.updateAllParameters(parameters);
        setParameters(parameters);
        setParameterByLGSF(parameters, true);
        if (!(FunctionProperties.getSupportedHal() == 2 || this.mManualViewManager == null)) {
            this.mManualViewManager.setAudioParamOnRecordingStart();
        }
        return true;
    }

    protected boolean doSetParamForStopRecording(CameraParameters parameters, boolean recordStart, String videoSize) {
        resetMicPath();
        int exposure = parameters.getExposureCompensation();
        revertParameterByLGSF(parameters);
        parameters.setExposureCompensation(exposure);
        this.mParamUpdater.updateAllParameters(parameters);
        this.mParamUpdater.setParameters(parameters, "focus-mode", ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO);
        this.mParamUpdater.setParameters(parameters, ParamConstants.MANUAL_FOCUS_STEP, this.mParamUpdater.getParamValue(ParamConstants.MANUAL_FOCUS_STEP));
        if (!isSlowMotionMode()) {
            String curZoomValue = this.mParamUpdater.getParamValue("zoom");
            if (!(curZoomValue == null || "not found".equals(curZoomValue))) {
                parameters.setZoom(Integer.valueOf(curZoomValue).intValue());
            }
        }
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setRecordSurfaceToTarget(false);
        }
        return true;
    }

    protected void setPreviewFpsRange(CameraParameters parameters, boolean isRecordingStarted) {
        String strFps = this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        int iFps = Utils.parseStringToInteger(strFps);
        if (iFps != 0) {
            if (iFps == 1 || iFps == 2) {
                iFps = 30;
            }
            String hfr = iFps >= 50 ? strFps : "off";
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_HFR, hfr);
            iFps *= 1000;
            String fpsRange = Integer.toString(iFps) + "," + Integer.toString(iFps);
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PREVIEW_FPS_RANGE, fpsRange);
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PREVIEW_FRAME_RATE, hfr);
            CamLog.m3d(CameraConstants.TAG, "set fpsRange = " + fpsRange + " strFps = " + strFps);
        }
    }

    protected void setVideoHDR(CameraParameters parameters, boolean isRecordingStarting) {
        if (!FunctionProperties.isSupportedLogProfile() || !"on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
            if (!FunctionProperties.isSupportedHDR10() || !"on".equals(getSettingValue(Setting.KEY_HDR10))) {
                int iFps = Utils.parseStringToInteger(this.mGet.getCurSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
                if (iFps != 0) {
                    if (iFps == 1 || iFps == 2) {
                        iFps = 30;
                    }
                    if (iFps >= 50) {
                        CamLog.m3d(CameraConstants.TAG, "Video HDR will be OFF because frame rate is : " + iFps);
                        this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_VIDEO_HDR_MODE, "off");
                        return;
                    }
                    super.setVideoHDR(parameters, isRecordingStarting);
                }
            }
        }
    }

    protected void updateSteadyCamParam(int type) {
        super.updateSteadyCamParam(type);
        if (this.mCameraDevice != null && isAvailableSteadyCam()) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            String value = type == 75 ? "off" : "on";
            setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, value, true);
            lockSteadyCamSetting();
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_STEADY_CAM, value);
            setupPreview(parameters);
        }
    }

    protected void updateRecordingTime() {
        if (this.mRecordingUIManager != null) {
            this.mUpdateTimer++;
            if (this.mUpdateTimer % 4 == 0) {
                long milliseconds = (long) Math.round(((float) (SystemClock.uptimeMillis() - this.mRecordingUIManager.getVideoTime(1))) / 1.0f);
                if (getCameraState() == 6) {
                    this.mRecordingUIManager.updateVideoTime(4, milliseconds);
                    updateUIRecordingTime(milliseconds);
                    long seconds = (long) Math.round(((float) milliseconds) / 1000.0f);
                    if (Long.compare(this.mManualRecordingTime, seconds) != 0) {
                        CamLog.m3d(CameraConstants.TAG, "Recording time : " + seconds);
                        this.mManualRecordingTime = seconds;
                    }
                    checkCacheSize();
                }
                this.mUpdateTimer = 0;
            }
            if (getCameraState() == 6) {
                this.mGet.postOnUiThread(this.mUpdateRecordingTime, TIME_TO_UPDATE_TIMER);
                this.mGet.postOnUiThread(this.mDrawWave, 0);
            }
        }
    }

    protected void updateUIRecordingTime(long milliseconds) {
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.updateUIManualRecordingTime(this.mIsFileSizeLimitReached, milliseconds, isVideoAttachMode(), getCameraState());
        }
    }

    protected boolean checkToUseFilmEffect() {
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.hasFilmLimitationForManualVideo()) {
            return super.checkToUseFilmEffect();
        }
        return false;
    }

    protected boolean isEnableManualView() {
        return (isAttachIntent() || isTimerShotCountdown()) ? false : true;
    }

    public void setTrackingFocusState(boolean enable) {
        if (this.mFocusManager != null) {
            this.mFocusManager.setTrackingFocusState(enable);
        }
    }

    public void onFrameRateListRefreshed(String previous, String next) {
        if (previous != null && next != null && !previous.equals(next)) {
            CamLog.m3d(CameraConstants.TAG, "previous = " + previous + " / changed = " + next);
            if (("120".equals(previous) || "120".equals(next) || "1".equals(previous) || "1".equals(next) || "2".equals(previous) || "2".equals(next)) && this.mManualViewManager != null) {
                this.mManualViewManager.onFrameRateChanged(previous, next);
            }
        }
    }

    public boolean doBackKey() {
        if (this.mManualViewManager != null && this.mManualViewManager.isAudioControlPanelShowing()) {
            this.mManualViewManager.audioBtnClicked(false);
            return true;
        } else if (!this.mHDR10MenuChanged && !this.mLogProfileMenuChanged && !this.mVideoSteadyChanged && !this.mPictureOrVideoSizeChanged) {
            return super.doBackKey();
        } else {
            CamLog.m7i(CameraConstants.TAG, "prevent back key while reopen camera HDR " + this.mHDR10MenuChanged + ", LOG " + this.mLogProfileMenuChanged + ", STEADY " + this.mVideoSteadyChanged + ", mPictureOrVideoSizeChanged " + this.mPictureOrVideoSizeChanged);
            return true;
        }
    }

    public boolean onShowMenu(int menuType) {
        if (this.mManualViewManager != null && this.mManualViewManager.isAudioControlPanelShowing()) {
            this.mManualViewManager.audioBtnClicked(false);
        }
        return super.onShowMenu(menuType);
    }

    protected boolean isAvailableSteadyCam() {
        String frameRate = getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        if (frameRate == null || "not found".equals(frameRate)) {
            return false;
        }
        try {
            int frameRateValue = Integer.parseInt(frameRate);
            if (isUHDmode() || frameRateValue < 24 || frameRateValue > 30) {
                CamLog.m3d(CameraConstants.TAG, "frameRateValue = " + frameRateValue + ", Steady cam not available");
                return false;
            }
            CamLog.m3d(CameraConstants.TAG, "frameRateValue = " + frameRateValue + ", Steady cam available");
            return true;
        } catch (NumberFormatException e) {
            CamLog.m3d(CameraConstants.TAG, "Exception error occured during converting frame rate value to integer");
            return false;
        }
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        if (this.mManualViewManager != null) {
            this.mManualViewManager.audioBtnClicked(false);
            this.mManualViewManager.setPanelVisibility(4);
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot() || this.mManualViewManager == null) {
            return false;
        }
        this.mManualViewManager.setPanelVisibility(0);
        return true;
    }

    public boolean isOpticZoomSupported(String specificMode) {
        if (!isZoomAvailable()) {
            return false;
        }
        if (FunctionProperties.isSupportedLogProfile() && "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG))) {
            return false;
        }
        if (FunctionProperties.isSupportedHDR10() && "on".equals(getSettingValue(Setting.KEY_HDR10))) {
            return false;
        }
        return super.isOpticZoomSupported(specificMode);
    }

    protected void startPreview(CameraParameters params, SurfaceTexture surfaceTexture) {
        CamLog.m7i(CameraConstants.TAG, "startPreview");
        if (FunctionProperties.getSupportedHal() == 2 && getCameraState() != 8) {
            if (getFilmState() >= 3 || getFilmState() == 2) {
                VideoRecorder.releasePersistentSurface();
            }
            String settingVideoSize = getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE);
            String mimeType = (!"on".equals(getSettingValue(Setting.KEY_HDR10)) || hasHDR10Limitation()) ? "video/avc" : "video/hevc";
            VideoRecorder.createPersistentSurface(settingVideoSize, 0, mimeType);
        }
        super.startPreview(params, surfaceTexture, true);
        if (FunctionProperties.getSupportedHal() == 2) {
            setEVFromControlData();
        }
    }

    public boolean isLiveSnapshotSupported() {
        return ("on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG)) || "on".equals(getSettingValue(Setting.KEY_HDR10)) || Integer.parseInt(getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE)) > 60) ? false : true;
    }

    public void playRecordingSound(boolean start) {
        super.playRecordingSound(start);
        if (FunctionProperties.getSupportedHal() == 2 && start) {
            AudioUtil.setSleepForRecordSound();
        }
    }

    private void setEVFromControlData() {
        String evValue = "0";
        if (!(this.mManualControlManager == null || this.mManualControlManager.getManualData(2) == null)) {
            String data = this.mManualControlManager.getManualData(2).getValue();
            evValue = data != null ? data : "0";
        }
        if (this.mCameraDevice != null) {
            CameraParameters parameter = this.mCameraDevice.getParameters();
            if (parameter != null) {
                parameter.setExposureCompensation(Integer.parseInt(evValue));
                this.mCameraDevice.setParameters(parameter);
            }
        }
    }

    protected boolean replaceZoomManager(boolean isSingleZoom) {
        boolean z = isSingleZoom || "on".equals(getSettingValue(Setting.KEY_HDR10)) || "on".equals(getSettingValue(Setting.KEY_MANUAL_VIDEO_LOG));
        return super.replaceZoomManager(z);
    }

    protected void restoreSteadyCamSetting() {
        if (!this.mVideoSteadyChanged) {
            super.restoreSteadyCamSetting();
        }
    }
}
