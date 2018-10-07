package com.lge.camera.managers;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.managers.ReviewThumbnailManagerBase.onQuickViewListener;
import com.lge.camera.managers.ReviewThumbnailManagerBase.onSelfieQuickViewListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.QuickClipSharedItem;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;
import java.util.ArrayList;

public class GestureViewManager extends ReviewThumbnailManager {
    private int mBurstCount = 0;
    private boolean mIsCompleteSaving = false;
    protected boolean mIsGetPictureCallback = true;
    private boolean mIsShowQuickClip = true;
    protected String mLastBurstFileName = null;
    private Thread mQuickViewUpdateThread = null;

    /* renamed from: com.lge.camera.managers.GestureViewManager$1 */
    class C09461 implements OnTouchListener {
        C09461() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.GestureViewManager$5 */
    class C09515 implements OnTouchListener {
        C09515() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (GestureViewManager.this.mGestureDetector != null) {
                GestureViewManager.this.mGestureDetector.onTouchEvent(event);
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.GestureViewManager$6 */
    class C09526 implements OnClickListener {
        C09526() {
        }

        public void onClick(View v) {
            if (GestureViewManager.this.mSelfieQuickViewListener != null) {
                GestureViewManager.this.mSelfieQuickViewListener.onDoMotionEngine(1);
            }
            GestureViewManager.this.mGet.showDialog(2);
        }
    }

    /* renamed from: com.lge.camera.managers.GestureViewManager$7 */
    class C09537 implements OnClickListener {
        C09537() {
        }

        public void onClick(View v) {
            if (GestureViewManager.this.mSelfieQuickViewListener != null) {
                GestureViewManager.this.mSelfieQuickViewListener.onDoMotionEngine(1);
            }
            if (GestureViewManager.this.mIsShowQuickClip) {
                GestureViewManager.this.launchQuickClipSharedApp(false);
            }
        }
    }

    public GestureViewManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void showGalleryQuickViewAnimation(boolean open, boolean deleteImage, boolean isAutoReview) {
        CamLog.m3d(CameraConstants.TAG, "showGalleryQuickViewAnimation start (" + open + "," + deleteImage + "," + isAutoReview + ")");
        if (this.mGet.checkModuleValidate(15) && this.mBaseView != null && this.mThumbBtn != null && this.mThumbBtnDragShadow != null && this.mReviewLayout != null && this.mReviewImageLayout != null && this.mDeleteBtn != null) {
            this.mBackCover.setOnTouchListener(new C09461());
            this.mReviewImageLayout.clearAnimation();
            readyAnimation(open, deleteImage, isAutoReview);
            CamLog.m3d(CameraConstants.TAG, "showGalleryQuickViewAnimation end");
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        setActivatedSelfieQuickView(false);
        this.mGalleryWindowAniState = 0;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mSelfieQuickViewListener = null;
    }

    private void readyAnimation(boolean open, boolean deleteImage, boolean isAutoReview) {
        int[] startPos = new int[2];
        int[] targetPos = new int[2];
        int[] adjustPos = new int[2];
        int[] trashPos = new int[2];
        this.mThumbBtn.getLocationOnScreen(startPos);
        this.mReviewLayout.getLocationOnScreen(targetPos);
        this.mReviewImageLayout.getLocationOnScreen(adjustPos);
        this.mDeleteBtn.getLocationOnScreen(trashPos);
        float scaleX = ((float) this.mThumbBtn.getMeasuredWidth()) / ((float) this.mReviewImageLayout.getMeasuredWidth());
        float scaleY = ((float) this.mThumbBtn.getMeasuredHeight()) / ((float) this.mReviewImageLayout.getMeasuredHeight());
        float srcX = (float) (startPos[0] + (targetPos[0] - adjustPos[0]));
        float srcY = (float) (startPos[1] + (targetPos[1] - adjustPos[1]));
        float destX = (float) targetPos[0];
        float destY = (float) targetPos[1];
        if ((Float.isInfinite(scaleX) || Float.isInfinite(scaleY)) && open && !deleteImage) {
            DisplayMetrics outMetrics = Utils.getWindowRealMatics(this.mGet.getAppContext());
            int lcdWidth = outMetrics.widthPixels;
            int lcdHeight = outMetrics.heightPixels;
            if (!Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                lcdWidth = outMetrics.heightPixels;
                lcdHeight = outMetrics.widthPixels;
            }
            scaleX = ((float) this.mThumbBtn.getMeasuredHeight()) / ((float) lcdHeight);
            scaleY = ((float) this.mThumbBtn.getMeasuredHeight()) / ((float) lcdWidth);
        } else if (!open && deleteImage) {
            scaleX = 0.0f;
            scaleY = 0.0f;
            srcX = (float) ((trashPos[0] + (targetPos[0] - adjustPos[0])) + (this.mDeleteBtn.getMeasuredWidth() / 2));
            srcY = (float) ((trashPos[1] + (targetPos[1] - adjustPos[1])) + (this.mDeleteBtn.getMeasuredHeight() / 2));
        }
        setAnimationSet(scaleX, scaleY, srcX, srcY, destX, destY, open, deleteImage, isAutoReview);
    }

    private void setAnimationSet(float scaleX, float scaleY, float srcX, float srcY, float destX, float destY, boolean open, boolean deleteImage, boolean isAutoReview) {
        ScaleAnimation sa;
        TranslateAnimation ta;
        AlphaAnimation aa;
        if (open) {
            if (this.mGalleryWindowAniState != 1) {
                hideQuickView(true);
                return;
            }
            sa = new ScaleAnimation(scaleX, 1.0f, scaleY, 1.0f);
            ta = new TranslateAnimation(srcX, destX, srcY, destY);
            aa = new AlphaAnimation(0.0f, 1.0f);
            this.mReviewLayout.setVisibility(0);
            this.mReviewImageLayout.setVisibility(0);
        } else if (this.mGalleryWindowAniState == 1 || this.mGalleryWindowAniState == 2) {
            hideQuickView(true);
            return;
        } else {
            sa = new ScaleAnimation(1.0f, scaleX, 1.0f, scaleY);
            ta = new TranslateAnimation(destX, srcX, destY, srcY);
            aa = new AlphaAnimation(1.0f, 0.0f);
            this.mThumbBtn.setPressed(false);
            this.mThumbBtnDragShadow.setVisibility(8);
        }
        AnimationSet aniSet = new AnimationSet(true);
        setAnimationSetListener(aniSet, open, deleteImage, isAutoReview);
        aniSet.addAnimation(sa);
        aniSet.addAnimation(ta);
        aniSet.addAnimation(aa);
        aniSet.setDuration(300);
        aniSet.setInterpolator(new DecelerateInterpolator(1.5f));
        this.mReviewImageLayout.startAnimation(aniSet);
        AnimationUtil.startShowingAnimation(this.mBackCover, open, 300, null);
    }

    private void setAnimationSetListener(AnimationSet aniSet, final boolean open, final boolean deleteImage, final boolean isAutoReview) {
        aniSet.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                if (!open) {
                    CamLog.m3d(CameraConstants.TAG, "[QuickView] onAnimationStart. set mGalleryWindowAniState to ANI_CLOSE_STARTED");
                    GestureViewManager.this.mGalleryWindowAniState = 2;
                    if (GestureViewManager.this.mIsShowQuickClip) {
                        AnimationUtil.startShowingAnimation(GestureViewManager.this.mQuickClipView, false, 200, null);
                    }
                    AnimationUtil.startShowingAnimation(GestureViewManager.this.mDeleteBtnView, false, 200, null);
                } else if (GestureViewManager.this.mIsActivatedSelfieQuickView) {
                    GestureViewManager.this.mSelfieQuickViewListener.onAnimationStartForGestureView(open);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                GestureViewManager.this.mGet.runOnUiThread(new HandlerRunnable(GestureViewManager.this) {
                    public void handleRun() {
                        if (GestureViewManager.this.mReviewImageLayout != null && GestureViewManager.this.mBackCover != null && GestureViewManager.this.mDeleteBtn != null && GestureViewManager.this.mDeleteBtnView != null && GestureViewManager.this.mQuickClipView != null && GestureViewManager.this.mThumbBtnDragShadow != null) {
                            if (open) {
                                GestureViewManager.this.doOpenAnimationEnd(isAutoReview);
                            } else {
                                GestureViewManager.this.doCloseAnimationEnd(deleteImage);
                            }
                        }
                    }
                });
            }
        });
    }

    private void doOpenAnimationEnd(boolean isAutoReview) {
        if (isActivatedSelfieQuickView()) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (GestureViewManager.this.mSelfieQuickViewListener != null) {
                        GestureViewManager.this.mSelfieQuickViewListener.onDoMotionEngine(3);
                    }
                }
            }, 500);
        }
        if (this.mGalleryWindowAniState != 0) {
            CamLog.m3d(CameraConstants.TAG, "[QuickView] set mGalleryWindowAniState to ANI_FINISHED");
            this.mGalleryWindowAniState = 3;
            if (!(isAutoReview || this.mThumbBtnDragShadow.getVisibility() == 0)) {
                this.mThumbBtnDragShadow.setVisibility(0);
            }
            AnimationUtil.startShowingAnimation(this.mDeleteBtnView, true, 200, null);
            if (this.mIsShowQuickClip) {
                AnimationUtil.startShowingAnimation(this.mQuickClipView, true, 200, null);
            }
        }
    }

    private void doCloseAnimationEnd(boolean deleteImage) {
        hideQuickView(false);
        updateSnapShotThumbnail(false);
        if (deleteImage) {
            this.mDeleteBtn.setPressed(false);
            deleteImageAndUpdateThumbnail();
        }
        if (this.mReviewLayout != null) {
            this.mReviewLayout.setVisibility(8);
        }
    }

    public void makeQuickViewFastBmp(byte[] data, boolean isUseThumbnail, boolean isUseFlip, int burstCount) {
        this.mReviewQuickBmp = null;
        this.mBurstCount = burstCount;
        if (data == null) {
            CamLog.m3d(CameraConstants.TAG, "jpeg data from picture callback is null!!");
        } else if (this.mBurstCount <= 0 || !this.mIsCompleteSaving) {
            ExifInterface exif = Exif.readExif(data);
            if (exif != null) {
                int exifDegree = Exif.getOrientation(exif);
                Bitmap thumbBmp = exif.getThumbnailBitmap();
                if (thumbBmp == null) {
                    CamLog.m3d(CameraConstants.TAG, "exif.getThumbnailBitmap is null!!!");
                    return;
                }
                if (isUseFlip) {
                    byte[] exifThumbnail = exif.getThumbnail();
                    if (exifThumbnail != null) {
                        thumbBmp = BitmapManagingUtil.makeFlipBitmap(exifThumbnail, true, exifDegree);
                    } else {
                        CamLog.m3d(CameraConstants.TAG, "exif.getThumbnail[jpeg] is null!!!");
                        return;
                    }
                }
                if (thumbBmp != null) {
                    this.mReviewQuickBmp = BitmapManagingUtil.getRotatedImage(thumbBmp, exifDegree, false);
                }
                updateFastImageforQuickView();
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "Do not need to make fast-bmp for quickview because completed saving of burst images!!");
        }
    }

    private void updateFastImageforQuickView() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (GestureViewManager.this.mReviewQuickBmp != null && GestureViewManager.this.mReviewImageLayout != null && GestureViewManager.this.mReviewImageView != null && GestureViewManager.this.mFrontCoverView != null) {
                    GestureViewManager.this.mFrontCoverView.setVisibility(0);
                    if (!GestureViewManager.this.mReviewQuickBmp.isRecycled()) {
                        GestureViewManager.this.mReviewImageView.setImageBitmap(GestureViewManager.this.mReviewQuickBmp);
                    }
                    CamLog.m3d(CameraConstants.TAG, "Fast thumbnail set to Quickview !!" + GestureViewManager.this.isActivatedSelfieQuickView() + "," + GestureViewManager.this.mReviewImageLayout.getVisibility() + "," + GestureViewManager.this.mIsCompleteSaving);
                    if (GestureViewManager.this.isActivatedSelfieQuickView() && GestureViewManager.this.mReviewImageLayout.getVisibility() == 4 && !GestureViewManager.this.mIsCompleteSaving) {
                        GestureViewManager.this.mGestureViewProgress = 1;
                        GestureViewManager.this.setQuickViewLayout(false, GestureViewManager.this.mReviewQuickBmp, null);
                    }
                }
            }
        });
    }

    public void readyforQuickview() {
        setIsCompleteSaving(false);
        this.mReviewQuickBmp = null;
        if (this.mReviewImageView != null) {
            this.mReviewImageView.setImageBitmap(null);
        }
        if (this.mReviewImageLayout != null) {
            this.mReviewImageLayout.setVisibility(4);
        }
        this.mIsGetPictureCallback = false;
        this.mGestureViewProgress = 0;
        this.mDegree = -1;
    }

    public void setIsCompleteSaving(boolean set) {
        this.mIsCompleteSaving = set;
    }

    public void setSelfieQuickViewListener(onSelfieQuickViewListener listener) {
        this.mSelfieQuickViewListener = listener;
    }

    public void setQuickViewListener(onQuickViewListener listener) {
        this.mQuickViewListener = listener;
    }

    public void setIsGetPictureCallback(boolean set) {
        this.mIsGetPictureCallback = set;
    }

    protected void doAfterCaptureForQuickView(Uri uri) {
        boolean z = true;
        String fullName = FileUtil.getRealPathFromURI(this.mGet.getActivity(), uri);
        if (this.mIsGetPictureCallback) {
            setIsCompleteSaving(true);
            if (Utils.isBurstshotFile(fullName)) {
                if (fullName.equals(this.mLastBurstFileName) && this.mIsActivatedSelfieQuickView) {
                    if (this.mGestureViewProgress == 1) {
                        z = false;
                    }
                    updateQuickViewImage(z, false);
                }
            } else if (this.mIsActivatedSelfieQuickView) {
                if (this.mGestureViewProgress == 1) {
                    z = false;
                }
                updateQuickViewImage(z, false);
            }
            super.doAfterCaptureForQuickView(uri);
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if ((!isActivatedSelfieQuickView() || this.mGalleryWindowAniState <= 1) && !isQuickViewAniStarted()) {
            super.setRotateDegree(degree, animation);
        }
    }

    public int getQuickviewAniState() {
        return this.mGalleryWindowAniState;
    }

    public void showQuickView(boolean isAutoReview) {
        CamLog.m3d(CameraConstants.TAG, "showQuickView start");
        if ((!SecureImageUtil.useSecureLockImage() || !SecureImageUtil.get().isSecureLockUriListEmpty()) && this.mThumbBtn != null && this.mDeleteBtnView != null && this.mQuickClipView != null) {
            resetQuickViewDrawableState(false);
            updateThumbnailDragShadowPostion(this.mThumbBtn.getWidth() / 2, this.mThumbBtn.getHeight() / 2);
            boolean isLandscape = Utils.isConfigureLandscape(getAppContext().getResources());
            int size = Utils.getPx(getAppContext(), C0088R.dimen.review_thumbnail.size);
            layoutDeleteButton(isLandscape, size);
            layoutQuickClipButton(isLandscape, size);
            this.mFrontCoverView.setVisibility(4);
            setRecentImage(isAutoReview);
            this.mDegree = -1;
            CamLog.m3d(CameraConstants.TAG, "showQuickView end");
        }
    }

    private void layoutDeleteButton(boolean isLandscape, int size) {
        boolean isCinemaSize;
        if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode()) && ManualUtil.isCinemaSize(getAppContext(), this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE))) {
            isCinemaSize = true;
        } else {
            isCinemaSize = false;
        }
        int marginBottom = RatioCalcUtil.getCommandBottomMargin(getAppContext());
        int marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.quick_view_delete_btn.marginEnd);
        if (ModelProperties.isTablet(getAppContext())) {
            marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.07f) + Utils.getPx(getAppContext(), C0088R.dimen.quick_view_delete_btn.marginEnd_tablet);
        } else if (isCinemaSize) {
            marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.quick_view_manual_delete_btn.marginEnd);
        }
        LayoutParams deleteBtnViewlp = (LayoutParams) this.mDeleteBtnView.getLayoutParams();
        LayoutParams deleteBtnParams = (LayoutParams) this.mDeleteBtn.getLayoutParams();
        deleteBtnParams.width = size;
        deleteBtnParams.height = size;
        this.mDeleteBtn.setLayoutParams(deleteBtnParams);
        if (isLandscape) {
            deleteBtnViewlp.addRule(10);
            deleteBtnViewlp.topMargin = marginEnd;
            deleteBtnViewlp.setMarginEnd(marginBottom);
        } else {
            deleteBtnViewlp.addRule(12);
            deleteBtnViewlp.bottomMargin = marginBottom;
            deleteBtnViewlp.setMarginEnd(marginEnd);
        }
        this.mDeleteBtn.setEnabled(false);
        this.mDeleteBtnView.setLayoutParams(deleteBtnViewlp);
    }

    private void layoutQuickClipButton(boolean isLandscape, int size) {
        boolean isCinemaSize = true;
        ArrayList<QuickClipSharedItem> item = QuickClipUtil.getPreferSharedList(getUri(), this.mGet.getAppContext(), 1);
        if (item == null || item.size() < 1 || item.get(0) == null) {
            this.mQuickClipView.setVisibility(8);
            this.mIsShowQuickClip = false;
            return;
        }
        this.mIsShowQuickClip = true;
        if (!(CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode()) && ManualUtil.isCinemaSize(getAppContext(), this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE)))) {
            isCinemaSize = false;
        }
        this.mQuickClipSharedItem = (QuickClipSharedItem) item.get(0);
        int marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.00694f);
        int marginThumbnailBtn = RatioCalcUtil.getCommandBottomMargin(getAppContext());
        if (isCinemaSize) {
            if (ModelProperties.isLongLCDModel()) {
                marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.167f);
            } else {
                marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.manual_extra_button.marginTop);
            }
        }
        LayoutParams quickClipBtnViewlp = (LayoutParams) this.mQuickClipView.getLayoutParams();
        quickClipBtnViewlp.width = size;
        quickClipBtnViewlp.height = size;
        this.mQuickClipBtn.setLayoutParams((LayoutParams) this.mQuickClipBtn.getLayoutParams());
        this.mQuickClipBtn.setImageDrawable(this.mQuickClipSharedItem.getAppIcon());
        this.mQuickClipBtn.setContentDescription(this.mQuickClipSharedItem.getLabel());
        if (isLandscape) {
            quickClipBtnViewlp.addRule(10);
            quickClipBtnViewlp.topMargin = marginEnd;
            quickClipBtnViewlp.setMarginEnd(marginThumbnailBtn);
        } else {
            quickClipBtnViewlp.addRule(12);
            quickClipBtnViewlp.bottomMargin = marginThumbnailBtn;
            quickClipBtnViewlp.setMarginEnd(marginEnd);
        }
        this.mQuickClipView.setEnabled(false);
        this.mQuickClipView.setLayoutParams(quickClipBtnViewlp);
        this.mQuickClipView.setVisibility(4);
    }

    public void hideQuickView(boolean hideReviewLayout) {
        CamLog.m3d(CameraConstants.TAG, "hideQuickView start");
        if (this.mReviewImageLayout == null || this.mReviewImageView == null || this.mQuickClipBtn == null || this.mThumbBtnDragShadow == null || this.mReviewLayout == null) {
            CamLog.m3d(CameraConstants.TAG, "exit hideQuickView");
            return;
        }
        this.mGestureViewProgress = 2;
        ViewUtil.clearImageViewDrawableOnly(this.mReviewImageView);
        this.mDeleteBtnView.clearAnimation();
        this.mDeleteBtnView.setVisibility(4);
        this.mThumbBtnDragShadow.setImageBitmap(null);
        this.mThumbBtnDragShadow.setVisibility(8);
        this.mReviewImageLayout.setVisibility(4);
        this.mBackCover.setVisibility(8);
        if (hideReviewLayout) {
            this.mReviewLayout.setVisibility(8);
        }
        this.mGalleryWindowAniState = 0;
        this.mIsGetPictureCallback = true;
        if (this.mIsActivatedSelfieQuickView) {
            this.mFrontCoverView.setVisibility(4);
            this.mDeleteBtn.setOnClickListener(null);
            this.mQuickClipView.setOnClickListener(null);
            this.mGestureDetector = null;
        }
        setActivatedSelfieQuickView(false);
        this.mQuickViewListener.onCompleteHideQuickview();
        CamLog.m3d(CameraConstants.TAG, "hideQuickView end");
    }

    public void showSelfieQuickView() {
        CamLog.m3d(CameraConstants.TAG, "showSelfieQuickView start mIsCompleteSaving : " + this.mIsCompleteSaving);
        if (this.mThumbBtn != null && this.mDeleteBtnView != null && this.mFrontCoverView != null && this.mQuickClipBtn != null) {
            resetQuickViewDrawableState(false);
            boolean isLandscape = Utils.isConfigureLandscape(getAppContext().getResources());
            int size = Utils.getPx(getAppContext(), C0088R.dimen.review_thumbnail.size);
            layoutDeleteButton(isLandscape, size);
            layoutQuickClipButton(isLandscape, size);
            this.mGestureDetector = new GestureDetector(this.mGet.getAppContext(), new QuickViewGestureDetector());
            setSelfieQuickViewListener();
            if (this.mIsCompleteSaving) {
                this.mGestureViewProgress = 1;
                updateQuickViewImage(true, false);
            } else {
                this.mDeleteBtn.setEnabled(false);
                this.mFrontCoverView.setVisibility(0);
                if (this.mReviewQuickBmp != null) {
                    this.mGestureViewProgress = 1;
                    setQuickViewLayout(false, this.mReviewQuickBmp, null);
                }
            }
            LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_GESTURE_VIEW);
            CamLog.m3d(CameraConstants.TAG, "showSelfieQuickView end");
        }
    }

    private void setSelfieQuickViewListener() {
        if (this.mReviewImageView != null && this.mDeleteBtnView != null && this.mQuickClipView != null) {
            this.mReviewImageView.setOnTouchListener(new C09515());
            this.mDeleteBtn.setOnClickListener(new C09526());
            this.mQuickClipView.setOnClickListener(new C09537());
        }
    }

    private void updateQuickViewImage(boolean useAnimation, boolean isShowFastImage) {
        CamLog.m3d(CameraConstants.TAG, "updateQuickViewImage  useAnimation : " + useAnimation);
        if (isShowFastImage) {
            this.mDeleteBtn.setEnabled(false);
            this.mQuickClipView.setEnabled(false);
            this.mFrontCoverView.setVisibility(0);
            if (this.mReviewQuickBmp != null) {
                setQuickViewLayout(false, this.mReviewQuickBmp, null);
            }
            useAnimation = false;
        }
        final boolean animation = useAnimation;
        this.mQuickViewUpdateThread = new Thread(new Runnable() {
            public void run() {
                Uri uri;
                synchronized (GestureViewManager.this.mUriLock) {
                    uri = GestureViewManager.this.getUri();
                }
                if (GestureViewManager.this.mGet.checkModuleValidate(1) && uri != null) {
                    String filePath = FileUtil.getRealPathFromURI(GestureViewManager.this.mGet.getActivity(), uri);
                    String mediaType = GestureViewManager.this.mGet.getAppContext().getContentResolver().getType(uri);
                    boolean isImageType = mediaType != null && mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE);
                    int orientationDegree = Utils.isConfigureLandscape(GestureViewManager.this.mGet.getAppContext().getResources()) ? 90 : 0;
                    ExifInterface exif = Exif.readExif(filePath);
                    int exifDegree = isImageType ? Exif.getOrientation(exif) : 0;
                    if (isImageType) {
                        int[] actualSize = Exif.getExifSize(exif);
                        if (actualSize[0] == 0 || actualSize[1] == 0) {
                            actualSize = Exif.getImageSize(exif);
                        }
                        int[] dstSize = BitmapManagingUtil.getFitSizeOfBitmapForLCD(GestureViewManager.this.getActivity(), actualSize[0], actualSize[1], orientationDegree);
                        Bitmap toSetBmp = BitmapManagingUtil.loadScaledandRotatedBitmap(GestureViewManager.this.mGet.getAppContext().getContentResolver(), uri.toString(), dstSize[0], dstSize[1], exifDegree);
                        if (GestureViewManager.this.mGet.checkModuleValidate(1) && !Thread.interrupted()) {
                            GestureViewManager.this.updateImageView(toSetBmp, animation);
                        }
                    }
                }
            }
        });
        this.mQuickViewUpdateThread.start();
        this.mReviewQuickBmp = null;
    }

    private void updateImageView(final Bitmap setBmp, final boolean useAnimation) {
        CamLog.m3d(CameraConstants.TAG, "updateImageView  useAnimation " + useAnimation);
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (GestureViewManager.this.mGet.checkModuleValidate(1) && GestureViewManager.this.mReviewImageView != null && setBmp != null && !Thread.interrupted()) {
                    ViewUtil.clearImageViewDrawableOnly(GestureViewManager.this.mReviewImageView);
                    AnimationUtil.startShowingAnimation(GestureViewManager.this.mFrontCoverView, false, 300, null);
                    if (!setBmp.isRecycled()) {
                        GestureViewManager.this.setImageViewSize(setBmp.getWidth(), setBmp.getHeight());
                        GestureViewManager.this.mReviewImageView.setImageBitmap(setBmp);
                    }
                    GestureViewManager.this.mDeleteBtn.setEnabled(GestureViewManager.this.mIsCompleteSaving);
                    GestureViewManager.this.mQuickClipView.setEnabled(GestureViewManager.this.mIsCompleteSaving);
                    if (useAnimation) {
                        GestureViewManager.this.setQuickViewLayout(false, setBmp, null);
                    }
                }
            }
        });
    }

    public void doButtonClickInDeleteConfirmDialog(boolean okClicked) {
        if (!this.mIsActivatedSelfieQuickView) {
            showGalleryQuickViewAnimation(false, okClicked, false);
        } else if (okClicked) {
            deleteImageAndUpdateThumbnail();
            this.mSelfieQuickViewListener.onHideQuickView();
        } else {
            this.mSelfieQuickViewListener.onDoMotionEngine(2);
        }
    }

    public void setLastBurstFileName(String lastFile) {
        this.mLastBurstFileName = lastFile;
    }

    public String getLastBurstFileName() {
        return this.mLastBurstFileName;
    }

    public void setActivatedSelfieQuickView(boolean set) {
        this.mIsActivatedSelfieQuickView = set;
    }

    public boolean isActivatedSelfieQuickView() {
        return this.mIsActivatedSelfieQuickView;
    }
}
