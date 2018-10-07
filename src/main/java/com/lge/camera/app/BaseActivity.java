package com.lge.camera.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardDismissCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.lge.app.permission.RequestPermissionsHelper;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraHolder;
import com.lge.camera.managers.HelpInterface;
import com.lge.camera.managers.VoiceAssistantManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CameraPermissionUiProvider;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.HandlerManager;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import com.lge.view.MotionPocket;

public abstract class BaseActivity extends Activity implements ActivityBridge, OnRemoveHandler, HelpInterface {
    public static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 100;
    protected static int sAPP_CAMERA_INSTANCE_COUNT = 0;
    protected boolean mAnimationShowing = false;
    protected int mCameraChanging = 0;
    protected boolean mCameraChangingOnSnap = false;
    private CameraOpenThreadOnCreate mCameraOpenThreadOnCreate;
    protected int mFromVolumeKey = 0;
    protected HandlerManager mHandlerManager = new HandlerManager();
    protected boolean mIgnoreRequestPermissionsIfNeededCall = false;
    protected boolean mIsCallAnswering = false;
    protected boolean mIsCallPopupShowing = false;
    private boolean mIsCaptureIntent = false;
    protected boolean mIsCheckingPermission = false;
    protected boolean mIsEnteringDirectGraphyMode = false;
    private boolean mIsFromLGUOEMCamera = false;
    private boolean mIsFromSmartTips = false;
    protected boolean mIsHidingNaviBar = false;
    protected boolean mIsJustGetWindowFocus = false;
    protected boolean mIsLocationOnByCamera = false;
    private boolean mIsMMSIntent = false;
    protected boolean mIsRegisterSystemUIListener = false;
    protected boolean mModuleChanging = false;
    public HandlerRunnable mNaviHider = new HandlerRunnable(this) {
        public void handleRun() {
            BaseActivity.this.setNaviBarVisibility(false, 0);
        }
    };
    protected Menu mOptionMenu = null;
    protected boolean mPaused = false;
    protected PhoneStateListener mPhoneStateListener = null;
    protected boolean mProcessFinish = false;
    protected boolean mResumeAfterProcessingDone = false;
    protected boolean mReturnFromHelp = false;
    protected VoiceAssistantManager mVoiceAssistantManager = new VoiceAssistantManager(this);
    protected WakeLock mWakeLock = null;
    private boolean misFromGraphyApp = false;

    /* renamed from: com.lge.camera.app.BaseActivity$2 */
    class C02692 implements OnTouchModeChangeListener {
        C02692() {
        }

        public void onTouchModeChanged(boolean isInTouchMode) {
            BaseActivity.this.handleTouchModeChanged(isInTouchMode);
        }
    }

    /* renamed from: com.lge.camera.app.BaseActivity$4 */
    class C02714 extends PhoneStateListener {
        C02714() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            BaseActivity.this.doPhoneStateListenerAction(state);
        }
    }

    /* renamed from: com.lge.camera.app.BaseActivity$5 */
    class C02725 implements OnSystemUiVisibilityChangeListener {
        C02725() {
        }

        public void onSystemUiVisibilityChange(int visibility) {
            CamLog.m7i(CameraConstants.TAG, "visibility : " + visibility);
            if (visibility == 0) {
                BaseActivity.this.setCaptureButtonEnableByNaviBar(false);
                if (!BaseActivity.this.mIsHidingNaviBar) {
                    BaseActivity.this.setNaviBarVisibility(true, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                    return;
                }
                return;
            }
            BaseActivity.this.setCaptureButtonEnableByNaviBar(true);
            if (!BaseActivity.this.mIsHidingNaviBar && !BaseActivity.this.mIsJustGetWindowFocus) {
                BaseActivity.this.setNaviBarVisibility(false, 0);
            }
        }
    }

    private class CameraOpenThreadOnCreate extends Thread {
        private volatile boolean mCancelled;

        private CameraOpenThreadOnCreate() {
        }

        /* synthetic */ CameraOpenThreadOnCreate(BaseActivity x0, C02681 x1) {
            this();
        }

        public void cancel() {
            CamLog.m3d(CameraConstants.TAG, "Camera Open cancel");
            this.mCancelled = true;
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "# CameraOpenThreadOnCreate-run");
            if (BaseActivity.this.isPaused()) {
                CamLog.m3d(CameraConstants.TAG, "EXIT by pausing");
            } else if (CheckStatusManager.checkEnterApplication(BaseActivity.this, false)) {
                int cameraId;
                if (BaseActivity.this.getAssistantBoolFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)) {
                    CamLog.m3d(CameraConstants.TAG, "frontCamera Specified!");
                    SharedPreferenceUtilBase.setCameraId(BaseActivity.this.getAppContext(), 1);
                } else if (BaseActivity.this.getAssistantBoolFlag(CameraConstantsEx.FLAG_USE_REAR_CAMERA, false)) {
                    CamLog.m3d(CameraConstants.TAG, "rearCamera Specified!");
                    boolean isWide = CameraConstantsEx.FLAG_VALUE_WIDE_CAMERA.equals(BaseActivity.this.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null));
                    if (FunctionProperties.getCameraTypeRear() != 1) {
                        isWide = false;
                    }
                    if (isWide) {
                        cameraId = 2;
                    } else {
                        cameraId = SharedPreferenceUtil.getRearCameraId(BaseActivity.this.getAppContext());
                    }
                    SharedPreferenceUtilBase.setCameraId(BaseActivity.this.getAppContext(), cameraId);
                } else if (BaseActivity.this.isLGUOEMCameraIntent()) {
                    SharedPreferenceUtilBase.setCameraId(BaseActivity.this.getAppContext(), 0);
                } else {
                    BaseActivity.this.setCameraIdForSmartTips();
                }
                if (!FunctionProperties.isSupportedConeUI() || SharedPreferenceUtil.getLastCameraMode(BaseActivity.this.getAppContext(), 1) != 3) {
                    cameraId = BaseActivity.this.checkCameraId(BaseActivity.this.getSharedPreferenceCameraId());
                    if (cameraId == -1) {
                        CamLog.m3d(CameraConstants.TAG, "EXIT by invalid camera id or entering manual video shortcut");
                        return;
                    }
                    CameraHolder.instance().open(null, cameraId, null, true, BaseActivity.this);
                    if (CameraHolder.instance().isCameraOpened() && this.mCancelled) {
                        CameraHolder.instance().release();
                    }
                }
            } else {
                CamLog.m3d(CameraConstants.TAG, "EXIT by entering condition");
            }
        }
    }

    private class KeyguardDismissCallbackLocal extends KeyguardDismissCallback {
        private KeyguardDismissCallbackLocal() {
        }

        /* synthetic */ KeyguardDismissCallbackLocal(BaseActivity x0, C02681 x1) {
            this();
        }

        public void onDismissError() {
            CamLog.m3d(CameraConstants.TAG, "dismiss error");
        }

        public void onDismissSucceeded() {
            CamLog.m3d(CameraConstants.TAG, "dismiss success");
        }

        public void onDismissCancelled() {
            CamLog.m3d(CameraConstants.TAG, "dismiss cancel");
        }
    }

    public abstract void handleTouchModeChanged(boolean z);

    protected abstract void initActivityManager();

    protected abstract void initModuleOnCreate();

    protected abstract void locationPermissionGranted();

    protected abstract void makeModuleMap();

    protected abstract void setCaptureButtonEnableByNaviBar(boolean z);

    protected abstract void showLocationToast();

    public BaseActivity() {
        Log.i(CameraConstants.TAG, "[Time Info][1] Please check the Time besides CameraApp : Info Touch Recognition, Launcher, Memory Allocation Layout");
        CamLog.setUiThreadHashCode(Thread.currentThread().hashCode());
    }

    protected String getInitCameraMode() {
        String modeStr = "mode_normal";
        if (isVoiceAssistantSpecified()) {
            if (this.mVoiceAssistantManager != null) {
                modeStr = this.mVoiceAssistantManager.getAssistantInitMode();
            }
            return modeStr;
        } else if (isVideoCameraIntent()) {
            CamLog.m7i(CameraConstants.TAG, "[mode] set last camera mode : NORMAL_VIEW_MODE, modeStr = " + modeStr);
            SharedPreferenceUtilBase.saveLastCameraMode(this, 1);
            return modeStr;
        } else {
            if (!AppControlUtil.isQuickShotMode() && FunctionProperties.isSupportedConeUI()) {
                int lastCameraMode = SharedPreferenceUtil.getLastCameraMode(this, 1);
                if (lastCameraMode == 2) {
                    modeStr = CameraConstants.MODE_MANUAL_CAMERA;
                } else if (lastCameraMode == 3) {
                    modeStr = CameraConstants.MODE_MANUAL_VIDEO;
                } else if (lastCameraMode == 4) {
                    modeStr = CameraConstants.MODE_SNAP;
                }
            }
            if (isFromGraphyApp()) {
                modeStr = CameraConstants.MODE_MANUAL_CAMERA;
            }
            return modeStr;
        }
    }

    public boolean isEnteringDirectFromShortcut() {
        if (isFromGraphyApp()) {
            return true;
        }
        return false;
    }

    protected int checkCameraId(int cameraId) {
        if (!FunctionProperties.isSupportedOpticZoom()) {
            return cameraId;
        }
        if (cameraId != 0 && cameraId != 2) {
            return cameraId;
        }
        CamLog.m7i(CameraConstants.TAG, "cameraId  " + QuickWindowUtils.isQuickWindowCameraMode());
        return !QuickWindowUtils.isQuickWindowCameraMode() ? 3 : cameraId;
    }

    private void setCameraIdForSmartTips() {
        if (isSmartTipsIntent()) {
            String function = getIntent().getStringExtra(CameraConstants.CAMERA_FUNCTION);
            CamLog.m3d(CameraConstants.TAG, "smart tips key: " + function);
            if (function.equals(CameraConstants.GESTURE_KEY) || function.equals(CameraConstants.INTERVAL_KEY)) {
                SharedPreferenceUtilBase.setCameraId(getAppContext(), 1);
            } else {
                SharedPreferenceUtilBase.setCameraId(getAppContext(), 0);
            }
        }
    }

    protected void setFlagsByIntentAction() {
        Intent intent = getIntent();
        String action = intent.getAction();
        boolean z = "android.media.action.IMAGE_CAPTURE".equals(action) || "android.media.action.VIDEO_CAPTURE".equals(action) || CameraConstants.ACTION_IMAGE_CAPTURE_SECURE.equals(action) || CameraConstants.ACTION_LGUPLUS_OEM_CAMERA.equals(action);
        this.mIsCaptureIntent = z;
        this.mIsFromLGUOEMCamera = CameraConstants.ACTION_LGUPLUS_OEM_CAMERA.equals(action);
        if (!(intent.getStringExtra(CameraConstants.CAMERA_FUNCTION) == null || "".equals(intent.getStringExtra(CameraConstants.CAMERA_FUNCTION)))) {
            this.mIsFromSmartTips = true;
        }
        handleFromVolumeKeyIntent(true);
        if (isFromFloatingBar()) {
            this.mIsMMSIntent = false;
        } else if (isFromVolumeKey()) {
            this.mIsMMSIntent = false;
        } else if (intent.getIntExtra("MMSAttach", 0) == 1 || "MMSAttach".equals(intent.getStringExtra("intentFrom")) || isCallingPackage(CameraConstants.PACKAGE_ANDROID_MMS)) {
            this.mIsMMSIntent = true;
        }
        AppControlUtil.setQuickShotCondition(getActivity());
    }

    public boolean isFromFloatingBar() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        return intent.getBooleanExtra("startFromConeShort", false);
    }

    public boolean isStillImageCameraIntent() {
        return "android.media.action.STILL_IMAGE_CAMERA".equals(getIntent().getAction());
    }

    protected void onCreate(Bundle savedInstanceState) {
        if (this.mIsCheckingPermission) {
            requestPermissions();
            super.onCreate(savedInstanceState);
            return;
        }
        setFlagsByIntentAction();
        ConfigurationUtil.setConfiguration(getApplicationContext());
        makeModuleMap();
        this.mCameraOpenThreadOnCreate = new CameraOpenThreadOnCreate(this, null);
        this.mCameraOpenThreadOnCreate.start();
        SecureImageUtil.setSecureCamera(getActivity());
        super.onCreate(savedInstanceState);
        CamLog.setLogOn();
        CheckStatusManager.setCheckEnterOutSecure(0);
        CamLog.m3d(CameraConstants.TAG, "version info : " + Utils.getUserAgent(getAppContext()));
        if (!CheckStatusManager.checkEnterApplication(this, false)) {
            CamLog.m3d(CameraConstants.TAG, "onCreate()-end, checkEnterApplication fail.");
            if (SecureImageUtil.isSecureCameraIntent(getIntent())) {
                CheckStatusManager.setCheckEnterOutSecure(1);
            } else {
                this.mCameraOpenThreadOnCreate.cancel();
                try {
                    Thread.currentThread();
                    Thread.sleep(1000);
                    this.mCameraOpenThreadOnCreate.join();
                } catch (InterruptedException e) {
                    CamLog.m6e(CameraConstants.TAG, "onCreate() InterruptedException ", e);
                }
                CheckStatusManager.checkCameraOut(this, null, true);
                if (CameraHolder.instance().isCameraOpened()) {
                    CameraHolder.instance().release();
                    return;
                }
                return;
            }
        }
        FunctionProperties.checkFuntionProperty(getApplicationContext());
        MmsProperties.resetMMSResolutionList();
        initActivityManager();
        changeThemeForActionBar();
        requestWindowFeature(9);
        setWindowRotation();
        initModuleOnCreate();
        if (ModelProperties.isSoftKeyNavigationBarModel(this)) {
            SystemBarUtil.hideSystemUI(this);
        }
        SystemBarUtil.setActionBarAnim(getActivity(), false);
        SystemBarUtil.setActionBarVisible(getActivity(), false);
        setSystemUiVisibilityListener(true);
        setLocationOnByCamera(false);
        sendLdbOnEntrance();
    }

    private void sendLdbOnEntrance() {
        if (QuickWindowUtils.isQuickWindowCameraMode()) {
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, "QuickCover");
        } else if (isFromVolumeKey()) {
            if (AppControlUtil.isQuickShotMode()) {
                LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, "Volume-QuickshotOn");
            } else {
                LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, "Volume-QuickshotOff");
            }
        } else if (isFromCameraKey()) {
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, "CameraKey");
        } else if (isEnteringDirectFromShortcut()) {
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, "AppShotcut");
        } else if (isVoiceAssistantSpecified()) {
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, "VoiceAssistant");
        } else if (isAttachIntent()) {
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, "Attach");
        } else {
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_ENTRANCE, -1, LdbConstants.LDB_LOOP_RECORDING_NONE);
        }
    }

    private void requestPermissions() {
        if (this.mIgnoreRequestPermissionsIfNeededCall) {
            CamLog.m3d(CameraConstants.TAG, "Ignore requestPermissionsIfNeeded call");
        } else if (QuickWindowUtils.isQuickWindowCameraMode()) {
            CamLog.m3d(CameraConstants.TAG, "Quick window can't work as not granted permissions");
        } else if (SecureImageUtil.isSecureCameraIntent(getIntent()) || AppControlUtil.checkFromVolumeKey(getIntent())) {
            CamLog.m3d(CameraConstants.TAG, "Secure camera can't launch as not granted permissions");
            AppControlUtil.configureWindowFlag(getWindow(), false, true, true, true);
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, getClass().getName());
            this.mWakeLock.setReferenceCounted(false);
            if (!this.mWakeLock.isHeld()) {
                this.mWakeLock.acquire(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            }
            CheckStatusManager.setCheckCameraPermissions();
            CheckStatusManager.checkCameraOutByPermissionCheck(this, getHandler(), true);
        } else {
            CamLog.m3d(CameraConstants.TAG, "requestPermissions : " + this);
            RequestPermissionsHelper.requestPermissionsIfNeeded(this, CameraConstants.UI_REQUIRED_PERMISSIONS, new CameraPermissionUiProvider());
        }
    }

    protected void onStart() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onStart();
        } else {
            super.onStart();
        }
    }

    protected void dismissKeyguard() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService("keyguard");
        if (!keyguardManager.isKeyguardSecure() && keyguardManager.isKeyguardLocked()) {
            keyguardManager.requestDismissKeyguard(this, new KeyguardDismissCallbackLocal(this, null));
        }
    }

    public void onResume() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onResume();
            return;
        }
        setSystemUiVisibilityListener(true);
        if (ModelProperties.isSoftKeyNavigationBarModel(this)) {
            SystemBarUtil.setTranslucentNavigationBar(this);
            SystemBarUtil.disableNavigationButton(this);
            if (!isPostviewShowing() || getPostviewType() == 1) {
                SystemBarUtil.hideSystemUI(this);
            }
        }
        if (CheckStatusManager.getCheckEnterOutSecure() == 1) {
            CheckStatusManager.setCheckEnterOutSecure(2);
            if (CheckStatusManager.checkVTCallStatus(getActivity())) {
                CheckStatusManager.checkCameraOut(getActivity(), getHandler(), true);
            } else {
                CheckStatusManager.checkCameraOut(getActivity(), null, true);
            }
        }
        if (!isPostviewShowing() || getPostviewType() == 1) {
            SystemBarUtil.setActionBarAnim(getActivity(), false);
            SystemBarUtil.setActionBarVisible(getActivity(), false);
        }
        handleFromVolumeKeyIntent(true);
        getWakeLock();
        if (isFromVolumeKey() || AppControlUtil.checkFromBleKey(getIntent()) || AppControlUtil.isQuickTools(getIntent())) {
            getWindow().addFlags(6291456);
            dismissKeyguard();
        } else {
            setWakeLock(this.mWakeLock, true);
        }
        IntentBroadcastUtil.setFmRadioOff(getApplicationContext());
        super.onResume();
        final View view = findViewById(C0088R.id.camera_base);
        if (view != null) {
            ViewTreeObserver vto = view.getViewTreeObserver();
            if (vto != null) {
                vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (BaseActivity.this.isFromVolumeKey() || AppControlUtil.checkFromBleKey(BaseActivity.this.getIntent()) || AppControlUtil.isQuickTools(BaseActivity.this.getIntent())) {
                            BaseActivity.setWakeLock(BaseActivity.this.mWakeLock, true);
                            BaseActivity.setWakeLock(BaseActivity.this.mWakeLock, false);
                            PowerManager powerManager = (PowerManager) BaseActivity.this.getSystemService("power");
                            BaseActivity.this.mWakeLock = powerManager.newWakeLock(1, getClass().getName());
                            BaseActivity.this.mWakeLock.setReferenceCounted(false);
                            BaseActivity.setWakeLock(BaseActivity.this.mWakeLock, true);
                        }
                    }
                });
                vto.addOnTouchModeChangeListener(new C02692());
            }
        }
        if (CheckStatusManager.checkEnterApplication(this, true) || CheckStatusManager.getCheckEnterOutSecure() == 2) {
            this.mProcessFinish = false;
            if (Utils.checkOOS()) {
                AppControlUtil.blurRecentThumbnail((Activity) this, true);
                return;
            } else {
                AppControlUtil.blurRecentThumbnail(1, (Activity) this);
                return;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "onResume()-end, checkEnterApplication fail.");
        finish();
    }

    public void onPause() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onPause();
            return;
        }
        CheckStatusManager.setEnterCheckComplete(false);
        removePostRunnable(this.mNaviHider);
        setSystemUiVisibilityListener(false);
        if (!SecureImageUtil.useSecureLockImage() && ModelProperties.isSoftKeyNavigationBarModel(this)) {
            SystemBarUtil.showSystemUI(this);
        }
        if (isFromVolumeKey() || SecureImageUtil.isSecureCamera() || AppControlUtil.checkFromBleKey(getIntent()) || AppControlUtil.isQuickTools(getIntent()) || QuickWindowUtils.isQuickWindowCameraMode()) {
            CamLog.m3d(CameraConstants.TAG, "clear FLAG_TURN_SCREEN_ON");
            getWindow().clearFlags(2097152);
        }
        setWakeLock(this.mWakeLock, false);
        setPhoneStateListener(false);
        IntentBroadcastUtil.sendBroadcastIntentCameraEnded(this);
        if (this.mCameraOpenThreadOnCreate != null) {
            try {
                this.mCameraOpenThreadOnCreate.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mCameraOpenThreadOnCreate = null;
        }
        super.onPause();
    }

    public void onStop() {
        super.onStop();
        if (!this.mIgnoreRequestPermissionsIfNeededCall && !this.mIsCheckingPermission) {
            if (isFromVolumeKey()) {
                CamLog.m3d(CameraConstants.TAG, "[QuickCamera] onStop : clear flag - FLAG_DISMISS_KEYGUARD");
                getWindow().clearFlags(4718592);
            }
            PowerManager pm = (PowerManager) getSystemService("power");
            if (!(!AppControlUtil.isQuickTools(getIntent()) || pm == null || pm.isScreenOn())) {
                CamLog.m3d(CameraConstants.TAG, "[quicktools] reset quick tools flag");
                getWindow().clearFlags(4718592);
                getIntent().putExtra("quicktray", false);
            }
            handleFromVolumeKeyIntent(false);
        }
    }

    public void onDestroy() {
        if (this.mIgnoreRequestPermissionsIfNeededCall || this.mIsCheckingPermission) {
            super.onDestroy();
            return;
        }
        CheckStatusManager.setCheckEnterOutSecure(0);
        SecureImageUtil.get().release();
        super.onDestroy();
        if (AppControlUtil.isQuickTools(getIntent())) {
            getWindow().clearFlags(524288);
            getIntent().putExtra("quicktray", false);
        }
        handleFromVolumeKeyIntent(false);
        setWakeLock(this.mWakeLock, false);
        removePostAllRunnables();
        this.mHandlerManager = null;
        if (this.mOptionMenu != null) {
            this.mOptionMenu.clear();
            this.mOptionMenu = null;
        }
        this.mWakeLock = null;
        this.mNaviHider = null;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        CamLog.m3d(CameraConstants.TAG, "onWindowFocusChanged() hasFocus ? =" + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        if (!ModelProperties.isSoftKeyNavigationBarModel(this)) {
            return;
        }
        if (hasFocus) {
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (!BaseActivity.this.isPostviewShowing() || BaseActivity.this.getPostviewType() == 1) {
                        SystemBarUtil.hideSystemUI(BaseActivity.this.getActivity());
                    }
                }
            }, 500);
            return;
        }
        removePostRunnable(this.mNaviHider);
        this.mIsHidingNaviBar = false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public boolean isPaused() {
        return this.mPaused;
    }

    public boolean isResumeAfterProcessingDone() {
        return this.mResumeAfterProcessingDone;
    }

    public Context getAppContext() {
        return getApplicationContext();
    }

    public View layoutInflate(int resource, ViewGroup root) {
        return getLayoutInflater().inflate(resource, root);
    }

    public View findViewById(int resource) {
        return super.findViewById(resource);
    }

    public View inflateStub(int id) {
        ViewStub viewStub = (ViewStub) findViewById(id);
        if (viewStub != null) {
            View view = viewStub.inflate();
            CamLog.m7i(CameraConstants.TAG, "inflated view: " + view);
            return view;
        }
        CamLog.m7i(CameraConstants.TAG, "inflated stubView is null.");
        return null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.mOptionMenu = menu;
        getMenuInflater().inflate(C0088R.menu.postview_action_menu, menu);
        return true;
    }

    protected void changeThemeForActionBar() {
    }

    public void setupOptionMenu(int postView) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }
    }

    protected void doPhoneStateListenerAction(int state) {
    }

    protected void setPhoneStateListener(boolean isSet) {
        if (isSet) {
            if (this.mPhoneStateListener == null) {
                this.mPhoneStateListener = new C02714();
                ((TelephonyManager) getSystemService("phone")).listen(this.mPhoneStateListener, 32);
            }
        } else if (this.mPhoneStateListener != null) {
            ((TelephonyManager) getSystemService("phone")).listen(this.mPhoneStateListener, 0);
            this.mPhoneStateListener = null;
        }
    }

    public void runOnUiThread(Object action) {
        if (this.mHandlerManager != null && (action instanceof HandlerRunnable)) {
            this.mHandlerManager.runOnUiThread((HandlerRunnable) action);
        }
    }

    public void postOnUiThread(Object action) {
        if (this.mHandlerManager != null && (action instanceof HandlerRunnable)) {
            this.mHandlerManager.postOnUiThread((HandlerRunnable) action);
        }
    }

    public void postOnUiThread(Object action, long delay) {
        if (this.mHandlerManager != null && (action instanceof HandlerRunnable)) {
            this.mHandlerManager.postOnUiThread((HandlerRunnable) action, delay);
        }
    }

    public void removePostRunnable(Object object) {
        if (this.mHandlerManager != null) {
            this.mHandlerManager.removePostRunnable(object);
        }
    }

    public void removePostAllRunnables() {
        if (this.mHandlerManager != null) {
            this.mHandlerManager.removePostAllRunnables();
        }
    }

    public boolean hasRunnable(Object object) {
        if (this.mHandlerManager != null) {
            return this.mHandlerManager.hasRunnable(object);
        }
        return false;
    }

    public Handler getHandler() {
        if (this.mHandlerManager != null) {
            return this.mHandlerManager.getHandler();
        }
        return new Handler();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CamLog.m3d(CameraConstants.TAG, "-c1- onActivityResult requestCode=" + requestCode + "/resultCode=" + resultCode);
        switch (requestCode) {
            case 1:
                resultVideoEditor(resultCode, data);
                return;
            case 5:
                resultSignature(resultCode, data);
                return;
            case 6:
            case 7:
                loadImageResult(requestCode, resultCode, data);
                return;
            case 1000:
                resultFromGraphy(resultCode, data);
                return;
            default:
                return;
        }
    }

    private void loadImageResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 6:
                if (resultCode == -1 && data != null) {
                    resultFromPickImage(data);
                    return;
                } else if (resultCode == 0) {
                    resultFromCancel();
                    return;
                } else {
                    return;
                }
            case 7:
                if (resultCode == -1 && data != null) {
                    resultFromCropImage(data);
                    return;
                } else if (resultCode == 0) {
                    resultFromCancel();
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    protected void resultVideoEditor(int resultCode, Intent data) {
    }

    protected void resultFromPickImage(Intent data) {
    }

    protected void resultFromCropImage(Intent data) {
    }

    protected void resultFromCancel() {
    }

    protected void resultSignature(int resultCode, Intent data) {
    }

    protected void resultFromGraphy(int resultCode, Intent data) {
    }

    public boolean isAttachIntent() {
        return this.mIsCaptureIntent;
    }

    public boolean isMMSIntent() {
        return this.mIsMMSIntent;
    }

    public boolean isCallingPackage(String packageName) {
        String callingPackage = getCallingPackage();
        if (callingPackage == null || packageName == null || !callingPackage.equals(packageName)) {
            return false;
        }
        return true;
    }

    public void setSmartTipsIntent(boolean isFromSmartTips) {
        this.mIsFromSmartTips = isFromSmartTips;
    }

    public boolean isSmartTipsIntent() {
        return this.mIsFromSmartTips;
    }

    public boolean isLGUOEMCameraIntent() {
        return this.mIsFromLGUOEMCamera;
    }

    public boolean isVideoCaptureMode() {
        return "android.media.action.VIDEO_CAPTURE".equals(getIntent().getAction());
    }

    public boolean isVideoCameraIntent() {
        return "android.media.action.VIDEO_CAMERA".equals(getIntent().getAction());
    }

    public void setFromGraphyFlag(boolean flag) {
        this.misFromGraphyApp = flag;
    }

    public boolean isFromGraphyApp() {
        return this.misFromGraphyApp;
    }

    public boolean isGraphyIntent() {
        String action = getIntent().getAction();
        return CameraConstantsEx.GRAPHY_MYFILTER_INTENT_ACTION.equals(action) || "com.lge.graphy.action.LDU".equals(action) || (isVoiceAssistantSpecified() && CameraConstantsEx.FLAG_VALUE_GRAPHY.equalsIgnoreCase(getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null)));
    }

    public boolean isCalledByQVoice() {
        return "com.lge.pa.action.CAMVOICE".equals(getIntent().getAction());
    }

    public boolean isPostviewShowing() {
        return false;
    }

    public int getPostviewType() {
        return -1;
    }

    public void getWakeLock() {
        this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(isFromVolumeKey() ? 805306394 : 1, getClass().getName());
        this.mWakeLock.setReferenceCounted(false);
    }

    public static void setWakeLock(WakeLock wakeLock, boolean isAcquire) {
        if (wakeLock != null) {
            CamLog.m3d(CameraConstants.TAG, "WakeLock.isHeld() = " + wakeLock.isHeld() + ", isAcquire = " + isAcquire);
            if (isAcquire) {
                if (!wakeLock.isHeld()) {
                    wakeLock.acquire();
                }
            } else if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    public void handleFromVolumeKeyIntent(boolean get) {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        if (get) {
            this.mFromVolumeKey = intent.getIntExtra(CameraConstants.EXTRA_CAMERA_LAUNCH_PATH, 0);
            return;
        }
        intent.putExtra(CameraConstants.EXTRA_CAMERA_LAUNCH_PATH, 0);
        this.mFromVolumeKey = 0;
    }

    public boolean isFromVolumeKey() {
        return this.mFromVolumeKey == 4;
    }

    public boolean isFromCameraKey() {
        return this.mFromVolumeKey == 6;
    }

    protected void setWindowRotation() {
        Window win = getWindow();
        LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = 1;
        win.setAttributes(winParams);
    }

    public void setSystemUiVisibilityListener(boolean register) {
        if (!ModelProperties.isSoftKeyNavigationBarModel(this)) {
            return;
        }
        if (!register) {
            this.mIsRegisterSystemUIListener = false;
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);
            setCaptureButtonEnableByNaviBar(true);
        } else if (!this.mIsRegisterSystemUIListener) {
            this.mIsRegisterSystemUIListener = true;
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new C02725());
        }
    }

    public void setNaviBarVisibility(boolean visible, long delay) {
        if (!ModelProperties.isSoftKeyNavigationBarModel(this)) {
            return;
        }
        if (!isPostviewShowing() || getPostviewType() == 1) {
            if (this.mIsJustGetWindowFocus) {
                if (delay == CameraConstants.TOAST_LENGTH_MIDDLE_SHORT) {
                    delay += 1000;
                } else if (delay == 200) {
                    delay += 100;
                }
            }
            if (!visible) {
                removePostRunnable(this.mNaviHider);
                SystemBarUtil.hideSystemUI(this);
                this.mIsHidingNaviBar = false;
                this.mIsJustGetWindowFocus = false;
            } else if (!this.mPaused) {
                postOnUiThread(this.mNaviHider, delay);
                this.mIsHidingNaviBar = true;
            }
        }
    }

    public boolean checkModuleValidate(int checkType) {
        return true;
    }

    public void onResumeAfterDone() {
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (BaseActivity.this.checkModuleValidate(1)) {
                    if (!BaseActivity.this.isPostviewShowing()) {
                        IntentBroadcastUtil.sendBroadcastIntentCameraStarted(BaseActivity.this.getActivity());
                    }
                    if (ModelProperties.isSoftKeyNavigationBarModel(BaseActivity.this.getActivity()) && !SystemBarUtil.isEnableHideNavigation(BaseActivity.this.getAppContext())) {
                        if (BaseActivity.this.isPostviewShowing() && BaseActivity.this.getPostviewType() != 1) {
                            return;
                        }
                        if (SystemBarUtil.isSystemUIVisible(BaseActivity.this.getActivity())) {
                            SystemBarUtil.hideSystemUI(BaseActivity.this.getActivity());
                        }
                    }
                    BaseActivity.this.mResumeAfterProcessingDone = true;
                }
            }
        }, 450);
    }

    public boolean isReturnFromHelp() {
        return this.mReturnFromHelp;
    }

    public void setReturnFromHelp(boolean set) {
        this.mReturnFromHelp = set;
    }

    public boolean showLocationPermissionRequestDialog(boolean needCameraLocSetting) {
        boolean showDialog;
        if (needCameraLocSetting) {
            showDialog = "on".equals(getCurSettingValue(Setting.KEY_TAG_LOCATION));
        } else {
            showDialog = true;
        }
        if (!showDialog || checkSelfPermission(CameraConstants.ACCESS_FINE_LOCATION) == 0) {
            return false;
        }
        requestPermissions(new String[]{CameraConstants.ACCESS_FINE_LOCATION}, 100);
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100:
                SharedPreferenceUtilBase.saveInitialTagLocation(getAppContext(), 1);
                if (grantResults == null || grantResults.length <= 0 || grantResults[0] == 0) {
                    CamLog.m3d(CameraConstants.TAG, "onRequestPermissionsResult granted ");
                    locationPermissionGranted();
                    return;
                }
                if (this.mIsLocationOnByCamera) {
                    CheckStatusManager.setSystemSettingUseLocation(getAppContext().getContentResolver(), false);
                }
                setLocationOnByCamera(false);
                setSetting(Setting.KEY_TAG_LOCATION, "off", true);
                showLocationToast();
                CamLog.m3d(CameraConstants.TAG, "onRequestPermissionsResult not granted ");
                return;
            default:
                return;
        }
    }

    public void finish() {
        CamLog.m3d(CameraConstants.TAG, "finish()");
        this.mProcessFinish = true;
        super.finish();
        try {
            Intent i = getIntent();
            if (i != null && i.getExtras() != null) {
                float[] coord = i.getExtras().getFloatArray("com.lge.intent.extra.SCALE_PIVOT");
                if (coord != null) {
                    MotionPocket.overridePendingTransitionScale(this, coord[0], coord[1], false);
                }
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (NoSuchMethodError e2) {
            e2.printStackTrace();
        }
    }

    public Intent getActivityIntent() {
        return getIntent();
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        removePostRunnable(runnable);
    }

    public void setLocationOnByCamera(boolean isOn) {
        this.mIsLocationOnByCamera = isOn;
    }
}
