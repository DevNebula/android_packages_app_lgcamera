package com.lge.camera.managers.ext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.R;
import com.lge.camera.C0088R;
import com.lge.camera.app.ext.MultiViewFrame;
import com.lge.camera.app.ext.SpliceManagersInterface;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.components.TouchImageViewInterface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MemoryUtils;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;

public class SpliceViewImageImportManagerBase extends ManagerInterfaceImpl implements TouchImageViewInterface {
    protected static final float LOCAL_ANGLE_0 = 0.0f;
    protected static final float LOCAL_ANGLE_180 = 180.0f;
    protected static final float LOCAL_ANGLE_270 = 270.0f;
    protected static final float LOCAL_ANGLE_90 = 90.0f;
    protected static final int LOCAL_DEGREE_0 = 0;
    protected static final int LOCAL_DEGREE_180 = 2;
    protected static final int LOCAL_DEGREE_270 = 1;
    protected static final int LOCAL_DEGREE_90 = 3;
    protected static final int PRE_POSTVIEW_INDEX_BOTTOM = 1;
    protected static final int PRE_POSTVIEW_INDEX_TOP = 0;
    public static final int SEQ_SHOT_BOTTOM = 1;
    public static final int SEQ_SHOT_TOP = 0;
    public static final int SIMUL_SHOT = 2;
    protected static float sANIM_HALF_SIZE = 1440.0f;
    protected static int sBATTERY_INDICATOR_BOTTOM_MARGIN = 1490;
    protected static int sBATTERY_INDICATOR_TOP_MARGIN = 288;
    protected static int sHALF_SIZE = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    protected static int sIMPORT_BUTTON_TOP_MARGIN = 288;
    protected static int sIMPORT_BUTTON_TOP_MARGIN_REVERSE = 1490;
    protected static int sIMPORT_STATE_IMPORT_ALREADY = 1;
    protected static int sIMPORT_STATE_IMPORT_IDLE = 0;
    protected static int sIMPORT_STATE_IMPORT_LAYOUT_NONE = 2;
    protected SpliceViewCameraAngleListener mAngleListener = null;
    protected ImageView mAnimImageViewFirst = null;
    protected ImageView mAnimImageViewFirstBg = null;
    protected ImageView mAnimImageViewSecond = null;
    protected ImageView mAnimImageViewSecondBg = null;
    protected ImageView mAnimPreviewBgView;
    protected ImageView mAnimPreviewView;
    protected ImageView mAnimTargetBgView;
    protected ImageView mAnimTargetView;
    protected RelativeLayout mAnimView = null;
    protected int mCurrentImportState = -1;
    protected RelativeLayout mDoubleCamLayoutAll = null;
    protected RelativeLayout mDoubleCamLayoutBottom = null;
    protected RelativeLayout mDoubleCamLayoutTop = null;
    protected RotateImageButton mDualNormalBottom = null;
    protected RotateImageButton mDualNormalTop = null;
    protected RotateImageButton mDualWideBottom = null;
    protected RotateImageButton mDualWideTop = null;
    protected RotateTextView mGuideTextBottom = null;
    protected RotateTextView mGuideTextTop = null;
    HandlerRunnable mHideGuideCueRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            SpliceViewImageImportManagerBase.this.mSpliceGuideCueLayout.setVisibility(8);
        }
    };
    protected RotateImageButton mImportButton = null;
    protected RelativeLayout mImportButtonLayout = null;
    protected View mImportViewLayout = null;
    protected boolean mIsAnimationStarted = false;
    protected boolean mIsGalleryButtonClicked = false;
    protected boolean mIsImportedCompleted = false;
    protected boolean mIsReverse = false;
    protected SpliceViewListener mListener = null;
    protected Bitmap mPrePostviewBitmap = null;
    private AnimationListener mRotateAnimListener = new C13327();
    protected RotateImageButton mRotateButton = null;
    private int mRotationBeforeStartingGallery = 0;
    HandlerRunnable mShowGuideCueRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            SpliceViewImageImportManagerBase.this.mSpliceGuideCueLayout.setVisibility(0);
        }
    };
    protected RelativeLayout mSpliceGuideCueLayout = null;
    protected SpliceManagersInterface mSpliceInteface = null;
    protected RelativeLayout mSpliceLayout = null;
    protected TouchImageView mSplicePrePostImageFirst = null;
    protected ImageView mSplicePrePostImageFirstBg = null;
    protected TouchImageView mSplicePrePostImageSecond = null;
    protected ImageView mSplicePrePostImageSecondBg = null;
    protected RelativeLayout mSplicePrePostView = null;
    private AnimationListener mSwapAnimListener = new C13338();
    protected RelativeLayout mSwapBtnLayout = null;
    protected ImageView mSwapButton = null;
    protected TouchImageView mTargetView;

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$1 */
    class C13261 implements OnTouchListener {
        C13261() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (SpliceViewImageImportManagerBase.this.mIsAnimationStarted || !SpliceViewImageImportManagerBase.this.mIsImportedCompleted) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$2 */
    class C13272 implements OnTouchListener {
        C13272() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$3 */
    class C13283 implements OnTouchListener {
        C13283() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (SpliceViewImageImportManagerBase.this.mIsAnimationStarted || !SpliceViewImageImportManagerBase.this.mIsImportedCompleted) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$4 */
    class C13294 implements OnClickListener {
        C13294() {
        }

        public void onClick(View arg0) {
            if (!SpliceViewImageImportManagerBase.this.mIsAnimationStarted && SpliceViewImageImportManagerBase.this.mIsImportedCompleted) {
                SpliceViewImageImportManagerBase.this.showSwapAnimView();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$5 */
    class C13305 implements OnClickListener {
        C13305() {
        }

        public void onClick(View arg0) {
            if (!SpliceViewImageImportManagerBase.this.mIsGalleryButtonClicked) {
                SpliceViewImageImportManagerBase.this.mIsGalleryButtonClicked = true;
                SpliceViewImageImportManagerBase.this.startGallery();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$6 */
    class C13316 implements OnClickListener {
        C13316() {
        }

        public void onClick(View arg0) {
            if (!SpliceViewImageImportManagerBase.this.mIsAnimationStarted && SpliceViewImageImportManagerBase.this.mIsImportedCompleted && SpliceViewImageImportManagerBase.this.mListener != null) {
                SpliceViewImageImportManagerBase.this.showRotateAnimView();
                SpliceViewImageImportManagerBase.this.mListener.onRotate();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$7 */
    class C13327 implements AnimationListener {
        C13327() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            SpliceViewImageImportManagerBase.this.mAnimTargetView.setVisibility(4);
            SpliceViewImageImportManagerBase.this.mAnimTargetBgView.setVisibility(8);
            SpliceViewImageImportManagerBase.this.mAnimView.setVisibility(8);
            SpliceViewImageImportManagerBase.this.removeAnimImageView();
            SpliceViewImageImportManagerBase.this.mSwapBtnLayout.setVisibility(0);
            SpliceViewImageImportManagerBase.this.mIsAnimationStarted = false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManagerBase$8 */
    class C13338 implements AnimationListener {
        C13338() {
        }

        public void onAnimationStart(Animation animation) {
            SpliceViewImageImportManagerBase.this.mIsAnimationStarted = true;
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            SpliceViewImageImportManagerBase.this.mAnimTargetView.setVisibility(4);
            SpliceViewImageImportManagerBase.this.mAnimTargetBgView.setVisibility(8);
            SpliceViewImageImportManagerBase.this.mAnimPreviewBgView.setVisibility(8);
            SpliceViewImageImportManagerBase.this.mAnimPreviewView.setVisibility(4);
            SpliceViewImageImportManagerBase.this.mAnimView.setVisibility(8);
            SpliceViewImageImportManagerBase.this.removeAnimImageView();
            SpliceViewImageImportManagerBase.this.mSwapBtnLayout.setVisibility(0);
            SpliceViewImageImportManagerBase.this.mImportButtonLayout.setVisibility(0);
            SpliceViewImageImportManagerBase.this.mIsAnimationStarted = false;
        }
    }

    public SpliceViewImageImportManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setSpliceViewListener(SpliceViewListener listener) {
        this.mListener = listener;
    }

    public void setSpliceManagerInterface(SpliceManagersInterface listener) {
        this.mSpliceInteface = listener;
    }

    public void removeViews() {
        releaseListener();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null && this.mImportViewLayout != null && this.mSpliceLayout != null) {
            MemoryUtils.releaseViews(this.mSpliceLayout);
            vg.removeView(this.mImportViewLayout);
            this.mImportViewLayout = null;
        }
    }

    protected void releaseListener() {
        if (this.mImportButton != null && this.mRotateButton != null && this.mSwapButton != null && this.mSplicePrePostImageFirst != null && this.mSplicePrePostImageSecond != null && this.mDualNormalTop != null && this.mDualNormalBottom != null && this.mDualWideTop != null && this.mDualWideBottom != null) {
            this.mSwapButton.setOnTouchListener(null);
            this.mImportButton.setOnTouchListener(null);
            this.mRotateButton.setOnTouchListener(null);
            this.mDualNormalTop.setOnTouchListener(null);
            this.mDualWideTop.setOnTouchListener(null);
            this.mDualNormalBottom.setOnTouchListener(null);
            this.mDualWideBottom.setOnTouchListener(null);
            this.mSwapButton.setOnClickListener(null);
            this.mImportButton.setOnClickListener(null);
            this.mRotateButton.setOnClickListener(null);
            this.mDualNormalTop.setOnClickListener(null);
            this.mDualWideTop.setOnClickListener(null);
            this.mDualNormalBottom.setOnClickListener(null);
            this.mDualWideBottom.setOnClickListener(null);
            this.mSplicePrePostImageFirst.setTouchImageViewInterface(null);
            this.mSplicePrePostImageSecond.setTouchImageViewInterface(null);
        }
    }

    protected void setButtonListener() {
        this.mSwapButton.setOnTouchListener(new C13261());
        this.mImportButton.setOnTouchListener(new C13272());
        this.mRotateButton.setOnTouchListener(new C13283());
        this.mSwapButton.setOnClickListener(new C13294());
        this.mImportButton.setOnClickListener(new C13305());
        this.mRotateButton.setOnClickListener(new C13316());
    }

    private void addAnimImageView() {
        this.mAnimView = (RelativeLayout) this.mGet.inflateView(C0088R.layout.splice_anim_view);
        if (this.mSpliceLayout != null) {
            this.mSpliceLayout.addView(this.mAnimView);
        }
        this.mAnimImageViewFirstBg = (ImageView) this.mAnimView.findViewById(C0088R.id.splice_anim_dummy_first_bg);
        this.mAnimImageViewSecondBg = (ImageView) this.mAnimView.findViewById(C0088R.id.splice_anim_dummy_second_bg);
        this.mAnimImageViewFirst = (ImageView) this.mAnimView.findViewById(C0088R.id.splice_anim_dummy_first);
        this.mAnimImageViewSecond = (ImageView) this.mAnimView.findViewById(C0088R.id.splice_anim_dummy_second);
        setImageViewParam(this.mAnimImageViewFirst);
        setImageViewParam(this.mAnimImageViewSecond);
        setImageViewParam(this.mAnimImageViewFirstBg);
        setImageViewParam(this.mAnimImageViewSecondBg);
    }

    private void removeAnimImageView() {
        if (this.mSpliceLayout != null) {
            this.mSpliceLayout.removeView(this.mAnimView);
        }
    }

    public void showRotateAnimView() {
        this.mIsAnimationStarted = true;
        this.mSwapBtnLayout.setVisibility(8);
        addAnimImageView();
        this.mAnimView.setVisibility(0);
        if (this.mIsReverse) {
            this.mAnimImageViewFirst.setVisibility(4);
            this.mAnimImageViewFirstBg.setVisibility(4);
            this.mAnimTargetView = this.mAnimImageViewSecond;
            this.mAnimTargetBgView = this.mAnimImageViewSecondBg;
            this.mTargetView = this.mSplicePrePostImageSecond;
        } else {
            this.mAnimImageViewSecond.setVisibility(4);
            this.mAnimImageViewSecondBg.setVisibility(4);
            this.mAnimTargetView = this.mAnimImageViewFirst;
            this.mAnimTargetBgView = this.mAnimImageViewFirstBg;
            this.mTargetView = this.mSplicePrePostImageFirst;
        }
        if (this.mListener != null) {
            int curDegree = this.mListener.getCurrentDegreeForImportImg();
            this.mAnimTargetBgView.setVisibility(0);
            this.mAnimTargetView.setImageBitmap(getImageInView(this.mTargetView));
            this.mAnimTargetView.setVisibility(0);
            startRotateAnimation(this.mAnimTargetView, getDegreeForRotate(curDegree), getDegreeForRotate((curDegree + 1) % 4), this.mRotateAnimListener);
            this.mTargetView.setRotation(getDegree((curDegree + 1) % 4));
        }
    }

    public void showSwapAnimView() {
        int originY = 0;
        this.mSwapBtnLayout.setVisibility(8);
        this.mImportButtonLayout.setVisibility(8);
        addAnimImageView();
        this.mAnimView.setVisibility(0);
        if (this.mIsReverse) {
            originY = sHALF_SIZE;
            this.mAnimTargetView = this.mAnimImageViewSecond;
            this.mAnimTargetBgView = this.mAnimImageViewSecondBg;
            this.mAnimPreviewView = this.mAnimImageViewFirst;
            this.mAnimPreviewBgView = this.mAnimImageViewFirstBg;
            this.mTargetView = this.mSplicePrePostImageSecond;
        } else {
            this.mAnimTargetView = this.mAnimImageViewFirst;
            this.mAnimTargetBgView = this.mAnimImageViewFirstBg;
            this.mAnimPreviewView = this.mAnimImageViewSecond;
            this.mAnimPreviewBgView = this.mAnimImageViewSecondBg;
            this.mTargetView = this.mSplicePrePostImageFirst;
        }
        if (this.mListener != null) {
            this.mAnimTargetView.setRotation(getDegree(this.mListener.getCurrentDegreeForImportImg()));
        }
        this.mAnimTargetView.setImageBitmap(getImageInView(this.mTargetView));
        MultiViewFrame.getPreviewBitmap(0, originY, sHALF_SIZE, true);
    }

    public void doShowSwapAnim() {
        boolean z = false;
        this.mAnimTargetBgView.setVisibility(0);
        this.mAnimPreviewBgView.setVisibility(0);
        this.mAnimPreviewView.setImageBitmap(MultiViewFrame.sPreviewCapture[0]);
        startTransAnimation(this.mAnimTargetView, true, this.mSwapAnimListener, !this.mIsReverse);
        View view = this.mAnimPreviewView;
        if (this.mIsReverse) {
            z = true;
        }
        startTransAnimation(view, true, null, z);
        if (this.mListener != null) {
            this.mListener.onSwap();
        }
        changeImportButtonLayoutLocation();
    }

    public void startTransAnimation(View aniView, boolean show, AnimationListener listener, boolean isReverse) {
        if (aniView != null) {
            aniView.clearAnimation();
            aniView.setVisibility(show ? 0 : 4);
            if (isReverse) {
            }
            Animation transAni = new TranslateAnimation(0.0f, 0.0f, 0.0f, isReverse ? sANIM_HALF_SIZE : -sANIM_HALF_SIZE);
            AnimationSet aniSet = new AnimationSet(true);
            aniSet.addAnimation(transAni);
            aniSet.setDuration(350);
            aniSet.setAnimationListener(listener);
            aniSet.setInterpolator(new DecelerateInterpolator(1.5f));
            aniView.startAnimation(aniSet);
        }
    }

    private int getDegreeForRotate(int curDegree) {
        switch (curDegree) {
            case 1:
                return 270;
            case 2:
                return 180;
            case 3:
                return 90;
            default:
                return 0;
        }
    }

    private float getDegree(int degree) {
        switch (degree) {
            case 1:
                return 270.0f;
            case 2:
                return LOCAL_ANGLE_180;
            case 3:
                return 90.0f;
            default:
                return 0.0f;
        }
    }

    public void startRotateAnimation(View aniView, int startDegree, int targetDegree, AnimationListener listener) {
        aniView.clearAnimation();
        if (startDegree == 0) {
            targetDegree = -90;
        }
        Animation rotateAni = new RotateAnimation((float) startDegree, (float) targetDegree, 1, 0.5f, 1, 0.5f);
        AnimationSet aniSet = new AnimationSet(true);
        aniSet.addAnimation(rotateAni);
        aniSet.setDuration(350);
        aniSet.setInterpolator(new DecelerateInterpolator(1.5f));
        aniSet.setAnimationListener(listener);
        aniView.startAnimation(aniSet);
    }

    public void changeImportButtonLayoutLocation() {
        if (this.mImportButtonLayout == null) {
            CamLog.m5e(CameraConstants.TAG, "mImportButtonLayout is null");
            return;
        }
        LayoutParams param = (LayoutParams) this.mImportButtonLayout.getLayoutParams();
        if (this.mIsReverse) {
            param.topMargin = sIMPORT_BUTTON_TOP_MARGIN_REVERSE;
            this.mSplicePrePostImageSecond.setZoom(this.mSplicePrePostImageFirst, this.mSplicePrePostImageFirst.getCurrentZoom());
            this.mSplicePrePostImageSecond.setRotation(this.mSplicePrePostImageFirst.getRotation());
        } else {
            param.topMargin = sIMPORT_BUTTON_TOP_MARGIN;
            this.mSplicePrePostImageFirst.setZoom(this.mSplicePrePostImageSecond, this.mSplicePrePostImageSecond.getCurrentZoom());
            this.mSplicePrePostImageFirst.setRotation(this.mSplicePrePostImageSecond.getRotation());
        }
        this.mImportButtonLayout.setLayoutParams(param);
        setImageToPrePostView(true);
    }

    private void startGallery() {
        this.mRotationBeforeStartingGallery = this.mGet.getOrientationDegree();
        CamLog.m3d(CameraConstants.TAG, "mRotationBeforeStartingGallery : " + this.mRotationBeforeStartingGallery);
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("vnd.android.cursor.dir/image");
        this.mGet.getActivity().startActivityForResult(intent, 6);
    }

    public void showImportButton(boolean isShow) {
        if (this.mImportButton != null) {
            this.mImportButton.setVisibility(isShow ? 0 : 8);
        }
    }

    public void showSwapAndRotateButton(boolean isShow) {
        if (this.mSwapButton != null && this.mRotateButton != null) {
            int visibility = isShow ? 0 : 8;
            this.mSwapButton.setVisibility(visibility);
            this.mRotateButton.setVisibility(visibility);
        }
    }

    public void showImportLayout(boolean isShow) {
        if (this.mSpliceLayout != null) {
            this.mSpliceLayout.setVisibility(isShow ? 0 : 8);
        }
    }

    public void setImageToPrePostView(boolean isImage) {
        if (isImage && this.mPrePostviewBitmap != null && !this.mPrePostviewBitmap.isRecycled()) {
            if (this.mIsReverse) {
                setImageToPrePostView(this.mPrePostviewBitmap, this.mSplicePrePostImageSecond, this.mSplicePrePostImageSecondBg);
                hidePrePostView(this.mSplicePrePostImageFirst, this.mSplicePrePostImageFirstBg);
            } else {
                setImageToPrePostView(this.mPrePostviewBitmap, this.mSplicePrePostImageFirst, this.mSplicePrePostImageFirstBg);
                hidePrePostView(this.mSplicePrePostImageSecond, this.mSplicePrePostImageSecondBg);
            }
            this.mIsImportedCompleted = true;
        }
    }

    public void setImageToPrePostView(Bitmap bm, TouchImageView tiv, ImageView iv) {
        if (this.mSplicePrePostView != null) {
            this.mSplicePrePostView.setVisibility(0);
        }
        if (iv != null) {
            tiv.setImageBitmap(bm);
            tiv.setVisibility(0);
        }
        if (iv != null) {
            iv.setVisibility(0);
        }
    }

    public void hidePrePostView(TouchImageView tiv, ImageView iv) {
        if (tiv != null) {
            CamLog.m5e(CameraConstants.TAG, "null check");
            tiv.setImageBitmap(null);
            tiv.setVisibility(8);
        }
        if (iv != null) {
            iv.setVisibility(8);
        }
    }

    public Bitmap getBitmapInPrePostView(int index) {
        if (index == 0) {
            return getImageInView(this.mSplicePrePostImageFirst);
        }
        return getImageInView(this.mSplicePrePostImageSecond);
    }

    private Bitmap getImageInView(View view) {
        if (view == null) {
            return null;
        }
        if (view.getMeasuredWidth() == 0 || view.getMeasuredHeight() == 0) {
            CamLog.m3d(CameraConstants.TAG, "w or h is 0");
            return null;
        }
        Bitmap bm = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Config.ARGB_8888);
        view.draw(new Canvas(bm));
        return bm;
    }

    protected boolean isSystemBarVisible() {
        return SystemBarUtil.isSystemUIVisible(getActivity());
    }

    public boolean isSystemUIVisible() {
        return isSystemBarVisible();
    }

    public void onClicked() {
    }

    public void onTouchStateChanged(boolean isTouchDown) {
    }

    public void onZoomScaleStart() {
    }

    public void processForTimerShot(boolean isTakePicBefore) {
        if (isTakePicBefore) {
            showImportButton(false);
            showSwapAndRotateButton(false);
        } else if (this.mCurrentImportState == sIMPORT_STATE_IMPORT_LAYOUT_NONE) {
        } else {
            if (this.mCurrentImportState == sIMPORT_STATE_IMPORT_IDLE) {
                showImportButton(true);
            } else {
                showSwapAndRotateButton(true);
            }
        }
    }

    protected void setRelativeLayoutParam(RelativeLayout rl) {
        LayoutParams param = (LayoutParams) rl.getLayoutParams();
        param.width = sHALF_SIZE;
        param.height = sHALF_SIZE;
        rl.setLayoutParams(param);
    }

    protected void resizeViewSize() {
        int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
        int height = lcdSize[1] * 2;
        sHALF_SIZE = lcdSize[1];
        sANIM_HALF_SIZE = ((float) height) / 2.0f;
        sIMPORT_BUTTON_TOP_MARGIN = height / 18;
        sIMPORT_BUTTON_TOP_MARGIN_REVERSE = sHALF_SIZE + (sIMPORT_BUTTON_TOP_MARGIN / 5);
        setRelativeLayoutParam(this.mDoubleCamLayoutTop);
        setRelativeLayoutParam(this.mDoubleCamLayoutBottom);
        setImageViewParam(this.mSplicePrePostImageFirstBg);
        setImageViewParam(this.mSplicePrePostImageSecondBg);
        setImageViewParam(this.mSplicePrePostImageFirst);
        setImageViewParam(this.mSplicePrePostImageSecond);
        String msg = getActivity().getString(C0088R.string.sp_image_SHORT);
        this.mSplicePrePostImageFirst.setContentDescription(msg);
        this.mSplicePrePostImageSecond.setContentDescription(msg);
    }

    protected void setImageViewParam(ImageView iv) {
        LayoutParams param = (LayoutParams) iv.getLayoutParams();
        param.width = sHALF_SIZE;
        param.height = sHALF_SIZE;
        iv.setLayoutParams(param);
    }

    public void setupCueLayout() {
        int i;
        this.mSpliceGuideCueLayout.setBackgroundResource(this.mSpliceInteface.isFrameShot() ? C0088R.drawable.ic_matchshot_wquential_01 : C0088R.drawable.ic_matchshot_same_time);
        int size = this.mGet.getActivity().getResources().getDimensionPixelSize(R.dimen.type_b01_dp);
        String num = "";
        this.mGuideTextTop.setText(String.format("%d", new Object[]{Integer.valueOf(1)}));
        this.mGuideTextTop.setTextSize(size);
        this.mGuideTextTop.setTextColor(getAppContext().getColor(C0088R.color.camera_black_txt));
        String str = "%d";
        Object[] objArr = new Object[1];
        if (this.mSpliceInteface.isFrameShot()) {
            i = 2;
        } else {
            i = 1;
        }
        objArr[0] = Integer.valueOf(i);
        this.mGuideTextBottom.setText(String.format(str, objArr));
        this.mGuideTextBottom.setTextSize(size);
        if (this.mSpliceInteface.isFrameShot()) {
            this.mGuideTextBottom.setTextColor(getAppContext().getColor(C0088R.color.camera_white_txt));
        } else {
            this.mGuideTextBottom.setTextColor(getAppContext().getColor(C0088R.color.camera_black_txt));
        }
        this.mGet.removePostRunnable(this.mShowGuideCueRunnable);
        this.mGet.removePostRunnable(this.mHideGuideCueRunnable);
        this.mGet.runOnUiThread(this.mShowGuideCueRunnable);
        this.mGet.postOnUiThread(this.mHideGuideCueRunnable, 1000);
    }

    public int getRotationBeforeStartingGallery() {
        CamLog.m3d(CameraConstants.TAG, "mRotationBeforeStartingGallery : " + this.mRotationBeforeStartingGallery);
        return this.mRotationBeforeStartingGallery;
    }
}
