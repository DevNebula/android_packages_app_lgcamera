package com.lge.camera.app;

import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.p000v4.view.PointerIconCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.BarView.BarManagerListener;
import com.lge.camera.components.ShutterButton;
import com.lge.camera.components.ShutterButton.OnShutterButtonListener;
import com.lge.camera.components.ShutterButtonBase;
import com.lge.camera.components.ShutterButtonBase.OnShutterZoomListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.device.CameraCapabilities;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.ImageRegisterRequest;
import com.lge.camera.file.MediaSaveService;
import com.lge.camera.file.MediaSaveService.OnMediaSavedListener;
import com.lge.camera.file.MediaSaveService.OnQueueStatusListener;
import com.lge.camera.file.SaveRequest;
import com.lge.camera.managers.AdvancedSelfieManager;
import com.lge.camera.managers.BarManager;
import com.lge.camera.managers.BinningManager;
import com.lge.camera.managers.CaptureButtonInterface;
import com.lge.camera.managers.CaptureButtonManager;
import com.lge.camera.managers.ConeUIManagerInterface.OnConeViewModeButtonListener;
import com.lge.camera.managers.DoubleCameraManager;
import com.lge.camera.managers.DoubleCameraManager.CameraSwitchingInterface;
import com.lge.camera.managers.FingerDetectionManager;
import com.lge.camera.managers.FlipManager;
import com.lge.camera.managers.FocusManager;
import com.lge.camera.managers.InAndOutZoomManager;
import com.lge.camera.managers.QuickButtonManager;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.managers.QuickclipManagerIF.DrawerShowOption;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.managers.TimerManager.TimerType;
import com.lge.camera.managers.ZoomManager;
import com.lge.camera.managers.ZoomManagerBase.ZoomInterface;
import com.lge.camera.managers.ext.QRCodeManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.ModeItem;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.ChildSettingRunnable;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SmartcamUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.TimeIntervalChecker;
import com.lge.camera.util.Utils;
import com.lge.hardware.LGCamera;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseModule extends ModuleInterfaceImpl implements OnQueueStatusListener, CaptureButtonInterface, ZoomInterface, BarManagerListener, CameraSwitchingInterface {
    protected static final int RECORDING_IN_AND_OUT_ZOOM_PLAN_A = 0;
    protected static final int RECORDING_IN_AND_OUT_ZOOM_PLAN_B = 1;
    protected final int ANIMATION_STOP_TIME = 300;
    protected final int ASSISTANT_RETRY_DELAY = 100;
    protected boolean isNeedHideDetailView = false;
    protected HandlerRunnable mAssistantNightVisionRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- mLowLightState = " + BaseModule.this.mLowLightState);
            if (BaseModule.this.mLowLightState == -1) {
                CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- Night vision callback is not called yet, just take picture");
                BaseModule.this.mGet.setAssistantFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
                BaseModule.this.handleVoiceAssitantIntent();
                return;
            }
            if (BaseModule.this.mLowLightState == 1) {
                BaseModule.this.mLowLightState = 2;
                BaseModule.this.mBinningManager.setBinningPictureSizeDirect(0);
                BaseModule.this.mBinningManager.setBinningEnabled(true);
            } else if (BaseModule.this.mLowLightState == 0) {
                BaseModule.this.mGet.showToast(BaseModule.this.getAppContext().getString(C0088R.string.assistant_night_vision_over_10lux), CameraConstants.TOAST_LENGTH_SHORT);
            }
            BaseModule.this.mGet.setAssistantFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
            BaseModule.this.handleVoiceAssitantIntent();
        }
    };
    protected BarManager mBarManager = new BarManager(this);
    private int mBeforeZoomCameraId = -1;
    protected BinningManager mBinningManager = new BinningManager(this);
    protected LGCamera mCameraDevice2 = null;
    protected int mCameraIdBeforeInAndOutZoom = 0;
    private boolean mCameraReOpenAfterInAndOutZooom = false;
    protected CaptureButtonManager mCaptureButtonManager = new CaptureButtonManager(this);
    protected HandlerRunnable mChangeToAttachModule = new HandlerRunnable(this) {
        public void handleRun() {
            BaseModule.this.mGet.changeToAttachModule();
        }
    };
    protected HashMap<String, ChildSettingRunnable> mChildSettingMap = null;
    protected ChildSettingRunnable mChildSettingUpdater_LivePhoto = new C014227();
    protected ChildSettingRunnable mChildSettingUpdater_auCloud = new C013924();
    protected ChildSettingRunnable mChildSettingUpdater_finger_detection = new C013722();
    protected ChildSettingRunnable mChildSettingUpdater_frameGrid = new C013218();
    protected ChildSettingRunnable mChildSettingUpdater_hdr = new C013117();
    protected ChildSettingRunnable mChildSettingUpdater_lens_select = new C014429();
    protected ChildSettingRunnable mChildSettingUpdater_lightFrame = new C013016();
    protected ChildSettingRunnable mChildSettingUpdater_manual_camera_graphy = new C014025();
    protected ChildSettingRunnable mChildSettingUpdater_manual_video_steady = new C013621();
    protected ChildSettingRunnable mChildSettingUpdater_motionQuickview = new C013520();
    protected ChildSettingRunnable mChildSettingUpdater_pictureSize = new C01749();
    protected ChildSettingRunnable mChildSettingUpdater_qr = new C014126();
    protected ChildSettingRunnable mChildSettingUpdater_saveDirection = new C013319();
    protected ChildSettingRunnable mChildSettingUpdater_signature = new C016647();
    protected ChildSettingRunnable mChildSettingUpdater_storage = new C012815();
    protected ChildSettingRunnable mChildSettingUpdater_sw_pixel_binning = new C014328();
    protected ChildSettingRunnable mChildSettingUpdater_swapCamera = new C013823();
    protected ChildSettingRunnable mChildSettingUpdater_tagLocation = new C012714();
    protected ChildSettingRunnable mChildSettingUpdater_timer = new C012613();
    protected ChildSettingRunnable mChildSettingUpdater_trackingAF = new C012512();
    protected ChildSettingRunnable mChildSettingUpdater_videoSize = new C012310();
    protected ChildSettingRunnable mChildSettingUpdater_voiceShutter = new C012411();
    protected DoubleCameraManager mDoubleCameraManager = new DoubleCameraManager(this);
    protected FingerDetectionManager mFingerDetectionManager = new FingerDetectionManager(this);
    protected FlipManager mFlipManager = new FlipManager(this);
    protected boolean mGraphyListOn = false;
    protected HandlerRunnable mHideZoomBar = new HandlerRunnable(this) {
        public void handleRun() {
            BaseModule.this.hideZoomBar();
        }
    };
    private long mInAndOutAnimationStartTime = 0;
    protected TimeIntervalChecker mIntervalChecker = new TimeIntervalChecker();
    private boolean mIsAngleButtonTouched = false;
    private boolean mIsBindingListner = false;
    protected boolean mIsBinningSettingEnabled = false;
    protected boolean mIsCollageShared = false;
    private boolean mIsCropAnimationStarted = false;
    private Object mLockUpdateZoomParam = new Object();
    protected OnMediaSavedListener mOnMediaSavedListener;
    private long mPreviousPreviewCaptureTime = 0;
    protected QRCodeManager mQRCodeManager;
    protected QuickButtonManager mQuickButtonManager = new QuickButtonManager(this);
    protected boolean mRecordingButtonPressed = false;
    private int mRecordingInAndOutZoomPlan = 0;
    protected int mRecordingPreviewState = 0;
    protected boolean mShowBinningToastCondition = true;
    protected ZoomManager mZoomManager = new InAndOutZoomManager(this);

    /* renamed from: com.lge.camera.app.BaseModule$10 */
    class C012310 extends ChildSettingRunnable {
        C012310() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.setSetting(key, value, true);
            ListPreference listPref = prefObject instanceof ListPreference ? (ListPreference) prefObject : null;
            if (listPref != null && BaseModule.this.mCameraDevice != null) {
                BaseModule.this.updateSecondCameraSettings(key, value, true);
                BaseModule.this.setupVideosize(BaseModule.this.mCameraDevice.getParameters(), listPref);
                if (BaseModule.this.isVideoCaptureMode()) {
                    if (ModelProperties.isMTKChipset()) {
                        CamLog.m3d(CameraConstants.TAG, "The MTK model updates picture size.");
                        BaseModule.this.mGet.setPreviewCoverVisibility(0, true);
                        BaseModule.this.stopPreview();
                    } else {
                        BaseModule.this.mGet.setPreviewCoverVisibility(0, true);
                    }
                    BaseModule.this.showFrameGridView("off", false);
                    BaseModule.this.mHandler.sendEmptyMessage(61);
                }
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$11 */
    class C012411 extends ChildSettingRunnable {
        C012411() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.updateVoiceShutter(value);
            BaseModule.this.updateIndicator(1, 0, false);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$12 */
    class C012512 extends ChildSettingRunnable {
        C012512() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            boolean z = false;
            BaseModule.this.updateTrackingAF(value);
            if (BaseModule.this.mFocusManager != null && "off".equals(value)) {
                BaseModule.this.mFocusManager.setTrackingFocusState(false);
                if (CameraConstants.MODE_MANUAL_CAMERA.equals(BaseModule.this.getShotMode())) {
                    FocusManager focusManager = BaseModule.this.mFocusManager;
                    if (!BaseModule.this.isManualFocusMode()) {
                        z = true;
                    }
                    focusManager.setAFPointVisible(z);
                }
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$13 */
    class C012613 extends ChildSettingRunnable {
        C012613() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.updateTimer(value);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$14 */
    class C012714 extends ChildSettingRunnable {
        C012714() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            if (CheckStatusManager.isSystemSettingUseLocation(BaseModule.this.mGet.getActivity().getContentResolver())) {
                BaseModule.this.updateTagLocation(value);
                LocationManager locationManager = (LocationManager) BaseModule.this.mGet.getAppContext().getSystemService("location");
                if (locationManager != null && (locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network"))) {
                    BaseModule.this.getHandler().sendEmptyMessage(60);
                }
                if (BaseModule.this.mInitGuideManager.checkLocationPermissionCondition()) {
                    BaseModule.this.mGet.showLocationPermissionRequestDialog(true);
                    return;
                }
                return;
            }
            BaseModule.this.showDialog(12);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$15 */
    class C012815 extends ChildSettingRunnable {
        C012815() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            if (!CameraConstants.STORAGE_NAME_EXTERNAL.equals(value) || !BaseModule.this.isStorageRemoved(1)) {
                boolean isCnas = CameraConstants.STORAGE_NAME_NAS.equals(value);
                if (!isCnas || !BaseModule.this.isStorageRemoved(2)) {
                    BaseModule.this.updateStorage(value);
                    boolean isAvailable = BaseModule.this.checkStorage();
                    if (isCnas && isAvailable) {
                        BaseModule.this.changeTilePreviewSetting();
                        BaseModule.this.mGet.postOnUiThread(new HandlerRunnable(BaseModule.this) {
                            public void handleRun() {
                                BaseModule.this.showDialog(147);
                            }
                        }, 0);
                    } else {
                        BaseModule.this.restoreTilePreviewSetting();
                        if ("on".equals(BaseModule.this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
                            BaseModule.this.mGet.thumbnailListInit();
                        }
                    }
                    boolean isInternalStorage = CameraConstants.STORAGE_NAME_INTERNAL.equals(value);
                    FunctionProperties.getSupportedBurstShotDuration(isInternalStorage, true, BaseModule.this.isRearCamera());
                    FunctionProperties.getSupportedBurstShotMaxCount(isInternalStorage, true, BaseModule.this.isRearCamera());
                    BaseModule.this.notifyChangeChildSetting(Setting.KEY_STORAGE);
                }
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$16 */
    class C013016 extends ChildSettingRunnable {
        C013016() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            int msg = 57;
            if ("on".equals(value)) {
                msg = 57;
            } else if ("off".equals(value)) {
                msg = 56;
            }
            if (BaseModule.this.mHandler != null) {
                BaseModule.this.mHandler.sendEmptyMessage(msg);
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$17 */
    class C013117 extends ChildSettingRunnable {
        C013117() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.updateHDRSettingValue(value);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$18 */
    class C013218 extends ChildSettingRunnable {
        C013218() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.updateFrameGrid(value);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$19 */
    class C013319 extends ChildSettingRunnable {
        C013319() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.updateSaveDirection(value);
            if (BaseModule.this.mCameraDevice != null) {
                CameraParameters parameters = BaseModule.this.mCameraDevice.getParameters();
                if (BaseModule.this.mFlipManager.isPreProcessPictureFlip()) {
                    BaseModule.this.setParameters(BaseModule.this.mFlipManager.setFlipParam(parameters, 0, false));
                }
                if (BaseModule.this.isLivePhotoEnabled()) {
                    BaseModule.this.mLivePhotoManager.restartLivePhoto();
                }
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$1 */
    class C01341 implements OnConeViewModeButtonListener {
        C01341() {
        }

        public void onConeAutoViewButtonClick() {
            if (BaseModule.this.mReviewThumbnailManager.isActivatedSelfieQuickView()) {
                BaseModule.this.mReviewThumbnailManager.hideQuickView(true);
            }
            BaseModule.this.sendChangeViewMsg(1);
        }

        public void onConeManualCameraButtonClick() {
            BaseModule.this.sendChangeViewMsg(2);
        }

        public void onConeManualCameraVideoButtonClick() {
            BaseModule.this.sendChangeViewMsg(3);
        }

        public boolean checkOnConeMenuClicked(int mode) {
            return BaseModule.this.checkConeMenuClicked(mode);
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$20 */
    class C013520 extends ChildSettingRunnable {
        C013520() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.updateMotionQuickview(value);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$21 */
    class C013621 extends ChildSettingRunnable {
        C013621() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "steady cam setting");
            BaseModule.this.mGet.setSetting(key, value, true);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$22 */
    class C013722 extends ChildSettingRunnable {
        C013722() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "finger detection setting");
            BaseModule.this.mGet.setSetting(key, value, true);
            CameraParameters params = BaseModule.this.mCameraDevice.getParameters();
            if ("on".equals(BaseModule.this.getSettingValue(Setting.KEY_FINGER_DETECTION))) {
                BaseModule.this.activateFingerDetection(true, params, true);
                BaseModule.this.setFingerDetectionListener(true);
                return;
            }
            BaseModule.this.activateFingerDetection(false, params, true);
            BaseModule.this.setFingerDetectionListener(false);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$23 */
    class C013823 extends ChildSettingRunnable {
        C013823() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            BaseModule.this.mGet.removeSettingMenu(true, true);
            if (BaseModule.this.mQuickButtonManager != null) {
                BaseModule.this.mQuickButtonManager.updateButton(100);
                BaseModule.this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_setting_expand, 0, false);
                BaseModule.this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_swap_camera, 0, false);
                BaseModule.this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_swap_camera_focusable, 0, false);
                BaseModule.this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_flash, 0, false);
                BaseModule.this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_light_frame, 0, false);
            }
            if (BaseModule.this.mHandler != null) {
                CamLog.m7i(CameraConstants.TAG, "-swap- mChildSettingUpdater_swapCamera");
                if (!BaseModule.this.mGet.isAnimationShowing()) {
                    BaseModule.this.mHandler.removeMessages(6);
                    BaseModule.this.mHandler.sendEmptyMessage(6);
                    BaseModule.this.mGet.startCameraSwitchingAnimation(1);
                }
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$24 */
    class C013924 extends ChildSettingRunnable {
        C013924() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            ApplicationInfo info = null;
            try {
                info = BaseModule.this.mGet.getActivity().getPackageManager().getApplicationInfo("com.kddi.android.auclouduploader", 128);
            } catch (NameNotFoundException e) {
                CamLog.m4d(CameraConstants.TAG, "Au Cloud cannot be founded:", e);
            }
            if (info == null || info.enabled) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.kddi.android.auclouduploader", "com.kddi.android.auclouduploader.activity.AutoUploadSettingActivity"));
                intent.addFlags(67108864);
                try {
                    BaseModule.this.mGet.getActivity().startActivity(intent);
                    return;
                } catch (ActivityNotFoundException ex) {
                    CamLog.m6e(CameraConstants.TAG, "AU Cloud App is not exist", ex);
                    return;
                }
            }
            BaseModule.this.mDialogManager.showDialogPopup(6);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$25 */
    class C014025 extends ChildSettingRunnable {
        C014025() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "graphy setting");
            if ("on".equals(value)) {
                BaseModule.this.mGraphyListOn = true;
            } else {
                BaseModule.this.mGraphyListOn = false;
            }
            BaseModule.this.mGet.setSetting(key, value, true);
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$26 */
    class C014126 extends ChildSettingRunnable {
        C014126() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "qr setting : " + value);
            BaseModule.this.mGet.setSetting(key, value, true);
            if ("on".equals(value)) {
                BaseModule.this.onStartQRCodeClicked();
            } else {
                BaseModule.this.onStopQRCodeClicked();
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$27 */
    class C014227 extends ChildSettingRunnable {
        C014227() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            if (BaseModule.this.mLivePhotoManager != null) {
                BaseModule.this.setSetting(Setting.KEY_LIVE_PHOTO, value, true);
                if ("on".equals(value)) {
                    BaseModule.this.mLivePhotoManager.enableLivePhoto();
                } else {
                    BaseModule.this.mLivePhotoManager.disableLivePhoto();
                }
            }
        }

        public boolean checkChildAvailable() {
            return BaseModule.this.isSettingMenuItemAvailable();
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$28 */
    class C014328 extends ChildSettingRunnable {
        C014328() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "[NightVision] binning setting : " + value);
            boolean isPrevBinningState = BaseModule.this.getBinningEnabledState();
            BaseModule.this.mGet.setSetting(key, value, true);
            if ("on".equals(value)) {
                BaseModule.this.mIsBinningSettingEnabled = true;
                BaseModule.this.mShowBinningToastCondition = true;
            } else {
                BaseModule.this.mBinningManager.setBinningEnabled(false);
                if (isPrevBinningState) {
                    BaseModule.this.mBinningManager.setBinningPictureSizeDirect(0);
                }
                BaseModule.this.mLowLightState = -1;
                BaseModule.this.mIsBinningSettingEnabled = "manual".equals(value);
                if (BaseModule.this.getShotMode().contains(CameraConstants.MODE_SMART_CAM)) {
                    BaseModule.this.onBinningIconVisible(false, 1);
                }
            }
            BaseModule.this.mIsBinningManualOff = false;
            BaseModule.this.mBinningManager.resetManuallyOffState();
            BaseModule.this.setNightVisionParameters(BaseModule.this.mIsBinningSettingEnabled, true);
            BaseModule.this.mBinningRefreshCnt = 0;
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$29 */
    class C014429 extends ChildSettingRunnable {
        C014429() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "[Lens] Lens setting : " + value);
            CheckStatusManager.setSystemSettingLensType(BaseModule.this.mGet.getActivity().getContentResolver(), value);
            BaseModule.this.mGet.setSetting(key, value, true);
            BaseModule.this.mGet.refreshUspZone(false);
            BaseModule.this.mGet.setUspVisibility(8);
        }

        public boolean checkChildAvailable() {
            return BaseModule.this.isSettingMenuItemAvailable();
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$30 */
    class C014630 implements OnMediaSavedListener {
        C014630() {
        }

        public void onMediaSaved(Uri uri) {
            if (uri != null) {
                BaseModule.this.mReviewThumbnailManager.updateThumbnail(uri, false, false, true, false);
                BaseModule.this.mGet.requestNotifyNewMediaonActivity(uri, true);
                CamLog.m7i(CameraConstants.TAG, "[Tile] add Video Thumb getShotMode : " + BaseModule.this.getShotMode());
                BaseModule.this.mGet.onNewItemAdded(uri, Utils.getVideoModeColumn(false, BaseModule.this.getShotMode()), null);
            }
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$33 */
    class C014933 implements OnShutterButtonListener {
        C014933() {
        }

        public void onShutterButtonLongClick() {
            BaseModule.this.onShutterTopButtonLongClickListener();
        }

        public void onShutterButtonFocus(boolean pressed) {
            BaseModule.this.onShutterTopButtonFocus(pressed);
        }

        public void onShutterButtonClick() {
            BaseModule.this.onShutterTopButtonClickListener();
        }

        public Handler getHandler() {
            return BaseModule.this.mHandler;
        }

        public void onShutterTouchUp() {
            BaseModule.this.onShutterTopTouchUp();
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$34 */
    class C015034 implements OnShutterButtonListener {
        C015034() {
        }

        public void onShutterButtonLongClick() {
            if (!SystemBarUtil.isSystemUIVisible(BaseModule.this.getActivity())) {
                LdbUtil.setShutterType("Button");
                BaseModule.this.onShutterBottomButtonLongClickListener();
            }
        }

        public void onShutterButtonFocus(boolean pressed) {
            BaseModule.this.onShutterBottomButtonFocus(pressed);
        }

        public void onShutterButtonClick() {
            LdbUtil.setShutterType("Button");
            BaseModule.this.onShutterBottomButtonClickListener();
        }

        public Handler getHandler() {
            return BaseModule.this.mHandler;
        }

        public void onShutterTouchUp() {
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$35 */
    class C015135 implements OnClickListener {
        C015135() {
        }

        public void onClick(View v) {
            LdbUtil.setShutterType("Button");
            BaseModule.this.onShutterLargeButtonClicked();
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$36 */
    class C015236 implements OnLongClickListener {
        C015236() {
        }

        public boolean onLongClick(View v) {
            LdbUtil.setShutterType("Button");
            return BaseModule.this.onShutterLargeButtonLongClicked();
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$37 */
    class C015337 implements OnShutterZoomListener {
        C015337() {
        }

        public boolean moveShutter(int moveX) {
            return BaseModule.this.moveShutterZoom(moveX);
        }

        public void stopShutter() {
            BaseModule.this.stopShutterZoom();
        }

        public void onTouchUp() {
            if (BaseModule.this.mZoomManager != null) {
                BaseModule.this.mZoomManager.stopSkipShutterZoom();
            }
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$38 */
    class C015438 implements OnClickListener {
        C015438() {
        }

        public void onClick(View v) {
            BaseModule.this.onExtraButtonClicked(1);
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$39 */
    class C015539 implements OnClickListener {
        C015539() {
        }

        public void onClick(View v) {
            BaseModule.this.onExtraButtonClicked(2);
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$40 */
    class C015840 implements OnClickListener {

        /* renamed from: com.lge.camera.app.BaseModule$40$1 */
        class C01591 extends Thread {
            C01591() {
            }

            public void run() {
                try {
                    new Instrumentation().sendKeyDownUpSync(82);
                } catch (Exception e) {
                    CamLog.m6e(CameraConstants.TAG, "Fail to make menu key event", e);
                }
            }
        }

        C015840() {
        }

        public void onClick(View v) {
            new C01591().start();
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$41 */
    class C016041 implements OnFocusChangeListener {
        C016041() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
            ImageView menuBtnBg = (ImageView) BaseModule.this.mGet.findViewById(C0088R.id.menu_button_bg);
            if (menuBtnBg != null) {
                if (hasFocus) {
                    menuBtnBg.setVisibility(0);
                    return;
                }
                menuBtnBg.setVisibility(8);
                BaseModule.this.mGet.findViewById(C0088R.id.menu_button).setFocusable(false);
            }
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$47 */
    class C016647 extends ChildSettingRunnable {
        C016647() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            runChildSettingMenu(prefObject, key, value, 0);
        }

        public void runChildSettingMenu(Object prefObject, String key, String value, int clickedType) {
            if (FunctionProperties.isSignatureSupported(BaseModule.this.getAppContext())) {
                CamLog.m3d(CameraConstants.TAG, "mChildSettingUpdater_signature, clickedType = " + clickedType);
                if (clickedType == 1) {
                    if (BaseModule.this.mGet.isNeedToStartSignatureActivity(value)) {
                        BaseModule.this.mGet.startSignatureActivity();
                    }
                    BaseModule.this.mGet.setSetting(Setting.KEY_SIGNATURE, value, true);
                } else if (clickedType == 0) {
                    BaseModule.this.mGet.startSignatureActivity();
                }
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$48 */
    class C016748 implements onQuickClipListListener {
        C016748() {
        }

        public void onListOpend() {
            BaseModule.this.showCommandArearUI(false);
            BaseModule.this.setVisibleGuideTextforQuickClip();
            if (BaseModule.this.isRearCamera() || BaseModule.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                BaseModule.this.setFilmStrengthButtonVisibility(false, false);
                if (!(CameraConstants.MODE_SLOW_MOTION.equals(BaseModule.this.getShotMode()) && BaseModule.this.isRecordingState())) {
                    BaseModule.this.hideFocusOnShowOtherBars(false);
                }
            }
            if (BaseModule.this.isActivatedQuickdetailView()) {
                BaseModule.this.setDeleteButtonVisibility(false);
            }
        }

        public void onListClosed() {
            if (!BaseModule.this.isTimerShotCountdown()) {
                BaseModule.this.showCommandArearUI(true);
                BaseModule.this.setVisibleGuideTextforQuickClip();
                if (BaseModule.this.isRearCamera() || BaseModule.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    BaseModule.this.setFilmStrengthButtonVisibility(true, false);
                    BaseModule.this.showFocusOnHideOtherBars();
                }
                if (BaseModule.this.isActivatedQuickdetailView()) {
                    BaseModule.this.setDeleteButtonVisibility(true);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.BaseModule$9 */
    class C01749 extends ChildSettingRunnable {
        C01749() {
        }

        public void runChildSettingMenu(Object prefObject, String key, String value) {
            CamLog.m3d(CameraConstants.TAG, "-picsize- mChildSettingUpdater_pictureSize");
            ListPreference listPref = prefObject instanceof ListPreference ? (ListPreference) prefObject : null;
            if (listPref != null && BaseModule.this.mCameraDevice != null && BaseModule.this.checkModuleValidate(208)) {
                BaseModule.this.changeFullVisionSettingOnPictureSizeChanged(listPref, value);
                BaseModule.this.setSetting(key, value, true);
                String previewSizePref = BaseModule.this.getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), null);
                CameraParameters parameters = BaseModule.this.mCameraDevice.getParameters();
                if (parameters.get(BaseModule.this.getPictureSizeParamKey(0)).equals(listPref.getValue())) {
                    CamLog.m3d(CameraConstants.TAG, "pictureSize is already same. return!");
                    return;
                }
                if (BaseModule.this.mLightFrameManager.isLightFrameMode()) {
                    BaseModule.this.mGet.getHybridView().setSurfaceViewTransparent(true);
                } else {
                    BaseModule.this.mGet.setPreviewCoverVisibility(0, true);
                }
                BaseModule.this.showFrameGridView("off", false);
                BaseModule.this.mPictureOrVideoSizeChanged = true;
                if (BaseModule.this.mFocusManager != null && (BaseModule.this.isFocusLock() || BaseModule.this.isAELock())) {
                    BaseModule.this.mFocusManager.resetAEAFFocus();
                }
                if (BaseModule.this.mFocusManager != null) {
                    BaseModule.this.mFocusManager.hideFocusForce();
                    BaseModule.this.setManualFocusButtonVisibility(false);
                    BaseModule.this.setManualFocusModeEx(false);
                }
                if (BaseModule.this.isNeedRestartByPictureSizeChanged()) {
                    if (BaseModule.this.mFocusManager != null) {
                        BaseModule.this.mFocusManager.stopFaceDetection();
                    }
                    BaseModule.this.mGet.setPreviewCoverVisibility(0, true);
                    BaseModule.this.stopPreview();
                } else {
                    String previewSize = parameters.get(ParamConstants.KEY_PREVIEW_SIZE);
                    if (!(previewSize == null || previewSize.compareTo(previewSizePref) == 0)) {
                        BaseModule.this.setPreviewCallbackAll(false);
                    }
                }
                BaseModule.this.updateSecondCameraSettings(key, value, true);
                BaseModule.this.setParamUpdater(parameters, BaseModule.this.getPictureSizeParamKey(0), listPref.getValue());
                if (BaseModule.this.isBinningSupportedMode() && BaseModule.this.mBinningManager != null && BaseModule.this.mBinningManager.checkChangeBinningSize()) {
                    BaseModule.this.setParamUpdater(parameters, BaseModule.this.getPictureSizeParamKey(0), BaseModule.this.mBinningManager.getBinningPictureSize(BaseModule.this.mCameraId));
                }
                if (BaseModule.this.isOpticZoomSupported(null)) {
                    BaseModule.this.setParamUpdater(parameters, BaseModule.this.getPictureSizeParamKey(1), BaseModule.this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(BaseModule.this.getShotMode(), BaseModule.this.mCameraId == 0 ? 2 : 0)).getValue());
                }
                if (!key.equals(SettingKeyWrapper.getVideoSizeKey(BaseModule.this.getShotMode(), BaseModule.this.mCameraId))) {
                    BaseModule.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_SIZE, previewSizePref);
                    if (FunctionProperties.getSupportedHal() == 2) {
                        int[] size = Utils.sizeStringToArray(previewSizePref);
                        BaseModule.this.mGet.setCameraPreviewSize(size[0], size[1]);
                    }
                }
                parameters.set(ParamConstants.KEY_HFR, "off");
                BaseModule.this.setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
                BaseModule.this.setTilePreviewSetting(listPref.getExtraInfo(1));
                BaseModule.this.setParameters(parameters);
                BaseModule.this.mHandler.sendEmptyMessage(61);
                BaseModule.this.mGet.setSwitchingAniViewParam(false);
            }
        }

        public boolean checkChildAvailable() {
            if (BaseModule.this.isSettingMenuItemAvailable()) {
                return true;
            }
            return false;
        }
    }

    public abstract void disableSettingValueInMdm();

    public abstract void doBLEOneKeyAction(boolean z);

    public abstract void onHeadsetPlugged(int i);

    public abstract void onHeadsetUnPlugged();

    public abstract void restoreSettingValueInMdm();

    protected abstract void saveImage(byte[] bArr, byte[] bArr2);

    protected abstract void setDefaultRecordingParameters(CameraParameters cameraParameters, String str, boolean z);

    protected abstract void setDefaultRecordingParameters(CameraParameters cameraParameters, String str, boolean z, boolean z2);

    protected abstract void startAudioZoom(CameraParameters cameraParameters);

    protected abstract boolean takePicture();

    public BaseModule(ActivityBridge activityBridge) {
        super(activityBridge);
        createMediaSaveService();
    }

    private boolean canSetFrontFlashBar(boolean isOn, boolean isAuto) {
        if (this.mFocusManager == null || this.mFocusManager == null || this.mFlashControlManager == null || this.mAdvancedFilmManager == null || this.mCameraCapabilities == null || isRearCamera() || !this.mCameraCapabilities.isFlashSupported()) {
            return false;
        }
        if (isOn) {
            if (isMenuShowing(CameraConstants.MENU_TYPE_ALL) || this.mGet.isOpeningSettingMenu() || isGestureGuideShowing()) {
                return false;
            }
            if ((this.mFocusManager.isAeControlBarShowing() || this.mAdvancedFilmManager.isSelfieOptionVisible()) && isAuto) {
                return false;
            }
        }
        return true;
    }

    protected void setFlashLevelControlMenu(boolean isOn) {
        if (canSetFrontFlashBar(isOn, false)) {
            if (isOn) {
                if (isBarVisible(1)) {
                    this.mAdvancedFilmManager.onClickSkinToneButton();
                }
                if (isFocusLock() || isAELock()) {
                    this.mFocusManager.resetAEAFFocus();
                } else {
                    this.mFocusManager.cancelTouchAutoFocus();
                }
            }
            super.setFlashLevelControlMenu(isOn);
        }
    }

    protected void setFlashLevelControlMenuAutoOn(boolean on) {
        if (canSetFrontFlashBar(on, true)) {
            if (ParamConstants.FLASH_MODE_TORCH.equals(getParamValue("flash-mode"))) {
                this.mFlashControlManager.showAndHideFlashControlBar(on);
            }
        }
    }

    private void sendChangeViewMsg(int mode) {
        hideMenu(CameraConstants.MENU_TYPE_ALL, false, true);
        this.mHandler.removeMessages(70);
        Message msg = new Message();
        msg.what = 70;
        msg.arg1 = mode;
        this.mHandler.sendMessage(msg);
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            setMenuButtonListener();
        }
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.setOnConeViewModeButtonListener(new C01341());
        }
    }

    public void initUIDone() {
        super.initUIDone();
        if (this.mQuickButtonManager != null && this.mQuickButtonManager.isButtonListEmpty()) {
            setQuickButtonByPreset(true, true);
        }
        if (!(this.mIsPreviewCallbackWaiting || getFilmState() == 3)) {
            setListenerAfterOneShotCallback();
        }
        this.mGet.refreshUspZone(true);
    }

    public void startedCameraByInAndOutZoom() {
        if (this.mFocusManager != null) {
            this.mFocusManager.initializeFocusManagerByForce();
        }
    }

    public boolean isShutterZoomSupported() {
        return true;
    }

    public List<String> getSupportedColorEffects() {
        if (this.mCameraCapabilities == null) {
            return null;
        }
        return this.mCameraCapabilities.getSupportedColorEffects();
    }

    public boolean isColorEffectSupported() {
        if (ModelProperties.getAppTier() <= 2 && getSupportedColorEffects() != null) {
            return true;
        }
        return false;
    }

    public boolean setColorEffect(String value) {
        if (this.mCameraDevice == null) {
            return false;
        }
        CameraParameters params = this.mCameraDevice.getParameters();
        if (params == null) {
            return false;
        }
        String curEffect = params.getColorEffect();
        if (value == null || curEffect == null) {
            return false;
        }
        if (!value.equals(curEffect)) {
            params.set(ParamConstants.KEY_EFFECT, value);
            this.mCameraDevice.setParameters(params);
            this.mParamUpdater.setParameters(params, ParamConstants.KEY_EFFECT, value);
            CamLog.m3d(CameraConstants.TAG, "[color] set color effect : " + value);
        }
        setSetting(Setting.KEY_COLOR_EFFECT, value, true);
        return true;
    }

    public void updateColorEffectQuickButton() {
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.updateButtonIcon(C0088R.id.quick_button_color_effect, this.mQuickButtonManager.getFilmQuickButtonSelector(), this.mQuickButtonManager.getFilmQuickButtonPressedImage());
            this.mQuickButtonManager.updateButton(C0088R.id.quick_button_color_effect);
            this.mQuickButtonManager.refreshButtonEnable(100, true, true);
        }
    }

    public void childSettingMenuClicked(String key, String value) {
        this.mGet.childSettingMenuClicked(key, value);
    }

    public boolean setBarSetting(String key, String value, boolean save) {
        setSetting(key, value, save);
        return false;
    }

    public void setZoomStep(int step, boolean actionEnd, boolean useTimer, boolean forJogZoom) {
        if (this.mZoomManager != null && checkModuleValidate(15) && this.mCameraDevice != null) {
            if (this.mLocalParamForZoom == null) {
                this.mLocalParamForZoom = this.mCameraDevice.getParameters();
            }
            if (forJogZoom) {
                if (FunctionProperties.getSupportedHal() == 2) {
                    this.mLocalParamForZoom = this.mCameraDevice.getParameters();
                }
                if (this.mLocalParamForZoom != null) {
                    this.mParamUpdater.setParameters(this.mLocalParamForZoom, ParamConstants.KEY_JOG_ZOOM, String.valueOf(step));
                    this.mCameraDevice.setParameters(this.mLocalParamForZoom);
                    return;
                }
                return;
            }
            updateZoomParam(this.mLocalParamForZoom, step);
        }
    }

    protected boolean isRecordingInAndOutZoomPlanA() {
        return this.mRecordingInAndOutZoomPlan == 0;
    }

    protected void setParameterForSecondCamera(CameraParameters param, int cameraId, boolean switchingByButton) {
        String zoomValue = "0";
        String switchingValue = "0";
        String videoHdr = "off";
        if (!switchingByButton && cameraId == 2) {
            zoomValue = Integer.toString(this.mZoomManager.getMaxZoomForWideCamera());
        }
        if (cameraId == 0) {
            switchingValue = "3";
            if ("1".equals(this.mGet.getCurSettingValue("hdr-mode"))) {
                videoHdr = "on";
            }
        } else {
            switchingValue = "2";
        }
        if (param != null) {
            param.setZoom(Integer.parseInt(zoomValue));
            param.set(ParamConstants.KEY_VIDEO_HDR_MODE, videoHdr);
            param.set("hdr-mode", "0");
            if (!FunctionProperties.isSupportedOpticZoom()) {
                param.set(ParamConstants.KEY_CAMERA_SWITCHING, switchingValue);
            }
            String liveSnapShotSize = getLiveSnapShotSize(param, getSettingValue(getVideoSizeSettingKey()));
            if (liveSnapShotSize != null) {
                param.set("picture-size", liveSnapShotSize);
            }
            param.set("focus-mode", CameraDeviceUtils.isAFSupported(param) ? ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO : ParamConstants.FOCUS_MODE_FIXED);
        }
    }

    public void doInAndOutZoom(boolean switchingByButton, boolean switchingByBar) {
        this.mLocalParamForZoom = null;
        CamLog.m7i(CameraConstants.TAG, "doInAndOutZoom button? " + switchingByButton + " bar ? " + switchingByBar);
        if (checkInAndOutZoomAvailable()) {
            int cameraIdForSwitch;
            if (this.mCameraId == 0) {
                cameraIdForSwitch = 2;
            } else {
                cameraIdForSwitch = 0;
            }
            if (!switchingByButton) {
                AudioUtil.performHapticFeedback(this.mZoomManager.getZoomBarView(), 65631);
            }
            if (isOpticZoomSupported(null)) {
                doOpticZoom(switchingByButton, switchingByBar, cameraIdForSwitch);
            } else if (checkModuleValidate(192)) {
                this.mGet.setCameraChanging(4);
                onInAndZoomStart();
                if (this.mFocusManager != null) {
                    this.mFocusManager.hideAndCancelAllFocus(false);
                    this.mFocusManager.resetEVValue(0);
                }
                stopPreview();
                closeCamera();
                SharedPreferenceUtilBase.setCameraId(getAppContext(), cameraIdForSwitch);
                SharedPreferenceUtil.saveRearCameraId(getAppContext(), cameraIdForSwitch);
                this.mGet.setupSetting();
                this.mGet.refreshSettingByCameraId();
                startCameraDevice(1);
                initializeSettingMenus();
                this.mGet.removeCameraChanging(4);
            } else {
                inAndOutZoomOnRecording(cameraIdForSwitch, switchingByButton);
            }
            sendLDBIntentForInAndOut(cameraIdForSwitch, switchingByButton, switchingByBar);
            if (this.mDoubleCameraManager != null) {
                this.mDoubleCameraManager.setForceButtonsSelected(cameraIdForSwitch);
                if (switchingByButton && checkModuleValidate(128)) {
                    this.mDoubleCameraManager.setDualViewControlEnabled(false);
                }
            }
        } else if (this.mZoomManager != null) {
            this.mZoomManager.notifySwitchingFinished();
        }
    }

    protected void doOpticZoom(boolean switchingByButton, boolean switchingByBar, int cameraIdForSwitch) {
        final CameraParameters parameters = this.mCameraDevice.getParameters();
        if (switchingByButton) {
            if (this.mFocusManager != null) {
                if (this.mFocusManager.isAEControlBarEnableCondition()) {
                    parameters.setExposureCompensation(0);
                }
                this.mFocusManager.hideAndCancelAllFocus(false);
            }
            this.mGet.setCameraChanging(4);
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_OPTIC_CAM_INIT, cameraIdForSwitch == 2 ? ParamConstants.VALUE_OPITC_CAM_INIT_WIDE : ParamConstants.VALUE_OPITC_CAM_INIT_TELE);
        }
        if (this.mGet.isAnimationShowing() && switchingByButton) {
            this.mInAndOutAnimationStartTime = SystemClock.uptimeMillis();
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (!BaseModule.this.mGet.checkCameraChanging(4)) {
                        CamLog.m7i(CameraConstants.TAG, "Expiered switch Animation timer");
                        BaseModule.this.doneOpticZoom(false);
                    }
                }
            }, 300);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    BaseModule.this.mGet.runOnUiThread(new HandlerRunnable(BaseModule.this) {
                        public void handleRun() {
                            if (!BaseModule.this.mGet.isPaused()) {
                                BaseModule.this.updateZoomParam(parameters, BaseModule.this.mZoomManager.getZoomValue());
                            }
                        }
                    });
                }
            }).start();
            return;
        }
        updateZoomParam(parameters, this.mZoomManager.getZoomValue());
    }

    public void detectCameraChanged(int cameraId) {
        if (isOpticZoomSupported(null) && this.mZoomManager != null && this.mCameraDevice != null) {
            boolean switchingByBar = false;
            InAndOutZoomManager manager = this.mZoomManager;
            CamLog.m7i(CameraConstants.TAG, "Changed Camera by optic zoom : mCameraId =  " + this.mCameraId + " notify Id = " + cameraId);
            this.mCameraId = cameraId;
            SharedPreferenceUtilBase.setCameraId(getAppContext(), this.mCameraId);
            SharedPreferenceUtil.saveRearCameraId(getAppContext(), this.mCameraId);
            if (this.mGet.checkCameraChanging(4)) {
                this.mGet.removeCameraChanging(4);
                manager.notifySwitchingFinished();
            } else {
                switchingByBar = true;
            }
            if (!isPaused()) {
                boolean isRecordingState;
                if (checkModuleValidate(192)) {
                    isRecordingState = false;
                } else {
                    isRecordingState = true;
                }
                if (!isZoomBarVisible()) {
                    refreshSettingByCameraId();
                }
                if (this.mFocusManager != null && this.mFocusManager.getFocusState() == 14) {
                    this.mFocusManager.hideTrackingAF();
                    setFocusState(0);
                }
                CameraParameters parameters = (checkModuleValidate(128) || this.mLocalParamForZoom == null) ? this.mCameraDevice.getParameters() : this.mLocalParamForZoom;
                updateParamForChangedCamera(parameters);
                if (this.mDoubleCameraManager != null && switchingByBar) {
                    this.mDoubleCameraManager.setButtonsSelected();
                }
                if (this.mQuickButtonManager != null && isRecordingState) {
                    this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 4, false);
                }
                if (!(isZoomBarVisible() || isJogZoomMoving() || SystemClock.uptimeMillis() - this.mInAndOutAnimationStartTime <= 300)) {
                    doneOpticZoom(switchingByBar);
                }
                CamLog.m3d(CameraConstants.TAG, "OpticZoom [END]");
            }
        }
    }

    private void refreshSettingByCameraId() {
        if (checkModuleValidate(192)) {
            this.mGet.refreshSettingByCameraId();
            initializeSettingMenus();
            return;
        }
        restoreTrackingAFSetting();
    }

    private void updateParamForChangedCamera(CameraParameters parameters) {
        if (parameters != null) {
            updateCapabilities(parameters);
            releaseParamUpdater();
            addModuleRequester();
            activateFingerDetection(this.mCameraId == 2, parameters, false);
            if (!checkModuleValidate(192) || this.mCaptureButtonManager.isWorkingShutterAnimation()) {
                ListPreference listPref = getListPreference(getVideoSizeSettingKey());
                if (listPref != null) {
                    String videoSize = listPref.getValue();
                    if (videoSize != null) {
                        String keepFlashMode = parameters.get("flash-mode");
                        setDefaultRecordingParameters(parameters, videoSize, true, true);
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue()), false, true);
                        this.mParamUpdater.setParameters(parameters, "picture-size", this.mParamUpdater.getParamValue("picture-size"));
                        if (!FunctionProperties.isSameResolutionOpticZoom()) {
                            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PICTURE_SIZE_WIDE, this.mParamUpdater.getParamValue(ParamConstants.KEY_PICTURE_SIZE_WIDE));
                        }
                        CamLog.m3d(CameraConstants.TAG, "[opticzoom] KEY_PICTURE_SIZE_WIDE param update!");
                        if (this.mGet.isOpticZoomSupported(null)) {
                            if (this.mCameraId == 0) {
                                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_OPTIC_CAM_INIT, ParamConstants.VALUE_OPITC_CAM_INIT_TELE);
                            } else if (this.mCameraId == 2) {
                                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_OPTIC_CAM_INIT, ParamConstants.VALUE_OPITC_CAM_INIT_WIDE);
                            }
                        }
                        this.mParamUpdater.setParameters(parameters, "flash-mode", keepFlashMode);
                        this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_EXPOSURE_COMPENSATION, this.mParamUpdater.getParamValue(ParamConstants.KEY_EXPOSURE_COMPENSATION));
                    } else {
                        return;
                    }
                }
                return;
            }
            if (this.mLocalParamForZoom != null) {
                CamLog.m3d(CameraConstants.TAG, "[JOG] set jog-zoom step : " + this.mLocalParamForZoom.get(ParamConstants.KEY_JOG_ZOOM));
                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_JOG_ZOOM, this.mLocalParamForZoom.get(ParamConstants.KEY_JOG_ZOOM));
            }
            this.mParamUpdater.updateAllParameters(parameters, false);
            setSpecificModuleParam(parameters);
            if (FunctionProperties.isBinningAllSupported() && this.mBinningManager.isBinningEnabled()) {
                this.mParamUpdater.setParameters(parameters, "picture-size", this.mBinningManager.getBinningPictureSize(this.mCameraId));
                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_BINNING_PARAM, ParamConstants.VALUE_BINNING_MODE);
                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_APP_OUTPUTS_TYPE, this.mBinningManager.getBinningCaptureOutputConfig());
                this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_APP_BINNING_TYPE, getBinningType());
            }
            this.mLocalParamForZoom = parameters;
            this.mCameraDevice.setParameters(parameters);
        }
    }

    public void syncOpticZoomCameraId(boolean switchingByBar) {
        if (isOpticZoomSupported(null) && this.mBeforeZoomCameraId != this.mCameraId) {
            CamLog.m7i(CameraConstants.TAG, "Update UI ");
            this.mBeforeZoomCameraId = this.mCameraId;
            refreshSettingByCameraId();
            doneOpticZoom(switchingByBar);
        }
    }

    protected void doneOpticZoom(boolean switchingByBar) {
        boolean isRecordingState;
        boolean z = true;
        CamLog.m7i(CameraConstants.TAG, "doneOpticZoom " + this.mSnapShotChecker.isIdle());
        if (checkModuleValidate(192)) {
            isRecordingState = false;
        } else {
            isRecordingState = true;
        }
        if (!(switchingByBar || isRecordingState)) {
            removePreviewAlphaCover(getShotMode());
            this.mGet.setPreviewVisibility(0);
            if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                this.mAdvancedFilmManager.moveFilmPreviewOutOfWindow(false);
            }
            enableControls(true, true);
            if (!(this.mDoubleCameraManager == null || this.mSnapShotChecker.checkMultiShotState(7) || !this.mSnapShotChecker.isIdle())) {
                CamLog.m7i(CameraConstants.TAG, "doneOpticZoom eanble control");
                this.mDoubleCameraManager.setDualViewControlEnabled(true);
            }
        }
        if (!isZoomBarVisible() && !isJogZoomMoving()) {
            if (this.mFocusManager != null) {
                this.mFocusManager.initializeFocusManagerByForce();
                this.mFocusManager.initAFView();
                if (!isRecordingState) {
                    this.mFocusManager.registerCallback();
                }
            }
            if (this.mCameraId != 2) {
                z = false;
            }
            setFingerDetectionListener(z);
            if (this.mCameraId == 0) {
                setTrackingAFListener();
            }
        }
    }

    public boolean isSwithingCameraDuringTheRecording() {
        return this.mIsSwitchingCameraDuringRecording;
    }

    protected void onInAndZoomStart() {
    }

    protected boolean checkInAndOutZoomAvailable() {
        if (this.mZoomManager == null) {
            CamLog.m3d(CameraConstants.TAG, "Zoom manager's null, return.");
            return false;
        } else if (!FunctionProperties.isSupportedInAndOutZoom()) {
            CamLog.m3d(CameraConstants.TAG, "Seems like in and out zoom's not supported, return.");
            return false;
        } else if (!checkModuleValidate(15)) {
            CamLog.m3d(CameraConstants.TAG, "Maybe camera's releasing, return.");
            return false;
        } else if (!checkModuleValidate(128) && this.mFocusManager != null && this.mFocusManager.getFocusState() == 1) {
            CamLog.m3d(CameraConstants.TAG, "Focusing state, return.");
            this.mFocusManager.hideAndCancelAllFocus(false, false, false);
            return false;
        } else if (this.mIsSwitchingCameraDuringRecording) {
            CamLog.m3d(CameraConstants.TAG, "mIsSwitchingCameraDuringRecording, return.");
            return false;
        } else if (isInAndOutZoomSupprotedModule()) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isInAndOutZoomSupprotedModule() {
        return true;
    }

    protected void sendLDBIntentForInAndOut(int cameraIdForSwitch, boolean switchingByButton, boolean switchingByBar) {
        String ldbKey = !checkModuleValidate(192) ? LdbConstants.LDB_FEATURE_NAME_IN_AND_OUT_RECORDING : LdbConstants.LDB_FEATURE_NAME_IN_AND_OUT;
        String switchingMethod = "Pinch";
        if (switchingByButton) {
            switchingMethod = "Button";
        } else if (switchingByBar) {
            switchingMethod = LdbConstants.LDB_INANDOUT_BAR;
        }
        if (LdbConstants.LDB_FEATURE_NAME_IN_AND_OUT.equals(ldbKey) && switchingByButton) {
            ldbKey = LdbConstants.LDB_FEATURE_NAME_CHANGE_CAMERA;
            switchingMethod = "";
        }
        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), ldbKey, cameraIdForSwitch, switchingMethod);
    }

    protected void inAndOutZoomOnRecording(final int cameraIdForSwitch, final boolean switchingByButton) {
        if (isRecordingInAndOutZoomPlanA()) {
            CamLog.m3d(CameraConstants.TAG, "inAndOutZoomOnRecording [START]");
            this.mIsSwitchingCameraDuringRecording = true;
            if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                this.mCameraId = cameraIdForSwitch;
                SharedPreferenceUtilBase.setCameraId(getAppContext(), cameraIdForSwitch);
                SharedPreferenceUtil.saveRearCameraId(getAppContext(), cameraIdForSwitch);
                this.mGet.setupSetting();
                releaseParamUpdater();
                final String toSetParamValues = getInAndOutZoomRecordingParameter(switchingByButton, cameraIdForSwitch);
                new Thread() {
                    public void run() {
                        CamLog.m3d(CameraConstants.TAG, "switchCamera [START]");
                        if (BaseModule.this.mCameraDevice != null) {
                            BaseModule.this.mCameraDevice.removeCallbacks();
                        }
                        if (VideoRecorder.switchCamera(cameraIdForSwitch, toSetParamValues)) {
                            CamLog.m3d(CameraConstants.TAG, "switchCamera [END]");
                            BaseModule.this.onCameraSwitchedDuringTheRecording(switchingByButton);
                        }
                    }
                }.start();
            } else {
                inAndOutZoomOnFilmRecording(switchingByButton);
            }
            restoreTrackingAFSetting();
            return;
        }
        this.mIsSwitchingCameraDuringRecording = true;
        this.mRecordingStateBeforeChangingCamera = getCameraState();
        stopPreview();
        closeCamera();
        SharedPreferenceUtilBase.setCameraId(getAppContext(), cameraIdForSwitch);
        SharedPreferenceUtil.saveRearCameraId(getAppContext(), cameraIdForSwitch);
        this.mGet.setupSetting();
        startCameraDevice(1);
        initializeSettingMenus();
        updateSettingDuringInAndOutRecording();
    }

    protected void updateSettingDuringInAndOutRecording() {
    }

    private void inAndOutZoomOnFilmRecording(final boolean switchingByButton) {
        int i = 0;
        CamLog.m3d(CameraConstants.TAG, "[Film] close previous camera device");
        if (this.mCameraDevice != null) {
            this.mCameraDevice.stopPreview();
            this.mCameraDevice.release(false);
            openSecondCamera(switchingByButton);
            if (this.mCameraId == 0) {
                i = 2;
            }
            this.mCameraId = i;
            CamLog.m3d(CameraConstants.TAG, "[Film] Camera will be switched to : " + this.mCameraId);
            this.mCameraDevice.switchCameraForInAndOut();
            SharedPreferenceUtilBase.setCameraId(getAppContext(), this.mCameraId);
            SharedPreferenceUtil.saveRearCameraId(getAppContext(), this.mCameraId);
            this.mGet.setupSetting();
            try {
                this.mCameraDevice2.getCamera().setPreviewTexture(this.mAdvancedFilmManager.getSurfaceTextureForInAndOut());
            } catch (IOException e) {
                e.printStackTrace();
            }
            CamLog.m3d(CameraConstants.TAG, "[Film] second camera startPreview");
            this.mCameraDevice2.getCamera().startPreview();
            this.mCameraDevice2.getCamera().setOneShotPreviewCallback(new PreviewCallback() {
                public void onPreviewFrame(byte[] arg0, Camera arg1) {
                    CamLog.m3d(CameraConstants.TAG, "[Film] onPreviewFrame on second camera!");
                    if (BaseModule.this.mAdvancedFilmManager != null) {
                        BaseModule.this.mAdvancedFilmManager.changeCamera();
                    }
                    BaseModule.this.setTrackingAFListener();
                    BaseModule.this.onCameraSwitchedDuringTheRecording(switchingByButton);
                }
            });
        }
    }

    private void openSecondCamera(boolean switchingByButton) {
        int cameraId = this.mCameraId == 0 ? 2 : 0;
        CamLog.m3d(CameraConstants.TAG, "[Film] open new camera device, cameraId : " + cameraId);
        if (FunctionProperties.getSupportedHal() == 1) {
            this.mCameraDevice2 = new LGCamera(cameraId, 256);
        } else {
            this.mCameraDevice2 = new LGCamera(cameraId);
        }
        if (this.mCameraDevice2 != null) {
            CameraParameters parameters = this.mCameraDevice.convertParameter(this.mCameraDevice2.getLGParameters().getParameters());
            updatePictureSizeList(parameters);
            this.mParamUpdater.updateAllParameters(parameters);
            setParameterForSecondCamera(parameters, cameraId, switchingByButton);
            CamLog.m3d(CameraConstants.TAG, "[Film] setParameters for second camera");
            this.mCameraDevice2.getCamera().setParameters((Parameters) parameters.getParameters());
            int displayOrientation = CameraDeviceUtils.getDisplayOrientation(CameraDeviceUtils.getDisplayRotation(this.mGet.getActivity()), this.mCameraId);
            CamLog.m3d(CameraConstants.TAG, "[Film] displayOrientation = " + displayOrientation);
            if (this.mInitialOrientation == -1) {
                this.mInitialOrientation = this.mDisplayOrientation;
                CamLog.m3d(CameraConstants.TAG, "[Film] mInitialOrientation is set to " + this.mInitialOrientation);
            }
            this.mCameraDevice2.getCamera().setDisplayOrientation(displayOrientation);
            this.mCameraDevice.setSecondCamera(this.mCameraDevice2);
        }
    }

    protected String getVideoSizeSettingKey() {
        return SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId);
    }

    protected void onCameraSwitchedDuringTheRecording(boolean switchingByButton) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.refreshCamera();
            final CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                updateCapabilities(parameters);
                addModuleRequester();
                ListPreference listPref = getListPreference(getVideoSizeSettingKey());
                if (listPref != null) {
                    String videoSize = listPref.getValue();
                    if (videoSize != null) {
                        setDefaultRecordingParameters(parameters, videoSize, true);
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue()), false, true);
                        this.mParamUpdater.setParameters(parameters, "picture-size", this.mParamUpdater.getParamValue("picture-size"));
                        this.mCameraDevice.setParameters(parameters);
                        this.mGet.runOnUiThread(new HandlerRunnable(this) {
                            public void handleRun() {
                                if (BaseModule.this.mFocusManager != null) {
                                    BaseModule.this.mFocusManager.initializeFocusManagerByForce();
                                    BaseModule.this.mFocusManager.initAFView();
                                }
                                BaseModule.this.mIsSwitchingCameraDuringRecording = false;
                                if (BaseModule.this.mZoomManager != null) {
                                    int zoomMax = 80;
                                    if (parameters != null) {
                                        zoomMax = parameters.getMaxZoom();
                                    }
                                    BaseModule.this.mZoomManager.setZoomMaxValue(zoomMax);
                                    BaseModule.this.mZoomManager.onBarSwitchedDuringRecording();
                                    BaseModule.this.mZoomManager.notifySwitchingFinished();
                                }
                                if (BaseModule.this.mQuickButtonManager != null) {
                                    BaseModule.this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 4, false);
                                }
                                CamLog.m3d(CameraConstants.TAG, "inAndOutZoomOnRecording [END]");
                            }
                        });
                    }
                }
            }
        }
    }

    public void setFlashTorch(CameraParameters parameters, boolean on, String flashMode, boolean setParam) {
        String currentFlashMode = parameters.getFlashMode();
        super.setFlashTorch(parameters, on, flashMode, setParam);
        if (FunctionProperties.isSupportedInAndOutZoom() && this.mIsSwitchingCameraDuringRecording && this.mCameraCapabilities != null && this.mCameraCapabilities.isFlashSupported()) {
            parameters.setFlashMode(currentFlashMode);
        }
    }

    protected String getInAndOutZoomRecordingParameter(boolean switchingByButton, int cameraIdForSwitch) {
        String zoomValue = "0";
        String switchingValue = "0";
        String videoHdr = "off";
        if (!switchingByButton && cameraIdForSwitch == 2) {
            zoomValue = Integer.toString(this.mZoomManager.getMaxZoomForWideCamera());
        }
        if (cameraIdForSwitch == 0) {
            switchingValue = "3";
            if ("1".equals(this.mGet.getCurSettingValue("hdr-mode")) && !isFHD60()) {
                videoHdr = "on";
            }
        } else {
            switchingValue = "2";
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_CAMERA_SWITCHING, switchingValue);
        this.mParamUpdater.setParamValue("zoom", zoomValue);
        String zoomArgument = "zoom=" + zoomValue + ";";
        String toSetParamValues = zoomArgument + ("key_switching=" + switchingValue + ";");
        if (this.mCameraCapabilities != null && this.mCameraCapabilities.isVideoHDRSupported(cameraIdForSwitch)) {
            toSetParamValues = toSetParamValues + ("video-hdr=" + videoHdr + ";");
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, videoHdr);
        }
        if (!isManualMode() && isFHD60()) {
            toSetParamValues = toSetParamValues + "video-hfr=" + CameraConstants.FPS_60 + ";";
            toSetParamValues = toSetParamValues + "preview-fps-range=" + (Integer.toString(6000) + "," + Integer.toString(6000)) + ";";
        }
        CamLog.m3d(CameraConstants.TAG, "[switchCamera] starts! Parameters = " + toSetParamValues);
        return toSetParamValues;
    }

    protected boolean changeCameraAfterInAndOutZoom() {
        if (!FunctionProperties.isSupportedInAndOutZoom()) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "mCameraIdBeforeInAndOutZoom = " + this.mCameraIdBeforeInAndOutZoom + " / mCameraId = " + this.mCameraId);
        if (this.mCameraIdBeforeInAndOutZoom == this.mCameraId) {
            return false;
        }
        if (isOpticZoomSupported(null)) {
            this.mGet.refreshSettingByCameraId();
            initializeSettingMenus();
            return false;
        }
        this.mCameraReOpenAfterInAndOutZooom = true;
        closeCamera();
        startCameraDevice(1);
        initializeSettingMenus();
        return true;
    }

    public boolean isCameraReOpeningAfterInAndOutRecording() {
        return this.mCameraReOpenAfterInAndOutZooom;
    }

    protected void oneShotPreviewCallbackDone() {
        super.oneShotPreviewCallbackDone();
        if (FunctionProperties.isSupportedInAndOutZoom()) {
            if (!FunctionProperties.isSupportedOpticZoom()) {
                if (this.mZoomManager != null) {
                    this.mZoomManager.notifySwitchingFinished();
                }
                this.mParamUpdater.setParamValue(ParamConstants.KEY_CAMERA_SWITCHING, "0");
            }
            this.mCameraReOpenAfterInAndOutZooom = false;
        }
        setListenerAfterOneShotCallback();
        if (this.mInitGuideManager != null) {
            this.mInitGuideManager.oneShotPreviewCallbackDone();
        }
        if (FunctionProperties.isSupportedSmartCam(getAppContext()) && !SmartcamUtil.isSmartcamBindService() && !isAttachIntent()) {
            SmartcamUtil.smartcamBindService(getAppContext());
        }
    }

    protected void oneShotPreviewCallbackDoneAfter() {
        super.oneShotPreviewCallbackDoneAfter();
        if (this.mGet.isVoiceAssistantSpecified() && CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equals(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null)) && ("manual".equals(getSettingValue(Setting.KEY_BINNING)) || "off".equals(getSettingValue(Setting.KEY_BINNING)))) {
            this.mChildSettingUpdater_sw_pixel_binning.runChildSettingMenu(getListPreference(Setting.KEY_BINNING), Setting.KEY_BINNING, "on");
        }
        setNightVisionDataCallback(FunctionProperties.isSupportedBinning(this.mCameraId));
        if (this.mGet.isVoiceAssistantSpecified()) {
            handleVoiceAssitantIntent();
        }
    }

    protected void handleVoiceAssitantIntent() {
        if (!checkAssistantCondition()) {
            this.mGet.clearAllAssistantFlag();
        } else if (this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_LAUNCH_GALLERY, false) && this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.onReviewThumbnailClick();
        } else if (onAssistantCommandBefore(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null)) && !this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, true)) {
            boolean returnValue;
            int timerDuration = this.mGet.getAssistantIntFlag(CameraConstantsEx.FLAG_TIMER_DURATION_SECONDS, -1);
            if (timerDuration > 0) {
                returnValue = doVoiceAssistantTimerCommand(timerDuration);
                CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- doTimerCommand, returnValue = " + returnValue);
            } else {
                returnValue = doVoiceAssistantTakeCommand();
                CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- doTakeCommand, returnValue = " + returnValue);
            }
            if (returnValue) {
                this.mGet.clearAllAssistantFlag();
            }
        }
    }

    protected boolean checkAssistantCondition() {
        if (!this.mGet.isVoiceAssistantSpecified() || isTimerShotCountdown() || isMenuShowing(CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE) || isRotateDialogVisible() || !checkModuleValidate(16) || isRecordingState()) {
            return false;
        }
        return true;
    }

    protected boolean onAssistantCommandBefore(String assistantMode) {
        String shotMode = getShotMode();
        if (CameraConstantsEx.FLAG_VALUE_MODE_SMARTCAM.equals(assistantMode) && FunctionProperties.isSupportedSmartCam(this.mGet.getAppContext()) && !"on".equals(this.mGet.getCurSettingValue(Setting.KEY_SMART_CAM_FILTER))) {
            this.mGet.updateUspLayout(CameraConstantsEx.USP_SMART_CAM, true);
            this.mGet.setAssistantFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
        }
        if (CameraConstantsEx.FLAG_VALUE_MODE_CINEMA.equals(assistantMode) && !FunctionProperties.isCineVideoAvailable(this.mGet.getAppContext())) {
            CamLog.m3d(CameraConstants.TAG, "maximum battery saver on! change to Auto mode");
            showToast(this.mGet.getAppContext().getString(C0088R.string.auto_mode_recording_on_max_power_saving_toast), CameraConstants.TOAST_LENGTH_SHORT);
        }
        if (CameraConstantsEx.FLAG_VALUE_MODE_OUTFOCUS.equals(assistantMode) && shotMode != null && shotMode.contains(ParamConstants.KEY_OUTFOCUS)) {
            this.mGet.updateUspLayout(CameraConstantsEx.USP_OUTFOCUS, true);
            this.mGet.setAssistantFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null);
        }
        if (CameraConstantsEx.FLAG_VALUE_MODE_STICKER.equals(assistantMode) && FunctionProperties.isSupportedSticker() && this.mStickerManager != null) {
            this.mStickerManager.startStickerApply(getGLViewMargin());
        }
        if (CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equals(assistantMode) && FunctionProperties.isSupportedBinning(0)) {
            postOnUiThread(this.mAssistantNightVisionRunnable, 1000);
            return false;
        }
        if (this.mGet.isVideoCameraIntent() && !this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, true) && this.mGet.getPreviewCoverVisibility() == 0) {
            this.mGet.setPreviewCoverVisibility(8, true);
        }
        return true;
    }

    protected boolean doVoiceAssistantTimerCommand(int timerDuration) {
        if (this.mGet.isAssistantImageIntent()) {
            return doStartTimerShot(new TimerType(String.valueOf(timerDuration)));
        }
        if (this.mGet.isAssistantVideoIntent()) {
            return onRecordStartButtonClicked();
        }
        return false;
    }

    protected boolean doVoiceAssistantTakeCommand() {
        if (this.mGet.isAssistantImageIntent()) {
            return doStartTimerShot(new TimerType(String.valueOf(2)));
        }
        if (this.mGet.isAssistantVideoIntent()) {
            return onRecordStartButtonClicked();
        }
        return false;
    }

    protected boolean doStartTimerShot(TimerType timerType) {
        return true;
    }

    protected void onOneShotPreviewFrameDirect() {
        if (isActivatedQuickdetailView() && this.isNeedHideDetailView) {
            this.mGet.closeDetailView();
            enableControls(true, !this.mGet.isCameraChanging());
        }
        this.isNeedHideDetailView = false;
        if (!FunctionProperties.isSupportedInAndOutZoom() || isRecordingInAndOutZoomPlanA() || checkModuleValidate(192)) {
            CamLog.m3d(CameraConstants.TAG, "mCameraState = " + this.mCameraState);
        } else if (this.mIsSwitchingCameraDuringRecording) {
            VideoRecorder.setCameraSwitch(this.mCameraDevice);
            this.mIsSwitchingCameraDuringRecording = false;
            if (this.mQuickButtonManager != null) {
                this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 4, false);
            }
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot()) {
            return false;
        }
        if (!isTimerShotCountdown()) {
            doShowDoubleCamera();
        }
        if (this.mIndicatorManager == null) {
            return true;
        }
        setBatteryIndicatorVisibility(true);
        setTimerIndicatorVisibility(true);
        return true;
    }

    public void setFilmStrengthButtonVisibility(boolean visible, boolean needShowBar) {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setFilmStrengthButtonVisibility(visible, needShowBar);
        }
    }

    public void showDoubleCamera(boolean show) {
        if (this.mDoubleCameraManager != null) {
            this.mDoubleCameraManager.showDualViewControl(show);
        }
    }

    public void setDoubleCameraEnable(boolean enable) {
        if (this.mDoubleCameraManager != null) {
            this.mDoubleCameraManager.setDualViewControlEnabled(enable);
        }
    }

    public void hideZoomBar() {
        this.mGet.removePostRunnable(this.mHideZoomBar);
        if (this.mZoomManager != null) {
            this.mZoomManager.setZoomBarVisibility(8);
        }
        this.mLocalParamForZoom = null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isColorEffectMenuVisible()) {
            if (event.getAction() != 1 || this.mHandler == null) {
                return true;
            }
            this.mHandler.sendEmptyMessage(101);
            return true;
        } else if (isSnapShotProcessing() || this.mStickerManager == null || !this.mStickerManager.isStickerGridVisible() || !this.mStickerManager.onTouchEvent(event)) {
            boolean touchResult = super.onTouchEvent(event);
            if (!touchResult) {
                if (this.mBarManager != null) {
                    touchResult = this.mBarManager.onTouchEvent(event);
                } else {
                    touchResult = false;
                }
            }
            if (touchResult) {
                return touchResult;
            }
            if (this.mZoomManager != null) {
                return this.mZoomManager.onTouchEvent(event);
            }
            return false;
        } else if (event.getAction() == 2) {
            CamLog.m3d(CameraConstants.TAG, "Action move skip during sticker drawing");
            return true;
        } else if (this.mStickerManager.isStickerDrawing()) {
            hideStickerMenu(false);
            return true;
        } else if (this.mStickerManager.isWaitOneShot()) {
            CamLog.m3d(CameraConstants.TAG, "skip during oneshot wait");
            return true;
        } else {
            hideStickerMenu(true);
            return true;
        }
    }

    public void resetBarDisappearTimer(int barType, int duration) {
        if (barType == 2) {
            this.mGet.removePostRunnable(this.mHideZoomBar);
            this.mGet.postOnUiThread(this.mHideZoomBar, (long) duration);
        }
    }

    protected void addModuleManager() {
        if (this.mManagerList != null) {
            this.mManagerList.add(this.mCaptureButtonManager);
            this.mManagerList.add(this.mQuickButtonManager);
            this.mManagerList.add(this.mFlashControlManager);
            this.mManagerList.add(this.mBarManager);
            if (isSupportedFingerDetection()) {
                this.mManagerList.add(this.mFingerDetectionManager);
            }
            this.mManagerList.add(this.mGifManager);
            addManagerConditionally();
        }
    }

    private void addManagerConditionally() {
        addDoubleCameraManager();
        addZoomManager();
        addBinningManager();
    }

    private void addBinningManager() {
        if (isRearCamera() && FunctionProperties.isSupportedBinning(0)) {
            this.mManagerList.add(this.mBinningManager);
        }
    }

    private void addZoomManager() {
        if (FunctionProperties.isSupportedInAndOutZoom() && checkDoubleCameraAvailableMode(false)) {
            if (this.mZoomManager != null && (this.mZoomManager instanceof ZoomManager)) {
                CamLog.m3d(CameraConstants.TAG, "change to InAndOutZoom");
                this.mZoomManager.onDestroy();
                this.mZoomManager = new InAndOutZoomManager(this);
            }
        } else if (this.mZoomManager != null && (this.mZoomManager instanceof InAndOutZoomManager)) {
            CamLog.m3d(CameraConstants.TAG, "change to singleZoom");
            this.mZoomManager.onDestroy();
            this.mZoomManager = new ZoomManager(this);
        }
        this.mManagerList.add(this.mZoomManager);
    }

    private void addDoubleCameraManager() {
        if (checkDoubleCameraAvailableMode(true)) {
            this.mManagerList.add(this.mDoubleCameraManager);
        }
    }

    protected boolean checkDoubleCameraAvailableMode(boolean checkDoubleCameraManager) {
        String currentShotMode = getShotMode();
        if (!"mode_normal".equals(currentShotMode) && !CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(currentShotMode) && !currentShotMode.contains(CameraConstants.MODE_PANORAMA) && !CameraConstants.MODE_SLOW_MOTION.equals(currentShotMode) && !"mode_food".equals(currentShotMode) && !currentShotMode.contains(CameraConstants.MODE_BEAUTY) && !CameraConstants.MODE_FRONT_OUTFOCUS.equals(currentShotMode) && !CameraConstants.MODE_SQUARE_SNAPSHOT.equals(currentShotMode) && !CameraConstants.MODE_SQUARE_GRID.equals(currentShotMode) && !CameraConstants.MODE_SQUARE_OVERLAP.equals(currentShotMode) && !isManualMode() && !CameraConstants.MODE_SMART_CAM.equals(currentShotMode) && !CameraConstants.MODE_SMART_CAM_FRONT.equals(currentShotMode) && !CameraConstants.MODE_FLASH_JUMPCUT.equals(currentShotMode) && (!checkDoubleCameraManager || !CameraConstants.MODE_SNAP.equals(getShotMode()) || AppControlUtil.getSnapMovieMVFrameType() != 0)) {
            return false;
        }
        if ((isRecordingSingleZoom() || isSlowMotionMode()) && (!checkModuleValidate(128) || this.mCameraState == 5)) {
            return false;
        }
        if (currentShotMode.contains(CameraConstants.MODE_SQUARE) && (FunctionProperties.getCameraTypeFront() != 0 || FunctionProperties.getCameraTypeRear() != 0)) {
            CamLog.m3d(CameraConstants.TAG, "check both front and rear camera at Square mode.");
            return true;
        } else if (!isRearCamera() && !isSupportedCropAngle() && FunctionProperties.getCameraTypeFront() != 1) {
            return false;
        } else {
            if (!isRearCamera() || FunctionProperties.getCameraTypeRear() == 1) {
                return true;
            }
            return false;
        }
    }

    protected void setManagersListener() {
        super.setManagersListener();
        if (this.mZoomManager != null) {
            this.mZoomManager.setZoomInterface(this);
        }
        if (this.mDoubleCameraManager != null) {
            this.mDoubleCameraManager.setCameraSwitchingInterface(this);
        }
    }

    protected void initializeControls(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "initializeControls: enable = " + enable);
        enableControls(enable, !this.mGet.isModuleChanging());
        if (this.mIntervalChecker != null) {
            this.mIntervalChecker.clearChecker();
            this.mIntervalChecker.addChecker(1, 1000);
            this.mIntervalChecker.addChecker(2, 500);
            this.mIntervalChecker.addChecker(3, 400);
            this.mIntervalChecker.addChecker(4, 150);
            this.mIntervalChecker.addChecker(5, 600);
        }
        setQuickClipIcon(false, false);
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        setCaptureButtonEnable(enable, getShutterButtonType());
    }

    public void setCaptureButtonEnable(boolean enable, int type) {
        if (this.mCameraState < 1) {
            CamLog.m3d(CameraConstants.TAG, "mCameraState = " + this.mCameraState + " enable : " + enable + ", However, preview is not available, so cannot enable capture button");
            enable = false;
        }
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonEnable(enable, type);
        }
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        super.setContentFrameLayoutParam(params);
        if (this.mDoubleCameraManager != null) {
            int previewTopMargin;
            int previewStartMargin = Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()) ? params.getMarginStart() : params.topMargin;
            if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                previewTopMargin = params.topMargin;
            } else {
                previewTopMargin = params.getMarginStart();
            }
            this.mDoubleCameraManager.setButtonParentLayout(params.width, params.height, previewStartMargin, previewTopMargin);
        }
        if (this.mZoomManager != null) {
            this.mZoomManager.setJogZoomMinimapLayout(params);
        }
        if (this.mStickerManager != null && !isStickerSupportedCameraMode() && this.mStickerManager.isRunning()) {
            this.mStickerManager.stop();
            this.mExtraPrevewUIManager.setEditDim(false);
            settingForSticker(false);
        }
    }

    boolean isStickerSupportedCameraMode() {
        if (!getShotMode().contains(CameraConstants.MODE_BEAUTY) && !getShotMode().equals("mode_normal")) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "Sticker supported! getShotMode() : " + getShotMode());
        return true;
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        doCleanView(false, false, false);
        doCleanViewAfter(false);
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)) && isAvailableTilePreview()) {
            this.mGet.showTilePreview(true);
        } else {
            this.mGet.showTilePreview(false);
        }
        if (isActivatedQuickdetailView()) {
            setDeleteButtonVisibility(true);
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        showSavingDialog(false, 0);
        showFrameGridView("off", false);
        if (isJogZoomMoving()) {
            stopShutterZoom();
        }
        stopPreview();
        if (this.mHandler != null) {
            this.mHandler.removeMessages(6);
            this.mHandler.removeMessages(85);
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.release();
        }
        setShutterButtonListener(false);
        setExtraButtonListener(false);
        closeCamera();
        this.mLocalParamForZoom = null;
        this.mIsBindingListner = false;
        CamLog.m3d(CameraConstants.TAG, "onPauseAfter - end");
    }

    public void onDestroy() {
        if (this.mIntervalChecker != null) {
            this.mIntervalChecker.clearChecker();
        }
        if (this.mChildSettingMap != null) {
            this.mChildSettingMap.clear();
            this.mChildSettingMap = null;
        }
        if (FunctionProperties.isSupportedSticker() && this.mStickerManager != null) {
            String modeNext = this.mGet.getCurSettingValue(Setting.KEY_MODE);
            if ((modeNext == null || !modeNext.contains(CameraConstants.MODE_BEAUTY)) && !"mode_normal".equals(modeNext)) {
                this.mExtraPrevewUIManager.setLastSelectedMenu(0);
                CamLog.m3d("StickerManager", "sticker stop next not available mode = " + modeNext);
                if (this.mStickerManager.isRunning()) {
                    this.mStickerManager.stop(true);
                    settingForSticker(false);
                }
            } else if (this instanceof AttachCameraModule) {
                CamLog.m3d("StickerManager", "onDestroy attach exception hasSticker + " + this.mStickerManager.hasSticker());
                if (!this.mStickerManager.hasSticker()) {
                    this.mExtraPrevewUIManager.setLastSelectedMenu(0);
                    if (this.mStickerManager.isRunning()) {
                        this.mStickerManager.stop(true);
                        settingForSticker(false);
                    }
                }
            }
        }
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration config) {
        this.mIsBindingListner = false;
        super.onConfigurationChanged(config);
        if (this.mBarManager != null) {
            this.mBarManager.destoryAllBar();
        }
        setPreviewLayoutParam();
        if (setListener()) {
            this.mIsBindingListner = true;
        }
    }

    protected int[] changeLightFramePreviewSize(int[] previewSize) {
        if (FunctionProperties.isSupportedLightFrame() && this.mLightFrameManager != null && this.mLightFrameManager.isLightFrameMode()) {
            return this.mLightFrameManager.changeLightFramePreviewSize(previewSize);
        }
        return previewSize;
    }

    protected void changeFullVisionSettingOnPictureSizeChanged(ListPreference listPref, String value) {
        boolean isFullSize = false;
        if (listPref != null) {
            int[] size = Utils.sizeStringToArray(value);
            if (size[0] >= size[1] * 2) {
                isFullSize = true;
            }
            if (isFullSize) {
                setSetting(Setting.KEY_FULLVISION, "on", true);
            } else {
                setSetting(Setting.KEY_FULLVISION, "off", true);
                SharedPreferenceUtil.setPictureSizeBackupIndex(getAppContext(), SettingKeyWrapper.getPictureSizeBackupKey(this.mCameraId), listPref.findIndexOfValue(value));
            }
            this.mGet.updateButtonBySetting(Setting.KEY_FULLVISION);
        }
    }

    protected void onFullVisionSettingClicked(boolean changeToFullVision) {
        boolean isLowPictureSize = true;
        CamLog.m3d(CameraConstants.TAG, "-FullVision- updateFullVisionSettingValue, changeToFullVision = " + changeToFullVision);
        setSetting(Setting.KEY_FULLVISION, changeToFullVision ? "on" : "off", true);
        ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), getCameraId()));
        int index = 0;
        if (listPref != null) {
            CharSequence[] entryValues = listPref.getEntryValues();
            if (changeToFullVision) {
                for (int i = 0; i < entryValues.length; i++) {
                    int[] size = Utils.sizeStringToArray(entryValues[i].toString());
                    if (size[0] >= size[1] * 2) {
                        index = i;
                        break;
                    }
                }
                if (entryValues.length > 6) {
                    if (listPref.findIndexOfValue(listPref.getValue()) % 2 == 0) {
                        isLowPictureSize = false;
                    }
                    if (isLowPictureSize) {
                        index++;
                    }
                }
                if (index >= 0 && index < entryValues.length) {
                    this.mGet.childSettingMenuClicked(listPref.getKey(), entryValues[index].toString());
                    return;
                }
                return;
            }
            String value = listPref.findValueOfIndex(SharedPreferenceUtil.getPictureSizeBackupIndex(getAppContext(), SettingKeyWrapper.getPictureSizeBackupKey(this.mCameraId)));
            CamLog.m3d(CameraConstants.TAG, "-FullVision- update picture size to backuped picture size, pictureSize = " + value);
            if (value == null || value.equals("")) {
                value = entryValues[0].toString();
            }
            this.mGet.childSettingMenuClicked(listPref.getKey(), value);
        }
    }

    protected void setTilePreviewSetting(String pictueSize) {
        if (RatioCalcUtil.getRatio(pictueSize) == 2.0f) {
            changeTilePreviewSetting();
            return;
        }
        restoreTilePreviewSetting();
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            this.mGet.thumbnailListInit();
            if (this.mGet.isActivatedTilePreview()) {
                this.mGet.showTilePreviewCoverView(false);
            }
        }
    }

    protected void changeTilePreviewSetting() {
        if (this.mGet.isActivatedTilePreview()) {
            this.mGet.showTilePreviewCoverView(true);
        } else {
            this.mGet.showTilePreview(false);
        }
        if (ModelProperties.isLguCloudServiceModel() && CameraConstants.STORAGE_NAME_NAS.equals(this.mGet.getCurSettingValue(Setting.KEY_STORAGE))) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] turnOff TilePreview by Storage changed");
            setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        } else if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] turnOff TilePreview by PictureSize changed");
            setSetting(Setting.KEY_TILE_PREVIEW, "off", false);
            if (isAvailableTilePreview()) {
                showToast(this.mGet.getActivity().getString(C0088R.string.camera_roll_off_desc), CameraConstants.TOAST_LENGTH_SHORT);
            }
        }
    }

    protected void afterPictureSizeChanged(String previewSize) {
    }

    protected void setupVideosize(CameraParameters parameters, ListPreference listPref) {
    }

    protected void updateSecondCameraSettings(String key, String value, boolean persist) {
        if (FunctionProperties.getCameraTypeRear() == 1 && isRearCamera() && !getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            int settingIndex = getSettingIndex(key);
            int notSelectedCameraId = this.mCameraId == 0 ? 2 : 0;
            String notSelectedCameraSettingKey = null;
            if (SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId).equals(key)) {
                notSelectedCameraSettingKey = SettingKeyWrapper.getPictureSizeKey(getShotMode(), notSelectedCameraId);
            } else if (SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId).equals(key)) {
                notSelectedCameraSettingKey = SettingKeyWrapper.getVideoSizeKey(getShotMode(), notSelectedCameraId);
            }
            ListPreference pref = getListPreference(notSelectedCameraSettingKey);
            if (pref != null) {
                CharSequence[] entryValues = pref.getEntryValues();
                String toSetValue = null;
                if (entryValues != null && entryValues.length >= settingIndex) {
                    toSetValue = entryValues[settingIndex];
                }
                setSetting(notSelectedCameraSettingKey, toSetValue, persist);
            }
        }
    }

    protected void updateHDRSettingValue(String value) {
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            String prevHDRValue = parameters.get("hdr-mode");
            String settingKey = !this.mCameraCapabilities.isFlashSupported() ? Setting.KEY_LIGHTFRAME : "flash-mode";
            if ("1".equals(value)) {
                if ("flash-mode".equals(settingKey)) {
                    setSpecificSettingValueAndDisable(settingKey, "off", false);
                    updateFlashParam(parameters, 50, false);
                }
            } else if ("flash-mode".equals(settingKey)) {
                ListPreference flashPref = getListPreference(settingKey);
                if (flashPref != null) {
                    String savedValue = flashPref.loadSavedValue();
                    setSettingMenuEnable(settingKey, true);
                    updateFlashParam(parameters, ParamUtils.getFlashMode(savedValue), true);
                } else {
                    return;
                }
            }
            updateHDRParam(parameters, value, true, true);
            this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
            setHDRMetaDataCallback(value);
            if (!FunctionProperties.isSupportedHDRPreview() || !isRearCamera()) {
                return;
            }
            if (("0".equals(prevHDRValue) && !"0".equals(value)) || (!"0".equals(prevHDRValue) && "0".equals(value))) {
                setupPreview(parameters);
            }
        }
    }

    protected void setNightVisionParameters(boolean isOn) {
        setNightVisionParameters(isOn, false);
    }

    protected void setNightVisionParameters(boolean isOn, boolean isForced) {
        if (FunctionProperties.isSupportedBinning() && this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                CamLog.m3d(CameraConstants.TAG, "[NightVision] setNightVisionParameters set param - isOn ? " + isOn + ", isForced : " + isForced);
                this.mParamUpdater.setParamValue(ParamConstants.KEY_NIGHTVISION_PARAM, isOn ? "on" : "off", false);
                if (isForced) {
                    parameters.set(ParamConstants.KEY_NIGHTVISION_PARAM, isOn ? "on" : "off");
                    if (!isOn) {
                        parameters.set(ParamConstants.KEY_BINNING_PARAM, "normal");
                    }
                    setParameters(parameters);
                }
            }
        }
    }

    public void onBinningIconVisible(boolean visible, int whereFrom) {
    }

    public boolean isSettingMenuItemAvailable() {
        if (!checkModuleValidate(31) || isPostviewShowing() || this.mSnapShotChecker.checkMultiShotState(1) || this.mIsGoingToPostview) {
            return false;
        }
        return true;
    }

    protected void createChildSettingMap() {
        this.mChildSettingMap = new HashMap();
        this.mChildSettingMap.put(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), this.mChildSettingUpdater_pictureSize);
        this.mChildSettingMap.put(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), this.mChildSettingUpdater_videoSize);
        if (FunctionProperties.isSupportedInAndOutZoom() && isRearCamera()) {
            int anotherCameraId = this.mCameraId == 0 ? 2 : 0;
            this.mChildSettingMap.put(SettingKeyWrapper.getPictureSizeKey(getShotMode(), anotherCameraId), this.mChildSettingUpdater_pictureSize);
            this.mChildSettingMap.put(SettingKeyWrapper.getVideoSizeKey(getShotMode(), anotherCameraId), this.mChildSettingUpdater_videoSize);
        }
        this.mChildSettingMap.put(Setting.KEY_VOICESHUTTER, this.mChildSettingUpdater_voiceShutter);
        this.mChildSettingMap.put("tracking-af", this.mChildSettingUpdater_trackingAF);
        this.mChildSettingMap.put(Setting.KEY_TIMER, this.mChildSettingUpdater_timer);
        this.mChildSettingMap.put(Setting.KEY_STORAGE, this.mChildSettingUpdater_storage);
        this.mChildSettingMap.put("hdr-mode", this.mChildSettingUpdater_hdr);
        this.mChildSettingMap.put(Setting.KEY_FRAME_GRID, this.mChildSettingUpdater_frameGrid);
        this.mChildSettingMap.put(Setting.KEY_SAVE_DIRECTION, this.mChildSettingUpdater_saveDirection);
        this.mChildSettingMap.put(Setting.KEY_LIGHTFRAME, this.mChildSettingUpdater_lightFrame);
        this.mChildSettingMap.put(Setting.KEY_SWAP_CAMERA, this.mChildSettingUpdater_swapCamera);
        this.mChildSettingMap.put(Setting.KEY_AU_CLOUD, this.mChildSettingUpdater_auCloud);
        this.mChildSettingMap.put(Setting.KEY_MOTION_QUICKVIEWER, this.mChildSettingUpdater_motionQuickview);
        this.mChildSettingMap.put(Setting.KEY_VIDEO_STEADY, this.mChildSettingUpdater_manual_video_steady);
        if (isSupportedFingerDetection()) {
            this.mChildSettingMap.put(Setting.KEY_FINGER_DETECTION, this.mChildSettingUpdater_finger_detection);
        }
        this.mChildSettingMap.put(Setting.KEY_TAG_LOCATION, this.mChildSettingUpdater_tagLocation);
        if (FunctionProperties.isSignatureSupported(getAppContext())) {
            this.mChildSettingMap.put(Setting.KEY_SIGNATURE, this.mChildSettingUpdater_signature);
        }
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) {
            this.mChildSettingMap.put(Setting.KEY_SQUARE_PICTURE_SIZE, this.mChildSettingUpdater_pictureSize);
            this.mChildSettingMap.put(Setting.KEY_SQUARE_VIDEO_SIZE, this.mChildSettingUpdater_videoSize);
        }
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
            this.mChildSettingMap.put(Setting.KEY_GRAPHY, this.mChildSettingUpdater_manual_camera_graphy);
        }
        this.mChildSettingMap.put(Setting.KEY_QR, this.mChildSettingUpdater_qr);
        if (FunctionProperties.isLivePhotoSupported()) {
            this.mChildSettingMap.put(Setting.KEY_LIVE_PHOTO, this.mChildSettingUpdater_LivePhoto);
        }
        if (isRearCamera() && FunctionProperties.isSupportedBinning(0)) {
            this.mChildSettingMap.put(Setting.KEY_BINNING, this.mChildSettingUpdater_sw_pixel_binning);
        }
        if (FunctionProperties.isSupportedLGLens(this.mGet.getAppContext()) && FunctionProperties.isSupportedGoogleLens()) {
            this.mChildSettingMap.put(Setting.KEY_LENS_SELECTION, this.mChildSettingUpdater_lens_select);
        }
    }

    public void childSettingMenuClicked(String key, String value, int clickedType) {
        ListPreference listPref = getListPreference(key);
        if (listPref != null) {
            if (this.mChildSettingMap == null) {
                createChildSettingMap();
            }
            addModuleChildSettingMap(this.mChildSettingMap);
            ChildSettingRunnable childSetting = (ChildSettingRunnable) this.mChildSettingMap.get(key);
            if (childSetting != null) {
                if (clickedType != -1) {
                    childSetting.runChildSettingMenu(listPref, key, value, clickedType);
                } else {
                    childSetting.runChild(listPref, key, value);
                }
            }
            if (!Setting.KEY_FILM_EMULATOR.equals(key) && this.mHandler != null && isMenuShowing(4)) {
                this.mHandler.sendEmptyMessage(84);
            }
        }
    }

    public void setSpecificSettingValueAndDisable(String key, String value, boolean save) {
        ListPreference listPref = getListPreference(key);
        if (listPref != null && value != null) {
            CharSequence[] entryValues = listPref.getEntryValues();
            boolean found = false;
            String foundValue = listPref.getDefaultValue();
            for (CharSequence entryValue : entryValues) {
                foundValue = String.valueOf(entryValue);
                if (value.equals(foundValue)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                setSettingMenuEnable(key, false);
                setSetting(key, foundValue, save);
                CamLog.m3d(CameraConstants.TAG, "Setting KEY : [" + key + "], listPref-Value : " + value + ", found value = " + foundValue);
                CamLog.m3d(CameraConstants.TAG, "listPref-Pref Saved Value : " + listPref.loadSavedValue());
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "Value is invalid.");
        }
    }

    protected void createMediaSaveService() {
        this.mOnMediaSavedListener = new C014630();
    }

    public void callMediaSave(Uri uri) {
        this.mOnMediaSavedListener.onMediaSaved(uri);
    }

    public void notifyNewMedia(final Uri uri, final boolean updateThumbnail) {
        CamLog.m3d(CameraConstants.TAG, "notifyNewMedia : URI = " + uri);
        new Thread() {
            public void run() {
                FileManager.broadcastNewMedia(BaseModule.this.mGet.getAppContext(), uri);
                BaseModule.this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
                if (BaseModule.this.mGet.isLGUOEMCameraIntent()) {
                    BaseModule.this.mGet.postOnUiThread(BaseModule.this.mChangeToAttachModule);
                } else if (updateThumbnail && BaseModule.this.mReviewThumbnailManager != null) {
                    BaseModule.this.mReviewThumbnailManager.doAfterCaptureProcess(uri, false);
                }
            }
        }.start();
    }

    protected void setMediaSaveServiceListener(boolean set) {
        MediaSaveService mediaSaveService = this.mGet.getMediaSaveService();
        if (mediaSaveService == null) {
            return;
        }
        if (set) {
            mediaSaveService.setQueueStatusListener(this);
        } else {
            mediaSaveService.setQueueStatusListener(null);
        }
    }

    public void onMediaSaveServiceConnected(MediaSaveService service) {
        if (service != null) {
            service.setQueueStatusListener(this);
        }
    }

    protected void updateLightFrameParam(int lightFrameMsg) {
        if (this.mLightFrameManager != null) {
            String lightMode = "off";
            switch (lightFrameMsg) {
                case 56:
                    setSetting(Setting.KEY_LIGHTFRAME, "off", true);
                    this.mLightFrameManager.turnOffLightFrame();
                    return;
                case 57:
                    setSetting(Setting.KEY_LIGHTFRAME, "on", true);
                    this.mLightFrameManager.turnOnLightFrame();
                    if (this.mFocusManager != null) {
                        this.mFocusManager.setAEAFLock(false);
                        this.mFocusManager.hideAEAFText();
                        this.mFocusManager.hideAllFocus();
                        this.mFocusManager.releaseTouchFocus();
                        this.mFocusManager.resetEVValue(0);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected void setZoomCompensation(CameraParameters parameters) {
        if (parameters != null && this.mZoomManager != null && this.mParamUpdater != null) {
            if (!this.mGet.checkCameraChanging(4) || !isOpticZoomSupported(null)) {
                int zoomValueInt;
                if (isZoomAvailable()) {
                    this.mZoomManager.initZoomValues(parameters);
                    String zoomValueStr = Integer.toString(parameters.getZoom());
                    String backupValue = Integer.toString(this.mZoomManager.getZoomValue());
                    String compensationValue = "0";
                    if (!"not found".equals(zoomValueStr) && !"not found".equals(backupValue) && this.mZoomManager.isReadyZoom()) {
                        if ("0".equals(zoomValueStr)) {
                            compensationValue = backupValue;
                        } else {
                            compensationValue = zoomValueStr;
                        }
                        if ("0".equals(compensationValue)) {
                            updateZoomParam(parameters, 0);
                            this.mZoomManager.setZoomValue(0);
                            return;
                        }
                        zoomValueInt = Integer.parseInt(compensationValue);
                        updateZoomParam(parameters, zoomValueInt);
                        this.mZoomManager.setZoomValue(zoomValueInt);
                        this.mParamUpdater.setParameters(parameters, "zoom", Integer.toString(zoomValueInt));
                        return;
                    }
                    return;
                }
                CamLog.m5e(CameraConstants.TAG, "setZoomValue =0 ");
                if (isOpticZoomSupported(null)) {
                    this.mZoomManager.initZoomValues(parameters);
                    zoomValueInt = this.mZoomManager.getInitZoomStep(this.mCameraId);
                    updateZoomParam(parameters, zoomValueInt);
                    this.mZoomManager.setZoomValue(zoomValueInt);
                    return;
                }
                this.mZoomManager.setZoomValue(0);
            }
        }
    }

    public void updateZoomParam(CameraParameters parameters, int value) {
        if (this.mCameraDevice != null && parameters != null) {
            if (isRearCamera() || isSupportFrontZoom()) {
                if (isOpticZoomSupported(null)) {
                    if (!checkModuleValidate(7)) {
                        return;
                    }
                    if (this.mGet.isCameraChanging() && !this.mGet.checkCameraChanging(4)) {
                        return;
                    }
                } else if (!checkModuleValidate(15) || this.mIsSwitchingCameraDuringRecording) {
                    return;
                }
                if (parameters != null && this.mParamUpdater != null && this.mCameraDevice != null) {
                    synchronized (this.mLockUpdateZoomParam) {
                        this.mParamUpdater.setParameters(parameters, "zoom", Integer.toString(value));
                        if (this.mCameraState != 5) {
                            if (FunctionProperties.isSupportedSuperZoom() && checkModuleValidate(128) && isSuperZoomEnableCondition()) {
                                this.mLocalParamForZoom = this.mCameraDevice.setSuperZoom(parameters);
                            } else {
                                this.mCameraDevice.setParameters(parameters);
                            }
                        }
                    }
                }
            }
        }
    }

    public void onZoomShow() {
        boolean isRecordingState;
        this.mBeforeZoomCameraId = this.mCameraId;
        if (checkModuleValidate(128)) {
            isRecordingState = false;
        } else {
            isRecordingState = true;
        }
        if (!(isRecordingState || !isSupportedQuickClip() || getShotMode().contains(CameraConstants.MODE_SQUARE))) {
            this.mQuickClipManager.hide(false);
        }
        if (!(isRecordingState || this.mGifManager == null || getShotMode().contains(CameraConstants.MODE_SQUARE))) {
            this.mGifManager.hideTransient();
        }
        this.mLocalParamForZoom = null;
        if (this.mZoomManager != null) {
            this.mZoomManager.setZoomButtonVisibility(8);
        }
        if (isManualFocusModeEx()) {
            this.mFocusManager.hideFocusForce();
            setManualFocusButtonVisibility(false);
            setManualFocusModeEx(false);
        }
        hideFocusOnShowOtherBars(true);
        setFilmStrengthButtonVisibility(false, false);
        setFingerDetectionListener(false);
    }

    public void onZoomHide() {
        boolean isRecordingState;
        CamLog.m7i(CameraConstants.TAG, "onZoomHide");
        if (checkModuleValidate(128)) {
            isRecordingState = false;
        } else {
            isRecordingState = true;
        }
        syncOpticZoomCameraId(true);
        if (this.mCameraId == 2) {
            setFingerDetectionListener(true);
        } else if (this.mCameraId == 0) {
            setTrackingAFListener();
        }
        if (this.mFocusManager != null && isFocusEnableCondition() && checkModuleValidate(79) && !isRecordingState && this.mCameraState >= 1) {
            this.mFocusManager.registerCallback(true);
        }
        if (!(isRecordingState || this.mGifManager == null || getShotMode().contains(CameraConstants.MODE_SQUARE))) {
            this.mGifManager.restoreVisibility();
        }
        if (!(isRecordingState || !isSupportedQuickClip() || getShotMode().contains(CameraConstants.MODE_SQUARE))) {
            setQuickClipIcon(false, true);
            this.mQuickClipManager.show(DrawerShowOption.KEEP_CURRENT_STATE, true);
        }
        if (this.mZoomManager != null && isZoomAvailable() && checkDoubleCameraAvailableMode(false) && isRecordingState) {
            this.mZoomManager.setZoomButtonVisibility(0);
        }
        setFilmStrengthButtonVisibility(true, false);
    }

    protected void updateFrameGrid(String value) {
        setSetting(Setting.KEY_FRAME_GRID, value, true);
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            showFrameGridView(value, true);
        }
    }

    protected void updateMultiviewShotType(String value) {
    }

    protected void updateSaveDirection(String value) {
        setSetting(Setting.KEY_SAVE_DIRECTION, value, true);
    }

    protected void updateVoiceShutter(String value) {
        setSetting(Setting.KEY_VOICESHUTTER, value, true);
    }

    protected void updateTrackingAF(String value) {
        setSetting("tracking-af", value, true);
    }

    protected void updateTimer(String value) {
        setSetting(Setting.KEY_TIMER, value, false);
    }

    protected void updateTagLocation(String value) {
        setSetting(Setting.KEY_TAG_LOCATION, value, true);
    }

    protected void updateStorage(String value) {
        setSetting(Setting.KEY_STORAGE, value, true);
    }

    protected void updateMotionQuickview(String value) {
        setSetting(Setting.KEY_MOTION_QUICKVIEWER, value, true);
    }

    public void onScaleGestureBegin() {
        if (checkModuleValidate(95) && !this.mSnapShotChecker.checkMultiShotState(7) && !isRotateDialogVisible() && !this.mIsSwitchingCameraDuringRecording && !this.mIsPreviewCallbackWaiting && !isMenuShowing(CameraConstants.MENU_TYPE_ALL) && this.mSnapShotChecker.isAvailableGestureZoom()) {
            CamLog.m3d(CameraConstants.TAG, "onScaleGestureBegin");
            if (this.mZoomManager != null && isZoomAvailable()) {
                this.mZoomManager.onGestureZoomBegin();
            }
        }
    }

    public void onMultiTouchDetected() {
        if (this.mZoomManager != null) {
            if (this.mZoomManager.isZoomBarVisible()) {
                resetBarDisappearTimer(2, 3000);
            } else {
                showZoomBar();
            }
        }
    }

    protected void showZoomBar() {
        if (checkModuleValidate(95) && this.mCameraDevice != null && !this.mSnapShotChecker.checkMultiShotState(7) && !this.mGet.isCameraChanging() && !isTimerShotCountdown() && isZoomAvailable() && !isMenuShowing(20) && !this.mIsSwitchingCameraDuringRecording && !this.mIsPreviewCallbackWaiting && this.mSnapShotChecker.isAvailableGestureZoom()) {
            CamLog.m3d(CameraConstants.TAG, "showZoomBar");
            if (this.mZoomManager != null) {
                resetBarDisappearTimer(2, 3000);
                this.mZoomManager.setZoomBarVisibility(0);
            }
        }
    }

    public void onScaleGesture(int gapSpan, int totalDistance) {
        if (checkModuleValidate(80) && this.mCameraDevice != null && !this.mSnapShotChecker.checkMultiShotState(7) && !isTimerShotCountdown() && !isRotateDialogVisible() && !this.mIsPreviewCallbackWaiting && !isMenuShowing(CameraConstants.MENU_TYPE_ALL) && this.mSnapShotChecker.isAvailableGestureZoom()) {
            if (isOpticZoomSupported(null)) {
                if (!checkModuleValidate(7)) {
                    return;
                }
                if (this.mGet.isCameraChanging() && !this.mGet.checkCameraChanging(4)) {
                    return;
                }
            } else if (!checkModuleValidate(15) || this.mIsSwitchingCameraDuringRecording) {
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "onScaleGesture, gapSpan : " + gapSpan);
            if (this.mZoomManager != null) {
                this.mZoomManager.onGestureZoomStep(gapSpan, totalDistance);
            }
        }
    }

    public void switchCameraOnFront(int aniType) {
        if (this.mHandler != null && !isTimerShotCountdown()) {
            ModuleDeviceHandler.sIsFrontSwitching = true;
            CamLog.m3d(CameraConstants.TAG, "switchCameraOnFront");
            if (!isLightFrameOn()) {
                this.mGet.startCameraSwitchingAnimation(aniType);
            }
            this.mHandler.removeMessages(6);
            this.mHandler.sendEmptyMessage(6);
        }
    }

    public void onScaleGestureEnd() {
        if (!checkModuleValidate(95) || this.mCameraDevice == null || isTimerShotCountdown() || isRotateDialogVisible() || this.mSnapShotChecker.checkMultiShotState(7) || this.mIsPreviewCallbackWaiting || isMenuShowing(CameraConstants.MENU_TYPE_ALL) || !this.mSnapShotChecker.isAvailableGestureZoom()) {
            this.mZoomManager.setGestureZooming(false);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "onScaleGestureEnd");
        if (this.mZoomManager != null && isRearCamera()) {
            this.mZoomManager.onGestureZoomEnd();
        }
    }

    protected void doPhoneStateListenerAction(int state) {
    }

    public boolean setShutterButtonListener(boolean set) {
        OnLongClickListener onLongClickListener = null;
        ShutterButton shutterTop = (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_top_comp);
        ShutterButton shutterBottom = (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_bottom_comp);
        ShutterButtonBase shutterLarge = (ShutterButtonBase) this.mGet.findViewById(C0088R.id.shutter_large_comp);
        if (shutterTop == null || shutterBottom == null || shutterLarge == null) {
            CamLog.m3d(CameraConstants.TAG, "shutterTop or shutterBottom or shutterLarge is null");
            return false;
        }
        OnShutterButtonListener c014933;
        if (set) {
            c014933 = new C014933();
        } else {
            c014933 = null;
        }
        shutterTop.setOnShutterButtonListener(c014933);
        if (set) {
            c014933 = new C015034();
        } else {
            c014933 = null;
        }
        shutterBottom.setOnShutterButtonListener(c014933);
        shutterBottom.setLongShutterDelay(500);
        shutterLarge.setOnClickListener(set ? new C015135() : null);
        if (set) {
            onLongClickListener = new C015236();
        }
        shutterLarge.setOnLongClickListener(onLongClickListener);
        shutterLarge.setOnShutterButtonTouchListener(set);
        setShutterZoomListener(set);
        return true;
    }

    protected void onShutterTopTouchUp() {
    }

    public void setShutterZoomListener(boolean set) {
        ShutterButton shutterBottom;
        ShutterButtonBase shutterLarge;
        if (!set) {
            shutterBottom = (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_bottom_comp);
            shutterLarge = (ShutterButtonBase) this.mGet.findViewById(C0088R.id.shutter_large_comp);
            if (shutterBottom != null) {
                shutterBottom.setOnShutterZoomListener(null);
            }
            if (shutterLarge != null) {
                shutterLarge.setOnShutterZoomListener(null);
            }
        } else if (isShutterZoomSupported()) {
            shutterBottom = (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_bottom_comp);
            shutterLarge = (ShutterButtonBase) this.mGet.findViewById(C0088R.id.shutter_large_comp);
            OnShutterZoomListener listener = new C015337();
            if (shutterBottom != null) {
                shutterBottom.setOnShutterZoomListener(listener);
            }
            if (shutterLarge != null) {
                shutterLarge.setOnShutterZoomListener(listener);
            }
        }
    }

    protected boolean moveShutterZoom(int moveX) {
        if (isJogZoomAvailable()) {
            int maxDistance = this.mCaptureButtonManager.getShutterZoomMaxDistance();
            int jogZoomLevel = (int) (((float) ((-moveX) + maxDistance)) / (((float) (maxDistance * 2)) / 18.0f));
            if (this.mZoomManager.skipShutterZoom(jogZoomLevel)) {
                return false;
            }
            boolean isJogZoomAvailable = this.mZoomManager.moveJogZoom(jogZoomLevel);
            if (!isJogZoomMoving()) {
                CamLog.m3d(CameraConstants.TAG, "[jog] shutter can't move");
                return false;
            } else if (this.mReviewThumbnailManager == null || this.mCaptureButtonManager == null || this.mBackButtonManager == null) {
                return false;
            } else {
                this.mBackButtonManager.hide();
                this.mReviewThumbnailManager.setThumbnailVisibility(8);
                this.mCaptureButtonManager.moveByShutterZoom(moveX);
                return isJogZoomAvailable;
            }
        }
        stopShutterZoom();
        return false;
    }

    public boolean isJogZoomAvailable() {
        if (!isZoomAvailable() || !checkModuleValidate(208) || this.mSnapShotChecker.getPictureCallbackState() != 0 || this.mSnapShotChecker.getRawPicState() != 0 || isMenuShowing(CameraConstants.MENU_TYPE_ALL) || this.mAdvancedFilmManager == null || this.mAdvancedFilmManager.getFilmState() == 3 || isPaused() || this.mReviewThumbnailManager == null || this.mReviewThumbnailManager.isQuickViewAniStarted() || this.mZoomManager == null || this.mZoomManager.isZoomControllersGetTouched() || isTimerShotCountdown() || isCameraChanging() || isAnimationShowing() || isProgressDialogVisible() || isRotateDialogVisible() || isModuleChanging() || this.mGet == null || this.mGet.isActivatedQuickdetailView() || this.mGet.isHelpListVisible() || !isRearCamera() || SystemBarUtil.isSystemUIVisible(getActivity())) {
            return false;
        }
        if (isOpticZoomSupported(null) || this.mZoomManager.isInAndOutSwithing() || !this.mIsPreviewCallbackWaiting) {
            return true;
        }
        return false;
    }

    public void stopShutterZoom() {
        if (this.mCaptureButtonManager != null && this.mBackButtonManager != null && this.mZoomManager != null && isJogZoomMoving()) {
            CamLog.m3d(CameraConstants.TAG, "[jog] stopShutterZoom");
            this.mZoomManager.stopJogZoom();
            this.mParamUpdater.setParameters(this.mLocalParamForZoom, ParamConstants.KEY_JOG_ZOOM, Integer.toString(0));
            this.mParamUpdater.setParameters(this.mLocalParamForZoom, "zoom", Integer.toString(this.mZoomManager.getZoomValue()));
            this.mBackButtonManager.show();
            if (!isAttachIntent()) {
                this.mReviewThumbnailManager.setThumbnailVisibility(0);
            }
            this.mCaptureButtonManager.stopShutterZoom();
            showFlashHDRIndicators(false);
        }
    }

    protected void setExtraButtonListener(boolean set) {
        OnClickListener onClickListener = null;
        ImageButton extraButtonTop = (ImageButton) this.mGet.findViewById(C0088R.id.extra_button_top_comp);
        ImageButton extraButtonBottom = (ImageButton) this.mGet.findViewById(C0088R.id.extra_button_bottom_comp);
        if (extraButtonTop == null || extraButtonBottom == null) {
            CamLog.m3d(CameraConstants.TAG, "extraButtonTop or extraButtonBottom is null");
            return;
        }
        extraButtonTop.setOnClickListener(set ? new C015438() : null);
        if (set) {
            onClickListener = new C015539();
        }
        extraButtonBottom.setOnClickListener(onClickListener);
    }

    protected void setMenuButtonListener() {
        ImageView menuButton = (ImageView) this.mGet.findViewById(C0088R.id.menu_button);
        menuButton.setVisibility(0);
        menuButton.setOnClickListener(new C015840());
        menuButton.setOnFocusChangeListener(new C016041());
    }

    protected void showMenuButton(boolean show) {
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            this.mGet.findViewById(C0088R.id.menu_button).setVisibility(show ? 0 : 4);
        }
    }

    protected void requestFocusOnMenuButton(boolean focus) {
        this.mGet.findViewById(C0088R.id.menu_button).setFocusable(focus);
        if (focus) {
            this.mGet.findViewById(C0088R.id.menu_button).requestFocus();
        } else {
            this.mGet.findViewById(C0088R.id.menu_button).clearFocus();
        }
    }

    public void stopRecordByCallPopup() {
        if (!checkModuleValidate(128)) {
            onVideoStopClicked(false, false);
        }
    }

    public void setQuickButtonByPreset(boolean enable, boolean visible) {
        if (this.mQuickButtonManager != null) {
            int preset;
            int supportedQuickButtons = 0;
            if (this.mCameraState == 6 || this.mCameraState == 7 || this.mCameraState == 5) {
                if (isRearCamera()) {
                    preset = 4;
                } else {
                    preset = 5;
                }
                if (this.mCameraCapabilities != null) {
                    if (!this.mCameraCapabilities.isFlashSupported()) {
                        supportedQuickButtons = 0 | 2;
                    }
                    if (!isRearCamera() && this.mCameraCapabilities.isFlashSupported()) {
                        supportedQuickButtons |= 8;
                    }
                    if (preset == 4 && this.mStickerManager != null && this.mStickerManager.isStickerDrawing()) {
                        supportedQuickButtons |= 2;
                    }
                }
            } else {
                preset = setQuickButtonPresetWithMode(1);
                supportedQuickButtons = setSupportedQuickbuttonType(0);
            }
            this.mQuickButtonManager.setQuickButtons(preset, supportedQuickButtons, enable, visible);
        }
    }

    protected int setSupportedQuickbuttonType(int supportedQuickButtons) {
        if (this.mCameraCapabilities == null) {
            return supportedQuickButtons;
        }
        if (!this.mCameraCapabilities.isFrontCameraSupported()) {
            supportedQuickButtons |= 1;
        }
        if (ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS.length <= 1 && isRearCamera()) {
            supportedQuickButtons |= 4;
        }
        if (ConfigurationUtil.sMODE_FRONT_SUPPORTED_ITEMS.length <= 1 && !isRearCamera()) {
            supportedQuickButtons |= 4;
        }
        if (!this.mCameraCapabilities.isFlashSupported() && isRearCamera()) {
            supportedQuickButtons |= 2;
        } else if (!FunctionProperties.isSupportedLightFrame() && !isRearCamera() && !this.mCameraCapabilities.isFlashSupported()) {
            supportedQuickButtons |= 2;
        } else if (this.mCameraCapabilities.isFlashSupported() && !isRearCamera()) {
            supportedQuickButtons |= 8;
        }
        return supportedQuickButtons;
    }

    protected int setQuickButtonPresetWithMode(int preset) {
        preset = isRearCamera() ? 1 : 2;
        if (preset == 1) {
            if (ManualUtil.isManualCameraMode(getShotMode())) {
                preset = 7;
            } else if (ManualUtil.isManualVideoMode(getShotMode())) {
                preset = 8;
            }
        }
        if (CameraConstants.MODE_SNAP.equals(getShotMode())) {
            preset = 9;
        }
        if (CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            preset = 10;
        }
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            return 6;
        }
        return preset;
    }

    public void setQuickButtonEnable(int id, boolean enable, boolean changeColor) {
        if (this.mQuickButtonManager == null) {
            return;
        }
        if (id == 100 && enable) {
            this.mQuickButtonManager.updateButton(id);
        } else {
            this.mQuickButtonManager.setEnable(id, enable, changeColor);
        }
    }

    public void setQuickButtonIndex(int id, int index) {
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setButtonIndex(id, index);
        }
    }

    public void setQuickButtonSelected(int id, boolean selected) {
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setSelected(id, selected);
        }
    }

    public void hideAllToast() {
        if (this.mToastManager != null) {
            this.mToastManager.hideAllToast();
        }
    }

    public String getStorageDir(int storageType) {
        if (this.mStorageManager != null) {
            return this.mStorageManager.getStorageDir(storageType);
        }
        return null;
    }

    public String getStorageSaveDir(int storageType) {
        if (this.mStorageManager != null) {
            return this.mStorageManager.getDir(storageType);
        }
        return null;
    }

    public ArrayList<String> getAllDir(boolean includeCNAS) {
        if (this.mStorageManager != null) {
            return this.mStorageManager.getAllDir(includeCNAS);
        }
        return null;
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        if (this.mIndicatorManager != null && this.mQuickButtonManager != null && this.mBackButtonManager != null && this.mCaptureButtonManager != null && this.mReviewThumbnailManager != null && this.mDoubleCameraManager != null && this.mAdvancedFilmManager != null && this.mIndicatorManager != null) {
            if (!this.mGet.isAttachIntent() || !saveState) {
                Log.d(CameraConstants.TAG, "[Time Info] doCleanView : doByAction = " + doByAction + ", useAnimation = " + useAnimation + ", saveState = " + saveState);
                if (doByAction) {
                    boolean z;
                    hideMenu(PointerIconCompat.TYPE_ZOOM_OUT, true, true);
                    if (FunctionProperties.isSupportedInAndOutZoom() || isSupportedCropAngle()) {
                        showDoubleCamera(false);
                    }
                    this.mQuickButtonManager.hide(false, useAnimation, false);
                    this.mGet.setUspVisibility(8);
                    if (!getRecordingPreviewState(1) || getRecordingPreviewState(16)) {
                        this.mCaptureButtonManager.setShutterButtonVisibility(8, getShutterButtonType(), useAnimation);
                        if (!isAttachIntent()) {
                            if (sFirstTaken) {
                                this.mReviewThumbnailManager.setThumbnailVisibility(0, false);
                            } else {
                                this.mReviewThumbnailManager.setThumbnailVisibility(8, useAnimation);
                            }
                        }
                    }
                    hideZoomBar();
                    if (this.mAdvancedFilmManager.isShowingFilmMenu()) {
                        this.mAdvancedFilmManager.showFilmMenu(false, 3, true, true, 0, true);
                        this.mExtraPrevewUIManager.changeButtonState(0, false);
                    }
                    if (!useAnimation || getRecordingPreviewState(16)) {
                        z = false;
                    } else {
                        z = true;
                    }
                    showExtraPreviewUI(false, z, true, true);
                    this.mGifManager.setGifVisibility(false);
                    this.mGifManager.setGifVisibleStatus(false);
                    if (isTimerShotCountdown()) {
                        this.mIndicatorManager.setBatteryIndicatorVisibility(false);
                        setTimerIndicatorVisibility(false);
                    }
                    this.mBackButtonManager.setBackButton(false);
                    if (saveState) {
                        setQuickClipIcon(false, false);
                        return;
                    }
                    return;
                }
                if (this.mGet.isAttachIntent()) {
                    this.mGet.setConeModeChanged();
                }
                if (checkModuleValidate(8) && !this.mGet.isSettingMenuVisible()) {
                    this.mQuickButtonManager.show(false, useAnimation, true);
                }
                this.mGet.setUspVisibility(0);
                doShowCmdButtons(useAnimation);
                if (this.mIndicatorManager != null) {
                    this.mIndicatorManager.initIndicatorListAndLayout();
                    setBatteryIndicatorVisibility(true);
                    setTimerIndicatorVisibility(true);
                }
                if (isRearCamera() || !this.mGet.isModuleChanging()) {
                    showExtraPreviewUI(true, false, false, !isRearCamera());
                }
                if (!(this.mSnapShotChecker.checkMultiShotState(7) || this.mGet.isModuleChanging())) {
                    doShowDoubleCamera();
                }
                this.mBackButtonManager.setBackButton(false);
            }
        }
    }

    protected void doShowCmdButtons(boolean useAnimation) {
        this.mCaptureButtonManager.setShutterButtonVisibility(0, getShutterButtonType(), useAnimation);
        if (!isAttachIntent() && !isJogZoomMoving()) {
            this.mReviewThumbnailManager.setThumbnailVisibility(0, useAnimation);
        }
    }

    protected void doShowDoubleCamera() {
        if (getShotMode().contains(CameraConstants.MODE_SQUARE) && this.mAdvancedFilmManager.getSelfieFilterVisibility()) {
            CamLog.m3d(CameraConstants.TAG, "[filter] doShowDoubleCamera return");
        } else {
            showDoubleCamera(true);
        }
    }

    public void doCleanViewAfter(boolean anim) {
    }

    protected void updateCurrentViewModeParam(boolean updateDeviceParam) {
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                setParamUpdater(parameters, ParamConstants.KEY_VIEW_MODE, getCurrentViewModeToString());
                if (updateDeviceParam) {
                    this.mCameraDevice.setParameters(parameters);
                }
            }
        }
    }

    protected boolean checkConeMenuClicked(int mode) {
        if (!checkModuleValidate(223) || checkCurrentConeMode(mode) || isIntervalShotProgress() || isTimerShotCountdown() || isProgressDialogVisible() || this.mGet.isModuleChanging()) {
            return false;
        }
        return true;
    }

    protected void setCameraIdBeforeChange(boolean showAni, int cameraId, boolean forceChange) {
        if (this.mCameraId != cameraId || forceChange) {
            boolean beforeIsRearCamera = isRearCamera();
            this.mGet.setCameraChanging(1);
            this.mCameraId = cameraId;
            SharedPreferenceUtilBase.setCameraId(getAppContext(), this.mCameraId);
            boolean afterIsRearCamera = isRearCamera();
            if (beforeIsRearCamera && !afterIsRearCamera) {
                this.mGet.setSetting(Setting.KEY_SWAP_CAMERA, "front", true);
                restoreSettingMenus();
                this.mGet.setupSetting();
            } else if (!beforeIsRearCamera && afterIsRearCamera) {
                this.mGet.setSetting(Setting.KEY_SWAP_CAMERA, "rear", true);
                restoreSettingMenus();
                this.mGet.setupSetting();
            }
            if (!this.mGet.isAnimationShowing() && showAni) {
                this.mGet.setPreviewVisibility(4);
            }
        }
    }

    protected void restoreTrackingAFSetting() {
    }

    protected boolean isDefaultMode() {
        if ("mode_normal".equals(getShotMode())) {
            return true;
        }
        if (getCameraIdFromPref() != 0) {
            String shotMode = getShotMode();
            if (shotMode != null && shotMode.contains(CameraConstants.MODE_BEAUTY)) {
                return true;
            }
        }
        return false;
    }

    protected void changeToAutoView() {
        CamLog.m11w(CameraConstants.TAG, "changeToAutoView, the same view mode is clicked");
        this.mGet.setPreviewCoverVisibility(8, false, null, true, false);
    }

    protected void changeToManualCamera() {
        boolean isForceChanged = isOpticZoomSupported(null) != isOpticZoomSupported(CameraConstants.MODE_MANUAL_CAMERA);
        this.mGet.setCurrentConeMode(2, true);
        changeCameraIdForManualMode(isForceChanged);
        setSetting(Setting.KEY_MODE, "mode_normal", true);
        this.mGet.modeMenuClicked(CameraConstants.MODE_MANUAL_CAMERA);
    }

    protected void changeToManualVideo() {
        this.mGet.setCurrentConeMode(3, true);
        changeCameraIdForManualMode(isOpticZoomSupported(CameraConstants.MODE_MANUAL_VIDEO));
        setSetting(Setting.KEY_MODE, "mode_normal", true);
        this.mGet.modeMenuClicked(CameraConstants.MODE_MANUAL_VIDEO);
    }

    protected void changeCameraIdForManualMode(boolean forceChange) {
        if (!isRearCamera() || forceChange) {
            setCameraIdBeforeChange(false, SharedPreferenceUtil.getRearCameraId(getAppContext()), forceChange);
        }
    }

    protected void changeToSnapMovie() {
        String modeStr = CameraConstants.MODE_SNAP;
        this.mGet.setCurrentConeMode(1, true);
        setSetting(Setting.KEY_MODE, modeStr, true);
        this.mGet.modeMenuClicked(CameraConstants.MODE_SNAP);
    }

    public void setTilePreviewLayout(boolean isTilePreviewOn) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] setTilePreviewLayout isTilePreviewOn : " + isTilePreviewOn + "  isRecording : " + isRecordingState());
        if (!FunctionProperties.isSupportedCameraRoll() || !this.mGet.checkModuleValidate(207) || !isAvailableTilePreview() || isAttachIntent() || isVideoAttachMode() || this.mGet.isAnimationShowing() || isTimerShotCountdown() || isActivatedQuickview() || isModuleChanging() || this.mGet.isPaused() || isRecordingAnimShowing()) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] return");
            return;
        }
        ListPreference listPref;
        if (isRecordingPriorityMode() || CameraConstants.MODE_MANUAL_VIDEO.equals(getShotMode())) {
            listPref = this.mGet.getListPreference(getVideoSizeSettingKey());
        } else {
            listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        }
        if (listPref != null) {
            int[] size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
            float ratio = ((float) size[0]) / ((float) size[1]);
            if (((double) ratio) == 2.0d) {
                this.mGet.showTilePreview(false);
            }
            if (!isLightFrameOn()) {
                if (isTilePreviewOn && !isRecordingState()) {
                    CamLog.m7i(CameraConstants.TAG, "[Tile] ratio : " + ratio);
                    if ((2.0d > ((double) ratio) && ((double) ratio) > 1.7d) || (1.7d > ((double) ratio) && ((double) ratio) > 1.3d)) {
                        int topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
                        this.mGet.setTextureLayoutParams(size[0], size[1], topMargin);
                        if (this.mStickerManager != null) {
                            this.mStickerManager.setMarginLayoutParamsForGLView(this.mGet.calStartMargin(size[0], size[1], topMargin, false));
                        }
                    } else if (Float.compare(ratio, 1.0f) == 0) {
                        this.mGet.setTextureLayoutParams(size[0], size[1], -1);
                        if (this.mStickerManager != null) {
                            this.mStickerManager.setMarginLayoutParamsForGLView(this.mGet.calStartMargin(size[0], size[1], -1, false));
                        }
                    }
                } else if (getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
                    this.mGet.setTextureLayoutParams(lcdSize[0] / 2, lcdSize[1], 0);
                    setAnimationLayout(1);
                } else {
                    this.mGet.setTextureLayoutParams(size[0], size[1], -1);
                    if (this.mStickerManager != null) {
                        this.mStickerManager.setMarginLayoutParamsForGLView(this.mGet.calStartMargin(size[0], size[1], 0, false));
                    }
                }
            }
            this.mIndicatorManager.changeInidicatorLayout();
            this.mGet.showTilePreview(isTilePreviewOn);
            this.mGet.showTilePreviewCoverView(getBurstProgress());
            setAnimationLayout(3);
            this.mQuickButtonManager.setLayoutForTilePreview();
            this.mAdvancedFilmManager.changePreviewSize(listPref.getExtraInfo(2), false);
        }
    }

    protected String getPreviewSize(String previewSize, String screenSize, String videoSize) {
        if (FunctionProperties.getSupportedHal() != 2 && FunctionProperties.isSupportedSticker() && isStickerSupportedCameraMode() && this.mStickerManager != null && this.mStickerManager.hasSticker()) {
            return this.mStickerManager.getSupportedPreviewSize();
        }
        return super.getPreviewSize(previewSize, screenSize, videoSize);
    }

    protected void changeToDefaultPictureAndVideoSize(boolean isTilePreviewOn, boolean doPreviewSizeChange) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] changeToDefaultPictureAndVideoSize isTilePreviewOn : " + isTilePreviewOn + "  doPreviewSizeChange : " + doPreviewSizeChange);
        if (isTilePreviewOn && isAvailableTilePreview()) {
            if (RatioCalcUtil.getRatio(getSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId))) == 2.0f) {
                ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
                String pictureSize = listPref.getDefaultValue();
                if (RatioCalcUtil.getRatio(pictureSize) == 2.0f) {
                    pictureSize = listPref.getEntryValues()[0].toString();
                }
                if (doPreviewSizeChange) {
                    childSettingMenuClicked(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), pictureSize, -1);
                } else {
                    this.mGet.setSetting(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), pictureSize, false);
                }
                if (isAvailableTilePreview()) {
                    showToast(this.mGet.getActivity().getString(C0088R.string.change_to_default_photo_size_desc), CameraConstants.TOAST_LENGTH_SHORT);
                }
            }
            this.mGet.refreshSetting();
        }
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        boolean useAnim;
        switch (msg.what) {
            case 17:
                if (this.mFocusManager != null && isFocusEnableCondition() && checkModuleValidate(15)) {
                    boolean checkFocusState = msg.arg1 == 1;
                    this.mFocusManager.releaseHandlerBeforeTakePicture();
                    this.mFocusManager.registerCallback(checkFocusState);
                }
                return true;
            case 18:
                if (this.mFocusManager != null) {
                    this.mFocusManager.hideAndCancelAllFocus(false, true, true);
                }
                return true;
            case 30:
                useAnim = !isMenuShowing(PointerIconCompat.TYPE_ZOOM_OUT);
                onShowMenu(1);
                boolean direct = msg.arg1 == 1;
                if (!(direct || useAnim)) {
                    direct = true;
                }
                this.mGet.showSettingMenu(direct);
                if (this.mFocusManager != null) {
                    this.mFocusManager.hideFocus(true);
                }
                if (this.mStickerManager != null) {
                    this.mStickerManager.hideGuideText();
                    this.mStickerManager.hideActionText();
                }
                return true;
            case 31:
                if (this.mDotIndicatorManager != null) {
                    this.mDotIndicatorManager.updateIndicatorPosition(3);
                }
                if (isSettingMenuVisible()) {
                    hideMenu(4, false, true);
                    showCommandArearUI(false);
                }
                return true;
            case 32:
                this.mGet.removeSettingMenu(msg.arg1 == 1, false);
                this.mAdvancedFilmManager.checkAndStopFilmEngine();
                if (this.mStickerManager != null && this.mStickerManager.isStickerDrawing()) {
                    this.mStickerManager.showGuideTextIfNeed();
                }
                return true;
            case 33:
                if (FunctionProperties.isSupportedCameraRoll()) {
                    setTilePreviewLayout("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)));
                }
                if (this.mDotIndicatorManager != null) {
                    this.mDotIndicatorManager.updateIndicatorPosition(2);
                }
                return true;
            case 34:
                useAnim = !isMenuShowing(PointerIconCompat.TYPE_ZOOM_OUT);
                onShowMenu(2);
                this.mGet.showModeMenu(useAnim);
                if (this.mFocusManager != null) {
                    this.mFocusManager.hideFocus(true);
                }
                if (this.mStickerManager != null) {
                    this.mStickerManager.hideGuideText();
                    this.mStickerManager.hideActionText();
                }
                return true;
            case 36:
                this.mGet.hideModeMenu(true, false);
                this.mAdvancedFilmManager.checkAndStopFilmEngine();
                if (this.mStickerManager != null && this.mStickerManager.isStickerDrawing()) {
                    this.mStickerManager.showGuideTextIfNeed();
                }
                return true;
            case 37:
                return true;
            case 38:
                if (!checkModuleValidate(192)) {
                    return true;
                }
                if (this.mHandler != null) {
                    this.mHandler.removeMessages(17);
                    this.mHandler.removeMessages(18);
                    Message focusMsg = this.mHandler.obtainMessage(17);
                    focusMsg.arg1 = 1;
                    this.mHandler.sendMessageDelayed(focusMsg, 700);
                }
                return true;
            case 39:
                if (this.mInitGuideManager != null) {
                    this.mInitGuideManager.showInitialGuide(msg.arg1);
                }
                return true;
            case 48:
                useAnim = !isMenuShowing(CameraConstants.MENU_TYPE_ALL);
                onShowMenu(msg.what);
                if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
                    setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
                    setTilePreviewLayout(false);
                }
                this.mGet.showHelpList(useAnim);
                if (this.mFocusManager != null) {
                    this.mFocusManager.hideFocus(true);
                }
                setFingerDetectionListener(false);
                return true;
            case 49:
                this.mGet.hideHelpList(true, false);
                this.mAdvancedFilmManager.checkAndStopFilmEngine();
                setFingerDetectionListener(true);
                if (this.mStickerManager != null && this.mStickerManager.isStickerDrawing()) {
                    this.mStickerManager.showGuideTextIfNeed();
                }
                return true;
            case 54:
                onAudioZoomButtonClicked(54);
                return true;
            case 55:
                onAudioZoomButtonClicked(55);
                return true;
            case 56:
                break;
            case 57:
                if (isSupportedQuickClip()) {
                    this.mQuickClipManager.setMiniView();
                    break;
                }
                break;
            case 58:
                onCAFButtonClicked();
                return true;
            case 59:
                this.mGet.getActivity().finish();
                return true;
            case 60:
                if (this.mLocationServiceManager != null) {
                    this.mLocationServiceManager.setRecordLocation(true);
                    this.mLocationServiceManager.startReceivingLocationUpdates();
                }
                return true;
            case 61:
                onChangePictureSize();
                setOneShotPreviewCallback();
                return true;
            case 70:
                changeMode(msg.arg1);
                return true;
            case 75:
            case 76:
                updateSteadyCamParam(msg.what);
                return true;
            case 80:
                updateMultiviewShotType("on");
                return true;
            case 81:
                updateMultiviewShotType("off");
                return true;
            case 83:
                if (this.mStickerManager != null && (this.mStickerManager.isStickerDrawing() || this.mExtraPrevewUIManager.getLastSelectedMenu() == 1)) {
                    this.mExtraPrevewUIManager.show(false, true, true);
                    this.mExtraPrevewUIManager.setStrengthBarVisibility(false, false);
                    onShowMenu(16);
                    setPreviewCallbackAll(true);
                    this.mStickerManager.start(getGLViewMargin());
                    showExtraPreviewUI(true, true, true, true);
                    this.mExtraPrevewUIManager.changeButtonState(1, true);
                    if (getBinningEnabledState()) {
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_DEFAULT_STR);
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_BINNING_PARAM, "normal");
                        this.mParamUpdater.setParamValue("picture-size", getCurrentSelectedPictureSize());
                    }
                    setNightVisionDataCallback(false);
                    if (!this.mStickerManager.isStickerDrawing()) {
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, this.mStickerManager.getSupportedPreviewSize());
                        startPreview(null, null, false);
                        this.mStickerManager.waitOneShot();
                        setCameraState(1);
                    }
                    this.mGet.postOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            BaseModule.this.mBinningManager.setBinningIconVisibility(false, true, false, 0);
                            BaseModule.this.settingForSticker(true);
                        }
                    }, 50);
                    if (this.mFocusManager != null) {
                        if (isFocusLock() || isAELock()) {
                            this.mFocusManager.resetAEAFFocus();
                        } else {
                            this.mFocusManager.cancelTouchAutoFocus();
                        }
                    }
                    return true;
                } else if (this.mHandler == null || this.mAdvancedFilmManager == null || this.mSnapShotChecker == null || this.mIsGoingToPostview || this.mQuickButtonManager == null) {
                    setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
                    return false;
                } else if (!checkModuleValidate(15)) {
                    CamLog.m7i(CameraConstants.TAG, "[Film] Can not open film menu");
                    setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
                    return false;
                } else if (getFilmState() == 1) {
                    setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
                    return false;
                } else {
                    if (this.mFocusManager != null) {
                        if (isFocusLock() || isAELock()) {
                            this.mFocusManager.resetAEAFFocus();
                        } else {
                            this.mFocusManager.cancelTouchAutoFocus();
                        }
                    }
                    final int aniType = (isMenuShowing(CameraConstants.MENU_TYPE_ALL) || msg.arg1 == 1) ? 1 : 3;
                    restoreSettingValue(Setting.KEY_FILM_EMULATOR);
                    String value = getSettingValue(Setting.KEY_FILM_EMULATOR);
                    int delay = 0;
                    if (!this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                        if (this.mSnapShotChecker.isIdle()) {
                            this.mAdvancedFilmManager.runFilmEmulator(value);
                            if (ModelProperties.getAppTier() < 5) {
                                this.mAdvancedFilmManager.setReadyToOpenFilterMenu(true);
                            }
                            this.mGet.setSetting(Setting.KEY_FILM_EMULATOR, value, true);
                            delay = 300;
                            this.mQuickButtonManager.setEnable(100, false);
                        } else {
                            CamLog.m7i(CameraConstants.TAG, "[Film] snapshot state is not idle, ruturn");
                            setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
                            return false;
                        }
                    }
                    postOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            if (BaseModule.this.mAdvancedFilmManager == null || BaseModule.this.mSnapShotChecker == null) {
                                BaseModule.this.setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
                                if (BaseModule.this.mQuickButtonManager != null) {
                                    BaseModule.this.mQuickButtonManager.refreshButtonEnable(100, true, true);
                                }
                            } else if (BaseModule.this.mSnapShotChecker.isIdle() && BaseModule.this.onShowMenu(4)) {
                                boolean setFilter;
                                boolean z;
                                BaseModule.this.mGet.setCameraChanging(2);
                                if (CameraConstants.FILM_NONE.equals(BaseModule.this.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                                    setFilter = false;
                                } else {
                                    setFilter = true;
                                }
                                BaseModule.this.mExtraPrevewUIManager.show(false, true, true);
                                BaseModule.this.mExtraPrevewUIManager.setStrengthBarVisibility(setFilter, false);
                                BaseModule.this.mExtraPrevewUIManager.changeButtonState(0, true);
                                AdvancedSelfieManager advancedSelfieManager = BaseModule.this.mAdvancedFilmManager;
                                int i = aniType;
                                if (aniType != 1) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                advancedSelfieManager.showFilmMenu(true, i, z, false, 0, true);
                                BaseModule.this.setFingerDetectionListener(false);
                            } else {
                                BaseModule.this.setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
                            }
                        }
                    }, (long) delay);
                    return true;
                }
            case 84:
                if (this.mHandler == null || this.mAdvancedFilmManager == null || this.mSnapShotChecker == null || this.mQuickButtonManager == null) {
                    return false;
                }
                if (!this.mSnapShotChecker.isIdle()) {
                    CamLog.m7i(CameraConstants.TAG, "snapshot state is not idle, ruturn");
                    return false;
                } else if (this.mStickerManager == null || !this.mStickerManager.isStickerGridVisible()) {
                    if (onHideMenu(4)) {
                        if (this.mHandler != null) {
                            this.mHandler.removeMessages(83);
                        }
                        int animType = 3;
                        if (msg.arg1 != 0) {
                            animType = msg.arg1;
                        }
                        if (!(this.mAdvancedFilmManager.getCurrentFilmIndex() != 0 || CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()))) {
                            this.mQuickButtonManager.setEnable(100, false);
                            this.mGet.setCameraChanging(2);
                        }
                        if (msg.arg2 == -5000) {
                            this.mAdvancedFilmManager.showFilmMenu(false, animType, true, false, 0, true);
                            this.mGet.setUspVisibility(8);
                        } else {
                            this.mAdvancedFilmManager.showFilmMenu(false, animType, true, true, 0, true);
                        }
                        showExtraPreviewUI(false, false, true, isRearCamera());
                        this.mExtraPrevewUIManager.changeButtonState(0, false);
                        setFingerDetectionListener(true);
                    }
                    return true;
                } else {
                    if (this.mStickerManager.isStickerDrawing() || (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator())) {
                        hideStickerMenu(false);
                    } else {
                        hideStickerMenu(true);
                    }
                    return true;
                }
            case 97:
                if (this.mInitGuideManager != null) {
                    this.mInitGuideManager.removeInitialHelpLayout(false);
                    this.mInitGuideManager.showInitDialog();
                }
                return true;
            case 100:
                if (this.mColorEffectManager != null && onShowMenu(32)) {
                    this.mColorEffectManager.showMenu(!isMenuShowing(991));
                    this.mExtraPrevewUIManager.show(false, true, true);
                    this.mExtraPrevewUIManager.changeButtonState(0, true);
                }
                return true;
            case 101:
                if (this.mColorEffectManager != null) {
                    this.mColorEffectManager.hideMenu(true);
                    onHideMenu(32);
                    showExtraPreviewUI(false, true, true, isRearCamera());
                    this.mExtraPrevewUIManager.changeButtonState(0, false);
                }
                return true;
            default:
                return super.mainHandlerHandleMessage(msg);
        }
        updateLightFrameParam(msg.what);
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.setMiniView();
        }
        return true;
    }

    protected void settingForSticker(boolean isStickerOn) {
        CamLog.m7i(CameraConstants.TAG, "settingForSticker isStickerOn : " + isStickerOn + " / CameraDevice : " + this.mCameraDevice);
        CameraParameters parameters = null;
        if (this.mCameraDevice != null) {
            parameters = this.mCameraDevice.getParameters();
        }
        String flashSetting = isRearCamera() ? "flash-mode" : Setting.KEY_LIGHTFRAME;
        if (isStickerOn) {
            if (this.mCameraDevice != null) {
                parameters.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, "24000,24000");
                this.mCameraDevice.setParameters(parameters);
            }
            setSpecificSettingValueAndDisable(flashSetting, "off", false);
            setSpecificSettingValueAndDisable("hdr-mode", "0", false);
            setHDRMetaDataCallback(null);
            setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
            setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
            setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
            setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_RECORDSIZE, getSettingValue(Setting.KEY_VIDEO_RECORDSIZE), false);
            setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_RECORDSIZE_SUB, getSettingValue(Setting.KEY_VIDEO_RECORDSIZE_SUB), false);
            if (!isRearCamera()) {
                setSpecificSettingValueAndDisable(Setting.KEY_SAVE_DIRECTION, "off", false);
            }
            updateFlashParam(parameters, 50, false);
            setNightVisionDataCallback(false);
            onStopQRCodeClicked();
            if (this.mStickerManager != null) {
                this.mExtraPrevewUIManager.setEditDim(this.mStickerManager.isEditDimStatus());
            }
        } else {
            if (this.mCameraDevice != null) {
                parameters.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, MultimediaProperties.getCameraFPSRange(isRearCamera()));
                this.mCameraDevice.setParameters(parameters);
            }
            restoreSettingValue("hdr-mode");
            setSettingMenuEnable("hdr-mode", true);
            String hdrValue = getSettingValue("hdr-mode");
            if ("2".equals(hdrValue)) {
                setHDRMetaDataCallback(hdrValue);
            }
            restoreSettingValue(Setting.KEY_QR);
            setSettingMenuEnable(Setting.KEY_QR, true);
            restoreSettingValue(Setting.KEY_VIDEO_STEADY);
            setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, true);
            restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE);
            setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, true);
            restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE_SUB);
            if (!isRearCamera()) {
                setSettingMenuEnable(Setting.KEY_SAVE_DIRECTION, true);
                restoreSettingValue(Setting.KEY_SAVE_DIRECTION);
            }
            if (isAvailableSteadyCam()) {
                setSettingMenuEnable(Setting.KEY_VIDEO_STEADY, true);
                this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, getSettingValue(Setting.KEY_VIDEO_STEADY));
            }
            if (Setting.KEY_LIGHTFRAME.equals(flashSetting)) {
                restoreSettingValue(flashSetting);
                setSettingMenuEnable(flashSetting, true);
            } else if (!"1".equals(this.mGet.getCurSettingValue("hdr-mode"))) {
                restoreFlashSetting();
                updateFlashParam(parameters, ParamUtils.getFlashMode(this.mGet.getCurSettingValue(flashSetting)), true);
                setSettingMenuEnable(flashSetting, true);
            }
            if (FunctionProperties.isSupportedQrCode(getAppContext()) && isRearCamera() && checkModuleValidate(192) && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR))) {
                onStartQRCodeClicked();
            }
            this.mExtraPrevewUIManager.setEditDim(false);
            restoreBinningSetting();
            setNightVisionDataCallback(true);
        }
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                BaseModule.this.mQuickButtonManager.updateButton(C0088R.id.quick_button_light_frame);
                BaseModule.this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
            }
        });
    }

    protected void restoreFlashSetting() {
    }

    protected int getGLViewMargin() {
        if (!FunctionProperties.isSupportedCameraRoll() || !this.mGet.checkModuleValidate(207) || !isAvailableTilePreview() || isAttachIntent() || isVideoAttachMode() || this.mGet.isAnimationShowing() || isTimerShotCountdown() || isActivatedQuickview() || isModuleChanging() || this.mGet.isPaused() || isRecordingAnimShowing()) {
            CamLog.m7i(CameraConstants.TAG, "return");
            return 0;
        }
        ListPreference listPref;
        if (isRecordingPriorityMode() || CameraConstants.MODE_MANUAL_VIDEO.equals(getShotMode())) {
            listPref = this.mGet.getListPreference(getVideoSizeSettingKey());
        } else {
            listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        }
        int[] size = null;
        int leftMargin = 0;
        if (listPref != null) {
            size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
        }
        if (size == null) {
            CamLog.m5e(CameraConstants.TAG, "size is null");
            return 0;
        }
        float ratio = ((float) size[0]) / ((float) size[1]);
        int[] lcd_size = Utils.getLCDsize(this.mGet.getAppContext(), true);
        if (ModelProperties.isLongLCDModel()) {
            leftMargin = RatioCalcUtil.getLongLCDModelTopMargin(this.mGet.getAppContext(), size[0], size[1], 0);
        } else if (ratio < 1.5f && ratio > 1.1f) {
            leftMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
        } else if (ratio < 1.1f && ratio > 0.9f) {
            leftMargin = (lcd_size[0] - size[0]) / 2;
        }
        leftMargin = RatioCalcUtil.getTilePreviewMargin(getAppContext(), "on".equals(getSettingValue(Setting.KEY_TILE_PREVIEW)), ratio, leftMargin);
        CamLog.m3d(CameraConstants.TAG, "leftMargin : " + leftMargin);
        return leftMargin;
    }

    public boolean isMenuShowing(int menuType) {
        if ((menuType & 1) != 0 && this.mGet.isSettingMenuVisible()) {
            return true;
        }
        if ((menuType & 2) != 0 && this.mGet.isModeMenuVisible()) {
            return true;
        }
        if ((menuType & 4) != 0 && this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isShowingFilmMenu()) {
            return true;
        }
        if ((menuType & 8) != 0 && this.mGet.isHelpListVisible()) {
            return true;
        }
        if ((menuType & 32) != 0 && isColorEffectMenuVisible()) {
            return true;
        }
        if ((menuType & 64) != 0 && isSquareInitGuideShowing()) {
            return true;
        }
        if ((menuType & 128) != 0 && this.mInitGuideManager != null && this.mInitGuideManager.isInitialHelpGuideVisible()) {
            return true;
        }
        if ((menuType & 256) != 0 && isCinemaInitGuideShowing()) {
            return true;
        }
        if ((menuType & 512) != 0 && isAICamInitGuideShowing()) {
            return true;
        }
        if ((menuType & 16) == 0 || this.mStickerManager == null || !this.mStickerManager.isStickerGridVisible()) {
            return false;
        }
        return true;
    }

    public boolean isColorEffectMenuVisible() {
        if (this.mColorEffectManager != null) {
            return this.mColorEffectManager.isMenuVisible();
        }
        return false;
    }

    public boolean isSquareInitGuideShowing() {
        return false;
    }

    public boolean isCinemaInitGuideShowing() {
        return false;
    }

    public boolean isAICamInitGuideShowing() {
        return false;
    }

    private boolean isAvailableHideMenu(int removeMenuTypes, int menuType) {
        return (removeMenuTypes & menuType) != 0 && isMenuShowing(menuType);
    }

    protected void hideMenu(int checkMenuTypes, boolean useAnim, boolean onlyHideMenu) {
        boolean z = true;
        if (isAvailableHideMenu(checkMenuTypes, 1)) {
            this.mGet.removeSettingMenu(!useAnim, onlyHideMenu);
            showCommandArearUI(true);
        }
        if (isAvailableHideMenu(checkMenuTypes, 2)) {
            this.mGet.hideModeMenu(useAnim, onlyHideMenu);
        }
        if (isAvailableHideMenu(checkMenuTypes, 8)) {
            this.mGet.hideHelpList(useAnim, onlyHideMenu);
        }
        if (isAvailableHideMenu(checkMenuTypes, 4)) {
            if (this.mAdvancedFilmManager != null) {
                this.mAdvancedFilmManager.showFilmMenu(false, useAnim ? 3 : 1, true, false, 0, true);
            }
            this.mExtraPrevewUIManager.changeButtonState(0, false);
            showExtraPreviewUI(false, false, true, true);
            if (!onlyHideMenu) {
                onHideMenu(4);
            }
        }
        if (isAvailableHideMenu(checkMenuTypes, 32)) {
            if (this.mColorEffectManager != null) {
                this.mColorEffectManager.hideMenu(useAnim);
            }
            showExtraPreviewUI(false, false, true, true);
            this.mExtraPrevewUIManager.changeButtonState(0, false);
            if (!onlyHideMenu) {
                onHideMenu(32);
            }
        }
        if (!isAvailableHideMenu(checkMenuTypes, 16)) {
            return;
        }
        if (this.mStickerManager.isStickerDrawing()) {
            hideStickerMenu(false);
            return;
        }
        if (this.mStickerManager.hideMenuOnTakePictureBefore()) {
            z = false;
        }
        hideStickerMenu(z);
        this.mExtraPrevewUIManager.changeButtonState(0, false);
    }

    public boolean onShowMenu(int menuType) {
        if (this.mHandler == null || this.mDoubleCameraManager == null || this.mFocusManager == null || this.mIndicatorManager == null || this.mBinningManager == null || this.mAdvancedFilmManager == null) {
            return false;
        }
        checkPictureCallbackRunnable(true);
        if ((!checkModuleValidate(15) && !this.mGet.checkCameraChanging(2)) || isMenuShowing(menuType)) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "onShowMenu, menu type : " + menuType);
        hideMenu((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL, false, true);
        if (!(menuType == 4 || menuType == 32 || menuType == 16)) {
            showExtraPreviewUI(false, false, true, true);
        }
        showDoubleCamera(false);
        hideZoomBar();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterZoomArrowVisibility(4);
        }
        this.mGet.setUspVisibility(8);
        onShowSpecificMenu(menuType);
        this.mHandler.removeMessages(17);
        this.mHandler.removeMessages(18);
        if (this.mGifManager != null && this.mGifManager.getGifVisibleStatus()) {
            this.mGifManager.setGifVisibility(false);
        }
        setQuickClipIcon(true, false);
        if (!(getFocusState() == 14 || isFocusLock() || isAELock())) {
            this.mHandler.sendEmptyMessage(18);
        }
        if (isFocusLock() || isAELock()) {
            this.mFocusManager.hideAEAFText();
            this.mFocusManager.hideFocus();
        } else {
            this.mFocusManager.registerEVCallback(true, false);
        }
        if (isManualFocusModeEx()) {
            this.mFocusManager.hideFocusForce();
            this.mManualFocusManager.setVisible(false);
        }
        setFilmStrengthButtonVisibility(false, false);
        if (menuType == 4 || menuType == 16) {
            setBatteryIndicatorVisibility(true);
            setTimerIndicatorVisibility(true);
        } else {
            this.mIndicatorManager.hideAllIndicator();
        }
        showFrameGridView("off", false);
        handleBinningIconUI(false, 2);
        if (FunctionProperties.isLivePhotoSupported() && this.mLivePhotoManager != null) {
            if (menuType == 4) {
                this.mLivePhotoManager.disableLivePhoto();
            } else if ((menuType == 1 || menuType == 2) && FunctionProperties.isLivePhotoSupported() && "on".equals(getSettingValue(Setting.KEY_LIVE_PHOTO)) && !isRecordingState()) {
                this.mLivePhotoManager.enableLivePhoto();
            }
        }
        return true;
    }

    protected void onShowSpecificMenu(int menuType) {
        switch (menuType) {
            case 1:
                if (FunctionProperties.isSignatureSupported(getAppContext())) {
                    this.mGet.initSignatureContent();
                    this.mGet.updateGuideTextSettingMenu(Setting.KEY_SIGNATURE, this.mGet.getSignatureText() == null ? "" : this.mGet.getSignatureText());
                }
                setQRLayoutVisibility(8);
                return;
            case 2:
                showCommandArearUI(true);
                setQRLayoutVisibility(8);
                return;
            case 4:
            case 32:
                setQRLayoutVisibility(8);
                return;
            default:
                return;
        }
    }

    public boolean onHideMenu(int menuType) {
        if (this.mHandler == null || this.mTimerManager == null || this.mDoubleCameraManager == null || this.mQuickClipManager == null || this.mFocusManager == null || this.mBinningManager == null || this.mAdvancedFilmManager == null || !checkModuleValidate(223)) {
            return false;
        }
        if (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress()) {
            setQuickClipIcon(false, false);
            if (isFocusLock() || (isAELock() && !isLightFrameOn())) {
                this.mFocusManager.showAEAFText();
                this.mFocusManager.showFocus();
                this.mFocusManager.showAEControlBar(false);
            }
            setFilmStrengthButtonVisibility(false, false);
            if (!isManualFocusModeEx()) {
                return false;
            }
            this.mFocusManager.showFocus();
            this.mFocusManager.setManualFocusButtonVisibility(true);
            this.mManualFocusManager.setVisible(true);
            return false;
        }
        if (!isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL)) {
            showExtraPreviewUI(true, false, false, !isRearCamera());
            onHideSpecificMenu(menuType);
            if (this.mCaptureButtonManager != null) {
                this.mCaptureButtonManager.setShutterZoomArrowVisibility(0);
            }
            setQuickClipIcon(false, false);
            showDoubleCamera(true);
            setDoubleCameraEnable(true);
            showFrameGridView(getGridSettingValue(), true);
            this.mGet.setUspVisibility(0);
            if (this.mGifManager != null && this.mGifManager.getGifVisibleStatus()) {
                this.mGifManager.setGifVisibility(true);
            }
            this.mHandler.removeMessages(17);
            this.mHandler.removeMessages(18);
            if (!(getFocusState() == 14 || isFocusLock() || isAELock())) {
                this.mHandler.sendEmptyMessage(17);
            }
            if (isFocusLock() || (isAELock() && !isLightFrameOn())) {
                this.mFocusManager.showAEAFText();
                this.mFocusManager.showFocus();
                this.mFocusManager.showAEControlBar(false);
            }
            showFlashHDRIndicators(false);
            if (isManualFocusModeEx()) {
                this.mFocusManager.showFocus();
                this.mFocusManager.setManualFocusButtonVisibility(true);
                this.mManualFocusManager.setVisible(true);
            }
            setBatteryIndicatorVisibility(true);
            setTimerIndicatorVisibility(true);
            handleBinningIconUI(true, 0);
            handlePreviewCallback();
            if (FunctionProperties.isLivePhotoSupported() && this.mLivePhotoManager != null && "on".equals(getSettingValue(Setting.KEY_LIVE_PHOTO)) && !isRecordingState()) {
                this.mLivePhotoManager.enableLivePhoto();
            }
        }
        return true;
    }

    protected void onHideSpecificMenu(int menuType) {
        switch (menuType) {
            case 1:
                showCommandArearUI(true);
                return;
            default:
                return;
        }
    }

    protected void showCommandArearUI(boolean show) {
        if (this.mCaptureButtonManager != null && this.mReviewThumbnailManager != null) {
            if (show) {
                this.mCaptureButtonManager.setShutterButtonVisibility(0, getShutterButtonType(), false);
                if (!isAttachIntent()) {
                    this.mReviewThumbnailManager.setThumbnailVisibility(0, false);
                    return;
                }
                return;
            }
            this.mCaptureButtonManager.setShutterButtonVisibility(8, getShutterButtonType(), false);
            if (!isAttachIntent()) {
                this.mReviewThumbnailManager.setThumbnailVisibility(8, false);
            }
        }
    }

    protected void showFlashHDRIndicators(boolean isAnim) {
        if (this.mCurFlash == 3) {
            updateIndicator(3, 0, isAnim);
        }
        if (this.mCurrentHDR == 3) {
            updateIndicator(2, 0, isAnim);
        }
    }

    protected void changeMode(int mode) {
        switch (mode) {
            case 1:
                changeToAutoView();
                return;
            case 2:
                changeToManualCamera();
                return;
            case 3:
                changeToManualVideo();
                return;
            default:
                CamLog.m3d(CameraConstants.TAG, "wrong mode");
                return;
        }
    }

    public void showModeDeleteDialog(ModeItem item) {
        if (this.mDialogManager != null) {
            this.mDialogManager.showModeDeleteDialog(item);
        }
    }

    protected void updateSteadyCamParam(int type) {
    }

    protected void onChangeModuleBefore() {
        CamLog.m3d(CameraConstants.TAG, "##[onChangeModuleBefore]");
        this.mGet.setBeforeMode(getShotMode());
        this.mGet.setModuleChanging(true);
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.backupPreviousButtonStatus();
        }
    }

    protected void onChangeModuleAfter() {
        CamLog.m3d(CameraConstants.TAG, "##[onChangeModuleAfter]");
        this.mGet.setModuleChanging(false);
    }

    protected void onCameraSwitchingStart() {
        CamLog.m7i(CameraConstants.TAG, "-swap- ##[onCameraSwitchingStart]");
        Log.d(CameraConstants.TAG, "TIME CHECK : switchCamera [START]");
    }

    protected void onCameraSwitchingEnd() {
        if (isRearCamera()) {
            showFrameGridView(getGridSettingValue(), false);
        }
        this.mGet.switchModeList();
        Log.d(CameraConstants.TAG, "TIME CHECK : switchCamera [END]");
        CamLog.m3d(CameraConstants.TAG, "##[onCameraSwitchingEnd]");
    }

    protected void onTakePictureBefore() {
        CamLog.m3d(CameraConstants.TAG, "##[onTakePictureBefore]");
        this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
    }

    protected void onTakePictureAfter() {
        CamLog.m3d(CameraConstants.TAG, "##[onTakePictureAfter]");
    }

    protected void doTakePicture() {
        CamLog.m3d(CameraConstants.TAG, "##[doTakePicture]");
    }

    public void onVideoStopClickedBefore() {
        CamLog.m3d(CameraConstants.TAG, "##[onVideoStopClickedBefore]");
    }

    protected void onChangePictureSize() {
        if (isNeedRestartByPictureSizeChanged() && !CameraConstants.MODE_POPOUT_CAMERA.equals(getShotMode())) {
            setupPreview(null);
        }
        ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        if (listPref != null) {
            if (this.mAdvancedFilmManager != null) {
                this.mAdvancedFilmManager.changePreviewSize(listPref.getExtraInfo(2), false);
            }
            if (this.mLivePhotoManager != null) {
                this.mLivePhotoManager.changePreviewSize(listPref.getExtraInfo(2));
            }
        }
        if (this.mFocusManager != null) {
            if (isFocusLock() || isAELock()) {
                this.mFocusManager.resetAEAFFocus();
            }
            this.mFocusManager.changeAEBarBottomMargin();
        }
        if (this.mManualFocusManager != null) {
            this.mManualFocusManager.rotateDrumLayout(270, this.mGet.getOrientationDegree());
        }
        if (this.mStickerManager != null) {
            this.mStickerManager.setLayoutParamsForGLView();
        }
    }

    public boolean isStorageRemoved(int storageType) {
        return this.mStorageManager.isStorageRemoved(storageType);
    }

    public void hideModeMenu(boolean showAnimation, boolean onlyHideMenu) {
        this.mGet.hideModeMenu(showAnimation, onlyHideMenu);
    }

    public void hideHelpList(boolean showAnimation, boolean onlyHideMenu) {
        this.mGet.hideHelpList(showAnimation, onlyHideMenu);
    }

    public boolean isQuickViewAniStarted() {
        if (this.mReviewThumbnailManager == null) {
            return false;
        }
        return this.mReviewThumbnailManager.isQuickViewAniStarted();
    }

    public boolean checkCurrentConeMode(int checkType) {
        if (this.mGet.getCurrentConeMode() == checkType) {
            return true;
        }
        return false;
    }

    public boolean isActivatedQuickview() {
        if (this.mReviewThumbnailManager == null) {
            return false;
        }
        if (this.mReviewThumbnailManager.isActivatedSelfieQuickView() || this.mReviewThumbnailManager.isQuickViewAniStarted()) {
            return true;
        }
        return false;
    }

    protected void doOnQuickViewShown(boolean isAutoReview) {
        hideMenu(CameraConstants.MENU_TYPE_ALL, false, false);
        super.doOnQuickViewShown(isAutoReview);
        hideZoomBar();
        handleBinningIconUI(false, 2);
        if (isJogZoomMoving()) {
            stopShutterZoom();
        }
    }

    protected void changeRequester() {
        boolean z = true;
        super.changeRequester();
        if (this.mParamUpdater != null) {
            changeRequesterForZoom();
            if (FunctionProperties.isSupportedInAndOutZoom() && !FunctionProperties.isSupportedOpticZoom()) {
                String value = "0";
                if (this.mZoomManager != null && this.mZoomManager.isInAndOutSwithing()) {
                    value = this.mCameraId == 0 ? "3" : "2";
                }
                if (this.mCameraReOpenAfterInAndOutZooom) {
                    value = "1";
                }
                this.mParamUpdater.addRequester(ParamConstants.KEY_CAMERA_SWITCHING, value, false, true);
            }
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIEW_MODE, getCurrentViewModeToString());
            restoreBinningSetting();
            if ("off".equals(getSettingValue(Setting.KEY_BINNING))) {
                z = false;
            }
            setNightVisionParameters(z);
        }
    }

    protected void afterCommonRequester() {
        super.afterCommonRequester();
        if (checkAssistantCondition() && SharedPreferenceUtil.getInitialHelpShown(getAppContext())) {
            String pictureSizeSpecified = this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_PICTURE_SIZE, null);
            if (pictureSizeSpecified != null) {
                ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
                if (listPref != null) {
                    CharSequence[] entryValues = listPref.getEntryValues();
                    float[] specifiedSize = Utils.sizeStringToFloatArray(pictureSizeSpecified);
                    float specifiedRatio = specifiedSize[1] / specifiedSize[0];
                    for (int i = 0; i < entryValues.length; i++) {
                        int[] size = Utils.sizeStringToArray(entryValues[i].toString());
                        if (Math.abs((((float) size[1]) / ((float) size[0])) - specifiedRatio) < 0.001f) {
                            CamLog.m3d(CameraConstants.TAG, "-Voice Assistant- change picture size : " + entryValues[i].toString());
                            String newPictureSize = entryValues[i].toString();
                            setSetting(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), newPictureSize, true);
                            this.mParamUpdater.setParamValue(getPictureSizeParamKey(0), newPictureSize);
                            int index = listPref.findIndexOfValue(newPictureSize);
                            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, listPref.getExtraInfo(1));
                            ListPreference otherListPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId == 0 ? 2 : 0));
                            if (otherListPref != null) {
                                otherListPref.setValue(otherListPref.findValueOfIndex(index));
                                if (isOpticZoomSupported(null)) {
                                    this.mParamUpdater.setParamValue(getPictureSizeParamKey(1), otherListPref.getValue());
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                    }
                    return;
                }
                return;
            }
            return;
        }
        this.mGet.setAssistantFlag(CameraConstantsEx.FLAG_PICTURE_SIZE, null);
    }

    public void changeRequesterForZoom() {
        if (this.mZoomManager != null) {
            int initZoomValue = 0;
            boolean bSet = true;
            if (isSupportedCropAngle()) {
                initZoomValue = this.mZoomManager.getCropAngleZoomValue();
            } else if (this.mGet.isOpticZoomSupported(null)) {
                initZoomValue = this.mZoomManager.getZoomValue();
            } else if (!isZoomAvailable() || isSlowMotionMode()) {
                bSet = false;
            } else {
                initZoomValue = this.mZoomManager.getZoomValue();
            }
            if (bSet) {
                this.mParamUpdater.addRequester("zoom", Integer.toString(initZoomValue), false, true);
                this.mParamUpdater.addRequester(ParamConstants.KEY_JOG_ZOOM, Integer.toString(0), false, true);
            }
            if (!this.mGet.isOpticZoomSupported(null)) {
                return;
            }
            if (this.mCameraId == 0) {
                this.mParamUpdater.addRequester(ParamConstants.KEY_OPTIC_CAM_INIT, ParamConstants.VALUE_OPITC_CAM_INIT_TELE, false, true);
            } else if (this.mCameraId == 2) {
                this.mParamUpdater.addRequester(ParamConstants.KEY_OPTIC_CAM_INIT, ParamConstants.VALUE_OPITC_CAM_INIT_WIDE, false, true);
            }
        }
    }

    public void onNormalAngleButtonClicked() {
        if (checkDoubleCameraButtonClickCondition()) {
            CamLog.m3d(CameraConstants.TAG, "onNormalAngleButtonClicked() : " + getCameraId());
            hideMenu(CameraConstants.MENU_TYPE_ALL, false, true);
            hideZoomBar();
            setFilmStrengthButtonVisibility(true, false);
            if (this.mIsAngleButtonTouched || onNormalAngleButtonTouched()) {
                this.mIsAngleButtonTouched = false;
                if (isSupportedCropAngle()) {
                    if (SharedPreferenceUtil.getCropAngleButtonId(getAppContext()) != 0) {
                        SharedPreferenceUtil.saveCropAngleButtonId(getAppContext(), 0);
                        CamLog.m3d(CameraConstants.TAG, "-ani- onNormalAngleButtonClicked type = crop + 4");
                        onCropZoomButtonClicked(0, 4);
                        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_FRONT_CROP, 0, "");
                    }
                } else if (FunctionProperties.getCameraTypeFront() == 1) {
                    if (getCameraId() != 2) {
                        if (isLightFrameOn()) {
                            switchCameraOnFront(6);
                        } else {
                            switchCameraOnFront(2);
                        }
                        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_CHANGE_CAMERA, 0, "");
                    }
                } else if (FunctionProperties.getCameraTypeRear() == 1) {
                    CamLog.m3d(CameraConstants.TAG, "-ani- onNormalAngleButtonClicked type = non crop +2");
                    if (this.mStickerManager != null && this.mStickerManager.isRunning() && getCameraId() == 2) {
                        this.mStickerManager.hideForChange();
                        getCurPreviewBlurredBitmap(202, 360, 13, true);
                    }
                    onRearCameraChangeButtonClicked(0, 2);
                }
            }
        }
    }

    public void onWideAngleButtonClicked() {
        if (checkDoubleCameraButtonClickCondition()) {
            CamLog.m3d(CameraConstants.TAG, "onWideAngleButtonClicked() : " + getCameraId());
            hideMenu(CameraConstants.MENU_TYPE_ALL, false, true);
            hideZoomBar();
            setFilmStrengthButtonVisibility(true, false);
            if (this.mIsAngleButtonTouched || onWideAngleButtonTouched()) {
                this.mIsAngleButtonTouched = false;
                if (isSupportedCropAngle()) {
                    if (SharedPreferenceUtil.getCropAngleButtonId(getAppContext()) != 1) {
                        SharedPreferenceUtil.saveCropAngleButtonId(getAppContext(), 1);
                        CamLog.m3d(CameraConstants.TAG, "-ani- onWideAngleButtonClicked type = crop +5");
                        onCropZoomButtonClicked(1, 5);
                        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_FRONT_CROP, 1, "");
                    }
                } else if (FunctionProperties.getCameraTypeFront() == 1) {
                    if (getCameraId() != 1) {
                        if (isLightFrameOn()) {
                            switchCameraOnFront(7);
                        } else {
                            switchCameraOnFront(3);
                        }
                        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_CHANGE_CAMERA, 1, "");
                    }
                } else if (FunctionProperties.getCameraTypeRear() == 1) {
                    CamLog.m3d(CameraConstants.TAG, "-ani- onWideAngleButtonClicked type = non crop +3");
                    if (this.mStickerManager != null && this.mStickerManager.isRunning() && getCameraId() == 0) {
                        this.mStickerManager.hideForChange();
                        getCurPreviewBlurredBitmap(202, 360, 25, true);
                    }
                    onRearCameraChangeButtonClicked(2, 3);
                }
            }
        }
    }

    public boolean checkDoubleCameraSwitchingAvailable() {
        if (!this.mGet.isModuleChanging() && !this.mGet.isCameraChanging() && !this.mGet.isAnimationShowing() && !isTimerShotCountdown() && getFocusState() != 2 && !this.mIsGoingToPostview && !isPostviewShowing() && !this.mSnapShotChecker.checkMultiShotState(7) && !this.mSnapShotChecker.isSnapShotProcessing() && checkModuleValidate(95) && !this.mIsSwitchingCameraDuringRecording) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "checkModuleValidate() : " + checkModuleValidate(95));
        CamLog.m3d(CameraConstants.TAG, "mIsSwitchingCameraDuringRecording : " + this.mIsSwitchingCameraDuringRecording);
        return false;
    }

    private boolean checkDoubleCameraButtonClickCondition() {
        if (!checkDoubleCameraSwitchingAvailable()) {
            return false;
        }
        if (this.mZoomManager == null || this.mZoomManager.isZoomControllersMoving()) {
            CamLog.m3d(CameraConstants.TAG, "Zoom controller's moving, return");
            return false;
        } else if ((isOpticZoomSupported(null) || isSupportedCropAngle()) && this.mIsPreviewCallbackWaiting) {
            CamLog.m3d(CameraConstants.TAG, "waiting oneshotcallback");
            return false;
        } else if (this.mGet.getPreviewCoverVisibility() == 0) {
            CamLog.m3d(CameraConstants.TAG, "preview cover visibie, return");
            return false;
        } else if (getFilmState() != 1) {
            return true;
        } else {
            return false;
        }
    }

    private void onRearCameraChangeButtonClicked(int cameraId, int animationType) {
        if (this.mCameraId == cameraId) {
            CamLog.m3d(CameraConstants.TAG, "onRearCameraChangeButtonClicked same camera id return");
        } else if (getFilmState() == 1) {
            CamLog.m3d(CameraConstants.TAG, "FilmEngine is releasing. Return");
        } else {
            if (this.mQuickClipManager != null && this.mQuickClipManager.isOpened()) {
                this.mQuickClipManager.drawerClose(true);
            }
            if (!FunctionProperties.isSupportedBinning(cameraId)) {
                this.mBinningManager.setBinningIconVisibility(false, 0);
            }
            if (this.mQRCodeManager != null) {
                this.mQRCodeManager.setQRLayoutVisibility(8);
            }
            int zoomValue = 0;
            if (this.mZoomManager != null) {
                zoomValue = this.mZoomManager.getInitZoomStep(cameraId);
                this.mZoomManager.setZoomValue(zoomValue);
                this.mZoomManager.stopDrawingExceedsLevel();
            }
            this.mParamUpdater.setParamValue("zoom", Integer.toString(zoomValue));
            if (this.mFocusManager != null) {
                this.mFocusManager.hideTrackingAF();
                setFocusState(0);
            }
            if (checkModuleValidate(128)) {
                if (this.mQuickButtonManager != null) {
                    this.mQuickButtonManager.setEnable(100, false);
                }
                this.mGet.movePreviewOutOfWindow(true);
                if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                    this.mAdvancedFilmManager.moveFilmPreviewOutOfWindow(true);
                }
                this.mGet.startCameraSwitchingAnimation(animationType);
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    BaseModule.this.mGet.setPreviewVisibility(0);
                }
            }, 100);
            if (isLivePhotoEnabled()) {
                this.mLivePhotoManager.disableLivePhoto();
            }
            doInAndOutZoom(true, false);
        }
    }

    public boolean isCropZoomStarting() {
        return this.mIsCropAnimationStarted;
    }

    protected int getCropZoomAnimationTime() {
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.getSelfieFilterVisibility() || this.mAdvancedFilmManager.getCurrentFilmIndex() != 0 || CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode())) {
            return 300;
        }
        return 300 * 2;
    }

    protected void onCropZoomButtonClicked(int cropId, int animationType) {
        if (SharedPreferenceUtil.getCropAngleButtonId(getAppContext()) != cropId) {
            CamLog.m3d(CameraConstants.TAG, "onCropZoomButtonClicked same camera id return");
        } else if (getFilmState() == 1) {
            CamLog.m3d(CameraConstants.TAG, "FilmEngine is releasing. Return");
        } else {
            int cropZoomValue;
            if (SharedPreferenceUtil.getCropAngleButtonId(getAppContext()) == 0) {
                cropZoomValue = FunctionProperties.getCropAngleZoomLevel();
            } else {
                cropZoomValue = 0;
            }
            this.mLocalParamForZoom = null;
            setZoomStep(cropZoomValue, false, false, false);
            CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity()).setParamToBackup("zoom", Integer.valueOf(cropZoomValue));
            int animationTime = getCropZoomAnimationTime();
            if (this.mDoubleCameraManager != null) {
                this.mDoubleCameraManager.setButtonsSelected();
            }
            if (this.mAdvancedFilmManager != null) {
                this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
            }
            if (this.mFocusManager != null && (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock())) {
                this.mFocusManager.showAEControlBar(false);
            }
            if (this.mQuickClipManager != null && this.mQuickClipManager.isOpened()) {
                this.mQuickClipManager.drawerClose(true);
            }
            if (checkModuleValidate(192)) {
                setQuickButtonEnable(100, false, false);
                if (this.mDoubleCameraManager != null) {
                    this.mDoubleCameraManager.setDualViewControlEnabled(false);
                }
                this.mGet.movePreviewOutOfWindow(true);
                if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                    this.mAdvancedFilmManager.moveFilmPreviewOutOfWindow(true);
                }
                this.mIsCropAnimationStarted = true;
                this.mGet.startCameraSwitchingAnimation(animationType);
                CamLog.m3d(CameraConstants.TAG, "[crop] after starting Animation");
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        BaseModule.this.cropAnimationEnd();
                        BaseModule.this.mIsCropAnimationStarted = false;
                    }
                }, (long) animationTime);
            }
            if (isLivePhotoEnabled()) {
                this.mLivePhotoManager.disableLivePhoto();
            }
        }
    }

    protected void cropAnimationEnd() {
        boolean isMovePreview;
        CamLog.m3d(CameraConstants.TAG, "[crop] cropAnimationEnd");
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            isMovePreview = true;
        } else {
            isMovePreview = false;
        }
        setPreviewCoverVisibility(4, isMovePreview);
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            this.mAdvancedFilmManager.moveFilmPreviewOutOfWindow(false);
        }
        if (this.mDoubleCameraManager != null) {
            this.mDoubleCameraManager.setDualViewControlEnabled(true);
        }
        setQuickButtonEnable(100, true, true);
        if (FunctionProperties.isLivePhotoSupported() && this.mLivePhotoManager != null && "on".equals(getSettingValue(Setting.KEY_LIVE_PHOTO)) && !isRecordingState() && this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.getFilmState() == 0) {
            this.mLivePhotoManager.enableLivePhoto();
        }
    }

    private boolean checkPreviewCaptureInterval() {
        long captureTime = System.currentTimeMillis();
        if (captureTime - this.mPreviousPreviewCaptureTime < 500) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "captureTime - mPreviousPreviewCaptureTime = " + (captureTime - this.mPreviousPreviewCaptureTime));
        this.mPreviousPreviewCaptureTime = captureTime;
        return true;
    }

    public boolean onNormalAngleButtonTouched() {
        if (this.mIsAngleButtonTouched || this.mGet.getPreviewCoverVisibility() == 0) {
            CamLog.m3d(CameraConstants.TAG, "normal angle button is already touched or preview cover is visible");
            return false;
        } else if (!checkDoubleCameraButtonClickCondition() || !checkPreviewCaptureInterval()) {
            return false;
        } else {
            CamLog.m3d(CameraConstants.TAG, "onNormalAngleButtonTouched");
            if (this.mStickerManager != null && this.mStickerManager.isRunning()) {
                CamLog.m3d(CameraConstants.TAG, "onNormalAngleButtonTouched skip create bitmap");
            } else if (checkModuleValidate(192)) {
                if (isLightFrameOn()) {
                    getCurPreviewBlurredBitmap(202, 360, 25, false);
                } else {
                    getCurPreviewBlurredBitmap(202, 360, 13, true);
                }
            }
            this.mIsAngleButtonTouched = true;
            return true;
        }
    }

    public boolean onWideAngleButtonTouched() {
        if (this.mIsAngleButtonTouched || this.mGet.getPreviewCoverVisibility() == 0) {
            CamLog.m3d(CameraConstants.TAG, "wide angle button is already touched or preview cover is visible");
            return false;
        } else if (!checkDoubleCameraButtonClickCondition() || !checkPreviewCaptureInterval()) {
            return false;
        } else {
            CamLog.m3d(CameraConstants.TAG, "onWideAngleButtonTouched");
            if (this.mStickerManager != null && this.mStickerManager.isRunning()) {
                CamLog.m3d(CameraConstants.TAG, "onWideAngleButtonTouched skip create bitmap");
            } else if (checkModuleValidate(192)) {
                if (isLightFrameOn()) {
                    getCurPreviewBlurredBitmap(202, 360, 25, false);
                } else {
                    getCurPreviewBlurredBitmap(202, 360, 25, true);
                }
            }
            this.mIsAngleButtonTouched = true;
            return true;
        }
    }

    public boolean isManualMode() {
        return false;
    }

    public boolean isRecordingPriorityMode() {
        return false;
    }

    public boolean isManualFocusMode() {
        return this.mFocusManager != null && this.mFocusManager.isManualFocusMode();
    }

    public void showConeViewMode(boolean show) {
        this.mGet.showConeViewMode(show);
    }

    public int getFilmState() {
        if (this.mAdvancedFilmManager != null) {
            return this.mAdvancedFilmManager.getFilmState();
        }
        return 0;
    }

    public boolean isSupportedExternalDeviceModule() {
        return false;
    }

    public void setListenerAfterOneShotCallback() {
        if (setListener()) {
            super.setListenerAfterOneShotCallback();
            this.mIsBindingListner = true;
        }
    }

    private boolean setListener() {
        CamLog.m7i(CameraConstants.TAG, "setListener " + this.mIsBindingListner + " mInitModule : " + this.mInitModule);
        if (this.mIsBindingListner || !this.mInitModule || !setShutterButtonListener(true)) {
            return false;
        }
        setExtraButtonListener(true);
        setQuickClipListListener();
        return true;
    }

    public boolean isFocusTrackingSupported() {
        if ((ModelProperties.isJoanOriginal() || !ModelProperties.isJoanRenewal()) && this.mCameraCapabilities != null) {
            return this.mCameraCapabilities.isFocusTrackingSupported();
        }
        return false;
    }

    protected void restoreTilePreviewSetting() {
        CamLog.m3d(CameraConstants.TAG, "[Tile] restoreTilePreviewSetting");
        if (!isAvailableTilePreview()) {
            setSetting(Setting.KEY_TILE_PREVIEW, "off", false);
            setTilePreviewLayout(false);
            if (isActivatedQuickdetailView()) {
                this.mGet.closeDetailView();
            }
        } else if (ModelProperties.isLguCloudServiceModel() && CameraConstants.STORAGE_NAME_NAS.equals(this.mGet.getCurSettingValue(Setting.KEY_STORAGE))) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] turn off ");
            setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        } else {
            if (RatioCalcUtil.getRatio(getSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId))) < 2.0f) {
                restoreSettingValue(Setting.KEY_TILE_PREVIEW);
            } else {
                setSetting(Setting.KEY_TILE_PREVIEW, "off", false);
            }
            setSettingMenuEnable(Setting.KEY_TILE_PREVIEW, true);
        }
    }

    public long getRecDurationTime() {
        return 0;
    }

    public long getRecDurationTime(String recordingFilePath) {
        return 0;
    }

    protected void resultVideoEditor(int resultCode, Intent data) {
    }

    public void disableCheeseShutterByCallPopup() {
    }

    public void deleteTempImages() {
    }

    public ImageRegisterRequest saveImageDataForImageRegister(SaveRequest sr) {
        return null;
    }

    public Uri insertImageContent(ImageRegisterRequest irr) {
        return null;
    }

    public void onImageSaverQueueStatus(int count) {
    }

    public void doAfterSaveImageSavers(Uri uri, boolean updateThumbnail) {
    }

    protected void onAudioZoomButtonClicked(int state) {
    }

    protected void onCAFButtonClicked() {
    }

    protected void saveImage(byte[] data, byte[] extraExif, boolean needCrop) {
    }

    public Uri getUri() {
        return null;
    }

    protected void launchGallery(Uri uri, int galleryPlayType) {
    }

    public void setLaunchGalleryLocation(float[] loc) {
    }

    public void onReceiveAudioNoiseIntent() {
    }

    public boolean stopBurstShotTaking(boolean isSkip) {
        return false;
    }

    public boolean isUHDmode() {
        return false;
    }

    public boolean isFHD60() {
        return false;
    }

    protected int[] getMaxResolutionWidthHeight() {
        return null;
    }

    public boolean isAvailableToChangeAutoModeByWatch() {
        return false;
    }

    public boolean isNeedToCheckFlashTemperature() {
        return true;
    }

    public boolean setFlashOffByHighTemperature(boolean isSetParam) {
        return false;
    }

    protected void updateManualModeParamAndSetting(boolean setZSLOff) {
    }

    protected void addModuleChildSettingMap(HashMap<String, ChildSettingRunnable> hashMap) {
    }

    public int getShutterButtonType() {
        return 3;
    }

    public void onBTConnectionStateChanged(boolean connect) {
    }

    public void onBTStateChanged(boolean isOn) {
    }

    public void onBTAudioConnectionStateChanged(boolean connect) {
    }

    public void onDualConnectionDeviceTypeChanged(int selectedBtn) {
    }

    public void changeLayoutOnMultiview(String layoutType) {
    }

    public boolean isRequestedSingleImage() {
        return false;
    }

    public void removeUIBeforeModeChange() {
    }

    public void onFrameRateListRefreshed(String previous, String next) {
    }

    public boolean onRecordStartButtonClicked() {
        return false;
    }

    public boolean onCameraShutterButtonClicked() {
        return true;
    }

    protected void locationPermissionGranted() {
    }

    protected void showLocationToast() {
    }

    protected void changeLocalModule() {
    }

    protected void stopSelfieEngin() {
    }

    protected void startSelfieEngine() {
    }

    public void onShowAEBar() {
    }

    public void onHideAEBar() {
    }

    public void onShowBeautyMenu(int type) {
    }

    public void onHideBeautyMenu() {
    }

    public void setBeautyButtonSelected(int type, boolean selected) {
    }

    public void setMenuType(int type) {
    }

    public boolean checkGraphyAppInstalled() {
        return false;
    }

    public void selectMyFilterItem() {
    }

    public void requeryGraphyItems() {
    }

    public void registerEVCallback(boolean isSet, boolean postResetRunnable) {
        if (this.mFocusManager != null) {
            this.mFocusManager.registerEVCallback(isSet, postResetRunnable);
        }
    }

    public boolean isGestureGuideShowing() {
        return false;
    }

    public boolean isMultishotState(int checkType) {
        return this.mSnapShotChecker == null ? false : this.mSnapShotChecker.checkMultiShotState(checkType);
    }

    protected void setQuickClipListListener() {
        this.mQuickClipManager.setQuickClipListListener(new C016748());
    }

    protected void hideFocusOnShowOtherBars(boolean reset) {
        if (this.mFocusManager != null && this.mCameraDevice != null) {
            boolean isAEAFlock;
            if (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock()) {
                isAEAFlock = true;
            } else {
                isAEAFlock = false;
            }
            if (reset) {
                if (isAEAFlock) {
                    CameraDeviceUtils.setEnable3ALocks(this.mCameraDevice, false, false);
                }
                if (isRecordingState()) {
                    this.mFocusManager.hideAndCancelAllFocus(false);
                    setContinuousFocus(this.mCameraDevice.getParameters(), true);
                } else {
                    this.mFocusManager.hideAndCancelAllFocus(false);
                    this.mFocusManager.releaseTouchFocus();
                }
                this.mFocusManager.resetEVValue(0, false);
            } else {
                this.mFocusManager.hideFocus(isAEAFlock);
            }
            if (this.mManualFocusManager != null && isManualFocusModeEx()) {
                setManualFocusButtonVisibility(false);
                setManualFocusVisibility(false);
            }
        }
    }

    protected void showFocusOnHideOtherBars() {
        if (this.mFocusManager != null) {
            if (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock()) {
                this.mFocusManager.showAEControlBar(false);
            }
            if (this.mManualFocusManager != null && isManualFocusModeEx()) {
                setManualFocusButtonVisibility(true);
                setManualFocusVisibility(true);
            }
        }
    }

    public boolean isAEControlBarEnableCondition() {
        if (this.mFocusManager != null) {
            return this.mFocusManager.isAEControlBarEnableCondition();
        }
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
    }

    public int getPostviewType() {
        return -1;
    }

    protected boolean checkFlashIndicatorChangeCondition() {
        boolean modeCheck = "mode_normal".equals(getShotMode()) ? this.mSnapShotChecker.isIdle() || this.mCurFlash == 3 : true;
        if ((modeCheck || isTimerShotCountdown()) && ((this.mCurFlash == 0 || this.mCurFlash == 3) && this.mLastFlash != this.mCurFlash)) {
            return true;
        }
        return false;
    }

    public boolean isManualVideoAudioPopupShowing() {
        return false;
    }

    public void setCaptureButtonEnableByNaviBar(boolean enable) {
        if (this.mCaptureButtonManager != null && this.mBackButtonManager != null) {
            this.mCaptureButtonManager.setButtonDimByNaviBar(enable);
            this.mBackButtonManager.setButtonDimByNaviBar(enable);
        }
    }

    public int getQuickButtonStartMargin() {
        if (this.mQuickButtonManager != null) {
            return this.mQuickButtonManager.getQuickButtonStartMargin();
        }
        return 0;
    }

    public boolean onFingerPrintSensorDown(int keyCode, KeyEvent event) {
        return true;
    }

    protected void onDetectedFinger(int x, int y) {
        if (isSupportedFingerDetection() && this.mFingerDetectionManager != null) {
            this.mFingerDetectionManager.onFingerDetected(x, y);
        }
    }

    public boolean isHALSignatureCaptureMode() {
        return true;
    }

    public boolean isSignatureEnableCondition() {
        if (FunctionProperties.isSignatureSupported(getAppContext()) && "on".equals(getSettingValue(Setting.KEY_SIGNATURE)) && !isRecordingState()) {
            return true;
        }
        return false;
    }

    protected void resultFromCropImage(Intent data) {
    }

    protected void resultFromPickImage(Intent data) {
    }

    protected void resultFromCancel() {
    }

    protected void resetFingerDetection() {
        if (isSupportedFingerDetection() && this.mFingerDetectionManager != null) {
            this.mFingerDetectionManager.reset();
        }
    }

    protected void informPreviewSizeToFingerDetectionManager(String previewSize) {
        if (isSupportedFingerDetection() && this.mFingerDetectionManager != null) {
            this.mFingerDetectionManager.setPreviewSize(previewSize);
        }
    }

    public void setTextGuideVisibilityForEachMode(boolean visible) {
    }

    public boolean isBarVisible(int barType) {
        return this.mBarManager.isBarVisible(barType);
    }

    public boolean getGifVisibleStatus() {
        if (this.mGifManager == null) {
            return false;
        }
        return this.mGifManager.getGifVisibleStatus();
    }

    public void setGifVisibleStatus(boolean state) {
        if (this.mGifManager != null) {
            this.mGifManager.setGifVisibleStatus(state);
        }
    }

    public void setGIFVisibility(boolean visible) {
        if (this.mGifManager != null) {
            this.mGifManager.setGifVisibility(visible);
        }
    }

    public boolean isOpticZoomSupported(String specificMode) {
        return this.mGet.isOpticZoomSupported(specificMode);
    }

    public int getInitZoomStep(int cameraId) {
        return this.mZoomManager == null ? 0 : this.mZoomManager.getInitZoomStep(cameraId);
    }

    public void setQuickClipSharedUri(Uri uri) {
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setSharedUri(uri);
        }
    }

    public void updateQuickClipForTilePreview(boolean isCircleView, boolean isTilePreviewOn, Uri uri) {
        if (isSupportedQuickClip()) {
            setQuickClipIcon(isTilePreviewOn, true);
            this.mQuickClipManager.setMiniView();
            this.mQuickClipManager.setForceStatusForTilePreview(isCircleView, isTilePreviewOn);
            QuickClipManager quickClipManager = this.mQuickClipManager;
            if (!isTilePreviewOn) {
                uri = getUri();
            }
            quickClipManager.setSharedUri(uri);
        }
    }

    protected void updateQuickClipWithEmptyUri(boolean isCircleView, boolean isTilePreviewOn, String contentType) {
        if (isSupportedQuickClip()) {
            setQuickClipIcon(isTilePreviewOn, true);
            this.mQuickClipManager.setMiniView();
            this.mQuickClipManager.setForceStatusForTilePreview(isCircleView, isTilePreviewOn);
            this.mQuickClipManager.setContentType(contentType);
        }
    }

    public void updateThumbnail(boolean useAni) {
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setEnabled(true);
            this.mReviewThumbnailManager.updateThumbnail(useAni);
        }
    }

    public boolean checkCollageContentsShareAvailable() {
        return false;
    }

    public void setCollageContentsSharedFlag() {
        this.mIsCollageShared = false;
    }

    public void updateBackupParameters() {
        if (this.mCameraDevice != null && !isPaused() && this.mZoomManager != null && this.mZoomManager.isReadyZoom() && isZoomAvailable()) {
            CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity()).setParamToBackup("zoom", Integer.valueOf(this.mZoomManager.getZoomValue()));
        }
    }

    public int[] getManualVideoScreenSize() {
        ListPreference listPref = getListPreference(Setting.KEY_MANUAL_VIDEO_SIZE);
        if (listPref == null) {
            return null;
        }
        int[] previewSize = Utils.sizeStringToArray(listPref.getExtraInfo(1));
        int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
        int[] screenSize = new int[2];
        if (((double) previewSize[0]) / ((double) previewSize[1]) > 2.3d) {
            screenSize[0] = lcdSize[0];
            screenSize[1] = (lcdSize[0] * previewSize[1]) / previewSize[0];
            return screenSize;
        }
        screenSize[1] = lcdSize[1];
        screenSize[0] = (lcdSize[1] * previewSize[0]) / previewSize[1];
        return screenSize;
    }

    public boolean checkCameraChanging(int changeType) {
        return this.mGet.checkCameraChanging(changeType);
    }

    public boolean isSpliceViewImporteImage() {
        return false;
    }

    public boolean isGridPostViesShowing() {
        return false;
    }

    public boolean isSquareSnapAccessView() {
        return false;
    }

    public boolean isManualDrumShowing(int type) {
        return false;
    }

    public boolean isSnapShotProcessing() {
        if (this.mSnapShotChecker == null) {
            return false;
        }
        return this.mSnapShotChecker.isSnapShotProcessing();
    }

    public void refreshSetting() {
        this.mGet.refreshSetting();
    }

    public boolean isUspVisible() {
        return this.mGet.isUspVisible();
    }

    public boolean isUspZoneSupportedMode(String shotMode) {
        return this.mGet.isUspZoneSupportedMode(shotMode);
    }

    public void refreshTilePreviewCursor() {
        this.mGet.refreshTilePreviewCursor();
    }

    public boolean isManualFocusModeEx() {
        return this.mManualFocusManager != null && this.mManualFocusManager.getManualFocusModeEx();
    }

    protected void stopPreviewForShowDetailView() {
        this.mHandler.sendEmptyMessageDelayed(20, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
    }

    protected void closeDetailViewAfterStartPreview() {
        this.mHandler.removeMessages(20);
        this.mHandler.sendEmptyMessage(95);
    }

    public boolean isInitialHelpSupportedModule() {
        return true;
    }

    protected void removeStopPreviewMessage() {
        this.mHandler.removeMessages(20);
    }

    protected void notifyChangeChildSetting(String key) {
    }

    protected void setSpecificModuleParam(CameraParameters parameters) {
    }

    public int getSpliceViewIndex() {
        return -1;
    }

    public void setProgress(int progress) {
        final int p = progress;
        if (this.mGet != null && this.mDialogManager != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    BaseModule.this.mDialogManager.setProgress(p);
                }
            });
        }
    }

    public boolean isRecordingAnimShowing() {
        if (this.mCaptureButtonManager == null) {
            return false;
        }
        return this.mCaptureButtonManager.isWorkingShutterAnimation();
    }

    public int getMaxEVStep() {
        return this.mCameraCapabilities == null ? 12 : this.mCameraCapabilities.getMaxEVStep();
    }

    public boolean isEVShutterSupportedMode() {
        return true;
    }

    public CameraCapabilities getCameraCapabilities() {
        return this.mCameraCapabilities;
    }

    public boolean isScreenPinningState() {
        if (this.mGet != null) {
            return this.mGet.isScreenPinningState();
        }
        return false;
    }

    public void composeSignatureImage(Image yuvImage) {
        if (yuvImage != null && isSignatureEnableCondition() && isHALSignatureCaptureMode()) {
            this.mGet.composeSignatureImage(yuvImage, CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), getOrientationDegree(), this.mCameraId));
        }
    }

    public boolean isLivePhotoEnabled() {
        return this.mLivePhotoManager != null && this.mLivePhotoManager.isLivePhotoEnabled();
    }

    public void updateIndicatorPosition(int position) {
        this.mDotIndicatorManager.updateIndicatorPosition(position);
    }

    public void showExtraPreviewUI(boolean show, boolean useAnim, boolean top, boolean bottom) {
        if (show) {
            this.mExtraPrevewUIManager.show(useAnim, top, bottom);
            return;
        }
        this.mExtraPrevewUIManager.hide(useAnim, top, bottom);
        this.mExtraPrevewUIManager.setStrengthBarVisibility(false, useAnim);
    }

    public void hideStickerMenu(boolean forceRestart) {
        CamLog.m3d(CameraConstants.TAG, "forceRestart : " + forceRestart);
        if (this.mStickerManager != null) {
            if (this.mStickerManager.isStickerGridVisible()) {
                this.mStickerManager.hideItemStickerLayout();
                onHideMenu(16);
            }
            if (forceRestart) {
                settingForSticker(false);
                stopSticker(true);
                this.mExtraPrevewUIManager.changeButtonState(1, false);
            } else {
                this.mExtraPrevewUIManager.changeButtonState(1, true);
            }
            showExtraPreviewUI(false, false, true, isRearCamera());
            this.mQuickButtonManager.updateButtonIcon(C0088R.id.quick_button_film_emulator, this.mQuickButtonManager.getFilmQuickButtonSelector(), this.mQuickButtonManager.getFilmQuickButtonPressedImage());
            this.mQuickButtonManager.updateButton(C0088R.id.quick_button_film_emulator);
            this.mQuickButtonManager.refreshButtonEnable(100, true, true);
            setQuickButtonIndex(C0088R.id.quick_button_film_emulator, 0);
            this.mGet.setUspVisibility(0);
        }
    }

    public void stopSticker(boolean needRestartPreview) {
        CamLog.m7i(CameraConstants.TAG, "needRestartPreview : " + needRestartPreview);
        if (this.mStickerManager != null && this.mStickerManager.isRunning()) {
            this.mStickerManager.stop(true);
            settingForSticker(false);
            if (isVideoCaptureMode()) {
                CamLog.m7i(CameraConstants.TAG, "isVideoCaptureMode");
                ListPreference listPref = getListPreference(getVideoSizeSettingKey());
                if (listPref != null) {
                    if (listPref.getValue() == null) {
                        CamLog.m5e(CameraConstants.TAG, "videoSize null");
                        return;
                    } else {
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue()));
                    }
                }
            } else {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId)).getExtraInfo(1));
            }
            this.mParamUpdater.setParamValue(ParamConstants.KEY_NIGHTVISION_PARAM, "normal");
            if (needRestartPreview && getCameraState() < 5 && !isTimerShotCountdown()) {
                startPreview(null, null, false);
                setCameraState(1);
            }
        }
    }

    public void setExtraPreviewButtonSelected(int buttonType, boolean isSelected) {
        this.mExtraPrevewUIManager.changeButtonState(buttonType, isSelected);
    }

    protected void setRecordingPreviewState(int stateType, boolean set) {
        CamLog.m3d(CameraConstants.TAG, "-Recording Preview- stateType = " + stateType + ", set = " + set);
        if (stateType == 0) {
            this.mRecordingPreviewState = 0;
        } else if (set) {
            this.mRecordingPreviewState |= stateType;
        } else {
            this.mRecordingPreviewState &= stateType ^ -1;
        }
    }

    public boolean getRecordingPreviewState(int stateType) {
        return (this.mRecordingPreviewState & stateType) != 0;
    }

    public boolean checkUspZoneAvailable() {
        if (!checkQuickButtonAvailable()) {
            return false;
        }
        if (getFilmState() != 1 && getFilmState() != 5 && getCameraState() > 0 && this.mGet.checkModuleValidate(48) && !isVolumeKeyPressed() && !isGIFEncoding()) {
            return true;
        }
        CamLog.m5e(CameraConstants.TAG, "[usp] can not changing usp zone");
        return false;
    }

    public boolean isIndicatorSupported(int indicatorId) {
        if (isRecordingState()) {
            switch (indicatorId) {
                case C0088R.id.indicator_item_hdr_or_flash:
                    return "1".equals(getSettingValue("hdr-mode"));
                case C0088R.id.indicator_item_steady:
                    return isAvailableSteadyCam();
            }
        }
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
            case C0088R.id.indicator_item_hdr_or_flash:
                return true;
            case C0088R.id.indicator_item_livephoto:
                return FunctionProperties.isLivePhotoSupported();
        }
        return false;
    }

    protected void restoreBinningSetting() {
    }

    protected void handleBinningIconUI(boolean isShow, int whereFrom) {
    }

    protected boolean isBinningSupportedMode() {
        return "mode_normal".equals(getShotMode()) || getShotMode().contains(CameraConstants.MODE_SMART_CAM);
    }

    public boolean isRecordingSingleZoom() {
        return FunctionProperties.getSupportedHal() == 2 && FunctionProperties.isSupportedInAndOutZoom() && !FunctionProperties.isSupportedOpticZoom() && isRearCamera();
    }

    public void hideAndCancelAllFocusForBinningState(boolean checkCaptureProgress) {
        if (this.mFocusManager != null) {
            this.mFocusManager.hideAndCancelAllFocus(checkCaptureProgress);
            this.mFocusManager.releaseTouchFocus();
        }
    }

    protected void handlePreviewCallback() {
    }

    public boolean isStickerSelected() {
        if (FunctionProperties.isSupportedSticker() && this.mStickerManager != null && this.mStickerManager.hasSticker()) {
            return true;
        }
        return false;
    }

    public boolean isStickerIconDisableCondition() {
        return false;
    }

    public void setEditDim(boolean dim) {
        if (this.mExtraPrevewUIManager != null) {
            this.mExtraPrevewUIManager.setEditDim(dim);
        }
    }
}
