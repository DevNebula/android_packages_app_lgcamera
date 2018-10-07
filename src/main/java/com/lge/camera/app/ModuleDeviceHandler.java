package com.lge.camera.app;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.components.HybridViewConfig;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.device.CameraCapabilities;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraDisabledException;
import com.lge.camera.device.CameraHolder;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraManagerBase;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.CameraOpsModuleBridge;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraPictureSizeUtil;
import com.lge.camera.device.ICameraCallback.CameraBacklightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraLowlightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.LGCameraMetaDataCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUpdater;
import com.lge.camera.device.ParamUpdater.OnParamsListener;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.managers.BinningManager;
import com.lge.camera.managers.FlashControlManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CamLog.TraceTag;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.MathUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SnapShotChecker;
import com.lge.camera.util.SnapShotChecker.SanpShotCheckerListener;
import com.lge.camera.util.Utils;
import com.lge.hardware.LGCamera.FingerDetectionData;
import com.lge.hardware.LGCamera.FingerDetectionDataListener;
import com.lge.hardware.LGCamera.ObtData;
import com.lge.hardware.LGCamera.ObtDataListener;
import java.util.List;

public abstract class ModuleDeviceHandler implements OnParamsListener, OnRemoveHandler, SanpShotCheckerListener, CameraOpsModuleBridge {
    private static final int MAX_REPEAT_CNT = 20;
    private static final int REPEAT_DELAY_TIME = 200;
    public static boolean sIsFrontSwitching = false;
    protected int mBinningRefreshCnt = 0;
    protected CameraCapabilities mCameraCapabilities = null;
    protected CameraProxy mCameraDevice = null;
    protected boolean mCameraDisabled = false;
    protected int mCameraId = 0;
    protected CameraOpenErrorCallback mCameraOpenErrorCallback = new C010314();
    protected CameraStartUpThread mCameraStartUpThread;
    protected int mCameraState = -2;
    protected int mCurFlash = 0;
    protected int mCurrentHDR = 0;
    protected int mDisplayOrientation = -1;
    protected final CameraErrorCallback mErrorCallback = new C01073();
    protected FingerDetectionDataListener mFingerDetectionListener = null;
    protected CameraLowlightDetectionCallback mFlashMetaDataCb = new C01139();
    protected LGCameraMetaDataCallback mFoodMetaDataCallback = new C01128();
    protected ActivityBridge mGet = null;
    protected CameraBacklightDetectionCallback mHDRMetaDataCallback = new C01084();
    protected final Handler mHandler = new MainHandler();
    protected boolean mInitModule = false;
    protected int mInitialOrientation = -1;
    protected boolean mIsBinningManualOff = false;
    protected boolean mIsFlashSetOffByBatteryLevel = false;
    protected boolean mIsPreviewCallbackWaiting = false;
    protected int mLastFlash = 0;
    private int mLastHDR = 0;
    protected CameraParameters mLocalParamForZoom = null;
    protected int mLowLightState = -1;
    protected LGCameraMetaDataCallback mManualFocusMetaDataCallback = new C01117();
    protected LGCameraMetaDataCallback mManualRecommendMetaDataCallback = new C01095();
    protected LGCameraMetaDataCallback mNightVisionMetaDataCallback = new C01106();
    protected boolean mOpenCameraFail = false;
    protected ParamUpdater mParamUpdater = new ParamUpdater(this);
    protected int mPrevBinningState = -1;
    protected ConditionVariable mSettingReady = new ConditionVariable();
    protected SnapShotChecker mSnapShotChecker = new SnapShotChecker(this);
    protected ConditionVariable mStartPreviewPrerequisiteReady = new ConditionVariable();
    protected ObtDataListener mTrackingAFListener = null;
    protected Rect mTrackingFocusRect = new Rect();
    private int mTrackingRefreshCnt = 0;

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$10 */
    class C009910 implements ObtDataListener {
        C009910() {
        }

        public void onDataListen(ObtData obtData, Camera arg1) {
            ModuleDeviceHandler.this.mTrackingRefreshCnt = ModuleDeviceHandler.this.mTrackingRefreshCnt + 1;
            if (ModuleDeviceHandler.this.mTrackingRefreshCnt % 2 == 0) {
                if (ModuleDeviceHandler.this.mTrackingFocusRect == null) {
                    ModuleDeviceHandler.this.mTrackingFocusRect = new Rect();
                }
                ModuleDeviceHandler.this.mTrackingFocusRect.set(obtData.left, obtData.top, obtData.width, obtData.height);
                ModuleDeviceHandler.this.drawTrackingAF(ModuleDeviceHandler.this.mTrackingFocusRect);
                ModuleDeviceHandler.this.mTrackingRefreshCnt = 0;
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$11 */
    class C010011 implements FingerDetectionDataListener {
        C010011() {
        }

        public void onDataListen(FingerDetectionData data, Camera arg1) {
            int x = data.fingerAxisX;
            int y = data.fingerAxisY;
            if (x > -1 && y > -1) {
                CamLog.m3d(CameraConstants.TAG, "finger detection x:" + x + "/ y:" + y);
            }
            ModuleDeviceHandler.this.onDetectedFinger(x, y);
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$14 */
    class C010314 implements CameraOpenErrorCallback {
        C010314() {
        }

        public void onCameraDisabled(int cameraId) {
            CameraDeviceUtils.showErrorAndFinish(ModuleDeviceHandler.this.mGet.getActivity(), C0088R.string.camera_error_title, C0088R.string.cannot_open_camera);
        }

        public void onDeviceOpenFailure(int cameraId) {
            CameraDeviceUtils.showErrorAndFinish(ModuleDeviceHandler.this.mGet.getActivity(), C0088R.string.camera_error_title, C0088R.string.cannot_open_camera);
        }

        public void onReconnectionFailure(CameraManagerBase mgr) {
            CameraDeviceUtils.showErrorAndFinish(ModuleDeviceHandler.this.mGet.getActivity(), C0088R.string.camera_error_title, C0088R.string.cannot_open_camera);
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$2 */
    class C01062 implements CameraPreviewDataCallback {
        C01062() {
        }

        public void onPreviewFrame(byte[] data, CameraProxy camera) {
            CamLog.traceEnd(TraceTag.MANDATORY, "StartCameraApp", 1000);
            CamLog.traceEnd(TraceTag.OPTIONAL, "startPreview", 3);
            if (!ModuleDeviceHandler.this.mGet.isPaused() && ModuleDeviceHandler.this.mCameraDevice != null) {
                if (ModuleDeviceHandler.this.isFastShotAvailable(3) && ModuleDeviceHandler.this.checkModuleValidate(192)) {
                    ModuleDeviceHandler.this.mCameraDevice.setLongshot(true);
                } else if (ModuleDeviceHandler.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    ModuleDeviceHandler.this.mCameraDevice.setLongshot(false);
                }
                if (AppControlUtil.isNeedQuickShotTaking() && !ModuleDeviceHandler.this.mGet.isPaused()) {
                    if (ModuleDeviceHandler.this.checkStorage(1, ModuleDeviceHandler.this.getCurStorage(), true)) {
                        String flash = ModuleDeviceHandler.this.getSettingValue("flash-mode");
                        if ("auto".equals(flash)) {
                            ModuleDeviceHandler.this.setFlashMetaDataCallback(flash);
                        }
                        ModuleDeviceHandler.this.setGestureEngineForQuickShot();
                        LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_QUICK_SHOT);
                        ModuleDeviceHandler.this.takePicture();
                    } else {
                        ModuleDeviceHandler.this.cancelQuickShotTaking();
                    }
                }
                Log.i(CameraConstants.TAG, "[Time Info][6] Device StartPreview End : Driver Preview Operation " + DebugUtil.interimCheckTime(true) + " ms");
                ModuleDeviceHandler.this.onOneShotPreviewFrameDirect();
                if (ModuleDeviceHandler.this.mHandler != null) {
                    ModuleDeviceHandler.this.mHandler.sendEmptyMessage(15);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$3 */
    class C01073 implements CameraErrorCallback {
        C01073() {
        }

        public void onError(int error, CameraProxy camera) {
            if (error == 100) {
                Log.e(CameraConstants.TAG, "Got camera error callback. error=" + error);
                throw new RuntimeException("Media server died.");
            } else {
                ModuleDeviceHandler.this.onErrorOccurred(error, camera);
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$4 */
    class C01084 implements CameraBacklightDetectionCallback {
        C01084() {
        }

        public void onDetected(boolean isDetected) {
            ModuleDeviceHandler moduleDeviceHandler = ModuleDeviceHandler.this;
            moduleDeviceHandler.mCurrentHDR = (isDetected ? 1 : -1) + moduleDeviceHandler.mCurrentHDR;
            if (ModuleDeviceHandler.this.mCurrentHDR >= 3) {
                ModuleDeviceHandler.this.mCurrentHDR = 3;
            } else if (ModuleDeviceHandler.this.mCurrentHDR < 0) {
                ModuleDeviceHandler.this.mCurrentHDR = 0;
            }
            if (ModuleDeviceHandler.this.mCurFlash == 3) {
                ModuleDeviceHandler.this.mCurrentHDR = 0;
            } else if ((ModuleDeviceHandler.this.mCurrentHDR == 0 || ModuleDeviceHandler.this.mCurrentHDR == 3) && ModuleDeviceHandler.this.mLastHDR != ModuleDeviceHandler.this.mCurrentHDR) {
                CamLog.m7i(CameraConstants.TAG, "change HDR indicator to " + ModuleDeviceHandler.this.mCurrentHDR);
                ModuleDeviceHandler.this.mLastHDR = ModuleDeviceHandler.this.mCurrentHDR;
                ModuleDeviceHandler.this.mSnapShotChecker.setLongshotAvailable(-1, ModuleDeviceHandler.this.mLastHDR, -1);
                ModuleDeviceHandler.this.updateIndicatorFromMetadataCallback(2, ModuleDeviceHandler.this.mLastHDR == 3 ? 0 : 4, true);
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$5 */
    class C01095 extends LGCameraMetaDataCallback {
        C01095() {
        }

        public void onCameraMetaData(byte[] data) {
            if (data != null && data.length >= 20) {
                float currentEV = MathUtil.byte2Float(data, 0, 4);
                float currentISO = MathUtil.byte2Float(data, 4, 4);
                float currentShutterSpeed = MathUtil.byte2Float(data, 8, 4);
                ModuleDeviceHandler.this.updateManualSettingValueFromMetadataCallback(MathUtil.byte2Float(data, 12, 4), currentEV, currentISO, currentShutterSpeed, MathUtil.byte2Float(data, 16, 4));
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$6 */
    class C01106 extends LGCameraMetaDataCallback {
        C01106() {
        }

        public void onCameraMetaData(byte[] data) {
            if (data != null) {
                float currentLux = MathUtil.byte2Float(data, 0, 4);
                ModuleDeviceHandler moduleDeviceHandler = ModuleDeviceHandler.this;
                moduleDeviceHandler.mBinningRefreshCnt++;
                if (ModuleDeviceHandler.this.mBinningRefreshCnt > 0 && ModuleDeviceHandler.this.mBinningRefreshCnt % 30 == 0) {
                    ModuleDeviceHandler.this.updateBinningStateFromMetadata(currentLux);
                } else if (currentLux > BinningManager.LOW_LIGHT_3_LUX_NORMAL || (ModuleDeviceHandler.this.mBinningRefreshCnt % 5 == 0 && currentLux < BinningManager.LOW_LIGHT_10_LUX_BINNING)) {
                    ModuleDeviceHandler.this.mBinningRefreshCnt = 0;
                    ModuleDeviceHandler.this.updateBinningStateFromMetadata(currentLux);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$7 */
    class C01117 extends LGCameraMetaDataCallback {
        C01117() {
        }

        public void onCameraMetaData(byte[] data) {
            if (data != null && data.length >= 20) {
                ModuleDeviceHandler.this.updateManualFocusValueFromMetadataCallback(MathUtil.byte2Float(data, 16, 4));
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$8 */
    class C01128 extends LGCameraMetaDataCallback {
        C01128() {
        }

        public void onCameraMetaData(byte[] data) {
            if (data != null && data.length >= 20) {
                ModuleDeviceHandler.this.updateWBValueFromMetadataCallback(MathUtil.byte2Float(data, 12, 4));
            }
        }
    }

    /* renamed from: com.lge.camera.app.ModuleDeviceHandler$9 */
    class C01139 implements CameraLowlightDetectionCallback {
        C01139() {
        }

        public void onDetected(boolean isDetected) {
            ModuleDeviceHandler moduleDeviceHandler = ModuleDeviceHandler.this;
            moduleDeviceHandler.mCurFlash = (isDetected ? 1 : -1) + moduleDeviceHandler.mCurFlash;
            ModuleDeviceHandler.this.mCurFlash = Math.min(ModuleDeviceHandler.this.mCurFlash, 3);
            ModuleDeviceHandler.this.mCurFlash = Math.max(ModuleDeviceHandler.this.mCurFlash, 0);
            if (ModuleDeviceHandler.this.mCurrentHDR == 3) {
                ModuleDeviceHandler.this.mCurFlash = 0;
            } else if (ModuleDeviceHandler.this.checkFlashIndicatorChangeCondition()) {
                CamLog.m7i(CameraConstants.TAG, "change Flash indicator to " + ModuleDeviceHandler.this.mCurFlash);
                ModuleDeviceHandler.this.mLastFlash = ModuleDeviceHandler.this.mCurFlash;
                ModuleDeviceHandler.this.mSnapShotChecker.setLongshotAvailable(ModuleDeviceHandler.this.mLastFlash, -1, -1);
                ModuleDeviceHandler.this.updateIndicatorFromMetadataCallback(3, ModuleDeviceHandler.this.mLastFlash == 3 ? 0 : 4, true);
            }
        }
    }

    protected class CameraStartUpThread extends Thread {
        private volatile boolean mCancelled;
        private final int mOpenType;

        public void cancel() {
            this.mCancelled = true;
        }

        public CameraStartUpThread() {
            this.mOpenType = 0;
        }

        public CameraStartUpThread(int openType) {
            this.mOpenType = openType;
        }

        public void run() {
            boolean z = true;
            CamLog.m3d(CameraConstants.TAG, "#CameraStartUpThread-run");
            if (this.mCancelled || ModuleDeviceHandler.this.mGet.isPaused()) {
                ModuleDeviceHandler.this.mHandler.sendEmptyMessage(4);
            } else if (this.mOpenType == 2 || CheckStatusManager.checkEnterApplication(ModuleDeviceHandler.this.mGet.getActivity(), true) || CheckStatusManager.getCheckEnterOutSecure() == 2) {
                ModuleDeviceHandler.this.mCameraDevice = ModuleDeviceHandler.this.openCamera(ModuleDeviceHandler.this.mGet.getActivity(), ModuleDeviceHandler.this.mCameraId, ModuleDeviceHandler.this.mHandler, null);
                if (ModuleDeviceHandler.this.mCameraDevice == null) {
                    if (CheckStatusManager.checkCameraPolicy()) {
                        ModuleDeviceHandler.this.retryOpenCamera(this.mCancelled);
                        if (ModuleDeviceHandler.this.mCameraDevice == null) {
                            ModuleDeviceHandler.this.mGet.runOnUiThread(new HandlerRunnable(ModuleDeviceHandler.this) {
                                public void handleRun() {
                                    if (!ModuleDeviceHandler.this.mGet.isPaused() && !CameraStartUpThread.this.mCancelled) {
                                        CameraDeviceUtils.showErrorAndFinish(ModuleDeviceHandler.this.mGet.getActivity(), C0088R.string.camera_error_title, C0088R.string.cannot_open_camera);
                                    }
                                }

                                public void removeRunnable() {
                                    ModuleDeviceHandler.this.mGet.removePostRunnable(this);
                                }
                            });
                            CamLog.m11w(CameraConstants.TAG, "Open Fail.");
                            return;
                        }
                    }
                    ModuleDeviceHandler.this.mGet.getActivity().finish();
                    return;
                }
                CameraManagerFactory.getAndroidCameraManager(ModuleDeviceHandler.this.getAppContext()).setCameraOpsModuleBridge(ModuleDeviceHandler.this);
                CameraParameters parameters = ModuleDeviceHandler.this.mCameraDevice.getParameters();
                ModuleDeviceHandler.this.updateCapabilities(parameters);
                CamLog.m7i(CameraConstants.TAG, "Wait Setting load");
                if (!ModuleDeviceHandler.this.mSettingReady.block(CameraConstants.TOAST_LENGTH_LONG)) {
                    CamLog.m5e(CameraConstants.TAG, "Setting Ready Failed!!!!");
                }
                CamLog.m7i(CameraConstants.TAG, "Done Setting load");
                ModuleDeviceHandler.this.addModuleRequester();
                CamLog.m7i(CameraConstants.TAG, "Texture state : " + ModuleDeviceHandler.this.mGet.getTextureState());
                if (!(this.mOpenType == 2 || this.mCancelled || isInterrupted() || ModuleDeviceHandler.this.mGet.getTextureState() != 0)) {
                    ModuleDeviceHandler.this.preparePreview(false, ModuleDeviceHandler.this.mParamUpdater, parameters);
                    Log.d(CameraConstants.TAG, "[Time Info] Wait texture available : block");
                    ModuleDeviceHandler.this.mStartPreviewPrerequisiteReady.block(CameraConstants.TOAST_LENGTH_LONG);
                }
                if (this.mCancelled || isInterrupted()) {
                    ModuleDeviceHandler.this.stopPreview();
                    ModuleDeviceHandler.this.closeCamera();
                    CamLog.m7i(CameraConstants.TAG, "StartUpThread cancelled.");
                    return;
                }
                ModuleDeviceHandler.this.mHandler.sendEmptyMessage(8);
                ModuleDeviceHandler.this.setupPreview(parameters);
                if (this.mOpenType != 2) {
                    ActivityBridge activityBridge = ModuleDeviceHandler.this.mGet;
                    if (this.mOpenType != 1) {
                        z = false;
                    }
                    activityBridge.startPreviewDone(z);
                }
                if (!this.mCancelled) {
                    ModuleDeviceHandler.this.mHandler.sendEmptyMessage(9);
                    ModuleDeviceHandler.this.mHandler.sendEmptyMessage(5);
                    ModuleDeviceHandler.this.cameraStartUpEnd();
                }
            }
        }
    }

    protected class MainHandler extends Handler {
        protected MainHandler() {
        }

        public void handleMessage(Message msg) {
            ModuleDeviceHandler.this.mainHandlerHandleMessage(msg);
        }
    }

    protected abstract void addModuleManager();

    protected abstract boolean changeCameraAfterInAndOutZoom();

    public abstract boolean checkCurrentConeMode(int i);

    protected abstract boolean checkPreviewCallbackCondition();

    public abstract void detectCameraChanged(int i);

    protected abstract void enableControls(boolean z);

    protected abstract void enableControls(boolean z, boolean z2);

    public abstract int getCurStorage();

    protected abstract String getShotMode();

    protected abstract void informPreviewSizeToFingerDetectionManager(String str);

    public abstract void initUI(View view);

    protected abstract void initializeControls(boolean z);

    protected abstract void onDetectedFinger(int i, int i2);

    protected abstract void onOneShotPreviewFrameDirect();

    protected abstract void resetFingerDetection();

    protected abstract void setMediaSaveServiceListener(boolean z);

    protected abstract void setupPreview(CameraParameters cameraParameters);

    protected abstract boolean setupPreviewBuffer(CameraParameters cameraParameters, SurfaceTexture surfaceTexture);

    public abstract void updateBackupParameters();

    public ModuleDeviceHandler(ActivityBridge activityBridge) {
        this.mGet = activityBridge;
    }

    public void setCameraState(int state) {
        CamLog.m3d(CameraConstants.TAG, "##[setCameraState] Camera state = " + state);
        this.mCameraState = state;
    }

    public boolean checkModuleValidate(int checkType) {
        int i;
        boolean invalid = false;
        if ((checkType & 1) != 0) {
            i = (this.mGet.isPaused() || this.mGet.getActivity().isFinishing()) ? 1 : 0;
            invalid = false | i;
        }
        if ((checkType & 2) != 0) {
            if (this.mCameraState < 1) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 4) != 0) {
            if (CameraHolder.instance().isCameraOpened()) {
                i = 0;
            } else {
                i = 1;
            }
            invalid |= i;
        }
        if ((checkType & 8) != 0) {
            invalid |= this.mGet.isCameraChanging();
        }
        if (!isFastShotAvailable(3)) {
            if ((checkType & 16) != 0) {
                invalid |= this.mSnapShotChecker.isSnapShotProcessing();
            }
            if ((checkType & 32) != 0) {
                i = (this.mSnapShotChecker.getSnapShotState() < 2 || this.mSnapShotChecker.getPictureCallbackState() >= 1) ? 0 : 1;
                invalid |= i;
            }
        }
        if ((checkType & 64) != 0) {
            if (this.mCameraState == 5 || this.mCameraState == 8 || this.mCameraState == 9) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if ((checkType & 128) != 0) {
            if (this.mCameraState == 6 || this.mCameraState == 7) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if (invalid) {
            return false;
        }
        return true;
    }

    public CameraProxy getCameraDevice() {
        return this.mCameraDevice;
    }

    protected void addModuleRequester() {
        beforeCommonRequester();
        addCommonRequester();
        afterCommonRequester();
        changeRequester();
    }

    protected void beforeCommonRequester() {
        if (this.mParamUpdater != null) {
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isFlashSupported()) {
                ListPreference listPref = this.mGet.getListPreference("flash-mode");
                if (listPref != null) {
                    String flashMode = listPref.getValue();
                    if (!isRearCamera()) {
                        this.mParamUpdater.addRequester(ParamConstants.KEY_FLASH_LEVEL, Integer.toString(FlashControlManager.getLevelFromStep(SharedPreferenceUtilBase.getFrontFlashStep(getAppContext()))), false, true);
                        if ("on".equals(flashMode)) {
                            flashMode = ParamConstants.FLASH_MODE_TORCH;
                        }
                    } else if (!("off".equals(flashMode) || checkFeatureDisableBatteryLevel(1, false) == 0)) {
                        CamLog.m3d(CameraConstants.TAG, "disableFlashSettingByBatteryStatus, set flash off");
                        flashMode = "off";
                        this.mGet.setSetting("flash-mode", flashMode, true);
                        this.mIsFlashSetOffByBatteryLevel = true;
                    }
                    this.mParamUpdater.addRequester("flash-mode", flashMode, false, true);
                }
            }
            if (isRearCamera()) {
                this.mParamUpdater.addRequester("focus-mode", getFocusSetting(), false, true);
                this.mParamUpdater.addRequester(ParamConstants.KEY_FOCUS_PEAKING, "off", false, true);
            }
            this.mParamUpdater.addRequester(ParamConstants.KEY_SCENE_MODE, "auto", false, true);
        }
    }

    public int checkFeatureDisableBatteryLevel(int checkType, boolean extra) {
        return 0;
    }

    protected void addCommonRequester() {
        if (this.mParamUpdater != null) {
            this.mParamUpdater.addRequester(ParamConstants.KEY_LGE_CAMERA, "1", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_ZSL, FunctionProperties.isSupportedZSL(this.mCameraId) ? "on" : "off", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_ZSL_BUFF_COUNT, "1", false, true);
            if (ModelProperties.isMTKChipset()) {
                this.mParamUpdater.addRequester(ParamConstants.KEY_MTK_CAMMODE, "1", false, false);
            }
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isVideoStabiliztionSupported()) {
                this.mParamUpdater.addRequester(ParamConstants.KEY_VIDEO_STABILIZATION, "false", false, false);
            }
            this.mParamUpdater.addRequester(ParamConstants.KEY_DUAL_RECORDER, "0", false, false);
            this.mParamUpdater.addRequester("recording-hint", "false", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_PREVIEW_FPS_RANGE, MultimediaProperties.getCameraFPSRange(isRearCamera()), false, true);
            this.mParamUpdater.addRequester(ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp", false, true);
            this.mParamUpdater.addRequester(ParamConstants.KEY_HFR, "off", false, false);
            if (FunctionProperties.isSupportedRAWPictureSaving() && isRearCamera()) {
                this.mParamUpdater.addRequester(ParamConstants.KEY_RAW_FORMAT, "0", false, true);
                this.mParamUpdater.setParamValue(ParamConstants.KEY_RAW_FORMAT, "on".equals(getSettingValue(Setting.KEY_RAW_PICTURE)) ? "1" : "0");
            }
            this.mParamUpdater.addRequester(ParamConstants.KEY_BEAUTY_LEVEL, "0", false, false);
            this.mParamUpdater.addRequester("beautyshot", "off", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_RELIGHTING_LEVEL, "0", false, false);
            String filmType = this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR);
            String filmEnable = CameraConstants.FILM_NONE.equals(filmType) ? "false" : "true";
            if (CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode())) {
                filmEnable = "true";
                filmType = CameraConstants.FILM_SMARTCAM_NONE;
            }
            this.mParamUpdater.addRequester(ParamConstants.KEY_FILM_ENABLE, filmEnable, false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_FILM_TYPE, filmType, false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_EFFECT, this.mGet.getCurSettingValue(Setting.KEY_COLOR_EFFECT), false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_NIGHTVISION_PARAM, !"off".equals(getSettingValue(Setting.KEY_BINNING)) ? "on" : "off", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_BINNING_PARAM, "normal", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_APP_BINNING_TYPE, getBinningType(), false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_DRAWING_STICKER, "off", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_DEFAULT_STR, false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_APP_FULL_FRAME_BUF_SIZE, ParamConstants.FULL_FRAME_DEFAULT, false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_APP_PREVIEW_CB_BUF_SIZE, "2", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_APP_APPLY_PREVIEW_CB_SOLUTION, "off", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_APP_VIDEO_SNAPSHOT, isLiveSnapshotSupported() ? "on" : "off", false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(getShotMode()), false, false);
        }
    }

    protected void afterCommonRequester() {
        if (this.mParamUpdater != null) {
            ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            if (listPref != null) {
                this.mParamUpdater.addRequester(getPictureSizeParamKey(0), listPref.getValue(), false, true);
                CamLog.m3d(CameraConstants.TAG, "[opticzoom] addPicureSizeParamReq : " + getPictureSizeParamKey(0) + " set to " + listPref.getValue());
                this.mParamUpdater.addRequester(ParamConstants.KEY_PREVIEW_SIZE, listPref.getExtraInfo(1), true, true);
            }
            if (this.mGet.isOpticZoomSupported(null) && !FunctionProperties.isSameResolutionOpticZoom()) {
                int i;
                ActivityBridge activityBridge = this.mGet;
                String shotMode = getShotMode();
                if (this.mCameraId == 0) {
                    i = 2;
                } else {
                    i = 0;
                }
                ListPreference otherListPref = activityBridge.getListPreference(SettingKeyWrapper.getPictureSizeKey(shotMode, i));
                if (otherListPref != null) {
                    this.mParamUpdater.addRequester(getPictureSizeParamKey(1), otherListPref.getValue(), false, true);
                    CamLog.m3d(CameraConstants.TAG, "[opticzoom] addPicsizeParamReq : " + getPictureSizeParamKey(1) + " set to " + otherListPref.getValue());
                }
            }
            listPref = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            if (listPref != null) {
                this.mParamUpdater.addRequester(ParamConstants.KEY_VIDEO_SIZE, listPref.getValue(), false, true);
            }
            this.mParamUpdater.addRequester("hdr-mode", "0", false, true);
            String hdrValue = this.mGet.getCurSettingValue("hdr-mode");
            if ("1".equals(hdrValue) || "2".equals(hdrValue)) {
                if (FunctionProperties.isSupportedHDR(isRearCamera()) == 1) {
                    this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, "off");
                }
                this.mParamUpdater.setParamValue("hdr-mode", hdrValue);
            }
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isVideoHDRSupported(this.mCameraId)) {
                this.mParamUpdater.addRequester(ParamConstants.KEY_VIDEO_HDR_MODE, "off", false, true);
            }
            if (FunctionProperties.isSupportedSteadyCamera(isRearCamera())) {
                restoreSteadyCamSetting();
                this.mParamUpdater.addRequester(ParamConstants.KEY_STEADY_CAM, getSettingValue(Setting.KEY_VIDEO_STEADY), false, true);
            }
            this.mParamUpdater.addRequester(ParamConstants.KEY_VIEW_MODE, getCurrentViewModeToString(), false, true);
            if (isSupportedFingerDetection()) {
                String onOff = (this.mCameraId == 2 && isFingerDetectionSupportedMode() && "on".equals(getSettingValue(Setting.KEY_FINGER_DETECTION))) ? "on" : "off";
                this.mParamUpdater.addRequester(ParamConstants.KEY_FINGER_DETECTION, onOff, false, true);
            }
            this.mParamUpdater.addRequester(ParamConstants.MANUAL_FOCUS_STEP, "-1", false, true);
        }
    }

    public String getCurrentViewModeToString() {
        return "normal";
    }

    protected void startPreview(CameraParameters params, final SurfaceTexture surfaceTexture, final boolean isRecording) {
        CamLog.m3d(CameraConstants.TAG, "#startPreview - start");
        if (this.mCameraDevice != null) {
            CameraParameters parameters;
            this.mCameraDevice.setErrorCallback(this.mHandler, this.mErrorCallback);
            if (this.mCameraState > 0 && this.mCameraState != 3) {
                stopPreview();
            }
            if (params == null) {
                parameters = this.mCameraDevice.getParameters();
            } else {
                parameters = params;
            }
            if (!(this.mParamUpdater == null || parameters == null)) {
                if (AppControlUtil.isNeedQuickShotTaking()) {
                    parameters.set(CameraConstants.QUICK_SHOT_KEY, 1);
                }
                this.mParamUpdater.updateAllParameters(parameters, false);
                Log.i(CameraConstants.TAG, "[Time Info][4] App Param setting End : Camera Parameter setting " + DebugUtil.interimCheckTime(true) + " ms");
                Log.i(CameraConstants.TAG, "[Time Info][5] Device Param setting Start : Device setting " + DebugUtil.interimCheckTime(false));
                this.mCameraDevice.setParameters(parameters);
                Log.i(CameraConstants.TAG, "[Time Info][5] Device Param setting End : Device setting " + DebugUtil.interimCheckTime(true) + " ms");
            }
            boolean isReadyPreview = setupPreviewBuffer(parameters, surfaceTexture);
            setDisplayOrientation(false);
            setOneShotPreviewCallback();
            if (isReadyPreview) {
                startPreview(isRecording, surfaceTexture);
            } else {
                preparePreview(isRecording, null, parameters);
                new Thread() {
                    public void run() {
                        ModuleDeviceHandler.this.mGet.waitConfigPreviewBuffer();
                        ModuleDeviceHandler.this.startPreview(isRecording, surfaceTexture);
                    }
                }.start();
            }
            CamLog.m3d(CameraConstants.TAG, "#startPreview - end");
        }
    }

    private void startPreview(boolean isRecording, SurfaceTexture surfaceTexture) {
        if (!this.mGet.isPaused() && this.mCameraDevice != null) {
            boolean isFilmRunning;
            if (surfaceTexture != null) {
                this.mCameraDevice.setPreviewTexture(surfaceTexture);
            } else if (HybridViewConfig.SURFACE.equals(this.mGet.getCurrentViewType())) {
                CamLog.m3d(CameraConstants.TAG, "-hybrid- setPreviewDisplay Surface");
                this.mCameraDevice.setPreviewDisplay(this.mGet.getSurfaceHolder());
            } else {
                CamLog.m3d(CameraConstants.TAG, "-hybrid- setPreviewDisplay Texture");
                this.mCameraDevice.setPreviewTexture(this.mGet.getSurfaceTexture());
            }
            Log.i(CameraConstants.TAG, "[Time Info][6] Device StartPreview Start : Driver Preview Operation " + DebugUtil.interimCheckTime(false));
            CamLog.traceBegin(TraceTag.OPTIONAL, "startPreview", 3);
            if (this.mGet.getFilmState() >= 3) {
                isFilmRunning = true;
            } else {
                isFilmRunning = false;
            }
            if (isFilmRunning && isRecording && !ManualUtil.isManualVideoMode(getShotMode())) {
                VideoRecorder.releasePersistentSurface();
                VideoRecorder.createPersistentSurface(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)), 0, null);
            }
            if (FunctionProperties.getSupportedHal() == 2 && isRecording) {
                this.mCameraDevice.startRecordingPreview(VideoRecorder.getSurface(false, 0));
            } else {
                this.mCameraDevice.startPreview();
            }
        }
    }

    private void preparePreview(boolean isRecording, ParamUpdater paramUpdater, CameraParameters parameters) {
        if (!this.mGet.isPaused() && !isRecording && parameters != null) {
            if (paramUpdater != null) {
                paramUpdater.updateParameters(parameters, ParamConstants.KEY_ZSL);
                paramUpdater.updateParameters(parameters, ParamConstants.KEY_PREVIEW_SIZE);
                paramUpdater.updateParameters(parameters, "picture-size");
            }
            parameters.set(ParamConstants.KEY_PREVIEW_SURFACE_TYPE, HybridViewConfig.SURFACE.equals(this.mGet.getCurrentViewType()) ? 1 : 0);
        }
    }

    protected void setOneShotPreviewCallback() {
        if (this.mCameraDevice != null) {
            this.mIsPreviewCallbackWaiting = true;
            this.mCameraDevice.setOneShotPreviewDataCallback(this.mHandler, new C01062());
        }
    }

    protected void onErrorOccurred(int error, CameraProxy camera) {
        CamLog.m11w(CameraConstants.TAG, "onErrorOccurred : error is " + error);
        onDropTakePicture(error);
    }

    protected void switchCamera() {
        if (checkModuleValidate(77)) {
            this.mGet.setCameraChanging(1);
            CamLog.m3d(CameraConstants.TAG, "Start to switch camera. current id = " + this.mCameraId);
            determineCameraIdBeforeChangingCamera();
            CamLog.m3d(CameraConstants.TAG, "Start to switch camera. next id = " + this.mCameraId);
            this.mGet.setSetting(Setting.KEY_SWAP_CAMERA, isRearCamera() ? "rear" : "front", true);
            SharedPreferenceUtilBase.setCameraId(getAppContext(), this.mCameraId);
            sIsFrontSwitching = false;
            restoreSettingMenus();
            this.mGet.setupSetting();
            changeCameraIdForSpecificMode();
            runSwitchCamera();
        }
    }

    protected void runSwitchCamera() {
        this.mGet.changeModule(true);
    }

    protected void determineCameraIdBeforeChangingCamera() {
        int i = 1;
        if (sIsFrontSwitching) {
            if (this.mCameraId == 1) {
                i = 2;
            }
            this.mCameraId = i;
            CamLog.m3d(CameraConstants.TAG, "switchCamera save front camera id mCameraId = " + this.mCameraId);
            SharedPreferenceUtil.saveFrontCameraId(this.mGet.getAppContext(), this.mCameraId);
        } else if (FunctionProperties.getCameraTypeFront() == 1) {
            int frontCameraId = SharedPreferenceUtil.getFrontCameraId(this.mGet.getAppContext());
            CamLog.m3d(CameraConstants.TAG, "switchCamera to mCameraId = " + this.mCameraId);
            if (!isRearCamera()) {
                frontCameraId = 0;
            }
            this.mCameraId = frontCameraId;
        } else if (FunctionProperties.getCameraTypeRear() == 1) {
            int rearCameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
            CamLog.m3d(CameraConstants.TAG, "switchCamera to mCameraId = " + this.mCameraId);
            if (isRearCamera()) {
                rearCameraId = 1;
            }
            this.mCameraId = rearCameraId;
        } else {
            if (!isRearCamera()) {
                i = 0;
            }
            this.mCameraId = i;
        }
    }

    protected void changeCameraIdForSpecificMode() {
        String mode = this.mGet.getCurSettingValue(Setting.KEY_MODE);
        if (CameraConstants.MODE_POPOUT_CAMERA.equals(mode) || CameraConstants.MODE_DUAL_POP_CAMERA.equals(mode) || CameraConstants.MODE_SLOW_MOTION.equals(mode) || CameraConstants.MODE_CINEMA.equals(mode)) {
            this.mCameraId = 0;
        }
        CamLog.m3d(CameraConstants.TAG, "Change camera id for specific Mode = " + this.mCameraId);
        SharedPreferenceUtilBase.setCameraId(getAppContext(), this.mCameraId);
    }

    public void setParameters(CameraParameters parameters) {
        if (this.mCameraDevice != null && this.mParamUpdater != null) {
            this.mCameraDevice.setParameters(parameters);
        }
    }

    public void setParamUpdater(CameraParameters parameters, String key, String value) {
        if (parameters != null) {
            if (!isRearCamera() && "on".equals(value)) {
                value = ParamConstants.FLASH_MODE_TORCH;
            }
            this.mParamUpdater.setParameters(parameters, key, value);
        }
    }

    public String getParamValue(String key) {
        if (this.mParamUpdater != null) {
            return this.mParamUpdater.getParamValue(key);
        }
        return null;
    }

    protected CameraParameters updateDeviceParameter() {
        if (this.mCameraDevice == null) {
            return null;
        }
        this.mCameraDevice.refreshParameters();
        return this.mCameraDevice.getParameters();
    }

    protected void revertParameterByLGSF(CameraParameters parameters) {
        CamLog.m3d(CameraConstants.TAG, "revertParameterByLGSF");
        if (this.mCameraDevice != null) {
            updateBackupParameters();
            this.mCameraDevice.restoreParameters(parameters);
            this.mSnapShotChecker.setLGSFParamState(0);
        }
    }

    protected void stopPreview() {
        CamLog.m3d(CameraConstants.TAG, "##[stopPreview] - start");
        resetFastShot();
        if (!(this.mCameraDevice == null || this.mCameraState == 0)) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(9);
            }
            this.mCameraDevice.stopPreview();
        }
        if (this.mCameraState > -2) {
            setCameraState(0);
        }
        setFingerDetectionListener(false);
        CamLog.m3d(CameraConstants.TAG, "##[stopPreview] - end");
    }

    protected synchronized void closeCamera() {
        CamLog.m3d(CameraConstants.TAG, "##[closeCamera] - start");
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setErrorCallback(null, null);
            CameraHolder.instance().release();
            this.mCameraDevice = null;
        }
        releaseParamUpdater();
        if (this.mCameraState > -2) {
            setCameraState(0);
        }
        CamLog.m3d(CameraConstants.TAG, "##[closeCamera] - end");
    }

    protected void releaseParamUpdater() {
        if (this.mParamUpdater != null) {
            this.mParamUpdater.releaseAllRequester();
        }
    }

    protected void setPreviewSize(CameraParameters parameters, String previewSize, boolean recordStart) {
        if (this.mParamUpdater != null && previewSize != null) {
            if (recordStart) {
                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PREVIEW_SIZE, previewSize, false);
                setupPreview(parameters);
                setCameraState(5);
                return;
            }
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, true);
        }
    }

    protected void setZSL(CameraParameters parameters, String zslValue) {
        if (this.mParamUpdater != null && parameters != null) {
            if (!FunctionProperties.isSupportedZSL(this.mCameraId)) {
                zslValue = "off";
            }
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_ZSL, zslValue);
        }
    }

    protected void setZSLBuffCount(CameraParameters parameters, String zslValue) {
        if (this.mParamUpdater != null && parameters != null) {
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_ZSL_BUFF_COUNT, zslValue);
        }
    }

    protected String getFocusSetting() {
        if (this.mCameraCapabilities == null || !this.mCameraCapabilities.isAFSupported()) {
            return ParamConstants.FOCUS_MODE_FIXED;
        }
        String focusMode = "auto";
        ListPreference listPref = this.mGet.getListPreference("focus-mode");
        if (listPref == null) {
            return focusMode;
        }
        focusMode = listPref.getValue();
        if (this.mCameraCapabilities.isContinousFocusSupported() && "auto".equals(focusMode)) {
            focusMode = this.mCameraCapabilities.isMWContinousFocusSupported() ? ParamConstants.FOCUS_MODE_MULTIWINDOWAF : ParamConstants.FOCUS_MODE_CONTINUOUS_PICTURE;
        }
        if (focusMode.equals(ParamConstants.FOCUS_MODE_MANUAL)) {
            return "normal";
        }
        return focusMode;
    }

    public String getCameraFocusMode() {
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                return parameters.get("focus-mode");
            }
        }
        return "auto";
    }

    public void setCameraFocusMode(String param) {
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters == null) {
                CamLog.m3d(CameraConstants.TAG, "Parameter's null, return ");
            } else if (param.equals(parameters.getFocusMode())) {
                CamLog.m3d(CameraConstants.TAG, "focus mode is equals so return ");
            } else {
                List<String> supportedFocusMode = parameters.getSupportedFocusModes();
                if (supportedFocusMode == null) {
                    CamLog.m3d(CameraConstants.TAG, "Focus is not supported, return ");
                } else if (!supportedFocusMode.contains(param)) {
                    CamLog.m3d(CameraConstants.TAG, "Required focus mode is not supported, return ");
                } else if (parameters != null) {
                    if (ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO.equals(param) || ParamConstants.FOCUS_MODE_CONTINUOUS_PICTURE.equals(param)) {
                        parameters.setFocusAreas(null);
                    }
                    parameters.setFocusMode(param);
                    CamLog.m3d(CameraConstants.TAG, "### setFocusMode-" + param);
                    try {
                        this.mCameraDevice.setParameters(parameters);
                    } catch (RuntimeException e) {
                        CamLog.m5e(CameraConstants.TAG, "setParameters failed: " + e);
                    }
                }
            }
        }
    }

    protected void setContinuousFocus(CameraParameters parameters, boolean recordStart) {
        String focusMode;
        if (!recordStart) {
            focusMode = getFocusSetting();
        } else if (this.mCameraCapabilities.isContinousFocusSupported()) {
            focusMode = ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO;
        } else {
            focusMode = ParamConstants.FOCUS_MODE_FIXED;
        }
        CamLog.m3d(CameraConstants.TAG, "### setFocusMode-" + focusMode);
        this.mParamUpdater.setParameters(parameters, "focus-mode", focusMode);
    }

    protected void setRecordingHint(CameraParameters parameters, String value, boolean setParam) {
        CamLog.m3d(CameraConstants.TAG, "setRecordingHint value = " + value);
        if (this.mParamUpdater != null && parameters != null) {
            this.mParamUpdater.setParameters(parameters, "recording-hint", value);
            if (setParam) {
                setParameters(parameters);
            }
        }
    }

    protected String getPreviewSize(String previewSize, String screenSize, String videoSize) {
        if (!"".equals(previewSize)) {
            return previewSize;
        }
        String fhdPreviewSize = "1920x1080";
        int[] screenSizeInt = Utils.sizeStringToArray(screenSize);
        int[] videoSizeInt = Utils.sizeStringToArray(videoSize);
        if (ParamConstants.VIDEO_3840_BY_2160.equals(videoSize)) {
            return "1920x1080";
        }
        if (videoSizeInt[0] <= screenSizeInt[0] && videoSizeInt[1] <= screenSizeInt[1]) {
            return CameraDeviceUtils.getPreviewSizeforMMSVideo(getAppContext(), videoSize.split("@")[0]);
        }
        if (isRecordingPriorityMode()) {
            return videoSize;
        }
        return screenSize;
    }

    public boolean isSlowMotionMode() {
        String videoSizeStr = getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        if (videoSizeStr == null) {
            return false;
        }
        if (CameraConstants.MODE_SLOW_MOTION.equals(getSettingValue(Setting.KEY_MODE))) {
            return true;
        }
        String[] videoSize = videoSizeStr.split("@");
        if (videoSize.length == 1 || Double.parseDouble(videoSize[1]) < 120.0d) {
            return false;
        }
        return true;
    }

    public boolean isLiveSnapshotSupported() {
        if (this.mCameraCapabilities == null) {
            return true;
        }
        if (isSlowMotionMode() || !this.mCameraCapabilities.isLiveSnapshotSupported()) {
            return false;
        }
        return true;
    }

    protected String getLiveSnapShotSize(CameraParameters parameters, String value) {
        if (this.mCameraCapabilities == null || !this.mCameraCapabilities.isLiveSnapshotSupported()) {
            return null;
        }
        List<String> supportedSize = CameraDeviceUtils.paramSplit(parameters.get(ParamConstants.KEY_VIDEO_SNAPSHOT_SIZE_SUPPORTED));
        if (value == null || supportedSize == null) {
            return null;
        }
        int[] videoSize = Utils.sizeStringToArray(value);
        if (videoSize[0] == 0) {
            return null;
        }
        float videoRatio = MathUtil.breakFloat(((float) videoSize[1]) / ((float) videoSize[0]), 2);
        String liveSnapshotSize = null;
        int supportedSizeCount = supportedSize.size();
        for (int i = 0; i < supportedSizeCount; i++) {
            liveSnapshotSize = (String) supportedSize.get(i);
            if (liveSnapshotSize != null) {
                CamLog.m3d(CameraConstants.TAG, "supportedSize = " + liveSnapshotSize);
                int[] liveSizeIntArray = Utils.sizeStringToArray(liveSnapshotSize);
                if (liveSizeIntArray[0] != 0 && Float.compare(videoRatio, MathUtil.breakFloat(((float) liveSizeIntArray[1]) / ((float) liveSizeIntArray[0]), 2)) == 0) {
                    value = liveSnapshotSize;
                    CamLog.m3d(CameraConstants.TAG, "liveSnapshotSize = " + liveSnapshotSize);
                    return liveSnapshotSize;
                }
            }
        }
        return liveSnapshotSize;
    }

    protected void setLiveSnapshotSize(CameraParameters parameters, String videoSize) {
        if (this.mParamUpdater != null && parameters != null && this.mCameraCapabilities != null) {
            String value = getLiveSnapShotSize(parameters, videoSize);
            this.mParamUpdater.setParamValue("picture-size", value, false);
            if (this.mGet.isOpticZoomSupported(null) && !FunctionProperties.isSameResolutionOpticZoom()) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PICTURE_SIZE_WIDE, value, false);
                CamLog.m3d(CameraConstants.TAG, "[opticzoom] set KEY_PICTURE_SIZE_WIDE livesnapshot : " + value);
            }
        }
    }

    protected void setVideoHDR(CameraParameters parameters, boolean isRecordingStarting) {
        if (this.mParamUpdater != null) {
            String videoHdrValue = "off";
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isVideoHDRSupported(this.mCameraId)) {
                if (isRecordingStarting) {
                    String hdrValue = getSettingValue("hdr-mode");
                    if (hdrValue != null) {
                        if ("0".equals(hdrValue)) {
                            videoHdrValue = "off";
                        } else if ("1".equals(hdrValue)) {
                            videoHdrValue = "on";
                        } else if ("2".equals(hdrValue)) {
                            videoHdrValue = FunctionProperties.getSupportedHal() == 2 ? "on" : "off";
                        }
                        if (CameraConstants.VIDEO_FHD_60FPS.equals(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)))) {
                            videoHdrValue = "off";
                        }
                    } else {
                        return;
                    }
                }
                videoHdrValue = "off";
            }
            CamLog.m3d(CameraConstants.TAG, "setVideoHDR, parameter = " + videoHdrValue);
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_VIDEO_HDR_MODE, videoHdrValue);
        }
    }

    protected void setPreviewFpsRange(CameraParameters parameters, boolean isRecordingStarted) {
        if (this.mParamUpdater != null && parameters != null) {
            String fps = SystemProperties.get(ParamConstants.FPS_RANGE_SYSTEM_PROPERTY_CAMCORDER, "15000,30000");
            String[] fpsValueArray = fps.split(",");
            if (isRecordingStarted) {
                String[] videoSize = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).getValue().split("@");
                String hfrValue = null;
                if (videoSize.length > 1) {
                    hfrValue = videoSize[1];
                }
                if (hfrValue != null) {
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_HFR, hfrValue);
                    if (Integer.valueOf(hfrValue).intValue() >= 60) {
                        fps = (Integer.valueOf(hfrValue).intValue() * 1000) + "," + (Integer.valueOf(hfrValue).intValue() * 1000);
                    } else {
                        fps = fpsValueArray[0] + "," + hfrValue + "000";
                    }
                } else {
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_HFR, "off");
                }
            } else {
                fps = MultimediaProperties.getCameraFPSRange(isRearCamera());
                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_HFR, "off");
            }
            if (CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                fps = "24000,24000";
            }
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PREVIEW_FPS_RANGE, fps);
            CamLog.m3d(CameraConstants.TAG, "setPreviewFpsRange = " + fps);
        }
    }

    protected void updateHDRParam(CameraParameters parameters, String value, boolean saveSetting, boolean setParam) {
        if (parameters != null) {
            if (FunctionProperties.isSupportedHDR(isRearCamera()) == 1) {
                if ("0".equals(value)) {
                    setParamUpdater(parameters, ParamConstants.KEY_ZSL, FunctionProperties.isSupportedZSL(this.mCameraId) ? "on" : "off");
                } else if ("1".equals(value) || "2".equals(value)) {
                    setParamUpdater(parameters, ParamConstants.KEY_ZSL, "off");
                }
                setupPreview(parameters);
            }
            setParamUpdater(parameters, "hdr-mode", value);
            setParamUpdater(parameters, ParamConstants.KEY_SCENE_MODE, "auto");
            if (setParam) {
                setParameters(parameters);
            }
            if (saveSetting) {
                this.mGet.setSetting("hdr-mode", value, true);
                this.mSnapShotChecker.setCurHDRSetting(value);
            }
        }
    }

    protected boolean checkCommonSettingValueForNightShot(CameraParameters params) {
        String mode = getSettingValue(Setting.KEY_MODE);
        if (!FunctionProperties.isSupportedMorphoNightShot()) {
            return false;
        }
        if ("mode_normal".equals(mode) || CameraConstants.MODE_BEAUTY.equals(mode)) {
            return true;
        }
        return false;
    }

    public boolean isZoomAvailable(boolean checkRecordingWait) {
        if (this.mCameraDevice == null) {
            return isRearCamera();
        }
        if (this.mCameraCapabilities == null) {
            return isRearCamera();
        }
        if (getCameraId() == 1 || (FunctionProperties.getCameraTypeFront() == 1 && getCameraId() == 2)) {
            return false;
        }
        if (this.mCameraCapabilities.isZoomSupported()) {
            if (isRearCamera()) {
                return true;
            }
            if (!isRearCamera() && isSupportFrontZoom()) {
                return true;
            }
        }
        return false;
    }

    protected boolean isSupportFrontZoom() {
        int pictureSizeFiveMega = getShotMode().contains(CameraConstants.MODE_SQUARE) ? 2097152 : 4823449;
        int[] pictureSize = getMaxResolutionWidthHeight();
        if (pictureSize == null) {
            return false;
        }
        int fileSize = pictureSize[0] * pictureSize[1];
        CamLog.m3d(CameraConstants.TAG, "fileSize =" + fileSize);
        CamLog.m3d(CameraConstants.TAG, "pictureSizeFiveMega =" + pictureSizeFiveMega);
        return fileSize > pictureSizeFiveMega;
    }

    protected int[] getMaxResolutionWidthHeight() {
        ListPreference pref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        if (pref != null) {
            CharSequence[] entryValues = pref.getEntryValues();
            if (entryValues != null) {
                return Utils.sizeStringToArray((String) entryValues[0]);
            }
        }
        return new int[]{0, 0};
    }

    public void setFlashTorch(CameraParameters parameters, boolean on, String flashMode, boolean setParam) {
        if (parameters != null && CameraDeviceUtils.isFlashSupported(parameters, flashMode)) {
            if (!on || (!"on".equals(flashMode) && !ParamConstants.FLASH_MODE_REAR_ON.equals(flashMode) && !"auto".equals(flashMode))) {
                setParamUpdater(parameters, "flash-mode", "off");
            } else if (!"auto".equals(flashMode)) {
                setParamUpdater(parameters, "flash-mode", ParamConstants.FLASH_MODE_TORCH);
            } else if (this.mCameraDevice == null || !this.mCameraDevice.isLowLightDetected(parameters)) {
                setParamUpdater(parameters, "flash-mode", "off");
            } else {
                setParamUpdater(parameters, "flash-mode", ParamConstants.FLASH_MODE_TORCH);
            }
            if (setParam) {
                setParameters(parameters);
            }
        }
    }

    protected void updateFlashParam(CameraParameters parameters, int flashMsg, boolean save) {
        boolean z = true;
        boolean on = false;
        CamLog.m3d(CameraConstants.TAG, "updateFlashParam : flashMsg = " + flashMsg);
        if (this.mCameraDevice != null && parameters != null) {
            boolean isSaveSetting = save;
            this.mCameraDevice.refreshParameters();
            if (parameters != null) {
                String flashMode = ParamUtils.getFlashMode(flashMsg);
                if (this.mCameraState == 6 || this.mCameraState == 7) {
                    isSaveSetting = isMaintainRecordingFlashValue();
                    if (flashMsg == 52 || flashMsg == 51 || flashMsg == 53) {
                        on = true;
                    }
                    setFlashTorch(parameters, on, flashMode, true);
                    setFlashMetaDataCallback(null);
                    this.mLocalParamForZoom = null;
                    this.mGet.setSetting("flash-mode", flashMode, isSaveSetting);
                    return;
                }
                if (isRearCamera()) {
                    setParamUpdater(parameters, "flash-mode", flashMode);
                } else {
                    if (flashMsg != 51) {
                        z = false;
                    }
                    setFlashTorch(parameters, z, flashMode, false);
                }
                if (FunctionProperties.getSupportedHal() == 2 || !FunctionProperties.isSupportedSuperZoom() || !checkModuleValidate(128) || CameraConstants.MODE_MANUAL_CAMERA.equals(getShotMode())) {
                    setParameters(parameters);
                } else {
                    this.mLocalParamForZoom = this.mCameraDevice.setSuperZoom(parameters);
                }
                setFlashMetaDataCallback(flashMode);
                this.mGet.setSetting("flash-mode", flashMode, isSaveSetting);
                this.mSnapShotChecker.setCurFlashSetting(flashMode);
            }
        }
    }

    protected boolean isMaintainRecordingFlashValue() {
        ListPreference listPref = this.mGet.getListPreference("flash-mode");
        if (listPref == null || !"auto".equals(listPref.loadSavedValue())) {
            return true;
        }
        return false;
    }

    protected void setFlashOnRecord(CameraParameters param, boolean on, boolean setParam) {
        if (this.mCameraDevice != null) {
            if (param == null) {
                param = this.mCameraDevice.getParameters();
            }
            setFlashTorch(param, on, this.mGet.getCurSettingValue("flash-mode"), setParam);
        }
    }

    protected void resetScreenOn() {
        this.mHandler.removeMessages(3);
        Activity act = this.mGet.getActivity();
        if (act != null) {
            act.getWindow().clearFlags(128);
        }
    }

    public void keepScreenOnAwhile() {
        this.mHandler.removeMessages(3);
        Activity act = this.mGet.getActivity();
        if (act != null) {
            act.getWindow().addFlags(128);
        }
        this.mHandler.sendEmptyMessageDelayed(3, 120000);
    }

    protected void clearScreenDelay() {
        Activity act = this.mGet.getActivity();
        if (act != null) {
            act.getWindow().clearFlags(128);
        }
    }

    public void keepScreenOn() {
        this.mHandler.removeMessages(3);
        Activity act = this.mGet.getActivity();
        if (act != null) {
            act.getWindow().addFlags(128);
        }
    }

    protected boolean setDisplayOrientation(boolean check) {
        int displayOrientation = CameraDeviceUtils.getDisplayOrientation(CameraDeviceUtils.getDisplayRotation(this.mGet.getActivity()), this.mCameraId);
        if (check && this.mDisplayOrientation == displayOrientation) {
            return false;
        }
        this.mDisplayOrientation = displayOrientation;
        CamLog.m3d(CameraConstants.TAG, "displayOrientation = " + displayOrientation);
        if (this.mInitialOrientation == -1) {
            this.mInitialOrientation = this.mDisplayOrientation;
            CamLog.m3d(CameraConstants.TAG, "mInitialOrientation is set to  " + this.mInitialOrientation);
        }
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setDisplayOrientation(displayOrientation);
        }
        return true;
    }

    public int getDisplayOrientation() {
        if (this.mDisplayOrientation == -1) {
            int degree = Utils.restoreDegree(this.mGet.getAppContext().getResources(), this.mGet.getOrientationDegree());
            CamLog.m3d(CameraConstants.TAG, "set mDisplayOrientation [" + degree + "]");
            this.mDisplayOrientation = degree;
        }
        return this.mDisplayOrientation;
    }

    public int getInitialOrientation() {
        return this.mInitialOrientation;
    }

    public int getCameraId() {
        return this.mCameraId;
    }

    public boolean isRearCamera() {
        return CameraDeviceUtils.isRearCamera(this.mCameraId);
    }

    public boolean isSupportedCropAngle() {
        if (FunctionProperties.getCameraTypeFront() != 2 || isRearCamera()) {
            return false;
        }
        return true;
    }

    protected void waitCameraStartUpThread() {
        try {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(8);
                this.mHandler.removeMessages(9);
                this.mHandler.removeMessages(5);
                this.mHandler.removeMessages(19);
            }
            if (this.mCameraStartUpThread != null) {
                this.mCameraStartUpThread.cancel();
                this.mStartPreviewPrerequisiteReady.open();
                if (this.mGet == null || !this.mGet.isPaused()) {
                    this.mCameraStartUpThread.join();
                } else {
                    CamLog.m3d(CameraConstants.TAG, "waitCameraStartUpThread wait for join till 5sec in onPause");
                    this.mCameraStartUpThread.join(CameraConstants.TOAST_LENGTH_LONG);
                }
                this.mCameraStartUpThread = null;
            }
        } catch (InterruptedException e) {
        }
    }

    protected void afterStopRecording() {
        if (this.mCameraDevice != null) {
            if (checkPreviewCoverVisibilityForRecording() && !getRecordingPreviewState(8)) {
                int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
                this.mGet.getCurPreviewBitmap((int) (((float) lcdSize[1]) * 0.5f), (int) (((float) lcdSize[0]) * 0.5f));
                this.mGet.setPreviewCoverVisibility(0, false, null, true, true);
            }
            stopPreview();
            if (!changeCameraAfterInAndOutZoom()) {
                releaseParamUpdater();
                CameraParameters parameters = this.mCameraDevice.getParameters();
                this.mHandler.sendEmptyMessage(8);
                addModuleRequester();
                this.mParamUpdater.setParameters(parameters, "zoom", Integer.toString(parameters.getZoom()));
                setupPreview(parameters);
                this.mHandler.sendEmptyMessage(9);
                this.mHandler.sendEmptyMessage(5);
                this.mSnapShotChecker.releaseSnapShotChecker();
            }
        }
    }

    public boolean getRecordingPreviewState(int stateType) {
        return false;
    }

    protected void cameraStartUpEnd() {
        this.mHandler.sendEmptyMessage(19);
        CameraDeviceUtils.dismissErrorAndFinish();
    }

    protected void setHDRMetaDataCallback(String hdrValue) {
        this.mLastHDR = 0;
        this.mCurrentHDR = 0;
        if (this.mCameraDevice == null || this.mHDRMetaDataCallback == null || FunctionProperties.isSupportedHDR(isRearCamera()) < 3) {
            CamLog.m3d(CameraConstants.TAG, "exit setHDRMetaDataCallback camera = " + this.mCameraDevice + " HDR mode = " + FunctionProperties.isSupportedHDR(isRearCamera()));
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "setHDRMetaDataCallback. mCurFlash = " + this.mCurFlash + ", hdrValue = " + hdrValue);
        if (this.mCurFlash != 3 && "".equals(hdrValue)) {
            updateIndicatorFromMetadataCallback(2, 4, false);
        }
        try {
            if ("2".equals(hdrValue)) {
                this.mCameraDevice.setBacklightDetectionCallback(this.mHandler, this.mHDRMetaDataCallback);
                return;
            }
            this.mCameraDevice.setBacklightDetectionCallback(this.mHandler, null);
            this.mSnapShotChecker.setLongshotAvailable(-1, -1, -1);
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, "Exception > " + e.getMessage());
        }
    }

    protected void setNightVisionDataCallback(boolean set) {
        if (this.mCameraDevice != null && this.mNightVisionMetaDataCallback != null && !"off".equals(getSettingValue(Setting.KEY_BINNING))) {
            CamLog.m3d(CameraConstants.TAG, "mNightVisionMetaDataCallback : " + set);
            this.mBinningRefreshCnt = 0;
            this.mCameraDevice.setLuxIndexMetadata(set ? this.mNightVisionMetaDataCallback : null);
        }
    }

    protected boolean checkFlashIndicatorChangeCondition() {
        return false;
    }

    protected void setFlashMetaDataCallback(String flashMode) {
        this.mLastFlash = 0;
        this.mCurFlash = 0;
        if (this.mCameraDevice != null && this.mFlashMetaDataCb != null && isRearCamera()) {
            if (this.mCameraCapabilities == null || this.mCameraCapabilities.isFlashSupported()) {
                CamLog.m3d(CameraConstants.TAG, "setFlashMetaDataCallback. mCurrentHDR = " + this.mCurrentHDR + ", flashMode = " + flashMode);
                if (this.mCurrentHDR != 3) {
                    updateIndicatorFromMetadataCallback(3, 4, false);
                }
                if ("auto".equals(flashMode)) {
                    this.mCameraDevice.setLowlightDetectionCallback(this.mHandler, this.mFlashMetaDataCb);
                    return;
                }
                int i;
                this.mCameraDevice.setLowlightDetectionCallback(this.mHandler, null);
                SnapShotChecker snapShotChecker = this.mSnapShotChecker;
                if ("off".equals(flashMode)) {
                    i = this.mCurrentHDR;
                } else {
                    i = -1;
                }
                snapShotChecker.setLongshotAvailable(-1, i, -1);
            }
        }
    }

    protected void setTrackingAFListener() {
        if (this.mGet.isFocusTrackingSupported()) {
            this.mTrackingAFListener = new C009910();
            if (this.mTrackingAFListener != null && this.mCameraDevice != null) {
                this.mCameraDevice.setObtDataListener(this.mTrackingAFListener);
            }
        }
    }

    protected void setFingerDetectionListener(boolean isSet) {
        CamLog.m3d(CameraConstants.TAG, "[finger detection] setFingerDetectionListener" + isSet);
        if (!isSupportedFingerDetection()) {
            return;
        }
        if ((!isSet || (isRearCamera() && getCameraId() == 2 && isFingerDetectionSupportedMode())) && !"off".equals(getSettingValue(Setting.KEY_FINGER_DETECTION))) {
            if (isSet) {
                informPreviewSizeToFingerDetectionManager(this.mParamUpdater.getParamValue(ParamConstants.KEY_PREVIEW_SIZE));
                this.mFingerDetectionListener = new C010011();
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (ModuleDeviceHandler.this.mCameraDevice != null) {
                            CamLog.m3d(CameraConstants.TAG, "finger detection listener set >" + ModuleDeviceHandler.this.mFingerDetectionListener);
                            ModuleDeviceHandler.this.mCameraDevice.setFingerDetectionDataListener(ModuleDeviceHandler.this.mFingerDetectionListener);
                        }
                    }
                }, 1000);
            } else {
                this.mFingerDetectionListener = null;
                if (this.mCameraDevice != null) {
                    CamLog.m3d(CameraConstants.TAG, "finger detection listener set >" + this.mFingerDetectionListener);
                    this.mCameraDevice.setFingerDetectionDataListener(this.mFingerDetectionListener);
                }
            }
            resetFingerDetection();
        }
    }

    protected void activateFingerDetection(boolean isOn, CameraParameters parameters, boolean isForced) {
        CamLog.m3d(CameraConstants.TAG, "[finger detection] activateFingerDetection" + isOn + " parameters:" + parameters + " isforced:" + isForced);
        if (!isSupportedFingerDetection()) {
            return;
        }
        if (!isOn || (isRearCamera() && getCameraId() == 2 && isFingerDetectionSupportedMode())) {
            if (parameters == null) {
                if (this.mCameraDevice != null) {
                    parameters = this.mCameraDevice.getParameters();
                } else {
                    return;
                }
            }
            boolean onoff = isOn && "on".equals(getSettingValue(Setting.KEY_FINGER_DETECTION));
            CamLog.m3d(CameraConstants.TAG, "[finger detection] onoff" + isOn);
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_FINGER_DETECTION, onoff ? "on" : "off");
            if (isForced) {
                setParameters(parameters);
            }
        }
    }

    protected boolean isFingerDetectionSupportedMode() {
        return true;
    }

    private static void throwIfCameraDisabled(Activity activity) throws CameraDisabledException {
        if (((DevicePolicyManager) activity.getSystemService("device_policy")).getCameraDisabled(null)) {
            throw new CameraDisabledException();
        }
    }

    private CameraProxy openCamera(Activity activity, int cameraId, Handler handler, final CameraOpenErrorCallback cb) {
        final int newCameraId = convertCameraId(cameraId);
        try {
            throwIfCameraDisabled(activity);
            return CameraHolder.instance().open(handler, newCameraId, cb, false, activity);
        } catch (CameraDisabledException e) {
            handler.post(new Runnable() {
                public void run() {
                    if (cb != null && cb != null) {
                        cb.onCameraDisabled(newCameraId);
                    }
                }
            });
            return null;
        }
    }

    protected int convertCameraId(int cameraId) {
        if (FunctionProperties.isSupportedOpticZoom()) {
            return ((cameraId == 0 || cameraId == 2) && this.mGet.isOpticZoomSupported(null)) ? 3 : cameraId;
        } else {
            return cameraId;
        }
    }

    private void retryOpenCamera(boolean isCanceled) {
        int retry_cnt = 0;
        while (true) {
            int i = retry_cnt;
            retry_cnt = i + 1;
            if (i < 20 && !isCanceled) {
                CamLog.m9v(CameraConstants.TAG, "CameraDevice is null, retry count:" + retry_cnt);
                this.mCameraDevice = openCamera(this.mGet.getActivity(), this.mCameraId, this.mHandler, null);
                if (this.mCameraDevice == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    return;
                }
            }
            return;
        }
    }

    public void updatePictureSizeList(CameraParameters parameters) {
        int cameraType = getPictureSizePreferenceId();
        if (isUpdatePictureSizeList(cameraType) && isUpdatePictureSizeListForSubCamera(cameraType)) {
            DebugUtil.checkTimeLog("[ConfigAuto] make picture config time", true);
            CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] make picture config time - start");
            ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey("mode_normal", this.mCameraId));
            int entrySize = 3;
            float defaultRatio = 1.3333334f;
            if (listPref != null) {
                entrySize = listPref.getEntries().length;
                int[] defaultSize = Utils.sizeStringToArray(listPref.getDefaultValue());
                if (defaultSize[1] != 0) {
                    defaultRatio = ((float) defaultSize[0]) / ((float) defaultSize[1]);
                }
                CamLog.m3d(CameraConstants.TAG, "[ConfigAuto] default ratio : " + defaultRatio);
            }
            String[] pictureSizeListFromParam = CameraPictureSizeUtil.getPictureSizeList(this.mGet.getAppContext(), parameters, entrySize, this.mCameraId, defaultRatio);
            if (pictureSizeListFromParam == null) {
                if (ModelProperties.isUserDebugMode()) {
                    this.mGet.runOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            CameraDeviceUtils.showErrorAndFinish(ModuleDeviceHandler.this.mGet.getActivity(), C0088R.string.camera_error_title, CameraPictureSizeUtil.sErrorStringId, CameraPictureSizeUtil.sErrorPictureSize);
                        }
                    });
                }
            } else if (this.mGet.updatePictureSizeListPreference(listPref, pictureSizeListFromParam, CameraPictureSizeUtil.sDefaultPictureSize, this.mCameraId, getShotMode())) {
                StringBuilder pictureList = new StringBuilder(pictureSizeListFromParam[0]);
                for (int i = 1; i < pictureSizeListFromParam.length; i++) {
                    pictureList.append("," + pictureSizeListFromParam[i]);
                }
                SharedPreferenceUtilBase.saveCameraPictureSizeList(getAppContext(), cameraType, pictureList.toString());
                SharedPreferenceUtilBase.saveCameraDefaultPictureSize(getAppContext(), cameraType, CameraPictureSizeUtil.sDefaultPictureSize);
            } else {
                CamLog.m11w(CameraConstants.TAG, "[ConfigAuto] fail updatePictureSizeListPreference, picture size is not updated");
                return;
            }
            DebugUtil.checkTimeLog("[ConfigAuto] make picture config time", false);
        }
    }

    protected int getPictureSizePreferenceId() {
        if (this.mCameraId == 0) {
            return 0;
        }
        if (this.mCameraId == 1) {
            return 1;
        }
        if (FunctionProperties.getCameraTypeRear() == 1 && this.mCameraId == 2) {
            return 2;
        }
        if (FunctionProperties.getCameraTypeFront() == 1 && this.mCameraId == 2) {
            return 3;
        }
        return 0;
    }

    protected boolean isUpdatePictureSizeList(int cameraType) {
        if (cameraType == 0) {
            if (!(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS == null && "".equals(SharedPreferenceUtilBase.getCameraPictureSizeList(getAppContext(), cameraType, "")))) {
                return false;
            }
        } else if (cameraType == 1 && !(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_FRONT_SUPPORTED_ITEMS == null && "".equals(SharedPreferenceUtilBase.getCameraPictureSizeList(getAppContext(), cameraType, "")))) {
            return false;
        }
        return true;
    }

    protected boolean isUpdatePictureSizeListForSubCamera(int cameraType) {
        if (cameraType == 2) {
            if (!(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_REAR_SUPPORTED_ITEMS == null && "".equals(SharedPreferenceUtilBase.getCameraPictureSizeList(getAppContext(), cameraType, "")))) {
                return false;
            }
        } else if (cameraType == 3 && !(ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_SUB_FRONT_SUPPORTED_ITEMS == null && "".equals(SharedPreferenceUtilBase.getCameraPictureSizeList(getAppContext(), cameraType, "")))) {
            return false;
        }
        return true;
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                setupPreview(null);
                return true;
            case 3:
                clearScreenDelay();
                this.mGet.getActivity().finish();
                return true;
            case 4:
                this.mCameraStartUpThread = null;
                return true;
            case 5:
                setDisplayOrientation(true);
                return true;
            case 6:
                handleSwitchCamera();
                return true;
            case 8:
                initializeAfterCameraOpen();
                return true;
            case 9:
                startPreviewDone();
                initializeAfterStartPreviewDone();
                return true;
            case 10:
                this.mCameraStartUpThread = null;
                this.mOpenCameraFail = true;
                CameraDeviceUtils.showErrorAndFinish(this.mGet.getActivity(), C0088R.string.camera_error_title, C0088R.string.cannot_open_camera);
                return true;
            case 11:
                this.mCameraStartUpThread = null;
                this.mCameraDisabled = true;
                CameraDeviceUtils.showErrorAndFinish(this.mGet.getActivity(), C0088R.string.camera_error_title, C0088R.string.camera_disabled);
                return true;
            case 12:
                this.mCameraStartUpThread = null;
                this.mCameraDisabled = true;
                CameraDeviceUtils.showErrorAndFinish(this.mGet.getActivity(), C0088R.string.camera_application_stopped, C0088R.string.camera_driver_needs_reset);
                return true;
            case 14:
                afterStopRecording();
                return true;
            case 15:
                if (this.mGet.isPaused()) {
                    CamLog.m3d(CameraConstants.TAG, "do not process oneshot preview onPause state");
                    return true;
                }
                oneShotPreviewCallbackDone();
                oneShotPreviewCallbackDoneAfter();
                return true;
            case 16:
                if (this.mCameraState != 1 || !checkModuleValidate(207)) {
                    return true;
                }
                takePicture();
                return true;
            case 19:
                setSuperZoomSync();
                return true;
            case 20:
                stopPreview();
                return true;
            case 50:
            case 51:
            case 52:
            case 53:
                setFlashSetting(msg);
                return true;
            default:
                return false;
        }
    }

    protected void setFlashSetting(Message msg) {
        if (this.mCameraDevice != null) {
            if (!(isRearCamera() || this.mCameraCapabilities == null || !this.mCameraCapabilities.isFlashSupported())) {
                setFlashLevelControlMenu(msg.what == 51);
            }
            updateFlashParam(this.mCameraDevice.getParameters(), msg.what, true);
        }
    }

    protected void setFlashLevelControlMenu(boolean isOn) {
    }

    protected void setCameraIdBeforeChange(boolean showAni, int cameraId, boolean forceChange) {
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    protected boolean isFastShotSupported() {
        if (getShotMode().contains(CameraConstants.MODE_SQUARE) || getShotMode().equals(CameraConstants.MODE_FLASH_JUMPCUT)) {
            return false;
        }
        return FunctionProperties.isSupportedFastShot();
    }

    protected boolean checkFastShot() {
        return false;
    }

    protected void updateCapabilities(CameraParameters param) {
        updatePictureSizeList(param);
        this.mCameraCapabilities = new CameraCapabilities(param);
        if (this.mGet.isOpticZoomSupported(null) && this.mCameraId == 2) {
            this.mCameraCapabilities.disalbeFocusFeature();
        }
    }

    public boolean isSupportedFingerDetection() {
        if (this.mCameraCapabilities != null) {
            return this.mCameraCapabilities.isFingerDetectionSupported();
        }
        return false;
    }

    public String getPictureSizeParamKey(int select_camera) {
        if (!this.mGet.isOpticZoomSupported(null) || FunctionProperties.isSameResolutionOpticZoom()) {
            return "picture-size";
        }
        if (select_camera == 0) {
            return this.mCameraId == 0 ? "picture-size" : ParamConstants.KEY_PICTURE_SIZE_WIDE;
        } else {
            if (select_camera == 1) {
                return this.mCameraId == 0 ? ParamConstants.KEY_PICTURE_SIZE_WIDE : "picture-size";
            } else {
                return "picture-size";
            }
        }
    }

    protected void startPreviewDone() {
    }

    protected void setSuperZoomSync() {
    }

    protected void handleSwitchCamera() {
    }

    protected void initializeAfterCameraOpen() {
    }

    protected void initializeAfterStartPreviewDone() {
    }

    protected void oneShotPreviewCallbackDone() {
    }

    protected void oneShotPreviewCallbackDoneAfter() {
    }

    protected void setPreviewCallbackAll(boolean set) {
    }

    protected void changeRequester() {
    }

    protected void setZoomCompensation(CameraParameters parameters) {
    }

    protected void updateLightFrameParam(int lightFrameMsg) {
    }

    protected boolean takePicture() {
        return true;
    }

    protected void updateIndicatorFromMetadataCallback(int type, int visible, boolean useAnim) {
    }

    protected void updateManualSettingValueFromMetadataCallback(float currentWB, float currentEV, float currentISO, float currentShutterSpeed, float currentMFStep) {
    }

    protected void updateManualFocusValueFromMetadataCallback(float currentMFStep) {
    }

    protected void updateWBValueFromMetadataCallback(float currentWB) {
    }

    protected double[] getSSKeyArray() {
        return null;
    }

    protected String[] getSSValueArray() {
        return null;
    }

    protected void changeViewMode() {
    }

    protected void changeViewMode(int viewMode) {
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    protected void restoreSteadyCamSetting() {
    }

    protected void restoreTilePreviewSetting() {
    }

    protected boolean isAvailableTilePreview() {
        return false;
    }

    protected void restoreSettingValue(String key) {
    }

    protected void setGestureEngineForQuickShot() {
    }

    protected void restoreSettingMenus() {
    }

    public boolean checkStorage(int checkFor, int storageType) {
        return true;
    }

    public boolean checkStorage(int checkFor, int storageType, boolean isNeedCheckCacheSize) {
        return true;
    }

    protected void cancelQuickShotTaking() {
    }

    protected void drawTrackingAF(Rect rect) {
    }

    protected boolean isFastShotAvailable(int checkItem) {
        return false;
    }

    public void resetFastShot() {
    }

    protected boolean checkPictureCallbackRunnable(boolean doingNow) {
        return true;
    }

    protected void onDropTakePicture(int error) {
    }

    public boolean checkPreviewCoverVisibilityForRecording() {
        return false;
    }

    protected void updateBinningStateFromMetadata(float currentLux) {
    }

    protected void handleBinningIconUI(boolean isShow, int whereFrom) {
    }

    protected String getBinningType() {
        return "none";
    }

    public boolean isRecordingPriorityMode() {
        return false;
    }
}
