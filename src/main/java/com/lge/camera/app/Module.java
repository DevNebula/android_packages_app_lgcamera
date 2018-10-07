package com.lge.camera.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.FrameGridView;
import com.lge.camera.components.PreviewFrameLayout;
import com.lge.camera.components.PreviewFrameLayout.OnSizeChangedListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.dialog.CamDialogInterface;
import com.lge.camera.dialog.DialogManager;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.managers.AdvancedSelfieManager;
import com.lge.camera.managers.AnimationManager;
import com.lge.camera.managers.BackButtonManager;
import com.lge.camera.managers.BackButtonManager.BackButtonListener;
import com.lge.camera.managers.ColorEffectManager;
import com.lge.camera.managers.ColorEffectManager.ColorEffectInterface;
import com.lge.camera.managers.ColorEffectRear;
import com.lge.camera.managers.DotIndicatorManager;
import com.lge.camera.managers.ExtraPreviewUIManager;
import com.lge.camera.managers.FlashControlManager;
import com.lge.camera.managers.FocusManager;
import com.lge.camera.managers.FocusManagerBase.FocusManagerInterface;
import com.lge.camera.managers.GestureManager;
import com.lge.camera.managers.GestureManager.GestureInterface;
import com.lge.camera.managers.GestureViewManager;
import com.lge.camera.managers.GifManager;
import com.lge.camera.managers.IndicatorManager;
import com.lge.camera.managers.InitialGuideManager;
import com.lge.camera.managers.InitialGuideManagerBase.InitialGuideListener;
import com.lge.camera.managers.KeyInterface;
import com.lge.camera.managers.KeyManager;
import com.lge.camera.managers.LightFrameManager;
import com.lge.camera.managers.LivePhotoManagerBase;
import com.lge.camera.managers.LivePhotoScreenFrameManager;
import com.lge.camera.managers.LocationServiceManager;
import com.lge.camera.managers.LocationServiceManager.OnLocationListener;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ManualFocusManager;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.managers.PhoneStorageManager;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.managers.ReviewThumbnailManagerBase.OnReviewThumbnailClickListener;
import com.lge.camera.managers.TemperatureManager;
import com.lge.camera.managers.TimerManager;
import com.lge.camera.managers.ToastManager;
import com.lge.camera.managers.UndoManager;
import com.lge.camera.managers.ext.sticker.StickerManager;
import com.lge.camera.managers.ext.sticker.StickerManager.StickerManagerInterface;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CamLog.TraceTag;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.PackageUtil;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Module extends ModuleDeviceHandler implements ModuleInterface, OnSizeChangedListener, GestureInterface, OnLocationListener, KeyInterface, FocusManagerInterface, CamDialogInterface, ColorEffectInterface, InitialGuideListener, BackButtonListener, StickerManagerInterface {
    public static boolean sFirstTaken = false;
    private boolean islistenerRegisteredAfterOneShotCallback = false;
    protected AdvancedSelfieManager mAdvancedFilmManager = new AdvancedSelfieManager(this);
    protected AnimationManager mAnimationManager = new AnimationManager(this);
    protected BackButtonManager mBackButtonManager = new BackButtonManager(this);
    protected View mBaseParentView = null;
    protected ColorEffectManager mColorEffectManager = null;
    protected boolean mConfigChanging = false;
    protected int mCoverAniTime = 0;
    protected ImageView mCoverView = null;
    protected DialogManager mDialogManager = new DialogManager(this);
    protected DotIndicatorManager mDotIndicatorManager = new DotIndicatorManager(this);
    protected ExtraPreviewUIManager mExtraPrevewUIManager = new ExtraPreviewUIManager(this);
    protected FlashControlManager mFlashControlManager = new FlashControlManager(this);
    protected FocusManager mFocusManager = new FocusManager(this);
    protected GestureManager mGestureManager = new GestureManager(this);
    protected GifManager mGifManager = new GifManager(this);
    protected SurfaceHolder mHolder = null;
    protected IndicatorManager mIndicatorManager = new IndicatorManager(this);
    protected InitialGuideManager mInitGuideManager = new InitialGuideManager(this);
    protected boolean mIsGoingToPostview = false;
    protected boolean mIsScreenCaptured = false;
    protected boolean mIsShutterlessSelfieProgress = false;
    protected boolean mIsSwitchingCameraDuringRecording = false;
    protected boolean mIsTouchStartedFromNaviArea = false;
    protected boolean mIsVolumeKeyPressed = false;
    protected KeyManager mKeyManager = new KeyManager(this);
    protected LightFrameManager mLightFrameManager = new LightFrameManager(this);
    protected LivePhotoManagerBase mLivePhotoManager = new LivePhotoScreenFrameManager(this);
    protected LocationServiceManager mLocationServiceManager = new LocationServiceManager(this);
    protected ArrayList<ManagerInterfaceImpl> mManagerList = new ArrayList();
    protected ManualFocusManager mManualFocusManager = new ManualFocusManager(this);
    protected int mNeedProgressDuringCapture = 0;
    public boolean mPictureOrVideoSizeChanged = false;
    private long mPrevButtonClickedTime = 0;
    protected PreviewFrameLayout mPreviewFrameLayout = null;
    protected QuickClipManager mQuickClipManager = new QuickClipManager(this);
    protected int mRecordingStateBeforeChangingCamera = 6;
    protected GestureViewManager mReviewThumbnailManager = new GestureViewManager(this);
    protected HandlerRunnable mShowIndicatorOnResumeRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (!Module.this.checkModuleValidate(1)) {
                return;
            }
            if (Module.this.mCameraState != 1) {
                Module.this.mGet.postOnUiThread(Module.this.mShowIndicatorOnResumeRunnable, 100);
            } else {
                Module.this.showIndicatorOnResume();
            }
        }
    };
    private int mStartX = 0;
    private int mStartY = 0;
    protected StickerManager mStickerManager = null;
    protected PhoneStorageManager mStorageManager = new PhoneStorageManager(this);
    protected TemperatureManager mTemperManager = new TemperatureManager(this);
    protected TimerManager mTimerManager = new TimerManager(this);
    protected ToastManager mToastManager = new ToastManager(this);
    protected UndoManager mUndoManager = new UndoManager(this);

    /* renamed from: com.lge.camera.app.Module$2 */
    class C01162 implements OnReviewThumbnailClickListener {
        C01162() {
        }

        public boolean onReviewThumbnailClick() {
            return Module.this.onReviewThumbnailClicked(2);
        }

        public void onReviewThumbnailUpdated(boolean showBubblePopup) {
        }

        public void onQuickViewShow(boolean isAutoReview) {
            Module.this.doOnQuickViewShown(isAutoReview);
        }

        public void onImageDeleted() {
        }
    }

    /* renamed from: com.lge.camera.app.Module$3 */
    class C01173 implements AnimationListener {
        C01173() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            Module.this.mGet.setPreviewCoverVisibility(8, true);
        }
    }

    /* renamed from: com.lge.camera.app.Module$4 */
    class C01184 implements Runnable {
        C01184() {
        }

        public void run() {
            ColorUtil.createRS(Module.this.getAppContext());
        }
    }

    private class VersionCheckTask extends AsyncTask<Void, Void, Void> {
        private VersionCheckTask() {
        }

        /* synthetic */ VersionCheckTask(Module x0, C01151 x1) {
            this();
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        protected Void doInBackground(Void... params) {
            PackageUtil.checkAppUpdated(Module.this.getAppContext());
            return null;
        }
    }

    public boolean islistenerRegisteredAfterOneShotCallback() {
        return this.islistenerRegisteredAfterOneShotCallback;
    }

    public Module(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void startCameraDeviceOnCreate() {
        startCameraDevice(0);
    }

    public void startCameraDevice(int openType) {
        this.mCameraId = getCameraIdFromPref();
        CamLog.m3d(CameraConstants.TAG, "startCameraDeviceOnCreate mCameraId = " + this.mCameraId + " openType = " + openType);
        this.mCameraStartUpThread = new CameraStartUpThread(openType);
        this.mCameraStartUpThread.start();
    }

    public int getCameraIdFromPref() {
        return SharedPreferenceUtil.getCameraId(getAppContext());
    }

    public void init() {
        this.mInitModule = true;
        if (getCameraState() == -1) {
            this.mStartPreviewPrerequisiteReady.open();
            setCameraState(0);
            CamLog.m3d(CameraConstants.TAG, "block open on init.");
        }
    }

    public void initUI(View baseParent) {
        this.mBaseParentView = baseParent;
        makeManagerList();
        setManagersListener();
        this.mCoverView = (ImageView) this.mGet.findViewById(C0088R.id.preview_cover_view);
        inflateModuleLayout(this.mBaseParentView);
        initializeMiscControls(this.mBaseParentView);
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).init();
        }
    }

    public void initUIDone() {
        initializeControls(false);
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onResumeBefore();
        }
        setPreviewLayoutParam();
        LdbUtil.makeLdbCategoryMap();
        LdbUtil.makeFilterMap(getAppContext());
        LdbUtil.makeFeatureNameMap();
    }

    public void onResumeBefore() {
        CamLog.m3d(CameraConstants.TAG, "onResumeBefore");
        SecureImageUtil.setSecureCamera(this.mGet.getActivity());
        Window window = getActivity().getWindow();
        if (!getActivity().getIntent().getBooleanExtra("quicktray", false)) {
            boolean isSecureCamera = SecureImageUtil.isSecureCamera();
            boolean isQuickWindowCameraMode = QuickWindowUtils.isQuickWindowCameraMode();
            boolean z = AppControlUtil.checkFromBleKey(getActivity().getIntent()) || AppControlUtil.checkFromVolumeKey(getActivity().getIntent());
            AppControlUtil.configureWindowFlag(window, false, isSecureCamera, isQuickWindowCameraMode, z);
        } else if (window != null) {
            window.addFlags(2621440);
        }
        PhoneStorageManager.setShotMode(getShotMode());
        FileManager.setShotMode(getShotMode());
        FileNamer.get().startFileNamer(this.mGet.getAppContext(), PhoneStorageManager.getInternalStorageDir() + PhoneStorageManager.DCF_DIRECTORY, 0, true);
        invokeInitializeSettingMenus();
        setPreviewLayoutParam();
        if (!(this.mGet.getHybridView() == null || this.mGet.getHybridView().isAcquiredListener())) {
            CamLog.m3d(CameraConstants.TAG, "-acq- acquireHybridView");
            this.mGet.getHybridView().acquireHybridView(getClass());
        }
        this.mSnapShotChecker.initSnapShotChecker();
        this.mCoverAniTime = (int) (this.mGet.isCameraChanging() ? 300 : 0);
        if (this.mGifManager != null && this.mGifManager.isGIFEncoding()) {
            CamLog.m7i(CameraConstants.TAG, "[GIF] Module sIsGifEncoding onResumeBefore");
            showProcessingDialog(true, 0);
        }
        if (!AppControlUtil.isStartFromOnCreate() && !CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            String savedPrefUriStr = SharedPreferenceUtil.getLastThumbnailUri(getAppContext());
            String savedPrefPathStr = SharedPreferenceUtil.getLastThumbnailPath(getAppContext());
            CamLog.m3d(CameraConstants.TAG, "[thumbnail] savedPrefUriStr = " + savedPrefUriStr + ", savedPrefPathStr = " + savedPrefPathStr);
            if (savedPrefUriStr != null) {
                String pathFromUri = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), Uri.parse(savedPrefUriStr));
                Uri deletedFromGallery = this.mGet.getDeletedUri();
                CamLog.m7i(CameraConstants.TAG, "[thumbnail] deletedFromGallery : " + deletedFromGallery + ", sharedUri : " + Uri.parse(savedPrefUriStr));
                if ((pathFromUri == null || !pathFromUri.equals(savedPrefPathStr) || (deletedFromGallery != null && deletedFromGallery.equals(Uri.parse(savedPrefUriStr)))) && this.mReviewThumbnailManager.mThumbBtn != null) {
                    CamLog.m3d(CameraConstants.TAG, "[thumbnail] set thumbnail image data null.");
                    this.mReviewThumbnailManager.mThumbBtn.setData(null, false, false);
                    this.mReviewThumbnailManager.setThumbnailAnimation(true);
                }
                this.mGet.setDeletedUriFromGallery(null);
            }
        }
    }

    protected void invokeInitializeSettingMenus() {
        CamLog.m7i(CameraConstants.TAG, " invokeInitializeSettingMenus ");
        initializeSettingMenus();
        this.mSettingReady.open();
    }

    public void checkAndStartCamera() {
        if (checkModuleValidate(1) && getFilmState() != 3 && this.mCameraState < 1 && this.mGet.getTextureState() >= 1 && this.mCameraStartUpThread == null) {
            checkCameraId();
            this.mCameraStartUpThread = new CameraStartUpThread(0);
            this.mCameraStartUpThread.start();
        }
    }

    public void onResumeJustAfter() {
        if (this.mInitGuideManager == null) {
            return;
        }
        if ("mode_normal".equals(getShotMode()) || getShotMode().contains(CameraConstants.MODE_BEAUTY)) {
            this.mInitGuideManager.showInitialHelp();
        }
    }

    public void onResumeAfter() {
        CamLog.m3d(CameraConstants.TAG, "onResumeAfter");
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onResumeAfter();
        }
        setMediaSaveServiceListener(true);
        if (this.mKeyManager != null) {
            this.mKeyManager.setKeyInterface(this);
        }
        if (this.mFocusManager != null) {
            this.mFocusManager.setFocusInterface(this);
        }
        CamLog.m3d(CameraConstants.TAG, "send CLEAR_SCREEN_DELAY message");
        keepScreenOnAwhile();
        showSceneIndicator(true);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                Module.this.mGet.getActivity().getWindow().setBackgroundDrawable(null);
            }
        });
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "onPauseBefore");
        this.mSettingReady.open();
        waitCameraStartUpThread();
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onPauseBefore();
        }
        showSceneIndicator(false);
        this.mIsVolumeKeyPressed = false;
        this.mSettingReady.close();
    }

    public void onPauseAfter() {
        CamLog.m3d(CameraConstants.TAG, "onPauseAfter - start");
        VideoRecorder.releasePersistentSurface();
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onPauseAfter();
        }
        if (!(this.mDialogManager == null || isPostviewShowing())) {
            this.mDialogManager.onPause();
        }
        if (!ModelProperties.isUseNewNamingRule()) {
            FileNamer.get().saveCountToPref(getAppContext(), 0);
        }
        CameraDeviceUtils.dismissErrorAndFinish();
        setMediaSaveServiceListener(false);
        resetScreenOn();
        setHDRMetaDataCallback(null);
        setFlashMetaDataCallback(null);
        if (this.mFingerDetectionListener != null) {
            setFingerDetectionListener(false);
        }
        LdbUtil.unbind();
        CamLog.m3d(CameraConstants.TAG, "onPauseAfter - end");
    }

    public void onStop() {
        CamLog.m3d(CameraConstants.TAG, "onStop");
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onStop();
        }
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "onDestroy");
        releaseParamUpdater();
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).onDestroy();
        }
        clearManagerList();
        this.mCoverView = null;
        this.mBaseParentView = null;
        this.mPreviewFrameLayout = null;
        this.mInitModule = false;
        this.mIsVolumeKeyPressed = false;
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "onConfigurationChanged - start");
        this.mConfigChanging = true;
        if (this.mToastManager != null) {
            this.mToastManager.beforeConfigChanged();
        }
        ViewGroup appRoot = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        if (!(this.mBaseParentView == null || appRoot == null)) {
            View controlsView = this.mGet.findViewById(C0088R.id.camera_controls);
            int contentsBaseIndex = appRoot.indexOfChild(controlsView);
            appRoot.removeView(controlsView);
            ((ViewGroup) this.mBaseParentView).removeAllViews();
            inflateModuleLayout(this.mBaseParentView);
            initializeMiscControls(this.mBaseParentView);
            View baseRootView = this.mGet.findViewById(C0088R.id.module_base_root);
            if (baseRootView != null) {
                baseRootView.requestLayout();
            }
            View view = this.mGet.layoutInflate(C0088R.layout.camera_controls, null);
            if (contentsBaseIndex < 0) {
                contentsBaseIndex = 0;
            }
            appRoot.addView(view, contentsBaseIndex, new LayoutParams(-1, -1));
            if (this.mCameraDevice != null) {
                setPreviewLayoutParam();
                setDisplayOrientation(false);
            }
            initializeControls(true);
            Iterator it = this.mManagerList.iterator();
            while (it.hasNext()) {
                ((ManagerInterfaceImpl) it.next()).onConfigurationChanged(config);
            }
        }
        if (this.mDialogManager != null && (!isPostviewShowing() || (isPostviewShowing() && !this.mGet.isOrientationLocked()))) {
            this.mDialogManager.onConfigurationChanged(config);
        }
        this.mConfigChanging = false;
        CamLog.m3d(CameraConstants.TAG, "onConfigurationChanged - end");
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        if (degree != -1) {
            Iterator it = this.mManagerList.iterator();
            while (it.hasNext()) {
                ((ManagerInterfaceImpl) it.next()).setDegree(degree, !isFirst);
            }
            if (this.mDialogManager != null && (isFirst || !isPostviewShowing() || (isPostviewShowing() && this.mGet.isOrientationLocked()))) {
                this.mDialogManager.setOrientationDegree(degree);
            }
            setDisplayOrientation(true);
        }
    }

    public void onUserInteraction() {
        CamLog.m3d(CameraConstants.TAG, "onUserInteraction");
        if (checkModuleValidate(129)) {
            CamLog.m3d(CameraConstants.TAG, "send CLEAR_SCREEN_DELAY message");
            keepScreenOnAwhile();
            return;
        }
        CamLog.m11w(CameraConstants.TAG, "return, maybe pausing, finishing or recording");
    }

    public boolean isShutterKeyOptionTimerActivated() {
        boolean z = false;
        String shutterKey = this.mGet.getCurSettingValue(Setting.KEY_TIMER);
        if (this.mGet.isVoiceAssistantSpecified() && (this.mGet.getAssistantIntFlag(CameraConstantsEx.FLAG_TIMER_DURATION_SECONDS, -1) > 0 || !this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, false))) {
            return true;
        }
        if ("3".equals(shutterKey) || "10".equals(shutterKey)) {
            z = true;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent event) {
        checkTouchCoordinate(event);
        if (this.mGestureManager == null || this.mFocusManager == null || this.mAdvancedFilmManager == null) {
            return false;
        }
        if (checkModuleValidate(128)) {
            if (isSupportedQuickClip()) {
                if (!this.mQuickClipManager.isOpened()) {
                    this.mQuickClipManager.setMiniView();
                } else if (event.getAction() != 1) {
                    return true;
                } else {
                    this.mQuickClipManager.setMiniView();
                    return true;
                }
            }
            if (this.mGestureManager.isLongPressed() && event.getAction() == 1) {
                this.mGestureManager.setLongPressed(false);
                return true;
            } else if (this.mAdvancedFilmManager.onTouchEvent(event)) {
                return true;
            } else {
                if (isRearCamera() || !isSelfieOptionVisible()) {
                    if (this.mGet.isHelpListVisible()) {
                        if (event.getAction() != 1 || this.mHandler == null) {
                            return true;
                        }
                        this.mHandler.sendEmptyMessage(49);
                        return true;
                    } else if (this.mGet.isModeMenuVisible()) {
                        if (event.getAction() != 1) {
                            return true;
                        }
                        if (this.mGet.isModeEditable()) {
                            this.mGet.setModeEditMode(false);
                            return true;
                        } else if (this.mHandler == null) {
                            return true;
                        } else {
                            this.mHandler.sendEmptyMessage(36);
                            return true;
                        }
                    }
                } else if (event.getAction() != 1) {
                    return true;
                } else {
                    this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
                    if (getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        showDoubleCamera(true);
                    }
                    showExtraPreviewUI(false, false, true, isRearCamera());
                    onHideBeautyMenu();
                    return true;
                }
            }
        }
        if (this.mGestureManager.gestureDetected(event)) {
            return true;
        }
        if (Utils.isIgnoreTouchEvent(this.mGet.getAppContext(), (int) event.getX(), (int) event.getY())) {
            if (event.getAction() != 1 || this.mFocusManager == null || !isFocusEnableCondition()) {
                return true;
            }
            this.mFocusManager.registerCallback(true);
            return true;
        } else if (!SystemBarUtil.isSystemUIVisible(getActivity())) {
            return isFocusOnTouchEvent() ? this.mFocusManager.onTouchEvent(event) : false;
        } else {
            if (event.getAction() != 1 || this.mIsTouchStartedFromNaviArea) {
                return true;
            }
            this.mGet.setNaviBarVisibility(true, 200);
            return true;
        }
    }

    private void checkTouchCoordinate(MotionEvent event) {
        if (event.getAction() == 0) {
            this.mStartX = (int) event.getX();
            this.mStartY = (int) event.getY();
            this.mIsScreenCaptured = false;
        } else if (event.getAction() == 1) {
            if (Utils.checkSystemUIArea(getAppContext(), this.mStartX, this.mStartY)) {
                CamLog.m7i(CameraConstants.TAG, "Start position is navi bar area");
                this.mIsTouchStartedFromNaviArea = true;
            } else {
                this.mIsTouchStartedFromNaviArea = false;
            }
            this.mStartX = 0;
            this.mStartY = 0;
        } else if (event.getAction() != 2 || this.mIsScreenCaptured || this.mGet.isAnimationShowing() || event.getPointerCount() != 1 || this.mCameraState != 1) {
        } else {
            if (Math.abs(event.getX() - ((float) this.mStartX)) > 50.0f || Math.abs(event.getY() - ((float) this.mStartY)) > 50.0f) {
                long curTime = System.currentTimeMillis();
                if (Math.abs(this.mPrevButtonClickedTime - curTime) > 500) {
                    this.mIsScreenCaptured = getBlurredBitmapForSwitchingCamera();
                    this.mPrevButtonClickedTime = curTime;
                }
            }
        }
    }

    protected boolean isFocusOnTouchEvent() {
        if (this.mFocusManager == null || !checkModuleValidate(95) || ((this.mCameraState != 1 && this.mCameraState != 2 && (!isAFSupported() || !isRearCamera() || checkModuleValidate(128))) || this.mIsSwitchingCameraDuringRecording || this.mIsPreviewCallbackWaiting || !isFocusEnableCondition() || this.mGet.isAnimationShowing())) {
            return false;
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == 24 || keyCode == 25) && event.getRepeatCount() == 0) {
            if (event.getAction() == 0) {
                this.mIsVolumeKeyPressed = true;
            }
            if (event.getAction() == 1) {
                this.mIsVolumeKeyPressed = false;
            }
        }
        if ((this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress() || this.mIsGoingToPostview || this.mReviewThumbnailManager.isQuickViewAniStarted()) && (keyCode == 24 || keyCode == 25)) {
            return true;
        }
        if (this.mKeyManager == null || !this.mKeyManager.onKeyDown(keyCode, event)) {
            return false;
        }
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == 24 || keyCode == 25) && event.getRepeatCount() == 0) {
            if (event.getAction() == 0) {
                this.mIsVolumeKeyPressed = true;
            }
            if (event.getAction() == 1) {
                this.mIsVolumeKeyPressed = false;
            }
        }
        if ((this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress() || this.mIsGoingToPostview || this.mReviewThumbnailManager.isQuickViewAniStarted()) && (keyCode == 24 || keyCode == 25)) {
            return true;
        }
        if (this.mKeyManager == null || !this.mKeyManager.onKeyUp(keyCode, event)) {
            return false;
        }
        return true;
    }

    public boolean isVolumeKeyPressed() {
        return this.mIsVolumeKeyPressed;
    }

    protected void makeManagerList() {
        if (checkManagerList()) {
            addDefaultManager();
            addModuleManager();
        }
    }

    protected boolean checkManagerList() {
        if (this.mManagerList == null) {
            return false;
        }
        clearManagerList();
        return true;
    }

    protected void addDefaultManager() {
        this.mManagerList.add(this.mStorageManager);
        this.mManagerList.add(this.mReviewThumbnailManager);
        this.mManagerList.add(this.mIndicatorManager);
        this.mManagerList.add(this.mTemperManager);
        this.mManagerList.add(this.mLocationServiceManager);
        this.mManagerList.add(this.mToastManager);
        this.mManagerList.add(this.mAnimationManager);
        this.mManagerList.add(this.mTimerManager);
        this.mManagerList.add(this.mKeyManager);
        this.mManagerList.add(this.mFocusManager);
        this.mManagerList.add(this.mUndoManager);
        this.mManagerList.add(this.mDotIndicatorManager);
        this.mManagerList.add(this.mExtraPrevewUIManager);
        if (!ModelProperties.isKeyPadSupported(getAppContext())) {
            this.mManagerList.add(this.mBackButtonManager);
        }
        this.mManagerList.add(this.mGestureManager);
        this.mManagerList.add(this.mInitGuideManager);
        if (FunctionProperties.isSupportedLightFrame()) {
            this.mManagerList.add(this.mLightFrameManager);
        }
        if (isSupportedQuickClip()) {
            this.mManagerList.add(this.mQuickClipManager);
        }
        this.mManagerList.add(this.mAdvancedFilmManager);
        if (isColorEffectSupported()) {
            setColorEffectManager();
        }
        if (FunctionProperties.isSupportedSticker()) {
            String shotString = getShotMode();
            if ((shotString != null && shotString.contains(CameraConstants.MODE_BEAUTY)) || "mode_normal".equals(shotString) || (this instanceof AttachCameraModule)) {
                if (this.mStickerManager == null) {
                    this.mStickerManager = new StickerManager(this);
                }
                this.mManagerList.add(this.mStickerManager);
            }
        }
        this.mManagerList.add(this.mLivePhotoManager);
        if (FunctionProperties.isSupportedManualFocus()) {
            this.mManagerList.add(this.mManualFocusManager);
        }
    }

    protected void setManagersListener() {
        if (this.mBackButtonManager != null) {
            this.mBackButtonManager.setBackButtonListener(this);
        }
        if (this.mGestureManager != null) {
            this.mGestureManager.setGestureInterface(this);
        }
        if (this.mLocationServiceManager != null) {
            this.mLocationServiceManager.setLocationListener(this);
        }
        if (this.mInitGuideManager != null) {
            this.mInitGuideManager.setInitialGuideListener(this);
        }
        if (this.mReviewThumbnailManager != null) {
            OnReviewThumbnailClickListener listener = getReviewThumbnailClickListener();
            if (listener != null) {
                this.mReviewThumbnailManager.setReviewThumbnailClickListener(listener);
            }
        }
    }

    protected void setColorEffectManager() {
        ColorEffectManager color = getColorEffectManager();
        if (this.mColorEffectManager == null) {
            this.mColorEffectManager = color;
        } else if (this.mManagerList.contains(this.mColorEffectManager)) {
            CamLog.m3d(CameraConstants.TAG, "[color] manager is already contained");
            return;
        } else {
            this.mColorEffectManager = null;
            this.mColorEffectManager = color;
        }
        this.mColorEffectManager.setInterface(this);
        this.mManagerList.add(this.mColorEffectManager);
    }

    protected ColorEffectManager getColorEffectManager() {
        CamLog.m3d(CameraConstants.TAG, "[color] add - rear");
        return new ColorEffectRear(this);
    }

    protected OnReviewThumbnailClickListener getReviewThumbnailClickListener() {
        return new C01162();
    }

    protected void doOnQuickViewShown(boolean isAutoReview) {
        if (this.mGet.isActivatedTilePreview()) {
            this.mGet.showTilePreview(false);
        }
        setQuickClipIcon(true, false);
    }

    protected boolean onReviewThumbnailClicked(int waitType) {
        return false;
    }

    protected void clearManagerList() {
        if (this.mManagerList != null && this.mManagerList.size() > 0) {
            this.mManagerList.clear();
            this.islistenerRegisteredAfterOneShotCallback = false;
        }
    }

    protected void initializeMiscControls(View baseParent) {
        if (baseParent != null) {
            this.mPreviewFrameLayout = (PreviewFrameLayout) baseParent.findViewById(C0088R.id.frame);
            if (this.mPreviewFrameLayout != null) {
                this.mPreviewFrameLayout.setOnSizeChangedListener(this);
            }
        }
    }

    protected void onTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Log.i(CameraConstants.TAG, "[Time Info][4-1] TextureView End : available w,h = " + width + ", " + height);
        this.mHolder = null;
        if (this.mInitModule) {
            this.mStartPreviewPrerequisiteReady.open();
            if (getCameraState() != 1) {
                setCameraState(0);
                Log.i(CameraConstants.TAG, "[Time Info] block open on onTextureAvailable.");
                if (checkModuleValidate(9) && this.mCameraStartUpThread == null) {
                    checkCameraId();
                    this.mCameraStartUpThread = new CameraStartUpThread();
                    this.mCameraStartUpThread.start();
                    return;
                }
                return;
            }
            return;
        }
        setCameraState(-1);
    }

    public void checkCameraId() {
        int savedCameraId = getCameraIdFromPref();
        CamLog.m3d(CameraConstants.TAG, "checkCameraId mCameraId = " + this.mCameraId + ", savedCameraId = " + savedCameraId);
        if (this.mCameraId != savedCameraId) {
            String cameraId = isRearCamera() ? "rear" : "front";
            if (this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_USE_FRONT_CAMERA, false)) {
                this.mCameraId = SharedPreferenceUtil.getFrontCameraId(getAppContext());
                cameraId = "front";
            } else if (this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_USE_REAR_CAMERA, false)) {
                this.mCameraId = SharedPreferenceUtil.getRearCameraId(getAppContext());
                cameraId = "rear";
            }
            this.mGet.setForcedSetting(Setting.KEY_SWAP_CAMERA, cameraId);
            SharedPreferenceUtilBase.setCameraId(getAppContext(), this.mCameraId);
        }
    }

    protected void onTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        CamLog.m3d(CameraConstants.TAG, "onTextureSizeChanged[module] : width = " + width + ", height = " + height);
    }

    protected boolean onTextureDestroyed(SurfaceTexture surfaceTexture) {
        CamLog.m3d(CameraConstants.TAG, "onTextureDestroyed. mCameraDevice is null ? " + (this.mCameraDevice == null));
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setPreviewTexture(null);
        }
        setCameraState(-2);
        return true;
    }

    protected void onTextureUpdated(SurfaceTexture surfaceTexture) {
        if (checkModuleValidate(9) && this.mCameraState > 0 && !this.mConfigChanging) {
            removeCoverOnTextureUpdated();
        }
    }

    protected void onSurfaceChanged(SurfaceHolder holder, int width, int height) {
        CamLog.m3d(CameraConstants.TAG, "onSurfaceChanged");
        if (CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            CamLog.m3d(CameraConstants.TAG, "current module do not need setPreviewDisplay; Do nothing");
        } else if (this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
            CamLog.m3d(CameraConstants.TAG, "mFilmEmulatorManager is running. return");
        } else if (this.mCameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "-hybrid- mCameraDevice is null");
        } else if (!holder.isCreating() && this.mCameraState != 8 && this.mCameraState != 9) {
        } else {
            if (holder == null || holder != this.mHolder) {
                this.mCameraDevice.setPreviewDisplay(holder);
                this.mHolder = holder;
            }
        }
    }

    protected boolean onSurfaceDestroyed(SurfaceHolder holder) {
        CamLog.m3d(CameraConstants.TAG, "onSurfaceDestroyed. mCameraDevice is null ? " + (this.mCameraDevice == null));
        if (!(this.mCameraDevice == null || isModuleChanging())) {
            if (this.mGet.getAnimationType() != 1) {
                CamLog.m3d(CameraConstants.TAG, "camera not switching");
                this.mCameraDevice.stopPreview();
            }
            this.mCameraDevice.setPreviewDisplay(null);
        }
        setCameraState(-2);
        this.mHolder = null;
        return true;
    }

    protected void inflateModuleLayout(View baseParent) {
        this.mGet.layoutInflate(C0088R.layout.module_base, (ViewGroup) baseParent);
    }

    protected void initializeAfterCameraOpen() {
        CamLog.m3d(CameraConstants.TAG, "initializeAfterCameraOpen ");
        if (!this.mIsSwitchingCameraDuringRecording) {
            setPreviewLayoutParam();
        }
        refreshCameraSetting();
    }

    protected void refreshCameraSetting() {
        ListPreference pref = (ListPreference) getListPreference("focus-mode");
        if (pref != null) {
            boolean cafSupported = this.mCameraCapabilities.isContinousFocusSupported();
            boolean mwafSupported = this.mCameraCapabilities.isMWContinousFocusSupported();
            if (cafSupported || mwafSupported) {
                pref.setDefaultValue("auto");
            } else {
                pref.setDefaultValue("none");
            }
        }
    }

    public boolean isRecordingState() {
        return this.mCameraState == 6 || this.mCameraState == 7 || this.mCameraState == 5;
    }

    protected void setPreviewLayoutParam() {
        String videoSizeKey;
        CamLog.m3d(CameraConstants.TAG, "-picsize- setPreviewLayoutParam");
        ActivityBridge activityBridge = this.mGet;
        if (isRecordingState()) {
            videoSizeKey = SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId);
        } else {
            videoSizeKey = SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId);
        }
        ListPreference listPref = activityBridge.getListPreference(videoSizeKey);
        if (listPref != null) {
            int[] size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
            int startMargin = -1;
            if (size != null) {
                if (FunctionProperties.isSupportedLightFrame() && this.mLightFrameManager != null && this.mLightFrameManager.isLightFrameMode()) {
                    CamLog.m3d(CameraConstants.TAG, "size[0] = " + size[0] + ", size[1] = " + size[1]);
                    int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), ModelProperties.isSoftKeyNavigationBarModel(getAppContext()));
                    size = changeLightFramePreviewSize(size);
                    if (size != null) {
                        if (lcdSize != null) {
                            startMargin = (lcdSize[0] - size[0]) / 2;
                        }
                    } else {
                        return;
                    }
                }
                if (!(isMMSIntent() && this.mGet.isVideoCaptureMode())) {
                    this.mGet.setTextureLayoutParams(size[0], size[1], startMargin);
                }
                if (this.mPreviewFrameLayout != null) {
                    this.mPreviewFrameLayout.setAspectRatio(((double) size[0]) / ((double) size[1]));
                }
                setAnimationLayout(3);
            }
        }
    }

    public boolean isAvailableTilePreview() {
        if (!FunctionProperties.isSupportedCameraRoll()) {
            return false;
        }
        CamLog.m7i(CameraConstants.TAG, "[Tile] curMode : " + getShotMode());
        if ((!"mode_normal".equals(getShotMode()) && !getShotMode().contains(CameraConstants.MODE_BEAUTY) && !CameraConstants.MODE_SMART_CAM.equals(getShotMode()) && !CameraConstants.MODE_SMART_CAM_FRONT.equals(getShotMode()) && !CameraConstants.MODE_REAR_OUTFOCUS.equals(getShotMode()) && !CameraConstants.MODE_FRONT_OUTFOCUS.equals(getShotMode())) || isAttachIntent() || isVideoAttachMode()) {
            return false;
        }
        return true;
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        int previewTopMargin;
        if (this.mPreviewFrameLayout != null) {
            this.mPreviewFrameLayout.setLayoutParams(params);
        }
        int previewStartMargin = Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()) ? params.getMarginStart() : params.topMargin;
        if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
            previewTopMargin = params.topMargin;
        } else {
            previewTopMargin = params.getMarginStart();
        }
        if (this.mFocusManager != null) {
            this.mFocusManager.setFocusAreaWindow(params.width, params.height, previewStartMargin, previewTopMargin);
        }
        if (this.mGestureManager != null) {
            this.mGestureManager.setTouchableArea(params.width, params.height, previewStartMargin, previewTopMargin);
        }
    }

    public void setCharging(boolean charging) {
        if (this.mTemperManager != null) {
            this.mTemperManager.setCharging(charging);
        }
    }

    public void onBatteryLevelChanged(int orgLevel, int indiLevel, float voltageLevel) {
        AppControlUtil.setBatteryLevel(orgLevel);
        AppControlUtil.setVoltageLevel(voltageLevel);
        if (this.mTemperManager != null && this.mTemperManager.getBatteryLevel() != indiLevel) {
            this.mTemperManager.setBatteryLevel(indiLevel);
        }
    }

    private boolean isAvailabeHeatingWarning() {
        if (this.mTemperManager == null || this.mCameraDevice == null || checkModuleValidate(192)) {
            return false;
        }
        String videoSizeStr = getSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        if (videoSizeStr == null) {
            return false;
        }
        String videoSize = videoSizeStr.split("@")[0];
        CamLog.m3d(CameraConstants.TAG, "isAvailabeHeatingWarning, videoSize = " + videoSize);
        if (TemperatureManager.IsHeatingVideoSize(videoSize)) {
            return true;
        }
        return false;
    }

    public void startHeatingWarning(boolean start) {
        if (this.mTemperManager != null) {
            if (!start) {
                this.mTemperManager.stopHeatingWarning();
            } else if (this.mTemperManager.isCharging() && isAvailabeHeatingWarning()) {
                this.mTemperManager.startHeatingWarning();
            }
        }
    }

    public void onPowerConnected() {
        if (isAvailabeHeatingWarning()) {
            this.mTemperManager.startHeatingWarning();
        }
    }

    public void onPowerDisconnected() {
        if (isAvailabeHeatingWarning()) {
            this.mTemperManager.stopHeatingWarning();
        }
    }

    public void setBatteryIndicatorVisibility(boolean visible) {
        if (this.mIndicatorManager != null) {
            this.mIndicatorManager.setBatteryIndicatorVisibility(visible);
        }
    }

    public void setTimerIndicatorVisibility(boolean visible) {
        if (this.mIndicatorManager != null) {
            this.mIndicatorManager.setTimerIndicatorVisibility(visible);
        }
    }

    public void updateIndicator(int id, int intParam, boolean boolParam) {
        if (this.mIndicatorManager != null) {
            this.mIndicatorManager.updateIndicator(id, intParam, boolParam);
        }
    }

    public void removeCoverOnTextureUpdated() {
        if (this.mCoverView == null || this.mCoverView.getVisibility() != 0) {
            return;
        }
        if (this.mCoverAniTime == 0) {
            this.mGet.setPreviewCoverVisibility(8, true);
        } else {
            AnimationUtil.startShowingAnimation(this.mCoverView, false, 300, new C01173());
        }
    }

    public void changeCoverState(boolean isCoverClosed) {
    }

    public void setAnimationLayout(int type) {
        ListPreference listPref;
        if (!checkModuleValidate(128) || this.mCameraState == 5 || isRecordingPriorityMode()) {
            listPref = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        } else {
            listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        }
        if (listPref != null && this.mAnimationManager != null) {
            int[] size;
            String screenSize = listPref.getExtraInfo(2);
            String mode = getShotMode();
            if ("".equals(screenSize) || CameraConstants.MODE_MULTIVIEW.equals(mode) || CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                size = Utils.getLCDsize(this.mGet.getAppContext(), true);
            } else {
                size = Utils.sizeStringToArray(screenSize);
            }
            if ((type & 1) != 0) {
                int startMargin;
                if (!(this.mLightFrameManager == null || !this.mLightFrameManager.isLightFrameMode() || this.mCameraState == 5)) {
                    size = Utils.getLCDsize(this.mGet.getAppContext(), true);
                }
                float previewRatio = ((float) size[0]) / ((float) size[1]);
                if (ModelProperties.isLongLCDModel()) {
                    startMargin = RatioCalcUtil.getLongLCDModelTopMargin(getAppContext(), size[0], size[1], 0);
                } else {
                    startMargin = RatioCalcUtil.getNormalLCDModelTopMargin(getAppContext(), size[0], size[1], 0);
                }
                if (checkModuleValidate(192)) {
                    startMargin = RatioCalcUtil.getTilePreviewMargin(this.mGet.getAppContext(), this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW).equals("on"), previewRatio, startMargin);
                }
                this.mAnimationManager.setSnapshotAniLayout(size[0], size[1], startMargin);
            }
            if ((type & 2) != 0) {
                this.mAnimationManager.setSwitchAniLayout(size[0], size[1], -1);
            }
        }
    }

    public void showFrameGridView(String value, boolean useAnimation) {
        boolean useAni = useAnimation;
        FrameGridView frameGridView = (FrameGridView) this.mGet.findViewById(C0088R.id.preview_frame_grid);
        if (!(useAnimation || !"on".equals(value) || frameGridView.getVisibility() == 0)) {
            useAni = true;
        }
        if (frameGridView != null) {
            int visibility = "on".equals(value) ? 0 : 8;
            if (visibility != frameGridView.getVisibility()) {
                if (useAni) {
                    frameGridView.startGridViewAnimation("on".equals(value));
                } else {
                    frameGridView.setVisibility(visibility);
                }
            }
        }
    }

    protected String getGridSettingValue() {
        return this.mGet.getCurSettingValue(Setting.KEY_FRAME_GRID);
    }

    protected void startPreviewDone() {
        boolean z = false;
        CamLog.m3d(CameraConstants.TAG, "startPreviewDone : start");
        if (checkModuleValidate(1)) {
            if (this.mAdvancedFilmManager == null || !(this.mAdvancedFilmManager == null || this.mAdvancedFilmManager.isRunningFilmEmulator())) {
                this.mGet.setPreviewVisibility(0);
            }
            this.mCameraStartUpThread = null;
            boolean isFilmSettingOff = CameraConstants.FILM_NONE.equals(getSettingValue(Setting.KEY_FILM_EMULATOR));
            if (this.mIsSwitchingCameraDuringRecording) {
                CamLog.m3d(CameraConstants.TAG, "Set camera state = " + this.mRecordingStateBeforeChangingCamera);
                setCameraState(this.mRecordingStateBeforeChangingCamera);
            } else if (isFilmSettingOff && !getShotMode().contains(CameraConstants.MODE_SMART_CAM)) {
                setCameraState(1);
            }
            CamLog.m3d(CameraConstants.TAG, "QuickClip setLayout");
            if (isSupportedQuickClip() && this.mQuickClipManager != null) {
                this.mQuickClipManager.setLayout();
            }
            if (!this.mGet.isCameraChanging()) {
                z = true;
            }
            enableControls(true, z);
            if (this.mFocusManager != null && isFocusEnableCondition() && ((isFilmSettingOff || getFilmState() == 4) && !(getShotMode().contains(CameraConstants.MODE_SMART_CAM) && (this.mAdvancedFilmManager == null || this.mAdvancedFilmManager.getFilmSurfaceTexture() == null)))) {
                this.mFocusManager.registerCallback();
            }
            if (FunctionProperties.isSupportedSwitchingAnimation()) {
                new Thread(new C01184()).start();
            }
            CamLog.m3d(CameraConstants.TAG, "startPreviewDone : end");
        }
    }

    protected void initializeAfterStartPreviewDone() {
        if (this.mCameraDevice != null) {
            CameraParameters parameters = this.mCameraDevice.getParameters();
            if (parameters != null) {
                if (getFilmState() != 3) {
                    setZoomCompensation(parameters);
                }
                String checkDeviceErrorTex = CameraDeviceUtils.checkDeviceComponentSupported(parameters, getActivity());
                if (checkDeviceErrorTex.length() != 0) {
                    this.mGet.showToast(checkDeviceErrorTex.toString(), CameraConstants.TOAST_LENGTH_LONG);
                }
            }
        }
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).initializeAfterStartPreviewDone();
        }
    }

    protected void setSuperZoomSync() {
        CamLog.m3d(CameraConstants.TAG, "setSuperZoomSync : start");
        if (this.mCameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "setSuperZoomSync : exit");
            return;
        }
        if (FunctionProperties.isSupportedSuperZoom() && !isManualMode()) {
            this.mCameraDevice.setSuperZoomSync(this.mCameraDevice.getParameters());
        }
        CamLog.m3d(CameraConstants.TAG, "setSuperZoomSync : end");
    }

    protected void oneShotPreviewCallbackDone() {
        boolean useAni;
        String mode = getShotMode();
        CamLog.m3d(CameraConstants.TAG, "oneShotPreviewCallbackDone. curMode = " + mode);
        if (AppControlUtil.isStartFromOnCreate()) {
            new VersionCheckTask(this, null).execute(new Void[0]);
        }
        AppControlUtil.setStartFromOnCreate(false);
        Activity activity = this.mGet.getActivity();
        if (activity != null) {
            TalkBackUtil.sendCameraTypeAccessibilityEvent(getAppContext(), activity.getClass().getName());
        }
        displayUIComponentAfterOneShot();
        removePreviewAlphaCover(mode);
        this.mGet.setPreviewVisibility(0);
        this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
        String value = getGridSettingValue();
        if ("on".equals(value)) {
            useAni = true;
        } else {
            useAni = false;
        }
        if (!checkModuleValidate(192)) {
            useAni = false;
        }
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            showFrameGridView(value, useAni);
        }
        if (this.mGet.isCameraChanging()) {
            this.mGet.onCameraSwitchingEnd();
        }
        if (!(this.mCameraState == 6 || this.mCameraState == 5)) {
            String hdrValue = getSettingValue("hdr-mode");
            if ("2".equals(hdrValue) && !isManualMode()) {
                setHDRMetaDataCallback(hdrValue);
            }
            String flash = getSettingValue("flash-mode");
            if ("auto".equals(flash)) {
                setFlashMetaDataCallback(flash);
            }
        }
        if (!isMenuShowing(4)) {
            setFingerDetectionListener(true);
        }
        this.mPictureOrVideoSizeChanged = false;
        this.mIsPreviewCallbackWaiting = false;
        if (FunctionProperties.isSupportedQrCode(getAppContext()) && isRearCamera() && checkModuleValidate(192) && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_QR))) {
            onStartQRCodeClicked();
        }
    }

    protected void removePreviewAlphaCover(String mode) {
        if (checkModuleValidate(192) && this.mAdvancedFilmManager != null) {
            if (getFilmState() == 1) {
                this.mGet.setPreviewCoverVisibility(8, false, null, false, false);
            } else if (this.mAdvancedFilmManager.isReadyToOpenFilterMenu()) {
                this.mAdvancedFilmManager.removePreviewCoverWithDelay(300, false);
                this.mAdvancedFilmManager.setReadyToOpenFilterMenu(false);
            } else if (!CameraConstants.MODE_POPOUT_CAMERA.equals(mode) && !CameraConstants.MODE_DUAL_POP_CAMERA.equals(mode)) {
                boolean isMovePreview = true;
                if (this.mAdvancedFilmManager.isAfterRecording()) {
                    this.mAdvancedFilmManager.setAfterRecording(false);
                }
                if (ModelProperties.isMTKChipset() && this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                    this.mAdvancedFilmManager.removePreviewCoverWithDelay(CameraConstantsEx.GOOGLE_ASSISTANT_TAKE_CMD_DELAY, true);
                    return;
                }
                if (this.mAdvancedFilmManager.isRunningFilmEmulator()) {
                    isMovePreview = false;
                }
                this.mGet.setPreviewCoverVisibility(8, true, null, isMovePreview, false);
            }
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!checkModuleValidate(192) || isMenuShowing(CameraConstants.MENU_TYPE_ALL) || this.mSnapShotChecker.checkMultiShotState(2)) {
            return false;
        }
        if ((this.mGet.isAnimationShowing() && !this.mGet.isCameraChanging()) || isZoomBarVisible()) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "displayUIComponentAfterOneShot");
        return true;
    }

    protected void setFlashLevelControlMenu(boolean isOn) {
        super.setFlashLevelControlMenu(isOn);
        if (this.mFlashControlManager != null) {
            this.mFlashControlManager.showAndHideFlashControlBar(isOn);
        }
    }

    public void handleSwitchCamera() {
        if (!checkModuleValidate(221) || isPostviewShowing() || this.mIsGoingToPostview) {
            CamLog.m7i(CameraConstants.TAG, "handleSwitchCamera-This state is not a Idle preview state, return.");
            return;
        }
        CamLog.traceBegin(TraceTag.MANDATORY, "SwitchCamera", 1001);
        this.mGet.removeSettingMenu(true, true);
        this.mGet.hideModeMenu(false, true);
        this.mGet.hideHelpList(false, true);
        if (this.mColorEffectManager != null) {
            this.mColorEffectManager.hideMenu(false);
        }
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.showFilmMenu(false, 1, false, false, 0, true);
        }
        showExtraPreviewUI(false, false, true, true);
        showFrameGridView("off", false);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (!Module.this.checkModuleValidate(221) || Module.this.isPostviewShowing() || Module.this.mIsGoingToPostview) {
                    CamLog.m7i(CameraConstants.TAG, "handleSwitchCamera, Post handle run, this state is not a Idle preview state, return.");
                    return;
                }
                Module.this.mGet.onCameraSwitchingStart();
                Module.this.switchCamera();
            }
        });
    }

    protected boolean isHandlerSwitchingModule() {
        return false;
    }

    public boolean isFocusEnableCondition() {
        return true;
    }

    protected void doBackButtonPress(boolean checkSystemUIVisible) {
        if (this.mBackButtonManager != null) {
            this.mBackButtonManager.doBackButtonEvent(checkSystemUIVisible);
        }
    }

    protected void showSceneIndicator(boolean show) {
        if (!show) {
            this.mGet.removePostRunnable(this.mShowIndicatorOnResumeRunnable);
        } else if (!this.mGet.isCameraChanging() && !this.mGet.isModuleChanging() && !this.mGet.isReturnFromHelp() && checkModuleValidate(1)) {
            this.mGet.postOnUiThread(this.mShowIndicatorOnResumeRunnable, 100);
        }
    }

    protected void showIndicatorOnResume() {
        if (this.mIndicatorManager != null) {
            String hdrValue = this.mGet.getCurSettingValue("hdr-mode");
            if (FunctionProperties.isSupportedHDR(isRearCamera()) >= 1 && "1".equals(hdrValue)) {
                this.mIndicatorManager.showSceneIndicator(2);
            }
            if (FunctionProperties.isLivePhotoSupported() && "on".equals(getSettingValue(Setting.KEY_LIVE_PHOTO))) {
                this.mIndicatorManager.showSceneIndicator(9);
            }
        }
    }

    protected void updateIndicatorFromMetadataCallback(int type, int visible, boolean useAnim) {
        final int i = type;
        final int i2 = visible;
        final boolean z = useAnim;
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (Module.this.mIndicatorManager != null) {
                    Module.this.mIndicatorManager.updateIndicator(i, i2, z);
                }
            }
        });
    }

    public void setQuickClipIcon(boolean isForceSet, boolean isOn) {
        if (isSupportedQuickClip()) {
            CamLog.m3d(CameraConstants.TAG, "isForceSet : " + isForceSet + " isOn : " + isOn + " isQuickClipShowingCondition() : " + isQuickClipShowingCondition());
            if (isForceSet) {
                this.mQuickClipManager.setQuickClipIcon(isOn);
            } else {
                this.mQuickClipManager.setQuickClipIcon(isQuickClipShowingCondition());
            }
        }
    }

    public boolean isQuickClipShowingCondition() {
        if (CameraConstants.MODE_MANUAL_CAMERA.equals(getShotMode()) || CameraConstants.MODE_MANUAL_VIDEO.equals(getShotMode()) || (((this.mGifManager == null || this.mGifManager.getGifVisibleStatus()) && !this.mGet.isActivatedQuickdetailView()) || !checkModuleValidate(128) || isActivatedQuickview())) {
            return false;
        }
        return true;
    }

    public boolean isSupportedQuickClip() {
        if (this.mQuickClipManager == null || isLGUOEMCameraIntent()) {
            return false;
        }
        return true;
    }

    protected void drawTrackingAF(Rect rect) {
        if (this.mFocusManager != null) {
            this.mFocusManager.drawTrackingAF(rect);
        }
    }

    protected boolean useAFTrackingModule() {
        CamLog.m3d(CameraConstants.TAG, "useAFTrackingModule getCameraId = " + getCameraId());
        if (getCameraId() == 0) {
            return true;
        }
        return false;
    }

    protected boolean isAEAFLockSupportedMode() {
        return true;
    }

    protected boolean isAEControlSupportedMode() {
        return true;
    }

    public boolean canUseAEAFLock() {
        if (isAEAFLockSupportedMode() && checkModuleValidate(192)) {
            return true;
        }
        return false;
    }

    public boolean canUseAeControlBar() {
        if (isAEControlSupportedMode()) {
            return true;
        }
        return false;
    }

    public boolean isFaceDetectionSupported() {
        if (isVideoCaptureMode() || this.mCameraCapabilities == null) {
            return false;
        }
        return this.mCameraCapabilities.isFaceDetectionSupported();
    }

    public boolean checkPreviewCoverVisibilityForRecording() {
        if ((isUHDmode() || isFHD60()) && getFilmState() == 1) {
            return false;
        }
        if (getShotMode().contains(CameraConstants.MODE_SQUARE) && !"on".equals(getSettingValue(Setting.KEY_VIDEO_STEADY))) {
            return false;
        }
        if (getShotMode().equals(CameraConstants.MODE_POPOUT_CAMERA)) {
            return true;
        }
        int[] videoScreenSize = new int[]{0, 0};
        int[] cameraScreenSize = new int[]{0, 0};
        ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        if (listPref != null) {
            videoScreenSize = Utils.sizeStringToArray(listPref.getExtraInfo(2));
        }
        listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        if (listPref != null) {
            cameraScreenSize = Utils.sizeStringToArray(listPref.getExtraInfo(2));
        }
        if ((videoScreenSize[0] != cameraScreenSize[0] || videoScreenSize[1] != cameraScreenSize[1]) && !isAttachIntent()) {
            CamLog.m3d(CameraConstants.TAG, "show preview cover");
            showFrameGridView("off", false);
            return true;
        } else if ("on".equals(getSettingValue(Setting.KEY_VIDEO_STEADY))) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "do not show preview cover");
            return false;
        }
    }

    public boolean getBlurredBitmapForSwitchingCamera() {
        int i = 240;
        if (getPreviewCoverVisibility() == 0) {
            return false;
        }
        if (isLightFrameOn()) {
            getCurPreviewBlurredBitmap(136, 240, 25, false);
        } else {
            if (getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                i = 136;
            }
            getCurPreviewBlurredBitmap(136, i, 25, true);
        }
        return true;
    }

    public void setListenerAfterOneShotCallback() {
        setListenerRegister();
    }

    protected void setListenerRegister() {
        CamLog.m7i(CameraConstants.TAG, "mManagerList : " + this.mManagerList);
        Iterator it = this.mManagerList.iterator();
        while (it.hasNext()) {
            ((ManagerInterfaceImpl) it.next()).setListenerAfterOneShotCallback();
            this.islistenerRegisteredAfterOneShotCallback = true;
        }
    }

    protected boolean setupPreviewBuffer(CameraParameters params, SurfaceTexture surfaceTexture) {
        if (params == null || FunctionProperties.getSupportedHal() != 2) {
            return true;
        }
        Size previewSize = params.getPreviewSize();
        if (previewSize == null) {
            return true;
        }
        if (surfaceTexture == null) {
            return this.mGet.setCameraPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        }
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        return true;
    }

    protected boolean isNeedRestartByPictureSizeChanged() {
        if (FunctionProperties.getSupportedHal() == 2 || ModelProperties.isMTKChipset()) {
            return true;
        }
        return false;
    }

    public boolean isShutterlessSelfieProgress() {
        return this.mIsShutterlessSelfieProgress;
    }

    protected void initializeSettingMenus() {
    }

    protected int[] changeLightFramePreviewSize(int[] previewSize) {
        return null;
    }

    public boolean isPostviewShowing() {
        return false;
    }

    protected void setVisibleGuideTextforQuickClip() {
    }

    public boolean isMenuShowing(int menuType) {
        return false;
    }

    public boolean getSpliceviewReverseState() {
        return false;
    }

    public void onStartQRCodeClicked() {
    }

    public void onStopQRCodeClicked() {
    }

    public void setQRLayoutVisibility(int visibility) {
    }

    protected boolean checkToUseFilmEffect() {
        return false;
    }

    public void stopTimerForSmartCamScene() {
    }

    public void startTimerForSmartCamScene() {
    }

    public void setStickerDrawing(boolean draw) {
        CamLog.m7i(CameraConstants.TAG, "setStickerDrawing : " + draw);
        setSetting(Setting.KEY_STICKER, draw ? "on" : "off", false);
        if (this.mCameraDevice != null) {
            CameraParameters param = this.mCameraDevice.getParameters();
            param.set(ParamConstants.KEY_DRAWING_STICKER, draw ? "on" : "off");
            this.mCameraDevice.setParameters(param);
        }
    }
}
