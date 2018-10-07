package com.lge.camera.app.ext;

import android.net.Uri;
import android.support.p000v4.view.PointerIconCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.file.MediaSaveService.OnLocalSaveByTimeListener;
import com.lge.camera.managers.TimerManager.TimerType;
import com.lge.camera.managers.TimerManager.TimerTypeFlashJumpCut;
import com.lge.camera.managers.ext.FlashJumpCutGifManager;
import com.lge.camera.managers.ext.FlashJumpcutViewManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import java.util.Timer;
import java.util.TimerTask;

public class FlashJumpCutRearModule extends DefaultCameraModule {
    protected FlashJumpCutGifManager mFlashJumpCutGifManager = null;
    protected int mFlashJumpCutJpegDegree = 0;
    private HandlerRunnable mFlashJumpCutRunnable = null;
    protected Timer mFlashJumpCutTimer = null;
    protected FlashJumpcutViewManager mFlashJumpcutViewManager = null;
    private boolean mIsStartedRunnable = false;
    private String mSavedFilePath = null;

    /* renamed from: com.lge.camera.app.ext.FlashJumpCutRearModule$2 */
    class C03762 extends TimerTask {
        C03762() {
        }

        public void run() {
            FlashJumpCutRearModule.this.mGet.postOnUiThread(FlashJumpCutRearModule.this.mFlashJumpCutRunnable);
        }
    }

    public FlashJumpCutRearModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (this.mFlashJumpCutGifManager == null) {
            CamLog.m3d(CameraConstants.TAG, "New mFlashJumpCutGifManager.");
            this.mFlashJumpCutGifManager = new FlashJumpCutGifManager(this);
        }
        if (this.mFlashJumpcutViewManager == null) {
            CamLog.m3d(CameraConstants.TAG, "New mFlashJumpcutManager.");
            this.mFlashJumpcutViewManager = new FlashJumpcutViewManager(this);
        }
        this.mManagerList.add(this.mFlashJumpcutViewManager);
        this.mManagerList.add(this.mFlashJumpCutGifManager);
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (isGIFEncoding()) {
            return true;
        }
        return super.onShutterUp(keyCode, event);
    }

    protected void showCommandArearUI(boolean show) {
        if (show || !this.mSnapShotChecker.checkMultiShotState(2)) {
            super.showCommandArearUI(show);
        }
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        this.mGet.backupSetting(Setting.KEY_TIMER, getSettingValue(Setting.KEY_TIMER));
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        setSpecificSettingValueAndDisable(Setting.KEY_TIMER, "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    public boolean onShutterDown(int keyCode, KeyEvent event) {
        if (isGIFEncoding()) {
            return true;
        }
        return super.onShutterDown(keyCode, event);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
        if (this.mFlashJumpcutViewManager != null) {
            this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(true, false);
        }
    }

    public void init() {
        super.init();
        if (this.mFlashJumpCutGifManager == null) {
            CamLog.m3d(CameraConstants.TAG, "[jumpcut] New FlashJumpCutGifManager.");
            this.mFlashJumpCutGifManager = new FlashJumpCutGifManager(this);
        }
        if (this.mFlashJumpcutViewManager == null) {
            CamLog.m3d(CameraConstants.TAG, "[jumpcut] New mFlashJumpcutManager.");
            this.mFlashJumpcutViewManager = new FlashJumpcutViewManager(this);
            this.mSnapShotChecker.initFlashJumpCutCount();
        }
        this.mFlashJumpCutRunnable = new HandlerRunnable(this) {
            public void handleRun() {
                if (FlashJumpCutRearModule.this.checkModuleValidate(1) && FlashJumpCutRearModule.this.mSnapShotChecker.checkMultiShotState(2) && !FlashJumpCutRearModule.this.isJumpCutMax() && !FlashJumpCutRearModule.this.mIsStartedRunnable && !FlashJumpCutRearModule.this.mTimerManager.isTimerShotCountdown() && !FlashJumpCutRearModule.this.mIsPreviewCallbackWaiting) {
                    FlashJumpCutRearModule.this.startTimerShotByJumpCut();
                }
            }
        };
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        restoreFlashSetting();
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue(Setting.KEY_RAW_PICTURE);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        this.mGet.restoreBackupSetting(Setting.KEY_TIMER, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (this.mFlashJumpcutViewManager == null) {
            this.mFlashJumpcutViewManager = new FlashJumpcutViewManager(this);
        }
        if (this.mFlashJumpCutGifManager == null) {
            this.mFlashJumpCutGifManager = new FlashJumpCutGifManager(this);
        }
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mSnapShotChecker.getFlashJumpCutCount() != 0) {
            return super.onCameraShutterButtonClicked();
        }
        doFlashJumpCut();
        return true;
    }

    public boolean isJumpCutMax() {
        return this.mSnapShotChecker.getFlashJumpCutCount() >= SharedPreferenceUtil.getJumpcutShotCount(getAppContext());
    }

    protected void doFlashJumpCut() {
        if (this.mQuickButtonManager == null || this.mCaptureButtonManager == null || this.mReviewThumbnailManager == null) {
            this.mSnapShotChecker.setSnapShotState(0);
        } else if (this.mFlashJumpCutTimer == null) {
            CamLog.m3d(CameraConstants.TAG, "[jumpcut] doJumpCut.");
            this.mSnapShotChecker.removeMultiShotState(8);
            this.mSnapShotChecker.setMultiShotState(2);
            AudioUtil.setAudioFocus(getAppContext(), true);
            this.mCaptureButtonManager.changeButtonByMode(11);
            doActionForJumpCut(true, true);
            TimerTask intervalShot = new C03762();
            this.mFlashJumpCutTimer = new Timer("timer_jumpCut_check");
            this.mFlashJumpCutTimer.schedule(intervalShot, 0, 100);
        }
    }

    protected void stopFlashJumpCut() {
        CamLog.m3d(CameraConstants.TAG, "[jumpcut] stopFlashJumpCut.");
        if (this.mWaitSavingDialogType != 4) {
            this.mSnapShotChecker.removeMultiShotState(2);
            this.mSnapShotChecker.releaseSnapShotChecker();
        }
        this.mIsStartedRunnable = false;
        this.mTimerManager.stopTimerShot();
        if (!(this.mFlashJumpCutGifManager == null || isGIFEncoding())) {
            CamLog.m7i(CameraConstants.TAG, "[gif] isGIFEncoding ? " + isGIFEncoding() + ", isGifListEmpty ? " + this.mFlashJumpCutGifManager.isGifListEmpty() + ", getFlashJumpCutCount : " + this.mSnapShotChecker.getFlashJumpCutCount());
            if (!this.mFlashJumpCutGifManager.isGifListEmpty() || this.mSnapShotChecker.getFlashJumpCutCount() == 1) {
                this.mFlashJumpCutGifManager.setGifShotCount(this.mSnapShotChecker.getFlashJumpCutCount());
                this.mFlashJumpCutGifManager.executeGifMake();
            } else {
                this.mFlashJumpCutGifManager.resetGifFileListArray();
                FileManager.deleteAllFileInPath(getAppContext(), getTempDir());
            }
        }
        if (this.mFlashJumpCutTimer != null) {
            this.mFlashJumpCutTimer.cancel();
            this.mFlashJumpCutTimer.purge();
            this.mFlashJumpCutTimer = null;
        }
        if (this.mWaitSavingDialogType == 4) {
            CamLog.m11w(CameraConstants.TAG, "[jumpcut] during waiting GIF dialog. Restart GIF maker.");
        } else {
            CamLog.m3d(CameraConstants.TAG, "[jumpcut] Normally GIF making.");
            this.mSnapShotChecker.setIntervalShotState(8);
            this.mSnapShotChecker.initFlashJumpCutCount();
        }
        if (!"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
            AudioUtil.setAudioFocus(getAppContext(), false);
        }
        doActionForJumpCut(false, false);
        setQuickClipIcon(false, false);
        onTakePictureAfter();
    }

    protected void doActionForJumpCut(boolean isJumpCutProgress, boolean useAnimation) {
        if (this.mQuickButtonManager == null || this.mCaptureButtonManager == null || this.mAdvancedFilmManager == null || this.mFlashJumpCutGifManager == null) {
            this.mSnapShotChecker.setPictureCallbackState(0);
            return;
        }
        CamLog.m11w(CameraConstants.TAG, "[jumpcut] doActionForJumpCut. isJumpCutProgress ? " + isJumpCutProgress);
        if (isJumpCutProgress) {
            boolean z;
            String str = CameraConstants.TAG;
            StringBuilder append = new StringBuilder().append("[jumpcut] isAvailableTakePictureBefore ? ").append(this.mSnapShotChecker.isAvailableTakePictureBefore()).append(", getLGSFParamState is LGSF_PARAM_IDLE ? ").append(this.mSnapShotChecker.getLGSFParamState() == 0).append(", !isFastShotAvailable(S2S_CHECK_ALL) ? ");
            if (isFastShotAvailable(3)) {
                z = false;
            } else {
                z = true;
            }
            CamLog.m3d(str, append.append(z).toString());
            showDoubleCamera(false);
            this.mQuickButtonManager.hide(false, false, false);
            if (this.mAdvancedFilmManager.isShowingFilmMenu()) {
                this.mAdvancedFilmManager.showFilmMenu(false, 3, true, true, 0, true);
                this.mExtraPrevewUIManager.changeButtonState(0, false);
            }
            sBurstShotCount = 0;
            this.mFlashJumpCutGifManager.createGifFileListArray();
            this.mGifManager.setGifVisibility(false);
            this.mGifManager.setGifVisibleStatus(false);
            if (this.mZoomManager != null) {
                this.mZoomManager.stopDrawingExceedsLevel();
            }
            if (this.mFlashJumpcutViewManager != null) {
                this.mFlashJumpcutViewManager.showJumpCutInitGuideText(false);
            }
            hideZoomBar();
            setQuickClipIcon(true, false);
            if (this.mFocusManager != null) {
                if (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock()) {
                    this.mFocusManager.setAEControlBarEnable(false);
                    this.mFocusManager.setEVshutterButtonEnable(false);
                }
                this.mFocusManager.hideAllFocus(true);
                this.mFocusManager.registerEVCallback(false, false);
                this.mFocusManager.setManualFocusButtonEnable(false);
            }
            if (this.mManualFocusManager != null && isManualFocusModeEx()) {
                this.mManualFocusManager.setManualFocusViewEnable(false);
            }
            this.mCaptureButtonManager.setShutterLargeButtonVisibility(0, getShutterButtonType(), useAnimation, true);
            return;
        }
        if (this.mCaptureButtonManager.getShutterButtonMode(4) == 11) {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
        this.mCaptureButtonManager.setShutterLargeButtonVisibility(0, getShutterButtonType(), useAnimation, true);
        doShowDoubleCamera();
        if (this.mFlashJumpcutViewManager != null) {
            this.mFlashJumpcutViewManager.hideFlashJumpCutCountGuide();
            this.mFlashJumpcutViewManager.setShotSelectViewVisibility(true, false);
            if (isInitGuideShowingCondition()) {
                this.mFlashJumpcutViewManager.showJumpCutInitGuideText(true);
            }
        }
        if (!(isModuleChanging() || !checkModuleValidate(8) || this.mGet.isSettingMenuVisible())) {
            this.mQuickButtonManager.show(false, true, true);
        }
        showExtraPreviewUI(true, false, false, false);
        if (this.mFocusManager != null && (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock())) {
            this.mFocusManager.setAEControlBarEnable(true);
            this.mFocusManager.setEVshutterButtonEnable(true);
        }
        if (!(this.mFocusManager == null || this.mManualFocusManager == null || !isManualFocusModeEx())) {
            this.mFocusManager.setManualFocusButtonEnable(true);
            this.mManualFocusManager.setManualFocusViewEnable(true);
        }
        if (!(this.mFocusManager == null || isManualFocusModeEx())) {
            this.mFocusManager.registerEVCallback(true, true);
        }
        this.mBackButtonManager.setBackButton(false);
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera, boolean updateIntervalShotThumbnail) {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "### [jumpcut] onPictureTakenCallback - start");
        if (this.mCameraDevice == null || this.mSnapShotChecker.isNotTaking()) {
            CamLog.m3d(CameraConstants.TAG, "[jumpcut] onPictureTakenCallbackAfter return : mCameraDevice is " + this.mCameraDevice + ", SNAPSHOT_READY state.");
            return;
        }
        this.mIsStartedRunnable = false;
        if (this.mFocusManager != null) {
            this.mFocusManager.registerEVCallback(true, true);
        }
        this.mSnapShotChecker.removeReleaser();
        this.mSnapShotChecker.setPictureCallbackState(2);
        if (this.mSnapShotChecker.checkMultiShotState(2)) {
            this.mSnapShotChecker.increaseFlashJumpCutCount();
            if (isJumpCutMax()) {
                CamLog.m7i(CameraConstants.TAG, "[jumpCut] isJumpCutMax. Stop interval shot.");
                stopFlashJumpCut();
            }
            saveImage(data, extraExif);
        } else if (this.mNeedProgressDuringCapture > 0) {
            this.mNeedProgressDuringCapture = 0;
            showSavingDialog(false, 0);
            this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
            this.mWaitSavingDialogType = 0;
        }
        if (!(ModelProperties.isMTKChipset() && Exif.getFlash(data) == 1)) {
            z = false;
        }
        this.mIsMTKFlashFired = z;
        this.mGet.removePostRunnable(this.mOnPictureTakenCallbackAfter);
        if (this.mSnapShotChecker.isAvailableNightAndFlash() && isFastShotAvailable(3)) {
            this.mGet.postOnUiThread(this.mOnPictureTakenCallbackAfter, 300);
        } else {
            onPictureCallbackAfterRun(false, false);
        }
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setAfterShot();
        }
        CamLog.m3d(CameraConstants.TAG, "### onPictureTakenCallback - end. getFlashJumpCutCount : " + this.mSnapShotChecker.getFlashJumpCutCount());
    }

    protected void setParameterBeforeTakePicture(CameraParameters param, boolean useBurst, boolean forceUpdate) {
        CamLog.m3d(CameraConstants.TAG, "setParameterBeforeTakePicture - start");
        if (param != null && forceUpdate) {
            try {
                if (this.mSnapShotChecker.getFlashJumpCutCount() == 0) {
                    this.mFlashJumpCutJpegDegree = setPictureOrientation(param);
                }
                param = this.mFlipManager.setPictureFlipParam(this.mCameraId, param, this.mFlashJumpCutJpegDegree);
                if (!useBurst && checkModuleValidate(128)) {
                    setParamUpdater(param, "flash-mode", getSettingValue("flash-mode"));
                }
                setFilmParameters(param, this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR));
                String signatureValue = (isHALSignatureCaptureMode() && isSignatureEnableCondition()) ? "on" : "off";
                CamLog.m3d(CameraConstants.TAG, "[jumpcut] set signature param value, value = " + signatureValue);
                param.set(ParamConstants.KEY_SIGNATURE_ENABLE, signatureValue);
                if (this.mSnapShotChecker.getFlashJumpCutCount() != 0) {
                    param = this.mCameraDevice.getParameters();
                }
                setParameterByLGSF(param, getShotMode(), false);
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "setParameterBeforeTakePicture is failed : ", e);
            }
        }
    }

    protected void saveImage(final byte[] data, final byte[] extraExif) {
        CamLog.m3d(CameraConstants.TAG, "saveImage - start");
        if (this.mGet.getMediaSaveService() != null && !this.mGet.getMediaSaveService().isQueueFull()) {
            this.mGet.getMediaSaveService().processLocal(new OnLocalSaveByTimeListener() {
                public void onPreExecute() {
                }

                public void onPostExecute(Uri uri) {
                    FlashJumpCutRearModule.this.doSaveImagePostExecute(uri);
                }

                public Uri onLocalSave(String markTime) {
                    String dir = FlashJumpCutRearModule.this.getTempDir();
                    String fileName = FlashJumpCutRearModule.this.access$700(true, dir, markTime);
                    FlashJumpCutRearModule.this.mSavedFilePath = dir + fileName + ".jpg";
                    CamLog.m3d(CameraConstants.TAG, "[jumpCut][gif] -filename- dir = " + dir + ", markTime = " + markTime + ", mSavedFilePath = " + FlashJumpCutRearModule.this.mSavedFilePath);
                    if (FlashJumpCutRearModule.this.mFlashJumpCutGifManager != null) {
                        FlashJumpCutRearModule.this.mFlashJumpCutGifManager.addFilePath(FlashJumpCutRearModule.this.mSavedFilePath);
                    }
                    return FlashJumpCutRearModule.this.access$900(data, extraExif, dir, fileName);
                }
            }, FileNamer.get().getTakeTime(getSettingValue(Setting.KEY_MODE)));
        }
    }

    public void takePictureByTimer(int type) {
        if (type == 3) {
            this.mSnapShotChecker.removeMultiShotState(12);
            if (this.mFlashJumpcutViewManager != null) {
                this.mFlashJumpcutViewManager.hideFlashJumpCutCountGuide();
            }
            takePicture();
        }
    }

    private void startTimerShotByJumpCut() {
        CamLog.m3d(CameraConstants.TAG, "[jumpcut][timer] startTimerShotByJumpCut.");
        this.mIsStartedRunnable = true;
        checkPictureCallbackRunnable(true);
        keepScreenOnAwhile();
        doStartTimerShot(new TimerTypeFlashJumpCut());
    }

    protected void onCameraSwitchingStart() {
        this.mGet.setCameraChangingOnFlashJumpCut(true);
        super.onCameraSwitchingStart();
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        super.setContentFrameLayoutParam(params);
        if (this.mFlashJumpcutViewManager != null) {
            int previewTopMargin;
            int previewStartMargin = Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()) ? params.getMarginStart() : params.topMargin;
            if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                previewTopMargin = params.topMargin;
            } else {
                previewTopMargin = params.getMarginStart();
            }
            this.mFlashJumpcutViewManager.setInitGuideTextLayout(params.width, params.height, previewStartMargin, previewTopMargin);
        }
    }

    protected void startTimerShotByType(TimerType timerType, String timerValue) {
        if ((timerType instanceof TimerTypeFlashJumpCut) && this.mFlashJumpcutViewManager != null) {
            CamLog.m7i(CameraConstants.TAG, "[jumpcut][timer] getFlashJumpCutCount() : " + this.mSnapShotChecker.getFlashJumpCutCount());
            this.mFlashJumpcutViewManager.showFlashJumpCutCountGuide(this.mSnapShotChecker.getFlashJumpCutCount());
        }
        super.startTimerShotByType(timerType, timerValue);
    }

    public void onShutterLargeButtonClicked() {
        if (!checkModuleValidate(15) || this.mGet.isAnimationShowing() || SystemBarUtil.isSystemUIVisible(getActivity())) {
            CamLog.m5e(CameraConstants.TAG, "[Jumpcut] return!");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "[Jumpcut] shutter large clicked. getShutterButtonMode(SHUTTER_BTN_LARGE) ? " + this.mCaptureButtonManager.getShutterButtonMode(4));
        switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
            case 11:
                CamLog.m3d(CameraConstants.TAG, "[jumpCut] MULTISHOT_INTERVAL ? " + this.mSnapShotChecker.checkMultiShotState(2) + ", VALIDATE_CAPTURE_PROGRESS : " + (!checkModuleValidate(16)));
                if (!this.mSnapShotChecker.checkMultiShotState(2)) {
                    return;
                }
                if (isTimerShotCountdown() || this.mGet.checkModuleValidate(48)) {
                    stopFlashJumpCut();
                    return;
                } else {
                    CamLog.m3d(CameraConstants.TAG, "[jumpcut] stopFlashJumpCut return! because of capture progress.");
                    return;
                }
            case 12:
                onShutterBottomButtonClickListener();
                return;
            default:
                super.onShutterLargeButtonClicked();
                return;
        }
    }

    public int getShutterButtonType() {
        return 4;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mFlashJumpCutRunnable = null;
        this.mFlashJumpcutViewManager = null;
        this.mFlashJumpCutGifManager = null;
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        stopFlashJumpCut();
        this.mGet.removePostRunnable(this.mFlashJumpCutRunnable);
    }

    public boolean doBackKey() {
        if (this.mFlashJumpcutViewManager != null && this.mFlashJumpcutViewManager.isDrawerOpen()) {
            this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(true, false);
            return true;
        } else if (!this.mIsStartedRunnable || !this.mSnapShotChecker.checkMultiShotState(2)) {
            if (isTimerShotCountdown() && this.mFlashJumpcutViewManager != null) {
                this.mFlashJumpcutViewManager.hideFlashJumpCutCountGuide();
            }
            return super.doBackKey();
        } else if (isTimerShotCountdown() || this.mGet.checkModuleValidate(48)) {
            stopFlashJumpCut();
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "[jumpcut] doBackKey return! because of capture progress.");
            return true;
        }
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (this.mFlashJumpcutViewManager != null) {
            this.mFlashJumpcutViewManager.setGuideTextLayoutParam();
        }
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL) || this.mFlashJumpcutViewManager == null) {
            return true;
        }
        this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(true, false);
        if (!isInitGuideShowingCondition()) {
            return true;
        }
        this.mFlashJumpcutViewManager.showJumpCutInitGuideText(true);
        return true;
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        if (this.mFlashJumpcutViewManager != null) {
            this.mFlashJumpcutViewManager.setShotSelectViewVisibility(false, false);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mFlashJumpcutViewManager != null) {
            if (isMenuShowing(CameraConstants.MENU_TYPE_ALL) && (event.getAction() == 1 || event.getAction() == 3)) {
                this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(false, false);
            }
            if (this.mFlashJumpcutViewManager.isDrawerOpen() && event.getAction() == 1) {
                this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(true, false);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isInitGuideShowingCondition() {
        boolean z;
        String str = CameraConstants.TAG;
        StringBuilder append = new StringBuilder().append("[JumpCut][init] During INTERVAL ? ").append(this.mSnapShotChecker.checkMultiShotState(2)).append(", isSelfieOptionVisible ? ");
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isSelfieOptionVisible()) {
            z = false;
        } else {
            z = true;
        }
        append = append.append(z).append(", menu showing ? ");
        if (isMenuShowing(PointerIconCompat.TYPE_ZOOM_OUT) || isOpeningSettingMenu()) {
            z = true;
        } else {
            z = false;
        }
        CamLog.m3d(str, append.append(z).append(", isAeControlBarShowing : ").append(isAeControlBarShowing()).append(", isZoomBarVisible : ").append(isZoomBarVisible()).toString());
        if (this.mSnapShotChecker.checkMultiShotState(2) || ((this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isSelfieOptionVisible()) || isMenuShowing(PointerIconCompat.TYPE_ZOOM_OUT) || isOpeningSettingMenu() || isAeControlBarShowing() || isZoomBarVisible())) {
            return false;
        }
        return true;
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (this.mFlashJumpcutViewManager != null) {
            this.mFlashJumpcutViewManager.showJumpCutInitGuideText(false);
        }
    }

    public void onZoomHide() {
        super.onZoomHide();
        if (this.mFlashJumpcutViewManager != null && isInitGuideShowingCondition()) {
            this.mFlashJumpcutViewManager.showJumpCutInitGuideText(true);
        }
    }

    public void onShowAEBar() {
        super.onShowAEBar();
        if (this.mFlashJumpcutViewManager != null) {
            this.mFlashJumpcutViewManager.showJumpCutInitGuideText(false);
        }
    }

    public void onHideAEBar() {
        super.onHideAEBar();
        if (this.mFlashJumpcutViewManager != null && isInitGuideShowingCondition()) {
            this.mFlashJumpcutViewManager.showJumpCutInitGuideText(true);
        }
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        if (this.mFlashJumpcutViewManager != null) {
            this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(false, false);
            this.mFlashJumpcutViewManager.showJumpCutInitGuideText(false);
        }
        return true;
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot()) {
            return false;
        }
        if (this.mFlashJumpcutViewManager == null) {
            return true;
        }
        this.mFlashJumpcutViewManager.setShotSelectViewVisibility(true, false);
        if (!isInitGuideShowingCondition()) {
            return true;
        }
        this.mFlashJumpcutViewManager.showJumpCutInitGuideText(true);
        return true;
    }

    protected void completeHideQuickview() {
        super.completeHideQuickview();
        if (this.mFlashJumpcutViewManager != null && isInitGuideShowingCondition()) {
            this.mFlashJumpcutViewManager.showJumpCutInitGuideText(true);
        }
    }

    protected void onTakePictureAfter() {
        this.mSnapShotChecker.setPictureCallbackState(3);
        if (!this.mSnapShotChecker.checkMultiShotState(2)) {
            LdbUtil.setMultiShotState(0);
            super.access$100();
            if (this.mFlashJumpcutViewManager != null) {
                this.mFlashJumpcutViewManager.setJumpcutBtnEnable(true);
                this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(true, false);
            } else {
                return;
            }
        }
        setCaptureButtonEnable(true, getShutterButtonType());
    }

    protected void doSaveImagePostExecute(Uri uri) {
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
    }

    public void onQueueStatus(int count) {
        CamLog.m3d(CameraConstants.TAG, "onQueueStatus : current queue count = " + count + " Progress type = " + this.mNeedProgressDuringCapture + ", mWaitSavingDialogType : " + this.mWaitSavingDialogType);
        if (this.mNeedProgressDuringCapture > 0) {
            this.mNeedProgressDuringCapture = 0;
            showSavingDialog(false, 0);
            this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    switch (FlashJumpCutRearModule.this.mWaitSavingDialogType) {
                        case 4:
                            FlashJumpCutRearModule.this.mWaitSavingDialogType = 0;
                            FlashJumpCutRearModule.this.stopFlashJumpCut();
                            return;
                        default:
                            FlashJumpCutRearModule.this.mWaitSavingDialogType = 0;
                            return;
                    }
                }
            });
            if (this.mIsNeedFinishAfterSaving) {
                this.mIsNeedFinishAfterSaving = false;
                getActivity().finish();
            }
        } else if (count == 0) {
            this.mNeedProgressDuringCapture = 0;
            showSavingDialog(false, 0);
            this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    switch (FlashJumpCutRearModule.this.mWaitSavingDialogType) {
                        case 1:
                            if (FlashJumpCutRearModule.this.checkModuleValidate(1)) {
                                FlashJumpCutRearModule.this.mGet.getActivity().finish();
                                break;
                            }
                            break;
                        case 2:
                            if (FlashJumpCutRearModule.this.mReviewThumbnailManager != null) {
                                FlashJumpCutRearModule.this.mReviewThumbnailManager.launchGallery(0);
                                break;
                            }
                            break;
                        case 4:
                            FlashJumpCutRearModule.this.mWaitSavingDialogType = 0;
                            FlashJumpCutRearModule.this.stopFlashJumpCut();
                            return;
                    }
                    FlashJumpCutRearModule.this.mWaitSavingDialogType = 0;
                }
            });
        }
    }

    protected void onTakePictureBefore() {
        CamLog.m3d(CameraConstants.TAG, "[jumpcut] onTakePictureBefore - start");
        this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
        this.mSnapShotChecker.setSnapShotState(2);
        hideMenusOnTakePictureBefore();
        showFrameGridView(getGridSettingValue(), false);
        if (this.mFlashJumpcutViewManager != null) {
            if (this.mFlashJumpcutViewManager.isDrawerOpen()) {
                this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(true, false);
            }
            this.mFlashJumpcutViewManager.setJumpcutBtnEnable(false);
            this.mFlashJumpcutViewManager.setJumpcutChildViewVisibility(false, false);
        }
    }

    public String getShotMode() {
        return CameraConstants.MODE_FLASH_JUMPCUT;
    }

    protected void onChangeModuleBefore() {
        super.onChangeModuleBefore();
        if (!this.mGet.isCameraChangingOnFlashJumpCut() && !CameraConstants.MODE_FLASH_JUMPCUT.equals(getSettingValue(Setting.KEY_MODE))) {
            ListPreference listPref = this.mGet.getListPreference(Setting.KEY_MODE);
            String defaultValue = listPref == null ? "mode_normal" : listPref.getDefaultValue();
            String modeRear = this.mGet.getSpecificSetting(true).getSettingValue(Setting.KEY_MODE);
            String modeFront = this.mGet.getSpecificSetting(false).getSettingValue(Setting.KEY_MODE);
            if (CameraConstants.MODE_FLASH_JUMPCUT.equals(modeRear)) {
                this.mGet.getSpecificSetting(true).setSetting(Setting.KEY_MODE, defaultValue, true);
            }
            if (CameraConstants.MODE_FLASH_JUMPCUT.equals(modeFront)) {
                this.mGet.getSpecificSetting(false).setSetting(Setting.KEY_MODE, defaultValue, true);
            }
        }
    }

    protected boolean isPauseWaitDuringShot() {
        return false;
    }
}
