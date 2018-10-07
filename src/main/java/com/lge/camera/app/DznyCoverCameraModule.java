package com.lge.camera.app;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;

public class DznyCoverCameraModule extends QuickCircleCameraModule {
    private final String DZ_PIC_SIZE = "3120x3120";
    private final String DZ_PREVIEW_SIZE = "1080x1080";
    private final int SWIPE_VELOCITY = 500;
    protected OnTouchListener mDznyTouchListener = new C03013();
    protected GestureDetector mGestureDetector;
    private final OnGestureListener mGestureListener = new C03024();
    private boolean mIsDznySizeComplete = false;
    private Float mSwipeDistance;

    /* renamed from: com.lge.camera.app.DznyCoverCameraModule$1 */
    class C02991 implements OnClickListener {
        C02991() {
        }

        public void onClick(View arg0) {
            if (DznyCoverCameraModule.this.mViewPager.getCurrentItem() == 1) {
                DznyCoverCameraModule.this.mViewPager.setCurrentItem(0, true);
            } else if (DznyCoverCameraModule.this.mIsSavingProgress || !DznyCoverCameraModule.this.checkModuleValidate(31)) {
                DznyCoverCameraModule.this.mViewPager.setCurrentItem(0, true);
                DznyCoverCameraModule.this.showDialog(5);
            } else {
                DznyCoverCameraModule.this.mViewPager.setCurrentItem(1, true);
                DznyCoverCameraModule.this.removeToast();
            }
        }
    }

    /* renamed from: com.lge.camera.app.DznyCoverCameraModule$2 */
    class C03002 implements OnClickListener {
        C03002() {
        }

        public void onClick(View v) {
            if (DznyCoverCameraModule.this.mViewPager == null) {
                DznyCoverCameraModule.this.finishQuickCircleCamera();
            } else if (DznyCoverCameraModule.this.mViewPager.getCurrentItem() == 0) {
                DznyCoverCameraModule.this.finishQuickCircleCamera();
            } else {
                DznyCoverCameraModule.this.mViewPager.setCurrentItem(0);
            }
        }
    }

    /* renamed from: com.lge.camera.app.DznyCoverCameraModule$3 */
    class C03013 implements OnTouchListener {
        C03013() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            DznyCoverCameraModule.this.mGestureDetector.onTouchEvent(event);
            return true;
        }
    }

    /* renamed from: com.lge.camera.app.DznyCoverCameraModule$4 */
    class C03024 implements OnGestureListener {
        C03024() {
        }

        public boolean onDown(MotionEvent arg0) {
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean isSwipe = false;
            if (e1.getX() - e2.getX() > DznyCoverCameraModule.this.mSwipeDistance.floatValue() && Math.abs(velocityX) > 500.0f) {
                isSwipe = true;
            } else if (e2.getX() - e1.getX() > DznyCoverCameraModule.this.mSwipeDistance.floatValue() && Math.abs(velocityX) > 500.0f) {
                isSwipe = true;
            } else if (e1.getY() - e2.getY() > DznyCoverCameraModule.this.mSwipeDistance.floatValue() && Math.abs(velocityY) > 500.0f) {
                isSwipe = true;
            } else if (e2.getY() - e1.getY() > DznyCoverCameraModule.this.mSwipeDistance.floatValue() && Math.abs(velocityY) > 500.0f) {
                isSwipe = true;
            }
            if (isSwipe) {
                doFlingEventForDzny();
            }
            return true;
        }

        private void doFlingEventForDzny() {
            if (1 == DznyCoverCameraModule.this.mViewPager.getCurrentItem()) {
                DznyCoverCameraModule.this.mViewPager.setCurrentItem(0);
            } else {
                DznyCoverCameraModule.this.finishQuickCircleCamera();
            }
        }

        public void onLongPress(MotionEvent arg0) {
        }

        public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
            return false;
        }

        public void onShowPress(MotionEvent arg0) {
        }

        public boolean onSingleTapUp(MotionEvent arg0) {
            DznyCoverCameraModule.this.onCameraShutterButtonClicked();
            return false;
        }
    }

    public DznyCoverCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
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
        startPreview(changeDznySize(parameters));
        setCameraState(1);
    }

    public void init() {
        this.mIsDznySizeComplete = false;
        super.init();
        this.mExitButtonId = C0088R.id.dzny_exit_btn;
        this.mSwipeDistance = Float.valueOf(TypedValue.applyDimension(5, 14.0f, this.mGet.getAppContext().getResources().getDisplayMetrics()));
        this.mReviewThumbnailManager.setThumbnailVisibility(8);
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        View notUsed = this.mQuickCirclePagerView.findViewById(C0088R.id.quick_circle_exit_btn);
        View notUsed2 = this.mQuickCirclePagerView.findViewById(C0088R.id.arrow_image_view);
        if (!(notUsed == null || notUsed2 == null)) {
            notUsed.setVisibility(8);
            notUsed2.setVisibility(8);
        }
        this.mQuickCirclePagerView.addView(inflateView(C0088R.layout.dzny_button_view));
        this.mArrowCue = this.mQuickCirclePagerView.findViewById(C0088R.id.dzny_thumb_view);
        changeQuickCoverPage(0);
        this.mGestureDetector = new GestureDetector(this.mGestureListener);
        this.mViewPager.setOffscreenPageLimit(0);
    }

    protected void getValFromFwk() {
        int top_margin = 0;
        this.mCoverType = QuickWindowUtils.getSmartCoverManager(getActivity()).getCoverType();
        int id_width = this.mGet.getActivity().getResources().getIdentifier("config_disney_total_width", "dimen", "com.lge.internal");
        int id_height = this.mGet.getActivity().getResources().getIdentifier("config_disney_total_height", "dimen", "com.lge.internal");
        int id_y_pos = this.mGet.getActivity().getResources().getIdentifier("config_circle_window_y_pos", "dimen", "com.lge.internal");
        if (id_width == 0 || id_height == 0 || id_y_pos == 0) {
            this.mCoverHeight = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_width);
        } else {
            this.mCoverWidth = this.mGet.getActivity().getResources().getDimensionPixelSize(id_width);
            this.mCoverHeight = this.mGet.getActivity().getResources().getDimensionPixelSize(id_height);
            top_margin = this.mGet.getActivity().getResources().getDimensionPixelSize(id_y_pos);
            CamLog.m7i(CameraConstants.TAG, "[QUICK COVER] Dzny defined value : " + this.mCoverWidth + "x" + this.mCoverHeight + " , top:" + top_margin);
            if (this.mCoverHeight == 0) {
                this.mCoverHeight = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_width);
            }
        }
        this.mQuickCircle_width = this.mCoverWidth;
        this.mQuickCircle_height = (int) (((float) this.mQuickCircle_width) / 1.3333334f);
        this.mQuickCircle_width_wide = (int) (((float) this.mQuickCircle_height) * 1.7777778f);
        this.mPadding = (this.mQuickCircle_width - this.mQuickCircle_height) / 2;
        switch (this.mCoverManager.getQuickCoverRatio()) {
            case 2:
                this.mQuickCircle_marginTop = (((this.mQuickCircle_width_wide - this.mQuickCircle_width) / 2) * -1) + top_margin;
                return;
            case 3:
                this.mPadding = (((this.mCoverWidth - this.mCoverHeight) / 2) * -1) + top_margin;
                this.mQuickCircle_marginTop = top_margin;
                return;
            default:
                this.mQuickCircle_marginTop = top_margin;
                return;
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        showArrowQue(false);
    }

    private CameraParameters changeDznySize(CameraParameters param) {
        if (this.mCameraDevice == null || this.mIsDznySizeComplete) {
            return null;
        }
        ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        if (param == null) {
            param = this.mCameraDevice.getParameters();
        }
        String curPicSize = listPref.getValue();
        listPref.getExtraInfo(1);
        if (!(curPicSize.equals("3120x3120") || param == null)) {
            setParamUpdater(param, "picture-size", "3120x3120");
            setParamUpdater(param, ParamConstants.KEY_PREVIEW_SIZE, "1080x1080");
            this.mHandler.sendEmptyMessage(61);
            this.mIsDznySizeComplete = true;
        }
        return param;
    }

    protected void judgePreviewRatio() {
        this.mCoverManager.setQuickCoverRatio(3);
    }

    protected void setViewPagerListener(boolean set) {
        if (this.mViewPager != null && this.mQuickCirclePagerView != null && this.mPagerAdapter != null) {
            if (set) {
                this.mViewPager.setOnTouchListener(this.mDznyTouchListener);
                this.mViewPager.setOnPageChangeListener(this.mQuickCirclePageChangeListner);
                return;
            }
            this.mViewPager.setOnTouchListener(null);
            this.mViewPager.setOnPageChangeListener(null);
        }
    }

    protected void showArrowQue(boolean set) {
        if (this.mQuickCirclePagerView != null) {
            if (this.mArrowCue == null) {
                this.mArrowCue = this.mQuickCirclePagerView.findViewById(C0088R.id.dzny_thumb_view);
            }
            if (set && this.mUri != null && this.mCoverManager.getQuickCoverPage() == 0) {
                if (this.mArrowCue.getVisibility() != 0) {
                    this.mArrowCue.setVisibility(0);
                }
                this.mArrowCue.setOnClickListener(new C02991());
                return;
            }
            this.mArrowCue.setVisibility(8);
        }
    }

    public void notifyNewMedia(Uri uri, boolean updateThumbnail) {
        if (checkBurst(uri)) {
            updateDznyThumbnail(uri);
        } else {
            this.mUri = uri;
            showArrowQue(true);
        }
        super.notifyNewMedia(uri, false);
    }

    private void setRoundThumbnail(Bitmap bitmap) {
        if (this.mQuickCirclePagerView != null) {
            View dznyGallery = this.mQuickCirclePagerView.findViewById(C0088R.id.dzny_thumb_view);
            if (dznyGallery != null) {
                if (dznyGallery.getVisibility() != 0) {
                    showArrowQue(true);
                }
                ImageView dznyImg = (ImageView) dznyGallery.findViewById(C0088R.id.dzny_thumb_img);
                if (dznyImg != null) {
                    dznyImg.setImageBitmap(BitmapManagingUtil.getRoundedImage(bitmap, 130, 130, 130));
                }
            }
        }
    }

    protected void setExitButtonListener(boolean enable) {
        if (this.mQuickCirclePagerView != null && this.mPagerAdapter != null && this.mViewPager != null) {
            View exitBtn = this.mQuickCirclePagerView.findViewById(C0088R.id.dzny_exit_btn);
            if (exitBtn == null) {
                return;
            }
            if (enable) {
                exitBtn.setOnClickListener(new C03002());
                return;
            }
            exitBtn.setVisibility(8);
            exitBtn.setOnClickListener(null);
        }
    }

    protected Drawable getSurfaceBitmap() {
        Bitmap bitmap = null;
        TextureView textureView = this.mGet.getTextureView();
        if (textureView != null) {
            bitmap = textureView.getBitmap();
            setRoundThumbnail(bitmap);
        } else {
            CamLog.m11w(CameraConstants.TAG, "TextureView was null, so bitmap would be null.");
        }
        if (bitmap != null) {
            return new BitmapDrawable(this.mGet.getAppContext().getResources(), bitmap);
        }
        return null;
    }

    public void setBurstCaptureEffect() {
        ImageView burstEffect = (ImageView) this.mQuickCirclePagerView.findViewById(C0088R.id.dzny_thumb_effect);
        burstEffect.setImageResource(C0088R.drawable.shutter_gallery_burst_effect);
        burstEffect.setVisibility(4);
    }

    public void startBurstCaptureEffect(boolean visible) {
        ImageView burstEffect = (ImageView) this.mQuickCirclePagerView.findViewById(C0088R.id.dzny_thumb_effect);
        if (visible) {
            AnimationUtil.startSnapShotAnimation(burstEffect, false, (long) FunctionProperties.getSupportedBurstShotDuration(false, false, isRearCamera()), null);
            return;
        }
        burstEffect.setImageDrawable(null);
        burstEffect.setVisibility(8);
    }

    protected void takePictureLongShot(boolean isInternalStorage) {
        if (!checkModuleValidate(79) || this.mSnapShotChecker.isBurstCountMax(isInternalStorage, false, isRearCamera())) {
            if (this.mButtonCheckTimer != null) {
                this.mButtonCheckTimer.cancel();
                this.mButtonCheckTimer.purge();
                this.mButtonCheckTimer = null;
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DznyCoverCameraModule.this.stopBurstShotTaking(false);
                }
            });
            return;
        }
        if (this.mSnapShotChecker.checkBurstState(4) && this.mSnapShotChecker.isBurstCaptureStarted()) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (DznyCoverCameraModule.this.mSnapShotChecker.getBurstCount() == 1) {
                        DznyCoverCameraModule.this.setBurstCaptureEffect();
                    }
                    DznyCoverCameraModule.this.startBurstCaptureEffect(true);
                }
            });
        }
        this.mCameraDevice.takePictureDirect(this.mHandler, this.mLongShotShutterCallback, null, null, this.mLongShotPictureCallback);
    }

    private void updateDznyThumbnail(Uri uri) {
        final Uri bitmapUri = uri;
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                DznyCoverCameraModule.this.setRoundThumbnail(DznyCoverCameraModule.this.loadCapturedImage(bitmapUri, 90));
            }
        });
    }
}
