package com.lge.camera.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.SurfaceHolder;
import android.widget.ImageButton;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.ColorEffectFront;
import com.lge.camera.managers.ColorEffectManager;
import com.lge.camera.managers.ColorEffectRear;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

@SuppressLint({"WorldReadableFiles"})
public class AttachCameraModule extends AttachCameraModuleBase {
    private String mFrontDefaultVideoSize = null;
    private String mRearDefaultVideoSize = null;
    private String mVideoSize = null;

    public AttachCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        if (this.mCaptureParam != null) {
            if (this.mGet.isLGUOEMCameraIntent() && this.mCaptureParam.getTargetUri() != null) {
                this.mIsFirstEntry = false;
            }
            this.mCaptureParam.setupCaptureParams(this.mGet.getActivity().getIntent());
        }
        setCameraState(0);
        readVideoIntentExtras();
        if (this.mGet.isVideoCaptureMode()) {
            setDefaultVideoSize();
        }
        if (this.mGet.isLGUOEMCameraIntent()) {
            Uri savedUri = this.mGet.getSavedUri();
            if (savedUri != null) {
                this.mSavedUriFromOtherModule = savedUri;
            }
        }
        this.mGet.enableConeMenuIcon(31, false);
    }

    protected ColorEffectManager getColorEffectManager() {
        CamLog.m3d(CameraConstants.TAG, "[color] add - attach");
        return isRearCamera() ? new ColorEffectRear(this) : new ColorEffectFront(this);
    }

    public boolean isColorEffectSupported() {
        if (ModelProperties.getAppTier() > 2 || getSupportedColorEffects() == null) {
            return false;
        }
        if (isRearCamera() || (!FunctionProperties.isSupportedBeautyShot() && ModelProperties.getAppTier() > 0)) {
            return true;
        }
        return false;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setThumbnailVisibility(8);
        }
        if (!checkModuleValidate(128) && !this.mSnapShotChecker.isSnapShotProcessing()) {
            this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
        }
    }

    protected void afterCommonRequester() {
        if (this.mParamUpdater != null) {
            super.afterCommonRequester();
            if (!isRearCamera() && this.mGet.isVideoCaptureMode()) {
                if (this.mGet.isMMSIntent()) {
                    this.mBeautyManager.setBeautyLevel(0);
                    this.mParamUpdater = this.mBeautyManager.setBeautyParam(this.mParamUpdater);
                }
                this.mBeautyManager.setBeautyRelightingLevel(0);
                this.mParamUpdater = this.mBeautyManager.setBeautyRelightingParam(this.mParamUpdater);
            }
        }
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
        if (!isLGUOEMCameraIntent()) {
            setQuickButtonEnable(C0088R.id.quick_button_mode, false, true);
        }
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.enableConeMenuIcon(31, false);
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setThumbnailVisibility(8);
        }
    }

    public boolean onVideoShutterClickedBefore() {
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        if (this.mCaptureButtonManager == null) {
            return true;
        }
        this.mCaptureButtonManager.changeExtraButton(0, 1);
        return true;
    }

    public void onVideoStopClicked(boolean useThread, boolean stopByButton) {
        if (!this.mIsSwitchingCameraDuringRecording) {
            this.mIsGoingToPostview = true;
            super.onVideoStopClicked(false, stopByButton);
            setQuickButtonEnable(100, false, true);
            if (this.mReviewThumbnailManager != null) {
                this.mReviewThumbnailManager.setThumbnailVisibility(8);
            }
        }
    }

    protected void onCameraSwitchedDuringTheRecording(boolean switchingByButton) {
        super.onCameraSwitchedDuringTheRecording(switchingByButton);
        if (this.mIsFileSizeLimitReached) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    AttachCameraModule.this.onVideoStopClicked(true, false);
                }
            }, 0);
        }
    }

    protected void checkRestartPreviewOnPictureCallback() {
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator() && this.mAdvancedFilmManager.getCurrentFilmIndex() == 0) {
            CamLog.m3d(CameraConstants.TAG, "[gesture] checkRestartPreviewOnPictureCallback. isIntervalShotProgress ? " + isIntervalShotProgress());
            if (!isIntervalShotProgress()) {
                this.mAdvancedFilmManager.stopFilmEmulator(false, false);
                return;
            }
            return;
        }
        super.checkRestartPreviewOnPictureCallback();
    }

    protected void hideMenusOnTakePictureBefore() {
        super.hideMenusOnTakePictureBefore();
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isShowingFilmMenu()) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(83);
            }
            hideMenu(4, false, false);
        }
    }

    protected void onTakePictureAfter() {
        super.onTakePictureAfter();
        if (this.mFocusManager != null) {
            this.mFocusManager.hideAndCancelAllFocus(false);
            this.mFocusManager.resetEVValue(0);
            this.mFocusManager.setAEControlBarEnable(true);
            this.mFocusManager.setManualFocusButtonEnable(true);
            this.mFocusManager.setEVshutterButtonEnable(true);
        }
        if (this.mGet.getSavedUri() == null) {
            this.mSnapShotChecker.releaseAttachShotState();
            enableControls(true);
            return;
        }
        if (!isLGUOEMCameraIntent()) {
            setQuickButtonEnable(C0088R.id.quick_button_mode, false, true);
        }
        if (this.mSnapShotChecker.isAttachShotPictureTaken()) {
            setCaptureButtonEnable(false, 4);
        }
    }

    protected void initializeSettingMenus() {
        this.mGet.setAllSettingMenuEnable(true);
        if (FunctionProperties.isSupportedFilmEmulator() && this.mStickerManager != null && this.mStickerManager.hasSticker()) {
            CamLog.m3d(CameraConstants.TAG, "filter setting clear for sticker");
            setSetting(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, true);
        }
        initializeSettingMenuForAttachMode();
        if (!isLGUOEMCameraIntent()) {
            setSetting(Setting.KEY_MODE, "mode_normal", false);
        } else if (this.mIsFirstEntry) {
            setSetting(Setting.KEY_MODE, "mode_normal", true);
        }
        setSetting(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), this.mVideoSize, false);
        updateSecondCameraSettings(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), this.mVideoSize, false);
        String voiceShutterValue = getSettingValue(Setting.KEY_VOICESHUTTER);
        if ("on".equals(voiceShutterValue)) {
            this.mGet.setSetting(Setting.KEY_VOICESHUTTER, voiceShutterValue, true);
        }
        String hdrValue = getSettingValue("hdr-mode");
        if (!"not found".equals(hdrValue) && "1".equals(hdrValue) && (!FunctionProperties.isAppyingFilmLimitation() || CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR)))) {
            setSetting("flash-mode", "off", false);
            setSettingMenuEnable("flash-mode", false);
        }
        if (this.mCaptureParam != null && this.mCaptureParam.isRequestedSingleImage()) {
            setSpecificSettingValueAndDisable(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, false);
        }
        if (FunctionProperties.isAppyingFilmLimitation() && !CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))) {
            setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        }
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        if (FunctionProperties.isLivePhotoSupported()) {
            setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        }
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    private void initializeSettingMenuForAttachMode() {
        if (this.mGet.isVideoCaptureMode()) {
            initializeVideoCaptureMode(isMMSIntent());
            setSpecificSettingValueAndDisable("tracking-af", "off", false);
            if (FunctionProperties.getSupportedHal() == 2) {
                setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
            }
            if (!FunctionProperties.isSupportedFilmRecording()) {
                setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
            }
            setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
            setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
            return;
        }
        initializeImageCaptureMode(isMMSIntent());
        restoreTrackingAFSetting();
    }

    private void setPictureSizeFor4x3recording(CameraParameters parameters) {
        if (FunctionProperties.getSupportedHal() == 2) {
            ListPreference pictureListPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            ListPreference videoListPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            if (pictureListPref != null && videoListPref != null) {
                int[] videoScreenSize = Utils.sizeStringToArray(videoListPref.getExtraInfo(2));
                float ratio = ((float) videoScreenSize[1]) / ((float) videoScreenSize[0]);
                if (ratio > 0.7f && ratio < 0.8f) {
                    String pictureSize = pictureListPref.getEntryValues()[0].toString();
                    setSetting(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), pictureSize, false);
                    setParamUpdater(parameters, "picture-size", pictureSize);
                }
            }
        }
    }

    protected void restoreSettingMenus() {
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_QR);
        if (isMMSIntent()) {
            restoreSettingValue(Setting.KEY_BINNING);
            restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        }
        restoreSettingValue(Setting.KEY_FULLVISION);
        if (FunctionProperties.isLivePhotoSupported()) {
            restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        }
        if (this.mGet.isVideoCaptureMode() && FunctionProperties.getSupportedHal() == 2) {
            restoreSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        }
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
        super.restoreSettingMenus();
    }

    protected void setPreviewLayoutParam() {
        super.setPreviewLayoutParam();
        if (this.mGet.isVideoCaptureMode() && this.mVideoSize != null) {
            ListPreference listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            if (listPref != null) {
                int[] size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
                this.mGet.setTextureLayoutParams(size[0], size[1], -1);
                setAnimationLayout(1);
            }
        }
    }

    protected void executeAttachPostview(final Uri uri) {
        if (this.mGet.isAttachIntent()) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (AttachCameraModule.this.mPostviewManager != null && uri != null && AttachCameraModule.this.checkModuleValidate(208) && !AttachCameraModule.this.isPostviewShowing()) {
                        AttachCameraModule.this.stopPreview();
                        if (!(AttachCameraModule.this.mCameraDevice == null || AttachCameraModule.this.isRearCamera())) {
                            CameraParameters param = AttachCameraModule.this.mCameraDevice.getParameters();
                            AttachCameraModule.this.setFlashTorch(param, false, param.getFlashMode(), true);
                        }
                        if (!(AttachCameraModule.this.isShutterlessSettingOn() || AttachCameraModule.this.mGestureShutterManager == null)) {
                            AttachCameraModule.this.mGestureShutterManager.stopGestureEngine();
                        }
                        AttachCameraModule.this.showFrameGridView("off", false);
                        String mediaType = AttachCameraModule.this.mGet.getAppContext().getContentResolver().getType(uri);
                        int type = 0;
                        if (mediaType != null) {
                            if (mediaType.startsWith(CameraConstants.MIME_TYPE_VIDEO)) {
                                type = 1;
                            } else {
                                type = 0;
                            }
                        }
                        ArrayList<Uri> uriList = new ArrayList();
                        uriList.add(uri);
                        AttachCameraModule.this.mGet.setupOptionMenu(1);
                        AttachCameraModule.this.mPostviewManager.executePostview(AttachCameraModule.this.mPostviewListener, 1, uriList, type);
                        AttachCameraModule.this.mIsGoingToPostview = false;
                    }
                }
            });
        }
    }

    public boolean isFocusEnableCondition() {
        if (this.mGet.isVideoCaptureMode() || isPostviewShowing() || this.mSnapShotChecker.isAttachShotPictureTaken()) {
            return false;
        }
        return super.isFocusEnableCondition();
    }

    protected void setPreviewFpsRange(CameraParameters parameters, boolean isRecordingStarted) {
        if (this.mParamUpdater != null && parameters != null) {
            parameters.set(ParamConstants.KEY_HFR, "off");
            if (isUHDmode() && this.mGet.isVideoCaptureMode()) {
                setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FPS_RANGE, SystemProperties.get(ParamConstants.FPS_RANGE_SYSTEM_PROPERTY_CAMCORDER, "15000,30000"));
            } else if (isRecordingStarted && isMMSRecording()) {
                setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FPS_RANGE, ParamConstants.PREVIEW_FPS_RANGE_MMS);
            } else {
                super.setPreviewFpsRange(parameters, isRecordingStarted);
            }
        }
    }

    protected boolean isNeedProgressBar() {
        long recordingDurationLimit;
        long recordingSizeLimit;
        if (this.mGet.isMMSIntent()) {
            recordingDurationLimit = (long) MultimediaProperties.getMMSMaxDuration();
            recordingSizeLimit = MmsProperties.getMmsVideoSizeLimit(this.mGet.getAppContext().getContentResolver());
        } else {
            recordingDurationLimit = (long) this.mAttachRecordingDuration;
            recordingSizeLimit = this.mAttachRecordingSize;
        }
        return this.mGet.isMMSIntent() || recordingDurationLimit > 0 || recordingSizeLimit > 0;
    }

    protected boolean isAvailableSteadyCam() {
        if (!this.mGet.isVideoCaptureMode()) {
            return false;
        }
        if ((FunctionProperties.getSupportedHal() == 2 && isVideoCaptureMode()) || MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), this.mVideoSize)) {
            return false;
        }
        return super.isAvailableSteadyCam();
    }

    private void readVideoIntentExtras() {
        if (isMMSIntent()) {
            this.mAttachRecordingSize = MmsProperties.getMmsVideoSizeLimit(this.mGet.getAppContext().getContentResolver());
            this.mAttachRecordingDuration = MultimediaProperties.getMMSMaxDuration();
        }
        Intent intent = getActivity().getIntent();
        Bundle getExBundle = intent.getExtras();
        if (getExBundle == null) {
            CamLog.m3d(CameraConstants.TAG, "intent.getExtras() is null. assume no limit.");
            this.mAttachRecordingSize = 0;
        } else {
            this.mAttachRecordingSize = getExBundle.getLong("android.intent.extra.sizeLimit", 0);
            if (this.mAttachRecordingSize == 0) {
                this.mAttachRecordingSize = (long) getExBundle.getInt("android.intent.extra.sizeLimit", 0);
            }
            CamLog.m3d(CameraConstants.TAG, String.format("requested file size limit: %d", new Object[]{Long.valueOf(this.mAttachRecordingSize)}));
        }
        if (intent.hasExtra("android.intent.extra.durationLimit")) {
            int seconds = intent.getIntExtra("android.intent.extra.durationLimit", 0);
            CamLog.m3d(CameraConstants.TAG, String.format("duration limit: %d", new Object[]{Integer.valueOf(seconds)}));
            if (this.mGet.isCallingPackage(CameraConstants.PACKAGE_TICTOC)) {
                seconds = 0;
            }
            this.mAttachRecordingDuration = seconds * 1000;
        }
        CamLog.m3d(CameraConstants.TAG, "limit recording filesize / duration : " + this.mAttachRecordingSize + " / " + this.mAttachRecordingDuration);
    }

    private void initializeVideoCaptureMode(boolean isMMSAttach) {
        CharSequence[] recordSize = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).getEntryValues();
        if ((isMMSAttach || (this.mAttachRecordingSize <= CameraConstants.VIDEO_QVGASET_LIMIT_SIZE && !isPlayRingMode())) && !(this.mAttachRecordingDuration == 0 && this.mAttachRecordingSize == 0)) {
            int i = 0;
            while (i < recordSize.length && !this.mVideoSize.equals(recordSize[i])) {
                this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), (String) recordSize[i], false);
                i++;
            }
            setSpecificSettingValueAndDisable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), this.mVideoSize, false);
            setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
            setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        } else if (this.mAttachRecordingSize != 0 && this.mAttachRecordingSize < 20971520) {
            this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), ParamConstants.VIDEO_3840_BY_2160, false);
        }
        setSettingMenuEnable("picture-size", false);
        setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, false);
        setDefaultSettingValueAndDisable(Setting.KEY_LIGHTFRAME, false);
        setDefaultSettingValueAndDisable(Setting.KEY_VOICESHUTTER, false);
        setSpecificSettingValueAndDisable(Setting.KEY_SAVE_DIRECTION, "off", false);
        setDefaultSettingValueAndDisable(Setting.KEY_MODE, false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_RAW_PICTURE, "off", false);
    }

    private void initializeImageCaptureMode(boolean isMMSAttach) {
        if (isMMSAttach) {
            setDefaultSettingValueAndDisable(Setting.KEY_MODE, false);
        }
        restoreBinningSetting();
        setDefaultSettingValueAndDisable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), false);
    }

    private void setDefaultVideoSize() {
        if (isRearCamera()) {
            if (this.mRearDefaultVideoSize != null) {
                this.mVideoSize = this.mGet.getCurSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
                return;
            }
        } else if (this.mFrontDefaultVideoSize != null) {
            this.mVideoSize = this.mGet.getCurSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            return;
        }
        if (isMMSIntent()) {
            this.mVideoSize = MmsProperties.getMaximumMmsResolutions(this.mGet.getActivity().getContentResolver());
            if (this.mVideoSize == null) {
                this.mVideoSize = CameraConstants.QCIF_RESOLUTION;
            }
        } else {
            ListPreference listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            if (listPref == null) {
                this.mVideoSize = "1280x720";
            } else if (isPlayRingMode()) {
                if (listPref.findIndexOfValue(CameraConstants.VGA_RESOLUTION) != -1) {
                    this.mVideoSize = CameraConstants.VGA_RESOLUTION;
                } else if (listPref.findIndexOfValue(CameraConstants.TV_RESOLUTION) != -1) {
                    this.mVideoSize = CameraConstants.TV_RESOLUTION;
                } else if (listPref.findIndexOfValue("1280x720") != -1) {
                    this.mVideoSize = "1280x720";
                } else {
                    this.mVideoSize = this.mGet.getCurSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
                }
                CamLog.m3d(CameraConstants.TAG, "set video-size to " + this.mVideoSize);
                return;
            } else if (this.mAttachRecordingDuration == 0 && this.mAttachRecordingSize == 0) {
                if (listPref.findIndexOfValue(CameraConstants.VGA_RESOLUTION) != -1) {
                    this.mVideoSize = CameraConstants.VGA_RESOLUTION;
                } else {
                    this.mVideoSize = this.mGet.getCurSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
                }
            } else if (this.mAttachRecordingSize <= CameraConstants.VIDEO_QVGASET_LIMIT_SIZE) {
                if (listPref.findIndexOfValue(CameraConstants.QVGA_RESOLUTION) != -1) {
                    this.mVideoSize = CameraConstants.QVGA_RESOLUTION;
                } else if (listPref.findIndexOfValue(CameraConstants.QCIF_RESOLUTION) != -1) {
                    this.mVideoSize = CameraConstants.QCIF_RESOLUTION;
                } else {
                    this.mVideoSize = this.mGet.getCurSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
                }
            } else if (this.mAttachRecordingSize > CameraConstants.VIDEO_QVGASET_LIMIT_SIZE || this.mAttachRecordingDuration > 0) {
                this.mVideoSize = CameraConstants.VGA_RESOLUTION;
            } else {
                this.mVideoSize = this.mGet.getCurSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            }
        }
        if (isRearCamera()) {
            this.mRearDefaultVideoSize = this.mVideoSize;
        } else {
            this.mFrontDefaultVideoSize = this.mVideoSize;
        }
        CamLog.m3d(CameraConstants.TAG, "set video-size to " + this.mVideoSize);
    }

    protected void setupVideosize(CameraParameters parameters, ListPreference listPref) {
        if (this.mCameraDevice != null && parameters != null && listPref != null) {
            setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
            setParamUpdater(parameters, ParamConstants.KEY_VIDEO_SIZE, listPref.getValue());
            setupVideoPreviewSize(listPref, parameters);
            parameters.set(ParamConstants.KEY_HFR, "off");
            setParameters(parameters);
            this.mVideoSize = listPref.getValue();
        }
    }

    protected void setupPreview(CameraParameters params) {
        if (this.mGet.isVideoCaptureMode() && this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (!(this.mParamUpdater == null || parameters == null)) {
                this.mParamUpdater.updateAllParameters(parameters);
                setParamForVideoPreview(parameters);
            }
        }
        super.setupPreview(params);
    }

    private void setParamForVideoPreview(CameraParameters parameters) {
        if (parameters == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera parameter is null.");
            return;
        }
        setZSL(parameters, "off");
        setZSLBuffCount(parameters, "1");
        if (checkModuleValidate(192)) {
            setPreviewFpsRange(parameters, false);
        }
        setupVideoPreviewSize(getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)), parameters);
        setParamUpdater(parameters, ParamConstants.KEY_VIDEO_SIZE, this.mVideoSize);
        setPictureSizeFor4x3recording(parameters);
    }

    protected void setSpecificModuleParam(CameraParameters parameters) {
        super.setSpecificModuleParam(parameters);
        if (this.mGet.isVideoCaptureMode()) {
            setParamForVideoPreview(parameters);
        }
    }

    protected boolean takePicture() {
        if (super.takePicture()) {
            this.mSnapShotChecker.setAttachShotState(1);
            if (isShutterlessSettingOn()) {
                stopShutterlessSelfie();
                this.mShutterlessSelfieManager.stopShutterlessEngin();
            }
            return true;
        }
        this.mIsGoingToPostview = false;
        this.mSnapShotChecker.releaseAttachShotState();
        return false;
    }

    private void setupVideoPreviewSize(ListPreference listPref, CameraParameters parameters) {
        if (listPref != null && parameters != null) {
            String previewSize = getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue().split("@")[0]);
            int[] videoPreviewSize = Utils.sizeStringToArray(previewSize);
            setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_SIZE, previewSize);
            if (FunctionProperties.getSupportedHal() == 2) {
                this.mGet.setCameraPreviewSize(videoPreviewSize[0], videoPreviewSize[1]);
            }
        }
    }

    protected void restoreBinningSetting() {
        super.restoreBinningSetting();
        if (this.mGet.isVideoCaptureMode()) {
            if (isMMSIntent()) {
                setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
            }
            setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        }
        restoreSteadyCamSetting();
    }

    public void setBinningSettings(boolean isBinningOn) {
        super.setBinningSettings(isBinningOn);
        if (!isPaused() && this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null && this.mGet.isVideoCaptureMode()) {
                setSpecificSettingValueAndDisable("hdr-mode", "0", false);
                setParamUpdater(parameters, "hdr-mode", "0");
                setHDRMetaDataCallback(null);
                restoreSteadyCamSetting();
            }
        }
    }

    protected void changeRequester() {
        super.changeRequester();
        restoreSteadyCamSetting();
        this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, this.mGet.getCurSettingValue(Setting.KEY_VIDEO_STEADY));
        this.mParamUpdater.setParamValue(ParamConstants.KEY_VIEW_MODE, "normal");
        if (isVideoAttachMode() && isLiveSnapshotSupported()) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_VIDEO_SNAPSHOT, "on");
        } else {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_VIDEO_SNAPSHOT, "off");
        }
    }

    public void disableSettingValueInMdm() {
        super.disableSettingValueInMdm();
        Activity activity = this.mGet.getActivity();
        if ("android.media.action.VIDEO_CAPTURE".equals(activity.getIntent().getAction())) {
            Toast.makeText(activity.getApplicationContext(), this.mGet.getAppContext().getString(FunctionProperties.isSupportedVoiceShutter() ? C0088R.string.block_recording_and_cheeseshutter_mdm : C0088R.string.block_recording_mdm), 0).show();
            activity.finish();
        }
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        super.onPictureTakenCallback(data, extraExif, camera);
        if (this.mGet.getSavedUri() != null) {
            setQuickButtonEnable(100, false, true);
        }
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mIsGoingToPostview || this.mSnapShotChecker.isAttachShotPictureTaken()) {
            CamLog.m3d(CameraConstants.TAG, "onCameraShutterButtonClicked return - mIsGoingToPostview : " + this.mIsGoingToPostview + ", isAttachShotPictureTaken : " + this.mSnapShotChecker.isAttachShotPictureTaken());
            return false;
        } else if (super.onCameraShutterButtonClicked()) {
            this.mIsGoingToPostview = true;
            return true;
        } else {
            this.mIsGoingToPostview = false;
            this.mSnapShotChecker.releaseAttachShotState();
            return false;
        }
    }

    public void notifyNewMedia(Uri uri, boolean updateThumbnail) {
        CamLog.m3d(CameraConstants.TAG, "notifyNewMedia : URI = " + uri);
        if (!this.mGet.isAttachIntent() || this.mGet.isVideoCaptureMode()) {
            FileManager.broadcastNewMedia(this.mGet.getAppContext(), uri);
            this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
        }
        if (updateThumbnail && this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.doAfterCaptureProcess(uri, false);
        }
        if (isLGUOEMCameraIntent()) {
            this.mGet.getSavedUri();
        }
        boolean noAttachPostview = getActivity().getIntent().getBooleanExtra("noAttachPostview", false);
        CamLog.m3d(CameraConstants.TAG, "noAttachPostview : " + noAttachPostview);
        if (noAttachPostview) {
            attatchMediaOnPostview(uri, 0);
        } else {
            executeAttachPostview(uri);
        }
    }

    public boolean isRequestedSingleImage() {
        if (this.mCaptureParam == null) {
            return false;
        }
        return this.mCaptureParam.isRequestedSingleImage();
    }

    public void setBarVisible(boolean bVisible) {
        if (isRearCamera() || isVideoCaptureMode() || !FunctionProperties.isSupportedBeautyShot()) {
            super.setBarVisible(false);
        } else {
            super.setBarVisible(bVisible);
        }
    }

    protected void onSurfaceChanged(SurfaceHolder holder, int width, int height) {
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            super.onSurfaceChanged(holder, width, height);
        }
    }

    protected void startSelfieEngine() {
        if (!this.mIsGoingToPostview && !this.mSnapShotChecker.isAttachShotPictureTaken()) {
            super.startSelfieEngine();
        }
    }

    public void onShutterLargeButtonClicked() {
        if (checkModuleValidate(15) && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 12:
                    onCameraShutterButtonClicked();
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

    protected int getLoopRecordingType() {
        return 0;
    }

    public boolean setShutterButtonListener(boolean set) {
        ImageButton shutterLarge = (ImageButton) this.mGet.findViewById(C0088R.id.shutter_large_comp);
        if (shutterLarge == null || !super.setShutterButtonListener(set)) {
            return false;
        }
        shutterLarge.setOnLongClickListener(null);
        return true;
    }

    protected void cropAnimationEnd() {
        super.cropAnimationEnd();
        if (!isLGUOEMCameraIntent()) {
            setQuickButtonEnable(C0088R.id.quick_button_mode, false, true);
        }
    }

    protected void onChangePictureSize() {
        super.onChangePictureSize();
        if (this.mGet.isVideoCaptureMode() && this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            this.mAdvancedFilmManager.changePreviewSize(this.mParamUpdater.getParamValue(ParamConstants.KEY_PREVIEW_SIZE), true);
        }
    }

    public boolean checkPreviewCoverVisibilityForRecording() {
        if (!isVideoCaptureMode()) {
            super.checkPreviewCoverVisibilityForRecording();
        } else if ("on".equals(getSettingValue(Setting.KEY_VIDEO_STEADY))) {
            return true;
        }
        return false;
    }

    public boolean isUnderHDResolution() {
        String videoSizeStr = getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        return CameraConstants.VGA_RESOLUTION.equals(videoSizeStr) || CameraConstants.TV_RESOLUTION.equals(videoSizeStr) || CameraConstants.QCIF_RESOLUTION.equals(videoSizeStr);
    }

    protected void doOneShotPreviewCallbackActionForLivePhoto() {
        if (FunctionProperties.isLivePhotoSupported() && this.mLivePhotoManager != null) {
            this.mLivePhotoManager.disableLivePhoto();
        }
    }

    public boolean isStickerIconDisableCondition() {
        if (FunctionProperties.isSupportedSticker()) {
            return this.mGet.isVideoCaptureMode();
        }
        return false;
    }
}
