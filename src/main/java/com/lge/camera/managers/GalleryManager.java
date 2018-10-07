package com.lge.camera.managers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SquareUtil;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class GalleryManager extends GalleryManagerBase implements UndoInterface {
    protected HandlerRunnable mGalleryViewFirstPageUpdateRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            try {
                CamLog.m3d(CameraConstants.TAG, "[Cell] mGalleryViewFirstPageUpdateRunnable " + GalleryManager.this.mIsCanceledForNewItem);
                if (GalleryManager.this.mIsCanceledForNewItem) {
                    GalleryManager.this.mIsCanceledForNewItem = false;
                } else if (GalleryManager.this.mCaptureDataQueue != null && GalleryManager.this.mGalleryItemList != null && GalleryManager.this.mListener != null) {
                    GalleryManager.this.showMainAccesView(false, false);
                    int removeIndex = 0;
                    if (GalleryManager.this.mGalleryItemList.size() != 0 && ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(0)).mType == -1) {
                        GalleryManager.this.mGalleryItemList.remove(0);
                        GalleryManager.this.mGalleryViewPager.setPageMarginDrawable((int) C0088R.color.black);
                    }
                    CamLog.m7i(CameraConstants.TAG, "[Cell] mCaptureDataQueue.size() : " + GalleryManager.this.mCaptureDataQueue.size());
                    while (!GalleryManager.this.mCaptureDataQueue.isEmpty()) {
                        removeIndex++;
                        GalleryManager.this.mGalleryItemList.add(0, GalleryManager.this.mCaptureDataQueue.take());
                    }
                    if (removeIndex < GalleryManager.this.mGalleryItemList.size()) {
                        Bitmap removeBmp = ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(removeIndex)).mThumbBitmap;
                        if (removeBmp != null) {
                            removeBmp.recycle();
                        }
                        ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(removeIndex)).mThumbBitmap = null;
                    }
                    GalleryManager.this.mPreviousIndex = GalleryManager.this.mCurrentIndex;
                    GalleryManager.this.mCurrentIndex = 0;
                    GalleryManager.this.mListener.onGalleryBitmapLoaded(((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(0)).mType != 1);
                    GalleryManager.this.mGalleryPagerAdapter.notifyDataSetChanged();
                    GalleryManager.this.mGalleryViewPager.setCurrentItem(0, false);
                    GalleryManager.this.mIsNewItemAdding = false;
                    if (!(GalleryManager.this.mThreadHandler == null || ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(0)).mType == 1)) {
                        Message msg = new Message();
                        msg.what = 1;
                        msg.arg1 = 0;
                        msg.arg2 = GalleryManager.this.mGalleryItemList.size();
                        GalleryManager.this.mThreadHandler.removeMessages(1);
                        GalleryManager.this.mThreadHandler.sendMessage(msg);
                    }
                    GalleryManager.this.mGet.deleteImmediatelyNotUndo();
                    if (GalleryManager.this.mVideoPlayerTextureView != null && GalleryManager.this.mVideoPlayerTextureView.getVisibility() == 0) {
                        GalleryManager.this.mVideoPlayerTextureView.setVisibility(4);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    protected boolean mIsCanceledForNewItem = false;

    /* renamed from: com.lge.camera.managers.GalleryManager$1 */
    class C09191 implements OnTouchListener {
        C09191() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            if (GalleryManager.this.mListener != null) {
                if (arg1.getAction() == 0) {
                    GalleryManager.this.mListener.onGalleryViewTouched(true);
                } else if (arg1.getAction() == 1) {
                    GalleryManager.this.mListener.onGalleryViewTouched(false);
                }
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManager$4 */
    class C09234 implements OnTouchListener {
        C09234() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManager$7 */
    class C09267 implements AnimationListener {
        C09267() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            if (GalleryManager.this.mGalleryItemList != null) {
                if (!(GalleryManager.this.mThreadHandler == null || ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(GalleryManager.this.mDeleteIndex)).mType == 1)) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.arg1 = GalleryManager.this.mDeleteIndex;
                    msg.arg2 = GalleryManager.this.mGalleryItemList.size();
                    GalleryManager.this.mThreadHandler.removeMessages(1);
                    GalleryManager.this.mThreadHandler.sendMessage(msg);
                }
                if (GalleryManager.this.mListener != null) {
                    GalleryManager.this.mListener.onGalleryPageChanged(((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(GalleryManager.this.mDeleteIndex)).mUri, false, false);
                }
                GalleryManager.this.showGalleryControlUI(true, ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(GalleryManager.this.mDeleteIndex)).mType, false);
            }
            GalleryManager.this.removeAnimationViews();
            GalleryManager.this.mIsUndoing = false;
        }
    }

    public GalleryManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setGalleryManagerInterface(GalleryManagerInterface listener) {
        this.mListener = listener;
    }

    public void initLayout() {
        if (this.mGalleryLayout == null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] GalleryManager - init");
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
            this.mGalleryLayout = (FrameLayout) this.mGet.inflateView(C0088R.layout.snap_gallery_layout);
            if (vg != null && this.mGalleryLayout != null) {
                View preview = this.mGet.findViewById(C0088R.id.preview_layout);
                int previewIndex = 1;
                if (preview != null) {
                    previewIndex = ((RelativeLayout) preview.getParent()).indexOfChild(preview);
                }
                vg.addView(this.mGalleryLayout, previewIndex + 3);
                int height = Utils.getLCDsize(getAppContext(), true)[1];
                LayoutParams rlp = (LayoutParams) this.mGalleryLayout.getLayoutParams();
                rlp.height = height;
                rlp.width = height;
                rlp.addRule(12);
                this.mGalleryRotateLayout = (RotateLayout) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_rotate_layout);
                this.mMainAccessView = (RotateLayout) this.mGalleryLayout.findViewById(C0088R.id.snap_galley_main_access_rotate_view);
                this.mMainAccessView.setOnTouchListener(new C09191());
                this.mMainAccessViewImageCue = (RotateImageView) this.mMainAccessView.findViewById(C0088R.id.snap_gallery_main_access_cue);
                setupGalleryViewPager();
                setupGalleryPlayerTextureView(height);
                setupGalleryExtraViews(height);
                setupGalleryControlButtons(height);
                if (!CameraConstants.MODE_SQUARE_OVERLAP.equals(this.mGet.getShotMode())) {
                    setRotateDegree(getOrientationDegree(), false);
                    this.mGalleryLayout.setVisibility(0);
                }
                setSquareSnapshotState(0);
                this.mCaptureDataQueue = new LinkedBlockingQueue(10);
                this.mPreviousIndex = this.mCurrentIndex;
                this.mCurrentIndex = 0;
                this.mIsDelAnimDirectionRtoL = true;
                if (SecureImageUtil.isSecureCamera()) {
                    getRecentSnapshotUriListForSecure();
                } else {
                    getRecentSnapshotUriList(true, false);
                }
            }
        }
    }

    public void showMainAccessViewForInitState() {
        if (this.mGalleryItemList != null) {
            SquareSnapGalleryItem item = getCurrentItem();
            boolean showGuide = false;
            boolean showCue = false;
            if (item == null || item.mType == -1) {
                showGuide = true;
            }
            if (this.mGalleryItemList.size() == 0) {
                showCue = false;
            } else if (this.mGalleryItemList.size() == 1 && ((SquareSnapGalleryItem) this.mGalleryItemList.get(0)).mType == -1) {
                showCue = false;
            } else if (((SquareSnapGalleryItem) this.mGalleryItemList.get(0)).mType == -1) {
                showCue = true;
            }
            showMainAccesView(showGuide, showCue);
        }
    }

    public void init() {
        super.init();
        showMainAccessViewForInitState();
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        this.mIsDeleting = false;
        this.mIsNewItemAdding = false;
        showGalleryView(true);
        showTouchBlockCoverView(false);
        if (!(CameraConstants.MODE_SQUARE_OVERLAP.equals(this.mGet.getShotMode()) || this.mVideoPlayerTextureView.getVisibility() == 0 || this.mVideoPlayerTextureView.isAvailable())) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] Video player texture view visible");
            this.mVideoPlayerTextureView.setVisibility(0);
        }
        if (this.mImageHandlerThread == null) {
            this.mImageHandlerThread = new HandlerThread("ImageHandlerThread");
            this.mImageHandlerThread.start();
        }
        if (this.mThreadHandler == null) {
            this.mThreadHandler = new Handler(this.mImageHandlerThread.getLooper()) {
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        final int curPosition = msg.arg1;
                        final int itemListSize = msg.arg2;
                        final Uri uri = msg.obj;
                        CamLog.m7i(CameraConstants.TAG, "[Cell] SQUARE_MSG_ORIGINAL_IMAGE_MAKING - " + curPosition + ", itemListSize : " + itemListSize);
                        if (GalleryManager.this.mGalleryItemList != null && GalleryManager.this.mGalleryItemList.size() != 0 && curPosition < GalleryManager.this.mGalleryItemList.size() && GalleryManager.this.mGalleryItemList.size() == itemListSize) {
                            final Bitmap bmp = GalleryManager.this.getBitmap((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(curPosition), curPosition);
                            if (bmp == null) {
                                CamLog.m7i(CameraConstants.TAG, "[Cell] SQUARE_MSG_ORIGINAL_IMAGE_MAKING - return");
                            } else if (GalleryManager.this.mThreadHandler == null || !GalleryManager.this.mThreadHandler.hasMessages(1)) {
                                GalleryManager.this.mGet.postOnUiThread(new HandlerRunnable(GalleryManager.this) {
                                    public void handleRun() {
                                        if (GalleryManager.this.mGalleryPagerAdapter != null && curPosition == GalleryManager.this.mCurrentIndex && GalleryManager.this.mGalleryItemList != null && GalleryManager.this.mGalleryItemList.size() != 0 && GalleryManager.this.mGalleryItemList.size() == itemListSize) {
                                            CamLog.m3d(CameraConstants.TAG, "[Cell] SQUARE_MSG_ORIGINAL_IMAGE_MAKING - handle run");
                                            if (uri == null || uri == ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(curPosition)).mUri) {
                                                ImageView view = GalleryManager.this.mGalleryPagerAdapter.getSpecficView(curPosition);
                                                if (view != null) {
                                                    BitmapManagingUtil.clearImageViewDrawable(view);
                                                    view.setVisibility(0);
                                                    view.setImageBitmap(bmp);
                                                }
                                                if (((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(curPosition)).mBitmapState == 0) {
                                                    ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(curPosition)).mBitmapState = 1;
                                                } else if (((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(curPosition)).mBitmapState == 1) {
                                                    ((SquareSnapGalleryItem) GalleryManager.this.mGalleryItemList.get(curPosition)).mBitmapState = 2;
                                                }
                                                GalleryManager.this.setPreviousViewAsThumbnailBitmap(curPosition, view);
                                                return;
                                            }
                                            CamLog.m3d(CameraConstants.TAG, "[Cell] SQUARE_MSG_ORIGINAL_IMAGE_MAKING different uri " + uri.toString());
                                        } else if (bmp != null && !bmp.isRecycled()) {
                                            bmp.recycle();
                                        }
                                    }
                                }, 0);
                            } else if (bmp != null && !bmp.isRecycled()) {
                                bmp.recycle();
                            }
                        }
                    }
                }
            };
        }
        if (this.mGet.isSquareGalleryBtn() && this.mThreadHandler != null && this.mGalleryItemList != null && this.mGalleryItemList.size() > 0 && ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mType != 1) {
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = this.mCurrentIndex;
            msg.arg2 = this.mGalleryItemList.size();
            msg.obj = ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mUri;
            this.mThreadHandler.removeMessages(1);
            this.mThreadHandler.sendMessage(msg);
        }
    }

    protected Bitmap getBitmap(SquareSnapGalleryItem item, int curPosition) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] getBitmap - start");
        if (item == null || item.mUri == null) {
            return null;
        }
        Uri uri = item.mUri;
        int bitmapState = item.mBitmapState;
        int height = SquareUtil.getHeight(getAppContext());
        if (uri.toString().contains(OverlapProjectDbAdapter.URI_OVERLAP)) {
            return SquareUtil.getOverlapSampleBitmap(this.mGet.getAppContext(), uri, height, height);
        }
        String filePath = FileUtil.getRealPathFromURI(getAppContext(), uri);
        if (filePath == null) {
            return null;
        }
        Bitmap reviewBmp;
        ContentResolver resolver = getAppContext().getContentResolver();
        String mediaType = resolver.getType(uri);
        boolean isImageType = mediaType != null && mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE);
        if (isImageType) {
            String strURI = uri.toString();
            Bitmap resizeBmp = null;
            if (strURI != null) {
                uri = Uri.parse(strURI);
                if (uri != null) {
                    String strScheme = uri.getScheme();
                    if (!(strScheme == null || strScheme.compareToIgnoreCase("file") == 0)) {
                        Bitmap bitmap = loadBitmapUsingFileDescriptor(resolver, uri);
                        if (bitmap == null) {
                            return null;
                        }
                        if (curPosition != this.mCurrentIndex || (this.mThreadHandler != null && this.mThreadHandler.hasMessages(1))) {
                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                            return null;
                        }
                        ExifInterface exif = Exif.readExif(filePath);
                        int[] actualSize = Exif.getExifSize(exif);
                        if (actualSize[0] == 0 || bitmapState == 0) {
                            actualSize[0] = height;
                            actualSize[1] = height;
                        }
                        CamLog.m3d(CameraConstants.TAG, "[Cell] actual size : " + actualSize[0] + "x" + actualSize[1]);
                        resizeBmp = loadRotatedBitmap(actualSize, isImageType ? Exif.getOrientation(exif) : 0, bitmap);
                    }
                    reviewBmp = resizeBmp;
                }
            }
            return null;
        }
        reviewBmp = BitmapManagingUtil.getThumbnailFromUri(getActivity(), uri, 1);
        CamLog.m3d(CameraConstants.TAG, "[Cell] getBitmap - end");
        return reviewBmp;
    }

    private Bitmap loadBitmapUsingFileDescriptor(ContentResolver cr, Uri uri) {
        Bitmap bitmap = null;
        ParcelFileDescriptor pfd = null;
        try {
            pfd = cr.openFileDescriptor(uri, LdbConstants.LDB_CAMERA_ID_REAR_NORMAL);
            if (pfd == null) {
                CamLog.m11w(CameraConstants.TAG, "File description is null.");
                if (pfd == null) {
                    return null;
                }
                try {
                    pfd.close();
                    return null;
                } catch (IOException ex) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex);
                    return null;
                }
            }
            FileDescriptor fd = pfd.getFileDescriptor();
            Options opts = new Options();
            opts.inDither = true;
            opts.inSampleSize = 1;
            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, opts);
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ex2) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex2);
                }
            }
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() FileNotFoundException! ");
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ex22) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex22);
                }
            }
        } catch (Throwable th) {
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ex222) {
                    CamLog.m5e(CameraConstants.TAG, "loadBitmapFromFile() IOException! " + ex222);
                }
            }
        }
    }

    private Bitmap loadRotatedBitmap(int[] actualSize, int exifDegree, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        if (!(bitmap.getWidth() == actualSize[0] && bitmap.getHeight() == actualSize[0])) {
            float s1;
            float s1x = ((float) actualSize[0]) / ((float) bitmap.getWidth());
            float s1y = ((float) actualSize[0]) / ((float) bitmap.getHeight());
            if (s1x < s1y) {
                s1 = s1x;
            } else {
                s1 = s1y;
            }
            matrix.postScale(s1, s1);
        }
        matrix.postRotate((float) exifDegree);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (matrix != null) {
        }
        if (resizeBmp != bitmap) {
            bitmap.recycle();
        }
        return resizeBmp;
    }

    private void setPreviousViewAsThumbnailBitmap(int curPosition, ImageView curView) {
        if (curPosition != this.mPreviousIndex && this.mPreviousIndex >= 0 && this.mPreviousIndex < this.mGalleryItemList.size()) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] update previous image to thumbnail, curIndex : " + this.mCurrentIndex + ", preIndex : " + this.mPreviousIndex);
            ImageView prevView = this.mGalleryPagerAdapter.getSpecficView(this.mPreviousIndex);
            if (curView == prevView) {
                CamLog.m7i(CameraConstants.TAG, "[Cell] The same view!!, so return");
                return;
            }
            SquareSnapGalleryItem prevItem = (SquareSnapGalleryItem) this.mGalleryItemList.get(this.mPreviousIndex);
            if (prevView != null && prevItem != null) {
                BitmapManagingUtil.clearImageViewDrawable(prevView);
                prevView.setVisibility(0);
                prevView.setImageBitmap(getThumbnailBitmap(prevItem.mUri, prevItem.mType, false));
                prevItem.mBitmapState = 0;
            }
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mMediaPlayer != null) {
            if (this.mMediaPlayer.isPlaying() || this.mSquareSnapState == 3) {
                stopVideoPlay();
                showGalleryControlUI(true, 1, false);
            }
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
        this.mGet.removePostRunnable(this.mGalleryViewFirstPageUpdateRunnable);
        cancelOriginalImageMaking();
        if (this.mThreadHandler != null) {
            this.mThreadHandler.removeCallbacksAndMessages(null);
            this.mThreadHandler.getLooper().quit();
            this.mThreadHandler = null;
        }
        if (this.mImageHandlerThread != null) {
            this.mImageHandlerThread = null;
        }
    }

    protected void clearGalleryItemList() {
        if (this.mGalleryItemList != null) {
            for (int i = 0; i < this.mGalleryItemList.size(); i++) {
                ((SquareSnapGalleryItem) this.mGalleryItemList.get(i)).unbind();
            }
            this.mGalleryItemList.clear();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        releaseViews();
        clearGalleryItemList();
        this.mGalleryItemList = null;
        if (this.mCaptureDataQueue != null && !this.mCaptureDataQueue.isEmpty()) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] unbindCapturedData, remain data : " + this.mCaptureDataQueue.size());
            for (int i = 0; i < this.mCaptureDataQueue.size(); i++) {
                ((SquareSnapGalleryItem) this.mCaptureDataQueue.peek()).unbind();
            }
            this.mCaptureDataQueue.clear();
            this.mCaptureDataQueue = null;
        }
    }

    public void releaseViews() {
        CamLog.m3d(CameraConstants.TAG, "[Cell] release views");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        if (!(vg == null || this.mGalleryLayout == null)) {
            vg.removeView(this.mGalleryLayout);
        }
        if (this.mGalleryViewPager != null) {
            this.mGalleryViewPager.setOnPageChangeListener(null);
            this.mGalleryViewPager = null;
        }
        ViewUtil.clearImageViewDrawableOnly(this.mSquareSnapshotAnimationFrontView);
        this.mSquareSnapshotAnimationFrontView = null;
        ViewUtil.clearImageViewDrawableOnly(this.mSquareSnapshotAnimationRearView);
        this.mSquareSnapshotAnimationRearView = null;
        if (this.mMainAccessView != null) {
            this.mMainAccessView.setVisibility(8);
            this.mMainAccessView.setOnTouchListener(null);
            this.mMainAccessView = null;
        }
        if (this.mVideoSurfaceTexture != null) {
            this.mVideoSurfaceTexture.release();
            this.mVideoSurfaceTexture = null;
        }
        if (this.mVideoPlayerTextureView != null) {
            this.mVideoPlayerTextureView.setVisibility(8);
            this.mVideoPlayerTextureView = null;
        }
        this.mPlayButton.setOnClickListener(null);
        this.mPlayButton = null;
        this.mMuteButton.setOnClickListener(null);
        this.mMuteButton = null;
        this.mDeleteButton.setOnClickListener(null);
        this.mDeleteButton = null;
        this.mBurstShotMark.setOnClickListener(null);
        this.mBurstShotMark = null;
        this.mControlButtonLayout = null;
        this.mTouchBlockCoverView = null;
        this.mGalleryRotateLayout = null;
        this.mGalleryPlayButtonLayout = null;
        this.mGalleryLayout = null;
        this.mGalleryPagerAdapter = null;
    }

    public void onNewItemAdded(Uri uri, Bitmap bitmap, int type) {
        if (this.mGet.isSquareGalleryBtn()) {
            CamLog.m3d(CameraConstants.TAG, "[cell] onNewItemAdded ");
            if (this.mCaptureDataQueue == null || this.mGet.isModuleChanging()) {
                CamLog.m11w(CameraConstants.TAG, "[cell] onNewItemAdded - return ");
            } else if (this.mGalleryItemList == null || this.mGalleryItemList.size() == 0 || uri == null || !uri.equals(((SquareSnapGalleryItem) this.mGalleryItemList.get(0)).mUri)) {
                this.mIsNewItemAdding = true;
                if (this.mThreadHandler != null) {
                    this.mThreadHandler.removeMessages(1);
                }
                this.mIsCanceledForNewItem = false;
                try {
                    this.mCaptureDataQueue.put(new SquareSnapGalleryItem(uri, bitmap, type));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.mGet.runOnUiThread(this.mGalleryViewFirstPageUpdateRunnable);
            } else {
                CamLog.m11w(CameraConstants.TAG, "[cell] already added, uri : " + uri);
            }
        }
    }

    public void cancleNewItemAdd() {
        this.mIsCanceledForNewItem = true;
        cancelOriginalImageMaking();
    }

    public void cancelOriginalImageMaking() {
        if (this.mThreadHandler != null) {
            CamLog.m3d(CameraConstants.TAG, "[cell]cancelOriginalImageMaking ");
            this.mIsCanceledForNewItem = true;
            this.mThreadHandler.removeMessages(1);
        }
    }

    public ArrayList<SquareSnapGalleryItem> getGalleryItemList() {
        return this.mGalleryItemList;
    }

    public SquareSnapGalleryItem getCurrentItem() {
        CamLog.m3d(CameraConstants.TAG, "[Cell] mCurrentIndex : " + this.mCurrentIndex);
        if (this.mGalleryItemList == null || this.mGalleryItemList.size() == 0) {
            return null;
        }
        return (SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex);
    }

    public void showGalleryView(boolean show) {
        if (this.mGalleryLayout != null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] showGalleryView : " + show);
            if (show && this.mGet.isSquareGalleryBtn()) {
                this.mGalleryLayout.setVisibility(0);
            } else {
                this.mGalleryLayout.setVisibility(4);
            }
        }
    }

    public void showTouchBlockCoverView(boolean show) {
        if (this.mTouchBlockCoverView == null) {
            CamLog.m11w(CameraConstants.TAG, "[Cell] return");
        } else if (show) {
            this.mTouchBlockCoverView.setVisibility(0);
            this.mTouchBlockCoverView.setOnTouchListener(new C09234());
        } else {
            this.mTouchBlockCoverView.setVisibility(8);
            this.mTouchBlockCoverView.setOnTouchListener(null);
        }
    }

    public int reloadList(boolean mIsForced) {
        if (this.mGet.getBurstProgress() || !this.mGet.checkModuleValidate(48) || this.mIsDeleting || this.mGet.checkUndoCurrentState(2)) {
            return -1;
        }
        int itemCount;
        CamLog.m7i(CameraConstants.TAG, "[Cell] reloadList");
        if (SecureImageUtil.isSecureCamera()) {
            itemCount = reloadSnapshotListForSecure();
        } else {
            itemCount = getRecentSnapshotUriList(false, mIsForced);
        }
        if (itemCount == -1) {
            return -1;
        }
        if (itemCount == 0) {
            if (this.mMainAccessView != null) {
                int degree = getOrientationDegree();
                setMainAccessViewLayout(degree);
                this.mMainAccessView.rotateLayout(degree);
            }
            showMainAccesView(true, false);
            showGalleryControlUI(false, 0, false);
            return 0;
        }
        if (this.mGalleryViewPager != null) {
            this.mGalleryViewPager.setCurrentItem(this.mCurrentIndex, false);
        }
        if (this.mListener != null) {
            this.mListener.onGalleryPageChanged(((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mUri, false, false);
        }
        if (this.mThreadHandler == null || this.mGalleryItemList.size() == 0) {
            return itemCount;
        }
        Message msg = new Message();
        msg.what = 1;
        msg.arg1 = this.mCurrentIndex;
        msg.arg2 = this.mGalleryItemList.size();
        msg.obj = ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mUri;
        this.mThreadHandler.removeMessages(1);
        this.mThreadHandler.sendMessage(msg);
        return itemCount;
    }

    /* JADX WARNING: Missing block: B:37:0x0127, code:
            if (r12.getCount() == 0) goto L_0x0129;
     */
    private int getRecentSnapshotUriList(boolean r22, boolean r23) {
        /*
        r21 = this;
        r2 = com.lge.camera.util.SecureImageUtil.isSecureCamera();
        if (r2 != 0) goto L_0x001c;
    L_0x0006:
        r2 = "mode_square_snap_shot";
        r0 = r21;
        r6 = r0.mGet;
        r6 = r6.getShotMode();
        r2 = r2.equals(r6);
        if (r2 == 0) goto L_0x001c;
    L_0x0016:
        r0 = r21;
        r2 = r0.mGalleryItemList;
        if (r2 != 0) goto L_0x001e;
    L_0x001c:
        r14 = 0;
    L_0x001d:
        return r14;
    L_0x001e:
        r2 = "external";
        r3 = android.provider.MediaStore.Files.getContentUri(r2);
        r2 = 7;
        r4 = new java.lang.String[r2];
        r2 = 0;
        r6 = "_id";
        r4[r2] = r6;
        r2 = 1;
        r6 = "mime_type";
        r4[r2] = r6;
        r2 = 2;
        r6 = "datetaken";
        r4[r2] = r6;
        r2 = 3;
        r6 = "_display_name";
        r4[r2] = r6;
        r2 = 4;
        r6 = "camera_mode";
        r4[r2] = r6;
        r2 = 5;
        r6 = "burst_id";
        r4[r2] = r6;
        r2 = 6;
        r6 = "count(*) as num";
        r4[r2] = r6;
        r0 = r21;
        r2 = r0.mGet;
        r6 = 1;
        r2 = r2.getAllDir(r6);
        r8 = com.lge.camera.util.FileUtil.getBucketIDStr(r2);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r6 = "burst_id LIKE '%SnapGIF%' OR (camera_mode='8' OR camera_mode='21')) AND (";
        r2 = r2.append(r6);
        r2 = r2.append(r8);
        r6 = ") GROUP BY burst_id, bucket_id HAVING MIN(";
        r2 = r2.append(r6);
        r6 = "datetaken";
        r2 = r2.append(r6);
        r5 = r2.toString();
        r7 = "datetaken DESC,_id DESC";
        r12 = 0;
        r14 = 0;
        r0 = r21;
        r2 = r0.mGet;	 Catch:{ Exception -> 0x01a1 }
        r2 = r2.getActivity();	 Catch:{ Exception -> 0x01a1 }
        r2 = r2.getContentResolver();	 Catch:{ Exception -> 0x01a1 }
        r6 = 0;
        r12 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x01a1 }
        if (r12 != 0) goto L_0x009c;
    L_0x008d:
        r2 = "CameraApp";
        r6 = "[Cell]imageQueryCursor null";
        com.lge.camera.util.CamLog.m3d(r2, r6);	 Catch:{ Exception -> 0x01a1 }
        r14 = 0;
        if (r12 == 0) goto L_0x001d;
    L_0x0097:
        r12.close();
        r12 = 0;
        goto L_0x001d;
    L_0x009c:
        r18 = -1;
        r11 = java.lang.Long.valueOf(r18);	 Catch:{ Exception -> 0x01a1 }
        r14 = r12.getCount();	 Catch:{ Exception -> 0x01a1 }
        if (r22 != 0) goto L_0x00c5;
    L_0x00a8:
        r0 = r21;
        r2 = r0.mGalleryItemList;	 Catch:{ Exception -> 0x01a1 }
        r2 = r2.size();	 Catch:{ Exception -> 0x01a1 }
        if (r2 == 0) goto L_0x011f;
    L_0x00b2:
        r0 = r21;
        r2 = r0.mGalleryItemList;	 Catch:{ Exception -> 0x01a1 }
        r0 = r21;
        r6 = r0.mCurrentIndex;	 Catch:{ Exception -> 0x01a1 }
        r2 = r2.get(r6);	 Catch:{ Exception -> 0x01a1 }
        r2 = (com.lge.camera.managers.SquareSnapGalleryItem) r2;	 Catch:{ Exception -> 0x01a1 }
        r2 = r2.mType;	 Catch:{ Exception -> 0x01a1 }
        r6 = -1;
        if (r2 != r6) goto L_0x011f;
    L_0x00c5:
        r13 = 1;
    L_0x00c6:
        r2 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01a1 }
        r6.<init>();	 Catch:{ Exception -> 0x01a1 }
        r17 = "[Cell] imageQueryCursor.getCount() : ";
        r0 = r17;
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x01a1 }
        r6 = r6.append(r14);	 Catch:{ Exception -> 0x01a1 }
        r17 = ", mGalleryItemList.size() : ";
        r0 = r17;
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x01a1 }
        r0 = r21;
        r0 = r0.mGalleryItemList;	 Catch:{ Exception -> 0x01a1 }
        r17 = r0;
        r17 = r17.size();	 Catch:{ Exception -> 0x01a1 }
        r0 = r17;
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x01a1 }
        r17 = ", isNeedMainAccessView : ";
        r0 = r17;
        r6 = r6.append(r0);	 Catch:{ Exception -> 0x01a1 }
        r6 = r6.append(r13);	 Catch:{ Exception -> 0x01a1 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x01a1 }
        com.lge.camera.util.CamLog.m7i(r2, r6);	 Catch:{ Exception -> 0x01a1 }
        if (r23 != 0) goto L_0x0121;
    L_0x0106:
        if (r22 != 0) goto L_0x0121;
    L_0x0108:
        r0 = r21;
        r2 = r0.mGalleryItemList;	 Catch:{ Exception -> 0x01a1 }
        r2 = r2.size();	 Catch:{ Exception -> 0x01a1 }
        if (r14 != r2) goto L_0x0121;
    L_0x0112:
        r12.close();	 Catch:{ Exception -> 0x01a1 }
        r12 = 0;
        r14 = -1;
        if (r12 == 0) goto L_0x001d;
    L_0x0119:
        r12.close();
        r12 = 0;
        goto L_0x001d;
    L_0x011f:
        r13 = 0;
        goto L_0x00c6;
    L_0x0121:
        if (r22 == 0) goto L_0x0129;
    L_0x0123:
        r2 = r12.getCount();	 Catch:{ Exception -> 0x01a1 }
        if (r2 != 0) goto L_0x012c;
    L_0x0129:
        r21.clearGalleryItemList();	 Catch:{ Exception -> 0x01a1 }
    L_0x012c:
        r2 = r12.getCount();	 Catch:{ Exception -> 0x01a1 }
        if (r2 == 0) goto L_0x0185;
    L_0x0132:
        if (r13 == 0) goto L_0x0149;
    L_0x0134:
        r0 = r21;
        r2 = r0.mGalleryItemList;	 Catch:{ Exception -> 0x01a1 }
        r6 = 0;
        r17 = new com.lge.camera.managers.SquareSnapGalleryItem;	 Catch:{ Exception -> 0x01a1 }
        r18 = 0;
        r19 = 0;
        r20 = -1;
        r17.<init>(r18, r19, r20);	 Catch:{ Exception -> 0x01a1 }
        r0 = r17;
        r2.add(r6, r0);	 Catch:{ Exception -> 0x01a1 }
    L_0x0149:
        r12.moveToFirst();	 Catch:{ Exception -> 0x01a1 }
    L_0x014c:
        r2 = "_id";
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ Exception -> 0x01a1 }
        r18 = r12.getLong(r2);	 Catch:{ Exception -> 0x01a1 }
        r11 = java.lang.Long.valueOf(r18);	 Catch:{ Exception -> 0x01a1 }
        r2 = "mime_type";
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ Exception -> 0x01a1 }
        r15 = r12.getString(r2);	 Catch:{ Exception -> 0x01a1 }
        r2 = "burst_id";
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ Exception -> 0x01a1 }
        r9 = r12.getString(r2);	 Catch:{ Exception -> 0x01a1 }
        r2 = "num";
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ Exception -> 0x01a1 }
        r16 = r12.getInt(r2);	 Catch:{ Exception -> 0x01a1 }
        r0 = r21;
        r1 = r16;
        r0.makeGalleryNewItem(r11, r9, r15, r1);	 Catch:{ Exception -> 0x01a1 }
        r2 = r12.moveToNext();	 Catch:{ Exception -> 0x01a1 }
        if (r2 != 0) goto L_0x014c;
    L_0x0185:
        r2 = "CameraApp";
        r6 = "[Cell] Image loading complete";
        com.lge.camera.util.CamLog.m7i(r2, r6);	 Catch:{ Exception -> 0x01a1 }
        if (r12 == 0) goto L_0x0192;
    L_0x018e:
        r12.close();
        r12 = 0;
    L_0x0192:
        r0 = r21;
        r2 = r0.mGalleryPagerAdapter;
        if (r2 == 0) goto L_0x001d;
    L_0x0198:
        r0 = r21;
        r2 = r0.mGalleryPagerAdapter;
        r2.notifyDataSetChanged();
        goto L_0x001d;
    L_0x01a1:
        r10 = move-exception;
        r10.printStackTrace();	 Catch:{ all -> 0x01ac }
        if (r12 == 0) goto L_0x0192;
    L_0x01a7:
        r12.close();
        r12 = 0;
        goto L_0x0192;
    L_0x01ac:
        r2 = move-exception;
        if (r12 == 0) goto L_0x01b3;
    L_0x01af:
        r12.close();
        r12 = 0;
    L_0x01b3:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.GalleryManager.getRecentSnapshotUriList(boolean, boolean):int");
    }

    private void makeGalleryNewItem(Long id, String burstId, String mimeType, int num) {
        SquareSnapGalleryItem newItem;
        if (mimeType.contains("image")) {
            Uri baseUri = Media.EXTERNAL_CONTENT_URI;
            if (burstId.contains(".gif")) {
                newItem = new SquareSnapGalleryItem(ContentUris.withAppendedId(baseUri, id.longValue()), null, 3);
            } else if (burstId.contains(".jpg")) {
                newItem = new SquareSnapGalleryItem(ContentUris.withAppendedId(baseUri, id.longValue()), null, 0);
            } else if (num > 1) {
                newItem = new SquareSnapGalleryItem(ContentUris.withAppendedId(baseUri, id.longValue()), null, 2);
            } else {
                newItem = new SquareSnapGalleryItem(ContentUris.withAppendedId(baseUri, id.longValue()), null, 0);
            }
        } else {
            newItem = new SquareSnapGalleryItem(ContentUris.withAppendedId(Video.Media.EXTERNAL_CONTENT_URI, id.longValue()), null, 1);
        }
        this.mGalleryItemList.add(newItem);
    }

    protected int getRecentSnapshotUriListForSecure() {
        if (this.mGalleryItemList == null || this.mGalleryPagerAdapter == null) {
            return 0;
        }
        CamLog.m3d(CameraConstants.TAG, "[Cell] getRecentSnapshotUriListForSecure, list size : " + this.mGalleryItemList.size());
        ArrayList<Uri> secureList = SecureImageUtil.get().getSecureLockUriList();
        if (SecureImageUtil.get().getSecureLockUriListSize() == 0) {
            return 0;
        }
        this.mGalleryItemList.add(0, new SquareSnapGalleryItem(null, null, -1));
        String[] imageProjection = new String[]{"_id", "mime_type", CameraConstants.ORIENTATION, "datetaken", "_display_name", CameraConstants.MODE_COLUMN, "burst_id", "count(*) as num"};
        String imageSelection = "burst_id LIKE '%SnapGIF%' OR (camera_mode='8' OR camera_mode='21')) AND (" + FileUtil.getBucketIDStr(this.mGet.getAllDir(true)) + ") GROUP BY burst_id, bucket_id HAVING MIN(" + "datetaken";
        String order = "datetaken DESC,_id DESC";
        Cursor imageQueryCursor = null;
        for (int i = secureList.size() - 1; i >= 0; i--) {
            try {
                imageQueryCursor = this.mGet.getActivity().getContentResolver().query((Uri) secureList.get(i), imageProjection, imageSelection, null, order);
                if (imageQueryCursor == null) {
                    CamLog.m3d(CameraConstants.TAG, "[Cell]imageQueryCursor null");
                    if (imageQueryCursor != null) {
                        imageQueryCursor.close();
                        imageQueryCursor = null;
                    }
                } else {
                    Long id = Long.valueOf(-1);
                    if (imageQueryCursor.getCount() != 0) {
                        imageQueryCursor.moveToFirst();
                        makeGalleryNewItem(Long.valueOf(imageQueryCursor.getLong(imageQueryCursor.getColumnIndexOrThrow("_id"))), imageQueryCursor.getString(imageQueryCursor.getColumnIndexOrThrow("burst_id")), imageQueryCursor.getString(imageQueryCursor.getColumnIndexOrThrow("mime_type")), imageQueryCursor.getInt(imageQueryCursor.getColumnIndexOrThrow("num")));
                    }
                    if (imageQueryCursor != null) {
                        imageQueryCursor.close();
                        imageQueryCursor = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (imageQueryCursor != null) {
                    imageQueryCursor.close();
                    imageQueryCursor = null;
                }
            } catch (Throwable th) {
                if (imageQueryCursor != null) {
                    imageQueryCursor.close();
                }
            }
        }
        if (this.mGalleryPagerAdapter != null) {
            this.mGalleryPagerAdapter.notifyDataSetChanged();
        }
        return this.mGalleryItemList.size();
    }

    protected int reloadSnapshotListForSecure() {
        if (this.mGalleryItemList == null || this.mGalleryPagerAdapter == null) {
            return 0;
        }
        ArrayList<SquareSnapGalleryItem> newItemList = new ArrayList();
        CamLog.m3d(CameraConstants.TAG, "[Cell] reloadSnapshotListForSecure, list size : " + this.mGalleryItemList.size());
        if (this.mGalleryItemList.size() != 0 && ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mType == -1) {
            newItemList.add(0, new SquareSnapGalleryItem(null, null, -1));
        }
        for (int i = 0; i < this.mGalleryItemList.size(); i++) {
            Uri uri = ((SquareSnapGalleryItem) this.mGalleryItemList.get(i)).mUri;
            int type = ((SquareSnapGalleryItem) this.mGalleryItemList.get(i)).mType;
            if (FileUtil.getIdFromUri(getActivity(), uri) != -1) {
                newItemList.add(new SquareSnapGalleryItem(uri, null, type));
            }
        }
        if (this.mGalleryItemList.size() == newItemList.size()) {
            return -1;
        }
        clearGalleryItemList();
        this.mGalleryItemList.addAll(newItemList);
        if (this.mGalleryPagerAdapter != null) {
            this.mGalleryPagerAdapter.notifyDataSetChanged();
        }
        return this.mGalleryItemList.size();
    }

    public void setRotateDegree(final int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mGalleryRotateLayout != null && this.mGalleryPlayButtonLayout != null && this.mMuteButton != null && this.mDeleteButton != null && this.mBurstShotMark != null && this.mMainAccessView != null) {
            if (this.mMainAccessView.getVisibility() == 0) {
                if (animation) {
                    final RelativeLayout rl = (RelativeLayout) this.mGalleryLayout.findViewById(C0088R.id.snap_galley_main_access);
                    AnimationUtil.startAlphaAnimation(this.mMainAccessView, 1.0f, 0.0f, 130, new AnimationListener() {
                        public void onAnimationStart(Animation arg0) {
                        }

                        public void onAnimationRepeat(Animation arg0) {
                        }

                        public void onAnimationEnd(Animation arg0) {
                            GalleryManager.this.mMainAccessView.rotateLayout(degree);
                            GalleryManager.this.setMainAccessViewLayout(degree);
                            AnimationUtil.startAlphaAnimation(rl, 0.0f, 1.0f, 200, null);
                        }
                    });
                } else {
                    this.mMainAccessView.rotateLayout(degree);
                    setMainAccessViewLayout(degree);
                }
                this.mGalleryRotateLayout.rotateLayout(degree);
            } else {
                int beforeDegree = this.mGalleryRotateLayout.getAngle();
                this.mGalleryRotateLayout.rotateLayout(degree);
                if (this.mGalleryLayout.getVisibility() == 0) {
                    AnimationUtil.startRotateAnimationForRotateLayout(this.mGalleryRotateLayout, beforeDegree, degree, false, 300, null);
                }
                this.mMainAccessView.rotateLayout(degree);
                setMainAccessViewLayout(degree);
            }
            this.mGalleryPlayButtonLayout.rotateLayout(degree);
            setRotatePlayButton(degree);
            this.mMuteButton.setDegree(degree, true);
            this.mDeleteButton.setDegree(degree, true);
            this.mBurstShotMark.setDegree(degree, true);
        }
    }

    public void onDeleteComplete(boolean isBurst, int deleteResult) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] onDeleteComplete, isBurst : " + isBurst);
        if (!isBurst) {
            clearUndoItem(false);
        } else if (deleteResult != 0) {
            this.mGet.showToast(this.mGet.getAppContext().getString(deleteResult), CameraConstants.TOAST_LENGTH_SHORT);
            clearUndoItem(true);
        } else {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    GalleryManager.this.doDeleteItemOnList(GalleryManager.this.mReadyToDeleteItem);
                }
            }, 50);
        }
    }

    public void onUndoClicked() {
        boolean isEmptyList = true;
        if (this.mReadyToDeleteItem == null || this.mGalleryItemList == null || this.mGalleryPagerAdapter == null || this.mGalleryViewPager == null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] mReadyToDeleteItem is null, return");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "[Cell] onUndoClicked, restoreIndex : " + this.mDeleteIndex + ", mReadyToDeleteItem uri : " + this.mReadyToDeleteItem.mUri);
        this.mIsUndoing = true;
        int nextIndex = SquareUtil.checkIndexBoundary(this.mDeleteIndex, this.mGalleryItemList.size());
        this.mCurrentIndex = nextIndex;
        if (this.mGalleryItemList.size() != 0) {
            isEmptyList = false;
        }
        SquareSnapGalleryItem undoItem = new SquareSnapGalleryItem(this.mReadyToDeleteItem);
        makeUndoAnimataionBitmap(nextIndex, isEmptyList, undoItem);
        this.mGalleryItemList.add(this.mDeleteIndex, undoItem);
        clearUndoItem(false);
        this.mGalleryPagerAdapter.notifyDataSetChanged();
        this.mGalleryViewPager.setCurrentItem(this.mDeleteIndex, false);
        startUndoAnimation(new C09267());
    }

    private void startUndoAnimation(AnimationListener animListener) {
        if (this.mGalleryItemList.size() == 1 || this.mDeleteIndex != this.mGalleryItemList.size() - 1) {
            AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationFrontView, getAppContext(), getOrientationDegree(), false, -1.0f, animListener);
            AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationRearView, getAppContext(), getOrientationDegree(), true, -1.0f, null);
            return;
        }
        AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationFrontView, getAppContext(), getOrientationDegree(), false, 1.0f, animListener);
        AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationRearView, getAppContext(), getOrientationDegree(), true, 1.0f, null);
    }

    private void makeUndoAnimataionBitmap(int nextIndex, boolean isEmptyList, SquareSnapGalleryItem undoItem) {
        Bitmap undoBmp = getThumbnailBitmap(undoItem.mUri, undoItem.mType, true);
        if (!(undoBmp == null || undoBmp.isRecycled())) {
            this.mSquareSnapshotAnimationFrontView.setImageBitmap(undoBmp);
        }
        if (isEmptyList) {
            captureMainAccessView(this.mSquareSnapshotAnimationRearView);
            return;
        }
        Bitmap prevBmp = getThumbnailBitmap(((SquareSnapGalleryItem) this.mGalleryItemList.get(nextIndex)).mUri, ((SquareSnapGalleryItem) this.mGalleryItemList.get(nextIndex)).mType, true);
        if (prevBmp != null && !prevBmp.isRecycled()) {
            this.mSquareSnapshotAnimationRearView.setImageBitmap(prevBmp);
        }
    }

    public void deleteOrUndo(Uri uri, String burstId) {
        this.mGet.deleteOrUndo(uri, burstId, this);
    }

    public boolean isNewItemAdding() {
        return this.mIsNewItemAdding;
    }
}
