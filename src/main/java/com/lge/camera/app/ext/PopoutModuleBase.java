package com.lge.camera.app.ext;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ext.PopoutCameraManager;
import com.lge.camera.managers.ext.PopoutCameraManager.onPopoutBtnListener;
import com.lge.camera.managers.ext.PopoutHelper;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ChildSettingRunnable;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import com.lge.hardware.LGCamera;
import java.util.HashMap;

public class PopoutModuleBase extends DefaultCameraModule implements onPopoutBtnListener {
    protected final float FULLSCREEN_TO_INNERFRAME_RATIO_CIRCLE = 0.72f;
    protected final float FULLSCREEN_TO_INNERFRAME_RATIO_DIVISION_HORI = 0.55f;
    protected final float FULLSCREEN_TO_INNERFRAME_RATIO_DIVISION_VERTI = 0.55f;
    protected final float FULLSCREEN_TO_INNERFRAME_RATIO_HEXAGON = 0.75f;
    protected final float FULLSCREEN_TO_INNERFRAME_RATIO_RECT = 0.625f;
    protected final int PREVIEW_RATIO_16_BY_9 = 0;
    protected final int PREVIEW_RATIO_18_BY_9 = 3;
    protected final int PREVIEW_RATIO_1_BY_1 = 2;
    protected final int PREVIEW_RATIO_4_BY_3 = 1;
    protected String[] mBackupOriginalEntries;
    protected String[] mBackupOriginalEntryValues;
    protected ChildSettingRunnable mChildSettingUpdater_popoutPictureSize = new C04651();
    protected int mCurrentBackgroundEffect = 2;
    protected int mCurrentFrameShape = 0;
    protected SurfaceHolder mHolder;
    protected Handler mHolderHandler = new Handler();
    protected boolean mIsMultiPopoutMode = true;
    protected boolean mIsOriginalListBackuped = false;
    protected boolean mIsSavingPicture = false;
    protected int[] mLCDSize;
    protected LGCamera mLGCamera1;
    protected String mNormalCameraPreviewSize;
    protected String[] mNormalPreviewSizeList;
    protected String mPictureSize;
    protected String[] mPictureSizeList;
    protected PopoutCameraManager mPopoutCameraManager = new PopoutCameraManager(this);
    protected PopoutHelper mPopoutHelper = new PopoutHelper();
    protected SurfaceView mPopoutSurfaceView;
    protected int mPreviewRatio = 0;
    protected String mScreenSize;
    protected int mShiftUpTouchArea = 0;
    protected int mTouchX;
    protected int mTouchY;
    protected String mWideCameraPreviewSize;
    protected String[] mWidePreviewSizeList;

    /* renamed from: com.lge.camera.app.ext.PopoutModuleBase$1 */
    class C04651 extends ChildSettingRunnable {
        C04651() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "-picsize- mChildSettingUpdater_popoutPictureSize");
            PopoutModuleBase.this.setSetting(key, value, true);
            ListPreference listPref = prefObject instanceof ListPreference ? (ListPreference) prefObject : null;
            if (listPref != null && PopoutModuleBase.this.mCameraDevice != null && PopoutModuleBase.this.checkModuleValidate(208)) {
                PopoutModuleBase.this.access$100(listPref, value);
                int settingIndex = PopoutModuleBase.this.mGet.getSettingIndex(SettingKeyWrapper.getPictureSizeKey(PopoutModuleBase.this.getShotMode(), PopoutModuleBase.this.mCameraId));
                PopoutModuleBase.this.mPictureSize = PopoutModuleBase.this.mPictureSizeList[settingIndex];
                PopoutModuleBase.this.mNormalCameraPreviewSize = PopoutModuleBase.this.mNormalPreviewSizeList[settingIndex];
                PopoutModuleBase.this.mWideCameraPreviewSize = PopoutModuleBase.this.mWidePreviewSizeList[settingIndex];
                PopoutModuleBase.this.mScreenSize = listPref.getExtraInfo(2, settingIndex);
                CameraParameters parameters = PopoutModuleBase.this.mCameraDevice.getParameters();
                if (!parameters.get("picture-size").equals(PopoutModuleBase.this.mPictureSize)) {
                    PopoutModuleBase.this.mCameraDevice.stopPreview();
                    CameraSecondHolder.subinstance().stopPreview();
                    PopoutModuleBase.this.releasePopoutEngine();
                    PopoutModuleBase.this.mPopoutSurfaceView.setVisibility(8);
                    PopoutModuleBase.this.mGet.setPreviewCoverVisibility(0, true);
                    PopoutModuleBase.this.showFrameGridView("off", false);
                    PopoutModuleBase.this.mPictureOrVideoSizeChanged = true;
                    String previewSize = parameters.get(ParamConstants.KEY_PREVIEW_SIZE);
                    if (!(previewSize == null || previewSize.compareTo(PopoutModuleBase.this.mNormalCameraPreviewSize) == 0)) {
                        PopoutModuleBase.this.access$700(false);
                    }
                    PopoutModuleBase.this.access$800(key, value, true);
                    PopoutModuleBase.this.setParamUpdater(parameters, "picture-size", PopoutModuleBase.this.mPictureSize);
                    if (!key.equals(SettingKeyWrapper.getVideoSizeKey(PopoutModuleBase.this.getShotMode(), 2))) {
                        PopoutModuleBase.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_SIZE, PopoutModuleBase.this.mNormalCameraPreviewSize);
                    }
                    parameters.set(ParamConstants.KEY_HFR, "off");
                    PopoutModuleBase.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
                    PopoutModuleBase.this.setParameters(parameters);
                    if (!(CameraSecondHolder.subinstance() == null || PopoutModuleBase.this.mCameraDevice == null)) {
                        parameters = CameraSecondHolder.subinstance().getParameters();
                        if (parameters != null) {
                            parameters.set("picture-size", PopoutModuleBase.this.mPictureSize);
                            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, PopoutModuleBase.this.mWideCameraPreviewSize);
                            CameraSecondHolder.subinstance().setParameters(parameters);
                        }
                    }
                    PopoutModuleBase.this.mHandler.sendEmptyMessage(61);
                    PopoutModuleBase.this.mHandler.sendEmptyMessage(86);
                }
            }
        }

        public boolean checkChildAvailable() {
            if (PopoutModuleBase.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    public PopoutModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        if (FunctionProperties.getCameraTypeRear() == 0) {
            this.mIsMultiPopoutMode = false;
        }
        this.mIsOriginalListBackuped = false;
        this.mLCDSize = Utils.getLCDsize(getAppContext(), true);
        this.mGet.setPreviewCoverVisibility(0, true);
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        this.mGet.movePreviewOutOfWindow(true);
        if (this.mGet.getPreviewCoverVisibility() == 8 || this.mGet.getPreviewCoverVisibility() == 4) {
            this.mGet.setPreviewCoverVisibility(0, true);
        }
        this.mPopoutCameraManager.initPopoutCameraLayout(this.mCurrentBackgroundEffect);
        this.mPopoutCameraManager.setOnPopoutBtnListener(this);
    }

    protected void initializeSettingMenus() {
        this.mGet.setAllSettingMenuEnable(true);
        setSpecificSettingValueAndDisable(Setting.KEY_FRAME_GRID, "off", false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        setSpecificSettingValueAndDisable("tracking-af", "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FINGER_DETECTION, "off", false);
        if (ModelProperties.isMTKChipset()) {
            this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), "1920x1080", false);
            if ("1920x1080".equals(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)))) {
                this.mGet.setSetting(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), "1280x720", false);
            }
        } else {
            this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), ParamConstants.VIDEO_3840_BY_2160, false);
            this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), CameraConstants.VIDEO_FHD_60FPS, false);
            if (isUHDmode() || isFHD60()) {
                this.mGet.setSetting(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).getDefaultValue(), false);
            }
        }
        restoreTilePreviewSetting();
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        restoreFlashSetting();
        restoreSettingValue(Setting.KEY_FRAME_GRID);
        restoreSettingValue("hdr-mode");
        restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue("tracking-af");
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_FINGER_DETECTION);
        for (String size : ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS) {
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, size, true);
        }
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
        setPictureSizeListAndSettingMenu(false);
    }

    public void setPictureSizeListAndSettingMenu(boolean set) {
        ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), 0));
        if (listPref != null) {
            int index;
            if (set) {
                if (!this.mIsOriginalListBackuped) {
                    this.mBackupOriginalEntries = (String[]) listPref.getEntries();
                    this.mBackupOriginalEntryValues = (String[]) listPref.getEntryValues();
                    index = listPref.findIndexOfValue(listPref.getValue());
                    String[] newEntry = new String[this.mBackupOriginalEntries.length];
                    for (int i = 0; i < newEntry.length; i++) {
                        newEntry[i] = this.mBackupOriginalEntries[i].split(" ")[0] + " (" + Utils.getMegaPixelOfPictureSize(this.mPictureSizeList[i], i) + ") " + this.mPictureSizeList[i];
                    }
                    listPref.setEntryValues(this.mPictureSizeList);
                    listPref.setEntries(newEntry);
                    listPref.setValue(this.mPictureSizeList[index]);
                    this.mGet.updateSpecificSettingMenu(SettingKeyWrapper.getPictureSizeKey(getShotMode(), 0), this.mPictureSizeList, newEntry, index);
                    this.mIsOriginalListBackuped = true;
                }
            } else if (isRearCamera() && this.mBackupOriginalEntries != null && this.mBackupOriginalEntryValues != null) {
                index = listPref.findIndexOfValue(listPref.getValue());
                listPref.setEntryValues(this.mBackupOriginalEntryValues);
                listPref.setEntries(this.mBackupOriginalEntries);
                listPref.setValue(this.mBackupOriginalEntryValues[index]);
                this.mGet.updateSpecificSettingMenu(SettingKeyWrapper.getPictureSizeKey(getShotMode(), 0), (String[]) listPref.getEntryValues(), (String[]) listPref.getEntries(), index);
            }
        }
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mPopoutCameraManager);
    }

    protected void changeRequester() {
        super.changeRequester();
        String initZoomValue = "0";
        if (this.mZoomManager != null) {
            initZoomValue = Integer.toString(this.mZoomManager.getZoomValue());
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_DUAL_RECORDER, "1");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, "off");
        this.mParamUpdater.addRequester("zoom", initZoomValue, false, true);
        this.mParamUpdater.setParamValue("hdr-mode", "0");
        this.mParamUpdater.setParamValue("flash-mode", "off");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_JPEG_STR);
    }

    protected void makePopoutPreviewAndPictureSizeList(CameraParameters normalParam, CameraParameters wideParam) {
        CamLog.m3d(CameraConstants.TAG, "makePopoutPreviewAndPictureSizeList");
        ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), 0));
        if (listPref != null && normalParam != null && wideParam != null) {
            this.mPictureSizeList = this.mPopoutHelper.getPictureSizeList(getAppContext(), normalParam, wideParam, listPref);
            this.mWidePreviewSizeList = (String[]) listPref.getExtraInfoByNum(1);
            this.mNormalPreviewSizeList = this.mPopoutHelper.getReducedPreviewSize(this.mWidePreviewSizeList);
            int settingIndex = this.mGet.getSettingIndex(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            this.mPictureSize = this.mPictureSizeList[settingIndex];
            this.mWideCameraPreviewSize = this.mWidePreviewSizeList[settingIndex];
            this.mNormalCameraPreviewSize = this.mWideCameraPreviewSize;
            if (this.mNormalPreviewSizeList != null) {
                this.mNormalCameraPreviewSize = this.mNormalPreviewSizeList[settingIndex];
            }
            this.mScreenSize = listPref.getExtraInfo(2, settingIndex);
            this.mParamUpdater.setParamValue("picture-size", this.mPictureSize);
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, this.mNormalCameraPreviewSize);
        }
    }

    protected void addModuleChildSettingMap(HashMap<String, ChildSettingRunnable> map) {
        super.addModuleChildSettingMap(map);
        if (map != null) {
            map.remove(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            map.put(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), this.mChildSettingUpdater_popoutPictureSize);
        }
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        CamLog.m3d(CameraConstants.TAG, "mainHandlerHandleMessage = " + msg.what);
        switch (msg.what) {
            case 86:
                if (this.mPopoutSurfaceView != null) {
                    CamLog.m3d(CameraConstants.TAG, "[popout] set Surfaceview layout param");
                    int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
                    this.mPopoutSurfaceView.setLayoutParams(new LayoutParams(lcdSize[1], lcdSize[0]));
                    this.mPopoutSurfaceView.setVisibility(0);
                    break;
                }
                break;
            case 110:
                setSetting(Setting.KEY_DUAL_POP_TYPE, "on", true);
                this.mGet.setPreviewCoverVisibility(0, true);
                boolean isMenuShow = isMenuShowing(CameraConstants.MENU_TYPE_ALL);
                if (isMenuShow) {
                    access$400(CameraConstants.MENU_TYPE_ALL, true, false);
                }
                setPopoutLayoutVisibility(false);
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        PopoutModuleBase.this.mGet.modeMenuClicked(CameraConstants.MODE_DUAL_POP_CAMERA);
                    }
                }, isMenuShow ? 150 : 0);
                break;
        }
        return super.mainHandlerHandleMessage(msg);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 1 && this.mPopoutCameraManager != null) {
            if (!this.mPopoutCameraManager.isEffectButtonVisible() && !isRecordingState() && !isTimerShotCountdown()) {
                this.mPopoutCameraManager.setEffectButtonVisibility(true);
            } else if (this.mPopoutCameraManager.isFrameLayoutOpen()) {
                this.mPopoutCameraManager.setVisibilityFrameChildMenu(false, true);
            }
        }
        this.mTouchX = (int) event.getX();
        this.mTouchY = (int) event.getY();
        return super.onTouchEvent(event);
    }

    protected boolean isFocusOnTouchEvent() {
        return super.isFocusOnTouchEvent() && isInnerFrameTouched();
    }

    private boolean isInnerFrameTouched() {
        int[] innerFrameSize = (int[]) this.mLCDSize.clone();
        float[] innerFrameRatio = new float[]{1.0f, 1.0f};
        Drawable focusView = this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_focus_rear_af_ae);
        int focusWidth = 0;
        if (focusView != null) {
            focusWidth = (int) ((((float) focusView.getIntrinsicWidth()) * 1.0f) / 2.0f);
        }
        if (this.mCurrentFrameShape == 1) {
            return isCircleInnerFrameTouched(this.mLCDSize, focusWidth);
        }
        if (this.mCurrentFrameShape == 2) {
            return isHexagonInnerFrameTouched(this.mLCDSize, focusWidth);
        }
        if (this.mCurrentFrameShape == 0) {
            innerFrameRatio[0] = 0.625f;
            innerFrameRatio[1] = 0.625f;
        } else if (this.mCurrentFrameShape == 3) {
            innerFrameRatio[0] = 0.55f;
        } else if (this.mCurrentFrameShape == 4) {
            innerFrameRatio[1] = 0.55f;
        }
        switch (this.mPreviewRatio) {
            case 0:
                innerFrameSize[0] = (innerFrameSize[1] * 16) / 9;
                break;
            case 1:
                innerFrameSize[0] = (innerFrameSize[1] * 4) / 3;
                break;
            case 2:
                innerFrameSize[0] = innerFrameSize[1];
                break;
        }
        innerFrameSize[0] = (int) (((float) innerFrameSize[0]) * innerFrameRatio[0]);
        innerFrameSize[1] = (int) (((float) innerFrameSize[1]) * innerFrameRatio[1]);
        int innerFrameStartX = ((this.mLCDSize[1] - innerFrameSize[1]) / 2) + focusWidth;
        int innerFrameStartY = (((this.mLCDSize[0] - innerFrameSize[0]) / 2) + focusWidth) - this.mShiftUpTouchArea;
        return new Rect(innerFrameStartX, innerFrameStartY, (innerFrameSize[1] + innerFrameStartX) - (focusWidth * 2), (innerFrameSize[0] + innerFrameStartY) - (focusWidth * 2)).contains(this.mTouchX, this.mTouchY);
    }

    private boolean isHexagonInnerFrameTouched(int[] lcdSize, int focusWidth) {
        if (((int) Math.sqrt(Math.pow((double) (this.mTouchX - (lcdSize[1] / 2)), 2.0d) + Math.pow((double) (this.mTouchY - ((lcdSize[0] / 2) - this.mShiftUpTouchArea)), 2.0d))) + ((int) Math.sqrt(Math.pow((double) focusWidth, 2.0d) * 2.0d)) < ((int) (((double) (((((float) lcdSize[1]) * 0.75f) / 2.0f) / 2.0f)) * Math.sqrt(3.0d)))) {
            return true;
        }
        return false;
    }

    private boolean isCircleInnerFrameTouched(int[] lcdSize, int focusWidth) {
        if (((float) (((int) Math.sqrt(Math.pow((double) (this.mTouchX - (lcdSize[1] / 2)), 2.0d) + Math.pow((double) (this.mTouchY - ((lcdSize[0] / 2) - this.mShiftUpTouchArea)), 2.0d))) + ((int) Math.sqrt(Math.pow((double) focusWidth, 2.0d) * 2.0d)))) < (((float) lcdSize[1]) * 0.72f) / 2.0f) {
            return true;
        }
        return false;
    }

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    public String getRecordingType() {
        return CameraConstants.VIDEO_POOPUT_TYPE;
    }

    protected void setPopoutEffect() {
        int blurStrength = ModelProperties.isMTKChipset() ? 4 : 10;
        setPopoutDrawMode(new float[]{0.314f}, new float[]{0.26f, 0.26f, 0.26f}, new float[]{0.41f, 0.7f, 0.7f, 0.61f}, new float[]{(float) blurStrength});
    }

    protected void setPopoutDrawMode(float[] perspectiveValue, float[] grayValue, float[] vignettingValue, float[] blurValue) {
    }

    public void changePopoutBackgrondEffect(int type) {
        if (type == 0) {
            this.mCurrentBackgroundEffect = type;
        } else {
            this.mCurrentBackgroundEffect ^= type;
        }
    }

    public boolean onPopoutEffectBtnClick(int type) {
        return true;
    }

    protected void onTakePictureBefore() {
        if (isTimerShotCountdown()) {
            setPopoutLayoutVisibility(false);
        } else if (this.mPopoutCameraManager != null) {
            this.mPopoutCameraManager.setEffectButtonEnable(false);
            this.mPopoutCameraManager.setFrameEnabled(false);
            this.mPopoutCameraManager.setVisibilityFrameChildMenu(false, false);
        }
        super.onTakePictureBefore();
    }

    protected void onTakePictureAfter() {
        if (this.mPopoutCameraManager != null) {
            this.mPopoutCameraManager.setEffectButtonEnable(true);
            this.mPopoutCameraManager.setFrameEnabled(true);
        }
        LdbUtil.setMultiShotState(0);
        super.access$100();
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (this.mPopoutCameraManager == null) {
            return extraStr + LdbConstants.LDB_LOOP_RECORDING_NONE;
        }
        return extraStr + "popout_mode=" + this.mPopoutHelper.getPopoutLDBIntentString(this.mCurrentBackgroundEffect, this.mPopoutCameraManager.getPopoutFrameLDBString()) + ";";
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
    }

    protected void changeToManualCamera() {
        boolean isForceChanged;
        if (isOpticZoomSupported(null) != isOpticZoomSupported(CameraConstants.MODE_MANUAL_CAMERA)) {
            isForceChanged = true;
        } else {
            isForceChanged = false;
        }
        changeCameraIdForManualMode(isForceChanged);
        this.mGet.setCurrentConeMode(2, true);
        this.mGet.setPreviewCoverVisibility(0, true);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                PopoutModuleBase.this.setSetting(Setting.KEY_MODE, "mode_normal", true);
                PopoutModuleBase.this.mGet.modeMenuClicked(CameraConstants.MODE_MANUAL_CAMERA);
            }
        });
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
        if (this.mPopoutCameraManager != null) {
            this.mPopoutCameraManager.setEffectButtonEnable(true);
            this.mPopoutCameraManager.setFrameEnabled(true);
        }
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    public boolean isZoomAvailable() {
        boolean result = super.isZoomAvailable();
        if (result || checkModuleValidate(64)) {
            return result;
        }
        return true;
    }

    protected void updateRecordingFlashState(boolean isRecordingStart, boolean hasAutoMode) {
    }

    public void doTouchAFInRecording() {
        super.doTouchAFInRecording();
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_flash, 4, false);
            this.mQuickButtonManager.setRecordingRotateLayoutMargin(this.mGet.getOrientationDegree());
        }
    }

    public boolean doBackKey() {
        this.mPopoutCameraManager.doBackKey();
        return super.doBackKey();
    }

    public void onPopoutFrameBtnClick(int frame) {
    }

    public void prepareRecordingVideo() {
        super.prepareRecordingVideo();
        if (isTimerShotCountdown()) {
            setPopoutLayoutVisibility(false);
        } else if (this.mPopoutCameraManager != null) {
            this.mPopoutCameraManager.setEffectButtonEnable(false);
            this.mPopoutCameraManager.setFrameEnabled(false);
            this.mPopoutCameraManager.setVisibilityFrameChildMenu(false, false);
        }
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        if (this.mPopoutCameraManager != null) {
            this.mPopoutCameraManager.setEffectButtonEnable(true);
            this.mPopoutCameraManager.setFrameEnabled(true);
        }
    }

    public Rect getRealFocusWindow(Rect mFocusRect, int mFocusAreaWidth, int mFocusAreaHeight, int x, int y) {
        Rect rect = new Rect();
        rect = mFocusRect;
        int popoutDiffX = ((int) ((((float) (x - (mFocusAreaWidth / 2))) * getCurInverseNumber()) + ((float) (mFocusAreaWidth / 2)))) - x;
        int popoutDiffY = ((int) ((((float) (y - (mFocusAreaHeight / 2))) * getCurInverseNumber()) + ((float) (mFocusAreaHeight / 2)))) - y;
        rect.set(mFocusRect.left + popoutDiffX, mFocusRect.top + popoutDiffY, mFocusRect.right + popoutDiffX, mFocusRect.bottom + popoutDiffY);
        return rect;
    }

    private float getCurInverseNumber() {
        float inverseNum = 1.0f;
        if (this.mCurrentFrameShape == 0) {
            inverseNum = 0.625f;
        } else if (this.mCurrentFrameShape == 1) {
            inverseNum = 0.72f;
        } else if (this.mCurrentFrameShape == 2) {
            inverseNum = 0.75f;
        }
        return 1.0f / inverseNum;
    }

    protected void setPopoutLayoutVisibility(boolean visible) {
        if (this.mPopoutCameraManager != null) {
            this.mPopoutCameraManager.setEffectButtonVisibility(visible);
            this.mPopoutCameraManager.setFrameVisibility(visible);
        }
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        setPopoutLayoutVisibility(false);
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL)) {
            return true;
        }
        setPopoutLayoutVisibility(true);
        return true;
    }

    protected boolean useAFTrackingModule() {
        return false;
    }

    protected boolean isFingerDetectionSupportedMode() {
        return false;
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    public boolean isHALSignatureCaptureMode() {
        return false;
    }

    protected void releasePopoutEngine() {
    }

    public boolean isIndicatorSupported(int indicatorId) {
        if (!isRecordingState()) {
            switch (indicatorId) {
                case C0088R.id.indicator_item_cheese_shutter_or_timer:
                    return true;
            }
        }
        return false;
    }

    protected boolean isPauseWaitDuringShot() {
        return false;
    }
}
