package com.lge.camera.app;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.support.p000v4.view.ViewPager;
import android.support.p000v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;
import java.lang.Thread.State;

public class QuickCircleCameraModule extends QuickCoverModuleBase {
    private final int MARGIN_FACTOR = 20;
    protected View mArrowCue;
    protected int mExitButtonId;
    protected final OnPageChangeListener mQuickCirclePageChangeListner = new C03072();
    protected OnTouchListener mQuickcircleTouchListener = new C03061();
    protected AnimationSet mSlideAnimation;

    /* renamed from: com.lge.camera.app.QuickCircleCameraModule$1 */
    class C03061 implements OnTouchListener {
        C03061() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event != null) {
                switch (event.getAction()) {
                    case 0:
                        if (!QuickCircleCameraModule.this.isTouchIn(0)) {
                            if (QuickCircleCameraModule.this.isTouchIn(1) && QuickCircleCameraModule.this.mGestureDetector != null) {
                                QuickCircleCameraModule.this.mGestureDetector.onTouchEvent(event);
                                break;
                            }
                        }
                        QuickCircleCameraModule.this.initTouchCoordinate();
                        QuickCircleCameraModule.this.mTouch_down_x = (int) event.getX();
                        QuickCircleCameraModule.this.mTouch_down_y = (int) event.getY();
                        break;
                    case 1:
                        if (QuickCircleCameraModule.this.isTouchIn(0)) {
                            QuickCircleCameraModule.this.showArrowQue(true);
                            QuickCircleCameraModule.this.checkDragOnPreview();
                            if (QuickCircleCameraModule.this.mIsDragOnPreview) {
                                if (QuickCircleCameraModule.this.mIsDragToFinish) {
                                    QuickCircleCameraModule.this.finishQuickCircleCamera();
                                }
                            } else if (QuickCircleCameraModule.this.checkTouchOnPreview((int) event.getX())) {
                                QuickCircleCameraModule.this.onCameraShutterButtonClicked();
                            }
                        } else if (QuickCircleCameraModule.this.isTouchIn(1) && QuickCircleCameraModule.this.mGestureDetector != null) {
                            QuickCircleCameraModule.this.mGestureDetector.onTouchEvent(event);
                        }
                        QuickCircleCameraModule.this.mTouchMoveOnPreview = false;
                        break;
                    case 2:
                        if (QuickCircleCameraModule.this.isTouchIn(0)) {
                            QuickCircleCameraModule.this.mTouchMoveOnPreview = true;
                            QuickCircleCameraModule.this.mTouch_move_x = (int) event.getX();
                            QuickCircleCameraModule.this.mTouch_move_y = (int) event.getY();
                            break;
                        }
                        break;
                    default:
                        if (QuickCircleCameraModule.this.isTouchIn(1) && QuickCircleCameraModule.this.mGestureDetector != null) {
                            QuickCircleCameraModule.this.mGestureDetector.onTouchEvent(event);
                            break;
                        }
                }
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.app.QuickCircleCameraModule$2 */
    class C03072 implements OnPageChangeListener {
        C03072() {
        }

        public void onPageSelected(int position) {
            QuickCircleCameraModule.this.changeQuickCoverPage(position);
            if (QuickCircleCameraModule.this.mViewPager != null && QuickCircleCameraModule.this.mQuickCirclePagerView != null && QuickCircleCameraModule.this.mPagerAdapter != null) {
                View exitButton = QuickCircleCameraModule.this.mQuickCirclePagerView.findViewById(QuickCircleCameraModule.this.mExitButtonId);
                if (position == 1) {
                    QuickCircleCameraModule.this.mIsPageChanging = true;
                    if (QuickCircleCameraModule.this.mIsSavingProgress || QuickCircleCameraModule.this.mGet.getQueueCount() > 0) {
                        QuickCircleCameraModule.this.mViewPager.setCurrentItem(0, true);
                        QuickCircleCameraModule.this.showDialog(5);
                        return;
                    }
                    QuickCircleCameraModule.this.removeToast();
                    QuickCircleCameraModule.this.showArrowQue(false);
                    QuickCircleCameraModule.this.mViewPager.setOverScrollMode(0);
                    if (exitButton != null) {
                        exitButton.setBackgroundResource(C0088R.drawable.selector_quickwindow_return_btn);
                        exitButton.setVisibility(0);
                        exitButton.setContentDescription(QuickCircleCameraModule.this.mGet.getActivity().getString(C0088R.string.app_name));
                    }
                } else if (position == 0) {
                    QuickCircleCameraModule.this.mIsPageChanging = false;
                    QuickCircleCameraModule.this.showArrowQue(true);
                    QuickCircleCameraModule.this.mViewPager.setOverScrollMode(2);
                    if (exitButton == null) {
                        return;
                    }
                    if (QuickCircleCameraModule.this.mCoverType == 6) {
                        exitButton.setVisibility(8);
                        return;
                    }
                    exitButton.setBackgroundResource(C0088R.drawable.selector_quickwindow_back_btn);
                    exitButton.setContentDescription(QuickCircleCameraModule.this.mGet.getActivity().getString(C0088R.string.camera_accessibility_back_button));
                }
            }
        }

        public void onPageScrolled(int page, float offset, int offsetPixels) {
        }

        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case 0:
                case 2:
                    QuickCircleCameraModule.this.mIsPageChanging = false;
                    return;
                case 1:
                    QuickCircleCameraModule.this.mIsPageChanging = true;
                    QuickCircleCameraModule.this.showArrowQue(false);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.lge.camera.app.QuickCircleCameraModule$3 */
    class C03083 implements OnClickListener {
        C03083() {
        }

        public void onClick(View v) {
            if (QuickCircleCameraModule.this.mViewPager == null) {
                QuickCircleCameraModule.this.finishQuickCircleCamera();
            } else if (QuickCircleCameraModule.this.mViewPager.getCurrentItem() == 0) {
                QuickCircleCameraModule.this.finishQuickCircleCamera();
            } else {
                QuickCircleCameraModule.this.mViewPager.setCurrentItem(0);
            }
        }
    }

    /* renamed from: com.lge.camera.app.QuickCircleCameraModule$4 */
    class C03094 implements AnimationListener {

        /* renamed from: com.lge.camera.app.QuickCircleCameraModule$4$1 */
        class C03101 implements Runnable {
            C03101() {
            }

            public void run() {
                QuickCircleCameraModule.this.savePage();
            }
        }

        C03094() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (QuickCircleCameraModule.this.mQuickAnimView != null) {
                QuickCircleCameraModule.this.mQuickAnimView.setImageDrawable(null);
                QuickCircleCameraModule.this.mQuickAnimView.setVisibility(4);
                if (QuickCircleCameraModule.this.checkModuleValidate(31) && QuickCircleCameraModule.this.mQuickAnimView != null) {
                    QuickCircleCameraModule.this.getHandler().postDelayed(new C03101(), 100);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.QuickCircleCameraModule$5 */
    class C03115 implements Runnable {
        C03115() {
        }

        public void run() {
            QuickCircleCameraModule.this.savePage();
        }
    }

    /* renamed from: com.lge.camera.app.QuickCircleCameraModule$6 */
    class C03126 extends Thread {
        C03126() {
        }

        public void run() {
            if (!QuickCircleCameraModule.this.mGet.isPaused()) {
                QuickCircleCameraModule.this.mSaveLastPictureDrawable = new BitmapDrawable(QuickCircleCameraModule.this.mGet.getAppContext().getResources(), QuickCircleCameraModule.this.loadCapturedImage(QuickCircleCameraModule.this.getUri(), 90));
                QuickCircleCameraModule.this.mGet.postOnUiThread(new HandlerRunnable(QuickCircleCameraModule.this) {
                    public void handleRun() {
                        if (!QuickCircleCameraModule.this.mGet.isPaused() && QuickCircleCameraModule.this.mPagerAdapter != null) {
                            QuickCircleCameraModule.this.mPagerAdapter.addPicture(QuickCircleCameraModule.this.mSaveLastPictureDrawable);
                            QuickCircleCameraModule.this.mIsSavingProgress = false;
                            QuickCircleCameraModule.this.showArrowQue(true);
                            QuickCircleCameraModule.this.removeToast();
                        }
                    }
                });
            }
        }
    }

    public QuickCircleCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        judgePreviewRatio();
        getValFromFwk();
        changeQuickCoverPage(0);
        this.mAudioManger = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        this.mQuickCirclePagerView = (RelativeLayout) inflateView(C0088R.layout.quick_circle_pager_view);
        if (this.mQuickCirclePagerView != null) {
            ViewGroup vg = (ViewGroup) findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                vg.addView(this.mQuickCirclePagerView, new LayoutParams(-1, -1));
            }
            initQuickCircleLayout();
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        judgePreviewRatio();
        showQuickCircleView(true);
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(0);
        }
        doCleanView(false, false, false);
        setFocusInVisible(true);
        if (this.mBackButtonManager != null) {
            this.mBackButtonManager.hide();
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        showQuickCircleView(false);
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(1);
        }
        this.mUri = null;
        this.mToast = null;
        this.mToastMsg = null;
        this.mSurfaceCapture = null;
        showQuickCircleView(false);
        if (this.mPostviewManager != null && this.mPostviewManager.isPostviewShowing()) {
            this.mPostviewManager.releaseLayout();
        }
        doCleanView(false, false, false);
        setFocusInVisible(false);
        this.mGet.setReviewThumbBmp(null);
    }

    public void onDestroy() {
        this.mArrowCue = null;
        this.mSlideAnimation = null;
        super.onDestroy();
    }

    private void initQuickCircleLayout() {
        RelativeLayout pagerView = (RelativeLayout) this.mQuickCirclePagerView.findViewById(C0088R.id.quick_circle_camera_view);
        pagerView.setLayoutParams(setQuickCircleLayoutParam((LayoutParams) pagerView.getLayoutParams(), false));
        this.mPagerAdapter = new QuickCirclePagerAdapter(this.mGet.getAppContext());
        this.mViewPager = (ViewPager) findViewById(C0088R.id.quick_circle_pager_view);
        if (this.mViewPager != null && this.mPagerAdapter != null) {
            this.mPagerAdapter.setValue(this.mQuickCircle_height, this.mQuickCircle_width, this.mCoverType, this.mCoverManager.getQuickCoverRatio());
            this.mViewPager.setPaddingRelative(this.mPadding, 0, this.mPadding, 0);
            this.mViewPager.setAdapter(this.mPagerAdapter);
            this.mViewPager.setOffscreenPageLimit(1);
            this.mViewPager.setOverScrollMode(2);
            this.mArrowCue = this.mQuickCirclePagerView.findViewById(C0088R.id.arrow_image_view);
            if (this.mArrowCue != null) {
                LayoutParams cueParam = (LayoutParams) this.mArrowCue.getLayoutParams();
                cueParam.setMarginStart((this.mQuickCircle_width + this.mQuickCircle_height) / 2);
                this.mArrowCue.setLayoutParams(cueParam);
                this.mArrowCue.setVisibility(0);
            }
            this.mExitButtonId = C0088R.id.quick_circle_exit_btn;
            View exitButton = this.mQuickCirclePagerView.findViewById(this.mExitButtonId);
            if (exitButton != null) {
                LayoutParams exitParam = (LayoutParams) exitButton.getLayoutParams();
                int bottomMargin = this.mQuickCircle_width / 20;
                if (this.mCoverManager.getQuickCoverRatio() == 2) {
                    bottomMargin += (this.mQuickCircle_width_wide - this.mQuickCircle_width) / 2;
                }
                exitParam.bottomMargin = bottomMargin;
                exitButton.setLayoutParams(exitParam);
                exitButton.bringToFront();
                exitButton.invalidate();
            }
            showArrowQue(false);
            this.mToastMarginTop = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_toast_marginTop);
            this.mQuickAnimView = (ImageView) this.mQuickCirclePagerView.findViewById(C0088R.id.quick_slide_view);
            this.mQuickAnimView.setLayoutParams(setQuickCircleLayoutParam((LayoutParams) this.mQuickAnimView.getLayoutParams(), true));
        }
    }

    protected void setViewPagerListener(boolean set) {
        if (this.mViewPager != null && this.mQuickCirclePagerView != null && this.mPagerAdapter != null) {
            if (set) {
                this.mViewPager.setOnTouchListener(this.mQuickcircleTouchListener);
                this.mViewPager.setOnPageChangeListener(this.mQuickCirclePageChangeListner);
                return;
            }
            this.mViewPager.setOnTouchListener(null);
            this.mViewPager.setOnPageChangeListener(null);
        }
    }

    protected boolean checkTouchOnPreview(int x) {
        int start = (int) (this.mViewPager.getX() + ((float) this.mPadding));
        int end = start + (this.mCoverManager.getQuickCoverRatio() == 3 ? this.mQuickCircle_width : this.mQuickCircle_height);
        if (start > x || x > end) {
            return false;
        }
        return true;
    }

    protected void checkDragOnPreview() {
        int touch_x = this.mTouch_move_x - this.mTouch_down_x;
        int touch_y = this.mTouch_move_y - this.mTouch_down_y;
        if (touch_x < 0) {
            touch_x *= -1;
        }
        if (touch_y < 0) {
            touch_y *= -1;
        }
        if ((touch_x > 50 || touch_y > 50) && this.mTouchMoveOnPreview) {
            CamLog.m7i(CameraConstants.TAG, "drag on QuickCircle Preview");
            this.mIsDragOnPreview = true;
        }
    }

    protected void setExitButtonListener(boolean enable) {
        if (this.mQuickCirclePagerView != null && this.mPagerAdapter != null && this.mViewPager != null) {
            View exitBtn = this.mQuickCirclePagerView.findViewById(this.mExitButtonId);
            if (exitBtn == null) {
                return;
            }
            if (enable) {
                exitBtn.setOnClickListener(new C03083());
            } else {
                exitBtn.setOnClickListener(null);
            }
        }
    }

    protected boolean takePicture() {
        if (this.mViewPager == null) {
            CamLog.m3d(CameraConstants.TAG, "takePicture : mViewPager is null. return");
            return false;
        } else if (this.mViewPager.getCurrentItem() == 0) {
            this.mIsSavingProgress = true;
            return super.takePicture();
        } else {
            this.mIsSavingProgress = false;
            CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] takePicture fail");
            return true;
        }
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

    protected void createSlideAnimation() {
        this.mSlideAnimation = new AnimationSet(false);
        this.mSlideAnimation.addAnimation(new TranslateAnimation(1, 0.0f, 1, 1.0f, 1, 0.0f, 1, 0.0f));
        this.mSlideAnimation.setDuration(300);
        this.mSlideAnimation.setAnimationListener(new C03094());
    }

    private void showQuickCircleView(boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] showQuickCircleView : " + enable);
        if (this.mViewPager != null) {
            this.mViewPager.setVisibility(enable ? 0 : 8);
        }
        this.mQuickAnimView = null;
        setViewPagerListener(enable);
        setExitButtonListener(enable);
        setKnockOffListener(this.mGet.getAppContext(), enable);
        initTouchCoordinate();
        if (enable) {
            createSlideAnimation();
        }
    }

    protected void showArrowQue(boolean set) {
        if (this.mQuickCirclePagerView != null) {
            if (this.mArrowCue == null) {
                this.mArrowCue = this.mQuickCirclePagerView.findViewById(C0088R.id.arrow_image_view);
            }
            if (this.mArrowCue != null && this.mArrowCue != null) {
                if (!set || this.mUri == null || this.mPagerAdapter == null || this.mPagerAdapter.isEmpty() || this.mCoverManager.getQuickCoverPage() != 0) {
                    this.mArrowCue.setVisibility(8);
                } else {
                    this.mArrowCue.setVisibility(0);
                }
            }
        }
    }

    public int getCameraIdFromPref() {
        return 0;
    }

    protected void setupPreview(CameraParameters params) {
        if (this.mCameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera device is null.");
            return;
        }
        CameraParameters parameters;
        if (params == null) {
            parameters = this.mCameraDevice.getParameters();
        } else {
            parameters = params;
        }
        setParameters(parameters);
        startPreview(parameters);
        setCameraState(1);
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
        judgePreviewRatio();
        getValFromFwk();
        super.setTextureLayoutParam();
    }

    protected Drawable getSurfaceBitmap() {
        Bitmap bitmap = null;
        TextureView textureView = this.mGet.getTextureView();
        if (textureView != null) {
            try {
                bitmap = textureView.getBitmap();
            } catch (OutOfMemoryError e) {
                CamLog.m5e(CameraConstants.TAG, "OutOfMemory Error : " + e);
            }
        } else {
            CamLog.m11w(CameraConstants.TAG, "TextureView was null, so bitmap would be null.");
        }
        if (bitmap != null) {
            return new BitmapDrawable(this.mGet.getAppContext().getResources(), bitmap);
        }
        return null;
    }

    public void startAnimationToCapture() {
        if (checkModuleValidate(15)) {
            if (!(this.mSlideAnimation == null || this.mQuickAnimView == null || !this.mSlideAnimation.hasStarted())) {
                this.mQuickAnimView.setVisibility(4);
                this.mSlideAnimation.cancel();
                this.mQuickAnimView.clearAnimation();
            }
            if (this.mQuickAnimView == null) {
                this.mQuickAnimView = (ImageView) this.mQuickCirclePagerView.findViewById(C0088R.id.quick_slide_view);
                this.mQuickAnimView.setLayoutParams(setQuickCircleLayoutParam((LayoutParams) this.mQuickAnimView.getLayoutParams(), true));
            }
            if (this.mQuickAnimView != null && this.mViewPager != null && !this.mSnapShotChecker.checkMultiShotState(5)) {
                this.mQuickAnimView.setVisibility(0);
                if (this.mSurfaceCapture != null) {
                    this.mQuickAnimView.setImageDrawable(this.mSurfaceCapture);
                } else {
                    this.mQuickAnimView.setImageDrawable(getSurfaceBitmap());
                }
                if (this.mSlideAnimation == null) {
                    createSlideAnimation();
                }
                this.mQuickAnimView.startAnimation(this.mSlideAnimation);
            }
        }
    }

    protected Bitmap loadCapturedImage(Uri uri, int degrees) {
        if (this.mGet.getActivity() == null || uri == null) {
            return null;
        }
        return BitmapManagingUtil.loadScaledandRotatedBitmap(this.mGet.getActivity().getContentResolver(), uri.toString(), this.mCurrentQuickCircleSize[0], this.mCurrentQuickCircleSize[1], 90);
    }

    public void notifyNewMedia(Uri uri, boolean updateThumbnail) {
        this.mUri = uri;
        if (checkBurst(uri) || this.mCoverType == 6) {
            getHandler().postDelayed(new C03115(), 0);
        } else {
            startAnimationToCapture();
        }
        super.notifyNewMedia(uri, false);
    }

    protected void savePage() {
        if (checkModuleValidate(31) || this.mPagerAdapter == null || this.mPagerAdapter.isEmpty()) {
            this.mSaveImgThread = new C03126();
            if (this.mSaveImgThread != null && this.mSaveImgThread.getState() == State.NEW) {
                this.mSaveImgThread.start();
            }
        }
    }

    protected void onShutterCallback(boolean sound, boolean animation, boolean recording) {
        super.onShutterCallback(true, true, false);
        this.mSurfaceCapture = getSurfaceBitmap();
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

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        saveImage(data, extraExif);
        if (checkModuleValidate(15)) {
            if (checkModuleValidate(128)) {
                revertParameterByLGSF(null);
            }
            if ((FunctionProperties.isSupportedHDR(isRearCamera()) == 1 && "1".equals(getSettingValue("hdr-mode"))) || !FunctionProperties.isSupportedZSL(this.mCameraId)) {
                setupPreview(null);
            }
            onTakePictureAfter();
            if (this.mFocusManager != null && isFocusEnableCondition()) {
                this.mFocusManager.registerCallback(true);
            }
            if (this.mTempGetAudioFocus) {
                AudioUtil.setAudioFocus(getAppContext(), false);
                this.mTempGetAudioFocus = false;
            }
        }
        releaseCaptureProgressOnPictureCallback();
    }

    public void changeCoverState(boolean isCoverClosed) {
        if (!isCoverClosed) {
            if (this.mViewPager != null && this.mViewPager.getCurrentItem() == 1) {
                this.mGet.launchGallery(getUri(), 0);
            }
            if (SharedPreferenceUtil.getCameraId(this.mGet.getAppContext()) != 0) {
                this.mGet.setForcedSetting(Setting.KEY_SWAP_CAMERA, "rear");
                SharedPreferenceUtilBase.setCameraId(getAppContext(), 0);
                this.mGet.setupSetting();
            }
            CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] quick window camera flow : change the normal module.");
            if (checkModuleValidate(1) && !AppControlUtil.isGalleryLaunched()) {
                this.mGet.changeModuleByCoverstate();
            }
        }
    }

    public void onShutterQuickButtonClickListener() {
        if ((this.mPostviewManager == null || !this.mPostviewManager.isPostviewShowing()) && stopBurstShotTaking(false)) {
        }
    }

    public void setUpAllViews() {
        judgePreviewRatio();
        getValFromFwk();
        showQuickCircleView(true);
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(0);
        }
        if (6 == getCameraState()) {
            hideRecordingUI();
        }
        doCleanView(true, false, false);
        showQuickCircleView(true);
    }

    protected void getValFromFwk() {
        int top_margin = 0;
        this.mCoverType = QuickWindowUtils.getSmartCoverManager(this.mGet.getActivity()).getCoverType();
        int id_width = this.mGet.getActivity().getResources().getIdentifier("config_circle_window_width", "dimen", "com.lge.internal");
        int id_height = this.mGet.getActivity().getResources().getIdentifier("config_circle_window_height", "dimen", "com.lge.internal");
        int id_y_pos = this.mGet.getActivity().getResources().getIdentifier("config_circle_window_y_pos", "dimen", "com.lge.internal");
        if (id_width == 0 || id_height == 0 || id_y_pos == 0) {
            this.mCoverHeight = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_width);
        } else {
            this.mCoverWidth = this.mGet.getActivity().getResources().getDimensionPixelSize(id_width);
            this.mCoverHeight = this.mGet.getActivity().getResources().getDimensionPixelSize(id_height);
            top_margin = this.mGet.getActivity().getResources().getDimensionPixelSize(id_y_pos);
            CamLog.m7i(CameraConstants.TAG, "[QUICK COVER] Circle size from defined value: " + this.mCoverWidth + "x" + this.mCoverHeight + " , top:" + top_margin);
            if (this.mCoverHeight == 0) {
                this.mCoverHeight = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_width);
            }
        }
        this.mQuickCircle_width = this.mCoverHeight;
        this.mQuickCircle_height = (int) (((float) this.mQuickCircle_width) / 1.3333334f);
        this.mQuickCircle_width_wide = (int) (((float) this.mQuickCircle_height) * 1.7777778f);
        this.mPadding = (this.mQuickCircle_width - this.mQuickCircle_height) / 2;
        switch (this.mCoverManager.getQuickCoverRatio()) {
            case 2:
                this.mQuickCircle_marginTop = (((this.mQuickCircle_width_wide - this.mQuickCircle_width) / 2) * -1) + top_margin;
                return;
            case 3:
                this.mPadding = 0;
                break;
        }
        this.mQuickCircle_marginTop = top_margin;
    }

    private LayoutParams setQuickCircleLayoutParam(LayoutParams layoutParams, boolean isChildView) {
        int width;
        int height;
        int topMargin;
        switch (this.mCoverManager.getQuickCoverRatio()) {
            case 1:
                width = this.mQuickCircle_width;
                height = this.mQuickCircle_width;
                topMargin = this.mQuickCircle_marginTop;
                break;
            case 2:
                width = this.mQuickCircle_width;
                height = this.mQuickCircle_width_wide;
                topMargin = this.mQuickCircle_marginTop;
                break;
            default:
                width = this.mQuickCircle_width;
                height = this.mQuickCircle_width;
                topMargin = this.mQuickCircle_marginTop;
                break;
        }
        layoutParams = new LayoutParams(width, height);
        if (isChildView) {
            topMargin = 0;
        }
        layoutParams.topMargin = topMargin;
        layoutParams.addRule(14, 1);
        return layoutParams;
    }

    public synchronized void setBeautyEngineOn(boolean bIsOn) {
        releaseEngine();
        super.setBeautyEngineOn(false);
    }
}
