package com.lge.camera.app;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import com.arcsoft.stickerlibrary.sticker.StickerConfiger;
import com.lge.app.MiniActivity;
import com.lge.app.permission.RequestPermissionsHelper;
import com.lge.camera.C0088R;
import com.lge.camera.app.ext.MultiViewFrame;
import com.lge.camera.components.CameraHybridView;
import com.lge.camera.components.CameraHybridView.CameraPreviewListener;
import com.lge.camera.components.CameraSwitchAnimationViewGL;
import com.lge.camera.components.ComponentInterface;
import com.lge.camera.components.HybridViewConfig;
import com.lge.camera.components.LayoutChangeNotifier.Listener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraCapabilities;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraHolder;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.file.FileNamer;
import com.lge.camera.file.ImageRegisterRequest;
import com.lge.camera.file.ImageSavers;
import com.lge.camera.file.ImageSavers.ImageSaverCallback;
import com.lge.camera.file.MediaSaveService;
import com.lge.camera.file.MediaSaveService.LocalBinder;
import com.lge.camera.file.SaveRequest;
import com.lge.camera.managers.ConeUIManagerBase;
import com.lge.camera.managers.QuickClipStatusManager;
import com.lge.camera.managers.UndoInterface;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.ModeItem;
import com.lge.camera.settings.Setting;
import com.lge.camera.systeminput.BroadCastReceiverManager;
import com.lge.camera.systeminput.ReceiverInterface;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AppControlUtilBase;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CamLog.TraceTag;
import com.lge.camera.util.CameraPermissionUiProvider;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.MemoryUtils;
import com.lge.camera.util.MiniViewUtil;
import com.lge.camera.util.PackageUtil;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.TimeIntervalChecker;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;
import java.util.ArrayList;
import java.util.HashMap;

public class CameraActivity extends ExpandActivity implements CameraPreviewListener, Listener, ReceiverInterface, ImageSaverCallback, ComponentInterface {
    static final int MEDIASERVER_LOCKING_TIME_MILLISECOND = 850;
    public static final int SLOMO_ERR_ALEADY_USING = -99;
    public static final int SLOMO_ERR_CNAS = -98;
    public static final int SLOMO_ERR_DURING_CALL = -96;
    public static final int SLOMO_ERR_NOT_ENOUGH = -94;
    public static final String SLOMO_ERR_TOKEN = "slomo_error_token";
    public static final int SLOMO_REPLYTO = 0;
    public static final int SLOMO_SAVE_CANCEL = -1;
    public static final int SLOMO_SAVE_DONE = 2;
    public static final int SLOMO_SAVE_ERR = -1;
    public static final int SLOMO_SAVE_PROGRESS = 1;
    public static final String SLOMO_SAVE_RESULT_PATH = "slomo_save_result_path";
    protected final int AI_SWITCH_ANI_DELAY = 200;
    private final int OUT_OF_SCREEN_MARGIN = 300;
    private final int SWITCH_ANI_HIDE_DELAY = 100;
    private boolean isScreenCapturing = false;
    protected BaseModule mAttachModule = null;
    private HandlerRunnable mAudioNoiseHandlerRunnable = null;
    protected String mBeforeMode = null;
    private boolean mCameraChangingOnFlashJumpCut = false;
    private boolean mCameraChangingOnSquareSnap = false;
    private boolean mCheckInitDone = false;
    public HandlerRunnable mCheckTextureViewRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (CameraActivity.this.checkModuleValidate(1)) {
                if (CameraActivity.this.isCalledByQVoice()) {
                    CameraActivity.this.setSetting(Setting.KEY_VOICESHUTTER, "on", true);
                }
                if (CameraActivity.this.mCurrentModule != null) {
                    CameraActivity.this.mCurrentModule.checkAndStartCamera();
                }
                CamLog.m3d(CameraConstants.TAG, "CheckTextureViewRunnable:onResumeAfter.");
                if (VideoRecorder.isInitialized()) {
                    VideoRecorder.release(null);
                    VideoRecorder.releaseSecondMediaRecorder();
                }
                CameraActivity.this.setPreviewVisibility(0);
                if (!(CameraActivity.this.mCurrentModule == null || CameraConstants.MODE_POPOUT_CAMERA.equals(CameraActivity.this.mCurrentModule.getShotMode()) || CameraConstants.MODE_DUAL_POP_CAMERA.equals(CameraActivity.this.getShotMode()) || !AppControlUtil.isStartFromOnCreate())) {
                    CameraActivity.this.setPreviewCoverVisibility(4, true);
                }
                CameraActivity.this.setReturnFromHelp(false);
                CameraActivity.this.onResumeAfterDone();
            }
        }
    };
    private ServiceConnection mConnection = new C02861();
    protected FrameLayout mContentsBaseView = null;
    private Bitmap mCurPreviewBmp = null;
    protected BaseModule mCurrentModule = null;
    private Uri mDeletedUri = null;
    private int mGraphyIndex = -1;
    private HandlerRunnable mHeadsetHandlerRunnable = null;
    private int mHeadsetState = 0;
    private HandlerRunnable mHideSwitchAnimationViewHandler = new HandlerRunnable(this) {
        public void handleRun() {
            if (CameraActivity.this.mSwitchAnimationView != null && CameraActivity.this.mSwitchAnimationView.getVisibility() == 0) {
                CameraActivity.this.mSwitchAnimationView.setVisibility(4);
                if (!CameraActivity.this.mIsPreviewCoverVisible) {
                    CameraActivity.this.setSwitchingAniViewParam(false);
                }
                CamLog.m3d(CameraConstants.TAG, "-ani- set animation view gone");
            }
            CameraActivity.this.setAnimationShowing(false);
            if (CameraActivity.this.mSwitchAnimationView != null) {
                CameraActivity.this.mSwitchAnimationView.resetAnimType();
            }
        }
    };
    protected CameraHybridView mHybridView = null;
    protected ImageSavers mImageSaver = null;
    protected boolean mIsNightVisionGuideShown = false;
    private boolean mIsPhotoSizeHelpShown = false;
    private boolean mIsPreviewCoverVisible = false;
    private boolean mIsSetPreviewBackground = false;
    private boolean mIsSetPreviewCaptureCover = false;
    protected boolean mIsStickerGuideShown = false;
    private Object mLock = new Object();
    protected MediaSaveService mMediaSaveService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private MiniActivity mMiniActivity = null;
    protected HashMap<String, String> mModuleClassNameMap = new HashMap();
    protected HashMap<String, BaseModule> mModuleMap = new HashMap();
    private int mOutofWindowDistance;
    private View mPreviewLayout = null;
    private BaseModule mRaMProcessedModule = null;
    protected BroadCastReceiverManager mReceiverManager = null;
    protected Bitmap mReviewThumbBmp = null;
    protected Uri mSavedUri = null;
    private Messenger mService;
    private ServiceConnection mSlomoConnection = new C02882();
    protected CameraSwitchAnimationViewGL mSwitchAnimationView = null;
    protected int mTextureState = 0;
    protected ConditionVariable mWaitPreviewBufferConfig = new ConditionVariable();

    /* renamed from: com.lge.camera.app.CameraActivity$1 */
    class C02861 implements ServiceConnection {
        C02861() {
        }

        public void onServiceDisconnected(ComponentName componentname) {
            if (CameraActivity.this.mMediaSaveService != null) {
                CameraActivity.this.mMediaSaveService.setQueueStatusListener(null);
                CameraActivity.this.mMediaSaveService = null;
            }
        }

        public void onServiceConnected(ComponentName componentname, IBinder ibinder) {
            CameraActivity.this.mMediaSaveService = ((LocalBinder) ibinder).getService();
            if (CameraActivity.this.mMediaSaveService != null && CameraActivity.this.mCurrentModule != null) {
                CameraActivity.this.mCurrentModule.onMediaSaveServiceConnected(CameraActivity.this.mMediaSaveService);
            }
        }
    }

    /* renamed from: com.lge.camera.app.CameraActivity$2 */
    class C02882 implements ServiceConnection {
        C02882() {
        }

        public void onServiceDisconnected(ComponentName componentname) {
            Log.d(CameraConstants.TAG, "[slomo_share] onServiceDisconnected");
            CameraActivity.this.mService = null;
            String toastMsg = CameraActivity.this.getString(C0088R.string.error_write_file);
            if (CameraActivity.this.mCurrentModule != null) {
                CameraActivity.this.mCurrentModule.showToast(toastMsg, CameraConstants.TOAST_LENGTH_SHORT);
                CameraActivity.this.mCurrentModule.showProgressBarDialog(false, 0);
            }
        }

        public void onServiceConnected(ComponentName componentname, IBinder service) {
            Log.d(CameraConstants.TAG, "[slomo_share] onServiceConnected");
            CameraActivity.this.mService = new Messenger(service);
            Message msg = Message.obtain(null, 0, 0, 0);
            try {
                msg.replyTo = CameraActivity.this.mMessenger;
                CameraActivity.this.mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    class IncomingHandler extends Handler {
        IncomingHandler() {
        }

        public void handleMessage(Message msg) {
            if (CameraActivity.this.mCurrentModule != null) {
                switch (msg.what) {
                    case -1:
                        CamLog.m5e(CameraConstants.TAG, "[slomo_share] error number : " + msg.arg1);
                        Bundle data = msg.getData();
                        if (msg.arg1 == -100) {
                            CamLog.m11w(CameraConstants.TAG, "[slomo_share] retry bind service");
                            CameraActivity.this.unbindSlomoSaveService();
                            return;
                        }
                        CameraActivity.this.mCurrentModule.showToast(CameraActivity.this.setSlomoErrorMessage(msg.arg1), CameraConstants.TOAST_LENGTH_SHORT);
                        CameraActivity.this.mCurrentModule.showProgressBarDialog(false, 0);
                        CameraActivity.this.mCurrentModule.setQuickClipIcon(false, true);
                        CameraActivity.this.unbindSlomoSaveService();
                        if (CameraActivity.this.mCurrentModule.mQuickClipManager != null && CameraActivity.this.mCurrentModule.mQuickClipManager.getSlomoBroadcastReceiver() != null) {
                            CameraActivity.this.unregisterBroadcastReceiver(CameraActivity.this.mCurrentModule.mQuickClipManager.getSlomoBroadcastReceiver());
                            return;
                        }
                        return;
                    case 1:
                        CamLog.m3d(CameraConstants.TAG, "[slomo_share] progress : " + msg.arg1);
                        if (!CameraActivity.this.mCurrentModule.isProgressDialogVisible()) {
                            CameraActivity.this.mCurrentModule.showProgressBarDialog(true, 0);
                        }
                        CameraActivity.this.mCurrentModule.setProgress(msg.arg1);
                        return;
                    case 2:
                        Uri resultPath = (Uri) msg.getData().getParcelable(CameraActivity.SLOMO_SAVE_RESULT_PATH);
                        CamLog.m3d(CameraConstants.TAG, "[slomo_share] save done result path : " + resultPath);
                        Toast.makeText(CameraActivity.this.getApplicationContext(), CameraActivity.this.getResources().getString(C0088R.string.slow_motion_share_complete_message), 0).show();
                        CameraActivity.this.mCurrentModule.showProgressBarDialog(false, 0);
                        if (CameraActivity.this.mCurrentModule.mQuickClipManager != null) {
                            CameraActivity.this.mCurrentModule.mQuickClipManager.setSharedUri(resultPath);
                            CameraActivity.this.mCurrentModule.mQuickClipManager.setSlomoSaveFile(false);
                            CameraActivity.this.mCurrentModule.mQuickClipManager.doClickQuickClip();
                        }
                        CameraActivity.this.unbindSlomoSaveService();
                        if (CameraActivity.this.mCurrentModule.mQuickClipManager != null && CameraActivity.this.mCurrentModule.mQuickClipManager.getSlomoBroadcastReceiver() != null) {
                            CameraActivity.this.unregisterBroadcastReceiver(CameraActivity.this.mCurrentModule.mQuickClipManager.getSlomoBroadcastReceiver());
                            return;
                        }
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        }
    }

    protected void setCaptureButtonEnableByNaviBar(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "[navibar] setCaptureButtonEnableByNaviBar enable = " + enable);
        if (ModelProperties.getCarrierCode() == 4 && !ModelProperties.isNavigationBarShowingModel() && this.mCurrentModule != null) {
            this.mCurrentModule.setCaptureButtonEnableByNaviBar(enable);
        }
    }

    public CameraActivity() {
        CamLog.traceBegin(TraceTag.MANDATORY, "StartCameraApp", 1000);
        sAPP_CAMERA_INSTANCE_COUNT++;
        CamLog.m7i(CameraConstants.TAG, "construct CAMERA app_instance_cnt = " + sAPP_CAMERA_INSTANCE_COUNT);
    }

    private void bindMediaSaveService() {
        getApplicationContext().bindService(new Intent(this, MediaSaveService.class), this.mConnection, 1);
    }

    private void unbindMediaSaveService() {
        if (this.mConnection != null) {
            try {
                getApplicationContext().unbindService(this.mConnection);
            } catch (Exception e) {
                CamLog.m5e(CameraConstants.TAG, "unbindeServie error : " + e);
            }
        }
    }

    private String setSlomoErrorMessage(int msg_arg) {
        String errorToken = getString(C0088R.string.error_write_file);
        switch (msg_arg) {
            case SLOMO_ERR_ALEADY_USING /*-99*/:
                errorToken = getString(C0088R.string.slowmotion_video_toast_already_use_QuickVideoEditor);
                break;
            case SLOMO_ERR_CNAS /*-98*/:
                errorToken = getString(C0088R.string.slowmotion_video_toast_internal_storage);
                break;
            case SLOMO_ERR_DURING_CALL /*-96*/:
                errorToken = getString(C0088R.string.slowmotion_video_toast_try_again);
                break;
            case SLOMO_ERR_NOT_ENOUGH /*-94*/:
                errorToken = getString(C0088R.string.slowmotion_video_toast_not_enough);
                break;
        }
        CamLog.m3d(CameraConstants.TAG, "[slomo_share] error Token : " + errorToken);
        return errorToken;
    }

    public void bindSlomoSaveService(Intent intent) {
        CamLog.m7i(CameraConstants.TAG, "[slomo_share] bind SlomoSaveService.");
        getApplicationContext().bindService(intent, this.mSlomoConnection, 1);
    }

    public void unbindSlomoSaveService() {
        if (this.mSlomoConnection != null) {
            CamLog.m7i(CameraConstants.TAG, "[slomo_share] Unbind SlomoSaveService.");
            try {
                getApplicationContext().unbindService(this.mSlomoConnection);
            } catch (Exception e) {
                CamLog.m5e(CameraConstants.TAG, "[slomo_share] unbindeServie error : " + e);
            }
        }
    }

    public void sendCancelNotiToSlomoSaveService() {
        Message msg = Message.obtain(null, -1, 0, 0);
        try {
            if (this.mService != null) {
                this.mService.send(msg);
            }
        } catch (Throwable e) {
            Log.w(CameraConstants.TAG, "Fail to send cancel message : ", e);
        }
    }

    public void registerBroadcastReceiver(BroadcastReceiver receiver, IntentFilter intentfilter) {
        CamLog.m7i(CameraConstants.TAG, "[slomo_share] registerBroadcastReceiver : rebind slomo save service");
        registerReceiver(receiver, intentfilter);
    }

    public void unregisterBroadcastReceiver(BroadcastReceiver receiver) {
        CamLog.m7i(CameraConstants.TAG, "[slomo_share] unregisterBroadcastReceiver : unregister broad cast for slomo save service");
        if (receiver != null) {
            unregisterReceiver(receiver);
            if (this.mCurrentModule != null && this.mCurrentModule.mQuickClipManager != null && this.mCurrentModule.mQuickClipManager.getSlomoBroadcastReceiver() != null) {
                this.mCurrentModule.mQuickClipManager.setSlomoBroadcastReceiver(null);
            }
        }
    }

    public MediaSaveService getMediaSaveService() {
        return this.mMediaSaveService;
    }

    public CameraActivity getActivity() {
        return this;
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onCreate - start");
        Utils.setDisplayValues(this);
        if (RequestPermissionsHelper.hasPermissions(this, CameraConstants.UI_REQUIRED_PERMISSIONS)) {
            this.mIsCheckingPermission = false;
            AppControlUtil.setStartFromOnCreate(true);
            AppControlUtil.setPopoutFirstTakePicture(false);
            ModelProperties.setLongLCDModel(Utils.getLCDsize(getAppContext(), true));
            CamLog.traceBegin(TraceTag.OPTIONAL, "UI_Initialize", 0);
            Log.i(CameraConstants.TAG, "[Time Info][2] Activity Start : UI Initialization" + DebugUtil.interimCheckTime(false));
            checkIntent(getIntent());
            Module.sFirstTaken = false;
            QuickClipStatusManager.resetStatus();
            super.onCreate(savedInstanceState);
            if (CheckStatusManager.getCheckEnterKind() != 0) {
                CamLog.m3d(CameraConstants.TAG, "return due to cannot enter camera.");
                return;
            }
            createBroadCastReceiver(1);
            if (!((!FunctionProperties.isSupportedMode(CameraConstants.MODE_MULTIVIEW) && !FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) || isAttachIntent() || isMMSIntent())) {
                MultiViewFrame.initializeStaticVariables(true);
            }
            if (!MDMUtil.allowMicrophone()) {
                final String toastMsg = getActivity().getString(FunctionProperties.isSupportedVoiceShutter() ? C0088R.string.block_recording_and_cheeseshutter_mdm : C0088R.string.block_recording_mdm);
                postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (toastMsg != null && CameraActivity.this.mCurrentModule != null) {
                            CameraActivity.this.mCurrentModule.showToast(toastMsg, CameraConstants.TOAST_LENGTH_SHORT);
                        }
                    }
                });
            }
            this.mOutofWindowDistance = Utils.getDefaultDisplayHeight(this) + 300;
            Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onCreate - end");
            return;
        }
        this.mIsCheckingPermission = true;
        super.onCreate(savedInstanceState);
    }

    public void setBackgroundPreviewLayout(boolean isSet) {
        if (this.mPreviewLayout != null && this.mIsSetPreviewBackground != isSet) {
            this.mIsSetPreviewBackground = isSet;
            if (isSet) {
                this.mPreviewLayout.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            } else {
                this.mPreviewLayout.setBackground(null);
            }
        }
    }

    public void onEnterAnimationComplete() {
        Log.i(CameraConstants.TAG, "onEnterAnimationComplete : Remove background color");
        super.onEnterAnimationComplete();
        setBackgroundPreviewLayout(false);
    }

    protected void checkIntent(Intent intent) {
    }

    public boolean setupModeForSmartTips() {
        if (!isSmartTipsIntent()) {
            return false;
        }
        String function = getIntent().getStringExtra(CameraConstants.CAMERA_FUNCTION);
        if (CameraConstants.TAP_SHOT_KEY.equals(function) || "mode_normal".equals(function) || CameraConstants.SWIPE_SWAP_VIEW_KEY.equals(function)) {
            this.mCurrentModule = getCreatedModule("mode_normal");
            setCurrentConeMode(1, true);
            return true;
        } else if (CameraConstants.MANUAL_KEY.equals(function)) {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_MANUAL_CAMERA);
            setCurrentConeMode(2, true);
            return true;
        } else if ("manual_video".equals(function)) {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_MANUAL_VIDEO);
            setCurrentConeMode(3, true);
            return true;
        } else if (CameraConstants.MULTIVIEW_KEY.equals(function)) {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_MULTIVIEW);
            setCurrentConeMode(1, true);
            return true;
        } else if (!CameraConstants.SNAP_VIDEO_KEY.equals(function)) {
            return false;
        } else {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SNAP);
            setCurrentConeMode(4, true);
            return true;
        }
    }

    protected void initModuleOnCreate() {
        CamLog.m3d(CameraConstants.TAG, "initModuleOnCreate : start");
        String mode = getInitCameraMode();
        createModule(mode, (String) this.mModuleClassNameMap.get(mode));
        selectModuleOnCreate();
        if (this.mCurrentModule != null) {
            createSettingManagers();
            if (this.mVoiceAssistantManager != null) {
                this.mVoiceAssistantManager.setSettingByAssistant();
            }
            CamLog.m7i(CameraConstants.TAG, "startCameraDeviceOnCreate");
            this.mCurrentModule.startCameraDeviceOnCreate();
            CamLog.traceBegin(TraceTag.OPTIONAL, "Prepare_View", 5);
            setContentView(C0088R.layout.camera_base);
            this.mPreviewLayout = findViewById(C0088R.id.preview_layout);
            setBackgroundPreviewLayout(true);
            showDummyCmdButtonsLayoutVisibility();
            this.mHybridView = new CameraHybridView(this);
            CamLog.m3d(CameraConstants.TAG, "-acq- initModuleOnCreate - mHybridView :: " + this.mHybridView);
            this.mHybridView.acquireHybridView(this.mCurrentModule.getClass());
            CamLog.m7i(CameraConstants.TAG, "mCurrentModule.init - s");
            this.mCurrentModule.init();
            CamLog.m7i(CameraConstants.TAG, "mCurrentModule.init - e");
            if (this.mSettingManager != null) {
                this.mSettingManager.removeUselessSetting();
            }
        }
        CamLog.m3d(CameraConstants.TAG, "initModuleOnCreate : end");
    }

    private void showDummyCmdButtonsLayoutVisibility() {
        int layoutId;
        String layoutName = "";
        if (!isAttachIntent()) {
            if (isEnteringDirectFromShortcut()) {
                layoutName = getShortcutDummyCmdButtonLayout();
            } else if (!isVoiceAssistantSpecified() || this.mVoiceAssistantManager == null) {
                layoutName = SharedPreferenceUtil.getCurrentCameraModeForStartingWindow(this);
            } else {
                layoutName = this.mVoiceAssistantManager.getAssistantDummyCmdButtonLayout();
            }
            layoutId = getResources().getIdentifier(layoutName, StickerConfiger.f72ID, "com.lge.camera");
        } else if (isVideoCaptureMode()) {
            layoutId = C0088R.id.attach_video_bg;
        } else {
            layoutId = C0088R.id.attach_camera_bg;
            if (!isRearCamera()) {
                View shutterZoomUi = findViewById(C0088R.id.attach_dummy_shutter_zoom_ui);
                if (shutterZoomUi != null) {
                    shutterZoomUi.setVisibility(8);
                }
            }
        }
        View rl = findViewById(layoutId);
        if (rl != null) {
            rl.setVisibility(0);
        }
    }

    protected String getShortcutDummyCmdButtonLayout() {
        return CameraConstants.START_CAMERA_NORMAL_VIEW_DUMMY;
    }

    private void hideDummyCmdButtonsLayoutVisibility() {
        int[] dummyLayoutId = new int[]{C0088R.id.auto_bg, C0088R.id.auto_shutter_zoom_bg, C0088R.id.attach_video_bg, C0088R.id.attach_camera_bg, C0088R.id.clean_bg, C0088R.id.manual_camera_bg, C0088R.id.manual_video_cinema_bg, C0088R.id.manual_video_bg};
        for (int findViewById : dummyLayoutId) {
            findViewById(findViewById).setVisibility(8);
        }
    }

    public void startPreviewDone(final boolean isStartedByInAndOutZoom) {
        CamLog.traceBegin(TraceTag.OPTIONAL, "UI_Initialize", 0);
        runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (!CameraActivity.this.checkModuleValidate(1)) {
                    return;
                }
                if (isStartedByInAndOutZoom) {
                    CameraActivity.this.mCurrentModule.startedCameraByInAndOutZoom();
                    return;
                }
                Log.i(CameraConstants.TAG, "[Time info][7] initialize layout - start ");
                CameraActivity.this.initUI();
                CameraActivity.this.mCurrentModule.initUIDone();
                CameraActivity.this.mCurrentModule.onResumeAfter();
                CameraActivity.this.setPhoneStateListener(true);
                CameraActivity.this.hideDummyCmdButtonsLayoutVisibility();
                if (CameraActivity.this.isModuleChanging()) {
                    CameraActivity.this.mCurrentModule.onChangeModuleAfter();
                }
                if (CameraActivity.this.mConeUIManager != null) {
                    CameraActivity.this.mConeUIManager.setConeModeChanged();
                }
                if (CameraActivity.this.mSwitchAnimationView != null && FunctionProperties.isSupportedSwitchingAnimation()) {
                    CameraActivity.this.mSwitchAnimationView.readyToShow();
                }
                if (AppControlUtil.isStartFromOnCreate()) {
                    AudioUtil.checkHeadsetState();
                    if (CameraActivity.this.mReceiverManager != null) {
                        CameraActivity.this.mReceiverManager.registerAllReceiver(CameraActivity.this, 2);
                    }
                }
                Log.i(CameraConstants.TAG, "[Time info][7] initialize layout - end ");
                CamLog.traceBegin(TraceTag.OPTIONAL, "UI_Initialize", 0);
            }
        });
    }

    private boolean isInflatedStubView() {
        return findViewById(C0088R.id.preview_cover_view) != null;
    }

    private void initUI() {
        if (checkModuleValidate(1) && this.mCurrentModule != null) {
            if (!isInflatedStubView()) {
                inflateStub(C0088R.id.stub_preview_overlay);
                inflateStub(C0088R.id.stub_app_view);
                inflateStub(C0088R.id.stub_controler_view);
                inflateStub(C0088R.id.stub_back_button);
                inflateStub(C0088R.id.stub_dot_page_indicator);
                inflateStub(C0088R.id.stub_full_screen_light_frame);
                this.mContentsBaseView = (FrameLayout) findViewById(C0088R.id.contents_base);
                if (FunctionProperties.isSupportedConeUI()) {
                    this.mConeUIManager = new ConeUIManagerBase(this);
                    if (this.mConeUIManager != null) {
                        this.mConeUIManager.init();
                        this.mConeUIManager.setRotateDegree(getOrientationDegree(), false);
                    }
                }
                if (FunctionProperties.isSupportedSwitchingAnimation()) {
                    this.mSwitchAnimationView = (CameraSwitchAnimationViewGL) findViewById(C0088R.id.preview_switch_anim_view);
                    this.mSwitchAnimationView.setComponentInterface(this);
                    setSwitchingAniViewParam(false);
                }
            }
            if (!this.mCheckInitDone) {
                this.mCurrentModule.initUI(this.mContentsBaseView);
            }
            if (this.mThumbnailListManager != null) {
                this.mThumbnailListManager.thumbnailListInit();
            }
            this.mCheckInitDone = true;
        }
    }

    protected void createSettingManagers() {
        super.createSettingManagers();
        if (isSmartTipsIntent()) {
            String function = getIntent().getStringExtra(CameraConstants.CAMERA_FUNCTION);
            if (function.equals(CameraConstants.GESTURE_KEY)) {
                setSetting(Setting.KEY_SWAP_CAMERA, "front", true);
                setupSetting();
            } else if (function.equals(CameraConstants.SWIPE_SWAP_VIEW_KEY) || function.equals(CameraConstants.TAP_SHOT_KEY)) {
                setSetting(Setting.KEY_SWAP_CAMERA, "rear", true);
                setupSetting();
                doCleanView(true, false, true);
            } else if (function.equals("mode_normal") || function.equals(CameraConstants.MANUAL_KEY) || function.equals("manual_video")) {
                setSetting(Setting.KEY_SWAP_CAMERA, "rear", true);
                setupSetting();
            }
        }
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            setSetting(Setting.KEY_MODE, CameraConstants.MODE_SQUARE_SNAPSHOT, false);
            setupSetting();
        }
        if (isFromGraphyApp()) {
            setSetting(Setting.KEY_SWAP_CAMERA, "rear", true);
            setupSetting();
        }
    }

    protected void doSmartTipsWorkAfterResume() {
        if (getIntent() != null && getIntent().getStringExtra(CameraConstants.CAMERA_FUNCTION) != null && isSmartTipsIntent()) {
            String function = getIntent().getStringExtra(CameraConstants.CAMERA_FUNCTION);
            if (function != null) {
                if (function.equals(CameraConstants.MANUAL_KEY)) {
                    setCurrentConeMode(2, false);
                } else if (function.equals("manual_video")) {
                    setCurrentConeMode(3, false);
                } else if (function.equals(CameraConstants.MULTIVIEW_KEY)) {
                    setCurrentConeMode(1, false);
                    setSetting(Setting.KEY_MODE, CameraConstants.MODE_MULTIVIEW, false);
                } else if (function.equals(CameraConstants.SNAP_VIDEO_KEY)) {
                    setCurrentConeMode(4, false);
                } else if (function.equals(CameraConstants.TAP_SHOT_KEY) || function.equals(CameraConstants.SWIPE_SWAP_VIEW_KEY) || function.equals(CameraConstants.GESTURE_KEY)) {
                    setSetting(Setting.KEY_MODE, "mode_normal", false);
                } else if (function.equals("mode_normal")) {
                    setCurrentConeMode(1, false);
                    setSetting(Setting.KEY_MODE, "mode_normal", false);
                }
            }
        }
    }

    protected void doAccessibilityWorkAfterResume() {
        if (ViewUtil.isAccessibilityServiceEnabled(getApplicationContext())) {
            enableConeMenuIcon(1, false);
        }
    }

    public void onStart() {
        Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onStart - start");
        super.onStart();
        if (!this.mIgnoreRequestPermissionsIfNeededCall && !this.mIsCheckingPermission) {
            this.mBeforeMode = null;
            if (!BoostService.isServiceLoaded()) {
                Intent startService = new Intent();
                startService.setClassName("com.lge.camera", "com.lge.camera.app.BoostService");
                startService(startService);
            }
            bindMediaSaveService();
            Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onStart - end");
        }
    }

    protected void initLaunchedFlag() {
        if (AppControlUtil.isGalleryLaunched()) {
            AppControlUtil.setLaunchingGallery(false);
        }
        if (AppControlUtilBase.isVideoLaunched()) {
            AppControlUtilBase.setLaunchingVideo(false);
        }
        if (AppControlUtilBase.isShareActivityLaunched()) {
            AppControlUtilBase.setLaunchingShareActivity(false);
        }
        if (AppControlUtilBase.isLGLensLaunched()) {
            AppControlUtilBase.setLaunchingLGLens(false);
        }
    }

    public void onResume() {
        Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onResume - start");
        if (!AppControlUtil.isStartFromOnCreate()) {
            Utils.setDisplayValues(this);
        }
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onResume();
            return;
        }
        this.mIsCallAnswering = false;
        setBackgroundPreviewLayout(true);
        initLaunchedFlag();
        if (FunctionProperties.isSupportedConeUI()) {
            getMiniActivity().show();
        }
        if (!(this.mCurrentModule == null || (isMMSIntent() && isVideoCaptureMode()))) {
            this.mCurrentModule.setPreviewLayoutParam();
            if (FunctionProperties.getSupportedHal() == 2) {
                int[] previewSize = getpreviewSize(getShotMode());
                if (previewSize != null && previewSize.length == 2) {
                    setCameraPreviewSize(previewSize[0], previewSize[1]);
                }
            }
        }
        this.mResumeAfterProcessingDone = false;
        this.mPaused = false;
        if (CheckStatusManager.getCheckEnterOutSecure() == 0 && !CheckStatusManager.checkEnterApplication(this, true)) {
            CamLog.m3d(CameraConstants.TAG, "onResume()-end, checkEnterApplication");
            if (SecureImageUtil.isSecureCameraIntent(getIntent())) {
                CheckStatusManager.setCheckEnterOutSecure(1);
            } else {
                super.onResume();
                CheckStatusManager.checkCameraOut(this, null, true);
                return;
            }
        }
        SecureImageUtil.setSecureCamera(this);
        this.mImageSaver = new ImageSavers(this, 99);
        if (!AppControlUtil.isStartFromOnCreate() || (FunctionProperties.isSupportedConeUI() && TelephonyUtil.phoneInCall(getAppContext()))) {
            changeModuleOnResume();
        }
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onResumeBefore();
        }
        super.onResume();
        postOnUiThread(this.mCheckTextureViewRunnable, 150);
        doAccessibilityWorkAfterResume();
        doSmartTipsWorkAfterResume();
        if (this.mSwitchAnimationView != null) {
            this.mSwitchAnimationView.onResume();
        }
        onResumeJustAfter();
        Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onResume - end");
    }

    private void onResumeJustAfter() {
        if (this.mConeUIManager != null) {
            this.mConeUIManager.init();
            this.mConeUIManager.setRotateDegree(getOrientationDegree(), false);
        }
        if (this.mCurrentModule != null) {
            String shotMode = this.mCurrentModule.getShotMode();
            if (shotMode != null) {
                if (isFromGraphyApp()) {
                    setSetting(Setting.KEY_MODE, CameraConstants.MODE_MANUAL_CAMERA, false);
                }
                if (shotMode.contains(CameraConstants.MODE_SQUARE) || "mode_normal".equals(shotMode) || shotMode.contains(CameraConstants.MODE_BEAUTY)) {
                    postOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            if (CameraActivity.this.mCurrentModule != null) {
                                CameraActivity.this.mCurrentModule.onResumeJustAfter();
                            }
                        }
                    });
                }
            }
        }
    }

    private boolean checkNeedModuleChange(int viewMode, String shotMode) {
        boolean isNeedChange = false;
        if (shotMode == null) {
            return false;
        }
        switch (viewMode) {
            case 1:
                if (!ManualUtil.isManualCameraMode(shotMode) && !ManualUtil.isManualVideoMode(shotMode) && !shotMode.contains(CameraConstants.MODE_SQUARE)) {
                    isNeedChange = false;
                    break;
                }
                isNeedChange = true;
                break;
                break;
            case 2:
                if (!CameraConstants.MODE_MANUAL_CAMERA.equals(shotMode)) {
                    isNeedChange = true;
                    break;
                }
                isNeedChange = false;
                break;
            case 3:
                if (!CameraConstants.MODE_MANUAL_VIDEO.equals(shotMode)) {
                    isNeedChange = true;
                    break;
                }
                isNeedChange = false;
                break;
            case 4:
                if (!CameraConstants.MODE_SNAP.equals(shotMode)) {
                    isNeedChange = true;
                    break;
                }
                isNeedChange = false;
                break;
        }
        CamLog.m3d(CameraConstants.TAG, "checkNeedModuleChange " + isNeedChange);
        return isNeedChange;
    }

    public MiniActivity getMiniActivity() {
        if (this.mMiniActivity == null) {
            this.mMiniActivity = new MiniActivity(this);
            this.mMiniActivity.setContentView(C0088R.layout.mini_activity_layout);
            this.mMiniActivity.setFixOrientation(true);
            this.mMiniActivity.setWindowType(4);
        }
        return this.mMiniActivity;
    }

    private void clearMiniActivity() {
        boolean isCameraActiviy = "com.lge.camera".equals(PackageUtil.getPackageName(AppControlUtil.getTopActivity(getAppContext())));
        if (!(!FunctionProperties.isSupportedConeUI() || this.mMiniActivity == null || isCameraActiviy)) {
            this.mMiniActivity.hide();
        }
        if (this.mMiniActivity != null) {
            this.mMiniActivity.dismiss();
            this.mMiniActivity = null;
        }
    }

    public void onPause() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onPause();
            return;
        }
        if (SecureImageUtil.isSecureCameraIntent(getIntent())) {
            CamLog.m3d(CameraConstants.TAG, "secure camera is exiting...");
            PowerManager pm = (PowerManager) getSystemService("power");
            if (pm != null) {
                CamLog.m3d(CameraConstants.TAG, "isScreenOn : " + pm.isScreenOn());
                if (!pm.isScreenOn()) {
                    finish();
                }
            }
        }
        CamLog.m3d(CameraConstants.TAG, "CameraActivity : onPause");
        this.mPaused = true;
        this.mResumeAfterProcessingDone = false;
        removePostRunnable(this.mCheckTextureViewRunnable);
        if (this.mCurrentModule != null) {
            this.mCurrentModule.stopBurstShotTaking(false);
        }
        ViewUtil.setPatialUpdate(this, true);
        finishImageSaver();
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onPauseBefore();
        }
        if (isActivatedQuickdetailView()) {
            removeStopPreviewMessage();
        }
        super.onPause();
        this.mWaitPreviewBufferConfig.open();
        onPauseAfter();
        MiniViewUtil.unbind();
        this.mIsCallAnswering = false;
        saveStartingWindowLayout(1);
        TalkBackUtil.setCameraTypeAnnounced(false);
        if (FunctionProperties.getSupportedHal() == 2 && !AppControlUtil.isGalleryLaunched()) {
            setPreviewVisibility(4);
        }
    }

    private void onPauseAfter() {
        resetMultiview();
        boolean isQuickViewActivate = false;
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onPauseAfter();
            isQuickViewActivate = this.mCurrentModule.isActivatedQuickview();
        }
        FileNamer.get().close(getApplicationContext(), 0);
        if (this.mReviewThumbBmp != null) {
            this.mReviewThumbBmp.recycle();
            this.mReviewThumbBmp = null;
        }
        if (this.mHybridView != null) {
            this.mHybridView.setAcquired(false);
            if (!AppControlUtil.isGalleryLaunched() || isQuickViewActivate || SecureImageUtil.isSecureCamera()) {
                switchAnimationEnd(false);
            }
        }
        if (this.mSwitchAnimationView != null) {
            this.mSwitchAnimationView.onPause();
        }
        this.mCameraChanging = 0;
        if (isSmartTipsIntent()) {
            getIntent().putExtra(CameraConstants.CAMERA_FUNCTION, "");
            setSmartTipsIntent(false);
        }
        if (FunctionProperties.isSupportedConeUI()) {
            getMiniActivity().hide();
        }
        if (AppControlUtilBase.isShareActivityLaunched() || SecureImageUtil.isSecureCamera() || AppControlUtilBase.isLGLensLaunched()) {
            setPreviewCoverVisibility(0, true);
        }
    }

    public void setStartingWindowTheme(String type) {
        CamLog.m3d(CameraConstants.TAG, "setStartingWindowTheme : " + type);
        ComponentName compName = new ComponentName("com.lge.camera", getClass().getSimpleName().trim());
        Window win = getWindow();
        try {
            win.getClass().getDeclaredMethod("setStartingWindowTheme", new Class[]{ComponentName.class, String.class}).invoke(win, new Object[]{compName, type});
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "setStartingWindowTheme invoke error : ", e);
        }
    }

    public void saveStartingWindowLayout(int mode) {
        CamLog.m3d(CameraConstants.TAG, "saveStartingWindowLayout getCurrentViewMode : " + mode);
        String appWindow = "";
        String localLayout = "";
        if (!isRearCamera()) {
            appWindow = CameraConstants.START_CAMERA_NORMAL_VIEW;
            localLayout = CameraConstants.START_CAMERA_NORMAL_VIEW_DUMMY;
        } else if (mode == 1) {
            appWindow = CameraConstants.START_CAMERA_NORMAL_SHUTTER_ZOOM_VIEW;
            localLayout = CameraConstants.START_CAMERA_NORMAL_SHUTTER_ZOOM_VIEW_DUMMY;
        } else if (mode == 2) {
            appWindow = CameraConstants.START_CAMERA_MANUAL_CAMERA_VIEW;
            localLayout = CameraConstants.START_CAMERA_MANUAL_CAMERA_VIEW_DUMMY;
        } else if (mode != 3) {
            appWindow = CameraConstants.START_CAMERA_NORMAL_VIEW;
            localLayout = CameraConstants.START_CAMERA_NORMAL_VIEW_DUMMY;
        } else if (ManualUtil.isCinemaSize(getAppContext(), getCurSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE))) {
            appWindow = CameraConstants.START_CAMERA_MANUAL_VIDEO_CINEMA_VIEW;
            localLayout = CameraConstants.START_CAMERA_MANUAL_VIDEO_CINEMA_VIEW_DUMMY;
        } else {
            appWindow = CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW;
            localLayout = CameraConstants.START_CAMERA_MANUAL_VIDEO_VIEW_DUMMY;
        }
        SharedPreferenceUtilBase.saveCurrentCameraModeForStartingWindow(getAppContext(), localLayout);
        setStartingWindowTheme(appWindow);
    }

    private void resetMultiview() {
        if (SecureImageUtil.isSecureCamera()) {
            MultiViewFrame.releaseFrame();
        }
    }

    public void onStop() {
        CamLog.m3d(CameraConstants.TAG, "CameraActivity : onStop");
        super.onStop();
        if (!this.mIgnoreRequestPermissionsIfNeededCall && !this.mIsCheckingPermission) {
            removePostRunnable(this.mCheckTextureViewRunnable);
            this.mBeforeMode = null;
            if (this.mCurrentModule != null) {
                this.mCurrentModule.onStop();
            }
            unbindMediaSaveService();
            if (FunctionProperties.isSupportedSwitchingAnimation()) {
                ColorUtil.destroyRS();
            }
            boolean isCameraActiviy = "com.lge.camera".equals(PackageUtil.getPackageName(AppControlUtil.getTopActivity(getAppContext())));
            if (FunctionProperties.isSupportedConeUI() && !isCameraActiviy) {
                getMiniActivity().hide();
            }
            switchAnimationEnd(false);
            setPreviewCoverVisibility(0, true);
            setPreviewVisibility(4);
        }
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "CameraActivity : onDestroy");
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onDestroy();
            return;
        }
        Module.sFirstTaken = false;
        releaseBroadCastReceiver();
        super.onDestroy();
        if (this.mModuleMap != null) {
            this.mModuleMap.clear();
            this.mModuleMap = null;
        }
        if (this.mModuleClassNameMap != null) {
            this.mModuleClassNameMap.clear();
            this.mModuleClassNameMap = null;
        }
        if (this.mHybridView != null) {
            this.mHybridView.unbind();
            this.mHybridView = null;
        }
        this.mContentsBaseView = null;
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onDestroy();
            this.mCurrentModule = null;
        }
        if (this.mAttachModule != null) {
            this.mAttachModule.onDestroy();
            this.mAttachModule = null;
        }
        this.mMediaSaveService = null;
        this.mSavedUri = null;
        this.mConnection = null;
        this.mContentsBaseView = null;
        releaseSwitchingAnimation();
        if (sAPP_CAMERA_INSTANCE_COUNT == 1) {
            MultiViewFrame.releaseFrame();
        }
        clearMiniActivity();
        QuickClipStatusManager.resetStatus();
        QuickClipUtil.resetQuickClipFakeMode();
        this.mIsNightVisionGuideShown = false;
        this.mIsStickerGuideShown = false;
        ProjectionMatrix.releaseMatrix();
        MemoryUtils.releaseViews(getWindow().getDecorView());
        System.gc();
    }

    public void releaseSwitchingAnimation() {
        if (!(this.mCurPreviewBmp == null || this.mCurPreviewBmp.isRecycled())) {
            this.mCurPreviewBmp.recycle();
        }
        this.mCurPreviewBmp = null;
        if (this.mSwitchAnimationView != null) {
            this.mSwitchAnimationView.unbind();
            this.mSwitchAnimationView = null;
        }
    }

    public void onConfigurationChanged(Configuration config) {
        Utils.setDisplayValues(this);
        this.mOutofWindowDistance = Utils.getDefaultDisplayHeight(this) + 300;
        CamLog.m7i(CameraConstants.TAG, "CameraActivitiy : onConfigurationChanged, config.Orientation" + config.orientation);
        super.onConfigurationChanged(config);
        if (!this.mPaused) {
            if (this.mCurrentModule != null) {
                this.mCurrentModule.onConfigurationChanged(config);
                if (this.mCurrentModule.isPostviewShowing()) {
                    return;
                }
            }
            if (this.mThumbnailListManager != null) {
                this.mThumbnailListManager.onConfigurationChanged(config);
            }
            onResumeAfterDone();
        }
    }

    protected void onNewIntent(Intent intent) {
        Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onNewIntent - start");
        Intent oldIntent = getIntent();
        setIntent(intent);
        setFlagsByIntentAction();
        if (isFromVolumeKey() || AppControlUtil.isQuickTools(getIntent())) {
            getWindow().addFlags(6291456);
            dismissKeyguard();
        }
        boolean isModuleChange = false;
        if (isVoiceAssistantSpecified() && this.mVoiceAssistantManager != null) {
            isModuleChange = true;
            this.mVoiceAssistantManager.handleVoiceAssistantOnNewIntent();
        }
        if (isModuleChange || !(oldIntent == null || oldIntent.filterEquals(getIntent()) || isGraphyIntent())) {
            this.mPaused = false;
            if (this.mUspZoneManager != null) {
                this.mUspZoneManager.updateUspIndex();
            }
            setupSetting();
            changeModule();
        }
        super.onNewIntent(intent);
        Log.i(CameraConstants.TAG, "[Time Info][UI] CameraActivity : onNewIntent - end");
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (this.mCurrentModule != null) {
            if (hasFocus && this.mCurrentModule.getCameraState() == 1) {
                this.mIsJustGetWindowFocus = true;
            } else {
                this.mIsJustGetWindowFocus = false;
            }
            this.mCurrentModule.onWindowFocusChanged(hasFocus);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onPrepareOptionsMenu(menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.onOptionsItemSelected(item);
        }
        return false;
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onUserInteraction();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        CamLog.m3d(CameraConstants.TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString(Setting.KEY_TIMER, getCurSettingValue(Setting.KEY_TIMER));
    }

    protected void onRestoreInstanceState(Bundle restore) {
        CamLog.m3d(CameraConstants.TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(restore);
        setSetting(Setting.KEY_TIMER, restore.getString(Setting.KEY_TIMER), false);
    }

    public int getSharedPreferenceCameraId() {
        return SharedPreferenceUtil.getCameraId(getAppContext());
    }

    public int getCurrentCameraId() {
        return getCameraId();
    }

    protected void makeModuleMap() {
        this.mModuleClassNameMap.put("mode_normal", "com.lge.camera.app.DefaultCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_BEAUTY, "com.lge.camera.app.BeautyShotCameraModule");
        if (FunctionProperties.isSupportedRearOutfocus()) {
            this.mModuleClassNameMap.put(CameraConstants.MODE_REAR_OUTFOCUS, "com.lge.camera.app.RearOutfocusModuleBase");
        }
        if (FunctionProperties.isSupportedFrontOutfocus()) {
            this.mModuleClassNameMap.put(CameraConstants.MODE_FRONT_OUTFOCUS, "com.lge.camera.app.FrontOutfocusModuleBase");
        }
        this.mModuleClassNameMap.put(CameraConstants.MODE_AKA_CAMERA, "com.lge.camera.app.AKACoverCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_QUICKCIRCLE_CAMERA, "com.lge.camera.app.QuickCircleCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_GESTURESHOT, "com.lge.camera.app.GestureShotCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_TIME_LAPSE_VIDEO, "com.lge.camera.app.ext.TimeLapseVideoModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SNAP, "com.lge.camera.app.ext.SnapMovieSingleCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SNAP_SINGLE, "com.lge.camera.app.ext.SnapMovieSingleCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SNAP_FRONT, "com.lge.camera.app.ext.SnapMovieFrontCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_MULTIVIEW, "com.lge.camera.app.ext.MultiViewCameraModuleExpand");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SLOW_MOTION, "com.lge.camera.app.ext.SlowMotionModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_POPOUT_CAMERA, ModelProperties.isMTKChipset() ? "com.lge.camera.app.ext.PopoutFrameModuleExpand" : "com.lge.camera.app.ext.PopoutShapeModule");
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_DUAL_POP_CAMERA)) {
            this.mModuleClassNameMap.put(CameraConstants.MODE_DUAL_POP_CAMERA, "com.lge.camera.app.ext.DualPopCameraModuleExpand");
        }
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
            this.mModuleClassNameMap.put(CameraConstants.MODE_MANUAL_CAMERA, "com.lge.camera.app.ManualDefaultCameraModule");
        }
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
            this.mModuleClassNameMap.put(CameraConstants.MODE_MANUAL_VIDEO, "com.lge.camera.app.ManualDefaultVideoModule");
        }
        this.mModuleClassNameMap.put("mode_food", "com.lge.camera.app.ext.FoodModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_PANORAMA_LG_360_PROJ, "com.lge.camera.app.ext.PanoramaModuleLG360Proj");
        this.mModuleClassNameMap.put(CameraConstants.MODE_FLASH_JUMPCUT, "com.lge.camera.app.ext.FlashJumpCutRearModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_FLASH_JUMPCUT_FRONT, "com.lge.camera.app.ext.FlashJumpCutFrontModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SQUARE_SNAPSHOT, "com.lge.camera.app.SquareSnapCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SQUARE_GRID, "com.lge.camera.app.ext.SquareGridCameraModuleExpand");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SQUARE_OVERLAP, "com.lge.camera.app.ext.SquareOverlapCameraModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SQUARE_SPLICE, "com.lge.camera.app.ext.SquareSpliceCameraModuleExpand");
        this.mModuleClassNameMap.put(CameraConstants.MODE_PANORAMA_LG, "com.lge.camera.app.ext.PanoramaModuleLGNormal");
        this.mModuleClassNameMap.put(CameraConstants.MODE_PANORAMA_LG_RAW, "com.lge.camera.app.ext.PanoramaModuleLGRaw");
        this.mModuleClassNameMap.put(CameraConstants.MODE_CINEMA, "com.lge.camera.app.ext.CinemaModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SMART_CAM, "com.lge.camera.app.ext.SmartCamModule");
        this.mModuleClassNameMap.put(CameraConstants.MODE_SMART_CAM_FRONT, "com.lge.camera.app.ext.SmartCamFrontModule");
    }

    private void createModule(String mode, String moduleClassName) {
        try {
            BaseModule module = (BaseModule) Class.forName(moduleClassName).getConstructor(new Class[]{ActivityBridge.class}).newInstance(new Object[]{this});
            if (this.mModuleMap != null) {
                this.mModuleMap.put(mode, module);
            }
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, "createModule error : " + e);
            CamLog.m5e(CameraConstants.TAG, "Mode : " + mode + ", moduleClassName : " + moduleClassName);
        }
    }

    protected boolean selectOtherModule() {
        String mode = getCurSettingValue(Setting.KEY_MODE);
        if (mode != null && isLGUOEMCameraIntent()) {
            if (!mode.equals("mode_normal")) {
                return false;
            }
            if (mode.equals("mode_normal") && this.mAttachModule != null) {
                this.mCurrentModule = this.mAttachModule;
                return true;
            }
        }
        if (isAttachIntent() || isMMSIntent()) {
            this.mCurrentModule = new AttachCameraModule(this);
            if (isLGUOEMCameraIntent()) {
                this.mAttachModule = this.mCurrentModule;
            }
            return true;
        } else if (!isVoiceAssistantSpecified() || this.mVoiceAssistantManager == null) {
            return false;
        } else {
            this.mCurrentModule = getCreatedModule(this.mVoiceAssistantManager.getAssistantInitMode());
            return true;
        }
    }

    protected void selectModuleOnCreate() {
        boolean isFrontCamera = true;
        if (!selectOtherModule() && this.mModuleMap != null && !setupModeForSmartTips()) {
            if (this.mModuleMap.containsKey("mode_normal") || this.mModuleMap.containsKey(CameraConstants.MODE_MANUAL_CAMERA) || this.mModuleMap.containsKey(CameraConstants.MODE_MANUAL_VIDEO) || this.mModuleMap.containsKey(CameraConstants.MODE_SQUARE_SNAPSHOT)) {
                if (!(SharedPreferenceUtil.getCameraId(getAppContext()) == 1 || (SharedPreferenceUtil.getCameraId(getAppContext()) == 2 && FunctionProperties.getCameraTypeFront() == 1))) {
                    isFrontCamera = false;
                }
                if (isFromGraphyApp()) {
                    this.mCurrentModule = (BaseModule) this.mModuleMap.get(CameraConstants.MODE_MANUAL_CAMERA);
                    return;
                } else if (FunctionProperties.isSupportedConeUI()) {
                    checkConeUiModuleCreation(isFrontCamera);
                    CamLog.m3d(CameraConstants.TAG, "Select Module of CONE onCreate : " + this.mCurrentModule.getShotMode());
                    return;
                } else {
                    if (isFrontCamera) {
                        if (FunctionProperties.isSupportedBeautyShot()) {
                            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_BEAUTY);
                            return;
                        } else if (FunctionProperties.isSupportedGestureShot()) {
                            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_GESTURESHOT);
                            return;
                        }
                    }
                    this.mCurrentModule = (BaseModule) this.mModuleMap.get("mode_normal");
                    CamLog.m3d(CameraConstants.TAG, "Select Module onCreate : " + this.mCurrentModule.getShotMode());
                    return;
                }
            }
            CamLog.m5e(CameraConstants.TAG, "It does not exist selected Module in onCreate");
        }
    }

    private void checkConeUiModuleCreation(boolean isFrontCamera) {
        switch (SharedPreferenceUtil.getLastCameraMode(this, 1)) {
            case 1:
                if (isFrontCamera) {
                    if (FunctionProperties.isSupportedBeautyShot()) {
                        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_BEAUTY);
                        return;
                    } else if (FunctionProperties.isSupportedGestureShot()) {
                        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_GESTURESHOT);
                        return;
                    }
                }
                break;
            case 2:
                this.mCurrentModule = (BaseModule) this.mModuleMap.get(CameraConstants.MODE_MANUAL_CAMERA);
                return;
            case 3:
                this.mCurrentModule = (BaseModule) this.mModuleMap.get(CameraConstants.MODE_MANUAL_VIDEO);
                return;
            case 4:
                if (!AppControlUtil.isQuickShotMode()) {
                    if (isFrontCamera) {
                        this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SNAP_FRONT);
                        return;
                    }
                    AppControlUtil.setSnapMovieMVFrameType(0);
                    this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SNAP_SINGLE);
                    return;
                }
                break;
        }
        this.mCurrentModule = (BaseModule) this.mModuleMap.get("mode_normal");
    }

    private void createSpecificModule(String mode) {
        if (this.mModuleMap != null && this.mModuleClassNameMap != null && !this.mModuleMap.containsKey(mode)) {
            createModule(mode, (String) this.mModuleClassNameMap.get(mode));
        }
    }

    protected BaseModule getCreatedModule(String mode) {
        if (this.mModuleMap == null) {
            return null;
        }
        createSpecificModule(mode);
        return (BaseModule) this.mModuleMap.get(mode);
    }

    protected void selectModule() {
        if (!selectOtherModule()) {
            if (this.mModuleMap == null) {
                CamLog.m5e(CameraConstants.TAG, "Module Map is empty, create default module;");
                this.mCurrentModule = new DefaultCameraModule(this);
                return;
            }
            String mode = getCurSettingValue(Setting.KEY_MODE);
            if (FunctionProperties.isSupportedConeUI()) {
                mode = selectConeMode(mode);
            }
            if (isCameraChangingOnSnap() && CameraConstants.MODE_SNAP.equals(getBeforeMode())) {
                mode = CameraConstants.MODE_SNAP;
                setCurrentConeMode(4, true);
                this.mSettingManager.setSetting(Setting.KEY_MODE, CameraConstants.MODE_SNAP, false);
            }
            if (this.mModeMenuManager.isDownloadableMode(mode) && this.mModeMenuManager.checkDownloadedMode(mode)) {
                CamLog.m3d(CameraConstants.TAG, "[mode] " + mode + " is deleted. change to Auto");
                mode = "mode_normal";
                setSetting(Setting.KEY_MODE, "mode_normal", false);
            }
            if (isCameraChangingOnFlashJumpCut() && CameraConstants.MODE_FLASH_JUMPCUT.equals(getBeforeMode())) {
                mode = CameraConstants.MODE_FLASH_JUMPCUT;
                this.mSettingManager.setSetting(Setting.KEY_MODE, CameraConstants.MODE_FLASH_JUMPCUT, false);
            }
            createSpecificModule(mode);
            if (this.mModuleMap.containsKey(mode)) {
                setCurrentModule(mode);
                CamLog.m3d(CameraConstants.TAG, "###Select Module : " + mode);
            }
        }
    }

    private String selectConeMode(String curMode) {
        int currentConeMode = getCurrentConeMode();
        CamLog.m3d(CameraConstants.TAG, "mode : " + curMode + ", currentViewMode : " + currentConeMode);
        String mode = curMode;
        if (isRearCamera()) {
            if (currentConeMode == 2) {
                mode = CameraConstants.MODE_MANUAL_CAMERA;
            } else if (currentConeMode == 3) {
                mode = CameraConstants.MODE_MANUAL_VIDEO;
            }
        }
        if (currentConeMode == 4) {
            return CameraConstants.MODE_SNAP;
        }
        return mode;
    }

    private void setCurrentModule(String mode) {
        if (this.mCurrentModule != null) {
            int currentCameraId = this.mCurrentModule.getCameraId();
            int savedCameraId = SharedPreferenceUtil.getCameraId(this);
            if (currentCameraId != savedCameraId) {
                setSetting(Setting.KEY_SWAP_CAMERA, CameraDeviceUtils.isRearCamera(savedCameraId) ? "rear" : "front", true);
            }
        }
        String cameraIdStr = getCurSettingValue(Setting.KEY_SWAP_CAMERA);
        if ("front".equals(cameraIdStr) && "mode_normal".equals(mode)) {
            if (FunctionProperties.isSupportedSmartCam(getAppContext()) && this.mUspZoneManager != null && CameraConstantsEx.USP_SMART_CAM.equals(this.mUspZoneManager.getCurValue())) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SMART_CAM_FRONT);
            } else if (FunctionProperties.isSupportedFrontOutfocus() && this.mUspZoneManager != null && (CameraConstantsEx.USP_OUTFOCUS.equals(this.mUspZoneManager.getCurValue()) || CameraConstantsEx.USP_OUTFOCUS.equals(this.mUspZoneManager.getPrevValue()))) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_FRONT_OUTFOCUS);
            } else if (FunctionProperties.isSupportedBeautyShot()) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_BEAUTY);
            } else if (FunctionProperties.isSupportedGestureShot()) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_GESTURESHOT);
            } else {
                this.mCurrentModule = (BaseModule) this.mModuleMap.get(mode);
            }
        } else if ("rear".equals(cameraIdStr) && "mode_normal".equals(mode)) {
            if (FunctionProperties.isSupportedSmartCam(getAppContext()) && this.mUspZoneManager != null && CameraConstantsEx.USP_SMART_CAM.equals(this.mUspZoneManager.getCurValue())) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SMART_CAM);
            } else if (FunctionProperties.isSupportedRearOutfocus() && this.mUspZoneManager != null && (CameraConstantsEx.USP_OUTFOCUS.equals(this.mUspZoneManager.getCurValue()) || CameraConstantsEx.USP_OUTFOCUS.equals(this.mUspZoneManager.getPrevValue()))) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_REAR_OUTFOCUS);
            } else {
                this.mCurrentModule = (BaseModule) this.mModuleMap.get(mode);
            }
        } else if (CameraConstants.MODE_SNAP.equals(mode)) {
            if ("front".equals(cameraIdStr)) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SNAP_FRONT);
            } else if (CameraConstants.MODE_SNAP.equals(getBeforeMode())) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SNAP_SINGLE);
            } else {
                AppControlUtil.setSnapMovieMVFrameType(0);
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_SNAP_SINGLE);
                CamLog.m3d(CameraConstants.TAG, "Create singleview snap module from beforeMode = " + getBeforeMode());
            }
        } else if (CameraConstants.MODE_DUAL_POP_CAMERA.equals(mode)) {
            if ("on".equals(getSettingValue(Setting.KEY_DUAL_POP_TYPE))) {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_DUAL_POP_CAMERA);
            } else {
                this.mCurrentModule = getCreatedModule(CameraConstants.MODE_POPOUT_CAMERA);
            }
        } else if (!CameraConstants.MODE_FLASH_JUMPCUT.equals(mode)) {
            this.mCurrentModule = (BaseModule) this.mModuleMap.get(mode);
        } else if ("front".equals(cameraIdStr)) {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_FLASH_JUMPCUT_FRONT);
        } else {
            this.mCurrentModule = getCreatedModule(CameraConstants.MODE_FLASH_JUMPCUT);
            CamLog.m3d(CameraConstants.TAG, "Create MODE_FLASH_JUMPCUT_REAR module from beforeMode = " + getBeforeMode());
        }
        CamLog.m3d(CameraConstants.TAG, "###Select Module : " + mode);
    }

    public void changeModule() {
        changeModule(false);
    }

    public void changeModule(boolean doByAction) {
        CamLog.m3d(CameraConstants.TAG, "##[ChangeModule]");
        if (this.mCurrentModule == null || isPaused() || isFinishing()) {
            CamLog.m3d(CameraConstants.TAG, "exit ChangeModule");
            return;
        }
        if (!(isAnimationShowing() || this.mCurrentModule.isLightFrameOn())) {
            setPreviewCoverVisibility(0, true);
        }
        this.mCurrentModule.onChangeModuleBefore();
        if (doByAction && this.mVoiceAssistantManager != null) {
            this.mVoiceAssistantManager.clearAllFlags();
        }
        if (this.mCurrentModule.isHandlerSwitchingModule()) {
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CamLog.m3d(CameraConstants.TAG, "Change module : handler switch.");
                    CameraActivity.this.directChangeModule();
                }
            });
        } else {
            directChangeModule();
        }
    }

    public void changeToAttachModule() {
        CamLog.m3d(CameraConstants.TAG, "##[ChangeModule]");
        if (this.mCurrentModule != null && !isPaused() && !isFinishing()) {
            this.mCurrentModule.onChangeModuleBefore();
            if (this.mCurrentModule.isHandlerSwitchingModule()) {
                postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        CamLog.m3d(CameraConstants.TAG, "Change module : handler switch.");
                        CameraActivity.this.returnToAttachModule();
                    }
                });
            } else {
                returnToAttachModule();
            }
        }
    }

    public void changeModuleByCoverstate() {
    }

    private void directChangeModule() {
        CamLog.m3d(CameraConstants.TAG, "-acq- directChangeModule");
        if (this.mCurrentModule == null || isPaused() || isFinishing()) {
            CamLog.m3d(CameraConstants.TAG, "exit directChangeModule");
            return;
        }
        String curMode = this.mCurrentModule.getShotMode();
        String changeMode = getCurSettingValue(Setting.KEY_MODE);
        CamLog.m3d(CameraConstants.TAG, "curMode : " + curMode + ", changeMode " + changeMode);
        if (!(isCameraChanging() || CameraConstants.MODE_DUAL_POP_CAMERA.equals(curMode) || CameraConstants.MODE_POPOUT_CAMERA.equals(curMode) || CameraConstants.MODE_PANORAMA_LG_360_PROJ.equals(curMode) || CameraConstants.MODE_PANORAMA_LG_360_PROJ.equals(changeMode) || CameraConstants.MODE_CINEMA.equals(curMode) || CameraConstants.MODE_CINEMA.equals(changeMode) || (isOpticZoomSupported(CameraConstants.MODE_MANUAL_VIDEO) && CameraConstants.MODE_MANUAL_VIDEO.equals(curMode)))) {
            CamLog.m3d(CameraConstants.TAG, "keep camera device");
            CameraHolder.instance().keep();
        }
        closeModule(this.mCurrentModule);
        this.mWaitPreviewBufferConfig.open();
        if (!(isCameraChanging() && isAttachIntent() && !isLGUOEMCameraIntent())) {
            selectModule();
        }
        if (this.mCurrentModule == null) {
            CamLog.m3d(CameraConstants.TAG, "exit directChangeModule. mCurrentModule is null");
            return;
        }
        this.mHybridView.acquireHybridView(this.mCurrentModule.getClass());
        if (FunctionProperties.getSupportedHal() == 2 && !isVideoCaptureMode()) {
            int[] previewSize = getpreviewSize(changeMode);
            if (previewSize != null && previewSize.length == 2) {
                setCameraPreviewSize(previewSize[0], previewSize[1]);
            }
        }
        openModule(this.mCurrentModule);
    }

    public void showToast(String string, long toastLengthLong) {
        this.mCurrentModule.showToast(string, toastLengthLong);
    }

    private void returnToAttachModule() {
        if (this.mCurrentModule != null && !isPaused() && !isFinishing()) {
            if (!isCameraChanging()) {
                CameraHolder.instance().keep();
            }
            closeModule(this.mCurrentModule);
            if (this.mAttachModule != null) {
                this.mCurrentModule = this.mAttachModule;
            } else {
                selectOtherModule();
            }
            openModule(this.mCurrentModule);
            this.mCurrentModule.onChangeModuleAfter();
        }
    }

    protected void changeModuleOnResume() {
        if (this.mCurrentModule != null) {
            boolean needChangeModule = false;
            if (this.mConeUIManager != null) {
                this.mConeUIManager.initConeMode();
                this.mCurrentModule.checkCameraId();
                if (checkNeedModuleChange(this.mConeUIManager.getCurrentViewMode(), this.mCurrentModule.getShotMode())) {
                    needChangeModule = true;
                }
            }
            String mode = getCurSettingValue(Setting.KEY_MODE);
            if (this.mModeMenuManager.isDownloadableMode(mode) && this.mModeMenuManager.checkDownloadedMode(mode)) {
                CamLog.m3d(CameraConstants.TAG, "[mode] " + mode + " is deleted. change to Auto");
                mode = "mode_normal";
                setSetting(Setting.KEY_MODE, "mode_normal", false);
                needChangeModule = true;
            }
            if (needChangeModule) {
                changeModule();
            }
        }
    }

    private void openModule(BaseModule module) {
        CamLog.m3d(CameraConstants.TAG, "##[openModule] - start");
        if (module != null) {
            module.startCameraDeviceOnCreate();
            this.mCheckInitDone = false;
            module.init();
            module.onResumeBefore();
        }
        CamLog.m3d(CameraConstants.TAG, "##[openModule] - end");
    }

    private void closeModule(BaseModule module) {
        CamLog.m3d(CameraConstants.TAG, "##[closeModule] - start");
        if (module != null) {
            module.onPauseBefore();
            module.onPauseAfter();
            module.onStop();
            module.onDestroy();
            module.restoreSettingMenus();
        }
        if (this.mContentsBaseView != null) {
            this.mContentsBaseView.removeAllViews();
        }
        CamLog.m3d(CameraConstants.TAG, "##[closeModule] - end");
    }

    protected void displayPreviewCoverDuringSwitching() {
        ImageView coverView = (ImageView) findViewById(C0088R.id.preview_cover_view);
        if (coverView != null) {
            TextureView view = getTextureView();
            if (view == null) {
                CamLog.m3d(CameraConstants.TAG, "-coverview- TextureView is null. return.");
                setPreviewCoverVisibility(0, true);
                return;
            }
            Bitmap bmp;
            CamLog.m3d(CameraConstants.TAG, "##[DisplayPreviewCoverDuringSwitching] - start");
            int width = view.getWidth();
            int height = view.getHeight();
            if (SharedPreferenceUtil.getCameraId(getAppContext()) == 0 && FunctionProperties.isSupportedLightFrame()) {
                Setting frontSetting = getSpecificSetting(false);
                if (frontSetting != null) {
                    if ("on".equals(frontSetting.getSettingValue(Setting.KEY_LIGHTFRAME))) {
                        CamLog.m3d(CameraConstants.TAG, "##[DisplayPreviewCoverDuringSwitching] - FLASH_MODE_ON");
                        bmp = view.getBitmap(width, height);
                        coverView.setScaleType(ScaleType.CENTER);
                        GradientDrawable gd = (GradientDrawable) getResources().getDrawable(C0088R.drawable.gradation);
                        gd.setGradientRadius(getResources().getDimension(C0088R.dimen.lcd.width));
                        coverView.setBackground(gd);
                        coverView.setImageBitmap(bmp);
                        setPreviewCoverVisibility(0, true);
                        return;
                    }
                }
                return;
            }
            bmp = view.getBitmap(width / 2, height / 2);
            coverView.setScaleType(ScaleType.FIT_CENTER);
            int[] lcdSize = Utils.getLCDsize(getAppContext(), false);
            if ((lcdSize != null && lcdSize[0] > height && lcdSize[1] > width) || isStartedFromQuickCover()) {
                LayoutParams lpCoverView = (LayoutParams) coverView.getLayoutParams();
                lpCoverView.width = width;
                lpCoverView.height = height;
                lpCoverView.topMargin = view.getTop();
                lpCoverView.setMarginStart(view.getLeft());
                coverView.setLayoutParams(lpCoverView);
            }
            if (ModelProperties.isKeyPadSupported(getAppContext())) {
                bmp = view.getBitmap(width, height);
                setCoverViewTopMargin(coverView, view);
            }
            coverView.setImageBitmap(bmp);
            setPreviewCoverVisibility(0, true);
            CamLog.m3d(CameraConstants.TAG, "##[DisplayPreviewCoverDuringSwitching] - end");
        }
    }

    public void setCoverViewTopMargin(ImageView coverView, TextureView textureView) {
        int[] lcdSize = Utils.getLCDsize(getAppContext(), false);
        int lcdWidth = CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL;
        int lcdHeight = 320;
        int textureWidth = textureView.getWidth();
        int textureHeight = textureView.getHeight();
        if (lcdSize != null) {
            lcdWidth = lcdSize[0];
            lcdHeight = lcdSize[1];
        }
        float lcdRatio = ((float) lcdWidth) / ((float) lcdHeight);
        float previewRatio = ((float) textureWidth) / ((float) textureHeight);
        if (!Utils.isConfigureLandscape(getResources())) {
            previewRatio = ((float) textureHeight) / ((float) textureWidth);
        }
        LayoutParams lp = (LayoutParams) coverView.getLayoutParams();
        lp.topMargin = 0;
        if (Float.compare(lcdRatio, previewRatio) != 0) {
            lp.topMargin = textureView.getTop();
            coverView.setScaleType(ScaleType.FIT_START);
        }
        coverView.setLayoutParams(lp);
    }

    public void setPreviewCoverVisibility(int visibility, boolean isMovePreview) {
        setPreviewCoverVisibility(visibility, false, null, isMovePreview, false);
    }

    public void setPreviewCoverVisibility(int visibility, boolean useAnim, AnimationListener listener, boolean isMovePreview, boolean usePreviewCapture) {
        final ImageView coverView = (ImageView) findViewById(C0088R.id.preview_cover_view);
        if (coverView == null || (visibility != 0 && isPaused())) {
            CamLog.m3d(CameraConstants.TAG, "-coverview- do not invisible cover on paused, coverView : " + coverView);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-coverview- setPreviewCoverVisibility visibility = " + visibility + ", useAnim : " + useAnim + ", mIsSetPreviewCaptureCover : " + this.mIsSetPreviewCaptureCover);
        if (!FunctionProperties.isSupportedSwitchingAnimation()) {
            CamLog.m3d(CameraConstants.TAG, "Not support switching animation");
        } else if (!(visibility == 0 || this.mSwitchAnimationView == null || this.mSwitchAnimationView.getVisibility() != 0)) {
            switchAnimationEnd(isMovePreview);
        }
        if (this.mIsSetPreviewCaptureCover && !isAnimationShowing()) {
            usePreviewCapture = true;
        }
        if (!usePreviewCapture || this.mCurPreviewBmp == null || this.mCurPreviewBmp.isRecycled()) {
            this.mIsSetPreviewCaptureCover = false;
            coverView.setImageBitmap(null);
            coverView.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        } else {
            this.mIsSetPreviewCaptureCover = true;
            coverView.setImageBitmap(this.mCurPreviewBmp);
        }
        coverView.setScaleType(ScaleType.FIT_CENTER);
        if (useAnim) {
            final boolean finUsePreviewCapture = usePreviewCapture;
            final int i = visibility;
            final AnimationListener animationListener = listener;
            HandlerRunnable aniRunnable = new HandlerRunnable(this) {
                public void handleRun() {
                    CameraActivity.this.setPreviewCoverAlphaAnimation(i, animationListener, coverView, finUsePreviewCapture);
                }
            };
            if (this.mCurrentModule == null || !this.mCurrentModule.getShotMode().contains(CameraConstants.MODE_SMART_CAM) || this.mSwitchAnimationView == null || !(this.mSwitchAnimationView.getAnimationType() == 2 || this.mSwitchAnimationView.getAnimationType() == 3)) {
                aniRunnable.run();
            } else {
                postOnUiThread(aniRunnable, 200);
            }
        } else if (coverView.getVisibility() == visibility) {
            CamLog.m7i(CameraConstants.TAG, "-coverview-, cover already visible, so return");
        } else {
            setPreviewCommandCoverVisibility(false, null, null, 0);
            coverView.setScaleX(1.0f);
            coverView.setScaleY(1.0f);
            coverView.setTranslationY(0.0f);
            coverView.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            coverView.setVisibility(visibility);
            coverView.setScaleType(ScaleType.FIT_CENTER);
            this.mIsPreviewCoverVisible = visibility == 0;
            if (visibility != 0) {
                if (!(this.mHybridView == null || this.mHybridView.getPreviewVisibility() == 0)) {
                    setPreviewVisibility(0);
                }
                this.mIsSetPreviewCaptureCover = false;
                BitmapManagingUtil.clearImageViewDrawable(coverView);
                CamLog.traceEnd(TraceTag.MANDATORY, "SwitchCamera", 1001);
            } else if (!usePreviewCapture) {
                BitmapManagingUtil.clearImageViewDrawable(coverView);
            }
        }
    }

    private void setPreviewCoverAlphaAnimation(int visibility, AnimationListener listener, final ImageView coverView, boolean usePreviewCapture) {
        if (visibility == 0) {
            CamLog.m7i(CameraConstants.TAG, "-coverview- show preview cover with animation, mIsSetPreviewCaptureCover : " + this.mIsSetPreviewCaptureCover);
            if (this.mIsSetPreviewCaptureCover) {
                doShowPreviewCoverWithAnimation(listener, coverView);
            } else {
                AnimationUtil.startShowingAnimation(coverView, true, 150, listener);
            }
            this.mIsPreviewCoverVisible = true;
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-coverview- current visibility " + coverView.getVisibility() + ", visibility : " + visibility);
        if (coverView.getVisibility() == 0) {
            CamLog.m7i(CameraConstants.TAG, "-coverview- hide preview cover with animation");
            AnimationListener aniListener = listener;
            if (aniListener == null) {
                aniListener = new AnimationListener() {
                    public void onAnimationStart(Animation arg0) {
                    }

                    public void onAnimationRepeat(Animation arg0) {
                    }

                    public void onAnimationEnd(Animation arg0) {
                        if (coverView != null) {
                            coverView.setScaleX(1.0f);
                            coverView.setScaleY(1.0f);
                            coverView.setTranslationY(0.0f);
                        }
                        BitmapManagingUtil.clearImageViewDrawable(coverView);
                        CameraActivity.this.setSwitchingAniViewParam(false);
                        CameraActivity.this.setPreviewCommandCoverVisibility(false, null, null, 0);
                        CameraActivity.this.mIsPreviewCoverVisible = false;
                    }
                };
            }
            AnimationUtil.startShowingAnimation(coverView, false, getHideCoverAnimDuration(), aniListener);
            this.mIsSetPreviewCaptureCover = false;
            return;
        }
        BitmapManagingUtil.clearImageViewDrawable(coverView);
        this.mIsPreviewCoverVisible = false;
        this.mIsSetPreviewCaptureCover = false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0183  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00eb  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x010b  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x011e  */
    private void doShowPreviewCoverWithAnimation(android.view.animation.Animation.AnimationListener r27, android.widget.ImageView r28) {
        /*
        r26 = this;
        r4 = "CameraApp";
        r10 = "-coverview- show preview cover with animation";
        com.lge.camera.util.CamLog.m7i(r4, r10);
        r4 = r26.getShotMode();
        r10 = r26.getCameraId();
        r4 = com.lge.camera.util.SettingKeyWrapper.getVideoSizeKey(r4, r10);
        r0 = r26;
        r22 = r0.getListPreference(r4);
        r4 = r26.getShotMode();
        r10 = r26.getCameraId();
        r4 = com.lge.camera.util.SettingKeyWrapper.getPictureSizeKey(r4, r10);
        r0 = r26;
        r12 = r0.getListPreference(r4);
        if (r22 == 0) goto L_0x002f;
    L_0x002d:
        if (r12 != 0) goto L_0x0030;
    L_0x002f:
        return;
    L_0x0030:
        r4 = 2;
        r0 = r22;
        r4 = r0.getExtraInfo(r4);
        r23 = com.lge.camera.util.Utils.sizeStringToArray(r4);
        r4 = 2;
        r4 = r12.getExtraInfo(r4);
        r17 = com.lge.camera.util.Utils.sizeStringToArray(r4);
        r4 = r26.isVideoCaptureMode();
        if (r4 == 0) goto L_0x004c;
    L_0x004a:
        r17 = r23;
    L_0x004c:
        r4 = 0;
        r4 = r17[r4];
        r4 = (float) r4;
        r10 = 1;
        r10 = r17[r10];
        r10 = (float) r10;
        r16 = r4 / r10;
        r4 = 0;
        r4 = r23[r4];
        r4 = (float) r4;
        r10 = 1;
        r10 = r23[r10];
        r10 = (float) r10;
        r19 = r4 / r10;
        r0 = r16;
        r1 = r19;
        r4 = java.lang.Float.compare(r0, r1);
        if (r4 == 0) goto L_0x0085;
    L_0x006a:
        r4 = 1072902963; // 0x3ff33333 float:1.9 double:5.300844953E-315;
        r4 = (r16 > r4 ? 1 : (r16 == r4 ? 0 : -1));
        if (r4 > 0) goto L_0x0078;
    L_0x0071:
        r4 = 1067869798; // 0x3fa66666 float:1.3 double:5.275977814E-315;
        r4 = (r16 > r4 ? 1 : (r16 == r4 ? 0 : -1));
        if (r4 >= 0) goto L_0x0085;
    L_0x0078:
        r4 = 1;
        r24 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r0 = r28;
        r1 = r24;
        r3 = r27;
        com.lge.camera.util.AnimationUtil.startPreviewCoverAlphaAnimation(r0, r4, r1, r3);
        goto L_0x002f;
    L_0x0085:
        r4 = r26.getAppContext();
        r10 = 1;
        r15 = com.lge.camera.util.Utils.getLCDsize(r4, r10);
        r21 = r26.getShotMode();
        r18 = 0;
        r20 = 0;
        if (r21 == 0) goto L_0x0157;
    L_0x0098:
        r4 = "mode_square";
        r0 = r21;
        r4 = r0.contains(r4);
        if (r4 == 0) goto L_0x0157;
    L_0x00a2:
        r4 = com.lge.camera.constants.ModelProperties.getLCDType();
        r10 = 2;
        if (r4 != r10) goto L_0x0151;
    L_0x00a9:
        r4 = r26.getAppContext();
        r10 = 18;
        r24 = 9;
        r25 = 0;
        r0 = r24;
        r1 = r25;
        r18 = com.lge.camera.util.RatioCalcUtil.getLongLCDModelTopMargin(r4, r10, r0, r1);
        r4 = r26.getAppContext();
        r10 = 18;
        r24 = 9;
        r25 = 0;
        r0 = r24;
        r1 = r25;
        r20 = com.lge.camera.util.RatioCalcUtil.getLongLCDModelTopMargin(r4, r10, r0, r1);
    L_0x00cd:
        r4 = 0;
        r11 = r17[r4];
        r4 = (float) r11;
        r4 = r4 / r19;
        r14 = (int) r4;
        r4 = 1;
        r4 = r17[r4];
        r4 = (float) r4;
        r10 = (float) r14;
        r5 = r4 / r10;
        r4 = "on";
        r10 = "key_tile_preview";
        r0 = r26;
        r10 = r0.getCurSettingValue(r10);
        r4 = r4.equals(r10);
        if (r4 == 0) goto L_0x0183;
    L_0x00eb:
        r4 = 1;
        r10 = 1038324450; // 0x3de392e2 float:0.11112 double:5.1300044E-315;
        r0 = r26;
        r13 = com.lge.camera.util.RatioCalcUtil.getSizeCalculatedByPercentage(r0, r4, r10);
    L_0x00f5:
        r4 = r13 + r18;
        r10 = 0;
        r10 = r17[r10];
        r10 = r10 / 2;
        r8 = r4 + r10;
        r4 = 0;
        r4 = r23[r4];
        r4 = r4 / 2;
        r9 = r20 + r4;
        r4 = r26.isVideoCaptureMode();
        if (r4 == 0) goto L_0x010c;
    L_0x010b:
        r9 = r8;
    L_0x010c:
        r6 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        r4 = "on";
        r10 = "key_video_steady";
        r0 = r26;
        r10 = r0.getCurSettingValue(r10);
        r4 = r4.equals(r10);
        if (r4 == 0) goto L_0x0128;
    L_0x011e:
        r4 = r26.isRearCamera();
        if (r4 == 0) goto L_0x0186;
    L_0x0124:
        r4 = 1067030938; // 0x3f99999a float:1.2 double:5.271833295E-315;
        r5 = r5 * r4;
    L_0x0128:
        r4 = 1;
        r0 = r26;
        r1 = r23;
        r2 = r20;
        r0.setPreviewCommandCoverVisibility(r4, r1, r15, r2);
        r4 = 0;
        r0 = r28;
        r0.setVisibility(r4);
        r4 = 1;
        r4 = r15[r4];
        r4 = r4 / 2;
        r4 = (float) r4;
        r0 = r28;
        r0.setPivotX(r4);
        r4 = (float) r8;
        r0 = r28;
        r0.setPivotY(r4);
        r10 = 0;
        r4 = r28;
        com.lge.camera.util.AnimationUtil.startPreviewCoverScaleAnimation(r4, r5, r6, r8, r9, r10);
        goto L_0x002f;
    L_0x0151:
        r18 = 0;
        r20 = 0;
        goto L_0x00cd;
    L_0x0157:
        r4 = r26.getAppContext();
        r10 = 0;
        r10 = r17[r10];
        r24 = 1;
        r24 = r17[r24];
        r25 = 0;
        r0 = r24;
        r1 = r25;
        r18 = com.lge.camera.util.RatioCalcUtil.getLongLCDModelTopMargin(r4, r10, r0, r1);
        r4 = r26.getAppContext();
        r10 = 0;
        r10 = r23[r10];
        r24 = 1;
        r24 = r23[r24];
        r25 = 0;
        r0 = r24;
        r1 = r25;
        r20 = com.lge.camera.util.RatioCalcUtil.getLongLCDModelTopMargin(r4, r10, r0, r1);
        goto L_0x00cd;
    L_0x0183:
        r13 = 0;
        goto L_0x00f5;
    L_0x0186:
        r4 = 1067702026; // 0x3fa3d70a float:1.28 double:5.27514891E-315;
        r5 = r5 * r4;
        goto L_0x0128;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.CameraActivity.doShowPreviewCoverWithAnimation(android.view.animation.Animation$AnimationListener, android.widget.ImageView):void");
    }

    private void setPreviewCommandCoverVisibility(boolean show, int[] videoScreenSize, int[] lcdSize, int previewTopMargin) {
        View commandCoverView = findViewById(C0088R.id.preview_command_cover);
        View quickbuttonCoverView = findViewById(C0088R.id.preview_quickbutton_cover);
        if (commandCoverView != null && quickbuttonCoverView != null) {
            if (!show) {
                commandCoverView.setVisibility(8);
                quickbuttonCoverView.setVisibility(8);
            } else if (videoScreenSize != null && lcdSize != null) {
                float videoSizeRatio = ((float) videoScreenSize[0]) / ((float) videoScreenSize[1]);
                LayoutParams rlp;
                if (videoSizeRatio > 0.9f && videoSizeRatio < 1.1f) {
                    rlp = (LayoutParams) quickbuttonCoverView.getLayoutParams();
                    rlp.height = previewTopMargin;
                    quickbuttonCoverView.setLayoutParams(rlp);
                    quickbuttonCoverView.setVisibility(0);
                } else if ((videoSizeRatio > 1.3f && videoSizeRatio < 1.4f) || ((videoSizeRatio > 1.7f && videoSizeRatio < 1.8f) || (videoSizeRatio >= 2.0f && videoSizeRatio <= 2.1f && ModelProperties.getLCDType() == 2))) {
                    rlp = (LayoutParams) commandCoverView.getLayoutParams();
                    rlp.height = (lcdSize[0] - previewTopMargin) - ((int) (((float) lcdSize[1]) * videoSizeRatio));
                    commandCoverView.setLayoutParams(rlp);
                    commandCoverView.setVisibility(0);
                    rlp = (LayoutParams) quickbuttonCoverView.getLayoutParams();
                    rlp.height = previewTopMargin;
                    quickbuttonCoverView.setLayoutParams(rlp);
                    quickbuttonCoverView.setVisibility(0);
                }
            }
        }
    }

    private long getHideCoverAnimDuration() {
        if (this.mIsSetPreviewCaptureCover) {
            return 200;
        }
        if (this.mSwitchAnimationView == null || this.mSwitchAnimationView.getAnimationType() == 1) {
            return 350;
        }
        return 200;
    }

    private void switchAnimationEnd(final boolean isMovePreview) {
        if (FunctionProperties.isSupportedSwitchingAnimation() && this.mSwitchAnimationView != null && this.mCurrentModule != null) {
            HandlerRunnable aniRunnable = new HandlerRunnable(this) {
                public void handleRun() {
                    if (CameraActivity.this.mSwitchAnimationView == null) {
                        CamLog.m7i(CameraConstants.TAG, "mSwitchAnimationView is null, return.");
                        return;
                    }
                    if (CameraActivity.this.mSwitchAnimationView.checkCameraSwitchingAnim(CameraActivity.this.mCurrentModule.isOpticZoomSupported(null))) {
                        CameraActivity.this.setPreviewCoverAlphaAnimation(8, null, (ImageView) CameraActivity.this.findViewById(C0088R.id.preview_cover_view), false);
                    }
                    CameraActivity.this.mSwitchAnimationView.setTranslationY((float) CameraActivity.this.mOutofWindowDistance);
                    if (isMovePreview) {
                        CameraActivity.this.movePreviewOutOfWindow(false);
                    }
                    CamLog.m3d(CameraConstants.TAG, "-cover- animation end");
                    CameraActivity.this.removePostRunnable(CameraActivity.this.mHideSwitchAnimationViewHandler);
                    CameraActivity.this.postOnUiThread(CameraActivity.this.mHideSwitchAnimationViewHandler, 100);
                    CameraActivity.this.mSwitchAnimationView.stopGLAnimation();
                }
            };
            if (this.mCurrentModule == null || !this.mCurrentModule.getShotMode().contains(CameraConstants.MODE_SMART_CAM) || this.mSwitchAnimationView == null || !(this.mSwitchAnimationView.getAnimationType() == 2 || this.mSwitchAnimationView.getAnimationType() == 3)) {
                aniRunnable.run();
            } else {
                postOnUiThread(aniRunnable, 200);
            }
        }
    }

    public int getAnimationType() {
        if (this.mSwitchAnimationView != null) {
            return this.mSwitchAnimationView.getAnimationType();
        }
        return -1;
    }

    public void startCameraSwitchingAnimation(int animationType) {
        if (!checkModuleValidate(192)) {
            movePreviewOutOfWindow(false);
        } else if (FunctionProperties.isSupportedSwitchingAnimation()) {
            CamLog.m3d(CameraConstants.TAG, "-coverview- startCameraSwitchingAnimation bitmap = " + this.mCurPreviewBmp);
            if (this.mCurPreviewBmp == null || this.mCurPreviewBmp.isRecycled()) {
                movePreviewOutOfWindow(false);
                return;
            }
            if (this.mSwitchAnimationView != null) {
                setSwitchingAniViewParam(animationType == 1);
                this.mSwitchAnimationView.setTranslationY(0.0f);
            }
            setAnimationShowing(true);
            if (animationType == 1 && !CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                movePreviewOutOfWindow(true);
            }
            if (this.mSwitchAnimationView != null) {
                this.mSwitchAnimationView.setTextureBitmap(this.mCurPreviewBmp);
                this.mSwitchAnimationView.startGLAnimation(animationType);
                this.mSwitchAnimationView.setVisibility(0);
                CamLog.m3d(CameraConstants.TAG, "-ani- set animation view visible");
                if (this.mSwitchAnimationView.checkCameraSwitchingAnim(this.mCurrentModule.isOpticZoomSupported(null)) && !CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                    setPreviewCoverVisibility(0, false, null, false, false);
                }
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "Not support switching animation");
            movePreviewOutOfWindow(false);
            setPreviewCoverVisibility(0, true);
        }
    }

    public void setPreviewCoverBackground(Drawable bg) {
        ImageView coverView = (ImageView) findViewById(C0088R.id.preview_cover_view);
        if (coverView != null) {
            CamLog.m3d(CameraConstants.TAG, "-coverview- setPreviewCoverBackground visibility : visible, BackgroundImage = " + bg);
            coverView.setImageDrawable(bg);
            coverView.setVisibility(0);
        }
    }

    public Bitmap getCurPreviewBitmap(int width, int height) {
        long startTime = System.currentTimeMillis();
        if (!(this.mCurPreviewBmp == null || this.mCurPreviewBmp.isRecycled())) {
            this.mCurPreviewBmp.recycle();
        }
        this.mCurPreviewBmp = Utils.getScreenShot(width, height, true, null);
        DebugUtil.showElapsedTime("screenshot", startTime);
        return this.mCurPreviewBmp;
    }

    public Bitmap getCurPreviewBlurredBitmap(int scaledWidth, int scaledHeight, int blurRadius, boolean surfaceOnly) {
        return getCurPreviewBlurredBitmap(scaledWidth, scaledHeight, blurRadius, surfaceOnly, false);
    }

    public Bitmap getCurPreviewBlurredBitmap(int scaledWidth, int scaledHeight, int blurRadius, boolean surfaceOnly, boolean isFinishWithBackkey) {
        if (!FunctionProperties.isSupportedSwitchingAnimation() || this.mProcessFinish) {
            return null;
        }
        if (!this.isScreenCapturing) {
            ListPreference listPref;
            if (isVideoCaptureMode() || isRecordingPriorityMode()) {
                listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), getCameraId()));
            } else {
                listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), getCameraId()));
            }
            if (listPref == null || listPref.getExtraInfo(2) == null) {
                CamLog.m3d(CameraConstants.TAG, "listPref is null");
                return null;
            }
            int[] coneSize = new int[]{0, 0};
            if (FunctionProperties.isSupportedConeUI() && this.mConeUIManager != null) {
                coneSize = this.mConeUIManager.getConeScreenSize();
            }
            int[] previewSize = Utils.sizeStringToArray(listPref.getExtraInfo(2));
            this.isScreenCapturing = true;
            if (!(this.mCurPreviewBmp == null || this.mCurPreviewBmp.isRecycled())) {
                this.mCurPreviewBmp.recycle();
            }
            int[] lcd_size = Utils.getLCDsize(this, true);
            if (!(getShotMode() == null || !getShotMode().contains(CameraConstants.MODE_SQUARE) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) || isFinishWithBackkey)) {
                int i = lcd_size[1];
                previewSize[1] = i;
                previewSize[0] = i;
                lcd_size[0] = i;
            }
            CamLog.m3d(CameraConstants.TAG, "screen size = " + previewSize[0] + "x" + previewSize[1]);
            this.mCurPreviewBmp = Utils.getScreenShot(lcd_size[1], lcd_size[0], surfaceOnly, getBlurBitmapCropRect(coneSize, previewSize, lcd_size, isFinishWithBackkey));
            if (this.mCurPreviewBmp == null) {
                this.isScreenCapturing = false;
                CamLog.m3d(CameraConstants.TAG, "-swap- screen capture fail");
                return null;
            }
            Bitmap bitmapResized = Bitmap.createScaledBitmap(this.mCurPreviewBmp, scaledWidth, scaledHeight, true);
            if (!(this.mCurPreviewBmp == null || this.mCurPreviewBmp.isRecycled())) {
                this.mCurPreviewBmp.recycle();
            }
            this.mCurPreviewBmp = ColorUtil.getBlurImage(this, bitmapResized, blurRadius);
            this.isScreenCapturing = false;
            if (bitmapResized != null) {
                bitmapResized.recycle();
            }
        }
        return this.mCurPreviewBmp;
    }

    private Rect getBlurBitmapCropRect(int[] coneSize, int[] previewSize, int[] lcd_size, boolean isFinishWithBackkey) {
        int startMargin = getStartMarginOfPreview(previewSize, lcd_size);
        CamLog.m3d(CameraConstants.TAG, "lcd size = " + lcd_size[0] + "x" + lcd_size[1] + " startMargin = " + startMargin);
        if (this.mCurrentModule.isLightFrameOn() || isFinishWithBackkey) {
            return null;
        }
        if (!CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            return new Rect(0, coneSize[1] + startMargin, lcd_size[1], (previewSize[0] + startMargin) + coneSize[1]);
        }
        int index = this.mCurrentModule.getSpliceViewIndex();
        if (index <= -1) {
            return null;
        }
        if (index == 0) {
            return new Rect(0, 0, lcd_size[1], lcd_size[1]);
        }
        if (index == 1) {
            return new Rect(0, lcd_size[1], lcd_size[1], lcd_size[0]);
        }
        return null;
    }

    public void movePreviewOutOfWindow(boolean outOfWindow) {
        CamLog.m3d(CameraConstants.TAG, "movePreviewOutOfWindow : " + outOfWindow);
        int distance = outOfWindow ? this.mOutofWindowDistance : 0;
        if (this.mHybridView != null) {
            this.mHybridView.positionPreviewToY(distance);
        }
    }

    public void setGestureType(int gestureType) {
        if (this.mSwitchAnimationView != null) {
            this.mSwitchAnimationView.setFlipDirection(gestureType);
        }
    }

    public int getPreviewCoverVisibility() {
        View coverView = findViewById(C0088R.id.preview_cover_view);
        if (coverView == null) {
            return 8;
        }
        if (this.mIsPreviewCoverVisible) {
            return 0;
        }
        return coverView.getVisibility();
    }

    public void onCameraSwitchingStart() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onCameraSwitchingStart();
        }
    }

    public void onCameraSwitchingEnd() {
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CameraActivity.this.mCurrentModule != null) {
                    if (CameraActivity.this.isCameraChanging()) {
                        CameraActivity.this.removeCameraChanging(1);
                        CameraActivity.this.setCameraChangingOnSnap(false);
                        CameraActivity.this.setCameraChangingOnFlashJumpCut(false);
                        CameraActivity.this.setCameraChangingOnSquareSnap(false);
                    }
                    CameraActivity.this.mCurrentModule.onCameraSwitchingEnd();
                }
            }
        });
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (CameraConstants.MODE_SMART_CAM.equals(getShotMode()) || CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode())) {
            switch (event.getActionMasked() & 255) {
                case 0:
                    if (this.mCurrentModule != null) {
                        this.mCurrentModule.stopTimerForSmartCamScene();
                        break;
                    }
                    break;
                case 1:
                    if (this.mCurrentModule != null) {
                        this.mCurrentModule.startTimerForSmartCamScene();
                        break;
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mCurrentModule == null) {
            return false;
        }
        if (this.mCurrentModule.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mCurrentModule != null) {
            return !this.mCurrentModule.onKeyDown(keyCode, event) ? super.onKeyDown(keyCode, event) : true;
        } else {
            return false;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mCurrentModule != null) {
            return !this.mCurrentModule.onKeyUp(keyCode, event) ? super.onKeyUp(keyCode, event) : true;
        } else {
            return false;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mCurrentModule != null && (event.getKeyCode() == 23 || event.getKeyCode() == 66)) {
            if (event.getAction() == 0) {
                this.mCurrentModule.onKeyDown(event.getKeyCode(), event);
            } else if (event.getAction() == 1) {
                this.mCurrentModule.onKeyUp(event.getKeyCode(), event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public TextureView getTextureView() {
        if (this.mHybridView != null) {
            return this.mHybridView.getCameraTextureView();
        }
        CamLog.m11w(CameraConstants.TAG, "mHybridView has not been generated yet.");
        return null;
    }

    public SurfaceTexture getSurfaceTexture() {
        if (this.mHybridView != null) {
            CamLog.m3d(CameraConstants.TAG, "getSurfaceTexture() mPreviewTexture :: " + this.mHybridView.getSurfaceTexture());
            return this.mHybridView.getSurfaceTexture();
        }
        CamLog.m11w(CameraConstants.TAG, "mHybridView has not been generated yet.");
        return null;
    }

    public String getCurrentViewType() {
        if (this.mHybridView != null) {
            return this.mHybridView.getCurrentViewType();
        }
        CamLog.m11w(CameraConstants.TAG, "mHybridView has not been generated yet.");
        return null;
    }

    public int getTextureState() {
        return this.mTextureState;
    }

    public void setPreviewVisibility(int visibility) {
        if (this.mHybridView != null) {
            this.mHybridView.setPreviewVisibility(visibility);
        }
    }

    public int getStartMarginOfPreview(int[] screenSize, int[] lcd_size) {
        int startMargin;
        int i = 0;
        float ratio = ((float) screenSize[0]) / ((float) screenSize[1]);
        if (ModelProperties.isLongLCDModel()) {
            startMargin = RatioCalcUtil.getLongLCDModelTopMargin(this, screenSize[0], screenSize[1], 0);
        } else {
            startMargin = RatioCalcUtil.getNormalLCDModelTopMargin(this, screenSize[0], screenSize[1], 0);
        }
        if (this.mCurrentModule.isLightFrameOn() || this.mCurrentModule.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
            startMargin = 0;
            if (ModelProperties.getLCDType() == 2) {
                startMargin = RatioCalcUtil.getQuickButtonWidth(this);
            }
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) && this.mCurrentModule.getSpliceViewIndex() > -1) {
                if (this.mCurrentModule.getSpliceViewIndex() != 0) {
                    i = lcd_size[1];
                }
                startMargin += i;
            }
        }
        return RatioCalcUtil.getTilePreviewMargin(getAppContext(), "on".equals(getCurSettingValue(Setting.KEY_TILE_PREVIEW)), ratio, startMargin);
    }

    public void setSwitchingAniViewParam(boolean isSwitchingAnim) {
        if (FunctionProperties.isSupportedSwitchingAnimation()) {
            int[] size = getpreviewScreenSize();
            if (size != null) {
                LayoutParams frameParams;
                int[] lcd_size = Utils.getLCDsize(this, true);
                int startMargin = getStartMarginOfPreview(size, lcd_size);
                if (this.mCurrentModule.isLightFrameOn()) {
                    frameParams = Utils.getRelativeLayoutParams(this, lcd_size[0], lcd_size[1]);
                    startMargin = 0;
                } else if (this.mCurrentModule.isRecordingPriorityMode()) {
                    frameParams = Utils.getRelativeLayoutParams(this, size[0], size[1]);
                    if (!ModelProperties.isLongLCDModel()) {
                        startMargin = lcd_size[0] - size[0];
                    }
                } else {
                    frameParams = this.mCurrentModule.getShotMode().contains(CameraConstants.MODE_SQUARE) ? Utils.getRelativeLayoutParams(this, lcd_size[1], lcd_size[1]) : Utils.getRelativeLayoutParams(this, size[0], size[1]);
                }
                if (frameParams != null) {
                    if (Utils.isConfigureLandscape(getResources())) {
                        frameParams.setMarginStart(startMargin);
                    } else {
                        frameParams.topMargin = startMargin;
                    }
                    CamLog.m3d(CameraConstants.TAG, "startMargin = " + startMargin + " , lcd w = " + lcd_size[0] + " , lcd h = " + lcd_size[1] + " , w = " + size[0] + " , h = " + size[1]);
                    if (this.mSwitchAnimationView != null) {
                        this.mSwitchAnimationView.setLayoutParams(frameParams);
                    }
                    if (!isAnimationShowing()) {
                        setPreviewCoverParam(isSwitchingAnim);
                    }
                }
            }
        }
    }

    public void setPreviewCoverParam(boolean isSwitchingAnim) {
        LayoutParams frameParams;
        int i = 0;
        int[] lcd_size = Utils.getLCDsize(this, true);
        int startMargin = 0;
        if (getShotMode() != null && getShotMode().contains(CameraConstants.MODE_SQUARE) && isSwitchingAnim) {
            if (ModelProperties.getLCDType() == 2) {
                startMargin = RatioCalcUtil.getQuickButtonWidth(this);
            }
            frameParams = Utils.getRelativeLayoutParams(this, lcd_size[1], lcd_size[1]);
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) && this.mCurrentModule.getSpliceViewIndex() > -1) {
                if (this.mCurrentModule.getSpliceViewIndex() != 0) {
                    i = lcd_size[1];
                }
                startMargin += i;
            }
        } else {
            frameParams = Utils.getRelativeLayoutParams(this, lcd_size[0], lcd_size[1]);
        }
        if (frameParams != null) {
            if (Utils.isConfigureLandscape(getResources())) {
                frameParams.setMarginStart(startMargin);
            } else {
                frameParams.topMargin = startMargin;
            }
            ImageView coverView = (ImageView) findViewById(C0088R.id.preview_cover_view);
            if (coverView != null) {
                coverView.setLayoutParams(frameParams);
            }
        }
    }

    private int[] getpreviewScreenSize() {
        if (this.mCurrentModule == null) {
            return null;
        }
        if (ModelProperties.isLongLCDModel() && CameraConstants.MODE_MANUAL_VIDEO.equals(getShotMode())) {
            return this.mCurrentModule.getManualVideoScreenSize();
        }
        ListPreference listPref;
        if (isVideoCaptureMode() || this.mCurrentModule.isRecordingPriorityMode()) {
            listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), getCameraId()));
        } else {
            listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), getCameraId()));
        }
        if (listPref == null) {
            return null;
        }
        String screenSize = listPref.getExtraInfo(2);
        if (screenSize != null) {
            return Utils.sizeStringToArray(screenSize);
        }
        return null;
    }

    private int[] getpreviewSize(String mode) {
        if (this.mCurrentModule == null || mode == null || mode.contains(CameraConstants.MODE_PANORAMA)) {
            return null;
        }
        String key;
        if (CameraConstants.MODE_MANUAL_VIDEO.equals(mode)) {
            key = Setting.KEY_MANUAL_VIDEO_SIZE;
        } else if (isVideoCaptureMode() || this.mCurrentModule.isRecordingPriorityMode()) {
            key = SettingKeyWrapper.getVideoSizeKey(mode, getCameraId());
        } else {
            key = SettingKeyWrapper.getPictureSizeKey(mode, getCameraId());
        }
        ListPreference listPref = getListPreference(key);
        if (listPref == null) {
            return null;
        }
        String previewSize = listPref.getExtraInfo(1);
        if (previewSize != null) {
            return Utils.sizeStringToArray(previewSize);
        }
        return null;
    }

    public void setTextureLayoutParams(int width, int height, int startMargin) {
        setTextureLayoutParams(width, height, startMargin, false);
    }

    public void setTextureLayoutParams(int width, int height, int startMargin, boolean isRecordBtnClicked) {
        LayoutParams relativeParams = Utils.getRelativeLayoutParams(this, width, height);
        FrameLayout.LayoutParams frameParams = Utils.getFrameLayoutParams(this, width, height);
        if (relativeParams != null && frameParams != null && this.mHybridView != null) {
            startMargin = calStartMargin(width, height, startMargin, isRecordBtnClicked);
            if (startMargin != -1) {
                if (Utils.isConfigureLandscape(getResources())) {
                    relativeParams.setMarginStart(startMargin);
                    frameParams.setMarginStart(startMargin);
                } else {
                    relativeParams.topMargin = startMargin;
                    frameParams.topMargin = startMargin;
                }
            }
            int[] size = Utils.getLCDsize(getAppContext(), true);
            RatioCalcUtil.setPreviewTextureRect(new Rect(Math.min(size[0], size[1]) - Math.min(width, height), startMargin, Math.min(size[0], size[1]), Math.max(width, height) + startMargin));
            CamLog.m3d(CameraConstants.TAG, "setTextureLayoutParams width = " + width + ", height = " + height);
            CamLog.m3d(CameraConstants.TAG, "-picsize- setTextureLayoutParams params.width = " + relativeParams.width + ", params.height = " + relativeParams.height + " startMargin : " + startMargin);
            View gridView = findViewById(C0088R.id.preview_frame_grid);
            if (gridView != null) {
                gridView.setLayoutParams(relativeParams);
            }
            if (this.mCurrentModule != null) {
                this.mCurrentModule.setContentFrameLayoutParam(relativeParams);
            }
            final FrameLayout.LayoutParams finalFrameParams = frameParams;
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CameraActivity.this.mHybridView.setLayoutParams(finalFrameParams);
                }
            });
        }
    }

    public int calStartMargin(int width, int height, int startMargin, boolean isRecordBtnClicked) {
        float ratio = ((float) width) / ((float) height);
        if (startMargin == -1) {
            if (ModelProperties.isLongLCDModel()) {
                startMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), width, height, 0);
            } else {
                startMargin = RatioCalcUtil.getNormalLCDModelTopMargin(getAppContext(), width, height, 0);
            }
        }
        if (this.mCurrentModule.isLightFrameOn() || isRecordBtnClicked) {
            return startMargin;
        }
        return RatioCalcUtil.getTilePreviewMargin(getAppContext(), "on".equals(getSettingValue(Setting.KEY_TILE_PREVIEW)), ratio, startMargin);
    }

    public void onLayoutChange(View v, int l, int t, int r, int b) {
    }

    public void onOrientationChanged(final int degree, final boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (!isFirst) {
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (CameraActivity.this.mCurrentModule != null) {
                        CameraActivity.this.mCurrentModule.onOrientationChanged(degree, isFirst);
                    }
                }
            });
        } else if (this.mCurrentModule != null) {
            this.mCurrentModule.onOrientationChanged(degree, isFirst);
        }
    }

    protected void doPhoneStateListenerAction(int state) {
        CamLog.m3d(CameraConstants.TAG, "doPhoneStateListenerAction state = " + state);
        if (this.mCurrentModule != null) {
            this.mCurrentModule.doPhoneStateListenerAction(state);
        }
    }

    protected void createBroadCastReceiver(int receiverType) {
        if (SecureImageUtil.isSecureCameraIntent(getActivity().getIntent())) {
            receiverType |= 4;
        }
        this.mReceiverManager = new BroadCastReceiverManager();
        this.mReceiverManager.registerAllReceiver(this, receiverType);
    }

    protected void releaseBroadCastReceiver() {
        if (this.mReceiverManager != null) {
            this.mReceiverManager.unregisterReceivers(this);
            this.mReceiverManager = null;
        }
    }

    public void changeCoverState(boolean isCoverClosed) {
        if (!isCoverClosed && this.mIsCheckingPermission) {
            RequestPermissionsHelper.requestPermissionsIfNeeded(this, CameraConstants.UI_REQUIRED_PERMISSIONS, new CameraPermissionUiProvider());
        } else if (this.mCurrentModule != null) {
            this.mCurrentModule.changeCoverState(isCoverClosed);
        }
    }

    public boolean isRecordingState() {
        if (this.mCurrentModule == null) {
            return false;
        }
        switch (this.mCurrentModule.getCameraState()) {
            case 6:
            case 7:
                return true;
            default:
                return false;
        }
    }

    public void setHeadsetState(int headsetState) {
        CamLog.m3d(CameraConstants.TAG, "headset state = " + headsetState);
        if (this.mCurrentModule != null) {
            this.mHeadsetState = headsetState;
            removePostRunnable(this.mHeadsetHandlerRunnable);
            this.mHeadsetHandlerRunnable = new HandlerRunnable(this) {
                public void handleRun() {
                    if (CameraActivity.this.mHeadsetState == 0) {
                        CameraActivity.this.mCurrentModule.onHeadsetUnPlugged();
                    } else {
                        CameraActivity.this.mCurrentModule.onHeadsetPlugged(CameraActivity.this.mHeadsetState);
                    }
                }
            };
            postOnUiThread(this.mHeadsetHandlerRunnable, this.mCurrentModule.getCameraState() == 5 ? 850 : 0);
        }
    }

    public void onReceiveAudioNoiseIntent() {
        if (this.mCurrentModule != null) {
            removePostRunnable(this.mAudioNoiseHandlerRunnable);
            this.mAudioNoiseHandlerRunnable = new HandlerRunnable(this) {
                public void handleRun() {
                    CameraActivity.this.mCurrentModule.onReceiveAudioNoiseIntent();
                }
            };
            postOnUiThread(this.mAudioNoiseHandlerRunnable, this.mCurrentModule.getCameraState() == 5 ? 850 : 0);
        }
    }

    public void onReceiveAudioRaMIntent(int status) {
        CamLog.m3d(CameraConstants.TAG, "[audio] receive RaM intent, status :" + status);
        if (status > 0 && this.mCurrentModule != null && this.mCurrentModule.getCameraState() == 6 && this.mCurrentModule != this.mRaMProcessedModule) {
            this.mCurrentModule.showToast(getString(C0088R.string.ram_recording_noti), CameraConstants.TOAST_LENGTH_SHORT);
            this.mRaMProcessedModule = this.mCurrentModule;
        }
    }

    public void requestNotifyNewMediaonActivity(Uri uri, boolean updateThumbnail) {
        CamLog.m3d(CameraConstants.TAG, "requestNotifyNewMediaonActivity : URI = " + uri);
        if (isLGUOEMCameraIntent()) {
            this.mSavedUri = uri;
        }
        if (this.mCurrentModule != null) {
            this.mCurrentModule.notifyNewMedia(uri, updateThumbnail);
        } else if (!AppControlUtil.isGuestMode()) {
            SharedPreferenceUtil.saveLastThumbnail(this, uri);
        }
    }

    public Uri getSavedUri() {
        Uri savedUri = this.mSavedUri;
        this.mSavedUri = null;
        return savedUri;
    }

    public void setMessageIndicatorReceived(int msgReceived, boolean isReadAllMsg) {
    }

    public void setVoiceMailIndicatorReceived(boolean isVoiceMailReceived) {
    }

    public void setCharging(boolean charging) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setCharging(charging);
        }
    }

    public void onBatteryLevelChanged(int orgLevel, int indiLevel, float voltageLevel) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onBatteryLevelChanged(orgLevel, indiLevel, voltageLevel);
        }
    }

    public void onPowerConnected() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onPowerConnected();
        }
    }

    public void onPowerDisconnected() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onPowerDisconnected();
        }
    }

    public void setBatteryIndicatorVisibility(boolean visible) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setBatteryIndicatorVisibility(visible);
        }
    }

    public void childSettingMenuClicked(String key, String value) {
        childSettingMenuClicked(key, value, -1);
    }

    public void childSettingMenuClicked(String key, String value, int clickedType) {
        childSettingMenuClicked(key, value, clickedType, 0);
    }

    public void childSettingMenuClicked(String key, String value, int clickedType, int delay) {
        final String str = key;
        final String str2 = value;
        final int i = clickedType;
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CameraActivity.this.mCurrentModule != null) {
                    CameraActivity.this.mCurrentModule.childSettingMenuClicked(str, str2, i);
                }
            }
        }, (long) delay);
    }

    public void setBlockTouchByCallPopup(boolean isBlock) {
        this.mIsCallPopupShowing = isBlock;
    }

    public boolean isCallAnswering() {
        return this.mIsCallAnswering;
    }

    public void disableCheeseShutterByCallPopup() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.disableCheeseShutterByCallPopup();
        }
    }

    public void stopRecordByCallPopup() {
        this.mIsCallAnswering = true;
        if (this.mCurrentModule != null) {
            this.mCurrentModule.stopRecordByCallPopup();
        }
    }

    public void modeMenuClicked(String value) {
        changeModule(true);
    }

    public void setCameraIdBeforeChange(boolean showAni, int cameraId, boolean forceChange) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setCameraIdBeforeChange(showAni, cameraId, forceChange);
        }
    }

    public void updateModeMenuIndicator() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.updateIndicator(6, 0, false);
        }
    }

    protected void resultVideoEditor(int resultCode, Intent data) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.resultVideoEditor(resultCode, data);
        }
    }

    protected void resultFromPickImage(Intent data) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.resultFromPickImage(data);
        }
    }

    protected void resultFromCropImage(Intent data) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.resultFromCropImage(data);
        }
    }

    protected void resultFromCancel() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.resultFromCancel();
        }
    }

    protected void resultFromGraphy(int resultCode, Intent data) {
        if (resultCode == -1) {
            setIntent(data);
            setFromGraphyFlag(true);
            setGraphyIndex(data.getIntExtra("graphy_idx", -1));
        }
    }

    public void setGraphyIndex(int idx) {
        this.mGraphyIndex = idx;
    }

    public int getGraphyIndex() {
        return this.mGraphyIndex;
    }

    public boolean isPostviewShowing() {
        if (this.mCurrentModule == null || !this.mCurrentModule.isPostviewShowing()) {
            return false;
        }
        return true;
    }

    public int getPostviewType() {
        return this.mCurrentModule != null ? this.mCurrentModule.getPostviewType() : -1;
    }

    public void hideAllToast() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.hideAllToast();
        }
    }

    public String getStorageDir(int storageType) {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.getStorageDir(storageType);
        }
        return null;
    }

    public void setReviewThumbBmp(Bitmap bmp) {
        synchronized (this.mLock) {
            Bitmap reviewBmp = this.mReviewThumbBmp;
            this.mReviewThumbBmp = bmp;
            if (reviewBmp != null) {
                reviewBmp.recycle();
            }
        }
    }

    public Bitmap getReviewThumbBmp() {
        Bitmap bitmap;
        synchronized (this.mLock) {
            if ((this.mReviewThumbBmp != null && this.mReviewThumbBmp.isRecycled()) || this.mCurrentModule.getUri() == null) {
                this.mReviewThumbBmp = null;
            }
            bitmap = this.mReviewThumbBmp;
        }
        return bitmap;
    }

    public void showModuleToast(String message, long hideDelayMillis) {
        final String str = message;
        final long j = hideDelayMillis;
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CameraActivity.this.mCurrentModule != null) {
                    CameraActivity.this.mCurrentModule.showToast(str, j);
                }
            }
        });
    }

    public boolean isCameraChanging() {
        return this.mCameraChanging != 0;
    }

    public boolean isCameraReOpeningAfterInAndOutRecording() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isCameraReOpeningAfterInAndOutRecording();
        }
        return false;
    }

    public void setCameraChanging(int changeType) {
        CamLog.m3d(CameraConstants.TAG, "setCameraChanging : " + changeType);
        this.mCameraChanging |= changeType;
        if (this.mCameraChanging == 0) {
            setCameraChangingOnSnap(false);
        }
    }

    public void removeCameraChanging(int changeType) {
        CamLog.m3d(CameraConstants.TAG, "removeCameraChanging : " + changeType);
        this.mCameraChanging &= changeType ^ -1;
    }

    public boolean checkCameraChanging(int changeType) {
        int i = 1;
        boolean retVal = false;
        if ((changeType & 1) != 0) {
            retVal = false | ((this.mCameraChanging & 1) != 0 ? 1 : 0);
        }
        if ((changeType & 2) != 0) {
            int i2;
            if ((this.mCameraChanging & 2) != 0) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            retVal |= i2;
        }
        if ((changeType & 4) == 0) {
            return retVal;
        }
        if ((this.mCameraChanging & 4) == 0) {
            i = 0;
        }
        return retVal | i;
    }

    public boolean isModuleChanging() {
        return this.mModuleChanging;
    }

    public void setModuleChanging(boolean isChange) {
        this.mModuleChanging = isChange;
    }

    public void finishImageSaver() {
        if (this.mImageSaver != null) {
            this.mImageSaver.finish();
            this.mImageSaver = null;
        }
    }

    public void waitSaveImageThreadDone() {
        if (this.mImageSaver != null) {
            this.mImageSaver.waitDone();
        }
    }

    public void waitAvailableQueueCount(int availableCount) {
        if (this.mImageSaver != null) {
            this.mImageSaver.waitAvailableQueueCount(availableCount);
        }
    }

    public void handleTouchModeChanged(boolean isInTouchMode) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.handleTouchModeChanged(isInTouchMode);
        }
    }

    public int getQueueCount() {
        if (this.mImageSaver != null) {
            return this.mImageSaver.getQueueCount();
        }
        return 0;
    }

    public boolean saveImageSavers(byte[] data, int degree, boolean updateThumbnail, int countBurstshot) {
        if (this.mImageSaver != null) {
            return this.mImageSaver.addImage(data, degree, updateThumbnail, countBurstshot);
        }
        CamLog.m11w(CameraConstants.TAG, "ImageSave is null!");
        return false;
    }

    public boolean deleteAllImageSavers() {
        if (this.mImageSaver != null) {
            return this.mImageSaver.deleteAllRequests();
        }
        CamLog.m11w(CameraConstants.TAG, "ImageSave is null!");
        return false;
    }

    public void setSaveRequest(SaveRequest sr, byte[] data, int degree, boolean isSetLastThumb, int countBurstshot) {
        sr.data = data;
        sr.dateTaken = System.currentTimeMillis();
        sr.degree = degree;
        sr.updateThumbnail = isSetLastThumb;
        sr.countBurstshot = countBurstshot;
    }

    public ImageRegisterRequest saveImageDataForImageRegister(SaveRequest sr) {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.saveImageDataForImageRegister(sr);
        }
        return null;
    }

    public void doAfterSaveImageSavers(Uri uri, boolean updateThumbnail) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.doAfterSaveImageSavers(uri, updateThumbnail);
        }
    }

    public void onQueueStatus(int count) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onQueueStatus(count);
        }
    }

    public void onImageSaverQueueStatus(int count) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onImageSaverQueueStatus(count);
        }
    }

    public Uri insertImageContent(ImageRegisterRequest irr) {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.insertImageContent(irr);
        }
        return null;
    }

    public void onShowSettingEnd() {
        if (this.mCurrentModule != null && this.mCurrentModule.getHandler() != null) {
            this.mCurrentModule.getHandler().sendEmptyMessage(31);
        }
    }

    public void onRemoveSettingEnd() {
        if (this.mCurrentModule != null && this.mCurrentModule.getHandler() != null) {
            this.mCurrentModule.getHandler().sendEmptyMessage(33);
        }
    }

    public void onShowModeMenuEnd() {
        if (this.mCurrentModule != null && this.mCurrentModule.getHandler() != null) {
            this.mCurrentModule.getHandler().sendEmptyMessage(35);
        }
    }

    public void onHideModeMenuEnd(boolean notifyModule) {
        if (this.mCurrentModule != null && this.mCurrentModule.getHandler() != null && notifyModule) {
            this.mCurrentModule.getHandler().sendEmptyMessage(37);
        }
    }

    public void onHideMultiviewMenuEnd() {
        if (this.mCurrentModule != null && this.mCurrentModule.getHandler() != null) {
            this.mCurrentModule.getHandler().sendEmptyMessage(47);
        }
    }

    public void onShowChildSettingEnd() {
        if (this.mCurrentModule != null && this.mCurrentModule.getHandler() != null) {
            this.mCurrentModule.getHandler().sendEmptyMessage(87);
        }
    }

    public void onRemoveChildSettingEnd() {
        if (this.mCurrentModule != null && this.mCurrentModule.getHandler() != null) {
            this.mCurrentModule.getHandler().sendEmptyMessage(88);
        }
    }

    public void showDialogPopup(int id) {
        if (this.mCurrentModule != null) {
            showDialogPopup(id, true);
        }
    }

    public void showDialogPopup(int id, boolean showAnim) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.showDialog(id, showAnim);
        }
    }

    public void updateIndicator(int id, int intParam, boolean boolParam) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.updateIndicator(id, intParam, boolParam);
        }
    }

    public boolean isRotateDialogVisible() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isRotateDialogVisible();
        }
        return false;
    }

    public void removeRotateDialog() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.removeRotateDialog();
        }
    }

    public void onBackPressed() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.doBackButtonPress(false);
        }
    }

    public void launchGallery(Uri uri, int galleryPlayType) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.launchGallery(uri, galleryPlayType);
        }
    }

    public void setLaunchGalleryLocation(float[] loc) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setLaunchGalleryLocation(loc);
        }
    }

    public boolean checkFocusStateForChangingSetting() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.checkFocusStateForChangingSetting();
        }
        return false;
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.doCleanView(doByAction, useAnimation, saveState);
        }
    }

    public boolean isStorageRemoved(int storageType) {
        if (this.mCurrentModule == null) {
            return true;
        }
        return this.mCurrentModule.isStorageRemoved(storageType);
    }

    public void doInitSettingOrder() {
        if (this.mSettingManager != null) {
            this.mSettingManager.initSettingOrder();
            this.mSettingManager.removeUselessSetting();
        }
    }

    public boolean checkModuleValidate(int checkType) {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.checkModuleValidate(checkType);
        }
        return false;
    }

    protected void finalize() throws Throwable {
        sAPP_CAMERA_INSTANCE_COUNT--;
        CamLog.m3d(CameraConstants.TAG, "destroy CAMERA app_instance_cnt = " + sAPP_CAMERA_INSTANCE_COUNT);
        super.finalize();
    }

    public void onSDcardInserted() {
        if (this.mCurrentModule.getCameraState() == 6) {
            this.mCurrentModule.onVideoStopClicked(false, false);
        }
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.restartLoader();
        }
    }

    public void setQuickButtonSelected(int id, boolean selected) {
        if (this.mCurrentModule != null) {
            if (selected) {
                this.mCurrentModule.setQuickButtonIndex(id, 1);
            } else {
                this.mCurrentModule.setQuickButtonIndex(id, 0);
            }
            this.mCurrentModule.setQuickButtonSelected(id, selected);
        }
    }

    public void cameraPolicyChanged() {
        if (!MDMUtil.allowCamera(this) || !CheckStatusManager.checkDevicePolicy(this)) {
            finish();
        }
    }

    public void changeSettingValueInMdm() {
        if (MDMUtil.allowMicrophone()) {
            this.mCurrentModule.restoreSettingValueInMdm();
        } else {
            this.mCurrentModule.disableSettingValueInMdm();
        }
    }

    public void doBLEOneKeyAction(boolean isShortKey) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.doBLEOneKeyAction(isShortKey);
        }
    }

    public void doBackKey() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.doBackKey();
        }
        stopQuickShotModeByBackkey();
    }

    public void stopQuickShotModeByBackkey() {
        AppControlUtil.setQuickShotMode(false);
    }

    public boolean isTimerShotCountdown() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isTimerShotCountdown();
        }
        return false;
    }

    public boolean isNeedToCheckFlashTemperature() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isNeedToCheckFlashTemperature();
        }
        return false;
    }

    public void setFlashOffByHighTemperature(boolean isSetParam) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setFlashOffByHighTemperature(isSetParam);
        }
    }

    public boolean isStartedFromQuickCover() {
        return false;
    }

    public boolean isScreenPinningState() {
        ActivityManager am = (ActivityManager) getSystemService("activity");
        if (am == null || !am.isInLockTaskMode()) {
            return false;
        }
        return true;
    }

    public void onPreviewCreated(Object previewView, int width, int height) {
        CamLog.m3d(CameraConstants.TAG, "-hybrid- onPreviewCreated");
        CamLog.traceEnd(TraceTag.OPTIONAL, "Prepare_View", 5);
        this.mTextureState = 1;
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onTextureAvailable(null, width, height);
        }
    }

    public void onPreviewChanged(Object previewView, int width, int height) {
        CamLog.m3d(CameraConstants.TAG, "-hybrid- onPreviewChanged");
        if (previewView instanceof SurfaceTexture) {
            if (this.mCurrentModule != null) {
                this.mCurrentModule.onTextureSizeChanged((SurfaceTexture) previewView, width, height);
            }
        } else if (previewView instanceof SurfaceHolder) {
            if (this.mHybridView.isReadyPreviewBuffer()) {
                this.mWaitPreviewBufferConfig.open();
            }
            if (this.mCurrentModule != null) {
                this.mCurrentModule.onSurfaceChanged((SurfaceHolder) previewView, width, height);
            }
        }
    }

    public boolean onPreviewDestroyed(Object previewView) {
        CamLog.m3d(CameraConstants.TAG, "-hybrid- onPreviewDestroyed");
        this.mTextureState = 0;
        if (previewView instanceof SurfaceTexture) {
            if (this.mCurrentModule != null) {
                return this.mCurrentModule.onTextureDestroyed((SurfaceTexture) previewView);
            }
            return false;
        } else if (this.mCurrentModule != null) {
            return this.mCurrentModule.onSurfaceDestroyed((SurfaceHolder) previewView);
        } else {
            return false;
        }
    }

    public void onPreviewUpdated(Object previewView) {
        if (this.mTextureState == 1) {
            CamLog.m3d(CameraConstants.TAG, "onTextureUpdated");
            this.mTextureState = 2;
        }
        if ((previewView instanceof SurfaceTexture) && this.mCurrentModule != null) {
            this.mCurrentModule.onTextureUpdated((SurfaceTexture) previewView);
        }
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mHybridView.getSurfaceHolder();
    }

    public Object getPreviewSurface() {
        if (HybridViewConfig.SURFACE.equals(getCurrentViewType())) {
            return getSurfaceHolder();
        }
        return getSurfaceTexture();
    }

    public CameraHybridView getHybridView() {
        return this.mHybridView;
    }

    public int getCameraId() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.getCameraId();
        }
        return SharedPreferenceUtil.getCameraId(getAppContext());
    }

    public boolean isRearCamera() {
        boolean z = true;
        if (this.mCurrentModule == null) {
            if (SharedPreferenceUtil.getCameraId(getAppContext()) != 0) {
                z = false;
            }
            return z;
        } else if (this.mCurrentModule.getCameraId() == 0 || (FunctionProperties.getCameraTypeRear() == 1 && this.mCurrentModule.getCameraId() == 2)) {
            return true;
        } else {
            return false;
        }
    }

    public SurfaceView getSurfaceView() {
        if (this.mHybridView != null) {
            return this.mHybridView.getSurfaceView();
        }
        CamLog.m11w(CameraConstants.TAG, "mHybridView has not been generated yet.");
        return null;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return true;
    }

    public void onFrameRateListRefreshed(String previous, String next) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onFrameRateListRefreshed(previous, next);
        }
    }

    public void setAnimationShowing(boolean isShowing) {
        this.mAnimationShowing = isShowing;
    }

    public boolean isAnimationShowing() {
        return this.mAnimationShowing;
    }

    public String getShotMode() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.getShotMode();
        }
        return "";
    }

    public void setPreviewCallbackAll(boolean set) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setPreviewCallbackAll(set);
        }
    }

    public void showCameraDialog(int dialogId) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.showDialog(dialogId);
        }
    }

    public boolean isRecordingPriorityMode() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isRecordingPriorityMode();
        }
        return false;
    }

    public void onBTConnectionStateChanged(boolean connect) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onBTConnectionStateChanged(connect);
        }
    }

    public void onBTStateChanged(boolean isOn) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onBTStateChanged(isOn);
        }
    }

    public void onBTAudioConnectionStateChanged(boolean connect) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onBTAudioConnectionStateChanged(connect);
        }
    }

    public void onDualConnectionDeviceTypeChanged(int selectedBtn) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.onDualConnectionDeviceTypeChanged(selectedBtn);
        }
    }

    public void changeMultiviewLayout(String value) {
        CamLog.m3d(CameraConstants.TAG, "changeMultiviewLayout value = " + value);
        if (CameraConstants.MODE_MULTIVIEW.equals(this.mCurrentModule.getShotMode()) || CameraConstants.MODE_SNAP.equals(this.mCurrentModule.getShotMode())) {
            this.mCurrentModule.changeLayoutOnMultiview(value);
        }
    }

    public boolean isRequestedSingleImage() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isRequestedSingleImage();
        }
        return false;
    }

    public void removeUIBeforeModeChange() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.removeUIBeforeModeChange();
        }
    }

    public String getBeforeMode() {
        return this.mBeforeMode;
    }

    public void setBeforeMode(String mode) {
        this.mBeforeMode = mode;
    }

    public void setCameraChangingOnSnap(boolean isChanging) {
        CamLog.m3d(CameraConstants.TAG, "-swap- setCameraChangingOnSnap = " + isChanging);
        this.mCameraChangingOnSnap = isChanging;
    }

    public boolean isCameraChangingOnSnap() {
        CamLog.m3d(CameraConstants.TAG, "-swap- isCameraChangingOnSnap = " + this.mCameraChangingOnSnap);
        return this.mCameraChangingOnSnap;
    }

    public void setCameraChangingOnFlashJumpCut(boolean isChanging) {
        CamLog.m3d(CameraConstants.TAG, "-swap- setCameraChangingOnFlashJumpCut = " + isChanging);
        this.mCameraChangingOnFlashJumpCut = isChanging;
    }

    public boolean isCameraChangingOnFlashJumpCut() {
        CamLog.m3d(CameraConstants.TAG, "-swap- isCameraChangingOnFlashJumpCut = " + this.mCameraChangingOnFlashJumpCut);
        return this.mCameraChangingOnFlashJumpCut;
    }

    public void setCameraChangingOnSquareSnap(boolean isChanging) {
        CamLog.m3d(CameraConstants.TAG, "-swap- mCameraChangingOnSquareSnap = " + isChanging);
        this.mCameraChangingOnSquareSnap = isChanging;
    }

    public boolean isCameraChangingOnSquareSnap() {
        CamLog.m3d(CameraConstants.TAG, "-swap- mCameraChangingOnSquareSnap = " + this.mCameraChangingOnSquareSnap);
        return this.mCameraChangingOnSquareSnap;
    }

    public boolean isMenuShowing(int menuType) {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.isMenuShowing(menuType);
    }

    public int getFilmState() {
        if (this.mCurrentModule == null) {
            return 0;
        }
        return this.mCurrentModule.getFilmState();
    }

    public boolean isManualMode() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isManualMode();
        }
        return false;
    }

    public boolean updatePictureSizeListPreference(ListPreference listPref, String[] supportedPictureSize, String defaultPictureSize, int cameraId, String shotMode) {
        if (this.mSettingManager == null) {
            return false;
        }
        return this.mSettingManager.updatePictureSizeListPreference(listPref, supportedPictureSize, defaultPictureSize, cameraId, shotMode);
    }

    public void updateSpecificSettingMenu(String key, String[] entryValues, String[] entreis, int selectedPosition) {
        if (this.mSettingManager != null) {
            this.mSettingManager.updateSpecificSettingManue(key, entryValues, entreis, selectedPosition);
        }
    }

    protected void locationPermissionGranted() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.locationPermissionGranted();
        }
    }

    protected void showLocationToast() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.showLocationToast();
        }
    }

    public void updateLocationSwitchButton() {
        if (this.mSettingManager != null) {
            this.mSettingManager.updateLocationSwitchButton();
        }
    }

    public void onCallLocalHelp() {
        this.mCurrentModule.getHandler().sendEmptyMessage(48);
    }

    public CameraProxy getCameraDevice() {
        return this.mCurrentModule.getCameraDevice();
    }

    public boolean canUseTrackingAF() {
        CamLog.m3d(CameraConstants.TAG, "canUseTrackingAF");
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.useAFTrackingModule();
        }
        return false;
    }

    public void refreshSetting() {
        if (this.mSettingManager != null) {
            this.mSettingManager.refreshSetting();
        }
    }

    public void showTilePreviewCoverView(boolean show) {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.showTilePreviewCoverView(show);
        }
    }

    public String getSettingDesc(String key) {
        if (this.mSettingManager != null) {
            return this.mSettingManager.getSettingDesc(key);
        }
        return null;
    }

    public void startSelfieEngine() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.startSelfieEngine();
        }
    }

    public void stopSelfieEngin() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.stopSelfieEngin();
        }
    }

    public boolean isSnapShotProcessing() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isSnapShotProcessing();
        }
        return false;
    }

    public void hideHelpList(boolean showAnimation, boolean onlyHideMenu) {
        restoreTilePreviewSetting();
        super.hideHelpList(showAnimation, onlyHideMenu);
        if (this.mCurrentModule == null) {
        }
    }

    protected void restoreTilePreviewSetting() {
        if (FunctionProperties.isSupportedCameraRoll() && isHelpListVisible()) {
            String shotMode = getShotMode();
            if (shotMode != null) {
                String previewSize = getSettingValue(SettingKeyWrapper.getPictureSizeKey(shotMode, getCameraId()));
                if (this.mCurrentModule.isAvailableTilePreview() && RatioCalcUtil.getRatio(previewSize) != 2.0f && !CameraConstants.STORAGE_NAME_NAS.equals(getSettingValue(Setting.KEY_STORAGE))) {
                    this.mCurrentModule.restoreSettingValue(Setting.KEY_TILE_PREVIEW);
                    this.mCurrentModule.setSettingMenuEnable(Setting.KEY_TILE_PREVIEW, true);
                    this.mCurrentModule.setTilePreviewLayout("on".equals(getCurSettingValue(Setting.KEY_TILE_PREVIEW)));
                }
            }
        }
    }

    public boolean onHideMenu(int menuType) {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.onHideMenu(menuType);
    }

    public boolean checkFocusingStateWithFlash() {
        if (this.mCurrentModule == null) {
            return false;
        }
        if ("off".equals(getCurSettingValue("flash-mode")) || this.mCurrentModule.checkFocusStateForChangingSetting()) {
            return true;
        }
        return false;
    }

    public boolean isFocusTrackingSupported() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.isFocusTrackingSupported();
        }
        return false;
    }

    public boolean isOpticZoomSupported(String specificMode) {
        if (!FunctionProperties.isSupportedOpticZoom() || this.mCurrentModule == null || this.mModuleClassNameMap == null || !isRearCamera()) {
            return false;
        }
        if (specificMode == null) {
            return OpticZoomConfig.isOpticZoomSupported(this.mCurrentModule.getClass().getName());
        }
        return OpticZoomConfig.isOpticZoomSupported((String) this.mModuleClassNameMap.get(specificMode));
    }

    public String getCurDir() {
        if (this.mCurrentModule.mStorageManager == null) {
            return "";
        }
        return this.mCurrentModule.mStorageManager.getDir(this.mCurrentModule.getCurStorage());
    }

    public ArrayList<String> getDirPath(boolean includeCNAS) {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.getAllDir(includeCNAS);
        }
        return null;
    }

    public void removeFileFromTilePreview() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] removeFileFromTilePreview");
        if (this.mCurrentModule.mReviewThumbnailManager != null) {
            this.mCurrentModule.mReviewThumbnailManager.updateThumbnail(true);
        }
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setGifVisibleStatus(false);
            this.mCurrentModule.setGIFVisibility(false);
        }
    }

    public void updateQuickClipForTilePreview(boolean isCircleView, boolean isTilePreviewOn, Uri uri) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.updateQuickClipForTilePreview(isCircleView, isTilePreviewOn, uri);
        }
    }

    public void showTilePreview(boolean enable) {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.showTilePreview(enable);
        }
    }

    public void turnOnTilePreview(boolean isOn) {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.turnOnTilePreview(isOn);
        }
    }

    public void setThumbnailListEnable(boolean enable) {
        if (this.mThumbnailListManager != null) {
            this.mThumbnailListManager.setThumbnailListEnable(enable);
        }
    }

    public void showThumbnailListDetailView(boolean show, boolean isEmpty, float z) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] showThumbnailListDetailView show : " + show + " z : " + z);
        this.mCurrentModule.mQuickClipManager.setQuickClipLayoutDepth(show, z);
        if (show) {
            this.mCurrentModule.hideMenu(CameraConstants.MENU_TYPE_ALL, false, false);
            this.mCurrentModule.stopPreviewForShowDetailView();
            if (!isRearCamera()) {
                this.mCurrentModule.stopMotionEngine();
                stopSelfieEngin();
                this.mCurrentModule.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
            }
            this.mCurrentModule.setFilmStrengthButtonVisibility(false, false);
            if (this.mCurrentModule.mFocusManager != null) {
                this.mCurrentModule.mFocusManager.hideAndCancelAllFocus(false);
                this.mCurrentModule.mFocusManager.resetEVValue(0);
            }
            if (this.mCurrentModule.mBinningManager != null) {
                this.mCurrentModule.handleBinningIconUI(false, 2);
                return;
            }
            return;
        }
        if (!isRearCamera()) {
            startSelfieEngine();
        }
        this.mCurrentModule.setFilmStrengthButtonVisibility(true, false);
        if (isEmpty) {
            this.mCurrentModule.mReviewThumbnailManager.setThumbnailDefault(false, false);
            this.mCurrentModule.mReviewThumbnailManager.setEnabled(false);
            this.mCurrentModule.setQuickClipIcon(true, false);
        } else {
            this.mCurrentModule.mReviewThumbnailManager.updateThumbnail(this.mCurrentModule.mReviewThumbnailManager.isNeedThumbnailAnimation());
        }
        if (this.mCurrentModule.mBinningManager != null) {
            this.mCurrentModule.handleBinningIconUI(true, 0);
        }
    }

    public void showSettingMenu(boolean direct) {
        super.showSettingMenu(direct);
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setFingerDetectionListener(false);
        }
    }

    public void removeSettingMenu(boolean direct, boolean onlyHideMenu) {
        super.removeSettingMenu(direct, onlyHideMenu);
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setFingerDetectionListener(true);
        }
    }

    public void showModeMenu(boolean useAnim) {
        super.showModeMenu(useAnim);
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setFingerDetectionListener(false);
        }
    }

    public void hideModeMenu(boolean showAnimation, boolean onlyHideMenu) {
        super.hideModeMenu(showAnimation, onlyHideMenu);
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setFingerDetectionListener(true);
        }
    }

    public void showModeDeleteDialog(ModeItem item) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.showModeDeleteDialog(item);
        }
    }

    public void closeDetailViewAfterStartPreview() {
        this.mCurrentModule.closeDetailViewAfterStartPreview();
    }

    public boolean checkInterval(int checkType) {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.checkInterval(checkType);
    }

    public void deleteOrUndo(Uri uri, String burstId, UndoInterface listener) {
        if (this.mCurrentModule != null && this.mCurrentModule.mUndoManager != null) {
            if (!(this.mCurrentModule == null || this.mCurrentModule.mReviewThumbnailManager == null)) {
                this.mCurrentModule.mReviewThumbnailManager.checkLastThumbnailDeleted(uri);
            }
            this.mCurrentModule.mUndoManager.deleteOrUndo(uri, burstId, listener);
        }
    }

    public boolean checkUndoCurrentState(int state) {
        if (this.mCurrentModule == null || this.mCurrentModule.mUndoManager == null) {
            return false;
        }
        return this.mCurrentModule.mUndoManager.checkCurrentState(state);
    }

    public void deleteImmediatelyNotUndo() {
        if (this.mCurrentModule != null && this.mCurrentModule.mUndoManager != null) {
            this.mCurrentModule.mUndoManager.deleteImmediately();
        }
    }

    public void quickClipDrawerClose(boolean isAnimation) {
        if (this.mCurrentModule != null && this.mCurrentModule.mQuickButtonManager != null) {
            this.mCurrentModule.mQuickClipManager.drawerClose(isAnimation);
        }
    }

    public boolean getBurstProgress() {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.getBurstProgress();
    }

    public boolean isActivityPaused() {
        return this.mPaused;
    }

    public boolean getGifVisibleStatus() {
        if (this.mCurrentModule != null) {
            return this.mCurrentModule.getGifVisibleStatus();
        }
        return false;
    }

    public void setGifVisibleStatus(boolean status) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setGifVisibleStatus(status);
        }
    }

    public void setGIFVisibility(boolean visible) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.setGIFVisibility(visible);
        }
    }

    public void updateThumbnail(boolean useAni) {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.updateThumbnail(useAni);
        }
    }

    public void setPhotoSizeHelpShown(boolean photoSizeHelpShown) {
        this.mIsPhotoSizeHelpShown = photoSizeHelpShown;
    }

    public boolean getPhotoSizeHelpShown() {
        return this.mIsPhotoSizeHelpShown;
    }

    public void removeStopPreviewMessage() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.removeStopPreviewMessage();
        }
    }

    public boolean isActivatedQuickview() {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.isActivatedQuickview();
    }

    public Uri getDeletedUri() {
        return this.mDeletedUri;
    }

    public void setDeletedUriFromGallery(Uri deletedUri) {
        this.mDeletedUri = deletedUri;
    }

    public boolean isVideoCameraMode() {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.isVideoCaptureMode();
    }

    public boolean isJogZoomMoving() {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.isJogZoomMoving();
    }

    public boolean isZoomControllerTouched() {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.isZoomControllerTouched();
    }

    public boolean isShutterZoomSupported() {
        if (this.mCurrentModule == null) {
            return false;
        }
        return this.mCurrentModule.isShutterZoomSupported();
    }

    public CameraCapabilities getCameraCapabilities() {
        if (this.mCurrentModule == null) {
            return null;
        }
        return this.mCurrentModule.getCameraCapabilities();
    }

    public Handler getModuleHandler() {
        if (this.mCurrentModule == null) {
            return null;
        }
        return this.mCurrentModule.getHandler();
    }

    public boolean setCameraPreviewSize(final int width, final int height) {
        if (FunctionProperties.getSupportedHal() != 2 || this.mHybridView == null) {
            return true;
        }
        if (!this.mHybridView.isNeedConfigCameraPreviewSize(width, height)) {
            return this.mHybridView.isReadyPreviewBuffer();
        }
        this.mHybridView.setReadyPreviewBufferFlag(false);
        runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CameraActivity.this.mHybridView != null && !CameraActivity.this.isPaused()) {
                    CameraActivity.this.mHybridView.setCameraPreviewSize(width, height);
                }
            }
        });
        return false;
    }

    public void waitConfigPreviewBuffer() {
        if (this.mHybridView != null && !this.mHybridView.isReadyPreviewBuffer() && !isPaused()) {
            CamLog.m7i(CameraConstants.TAG, "preview lock ");
            this.mWaitPreviewBufferConfig.close();
            this.mWaitPreviewBufferConfig.block(CameraConstants.TOAST_LENGTH_SHORT);
            CamLog.m7i(CameraConstants.TAG, "preview open ");
        }
    }

    public void setAssistantFlag(String key, Object value) {
        if (this.mVoiceAssistantManager != null) {
            this.mVoiceAssistantManager.setFlag(key, value);
        }
    }

    public boolean getAssistantBoolFlag(String key, boolean defaultValue) {
        if (this.mVoiceAssistantManager == null) {
            return defaultValue;
        }
        return this.mVoiceAssistantManager.getBooleanFlag(key, defaultValue);
    }

    public int getAssistantIntFlag(String key, int defaultValue) {
        if (this.mVoiceAssistantManager == null) {
            return defaultValue;
        }
        return this.mVoiceAssistantManager.getIntFlag(key, defaultValue);
    }

    public String getAssistantStringFlag(String key, String defaultValue) {
        if (this.mVoiceAssistantManager == null) {
            return defaultValue;
        }
        return this.mVoiceAssistantManager.getStringFlag(key, defaultValue);
    }

    public boolean isVoiceAssistantSpecified() {
        if (this.mVoiceAssistantManager == null) {
            return false;
        }
        return this.mVoiceAssistantManager.isAssistantSpecified();
    }

    public boolean isAssistantImageIntent() {
        return isStillImageCameraIntent() || CameraConstants.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(getIntent().getAction());
    }

    public boolean isAssistantVideoIntent() {
        return isVideoCameraIntent() || CameraConstants.INTENT_ACTION_VIDEO_CAMERA_SECURE.equals(getIntent().getAction());
    }

    public void clearAllAssistantFlag() {
        if (this.mVoiceAssistantManager != null) {
            this.mVoiceAssistantManager.clearAllFlags();
        }
    }

    public boolean onOutfocusModeClicked(boolean isOn) {
        if (isRearCamera()) {
            if (FunctionProperties.isSupportedRearOutfocus()) {
                setCameraIdBeforeChange(false, 0, true);
            } else {
                showToast("Not implementation!", CameraConstants.TOAST_LENGTH_SHORT);
                return false;
            }
        }
        if (FunctionProperties.isSupportedFrontOutfocus() || isRearCamera()) {
            if (isOn) {
                this.mCurrentModule.stopSticker(false);
            }
            changeModule(true);
            return true;
        }
        showToast("Not implementation!", CameraConstants.TOAST_LENGTH_SHORT);
        return false;
    }

    public boolean onSmartCamModeClicked(boolean isOn) {
        if (isOn) {
            this.mCurrentModule.stopSticker(false);
        }
        if (isRearCamera()) {
            setCameraIdBeforeChange(false, SharedPreferenceUtil.getRearCameraId(getAppContext()), true);
        }
        changeModule(true);
        return true;
    }

    public void updateButtonBySetting(String key) {
        if (this.mSettingManager != null) {
            this.mSettingManager.updateButtonBySetting(key);
        }
    }

    public boolean checkUSPZoneAvailable() {
        if (this.mCurrentModule != null && this.mCurrentModule.checkUspZoneAvailable()) {
            return true;
        }
        return false;
    }

    public void refreshUspZone(boolean isCameraSwitching) {
        if (this.mUspZoneManager != null) {
            this.mUspZoneManager.refreshUspZone(isCameraSwitching);
        }
    }

    public boolean isNightVisionGuideShown() {
        return this.mIsNightVisionGuideShown;
    }

    public void setNightVisionGuideShown() {
        this.mIsNightVisionGuideShown = true;
    }

    public boolean isStickerGuideShown() {
        return this.mIsStickerGuideShown;
    }

    public void setIsStickerGuideShown() {
        this.mIsStickerGuideShown = true;
    }

    public TimeIntervalChecker getTimeIntervalChecker() {
        return this.mCurrentModule == null ? null : this.mCurrentModule.mIntervalChecker;
    }

    public void stopPreview() {
        if (this.mCurrentModule != null) {
            this.mCurrentModule.stopPreview();
        }
    }
}
