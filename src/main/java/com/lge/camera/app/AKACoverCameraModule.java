package com.lge.camera.app;

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
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.file.Exif;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import java.lang.Thread.State;

public class AKACoverCameraModule extends QuickCoverModuleBase {
    private ImageView mAnimView = null;
    private boolean mIsDragForSwapCamera = false;
    private final OnPageChangeListener mViewPagerChangeListener = new C00902();
    private final OnTouchListener mViewPagerTouchListener = new C00891();

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$1 */
    class C00891 implements OnTouchListener {
        C00891() {
        }

        public boolean onTouch(android.view.View r5, android.view.MotionEvent r6) {
            /*
            r4 = this;
            r3 = 1;
            r2 = 0;
            if (r6 != 0) goto L_0x0005;
        L_0x0004:
            return r2;
        L_0x0005:
            r0 = r6.getAction();
            switch(r0) {
                case 0: goto L_0x002c;
                case 1: goto L_0x0022;
                case 2: goto L_0x0062;
                default: goto L_0x000c;
            };
        L_0x000c:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0 = r0.isTouchIn(r3);
            if (r0 == 0) goto L_0x0004;
        L_0x0014:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0 = r0.mGestureDetector;
            if (r0 == 0) goto L_0x0004;
        L_0x001a:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0 = r0.mGestureDetector;
            r0.onTouchEvent(r6);
            goto L_0x0004;
        L_0x0022:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0.doTouchUpInViewPager(r6);
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0.mTouchMoveOnPreview = r2;
            goto L_0x0004;
        L_0x002c:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0 = r0.isTouchIn(r2);
            if (r0 == 0) goto L_0x0062;
        L_0x0034:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0.initTouchCoordinate();
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r1 = r6.getX();
            r1 = (int) r1;
            r0 = r0.checkTouchOnPreview(r1);
            if (r0 == 0) goto L_0x005d;
        L_0x0046:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0.mTouchMoveOnPreview = r3;
        L_0x004a:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r1 = r6.getX();
            r1 = (int) r1;
            r0.mTouch_down_x = r1;
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r1 = r6.getY();
            r1 = (int) r1;
            r0.mTouch_down_y = r1;
            goto L_0x0004;
        L_0x005d:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0.mTouchMoveOnPreview = r2;
            goto L_0x004a;
        L_0x0062:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r0 = r0.isTouchIn(r2);
            if (r0 == 0) goto L_0x000c;
        L_0x006a:
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r1 = r6.getX();
            r1 = (int) r1;
            r0.mTouch_move_x = r1;
            r0 = com.lge.camera.app.AKACoverCameraModule.this;
            r1 = r6.getY();
            r1 = (int) r1;
            r0.mTouch_move_y = r1;
            goto L_0x0004;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.AKACoverCameraModule.1.onTouch(android.view.View, android.view.MotionEvent):boolean");
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$2 */
    class C00902 implements OnPageChangeListener {
        C00902() {
        }

        public void onPageSelected(int position) {
            AKACoverCameraModule.this.changeQuickCoverPage(position);
            if (AKACoverCameraModule.this.mViewPager != null && AKACoverCameraModule.this.mQuickCirclePagerView != null) {
                if (position == 1) {
                    AKACoverCameraModule.this.mIsPageChanging = true;
                    if (AKACoverCameraModule.this.mIsSavingProgress || !AKACoverCameraModule.this.checkModuleValidate(31)) {
                        AKACoverCameraModule.this.mViewPager.setCurrentItem(0, true);
                        AKACoverCameraModule.this.showDialog(5);
                        return;
                    }
                    AKACoverCameraModule.this.removeToast();
                    AKACoverCameraModule.this.setExitButtonListener(false);
                    AKACoverCameraModule.this.setQuickCircleShutterButtonListener(false);
                    AKACoverCameraModule.this.showArrowQue(true);
                } else if (position == 0) {
                    AKACoverCameraModule.this.mIsPageChanging = false;
                    AKACoverCameraModule.this.showArrowQue(true);
                    AKACoverCameraModule.this.setExitButtonListener(true);
                    AKACoverCameraModule.this.setQuickCircleShutterButtonListener(true);
                    View exitButton = AKACoverCameraModule.this.mQuickCirclePagerView.findViewById(C0088R.id.aka_back_button);
                    if (exitButton != null) {
                        exitButton.setBackgroundResource(C0088R.drawable.selector_quickwindow_back_btn);
                        exitButton.setContentDescription(AKACoverCameraModule.this.mGet.getActivity().getString(C0088R.string.camera_accessibility_back_button));
                    }
                }
            }
        }

        public void onPageScrolled(int page, float offset, int offsetPixels) {
        }

        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case 0:
                case 2:
                    AKACoverCameraModule.this.mIsPageChanging = false;
                    return;
                case 1:
                    AKACoverCameraModule.this.mIsPageChanging = true;
                    AKACoverCameraModule.this.showArrowQue(false);
                    if (AKACoverCameraModule.this.mUri == null) {
                        ImageButton exitBtn = (ImageButton) AKACoverCameraModule.this.mQuickCirclePagerView.findViewById(C0088R.id.aka_back_button);
                        ImageButton shutterBtn = (ImageButton) AKACoverCameraModule.this.mQuickCirclePagerView.findViewById(C0088R.id.aka_shutter_button);
                        if (exitBtn != null && shutterBtn != null) {
                            exitBtn.setVisibility(4);
                            shutterBtn.setVisibility(4);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$3 */
    class C00913 implements OnClickListener {
        C00913() {
        }

        public void onClick(View v) {
            if (AKACoverCameraModule.this.mViewPager != null) {
                if (AKACoverCameraModule.this.mViewPager.getCurrentItem() == 0) {
                    AKACoverCameraModule.this.finishQuickCircleCamera();
                } else {
                    AKACoverCameraModule.this.mViewPager.setCurrentItem(0);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$4 */
    class C00924 implements OnClickListener {
        C00924() {
        }

        public void onClick(View v) {
            if (AKACoverCameraModule.this.mViewPager != null && AKACoverCameraModule.this.mViewPager.getCurrentItem() == 0) {
                AKACoverCameraModule.this.onCameraShutterButtonClicked();
            }
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$5 */
    class C00935 implements OnLongClickListener {
        C00935() {
        }

        public boolean onLongClick(View v) {
            LdbUtil.setShutterType("Button");
            AKACoverCameraModule.this.onShutterBottomButtonLongClickListener();
            return true;
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$6 */
    class C00946 implements OnTouchListener {
        C00946() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 1) {
                if (AKACoverCameraModule.this.mViewPager.getCurrentItem() == 1) {
                    AKACoverCameraModule.this.mViewPager.setCurrentItem(0, true);
                } else if (AKACoverCameraModule.this.mIsSavingProgress || !AKACoverCameraModule.this.checkModuleValidate(31)) {
                    AKACoverCameraModule.this.mViewPager.setCurrentItem(0, true);
                    AKACoverCameraModule.this.showDialog(5);
                } else {
                    AKACoverCameraModule.this.mViewPager.setCurrentItem(1, true);
                    AKACoverCameraModule.this.removeToast();
                }
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$7 */
    class C00957 implements AnimationListener {
        C00957() {
        }

        public void onAnimationStart(Animation animation) {
            if (AKACoverCameraModule.this.mCoverManager.getQuickCoverRatio() == 2) {
                AKACoverCameraModule.this.mAnimView.setY(0.0f);
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (AKACoverCameraModule.this.checkModuleValidate(15) && AKACoverCameraModule.this.mQuickWindowView != null) {
                AKACoverCameraModule.this.mAnimView.setImageDrawable(null);
                AKACoverCameraModule.this.mQuickWindowView.setVisibility(8);
            }
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$8 */
    class C00968 implements Runnable {
        C00968() {
        }

        public void run() {
            AKACoverCameraModule.this.savePage();
        }
    }

    /* renamed from: com.lge.camera.app.AKACoverCameraModule$9 */
    class C00989 extends Thread {
        C00989() {
        }

        public void run() {
            AKACoverCameraModule.this.mSaveLastPictureDrawable = new BitmapDrawable(AKACoverCameraModule.this.mGet.getAppContext().getResources(), AKACoverCameraModule.this.loadCapturedImage(AKACoverCameraModule.this.getUri(), 90));
            AKACoverCameraModule.this.mGet.postOnUiThread(new HandlerRunnable(AKACoverCameraModule.this) {
                public void handleRun() {
                    if (AKACoverCameraModule.this.mPagerAdapter != null) {
                        AKACoverCameraModule.this.mPagerAdapter.addPicture(AKACoverCameraModule.this.mSaveLastPictureDrawable);
                    }
                    AKACoverCameraModule.this.mIsSavingProgress = false;
                    AKACoverCameraModule.this.showArrowQue(true);
                    AKACoverCameraModule.this.removeToast();
                }
            });
        }
    }

    public AKACoverCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void initUI(View baseParent) {
        this.mQuickCirclePagerView = (RelativeLayout) inflateView(C0088R.layout.aka_pager_view);
        if (this.mQuickCirclePagerView != null) {
            ViewGroup vg = (ViewGroup) findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                vg.addView(this.mQuickCirclePagerView, new LayoutParams(-1, -1));
            }
            judgePreviewRatio();
            getValFromFwk();
            RelativeLayout pagerView = (RelativeLayout) this.mQuickCirclePagerView.findViewById(C0088R.id.aka_camera_view);
            if (pagerView != null) {
                pagerView.setLayoutParams(setPostQuickCircleLayout((LayoutParams) pagerView.getLayoutParams()));
                this.mPagerAdapter = new QuickCirclePagerAdapter(this.mGet.getAppContext());
                this.mViewPager = (ViewPager) findViewById(C0088R.id.aka_pager_view);
                if (this.mViewPager != null && this.mPagerAdapter != null) {
                    this.mPagerAdapter.setValue(this.mCoverWidth, this.mCoverHeight, this.mCoverType, this.mCoverManager.getQuickCoverRatio());
                    this.mViewPager.setPaddingRelative(this.mPadding, 0, this.mPadding, 0);
                    this.mViewPager.setAdapter(this.mPagerAdapter);
                    this.mViewPager.setOffscreenPageLimit(1);
                    showArrowQue(false);
                    this.mAudioManger = (AudioManager) this.mGet.getAppContext().getSystemService("audio");
                    this.mToastMarginTop = (int) (((float) this.mCoverHeight) / 3.0f);
                    RelativeLayout buttonLayout = (RelativeLayout) this.mQuickCirclePagerView.findViewById(C0088R.id.aka_exit_view);
                    if (buttonLayout != null) {
                        LayoutParams buttonParam = (LayoutParams) buttonLayout.getLayoutParams();
                        buttonParam.height = this.mCoverHeight;
                        buttonLayout.setLayoutParams(buttonParam);
                        super.initUI(baseParent);
                    }
                }
            }
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        SystemBarUtil.disableStatusBarExpand(getAppContext(), true);
        judgePreviewRatio();
        showQuickCircleView(true);
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(0);
        }
        doCleanView(false, false, false);
        setFocusInVisible(true);
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (this.mViewPager != null) {
            this.mViewPager.setCurrentItem(0, true);
        }
        showQuickCircleView(false);
        this.mSaveLastPictureDrawable = null;
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(1);
        }
        this.mUri = null;
        this.mToast = null;
        this.mToastMsg = null;
        this.mSurfaceCapture = null;
        this.mAnimView = null;
        this.mQuickWindowView = null;
        showQuickCircleView(false);
        if (this.mPostviewManager != null && this.mPostviewManager.isPostviewShowing()) {
            this.mPostviewManager.releaseLayout();
        }
        doCleanView(false, false, false);
        setFocusInVisible(false);
        this.mGet.setReviewThumbBmp(null);
        SystemBarUtil.disableStatusBarExpand(getAppContext(), false);
    }

    public void onDestroy() {
        this.mAnimView = null;
        super.onDestroy();
    }

    protected void setViewPagerListener(boolean set) {
        if (this.mViewPager != null) {
            if (set) {
                this.mViewPager.setOnTouchListener(this.mViewPagerTouchListener);
                this.mViewPager.setOnPageChangeListener(this.mViewPagerChangeListener);
                return;
            }
            this.mViewPager.setOnTouchListener(null);
            this.mViewPager.setOnPageChangeListener(null);
        }
    }

    private void doTouchUpInViewPager(MotionEvent event) {
        if (isTouchIn(0)) {
            showArrowQue(true);
            setExitButtonListener(true);
            setQuickCircleShutterButtonListener(true);
            checkDragOnPreview();
            if (this.mIsDragOnPreview) {
                if (this.mIsDragForSwapCamera && this.mTouchMoveOnPreview) {
                    switchCamera();
                }
                this.mTouchMoveOnPreview = false;
            }
        } else if (isTouchIn(1)) {
            showArrowQue(true);
            if (!(this.mIsPageChanging || this.mViewPager == null)) {
                this.mViewPager.setCurrentItem(0, true);
            }
            if (this.mGestureDetector != null) {
                this.mGestureDetector.onTouchEvent(event);
            }
        }
    }

    protected boolean checkTouchOnPreview(int x) {
        int width = this.mCoverManager.getQuickCoverRatio() == 3 ? this.mQuickCircle_width : this.mQuickCircle_height;
        int[] lcd_size = Utils.getLCDsize(this.mGet.getAppContext(), false);
        int start = (int) (((float) (lcd_size[1] - width)) / 2.0f);
        int end = lcd_size[1] - start;
        CamLog.m7i(CameraConstants.TAG, "touch x:" + start + "x" + end);
        return start <= x && x <= end;
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
            CamLog.m7i(CameraConstants.TAG, "[AKA COVER] drag on QuickCircle Preview");
            this.mIsDragOnPreview = true;
            if (touch_x > 100 || touch_y > 100) {
                this.mIsDragForSwapCamera = true;
            }
        }
    }

    protected void setExitButtonListener(boolean enable) {
        if (this.mQuickCirclePagerView != null && this.mViewPager != null) {
            ImageButton exitBtn = (ImageButton) this.mQuickCirclePagerView.findViewById(C0088R.id.aka_back_button);
            if (exitBtn == null) {
                return;
            }
            if (enable) {
                exitBtn.setVisibility(0);
                exitBtn.setEnabled(true);
                exitBtn.setOnClickListener(new C00913());
                return;
            }
            exitBtn.setOnClickListener(null);
            exitBtn.setVisibility(8);
        }
    }

    protected void setQuickCircleShutterButtonListener(boolean enable) {
        if (this.mQuickCirclePagerView != null && this.mViewPager != null) {
            ImageButton shutterBtn = (ImageButton) this.mQuickCirclePagerView.findViewById(C0088R.id.aka_shutter_button);
            if (shutterBtn == null) {
                return;
            }
            if (enable) {
                shutterBtn.setVisibility(0);
                shutterBtn.setEnabled(true);
                shutterBtn.setAlpha(1.0f);
                shutterBtn.setOnClickListener(new C00924());
                shutterBtn.setOnLongClickListener(new C00935());
                return;
            }
            shutterBtn.setOnClickListener(null);
            shutterBtn.setVisibility(8);
        }
    }

    protected boolean isQuickCoverButtonLongPressed() {
        if (this.mQuickCirclePagerView != null) {
            ImageButton shutterBtn = (ImageButton) this.mQuickCirclePagerView.findViewById(C0088R.id.aka_shutter_button);
            if (shutterBtn != null) {
                return shutterBtn.isPressed();
            }
        }
        return false;
    }

    protected void showQuickCircleView(boolean enable) {
        setViewPagerListener(enable);
        if (this.mViewPager != null && this.mViewPager.getCurrentItem() != 1) {
            this.mViewPager.setVisibility(enable ? 0 : 8);
            setExitButtonListener(enable);
            setQuickCircleShutterButtonListener(enable);
        }
    }

    protected void showArrowQue(boolean set) {
        if (this.mQuickCirclePagerView != null && this.mViewPager != null) {
            RelativeLayout arrowCue = (RelativeLayout) this.mQuickCirclePagerView.findViewById(C0088R.id.aka_arrow_button_view);
            if (arrowCue == null) {
                return;
            }
            if (!set || this.mUri == null) {
                arrowCue.setVisibility(8);
                return;
            }
            LayoutParams lParam = (LayoutParams) arrowCue.getLayoutParams();
            ImageView arrImg = (ImageView) arrowCue.findViewById(C0088R.id.arrow_image_view);
            if (arrImg != null) {
                lParam.height = this.mQuickCircle_width;
                if (this.mViewPager.getCurrentItem() == 1) {
                    lParam.removeRule(21);
                    lParam.addRule(20);
                    arrImg.setImageResource(C0088R.drawable.quickcircle_icon_right_arrow_normal);
                } else {
                    lParam.removeRule(20);
                    lParam.addRule(21);
                    arrImg.setImageResource(C0088R.drawable.quickcircle_icon_left_arrow_normal);
                }
                arrowCue.setLayoutParams(lParam);
                arrowCue.setVisibility(0);
                arrImg.setVisibility(0);
                arrImg.setOnTouchListener(new C00946());
            }
        }
    }

    public int getCameraIdFromPref() {
        return super.getCameraIdFromPref();
    }

    protected Drawable getSurfaceBitmap() {
        Bitmap bitmap = null;
        TextureView textureView = this.mGet.getTextureView();
        if (textureView != null) {
            bitmap = textureView.getBitmap();
        } else {
            CamLog.m11w(CameraConstants.TAG, "TextureView was null, so bitmap would be null.");
        }
        if (bitmap == null) {
            return null;
        }
        return new BitmapDrawable(this.mGet.getAppContext().getResources(), bitmap);
    }

    public void prepareAnimation() {
        this.mQuickWindowView = findViewById(C0088R.id.quick_window_camera_view);
        if (this.mQuickWindowView != null && this.mViewPager != null && !this.mSnapShotChecker.checkMultiShotState(5)) {
            this.mQuickWindowView.setLayoutParams(setPostQuickCircleLayout((LayoutParams) this.mQuickWindowView.getLayoutParams()));
            this.mAnimView = (ImageView) this.mQuickWindowView.findViewById(C0088R.id.quick_window_anim_img);
            if (this.mAnimView == null) {
                return;
            }
            if (this.mSurfaceCapture != null) {
                this.mAnimView.setImageDrawable(this.mSurfaceCapture);
            } else {
                this.mAnimView.setImageDrawable(getSurfaceBitmap());
            }
        }
    }

    public void startAnimationToCapture() {
        if (checkModuleValidate(15) && this.mQuickWindowView != null && this.mViewPager != null && this.mAnimView != null && !this.mSnapShotChecker.checkMultiShotState(5)) {
            this.mQuickWindowView.setVisibility(0);
            this.mAnimView.setVisibility(0);
            AnimationSet quickAnimation = new AnimationSet(false);
            int captureMargin = ((int) this.mQuickWindowView.getY()) - this.mQuickCircle_marginTop;
            int marginStart = (int) this.mViewPager.getX();
            if (this.mCoverManager.getQuickCoverRatio() != 3) {
                marginStart = (int) this.mQuickWindowView.getX();
            }
            if (this.mCoverManager.getQuickCoverRatio() == 2) {
                captureMargin = 0;
            }
            quickAnimation.addAnimation(new TranslateAnimation(0.0f, (float) ((this.mQuickCircle_height + marginStart) + 100), (float) captureMargin, (float) captureMargin));
            quickAnimation.setDuration(300);
            quickAnimation.setAnimationListener(new C00957());
            this.mAnimView.startAnimation(quickAnimation);
        }
    }

    public void notifyNewMedia(Uri uri, boolean updateThumbnail) {
        this.mUri = uri;
        getHandler().postDelayed(new C00968(), 0);
        super.notifyNewMedia(uri, false);
    }

    public void savePage() {
        if (checkModuleValidate(15)) {
            this.mSaveImgThread = new C00989();
            if (this.mSaveImgThread != null && this.mSaveImgThread.getState() == State.NEW) {
                this.mSaveImgThread.start();
            }
        }
    }

    protected Bitmap loadCapturedImage(Uri uri, int degrees) {
        if (this.mGet.getActivity() == null || uri == null) {
            return null;
        }
        int degree = Exif.getOrientation(FileUtil.getRealPathFromURI(getActivity(), uri));
        if (isRearCamera()) {
            degree = 90;
        } else if (degree != 270) {
            degree = 90;
        }
        return BitmapManagingUtil.loadScaledandRotatedBitmap(this.mGet.getActivity().getContentResolver(), uri.toString(), this.mCurrentQuickCircleSize[0], this.mCurrentQuickCircleSize[1], degree);
    }

    protected void onShutterCallback(boolean sound, boolean animation, boolean recording) {
        super.onShutterCallback(true, false, false);
        this.mSurfaceCapture = getSurfaceBitmap();
        prepareAnimation();
        startAnimationToCapture();
    }

    protected void onTakePictureBefore() {
        this.mSnapShotChecker.setSnapShotState(2);
        setShutterEnable(false);
    }

    protected void onTakePictureAfter() {
        this.mSnapShotChecker.setPictureCallbackState(3);
        if (this.mSnapShotChecker.isIdle()) {
            setShutterEnable(true);
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setEnabled(false);
        }
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setAfterShot();
        }
    }

    protected void takePictureFailed() {
        setShutterEnable(true);
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        CamLog.m3d(CameraConstants.TAG, "[AKA COVER] ## onPictureTaken");
        releaseCaptureProgressOnPictureCallback();
        onTakePictureAfter();
        saveImage(data, extraExif);
        if (checkModuleValidate(15)) {
            if (checkModuleValidate(128)) {
                revertParameterByLGSF(null);
            }
            if ((FunctionProperties.isSupportedHDR(isRearCamera()) == 1 && "1".equals(getSettingValue("hdr-mode"))) || !FunctionProperties.isSupportedZSL(this.mCameraId)) {
                setupPreview(null);
            }
            if (this.mFocusManager != null && isFocusEnableCondition()) {
                this.mFocusManager.registerCallback(true);
            }
            if (this.mTempGetAudioFocus) {
                AudioUtil.setAudioFocus(getAppContext(), false);
                this.mTempGetAudioFocus = false;
            }
        }
    }

    private void setShutterEnable(boolean enable) {
        if (this.mQuickCirclePagerView != null) {
            ImageButton shutterBtn = (ImageButton) this.mQuickCirclePagerView.findViewById(C0088R.id.aka_shutter_button);
            if (shutterBtn != null) {
                if (enable) {
                    shutterBtn.setAlpha(1.0f);
                    shutterBtn.setEnabled(true);
                    return;
                }
                shutterBtn.setAlpha(0.4f);
                shutterBtn.setEnabled(false);
            }
        }
    }

    protected void getValFromFwk() {
        int top_margin = 0;
        this.mCoverType = QuickWindowUtils.getSmartCoverManager(getActivity()).getCoverType();
        int id_width = this.mGet.getActivity().getResources().getIdentifier("config_cover_window_width", "dimen", "com.lge.internal");
        int id_height = this.mGet.getActivity().getResources().getIdentifier("config_cover_window_height", "dimen", "com.lge.internal");
        int id_y_pos = this.mGet.getActivity().getResources().getIdentifier("config_cover_window_y_pos", "dimen", "com.lge.internal");
        if (id_width == 0 || id_height == 0 || id_y_pos == 0) {
            this.mCoverHeight = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_width);
        } else {
            this.mCoverWidth = this.mGet.getActivity().getResources().getDimensionPixelSize(id_width);
            this.mCoverHeight = this.mGet.getActivity().getResources().getDimensionPixelSize(id_height);
            top_margin = this.mGet.getActivity().getResources().getDimensionPixelSize(id_y_pos);
            CamLog.m7i(CameraConstants.TAG, "[AKA COVER] Cover size from defined value: " + this.mCoverWidth + "x" + this.mCoverHeight + " , top:" + top_margin);
            if (this.mCoverHeight == 0) {
                this.mCoverHeight = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_width);
            }
        }
        this.mQuickCircle_width = this.mCoverHeight;
        this.mQuickCircle_height = (int) (((float) this.mQuickCircle_width) / 1.3333334f);
        this.mQuickCircle_width_wide = (int) (((float) this.mQuickCircle_height) * 1.7777778f);
        this.mPadding = 0;
        switch (this.mCoverManager.getQuickCoverRatio()) {
            case 2:
                this.mQuickCircle_width_wide = this.mCoverHeight;
                this.mQuickCircle_height = (int) (((float) this.mQuickCircle_width_wide) / 1.7777778f);
                this.mQuickCircle_marginTop = (((this.mQuickCircle_width_wide - this.mQuickCircle_width) / 2) * -1) + top_margin;
                return;
            case 3:
                this.mPadding = 0;
                this.mQuickCircle_marginTop = top_margin;
                return;
            default:
                this.mQuickCircle_marginTop = top_margin;
                return;
        }
    }

    protected LayoutParams setPostQuickCircleLayout(LayoutParams layoutParams) {
        int height;
        int topMargin;
        int width;
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
        layoutParams = new LayoutParams(-1, height);
        layoutParams.topMargin = topMargin;
        layoutParams.addRule(14, 1);
        return layoutParams;
    }

    public synchronized void setBeautyEngineOn(boolean bIsOn) {
        CamLog.m3d(CameraConstants.TAG, "[AKA COVER] setBeautyEngine on :" + bIsOn);
        if (isRearCamera()) {
            super.setBeautyEngineOn(bIsOn);
            this.mBeautyManager.setBeautyPreviewOn(false);
        } else {
            super.setBeautyEngineOn(false);
        }
    }
}
