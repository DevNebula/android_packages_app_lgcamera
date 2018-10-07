package com.lge.camera.app;

import android.os.Handler;
import android.os.Message;
import android.util.Size;
import android.view.KeyEvent;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.FaceCommon;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ColorEffectFront;
import com.lge.camera.managers.ColorEffectManager;
import com.lge.camera.managers.ShutterlessSelfieManager;
import com.lge.camera.managers.ShutterlessSelfieManager.OnShutterlessSelfieListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ChildSettingRunnable;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SystemBarUtil;
import java.util.HashMap;

public class GestureShotCameraModuleBase extends DefaultCameraModule implements OnShutterlessSelfieListener {
    private static final int FACE_COUNT_THRESHHOLD = 3;
    protected static final int sLightFrameTime = 1332;
    protected ChildSettingRunnable mChildSettingUpdater_shutterlessSelfie = new C02563();
    private int mCurFace = 0;
    protected boolean mIsCaptureByShutterless = false;
    private boolean mIsLightFrameOn = false;
    protected boolean mIsShootingStartedWithLightFrame = false;
    private int mLastFace = 0;
    private HandlerRunnable mLightFrameTakePictureRunnable = null;
    private View mLightFrameView = null;
    private Runnable mShutterlessRunnable = null;
    protected ShutterlessSelfieManager mShutterlessSelfieManager = new ShutterlessSelfieManager(this);
    private Handler mTakePictureByShutterless = null;

    /* renamed from: com.lge.camera.app.GestureShotCameraModuleBase$1 */
    class C02541 implements Runnable {
        C02541() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "take Shutterless Selfie");
            GestureShotCameraModuleBase.this.mIsCaptureByShutterless = true;
            GestureShotCameraModuleBase.this.onCameraShutterButtonClicked();
            LdbUtil.setShutterType("Shutterless");
            GestureShotCameraModuleBase.this.mIsCaptureByShutterless = false;
            GestureShotCameraModuleBase.this.mShutterlessSelfieManager.setIsFaceDetected(false);
            GestureShotCameraModuleBase.this.mShutterlessSelfieManager.setIsBlingAniStarted(false);
        }
    }

    /* renamed from: com.lge.camera.app.GestureShotCameraModuleBase$3 */
    class C02563 extends ChildSettingRunnable {
        C02563() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            GestureShotCameraModuleBase.this.stopShutterlessSelfie();
            GestureShotCameraModuleBase.this.setSetting(Setting.KEY_SHUTTERLESS_SELFIE, value, true);
            if ("Shutterless".equals(value) && GestureShotCameraModuleBase.this.mShutterlessSelfieManager != null) {
                GestureShotCameraModuleBase.this.mShutterlessSelfieManager.setIsFirstShot(true);
            }
            GestureShotCameraModuleBase.this.startSelfieEngine();
        }

        public boolean checkChildAvailable() {
            if (GestureShotCameraModuleBase.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    public GestureShotCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        this.mTakePictureByShutterless = new Handler();
        this.mShutterlessRunnable = new C02541();
        this.mLightFrameTakePictureRunnable = new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "[lightFrame] Run mLightFrameTakePictureRunnable");
                GestureShotCameraModuleBase.this.mIsShootingStartedWithLightFrame = true;
                GestureShotCameraModuleBase.this.doTakePicture();
            }
        };
    }

    protected void doTakePicture() {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "[lightFrame] doTakePicture. mIsShootingStartedWithLightFrame : " + this.mIsShootingStartedWithLightFrame);
        this.mLightFrameView = this.mGet.findViewById(C0088R.id.full_light_frame);
        if (this.mIsShootingStartedWithLightFrame) {
            if (isPaused()) {
                CamLog.m5e(CameraConstants.TAG, "[lightFrame] stop lightframe and reset value. because camera app is paused.");
            } else {
                super.doTakePicture();
            }
        } else if (!"on".equals(getSettingValue(Setting.KEY_LIGHTFRAME)) || this.mSnapShotChecker.checkMultiShotState(4)) {
            super.doTakePicture();
        } else if (this.mLightFrameView != null) {
            int lightFrameTime;
            if (this.mLightFrameManager == null) {
                lightFrameTime = sLightFrameTime;
            } else {
                lightFrameTime = this.mLightFrameManager.getLightFrameRemainTime();
            }
            if (this.mCameraDevice != null) {
                CameraParameters parameters = this.mCameraDevice.getParameters();
                parameters.set(ParamConstants.KEY_LIGHTFRMAE_TIME, lightFrameTime);
                setParameters(parameters);
            }
            AnimationUtil.startShowingAnimation(this.mLightFrameView, true, 300, null);
            if (this.mLightFrameManager != null) {
                this.mLightFrameManager.setBacklightToMax(this.mGet.getActivity());
            }
            this.mIsLightFrameOn = true;
            String str = CameraConstants.TAG;
            StringBuilder append = new StringBuilder().append("[lightFrame] doTakePicture. show light frame view. mLightFrameTakePictureRunnable is null ? ");
            if (this.mLightFrameTakePictureRunnable != null) {
                z = false;
            }
            CamLog.m3d(str, append.append(z).append(", delay time : ").append(lightFrameTime).toString());
            this.mGet.removePostRunnable(this.mLightFrameTakePictureRunnable);
            this.mGet.postOnUiThread(this.mLightFrameTakePictureRunnable, (long) lightFrameTime);
        }
    }

    protected boolean checkForShutterBottomButtonLongClick() {
        return !this.mIsLightFrameOn && super.checkForShutterBottomButtonLongClick();
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (!this.mIsLightFrameOn) {
            return super.onShutterUp(keyCode, event);
        }
        CamLog.m11w(CameraConstants.TAG, "[lightFrame] onShutterUp mIsLightFrameOn, Stop volume key!!!");
        return true;
    }

    public void updateBackupParameters() {
        super.updateBackupParameters();
        CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity()).setParamToBackup(ParamConstants.KEY_LIGHTFRMAE_TIME, CameraConstants.NULL);
    }

    public boolean onShutterDown(int keyCode, KeyEvent event) {
        if (!this.mIsLightFrameOn) {
            return super.onShutterDown(keyCode, event);
        }
        CamLog.m11w(CameraConstants.TAG, "[lightFrame] onShutterDown mIsLightFrameOn, Stop volume key!!!");
        return true;
    }

    protected void onShutterCallback(boolean sound, boolean animation, boolean recording) {
        super.onShutterCallback(sound, animation, recording);
        CamLog.m3d(CameraConstants.TAG, "[lightFrame] onShutterCallback. remove Light Frame View");
        if (this.mLightFrameView != null) {
            this.mLightFrameView.setVisibility(4);
        }
        this.mLightFrameManager.setBacklightToSystemSetting(this.mGet.getActivity());
        this.mIsShootingStartedWithLightFrame = false;
        this.mIsLightFrameOn = false;
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (this.mLightFrameManager != null) {
            this.mLightFrameManager.showInitView();
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        setPreviewCallbackAll(false);
        CamLog.m3d(CameraConstants.TAG, "[lightFrame] onPauseBefore. reset value for lightFrame.");
        this.mGet.removePostRunnable(this.mLightFrameTakePictureRunnable);
        if (this.mLightFrameManager != null) {
            this.mLightFrameManager.setBacklightToSystemSetting(this.mGet.getActivity());
        }
        if (this.mLightFrameView != null) {
            this.mLightFrameView.setVisibility(4);
            this.mLightFrameView = null;
        }
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setImageMetaCallback(null);
        }
        this.mIsShootingStartedWithLightFrame = false;
        this.mIsLightFrameOn = false;
        stopShutterlessSelfie();
        if (isShutterlessSettingOn()) {
            this.mShutterlessSelfieManager.stopShutterlessEngin();
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot() || this.mAdvancedFilmManager == null || this.mQuickClipManager.isOpened() || this.mExtraPrevewUIManager == null) {
            return false;
        }
        this.mAdvancedFilmManager.setMenuEnable(true);
        this.mAdvancedFilmManager.setSelfieMenuVisibility(true);
        this.mExtraPrevewUIManager.show(false, false, !isRearCamera());
        this.mExtraPrevewUIManager.setAllButtonsEnable(true);
        if (this.mColorEffectManager != null) {
            this.mColorEffectManager.setButtonEnabled(true);
        }
        setFlashBarEnabled(true);
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mTakePictureByShutterless = null;
        this.mShutterlessRunnable = null;
        this.mLightFrameTakePictureRunnable = null;
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mTimerManager == null || this.mGestureShutterManager == null || this.mShutterlessSelfieManager == null || this.mIntervalShotManager == null) {
            return false;
        }
        if (isShutterKeyOptionTimerActivated() || isAttachIntent()) {
            stopSelfieEngin();
        } else if (!(this.mIsShutterlessSelfieProgress && this.mIsCaptureByShutterless)) {
            stopShutterlessSelfie();
            pauseShutterless();
        }
        if (this.mSnapShotChecker.checkMultiShotState(2)) {
            if (checkModuleValidate(16)) {
                if (this.mIntervalShotManager.getIntervalShotState() == 1) {
                    this.mIntervalShotManager.setIntervalShotState(0);
                    rescheduleIntervalShot(false);
                }
            } else if (this.mIntervalShotManager.getIntervalShotState() == 0) {
                this.mIntervalShotManager.setIntervalShotState(1);
                rescheduleIntervalShot(true);
            }
        }
        return super.onCameraShutterButtonClicked();
    }

    protected void stopSelfieEngin() {
        if (this.mGestureShutterManager != null) {
            this.mGestureShutterManager.stopGestureEngine();
        }
        if (this.mShutterlessSelfieManager != null) {
            this.mShutterlessSelfieManager.stopShutterlessEngin();
        }
    }

    protected void doAfterStopRecorderThread() {
        super.doAfterStopRecorderThread();
        startSelfieEngine();
    }

    public void takePictureByTimer(int type) {
        super.takePictureByTimer(type);
        if (!this.mSnapShotChecker.checkMultiShotState(2) && !isAttachIntent() && getCameraState() != 5 && getCameraState() != 6) {
            startSelfieEngine();
        }
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        super.onPictureTakenCallback(data, extraExif, camera);
        if (isShutterlessSettingOn() && this.mShutterlessSelfieManager != null && !this.mSnapShotChecker.checkMultiShotState(2) && !isAttachIntent()) {
            if (this.mIsShutterlessSelfieProgress) {
                this.mIsShutterlessSelfieProgress = false;
                this.mShutterlessSelfieManager.unlockEngine();
            } else {
                startSelfieEngine();
            }
            if (this.mShutterlessSelfieManager.getIsFirstShot()) {
                this.mShutterlessSelfieManager.setIsFirstShot(false);
                this.mShutterlessSelfieManager.setFaceVisibility(false);
                this.mShutterlessSelfieManager.setGuideVisibility(false);
            }
        }
    }

    protected void oneShotPreviewCallbackDone() {
        boolean z = true;
        String shotMode = this.mGet.getCurSettingValue(Setting.KEY_MODE);
        if (((!isRearCamera() && "mode_normal".equals(shotMode)) || shotMode.contains(CameraConstants.MODE_SQUARE) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode())) && checkModuleValidate(128) && !this.mGet.isVideoCaptureMode()) {
            if (this.mGestureShutterManager != null) {
                this.mGestureShutterManager.initGestureEngine();
                if (!FunctionProperties.checkSensorStatus(this.mGet.getAppContext())) {
                    CamLog.m3d(CameraConstants.TAG, "Gesture view is not supported!! Because sensors is not attached");
                }
                if (FunctionProperties.isSupportedMotionQuickView()) {
                    this.mGestureShutterManager.initMotionEngine();
                }
            }
            startSelfieEngine();
        }
        if (!(this.mLightFrameManager == null || !"on".equals(getSettingValue(Setting.KEY_LIGHTFRAME)) || isRecordingState())) {
            this.mLightFrameManager.turnOnLightFrame();
            setImageMetaCallback(57);
        }
        setFlashLevelControlMenuAutoOn(true);
        super.oneShotPreviewCallbackDone();
        boolean isRunning = false;
        if (this.mStickerManager != null && isStickerSupportedCameraMode()) {
            if (this.mStickerManager.hasSticker()) {
                this.mStickerManager.resumeEngine();
                settingForSticker(true);
                if (FunctionProperties.getSupportedHal() == 2 && this.mCameraDevice != null) {
                    this.mCameraDevice.startFaceDetection();
                }
            }
            isRunning = this.mStickerManager.isRunning();
        }
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                Size previewSize = parameters.getPreviewSize();
                if (!(previewSize == null || this.mGestureShutterManager == null)) {
                    this.mGestureShutterManager.setPreviewSize(previewSize);
                }
            } else {
                CamLog.m5e(CameraConstants.TAG, "Camera Parameter is null");
            }
        }
        if (!(isRunning || checkModuleValidate(192))) {
            z = false;
        }
        setPreviewCallbackAll(z);
        CamLog.m3d(CameraConstants.TAG, "oneShotPreviewCallbackDone");
    }

    public boolean doBackKey() {
        if (this.mIsShutterlessSelfieProgress) {
            stopShutterlessSelfie();
            this.mLastFace = 0;
            this.mCurFace = 0;
        }
        if (this.mTimerManager != null && this.mTimerManager.isTimerShotCountdown()) {
            startSelfieEngine();
        }
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.getSelfieFilterVisibility()) {
            return super.doBackKey();
        }
        this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
        if (!getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            return true;
        }
        showDoubleCamera(true);
        return true;
    }

    protected void addModuleChildSettingMap(HashMap<String, ChildSettingRunnable> map) {
        super.addModuleChildSettingMap(map);
        if (map != null) {
            map.put(Setting.KEY_SHUTTERLESS_SELFIE, this.mChildSettingUpdater_shutterlessSelfie);
        }
    }

    protected void startSelfieEngine() {
        CamLog.m3d(CameraConstants.TAG, "start SelfieEngine");
        if (this.mShutterlessSelfieManager == null || this.mGestureShutterManager == null || !checkModuleValidate(192) || (!("mode_normal".equals(getShotMode()) || getShotMode().contains(CameraConstants.MODE_BEAUTY) || getShotMode().contains(CameraConstants.MODE_SQUARE) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()) || CameraConstants.MODE_FRONT_OUTFOCUS.equals(getShotMode())) || isVideoCaptureMode() || isActivatedQuickdetailView())) {
            CamLog.m3d(CameraConstants.TAG, "it's not available to start selfie engine");
            return;
        }
        if ("Shutterless".equals(this.mGet.getCurSettingValue(Setting.KEY_SHUTTERLESS_SELFIE)) && FunctionProperties.isShutterlessSupported(this.mGet.getAppContext())) {
            this.mGestureShutterManager.stopGestureEngine();
            this.mShutterlessSelfieManager.startShutterlessEngin();
            if (isSettingOpen()) {
                this.mShutterlessSelfieManager.pauseEngine();
                CamLog.m3d(CameraConstants.TAG, "pause Shutterless engine, setting menu is open");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "start Shutterless engine");
            return;
        }
        this.mShutterlessSelfieManager.stopShutterlessEngin();
        if (!this.mGestureShutterManager.isAvailableGestureShutterStarted()) {
            this.mGestureShutterManager.stopGestureEngine();
            this.mGestureShutterManager.releaseGestureEngine();
        } else if (isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            this.mGestureShutterManager.stopGestureEngine();
        } else {
            this.mGestureShutterManager.startGestureEngine();
        }
    }

    public boolean isQuickClipShowingCondition() {
        if (!super.isQuickClipShowingCondition() || this.mGestureShutterManager.getGesutreGuideVisibility()) {
            return false;
        }
        return true;
    }

    protected boolean isShutterlessSettingOn() {
        if ("Shutterless".equals(this.mGet.getCurSettingValue(Setting.KEY_SHUTTERLESS_SELFIE)) && (("mode_normal".equals(getShotMode()) || getShotMode().contains(CameraConstants.MODE_BEAUTY) || getShotMode().contains(CameraConstants.MODE_SQUARE) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()) || CameraConstants.MODE_FRONT_OUTFOCUS.equals(getShotMode())) && this.mGet != null && FunctionProperties.isShutterlessSupported(this.mGet.getAppContext()))) {
            return true;
        }
        return false;
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (FunctionProperties.isShutterlessSupported(this.mGet.getAppContext())) {
            this.mManagerList.add(this.mShutterlessSelfieManager);
        }
    }

    protected ColorEffectManager getColorEffectManager() {
        CamLog.m3d(CameraConstants.TAG, "[color] add - front");
        return new ColorEffectFront(this);
    }

    public boolean isColorEffectSupported() {
        if (ModelProperties.getAppTier() <= 0) {
            return false;
        }
        return super.isColorEffectSupported();
    }

    public boolean takeShutterlessSelfie() {
        if (this.mShutterlessSelfieManager == null || this.mReviewThumbnailManager == null) {
            return false;
        }
        if (!checkModuleValidate(223) || SystemBarUtil.isSystemUIVisible(getActivity()) || isTimerShotCountdown() || isRotateDialogVisible() || this.mCameraDevice == null || this.mSnapShotChecker.checkMultiShotState(2) || isSettingOpen() || isAnimationShowing() || this.mReviewThumbnailManager.isQuickViewAniStarted()) {
            if (this.mIsShutterlessSelfieProgress) {
                stopShutterlessSelfie();
            }
            CamLog.m7i(CameraConstants.TAG, "-sh- it's not available to start ShutterlessSelfie");
            return false;
        }
        this.mSnapShotChecker.removeMultiShotState(12);
        CamLog.m3d(CameraConstants.TAG, "send CLEAR_SCREEN_DELAY message");
        keepScreenOnAwhile();
        if (!this.mIsShutterlessSelfieProgress) {
            CamLog.m3d(CameraConstants.TAG, "start Shutterless Selfie animation");
            this.mIsShutterlessSelfieProgress = true;
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    GestureShotCameraModuleBase.this.mShutterlessSelfieManager.setIsFaceDetected(true);
                    GestureShotCameraModuleBase.this.mShutterlessSelfieManager.setIsBlingAniStarted(true);
                }
            });
            boolean requestAudioFocus = !"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER)) && AudioUtil.isWiredHeadsetOn();
            playSound(1, true, 0, requestAudioFocus);
            if (this.mTakePictureByShutterless != null) {
                this.mTakePictureByShutterless.postDelayed(this.mShutterlessRunnable, 1000);
            }
        }
        return true;
    }

    public void stopShutterlessSelfie() {
        if (this.mShutterlessSelfieManager != null && isShutterlessSettingOn()) {
            this.mIsCaptureByShutterless = false;
            if (this.mIsShutterlessSelfieProgress) {
                if (this.mTakePictureByShutterless != null) {
                    this.mTakePictureByShutterless.removeCallbacks(this.mShutterlessRunnable);
                    this.mShutterlessSelfieManager.setIsBlingAniStarted(false);
                }
                this.mIsShutterlessSelfieProgress = false;
                this.mShutterlessSelfieManager.unlockEngine();
            }
            this.mShutterlessSelfieManager.setIsBlingAniStarted(false);
            this.mShutterlessSelfieManager.setIsFaceDetected(false);
            this.mShutterlessSelfieManager.setFaceVisibility(false);
            this.mCurFace = 0;
            this.mLastFace = 0;
        }
    }

    public void onFaceDetection(FaceCommon[] faces) {
        int i = 1;
        if (faces != null && isShutterlessSettingOn() && this.mCameraState == 1) {
            int i2 = this.mCurFace;
            if (faces.length == 0) {
                i = -1;
            }
            this.mCurFace = i + i2;
            this.mCurFace = Math.min(this.mCurFace, 3);
            this.mCurFace = Math.max(this.mCurFace, 0);
            if ((this.mCurFace == 0 || this.mCurFace == 3) && isShutterlessAvailable() && !isSettingOpen() && this.mShutterlessSelfieManager != null && this.mShutterlessSelfieManager.isEnginRunning() && !this.mShutterlessSelfieManager.isEnginPaused()) {
                this.mLastFace = this.mCurFace;
                this.mShutterlessSelfieManager.onFaceDetection(faces);
                this.mCurFace = 0;
                if (this.mLastFace == 0) {
                    stopShutterlessSelfie();
                    this.mShutterlessSelfieManager.setFaceVisibility(false);
                }
            }
        }
    }

    protected boolean isShutterlessAvailable() {
        if (this.mReviewThumbnailManager == null || this.mFocusManager == null || this.mAdvancedFilmManager == null || !isShutterlessSettingOn() || isVideoCaptureMode() || this.mReviewThumbnailManager.isActivatedSelfieQuickView() || this.mSnapShotChecker.checkMultiShotState(2) || !checkModuleValidate(208) || isTimerShotCountdown() || !this.mFocusManager.isFaceDetectionStarted() || isRotateDialogVisible() || isSelfieOptionVisible()) {
            return false;
        }
        if (this.mStickerManager == null || !this.mStickerManager.prepareStickerDrawing()) {
            return true;
        }
        return false;
    }

    protected boolean isSettingOpen() {
        if (this.mBarManager == null || this.mQuickClipManager == null) {
            return true;
        }
        if (isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            CamLog.m3d(CameraConstants.TAG, "-sh- Setting is open, it's not available to resume Shutter-less");
            return true;
        } else if (this.mQuickClipManager.isOpened()) {
            CamLog.m3d(CameraConstants.TAG, "-sh- Quick clip menu is open, it's not available to resume Shutter-less");
            return true;
        } else if (!this.mBarManager.isBarTouching()) {
            return false;
        } else {
            CamLog.m3d(CameraConstants.TAG, "-sh- touching bar, it's not available to resume Shutter-less");
            return true;
        }
    }

    public void resumeShutterless() {
        if (isShutterlessAvailable() && this.mShutterlessSelfieManager != null && !isSettingOpen()) {
            this.mShutterlessSelfieManager.resumeEngine();
        }
    }

    public void pauseShutterless() {
        if (this.mShutterlessSelfieManager != null && isShutterlessSettingOn()) {
            stopShutterlessSelfie();
            this.mShutterlessSelfieManager.pauseEngine();
        }
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        switch (msg.what) {
            case 6:
                stopShutterlessSelfie();
                return super.mainHandlerHandleMessage(msg);
            case 31:
                break;
            case 56:
            case 57:
                setImageMetaCallback(msg.what);
                break;
            case 93:
                CamLog.m3d(CameraConstants.TAG, "resume shutterless by handler");
                resumeShutterless();
                return super.mainHandlerHandleMessage(msg);
            case 94:
                CamLog.m3d(CameraConstants.TAG, "pause shutterless by handler");
                if (this.mShutterlessSelfieManager != null) {
                    this.mShutterlessSelfieManager.cancelTimerTask();
                }
                pauseShutterless();
                return super.mainHandlerHandleMessage(msg);
            default:
                return super.mainHandlerHandleMessage(msg);
        }
        this.mCurFace = 0;
        this.mLastFace = 0;
        if (this.mShutterlessSelfieManager != null) {
            this.mShutterlessSelfieManager.setFaceVisibility(false);
        }
        return super.mainHandlerHandleMessage(msg);
    }

    private void setImageMetaCallback(int msg) {
        if (this.mLightFrameManager != null && this.mCameraDevice != null) {
            if (msg == 57) {
                this.mCameraDevice.setImageMetaCallback(this.mLightFrameManager.getImageMetaCallback());
            } else if (msg == 56) {
                this.mCameraDevice.setImageMetaCallback(null);
            }
        }
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        pauseShutterless();
        if (this.mGestureShutterManager != null) {
            this.mGestureShutterManager.stopGestureEngine();
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setSelfieMenuVisibility(false);
            this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
        }
        setFlashLevelControlMenuAutoOn(false);
        setGIFVisibility(false);
        setQuickClipIcon(true, false);
        return true;
    }

    public void checkCameraId() {
        super.checkCameraId();
        if (FunctionProperties.getCameraTypeFront() == 2 && this.mDoubleCameraManager != null) {
            int savedCropAngle = SharedPreferenceUtil.getCropAngleButtonId(getAppContext());
            int selectedCropAngle = this.mDoubleCameraManager.getSelectedCropAngleButtonId();
            CamLog.m3d(CameraConstants.TAG, "check CropAngle, selectedCropAngle = " + selectedCropAngle + ", savedCropAngle = " + savedCropAngle);
            if (selectedCropAngle != savedCropAngle && selectedCropAngle != -1) {
                int cropZoomValue;
                SharedPreferenceUtil.saveCropAngleButtonId(getAppContext(), selectedCropAngle);
                if (selectedCropAngle == 0) {
                    cropZoomValue = FunctionProperties.getCropAngleZoomLevel();
                } else {
                    cropZoomValue = 0;
                }
                this.mLocalParamForZoom = null;
                setZoomStep(cropZoomValue, false, false, false);
                CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity()).setParamToBackup("zoom", Integer.valueOf(cropZoomValue));
            }
        }
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL)) {
            return true;
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setSelfieMenuVisibility(true);
        }
        if (this.mGestureShutterManager != null && this.mGestureShutterManager.isAvailableGestureShutterStarted()) {
            this.mGestureShutterManager.startGestureEngine();
        }
        setFlashLevelControlMenuAutoOn(true);
        resumeShutterless();
        return true;
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    public void showFaceViewVisible(boolean isShow) {
    }
}
