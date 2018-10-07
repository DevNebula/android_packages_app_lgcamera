package com.lge.camera.managers;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.provider.Settings.System;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.ThumbnailButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AppControlUtilBase;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.QuickClipSharedItem;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.UpdateThumbnailRequest;
import com.lge.camera.util.Utils;
import com.lge.view.MotionPocket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

public class ReviewThumbnailManagerBase extends ManagerInterfaceImpl {
    public static final int GESTUREVIEW_READY = 0;
    public static final int GESTUREVIEW_STARTED_HIDING = 2;
    public static final int GESTUREVIEW_STARTED_SHOWING = 1;
    public static final int SNAPSHOT_ANIDUR = 3000;
    public static final int SNAPSHOT_COMPLETE = 2;
    public static final int SNAPSHOT_SHOWING = 1;
    protected RotateImageView mAdditionalInfoView = null;
    protected View mBackCover = null;
    protected View mBaseView = null;
    protected Bitmap mBitmap = null;
    protected RotateTextView mBurstCount = null;
    private HandlerRunnable mCheckThumbRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (!AppControlUtil.isNeedQuickShotTaking()) {
                ReviewThumbnailManagerBase.this.setContentObserver(false, null);
                ReviewThumbnailManagerBase.this.mGet.setReviewThumbBmp(null);
                ReviewThumbnailManagerBase.this.updateRecentContent(false);
            }
        }
    };
    public ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri changeUri) {
            super.onChange(selfChange, changeUri);
            ReviewThumbnailManagerBase.this.onContentsChanged(changeUri);
        }
    };
    protected RotateImageButton mDeleteBtn = null;
    protected View mDeleteBtnView = null;
    protected Thread mDeleteThumbThread = null;
    protected int mExtraInfoType = -1;
    protected String mFilePath = null;
    protected View mFrontCoverView = null;
    protected GestureDetector mGestureDetector = null;
    protected int mGestureViewProgress = 0;
    protected Thread mImageMakeThread = null;
    protected boolean mIsActivatedSelfieQuickView = false;
    protected boolean mIsHideMode = false;
    protected boolean mIsLaunchGalleryAvail = true;
    protected boolean mIsLaunchingGallery = false;
    protected boolean mIsNeedThumbnailAnimation = false;
    protected float[] mLaunchGalleryLocation = null;
    protected OnReviewThumbnailClickListener mListener = null;
    protected LinkedBlockingQueue<UpdateThumbnailRequest> mQueue = new LinkedBlockingQueue();
    protected RotateImageButton mQuickClipBtn = null;
    protected QuickClipSharedItem mQuickClipSharedItem = null;
    protected View mQuickClipView = null;
    protected onQuickViewListener mQuickViewListener = null;
    protected Uri mRecentUri = null;
    protected RotateLayout mReviewImageLayout = null;
    protected ImageView mReviewImageView = null;
    protected View mReviewLayout = null;
    protected Bitmap mReviewQuickBmp = null;
    protected onSelfieQuickViewListener mSelfieQuickViewListener = null;
    protected Handler mSnapShotHandler = new C09561();
    public ThumbnailButton mThumbBtn = null;
    protected ImageView mThumbBtnBg = null;
    protected RotateImageView mThumbBtnDragShadow = null;
    protected View mThumbnailLayout = null;
    protected Thread mUpdateThumbThread = null;

    public interface onSelfieQuickViewListener {
        void onAnimationStartForGestureView(boolean z);

        void onDoMotionEngine(int i);

        void onHideQuickView();
    }

    public interface onQuickViewListener {
        void onCompleteHideQuickview();
    }

    public interface OnReviewThumbnailClickListener {
        void onImageDeleted();

        void onQuickViewShow(boolean z);

        boolean onReviewThumbnailClick();

        void onReviewThumbnailUpdated(boolean z);
    }

    /* renamed from: com.lge.camera.managers.ReviewThumbnailManagerBase$1 */
    class C09561 extends Handler {
        C09561() {
        }

        public void handleMessage(Message msg) {
            CamLog.m7i(CameraConstants.TAG, "handleMessage what =" + msg.what);
            switch (msg.what) {
                case 1:
                    if (ReviewThumbnailManagerBase.this.mThumbnailLayout != null && ReviewThumbnailManagerBase.this.mGet.checkModuleValidate(1) && ReviewThumbnailManagerBase.this.mGet.checkModuleValidate(192) && !ReviewThumbnailManagerBase.this.mGet.isTimerShotCountdown() && ReviewThumbnailManagerBase.this.mThumbnailLayout.getVisibility() != 0) {
                        ReviewThumbnailManagerBase.this.setUpAllViews(true);
                        AnimationUtil.startSnapShotAnimation(ReviewThumbnailManagerBase.this.mThumbnailLayout, true, 300, null);
                        ReviewThumbnailManagerBase.this.mGet.setReviewThumbBmp(null);
                        return;
                    }
                    return;
                case 2:
                    if (ReviewThumbnailManagerBase.this.mThumbnailLayout != null && ReviewThumbnailManagerBase.this.mGet.checkModuleValidate(1)) {
                        AnimationUtil.startSnapShotAnimation(ReviewThumbnailManagerBase.this.mThumbnailLayout, false, 300, null);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected class QuickViewGestureDetector extends SimpleOnGestureListener {
        protected QuickViewGestureDetector() {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            CamLog.m3d(CameraConstants.TAG, "onFling");
            if (ReviewThumbnailManagerBase.this.mIsActivatedSelfieQuickView && ReviewThumbnailManagerBase.this.mSelfieQuickViewListener != null) {
                ReviewThumbnailManagerBase.this.mSelfieQuickViewListener.onHideQuickView();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public ReviewThumbnailManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setReviewThumbnailClickListener(OnReviewThumbnailClickListener listener) {
        this.mListener = listener;
    }

    public void init() {
        setUpAllViews(false);
        if (this.mThumbnailLayout != null) {
            this.mThumbnailLayout.setVisibility(4);
        }
    }

    public void setThumbnailAnimation(boolean isNeedAni) {
        this.mIsNeedThumbnailAnimation = isNeedAni;
    }

    public boolean isNeedThumbnailAnimation() {
        return this.mIsNeedThumbnailAnimation;
    }

    public void onResumeAfter() {
        setListeners();
        if (AppControlUtil.isNeedQuickShotTaking()) {
            this.mIsLaunchingGallery = false;
        } else {
            updateRecentContent(this.mIsNeedThumbnailAnimation);
        }
        this.mIsNeedThumbnailAnimation = false;
        if (this.mGet.isSquareGalleryBtn() && (getUri() == null || this.mGet.getCurSquareSnapItem() == null || this.mGet.getCurSquareSnapItem().mType == -1)) {
            if (SecureImageUtil.useSecureLockImage() && this.mThumbBtn != null) {
                this.mThumbBtn.setSecureDefaultImage(true);
                this.mThumbBtn.setData(null, false, false, false);
            }
            setEnabled(false);
        }
        setRotateDegree(getOrientationDegree(), false);
        this.mThumbBtn.setVisibility(0);
        setLaunchGalleryAvailable(true);
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (!AppControlUtil.isGalleryLaunched()) {
            hideQuickView(true);
        }
        setContentObserver(false, null);
    }

    public void onStop() {
        hideQuickView(true);
        startBurstCaptureEffect(false);
        setBurstCountVisibility(8, 0);
        if (!(!SecureImageUtil.isSecureCamera() || this.mGet.isStartedFromQuickCover() || this.mGet.isModuleChanging() || AppControlUtil.isGalleryLaunched() || AppControlUtilBase.isVideoLaunched())) {
            SecureImageUtil.get().release();
        }
        if (this.mUpdateThumbThread != null && this.mUpdateThumbThread.isAlive()) {
            this.mUpdateThumbThread.interrupt();
        }
        if (this.mImageMakeThread != null && this.mImageMakeThread.isAlive()) {
            this.mImageMakeThread.interrupt();
        }
        if (!(this.mGet.isModuleChanging() || this.mReviewImageView == null)) {
            this.mReviewImageView.setImageBitmap(null);
        }
        if (this.mThumbBtnDragShadow != null) {
            this.mThumbBtnDragShadow.setImageBitmap(null);
        }
        if (this.mQueue != null) {
            this.mQueue.clear();
        }
        super.onStop();
    }

    public void onDestroy() {
        this.mBaseView = null;
        if (!(this.mThumbBtn == null || this.mGet.isModuleChanging())) {
            this.mThumbBtn.close();
            this.mThumbBtn = null;
        }
        if (this.mQueue != null) {
            this.mQueue.clear();
        }
        this.mThumbBtnBg = null;
        this.mReviewLayout = null;
        this.mThumbnailLayout = null;
        this.mReviewImageView = null;
        this.mReviewImageLayout = null;
        this.mDeleteBtnView = null;
        this.mDeleteBtn = null;
        this.mQuickClipView = null;
        this.mQuickClipBtn = null;
        this.mThumbBtnDragShadow = null;
        this.mBackCover = null;
        this.mBurstCount = null;
        this.mAdditionalInfoView = null;
        this.mDeleteThumbThread = null;
        this.mUpdateThumbThread = null;
        this.mFrontCoverView = null;
        if (this.mReviewQuickBmp != null) {
            this.mReviewQuickBmp.recycle();
            this.mReviewQuickBmp = null;
        }
        this.mListener = null;
        this.mQuickViewListener = null;
    }

    public void onConfigurationChanged(Configuration config) {
        setUpAllViews(true);
        setListeners();
        updateRecentContent(false);
        if (this.mGet.checkModuleValidate(128)) {
            setThumbnailVisibility(0);
        } else {
            setThumbnailVisibility(8);
        }
        super.onConfigurationChanged(config);
    }

    public void setUpAllViews(boolean updateThumb) {
        CamLog.m7i(CameraConstants.TAG, "setUpAllViews()");
        this.mBaseView = this.mGet.findViewById(C0088R.id.camera_controls);
        this.mThumbnailLayout = this.mBaseView.findViewById(C0088R.id.thumbnail_layout);
        this.mThumbBtn = (ThumbnailButton) this.mBaseView.findViewById(C0088R.id.thumbnail);
        this.mThumbBtn.setModuleIF(this.mGet);
        this.mReviewLayout = this.mBaseView.findViewById(C0088R.id.quick_view_layout);
        this.mReviewImageLayout = (RotateLayout) this.mBaseView.findViewById(C0088R.id.review_image_layout_rotate);
        this.mReviewImageView = (ImageView) this.mBaseView.findViewById(C0088R.id.review_image);
        this.mDeleteBtnView = this.mBaseView.findViewById(C0088R.id.quick_view_delete_layout);
        this.mDeleteBtn = (RotateImageButton) this.mBaseView.findViewById(C0088R.id.quick_view_delete);
        this.mQuickClipView = this.mBaseView.findViewById(C0088R.id.quick_view_quick_clip_layout);
        this.mQuickClipBtn = (RotateImageButton) this.mBaseView.findViewById(C0088R.id.quick_view_quick_clip);
        this.mThumbBtnDragShadow = (RotateImageView) this.mBaseView.findViewById(C0088R.id.thumbnail_drag_shadow);
        this.mBackCover = this.mBaseView.findViewById(C0088R.id.quick_view_backcover);
        this.mBurstCount = (RotateTextView) this.mBaseView.findViewById(C0088R.id.thumbnail_burst_count);
        this.mAdditionalInfoView = (RotateImageView) this.mBaseView.findViewById(C0088R.id.thumbnail_additional_info);
        this.mFrontCoverView = this.mBaseView.findViewById(C0088R.id.frontcover);
        setUpManualModeThumbnail();
        Bitmap bmp = this.mGet.getReviewThumbBmp();
        if (this.mGet.isSquareGalleryBtn()) {
            this.mThumbBtn.setSecureDefaultImage(false);
            this.mThumbBtn.setData(null, false, false);
        } else if (!(bmp == null || bmp.isRecycled() || this.mThumbBtn == null)) {
            this.mThumbBtn.setData(bmp, false, false);
        }
        LayoutParams thumbnailLayoutparams = (LayoutParams) this.mThumbnailLayout.getLayoutParams();
        thumbnailLayoutparams.bottomMargin = RatioCalcUtil.getCommandBottomMargin(getAppContext());
        this.mThumbnailLayout.setLayoutParams(thumbnailLayoutparams);
    }

    public void setUpManualModeThumbnail() {
        if (this.mThumbnailLayout != null && this.mThumbBtn != null && this.mAdditionalInfoView != null && this.mBurstCount != null) {
            boolean isCinemaSize;
            int marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.extra_button_marginTop);
            if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode()) && ManualUtil.isCinemaSize(getAppContext(), this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE))) {
                isCinemaSize = true;
            } else {
                isCinemaSize = false;
            }
            LayoutParams thumbnailLayoutparams = (LayoutParams) this.mThumbnailLayout.getLayoutParams();
            if (ModelProperties.isTablet(getAppContext())) {
                marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.07f);
            } else if (isCinemaSize) {
                if (ModelProperties.isLongLCDModel()) {
                    marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.167f);
                } else {
                    marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.152f);
                }
            }
            thumbnailLayoutparams.setMarginEnd(marginEnd);
            this.mThumbnailLayout.setLayoutParams(thumbnailLayoutparams);
        }
    }

    public boolean checkAvailabilityForExecutingAction(int x, int y, View view) {
        if (view == null) {
            return false;
        }
        int[] viewCoord = new int[2];
        view.getLocationOnScreen(viewCoord);
        int viewLeft = viewCoord[0];
        int viewTop = viewCoord[1];
        int viewRight = viewLeft + view.getWidth();
        int viewBottom = viewTop + view.getHeight();
        if (x <= viewLeft || x >= viewRight || y <= viewTop || y >= viewBottom) {
            return false;
        }
        return true;
    }

    public void updateRecentContent(boolean useAni) {
        if (this.mGet.checkModuleValidate(1) && !this.mGet.isSquareGalleryBtn()) {
            Bitmap bmp = this.mGet.getReviewThumbBmp();
            if (bmp == null) {
                updateThumbnail(useAni);
            } else if (this.mThumbBtn != null) {
                this.mThumbBtn.setData(bmp, false, false);
            }
        }
    }

    public void setThumbnailDefault(boolean isEmptyImg, boolean notUseSecureImg) {
        CamLog.m3d(CameraConstants.TAG, "setThumbnailDefault, isEmptyImg : " + isEmptyImg + ", notUseSecureImg : " + notUseSecureImg);
        this.mThumbBtn.setSecureDefaultImage(false);
        this.mThumbBtn.setData(null, false, isEmptyImg, notUseSecureImg);
    }

    protected boolean isAutoReviewAvailable() {
        if (this.mGet.checkModuleValidate(128)) {
            return true;
        }
        return false;
    }

    public void launchGallery(int galleryPlayType) {
        boolean retVal = false;
        CamLog.m3d(CameraConstants.TAG, "launchGallery");
        if (this.mListener != null) {
            retVal = this.mListener.onReviewThumbnailClick();
        }
        Uri uri = getUri();
        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE) && this.mGet.isSquareGalleryBtn()) {
            SquareSnapGalleryItem item = this.mGet.getCurSquareSnapItem();
            if (item != null) {
                uri = item.mUri;
            }
        }
        if (!(retVal || uri == null)) {
            CamLog.m3d(CameraConstants.TAG, "launchGallery - uri : " + uri);
            launchGallery(uri, galleryPlayType);
        }
        if (Utils.checkOOS()) {
            AppControlUtil.blurRecentThumbnail(this.mGet.getActivity(), false);
        } else {
            AppControlUtil.blurRecentThumbnail(0, this.mGet.getActivity());
        }
    }

    public boolean launchSecureGooglePhoto(Uri uri) {
        if (this.mGet == null || this.mGet.getActivity() == null || this.mGet.getActivity().getWindow() == null) {
            return false;
        }
        this.mGet.getActivity().getWindow().addFlags(524288);
        Intent launchIntent = new Intent();
        launchIntent.setPackage(CameraConstants.PACKAGE_GOOGLE_PHOTO);
        launchIntent.setAction("com.android.camera.action.REVIEW");
        launchIntent.addFlags(268468224);
        launchIntent.putExtra(CameraConstants.EXTRA_IS_SECURE_MODE, true);
        ArrayList<Uri> secureList = SecureImageUtil.get().getSecureLockUriList();
        long[] secureSet = new long[secureList.size()];
        for (int i = 0; i < secureList.size(); i++) {
            secureSet[i] = FileUtil.getIdFromUri(getActivity(), (Uri) secureList.get(i));
            CamLog.m3d(CameraConstants.TAG, "google photo secure id : " + secureSet[i]);
        }
        launchIntent.setData(uri);
        launchIntent.putExtra(CameraConstants.EXTRA_SECURE_MODE_MEDIA_STORE_IDS, secureSet);
        return startGalleryActivity(launchIntent);
    }

    public void launchGallery(Uri uri, int galleryPlayType) {
        if (uri == null) {
            CamLog.m3d(CameraConstants.TAG, "Uri is null, return not launching gallery application.");
            return;
        }
        PackageManager pm = this.mGet.getActivity().getPackageManager();
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(CameraConstants.PACKAGE_GALLERY, 128);
        } catch (Exception e) {
            CamLog.m4d(CameraConstants.TAG, "launchGallery error, ", e);
        }
        String mediaType = this.mGet.getAppContext().getContentResolver().getType(uri);
        boolean isImageType = mediaType != null && mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE);
        boolean isCnasContents = FileUtil.isCNasContents(uri, this.mGet.getAppContext());
        Intent intent = new Intent("com.android.camera.action.REVIEW", uri);
        if (info != null) {
            if (info.enabled) {
                setGalleryIntentExtra(intent, galleryPlayType);
            } else {
                this.mGet.showDialog(4);
            }
        }
        if (intent != null) {
            if (isImageType) {
                intent.setDataAndType(uri, "image/*");
            } else {
                intent.setDataAndType(uri, "video/*");
            }
            intent.addFlags(67108864);
            if (isCnasContents) {
                intent.setPackage(CameraConstants.PACKAGE_GALLERY);
            }
            if (QuickWindowUtils.isQuickWindowCameraMode()) {
                CamLog.m7i(CameraConstants.TAG, "set extra to finish gallery when cover closed");
                intent.putExtra("auto-finish-on-cover", true);
            }
            if (SecureImageUtil.useSecureLockImage()) {
                Intent secureIntent = (Intent) intent.clone();
                secureIntent.putParcelableArrayListExtra("secure-picture-list", SecureImageUtil.get().getSecureLockUriList());
                if (galleryPlayType == 1) {
                    secureIntent.setClassName(CameraConstants.PACKAGE_GALLERY, CameraConstantsEx.ACTIVITY_GALLERY_SECURE_SPHERICAL_VIEWER);
                } else {
                    secureIntent.setClassName(CameraConstants.PACKAGE_GALLERY, CameraConstants.ACTIVITY_GALLERY_SECURE);
                }
                if (!startGalleryActivity(secureIntent)) {
                    if (info == null && !isCnasContents && launchSecureGooglePhoto(uri)) {
                        CamLog.m3d(CameraConstants.TAG, "launch secure GooglePhoto ");
                        return;
                    }
                }
                return;
            }
            if (!startGalleryActivity(intent) && !isCnasContents) {
                enableGooglePhotos(pm, info);
            }
        }
    }

    protected void enableGooglePhotos(PackageManager pm, ApplicationInfo info) {
        if (pm != null) {
            info = null;
            try {
                info = pm.getApplicationInfo(CameraConstants.PACKAGE_GOOGLE_PHOTO, 128);
            } catch (Exception e) {
                CamLog.m4d(CameraConstants.TAG, "launchGooglePhoto error, ", e);
            }
            if (info != null && !info.enabled) {
                this.mGet.showDialog(11);
            }
        }
    }

    private boolean startGalleryActivity(Intent intent) {
        ComponentName componentName = null;
        boolean z = true;
        try {
            if (MDMUtil.isAppDisabledByMDM(CameraConstants.PACKAGE_GALLERY)) {
                AppControlUtil.setLaunchingGallery(false);
            } else {
                AppControlUtil.setLaunchingGallery(true);
                this.mIsLaunchingGallery = true;
            }
            if (this.mLaunchGalleryLocation == null) {
                Object obj = new float[]{0.9f, 0.97f};
            }
            intent.putExtra("com.lge.intent.extra.SCALE_PIVOT", this.mLaunchGalleryLocation);
            this.mGet.getActivity().startActivity(intent);
            MotionPocket.overridePendingTransitionScale(this.mGet.getActivity(), this.mLaunchGalleryLocation[0], this.mLaunchGalleryLocation[1], true);
            this.mLaunchGalleryLocation = null;
            return z;
        } catch (ActivityNotFoundException ex) {
            this.mGet.setPreviewCoverBackground(null);
            this.mGet.setPreviewCoverVisibility(8, true);
            AppControlUtil.setLaunchingGallery(false);
            z = CameraConstants.TAG;
            CamLog.m6e(z, "review fail", ex);
            return componentName;
        } catch (SecurityException e) {
            componentName = null;
            intent.setComponent(null);
            componentName = this.mGet.getActivity();
            componentName.startActivity(intent);
            return z;
        } catch (NoClassDefFoundError e2) {
            componentName = this.mGet.getActivity();
            componentName.startActivity(intent);
            e2.printStackTrace();
            return z;
        } catch (NoSuchMethodError e3) {
            componentName = this.mGet.getActivity();
            componentName.startActivity(intent);
            e3.printStackTrace();
            return z;
        } finally {
            this.mLaunchGalleryLocation = null;
        }
    }

    public void setGalleryIntentExtra(Intent intent, int galleryPlayType) {
        boolean isRotationAutoOff = false;
        if (System.getInt(this.mGet.getActivity().getContentResolver(), "accelerometer_rotation", 0) == 0) {
            isRotationAutoOff = true;
        }
        if (!isRotationAutoOff) {
            intent.putExtra("screen-orientation", getOrientaionString());
        } else if (setExtraForKDDI()) {
            CamLog.m7i(CameraConstants.TAG, "set extra to reversePortrait for KDDI");
            intent.putExtra("screen-orientation", "reversePortrait");
        }
        if (galleryPlayType == 0) {
            return;
        }
        if (galleryPlayType == 1) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] 360 panorama play");
            intent.setClassName(CameraConstants.PACKAGE_GALLERY, CameraConstantsEx.ACTIVITY_GALLERY_SPHERICAL_VIEWER);
        } else if (galleryPlayType == 2) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] burst shot play");
            intent.putExtra("lge-play-burstshot", true);
        } else if (galleryPlayType == 3) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] guide shot play");
            intent.putExtra("lge-play-overlapshot", true);
        }
    }

    protected String getOrientaionString() {
        String strOrientation = null;
        switch (this.mGet.getOrientationDegree()) {
            case 0:
                strOrientation = "portrait";
                break;
            case 90:
                strOrientation = "reverseLandscape";
                break;
            case 180:
                strOrientation = "reversePortrait";
                break;
            case 270:
                strOrientation = "landscape";
                break;
        }
        CamLog.m7i(CameraConstants.TAG, "set extra orientation :" + strOrientation);
        return strOrientation;
    }

    protected boolean setExtraForKDDI() {
        if (ModelProperties.getCarrierCode() != 7) {
            return false;
        }
        if (!(this.mGet.getOrientationDegree() == 180) || this.mGet.isRearCamera()) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:51:?, code:
            return null;
     */
    public android.net.Uri getMostRecentContent(java.lang.String r21) {
        /*
        r20 = this;
        r15 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        r16 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        r3 = r15.buildUpon();
        r7 = "limit";
        r9 = "1";
        r3 = r3.appendQueryParameter(r7, r9);
        r4 = r3.build();
        r3 = r16.buildUpon();
        r7 = "limit";
        r9 = "1";
        r3 = r3.appendQueryParameter(r7, r9);
        r10 = r3.build();
        r3 = 4;
        r5 = new java.lang.String[r3];
        r3 = 0;
        r7 = "_id";
        r5[r3] = r7;
        r3 = 1;
        r7 = "orientation";
        r5[r3] = r7;
        r3 = 2;
        r7 = "datetaken";
        r5[r3] = r7;
        r3 = 3;
        r7 = "_display_name";
        r5[r3] = r7;
        r3 = 3;
        r11 = new java.lang.String[r3];
        r3 = 0;
        r7 = "_id";
        r11[r3] = r7;
        r3 = 1;
        r7 = "datetaken";
        r11[r3] = r7;
        r3 = 2;
        r7 = "_display_name";
        r11[r3] = r7;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r7 = "(mime_type='image/jpeg' OR mime_type='image/gif') AND (";
        r3 = r3.append(r7);
        r0 = r21;
        r3 = r3.append(r0);
        r7 = ")";
        r3 = r3.append(r7);
        r6 = r3.toString();
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r7 = "(mime_type='video/mp4' OR mime_type='video/3gpp') AND (";
        r3 = r3.append(r7);
        r0 = r21;
        r3 = r3.append(r0);
        r7 = ")";
        r3 = r3.append(r7);
        r12 = r3.toString();
        r8 = "datetaken DESC,_id DESC";
        r18 = 0;
        r19 = 0;
        r3 = r20.getActivity();
        if (r3 != 0) goto L_0x0091;
    L_0x008f:
        r3 = 0;
    L_0x0090:
        return r3;
    L_0x0091:
        r3 = "CameraApp";
        r7 = "getContentResolver start";
        com.lge.camera.util.CamLog.m3d(r3, r7);	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r0 = r20;
        r3 = r0.mGet;	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r3 = r3.getActivity();	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r3 = r3.getContentResolver();	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r7 = 0;
        r18 = r3.query(r4, r5, r6, r7, r8);	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r0 = r20;
        r3 = r0.mGet;	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r3 = r3.getActivity();	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r9 = r3.getContentResolver();	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r13 = 0;
        r14 = r8;
        r19 = r9.query(r10, r11, r12, r13, r14);	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        if (r18 == 0) goto L_0x00bf;
    L_0x00bd:
        if (r19 != 0) goto L_0x00cf;
    L_0x00bf:
        r3 = 0;
        if (r18 == 0) goto L_0x00c7;
    L_0x00c2:
        r18.close();
        r18 = 0;
    L_0x00c7:
        if (r19 == 0) goto L_0x0090;
    L_0x00c9:
        r19.close();
        r19 = 0;
        goto L_0x0090;
    L_0x00cf:
        r3 = "CameraApp";
        r7 = "getContentResolver end";
        com.lge.camera.util.CamLog.m3d(r3, r7);	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        r0 = r20;
        r1 = r18;
        r2 = r19;
        r3 = r0.getRecentUri(r1, r2);	 Catch:{ SQLiteException -> 0x00ef, IllegalStateException -> 0x0109, SecurityException -> 0x0122 }
        if (r18 == 0) goto L_0x00e7;
    L_0x00e2:
        r18.close();
        r18 = 0;
    L_0x00e7:
        if (r19 == 0) goto L_0x0090;
    L_0x00e9:
        r19.close();
        r19 = 0;
        goto L_0x0090;
    L_0x00ef:
        r17 = move-exception;
        r3 = "CameraApp";
        r7 = "cursor error ";
        r0 = r17;
        com.lge.camera.util.CamLog.m6e(r3, r7, r0);	 Catch:{ all -> 0x013b }
        if (r18 == 0) goto L_0x0100;
    L_0x00fb:
        r18.close();
        r18 = 0;
    L_0x0100:
        if (r19 == 0) goto L_0x0107;
    L_0x0102:
        r19.close();
        r19 = 0;
    L_0x0107:
        r3 = 0;
        goto L_0x0090;
    L_0x0109:
        r17 = move-exception;
        r3 = "CameraApp";
        r7 = "cursor error ";
        r0 = r17;
        com.lge.camera.util.CamLog.m6e(r3, r7, r0);	 Catch:{ all -> 0x013b }
        if (r18 == 0) goto L_0x011a;
    L_0x0115:
        r18.close();
        r18 = 0;
    L_0x011a:
        if (r19 == 0) goto L_0x0107;
    L_0x011c:
        r19.close();
        r19 = 0;
        goto L_0x0107;
    L_0x0122:
        r17 = move-exception;
        r3 = "CameraApp";
        r7 = "Security Exception error ";
        r0 = r17;
        com.lge.camera.util.CamLog.m6e(r3, r7, r0);	 Catch:{ all -> 0x013b }
        if (r18 == 0) goto L_0x0133;
    L_0x012e:
        r18.close();
        r18 = 0;
    L_0x0133:
        if (r19 == 0) goto L_0x0107;
    L_0x0135:
        r19.close();
        r19 = 0;
        goto L_0x0107;
    L_0x013b:
        r3 = move-exception;
        if (r18 == 0) goto L_0x0143;
    L_0x013e:
        r18.close();
        r18 = 0;
    L_0x0143:
        if (r19 == 0) goto L_0x014a;
    L_0x0145:
        r19.close();
        r19 = 0;
    L_0x014a:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ReviewThumbnailManagerBase.getMostRecentContent(java.lang.String):android.net.Uri");
    }

    protected Uri getRecentUri(Cursor imageQueryCursor, Cursor videoQueryCursor) {
        Uri baseImageUri = Media.EXTERNAL_CONTENT_URI;
        Uri baseVideoUri = Video.Media.EXTERNAL_CONTENT_URI;
        Long id = Long.valueOf(-1);
        if (imageQueryCursor.getCount() == 0 && videoQueryCursor.getCount() == 0) {
            return null;
        }
        Uri returnUri;
        if (imageQueryCursor.getCount() == 0 && videoQueryCursor.getCount() != 0) {
            videoQueryCursor.moveToFirst();
            returnUri = baseVideoUri;
            id = Long.valueOf(videoQueryCursor.getLong(videoQueryCursor.getColumnIndexOrThrow("_id")));
        } else if (imageQueryCursor.getCount() == 0 || videoQueryCursor.getCount() != 0) {
            imageQueryCursor.moveToFirst();
            videoQueryCursor.moveToFirst();
            if (Long.valueOf(imageQueryCursor.getLong(imageQueryCursor.getColumnIndexOrThrow("datetaken"))).longValue() > Long.valueOf(videoQueryCursor.getLong(videoQueryCursor.getColumnIndexOrThrow("datetaken"))).longValue()) {
                returnUri = baseImageUri;
                id = Long.valueOf(imageQueryCursor.getLong(imageQueryCursor.getColumnIndexOrThrow("_id")));
            } else {
                returnUri = baseVideoUri;
                id = Long.valueOf(videoQueryCursor.getLong(videoQueryCursor.getColumnIndexOrThrow("_id")));
            }
        } else {
            imageQueryCursor.moveToFirst();
            returnUri = baseImageUri;
            id = Long.valueOf(imageQueryCursor.getLong(imageQueryCursor.getColumnIndexOrThrow("_id")));
        }
        return ContentUris.withAppendedId(returnUri, id.longValue());
    }

    public static String getBucketId(String currentDir) {
        String currentStorage = currentDir;
        if (currentStorage.length() <= 0) {
            return null;
        }
        return String.valueOf(currentStorage.substring(0, currentStorage.length() - 1).toLowerCase(Locale.US).hashCode());
    }

    public void setThumbnailVisibilityAfterStopRecording() {
        if (!this.mIsHideMode) {
            Animation scaleAni = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, 0.5f, 1, 0.5f);
            scaleAni.setDuration(400);
            scaleAni.setInterpolator(new DecelerateInterpolator());
            this.mThumbnailLayout.startAnimation(scaleAni);
        }
        this.mThumbnailLayout.setVisibility(this.mIsHideMode ? 4 : 0);
        if (this.mSnapShotHandler != null) {
            this.mSnapShotHandler.removeMessages(1);
            this.mSnapShotHandler.removeMessages(2);
        }
    }

    public void setThumbnailVisibility(int visibility) {
        setThumbnailVisibility(visibility, false);
    }

    public void setThumbnailVisibility(int visibility, boolean useAnimation) {
        setThumbnailVisibility(visibility, useAnimation, false);
    }

    public void setThumbnailVisibility(int visibility, boolean useAnimation, boolean enforced) {
        if (this.mThumbnailLayout != null) {
            if (this.mIsHideMode && !enforced) {
                visibility = 8;
            }
            if (useAnimation) {
                if ((this.mThumbnailLayout.getVisibility() != 0 && visibility == 0) || (this.mThumbnailLayout.getVisibility() == 0 && visibility != 0)) {
                    AnimationUtil.startTransAnimation(this.mThumbnailLayout, visibility == 0, Utils.isConfigureLandscape(getAppContext().getResources()), null, false);
                }
            } else if (this.mThumbnailLayout.getVisibility() != visibility) {
                this.mThumbnailLayout.setVisibility(visibility);
            }
            if (this.mSnapShotHandler != null) {
                this.mSnapShotHandler.removeMessages(1);
                this.mSnapShotHandler.removeMessages(2);
            }
        }
    }

    public void updateSnapShotThumbnail(boolean update) {
        if (this.mThumbnailLayout != null && this.mGet.checkModuleValidate(1) && checkRecordingStateValidate(192) && this.mSnapShotHandler != null && update) {
            this.mSnapShotHandler.removeMessages(2);
            if (this.mThumbnailLayout.getVisibility() != 0) {
                this.mSnapShotHandler.sendEmptyMessage(1);
            }
        }
    }

    public void setBurstCountVisibility(int visibility, int burstCount) {
        if (this.mBaseView != null && this.mBurstCount != null) {
            if (visibility == 8) {
                this.mBurstCount.setVisibility(visibility);
                if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(this.mGet.getShotMode())) {
                    this.mThumbBtn.setData(null, false, false, true);
                    return;
                }
                return;
            }
            if (visibility == 0 && CameraConstants.MODE_SQUARE_SNAPSHOT.equals(this.mGet.getShotMode()) && burstCount == 1) {
                this.mThumbBtn.setData(null, false, true, true);
            }
            if (this.mBurstCount.getVisibility() != visibility) {
                this.mBurstCount.setVisibility(visibility);
            }
            this.mBurstCount.textChanging(String.valueOf(burstCount));
            this.mBurstCount.invalidate();
            CamLog.m3d(CameraConstants.TAG, "mBurstCount invalidate. burstCount : " + burstCount);
        }
    }

    public void setBurstCaptureEffect() {
        if (this.mBaseView != null && this.mAdditionalInfoView != null && this.mAdditionalInfoView.getVisibility() == 8) {
            this.mAdditionalInfoView.setImageResource(C0088R.drawable.shutter_gallery_burst_effect);
            this.mAdditionalInfoView.setVisibility(4);
        }
    }

    public void startBurstCaptureEffect(boolean visible) {
        if (this.mBaseView != null && this.mAdditionalInfoView != null) {
            if (visible) {
                setBurstCaptureEffect();
                AnimationUtil.startSnapShotAnimation(this.mAdditionalInfoView, false, (long) FunctionProperties.getSupportedBurstShotDuration(false, false, this.mGet.isRearCamera()), null);
                return;
            }
            this.mAdditionalInfoView.setImageDrawable(null);
            this.mAdditionalInfoView.setVisibility(8);
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.mGet.checkModuleValidate(8) && this.mThumbBtn != null) {
            this.mThumbBtn.setEnabled(enabled);
        }
    }

    public void setPressed(boolean pressed) {
        if (this.mThumbBtn != null) {
            this.mThumbBtn.setPressed(pressed);
        }
    }

    public void setLaunchGalleryAvailable(boolean available) {
        CamLog.m3d(CameraConstants.TAG, "setLaunchGalleryAvailable : " + available);
        this.mIsLaunchGalleryAvail = available;
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mThumbBtn != null && this.mDeleteBtn != null && this.mThumbBtnDragShadow != null && this.mReviewImageLayout != null && this.mBurstCount != null && this.mAdditionalInfoView != null) {
            this.mThumbBtn.setDegree(degree, animation);
            this.mDeleteBtn.setDegree(degree, animation);
            this.mThumbBtnDragShadow.setDegree(degree, animation);
            this.mReviewImageLayout.rotateLayout(degree);
            this.mBurstCount.setDegree(degree, animation);
            this.mAdditionalInfoView.setDegree(degree, animation);
            this.mQuickClipBtn.setDegree(degree, true);
        }
    }

    public Uri getUri() {
        if (AppControlUtil.isGuestMode()) {
            if (AppControlUtil.checkGalleryEnabledOnGuestMode(this.mGet.getAppContext().getContentResolver())) {
                return getMostRecentContent(FileUtil.getBucketIDStr(this.mGet.getAllDir(false)));
            }
            SecureImageUtil.get().updateSecureLockRecentUri(this.mGet.getActivity());
            if (SecureImageUtil.get().isSecureLockUriListEmpty()) {
                return null;
            }
            return (Uri) SecureImageUtil.get().getSecureLockUriList().get(SecureImageUtil.get().getSecureLockUriListSize() - 1);
        } else if (SecureImageUtil.useSecureLockImage()) {
            SecureImageUtil.get().updateSecureLockRecentUri(this.mGet.getActivity());
            if (SecureImageUtil.get().isSecureLockUriListEmpty()) {
                return null;
            }
            return (Uri) SecureImageUtil.get().getSecureLockUriList().get(SecureImageUtil.get().getSecureLockUriListSize() - 1);
        } else {
            Uri uri;
            String savedPrefUriStr = SharedPreferenceUtil.getLastThumbnailUri(getAppContext());
            String savedPrefPathStr = SharedPreferenceUtil.getLastThumbnailPath(getAppContext());
            if (savedPrefUriStr != null) {
                String pathFromUri = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), Uri.parse(savedPrefUriStr));
                if (pathFromUri == null || !pathFromUri.equals(savedPrefPathStr)) {
                    CamLog.m3d(CameraConstants.TAG, String.format("Saved uri is not valid. Find most recent uri.", new Object[0]));
                    SharedPreferenceUtil.saveLastThumbnail(this.mGet.getAppContext(), null);
                    savedPrefUriStr = null;
                }
            }
            if (savedPrefUriStr != null) {
                uri = Uri.parse(savedPrefUriStr);
            } else {
                uri = getMostRecentContent(FileUtil.getBucketIDStr(this.mGet.getAllDir(true)));
            }
            if (savedPrefUriStr != null) {
                return uri;
            }
            CamLog.m3d(CameraConstants.TAG, "save URI to sharedPreferenceUtil. uri = " + uri);
            SharedPreferenceUtil.saveLastThumbnail(getAppContext(), uri);
            return uri;
        }
    }

    public void checkLastThumbnailDeleted(Uri uri) {
        if (uri != null && this.mRecentUri != null && this.mRecentUri.equals(uri) && this.mThumbBtn != null) {
            CamLog.m3d(CameraConstants.TAG, "[thumbnail] uri = " + uri);
            this.mThumbBtn.setData(null, false, false);
            this.mIsNeedThumbnailAnimation = true;
        }
    }

    public void onContentsChanged(Uri changeUri) {
        if (changeUri != null && this.mRecentUri != null) {
            CamLog.m3d(CameraConstants.TAG, "changeUri : " + changeUri.toString());
            this.mGet.removePostRunnable(this.mCheckThumbRunnable);
            this.mGet.postOnUiThread(this.mCheckThumbRunnable, 500);
        }
    }

    public void setContentObserver(boolean register, Uri uri) {
        Context context = this.mGet.getAppContext();
        if (context != null) {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return;
            }
            if (!register) {
                CamLog.m3d(CameraConstants.TAG, "setContentObserver : unregister");
                resolver.unregisterContentObserver(this.mContentObserver);
            } else if (uri != null) {
                CamLog.m3d(CameraConstants.TAG, "setContentObserver : register");
                resolver.unregisterContentObserver(this.mContentObserver);
                resolver.registerContentObserver(uri, true, this.mContentObserver);
            }
        }
    }

    public void setListeners() {
    }

    protected void updateThumbnail(boolean useAni) {
    }

    protected void showQuickView(boolean isAutoReview) {
    }

    protected void hideQuickView(boolean hideReviewLayout) {
    }

    protected void showGalleryQuickViewAnimation(boolean open, boolean deleteImage, boolean isAutoReview) {
    }

    public void addRequest(UpdateThumbnailRequest request) {
        if (!QuickWindowUtils.isQuickWindowCameraMode() && request != null && this.mQueue != null) {
            try {
                this.mQueue.put(request);
            } catch (InterruptedException e) {
                CamLog.m6e(CameraConstants.TAG, "InterruptedException : ", e);
            }
        }
    }

    public UpdateThumbnailRequest getRequest() {
        if (this.mQueue == null) {
            return null;
        }
        return (UpdateThumbnailRequest) this.mQueue.poll();
    }

    public void pollRequest() {
        if (this.mQueue != null) {
            UpdateThumbnailRequest request = (UpdateThumbnailRequest) this.mQueue.poll();
            if (request != null) {
                if (!(request.mThumbBitmap == null || request.mThumbBitmap.isRecycled())) {
                    request.mThumbBitmap.recycle();
                }
                request.unbind();
            }
        }
    }

    public void removeRequest(UpdateThumbnailRequest request) {
        if (this.mQueue != null) {
            this.mQueue.remove(request);
            request.unbind();
        }
    }

    public void setHideMode(boolean isHide) {
        this.mIsHideMode = isHide;
    }

    protected boolean checkRecordingStateValidate(int checkType) {
        int i;
        boolean invalid = false;
        int cameraState = this.mGet.getCameraState();
        if ((checkType & 64) != 0) {
            i = (cameraState == 5 || cameraState == 8) ? 1 : 0;
            invalid = false | i;
        }
        if ((checkType & 128) != 0) {
            if (cameraState == 6 || cameraState == 7) {
                i = 1;
            } else {
                i = 0;
            }
            invalid |= i;
        }
        if (invalid) {
            return false;
        }
        return true;
    }

    public void setLaunchGalleryLocation(float[] loc) {
        this.mLaunchGalleryLocation = loc;
    }
}
