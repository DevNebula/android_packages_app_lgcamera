package com.lge.camera.managers;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.support.p000v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.FastThumbnailAdapter;
import com.lge.camera.app.ThumbnailAdapter;
import com.lge.camera.app.ThumbnailCache;
import com.lge.camera.app.ThumbnailHelper;
import com.lge.camera.app.ThumbnailLoader;
import com.lge.camera.components.FastThumbnailListView;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.ThumbnailListView;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

class ThumbnailListManagerBase implements OnRemoveHandler, UndoInterface, LoaderCallbacks<Cursor> {
    private static final String[] DEFAULT_ROLL_PROJECTION = new String[]{"_id", "_data", "mime_type", CameraConstants.ORIENTATION, "datetaken", "_display_name", CameraConstants.MODE_COLUMN, "burst_id", "num"};
    protected static final int FRICTION_SCALE_FACTOR = 5;
    protected static final int MSG_DECODE_JPEG = 0;
    protected static long sCurrentTime = 0;
    protected final float ALPHA_DIM = 0.4f;
    protected final float ALPHA_NORMAL = 1.0f;
    protected final int VIEW_PAGER_OFFSCREEN_CNT = 3;
    protected ThumbnailLoader cursorLoader;
    protected ThumbnailAdapter mAdapter = null;
    private long mAniCheckTime = SystemClock.uptimeMillis();
    protected RotateLayout mAniFrontView = null;
    protected RotateLayout mAniRearView = null;
    protected ImageView mBackCover = null;
    protected ImageView mCameraRollAnimationFrontView = null;
    protected ImageView mCameraRollAnimationRearView = null;
    protected int mCurrentPage = 0;
    protected Cursor mCursor;
    protected int mDegree;
    protected RotateImageButton mDeleteBtn = null;
    protected ArrayList<Long> mDeleteIds = new ArrayList();
    protected ImageView mFakePreview = null;
    protected FastThumbnailAdapter mFastThumbnailAdapter = null;
    protected FastThumbnailListView mFastThumbnailListView = null;
    protected int mGalleryPlayType = 0;
    protected TilePreviewInterface mGet = null;
    protected HandlerThread mImageHandlerThread;
    protected boolean mIsActivatedQuickDetailView = false;
    private boolean mIsActivatedTilePreview = false;
    protected boolean mIsDelAnimDirectionRtoL = true;
    protected boolean mIsFromInitLayout = false;
    protected boolean mIsLastItemDeleted = false;
    protected boolean mIsRTL = false;
    protected ViewPager mPager = null;
    protected ThumbnailListPagerAdapter mPagerAdapter = null;
    protected RotateImageButton mPagerBadge = null;
    ArrayList<ThumbnailListItem> mRecentItems = null;
    protected ThumbnailCache mRotateCache;
    protected boolean mSkipPageChangedNotification = false;
    protected int mThreadCount = 0;
    protected Handler mThreadHandler = null;
    protected ThumbnailHelper mThumbHelper;
    protected View mThumbnailListCoverView = null;
    protected LinearLayout mThumbnailListEmptyView = null;
    protected ThumbnailListView mThumbnailListView = null;
    protected RotateLayout mTilePreviewLayout = null;
    protected ThumbnailListItem mUndoItem = null;
    HandlerRunnable mUpdateCursorRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            ThumbnailListManagerBase.this.loadCursor();
        }
    };

    class LoadCursorTask extends AsyncTask<Void, Void, Cursor> {
        LoadCursorTask() {
        }

        protected Cursor doInBackground(Void... arg0) {
            return new ThumbnailLoader(ThumbnailListManagerBase.this.mGet.getAppContext(), FileUtil.getBucketIDStr(ThumbnailListManagerBase.this.mGet.getDirPath(false)), ThumbnailListManagerBase.sCurrentTime).loadInBackground();
        }

        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && !cursor.isClosed() && !ThumbnailListManagerBase.this.mGet.isPaused()) {
                CamLog.m3d(CameraConstants.TAG, "[Tile] cursor load finished.");
                ThumbnailListManagerBase.this.onLoadFinished(ThumbnailListManagerBase.this.cursorLoader, cursor);
            }
        }
    }

    class LoadThumbnailTask extends AsyncTask<Void, Void, Void> {
        LoadThumbnailTask() {
        }

        protected Void doInBackground(Void... arg0) {
            try {
                if (!(ThumbnailListManagerBase.this.mRecentItems == null || ThumbnailListManagerBase.this.mRecentItems.size() == 0)) {
                    CamLog.m3d(CameraConstants.TAG, "[Tile] mRecentItems : " + ThumbnailListManagerBase.this.mRecentItems.size());
                    for (int i = 0; i < ThumbnailListManagerBase.this.mRecentItems.size(); i++) {
                        ThumbnailListItem item = (ThumbnailListItem) ThumbnailListManagerBase.this.mRecentItems.get(i);
                        if (item != null) {
                            Bitmap bitmap;
                            if (item.mIsImage) {
                                bitmap = Thumbnails.getThumbnail(ThumbnailListManagerBase.this.mGet.getAppContext().getContentResolver(), item.f33id, 0, 3, null);
                            } else {
                                bitmap = Video.Thumbnails.getThumbnail(ThumbnailListManagerBase.this.mGet.getAppContext().getContentResolver(), item.f33id, 0, 3, null);
                            }
                            CamLog.m3d(CameraConstants.TAG, "[Tile] getThumbnail in background - " + item.mUri);
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public ThumbnailListManagerBase(TilePreviewInterface tilePreviewInterface) {
        this.mGet = tilePreviewInterface;
    }

    public void sendUpdateJPEGMsg(int currentPage) {
        if (this.mThreadHandler != null) {
            this.mThreadHandler.removeMessages(0);
            Message msg = new Message();
            if (getCount() != 0) {
                ThumbnailListItem item = getItem(currentPage);
                if (item != null) {
                    msg.obj = item.mUri;
                    msg.arg1 = currentPage;
                    msg.arg2 = item.mModeColumn;
                    msg.what = 0;
                    this.mThreadHandler.sendMessage(msg);
                }
            }
        }
    }

    protected Bitmap getBitmap(Uri uri, int modeColumn) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] getBitmap uri : " + uri);
        ContentResolver resolver = this.mGet.getAppContext().getContentResolver();
        String mediaType = resolver.getType(uri);
        CamLog.m7i(CameraConstants.TAG, "[Tile] mediaType : " + mediaType);
        boolean isImageType = mediaType != null && mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE);
        if (!isImageType) {
            return null;
        }
        String filePath = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), uri);
        if (filePath == null) {
            return null;
        }
        ExifInterface exif = Exif.readExif(filePath);
        int exifDegree = Exif.getOrientation(exif);
        if (mediaType.contains("dng")) {
            exifDegree = FileUtil.getOrientationFromDB(resolver, uri);
        }
        int[] actualSize = Exif.getExifSize(exif);
        if (actualSize[0] == 0 || actualSize[1] == 0) {
            actualSize = Exif.getImageSize(exif);
        }
        if (actualSize[0] == 0 || actualSize[1] == 0) {
            actualSize = BitmapManagingUtil.getImageSize(this.mGet.getAppContext(), uri);
        }
        if (modeColumn == 100 || modeColumn == 1) {
            actualSize = BitmapManagingUtil.getFitSizeOfBitmapForLCD(this.mGet.getActivity(), actualSize[0] * 3, actualSize[1] * 3, this.mDegree);
        }
        CamLog.m3d(CameraConstants.TAG, "[Tile] actualSize[0] = " + actualSize[0] + " actualSize[1] : " + actualSize[1] + " modeColumn : " + modeColumn + " exifDegree : " + exifDegree + " mediaType : " + mediaType);
        return BitmapManagingUtil.loadScaledandRotatedBitmap(this.mGet.getAppContext().getContentResolver(), uri.toString(), actualSize[0], actualSize[1], exifDegree);
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }

    public void setActivatedQuickDetailView(boolean set) {
        this.mIsActivatedQuickDetailView = set;
        if (set) {
            setDeleteButtonVisibility(true);
        }
    }

    public boolean isActivatedQuickdetailView() {
        return this.mIsActivatedQuickDetailView;
    }

    public void setTilePreviewLayout() {
        LayoutParams lp;
        int thumbnailWidth = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
        CamLog.m7i(CameraConstants.TAG, "[Tile] thumbnailWidth : " + thumbnailWidth);
        if (this.mThumbnailListView != null) {
            lp = (LayoutParams) this.mThumbnailListView.getLayoutParams();
            lp.width = thumbnailWidth;
            this.mThumbnailListView.setLayoutParams(lp);
        }
        if (this.mFastThumbnailListView != null) {
            lp = (LayoutParams) this.mFastThumbnailListView.getLayoutParams();
            lp.width = thumbnailWidth;
            this.mFastThumbnailListView.setLayoutParams(lp);
        }
        if (this.mThumbnailListCoverView != null) {
            LayoutParams rlp = (LayoutParams) this.mThumbnailListCoverView.getLayoutParams();
            rlp.width = thumbnailWidth;
            this.mThumbnailListCoverView.setLayoutParams(rlp);
        }
        FrameLayout layout = (FrameLayout) this.mGet.findViewById(C0088R.id.thumbnail_list_layout);
        if (layout != null) {
            LayoutParams lp2 = (LayoutParams) layout.getLayoutParams();
            lp2.topMargin = thumbnailWidth;
            layout.setLayoutParams(lp2);
        }
        RotateLayout aniLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.detail_animation_view_layout);
        if (aniLayout != null) {
            LayoutParams lp3 = (LayoutParams) aniLayout.getLayoutParams();
            lp3.topMargin = thumbnailWidth;
            aniLayout.setLayoutParams(lp3);
        }
        if (this.mThumbnailListEmptyView != null) {
            LayoutParams lp4 = (LayoutParams) this.mThumbnailListEmptyView.getLayoutParams();
            lp4.width = thumbnailWidth;
            this.mThumbnailListEmptyView.setLayoutParams(lp4);
        }
    }

    protected int getClickedItemPosition(AdapterView<?> listview, int position) {
        int diff = Math.abs(position - listview.getFirstVisiblePosition());
        CamLog.m7i(CameraConstants.TAG, "[Tile] diff : " + diff);
        return diff;
    }

    public boolean isActivatedTilePreview() {
        return this.mIsActivatedTilePreview;
    }

    public void setIsActiviatedTilePreview(boolean isEnableTilePreview) {
        this.mIsActivatedTilePreview = isEnableTilePreview;
    }

    protected Bitmap getViewBitmap(View view) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] getViewBitmap");
        if (view == null) {
            return null;
        }
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();
        if (bitmap == null) {
            return null;
        }
        Bitmap result = Bitmap.createBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        return result;
    }

    protected void setSelectedPagerType() {
        if (getCount() != 0 && getCount() > this.mCurrentPage) {
            ThumbnailListItem item = getItem(this.mCurrentPage);
            if (item != null) {
                String mimeType = item.mMediaType;
                int modeColumn = item.mModeColumn;
                boolean isBurstShot = item.mIsBurstShot;
                CamLog.m7i(CameraConstants.TAG, "[Tile] setSelectedPagerType : " + this.mCurrentPage + " / mimeType : " + mimeType + " / modeColumn : " + modeColumn + " / isBurstShot : " + isBurstShot);
                if (mimeType != null && this.mPagerBadge != null) {
                    if (mimeType.contains(QuickClipUtil.GIF)) {
                        this.mPagerBadge.setBackgroundResource(C0088R.drawable.btn_gif_play);
                        this.mPagerBadge.setVisibility(0);
                        this.mPagerBadge.setClickable(true);
                        this.mPagerBadge.setContentDescription(this.mGet.getActivity().getString(C0088R.string.gif_viewer));
                        this.mGalleryPlayType = 0;
                    } else if (mimeType.contains("dng") || modeColumn == 31) {
                        this.mPagerBadge.setBackgroundResource(C0088R.drawable.camera_quick_clip_badge_dng);
                        this.mPagerBadge.setVisibility(0);
                        this.mPagerBadge.setClickable(false);
                        this.mGalleryPlayType = 0;
                    } else if (modeColumn == 100) {
                        this.mPagerBadge.setBackgroundResource(C0088R.drawable.btn_360_panorama_play);
                        this.mPagerBadge.setVisibility(0);
                        this.mPagerBadge.setClickable(true);
                        this.mPagerBadge.setContentDescription(this.mGet.getActivity().getString(C0088R.string.gallery));
                        this.mGalleryPlayType = 1;
                    } else if (modeColumn == 1) {
                        this.mPagerBadge.setBackgroundResource(C0088R.drawable.btn_2d_panorama_play);
                        this.mPagerBadge.setVisibility(0);
                        this.mPagerBadge.setClickable(true);
                        this.mPagerBadge.setContentDescription(this.mGet.getActivity().getString(C0088R.string.gallery));
                        this.mGalleryPlayType = 0;
                    } else if (modeColumn == 4) {
                        this.mPagerBadge.setBackgroundResource(C0088R.drawable.btn_guide_shot_play);
                        this.mPagerBadge.setVisibility(0);
                        this.mPagerBadge.setClickable(true);
                        this.mPagerBadge.setContentDescription(this.mGet.getActivity().getString(C0088R.string.gallery));
                        this.mGalleryPlayType = 3;
                    } else if (modeColumn == 201) {
                        this.mPagerBadge.setBackgroundResource(C0088R.drawable.btn_live_photo_play);
                        this.mPagerBadge.setVisibility(0);
                        this.mPagerBadge.setClickable(true);
                        this.mPagerBadge.setContentDescription(this.mGet.getActivity().getString(C0088R.string.gallery));
                        this.mGalleryPlayType = 0;
                    } else if (isBurstShot) {
                        this.mPagerBadge.setBackgroundResource(C0088R.drawable.btn_burst_play);
                        this.mPagerBadge.setVisibility(0);
                        this.mPagerBadge.setClickable(true);
                        this.mPagerBadge.setContentDescription(this.mGet.getActivity().getString(C0088R.string.burstshot_viewer));
                        this.mGalleryPlayType = 2;
                    } else {
                        this.mPagerBadge.setClickable(false);
                        this.mPagerBadge.setVisibility(8);
                        this.mGalleryPlayType = 0;
                    }
                }
            }
        }
    }

    protected boolean showDetailViewLayout(boolean show, boolean isDelete) {
        boolean z = true;
        int visiblity = 8;
        CamLog.m7i(CameraConstants.TAG, "[Tile] showDetailViewLayout start show : " + show);
        SystemBarUtil.hideSystemUI(this.mGet.getActivity());
        FrameLayout layout = (FrameLayout) this.mGet.findViewById(C0088R.id.thumbnail_list_layout);
        if (layout == null) {
            return false;
        }
        RotateLayout pagerLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.tile_preview_rotate_layout);
        if (pagerLayout != null) {
            pagerLayout.clearAnimation();
        }
        TilePreviewInterface tilePreviewInterface;
        if (getCount() != 0 || this.mAdapter == null || this.mFakePreview == null) {
            boolean retVal = false;
            if (show) {
                visiblity = 0;
            }
            if (!show) {
                this.mGet.updateQuickClipForTilePreview(isActivatedQuickdetailView(), false, null);
                if (layout.getVisibility() == 0) {
                    this.mIsActivatedQuickDetailView = false;
                    retVal = true;
                    if (!isDelete) {
                        readyAnimation(this.mPager, this.mThumbnailListView.getChildAt(getClickedItemPosition(this.mThumbnailListView, this.mCurrentPage)), false, false);
                    }
                    TouchImageView view = this.mPagerAdapter.getSpecficView(getPagerPosition(this.mCurrentPage));
                    if (view != null && view.isZoomed()) {
                        view.resetZoom();
                    }
                }
                setSelectedItem(-1);
                this.mThreadCount = 0;
            }
            setActivatedQuickDetailView(show);
            layout.setVisibility(visiblity);
            tilePreviewInterface = this.mGet;
            if (getCount() != 0) {
                z = false;
            }
            tilePreviewInterface.showThumbnailListDetailView(show, z, layout.getZ());
            if (show && this.mFastThumbnailListView != null) {
                this.mFastThumbnailListView.setVisibility(4);
            }
            CamLog.m7i(CameraConstants.TAG, "[Tile] showDetailViewLayout end ");
            return retVal;
        }
        this.mIsActivatedQuickDetailView = false;
        layout.setVisibility(8);
        this.mFakePreview.setVisibility(8);
        setSelectedItem(-1);
        tilePreviewInterface = this.mGet;
        if (getCount() != 0) {
            z = false;
        }
        tilePreviewInterface.showThumbnailListDetailView(false, z, 0.0f);
        return false;
    }

    protected void setSelectedItem(int position) {
        if (this.mAdapter != null) {
            this.mAdapter.setSelectedItem(position);
        }
    }

    protected void readyAnimation(View origin, View target, boolean open, boolean isDelete) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] open : " + open + " isDelete : " + isDelete);
        if (origin != null && target != null) {
            float scaleX;
            float scaleY;
            int[] startPos = new int[2];
            int[] targetPos = new int[2];
            int[] deletePos = new int[2];
            float srcX = 0.0f;
            float destX = 0.0f;
            float destY = 0.0f;
            origin.getLocationOnScreen(startPos);
            target.getLocationOnScreen(targetPos);
            View deleteBtn = this.mGet.findViewById(C0088R.id.thumbnail_pager_button_delete);
            if (deleteBtn != null) {
                deleteBtn.getLocationOnScreen(deletePos);
            }
            DisplayMetrics outMetrics = Utils.getWindowRealMatics(this.mGet.getAppContext());
            int lcdWidth = outMetrics.heightPixels;
            int lcdHeight = outMetrics.widthPixels;
            if (open) {
                scaleX = ((float) origin.getMeasuredWidth()) / ((float) target.getMeasuredWidth());
                scaleY = ((float) origin.getMeasuredHeight()) / ((float) target.getMeasuredHeight());
            } else {
                scaleX = ((float) target.getMeasuredWidth()) / ((float) origin.getMeasuredWidth());
                scaleY = ((float) target.getMeasuredHeight()) / ((float) origin.getMeasuredHeight());
            }
            if (Float.isInfinite(scaleX) || Float.isInfinite(scaleY)) {
                scaleX = ((float) origin.getMeasuredHeight()) / ((float) lcdHeight);
                scaleY = ((float) origin.getMeasuredHeight()) / ((float) lcdWidth);
            }
            showAnimationView(true);
            ThumbnailListItem item = getItem(this.mCurrentPage);
            if (item != null) {
                ImageView imageView = (ImageView) this.mGet.findViewById(C0088R.id.detail_animation_view);
                setDetailImageView(imageView, item);
                if (open) {
                    srcX = (float) (lcdHeight - (lcdHeight - startPos[0]));
                } else {
                    destX = (float) targetPos[0];
                    destY = -160.0f;
                    if (isDelete) {
                        destX = ((float) deletePos[0]) - ((float) this.mDeleteBtn.getWidth());
                        destY = ((float) deletePos[1]) - ((float) ((this.mDeleteBtn.getHeight() / 2) + RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f)));
                    }
                }
                CamLog.m7i(CameraConstants.TAG, "[Tile] animation srcX : " + srcX + " / srcY : " + 0.0f + "  destX : " + destX + "  /  destY : " + destY);
                setAnimationSetAndStart(imageView, scaleX, scaleY, srcX, 0.0f, destX, destY, open, isDelete);
            }
        }
    }

    private void setDetailImageView(ImageView imageView, ThumbnailListItem item) {
        Bitmap bmp = null;
        if (this.mRotateCache != null) {
            bmp = (Bitmap) this.mRotateCache.get(item.mUri);
        }
        if (bmp != null && !bmp.isRecycled()) {
            imageView.setImageBitmap(bmp);
            imageView.setVisibility(0);
        }
    }

    private void setAnimationSetAndStart(View view, float scaleX, float scaleY, float srcX, float srcY, float destX, float destY, boolean open, boolean isDelete) {
        ScaleAnimation sa;
        AlphaAnimation aa;
        TranslateAnimation ta = new TranslateAnimation(srcX, destX, srcY, destY);
        if (open) {
            sa = new ScaleAnimation(scaleX, 1.0f, scaleY, 1.0f);
            aa = new AlphaAnimation(0.0f, 1.0f);
        } else {
            sa = new ScaleAnimation(1.0f, scaleX, 1.0f, scaleY);
            aa = new AlphaAnimation(1.0f, 0.0f);
        }
        AnimationSet aniSet = new AnimationSet(true);
        setAnimationSetListener(view, aniSet, open, isDelete);
        aniSet.addAnimation(sa);
        aniSet.addAnimation(ta);
        aniSet.addAnimation(aa);
        aniSet.setDuration(200);
        aniSet.setInterpolator(new DecelerateInterpolator());
        view.setVisibility(0);
        view.startAnimation(aniSet);
        if (open) {
            this.mIsActivatedQuickDetailView = true;
        }
    }

    protected void setAnimationSetListener(View view, AnimationSet aniSet, final boolean open, final boolean isDelete) {
        aniSet.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                CamLog.m7i(CameraConstants.TAG, "[Tile] onAnimationStart open : " + open + " isDelete : " + isDelete);
                if (open) {
                    ThumbnailListManagerBase.this.mFakePreview.setVisibility(0);
                    ThumbnailListManagerBase.this.showAnimationBGView(true);
                    ThumbnailListManagerBase.this.mThumbnailListView.setEnabled(false);
                } else {
                    ThumbnailListManagerBase.this.mFakePreview.setVisibility(8);
                }
                ((ImageView) ThumbnailListManagerBase.this.mGet.findViewById(C0088R.id.detail_animation_view)).setVisibility(0);
                if (isDelete) {
                    ThumbnailListManagerBase.this.mDeleteBtn.setEnabled(false);
                    ThumbnailListManagerBase.this.mDeleteBtn.setAlpha(0.4f);
                }
            }

            public void onAnimationRepeat(Animation animation) {
                CamLog.m7i(CameraConstants.TAG, "[Tile] onAnimationRepeat");
            }

            public void onAnimationEnd(Animation animation) {
                CamLog.m7i(CameraConstants.TAG, "[Tile] onAnimationEnd");
                ThumbnailListManagerBase.this.mGet.runOnUiThread(new HandlerRunnable(ThumbnailListManagerBase.this) {
                    public void handleRun() {
                        ThumbnailListManagerBase.this.setDetailViewAfterAnimationEnd(open, isDelete);
                    }
                });
            }
        });
    }

    protected void setDetailViewAfterAnimationEnd(boolean open, boolean isDelete) {
        if (open) {
            this.mCurrentPage = getPagerPosition(this.mCurrentPage);
            showDetailViewLayout(true, false);
            this.mThumbnailListView.setEnabled(true);
            sendUpdateJPEGMsg(this.mCurrentPage);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    ThumbnailListManagerBase.this.mPager.setCurrentItem(ThumbnailListManagerBase.this.mCurrentPage, false);
                    ThumbnailListManagerBase.this.showAnimationView(false);
                    ThumbnailListManagerBase.this.showAnimationBGView(false);
                }
            }, 50);
            ThumbnailListItem item = getItem(this.mCurrentPage);
            if (item != null) {
                this.mGet.updateQuickClipForTilePreview(isActivatedQuickdetailView(), true, item.mUri);
                return;
            }
            return;
        }
        if (this.mFakePreview != null && this.mFakePreview.getVisibility() == 0) {
            this.mFakePreview.setVisibility(8);
        }
        if (isDelete) {
            this.mDeleteBtn.setEnabled(true);
            this.mDeleteBtn.setAlpha(1.0f);
        } else {
            showDetailViewLayout(false, isDelete);
        }
        showAnimationView(false);
    }

    protected void showAnimationBGView(boolean show) {
        RelativeLayout layout = (RelativeLayout) this.mGet.findViewById(C0088R.id.detail_animation_view_bg);
        if (layout == null) {
            return;
        }
        if (show) {
            layout.setVisibility(0);
        } else {
            layout.setVisibility(8);
        }
    }

    protected void showAnimationView(boolean show) {
        RelativeLayout layout = (RelativeLayout) this.mGet.findViewById(C0088R.id.detail_animation_view_layout);
        if (show) {
            layout.setVisibility(0);
        } else {
            layout.setVisibility(8);
        }
    }

    protected ThumbnailListItem makeThumbnailListItem(Uri uri, int mode, String burstId) {
        boolean z;
        ThumbnailListItem item = new ThumbnailListItem();
        try {
            item.f33id = ContentUris.parseId(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        item.setUri(uri);
        item.setModeColumn(mode);
        item.setBurstId(burstId);
        if (burstId != null) {
            z = true;
        } else {
            z = false;
        }
        item.setIsBurstShot(z);
        if (uri != null) {
            if (uri.toString().contains("image")) {
                item.setMediaType("image");
                item.mIsImage = true;
            } else if (uri.toString().contains("video")) {
                item.setMediaType("video");
                item.mIsImage = false;
            }
        }
        return item;
    }

    public void onNewItemAdded(final Uri uri, int mode, String burstId) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] onNewItemAdded");
        final ThumbnailListItem item = makeThumbnailListItem(uri, mode, burstId);
        addRecentItem(item);
        String currentShotMode = this.mGet.getShotMode();
        if (currentShotMode != null && !this.mGet.isPaused() && "on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            if (currentShotMode.contains("mode_normal") || currentShotMode.contains(CameraConstants.MODE_BEAUTY) || currentShotMode.contains(CameraConstants.MODE_SMART_CAM) || currentShotMode.contains(CameraConstants.MODE_SMART_CAM_FRONT)) {
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (!ThumbnailListManagerBase.this.mGet.isPaused() && ThumbnailListManagerBase.this.mThumbnailListView != null && ThumbnailListManagerBase.this.mFastThumbnailAdapter != null && ThumbnailListManagerBase.this.mFastThumbnailListView != null) {
                            if (ThumbnailListManagerBase.this.getCount() == 0) {
                                ThumbnailListManagerBase.this.mFastThumbnailAdapter.resetItemList();
                                if (ThumbnailListManagerBase.this.mGet.checkUndoCurrentState(6)) {
                                    ThumbnailListManagerBase.this.mGet.deleteImmediatelyNotUndo();
                                    return;
                                }
                            }
                            if (ThumbnailListManagerBase.this.isActivatedTilePreview() && uri != null && uri.toString().contains("image")) {
                                ThumbnailListManagerBase.this.startNewContentAddAnimation();
                            }
                            ThumbnailListManagerBase.this.mFastThumbnailAdapter.addItem(item);
                            if (!ThumbnailListManagerBase.this.isActivatedQuickdetailView()) {
                                ThumbnailListManagerBase.this.mThumbnailListView.stopNestedScroll();
                                ThumbnailListManagerBase.this.mThumbnailListView.setSelection(0);
                                ThumbnailListManagerBase.this.mFastThumbnailListView.stopNestedScroll();
                                ThumbnailListManagerBase.this.mFastThumbnailListView.setSelection(0);
                            }
                        }
                    }
                });
                this.mGet.removePostRunnable(this.mUpdateCursorRunnable);
                this.mGet.postOnUiThread(this.mUpdateCursorRunnable, 400);
            }
        }
    }

    public void delayCursorUpdate() {
        this.mGet.removePostRunnable(this.mUpdateCursorRunnable);
    }

    protected boolean readyNewContentAddAnimation() {
        if (this.mFastThumbnailListView == null || this.mThumbnailListView == null) {
            return false;
        }
        if (isActivatedQuickdetailView()) {
            this.mFastThumbnailListView.setVisibility(4);
            return false;
        }
        if (this.mFastThumbnailListView.getVisibility() != 0) {
            this.mFastThumbnailListView.setVisibility(0);
        }
        long currentTime = SystemClock.uptimeMillis();
        long gapTime = currentTime - this.mAniCheckTime;
        this.mAniCheckTime = currentTime;
        if (gapTime < 500) {
            return false;
        }
        if (this.mFastThumbnailAdapter.getCount() < 5 && this.mFastThumbnailAdapter.getCount() > 0) {
            this.mFastThumbnailListView.setVisibility(4);
            this.mThumbnailListView.setVisibility(4);
        } else if (this.mFastThumbnailAdapter.getCount() == 0) {
            return false;
        }
        if (this.mThumbnailListView.canScrollList(-1) || this.mFastThumbnailListView.canScrollList(-1)) {
            return false;
        }
        return true;
    }

    protected void startNewContentAddAnimation() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] startNewContentAddAnimation");
        if (!this.mGet.checkModuleValidate(207)) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] recording...");
        } else if (this.mFastThumbnailListView != null && this.mThumbnailListView != null && readyNewContentAddAnimation()) {
            View rowView = this.mFastThumbnailListView.getChildAt(0);
            if (rowView != null) {
                float f;
                final ImageView imageView = (ImageView) this.mGet.findViewById(C0088R.id.bitmapImageView);
                imageView.setImageBitmap(getViewBitmap(this.mFastThumbnailListView));
                if (this.mIsRTL) {
                    f = 180.0f;
                } else {
                    f = 0.0f;
                }
                imageView.setRotation(f);
                imageView.setVisibility(0);
                TranslateAnimation transAnim = new TranslateAnimation(0.0f, 0.0f, 0.0f, this.mIsRTL ? (float) (rowView.getHeight() * -1) : (float) rowView.getHeight());
                transAnim.setDuration(150);
                transAnim.setAnimationListener(new AnimationListener() {
                    public void onAnimationEnd(Animation animation) {
                        imageView.setVisibility(8);
                        if (!(ThumbnailListManagerBase.this.mThumbnailListView == null || ThumbnailListManagerBase.this.mThumbnailListView.getVisibility() == 0)) {
                            ThumbnailListManagerBase.this.mThumbnailListView.setVisibility(0);
                        }
                        if (ThumbnailListManagerBase.this.mFastThumbnailListView != null && ThumbnailListManagerBase.this.mFastThumbnailListView.getVisibility() != 0) {
                            ThumbnailListManagerBase.this.mFastThumbnailListView.setVisibility(0);
                        }
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationStart(Animation animation) {
                    }
                });
                imageView.startAnimation(transAnim);
            }
        }
    }

    protected boolean isLastIndex() {
        if (this.mAdapter == null || this.mAdapter.getCount() - 1 != this.mCurrentPage) {
            return false;
        }
        return true;
    }

    protected void doDeleteItemOnList() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] doDeleteItemOnList");
        if (isActivatedQuickdetailView()) {
            final int position = this.mCurrentPage;
            if (position < getCount()) {
                int nextPosition = position + 1;
                if (nextPosition >= getCount()) {
                    nextPosition = getCount() - 1;
                }
                if (nextPosition < 0) {
                    nextPosition = 0;
                }
                final RelativeLayout layout = (RelativeLayout) this.mGet.findViewById(C0088R.id.detail_animation_view_bg);
                if (layout != null) {
                    layout.setVisibility(0);
                }
                makeDeleteAnimationBitmap(nextPosition);
                startDeleteAnimation(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        if (layout != null) {
                            layout.setVisibility(8);
                        }
                        ThumbnailListManagerBase.this.setCurrentItemWithoutPageChangedNoti(true);
                        if (position < ThumbnailListManagerBase.this.getCount()) {
                            if (ThumbnailListManagerBase.this.getCount() == 0) {
                                ThumbnailListManagerBase.this.mGet.closeDetailViewAfterStartPreview();
                            }
                            if (ThumbnailListManagerBase.this.mCameraRollAnimationFrontView != null) {
                                ThumbnailListManagerBase.this.mCameraRollAnimationFrontView.setVisibility(4);
                            }
                            if (ThumbnailListManagerBase.this.mCameraRollAnimationRearView != null) {
                                ThumbnailListManagerBase.this.mCameraRollAnimationRearView.setVisibility(4);
                            }
                            if (ThumbnailListManagerBase.this.getCount() > position) {
                                ThumbnailListManagerBase.this.sendUpdateJPEGMsg(position);
                            }
                            ThumbnailListManagerBase.this.setSelectedPagerType();
                            ThumbnailListManagerBase.this.mGet.updateQuickClipForTilePreview(false, true, ThumbnailListManagerBase.this.getUri(ThumbnailListManagerBase.this.mCurrentPage));
                        }
                    }
                });
            }
        }
    }

    protected void setCurrentItemWithoutPageChangedNoti(boolean isDeleted) {
        CamLog.m3d(CameraConstants.TAG, "[Tile] setCurrentItemWithoutPageChangedNoti");
        if (this.mIsRTL) {
            int rtlPosition = getPagerPosition(this.mCurrentPage);
            this.mSkipPageChangedNotification = true;
            this.mPager.setCurrentItem(rtlPosition, false);
            if (this.mIsLastItemDeleted) {
                if (isDeleted) {
                    this.mCurrentPage--;
                }
                if (this.mAdapter != null) {
                    this.mAdapter.setSelectedItem(this.mCurrentPage);
                    this.mThumbnailListView.smoothScrollToPositionFromTop(this.mCurrentPage, (this.mThumbnailListView.getHeight() / 2) - (this.mThumbnailListView.getWidth() / 2));
                    if (this.mIsActivatedQuickDetailView) {
                        sendUpdateJPEGMsg(this.mCurrentPage);
                    }
                    this.mGet.updateQuickClipForTilePreview(false, true, getUri(this.mCurrentPage));
                }
                setSelectedPagerType();
            }
            this.mSkipPageChangedNotification = false;
        }
    }

    protected void addDeleteId() {
        try {
            if (this.mDeleteIds != null) {
                this.mDeleteIds.add(Long.valueOf(ContentUris.parseId(getUri(this.mCurrentPage))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUndoClicked() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] onUndoClicked");
        if (this.mDeleteIds != null) {
            this.mDeleteIds.clear();
        }
        if (isActivatedQuickdetailView()) {
            final RelativeLayout layout = (RelativeLayout) this.mGet.findViewById(C0088R.id.detail_animation_view_bg);
            if (layout != null) {
                layout.setVisibility(0);
            }
            makeUndoAnimataionBitmap(this.mUndoItem);
            startUndoAnimation(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (layout != null) {
                        layout.setVisibility(8);
                    }
                    ThumbnailListManagerBase.this.mCursor = ThumbnailListManagerBase.this.cursorLoader.loadInBackground();
                    ThumbnailListManagerBase.this.changeCursor(ThumbnailListManagerBase.this.mCursor);
                    ThumbnailListManagerBase.this.initFastThumbnailList(ThumbnailListManagerBase.this.mCursor);
                    if (ThumbnailListManagerBase.this.mIsLastItemDeleted) {
                        CamLog.m7i(CameraConstants.TAG, "[Tile] select last index..");
                        ThumbnailListManagerBase.this.mCurrentPage = ThumbnailListManagerBase.this.getCount() - 1;
                        ThumbnailListManagerBase.this.mAdapter.setSelectedItem(ThumbnailListManagerBase.this.mCurrentPage);
                        ThumbnailListManagerBase.this.mPager.setCurrentItem(ThumbnailListManagerBase.this.getPagerPosition(ThumbnailListManagerBase.this.mCurrentPage), false);
                        ThumbnailListManagerBase.this.mAdapter.notifyDataSetChanged();
                    }
                    ThumbnailListManagerBase.this.setCurrentItemWithoutPageChangedNoti(false);
                    ThumbnailListManagerBase.this.setSelectedPagerType();
                    ThumbnailListManagerBase.this.sendUpdateJPEGMsg(ThumbnailListManagerBase.this.mCurrentPage);
                }
            });
            return;
        }
        this.mCursor = this.cursorLoader.loadInBackground();
        changeCursor(this.mCursor);
        initFastThumbnailList(this.mCursor);
        this.mGet.updateThumbnail(true);
        ThumbnailListItem item = this.mUndoItem;
        if (item != null) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] item : " + item.path);
            this.mGet.updateQuickClipForTilePreview(true, true, item.mUri);
        }
    }

    public void onDeleteComplete(boolean isBurst, int deleteResult) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] onDeleteComplete isBurst : " + isBurst);
        if (this.mDeleteIds != null && this.mDeleteIds.size() > 0) {
            this.mFastThumbnailAdapter.removeItem(((Long) this.mDeleteIds.get(0)).longValue());
            this.mDeleteIds.remove(0);
        }
        if (isBurst) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (ThumbnailListManagerBase.this.getCount() != 1) {
                        ThumbnailListManagerBase.this.doDeleteItemOnList();
                    }
                    if (ThumbnailListManagerBase.this.mGet.getGifVisibleStatus()) {
                        ThumbnailListManagerBase.this.mGet.setGifVisibleStatus(false);
                        ThumbnailListManagerBase.this.mGet.setGIFVisibility(false);
                    }
                    ThumbnailListManagerBase.this.mCursor = ThumbnailListManagerBase.this.cursorLoader.loadInBackground();
                    ThumbnailListManagerBase.this.changeCursor(ThumbnailListManagerBase.this.mCursor);
                    ThumbnailListManagerBase.this.initFastThumbnailList(ThumbnailListManagerBase.this.mCursor);
                    if (ThumbnailListManagerBase.this.isActivatedQuickdetailView() && ThumbnailListManagerBase.this.getCount() == 0) {
                        ThumbnailListManagerBase.this.mGet.closeDetailViewAfterStartPreview();
                        ThumbnailListManagerBase.this.mFastThumbnailAdapter.resetItemList();
                    }
                }
            }, 50);
        }
        if (!(isActivatedQuickdetailView() || getCount() == 0)) {
            this.mGet.updateThumbnail(false);
        }
        if (this.mThumbnailListEmptyView != null && getCount() < 5 && isActivatedTilePreview()) {
            CamLog.m3d(CameraConstants.TAG, "[Tile] emptyview visible getCount() : " + getCount());
            this.mThumbnailListEmptyView.setVisibility(0);
        }
    }

    protected void makeDeleteAnimationBitmap(int nextPosition) {
        CamLog.m7i(CameraConstants.TAG, "[Roll] nextPosition : " + nextPosition + " / mCurrentPage : " + this.mCurrentPage);
        if (this.mCameraRollAnimationFrontView != null && this.mCameraRollAnimationRearView != null) {
            Uri deleteImgUri = getUri(this.mCurrentPage);
            if (deleteImgUri != null) {
                Uri nextImgUri;
                Bitmap deleteImgBmp = (Bitmap) this.mRotateCache.get(deleteImgUri);
                if (!(deleteImgBmp == null || deleteImgBmp.isRecycled())) {
                    this.mCameraRollAnimationFrontView.setImageBitmap(deleteImgBmp);
                }
                if (isLastIndex()) {
                    nextImgUri = getUri(this.mCurrentPage - 1);
                    this.mIsDelAnimDirectionRtoL = this.mIsRTL;
                } else {
                    nextImgUri = getUri(nextPosition);
                    this.mIsDelAnimDirectionRtoL = !this.mIsRTL;
                }
                if (nextImgUri != null) {
                    Bitmap nextImgBmp = (Bitmap) this.mRotateCache.get(nextImgUri);
                    if (nextImgBmp != null && !nextImgBmp.isRecycled()) {
                        this.mCameraRollAnimationRearView.setImageBitmap(nextImgBmp);
                    }
                }
            }
        }
    }

    protected void makeUndoAnimataionBitmap(ThumbnailListItem undoItem) {
        Uri currentPageUri = getUri(this.mCurrentPage);
        if (currentPageUri != null && undoItem != null) {
            Bitmap currentPageBmp = (Bitmap) this.mRotateCache.get(currentPageUri);
            if (!(currentPageBmp == null || currentPageBmp.isRecycled())) {
                this.mCameraRollAnimationFrontView.setImageBitmap(currentPageBmp);
            }
            Uri undoUri = undoItem.mUri;
            if (undoUri != null) {
                Bitmap undoBmp = (Bitmap) this.mRotateCache.get(undoUri);
                if (!(undoBmp == null || undoBmp.isRecycled())) {
                    this.mCameraRollAnimationRearView.setImageBitmap(undoBmp);
                }
                if (this.mIsLastItemDeleted) {
                    this.mIsDelAnimDirectionRtoL = this.mIsRTL;
                } else {
                    this.mIsDelAnimDirectionRtoL = !this.mIsRTL;
                }
            }
        }
    }

    protected void startUndoAnimation(AnimationListener animListener) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] startUndoAnimation count : " + getCount());
        if (getCount() != 0) {
            if (this.mIsDelAnimDirectionRtoL) {
                AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationRearView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), false, -1.0f, animListener);
                AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationFrontView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), true, -1.0f, null);
                return;
            }
            AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationRearView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), false, 1.0f, animListener);
            AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationFrontView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), true, 1.0f, null);
        }
    }

    protected void startDeleteAnimation(AnimationListener animListener) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] startDeleteAnimation");
        if (this.mIsDelAnimDirectionRtoL) {
            AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationRearView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), false, 1.0f, null);
            AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationFrontView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), true, 1.0f, animListener);
            return;
        }
        AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationRearView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), false, -1.0f, null);
        AnimationUtil.startCameraRollDeleteOrUndoAnimation(this.mCameraRollAnimationFrontView, this.mGet.getAppContext(), this.mGet.getOrientationDegree(), true, -1.0f, animListener);
    }

    private void includeData(MatrixCursor result, ThumbnailListItem item) {
        Cursor cursor = getCursor();
        if (cursor != null && item != null) {
            RowBuilder row;
            cursor.moveToFirst();
            if (!cursor.getString(cursor.getColumnIndexOrThrow("_data")).equals(item.path)) {
                CamLog.m5e(CameraConstants.TAG, "[Tile] row.add : ");
                row = result.newRow();
                row.add("_id", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("_id"))));
                row.add("_data", cursor.getString(cursor.getColumnIndexOrThrow("_data")));
                row.add("mime_type", cursor.getString(cursor.getColumnIndexOrThrow("mime_type")));
                row.add(CameraConstants.ORIENTATION, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CameraConstants.ORIENTATION))));
                row.add("datetaken", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("datetaken"))));
                row.add("_display_name", cursor.getString(cursor.getColumnIndexOrThrow("_display_name")));
                row.add(CameraConstants.MODE_COLUMN, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CameraConstants.MODE_COLUMN))));
                row.add("burst_id", cursor.getString(cursor.getColumnIndexOrThrow("burst_id")));
                row.add("num", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("num"))));
            }
            while (cursor.moveToNext()) {
                if (!cursor.getString(cursor.getColumnIndexOrThrow("_data")).equals(item.path)) {
                    row = result.newRow();
                    row.add("_id", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("_id"))));
                    row.add("_data", cursor.getString(cursor.getColumnIndexOrThrow("_data")));
                    row.add("mime_type", cursor.getString(cursor.getColumnIndexOrThrow("mime_type")));
                    row.add(CameraConstants.ORIENTATION, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CameraConstants.ORIENTATION))));
                    row.add("datetaken", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("datetaken"))));
                    row.add("_display_name", cursor.getString(cursor.getColumnIndexOrThrow("_display_name")));
                    row.add(CameraConstants.MODE_COLUMN, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CameraConstants.MODE_COLUMN))));
                    row.add("burst_id", cursor.getString(cursor.getColumnIndexOrThrow("burst_id")));
                    row.add("num", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("num"))));
                }
            }
        }
    }

    protected int getPagerPosition(int position) {
        int rtlPosition = position;
        if (!this.mIsRTL) {
            return rtlPosition;
        }
        rtlPosition = (getCount() - position) - 1;
        if (rtlPosition < 0) {
            return 0;
        }
        return rtlPosition;
    }

    protected int getCount() {
        if (this.mAdapter != null) {
            return this.mAdapter.getCount();
        }
        return 0;
    }

    protected Uri getUri() {
        if (canAccess()) {
            return ThumbnailListItem.fromCursor(this.mAdapter.getCursor()).mUri;
        }
        return null;
    }

    protected Uri getUri(int pos) {
        if (canAccess(pos)) {
            return ThumbnailListItem.fromCursor((Cursor) this.mAdapter.getItem(pos)).mUri;
        }
        return null;
    }

    protected String getMediaType() {
        if (canAccess()) {
            return ThumbnailListItem.fromCursor(this.mAdapter.getCursor()).mMediaType;
        }
        return null;
    }

    protected String getMediaType(int pos) {
        if (canAccess(pos)) {
            return ThumbnailListItem.fromCursor((Cursor) this.mAdapter.getItem(pos)).mMediaType;
        }
        return null;
    }

    protected ThumbnailListItem getItem() {
        if (canAccess()) {
            return ThumbnailListItem.fromCursor(this.mAdapter.getCursor());
        }
        return null;
    }

    protected ThumbnailListItem getItem(int pos) {
        if (canAccess(pos)) {
            return ThumbnailListItem.fromCursor((Cursor) this.mAdapter.getItem(pos));
        }
        return null;
    }

    protected Cursor getCursor() {
        if (canAccess()) {
            return this.mAdapter.getCursor();
        }
        return null;
    }

    protected Cursor getUndoCursor(ThumbnailListItem item) {
        MatrixCursor result = new MatrixCursor(DEFAULT_ROLL_PROJECTION);
        if (canAccess()) {
            includeData(result, item);
        }
        return result;
    }

    protected void undoDelete(ThumbnailListItem item) {
        if (canAccess()) {
            this.mAdapter.changeCursor(getUndoCursor(item));
        }
    }

    protected void changeCursor(Cursor cursor) {
        if (canSwap()) {
            this.mAdapter.changeCursor(cursor);
        }
    }

    protected boolean canAccess() {
        if (this.mAdapter == null || this.mAdapter.getCursor() == null || this.mAdapter.getCursor().isClosed() || this.mAdapter.getCursor().getCount() <= 0) {
            return false;
        }
        return true;
    }

    protected boolean canAccess(int pos) {
        if (this.mAdapter == null || this.mAdapter.getCursor() == null || this.mAdapter.getCursor().isClosed() || this.mAdapter.getCursor().getCount() <= 0 || this.mAdapter.getCursor().getCount() <= pos || pos < 0) {
            return false;
        }
        return true;
    }

    protected boolean canSwap() {
        if (this.mAdapter == null || this.mAdapter.getCursor() == null || this.mAdapter.getCursor().isClosed() || this.mAdapter.getCursor().getCount() < 0) {
            return false;
        }
        return true;
    }

    public void loadCursor() {
        if (!this.mGet.isSettingMenuVisible() && !this.mGet.checkUndoCurrentState(2)) {
            Bundle info = new Bundle();
            info.putString("bucket", FileUtil.getBucketIDStr(this.mGet.getDirPath(false)));
            info.putLong("time", sCurrentTime);
            if (this.cursorLoader == null) {
                this.cursorLoader = (ThumbnailLoader) this.mGet.getActivity().getLoaderManager().initLoader(255, info, this);
            } else {
                this.cursorLoader.forceLoad();
            }
            if (this.cursorLoader != null && !this.cursorLoader.isStarted()) {
                CamLog.m3d(CameraConstants.TAG, "[Tile] cursor loader is not started, instead LoadCursorTask is used.");
                new LoadCursorTask().execute(new Void[0]);
            }
        }
    }

    public void restartLoader() {
        Bundle info = new Bundle();
        info.putString("bucket", FileUtil.getBucketIDStr(this.mGet.getDirPath(false)));
        info.putLong("time", sCurrentTime);
        this.cursorLoader = (ThumbnailLoader) this.mGet.getActivity().getLoaderManager().restartLoader(255, info, this);
    }

    protected void initFastThumbnailList(Cursor cursor) {
        if (this.mFastThumbnailAdapter != null && cursor != null && !cursor.isClosed() && cursor.getCount() != 0) {
            ArrayList<ThumbnailListItem> itemList = new ArrayList();
            if (cursor.moveToFirst()) {
                for (int i = 0; i < 11; i++) {
                    itemList.add(i, ThumbnailListItem.fromCursor(cursor));
                    if (!cursor.moveToNext()) {
                        break;
                    }
                }
            }
            if (!this.mGet.isPaused()) {
                this.mFastThumbnailAdapter.setItemList(itemList);
            }
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle arg) {
        CamLog.m3d(CameraConstants.TAG, "[Tile] onCreateLoader");
        ThumbnailLoader loader = new ThumbnailLoader(this.mGet.getAppContext(), arg.getString("bucket"), arg.getLong("time"));
        loader.forceLoad();
        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        CamLog.m3d(CameraConstants.TAG, "[Tile] onLoadFinished");
        if (cursor == null || cursor.isClosed() || this.mGet.isPaused()) {
            new LoadCursorTask().execute(new Void[0]);
            return;
        }
        setEnableDeleteButton(true);
        CamLog.m3d(CameraConstants.TAG, "[Tile] cursor size : " + cursor.getCount());
        this.mCursor = cursor;
        initFastThumbnailList(cursor);
        if (this.mAdapter != null) {
            this.mAdapter.changeCursor(cursor);
        }
        if (this.mThumbnailListEmptyView != null && cursor.getCount() < 5 && !this.mGet.isSettingMenuVisible() && isActivatedTilePreview()) {
            CamLog.m3d(CameraConstants.TAG, "[Tile] emptyview visible getCount() : " + getCount());
            this.mThumbnailListEmptyView.setVisibility(0);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        CamLog.m3d(CameraConstants.TAG, "[Tile] onLoaderReset");
        if (this.mAdapter != null) {
            this.mAdapter.changeCursor(null);
        }
    }

    protected void addRecentItem(ThumbnailListItem item) {
        if (this.mRecentItems == null) {
            this.mRecentItems = new ArrayList();
        }
        this.mRecentItems.add(item);
        if (this.mRecentItems.size() > 5) {
            this.mRecentItems.remove(5);
        }
    }

    protected void setEnableDeleteButton(boolean enable) {
        if (this.mDeleteBtn == null) {
            return;
        }
        if (enable) {
            this.mDeleteBtn.setEnabled(true);
            this.mDeleteBtn.setAlpha(1.0f);
            return;
        }
        this.mDeleteBtn.setEnabled(false);
        this.mDeleteBtn.setAlpha(0.4f);
    }

    public void setDeleteButtonVisibility(boolean visibility) {
        if (this.mDeleteBtn != null) {
            this.mDeleteBtn.setVisibility(visibility ? 0 : 8);
        }
    }
}
