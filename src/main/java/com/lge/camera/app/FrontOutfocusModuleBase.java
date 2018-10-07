package com.lge.camera.app;

import android.content.res.Configuration;
import android.net.Uri;
import android.view.KeyEvent;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.ICameraCallback.OutFocusCallback;
import com.lge.camera.device.OutfocusCaptureResult;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.managers.ArcOutfocusManager;
import com.lge.camera.managers.OutfocusManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.XMPWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FrontOutfocusModuleBase extends BeautyShotCameraModule {
    private OutFocusCallback mOutfocusCallback = null;
    private OutfocusCaptureResult mOutfocusCaptureResult = null;
    private int mOutfocusCaptureState = 0;
    private OutfocusManager mOutfocusManager = null;
    private int mOutfocusState = 0;

    /* renamed from: com.lge.camera.app.FrontOutfocusModuleBase$1 */
    class C03151 extends OutFocusCallback {
        C03151() {
        }

        public void onOutFocusResult(int errorType) {
            FrontOutfocusModuleBase.this.updateOutFocusResult(errorType);
        }

        public void onOutFocusCaptureResult(final OutfocusCaptureResult outfocusResult) {
            if (outfocusResult != null) {
                CamLog.m7i(CameraConstants.TAG, "OutFocus Capture Result : " + outfocusResult.getErrorType() + " Result when to start capture : " + FrontOutfocusModuleBase.this.mOutfocusCaptureState);
                FrontOutfocusModuleBase.this.mGet.runOnUiThread(new HandlerRunnable(FrontOutfocusModuleBase.this) {
                    public void handleRun() {
                        if (FrontOutfocusModuleBase.this.mOutfocusCaptureState != 0) {
                            outfocusResult.setErrorType(FrontOutfocusModuleBase.this.mOutfocusCaptureState);
                        }
                        FrontOutfocusModuleBase.this.mOutfocusCaptureResult = outfocusResult;
                    }
                });
            }
        }
    }

    public FrontOutfocusModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
        if (this.mOutfocusManager == null) {
            this.mOutfocusManager = new ArcOutfocusManager(this, 5);
        }
    }

    public String getShotMode() {
        return CameraConstants.MODE_FRONT_OUTFOCUS;
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (this.mOutfocusManager != null) {
            this.mManagerList.add(this.mOutfocusManager);
        }
    }

    protected void changeRequester() {
        super.changeRequester();
        if (this.mParamUpdater != null) {
            if (this.mOutfocusManager != null) {
                this.mOutfocusManager.initParamUpdater(this.mParamUpdater);
            }
            ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            if (listPref != null) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), null));
            }
        }
    }

    protected void oneShotPreviewCallbackDone() {
        setOutfocusCallback(true);
        super.oneShotPreviewCallbackDone();
    }

    private void setOutfocusCallback(boolean isSet) {
        if (this.mCameraDevice != null) {
            CamLog.m7i(CameraConstants.TAG, "[outfocus] setOutfocusCallback : " + isSet);
            if (!isSet) {
                this.mOutfocusCallback = null;
            } else if (this.mOutfocusCallback == null) {
                this.mOutfocusCallback = new C03151();
            }
            this.mCameraDevice.setOutFocusCallback(this.mOutfocusCallback);
        }
    }

    public void updateOutFocusResult(final int errorType) {
        if (this.mOutfocusManager != null && this.mOutfocusManager.isUpdateNewState(errorType, this.mOutfocusState)) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    FrontOutfocusModuleBase.this.mOutfocusState = errorType;
                    CamLog.m7i(CameraConstants.TAG, "[outfocus] onOutfocusMetaData : " + FrontOutfocusModuleBase.this.mOutfocusState);
                    if (FrontOutfocusModuleBase.this.mOutfocusManager != null) {
                        String msg = FrontOutfocusModuleBase.this.mOutfocusManager.getErrorMessage(FrontOutfocusModuleBase.this.mOutfocusState);
                        if (msg != null) {
                            FrontOutfocusModuleBase.this.pauseShutterless();
                        } else {
                            FrontOutfocusModuleBase.this.resumeShutterless();
                        }
                        FrontOutfocusModuleBase.this.mOutfocusManager.showGuideText(msg != null, msg);
                    }
                }
            });
        }
    }

    protected String getPreviewSize(String previewSize, String screenSize, String videoSize) {
        if ("".equals(previewSize) || ConfigurationUtil.sFRONT_OUTFOCUS_PREVIEW_SIZE == null) {
            return super.getPreviewSize(previewSize, screenSize, videoSize);
        }
        ListPreference listPref = getListPreference("picture-size");
        int index = listPref.findIndexOfValue(listPref.getValue());
        if (index != -1) {
            return super.getPreviewSize(ConfigurationUtil.sFRONT_OUTFOCUS_PREVIEW_SIZE[index], screenSize, videoSize);
        }
        CamLog.m5e(CameraConstants.TAG, "[outfocus] get preview size - index error");
        return super.getPreviewSize(previewSize, screenSize, videoSize);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
    }

    protected void closeCamera() {
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.restoreParam(this.mParamUpdater);
        }
        super.closeCamera();
    }

    public void onPauseAfter() {
        setOutfocusCallback(false);
        super.onPauseAfter();
    }

    public void onDestroy() {
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.removeParamUpdater(this.mParamUpdater);
        }
        super.onDestroy();
    }

    public int getShutterButtonType() {
        return 4;
    }

    public void onShutterLargeButtonClicked() {
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
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.setUIVisibility(8);
        }
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
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.setUIVisibility(0);
        }
        return true;
    }

    protected void onTakePictureBefore() {
        super.onTakePictureBefore();
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.setBarEnable(false);
        }
    }

    protected void doTakePicture() {
        super.doTakePicture();
        this.mOutfocusCaptureState = this.mOutfocusState;
    }

    protected void onTakePictureAfter() {
        super.onTakePictureAfter();
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.setBarEnable(true);
        }
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIGHTFRAME, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        restoreSettingValue("hdr-mode");
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_SIGNATURE);
        if (!"1".equals(getSettingValue("hdr-mode"))) {
            restoreFlashSetting();
        } else if (getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        restoreSettingValue(Setting.KEY_LIGHTFRAME);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected void stopPreview() {
        runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                FrontOutfocusModuleBase.this.mOutfocusState = 0;
                FrontOutfocusModuleBase.this.mOutfocusManager.showGuideText(false, null);
            }
        });
        super.stopPreview();
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected Uri doSaveImage(byte[] data, byte[] extraExif, String dir, String filename) {
        if (this.mCameraDevice == null) {
            return null;
        }
        if (FunctionProperties.getSupportedHal() == 2) {
            return doSaveImageForApi2(data, extraExif, dir, filename);
        }
        if (this.mOutfocusState == 4) {
            CamLog.m3d(CameraConstants.TAG, "[outfocus] low light status - not insert xmp");
            return super.doSaveImage(data, extraExif, dir, filename);
        }
        List<String> itemList = getOutfocusExtraData(data);
        if (itemList == null) {
            showToast(this.mGet.getAppContext().getString(C0088R.string.outfocus_effect_save_fail), CameraConstants.TOAST_LENGTH_LONG);
            return super.doSaveImage(data, extraExif, dir, filename);
        }
        String jpgFileName = filename + ".jpg";
        ExifInterface exif = Exif.readExif(data);
        exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf((short) 21)));
        int exifDegree = Exif.getOrientation(exif);
        updateThumbnail(exif, exifDegree, false);
        byte[] cut = Arrays.copyOfRange(data, 0, data.length - 32);
        byte[] convertJpeg = this.mFlipManager.checkPostProcessAndMakeJpegFlip(this.mCameraId, exifDegree, cut, exif);
        Uri uri = FileManager.addOutFocusJpegImage(XMPWriter.insertOutfocusXMP(cut, XMPWriter.getOutfousXmpString(itemList, this.mOutfocusManager != null ? this.mOutfocusManager.getBlurLevelForParam() : 50)), extraExif, this.mGet.getAppContext().getContentResolver(), dir, jpgFileName, System.currentTimeMillis(), this.mLocationServiceManager.getCurrentLocation(), exifDegree, exif, null, null);
        checkSavedURI(uri);
        FileNamer.get().removeFileNameInSaving(dir + jpgFileName);
        CamLog.m3d(CameraConstants.TAG, "[outfocus] jpeg uri = " + uri);
        if (FunctionProperties.isSupportedCameraRoll()) {
            makeThumbnailForTilePreview(exif, uri);
            doSaveImageAfter(uri, 202);
        }
        checkStorage();
        return uri;
    }

    private Uri doSaveImageForApi2(byte[] data, byte[] extraExif, String dir, String filename) {
        if (this.mOutfocusCaptureResult == null) {
            CamLog.m5e(CameraConstants.TAG, " mOutfocusCaptureResult is null");
            return super.doSaveImage(data, extraExif, dir, filename);
        } else if (this.mOutfocusCaptureResult.getErrorType() != 0 || this.mOutfocusCaptureResult.getBluredImage() == null) {
            if (getDialogID() != 5) {
                showToast(this.mGet.getAppContext().getString(C0088R.string.outfocus_effect_save_fail), CameraConstants.TOAST_LENGTH_LONG);
            }
            return super.doSaveImage(this.mOutfocusCaptureResult.getOriginImage(), extraExif, dir, filename);
        } else {
            String jpgFileName = filename + ".jpg";
            ExifInterface exif = Exif.readExif(data);
            exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf((short) 21)));
            int exifDegree = Exif.getOrientation(exif);
            int level = this.mOutfocusManager != null ? this.mOutfocusManager.getBlurLevelForParam() : 50;
            int metaSize = this.mOutfocusCaptureResult.getMetaSize();
            byte[] bluredImage = XMPWriter.insertOutfocusXMP(data, XMPWriter.getOutfousXmpString(this.mOutfocusCaptureResult.getSolutionType(), metaSize, metaSize + this.mOutfocusCaptureResult.getOriginalImageSize(), level, this.mOutfocusCaptureResult.getFocusRegions(), this.mOutfocusCaptureResult.getSensorActiveSize()));
            updateThumbnail(exif, exifDegree, false);
            Uri uri = FileManager.addOutFocusJpegImage(bluredImage, extraExif, this.mGet.getAppContext().getContentResolver(), dir, jpgFileName, System.currentTimeMillis(), this.mLocationServiceManager.getCurrentLocation(), exifDegree, exif, this.mOutfocusCaptureResult.getOriginImage(), this.mOutfocusCaptureResult.getMeta());
            checkSavedURI(uri);
            FileNamer.get().removeFileNameInSaving(dir + jpgFileName);
            CamLog.m3d(CameraConstants.TAG, "[outfocus] jpeg uri = " + uri);
            if (FunctionProperties.isSupportedCameraRoll()) {
                makeThumbnailForTilePreview(exif, uri);
                doSaveImageAfter(uri, 202);
            }
            checkStorage();
            this.mOutfocusCaptureResult = null;
            this.mOutfocusCaptureState = 0;
            return uri;
        }
    }

    private List<String> getOutfocusExtraData(byte[] data) {
        byte[] copy = data;
        int length = copy.length;
        List<String> itemList = new ArrayList();
        byte[] cut = Arrays.copyOfRange(copy, length - 32, length);
        for (int i = 0; i < 4; i++) {
            int from = i * 8;
            String value = "";
            for (byte b : Arrays.copyOfRange(cut, from, from + 8)) {
                if (b != (byte) 0) {
                    value = value + ((char) (b & 255));
                }
            }
            switch (i) {
                case 0:
                    if (CameraConstantsEx.OUTFOCUS_SOLUTION_TYPE_ARC.equals(value)) {
                        break;
                    }
                    CamLog.m3d(CameraConstants.TAG, "[outfocus] low light engine is running onTakePicture - not insert xmp : " + value);
                    return null;
                case 2:
                    if (!"0".equals(value)) {
                        break;
                    }
                    CamLog.m3d(CameraConstants.TAG, "[outfocus] subject can not be recognized - not insert xmp");
                    return null;
                default:
                    break;
            }
            CamLog.m3d(CameraConstants.TAG, "[outfocus] " + value);
            itemList.add(value);
        }
        return itemList;
    }

    public boolean onVolumeKeyLongPressed(int keyCode, KeyEvent event) {
        return true;
    }

    protected boolean checkEnableRecogSucess() {
        if (!this.mSnapShotChecker.isSnapShotProcessing()) {
            return super.checkEnableRecogSucess();
        }
        CamLog.m3d(CameraConstants.TAG, "exit doVoiceRecogSuccess SnapShotProcessing");
        return false;
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.setUIVisibility(doByAction ? 8 : 0);
        }
    }

    public boolean displayUIComponentAfterOneShot() {
        if (super.displayUIComponentAfterOneShot() && this.mOutfocusManager != null) {
            this.mOutfocusManager.setUIVisibility(0);
            this.mOutfocusManager.setBarEnable(true);
        }
        return false;
    }

    public boolean checkQuickButtonAvailable() {
        if (this.mOutfocusManager == null || this.mOutfocusManager.isOutfocusBarTouched()) {
            return false;
        }
        return super.checkQuickButtonAvailable();
    }

    protected boolean isSettingOpen() {
        if (this.mOutfocusManager == null) {
            return super.isSettingOpen();
        }
        if (this.mOutfocusManager.isBarTextVisible()) {
            CamLog.m3d(CameraConstants.TAG, "-sh- Outfocus bar text is visible, it's not available to resume Shutter-less");
            return true;
        } else if (!this.mOutfocusManager.isOutfocusBarTouched()) {
            return super.isSettingOpen();
        } else {
            CamLog.m3d(CameraConstants.TAG, "-sh- touching Outfocus bar, it's not available to resume Shutter-less");
            return true;
        }
    }

    protected boolean isShutterlessAvailable() {
        if (this.mOutfocusState != 0) {
            return false;
        }
        return super.isShutterlessAvailable();
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        super.setContentFrameLayoutParam(params);
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.setLayoutParam(params);
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

    public boolean isIntervalShotEnableCondition() {
        return false;
    }

    public void showToast(String message, long hideDelayMillis) {
        super.showToast(message, hideDelayMillis);
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.showToast(hideDelayMillis);
        }
    }

    public void hideAllToast() {
        super.hideAllToast();
        if (this.mOutfocusManager != null) {
            this.mOutfocusManager.hideToast();
        }
    }

    protected int getCropZoomAnimationTime() {
        return 600;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        String msg = this.mOutfocusManager.getErrorMessage(this.mOutfocusState);
        this.mOutfocusManager.showGuideText(msg != null, msg);
    }
}
