package com.lge.camera.managers;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.dialog.CamDialogInterface;
import com.lge.camera.dialog.QuickClipPopUpDialog;
import com.lge.camera.dialog.QuickClipPopUpDialog.QuickClipPopupInterface;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.UpdateThumbnailRequest;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;
import java.io.File;

public class ReviewThumbnailManager extends ReviewThumbnailManagerBase implements OnClickListener, OnLongClickListener, OnTouchListener {
    protected int mDegree = -1;
    protected int mGalleryWindowAniState = 0;
    private Thread mImageMakeThread = null;
    protected Object mUriLock = new Object();

    /* renamed from: com.lge.camera.managers.ReviewThumbnailManager$11 */
    class C096011 extends Thread {
        C096011() {
        }

        /* JADX WARNING: Missing block: B:14:0x006d, code:
            if (isInterrupted() == false) goto L_0x00a4;
     */
        /* JADX WARNING: Missing block: B:15:0x006f, code:
            com.lge.camera.util.CamLog.m3d(com.lge.camera.constants.CameraConstants.TAG, "mDeleteThumbnailThread is isInterrupted()");
     */
        /* JADX WARNING: Missing block: B:26:0x00a4, code:
            com.lge.camera.util.CamLog.m3d(com.lge.camera.constants.CameraConstants.TAG, "mDeleteThumbThread end");
            r10.this$0.mGet.postOnUiThread(new com.lge.camera.managers.ReviewThumbnailManager.C096011.C09611(r10, r10.this$0), 0);
     */
        /* JADX WARNING: Missing block: B:27:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            return;
     */
        public void run() {
            /*
            r10 = this;
            r4 = "CameraApp";
            r5 = "mDeleteThumbThread start";
            com.lge.camera.util.CamLog.m3d(r4, r5);
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;
            r5 = r4.mUriLock;
            monitor-enter(r5);
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ all -> 0x0098 }
            r3 = r4.getUri();	 Catch:{ all -> 0x0098 }
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ all -> 0x0098 }
            r4 = r4.mGet;	 Catch:{ all -> 0x0098 }
            r4 = r4.getActivity();	 Catch:{ all -> 0x0098 }
            r2 = com.lge.camera.util.FileUtil.getRealPathFromURI(r4, r3);	 Catch:{ all -> 0x0098 }
            if (r2 == 0) goto L_0x0068;
        L_0x0020:
            r1 = new java.io.File;	 Catch:{ Exception -> 0x009b }
            r1.<init>(r2);	 Catch:{ Exception -> 0x009b }
            r4 = r1.exists();	 Catch:{ Exception -> 0x009b }
            if (r4 == 0) goto L_0x0077;
        L_0x002b:
            r4 = r1.delete();	 Catch:{ Exception -> 0x009b }
            if (r4 == 0) goto L_0x0077;
        L_0x0031:
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ Exception -> 0x009b }
            r4 = r4.mGet;	 Catch:{ Exception -> 0x009b }
            r4 = r4.getActivity();	 Catch:{ Exception -> 0x009b }
            r4 = r4.getContentResolver();	 Catch:{ Exception -> 0x009b }
            r6 = 0;
            r7 = 0;
            r4.delete(r3, r6, r7);	 Catch:{ Exception -> 0x009b }
            r4 = com.lge.camera.util.SecureImageUtil.get();	 Catch:{ Exception -> 0x009b }
            r4.removeSecureLockUri(r3);	 Catch:{ Exception -> 0x009b }
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ Exception -> 0x009b }
            r4 = r4.mGet;	 Catch:{ Exception -> 0x009b }
            r4 = r4.getAppContext();	 Catch:{ Exception -> 0x009b }
            com.lge.camera.util.FileUtil.deleteDNGFile(r4, r2);	 Catch:{ Exception -> 0x009b }
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ Exception -> 0x009b }
            r4 = r4.mGet;	 Catch:{ Exception -> 0x009b }
            r4 = r4.getAppContext();	 Catch:{ Exception -> 0x009b }
            r6 = 0;
            com.lge.camera.util.SharedPreferenceUtil.saveLastThumbnail(r4, r6, r2);	 Catch:{ Exception -> 0x009b }
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ Exception -> 0x009b }
            r4 = r4.mGet;	 Catch:{ Exception -> 0x009b }
            r6 = 0;
            r4.setReviewThumbBmp(r6);	 Catch:{ Exception -> 0x009b }
        L_0x0068:
            monitor-exit(r5);	 Catch:{ all -> 0x0098 }
            r4 = r10.isInterrupted();
            if (r4 == 0) goto L_0x00a4;
        L_0x006f:
            r4 = "CameraApp";
            r5 = "mDeleteThumbnailThread is isInterrupted()";
            com.lge.camera.util.CamLog.m3d(r4, r5);
        L_0x0076:
            return;
        L_0x0077:
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ Exception -> 0x009b }
            r4 = r4.mGet;	 Catch:{ Exception -> 0x009b }
            r6 = com.lge.camera.managers.ReviewThumbnailManager.this;	 Catch:{ Exception -> 0x009b }
            r6 = r6.mGet;	 Catch:{ Exception -> 0x009b }
            r6 = r6.getAppContext();	 Catch:{ Exception -> 0x009b }
            r7 = 2131231578; // 0x7f08035a float:1.807924E38 double:1.052968306E-314;
            r6 = r6.getString(r7);	 Catch:{ Exception -> 0x009b }
            r8 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
            r4.showToast(r6, r8);	 Catch:{ Exception -> 0x009b }
            r4 = "CameraApp";
            r6 = "Delete failed";
            com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ Exception -> 0x009b }
            monitor-exit(r5);	 Catch:{ all -> 0x0098 }
            goto L_0x0076;
        L_0x0098:
            r4 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x0098 }
            throw r4;
        L_0x009b:
            r0 = move-exception;
            r4 = "CameraApp";
            r6 = "delete Thumbnail fail : ";
            com.lge.camera.util.CamLog.m6e(r4, r6, r0);	 Catch:{ all -> 0x0098 }
            goto L_0x0068;
        L_0x00a4:
            r4 = "CameraApp";
            r5 = "mDeleteThumbThread end";
            com.lge.camera.util.CamLog.m3d(r4, r5);
            r4 = com.lge.camera.managers.ReviewThumbnailManager.this;
            r4 = r4.mGet;
            r5 = new com.lge.camera.managers.ReviewThumbnailManager$11$1;
            r6 = com.lge.camera.managers.ReviewThumbnailManager.this;
            r5.<init>(r6);
            r6 = 0;
            r4.postOnUiThread(r5, r6);
            goto L_0x0076;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ReviewThumbnailManager.11.run():void");
        }
    }

    /* renamed from: com.lge.camera.managers.ReviewThumbnailManager$2 */
    class C09642 implements OnFocusChangeListener {
        C09642() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (ReviewThumbnailManager.this.mThumbBtnBg != null) {
                if (hasFocus) {
                    ReviewThumbnailManager.this.mThumbBtnBg.setVisibility(0);
                    ReviewThumbnailManager.this.mThumbBtnBg.setNextFocusDownId(ReviewThumbnailManager.this.mThumbBtn.getId());
                    return;
                }
                ReviewThumbnailManager.this.mThumbBtnBg.setVisibility(8);
            }
        }
    }

    public ReviewThumbnailManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public boolean onLongClick(View v) {
        CamLog.m3d(CameraConstants.TAG, "onLongClick");
        if ((this.mBurstCount != null && this.mBurstCount.getVisibility() == 0) || this.mGet.isVolumeKeyPressed()) {
            CamLog.m3d(CameraConstants.TAG, "burst shot taking");
            return false;
        } else if (this.mGet.isTimerShotCountdown()) {
            CamLog.m3d(CameraConstants.TAG, "return review thumbnail longClick. During timerShot.");
            return false;
        } else if (this.mGet.isSquareGalleryBtn()) {
            return false;
        } else {
            if ((SecureImageUtil.useSecureLockImage() && SecureImageUtil.get().isSecureLockUriListEmpty()) || SystemBarUtil.isSystemUIVisible(getActivity()) || !this.mGet.checkModuleValidate(223) || this.mGet.isRotateDialogVisible() || this.mGet.isCenterKeyPressed() || this.mIsActivatedSelfieQuickView || this.mGet.isActivatedQuickdetailView()) {
                return false;
            }
            if (this.mGet.isShutterlessSelfieProgress()) {
                this.mGet.pauseShutterless();
            }
            showQuickView(false);
            return true;
        }
    }

    public void onClick(View v) {
        onReviewThumbnailClick();
    }

    public void onReviewThumbnailClick() {
        CamLog.m3d(CameraConstants.TAG, "ReviewThumbnailManager - onClick");
        if (!SystemBarUtil.isSystemUIVisible(getActivity())) {
            if (!this.mGet.checkModuleValidate(255) || this.mGet.isCenterKeyPressed() || this.mGet.isAnimationShowing() || this.mGet.isIntervalShotProgress() || this.mGet.isActivatedQuickdetailView() || this.mGet.isTimerShotCountdown()) {
                CamLog.m3d(CameraConstants.TAG, "camera state is not available to launch gallery");
                return;
            }
            if (this.mGet.isShutterlessSelfieProgress()) {
                this.mGet.pauseShutterless();
            }
            if (this.mIsLaunchGalleryAvail) {
                launchGallery(0);
            } else if (this.mListener.onReviewThumbnailClick()) {
                CamLog.m3d(CameraConstants.TAG, "Not available to launch gallery, saving not completed.");
            } else {
                CamLog.m3d(CameraConstants.TAG, "saving dialog : review thumbnail manager");
                this.mGet.showSavingDialog(true, 0);
                CamLog.m3d(CameraConstants.TAG, "Not available to launch gallery, saving not completed.");
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        int[] thumbnailViewPosition = new int[2];
        if (this.mThumbBtn != null) {
            this.mThumbBtn.getLocationOnScreen(thumbnailViewPosition);
        }
        int convertedX = thumbnailViewPosition[0] + x;
        int convertedY = thumbnailViewPosition[1] + y;
        switch (action) {
            case 1:
                doActionUp(x, y, convertedX, convertedY);
                break;
            case 2:
                if (!this.mIsActivatedSelfieQuickView) {
                    if (this.mGalleryWindowAniState != 0) {
                        updateThumbnailDragShadowPostion(x, y);
                    }
                    if (this.mGalleryWindowAniState == 3) {
                        if (!checkAvailabilityForExecutingAction(convertedX, convertedY, this.mDeleteBtn)) {
                            if (!checkAvailabilityForExecutingAction(convertedX, convertedY, this.mQuickClipView)) {
                                resetQuickViewDrawableState(false);
                                break;
                            }
                            resetQuickViewQuickClipDrawableState(true);
                            break;
                        }
                        resetQuickViewDrawableState(true);
                        break;
                    }
                }
                break;
        }
        return false;
    }

    private void doActionUp(int x, int y, int convertedX, int convertedY) {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "[QuickView] mIsActivatedSelfieQuickView : " + this.mIsActivatedSelfieQuickView + ", mGalleryWindowAniState : " + this.mGalleryWindowAniState);
        if (!this.mIsActivatedSelfieQuickView) {
            if (this.mGalleryWindowAniState != 3) {
                String str = CameraConstants.TAG;
                StringBuilder append = new StringBuilder().append("[QuickView] mImageMakeThread is null ? ");
                if (this.mImageMakeThread != null) {
                    z = false;
                }
                CamLog.m3d(str, append.append(z).toString());
                CamLog.m3d(CameraConstants.TAG, "[QuickView] mGalleryWindowAniState : " + this.mGalleryWindowAniState);
                if (this.mImageMakeThread != null && this.mImageMakeThread.isAlive()) {
                    CamLog.m3d(CameraConstants.TAG, "[QuickView] mImageMakeThread is alive ? true");
                    this.mImageMakeThread.interrupt();
                    this.mGet.postOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            CamLog.m3d(CameraConstants.TAG, "[QuickView] By postOnUiThread. hide gallery quick view after doAction up. mGalleryWindowAniState : " + ReviewThumbnailManager.this.mGalleryWindowAniState);
                            if (ReviewThumbnailManager.this.mGalleryWindowAniState == 1) {
                                ReviewThumbnailManager.this.showGalleryQuickViewAnimation(false, false, false);
                            }
                        }
                    }, 0);
                }
                if (this.mGalleryWindowAniState == 0) {
                    return;
                }
            } else if (checkAvailabilityForExecutingAction(convertedX, convertedY, this.mDeleteBtn)) {
                CamLog.m3d(CameraConstants.TAG, "[QuickView] delete button availbale, hide gallery quick view.");
                if (ModelProperties.getCarrierCode() == 6) {
                    this.mGet.showDialog(2);
                    return;
                } else {
                    showGalleryQuickViewAnimation(false, true, false);
                    return;
                }
            } else if (checkAvailabilityForExecutingAction(convertedX, convertedY, this.mQuickClipView)) {
                CamLog.m3d(CameraConstants.TAG, "[QuickView] launch quick clip share app after quick clip icon was clicked.");
                launchQuickClipSharedApp(true);
                return;
            }
            showGalleryQuickViewAnimation(false, false, false);
        }
    }

    public void resetQuickViewDrawableState(boolean pressed) {
        if (this.mReviewImageView != null && this.mDeleteBtn != null) {
            int padding = this.mReviewImageView.getPaddingTop();
            this.mReviewImageView.setPaddingRelative(padding, padding, padding, padding);
            this.mDeleteBtn.setPressed(pressed);
            if (pressed) {
                this.mReviewImageView.setPaddingRelative(padding, padding, padding, padding);
            }
            resetQuickViewQuickClipDrawableState(false);
        }
    }

    public void resetQuickViewQuickClipDrawableState(boolean pressed) {
        if (this.mQuickClipBtn != null && this.mQuickClipView != null) {
            float alpha = pressed ? 0.5f : 1.0f;
            this.mQuickClipBtn.setAlpha(alpha);
            this.mQuickClipView.setAlpha(alpha);
            this.mQuickClipView.setPressed(pressed);
        }
    }

    public void updateThumbnailDragShadowPostion(int x, int y) {
        if (this.mBaseView != null && this.mThumbBtn != null && this.mGet.getActivity() != null) {
            RelativeLayout thumbnailDragShadowParent = (RelativeLayout) this.mBaseView.findViewById(C0088R.id.thumbnail_drag_shadow_layout);
            LayoutParams dragShadowLP = (LayoutParams) thumbnailDragShadowParent.getLayoutParams();
            int[] thumbnailViewPosition = new int[2];
            this.mThumbBtn.getLocationOnScreen(thumbnailViewPosition);
            DisplayMetrics outMetrics = Utils.getWindowRealMatics(getActivity());
            int lcdWidth = outMetrics.widthPixels;
            int lcdHeight = outMetrics.heightPixels;
            x = (thumbnailViewPosition[0] + x) - (this.mThumbBtn.getWidth() / 2);
            y = (thumbnailViewPosition[1] + y) - (this.mThumbBtn.getHeight() / 2);
            if (x < 0) {
                x = 0;
            } else if (x > lcdWidth) {
                x = lcdWidth - this.mThumbBtn.getWidth();
            }
            if (y < 0) {
                y = 0;
            } else if (y > lcdHeight) {
                y = lcdHeight - this.mThumbBtn.getHeight();
            }
            dragShadowLP.setMarginStart(x);
            dragShadowLP.topMargin = y;
            thumbnailDragShadowParent.setLayoutParams(dragShadowLP);
        }
    }

    public void setListeners() {
        if (this.mThumbBtn != null) {
            this.mThumbBtn.setOnClickListener(this);
            this.mThumbBtn.setOnLongClickListener(this);
            this.mThumbBtn.setOnTouchListener(this);
            if (ModelProperties.isKeyPadSupported(getAppContext())) {
                this.mThumbBtnBg = (ImageView) this.mGet.findViewById(C0088R.id.thumbnail_bg);
                this.mThumbBtn.setFocusable(true);
                this.mThumbBtn.setOnFocusChangeListener(new C09642());
            }
        }
    }

    public void doAfterCaptureProcess(final Uri uri, boolean useExtraInfo) {
        CamLog.m3d(CameraConstants.TAG, "doAfterCaptureProcess uri = " + uri);
        if (!(this.mThumbnailLayout == null || this.mThumbnailLayout.getVisibility() != 0 || this.mThumbBtn.isThumbnailExtracting() || this.mGet.isModuleChanging())) {
            this.mGet.setReviewThumbBmp(null);
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (!ReviewThumbnailManager.this.mGet.isRearCamera()) {
                    ReviewThumbnailManager.this.doAfterCaptureForQuickView(uri);
                }
                ReviewThumbnailManager.this.setEnabled(true);
            }
        }, 0);
    }

    protected void doAfterCaptureForQuickView(Uri uri) {
    }

    protected void setQuickViewLayout(final boolean isAutoReview, Bitmap thumbBmp, Bitmap dragShadowBmp) {
        if (this.mGet.checkModuleValidate(1) && this.mReviewImageView != null && this.mThumbBtnDragShadow != null && this.mThumbBtn != null && this.mDeleteBtnView != null) {
            CamLog.m3d(CameraConstants.TAG, "setQuickViewLayout isAutoReview: " + isAutoReview);
            this.mReviewImageLayout.setVisibility(4);
            this.mReviewLayout.setVisibility(4);
            ViewUtil.clearImageViewDrawableOnly(this.mReviewImageView);
            setImageViewSize(thumbBmp.getWidth(), thumbBmp.getHeight());
            if (!(thumbBmp == null || thumbBmp.isRecycled())) {
                this.mReviewImageView.setImageBitmap(thumbBmp);
            }
            if (!(isAutoReview || dragShadowBmp == null || dragShadowBmp.isRecycled())) {
                this.mThumbBtnDragShadow.setImageBitmap(dragShadowBmp);
                ViewGroup.LayoutParams lp = this.mThumbBtnDragShadow.getLayoutParams();
                lp.width = dragShadowBmp.getWidth();
                lp.height = dragShadowBmp.getHeight();
                this.mThumbBtnDragShadow.setLayoutParams(lp);
            }
            if (isAutoReview) {
                this.mDeleteBtnView.setVisibility(8);
                this.mThumbBtn.setVisibility(8);
                this.mThumbBtnDragShadow.setVisibility(8);
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    ReviewThumbnailManager.this.mGalleryWindowAniState = 1;
                    if (ReviewThumbnailManager.this.mGet.isIntervalShotProgress()) {
                        CamLog.m3d(CameraConstants.TAG, "[QuickView] during interval shot. set mGalleryWindowAniState : ANI_NONE");
                        ReviewThumbnailManager.this.mGalleryWindowAniState = 0;
                        return;
                    }
                    CamLog.m3d(CameraConstants.TAG, "[QuickView] start GalleryQuick View and set mGalleryWindowAniState to : " + ReviewThumbnailManager.this.mGalleryWindowAniState);
                    ReviewThumbnailManager.this.showGalleryQuickViewAnimation(true, false, false);
                    ReviewThumbnailManager.this.mGet.showTilePreview(false);
                    if (ReviewThumbnailManager.this.mListener != null) {
                        ReviewThumbnailManager.this.mListener.onQuickViewShow(isAutoReview);
                    }
                }
            }, 0);
        }
    }

    public void setRecentImage(final boolean isAutoReview) {
        this.mImageMakeThread = new Thread(new Runnable() {
            public void run() {
                Uri uri;
                synchronized (ReviewThumbnailManager.this.mUriLock) {
                    uri = ReviewThumbnailManager.this.getUri();
                    CamLog.m3d(CameraConstants.TAG, "setRecentImage uri = " + uri);
                }
                if (ReviewThumbnailManager.this.mGet.checkModuleValidate(1) && uri != null) {
                    Bitmap reviewBmp;
                    CamLog.m3d(CameraConstants.TAG, "setRecentImage thread start");
                    String filePath = FileUtil.getRealPathFromURI(ReviewThumbnailManager.this.mGet.getActivity(), uri);
                    String mediaType = ReviewThumbnailManager.this.mGet.getAppContext().getContentResolver().getType(uri);
                    boolean isImageType = mediaType != null && mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE);
                    int orientationDegree = Utils.isConfigureLandscape(ReviewThumbnailManager.this.mGet.getAppContext().getResources()) ? 90 : 0;
                    ExifInterface exif = Exif.readExif(filePath);
                    int exifDegree = isImageType ? Exif.getOrientation(exif) : 0;
                    Bitmap thumbBmp = ReviewThumbnailManager.this.mGet.getReviewThumbBmp();
                    if (thumbBmp == null || thumbBmp.isRecycled()) {
                        ReviewThumbnailManager.this.mGet.setReviewThumbBmp(null);
                        reviewBmp = BitmapManagingUtil.getThumbnailFromUri(ReviewThumbnailManager.this.getActivity(), uri, isImageType ? 1 : 1);
                        if (reviewBmp == null) {
                            CamLog.m3d(CameraConstants.TAG, "reviewBmp is null.");
                            return;
                        }
                        reviewBmp = BitmapManagingUtil.getRotatedImage(reviewBmp, exifDegree, false);
                    } else {
                        reviewBmp = thumbBmp.copy(isImageType ? Config.RGB_565 : Config.ARGB_8888, true);
                    }
                    ReviewThumbnailManager.this.updateRecentImage(reviewBmp, isImageType, isAutoReview);
                    if (isImageType) {
                        ReviewThumbnailManager.this.updateJpegImageforQuickview(exif, orientationDegree, uri, exifDegree);
                    }
                }
            }
        });
        this.mImageMakeThread.start();
    }

    private void updateRecentImage(Bitmap reviewBmp, boolean isImageType, boolean isAutoReview) {
        final Bitmap resizeThumbBmp = reviewBmp.copy(isImageType ? Config.RGB_565 : Config.ARGB_8888, true);
        final Bitmap roundBmp = makeRoundBmp(reviewBmp);
        final int width = reviewBmp.getWidth();
        final int height = reviewBmp.getHeight();
        if (!Thread.interrupted()) {
            final boolean z = isAutoReview;
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (!Thread.interrupted()) {
                        ReviewThumbnailManager.this.setImageViewSize(width, height);
                        ReviewThumbnailManager.this.setQuickViewLayout(z, resizeThumbBmp, roundBmp);
                    }
                }
            });
        }
    }

    private Bitmap makeRoundBmp(Bitmap reviewBmp) {
        int size = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.review_thumbnail.size);
        return BitmapManagingUtil.getRoundedImage(reviewBmp, size, size, (int) (((float) size) * 0.95f));
    }

    private int[] getImageSizeInFile(Uri uri) {
        String filePath = FileUtil.getRealPathFromURI(this.mGet.getActivity(), uri);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        CamLog.m3d(CameraConstants.TAG, "getImageSizeInFile : width = " + options.outWidth + ", height = " + options.outHeight);
        return new int[]{options.outWidth, options.outHeight};
    }

    private void updateJpegImageforQuickview(ExifInterface exif, int orientationDegree, Uri uri, int exifDegree) {
        CamLog.m3d(CameraConstants.TAG, "setRecentImage - jpeg decoding start");
        int[] actualSize = Exif.getExifSize(exif);
        if (actualSize[0] == 0 || actualSize[1] == 0) {
            actualSize = Exif.getImageSize(exif);
        }
        if (actualSize[0] == 0 || actualSize[1] == 0) {
            actualSize = getImageSizeInFile(uri);
        }
        int[] dstSize = BitmapManagingUtil.getFitSizeOfBitmapForLCD(getActivity(), actualSize[0], actualSize[1], orientationDegree);
        final Bitmap toSetBmp = BitmapManagingUtil.loadScaledandRotatedBitmap(this.mGet.getAppContext().getContentResolver(), uri.toString(), dstSize[0], dstSize[1], exifDegree);
        if (this.mGet.checkModuleValidate(1) && !Thread.interrupted()) {
            CamLog.m3d(CameraConstants.TAG, "setRecentImage - jpeg decoding end");
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (ReviewThumbnailManager.this.mGet.checkModuleValidate(1) && ReviewThumbnailManager.this.mReviewImageView != null && !Thread.interrupted() && toSetBmp != null && !toSetBmp.isRecycled()) {
                        ViewUtil.clearImageViewDrawableOnly(ReviewThumbnailManager.this.mReviewImageView);
                        ReviewThumbnailManager.this.mReviewImageView.setImageBitmap(toSetBmp);
                    }
                }
            });
        }
    }

    protected void setImageViewSize(int imageWidth, int imageHeight) {
        int degree;
        if (this.mDegree == -1) {
            this.mDegree = getOrientationDegree();
        }
        if (imageWidth > imageHeight) {
            degree = (this.mDegree + 90) % 360;
        } else {
            degree = this.mDegree;
        }
        int[] photoSize = BitmapManagingUtil.getFitSizeOfBitmapForLCD(getActivity(), imageWidth, imageHeight, degree);
        int width = photoSize[0];
        int height = photoSize[1];
        if (this.mReviewImageView != null) {
            this.mReviewImageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        }
    }

    public void updateThumbnail(boolean useAni) {
        updateThumbnail(null, false, false, useAni, false);
    }

    public void updateThumbnail(Uri uri, boolean useSnapShot, boolean useExtraInfo, boolean useAni, boolean isRestore) {
        CamLog.m3d(CameraConstants.TAG, "updateThumbnail START");
        this.mIsNeedThumbnailAnimation = false;
        if (!QuickWindowUtils.isQuickWindowCameraMode() && !this.mGet.isSquareGalleryBtn()) {
            if (this.mUpdateThumbThread != null && this.mUpdateThumbThread.isAlive()) {
                if (uri != null || isRestore) {
                    this.mUpdateThumbThread.interrupt();
                    CamLog.m3d(CameraConstants.TAG, "updateThumbnail interrupt");
                } else {
                    CamLog.m3d(CameraConstants.TAG, "updateThumbnail return");
                    return;
                }
            }
            final Uri uri2 = uri;
            final boolean z = useSnapShot;
            final boolean z2 = useExtraInfo;
            final boolean z3 = useAni;
            this.mUpdateThumbThread = new Thread() {
                public void run() {
                    if (!ReviewThumbnailManager.this.mGet.checkModuleValidate(1) || isInterrupted() || ReviewThumbnailManager.this.mGet.isAttachIntent()) {
                        CamLog.m3d(CameraConstants.TAG, "updateThumbnail interrupted");
                        ReviewThumbnailManager.this.pollRequest();
                        return;
                    }
                    ReviewThumbnailManager.this.doUpdateThumbnailThread(uri2, z, z2, z3);
                }
            };
            this.mUpdateThumbThread.start();
        }
    }

    public void restoreThumbnail(boolean useAni) {
        updateThumbnail(null, false, false, useAni, true);
    }

    private void doUpdateThumbnailThread(Uri uri, boolean useSnapShot, boolean useExtraInfo, boolean useAni) {
        CamLog.m3d(CameraConstants.TAG, "mUpdateThumbThread thread start");
        this.mRecentUri = uri;
        boolean isFileDeleted = uri == null;
        if (this.mRecentUri == null) {
            this.mRecentUri = getUri();
            CamLog.m3d(CameraConstants.TAG, "Recent uri = " + this.mRecentUri);
        }
        if (this.mIsLaunchingGallery) {
            setContentObserver(true, this.mRecentUri);
            this.mIsLaunchingGallery = false;
        }
        this.mBitmap = null;
        this.mFilePath = null;
        int degree = 0;
        UpdateThumbnailRequest request = isFileDeleted ? null : getRequest();
        boolean isBusrtShot = false;
        if (request == null || request.mUri == null || !this.mRecentUri.toString().equals(request.mUri.toString()) || request.mThumbBitmap == null) {
            CamLog.m3d(CameraConstants.TAG, "getThumbnail image start");
            this.mBitmap = BitmapManagingUtil.getThumbnailFromUri(getActivity(), this.mRecentUri, 1);
            CamLog.m3d(CameraConstants.TAG, "getThumbnail image end. bitmap is " + (this.mBitmap == null ? "null." : "not null."));
            this.mFilePath = FileUtil.getRealPathFromURI(this.mGet.getActivity(), this.mRecentUri);
            CamLog.m3d(CameraConstants.TAG, "filePath = " + this.mFilePath);
            if (this.mFilePath == null) {
                retryRecentUri();
            }
            if (!(this.mRecentUri == null || this.mFilePath == null)) {
                ContentResolver resolver = this.mGet.getAppContext().getContentResolver();
                String mediaType = resolver.getType(this.mRecentUri);
                boolean isImageType = mediaType != null && mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE);
                if (isImageType) {
                    degree = new File(this.mFilePath).exists() ? Exif.getOrientation(this.mFilePath) : FileUtil.getOrientationFromDB(resolver, this.mRecentUri);
                }
            }
        } else {
            this.mBitmap = request.mThumbBitmap;
            degree = request.mExifDegree;
            isBusrtShot = request.mIsBurstShot;
        }
        updateThumbnailWithTransition(BitmapManagingUtil.getRotatedImage(this.mBitmap, degree, false), request, useSnapShot, useExtraInfo, useAni, isBusrtShot);
    }

    public void retryRecentUri() {
        this.mRecentUri = getUri();
        CamLog.m3d(CameraConstants.TAG, "Recent uri = " + this.mRecentUri);
        this.mBitmap = BitmapManagingUtil.getThumbnailFromUri(getActivity(), this.mRecentUri, 1);
        CamLog.m3d(CameraConstants.TAG, new StringBuilder().append("getThumbnail image end. bitmap is ").append(this.mBitmap).toString() == null ? "null." : "not null.");
        this.mFilePath = FileUtil.getRealPathFromURI(this.mGet.getActivity(), this.mRecentUri);
        CamLog.m3d(CameraConstants.TAG, "filePath = " + this.mFilePath);
    }

    public void updateThumbnail(final boolean useSnapShot, final boolean useExtraInfo, final boolean useAni) {
        if (!QuickWindowUtils.isQuickWindowCameraMode() && !this.mGet.isSquareGalleryBtn()) {
            if (this.mUpdateThumbThread != null && this.mUpdateThumbThread.isAlive()) {
                this.mUpdateThumbThread.interrupt();
                CamLog.m3d(CameraConstants.TAG, "updateThumbnail interrupt");
            }
            setLaunchGalleryAvailable(false);
            this.mUpdateThumbThread = new Thread() {
                public void run() {
                    if (!ReviewThumbnailManager.this.mGet.checkModuleValidate(1) || isInterrupted() || ReviewThumbnailManager.this.mGet.isAttachIntent()) {
                        CamLog.m3d(CameraConstants.TAG, "updateThumbnail interrupted");
                        ReviewThumbnailManager.this.pollRequest();
                        ReviewThumbnailManager.this.setLaunchGalleryAvailable(true);
                        return;
                    }
                    CamLog.m3d(CameraConstants.TAG, "mUpdateThumbThread thread start");
                    Bitmap bitmap = null;
                    int degree = 0;
                    UpdateThumbnailRequest request = ReviewThumbnailManager.this.getRequest();
                    boolean isBusrtShot = false;
                    if (!(request == null || request.mThumbBitmap == null)) {
                        bitmap = request.mThumbBitmap;
                        degree = request.mExifDegree;
                        isBusrtShot = request.mIsBurstShot;
                    }
                    Bitmap toSetBmp = BitmapManagingUtil.getRotatedImage(bitmap, degree, false);
                    if (!ReviewThumbnailManager.this.mGet.checkModuleValidate(1) || isInterrupted()) {
                        CamLog.m3d(CameraConstants.TAG, "updateThumbnail interrupted");
                        ReviewThumbnailManager.this.setLaunchGalleryAvailable(true);
                        return;
                    }
                    ReviewThumbnailManager.this.updateThumbnailWithTransition(toSetBmp, request, useSnapShot, useExtraInfo, useAni, isBusrtShot);
                }
            };
            if (this.mUpdateThumbThread != null) {
                this.mUpdateThumbThread.start();
            }
        }
    }

    private void updateThumbnailWithTransition(Bitmap toSetBmp, UpdateThumbnailRequest request, boolean useSnapShot, boolean useExtraInfo, boolean useAni, boolean isBusrtShot) {
        if (!QuickWindowUtils.isQuickWindowCameraMode()) {
            int delay = (ModelProperties.isMTKChipset() && this.mGet.getFilmState() == 4) ? 250 : 0;
            long j = (!useAni || isBusrtShot) ? 0 : 50;
            final int transDur = (int) j;
            final Bitmap bitmap = toSetBmp;
            final boolean z = useExtraInfo;
            final boolean z2 = useSnapShot;
            final UpdateThumbnailRequest updateThumbnailRequest = request;
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    boolean isTransitionAni;
                    CamLog.m3d(CameraConstants.TAG, "updateThumbnail handleRun");
                    if (transDur > 0) {
                        isTransitionAni = true;
                    } else {
                        isTransitionAni = false;
                    }
                    if (ReviewThumbnailManager.this.mGet.checkModuleValidate(1) && ReviewThumbnailManager.this.mThumbBtn != null) {
                        boolean enable;
                        if (bitmap != null) {
                            enable = true;
                        } else {
                            enable = false;
                        }
                        if (ReviewThumbnailManager.this.mIsHideMode) {
                            ReviewThumbnailManager.this.setThumbnailVisibility(8, false, true);
                        }
                        ReviewThumbnailManager.this.mThumbBtn.setEnabled(enable);
                        if (ModelProperties.isKeyPadSupported(ReviewThumbnailManager.this.getAppContext())) {
                            ReviewThumbnailManager.this.mThumbBtn.setFocusable(enable);
                            ReviewThumbnailManager.this.mThumbBtn.setClickable(enable);
                        }
                        ReviewThumbnailManager.this.mThumbBtn.setData(bitmap, isTransitionAni, false);
                        CamLog.m3d(CameraConstants.TAG, "thumbnail button updated!");
                        ReviewThumbnailManager.this.mGet.setReviewThumbBmp(bitmap);
                        ReviewThumbnailManager.this.mUpdateThumbThread = null;
                        if (z && ReviewThumbnailManager.this.mListener != null) {
                            ReviewThumbnailManager.this.mListener.onReviewThumbnailUpdated(z);
                        }
                        if (z2) {
                            ReviewThumbnailManager.this.updateSnapShotThumbnail(true);
                        }
                        if (updateThumbnailRequest != null) {
                            updateThumbnailRequest.unbind();
                        }
                        CamLog.m3d(CameraConstants.TAG, "updateThumbnail END");
                        DebugUtil.setEndTime("[+] MV : updateThumbnail");
                    }
                }
            }, (long) delay);
        }
    }

    protected void deleteImageAndUpdateThumbnail() {
        setContentObserver(false, null);
        this.mDeleteThumbThread = new C096011();
        this.mDeleteThumbThread.start();
    }

    public boolean isQuickViewAniStarted() {
        return this.mGalleryWindowAniState > 0;
    }

    protected void launchQuickClipSharedApp(final boolean isQuickView) {
        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_QUICKCLIP_SHAREBYQUICKVIEW);
        if (QuickClipUtil.isFakeMode() && QuickClipUtil.sIsEnabled) {
            new QuickClipPopUpDialog((CamDialogInterface) this.mGet, new QuickClipPopupInterface() {
                public void isUpload(boolean isUpload) {
                    QuickClipUtil.launchSharedActivity(ReviewThumbnailManager.this.mGet.getActivity(), ReviewThumbnailManager.this.getUri(), ReviewThumbnailManager.this.mQuickClipSharedItem, isUpload);
                    if (!isUpload && isQuickView) {
                        ReviewThumbnailManager.this.showGalleryQuickViewAnimation(false, false, false);
                    }
                }

                public void resetClickedFlag() {
                }
            }).create();
        } else {
            QuickClipUtil.launchSharedActivity(this.mGet.getActivity(), getUri(), this.mQuickClipSharedItem);
        }
    }
}
