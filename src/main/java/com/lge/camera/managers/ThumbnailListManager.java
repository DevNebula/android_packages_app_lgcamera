package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.p000v4.view.ViewPager;
import android.support.p000v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.FastThumbnailAdapter;
import com.lge.camera.app.LGCameraApplication;
import com.lge.camera.app.ThumbnailAdapter;
import com.lge.camera.app.ThumbnailHelper;
import com.lge.camera.components.FastThumbnailListView;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.ThumbnailListView;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.ThumbnailListPagerAdapter.ThumbnailListPagerListener;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AppControlUtilBase;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;

public class ThumbnailListManager extends ThumbnailListManagerBase {
    protected OnClickListener mDeleteButtonClickListener = new C11469();
    protected OnItemClickListener mListClickListener = new C113310();
    protected OnPageChangeListener mPageChangeListener = new C11415();
    protected ThumbnailListPagerListener mThumbnailListPagerListener = new C11404();

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$10 */
    class C113310 implements OnItemClickListener {
        C113310() {
        }

        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            CamLog.m3d(CameraConstants.TAG, "onItemClick called position : " + position);
            if (ThumbnailListManager.this.mAdapter == null || ThumbnailListManager.this.mGet.isTimerShotCountdown() || ThumbnailListManager.this.mGet.getBurstProgress()) {
                CamLog.m7i(CameraConstants.TAG, "[Tile] onItemClick return");
            } else if (ThumbnailListManager.this.mGet == null || ThumbnailListManager.this.mGet.isSnapShotProcessing() || ThumbnailListManager.this.mGet.isModuleChanging() || ThumbnailListManager.this.mGet.getPreviewCoverVisibility() == 0 || !ThumbnailListManager.this.mGet.checkModuleValidate(192) || ThumbnailListManager.this.mGet.isActivatedQuickview() || ThumbnailListManager.this.mGet.isJogZoomMoving() || ThumbnailListManager.this.mGet.isZoomControllerTouched()) {
                CamLog.m7i(CameraConstants.TAG, "[Tile] onItemClick capture progress");
            } else if (ThumbnailListManager.this.getItem(position) != null) {
                ThumbnailListManager.this.setCurrentPagerPosition(arg0, position);
                ThumbnailListManager.this.setSelectedItem(position);
                ThumbnailListManager.this.setSelectedPagerType();
                if (ThumbnailListManager.this.mFastThumbnailListView != null) {
                    ThumbnailListManager.this.mFastThumbnailListView.setVisibility(4);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$1 */
    class C11361 implements OnTouchListener {
        C11361() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (ThumbnailListManager.this.mThumbnailListView != null) {
                ThumbnailListManager.this.mThumbnailListView.dispatchTouchEvent(event);
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$2 */
    class C11372 implements OnScrollListener {
        C11372() {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (ThumbnailListManager.this.mFastThumbnailListView != null && firstVisibleItem >= 5) {
                ThumbnailListManager.this.mFastThumbnailListView.setVisibility(4);
            }
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$4 */
    class C11404 implements ThumbnailListPagerListener {
        C11404() {
        }

        public void onClicked() {
            CamLog.m7i(CameraConstants.TAG, "[Tile] Pager click ");
            ThumbnailListManager.this.mGet.updateQuickClipForTilePreview(false, true, ThumbnailListManager.this.getUri(ThumbnailListManager.this.mCurrentPage));
            SystemBarUtil.hideSystemUI(ThumbnailListManager.this.mGet.getActivity());
        }
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$5 */
    class C11415 implements OnPageChangeListener {
        C11415() {
        }

        public void onPageSelected(int currentPage) {
            if (!ThumbnailListManager.this.mSkipPageChangedNotification && ThumbnailListManager.this.isActivatedQuickdetailView()) {
                TouchImageView view = ThumbnailListManager.this.mPagerAdapter.getSpecficView(ThumbnailListManager.this.getPagerPosition(ThumbnailListManager.this.mCurrentPage));
                if (view != null && view.isZoomed()) {
                    view.resetZoom();
                }
                ThumbnailListManager.this.mCurrentPage = ThumbnailListManager.this.getPagerPosition(currentPage);
                CamLog.m7i(CameraConstants.TAG, "[Tile] onPageSelected mCurrentPage " + ThumbnailListManager.this.mCurrentPage);
                if (ThumbnailListManager.this.mAdapter != null) {
                    ThumbnailListManager.this.mAdapter.setSelectedItem(ThumbnailListManager.this.mCurrentPage);
                    ThumbnailListManager.this.mThumbnailListView.smoothScrollToPositionFromTop(ThumbnailListManager.this.mCurrentPage, (ThumbnailListManager.this.mThumbnailListView.getHeight() / 2) - (ThumbnailListManager.this.mThumbnailListView.getWidth() / 2));
                    if (ThumbnailListManager.this.mIsActivatedQuickDetailView) {
                        ThumbnailListManager.this.sendUpdateJPEGMsg(ThumbnailListManager.this.mCurrentPage);
                    }
                    if (ThumbnailListManager.this.mGet.checkUndoCurrentState(2)) {
                        ThumbnailListManager.this.mGet.deleteImmediatelyNotUndo();
                    }
                }
                ThumbnailListManager.this.setSelectedPagerType();
            }
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int scrollState) {
            if ((scrollState == 1 || scrollState == 2) && ThumbnailListManager.this.mThreadHandler != null) {
                ThumbnailListManager.this.mThreadHandler.removeMessages(0);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$7 */
    class C11447 implements OnClickListener {
        C11447() {
        }

        public void onClick(View v) {
            if (ThumbnailListManager.this.mCurrentPage < ThumbnailListManager.this.getCount()) {
                ThumbnailListManager.this.mGet.setLaunchGalleryLocation(new float[]{0.95f, 0.18f});
                ThumbnailListManager.this.mGet.launchGallery(ThumbnailListManager.this.getUri(ThumbnailListManager.this.mCurrentPage), ThumbnailListManager.this.mGalleryPlayType);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$8 */
    class C11458 implements OnClickListener {
        C11458() {
        }

        public void onClick(View arg0) {
            CamLog.m3d(CameraConstants.TAG, "backCover clicked");
            ThumbnailListManager.this.mGet.closeDetailViewAfterStartPreview();
        }
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListManager$9 */
    class C11469 implements OnClickListener {
        C11469() {
        }

        public void onClick(View arg0) {
            CamLog.m5e(CameraConstants.TAG, "[Tile] delete button click");
            if (ThumbnailListManager.this.mGet.getMediaSaveService().getQueueCount() > 0) {
                ThumbnailListManager.this.mGet.showToast(ThumbnailListManager.this.mGet.getAppContext().getResources().getString(C0088R.string.saving_files_try_later), CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            } else if (ThumbnailListManager.this.mGet.checkInterval(5)) {
                ThumbnailListItem item = ThumbnailListManager.this.getItem(ThumbnailListManager.this.mCurrentPage);
                if (item == null) {
                    CamLog.m5e(CameraConstants.TAG, "[Tile] item is null");
                } else if (FileManager.isFileExist(item.path)) {
                    ThumbnailListManager.this.deleteOrUndo(item);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "[Tile] delete item is not exist - refresh cursor");
                    if (ThumbnailListManager.this.cursorLoader != null) {
                        ThumbnailListManager.this.mCursor = ThumbnailListManager.this.cursorLoader.loadInBackground();
                        ThumbnailListManager.this.changeCursor(ThumbnailListManager.this.mCursor);
                    }
                    if (ThumbnailListManager.this.getCount() == 0) {
                        ThumbnailListManager.this.mGet.closeDetailViewAfterStartPreview();
                    }
                }
            }
        }
    }

    public ThumbnailListManager(TilePreviewInterface tilePreviewInterface) {
        super(tilePreviewInterface);
    }

    public void initLayout() {
        if (this.mFakePreview != null) {
            this.mFakePreview.setVisibility(8);
        }
        if (this.mAdapter == null || this.mThumbnailListView == null || this.mPager == null || this.mPagerAdapter == null || this.mTilePreviewLayout == null || this.mFastThumbnailListView == null) {
            this.mIsFromInitLayout = true;
            this.mThumbHelper = new ThumbnailHelper(this.mGet.getAppContext(), this.mGet.getActivity());
            this.mFakePreview = (ImageView) this.mGet.findViewById(C0088R.id.fake_preview);
            this.mTilePreviewLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.thumbnail_listview_rotate_layout);
            this.mAdapter = new ThumbnailAdapter(this.mGet.getAppContext(), null, this.mThumbHelper, this.mGet);
            this.mAdapter.setAutoRequery(false);
            this.mFastThumbnailAdapter = new FastThumbnailAdapter(this.mGet.getAppContext(), this.mGet, this.mThumbHelper);
            loadCursor();
            this.mPager = (ViewPager) this.mGet.findViewById(C0088R.id.thumbnail_list_view_pager);
            this.mPagerAdapter = new ThumbnailListPagerAdapter(this.mGet.getAppContext(), this.mGet.getActivity(), this.mThumbnailListPagerListener, this.mAdapter, this.mThumbHelper);
            this.mPagerAdapter.setBucketId(FileUtil.getBucketIDStr(this.mGet.getDirPath(false)));
            this.mAdapter.setPagerAdapter(this.mPagerAdapter);
            if (this.mPager != null) {
                this.mPager.setOffscreenPageLimit(3);
                this.mPager.setPageMargin(50);
                this.mPager.setPageMarginDrawable(17170444);
                this.mPager.setAdapter(this.mPagerAdapter);
                this.mPager.setOnPageChangeListener(this.mPageChangeListener);
            }
            setListView();
            this.mThumbnailListCoverView = this.mGet.findViewById(C0088R.id.thumbnail_listview_black_cover);
            this.mThumbnailListEmptyView = (LinearLayout) this.mGet.findViewById(C0088R.id.thumbnail_listview_empty_view);
            if (this.mThumbnailListEmptyView != null) {
                setEmptyListViewParamForRtl();
                if (getCount() > 5) {
                    this.mThumbnailListEmptyView.setVisibility(8);
                }
            }
            this.mRotateCache = LGCameraApplication.getRotateCache(this.mGet.getAppContext());
            setEventListenerForFastThumbnail();
            setTilePreviewExtraView();
            setTilePreviewLayout();
            setDegree(this.mGet.getOrientationDegree(), true);
            View thumbnailListRootView = this.mGet.findViewById(C0088R.id.thumbnail_list_root_view);
            View thumbnailPagerRootView = this.mGet.findViewById(C0088R.id.thumbnail_pager_root_view);
            if (thumbnailListRootView != null && thumbnailPagerRootView != null) {
                LayoutParams thumbnailListRootViewParam = (LayoutParams) thumbnailListRootView.getLayoutParams();
                LayoutParams thumbnailPagerRootViewParam = (LayoutParams) thumbnailPagerRootView.getLayoutParams();
                thumbnailListRootViewParam.topMargin = RatioCalcUtil.getNotchDisplayHeight(this.mGet.getAppContext());
                thumbnailPagerRootViewParam.topMargin = RatioCalcUtil.getNotchDisplayHeight(this.mGet.getAppContext());
                if (thumbnailListRootViewParam != null && thumbnailPagerRootViewParam != null) {
                    thumbnailListRootView.setLayoutParams(thumbnailListRootViewParam);
                    thumbnailPagerRootView.setLayoutParams(thumbnailPagerRootViewParam);
                    return;
                }
                return;
            }
            return;
        }
        loadCursor();
    }

    protected void setListView() {
        this.mThumbnailListView = (ThumbnailListView) this.mGet.findViewById(C0088R.id.thumbnail_listview);
        if (this.mThumbnailListView != null) {
            this.mThumbnailListView.setAdapter(this.mAdapter);
            this.mThumbnailListView.setFriction(ViewConfiguration.getScrollFriction() * 5.0f);
            this.mThumbnailListView.setOnItemClickListener(this.mListClickListener);
        }
        this.mFastThumbnailListView = (FastThumbnailListView) this.mGet.findViewById(C0088R.id.fast_thumbnail_listview);
        if (this.mFastThumbnailListView != null) {
            this.mFastThumbnailListView.setAdapter(this.mFastThumbnailAdapter);
            this.mFastThumbnailListView.setFriction(ViewConfiguration.getScrollFriction() * 5.0f);
        }
        if (this.mIsRTL && this.mThumbnailListView != null && this.mFastThumbnailListView != null) {
            this.mThumbnailListView.setRotation(180.0f);
            this.mFastThumbnailListView.setRotation(180.0f);
        }
    }

    protected void setEmptyListViewParamForRtl() {
        if (this.mThumbnailListEmptyView != null) {
            View firstEmptyView = this.mThumbnailListEmptyView.findViewById(C0088R.id.first_empty_view);
            View lastEmptyView = this.mThumbnailListEmptyView.findViewById(C0088R.id.last_empty_view);
            if (firstEmptyView != null && lastEmptyView != null) {
                LinearLayout.LayoutParams firstLP = (LinearLayout.LayoutParams) firstEmptyView.getLayoutParams();
                LinearLayout.LayoutParams lastLP = (LinearLayout.LayoutParams) lastEmptyView.getLayoutParams();
                if (this.mIsRTL) {
                    firstLP.weight = 1.0f;
                    lastLP.weight = 2.0f;
                } else {
                    firstLP.weight = 2.0f;
                    lastLP.weight = 1.0f;
                }
                firstEmptyView.setLayoutParams(firstLP);
                lastEmptyView.setLayoutParams(lastLP);
            }
        }
    }

    protected void setEventListenerForFastThumbnail() {
        if (this.mFastThumbnailListView != null) {
            this.mFastThumbnailListView.setOnTouchListener(new C11361());
            this.mFastThumbnailListView.setOnScrollListener(new C11372());
        }
    }

    protected void initImageHandlerThread() {
        if (this.mImageHandlerThread == null) {
            this.mImageHandlerThread = new HandlerThread("CameraRoll_ImageHandler_Thread");
            this.mImageHandlerThread.start();
            this.mThreadHandler = new Handler(this.mImageHandlerThread.getLooper()) {
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {
                        CamLog.m7i(CameraConstants.TAG, "[Tile] receive MSG_DECODE_JPEG");
                        ThumbnailListManager thumbnailListManager = ThumbnailListManager.this;
                        thumbnailListManager.mThreadCount++;
                        final int curPosition = msg.arg1;
                        int modeColumn = msg.arg2;
                        if (ThumbnailListManager.this.getCount() != 0) {
                            if ("video".equals(ThumbnailListManager.this.getMediaType(curPosition))) {
                                thumbnailListManager = ThumbnailListManager.this;
                                thumbnailListManager.mThreadCount--;
                                return;
                            }
                            final Uri uri = msg.obj;
                            final Bitmap bmp = ThumbnailListManager.this.getBitmap(uri, modeColumn);
                            if (bmp == null) {
                                thumbnailListManager = ThumbnailListManager.this;
                                thumbnailListManager.mThreadCount--;
                                return;
                            }
                            ThumbnailListManager.this.mGet.postOnUiThread(new HandlerRunnable(ThumbnailListManager.this) {
                                public void handleRun() {
                                    CamLog.m3d(CameraConstants.TAG, "[Tile] currentItem = " + curPosition + " / mCurrentPage = " + ThumbnailListManager.this.mCurrentPage + " / mThreadCount : " + ThumbnailListManager.this.mThreadCount);
                                    Uri currentPageUri = ThumbnailListManager.this.getUri(ThumbnailListManager.this.mCurrentPage);
                                    ThumbnailListManager thumbnailListManager;
                                    if (curPosition == ThumbnailListManager.this.mCurrentPage && ThumbnailListManager.this.mThreadCount <= 1 && uri.equals(currentPageUri)) {
                                        TouchImageView view = ThumbnailListManager.this.mPagerAdapter.getSpecficView(ThumbnailListManager.this.getPagerPosition(ThumbnailListManager.this.mCurrentPage));
                                        if (!(view == null || bmp.isRecycled())) {
                                            CamLog.m7i(CameraConstants.TAG, "[Tile] update original image");
                                            ViewUtil.clearImageViewDrawableOnly(view);
                                            view.setVisibility(0);
                                            view.setImageBitmap(bmp);
                                        }
                                        thumbnailListManager = ThumbnailListManager.this;
                                        thumbnailListManager.mThreadCount--;
                                        return;
                                    }
                                    if (!(bmp == null || bmp.isRecycled())) {
                                        bmp.recycle();
                                    }
                                    CamLog.m7i(CameraConstants.TAG, "[Tile] skip original image update");
                                    thumbnailListManager = ThumbnailListManager.this;
                                    thumbnailListManager.mThreadCount--;
                                }
                            }, 0);
                        }
                    }
                }
            };
        }
    }

    public void thumbnailListInit() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] thumbnailListInit");
        if (!FunctionProperties.isSupportedCameraRoll() || "off".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] camera roll off. return");
            return;
        }
        initLayout();
        if (!this.mGet.checkModuleValidate(192)) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] recording...Do not refresh ListView..");
        } else if (this.mGet.isSettingMenuVisible()) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] return. Setting Menu Visible");
        } else if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
            if (this.mThumbnailListEmptyView != null) {
                if (getCount() >= 5 || !isActivatedTilePreview()) {
                    this.mThumbnailListEmptyView.setVisibility(8);
                } else {
                    CamLog.m3d(CameraConstants.TAG, "[Tile] emptyview visible getCount() : " + getCount());
                    this.mThumbnailListEmptyView.setVisibility(0);
                }
            }
            setThumbnailListAnimation(this.mThumbnailListView, true);
        } else {
            setThumbnailListAnimation(this.mThumbnailListView, false);
        }
    }

    public void showTilePreviewCoverView(final boolean show) {
        if (this.mThumbnailListCoverView != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {

                /* renamed from: com.lge.camera.managers.ThumbnailListManager$6$1 */
                class C11431 implements OnTouchListener {
                    C11431() {
                    }

                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                }

                public void handleRun() {
                    if (show && ThumbnailListManager.this.isActivatedTilePreview()) {
                        ThumbnailListManager.this.mThumbnailListCoverView.setVisibility(0);
                        ThumbnailListManager.this.mThumbnailListCoverView.setOnTouchListener(new C11431());
                        return;
                    }
                    ThumbnailListManager.this.mThumbnailListCoverView.setVisibility(8);
                    ThumbnailListManager.this.mThumbnailListCoverView.setOnTouchListener(null);
                }
            });
        }
    }

    protected void setTilePreviewExtraView() {
        this.mAniFrontView = (RotateLayout) this.mGet.findViewById(C0088R.id.camera_roll_animation_front_view_layout);
        this.mAniRearView = (RotateLayout) this.mGet.findViewById(C0088R.id.camera_roll_animation_rear_view_layout);
        this.mCameraRollAnimationRearView = (ImageView) this.mGet.findViewById(C0088R.id.camera_roll_animation_rear_view);
        this.mCameraRollAnimationFrontView = (ImageView) this.mGet.findViewById(C0088R.id.camera_roll_animation_front_view);
        if (!(this.mCameraRollAnimationFrontView == null || this.mCameraRollAnimationRearView == null)) {
            this.mCameraRollAnimationRearView.setVisibility(4);
            this.mCameraRollAnimationFrontView.setVisibility(4);
        }
        this.mDeleteBtn = (RotateImageButton) this.mGet.findViewById(C0088R.id.thumbnail_pager_button_delete);
        if (this.mDeleteBtn != null) {
            LayoutParams lp = (LayoutParams) this.mDeleteBtn.getLayoutParams();
            if (lp != null) {
                lp.bottomMargin = RatioCalcUtil.getCommandBottomMargin(this.mGet.getAppContext());
                lp.width = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.review_thumbnail.size);
                lp.height = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.review_thumbnail.size);
                this.mDeleteBtn.setLayoutParams(lp);
            }
            this.mDeleteBtn.setOnClickListener(this.mDeleteButtonClickListener);
        }
        this.mPagerBadge = (RotateImageButton) this.mGet.findViewById(C0088R.id.thumbnail_pager_badge);
        if (this.mPagerBadge != null) {
            this.mPagerBadge.setOnClickListener(new C11447());
        }
        this.mBackCover = (ImageView) this.mGet.findViewById(C0088R.id.thumbnail_list_backcover);
        if (this.mBackCover != null) {
            this.mBackCover.setOnClickListener(new C11458());
        }
    }

    protected void deleteOrUndo(ThumbnailListItem item) {
        String burstId = null;
        if (item.mIsBurstShot) {
            this.mUndoItem = item;
            burstId = item.mBurstId;
        } else {
            addDeleteId();
            if (getCount() != 1) {
                doDeleteItemOnList();
            }
            if (isLastIndex()) {
                this.mIsLastItemDeleted = true;
            } else {
                this.mIsLastItemDeleted = false;
            }
            this.mUndoItem = item;
            undoDelete(item);
            if (getCount() == 0) {
                this.mGet.closeDetailViewAfterStartPreview();
            }
        }
        this.mGet.deleteOrUndo(item.mUri, burstId, this);
    }

    public void onResume() {
        long j = 0;
        CamLog.m7i(CameraConstants.TAG, "[Tile] onResume isActivatedTilePreview : " + isActivatedTilePreview() + " / isActivatedQuickdetailView : " + isActivatedQuickdetailView() + " / mCurrentPage : " + this.mCurrentPage + " /  sCurrentTime : " + sCurrentTime);
        if (sCurrentTime == 0) {
            if (SecureImageUtil.useSecureLockImage()) {
                j = System.currentTimeMillis() / 1000;
                sCurrentTime = j;
            }
            sCurrentTime = j;
        }
        CamLog.m7i(CameraConstants.TAG, "[Tile] sCurrentTime : " + sCurrentTime);
        if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW)) && this.cursorLoader != null) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] refresh Cursor");
            this.cursorLoader.setCurrentTimeSec(sCurrentTime);
        }
        checkRTLMode();
        initImageHandlerThread();
        showTilePreviewCoverView(false);
        if (this.mThumbnailListView != null) {
            setIsActiviatedTilePreview(this.mThumbnailListView.getVisibility() == 0);
        }
        if (this.mGet != null) {
            this.mGet.updateQuickClipForTilePreview(false, true, getUri(this.mCurrentPage));
        }
        if (isActivatedQuickdetailView()) {
            setEnableDeleteButton(false);
            if (this.mThumbnailListView != null) {
                this.mThumbnailListView.smoothScrollToPosition(this.mCurrentPage);
                setSelectedItem(this.mCurrentPage);
                ThumbnailListItem item = getItem(this.mCurrentPage);
                String path = null;
                if (item != null) {
                    path = item.path;
                }
                if (FileManager.isFileExist(path)) {
                    sendUpdateJPEGMsg(this.mCurrentPage);
                    setSelectedPagerType();
                    return;
                }
                closeDetailView();
                return;
            }
            setSelectedItem(-1);
        }
    }

    protected void checkRTLMode() {
        this.mIsRTL = Utils.isRTLLanguage();
        CamLog.m3d(CameraConstants.TAG, "[Tile] is RTL? : " + this.mIsRTL);
    }

    public void onStop() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] onStop");
        sCurrentTime = 0;
    }

    public void setDegree(int degree, boolean init) {
        this.mDegree = degree;
        RotateLayout layout = (RotateLayout) this.mGet.findViewById(C0088R.id.thumbnail_listview_rotate_layout);
        if (layout != null) {
            layout.rotateLayout(90);
        }
        RotateLayout pagerLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.tile_preview_rotate_layout);
        if (pagerLayout != null) {
            int beforeDegree = pagerLayout.getAngle();
            if (pagerLayout.getVisibility() == 0 && beforeDegree != degree) {
                pagerLayout.clearAnimation();
                AnimationUtil.startRotateAnimationForRotateLayout(pagerLayout, beforeDegree, degree, false, 300, null);
                pagerLayout.rotateLayout(degree);
            }
        }
        RotateLayout aniLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.detail_animation_view_layout);
        if (aniLayout != null) {
            aniLayout.rotateLayout(this.mDegree);
        }
        if (!(this.mAniFrontView == null || this.mAniRearView == null)) {
            this.mAniFrontView.rotateLayout(degree);
            this.mAniRearView.rotateLayout(degree);
        }
        setDegreeOfComponent(init);
    }

    protected void setDegreeOfComponent(boolean init) {
        if (!(this.mAdapter == null || this.mFastThumbnailAdapter == null)) {
            if (init) {
                this.mAdapter.setDegree(this.mDegree);
                this.mFastThumbnailAdapter.setDegree(this.mDegree);
            } else if (!(this.mThumbnailListView == null || this.mFastThumbnailListView == null)) {
                if (this.mPagerAdapter != null) {
                    this.mPagerAdapter.notifyDataSetChanged();
                }
                this.mAdapter.rotateThumbnailListView(this.mDegree, this.mThumbnailListView);
                this.mFastThumbnailAdapter.rotateFastThumbnailListView(this.mDegree, this.mFastThumbnailListView);
            }
        }
        if (this.mFastThumbnailAdapter != null) {
            this.mFastThumbnailAdapter.setDegree(this.mDegree);
        }
        if (this.mPagerAdapter != null) {
            this.mPagerAdapter.setDegree(this.mDegree);
        }
        if (this.mDeleteBtn != null) {
            this.mDeleteBtn.setDegree(this.mDegree, true);
        }
        if (this.mPagerBadge != null) {
            this.mPagerBadge.setDegree(this.mDegree, true);
        }
        if (this.mIsActivatedQuickDetailView) {
            sendUpdateJPEGMsg(this.mCurrentPage);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        if (FunctionProperties.isSupportedCameraRoll()) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] onConfigurationChanged");
            this.mTilePreviewLayout = null;
            this.mThumbnailListView = null;
            this.mFastThumbnailListView = null;
            initLayout();
            if ("on".equals(this.mGet.getCurSettingValue(Setting.KEY_TILE_PREVIEW))) {
                showTilePreview(true);
            }
            if (this.mIsActivatedQuickDetailView) {
                this.mGet.closeDetailViewAfterStartPreview();
            }
        }
    }

    public void removeFile(int position) {
        FileManager.deleteFile(this.mGet.getAppContext(), getUri(position));
    }

    public void releaseResource() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] releaseResource");
        if (this.mThumbnailListView != null) {
            this.mThumbnailListView.setOnItemClickListener(null);
            this.mThumbnailListView = null;
        }
        if (this.mFastThumbnailListView != null) {
            this.mFastThumbnailListView.setOnTouchListener(null);
            this.mFastThumbnailListView.setOnScrollListener(null);
            this.mFastThumbnailListView = null;
        }
        if (this.mFastThumbnailAdapter != null) {
            this.mFastThumbnailAdapter = null;
        }
        if (this.mAdapter != null) {
            this.mAdapter = null;
        }
        if (this.mPagerAdapter != null) {
            this.mPagerAdapter = null;
        }
        if (this.mPager != null) {
            this.mPager.setOnPageChangeListener(null);
            this.mPager = null;
        }
        if (this.mDeleteBtn != null) {
            this.mDeleteBtn.setOnClickListener(null);
            this.mDeleteBtn = null;
        }
        if (!(this.mFakePreview == null && this.mThumbnailListCoverView == null && this.mThumbnailListEmptyView == null)) {
            this.mFakePreview = null;
            this.mThumbnailListCoverView = null;
            this.mThumbnailListEmptyView = null;
        }
        if (this.mCameraRollAnimationFrontView != null || this.mCameraRollAnimationRearView != null || this.mAniFrontView != null || this.mAniRearView != null) {
            this.mCameraRollAnimationFrontView = null;
            this.mCameraRollAnimationRearView = null;
            this.mAniFrontView = null;
            this.mAniRearView = null;
        }
    }

    protected void setCurrentPagerPosition(AdapterView<?> arg0, int position) {
        ThumbnailListItem item = getItem(position);
        if (this.mPager != null && item != null) {
            this.mCurrentPage = position;
            int pager_position = getPagerPosition(position);
            CamLog.m3d(CameraConstants.TAG, "[Tile] pager_position : " + pager_position);
            if (isActivatedQuickdetailView()) {
                if (pager_position == this.mPager.getCurrentItem()) {
                    if (this.mGet.checkUndoCurrentState(2)) {
                        this.mGet.deleteImmediatelyNotUndo();
                    }
                    this.mGet.closeDetailViewAfterStartPreview();
                } else {
                    this.mGet.updateQuickClipForTilePreview(false, true, item.mUri);
                }
                this.mPager.setCurrentItem(pager_position, false);
                return;
            }
            this.mGet.updateQuickClipForTilePreview(false, true, item.mUri);
            readyAnimation(arg0.getChildAt(getClickedItemPosition(arg0, position)), this.mPager, true, false);
        }
    }

    public void closeDetailView() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] closeDetailView");
        this.mIsActivatedQuickDetailView = false;
        if (this.mFakePreview != null && this.mFakePreview.getVisibility() == 0) {
            this.mFakePreview.setVisibility(8);
        }
        setSelectedPagerType();
        if (this.mGet.checkUndoCurrentState(2) && getCount() != 0) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] force delete...");
            this.mGet.deleteImmediatelyNotUndo();
        }
        showDetailViewLayout(false, false);
        this.mGet.removeStopPreviewMessage();
    }

    public void closeDetailViewBySecureUnlock() {
    }

    public void turnOnTilePreview(boolean isOn) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] turnOnTilePreview isOn : " + isOn);
    }

    public void setThumbnailListEnable(boolean enable) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] setThumbnailListEnable enable : " + enable);
        if (this.mThumbnailListView != null) {
            this.mThumbnailListView.setEnabled(enable);
        }
    }

    public void showTilePreview(boolean enable) {
        if (FunctionProperties.isSupportedCameraRoll()) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] showTilePreview enable : " + enable);
            if (this.mThumbnailListView != null && this.mAdapter != null && this.mFastThumbnailListView != null) {
                if (getCount() == 0 && this.mFastThumbnailAdapter != null) {
                    this.mFastThumbnailAdapter.resetItemList();
                    if (!(this.mDeleteIds == null || this.mDeleteIds.size() == 0)) {
                        this.mDeleteIds.clear();
                    }
                }
                if (enable) {
                    initLayout();
                    setThumbnailListAnimation(this.mThumbnailListView, true);
                    setIsActiviatedTilePreview(true);
                    if (isActivatedQuickdetailView()) {
                        setSelectedItem(this.mCurrentPage);
                        return;
                    }
                    return;
                }
                this.mFastThumbnailListView.setVisibility(4);
                setThumbnailListAnimation(this.mThumbnailListView, false);
                setIsActiviatedTilePreview(false);
            }
        }
    }

    protected void setThumbnailListAnimation(View thumbnailListView, boolean show) {
        CamLog.m3d(CameraConstants.TAG, "setThumbnailListAnimation-start");
        int visibility = this.mThumbnailListView.getVisibility();
        if ((show && visibility == 0) || ((!show && visibility == 8) || thumbnailListView == null || this.mFastThumbnailListView == null)) {
            CamLog.m3d(CameraConstants.TAG, "thumbnail list animation is not started");
            return;
        }
        Animation showAni = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_in);
        Animation hideAni = AnimationUtils.loadAnimation(this.mGet.getAppContext(), C0088R.anim.fade_out);
        if (this.mGet.getActivity().isFinishing() || this.mGet.isPaused()) {
            thumbnailListView.clearAnimation();
            thumbnailListView.setVisibility(4);
            this.mFastThumbnailListView.setVisibility(4);
            return;
        }
        Animation animation;
        if (show) {
            animation = showAni;
        } else {
            animation = hideAni;
        }
        startThumbnailListAnimation(thumbnailListView, show, animation);
    }

    protected void startThumbnailListAnimation(final View thumbnailListView, final boolean show, Animation animation) {
        thumbnailListView.clearAnimation();
        thumbnailListView.setVisibility(4);
        this.mFastThumbnailListView.setVisibility(4);
        if (animation != null) {
            animation.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    CamLog.m3d(CameraConstants.TAG, "- animation start - show : " + show + " getCount() : " + ThumbnailListManager.this.getCount());
                    if (!show || ThumbnailListManager.this.getCount() >= 5) {
                        ThumbnailListManager.this.mThumbnailListEmptyView.setVisibility(8);
                    } else {
                        ThumbnailListManager.this.mThumbnailListEmptyView.setVisibility(0);
                    }
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    CamLog.m3d(CameraConstants.TAG, "- animation end -");
                    if (thumbnailListView != null && ThumbnailListManager.this.mFastThumbnailListView != null) {
                        if (show) {
                            if (ThumbnailListManager.this.mIsFromInitLayout) {
                                ThumbnailListManager.this.mFastThumbnailListView.setVisibility(0);
                                ThumbnailListManager.this.mIsFromInitLayout = false;
                            }
                            thumbnailListView.setVisibility(0);
                        } else {
                            ThumbnailListManager.this.mGet.runOnUiThread(new HandlerRunnable(ThumbnailListManager.this) {
                                public void handleRun() {
                                    int visibile = 0;
                                    ThumbnailListManager.this.showTilePreviewCoverView(false);
                                    if (!ThumbnailListManager.this.mGet.getBurstProgress()) {
                                        visibile = 8;
                                    }
                                    if (ThumbnailListManager.this.isActivatedTilePreview()) {
                                        ThumbnailListManager.this.mThumbnailListCoverView.setVisibility(visibile);
                                    }
                                    thumbnailListView.setVisibility(8);
                                    ThumbnailListManager.this.mFastThumbnailListView.setVisibility(8);
                                }
                            });
                        }
                        ThumbnailListManager.this.setIsActiviatedTilePreview(show);
                    }
                }
            });
            if (this.mFastThumbnailListView.getFirstVisiblePosition() > 0) {
                thumbnailListView.startAnimation(animation);
            } else {
                this.mFastThumbnailListView.startAnimation(animation);
            }
        }
    }

    public void onPause() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] onPause");
        if (this.mGet.checkUndoCurrentState(2)) {
            this.mGet.deleteImmediatelyNotUndo();
        }
        if (this.cursorLoader != null) {
            this.cursorLoader.cancelLoadInBackground();
        }
        if (this.mFastThumbnailListView != null) {
            this.mFastThumbnailListView.setVisibility(4);
        }
        if (this.mFastThumbnailAdapter != null) {
            this.mFastThumbnailAdapter.resetItemList();
        }
        this.mImageHandlerThread = null;
        if (this.mThreadHandler != null) {
            this.mThreadHandler.removeCallbacksAndMessages(null);
            this.mThreadHandler.getLooper().quit();
            this.mThreadHandler = null;
        }
        if (isActivatedQuickdetailView() && !AppControlUtil.isGalleryLaunched() && !AppControlUtilBase.isVideoLaunched() && !AppControlUtilBase.isShareActivityLaunched()) {
            setActivatedQuickDetailView(false);
            FrameLayout layout = (FrameLayout) this.mGet.findViewById(C0088R.id.thumbnail_list_layout);
            if (layout != null) {
                layout.setVisibility(8);
            }
            setSelectedItem(-1);
            if (this.mGet != null) {
                this.mGet.removeStopPreviewMessage();
            }
        }
    }

    public void onDestroy() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] onDestroy");
        try {
            ListPreference listPref = this.mGet.getListPreference(Setting.KEY_TILE_PREVIEW);
            if (listPref != null && "on".equals(listPref.loadSavedValue())) {
                new LoadThumbnailTask().execute(new Void[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mThumbnailListView != null) {
            int count = this.mThumbnailListView.getChildCount();
            for (int i = 0; i < count; i++) {
                cancelThumbnailTask(this.mThumbnailListView.getChildAt(i));
            }
        }
        if (!(this.mCursor == null || this.mCursor.isClosed())) {
            this.mCursor.close();
            this.mCursor = null;
        }
        if (this.cursorLoader != null) {
            this.cursorLoader.reset();
            this.cursorLoader = null;
        }
        releaseResource();
    }

    protected void cancelThumbnailTask(View view) {
        ImageView microThumb = (ImageView) view.findViewById(C0088R.id.thumbnail_list_item_image_micro);
        ImageView miniThumb = (ImageView) view.findViewById(C0088R.id.thumbnail_list_item_image);
        if (microThumb != null) {
            this.mThumbHelper.stopLoading(microThumb);
        }
        if (miniThumb != null) {
            this.mThumbHelper.stopLoading(miniThumb);
        }
    }

    public void refreshAdaptersByQuickView() {
        loadCursor();
    }
}
