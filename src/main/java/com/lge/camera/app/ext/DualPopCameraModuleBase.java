package com.lge.camera.app.ext;

import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ChildSettingRunnable;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import com.lge.hardware.LGCamera;
import java.util.HashMap;

public class DualPopCameraModuleBase extends DefaultCameraModule {
    protected static final int DUALPOP_ALL_PREVIEW_AVAILABLE = 3;
    protected static final int DUALPOP_NORMAL_PREVIEW_AVAILABLE = 1;
    protected static final int DUALPOP_WIDE_PREVIEW_AVAILABLE = 2;
    protected final int SECOND_SURFACE_DUMMY_HEIGHT = 50;
    protected final int SECOND_SURFACE_DUMMY_WIDTH = 50;
    protected String[] mBackupOriginalEntries;
    protected String[] mBackupOriginalEntryValues;
    public Callback mCallback = new C03612();
    protected ChildSettingRunnable mChildSettingUpdater_dualpopPictureSize = new C03601();
    protected ExifInterface mExif;
    protected SurfaceHolder mHolder;
    protected Handler mHolderHandler = new Handler();
    protected int mIsAllPreviewAvailable = 0;
    protected boolean mIsNormalCaptured = false;
    protected boolean mIsSavingPicture = false;
    protected boolean mIsWideCaptured = false;
    protected LGCamera mLGCamera1;
    protected String mNormalCameraPreviewSize;
    protected String[] mNormalPreviewSizeList;
    protected byte[] mNormalViewCaptureData;
    protected String mPictureSize;
    protected String[] mPictureSizeList;
    protected String mScreenSize;
    protected SurfaceView mSecondSurfaceView = null;
    protected String mWideCameraPreviewSize;
    protected String mWidePictureSize;
    protected String[] mWidePictureSizeList;
    protected String[] mWidePreviewSizeList;
    protected byte[] mWideViewCaptureData;

    /* renamed from: com.lge.camera.app.ext.DualPopCameraModuleBase$1 */
    class C03601 extends ChildSettingRunnable {
        C03601() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "-picsize- mChildSettingUpdater_dualpopPictureSize");
            DualPopCameraModuleBase.this.mCameraDevice.stopPreview();
            CameraSecondHolder.subinstance().stopPreview();
            DualPopCameraModuleBase.this.setSetting(key, value, true);
            ListPreference listPref = prefObject instanceof ListPreference ? (ListPreference) prefObject : null;
            if (listPref != null && DualPopCameraModuleBase.this.mCameraDevice != null && DualPopCameraModuleBase.this.checkModuleValidate(208)) {
                int settingIndex = DualPopCameraModuleBase.this.mGet.getSettingIndex(SettingKeyWrapper.getPictureSizeKey(DualPopCameraModuleBase.this.getShotMode(), DualPopCameraModuleBase.this.mCameraId));
                DualPopCameraModuleBase.this.mPictureSize = DualPopCameraModuleBase.this.mPictureSizeList[settingIndex];
                DualPopCameraModuleBase.this.mNormalCameraPreviewSize = DualPopCameraModuleBase.this.mNormalPreviewSizeList[settingIndex];
                DualPopCameraModuleBase.this.mWideCameraPreviewSize = DualPopCameraModuleBase.this.mWidePreviewSizeList[settingIndex];
                DualPopCameraModuleBase.this.mScreenSize = listPref.getExtraInfo(2, settingIndex);
                CameraParameters parameters = DualPopCameraModuleBase.this.mCameraDevice.getParameters();
                if (!parameters.get("picture-size").equals(DualPopCameraModuleBase.this.mPictureSize)) {
                    DualPopCameraModuleBase.this.mGet.setPreviewCoverVisibility(0, true);
                    DualPopCameraModuleBase.this.showFrameGridView("off", false);
                    DualPopCameraModuleBase.this.mPictureOrVideoSizeChanged = true;
                    String previewSize = parameters.get(ParamConstants.KEY_PREVIEW_SIZE);
                    DualPopCameraModuleBase.this.access$600(key, value, true);
                    DualPopCameraModuleBase.this.setParamUpdater(parameters, "picture-size", DualPopCameraModuleBase.this.mPictureSize);
                    if (!key.equals(SettingKeyWrapper.getVideoSizeKey(DualPopCameraModuleBase.this.getShotMode(), 2))) {
                        DualPopCameraModuleBase.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_SIZE, DualPopCameraModuleBase.this.mNormalCameraPreviewSize);
                        if (FunctionProperties.getSupportedHal() == 2) {
                            int[] size = Utils.sizeStringToArray(DualPopCameraModuleBase.this.mNormalCameraPreviewSize);
                            DualPopCameraModuleBase.this.mGet.setCameraPreviewSize(size[0], size[1]);
                        }
                    }
                    parameters.set(ParamConstants.KEY_HFR, "off");
                    DualPopCameraModuleBase.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
                    DualPopCameraModuleBase.this.setParameters(parameters);
                    if (!(CameraSecondHolder.subinstance() == null || DualPopCameraModuleBase.this.mCameraDevice == null)) {
                        parameters = CameraSecondHolder.subinstance().getParameters();
                        DualPopCameraModuleBase.this.mWidePictureSize = DualPopCameraModuleBase.this.mWidePictureSizeList[settingIndex];
                        if (parameters != null) {
                            parameters.set("picture-size", DualPopCameraModuleBase.this.mWidePictureSize);
                            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, DualPopCameraModuleBase.this.mWideCameraPreviewSize);
                            parameters.set(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(DualPopCameraModuleBase.this.getShotMode()));
                            CameraSecondHolder.subinstance().setParameters(parameters);
                        }
                    }
                    DualPopCameraModuleBase.this.mHandler.sendEmptyMessage(61);
                    DualPopCameraModuleBase.this.mHandler.sendEmptyMessage(112);
                    DualPopCameraModuleBase.this.mGet.setSwitchingAniViewParam(false);
                }
            }
        }

        public boolean checkChildAvailable() {
            if (DualPopCameraModuleBase.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.ext.DualPopCameraModuleBase$2 */
    class C03612 implements Callback {
        C03612() {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            if (DualPopCameraModuleBase.this.isCameraDeviceAvailable()) {
                CamLog.m7i(CameraConstants.TAG, "[dualpop] second surfaceCreated");
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            CamLog.m7i(CameraConstants.TAG, "[dualpop] second surfaceChanged");
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            CamLog.m7i(CameraConstants.TAG, "[dualpop] second surfaceDestroyed");
        }
    }

    public DualPopCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void initializeSettingMenus() {
        this.mGet.setAllSettingMenuEnable(true);
        setDefaultSettingValueAndDisable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        setSpecificSettingValueAndDisable("tracking-af", "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FINGER_DETECTION, "off", false);
        restoreTilePreviewSetting();
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        restoreFlashSetting();
        restoreSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue("tracking-af");
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_FINGER_DETECTION);
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
            } else if (isRearCamera() && this.mBackupOriginalEntries != null && this.mBackupOriginalEntryValues != null) {
                index = listPref.findIndexOfValue(listPref.getValue());
                listPref.setEntryValues(this.mBackupOriginalEntryValues);
                listPref.setEntries(this.mBackupOriginalEntries);
                listPref.setValue(this.mBackupOriginalEntryValues[index]);
                this.mGet.updateSpecificSettingMenu(SettingKeyWrapper.getPictureSizeKey(getShotMode(), 0), (String[]) listPref.getEntryValues(), (String[]) listPref.getEntries(), index);
            }
        }
    }

    protected void changeRequester() {
        super.changeRequester();
        String initZoomValue = "0";
        if (this.mZoomManager != null) {
            initZoomValue = Integer.toString(this.mZoomManager.getZoomValue());
        }
        this.mParamUpdater.addRequester("zoom", initZoomValue, false, true);
        this.mParamUpdater.setParamValue("flash-mode", "off");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_JPEG_STR);
    }

    protected void makePopoutPreviewAndPictureSizeList(CameraParameters normalParam, CameraParameters wideParam) {
        CamLog.m3d(CameraConstants.TAG, "makedualpopPreviewAndPictureSizeList");
        ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), 0));
        if (listPref != null && normalParam != null && wideParam != null) {
            this.mPictureSizeList = ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS;
            this.mWidePictureSizeList = ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_REAR_SUPPORTED_ITEMS;
            this.mWidePreviewSizeList = (String[]) listPref.getExtraInfoByNum(1);
            this.mNormalPreviewSizeList = this.mWidePreviewSizeList;
            int settingIndex = this.mGet.getSettingIndex(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            this.mPictureSize = this.mPictureSizeList[settingIndex];
            this.mWidePictureSize = this.mWidePictureSizeList[settingIndex];
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
            map.put(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), this.mChildSettingUpdater_dualpopPictureSize);
        }
    }

    protected void setupPreview(CameraParameters params) {
        CamLog.m7i(CameraConstants.TAG, "[dualpop] setupPreview");
        if (this.mCameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "[dualpop] Camera device is null!");
        } else if (this.mPictureOrVideoSizeChanged) {
            startPreview(params);
            setCameraState(1);
        } else {
            this.mIsAllPreviewAvailable = 0;
            CameraParameters param = this.mCameraDevice.getParameters();
            param.set(ParamConstants.KEY_DUAL_RECORDER, 1);
            param.set(ParamConstants.KEY_LGE_CAMERA, 1);
            param.set(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(getShotMode()));
            CamLog.m3d(CameraConstants.TAG, "[dualpop] Normal camera pre setParameter");
            startPreview(params);
            setCameraState(1);
            CameraSecondHolder.subinstance().open(this.mHolderHandler, 2, null, false, this.mGet.getActivity());
            makePopoutPreviewAndPictureSizeList(param, CameraSecondHolder.subinstance().getParameters());
            CamLog.m3d(CameraConstants.TAG, "[dualpop] Create dualpop second surfaceView");
            this.mSecondSurfaceView = (SurfaceView) findViewById(C0088R.id.preview_surface_dualpop);
            this.mSecondSurfaceView.getHolder().addCallback(this.mCallback);
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessage(112);
            }
        }
    }

    protected boolean isCameraDeviceAvailable() {
        if (this.mCameraDevice == null || this.mCameraDevice.getCamera() == null) {
            CamLog.m3d(CameraConstants.TAG, "[dualpop] Normal camera is null, so return");
            return false;
        } else if (CameraSecondHolder.subinstance() != null && this.mSecondSurfaceView != null) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "[dualpop] Wide camera is null, so return");
            return false;
        }
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        CamLog.m3d(CameraConstants.TAG, "mainHandlerHandleMessage = " + msg.what);
        switch (msg.what) {
            case 111:
                setSetting(Setting.KEY_DUAL_POP_TYPE, "off", true);
                this.mGet.setPreviewCoverVisibility(0, true);
                boolean isMenuShow = isMenuShowing(CameraConstants.MENU_TYPE_ALL);
                if (isMenuShow) {
                    hideMenu(CameraConstants.MENU_TYPE_ALL, true, false);
                }
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DualPopCameraModuleBase.this.mGet.modeMenuClicked(CameraConstants.MODE_POPOUT_CAMERA);
                    }
                }, isMenuShow ? 150 : 0);
                break;
            case 112:
                if (this.mSecondSurfaceView != null) {
                    CamLog.m3d(CameraConstants.TAG, "[dualpop] set Surfaceview layout param");
                    int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
                    this.mSecondSurfaceView.setLayoutParams(new LayoutParams(50, 50));
                    this.mSecondSurfaceView.setVisibility(0);
                    this.mSecondSurfaceView.setTranslationY((float) (lcdSize[1] + 300));
                    break;
                }
                break;
        }
        return super.mainHandlerHandleMessage(msg);
    }

    public boolean isIndicatorSupported(int indicatorId) {
        if (isRecordingState()) {
            switch (indicatorId) {
                case C0088R.id.indicator_item_hdr_or_flash:
                    return "1".equals(getSettingValue("hdr-mode"));
            }
        }
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
            case C0088R.id.indicator_item_hdr_or_flash:
                return true;
        }
        return false;
    }
}
