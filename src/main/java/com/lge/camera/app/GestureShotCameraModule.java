package com.lge.camera.app;

import android.graphics.SurfaceTexture;
import android.media.Image;
import android.view.KeyEvent;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.managers.GestureShutterManagerIF.onGestureUIListener;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;

public class GestureShotCameraModule extends GestureShotCameraModuleBase {

    /* renamed from: com.lge.camera.app.GestureShotCameraModule$1 */
    class C02581 implements onGestureUIListener {
        boolean mIsGuideOn = false;

        C02581() {
        }

        public void onShowGestureGuide() {
            if (!this.mIsGuideOn) {
                GestureShotCameraModule.this.doShowGestureGuide();
                this.mIsGuideOn = true;
            }
            if (GestureShotCameraModule.this.isFlashControlBarShowing()) {
                GestureShotCameraModule.this.setFlashLevelControlMenuAutoOn(false);
            }
        }

        public void onHideGestureGuide() {
            if (this.mIsGuideOn) {
                GestureShotCameraModule.this.doHideGestureGuide();
                this.mIsGuideOn = false;
            }
            if (!GestureShotCameraModule.this.isFlashControlBarShowing()) {
                GestureShotCameraModule.this.setFlashLevelControlMenuAutoOn(true);
            }
        }
    }

    /* renamed from: com.lge.camera.app.GestureShotCameraModule$2 */
    class C02592 implements onQuickClipListListener {
        C02592() {
        }

        public void onListOpend() {
            GestureShotCameraModule.this.pauseShutterless();
            GestureShotCameraModule.this.showCommandArearUI(false);
            if (!GestureShotCameraModule.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                GestureShotCameraModule.this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
                GestureShotCameraModule.this.mAdvancedFilmManager.hideSelfieMenuTransient();
                GestureShotCameraModule.this.hideFocusOnShowOtherBars(false);
                if (GestureShotCameraModule.this.mColorEffectManager != null) {
                    GestureShotCameraModule.this.mColorEffectManager.hideMenu(false);
                }
            }
            if (GestureShotCameraModule.this.isActivatedQuickdetailView()) {
                GestureShotCameraModule.this.setDeleteButtonVisibility(false);
            }
        }

        public void onListClosed() {
            GestureShotCameraModule.this.resumeShutterless();
            GestureShotCameraModule.this.showCommandArearUI(true);
            if (!GestureShotCameraModule.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                GestureShotCameraModule.this.mAdvancedFilmManager.restoreSelfieMenuVisibility();
                if (!GestureShotCameraModule.this.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
                    GestureShotCameraModule.this.showFocusOnHideOtherBars();
                }
            }
            if (GestureShotCameraModule.this.isActivatedQuickdetailView()) {
                GestureShotCameraModule.this.setDeleteButtonVisibility(true);
            }
        }
    }

    public GestureShotCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void completeHideQuickview() {
        super.completeHideQuickview();
        CamLog.m3d(CameraConstants.TAG, "[Shutterless] isShutterlessSettingOn ? " + isShutterlessSettingOn() + ", mIsShutterlessSelfieProgress ? " + this.mIsShutterlessSelfieProgress);
        if (isShutterlessSettingOn()) {
            if (this.mShutterlessSelfieManager != null) {
                if (this.mIsShutterlessSelfieProgress) {
                    this.mIsShutterlessSelfieProgress = false;
                    this.mShutterlessSelfieManager.resetShutterlessEngine();
                } else {
                    resumeShutterless();
                }
            } else {
                return;
            }
        }
        setFlashLevelControlMenuAutoOn(true);
    }

    protected void cropAnimationEnd() {
        super.cropAnimationEnd();
        setFlashLevelControlMenuAutoOn(true);
    }

    protected void doIntervalShot() {
        stopShutterlessSelfie();
        if (isShutterlessSettingOn()) {
            this.mShutterlessSelfieManager.stopShutterlessEngin();
        }
        super.doIntervalShot();
    }

    protected void stopIntervalShot(int delay) {
        super.stopIntervalShot(delay);
        this.mSnapShotChecker.releaseSnapShotChecker();
        if (this.mShutterlessSelfieManager != null && isShutterlessSettingOn()) {
            this.mShutterlessSelfieManager.startShutterlessEngin();
        }
    }

    protected void updateRecordingUi() {
        super.updateRecordingUi();
        if (this.mRecordingUIManager != null && this.mRecordingUIManager.getVideoTime(4) / 1000 < 1) {
            setFlashLevelControlMenuAutoOn(true);
        }
    }

    public boolean onRecordStartButtonClicked() {
        if (isFlashBarPressed()) {
            return false;
        }
        boolean retValue = super.onRecordStartButtonClicked();
        if (!retValue) {
            return retValue;
        }
        if (isShutterlessSettingOn()) {
            stopShutterlessSelfie();
        }
        stopSelfieEngin();
        setQuickClipIcon(true, false);
        return retValue;
    }

    protected boolean isFocusOnTouchEvent() {
        return super.isFocusOnTouchEvent() && !isLightFrameOn();
    }

    protected void doShowGestureGuide() {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
        }
        if (this.mColorEffectManager != null) {
            this.mColorEffectManager.hideMenu(false);
        }
        if (this.mFocusManager != null) {
            hideFocusOnShowOtherBars(false);
        }
    }

    protected void doHideGestureGuide() {
        if (this.mFocusManager != null) {
            showFocusOnHideOtherBars();
        }
    }

    protected void initializeControls(boolean enable) {
        super.initializeControls(enable);
        if (this.mGestureShutterManager != null) {
            this.mGestureShutterManager.setGestureUIListener(new C02581());
        }
    }

    protected void onTakePictureBefore() {
        super.onTakePictureBefore();
        if (!isFastShotAvailable(3) && !isRearCamera()) {
            if (this.mAdvancedFilmManager != null) {
                CamLog.m3d(CameraConstants.TAG, "[filter] take photo. filter UI set invisibile");
                this.mAdvancedFilmManager.setMenuEnable(false);
            }
            if (this.mColorEffectManager != null) {
                this.mColorEffectManager.setButtonEnabled(false);
                this.mColorEffectManager.hideMenu(false);
            }
            setFlashBarEnabled(false);
        }
    }

    protected void onTakePictureAfter() {
        super.onTakePictureAfter();
        if (this.mAdvancedFilmManager != null) {
            CamLog.m3d(CameraConstants.TAG, "[filter] take photo. filter UI set visibile");
            if (!this.mSnapShotChecker.checkMultiShotState(6)) {
                this.mAdvancedFilmManager.setMenuEnable(true);
                setFlashBarEnabled(true);
            }
            onHideBeautyMenu();
        }
        if (this.mColorEffectManager != null) {
            this.mColorEffectManager.setButtonEnabled(true);
        }
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        boolean z = true;
        if (isRearCamera() || this.mAdvancedFilmManager == null) {
            super.doCleanView(doByAction, useAnimation, saveState);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "[filter] doByAction = " + doByAction);
        if (doByAction) {
            this.mAdvancedFilmManager.setSelfieMenuVisibility(false);
            this.mAdvancedFilmManager.setSelfieOptionVisibility(false, !isIntervalShotProgress());
        } else if (!this.mIsPreviewCallbackWaiting) {
            this.mAdvancedFilmManager.setSelfieMenuVisibility(true);
        }
        if (doByAction) {
            z = false;
        }
        setFlashLevelControlMenuAutoOn(z);
        super.doCleanView(doByAction, useAnimation, saveState);
    }

    protected void startRecorder() {
        if (!(this.mStickerManager == null || this.mStickerManager.isStickerDrawing())) {
            setPreviewCallbackAll(false);
        }
        super.startRecorder();
    }

    protected void stopPreview() {
        setPreviewCallbackAll(false);
        if (this.mFocusManager != null && FunctionProperties.getSupportedHal() == 2) {
            this.mFocusManager.stopFaceDetection();
        }
        super.stopPreview();
    }

    protected boolean checkPreviewCallbackCondition() {
        return true;
    }

    protected void onTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (!checkModuleValidate(128)) {
        }
    }

    protected void onChangePictureSize() {
        if (isNeedRestartByPictureSizeChanged()) {
            if (!isRearCamera()) {
                setPreviewCallbackAll(false);
            }
            super.onChangePictureSize();
            return;
        }
        if (!isRearCamera()) {
            setPreviewCallbackAll(false);
            setPreviewCallbackAll(true);
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.changePreviewSize(this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId)).getExtraInfo(2), false);
        }
        if (this.mFocusManager == null) {
            return;
        }
        if (isFocusLock() || isAELock()) {
            this.mFocusManager.resetAEAFFocus();
        }
    }

    public void onPreviewFrame(byte[] data, CameraProxy camera) {
        if (!(isShutterlessSettingOn() || this.mGestureShutterManager == null)) {
            this.mGestureShutterManager.onPreviewFrame(data, camera);
        }
        if (this.mStickerManager != null) {
            this.mStickerManager.onPreviewFrame(data);
        }
        this.mDirectCallbackManager.addCallbackBuffer();
    }

    public void onImageData(Image image) {
        if (image != null) {
            if (this.mStickerManager != null) {
                this.mStickerManager.onPreviewFrame(image);
            }
            if (this.mGestureShutterManager != null) {
                this.mGestureShutterManager.onImageData(image);
            }
            if (isLivePhotoEnabled()) {
                this.mLivePhotoManager.onPreviewFrame(image);
            }
            image.close();
        }
    }

    public void onOrientationChanged(int orientation, boolean isFirst) {
        super.onOrientationChanged(orientation, isFirst);
        this.mIntervalShotManager.setGuideTextLayoutParam();
        this.mShutterlessSelfieManager.setDegree(orientation, isFirst);
    }

    protected boolean takePicture() {
        if (this.mShutterlessSelfieManager != null && this.mIsCaptureByShutterless) {
            this.mShutterlessSelfieManager.onTakePicture();
        }
        return super.takePicture();
    }

    protected void setManagersListener() {
        super.setManagersListener();
        if (this.mShutterlessSelfieManager != null) {
            this.mShutterlessSelfieManager.setListener(this);
        }
    }

    public void onDialogDismiss() {
        if (this.mDialogManager != null && this.mGet != null) {
            super.onDialogDismiss();
        }
    }

    public void handleSwitchCamera() {
        stopShutterlessSelfie();
        stopSelfieEngin();
        super.handleSwitchCamera();
    }

    protected void setQuickClipListListener() {
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.setQuickClipListListener(new C02592());
        }
    }

    protected void onCropZoomButtonClicked(int cropId, int animationType) {
        stopShutterlessSelfie();
        stopSelfieEngin();
        super.onCropZoomButtonClicked(cropId, animationType);
    }

    public void setPreviewCoverVisibility(int visibility, boolean isMovePreview) {
        super.setPreviewCoverVisibility(visibility, isMovePreview);
        startSelfieEngine();
    }

    public void doCleanViewAfter(boolean anim) {
        super.doCleanViewAfter(anim);
    }

    protected void onFilmEngineReleased(boolean isRestartPreview, boolean isStopByRecording) {
        if (this.mLightFrameManager != null) {
            if (this.mLightFrameManager.isLightFrameMode()) {
                this.mLightFrameManager.turnOnLightFrame();
            } else {
                this.mLightFrameManager.turnOffLightFrame();
            }
        }
        super.onFilmEngineReleased(isRestartPreview, isStopByRecording);
    }

    public boolean onFingerPrintSensorDown(int keyCode, KeyEvent event) {
        CamLog.m3d(CameraConstants.TAG, "Fingerprint -- onFingerPrintSensorDown, keyCode = " + keyCode);
        if (!this.mSnapShotChecker.isIdle() || isRotateDialogVisible()) {
            return false;
        }
        if (this.mSnapShotChecker.checkMultiShotState(2) || event.getRepeatCount() != 1) {
            return true;
        }
        if (SharedPreferenceUtil.getFingerprintShotGuide(this.mGet.getAppContext()) || this.mCameraState == 6 || this.mCameraState == 7) {
            switch (this.mCameraState) {
                case 6:
                case 7:
                    onSnapShotButtonClicked();
                    return true;
                default:
                    if (this.mFocusManager == null || !this.mFocusManager.checkFocusStateForChangingSetting()) {
                        return true;
                    }
                    onCameraShutterButtonClicked();
                    return true;
            }
        }
        this.mDialogManager.showDialogPopup(140);
        return true;
    }

    public boolean isGestureGuideShowing() {
        if (this.mGestureShutterManager != null) {
            return this.mGestureShutterManager.getGesutreGuideVisibility();
        }
        return false;
    }

    public boolean isShutterZoomSupported() {
        return false;
    }

    public boolean onCameraShutterButtonClicked() {
        if (isFlashBarPressed()) {
            return false;
        }
        return super.onCameraShutterButtonClicked();
    }

    public void onShowAEBar() {
        setFlashLevelControlMenuAutoOn(false);
    }

    public void onHideAEBar() {
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !isOpeningSettingMenu() && !isBarVisible(1)) {
            setFlashLevelControlMenuAutoOn(true);
        }
    }

    public void onShowBeautyMenu(int type) {
        setFlashLevelControlMenuAutoOn(false);
        hideFocusOnShowOtherBars(false);
        if (type != 4 && this.mColorEffectManager != null) {
            this.mColorEffectManager.hideMenu(false);
        }
    }

    public void onHideBeautyMenu() {
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !isOpeningSettingMenu() && !isBarVisible(1)) {
            setFlashLevelControlMenuAutoOn(true);
            showFocusOnHideOtherBars();
        }
    }

    public boolean isSelfieOptionVisible() {
        if (this.mColorEffectManager == null) {
            return super.isSelfieOptionVisible();
        }
        if (this.mColorEffectManager.isMenuVisible() || isBarVisible(1)) {
            return true;
        }
        return false;
    }

    public void setBeautyButtonSelected(int type, boolean selected) {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setMenuButtonSelected(type, selected);
        }
    }

    public void setMenuType(int type) {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setMenuType(type);
        }
    }

    protected void doTakePictureLongShotBefore() {
        if (this.mColorEffectManager != null) {
            this.mColorEffectManager.setButtonEnabled(false);
            hideMenu(32, false, false);
        }
        super.doTakePictureLongShotBefore();
    }

    protected void handlePreviewCallback() {
    }

    public boolean setColorEffect(String value) {
        boolean set = super.setColorEffect(value);
        if (set) {
            String curValue = this.mGet.getCurSettingValue(Setting.KEY_SHUTTERLESS_SELFIE);
            if ("negative".equals(value) && FunctionProperties.isShutterlessSupported(this.mGet.getAppContext()) && curValue != null && "Shutterless".equals(curValue)) {
                showToast(getActivity().getString(C0088R.string.auto_selfie_not_work_in_negative_filter1), CameraConstants.TOAST_LENGTH_SHORT);
            }
        }
        return set;
    }

    public void childSettingMenuClicked(String key, String value, int clickedType) {
        super.childSettingMenuClicked(key, value, clickedType);
        if (!FunctionProperties.isSupportedFilmEmulator() && isColorEffectSupported()) {
            String curEffect = this.mGet.getCurSettingValue(Setting.KEY_COLOR_EFFECT);
            if (Setting.KEY_SHUTTERLESS_SELFIE.equals(key) && "Shutterless".equals(value) && "negative".equals(curEffect)) {
                showToast(getActivity().getString(C0088R.string.auto_selfie_not_work_in_negative_filter1), CameraConstants.TOAST_LENGTH_SHORT);
            }
        }
    }
}
