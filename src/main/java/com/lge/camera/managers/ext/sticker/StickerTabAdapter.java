package com.lge.camera.managers.ext.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.os.Message;
import android.support.p001v7.widget.RecyclerView.Adapter;
import android.support.p001v7.widget.RecyclerView.LayoutParams;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.arcsoft.stickerlibrary.utils.ZipUtil;
import com.lge.camera.C0088R;
import com.lge.camera.managers.ext.sticker.TabViewHolder.OnViewHolderClickListener;
import com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler;
import com.lge.camera.managers.ext.sticker.contents.scheduler.DeleteContentScheduler;
import com.lge.camera.managers.ext.sticker.recentdb.RecentDBHelper;
import com.lge.camera.managers.ext.sticker.utils.DecompressingFilenameFilter;
import com.lge.camera.managers.ext.sticker.utils.IconFilenameFilter;
import com.lge.camera.managers.ext.sticker.utils.OriginFilenameFilter;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StickerTabAdapter extends Adapter<TabViewHolder> implements OnViewHolderClickListener {
    private static final int MSG_FINISH_LOAD_LIST = 1;
    private static final int MSG_FINISH_LOAD_ONE = 2;
    private static final String TAG = "StickerTabAdapter";
    private Context mCtx;
    private boolean mCurrentIsDeleteMode = false;
    private int mDegree;
    private ColorMatrixColorFilter mGrayScaleFilter;
    private ArrayList<TabItem> mList;
    private final TabItemCallback mListener;
    private TabLoader mLoader;
    private Handler mLoaderHandler = new C13481();
    private int mSelectedPosition = -1;

    public interface TabItemCallback {
        void onCheckEditStatus(boolean z);

        void onTabItemClicked(String str, String str2);

        void onTabItemDeleted(int i);

        void onTabListLoadCompleted();
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.StickerTabAdapter$1 */
    class C13481 extends Handler {
        C13481() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    if (msg.obj != null) {
                        StickerTabAdapter.this.mList.clear();
                        StickerTabAdapter.this.mList = null;
                        StickerTabAdapter.this.mList = (ArrayList) msg.obj;
                        StickerTabAdapter.this.notifyDataSetChanged();
                        StickerTabAdapter.this.mListener.onTabListLoadCompleted();
                        StickerTabAdapter.this.mListener.onCheckEditStatus(!StickerTabAdapter.this.isRemainDeletableItem());
                        break;
                    }
                    break;
                case 2:
                    StickerTabAdapter.this.mList.add((TabItem) msg.obj);
                    StickerTabAdapter.this.notifyItemInserted(StickerTabAdapter.this.mList.size() - 1);
                    TabItemCallback access$600 = StickerTabAdapter.this.mListener;
                    if (StickerTabAdapter.this.isRemainDeletableItem()) {
                        z = false;
                    }
                    access$600.onCheckEditStatus(z);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public class TabItem {
        public boolean isDecompressing = false;
        public boolean mCanDeleted = false;
        public Bitmap tab_icon_normal;
        public int tab_icon_resource_normal = -1;
        public String tab_name;
        public String tab_path;

        public TabItem(String name, String icpath_normal) {
            this.tab_name = name;
            this.tab_icon_normal = BitmapFactory.decodeFile(icpath_normal);
            this.tab_icon_resource_normal = -1;
            this.mCanDeleted = true;
            init();
        }

        public TabItem(String name) {
            this.tab_name = name;
            this.tab_icon_normal = null;
            this.tab_icon_resource_normal = -1;
            this.mCanDeleted = true;
            init();
        }

        public TabItem(String name, int icon_res_normal) {
            this.tab_name = name;
            this.tab_icon_normal = null;
            this.tab_icon_resource_normal = icon_res_normal;
            this.mCanDeleted = true;
            init();
        }

        private void init() {
            if ("recent".equals(this.tab_name)) {
                this.mCanDeleted = false;
            } else if (this.tab_path != null && this.tab_path.startsWith(StickerTabAdapter.this.mCtx.getFilesDir().getAbsolutePath() + "/Sticker")) {
                this.mCanDeleted = false;
            }
            this.isDecompressing = false;
        }
    }

    private class TabLoader {
        private String PRE_INSTALLED_PATH = (StickerTabAdapter.this.mCtx.getFilesDir().getAbsolutePath() + "/PreloadedSticker");
        private LoadListThread mLoadListThread;
        private ThreadPoolExecutor mTPE = new ThreadPoolExecutor(2, 10, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
        private String stickerListPath = (StickerTabAdapter.this.mCtx.getFilesDir().getAbsolutePath() + "/Sticker");

        private class LoadListThread extends Thread {
            public boolean isRunning;
            ArrayList<TabItem> newList;

            /* renamed from: com.lge.camera.managers.ext.sticker.StickerTabAdapter$TabLoader$LoadListThread$1 */
            class C13491 implements FileFilter {
                C13491() {
                }

                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            }

            /* renamed from: com.lge.camera.managers.ext.sticker.StickerTabAdapter$TabLoader$LoadListThread$2 */
            class C13502 implements Comparator<File> {
                C13502() {
                }

                public int compare(File o1, File o2) {
                    return o1.compareTo(o2);
                }
            }

            private LoadListThread() {
                this.newList = new ArrayList();
                this.isRunning = false;
            }

            /* synthetic */ LoadListThread(TabLoader x0, C13481 x1) {
                this();
            }

            public void run() {
                this.isRunning = true;
                CamLog.m3d(StickerTabAdapter.TAG, "start tab load");
                this.newList.add(new TabItem("recent", (int) C0088R.drawable.btn_sticker_tab_recent_pressed));
                loadinner(TabLoader.this.PRE_INSTALLED_PATH);
                loadinner(TabLoader.this.stickerListPath);
                CamLog.m3d(StickerTabAdapter.TAG, "end tab load loaded count = " + this.newList.size());
                if (this.newList != null && this.newList.size() > 0) {
                    Message msg = StickerTabAdapter.this.mLoaderHandler.obtainMessage(1);
                    msg.obj = this.newList;
                    StickerTabAdapter.this.mLoaderHandler.sendMessage(msg);
                }
                this.isRunning = false;
            }

            private void loadinner(String path) {
                File[] eachFolder = new File(path).listFiles(new C13491());
                if (eachFolder != null && eachFolder.length > 0) {
                    Arrays.sort(eachFolder, new C13502());
                    for (File each : eachFolder) {
                        if (each.exists() && each.isDirectory()) {
                            File[] isDecompressing = each.listFiles(new DecompressingFilenameFilter());
                            if (isDecompressing == null || isDecompressing.length <= 0 || !isDecompressing[0].exists()) {
                                File[] icon = each.listFiles(new IconFilenameFilter());
                                if (!(icon == null || icon.length <= 0 || icon[0] == null)) {
                                    TabItem ti;
                                    if (TabLoader.this.PRE_INSTALLED_PATH.equals(path)) {
                                        if (each.getName().equals("00") || each.getName().equals("01")) {
                                            ti = new TabItem(StickerTabAdapter.this.mCtx.getResources().getString(C0088R.string.sticker_fancy_masks), icon[0].getAbsolutePath());
                                        } else if (each.getName().equals("02")) {
                                            ti = new TabItem(StickerTabAdapter.this.mCtx.getResources().getString(C0088R.string.sticker_funny_face), icon[0].getAbsolutePath());
                                        } else if (each.getName().equals("03")) {
                                            ti = new TabItem(StickerTabAdapter.this.mCtx.getResources().getString(C0088R.string.sticker_background), icon[0].getAbsolutePath());
                                        } else {
                                            ti = new TabItem(each.getName(), icon[0].getAbsolutePath());
                                        }
                                        ti.mCanDeleted = false;
                                        ti.tab_path = each.getAbsolutePath();
                                        this.newList.add(ti);
                                    } else {
                                        String name = each.getName();
                                        File[] origin = each.listFiles(new OriginFilenameFilter());
                                        if (!(origin == null || origin.length <= 0 || origin[0] == null)) {
                                            String temp = origin[0].getName().replace(".zip.origin", "");
                                            int index = temp.lastIndexOf("_");
                                            if (index > 0) {
                                                name = temp.substring(0, index);
                                            } else {
                                                name = temp;
                                            }
                                            ti = new TabItem(name, icon[0].getAbsolutePath());
                                            ti.mCanDeleted = true;
                                            ti.tab_path = each.getAbsolutePath();
                                            this.newList.add(ti);
                                        }
                                    }
                                }
                            } else {
                                CamLog.m5e(StickerTabAdapter.TAG, "decompressing exist!!!!");
                                if (DecompressScheduler.getInstance(StickerTabAdapter.this.mCtx).getIsDecompressing(each.getName())) {
                                    CamLog.m5e(StickerTabAdapter.TAG, "pass because this is decompressing now");
                                } else {
                                    CamLog.m5e(StickerTabAdapter.TAG, "should rerequest for decompressing");
                                }
                            }
                        }
                    }
                }
            }
        }

        private class LoadOneThread implements Runnable {
            private String tab_path;

            public LoadOneThread(String path) {
                this.tab_path = path.replace(ZipUtil.EXT, "");
            }

            public void run() {
                File targetFolder = new File(this.tab_path);
                if (targetFolder.exists() && targetFolder.isDirectory()) {
                    String name = this.tab_path.substring(this.tab_path.lastIndexOf("/"));
                    File[] icon = targetFolder.listFiles(new IconFilenameFilter());
                    if (icon != null && icon.length > 0 && icon[0] != null) {
                        File[] origin = targetFolder.listFiles(new OriginFilenameFilter());
                        if (origin != null && origin.length > 0 && origin[0] != null) {
                            String temp = origin[0].getName().replace(".zip.origin", "");
                            int index = temp.lastIndexOf("_");
                            if (index > 0) {
                                name = temp.substring(0, index);
                            } else {
                                name = temp;
                            }
                            TabItem ti = new TabItem(name, icon[0].getAbsolutePath());
                            ti.tab_path = this.tab_path;
                            ti.mCanDeleted = true;
                            ti.isDecompressing = false;
                            Message msg = StickerTabAdapter.this.mLoaderHandler.obtainMessage(2);
                            msg.obj = ti;
                            StickerTabAdapter.this.mLoaderHandler.sendMessage(msg);
                        }
                    }
                }
            }
        }

        public void load() {
            if (this.mLoadListThread == null || !this.mLoadListThread.isRunning) {
                this.mLoadListThread = new LoadListThread(this, null);
                this.mLoadListThread.start();
                return;
            }
            CamLog.m3d(StickerTabAdapter.TAG, "now loading.....");
        }

        public void loadOne(String path) {
            this.mTPE.execute(new LoadOneThread(path));
        }
    }

    public void onClicked(int pos) {
        if (pos == -1) {
            CamLog.m5e(TAG, "position error");
        } else if (!this.mCurrentIsDeleteMode) {
            int before = this.mSelectedPosition;
            this.mSelectedPosition = pos;
            notifyItemChanged(this.mSelectedPosition);
            notifyItemChanged(before);
            this.mListener.onTabItemClicked(((TabItem) this.mList.get(pos)).tab_name, ((TabItem) this.mList.get(pos)).tab_path);
        } else if (((TabItem) this.mList.get(pos)).mCanDeleted) {
            this.mListener.onTabItemDeleted(pos);
        }
    }

    public StickerTabAdapter(Context ctx, TabItemCallback listener) {
        this.mCtx = ctx;
        this.mList = new ArrayList();
        this.mListener = listener;
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.0f);
        this.mGrayScaleFilter = new ColorMatrixColorFilter(matrix);
    }

    public TabViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TabViewHolder(LayoutInflater.from(this.mCtx).inflate(C0088R.layout.sticker_tab_item, parent, false));
    }

    public void onBindViewHolder(TabViewHolder holder, int position) {
        LayoutParams lp;
        if (position == 0) {
            lp = (LayoutParams) holder.itemView.getLayoutParams();
            lp.leftMargin = this.mCtx.getResources().getDimensionPixelSize(C0088R.dimen.sticker_tab_first_item_left_margin);
            lp.rightMargin = this.mCtx.getResources().getDimensionPixelSize(C0088R.dimen.sticker_tab_margin_side);
            holder.itemView.setLayoutParams(lp);
        } else {
            lp = (LayoutParams) holder.itemView.getLayoutParams();
            lp.leftMargin = this.mCtx.getResources().getDimensionPixelSize(C0088R.dimen.sticker_tab_margin_side);
            lp.rightMargin = this.mCtx.getResources().getDimensionPixelSize(C0088R.dimen.sticker_tab_margin_side);
            holder.itemView.setLayoutParams(lp);
        }
        TabItem ti = (TabItem) this.mList.get(position);
        if (position == 0) {
            holder.itemView.setContentDescription(this.mCtx.getString(C0088R.string.sticker_recnet));
        } else {
            holder.itemView.setContentDescription(ti.tab_name);
        }
        if (ti.tab_icon_normal != null) {
            holder.mIcon.setImageBitmap(((TabItem) this.mList.get(position)).tab_icon_normal);
        } else if (ti.tab_icon_resource_normal != -1) {
            holder.mIcon.setImageResource(ti.tab_icon_resource_normal);
        }
        if (this.mSelectedPosition == position) {
            holder.mIcon.setColorFilter(null);
            holder.mBottomLine.setVisibility(0);
        } else {
            holder.mIcon.setColorFilter(this.mGrayScaleFilter);
            holder.mBottomLine.setVisibility(8);
        }
        if (!this.mCurrentIsDeleteMode) {
            holder.mDeleteIcon.setVisibility(8);
        } else if (((TabItem) this.mList.get(position)).mCanDeleted) {
            holder.mDeleteIcon.setVisibility(0);
        } else {
            holder.mDeleteIcon.setVisibility(8);
        }
        if (ti.isDecompressing) {
            holder.mIcon.setVisibility(4);
        } else {
            holder.mIcon.setVisibility(0);
        }
        holder.mIcon.setDegree(this.mDegree, false);
        holder.mDeleteIcon.setDegree(this.mDegree, false);
        holder.setOnViewHolderClickListener(this);
    }

    public String deleteTab(int index) {
        CamLog.m3d(TAG, "deleteTab index = " + index);
        String path = ((TabItem) this.mList.get(index)).tab_path;
        RecentDBHelper.getInstance(this.mCtx).deleteRecord(path);
        this.mList.remove(index);
        notifyItemRemoved(index);
        this.mSelectedPosition = 0;
        DeleteContentScheduler dcs = DeleteContentScheduler.getInstance(this.mCtx);
        if (dcs != null) {
            dcs.excuteJob(path);
        }
        if (!isRemainDeletableItem()) {
            deleteOff();
        }
        notifyItemChanged(this.mSelectedPosition);
        this.mListener.onTabItemClicked(((TabItem) this.mList.get(this.mSelectedPosition)).tab_name, ((TabItem) this.mList.get(this.mSelectedPosition)).tab_path);
        return path;
    }

    private void resetSelectedPosition() {
    }

    public int getItemCount() {
        return this.mList.size();
    }

    public void loadOne(String path) {
        if (this.mLoader != null) {
            this.mLoader.loadOne(path);
        }
    }

    public void loadList() {
        if (this.mLoader == null) {
            this.mLoader = new TabLoader();
        }
        this.mLoader.load();
    }

    public void setDegreeJustSet(int degree) {
        this.mDegree = degree;
    }

    public void setDegree(int degree) {
        boolean rotate = degree != this.mDegree;
        this.mDegree = degree;
        if (rotate) {
            notifyDataSetChanged();
        }
    }

    public void setSelection(int position) {
        boolean needChange = this.mSelectedPosition != position;
        this.mSelectedPosition = position;
        if (needChange) {
            notifyDataSetChanged();
        }
    }

    public void deleteOnOff() {
        boolean z = false;
        if (isRemainDeletableItem()) {
            if (!this.mCurrentIsDeleteMode) {
                z = true;
            }
            this.mCurrentIsDeleteMode = z;
        } else {
            this.mCurrentIsDeleteMode = false;
        }
        CamLog.m3d(TAG, "deletemode? = " + this.mCurrentIsDeleteMode);
        deleteOnOffInner();
    }

    public void deleteOff() {
        this.mCurrentIsDeleteMode = false;
        CamLog.m5e(TAG, "deletemode? = " + this.mCurrentIsDeleteMode);
        deleteOnOffInner();
    }

    private void deleteOnOffInner() {
        if (this.mCurrentIsDeleteMode) {
            int start = -1;
            int end = -1;
            int i = 0;
            while (i < this.mList.size()) {
                if (start == -1 && ((TabItem) this.mList.get(i)).mCanDeleted) {
                    start = i;
                }
                if (start != -1 && i >= start && ((TabItem) this.mList.get(i)).mCanDeleted) {
                    end = i;
                }
                i++;
            }
            notifyItemRangeChanged(start, (end - start) + 1);
            return;
        }
        notifyItemRangeChanged(0, this.mList.size());
    }

    public boolean getDeleteOnOff() {
        return this.mCurrentIsDeleteMode;
    }

    public String getSelectedStickerPackPath() {
        if (this.mList != null && this.mSelectedPosition < this.mList.size()) {
            return ((TabItem) this.mList.get(this.mSelectedPosition)).tab_path;
        }
        CamLog.m3d(TAG, "Check sticker contents");
        return null;
    }

    public boolean isRemainDeletableItem() {
        if (this.mList != null) {
            Iterator it = this.mList.iterator();
            while (it.hasNext()) {
                if (((TabItem) it.next()).mCanDeleted) {
                    return true;
                }
            }
        }
        deleteOff();
        return false;
    }

    public void clearAdapter() {
        if (this.mList != null) {
            this.mList.clear();
        }
        this.mList = null;
        this.mLoader = null;
    }
}
