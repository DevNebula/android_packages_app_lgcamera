package com.lge.camera.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.Image;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.p000v4.view.PointerIconCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.components.BarView;
import com.lge.camera.components.HybridViewConfig;
import com.lge.camera.components.ShutterButton;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.constants.StorageProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.FaceCommon;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.device.ICameraCallback.CameraPictureCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.ICameraCallback.CameraShutterCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.file.ImageRegisterRequest;
import com.lge.camera.file.MediaSaveService.OnLocalSaveByTimeListener;
import com.lge.camera.file.SaveRequest;
import com.lge.camera.file.SupportedExif;
import com.lge.camera.managers.AdvancedSelfieManager;
import com.lge.camera.managers.AudioZoomManager;
import com.lge.camera.managers.AudioZoomManager.RecorderListener;
import com.lge.camera.managers.BinningManager;
import com.lge.camera.managers.CheeseShutterManager;
import com.lge.camera.managers.CheeseShutterManager.OnVoiceRecogListener;
import com.lge.camera.managers.DirectCallbackManager;
import com.lge.camera.managers.DotIndicatorManager.DotIndicatorInterface;
import com.lge.camera.managers.DoubleCameraManager;
import com.lge.camera.managers.ExtraPreviewUIManager;
import com.lge.camera.managers.ExtraPreviewUIManager.ExtraPreviewUIInterface;
import com.lge.camera.managers.FilmEmulatorEnginInterface;
import com.lge.camera.managers.FocusManager;
import com.lge.camera.managers.GestureShutterManager;
import com.lge.camera.managers.GestureShutterManagerIF;
import com.lge.camera.managers.GestureShutterManagerIF.OnGestureRecogListener;
import com.lge.camera.managers.GestureViewManager;
import com.lge.camera.managers.ImageUriInfo;
import com.lge.camera.managers.InAndOutZoomManager;
import com.lge.camera.managers.IntervalShotManager;
import com.lge.camera.managers.IntervalShotManagerIF;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.managers.PostviewManager;
import com.lge.camera.managers.PostviewManager.OnPostviewListener;
import com.lge.camera.managers.RecordingUIManager;
import com.lge.camera.managers.RecordingUIManager.RecorderInterface;
import com.lge.camera.managers.ReviewThumbnailManagerBase.onQuickViewListener;
import com.lge.camera.managers.ReviewThumbnailManagerBase.onSelfieQuickViewListener;
import com.lge.camera.managers.SquareSnapGalleryItem;
import com.lge.camera.managers.TemperatureManager;
import com.lge.camera.managers.TimerManager.TimerType;
import com.lge.camera.managers.TimerManager.TimerTypeCamera;
import com.lge.camera.managers.TimerManager.TimerTypeGestureShot;
import com.lge.camera.managers.ToastManager;
import com.lge.camera.managers.UndoInterface;
import com.lge.camera.managers.ZoomManager;
import com.lge.camera.managers.ext.QRCodeManager;
import com.lge.camera.managers.ext.sticker.StickerGLSurfaceView;
import com.lge.camera.managers.ext.sticker.StickerManager;
import com.lge.camera.managers.ext.sticker.StickerManager.StickerContentCallback;
import com.lge.camera.managers.ext.sticker.solutions.ContentsInformation;
import com.lge.camera.settings.CameraPreference;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.ModeItem;
import com.lge.camera.settings.PreferenceGroup;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.PackageUtil;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SnapShotChecker;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.UpdateThumbnailRequest;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;
import com.lge.camera.util.XMPWriter;
import com.lge.hardware.LGCamera.CameraMetaDataCallback;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGThermalManager;
import com.lge.systemservice.core.WfdManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultCameraModule extends BaseModule implements CameraPreviewDataCallback, CameraImageCallback, OnAudioFocusChangeListener {
    private static final String LOOP_TEMP_FILE = "looping_temp";
    private static final long TIME_TO_ENABLE_STEADY_CAM_SETTING = 2000;
    protected static int sBurstShotCount;
    protected static int[] sVideoSize = null;
    protected boolean mAllowPause = true;
    protected AudioZoomManager mAudioZoomManager = new AudioZoomManager(this);
    protected int mBeforeCaptureCount = 0;
    private boolean mBurstCountAvailable = false;
    protected HandlerRunnable mBurstCountInvisibilityRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (DefaultCameraModule.this.mSnapShotChecker.checkMultiShotState(12) && DefaultCameraModule.this.mSnapShotChecker.isSnapShotProcessing()) {
                CamLog.m3d(CameraConstants.TAG, "Repost mBurstCountInvisibilityRunnable");
                DefaultCameraModule.this.mGet.postOnUiThread(DefaultCameraModule.this.mBurstCountInvisibilityRunnable, 1500);
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "mBurstCountInvisible " + DefaultCameraModule.this.mReviewThumbnailManager);
            if (DefaultCameraModule.this.mReviewThumbnailManager != null) {
                DefaultCameraModule.this.mReviewThumbnailManager.startBurstCaptureEffect(false);
                DefaultCameraModule.this.mReviewThumbnailManager.setBurstCountVisibility(8, 0);
                DefaultCameraModule.this.mReviewThumbnailManager.setEnabled(true);
            }
        }
    };
    protected HandlerRunnable mBurstCountUpdateRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (DefaultCameraModule.this.mReviewThumbnailManager != null) {
                DefaultCameraModule.this.mReviewThumbnailManager.setThumbnailVisibility(0, false);
                DefaultCameraModule.this.mReviewThumbnailManager.setBurstCountVisibility(0, DefaultCameraModule.this.mUpdateCount);
            }
        }
    };
    protected String mBurstShotFileName;
    protected Timer mButtonCheckTimer = null;
    protected byte[] mCaptureData = null;
    protected long mCheckInterval = 0;
    protected CheeseShutterManager mCheeseShutterManager = new CheeseShutterManager(this);
    protected CameraPictureCallback mCroppedPictureCallback = new C022149();
    protected HandlerRunnable mDeactivateQuickView = new HandlerRunnable(this) {
        public void handleRun() {
            if (!(DefaultCameraModule.this.mReviewThumbnailManager.isActivatedSelfieQuickView() || DefaultCameraModule.this.mGestureShutterManager == null)) {
                DefaultCameraModule.this.mGestureShutterManager.stopMotionEngine();
                CamLog.m3d(CameraConstants.TAG, "run stopMotionEngine");
            }
            DefaultCameraModule.this.mMakeQuickViewImageThread = null;
        }
    };
    protected HandlerRunnable mDetectCameraChangeRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (DefaultCameraModule.this.mDetectedCameraIdByOpticZoom != DefaultCameraModule.this.mCameraId) {
                DefaultCameraModule.this.detectCameraChanged(DefaultCameraModule.this.mDetectedCameraIdByOpticZoom);
            }
        }
    };
    protected int mDetectedCameraIdByOpticZoom;
    protected final DirectCallbackManager mDirectCallbackManager = new DirectCallbackManager();
    private Handler mEnableSteadyCamParam = new Handler();
    private Runnable mEnableSteadyCamSetting = new C020235();
    protected String mFileName = null;
    protected FilmEmulatorEnginInterface mFilmEmulatorEnginInterface = new C023561();
    protected HandlerRunnable mFilmEmulatorStartHandler = new HandlerRunnable(this) {
        public void handleRun() {
            CamLog.m3d(CameraConstants.TAG, "surface Texture is null, so recreate textures");
            if (DefaultCameraModule.this.mAdvancedFilmManager != null) {
                AdvancedSelfieManager advancedSelfieManager = DefaultCameraModule.this.mAdvancedFilmManager;
                ActivityBridge activityBridge = DefaultCameraModule.this.mGet;
                String str = (CameraConstants.MODE_SMART_CAM.equals(DefaultCameraModule.this.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(DefaultCameraModule.this.getShotMode())) ? Setting.KEY_SMART_CAM_FILTER : Setting.KEY_FILM_EMULATOR;
                advancedSelfieManager.runFilmEmulator(activityBridge.getCurSettingValue(str));
                if (DefaultCameraModule.this.isFastShotSupported() && DefaultCameraModule.this.mCameraDevice != null) {
                    DefaultCameraModule.this.mCameraDevice.setLongshot(false);
                }
            }
        }
    };
    protected FilmLiveSnapShotThread mFilmLiveSnapShotThread;
    protected byte[] mFirstBurstData = null;
    protected Uri mFirstBurstUri = null;
    private long mFreeSpace = 0;
    protected long mFreeSpaceForSafeRecording = 0;
    protected OnGestureRecogListener mGestureRecogListener = new C018822();
    protected GestureShutterManagerIF mGestureShutterManager = new GestureShutterManagerIF(this);
    protected IntervalShotManagerIF mIntervalShotManager = new IntervalShotManagerIF(this);
    private HandlerRunnable mIntervalShotRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            CamLog.m3d(CameraConstants.TAG, "Run mIntervalShotRunnable. Count : " + DefaultCameraModule.this.mSnapShotChecker.getIntervalShotCount());
            if (!DefaultCameraModule.this.checkModuleValidate(1) || DefaultCameraModule.this.mIntervalShotManager == null || DefaultCameraModule.this.mSnapShotChecker.isIntervalShotMax() || !DefaultCameraModule.this.mSnapShotChecker.checkMultiShotState(2)) {
                CamLog.m3d(CameraConstants.TAG, "Interval shot cannot be taken due to certain state");
                return;
            }
            DefaultCameraModule.this.setCaptureButtonEnable(false, DefaultCameraModule.this.getShutterButtonType());
            DefaultCameraModule.this.onCameraShutterButtonClicked();
        }
    };
    protected Timer mIntervalShotTimer = null;
    protected boolean mIsFileSizeLimitReached = false;
    protected boolean mIsFirstBurstShot = false;
    private boolean mIsMMSRecordingSize = false;
    protected boolean mIsMTKFlashFired = false;
    protected boolean mIsNeedFinishAfterSaving = false;
    protected boolean mIsNeedToCheckFlashTemperature = true;
    protected boolean mIsUHDRecTimeLimitWarningDisplayed = false;
    private long mLastLongShotTime = 0;
    protected int mLimitRecordingDuration = 0;
    protected long mLimitRecordingSize = 0;
    protected CameraPictureCallback mLongShotPictureCallback = new C017914();
    protected CameraShutterCallback mLongShotShutterCallback = new C017813();
    private boolean mLongShotShutterCallbackReceived = true;
    private final ConditionVariable mLongShotSync = new ConditionVariable(true);
    protected int mLongshotJpegDegree = 0;
    protected StartRecorderInfo mLoopRecorderInfo = null;
    private String mLoopTempFilePath = "";
    private boolean mLoopToggle = true;
    private Thread mMakeQuickViewImageThread = null;
    protected boolean mNeedCropPicture = false;
    protected HandlerRunnable mOnPictureTakenCallbackAfter = new HandlerRunnable(this) {
        public void handleRun() {
            DefaultCameraModule.this.onPictureCallbackAfterRun(true, true);
        }
    };
    protected CameraMetaDataCallback mOpticZoomCallback = null;
    protected int mOrientationAtStartRecording = 2;
    protected CameraPictureCallback mPictureCallback = new C022048();
    private final ConditionVariable mPictureCallbackLock = new ConditionVariable(true);
    protected OnPostviewListener mPostviewListener = new C021846();
    protected PostviewManager mPostviewManager = new PostviewManager(this);
    protected Thread mPrepareStartRecordingThread;
    private String mPrevFileName = null;
    protected onQuickViewListener mQuickViewListener = new C019024();
    protected int mRAWJPGFileNameSyncNum = 0;
    protected CameraPictureCallback mRAWPictureCallback = null;
    protected RecordingUIManager mRecordingUIManager = new RecordingUIManager(this);
    protected onSelfieQuickViewListener mSelfieQuickViewListener = new C018923();
    private HandlerRunnable mShotToShotTakeRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (!DefaultCameraModule.this.mTimerManager.getIsGesureTimerShotProgress()) {
                if (DefaultCameraModule.this.checkModuleValidate(17) && DefaultCameraModule.this.isShutterButtonStateAvailableForBurstShot()) {
                    CamLog.m3d(CameraConstants.TAG, "Shot to shot take");
                    DefaultCameraModule.this.mSnapShotChecker.setMultiShotState(4);
                    DefaultCameraModule.this.onCameraShutterButtonClicked();
                    return;
                }
                if (!DefaultCameraModule.this.isShutterButtonStateAvailableForBurstShot()) {
                    DefaultCameraModule.this.onTakePictureAfter();
                }
                CamLog.m3d(CameraConstants.TAG, "Shot to shot cannot be taken due to certain state");
            }
        }
    };
    private HandlerRunnable mShowRecordingCoverRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            ListPreference listPref = DefaultCameraModule.this.getListPreference(DefaultCameraModule.this.getVideoSizeSettingKey());
            if (listPref == null) {
                CamLog.m5e(CameraConstants.TAG, "KEY_VIDEO_RECORDSIZE listPref is null in startRecorder");
            } else if (!DefaultCameraModule.this.mGet.isPaused()) {
                final int[] videoSize = Utils.sizeStringToArray(listPref.getExtraInfo(2));
                if (DefaultCameraModule.this.checkPreviewCoverVisibilityForRecording()) {
                    boolean useAnimation;
                    int[] lcdSize = Utils.getLCDsize(DefaultCameraModule.this.getAppContext(), true);
                    DefaultCameraModule.this.mGet.getCurPreviewBitmap((int) (((float) lcdSize[1]) * 0.5f), (int) (((float) lcdSize[0]) * 0.5f));
                    if (CameraConstants.MODE_POPOUT_CAMERA.equals(DefaultCameraModule.this.getShotMode())) {
                        useAnimation = false;
                    } else {
                        useAnimation = true;
                    }
                    DefaultCameraModule.this.postOnUiThread(new HandlerRunnable(DefaultCameraModule.this) {
                        public void handleRun() {
                            if (!DefaultCameraModule.this.mGet.isPaused()) {
                                CamLog.m3d(CameraConstants.TAG, "[Animation], animation end");
                                if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(DefaultCameraModule.this.getShotMode()) || CameraConstants.MODE_SQUARE_GRID.equals(DefaultCameraModule.this.getShotMode())) {
                                    DefaultCameraModule.this.mGet.setTextureLayoutParams(videoSize[0], videoSize[1], RatioCalcUtil.getSqureTopMargin(DefaultCameraModule.this.getAppContext()));
                                } else {
                                    DefaultCameraModule.this.mGet.setTextureLayoutParams(videoSize[0], videoSize[1], -1, true);
                                }
                            }
                        }
                    }, ModelProperties.getAppTier() == 5 ? 300 : 250);
                    DefaultCameraModule.this.mGet.setPreviewCoverVisibility(0, useAnimation, null, true, true);
                } else if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(DefaultCameraModule.this.getShotMode()) || CameraConstants.MODE_SQUARE_GRID.equals(DefaultCameraModule.this.getShotMode())) {
                    DefaultCameraModule.this.mGet.setTextureLayoutParams(videoSize[0], videoSize[1], RatioCalcUtil.getSqureTopMargin(DefaultCameraModule.this.getAppContext()));
                } else {
                    DefaultCameraModule.this.mGet.setTextureLayoutParams(videoSize[0], videoSize[1], -1, true);
                }
            }
        }
    };
    protected CameraShutterCallback mShutterCallback = new C021947();
    protected StartRecorderInfo mStartRecorderInfo = null;
    protected Thread mStartRecorderThread = null;
    StickerContentCallback mStickerContentCallback = new C024971();
    protected HandlerRunnable mStopBurstShotRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (DefaultCameraModule.this.mCaptureButtonManager != null) {
                DefaultCameraModule.this.mCaptureButtonManager.setShutterButtonPressed(false, 2);
            }
            DefaultCameraModule.this.mGet.showTilePreviewCoverView(false);
            DefaultCameraModule.this.stopBurstShotTaking(false);
        }
    };
    protected Runnable mStopRecorderByThread = new C020336();
    protected Thread mStopRecordingThread = null;
    protected StartRecorderInfo mTempLoopRecorderInfo = null;
    protected HandlerRunnable mUpdateBurstCountRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (!DefaultCameraModule.this.mSnapShotChecker.isBurstCountMax(DefaultCameraModule.this.getCurStorage() == 0, false, DefaultCameraModule.this.isRearCamera())) {
                DefaultCameraModule.this.updateBurstCount(false, true);
                if (DefaultCameraModule.this.mReviewThumbnailManager != null) {
                    DefaultCameraModule.this.mReviewThumbnailManager.startBurstCaptureEffect(true);
                }
            }
        }
    };
    protected int mUpdateCount = 0;
    protected HandlerRunnable mUpdateRecordingTime = new HandlerRunnable(this) {
        public void handleRun() {
            DefaultCameraModule.this.updateRecordingTime();
        }
    };
    private OnVoiceRecogListener mVoiceRecogListener = new C018721();
    protected int mWaitSavingDialogType = 0;

    /* renamed from: com.lge.camera.app.DefaultCameraModule$13 */
    class C017813 implements CameraShutterCallback {
        C017813() {
        }

        public void onShutter(CameraProxy camera) {
            if (DefaultCameraModule.this.mSnapShotChecker.checkMultiShotState(1)) {
                DefaultCameraModule.this.mLongShotShutterCallbackReceived = true;
                DefaultCameraModule.this.mSnapShotChecker.setSnapShotState(0);
                DefaultCameraModule.this.mSnapShotChecker.setPictureCallbackState(1);
            }
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$14 */
    class C017914 extends CameraPictureCallback {
        C017914() {
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            int i = 5;
            Module.sFirstTaken = true;
            CamLog.m3d(CameraConstants.TAG, "onPictureTaken - burst state : " + DefaultCameraModule.this.mSnapShotChecker.getBurstState());
            DefaultCameraModule.this.mSnapShotChecker.removeBurstState(64);
            if (!DefaultCameraModule.this.mSnapShotChecker.checkBurstState(16) && DefaultCameraModule.this.mSnapShotChecker.getBurstState() != 0) {
                DefaultCameraModule.this.mSnapShotChecker.setPictureCallbackState(3);
                boolean callbackResult = DefaultCameraModule.this.doLongShotPictureCallback(data, camera);
                DefaultCameraModule.this.mCaptureData = data;
                if (callbackResult) {
                    ActivityBridge activityBridge;
                    if (DefaultCameraModule.this.mSnapShotChecker.getBurstCount() == 1 && DefaultCameraModule.this.mSnapShotChecker.checkBurstState(2)) {
                        CamLog.m3d(CameraConstants.TAG, "LongShot 1 playsound and return.");
                        DefaultCameraModule.this.mSnapShotChecker.removeBurstState(2);
                        activityBridge = DefaultCameraModule.this.mGet;
                        if (DefaultCameraModule.this.isRearCamera()) {
                            i = 6;
                        }
                        activityBridge.playSound(i, false, 0);
                        DefaultCameraModule.this.mSnapShotChecker.removeBurstState(32);
                        DefaultCameraModule.this.stopBurstShot(false);
                        DefaultCameraModule.this.mSnapShotChecker.initBurstCountLater(false);
                        return;
                    } else if (!DefaultCameraModule.this.mSnapShotChecker.checkBurstState(20)) {
                        DefaultCameraModule.this.mSnapShotChecker.setBurstState(4);
                        activityBridge = DefaultCameraModule.this.mGet;
                        if (DefaultCameraModule.this.isRearCamera()) {
                            i = 6;
                        }
                        activityBridge.playSound(i, true, 0);
                        DefaultCameraModule.this.mSnapShotChecker.setPictureCallbackState(1);
                    }
                }
                if (DefaultCameraModule.this.mSnapShotChecker.checkBurstState(32)) {
                    DefaultCameraModule.this.mSnapShotChecker.removeBurstState(32);
                    DefaultCameraModule.this.stopBurstShot(DefaultCameraModule.this.mSnapShotChecker.checkMultiShotState(1));
                    DefaultCameraModule.this.mSnapShotChecker.initBurstCountLater(false);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$21 */
    class C018721 implements OnVoiceRecogListener {
        C018721() {
        }

        public void voiceRecogSuccess(int type) {
            if (DefaultCameraModule.this.checkEnableRecogSucess()) {
                DefaultCameraModule.this.updateIndicator(1, 0, true);
                DefaultCameraModule.this.doVoiceRecogSuccess(type);
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "cannot takepicture by voice Shutter");
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$22 */
    class C018822 implements OnGestureRecogListener {
        C018822() {
        }

        public void doTimershotByGestureRecog(int captureType) {
            if (!DefaultCameraModule.this.checkModuleValidate(223) || SystemBarUtil.isSystemUIVisible(DefaultCameraModule.this.getActivity()) || DefaultCameraModule.this.isRotateDialogVisible() || DefaultCameraModule.this.mCameraDevice == null || ((DefaultCameraModule.this.mTimerManager != null && DefaultCameraModule.this.mTimerManager.isTimerShotCountdown()) || (DefaultCameraModule.this.mReviewThumbnailManager != null && DefaultCameraModule.this.mReviewThumbnailManager.isQuickViewAniStarted()))) {
                DefaultCameraModule.this.mGestureShutterManager.startGestureEngine();
                return;
            }
            if (!(DefaultCameraModule.this.mReviewThumbnailManager == null || DefaultCameraModule.this.mGestureShutterManager == null)) {
                if (DefaultCameraModule.this.mReviewThumbnailManager.isQuickViewAniStarted()) {
                    DefaultCameraModule.this.mGestureShutterManager.startGestureEngine();
                    return;
                } else {
                    DefaultCameraModule.this.mGestureShutterManager.stopMotionEngine();
                    DefaultCameraModule.this.mReviewThumbnailManager.setActivatedSelfieQuickView(false);
                }
            }
            if (DefaultCameraModule.this.mButtonCheckTimer == null) {
                DefaultCameraModule.this.checkPictureCallbackRunnable(true);
                DefaultCameraModule.this.mSnapShotChecker.removeMultiShotState(8);
                CamLog.m3d(CameraConstants.TAG, "send CLEAR_SCREEN_DELAY message");
                DefaultCameraModule.this.keepScreenOnAwhile();
                if (DefaultCameraModule.this.mGet.isActivatedTilePreview() && captureType == 2) {
                    DefaultCameraModule.this.mGet.showTilePreview(false);
                }
                DefaultCameraModule.this.doStartTimerShot(new TimerTypeGestureShot());
            }
        }

        public void onShowQuickView() {
            if (DefaultCameraModule.this.mGestureShutterManager != null && DefaultCameraModule.this.mReviewThumbnailManager != null && !DefaultCameraModule.this.mReviewThumbnailManager.isQuickViewAniStarted() && !DefaultCameraModule.this.mGet.isModuleChanging() && DefaultCameraModule.this.getCameraState() == 1 && !DefaultCameraModule.this.mTimerManager.isTimerShotCountdown() && !DefaultCameraModule.this.mSnapShotChecker.checkMultiShotState(2) && !DefaultCameraModule.this.mQuickClipManager.isOpened()) {
                if (DefaultCameraModule.this.mGifManager.isGIFEncoding()) {
                    CamLog.m3d(CameraConstants.TAG, "[GIF] in GIF encoding. onShowQuickView return");
                } else if (DefaultCameraModule.this.isActivatedQuickdetailView()) {
                    CamLog.m3d(CameraConstants.TAG, "[gestureView] isActivatedQuickdetailView ? " + DefaultCameraModule.this.isActivatedQuickdetailView());
                } else {
                    DefaultCameraModule.this.mReviewThumbnailManager.setActivatedSelfieQuickView(true);
                    if (DefaultCameraModule.this.checkModuleValidate(16) && DefaultCameraModule.this.mMakeQuickViewImageThread != null && !DefaultCameraModule.this.mMakeQuickViewImageThread.isAlive() && DefaultCameraModule.this.mMakeQuickViewImageThread.getState() == State.NEW) {
                        DefaultCameraModule.this.mMakeQuickViewImageThread.start();
                    }
                    DefaultCameraModule.this.mReviewThumbnailManager.showSelfieQuickView();
                    DefaultCameraModule.this.setQuickClipIcon(true, false);
                    CamLog.m3d(CameraConstants.TAG, "call showSelfieQuickView");
                }
            }
        }

        public void onHideQuickView() {
            DefaultCameraModule.this.hideSelfieQuickView();
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$23 */
    class C018923 implements onSelfieQuickViewListener {
        C018923() {
        }

        public void onHideQuickView() {
            DefaultCameraModule.this.hideSelfieQuickView();
        }

        public void onDoMotionEngine(int Action) {
            if (DefaultCameraModule.this.mGestureShutterManager != null) {
                switch (Action) {
                    case 0:
                        if (!DefaultCameraModule.this.isRearCamera() && DefaultCameraModule.this.mGestureShutterManager != null && DefaultCameraModule.this.mGestureShutterManager.isAvailableMotionQuickView()) {
                            DefaultCameraModule.this.mGestureShutterManager.startMotionEngine();
                            return;
                        }
                        return;
                    case 1:
                        if (DefaultCameraModule.this.mGestureShutterManager != null) {
                            DefaultCameraModule.this.mGestureShutterManager.stopMotionEngine();
                            return;
                        }
                        return;
                    case 2:
                        if (!DefaultCameraModule.this.isRearCamera() && DefaultCameraModule.this.mGestureShutterManager != null && DefaultCameraModule.this.mGestureShutterManager.isAvailableMotionQuickView()) {
                            DefaultCameraModule.this.mGestureShutterManager.startMotionEngine();
                            return;
                        }
                        return;
                    case 3:
                        if (DefaultCameraModule.this.mGestureShutterManager != null && DefaultCameraModule.this.mGestureShutterManager.isAvailableMotionQuickView()) {
                            DefaultCameraModule.this.mGestureShutterManager.resumePushMotionEngine();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }

        public void onAnimationStartForGestureView(boolean open) {
            if (open) {
                DefaultCameraModule.this.readyForGestureview();
                DefaultCameraModule.this.setQuickClipIcon(true, false);
            }
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$24 */
    class C019024 implements onQuickViewListener {
        C019024() {
        }

        public void onCompleteHideQuickview() {
            DefaultCameraModule.this.completeHideQuickview();
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$27 */
    class C019327 extends TimerTask {
        C019327() {
        }

        public void run() {
            DefaultCameraModule.this.mGet.runOnUiThread(DefaultCameraModule.this.mIntervalShotRunnable);
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$28 */
    class C019428 extends TimerTask {
        C019428() {
        }

        public void run() {
            DefaultCameraModule.this.mGet.runOnUiThread(DefaultCameraModule.this.mIntervalShotRunnable);
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$35 */
    class C020235 implements Runnable {
        C020235() {
        }

        public void run() {
            if (DefaultCameraModule.this.isAvailableSteadyCam()) {
                DefaultCameraModule.this.setSettingMenuEnable(Setting.KEY_VIDEO_STEADY, true);
            } else {
                DefaultCameraModule.this.setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
            }
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$36 */
    class C020336 implements Runnable {
        C020336() {
        }

        public void run() {
            DefaultCameraModule.this.mGet.runOnUiThread(new HandlerRunnable(DefaultCameraModule.this) {
                public void handleRun() {
                    if (DefaultCameraModule.this.getRecordingPreviewState(8) && DefaultCameraModule.this.checkPreviewCoverVisibilityForRecording()) {
                        int[] lcdSize = Utils.getLCDsize(DefaultCameraModule.this.getAppContext(), true);
                        DefaultCameraModule.this.mGet.getCurPreviewBitmap((int) (((float) lcdSize[1]) * 0.5f), (int) (((float) lcdSize[0]) * 0.5f));
                        DefaultCameraModule.this.mGet.setPreviewCoverVisibility(0, false, null, true, true);
                    }
                }
            });
            if (DefaultCameraModule.this.mQuickClipManager != null) {
                DefaultCameraModule.this.mQuickClipManager.setAfterShot();
                DefaultCameraModule.this.setQuickClipIcon(false, false);
            }
            if (VideoRecorder.getLoopState() != 0) {
                VideoRecorder.setUUID();
            }
            DefaultCameraModule.this.stopRecorder();
            DefaultCameraModule.this.mAllowPause = true;
            VideoRecorder.setLoopState(0);
            DefaultCameraModule.this.releaseCaptureProgressOnPictureCallback();
            DefaultCameraModule.this.mGet.runOnUiThread(new HandlerRunnable(DefaultCameraModule.this) {
                public void handleRun() {
                    if (DefaultCameraModule.this.isUHDmode() && !DefaultCameraModule.this.getRecordingPreviewState(1)) {
                        DefaultCameraModule.this.showSavingDialog(false, 0);
                    }
                    DefaultCameraModule.this.doAfterStopRecorderThread();
                }
            });
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$3 */
    class C02113 implements RecorderListener {
        C02113() {
        }

        public void setAudiozoomMetadata() {
            VideoRecorder.setAudiozoomMetadata();
        }

        public boolean isVideoRecording() {
            return VideoRecorder.isRecording();
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$46 */
    class C021846 implements OnPostviewListener {
        C021846() {
        }

        public void onPostviewReleased() {
            DefaultCameraModule.this.onPostviewRelease();
            if (DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isStickerDrawing()) {
                DefaultCameraModule.this.mStickerManager.onPostviewReleased();
            }
        }

        public void onPostviewReleasedAfter(int type) {
            DefaultCameraModule.this.onPostviewReleaseAfter(type);
        }

        public void onPostviewDisplayed() {
            DefaultCameraModule.this.mGet.setPreviewCoverVisibility(0, true);
            if (DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isStickerDrawing()) {
                DefaultCameraModule.this.mStickerManager.onPostviewDisplayed();
            }
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$47 */
    class C021947 implements CameraShutterCallback {
        C021947() {
        }

        public void onShutter(CameraProxy camera) {
            DefaultCameraModule.this.onShutterCallback(true, true, false);
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$48 */
    class C022048 extends CameraPictureCallback {
        C022048() {
        }

        public void onPictureTakenBefore(byte[] data, byte[] extraExif) {
            if (DefaultCameraModule.this.isPaused()) {
                CamLog.m3d(CameraConstants.TAG, "app pausing.. saveImage directly");
                DefaultCameraModule.this.saveImage(data, extraExif);
            }
            DefaultCameraModule.this.mPictureCallbackLock.open();
            CamLog.m3d(CameraConstants.TAG, "onPictureTakenBefore, mPictureCallbackLock open");
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            Module.sFirstTaken = true;
            DefaultCameraModule.this.mNeedCropPicture = false;
            DefaultCameraModule.this.onPictureTakenCallback(data, extraExif, camera);
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$49 */
    class C022149 extends CameraPictureCallback {
        C022149() {
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            CamLog.m3d(CameraConstants.TAG, "-dualview- ### mCroppedPictureCallback");
            DefaultCameraModule.this.mNeedCropPicture = true;
            DefaultCameraModule.this.onPictureTakenCallback(data, extraExif, camera, false);
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$4 */
    class C02224 implements RecorderInterface {
        C02224() {
        }

        public String getFilePath() {
            return VideoRecorder.getFilePath();
        }

        public int getMaxDuration() {
            return VideoRecorder.getMaxDuration();
        }

        public long getMaxFileSize() {
            return VideoRecorder.getMaxFileSize();
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$5 */
    class C02335 implements ExtraPreviewUIInterface {
        C02335() {
        }

        public boolean onStickerMenuClicked() {
            if (DefaultCameraModule.this.mStickerManager == null) {
                return false;
            }
            if ((DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isWaitOneShot()) || !DefaultCameraModule.this.mQuickButtonManager.checkCommonButtonState(C0088R.id.quick_button_film_emulator)) {
                return false;
            }
            if (DefaultCameraModule.this.mStickerManager.isStickerGridVisible()) {
                if (DefaultCameraModule.this.mStickerManager.isStickerDrawing()) {
                    DefaultCameraModule.this.hideStickerMenu(false);
                } else {
                    DefaultCameraModule.this.hideStickerMenu(true);
                }
                return true;
            }
            if (DefaultCameraModule.this.mAdvancedFilmManager != null && DefaultCameraModule.this.mAdvancedFilmManager.isShowingFilmMenu()) {
                DefaultCameraModule.this.setSetting(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, true);
                DefaultCameraModule.this.mAdvancedFilmManager.setCurIndex(CameraConstants.FILM_NONE);
                DefaultCameraModule.this.mAdvancedFilmManager.showFilmMenu(false, 1, false, false, 0, false);
                DefaultCameraModule.this.mExtraPrevewUIManager.changeButtonState(0, false);
            }
            if (DefaultCameraModule.this.mAdvancedFilmManager != null && DefaultCameraModule.this.isBarVisible(1)) {
                DefaultCameraModule.this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
            }
            CamLog.m3d(CameraConstants.TAG, "Sticker start!!!");
            DefaultCameraModule.this.onShowMenu(16);
            if (DefaultCameraModule.this.mStickerManager.isGLSurfaceViewShowing() && DefaultCameraModule.this.mStickerManager.isRunning()) {
                DefaultCameraModule.this.mStickerManager.showItemStickerLayout();
            } else {
                if (!DefaultCameraModule.this.mGet.isStickerGuideShown()) {
                    DefaultCameraModule.this.mToastManager.showShortToast(DefaultCameraModule.this.getActivity().getString(C0088R.string.sticker_setting_toast));
                    DefaultCameraModule.this.mGet.setIsStickerGuideShown();
                }
                if (DefaultCameraModule.this.getBinningEnabledState()) {
                    DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_DEFAULT_STR);
                    DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_BINNING_PARAM, "normal");
                    DefaultCameraModule.this.mParamUpdater.setParamValue("picture-size", DefaultCameraModule.this.getCurrentSelectedPictureSize());
                }
                boolean wait = false;
                if (!(DefaultCameraModule.this.mAdvancedFilmManager == null || DefaultCameraModule.this.mAdvancedFilmManager.isRunningFilmEmulator())) {
                    DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, DefaultCameraModule.this.mStickerManager.getSupportedPreviewSize());
                    DefaultCameraModule.this.startPreview(null);
                    DefaultCameraModule.this.setCameraState(1);
                    wait = true;
                }
                wait = wait || (DefaultCameraModule.this.mAdvancedFilmManager != null && DefaultCameraModule.this.mAdvancedFilmManager.isRunningFilmEmulator());
                if (wait) {
                    DefaultCameraModule.this.mStickerManager.waitOneShot();
                    DefaultCameraModule.this.mStickerManager.showItemStickerLayout();
                } else {
                    DefaultCameraModule.this.mStickerManager.start(DefaultCameraModule.this.getGLViewMargin());
                }
                if (DefaultCameraModule.this.mAdvancedFilmManager != null && DefaultCameraModule.this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                    DefaultCameraModule.this.setSetting(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, true);
                    DefaultCameraModule.this.mAdvancedFilmManager.setCurIndex(CameraConstants.FILM_NONE);
                    DefaultCameraModule.this.mAdvancedFilmManager.stopFilmEmulator(true, false);
                }
            }
            DefaultCameraModule.this.settingForSticker(true);
            DefaultCameraModule.this.mGet.setUspVisibility(8);
            DefaultCameraModule.this.showExtraPreviewUI(true, false, true, true);
            return true;
        }

        public boolean onFilterMenuClicked() {
            if ((DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isWaitOneShot()) || !DefaultCameraModule.this.mQuickButtonManager.checkCommonButtonState(C0088R.id.quick_button_film_emulator)) {
                return false;
            }
            Message msg;
            if (DefaultCameraModule.this.isColorEffectSupported()) {
                if (DefaultCameraModule.this.isMenuShowing(32)) {
                    msg = new Message();
                    msg.what = 101;
                    DefaultCameraModule.this.mHandler.sendMessage(msg);
                    return true;
                }
                msg = new Message();
                msg.what = 100;
                DefaultCameraModule.this.mHandler.sendMessage(msg);
            } else if (DefaultCameraModule.this.mAdvancedFilmManager.isRunningFilmEmulator() && DefaultCameraModule.this.isMenuShowing(4)) {
                msg = new Message();
                msg.what = 84;
                msg.arg1 = 3;
                DefaultCameraModule.this.mHandler.sendMessage(msg);
                return true;
            } else {
                if (DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isGLSurfaceViewShowing()) {
                    String previewSize;
                    if (DefaultCameraModule.this.mStickerManager.isStickerGridVisible()) {
                        DefaultCameraModule.this.mStickerManager.hideItemStickerLayout();
                    }
                    DefaultCameraModule.this.mStickerManager.stop(true);
                    DefaultCameraModule.this.settingForSticker(false);
                    if (DefaultCameraModule.this.mStickerManager.shoudRestoreByManager()) {
                        previewSize = DefaultCameraModule.this.mStickerManager.getSupportedPreviewSize();
                    } else {
                        previewSize = DefaultCameraModule.this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(DefaultCameraModule.this.getShotMode(), DefaultCameraModule.this.mCameraId)).getExtraInfo(1);
                    }
                    DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, previewSize);
                }
                msg = new Message();
                msg.what = 83;
                msg.arg1 = 3;
                DefaultCameraModule.this.mHandler.sendMessage(msg);
            }
            return true;
        }

        public boolean onSkinToneMenuClicked() {
            if (DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isWaitOneShot()) {
                return false;
            }
            if (DefaultCameraModule.this.mAdvancedFilmManager.isRunningFilmEmulator() && DefaultCameraModule.this.isMenuShowing(4)) {
                Message msg = new Message();
                msg.what = 84;
                msg.arg1 = 3;
                DefaultCameraModule.this.mHandler.sendMessage(msg);
            } else if (DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isGLSurfaceViewShowing()) {
                DefaultCameraModule.this.hideStickerMenu(true);
            }
            DefaultCameraModule.this.mAdvancedFilmManager.onClickSkinToneButton();
            return true;
        }

        public boolean onRelightingMenuClicked() {
            if (DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isWaitOneShot()) {
                return false;
            }
            if (DefaultCameraModule.this.mAdvancedFilmManager.isRunningFilmEmulator() && DefaultCameraModule.this.isMenuShowing(4)) {
                Message msg = new Message();
                msg.what = 84;
                msg.arg1 = 3;
                DefaultCameraModule.this.mHandler.sendMessage(msg);
            } else if (DefaultCameraModule.this.mStickerManager != null && DefaultCameraModule.this.mStickerManager.isGLSurfaceViewShowing()) {
                DefaultCameraModule.this.hideStickerMenu(true);
            }
            DefaultCameraModule.this.mAdvancedFilmManager.onClickRelightingButton();
            return true;
        }

        public boolean onEditClicked() {
            if (DefaultCameraModule.this.isMenuShowing(4)) {
                DefaultCameraModule.this.mAdvancedFilmManager.showEditToastPopup();
                DefaultCameraModule.this.mAdvancedFilmManager.setEditMode(!DefaultCameraModule.this.mAdvancedFilmManager.isEditMode());
                DefaultCameraModule.this.mExtraPrevewUIManager.setEditMode(DefaultCameraModule.this.mAdvancedFilmManager.isEditMode());
            } else if (DefaultCameraModule.this.isMenuShowing(16) && DefaultCameraModule.this.mStickerManager.isRunning()) {
                DefaultCameraModule.this.mExtraPrevewUIManager.setEditMode(DefaultCameraModule.this.mStickerManager.onEditClickOnOff());
            }
            return true;
        }

        public boolean onDownloadClicked() {
            try {
                Intent intent = new Intent(CameraConstantsEx.INTENT_SMART_WORLD);
                intent.putExtra(CameraConstantsEx.EXTRA_TYPE_SMART_WORLD, CameraConstantsEx.EXTRA_VALUE_SMART_WORLD_CATEGORY);
                if (DefaultCameraModule.this.isMenuShowing(16)) {
                    intent.putExtra(CameraConstantsEx.EXTRA_TYPE_CATEGORY_ID, CameraConstantsEx.EXTRA_VALUE_CATEGORY_STICKER);
                } else {
                    intent.putExtra(CameraConstantsEx.EXTRA_TYPE_CATEGORY_ID, CameraConstantsEx.EXTRA_VALUE_CATEGORY_FILTER);
                }
                DefaultCameraModule.this.getActivity().startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                CamLog.m5e(CameraConstants.TAG, "smartwolrd not found");
                return false;
            }
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$61 */
    class C023561 implements FilmEmulatorEnginInterface {
        C023561() {
        }

        public void onEngineInitializeDone(SurfaceTexture surfaceTexture) {
            if (DefaultCameraModule.this.mCameraDevice != null) {
                DefaultCameraModule.this.mGet.runOnUiThread(new HandlerRunnable(DefaultCameraModule.this) {
                    public void handleRun() {
                        if (DefaultCameraModule.this.mAdvancedFilmManager != null && DefaultCameraModule.this.mAdvancedFilmManager.isReadyToOpenFilterMenu()) {
                            int[] lcdSize = Utils.getLCDsize(DefaultCameraModule.this.getAppContext(), true);
                            DefaultCameraModule.this.mGet.getCurPreviewBitmap((int) (((float) lcdSize[1]) * 0.5f), (int) (((float) lcdSize[0]) * 0.5f));
                            DefaultCameraModule.this.mGet.setPreviewCoverVisibility(0, false, null, true, true);
                        }
                    }
                });
                if (CameraConstants.MODE_MANUAL_VIDEO.equals(DefaultCameraModule.this.getShotMode()) || DefaultCameraModule.this.mGet.isVideoCaptureMode()) {
                    DefaultCameraModule.this.mAdvancedFilmManager.changePreviewSize(DefaultCameraModule.this.mParamUpdater.getParamValue(ParamConstants.KEY_PREVIEW_SIZE), true);
                } else if (DefaultCameraModule.this.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    int[] lcdSize = Utils.getLCDsize(DefaultCameraModule.this.getAppContext(), true);
                    DefaultCameraModule.this.mAdvancedFilmManager.changePreviewSize(lcdSize[1] + "x" + lcdSize[1], false);
                } else {
                    DefaultCameraModule.this.mAdvancedFilmManager.changePreviewSize(DefaultCameraModule.this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(DefaultCameraModule.this.getShotMode(), DefaultCameraModule.this.mCameraId)).getExtraInfo(2), false);
                }
                if (!(CameraConstants.MODE_SMART_CAM.equals(DefaultCameraModule.this.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(DefaultCameraModule.this.getShotMode()))) {
                    DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_FILM_TYPE, DefaultCameraModule.this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR));
                }
                DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_FILM_ENABLE, "true");
                DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_FILM_STRENGTH, DefaultCameraModule.this.mAdvancedFilmManager.getFilmStrengthValue() + "");
                if (FunctionProperties.isAppyingFilmLimitation()) {
                    DefaultCameraModule.this.mParamUpdater.setParamValue("hdr-mode", "0");
                }
                if (DefaultCameraModule.this.isFastShotSupported() && DefaultCameraModule.this.mCameraDevice != null) {
                    DefaultCameraModule.this.mCameraDevice.setLongshot(false);
                }
                DefaultCameraModule.this.startPreview(null, surfaceTexture);
                DefaultCameraModule.this.setCameraState(1);
                DefaultCameraModule.this.mGet.runOnUiThread(new HandlerRunnable(DefaultCameraModule.this) {
                    public void handleRun() {
                        if (!(DefaultCameraModule.this.mZoomManager == null || DefaultCameraModule.this.mCameraDevice == null)) {
                            CameraParameters parameters = DefaultCameraModule.this.mCameraDevice.getParameters();
                            if (parameters != null) {
                                DefaultCameraModule.this.setZoomCompensation(parameters);
                                DefaultCameraModule.this.mZoomManager.initZoomBarValue(parameters.getZoom());
                            }
                            DefaultCameraModule.this.mZoomManager.initInAndOutZoomBarValue();
                        }
                        if (DefaultCameraModule.this.mFocusManager != null && DefaultCameraModule.this.isFocusEnableCondition()) {
                            DefaultCameraModule.this.mFocusManager.registerCallback();
                        }
                        if (DefaultCameraModule.this.mCaptureButtonManager != null) {
                            DefaultCameraModule.this.mCaptureButtonManager.setShutterButtonEnable(true, 3);
                        }
                        if (DefaultCameraModule.this.mExtraPrevewUIManager != null) {
                            DefaultCameraModule.this.mExtraPrevewUIManager.changeButtonState(0, DefaultCameraModule.this.isMenuShowing(4));
                        }
                        DefaultCameraModule.this.enableControls(true);
                        if (!(DefaultCameraModule.this.isRearCamera() || DefaultCameraModule.this.mLightFrameManager == null)) {
                            if (DefaultCameraModule.this.mLightFrameManager.isLightFrameMode()) {
                                DefaultCameraModule.this.mLightFrameManager.setBacklightToMax(DefaultCameraModule.this.getActivity());
                            } else {
                                DefaultCameraModule.this.mLightFrameManager.setBacklightToSystemSetting(DefaultCameraModule.this.getActivity());
                            }
                        }
                        DefaultCameraModule.this.mGet.removeCameraChanging(2);
                    }
                });
                CamLog.m3d(CameraConstants.TAG, "#startPreview - end");
            }
        }

        public void onEnginReleased(boolean isRestartPreview, boolean isStopByRecording) {
            if (isRestartPreview) {
                DefaultCameraModule.this.onFilmEngineReleased(isRestartPreview, isStopByRecording);
            }
            DefaultCameraModule.this.mGet.removeCameraChanging(2);
            DefaultCameraModule.this.runOnUiThread(new HandlerRunnable(DefaultCameraModule.this) {
                public void handleRun() {
                    if (FunctionProperties.isAppyingFilmLimitation() && "1".equals(DefaultCameraModule.this.getSettingValue("hdr-mode"))) {
                        DefaultCameraModule.this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
                    }
                    if (DefaultCameraModule.this.mQuickButtonManager != null) {
                        DefaultCameraModule.this.mQuickButtonManager.refreshButtonEnable(100, true, true);
                    }
                }
            });
        }

        public void onFilmEffectChanged(String filmValue) {
            boolean z = true;
            if (DefaultCameraModule.this.mCameraDevice != null) {
                boolean isSmartcamMode;
                if (CameraConstants.MODE_SMART_CAM.equals(DefaultCameraModule.this.getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(DefaultCameraModule.this.getShotMode())) {
                    isSmartcamMode = true;
                } else {
                    isSmartcamMode = false;
                }
                DefaultCameraModule.this.mGet.setSetting(isSmartcamMode ? Setting.KEY_SMART_CAM_FILTER : Setting.KEY_FILM_EMULATOR, filmValue, !isSmartcamMode);
                DefaultCameraModule.this.restoreSteadyCamSetting();
                DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, DefaultCameraModule.this.mGet.getCurSettingValue(Setting.KEY_VIDEO_STEADY));
                DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_FILM_TYPE, filmValue);
                DefaultCameraModule.this.mParamUpdater.setParamValue(ParamConstants.KEY_FILM_STRENGTH, DefaultCameraModule.this.mAdvancedFilmManager.getFilmStrengthValue() + "");
                CameraParameters parameters = DefaultCameraModule.this.mCameraDevice.getParameters();
                if (FunctionProperties.isAppyingFilmLimitation()) {
                    DefaultCameraModule.this.setSpecificSettingValueAndDisable("hdr-mode", "0", false);
                    if (DefaultCameraModule.this.isRearCamera()) {
                        ListPreference flashPref = DefaultCameraModule.this.getListPreference("flash-mode");
                        if (flashPref != null) {
                            String savedValue = flashPref.loadSavedValue();
                            DefaultCameraModule.this.setSettingMenuEnable("flash-mode", true);
                            DefaultCameraModule.this.setParamUpdater(parameters, "flash-mode", savedValue);
                            DefaultCameraModule.this.setFlashMetaDataCallback(savedValue);
                            DefaultCameraModule.this.setSetting("flash-mode", savedValue, true);
                            DefaultCameraModule.this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
                        } else {
                            return;
                        }
                    }
                }
                DefaultCameraModule.this.setFilmParameters(parameters, filmValue);
                DefaultCameraModule.this.mCameraDevice.setParameters(parameters);
                if (DefaultCameraModule.this.mExtraPrevewUIManager != null && !CameraConstants.MODE_SMART_CAM.equals(DefaultCameraModule.this.getShotMode()) && !CameraConstants.MODE_SMART_CAM_FRONT.equals(DefaultCameraModule.this.getShotMode())) {
                    ExtraPreviewUIManager extraPreviewUIManager = DefaultCameraModule.this.mExtraPrevewUIManager;
                    if (CameraConstants.FILM_NONE.equals(filmValue)) {
                        z = false;
                    }
                    extraPreviewUIManager.setStrengthBarVisibility(z, false);
                }
            }
        }

        public void onFilmMenuHandleDone() {
            DefaultCameraModule.this.mGet.removeCameraChanging(2);
            if (!DefaultCameraModule.this.isMenuShowing(4)) {
                DefaultCameraModule.this.showFlashHDRIndicators(false);
                if (!DefaultCameraModule.this.isRearCamera()) {
                    DefaultCameraModule.this.resumeShutterless();
                }
            }
            if (DefaultCameraModule.this.mQuickButtonManager != null) {
                DefaultCameraModule.this.mQuickButtonManager.refreshButtonEnable(100, true, true);
            }
        }

        public void updateFilterQuickButton(boolean updateQuickButton) {
            if (DefaultCameraModule.this.mQuickButtonManager != null && updateQuickButton) {
                CamLog.m3d(CameraConstants.TAG, "updateFilmEmulatorButton");
                DefaultCameraModule.this.mQuickButtonManager.updateButtonIcon(C0088R.id.quick_button_film_emulator, DefaultCameraModule.this.mQuickButtonManager.getFilmQuickButtonSelector(), DefaultCameraModule.this.mQuickButtonManager.getFilmQuickButtonPressedImage());
                DefaultCameraModule.this.mQuickButtonManager.updateButton(C0088R.id.quick_button_film_emulator);
                DefaultCameraModule.this.mQuickButtonManager.refreshButtonEnable(100, true, true);
            }
        }

        public void onFilterItemLongPressed() {
            DefaultCameraModule.this.mExtraPrevewUIManager.setEditMode(true);
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$6 */
    class C02476 implements DotIndicatorInterface {
        C02476() {
        }

        public boolean filterMenuAvailable() {
            return DefaultCameraModule.this.isSupportedFilterMenu();
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$71 */
    class C024971 implements StickerContentCallback {
        C024971() {
        }

        public void onContentTaken(final ContentsInformation ci) {
            if (ci != null) {
                DefaultCameraModule.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (ci.isPicture()) {
                            CameraProxy cd = DefaultCameraModule.this.getCameraDevice();
                            DefaultCameraModule.this.mShutterCallback.onShutter(cd);
                            DefaultCameraModule.this.mPictureCallback.onPictureTaken(ci.getPictureData(), null, cd);
                        } else if (ci.isVideoError()) {
                            DefaultCameraModule.this.restoreRecorderToIdleOnUIThread();
                            DefaultCameraModule.this.mToastManager.showShortToast(DefaultCameraModule.this.getActivity().getString(DefaultCameraModule.this.isMiracastState() ? C0088R.string.error_video_recording_during_miracast : C0088R.string.error_occurred));
                            AudioUtil.setAllSoundCaseMute(DefaultCameraModule.this.getAppContext(), false);
                        } else if (ci.isRecordinEndAndWaitSave()) {
                            DefaultCameraModule.this.onVideoStopClicked(false, false);
                        }
                    }
                });
            }
        }
    }

    /* renamed from: com.lge.camera.app.DefaultCameraModule$7 */
    class C02517 implements CameraMetaDataCallback {
        C02517() {
        }

        public void onCameraMetaData(byte[] data, Camera camera) {
            int i = 0;
            if (data.length >= 1 && DefaultCameraModule.this.mZoomManager != null) {
                CamLog.m7i(CameraConstants.TAG, "Camera Switch Callback : " + data[0]);
                DefaultCameraModule defaultCameraModule = DefaultCameraModule.this;
                if (data[0] != (byte) 2) {
                    i = 2;
                }
                defaultCameraModule.mDetectedCameraIdByOpticZoom = i;
                DefaultCameraModule.this.mGet.runOnUiThread(DefaultCameraModule.this.mDetectCameraChangeRunnable);
            }
        }
    }

    protected class CameraPictureCallbackInRecording extends CameraPictureCallback {
        protected CameraPictureCallbackInRecording() {
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            Module.sFirstTaken = true;
            DefaultCameraModule.this.onPictureTakenCallbackInRecording(data, extraExif, camera);
        }
    }

    protected class FilmLiveSnapShotThread extends Thread {
        protected FilmLiveSnapShotThread() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[Film] FilmLiveSnapShotThread - start");
            if (DefaultCameraModule.this.mParamUpdater != null) {
                int[] size;
                String previewSize = DefaultCameraModule.this.mParamUpdater.getParamValue(ParamConstants.KEY_PREVIEW_SIZE);
                if (ModelProperties.isLongLCDModel()) {
                    size = Utils.getLCDsize(DefaultCameraModule.this.getAppContext(), true);
                } else {
                    size = Utils.sizeStringToArray(previewSize);
                }
                Bitmap previewBitmap = Utils.getScreenShot(size[0], size[1], true, 3);
                if (previewBitmap != null) {
                    previewBitmap = DefaultCameraModule.this.cropFlimLiveSnapShotBitmap(previewBitmap);
                }
                ByteArrayOutputStream previewStream = new ByteArrayOutputStream();
                if (previewBitmap != null) {
                    DebugUtil.setStartTime("[1] Film snapshot image: jpeg compression");
                    previewBitmap.compress(CompressFormat.JPEG, 80, previewStream);
                    DebugUtil.setEndTime("[1] Film snapshot image: jpeg compression");
                    DefaultCameraModule.this.saveImage(previewStream.toByteArray(), null);
                    CamLog.m3d(CameraConstants.TAG, "[Film] - jpeg compression - DONE");
                    try {
                        previewStream.close();
                    } catch (Exception e) {
                        Log.e(CameraConstants.TAG, "[Film] - mPreviewStream.close() failed");
                    }
                    previewBitmap.recycle();
                }
                if (DefaultCameraModule.this.mSnapShotChecker != null) {
                    DefaultCameraModule.this.mSnapShotChecker.setSnapShotState(0);
                }
                CamLog.m3d(CameraConstants.TAG, "[Film] FilmLiveSnapShotThread - end");
            }
        }
    }

    public final class MediaRecorderListener implements OnInfoListener, OnErrorListener {
        public void onInfo(MediaRecorder mr, int what, int extra) {
            DefaultCameraModule.this.recorderOnInfo(mr, what, extra);
        }

        public void onError(MediaRecorder mr, int what, int extra) {
            DefaultCameraModule.this.recorderOnError(mr, what, extra);
        }
    }

    protected class ShutterCallbackInRecording implements CameraShutterCallback {
        protected ShutterCallbackInRecording() {
        }

        public void onShutter(CameraProxy camera) {
            if (DefaultCameraModule.this.checkModuleValidate(128)) {
                CamLog.m7i(CameraConstants.TAG, "ShutterCallbackInRecording : camera state is not recording state.");
            } else {
                DefaultCameraModule.this.onShutterCallback(true, true, true);
            }
        }
    }

    class StopPreviewThread extends Thread {
        StopPreviewThread() {
        }

        public void run() {
            DefaultCameraModule.this.stopPreview();
        }
    }

    public DefaultCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
        if (FunctionProperties.isSupportedQrCode(getAppContext())) {
            this.mQRCodeManager = new QRCodeManager(this);
        }
        this.mLongShotSync.open();
    }

    public void init() {
        super.init();
        if (FunctionProperties.isSupportedFilmEmulator() && !isAnimationShowing()) {
            if (this.mGet.getBeforeMode() == null || !this.mGet.getBeforeMode().contains(CameraConstants.MODE_SMART_CAM) || (!getShotMode().contains(CameraConstants.MODE_BEAUTY) && !"mode_normal".equals(getShotMode()))) {
                if (CameraConstants.FILM_NONE.equals(this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR)) || !("mode_normal".equals(getShotMode()) || ManualUtil.isManualCameraMode(getShotMode()) || ManualUtil.isManualVideoMode(getShotMode()) || getShotMode().contains(CameraConstants.MODE_BEAUTY) || getShotMode().contains(ParamConstants.KEY_OUTFOCUS) || getShotMode().contains(CameraConstants.MODE_SMART_CAM) || getShotMode().contains(CameraConstants.MODE_FLASH_JUMPCUT))) {
                    this.mGet.movePreviewOutOfWindow(false);
                }
            }
        }
    }

    public void onAudioFocusChange(int focusChange) {
        CamLog.m3d(CameraConstants.TAG, "AudiofocusChange : " + focusChange + ", mCameraState : " + this.mCameraState);
        switch (focusChange) {
            case -1:
                if (this.mCameraState == 6 || this.mCameraState == 5) {
                    AudioUtil.enableRaM(getAppContext(), false);
                    return;
                } else if ("on".equals(getSettingValue(Setting.KEY_VOICESHUTTER)) && this.mCheeseShutterManager != null) {
                    setSetting(Setting.KEY_VOICESHUTTER, "off", true);
                    childSettingMenuClicked(Setting.KEY_VOICESHUTTER, "off");
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    public void initUI(View baseParent) {
        if (isGestureShutterEnableCondition()) {
            this.mGestureShutterManager = new GestureShutterManager(this, isIntervalShotEnableCondition());
        }
        if (isIntervalShotEnableCondition()) {
            this.mIntervalShotManager = new IntervalShotManager(this);
        }
        super.initUI(baseParent);
        if ((!FunctionProperties.isSupportedConeUI() || checkCurrentConeMode(1)) && this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(1);
        }
    }

    public void onPauseBefore() {
        waitPrepareStartRecorderThread();
        waitStartRecorderThread();
        if (!checkModuleValidate(128)) {
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateVideoTime(3, SystemClock.uptimeMillis());
                this.mRecordingUIManager.setRecDurationTime((long) getRecCompensationTime());
            }
            onVideoStopClicked(false, false);
        }
        if (!checkModuleValidate(64)) {
            boolean z;
            String str = CameraConstants.TAG;
            StringBuilder append = new StringBuilder().append("recording wait state, handle stop recording thread, mStopRecordingThread is null ? ");
            if (this.mStopRecordingThread == null) {
                z = true;
            } else {
                z = false;
            }
            CamLog.m3d(str, append.append(z).toString());
            CamLog.m3d(CameraConstants.TAG, "mStopRecordingThread is alive ? " + (this.mStopRecordingThread == null ? false : this.mStopRecordingThread.isAlive()));
            waitStopRecordingThread();
        }
        this.mPictureCallbackLock.block(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
        CamLog.m3d(CameraConstants.TAG, "onPauseBefore mPictureCallbackLock opened");
        if (this.mIsNeedFinishAfterSaving) {
            this.mGet.stopQuickShotModeByBackkey();
        }
        if (this.mSnapShotChecker.checkMultiShotState(2)) {
            stopIntervalShot(0);
        }
        this.mSnapShotChecker.removeBurstState(96);
        stopBurstShotTaking(false);
        this.mGifManager.setGifVisibility(false);
        this.mGifManager.setGifVisibleStatus(false);
        if (this.mFilmEmulatorStartHandler != null) {
            removePostRunnable(this.mFilmEmulatorStartHandler);
        }
        this.isNeedHideDetailView = false;
        if (FunctionProperties.isSupportedQrCode(getAppContext()) && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR))) {
            setPreviewCallbackAll(false);
        }
        onStopQRCodeClicked();
        setQRLayoutVisibility(8);
        if (this.mStickerManager != null && isStickerSupportedCameraMode()) {
            setPreviewCallbackAll(false);
        }
        super.onPauseBefore();
        String newMode = this.mGet.getCurSettingValue(Setting.KEY_MODE);
        if (!("mode_normal".equals(newMode) || getShotMode().contains(CameraConstants.MODE_BEAUTY) || CameraConstants.MODE_SMART_CAM.equals(newMode) || CameraConstants.MODE_SMART_CAM_FRONT.equals(newMode))) {
            this.mGet.setUspVisibility(8);
        }
        setRecordingPreviewState(0, true);
        if (this.mStickerManager != null && this.mStickerManager.isGLSurfaceViewShowing() && !this.mStickerManager.hasSticker()) {
            hideStickerMenu(true);
        }
    }

    protected boolean onSurfaceDestroyed(SurfaceHolder holder) {
        waitStartRecorderThread();
        if (!checkModuleValidate(128)) {
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateVideoTime(3, SystemClock.uptimeMillis());
                this.mRecordingUIManager.setRecDurationTime((long) getRecCompensationTime());
            }
            onVideoStopClicked(false, false);
        }
        if (!checkModuleValidate(64)) {
            waitStopRecordingThread();
        }
        return super.onSurfaceDestroyed(holder);
    }

    public void onPauseAfter() {
        CamLog.m3d(CameraConstants.TAG, "onPauseAfter - start");
        this.mAllowPause = true;
        this.mSnapShotChecker.resetSnapShotChecker();
        setOpticZoomCallback(false);
        setNightVisionDataCallback(false);
        if (isPaused()) {
            this.mShowBinningToastCondition = true;
            handleBinningIconUI(false, 0);
        }
        this.mLowLightState = -1;
        this.mBinningManager.resetManuallyOffState();
        resetToNormalSettingFromBinning();
        super.onPauseAfter();
        if (this.mAdvancedFilmManager != null && getFilmState() == 1) {
            this.mAdvancedFilmManager.filmEmulatorStopDone();
        }
        this.mIsNeedToCheckFlashTemperature = true;
        if (isAvailableManualFocus(false)) {
            setManualManualFocusMetaDataCallback(false);
        }
    }

    public void onStop() {
        if (this.mGet.isPaused()) {
            this.mGet.setFrontFlashOff();
            if (FunctionProperties.isSupportedFilmEmulator() && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                this.mGet.movePreviewOutOfWindow(false);
            }
        }
        AudioUtil.setAudioFocus(getAppContext(), false);
        AudioUtil.setAudioFocusChangeListener(null);
        super.onStop();
    }

    public void onDestroy() {
        this.mCaptureData = null;
        this.mDirectCallbackManager.release();
        super.onDestroy();
    }

    protected void setupPreview(CameraParameters params) {
        if (this.mPostviewManager == null || !this.mPostviewManager.isPostviewShowing()) {
            CamLog.m3d(CameraConstants.TAG, "setupPreview");
            this.mSnapShotChecker.removeBurstState(1);
            if (checkToUseFilmEffect()) {
                startPreviewWithFilmEffect(params);
            } else if (this.mIsSwitchingCameraDuringRecording) {
                ListPreference listPref = getListPreference(getVideoSizeSettingKey());
                if (listPref != null) {
                    String videoSize = listPref.getValue();
                    if (videoSize != null) {
                        String previewSize = listPref.getExtraInfo(1);
                        String screenSize = listPref.getExtraInfo(2);
                        sVideoSize = Utils.sizeStringToArray(screenSize);
                        setDefaultRecordingParameters(params, videoSize, true);
                        this.mParamUpdater.setParameters(params, ParamConstants.KEY_VIDEO_SIZE, videoSize);
                        this.mParamUpdater.setParameters(params, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
                        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, getPreviewSize(previewSize, screenSize, videoSize), false, true);
                        setParameterByLGSF(params, true);
                        updateHDRParam(params, "0", false, false);
                        setHDRMetaDataCallback(null);
                        setFlashMetaDataCallback(null);
                        startPreview(params);
                        CamLog.m3d(CameraConstants.TAG, "Set camera state = " + this.mRecordingStateBeforeChangingCamera);
                        setCameraState(this.mRecordingStateBeforeChangingCamera);
                    }
                }
            } else {
                startPreview(params);
                setCameraState(1);
            }
        }
    }

    protected void startPreviewWithFilmEffect(CameraParameters params) {
        CamLog.m3d(CameraConstants.TAG, "startPreviewWithFilmEffect");
        if (!checkModuleValidate(192) && ((CameraConstants.FILM_NONE.equals(this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR)) || isUHDmode() || isFHD60() || !FunctionProperties.isSupportedFilmRecording()) && !CameraConstants.MODE_SMART_CAM.equals(getShotMode()) && !CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()))) {
            if (!(isLightFrameOn() || this.mGet.getPreviewCoverVisibility() != 0 || this.mCameraState == 5)) {
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DefaultCameraModule.this.mGet.setPreviewCoverVisibility(8, false, null, true, true);
                    }
                });
            }
            this.mAdvancedFilmManager.stopFilmEmulator(true, true);
        } else if (this.mAdvancedFilmManager.getFilmSurfaceTexture() != null) {
            CamLog.m3d(CameraConstants.TAG, "surface Texture is not null, so reuse texture");
            boolean isRecordingState = !checkModuleValidate(192);
            startPreview(params, this.mAdvancedFilmManager.getFilmSurfaceTexture());
            if (isRecordingState || CameraConstants.MODE_MANUAL_VIDEO.equals(getShotMode()) || isVideoAttachMode()) {
                this.mAdvancedFilmManager.changePreviewSize(this.mParamUpdater.getParamValue(ParamConstants.KEY_PREVIEW_SIZE), true);
            } else if (getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
                this.mAdvancedFilmManager.changePreviewSize(lcdSize[1] + "x" + lcdSize[1], false);
            } else {
                this.mAdvancedFilmManager.changePreviewSize(this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId)).getExtraInfo(2), false);
            }
            setCameraState(1);
        } else if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(85);
        }
    }

    protected void stopPreview() {
        if (ModelProperties.isMTKChipset() && this.mFocusManager != null) {
            this.mFocusManager.stopFaceDetection();
        }
        if (this.mStickerManager != null) {
            setPreviewCallbackAll(false);
        }
        if (FunctionProperties.isSupportedQrCode(getAppContext()) && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR))) {
            setPreviewCallbackAll(false);
        } else {
            this.mDirectCallbackManager.release();
        }
        super.stopPreview();
    }

    protected boolean checkToUseFilmEffect() {
        if (!FunctionProperties.isSupportedFilmEmulator() || this.mIsSwitchingCameraDuringRecording || this.mAdvancedFilmManager == null) {
            return false;
        }
        if (this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            return true;
        }
        String filmValue = this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR);
        if (CameraConstants.FILM_NONE.equals(filmValue)) {
            return this.mAdvancedFilmManager.isSetDownloadFilm(filmValue);
        }
        if (!filmValue.contains("sdcard") || FileManager.isFileExist(filmValue)) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "[Film] Download filter was deleted, so change to normal");
        setSetting(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, true);
        SharedPreferenceUtilBase.saveLastSelectFilter(getAppContext(), CameraConstants.FILM_NONE);
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mPostviewManager != null && !this.mPostviewManager.onTouchEvent(event)) {
            return super.onTouchEvent(event);
        }
        if (this.mIsPreviewCallbackWaiting || this.mGet.isCameraChanging()) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mPostviewManager == null || !this.mPostviewManager.onKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            case 4:
                requestFocusOnShutterButton(true, true);
                return true;
            default:
                return true;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isPostviewShowing() || !this.mPostviewManager.onKeyUp(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        switch (keyCode) {
            case 24:
            case 25:
                requestFocusOnShutterButton(true, true);
                return true;
            default:
                return true;
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (checkModuleValidate(128)) {
            doCleanView(false, false, false);
        } else {
            if (this.mQuickButtonManager != null) {
                if (this.mFocusManager.getIsOnlyTAF()) {
                    this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 0, false);
                } else {
                    this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 8, false);
                }
            }
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.show(isNeedProgressBar());
                if (sVideoSize != null) {
                    this.mGet.setTextureLayoutParams(sVideoSize[0], sVideoSize[1], -1);
                }
            }
            if ((isUHDmode() || !isLiveSnapshotSupported()) && this.mCaptureButtonManager != null) {
                this.mCaptureButtonManager.changeExtraButton(0, 1);
                this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
            }
        }
        stopBurstShotTaking(false);
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (!checkModuleValidate(128)) {
            setMicPath();
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mPostviewManager != null) {
            return this.mPostviewManager.onPrepareOptionsMenu(menu);
        }
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mPostviewManager != null) {
            return this.mPostviewManager.onOptionsItemSelected(item);
        }
        return false;
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (FunctionProperties.isSupportedVoiceShutter()) {
            this.mManagerList.add(this.mCheeseShutterManager);
        }
        this.mManagerList.add(this.mPostviewManager);
        this.mManagerList.add(this.mRecordingUIManager);
        this.mManagerList.add(this.mAudioZoomManager);
        this.mManagerList.add(this.mGestureShutterManager);
        this.mManagerList.add(this.mIntervalShotManager);
        if (this.mQRCodeManager != null) {
            this.mManagerList.add(this.mQRCodeManager);
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (isRecordingSingleZoom() && checkDoubleCameraAvailableMode(false)) {
            replaceZoomManager(false);
        }
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
            setVoiceShutter(false, 0);
        }
        if (AudioUtil.sAudioFocusChangeLister == null) {
            AudioUtil.setAudioFocusChangeListener(this);
        }
        this.mShowBinningToastCondition = true;
        this.mIsNeedFinishAfterSaving = false;
        this.mWaitSavingDialogType = 0;
        this.mPictureCallbackLock.open();
    }

    protected void onChangeModuleAfter() {
        super.onChangeModuleAfter();
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
            setVoiceShutter(false, 0);
        } else {
            setVoiceShutter(true, 0);
        }
        if (isMenuShowing(3)) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (DefaultCameraModule.this.checkModuleValidate(1)) {
                        DefaultCameraModule.this.mGet.removeSettingMenu(false, true);
                        DefaultCameraModule.this.mGet.hideModeMenu(true, true);
                        DefaultCameraModule.this.mGet.hideHelpList(true, true);
                    }
                }
            }, 500);
        }
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onChangeModuleAfter();
        }
    }

    protected void setManagersListener() {
        super.setManagersListener();
        if (this.mCheeseShutterManager != null) {
            this.mCheeseShutterManager.setVoiceRecogListener(this.mVoiceRecogListener);
        }
        if (this.mGestureShutterManager != null) {
            this.mGestureShutterManager.setGestureRecogEngineListener(this.mGestureRecogListener);
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setSelfieQuickViewListener(this.mSelfieQuickViewListener);
            this.mReviewThumbnailManager.setQuickViewListener(this.mQuickViewListener);
        }
        if (this.mAudioZoomManager != null) {
            this.mAudioZoomManager.setRecorderListener(new C02113());
        }
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.setRecordingInterface(new C02224());
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setFilmEmulatorEnginInterface(this.mFilmEmulatorEnginInterface);
        }
        setDotIndicatorManagerListener();
        setLeftSwipeMenuManagerListener();
    }

    private void setLeftSwipeMenuManagerListener() {
        if (this.mExtraPrevewUIManager != null) {
            this.mExtraPrevewUIManager.setExtraPreviewUIListener(new C02335());
        }
    }

    private void setDotIndicatorManagerListener() {
        if (this.mDotIndicatorManager != null) {
            this.mDotIndicatorManager.setDotIndicatorListener(new C02476());
        }
    }

    protected void initializeAfterCameraOpen() {
        super.initializeAfterCameraOpen();
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.initializeAfterCameraOpen();
            this.mAdvancedFilmManager.setFilmEmulatorEnginInterface(this.mFilmEmulatorEnginInterface);
        }
        setTrackingAFListener();
        this.mFlipManager.setPreProcessSupported(this.mCameraCapabilities.isPictureFlipSupported(), this.mCameraCapabilities.isVideoFlipSupported());
        if (isAvailableManualFocus(true)) {
            setManualManualFocusMetaDataCallback(true);
        }
    }

    private void setOpticZoomCallback(boolean isSet) {
        if (isOpticZoomSupported(null) && this.mCameraDevice != null) {
            CamLog.m7i(CameraConstants.TAG, "setOpticZoomCallback : " + isSet);
            if (isSet) {
                this.mOpticZoomCallback = new C02517();
                this.mCameraDevice.setOpticZoomMetadataCb(this.mOpticZoomCallback);
                return;
            }
            this.mCameraDevice.setOpticZoomMetadataCb(null);
            this.mOpticZoomCallback = null;
        }
    }

    protected void enableControls(boolean enable) {
        enableControls(enable, true);
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.refreshButtonEnable(100, enable, changeColor);
        }
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
            setCaptureButtonEnable(false, 1);
        }
        if (this.mGet.isLGUOEMCameraIntent()) {
            setCaptureButtonEnable(false, 1);
        }
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.setConeClickable(enable);
        }
        if (this.mDoubleCameraManager != null && !this.mIsGoingToPostview) {
            DoubleCameraManager doubleCameraManager = this.mDoubleCameraManager;
            if (this.mGet.isAnimationShowing()) {
                enable = false;
            }
            doubleCameraManager.setDualViewControlEnabled(enable);
        }
    }

    protected void initializeSettingMenus() {
        this.mGet.setAllSettingMenuEnable(true);
        if (FunctionProperties.isSupportedFilmEmulator()) {
            if ("on".equals(getSettingValue(Setting.KEY_STICKER))) {
                CamLog.m3d(CameraConstants.TAG, "filter setting clear for sticker");
                setSetting(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, true);
            }
            if (!(CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR)) || this.mExtraPrevewUIManager == null)) {
                this.mExtraPrevewUIManager.setLastSelectedMenu(0);
            }
        }
        String voiceShutterValue = getSettingValue(Setting.KEY_VOICESHUTTER);
        if (TelephonyUtil.phoneInCall(getAppContext()) || !MDMUtil.allowMicrophone()) {
            setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
        }
        if ("on".equals(voiceShutterValue)) {
            this.mGet.setSetting(Setting.KEY_VOICESHUTTER, voiceShutterValue, true);
        }
        String hdrValue = getSettingValue("hdr-mode");
        if (!"not found".equals(hdrValue) && "1".equals(hdrValue)) {
            if (!FunctionProperties.isAppyingFilmLimitation() || CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                setSetting("flash-mode", "off", false);
                setSettingMenuEnable("flash-mode", false);
            } else {
                restoreFlashSetting();
            }
        }
        if (FunctionProperties.isAppyingFilmLimitation() && !CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))) {
            setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        }
        restoreVideoSizeSetting();
        restoreSteadyCamSetting();
        restoreTilePreviewSetting();
        requestFocusOnShutterButton(true);
        restoreTrackingAFSetting();
        restoreBinningSetting();
        if (this.mGet.isVoiceAssistantSpecified()) {
            String pictureSizeSpecified = this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_PICTURE_SIZE, null);
            if (pictureSizeSpecified != null) {
                float[] size = Utils.sizeStringToFloatArray(pictureSizeSpecified);
                if (Float.compare(size[0] / size[1], 2.0f) == 0) {
                    setSetting(Setting.KEY_TILE_PREVIEW, "off", true);
                }
            }
        }
    }

    protected void restoreVideoSizeSetting() {
        if (this.mGet.isFromFloatingBar()) {
            String key = SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId);
            ListPreference listPref = getListPreference(key);
            if (listPref != null) {
                String currentValue = listPref.getValue();
                String savedValue = listPref.loadSavedValue();
                if (currentValue != null && !currentValue.equals(savedValue)) {
                    listPref.setValue(savedValue);
                    this.mGet.setAllSettingChildMenuEnable(key, true);
                }
            }
        }
    }

    protected void restoreSettingMenus() {
        if ("1".equals(getSettingValue("hdr-mode"))) {
            ListPreference listPref = getListPreference("flash-mode");
            if (listPref != null) {
                setSetting("flash-mode", listPref.loadSavedValue(), false);
            }
        }
        if (FunctionProperties.isAppyingFilmLimitation() && !CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))) {
            restoreSettingValue("hdr-mode");
        }
    }

    protected void setPreviewLayoutParam() {
        super.setPreviewLayoutParam();
        if (this.mCameraState == 6 && isMMSRecording()) {
            ListPreference listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            if (listPref != null) {
                int[] size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
                this.mGet.setTextureLayoutParams(size[0], size[1], -1);
            }
        }
    }

    protected int setPictureOrientation(CameraParameters param) {
        int cameraDegree = getOrientationDegree();
        if (this.mDisplayOrientation == -1) {
            return cameraDegree;
        }
        cameraDegree = CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), Utils.restoreDegree(this.mGet.getAppContext().getResources(), cameraDegree), this.mCameraId);
        CamLog.m3d(CameraConstants.TAG, "setRotation [" + cameraDegree + "]");
        param.setRotation(cameraDegree);
        return cameraDegree;
    }

    protected void setParameterBeforeTakePicture(CameraParameters param, boolean useBurst, boolean forceUpdate) {
        boolean isUseParam = false;
        CamLog.m3d(CameraConstants.TAG, "setParameterBeforeTakePicture - start");
        if (param != null && forceUpdate) {
            try {
                int cameraDegree = setPictureOrientation(param);
                if (!useBurst || forceUpdate) {
                    if (this.mLocationServiceManager != null) {
                        CamLog.m3d(CameraConstants.TAG, "setLocation = " + this.mLocationServiceManager.getRecordLocation());
                        Location loc = this.mLocationServiceManager.getRecordLocation() ? this.mLocationServiceManager.getCurrentLocation() : null;
                        this.mLocationServiceManager.setGPSlocation(param, loc);
                        this.mCameraDevice.setGPSlocation(loc);
                    }
                    param.set(ParamConstants.KEY_EXIF_DATE, Utils.getCurrentDateTime(System.currentTimeMillis()));
                }
                if (useBurst && this.mFocusManager != null) {
                    if (!(this.mFocusManager.isAELock() || this.mFocusManager.isFocusLock() || isManualFocusModeEx())) {
                        if (this.mCameraCapabilities.isAFSupported()) {
                            this.mFocusManager.hideAndCancelAllFocus(false);
                            param.setFocusMode("auto");
                        }
                        CameraDeviceUtils.setEnable3ALocks(this.mCameraDevice, false, false);
                    }
                    forceUpdate = true;
                }
                param = this.mFlipManager.setPictureFlipParam(this.mCameraId, param, cameraDegree);
                if (!useBurst && checkModuleValidate(128)) {
                    setParamUpdater(param, "flash-mode", getSettingValue("flash-mode"));
                }
                boolean isSmartcamMode = CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode());
                setFilmParameters(param, this.mGet.getCurSettingValue(isSmartcamMode ? Setting.KEY_SMART_CAM_FILTER : Setting.KEY_FILM_EMULATOR));
                String signatureValue = "off";
                if (isHALSignatureCaptureMode() && isSignatureEnableCondition()) {
                    signatureValue = "on";
                }
                CamLog.m3d(CameraConstants.TAG, "set signature param value, value = " + signatureValue);
                param.set(ParamConstants.KEY_SIGNATURE_ENABLE, signatureValue);
                if (!(useBurst && !forceUpdate && this.mLongshotJpegDegree == cameraDegree)) {
                    isUseParam = true;
                }
                if (checkModuleValidate(128)) {
                    if (!isUseParam) {
                        param = this.mCameraDevice.getParameters();
                    }
                    String shotMode = getShotMode().contains(CameraConstants.MODE_SQUARE) ? "mode_normal" : getShotMode();
                    if (useBurst) {
                        shotMode = CameraConstants.MODE_BURST;
                    }
                    setParameterByLGSF(param, shotMode, false);
                } else if (isUseParam) {
                    setParameters(param);
                }
                this.mLongshotJpegDegree = cameraDegree;
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "setParameterBeforeTakePicture is failed : ", e);
            }
        }
    }

    protected void setFilmParameters(CameraParameters param, String filmValue) {
        if (FunctionProperties.isSupportedFilmEmulator()) {
            if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                param.set(ParamConstants.KEY_FILM_ENABLE, "false");
            } else if (filmValue.equals(CameraConstants.FILM_NONE)) {
                param.set(ParamConstants.KEY_FILM_ENABLE, "false");
            } else {
                param.set(ParamConstants.KEY_FILM_ENABLE, "true");
                param.set(ParamConstants.KEY_FILM_STRENGTH, this.mAdvancedFilmManager.getFilmStrengthValue());
            }
            param.set(ParamConstants.KEY_FILM_TYPE, filmValue);
        }
    }

    public String getShotMode() {
        return "mode_normal";
    }

    protected void setParameterByLGSF(CameraParameters parameters, boolean isRecording) {
        setParameterByLGSF(parameters, getShotMode(), isRecording);
    }

    protected void setParameterByLGSF(CameraParameters parameters, String shotMode, boolean isRecording) {
        CamLog.m3d(CameraConstants.TAG, "setParameterByLGSF - start");
        if (this.mCameraDevice != null) {
            CameraParameters changedParam = null;
            if (isRecording) {
                this.mCameraDevice.setNightandHDRorAutoSync(parameters, shotMode, isRecording);
            } else {
                setBurstWBForAPI2(true, shotMode, parameters);
                changedParam = this.mCameraDevice.setNightandHDRorAuto(parameters, shotMode, isRecording);
            }
            if (!(changedParam == null || isRecording)) {
                this.mNeedProgressDuringCapture = 0;
                if ("1".equals(changedParam.get("hdr-mode"))) {
                    this.mNeedProgressDuringCapture |= 1;
                }
                if (ParamConstants.SCENE_MODE_NIGHT.equals(changedParam.getSceneMode())) {
                    this.mNeedProgressDuringCapture |= 16;
                }
                if ("on".equals(changedParam.get(ParamConstants.KEY_SUPERZOOM))) {
                    this.mNeedProgressDuringCapture |= 256;
                }
                LdbUtil.setSceneMode(this.mNeedProgressDuringCapture);
            }
            this.mSnapShotChecker.setLGSFParamState(1);
            CamLog.m3d(CameraConstants.TAG, "setParameterByLGSF - end");
        }
    }

    private void setBurstWBForAPI2(boolean isStart, String shotMode, CameraParameters parameters) {
        if (FunctionProperties.getSupportedHal() == 2 && !isRearCamera() && CameraConstants.MODE_BURST.equals(shotMode)) {
            CamLog.m3d(CameraConstants.TAG, " Temp code : awb " + (isStart ? " no set " : " set"));
            parameters.set("lg-wb", isStart ? ParamConstants.PARAM_VALUE_NOSET : "0");
        }
    }

    public void onQueueStatus(boolean full) {
        boolean buttonEnable;
        CamLog.m3d(CameraConstants.TAG, "onQueueStatus : is full = " + full);
        if (full) {
            buttonEnable = false;
        } else {
            buttonEnable = true;
        }
        if (!this.mSnapShotChecker.checkMultiShotState(5)) {
            if (this.mSnapShotChecker.isAvailableNightAndFlash() && checkModuleValidate(16) && this.mFocusManager != null && this.mFocusManager.checkFocusStateForChangingSetting() && !(isLGUOEMCameraIntent() && buttonEnable)) {
                setCaptureButtonEnable(buttonEnable, 3);
            }
            if ((TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone() || this.mGet.isLGUOEMCameraIntent()) && checkModuleValidate(192)) {
                setCaptureButtonEnable(false, 1);
            }
        }
    }

    public boolean isStillBurstShotSaving() {
        return this.mSnapShotChecker.isStillSavingBurstShot();
    }

    public void onImageSaverQueueStatus(int count) {
        CamLog.m3d(CameraConstants.TAG, "onImageSaverQueueStatus : queue count = " + count);
        if (count > 0) {
            this.mSnapShotChecker.setStillSavingBurstShot(true);
        } else {
            this.mSnapShotChecker.setStillSavingBurstShot(false);
        }
    }

    public void onQueueStatus(int count) {
        CamLog.m3d(CameraConstants.TAG, "onQueueStatus : current queue count = " + count + " Progress type = " + this.mNeedProgressDuringCapture);
        if (this.mNeedProgressDuringCapture > 0) {
            if (isLivePhotoEnabled()) {
                this.mLivePhotoManager.setLivePhotoSavingProgress(true);
            }
            this.mNeedProgressDuringCapture = 0;
            showSavingDialog(false, 0);
            this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
            this.mWaitSavingDialogType = 0;
            if (this.mIsNeedFinishAfterSaving) {
                this.mIsNeedFinishAfterSaving = false;
                if (!this.mGet.unselectUspOnBackKey()) {
                    getActivity().finish();
                }
            }
        } else if (count == 0) {
            this.mNeedProgressDuringCapture = 0;
            showSavingDialog(false, 0);
            this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    switch (DefaultCameraModule.this.mWaitSavingDialogType) {
                        case 1:
                            if (DefaultCameraModule.this.checkModuleValidate(1)) {
                                DefaultCameraModule.this.mGet.getActivity().finish();
                                break;
                            }
                            break;
                        case 2:
                            if (DefaultCameraModule.this.mReviewThumbnailManager != null) {
                                DefaultCameraModule.this.mReviewThumbnailManager.launchGallery(0);
                                break;
                            }
                            break;
                        case 4:
                            DefaultCameraModule.this.mGifManager.executeGifMake();
                            break;
                    }
                    DefaultCameraModule.this.mWaitSavingDialogType = 0;
                }
            });
        }
    }

    protected boolean takePicture() {
        if (!checkModuleValidate(223) || getCameraState() != 1) {
            this.mSnapShotChecker.setSnapShotState(0);
            return false;
        } else if (this.mStickerManager != null && this.mStickerManager.isStickerDrawing() && !this.mStickerManager.isStickerLoadCompleted()) {
            CamLog.m5e(CameraConstants.TAG, "skip take picutre because sticker is not loaded");
            return false;
        } else if (this.mCameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera Device is null.");
            this.mSnapShotChecker.setSnapShotState(0);
            return false;
        } else {
            if (this.mSnapShotChecker.checkMultiShotState(4)) {
                if (this.mSnapShotChecker.isBurstCountMax(getCurStorage() == 0, true, isRearCamera())) {
                    if (this.mCaptureButtonManager != null) {
                        this.mCaptureButtonManager.setShutterButtonPressed(false, 2);
                    }
                    this.mSnapShotChecker.setMultiShotState(8);
                    stopBurstShotTaking(false);
                    this.mSnapShotChecker.setSnapShotState(0);
                    return false;
                }
            }
            if (!checkFocusStateForTakingPicture()) {
                return false;
            }
            if (!(this.mStorageManager == null || isAvailableTakePicture())) {
                checkStorage(1, getCurStorage(), true);
                hideMenu(CameraConstants.MENU_TYPE_ALL, false, false);
                enableControls(true);
                if (this.mReviewThumbnailManager != null) {
                    this.mReviewThumbnailManager.setEnabled(true);
                }
                if (this.mFocusManager != null) {
                    this.mFocusManager.releaseTouchFocus();
                }
            }
            if (!(AudioUtil.isWiredHeadsetOn() || AudioUtil.isBluetoothA2dpOn() || this.mSnapShotChecker.checkMultiShotState(6) || QuickWindowUtils.isQuickWindowCameraMode())) {
                AudioUtil.setAudioFocus(getAppContext(), true);
            }
            checkInterval(3);
            onTakePictureBefore();
            doTakePicture();
            return true;
        }
    }

    protected boolean checkFocusStateForTakingPicture() {
        if (this.mCameraCapabilities == null || !this.mCameraCapabilities.isAFSupported() || this.mFocusManager == null) {
            return true;
        }
        this.mFocusManager.releaseHandlerBeforeTakePicture();
        if (this.mFocusManager.checkEnableTakePicture() || this.mSnapShotChecker.checkMultiShotState(4) || this.mTimerManager == null || this.mTimerManager.isTimerShotCountdown() || this.mFocusManager.isManualFocusMode()) {
            return true;
        }
        if (this.mFocusManager.doFocusCaf()) {
            onTakePictureBefore();
        }
        return false;
    }

    protected void onTakePictureBefore() {
        CamLog.m3d(CameraConstants.TAG, "onTakePictureBefore - start");
        this.mSnapShotChecker.setSnapShotState(2);
        super.onTakePictureBefore();
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.setConeClickable(false);
        }
        hideMenusOnTakePictureBefore();
        if (!isMenuShowing(20)) {
            showFrameGridView(getGridSettingValue(), false);
        }
        if (isFastShotSupported() && (!this.mSnapShotChecker.isAvailableTakePictureBefore() || this.mSnapShotChecker.getLGSFParamState() != 0)) {
            CamLog.m3d(CameraConstants.TAG, "[SKIP] - doTakePictureBefore : pictureCallbackState = " + this.mSnapShotChecker.getPictureCallbackState() + ", LGSF state = " + this.mSnapShotChecker.getLGSFParamState());
        } else if (!(isFastShotAvailable(3) || this.mDoubleCameraManager == null || this.mQuickButtonManager == null || this.mReviewThumbnailManager == null || this.mCaptureButtonManager == null || this.mExtraPrevewUIManager == null)) {
            this.mDoubleCameraManager.setDualViewControlEnabled(false);
            this.mQuickButtonManager.setEnable(100, false);
            this.mReviewThumbnailManager.setEnabled(false);
            this.mExtraPrevewUIManager.setAllButtonsEnable(false);
            setCaptureButtonEnable(false, getShutterButtonType());
            this.mCaptureButtonManager.setExtraButtonEnable(false, 3);
        }
        if (this.mZoomManager != null) {
            this.mZoomManager.stopDrawingExceedsLevel();
        }
        if (this.mFocusManager != null) {
            this.mFocusManager.setEVshutterButtonEnable(false);
            this.mFocusManager.setAEControlBarEnable(false);
            if (this.mManualFocusManager != null) {
                this.mFocusManager.setManualFocusButtonEnable(false);
                this.mManualFocusManager.setManualFocusViewEnable(false);
            }
        }
        hideManagerForSelfTimer(true);
        CamLog.m3d(CameraConstants.TAG, "onTakePictureBefore - end");
    }

    protected void hideMenusOnTakePictureBefore() {
        if (this.mStickerManager == null || !this.mStickerManager.hideMenuOnTakePictureBefore()) {
            hideMenu(1003, false, false);
        } else {
            hideMenu(PointerIconCompat.TYPE_ZOOM_OUT, false, false);
        }
        setNightVisionDataCallback(false);
        if (isTimerShotCountdown()) {
            if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isShowingFilmMenu()) {
                if (this.mHandler != null) {
                    this.mHandler.removeMessages(83);
                }
                hideMenu(4, false, false);
            }
            if (this.mQRCodeManager != null) {
                this.mQRCodeManager.setQRLayoutVisibility(8);
            }
            handleBinningIconUI(false, 0);
        }
    }

    protected void onTakePictureAfter() {
        CamLog.m3d(CameraConstants.TAG, "onTakePictureAfter - start");
        this.mSnapShotChecker.setPictureCallbackState(3);
        super.onTakePictureAfter();
        this.mGet.showTilePreviewCoverView(false);
        if (!this.mSnapShotChecker.checkMultiShotState(6)) {
            if (!(this.mDoubleCameraManager == null || this.mIsGoingToPostview || isAttachIntent() || isMenuShowing(CameraConstants.MENU_TYPE_ALL) || isTimerShotCountdown())) {
                showDoubleCamera(true);
                setDoubleCameraEnable(true);
            }
            if (this.mQuickButtonManager != null) {
                if (isMenuShowing(20)) {
                    this.mQuickButtonManager.refreshButtonEnable(100, true, false);
                } else {
                    this.mQuickButtonManager.updateButton(100);
                }
            }
            if (this.mExtraPrevewUIManager != null) {
                this.mExtraPrevewUIManager.setAllButtonsEnable(true);
            }
            if (this.mCaptureButtonManager != null) {
                setCaptureButtonEnable(true, getShutterButtonType());
                requestFocusOnShutterButton(true);
                Log.d(CameraConstants.TAG, "TIME CHECK : Shot to Shot [END] - onTakePictureAfter, shutter button will be enabled ");
                if (FunctionProperties.isSupportedConeUI() && !this.mIsGoingToPostview && !isAttachIntent() && this.mGet.checkModuleValidate(192)) {
                    this.mGet.enableConeMenuIcon(31, true);
                }
                this.mCaptureButtonManager.setExtraButtonEnable(true, 3);
                if (!(isFastShotSupported() || this.mSnapShotChecker.checkMultiShotState(4))) {
                    this.mCaptureButtonManager.setShutterButtonPressed(false, 2);
                }
                if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
                    setCaptureButtonEnable(false, 1);
                }
                setButtonEnableForLGUOEMCamera(false);
                if (this.mFocusManager != null) {
                    if (!(this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock() || isAFSupported() || isAttachIntent())) {
                        this.mFocusManager.releaseFocusHandler();
                        this.mFocusManager.releaseTouchFocus();
                    }
                    this.mFocusManager.setAEControlBarEnable(true);
                    this.mFocusManager.setManualFocusButtonEnable(true);
                    this.mFocusManager.setEVshutterButtonEnable(true);
                }
                if (this.mManualFocusManager != null) {
                    this.mManualFocusManager.setManualFocusViewEnable(true);
                }
            }
        }
        setFilmStrengthButtonVisibility(true, false);
        showManagerForSelfTimer();
        this.mSnapShotChecker.setPictureCallbackState(0);
        this.mIsMTKFlashFired = false;
        handleBinningIconUI(true, 0);
        this.mShowBinningToastCondition = true;
        sendLDBIntentOnTakePictureAfter();
        CamLog.m3d(CameraConstants.TAG, "onTakePictureAfter - end");
    }

    protected void sendLDBIntentOnTakePictureAfter() {
        sendLDBIntentAfterContentsCreated(LdbConstants.LDB_FEATURE_NAME_NORMAL_CAMERA, 1, false);
    }

    protected void sendLDBIntentOnAfterStopRecording() {
        sendLDBIntentAfterContentsCreated(LdbConstants.LDB_FEATURE_NAME_NORMAL_VIDEO, 2, true);
    }

    protected void sendLDBIntentOnLoopRecording() {
        String loopingType = "";
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            loopingType = LdbConstants.LDB_LOOP_RECORDING_NONE;
        } else {
            loopingType = LdbConstants.LDB_LOOP_RECORDING_FILM;
        }
        LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_LOOP_RECORDING, -1, loopingType);
    }

    protected void sendLDBIntentAfterContentsCreated(final String ldbKey, final int category, final boolean isRecording) {
        if (!this.mTimerManager.isTimerShotCountdown() && !this.mTimerManager.getIsGesureTimerShotProgress() && !this.mSnapShotChecker.checkMultiShotState(3)) {
            if (LdbUtil.skipNextLdbBroadcast()) {
                LdbUtil.setSkipNextLdbBroadcast(false);
            } else {
                new Thread(new Runnable() {
                    public void run() {
                        CamLog.m3d(CameraConstants.TAG, "sendLDBIntentAfterContentsCreated [START]");
                        LdbUtil.sendLDBIntent(DefaultCameraModule.this.getAppContext(), ldbKey, -1, DefaultCameraModule.this.getLdbStringExtra(ldbKey, category, isRecording));
                        CamLog.m3d(CameraConstants.TAG, "sendLDBIntentAfterContentsCreated [END]");
                    }
                }).start();
            }
        }
    }

    private String getLdbStringExtra(String ldbKey, int category, boolean isRecording) {
        String str = "";
        return (getLdbCommonString(isRecording) + getLdbSettingString(category)) + getLDBNonSettingString();
    }

    private String getLdbCommonString(boolean isRecording) {
        String strExtra = "";
        String shotMode = getShotMode();
        if (getShotMode().contains(CameraConstants.MODE_BEAUTY)) {
            shotMode = CameraConstants.MODE_BEAUTY;
        }
        strExtra = (((strExtra + "key_mode=" + shotMode + ";") + "shutter_type=" + LdbUtil.getShutterType() + ";") + "camera_id=" + LdbUtil.getLDBCameraId(getAppContext(), isRearCamera()) + ";") + "zoom=" + this.mParamUpdater.getParamValue("zoom") + ";";
        if (this.mFocusManager != null && this.mFocusManager.isAEControlBarEnableCondition()) {
            strExtra = strExtra + "ev_control=" + this.mFocusManager.getCurrentValue() + ";";
        }
        if (!isRecording) {
            strExtra = strExtra + "picture-size=" + getSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId)) + ";";
            return !isManualMode() ? strExtra + "multi_shot=" + LdbUtil.getMultiShotState() + ";" : strExtra;
        } else if (isManualMode()) {
            return strExtra + "key_video_recordsize=" + getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE) + ";";
        } else {
            return strExtra + "key_video_recordsize=" + getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)) + ";";
        }
    }

    private String getLdbSettingString(int category) {
        String res = "";
        PreferenceGroup prefGroup = this.mGet.getPreferenceGroup();
        if (prefGroup == null) {
            return res;
        }
        for (int i = 0; i < prefGroup.size(); i++) {
            CameraPreference pref = prefGroup.get(i);
            if (pref != null && (pref instanceof ListPreference)) {
                ListPreference listPref = (ListPreference) pref;
                String key = listPref.getKey();
                if (this.mGet.getSettingMenuEnable(key) && (LdbUtil.getCategory(key) & category) == category) {
                    String value = listPref.getValue();
                    if (!(key == null || value == null)) {
                        String mappedKey = LdbUtil.getLDBFeatureName(key);
                        if (mappedKey != null) {
                            if (mappedKey.equals(LdbConstants.LDB_FEAT_NAME_FILM_EMULATOR)) {
                                res = res + mappedKey + "=" + LdbUtil.getLDBFilterName(value) + ";";
                            } else {
                                res = res + mappedKey + "=" + value + ";";
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    protected String getLDBNonSettingString() {
        String extraStr = "";
        if (this.mBinningManager != null && this.mGet.getSettingMenuEnable(Setting.KEY_BINNING)) {
            extraStr = extraStr + "night_vision_enabled=" + this.mBinningManager.isBinningEnabled() + ";";
        }
        if (this.mStickerManager == null || !this.mStickerManager.isStickerDrawing()) {
            return extraStr;
        }
        return extraStr + this.mStickerManager.getLDBInfo();
    }

    protected void setButtonEnableForLGUOEMCamera(boolean isBackkey) {
        if (!this.mGet.isLGUOEMCameraIntent()) {
            return;
        }
        if (isBackkey) {
            setCaptureButtonEnable(true, 2);
            setCaptureButtonEnable(false, 1);
            return;
        }
        setCaptureButtonEnable(false, 3);
    }

    protected void doTakePicture() {
        Log.d(CameraConstants.TAG, "TIME CHECK : Shot to Shot [START] - doTakePicture");
        if (!this.mSnapShotChecker.checkMultiShotState(4)) {
            if (!isFastShotSupported() || this.mSnapShotChecker.getLGSFParamState() == 0 || this.mSnapShotChecker.getLGSFParamState() == 2) {
                setParameterBeforeTakePicture(updateDeviceParameter(), this.mSnapShotChecker.checkMultiShotState(4), true);
            } else {
                CamLog.m3d(CameraConstants.TAG, "[SKIP] - setParameterBeforeTakePicture  : pictureCallbackState = " + this.mSnapShotChecker.getPictureCallbackState() + ", LGSF state = " + this.mSnapShotChecker.getLGSFParamState());
            }
        }
        if (this.mSnapShotChecker.checkMultiShotState(4) && this.mSnapShotChecker.isBurstCaptureStarted() && this.mReviewThumbnailManager != null) {
            updateBurstCount(false, false);
            this.mReviewThumbnailManager.startBurstCaptureEffect(true);
        }
        Log.d(CameraConstants.TAG, "[Shot2Shot] Current Storage is " + (getCurStorage() == 0 ? "Internal." : "External"));
        this.mGet.delayCursorUpdate();
        this.mSnapShotChecker.setSnapShotState(3);
        if (this.mStickerManager == null || !this.mStickerManager.isStickerDrawing()) {
            this.mCameraDevice.takePicture(this.mHandler, this.mShutterCallback, this.mRAWPictureCallback, null, this.mPictureCallback);
        } else if ("on".equals(getSettingValue(Setting.KEY_SIGNATURE))) {
            int[] viewportSize = this.mStickerManager.getViewportSize();
            if (viewportSize != null) {
                this.mStickerManager.takePicture(this.mStickerContentCallback, "on".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION)), this.mGet.getSignatureBitmap(viewportSize[0], viewportSize[1], Integer.MIN_VALUE));
            } else {
                this.mStickerManager.takePicture(this.mStickerContentCallback, "on".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION)), null);
            }
        } else {
            this.mStickerManager.takePicture(this.mStickerContentCallback, "on".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION)), null);
        }
    }

    protected void takePictureLongShot(boolean isInternalStorage) {
        long curTime = System.currentTimeMillis() - this.mLastLongShotTime;
        if (!FunctionProperties.isSupportedFastShot() && !ModelProperties.isMTKChipset() && (!this.mLongShotShutterCallbackReceived || curTime < ((long) FunctionProperties.getSupportedBurstShotDuration(isInternalStorage, false, isRearCamera())))) {
            return;
        }
        if (!checkModuleValidate(79) || this.mSnapShotChecker.isBurstCountMax(isInternalStorage, false, isRearCamera())) {
            if (this.mButtonCheckTimer != null) {
                this.mButtonCheckTimer.cancel();
                this.mButtonCheckTimer.purge();
                this.mButtonCheckTimer = null;
            }
            this.mSnapShotChecker.removeBurstState(64);
            this.mGet.postOnUiThread(this.mStopBurstShotRunnable);
            return;
        }
        this.mLongShotSync.close();
        this.mLongShotShutterCallbackReceived = false;
        this.mLastLongShotTime = System.currentTimeMillis();
        if (this.mReviewThumbnailManager != null && !QuickWindowUtils.isQuickWindowCameraMode() && this.mSnapShotChecker.checkBurstState(4) && this.mSnapShotChecker.isBurstCaptureStarted()) {
            this.mGet.postOnUiThread(this.mUpdateBurstCountRunnable);
        }
        this.mSnapShotChecker.setSnapShotState(3);
        this.mSnapShotChecker.setBurstState(64);
        this.mCameraDevice.takePictureDirect(this.mHandler, this.mLongShotShutterCallback, null, null, this.mLongShotPictureCallback);
        this.mLongShotSync.open();
    }

    protected boolean doLongShotPictureCallback(byte[] data, CameraProxy camera) {
        if (this.mSnapShotChecker.isBurstCountMax(false, false, isRearCamera())) {
            return false;
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setIsCompleteSaving(false);
        }
        if (data == null) {
            CamLog.m3d(CameraConstants.TAG, "error!! BurstShot abnormal jpegData stream");
            stopBurstShotTaking(false);
            return false;
        }
        int captureCount = this.mSnapShotChecker.increaseBurstCount();
        CamLog.m3d(CameraConstants.TAG, "###LongShot onPictureTaken : " + captureCount);
        if (captureCount == 1) {
            try {
                this.mFirstBurstData = Arrays.copyOf(data, data.length);
            } catch (Exception e) {
                e.printStackTrace();
                this.mFirstBurstData = null;
            }
        }
        if (((long) data.length) < this.mFreeSpace) {
            if (!QuickWindowUtils.isQuickWindowCameraMode()) {
                updateBurstCount(true, true);
            }
            if (this.mGet.saveImageSavers(data, 0, true, captureCount)) {
                this.mFreeSpace -= (long) data.length;
                CamLog.m3d(CameraConstants.TAG, "burstshot free space : " + this.mFreeSpace);
                if (isSupportedQuickClip()) {
                    this.mQuickClipManager.setQuickClipLongshot(true);
                }
            }
            return true;
        }
        this.mStorageManager.doHandleStorageFull(1, getCurStorage(), true);
        this.mStorageManager.setRemainingPictureCount(-1);
        stopBurstShotTaking(false);
        return false;
    }

    protected void updateBurstCount(boolean callFromCallback, boolean useBurst) {
        int captureCount = this.mSnapShotChecker.getBurstCount();
        if (callFromCallback) {
            this.mUpdateCount = captureCount;
            this.mBeforeCaptureCount = captureCount;
            this.mBurstCountUpdateRunnable.run();
        } else if (useBurst) {
            long startTime = System.currentTimeMillis();
            int interval = FunctionProperties.getSupportedBurstShotDuration(false, false, isRearCamera());
            if (this.mCheckInterval > 0 && startTime - this.mCheckInterval >= ((long) interval) && this.mUpdateCount == captureCount && this.mUpdateCount < FunctionProperties.getSupportedBurstShotMaxCount(false, false, isRearCamera()) && this.mBurstCountAvailable) {
                this.mUpdateCount++;
                this.mBurstCountUpdateRunnable.run();
            }
        } else if (this.mBeforeCaptureCount == captureCount) {
            int i = this.mBeforeCaptureCount;
            this.mBeforeCaptureCount = i + 1;
            this.mUpdateCount = i;
            this.mBurstCountUpdateRunnable.run();
        }
        this.mCheckInterval = System.currentTimeMillis();
    }

    public ModuleInterface getModuleInterface() {
        return this;
    }

    protected void takePictureInRecording() {
        if (this.mCameraDevice == null) {
            this.mSnapShotChecker.setSnapShotState(0);
            return;
        }
        if ((this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator()) && !CameraConstants.MODE_CINEMA.equals(getShotMode())) {
            setParameterBeforeTakePicture(this.mCameraDevice.getParameters(), false, true);
            this.mSnapShotChecker.setSnapShotState(3);
            this.mCameraDevice.takePicture(this.mHandler, new ShutterCallbackInRecording(), null, null, new CameraPictureCallbackInRecording());
        } else if (this.mFilmLiveSnapShotThread == null || !this.mFilmLiveSnapShotThread.isAlive()) {
            this.mSnapShotChecker.setSnapShotState(3);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DefaultCameraModule.this.doSnapshotEffect(true, 0.6f, 300);
                }
            });
            this.mFilmLiveSnapShotThread = new FilmLiveSnapShotThread();
            this.mFilmLiveSnapShotThread.start();
        } else {
            return;
        }
        LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_LIVE_SNAPSHOT, -1, "key_mode=" + getShotMode() + ";");
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    protected android.graphics.Bitmap cropFlimLiveSnapShotBitmap(android.graphics.Bitmap r14) {
        /*
        r13 = this;
        r10 = r13.getVideoSizeSettingKey();
        r8 = r13.getSettingValue(r10);
        if (r8 != 0) goto L_0x000c;
    L_0x000a:
        r0 = r14;
    L_0x000b:
        return r0;
    L_0x000c:
        r9 = com.lge.camera.util.Utils.sizeStringToArray(r8);
        r10 = r13.getAppContext();
        r11 = 1;
        r4 = com.lge.camera.util.Utils.getLCDsize(r10, r11);
        r10 = 0;
        r10 = r9[r10];
        r10 = (float) r10;
        r11 = 1;
        r11 = r9[r11];
        r11 = (float) r11;
        r7 = r10 / r11;
        r10 = 0;
        r10 = r4[r10];
        r10 = (float) r10;
        r11 = 1;
        r11 = r4[r11];
        r11 = (float) r11;
        r10 = r10 / r11;
        r10 = java.lang.Float.compare(r7, r10);
        if (r10 != 0) goto L_0x0034;
    L_0x0032:
        r0 = r14;
        goto L_0x000b;
    L_0x0034:
        if (r9 == 0) goto L_0x00f6;
    L_0x0036:
        r10 = r9.length;
        r11 = 2;
        if (r10 < r11) goto L_0x00f6;
    L_0x003a:
        r0 = r14;
        r1 = r14.getHeight();
        r2 = r14.getWidth();
        r6 = 0;
        r5 = 0;
        r10 = 1075000115; // 0x40133333 float:2.3 double:5.31120626E-315;
        r10 = (r7 > r10 ? 1 : (r7 == r10 ? 0 : -1));
        if (r10 <= 0) goto L_0x0092;
    L_0x004c:
        r10 = com.lge.camera.constants.ModelProperties.isLongLCDModel();
        if (r10 == 0) goto L_0x0082;
    L_0x0052:
        r10 = r13.getAppContext();
        r5 = com.lge.camera.util.RatioCalcUtil.getQuickButtonWidth(r10);
        r10 = r14.getWidth();
        r10 = r10 - r5;
        r11 = r13.getAppContext();
        r11 = com.lge.camera.util.RatioCalcUtil.getNavigationBarHeight(r11);
        r2 = r10 - r11;
        r10 = (float) r2;
        r10 = r10 / r7;
        r1 = (int) r10;
        r6 = 0;
    L_0x006d:
        r0 = android.graphics.Bitmap.createBitmap(r14, r5, r6, r2, r1);	 Catch:{ OutOfMemoryError -> 0x00ca }
        if (r14 == 0) goto L_0x000b;
    L_0x0073:
        r10 = r0.hashCode();
        r11 = r14.hashCode();
        if (r10 == r11) goto L_0x000b;
    L_0x007d:
        r14.recycle();
        r14 = 0;
        goto L_0x000b;
    L_0x0082:
        r2 = r14.getWidth();
        r10 = (float) r2;
        r10 = r10 / r7;
        r1 = (int) r10;
        r5 = 0;
        r10 = r14.getHeight();
        r10 = r10 - r1;
        r6 = r10 / 2;
        goto L_0x006d;
    L_0x0092:
        r10 = 1071225242; // 0x3fd9999a float:1.7 double:5.29255591E-315;
        r10 = (r7 > r10 ? 1 : (r7 == r10 ? 0 : -1));
        if (r10 <= 0) goto L_0x00a9;
    L_0x0099:
        r1 = r14.getHeight();
        r10 = (float) r1;
        r10 = r10 * r7;
        r2 = (int) r10;
        r6 = 0;
        r10 = r14.getWidth();
        r10 = r10 - r2;
        r5 = r10 / 2;
        goto L_0x006d;
    L_0x00a9:
        r10 = "mode_square_snap_shot";
        r11 = r13.getShotMode();
        r10 = r10.equals(r11);
        if (r10 == 0) goto L_0x006d;
    L_0x00b5:
        r1 = r14.getHeight();
        r2 = r1;
        r10 = com.lge.camera.constants.ModelProperties.getLCDType();
        r11 = 2;
        if (r10 != r11) goto L_0x006d;
    L_0x00c1:
        r10 = r13.getAppContext();
        r5 = com.lge.camera.util.RatioCalcUtil.getQuickButtonWidth(r10);
        goto L_0x006d;
    L_0x00ca:
        r3 = move-exception;
        r10 = "CameraApp";
        r11 = "error occurred rotating image because of OutOfMemory";
        com.lge.camera.util.CamLog.m6e(r10, r11, r3);	 Catch:{ all -> 0x00e4 }
        if (r14 == 0) goto L_0x000b;
    L_0x00d4:
        r10 = r0.hashCode();
        r11 = r14.hashCode();
        if (r10 == r11) goto L_0x000b;
    L_0x00de:
        r14.recycle();
        r14 = 0;
        goto L_0x000b;
    L_0x00e4:
        r10 = move-exception;
        if (r14 == 0) goto L_0x00f5;
    L_0x00e7:
        r11 = r0.hashCode();
        r12 = r14.hashCode();
        if (r11 == r12) goto L_0x00f5;
    L_0x00f1:
        r14.recycle();
        r14 = 0;
    L_0x00f5:
        throw r10;
    L_0x00f6:
        r0 = r14;
        goto L_0x000b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.DefaultCameraModule.cropFlimLiveSnapShotBitmap(android.graphics.Bitmap):android.graphics.Bitmap");
    }

    public ImageRegisterRequest saveImageDataForImageRegister(SaveRequest sr) {
        if (sr == null) {
            return null;
        }
        int exifDegree;
        byte[] convertJpeg;
        ExifInterface exif;
        String dir = getCurDir();
        int storage = getCurStorage();
        int countMultiShot = sr.countBurstshot;
        if (countMultiShot == 1) {
            this.mIsFirstBurstShot = true;
            FileNamer.get().markTakeTime(CameraConstants.MODE_BURST);
        }
        String fileName = FileNamer.get().getFileNewNameMultiShot(getAppContext(), 0, storage, dir, false, CameraConstants.MODE_BURST, countMultiShot) + ".jpg";
        CamLog.m3d(CameraConstants.TAG, "output fileName = " + fileName);
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setLastBurstFileName(dir + fileName);
        }
        if (getBinningEnabledState() && FunctionProperties.getSupportedHal() == 2) {
            exifDegree = CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), Utils.restoreDegree(this.mGet.getAppContext().getResources(), getOrientationDegree()), this.mCameraId);
            convertJpeg = getConvertJpegDataForFilmSnapshot(sr.data, exifDegree);
            exif = getExifForNightVision(convertJpeg, exifDegree);
            Exif.updateThumbnail(exif, convertJpeg);
        } else {
            exif = Exif.readExif(sr.data);
            sr.degree = Exif.getOrientation(exif);
            convertJpeg = this.mFlipManager.checkPostProcessAndMakeJpegFlip(this.mCameraId, sr.degree, sr.data, exif);
            exifDegree = sr.degree;
        }
        updateThumbnail(exif, exifDegree, true);
        try {
            if (!FileManager.writeJpegImageToFile(convertJpeg, sr.extraExif, dir, fileName, exif)) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageRegisterRequest imgReg = new ImageRegisterRequest();
        imgReg.mDirectory = dir;
        imgReg.mFileName = fileName;
        imgReg.mDateTaken = sr.dateTaken;
        imgReg.mLocation = this.mLocationServiceManager.getCurrentLocation();
        imgReg.mDegree = exifDegree;
        imgReg.mExifSize = Exif.getExifSize(exif);
        imgReg.mIsBurstShot = true;
        imgReg.mUpdateThumbnail = sr.updateThumbnail;
        return imgReg;
    }

    public Uri insertImageContent(ImageRegisterRequest irr) {
        CamLog.m3d(CameraConstants.TAG, "insertImageContent. - start");
        Uri resultUri = FileManager.registerImageUri(this.mGet.getAppContext().getContentResolver(), irr.mDirectory, irr.mFileName, irr.mDateTaken, irr.mLocation, irr.mDegree, irr.mExifSize, irr.mIsBurstShot);
        if (this.mIsFirstBurstShot) {
            this.mFirstBurstUri = resultUri;
            this.mBurstShotFileName = irr.mFileName;
            this.mIsFirstBurstShot = false;
            if (!(this.mFirstBurstData == null || this.mFirstBurstUri == null)) {
                makeThumbnailForTilePreview(Exif.readExif(this.mFirstBurstData), this.mFirstBurstUri);
                this.mFirstBurstData = null;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "insertImageContent. - end, resultUri = " + resultUri);
        checkSavedURI(resultUri);
        return resultUri;
    }

    protected void updateThumbnail(ExifInterface exif, int exifDegree, boolean isBurst) {
        if (!isSquareGalleryBtn()) {
            Bitmap bmp = exif.getThumbnailBitmap();
            if (bmp != null && this.mReviewThumbnailManager != null) {
                this.mReviewThumbnailManager.addRequest(new UpdateThumbnailRequest(bmp, null, null, exifDegree, true, isBurst));
                this.mReviewThumbnailManager.updateThumbnail(false, false, true);
            }
        }
    }

    protected void makeThumbnailForTilePreview(ExifInterface exif, Uri savedUri) {
        if (FunctionProperties.isSupportedCameraRoll()) {
            ListPreference listPref = getListPreference(Setting.KEY_TILE_PREVIEW);
            if (listPref != null && "off".equals(listPref.loadSavedValue())) {
                CamLog.m3d(CameraConstants.TAG, "tilePreview is off");
            } else if (savedUri == null) {
                CamLog.m3d(CameraConstants.TAG, "saved Uri is null");
            } else {
                ThumbnailCache cropThumbs = LGCameraApplication.getCropCache(this.mGet.getAppContext());
                ThumbnailCache rotateThumbs = LGCameraApplication.getRotateCache(this.mGet.getAppContext());
                if (cropThumbs.get(savedUri) == null) {
                    Bitmap rotateBitmap = BitmapManagingUtil.getRotatedImage(exif.getThumbnailBitmap(), Exif.getOrientation(exif), false);
                    Bitmap scaledBitmap = BitmapManagingUtil.cropBitmap(this.mGet.getAppContext(), rotateBitmap);
                    if (savedUri != null && scaledBitmap != null && rotateBitmap != null) {
                        cropThumbs.put(savedUri, scaledBitmap);
                        rotateThumbs.put(savedUri, rotateBitmap);
                    }
                }
            }
        }
    }

    protected Uri doSaveImage(byte[] data, byte[] extraExif, String dir, String filename) {
        if (this.mCameraDevice == null) {
            return null;
        }
        byte[] convertJpeg;
        int exifDegree;
        ExifInterface exif;
        String jpgFileName = filename + ".jpg";
        boolean isLivePhotoEnabled = this.mLivePhotoManager != null && this.mLivePhotoManager.isLivePhotoEnabled();
        if (extraExif == null && this.mStickerManager != null && this.mStickerManager.isStickerDrawing() && this.mStickerManager.getJpegOrientationWithoutRemove(data.hashCode()) != -1) {
            int hashcode = data.hashCode();
            convertJpeg = (byte[]) data.clone();
            exifDegree = this.mStickerManager.getJpegOrientation(hashcode);
            exif = getExifForSticker(convertJpeg, exifDegree, hashcode);
            Exif.updateThumbnail(exif, convertJpeg);
        } else if (getBinningEnabledState() && FunctionProperties.getSupportedHal() == 2) {
            exifDegree = CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), Utils.restoreDegree(this.mGet.getAppContext().getResources(), getOrientationDegree()), this.mCameraId);
            convertJpeg = getConvertJpegDataForFilmSnapshot(data, exifDegree);
            exif = getExifForNightVision(convertJpeg, exifDegree);
            Exif.updateThumbnail(exif, convertJpeg);
        } else if (checkModuleValidate(192) || this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            exif = Exif.readExif(data);
            exifDegree = Exif.getOrientation(exif);
            convertJpeg = this.mFlipManager.checkPostProcessAndMakeJpegFlip(this.mCameraId, exifDegree, data, exif);
        } else {
            exifDegree = CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), getOrientationDegree(), this.mCameraId);
            convertJpeg = getConvertJpegDataForFilmSnapshot(data, exifDegree);
            exif = getExifForFilmSnapshot(convertJpeg);
        }
        if (!isRecordingState()) {
            if ("mode_food".equals(getShotMode())) {
                exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf((short) 16)));
            } else if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
                exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf((short) 17)));
            } else if (isLivePhotoEnabled) {
                exif.setTag(exif.buildTag(ExifInterface.TAG_SCENE_CAPTURE_TYPE, Short.valueOf((short) 20)));
            } else if (CameraConstants.MODE_SQUARE_OVERLAP.equals(getShotMode()) && FileManager.getProjectId() != null) {
                Exif.setSceneCaptureType(exif, (short) 13);
                convertJpeg = XMPWriter.InsertXMPData(convertJpeg, FileManager.getProjectId(), -1);
                CamLog.m3d(CameraConstants.TAG, "xmp write to exif " + FileManager.getProjectId());
            }
        }
        if (!isLivePhotoEnabled) {
            updateThumbnail(exif, exifDegree, false);
        }
        Uri uri = FileManager.addJpegImage(convertJpeg, extraExif, this.mGet.getAppContext().getContentResolver(), dir, jpgFileName, System.currentTimeMillis(), this.mLocationServiceManager.getCurrentLocation(), exifDegree, exif, false, isLivePhotoEnabled);
        if (!isLivePhotoEnabled) {
            checkSavedURI(uri);
            FileNamer.get().removeFileNameInSaving(dir + jpgFileName);
        }
        CamLog.m3d(CameraConstants.TAG, "Jpeg uri = " + uri);
        setLivePhotoImage(dir, filename, exifDegree, exif);
        if (!isLivePhotoEnabled) {
            makeThumbnailForTilePreview(exif, uri);
            doSaveImageAfter(uri, Utils.getCameraModeColumn(jpgFileName, getShotMode()));
        }
        if (VideoRecorder.isRecording()) {
            File file = new File(dir + jpgFileName);
            if (file != null) {
                long savedFileSize = file.length();
                CamLog.m3d(CameraConstants.TAG, "changeMaxFileSize = " + savedFileSize);
                if (savedFileSize > 0) {
                    VideoRecorder.changeMaxFileSize(savedFileSize);
                }
            }
        }
        if (isLivePhotoEnabled) {
            return uri;
        }
        checkStorage();
        return uri;
    }

    private void setLivePhotoImage(String dir, String fileName, int exifDegree, ExifInterface exif) {
        if (isLivePhotoEnabled()) {
            if (this.mReviewThumbnailManager != null) {
                this.mReviewThumbnailManager.setLaunchGalleryAvailable(false);
            }
            String jpgFileName = fileName + ".jpg";
            this.mLivePhotoManager.onImageSaved(fileName, this.mGet.getOrientationDegree());
            this.mLivePhotoManager.setImageUriInfo(this.mGet.getAppContext().getContentResolver(), dir, jpgFileName, System.currentTimeMillis(), this.mLocationServiceManager.getCurrentLocation(), exifDegree, exif, false);
        }
    }

    protected void doSaveImageAfter(Uri uri, int mode) {
        if (uri != null) {
            this.mGet.onNewItemAdded(uri, mode, null);
        }
    }

    private ExifInterface getExifForSticker(byte[] data, int degree, int hashcode) {
        if (this.mCameraCapabilities == null || this.mCameraDevice == null) {
            return Exif.readExif(data);
        }
        SupportedExif supportedExif = new SupportedExif(this.mCameraCapabilities.isFocusAreaSupported(), this.mCameraCapabilities.isFlashSupported(), this.mCameraCapabilities.isWBSupported(), this.mCameraCapabilities.isMeteringAreaSupported(), this.mCameraCapabilities.isZoomSupported(), true, true, true);
        int[] pictureSize = this.mStickerManager.getPictureSize(hashcode);
        if (pictureSize == null || degree == -1) {
            return Exif.readExif(data);
        }
        CameraParameters param = this.mCameraDevice.getParameters();
        ExifInterface exif = Exif.createExif(data, pictureSize[0], pictureSize[1], param, this.mLocationServiceManager.getCurrentLocation(), degree, -1, supportedExif, (short) 0);
        exif.setTagValue(ExifInterface.TAG_PIXEL_X_DIMENSION, Integer.valueOf(pictureSize[0]));
        exif.setTagValue(ExifInterface.TAG_PIXEL_Y_DIMENSION, Integer.valueOf(pictureSize[1]));
        return exif;
    }

    private ExifInterface getExifForNightVision(byte[] data, int degree) {
        if (this.mCameraCapabilities == null || this.mCameraDevice == null) {
            return Exif.readExif(data);
        }
        SupportedExif supportedExif = new SupportedExif(this.mCameraCapabilities.isFocusAreaSupported(), this.mCameraCapabilities.isFlashSupported(), this.mCameraCapabilities.isWBSupported(), this.mCameraCapabilities.isMeteringAreaSupported(), this.mCameraCapabilities.isZoomSupported(), true, true, true);
        int[] pictureSize = Utils.sizeStringToArray(this.mParamUpdater.getParamValue("picture-size"));
        CameraParameters param = this.mCameraDevice.getParameters();
        return Exif.createExif(data, pictureSize[0], pictureSize[1], param, this.mLocationServiceManager.getCurrentLocation(), degree, -1, supportedExif, (short) 0);
    }

    protected ExifInterface getExifForFilmSnapshot(byte[] data) {
        if (this.mCameraCapabilities == null || this.mCameraDevice == null) {
            return Exif.readExif(data);
        }
        SupportedExif supportedExif = new SupportedExif(this.mCameraCapabilities.isFocusAreaSupported(), this.mCameraCapabilities.isFlashSupported(), this.mCameraCapabilities.isWBSupported(), this.mCameraCapabilities.isMeteringAreaSupported(), this.mCameraCapabilities.isZoomSupported(), true, true, true);
        int[] pictureSize = Utils.sizeStringToArray(this.mParamUpdater.getParamValue(ParamConstants.KEY_PREVIEW_SIZE));
        int cameraDegree = CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), getOrientationDegree(), this.mCameraId);
        CameraParameters param = this.mCameraDevice.getParameters();
        short sceneCaptureType = (short) 0;
        if ("mode_food".equals(getShotMode())) {
            sceneCaptureType = (short) 16;
        } else if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            sceneCaptureType = (short) 17;
        }
        return Exif.createExif(data, pictureSize[0], pictureSize[1], param, this.mLocationServiceManager.getCurrentLocation(), cameraDegree, -1, supportedExif, sceneCaptureType);
    }

    protected byte[] getConvertJpegDataForFilmSnapshot(byte[] data, int exifDegree) {
        byte[] convertData = data;
        if (isRearCamera()) {
            return convertData;
        }
        boolean needConvertJpeg = false;
        boolean isFlip = false;
        int convertDegree = 0;
        if (!this.mFlipManager.isNeedFlip(this.mCameraId)) {
            needConvertJpeg = true;
            isFlip = true;
        }
        if (exifDegree == 90 || exifDegree == 270) {
            needConvertJpeg = true;
            convertDegree = 180;
        }
        if (!needConvertJpeg) {
            return convertData;
        }
        ByteArrayOutputStream jpegRotated = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapManagingUtil.makeFlipBitmap(data, isFlip, exifDegree);
        if (bitmap != null) {
            bitmap = BitmapManagingUtil.getRotatedImage(bitmap, convertDegree, false);
            if (bitmap != null) {
                bitmap.compress(CompressFormat.JPEG, 80, jpegRotated);
                bitmap.recycle();
            }
        }
        return jpegRotated.toByteArray();
    }

    protected void doSaveImagePostExecute(Uri uri) {
        if (uri != null) {
            this.mGet.requestNotifyNewMediaonActivity(uri, checkModuleValidate(128));
        }
    }

    protected void saveImage(final byte[] data, final byte[] extraExif) {
        CamLog.m3d(CameraConstants.TAG, "saveImage - start");
        if (this.mGet.getMediaSaveService() != null && !this.mGet.getMediaSaveService().isQueueFull()) {
            this.mGet.getMediaSaveService().processLocal(new OnLocalSaveByTimeListener() {
                public void onPreExecute() {
                }

                public void onPostExecute(Uri uri) {
                    DefaultCameraModule.this.doSaveImagePostExecute(uri);
                }

                public Uri onLocalSave(String markTime) {
                    String dir = DefaultCameraModule.this.getCurDir();
                    CamLog.m3d(CameraConstants.TAG, "-filename- onLocalSaveByTime dir = " + dir + ", markTime = " + markTime);
                    return DefaultCameraModule.this.doSaveImage(data, extraExif, dir, DefaultCameraModule.this.getFileNameByTime(true, dir, markTime));
                }
            }, FileNamer.get().getTakeTime(getSettingValue(Setting.KEY_MODE)));
        }
    }

    protected void checkSavedURI(Uri uri) {
        if (uri == null) {
            CamLog.m3d(CameraConstants.TAG, "checkSaveURI : uri is null.");
            if (this.mReviewThumbnailManager != null) {
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DefaultCameraModule.this.mReviewThumbnailManager.restoreThumbnail(true);
                        DefaultCameraModule.this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
                    }
                });
            }
            if (this.mToastManager != null && isAvailableTakePicture()) {
                this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
            }
        }
    }

    protected String checkRawFileNameSync(String dir) {
        if ("on".equals(getSettingValue(Setting.KEY_RAW_PICTURE)) && this.mSnapShotChecker.getRawPicState() == 3) {
            this.mRAWJPGFileNameSyncNum++;
            CamLog.m3d(CameraConstants.TAG, "mRAWJPGFileNameSyncNum=" + this.mRAWJPGFileNameSyncNum);
            if (this.mRAWJPGFileNameSyncNum == 2) {
                CamLog.m3d(CameraConstants.TAG, "Use same filename for RAW+JPG, mFileName=" + this.mFileName);
                return this.mFileName;
            }
        }
        return null;
    }

    protected synchronized String getFileName(String dir) {
        return getFileName(dir, ".jpg");
    }

    protected synchronized String getFileName(String dir, String ext) {
        return getFileNameByTime(false, dir, FileNamer.get().getTakeTime(getSettingValue(Setting.KEY_MODE)), ext);
    }

    protected synchronized String getFileNameByTime(boolean addFileSavingList, String dir, String markTime) {
        return getFileNameByTime(addFileSavingList, dir, markTime, ".jpg");
    }

    protected synchronized String getFileNameByTime(boolean addFileSavingList, String dir, String markTime, String ext) {
        String checkRawSyncName;
        int useType = 0;
        synchronized (this) {
            checkRawSyncName = checkRawFileNameSync(dir);
            if (checkRawSyncName == null) {
                int storage = getCurStorage();
                String shotMode = getSettingValue(Setting.KEY_MODE);
                String hdrSettingValue = getSettingValue("hdr-mode");
                if ("1".equals(hdrSettingValue) || ("2".equals(hdrSettingValue) && 1 == (this.mNeedProgressDuringCapture & 1))) {
                    shotMode = CameraConstants.MODE_HDR_PICTURE;
                }
                if (getCameraState() == 6 || getCameraState() == 7) {
                    shotMode = "mode_normal";
                }
                if (CameraConstants.CAM_RAW_EXTENSION.equals(ext)) {
                    useType = 2;
                }
                this.mFileName = makeFileName(useType, storage, dir, markTime, false, shotMode);
                CamLog.m3d(CameraConstants.TAG, "mFileName=" + this.mFileName);
                if (addFileSavingList) {
                    FileNamer.get().addFileNameInSaving(dir + this.mFileName + ".jpg");
                }
                checkRawSyncName = this.mFileName;
            }
        }
        return checkRawSyncName;
    }

    public String makeFileName(int useType, int storage, String dir, boolean useThread, String shotMode) {
        return makeFileName(useType, storage, dir, FileNamer.get().getTakeTime(shotMode), useThread, shotMode);
    }

    public String makeFileName(int useType, int storage, String dir, String markTime, boolean useThread, String shotMode) {
        return FileNamer.get().getFileNewName(getAppContext(), useType, storage, dir, markTime, useThread, shotMode);
    }

    public void doAfterSaveImageSavers(Uri uri, boolean updateThumbnail) {
        if (uri != null) {
            CamLog.m3d(CameraConstants.TAG, "doAfterSaveImageSavers : " + uri);
            this.mGet.requestNotifyNewMediaonActivity(uri, updateThumbnail);
            this.mGifManager.addUri(uri);
        }
    }

    public boolean stopBurstShotTaking(boolean skip) {
        if (!this.mSnapShotChecker.checkBurstState(64)) {
            return stopBurstShot(skip);
        }
        this.mSnapShotChecker.setBurstState(32);
        CamLog.m3d(CameraConstants.TAG, "waiting last burst shot...");
        if (!this.mSnapShotChecker.checkMultiShotState(1) || this.mSnapShotChecker.isBurstCaptureStarted() || !skip) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "Do Not Stop BurstShot Taking.");
        this.mSnapShotChecker.setBurstState(10);
        return true;
    }

    protected boolean stopBurstShot(boolean skip) {
        CamLog.m3d(CameraConstants.TAG, "stopBurstShotTaking, isBurstProgress = " + this.mSnapShotChecker.checkMultiShotState(1) + ", isShotToShotProgress = " + this.mSnapShotChecker.checkMultiShotState(4));
        if (this.mSnapShotChecker.checkMultiShotState(1) && !this.mSnapShotChecker.isBurstCaptureStarted() && skip) {
            CamLog.m3d(CameraConstants.TAG, "Do Not Stop BurstShot Taking.");
            this.mSnapShotChecker.setBurstState(10);
            return true;
        }
        this.mBurstCountAvailable = false;
        if (this.mUpdateBurstCountRunnable != null) {
            this.mGet.removePostRunnable(this.mUpdateBurstCountRunnable);
        }
        if (this.mButtonCheckTimer != null) {
            this.mButtonCheckTimer.cancel();
            this.mButtonCheckTimer.purge();
            this.mButtonCheckTimer = null;
        }
        if (this.mSnapShotChecker.checkMultiShotState(5)) {
            this.mGet.postOnUiThread(this.mBurstCountInvisibilityRunnable, (long) (FunctionProperties.isSupportedLongShot(isRearCamera()) ? CameraConstants.BURST_COUNT_INVISIBLE_DELAY : 1500));
            if (this.mFocusManager != null) {
                if (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock()) {
                    this.mFocusManager.setAEControlBarEnable(true);
                    this.mFocusManager.setEVshutterButtonEnable(true);
                } else {
                    this.mFocusManager.registerEVCallback(true, true);
                }
                this.mFocusManager.setManualFocusButtonEnable(true);
            }
            if (this.mManualFocusManager != null && isManualFocusModeEx()) {
                this.mManualFocusManager.setManualFocusViewEnable(true);
            }
        }
        this.mSnapShotChecker.removeBurstState(96);
        if (FunctionProperties.isSupportedLongShot(isRearCamera())) {
            return stopBurstShotTakingForLongshot();
        }
        return stopBurstShotTakingForShot2Shot();
    }

    public void onNewBurstShotItemAdded() {
        if (isAvailableTilePreview()) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CamLog.m7i(CameraConstants.TAG, "[Tile] mFirstBurstUri : " + DefaultCameraModule.this.mFirstBurstUri);
                    if (DefaultCameraModule.this.mFirstBurstUri != null) {
                        Uri uri = DefaultCameraModule.this.mFirstBurstUri;
                        DefaultCameraModule.this.mFirstBurstUri = null;
                        int type = 30;
                        if (DefaultCameraModule.sBurstShotCount <= 1) {
                            type = 0;
                        }
                        CamLog.m7i(CameraConstants.TAG, "[Tile] type : " + type + " / burstCount : " + DefaultCameraModule.sBurstShotCount);
                        DefaultCameraModule.this.mGet.onNewItemAdded(uri, type, FileManager.getConsecutiveID(0, DefaultCameraModule.this.mBurstShotFileName));
                    }
                }
            }, 250);
        }
    }

    public int getCompleteBurstCount() {
        return sBurstShotCount;
    }

    protected boolean stopBurstShotTakingForLongshot() {
        if (this.mSnapShotChecker.checkMultiShotState(1)) {
            boolean setGifVisble;
            sBurstShotCount = this.mSnapShotChecker.getBurstCount();
            if (!(this.mGestureShutterManager == null || !this.mGestureShutterManager.isAvailableMotionQuickView() || this.mReviewThumbnailManager == null)) {
                this.mGestureShutterManager.startMotionEngine();
                this.mReviewThumbnailManager.readyforQuickview();
                makeMotionQuickViewFastImage(this.mCaptureData, sBurstShotCount, false);
            }
            if (this.mGestureShutterManager != null && this.mGestureShutterManager.isAvailableGestureShutterStarted()) {
                this.mGestureShutterManager.startGestureEngine();
            }
            onNewBurstShotItemAdded();
            this.mLongShotSync.block(2000);
            this.mSnapShotChecker.setBurstState(16);
            this.mSnapShotChecker.removeMultiShotState(1);
            this.mSnapShotChecker.removeBurstState(6);
            this.mSnapShotChecker.releaseSnapShotChecker();
            if (!this.mSnapShotChecker.checkBurstState(8)) {
                this.mGet.stopSound(6);
                if (isRearCamera()) {
                    this.mGet.playSound(12, false, 0);
                }
            }
            if (this.mExtraPrevewUIManager != null) {
                this.mExtraPrevewUIManager.setAllButtonsEnable(true);
            }
            if (this.mCameraDevice != null) {
                if (!isFastShotAvailable(3)) {
                    this.mCameraDevice.setLongshot(false);
                }
                this.mCameraDevice.stopBurstShot();
            }
            if (isSupportedQuickClip()) {
                this.mQuickClipManager.setQuickClipLongshot(false);
            }
            CamLog.m7i(CameraConstants.TAG, "[GIF] " + String.format("%d taken. %d queue count.", new Object[]{Integer.valueOf(sBurstShotCount), Integer.valueOf(this.mGet.getQueueCount())}));
            setHDRMetaDataCallback(getSettingValue("hdr-mode"));
            setFlashMetaDataCallback(getSettingValue("flash-mode"));
            this.mLongShotShutterCallbackReceived = true;
            if (!"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
                AudioUtil.setAudioFocus(getAppContext(), false);
            }
            LdbUtil.setMultiShotState(1);
            onTakePictureAfter();
            if (sBurstShotCount == 1) {
                setGifVisble = false;
            } else {
                setGifVisble = true;
            }
            this.mGifManager.setGifVisibleStatus(setGifVisble);
            this.mGifManager.setGifVisibility(setGifVisble);
            this.mSnapShotChecker.removeBurstState(96);
            this.mSnapShotChecker.releaseSnapShotChecker();
            if (revertParameterOnStopBurstShotTaking()) {
                if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator() || this.mAdvancedFilmManager.getCurrentFilmIndex() != 0 || CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode())) {
                    return true;
                }
                this.mAdvancedFilmManager.stopFilmEmulator(true, false);
                return true;
            }
        }
        return false;
    }

    protected boolean stopBurstShotTakingForShot2Shot() {
        if (!this.mSnapShotChecker.checkMultiShotState(4)) {
            return false;
        }
        boolean setGifVisble;
        sBurstShotCount = this.mSnapShotChecker.getBurstCount();
        setHDRMetaDataCallback(getSettingValue("hdr-mode"));
        setFlashMetaDataCallback(getSettingValue("flash-mode"));
        if (!(this.mGestureShutterManager == null || !this.mGestureShutterManager.isAvailableMotionQuickView() || this.mReviewThumbnailManager == null)) {
            this.mGestureShutterManager.startMotionEngine();
            this.mReviewThumbnailManager.readyforQuickview();
            makeMotionQuickViewFastImage(this.mCaptureData, sBurstShotCount, false);
        }
        if (this.mGestureShutterManager != null && this.mGestureShutterManager.isAvailableGestureShutterStarted()) {
            this.mGestureShutterManager.startGestureEngine();
        }
        if (sBurstShotCount == 1) {
            setGifVisble = false;
        } else {
            setGifVisble = true;
        }
        CamLog.m7i(CameraConstants.TAG, "[GIF] " + String.format("%d taken. %d queue count.", new Object[]{Integer.valueOf(sBurstShotCount), Integer.valueOf(this.mGet.getQueueCount())}));
        this.mGifManager.setGifVisibleStatus(setGifVisble);
        this.mGifManager.setGifVisibility(setGifVisble);
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.setQuickClipLongshot(false);
        }
        this.mSnapShotChecker.removeMultiShotState(4);
        if (!"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
            AudioUtil.setAudioFocus(getAppContext(), false);
        }
        LdbUtil.setMultiShotState(4);
        if (!this.mSnapShotChecker.isSnapShotProcessing()) {
            onTakePictureAfter();
            this.mSnapShotChecker.releaseSnapShotChecker();
        }
        return revertParameterOnStopBurstShotTaking();
    }

    private boolean revertParameterOnStopBurstShotTaking() {
        if (this.mCameraDevice == null) {
            return false;
        }
        CameraParameters parameters = this.mCameraDevice.getParameters();
        revertParameterByLGSF(parameters);
        if (parameters != null) {
            String changedHdrValue = this.mGet.getCurSettingValue("hdr-mode");
            if (("1".equals(changedHdrValue) || "2".equals(changedHdrValue)) && FunctionProperties.isSupportedHDR(isRearCamera()) == 2) {
                updateHDRParam(parameters, "1", false, false);
            }
            setParamUpdater(parameters, "flash-mode", getSettingValue("flash-mode"));
            if (!(this.mFocusManager == null || this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock() || isManualFocusModeEx())) {
                CameraDeviceUtils.setEnable3ALocks(this.mCameraDevice, false, false);
            }
            setBurstWBForAPI2(false, CameraConstants.MODE_BURST, parameters);
            setParameters(parameters);
            if (this.mFocusManager != null) {
                this.mFocusManager.registerCallback();
            }
        }
        return true;
    }

    protected boolean checkEnableRecogSucess() {
        if (this.mSnapShotChecker.checkMultiShotState(2) || !checkModuleValidate(31) || this.mTimerManager == null || this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress() || this.mGet.isAnimationShowing() || isPostviewShowing() || isRotateDialogVisible() || isJogZoomMoving() || isZoomControllerTouched() || isGestureZooming()) {
            return false;
        }
        return true;
    }

    protected void doVoiceRecogSuccess(int type) {
        switch (type) {
            case 0:
                LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_VOICE);
                onCameraShutterButtonClicked();
                return;
            default:
                return;
        }
    }

    protected void completeHideQuickview() {
        if (this.mBackButtonManager != null && this.mReviewThumbnailManager != null) {
            int curDegree = getOrientationDegree();
            this.mBackButtonManager.setRotateDegree(curDegree, false);
            this.mReviewThumbnailManager.setRotateDegree(curDegree, false);
            if (this.mGifManager.getGifVisibleStatus()) {
                setQuickClipIcon(true, false);
            } else {
                setQuickClipIcon(false, false);
            }
            if (this.mAdvancedFilmManager != null) {
                this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
                setFilmStrengthButtonVisibility(true, false);
                if (!(isModuleChanging() || isAnimationShowing() || getPreviewCoverVisibility() == 0 || isCameraChanging())) {
                    if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)) && isAvailableTilePreview() && !this.mGet.isAnimationShowing()) {
                        setTilePreviewLayout(true);
                    } else {
                        setTilePreviewLayout(false);
                    }
                }
            }
            handleBinningIconUI(true, 0);
        }
    }

    protected void hideSelfieQuickView() {
        if (this.mReviewThumbnailManager != null && this.mGestureShutterManager != null) {
            if (!this.mReviewThumbnailManager.isActivatedSelfieQuickView() || this.mReviewThumbnailManager.getQuickviewAniState() == 3) {
                if (this.mReviewThumbnailManager.isQuickViewAniStarted()) {
                    if (this.mReviewThumbnailManager != null) {
                        this.mReviewThumbnailManager.setActivatedSelfieQuickView(false);
                        this.mReviewThumbnailManager.hideQuickView(true);
                    }
                    if (this.mGestureShutterManager != null) {
                        this.mGestureShutterManager.stopMotionEngine();
                    }
                }
                this.mReviewThumbnailManager.setActivatedSelfieQuickView(false);
                if (checkModuleValidate(16)) {
                    setCaptureButtonEnable(true, getShutterButtonType());
                    if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
                        setCaptureButtonEnable(false, 1);
                    }
                }
                setQuickClipIcon(false, false);
                this.mMakeQuickViewImageThread = null;
                CamLog.m3d(CameraConstants.TAG, "hideSelfieQuickView!!!");
            }
        }
    }

    protected void doPhoneStateListenerAction(int state) {
        if (state == 0) {
            if (this.mCheeseShutterManager != null && checkModuleValidate(192)) {
                if (!this.mGet.isAttachIntent()) {
                    restoreSettingValue(Setting.KEY_VOICESHUTTER);
                }
                if (MDMUtil.allowMicrophone()) {
                    setCaptureButtonEnable(true, 1);
                    setVoiceShutter(true, 100);
                    return;
                }
                setCaptureButtonEnable(false, 1);
                setVoiceShutter(false, 0);
            }
        } else if (state != 2) {
            if (state == 1) {
                if (!(getCameraState() == 6 || getCameraState() == 7)) {
                    setCaptureButtonEnable(false, 1);
                    stopBurstShotTaking(false);
                }
                if (getRecordingPreviewState(1)) {
                    if (this.mTimerManager != null && this.mTimerManager.isTimerShotCountdown()) {
                        this.mTimerManager.stopTimerShot();
                    }
                    this.mRecordingButtonPressed = false;
                    doShutterTopTouchUp();
                }
            }
            if (this.mCheeseShutterManager != null) {
                setVoiceShutter(false, 0);
            }
        } else if (getCameraState() == 6 || getCameraState() == 7) {
            onVideoStopClicked(true, false);
            stopBurstShotTaking(false);
        }
    }

    protected boolean replaceZoomManager(boolean isSingleZoom) {
        if (this.mZoomManager == null) {
            return false;
        }
        ZoomManager targetZoomManager = null;
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ManagerInterfaceImpl manager = (ManagerInterfaceImpl) it.next();
            if (manager == this.mZoomManager) {
                targetZoomManager = (ZoomManager) manager;
                break;
            }
        }
        if (targetZoomManager == null) {
            CamLog.m3d(CameraConstants.TAG, "can't find zoom manager");
            return false;
        }
        if (isSingleZoom) {
            if (!(targetZoomManager instanceof InAndOutZoomManager)) {
                return false;
            }
            targetZoomManager.onDestroy();
            this.mZoomManager = new ZoomManager(this);
            CamLog.m3d(CameraConstants.TAG, "-normal angle only- replaced the InAndOutZoom manager to the Zoom manager");
        } else if (targetZoomManager instanceof InAndOutZoomManager) {
            return false;
        } else {
            targetZoomManager.onDestroy();
            this.mZoomManager = new InAndOutZoomManager(this);
            CamLog.m3d(CameraConstants.TAG, "-normal angle only- replaced the Zoom manager to the InAndOutZoom manager");
        }
        this.mZoomManager.init();
        this.mZoomManager.initializeAfterStartPreviewDone();
        this.mZoomManager.setZoomInterface(this);
        this.mZoomManager.onResumeBefore();
        this.mZoomManager.onResumeAfter();
        this.mZoomManager.setZoomValue(0);
        this.mZoomManager.updateExtraInfo("0");
        this.mManagerList.remove(targetZoomManager);
        this.mManagerList.add(this.mZoomManager);
        return true;
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.updateButton(100);
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setAfterRecording(true);
            if (this.mLightFrameManager != null && this.mLightFrameManager.isLightFrameMode()) {
                this.mAdvancedFilmManager.turnOnLightFrame(true);
            }
        }
        if (this.mCheeseShutterManager != null) {
            this.mCheeseShutterManager.setCheeseShutterSetting("on".equals(this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER)), true);
        }
        if (FunctionProperties.isAppyingFilmLimitation() && !CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))) {
            setSpecificSettingValueAndDisable("hdr-mode", "0", false);
            if (isRearCamera()) {
                ListPreference flashPref = getListPreference("flash-mode");
                if (flashPref != null) {
                    String savedValue = flashPref.loadSavedValue();
                    setSettingMenuEnable("flash-mode", true);
                    this.mParamUpdater.setParamValue("flash-mode", savedValue);
                    setFlashMetaDataCallback(savedValue);
                    setSetting("flash-mode", savedValue, true);
                    if (this.mQuickButtonManager != null) {
                        this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
                    }
                } else {
                    return;
                }
            }
        }
        this.mDotIndicatorManager.show();
        doCleanViewAfterStopRecording();
        showDoubleCamera(false);
        showMenuButton(true);
        sendLDBIntentOnAfterStopRecording();
        checkThemperatureOnRecording(false);
        this.mGet.setUspVisibility(0);
        if (this.mFocusManager != null) {
            this.mFocusManager.setTrackingFocusState(false);
            if (!isRecordingPriorityMode()) {
                this.mFocusManager.resetEVValue(0);
            }
            this.mFocusManager.setRotateDegree(this.mGet.getOrientationDegree(), false);
            this.mFocusManager.setEVshutterButtonEnable(true);
            this.mFocusManager.setManualFocusButtonEnable(true);
        }
        handleBinningIconUI(true, 0);
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setRecordSurfaceToTarget(false);
        }
        if (this.mManualFocusManager != null) {
            this.mManualFocusManager.setManualFocusViewEnable(true);
        }
    }

    protected void doCleanViewAfterStopRecording() {
        doCleanView(false, false, false);
    }

    public boolean onShutterBottomButtonLongClickListener() {
        CamLog.m3d(CameraConstants.TAG, "onShutterBottomButtonLongClickListener");
        if (!checkForShutterBottomButtonLongClick() || this.mGet.isActivatedQuickdetailView() || !isShutterButtonStateAvailableForBurstShot() || !setParameterOnShutterBottomButtonLongClick()) {
            return false;
        }
        deleteImmediatelyNotUndo();
        this.mSnapShotChecker.setSnapShotState(1);
        this.mSnapShotChecker.initBurstCount();
        this.mBeforeCaptureCount = 0;
        doTakePictureLongShotBefore();
        if (FunctionProperties.isSupportedLongShot(isRearCamera())) {
            this.mSnapShotChecker.setSnapShotState(2);
            this.mSnapShotChecker.removeBurstState(28);
            this.mSnapShotChecker.setMultiShotState(1);
            if (!(isFastShotAvailable(3) || this.mCameraDevice == null)) {
                this.mCameraDevice.setLongshot(true);
            }
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setLastBurstFileName(null);
            this.mReviewThumbnailManager.setIsCompleteSaving(false);
        }
        int[] maxResol = getMaxResolutionWidthHeight();
        this.mFreeSpace = (this.mStorageManager.getFreeSpace(this.mStorageManager.getCurrentStorage()) - CameraConstants.VIDEO_LOW_STORAGE_THRESHOLD) - (((long) ((((double) (maxResol[0] * maxResol[1])) * 0.3d) * 1.2d)) * ((long) this.mGet.getQueueCount()));
        if (this.mFreeSpace < 0) {
            this.mFreeSpace = 0;
        }
        startBurstShotTimer();
        return true;
    }

    private void startBurstShotTimer() {
        if (this.mButtonCheckTimer == null) {
            final boolean isInternalStorage = getCurStorage() == 0;
            Log.d(CameraConstants.TAG, "[BurstShot] Current Storage is " + (isInternalStorage ? "Internal." : "External"));
            TimerTask longShutter = new TimerTask() {
                public void run() {
                    if (!DefaultCameraModule.this.isShutterButtonStateAvailableForBurstShot() || (!FunctionProperties.isSupportedZSL(DefaultCameraModule.this.mCameraId) && !DefaultCameraModule.this.mSnapShotChecker.checkBurstState(1))) {
                        DefaultCameraModule.this.mGet.postOnUiThread(DefaultCameraModule.this.mStopBurstShotRunnable);
                    } else if (FunctionProperties.isSupportedLongShot(DefaultCameraModule.this.isRearCamera())) {
                        DefaultCameraModule.this.takePictureLongShot(isInternalStorage);
                    } else {
                        DefaultCameraModule.this.mGet.postOnUiThread(DefaultCameraModule.this.mShotToShotTakeRunnable);
                    }
                }
            };
            int timerDur = 150;
            if (FunctionProperties.isSupportedFastShot()) {
                timerDur = FunctionProperties.getSupportedBurstShotDuration(isInternalStorage, true, isRearCamera());
            } else if (FunctionProperties.isSupportedLongShot(isRearCamera())) {
                FunctionProperties.getSupportedBurstShotDuration(isInternalStorage, true, isRearCamera());
                timerDur = 20;
                this.mLastLongShotTime = 0;
            }
            FunctionProperties.getSupportedBurstShotMaxCount(isInternalStorage, true, isRearCamera());
            this.mButtonCheckTimer = new Timer("timer_long_press_check");
            this.mButtonCheckTimer.scheduleAtFixedRate(longShutter, 0, (long) timerDur);
            this.mUpdateCount = 0;
            this.mBurstCountAvailable = true;
        }
    }

    protected boolean checkForShutterBottomButtonLongClick() {
        if (!checkModuleValidate(223) || getCameraState() != 1 || this.mGet.isAttachIntent() || this.mGet.isMMSIntent() || isRotateDialogVisible() || this.mCameraDevice == null || this.mGet.isAnimationShowing() || isPostviewShowing() || this.mGet.getPreviewCoverVisibility() == 0 || this.mPictureOrVideoSizeChanged || this.mIsPreviewCallbackWaiting || !this.mSnapShotChecker.isAvailableNightAndFlash() || ((this.mTimerManager != null && this.mTimerManager.isTimerShotCountdown()) || ((this.mReviewThumbnailManager != null && this.mReviewThumbnailManager.isQuickViewAniStarted()) || this.mSnapShotChecker.checkMultiShotState(2) || this.mAdvancedFilmManager.checkBlockingButtonState() || isCropZoomStarting() || this.mGifManager.isGIFEncoding()))) {
            CamLog.m3d(CameraConstants.TAG, "return: before burstshot");
            return false;
        } else if (!checkStorage(1, this.mStorageManager.getCurrentStorage(), true)) {
            CamLog.m3d(CameraConstants.TAG, "return: storage unavailable");
            return false;
        } else if ((isRearCamera() && !FunctionProperties.isSupportedZSL(0)) || (!isRearCamera() && !FunctionProperties.isSupportedFrontIntervalShot())) {
            CamLog.m3d(CameraConstants.TAG, "return: FunctionProperties.isSupportedZSL = " + FunctionProperties.isSupportedZSL(0));
            return false;
        } else if (this.mTimerManager != null && this.mTimerManager.getIsGesureTimerShotProgress()) {
            CamLog.m3d(CameraConstants.TAG, "return: getIsGesureTimerShotProgress = " + this.mTimerManager.getIsGesureTimerShotProgress());
            return false;
        } else if (this.mGet.getQueueCount() > 0 || this.mNeedProgressDuringCapture > 0) {
            if (this.mCaptureButtonManager != null) {
                this.mCaptureButtonManager.setShutterButtonPressed(false, 2);
            }
            stopBurstShotTaking(false);
            if (this.mGet.getQueueCount() > 0 || !isFastShotSupported()) {
                CamLog.m3d(CameraConstants.TAG, "saving dialog : exist burstshot queue count");
                showSavingDialog(true, 0);
            }
            this.mWaitSavingDialogType = 3;
            CamLog.m3d(CameraConstants.TAG, "return: getQueueCount = " + this.mGet.getQueueCount() + " mNeedProgressDuringCapture = " + this.mNeedProgressDuringCapture);
            return false;
        } else if (!checkPictureCallbackRunnable(true)) {
            return false;
        } else {
            if (this.mStickerManager != null && (this.mStickerManager.isStickerGridVisible() || this.mStickerManager.isStickerDrawing())) {
                CamLog.m3d(CameraConstants.TAG, "sticker is running so exit burstshot");
                showToast(this.mGet.getActivity().getString(C0088R.string.sticker_burstshot_limitation_desc), 2000);
                return false;
            } else if (this.mBinningManager == null || !this.mBinningManager.isBinningEnabled()) {
                return true;
            } else {
                showToast(this.mGet.getActivity().getString(FunctionProperties.isUseSuperBright() ? C0088R.string.super_bright_camera_toast_burst_limitation_rev2 : C0088R.string.bright_mode_toast_burst_limitation), 2000);
                return false;
            }
        }
    }

    private boolean setParameterOnShutterBottomButtonLongClick() {
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            if (this.mAdvancedFilmManager.getCurrentFilmIndex() != 0) {
                CamLog.m3d(CameraConstants.TAG, "return: mFilmEmulatorManager = " + this.mAdvancedFilmManager.isRunningFilmEmulator());
                if (this.mHandler != null) {
                    this.mHandler.sendEmptyMessageDelayed(89, 1300);
                }
                this.mSnapShotChecker.setSnapShotState(0);
                return false;
            }
            this.mAdvancedFilmManager.showFilmMenu(false, 1, false, false, 0, true);
            this.mExtraPrevewUIManager.changeButtonState(0, false);
            onHideMenu(4);
            showExtraPreviewUI(false, false, true, isRearCamera());
        }
        CameraParameters parameters = this.mCameraDevice.getParameters();
        if (parameters != null) {
            if ("1".equals(getSettingValue("hdr-mode")) || "2".equals(getSettingValue("hdr-mode"))) {
                if (FunctionProperties.isSupportedHDR(isRearCamera()) == 1) {
                    showToast(this.mGet.getActivity().getString(C0088R.string.sp_notice_hdr_not_supported_burstshot1), 2000);
                    this.mSnapShotChecker.setSnapShotState(0);
                    return false;
                } else if (FunctionProperties.isSupportedHDR(isRearCamera()) == 2) {
                    updateHDRParam(parameters, "0", false, false);
                }
            }
            if (!"off".equals(getSettingValue("flash-mode"))) {
                setParamUpdater(parameters, "flash-mode", "off");
            }
            parameters.setSceneMode("auto");
            setParameterBeforeTakePicture(parameters, true, true);
        }
        setHDRMetaDataCallback(null);
        setFlashMetaDataCallback(null);
        return true;
    }

    protected void doTakePictureLongShotBefore() {
        hideMenu(PointerIconCompat.TYPE_ZOOM_OUT, false, false);
        if (this.mCaptureButtonManager != null) {
            setCaptureButtonEnable(false, 1);
            if (this.mKeyManager.isVolumeLongKeyPressed() || this.mKeyManager.isCenterKeyPressed()) {
                setCaptureButtonEnable(false, 2);
            }
            this.mCaptureButtonManager.setExtraButtonEnable(false, 3);
        }
        sBurstShotCount = 0;
        this.mGet.showTilePreviewCoverView(true);
        if (!(isRearCamera() || this.mGestureShutterManager == null)) {
            this.mGestureShutterManager.stopMotionEngine();
            this.mGestureShutterManager.stopGestureEngine();
        }
        showDoubleCamera(true);
        setDoubleCameraEnable(false);
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setEnable(100, false, true);
        }
        if (this.mAdvancedFilmManager != null) {
            CamLog.m3d(CameraConstants.TAG, "[filter] doTakePictureLongShotBefore. filter UI set invisibile");
            this.mAdvancedFilmManager.setMenuEnable(false);
            this.mAdvancedFilmManager.setSelfieOptionVisibility(false, false);
        }
        if (this.mExtraPrevewUIManager != null) {
            this.mExtraPrevewUIManager.setAllButtonsEnable(false);
        }
        this.mGifManager.createGifUriList();
        this.mGifManager.setGifVisibility(false);
        this.mGifManager.setHideTransient(false);
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.enableConeMenuIcon(31, false);
        }
        hideZoomBar();
        this.mBarManager.setEnable(1, false);
        if (this.mFocusManager != null) {
            if (isFocusLock() || isAELock()) {
                showFocusOnHideOtherBars();
            } else {
                this.mFocusManager.hideAllFocus();
            }
        }
        if (this.mZoomManager != null) {
            this.mZoomManager.stopDrawingExceedsLevel();
        }
        if (!(AudioUtil.isWiredHeadsetOn() || AudioUtil.isBluetoothA2dpOn())) {
            AudioUtil.setAudioFocus(getAppContext(), true);
        }
        if (this.mFocusManager != null && (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock())) {
            this.mFocusManager.setAEControlBarEnable(false);
        }
        if (this.mFocusManager != null) {
            this.mFocusManager.setManualFocusButtonEnable(false);
            this.mFocusManager.setEVshutterButtonEnable(false);
        }
        if (this.mManualFocusManager != null && isManualFocusModeEx()) {
            this.mManualFocusManager.setManualFocusViewEnable(false);
        }
        if (this.mBinningManager != null) {
            handleBinningIconUI(false, 0);
        }
    }

    public boolean isGifButtonAvailable() {
        if (this.mCaptureButtonManager == null || this.mKeyManager == null || this.mSnapShotChecker == null || this.mReviewThumbnailManager == null) {
            return false;
        }
        if (!this.mCaptureButtonManager.isShutterButtonPressed(2) && !this.mCaptureButtonManager.isShutterButtonPressed(1) && !this.mKeyManager.isVolumeLongKeyPressed() && !this.mKeyManager.isCenterKeyPressed() && !isQuickCoverButtonLongPressed() && checkModuleValidate(192) && !this.mReviewThumbnailManager.isActivatedSelfieQuickView() && !getBurstProgress() && getFilmState() != 3 && getFilmState() != 1 && getFilmState() != 5 && getCameraState() > 0) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "[gif] gifButton is not available. isShutterButtonPressed Top = " + this.mCaptureButtonManager.isShutterButtonPressed(1) + ", Bottom = " + this.mCaptureButtonManager.isShutterButtonPressed(2) + ", getBurstState = " + getBurstProgress());
        return false;
    }

    protected boolean isShutterButtonStateAvailableForBurstShot() {
        if (this.mCaptureButtonManager == null || this.mKeyManager == null || this.mCaptureButtonManager.isShutterButtonPressed(2) || this.mKeyManager.isVolumeLongKeyPressed() || this.mKeyManager.isCenterKeyPressed() || isQuickCoverButtonLongPressed() || this.mSnapShotChecker.checkBurstState(2)) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "Capture button or hot key is not pressed");
        return false;
    }

    protected void doIntervalShot() {
        if (this.mQuickButtonManager == null || this.mGestureShutterManager == null || this.mCaptureButtonManager == null || this.mReviewThumbnailManager == null || this.mIntervalShotManager == null) {
            this.mSnapShotChecker.setSnapShotState(0);
        } else if (this.mIntervalShotTimer == null) {
            CamLog.m3d(CameraConstants.TAG, "doIntervalShot");
            AudioUtil.setAudioFocus(getAppContext(), true);
            doCleanView(true, false, false);
            if (this.mDoubleCameraManager != null) {
                this.mDoubleCameraManager.showDualViewControl(false);
            }
            this.mGestureShutterManager.stopMotionEngine();
            this.mGestureShutterManager.stopGestureEngine();
            this.mCaptureButtonManager.changeButtonByMode(11);
            this.mReviewThumbnailManager.setPressed(false);
            hideZoomBar();
            setQuickClipIcon(true, false);
            this.mIntervalShotManager.showIntervalshotLayout();
            this.mSnapShotChecker.removeIntervalShotState(8);
            this.mIntervalShotManager.setIntervalShotState(0);
            TimerTask intervalShot = new C019327();
            if (FunctionProperties.isSupportedConeUI()) {
                this.mGet.enableConeMenuIcon(31, false);
            }
            this.mIntervalShotTimer = new Timer("timer_intervalshot_check");
            this.mIntervalShotTimer.scheduleAtFixedRate(intervalShot, 0, 2000);
            if (this.mFocusManager != null && (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock())) {
                this.mFocusManager.setAEControlBarEnable(false);
                this.mFocusManager.setEVshutterButtonEnable(false);
            }
            if (this.mFocusManager != null) {
                this.mFocusManager.setManualFocusButtonEnable(false);
            }
            if (this.mManualFocusManager != null && isManualFocusModeEx()) {
                this.mManualFocusManager.setManualFocusViewEnable(false);
            }
        }
    }

    protected void rescheduleIntervalShot(boolean isDelayState) {
        if (this.mIntervalShotTimer != null) {
            int interval;
            CamLog.m3d(CameraConstants.TAG, "rescheduleIntervalShot : " + isDelayState);
            if (isDelayState) {
                interval = 100;
            } else {
                interval = 2000;
            }
            this.mIntervalShotTimer.cancel();
            TimerTask intervalShot = new C019428();
            this.mIntervalShotTimer = new Timer("timer_intervalshot_check");
            this.mIntervalShotTimer.scheduleAtFixedRate(intervalShot, 0, (long) interval);
        }
    }

    private void doAfterIntervalshot(byte[] data) {
        if (!(this.mCameraDevice == null || this.mFlipManager == null || this.mIntervalShotManager == null)) {
            boolean setFlip;
            if (this.mFlipManager.isPreProcessPictureFlip()) {
                setFlip = false;
            } else {
                setFlip = this.mFlipManager.isNeedFlip(this.mCameraId);
            }
            if (this.mStickerManager == null || !this.mStickerManager.isStickerDrawing()) {
                this.mIntervalShotManager.updateThumbnail(this.mSnapShotChecker.getIntervalShotCount(), data, setFlip);
            } else {
                int hashcode = data.hashCode();
                int[] size = Utils.sizeStringToArray(getCurrentSelectedPictureSize());
                this.mIntervalShotManager.updateThumbnail(this.mSnapShotChecker.getIntervalShotCount(), data, this.mStickerManager.getJpegOrientationWithoutRemove(hashcode), ((float) size[0]) / ((float) size[1]));
            }
        }
        this.mSnapShotChecker.increaseIntervalShotCount();
        if (this.mSnapShotChecker.isIntervalShotMax() || this.mSnapShotChecker.checkIntervalShotState(8)) {
            int delay = (this.mSnapShotChecker.checkIntervalShotState(8) || "on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) ? 0 : 300;
            if (delay != 0 && this.mGestureShutterManager.isAvailableMotionQuickView()) {
                this.mGestureShutterManager.startMotionEngine();
            }
            stopIntervalShot(delay);
        }
    }

    protected void stopIntervalShot(int delay) {
        CamLog.m3d(CameraConstants.TAG, "stopIntervalShot delay : " + delay);
        this.mSnapShotChecker.removeMultiShotState(2);
        if (this.mIntervalShotTimer != null) {
            this.mIntervalShotTimer.cancel();
            this.mIntervalShotTimer.purge();
            this.mIntervalShotTimer = null;
        }
        if (this.mGestureShutterManager != null) {
            if (this.mGestureShutterManager.isAvailableGestureShutterStarted()) {
                this.mGestureShutterManager.startGestureEngine();
            }
            if (this.mGestureShutterManager.isAvailableMotionQuickView() && this.mReviewThumbnailManager != null && delay == 0) {
                this.mGestureShutterManager.startMotionEngine();
            }
        }
        if (!"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
            AudioUtil.setAudioFocus(getAppContext(), false);
        }
        if (!CameraConstants.MODE_FLASH_JUMPCUT.equals(getShotMode())) {
            if (this.mIntervalShotManager != null) {
                this.mIntervalShotManager.stopWaitingUI();
            }
            if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)) && !this.mGet.isActivatedTilePreview()) {
                this.mGet.showTilePreview(true);
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DefaultCameraModule.this.doActionAfterIntervshot();
                }
            }, (long) delay);
            setQuickClipIcon(false, false);
            if (this.mFocusManager != null && (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock())) {
                this.mFocusManager.setAEControlBarEnable(true);
                this.mFocusManager.setEVshutterButtonEnable(true);
            }
            if (!(this.mFocusManager == null || this.mManualFocusManager == null || !isManualFocusModeEx())) {
                this.mFocusManager.setManualFocusButtonEnable(true);
                this.mManualFocusManager.setManualFocusViewEnable(true);
            }
            onTakePictureAfter();
        }
    }

    protected void doActionAfterIntervshot() {
        if (this.mIntervalShotManager == null || this.mCaptureButtonManager == null || this.mQuickButtonManager == null) {
            this.mSnapShotChecker.setPictureCallbackState(0);
            return;
        }
        String curShotMode = getShotMode();
        if ((!CameraConstants.MODE_SQUARE_GRID.equals(curShotMode) || FunctionProperties.isSupportedCollageRecording()) && !CameraConstants.MODE_SQUARE_OVERLAP.equals(curShotMode)) {
            this.mCaptureButtonManager.changeButtonByMode(3);
        } else {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
        LdbUtil.setMultiShotState(2);
        if (this.mSnapShotChecker.isIntervalShotMax() || this.mSnapShotChecker.getPictureCallbackState() == 0) {
            LdbUtil.setSkipNextLdbBroadcast(true);
        } else {
            LdbUtil.setSkipNextLdbBroadcast(false);
        }
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            revertParameterByLGSF(parameters);
            if (parameters != null) {
                String changedHdrValue = this.mGet.getCurSettingValue("hdr-mode");
                if (("1".equals(changedHdrValue) || "2".equals(changedHdrValue)) && FunctionProperties.isSupportedHDR(isRearCamera()) == 2) {
                    updateHDRParam(parameters, "1", false, false);
                }
                setParamUpdater(parameters, "flash-mode", getSettingValue("flash-mode"));
                if (!isAELock()) {
                    CameraDeviceUtils.setEnable3ALocks(this.mCameraDevice, false, false);
                }
                setParameters(parameters);
                if (!(this.mFocusManager == null || this.mFocusManager.isAELock())) {
                    this.mFocusManager.releaseTouchFocus();
                }
            }
        }
        processForCleanViewAfterInterval();
        this.mIntervalShotManager.hideIntervalshotLayout();
        setQuickClipIcon(false, false);
    }

    protected void processForCleanViewAfterInterval() {
        doCleanView(false, true, false);
        doCleanViewAfter(true);
    }

    protected boolean isQuickCoverButtonLongPressed() {
        return false;
    }

    protected void oneShotPreviewCallbackDone() {
        if (this.mFocusManager != null) {
            this.mFocusManager.setPreviewSizeForAF();
        }
        if (!(this.mCameraDevice == null || this.mZoomManager == null || this.mZoomManager.isReadyZoom())) {
            setZoomCompensation(this.mCameraDevice.getParameters());
        }
        setOpticZoomCallback(true);
        doOneShotPreviewCallbackActionForRecording();
        if (checkModuleValidate(128) && checkModuleValidate(64) && !isPostviewShowing()) {
            requestFocusOnShutterButton(true);
        }
        this.mSnapShotChecker.setBurstState(1);
        doOneShotPreviewCallbackActionForFilmEffect();
        super.oneShotPreviewCallbackDone();
        if (this.mAdvancedFilmManager != null && getFilmState() == 1) {
            this.mAdvancedFilmManager.filmEmulatorStopDone();
        }
        configPreviewCallback();
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (!DefaultCameraModule.this.isPaused()) {
                    DefaultCameraModule.this.mGet.getHybridView().setSurfaceViewTransparent(false);
                    if (DefaultCameraModule.this.mIsFlashSetOffByBatteryLevel) {
                        DefaultCameraModule.this.mToastManager.showShortToast(DefaultCameraModule.this.mGet.getAppContext().getString(C0088R.string.msg_battery_too_low_to_use_function_flash), false);
                        DefaultCameraModule.this.mIsFlashSetOffByBatteryLevel = false;
                    }
                }
            }
        }, 500);
        doOneShotPrevieCallbackActionForQR();
        doOneShotPreviewCallbackActionForSticker();
        doOneShotPreviewCallbackActionForLivePhoto();
        this.mGet.loadSound();
        if (!(this.mDoubleCameraManager == null || this.mSnapShotChecker.checkMultiShotState(7))) {
            this.mDoubleCameraManager.setDualViewControlEnabled(true);
        }
        if (getBinningEnabledState() && FunctionProperties.getSupportedHal() != 2) {
            this.mBinningManager.setBinningPictureSizeDirect(0);
        }
        ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        if (listPref != null) {
            changeFullVisionSettingOnPictureSizeChanged(listPref, listPref.getValue());
        }
        PackageUtil.checkLGSmartWorldPreloaded(getAppContext());
    }

    protected void doOneShotPreviewCallbackActionForRecording() {
        if (getRecordingPreviewState(1)) {
            setRecordingPreviewState(2, true);
        }
        if (this.mCameraState != 5) {
            return;
        }
        if (getRecordingPreviewState(4)) {
            doShutterTopTouchUp();
            return;
        }
        if (!CameraConstants.MODE_POPOUT_CAMERA.equals(getShotMode())) {
            this.mGet.setPreviewCoverVisibility(8, false, null, true, true);
        }
        if (!getRecordingPreviewState(1)) {
            setAnimationLayout(3);
            runStartRecorder(true);
        }
    }

    private void doOneShotPreviewCallbackActionForSticker() {
        if (this.mStickerManager != null && isStickerSupportedCameraMode()) {
            if (this.mStickerManager.hasSticker()) {
                this.mStickerManager.resumeEngine();
            } else if (this.mStickerManager.isWaitOneShot()) {
                this.mStickerManager.start(getGLViewMargin());
            }
            if (this.mStickerManager.isRunning()) {
                settingForSticker(true);
                setPreviewCallbackAll(true);
                if (FunctionProperties.getSupportedHal() == 2 && this.mCameraDevice != null) {
                    this.mCameraDevice.startFaceDetection();
                }
            }
        }
    }

    protected void doOneShotPreviewCallbackActionForLivePhoto() {
        if (FunctionProperties.isLivePhotoSupported() && this.mLivePhotoManager != null && "on".equals(getSettingValue(Setting.KEY_LIVE_PHOTO)) && !isRecordingState() && this.mAdvancedFilmManager != null && !this.mAdvancedFilmManager.isShowingFilmMenu()) {
            this.mLivePhotoManager.enableLivePhoto();
        }
    }

    protected void oneShotPreviewCallbackDoneAfter() {
        super.oneShotPreviewCallbackDoneAfter();
        this.mGet.setSwitchingAniViewParam(false);
        showNeedUpdateCamModule();
    }

    protected void doOneShotPreviewCallbackActionForFilmEffect() {
        if (this.mAdvancedFilmManager != null) {
            if (!(this.mCameraState == 5 || this.mCameraState == 6)) {
                if (checkToUseFilmEffect()) {
                    boolean z;
                    this.mAdvancedFilmManager.movePreviewOutOfWindow(true, 500);
                    this.mAdvancedFilmManager.setFilmState(4);
                    this.mAdvancedFilmManager.setFilmPreviewAlpha(false);
                    this.mAdvancedFilmManager.moveFilmPreviewOutOfWindow(false);
                    boolean aiCam = CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode());
                    AdvancedSelfieManager advancedSelfieManager = this.mAdvancedFilmManager;
                    int orientationDegree = getOrientationDegree();
                    if (aiCam) {
                        z = false;
                    } else {
                        z = true;
                    }
                    advancedSelfieManager.setFilterMenuOrientation(orientationDegree, z);
                } else if (!(!CameraConstants.FILM_NONE.equals(this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR)) || CameraConstants.MODE_POPOUT_CAMERA.equals(getShotMode()) || CameraConstants.MODE_DUAL_POP_CAMERA.equals(getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) || CameraConstants.MODE_MULTIVIEW.equals(getShotMode()))) {
                    this.mAdvancedFilmManager.movePreviewOutOfWindow(false, 100);
                }
            }
            if (getFilmState() != 1) {
                return;
            }
            if (this.mCameraState == 5) {
                this.mAdvancedFilmManager.movePreviewOutOfWindow(false, 0);
                this.mAdvancedFilmManager.moveFilmPreviewOutOfWindow(true);
                return;
            }
            this.mAdvancedFilmManager.movePreviewOutOfWindow(false, 100);
            enableControls(true);
        }
    }

    public void onShutterBottomButtonFocus(boolean pressed) {
        CamLog.m3d(CameraConstants.TAG, "onShutterBottomButtonFocus : " + pressed);
        if (!pressed && SystemBarUtil.isSystemUIVisible(getActivity())) {
            return;
        }
        if (!pressed && (this.mCaptureButtonManager.isShutterButtonPressed(2) || this.mKeyManager.isVolumeLongKeyPressed() || this.mKeyManager.isCenterKeyPressed())) {
            return;
        }
        if (pressed) {
            if (this.mReviewThumbnailManager != null) {
                this.mReviewThumbnailManager.setPressed(false);
            }
            if (this.mCaptureButtonManager != null) {
                this.mCaptureButtonManager.setExtraButtonPressed(false, 3);
            }
            if (this.mQuickButtonManager != null) {
                this.mQuickButtonManager.setPressed(100, false);
            }
        } else if (this.mSnapShotChecker.checkMultiShotState(4)) {
            this.mSnapShotChecker.setMultiShotState(8);
            stopBurstShotTaking(false);
            if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator() && this.mAdvancedFilmManager.getCurrentFilmIndex() == 0 && !this.mAdvancedFilmManager.isShowingFilmMenu() && !CameraConstants.MODE_SMART_CAM.equals(getShotMode()) && !CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode())) {
                this.mAdvancedFilmManager.stopFilmEmulator(true, false);
            }
        } else {
            stopBurstShotTaking(this.mSnapShotChecker.checkMultiShotState(1));
            this.mSnapShotChecker.initBurstCountLater(true);
        }
    }

    public void onShutterBottomButtonClickListener() {
        CamLog.m3d(CameraConstants.TAG, "onShutterBottomButtonClickListener");
        if (!this.mSnapShotChecker.checkMultiShotState(2) && checkModuleValidate(15) && !stopBurstShotTaking(this.mSnapShotChecker.checkMultiShotState(1)) && !SystemBarUtil.isSystemUIVisible(getActivity()) && !this.mGifManager.isGIFEncoding()) {
            if (this.mPostviewManager != null && this.mPostviewManager.isPostviewShowing()) {
                return;
            }
            if (this.mReviewThumbnailManager == null || !this.mReviewThumbnailManager.isQuickViewAniStarted()) {
                clickShutterBottomButton();
            }
        }
    }

    protected void clickShutterBottomButton() {
        if (this.mCaptureButtonManager != null) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(2)) {
                case 1:
                    onCameraShutterButtonClicked();
                    return;
                case 3:
                    onShutterStopButtonClicked();
                    return;
                case 8:
                    onShutterStopButtonClicked();
                    return;
                default:
                    return;
            }
        }
    }

    public boolean onCameraShutterButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "onCameraShutterButtonClicked. - start");
        if (AppControlUtil.isNeedQuickShotTaking() || !(!checkModuleValidate(95) || isRotateDialogVisible() || ((this.mReviewThumbnailManager != null && this.mReviewThumbnailManager.isQuickViewAniStarted()) || getCameraState() != 1 || this.mGet.isAnimationShowing() || this.mGifManager.isGIFEncoding() || isPostviewShowing() || this.mGet.getPreviewCoverVisibility() == 0 || !easyCheckStorage(1, getCurStorage(), true) || this.mPictureOrVideoSizeChanged || this.mIsPreviewCallbackWaiting || !this.mSnapShotChecker.isAvailableNightAndFlash() || this.mAdvancedFilmManager.checkBlockingButtonState() || isCropZoomStarting() || this.mGet.isActivatedQuickdetailView() || CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equalsIgnoreCase(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null))))) {
            hideZoomBar();
            this.mGifManager.setGifVisibility(false);
            this.mGifManager.setGifVisibleStatus(false);
            this.mGifManager.setHideTransient(false);
            this.mSnapShotChecker.setSnapShotState(1);
            if (this.mHandler != null && this.mHandler.hasMessages(89)) {
                this.mHandler.removeMessages(89);
            }
            this.mSnapShotChecker.removeMultiShotState(8);
            if (!(isRearCamera() || this.mGestureShutterManager == null || this.mSnapShotChecker.checkMultiShotState(2))) {
                this.mGestureShutterManager.stopMotionEngine();
                this.mGet.removePostRunnable(this.mDeactivateQuickView);
            }
            if (isShutterKeyOptionTimerActivated() && !this.mSnapShotChecker.checkMultiShotState(6)) {
                return doStartTimerShot(new TimerTypeCamera());
            }
            if (!(isRearCamera() || this.mGestureShutterManager == null || !this.mGestureShutterManager.isAvailableMotionQuickView() || this.mReviewThumbnailManager == null)) {
                if (!this.mSnapShotChecker.checkMultiShotState(2)) {
                    this.mGestureShutterManager.startMotionEngine();
                    this.mMakeQuickViewImageThread = null;
                }
                this.mReviewThumbnailManager.readyforQuickview();
            }
            return takePicture();
        }
        CamLog.m3d(CameraConstants.TAG, "Exit camera shutter clicked. getCameraState() : " + getCameraState() + " mIsPreviewCallbackWaiting :" + this.mIsPreviewCallbackWaiting);
        CamLog.m3d(CameraConstants.TAG, "getLGSFParamState : " + this.mSnapShotChecker.getLGSFParamState() + ", getTakePicFlashState : " + this.mSnapShotChecker.getTakePicFlashState() + ", isActivatedQuickDetailView : " + this.mGet.isActivatedQuickdetailView());
        CamLog.m3d(CameraConstants.TAG, "checkModuleValidate : VALIDATE_ALL " + checkModuleValidate(15) + " VALIDATE_CAPTURE_PROGRESS " + checkModuleValidate(16) + " VALIDATE_RECORD_STATE_WAIT " + checkModuleValidate(64));
        CamLog.m3d(CameraConstants.TAG, "takePicture cancel by assistant nightmode? " + CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equalsIgnoreCase(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null)));
        if (!isRotateDialogVisible()) {
            return false;
        }
        this.mCaptureButtonManager.setExtraButtonEnable(true, 3);
        return false;
    }

    protected boolean doStartTimerShot(TimerType timerType) {
        if (!checkModuleValidate(207) || !checkStorageByTimerType(timerType)) {
            return false;
        }
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.enableConeMenuIcon(31, false);
        }
        if (!(this.mTimerManager == null || this.mFocusManager == null || this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress())) {
            startTimerShotByType(timerType, setTimerValueByType(timerType));
        }
        return true;
    }

    private boolean checkStorageByTimerType(TimerType timerType) {
        int storageType = timerType.getStorage();
        if (this.mStorageManager == null || checkStorage(storageType, this.mStorageManager.getCurrentStorage(), true)) {
            return true;
        }
        if (this.mGestureShutterManager != null && this.mGestureShutterManager.isAvailableGestureShutterStarted()) {
            this.mGestureShutterManager.startGestureEngine();
        }
        return false;
    }

    private String setTimerValueByType(TimerType timerType) {
        String timerValue = timerType.getTime();
        if ("0".equals(timerValue)) {
            return this.mGet.getCurSettingValue(Setting.KEY_TIMER);
        }
        return timerValue;
    }

    protected void startTimerShotByType(TimerType timerType, String timerValue) {
        if (this.mTimerManager != null) {
            this.mTimerManager.startTimerShot(Integer.valueOf(timerType.getTime()).intValue(), timerType.getShutter());
        }
        if ((timerType instanceof TimerTypeGestureShot) && this.mGestureShutterManager != null && this.mGestureShutterManager.getGestureCaptureType() == 2 && this.mIntervalShotManager != null) {
            this.mIntervalShotManager.startWatingUI(0);
            this.mIntervalShotManager.showIntervalshotEnteringGuide();
        }
        onTakePictureBefore();
        if (this.mFocusManager != null) {
            this.mFocusManager.hideAllFocus(true);
            this.mFocusManager.registerEVCallback(false, false);
        }
        if (!CameraConstants.MODE_FLASH_JUMPCUT.equals(getShotMode())) {
            doCleanView(true, false, false);
            if (this.mDoubleCameraManager != null) {
                this.mDoubleCameraManager.showDualViewControl(false);
            }
            if (this.mReviewThumbnailManager != null) {
                this.mReviewThumbnailManager.setThumbnailVisibility(8, false);
            }
            this.mGet.showTilePreviewCoverView(true);
        }
    }

    public void onShutterStopButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "video stop clicked.");
        if (checkModuleValidate(15)) {
            if (!(checkModuleValidate(64) || getRecordingPreviewState(1))) {
                CamLog.m3d(CameraConstants.TAG, "recording wait not finished, return");
            }
            if (this.mPictureOrVideoSizeChanged || this.mIsSwitchingCameraDuringRecording) {
                CamLog.m3d(CameraConstants.TAG, "mPictureOrVideoSizeChanged or mIsSwitchingCameraDuringRecording, return");
                return;
            } else if (this.mRecordingUIManager == null || this.mRecordingUIManager.checkMinRecTime() || getRecordingPreviewState(1)) {
                onVideoStopClicked(true, true);
                return;
            } else {
                CamLog.m7i(CameraConstants.TAG, "Video recording time is too short.");
                return;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "Camera state is not valid, return");
    }

    public void onVideoPauseClicked() {
        CamLog.m3d(CameraConstants.TAG, "video pause clicked.");
        if (!checkModuleValidate(79) || this.mCameraState == 1) {
            CamLog.m3d(CameraConstants.TAG, "exit video pause clicked.");
        } else if (this.mAllowPause && (this.mRecordingUIManager == null || this.mRecordingUIManager.checkMinRecTime())) {
            if (this.mCaptureButtonManager != null) {
                this.mCaptureButtonManager.changeButtonByMode(4);
                if (isUHDmode() || !isLiveSnapshotSupported() || (this.mStickerManager != null && this.mStickerManager.isGLSurfaceViewShowing())) {
                    this.mCaptureButtonManager.changeExtraButton(0, 1);
                    this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
                }
            }
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateVideoTime(2, SystemClock.uptimeMillis());
            }
            this.mAllowPause = false;
            pauseAndResumeRecorder(true);
            setCameraState(7);
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateRecStatusIcon();
            }
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_RECORDING_PAUSE);
        } else {
            CamLog.m7i(CameraConstants.TAG, "Video recording time is too short.");
        }
    }

    public void onVideoResumeClicked() {
        CamLog.m3d(CameraConstants.TAG, "video resume clicked.");
        if (checkModuleValidate(79)) {
            if (this.mCaptureButtonManager != null) {
                this.mCaptureButtonManager.changeButtonByMode(5);
                if (isUHDmode() || !isLiveSnapshotSupported() || (this.mStickerManager != null && this.mStickerManager.isGLSurfaceViewShowing())) {
                    this.mCaptureButtonManager.changeExtraButton(0, 1);
                    this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
                }
            }
            pauseAndResumeRecorder(false);
            setCameraState(6);
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.show(isNeedProgressBar());
                this.mRecordingUIManager.updateRecStatusIcon();
            }
            if (this.mZoomManager != null && this.mZoomManager.isZoomBarVisible()) {
                this.mZoomManager.setZoomBarVisibility(4);
            }
        }
    }

    public void onShutterTopButtonClickListener() {
        if ((this.mPostviewManager != null && this.mPostviewManager.isPostviewShowing()) || SystemBarUtil.isSystemUIVisible(getActivity()) || !checkModuleValidate(95)) {
            return;
        }
        if ((this.mReviewThumbnailManager == null || !this.mReviewThumbnailManager.isQuickViewAniStarted()) && !this.mGifManager.isGIFEncoding() && this.mCaptureButtonManager != null) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(1)) {
                case 2:
                case 14:
                    onRecordStartButtonClicked();
                    return;
                case 3:
                    onShutterStopButtonClicked();
                    return;
                case 4:
                    if (checkInterval(1)) {
                        onVideoPauseClicked();
                        return;
                    }
                    return;
                case 5:
                    if (checkInterval(1)) {
                        onVideoResumeClicked();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void onShutterTopButtonFocus(boolean pressed) {
    }

    public void onShutterTopButtonLongClickListener() {
        startRecordingPreview();
    }

    protected void startRecordingPreview() {
        setRecordingPreviewState(0, true);
        if (checkRecordingStartCondition()) {
            ShutterButton shutterTop = (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_top_comp);
            if (shutterTop == null) {
                return;
            }
            if (shutterTop.isPressed()) {
                setRecordingPreviewState(1, true);
                onStartRecordingPreview();
                if (this.mStickerManager != null && this.mStickerManager.isStickerDrawing()) {
                    setRecordingPreviewState(2, true);
                }
                onRecordStartButtonClicked();
                return;
            }
            setRecordingPreviewState(0, true);
        }
    }

    protected void onStartRecordingPreview() {
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setEnable(C0088R.id.quick_button_film_emulator, false);
        }
        if (this.mExtraPrevewUIManager != null) {
            this.mExtraPrevewUIManager.setEnable(0, false);
            this.mExtraPrevewUIManager.setEnable(2, false);
            this.mExtraPrevewUIManager.setEnable(3, false);
        }
    }

    protected void onShutterTopTouchUp() {
        ShutterButton shutterTop = (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_top_comp);
        if (shutterTop != null) {
            this.mRecordingButtonPressed = shutterTop.isPressed();
        }
        doShutterTopTouchUp();
    }

    protected void doShutterTopTouchUp() {
        CamLog.m3d(CameraConstants.TAG, "-Recording preview- doShutterTopTouchUp, mRecordingButtonPressed = " + this.mRecordingButtonPressed);
        if (!getRecordingPreviewState(1) || getRecordingPreviewState(8)) {
            CamLog.m3d(CameraConstants.TAG, "recording preview is not started or finishing, return");
        } else if (getRecordingPreviewState(2)) {
            if (this.mExtraPrevewUIManager != null) {
                this.mExtraPrevewUIManager.setEnable(0, true);
                this.mExtraPrevewUIManager.setEnable(2, true);
                this.mExtraPrevewUIManager.setEnable(3, true);
            }
            if (this.mRecordingButtonPressed) {
                CamLog.m3d(CameraConstants.TAG, "-Recording Preview- touch event is release on button");
                if (isShutterKeyOptionTimerActivated()) {
                    setRecordingPreviewState(16, true);
                    doRecordTimerShot();
                    if (!CameraConstants.MODE_POPOUT_CAMERA.equals(getShotMode())) {
                        this.mGet.setPreviewCoverVisibility(8, false, null, true, true);
                        return;
                    }
                    return;
                }
                setRecordingPreviewState(0, true);
                startRecordingAfterRecordingPreview();
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "-Recording Preview- touch event is release on other area");
            setRecordingPreviewState(8, true);
            onShutterStopButtonClicked();
        } else {
            CamLog.m3d(CameraConstants.TAG, "-Recording Preview- delay touch up event after oneshot called");
            setRecordingPreviewState(4, true);
        }
    }

    protected void startRecordingAfterRecordingPreview() {
        AudioUtil.setAudioFocus(getAppContext(), true, false);
        playRecordingSound(true);
        changeCommandButtons();
        this.mReviewThumbnailManager.setThumbnailVisibility(8);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (DefaultCameraModule.this.mStickerManager == null || !DefaultCameraModule.this.mStickerManager.isStickerDrawing()) {
                    if (DefaultCameraModule.this.mCameraDevice != null) {
                        DefaultCameraModule.this.setFlashOnRecord(DefaultCameraModule.this.mCameraDevice.getParameters(), true, true);
                    }
                    DefaultCameraModule.this.doOneShotPreviewCallbackActionForRecording();
                    return;
                }
                DefaultCameraModule.this.stickerRecording();
            }
        }, (long) AudioUtil.getDelayTimeForRecordSound(false));
    }

    private boolean checkAbsoluteCoordinate(View button, Point touchPoint) {
        int[] coordinate = new int[2];
        button.getLocationOnScreen(coordinate);
        CamLog.m3d(CameraConstants.TAG, String.format("-Recording Preview- checkCoordinate, shutterButton x = %d, y = %d", new Object[]{Integer.valueOf(coordinate[0]), Integer.valueOf(coordinate[1])}));
        CamLog.m3d(CameraConstants.TAG, String.format("-Recording Preview- checkCoordinate, touchPoint x = %d, y = %d", new Object[]{Integer.valueOf(touchPoint.x), Integer.valueOf(touchPoint.y)}));
        Rect buttonRect = new Rect();
        buttonRect.left = coordinate[0];
        buttonRect.right = coordinate[0] + button.getWidth();
        buttonRect.top = coordinate[1];
        buttonRect.bottom = coordinate[1] + button.getHeight();
        if (touchPoint.x < buttonRect.left || touchPoint.x > buttonRect.right || touchPoint.y < buttonRect.top || touchPoint.y > buttonRect.bottom) {
            return false;
        }
        return true;
    }

    protected boolean checkRecordingStartCondition() {
        boolean z = true;
        if (!checkModuleValidate(223) || !checkStorageAndLimit() || this.mGet.getPreviewCoverVisibility() == 0 || getCameraState() != 1 || this.mSnapShotChecker.checkMultiShotState(2) || ((this.mTimerManager != null && (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress())) || this.mSnapShotChecker.getSnapShotState() >= 2 || this.mSnapShotChecker.getPictureCallbackState() > 0 || this.mPictureOrVideoSizeChanged || this.mIsPreviewCallbackWaiting || !this.mSnapShotChecker.isAvailableNightAndFlash() || this.mAdvancedFilmManager.checkBlockingButtonState() || isCropZoomStarting() || isAnimationShowing() || this.mGet.isActivatedQuickdetailView() || isActivatedQuickview() || CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equalsIgnoreCase(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null)))) {
            String str = CameraConstants.TAG;
            StringBuilder append = new StringBuilder().append("PreviewCoverVisibility : ").append(this.mGet.getPreviewCoverVisibility()).append("\ncamera state : ").append(getCameraState()).append("\ninterval shot check : ").append(this.mSnapShotChecker.checkMultiShotState(2)).append("\ntimer check : ");
            boolean z2 = this.mTimerManager != null && (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress());
            StringBuilder append2 = append.append(z2).append("\nsnapshot state : ").append(this.mSnapShotChecker.getSnapShotState()).append("\npictureCallback state : ").append(this.mSnapShotChecker.getPictureCallbackState()).append("\nmPictureOrVideoSizeChanged : ").append(this.mPictureOrVideoSizeChanged).append("\nmIsPreviewCallbackWaiting : ").append(this.mIsPreviewCallbackWaiting).append("\nnot available night and flash : ");
            if (this.mSnapShotChecker.isAvailableNightAndFlash()) {
                z = false;
            }
            CamLog.m3d(str, append2.append(z).append("\nblocking button state : ").append(this.mAdvancedFilmManager.checkBlockingButtonState()).append("\nisCropZoomStarting : ").append(isCropZoomStarting()).append("\nisAnimationShowing : ").append(isAnimationShowing()).append("\nisActivatedQuickdetailView : ").append(this.mGet.isActivatedQuickdetailView()).append("\nisActivatedQuickview : ").append(isActivatedQuickview()).append("\nisAssistantNightMode : ").append(CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equalsIgnoreCase(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null))).toString());
            return false;
        } else if (this.mStickerManager != null && this.mStickerManager.prepareStickerDrawing()) {
            CamLog.m7i(CameraConstants.TAG, "perparing sticker drawing");
            return false;
        } else if (TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
            this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_video_recording_during_call));
            return false;
        } else if (!AudioUtil.isInP2PCallMode(this.mGet.getAppContext())) {
            return true;
        } else {
            this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_video_recording_during_call));
            return false;
        }
    }

    public boolean onRecordStartButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "video record clicked.");
        if (!checkRecordingStartCondition()) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "isRecordingPreview = " + getRecordingPreviewState(1));
        IntentBroadcastUtil.stopVoiceRec(this.mGet.getActivity(), true);
        this.mGifManager.setGifVisibility(false);
        this.mGifManager.setGifVisibleStatus(false);
        turnOffCheeseShutter(true);
        setAudioLoopbackOnPreview(false);
        if (this.mQRCodeManager != null) {
            this.mQRCodeManager.setQRLayoutVisibility(8);
        }
        if (this.mQuickButtonManager != null && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)) && isAvailableTilePreview()) {
            this.mQuickButtonManager.setLayoutForTilePreview();
        }
        if (this.mFocusManager != null && isRecordingPriorityMode()) {
            this.mFocusManager.registerEVCallback(false, false);
        }
        if (AudioUtil.isAudioRecording(getAppContext())) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DefaultCameraModule.this.prepareAudioRecording();
                }
            }, 300);
            return AudioUtil.checkAudioAvailabilityBeforeRecording();
        }
        AudioUtil.setAudioAvailability(true);
        prepareRecordingVideo();
        this.mGet.showTilePreview(false);
        return true;
    }

    private boolean checkStorageAndLimit() {
        if (getCurStorage() == 2 && (isUHDmode() || isFHD60())) {
            if (!checkStorage(0, 0)) {
                return false;
            }
        } else if (!checkStorage(true)) {
            return false;
        }
        return true;
    }

    private void prepareAudioRecording() {
        if (this.mGet != null) {
            if (AudioUtil.isAudioRecording(getAppContext())) {
                AudioUtil.setAudioAvailability(false);
                if (this.mToastManager != null) {
                    this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_cannot_start_recording_with_audio));
                }
                setAudioLoopbackOnPreview(true);
                turnOffCheeseShutter(false);
                return;
            }
            AudioUtil.setAudioAvailability(true);
            prepareRecordingVideo();
            this.mGet.showTilePreview(false);
        }
    }

    public void prepareRecordingVideo() {
        if (this.mFocusManager != null && this.mTimerManager != null && this.mReviewThumbnailManager != null) {
            setBatteryIndicatorVisibility(false);
            if (!isShutterKeyOptionTimerActivated() || getRecordingPreviewState(1)) {
                turnOffCheeseShutter(true);
                if (onVideoShutterClickedBefore() && checkRecordingConditionsAfterDelayed()) {
                    onVideoShutterClicked();
                    return;
                }
                return;
            }
            doRecordTimerShot();
        }
    }

    protected void doRecordTimerShot() {
        if (!this.mTimerManager.isTimerShotCountdown()) {
            if (FunctionProperties.isSupportedConeUI()) {
                this.mGet.enableConeMenuIcon(31, false);
            }
            int timerDuration = this.mGet.getAssistantIntFlag(CameraConstantsEx.FLAG_TIMER_DURATION_SECONDS, -1);
            if (!this.mGet.isVoiceAssistantSpecified()) {
                this.mTimerManager.startTimerShot(0, 1);
            } else if (timerDuration > 0) {
                this.mTimerManager.startTimerShot(timerDuration, 1);
            } else if (!this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, false)) {
                this.mTimerManager.startTimerShot(2, 1);
            }
            setQuickButtonEnable(100, false, true);
            this.mGet.hideModeMenu(false, false);
            this.mGet.hideHelpList(false, false);
            if (isMenuShowing(4)) {
                this.mAdvancedFilmManager.showFilmMenu(false, 1, false, false, 0, true);
                this.mExtraPrevewUIManager.changeButtonState(0, false);
            }
            showExtraPreviewUI(false, false, true, true);
            hideManagerForSelfTimer(true);
            if (this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock() || isManualFocusModeEx()) {
                this.mFocusManager.setEVshutterButtonEnable(false);
                this.mFocusManager.setManualFocusButtonEnable(false);
                this.mManualFocusManager.setManualFocusViewEnable(false);
            } else {
                this.mFocusManager.hideAllFocus();
            }
            doCleanView(true, true, false);
            this.mGet.setUspVisibility(8);
            this.mReviewThumbnailManager.setThumbnailVisibility(8, true);
            showFrameGridView(getGridSettingValue(), false);
            handleBinningIconUI(false, 0);
            if (this.mQRCodeManager != null) {
                this.mQRCodeManager.setQRLayoutVisibility(8);
            }
        }
    }

    private boolean checkRecordingConditionsAfterDelayed() {
        if (checkModuleValidate(64) || this.mGet.isPaused()) {
            CamLog.m3d(CameraConstants.TAG, "video shutter clicked - return, camera state = " + getCameraState() + ", isPaused = " + this.mGet.isPaused());
            restoreRecorderToIdle();
            return false;
        } else if (!TelephonyUtil.phoneInCall(this.mGet.getAppContext()) && !AudioUtil.isInP2PCallMode(this.mGet.getAppContext())) {
            return true;
        } else {
            this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_video_recording_during_call));
            CamLog.m3d(CameraConstants.TAG, "video shutter clicked - return, phone in call");
            restoreRecorderToIdle();
            return false;
        }
    }

    public void onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "shutter large clicked.");
        if (checkModuleValidate(15) && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 8:
                    onShutterStopButtonClicked();
                    return;
                case 11:
                    if (!this.mSnapShotChecker.checkMultiShotState(2)) {
                        return;
                    }
                    if (checkModuleValidate(16)) {
                        stopIntervalShot(0);
                        return;
                    } else {
                        this.mSnapShotChecker.setIntervalShotState(8);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public boolean onShutterLargeButtonLongClicked() {
        return false;
    }

    public void onSnapShotButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "snap shot clicked.");
        if (!checkModuleValidate(95) || SystemBarUtil.isSystemUIVisible(getActivity())) {
            CamLog.m3d(CameraConstants.TAG, "snap shot state return false.");
        } else if (this.mStickerManager != null && this.mStickerManager.isRecording()) {
            CamLog.m3d(CameraConstants.TAG, "skip because now sticker recording");
        } else if (!isUHDmode() && isLiveSnapshotSupported()) {
            if (this.mIsSwitchingCameraDuringRecording || isCropZoomStarting()) {
                CamLog.m3d(CameraConstants.TAG, "Camera's switching during the recording, return");
                return;
            }
            if (this.mZoomManager != null) {
                if (this.mZoomManager.isZoomControllersMoving()) {
                    CamLog.m3d(CameraConstants.TAG, "Zoom controller's moving, return");
                    return;
                }
                this.mZoomManager.stopDrawingExceedsLevel();
            }
            if (this.mIntervalChecker != null && this.mIntervalChecker.checkTimeInterval(2)) {
                this.mSnapShotChecker.setSnapShotState(2);
                takePictureInRecording();
            }
        }
    }

    public void onSwapCameraButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "swap button clicked.");
        if (checkModuleValidate(15) && this.mHandler != null) {
            this.mHandler.removeMessages(6);
            this.mHandler.sendEmptyMessage(6);
        }
    }

    public void onExtraButtonClicked(int type) {
        if (checkModuleValidate(15) && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getExtraButtonMode(type)) {
                case 6:
                    onSnapShotButtonClicked();
                    return;
                case 10:
                    onSwapCameraButtonClicked();
                    return;
                default:
                    return;
            }
        }
    }

    public void childSettingMenuClicked(String key, String value, int clickedType) {
        boolean z = true;
        if (checkModuleValidate(15)) {
            super.childSettingMenuClicked(key, value, clickedType);
            if (Setting.KEY_VOICESHUTTER.equals(key)) {
                setCheeseShutterManager();
            } else if (("mode_normal".equals(getShotMode()) || isRecordingPriorityMode()) && (SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId).equals(key) || Setting.KEY_MANUAL_VIDEO_SIZE.equals(key) || Setting.KEY_MANUAL_VIDEO_FRAME_RATE.equals(key))) {
                restoreSteadyCamSetting();
                this.mParamUpdater.setParamValue(ParamConstants.KEY_STEADY_CAM, this.mGet.getCurSettingValue(Setting.KEY_VIDEO_STEADY));
                if (getCurStorage() != 2) {
                    return;
                }
                if ((isUHDmode() || isFHD60() || isFHDCinema60()) && this.mToastManager != null && !this.mToastManager.isShowing()) {
                    this.mToastManager.showShortToast(this.mGet.getAppContext().getString(C0088R.string.uplus_cloud_storage_recording_limit));
                }
            } else if (Setting.KEY_VIDEO_STEADY.equals(key)) {
                if (this.mCameraDevice != null && isAvailableSteadyCam()) {
                    CameraParameters parameters = this.mCameraDevice.getParameters();
                    String steadyValue = this.mGet.getCurSettingValue(Setting.KEY_VIDEO_STEADY);
                    lockSteadyCamSetting();
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_STEADY_CAM, steadyValue);
                    setParameters(parameters);
                }
            } else if (Setting.KEY_TILE_PREVIEW.equals(key)) {
                boolean isThumbnailListOn = "on".equals(value);
                this.mGet.setSetting(Setting.KEY_TILE_PREVIEW, value, true);
                if (isThumbnailListOn) {
                    this.mGet.thumbnailListInit();
                }
                this.mGet.turnOnTilePreview(isThumbnailListOn);
                changeToDefaultPictureAndVideoSize(isThumbnailListOn, true);
                this.mGet.setThumbnailListEnable(isThumbnailListOn);
                if (this.mGet.isActivatedTilePreview()) {
                    ActivityBridge activityBridge = this.mGet;
                    if (isThumbnailListOn) {
                        z = false;
                    }
                    activityBridge.showTilePreviewCoverView(z);
                }
            }
        }
    }

    protected void lockSteadyCamSetting() {
        if (this.mEnableSteadyCamParam != null) {
            setSettingMenuEnable(Setting.KEY_VIDEO_STEADY, false);
            this.mEnableSteadyCamParam.removeCallbacks(this.mEnableSteadyCamSetting);
            this.mEnableSteadyCamParam.postDelayed(this.mEnableSteadyCamSetting, 2000);
        }
    }

    protected boolean isAvailableSteadyCam() {
        if (!FunctionProperties.isSupportedSteadyCamera(isRearCamera())) {
            return false;
        }
        if ((this.mStickerManager != null && this.mStickerManager.isStickerDrawing() && this.mStickerManager.isRunning()) || isUHDmode() || isFHD60() || this.mBinningManager.isBinningEnabled()) {
            return false;
        }
        return true;
    }

    private void setCheeseShutterManager() {
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER))) {
            if (this.mCheeseShutterManager != null) {
                this.mCheeseShutterManager.setCheeseShutterSetting(true, true);
            }
            updateIndicator(1, 0, false);
        } else if (this.mCheeseShutterManager != null) {
            this.mCheeseShutterManager.setCheeseShutterSetting(false, true);
        }
    }

    public boolean onVideoShutterClickedBefore() {
        CamLog.m3d(CameraConstants.TAG, "video shutter clicked - start.");
        if (!checkOnVideoShutterClicked()) {
            return false;
        }
        if (this.mGet.hasRunnable(this.mDetectCameraChangeRunnable)) {
            CamLog.m3d(CameraConstants.TAG, "detect camera changing runnable working");
            return false;
        } else if (this.mZoomManager != null && this.mZoomManager.isZoomControllersMoving()) {
            return false;
        } else {
            setCameraState(5);
            hideMenuOnVideoShutterClicked();
            if (isRecordingSingleZoom() && replaceZoomManager(true)) {
                CameraParameters param = this.mCameraDevice.getParameters();
                this.mZoomManager.initZoomValues(param);
                this.mZoomManager.setZoomValue(param.getZoom());
                this.mZoomManager.updateExtraInfo(Integer.toString(this.mZoomManager.getZoomRatio()));
                if (getCameraId() == 2) {
                    this.mZoomManager.setZoomMaxValue(this.mZoomManager.getSecondCameraMaxZoomLevel());
                    this.mZoomManager.initZoomBarValue(param.getZoom());
                }
            }
            if (!getRecordingPreviewState(1)) {
                AudioUtil.setAudioFocus(getAppContext(), true, false);
            }
            sFirstTaken = true;
            this.mSnapShotChecker.removeMultiShotState(12);
            setQuickClipIcon(true, false);
            handleBinningIconUI(false, 0);
            if (this.mQRCodeManager != null) {
                this.mQRCodeManager.setQRLayoutVisibility(8);
            }
            onStopQRCodeClicked();
            if (isLivePhotoEnabled()) {
                this.mLivePhotoManager.disableLivePhoto();
            }
            if (FunctionProperties.getSupportedHal() == 2) {
                if (isRecordingPriorityMode()) {
                    return true;
                }
                if (this.mStickerManager == null || !this.mStickerManager.isStickerDrawing()) {
                    ListPreference listPref = this.mGet.getListPreference(getVideoSizeSettingKey());
                    if (listPref != null) {
                        int[] videoPreviewSize = Utils.sizeStringToArray(getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue().split("@")[0]));
                        this.mGet.setCameraPreviewSize(videoPreviewSize[0], videoPreviewSize[1]);
                    }
                }
            }
            return true;
        }
    }

    public void onVideoShutterClicked() {
        if (this.mZoomManager == null || !this.mZoomManager.isZoomControllersMoving()) {
            this.mIsMMSRecordingSize = false;
            if (isAudioZoomAvailable() && this.mAudioZoomManager != null) {
                this.mIsMMSRecordingSize = MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).split("@")[0]);
                this.mAudioZoomManager.setAudioZoomHandler(true);
            }
            deleteImmediatelyNotUndo();
            if (FunctionProperties.getSupportedHal() == 2) {
                if (!getRecordingPreviewState(1)) {
                    playRecordingSound(true);
                }
                if (!getRecordingPreviewState(16)) {
                    startRecorder();
                }
            } else {
                if (!getRecordingPreviewState(16)) {
                    startRecorder();
                }
                if (!getRecordingPreviewState(1)) {
                    playRecordingSound(true);
                }
            }
            CamLog.m3d(CameraConstants.TAG, "video shutter clicked - end");
        }
    }

    private boolean checkOnVideoShutterClicked() {
        if (!checkModuleValidate(15) || this.mPictureOrVideoSizeChanged || this.mIsSwitchingCameraDuringRecording) {
            return false;
        }
        if (isRecordingState() && !getRecordingPreviewState(1)) {
            return false;
        }
        if (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isShowingFilmMenu()) {
            int disableFeature = checkFeatureDisableBatteryLevel(2, false);
            if (disableFeature != 0) {
                String messageId;
                if (disableFeature == 1) {
                    messageId = this.mGet.getAppContext().getString(C0088R.string.msg_battery_too_low_to_use_function_slow_motion);
                } else {
                    messageId = this.mGet.getAppContext().getString(C0088R.string.msg_voltage_too_low_to_use_function_slow_motion);
                }
                this.mToastManager.showShortToast(messageId, false);
                return false;
            } else if (TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
                this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_video_recording_during_call));
                return false;
            } else if (MDMUtil.allowMicrophone() && checkPictureCallbackRunnable(true)) {
                return true;
            } else {
                return false;
            }
        }
        this.mAdvancedFilmManager.showFilmMenu(false, 1, false, false, 0, true);
        this.mExtraPrevewUIManager.changeButtonState(0, false);
        showExtraPreviewUI(false, false, true, isRearCamera());
        if (this.mHandler == null) {
            return false;
        }
        this.mHandler.sendEmptyMessageDelayed(90, 100);
        return false;
    }

    private void hideMenuOnVideoShutterClicked() {
        if (this.mCaptureButtonManager != null && this.mReviewThumbnailManager != null && this.mBackButtonManager != null && this.mCheeseShutterManager != null && this.mDoubleCameraManager != null && this.mQuickButtonManager != null) {
            if (isMenuShowing(PointerIconCompat.TYPE_ZOOM_OUT)) {
                hideMenu(PointerIconCompat.TYPE_ZOOM_OUT, checkPreviewCoverVisibilityForRecording(), true);
            }
            setQuickButtonEnable(100, true, true);
            showMenuButton(false);
            setBatteryIndicatorVisibility(false);
            this.mIndicatorManager.updateIndicator(2, 4, false);
            doCleanView(true, false, false);
            if (!getRecordingPreviewState(1)) {
                changeCommandButtons();
                this.mReviewThumbnailManager.setThumbnailVisibility(8);
            }
            this.mBackButtonManager.setBackButton(false);
            this.mCheeseShutterManager.setCheeseShutterSetting(false, true);
            if ((FunctionProperties.isSupportedInAndOutZoom() || isSupportedCropAngle()) && !checkDoubleCameraAvailableMode(false)) {
                this.mDoubleCameraManager.showDualViewControl(false);
            }
            this.mQuickButtonManager.backup();
            this.mQuickButtonManager.hide(false, false, false);
            this.mDotIndicatorManager.hide();
            setFocusStateBeforeStartRecording();
            if (FunctionProperties.isSupportedConeUI()) {
                this.mGet.enableConeMenuIcon(31, false);
            }
            if (isRotateDialogVisible()) {
                removeRotateDialog();
            }
            if (FunctionProperties.hideNaviBarWhileRecording()) {
                SystemBarUtil.setSystemUiVisibility(getActivity(), false);
            }
        }
    }

    protected void changeCommandButtons() {
        if (this.mCaptureButtonManager != null && !CameraConstants.MODE_SNAP.equals(getShotMode())) {
            this.mCaptureButtonManager.changeButtonByMode(2);
            if (isUHDmode() || !isLiveSnapshotSupported() || (this.mStickerManager != null && this.mStickerManager.isStickerDrawing())) {
                this.mCaptureButtonManager.changeExtraButton(0, 1);
                this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
            }
        }
    }

    protected void updateRecordingFlashState(boolean isRecordingStart, boolean hasAutoMode) {
        if (isRecordingStart) {
            if (this.mQuickButtonManager != null) {
                this.mQuickButtonManager.setVisibility(100, 0, false);
                this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 4, false);
                this.mQuickButtonManager.setRotateDegree(getOrientationDegree(), false);
                if (getBinningEnabledState() && isBinningRecordResolutionLimitation()) {
                    this.mQuickButtonManager.setEnable(C0088R.id.quick_button_flash, false, true);
                    return;
                }
                if (hasAutoMode) {
                    if (ParamConstants.FLASH_MODE_TORCH.equals(this.mParamUpdater.getParamValue("flash-mode"))) {
                        this.mQuickButtonManager.setButtonIndex(C0088R.id.quick_button_flash, 1);
                    } else {
                        this.mQuickButtonManager.setButtonIndex(C0088R.id.quick_button_flash, 0);
                    }
                }
                if (!this.mIsNeedToCheckFlashTemperature) {
                    this.mQuickButtonManager.setEnable(C0088R.id.quick_button_flash, false, true);
                }
            }
        } else if (!getBinningEnabledState() || !isBinningRecordResolutionLimitation()) {
            if (hasAutoMode || (!isRearCamera() && this.mCameraCapabilities.isFlashSupported())) {
                ListPreference listPref = getListPreference("flash-mode");
                if (listPref != null) {
                    String str;
                    String str2 = "flash-mode";
                    if ("1".equals(getSettingValue("hdr-mode"))) {
                        str = "off";
                    } else {
                        str = listPref.loadSavedValue();
                    }
                    setSetting(str2, str, false);
                }
                this.mIsNeedToCheckFlashTemperature = true;
            }
        }
    }

    protected void setCAFButtonVisibility() {
    }

    protected void setFocusStateBeforeStartRecording() {
        if (this.mFocusManager != null) {
            this.mFocusManager.hideAndCancelAllFocus(false);
            if (this.mManualFocusManager != null && isManualFocusModeEx()) {
                this.mFocusManager.hideFocusForce();
                this.mFocusManager.setManualFocusButtonVisibility(false);
                this.mManualFocusManager.setManualFocusModeEx(false);
            }
        }
    }

    public void onVideoStopClickedBefore() {
        super.onVideoStopClickedBefore();
        if (this.mDoubleCameraManager != null && ((FunctionProperties.isSupportedInAndOutZoom() || isSupportedCropAngle()) && checkDoubleCameraAvailableMode(false))) {
            this.mDoubleCameraManager.showDualViewControl(false);
        }
        if (FunctionProperties.hideNaviBarWhileRecording()) {
            SystemBarUtil.setSystemUiVisibility(getActivity(), true);
        }
        setZoomUiVisibility(false);
        if (this.mIndicatorManager != null) {
            setBatteryIndicatorVisibility(false);
            this.mIndicatorManager.updateIndicator(5, 4, false);
            this.mIndicatorManager.updateIndicator(2, 4, false);
        }
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.hide();
            this.mRecordingUIManager.destroyLayout();
        }
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setVisibility(100, 4, false);
            this.mQuickButtonManager.restore(true);
            this.mQuickButtonManager.updateButton(100);
            this.mQuickButtonManager.setVisibility(100, 0, false);
        }
        if (FunctionProperties.isLivePhotoSupported() && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_LIVE_PHOTO)) && this.mLivePhotoManager != null) {
            this.mLivePhotoManager.enableLivePhoto();
        }
    }

    public void onVideoStopClicked(boolean useThread, boolean stopByButton) {
        onVideoStopClickedBefore();
        updateButtonsOnVideoStopClicked();
        if (this.mFocusManager != null) {
            this.mFocusManager.cancelTouchAutoFocus();
        }
        setCameraState(8);
        if (!useThread) {
            this.mStopRecorderByThread.run();
        } else if (this.mStopRecordingThread == null || !this.mStopRecordingThread.isAlive()) {
            if (isUHDmode() && !getRecordingPreviewState(1)) {
                CamLog.m3d(CameraConstants.TAG, "saving dialog : UHD recorded");
                showSavingDialog(true, 0);
            }
            this.mStopRecordingThread = new Thread(this.mStopRecorderByThread);
            this.mStopRecordingThread.start();
        } else {
            CamLog.m3d(CameraConstants.TAG, "mStopRecordingThread is alive...so return");
            AudioUtil.setAllSoundCaseMute(getAppContext(), false);
        }
    }

    protected void updateButtonsOnVideoStopClicked() {
        if (!getRecordingPreviewState(1)) {
            updateShutterButtonsOnVideoStopClicked();
            if (!(CameraConstants.MODE_SQUARE_GRID.equals(getShotMode()) || this.mReviewThumbnailManager == null)) {
                this.mReviewThumbnailManager.setThumbnailVisibilityAfterStopRecording();
            }
            if (this.mBackButtonManager != null) {
                this.mBackButtonManager.setBackButton(false);
            }
            if (FunctionProperties.isSupportedConeUI() && !this.mIsGoingToPostview) {
                this.mGet.enableConeMenuIcon(31, true);
            }
            showManagerForSelfTimer();
        }
    }

    protected void updateShutterButtonsOnVideoStopClicked() {
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(3);
        }
    }

    public void onBatteryLevelChanged(int orgLevel, int indiLevel, float voltageLevel) {
        super.onBatteryLevelChanged(orgLevel, indiLevel, voltageLevel);
        if (checkModuleValidate(121) && this.mCameraState >= 1 && this.mGet != null && this.mQuickButtonManager != null && this.mToastManager != null && this.mCameraDevice != null) {
            String messageId;
            int disableFeature = checkFeatureDisableBatteryLevel(2, false);
            if (disableFeature != 0 && (this.mCameraState == 6 || this.mCameraState == 7)) {
                onVideoStopClicked(true, false);
                if (disableFeature == 1) {
                    messageId = this.mGet.getAppContext().getString(C0088R.string.msg_battery_too_low_to_use_function_slow_motion);
                } else {
                    messageId = this.mGet.getAppContext().getString(C0088R.string.msg_voltage_too_low_to_use_function_slow_motion);
                }
                this.mToastManager.showShortToast(messageId, false);
            }
            if (!"off".equals(getSettingValue("flash-mode"))) {
                disableFeature = checkFeatureDisableBatteryLevel(1, false);
                if (disableFeature != 0) {
                    if (disableFeature == 1) {
                        messageId = this.mGet.getAppContext().getString(C0088R.string.msg_battery_too_low_to_use_function_flash);
                    } else {
                        messageId = this.mGet.getAppContext().getString(C0088R.string.msg_voltage_too_low_to_use_function_flash);
                    }
                    this.mToastManager.showShortToast(messageId, false);
                    updateFlashParam(this.mCameraDevice.getParameters(), 50, true);
                    if (!(this.mLocalParamForZoom == null || this.mCameraDevice == null)) {
                        CamLog.m3d(CameraConstants.TAG, "Update zoom parameter");
                        this.mLocalParamForZoom = this.mCameraDevice.getParameters();
                    }
                    this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
                }
            }
        }
    }

    protected void doAfterStopRecorderThread() {
        setRecordingPreviewState(0, true);
        if (isRecordingSingleZoom() && checkDoubleCameraAvailableMode(false) && this.mCameraDevice != null && replaceZoomManager(false)) {
            CameraParameters param = this.mCameraDevice.getParameters();
            this.mZoomManager.initZoomValues(param);
            this.mZoomManager.setZoomValue(param.getZoom());
            this.mZoomManager.updateExtraInfo(Integer.toString(this.mZoomManager.getZoomRatio()));
            this.mZoomManager.initInAndOutZoomBarValue();
        }
        if (checkModuleValidate(1)) {
            afterStopRecording();
            AudioUtil.setAudioFocus(getAppContext(), false);
            return;
        }
        if (this.mCameraState > -2) {
            setCameraState(0);
        }
        this.mRecordingStateBeforeChangingCamera = getCameraState();
        this.mIsSwitchingCameraDuringRecording = false;
    }

    protected void waitPrepareStartRecorderThread() {
        try {
            if (this.mPrepareStartRecordingThread != null && this.mPrepareStartRecordingThread.isAlive()) {
                CamLog.m3d(CameraConstants.TAG, "waitPrepareStartRecorderThread");
                this.mPrepareStartRecordingThread.join();
                this.mPrepareStartRecordingThread = null;
            }
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "Prepare Start recorder thread wait exception : ", e);
        }
    }

    protected void waitStartRecorderThread() {
        try {
            if (this.mStartRecorderThread != null && this.mStartRecorderThread.isAlive()) {
                CamLog.m3d(CameraConstants.TAG, "waitStartRecorderThread");
                this.mStartRecorderThread.join();
                this.mStartRecorderThread = null;
            }
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "Start recorder thread wait exception : ", e);
        }
    }

    protected void waitStopRecordingThread() {
        try {
            if (this.mFilmLiveSnapShotThread != null && this.mFilmLiveSnapShotThread.isAlive()) {
                this.mFilmLiveSnapShotThread.join();
                this.mFilmLiveSnapShotThread = null;
            }
            if (this.mStopRecordingThread != null && this.mStopRecordingThread.isAlive()) {
                this.mStopRecordingThread.join();
                this.mStopRecordingThread = null;
            }
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "Stop recorder thread wait exception : ", e);
        }
    }

    public void playRecordingSound(boolean start) {
        if (!start) {
            AudioUtil.camcorderEndSound(getAppContext());
        }
        this.mGet.playSound(3, start, 0);
    }

    public boolean setParamForVideoRecord(boolean recordStart, ListPreference listPref) {
        return setParamForVideoRecord(recordStart, listPref, this.mStartRecorderInfo.mVideoSize);
    }

    public boolean setParamForVideoRecord(boolean recordStart, ListPreference listPref, String videoSize) {
        if (this.mCameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera device is null.");
            return false;
        }
        CameraParameters parameters = updateDeviceParameter();
        if (parameters == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera parameter is null.");
            return false;
        } else if (recordStart) {
            return doSetParamForStartRecording(parameters, recordStart, listPref, videoSize);
        } else {
            return doSetParamForStopRecording(parameters, recordStart, videoSize);
        }
    }

    protected boolean doSetParamForStartRecording(CameraParameters parameters, boolean recordStart, ListPreference listPref, String videoSize) {
        if (listPref == null || videoSize == null) {
            return false;
        }
        String previewSize = listPref.getExtraInfo(1);
        String screenSize = listPref.getExtraInfo(2);
        sVideoSize = Utils.sizeStringToArray(screenSize);
        if (isFHD60()) {
            setTrackingFocusState(false);
            ViewUtil.setPatialUpdate(getAppContext(), false);
        }
        if (isUHDmode()) {
            setTrackingFocusState(false);
        }
        setDefaultRecordingParameters(parameters, videoSize, recordStart);
        checkHeatingConditionForFlashOff();
        setVideoSizeAndRestartPreview(parameters, previewSize, screenSize, videoSize);
        setMicPath();
        if (this.mCameraDevice != null) {
            setParameterByLGSF(this.mCameraDevice.getParameters(), true);
        }
        setHDRMetaDataCallback(null);
        setFlashMetaDataCallback(null);
        if (this.mCameraDevice == null || !isFastShotSupported()) {
            return true;
        }
        this.mCameraDevice.setLongshot(false);
        return true;
    }

    protected void setVideoSizeAndRestartPreview(CameraParameters parameters, String previewSize, String screenSize, String videoSize) {
        if (this.mParamUpdater != null && parameters != null) {
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_VIDEO_SIZE, videoSize);
            if (this.mBinningManager.isBinningEnabled()) {
                if (isBinningRecordResolutionLimitation()) {
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_BINNING_PARAM, ParamConstants.VALUE_BINNING_MODE);
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_APP_OUTPUTS_TYPE, this.mBinningManager.getBinningRecordingOutputConfig());
                    this.mParamUpdater.setParameters(parameters, "picture-size", this.mBinningManager.getBinningPictureSize(this.mCameraId));
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PICTURE_SIZE_WIDE, this.mBinningManager.getBinningPictureSize(this.mCameraId));
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_APP_BINNING_TYPE, getBinningType());
                } else {
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_BINNING_PARAM, "normal");
                    this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_DEFAULT_STR);
                }
            }
            this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
            setPreviewSize(parameters, getPreviewSize(previewSize, screenSize, videoSize), true);
        }
    }

    protected void setDefaultRecordingParameters(CameraParameters parameters, String videoSize, boolean isRecordStart) {
        setDefaultRecordingParameters(parameters, videoSize, isRecordStart, false);
    }

    protected void setDefaultRecordingParameters(CameraParameters parameters, String videoSize, boolean isRecordStart, boolean isCameraChanging) {
        setRecordingFocusParameter(parameters, isCameraChanging);
        if (getRecordingPreviewState(1)) {
            setFlashOnRecord(parameters, false, false);
        } else {
            setFlashOnRecord(parameters, true, false);
        }
        setZSL(parameters, "off");
        setZSLBuffCount(parameters, "1");
        setRecordingHint(parameters, "true", false);
        setPreviewFpsRange(parameters, true);
        setLiveSnapshotSize(parameters, videoSize);
        setVideoHDR(parameters, isRecordStart);
    }

    protected void setRecordingFocusParameter(CameraParameters parameters, boolean isCameraChanging) {
        if (parameters != null && this.mFocusManager != null) {
            if (!isRecordingPriorityMode() || isCameraChanging) {
                setRecordingEVParameters(parameters);
                setContinuousFocus(parameters, true);
                parameters.setMeteringAreas(null);
            }
            if (parameters.isAutoExposureLockSupported()) {
                parameters.setAutoExposureLock(false);
            }
            if (parameters.isAutoWhiteBalanceLockSupported()) {
                parameters.setAutoWhiteBalanceLock(false);
            }
        }
    }

    protected void setRecordingEVParameters(CameraParameters parameters) {
        if (parameters != null) {
            parameters.setExposureCompensation(0);
        }
    }

    protected boolean doSetParamForStopRecording(CameraParameters parameters, boolean recordStart, String videoSize) {
        ViewUtil.setPatialUpdate(getAppContext(), true);
        resetMicPath();
        if (this.mCameraIdBeforeInAndOutZoom == this.mCameraId) {
            revertParameterByLGSF(parameters);
        }
        setContinuousFocus(parameters, false);
        this.mFlipManager.setForceVideoFlipParam(this.mCameraId, parameters, 0, false, false);
        setPreviewFpsRange(parameters, recordStart);
        setPreviewSize(parameters, videoSize, false);
        setParamUpdater(parameters, "flash-mode", "off");
        String curZoomValue = this.mParamUpdater.getParamValue("zoom");
        if (!(curZoomValue == null || "not found".equals(curZoomValue))) {
            parameters.setZoom(Integer.valueOf(curZoomValue).intValue());
        }
        this.mCameraDevice.setParameters(parameters);
        if (FunctionProperties.isSupportedHDR(isRearCamera()) >= 3) {
            setHDRMetaDataCallback(getSettingValue("hdr-mode"));
        }
        updateRecordingFlashState(false, isRearCamera());
        setFlashMetaDataCallback(getSettingValue("flash-mode"));
        if (this.mCameraDevice != null) {
            if (isFastShotAvailable(3)) {
                this.mCameraDevice.setLongshot(true);
            }
            this.mCameraDevice.setRecordSurfaceToTarget(false);
        }
        return true;
    }

    public void disableCheeseShutterByCallPopup() {
        setSettingMenuEnable(Setting.KEY_VOICESHUTTER, false);
        if (this.mCheeseShutterManager != null) {
            this.mCheeseShutterManager.setCheeseShutterSetting(false, true);
        }
    }

    protected void setDefaultFocusParam() {
        if (this.mParamUpdater != null) {
            this.mParamUpdater.setParamValue("focus-mode", getFocusSetting());
        }
    }

    protected void setDefaultSettingValueAndDisable(String key, boolean save) {
        ListPreference listPref = getListPreference(key);
        if (listPref != null) {
            setSettingMenuEnable(key, false);
            setSetting(key, listPref.getDefaultValue(), save);
        }
    }

    public void restoreSettingValue(String key) {
        if (key != null) {
            ListPreference listPref = getListPreference(key);
            if (listPref != null) {
                setSetting(key, listPref.loadSavedValue(), true);
            }
            if (FunctionProperties.isSupportedInAndOutZoom()) {
                int anotherCameraId = this.mCameraId == 0 ? 2 : 0;
                String anotherCameraIdSettingKey;
                if (key.contains("picture-size")) {
                    anotherCameraIdSettingKey = SettingKeyWrapper.getPictureSizeKey(getShotMode(), anotherCameraId);
                    listPref = getListPreference(anotherCameraIdSettingKey);
                    if (listPref != null) {
                        setSetting(anotherCameraIdSettingKey, listPref.loadSavedValue(), true);
                    }
                } else if (key.contains(Setting.KEY_VIDEO_RECORDSIZE)) {
                    anotherCameraIdSettingKey = SettingKeyWrapper.getVideoSizeKey(getShotMode(), anotherCameraId);
                    listPref = getListPreference(anotherCameraIdSettingKey);
                    if (listPref != null) {
                        setSetting(anotherCameraIdSettingKey, listPref.loadSavedValue(), true);
                    }
                }
            }
        }
    }

    public void restoreFlashSetting() {
        if (checkFeatureDisableBatteryLevel(1, false) != 0) {
            this.mGet.setSetting("flash-mode", "off", false);
        } else {
            restoreSettingValue("flash-mode");
        }
    }

    public boolean isMMSRecording() {
        boolean isMMS = MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).split("@")[0]);
        if (isMMSIntent() || isMMS) {
            return true;
        }
        return false;
    }

    protected boolean isMiracastState() {
        try {
            WfdManager wfdManager = (WfdManager) new LGContext(getAppContext()).getLGSystemService("wfdService");
            if (wfdManager != null && wfdManager.getWfdState() == 5) {
                return true;
            }
        } catch (SecurityException e) {
            CamLog.m3d(CameraConstants.TAG, "Security exception occured!!!!");
        }
        return false;
    }

    protected void initRecorder(StartRecorderInfo info) {
        boolean surfaceRecording = false;
        if (info != null) {
            VideoRecorder.setMaxDuration(this.mLimitRecordingDuration);
            VideoRecorder.setMaxFileSize(this.mLimitRecordingSize, this.mStorageManager.getFreeSpace(info.mStorageType), info.mStorageType);
            VideoRecorder.setFileName(info.mFileName);
            VideoRecorder.setFilePath(info.mOutFilePath);
            VideoRecorder.setOrientationHint(getVideoOrientation());
            if (FunctionProperties.getSupportedHal() != 2) {
                VideoRecorder.setWaitStartRecoding(true);
            }
            int purpose = info.mPurpose;
            if (!(CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()) || isManualMode() || (this.mAdvancedFilmManager.isRunningFilmEmulator() && !isUHDmode() && !isFHD60() && !CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))))) {
                purpose = (purpose & -3) | 1;
                info.mPurpose = purpose;
            }
            if (!isRecordingInAndOutZoomPlanA()) {
                surfaceRecording = checkDoubleCameraAvailableMode(false);
            }
            Camera camera = null;
            if (FunctionProperties.getSupportedHal() != 2) {
                camera = (Camera) this.mCameraDevice.getCamera();
            }
            VideoRecorder.init(camera, this.mCameraId, info.mVideoSize, this.mLocationServiceManager.getCurrentLocation(), purpose, info.mVideoFps, info.mVideoBitrate, getRecordingType(), surfaceRecording, info.mVideoFlipType);
            VideoRecorder.resetWaitStartRecoding();
        }
    }

    protected void startRecorder() {
        CamLog.m3d(CameraConstants.TAG, "startRecorder");
        if (!(CameraConstants.MODE_SNAP.equals(getShotMode()) || CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()))) {
            AudioUtil.setUseBuiltInMicForRecording(getAppContext(), false);
        }
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.initVideoTime();
        }
        IntentBroadcastUtil.stopVoiceRec(this.mGet.getActivity(), true);
        if (this.mToastManager.isShowing()) {
            this.mToastManager.hideAndResetDisturb(0);
        }
        if (this.mStickerManager == null || !this.mStickerManager.isStickerDrawing()) {
            if (FunctionProperties.getSupportedHal() == 2) {
                stopPreview();
            }
            setCameraState(5);
            this.mStartRecorderInfo = makeRecorderInfo();
            final ListPreference listPref = getListPreference(getVideoSizeSettingKey());
            if (listPref == null) {
                CamLog.m5e(CameraConstants.TAG, "KEY_VIDEO_RECORDSIZE listPref is null in startRecorder");
                return;
            } else {
                this.mPrepareStartRecordingThread = new Thread(new Runnable() {
                    public void run() {
                        if (!DefaultCameraModule.this.getRecordingPreviewState(16)) {
                            DefaultCameraModule.this.postOnUiThread(DefaultCameraModule.this.mShowRecordingCoverRunnable, 0);
                        }
                        if (FunctionProperties.getSupportedHal() == 2) {
                            DefaultCameraModule.this.initRecorder(DefaultCameraModule.this.mStartRecorderInfo);
                        }
                        if (!DefaultCameraModule.this.mGet.isPaused() && DefaultCameraModule.this.mStartRecorderInfo != null && !DefaultCameraModule.this.setParamForVideoRecord(true, listPref, DefaultCameraModule.this.mStartRecorderInfo.mVideoSize)) {
                            DefaultCameraModule.this.runOnUiThread(new HandlerRunnable(DefaultCameraModule.this) {
                                public void handleRun() {
                                    DefaultCameraModule.this.restoreRecorderToIdle();
                                    CamLog.m5e(CameraConstants.TAG, "video statrt record param update failed");
                                }
                            });
                        }
                    }
                });
                this.mPrepareStartRecordingThread.start();
            }
        } else if (!getRecordingPreviewState(1)) {
            setCameraState(5);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DefaultCameraModule.this.stickerRecording();
                }
            }, (long) AudioUtil.getDelayTimeForRecordSound(false));
        }
        this.mIsFileSizeLimitReached = false;
        this.mIsUHDRecTimeLimitWarningDisplayed = false;
        if (this.mLightFrameManager.isLightFrameMode()) {
            this.mLightFrameManager.turnOffLightFrame();
            this.mAdvancedFilmManager.turnOnLightFrame(false);
        }
        this.mCameraIdBeforeInAndOutZoom = this.mCameraId;
    }

    private void stickerRecording() {
        if (!checkModuleValidate(15) || this.mCameraDevice == null || this.mRecordingUIManager == null || this.mLocationServiceManager == null) {
            this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
            restoreRecorderToIdleOnUIThread();
            return;
        }
        IntentBroadcastUtil.blockAlarmInRecording(this.mGet.getActivity(), true);
        if (isPaused()) {
            CamLog.m11w(CameraConstants.TAG, "App is pausing, can not start recording a video");
            return;
        }
        if (AudioUtil.isWiredHeadsetHasMicOn()) {
            showHeadsetRecordingToastPopup();
        }
        String dir = getCurDir();
        AudioUtil.setAllSoundCaseMute(getAppContext(), true);
        setCameraState(6);
        this.mOrientationAtStartRecording = getActivity().getResources().getConfiguration().orientation;
        startHeatingWarning(true);
        this.mRecordingUIManager.initLayout();
        updateRecordingUi();
        keepScreenOn();
        int storage = getCurStorage();
        if (isCnasRecordingLimitation(storage)) {
            storage = 0;
        }
        String fileName = makeFileName(1, storage, dir, false, getSettingValue(Setting.KEY_MODE));
        if (isMMSRecording()) {
            this.mLimitRecordingSize = MmsProperties.getMmsVideoSizeLimit(this.mGet.getAppContext().getContentResolver());
            this.mLimitRecordingDuration = MultimediaProperties.getMMSMaxDuration();
            VideoRecorder.setMaxDuration(this.mLimitRecordingDuration);
            VideoRecorder.setMaxFileSize(this.mLimitRecordingSize, this.mStorageManager.getFreeSpace(storage), storage);
            VideoRecorder.setFilePath(dir + fileName + ".mp4");
        }
        this.mStickerManager.startRecording(dir, fileName, this.mStickerContentCallback);
        if (FunctionProperties.getSupportedHal() == 2 && this.mCameraDevice != null) {
            this.mCameraDevice.startFaceDetection();
            this.mCameraDevice.setPreviewSurfaceToTarget(false);
        }
    }

    public boolean isCnasRecordingLimitation(int storage) {
        return storage == 2 && (isUHDmode() || isFHD60());
    }

    protected StartRecorderInfo makeRecorderInfo() {
        double videoFps;
        int storage = getCurStorage();
        boolean isCNasLimitation = isCnasRecordingLimitation(storage);
        if (isCNasLimitation) {
            storage = 0;
        }
        String dir = (!isCNasLimitation || this.mStorageManager == null) ? getCurDir() : this.mStorageManager.getDir(storage);
        String videoSizeStr = getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        String[] removeAt = videoSizeStr.split("@");
        String videoSize = removeAt[0];
        String prefFps = removeAt.length > 1 ? removeAt[1] : null;
        boolean isMMS = MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), videoSize);
        int purpose = 1;
        if (this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            purpose = 2;
        }
        String extend = ".mp4";
        double fps = 30.0d;
        if (isMMS) {
            purpose |= 4;
            extend = MultimediaProperties.VIDEO_EXTENSION_3GP;
            fps = 15.0d;
        }
        if (CameraConstants.VIDEO_FHD_60FPS.equals(videoSizeStr)) {
            fps = 60.0d;
        }
        if (prefFps != null) {
            videoFps = (double) Integer.valueOf(prefFps).intValue();
        } else {
            videoFps = fps;
        }
        String fileName = makeFileName(1, storage, dir, false, getSettingValue(Setting.KEY_MODE));
        String outFilePath = dir + fileName + extend;
        CamLog.m3d(CameraConstants.TAG, "output file is : " + outFilePath);
        setVideoLimitSize();
        boolean flip = "off".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION));
        if (CameraConstants.MODE_SQUARE_GRID.equals(getShotMode()) && !isRearCamera()) {
            flip = false;
        }
        return new StartRecorderInfo(storage, fileName, outFilePath, videoSize, purpose, videoFps, 0, CameraDeviceUtils.getVideoFlipType(getVideoOrientation(), this.mCameraId, flip, false));
    }

    protected void setRecordingIndicator() {
        if (this.mIndicatorManager != null) {
            if (isAvailableSteadyCam() && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_VIDEO_STEADY))) {
                this.mIndicatorManager.showSceneIndicator(5);
            }
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isVideoHDRSupported(this.mCameraId) && "1".equals(this.mGet.getCurSettingValue("hdr-mode")) && !isFHD60()) {
                this.mIndicatorManager.showSceneIndicator(2);
            }
        }
    }

    public boolean isJogZoomMoving() {
        if (this.mZoomManager == null) {
            return false;
        }
        return this.mZoomManager.isJogZoomMoving();
    }

    public void onChangeZoomMinimapVisibility(boolean show) {
    }

    public boolean isZoomControllerTouched() {
        if (this.mZoomManager == null) {
            return false;
        }
        return this.mZoomManager.isZoomControllersGetTouched();
    }

    public boolean isGestureZooming() {
        if (this.mZoomManager == null) {
            return false;
        }
        return this.mZoomManager.isGestureZooming();
    }

    public synchronized void runStartRecorder(boolean useThread) {
        if (this.mStartRecorderInfo != null) {
            if (this.mStartRecorderInfo.mNeedRun) {
                this.mStartRecorderInfo.mNeedRun = false;
            } else {
                CamLog.m3d(CameraConstants.TAG, "exit runStartRecorder because recording is already started");
            }
        }
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setRecordSurfaceToTarget(true);
        }
        Runnable startRecorderRunnable = new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "- gesture - startRecorderRunnable");
                DefaultCameraModule.this.doRunnableStartRecorder(DefaultCameraModule.this.mStartRecorderInfo);
            }
        };
        CamLog.m3d(CameraConstants.TAG, "runStartRecorder - useThread : " + useThread);
        if (useThread) {
            this.mStartRecorderThread = new Thread(startRecorderRunnable);
            this.mStartRecorderThread.start();
        } else {
            this.mGet.postOnUiThread(startRecorderRunnable, 0);
        }
    }

    public int getVideoOrientation() {
        int degree = (getDisplayOrientation() + getOrientationDegree()) % 360;
        return (this.mAdvancedFilmManager == null || !this.mAdvancedFilmManager.isRunningFilmEmulator() || isRearCamera() || CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR))) ? CameraDeviceUtils.getOrientationHint(degree, this.mCameraId) : degree;
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        if (!checkModuleValidate(15) || this.mCameraDevice == null || this.mRecordingUIManager == null || this.mStorageManager == null || this.mLocationServiceManager == null || info == null || this.mAdvancedFilmManager == null) {
            this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
            restoreRecorderToIdleOnUIThread();
            return;
        }
        if (FunctionProperties.getSupportedHal() != 2) {
            initRecorder(this.mStartRecorderInfo);
        }
        if (VideoRecorder.isInitialized()) {
            this.mAdvancedFilmManager.prepareRecording(info.mVideoSize, true);
            MediaRecorderListener listener = new MediaRecorderListener();
            VideoRecorder.setInfoListener(listener);
            VideoRecorder.setErrorListener(listener);
            IntentBroadcastUtil.blockAlarmInRecording(this.mGet.getActivity(), true);
            if (VideoRecorder.start()) {
                if (!isPaused()) {
                    this.mAdvancedFilmManager.startRecorder();
                }
                prepareLoopRecording(info);
                if (AudioUtil.isWiredHeadsetHasMicOn()) {
                    showHeadsetRecordingToastPopup();
                }
                this.mAdvancedFilmManager.showFilmLimitPopupForNormalMode();
                if (getCurStorage() == 2 && (isUHDmode() || isFHD60() || isFHDCinema60())) {
                    this.mToastManager.showShortToast(this.mGet.getAppContext().getString(C0088R.string.uplus_cloud_storage_recording_limit));
                }
                AudioUtil.setAllSoundCaseMute(getAppContext(), true);
                updateDeviceParameter();
                setCameraState(6);
                this.mOrientationAtStartRecording = getActivity().getResources().getConfiguration().orientation;
                startHeatingWarning(true);
                this.mStartRecorderInfo = null;
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (DefaultCameraModule.this.mRecordingUIManager != null && !DefaultCameraModule.this.mGet.isPaused() && DefaultCameraModule.this.mCameraState == 6) {
                            DefaultCameraModule.this.mRecordingUIManager.initLayout();
                            DefaultCameraModule.this.updateRecordingUi();
                            DefaultCameraModule.this.keepScreenOn();
                        }
                    }
                });
                return;
            }
            restoreRecorderToIdleOnUIThread();
            this.mToastManager.showShortToast(getActivity().getString(isMiracastState() ? C0088R.string.error_video_recording_during_miracast : C0088R.string.error_occurred));
            return;
        }
        this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
        restoreRecorderToIdleOnUIThread();
    }

    private void prepareLoopRecording(StartRecorderInfo info) {
        CamLog.m3d(CameraConstants.TAG, "prepareLoopRecording");
        this.mLoopRecorderInfo = null;
        this.mTempLoopRecorderInfo = null;
        VideoRecorder.setLoopState(0);
        if (getLoopRecordingType() == 0) {
            CamLog.m3d(CameraConstants.TAG, "loop recording not suppport shot mode");
            return;
        }
        VideoRecorder.makeNewUUID();
        if (getLoopRecordingType() == 2 || (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator())) {
            setLoopRecordFilePath();
            Camera camera = null;
            if (FunctionProperties.getSupportedHal() != 2) {
                camera = (Camera) this.mCameraDevice.getCamera();
                startHeatingWarning(false);
            }
            VideoRecorder.init(false, camera, this.mCameraId, info.mVideoSize, this.mLocationServiceManager.getCurrentLocation(), info.mPurpose, info.mVideoFps, info.mVideoBitrate, getRecordingType(), false, info.mVideoFlipType);
        }
    }

    protected int getLoopRecordingType() {
        return FunctionProperties.isGaplessLoopRecordingSupported() ? 3 : 1;
    }

    protected void updateRecordingUi() {
        boolean z = false;
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.updateVideoTime(1, SystemClock.uptimeMillis());
            this.mRecordingUIManager.updateRecStatusIcon();
            this.mRecordingUIManager.show(isNeedProgressBar());
            updateRecordingTime();
            checkThemperatureOnRecording(true);
            setRecordingIndicator();
            if (this.mDoubleCameraManager != null && ((FunctionProperties.isSupportedInAndOutZoom() || isSupportedCropAngle()) && checkDoubleCameraAvailableMode(false))) {
                this.mDoubleCameraManager.showDualViewControl(true);
            }
            if (this.mIndicatorManager != null) {
                this.mIndicatorManager.initIndicatorListAndLayout();
                setBatteryIndicatorVisibility(true);
            }
            if (isZoomAvailable(false)) {
                setZoomUiVisibility(true);
            }
            if (!isRearCamera()) {
                z = true;
            }
            setQuickButtonByPreset(true, z);
            updateRecordingFlashState(true, isRearCamera());
            setCAFButtonVisibility();
            if (this.mCaptureButtonManager != null) {
                setCaptureButtonEnable(true, 3);
            }
        }
    }

    public String getRecordingType() {
        if (CameraConstants.MODE_SNAP.equals(getShotMode())) {
            return CameraConstants.VIDEO_SNAP_TYPE;
        }
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            return CameraConstants.VIDEO_SNAP_SHOT_TYPE;
        }
        if (CameraConstants.MODE_SLOW_MOTION.equals(getShotMode())) {
            return CameraConstants.VIDEO_SLOMO_TYPE;
        }
        int[] size = Utils.sizeStringToArray(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).split("@")[0]);
        if (size[0] == 0 || Float.compare(((float) size[1]) / ((float) size[0]), 0.5f) != 0) {
            return String.valueOf(0);
        }
        return CameraConstants.VIDEO_18_9_TYPE;
    }

    protected void checkThemperatureOnRecording(boolean enable) {
    }

    protected void setSSRSetting(boolean enable) {
    }

    protected void setAudioLoopbackOnPreview(boolean enable) {
    }

    protected void setAudioLoopbackInRecording(boolean enable) {
    }

    public void setZoomUiVisibility(boolean visibility) {
        if (this.mZoomManager != null) {
            if (!visibility) {
                this.mZoomManager.setJogZoomVisibility(false);
                this.mZoomManager.setJogZoomMinimapVisibility(8);
                hideZoomBar();
                this.mZoomManager.setZoomButtonVisibility(8);
            } else if (isZoomAvailable()) {
                this.mZoomManager.setJogZoomVisibility(true);
                if (checkDoubleCameraAvailableMode(false)) {
                    this.mZoomManager.setRecordingZoomBtnPosition(isManualMode());
                    this.mZoomManager.setZoomButtonVisibility(0);
                }
            }
        }
    }

    public void setZoomUiVisibility(boolean visibility, int id) {
        if (this.mZoomManager != null) {
            if (!visibility || isZoomAvailable()) {
                switch (id) {
                    case 0:
                        setZoomUiVisibility(visibility);
                        return;
                    case 1:
                        if (visibility) {
                            showZoomBar();
                            return;
                        } else {
                            hideZoomBar();
                            return;
                        }
                    case 2:
                        if (!visibility) {
                            this.mZoomManager.setZoomButtonVisibility(8);
                            return;
                        } else if (checkDoubleCameraAvailableMode(false) && !checkModuleValidate(128)) {
                            this.mZoomManager.setRecordingZoomBtnPosition(isManualMode());
                            this.mZoomManager.setZoomButtonVisibility(0);
                            return;
                        } else {
                            return;
                        }
                    case 3:
                        this.mZoomManager.setJogZoomVisibility(visibility);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    protected void restoreRecorderToIdleOnUIThread() {
        runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                DefaultCameraModule.this.restoreRecorderToIdle();
            }
        });
    }

    protected void restoreRecorderToIdle() {
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.enableConeMenuIcon(31, true);
        }
        if (FunctionProperties.hideNaviBarWhileRecording()) {
            SystemBarUtil.setSystemUiVisibility(getActivity(), true);
        }
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(3);
        }
        if (!(this.mReviewThumbnailManager == null || isAttachIntent())) {
            this.mReviewThumbnailManager.setThumbnailVisibility(0);
        }
        if (this.mBackButtonManager != null) {
            this.mBackButtonManager.setBackButton(false);
        }
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.hide();
        }
        if (this.mCameraState < 1) {
            CamLog.m7i(CameraConstants.TAG, "Camera device was closed already, so can not restore to idle");
            return;
        }
        try {
            if (this.mCameraDevice != null) {
                this.mCameraDevice.lock();
                CamLog.m3d(CameraConstants.TAG, "### mCameraDevice.reconnect()");
                this.mCameraDevice.reconnect(this.mHandler, null);
                CamLog.m3d(CameraConstants.TAG, "### camera reconnected");
            }
        } catch (RuntimeException e) {
            CamLog.m3d(CameraConstants.TAG, "cameraDevice.lock() or reconnect() RuntimeException: " + e);
            e.printStackTrace();
        }
        setZoomUiVisibility(false);
        setCameraState(1);
        this.mStartRecorderInfo = null;
        setCaptureButtonEnable(false, getShutterButtonType());
        this.mIsFileSizeLimitReached = false;
        if (this.mFocusManager != null) {
            this.mFocusManager.cancelTouchAutoFocus();
        }
        playRecordingSound(false);
        this.mAllowPause = true;
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.restore(true);
            this.mQuickButtonManager.show(false, false, false);
        }
        hideZoomBar();
        IntentBroadcastUtil.unblockAlarmInRecording(this.mGet.getActivity());
        CamLog.m3d(CameraConstants.TAG, "send CLEAR_SCREEN_DELAY message");
        keepScreenOnAwhile();
        if (!(this.mLightFrameManager == null || this.mAdvancedFilmManager == null || !this.mLightFrameManager.isLightFrameMode())) {
            this.mLightFrameManager.turnOnLightFrame();
            this.mAdvancedFilmManager.turnOnLightFrame(true);
        }
        setParamForVideoRecord(false, getListPreference(getVideoSizeSettingKey()), getSettingValue(getVideoSizeSettingKey()).split("@")[0]);
        startHeatingWarning(false);
        if (checkPreviewCoverVisibilityForRecording()) {
            showFrameGridView("off", false);
            this.mGet.setPreviewCoverVisibility(0, true);
        }
        afterStopRecording();
    }

    protected void stopRecorder() {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.stopRecorder(false);
        }
        stopRecorder(true);
    }

    protected void stopRecorder(boolean useDB) {
        if (checkModuleValidate(192)) {
            AudioUtil.setAllSoundCaseMute(getAppContext(), false);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "stopRecorder.");
        IntentBroadcastUtil.unblockAlarmInRecording(this.mGet.getActivity());
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "send CLEAR_SCREEN_DELAY message");
                DefaultCameraModule.this.keepScreenOnAwhile();
            }
        });
        if (FunctionProperties.getSupportedHal() == 2) {
            stopPreview();
            setCameraState(8);
        }
        this.mIsFileSizeLimitReached = false;
        videoRecorderRelease();
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                DefaultCameraModule.this.hideZoomBar();
            }
        });
        renameTempLoopFile();
        saveFile(useDB);
        if (this.mTempLoopRecorderInfo != null) {
            VideoRecorder.clearEmptyVideoFile(this.mTempLoopRecorderInfo.mOutFilePath, true);
        } else if (VideoRecorder.getLoopState() == 2) {
            VideoRecorder.clearEmptyVideoFile(this.mLoopTempFilePath, true);
        }
        startHeatingWarning(false);
        AudioUtil.setAllSoundCaseMute(getAppContext(), false);
        if (!getRecordingPreviewState(1)) {
            doPlayRecordingStopSound();
        }
        AudioUtil.enableRaM(getAppContext(), true);
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if ("on".equals(DefaultCameraModule.this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)) && DefaultCameraModule.this.isAvailableTilePreview()) {
                    DefaultCameraModule.this.mGet.showTilePreview(true);
                    DefaultCameraModule.this.mQuickButtonManager.setLayoutForTilePreview();
                }
            }
        });
        if (FunctionProperties.getSupportedHal() != 2) {
            stopPreview();
        }
        checkStorage();
    }

    protected void doPlayRecordingStopSound() {
        playRecordingSound(false);
    }

    private void checkRecordingMaxFileSize() {
        CamLog.m3d(CameraConstants.TAG, "MAX_FILESIZE_REACHED safe freespace = " + this.mFreeSpaceForSafeRecording);
        if (VideoRecorder.getMaxFileSize() > this.mFreeSpaceForSafeRecording) {
            long reducedFileSize = VideoRecorder.getMaxFileSize() - this.mFreeSpaceForSafeRecording;
            CamLog.m3d(CameraConstants.TAG, "MAX_FILESIZE_REACHED reducedFileSize = " + reducedFileSize);
            VideoRecorder.setMaxFileSize(this.mFreeSpaceForSafeRecording, this.mFreeSpaceForSafeRecording, VideoRecorder.getStorageType());
            VideoRecorder.changeMaxFileSize(reducedFileSize);
        }
    }

    protected void saveFile(boolean useDB) {
        String videoSize = getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).split("@")[0];
        String fileName = VideoRecorder.getFileName();
        String outFilePath = VideoRecorder.getFilePath();
        String uuid = "";
        if (this.mLoopRecorderInfo != null && VideoRecorder.getLoopState() == 3) {
            fileName = this.mLoopRecorderInfo.mFileName;
            outFilePath = this.mLoopRecorderInfo.mOutFilePath;
        }
        if (VideoRecorder.getLoopState() != 0) {
            uuid = VideoRecorder.getUUID();
        }
        setParamForVideoRecord(false, getListPreference(getVideoSizeSettingKey()), videoSize);
        if (!getRecordingPreviewState(1)) {
            saveFile(useDB, videoSize, fileName, outFilePath, uuid);
        }
    }

    protected void saveFile(boolean useDB, String videoSize, String fileName, String outFilePath) {
        saveFile(useDB, videoSize, fileName, outFilePath, "");
    }

    private boolean isNeedCheckFileRecTime() {
        if (CameraConstants.MODE_SQUARE_GRID.equals(getShotMode())) {
            return false;
        }
        if (!CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            return true;
        }
        if ("off".equals(getSettingValue(Setting.KEY_MULTIVIEW_FRAMESHOT))) {
            return true;
        }
        return false;
    }

    protected void saveFile(boolean useDB, String videoSize, String fileName, String outFilePath, String uuid) {
        boolean deleteFile = false;
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.updateVideoTime(3, SystemClock.uptimeMillis());
            this.mRecordingUIManager.setRecDurationTime((long) getRecCompensationTime());
            if (VideoRecorder.getLoopState() != 0) {
                deleteFile = getRecDurationTime(outFilePath) < ((long) MultimediaProperties.getMinRecordingTime());
            } else if (isNeedCheckFileRecTime()) {
                deleteFile = !this.mRecordingUIManager.checkMinRecTime();
            }
        }
        int purpose = MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), videoSize) ? 4 : 1;
        String dir = FileUtil.getDirFromFullName(outFilePath);
        setCameraState(9);
        CamLog.m3d(CameraConstants.TAG, "file save start.");
        long duration;
        File file;
        if (this.mStickerManager != null && this.mStickerManager.isRecording()) {
            if (this.mCameraDevice != null) {
                this.mCameraDevice.setPreviewSurfaceToTarget(true);
            }
            ContentsInformation ci = this.mStickerManager.stopRecording();
            if (ci == null) {
                return;
            }
            if (this.mGet == null || this.mGet.getMediaSaveService() == null) {
                CamLog.m11w(CameraConstants.TAG, "MediaSaveService is null, so can not save the video");
                return;
            }
            duration = ci.getVideoDuration();
            if (duration < ((long) MultimediaProperties.getMinRecordingTime()) || !(this.mRecordingUIManager == null || this.mRecordingUIManager.checkMinRecTime())) {
                file = new File(ci.getVideoFileFullPath());
                if (file.exists()) {
                    file.delete();
                    CamLog.m3d(CameraConstants.TAG, "The recording time is too short, delete the file.");
                    return;
                }
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "file save content info = " + ci.toString());
            file = new File(ci.getVideoFileFullPath());
            if (file != null && file.exists()) {
                this.mGet.getMediaSaveService().addVideo(this.mGet.getAppContext(), this.mGet.getAppContext().getContentResolver(), ci.getVidedoFileDir(), ci.getVideoFileName(), ci.getVideoResolution(), duration, file.length(), this.mLocationServiceManager.getCurrentLocation(), 1, this.mOnMediaSavedListener, uuid);
            }
        } else if (outFilePath != null) {
            file = new File(outFilePath);
            if (file == null) {
                return;
            }
            if (deleteFile) {
                CamLog.m3d(CameraConstants.TAG, "The recording time is too short, delete the file.");
                file.delete();
            } else if (useDB) {
                if (getSpliceSurfaceRecordingVideoSize() != null) {
                    videoSize = getSpliceSurfaceRecordingVideoSize();
                }
                duration = getRecDurationTime(outFilePath);
                if (this.mGet == null || this.mGet.getMediaSaveService() == null) {
                    CamLog.m11w(CameraConstants.TAG, "MediaSaveService is null, so can not save the video");
                } else {
                    this.mGet.getMediaSaveService().addVideo(this.mGet.getAppContext(), this.mGet.getAppContext().getContentResolver(), dir, fileName, videoSize, duration, file.length(), this.mLocationServiceManager.getCurrentLocation(), purpose, this.mOnMediaSavedListener, uuid);
                }
            }
        }
    }

    protected String getSpliceSurfaceRecordingVideoSize() {
        return null;
    }

    public void videoRecorderRelease() {
        try {
            if (this.mCameraDevice != null) {
                VideoRecorder.release(null);
            }
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "VideoRecorder stop error! ", e);
        }
        VideoRecorder.releaseSecondMediaRecorder();
    }

    protected void pauseAndResumeRecorder(boolean pause) {
        if (this.mStickerManager != null && this.mStickerManager.isStickerDrawing()) {
            if (pause) {
                this.mStickerManager.pauseRecording();
            } else {
                this.mStickerManager.resumeRecording();
            }
            if (!pause) {
                setCameraState(6);
                resumeUpdateReordingTime();
            }
        } else if (pause) {
            VideoRecorder.pause();
        } else {
            VideoRecorder.resume();
        }
    }

    public boolean isPostviewShowing() {
        if (this.mPostviewManager == null || !this.mPostviewManager.isPostviewShowing()) {
            return false;
        }
        return true;
    }

    public int getPostviewType() {
        return this.mPostviewManager != null ? this.mPostviewManager.getCurPostviewType() : -1;
    }

    protected void onPostviewRelease() {
        if (this.mAdvancedFilmManager == null || !(this.mAdvancedFilmManager.getCurrentFilmIndex() != 0 || CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()))) {
            if (this.mStickerManager != null && this.mStickerManager.hasSticker() && this.mStickerManager.isRunning()) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, this.mStickerManager.getSupportedPreviewSize());
            }
            startPreview(null);
        } else if (this.mAdvancedFilmManager.getFilmSurfaceTexture() != null) {
            startPreview(null, this.mAdvancedFilmManager.getFilmSurfaceTexture());
            setCameraState(1);
        } else {
            this.mAdvancedFilmManager.runFilmEmulator(this.mGet.getCurSettingValue(Setting.KEY_FILM_EMULATOR));
            if (isFastShotSupported() && this.mCameraDevice != null) {
                this.mCameraDevice.setLongshot(false);
            }
        }
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(9, 500);
            this.mHandler.sendEmptyMessage(40);
        }
    }

    protected void onPostviewReleaseAfter(int type) {
    }

    protected boolean isPauseWaitDuringShot() {
        return true;
    }

    protected void onShutterCallback(boolean sound, boolean animation, boolean recording) {
        CamLog.m3d(CameraConstants.TAG, "### onShutterCallback");
        if (this.mCameraDevice == null || this.mSnapShotChecker.isNotTaking()) {
            CamLog.m3d(CameraConstants.TAG, "onShutterCallback return : mCameraDevice is " + this.mCameraDevice + ", SNAPSHOT_READY state.");
            return;
        }
        if (isPauseWaitDuringShot() && !recording && (this.mStickerManager == null || !this.mStickerManager.isStickerDrawing())) {
            this.mPictureCallbackLock.close();
            CamLog.m3d(CameraConstants.TAG, "mPictureCallbackLock close");
        }
        this.mSnapShotChecker.setSnapShotState(0);
        this.mSnapShotChecker.setPictureCallbackState(1);
        final boolean ani = animation;
        final boolean snd = sound;
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (DefaultCameraModule.this.isRecordingState()) {
                    DefaultCameraModule.this.doSnapshotEffect(ani, 0.6f, 300);
                } else {
                    DefaultCameraModule.this.doSnapshotEffect(ani, 0.3f, 100);
                }
                DefaultCameraModule.this.playShutterSound(snd);
            }
        });
        if (AppControlUtil.isNeedQuickShotTaking()) {
            CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity()).setParamToBackup(CameraConstants.QUICK_SHOT_KEY, Integer.valueOf(0));
            AppControlUtil.setNeedQuickShotTaking(false);
        }
        if (!recording) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (DefaultCameraModule.this.mFocusManager == null) {
                        return;
                    }
                    if ((!DefaultCameraModule.this.mFocusManager.isFocusLock() && !DefaultCameraModule.this.mFocusManager.isAELock() && !DefaultCameraModule.this.mSnapShotChecker.checkMultiShotState(12) && !DefaultCameraModule.this.mFocusManager.isAFPointVisible()) || DefaultCameraModule.this.isAttachIntent()) {
                        DefaultCameraModule.this.mFocusManager.registerEVCallback(false, false);
                        DefaultCameraModule.this.mFocusManager.hideAndCancelAllFocus(false, true, true);
                    }
                }
            });
        }
        if (FunctionProperties.isSupportedHDR(isRearCamera()) == 1 && 1 == (this.mNeedProgressDuringCapture & 1)) {
            CamLog.m3d(CameraConstants.TAG, "saving dialog : shutter callback");
            showSavingDialog(true, 200);
        }
        if (this.mSnapShotChecker.checkMultiShotState(2)) {
            this.mIntervalShotManager.startWatingUI(this.mSnapShotChecker.getIntervalShotCount() + 1);
        }
    }

    protected void playShutterSound(boolean sound) {
        if (sound && checkModuleValidate(128)) {
            if (this.mCaptureButtonManager != null) {
                View v = this.mCaptureButtonManager.getShutterButtonView(1);
                if (v != null) {
                    AudioUtil.performHapticFeedback(v, 65573);
                }
            }
            this.mGet.playSound(2, false, 0);
        }
    }

    protected void doSnapshotEffect(boolean animation, float fromAlpha, long duration) {
        if (animation && this.mAnimationManager != null && !this.mSnapShotChecker.checkMultiShotState(12)) {
            this.mAnimationManager.startSnapShotEffect(fromAlpha, duration);
        }
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        onPictureTakenCallback(data, extraExif, camera, true);
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera, boolean updateIntervalShotThumbnail) {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "### onPictureTakenCallback - start");
        if (this.mCameraDevice == null || this.mSnapShotChecker.isNotTaking()) {
            CamLog.m3d(CameraConstants.TAG, "onPictureTakenCallbackAfter return : mCameraDevice is " + this.mCameraDevice + ", SNAPSHOT_READY state.");
            return;
        }
        if (!(this.mFocusManager == null || isManualFocusModeEx())) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DefaultCameraModule.this.mFocusManager.registerEVCallback(true, true);
                }
            });
        }
        this.mSnapShotChecker.removeReleaser();
        this.mSnapShotChecker.setPictureCallbackState(2);
        if (this.mSnapShotChecker.checkMultiShotState(12)) {
            if (doLongShotPictureCallback(data, camera)) {
                this.mSnapShotChecker.removeMultiShotState(16);
            } else {
                this.mSnapShotChecker.setMultiShotState(16);
            }
            onPictureCallbackAfterRun(false, false);
        } else {
            if (this.mSnapShotChecker.checkMultiShotState(2) && updateIntervalShotThumbnail && !CameraConstants.MODE_FLASH_JUMPCUT.equals(getShotMode())) {
                doAfterIntervalshot(data);
            }
            makeMotionQuickViewFastImage(data, 0, this.mSnapShotChecker.checkMultiShotState(2));
            if (this.mNeedCropPicture) {
                CamLog.m3d(CameraConstants.TAG, "-dualview- save cropped image");
                saveImage(data, extraExif, this.mNeedCropPicture);
            } else {
                saveImage(data, extraExif);
            }
            if (!(ModelProperties.isMTKChipset() && Exif.getFlash(data) == 1)) {
                z = false;
            }
            this.mIsMTKFlashFired = z;
            this.mGet.removePostRunnable(this.mOnPictureTakenCallbackAfter);
            if (!isLivePhotoEnabled()) {
                if (this.mSnapShotChecker.isAvailableNightAndFlash() && isFastShotAvailable(3)) {
                    this.mGet.postOnUiThread(this.mOnPictureTakenCallbackAfter, 300);
                } else {
                    onPictureCallbackAfterRun(false, false);
                }
                if (this.mQuickClipManager != null) {
                    this.mQuickClipManager.setAfterShot();
                }
            }
        }
        CamLog.m3d(CameraConstants.TAG, "onPictureTakenCallback - end");
    }

    public boolean onPictureCallbackAfterRun(boolean checkFlash, boolean fromPicCbRunnable) {
        CamLog.m3d(CameraConstants.TAG, "mOnPictureTakenCallbackAfter - start");
        if (this.mCameraDevice == null || this.mSnapShotChecker.isNotTaking()) {
            return true;
        }
        if (!checkFlash || checkFlashOnPictureCallbackAfter(fromPicCbRunnable)) {
            this.mSnapShotChecker.removeReleaser();
            this.mSnapShotChecker.resetTakePicFlashState();
            this.mSnapShotChecker.setPictureCallbackState(3);
            if (checkModuleValidate(15)) {
                if (!(!checkModuleValidate(128) || this.mSnapShotChecker.checkMultiShotState(29) || this.mSnapShotChecker.getLGSFParamState() == 0)) {
                    revertParameterByLGSF(null);
                }
                checkRestartPreviewOnPictureCallback();
                LdbUtil.setMultiShotState(0);
                onTakePictureAfter();
                if (!this.mSnapShotChecker.checkMultiShotState(15)) {
                    if (!(this.mFocusManager == null || !isFocusEnableCondition() || isAttachIntent())) {
                        this.mFocusManager.registerCallback(true);
                    }
                    if (!"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
                        AudioUtil.setAudioFocus(getAppContext(), false);
                    }
                }
            }
            releaseCaptureProgressOnPictureCallback();
            if (!(this.mTimerManager == null || this.mSnapShotChecker.checkMultiShotState(4))) {
                this.mTimerManager.setIsGesureTimerShotProgress(false);
            }
            this.mSnapShotChecker.removeMultiShotState(16);
            CamLog.m3d(CameraConstants.TAG, "mOnPictureTakenCallbackAfter - end");
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "onPictureTakenCallbackAfter check flash false return.");
        return false;
    }

    public boolean checkFlashOnPictureCallbackAfter(boolean fromPicCbRunnable) {
        CamLog.m3d(CameraConstants.TAG, "checkFlashOnPictureCallbackAfter, from picture cb : " + fromPicCbRunnable);
        if (!isFastShotSupported()) {
            return true;
        }
        if (this.mCameraState == 5 || this.mCameraState == 8 || this.mCameraState == 6 || this.mCameraState == 7 || this.mCameraState == 9) {
            CamLog.m3d(CameraConstants.TAG, "checkFlashStateOnPictureCallbackAfter : state recording - mCameraState : " + this.mCameraState);
            return true;
        } else if ((!fromPicCbRunnable && this.mSnapShotChecker.isFlashCheckTimeOut()) || this.mSnapShotChecker.isAvailableStateWithFlash()) {
            return true;
        } else {
            if (this.mSnapShotChecker.getSnapShotState() < 2 && this.mSnapShotChecker.getTakePicFlashState() != 1) {
                return true;
            }
            this.mGet.removePostRunnable(this.mOnPictureTakenCallbackAfter);
            if (fromPicCbRunnable) {
                this.mGet.postOnUiThread(this.mOnPictureTakenCallbackAfter, 1000);
            } else {
                this.mSnapShotChecker.increaseFlashCheckTimeOut();
            }
            CamLog.m3d(CameraConstants.TAG, "Flash state : pre-flash firing");
            return false;
        }
    }

    protected boolean checkPictureCallbackRunnable(boolean runPictureCallback) {
        if (!this.mGet.hasRunnable(this.mOnPictureTakenCallbackAfter)) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "mOnPictureTakenCallbackAfter not processing yet.");
        this.mGet.removePostRunnable(this.mOnPictureTakenCallbackAfter);
        this.mSnapShotChecker.removeReleaser();
        if (!runPictureCallback) {
            return false;
        }
        onPictureCallbackAfterRun(false, false);
        return false;
    }

    protected void checkRestartPreviewOnPictureCallback() {
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator() && this.mAdvancedFilmManager.getCurrentFilmIndex() == 0 && !this.mAdvancedFilmManager.isShowingFilmMenu() && !CameraConstants.MODE_SMART_CAM.equals(getShotMode()) && !CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()) && !CameraConstants.MODE_FLASH_JUMPCUT.equals(getShotMode())) {
            CamLog.m3d(CameraConstants.TAG, "[gesture] checkRestartPreviewOnPictureCallback. isIntervalShotProgress ? " + isIntervalShotProgress());
            if (!isIntervalShotProgress() && !this.mSnapShotChecker.checkMultiShotState(4)) {
                this.mAdvancedFilmManager.stopFilmEmulator(true, false);
            }
        } else if (((FunctionProperties.isSupportedHDR(isRearCamera()) == 1 && "1".equals(getSettingValue("hdr-mode"))) || !FunctionProperties.isSupportedZSL(this.mCameraId) || (this.mIsMTKFlashFired && isRearCamera())) && !this.mGet.isLGUOEMCameraIntent()) {
            if (this.mFocusManager != null) {
                this.mFocusManager.stopFaceDetection();
            }
            setupPreview(null);
        }
    }

    public void resetFastShot() {
        CamLog.m3d(CameraConstants.TAG, "resetFastShot");
        checkPictureCallbackRunnable(false);
        this.mSnapShotChecker.resetSnapShotChecker();
        this.mSnapShotChecker.resetLGSFParamState();
        this.mSnapShotChecker.resetTakePicFlashState();
        this.mSnapShotChecker.setLongshotAvailable(-1, -1, -1);
    }

    public boolean doTakePictureAfterOnReleaser() {
        if (!checkModuleValidate(207) || this.mGet.hasRunnable(this.mOnPictureTakenCallbackAfter)) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "mOnPictureTakenCallbackAfter not processing yet. mNeedProgressDuringCapture : " + this.mNeedProgressDuringCapture);
        if (onPictureCallbackAfterRun(true, false)) {
            this.mSnapShotChecker.releaseSnapShotChecker();
            this.mSnapShotChecker.resetTakePicFlashState();
            if (this.mNeedProgressDuringCapture <= 0) {
                return true;
            }
            this.mNeedProgressDuringCapture = 0;
            showSavingDialog(false, 0);
            this.mWaitSavingDialogType = 0;
            if (!this.mIsNeedFinishAfterSaving) {
                return true;
            }
            this.mIsNeedFinishAfterSaving = false;
            getActivity().finish();
            return true;
        }
        this.mSnapShotChecker.sendReleaser(true);
        return true;
    }

    protected void onDropTakePicture(int error) {
        switch (error) {
            case 94:
            case 95:
                if (!"on".equals(getSettingValue("flash-mode"))) {
                    this.mSnapShotChecker.setTakePicFlashState(1);
                    checkPictureCallbackRunnable(false);
                    setCaptureButtonEnable(false, getShutterButtonType());
                    this.mQuickButtonManager.setEnable(100, false);
                    this.mSnapShotChecker.sendReleaser(true);
                    return;
                }
                return;
            case 96:
            case 97:
                this.mSnapShotChecker.setLGSFParamState(2);
                checkPictureCallbackRunnable(false);
                if (error == 97 && this.mSnapShotChecker.isAvailableStateWithFlash() && this.mSnapShotChecker.getTakePicFlashState() == 0) {
                    this.mSnapShotChecker.setSnapShotState(0);
                    this.mSnapShotChecker.sendReleaser(true);
                    return;
                }
                return;
            case 99:
                this.mSnapShotChecker.setSnapShotState(0);
                this.mSnapShotChecker.sendReleaser(false);
                return;
            default:
                return;
        }
    }

    protected void makeMotionQuickViewFastImage(final byte[] data, final int burstCount, boolean isIntervalShot) {
        boolean setFlip = false;
        if (this.mReviewThumbnailManager != null && this.mGestureShutterManager.isAvailableMotionQuickView()) {
            if (burstCount > 0 && this.mReviewThumbnailManager.getLastBurstFileName() != null) {
                this.mReviewThumbnailManager.setIsCompleteSaving(true);
            } else if (burstCount == 0) {
                this.mReviewThumbnailManager.setIsCompleteSaving(false);
            }
            this.mReviewThumbnailManager.setIsGetPictureCallback(true);
            if (!this.mFlipManager.isPreProcessPictureFlip()) {
                setFlip = this.mFlipManager.isNeedFlip(this.mCameraId);
            }
            this.mGet.postOnUiThread(this.mDeactivateQuickView, CameraConstants.TOAST_LENGTH_LONG);
            this.mMakeQuickViewImageThread = new Thread(new Runnable() {
                public void run() {
                    if (DefaultCameraModule.this.mGestureShutterManager != null && DefaultCameraModule.this.mReviewThumbnailManager != null && DefaultCameraModule.this.mGestureShutterManager.isAvailableMotionQuickView()) {
                        DefaultCameraModule.this.mReviewThumbnailManager.makeQuickViewFastBmp(data, true, setFlip, burstCount);
                    }
                }
            });
            if (this.mReviewThumbnailManager.isActivatedSelfieQuickView()) {
                this.mMakeQuickViewImageThread.start();
            }
        }
    }

    protected void releaseCaptureProgressOnPictureCallback() {
        this.mSnapShotChecker.setPictureCallbackState(0);
    }

    protected void showManagerForSelfTimer() {
        if (isSupportedQuickClip()) {
            if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL) || (this.mTimerManager != null && (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress()))) {
                this.mQuickClipManager.showSelfTimer(false, false);
            }
            this.mQuickClipManager.enableQuickClip(true);
        }
    }

    protected void hideManagerForSelfTimer(boolean keepState) {
        hideZoomBar();
        if (isSupportedQuickClip()) {
            if (!keepState || (this.mTimerManager != null && (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress() || this.mQuickClipManager.isOpened()))) {
                this.mQuickClipManager.showSelfTimer(true, keepState);
            }
            if (keepState) {
                this.mQuickClipManager.enableQuickClip(false);
            }
        }
    }

    protected void onPictureTakenCallbackInRecording(byte[] data, byte[] extraExif, CameraProxy camera) {
        CamLog.m3d(CameraConstants.TAG, "##picture taken LiveSnapShot!");
        this.mSnapShotChecker.setPictureCallbackState(2);
        if (data == null) {
            this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_write_file));
        } else {
            saveImage(data, extraExif);
        }
        releaseCaptureProgressOnPictureCallback();
    }

    protected void updateRecordingTime() {
        if (this.mRecordingUIManager != null) {
            long delta = SystemClock.uptimeMillis() - this.mRecordingUIManager.getVideoTime(1);
            long next_update_delay = 1000 - (delta % 1000);
            long seconds = (long) Math.round(((float) delta) / 1000.0f);
            if (getCameraState() == 6) {
                this.mRecordingUIManager.updateVideoTime(4, 1000 * seconds);
                updateUIRecordingTime(seconds);
                if (CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(getShotMode())) {
                    updateTimeLapseUIRecordingTime(seconds);
                }
                checkCacheSize();
                CamLog.m3d(CameraConstants.TAG, "updateRecordingTime : " + seconds);
            }
            if (CheckStatusManager.useBackLightControlInRecording() && seconds == CheckStatusManager.TEMPERATURE_LCD_CONTROL_SECOND) {
                if (TemperatureManager.IsHeatingVideoSize(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).split("@")[0])) {
                    TemperatureManager.backlightControlByVal(this.mGet.getActivity(), CheckStatusManager.TEMPERATURE_LCD_CONTROL_RATIO);
                    CamLog.m9v(CameraConstants.TAG, "backlight set to :" + CheckStatusManager.TEMPERATURE_LCD_CONTROL_RATIO);
                }
            }
            if (this.mIsSwitchingCameraDuringRecording || getCameraState() == 6) {
                this.mGet.postOnUiThread(this.mUpdateRecordingTime, next_update_delay);
            }
        }
    }

    protected void checkCacheSize() {
        if (VideoRecorder.getStorageType() == 2 && getCameraState() == 6 && this.mRecordingUIManager != null && this.mStorageManager != null && ((this.mRecordingUIManager.getVideoTime(4) / 1000) + 1) % 5 == 0 && !this.mStorageManager.checkCNasCacheStorage(0, true)) {
            onVideoStopClicked(true, false);
        }
    }

    public void resumeUpdateReordingTime() {
        if (this.mRecordingUIManager != null) {
            long now = SystemClock.uptimeMillis();
            long startTime = this.mRecordingUIManager.getVideoTime(1);
            this.mRecordingUIManager.updateVideoTime(1, (startTime + now) - this.mRecordingUIManager.getVideoTime(2));
            this.mRecordingUIManager.updateVideoTime(2, 0);
            updateRecordingTime();
            this.mAllowPause = true;
        }
    }

    public int getRecCompensationTime() {
        return 0;
    }

    public long getRecDurationTime() {
        if (this.mRecordingUIManager == null) {
            return 0;
        }
        long time = this.mRecordingUIManager.getVideoTime(4);
        CamLog.m9v(CameraConstants.TAG, "getRecDurationTime : " + time);
        return time;
    }

    public long getRecDurationTime(String recordingFilePath) {
        File mRecordingFile = new File(recordingFilePath);
        if (mRecordingFile == null || !mRecordingFile.exists()) {
            return 0;
        }
        long mRecDuration = (long) FileUtil.getDurationFromFilePath(this.mGet.getAppContext(), recordingFilePath);
        CamLog.m3d(CameraConstants.TAG, "filePath = " + recordingFilePath + ", duration = " + mRecDuration);
        return mRecDuration;
    }

    protected void updateUIRecordingTime(long seconds) {
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.updateUIRecordingTime(this.mIsFileSizeLimitReached, seconds, isVideoAttachMode(), getCameraState());
        }
    }

    protected void updateTimeLapseUIRecordingTime(long seconds) {
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.updateTimeLapseUIRecordingTime(seconds, getCameraState());
        }
    }

    protected void startAudioZoom(CameraParameters parameters) {
        if (this.mAudioZoomManager != null && parameters != null && this.mParamUpdater != null) {
            if (isAudioZoomAvailable()) {
                int orientation = getOrientationDegree();
                int curZoomValue = 0;
                int maxZoomValue = parameters.getMaxZoom();
                boolean[] checkAudioZoom = isAvailableAudiozoom();
                if (checkAudioZoom != null && checkAudioZoom.length >= 2) {
                    if (checkAudioZoom[0]) {
                        String sCurZoomValue = this.mParamUpdater.getParamValue("zoom");
                        if (!"not found".equals(sCurZoomValue)) {
                            curZoomValue = Integer.valueOf(sCurZoomValue).intValue();
                        }
                    }
                    if (checkAudioZoom[1]) {
                        orientation = 6;
                    }
                }
                CamLog.m3d(CameraConstants.TAG, "curZoomValue / maxZoomValue = " + curZoomValue + " / " + maxZoomValue);
                this.mAudioZoomManager.start(orientation, curZoomValue, maxZoomValue);
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "AudioZoom is not available");
        }
    }

    protected void stopAudioZoom() {
        if (this.mAudioZoomManager != null && isAudioZoomAvailable()) {
            this.mAudioZoomManager.stop();
        }
    }

    protected boolean[] isAvailableAudiozoom() {
        boolean z;
        boolean isMicOn;
        if (AudioUtil.getHeadsetState() == 2) {
            isMicOn = true;
        } else {
            isMicOn = false;
        }
        boolean[] result = new boolean[2];
        if (this.mIsMMSRecordingSize || !isRearCamera() || isMicOn) {
            z = false;
        } else {
            z = true;
        }
        result[0] = z;
        result[1] = this.mIsMMSRecordingSize;
        if (FunctionProperties.isSupportedInAndOutZoom() && this.mCameraId == 2) {
            result[0] = false;
        }
        return result;
    }

    protected void setMicPath() {
        if (isAudioRecordingAvailable()) {
            int degree = -1;
            switch (getOrientationDegree()) {
                case 0:
                    degree = 90;
                    break;
                case 90:
                    degree = 180;
                    break;
                case 180:
                    degree = 270;
                    break;
                case 270:
                    degree = 0;
                    break;
            }
            String orientation = ParamConstants.MIC_PATH_ORIENTATION + degree;
            AudioManager audioManager = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
            if (audioManager != null) {
                audioManager.setParameters(orientation);
                CamLog.m3d(CameraConstants.TAG, "[mic path] ===>" + orientation);
            }
        }
    }

    protected void resetMicPath() {
        if (isAudioRecordingAvailable()) {
            String orientation = "MIC_PATH_ORIENTATION=-1";
            AudioManager audioManager = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
            if (audioManager != null) {
                audioManager.setParameters(orientation);
                CamLog.m3d(CameraConstants.TAG, "[mic path] ===>" + orientation);
            }
        }
    }

    protected boolean isAudioRecordingAvailable() {
        return true;
    }

    public boolean isAvailableTakePicture() {
        if (this.mStorageManager == null || this.mGet.getMediaSaveService() == null) {
            return false;
        }
        return this.mStorageManager.isAvailableTakePicture(this.mGet.getMediaSaveService().getQueueCount());
    }

    protected void onCAFButtonClicked() {
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 4, false);
        }
        if (this.mFocusManager != null) {
            this.mFocusManager.cancelTouchAutoFocus();
            this.mFocusManager.setIsOnlyTAF(false);
            setCameraFocusMode(ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO);
            this.mFocusManager.resetEVValue(0);
        }
    }

    public void doTouchAFInRecording() {
        if (this.mQuickButtonManager != null && this.mFocusManager != null) {
            this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 0, false);
            this.mQuickButtonManager.show(false, false, false);
            if (this.mGet.findViewById(C0088R.id.quick_button_caf) != null) {
                this.mGet.findViewById(C0088R.id.quick_button_caf).setNextFocusDownId(C0088R.id.shutter_bottom_comp);
            }
            this.mFocusManager.setIsOnlyTAF(true);
        }
    }

    public void takePictureByTimer(int type) {
        if (type == 0 || type == 2) {
            this.mSnapShotChecker.removeMultiShotState(12);
            if (type == 2 && this.mGestureShutterManager.getGestureCaptureType() == 2) {
                this.mIntervalShotManager.hideIntervalshotEnteringGuide();
                if (!this.mSnapShotChecker.checkMultiShotState(2)) {
                    this.mSnapShotChecker.setMultiShotState(2);
                    this.mSnapShotChecker.initIntervalShotCount();
                    doIntervalShot();
                    return;
                }
                return;
            }
            if (!(isRearCamera() || this.mGestureShutterManager == null || !this.mGestureShutterManager.isAvailableMotionQuickView() || this.mReviewThumbnailManager == null)) {
                this.mGestureShutterManager.startMotionEngine();
                this.mReviewThumbnailManager.readyforQuickview();
            }
            takePicture();
            restoreCleanViewForSelfTimer(500);
            return;
        }
        this.mTimerManager.setIsGesureTimerShotProgress(false);
        if (onVideoShutterClickedBefore()) {
            onVideoShutterClicked();
        }
        if (getRecordingPreviewState(16)) {
            setRecordingPreviewState(0, true);
            startRecordingAfterRecordingPreview();
        }
    }

    protected void restoreCleanViewForSelfTimer(long delay) {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                DefaultCameraModule.this.doCleanView(false, DefaultCameraModule.this.isMultiviewFrameShot(), false);
                DefaultCameraModule.this.doCleanViewAfter(true);
                if (DefaultCameraModule.this.isAvailableTilePreview()) {
                    DefaultCameraModule.this.setTilePreviewLayout("on".equals(DefaultCameraModule.this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)));
                }
            }
        }, delay);
    }

    public boolean isMultiviewFrameShot() {
        return false;
    }

    public boolean isVideoAttachMode() {
        boolean isMmsVideoSetting;
        if (MmsProperties.getMmsResolutionsLength(this.mGet.getAppContext().getContentResolver()) == 0) {
            isMmsVideoSetting = false;
        } else {
            isMmsVideoSetting = MmsProperties.isAvailableMmsResolution(this.mGet.getAppContext().getContentResolver(), getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).split("@")[0]);
        }
        if (this.mGet.isAttachIntent() || isMmsVideoSetting) {
            return true;
        }
        return false;
    }

    protected boolean isNeedProgressBar() {
        return isMMSRecording();
    }

    protected void setVideoLimitSize() {
        if (isMMSRecording()) {
            this.mLimitRecordingSize = MmsProperties.getMmsVideoSizeLimit(this.mGet.getAppContext().getContentResolver());
            this.mLimitRecordingDuration = MultimediaProperties.getMMSMaxDuration();
            return;
        }
        this.mLimitRecordingSize = 0;
        this.mLimitRecordingDuration = 0;
    }

    protected String getVideoOutputFilePath() {
        return VideoRecorder.getFilePath();
    }

    public void setLoopFilePath(String oldPath) {
        int storage = getCurStorage();
        String dir = (!isCnasRecordingLimitation(storage) || this.mStorageManager == null) ? getCurDir() : this.mStorageManager.getDir(storage);
        String newFileName = makeFileName(1, storage, dir, false, getSettingValue(Setting.KEY_MODE));
        String newFilePath = dir + newFileName + ".mp4";
        if (oldPath != null) {
            VideoRecorder.setFilePath(newFilePath);
            VideoRecorder.setFileName(newFileName);
        }
    }

    private boolean isAvailableGaplessLooping() {
        if (!FunctionProperties.isGaplessLoopRecordingSupported()) {
            CamLog.m7i(CameraConstants.TAG, "not support gapless looping");
            return false;
        } else if (isAttachIntent() || isMMSIntent() || isMMSRecording()) {
            CamLog.m7i(CameraConstants.TAG, "attach mode is not support looping");
            return false;
        } else if (getLoopRecordingType() != 0) {
            return true;
        } else {
            CamLog.m7i(CameraConstants.TAG, "This mode is not support looping");
            return false;
        }
    }

    protected void recorderOnInfo(MediaRecorder mr, int what, int extra) {
        CamLog.m3d(CameraConstants.TAG, "MediaRecorder onInfo what = " + what + " / extra = " + extra);
        int storageType = VideoRecorder.getStorageType();
        int storage = getCurStorage();
        long freeSpace = ((this.mStorageManager.getFreeSpace(storageType) - CameraConstants.VIDEO_LOW_STORAGE_THRESHOLD) - CameraConstants.VIDEO_SAFE_MAX_FILE_SIZE_DAMPER) - CameraConstants.VIDEO_RECORD_LOOPING_THRESHOLD;
        if (what == 1003) {
            this.mIsFileSizeLimitReached = false;
            resumeUpdateReordingTime();
        } else if (what == 802) {
            if (isAvailableGaplessLooping()) {
                long maxFileSize = VideoRecorder.getMaxFileSize();
                CamLog.m7i(CameraConstants.TAG, "maxFileSize : " + maxFileSize + "  freeSpace : " + freeSpace);
                if (maxFileSize < CameraConstants.MEDIA_RECORDING_MAX_LIMIT) {
                    CamLog.m7i(CameraConstants.TAG, "no storage space. loop recording will not work");
                    return;
                }
                String dir = (!isCnasRecordingLimitation(storage) || this.mStorageManager == null) ? getCurDir() : this.mStorageManager.getDir(storage);
                this.mPrevFileName = VideoRecorder.getFileName();
                this.mLoopTempFilePath = dir + CameraConstants.LOOP_TEMP_FILE_0;
                VideoRecorder.setNextOutputFile(new File(this.mLoopTempFilePath));
                VideoRecorder.setLoopState(2);
            }
        } else if (what == 803) {
            showToast(this.mGet.getAppContext().getString(C0088R.string.camera_gapless_toast), CameraConstants.TOAST_LENGTH_LONG);
            saveFile(true, getSettingValue(Setting.KEY_VIDEO_RECORDSIZE).split("@")[0], this.mPrevFileName, VideoRecorder.getFilePath(), VideoRecorder.getUUID());
            renameTempLoopFile(this.mLoopTempFilePath, VideoRecorder.getLoopState());
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateRecTimeLayout();
            }
            setCameraState(6);
            sendLDBIntentOnLoopRecording();
            if (freeSpace <= CameraConstants.MEDIA_RECORDING_MAX_LIMIT) {
                VideoRecorder.changeMaxFileSize(Math.abs(freeSpace - CameraConstants.MEDIA_RECORDING_MAX_LIMIT));
                VideoRecorder.setMaxFileSize(0, freeSpace, storage);
            }
        } else if (!this.mIsFileSizeLimitReached) {
            if (what == 800 || what == 801) {
                this.mIsFileSizeLimitReached = true;
                updateRecordingTime();
                CamLog.m3d(CameraConstants.TAG, "recorderOnInfo  mIsFileSizeLimitReached true ");
                if (!(what != 801 || this.mGet.isAttachIntent() || this.mGet.isMMSIntent())) {
                    if (this.mStorageManager.isStorageFull(storageType)) {
                        showToast(this.mGet.getAppContext().getString(C0088R.string.popup_storage_full_save1), 2000);
                    } else if (VideoRecorder.getMaxFileSize() != CameraConstants.MEDIA_RECORDING_MAX_LIMIT || getLoopRecordingType() == 0 || this.mCameraState == 8 || storageType == 2) {
                        showMaxFileSizeReachedToast();
                    } else {
                        CamLog.m3d(CameraConstants.TAG, "MediaRecorder max FileSize reached");
                        showToast(this.mGet.getAppContext().getString(C0088R.string.camera_gapless_toast), CameraConstants.TOAST_LENGTH_LONG);
                        VideoRecorder.setUUID();
                        if (this.mAdvancedFilmManager.isRunningFilmEmulator() || getLoopRecordingType() == 2) {
                            doLoopSurfaceRecording();
                            checkRecordingMaxFileSize();
                            return;
                        } else if (getLoopRecordingType() == 1) {
                            doLoopGapRecording();
                            return;
                        }
                    }
                }
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DefaultCameraModule.this.onVideoStopClicked(true, false);
                    }
                }, 0);
            }
        }
    }

    protected void showMaxFileSizeReachedToast() {
        String fileMaxSize;
        CamLog.m3d(CameraConstants.TAG, "MediaRecorder max fileSize reached");
        double mFileSize = (double) new File(getVideoOutputFilePath()).length();
        double mStringSize = mFileSize / 1.073741824E9d;
        CamLog.m3d(CameraConstants.TAG, "File Size: " + mFileSize);
        if (mStringSize >= 1.0d) {
            fileMaxSize = new BigDecimal(mStringSize).setScale(2, 3) + " GB";
        } else {
            fileMaxSize = new BigDecimal(mStringSize * 1024.0d).setScale(2, 3) + " MB";
        }
        showToast(String.format(this.mGet.getAppContext().getString(C0088R.string.sp_popup_storage_limit_with_exact_size_NORMAL1), new Object[]{fileMaxSize}), 2000);
    }

    protected void recorderOnError(MediaRecorder mr, int what, int extra) {
        CamLog.m3d(CameraConstants.TAG, "MediaRecorder onError what = " + what + " / extra = " + extra);
        if (what == 300) {
            onVideoStopClicked(true, false);
        } else if (what == 1 || what == 2 || what == 100) {
            this.mIsFileSizeLimitReached = false;
            videoRecorderRelease();
            AudioUtil.setAllSoundCaseMute(getAppContext(), false);
            setCameraState(1);
            if (extra == -1007 && getRecDurationTime(VideoRecorder.getFilePath()) < ((long) MultimediaProperties.getMinRecordingTime())) {
                CamLog.m5e(CameraConstants.TAG, "Short recording time error!! time : " + getRecDurationTime(VideoRecorder.getFilePath()) + "ms");
                setCameraState(-1);
            } else if (!this.mGet.getActivity().isFinishing()) {
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DefaultCameraModule.this.mHandler.sendEmptyMessage(12);
                    }
                });
            }
        }
    }

    protected void doLoopSurfaceRecording() {
        CamLog.m3d(CameraConstants.TAG, "doLoopSurfaceRecording");
        if (this.mCameraDevice == null || this.mCameraDevice.getCamera() == null || this.mCameraDevice.getParameters() == null) {
            CamLog.m3d(CameraConstants.TAG, "doLoopSurfaceRecording fail");
            return;
        }
        this.mAdvancedFilmManager.stopRecorder(false);
        try {
            VideoRecorder.stop();
        } catch (Exception e) {
            CamLog.m3d(CameraConstants.TAG, "loop recording stop Exception : " + e);
        }
        VideoRecorder.changeMediaRecorder();
        renameTempLoopFile();
        setAudioLoopbackInRecording(false);
        resetMicPath();
        String videoSize = getSettingValue(getVideoSizeSettingKey()).split("@")[0];
        String uuid = VideoRecorder.getUUID();
        if (VideoRecorder.getLoopState() != 3 || this.mLoopRecorderInfo == null) {
            saveFile(true, videoSize, VideoRecorder.getFileName(), VideoRecorder.getFilePath(), uuid);
        } else {
            saveFile(true, videoSize, this.mLoopRecorderInfo.mFileName, this.mLoopRecorderInfo.mOutFilePath, uuid);
        }
        setMicPath();
        this.mAdvancedFilmManager.prepareRecording(videoSize, false);
        VideoRecorder.reInitMediaRecorder();
        setSSRSetting(true);
        if (!VideoRecorder.start()) {
            CamLog.m3d(CameraConstants.TAG, "startForLooping fail");
            restoreRecorderToIdle();
            this.mRecordingUIManager.hide();
            this.mToastManager.showShortToast(getActivity().getString(isMiracastState() ? C0088R.string.error_video_recording_during_miracast : C0088R.string.error_occurred));
        }
        this.mAdvancedFilmManager.startRecorder();
        setCameraState(6);
        if (this.mIsFileSizeLimitReached) {
            this.mIsFileSizeLimitReached = false;
        }
        this.mLoopRecorderInfo = makeLoopRecorderInfo(false);
        this.mStartRecorderInfo = makeRecorderInfo();
        this.mFreeSpaceForSafeRecording = ((this.mStorageManager.getFreeSpace(this.mLoopRecorderInfo.mStorageType) - CameraConstants.VIDEO_LOW_STORAGE_THRESHOLD) - CameraConstants.VIDEO_SAFE_MAX_FILE_SIZE_DAMPER) - CameraConstants.VIDEO_RECORD_LOOPING_THRESHOLD;
        initNewVideoRecorderForSurfaceLooping();
        LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_LOOP_RECORDING, -1, LdbConstants.LDB_LOOP_RECORDING_FILM);
        setAudioLoopbackInRecording(true);
    }

    protected void initNewVideoRecorderForSurfaceLooping() {
        VideoRecorder.setMaxFileSize(this.mLimitRecordingSize, this.mStorageManager.getFreeSpace(this.mTempLoopRecorderInfo.mStorageType), this.mTempLoopRecorderInfo.mStorageType);
        VideoRecorder.setFileName(this.mTempLoopRecorderInfo.mFileName);
        VideoRecorder.setFilePath(this.mTempLoopRecorderInfo.mOutFilePath);
        VideoRecorder.setLoopState(3);
        MediaRecorderListener listener = new MediaRecorderListener();
        VideoRecorder.setInfoListener(listener);
        VideoRecorder.setErrorListener(listener);
        setLoopRecordFilePath();
        VideoRecorder.releaseSecondMediaRecorder();
        Camera camera = null;
        if (FunctionProperties.getSupportedHal() != 2) {
            camera = (Camera) this.mCameraDevice.getCamera();
        }
        VideoRecorder.init(false, camera, this.mCameraId, this.mStartRecorderInfo.mVideoSize, this.mLocationServiceManager.getCurrentLocation(), this.mStartRecorderInfo.mPurpose, this.mStartRecorderInfo.mVideoFps, this.mStartRecorderInfo.mVideoBitrate, getRecordingType(), true, CameraDeviceUtils.getVideoFlipType(getVideoOrientation(), this.mCameraId, "off".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION)), false));
    }

    protected void setLoopRecordFilePath() {
        CamLog.m3d(CameraConstants.TAG, "setLoopRecordFilePath");
        this.mTempLoopRecorderInfo = makeLoopRecorderInfo(true);
        VideoRecorder.setLoopRecordFilePath(this.mTempLoopRecorderInfo.mOutFilePath);
    }

    private StartRecorderInfo makeLoopRecorderInfo(boolean isTemp) {
        String outFilePath;
        int storage = getCurStorage();
        boolean isCNasLimitation = isCnasRecordingLimitation(storage);
        if (isCNasLimitation) {
            storage = 0;
        }
        String dir = (!isCNasLimitation || this.mStorageManager == null) ? getCurDir() : this.mStorageManager.getDir(storage);
        String extend = ".mp4";
        String fileName = makeFileName(1, storage, dir, false, getSettingValue(Setting.KEY_MODE));
        if (isTemp) {
            outFilePath = dir + (this.mLoopToggle ? CameraConstants.LOOP_TEMP_FILE_0 : CameraConstants.LOOP_TEMP_FILE_1);
            this.mLoopToggle = !this.mLoopToggle;
        } else {
            outFilePath = dir + fileName + extend;
        }
        CamLog.m3d(CameraConstants.TAG, "output loop file is : " + outFilePath);
        return new StartRecorderInfo(storage, fileName, outFilePath, null, 0, 0.0d, 0, CameraDeviceUtils.getVideoFlipType(getVideoOrientation(), this.mCameraId, "off".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION)), false));
    }

    protected void doLoopGapRecording() {
        CamLog.m3d(CameraConstants.TAG, "doLoopGapRecording");
        if (this.mCameraDevice == null || this.mCameraDevice.getCamera() == null || this.mCameraDevice.getParameters() == null) {
            CamLog.m3d(CameraConstants.TAG, "doLoopGapRecording fail");
            return;
        }
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.updateVideoTime(2, SystemClock.uptimeMillis());
        }
        try {
            VideoRecorder.stop();
        } catch (Exception e) {
            CamLog.m3d(CameraConstants.TAG, "loop recording stop Exception : " + e);
        }
        setMeidaRecorderForGapLooping();
        if (VideoRecorder.start()) {
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateRecTimeLayout();
            }
            setCameraState(6);
            if (this.mIsFileSizeLimitReached) {
                this.mIsFileSizeLimitReached = false;
            }
            setAudioLoopbackInRecording(true);
            return;
        }
        int i;
        CamLog.m3d(CameraConstants.TAG, "startLoopGap fail");
        restoreRecorderToIdle();
        this.mRecordingUIManager.hide();
        ToastManager toastManager = this.mToastManager;
        Activity activity = getActivity();
        if (isMiracastState()) {
            i = C0088R.string.error_video_recording_during_miracast;
        } else {
            i = C0088R.string.error_occurred;
        }
        toastManager.showShortToast(activity.getString(i));
    }

    private void setMeidaRecorderForGapLooping() {
        setAudioLoopbackInRecording(false);
        stopAudioZoom();
        saveFile(true, getSettingValue(Setting.KEY_VIDEO_RECORDSIZE).split("@")[0], VideoRecorder.getFileName(), VideoRecorder.getFilePath(), VideoRecorder.getUUID());
        startAudioZoom(this.mCameraDevice.getParameters());
        VideoRecorder.setLoopState(1);
        this.mStartRecorderInfo = makeRecorderInfo();
        VideoRecorder.setMaxDuration(this.mLimitRecordingDuration);
        VideoRecorder.setMaxFileSize(this.mLimitRecordingSize, this.mStorageManager.getFreeSpace(this.mStartRecorderInfo.mStorageType), this.mStartRecorderInfo.mStorageType);
        VideoRecorder.setFileName(this.mStartRecorderInfo.mFileName);
        VideoRecorder.setFilePath(this.mStartRecorderInfo.mOutFilePath);
        Camera camera = null;
        if (FunctionProperties.getSupportedHal() != 2) {
            camera = (Camera) this.mCameraDevice.getCamera();
        }
        VideoRecorder.init(camera, this.mCameraId, this.mStartRecorderInfo.mVideoSize, this.mLocationServiceManager.getCurrentLocation(), this.mStartRecorderInfo.mPurpose, this.mStartRecorderInfo.mVideoFps, this.mStartRecorderInfo.mVideoBitrate, getRecordingType(), false, this.mStartRecorderInfo.mVideoFlipType);
        setSSRSetting(true);
    }

    public void renameTempLoopFile() {
        renameTempLoopFile(VideoRecorder.getFilePath(), VideoRecorder.getLoopState());
    }

    public void renameTempLoopFile(String oldPath, int loopState) {
        if (loopState != 0 && loopState != 1 && oldPath != null) {
            String newPath = "";
            if (loopState == 2) {
                setLoopFilePath(oldPath);
                newPath = VideoRecorder.getFilePath();
            } else if (loopState == 3) {
                newPath = this.mLoopRecorderInfo.mOutFilePath;
            }
            File file = new File(oldPath);
            if (file != null && file.exists()) {
                CamLog.m3d(CameraConstants.TAG, "rename from : " + oldPath + " / to : " + newPath + " / result : " + file.renameTo(new File(newPath)) + " / loopState : " + loopState);
            }
        }
    }

    protected boolean checkShutterUpState() {
        if (this.mSnapShotChecker.isBurstCountMax(getCurStorage() == 0, true, isRearCamera())) {
            this.mSnapShotChecker.initBurstCount();
            return true;
        } else if (this.mSnapShotChecker.checkMultiShotState(2)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (!checkShutterUpState()) {
            boolean bZoomByVolumeKey = (keyCode == 24 || keyCode == 25) && ModelProperties.isKeyPadSupported(getAppContext());
            if (!this.mSnapShotChecker.checkMultiShotState(1) && event.getRepeatCount() == 0) {
                switch (this.mCameraState) {
                    case 6:
                    case 7:
                        if (!bZoomByVolumeKey) {
                            LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_VOLUME);
                            onSnapShotButtonClicked();
                            break;
                        }
                        break;
                    default:
                        if (!(bZoomByVolumeKey || this.mFocusManager == null || !this.mFocusManager.checkFocusStateForChangingSetting())) {
                            LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_VOLUME);
                            onCameraShutterButtonClicked();
                            break;
                        }
                }
            }
            if (bZoomByVolumeKey) {
                this.mZoomManager.stopZoomRepeat();
            }
            stopBurstShotTakingByVolumeKey(keyCode);
        }
        return true;
    }

    private void stopBurstShotTakingByVolumeKey(int keyCode) {
        if (keyCode != 24 && keyCode != 25) {
            return;
        }
        if (this.mSnapShotChecker.checkMultiShotState(4)) {
            this.mSnapShotChecker.setMultiShotState(8);
            stopBurstShotTaking(false);
            return;
        }
        stopBurstShotTaking(this.mSnapShotChecker.checkMultiShotState(1));
    }

    public boolean onShutterDown(int keyCode, KeyEvent event) {
        boolean bZoomByVolumeKey;
        CamLog.m3d(CameraConstants.TAG, "onShutterDown keyCode" + keyCode);
        if ((keyCode == 24 || keyCode == 25) && ModelProperties.isKeyPadSupported(getAppContext())) {
            bZoomByVolumeKey = true;
        } else {
            bZoomByVolumeKey = false;
        }
        if (checkModuleValidate(95)) {
            this.mSnapShotChecker.removeMultiShotState(8);
            if (bZoomByVolumeKey && this.mZoomManager != null) {
                if (isZoomAvailable()) {
                    this.mZoomManager.onKeyZoomInOut((keyCode == 24 ? 1 : -1) * 6, false);
                } else {
                    showToastConstant(this.mGet.getAppContext().getString(C0088R.string.volume_key_zoom_disable));
                }
            }
        }
        return true;
    }

    public boolean onVolumeKeyLongPressed(int keyCode, KeyEvent event) {
        if (!this.mSnapShotChecker.checkMultiShotState(3) && checkModuleValidate(16) && ("mode_normal".equals(getSettingValue(Setting.KEY_MODE)) || CameraConstants.MODE_POPOUT_CAMERA.equals(getSettingValue(Setting.KEY_MODE)) || CameraConstants.MODE_DUAL_POP_CAMERA.equals(getSettingValue(Setting.KEY_MODE)) || CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getSettingValue(Setting.KEY_MODE)))) {
            LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_VOLUME);
            onShutterBottomButtonLongClickListener();
        }
        return true;
    }

    public boolean doMenuKey() {
        if (checkModuleValidate(145) && ModelProperties.isKeyPadSupported(getAppContext()) && !this.mTimerManager.isTimerShotCountdown()) {
            if (!isRotateDialogVisible()) {
                if (isRearCamera()) {
                    showDialog(9);
                } else {
                    showDialog(10);
                }
            }
            return true;
        }
        if (this.mHandler != null && isMenuShowing(4)) {
            this.mHandler.sendEmptyMessage(84);
        }
        return true;
    }

    public boolean doDpadKey(int keyCode, KeyEvent event) {
        CamLog.m3d(CameraConstants.TAG, "doDpadKey keyCode : " + keyCode + " event: " + event);
        if ((!ModelProperties.isKeyPadSupported(getAppContext()) && !ViewUtil.isAccessibilityServiceEnabled(getAppContext())) || this.mReviewThumbnailManager.isQuickViewAniStarted()) {
            return true;
        }
        if (!checkModuleValidate(192)) {
            return false;
        }
        if (keyCode == 218) {
            launchGallery(getUri(), 0);
            return true;
        } else if (keyCode != 19 || event.getAction() != 0 || this.mDialogManager.isRotateDialogVisible()) {
            return false;
        } else {
            requestFocusOnMenuButton(true);
            return false;
        }
    }

    public boolean doDpadCenterKey(KeyEvent event) {
        if (this.mSnapShotChecker.checkMultiShotState(2)) {
            return true;
        }
        if (!ModelProperties.isKeyPadSupported(getAppContext()) && !ViewUtil.isAccessibilityServiceEnabled(getAppContext())) {
            return true;
        }
        if (checkModuleValidate(192)) {
            CamLog.m3d(CameraConstants.TAG, "doDpadCenterKey action = " + event.getAction());
            View shutter = this.mGet.findViewById(C0088R.id.shutter_bottom_comp);
            if (event.getAction() != 0) {
                if (event.getAction() == 1) {
                    if (this.mSnapShotChecker.checkMultiShotState(4)) {
                        this.mSnapShotChecker.setMultiShotState(8);
                        stopBurstShotTaking(false);
                        return true;
                    } else if (this.mReviewThumbnailManager.isQuickViewAniStarted()) {
                        this.mReviewThumbnailManager.hideQuickView(true);
                        return true;
                    } else if (shutter != null) {
                        CamLog.m3d(CameraConstants.TAG, "shutter.isEnabled() = " + shutter.isEnabled() + " / shutter.isFocused() = " + shutter.isFocused() + " / mIsShotToShotJustEnd = " + this.mSnapShotChecker.checkMultiShotState(8));
                        if (shutter.isEnabled() && shutter.isFocused() && !this.mSnapShotChecker.checkMultiShotState(8)) {
                            onCameraShutterButtonClicked();
                            return true;
                        }
                    }
                }
                return false;
            } else if (shutter != null && !shutter.isFocused()) {
                CamLog.m3d(CameraConstants.TAG, "Shutter button not focused");
                return false;
            } else if (!this.mKeyManager.isCenterKeyPressed() || this.mSnapShotChecker.checkMultiShotState(4)) {
                return true;
            } else {
                LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_DPAD_CENTER);
                onShutterBottomButtonLongClickListener();
                return true;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "doDpadCenterKey return due to camera state");
        return true;
    }

    public void doHotKeyUp() {
        if (!this.mSnapShotChecker.checkMultiShotState(7)) {
            CamLog.m3d(CameraConstants.TAG, "doHotKeyUp");
            if (isRecordingState()) {
                onShutterStopButtonClicked();
                return;
            }
            onRecordStartButtonClicked();
            if (!SharedPreferenceUtil.getHotKeyMessageShown(getAppContext()) && this.mToastManager != null) {
                SharedPreferenceUtil.saveHotKeyMessageShown(getAppContext(), true);
                this.mToastManager.showLongToast(getAppContext().getResources().getString(C0088R.string.hotkey_toast_msg1));
            }
        }
    }

    public boolean doVolumeUpKey() {
        return false;
    }

    public boolean doBackKey() {
        CamLog.m7i(CameraConstants.TAG, "doBackKey");
        if (this.mTimerManager == null || this.mCaptureButtonManager == null || this.mQuickButtonManager == null || this.mReviewThumbnailManager == null || this.mBarManager == null || this.mIntervalShotManager == null) {
            this.mGet.stopQuickShotModeByBackkey();
            CamLog.m7i(CameraConstants.TAG, "return");
            return false;
        }
        this.mGet.showTilePreviewCoverView(false);
        if (this.mGet.isActivatedQuickdetailView()) {
            if (!this.mGet.isResumeAfterProcessingDone()) {
                return true;
            }
            closeDetailViewAfterStartPreview();
            return true;
        } else if (this.mReviewThumbnailManager.isActivatedSelfieQuickView()) {
            hideSelfieQuickView();
            return true;
        } else if (this.mSnapShotChecker.checkMultiShotState(2)) {
            if (!checkModuleValidate(16)) {
                this.mSnapShotChecker.setIntervalShotState(8);
                return true;
            } else if (CameraConstants.MODE_FLASH_JUMPCUT.equals(getShotMode())) {
                return true;
            } else {
                stopIntervalShot(0);
                return true;
            }
        } else if (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress()) {
            if (this.mGestureShutterManager != null && this.mTimerManager.getIsGesureTimerShotProgress() && this.mGestureShutterManager.getGestureCaptureType() == 2) {
                this.mIntervalShotManager.hideIntervalshotEnteringGuide();
            }
            this.mTimerManager.stopTimerShot();
            onTakePictureAfter();
            this.mSnapShotChecker.releaseSnapShotChecker();
            setAudioLoopbackOnPreview(true);
            if (this.mIntervalShotManager.getIntervalshotVisibiity() != 4) {
                this.mIntervalShotManager.hideIntervalshotLayout();
            }
            doCleanView(false, true, false);
            doCleanViewAfter(true);
            setFilmStrengthButtonVisibility(true, false);
            setButtonEnableForLGUOEMCamera(true);
            this.mReviewThumbnailManager.setEnabled(getUri() != null);
            if (!(this.mFocusManager == null || this.mFocusManager.isFocusLock() || this.mFocusManager.isAELock())) {
                this.mFocusManager.hideAndCancelAllFocus(false);
                if (isFocusEnableCondition()) {
                    this.mFocusManager.registerCallback(true);
                }
            }
            hideZoomBar();
            if (!getRecordingPreviewState(1)) {
                return true;
            }
            setRecordingPreviewState(8, true);
            onShutterStopButtonClicked();
            return true;
        } else if (!checkModuleValidate(64)) {
            return true;
        } else {
            if (checkModuleValidate(128)) {
                boolean isShutterCallback;
                boolean isProgressCapture;
                boolean isLivePhotoSavingProgress;
                boolean z;
                if (this.mHandler != null) {
                    if (this.mGet.isSettingChildMenuVisible()) {
                        this.mGet.removeChildSettingView(true);
                        return true;
                    } else if (this.mGet.isSettingMenuVisible()) {
                        this.mHandler.sendEmptyMessage(32);
                        return true;
                    } else if (this.mGet.isModeMenuVisible()) {
                        if (this.mGet.isModeEditable()) {
                            if (getDialogID() == 150) {
                                this.mDialogManager.onDismissRotateDialog();
                                return true;
                            }
                            this.mGet.setModeEditMode(false);
                            return true;
                        } else if (this.mHandler == null) {
                            return true;
                        } else {
                            this.mHandler.sendEmptyMessage(36);
                            return true;
                        }
                    } else if (this.mGet.isHelpListVisible()) {
                        this.mHandler.sendEmptyMessage(49);
                        return true;
                    } else if (this.mInitGuideManager != null && this.mInitGuideManager.isInitialHelpGuideVisible()) {
                        this.mHandler.sendEmptyMessage(97);
                        return true;
                    } else if (isColorEffectMenuVisible()) {
                        this.mHandler.sendEmptyMessage(101);
                        return true;
                    }
                }
                stopBurstShotTaking(false);
                if (this.mSnapShotChecker.getPictureCallbackState() >= 1) {
                    isShutterCallback = true;
                } else {
                    isShutterCallback = false;
                }
                if (checkModuleValidate(16)) {
                    isProgressCapture = false;
                } else {
                    isProgressCapture = true;
                }
                if (isLivePhotoEnabled() && this.mLivePhotoManager.isLivePhotoSavingProgress()) {
                    isLivePhotoSavingProgress = true;
                } else {
                    isLivePhotoSavingProgress = false;
                }
                String str = CameraConstants.TAG;
                StringBuilder append = new StringBuilder().append("doBackKey - isShutterCallback : ").append(isShutterCallback).append(", isProgressCapture = ").append(isProgressCapture).append(", !isLivePhotoSavingProgress = ");
                if (isLivePhotoSavingProgress) {
                    z = false;
                } else {
                    z = true;
                }
                CamLog.m3d(str, append.append(z).toString());
                if (isProgressCapture && isShutterCallback && !isLivePhotoSavingProgress) {
                    this.mNeedProgressDuringCapture |= 65536;
                }
                if (this.mWaitSavingDialogType == 4) {
                    this.mWaitSavingDialogType = 1;
                }
                if (onReviewThumbnailClicked(1)) {
                    this.mIsNeedFinishAfterSaving = true;
                    return true;
                } else if (!this.mGet.checkModuleValidate(48)) {
                    CamLog.m3d(CameraConstants.TAG, "doBackKey return! because of capture progress.");
                    return true;
                } else if (this.mDialogManager != null && this.mDialogManager.isBackKeyAvailable()) {
                    if (this.mDialogManager.getDialogID() == 2) {
                        this.mReviewThumbnailManager.hideQuickView(true);
                    }
                    this.mDialogManager.onDismissRotateDialog();
                    return true;
                } else if (this.mQuickClipManager != null && this.mQuickClipManager.isOpened()) {
                    this.mQuickClipManager.drawerClose(true);
                    return true;
                } else if (this.mGet.unselectUspOnBackKey()) {
                    return true;
                } else {
                    if (this.mStickerManager != null && this.mStickerManager.isStickerGridVisible()) {
                        if (!this.mStickerManager.isWaitOneShot()) {
                            if (this.mStickerManager.isStickerDrawing()) {
                                hideStickerMenu(false);
                            } else {
                                hideStickerMenu(true);
                            }
                        }
                        if (this.mHandler == null || !isMenuShowing(4)) {
                            return true;
                        }
                    }
                    if (this.mHandler == null || !isMenuShowing(4) || this.mAdvancedFilmManager.isFilterMenuAnimationWorking()) {
                        this.mGet.stopQuickShotModeByBackkey();
                        if (isRearCamera() && FunctionProperties.getCameraTypeRear() == 1) {
                            int rearCameraId = SharedPreferenceUtil.getRearCameraId(this.mGet.getAppContext());
                            CamLog.m3d(CameraConstants.TAG, "rearCameraId = " + rearCameraId);
                            SharedPreferenceUtilBase.setCameraId(getAppContext(), rearCameraId);
                        }
                        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                            this.mGet.setPreviewCoverParam(false);
                        }
                        return false;
                    }
                    this.mHandler.sendEmptyMessage(84);
                    return true;
                }
            }
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.updateVideoTime(3, SystemClock.uptimeMillis());
                this.mRecordingUIManager.setRecDurationTime((long) getRecCompensationTime());
                if (!this.mRecordingUIManager.checkMinRecTime()) {
                    CamLog.m7i(CameraConstants.TAG, "Video recording time is too short.");
                    return true;
                }
            }
            onShutterStopButtonClicked();
            this.mReviewThumbnailManager.setThumbnailVisibility(0);
            return true;
        }
    }

    public boolean doHeadSetHookAndMediaKey(KeyEvent event) {
        if (!checkModuleValidate(192) || this.mTimerManager.isTimerShotCountdown() || "on".equals(this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER))) {
            return true;
        }
        return false;
    }

    public boolean isEnableVolumeKey() {
        return false;
    }

    public void doOkClickInDeleteConfirmDialog(int type) {
        if (this.mStickerManager != null && this.mStickerManager.isRunning() && type == 151 && !this.mStickerManager.deleteOK()) {
            this.mExtraPrevewUIManager.setEditMode(false);
        }
        if (type == 149) {
            this.mAdvancedFilmManager.deleteDownloadedFilter();
            return;
        }
        if (this.mPostviewManager != null) {
            this.mPostviewManager.doOkClickInDeleteConfirmDialog();
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.doButtonClickInDeleteConfirmDialog(true);
        }
    }

    public void doCancelClickInDeleteConfirmDialog() {
        if (this.mStickerManager != null && this.mStickerManager.isRunning()) {
            this.mStickerManager.deleteCancel();
        }
        if (this.mPostviewManager != null) {
            this.mPostviewManager.doCancelClickInDeleteConfirmDialog();
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.doButtonClickInDeleteConfirmDialog(false);
        }
    }

    public void doCancleClickDuringSlomoSaving() {
        if (this.mGet != null) {
            this.mGet.sendCancelNotiToSlomoSaveService();
            this.mGet.unbindSlomoSaveService();
            if (this.mQuickClipManager != null) {
                CamLog.m3d(CameraConstants.TAG, "[slomo_save] doCancleClickDuringSlomoSaving, set quick clip");
                this.mQuickClipManager.drawerClose(true);
                if (this.mQuickClipManager.getSlomoBroadcastReceiver() != null) {
                    CamLog.m7i(CameraConstants.TAG, "[slomo_share] Unregister BroadCast for SlomoSaveService.");
                    this.mGet.unregisterBroadcastReceiver(this.mQuickClipManager.getSlomoBroadcastReceiver());
                }
            }
        }
    }

    public void doTouchShot() {
        if (!checkModuleValidate(192) || ((this.mReviewThumbnailManager != null && this.mReviewThumbnailManager.isQuickViewAniStarted()) || this.mSnapShotChecker.checkMultiShotState(2) || this.mIsShutterlessSelfieProgress)) {
            this.mFocusManager.releaseTouchFocus();
            return;
        }
        LdbUtil.setShutterType(CameraConstants.SHUTTER_TYPE_TOUCH);
        onCameraShutterButtonClicked();
    }

    public void unregisterBroadcastReceiver(BroadcastReceiver receiver) {
        this.mGet.unregisterBroadcastReceiver(receiver);
    }

    public void addQuickButtonListDone() {
    }

    public void onPrepareCineZoom() {
        if (this.mZoomManager != null && this.mCameraDevice != null && this.mParamUpdater != null && this.mQuickButtonManager != null) {
            CameraParameters params = this.mCameraDevice.getParameters();
            if (params != null) {
                this.mQuickButtonManager.setVisibility(C0088R.id.quick_button_caf, 4, false);
                hideFocusOnShowOtherBars(true);
                this.mZoomManager.setZoomValue(0);
                this.mZoomManager.updateExtraInfo("0");
                this.mParamUpdater.setParameters(params, "zoom", "0");
                this.mCameraDevice.setParameters(params);
                setTrackingFocusState(false);
            }
        }
    }

    public void setFocusInVisible(boolean isInvisible) {
        if (this.mFocusManager != null) {
            this.mFocusManager.setFocusInVisible(isInvisible);
        }
    }

    public void requestFocusOnShutterButton(boolean focusable) {
        requestFocusOnShutterButton(focusable, false);
    }

    public void requestFocusOnShutterButton(boolean focusable, boolean isInTouchMode) {
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            this.mCaptureButtonManager.requestShutterButtonFocus(focusable, this.mGet.isVideoCaptureMode() ? 1 : 2, isInTouchMode);
        }
    }

    public boolean isFocusEnableCondition() {
        if (this.mZoomManager == null || this.mZoomManager.isZoomBarVisible() || isJogZoomMoving() || ((this.mTimerManager != null && this.mTimerManager.isTimerShotCountdown()) || this.mSnapShotChecker.getSnapShotState() >= 2 || this.mIsSwitchingCameraDuringRecording || this.mSnapShotChecker.checkMultiShotState(7) || (this.mGet.isVoiceAssistantSpecified() && CameraConstantsEx.FLAG_VALUE_MODE_NIGHTVISION.equals(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null))))) {
            return true & 0;
        }
        return true;
    }

    public void onGestureFlicking(MotionEvent e1, MotionEvent e2, int gestureType) {
        if (this.mReviewThumbnailManager != null && this.mReviewThumbnailManager.isActivatedSelfieQuickView()) {
            CamLog.m3d(CameraConstants.TAG, "-swap- isActivatedSelfieQuickView is on return.");
            hideSelfieQuickView();
        } else if (this.mSnapShotChecker.checkMultiShotState(2)) {
            CamLog.m3d(CameraConstants.TAG, "-swap- mIsIntervalShotProgress is on return.");
        } else if (!isRecordingState()) {
            CamLog.m3d(CameraConstants.TAG, "onGestureFlicking : " + gestureType);
            switch (this.mGestureManager.getConvertedGestureType(getOrientationDegree(), gestureType)) {
                case 0:
                case 1:
                case 2:
                case 3:
                    if (!isMenuShowing(5)) {
                        onGestureCleanViewDetected();
                        return;
                    }
                    return;
                case 4:
                    if (!isMenuShowing(4)) {
                        if (this.mStickerManager != null && this.mStickerManager.isStickerMenuVisible()) {
                            return;
                        }
                        if (isSettingMenuVisible()) {
                            this.mHandler.sendEmptyMessage(32);
                            return;
                        } else if (isSupportedFilterMenu() && this.mExtraPrevewUIManager.getLastSelectedMenu() == 0) {
                            this.mHandler.sendEmptyMessage(83);
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                case 5:
                    if (!isSettingMenuVisible()) {
                        if (isMenuShowing(4)) {
                            this.mHandler.sendEmptyMessage(84);
                            return;
                        } else {
                            this.mHandler.sendEmptyMessage(30);
                            return;
                        }
                    }
                    return;
                default:
                    CamLog.m3d(CameraConstants.TAG, "gesture action fail");
                    return;
            }
        }
    }

    public void onGestureCleanViewDetected() {
        if (this.mIsTouchStartedFromNaviArea || this.mIsPreviewCallbackWaiting || ModelProperties.isKeyPadSupported(getAppContext()) || !((this.mCameraCapabilities == null || this.mCameraCapabilities.isFrontCameraSupported()) && this.mSnapShotChecker.getSnapShotState() < 2 && checkModuleValidate(48) && !this.mSnapShotChecker.checkMultiShotState(7) && this.mIsScreenCaptured)) {
            CamLog.m3d(CameraConstants.TAG, "exit onGestureCleanViewDetected, mIsScreenCaptured : " + this.mIsScreenCaptured);
        } else if (SystemBarUtil.isSystemUIVisible(getActivity())) {
            this.mGet.setNaviBarVisibility(true, 200);
        } else if (checkModuleValidate(200) && this.mGet.getPreviewCoverVisibility() != 0) {
            if ((this.mTimerManager == null || !this.mTimerManager.isTimerShotCountdown()) && !this.mZoomManager.isZoomBarVisible() && !this.mGet.isAnimationShowing() && !CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) && !CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) && !isActivatedQuickdetailView()) {
                CamLog.m7i(CameraConstants.TAG, "-swap- onGestureCleanViewDetected");
                stopPreviewThread();
                this.mGet.setGestureType(this.mGestureManager.getGestureFlickingType());
                this.mGet.startCameraSwitchingAnimation(1);
                if (this.mHandler != null) {
                    this.mHandler.removeMessages(6);
                    this.mHandler.sendEmptyMessage(6);
                    afterSendSwitchCameraMsgByGesture();
                    LdbUtil.sendLDBIntentForSwapCamera(this.mGet.getAppContext(), isRearCamera(), "Flicking");
                }
            }
        }
    }

    protected void afterSendSwitchCameraMsgByGesture() {
    }

    public Uri getUri() {
        if (this.mReviewThumbnailManager == null) {
            return null;
        }
        if (!isSquareGalleryBtn()) {
            return this.mReviewThumbnailManager.getUri();
        }
        SquareSnapGalleryItem item = getCurSquareSnapItem();
        if (item != null) {
            return item.mUri;
        }
        return null;
    }

    public void onInitialGuideConfirm(int guideStep) {
        if (this.mInitGuideManager != null && this.mHandler != null) {
            this.mHandler.sendEmptyMessage(38);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(39, guideStep, 0), 1000);
        }
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        CamLog.m7i(CameraConstants.TAG, "mainHandlerHandleMessage : " + msg.what);
        switch (msg.what) {
            case 40:
                CamLog.m3d(CameraConstants.TAG, "postview released");
                int curDegree = getOrientationDegree();
                Iterator it = this.mManagerList.iterator();
                while (it.hasNext()) {
                    ((ManagerInterfaceImpl) it.next()).setRotateDegree(curDegree, false);
                }
                if (this.mDialogManager != null) {
                    this.mDialogManager.setOrientationDegree(curDegree);
                }
                return true;
            case 62:
                updateHDRSettingValue("0");
                return true;
            case 63:
                updateHDRSettingValue("1");
                return true;
            case 64:
                updateHDRSettingValue("2");
                return true;
            case 85:
                CamLog.m3d(CameraConstants.TAG, "[Film] start film emulator");
                if (this.mAdvancedFilmManager != null) {
                    this.mAdvancedFilmManager.setFilmState(3);
                }
                postOnUiThread(this.mFilmEmulatorStartHandler, 0);
                return true;
            case 89:
                showToast(this.mGet.getAppContext().getResources().getString(C0088R.string.filter_burstshot_limitation_desc), CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                return true;
            case 90:
                prepareRecordingVideo();
                return true;
            case 95:
                CamLog.m3d(CameraConstants.TAG, "handle HIDE_TILE_DETAIL_VIEW");
                if (this.mFocusManager != null) {
                    this.mFocusManager.registerCallback(false);
                }
                if (this.mCameraState >= 1 || this.mIsPreviewCallbackWaiting) {
                    this.mGet.closeDetailView();
                } else {
                    this.isNeedHideDetailView = true;
                    setupPreview(null);
                }
                this.mShowBinningToastCondition = true;
                return true;
            case 102:
                updateTimer("0");
                return true;
            case 103:
                updateTimer("3");
                return true;
            case 104:
                updateTimer("10");
                return true;
            case 109:
                onLivePhotoEncodeAfter((ImageUriInfo) msg.obj);
                return true;
            case 115:
                onFullVisionSettingClicked(true);
                return true;
            case 116:
                onFullVisionSettingClicked(false);
                return true;
            default:
                return super.mainHandlerHandleMessage(msg);
        }
    }

    protected void onLivePhotoEncodeAfter(ImageUriInfo info) {
        if (this.mDialogManager != null && this.mDialogManager.isRotateDialogVisible(5)) {
            CamLog.m3d(CameraConstants.TAG, "-Live photo- mWaitSavingDialogType = " + this.mWaitSavingDialogType);
            this.mDialogManager.onDismissRotateDialog();
            if (this.mWaitSavingDialogType == 1 && checkModuleValidate(1)) {
                this.mGet.getActivity().finish();
            }
        }
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonEnable(true, getShutterButtonType());
            this.mCaptureButtonManager.setExtraButtonEnable(true, 3);
        }
        if (this.mSnapShotChecker.isAvailableNightAndFlash() && isFastShotAvailable(3)) {
            this.mGet.postOnUiThread(this.mOnPictureTakenCallbackAfter, 300);
        } else {
            onPictureCallbackAfterRun(false, false);
        }
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setAfterShot();
        }
        if (info == null) {
            CamLog.m11w(CameraConstants.TAG, "-Live Photo- ImageUriInfo is null");
            return;
        }
        ExifInterface exif = info.mExif;
        int exifDegree = info.mDegree;
        int[] exifSize = Exif.getExifSize(exif);
        updateThumbnail(exif, exifDegree, false);
        Uri uri = FileManager.registerImageUri(info.mCR, info.mDirectory, info.mFileName, info.mDateTaken, info.mLocation, info.mDegree, exifSize, info.mIsBurstShot, true);
        checkSavedURI(uri);
        FileNamer.get().removeFileNameInSaving(info.mFileName + ".jpg");
        makeThumbnailForTilePreview(exif, uri);
        doSaveImageAfter(uri, Utils.getCameraModeColumn(info.mFileName + ".jpg", getShotMode()));
        checkStorage();
        doSaveImagePostExecute(uri);
    }

    public boolean isLightFrameOn() {
        return this.mLightFrameManager.isLightFrameMode();
    }

    protected void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onCameraSwitchingStart();
        }
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            this.mAdvancedFilmManager.moveFilmPreviewOutOfWindow(true);
        }
        if (!isLightFrameOn() && HybridViewConfig.SURFACE.equals(this.mGet.getCurrentViewType())) {
            this.mGet.setPreviewVisibility(4);
        }
    }

    public void stopPreviewThread() {
        new StopPreviewThread().start();
    }

    protected void onCameraSwitchingEnd() {
        super.onCameraSwitchingEnd();
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onCameraSwitchingEnd();
        }
    }

    public void launchGallery(final Uri uri, final int galleryPlayType) {
        if (!isCaptureCompleted()) {
            CamLog.m3d(CameraConstants.TAG, "Capture should be completed, return.");
        } else if (this.mReviewThumbnailManager == null) {
        } else {
            if (SecureImageUtil.isSecureCamera()) {
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (uri == null && DefaultCameraModule.this.getUri() == null) {
                            DefaultCameraModule.this.mReviewThumbnailManager.launchGallery(galleryPlayType);
                        } else {
                            DefaultCameraModule.this.mReviewThumbnailManager.launchGallery(uri == null ? DefaultCameraModule.this.getUri() : uri, galleryPlayType);
                        }
                    }
                });
            } else if (uri == null && getUri() == null) {
                this.mReviewThumbnailManager.launchGallery(galleryPlayType);
            } else {
                GestureViewManager gestureViewManager = this.mReviewThumbnailManager;
                if (uri == null) {
                    uri = getUri();
                }
                gestureViewManager.launchGallery(uri, galleryPlayType);
            }
        }
    }

    public void setLaunchGalleryLocation(float[] loc) {
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setLaunchGalleryLocation(loc);
        }
    }

    protected boolean onReviewThumbnailClicked(int waitType) {
        CamLog.m3d(CameraConstants.TAG, "onReviewThumbnailClicked. waitType : " + waitType + ", mWaitSavingDialogType : " + this.mWaitSavingDialogType);
        if (this.mSnapShotChecker.checkMultiShotState(1) || this.mWaitSavingDialogType == 4) {
            return true;
        }
        if (isMenuShowing(4) && waitType != 1) {
            this.mGet.removeSettingMenu(true, true);
        }
        int curQueueCount = this.mGet.getQueueCount();
        CamLog.m3d(CameraConstants.TAG, "Queue count = " + curQueueCount + " Progress type= " + this.mNeedProgressDuringCapture);
        boolean showDialog = false;
        if (this.mNeedProgressDuringCapture > 0) {
            showDialog = true;
        }
        if (curQueueCount <= 0 && !showDialog) {
            this.mWaitSavingDialogType = 0;
            return false;
        } else if (this.mDialogManager != null && this.mDialogManager.isProgressDialogVisible()) {
            return true;
        } else {
            if (CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) || CameraConstants.MODE_SQUARE_GRID.equals(getShotMode())) {
                CamLog.m3d(CameraConstants.TAG, "multiview back key should not be operted here.");
                return true;
            }
            CamLog.m3d(CameraConstants.TAG, "saving dialog : review thumbnail clicked");
            showSavingDialog(true, 0);
            this.mWaitSavingDialogType = waitType;
            return true;
        }
    }

    public void setWaitSavingDialogType(int waitType) {
        this.mWaitSavingDialogType = waitType;
    }

    public int getWaitSavingDialogType() {
        return this.mWaitSavingDialogType;
    }

    public void onDialogShowing() {
        if (this.mDialogManager != null) {
            requestFocusOnShutterButton(false);
            this.mGet.enableConeMenuIcon(31, false);
        }
    }

    public void onDialogDismiss() {
        if (this.mDialogManager != null) {
            switch (this.mDialogManager.getDialogID()) {
                case 7:
                    if (checkModuleValidate(1)) {
                        checkPastSDInsertionStatus();
                        if (SharedPreferenceUtil.getNeedShowStorageInitDialog(this.mGet.getAppContext()) != 0 || this.mStorageManager.isStorageRemoved(1)) {
                            this.mStorageManager.selectStorageBySystemSetting(false);
                            showManualModeInitDialog();
                            break;
                        }
                        this.mDialogManager.onDismiss();
                        if (!isRequestedSingleImage()) {
                            showDialog(8, true);
                            return;
                        }
                        return;
                    }
                    break;
                case 8:
                    if (SharedPreferenceUtil.getNeedShowStorageInitDialog(this.mGet.getAppContext()) == 1) {
                        this.mStorageManager.selectStorageBySystemSetting(true);
                        showManualModeInitDialog();
                        break;
                    }
                    break;
                case 12:
                    if (checkModuleValidate(1)) {
                        if (isSettingMenuVisible()) {
                            this.mGet.updateLocationSwitchButton();
                        }
                        this.mDialogManager.onDismiss();
                        this.mGet.enableConeMenuIcon(31, true);
                        requestFocusOnShutterButton(true);
                        return;
                    }
                    break;
                case 138:
                    if (isPaused()) {
                        this.mGet.enableConeMenuIcon(31, true);
                        requestFocusOnShutterButton(true);
                    }
                    this.mDialogManager.onDismiss();
                    return;
                case 144:
                    if (this.mUndoManager != null) {
                        this.mUndoManager.onDismissUndoDialog();
                        break;
                    }
                    break;
            }
            this.mDialogManager.onDismiss();
            this.mGet.enableConeMenuIcon(31, true);
            requestFocusOnShutterButton(true);
        }
    }

    private void showManualModeInitDialog() {
        if (CameraConstants.MODE_MANUAL_CAMERA.equals(getShotMode()) && FunctionProperties.isNeedToShowInitialManualModeDialog(getAppContext()) && this.mDialogManager != null) {
            this.mDialogManager.showDialogPopup(148, true);
        }
    }

    public boolean showLocationPermissionRequestDialog(boolean needCameraLocSetting) {
        CamLog.m3d(CameraConstants.TAG, "showLocationPermissionRequestDialog");
        return this.mGet.showLocationPermissionRequestDialog(needCameraLocSetting);
    }

    protected void checkPastSDInsertionStatus() {
        if (SharedPreferenceUtil.getPastSDInsertionStatus(this.mGet.getAppContext()) == 0 && !this.mStorageManager.isStorageRemoved(1) && StorageProperties.isAllMemoryMounted(this.mGet.getAppContext())) {
            SharedPreferenceUtilBase.saveNeedShowStorageInitDialog(this.mGet.getAppContext(), 0);
        }
    }

    public boolean isUHDmode() {
        return ParamConstants.VIDEO_3840_BY_2160.equalsIgnoreCase(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)).split("@")[0]);
    }

    public boolean isFHD60() {
        return CameraConstants.VIDEO_FHD_60FPS.equals(getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId)));
    }

    public boolean isFHDCinema60() {
        return false;
    }

    public boolean isUnderHDResolution() {
        return false;
    }

    protected boolean isBinningRecordResolutionLimitation() {
        return (isUHDmode() || isFHD60() || isUnderHDResolution()) ? false : true;
    }

    protected int[] getMaxResolutionWidthHeight() {
        ListPreference pref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        if (pref != null) {
            CharSequence[] entryValues = pref.getEntryValues();
            if (entryValues != null) {
                return Utils.sizeStringToArray((String) entryValues[0]);
            }
        }
        return new int[]{0, 0};
    }

    protected void setVoiceShutter(boolean enable, int watingTime) {
        if (this.mCheeseShutterManager == null) {
            return;
        }
        if (enable) {
            String value = this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER);
            if (watingTime != 0) {
                this.mCheeseShutterManager.setCheeseShutterSetting("on".equals(value), true, watingTime);
            } else {
                this.mCheeseShutterManager.setCheeseShutterSetting("on".equals(value), true);
            }
            setSettingMenuEnable(Setting.KEY_VOICESHUTTER, true);
            return;
        }
        this.mGet.setSettingMenuEnable(Setting.KEY_VOICESHUTTER, false);
        this.mCheeseShutterManager.setCheeseShutterSetting(false, true);
        setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
    }

    protected void showHeadsetRecordingToastPopup() {
        this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.recording_start_with_mic_on_earphones));
    }

    public int checkFeatureDisableBatteryLevel(int checkType, boolean extra) {
        if (!ModelProperties.isBatteryLevelConstraintNeeded()) {
            int disableReason = 0;
            if (checkType == 1) {
                if (AppControlUtil.getBatteryLevel() <= FunctionProperties.getDisabledFlashBatteryLevel()) {
                    disableReason = 1;
                }
                if (this.mCameraCapabilities != null && this.mCameraCapabilities.isFlashSupported()) {
                    if (extra && "off".equals(getSettingValue("flash-mode"))) {
                        return disableReason;
                    }
                    if (extra) {
                        return 0;
                    }
                    return disableReason;
                }
            }
        } else if (AppControlUtil.getBatteryLevel() > 10 && Float.compare(AppControlUtil.getVoltageLevel(), 3.52f) > 0) {
            return 0;
        } else {
            ListPreference videoSizePref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
            String slowMotion = this.mGet.getAppContext().getString(C0088R.string.slow_motion);
            if (checkType == 2 && slowMotion != null && videoSizePref != null && slowMotion.equals(videoSizePref.getEntry())) {
                return 1;
            }
            if (checkType == 1 && this.mCameraCapabilities != null && this.mCameraCapabilities.isFlashSupported()) {
                if (extra && "off".equals(getSettingValue("flash-mode"))) {
                    return 1;
                }
                if ("off".equals(getSettingValue("flash-mode"))) {
                    return 0;
                }
                return 1;
            }
        }
        return 0;
    }

    protected void turnOffCheeseShutter(boolean turnOff) {
        String value = this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER);
        if (!"on".equals(value)) {
            return;
        }
        if (turnOff) {
            if (this.mCheeseShutterManager != null) {
                this.mCheeseShutterManager.setCheeseShutterSetting(false, false);
            }
        } else if (this.mCheeseShutterManager != null) {
            this.mCheeseShutterManager.setCheeseShutterSetting("on".equals(value), true);
        }
    }

    public boolean isGestureShutterEnableCondition() {
        return !isRearCamera() || getShotMode().contains(CameraConstants.MODE_SQUARE);
    }

    public boolean isIntervalShotEnableCondition() {
        return !isRearCamera() || getShotMode().contains(CameraConstants.MODE_SQUARE);
    }

    public boolean isAudioZoomAvailable() {
        return isZoomAvailable(false);
    }

    public boolean isZoomAvailable(boolean checkRecordingState) {
        ListPreference listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        String slowMotion = this.mGet.getAppContext().getString(C0088R.string.slow_motion);
        if (listPref != null && ((this.mCameraState == 6 || this.mCameraState == 7) && slowMotion != null && slowMotion.equals(listPref.getEntry()))) {
            return false;
        }
        if (!checkRecordingState || checkModuleValidate(64)) {
            return super.isZoomAvailable(checkRecordingState);
        }
        CamLog.m3d(CameraConstants.TAG, "cannot zoom camera state is STATE_VIDEO_RECORDING_WAIT");
        return false;
    }

    public boolean isZoomAvailable() {
        return isZoomAvailable(true);
    }

    public boolean isSuperZoomEnableCondition() {
        return isZoomAvailable() && FunctionProperties.isSupportedSuperZoom();
    }

    public void onHeadsetPlugged(int headsetState) {
        AudioUtil.setHeadsetState(headsetState);
        if (headsetState == 2) {
            if (VideoRecorder.isRecording() && isAudioZoomAvailable() && this.mAudioZoomManager != null) {
                this.mAudioZoomManager.setAngle(0);
            }
            if (this.mCameraState == 6 || this.mCameraState == 7) {
                showHeadsetRecordingToastPopup();
            }
        }
    }

    public void onHeadsetUnPlugged() {
        doOnHeadsetUnPlugged();
    }

    protected void doOnHeadsetUnPlugged() {
        AudioUtil.setHeadsetState(0);
        if (this.mCameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera device is null.");
        } else if (this.mCameraDevice.getParameters() == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera parameters is null.");
        } else if (!checkModuleValidate(128)) {
            setMicPath();
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            AudioManager am = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
            if ("on".equals(getSettingValue(Setting.KEY_VOICESHUTTER)) && !TelephonyUtil.phoneInCall(this.mGet.getAppContext()) && am.isMusicActive() && !QuickWindowUtils.isQuickWindowCameraMode() && this.mGet.isResumeAfterProcessingDone() && this.mCheeseShutterManager != null) {
                setSetting(Setting.KEY_VOICESHUTTER, "off", true);
                refreshSetting();
                this.mCheeseShutterManager.setCheeseShutterSetting(false, false);
            }
            if (FunctionProperties.isLivePhotoSupported() && this.mLivePhotoManager != null) {
                this.mLivePhotoManager.setHasWindowFocus(true);
                if ("on".equals(getSettingValue(Setting.KEY_LIVE_PHOTO)) && !isRecordingState() && this.mAdvancedFilmManager != null && !this.mAdvancedFilmManager.isShowingFilmMenu()) {
                    this.mLivePhotoManager.enableLivePhoto();
                    return;
                }
                return;
            }
            return;
        }
        stopBurstShotTaking(false);
        setBatteryIndicatorVisibility(true);
        if (FunctionProperties.isLivePhotoSupported() && this.mLivePhotoManager != null) {
            this.mLivePhotoManager.setHasWindowFocus(false);
            this.mLivePhotoManager.disableLivePhoto();
        }
    }

    public boolean isSupportFaceFocusModule() {
        return true;
    }

    public boolean isShoToShotJustEnd() {
        return this.mSnapShotChecker.checkMultiShotState(8);
    }

    public void restoreSettingValueInMdm() {
        if (this.mCheeseShutterManager != null) {
            restoreSettingValue(Setting.KEY_VOICESHUTTER);
            this.mCheeseShutterManager.setCheeseShutterSetting("on".equals(this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER)), true);
            setCaptureButtonEnable(true, 1);
            setSettingMenuEnable(Setting.KEY_VOICESHUTTER, true);
        }
    }

    public void disableSettingValueInMdm() {
        if (!checkModuleValidate(128)) {
            onVideoStopClicked(false, false);
        }
        showToast(this.mGet.getAppContext().getString(FunctionProperties.isSupportedVoiceShutter() ? C0088R.string.block_recording_and_cheeseshutter_mdm : C0088R.string.block_recording_mdm), 2000);
        setVoiceShutter(false, 0);
        if (this.mDialogManager.isRotateDialogVisible()) {
            this.mDialogManager.onDismissRotateDialog();
        }
        setCaptureButtonEnable(false, 1);
    }

    public boolean isOrientationLocked() {
        return this.mGet.isOrientationLocked();
    }

    public void onPreviewFrame(byte[] data, CameraProxy camera) {
        if (data != null && camera != null) {
            if (this.mQRCodeManager != null && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR))) {
                this.mQRCodeManager.onImageData(null, data, camera);
            }
            if (this.mStickerManager != null && this.mStickerManager.isGLSurfaceViewShowing()) {
                this.mStickerManager.onPreviewFrame(data);
            }
            this.mDirectCallbackManager.addCallbackBuffer();
        }
    }

    public void onImageData(Image image) {
        if (image != null) {
            if (this.mQRCodeManager != null && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR))) {
                this.mQRCodeManager.onImageData(image, null, null);
            }
            if (this.mStickerManager != null && this.mStickerManager.isGLSurfaceViewShowing()) {
                this.mStickerManager.onPreviewFrame(image);
            }
            if (isLivePhotoEnabled()) {
                this.mLivePhotoManager.onPreviewFrame(image);
            }
            image.close();
        }
    }

    protected boolean checkPreviewCallbackCondition() {
        return isRearCamera();
    }

    protected void setPreviewCallbackAll(boolean set) {
        CamLog.m3d(CameraConstants.TAG, "setPreviewCallbackAll: " + set + ", checkPreviewCallbackCondition = " + checkPreviewCallbackCondition());
        if (checkPreviewCallbackCondition()) {
            Object thisR;
            DirectCallbackManager directCallbackManager = this.mDirectCallbackManager;
            if (!set) {
                thisR = null;
            }
            directCallbackManager.setPreviewDataCallback(thisR);
        }
    }

    protected void onSurfaceChanged(SurfaceHolder holder, int width, int height) {
        if (this.mQRCodeManager != null && this.mQRCodeManager.isRunningQRCode()) {
            this.mQRCodeManager.onSurfaceChanged();
        }
        super.onSurfaceChanged(holder, width, height);
    }

    public void onCameraKeyUp(KeyEvent event) {
        if (!checkShutterUpState() && !this.mSnapShotChecker.checkMultiShotState(2)) {
            switch (this.mCameraState) {
                case 6:
                case 7:
                    onSnapShotButtonClicked();
                    break;
                default:
                    onCameraShutterButtonClicked();
                    break;
            }
            if (this.mSnapShotChecker.checkMultiShotState(4)) {
                this.mSnapShotChecker.setMultiShotState(8);
            }
            stopBurstShotTaking(false);
        }
    }

    public void doBLEOneKeyAction(boolean isShortKey) {
        if (!this.mSnapShotChecker.checkMultiShotState(2) && !isPostviewShowing()) {
            if (!checkModuleValidate(192)) {
                onSnapShotButtonClicked();
            } else if (!isVideoCaptureMode()) {
                onCameraShutterButtonClicked();
            }
        }
    }

    public void updateZoomParam(CameraParameters parameters, int value) {
        super.updateZoomParam(parameters, value);
        if (this.mAudioZoomManager != null && getCameraState() == 6) {
            boolean[] checkAudioZoom = isAvailableAudiozoom();
            int angle = 0;
            int orientation = getOrientationDegree();
            if (checkAudioZoom[0]) {
                angle = this.mZoomManager == null ? 0 : this.mZoomManager.getZoomValue();
            } else if (checkAudioZoom[1]) {
                orientation = 6;
            }
            if (isAudioZoomAvailable()) {
                this.mAudioZoomManager.setAngle(angle);
                this.mAudioZoomManager.setOrientation(orientation);
            }
        }
    }

    public void setBarVisible(int barType, boolean show, boolean enable) {
        if (this.mBarManager != null) {
            this.mBarManager.setVisible(barType, show, true);
            this.mBarManager.setEnable(barType, enable);
        }
    }

    public boolean isZoomBarVisible() {
        return this.mZoomManager != null && this.mZoomManager.isZoomBarVisible();
    }

    public boolean getBurstProgress() {
        return this.mSnapShotChecker.checkMultiShotState(1);
    }

    public boolean isNeedToCheckFlashTemperature() {
        return this.mIsNeedToCheckFlashTemperature;
    }

    protected void checkHeatingConditionForFlashOff() {
        if (ModelProperties.getCarrierCode() == 4) {
            CamLog.m3d(CameraConstants.TAG, "checkHeatingConditionForFlashOff, mIsNeedToCheckFlashTemperature : " + this.mIsNeedToCheckFlashTemperature);
            this.mIsNeedToCheckFlashTemperature = true;
            if (((LGThermalManager) new LGContext(getActivity()).getLGSystemService("lgthermal")).isThermalActionTriggered("com.lge.camera.flash_off")) {
                setFlashOffByHighTemperature(true);
            }
        }
    }

    public boolean setFlashOffByHighTemperature(boolean isSetParam) {
        if (this.mParamUpdater == null || ModelProperties.getCarrierCode() != 4) {
            return false;
        }
        if (!ParamConstants.FLASH_MODE_TORCH.equals(this.mParamUpdater.getParamValue("flash-mode"))) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "Temperature is high, so flash is set to off");
        showToast(this.mGet.getActivity().getString(C0088R.string.warning_high_temp_action_flash_off), 2000);
        this.mGet.setSetting("flash-mode", "off", true);
        if (this.mCameraDevice == null) {
            return false;
        }
        setFlashOnRecord(this.mCameraDevice.getParameters(), false, isSetParam);
        this.mIsNeedToCheckFlashTemperature = false;
        setQuickButtonIndex(C0088R.id.quick_button_flash, 0);
        setQuickButtonEnable(C0088R.id.quick_button_flash, false, true);
        return true;
    }

    public void startCameraSwitchingAnimation(int animationType) {
        this.mGet.startCameraSwitchingAnimation(animationType);
    }

    public void setPreviewCoverBackground(Drawable bg) {
        this.mGet.setPreviewCoverBackground(bg);
    }

    public void setPreviewCoverVisibility(int visibility, boolean isMovePreview) {
        this.mGet.setPreviewCoverVisibility(visibility, isMovePreview);
    }

    public void setPreviewCoverVisibility(int visibility, boolean useAnim, AnimationListener listener, boolean isMovePreview, boolean usePreviewCapture) {
        this.mGet.setPreviewCoverVisibility(visibility, useAnim, listener, isMovePreview, usePreviewCapture);
    }

    public void setGestureType(int type) {
        this.mGet.setGestureType(type);
    }

    public boolean isAnimationShowing() {
        return this.mGet.isAnimationShowing();
    }

    public void setAnimationShowing(boolean isShowing) {
        this.mGet.setAnimationShowing(isShowing);
    }

    public boolean isIntervalShotProgress() {
        return this.mSnapShotChecker.checkMultiShotState(2);
    }

    public Bitmap getCurPreviewBlurredBitmap(int scaledWidth, int scaledHeight, int blurRadius, boolean surfaceOnly) {
        if (CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            return null;
        }
        return this.mGet.getCurPreviewBlurredBitmap(scaledWidth, scaledHeight, blurRadius, surfaceOnly);
    }

    public Bitmap getCurPreviewBlurredBitmap(int scaledWidth, int scaledHeight, int blurRadius, boolean surfaceOnly, boolean isFinishWithBackkey) {
        return this.mGet.getCurPreviewBlurredBitmap(scaledWidth, scaledHeight, blurRadius, surfaceOnly, isFinishWithBackkey);
    }

    public long getSavingQueueCount() {
        return this.mGet.getMediaSaveService() == null ? 0 : (long) this.mGet.getMediaSaveService().getQueueCount();
    }

    protected void readyForGestureview() {
        setCaptureButtonEnable(false, getShutterButtonType());
    }

    public void handleTouchModeChanged(boolean isInTouchMode) {
        CamLog.m3d(CameraConstants.TAG, "handleTouchModeChanged : " + isInTouchMode);
        if (!isInTouchMode) {
            if (isPostviewShowing()) {
                this.mPostviewManager.requestPostViewButtonFocus();
            } else {
                requestFocusOnShutterButton(true);
            }
        }
    }

    public boolean isCaptureCompleted() {
        CamLog.m7i(CameraConstants.TAG, "mInCaptureProgress " + this.mSnapShotChecker.getSnapShotState());
        if (checkModuleValidate(48) && !this.mSnapShotChecker.checkMultiShotState(6) && !isAnimationShowing()) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "Capture should be completed, return.");
        return false;
    }

    public int getDualviewType() {
        return 2;
    }

    protected int getLongshutterIntervalCnt() {
        return 0;
    }

    protected void restoreSteadyCamSetting() {
        if (!this.mBinningManager.isBinningEnabled()) {
            if (isAvailableSteadyCam()) {
                restoreSettingValue(Setting.KEY_VIDEO_STEADY);
                setSettingMenuEnable(Setting.KEY_VIDEO_STEADY, true);
                return;
            }
            setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        }
    }

    protected void restoreTrackingAFSetting() {
        CamLog.m3d(CameraConstants.TAG, "restoreTrackingAFSetting");
        if ((getCameraId() == 2 && FunctionProperties.getCameraTypeRear() == 1) || ((isUHDmode() && isRecordingPriorityMode()) || isVideoCaptureMode())) {
            setSpecificSettingValueAndDisable("tracking-af", "off", false);
        } else if (getCameraId() == 0) {
            restoreSettingValue("tracking-af");
            setSettingMenuEnable("tracking-af", true);
        }
        CamLog.m3d(CameraConstants.TAG, "getSettingValue(Setting.KEY_TRACKING_AF) : " + getSettingValue("tracking-af"));
    }

    public void notifyNewMediaFromVideoTrim(Uri uri) {
    }

    public int getSnapFixedDegree() {
        return -1;
    }

    public boolean isOutfocusAvailable() {
        return false;
    }

    public int getOutfocusErrorTextHeight() {
        return 0;
    }

    public boolean isLoopbackAvailable() {
        return false;
    }

    protected void cancelQuickShotTaking() {
        if (AppControlUtil.isNeedQuickShotTaking()) {
            CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity()).setParamToBackup(CameraConstants.QUICK_SHOT_KEY, Integer.valueOf(0));
            AppControlUtil.setNeedQuickShotTaking(false);
            if (this.mFocusManager != null) {
                this.mFocusManager.registerCallback();
            }
            if (this.mReviewThumbnailManager != null) {
                this.mReviewThumbnailManager.updateRecentContent(false);
            }
        }
    }

    public boolean checkQuickButtonAvailable() {
        if (this.mCameraState < 1 || this.mCameraState == 8 || this.mCameraState == 9 || this.mCameraState == 5) {
            CamLog.m3d(CameraConstants.TAG, "return because camera state : " + this.mCameraState);
            return false;
        } else if (this.mZoomManager == null || this.mZoomManager.isZoomControllersMoving() || this.mZoomManager.isJogZoomMoving()) {
            CamLog.m3d(CameraConstants.TAG, "Zoom controller's moving or Jog Zoom is working, return");
            return false;
        } else if (this.mBarManager != null && this.mBarManager.isBarTouching()) {
            CamLog.m3d(CameraConstants.TAG, "Bar's touched, return");
            return false;
        } else if (this.mIsSwitchingCameraDuringRecording || !this.mGet.checkModuleValidate(8)) {
            CamLog.m3d(CameraConstants.TAG, "mIsSwitchingCameraDuringRecording or camera switching return");
            return false;
        } else if (this.mDoubleCameraManager != null && this.mDoubleCameraManager.isAngleButtonPressed()) {
            CamLog.m3d(CameraConstants.TAG, "Double camera button pressed, return");
            return false;
        } else if (this.mSnapShotChecker != null && this.mSnapShotChecker.checkMultiShotState(1)) {
            CamLog.m3d(CameraConstants.TAG, "Multishot is in progress, return");
            return false;
        } else if (this.mGet.getPreviewCoverVisibility() == 0) {
            CamLog.m3d(CameraConstants.TAG, "Preview cover is showing, return");
            return false;
        } else if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.checkBlockingButtonState()) {
            CamLog.m3d(CameraConstants.TAG, "Film engine is starting or releasing, return");
            return false;
        } else if (this.mIsGoingToPostview) {
            CamLog.m3d(CameraConstants.TAG, "going to postview, return");
            return false;
        } else if (!this.mBinningManager.isBinningSettingProcessing()) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "binning setting, return");
            return false;
        }
    }

    public void onFaceDetection(FaceCommon[] faces) {
    }

    public boolean isStartedFromQuickCover() {
        return this.mGet.isStartedFromQuickCover();
    }

    protected void onFilmEngineReleased(boolean isRestartPreview, boolean isStopByRecording) {
        this.mParamUpdater.setParamValue(ParamConstants.KEY_FILM_ENABLE, "false");
        stopPreview();
        if (!getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    int[] lcdSize = Utils.getLCDsize(DefaultCameraModule.this.getAppContext(), true);
                    DefaultCameraModule.this.mGet.getCurPreviewBitmap((int) (((float) lcdSize[1]) * 0.5f), (int) (((float) lcdSize[0]) * 0.5f));
                    DefaultCameraModule.this.mGet.setPreviewCoverVisibility(0, false, null, true, true);
                }
            });
        }
        if (FunctionProperties.isAppyingFilmLimitation()) {
            restoreSettingValue("hdr-mode");
            setSettingMenuEnable("hdr-mode", true);
            this.mParamUpdater.setParamValue("hdr-mode", this.mGet.getCurSettingValue("hdr-mode"));
            if ("1".equals(getSettingValue("hdr-mode"))) {
                setSpecificSettingValueAndDisable("flash-mode", "off", false);
                this.mParamUpdater.setParamValue("flash-mode", "off");
                setFlashMetaDataCallback("off");
            }
        }
        if (isStopByRecording) {
            setCameraState(5);
        }
        if (this.mStickerManager != null && (this.mStickerManager.isRunning() || this.mStickerManager.isWaitOneShot())) {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, this.mStickerManager.getSupportedPreviewSize());
        } else if (isStopByRecording || this.mGet.isVideoCaptureMode() || ManualUtil.isManualVideoMode(getShotMode())) {
            ListPreference listPref = getListPreference(getVideoSizeSettingKey());
            if (listPref != null) {
                if (listPref.getValue() != null) {
                    this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, getPreviewSize(listPref.getExtraInfo(1), listPref.getExtraInfo(2), listPref.getValue()));
                } else {
                    return;
                }
            }
        } else {
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId)).getExtraInfo(1));
        }
        startPreview(null, null);
        setCameraState(1);
        if (this.mFocusManager != null && isFocusEnableCondition() && !isStopByRecording && getFocusState() != 14) {
            this.mFocusManager.registerCallback();
        }
    }

    public void resumeShutterless() {
    }

    public void pauseShutterless() {
    }

    public Rect getRealFocusWindow(Rect mFocusRect, int mFocusAreaWidth, int mFocusAreaHeight, int x, int y) {
        return mFocusRect;
    }

    public void closeNetworkCamera() {
    }

    public boolean checkInterval(int checkType) {
        return this.mIntervalChecker != null && this.mIntervalChecker.checkTimeInterval(checkType);
    }

    protected void showLocationToast() {
        Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.fail_to_get_location_permission), 1).show();
    }

    public boolean isCameraChanging() {
        if (this.mGet == null) {
            return false;
        }
        return this.mGet.isCameraChanging();
    }

    public String getCurrentSelectedVideoSize() {
        return getSettingValue(Setting.KEY_VIDEO_RECORDSIZE);
    }

    public String getCurrentSelectedPreviewSize() {
        return this.mParamUpdater.getParamValue(ParamConstants.KEY_PREVIEW_SIZE);
    }

    public String getCurrentSelectedPictureSize() {
        return getSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
    }

    public void setTrackingFocusState(boolean enable) {
        if (this.mFocusManager != null) {
            this.mFocusManager.setTrackingFocusState(enable);
        }
    }

    protected boolean isFastShotAvailable(int checkItem) {
        if (!isFastShotSupported()) {
            return false;
        }
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            return false;
        }
        if ((this.mLivePhotoManager != null && this.mLivePhotoManager.isLivePhotoEnabled()) || this.mCameraState == 5 || this.mCameraState == 8 || this.mCameraState == 6 || this.mCameraState == 7 || this.mCameraState == 9) {
            return false;
        }
        return this.mSnapShotChecker.isFastShotAvailable(checkItem);
    }

    public void removeUIBeforeModeChange() {
        hideMenu(PointerIconCompat.TYPE_GRABBING, true, true);
        showDoubleCamera(false);
        if (this.mFocusManager != null) {
            this.mFocusManager.hideAllFocus();
        }
        showFrameGridView("off", false);
    }

    public int getPreviewCoverVisibility() {
        return this.mGet.getPreviewCoverVisibility();
    }

    public boolean getGestureVisibility() {
        if (this.mGestureShutterManager == null) {
            return false;
        }
        return this.mGestureShutterManager.getGesutreGuideVisibility();
    }

    public boolean isActivatedQuickdetailView() {
        return this.mGet.isActivatedQuickdetailView();
    }

    public void setDeleteButtonVisibility(boolean visibility) {
        this.mGet.setDeleteButtonVisibility(visibility);
    }

    public boolean isActivatedTilePreview() {
        return this.mGet.isActivatedTilePreview();
    }

    public boolean isSelfieOptionVisible() {
        if (this.mAdvancedFilmManager == null) {
            return false;
        }
        return this.mAdvancedFilmManager.isSelfieOptionVisible();
    }

    public void stopMotionEngine() {
        if (!isRearCamera() && this.mGestureShutterManager != null) {
            this.mGestureShutterManager.stopMotionEngine();
        }
    }

    public SquareSnapGalleryItem getCurSquareSnapItem() {
        return null;
    }

    public boolean isSquareGalleryBtn() {
        return false;
    }

    public void setQuickShareAfterGifMaking() {
        if (isSupportedQuickClip() && this.mQuickClipManager != null) {
            this.mQuickClipManager.setAfterShot();
            this.mQuickClipManager.setIsVisible(true);
        }
    }

    public void initBeautyBar(int mSelectedMenuType) {
        CamLog.m3d(CameraConstants.TAG, "[relighting] mSelectedMenuType>" + mSelectedMenuType);
        BarView barView = null;
        if (mSelectedMenuType == 1) {
            this.mBarManager.initBar(1);
            barView = this.mBarManager.getBar(1);
        } else if (mSelectedMenuType == 3) {
            this.mBarManager.initBar(4);
            barView = this.mBarManager.getBar(4);
        }
        if (barView != null) {
            barView.refreshBar();
        }
    }

    public void doDeleteOnUndoDialog() {
        if (this.mUndoManager != null) {
            this.mUndoManager.doDeleteOnUndoDialog();
        }
    }

    public boolean stopBurstSaving() {
        if (this.mGet.getQueueCount() > 0) {
            return this.mGet.deleteAllImageSavers();
        }
        return true;
    }

    public void deleteOrUndo(Uri uri, String burstId, UndoInterface listener) {
        if (this.mUndoManager != null) {
            this.mUndoManager.deleteOrUndo(uri, burstId, listener);
        }
    }

    public boolean checkUndoCurrentState(int state) {
        if (this.mUndoManager != null) {
            return this.mUndoManager.checkCurrentState(state);
        }
        return false;
    }

    public void deleteImmediatelyNotUndo() {
        if (this.mUndoManager != null) {
            this.mUndoManager.deleteImmediately();
        }
    }

    public void restoreSelfieMenuVisibility() {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.restoreSelfieMenuVisibility();
        }
    }

    public int getQuickClipTopPosition() {
        if (this.mQuickClipManager != null) {
            return this.mQuickClipManager.getTopPosition();
        }
        return 0;
    }

    public boolean isGIFEncoding() {
        if (this.mGifManager == null) {
            return false;
        }
        return this.mGifManager.isGIFEncoding();
    }

    public void setSelfieOptionVisibility(boolean visible, boolean isRestart) {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setSelfieOptionVisibility(visible, isRestart);
        }
    }

    public void showTilePreview(boolean enable) {
        this.mGet.showTilePreview(enable);
    }

    public boolean isCollageProgressing() {
        return false;
    }

    public void doSelectInOverlapSampleDialog(int id) {
    }

    public void setPhotoSizeHelpShown(boolean photoSizeHelpShown) {
        this.mGet.setPhotoSizeHelpShown(photoSizeHelpShown);
    }

    public boolean getPhotoSizeHelpShown() {
        return this.mGet.getPhotoSizeHelpShown();
    }

    public void showInitDialog() {
        if (this.mInitGuideManager != null) {
            this.mInitGuideManager.showInitDialog();
        }
    }

    public void removeSpliceDimColor() {
    }

    public void bindSlomoSaveService(Intent intent) {
        this.mGet.bindSlomoSaveService(intent);
    }

    public void registerBroadcastReceiver(BroadcastReceiver receiver, IntentFilter intentfilter) {
        this.mGet.registerBroadcastReceiver(receiver, intentfilter);
    }

    public boolean isCinemaLUTVisible() {
        return false;
    }

    protected void configPreviewCallback() {
        this.mDirectCallbackManager.configPreviewCallback(this.mCameraDevice, 1);
    }

    protected void startPreview(CameraParameters params) {
        startPreview(params, null);
    }

    private void replacePreviewSizeForSticker() {
        if (FunctionProperties.isSupportedSticker() && isStickerSupportedCameraMode()) {
            String previewSize = (this.mStickerManager == null || !this.mStickerManager.hasSticker()) ? null : this.mStickerManager.getSupportedPreviewSize();
            if (previewSize == null && this.mStickerManager == null) {
                StickerGLSurfaceView gl = (StickerGLSurfaceView) this.mGet.findViewById(C0088R.id.preview_glsurfaceview);
                previewSize = (gl == null || !gl.hasSticker()) ? null : StickerManager.CalculatePreviewSize(getCurrentSelectedPictureSize());
            }
            if (previewSize != null) {
                CamLog.m3d("StickerManager", "replaced previewSize for sticker = " + previewSize);
                if (getBinningEnabledState()) {
                    this.mParamUpdater.setParamValue("picture-size", getCurrentSelectedPictureSize());
                    this.mParamUpdater.setParamValue(ParamConstants.KEY_BINNING_PARAM, "normal");
                    this.mParamUpdater.setParamValue(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_DEFAULT_STR);
                }
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, previewSize);
                settingForSticker(true);
                runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (DefaultCameraModule.this.mStickerManager != null) {
                            DefaultCameraModule.this.mStickerManager.doInStartPreview();
                        }
                    }
                });
            }
        }
    }

    protected void startPreview(CameraParameters params, SurfaceTexture surfaceTexture) {
        replacePreviewSizeForSticker();
        startPreview(params, surfaceTexture, getCameraState() == 5);
    }

    public void onStartQRCodeClicked() {
        if (this.mQRCodeManager != null && !this.mQRCodeManager.isRunningQRCode()) {
            CamLog.m3d(CameraConstants.TAG, "onStartQRCodeClicked");
            this.mQRCodeManager.startDetectQR();
        }
    }

    public void onStopQRCodeClicked() {
        if (this.mQRCodeManager != null && this.mQRCodeManager.isRunningQRCode()) {
            CamLog.m3d(CameraConstants.TAG, "onStopQRCodeClicked");
            this.mQRCodeManager.stopDetectQR();
        }
    }

    public void setFlashBarEnabled(boolean enabled) {
        if (this.mFlashControlManager != null) {
            this.mFlashControlManager.setFlashBarEnabled(enabled);
        }
    }

    public boolean isFlashBarPressed() {
        if (this.mFlashControlManager != null) {
            return this.mFlashControlManager.isFlashBarPressed();
        }
        return false;
    }

    public void setQRLayoutVisibility(int visibility) {
        if (this.mQRCodeManager != null) {
            this.mQRCodeManager.setQRLayoutVisibility(visibility);
        }
    }

    private void doOneShotPrevieCallbackActionForQR() {
        if (FunctionProperties.isSupportedQrCode(getAppContext()) && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR))) {
            setPreviewCallbackAll(true);
        }
    }

    public void deleteMode(ModeItem item) {
        if (this.mGet != null) {
            this.mGet.deleteMode(item);
        }
    }

    public void setFilmStrength(float filmStrength) {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.setFilmStrength(filmStrength);
        }
    }

    public int getFilmStrengthValue() {
        if (this.mAdvancedFilmManager == null) {
            return 100;
        }
        return this.mAdvancedFilmManager.getFilmStrengthValue();
    }

    public boolean isSupportedFilterMenu() {
        return true;
    }

    public void setManualFocus(boolean set) {
        if (("mode_normal".equals(getShotMode()) || this.mGet.isAttachIntent()) && this.mFocusManager != null) {
            CamLog.m3d(CameraConstants.TAG, "set manual focus - isSet : " + set);
            if (set) {
                this.mFocusManager.setManualFocus(true);
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DefaultCameraModule.this.setFocusPointVisibility(false);
                        DefaultCameraModule.this.mFocusManager.cancelTouchAutoFocus();
                        DefaultCameraModule.this.mFocusManager.hideAndCancelAllFocus(false);
                    }
                });
                setSetting("focus-mode", ParamConstants.FOCUS_MODE_MANUAL, false);
                return;
            }
            this.mFocusManager.setManualFocus(false);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DefaultCameraModule.this.setFocusPointVisibility(!"mode_normal".equals(DefaultCameraModule.this.getShotMode()));
                }
            });
            if (this.mCameraCapabilities != null && this.mCameraCapabilities.isAFSupported()) {
                String focusMode = "auto";
                if (isMWAFSupported()) {
                    focusMode = ParamConstants.FOCUS_MODE_MULTIWINDOWAF;
                }
                setSetting("focus-mode", focusMode, false);
                this.mParamUpdater.setParamValue("focus-mode", focusMode);
                setCameraFocusMode(focusMode);
                this.mFocusManager.registerCallback(true);
            }
        }
    }

    public void setFocusPointVisibility(boolean visible) {
        boolean z = false;
        if (this.mFocusManager != null) {
            if (!checkModuleValidate(192)) {
                this.mFocusManager.setAFPointVisible(false);
            } else if (!isZoomBarVisible() && !isJogZoomMoving()) {
                if (visible) {
                    FocusManager focusManager = this.mFocusManager;
                    if (!isManualFocusMode()) {
                        z = true;
                    }
                    focusManager.setAFPointVisible(z);
                    return;
                }
                this.mFocusManager.setAFPointVisible(false);
            }
        }
    }

    public void setManualFocusModeEx(boolean isSet) {
        this.mManualFocusManager.setManualFocusModeEx(isSet);
    }

    protected void updateManualFocusValueFromMetadataCallback(float currentMFStep) {
        this.mManualFocusManager.setManualDataMFValue(currentMFStep);
    }

    protected void setManualManualFocusMetaDataCallback(boolean bSet) {
        CamLog.m3d(CameraConstants.TAG, "setManualManualFocusMetaDataCallback = " + this.mCameraDevice + ", bSet : " + bSet);
        if (this.mCameraDevice != null && this.mManualFocusMetaDataCallback != null) {
            if (bSet) {
                this.mCameraDevice.setManualCameraMetadataCb(this.mManualFocusMetaDataCallback);
            } else {
                this.mCameraDevice.setManualCameraMetadataCb(null);
            }
        }
    }

    public void setManualFocusButtonVisibility(boolean show) {
        if (this.mFocusManager != null) {
            this.mFocusManager.setManualFocusButtonVisibility(show);
        }
    }

    public void setManualFocusVisibility(boolean show) {
        if (this.mManualFocusManager != null) {
            this.mManualFocusManager.setVisible(show);
        }
    }

    public boolean isAvailableManualFocus(boolean fromInitOpenCameraRoutine) {
        if (this.mManualFocusManager != null) {
            return this.mManualFocusManager.isAvailableManualFocus(fromInitOpenCameraRoutine);
        }
        return false;
    }

    public void setBinningSettings(boolean isBinningOn) {
        if (FunctionProperties.isSupportedBinning() && !isModuleChanging() && !isPaused() && this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                if (isBinningOn) {
                    setSpecificSettingValueAndDisable("flash-mode", "off", false);
                    setSpecificSettingValueAndDisable("hdr-mode", "0", false);
                    setParamUpdater(parameters, "hdr-mode", "0");
                    setParamUpdater(parameters, "flash-mode", "off");
                    setHDRMetaDataCallback(null);
                    setFlashMetaDataCallback(null);
                    setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
                    setParamUpdater(parameters, ParamConstants.KEY_STEADY_CAM, "off");
                    setParamUpdater(parameters, "picture-size", this.mBinningManager.getBinningPictureSize(this.mCameraId));
                    setParamUpdater(parameters, ParamConstants.KEY_BINNING_PARAM, ParamConstants.VALUE_BINNING_MODE);
                    setParamUpdater(parameters, ParamConstants.KEY_APP_OUTPUTS_TYPE, this.mBinningManager.getBinningCaptureOutputConfig());
                    setParamUpdater(parameters, ParamConstants.KEY_APP_BINNING_TYPE, getBinningType());
                } else {
                    restoreSettingValue("hdr-mode");
                    setSettingMenuEnable("hdr-mode", true);
                    String hdrValue = getSettingValue("hdr-mode");
                    setParamUpdater(parameters, "hdr-mode", hdrValue);
                    if ("2".equals(hdrValue)) {
                        setHDRMetaDataCallback(hdrValue);
                    }
                    if (!"1".equals(hdrValue)) {
                        restoreFlashSetting();
                        setFlashMetaDataCallback(getSettingValue("flash-mode"));
                        setParamUpdater(parameters, "flash-mode", getListPreference("flash-mode").loadSavedValue());
                        setSettingMenuEnable("flash-mode", true);
                    }
                    if (isAvailableSteadyCam()) {
                        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
                        setSettingMenuEnable(Setting.KEY_VIDEO_STEADY, true);
                        setParamUpdater(parameters, ParamConstants.KEY_STEADY_CAM, this.mGet.getCurSettingValue(Setting.KEY_VIDEO_STEADY));
                    }
                    setParamUpdater(parameters, "picture-size", getCurrentSelectedPictureSize());
                    setParamUpdater(parameters, ParamConstants.KEY_BINNING_PARAM, "normal");
                    setParamUpdater(parameters, ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_DEFAULT_STR);
                }
                this.mBinningRefreshCnt = 0;
                postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DefaultCameraModule.this.mQuickButtonManager.updateButton(C0088R.id.quick_button_flash);
                    }
                }, 150);
            }
        }
    }

    protected void resetToNormalSettingFromBinning() {
        if (getBinningEnabledState()) {
            restoreSettingValue("hdr-mode");
            if (!"1".equals(getSettingValue("hdr-mode"))) {
                restoreFlashSetting();
            }
            if (isAvailableSteadyCam()) {
                restoreSettingValue(Setting.KEY_VIDEO_STEADY);
                setSettingMenuEnable(Setting.KEY_VIDEO_STEADY, true);
            }
            this.mBinningManager.setBinningEnabled(false);
        }
    }

    protected void restoreBinningSetting() {
        boolean z = true;
        if (!FunctionProperties.isSupportedBinning()) {
            return;
        }
        if ((this.mStickerManager == null || !this.mStickerManager.isRunning()) && checkModuleValidate(192) && isBinningSupportedMode()) {
            if (FunctionProperties.isSupportedBinning(this.mCameraId)) {
                CamLog.m3d(CameraConstants.TAG, "[NightVision] restore binning setting - true");
                this.mBinningManager.showBinningIcon(this.mLowLightState, this.mSnapShotChecker.checkMultiShotState(7), 1);
                restoreSettingValue(Setting.KEY_BINNING);
                setSettingMenuEnable(Setting.KEY_BINNING, true);
                setBinningSettings(this.mBinningManager.isBinningEnabled());
            } else {
                CamLog.m3d(CameraConstants.TAG, "[NightVision] restore binning setting - false");
                setBinningSettings(false);
                this.mBinningManager.setBinningIconVisibility(false, 1);
                setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
            }
            setNightVisionDataCallback(FunctionProperties.isSupportedBinning(this.mCameraId));
            if ("off".equals(getSettingValue(Setting.KEY_BINNING))) {
                z = false;
            }
            setNightVisionParameters(z);
        }
    }

    protected void updateBinningStateFromMetadata(float currentLux) {
        if (isBinningSupportedMode() && !"off".equals(getSettingValue(Setting.KEY_BINNING))) {
            if ((!getShotMode().contains(CameraConstants.MODE_SMART_CAM) || "on".equals(getSettingValue(Setting.KEY_BINNING))) && !isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !isPaused() && !isAnimationShowing() && !isFocusLock() && !isAELock() && !this.mBinningManager.checkSnapshotProcForcely()) {
                if ((this.mStickerManager != null && this.mStickerManager.isRunning()) || !checkModuleValidate(192) || this.mGet.isActivatedQuickdetailView()) {
                    return;
                }
                if (!this.mBinningManager.checkAvailableChangeBinningMode()) {
                    CamLog.m3d(CameraConstants.TAG, "focusing, return");
                } else if (this.mBinningManager.isBinningSettingProcessing()) {
                    CamLog.m3d(CameraConstants.TAG, "binning setting, return");
                } else if (this.mIsBinningManualOff) {
                    this.mIsBinningManualOff = false;
                } else {
                    int prevState = this.mLowLightState;
                    if (this.mBinningManager.checkBinningOffManually()) {
                        if (currentLux >= BinningManager.LOW_LIGHT_10_LUX_NORMAL) {
                            this.mLowLightState = 3;
                        } else {
                            this.mLowLightState = 0;
                            this.mBinningManager.resetManuallyOffState();
                        }
                    } else if (currentLux >= BinningManager.LOW_LIGHT_3_LUX_NORMAL) {
                        if (getShotMode().contains(CameraConstants.MODE_SMART_CAM)) {
                            this.mLowLightState = 2;
                        } else {
                            this.mLowLightState = "on".equals(getSettingValue(Setting.KEY_BINNING)) ? 2 : 1;
                        }
                    } else if (currentLux >= BinningManager.LOW_LIGHT_10_LUX_NORMAL && currentLux < BinningManager.LOW_LIGHT_3_LUX_NORMAL && !this.mBinningManager.isBinningEnabled()) {
                        this.mLowLightState = getShotMode().contains(CameraConstants.MODE_SMART_CAM) ? 0 : 1;
                    } else if (currentLux < BinningManager.LOW_LIGHT_10_LUX_BINNING && this.mBinningManager.isBinningEnabled()) {
                        this.mLowLightState = 0;
                    } else if (currentLux < BinningManager.LOW_LIGHT_10_LUX_NORMAL && !this.mBinningManager.isBinningEnabled()) {
                        this.mLowLightState = 0;
                    }
                    if (this.mLowLightState != prevState) {
                        if (this.mLowLightState == 2 && !this.mBinningManager.isBinningEnabled()) {
                            showBinningToast(true);
                        } else if (this.mLowLightState == 0 && (prevState == 2 || this.mBinningManager.isBinningEnabled())) {
                            showBinningToast(false);
                        }
                    }
                    this.mBinningManager.showBinningIcon(this.mLowLightState, this.mSnapShotChecker.checkMultiShotState(7), 0);
                }
            }
        }
    }

    private void showBinningToast(boolean on) {
        if (isMenuShowing(CameraConstants.MENU_TYPE_ALL) || !FunctionProperties.isSupportedBinning(this.mCameraId)) {
            return;
        }
        if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator() && this.mAdvancedFilmManager.getFilmState() != 4) {
            return;
        }
        if (this.mShowBinningToastCondition) {
            final String toastMsg = this.mGet.getAppContext().getString(getBinningToastMsg(on, true));
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (DefaultCameraModule.this.mToastManager.isShowing()) {
                        DefaultCameraModule.this.mToastManager.hideAllToast();
                    }
                    DefaultCameraModule.this.mToastManager.showToastForcely(toastMsg);
                    DefaultCameraModule.this.mToastManager.hideToastForcely();
                }
            }, 100);
            return;
        }
        this.mShowBinningToastCondition = true;
        this.mPrevBinningState = -1;
    }

    protected void handleBinningIconUI(boolean isShow, final int whereFrom) {
        if (isShow) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DefaultCameraModule.this.setNightVisionDataCallback(true);
                }
            }, 300);
            if (!(this.mPrevBinningState == this.mLowLightState || this.mIsBinningSettingEnabled)) {
                this.mShowBinningToastCondition = false;
            }
            if (this.mIsBinningSettingEnabled) {
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        DefaultCameraModule.this.mIsBinningSettingEnabled = false;
                        DefaultCameraModule.this.mLowLightState = -1;
                    }
                }, 150);
                return;
            }
            return;
        }
        this.mPrevBinningState = this.mLowLightState;
        setNightVisionDataCallback(false);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                DefaultCameraModule.this.mBinningManager.setBinningIconVisibility(false, true, false, whereFrom);
            }
        }, 50);
    }

    public void setBinningManualOff() {
        this.mBinningRefreshCnt = 0;
        this.mLowLightState = 3;
        this.mIsBinningManualOff = true;
    }

    public void setPreviewForBinning(boolean isStop) {
        if (FunctionProperties.getSupportedHal() != 2) {
            return;
        }
        if (isStop) {
            stopPreview();
        } else {
            setupPreview(null);
        }
    }

    public String getBinningPictureSize() {
        if (this.mBinningManager != null) {
            return this.mBinningManager.getBinningPictureSize(this.mCameraId);
        }
        return ConfigurationResolutionUtilBase.sCAMERA_PICTURESIZE_BINNING_REAR_SUPPORTED_ITEMS[0];
    }

    public boolean getBinningEnabledState() {
        if (this.mBinningManager != null) {
            return this.mBinningManager.isBinningEnabled();
        }
        return false;
    }

    public void showSetBinningToastManually(boolean on) {
        String toastMsg = this.mGet.getAppContext().getString(getBinningToastMsg(on, false));
        if (this.mToastManager.isShowing()) {
            this.mToastManager.hideAllToast();
        }
        this.mToastManager.showToastForcely(toastMsg);
        this.mToastManager.hideToastForcely();
    }

    private int getBinningToastMsg(boolean isOn, boolean isAuto) {
        return isOn ? isAuto ? FunctionProperties.isUseSuperBright() ? C0088R.string.super_bright_camera_toast_auto_on : C0088R.string.bright_mode_toast_auto_on : FunctionProperties.isUseSuperBright() ? C0088R.string.super_bright_camera_toast_manual_on : C0088R.string.bright_mode_toast_manual_on : isAuto ? FunctionProperties.isUseSuperBright() ? C0088R.string.super_bright_camera_toast_auto_off : C0088R.string.bright_mode_toast_auto_off : FunctionProperties.isUseSuperBright() ? C0088R.string.super_bright_camera_toast_manual_off : C0088R.string.bright_mode_toast_manual_off;
    }

    public SnapShotChecker getSnapshotChecker() {
        return this.mSnapShotChecker;
    }

    public boolean isNightVisionGuideShown() {
        return this.mGet.isNightVisionGuideShown();
    }

    public void setNightVisionGuideShown() {
        this.mGet.setNightVisionGuideShown();
    }

    protected String getBinningType() {
        if (this.mBinningManager != null) {
            return this.mBinningManager.getBinningType();
        }
        return "none";
    }

    private void showNeedUpdateCamModule() {
        if (FunctionProperties.getSupportedHal() == 2 && ModelProperties.isUserDebugMode()) {
            int type = SharedPreferenceUtil.getSensorModuleType(getAppContext());
            CamLog.m3d(CameraConstants.TAG, "[ModuleType] from preference : " + type + ", from device : " + CameraDeviceUtils.sCameraModuleType);
            if (type == 1) {
                CamLog.m3d(CameraConstants.TAG, "[ModuleType] do not show update text");
                return;
            }
            TextView tv = (TextView) this.mGet.findViewById(C0088R.id.sensor_module_type_text);
            if (type == -1) {
                CamLog.m3d(CameraConstants.TAG, "[ModuleType] set to preference");
                SharedPreferenceUtil.setSensorModuleType(getAppContext(), CameraDeviceUtils.sCameraModuleType);
                type = SharedPreferenceUtil.getSensorModuleType(getAppContext());
                CamLog.m3d(CameraConstants.TAG, "[ModuleType] from preference : " + type + ", after set first");
            }
            if (type == 0) {
                CamLog.m3d(CameraConstants.TAG, "[ModuleType] show update text");
                tv.setVisibility(0);
            }
        }
    }

    public int getIntervalshotVisibiity() {
        if (this.mIntervalShotManager != null) {
            return this.mIntervalShotManager.getIntervalshotVisibiity();
        }
        return 4;
    }

    public void setGifEncoding(boolean isGifEncoding) {
        if (this.mGifManager != null) {
            this.mGifManager.setGifEncoding(isGifEncoding);
        }
    }

    protected void handlePreviewCallback() {
        if ("mode_normal".equals(getShotMode()) && !this.mPictureOrVideoSizeChanged) {
            boolean condition = "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR)) || (this.mStickerManager != null && this.mStickerManager.isGLSurfaceViewShowing());
            setPreviewCallbackAll(condition);
        }
    }

    protected boolean isFlashControlBarShowing() {
        return this.mFlashControlManager != null && this.mFlashControlManager.isFlashControlBarShowing();
    }
}
