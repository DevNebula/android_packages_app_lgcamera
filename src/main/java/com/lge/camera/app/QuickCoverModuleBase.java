package com.lge.camera.app;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.support.p000v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.QuickCoverManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.OsManager;

public class QuickCoverModuleBase extends BeautyShotCameraModule {
    protected static final int CHECK_DURATION = 1000;
    protected static final int NOT_FOUND = 0;
    protected static final String STR_BURST_SHOT = "_Burst";
    protected final float ALPHA_DIM;
    protected final float ALPHA_NORMAL;
    protected AudioManager mAudioManger;
    protected int mCoverHeight;
    protected QuickCoverManager mCoverManager;
    protected int mCoverType;
    protected int mCoverWidth;
    protected int[] mCurrentQuickCircleSize;
    protected GestureDetector mGestureDetector;
    protected boolean mIsDragOnPreview;
    protected boolean mIsDragToFinish;
    protected boolean mIsPageChanging;
    protected boolean mIsSavingProgress;
    protected int mPadding;
    protected QuickCirclePagerAdapter mPagerAdapter;
    protected ImageView mQuickAnimView;
    protected RelativeLayout mQuickCirclePagerView;
    protected int mQuickCircle_height;
    protected int mQuickCircle_marginTop;
    protected int mQuickCircle_width;
    protected int mQuickCircle_width_wide;
    protected View mQuickWindowView;
    protected Thread mSaveImgThread;
    protected BitmapDrawable mSaveLastPictureDrawable;
    protected Drawable mSurfaceCapture;
    protected boolean mTempGetAudioFocus;
    protected final int mThreshold;
    protected Toast mToast;
    protected int mToastMarginTop;
    protected String mToastMsg;
    protected boolean mTouchMoveOnPreview;
    protected int mTouch_down_x;
    protected int mTouch_down_y;
    protected int mTouch_move_x;
    protected int mTouch_move_y;
    protected Uri mUri;
    protected ViewPager mViewPager;

    /* renamed from: com.lge.camera.app.QuickCoverModuleBase$1 */
    class C02621 extends SimpleOnGestureListener {
        C02621() {
        }

        public boolean onDoubleTap(MotionEvent me) {
            return false;
        }
    }

    public QuickCoverModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
        this.ALPHA_NORMAL = 1.0f;
        this.ALPHA_DIM = 0.4f;
        this.mViewPager = null;
        this.mQuickWindowView = null;
        this.mQuickAnimView = null;
        this.mQuickCirclePagerView = null;
        this.mPagerAdapter = null;
        this.mSaveLastPictureDrawable = null;
        this.mSurfaceCapture = null;
        this.mAudioManger = null;
        this.mTempGetAudioFocus = false;
        this.mGestureDetector = null;
        this.mUri = null;
        this.mToast = null;
        this.mToastMsg = null;
        this.mToastMarginTop = 0;
        this.mTouch_down_x = 0;
        this.mTouch_down_y = 0;
        this.mTouch_move_x = 0;
        this.mTouch_move_y = 0;
        this.mThreshold = 50;
        this.mSaveImgThread = null;
        this.mIsPageChanging = false;
        this.mIsSavingProgress = false;
        this.mIsDragOnPreview = false;
        this.mIsDragToFinish = false;
        this.mTouchMoveOnPreview = false;
        this.mPadding = 0;
        this.mCoverType = 0;
        this.mCoverWidth = 0;
        this.mCoverHeight = 0;
        this.mQuickCircle_width = 0;
        this.mQuickCircle_width_wide = 0;
        this.mQuickCircle_height = 0;
        this.mQuickCircle_marginTop = 0;
        this.mCurrentQuickCircleSize = new int[]{0, 0, 0};
        this.mCoverManager = null;
        this.mCoverManager = new QuickCoverManager(this);
    }

    protected void initializeAfterCameraOpen() {
        super.initializeAfterCameraOpen();
        doCleanView(false, false, false);
        if (AppControlUtil.checkFromVolumeKey(this.mGet.getActivity().getIntent())) {
            setCheckLowDistance();
        }
    }

    protected void setPreviewLayoutParam() {
        if (getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId)) != null) {
            setTextureLayoutParam();
        }
        if (this.mFocusManager != null) {
            this.mFocusManager.setFocusAreaWindow(this.mCurrentQuickCircleSize[0], this.mCurrentQuickCircleSize[1], this.mCurrentQuickCircleSize[2], -1);
        }
    }

    protected void setTextureLayoutParam() {
        int coverHeight = this.mCoverManager.getQuickCoverRatio() == 2 ? this.mQuickCircle_width_wide : this.mQuickCircle_width;
        int coverWidth = this.mCoverManager.getQuickCoverRatio() == 3 ? this.mQuickCircle_width : this.mQuickCircle_height;
        this.mGet.setTextureLayoutParams(coverHeight, coverWidth, this.mQuickCircle_marginTop);
        if (this.mAnimationManager != null) {
            this.mAnimationManager.setSnapshotAniLayout(coverHeight, coverWidth, this.mQuickCircle_marginTop);
        }
        this.mCurrentQuickCircleSize[0] = coverHeight;
        this.mCurrentQuickCircleSize[1] = coverWidth;
        this.mCurrentQuickCircleSize[2] = this.mQuickCircle_marginTop;
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setUpAllViews();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(0);
        }
        setDefaultSettingValueAndDisable("focus-mode", true);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    public void setUpAllViews() {
    }

    public String getShotMode() {
        if (this.mBeautyManager.isBeautyOn()) {
            return super.getShotMode();
        }
        return "mode_normal";
    }

    protected void restoreSettingMenus() {
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(1);
        }
        restoreSettingValue("focus-mode");
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected void changeRequester() {
        if (this.mParamUpdater != null) {
            if (isZoomAvailable()) {
                this.mParamUpdater.addRequester("zoom", "0", false, true);
            }
            this.mParamUpdater.setParamValue(ParamConstants.KEY_EFFECT, "none");
            this.mParamUpdater.setParamValue(ParamConstants.KEY_WHITE_BALANCE, "auto");
            this.mParamUpdater.setParamValue(ParamConstants.KEY_ZSL, "on");
            this.mParamUpdater.setParamValue(ParamConstants.KEY_SCENE_MODE, "auto");
        }
    }

    protected boolean takePicture() {
        if (this.mViewPager == null) {
            CamLog.m3d(CameraConstants.TAG, "takePicture : mViewPager is null. return");
            takePictureFailed();
            return false;
        } else if (this.mViewPager.getCurrentItem() == 0) {
            this.mIsSavingProgress = true;
            if (!(AudioUtil.isWiredHeadsetOn() || AudioUtil.isBluetoothA2dpOn() || !isAudioUsing() || this.mStorageManager == null || !isAvailableTakePicture())) {
                AudioUtil.setAudioFocus(getAppContext(), true);
                this.mTempGetAudioFocus = true;
            }
            return super.takePicture();
        } else {
            takePictureFailed();
            return true;
        }
    }

    protected void onTakePictureAfter() {
        this.mSnapShotChecker.setPictureCallbackState(3);
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setEnabled(true);
        }
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setAfterShot();
        }
    }

    protected void takePictureFailed() {
        CamLog.m3d(CameraConstants.TAG, "[QuickCover] takePicture fail");
        this.mIsSavingProgress = false;
    }

    protected void takePictureLongShot(boolean isInternalStroge) {
        if (this.mViewPager != null && this.mViewPager.getCurrentItem() == 0) {
            this.mIsSavingProgress = true;
            super.takePictureLongShot(isInternalStroge);
        }
    }

    public boolean stopBurstShotTaking(boolean isSkip) {
        this.mIsSavingProgress = false;
        return super.stopBurstShotTaking(false);
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mIsPageChanging) {
            return false;
        }
        return super.onCameraShutterButtonClicked();
    }

    public void onShutterQuickButtonClickListener() {
        if ((this.mPostviewManager == null || !this.mPostviewManager.isPostviewShowing()) && stopBurstShotTaking(false)) {
        }
    }

    public boolean onVideoShutterClickedBefore() {
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        setTextureLayoutParam();
        hideRecordingUI();
        return true;
    }

    protected void doAfterStopRecorderThread() {
        super.doAfterStopRecorderThread();
        setTextureLayoutParam();
        hideRecordingUI();
    }

    public void doAfterSaveImageSavers(Uri uri, boolean updateThumbnail) {
        if (uri != null) {
            this.mGet.requestNotifyNewMediaonActivity(uri, updateThumbnail);
        }
    }

    protected void judgePreviewRatio() {
        int previewRatio = 1;
        int[] size = Utils.sizeStringToArray(this.mGet.getCurSettingValue("picture-size"));
        if (!(size == null || size.length <= 1 || size[1] == 0)) {
            float ratio = ((float) size[0]) / ((float) size[1]);
            if (((double) ratio) > 1.6d) {
                previewRatio = 2;
            } else if (((double) ratio) <= 1.1d) {
                previewRatio = 3;
            }
        }
        this.mCoverManager.setQuickCoverRatio(previewRatio);
    }

    public void hideRecordingUI() {
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.hide();
        }
    }

    protected boolean isAudioUsing() {
        boolean isMusiPlaying = false;
        boolean isAudioRecording;
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER))) {
            isAudioRecording = false;
        } else {
            isAudioRecording = AudioUtil.isAudioRecording(getAppContext());
        }
        if (this.mAudioManger != null) {
            isMusiPlaying = this.mAudioManger.isMusicActive();
        }
        if (isMusiPlaying || isAudioRecording) {
            return true;
        }
        return false;
    }

    protected void finishQuickCircleCamera() {
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] finish app.");
        if (!doBackKey()) {
            this.mGet.getActivity().finish();
        }
    }

    public void changeCoverState(boolean isCoverClosed) {
        if (!isCoverClosed && this.mViewPager != null) {
            if (this.mViewPager.getCurrentItem() == 1) {
                this.mGet.launchGallery(getUri(), 0);
            }
            CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] quick window camera flow : change the normal module.");
            if (checkModuleValidate(1) && !AppControlUtil.isGalleryLaunched()) {
                this.mGet.changeModuleByCoverstate();
            }
        }
    }

    public boolean isSupportFaceFocusModule() {
        return false;
    }

    public void changeQuickCoverPage(int page) {
        this.mCoverManager.setQuickCoverPage(page);
    }

    public Uri getUri() {
        return this.mUri;
    }

    protected boolean checkBurst(Uri uri) {
        String str = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), uri);
        return str == null ? false : str.contains("_Burst");
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        CamLog.m3d(CameraConstants.TAG, "doCleanView : doByAction = " + doByAction + ", useAnimation = " + useAnimation + ", saveState = " + saveState);
        if (this.mHandler != null && this.mQuickButtonManager != null && this.mBackButtonManager != null && this.mCaptureButtonManager != null && this.mReviewThumbnailManager != null) {
            Message msg = this.mHandler.obtainMessage(32);
            msg.arg1 = useAnimation ? 0 : 1;
            this.mHandler.sendMessage(msg);
            this.mGet.hideModeMenu(false, true);
            this.mGet.hideHelpList(false, true);
            this.mQuickButtonManager.hide(false, false, false);
            this.mCaptureButtonManager.setShutterButtonVisibility(8, 3, false);
            this.mReviewThumbnailManager.setThumbnailVisibility(8, false);
            hideZoomBar();
            this.mBackButtonManager.setBackButton(true);
        }
    }

    protected void setKnockOffListener(final Context context, boolean enable) {
        if (enable || this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(context, new C02621());
            if (this.mGestureDetector != null) {
                this.mGestureDetector.setOnDoubleTapListener(new OnDoubleTapListener() {
                    public boolean onDoubleTapEvent(MotionEvent me) {
                        CamLog.m3d(CameraConstants.TAG, "setKnockOffListener - onDoubleTapEvent");
                        QuickCoverModuleBase.this.screenOffWithForce(context);
                        return true;
                    }

                    public boolean onSingleTapConfirmed(MotionEvent me) {
                        CamLog.m3d(CameraConstants.TAG, "setKnockOffListener - onSingleTapConfirmed");
                        return false;
                    }

                    public boolean onDoubleTap(MotionEvent me) {
                        CamLog.m3d(CameraConstants.TAG, "setKnockOffListener - onDoubleTap");
                        return false;
                    }
                });
                return;
            }
            return;
        }
        this.mGestureDetector.setOnDoubleTapListener(null);
        this.mGestureDetector = null;
    }

    protected void setCheckLowDistance() {
        CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity()).setLGProxyDataListener(true);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CameraManagerFactory.getAndroidCameraManager(QuickCoverModuleBase.this.mGet.getActivity()) != null) {
                    boolean isLow = CameraManagerFactory.getAndroidCameraManager(QuickCoverModuleBase.this.mGet.getActivity()).isLowDistance();
                    CamLog.m3d(CameraConstants.TAG, "[APLUS] check 1 sec. low distance ?" + isLow);
                    if (isLow) {
                        QuickCoverModuleBase.this.mGet.getActivity().finish();
                        QuickCoverModuleBase.this.screenOffWithForce(QuickCoverModuleBase.this.getAppContext());
                    }
                    CameraManagerFactory.getAndroidCameraManager(QuickCoverModuleBase.this.mGet.getActivity()).setLGProxyDataListener(false);
                }
            }
        }, 1000);
    }

    protected void screenOffWithForce(Context context) {
        boolean isKnockScreenOff = true;
        if (System.getInt(context.getContentResolver(), "gesture_trun_screen_on", 0) != 1) {
            isKnockScreenOff = false;
        }
        if (isKnockScreenOff) {
            try {
                OsManager osManager = (OsManager) new LGContext(context).getLGSystemService("osservice");
                if (osManager != null) {
                    CamLog.m3d(CameraConstants.TAG, "goToSleepWithForce");
                    osManager.goToSleepWithForce(SystemClock.uptimeMillis(), 0);
                }
            } catch (Exception e) {
                CamLog.m5e(CameraConstants.TAG, "goToSleepWithForce Exception : " + e);
            }
        }
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    protected void initTouchCoordinate() {
        this.mIsDragOnPreview = false;
        this.mIsDragToFinish = false;
        this.mTouchMoveOnPreview = false;
        this.mTouch_down_x = 0;
        this.mTouch_down_y = 0;
        this.mTouch_move_x = 0;
        this.mTouch_move_y = 0;
    }

    protected boolean isTouchIn(int viewPage) {
        if (this.mViewPager == null || this.mViewPager.getCurrentItem() != viewPage) {
            return false;
        }
        return true;
    }

    public void showDialog(int id) {
        int toastMsg = -1;
        if (id >= 122 && id <= 128) {
            toastMsg = C0088R.string.sp_storage_full_popup_ics_title_NORMAL;
            this.mIsSavingProgress = false;
        } else if (id == 5) {
            toastMsg = C0088R.string.msg_save_progress;
        } else if (id == 3) {
            toastMsg = C0088R.string.pd_message_processing;
        }
        if (toastMsg > 0) {
            showQuickCircleToast(getActivity().getString(toastMsg));
        }
    }

    protected void showQuickCircleToast(String toastMsg) {
        if (this.mToast != null) {
            if (this.mToastMsg == null || !this.mToastMsg.equals(toastMsg)) {
                this.mToast.cancel();
                this.mToast = null;
            } else {
                return;
            }
        }
        if (getActivity() != null && !this.mGet.isPaused()) {
            Context context = getActivity().getApplicationContext();
            this.mToastMsg = toastMsg;
            int width = Utils.getPx(context, C0088R.dimen.quick_circle_toast_width);
            Paint textPaint = new Paint();
            Rect bound = new Rect();
            textPaint.getTextBounds(toastMsg, 0, toastMsg.length(), bound);
            String message = toastMsg;
            if (width < bound.width()) {
                message = Utils.breakTextToMultiLine(textPaint, toastMsg, width);
            }
            this.mToast = Toast.makeText(context, message, 0);
            if (this.mToast != null) {
                this.mToast.setGravity(49, 0, this.mToastMarginTop);
                this.mToast.getView().setContentDescription(toastMsg);
                this.mToast.show();
            }
            this.mToastMsg = null;
        }
    }

    protected void removeToast() {
        if (this.mToast != null) {
            this.mToast.cancel();
            this.mToast = null;
        }
    }

    public void onStop() {
        if (this.mPagerAdapter != null) {
            this.mPagerAdapter.unbind();
            this.mPagerAdapter = null;
        }
        if (this.mViewPager != null) {
            this.mViewPager.setAdapter(null);
            this.mViewPager = null;
        }
        super.onStop();
    }

    public void onDestroy() {
        if (this.mSaveLastPictureDrawable != null) {
            BitmapManagingUtil.recycleBitmapDrawable(this.mSaveLastPictureDrawable);
            this.mSaveLastPictureDrawable = null;
        }
        ViewGroup vg = (ViewGroup) findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mQuickCirclePagerView == null)) {
            vg.removeView(this.mQuickCirclePagerView);
            this.mQuickCirclePagerView = null;
        }
        this.mCoverManager = null;
        this.mAudioManger = null;
        this.mUri = null;
        super.onDestroy();
    }

    public boolean isSupportedQuickClip() {
        return false;
    }

    protected boolean isFastShotSupported() {
        return false;
    }
}
