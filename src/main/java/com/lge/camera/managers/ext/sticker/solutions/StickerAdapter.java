package com.lge.camera.managers.ext.sticker.solutions;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.p001v7.widget.RecyclerView.Adapter;
import android.support.p001v7.widget.RecyclerView.LayoutParams;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.managers.ext.sticker.AdapterInterface;
import com.lge.camera.managers.ext.sticker.StickerInformationDataClass;
import com.lge.camera.managers.ext.sticker.ViewHolder;
import com.lge.camera.managers.ext.sticker.ViewHolder.OnViewHolderClickListener;
import com.lge.camera.managers.ext.sticker.solutions.IStickerLoader.OnLoadCompleteListener;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class StickerAdapter extends Adapter<ViewHolder> implements OnLoadCompleteListener, AdapterInterface, OnViewHolderClickListener {
    protected static final int ITEM_LAYOUT = 2130968796;
    private static final String TAG = "StickerAdapter";
    private int dpUnit = 0;
    private int itemHeight = 0;
    protected Context mCtx;
    private int mDegree = 0;
    protected Handler mHandler = new Handler();
    protected OnStickerItemClickListener mListener;
    protected OnStickerItemLoadCompleteListener mLoadListener;
    protected IStickerLoader mLoader;
    protected int mSelectedPos = -1;
    protected ArrayList<StickerInformationDataClass> mStickerList;
    protected String pendingConfig = null;

    public interface OnStickerItemLoadCompleteListener {
        void onItemLoadComplete(ArrayList<StickerInformationDataClass> arrayList);
    }

    public interface OnStickerItemClickListener {
        void onItemClicked(StickerInformationDataClass stickerInformationDataClass, int i);
    }

    public int getItemCount() {
        if (this.mStickerList != null) {
            return this.mStickerList.size();
        }
        return 0;
    }

    public void onClicked(int pos) {
        int before = this.mSelectedPos;
        this.mSelectedPos = pos;
        notifyItemChanged(this.mSelectedPos);
        notifyItemChanged(before);
        if (this.mListener != null) {
            this.mListener.onItemClicked((StickerInformationDataClass) this.mStickerList.get(pos), pos);
        }
    }

    public StickerAdapter(Context ctx, OnStickerItemClickListener listener, int solution, int height) {
        this.mCtx = ctx;
        this.itemHeight = height;
        this.mStickerList = new ArrayList();
        this.dpUnit = this.mCtx.getResources().getDimensionPixelSize(C0088R.dimen.dp_unit);
        setOnStickerItemClickListener(listener);
        if (this.mLoader != null) {
            return;
        }
        if (solution == 2 || solution == 1) {
            this.mLoader = new StickerListFactory().createListLoader(this.mCtx, solution);
        } else {
            CamLog.m3d(TAG, "loader created in child class");
        }
    }

    public void setOnStickerItemClickListener(OnStickerItemClickListener listener) {
        this.mListener = listener;
    }

    public void load() {
    }

    public void load(String basePath) {
        this.mStickerList.clear();
        if (this.mLoader != null) {
            this.mLoader.setOnLoadCompleteListener(this);
            this.mLoader.setBasePath(basePath);
            this.mLoader.load(this.mHandler);
        }
    }

    public void setmLoadListener(OnStickerItemLoadCompleteListener loadCompleteListener) {
        if (loadCompleteListener != null) {
            this.mLoadListener = loadCompleteListener;
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder((RelativeLayout) LayoutInflater.from(this.mCtx).inflate(C0088R.layout.sticker_menu_item, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        CamLog.m5e(TAG, "onBindViewHolder position = " + position);
        CamLog.m5e(TAG, "onBindViewHolder mSelectedPos = " + this.mSelectedPos);
        relayout(holder);
        StickerInformationDataClass sid = (StickerInformationDataClass) this.mStickerList.get(position);
        if (sid.icon_image == null) {
            sid.icon_image = BitmapFactory.decodeFile(sid.icon_path);
        }
        holder.itemView.setContentDescription(sid.sticker_name);
        holder.mImageView.setImageBitmap(sid.icon_image);
        holder.mImageView.setDegree(this.mDegree, false);
        if (position == this.mSelectedPos) {
            holder.mSelectedImage.setVisibility(0);
        } else {
            holder.mSelectedImage.setVisibility(8);
        }
        holder.setOnViewHolderClickListener(this);
    }

    public void relayout(ViewHolder holder) {
        LayoutParams lp = (LayoutParams) holder.itemView.getLayoutParams();
        lp.width = this.itemHeight;
        lp.height = this.itemHeight;
        RotateImageView mImageView = holder.mImageView;
        RelativeLayout.LayoutParams ilp = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
        ilp.width = this.itemHeight - (this.dpUnit * 20);
        ilp.height = this.itemHeight - (this.dpUnit * 20);
        ImageView mSelected = holder.mSelectedImage;
        RelativeLayout.LayoutParams slp = (RelativeLayout.LayoutParams) mSelected.getLayoutParams();
        slp.width = this.itemHeight;
        slp.height = this.itemHeight;
        holder.itemView.setLayoutParams(lp);
        mImageView.setLayoutParams(ilp);
        mSelected.setLayoutParams(slp);
    }

    public void onLoadComplete() {
        this.mStickerList.clear();
        this.mStickerList = null;
        this.mStickerList = this.mLoader.getList();
        notifyDataSetChanged();
        if (this.pendingConfig != null) {
            setSelection(this.pendingConfig);
            this.pendingConfig = null;
        }
        if (this.mLoadListener != null) {
            this.mLoadListener.onItemLoadComplete(this.mStickerList);
            this.mLoadListener = null;
        }
    }

    public void setDegreeJustSet(int degree) {
        this.mDegree = degree;
    }

    public void setDegree(int degree) {
        boolean rotate;
        if (degree != this.mDegree) {
            rotate = true;
        } else {
            rotate = false;
        }
        this.mDegree = degree;
        if (rotate) {
            notifyItemRangeChanged(0, this.mStickerList.size());
        }
    }

    public StickerInformationDataClass getItem(int index) {
        if (this.mStickerList == null || this.mStickerList.size() <= index) {
            return null;
        }
        return (StickerInformationDataClass) this.mStickerList.get(index);
    }

    public void setSelection(String configPath) {
        if (this.mStickerList == null || this.mStickerList.size() <= 0) {
            CamLog.m3d(TAG, " pendingSelection");
            this.pendingConfig = configPath;
            return;
        }
        int selected = -1;
        for (int i = 0; i < this.mStickerList.size(); i++) {
            if (((StickerInformationDataClass) this.mStickerList.get(i)).configFile.equals(configPath)) {
                selected = i;
                break;
            }
        }
        this.mSelectedPos = selected;
        CamLog.m5e(TAG, "setSelection mSelectedPos = " + this.mSelectedPos);
        notifyItemRangeChanged(0, this.mStickerList.size());
    }

    public void clearSelection() {
        this.mSelectedPos = -1;
        CamLog.m5e(TAG, "clearSelection mSelectedPos = " + this.mSelectedPos);
        if (this.mLoader.isListLoaded()) {
            notifyItemRangeChanged(0, this.mStickerList.size());
        }
    }

    public void clearAdapter() {
        if (this.mStickerList != null) {
            this.mStickerList.clear();
        }
        this.mStickerList = null;
        this.mLoader = null;
    }
}
