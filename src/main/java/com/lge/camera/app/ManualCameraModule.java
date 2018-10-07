package com.lge.camera.app;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.DngCreator;
import android.net.Uri;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraPictureCallbackForDng;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.MediaSaveService.OnLocalSaveListener;
import com.lge.camera.managers.HistogramManager;
import com.lge.camera.managers.InclinometerManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ChildSettingRunnable;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class ManualCameraModule extends ManualBaseModule {
    protected ChildSettingRunnable mChildSettingUpdater_ManualPictureSize = new C03213();
    protected ChildSettingRunnable mChildSettingUpdater_histogram = new C03224();
    protected ChildSettingRunnable mChildSettingUpdater_inclinometer = new C03235();
    protected ChildSettingRunnable mChildSettingUpdater_noise_reduction = new C03202();
    protected ChildSettingRunnable mChildSettingUpdater_saving_raw_picture = new C03191();
    protected HistogramManager mHistogramManager = new HistogramManager(this);
    protected boolean mIsRawIndicatorShown;
    protected InclinometerManager mLevelControlManager = new InclinometerManager(this);
    protected int mRawSavingCnt;

    /* renamed from: com.lge.camera.app.ManualCameraModule$1 */
    class C03191 extends ChildSettingRunnable {
        C03191() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            if (ManualCameraModule.this.mCameraDevice != null) {
                ManualCameraModule.this.setSetting(key, value, true);
                ManualCameraModule.this.setRAWPictureSetting(value, false);
                ManualCameraModule.this.setRAWParameter(true);
            }
        }

        public boolean checkChildAvailable() {
            if (ManualCameraModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.ManualCameraModule$2 */
    class C03202 extends ChildSettingRunnable {
        C03202() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            if (ManualCameraModule.this.mCameraDevice != null) {
                ManualCameraModule.this.setSetting(key, value, true);
                ManualCameraModule.this.setNoiseReductionParameter(true);
            }
        }

        public boolean checkChildAvailable() {
            if (ManualCameraModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.ManualCameraModule$3 */
    class C03213 extends ChildSettingRunnable {
        C03213() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            ListPreference listPref;
            int i = 0;
            CamLog.m3d(CameraConstants.TAG, "-picsize- mChildSettingUpdater_ManualPictureSize");
            if (prefObject instanceof ListPreference) {
                listPref = (ListPreference) prefObject;
            } else {
                listPref = null;
            }
            if (listPref != null && ManualCameraModule.this.mCameraDevice != null && ManualCameraModule.this.checkModuleValidate(208)) {
                ManualCameraModule.this.changeFullVisionSettingOnPictureSizeChanged(listPref, value);
                ManualCameraModule.this.setSetting(key, value, true);
                CameraParameters parameters = ManualCameraModule.this.mCameraDevice.getParameters();
                if (parameters.get(ManualCameraModule.this.getPictureSizeParamKey(0)).equals(listPref.getValue())) {
                    CamLog.m3d(CameraConstants.TAG, "pictureSize is already same. return!");
                    return;
                }
                if (ManualCameraModule.this.mLightFrameManager.isLightFrameMode()) {
                    ManualCameraModule.this.mGet.getHybridView().setSurfaceViewTransparent(true);
                } else {
                    ManualCameraModule.this.mGet.setPreviewCoverVisibility(0, true);
                }
                ManualCameraModule.this.showFrameGridView("off", false);
                ManualCameraModule.this.mPictureOrVideoSizeChanged = true;
                ManualCameraModule.this.updateSecondCameraSettings(key, value, true);
                ManualCameraModule.this.setParamUpdater(parameters, ManualCameraModule.this.getPictureSizeParamKey(0), listPref.getValue());
                if (ManualCameraModule.this.isOpticZoomSupported(null)) {
                    ActivityBridge activityBridge = ManualCameraModule.this.mGet;
                    String shotMode = ManualCameraModule.this.getShotMode();
                    if (ManualCameraModule.this.mCameraId == 0) {
                        i = 2;
                    }
                    ManualCameraModule.this.setParamUpdater(parameters, ManualCameraModule.this.getPictureSizeParamKey(1), activityBridge.getListPreference(SettingKeyWrapper.getPictureSizeKey(shotMode, i)).getValue());
                }
                ManualCameraModule.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_SIZE, ManualCameraModule.this.getDefaultPreviewSize());
                parameters.set(ParamConstants.KEY_HFR, "off");
                ManualCameraModule.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
                ManualCameraModule.this.setParameters(parameters);
                ManualCameraModule.this.mHandler.sendEmptyMessage(61);
                ManualCameraModule.this.onContentSizeChanged();
            }
        }

        public boolean checkChildAvailable() {
            if (ManualCameraModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.ManualCameraModule$4 */
    class C03224 extends ChildSettingRunnable {
        C03224() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            ManualCameraModule.this.setSetting(key, value, true);
        }

        public boolean checkChildAvailable() {
            if (ManualCameraModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.ManualCameraModule$5 */
    class C03235 extends ChildSettingRunnable {
        C03235() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            ManualCameraModule.this.setSetting(key, value, true);
        }

        public boolean checkChildAvailable() {
            if (ManualCameraModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.ManualCameraModule$6 */
    class C03246 extends CameraPictureCallbackForDng {
        C03246() {
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            ManualCameraModule.this.onRawPictureTakenCallback(data, null, null, null);
        }

        public void onPictureTaken(DngCreator dngCreator, ByteBuffer byteBuffer, Size size) {
            ManualCameraModule.this.onRawPictureTakenCallback(null, dngCreator, byteBuffer, size);
        }
    }

    public ManualCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (!this.mGet.isModuleChanging()) {
            setFocusPointVisibility(true);
        }
        this.mCaptureButtonManager.changeButtonByMode(12);
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        LayoutParams params = getCurrentlayoutParams();
        if (params != null) {
            setContentFrameLayoutParam(params);
        }
    }

    private LayoutParams getCurrentlayoutParams() {
        ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), getCameraId()));
        CameraActivity cameraActivity = (CameraActivity) this.mGet.getActivity();
        int[] lcd_size = Utils.getLCDsize(cameraActivity, true);
        if (listPref == null) {
            return null;
        }
        int[] size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
        int startMargin = cameraActivity.getStartMarginOfPreview(size, lcd_size);
        LayoutParams frameParams = Utils.getRelativeLayoutParams(cameraActivity, size[0], size[1]);
        if (frameParams == null) {
            return null;
        }
        if (Utils.isConfigureLandscape(cameraActivity.getResources())) {
            frameParams.setMarginStart(startMargin);
        } else {
            frameParams.topMargin = startMargin;
        }
        return frameParams;
    }

    protected void showIndicatorOnResume() {
        super.showIndicatorOnResume();
        if (this.mIndicatorManager != null) {
            String rawValue = this.mGet.getCurSettingValue(Setting.KEY_RAW_PICTURE);
            if (FunctionProperties.isSupportedRAWPictureSaving() && "on".equals(rawValue)) {
                this.mIndicatorManager.showSceneIndicator(7);
            }
        }
    }

    protected void setRAWPictureCallback() {
        this.mRAWJPGFileNameSyncNum = 0;
        if (FunctionProperties.isSupportedRAWPictureSaving()) {
            int rawSize = 0;
            if ("on".equals(getSettingValue(Setting.KEY_RAW_PICTURE))) {
                this.mSnapShotChecker.setRawPicState(1);
                if (this.mStorageManager != null) {
                    rawSize = this.mStorageManager.getRawSize();
                }
                CamLog.m3d(CameraConstants.TAG, "-raw- rawSize=" + rawSize);
                if (this.mRAWPictureCallback == null) {
                    this.mRAWPictureCallback = new C03246();
                }
                if (rawSize > 0) {
                    try {
                        this.mCameraDevice.addRawImageCallbackBuffer(new byte[rawSize]);
                        return;
                    } catch (OutOfMemoryError e) {
                        CamLog.m3d(CameraConstants.TAG, "-raw- OutOfMemory Error");
                        e.printStackTrace();
                        System.gc();
                        this.mToastManager.showShortToast(getActivity().getString(C0088R.string.camera_error_occurred_try_again));
                        return;
                    }
                }
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "-raw- set mRAWPictureCallback as null");
            this.mRAWPictureCallback = null;
        }
    }

    protected void onRawPictureTakenCallback(byte[] data, DngCreator dngCreator, ByteBuffer byteBuffer, Size size) {
        this.mSnapShotChecker.setRawPicState(3);
        if (data != null) {
            CamLog.m3d(CameraConstants.TAG, "-raw- ### onRawPictureTakenCallback, RAW data size is = " + (data == null ? 0 : data.length));
            if (this.mSnapShotChecker.getPictureCallbackState() == 2) {
                CamLog.m3d(CameraConstants.TAG, "-raw- start preview");
                setupPreview(null);
                if (!(this.mSnapShotChecker.checkMultiShotState(14) || this.mFocusManager == null || !isFocusEnableCondition())) {
                    this.mFocusManager.registerCallback(true);
                }
            }
            saveRawPictureImage(data, null, null, null);
        } else if (!(dngCreator == null || byteBuffer == null || size == null)) {
            saveRawPictureImage(null, dngCreator, byteBuffer, size);
        }
        this.mSnapShotChecker.setRawPicState(0);
    }

    protected void saveRawPictureImage(byte[] data, DngCreator dngCreator, ByteBuffer byteBuffer, Size size) {
        if (this.mGet.getMediaSaveService() != null && !this.mGet.getMediaSaveService().isQueueFull()) {
            String dir = getCurDir();
            final byte[] bArr = data;
            final DngCreator dngCreator2 = dngCreator;
            final ByteBuffer byteBuffer2 = byteBuffer;
            final Size size2 = size;
            this.mGet.getMediaSaveService().processLocal(new OnLocalSaveListener() {
                public void onPreExecute() {
                }

                public void onPostExecute(Uri uri) {
                    if (uri != null) {
                        ManualCameraModule.this.mGet.requestNotifyNewMediaonActivity(uri, ManualCameraModule.this.checkModuleValidate(128));
                    }
                }

                public Uri onLocalSave(String dir, String fileName) {
                    if (ManualCameraModule.this.mCameraDevice != null) {
                        ManualCameraModule manualCameraModule = ManualCameraModule.this;
                        manualCameraModule.mRawSavingCnt++;
                        CamLog.m3d(CameraConstants.TAG, "mRawSavingCnt = " + ManualCameraModule.this.mRawSavingCnt);
                        String str = dir;
                        ManualCameraModule.this.saveRawFile(str, fileName + CameraConstants.CAM_RAW_EXTENSION, bArr, dngCreator2, byteBuffer2, size2);
                    }
                    return null;
                }
            }, dir, getFileName(dir, CameraConstants.CAM_RAW_EXTENSION));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0156 A:{SYNTHETIC, Splitter: B:48:0x0156} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0116 A:{SYNTHETIC, Splitter: B:38:0x0116} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0140  */
    protected void saveRawFile(java.lang.String r12, java.lang.String r13, byte[] r14, android.hardware.camera2.DngCreator r15, java.nio.ByteBuffer r16, android.util.Size r17) {
        /*
        r11 = this;
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r0 = r0.append(r12);
        r0 = r0.append(r13);
        r9 = r0.toString();
        r8 = new java.io.File;
        r8.<init>(r9);
        r0 = "CameraApp";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "-raw- filepath = ";
        r2 = r2.append(r3);
        r3 = r8.getAbsolutePath();
        r2 = r2.append(r3);
        r2 = r2.toString();
        com.lge.camera.util.CamLog.m3d(r0, r2);
        r10 = 0;
        r1 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x0196, IOException -> 0x0108, all -> 0x0152 }
        r1.<init>(r8);	 Catch:{ FileNotFoundException -> 0x0196, IOException -> 0x0108, all -> 0x0152 }
        if (r14 == 0) goto L_0x00a8;
    L_0x003a:
        r1.write(r14);	 Catch:{ FileNotFoundException -> 0x00b9, IOException -> 0x0193 }
    L_0x003d:
        if (r1 == 0) goto L_0x0042;
    L_0x003f:
        r1.close();	 Catch:{ IOException -> 0x00fd }
    L_0x0042:
        r15 = 0;
        r16 = 0;
        r0 = r11.mRawSavingCnt;
        r0 = r0 + -1;
        r11.mRawSavingCnt = r0;
        r0 = "CameraApp";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "mRawSavingCnt = ";
        r2 = r2.append(r3);
        r3 = r11.mRawSavingCnt;
        r2 = r2.append(r3);
        r2 = r2.toString();
        com.lge.camera.util.CamLog.m3d(r0, r2);
        r0 = r11.mRawSavingCnt;
        if (r0 > 0) goto L_0x0074;
    L_0x0069:
        r0 = 0;
        r11.mRawSavingCnt = r0;
        r0 = 0;
        r11.mNeedProgressDuringCapture = r0;
        r0 = 0;
        r2 = 0;
        r11.showSavingDialog(r0, r2);
    L_0x0074:
        r0 = "CameraApp";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "-raw- raw file saved filepath = ";
        r2 = r2.append(r3);
        r3 = r8.getAbsolutePath();
        r2 = r2.append(r3);
        r3 = " rawtimecheck = ";
        r2 = r2.append(r3);
        r4 = java.lang.System.currentTimeMillis();
        r2 = r2.append(r4);
        r2 = r2.toString();
        com.lge.camera.util.CamLog.m3d(r0, r2);
        r0 = r11.mGet;
        r0 = r0.getAppContext();
        com.lge.camera.file.FileManager.registerFileUri(r0, r12, r13);
        return;
    L_0x00a8:
        if (r15 == 0) goto L_0x003d;
    L_0x00aa:
        if (r16 == 0) goto L_0x003d;
    L_0x00ac:
        if (r17 == 0) goto L_0x003d;
    L_0x00ae:
        r4 = 0;
        r0 = r15;
        r2 = r17;
        r3 = r16;
        r0.writeByteBuffer(r1, r2, r3, r4);	 Catch:{ FileNotFoundException -> 0x00b9, IOException -> 0x0193 }
        goto L_0x003d;
    L_0x00b9:
        r7 = move-exception;
    L_0x00ba:
        r0 = "CameraApp";
        r2 = "FileNotFoundException";
        com.lge.camera.util.CamLog.m3d(r0, r2);	 Catch:{ all -> 0x0191 }
        r7.printStackTrace();	 Catch:{ all -> 0x0191 }
        if (r1 == 0) goto L_0x00c9;
    L_0x00c6:
        r1.close();	 Catch:{ IOException -> 0x0103 }
    L_0x00c9:
        r15 = 0;
        r16 = 0;
        r0 = r11.mRawSavingCnt;
        r0 = r0 + -1;
        r11.mRawSavingCnt = r0;
        r0 = "CameraApp";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "mRawSavingCnt = ";
        r2 = r2.append(r3);
        r3 = r11.mRawSavingCnt;
        r2 = r2.append(r3);
        r2 = r2.toString();
        com.lge.camera.util.CamLog.m3d(r0, r2);
        r0 = r11.mRawSavingCnt;
        if (r0 > 0) goto L_0x0074;
    L_0x00f0:
        r0 = 0;
        r11.mRawSavingCnt = r0;
        r0 = 0;
        r11.mNeedProgressDuringCapture = r0;
        r0 = 0;
        r2 = 0;
        r11.showSavingDialog(r0, r2);
        goto L_0x0074;
    L_0x00fd:
        r6 = move-exception;
        r6.printStackTrace();
        goto L_0x0042;
    L_0x0103:
        r6 = move-exception;
        r6.printStackTrace();
        goto L_0x00c9;
    L_0x0108:
        r6 = move-exception;
        r1 = r10;
    L_0x010a:
        r0 = "CameraApp";
        r2 = "IOException";
        com.lge.camera.util.CamLog.m3d(r0, r2);	 Catch:{ all -> 0x0191 }
        r6.printStackTrace();	 Catch:{ all -> 0x0191 }
        if (r1 == 0) goto L_0x0119;
    L_0x0116:
        r1.close();	 Catch:{ IOException -> 0x014d }
    L_0x0119:
        r15 = 0;
        r16 = 0;
        r0 = r11.mRawSavingCnt;
        r0 = r0 + -1;
        r11.mRawSavingCnt = r0;
        r0 = "CameraApp";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "mRawSavingCnt = ";
        r2 = r2.append(r3);
        r3 = r11.mRawSavingCnt;
        r2 = r2.append(r3);
        r2 = r2.toString();
        com.lge.camera.util.CamLog.m3d(r0, r2);
        r0 = r11.mRawSavingCnt;
        if (r0 > 0) goto L_0x0074;
    L_0x0140:
        r0 = 0;
        r11.mRawSavingCnt = r0;
        r0 = 0;
        r11.mNeedProgressDuringCapture = r0;
        r0 = 0;
        r2 = 0;
        r11.showSavingDialog(r0, r2);
        goto L_0x0074;
    L_0x014d:
        r6 = move-exception;
        r6.printStackTrace();
        goto L_0x0119;
    L_0x0152:
        r0 = move-exception;
        r1 = r10;
    L_0x0154:
        if (r1 == 0) goto L_0x0159;
    L_0x0156:
        r1.close();	 Catch:{ IOException -> 0x018c }
    L_0x0159:
        r15 = 0;
        r16 = 0;
        r2 = r11.mRawSavingCnt;
        r2 = r2 + -1;
        r11.mRawSavingCnt = r2;
        r2 = "CameraApp";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "mRawSavingCnt = ";
        r3 = r3.append(r4);
        r4 = r11.mRawSavingCnt;
        r3 = r3.append(r4);
        r3 = r3.toString();
        com.lge.camera.util.CamLog.m3d(r2, r3);
        r2 = r11.mRawSavingCnt;
        if (r2 > 0) goto L_0x018b;
    L_0x0180:
        r2 = 0;
        r11.mRawSavingCnt = r2;
        r2 = 0;
        r11.mNeedProgressDuringCapture = r2;
        r2 = 0;
        r3 = 0;
        r11.showSavingDialog(r2, r3);
    L_0x018b:
        throw r0;
    L_0x018c:
        r6 = move-exception;
        r6.printStackTrace();
        goto L_0x0159;
    L_0x0191:
        r0 = move-exception;
        goto L_0x0154;
    L_0x0193:
        r6 = move-exception;
        goto L_0x010a;
    L_0x0196:
        r7 = move-exception;
        r1 = r10;
        goto L_0x00ba;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ManualCameraModule.saveRawFile(java.lang.String, java.lang.String, byte[], android.hardware.camera2.DngCreator, java.nio.ByteBuffer, android.util.Size):void");
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        super.setContentFrameLayoutParam(params);
        if (this.mLevelControlManager != null) {
            int previewTopMargin;
            int previewStartMargin = Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()) ? params.getMarginStart() : params.topMargin;
            if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                previewTopMargin = params.topMargin;
            } else {
                previewTopMargin = params.getMarginStart();
            }
            this.mLevelControlManager.setInclinometerMargin(params.width, params.height, previewStartMargin, previewTopMargin);
        }
    }

    protected void setRAWPictureSetting(String value, boolean disableMenu) {
        String key = Setting.KEY_RAW_PICTURE;
        ListPreference listPref = getListPreference(key);
        if (listPref != null) {
            setSettingMenuEnable(key, !disableMenu);
            if (value == null) {
                value = listPref.loadSavedValue();
            }
            setSetting(key, value, true);
        }
    }

    protected void setRAWParameter(boolean immediateSet) {
        if (this.mCameraDevice != null && this.mStorageManager != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            String raw_value = isManualMode() ? "on".equals(getSettingValue(Setting.KEY_RAW_PICTURE)) ? "1" : "0" : "0";
            CamLog.m3d(CameraConstants.TAG, "-raw- setParam KEY_RAW_FORMAT : " + raw_value);
            setParamUpdater(parameters, ParamConstants.KEY_RAW_FORMAT, raw_value);
            if (immediateSet) {
                setParameters(parameters);
            }
            this.mStorageManager.getRawSize();
        }
    }

    protected void setNoiseReductionParameter(boolean immediateSet) {
        if (FunctionProperties.isSupportedManualNoiseReduction() && this.mCameraDevice != null && this.mStorageManager != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            String value = isManualMode() ? "on".equals(getSettingValue(Setting.KEY_MANUAL_NOISE_REDUCTION)) ? "on" : "off" : "off";
            CamLog.m3d(CameraConstants.TAG, "-nr- setParam night-for-pro : " + value);
            setParamUpdater(parameters, ParamConstants.KEY_MANUAL_NOISE_REDUCTION, value);
            if (immediateSet) {
                setParameters(parameters);
            }
        }
    }

    protected String getDefaultPreviewSize() {
        String previewSize = "1920x1080";
        ListPreference pref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        if (pref == null) {
            return previewSize;
        }
        if (isRatioGuideNeeded()) {
            return pref.getExtraInfo(1, pref.findIndexOfValue(pref.getDefaultValue()));
        }
        return pref.getExtraInfo(1);
    }

    protected int setQuickButtonPresetWithMode(int preset) {
        return 7;
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mLevelControlManager);
        this.mManagerList.add(this.mHistogramManager);
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        if (this.mHistogramManager != null) {
            this.mHistogramManager.initHistogramView();
        }
        if (this.mLevelControlManager != null) {
            this.mLevelControlManager.initInclinometerView();
        }
        this.mIsRawIndicatorShown = false;
    }

    protected void initializeSettingMenus() {
        setSetting("hdr-mode", "0", false);
        super.initializeSettingMenus();
        setSettingMenuEnable("hdr-mode", false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setGuideTextSettingMenu(true);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        restoreSettingValue("hdr-mode");
        restoreFlashSetting();
        if (ParamConstants.FLASH_MODE_REAR_ON.equals(getSettingValue("flash-mode"))) {
            this.mGet.setSetting("flash-mode", "on", true);
        }
        if ("1".equals(getSettingValue("hdr-mode")) && getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue(Setting.KEY_RAW_PICTURE);
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        setGuideTextSettingMenu(false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
        setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected void addModuleChildSettingMap(HashMap<String, ChildSettingRunnable> map) {
        super.addModuleChildSettingMap(map);
        if (map != null) {
            map.remove(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            map.put(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), this.mChildSettingUpdater_ManualPictureSize);
            map.put(Setting.KEY_RAW_PICTURE, this.mChildSettingUpdater_saving_raw_picture);
            if (FunctionProperties.isSupportedManualNoiseReduction()) {
                map.put(Setting.KEY_MANUAL_NOISE_REDUCTION, this.mChildSettingUpdater_noise_reduction);
            }
            map.put(Setting.KEY_INCLINOMETER, this.mChildSettingUpdater_inclinometer);
            map.put(Setting.KEY_HISTOGRAM, this.mChildSettingUpdater_histogram);
        }
    }

    protected void addCommonRequester() {
        super.addCommonRequester();
        if (FunctionProperties.isSupportedManualNoiseReduction()) {
            this.mParamUpdater.addRequester(ParamConstants.KEY_MANUAL_NOISE_REDUCTION, "off", false, false);
        }
    }

    protected void changeRequester() {
        CamLog.m3d(CameraConstants.TAG, "changeRequester()");
        super.changeRequester();
        if (!FunctionProperties.isSupportedManualZSL()) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, "off");
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, getDefaultPreviewSize());
        this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, "off");
        restoreManualFlashSetting();
        this.mParamUpdater.setParamValue("flash-mode", getSettingValue("flash-mode"));
        if (FunctionProperties.isSupportedManualNoiseReduction()) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_MANUAL_NOISE_REDUCTION, getSettingValue(Setting.KEY_MANUAL_NOISE_REDUCTION), false, true);
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_JPEG_RAW_STR);
        if (this.mManualControlManager != null && this.mManualControlManager.getAELock()) {
            updateFlashSetting(true);
        }
    }

    protected void startPreview(CameraParameters params, SurfaceTexture surfaceTexture) {
        if (!this.mGet.isPaused()) {
            super.startPreview(params, surfaceTexture);
        }
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (!this.mGet.isModuleChanging()) {
            boolean enableManualView = isEnableManualView();
            if (doByAction) {
                boolean isFocusAvailable = true;
                if (this.mCameraCapabilities != null) {
                    isFocusAvailable = this.mCameraCapabilities.isAFSupported() && enableManualView;
                }
                setFocusPointVisibility(isFocusAvailable);
            }
            doCleanHistogramAndInclinometerView(enableManualView);
            setLongShotGuideVisibiltiy(enableManualView);
        }
    }

    private void doCleanHistogramAndInclinometerView(boolean enableManualView) {
        boolean isEnableHistogram = false;
        if (this.mLevelControlManager != null && this.mHistogramManager != null && checkModuleValidate(5)) {
            if (enableManualView) {
                if (!"off".equals(getSettingValue(Setting.KEY_HISTOGRAM))) {
                    isEnableHistogram = true;
                }
                this.mLevelControlManager.showBySettingValue();
                this.mHistogramManager.enable(isEnableHistogram, true);
                return;
            }
            this.mLevelControlManager.show(false);
            this.mHistogramManager.enable(false, false);
        }
    }

    protected void oneShotPreviewCallbackDoneAfter() {
        super.oneShotPreviewCallbackDoneAfter();
        if (this.mIndicatorManager != null && FunctionProperties.isSupportedRAWPictureSaving() && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_RAW_PICTURE)) && !this.mIsRawIndicatorShown) {
            this.mIndicatorManager.showSceneIndicator(7);
            this.mIsRawIndicatorShown = true;
        }
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        updateLongShutterGuideViewDegree(degree);
        updateZoomBarMargin(degree);
    }

    private void updateZoomBarMargin(int degree) {
    }

    public void onGestureCleanViewDetected() {
        if (checkModuleValidate(31)) {
            super.onGestureCleanViewDetected();
        }
    }

    protected void doTakePicture() {
        playManualShutterSound(true);
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_MANUAL_NOISE_REDUCTION)) && isShutterSpeedLongerThan(1.0f)) {
            setLongShutterGuideViewVisibility(true);
            setGraphyEnable(false);
        } else if (isShutterSpeedLongerThan(2.0f)) {
            setLongShutterGuideViewVisibility(true);
            setGraphyEnable(false);
        }
        if (this.mAnimationManager != null && isShutterSpeedLongerThan(1.0f)) {
            this.mAnimationManager.startManualSnapShotEffect();
            setGraphyEnable(false);
        }
        setRAWPictureCallback();
        super.doTakePicture();
    }

    protected void setGraphyEnable(boolean enable) {
        if (this.mGraphyViewManager != null) {
            this.mGraphyViewManager.setGraphyEnable(enable);
        }
    }

    protected void onDropTakePicture(int error) {
        CamLog.m3d(CameraConstants.TAG, "onDropTakePicture : " + error);
        switch (error) {
            case 99:
                this.mSnapShotChecker.setSnapShotState(0);
                return;
            default:
                return;
        }
    }

    protected void playShutterSound(boolean sound) {
        if (!sound || !checkModuleValidate(128)) {
            return;
        }
        if (isShutterSpeedLongerThan(0.5f)) {
            playManualShutterSound(false);
        } else {
            super.playShutterSound(sound);
        }
    }

    private void playManualShutterSound(boolean mirrorUp) {
        if (isShutterSpeedLongerThan(0.5f)) {
            this.mGet.playSound(10, mirrorUp, 0);
        }
    }

    protected void doSnapshotEffect(boolean animation, float fromAlpha, long duration) {
        if (isShutterSpeedLongerThan(1.0f)) {
            this.mAnimationManager.stopManualSnapShotEffect();
            setLongShutterGuideViewVisibility(false);
            setGraphyEnable(true);
            return;
        }
        super.doSnapshotEffect(animation, fromAlpha, duration);
    }

    protected void updateFlashParam(CameraParameters parameters, int flashMsg, boolean save) {
        boolean isRearFlash = flashMsg == 53;
        CamLog.m3d(CameraConstants.TAG, "updateFlashParam : flashMsg = " + flashMsg + " is rear flash = " + isRearFlash);
        SharedPreferenceUtil.saveRearFlashMode(getAppContext(), isRearFlash);
        super.updateFlashParam(parameters, flashMsg, save);
    }

    private boolean isShutterSpeedLongerThan(float sec) {
        if (this.mParamUpdater != null) {
            String ss = this.mParamUpdater.getParamValue(getShutterSpeedParamKey());
            if (ss == null || "not found".equals(ss)) {
                return false;
            }
            if (FunctionProperties.isSupportedManualZSL()) {
                try {
                    if (Float.compare(Float.valueOf(ss).floatValue(), sec * 1000.0f) < 0) {
                        return false;
                    }
                    return true;
                } catch (NumberFormatException e) {
                    CamLog.m3d(CameraConstants.TAG, "invalide shutter speed :" + e);
                    return false;
                }
            } else if (Double.compare(ManualUtil.getShutterSpeedInDouble(ss), (double) sec) >= 0) {
                return true;
            }
        }
        return false;
    }

    protected void updateLongShutterGuideViewDegree(int degree) {
        RotateLayout captureTextLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.manual_long_shutter_guide_layout_rotate);
        if (captureTextLayout != null) {
            captureTextLayout.rotateLayout(degree);
            LayoutParams lp = (LayoutParams) captureTextLayout.getLayoutParams();
            Utils.resetLayoutParameter(lp);
            switch (Utils.convertDegree(this.mGet.getAppContext().getResources(), degree)) {
                case 0:
                    lp.addRule(12, 1);
                    lp.addRule(14, 1);
                    if ((this.mManualViewManager != null && this.mManualViewManager.isDrumShowing(31)) || (this.mGraphyViewManager != null && this.mGraphyViewManager.isGraphyListVisible())) {
                        lp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.3f);
                        break;
                    } else {
                        lp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.195f);
                        break;
                    }
                    break;
                case 90:
                    lp.addRule(21, 1);
                    lp.addRule(15, 1);
                    lp.setMarginEnd(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.manual_control_capturing_text.marginStart));
                    break;
                case 180:
                    lp.addRule(10, 1);
                    lp.addRule(14, 1);
                    lp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.195f);
                    break;
                case 270:
                    lp.addRule(20, 1);
                    lp.addRule(15, 1);
                    lp.setMarginStart(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.manual_control_capturing_text.marginStart));
                    break;
            }
            captureTextLayout.setLayoutParams(lp);
        }
    }

    protected void setLongShotGuideVisibiltiy(boolean enableManualView) {
        if (enableManualView) {
            if (this.mGet.findViewById(C0088R.id.manual_long_shutter_guide_layout) == null) {
                this.mGet.layoutInflate(C0088R.layout.manual_long_shutter_guide, (FrameLayout) findViewById(C0088R.id.contents_base));
                updateLongShutterGuideViewDegree(getOrientationDegree());
            }
        } else if (!isTimerShotCountdown()) {
            View guideTextView = this.mGet.findViewById(C0088R.id.manual_long_shutter_guide_layout);
            if (guideTextView != null) {
                ((ViewGroup) guideTextView.getParent()).removeView(guideTextView);
            }
        }
    }

    protected void setLongShutterGuideViewVisibility(boolean show) {
        RelativeLayout captureTextLayout = (RelativeLayout) this.mGet.findViewById(C0088R.id.manual_long_shutter_guide_layout);
        if (captureTextLayout != null) {
            updateLongShutterGuideViewDegree(getOrientationDegree());
            captureTextLayout.findViewById(C0088R.id.manual_long_shutter_guide_textview).setVisibility(show ? 0 : 8);
        }
    }

    protected String getGridSettingValue() {
        return "grid".equals(this.mGet.getCurSettingValue(Setting.KEY_INCLINOMETER)) ? "on" : "off";
    }

    public void onPauseAfter() {
        this.mHistogramManager.enable(false, false);
        this.mLevelControlManager.show(false);
        super.onPauseAfter();
        this.mRawSavingCnt = 0;
        if (ParamConstants.FLASH_MODE_REAR_ON.equals(this.mGet.getCurSettingValue("flash-mode"))) {
            restoreSettingMenus();
        }
        setLongShutterGuideViewVisibility(false);
        setGraphyEnable(true);
    }

    public String getCurrentViewModeToString() {
        return "manual";
    }

    public String getShotMode() {
        return CameraConstants.MODE_MANUAL_CAMERA;
    }

    public int getShutterButtonType() {
        return 4;
    }

    protected boolean takePicture() {
        if (this.mRawSavingCnt >= 1) {
            CamLog.m3d(CameraConstants.TAG, "mRawSavingCnt = " + this.mRawSavingCnt);
            showSavingDialog(true, 0);
            onDropTakePicture(99);
            return false;
        } else if (this.mManualViewManager.isDrumMoving(31)) {
            CamLog.m3d(CameraConstants.TAG, "Drum is moving");
            onDropTakePicture(99);
            return false;
        } else {
            this.mGraphyListOn = false;
            return super.takePicture();
        }
    }

    protected void onShutterCallback(boolean sound, boolean animation, boolean recording) {
        super.onShutterCallback(sound, animation, recording);
        if ("on".equals(getSettingValue(Setting.KEY_RAW_PICTURE))) {
            showSavingDialog(true, 0);
        }
    }

    public boolean onHideMenu(int menuType) {
        if (this.mHistogramManager != null) {
            boolean isEnableHistogram = (CameraConstants.MODE_MANUAL_VIDEO.equals(getShotMode()) || "off".equals(getSettingValue(Setting.KEY_HISTOGRAM))) ? false : true;
            this.mHistogramManager.enable(isEnableHistogram, true);
        }
        this.mLevelControlManager.showBySettingValue();
        return super.onHideMenu(menuType);
    }

    public boolean onShowMenu(int menuType) {
        if (isMenuShowing(menuType)) {
            return false;
        }
        if (this.mHistogramManager != null) {
            this.mHistogramManager.enable(false, false);
        }
        if (this.mLevelControlManager != null) {
            this.mLevelControlManager.show(false);
        }
        return super.onShowMenu(menuType);
    }

    protected void onInAndZoomStart() {
        if (this.mHistogramManager != null) {
            this.mHistogramManager.enable(false, false);
        }
    }

    public void setManualFocus(boolean set) {
        if (this.mFocusManager != null) {
            CamLog.m3d(CameraConstants.TAG, "set manual focus - isSet : " + set);
            if (set) {
                this.mFocusManager.setManualFocus(true);
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        ManualCameraModule.this.setFocusPointVisibility(false);
                        ManualCameraModule.this.mFocusManager.cancelTouchAutoFocus();
                        ManualCameraModule.this.mFocusManager.hideAndCancelAllFocus(false);
                    }
                });
                setSetting("focus-mode", ParamConstants.FOCUS_MODE_MANUAL, false);
                return;
            }
            this.mFocusManager.setManualFocus(false);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    ManualCameraModule.this.setFocusPointVisibility(true);
                }
            });
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isAFSupported()) {
                setSetting("focus-mode", "auto", false);
                this.mParamUpdater.setParamValue("focus-mode", ParamConstants.FOCUS_MODE_MULTIWINDOWAF);
                setCameraFocusMode(ParamConstants.FOCUS_MODE_MULTIWINDOWAF);
                this.mFocusManager.registerCallback(true);
            }
        }
    }

    protected void onTakePictureBefore() {
        super.onTakePictureBefore();
        if (this.mLevelControlManager != null) {
            this.mLevelControlManager.setAlpha(true);
        }
        if (isShutterSpeedLongerThan(1.0f)) {
            if (this.mManualViewManager != null) {
                this.mManualViewManager.enableManualControls(false);
            }
            if (this.mQuickButtonManager != null) {
                this.mQuickButtonManager.setEnable(100, false, true);
                return;
            }
            return;
        }
        this.mManualViewManager.setDrumsEnabled(31, false);
        this.mManualViewManager.setAutoButtonsEnabled(31, false);
    }

    protected void onTakePictureAfter() {
        if (this.mFocusManager != null && this.mFocusManager.isManualFocusMode()) {
            this.mParamUpdater.setParamValue("focus-mode", "normal");
        }
        if (!(this.mTimerManager.isTimerShotCountdown() || FunctionProperties.isSupportedManualZSL())) {
            setupPreview(null);
        }
        LdbUtil.setMultiShotState(0);
        super.onTakePictureAfter();
        if (!isMenuShowing(4)) {
            if (isRearCamera()) {
                if (this.mLevelControlManager != null) {
                    this.mLevelControlManager.setAlpha(false);
                    this.mLevelControlManager.showBySettingValue();
                }
                setFocusPointVisibility(true);
                if (!(this.mFocusManager == null || !this.mFocusManager.isAFPointVisible() || isManualFocusMode())) {
                    this.mFocusManager.showFocusMove();
                }
                showHistogram(true);
            }
            if (this.mManualViewManager != null) {
                this.mManualViewManager.setPanelVisibility(0);
                this.mManualViewManager.enableManualControls(true);
            }
        }
    }

    protected void sendLDBIntentOnTakePictureAfter() {
        sendLDBIntentAfterContentsCreated(LdbConstants.LDB_FEATURE_NAME_MANUAL_CAMERA, 4, false);
    }

    public void showHistogram(boolean enable) {
        boolean isEnableHistogram = false;
        if (this.mHistogramManager == null) {
            return;
        }
        if (enable) {
            if (!"off".equals(getSettingValue(Setting.KEY_HISTOGRAM))) {
                isEnableHistogram = true;
            }
            this.mHistogramManager.enable(isEnableHistogram, true);
            return;
        }
        this.mHistogramManager.enable(false, true);
    }

    private void restoreManualFlashSetting() {
        restoreFlashSetting();
        if ("on".equals(getSettingValue("flash-mode")) && SharedPreferenceUtil.getRearFlashMode(getAppContext())) {
            this.mGet.setSetting("flash-mode", ParamConstants.FLASH_MODE_REAR_ON, false);
        }
    }

    public boolean onVideoShutterClickedBefore() {
        if (this.mManualViewManager.isDrumMoving(31)) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "-manual- onVideoShutterClickedBefore");
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        setFocusPointVisibility(false);
        doCleanHistogramAndInclinometerView(false);
        return true;
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        doCleanView(false, false, false);
        setFocusPointVisibility(true);
        doCleanHistogramAndInclinometerView(true);
        showMenuButton(true);
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonEnable(true, 4);
        }
        setupPreview(null);
    }

    protected void updateButtonsOnVideoStopClicked() {
        super.updateButtonsOnVideoStopClicked();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
    }

    public boolean isRatioGuideNeeded() {
        return false;
    }

    public String getContentSize() {
        return getSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        if (this.mManualViewManager != null) {
            this.mManualViewManager.setPanelVisibility(4);
        }
        if (this.mLevelControlManager != null) {
            this.mLevelControlManager.show(false);
        }
        if (this.mHistogramManager != null) {
            this.mHistogramManager.enable(false, false);
        }
    }

    protected void onContentSizeChanged() {
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot() || this.mManualViewManager == null) {
            return false;
        }
        this.mManualViewManager.setPanelVisibility(0);
        this.mManualViewManager.setApertureNumber();
        setFocusPointVisibility(true);
        doCleanHistogramAndInclinometerView(true);
        setLongShotGuideVisibiltiy(true);
        return true;
    }

    public void startedCameraByInAndOutZoom() {
        super.startedCameraByInAndOutZoom();
        setFocusPointVisibility(true);
    }

    public void onZoomHide() {
        super.onZoomHide();
        setFocusPointVisibility(true);
    }

    public void detectCameraChanged(int cameraId) {
        super.detectCameraChanged(cameraId);
        if (this.mManualViewManager != null) {
            this.mManualViewManager.updateManualPannel();
        }
    }

    protected void doneOpticZoom(boolean switchingByBar) {
        super.doneOpticZoom(switchingByBar);
        setFocusPointVisibility(true);
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    protected boolean isPauseWaitDuringShot() {
        return !"on".equals(getSettingValue(Setting.KEY_RAW_PICTURE));
    }
}
