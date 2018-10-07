package com.lge.camera.managers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.p000v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.app.SecureThumbnailAdapter;
import com.lge.camera.app.SecureThumbnailPagerAdapter;
import com.lge.camera.app.ThumbnailLoader;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.file.FileManager;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SecureImageUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class SecureThumbnailListManager extends ThumbnailListManager {
    protected OnClickListener mDeleteButtonClickListener = new C11181();
    protected SecureThumbnailAdapter mSecureAdapter = null;
    protected OnPageChangeListener mSecurePageChangeListener = new C11236();

    /* renamed from: com.lge.camera.managers.SecureThumbnailListManager$1 */
    class C11181 implements OnClickListener {
        C11181() {
        }

        public void onClick(View arg0) {
            CamLog.m5e(CameraConstants.TAG, "[Tile] delete button click");
            if (SecureThumbnailListManager.this.mGet.checkInterval(5)) {
                ThumbnailListItem item = SecureThumbnailListManager.this.getItem(SecureThumbnailListManager.this.mCurrentPage);
                if (item == null) {
                    CamLog.m5e(CameraConstants.TAG, "[Tile] item is null");
                } else if (FileManager.isFileExist(item.path)) {
                    SecureThumbnailListManager.this.deleteOrUndo(item);
                } else {
                    CamLog.m7i(CameraConstants.TAG, "[Tile] delete item is not exist - refresh cursor");
                    SecureThumbnailListManager.this.refreshAdapters((ArrayList) SecureImageUtil.get().getSecureThumbnailList().clone());
                    if (SecureThumbnailListManager.this.getCount() == 0) {
                        SecureThumbnailListManager.this.mGet.closeDetailViewAfterStartPreview();
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.SecureThumbnailListManager$6 */
    class C11236 implements OnPageChangeListener {
        C11236() {
        }

        public void onPageSelected(int currentPage) {
            if (!SecureThumbnailListManager.this.mSkipPageChangedNotification && SecureThumbnailListManager.this.isActivatedQuickdetailView()) {
                SecureThumbnailListManager.this.mCurrentPage = SecureThumbnailListManager.this.getPagerPosition(currentPage);
                CamLog.m7i(CameraConstants.TAG, "[Tile] onPageSelected mCurrentPage " + SecureThumbnailListManager.this.mCurrentPage);
                if (SecureThumbnailListManager.this.mSecureAdapter != null) {
                    SecureThumbnailListManager.this.mSecureAdapter.setSelectedItem(SecureThumbnailListManager.this.mCurrentPage);
                    SecureThumbnailListManager.this.mThumbnailListView.smoothScrollToPositionFromTop(SecureThumbnailListManager.this.mCurrentPage, (SecureThumbnailListManager.this.mThumbnailListView.getHeight() / 2) - (SecureThumbnailListManager.this.mThumbnailListView.getWidth() / 2));
                    if (SecureThumbnailListManager.this.mIsActivatedQuickDetailView) {
                        SecureThumbnailListManager.this.sendUpdateJPEGMsg(SecureThumbnailListManager.this.mCurrentPage);
                    }
                    if (SecureThumbnailListManager.this.mGet.checkUndoCurrentState(2)) {
                        SecureThumbnailListManager.this.mGet.deleteImmediatelyNotUndo();
                    }
                    SecureThumbnailListManager.this.mGet.updateQuickClipForTilePreview(false, true, SecureThumbnailListManager.this.getUri(SecureThumbnailListManager.this.mCurrentPage));
                }
                SecureThumbnailListManager.this.setSelectedPagerType();
            }
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int scrollState) {
            if ((scrollState == 1 || scrollState == 2) && SecureThumbnailListManager.this.mThreadHandler != null) {
                SecureThumbnailListManager.this.mThreadHandler.removeMessages(0);
            }
        }
    }

    class SupplementItemInfo extends AsyncTask<ArrayList<ThumbnailListItem>, Void, ArrayList<ThumbnailListItem>> {
        SupplementItemInfo() {
        }

        protected ArrayList<ThumbnailListItem> doInBackground(ArrayList<ThumbnailListItem>... arg0) {
            long startTime = SystemClock.uptimeMillis();
            ArrayList<ThumbnailListItem> items = arg0[0];
            if (items == null) {
                return null;
            }
            HashSet<ThumbnailListItem> deletedItems = new HashSet();
            Iterator it = items.iterator();
            while (it.hasNext()) {
                ThumbnailListItem item = (ThumbnailListItem) it.next();
                if (item.mUri != null && (item.path == null || "".equals(item.path.trim()))) {
                    ContentResolver cr = SecureThumbnailListManager.this.mGet.getAppContext().getContentResolver();
                    item.path = FileUtil.getRealPathFromURI(SecureThumbnailListManager.this.mGet.getAppContext(), item.mUri);
                    String mediaType = cr.getType(item.mUri);
                    if (mediaType != null) {
                        item.mMediaType = mediaType;
                        item.mIsImage = item.mMediaType.contains("image");
                    }
                    if (item.mIsImage) {
                        item.mOri = FileUtil.getOrientationFromDB(cr, item.mUri);
                    }
                    CamLog.m3d(CameraConstants.TAG, "[Tile] SupplementItemInfo path : " + item.path);
                }
                if (!(deletedItems == null || items == null || item == null || item.path == null)) {
                    File file = new File(item.path);
                    if (!(file == null || file.exists())) {
                        deletedItems.add(item);
                    }
                }
            }
            Iterator<ThumbnailListItem> iterator = deletedItems.iterator();
            while (iterator.hasNext()) {
                items.remove((ThumbnailListItem) iterator.next());
            }
            if (items.size() == 0) {
                items = null;
            }
            CamLog.m3d(CameraConstants.TAG, "[Tile] ellapsed time : " + (SystemClock.uptimeMillis() - startTime));
            return items;
        }

        protected void onPostExecute(ArrayList<ThumbnailListItem> items) {
            SecureThumbnailListManager.this.refreshAdapters(items);
        }
    }

    class UpdateItemListTask extends AsyncTask<Void, Void, ArrayList<ThumbnailListItem>> {
        UpdateItemListTask() {
        }

        protected ArrayList<ThumbnailListItem> doInBackground(Void... arg0) {
            ArrayList<ThumbnailListItem> items = (ArrayList) SecureImageUtil.get().getSecureThumbnailList().clone();
            if (items == null || items.size() == 0) {
                return null;
            }
            String[] ids = new String[items.size()];
            int idx = 0;
            Iterator it = items.iterator();
            while (it.hasNext()) {
                ids[idx] = Long.toString(((ThumbnailListItem) it.next()).f33id);
                idx++;
            }
            ArrayList<ThumbnailListItem> updatedItems = new ArrayList();
            Cursor cursor = new ThumbnailLoader(SecureThumbnailListManager.this.mGet.getAppContext(), null, 0).getCursorWithIds(ids);
            CamLog.m3d(CameraConstants.TAG, "[Tile] cursor : " + cursor);
            if (cursor != null) {
                CamLog.m3d(CameraConstants.TAG, "[Tile] cursor size : " + cursor.getCount());
                Iterator it2 = items.iterator();
                while (it2.hasNext()) {
                    ThumbnailListItem item = (ThumbnailListItem) it2.next();
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        if (cursor.getLong(cursor.getColumnIndexOrThrow("_id")) == item.f33id) {
                            updatedItems.add(item);
                            break;
                        }
                        cursor.moveToNext();
                    }
                }
            }
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
            }
            CamLog.m3d(CameraConstants.TAG, "[Tile] updatedItems size : " + updatedItems.size());
            return updatedItems;
        }

        protected void onPostExecute(ArrayList<ThumbnailListItem> items) {
            SecureThumbnailListManager.this.refreshAdapters(items);
            SecureThumbnailListManager.this.setEnableDeleteButton(true);
        }
    }

    public SecureThumbnailListManager(TilePreviewInterface tilePreviewInterface) {
        super(tilePreviewInterface);
    }

    public void initLayout() {
        super.initLayout();
        if (this.mSecureAdapter != null) {
            loadCursor();
            return;
        }
        this.mSecureAdapter = new SecureThumbnailAdapter(this.mGet.getAppContext(), this.mThumbHelper, this.mGet);
        this.mPagerAdapter = new SecureThumbnailPagerAdapter(this.mGet.getAppContext(), this.mGet.getActivity(), this.mThumbnailListPagerListener, this.mSecureAdapter, this.mThumbHelper);
        this.mSecureAdapter.setPagerAdapter(this.mPagerAdapter);
        this.mPagerAdapter.setBucketId(FileUtil.getBucketIDStr(this.mGet.getDirPath(false)));
        this.mPagerAdapter.setSecureTime(Long.toString(System.currentTimeMillis() / 1000));
        if (this.mThumbnailListView != null) {
            this.mThumbnailListView.setAdapter(this.mSecureAdapter);
        }
        if (this.mPager != null) {
            this.mPager.setAdapter(this.mPagerAdapter);
            this.mPager.setOnPageChangeListener(this.mSecurePageChangeListener);
        }
        setDegree(this.mGet.getOrientationDegree(), true);
        if (this.mDeleteBtn != null) {
            this.mDeleteBtn.setOnClickListener(this.mDeleteButtonClickListener);
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void thumbnailListInit() {
        super.thumbnailListInit();
        new UpdateItemListTask().execute(new Void[0]);
    }

    protected int getCount() {
        if (this.mSecureAdapter != null) {
            return this.mSecureAdapter.getCount();
        }
        return 0;
    }

    protected Uri getUri(int pos) {
        Uri result = null;
        if (this.mSecureAdapter == null) {
            return null;
        }
        ArrayList<ThumbnailListItem> items = this.mSecureAdapter.getItemList();
        if (items != null && items.size() > pos) {
            result = ((ThumbnailListItem) items.get(pos)).mUri;
        }
        return result;
    }

    protected String getMediaType(int pos) {
        ArrayList<ThumbnailListItem> items = this.mSecureAdapter.getItemList();
        if (items == null || items.size() <= pos) {
            return null;
        }
        return ((ThumbnailListItem) items.get(pos)).mMediaType;
    }

    protected ThumbnailListItem getItem(int pos) {
        ArrayList<ThumbnailListItem> items = this.mSecureAdapter.getItemList();
        CamLog.m3d(CameraConstants.TAG, "[Tile] items size : " + items.size());
        if (items == null || items.size() <= pos) {
            return null;
        }
        ThumbnailListItem result = (ThumbnailListItem) items.get(pos);
        CamLog.m3d(CameraConstants.TAG, "[Tile] item's uri : " + result.mUri);
        return result;
    }

    protected void undoDelete(ThumbnailListItem item) {
        this.mSecureAdapter.removeItem(item.f33id);
        this.mFastThumbnailAdapter.removeItem(item.f33id);
        SecureImageUtil.get().removeSecureLockUri(item.mUri);
        SecureImageUtil.get().removeSecureThumbnailItem(item.f33id);
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
                    if (SecureThumbnailListManager.this.mIsLastItemDeleted) {
                        CamLog.m7i(CameraConstants.TAG, "[Tile] select last index..");
                        SecureThumbnailListManager.this.mCurrentPage = SecureThumbnailListManager.this.getCount();
                        SecureThumbnailListManager.this.mSecureAdapter.addItem(SecureThumbnailListManager.this.mUndoItem, SecureThumbnailListManager.this.mCurrentPage);
                        SecureThumbnailListManager.this.mSecureAdapter.setSelectedItem(SecureThumbnailListManager.this.mCurrentPage);
                        SecureThumbnailListManager.this.mPager.setCurrentItem(SecureThumbnailListManager.this.getPagerPosition(SecureThumbnailListManager.this.mCurrentPage), false);
                        SecureThumbnailListManager.this.mIsDelAnimDirectionRtoL = false;
                    } else {
                        SecureThumbnailListManager.this.mSecureAdapter.addItem(SecureThumbnailListManager.this.mUndoItem, SecureThumbnailListManager.this.mCurrentPage);
                        SecureThumbnailListManager.this.mIsDelAnimDirectionRtoL = true;
                    }
                    SecureThumbnailListManager.this.mFastThumbnailAdapter.setItemList(SecureThumbnailListManager.this.mSecureAdapter.getItemList());
                    SecureImageUtil.get().addSecureLockImageUri(SecureThumbnailListManager.this.mUndoItem.mUri, SecureThumbnailListManager.this.mCurrentPage);
                    SecureImageUtil.get().addSecureThumbnailItem(SecureThumbnailListManager.this.mUndoItem, SecureThumbnailListManager.this.mCurrentPage);
                    SecureThumbnailListManager.this.mGet.updateThumbnail(true);
                    SecureThumbnailListManager.this.setCurrentItemWithoutPageChangedNoti(false);
                    SecureThumbnailListManager.this.setSelectedPagerType();
                    SecureThumbnailListManager.this.sendUpdateJPEGMsg(SecureThumbnailListManager.this.mCurrentPage);
                }
            });
            return;
        }
        ThumbnailListItem item = this.mUndoItem;
        if (item != null) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] item : " + item.path);
            this.mGet.updateQuickClipForTilePreview(true, true, item.mUri);
            this.mSecureAdapter.addItem(item, this.mCurrentPage);
            this.mFastThumbnailAdapter.setItemList(this.mSecureAdapter.getItemList());
            SecureImageUtil.get().addSecureLockImageUri(item.mUri, this.mCurrentPage);
            SecureImageUtil.get().addSecureThumbnailItem(item, this.mCurrentPage);
            this.mGet.updateThumbnail(true);
        }
    }

    public void onDeleteComplete(boolean isBurst, int deleteResult) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] onDeleteComplete isBurst : " + isBurst);
        if (this.mDeleteIds != null && this.mDeleteIds.size() > 0) {
            CamLog.m3d(CameraConstants.TAG, "[Tile] delete id : " + ((Long) this.mDeleteIds.get(0)).longValue());
            CamLog.m3d(CameraConstants.TAG, "secureimageutil size : " + SecureImageUtil.get().getSecureThumbnailList().size());
            CamLog.m3d(CameraConstants.TAG, "secureadapter size : " + getCount());
            this.mDeleteIds.remove(0);
        }
        final long delete_id = this.mUndoItem.f33id;
        if (isBurst) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SecureThumbnailListManager.this.getCount() != 1) {
                        SecureThumbnailListManager.this.doDeleteItemOnList();
                    }
                    SecureThumbnailListManager.this.mSecureAdapter.removeItem(delete_id);
                    SecureThumbnailListManager.this.mFastThumbnailAdapter.removeItem(delete_id);
                    ThumbnailListItem item = SecureImageUtil.get().getSecureThumbnailItem(delete_id);
                    if (item != null) {
                        SecureImageUtil.get().removeSecureLockUri(item.mUri);
                    }
                    SecureImageUtil.get().removeSecureThumbnailItem(delete_id);
                    if (SecureThumbnailListManager.this.mGet.getGifVisibleStatus()) {
                        SecureThumbnailListManager.this.mGet.setGifVisibleStatus(false);
                        SecureThumbnailListManager.this.mGet.setGIFVisibility(false);
                    }
                    if (SecureThumbnailListManager.this.isActivatedQuickdetailView() && SecureThumbnailListManager.this.getCount() == 0) {
                        SecureThumbnailListManager.this.mGet.closeDetailViewAfterStartPreview();
                        SecureThumbnailListManager.this.mFastThumbnailAdapter.resetItemList();
                    }
                }
            }, 50);
        }
        if (!isActivatedQuickdetailView()) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    SecureThumbnailListManager.this.mGet.updateThumbnail(false);
                }
            }, 100);
        }
        if (this.mThumbnailListEmptyView != null && getCount() < 5 && isActivatedTilePreview()) {
            this.mThumbnailListEmptyView.setVisibility(0);
        }
    }

    protected void addRecentItem(final ThumbnailListItem item) {
        super.addRecentItem(item);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SecureImageUtil.get().addSecureThumbnailItem(item);
            }
        }, 200);
    }

    protected void setSelectedItem(int position) {
        if (this.mSecureAdapter != null) {
            this.mSecureAdapter.setSelectedItem(position);
        }
    }

    public void loadCursor() {
        new SupplementItemInfo().execute(new ArrayList[]{(ArrayList) SecureImageUtil.get().getSecureThumbnailList().clone()});
    }

    protected void setDegreeOfComponent(boolean init) {
        CamLog.m3d(CameraConstants.TAG, "[Tile] init : " + init);
        CamLog.m3d(CameraConstants.TAG, "[Tile] mSecureAdapter : " + this.mSecureAdapter + " mFastThumbnailAdapter : " + this.mFastThumbnailAdapter);
        if (!(this.mSecureAdapter == null || this.mFastThumbnailAdapter == null)) {
            if (init) {
                this.mSecureAdapter.setDegree(this.mDegree);
                this.mFastThumbnailAdapter.setDegree(this.mDegree);
            } else if (!(this.mThumbnailListView == null || this.mFastThumbnailListView == null)) {
                if (this.mPagerAdapter != null) {
                    this.mPagerAdapter.notifyDataSetChanged();
                }
                this.mSecureAdapter.rotateThumbnailListView(this.mDegree, this.mThumbnailListView);
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

    protected boolean isLastIndex() {
        if (this.mSecureAdapter == null || this.mSecureAdapter.getCount() - 1 != this.mCurrentPage) {
            return false;
        }
        return true;
    }

    public void closeDetailViewBySecureUnlock() {
        CamLog.m7i(CameraConstants.TAG, "[Tile] closeDetailViewBySecureUnlock");
        this.mIsActivatedQuickDetailView = false;
        if (this.mFakePreview != null && this.mFakePreview.getVisibility() == 0) {
            this.mFakePreview.setVisibility(8);
        }
        setSelectedPagerType();
        if (this.mGet.checkUndoCurrentState(2) && getCount() != 0) {
            CamLog.m7i(CameraConstants.TAG, "[Tile] force delete...");
            this.mGet.deleteImmediatelyNotUndo();
        }
        showDetailViewLayout(false, true);
        this.mGet.removeStopPreviewMessage();
    }

    public void refreshAdaptersByQuickView() {
        ArrayList<ThumbnailListItem> items = (ArrayList) SecureImageUtil.get().getSecureThumbnailList().clone();
        if (items != null && items.size() >= 1) {
            items.remove(0);
            SecureImageUtil.get().removeSecureThumbnailItem(0);
            refreshAdapters(items);
        }
    }

    public void restartLoader() {
    }

    private void refreshAdapters(ArrayList<ThumbnailListItem> items) {
        int limit = 11;
        if (items == null) {
            items = new ArrayList();
        }
        SecureImageUtil.get().setSecureThumbnailList(items);
        if (this.mSecureAdapter != null && this.mFastThumbnailAdapter != null) {
            this.mSecureAdapter.setItemList(items);
            ArrayList<ThumbnailListItem> thumbnailList = new ArrayList();
            if (items.size() <= 11) {
                limit = items.size();
            }
            for (int i = 0; i < limit; i++) {
                thumbnailList.add(items.get(i));
            }
            this.mFastThumbnailAdapter.setItemList(thumbnailList);
            if (this.mThumbnailListEmptyView != null && this.mSecureAdapter.getCount() < 5 && !this.mGet.isSettingMenuVisible() && isActivatedTilePreview()) {
                this.mThumbnailListEmptyView.setVisibility(0);
            }
        }
    }
}
