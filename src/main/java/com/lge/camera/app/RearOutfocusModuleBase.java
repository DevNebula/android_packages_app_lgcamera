package com.lge.camera.app;

import android.content.res.Configuration;
import android.net.Uri;
import android.view.KeyEvent;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.ICameraCallback.OutFocusCallback;
import com.lge.camera.device.OutfocusCaptureResult;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.managers.ArcOutfocusManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.XMPWriter;

public class RearOutfocusModuleBase extends DefaultCameraModule {
    private static final int INIT_GUIDE_DURATION = 3000;
    private static final int OUTFOCUS_CB_DELAY = 1000;
    private boolean mIsNeedInitGuide = true;
    private boolean mIsShowingInitGuide = false;
    private OutfocusCaptureResult mOutfocusCaptureResult = null;
    private int mOutfocusCaptureState = 0;
    private ArcOutfocusManager mOutfocusManager = new ArcOutfocusManager(this, 7);
    private int mOutfocusState = 0;

    /* renamed from: com.lge.camera.app.RearOutfocusModuleBase$1 */
    class C03421 extends OutFocusCallback {
        C03421() {
        }

        public void onOutFocusResult(final int errorType) {
            if (RearOutfocusModuleBase.this.mOutfocusManager != null && RearOutfocusModuleBase.this.mOutfocusManager.isUpdateNewState(errorType, RearOutfocusModuleBase.this.mOutfocusState)) {
                RearOutfocusModuleBase.this.mGet.runOnUiThread(new HandlerRunnable(RearOutfocusModuleBase.this) {
                    public void handleRun() {
                        RearOutfocusModuleBase.this.mOutfocusState = errorType;
                        if (!RearOutfocusModuleBase.this.mIsShowingInitGuide && RearOutfocusModuleBase.this.mOutfocusManager != null) {
                            String msg = RearOutfocusModuleBase.this.mOutfocusManager.getErrorMessage(RearOutfocusModuleBase.this.mOutfocusState);
                            CamLog.m7i(CameraConstants.TAG, "OutFocus Result " + RearOutfocusModuleBase.this.mOutfocusState);
                            RearOutfocusModuleBase.this.mOutfocusManager.showGuideText(msg != null, msg);
                        }
                    }
                });
            }
        }

        public void onOutFocusCaptureResult(final OutfocusCaptureResult outfocusResult) {
            if (outfocusResult != null) {
                CamLog.m7i(CameraConstants.TAG, "OutFocus Capture Result : " + outfocusResult.getErrorType() + " Result when to start capture : " + RearOutfocusModuleBase.this.mOutfocusCaptureState);
                RearOutfocusModuleBase.this.mGet.runOnUiThread(new HandlerRunnable(RearOutfocusModuleBase.this) {
                    public void handleRun() {
                        if (RearOutfocusModuleBase.this.mOutfocusCaptureState != 0) {
                            outfocusResult.setErrorType(RearOutfocusModuleBase.this.mOutfocusCaptureState);
                        }
                        RearOutfocusModuleBase.this.mOutfocusCaptureResult = outfocusResult;
                    }
                });
            }
        }
    }

    public RearOutfocusModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public String getShotMode() {
        return CameraConstants.MODE_REAR_OUTFOCUS;
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mOutfocusManager);
    }

    protected void changeRequester() {
        super.changeRequester();
        this.mOutfocusManager.initParamUpdater(this.mParamUpdater);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_FPS_RANGE, "30000,30000");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_JPEG_STR);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
    }

    protected void closeCamera() {
        this.mOutfocusManager.restoreParam(this.mParamUpdater);
        super.closeCamera();
    }

    public void onDestroy() {
        this.mOutfocusManager.removeParamUpdater(this.mParamUpdater);
        this.mIsNeedInitGuide = true;
        super.onDestroy();
    }

    public int getShutterButtonType() {
        return 4;
    }

    private void setOutFocusResultCallback(boolean bSet) {
        CamLog.m3d(CameraConstants.TAG, "setOutFocusResultCallback = " + this.mCameraDevice + ", bSet : " + bSet);
        if (this.mCameraDevice != null) {
            this.mOutfocusState = 0;
            if (bSet && checkModuleValidate(1)) {
                this.mCameraDevice.setOutFocusCallback(new C03421());
            } else {
                this.mCameraDevice.setOutFocusCallback(null);
            }
        }
    }

    public void onPauseAfter() {
        setOutFocusResultCallback(false);
        super.onPauseAfter();
    }

    public void onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "shutter large clicked.");
        if (checkModuleValidate(15) && !this.mGet.isAnimationShowing() && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 12:
                    onShutterBottomButtonClickListener();
                    return;
                default:
                    super.onShutterLargeButtonClicked();
                    return;
            }
        }
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        this.mOutfocusManager.setUIVisibility(8);
        return true;
    }

    public boolean onHideMenu(int menuType) {
        boolean hidemenu = super.onHideMenu(menuType);
        if (!hidemenu) {
            return hidemenu;
        }
        int checkMenuTypes = (menuType ^ -1) & CameraConstants.MENU_TYPE_ALL;
        if (this.mGet.isOpeningSettingMenu() || isMenuShowing(checkMenuTypes)) {
            return hidemenu;
        }
        this.mOutfocusManager.setUIVisibility(0);
        return true;
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (!this.mIsNeedInitGuide) {
            this.mOutfocusManager.setUIVisibility(doByAction ? 8 : 0);
        }
    }

    protected void onTakePictureBefore() {
        super.onTakePictureBefore();
        this.mOutfocusManager.setBarEnable(false);
    }

    protected void doTakePicture() {
        super.doTakePicture();
        this.mOutfocusCaptureState = this.mOutfocusState;
    }

    protected void onTakePictureAfter() {
        super.onTakePictureAfter();
        this.mOutfocusManager.setBarEnable(true);
    }

    protected int convertCameraId(int cameraId) {
        return 3;
    }

    public boolean isZoomAvailable() {
        return false;
    }

    public boolean isZoomAvailable(boolean checkRecordingState) {
        return isZoomAvailable();
    }

    public boolean isShutterZoomSupported() {
        return false;
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue("hdr-mode");
        restoreSettingValue("flash-mode");
        restoreSettingValue(Setting.KEY_SIGNATURE);
        if (!"1".equals(getSettingValue("hdr-mode"))) {
            restoreFlashSetting();
        } else if (getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected void stopPreview() {
        runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                RearOutfocusModuleBase.this.mOutfocusState = 0;
                RearOutfocusModuleBase.this.mOutfocusManager.showGuideText(false, null);
            }
        });
        super.stopPreview();
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    public boolean displayUIComponentAfterOneShot() {
        if (super.displayUIComponentAfterOneShot()) {
            this.mOutfocusManager.setUIVisibility(0);
            this.mOutfocusManager.setBarEnable(true);
            if (this.mIsNeedInitGuide) {
                this.mIsShowingInitGuide = true;
                this.mOutfocusManager.showGuideText(true, this.mGet.getAppContext().getString(C0088R.string.outfocus_guide_distance));
                postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        boolean z = false;
                        RearOutfocusModuleBase.this.mIsShowingInitGuide = false;
                        String msg = RearOutfocusModuleBase.this.mOutfocusManager.getErrorMessage(RearOutfocusModuleBase.this.mOutfocusState);
                        CamLog.m7i(CameraConstants.TAG, "OutFocus Result " + RearOutfocusModuleBase.this.mOutfocusState);
                        ArcOutfocusManager access$000 = RearOutfocusModuleBase.this.mOutfocusManager;
                        if (msg != null) {
                            z = true;
                        }
                        access$000.showGuideText(z, msg);
                    }
                }, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                this.mIsNeedInitGuide = false;
            }
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    RearOutfocusModuleBase.this.setOutFocusResultCallback(true);
                }
            }, 1000);
        }
        return false;
    }

    public boolean onVolumeKeyLongPressed(int keyCode, KeyEvent event) {
        return true;
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        super.setContentFrameLayoutParam(params);
        this.mOutfocusManager.setLayoutParam(params);
    }

    protected boolean checkEnableRecogSucess() {
        if (!this.mSnapShotChecker.isSnapShotProcessing()) {
            return super.checkEnableRecogSucess();
        }
        CamLog.m3d(CameraConstants.TAG, "exit doVoiceRecogSuccess SnapShotProcessing");
        return false;
    }

    protected Uri doSaveImage(byte[] data, byte[] extraExif, String dir, String filename) {
        if (this.mOutfocusCaptureResult == null) {
            CamLog.m5e(CameraConstants.TAG, " mOutfocusCaptureResult is null");
            return super.doSaveImage(data, extraExif, dir, filename);
        } else if (this.mOutfocusCaptureResult.getErrorType() != 0 || this.mOutfocusCaptureResult.getBluredImage() == null) {
            if (getDialogID() != 5) {
                showToast(this.mGet.getAppContext().getString(C0088R.string.outfocus_effect_save_fail_rear), CameraConstants.TOAST_LENGTH_LONG);
            }
            return super.doSaveImage(this.mOutfocusCaptureResult.getOriginImage(), extraExif, dir, filename);
        } else {
            String jpgFileName = filename + ".jpg";
            ExifInterface exif = Exif.readExif(data);
            exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf((short) 24)));
            int exifDegree = Exif.getOrientation(exif);
            int level = this.mOutfocusManager != null ? this.mOutfocusManager.getBlurLevelForParam() : 70;
            int metaSize = this.mOutfocusCaptureResult.getMetaSize();
            byte[] bluredImage = XMPWriter.insertOutfocusXMP(data, XMPWriter.getOutfousXmpString(this.mOutfocusCaptureResult.getSolutionType(), metaSize, metaSize + this.mOutfocusCaptureResult.getOriginalImageSize(), level, this.mOutfocusCaptureResult.getFocusRegions(), this.mOutfocusCaptureResult.getSensorActiveSize()));
            updateThumbnail(exif, exifDegree, false);
            Uri uri = FileManager.addOutFocusJpegImage(bluredImage, extraExif, this.mGet.getAppContext().getContentResolver(), dir, jpgFileName, System.currentTimeMillis(), this.mLocationServiceManager.getCurrentLocation(), exifDegree, exif, this.mOutfocusCaptureResult.getOriginImage(), this.mOutfocusCaptureResult.getMeta());
            checkSavedURI(uri);
            FileNamer.get().removeFileNameInSaving(dir + jpgFileName);
            CamLog.m3d(CameraConstants.TAG, "[outfocus] jpeg uri = " + uri);
            if (FunctionProperties.isSupportedCameraRoll()) {
                makeThumbnailForTilePreview(exif, uri);
                doSaveImageAfter(uri, 205);
            }
            checkStorage();
            this.mOutfocusCaptureResult = null;
            this.mOutfocusCaptureState = 0;
            return uri;
        }
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (this.mOutfocusManager != null) {
            return extraStr + this.mOutfocusManager.getLDBExtraString() + ";";
        }
        return extraStr;
    }

    public boolean isOutfocusAvailable() {
        return this.mOutfocusState == 0;
    }

    public int getOutfocusErrorTextHeight() {
        if (this.mOutfocusManager == null || isOutfocusAvailable()) {
            return super.getOutfocusErrorTextHeight();
        }
        return this.mOutfocusManager.getOutfocusErrorHeight();
    }

    public void showToast(String message, long hideDelayMillis) {
        super.showToast(message, hideDelayMillis);
        this.mOutfocusManager.showToast(hideDelayMillis);
    }

    public void hideAllToast() {
        super.hideAllToast();
        this.mOutfocusManager.hideToast();
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        String msg = this.mOutfocusManager.getErrorMessage(this.mOutfocusState);
        this.mOutfocusManager.showGuideText(msg != null, msg);
    }
}
