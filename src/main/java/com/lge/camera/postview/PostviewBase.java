package com.lge.camera.postview;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SystemBarUtil;
import java.io.File;
import java.util.ArrayList;

public abstract class PostviewBase extends PostviewInterfaceImpl {
    protected static final int DISPLAY_DURATION = 300;
    protected Bitmap mCapturedBitmap = null;
    protected boolean mIsFromCreateProcess = false;
    protected boolean mLoadCompleted = false;
    protected PostviewParameters mParam = new PostviewParameters();
    ImageView mPostview = null;
    private int mPreviewHeight = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    private int mPreviewWidth = 1920;

    /* renamed from: com.lge.camera.postview.PostviewBase$1 */
    class C13711 implements OnTouchListener {
        C13711() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return PostviewBase.this.onTouchEvent(event);
        }
    }

    /* renamed from: com.lge.camera.postview.PostviewBase$2 */
    class C13722 implements OnClickListener {
        C13722() {
        }

        public void onClick(View v) {
        }
    }

    public PostviewBase(PostviewBridge postviewBridge) {
        super(postviewBridge);
    }

    public PostviewParameters getPostviewParams() {
        return this.mParam;
    }

    public void executePostview(PostviewParameters params) {
        this.mParam = params;
        init();
        onResumeBefore();
        onResumeAfter();
    }

    public void releasePostview() {
        this.mGet.releasePostview();
        SystemBarUtil.setActionBarAnim(getActivity(), false);
        SystemBarUtil.setActionBarVisible(this.mGet.getActivity(), false);
        IntentBroadcastUtil.sendBroadcastIntentCameraStarted(getActivity());
        onPauseBefore();
        onPauseAfter();
        onStop();
        onDestroy();
        releaseLayout();
        this.mGet.releasePostviewAfter(0);
    }

    public void init() {
        setupPostviewActionBar();
        setupLayout();
        setNaviBarMargin();
        setTouchEventOnPostView();
    }

    public void getPreviewSize(int previewWidth, int previewHeight) {
        CamLog.m3d(CameraConstants.TAG, "[postview] previewHeight : " + previewHeight + ", previewWidth : " + previewWidth);
        this.mPreviewWidth = previewWidth;
        this.mPreviewHeight = previewHeight;
    }

    public void onResumeAfter() {
        if (checkValidateImage()) {
            postviewShow();
        } else {
            releasePostview();
        }
    }

    public void onDestroy() {
        releaseCaptureImage();
        if (this.mCapturedBitmap != null) {
            if (!this.mCapturedBitmap.isRecycled()) {
                this.mCapturedBitmap.recycle();
            }
            this.mCapturedBitmap = null;
        }
        if (this.mParam != null) {
            this.mParam.clearUriList();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        setNaviBarMargin();
    }

    protected void setNaviBarMargin() {
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                releasePostview();
                return true;
            default:
                return false;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                releasePostview();
                return true;
            case 24:
            case 25:
            case 82:
                return true;
            default:
                return false;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 24:
            case 25:
                releasePostview();
                return true;
            case 82:
                return true;
            default:
                return false;
        }
    }

    public void setTouchEventOnPostView() {
        View postviewBase = getBaseLayout();
        if (postviewBase != null) {
            postviewBase.setOnTouchListener(new C13711());
        }
    }

    public void setupActionBar(boolean homeAsUp, boolean showTitle) {
        ActionBar actionBar = this.mGet.getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(homeAsUp);
            actionBar.setDisplayShowTitleEnabled(showTitle);
            actionBar.show();
        }
    }

    public void setupLayout() {
        View view = getRootView();
        if (view == null) {
            this.mGet.inflateStub(C0088R.id.stub_post_view);
            view = getRootView();
        }
        if (view != null) {
            view.setVisibility(0);
            view.setOnClickListener(new C13722());
        }
    }

    public void releaseLayout() {
        View view = getRootView();
        if (view != null) {
            view.setVisibility(8);
        }
    }

    public void reloadedPostview() {
        if (this.mCapturedBitmap != null && !this.mCapturedBitmap.isRecycled()) {
            this.mPostview = (ImageView) this.mGet.findViewById(C0088R.id.captured_image);
            View view = getRootView();
            if (view != null) {
                LayoutParams RootViewParam = (LayoutParams) view.getLayoutParams();
                CamLog.m3d(CameraConstants.TAG, "[postview] mPreviewHeight : " + this.mPreviewHeight + ", mPreviewWidth : " + this.mPreviewWidth + ", calculate : " + (((float) this.mPreviewHeight) / ((float) this.mPreviewWidth)));
                if (ModelProperties.getLCDType() == 2) {
                    RootViewParam.topMargin = RatioCalcUtil.getNotchDisplayHeight(this.mGet.getAppContext());
                }
            }
            if (this.mPostview != null) {
                this.mPostview.setImageBitmap(this.mCapturedBitmap);
            }
        }
    }

    protected View getBaseLayout() {
        return this.mGet.findViewById(C0088R.id.postview_contents_layout);
    }

    protected View getRootView() {
        return this.mGet.findViewById(C0088R.id.postview_layout);
    }

    protected void setBaseLayoutVisibility(int visible) {
        View contentView = getBaseLayout();
        if (contentView != null) {
            contentView.setVisibility(visible);
        }
    }

    protected synchronized boolean loadSingleCapturedImages() {
        boolean z;
        CamLog.m3d(CameraConstants.TAG, "loadSingleCapturedImages");
        ArrayList<Uri> uriList = this.mParam.getUriList();
        if (uriList == null || uriList.size() == 0) {
            CamLog.m5e(CameraConstants.TAG, "mUriList.size() is 0 !!");
            z = false;
        } else {
            this.mPostview = (ImageView) this.mGet.findViewById(C0088R.id.captured_image);
            if (this.mCapturedBitmap != null) {
                reloadedPostview();
                z = true;
            } else {
                try {
                    Uri capturedImageUri = (Uri) uriList.get(0);
                    if (this.mCapturedBitmap != null) {
                        this.mCapturedBitmap.recycle();
                        this.mCapturedBitmap = null;
                    }
                    this.mCapturedBitmap = loadCapturedImage(capturedImageUri, 0, true);
                    if (this.mCapturedBitmap == null) {
                        z = false;
                    } else {
                        reloadedPostview();
                        if (this.mParam.getContentType() == 0) {
                            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                                public void handleRun() {
                                    if (PostviewBase.this.mParam != null && PostviewBase.this.mParam.getUriList() != null) {
                                        PostviewBase.this.mCapturedBitmap = PostviewBase.this.loadCapturedImage((Uri) PostviewBase.this.mParam.getUriList().get(0), 0, false);
                                        if (PostviewBase.this.mPostview != null) {
                                            PostviewBase.this.reloadedPostview();
                                        } else {
                                            CamLog.m3d(CameraConstants.TAG, "postview layout null");
                                        }
                                    }
                                }
                            }, 0);
                        }
                        z = true;
                    }
                } catch (Exception e) {
                    CamLog.m6e(CameraConstants.TAG, "Exception!", e);
                }
            }
        }
        return z;
    }

    protected Bitmap loadCapturedImage(Uri uri, int degrees, boolean useThumb) {
        CamLog.m3d(CameraConstants.TAG, String.format("Load captured image:%s, degrees:%d, useThumb:%s", new Object[]{uri, Integer.valueOf(degrees), Boolean.valueOf(useThumb)}));
        Bitmap bmp = null;
        if (this.mParam.getContentType() == 0) {
            String filePath = FileUtil.getRealPathFromURI(getActivity(), uri);
            if (filePath == null) {
                CamLog.m3d(CameraConstants.TAG, "filePath is null");
                return null;
            }
            ExifInterface exif = Exif.readExif(filePath);
            int[] exifSize = Exif.getExifSize(exif);
            int[] dstSize = BitmapManagingUtil.getFitSizeOfBitmapForLCD(getActivity(), exifSize[0], exifSize[1], getOrientationDegree());
            int degree = Exif.getOrientation(exif);
            if (!useThumb) {
                bmp = BitmapManagingUtil.loadScaledandRotatedBitmap(this.mGet.getActivity().getContentResolver(), uri.toString(), dstSize[0], dstSize[1], degree);
            } else if (exif != null) {
                Bitmap bmp_thumb = exif.getThumbnailBitmap();
                if (bmp_thumb == null) {
                    CamLog.m3d(CameraConstants.TAG, "exif.getThumbnailBitmap is null!!!");
                    return null;
                } else if (dstSize[0] < bmp_thumb.getWidth() || dstSize[1] < bmp_thumb.getHeight()) {
                    bmp = BitmapManagingUtil.getRotatedImage(BitmapManagingUtil.resizeBitmapImage(bmp_thumb, dstSize[0], dstSize[1]), degree, false);
                } else {
                    bmp = BitmapManagingUtil.getRotatedImage(bmp_thumb, degree, false);
                }
            }
            if (bmp != null) {
                return bmp;
            }
            CamLog.m5e(CameraConstants.TAG, "LoadBitmap fail!");
            return null;
        }
        long id = -1;
        if (uri != null) {
            id = FileUtil.getIdFromUri(this.mGet.getActivity(), uri);
            CamLog.m3d(CameraConstants.TAG, String.format("GET THUMB start id is %d, and uri is %s", new Object[]{Long.valueOf(id), uri.toString()}));
        }
        if (id == -1) {
            CamLog.m11w(CameraConstants.TAG, String.format("GET THUMB end: uri not valid", new Object[0]));
            return null;
        }
        bmp = Thumbnails.getThumbnail(this.mGet.getActivity().getContentResolver(), id, Thread.currentThread().getId(), 1, null);
        if (bmp == null) {
            CamLog.m5e(CameraConstants.TAG, "LoadBitmap fail!");
            return null;
        }
        CamLog.m3d(CameraConstants.TAG, String.format("GET VIDEO THUMB end", new Object[0]));
        return BitmapManagingUtil.getRotatedImage(bmp, 0, false);
    }

    protected void releaseCaptureImage() {
        ImageView postview = (ImageView) this.mGet.findViewById(C0088R.id.captured_image);
        if (postview != null) {
            BitmapManagingUtil.clearImageViewDrawable(postview);
        }
    }

    protected boolean checkValidateImage() {
        try {
            ArrayList<Uri> uriList = this.mParam.getUriList();
            if (uriList != null) {
                String tempFilePath = FileUtil.getRealPathFromURI(getActivity(), (Uri) uriList.get(0));
                if (tempFilePath == null) {
                    return false;
                }
                if (!new File(tempFilePath).exists()) {
                    uriList.remove(0);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "Exception!", e);
            return false;
        }
    }

    protected void startPostviewAnimation(final View postView, final boolean show) {
        if (postView == null) {
            if (!show) {
                releasePostview();
            } else {
                return;
            }
        }
        AnimationUtil.startShowingAnimation(postView, show, 300, new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (!show) {
                    PostviewBase.this.releasePostview();
                } else if (postView != null) {
                    postView.setVisibility(0);
                }
            }
        });
    }

    public void releasePostviewAfterAnimation() {
    }

    public void setSecureImageList(Uri uri, boolean add) {
        if (!SecureImageUtil.useSecureLockImage() && !AppControlUtil.checkGalleryEnabledOnGuestMode(this.mGet.getAppContext().getContentResolver())) {
            return;
        }
        if (add) {
            SecureImageUtil.get().addSecureLockImageUri(uri);
        } else {
            SecureImageUtil.get().removeSecureLockUri(uri);
        }
    }

    public void doOkClickInDeleteConfirmDialog() {
    }

    public void doCancelClickInDeleteConfirmDialog() {
    }

    public void setDegree(int degree, boolean animation) {
        ((RotateLayout) this.mGet.findViewById(C0088R.id.postview_rotate_layout)).rotateLayout(degree);
        super.setDegree(degree, animation);
    }

    public void requestPostViewButtonFocus() {
    }
}
